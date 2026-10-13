import csv
import hashlib
from collections import Counter
from collections.abc import Iterator
from datetime import datetime, time
from pathlib import Path

from sqlalchemy import func, select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.orm import Session

from app.convert import KST
from app.models import LostReport, RescuedAnimal
from app.normalize import blank_to_none, gender_label, parse_date, short_region

BATCH = 2000


def read_csv(path: Path) -> Iterator[dict[str, str]]:
    with path.open(encoding="utf-8-sig", newline="") as f:
        yield from csv.DictReader(f)


def stable_id(prefix: str, *parts: str | None) -> str:
    return prefix + hashlib.md5("|".join(p or "" for p in parts).encode()).hexdigest()[:12]


def insert_batches(db: Session, model, rows: list[dict]) -> None:
    for start in range(0, len(rows), BATCH):
        db.execute(insert(model), rows[start:start + BATCH])


def _upsert(db: Session, model, rows: list[dict]) -> None:
    rows = list({r["id"]: r for r in rows}.values())
    for start in range(0, len(rows), BATCH):
        stmt = insert(model).values(rows[start:start + BATCH])
        updatable = {c.name: stmt.excluded[c.name] for c in model.__table__.columns if c.name != "id"}
        db.execute(stmt.on_conflict_do_update(index_elements=["id"], set_=updatable))


def upsert_rescued(db: Session, rows: list[dict]) -> None:
    """Insert or refresh rescued animals keyed by desertionNo."""
    _upsert(db, RescuedAnimal, rows)


def upsert_lost_reports(db: Session, rows: list[dict]) -> None:
    """Insert or refresh public lost reports keyed by lost_report_id()."""
    _upsert(db, LostReport, rows)


# --- 농식품부 분실동물 (CSV snapshot and lossInfo OpenAPI share these) -------------

def mask_name(name: str | None) -> str | None:
    """'이건희' -> '이○희', '김수' -> '김○', '남궁민수' -> '남○○수'."""
    name = blank_to_none(name)
    if name is None:
        return None
    if len(name) == 1:
        return name
    if len(name) == 2:
        return name[0] + "○"
    return name[0] + "○" * (len(name) - 2) + name[-1]


def lost_contact(call_name: str | None, call_tel: str | None) -> str | None:
    """Owner contact shown to finders: masked name + phone number."""
    parts = [p for p in (mask_name(call_name), blank_to_none(call_tel)) if p]
    return " ".join(parts) or None


def breed_species_map(db: Session) -> dict[str, str]:
    """Breed -> species learned from rescued animals, whose source data has both
    (the lost-animal data only has the breed, e.g. '레그돌')."""
    counts: dict[str, Counter] = {}
    rows = db.execute(
        select(RescuedAnimal.breed, RescuedAnimal.species, func.count())
        .where(RescuedAnimal.breed.is_not(None))
        .group_by(RescuedAnimal.breed, RescuedAnimal.species)
    )
    for breed, species, n in rows:
        counts.setdefault(breed, Counter())[species] += n
    return {breed: c.most_common(1)[0][0] for breed, c in counts.items()}


def lost_species(kind: str | None, breed_map: dict[str, str]) -> tuple[str, str | None]:
    """(species, breed) for a lost-animal kind name."""
    kind = (kind or "").strip()
    if kind in ("", "기타", "기타축종"):
        return "기타", None
    if kind == "고양이":
        return "고양이", None
    if kind in breed_map:
        return breed_map[kind], kind
    if "고양이" in kind or kind.endswith("묘"):
        return "고양이", kind
    return "개", kind


def lost_report_id(image: str | None, happen_dt: str | None, happen_addr: str | None,
                   kind: str | None, special_mark: str | None) -> str:
    """The data has no id; the photo URL is unique per report, otherwise hash the details."""
    if blank_to_none(image):
        return stable_id("pl_", image.strip())
    return stable_id("pl_", (happen_dt or "")[:10], happen_addr, kind, special_mark)


def lost_row(*, kind: str | None, sex: str | None, region: str | None, happen_dt: str | None,
             happen_addr: str | None, special_mark: str | None, color: str | None, age: str | None,
             image: str | None, call_name: str | None, call_tel: str | None,
             breed_map: dict[str, str]) -> dict:
    species, breed = lost_species(kind, breed_map)
    lost_date = parse_date(happen_dt)
    return {
        "id": lost_report_id(image, happen_dt, happen_addr, kind, special_mark),
        "source": "public",
        "user_id": None,
        "name": None,
        "species": species,
        "breed": breed,
        "gender": gender_label(sex),
        "region": short_region(region) or short_region(happen_addr),
        "lost_date": lost_date,
        # Color/age help the keyword part of matching (app/matching.py).
        "features": " · ".join(p for p in map(blank_to_none, (special_mark, color, age)) if p) or None,
        "contact_info": lost_contact(call_name, call_tel),
        "photo_url": blank_to_none(image),
        "reported_at": datetime.combine(lost_date, time(), KST) if lost_date else datetime.now(KST),
    }

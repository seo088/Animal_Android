"""Pull recent rescued animals from the 농림축산식품부 구조동물 조회 OpenAPI.

    python -m scripts.sync_rescued_animals --days 7

Needs ANIMAL_API_KEY (공공데이터포털 '구조동물 조회 서비스' Decoding key) in .env.
Run it on a schedule (e.g. hourly) so home stats like '오늘 구조' stay current.
"""

import argparse
from datetime import timedelta

import httpx
from sqlalchemy import select

from app.config import settings
from app.convert import now_kst
from app.db import Base, SessionLocal, engine
from app.models import Facility
from app.normalize import blank_to_none, gender_label, parse_date, short_region
from scripts.common import upsert_rescued

ENDPOINT = "https://apis.data.go.kr/1543061/abandonmentPublicService_v2/abandonmentPublic_v2"
PAGE_SIZE = 1000


def fetch(start: str, end: str) -> list[dict]:
    items, page = [], 1
    while True:
        response = httpx.get(ENDPOINT, timeout=30, params={
            "serviceKey": settings.animal_api_key, "bgnde": start, "endde": end,
            "pageNo": page, "numOfRows": PAGE_SIZE, "_type": "json",
        })
        response.raise_for_status()
        body = response.json()["response"]["body"]
        batch = (body.get("items") or {}).get("item") or []
        if isinstance(batch, dict):  # single result comes back as an object
            batch = [batch]
        items.extend(batch)
        if page * PAGE_SIZE >= int(body.get("totalCount", 0)) or not batch:
            return items
        page += 1


def _species_and_breed(item: dict) -> tuple[str, str | None]:
    # v2 has upKindNm/kindNm; v1 only kindCd like "[개] 믹스견".
    if item.get("upKindNm"):
        return item["upKindNm"], blank_to_none(item.get("kindNm"))
    kind = item.get("kindCd", "")
    if kind.startswith("[") and "]" in kind:
        return kind[1:kind.index("]")], blank_to_none(kind[kind.index("]") + 1:])
    return "기타", blank_to_none(kind)


def to_row(item: dict, shelter_coords: dict[str, tuple]) -> dict:
    species, breed = _species_and_breed(item)
    lat, lng = shelter_coords.get(item.get("careRegNo", ""), (None, None))
    return {
        "id": str(item["desertionNo"]),
        "notice_no": blank_to_none(item.get("noticeNo")),
        "species": "기타" if species.startswith("기타") else species,
        "breed": breed,
        "gender": gender_label(item.get("sexCd")),
        "region": short_region(item.get("orgNm")),
        "rescued_date": parse_date(item.get("happenDt")),
        "status": blank_to_none(item.get("processState")),
        "image_url": blank_to_none(item.get("popfile1") or item.get("popfile")),
        "features": blank_to_none(item.get("specialMark")),
        "latitude": lat,
        "longitude": lng,
        "care_name": blank_to_none(item.get("careNm")),
        "care_tel": blank_to_none(item.get("careTel")),
        "care_addr": blank_to_none(item.get("careAddr")),
    }


def sync(days: int) -> int:
    """Fetch the last `days` days and upsert them. Returns the number of items."""
    today = now_kst().date()
    items = fetch((today - timedelta(days=days)).strftime("%Y%m%d"), today.strftime("%Y%m%d"))

    Base.metadata.create_all(engine)
    with SessionLocal() as db:
        shelter_coords = {
            f.id.removeprefix("she_"): (f.latitude, f.longitude)
            for f in db.scalars(select(Facility).where(Facility.category == "SHELTER"))
        }
        upsert_rescued(db, [to_row(item, shelter_coords) for item in items])
        db.commit()
    return len(items)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--days", type=int, default=7, help="오늘부터 며칠 전까지 가져올지")
    args = parser.parse_args()
    if not settings.animal_api_key:
        raise SystemExit("ANIMAL_API_KEY가 .env에 없습니다.")
    print(f"synced {sync(args.days)} rescued animals")


if __name__ == "__main__":
    main()

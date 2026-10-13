"""Lost report <-> rescued animal matching.

Scoring mirrors MockLostAnimalRepository in the app so results stay consistent
when the app switches from the mock to this server."""

import re
from datetime import date

from sqlalchemy import or_, select
from sqlalchemy.orm import Session

from app import schemas
from app.models import RescuedAnimal
from app.normalize import canonical_breed, short_region

MAX_MATCHES = 6
MIN_SCORE = 40


def _regions_overlap(a: str, b: str) -> bool:
    return a in b or b in a


def _has_common_keyword(a: str, b: str) -> bool:
    return any(len(word) >= 2 and word in b for word in re.split(r"[\s,]+", a))


def _score(breed, gender, region, species, features, animal: RescuedAnimal) -> int:
    score = 30
    if breed and breed == animal.breed:
        score += 30
    if gender and gender == animal.gender:
        score += 10
    elif gender == "미상" or animal.gender == "미상":
        score += 4
    if region and animal.region and _regions_overlap(region, animal.region):
        score += 20
    if species and species == animal.species:
        score += 10
    if features and animal.features and _has_common_keyword(features, animal.features):
        score += 10
    return min(score, 95)


def _grade(score: int) -> str:
    if score >= 75:
        return "A"
    if score >= 55:
        return "B"
    return "C"


def find_matches(
    db: Session,
    *,
    species: str | None,
    breed: str | None,
    gender: str | None,
    region: str | None,
    lost_date: date | None,
    features: str | None,
) -> list[schemas.MatchResultOut]:
    if not species:
        return []
    region = short_region(region)
    breed = canonical_breed(breed)
    stmt = select(RescuedAnimal).where(
        RescuedAnimal.species == species,
        or_(RescuedAnimal.status.is_(None), RescuedAnimal.status.like("보호%")),
    )
    if lost_date:
        stmt = stmt.where(RescuedAnimal.rescued_date >= lost_date)

    scored = []
    for animal in db.scalars(stmt):
        score = _score(breed, gender, region, species, features, animal)
        if score >= MIN_SCORE:
            scored.append((score, animal))
    scored.sort(key=lambda pair: pair[0], reverse=True)

    return [
        schemas.MatchResultOut(
            rescued_animal_id=animal.id,
            breed=animal.breed,
            gender=animal.gender,
            found_region=animal.region,
            found_date=animal.rescued_date.isoformat() if animal.rescued_date else None,
            image_url=animal.image_url,
            grade=_grade(score),
            similarity_percent=score,
        )
        for score, animal in scored[:MAX_MATCHES]
    ]

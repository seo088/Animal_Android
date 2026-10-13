from fastapi import APIRouter, Depends
from sqlalchemy import func, or_, select
from sqlalchemy.orm import Session

from app import schemas
from app.convert import now_kst
from app.db import get_db
from app.models import APP_FACILITY_CATEGORIES, Facility, LostReport, Pet, RescuedAnimal, User
from app.security import current_user

router = APIRouter(prefix="/home", tags=["홈 대시보드"])


@router.get("/stats", response_model=schemas.HomeStatsOut)
def home_stats(db: Session = Depends(get_db)):
    count = lambda stmt: db.scalar(stmt) or 0  # noqa: E731
    return schemas.HomeStatsOut(
        protected_count=count(
            select(func.count()).select_from(RescuedAnimal)
            .where(or_(RescuedAnimal.status.is_(None), RescuedAnimal.status.like("보호%")))
        ),
        rescued_today_count=count(
            select(func.count()).select_from(RescuedAnimal).where(RescuedAnimal.rescued_date == now_kst().date())
        ),
        lost_report_count=count(select(func.count()).select_from(LostReport)),
        facility_count=count(
            select(func.count()).select_from(Facility).where(Facility.category.in_(APP_FACILITY_CATEGORIES))
        ),
    )


def _profile(user: User, pet: Pet | None) -> schemas.HomeProfileOut:
    return schemas.HomeProfileOut(
        user_display_name=user.display_name,
        user_photo_url=user.photo_url,
        pet_name=pet.name if pet else None,
        pet_breed=pet.breed if pet else None,
        pet_age=pet.age if pet else None,
    )


@router.get("/profile", response_model=schemas.HomeProfileOut)
def home_profile(user: User = Depends(current_user), db: Session = Depends(get_db)):
    return _profile(user, db.get(Pet, user.id))


@router.put("/pet", response_model=schemas.HomeProfileOut)
def upsert_pet(body: schemas.PetIn, user: User = Depends(current_user), db: Session = Depends(get_db)):
    pet = db.get(Pet, user.id) or Pet(user_id=user.id)
    pet.name, pet.breed, pet.age = body.name, body.breed, body.age
    db.add(pet)
    db.commit()
    return _profile(user, pet)

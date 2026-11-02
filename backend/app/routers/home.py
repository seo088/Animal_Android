import secrets

from fastapi import APIRouter, Depends, File, HTTPException, UploadFile
from sqlalchemy import func, or_, select
from sqlalchemy.orm import Session

from app import schemas
from app.convert import now_kst
from app.db import get_db
from app.models import APP_FACILITY_CATEGORIES, Facility, LostReport, Pet, RescuedAnimal, User
from app.security import current_user
from app.config import settings

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
        pet_species=pet.species if pet else None,
        pet_gender=pet.gender if pet else None,
        pet_neutered=pet.neutered if pet else None,
        pet_weight_kg=pet.weight_kg if pet else None,
        pet_birth_date=pet.birth_date.isoformat() if pet and pet.birth_date else None,
        pet_adoption_date=pet.adoption_date.isoformat() if pet and pet.adoption_date else None,
        pet_photo_url=(settings.public_base_url.rstrip("/") + pet.photo_url)
        if pet and pet.photo_url and pet.photo_url.startswith("/") else (pet.photo_url if pet else None),
    )


@router.get("/profile", response_model=schemas.HomeProfileOut)
def home_profile(user: User = Depends(current_user), db: Session = Depends(get_db)):
    return _profile(user, db.get(Pet, user.id))


@router.put("/pet", response_model=schemas.HomeProfileOut)
def upsert_pet(body: schemas.PetIn, user: User = Depends(current_user), db: Session = Depends(get_db)):
    pet = db.get(Pet, user.id) or Pet(user_id=user.id)
    pet.name, pet.breed, pet.age = body.name, body.breed, body.age
    pet.species, pet.gender, pet.neutered = body.species, body.gender, body.neutered
    pet.weight_kg = body.weight_kg
    from datetime import date
    pet.birth_date = date.fromisoformat(body.birth_date) if body.birth_date else None
    pet.adoption_date = date.fromisoformat(body.adoption_date) if body.adoption_date else None
    db.add(pet)
    db.commit()
    return _profile(user, pet)


@router.post("/pet/photo", response_model=schemas.HomeProfileOut)
def upload_pet_photo(photo: UploadFile = File(...), user: User = Depends(current_user), db: Session = Depends(get_db)):
    if photo.content_type not in {"image/jpeg", "image/png", "image/webp", "image/heic"}:
        raise HTTPException(415, "사진은 JPG, PNG, WEBP, HEIC만 업로드할 수 있습니다.")
    data = photo.file.read(10 * 1024 * 1024 + 1)
    if len(data) > 10 * 1024 * 1024:
        raise HTTPException(413, "사진은 10MB 이하만 업로드할 수 있습니다.")
    pet = db.get(Pet, user.id)
    if pet is None:
        raise HTTPException(404, "반려동물 정보를 먼저 등록해 주세요.")
    extension = {"image/jpeg": ".jpg", "image/png": ".png", "image/webp": ".webp", "image/heic": ".heic"}[photo.content_type]
    directory = settings.upload_path / "pets"
    directory.mkdir(parents=True, exist_ok=True)
    filename = f"{user.id}_{secrets.token_hex(4)}{extension}"
    (directory / filename).write_bytes(data)
    pet.photo_url = f"/uploads/pets/{filename}"
    db.commit()
    return _profile(user, pet)

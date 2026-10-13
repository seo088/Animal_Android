from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from app import schemas
from app.convert import animal_out
from app.db import get_db
from app.models import RescuedAnimal
from app.normalize import canonical_breed, is_all, short_region

router = APIRouter(prefix="/animals", tags=["구조동물 열람"])


@router.get("", response_model=list[schemas.RescuedAnimalOut])
def list_animals(
    breed: str | None = Query(None, description="부분 일치, '전체'는 무시"),
    region: str | None = Query(None, description="'서울' 또는 '서울 송파구' (전체 시도명도 허용)"),
    gender: str | None = Query(None, description="수컷 | 암컷 | 미상"),
    species: str | None = Query(None, description="개 | 고양이 | 기타"),
    page: int = Query(0, ge=0),
    size: int = Query(50, ge=1, le=500),
    db: Session = Depends(get_db),
):
    stmt = select(RescuedAnimal)
    if not is_all(breed):
        stmt = stmt.where(RescuedAnimal.breed.contains(canonical_breed(breed)))
    if not is_all(region):
        stmt = stmt.where(RescuedAnimal.region.startswith(short_region(region)))
    if not is_all(gender):
        stmt = stmt.where(RescuedAnimal.gender == gender.strip())
    if not is_all(species):
        stmt = stmt.where(RescuedAnimal.species == species.strip())
    stmt = (
        stmt.order_by(RescuedAnimal.rescued_date.desc().nulls_last(), RescuedAnimal.id.desc())
        .offset(page * size)
        .limit(size)
    )
    return [animal_out(a) for a in db.scalars(stmt)]


@router.get("/{animal_id}", response_model=schemas.RescuedAnimalOut)
def animal_detail(animal_id: str, db: Session = Depends(get_db)):
    animal = db.get(RescuedAnimal, animal_id)
    if animal is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "구조동물을 찾을 수 없습니다.")
    return animal_out(animal)

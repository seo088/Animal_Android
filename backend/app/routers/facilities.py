from enum import Enum

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import ColumnElement, literal, or_, select
from sqlalchemy.orm import Session

from app import schemas
from app.convert import facility_out
from app.db import get_db
from app.geo import bounding_box, distance_km
from app.models import APP_FACILITY_CATEGORIES, Facility
from app.normalize import ALL, is_all

router = APIRouter(prefix="/facilities", tags=["시설 지도 / 동반시설"])

FacilityCategory = Enum("FacilityCategory", {c: c for c in APP_FACILITY_CATEGORIES}, type=str)


def query_facilities(
    db: Session,
    filters: list[ColumnElement[bool]],
    lat: float | None,
    lng: float | None,
    radius_km: float,
    limit: int,
) -> list[tuple[Facility, float | None]]:
    """Filtered facilities; nearest-first within radius when a location is given."""
    if (lat is None) != (lng is None):
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "lat과 lng는 함께 보내야 합니다.")
    if lat is None:
        stmt = select(Facility, literal(None)).where(*filters).order_by(Facility.name).limit(limit)
    else:
        dist = distance_km(Facility.latitude, Facility.longitude, lat, lng)
        stmt = (
            select(Facility, dist)
            .where(*filters, bounding_box(Facility.latitude, Facility.longitude, lat, lng, radius_km), dist <= radius_km)
            .order_by(dist)
            .limit(limit)
        )
    return [(facility, distance) for facility, distance in db.execute(stmt)]


@router.get("", response_model=list[schemas.FacilityOut])
def list_facilities(
    category: FacilityCategory | None = None,
    lat: float | None = Query(None, ge=-90, le=90),
    lng: float | None = Query(None, ge=-180, le=180),
    radius_km: float = Query(10, alias="radiusKm", gt=0, le=300),
    limit: int = Query(200, ge=1, le=1000),
    db: Session = Depends(get_db),
):
    filters = [Facility.category == category.value] if category else [Facility.category.in_(APP_FACILITY_CATEGORIES)]
    return [facility_out(f, d) for f, d in query_facilities(db, filters, lat, lng, radius_km, limit)]


# Declared before /{facility_id} so "pet-friendly" isn't captured as an id.
@router.get("/pet-friendly", response_model=list[schemas.FacilityOut])
def pet_friendly_facilities(
    pet_type: str | None = Query(None, alias="petType", description="전체 | 개 | 고양이"),
    size: str | None = Query(None, description="전체 | 소형 (5kg 이하) | 중형 (5~15kg) | 대형 (15kg 이상)"),
    indoor: bool | None = None,
    carrier: bool | None = None,
    leash: bool | None = None,
    lat: float | None = Query(None, ge=-90, le=90),
    lng: float | None = Query(None, ge=-180, le=180),
    radius_km: float = Query(20, alias="radiusKm", gt=0, le=300),
    limit: int = Query(200, ge=1, le=1000),
    db: Session = Depends(get_db),
):
    """Same semantics as MockFacilityRepository.getPetFriendlyFacilities: a facility
    whose allowedPetType/sizeLimit is '전체' matches any requested value."""
    filters = [Facility.pet_friendly.is_(True), Facility.category.in_(APP_FACILITY_CATEGORIES)]
    if not is_all(pet_type):
        filters.append(or_(Facility.allowed_pet_type == ALL, Facility.allowed_pet_type == pet_type))
    if not is_all(size):
        filters.append(or_(Facility.size_limit == ALL, Facility.size_limit == size))
    for column, wanted in (
        (Facility.indoor_allowed, indoor),
        (Facility.carrier_required, carrier),
        (Facility.leash_required, leash),
    ):
        if wanted is not None:
            filters.append(column.is_(wanted))
    return [facility_out(f, d) for f, d in query_facilities(db, filters, lat, lng, radius_km, limit)]


@router.get("/{facility_id}", response_model=schemas.FacilityOut)
def facility_detail(facility_id: str, db: Session = Depends(get_db)):
    facility = db.get(Facility, facility_id)
    if facility is None or facility.category not in APP_FACILITY_CATEGORIES:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "시설을 찾을 수 없습니다.")
    return facility_out(facility)

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from app import schemas
from app.convert import hospital_out
from app.db import get_db
from app.models import Facility
from app.routers.facilities import query_facilities

router = APIRouter(prefix="/hospitals", tags=["증상 확인"])

# Hospitals are the HOSPITAL rows of the facilities table (행안부 동물병원),
# so ids are shared with /facilities/{id}.
_IS_HOSPITAL = [Facility.category == "HOSPITAL"]


@router.get("", response_model=list[schemas.HospitalOut])
def list_hospitals(
    lat: float | None = Query(None, ge=-90, le=90),
    lng: float | None = Query(None, ge=-180, le=180),
    radius_km: float = Query(10, alias="radiusKm", gt=0, le=300),
    limit: int = Query(100, ge=1, le=1000),
    db: Session = Depends(get_db),
):
    return [hospital_out(f, d) for f, d in query_facilities(db, _IS_HOSPITAL, lat, lng, radius_km, limit)]


@router.get("/nearby", response_model=list[schemas.HospitalOut])
def nearby_hospitals(
    lat: float = Query(..., ge=-90, le=90),
    lng: float = Query(..., ge=-180, le=180),
    radius_km: float = Query(5, alias="radiusKm", gt=0, le=100),
    limit: int = Query(20, ge=1, le=200),
    db: Session = Depends(get_db),
):
    return [hospital_out(f, d) for f, d in query_facilities(db, _IS_HOSPITAL, lat, lng, radius_km, limit)]


@router.get("/{hospital_id}", response_model=schemas.HospitalOut)
def hospital_detail(hospital_id: str, db: Session = Depends(get_db)):
    facility = db.get(Facility, hospital_id)
    if facility is None or facility.category != "HOSPITAL":
        raise HTTPException(status.HTTP_404_NOT_FOUND, "병원을 찾을 수 없습니다.")
    return hospital_out(facility)

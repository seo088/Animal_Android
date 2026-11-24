from sqlalchemy import func, select
from sqlalchemy.orm import Session

from fastapi import APIRouter, Depends

from app import schemas
from app.config import settings
from app.db import get_db
from app.models import Facility, LostReport, RescuedAnimal

router = APIRouter(prefix="/data-status", tags=["공공데이터 상태"])


@router.get("", response_model=schemas.PublicDataStatusOut)
def get_public_data_status(db: Session = Depends(get_db)):
    """Expose only factual, non-sensitive metadata for the public-data screen."""
    rescued_count, latest_rescued_date = db.execute(
        select(func.count(RescuedAnimal.id), func.max(RescuedAnimal.rescued_date))
    ).one()
    public_lost_count, latest_lost_date = db.execute(
        select(func.count(LostReport.id), func.max(LostReport.lost_date))
        .where(LostReport.source == "public")
    ).one()

    return schemas.PublicDataStatusOut(
        facility_count=db.scalar(select(func.count(Facility.id))) or 0,
        rescued_animal_count=rescued_count or 0,
        public_lost_report_count=public_lost_count or 0,
        latest_rescued_date=latest_rescued_date.isoformat() if latest_rescued_date else None,
        latest_lost_date=latest_lost_date.isoformat() if latest_lost_date else None,
        public_data_sync_configured=bool(settings.animal_api_key),
    )

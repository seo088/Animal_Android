from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from app import schemas
from app.convert import LOST_ALERT_PREFIX, RESCUE_ALERT_PREFIX, lost_alert, rescue_alert
from app.db import get_db
from app.models import LostReport, RescuedAnimal

router = APIRouter(prefix="/alerts", tags=["홈 대시보드"])


@router.get("", response_model=list[schemas.AlertOut])
def recent_alerts(limit: int = Query(20, ge=1, le=100), db: Session = Depends(get_db)):
    """Latest rescues (농식품부) and lost reports (public + user), newest first."""
    animals = db.scalars(
        select(RescuedAnimal)
        .order_by(RescuedAnimal.rescued_date.desc().nulls_last(), RescuedAnimal.id.desc())
        .limit(limit)
    )
    reports = db.scalars(
        select(LostReport)
        .order_by(LostReport.lost_date.desc().nulls_last(), LostReport.reported_at.desc())
        .limit(limit)
    )
    rescues = [rescue_alert(a) for a in animals]
    losts = [lost_alert(r) for r in reports]
    # Rescues outnumber lost reports ~40:1 and dates have no time of day, so a plain
    # date sort fills the feed with rescues. Reserve up to half for lost reports.
    lost_quota = min(len(losts), limit // 2)
    alerts = losts[:lost_quota] + rescues[:limit - lost_quota]
    if len(alerts) < limit:
        alerts += losts[lost_quota:limit - len(alerts) + lost_quota]
    alerts.sort(key=lambda a: a.occurred_at or "", reverse=True)
    return alerts[:limit]


@router.get("/{alert_id}", response_model=schemas.AlertOut)
def alert_detail(alert_id: str, db: Session = Depends(get_db)):
    if alert_id.startswith(RESCUE_ALERT_PREFIX):
        animal = db.get(RescuedAnimal, alert_id.removeprefix(RESCUE_ALERT_PREFIX))
        if animal:
            return rescue_alert(animal)
    elif alert_id.startswith(LOST_ALERT_PREFIX):
        report = db.get(LostReport, alert_id.removeprefix(LOST_ALERT_PREFIX))
        if report:
            return lost_alert(report)
    raise HTTPException(status.HTTP_404_NOT_FOUND, "알림을 찾을 수 없습니다.")

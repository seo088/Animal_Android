"""ORM row -> response schema conversions shared across routers."""

import re
from datetime import datetime, time, timedelta, timezone

from app import schemas
from app.config import settings
from app.hours import is_open as weekly_is_open
from app.models import Facility, LostReport, RescuedAnimal

# Fixed offset instead of ZoneInfo: Windows has no system tz database without tzdata.
KST = timezone(timedelta(hours=9))

_HOURS_RANGE = re.compile(r"(\d{1,2}):(\d{2})\s*[-~]\s*(\d{1,2}):(\d{2})")


def now_kst() -> datetime:
    return datetime.now(KST)


def is_open_now(hours: str | None, opening_hours: dict | None = None, now: datetime | None = None) -> bool:
    """Weekly schedule when known, else best effort on 'HH:MM-HH:MM' / '24시간' text.
    Unknown hours count as closed."""
    if opening_hours:
        return weekly_is_open(opening_hours, now or now_kst())
    if not hours:
        return False
    if "24시" in hours:
        return True
    match = _HOURS_RANGE.search(hours)
    if not match:
        return False
    h1, m1, h2, m2 = (int(g) for g in match.groups())
    current = (now or now_kst()).time()
    start = time(h1 % 24, m1)
    end = time(23, 59, 59) if h2 >= 24 else time(h2, m2)
    if start <= end:
        return start <= current <= end
    return current >= start or current <= end  # overnight range


def round_km(distance: float | None) -> float | None:
    return None if distance is None else round(distance, 2)


def facility_out(facility: Facility, distance: float | None = None) -> schemas.FacilityOut:
    return schemas.FacilityOut(
        id=facility.id,
        name=facility.name,
        category=facility.category,
        address=facility.address,
        phone=facility.phone,
        hours=facility.hours,
        latitude=facility.latitude,
        longitude=facility.longitude,
        distance_km=round_km(distance),
        image_url=facility.image_url,
        description=facility.description,
        pet_friendly=facility.pet_friendly,
        allowed_pet_type=facility.allowed_pet_type,
        size_limit=facility.size_limit,
        indoor_allowed=facility.indoor_allowed,
        carrier_required=facility.carrier_required,
        leash_required=facility.leash_required,
    )


def hospital_out(facility: Facility, distance: float | None = None) -> schemas.HospitalOut:
    return schemas.HospitalOut(
        id=facility.id,
        name=facility.name,
        address=facility.address,
        phone=facility.phone,
        hours=facility.hours,
        latitude=facility.latitude,
        longitude=facility.longitude,
        distance_km=round_km(distance),
        open_now=is_open_now(facility.hours, facility.opening_hours),
        description=facility.description,
    )


def animal_out(animal: RescuedAnimal) -> schemas.RescuedAnimalOut:
    return schemas.RescuedAnimalOut(
        id=animal.id,
        name=animal.notice_no,
        species=animal.species,
        breed=animal.breed,
        gender=animal.gender,
        region=animal.region,
        rescued_date=animal.rescued_date.isoformat() if animal.rescued_date else None,
        protection_status=animal.status,
        image_url=animal.image_url,
        features=animal.features,
        latitude=animal.latitude,
        longitude=animal.longitude,
    )


def photo_url(stored: str | None) -> str | None:
    """User uploads are stored as '/uploads/...' and need the public base URL."""
    if stored and stored.startswith("/"):
        return settings.public_base_url.rstrip("/") + stored
    return stored


def lost_report_out(report: LostReport) -> schemas.LostReportOut:
    return schemas.LostReportOut(
        id=report.id,
        name=report.name,
        species=report.species,
        breed=report.breed,
        gender=report.gender,
        region=report.region,
        lost_date=report.lost_date.isoformat() if report.lost_date else None,
        features=report.features,
        contact_info=report.contact_info,
        photo_path=photo_url(report.photo_url),
        reported_at=int(report.reported_at.timestamp() * 1000),
    )


RESCUE_ALERT_PREFIX = "R-"
LOST_ALERT_PREFIX = "L-"


def rescue_alert(animal: RescuedAnimal) -> schemas.AlertOut:
    return schemas.AlertOut(
        id=RESCUE_ALERT_PREFIX + animal.id,
        type="RESCUE",
        animal_type=animal.breed or animal.species,
        region=animal.region,
        occurred_at=animal.rescued_date.isoformat() if animal.rescued_date else None,
        status=animal.status,
        image_url=animal.image_url,
        related_item_id=animal.id,
    )


def lost_alert(report: LostReport) -> schemas.AlertOut:
    occurred = report.lost_date.isoformat() if report.lost_date else report.reported_at.astimezone(KST).strftime("%Y-%m-%d %H:%M")
    return schemas.AlertOut(
        id=LOST_ALERT_PREFIX + report.id,
        type="LOST",
        animal_type=report.breed or report.species,
        region=report.region,
        occurred_at=occurred,
        status="실종 신고",
        image_url=photo_url(report.photo_url),
        related_item_id=report.id,
    )

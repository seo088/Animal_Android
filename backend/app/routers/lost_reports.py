import secrets

from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile, status
from sqlalchemy.orm import Session

from app import schemas
from app.config import settings
from app.convert import lost_report_out
from app.db import get_db
from app.matching import find_matches
from app.models import LostReport, User
from app.normalize import blank_to_none, parse_date, short_region
from app.security import optional_user

router = APIRouter(prefix="/lost-reports", tags=["분실신고"])

_IMAGE_EXTENSIONS = {"image/jpeg": ".jpg", "image/png": ".png", "image/webp": ".webp", "image/heic": ".heic"}
_MAX_PHOTO_BYTES = 10 * 1024 * 1024


def _save_photo(report_id: str, photo: UploadFile) -> str:
    extension = _IMAGE_EXTENSIONS.get(photo.content_type or "")
    if extension is None:
        raise HTTPException(status.HTTP_415_UNSUPPORTED_MEDIA_TYPE, "사진은 JPG, PNG, WEBP, HEIC만 업로드할 수 있습니다.")
    data = photo.file.read(_MAX_PHOTO_BYTES + 1)
    if len(data) > _MAX_PHOTO_BYTES:
        raise HTTPException(status.HTTP_413_REQUEST_ENTITY_TOO_LARGE, "사진은 10MB 이하만 업로드할 수 있습니다.")
    directory = settings.upload_path / "lost"
    directory.mkdir(parents=True, exist_ok=True)
    filename = report_id + extension
    (directory / filename).write_bytes(data)
    return f"/uploads/lost/{filename}"


@router.post("", response_model=schemas.LostReportOut, status_code=status.HTTP_201_CREATED)
def submit_report(
    species: str = Form(...),
    name: str | None = Form(None),
    breed: str | None = Form(None),
    gender: str | None = Form(None),
    region: str | None = Form(None),
    lost_date: str | None = Form(None, alias="lostDate"),
    features: str | None = Form(None),
    contact_info: str | None = Form(None, alias="contactInfo"),
    photo: UploadFile | None = File(None),
    user: User | None = Depends(optional_user),
    db: Session = Depends(get_db),
):
    """multipart/form-data. `photo` is optional; returned as an absolute URL in photoPath."""
    report_id = "lr_" + secrets.token_hex(6)
    report = LostReport(
        id=report_id,
        source="user",
        user_id=user.id if user else None,
        name=blank_to_none(name),
        species=species.strip(),
        breed=blank_to_none(breed),
        gender=blank_to_none(gender),
        region=short_region(region),
        lost_date=parse_date(lost_date),
        features=blank_to_none(features),
        contact_info=blank_to_none(contact_info),
        photo_url=_save_photo(report_id, photo) if photo and photo.filename else None,
    )
    db.add(report)
    db.commit()
    db.refresh(report)
    return lost_report_out(report)


@router.post("/matches", response_model=list[schemas.MatchResultOut])
def match_unsaved_report(body: schemas.LostReportIn, db: Session = Depends(get_db)):
    """Match without saving — the app calls findMatches with the in-memory report."""
    return find_matches(
        db,
        species=body.species,
        breed=body.breed,
        gender=body.gender,
        region=body.region,
        lost_date=parse_date(body.lost_date),
        features=body.features,
    )


@router.get("/{report_id}", response_model=schemas.LostReportOut)
def report_detail(report_id: str, db: Session = Depends(get_db)):
    report = db.get(LostReport, report_id)
    if report is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "분실신고를 찾을 수 없습니다.")
    return lost_report_out(report)


@router.get("/{report_id}/matches", response_model=list[schemas.MatchResultOut])
def match_saved_report(report_id: str, db: Session = Depends(get_db)):
    report = db.get(LostReport, report_id)
    if report is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "분실신고를 찾을 수 없습니다.")
    return find_matches(
        db,
        species=report.species,
        breed=report.breed,
        gender=report.gender,
        region=report.region,
        lost_date=report.lost_date,
        features=report.features,
    )

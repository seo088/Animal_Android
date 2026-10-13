from datetime import date, datetime

from sqlalchemy import Boolean, Date, DateTime, Float, ForeignKey, Index, String, Text, func
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.orm import Mapped, mapped_column

from app.db import Base

# Categories the Android FacilityCategory enum understands. Rows with any other
# category (e.g. SHOPPING) are stored but never returned, since Gson would map an
# unknown enum constant to null.
APP_FACILITY_CATEGORIES = ("HOSPITAL", "PHARMACY", "SHELTER", "RESTAURANT", "CAFE", "HOTEL", "TOURISM")


class User(Base):
    __tablename__ = "users"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    email: Mapped[str] = mapped_column(String(255), unique=True, index=True)
    display_name: Mapped[str] = mapped_column(String(100))
    password_hash: Mapped[str] = mapped_column(String(255))
    photo_url: Mapped[str | None] = mapped_column(Text)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class Pet(Base):
    __tablename__ = "pets"

    user_id: Mapped[str] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    name: Mapped[str] = mapped_column(String(100))
    breed: Mapped[str | None] = mapped_column(String(100))
    age: Mapped[str | None] = mapped_column(String(50))


class Facility(Base):
    __tablename__ = "facilities"
    __table_args__ = (Index("ix_facilities_lat_lng", "latitude", "longitude"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True)
    source: Mapped[str] = mapped_column(String(32), index=True)
    category: Mapped[str] = mapped_column(String(32), index=True)
    name: Mapped[str] = mapped_column(String(255))
    address: Mapped[str | None] = mapped_column(Text)
    region: Mapped[str | None] = mapped_column(String(100), index=True)
    phone: Mapped[str | None] = mapped_column(String(100))
    hours: Mapped[str | None] = mapped_column(String(255))
    opening_hours: Mapped[dict | None] = mapped_column(JSONB)  # see app/hours.py
    latitude: Mapped[float | None] = mapped_column(Float)
    longitude: Mapped[float | None] = mapped_column(Float)
    image_url: Mapped[str | None] = mapped_column(Text)
    description: Mapped[str | None] = mapped_column(Text)
    pet_friendly: Mapped[bool] = mapped_column(Boolean, default=False, index=True)
    allowed_pet_type: Mapped[str | None] = mapped_column(String(50))
    size_limit: Mapped[str | None] = mapped_column(String(50))
    indoor_allowed: Mapped[bool | None] = mapped_column(Boolean)
    carrier_required: Mapped[bool | None] = mapped_column(Boolean)
    leash_required: Mapped[bool | None] = mapped_column(Boolean)


class RescuedAnimal(Base):
    __tablename__ = "rescued_animals"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)  # 유기번호(desertionNo)
    notice_no: Mapped[str | None] = mapped_column(String(64))
    species: Mapped[str] = mapped_column(String(20), index=True)
    breed: Mapped[str | None] = mapped_column(String(100), index=True)
    gender: Mapped[str] = mapped_column(String(10), index=True)
    region: Mapped[str | None] = mapped_column(String(100), index=True)
    rescued_date: Mapped[date | None] = mapped_column(Date, index=True)
    status: Mapped[str | None] = mapped_column(String(50), index=True)
    image_url: Mapped[str | None] = mapped_column(Text)
    features: Mapped[str | None] = mapped_column(Text)
    latitude: Mapped[float | None] = mapped_column(Float)
    longitude: Mapped[float | None] = mapped_column(Float)
    care_name: Mapped[str | None] = mapped_column(String(255))
    care_tel: Mapped[str | None] = mapped_column(String(100))
    care_addr: Mapped[str | None] = mapped_column(Text)


class LostReport(Base):
    __tablename__ = "lost_reports"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    source: Mapped[str] = mapped_column(String(16), index=True)  # "user" | "public"
    user_id: Mapped[str | None] = mapped_column(ForeignKey("users.id", ondelete="SET NULL"))
    name: Mapped[str | None] = mapped_column(String(100))
    species: Mapped[str | None] = mapped_column(String(20))
    breed: Mapped[str | None] = mapped_column(String(100))
    gender: Mapped[str | None] = mapped_column(String(10))
    region: Mapped[str | None] = mapped_column(String(100))
    lost_date: Mapped[date | None] = mapped_column(Date, index=True)
    features: Mapped[str | None] = mapped_column(Text)
    contact_info: Mapped[str | None] = mapped_column(String(255))
    photo_url: Mapped[str | None] = mapped_column(Text)
    reported_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), index=True)


class Favorite(Base):
    __tablename__ = "favorites"

    user_id: Mapped[str] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    key: Mapped[str] = mapped_column(String(128), primary_key=True)

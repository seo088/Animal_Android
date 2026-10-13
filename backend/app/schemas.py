"""Response/request bodies. Field names serialize as camelCase so they map 1:1 onto
the Android models in app/src/main/java/com/animalloo/data/model via Gson."""

from typing import Literal

from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel


class CamelModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


# --- auth -------------------------------------------------------------------

class UserOut(CamelModel):
    id: str
    display_name: str
    email: str


class AuthResponse(CamelModel):
    token: str
    user: UserOut


class LoginRequest(CamelModel):
    email: str
    password: str


class SignUpRequest(CamelModel):
    display_name: str = Field(min_length=1, max_length=100)
    email: str = Field(min_length=1, max_length=255)
    password: str = Field(min_length=4, max_length=128)


# --- home -------------------------------------------------------------------

class HomeStatsOut(CamelModel):
    protected_count: int
    rescued_today_count: int
    lost_report_count: int
    facility_count: int


class HomeProfileOut(CamelModel):
    user_display_name: str
    user_photo_url: str | None
    pet_name: str | None
    pet_breed: str | None
    pet_age: str | None


class PetIn(CamelModel):
    name: str = Field(min_length=1, max_length=100)
    breed: str | None = None
    age: str | None = None


# --- alerts -----------------------------------------------------------------

class AlertOut(CamelModel):
    id: str
    type: Literal["RESCUE", "LOST"]
    animal_type: str | None
    region: str | None
    occurred_at: str | None
    status: str | None
    image_url: str | None
    related_item_id: str


# --- facilities / hospitals ---------------------------------------------------

class FacilityOut(CamelModel):
    id: str
    name: str
    category: str
    address: str | None
    phone: str | None
    hours: str | None
    latitude: float | None
    longitude: float | None
    distance_km: float | None
    image_url: str | None
    description: str | None
    pet_friendly: bool
    allowed_pet_type: str | None
    size_limit: str | None
    indoor_allowed: bool | None
    carrier_required: bool | None
    leash_required: bool | None


class HospitalOut(CamelModel):
    id: str
    name: str
    address: str | None
    phone: str | None
    hours: str | None
    latitude: float | None
    longitude: float | None
    distance_km: float | None
    open_now: bool
    description: str | None


# --- diagnosis ----------------------------------------------------------------

class SymptomOut(CamelModel):
    id: str
    name: str
    category: str | None


class InferRequest(CamelModel):
    symptom_ids: list[str] = Field(min_length=1, max_length=30)


class DiagnosisResultOut(CamelModel):
    id: str
    disease_name: str
    probability_percent: int
    related_symptoms: list[str]
    description: str
    caution: str


# --- rescued animals / lost reports -----------------------------------------------

class RescuedAnimalOut(CamelModel):
    id: str
    name: str | None
    species: str
    breed: str | None
    gender: str
    region: str | None
    rescued_date: str | None
    protection_status: str | None
    image_url: str | None
    features: str | None
    latitude: float | None
    longitude: float | None


class LostReportIn(CamelModel):
    name: str | None = None
    species: str
    breed: str | None = None
    gender: str | None = None
    region: str | None = None
    lost_date: str | None = None
    features: str | None = None
    contact_info: str | None = None


class LostReportOut(CamelModel):
    id: str
    name: str | None
    species: str | None
    breed: str | None
    gender: str | None
    region: str | None
    lost_date: str | None
    features: str | None
    contact_info: str | None
    # The app model calls this photoPath; on the server side it is a URL.
    photo_path: str | None
    reported_at: int  # epoch millis, matches LostAnimalReport.reportedAt


class MatchResultOut(CamelModel):
    rescued_animal_id: str
    breed: str | None
    gender: str
    found_region: str | None
    found_date: str | None
    image_url: str | None
    grade: Literal["A", "B", "C"]
    similarity_percent: int

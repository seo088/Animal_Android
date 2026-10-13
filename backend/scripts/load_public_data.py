"""Load the already-downloaded public data snapshots into PostgreSQL.

    python -m scripts.load_public_data --data-dir C:/Users/jwon/Downloads/preprocessed_data

Re-runnable: facility rows and public lost reports are replaced per source,
rescued animals are upserted. Creates tables and the demo account (admin / 1234).
"""

import argparse
import json
import math
import re
from pathlib import Path

from sqlalchemy import delete, select, text

from app.db import Base, SessionLocal, engine
from app.hours import format_hours, parse_opr_time
from app.models import Facility, LostReport, RescuedAnimal, User
from app.normalize import ALL, blank_to_none, gender_label, parse_date, short_region, to_float
from app.security import hash_password
from scripts.common import (
    breed_species_map,
    insert_batches,
    lost_row,
    read_csv,
    stable_id,
    upsert_lost_reports,
    upsert_rescued,
)


def facility(id, source, category, name, address, **extra) -> dict:
    row = {c.name: None for c in Facility.__table__.columns}
    row.update(id=id, source=source, category=category, name=name.strip(),
               address=blank_to_none(address), region=short_region(address), pet_friendly=False)
    row.update(extra)
    return row


# --- 행정안전부 LOCALDATA: 동물병원 / 동물약국 ------------------------------------

def hospitals(d: Path) -> list[dict]:
    return [
        facility(f"hos_{r['Facility_ID']}", "mois_hospital", "HOSPITAL", r["Name"],
                 r["RoadAddress"] or r["LotNumber"], phone=blank_to_none(r["PhoneNumber"]),
                 latitude=to_float(r["lat"]), longitude=to_float(r["lon"]), description="동물병원")
        for r in read_csv(d / "01_hospital.csv") if r["BusinessStatus"].startswith("영업")
    ]


def _hospital_name_key(name: str) -> str:
    return re.sub(r"[\s()\[\]·.,-]|주식회사|\(주\)", "", name or "").replace("동물의료센터", "동물병원")


def _road_key(address: str) -> str | None:
    """'경기도 부천시 원미구 길주로 281 (중동)' -> '부천시 원미구 길주로' style key."""
    tokens = re.sub(r"\(.*?\)", "", address or "").split(",")[0].split()
    return " ".join(tokens[1:4]) if len(tokens) >= 4 else None


def attach_hospital_hours(rows: list[dict], hours_file: Path) -> int:
    """Join 동물병원_전국 OPR_TIME_INFO onto 행안부 hospitals by name within 1 km,
    falling back to a unique road-address match. Returns the number joined."""
    by_name: dict[str, list[dict]] = {}
    by_road: dict[str | None, list[dict]] = {}
    for row in rows:
        by_name.setdefault(_hospital_name_key(row["name"]), []).append(row)
        by_road.setdefault(_road_key(row["address"]), []).append(row)

    joined = 0
    for src in read_csv(hours_file):
        opening_hours = parse_opr_time(src["OPR_TIME_INFO"])
        if opening_hours is None:
            continue
        lat, lng = to_float(src["LA_VLUE"]), to_float(src["LO_VLUE"])
        candidates = [
            r for r in by_name.get(_hospital_name_key(src["FRNM_NM"]), [])
            if lat and r["latitude"] and math.hypot((lat - r["latitude"]) * 111, (lng - r["longitude"]) * 88) < 1.0
        ]
        if len(candidates) != 1:
            road = _road_key(src["RN_ADDR"])
            candidates = by_road.get(road, []) if road else []
        if len(candidates) == 1 and candidates[0]["opening_hours"] is None:
            candidates[0]["opening_hours"] = opening_hours
            candidates[0]["hours"] = format_hours(opening_hours)
            joined += 1
    return joined


def pharmacies(d: Path) -> list[dict]:
    return [
        facility(f"pha_{r['Facility_ID']}", "mois_pharmacy", "PHARMACY", r["Name"],
                 r["RoadAddress"] or r["LotNumber"], phone=blank_to_none(r["PhoneNumber"]),
                 latitude=to_float(r["lat"]), longitude=to_float(r["lon"]), description="동물약국")
        for r in read_csv(d / "02_pharmacy.csv") if r["BusinessStatus"].startswith("영업")
    ]


# --- 농림축산식품부: 동물보호센터 ----------------------------------------------------

def shelters(d: Path) -> list[dict]:
    rows = []
    for r in read_csv(d / "10_shelter.csv"):
        hours = f"{r['WeekOprStime']}-{r['WeekOprEtime']}" if r["WeekOprStime"] and r["WeekOprEtime"] else None
        description = " · ".join(filter(None, [r["DivisionNm"], f"보호대상: {r['SaveTrgtAnimal']}" if r["SaveTrgtAnimal"] else None]))
        rows.append(facility(f"she_{r['CareRegNo']}", "mafra_shelter", "SHELTER", r["CareNm"], r["CareAddr"],
                             phone=blank_to_none(r["CareTel"]), hours=hours, latitude=to_float(r["lat"]),
                             longitude=to_float(r["lon"]), description=description or "동물보호센터"))
    return rows


# --- 한국문화정보원: 반려동물 동반 가능 문화시설 ----------------------------------------

# 동물병원/동물약국 rows duplicate the 행안부 data, 반려동물용품 has no app category.
CULTURE_CATEGORY = {"카페": "CAFE", "미술관": "TOURISM"}


def culture(d: Path) -> list[dict]:
    rows = []
    for r in read_csv(d / "11_culture.csv"):
        category = CULTURE_CATEGORY.get(r["Category3"])
        if category is None:
            continue
        restriction = r["PetRestriction"] if r["PetRestriction"] not in ("제한사항 없음", "해당없음") else None
        rows.append(facility(
            stable_id("cul_", r["Name"], r["RoadAddress"]), "kcisa_culture", category, r["Name"], r["RoadAddress"],
            phone=blank_to_none(r["PhoneNumber"]), hours=blank_to_none(r["OpeningHours"]),
            latitude=to_float(r["lat"]), longitude=to_float(r["lon"]),
            description=" · ".join(filter(None, [r["Category3"], restriction])),
            pet_friendly=r["PetAllowedInfo"] == "Y",
            allowed_pet_type=ALL,
            size_limit=ALL if r["AllowedPetSize"] == "모두 가능" else None,
            indoor_allowed=r["IsIndoor"] == "Y",
            leash_required=True if restriction and "목줄" in restriction else None,
        ))
    return rows


# --- 한국관광공사: 반려동물 동반여행 (목록 API 스냅샷) ---------------------------------

def _travel_category(item: dict) -> str | None:
    content_type = item["contenttypeid"]
    if content_type == "39":
        return "CAFE" if item.get("lclsSystm2") == "FD05" else "RESTAURANT"
    if content_type == "32":
        return "HOTEL"
    if content_type in ("12", "14", "28"):
        return "TOURISM"
    return None  # 38 쇼핑 etc. — no app category


def travel(d: Path) -> list[dict]:
    rows = []
    for item in json.loads((d / "07_travel.json").read_text(encoding="utf-8")):
        category = _travel_category(item)
        if category is None:
            continue
        address = " ".join(filter(None, [item.get("addr1"), item.get("addr2")]))
        # Pet conditions (크기·실내·목줄) come from detailPetTour, which isn't in the snapshot yet.
        rows.append(facility(
            f"tour_{item['contentid']}", "kto_travel", category, item["title"], address,
            phone=blank_to_none(item.get("tel")), latitude=to_float(item.get("mapy")),
            longitude=to_float(item.get("mapx")), image_url=blank_to_none(item.get("firstimage")),
            description="한국관광공사 반려동물 동반여행", pet_friendly=True,
        ))
    return rows


# --- 한국도로공사: 휴게소 반려동물 놀이터 -----------------------------------------------

def rest_areas(d: Path) -> list[dict]:
    rows = []
    for r in read_csv(d / "12_restarea.csv"):
        description = " · ".join(filter(None, [f"고속도로 휴게소 반려동물 {r['FacilityType']}", r["Location"], r["Remark"]]))
        rows.append(facility(
            stable_id("rest_", r["Name"], r["FacilityType"], r["Location"], r["lat"], r["lon"]), "ex_restarea", "TOURISM", f"{r['Name']} 휴게소 반려동물 {r['FacilityType']}",
            f"{r['Sido']} {r['Sigungu']}", hours=blank_to_none(r["OpeningHours"]),
            latitude=to_float(r["lat"]), longitude=to_float(r["lon"]), description=description,
            pet_friendly=True, allowed_pet_type="개", size_limit=ALL, indoor_allowed=False,
        ))
    return rows


FACILITY_LOADERS = {
    "mois_hospital": hospitals,
    "mois_pharmacy": pharmacies,
    "mafra_shelter": shelters,
    "kcisa_culture": culture,
    "kto_travel": travel,
    "ex_restarea": rest_areas,
}


# --- 농림축산식품부: 구조동물 / 분실동물 --------------------------------------------------

def rescued_animals(d: Path) -> list[dict]:
    shelter_coords = {
        r["CareRegNo"]: (to_float(r["lat"]), to_float(r["lon"])) for r in read_csv(d / "10_shelter.csv")
    }
    rows = []
    for r in read_csv(d / "08_abandoned_animal.csv"):
        lat, lng = shelter_coords.get(r["CareRegNo"], (None, None))
        rows.append({
            "id": r["DesertionNo"],
            "notice_no": blank_to_none(r["NoticeNo"]),
            "species": r["UpKindName"] or "기타",
            "breed": blank_to_none(r["KindName"]),
            "gender": gender_label(r["SexCode"]),
            "region": short_region(r["OrgNm"]),
            "rescued_date": parse_date(r["HappenDate"]),
            "status": blank_to_none(r["ProcessState"]),
            "image_url": blank_to_none(r["ImageMain"]),
            "features": blank_to_none(r["SpecialMark"]),
            "latitude": lat,
            "longitude": lng,
            "care_name": blank_to_none(r["CareNm"]),
            "care_tel": blank_to_none(r["CareTel"]),
            "care_addr": blank_to_none(r["CareAddr"]),
        })
    return rows


def public_lost_reports(d: Path, breed_map: dict[str, str]) -> list[dict]:
    rows = [
        lost_row(
            kind=r["KindCode"], sex=r["SexCode"], region=f"{r['Sido']} {r['Sigungu']}",
            happen_dt=r["HappenDate"], happen_addr=r["HappenAddr"], special_mark=r["SpecialMark"],
            color=r["ColorCode"], age=r["Age"], image=r["Image"],
            call_name=r["CallName"], call_tel=r["CallTel"], breed_map=breed_map,
        )
        for r in read_csv(d / "09_lost_animal.csv")
    ]
    return list({row["id"]: row for row in rows}.values())


def seed_demo_user(db) -> None:
    if db.scalar(select(User).where(User.email == "admin")) is None:
        db.add(User(id="usr_admin", email="admin", display_name="관리자", password_hash=hash_password("1234")))


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--data-dir", type=Path, required=True, help="preprocessed_data 폴더")
    parser.add_argument("--hospital-hours", type=Path, help="동물병원_전국.csv (진료시간 OPR_TIME_INFO)")
    args = parser.parse_args()

    Base.metadata.create_all(engine)
    with SessionLocal() as db:
        # Columns added after the first release; create_all doesn't alter existing tables.
        db.execute(text("ALTER TABLE facilities ADD COLUMN IF NOT EXISTS opening_hours JSONB"))
        for source, loader in FACILITY_LOADERS.items():
            rows = list({r["id"]: r for r in loader(args.data_dir)}.values())
            if source == "mois_hospital" and args.hospital_hours:
                print(f"hospital hours joined: {attach_hospital_hours(rows, args.hospital_hours)}")
            db.execute(delete(Facility).where(Facility.source == source))
            insert_batches(db, Facility, rows)
            print(f"facilities[{source}]: {len(rows)}")

        animals = rescued_animals(args.data_dir)
        upsert_rescued(db, animals)
        print(f"rescued_animals: {len(animals)}")

        # Species for lost animals is inferred from rescued-animal breeds, so load those first.
        reports = public_lost_reports(args.data_dir, breed_species_map(db))
        db.execute(delete(LostReport).where(LostReport.source == "public"))
        upsert_lost_reports(db, reports)
        print(f"lost_reports[public]: {len(reports)} (run sync_lost_animals afterwards for recent ones)")

        seed_demo_user(db)
        db.commit()
    print("done")


if __name__ == "__main__":
    main()

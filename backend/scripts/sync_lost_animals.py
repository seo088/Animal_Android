"""Pull recent lost-animal reports from the 농림축산식품부 분실동물 조회 OpenAPI.

    python -m scripts.sync_lost_animals --days 7

Uses the same ANIMAL_API_KEY as sync_rescued_animals. Owner phone numbers are
kept (finders need them) and owner names are masked, e.g. 이건희 -> 이○희.
"""

import argparse
from datetime import timedelta

import httpx

from app.config import settings
from app.convert import now_kst
from app.db import Base, SessionLocal, engine
from scripts.common import breed_species_map, lost_row, upsert_lost_reports

ENDPOINT = "https://apis.data.go.kr/1543061/lossInfoService/lossInfo"
PAGE_SIZE = 1000


def fetch(start: str, end: str) -> list[dict]:
    items, page = [], 1
    while True:
        response = httpx.get(ENDPOINT, timeout=30, params={
            "serviceKey": settings.animal_api_key, "bgnde": start, "endde": end,
            "pageNo": page, "numOfRows": PAGE_SIZE, "_type": "json",
        })
        response.raise_for_status()
        body = response.json()["response"]["body"]
        batch = (body.get("items") or {}).get("item") or []
        if isinstance(batch, dict):  # single result comes back as an object
            batch = [batch]
        items.extend(batch)
        if page * PAGE_SIZE >= int(body.get("totalCount", 0)) or not batch:
            return items
        page += 1


def to_row(item: dict, breed_map: dict[str, str]) -> dict:
    return lost_row(
        kind=item.get("kindCd"),
        sex=item.get("sexCd"),
        region=item.get("orgNm"),
        happen_dt=item.get("happenDt"),
        happen_addr=item.get("happenAddr"),
        special_mark=item.get("specialMark"),
        color=item.get("colorCd"),
        age=item.get("age"),
        image=item.get("popfile"),
        call_name=item.get("callName"),
        call_tel=item.get("callTel"),
        breed_map=breed_map,
    )


def sync(days: int) -> int:
    """Fetch the last `days` days and upsert them. Returns the number of items."""
    today = now_kst().date()
    items = fetch((today - timedelta(days=days)).strftime("%Y%m%d"), today.strftime("%Y%m%d"))

    Base.metadata.create_all(engine)
    with SessionLocal() as db:
        breed_map = breed_species_map(db)
        upsert_lost_reports(db, [to_row(item, breed_map) for item in items])
        db.commit()
    return len(items)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--days", type=int, default=7, help="오늘부터 며칠 전까지 가져올지")
    args = parser.parse_args()
    if not settings.animal_api_key:
        raise SystemExit("ANIMAL_API_KEY가 .env에 없습니다.")
    print(f"synced {sync(args.days)} lost animals")


if __name__ == "__main__":
    main()

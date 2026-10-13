"""Value normalization shared by the ETL scripts and the API, so that stored data
uses the same vocabulary as the Android app (e.g. "서울 송파구", "수컷")."""

from datetime import date, datetime

SIDO_SHORT = {
    "서울특별시": "서울", "부산광역시": "부산", "대구광역시": "대구", "인천광역시": "인천",
    "광주광역시": "광주", "대전광역시": "대전", "울산광역시": "울산", "세종특별자치시": "세종",
    "경기도": "경기", "강원도": "강원", "강원특별자치도": "강원",
    "충청북도": "충북", "충청남도": "충남",
    "전라북도": "전북", "전북특별자치도": "전북", "전라남도": "전남",
    "경상북도": "경북", "경상남도": "경남", "제주특별자치도": "제주",
}

GENDER = {"M": "수컷", "F": "암컷", "Q": "미상"}

ALL = "전체"

# App breed names (res/values/arrays.xml) -> 농식품부 KindName spelling.
BREED_ALIASES = {
    "진돗개": "진도견",
    "시바견": "시바",
    "코리안숏헤어": "한국 고양이",
    "Russian Blue": "러시안 블루",
}


def short_region(value: str | None) -> str | None:
    """'서울특별시 송파구 ...' -> '서울 송파구'. Already-short input is kept as is."""
    if not value:
        return None
    parts = value.split()
    if not parts:
        return None
    sido = SIDO_SHORT.get(parts[0], parts[0])
    if len(parts) == 1:
        return sido
    return f"{sido} {parts[1]}"


def canonical_breed(value: str | None) -> str | None:
    if not value:
        return None
    value = value.strip()
    return BREED_ALIASES.get(value, value)


def gender_label(code: str | None) -> str:
    if not code:
        return "미상"
    return GENDER.get(code.strip().upper(), code.strip())


def is_all(value: str | None) -> bool:
    return value is None or value.strip() in ("", ALL)


def parse_date(value: str | None) -> date | None:
    if not value:
        return None
    value = value.strip()
    for fmt, length in (("%Y-%m-%d", 10), ("%Y.%m.%d", 10), ("%Y%m%d", 8)):
        try:
            return datetime.strptime(value[:length], fmt).date()
        except ValueError:
            continue
    return None


def blank_to_none(value: str | None) -> str | None:
    if value is None:
        return None
    value = value.strip()
    return value or None


def to_float(value: str | None) -> float | None:
    try:
        return float(value) if value not in (None, "") else None
    except ValueError:
        return None

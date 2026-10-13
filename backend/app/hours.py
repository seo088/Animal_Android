"""Weekly opening hours: parse the '동물병원_전국' OPR_TIME_INFO text, store it as
JSON, render a short display string, and answer 'open now?'.

Input looks like:
  "월 10:00 - 19:00 | 화 10:00 - 19:00 | ... | 일 정기휴무 (매주 일요일) | 12:00 - 14:00 휴게시간"
  "매일 00:00 - 24:00 | 13:00 - 14:00 휴게시간"
Stored shape: {"days": {"0": ["10:00", "19:00"], ..., "6": null}, "break": ["12:00", "14:00"]}
with 0 = Monday. A missing day means unknown, null means closed.
"""

import re
from datetime import datetime

DAY_NAMES = "월화수목금토일"
_RANGE = re.compile(r"(\d{1,2}):(\d{2})\s*[-~]\s*(\d{1,2}):(\d{2})")
_LEADING_DAYS = re.compile(r"^((?:[월화수목금토일](?:요일)?(?:\(\d+/\d+\))?\s*)+)")


def _days_in(segment: str) -> list[int]:
    if segment.startswith("매일"):
        return list(range(7))
    if segment.startswith("평일"):
        return list(range(5))
    if segment.startswith("주말"):
        return [5, 6]
    match = _LEADING_DAYS.match(segment)
    if not match:
        return []
    return sorted({DAY_NAMES.index(ch) for ch in re.sub(r"요일|\(\d+/\d+\)", "", match.group(1)) if ch in DAY_NAMES})


def _range(match: re.Match) -> list[str]:
    h1, m1, h2, m2 = match.groups()
    return [f"{int(h1):02d}:{m1}", f"{int(h2):02d}:{m2}"]


def parse_opr_time(text: str | None) -> dict | None:
    if not text or not text.strip():
        return None
    days: dict[str, list[str] | None] = {}
    every_day: list[str] | None = None
    lunch: list[str] | None = None
    for segment in (s.strip() for s in text.split("|")):
        time_range = _RANGE.search(segment)
        if "휴게" in segment:
            if time_range:
                lunch = _range(time_range)
            continue
        if "접수" in segment or "정보없음" in segment:
            continue
        if "휴무" in segment and re.search(r"\(\d+/\d+\)", segment):
            continue  # one-off closure on a specific date, not a weekly rule
        target_days = _days_in(segment)
        if "휴무" in segment and not time_range:
            for day in target_days:
                days[str(day)] = None
        elif time_range and target_days:
            for day in target_days:
                days[str(day)] = _range(time_range)
        elif time_range:
            every_day = _range(time_range)
    if every_day:
        for day in range(7):
            days.setdefault(str(day), every_day)
    if not days:
        return None
    return {"days": days, "break": lunch}


def _minutes(hhmm: str) -> int:
    h, m = hhmm.split(":")
    return int(h) * 60 + int(m)


def is_open(opening_hours: dict, now: datetime) -> bool:
    today = opening_hours["days"].get(str(now.weekday()))
    if not today:
        return False
    current = now.hour * 60 + now.minute
    start, end = _minutes(today[0]), _minutes(today[1])
    within = start <= current < end if start < end else current >= start or current < end
    lunch = opening_hours.get("break")
    if within and lunch:
        lunch_start, lunch_end = _minutes(lunch[0]), _minutes(lunch[1])
        # The source lists one break per weekday; only apply it to days whose hours
        # span the whole break (e.g. not to a Saturday that closes at 13:00).
        if start <= lunch_start and lunch_end <= end and lunch_start <= current < lunch_end:
            return False
    return within


def format_hours(opening_hours: dict) -> str:
    """'월~금 10:00-19:00 · 토 09:30-13:00 · 일 휴무 · 휴게 12:00-14:00'"""
    def label(day: int) -> str:
        value = opening_hours["days"].get(str(day), "unknown")
        if value == "unknown":
            return "unknown"
        return "휴무" if value is None else f"{value[0]}-{value[1]}"

    parts, start = [], 0
    while start < 7:
        end = start
        while end + 1 < 7 and label(end + 1) == label(start):
            end += 1
        if label(start) != "unknown":
            days = DAY_NAMES[start] if start == end else f"{DAY_NAMES[start]}~{DAY_NAMES[end]}"
            parts.append(f"{'매일' if (start, end) == (0, 6) else days} {label(start)}")
        start = end + 1
    if opening_hours.get("break"):
        parts.append(f"휴게 {opening_hours['break'][0]}-{opening_hours['break'][1]}")
    return " · ".join(parts)[:255]

"""보호자용 증상 선택 항목.

원문 지식그래프의 증상 ID는 추론 요청에 그대로 사용한다. 이 목록은 그 ID를
보호자가 관찰할 수 있는 표현과 부위로만 변환하며, 수의학적 해석이 필요한
원문 항목은 선택 화면에서 제외한다.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Any


@dataclass(frozen=True)
class CuratedSymptom:
    source_name: str
    category: str
    display_name: str


# 원문 라벨은 KISTI 지식그래프의 해당 ID와 일대일로 대조했다. 동의어·중증도
# 차이가 섞인 원문 항목은 넓은 표현으로 합치지 않고 목록에서 제외한다.
CURATED_SYMPTOMS: dict[str, CuratedSymptom] = {
    # 눈
    "d003": CuratedSymptom("눈의 화농성 분비물", "눈", "눈곱·분비물이 많아요"),
    "d013": CuratedSymptom("결막, 공막의 충혈, 비정상적인 혈관", "눈", "눈이 빨개요"),
    "d017": CuratedSymptom("각막의 부종, 각막 혼탁", "눈", "눈이 뿌옇게 보여요"),
    "d021": CuratedSymptom("각막손상, 각막의 열상, 열창", "눈", "눈을 다쳤어요"),
    "d022": CuratedSymptom("눈물흘림, 유루, 장액성 안구 분비물, 젖은눈", "눈", "눈물이 많이 나요"),
    "e090": CuratedSymptom("각막 또는 안구 이물", "눈", "눈에 이물질이 보여요"),
    # 귀
    "a003": CuratedSymptom("귀의 화농성, 점액성 분비물, 과도한 이구(귀밥), 악취", "귀", "귀 분비물·귀밥·냄새가 심해요"),
    "a005": CuratedSymptom("귀를 긁음", "귀", "귀를 자주 긁어요"),
    "a001": CuratedSymptom("난청, 귀머거리", "귀", "소리에 잘 반응하지 않아요"),
    # 피부
    "j004": CuratedSymptom("피부의 화농성 분비물", "피부", "피부에 분비물이나 고름이 보여요"),
    "j007": CuratedSymptom("피부의 소양감, 가려움", "피부", "피부를 자주 긁어요"),
    # 소화
    "c003": CuratedSymptom("식욕부진, 식욕감소, 식욕절폐", "소화", "식욕이 줄었어요"),
    "c036": CuratedSymptom("구토, 역류", "소화", "토하거나 게워내요"),
    "c050": CuratedSymptom("설사", "소화", "설사를 해요"),
    "c055": CuratedSymptom("혈변, 분변내에 잠혈", "소화", "대변에 피가 보여요"),
    # 호흡
    "i003": CuratedSymptom("화농성 비강 분비물", "호흡", "콧물이 나요"),
    "i004": CuratedSymptom("점액성, 장액성, 수양성 비강 분비물", "호흡", "콧물이 나요"),
    "i008": CuratedSymptom("호흡곤란, 개구호흡, 그르렁거림, 가쁜호흡", "호흡", "숨쉬기 힘들어 보여요"),
    "i011": CuratedSymptom("재채기", "호흡", "재채기를 해요"),
    "i012": CuratedSymptom("기침", "호흡", "기침을 해요"),
    # 움직임
    "e079": CuratedSymptom("운동실조, 협조불능, 절뚝거림, 전도", "움직임", "절뚝거리거나 비틀거려요"),
    # 행동·기타
    "g001": CuratedSymptom("진전", "행동·기타", "몸을 떨어요"),
    "g011": CuratedSymptom("무기력, 침울, 졸림, 생기없는, 불안", "행동·기타", "기운이 없거나 축 처져요"),
}


def curate_symptoms(items: list[dict[str, Any]]) -> list[dict[str, str]]:
    """원문 목록에서 검증된 보호자용 선택 항목만 반환한다.

    업스트림의 코드가 아직 같더라도 라벨이 달라진 경우를 막기 위해, 아래
    catalog에 없는 항목은 노출하지 않는다. ID는 추론 API 요청 호환성을 위해
    절대 변경하지 않는다.
    """
    curated: list[dict[str, str]] = []
    seen_display_names: set[tuple[str, str]] = set()
    for item in items:
        symptom_id = str(item.get("code", "")).strip()
        symptom = CURATED_SYMPTOMS.get(symptom_id)
        source_name = str(item.get("name", "")).strip()
        if not symptom or source_name != symptom.source_name:
            continue
        key = (symptom.category, symptom.display_name)
        if key in seen_display_names:
            continue
        seen_display_names.add(key)
        curated.append({
            "id": symptom_id,
            "name": symptom.display_name,
            "category": symptom.category,
        })
    return curated

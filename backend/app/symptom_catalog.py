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
    "d011": CuratedSymptom("결막부종, 결막 공막의 부종", "눈", "눈이 부어 보여요"),
    "d012": CuratedSymptom("결막, 공막의 발적", "눈", "눈이 빨개요"),
    "d013": CuratedSymptom("결막, 공막의 충혈, 비정상적인 혈관", "눈", "눈이 빨개요"),
    "d014": CuratedSymptom("결막, 공막의 열상, 열창, 손상", "눈", "눈 주변을 다쳤어요"),
    "d017": CuratedSymptom("각막의 부종, 각막 혼탁", "눈", "눈이 뿌옇게 보여요"),
    "d020": CuratedSymptom("각막의 궤양, 미란", "눈", "눈 표면에 상처가 보여요"),
    "d021": CuratedSymptom("각막손상, 각막의 열상, 열창", "눈", "눈을 다쳤어요"),
    "d022": CuratedSymptom("눈물흘림, 유루, 장액성 안구 분비물, 젖은눈", "눈", "눈물이 많이 나요"),
    "d049": CuratedSymptom("눈부심, 수명", "눈", "빛을 싫어해요"),
    "d050": CuratedSymptom("안검연축", "눈", "눈을 자주 찡그려요"),
    "e090": CuratedSymptom("각막 또는 안구 이물", "눈", "눈에 이물질이 보여요"),
    # 귀
    "a003": CuratedSymptom("귀의 화농성, 점액성 분비물, 과도한 이구(귀밥), 악취", "귀", "귀 분비물·귀밥·냄새가 심해요"),
    "a004": CuratedSymptom("귀의 진드기, 응애, 기생충", "귀", "귀 안에 검은 찌꺼기나 벌레가 보여요"),
    "a005": CuratedSymptom("귀를 긁음", "귀", "귀를 자주 긁어요"),
    "a001": CuratedSymptom("난청, 귀머거리", "귀", "소리에 잘 반응하지 않아요"),
    "e096": CuratedSymptom("귀의 이물", "귀", "귀에 이물질이 보여요"),
    # 피부
    "j004": CuratedSymptom("피부의 화농성 분비물", "피부", "피부에 분비물이나 고름이 보여요"),
    "j005": CuratedSymptom("피부의 악취", "피부", "피부 냄새가 심해요"),
    "j006": CuratedSymptom("피부홍반, 염증, 발적", "피부", "피부가 빨개요"),
    "j007": CuratedSymptom("피부의 소양감, 가려움", "피부", "피부를 자주 긁어요"),
    "j009": CuratedSymptom("피부 구진", "피부", "피부에 작은 돌기가 보여요"),
    "j010": CuratedSymptom("피부 농포", "피부", "피부에 고름집이 보여요"),
    "j011": CuratedSymptom("피부 수포, 소수포, 물집", "피부", "피부에 물집이 보여요"),
    "j013": CuratedSymptom("피부 팽진, 두드러기", "피부", "피부에 두드러기가 보여요"),
    "j014": CuratedSymptom("피부 부종", "피부", "피부가 부어 보여요"),
    "j017": CuratedSymptom("피부의 열상, 열창, 교상", "피부", "피부에 상처가 있어요"),
    "j020": CuratedSymptom("피부, 털의 건조", "피부", "피부나 털이 건조해 보여요"),
    "j022": CuratedSymptom("피부 인설, 비듬", "피부", "비듬이 많이 보여요"),
    "j023": CuratedSymptom("피부의 가피, 딱지", "피부", "피부에 딱지가 있어요"),
    "j044": CuratedSymptom("탈모, 쉽게 빠지는 털", "피부", "털이 많이 빠져요"),
    # 소화
    "c003": CuratedSymptom("식욕부진, 식욕감소, 식욕절폐", "소화", "식욕이 줄었어요"),
    "c005": CuratedSymptom("저작곤란, 잘 씹지 못함", "소화", "음식을 잘 씹지 못해요"),
    "c017": CuratedSymptom("과도한 유연, 침흘림, 타액분비과다, 입에거품", "소화", "침을 많이 흘리거나 입에 거품이 나요"),
    "c033": CuratedSymptom("연하곤란, 연하장애", "소화", "삼키기 어려워해요"),
    "c036": CuratedSymptom("구토, 역류", "소화", "토하거나 게워내요"),
    "c037": CuratedSymptom("토혈증", "소화", "토할 때 피가 보여요"),
    "c041": CuratedSymptom("복부팽만", "소화", "배가 부풀어 보여요"),
    "c049": CuratedSymptom("배변량 감소, 변비", "소화", "변을 보기 어려워해요"),
    "c050": CuratedSymptom("설사", "소화", "설사를 해요"),
    "c054": CuratedSymptom("점액변, 점액양분변", "소화", "변에 점액이 보여요"),
    "c055": CuratedSymptom("혈변, 분변내에 잠혈", "소화", "대변에 피가 보여요"),
    "c057": CuratedSymptom("변의 비정상적인 냄새, 악취", "소화", "변 냄새가 평소보다 심해요"),
    # 호흡
    "i002": CuratedSymptom("비출혈, 코피", "호흡", "코피가 나요"),
    "i003": CuratedSymptom("화농성 비강 분비물", "호흡", "콧물이 나요"),
    "i004": CuratedSymptom("점액성, 장액성, 수양성 비강 분비물", "호흡", "콧물이 나요"),
    "i008": CuratedSymptom("호흡곤란, 개구호흡, 그르렁거림, 가쁜호흡", "호흡", "숨쉬기 힘들어 보여요"),
    "i009": CuratedSymptom("호흡수 증가, 다호흡, 빠른호흡(빈호흡), 과호흡", "호흡", "숨이 평소보다 빨라요"),
    "i011": CuratedSymptom("재채기", "호흡", "재채기를 해요"),
    "i012": CuratedSymptom("기침", "호흡", "기침을 해요"),
    "i013": CuratedSymptom("기침시 객혈", "호흡", "기침할 때 피가 보여요"),
    "i014": CuratedSymptom("상부기도에서의 비정상 호흡음, 공기흐름폐쇄, 코고는 소리", "호흡", "숨쉴 때 이상한 소리가 나요"),
    "i017": CuratedSymptom("목소리 변성", "호흡", "짖거나 울 때 목소리가 달라요"),
    # 움직임
    "e005": CuratedSymptom("운동기피, 쉽게 지침", "움직임", "움직이려 하지 않거나 쉽게 지쳐요"),
    "e061": CuratedSymptom("앞다리 파행, 경직, 절름발이", "움직임", "앞다리를 절뚝거려요"),
    "e062": CuratedSymptom("뒷다리 파행, 경직, 절름발이", "움직임", "뒷다리를 절뚝거려요"),
    "e076": CuratedSymptom("기립불능, 쇠약", "움직임", "혼자 일어서기 어려워해요"),
    "e079": CuratedSymptom("운동실조, 협조불능, 절뚝거림, 전도", "움직임", "절뚝거리거나 비틀거려요"),
    "e080": CuratedSymptom("이동거부", "움직임", "움직이기를 거부해요"),
    # 행동·기타
    "g001": CuratedSymptom("진전", "행동·기타", "몸을 떨어요"),
    "e002": CuratedSymptom("불안감, 안절부절, 조류", "행동·기타", "안절부절못해요"),
    "e003": CuratedSymptom("발열, 고열, 고체온", "행동·기타", "몸이 평소보다 뜨거워 보여요"),
    "g011": CuratedSymptom("무기력, 침울, 졸림, 생기없는, 불안", "행동·기타", "기운이 없거나 축 처져요"),
    "g003": CuratedSymptom("방향 감각 상실, 기억력 소실", "행동·기타", "평소와 다르게 헤매요"),
    "g004": CuratedSymptom("머리를 기울임, 두부경사", "행동·기타", "고개를 한쪽으로 기울여요"),
    "g005": CuratedSymptom("선회운동", "행동·기타", "같은 자리를 빙글빙글 돌아요"),
    "g013": CuratedSymptom("발작, 실신, 경련, 간질발작, 허탈", "행동·기타", "경련하거나 의식을 잃었어요"),
    "g015": CuratedSymptom("머리 흔들기", "행동·기타", "머리를 자주 흔들어요"),
    "g016": CuratedSymptom("수면과다 또는 수면감소", "행동·기타", "잠자는 시간이 평소와 달라요"),
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

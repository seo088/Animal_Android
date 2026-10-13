"""Symptom check backed by the KISTI symptom/disease knowledge graph in Fuseki.

Graph shape (turtle/13_symptoms.ttl, 15_diseases.ttl, 16_reasoning_chain.ttl):
  symptom  a def:Symptom ; rdfs:label ; def:categoryOf category
  disease  a def:Disease ; rdfs:label ; def:causeClass ; def:targetAnimal ;
           def:hasSymptom symptom ; def:treatedByDept dept
  dept     a def:VetDepartment ; rdfs:label
"""

import re

from fastapi import APIRouter, HTTPException, Query, status

from app import schemas
from app.sparql import DISEASE_NS, SYMPTOM_NS, local_id, select

router = APIRouter(tags=["증상 확인"])

_SYMPTOM_ID = re.compile(r"^[a-z]\d{3}$")
_DISEASE_ID = re.compile(r"^\d+$")
_MAX_RESULTS = 5
_DISCLAIMER = "이 결과는 참고용이며, 정확한 진단은 반드시 수의사 진료를 통해 받으세요."

# Pet diseases only: the first entry of the source's 주요 감염동물 list must be 개 or 고양이.
# Livestock-first diseases (소결핵병, 큐열, and also 광견병 whose list starts with 소) are excluded.
def _pet_disease(var: str) -> str:
    return f'{var} def:targetAnimal ?petTarget{var[1:]} . FILTER(REGEX(?petTarget{var[1:]}, "^(개|고양이)(,|$)"))'


@router.get("/symptoms", response_model=list[schemas.SymptomOut])
def symptoms(all_symptoms: bool = Query(False, alias="all", description="false면 반려동물 질병과 연결된 증상만")):
    linked_only = "" if all_symptoms else (
        f"FILTER EXISTS {{ ?s def:indicatesDisease ?anyDisease . {_pet_disease('?anyDisease')} }}"
    )
    rows = select(f"""
        SELECT ?s ?name ?category WHERE {{
          ?s a def:Symptom ; rdfs:label ?name .
          FILTER(lang(?name) = "ko")
          OPTIONAL {{ ?s def:categoryOf ?c . ?c rdfs:label ?category . FILTER(lang(?category) = "ko") }}
          {linked_only}
        }} ORDER BY ?s
    """)
    return [
        schemas.SymptomOut(id=local_id(r["s"], SYMPTOM_NS), name=r["name"], category=r.get("category"))
        for r in rows
    ]


def _description(cause: str | None, target: str | None) -> str:
    parts = []
    if cause:
        parts.append(f"원인 분류: {cause}")
    if target:
        parts.append(f"주요 대상 동물: {target}")
    return " · ".join(parts) or "KISTI 동물질병 지식그래프 기반 추론 결과입니다."


def _caution(department: str | None) -> str:
    if department:
        return f"{department} 진료가 가능한 동물병원 방문을 권장합니다. {_DISCLAIMER}"
    return _DISCLAIMER


@router.post("/diagnosis/infer", response_model=list[schemas.DiagnosisResultOut])
def infer(body: schemas.InferRequest):
    ids = sorted(set(body.symptom_ids))
    invalid = [i for i in ids if not _SYMPTOM_ID.match(i)]
    if invalid:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, f"알 수 없는 증상 ID: {', '.join(invalid)}")
    values = " ".join(f"<{SYMPTOM_NS}{i}>" for i in ids)

    rows = select(f"""
        SELECT ?d ?name ?total (COUNT(DISTINCT ?m) AS ?matched)
               (GROUP_CONCAT(DISTINCT ?mName; separator="|") AS ?matchedNames)
               (SAMPLE(?cause) AS ?causeClass) (SAMPLE(?target) AS ?targetAnimal) (SAMPLE(?deptName) AS ?dept)
        WHERE {{
          VALUES ?m {{ {values} }}
          ?d def:hasSymptom ?m ; rdfs:label ?name . FILTER(lang(?name) = "ko")
          {_pet_disease('?d')}
          ?m rdfs:label ?mName . FILTER(lang(?mName) = "ko")
          {{ SELECT ?d (COUNT(DISTINCT ?x) AS ?total) WHERE {{ ?d def:hasSymptom ?x }} GROUP BY ?d }}
          OPTIONAL {{ ?d def:causeClass ?cause }}
          OPTIONAL {{ ?d def:targetAnimal ?target }}
          OPTIONAL {{ ?d def:treatedByDept ?deptIri . ?deptIri rdfs:label ?deptName . FILTER(lang(?deptName) = "ko") }}
        }}
        GROUP BY ?d ?name ?total
    """)

    results = []
    for r in rows:
        matched, total = int(r["matched"]), int(r["total"])
        # Blend "how much of the disease's profile is present" with "how much of what
        # the user reported this disease explains".
        score = 0.6 * matched / total + 0.4 * matched / len(ids)
        results.append(schemas.DiagnosisResultOut(
            id=local_id(r["d"], DISEASE_NS),
            disease_name=r["name"],
            probability_percent=max(1, min(95, round(score * 100))),
            related_symptoms=r["matchedNames"].split("|"),
            description=_description(r.get("causeClass"), r.get("targetAnimal")),
            caution=_caution(r.get("dept")),
        ))
    results.sort(key=lambda d: d.probability_percent, reverse=True)
    return results[:_MAX_RESULTS]


@router.get("/diagnosis/{disease_id}", response_model=schemas.DiagnosisResultOut)
def disease_detail(disease_id: str):
    if not _DISEASE_ID.match(disease_id):
        raise HTTPException(status.HTTP_404_NOT_FOUND, "질병 정보를 찾을 수 없습니다.")
    rows = select(f"""
        SELECT ?name (GROUP_CONCAT(DISTINCT ?sName; separator="|") AS ?symptoms)
               (SAMPLE(?cause) AS ?causeClass) (SAMPLE(?target) AS ?targetAnimal) (SAMPLE(?deptName) AS ?dept)
        WHERE {{
          BIND(<{DISEASE_NS}{disease_id}> AS ?d)
          ?d a def:Disease ; rdfs:label ?name . FILTER(lang(?name) = "ko")
          OPTIONAL {{ ?d def:hasSymptom ?s . ?s rdfs:label ?sName . FILTER(lang(?sName) = "ko") }}
          OPTIONAL {{ ?d def:causeClass ?cause }}
          OPTIONAL {{ ?d def:targetAnimal ?target }}
          OPTIONAL {{ ?d def:treatedByDept ?deptIri . ?deptIri rdfs:label ?deptName . FILTER(lang(?deptName) = "ko") }}
        }}
        GROUP BY ?name
    """)
    if not rows:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "질병 정보를 찾을 수 없습니다.")
    r = rows[0]
    return schemas.DiagnosisResultOut(
        id=disease_id,
        disease_name=r["name"],
        probability_percent=0,
        related_symptoms=[s for s in r.get("symptoms", "").split("|") if s],
        description=_description(r.get("causeClass"), r.get("targetAnimal")),
        caution=_caution(r.get("dept")),
    )

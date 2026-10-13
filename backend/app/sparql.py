import httpx
from fastapi import HTTPException, status

from app.config import settings

PREFIXES = """
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX def:  <https://knowledgemap.kr/koah/def/>
"""
SYMPTOM_NS = "https://knowledgemap.kr/koah/id/symptom/"
DISEASE_NS = "https://knowledgemap.kr/koah/id/disease/"


def select(query: str) -> list[dict[str, str]]:
    """Run a SELECT against Fuseki and flatten bindings to {var: value}."""
    try:
        response = httpx.post(
            settings.fuseki_query_url,
            data={"query": PREFIXES + query},
            headers={"Accept": "application/sparql-results+json"},
            timeout=10.0,
        )
        response.raise_for_status()
    except httpx.HTTPError as exc:
        raise HTTPException(status.HTTP_503_SERVICE_UNAVAILABLE, "지식그래프 서버(Fuseki)에 연결할 수 없습니다.") from exc
    bindings = response.json()["results"]["bindings"]
    return [{var: cell["value"] for var, cell in row.items()} for row in bindings]


def local_id(iri: str, namespace: str) -> str:
    return iri[len(namespace):] if iri.startswith(namespace) else iri

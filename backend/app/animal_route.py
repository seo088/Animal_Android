"""Server-side client for Animal-Route's read-only knowledge-graph API.

The developer API key must remain in this backend's environment.  Android clients
only call this service's /diagnosis endpoints and never receive the key.
"""

from __future__ import annotations

from typing import Any

import httpx
from fastapi import HTTPException, status

from app.config import settings


class AnimalRouteClient:
    def _request(self, path: str, params: dict[str, str]) -> dict[str, Any]:
        if not settings.animal_route_api_key:
            raise HTTPException(
                status.HTTP_503_SERVICE_UNAVAILABLE,
                "지식그래프 연결 설정이 아직 완료되지 않았습니다.",
            )

        url = f"{settings.animal_route_api_base_url.rstrip('/')}/{path.lstrip('/')}"
        try:
            with httpx.Client(timeout=8.0, follow_redirects=False) as client:
                response = client.get(
                    url,
                    params=params,
                    headers={"X-API-Key": settings.animal_route_api_key},
                )
        except httpx.HTTPError:
            raise HTTPException(
                status.HTTP_503_SERVICE_UNAVAILABLE,
                "지식그래프 서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.",
            ) from None

        if response.status_code == status.HTTP_429_TOO_MANY_REQUESTS:
            retry_after = response.headers.get("Retry-After")
            headers = {"Retry-After": retry_after} if retry_after else None
            raise HTTPException(
                status.HTTP_429_TOO_MANY_REQUESTS,
                "지식그래프 조회 요청이 잠시 많습니다. 잠시 후 다시 시도해 주세요.",
                headers=headers,
            )
        if response.status_code in (status.HTTP_401_UNAUTHORIZED, status.HTTP_403_FORBIDDEN):
            raise HTTPException(
                status.HTTP_503_SERVICE_UNAVAILABLE,
                "지식그래프 접근 권한을 확인하지 못했습니다.",
            )
        if response.status_code >= 500:
            raise HTTPException(
                status.HTTP_503_SERVICE_UNAVAILABLE,
                "지식그래프 데이터를 지금 조회하지 못했습니다. 잠시 후 다시 시도해 주세요.",
            )
        if response.status_code >= 400:
            raise HTTPException(status.HTTP_400_BAD_REQUEST, "지식그래프 조회 요청이 올바르지 않습니다.")

        try:
            payload = response.json()
        except ValueError:
            raise HTTPException(
                status.HTTP_503_SERVICE_UNAVAILABLE,
                "지식그래프 응답 형식을 확인하지 못했습니다.",
            ) from None
        if not isinstance(payload, dict) or not isinstance(payload.get("data"), list):
            raise HTTPException(
                status.HTTP_503_SERVICE_UNAVAILABLE,
                "지식그래프 응답 형식을 확인하지 못했습니다.",
            )
        return payload

    def symptoms(self, species: str) -> list[dict[str, Any]]:
        return self._request("symptoms", {"species": species}).get("data", [])

    def relations(self, species: str, symptom_code: str) -> list[dict[str, Any]]:
        return self._request(
            "relations", {"species": species, "symptom_code": symptom_code}
        ).get("data", [])


animal_route_client = AnimalRouteClient()

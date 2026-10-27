# AnimalLoo Backend

FastAPI + PostgreSQL + Apache Jena Fuseki 기반 AnimalLoo 앱 서버입니다.
증상 추론(`/diagnosis/*`, `/symptoms`)만 Fuseki SPARQL로 조회하고, 나머지는 모두 PostgreSQL에서 꺼냅니다.

응답 JSON 필드는 앱의 `app/src/main/java/com/animalloo/data/model/*` 필드명과 같은 camelCase입니다.
enum은 이름 그대로(`HOSPITAL`, `RESCUE`, `A`) 내려가므로 Gson으로 바로 매핑됩니다.

## 기존 Windows 개발 환경 다시 실행

이미 `.env`, `.venv/`, PostgreSQL 데이터와 `fuseki-data/`가 있는 환경에서는 저장소 루트에서 아래 명령만 실행합니다.

```powershell
.\backend\start-local.ps1
```

- 기존 DB 연결 설정을 유지하며 데이터 초기화·재적재를 하지 않습니다.
- API는 `127.0.0.1:8000`, Fuseki는 localhost의 `3030`에서 실행합니다. Fuseki의 SPARQL 업데이트 API는 켜지 않습니다.
- 기존 서비스가 있으면 재사용하고 `/health`의 DB·Fuseki 상태를 확인합니다.
- 공공데이터 자동 수집은 기본으로 꺼집니다. 기존 자동 수집 정책을 사용하려면 서버를 시작할 때 `-EnablePublicSync`를 명시합니다. 이미 실행 중인 서버의 정책은 바꾸지 않습니다.
- 로그는 `.runtime/`에 저장합니다. Fuseki 실행 파일 위치가 다르면 `-FusekiJar`, Java 실행 파일 위치가 다르면 `-Java`로 지정합니다.
- Android Studio에는 `app/`과 `backend/`가 함께 있는 프로젝트 루트를 엽니다. 중첩된 예전 프로젝트 사본으로 실행하면 Mock 버전이 다시 설치됩니다.

아래 최초 설치·적재 절차는 **신규 환경을 구성할 때만** 사용합니다. 연결 문제를 해결하려고 기존 데이터에 재실행하지 않습니다.

## 구조

```
backend/
├─ app/
│  ├─ main.py            # FastAPI 앱, 라우터 등록, /uploads 정적 파일, /health
│  ├─ config.py          # .env 설정
│  ├─ db.py, models.py   # SQLAlchemy (users, pets, facilities, rescued_animals, lost_reports, favorites)
│  ├─ schemas.py         # 요청/응답 (camelCase)
│  ├─ normalize.py       # 지역/성별/날짜 정규화 ("서울특별시 송파구" → "서울 송파구", M → 수컷)
│  ├─ convert.py         # ORM → 응답 변환
│  ├─ matching.py        # 분실신고 ↔ 구조동물 매칭 (앱 Mock과 같은 점수 규칙)
│  ├─ sparql.py          # Fuseki 클라이언트
│  └─ routers/           # 기능별 API
└─ scripts/
   ├─ load_public_data.py      # 이미 받아 둔 공공데이터 CSV/JSON → Postgres
   ├─ sync_rescued_animals.py  # 농식품부 구조동물 OpenAPI → Postgres
   ├─ sync_lost_animals.py     # 농식품부 분실동물 OpenAPI → Postgres
   └─ load_fuseki.py           # KISTI 증상·질병 TTL → Fuseki
```

## 실행

```powershell
cd backend
python -m venv .venv
.venv\Scripts\pip install -r requirements.txt
copy .env.example .env   # DATABASE_URL 등 채우기
```

1. **PostgreSQL**: DB와 계정을 만듭니다 (psql, postgres 계정으로).
   ```sql
   CREATE USER animalloo WITH PASSWORD 'animalloo';
   CREATE DATABASE animalloo OWNER animalloo;
   ```
2. **공공데이터 적재** (테이블 생성 + 데모 계정 `admin / 1234` 포함, 재실행 가능)
   ```powershell
   .venv\Scripts\python -m scripts.load_public_data --data-dir C:\Users\jwon\Downloads\preprocessed_data --hospital-hours C:\Users\jwon\Downloads\동물병원_전국.csv
   ```
3. **Fuseki 실행 + 지식그래프 적재** (Java 17+ 필요)
   ```powershell
   java -jar <fuseki>\fuseki-server.jar --localhost --update --tdb2 --loc=fuseki-data /animalloo
   .venv\Scripts\python -m scripts.load_fuseki --turtle-dir C:\Users\jwon\Downloads\turtle
   ```
   `fuseki-data/`에 저장되므로 적재는 한 번이면 됩니다. 다시 넣을 때는 Fuseki를 끄고 폴더를 지운 뒤 적재합니다.
4. **API 서버**
   ```powershell
   .venv\Scripts\python -m uvicorn app.main:app --host 127.0.0.1 --port 8000
   ```
   서버는 시작할 때와 `SYNC_INTERVAL_MINUTES`(기본 60분)마다 구조·분실동물 OpenAPI의 최근 3일치를 자동으로 다시 가져옵니다(`ANIMAL_API_KEY` 필요).
   더 긴 기간은 `python -m scripts.sync_rescued_animals --days 30`, `python -m scripts.sync_lost_animals --days 30`으로 가져옵니다.
   `load_public_data`는 공공 분실신고를 스냅샷으로 교체하므로, 다시 실행한 뒤에는 sync를 한 번 돌려 주세요.
   이 PC에서는 `--reload`가 첫 리로드 뒤 멈추는 일이 있어서, 코드를 바꾸면 서버를 직접 재시작하는 쪽이 안전합니다.
   에뮬레이터의 `10.0.2.2`는 호스트의 `127.0.0.1`로 연결되므로 localhost 바인딩으로 충분합니다.
   **`--host 0.0.0.0`이나 Fuseki의 `--localhost` 생략은 피하세요.** 이 PC는 외부에서 접근할 수 있어서 실제로 외부 접속 시도가 있었습니다. 실기기 테스트처럼 꼭 열어야 하면 방화벽으로 범위를 제한하고, `.env`의 `JWT_SECRET`을 무작위 값으로 설정해야 합니다.
   Swagger 문서: http://localhost:8000/docs · 상태 확인: http://localhost:8000/health

## API

| 기능 | Repository (앱) | API |
|---|---|---|
| 홈 대시보드 | HomeRepository | `GET /home/stats`, `GET /home/profile`🔒, `PUT /home/pet`🔒 |
| | AlertRepository | `GET /alerts?limit=`, `GET /alerts/{id}` (id: `R-<유기번호>`, `L-<신고ID>`) |
| 시설 지도 | FacilityRepository | `GET /facilities?category=&lat=&lng=&radiusKm=&limit=`, `GET /facilities/{id}` |
| 동반시설 | FacilityRepository | `GET /facilities/pet-friendly?petType=&size=&indoor=&carrier=&leash=&lat=&lng=` |
| 증상 확인 | DiagnosisRepository | `GET /symptoms?all=`, `POST /diagnosis/infer` `{"symptomIds":["i012"]}`, `GET /diagnosis/{id}` |
| | HospitalRepository | `GET /hospitals`, `GET /hospitals/nearby?lat=&lng=&radiusKm=`, `GET /hospitals/{id}` |
| 구조동물 | AnimalRepository | `GET /animals?breed=&region=&gender=&species=&page=&size=`, `GET /animals/{id}` |
| 분실신고 | LostAnimalRepository | `POST /lost-reports` (multipart, `photo` 선택), `POST /lost-reports/matches`, `GET /lost-reports/{id}`, `GET /lost-reports/{id}/matches` |
| 로그인 | AuthRepository | `POST /auth/login`, `POST /auth/signup`, `GET /auth/me`🔒 |
| 즐겨찾기 | FavoriteRepository | `GET /favorites`🔒, `PUT /favorites/{key}`🔒, `DELETE /favorites/{key}`🔒 |

🔒 = `Authorization: Bearer <token>` 필요. 로그인/가입 응답은 `{"token": "...", "user": {...}}`입니다.
오류는 `{"detail": "한국어 메시지"}` 형식이므로 앱에서 `onError(message)`로 그대로 보여줄 수 있습니다.

`lat`/`lng`를 보내면 반경 안의 결과가 가까운 순으로 정렬되고 `distanceKm`이 채워집니다. 보내지 않으면 `distanceKm`은 `null`입니다.

## 데이터 출처

| 구분 | 데이터 | 테이블/저장소 | 갱신 방법 |
|---|---|---|---|
| 공공데이터 (파일 확보) | 행안부 동물병원·동물약국 | facilities `HOSPITAL`, `PHARMACY` | `load_public_data` |
| | 농식품부 동물보호센터 | facilities `SHELTER` | `load_public_data` |
| | 한국문화정보원 반려동물 동반 문화시설 | facilities `CAFE`, `TOURISM` | `load_public_data` |
| | 한국관광공사 반려동물 동반여행 | facilities `RESTAURANT`, `CAFE`, `HOTEL`, `TOURISM` | `load_public_data` |
| | 한국도로공사 휴게소 반려동물 놀이터 | facilities `TOURISM` | `load_public_data` |
| | KISTI 동물질병 증상분류 | Fuseki `/animalloo` | `load_fuseki` |
| 공공데이터 (OpenAPI 연동) | 농식품부 구조동물 (`abandonmentPublicService_v2`) | rescued_animals | 스냅샷: `load_public_data`, 최신: 서버 자동 동기화 / `sync_rescued_animals` |
| | 농식품부 분실동물 (`lossInfoService/lossInfo`) | lost_reports (`source=public`) | 스냅샷: `load_public_data`, 최신: 서버 자동 동기화 / `sync_lost_animals` |
| 공공데이터 (파일, 보조) | 동물병원 진료시간 (`동물병원_전국.csv`) | facilities.opening_hours | `load_public_data --hospital-hours` |
| 자체 데이터 | 회원, 반려동물 프로필, 분실신고, 즐겨찾기 | users, pets, lost_reports (`source=user`), favorites | API |

### 매핑 규칙과 알려진 한계
- 앱의 `FacilityCategory`는 7종뿐이라 미용·위탁·장묘업, 관광공사 쇼핑(38), 문화정보원 동물병원/약국(행안부와 중복)·용품점은 적재하지 않습니다.
- 관광공사 음식점(39)은 `lclsSystm2 = FD05`일 때 `CAFE`, 그 외는 `RESTAURANT`로 분류합니다.
- 관광공사 데이터에는 동반 조건(크기·실내·목줄)이 없어 해당 필드가 `null`입니다. **`detailPetTour` API 연동이 필요**합니다.
- 행안부 동물병원 데이터에는 진료시간이 없어 `hours = null`, `openNow = false`입니다.
- 공공 분실동물의 보호자 연락처는 발견자가 연락할 수 있도록 `contactInfo`에 저장하되, 이름은 가운데를 가립니다(이건희 → `이○희 010-…`).
- 분실동물 데이터에는 종(개/고양이)과 고유 ID가 없습니다. 종은 구조동물 데이터의 품종→종 관계로 추정하고(레그돌 → 고양이), ID는 사진 URL로 만듭니다.
- `/alerts`는 구조가 분실보다 수십 배 많아서, 목록의 절반까지를 분실신고에 배정합니다.
- 진료시간은 `동물병원_전국.csv`를 이름+좌표 또는 도로명주소로 매칭해 약 2,500곳에 붙였고, 그 병원만 `openNow`가 실제로 계산됩니다.
- 앱 품종명과 농식품부 품종명이 다른 경우(진돗개→진도견, 시바견→시바, 코리안숏헤어→한국 고양이, Russian Blue→러시안 블루)는 `normalize.BREED_ALIASES`로 변환해 조회·매칭합니다.
- 지식그래프에는 증상–질병 연결이 13개 질병, 58건뿐이라 `/symptoms`는 기본적으로 연결된 22개 증상만 반환합니다 (`?all=true`면 516개 전체).

## 앱 연동 (app/)
- 앱은 기본으로 이 서버를 씁니다(`app/.../data/remote/Remote*Repository`). 루트 `.env`나 `local.properties`의 `USE_MOCK_API=true`면 기존 Mock으로, `API_BASE_URL`로 서버 주소를 바꿉니다(기본 `http://10.0.2.2:8000/`).
- `res/xml/network_security_config.xml`이 `10.0.2.2`, `localhost`, `openapi.animal.go.kr`(농식품부 이미지)에만 HTTP를 허용합니다.
- 주변 병원·시설은 `util/DeviceLocationProvider`로 위치를 구해 `lat`/`lng`로 보냅니다. 권한이 없거나 위치가 국내 범위를 벗어나면 서울시청 좌표를 씁니다(에뮬레이터 기본 위치는 미국입니다).
- 로그인 토큰은 `SessionStore`(SharedPreferences)에 저장하고, 401을 받으면 지웁니다.
- 즐겨찾기는 인터페이스가 동기라 로컬 캐시에서 읽고, 로그인 상태면 서버에 PUT/DELETE로 반영하며, 로그인할 때 서버 목록으로 새로 받습니다.

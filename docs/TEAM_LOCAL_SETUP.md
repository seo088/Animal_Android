# 팀 로컬 실행 안내

이 문서는 Android 앱, 로컬 백엔드, 카카오맵 설정을 각 팀원이 자신의 PC에서 안전하게 재현하기 위한 안내입니다. 실제 API 키, JWT 비밀값, 데이터베이스 비밀번호는 GitHub에 올리지 않습니다.

## 1. 저장소와 Android 설정

1. 저장소를 영문 경로(예: `C:\dev\Animal_Android`)에 복제합니다. Android Gradle Plugin은 한글 경로에서 빌드를 차단할 수 있습니다.
2. JDK 21과 Android SDK API 34를 설치합니다.
3. 루트의 `.env.example`을 `.env`로 복사합니다.
4. `KAKAO_MAP_KEY`에 팀에서 안전한 채널로 전달받은 **Native App Key**를 넣습니다.
5. `local.properties.example`을 `local.properties`로 복사하고, 본인 Android SDK 경로를 지정합니다.

에뮬레이터에서 로컬 백엔드를 사용할 때의 설정은 다음과 같습니다.

```properties
API_BASE_URL=http://10.0.2.2:8000/
USE_MOCK_API=false
```

`10.0.2.2`는 Android 에뮬레이터에서 개발 PC의 `127.0.0.1`을 가리킵니다. 실제 기기에서는 사용할 수 없으므로, 팀 VPN 또는 배포 서버 URL을 별도로 사용해야 합니다.

## 2. 백엔드 최초 준비

필수 프로그램은 Python 3.13, PostgreSQL, Java 17 이상, Apache Jena Fuseki입니다.

```powershell
cd backend
py -3.13 -m venv .venv
.venv\Scripts\pip install -r requirements.txt
Copy-Item .env.example .env
```

`backend/.env`에는 각자 로컬 DB 연결 정보와 긴 무작위 `JWT_SECRET`을 설정합니다. 공용 키나 실제 비밀번호를 커밋하지 마세요.

PostgreSQL을 처음 준비할 때는 `animalloo` 데이터베이스와 계정을 만들고, 다음 명령으로 포함된 데이터 스냅샷을 적재합니다.

```powershell
.venv\Scripts\python -m scripts.load_public_data
```

Fuseki 데이터셋 준비과 상세 적재 방법은 [backend README](../backend/README.md)를 따릅니다. 기존 `fuseki-data`와 PostgreSQL 데이터가 있는 경우에는 재적재하지 마세요.

## 3. 백엔드 시작 및 상태 확인

준비가 끝난 뒤 저장소 루트에서 실행합니다.

```powershell
.\backend\start-local.ps1
Invoke-RestMethod http://127.0.0.1:8000/health
```

정상 응답은 다음과 같습니다.

```json
{"database":"ok","fuseki":"ok"}
```

## 4. 비밀값 공유 원칙

- `KAKAO_MAP_KEY`, `JWT_SECRET`, `ANIMAL_API_KEY`, DB 비밀번호는 GitHub에 올리지 않습니다.
- 팀원 전달은 비공개 메신저, 비밀번호 관리자, GitHub Secrets 중 팀 규칙에 맞는 수단을 사용합니다.
- 실수로 키를 커밋했다면 즉시 카카오 개발자 콘솔에서 키를 재발급하고 Git 기록에서도 제거합니다.

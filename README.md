# AnimalLoo

공공데이터 기반 반려동물 통합 지식그래프 플랫폼 **AnimalLoo** Android Native 앱입니다.

대학 텀프로젝트 프론트엔드 저장소이며, **Java + XML + MVVM + Repository + Mock Data** 구조로 Android Studio에서 바로 빌드·실행할 수 있습니다.

> **현재 버전은 Mock Repository를 사용합니다.** 실제 백엔드 서버/API는 구현되어 있지 않으며, 향후 백엔드 팀원이 Repository 구현체만 교체하면 연동할 수 있도록 설계했습니다.

---

## 주요 기능

| 영역 | 기능 |
|------|------|
| **스플래시** | Circular Reveal, ObjectAnimator, AnimatorSet, MediaPlayer 짖는 효과음 |
| **홈** | 통계 카드, 5개 바로가기, 실시간 구조/분실 알림 RecyclerView |
| **지도** | Google Maps + 7종 카테고리 Chip 필터, Marker, BottomSheet |
| **증상진단** | 3단계(증상 → Mock 추론 결과 → 인근 병원) 단일 Fragment |
| **구조/분실** | 분실 신고 Form, Photo Picker, 캐시 저장, A/B/C 매칭, 구조동물 필터 |
| **더보기** | 동반시설(리스트↔지도), 설정, 데이터 출처, 네트워크 데모 |
| **상세** | 시설/병원/구조동물/알림/분실신고 공통 DetailFragment |
| **알림** | FCM 수신 구조, NotificationChannel, 테스트 알림 |
| **네트워크** | Retrofit `enqueue()` 기반 공공데이터 데모 API 호출 |

---

## 기술 스택

| 항목 | 내용 |
|------|------|
| 언어 | **Java 17** (Kotlin 미사용) |
| UI | XML Layout, ViewBinding, Material Components |
| Architecture | MVVM + Repository Pattern |
| 상태 | ViewModel, LiveData, UiState |
| 리스트 | RecyclerView, Adapter, ViewHolder |
| 이미지 | Glide |
| 지도 | Google Maps SDK for Android |
| 네트워크 | Retrofit 2 + OkHttp (데모), ExecutorService + Handler (Mock) |
| 푸시 | Firebase Cloud Messaging (프론트 수신 구조) |
| Gradle | Groovy DSL (`build.gradle`, `settings.gradle`) |
| SDK | minSdk 26, targetSdk 34, compileSdk 34 |

---

## 프로젝트 구조

```
app/src/main/
├── java/com/animalloo/
│   ├── AnimalLooApplication.java
│   ├── adapter/              # RecyclerView Adapter
│   ├── data/
│   │   ├── model/            # 데이터 모델, UiState, enum
│   │   ├── repository/       # Repository 인터페이스
│   │   ├── mock/             # MockRepository, MockDataProvider
│   │   └── remote/           # Retrofit API, DTO (데모)
│   ├── notification/         # FCM Service, NotificationHelper
│   ├── ui/
│   │   ├── splash/
│   │   ├── main/
│   │   ├── home/
│   │   ├── map/
│   │   ├── diagnosis/
│   │   ├── rescue/
│   │   ├── more/
│   │   ├── detail/
│   │   └── common/
│   └── util/                 # RepositoryProvider, ImageFileHelper 등
└── res/
    ├── layout/
    ├── menu/
    ├── values/
    ├── drawable/
    └── raw/                  # dog_bark.wav
```

---

## 실행 방법

### 1. 사전 요구사항

- Android Studio (Giraffe 이상 권장)
- JDK 17
- Android SDK 34

### 2. 프로젝트 열기

```bash
git clone https://github.com/seo088/Animal_Android.git
cd Animal_Android
```

Android Studio → **Open** → 프로젝트 루트 선택 → **Gradle Sync**

### 3. local.properties 설정

프로젝트 루트에 `local.properties` 파일을 생성합니다. (Git에 커밋하지 않음)

```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
```

### 4. 빌드 및 실행

- Run ▶ 버튼 또는 `./gradlew assembleDebug`
- 에뮬레이터/실기기(API 26+)에서 실행

### 5. 정상 실행 흐름

```
SplashActivity (애니메이션 + 효과음)
  → MainActivity (BottomNavigation 5탭)
    → Home / Map / Diagnosis / Rescue / More
```

---

## Google Maps API Key 설정

1. [Google Cloud Console](https://console.cloud.google.com/)에서 Maps SDK for Android 활성화
2. API Key 발급
3. `local.properties`에 추가:

```properties
MAPS_API_KEY=발급받은_API_KEY
```

4. `app/build.gradle`의 `manifestPlaceholders`가 자동으로 Manifest에 주입합니다.

**API Key가 없는 경우:** 지도 탭은 리스트 fallback UI로 동작하며, 앱의 다른 기능은 정상 실행됩니다.

---

## Firebase 설정

FCM 수신 구조는 코드에 포함되어 있습니다. 실제 푸시 수신을 테스트하려면 Firebase 프로젝트 설정이 필요합니다.

### 설정 절차

1. [Firebase Console](https://console.firebase.google.com/)에서 Android 앱 등록
   - 패키지명: `com.animalloo`
2. `google-services.json` 다운로드
3. `app/google-services.json` 경로에 배치

```bash
cp app/google-services.json.example app/google-services.json
# example 파일을 참고하여 Firebase Console 값으로 교체
```

> `google-services.json`은 `.gitignore`에 포함되어 **GitHub에 올리지 않습니다.**

### Firebase 없이 실행

- `google-services.json` 없이도 **빌드·실행 가능**합니다.
- **더보기 → 설정**에서 **구조/분실 알림 테스트** 버튼으로 로컬 Notification을 확인할 수 있습니다.

---

## Mock Data 구조

| Repository | Mock 구현체 | 데이터 규모 |
|------------|-------------|-------------|
| FacilityRepository | MockFacilityRepository | 시설 18개 |
| HospitalRepository | MockHospitalRepository | 병원 12개 |
| AnimalRepository | MockAnimalRepository | 구조동물 16마리 |
| AlertRepository | MockAlertRepository | 알림 12건 |
| DiagnosisRepository | MockDiagnosisRepository | 질병 추론 8건 |
| LostAnimalRepository | MockLostAnimalRepository | 분실 신고 + 매칭 |
| HomeRepository | MockHomeRepository | 홈 통계 |
| PublicDataRepository | RetrofitPublicDataRepository | JSONPlaceholder 데모 API |

Mock 데이터는 `MockDataProvider`에서 중앙 관리합니다.  
ViewModel은 Mock List를 직접 읽지 않고 **반드시 Repository Interface**를 통해 접근합니다.

비동기 Mock 조회는 `MockAsyncHelper`(ExecutorService + Handler)를 사용합니다.

---

## 백엔드 연동 예정 지점

백엔드 담당자는 아래 Interface의 Mock 구현체를 **Remote 구현체로 교체**하면 됩니다.

| Interface | 교체 대상 파일 (예시) |
|-----------|----------------------|
| `FacilityRepository` | `RemoteFacilityRepository` |
| `HospitalRepository` | `RemoteHospitalRepository` |
| `AnimalRepository` | `RemoteAnimalRepository` |
| `LostAnimalRepository` | `RemoteLostAnimalRepository` |
| `DiagnosisRepository` | `RemoteDiagnosisRepository` |
| `AlertRepository` | `RemoteAlertRepository` |
| `HomeRepository` | `RemoteHomeRepository` |

교체 위치: `RepositoryProvider.java`

```java
// 현재
facilityRepository = new MockFacilityRepository();

// 연동 후 (예시)
facilityRepository = new RemoteFacilityRepository(apiService);
```

Retrofit Interface/DTO는 `data/remote/` 패키지에 추가하면 됩니다.

---

## 팀원이 수정하면 안 되는 프론트 구조

아래 구조는 프론트엔드 아키텍처의 핵심입니다. **임의 변경 시 화면 간 충돌·상태 초기화 문제**가 발생할 수 있습니다.

1. **View → ViewModel → Repository Interface** 단방향 의존
2. ViewModel에서 JSON/Mock List 직접 접근 금지
3. `RepositoryProvider`를 통한 Repository 주입
4. Fragment `onDestroyView()`에서 `binding = null`
5. MainActivity의 BottomNavigation show/hide Fragment 패턴
6. `DetailNavigator` / `MainNavigator` 인터페이스 기반 화면 전환
7. Java-only, XML UI-only (Kotlin/Compose 금지)

백엔드 팀원은 **`data/repository/` Interface**와 **`data/remote/`** 영역만 확장하는 것을 권장합니다.

---

## GitHub 협업 시 주의사항

| 항목 | 설명 |
|------|------|
| **커밋 금지** | `local.properties`, `google-services.json`, API Key, Secret |
| **커밋 금지** | `AGENTS.md`, `scripts/` (로컬 전용) |
| **브랜치** | 기능별 feature branch → PR 권장 |
| **Gradle Sync** | pull 후 Android Studio Gradle Sync 필수 |
| **Mock 유지** | 백엔드 미완성 시 MockRepository 유지, Remote는 별도 구현체로 추가 |
| **충돌 주의** | `RepositoryProvider`, `MainActivity`, `strings.xml` 동시 수정 시 주의 |

---

## 수업 평가요소 확인 가이드

| 평가요소 | 확인 위치 |
|----------|-----------|
| ConstraintLayout / LinearLayout / FrameLayout | 각 화면 XML (`fragment_*.xml`, `activity_*.xml`) |
| EditText / Spinner / Button | **구조 → 분실 신고** |
| ChipGroup | **지도**, **증상진단**, **동반시설** |
| Options Menu | **홈** 툴바 새로고침 |
| Context Menu | **홈 알림** 롱클릭, **구조동물** 롱클릭 |
| RecyclerView | 홈, 지도 fallback, 진단, 구조, 더보기 등 |
| File 처리 | **분실 신고** Photo Picker → 캐시 저장 |
| Glide | 홈 알림, 구조동물, DetailFragment |
| ObjectAnimator / AnimatorSet / Circular Reveal / MediaPlayer | **SplashActivity** |
| Network Thread | **MockAsyncHelper**, **RetrofitPublicDataRepository**, **데이터 출처 → 데모 API** |
| ViewModel / LiveData | 각 Feature ViewModel |
| Repository Pattern | `data/repository/` + `data/mock/` |
| FCM | **설정 → 알림 테스트**, `AnimalLooFirebaseMessagingService` |

---

## 라이선스 / 데이터 안내

- Mock 데이터의 시설명·지역 등은 **데모용 가상 데이터**입니다.
- 실제 공공데이터 API 연동은 백엔드 완료 후 Remote Repository로 교체 예정입니다.

---

## 문의

프론트엔드 Android 담당: GitHub Issues 또는 팀 Slack/카카오톡 채널을 이용해 주세요.

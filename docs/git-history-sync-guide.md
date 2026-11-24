# 커밋 기록 정리 후 동기화 안내

## 무엇이 바뀌었나요

- 작업 일시: 2026-11-24 21:36
- 대상 브랜치: `main`, `Backend`, `test` (`Frontend`는 해당 없음)
- 일부 커밋 메시지 끝의 `Co-Authored-By: Claude ...` 줄을 지웠습니다. 저장소 사용자 목록에 claude가 표시되던 문제를 정리하기 위해서입니다.
- **코드, 작성자, 날짜, 커밋 수는 바뀌지 않았습니다.** 바뀐 것은 커밋 ID(해시)뿐입니다.

커밋 ID가 바뀌었기 때문에, 위 작업 일시 이전에 clone하거나 pull한 PC에서는 아래 동기화를 **한 번** 해야 합니다.

## 꼭 지켜 주세요

- 예전 PC 사본에서 **`git pull`을 바로 하지 마세요.** 옛 기록과 새 기록이 합쳐져서 커밋이 중복으로 생기고, 커밋 수가 늘어나며, Claude 표시도 다시 생깁니다.
- 예전 사본에서 동기화하지 않은 채 push하지 마세요.

## 동기화 방법

### 방법 1. 새로 clone하기 (가장 간단)

예전 폴더는 지우거나 이름을 바꾸고 새로 받습니다.

```
git clone https://github.com/seo088/Animal_Android.git
```

`local.properties`, `.env`처럼 저장소에 올라가지 않는 설정 파일은 예전 폴더에서 복사해 오세요.

### 방법 2. 기존 폴더 그대로 맞추기 (push 안 한 작업이 없을 때)

```
git fetch origin
git checkout main
git reset --hard origin/main
```

`Backend`, `test` 브랜치도 로컬에 있다면 같은 방식으로 맞춥니다.

```
git checkout Backend
git reset --hard origin/Backend
git checkout test
git reset --hard origin/test
```

`reset --hard`는 로컬에서 커밋하지 않은 변경을 지웁니다. 남겨야 할 변경이 있으면 먼저 방법 3을 보세요.

### 방법 3. push 안 한 작업이 있을 때

```
git fetch origin
git checkout <내 작업 브랜치>
git branch backup-mywork
git rebase origin/<main 또는 Backend 또는 test>
```

- `backup-mywork`는 혹시 모를 상황에 대비한 백업입니다.
- 충돌이 나거나 결과가 이상하면 push하지 말고 김서영에게 연락해 주세요.

## 동기화 확인

```
git log --oneline -3
```

결과의 커밋 ID가 GitHub 화면의 최신 커밋 ID와 같으면 끝입니다. 이후에는 평소처럼 `git pull`과 `git push`를 써도 됩니다.

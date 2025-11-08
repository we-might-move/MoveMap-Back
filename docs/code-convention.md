# Git Convention

## Branch

- `main`
- `develop`
- `feat/{도메인명}`
- `fix/{도메인명}`
- `refactor/{도메인명}`

### 예시

`feat/auth` 혹은 `feat/member`

## Commit

### 커밋 형식

```
<tag>: <subject>
<BLANK LINE>
<body>
```

### tag

- `Feat`: 새로운 기능 추가, 기존의 기능을 요구 사항에 맞추어 수정
- `Fix`: 버그 수정
- `Docs`: 문서 작성
- `Style`: 코드 포맷팅, 오타 수정 등
- `Refactor`: 코드 리팩터링 등
- `Chore`: 빌드 및 패키지 수정 및 삭제 등 비즈니스 로직 외 작업
- `Test`: 테스트 코드 추가/수정
- `Release`: 버전 릴리즈
- `Comment`: 주석 관련 작업

### subject

- 50자 이내 작성
- 마침표(.) 또는 특수문자 포함 X
- 간결하고 요점적인 서술

### body

- 추가적인 설명이 필요한 경우 작성 (Optional)
- 구현 방법에 대한 내용은 작성 X → PR을 이용할 것
- 무엇을 왜 변경했는지에 대한 내용을 포함

### 예시

```
Feat: 로그인 기능 구현
```

```
Feat: 회원가입 기능 수정

기획 요구사항 변경에 따라
회원가입 시 성별 정보도 같이 입력받아 저장되도록 수정
```

## Issue

- 담당자 (Assignees) 명시
- 내용에 맞는 Label 설정
- Task list 활용

### Labels

- Plan: 주차별 계획 작성
- Feature: 기능 관련
- Bug: 버그 관련
- Refactoring: 코드 리팩토링 관련
- Documentation: 문서 작성 관련
- Deployment: 배포 관련
- Question: 논의가 필요한 PR 관련

## Pull Requests

- 담당자 (Assignees) 명시
- **Issue 와 연동하기**

### PR 템플릿

```
## 🛠️ 구현 기능
  - 구현한 (또는 구현할) 기능을 요약해 작성

## ❔ 구현 방법
  - 구현 방법 설명 또는 논의가 필요한 내용 작성 (Optional)

## 💭 구현 결과
  - Swagger 캡처 이미지 등 (Optional)

## 🎯 Resolve
  - 이슈 태그(ex: #7)
```

## Code Review

- 리뷰어는 **사전에 정의된 Emoji를 코멘트의 제일 앞에 붙이는 방식으로 코드를 리뷰**한다.
- ❗ 관련 코멘트가 하나 이상 존재할 경우 Request changes 상태로 리뷰 제출
- 본인이 남긴 코멘트에 대한 답변이 리뷰 결과에 영향을 주는 경우, 의견 보류의 의미로 Comment 상태로 리뷰 제출
- [ref.](https://wormwlrm.github.io/2024/02/04/Code-Review-with-Emoji.html)

### Emoji

- 👍 (:thumb) - 리뷰이의 코드에 칭찬을 남기고 싶을 때 사용
- ❗ (:exclamation) - 리뷰이의 코드에 필수적인 코드 수정을 요청할 때 사용
- ❓ (:question) - 리뷰이의 코드에서 이해하기 어렵거나 궁금한 점이 있을 때 사용
- 💊 (:pill) - 리뷰어가 리뷰이의 코드에서 개선된 방법을 제안하지만 그것의 반영이 필수까지는 아닐 때 사용
- 💬 (:speech_balloon) - 리뷰어가 단순히 개인의 감상이나 의견, 여담을 남거나 공유하고 싶을 때 사용

# Coding Convention

## Naming Rules

### 변수명 작성 시 정관사 또는 전치사는 최대한 생략

```
int countOfFollower   //(X)
int followerCount     //(O)
```

### Boolean 변수 네이밍

> is + 명사 : ~인가?
>
>
> is + 현재진행형 : ~하는 중인가?
>
> is + 형용사 : ~한 상태인가?
>
> is + 과거분사 (수동태) : ~됐는가?
>

```
boolean isMember;
boolean isConnecting;
boolean isActive;
boolean isEditable;
boolean isDeleted;
```

> has + 명사 : ~을 가지고있는가?
>
>
> has + 과거분사 : (과거에 했던 동작이나 상태) 가 유지되고 있는가?
>

```
hasAccount;
hasConnected;
```

> supports : ~을 지원하는가?
>
>
> includes : ~을 포함하는가?
>
> shows : ~을 보여줄 것인가?
>
> allows : ~을 허용할 것인가?
>
> accepts : ~을 받아주는가?
>
> contains : ~을 포함하고 있는가?
>

```
includesReview;
allowsViewing;
```

## Controller

- Swagger 문서를 위해 @Operation 어노테이션 활용

## Service

- 인터페이스랑 구현체로 분리
- 여러 레포지토리 주입받아서 비즈니스 로직 완성

## DTO

- record 클래스로 구현
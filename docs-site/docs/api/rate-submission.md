---
sidebar_position: 1
title: 단가 제출 API
description: POST /v1/submissions 엔드포인트 명세 및 검증 규칙
---

## 엔드포인트 목록

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| `POST` | `/v1/submissions` | Bearer JWT 필요 | 단가 제보 생성 |
| `GET` | `/v1/submissions/{id}` | Bearer JWT 필요 | 단가 제보 단건 조회 |
| `DELETE` | `/v1/submissions/{id}` | Bearer JWT 필요 | 단가 제보 삭제 (소프트 삭제) |

`@SecurityRequirement(name = "BearerAuth")` 가 컨트롤러 클래스에 선언되어 있어 모든 엔드포인트에 인증이 필요하다.

---

## POST /v1/submissions

단가 제보를 생성한다.

### 요청

**Content-Type:** `application/json`

**헤더:**

```
Authorization: Bearer <JWT>
```

**요청 바디:**

| 필드 | 타입 | 필수 | 제약 | 설명 |
|------|------|------|------|------|
| `jobCategoryId` | Long | 필수 | — | 직무 카테고리 ID |
| `experienceLevelId` | Long | 필수 | — | 경력 수준 ID |
| `userId` | Long | 선택 | — | 연결할 사용자 ID. 없으면 null |
| `submissionType` | String | 필수 | `TRACK_A` \| `TRACK_B` | 제보 트랙 |
| `workFormat` | String | 필수 | `ON_SITE` \| `REMOTE` \| `HYBRID` | 근무 형태 |
| `duration` | String | 선택 | 최대 50자 | 계약 기간 |
| `amount` | Integer | 필수 | 최솟값 10 | 단가 금액 (만원) |
| `amountUnit` | String | 필수 | `MONTHLY` \| `TOTAL` | 금액 단위 |
| `sessionId` | UUID | 필수 | — | 클라이언트 세션 ID |

`duration` 필드에 사용 가능한 문자열 값 (`amountUnit = TOTAL` 일 때 월 환산에 사용):

| 값 | 환산 개월 수 |
|----|-------------|
| `1주일 이하` | 0.25개월 |
| `2~3주` | 0.625개월 |
| `1개월` | 1개월 |
| `2~3개월` | 2.5개월 |
| `3개월 이상` | 3개월 |
| 숫자 문자열 | 해당 숫자를 개월 수로 파싱 |

위 목록에 없는 문자열이 오거나 `duration` 이 null이면 `normalizedMonthly` 는 계산되지 않는다 (`null` 반환).

**요청 예시:**

```json
{
  "jobCategoryId": 1,
  "experienceLevelId": 2,
  "userId": 12,
  "submissionType": "TRACK_A",
  "workFormat": "REMOTE",
  "duration": "2~3개월",
  "amount": 500,
  "amountUnit": "TOTAL",
  "sessionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 검증 규칙

검증은 두 단계에서 수행된다.

**1단계 — Bean Validation** (`RateSubmissionRequest.java`):

- `amount`: `@Min(10)` — 10 미만이면 400 Bad Request 반환.
- 필수 필드 누락: `@NotNull` 위반 시 400 Bad Request 반환.

**2단계 — 서비스 레이어** (`RateSubmissionService.java`, lines 51–54):

```java
Integer normalized = submission.getNormalizedMonthly();
if (normalized != null && (normalized < 10 || normalized > 9999)) {
    throw new IllegalArgumentException("환산 월 단가는 10~9,999만원 범위여야 합니다.");
}
```

`normalizedMonthly` 가 계산된 경우 10~9,999만원 범위를 벗어나면 400 Bad Request 반환.
`normalizedMonthly` 가 null이면 범위 검사를 건너뛴다.

### normalizedMonthly 계산 로직

`src/main/java/com/olma/domain/entity/RateSubmission.java` (lines 92–103):

- `amountUnit = MONTHLY`: `normalizedMonthly = amount`
- `amountUnit = TOTAL`: `normalizedMonthly = amount / parsedMonths` (HALF_UP 반올림)
- `duration` 파싱 실패 또는 null: `normalizedMonthly = null`

### 응답

**HTTP 201 Created**

```json
{
  "id": 45,
  "jobCategoryName": "UI/UX 디자이너",
  "experienceLevelLabel": "3~5년",
  "submissionType": "TRACK_A",
  "workFormat": "REMOTE",
  "duration": "2~3개월",
  "amount": 500,
  "amountUnit": "TOTAL",
  "normalizedMonthly": 200,
  "createdAt": "2026-06-27T12:34:56.789+09:00"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 생성된 제보 ID |
| `jobCategoryName` | String | 직무 카테고리명 |
| `experienceLevelLabel` | String | 경력 수준 레이블 |
| `submissionType` | String | 제보 트랙 |
| `workFormat` | String | 근무 형태 |
| `duration` | String | 계약 기간 (nullable) |
| `amount` | Integer | 입력 단가 금액 |
| `amountUnit` | String | 금액 단위 |
| `normalizedMonthly` | Integer | 환산 월 단가 (nullable) |
| `createdAt` | OffsetDateTime | 생성 시각 (KST offset 포함) |

### 오류 응답

| HTTP 상태 | 원인 |
|-----------|------|
| 400 | Bean Validation 실패 (`amount < 10` 또는 필수 필드 누락) |
| 400 | `normalizedMonthly` 가 10~9,999 범위를 벗어남 |
| 400 | 존재하지 않는 `jobCategoryId` 또는 `experienceLevelId` |
| 401 | JWT 미제공 또는 유효하지 않은 JWT |

---

## GET /v1/submissions/\{id\}

단가 제보를 단건 조회한다.

### 요청

**헤더:**

```
Authorization: Bearer <JWT>
```

**경로 파라미터:**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `id` | Long | 단가 제보 ID |

### 응답

**HTTP 200 OK** — POST 응답과 동일한 `RateSubmissionResponse` 구조.

### 오류 응답

| HTTP 상태 | 원인 |
|-----------|------|
| 401 | JWT 미제공 또는 유효하지 않은 JWT |
| 404 | 해당 ID의 제보가 존재하지 않음 |

---

## DELETE /v1/submissions/\{id\}

단가 제보를 숨김 처리한다. 물리 삭제가 아닌 소프트 삭제다 (`status = HIDDEN`).

### 요청

**헤더:**

```
Authorization: Bearer <JWT>
```

**경로 파라미터:**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `id` | Long | 단가 제보 ID |

### 응답

**HTTP 204 No Content** — 응답 바디 없음.

### 오류 응답

| HTTP 상태 | 원인 |
|-----------|------|
| 401 | JWT 미제공 또는 유효하지 않은 JWT |
| 404 | 해당 ID의 제보가 존재하지 않음 |

---

## 로그

제보 생성 성공 시 다음 로그가 남는다 (`RateSubmissionService.java` line 57):

```
INFO rate submission created submissionId=45 userId=12
```

`userId` 는 요청 바디의 `userId` 값이다. 비로그인 제보인 경우 `null` 로 기록된다.

---

## CORS

`src/main/java/com/olma/config/WebConfig.java` (line 36):

```java
.allowCredentials(true)
```

`allowCredentials(true)` 가 설정되어 있어 클라이언트가 쿠키 또는 `Authorization` 헤더를 포함한 크로스 오리진 요청을 보낼 수 있다. `allowedOriginPatterns("*")` 와 함께 사용 중이다.

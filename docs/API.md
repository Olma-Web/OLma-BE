# OLma Backend API 사용 가이드

> 프론트엔드 분들을 위한 빠른 시작 가이드입니다. 막히는 게 있으면 백엔드에 바로 물어봐 주세요.

## 1. 환경

| 환경 | Base URL |
|---|---|
| 개발 (현재) | `http://13.124.31.106` |
| 프로덕션 | (도메인 정해지면 여기에) |

- HTTP 입니다 (HTTPS 아님). 도메인 등록 후 Caddy 가 자동으로 인증서 받으면 HTTPS 됩니다.
- CORS 는 모든 origin 허용 (인증 없으니 자유롭게 호출 가능).

## 2. 인증

**현재 없습니다.** 모든 엔드포인트가 인증 없이 호출 가능합니다.
- `/v1/users/{userId}/...` 같은 경로의 `userId` 는 path 로 직접 받습니다 (임시).
- 추후 회원가입/로그인 도입되면 `Authorization: Bearer ...` 헤더 + `/v1/users/me/...` 로 바뀔 예정.

## 3. 빠른 시작 (cURL)

```bash
# 1) 차트 통계 — 웹 UI/UX 미들 디자이너 단가 분포
curl "http://13.124.31.106/v1/benchmark?jobCategoryId=14&experienceLevelId=3"

# 2) 단가 제출
curl -X POST http://13.124.31.106/v1/submissions \
  -H 'Content-Type: application/json' \
  -d '{
    "jobCategoryId": 14,
    "experienceLevelId": 3,
    "submissionType": "TRACK_A",
    "workFormat": "REMOTE",
    "amount": 500,
    "amountUnit": "MONTHLY",
    "sessionId": "11111111-1111-1111-1111-111111111111"
  }'

# 3) Swagger UI (브라우저에서 열기)
open http://13.124.31.106/swagger-ui/index.html
```

## 4. 기능 ↔ API 매핑

기능명세서의 F# 와 실제 API 호출을 매핑한 표입니다. **FE 가 어떤 화면에서 뭘 호출해야 할지** 가 핵심입니다.

| 기획서 | 동작 | API |
|---|---|---|
| **F0 회원가입/로그인** | 인증 | ❌ 미구현 |
| **F1 챗봇 데이터 수집 (Q1~Q8)** | 답변 모음 → 제출 | `POST /v1/submissions` |
| **F2 Track 분기** | TRACK_A vs TRACK_B | `submissionType` 필드로 구분 |
| **F3 결과 분석 로딩** | UX 화면 | (FE 처리) |
| **F4 차트 + 통계** | 분포/중앙값 | `GET /v1/benchmark` |
| **F4 자격증 비율 툴팁** | "10명 중 7명" | benchmark 응답의 `distribution[].certRatio` |
| **F4 평균 작업 기간 툴팁** | 건별 계약 | benchmark 응답의 `distribution[].mostCommonDuration` |
| **F4 내 단가 마커** | userPercentile | benchmark 호출 시 `userAmount` 쿼리 파라미터 추가 |
| **F6 이상치 절사** | 자동 백그라운드 | BE 가 매일 04:00 KST 자동 마킹 |
| **F6 데이터 부족 방어** | 빈 차트 안내 | benchmark 응답의 `n: 0` 으로 판단 |
| **F7-1 커리어 대시보드** | 프로필 | `GET /v1/users/{userId}` |
| **F7-2 기본 스펙 수정** | 직군/연차/자격증 | `PUT /v1/users/{userId}/profile` |
| **F7-3 히스토리 타임라인** | 내 카드 리스트 | `GET /v1/users/{userId}/submissions` |
| **F7-3 카드 삭제** | 카드 우측 상단 | `DELETE /v1/submissions/{id}` |
| **F8-1 활동 내역** | 커뮤니티 | ❌ 미구현 (F10 의존) |
| **F8-2 비밀번호 변경** | | ❌ 미구현 |
| **F8-2 로그아웃** | | ❌ 미구현 |
| **F8-2 탈퇴** | | ❌ 미구현 |
| **F9 견적 계산기 전체** | | ❌ 미구현 (테이블만 존재) |
| **F10 커뮤니티** | | ❌ MVP 제외 |

## 5. 마스터 데이터 가져오기 (셀렉트박스 채우기)

FE 가 직접 ID 를 하드코딩하지 마시고 아래 reference 엔드포인트로 받아오세요. 백엔드에서 시드 데이터가 바뀌어도 자동 반영됩니다.

| 용도 | API |
|---|---|
| 직군 목록 (계층형) | `GET /v1/reference/job-categories` |
| 연차 목록 | `GET /v1/reference/experience-levels` |
| 지역 목록 | `GET /v1/reference/regions` |
| 업무 유형 | `GET /v1/reference/work-types` |
| 자격증 종류 | ❌ 아직 엔드포인트 없음 (BE 에 요청 필요) |

## 6. 자주 쓰는 ID 표 (개발 중 참고)

스펙대로면 디자인 직군 2개만 사용합니다.

### 직군 (`jobCategoryId`)
| ID | slug | 이름 |
|---|---|---|
| 14 | `web-uiux` | 웹 UI/UX |
| 28 | `app-uiux` | 앱 UI/UX |

### 연차 (`experienceLevelId`)
| ID | label |
|---|---|
| 1 | 1년 미만 |
| 2 | 1~3년 차 (주니어) |
| 3 | 4~6년 차 (미들) |
| 4 | 7~9년 차 (시니어) |
| 5 | 10년 차 이상 (리더) |

### 자격증 (`certificateTypeId`)
| ID | 이름 |
|---|---|
| 1 | 웹디자인기능사 |
| 2 | 시각디자인기사·산업기사 |
| 3 | 기타 자격증 |

### Enum 값
| 필드 | 가능한 값 |
|---|---|
| `submissionType` | `TRACK_A` (이미 정해진 단가) / `TRACK_B` (견적 산정) |
| `workFormat` | `ON_SITE` / `REMOTE` / `HYBRID` |
| `amountUnit` | `MONTHLY` / `TOTAL` |
| `duration` (TOTAL 만 필요) | `1주일 이하` / `2~3주` / `1개월` / `2~3개월` / `3개월 이상` |

## 7. 응답 예시

### `GET /v1/benchmark?jobCategoryId=14&experienceLevelId=3`

```json
{
  "n": 61,
  "p10": 410,
  "p25": 470,
  "median": 515,
  "p75": 620,
  "p90": 720,
  "userPercentile": null,
  "distribution": [
    {
      "bucket": 1,
      "rangeStart": 398,
      "rangeEnd": 411,
      "count": 5,
      "cohortSize": 1,
      "certHoldersCount": 0,
      "certRatio": 0.0,
      "mostCommonDuration": "1개월"
    }
  ]
}
```

- `n`: 표본 수 (이상치 제외, ACTIVE 만)
- `p10`~`p90`: 백분위수 (만 원 단위)
- `median`: 차트 중앙값
- `userPercentile`: 사용자가 `userAmount` 쿼리로 자기 단가 보내면 `0.0~100.0` 으로 반환 (Track A 의 "📍 내 단가" 마커용)
- `distribution[]`: 막대그래프 버킷
  - `cohortSize`: 그 버킷의 회원 가입자 수
  - `certHoldersCount` / `certRatio`: F4 자격증 비율 툴팁용
  - `mostCommonDuration`: F4 건별 평균 기간 툴팁 (TOTAL 계약만 집계, MONTHLY 라면 무시)

### `GET /v1/users/1`

```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "지민",
  "jobCategoryId": 14,
  "jobCategoryName": "웹 UI/UX",
  "experienceLevelId": 3,
  "experienceLevelLabel": "4~6년 차 (미들)",
  "certificates": [
    { "id": 1, "name": "웹디자인기능사" }
  ]
}
```

### `PUT /v1/users/1/profile`

```json
// 요청
{
  "jobCategoryId": 28,
  "experienceLevelId": 4,
  "certificateTypeIds": [1, 2]
}
```

응답은 위 GET 과 동일 형태로 갱신된 프로필 반환.

### `POST /v1/submissions`

```json
// 요청 - Track A (실제 데이터, 월 단위)
{
  "jobCategoryId": 14,
  "experienceLevelId": 3,
  "userId": 1,                    // 회원이면 넣고 익명이면 생략
  "submissionType": "TRACK_A",
  "workFormat": "REMOTE",
  "amount": 500,
  "amountUnit": "MONTHLY",
  "sessionId": "11111111-1111-1111-1111-111111111111"
}

// 요청 - Track B (희망 단가, 건별)
{
  "jobCategoryId": 14,
  "experienceLevelId": 3,
  "submissionType": "TRACK_B",
  "workFormat": "ON_SITE",
  "duration": "2~3개월",          // TOTAL 일 때만 필수
  "amount": 1500,
  "amountUnit": "TOTAL",
  "sessionId": "11111111-1111-1111-1111-111111111111"
}
```

응답: `201 Created` + 저장된 객체 반환.

## 8. 에러 응답

현재 별도 에러 포맷은 없고 Spring 기본 에러 응답입니다.

- `400 Bad Request`: validation 실패 (필수 필드 누락, 잘못된 enum 등)
- `404 Not Found`: 존재하지 않는 리소스 (잘못된 userId, submissionId 등)
- `500 Internal Server Error`: BE 버그. 즉시 백엔드에 알려주세요.

## 9. Swagger UI

브라우저에서 직접 호출 / 응답 확인 가능합니다.

- **UI**: http://13.124.31.106/swagger-ui/index.html
- **OpenAPI JSON**: http://13.124.31.106/v3/api-docs (Postman, Bruno 등에 import 가능)

## 10. 헬스체크

- http://13.124.31.106/actuator/health → `{"status":"UP"}` 면 정상

---

## 11. 막히면

- API 자체 문제 같으면: 백엔드 슬랙
- 기획 의도 모호: 프로덕트 팀
- "이거 어떻게 호출하지?" 모르겠으면: 일단 Swagger UI 에서 직접 눌러보기

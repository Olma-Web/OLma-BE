# OLma Backend

OLma는 IT 직무별 외주/채용 단가 데이터를 수집하고, 이를 기반으로 벤치마크와 견적 계산, 커뮤니티 기능을 제공하는 Spring Boot 백엔드 프로젝트입니다.

## 프로젝트 목적

OLma 백엔드는 단가 제보 데이터를 안정적으로 수집하고, 이를 벤치마크/견적/커뮤니티 도메인에서 재사용할 수 있도록 API와 운영 기반을 제공합니다.

- 도메인별 책임을 분리해 단가 제보, 벤치마크, 견적, 커뮤니티 기능을 독립적으로 관리합니다.
- JWT 기반 인증, 전역 예외 처리, 요청 로깅을 통해 API 응답과 운영 로그 형식을 표준화합니다.
- PostgreSQL, JPA, Flyway를 사용해 데이터 모델과 마이그레이션 이력을 관리합니다.
- GitHub Actions, Docker, EC2, RDS 기반 배포 흐름을 구성합니다.
- Swagger/OpenAPI로 API 스키마를 제공하고, 상세 기술 문서는 별도 문서 레포에서 관리합니다.

## 공개 링크

| 구분 | 링크 |
|------|------|
| 기술 문서 | https://docs.olma.kro.kr/ |
| Swagger UI | https://api.olma.kro.kr/swagger-ui.html |
| OpenAPI JSON | https://api.olma.kro.kr/v3/api-docs |
| Grafana | https://grafana.olma.kro.kr/ |

## 핵심 기능

| 도메인 | 기능 |
|--------|------|
| Auth | 회원가입, 로그인, 로그아웃, JWT 발급/검증 |
| RateSubmission | 직무/경력/근무 형태별 단가 제보 생성, 조회, 수정, 소프트 삭제 |
| Benchmark | 제보 데이터를 기반으로 직무별 단가 통계와 사용자 입력값 비교 |
| Estimate | 벤치마크와 옵션 배율을 이용한 외주 견적 계산 및 저장 |
| Community | 게시글/댓글 CRUD, 좋아요, 신고, 내 활동 조회 |
| UserProfile | 프로필 조회/수정, 제보 타임라인, 비밀번호 변경, 회원 탈퇴 |
| ReferenceData | 직무, 경력, 지역, 근무 형태 등 공통 기준 데이터 조회 |

## 기술 스택

| 영역 | 사용 기술 |
|------|-----------|
| Language / Framework | Java 21, Spring Boot 3.3.7 |
| Persistence | Spring Data JPA, PostgreSQL 17, Flyway |
| Auth / Security | JJWT, BCryptPasswordEncoder |
| API / Docs | springdoc-openapi, Swagger UI |
| Observability | Actuator, RequestLoggingFilter, MDC, Promtail, Loki, Grafana |
| Test / Build | JUnit, Testcontainers, Gradle |
| Deploy | Docker, GHCR, GitHub Actions, EC2, RDS, Caddy |

## 아키텍처 요약

```text
Client
  -> Caddy
  -> Spring Boot API
      -> RequestLoggingFilter
      -> JwtFilter
      -> Controller
      -> Service
      -> Repository / Entity
      -> PostgreSQL
```

패키지는 다음 기준으로 분리했습니다.

```text
src/main/java/com/olma/
├── config/       # 인증 필터, CORS, Swagger, Jackson, 로깅 설정
├── controller/   # HTTP 요청 진입점
├── service/      # 트랜잭션 단위 비즈니스 로직
├── domain/       # 엔티티, 레포지토리, enum
├── dto/          # 요청/응답 DTO
└── exception/    # 전역 예외 응답과 커스텀 예외
```

## 실행 방법

### 1. 환경 변수

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=olma
export DB_USERNAME=olma
export DB_PASSWORD=olma
export JWT_SECRET=local-development-secret-key
```

### 2. 테스트

```bash
./gradlew test
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

기본 포트는 `8080`이며, 로컬 실행 후 Swagger UI는 `http://localhost:8080/swagger-ui.html`에서 확인할 수 있습니다.

## 문서

상세 기술 문서는 별도 레포에서 관리합니다.

- 문서 사이트: https://docs.olma.kro.kr/
- 문서 레포: https://github.com/Olma-Web/OLma-Docs

## 설계 및 운영 기준

- 인증은 Spring Security Starter 없이 `JwtFilter`에서 직접 처리하고, 컨트롤러는 `HttpServletRequest`의 `userId` 속성을 사용합니다.
- `GlobalExceptionHandler`로 예외 응답 형식을 `timestamp/status/error/message/path/fieldErrors` 구조로 통일했습니다.
- `RequestLoggingFilter`에서 `requestId`를 생성하고 MDC에 저장해 요청 단위 로그 추적이 가능하도록 했습니다.
- 단가 제보의 `amountUnit`, `duration`을 이용해 월 단가(`normalizedMonthly`)를 계산하고, 벤치마크/견적 도메인에서 활용할 수 있게 했습니다.
- 운영 문서에는 CI/CD, EC2/RDS, Docker, Caddy, 모니터링 파이프라인의 실제 구성과 한계를 함께 기록했습니다.

## 구현 한계 및 개선 계획

현재 구현 기준으로 추적 중인 개선 대상입니다.

| 항목 | 현재 상태 | 개선 방향 |
|------|-----------|-----------|
| 단가 제보 삭제 권한 | `DELETE /v1/submissions/{id}`는 소유권 검사를 하지 않고 `id`만으로 소프트 삭제합니다. | 요청자의 `userId`와 제보 소유자를 비교하고, 타 사용자 삭제 시 404 또는 403을 반환하는 테스트를 추가합니다. |
| 숨김 제보 조회 | `RateSubmissionService.getById()`는 `HIDDEN` 상태도 그대로 반환합니다. | 단건 조회에서도 상태 필터링 정책을 정하고, 목록/상세 조회 동작을 일관화합니다. |
| CORS 정책 | 운영 환경에서도 와일드카드 오리진과 credentials 허용이 적용됩니다. | 프로파일별 허용 오리진을 분리하고 운영 도메인만 허용합니다. |
| 인프라 적용 상태 | Terraform은 API HTTPS 도메인과 8080 루프백 바인딩 기준으로 정리되어 있습니다. | 실제 EC2 보안 그룹/컨테이너 설정이 Terraform desired state와 일치하는지 배포 후 확인합니다. |
| API 문서 범위 | 대표 도메인 중심으로 Swagger 설명을 보강한 상태입니다. | Community, Estimate 등 주요 도메인으로 `@Operation`, `@ApiResponse`, DTO `@Schema`를 확장합니다. |

## 관련 문서

- 기술 문서: https://docs.olma.kro.kr/
- 문서 레포: https://github.com/Olma-Web/OLma-Docs
- 로컬 개발 환경: https://docs.olma.kro.kr/getting-started/local-development
- 프론트엔드 개요: https://docs.olma.kro.kr/frontend/overview
- 프론트엔드 API 연동: https://docs.olma.kro.kr/frontend/api-integration
- 백엔드 요청 처리 흐름: https://docs.olma.kro.kr/development/request-flow
- API 공통 규격: https://docs.olma.kro.kr/api/common
- 도메인별 API 요약: https://docs.olma.kro.kr/api/domain-summary
- 단가 제출 API 가이드: https://docs.olma.kro.kr/api/rate-submission
- 운영/배포 문서: https://docs.olma.kro.kr/ops/deploy
- 런타임 설정: https://docs.olma.kro.kr/ops/runtime-configuration
- 로깅/모니터링 문서: https://docs.olma.kro.kr/observability/logging

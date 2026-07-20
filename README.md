# OLma Backend

OLma의 백엔드 API 서버 레포지토리입니다. 인증, 단가 제출, 벤치마크, 견적 계산, 커뮤니티, 사용자 프로필 API를 제공합니다.

상세한 아키텍처, 데이터 모델, API 정책, 배포/운영 문서는 `OLma-Docs`에서 관리합니다.

## 담당 범위

- 회원가입 및 로그인 API
- JWT 기반 인증 처리
- 단가 제출 및 조회 API
- 시장 단가 벤치마크 API
- 스마트 견적 계산 및 저장 API
- 커뮤니티 게시글/댓글 API
- 사용자 프로필 및 커리어 관리 API
- DB 마이그레이션
- Docker 기반 애플리케이션 배포
- 로깅 및 모니터링 연동

## 기술 스택

- Java 21
- Spring Boot 3.3
- Spring Data JPA
- PostgreSQL
- Flyway
- Gradle Kotlin DSL
- JJWT
- springdoc-openapi
- Docker
- Actuator, Prometheus, Loki, Grafana

## 로컬 실행

### 환경 변수

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=olma
export DB_USERNAME=olma
export DB_PASSWORD=olma
export JWT_SECRET=local-development-secret-key
```

### 애플리케이션 실행

```bash
./gradlew bootRun
```

기본 실행 주소는 `http://localhost:8080`입니다.

## 테스트

```bash
./gradlew test
```

## API 문서

로컬 실행 후 Swagger UI에서 API 스키마를 확인할 수 있습니다.

- Local Swagger: `http://localhost:8080/swagger-ui.html`
- Local OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 관련 레포

- Frontend: https://github.com/Olma-Web/OLma-FE
- Docs: https://github.com/Olma-Web/OLma-Docs

## 관련 문서

- 백엔드 아키텍처: https://olma-web.github.io/OLma-Docs/development/architecture-overview
- 요청 처리 흐름: https://olma-web.github.io/OLma-Docs/development/request-flow
- 데이터 모델: https://olma-web.github.io/OLma-Docs/development/data-model
- 도메인 지식 가이드: https://olma-web.github.io/OLma-Docs/development/domain-knowledge-guide
- API 공통 규격: https://olma-web.github.io/OLma-Docs/api/common
- 도메인별 API 요약: https://olma-web.github.io/OLma-Docs/api/domain-summary
- 배포 문서: https://olma-web.github.io/OLma-Docs/ops/deploy
- 런타임 설정: https://olma-web.github.io/OLma-Docs/ops/runtime-configuration
- 로깅/모니터링: https://olma-web.github.io/OLma-Docs/observability/logging

> 운영 도메인 종료 후에는 GitHub Pages 또는 `OLma-Docs` 레포지토리의 로컬 실행으로 문서를 확인할 수 있습니다.

# OLma Logging Strategy

이 문서는 OLma 백엔드의 로그 전략을 정리한 문서다. 목적은 단순히 로그를 많이 남기는 것이 아니라, 운영 중 문제가 생겼을 때 빠르게 원인을 추적할 수 있게 만드는 것이다.

## 1. 로그 전략의 목적

로그는 다음 질문에 답할 수 있어야 한다.

- 어떤 요청이 실패했는가?
- 어떤 사용자의 요청 흐름에서 문제가 발생했는가?
- 서버 내부 예외가 무엇이었는가?
- 핵심 비즈니스 이벤트가 정상적으로 처리되었는가?
- 배치 작업이 정상적으로 수행되었는가?
- 특정 시점에 에러나 지연이 증가했는가?

따라서 로그는 개발이 끝난 뒤 감으로 추가하는 것이 아니라, 운영에서 확인해야 할 질문을 기준으로 설계한다.

## 2. 현재 로그 아키텍처

현재 레포는 다음 구조를 기본으로 한다.

```text
Spring Boot application
  -> console stdout
  -> Docker container logs
  -> Promtail
  -> Loki
  -> Grafana
```

현재 구성 요소는 다음과 같다.

- Spring Boot 애플리케이션은 콘솔 로그를 출력한다.
- Docker 컨테이너의 stdout/stderr 로그를 Promtail이 수집한다.
- Promtail은 로그를 Loki로 전송한다.
- Grafana는 Loki를 datasource로 사용해 로그를 조회한다.
- Caddy access log는 `/var/log/caddy/access.log`에 JSON 형식으로 남기고 Promtail이 수집한다.

관련 파일:

- `src/main/resources/application.yaml`
- `src/main/resources/application-dev.yaml`
- `src/main/resources/application-prod.yaml`
- `monitoring/promtail/config.yaml`
- `monitoring/loki/config.yaml`
- `monitoring/grafana/provisioning/dashboards/olma-logs.json`
- `terraform/main.tf`

## 3. 현재 애플리케이션 로그 설정

기본 로그 레벨:

```yaml
logging:
  level:
    com.olma: INFO
```

개발 환경:

```yaml
logging:
  level:
    com.olma: DEBUG
```

운영 환경:

```yaml
logging:
  level:
    com.olma: INFO
    org.springframework: WARN
```

현재 콘솔 로그 패턴:

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %5p [%X{requestId:-no-req-id}] [%X{userId:-anonymous}] %logger{36} - %msg%n"
```

`RequestLoggingFilter`에서 모든 요청에 `requestId`를 생성하거나 클라이언트가 보낸 `X-Request-Id` 헤더 값을 사용해 MDC에 넣는다.

`JwtFilter`에서 인증된 요청의 `userId`를 MDC에 넣는다.

```java
MDC.put("requestId", requestId); // RequestLoggingFilter
MDC.put("userId", String.valueOf(userId)); // JwtFilter
```

MDC 정리는 `RequestLoggingFilter`의 `finally` 블록에서 `MDC.clear()`로 일괄 처리한다.

## 4. 개선 방향 요약

1차 개선 목표는 다음과 같다.

- 요청 단위 로그를 남긴다.
- 모든 요청에 `requestId`를 부여한다.
- 인증된 요청에는 `userId`를 함께 남긴다.
- 예외 로그를 표준화한다.
- 핵심 비즈니스 이벤트만 선별적으로 남긴다.
- 민감정보는 로그에 남기지 않는다.
- Loki label은 낮은 cardinality 값만 사용한다.

2차 개선 목표는 다음과 같다.

- plain text 로그를 JSON structured log로 전환한다.
- Promtail 또는 Loki에서 JSON 필드를 파싱한다.
- Grafana 대시보드와 알림을 보강한다.
- 필요하면 OpenTelemetry 기반 trace/log 연계를 검토한다.

## 5. 요청 로그 표준

모든 API 요청은 완료 시점에 한 줄의 요청 로그를 남긴다.

필수 필드:

- `requestId`
- `userId`
- `method`
- `path`
- `status`
- `durationMs`

예시:

```text
INFO request completed requestId=0f7c method=POST path=/v1/rate-submissions status=201 durationMs=42 userId=12
WARN request completed requestId=4a91 method=POST path=/v1/auth/login status=401 durationMs=18 userId=anonymous
ERROR request failed requestId=8de2 method=GET path=/v1/estimates status=500 durationMs=91 userId=12
```

요청 로그의 목적은 API 단위 장애 분석이다. 모든 service/repository 호출 전후에 로그를 남기지 않는다.

## 6. MDC 전략

MDC에는 요청 흐름 전체에서 반복적으로 필요한 값을 넣는다.

사용할 MDC 값:

- `requestId`
- `userId`

`requestId` 정책:

- 클라이언트가 `X-Request-Id` 헤더를 보내면 해당 값을 사용한다.
- 없으면 서버에서 UUID를 생성한다.
- 응답 헤더에도 `X-Request-Id`를 내려준다.

`userId` 정책:

- 인증 성공 시 JWT에서 추출한 `userId`를 MDC에 넣는다.
- 인증되지 않은 요청은 `anonymous`로 출력한다.
- 요청 종료 시 반드시 MDC를 정리한다.

필터 순서와 MDC 생명주기:

- `RequestLoggingFilter`(`@Order(1)`)가 바깥을 감싸고, `JwtFilter`(`@Order(2)`)가 안쪽에서 실행된다.
- `JwtFilter`는 `userId`를 MDC에 넣기만 하고 직접 정리하지 않는다.
- MDC 전체 정리(`MDC.clear()`)는 `RequestLoggingFilter`의 `finally` 블록에서 담당한다.
- 이 구조 덕분에 완료 로그를 찍는 시점에 `userId`가 MDC에 남아 있다.

```text
RequestLoggingFilter (바깥)
  MDC.put("requestId", ...)
  └─ JwtFilter (안쪽)
       MDC.put("userId", ...)
       └─ Controller/Service
  완료 로그 출력 (requestId + userId 모두 MDC에 있음)
  MDC.clear()
```

주의:

- 기존 패턴의 `'Z'`는 UTC를 의미하는 값이 아니라 문자 `Z`를 그대로 출력한다.
- 실제 timezone offset을 출력하려면 `XXX` 같은 offset 패턴을 사용하는 것이 더 명확하다.

## 7. 로그 레벨 기준

### TRACE

운영 기본 비활성. 매우 상세한 내부 흐름을 확인할 때만 사용한다.

### DEBUG

개발 환경 또는 장애 분석 시 임시로 사용한다.

예시:

- 견적 계산 중간값
- 특정 서비스 내부 분기
- SQL 디버깅

운영에서는 기본적으로 비활성화하고, 필요할 때 특정 패키지만 일시적으로 활성화한다.

### INFO

정상적인 주요 이벤트를 남긴다.

예시:

- 서버 시작
- 회원가입 성공
- 시급 제보 생성
- 견적 저장
- 프로필 수정
- 배치 시작/완료

INFO는 너무 많이 남기지 않는다. 단순 조회 성공, DTO 변환 성공, repository 호출 성공은 남기지 않는다.

### WARN

처리는 되었지만 주의가 필요한 상황을 남긴다.

예시:

- 로그인 실패
- 권한 없는 접근
- 잘못된 요청
- 외부 의존성 지연
- 재시도 발생

4xx 응답은 대부분 클라이언트 요청 문제이므로 stack trace 없이 WARN 수준으로 충분하다.

### ERROR

서버 내부 오류나 요청 처리 실패를 남긴다.

예시:

- 5xx 예외
- DB 오류
- 배치 실패
- 예상하지 못한 예외

ERROR 로그에는 stack trace를 포함한다.

## 8. 예외 로그 전략

`GlobalExceptionHandler`에서 예외 응답만 만드는 것으로 끝내지 않고, 서버 관점에서 의미 있는 오류를 로그로 남긴다.

기준:

- 400, 401, 403, 404: 필요 시 WARN, stack trace 생략
- 409: 중복/충돌 상황이므로 보통 WARN 또는 로그 생략
- 500: ERROR, stack trace 포함

예시:

```java
log.error("Unhandled exception occurred. path={}", req.getRequestURI(), ex);
```

주의:

- 클라이언트에게 반환하는 에러 메시지와 서버 로그 메시지는 다를 수 있다.
- 클라이언트 응답에는 내부 예외 상세를 노출하지 않는다.
- 서버 로그에는 장애 분석에 필요한 stack trace를 남긴다.

## 9. 비즈니스 이벤트 로그

모든 메서드에 로그를 넣지 않는다. 운영상 의미 있는 이벤트만 선별한다.

남길 이벤트:

- 회원가입 성공
- 로그인 실패
- 비밀번호 변경 성공
- 프로필 수정
- 시급 제보 생성
- 견적 저장
- 이상치 마킹 배치 시작/완료/실패

굳이 남기지 않을 이벤트:

- 단순 조회 성공
- DTO 변환 성공
- repository 호출 전후
- 계산 로직의 모든 중간값
- request body 전체
- response body 전체

예시:

```text
INFO rate submission created submissionId=45 userId=12
INFO estimate saved estimateId=91 userId=12
WARN login failed emailHash=... reason=INVALID_CREDENTIALS
INFO outlier marking completed rowsTouched=37
ERROR outlier marking failed
```

## 10. 민감정보 로그 금지 정책

다음 값은 로그에 남기지 않는다.

- 비밀번호
- JWT 원문
- Authorization header
- refresh token
- 주민등록번호
- 카드번호
- 계좌번호
- 이메일 원문
- 전화번호 원문
- request body 전체
- response body 전체

필요한 경우 마스킹 또는 해싱된 값만 남긴다.

예시:

```text
BAD  login failed email=user@example.com password=1234
GOOD login failed emailHash=ab12cd reason=INVALID_CREDENTIALS
```

## 11. Loki Label 전략

Loki에서는 label cardinality를 조심해야 한다. 값의 종류가 너무 많은 필드는 label로 만들지 않는다.

label로 사용해도 되는 값:

- `service`
- `env`
- `container`
- `job`
- `level`

label로 만들면 안 되는 값:

- `userId`
- `requestId`
- `traceId`
- `email`
- `path` 전체
- `orderId`
- `submissionId`

이유:

- `userId`, `requestId`처럼 값이 계속 늘어나는 필드를 label로 만들면 Loki index가 커지고 조회 성능과 저장 비용이 나빠진다.
- 이런 값은 로그 본문 필드로 남기고 필요할 때 검색한다.

## 12. JSON 로그 전환 계획

현재는 plain text 로그다. 2차 개선에서는 JSON structured log로 전환한다.

목표 예시:

```json
{
  "timestamp": "2026-06-27T12:34:56.789+09:00",
  "level": "INFO",
  "service": "olma-backend",
  "env": "prod",
  "logger": "com.olma.config.RequestLoggingFilter",
  "message": "request completed",
  "requestId": "0f7c",
  "userId": "12",
  "method": "POST",
  "path": "/v1/rate-submissions",
  "status": 201,
  "durationMs": 42
}
```

Spring Boot에서는 `logstash-logback-encoder` 같은 라이브러리를 사용해 JSON console log를 만들 수 있다.

초기에는 plain text + MDC로 시작하고, 로그 검색/집계 필요가 커지면 JSON으로 전환한다.

## 13. 구현 체크리스트

1차 구현:

- [x] `RequestLoggingFilter` 추가
- [x] 모든 요청에 `requestId` 생성 또는 전파
- [x] `X-Request-Id` 응답 헤더 추가
- [x] MDC에 `requestId` 추가
- [x] 기존 `JwtFilter`의 `userId` MDC와 충돌 없이 동작하도록 필터 순서 정리
- [x] 로그 패턴에 `requestId` 추가
- [x] 요청 완료 로그 추가
- [ ] 5xx 예외에 대해 `GlobalExceptionHandler`에서 ERROR 로그 추가
- [ ] 배치 작업 시작/완료/실패 로그 보강
- [ ] 민감정보 로그 금지 원칙을 코드 리뷰 기준에 추가

2차 구현:

- [ ] JSON console log 전환
- [ ] Promtail pipeline에서 JSON 파싱 검토
- [ ] Grafana 로그 대시보드 개선
- [ ] ERROR/WARN 로그 기반 알림 추가
- [ ] traceId/spanId 도입 검토

## 14. 면접 또는 설명용 요약

짧은 버전:

> 이 프로젝트는 Spring Boot 로그를 stdout으로 남기고, Docker 로그를 Promtail이 Loki로 수집한 뒤 Grafana에서 조회하는 구조로 가져갑니다. 애플리케이션에서는 requestId와 userId를 MDC에 넣어 요청 단위 추적이 가능하게 하고, INFO는 핵심 비즈니스 이벤트와 배치 결과, WARN은 이상 징후, ERROR는 5xx와 예외 중심으로 남기겠습니다. JWT, 비밀번호, 요청 바디 전체 같은 민감정보는 로그에서 제외하고, Loki label은 service/env/container처럼 cardinality가 낮은 값만 사용하겠습니다.

조금 긴 버전:

> 처음에는 개발이 끝난 뒤 중요도에 따라 로그를 추가하면 된다고 생각했지만, 로그는 운영 관측성 설계의 일부라고 판단했습니다. 그래서 이 프로젝트에서는 운영 중 답해야 할 질문을 기준으로 로그 정책을 잡으려고 합니다. 특정 요청이 왜 실패했는지, 어떤 사용자의 흐름에서 문제가 발생했는지, 배치가 정상적으로 돌았는지, 5xx나 WARN이 증가하는지 확인할 수 있어야 합니다. 현재는 Promtail-Loki-Grafana 수집 구조가 있고, requestId 기반 요청 로그와 userId MDC가 적용되어 있습니다. 이후 GlobalExceptionHandler 예외 로깅, 비즈니스 이벤트 로그를 보강하고, 필요하면 JSON structured log로 전환해 Grafana/Loki에서 더 쉽게 검색하고 집계할 수 있게 만들겠습니다.

## 15. 현재 레포에 대한 판단

현재 레포는 로그 수집 인프라의 뼈대는 이미 있다.

잘 되어 있는 점:

- Spring Boot console log 설정이 있다.
- `requestId`와 `userId`를 MDC에 넣는 흐름이 있다.
- 모든 요청에 완료 로그가 남는다.
- Promtail, Loki, Grafana 구성이 있다.
- Caddy access log를 JSON으로 남기고 수집한다.

부족한 점:

- 예외 로그가 부족하다.
- 앱 로그가 plain text라 구조화 검색이 약하다.
- 비즈니스 이벤트 로그 기준이 명확하지 않다.
- 민감정보 로그 금지 정책이 문서화되어 있지 않다.

따라서 당장 가장 실용적인 개선은 `GlobalExceptionHandler` 예외 로깅과 핵심 비즈니스 이벤트 로그 추가다. JSON 로그 전환과 OpenTelemetry 도입은 그 다음 단계로 둔다.

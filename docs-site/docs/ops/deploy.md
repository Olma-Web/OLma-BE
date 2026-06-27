---
sidebar_position: 1
title: 배포 아키텍처
description: GitHub Actions CI/CD, EC2/RDS 인프라, Docker 빌드 흐름
---

## 전체 배포 흐름

```
main 브랜치 push
  -> GitHub Actions: test (ubuntu-latest)
  -> GitHub Actions: build & push to GHCR (ubuntu-24.04-arm)
  -> GitHub Actions: deploy (self-hosted runner on EC2)
       docker pull ghcr.io/olma-web/olma-backend:latest
       docker stop olma-backend && docker rm olma-backend
       docker run -d ...
```

배포는 GitHub Actions의 `deploy` job이 EC2 위에 올라간 self-hosted runner를 통해 직접 처리한다. Watchtower 같은 자동 업데이트 도구는 사용하지 않는다.

---

## GitHub Actions 워크플로우

`.github/workflows/ci.yml`

### test job

- 실행 환경: `ubuntu-latest`
- PostgreSQL 17 서비스 컨테이너를 함께 기동한다.
- `./gradlew test` 실행.
- PR과 main 브랜치 push 모두에서 실행된다.

### build job

- 조건: `push` 이벤트이고 브랜치가 `main` 일 때만 실행 (`if: github.event_name == 'push' && github.ref == 'refs/heads/main'`).
- 실행 환경: `ubuntu-24.04-arm` — 이미지가 `linux/arm64` 아키텍처로 빌드된다.
- GHCR(`ghcr.io`)에 두 태그로 푸시된다.
  - `ghcr.io/olma-web/olma-backend:latest`
  - `ghcr.io/olma-web/olma-backend:<git-sha>`

### deploy job

- 실행 환경: `self-hosted` — EC2 인스턴스 위의 GitHub Actions runner.
- 순서:
  1. GHCR에서 `:latest` 이미지 pull.
  2. 기존 `olma-backend` 컨테이너 stop & rm.
  3. 새 컨테이너 기동. `--network monitoring` 으로 Promtail 등 모니터링 컨테이너와 같은 네트워크에 연결.

```yaml
# .github/workflows/ci.yml (lines 86–103)
deploy:
  needs: build
  runs-on: self-hosted
  steps:
    - name: Pull new image
      run: |
        echo "${{ secrets.GITHUB_TOKEN }}" | docker login ghcr.io -u ${{ github.actor }} --password-stdin
        docker pull ghcr.io/olma-web/olma-backend:latest
    - name: Restart olma-backend
      run: |
        docker network create monitoring 2>/dev/null || true
        docker stop olma-backend && docker rm olma-backend
        docker run -d \
          --name olma-backend \
          --env-file /home/ubuntu/olma.env \
          --network monitoring \
          -p 8080:8080 \
          --restart unless-stopped \
          ghcr.io/olma-web/olma-backend:latest
```

---

## Docker 이미지 빌드

`Dockerfile`

- 멀티 스테이지 빌드.
  - builder 스테이지: `eclipse-temurin:21-jdk`, `./gradlew bootJar` 실행.
  - 실행 스테이지: `eclipse-temurin:21-jre-alpine`, `spring` 전용 사용자로 실행.
- JVM 옵션: `-XX:+UseG1GC -XX:MaxRAMPercentage=75.0`
- 포트: `8080`

---

## EC2 인프라

`terraform/main.tf`

| 항목 | 값 |
|------|----|
| 인스턴스 타입 | `t4g.small` (ARM, CPU credits: standard) |
| AMI | Ubuntu 24.04 ARM64 (`ubuntu-noble-24.04-arm64`) |
| 스토리지 | 20GB gp3 |
| 퍼블릭 IP | Elastic IP (EIP) 고정 할당 |
| 리버스 프록시 | Caddy (포트 80, 443) |
| 백엔드 포트 | 8080 (보안 그룹에 직접 오픈되어 있음) |

Caddy는 EC2 user_data로 설치되며 `localhost:8080` 으로 리버스 프록시한다.

```
Caddyfile:
:80 {
  reverse_proxy localhost:8080
}
```

---

## RDS

| 항목 | 값 |
|------|----|
| 엔진 | PostgreSQL 17 |
| 인스턴스 클래스 | `var.db_instance_class` (variables.tf 참고) |
| 스토리지 | 20GB gp3 |
| 퍼블릭 접근 | 비활성 (`publicly_accessible = false`) |
| 보안 그룹 | `olma-backend-sg` 에서만 5432 포트 접근 허용 |

---

## 환경 변수

EC2 `/home/ubuntu/olma.env` 파일로 관리된다. Terraform user_data에서 초기 생성하며, CI/CD 배포 시 `--env-file /home/ubuntu/olma.env` 로 컨테이너에 전달된다.

| 변수 | 설명 |
|------|------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | RDS 연결 URL (sslmode=require 포함) |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자명 |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 |
| `JWT_SECRET` | JWT 서명 시크릿 |

---

## Watchtower 제거 이유

이전에는 Watchtower를 사용하여 새 이미지를 자동 감지하고 컨테이너를 재기동했다. 이 방식은 다음 이유로 제거되었다.

- EC2 인스턴스가 재생성되면 Terraform user_data가 초기 컨테이너를 기동하는 동시에 Watchtower가 별도로 컨테이너를 교체하는 타이밍 충돌이 발생했다.
- 배포 트리거와 결과를 GitHub Actions 워크플로우에서 단일하게 추적하기 어려웠다.

현재는 GitHub Actions의 `deploy` job (self-hosted runner)이 유일한 배포 실행 주체다. EC2 재생성 후 runner가 재등록되면 이후 배포부터 정상 동작한다.

---

## 보안 그룹 인바운드 규칙

`terraform/main.tf` — `aws_security_group.backend`

| 포트 | 프로토콜 | 설명 |
|------|----------|------|
| 22 | TCP | SSH |
| 80 | TCP | HTTP (Caddy) |
| 443 | TCP | HTTPS (Caddy) |
| 8080 | TCP | 백엔드 API 직접 접근 |

:::warning
포트 8080이 `0.0.0.0/0` 으로 열려 있다. 운영 환경에서 직접 노출이 필요한지 검토가 필요하다.
:::

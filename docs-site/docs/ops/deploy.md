---
sidebar_position: 1
title: 배포 아키텍처
description: GitHub Actions CI/CD, EC2/RDS 인프라, Docker 빌드 흐름, 문서 사이트 배포
---

## 전체 배포 흐름

```
main 브랜치 push
  -> GitHub Actions: test (ubuntu-latest)
  -> GitHub Actions: build & push to GHCR (ubuntu-24.04-arm)
  -> GitHub Actions: deploy (self-hosted runner on EC2)        -- 백엔드 컨테이너 교체
  -> GitHub Actions: deploy-docs (self-hosted runner on EC2)   -- 이 문서 사이트 자체 배포
```

배포는 GitHub Actions의 `deploy`/`deploy-docs` job이 EC2 위에 올라간 self-hosted runner를 통해 직접 처리한다. Watchtower 같은 자동 업데이트 도구는 사용하지 않는다.

### Self-hosted runner를 쓰는 이유

`test`/`build` job은 GitHub이 제공하는 호스티드 러너(`ubuntu-latest`, `ubuntu-24.04-arm`)를 쓰지만, 실제 배포(`deploy`, `deploy-docs`)는 EC2 내부에 설치된 self-hosted runner가 담당한다.

- 배포 스크립트가 EC2 "내부"에서 로컬로 실행되므로, GitHub 쪽 시크릿으로는 GitHub가 자동 발급하는 `secrets.GITHUB_TOKEN`(GHCR 로그인용)만 있으면 된다. SSH 개인키나 AWS 자격 증명을 GitHub Secrets에 등록해 외부에서 EC2로 밀어 넣는(push) 구조가 아니다.
- DB 비밀번호, JWT 시크릿 같은 런타임 민감 정보는 GitHub을 거치지 않고 EC2에 이미 있는 `/home/ubuntu/olma.env`(Terraform user_data가 생성)에서 컨테이너로 바로 주입된다.

:::info[대가 없는 이점은 아니다]
Self-hosted runner는 EC2 인스턴스 한 대에 강하게 결합되어 있다. 인스턴스가 재생성되면 runner 등록도 함께 사라지므로, 누군가 수동으로 재등록하기 전까지는 `deploy`/`deploy-docs` job이 실행될 러너 자체가 없어 배포가 조용히 진행되지 않는다. 자세한 배경은 아래 [Watchtower 제거 이유](#watchtower-제거-이유) 참고.
:::

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
- 이미지명은 `ghcr.io/$(조직명을 소문자로 변환)/olma-backend`로 워크플로우 실행 시 동적으로 계산된다(`github.repository_owner`를 소문자화).
- GHCR에 두 태그로 푸시된다: `:latest`, `:${{ github.sha }}`.

### deploy job (백엔드)

- 실행 환경: `self-hosted` — EC2 인스턴스 위의 GitHub Actions runner.
- 순서:
  1. GHCR에서 `:latest` 이미지 pull.
  2. 기존 `olma-backend` 컨테이너 stop & rm.
  3. 새 컨테이너 기동. `--network monitoring` 으로 Promtail 등 모니터링 컨테이너와 같은 네트워크에 연결.

```yaml
# .github/workflows/ci.yml
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

### deploy-docs job (문서 사이트 — 이 사이트 자체)

- 조건: `push` 이벤트이고 브랜치가 `main` 일 때만 실행.
- 실행 환경: `self-hosted` — 백엔드와 같은 EC2, 같은 runner.
- `docs-site/`에서 `npm ci` → `npm run build` 실행 후, 빌드 결과물을 `rsync --delete`로 EC2의 `/var/www/docs`에 동기화한다.
- Docker 컨테이너가 아니라 Caddy가 `/var/www/docs`를 정적 파일로 직접 서빙한다(아래 EC2 인프라 섹션 참고).

```yaml
# .github/workflows/ci.yml
deploy-docs:
  if: github.event_name == 'push' && github.ref == 'refs/heads/main'
  runs-on: self-hosted
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-node@v4
      with:
        node-version: 20
    - name: Install dependencies
      run: npm ci
      working-directory: docs-site
    - name: Build
      run: npm run build
      working-directory: docs-site
    - name: Deploy to /var/www/docs
      run: |
        sudo mkdir -p /var/www/docs
        sudo rsync -a --delete docs-site/build/ /var/www/docs/
```

---

## Docker 이미지 빌드

`Dockerfile`

- 멀티 스테이지 빌드.
  - builder 스테이지: `eclipse-temurin:21-jdk`, `./gradlew bootJar` 실행.
  - 실행 스테이지: `eclipse-temurin:21-jre-alpine`, `spring` 전용 사용자로 실행.
- JVM 옵션: `-XX:+UseG1GC -XX:MaxRAMPercentage=75.0`
- 포트: `8080`

문서 사이트(`deploy-docs`)는 Docker 이미지로 빌드되지 않는다 — Node.js로 정적 파일을 빌드해 EC2에 직접 동기화하는 별도 경로다.

---

## EC2 인프라

`terraform/main.tf`

| 항목 | 값 |
|------|----|
| 인스턴스 타입 | `t4g.small` (ARM, CPU credits: standard) |
| AMI | Ubuntu 24.04 ARM64 (`ubuntu-noble-24.04-arm64`) |
| 스토리지 | 20GB gp3 |
| 퍼블릭 IP | Elastic IP (EIP) 고정 할당 |
| 리버스 프록시 | Caddy |
| 백엔드 포트 | 8080 (보안 그룹에 직접 오픈되어 있음) |

Caddy는 EC2 user_data로 설치되며, 실제 `Caddyfile`엔 도메인이 있는 블록과 없는 블록이 함께 있다:

```
Caddyfile:
docs.olma.kro.kr {
  root * /var/www/docs
  file_server
}

:80 {
  reverse_proxy localhost:8080
}
```

:::warning[백엔드 API는 HTTPS가 안 된다]
`docs.olma.kro.kr`은 도메인이 지정된 블록이라 Caddy가 자동으로 TLS 인증서를 발급해 443도 서빙한다. 반면 백엔드용 `:80` 블록은 도메인 없는 catch-all이라 **80 포트 평문 HTTP만 리버스 프록시하며 443/TLS가 없다.** 백엔드에 HTTPS가 필요하면 도메인을 지정한 별도 블록이 추가로 필요하다.
:::

:::info[사용되지 않는 Terraform 변수]
`terraform/variables.tf`에 `domain`(기본값 `olma.lumie-infra.com`)이 선언돼 있지만 `main.tf` 어디에서도 참조되지 않는다. 실제 도메인(`docs.olma.kro.kr`)은 Caddyfile에 하드코딩되어 있어, 이 변수를 바꿔도 아무 효과가 없다.
:::

---

## RDS

| 항목 | 값 |
|------|----|
| 엔진 | PostgreSQL 17 |
| 인스턴스 클래스 | `var.db_instance_class` (기본값 `db.t4g.micro`, `variables.tf` 참고) |
| 스토리지 | 20GB gp3 |
| 퍼블릭 접근 | 비활성 (`publicly_accessible = false`) |
| 보안 그룹 | `olma-backend-sg` 에서만 5432 포트 접근 허용 |

---

## 환경 변수

백엔드 환경 변수는 EC2 `/home/ubuntu/olma.env` 파일로 관리된다. Terraform user_data에서 초기 생성하며, CI/CD 배포 시 `--env-file /home/ubuntu/olma.env` 로 컨테이너에 전달된다.

| 변수 | 설명 |
|------|------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | RDS 연결 URL (sslmode=require 포함) |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자명 |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 |
| `JWT_SECRET` | JWT 서명 시크릿 |

모니터링 환경 변수는 EC2 `/home/ubuntu/monitoring.env` 파일로 관리된다. Terraform user_data에서 초기 생성하며, 모니터링 배포 시 Grafana 컨테이너에 전달된다.

| 변수 | 설명 |
|------|------|
| `DISCORD_WEBHOOK_URL` | Grafana alert Discord webhook URL |

---

## Watchtower 제거 이유

이전에는 Watchtower를 사용하여 새 이미지를 자동 감지하고 컨테이너를 재기동했다. 이 방식은 다음 이유로 제거되었다.

- EC2 인스턴스가 재생성되면 Terraform user_data가 초기 컨테이너를 기동하는 동시에 Watchtower가 별도로 컨테이너를 교체하는 타이밍 충돌이 발생했다.
- 배포 트리거와 결과를 GitHub Actions 워크플로우에서 단일하게 추적하기 어려웠다.

현재는 GitHub Actions의 `deploy`/`deploy-docs` job (self-hosted runner)이 유일한 배포 실행 주체다. EC2 재생성 후 runner가 재등록되면 이후 배포부터 정상 동작한다.

---

## 보안 그룹 인바운드 규칙

`terraform/main.tf` — `aws_security_group.backend`

| 포트 | 프로토콜 | 설명 |
|------|----------|------|
| 22 | TCP | SSH |
| 80 | TCP | HTTP (Caddy — 문서 사이트는 HTTPS, 백엔드는 평문 HTTP) |
| 443 | TCP | HTTPS (Caddy — `docs.olma.kro.kr`에만 적용) |
| 8080 | TCP | 백엔드 API 직접 접근 |

:::warning
포트 8080이 `0.0.0.0/0` 으로 열려 있다. 운영 환경에서 직접 노출이 필요한지 검토가 필요하다.
:::

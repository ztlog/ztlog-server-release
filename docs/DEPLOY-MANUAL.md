# DEPLOY-MANUAL.md — 배포 매뉴얼

---

## 배포 구조 개요

```
GitHub Push
    │
    ├── develop 브랜치 → dev 환경 (포트: ADM 9089 / API 9088)
    └── main 브랜치    → prd 환경 (포트: ADM 8089 / API 8088)
```

CI/CD는 **GitHub Actions**로 자동화되어 있으며, 두 개의 워크플로우 파일로 분리됩니다.

| 워크플로우 | 파일 | 대상 모듈 |
|---|---|---|
| Admin 배포 | `.github/workflows/deploy-admin.yml` | `ztlog-admin` |
| API 배포 | `.github/workflows/deploy-api.yml` | `ztlog-api` |

---

## 트리거 조건

다음 경로에 변경이 있을 때만 워크플로우가 실행됩니다.

**Admin:**
- `ztlog-admin/**`
- `ztlog-core/**`
- `Dockerfile-admin`
- `docker-compose.yml`

**API:**
- `ztlog-api/**`
- `ztlog-core/**`
- `Dockerfile-api`
- `docker-compose.yml`

---

## 배포 단계

### 1단계: 빌드 및 DockerHub Push (GitHub Actions)

```
1. actions/checkout@v4 — 코드 체크아웃
2. JDK 17 (temurin) 세팅
3. ./gradlew clean build -x test — 테스트 제외 전체 빌드
4. DockerHub 로그인
5. docker build -f Dockerfile-{admin|api} → push
```

### 2단계: EC2 배포 (appleboy/ssh-action)

```
1. 프로젝트 디렉토리 없으면 git clone, 있으면 git pull
2. 로그 폴더 생성: /home/ec2-user/logs/{admin|api}
3. .env 파일 작성 (EC2 현지)
4. 기존 컨테이너 중지 및 제거 (docker stop / rm -f)
5. docker-compose pull → up -d --no-deps --force-recreate
6. docker image prune -f (미사용 이미지 정리)
```

---

## 환경 변수 (.env)

배포 시 EC2에 자동으로 작성되는 `.env` 파일 항목입니다.

| 변수 | dev | prd |
|---|---|---|
| `ENV` | `dev` | `prd` |
| `API_PORT` | `9088` | `8088` |
| `ADM_PORT` | `9089` | `8089` |
| `DB_NAME` | `ztlog_dev` | `ztlog_prd` |
| `DOCKERHUB_USERNAME` | Secret | Secret |
| `AWS_ACCESS_KEY` | Secret | Secret |
| `AWS_SECRET_KEY` | Secret | Secret |

---

## GitHub Secrets 목록

GitHub 저장소 Settings → Secrets and variables → Actions 에서 설정.

| Secret 키 | 설명 |
|---|---|
| `DOCKERHUB_USERNAME` | DockerHub 사용자명 |
| `DOCKERHUB_TOKEN` | DockerHub Access Token |
| `EC2_HOST` | EC2 퍼블릭 IP 또는 도메인 |
| `EC2_SSH_KEY` | EC2 접속용 PEM 키 (개인키) |
| `AWS_ACCESS_KEY` | S3 접근용 AWS Access Key |
| `AWS_SECRET_KEY` | S3 접근용 AWS Secret Key |

---

## Docker Compose 구성

```yaml
services:
  ztlog-admin:
    ports: ADM_PORT:ADM_PORT
    environment:
      SPRING_PROFILES_ACTIVE: ${ENV}    # dev / prd
      SERVER_PORT: ${ADM_PORT}
      AWS_ACCESS_KEY / AWS_SECRET_KEY
      JAVA_OPTS: -Xmx256M -Xms256M
    volumes:
      - ./logs/admin:/app/logs

  ztlog-api:
    ports: API_PORT:API_PORT
    environment:
      SPRING_PROFILES_ACTIVE: ${ENV}
      SERVER_PORT: ${API_PORT}
      JAVA_OPTS: -Xmx256M -Xms256M
    volumes:
      - ./logs/api:/app/logs
```

로그는 EC2의 `/home/ec2-user/apps/{repo}/logs/` 하위에 JSON 형식으로 저장됩니다 (최대 50MB × 5개 롤링).

---

## 수동 배포 방법 (긴급 시)

```bash
# EC2 접속
ssh -i <pem-key> ec2-user@<EC2_HOST>

cd /home/ec2-user/apps/ztlog-server-release

# .env 파일 직접 수정 후
docker-compose pull ztlog-admin
docker-compose up -d --no-deps --force-recreate ztlog-admin

# 로그 확인
docker logs -f ztlog-admin
```

---

## 스프링 프로파일

| 프로파일 | 설명 |
|---|---|
| `local` | 로컬 개발 환경 |
| `dev` | 개발 서버 (`develop` 브랜치 배포) |
| `prd` | 운영 서버 (`main` 브랜치 배포) |
| `common` | 모든 환경 공통 설정 |

프로파일 설정 위치: `ztlog-core/src/main/resources/application.yml`

---

*작성일: 2026-05-18*

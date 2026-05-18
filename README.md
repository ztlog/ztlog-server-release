
# ztlog-server-release

개발 블로그의 백엔드 시스템입니다. Spring Boot 기반의 **멀티 모듈 아키텍처**로 설계되었으며, 관리자 설정 및 블로그 콘텐츠 제공을 위한 REST API를 지원합니다.

![Spring](https://img.shields.io/badge/Spring_Boot-3.2.2-6DB33F?style=flat-square&logo=spring&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)

---

## 기술 스택 (Tech Stack)

| 분류 | 기술 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.2.2 |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA + QueryDSL, MyBatis (Hybrid) |
| Security | Spring Security, JWT (JJWT 0.11.5) |
| Build | Gradle (Multi-module) |
| Documentation | Swagger (OpenAPI 3.0) |

---

## 프로젝트 구조 (Project Structure)

```text
ztlog-server-release/
├── ztlog-core/           # 공통 모듈 (no boot JAR)
│   ├── entity/           # JPA 엔티티 (@MapsId, @EmbeddedId 구조)
│   ├── repository/       # QueryDSL 기반 데이터 접근 계층
│   └── common/           # 공통 유틸리티, 예외 계층, 응답 코드
├── ztlog-admin/          # 관리자 서비스 (Port: 8080, context: /admin)
│   ├── controller/       # 게시글 등록/수정/삭제 및 파일 관리
│   ├── mapper/           # MyBatis 기반 통계/대시보드 쿼리
│   └── config/security/  # JWT 인증 및 권한 제어
└── ztlog-api/            # 사용자 서비스 (Port: 8086, context: /front)
    ├── controller/       # 게시글 조회 및 태그 검색
    └── config/swagger/   # API 명세 자동화 설정
```

### 계층 구조

```
controller → service → repository (core) → entity (core)
```

---

## 데이터베이스 구조 (Database Design)

물리적 외래 키(FK) 제약을 제거하여 유연성을 확보하고, 정합성은 애플리케이션 계층(JPA)에서 관리합니다.

| 테이블 | 설명 |
| --- | --- |
| `user_mst` | 사용자 계정 및 권한 관리 |
| `cate_mst` | 카테고리 (계층형 자기참조) |
| `contents_mst` | 게시글 메타데이터 (제목, 부제목 등) |
| `contents_dtl` | 게시글 상세 본문 (`@MapsId`를 통한 마스터 PK 공유) |
| `tags_mst` | 태그 마스터 정보 |
| `contents_tags` | 게시글-태그 다대다(N:M) 매핑 테이블 |
| `file_mst` | 첨부 파일 이력 관리 |

> 테이블 컬럼 상세 및 관계도 → [docs/ERD-DIAGRAM.md](docs/ERD-DIAGRAM.md)

---

## 빌드 및 실행 (Build & Run)

### Gradle로 실행

```bash
# 전체 빌드
./gradlew build

# 모듈별 빌드
./gradlew :ztlog-api:build
./gradlew :ztlog-admin:build
./gradlew :ztlog-core:build

# 서버 실행
./gradlew :ztlog-admin:bootRun   # 관리자 서비스 (8080)
./gradlew :ztlog-api:bootRun     # 사용자 서비스 (8086)

# 테스트
./gradlew test
./gradlew :ztlog-admin:test
./gradlew :ztlog-api:test

# 클린 빌드
./gradlew clean build
```

### 🐳 Docker 자동 배포

**GitHub Actions를 통한 자동 배포**
- `develop` 브랜치 → dev 환경 (포트: ADM 9089 / API 9088)
- `main` 브랜치 → prd 환경 (포트: ADM 8089 / API 8088)
- 자동으로 Docker 이미지 빌드 및 EC2 배포

**프로파일**: `local`, `dev`, `prd` (공통 설정은 `common` 프로파일로 관리)

> 배포 절차 상세 → [docs/DEPLOY-MANUAL.md](docs/DEPLOY-MANUAL.md)

---

## API 문서 (API Documentation)

| 서비스 | URL |
| --- | --- |
| Admin Swagger UI | `http://localhost:8080/admin/swagger-ui/index.html` |
| Front Swagger UI | `http://localhost:8086/front/swagger-ui/index.html` |

> 전체 엔드포인트 목록 → [docs/API-LIST.md](docs/API-LIST.md)

---

## 보안 (Security)

- JWT 기반 인증 (JJWT 0.11.5)
- 공개 엔드포인트: `/api/v1/user/login`, `/api/v1/user/signup`, `/main/**`, Swagger 경로

---

## 회원 관리 (User Management)

### 계정 상태 (UserStatus)

| 상태 | 설명 |
| --- | --- |
| `PENDING` | 회원가입 후 관리자 승인 대기 상태 |
| `ACTIVE` | 승인 완료, 정상 로그인 가능 상태 |

### 회원 승인 플로우

```
회원가입 (/signup) → PENDING 상태로 생성
         ↓
관리자 승인 (PATCH /api/v1/user/{userNo}/approve)
         ↓
ACTIVE 상태로 전환 → 로그인 가능
```

미승인 계정(`PENDING`)으로 로그인 시도 시 `DisabledException`이 발생하며, 클라이언트에 승인 대기 안내 메시지가 반환됩니다.

### 슈퍼유저 자동 생성 (SuperUserInitializer)

서버 기동 시 `SuperUserInitializer`가 실행되어, 등록된 슈퍼유저 계정이 없으면 자동으로 생성합니다.

환경 변수로 계정 정보를 주입할 수 있습니다:

```yaml
# application.yml
admin:
  super-user:
    id: ${SUPER_USER_ID}
    password: ${SUPER_USER_PASSWORD}
```

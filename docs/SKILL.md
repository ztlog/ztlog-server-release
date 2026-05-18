# SKILL.md — 기술 스택 정리

> ztlog-server-release 프로젝트에 사용된 기술 스택과 선택 이유를 정리합니다.

---

## Language & Runtime

| 기술 | 버전 | 선택 이유 |
|---|---|---|
| Java | 17 | LTS 버전, Record/Sealed class 등 현대적 문법 지원 |

---

## Framework

| 기술 | 버전 | 선택 이유 |
|---|---|---|
| Spring Boot | 3.2.2 | 자동 설정, 내장 서버, Spring Security 통합 |
| Spring Security | (Boot 관리) | JWT 기반 인증·인가, `UserDetailsService` 커스터마이징 |
| Spring Data JPA | (Boot 관리) | ORM 추상화, 엔티티 생명주기 관리 |

---

## ORM / 데이터 접근

| 기술 | 버전 | 선택 이유 |
|---|---|---|
| Hibernate | (JPA 구현체) | JPA 표준 구현, 복합 키(`@EmbeddedId`, `@MapsId`) 지원 |
| QueryDSL | 5.0.0 | 타입 안전한 동적 쿼리, 복잡한 조건 조합에 유리 |
| MyBatis | 3.0.3 | 통계/대시보드처럼 SQL 직접 제어가 필요한 집계 쿼리에 사용 |

> **Hybrid 전략**: 도메인 CRUD는 JPA + QueryDSL, 집계 통계는 MyBatis XML 매퍼로 분리

---

## Database

| 기술 | 버전 | 선택 이유 |
|---|---|---|
| MySQL | 8.0 | 안정적인 RDBMS, JSON 타입 지원, UTF8MB4 한글 처리 |

- **물리적 FK 제거**: 배포 유연성을 위해 DB 레벨 FK 대신 애플리케이션 레벨에서 정합성 관리
- **Timezone**: `serverTimezone=Asia/Seoul`

---

## 보안 (Security)

| 기술 | 버전 | 선택 이유 |
|---|---|---|
| JJWT | 0.11.5 | JWT 생성·검증, 서명 알고리즘(HMAC-SHA512) 지원 |
| Pbkdf2PasswordEncoder | (Spring Security) | HMAC-SHA512, 16-byte salt, 310,000 iterations — NIST 권고 수준 |

- **토큰 전략**: Access Token 단기 발급, stateless 세션(`STATELESS` 정책)
- **계정 상태**: `UserStatus.PENDING` → 관리자 승인 → `UserStatus.ACTIVE` 순서로 활성화

---

## 빌드 & 인프라

| 기술 | 설명 |
|---|---|
| Gradle (Multi-module) | `ztlog-core` / `ztlog-admin` / `ztlog-api` 3개 모듈 관리 |
| GitHub Actions | `develop` → dev 환경, `main` → prd 환경 자동 배포 |
| Docker | 이미지 빌드 및 EC2 컨테이너 배포 |

---

## 외부 연동 (External Integration)

| 기술 | 버전 | 설명 |
|---|---|---|
| AWS SDK v2 (S3) | 2.20.26 | 파일 업로드/다운로드 — `file_mst` 연동 |
| Google API Client | 2.2.0 | Google Search Console API 호출 |
| Google Auth Library | 1.19.0 | OAuth2 인증 처리 |

---

## 문서화

| 기술 | 버전 | 설명 |
|---|---|---|
| springdoc-openapi | 2.3.0 | 컨트롤러 어노테이션 기반 API 명세 자동화 (OpenAPI 3.0) |

- Admin: `http://localhost:8080/admin/swagger-ui/index.html`
- Front: `http://localhost:8086/front/swagger-ui/index.html`

---

## 개발 편의

| 기술 | 버전 | 설명 |
|---|---|---|
| Lombok | (Boot 관리) | `@Builder`, `@Getter`, `@Slf4j` 등 보일러플레이트 제거 |
| Apache Commons Lang | 3.18.0 | 문자열·객체 유틸리티 |
| `BaseTimeEntity` | — | `@EntityListeners` 기반 `inpDttm` / `updDttm` 자동 감사(Audit) |

---

## 버전 업데이트 현황

| 기술 | 현재 버전 | 최신 버전 | 상태 | 비고 |
|---|---|---|---|---|
| Spring Boot | 3.2.2 | 3.4.x | 🟡 업그레이드 권장 | 3.2 EOL 예정, 3.3+ 마이그레이션 필요 |
| JJWT | 0.11.5 | 0.12.x | 🟡 업그레이드 권장 | 0.12.x API 변경으로 `TokenUtils` 수정 필요 |
| AWS SDK v2 | 2.20.26 | 2.26.x+ | 🟡 업그레이드 권장 | 버그 픽스 및 성능 개선 포함 |
| springdoc-openapi | 2.3.0 | 2.8.x | 🟡 업그레이드 권장 | Spring Boot 버전 업 시 함께 맞출 것 |
| QueryDSL | 5.0.0 | 5.0.0 | 🟢 최신 | 사실상 최신 안정 버전 |
| MyBatis SB Starter | 3.0.3 | 3.0.x | 🟢 최신 | 큰 변화 없음 |

> **우선순위**: Spring Boot 버전 업그레이드가 핵심. 3.3 이상으로 올릴 때 JJWT, springdoc-openapi 등 의존성을 함께 맞추는 것이 효율적.

---

*작성일: 2026-05-18*
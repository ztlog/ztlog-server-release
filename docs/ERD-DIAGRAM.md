# ERD-DIAGRAM.md — 데이터베이스 구조

> 물리적 FK 제약 없음. 참조 무결성은 애플리케이션(JPA) 레이어에서 관리.
> `BaseTimeEntity` 상속 테이블은 `INP_DTTM`, `UPD_DTTM` 컬럼을 공통으로 가짐.

---

## 테이블 목록

| 테이블 | 엔티티 클래스 | 설명 |
|---|---|---|
| `user_mst` | `User` | 관리자 계정 |
| `cate_mst` | `Category` | 카테고리 (계층형 자기참조) |
| `contents_mst` | `Content` | 게시글 메타데이터 |
| `contents_dtl` | `ContentDetail` | 게시글 본문 (`@MapsId`) |
| `tags_mst` | `Tag` | 태그 마스터 |
| `contents_tags` | `ContentTag` | 게시글-태그 N:M 매핑 |
| `file_mst` | `File` | 첨부 파일 이력 |

---

## 테이블 상세

### user_mst

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `USER_NO` | BIGINT | PK, AUTO_INCREMENT | 사용자 번호 |
| `USER_ID` | VARCHAR | | 로그인 아이디 |
| `USERNAME` | VARCHAR | | 표시명 |
| `PASSWORD` | VARCHAR | | Pbkdf2 암호화 비밀번호 |
| `GRANT` | VARCHAR | | 권한 (`ADMIN`) |
| `STATUS` | VARCHAR(20) | NOT NULL | 계정 상태 (`PENDING` / `ACTIVE`) |
| `INP_DTTM` | DATETIME | | 생성일시 |
| `UPD_DTTM` | DATETIME | | 수정일시 |

---

### cate_mst

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `CATE_NO` | BIGINT | PK, AUTO_INCREMENT | 카테고리 번호 |
| `CATE_NM` | VARCHAR | NOT NULL | 카테고리명 |
| `CATE_DEPTH` | INT | NOT NULL | 계층 깊이 (1: 최상위) |
| `DISP_ORD` | INT | NOT NULL | 표시 순서 |
| `USE_YN` | VARCHAR | | 사용 여부 (`Y` / `N`) |
| `UPPER_CATE_NO` | BIGINT | | 상위 카테고리 번호 (자기참조) |
| `INP_USER` | VARCHAR | | 등록자 |
| `INP_DTTM` | DATETIME | | 생성일시 |
| `UPD_DTTM` | DATETIME | | 수정일시 |

> 자기참조 계층 구조: `UPPER_CATE_NO` → `CATE_NO`

---

### contents_mst

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `CTNT_NO` | BIGINT | PK, AUTO_INCREMENT | 게시글 번호 |
| `CTNT_TITLE` | VARCHAR | NOT NULL | 제목 |
| `CTNT_SUBTITLE` | VARCHAR | NOT NULL | 부제목 |
| `CATE_NO` | BIGINT | | 카테고리 번호 (앱 레벨 참조) |
| `INP_USER` | VARCHAR | NOT NULL | 등록자 |
| `DELETED_YN` | CHAR(1) | | 소프트 삭제 (`Y` / `N`) |
| `INP_DTTM` | DATETIME | | 생성일시 |
| `UPD_DTTM` | DATETIME | | 수정일시 |

> `@SoftDelete(columnName = "DELETED_YN")` — 물리 삭제 없이 논리 삭제 처리

---

### contents_dtl

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `CTNT_NO` | BIGINT | PK (`@MapsId`) | `contents_mst.CTNT_NO` 공유 |
| `CTNT_TITLE` | VARCHAR | NOT NULL | 제목 (마스터와 동기화) |
| `CTNT_BODY` | TEXT | NOT NULL | 본문 내용 |
| `CTNT_PATH` | VARCHAR | | 썸네일 경로 |
| `CTNT_NAME` | VARCHAR | | 썸네일 파일명 |
| `CTNT_EXT` | VARCHAR | | 썸네일 확장자 |
| `INP_USER` | VARCHAR | NOT NULL | 등록자 |

> `@MapsId`: `contents_mst`와 PK를 공유. 1:1 관계이며 별도 JOIN 키 없음.

---

### tags_mst

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `TAG_NO` | BIGINT | PK, AUTO_INCREMENT | 태그 번호 |
| `TAG_NAME` | VARCHAR | NOT NULL | 태그명 |
| `INP_DTTM` | DATETIME | | 생성일시 |
| `UPD_DTTM` | DATETIME | | 수정일시 |

---

### contents_tags

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `CTNT_NO` | BIGINT | PK (복합) | 게시글 번호 |
| `TAG_NO` | BIGINT | PK (복합) | 태그 번호 |
| `SORT` | INT | NOT NULL | 태그 표시 순서 |

> `@IdClass(ContentTagPK)` 복합 PK. `@EqualsAndHashCode` 적용으로 컬렉션 비교 보장.

---

### file_mst

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `FILE_NO` | BIGINT | PK, AUTO_INCREMENT | 파일 번호 |
| `FILE_PATH` | VARCHAR | | S3 파일 URL |
| `FILE_NAME` | VARCHAR | | 파일명 |
| `FILE_EXT` | VARCHAR | | 확장자 |
| `CTNT_NO` | VARCHAR | | 연결된 게시글 번호 (앱 레벨 참조) |
| `INP_DTTM` | DATETIME | | 생성일시 |
| `UPD_DTTM` | DATETIME | | 수정일시 |

---

## 관계도 (텍스트 ERD)

```
user_mst
  └── (앱 레벨) contents_mst.INP_USER

cate_mst ──┐ (자기참조 계층)
           └── cate_mst.UPPER_CATE_NO

cate_mst ──── contents_mst (CATE_NO, 앱 레벨)

contents_mst (1) ──── (1) contents_dtl   [@MapsId, PK 공유]
contents_mst (1) ──── (N) contents_tags
tags_mst     (1) ──── (N) contents_tags

contents_mst ──── (앱 레벨) file_mst.CTNT_NO
```

---

*작성일: 2026-05-18*

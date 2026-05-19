# API-LIST.md — API 엔드포인트 목록

> **인증**: `🔒` 표시 엔드포인트는 JWT Bearer 토큰 필요  
> Admin base URL: `http://localhost:8080/admin`  
> API base URL: `http://localhost:8086/front`

---

## Admin 서비스 (`ztlog-admin`, port 8080)

### 유저 (`/api/v1/user`)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `GET` | `/api/v1/user/info` | 🔒 | 어드민 정보 조회 |
| `POST` | `/api/v1/user/signup` | - | 회원가입 (PENDING 상태로 생성) |
| `POST` | `/api/v1/user/login` | - | 로그인 → JWT 토큰 반환 |
| `POST` | `/api/v1/user/logout` | 🔒 | 로그아웃 |
| `PATCH` | `/api/v1/user/{userNo}/approve` | 🔒 ADMIN | 회원가입 승인 (PENDING → ACTIVE) |
| `DELETE` | `/api/v1/user/withdraw` | 🔒 | 회원탈퇴 |

---

### 컨텐츠 (`/api/v1/contents`)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `GET` | `/api/v1/contents` | 🔒 | 컨텐츠 목록 조회 (`?page=1`) |
| `GET` | `/api/v1/contents/{ctntNo}` | 🔒 | 컨텐츠 상세 조회 |
| `GET` | `/api/v1/contents/search` | 🔒 | 컨텐츠 검색 (`?type=&param=&page=1`) |
| `POST` | `/api/v1/contents` | 🔒 | 컨텐츠 등록 |
| `PUT` | `/api/v1/contents` | 🔒 | 컨텐츠 수정 |
| `DELETE` | `/api/v1/contents/{ctntNo}` | 🔒 | 컨텐츠 삭제 (소프트 삭제) |

**검색 타입 (`type`)**: `TITLE` / `TITLE_CONTENT` / `CONTENT` / `TAG`

---

### 카테고리 (`/api/v1/categories`)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `GET` | `/api/v1/categories` | 🔒 | 카테고리 목록 조회 |
| `GET` | `/api/v1/categories/{cateNo}` | 🔒 | 카테고리 상세 조회 |
| `POST` | `/api/v1/categories` | 🔒 | 카테고리 등록 |
| `PUT` | `/api/v1/categories` | 🔒 | 카테고리 수정 |
| `DELETE` | `/api/v1/categories/{cateNo}` | 🔒 | 카테고리 삭제 |

---

### 태그 (`/api/v1/tags`)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `GET` | `/api/v1/tags` | 🔒 | 태그 목록 조회 (`?page=1`) |
| `GET` | `/api/v1/tags/{tagNo}` | 🔒 | 태그 상세 조회 |
| `POST` | `/api/v1/tags` | 🔒 | 태그 등록 |
| `PUT` | `/api/v1/tags` | 🔒 | 태그 수정 |
| `DELETE` | `/api/v1/tags/{tagNo}` | 🔒 | 태그 삭제 |

---

### 파일 (`/api/v1/file`)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `POST` | `/api/v1/file/upload` | 🔒 | 이미지 파일 S3 업로드 (`multipart/form-data`) |

**파라미터**:
- `file` (required): 이미지 파일 (JPEG, PNG, GIF, WebP, 최대 10MB)
- `directory` (optional, default: `images/content`): S3 디렉토리 경로
- `ctntNo` (optional): 연결할 게시글 번호

---

### 대시보드 / 통계

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `GET` | `/api/v1/main` | 🔒 | 메인 대시보드 통계 조회 |
| `GET` | `/api/v1/stats/daily-growth` | 🔒 | 일별 성장 통계 (`?startDate=&endDate=`) |
| `GET` | `/api/v1/stats/comments/current` | 🔒 | 실시간 댓글 통계 현황 |
| `GET` | `/api/v1/stats/views/ranking` | 🔒 | 누적 조회수 랭킹 |
| `POST` | `/api/v1/stats/daily` | 🔒 | 일별 통계 수동 수집 실행 |
| `POST` | `/api/v1/stats/comments/sync` | 🔒 | 실시간 댓글 통계 업데이트 |
| `POST` | `/api/v1/stats/views/total` | 🔒 | 누적 조회수 합산 업데이트 |
| `POST` | `/api/v1/stats/views/raw` | 🔒 | 조회수 로우 데이터 추출 |

---

## API 서비스 (`ztlog-api`, port 8086)

### 유저 (`/api/v1/user`)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `GET` | `/api/v1/user/info` | - | 블로그 운영자 정보 조회 |

---

### 컨텐츠 (`/api/v1/contents`)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `GET` | `/api/v1/contents` | - | 컨텐츠 목록 조회 (`?page=1`) |
| `GET` | `/api/v1/contents/{ctntNo}` | - | 컨텐츠 상세 조회 |
| `GET` | `/api/v1/contents/search` | - | 컨텐츠 검색 (`?type=&param=&page=1`) |

---

### 카테고리 (`/api/v1/categories`)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `GET` | `/api/v1/categories` | - | 카테고리 목록 조회 |
| `GET` | `/api/v1/categories/{cateNo}` | - | 카테고리별 컨텐츠 목록 (`?page=1`) |

---

### 태그 (`/api/v1/tags`)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `GET` | `/api/v1/tags` | - | 태그 목록 조회 |
| `GET` | `/api/v1/tags/{tagNo}` | - | 태그별 컨텐츠 목록 (`?page=1`) |

---

## 공통 응답 형식

```json
{
  "code": "200",
  "message": "SUCCESS",
  "data": { ... }
}
```

## 에러 코드

| HTTP | 설명 |
|---|---|
| `200` | 성공 |
| `201` | 생성 성공 |
| `400` | 잘못된 요청 / 유효성 검사 실패 |
| `401` | 인증 실패 (JWT 없음 또는 만료) |
| `403` | 권한 없음 (인증은 됐으나 접근 불가) |
| `404` | 리소스 없음 |
| `405` | 지원하지 않는 HTTP 메서드 |
| `409` | 중복 데이터 (e.g. 이미 존재하는 아이디) |
| `413` | 파일 크기 초과 (최대 10MB) |
| `500` | 서버 내부 오류 |

---

*작성일: 2026-05-18*

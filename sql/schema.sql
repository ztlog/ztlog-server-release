-- =============================================================================
-- ZTLOG Database Schema
-- Database: MySQL 8.0
-- Charset: utf8mb4 / Collation: utf8mb4_unicode_ci
-- =============================================================================

CREATE DATABASE IF NOT EXISTS ztlog
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE ztlog;

-- =============================================================================
-- 1. user_mst (사용자)
-- =============================================================================
CREATE TABLE IF NOT EXISTS user_mst
(
    USER_NO   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '사용자 번호',
    USER_ID   VARCHAR(100) NOT NULL COMMENT '사용자 ID',
    USERNAME  VARCHAR(100) NOT NULL COMMENT '사용자명',
    PASSWORD  VARCHAR(300) NOT NULL COMMENT '암호화된 비밀번호',
    `GRANT`   VARCHAR(50)  NOT NULL COMMENT '권한 (ROLE_ADMIN 등)',
    INP_DTTM  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    UPD_DTTM  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (USER_NO),
    UNIQUE KEY uq_user_id (USER_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '사용자 마스터';

-- =============================================================================
-- 2. cate_mst (카테고리)
-- =============================================================================
CREATE TABLE IF NOT EXISTS cate_mst
(
    CATE_NO       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '카테고리 번호',
    CATE_NM       VARCHAR(100) NOT NULL COMMENT '카테고리명',
    CATE_DEPTH    INT          NOT NULL COMMENT '카테고리 깊이 (1: 최상위)',
    DISP_ORD      INT          NOT NULL DEFAULT 0 COMMENT '표시 순서',
    USE_YN        CHAR(1)      NOT NULL DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    INP_USER      VARCHAR(100) NULL COMMENT '등록자',
    UPPER_CATE_NO BIGINT       NULL COMMENT '상위 카테고리 번호 (self-ref)',
    INP_DTTM      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    UPD_DTTM      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (CATE_NO),
    KEY idx_upper_cate_no (UPPER_CATE_NO)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '카테고리 마스터';

-- =============================================================================
-- 3. tags_mst (태그)
-- =============================================================================
CREATE TABLE IF NOT EXISTS tags_mst
(
    TAG_NO   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '태그 번호',
    TAG_NAME VARCHAR(100) NOT NULL COMMENT '태그명',
    INP_DTTM DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    UPD_DTTM DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (TAG_NO),
    UNIQUE KEY uq_tag_name (TAG_NAME)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '태그 마스터';

-- =============================================================================
-- 4. contents_mst (게시물)
-- =============================================================================
CREATE TABLE IF NOT EXISTS contents_mst
(
    CTNT_NO      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '게시물 번호',
    CTNT_TITLE   VARCHAR(500)  NOT NULL COMMENT '게시물 제목',
    CTNT_SUBTITLE VARCHAR(500) NOT NULL COMMENT '게시물 부제목',
    CATE_NO      BIGINT        NULL COMMENT '카테고리 번호',
    INP_USER     VARCHAR(100)  NOT NULL COMMENT '등록자',
    DELETED_YN   CHAR(1)       NOT NULL DEFAULT 'N' COMMENT '삭제 여부 (Y/N, Hibernate SoftDelete)',
    INP_DTTM     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    UPD_DTTM     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (CTNT_NO),
    KEY idx_cate_no (CATE_NO),
    KEY idx_deleted_yn (DELETED_YN)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '게시물 마스터';

-- =============================================================================
-- 5. contents_dtl (게시물 상세)
-- =============================================================================
CREATE TABLE IF NOT EXISTS contents_dtl
(
    CTNT_NO    BIGINT       NOT NULL COMMENT '게시물 번호 (PK, FK → contents_mst)',
    CTNT_TITLE VARCHAR(500) NOT NULL COMMENT '게시물 제목',
    CTNT_BODY  LONGTEXT     NOT NULL COMMENT '게시물 본문',
    CTNT_PATH  VARCHAR(500) NULL COMMENT '파일 경로',
    CTNT_NAME  VARCHAR(300) NULL COMMENT '파일명',
    CTNT_EXT   VARCHAR(20)  NULL COMMENT '파일 확장자',
    INP_USER   VARCHAR(100) NOT NULL COMMENT '등록자',
    PRIMARY KEY (CTNT_NO)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '게시물 상세 (1:1 with contents_mst via @MapsId)';

-- =============================================================================
-- 6. contents_tags (게시물-태그 연결)
-- =============================================================================
CREATE TABLE IF NOT EXISTS contents_tags
(
    TAG_NO  BIGINT NOT NULL COMMENT '태그 번호 (FK → tags_mst)',
    CTNT_NO BIGINT NOT NULL COMMENT '게시물 번호 (FK → contents_mst)',
    SORT    INT    NOT NULL DEFAULT 0 COMMENT '태그 표시 순서',
    PRIMARY KEY (TAG_NO, CTNT_NO)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '게시물-태그 연결 (N:M)';

-- =============================================================================
-- 7. file_mst (파일)
-- =============================================================================
CREATE TABLE IF NOT EXISTS file_mst
(
    FILE_NO   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '파일 번호',
    FILE_PATH VARCHAR(500) NULL COMMENT 'S3 파일 URL',
    FILE_NAME VARCHAR(300) NULL COMMENT '파일명',
    FILE_EXT  VARCHAR(20)  NULL COMMENT '파일 확장자',
    CTNT_NO   VARCHAR(50)  NULL COMMENT '연결된 게시물 번호',
    INP_DTTM  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    UPD_DTTM  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (FILE_NO)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '파일 마스터 (S3 업로드 파일)';

-- =============================================================================
-- 8. contents_view_stats (게시물 누적 조회수)
-- =============================================================================
CREATE TABLE IF NOT EXISTS contents_view_stats
(
    CTNT_NO  BIGINT NOT NULL COMMENT '게시물 번호 (PK, FK → contents_mst)',
    VIEW_CNT BIGINT NOT NULL DEFAULT 0 COMMENT '누적 조회수',
    PRIMARY KEY (CTNT_NO)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '게시물 누적 조회수 통계';

-- =============================================================================
-- 9. contents_daily_stats (일별 조회수)
-- =============================================================================
CREATE TABLE IF NOT EXISTS contents_daily_stats
(
    STAT_DATE DATE   NOT NULL COMMENT '통계 날짜',
    CTNT_NO   BIGINT NOT NULL COMMENT '게시물 번호 (FK → contents_mst)',
    VIEW_CNT  INT    NOT NULL DEFAULT 0 COMMENT '해당 날짜 조회수',
    PRIMARY KEY (STAT_DATE, CTNT_NO)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '일별 게시물 조회수 통계 (Google Search Console 기반)';

-- =============================================================================
-- 10. contents_cmmt_stats (게시물 댓글수)
-- =============================================================================
CREATE TABLE IF NOT EXISTS contents_cmmt_stats
(
    CTNT_NO  BIGINT   NOT NULL COMMENT '게시물 번호 (PK, FK → contents_mst)',
    CMMT_CNT INT      NOT NULL DEFAULT 0 COMMENT '댓글 수 (Giscus 기반)',
    INP_DTTM DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    UPD_DTTM DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (CTNT_NO)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '게시물 댓글수 통계 (Giscus/GitHub Discussions 기반)';

-- =============================================================================
-- 11. contents_draft (게시물 초안)
-- =============================================================================
CREATE TABLE IF NOT EXISTS contents_draft
(
    DRAFT_NO      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '초안 번호',
    CTNT_NO       BIGINT       NULL COMMENT '연결된 발행글 번호 (신규 글 초안이면 NULL, FK 없음)',
    CTNT_TITLE    VARCHAR(500) NULL COMMENT '초안 제목',
    CTNT_SUBTITLE VARCHAR(500) NULL COMMENT '초안 부제목',
    CTNT_BODY     LONGTEXT     NULL COMMENT '초안 본문',
    CATE_NO       BIGINT       NULL COMMENT '카테고리 번호',
    CTNT_PATH     VARCHAR(500) NULL COMMENT '파일 경로',
    CTNT_NAME     VARCHAR(300) NULL COMMENT '파일명',
    CTNT_EXT      VARCHAR(20)  NULL COMMENT '파일 확장자',
    INP_USER      VARCHAR(100) NOT NULL COMMENT '등록자',
    INP_DTTM      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    UPD_DTTM      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (DRAFT_NO),
    KEY idx_ctnt_no (CTNT_NO)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '게시물 초안 (발행본과 분리 관리, 발행 완료 시 삭제)';

-- =============================================================================
-- 12. contents_draft_tags (초안-태그 연결)
-- =============================================================================
CREATE TABLE IF NOT EXISTS contents_draft_tags
(
    DRAFT_TAG_NO BIGINT       NOT NULL AUTO_INCREMENT COMMENT '초안-태그 연결 번호',
    DRAFT_NO     BIGINT       NOT NULL COMMENT '초안 번호 (FK → contents_draft)',
    TAG_NO       BIGINT       NULL COMMENT '태그 번호 (FK → tags_mst, 아직 생성 안 된 새 태그명이면 NULL)',
    TAG_NAME     VARCHAR(100) NOT NULL COMMENT '태그명 (TAG_NO 미확정 상태에서도 표시용으로 항상 저장)',
    SORT         INT          NOT NULL DEFAULT 0 COMMENT '태그 표시 순서',
    PRIMARY KEY (DRAFT_TAG_NO),
    KEY idx_draft_no (DRAFT_NO)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '초안-태그 연결 (N:M, 초안 저장 시점엔 새 태그를 tags_mst에 생성하지 않으므로 TAG_NO는 nullable)';

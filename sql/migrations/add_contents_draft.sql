-- =============================================================================
-- Migration: U3 게시물 초안(임시저장) 테이블 추가
-- Date: 2026-09-21
-- 대상: local(ztlog) / dev(ztlog_dev) / prd(ztlog_prd) — 실행 전 DB 선택(USE) 확인
-- 관련 문서: docs/U3-CONTENT-STATUS-DESIGN.md, docs/U3-IMPLEMENTATION-PLAN.md
--
-- 이 스크립트는 sql/schema.sql 11~12번 섹션과 동일한 DDL이다.
-- CREATE TABLE IF NOT EXISTS 사용 — 이미 적용된 환경에 재실행해도 안전(idempotent).
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
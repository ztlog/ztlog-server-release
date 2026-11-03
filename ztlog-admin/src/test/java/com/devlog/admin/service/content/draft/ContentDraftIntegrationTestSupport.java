package com.devlog.admin.service.content.draft;

import com.devlog.admin.service.content.ContentDraftService;
import com.devlog.admin.service.content.dto.request.ContentDraftReqDto;
import com.devlog.admin.service.content.dto.response.ContentDraftResDto;
import com.devlog.core.common.enumulation.ContentDraftType;
import com.devlog.core.common.utils.TokenUtils;
import com.devlog.core.entity.category.Category;
import com.devlog.core.entity.content.Content;
import com.devlog.core.entity.content.ContentTag;
import com.devlog.core.entity.tag.Tag;
import com.devlog.core.repository.category.CategoryRepository;
import com.devlog.core.repository.content.ContentDraftRepository;
import com.devlog.core.repository.content.ContentRepository;
import com.devlog.core.repository.content.ContentTagRepository;
import com.devlog.core.repository.tag.TagRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ContentDraftService 통합 테스트 공통 설정 (U3 · 게시물 초안/발행).
 * <p>
 * local DB(localhost:3306/ztlog)에 붙어서 실행하며, 각 테스트는 {@link Transactional}로 롤백되므로 데이터가 남지 않는다.
 * 실행 전 local DB에 {@code sql/migrations/add_contents_draft.sql}이 적용돼 있어야 한다.
 * <p>
 * 서비스 메서드 하나 = 실제 요청 하나이므로, 단계 사이에는 {@link #flushAndClear()}로 영속성 컨텍스트를 비워
 * 다음 단계가 DB에 저장된 상태를 다시 읽도록 한다. (MyBatis 매퍼 조회도 flush 이후에만 JPA 변경분을 본다)
 */
@SpringBootTest
@ActiveProfiles("local")
@Transactional
abstract class ContentDraftIntegrationTestSupport {

    protected static final String USER_ID = "draft-test-user";

    @Autowired
    protected ContentDraftService contentDraftService;

    @Autowired
    protected ContentDraftRepository contentDraftRepository;

    @Autowired
    protected ContentRepository contentRepository;

    @Autowired
    protected ContentTagRepository contentTagRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected TagRepository tagRepository;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    protected MockHttpServletRequest request;
    protected Category category;
    protected String uid;

    @BeforeEach
    void setUpDraftFixture() {
        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenUtils.generateToken(USER_ID).getAccessToken());

        // 기존 데이터와 겹치지 않도록 테스트마다 고유 접미사 사용
        uid = UUID.randomUUID().toString().substring(0, 8);
        category = categoryRepository.save(Category.created("draft-test-cate-" + uid, 1, 999, USER_ID, null));
        flushAndClear();
    }

    protected void flushAndClear() {
        em.flush();
        em.clear();
    }

    /** createDraft는 반환값이 없으므로, 테스트 전용 INP_USER로 저장된 가장 최근 초안 번호를 찾는다. */
    protected Long createDraft(ContentDraftReqDto reqDto) {
        contentDraftService.createDraft(request, reqDto);
        em.flush();
        return jdbcTemplate.queryForObject(
                "select max(DRAFT_NO) from contents_draft where INP_USER = ?", Long.class, USER_ID);
    }

    protected ContentDraftReqDto draftReq(Long draftNo, Long ctntNo, String title, String body, Long cateNo,
                                          List<Map<String, Object>> tags) {
        // 요청 DTO는 생성자가 private이고 setter가 없어서, 실제 요청처럼 JSON 바인딩 경로(ObjectMapper)로 만든다
        Map<String, Object> map = new HashMap<>();
        map.put("draftNo", draftNo);
        map.put("ctntNo", ctntNo);
        map.put("title", title);
        map.put("body", body);
        map.put("cateNo", cateNo);
        map.put("tags", tags);
        return objectMapper.convertValue(map, ContentDraftReqDto.class);
    }

    protected Map<String, Object> tagReq(Long tagNo, String tagName, Integer sort) {
        Map<String, Object> map = new HashMap<>();
        map.put("tagNo", tagNo);
        map.put("tagName", tagName);
        map.put("sort", sort);
        return map;
    }

    /** 이미 발행된 글 하나를 만든다 (태그는 주어진 순서대로 sort 1부터). */
    protected Content savePublishedContent(String title, List<Tag> tags) {
        Content content = contentRepository.save(Content.created(title, "origin-sub", "origin-body", category, USER_ID));
        List<ContentTag> contentTags = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            contentTags.add(ContentTag.created(tags.get(i), i + 1, content));
        }
        contentTagRepository.saveAll(contentTags);
        flushAndClear();
        return content;
    }

    protected Content findContentByTitle(String title) {
        Long ctntNo = jdbcTemplate.queryForObject(
                "select CTNT_NO from contents_mst where CTNT_TITLE = ? and DELETED_YN = 'N'", Long.class, title);
        return contentRepository.findById(ctntNo).orElseThrow();
    }

    protected void softDelete(Long ctntNo) {
        // ContentDetail이 cascade ALL이라 repository.delete()는 상세를 물리 삭제할 수 있어 컬럼만 직접 갱신
        jdbcTemplate.update("update contents_mst set DELETED_YN = 'Y' where CTNT_NO = ?", ctntNo);
    }

    protected List<Long> draftNos(ContentDraftType type) {
        // 테스트 데이터는 항상 최신 DRAFT_NO라 1페이지(내림차순)에 포함된다
        return contentDraftService.getDraftList(type, 1).getList().stream()
                .map(ContentDraftResDto::getDraftNo)
                .toList();
    }

    protected int countTagsByName(String tagName) {
        return jdbcTemplate.queryForObject("select count(*) from tags_mst where TAG_NAME = ?", Integer.class, tagName);
    }

    protected int countDraftTags(Long draftNo) {
        return jdbcTemplate.queryForObject("select count(*) from contents_draft_tags where DRAFT_NO = ?", Integer.class, draftNo);
    }

    protected int countDraftsByTitleSuffix(String suffix) {
        return jdbcTemplate.queryForObject(
                "select count(*) from contents_draft where INP_USER = ? and CTNT_TITLE like ?", Integer.class, USER_ID, "%" + suffix);
    }
}
package com.devlog.admin.service.content.draft;

import com.devlog.core.config.exception.DataNotFoundException;
import com.devlog.core.entity.content.ContentDraft;
import com.devlog.core.entity.tag.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/**
 * 구현 계획 4번 — 초안 생성 (임시저장 — 신규)
 */
@DisplayName("초안 생성")
class ContentDraftCreateTest extends ContentDraftIntegrationTestSupport {

    @Test
    @DisplayName("제목/본문/카테고리 없이도 임시저장된다")
    void createIncompleteDraft() {
        Long draftNo = createDraft(draftReq(null, null, "only-title-" + uid, null, null, null));
        flushAndClear();

        ContentDraft draft = contentDraftRepository.findById(draftNo).orElseThrow();
        assertThat(draft.getCtntNo()).isNull();
        assertThat(draft.getCtntBody()).isNull();
        assertThat(draft.getCategory()).isNull();
        assertThat(draft.getInpUser()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("새 태그명은 초안 저장 시 tags_mst에 생성되지 않고 이름만 저장된다")
    void newTagNameNotCreatedOnDraftSave() {
        String newTagName = "new-tag-" + uid;
        Long draftNo = createDraft(draftReq(null, null, "t-" + uid, null, null, List.of(tagReq(null, newTagName, 1))));
        flushAndClear();

        assertThat(countTagsByName(newTagName)).isZero();
        assertThat(contentDraftRepository.findById(draftNo).orElseThrow().getDraftTags())
                .extracting(t -> t.getTags(), t -> t.getTagName(), t -> t.getSort())
                .containsExactly(tuple(null, newTagName, 1));
    }

    @Test
    @DisplayName("tagNo가 있는 기존 태그는 Tag 엔티티와 연결된다")
    void existingTagLinked() {
        Tag tag = tagRepository.save(Tag.created("exist-tag-" + uid));
        Long draftNo = createDraft(draftReq(null, null, "t-" + uid, null, null,
                List.of(tagReq(tag.getTagNo(), tag.getTagName(), 1))));
        flushAndClear();

        assertThat(contentDraftRepository.findById(draftNo).orElseThrow().getDraftTags())
                .extracting(t -> t.getTags().getTagNo())
                .containsExactly(tag.getTagNo());
    }

    @Test
    @DisplayName("완전히 같은 태그명이 중복으로 오면 첫 항목만 저장된다")
    void duplicateTagNamesDeduplicated() {
        String name = "dup-" + uid;
        Long draftNo = createDraft(draftReq(null, null, "t-" + uid, null, null,
                List.of(tagReq(null, name, 1), tagReq(null, name, 2))));
        flushAndClear();

        assertThat(contentDraftRepository.findById(draftNo).orElseThrow().getDraftTags())
                .extracting(t -> t.getTagName(), t -> t.getSort())
                .containsExactly(tuple(name, 1));
    }

    @Test
    @DisplayName("없는 ctntNo로 수정 초안을 만들면 DataNotFoundException")
    void createWithUnknownCtntNo() {
        assertThatThrownBy(() -> contentDraftService.createDraft(request,
                draftReq(null, Long.MAX_VALUE, "t-" + uid, null, null, null)))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @DisplayName("없는 cateNo로 만들면 DataNotFoundException")
    void createWithUnknownCateNo() {
        assertThatThrownBy(() -> contentDraftService.createDraft(request,
                draftReq(null, null, "t-" + uid, null, Long.MAX_VALUE, null)))
                .isInstanceOf(DataNotFoundException.class);
    }
}
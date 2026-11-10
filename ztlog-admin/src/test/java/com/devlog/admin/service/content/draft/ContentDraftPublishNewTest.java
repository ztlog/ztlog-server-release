package com.devlog.admin.service.content.draft;

import com.devlog.core.config.exception.DataNotFoundException;
import com.devlog.core.config.exception.ValidationException;
import com.devlog.core.entity.content.Content;
import com.devlog.core.entity.content.ContentTag;
import com.devlog.core.entity.tag.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/**
 * 구현 계획 6번 — 발행 (신규 글, ctntNo == null)
 */
@DisplayName("발행 - 신규 글")
class ContentDraftPublishNewTest extends ContentDraftIntegrationTestSupport {

    @Test
    @DisplayName("신규 초안을 발행하면 Content가 생성되고 초안은 삭제된다")
    void publishCreatesContentAndRemovesDraft() {
        String title = "pub-new-" + uid;
        Long draftNo = createDraft(draftReq(null, null, title, "pub-body", category.getCateNo(), List.of(tagReq(null, "a-" + uid, 1))));
        flushAndClear();
        long contentCountBefore = contentRepository.count();

        contentDraftService.publish(request, draftNo);
        flushAndClear();

        assertThat(contentRepository.count()).isEqualTo(contentCountBefore + 1);
        Content published = findContentByTitle(title);
        assertThat(published.getContentDetail().getCtntBody()).isEqualTo("pub-body");
        assertThat(published.getCategory().getCateNo()).isEqualTo(category.getCateNo());
        assertThat(published.getInpUser()).isEqualTo(USER_ID);

        assertThat(contentDraftRepository.findById(draftNo)).isEmpty();
        assertThat(countDraftTags(draftNo)).isZero();
    }

    @Test
    @DisplayName("초안 저장 때 쓴 새 태그명은 발행 시점에 tags_mst에 정확히 하나만 생성된다")
    void publishCreatesNewTagOnce() {
        String newTagName = "pub-tag-" + uid;
        String title = "pub-new-" + uid;
        Long draftNo = createDraft(draftReq(null, null, title, "body", category.getCateNo(), List.of(tagReq(null, newTagName, 1))));
        flushAndClear();
        assertThat(countTagsByName(newTagName)).isZero();

        contentDraftService.publish(request, draftNo);
        flushAndClear();

        assertThat(countTagsByName(newTagName)).isEqualTo(1);
        assertThat(findContentByTitle(title).getContentTags())
                .extracting(ct -> ct.getTags().getTagName(), ContentTag::getSort)
                .containsExactly(tuple(newTagName, 1));
    }

    @Test
    @DisplayName("새 태그명이 그사이 tags_mst에 생겼으면 새로 만들지 않고 기존 태그를 재사용한다")
    void publishReusesExistingTagByName() {
        String tagName = "reuse-" + uid;
        String title = "pub-new-" + uid;
        Long draftNo = createDraft(draftReq(null, null, title, "body", category.getCateNo(), List.of(tagReq(null, tagName, 1))));
        Tag createdMeanwhile = tagRepository.save(Tag.created(tagName));
        flushAndClear();

        contentDraftService.publish(request, draftNo);
        flushAndClear();

        assertThat(countTagsByName(tagName)).isEqualTo(1);
        assertThat(findContentByTitle(title).getContentTags())
                .extracting(ct -> ct.getTags().getTagNo())
                .containsExactly(createdMeanwhile.getTagNo());
    }

    @Test
    @DisplayName("대소문자만 다른 새 태그명은 하나의 태그로 합쳐져 매핑도 하나만 생긴다")
    void publishDeduplicatesCaseInsensitiveTags() {
        String title = "pub-new-" + uid;
        Long draftNo = createDraft(draftReq(null, null, title, "body", category.getCateNo(),
                List.of(tagReq(null, "CaseTag-" + uid, 1), tagReq(null, "casetag-" + uid, 2))));
        flushAndClear();

        contentDraftService.publish(request, draftNo);
        flushAndClear();

        assertThat(countTagsByName("casetag-" + uid)).isEqualTo(1);
        assertThat(findContentByTitle(title).getContentTags()).hasSize(1);
    }

    @Test
    @DisplayName("부제목 없는 초안은 빈 문자열 부제목으로 발행된다")
    void publishWithNullSubTitle() {
        String title = "pub-new-" + uid;
        Long draftNo = createDraft(draftReq(null, null, title, "body", category.getCateNo(), null));
        flushAndClear();

        contentDraftService.publish(request, draftNo);
        flushAndClear();

        assertThat(findContentByTitle(title).getCtntSubTitle()).isEmpty();
    }

    @Test
    @DisplayName("제목이 없으면 ValidationException이고 초안은 남는다")
    void publishWithoutTitle() {
        Long draftNo = createDraft(draftReq(null, null, null, "body", category.getCateNo(), null));
        flushAndClear();

        assertThatThrownBy(() -> contentDraftService.publish(request, draftNo))
                .isInstanceOf(ValidationException.class);
        flushAndClear();
        assertThat(contentDraftRepository.findById(draftNo)).isPresent();
    }

    @Test
    @DisplayName("본문이 없으면 ValidationException")
    void publishWithoutBody() {
        Long draftNo = createDraft(draftReq(null, null, "t-" + uid, " ", category.getCateNo(), null));
        flushAndClear();

        assertThatThrownBy(() -> contentDraftService.publish(request, draftNo))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("카테고리가 없으면 ValidationException")
    void publishWithoutCategory() {
        Long draftNo = createDraft(draftReq(null, null, "t-" + uid, "body", null, null));
        flushAndClear();

        assertThatThrownBy(() -> contentDraftService.publish(request, draftNo))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("없는 초안 발행은 DataNotFoundException")
    void publishUnknownDraft() {
        assertThatThrownBy(() -> contentDraftService.publish(request, Long.MAX_VALUE))
                .isInstanceOf(DataNotFoundException.class);
    }
}
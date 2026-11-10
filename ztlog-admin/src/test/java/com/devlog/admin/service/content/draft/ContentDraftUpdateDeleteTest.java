package com.devlog.admin.service.content.draft;

import com.devlog.core.common.enumulation.ContentDraftType;
import com.devlog.core.config.exception.DataNotFoundException;
import com.devlog.core.config.exception.ValidationException;
import com.devlog.core.entity.content.ContentDraft;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/**
 * 구현 계획 5번 — 초안 수정 (임시저장 — 갱신) / 삭제
 */
@DisplayName("초안 수정/삭제")
class ContentDraftUpdateDeleteTest extends ContentDraftIntegrationTestSupport {

    @Test
    @DisplayName("여러 번 수정 저장해도 draftNo는 유지되고 마지막 내용으로 덮어써진다")
    void updateKeepsDraftNo() {
        Long draftNo = createDraft(draftReq(null, null, "v1-" + uid, null, null, null));
        flushAndClear();

        contentDraftService.updateDraft(request, draftReq(draftNo, null, "v2-" + uid, "body2", null, null));
        flushAndClear();
        contentDraftService.updateDraft(request, draftReq(draftNo, null, "v3-" + uid, "body3", category.getCateNo(), null));
        flushAndClear();

        ContentDraft draft = contentDraftRepository.findById(draftNo).orElseThrow();
        assertThat(draft.getCtntTitle()).isEqualTo("v3-" + uid);
        assertThat(draft.getCtntBody()).isEqualTo("body3");
        assertThat(draft.getCategory().getCateNo()).isEqualTo(category.getCateNo());
        assertThat(countDraftsByTitleSuffix(uid)).isEqualTo(1);
    }

    @Test
    @DisplayName("수정 저장 시 태그는 요청값으로 통째로 교체된다")
    void updateReplacesTags() {
        Long draftNo = createDraft(draftReq(null, null, "t-" + uid, null, null,
                List.of(tagReq(null, "a-" + uid, 1), tagReq(null, "b-" + uid, 2))));
        flushAndClear();

        contentDraftService.updateDraft(request, draftReq(draftNo, null, "t-" + uid, null, null,
                List.of(tagReq(null, "b-" + uid, 1), tagReq(null, "c-" + uid, 2))));
        flushAndClear();

        assertThat(contentDraftRepository.findById(draftNo).orElseThrow().getDraftTags())
                .extracting(t -> t.getTagName(), t -> t.getSort())
                .containsExactly(tuple("b-" + uid, 1), tuple("c-" + uid, 2));
        assertThat(countDraftTags(draftNo)).isEqualTo(2);
    }

    @Test
    @DisplayName("수정 저장 시 태그를 비워 보내면 초안 태그가 모두 지워진다")
    void updateClearsTags() {
        Long draftNo = createDraft(draftReq(null, null, "t-" + uid, null, null, List.of(tagReq(null, "a-" + uid, 1))));
        flushAndClear();

        contentDraftService.updateDraft(request, draftReq(draftNo, null, "t-" + uid, null, null, List.of()));
        flushAndClear();

        assertThat(countDraftTags(draftNo)).isZero();
    }

    @Test
    @DisplayName("draftNo 없이 수정하면 ValidationException")
    void updateWithoutDraftNo() {
        assertThatThrownBy(() -> contentDraftService.updateDraft(request, draftReq(null, null, "t", null, null, null)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("없는 draftNo로 수정하면 DataNotFoundException")
    void updateUnknownDraft() {
        assertThatThrownBy(() -> contentDraftService.updateDraft(request, draftReq(Long.MAX_VALUE, null, "t", null, null, null)))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @DisplayName("삭제하면 초안과 초안 태그가 함께 지워지고 목록에서 사라진다")
    void deleteRemovesDraftAndTags() {
        Long draftNo = createDraft(draftReq(null, null, "t-" + uid, null, null, List.of(tagReq(null, "a-" + uid, 1))));
        flushAndClear();

        contentDraftService.deleteDraft(draftNo);
        flushAndClear();

        assertThat(contentDraftRepository.findById(draftNo)).isEmpty();
        assertThat(countDraftTags(draftNo)).isZero();
        assertThat(draftNos(ContentDraftType.NEW)).doesNotContain(draftNo);
    }

    @Test
    @DisplayName("없는 초안 삭제는 DataNotFoundException")
    void deleteUnknownDraft() {
        assertThatThrownBy(() -> contentDraftService.deleteDraft(Long.MAX_VALUE))
                .isInstanceOf(DataNotFoundException.class);
    }
}
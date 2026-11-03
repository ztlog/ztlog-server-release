package com.devlog.admin.service.content.draft;

import com.devlog.core.config.exception.DataNotFoundException;
import com.devlog.core.entity.category.Category;
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
 * 구현 계획 7번 — 발행 (기존 글 수정, ctntNo != null)
 */
@DisplayName("발행 - 기존 글 수정")
class ContentDraftPublishEditTest extends ContentDraftIntegrationTestSupport {

    @Test
    @DisplayName("발행 전까지는 수정 초안을 저장/갱신해도 원본 발행글은 그대로다")
    void originUntouchedBeforePublish() {
        Tag keep = tagRepository.save(Tag.created("keep-" + uid));
        Content origin = savePublishedContent("origin-" + uid, List.of(keep));
        Long ctntNo = origin.getCtntNo();

        Long draftNo = createDraft(draftReq(null, ctntNo, "changed-" + uid, "changed-body", category.getCateNo(),
                List.of(tagReq(null, "other-" + uid, 1))));
        flushAndClear();
        contentDraftService.updateDraft(request, draftReq(draftNo, ctntNo, "changed2-" + uid, "changed-body2",
                category.getCateNo(), List.of()));
        flushAndClear();

        Content reloaded = contentRepository.findById(ctntNo).orElseThrow();
        assertThat(reloaded.getCtntTitle()).isEqualTo("origin-" + uid);
        assertThat(reloaded.getContentDetail().getCtntBody()).isEqualTo("origin-body");
        assertThat(reloaded.getContentTags()).extracting(ct -> ct.getTags().getTagNo())
                .containsExactly(keep.getTagNo());
    }

    @Test
    @DisplayName("수정 초안을 발행하면 원본 Content가 갱신되고(새 row 아님) 초안은 삭제된다")
    void publishUpdatesOriginInPlace() {
        Content origin = savePublishedContent("origin-" + uid, List.of());
        Long ctntNo = origin.getCtntNo();
        Category newCategory = categoryRepository.save(Category.created("draft-test-cate2-" + uid, 1, 999, USER_ID, null));
        Long draftNo = createDraft(draftReq(null, ctntNo, "edited-" + uid, "edited-body", newCategory.getCateNo(), null));
        flushAndClear();
        long contentCountBefore = contentRepository.count();

        contentDraftService.publish(request, draftNo);
        flushAndClear();

        assertThat(contentRepository.count()).isEqualTo(contentCountBefore);
        Content reloaded = contentRepository.findById(ctntNo).orElseThrow();
        assertThat(reloaded.getCtntTitle()).isEqualTo("edited-" + uid);
        assertThat(reloaded.getCtntSubTitle()).isEmpty();
        assertThat(reloaded.getContentDetail().getCtntTitle()).isEqualTo("edited-" + uid);
        assertThat(reloaded.getContentDetail().getCtntBody()).isEqualTo("edited-body");
        assertThat(reloaded.getCategory().getCateNo()).isEqualTo(newCategory.getCateNo());
        assertThat(reloaded.getInpUser()).isEqualTo(USER_ID);

        assertThat(contentDraftRepository.findById(draftNo)).isEmpty();
        assertThat(countDraftTags(draftNo)).isZero();
    }

    @Test
    @DisplayName("태그는 유지분 정렬 갱신 · 신규 추가 · 누락분 삭제로 반영된다")
    void publishAppliesTagDiff() {
        Tag keep = tagRepository.save(Tag.created("keep-" + uid));
        Tag drop = tagRepository.save(Tag.created("drop-" + uid));
        Content origin = savePublishedContent("origin-" + uid, List.of(keep, drop));
        Long ctntNo = origin.getCtntNo();
        String addedName = "added-" + uid;

        // keep는 정렬을 1 -> 2로 변경, drop은 제외, added는 새 태그명
        Long draftNo = createDraft(draftReq(null, ctntNo, "edited-" + uid, "body", category.getCateNo(),
                List.of(tagReq(null, addedName, 1), tagReq(keep.getTagNo(), keep.getTagName(), 2))));
        flushAndClear();

        contentDraftService.publish(request, draftNo);
        flushAndClear();

        assertThat(contentRepository.findById(ctntNo).orElseThrow().getContentTags())
                .extracting(ct -> ct.getTags().getTagName(), ContentTag::getSort)
                .containsExactly(tuple(addedName, 1), tuple(keep.getTagName(), 2));
        assertThat(countTagsByName(addedName)).isEqualTo(1);
        // 매핑만 지워지고 태그 마스터는 남는다
        assertThat(countTagsByName(drop.getTagName())).isEqualTo(1);
    }

    @Test
    @DisplayName("원본이 소프트 삭제됐으면 DataNotFoundException이고 초안은 남는다")
    void publishWhenOriginSoftDeleted() {
        Content origin = savePublishedContent("origin-" + uid, List.of());
        Long draftNo = createDraft(draftReq(null, origin.getCtntNo(), "edited-" + uid, "body", category.getCateNo(), null));
        flushAndClear();
        softDelete(origin.getCtntNo());

        assertThatThrownBy(() -> contentDraftService.publish(request, draftNo))
                .isInstanceOf(DataNotFoundException.class);
        flushAndClear();
        assertThat(contentDraftRepository.findById(draftNo)).isPresent();
    }
}
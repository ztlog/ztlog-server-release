package com.devlog.admin.service.content.draft;

import com.devlog.admin.service.content.dto.response.ContentDraftListResDto;
import com.devlog.admin.service.content.dto.response.ContentDraftResDto;
import com.devlog.admin.service.tag.dto.request.TagInfoReqDto;
import com.devlog.core.common.enumulation.ContentDraftType;
import com.devlog.core.config.exception.DataNotFoundException;
import com.devlog.core.entity.content.Content;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 구현 계획 3번 — 초안 목록 / 상세 조회
 */
@DisplayName("초안 목록/상세 조회")
class ContentDraftReadTest extends ContentDraftIntegrationTestSupport {

    @Test
    @DisplayName("type=NEW는 ctntNo 없는 초안만, type=EDIT는 ctntNo 있는 초안만 조회된다")
    void listFiltersByType() {
        Content origin = savePublishedContent("origin-" + uid, List.of());
        Long newDraftNo = createDraft(draftReq(null, null, "new-" + uid, "body", category.getCateNo(), List.of()));
        Long editDraftNo = createDraft(draftReq(null, origin.getCtntNo(), "edit-" + uid, "body", category.getCateNo(), List.of()));
        flushAndClear();

        List<Long> newList = draftNos(ContentDraftType.NEW);
        List<Long> editList = draftNos(ContentDraftType.EDIT);

        assertThat(newList).contains(newDraftNo).doesNotContain(editDraftNo);
        assertThat(editList).contains(editDraftNo).doesNotContain(newDraftNo);
    }

    @Test
    @DisplayName("목록 totalCount는 type별 실제 초안 수와 같다")
    void listTotalCountMatchesType() {
        Content origin = savePublishedContent("origin-" + uid, List.of());
        createDraft(draftReq(null, null, "new1-" + uid, null, null, List.of()));
        createDraft(draftReq(null, null, "new2-" + uid, null, null, List.of()));
        createDraft(draftReq(null, origin.getCtntNo(), "edit-" + uid, null, null, List.of()));
        flushAndClear();

        // 같은 트랜잭션에서 같은 파라미터로 매퍼를 두 번 부르면 MyBatis 로컬 캐시가 이전 결과를 돌려주므로,
        // 기준값은 JDBC로 직접 세고 서비스는 type별로 한 번씩만 호출한다
        ContentDraftListResDto newResult = contentDraftService.getDraftList(ContentDraftType.NEW, 1);
        ContentDraftListResDto editResult = contentDraftService.getDraftList(ContentDraftType.EDIT, 1);

        assertThat(newResult.getTotalCount()).isEqualTo(countDraftsByType(ContentDraftType.NEW)).isGreaterThanOrEqualTo(2);
        assertThat(editResult.getTotalCount()).isEqualTo(countDraftsByType(ContentDraftType.EDIT)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("태그가 여러 개인 초안도 목록에서 한 건으로 묶이고 태그가 모두 담긴다")
    void listGroupsTagsPerDraft() {
        Long draftNo = createDraft(draftReq(null, null, "tags-" + uid, null, null,
                List.of(tagReq(null, "t1-" + uid, 1), tagReq(null, "t2-" + uid, 2))));
        flushAndClear();

        List<ContentDraftResDto> list = contentDraftService.getDraftList(ContentDraftType.NEW, 1).getList();

        assertThat(list).filteredOn(d -> d.getDraftNo().equals(draftNo)).hasSize(1);
        ContentDraftResDto found = list.stream().filter(d -> d.getDraftNo().equals(draftNo)).findFirst().orElseThrow();
        assertThat(found.getTags()).extracting(TagInfoReqDto::getTagName)
                .containsExactly("t1-" + uid, "t2-" + uid);
    }

    @Test
    @DisplayName("수정 초안 상세는 originTitle에 원본 발행글의 현재 제목을 담는다")
    void detailOriginTitleForEditDraft() {
        Content origin = savePublishedContent("origin-" + uid, List.of());
        Long draftNo = createDraft(draftReq(null, origin.getCtntNo(), "edit-" + uid, "body", category.getCateNo(), List.of()));
        flushAndClear();

        ContentDraftResDto detail = contentDraftService.getDraftDetail(draftNo);

        assertThat(detail.getCtntNo()).isEqualTo(origin.getCtntNo());
        assertThat(detail.getOriginTitle()).isEqualTo("origin-" + uid);
        assertThat(detail.getTitle()).isEqualTo("edit-" + uid);
        assertThat(detail.getBody()).isEqualTo("body");
        assertThat(detail.getCateNo()).isEqualTo(category.getCateNo());
    }

    @Test
    @DisplayName("신규 초안 상세는 ctntNo/originTitle이 null이다")
    void detailOriginTitleNullForNewDraft() {
        Long draftNo = createDraft(draftReq(null, null, "new-" + uid, null, null, List.of()));
        flushAndClear();

        ContentDraftResDto detail = contentDraftService.getDraftDetail(draftNo);

        assertThat(detail.getCtntNo()).isNull();
        assertThat(detail.getOriginTitle()).isNull();
    }

    @Test
    @DisplayName("원본이 소프트 삭제된 수정 초안은 originTitle이 null이다 (DELETED_YN 조인 조건)")
    void detailOriginTitleNullWhenOriginSoftDeleted() {
        Content origin = savePublishedContent("origin-" + uid, List.of());
        Long draftNo = createDraft(draftReq(null, origin.getCtntNo(), "edit-" + uid, "body", category.getCateNo(), List.of()));
        flushAndClear();
        softDelete(origin.getCtntNo());

        ContentDraftResDto detail = contentDraftService.getDraftDetail(draftNo);

        assertThat(detail.getCtntNo()).isEqualTo(origin.getCtntNo());
        assertThat(detail.getOriginTitle()).isNull();
    }

    @Test
    @DisplayName("없는 초안 상세 조회는 DataNotFoundException")
    void detailNotFound() {
        assertThatThrownBy(() -> contentDraftService.getDraftDetail(Long.MAX_VALUE))
                .isInstanceOf(DataNotFoundException.class);
    }
}
package com.devlog.admin.service.content;

import com.devlog.admin.mapper.content.ContentDraftMapper;
import com.devlog.admin.service.content.dto.request.ContentDraftReqDto;
import com.devlog.admin.service.content.dto.response.ContentDraftListResDto;
import com.devlog.admin.service.content.dto.response.ContentDraftResDto;
import com.devlog.admin.service.tag.dto.request.TagReqDto;
import com.devlog.core.common.enumulation.ContentDraftType;
import com.devlog.core.common.enumulation.ResponseCode;
import com.devlog.core.common.utils.PageUtils;
import com.devlog.core.common.utils.TokenUtils;
import com.devlog.core.config.exception.DataNotFoundException;
import com.devlog.core.config.exception.ValidationException;
import com.devlog.core.entity.category.Category;
import com.devlog.core.entity.content.ContentDraft;
import com.devlog.core.entity.content.ContentDraftTag;
import com.devlog.core.entity.tag.Tag;
import com.devlog.core.repository.category.CategoryRepository;
import com.devlog.core.repository.content.ContentDraftRepository;
import com.devlog.core.repository.content.ContentDraftTagRepository;
import com.devlog.core.repository.tag.TagRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ContentDraftService {

    // repository
    private final ContentDraftRepository contentDraftRepository;
    private final ContentDraftTagRepository contentDraftTagRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    // mapper
    private final ContentDraftMapper contentDraftMapper;

    // utils
    private final TokenUtils tokenUtils;
    private final PageUtils pageUtils;

    /**
     * 초안 목록 조회하기
     *
     * @param type 초안 유형 (NEW: 신규 글, EDIT: 발행글 수정)
     * @param page 페이지 번호
     * @return 초안 리스트
     */
    public ContentDraftListResDto getDraftList(ContentDraftType type, Integer page) {
        RowBounds rowBounds = pageUtils.getRowBounds(page);
        Integer totalCount = contentDraftMapper.selectCountDraftList(type);
        List<ContentDraftResDto> draftList = contentDraftMapper.selectDraftList(type, rowBounds);
        return ContentDraftListResDto.of(draftList, page, totalCount);
    }

    /**
     * 초안 상세 조회하기
     *
     * @param draftNo 초안 번호
     * @return 초안 객체
     */
    public ContentDraftResDto getDraftDetail(Long draftNo) {
        return contentDraftMapper.selectDraft(draftNo)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));
    }

    /**
     * 초안 생성하기 (임시저장)
     * ctntNo가 없으면 신규 글 초안, 있으면 발행글 수정 초안.
     * 새 태그명은 여기서 tags_mst에 생성하지 않고 이름만 들고 있다가 발행 시점에 생성한다.
     *
     * @param request http 요청 객체
     * @param reqDto  초안 요청 객체
     */
    public void createDraft(HttpServletRequest request, ContentDraftReqDto reqDto) {
        String userId = tokenUtils.getUserIdFromHeader(request);

        Category category = findCategoryOrThrow(reqDto.getCateNo());

        ContentDraft draft = ContentDraft.created(reqDto.getCtntNo(), reqDto.getTitle(), reqDto.getSubTitle(),
                reqDto.getBody(), category, reqDto.getPath(), reqDto.getName(), reqDto.getExt(), userId);
        contentDraftRepository.save(draft);

        contentDraftTagRepository.saveAll(toDraftTags(reqDto.getTags(), draft));
    }

    /**
     * 초안 수정하기 (임시저장 갱신 — 같은 draftNo로 덮어쓰기 저장)
     *
     * @param request http 요청 객체
     * @param reqDto  초안 요청 객체
     */
    public void updateDraft(HttpServletRequest request, ContentDraftReqDto reqDto) {
        if (Objects.isNull(reqDto.getDraftNo())) {
            throw new ValidationException("초안 번호는 필수입니다.");
        }
        String userId = tokenUtils.getUserIdFromHeader(request);

        ContentDraft draft = contentDraftRepository.findById(reqDto.getDraftNo())
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));
        Category category = findCategoryOrThrow(reqDto.getCateNo());

        draft.updated(reqDto.getTitle(), reqDto.getSubTitle(), reqDto.getBody(), category,
                reqDto.getPath(), reqDto.getName(), reqDto.getExt(), userId);

        // 임시저장 갱신이므로 기존 태그 매핑을 diff 없이 통째로 덮어씀
        if (!draft.getDraftTags().isEmpty()) {
            contentDraftTagRepository.deleteAllInBatch(draft.getDraftTags());
        }
        contentDraftTagRepository.saveAll(toDraftTags(reqDto.getTags(), draft));
    }

    /**
     * 초안 삭제하기
     *
     * @param draftNo 초안 번호
     */
    public void deleteDraft(Long draftNo) {
        ContentDraft draft = contentDraftRepository.findById(draftNo)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DELETE_DATA.getMessage()));
        contentDraftTagRepository.deleteAll(draft.getDraftTags());
        contentDraftRepository.delete(draft);
    }

    private Category findCategoryOrThrow(Long cateNo) {
        if (Objects.isNull(cateNo)) {
            return null;
        }
        return categoryRepository.findById(cateNo)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));
    }

    private List<ContentDraftTag> toDraftTags(List<TagReqDto> tagReqDtoList, ContentDraft draft) {
        List<ContentDraftTag> draftTags = new ArrayList<>();
        if (Objects.isNull(tagReqDtoList)) {
            return draftTags;
        }
        tagReqDtoList.forEach(tagReqDto -> {
            // 기존 태그를 고른 경우만 Tag 엔티티를 채움 — 새 태그명은 발행 시점에 생성(§6-1)
            Tag tag = Objects.isNull(tagReqDto.getTagNo()) ? null : tagRepository.findById(tagReqDto.getTagNo()).orElse(null);
            int sort = Objects.isNull(tagReqDto.getSort()) ? 0 : tagReqDto.getSort();
            draftTags.add(ContentDraftTag.created(draft, tag, tagReqDto.getTagName(), sort));
        });
        return draftTags;
    }

}
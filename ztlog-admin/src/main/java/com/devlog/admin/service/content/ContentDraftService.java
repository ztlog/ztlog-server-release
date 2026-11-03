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
import com.devlog.core.entity.content.Content;
import com.devlog.core.entity.content.ContentDraft;
import com.devlog.core.entity.content.ContentDraftTag;
import com.devlog.core.entity.content.ContentTag;
import com.devlog.core.entity.tag.Tag;
import com.devlog.core.repository.category.CategoryRepository;
import com.devlog.core.repository.content.ContentDraftRepository;
import com.devlog.core.repository.content.ContentDraftTagRepository;
import com.devlog.core.repository.content.ContentRepository;
import com.devlog.core.repository.content.ContentTagRepository;
import com.devlog.core.repository.tag.TagRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ContentDraftService {

    // repository
    private final ContentDraftRepository contentDraftRepository;
    private final ContentDraftTagRepository contentDraftTagRepository;
    private final ContentRepository contentRepository;
    private final ContentTagRepository contentTagRepository;
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
        List<ContentDraftResDto> draftList = contentDraftMapper.selectDraftList(type, rowBounds.getOffset(), rowBounds.getLimit());
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

        // 발행글 수정 초안이면 원본이 실제로 있는지 확인 (없는 ctntNo로 만든 초안은 발행이 불가능함)
        if (!Objects.isNull(reqDto.getCtntNo()) && !contentRepository.existsById(reqDto.getCtntNo())) {
            throw new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage());
        }
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
        removeDraft(draft);
    }

    /**
     * 초안 발행하기
     * ctntNo가 없으면 신규 글로 발행하고, 있으면 기존 발행글을 초안 내용으로 덮어쓴다.
     * 새 태그명은 이 시점에 처음으로 tags_mst에 생성된다. 발행에 성공하면 초안은 삭제된다.
     *
     * @param request http 요청 객체
     * @param draftNo 초안 번호
     */
    public void publish(HttpServletRequest request, Long draftNo) {
        ContentDraft draft = contentDraftRepository.findById(draftNo)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));

        // 발행 시점 필수값 검증 (임시저장은 미완성 허용)
        if (!StringUtils.hasText(draft.getCtntTitle())) {
            throw new ValidationException("제목은 필수입니다.");
        }
        if (!StringUtils.hasText(draft.getCtntBody())) {
            throw new ValidationException("본문은 필수입니다.");
        }
        if (Objects.isNull(draft.getCategory())) {
            throw new ValidationException("카테고리 번호는 필수입니다.");
        }

        String userId = tokenUtils.getUserIdFromHeader(request);
        Category category = findCategoryOrThrow(draft.getCategory().getCateNo());

        if (Objects.isNull(draft.getCtntNo())) {
            publishNew(draft, category, userId);
        } else {
            publishEdit(draft, category, userId);
        }
        removeDraft(draft);
    }

    private void publishNew(ContentDraft draft, Category category, String userId) {
        Content content = Content.created(draft.getCtntTitle(), subTitleOrEmpty(draft), draft.getCtntBody(), category, userId);
        contentRepository.save(content);

        List<ContentTag> contentTags = new ArrayList<>();
        Set<Long> seenTagNos = new HashSet<>();
        draft.getDraftTags().forEach(draftTag -> {
            Tag tag = resolveTag(draftTag);
            // 대소문자만 다른 태그명은 DB(utf8mb4_unicode_ci)에서 같은 태그로 풀리므로 tagNo 기준으로 한 번 더 중복 제거
            if (seenTagNos.add(tag.getTagNo())) {
                contentTags.add(ContentTag.created(tag, draftTag.getSort(), content));
            }
        });
        contentTagRepository.saveAll(contentTags);
    }

    private void publishEdit(ContentDraft draft, Category category, String userId) {
        // 원본이 소프트 삭제됐거나 없으면 DataNotFoundException
        Content content = contentRepository.findById(draft.getCtntNo())
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));

        // 기존 태그 리스트 Map으로 변환
        Map<Long, ContentTag> exTagMap = content.getContentTags().stream()
                .collect(Collectors.toMap(contentTag -> contentTag.getTags().getTagNo(), contentTag -> contentTag));

        List<ContentTag> newTagsList = new ArrayList<>();
        Set<Long> seenTagNos = new HashSet<>();
        draft.getDraftTags().forEach(draftTag -> {
            Tag tag = resolveTag(draftTag);
            // 대소문자만 다른 태그명은 DB(utf8mb4_unicode_ci)에서 같은 태그로 풀리므로 tagNo 기준으로 한 번 더 중복 제거
            if (!seenTagNos.add(tag.getTagNo())) {
                return;
            }
            ContentTag existing = exTagMap.remove(tag.getTagNo());
            if (!Objects.isNull(existing)) {
                // 이미 존재하는 태그는 유지 -> 정렬 순서만 업데이트
                existing.updated(existing.getTags(), draftTag.getSort());
                newTagsList.add(existing);
            } else {
                newTagsList.add(ContentTag.created(tag, draftTag.getSort(), content));
            }
        });

        // 요청에 포함되지 않은 기존 태그는 삭제
        if (!exTagMap.isEmpty()) {
            contentTagRepository.deleteAllInBatch(exTagMap.values());
        }

        content.updated(draft.getCtntTitle(), subTitleOrEmpty(draft), category, userId);
        content.getContentDetail().updated(draft.getCtntTitle(), draft.getCtntBody(), content, userId);
        contentTagRepository.saveAll(newTagsList);
    }

    /**
     * 초안 태그를 실제 Tag 엔티티로 변환.
     * tagNo가 있는 태그는 조회(없으면 DataNotFoundException), 새 태그명은 같은 이름이 없을 때만 여기서 tags_mst에 생성한다.
     */
    private Tag resolveTag(ContentDraftTag draftTag) {
        if (Objects.isNull(draftTag.getTags())) {
            // tags_mst.TAG_NAME은 unique이므로 같은 이름이 이미 있으면 재사용
            return tagRepository.findByTagName(draftTag.getTagName())
                    .orElseGet(() -> tagRepository.save(Tag.created(draftTag.getTagName())));
        }
        return tagRepository.findById(draftTag.getTags().getTagNo())
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));
    }

    // contents_mst.CTNT_SUBTITLE은 NOT NULL인데 초안은 부제목 없이도 저장되므로 발행 시 빈 문자열로 대체
    private String subTitleOrEmpty(ContentDraft draft) {
        return Objects.requireNonNullElse(draft.getCtntSubTitle(), "");
    }

    private void removeDraft(ContentDraft draft) {
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
        // 완전히 같은 태그명이 중복으로 들어오면 첫 항목만 남김 (대소문자 차이는 발행 시 tagNo 기준으로 다시 걸러짐)
        Set<String> seenTagNames = new HashSet<>();
        tagReqDtoList.forEach(tagReqDto -> {
            if (!seenTagNames.add(tagReqDto.getTagName())) {
                return;
            }
            // 기존 태그를 고른 경우만 Tag 엔티티를 채움 — 새 태그명은 발행 시점에 생성(§6-1)
            Tag tag = Objects.isNull(tagReqDto.getTagNo()) ? null : tagRepository.findById(tagReqDto.getTagNo()).orElse(null);
            int sort = Objects.isNull(tagReqDto.getSort()) ? 0 : tagReqDto.getSort();
            draftTags.add(ContentDraftTag.created(draft, tag, tagReqDto.getTagName(), sort));
        });
        return draftTags;
    }

}
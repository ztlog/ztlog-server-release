package com.devlog.admin.service.content;

import com.devlog.admin.service.content.dto.request.ContentReqDto;
import com.devlog.admin.mapper.content.ContentMapper;
import com.devlog.admin.service.content.dto.response.ContentResDto;
import com.devlog.admin.service.content.dto.response.ContentListResDto;
import com.devlog.core.common.enumulation.ResponseCode;
import com.devlog.core.common.enumulation.SearchType;
import com.devlog.core.common.utils.PageUtils;
import com.devlog.core.common.utils.TokenUtils;
import com.devlog.core.config.exception.DataNotFoundException;
import com.devlog.core.entity.category.Category;
import com.devlog.core.entity.content.Content;
import com.devlog.core.entity.content.ContentTag;
import com.devlog.core.entity.tag.Tag;
import com.devlog.core.repository.category.CategoryRepository;
import com.devlog.core.repository.content.ContentRepository;
import com.devlog.core.repository.content.ContentTagRepository;
import com.devlog.core.repository.tag.TagRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ContentService {

    private final EntityManager entityManager;

    // repository
    private final ContentRepository contentRepository;
    private final CategoryRepository categoryRepository;
    private final ContentTagRepository contentTagRepository;
    private final TagRepository tagRepository;

    // mapper
    private final ContentMapper contentMapper;

    // utils
    private final TokenUtils tokenUtils;
    private final PageUtils pageUtils;

    /**
     * 컨텐츠 리스트 조회하기
     *
     * @param page 페이지 번호
     * @return 컨텐츠 리스트
     */
    public ContentListResDto getContentList(Integer page) {
        RowBounds rowBounds = pageUtils.getRowBounds(page);
        Integer totalCount = contentMapper.selectCountContentList();
        List<ContentResDto> contentList = contentMapper.selectContentList(rowBounds);
        return ContentListResDto.of(contentList, page, totalCount);
    }

    /**
     * 컨텐츠 상세 조회하기
     *
     * @param ctntNo 컨텐츠 번호
     * @return 컨텐츠 객체
     */
    public ContentResDto getContentDetail(Long ctntNo) {
        return contentMapper.selectContent(ctntNo)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));
    }

    /**
     * 컨텐츠 등록하기
     *
     * @param request http 요청 객체
     * @param reqDto  컨텐츠 요청 객체
     */
    public void createContentDetail(HttpServletRequest request, ContentReqDto.ContentReqInfoDto reqDto) {
        // 카테고리 조회
        Category category = categoryRepository.findById(reqDto.getCateNo())
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));
        // 컨텐츠 생성
        Content content = Content.created(reqDto.getTitle(), reqDto.getSubTitle(), reqDto.getBody(), category, tokenUtils.getUserIdFromHeader(request));

        // 컨텐츠 태그 no로 조회, 없을 경우 새로 생성
        List<ContentTag> contentTags = new ArrayList<>();
        reqDto.getTags().forEach(tagReqDto -> {
            final var tag = tagRepository.findById(tagReqDto.getTagNo()).orElseGet(() -> tagRepository.save(Tag.created(tagReqDto.getTagName())));
            contentTags.add(ContentTag.created(tag, tagReqDto.getSort(), content));
        });
        contentRepository.save(content);
        contentTagRepository.saveAll(contentTags);
    }

    /**
     * 컨텐츠 수정하기
     *
     * @param request http 요청 객체
     * @param reqDto  컨텐츠 요청 객체
     */
    public void updateContentDetail(HttpServletRequest request, ContentReqDto.ContentReqInfoDto reqDto) {
        String userId = tokenUtils.getUserIdFromHeader(request);

        // 컨텐츠 null check
        Content content = contentRepository.findById(reqDto.getCtntNo())
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));

        // 카테고리 번호가 바뀐 경우에만 조회해서 엔티티에 넘김 (아니면 null -> updated 메소드에서 수정)
        Category category = null;
        if (!reqDto.getCateNo().equals(content.getCategory().getCateNo())) {
            category = categoryRepository.findById(reqDto.getCateNo())
                    .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));
        }

        // 기존 태그 리스트 Map으로 변환
        Map<Long, ContentTag> exTagMap = content.getContentTags().stream()
                .collect(Collectors.toMap(tag -> tag.getTags().getTagNo(), tag -> tag));

        // 새 태그 리스트 변경사항 체크
        List<ContentTag> newTagsList = new ArrayList<>();
        reqDto.getTags().forEach(tagReqDto -> {
            ContentTag existing = exTagMap.remove(tagReqDto.getTagNo());
            if (!Objects.isNull(existing)) {
                // 이미 존재하는 태그는 유지 -> 정렬 순서만 업데이트
                existing.updated(existing.getTags(), tagReqDto.getSort());
                newTagsList.add(existing);
            } else {
                // 기존에 없을 경우 -> 새로 생성
                Tag tag = tagRepository.findById(tagReqDto.getTagNo()).orElseGet(() -> tagRepository.save(Tag.created(tagReqDto.getTagName())));
                newTagsList.add(ContentTag.created(tag, tagReqDto.getSort(), content));
            }
        });

        // exTagMap 남은 객체들은 요청에 포함되지 않은 것이므로 삭제
        if (!exTagMap.isEmpty()) {
            contentTagRepository.deleteAllInBatch(exTagMap.values());
        }

        // 컨텐츠 수정 - 새로 추가되거나 수정된 리스트 반영
        content.updated(reqDto.getTitle(), reqDto.getSubTitle(), category, userId);
        content.getContentDetail().updated(reqDto.getTitle(), reqDto.getBody(), content, userId);
        contentTagRepository.saveAll(newTagsList);

    }

    /**
     * 컨텐츠 삭제하기
     *
     * @param ctntNo 컨텐츠 번호
     */
    public void deleteContentDetail(Long ctntNo) {
        final var content = contentRepository.findById(ctntNo)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DELETE_DATA.getMessage()));
        // soft delete 적용
        contentTagRepository.deleteAll(content.getContentTags());
        contentRepository.delete(content);
        contentRepository.flush();  // DB에 즉시 반영 (UPD_DTTM 갱신됨)
    }

    /**
     * 컨텐츠 검색하기
     *
     * @param type  검색 옵션
     * @param param 검색 키워드
     * @param page  페이지 번호 (기본값 = 1)
     * @return 검색한 키워드 관련 리스트 반환
     */
    public ContentListResDto searchContentList(SearchType type, String param, Integer page) {
        RowBounds rowBounds = pageUtils.getRowBounds(page);
        Integer totalCount = contentMapper.selectCountSearchContentList(type, param);
        List<ContentResDto> list = contentMapper.selectSearchContentList(type, param, rowBounds);
        return ContentListResDto.of(list, page, totalCount);
    }
}

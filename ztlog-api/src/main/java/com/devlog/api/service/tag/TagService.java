package com.devlog.api.service.tag;

import com.devlog.api.service.content.dto.ContentListResDto;
import com.devlog.api.service.tag.dto.TagResDto;
import com.devlog.core.common.enumulation.ResponseCode;
import com.devlog.core.common.utils.PageUtils;
import com.devlog.core.config.exception.DataNotFoundException;
import com.devlog.core.entity.content.Content;
import com.devlog.core.repository.content.ContentRepository;
import com.devlog.core.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final ContentRepository contentRepository;
    private final PageUtils pageUtils;

    /**
     * 태그 목록 조회하기
     *
     * @return 태그 리스트
     */
    public List<TagResDto> getTagList() {
        return tagRepository.findAllOrderByContentCount().stream()
                .map(TagResDto::of)
                .collect(Collectors.toList());
    }

    /**
     * 태그 게시물 목록 조회하기
     *
     * @param tagNo 태그 번호
     * @param page  페이지 번호 (기본값 = 1)
     * @return 태그 게시물 리스트
     */
    public ContentListResDto getTagContentList(Integer tagNo, Integer page) {
        if (!tagRepository.existsById(Long.valueOf(tagNo))) {
            throw new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage());
        }
        Page<Content> contentPage = contentRepository.findAllByContentTagsTagsTagNo(Long.valueOf(tagNo), pageUtils.getPageable(page, Content.class));
        return ContentListResDto.of(contentPage);
    }
}

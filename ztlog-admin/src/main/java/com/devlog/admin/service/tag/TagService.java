package com.devlog.admin.service.tag;

import com.devlog.admin.service.tag.dto.request.TagReqDto;
import com.devlog.admin.service.tag.dto.response.TagCountResDto;
import com.devlog.admin.service.tag.dto.response.TagListResDto;
import com.devlog.admin.service.tag.dto.response.TagResDto;
import com.devlog.admin.mapper.tag.TagMapper;
import com.devlog.core.common.enumulation.ResponseCode;
import com.devlog.core.common.utils.PageUtils;
import com.devlog.core.config.exception.DataConflictException;
import com.devlog.core.config.exception.DataNotFoundException;
import com.devlog.core.entity.tag.Tag;
import com.devlog.core.repository.content.ContentTagRepository;
import com.devlog.core.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    // repository
    private final TagRepository tagRepository;
    private final ContentTagRepository contentTagRepository;

    // mapper
    private final TagMapper tagMapper;
    private final PageUtils pageUtils;

    /**
     * 태그 목록 조회
     *
     * @return 태그 리스트
     */
    public TagListResDto getTagList(Integer page) {
        RowBounds rowBounds = pageUtils.getRowBounds(page);
        List<TagCountResDto> tagList = tagMapper.selectTagList(rowBounds);
        return TagListResDto.of(tagList, page);
    }

    /**
     * 태그 상세 조회
     *
     * @param tagNo 태그 번호
     * @return 태그 객체
     */
    public TagResDto getTagDetail(Long tagNo) {
        final var tag = tagRepository.findById(tagNo)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));
        return TagResDto.of(tag);
    }

    /**
     * 태그 등록하기
     *
     * @param reqDto 태그 요청 객체
     */
    public void createTagDetail(TagReqDto reqDto) {
        if (tagRepository.existsByTagName(reqDto.getTagName()))
            throw new DataConflictException(ResponseCode.CONFLICT_DATA_ERROR.getMessage());
        tagRepository.save(Tag.created(reqDto.getTagName()));
    }

    /**
     * 태그 수정하기
     *
     * @param reqDto 태그 요청 객체
     */
    public void updateTagDetail(TagReqDto reqDto) {
        Tag tag = tagRepository.findById(reqDto.getTagNo())
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));
        tag.updated(reqDto.getTagName());
    }

    /**
     * 태그 삭제하기
     *
     * @param tagNo 태그 번호
     */
    public void deleteTagDetail(Long tagNo) {
        var tag = tagRepository.findById(tagNo)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DELETE_DATA.getMessage()));
        contentTagRepository.deleteAll(tag.getContentTags());
        tagRepository.delete(tag);
    }

}

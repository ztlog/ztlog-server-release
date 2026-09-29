package com.devlog.admin.mapper.content;

import com.devlog.admin.service.content.dto.response.ContentDraftResDto;
import com.devlog.core.common.enumulation.ContentDraftType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ContentDraftMapper {

    Integer selectCountDraftList(@Param("type") ContentDraftType type);

    List<ContentDraftResDto> selectDraftList(@Param("type") ContentDraftType type, RowBounds rowBounds);

    Optional<ContentDraftResDto> selectDraft(Long draftNo);
}
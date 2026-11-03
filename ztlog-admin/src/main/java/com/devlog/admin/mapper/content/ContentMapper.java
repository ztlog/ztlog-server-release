package com.devlog.admin.mapper.content;

import com.devlog.admin.service.content.dto.response.ContentResDto;
import com.devlog.core.common.enumulation.SearchType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ContentMapper {
    Integer selectCountContentList();

    List<ContentResDto> selectContentList(@Param("offset") int offset, @Param("limit") int limit);

    Integer selectCountSearchContentList(SearchType type, String param);

    List<ContentResDto> selectSearchContentList(@Param("type") SearchType type, @Param("param") String param,
                                                @Param("offset") int offset, @Param("limit") int limit);

    Optional<ContentResDto> selectContent(Long ctntNo);
}

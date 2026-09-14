package com.devlog.core.repository.tag;

import com.devlog.core.entity.tag.Tag;

import java.util.List;

public interface TagRepositoryCustom {

    Tag findTagById(Long id);

    List<Tag> findAllOrderByContentCount();
}

package com.devlog.core.repository.tag;

import com.devlog.core.entity.tag.Tag;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.devlog.core.entity.content.QContentTag.contentTag;
import static com.devlog.core.entity.tag.QTag.tag;

@RequiredArgsConstructor
public class TagRepositoryCustomImpl implements TagRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Tag findTagById(Long id) {
        return queryFactory.selectFrom(tag)
                .where(tag.tagNo.eq(id))
                .fetchOne();
    }

    @Override
    public List<Tag> findAllOrderByContentCount() {
        return queryFactory.selectFrom(tag)
                .leftJoin(tag.contentTags, contentTag).fetchJoin()
                .distinct()
                .fetch()
                .stream()
                .sorted(Comparator.comparingInt((Tag t) -> t.getContentTags().size()).reversed())
                .collect(Collectors.toList());
    }

}

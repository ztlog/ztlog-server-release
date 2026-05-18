package com.devlog.core.repository.content.impl;

import com.devlog.core.common.enumulation.SearchType;
import com.devlog.core.common.utils.PageUtils;
import com.devlog.core.entity.content.Content;
import com.devlog.core.repository.content.ContentRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.devlog.core.entity.content.QContent.content;

@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepositoryCustom {

    private final JPAQueryFactory query;
    private final PageUtils pageUtils;

    @Override
    public Page<Content> findContentsByCondition(SearchType type, String keyword, int page) {
        Pageable pageable = pageUtils.getPageable(page, Content.class);

        List<Content> contentList = query.selectFrom(content)
                .where(searchCondition(type, keyword))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query.select(content.count())
                .from(content)
                .where(searchCondition(type, keyword));

        return PageableExecutionUtils.getPage(contentList, pageable, () -> {
            Long totalCount = countQuery.fetchOne();
            return (totalCount != null) ? totalCount : 0L;
        });
    }

    private BooleanExpression searchCondition(SearchType type, String keyword) {
        if (keyword == null || keyword.isBlank())
            return null;

        return switch (type) {
            case TITLE -> content.ctntTitle.contains(keyword);
            case CONTENT -> content.contentDetail.ctntBody.contains(keyword);
            case TITLE_CONTENT -> content.ctntTitle.contains(keyword).or(content.contentDetail.ctntBody.contains(keyword));
            case TAG -> content.contentTags.any().tags.tagName.eq(keyword);
        };
    }

}

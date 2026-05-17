package com.devlog.core.common.utils;

import com.devlog.core.common.constants.CommonConstants;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PageUtils {

    private final EntityManager entityManager;

    public PageRequest getPageable(int page, Class<?> entityClass) {
        String pkName = getPrimaryKeyName(entityClass);
        return PageRequest.of(page - 1, CommonConstants.PAGE_SIZE, Sort.by(pkName).descending());
    }

    public PageRequest getPageableEx(int page, Class<?> entityClass) {
        String pkName = getPrimaryKeyName(entityClass);
        return PageRequest.of(page - 1, CommonConstants.PAGE_LIST_SIZE, Sort.by(pkName).descending());
    }

    public int getStartIdx(int page) {
        return Long.valueOf(PageRequest.of(page - 1, CommonConstants.PAGE_SIZE).getOffset()).intValue();
    }

    public int getEndIdx(int start, int page, Class<?> entityClass) {
        return start + getPageable(page, entityClass).getPageSize();
    }

    public RowBounds getRowBounds(Integer page) {
        int offset = (page - 1) * CommonConstants.PAGE_LIST_SIZE;
        return new RowBounds(offset, CommonConstants.PAGE_LIST_SIZE);
    }

    private String getPrimaryKeyName(Class<?> entityClass) {
        try {
            //JPA Metamodel을 이용하여 엔티티의 PK 변수명을 동적으로 획득
            return entityManager.getMetamodel()
                    .entity(entityClass)
                    .getId(Long.class)
                    .getName();
        } catch (Exception e) {
            log.error("PK를 찾지 못함: {}", e.getMessage());
            // PK를 찾지 못할 경우 기본값 'id' 반환 (Safety Net)
            return "id";
        }
    }

}

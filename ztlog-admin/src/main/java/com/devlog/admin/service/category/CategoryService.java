package com.devlog.admin.service.category;

import com.devlog.admin.service.category.dto.request.CategorySaveReqDto;
import com.devlog.admin.service.category.dto.request.CategoryUpdateReqDto;
import com.devlog.admin.service.category.dto.response.CategoryListResDto;
import com.devlog.admin.service.category.dto.response.CategoryResDto;
import com.devlog.admin.mapper.category.CategoryMapper;
import com.devlog.core.common.enumulation.ResponseCode;
import com.devlog.core.common.enumulation.UseYN;
import com.devlog.core.common.utils.PageUtils;
import com.devlog.core.common.utils.TokenUtils;
import com.devlog.core.config.exception.DataConflictException;
import com.devlog.core.config.exception.DataNotFoundException;
import com.devlog.core.entity.category.Category;
import com.devlog.core.repository.category.CategoryRepository;
import com.devlog.core.repository.content.ContentRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final EntityManager entityManager;

    // repository
    private final CategoryRepository categoryRepository;
    private final ContentRepository contentRepository;

    // mapper
    private final CategoryMapper categoryMapper;

    // utils
    private final TokenUtils tokenUtils;
    private final PageUtils pageUtils;

    /**
     * 카테고리 목록 조회하기
     *
     * @return 컨텐츠 리스트 반환
     */
    public List<CategoryListResDto> getCategoryList() {
        return categoryMapper.selectCategoryList();
    }

    /**
     * 카테고리 상세 조회
     *
     * @param cateNo 카테고리 번호
     * @return 카테고리 상세 정보
     */
    public CategoryResDto getCategoryDetail(Long cateNo) {
        return categoryMapper.selectCategory(cateNo).orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));
    }

    /**
     * 카테고리 등록하기
     *
     * @param request HTTP 요청 객체
     * @param reqDto  카테고리 정보 객체
     */
    public void createCategoryDetail(HttpServletRequest request, CategorySaveReqDto reqDto) {
        // 상위 카테고리 조회
        Category upperCategory = null;
        if (reqDto.getUpperCateNo() != null) {
            upperCategory = categoryRepository.findById(reqDto.getUpperCateNo()).orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DELETE_DATA.getMessage()));
        }

        Category category = Category.created(reqDto.getCateNm(), reqDto.getCateDepth(), reqDto.getDispOrd(), tokenUtils.getUserIdFromHeader(request), upperCategory);
        categoryRepository.save(category);
    }

    /**
     * 카테고리 수정하기
     *
     * @param request HTTP 요청 객체
     * @param reqDto  카테고리 정보 객체
     */
    public void updateCategoryDetail(HttpServletRequest request, CategoryUpdateReqDto reqDto) {
        // 카테고리 조회 (Fetch Join은 Repository 단계에서 처리)
        Category category = categoryRepository.findById(reqDto.getCateNo())
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DELETE_DATA.getMessage()));

        // 상위 카테고리 조회 (Optional로 간결화)
        Category upperCategory = Optional.ofNullable(reqDto.getUpperCateNo())
                .map(id -> categoryRepository.findById(id).orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DELETE_DATA.getMessage())))
                .orElse(null);

        // 자식 카테고리 재귀 업데이트 (CollectionUtils나 isEmpty 체크 활용)
        String currentUser = tokenUtils.getUserIdFromHeader(request);
        if (!reqDto.getCategories().isEmpty()) {
            reqDto.getCategories().forEach(childDto -> updateOrCreateChild(childDto, category, currentUser));
        }

        // 4. 정보 갱신
        category.updated(reqDto.getCateNm(), reqDto.getCateDepth(), reqDto.getDispOrd(), UseYN.valueOf(reqDto.getUseYn()), currentUser, upperCategory);
    }


    /**
     * 하위 카테고리 수정하기
     *
     * @param childDto 하위 카테고리 요청 객체
     * @param parent   상위 카테고리 엔티티
     */
    private void updateOrCreateChild(CategoryUpdateReqDto childDto, Category parent, String currentUser) {
        if (childDto.getCateNo() != null) {
            // 기존 자식 카테고리 수정
            Category child = categoryRepository.findById(childDto.getCateNo())
                    .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DELETE_DATA.getMessage()));

            child.updated(childDto.getCateNm(), childDto.getCateDepth(), childDto.getDispOrd(), UseYN.valueOf(childDto.getUseYn()), currentUser, parent);

            // 자식의 자식들도 재귀 처리
            if (childDto.getCategories() != null) {
                childDto.getCategories().forEach(grandChild -> updateOrCreateChild(grandChild, child, currentUser));
            }
        } else {
            // 신규 자식 카테고리 등록
            Category newChild = Category.created(childDto.getCateNm(), childDto.getCateDepth(), childDto.getDispOrd(), currentUser, parent);
            categoryRepository.save(newChild);
        }
    }

    /**
     * 카테고리 삭제하기
     *
     * @param cateNo 카테고리 번호
     */
    public void deleteCategoryDetail(Long cateNo) {
        var category = categoryRepository.findById(cateNo)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DELETE_DATA.getMessage()));

        if (!category.getCategories().isEmpty()) {
            throw new DataConflictException(ResponseCode.CONFLICT_CATEGORY_HAS_CHILDREN.getMessage(), ResponseCode.CONFLICT_CATEGORY_HAS_CHILDREN);
        }
        if (contentRepository.countByCategoryCateNo(cateNo) > 0) {
            throw new DataConflictException(ResponseCode.CONFLICT_CATEGORY_IN_USE.getMessage(), ResponseCode.CONFLICT_CATEGORY_IN_USE);
        }

        categoryRepository.delete(category);
    }
}

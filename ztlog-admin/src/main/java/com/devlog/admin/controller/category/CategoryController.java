package com.devlog.admin.controller.category;

import com.devlog.admin.service.category.dto.request.CategorySaveReqDto;
import com.devlog.admin.service.category.dto.request.CategoryUpdateReqDto;
import com.devlog.admin.service.category.dto.response.CategoryListResDto;
import com.devlog.admin.service.category.dto.response.CategoryResDto;
import com.devlog.admin.service.category.CategoryService;
import com.devlog.core.common.dto.Response;
import com.devlog.core.common.enumulation.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "카테고리 컨트롤러", description = "컨텐츠 컨트롤러")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 카테고리 목록 조회하기
     *
     * @return 컨텐츠 리스트 반환
     */
    @Operation(summary = "카테고리 목록 조회", description = "카테고리 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @GetMapping("/categories")
    public ResponseEntity<Response<List<CategoryListResDto>>> getCategoryList() {
        return Response.success(ResponseCode.OK_SUCCESS, categoryService.getCategoryList());
    }

    /**
     * 카테고리 상세 조회
     *
     * @param cateNo 카테고리 번호
     * @return 카테고리 상세 정보
     */
    @Operation(summary = "카테고리 상세 조회", description = "카테고리 상세 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @GetMapping("/categories/{cateNo}")
    public ResponseEntity<Response<CategoryResDto>> getCategoryDetail(@PathVariable Long cateNo) {
        return Response.success(ResponseCode.OK_SUCCESS, categoryService.getCategoryDetail(cateNo));
    }

    /**
     * 카테고리 등록하기
     *
     * @param request HTTP 요청 객체
     * @param reqDto  카테고리 정보 객체
     * @return 성공 응답
     */
    @Operation(summary = "카테고리 등록", description = "카테고리 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @PostMapping("/categories")
    public ResponseEntity<Response<String>> createCategoryDetail(HttpServletRequest request, @RequestBody @Valid CategorySaveReqDto reqDto) {
        categoryService.createCategoryDetail(request, reqDto);
        return Response.success(ResponseCode.CREATED_SUCCESS);
    }

    /**
     * 카테고리 수정하기
     *
     * @param request HTTP 요청 객체
     * @param reqDto  카테고리 정보 객체
     * @return 성공 응답
     */
    @Operation(summary = "카테고리 수정", description = "카테고리 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @PutMapping("/categories")
    public ResponseEntity<Response<String>> updateCategoryDetail(HttpServletRequest request, @RequestBody @Valid CategoryUpdateReqDto reqDto) {
        categoryService.updateCategoryDetail(request, reqDto);
        return Response.success(ResponseCode.CREATED_SUCCESS);
    }

    /**
     * 카테고리 삭제하기
     *
     * @param cateNo 카테고리 번호
     * @return 성공 응답
     */
    @Operation(summary = "카테고리 삭제", description = "카테고리 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @DeleteMapping("/categories/{cateNo}")
    public ResponseEntity<Response<String>> deleteCategoryDetail(@PathVariable Long cateNo) {
        categoryService.deleteCategoryDetail(cateNo);
        return Response.success(ResponseCode.OK_SUCCESS);
    }
}

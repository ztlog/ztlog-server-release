package com.devlog.admin.controller.content;

import com.devlog.admin.service.content.dto.request.ContentReqDto;
import com.devlog.admin.service.content.dto.response.ContentResDto;
import com.devlog.admin.service.content.dto.response.ContentListResDto;
import com.devlog.admin.service.content.ContentService;
import com.devlog.core.common.enumulation.ResponseCode;
import com.devlog.core.common.dto.Response;
import com.devlog.core.common.enumulation.SearchType;
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

@Tag(name = "컨텐츠 컨트롤러", description = "컨텐츠 컨트롤러")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ContentController {

    private final ContentService contentService;

    /**
     * 컨텐츠 목록 조회하기
     *
     * @param page 페이지 번호 (기본값 = 1)
     * @return 컨텐츠 리스트 반환
     */
    @Operation(summary = "컨텐츠 목록 조회", description = "컨텐츠 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @GetMapping("/contents")
    public ResponseEntity<Response<ContentListResDto>> getContentList(@RequestParam(value = "page", defaultValue = "1") Integer page) {
        return Response.success(ResponseCode.OK_SUCCESS, contentService.getContentList(page));
    }

    /**
     * 컨텐츠 상세 조회하기
     *
     * @param ctntNo 컨텐츠 번호
     * @return 컨텐츠 반환
     */
    @Operation(summary = "컨텐츠 상세 조회", description = "컨텐츠 상세 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @GetMapping("/contents/{ctntNo}")
    public ResponseEntity<Response<ContentResDto>> getContentDetail(@PathVariable Long ctntNo) {
        return Response.success(ResponseCode.OK_SUCCESS, contentService.getContentDetail(ctntNo));
    }


    /**
     * 컨텐츠 등록하기
     *
     * @param request HTTP 요청 객체
     * @param reqDto  컨텐츠 객체
     * @return 성공 응답
     */
    @Operation(summary = "컨텐츠 등록", description = "컨텐츠 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @PostMapping("/contents")
    public ResponseEntity<Response<String>> createContentDetail(HttpServletRequest request, @RequestBody @Valid ContentReqDto.ContentReqInfoDto reqDto) {
        contentService.createContentDetail(request, reqDto);
        return Response.success(ResponseCode.CREATED_SUCCESS);
    }

    /**
     * 컨텐츠 수정하기
     *
     * @param reqDto 컨텐츠 객체
     * @return 성공 응답
     */
    @Operation(summary = "컨텐츠 수정", description = "컨텐츠 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @PutMapping("/contents")
    public ResponseEntity<Response<String>> updateContentDetail(HttpServletRequest request, @RequestBody @Valid ContentReqDto.ContentReqInfoDto reqDto) {
        contentService.updateContentDetail(request, reqDto);
        return Response.success(ResponseCode.CREATED_SUCCESS);
    }

    /**
     * 컨텐츠 삭제하기
     *
     * @param ctntNo 컨텐츠 번호
     * @return 성공 응답
     */
    @Operation(summary = "컨텐츠 삭제", description = "컨텐츠 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @DeleteMapping("/contents/{ctntNo}")
    public ResponseEntity<Response<String>> deleteContentDetail(@PathVariable Long ctntNo) {
        contentService.deleteContentDetail(ctntNo);
        return Response.success(ResponseCode.OK_SUCCESS);
    }

    /**
     * 컨텐츠 검색하기
     *
     * @param type 검색 옵션(제목, 제목+내용, 내용, 태크)
     * @param param 검색 키워드
     * @param page 페이지 번호
     * @return 검색 결과 리스트
     */
    @Operation(summary = "컨텐츠 검색", description = "컨텐츠 검색")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ContentListResDto.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러가 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @GetMapping("/contents/search")
    public ResponseEntity<Response<ContentListResDto>> searchContentList(
            @RequestParam(value = "type") SearchType type,
            @RequestParam(value = "param") String param,
            @RequestParam(value = "page", defaultValue = "1") Integer page
    ) {
        return Response.success(ResponseCode.OK_SUCCESS, contentService.searchContentList(type, param, page));
    }

}

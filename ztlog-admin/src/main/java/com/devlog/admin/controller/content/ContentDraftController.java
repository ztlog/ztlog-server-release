package com.devlog.admin.controller.content;

import com.devlog.admin.service.content.ContentDraftService;
import com.devlog.admin.service.content.dto.request.ContentDraftReqDto;
import com.devlog.admin.service.content.dto.response.ContentDraftListResDto;
import com.devlog.admin.service.content.dto.response.ContentDraftResDto;
import com.devlog.core.common.dto.Response;
import com.devlog.core.common.enumulation.ContentDraftType;
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

@Tag(name = "컨텐츠 초안 컨트롤러", description = "컨텐츠 초안(임시저장) 컨트롤러")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents/drafts")
public class ContentDraftController {

    private final ContentDraftService contentDraftService;

    /**
     * 초안 목록 조회하기
     *
     * @param type 초안 유형 (NEW: 신규 글, EDIT: 발행글 수정)
     * @param page 페이지 번호 (기본값 = 1)
     * @return 초안 리스트 반환
     */
    @Operation(summary = "초안 목록 조회", description = "초안 목록 조회 (신규 글 초안/발행글 수정 초안 분리 조회)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @GetMapping
    public ResponseEntity<Response<ContentDraftListResDto>> getDraftList(
            @RequestParam(value = "type") ContentDraftType type,
            @RequestParam(value = "page", defaultValue = "1") Integer page
    ) {
        return Response.success(ResponseCode.OK_SUCCESS, contentDraftService.getDraftList(type, page));
    }

    /**
     * 초안 상세 조회하기
     *
     * @param draftNo 초안 번호
     * @return 초안 반환
     */
    @Operation(summary = "초안 상세 조회", description = "초안 상세 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @GetMapping("/{draftNo}")
    public ResponseEntity<Response<ContentDraftResDto>> getDraftDetail(@PathVariable Long draftNo) {
        return Response.success(ResponseCode.OK_SUCCESS, contentDraftService.getDraftDetail(draftNo));
    }

    /**
     * 초안 생성하기 (임시저장)
     *
     * @param request HTTP 요청 객체
     * @param reqDto  초안 요청 객체
     * @return 성공 응답
     */
    @Operation(summary = "초안 생성", description = "초안 생성 (임시저장) — ctntNo가 없으면 신규 글 초안, 있으면 발행글 수정 초안")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @PostMapping
    public ResponseEntity<Response<String>> createDraft(HttpServletRequest request, @RequestBody @Valid ContentDraftReqDto reqDto) {
        contentDraftService.createDraft(request, reqDto);
        return Response.success(ResponseCode.CREATED_SUCCESS);
    }

    /**
     * 초안 수정하기 (임시저장 갱신)
     *
     * @param request HTTP 요청 객체
     * @param reqDto  초안 요청 객체
     * @return 성공 응답
     */
    @Operation(summary = "초안 수정", description = "초안 수정 (임시저장 갱신 — 같은 draftNo로 덮어쓰기 저장)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @PutMapping
    public ResponseEntity<Response<String>> updateDraft(HttpServletRequest request, @RequestBody @Valid ContentDraftReqDto reqDto) {
        contentDraftService.updateDraft(request, reqDto);
        return Response.success(ResponseCode.CREATED_SUCCESS);
    }

    /**
     * 초안 삭제하기
     *
     * @param draftNo 초안 번호
     * @return 성공 응답
     */
    @Operation(summary = "초안 삭제", description = "초안 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @DeleteMapping("/{draftNo}")
    public ResponseEntity<Response<String>> deleteDraft(@PathVariable Long draftNo) {
        contentDraftService.deleteDraft(draftNo);
        return Response.success(ResponseCode.OK_SUCCESS);
    }

}
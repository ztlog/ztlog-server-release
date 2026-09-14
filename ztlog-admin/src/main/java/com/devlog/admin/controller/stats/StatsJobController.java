package com.devlog.admin.controller.stats;

import com.devlog.admin.service.stats.dto.request.CommentStatsReqDto;
import com.devlog.admin.service.stats.dto.request.ViewRawLogReqDto;
import com.devlog.admin.service.stats.dto.request.ViewStatsReqDto;
import com.devlog.admin.service.stats.CommentStatsService;
import com.devlog.admin.service.stats.ViewStatsService;
import com.devlog.core.common.dto.Response;
import com.devlog.core.common.enumulation.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "통계 집계 실행", description = "통계 집계 수동 실행 및 관리 API 컨트롤러")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stats")
public class StatsJobController {

    private final ViewStatsService viewStatsService;
    private final CommentStatsService commentStatsService;

    /**
     * 일별 통계 기록 (조회수, 댓글수 증가량 외부 API 연동 저장)
     * 수동 실행 시 특정 기간의 데이터를 수집하여 DB에 기록
     *
     * @return 성공 응답
     */
    @Operation(summary = "일별 통계 기록", description = "일별 통계 기록 (조회수, 댓글수 증가량 외부 API 연동 저장)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @PostMapping("/daily")
    public ResponseEntity<Response<List<String>>> collectDailyGrowthStats() {
        viewStatsService.collectDailyGrowthStats();
        return Response.success(ResponseCode.OK_SUCCESS);
    }

    /**
     * 실시간 댓글 통계 업데이트 (작성, 삭제 시 즉시 동기화)
     * 댓글 이벤트 발생 시 통계 테이블의 수치를 즉시 변경
     *
     * @param reqDto 댓글 통계 데이터
     * @return 성공 응답
     */
    @Operation(summary = "실시간 댓글 통계 업데이트", description = "실시간 댓글 통계 업데이트 (작성, 삭제 시 즉시 동기화)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @PostMapping("/comments/sync")
    public ResponseEntity<Response<String>> syncCommentStats(@RequestBody @Valid CommentStatsReqDto reqDto) {
        commentStatsService.syncCommentStats(reqDto);
        return Response.success(ResponseCode.OK_SUCCESS);
    }

    /**
     * 누적 조회수 업데이트 (1시간 주기적 합산 및 저장)
     * 배치 프로그램이 호출하여 전체 누적 조회수를 갱신
     *
     * @param reqDto 누적 조회수 데이터
     * @return 성공 응답
     */
    @Operation(summary = "누적 조회수 업데이트", description = "누적 조회수 업데이트 (1시간 주기적 합산 및 저장)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @PostMapping("/views/total")
    public ResponseEntity<Response<String>> syncTotalViews(@RequestBody(required = false) @Valid ViewStatsReqDto reqDto) {
        viewStatsService.syncTotalViews(reqDto);
        return Response.success(ResponseCode.OK_SUCCESS);
    }

    /**
     * 조회수 수집 로우 데이터 추출 (배치 처리용 원본 로그)
     * 대량의 로그 데이터를 조건에 맞게 추출하여 반환
     *
     * @param reqDto 조회수 수집 로우 데이터
     * @return 성공 응답
     */
    @Operation(summary = "조회수 수집 로우 데이터 추출", description = "조회수 수집 로우 데이터 추출 (배치 처리용 원본 로그)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @PostMapping("/views/raw")
    public ResponseEntity<Response<String>> collectViewRawLogs(@RequestBody @Valid ViewRawLogReqDto reqDto) {
        viewStatsService.collectViewRawLogs(reqDto.getStartDate(), reqDto.getEndDate());
        return Response.success(ResponseCode.OK_SUCCESS);
    }

}

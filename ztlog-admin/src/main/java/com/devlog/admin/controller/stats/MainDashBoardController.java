package com.devlog.admin.controller.stats;

import com.devlog.admin.service.stats.MainStatsService;
import com.devlog.admin.service.stats.dto.response.MainStatsResDto;
import com.devlog.core.common.enumulation.ResponseCode;
import com.devlog.core.common.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "메인(대쉬보드) 컨트롤러", description = "메인(대쉬보드) 컨트롤러")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MainDashBoardController {

    private final MainStatsService mainStatsService;

    /**
     * 메인화면(대쉬보드) 조회
     *
     * @return 메인화면(대쉬보드) 정보
     */
    @Operation(summary = "메인화면(대쉬보드) 조회", description = "메인화면(대쉬보드) 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseCode.class))),
            @ApiResponse(responseCode = "500", description = "예상치 못한 서버 에러 발생", content = @Content(schema = @Schema(implementation = ResponseCode.class)))
    })
    @GetMapping("/main")
    public ResponseEntity<Response<MainStatsResDto>> getMainStatisticsInfo() {
        return Response.success(ResponseCode.OK_SUCCESS, mainStatsService.getMainStatisticsInfo());
    }

}

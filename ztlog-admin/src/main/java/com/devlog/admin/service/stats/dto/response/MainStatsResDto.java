package com.devlog.admin.service.stats.dto.response;

import com.devlog.admin.service.stats.dto.request.MainStatsReqDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class MainStatsResDto {

    @Schema(description = "총 게시물 갯수")
    private long totalPostCount;

    @Schema(description = "총 태그 갯수")
    private long totalTagCount;

    @Schema(description = "총 조회수")
    private long totalViewCount;

    @Schema(description = "총 댓글 수")
    private long totalCommentCount;

    public static MainStatsResDto of(MainStatsReqDto statistics) {
        return MainStatsResDto.builder()
                .totalPostCount(statistics.getTotalPostCount())
                .totalTagCount(statistics.getTotalTagCount())
                .totalViewCount(statistics.getTotalViewCount())
                .totalCommentCount(statistics.getTotalCommentCount())
                .build();
    }

}
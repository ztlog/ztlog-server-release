package com.devlog.admin.service.stats.dto.request;

import lombok.Getter;

@Getter
public class MainStatsReqDto {
    private long totalPostCount;
    private long totalTagCount;
    private long totalViewCount;
    private long totalCommentCount;
}

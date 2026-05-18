package com.devlog.admin.service.stats;

import com.devlog.admin.mapper.stats.MainStatsMapper;
import com.devlog.admin.service.stats.dto.request.MainStatsReqDto;
import com.devlog.admin.service.stats.dto.response.MainStatsResDto;
import com.devlog.core.common.enumulation.ResponseCode;
import com.devlog.core.config.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MainStatsService {

    private final MainStatsMapper mainStatsMapper;

    /**
     * 메인화면 대쉬보드 통계 조회
     *
     * @return 메인화면(대쉬보드) 정보
     */
    public MainStatsResDto getMainStatisticsInfo() {
        MainStatsReqDto mainStatsReqDto = mainStatsMapper.selectMainStatistics()
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));
        return MainStatsResDto.of(mainStatsReqDto);
    }

}

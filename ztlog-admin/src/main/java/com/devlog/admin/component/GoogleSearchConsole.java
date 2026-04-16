package com.devlog.admin.component;

import com.devlog.admin.service.stats.dto.response.DailyStatsResDto;
import com.devlog.admin.service.stats.dto.request.ViewRawDataReqDto;
import com.devlog.core.common.constants.CommonConstants;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.searchconsole.v1.SearchConsole;
import com.google.api.services.searchconsole.v1.SearchConsoleScopes;
import com.google.api.services.searchconsole.v1.model.ApiDimensionFilter;
import com.google.api.services.searchconsole.v1.model.ApiDimensionFilterGroup;
import com.google.api.services.searchconsole.v1.model.SearchAnalyticsQueryRequest;
import com.google.api.services.searchconsole.v1.model.SearchAnalyticsQueryResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

@Slf4j
@Component
public class GoogleSearchConsole {

    @Value("${google.search-console.site-url}")
    private String siteUrl;

    private SearchConsole searchConsole;

    @PostConstruct
    public void init() {
        try (InputStream keyFile = getClass().getResourceAsStream("/google-search-console-key.json")) {
            if (keyFile == null) {
                log.warn("구글 서비스 키 파일을 찾을 수 없습니다. Google Search Console 기능이 비활성화됩니다.");
                return;
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(keyFile)
                    .createScoped(Collections.singleton(SearchConsoleScopes.WEBMASTERS_READONLY));

            this.searchConsole = new SearchConsole.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("ztlog.io")
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Google Search Console 클라이언트 초기화 실패", e);
        }
    }

    // 전체 페이지 일별 조회수 조회
    public List<DailyStatsResDto> fetchAllPageViews(String startDate, String endDate) {
        try {
            SearchAnalyticsQueryRequest request = new SearchAnalyticsQueryRequest()
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    .setDimensions(List.of("page"))
                    .setRowLimit(100);

            return executeQuery(searchConsole, request, endDate);

        } catch (GoogleJsonResponseException e) {
            throw new RuntimeException("Google API 응답 실패: " + e.getDetails().getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("통계 데이터 수집 중 시스템 오류 발생", e);
        }
    }

    // 특정 컨텐츠 누적 조회수 조회
    public List<DailyStatsResDto> fetchContentViews(String startDate, String endDate, Long ctntNo) {
        try {
            ApiDimensionFilterGroup filterGroup = new ApiDimensionFilterGroup()
                    .setFilters(Collections.singletonList(new ApiDimensionFilter()
                            .setDimension("page")
                            .setOperator("contains")
                            .setExpression("/contents/" + ctntNo)));

            SearchAnalyticsQueryRequest request = new SearchAnalyticsQueryRequest()
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    .setDimensions(List.of("page"))
                    .setDimensionFilterGroups(List.of(filterGroup))
                    .setRowLimit(10);

            return executeQuery(searchConsole, request, endDate);

        } catch (Exception e) {
            log.error("콘텐츠 {} 데이터 fetch 실패: {}", ctntNo, e.getMessage());
            return Collections.emptyList();
        }
    }

    // 날짜별 페이지 로우 데이터 조회 (배치 적재용)
    public List<ViewRawDataReqDto> fetchRawLogData(String startDate, String endDate) {
        try {
            SearchAnalyticsQueryRequest request = new SearchAnalyticsQueryRequest()
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    .setDimensions(List.of("date", "page"))
                    .setRowLimit(25000);

            SearchAnalyticsQueryResponse response = searchConsole.searchanalytics()
                    .query(siteUrl, request)
                    .execute();

            return Optional.ofNullable(response.getRows())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(row -> {
                        LocalDate viewDt = LocalDate.parse(row.getKeys().get(0));
                        String pageUrl = row.getKeys().get(1);
                        Matcher matcher = CommonConstants.POST_ID_PATTERN.matcher(pageUrl);
                        Long ctntNo = matcher.find() ? Long.parseLong(matcher.group(1)) : null;
                        return ViewRawDataReqDto.of(row, viewDt, pageUrl, ctntNo);
                    })
                    .filter(dto -> dto.getCtntNo() != null)
                    .toList();

        } catch (GoogleJsonResponseException e) {
            throw new RuntimeException("Google API 응답 실패: " + e.getDetails().getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("통계 데이터 수집 중 시스템 오류 발생", e);
        }
    }

    private List<DailyStatsResDto> executeQuery(SearchConsole service, SearchAnalyticsQueryRequest request, String endDate)
            throws IOException {
        SearchAnalyticsQueryResponse response = service.searchanalytics()
                .query(siteUrl, request)
                .execute();

        LocalDate statDt = LocalDate.parse(endDate);
        return Optional.ofNullable(response.getRows())
                .orElse(Collections.emptyList())
                .stream()
                .map(row -> {
                    String pageUrl = row.getKeys().isEmpty() ? "" : row.getKeys().get(0);
                    Matcher matcher = CommonConstants.POST_ID_PATTERN.matcher(pageUrl);
                    Long ctntNo = matcher.find() ? Long.parseLong(matcher.group(1)) : null;
                    return DailyStatsResDto.of(row, statDt, pageUrl, ctntNo);
                })
                .toList();
    }

}

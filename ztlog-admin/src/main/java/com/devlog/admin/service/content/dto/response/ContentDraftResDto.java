package com.devlog.admin.service.content.dto.response;

import com.devlog.admin.service.tag.dto.request.TagInfoReqDto;
import com.devlog.core.common.constants.CommonConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentDraftResDto {

    @Schema(description = "초안 번호")
    private Long draftNo;

    @Schema(description = "연결된 발행글 번호 (신규 글 초안이면 null)")
    private Long ctntNo;

    @Schema(description = "연결된 발행글의 현재 제목 (신규 글 초안이면 null)")
    private String originTitle;

    @Schema(description = "초안 제목")
    private String title;

    @Schema(description = "초안 부제목")
    private String subTitle;

    @Schema(description = "초안 내용")
    private String body;

    @Schema(description = "카테고리 번호")
    private Long cateNo;

    @Schema(description = "초안 태그 목록")
    private List<TagInfoReqDto> tags;

    @Schema(description = "초안 파일 경로")
    private String path;

    @Schema(description = "초안 파일 이름")
    private String name;

    @Schema(description = "초안 파일 확장자")
    private String ext;

    @Schema(description = "등록자", defaultValue = CommonConstants.ADMIN_NAME)
    private String inpUser;

    @Schema(description = "등록일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CommonConstants.DEFAULT_DATETIME_FORMAT, timezone = "Asia/Seoul")
    private LocalDateTime inpDttm;

    @Schema(description = "수정일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CommonConstants.DEFAULT_DATETIME_FORMAT, timezone = "Asia/Seoul")
    private LocalDateTime updDttm;

}
package com.devlog.admin.service.content.dto.request;

import com.devlog.admin.service.tag.dto.request.TagReqDto;
import com.devlog.core.common.constants.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentDraftReqDto {

    @Schema(description = "초안 번호 (수정 저장 시에만 필요)")
    private Long draftNo;

    @Schema(description = "연결된 발행글 번호 (신규 글 초안이면 null)")
    private Long ctntNo;

    @Schema(description = "초안 제목")
    @Size(max = CommonConstants.TITLE_SIZE, message = "content title length is too long!!")
    private String title;

    @Schema(description = "초안 부제목")
    @Size(max = CommonConstants.SUBTITLE_SIZE, message = "content sub-title length is too long!!")
    private String subTitle;

    @Schema(description = "초안 내용")
    private String body;

    @Schema(description = "카테고리 번호")
    private Long cateNo;

    @Schema(description = "초안 생성자", defaultValue = CommonConstants.ADMIN_NAME)
    private String inpUser;

    @Schema(description = "초안 태그 목록")
    private List<TagReqDto> tags;

    @Schema(description = "초안 파일 경로")
    private String path;

    @Schema(description = "초안 파일 이름")
    private String name;

    @Schema(description = "초안 파일 확장자")
    private String ext;

}

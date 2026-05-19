package com.devlog.core.common.enumulation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {

    /**
     *
     * Success Result Code
     *
     */

    // 200
    OK_SUCCESS(200, "성공입니다."),
    REFRESH_TOKEN_SUCCESS(200, "토큰 갱신 성공입니다."),

    // 201
    CREATED_SUCCESS(201, "생성 성공입니다."),


    /**
     *
     * Error Result Code
     *
     */

    // 400 Bad Request
    INVALID_DATA_ERROR(400, "유효하지 않은 정보입니다."),
    INVALID_REQUIRED_DATA(400, "필수 정보가 부족합니다."),
    INVALID_PASSWORD_LENGTH(400, "비밀번호 길이는 9 ~ 15 사이입니다."),
    INVALID_PASSWORD_CHAR(400, "비밀번호는 대문자, 소문자, 숫자, 특수문자가 포함되어야 합니다."),
    INVALID_DATE_FORMAT(400, "요청 날짜 형식이 잘못되었습니다."),

    // 401 UnAuthorized
    UNAUTHORIZED_USER_GRANT(401, "권한이 없습니다."),
    UNAUTHORIZED_INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    UNAUTHORIZED_EXPIRED_TOKEN(401, "토큰이 만료되었습니다. 다시 로그인 해주세요."),

    // 403 Forbidden
    FORBIDDEN(403, "접근 권한이 없습니다."),
    FORBIDDEN_FILE_TYPE(403, "허용되지 않은 파일 형식입니다."),
    FORBIDDEN_FILE_NAME(403, "허용되지 않은 파일 이름입니다."),

    // 404 Not Found
    NOT_FOUND_DATA(404, "조회된 정보가 없습니다."),
    NOT_FOUND_DELETE_DATA(404, "삭제할 정보가 없습니다."),
    NOT_FOUND_MATCH_PASSWORD(404, "비밀번호가 일치하지 않습니다."),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED_ERROR(405, "지원하지 않는 메소드 입니다."),

    // 406 Not Acceptable
    NOT_ACCEPTABLE_ERROR(406, "Not Acceptable"),

    // 409 Conflict
    CONFLICT_DATA_ERROR(409, "중복된 정보입니다."),
    CONFLICT_USER_ERROR(409, "이미 해당 계정으로 회원가입하셨습니다."),
    CONFLICT_LOGIN_ERROR(409, "이미 로그인 중인 유저입니다."),
    CONFLICT_REQUEST_ERROR(409, "처리중인 요청입니다."),

    // 413 Payload Too Large
    PAYLOAD_TOO_LARGE_ERROR(413, "파일 크기가 너무 큽니다. 최대 10MB까지 업로드 가능합니다."),

    // 415 Unsupported Media Type
    UNSUPPORTED_MEDIA_TYPE_ERROR(415, "지원하지 않는 파일 형식입니다."),

    // 500
    INTERNAL_SERVER_ERROR(500, "예상치 못한 서버 에러가 발생하였습니다."),

    // 502
    BAD_GATEWAY_ERROR(502, "일시적인 에러가 발생하였습니다.\n잠시 후 다시 시도해주세요!"),

    // 503 Service UnAvailable
    SERVICE_UNAVAILABLE_EXCEPTION(503, "현재 점검 중입니다.\n잠시 후 다시 시도해주세요!"),

    // 900
    DEFAULT_ERROR(900, "일시적인 오류입니다. 잠시후 다시 이용해 주세요."),

    /**
     *
     * batch error code
     *
     */

    // 9000
    BATCH_ERROR_9001(9001, "존재하지 않는 사용자"),
    BATCH_ERROR_9003(9003, "데이터 형식 오류"),
    BATCH_ERROR_9999(9999, "정의되지 않은 오류");

    private int status;
    private String message;

    ResponseCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

}
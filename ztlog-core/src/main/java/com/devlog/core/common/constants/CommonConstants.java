package com.devlog.core.common.constants;

import java.util.Set;
import java.util.regex.Pattern;

public class CommonConstants {

    // string
    public static final String ZTLOG = "ztlog";
    public static final String ADMIN_NAME = "admin";


    // number
    public static final int PAGE_SIZE = 5;
    public static final int PAGE_LIST_SIZE = 10;
    public static final int TAG_NAME_SIZE = 15;
    public static final int TITLE_SIZE = 100;
    public static final int SUBTITLE_SIZE = 200;
    public static final int SEARCH_PARAM_MAX_SIZE = 100;

    // file upload
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    public static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    // auth
    public static final String BEARER_TYPE = "Bearer";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_HEADER = "Refresh";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String EXPIRED = "EXPIRED_TOKEN";


    // dateformat
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_MILLISECONDS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    // 정규식 패턴 미리 컴파일 (성능 최적화)
    public static final Pattern POST_ID_PATTERN = Pattern.compile("/contents/(\\d+)");

}

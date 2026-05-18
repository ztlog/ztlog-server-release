package com.devlog.core.common.enumulation;

public enum UserStatus {

    PENDING("PENDING", "승인 대기"),
    ACTIVE("ACTIVE", "활성");

    private final String value;
    private final String desc;

    UserStatus(String v, String d) {
        value = v;
        desc = d;
    }

    public String value() {
        return value;
    }
}

package com.devlog.core.common.enumulation;

public enum ContentDraftType {

    NEW("NEW", "신규 글 초안"),
    EDIT("EDIT", "발행글 수정 초안");

    private final String value;
    private final String desc;

    ContentDraftType(String v, String d) {
        value = v;
        desc = d;
    }

    public String value() {
        return value;
    }
}
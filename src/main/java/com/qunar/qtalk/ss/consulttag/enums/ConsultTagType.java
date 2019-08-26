package com.qunar.qtalk.ss.consulttag.enums;


public enum ConsultTagType {
    UNKNOWN(0, "未知"),
    URL(1, "url"),
    SCHEME(2, "scheme"),
    SERVER_API(3, "serverApi");

    public int code;
    public String desc;

    ConsultTagType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ConsultTagType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ConsultTagType consultTagType : ConsultTagType.values()) {
            if (consultTagType.code == code) {
                return consultTagType;
            }
        }
        return null;
    }
}

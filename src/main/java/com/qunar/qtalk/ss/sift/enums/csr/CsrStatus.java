package com.qunar.qtalk.ss.sift.enums.csr;

public enum CsrStatus {
    online(1, "online"),
    offline(0, "offline");

    public String desc;
    public int code;

    CsrStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

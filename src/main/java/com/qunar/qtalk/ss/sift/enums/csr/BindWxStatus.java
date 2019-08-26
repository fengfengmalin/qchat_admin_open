package com.qunar.qtalk.ss.sift.enums.csr;

public enum BindWxStatus {
    UNBIND_WX(0, "未绑定微信"),
    BIND_WX(1, "已绑定微信");

    public int code;
    public String desc;

    BindWxStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BindWxStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (BindWxStatus bindWxStatus : BindWxStatus.values()) {
            if (bindWxStatus.code == code) {
                return bindWxStatus;
            }
        }
        return null;
    }
}

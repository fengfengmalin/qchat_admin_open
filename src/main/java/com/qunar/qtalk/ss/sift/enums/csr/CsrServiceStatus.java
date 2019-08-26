package com.qunar.qtalk.ss.sift.enums.csr;

public enum CsrServiceStatus {
    STANDARD_MODE(0, "标准模式"),
    DND_MODE(1, "勿扰模式"),
    SUPER_MODE(4, "超人模式"),
    UNKNOW_MODE(99, "未知模式");

    public int code;
    public String desc;

    CsrServiceStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}

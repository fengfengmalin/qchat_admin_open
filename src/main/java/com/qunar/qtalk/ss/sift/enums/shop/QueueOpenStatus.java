package com.qunar.qtalk.ss.sift.enums.shop;

public enum QueueOpenStatus {
    open(1, "开启"),
    close(0, "关闭");

    public int code;
    public String desc;

    QueueOpenStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

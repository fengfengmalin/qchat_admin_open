package com.qunar.qtalk.ss.sift.enums.shop;

public enum ShopStatus {
    online(1, "online"),
    offline(0, "offline");

    public String desc;
    public int code;

    ShopStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

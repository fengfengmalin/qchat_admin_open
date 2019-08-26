package com.qunar.qtalk.ss.sift.enums.shop;

public enum AssignStragegy {
    POLLING(1, "轮询"),
    IDLE(2, "最闲优先"),
    RANDOM(3, "随机分配");

    public int code;
    public String desc;

    AssignStragegy (int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AssignStragegy of(Integer code) {
        if (code == null) {
            return null;
        }
        for (AssignStragegy assignStragegy : AssignStragegy.values()) {
            if (assignStragegy.code == code) {
                return assignStragegy;
            }
        }
        return null;
    }
}

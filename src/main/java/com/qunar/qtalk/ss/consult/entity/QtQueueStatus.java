package com.qunar.qtalk.ss.consult.entity;

import com.qunar.qtalk.ss.utils.CustomRuntimeException;

public enum QtQueueStatus {
    LINEUP(1, "lineup"),//排队中
    INSERVICE(2, "in service"),//服务中
    CustomerLast(3, "客人最后一句"),
    SeatLast(4, "坐席最后一句"),
    SeatReleased(5, "坐席已释放"),
    Unknown(-1, "unknown");

    public String desc;
    public int code;

    public int getCode() {
        return code;
    }

    QtQueueStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static QtQueueStatus valueOf(int code) {
        for (QtQueueStatus e : QtQueueStatus.values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new CustomRuntimeException("QtQueueStatus 不支持这种类型,【code: " + code + "】");
    }

    public static void main(String[] args) {
//        QtQueueStatus status = QtQueueStatus.valueOf(null);
//
//        int i = 10;


    }
}

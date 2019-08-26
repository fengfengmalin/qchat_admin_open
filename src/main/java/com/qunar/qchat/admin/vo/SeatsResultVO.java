package com.qunar.qchat.admin.vo;

/**
 * Author : mingxing.shao
 * Date : 15-10-26
 *
 */
public class SeatsResultVO<T> {
    private long time;
    private String businessName;
    private T data;

    public SeatsResultVO() {

    }

    public SeatsResultVO(long time,String businessName, T data) {
        this.time = time;
        this.businessName = businessName;
        this.data = data;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

package com.qunar.qchat.admin.model;

/**
 * 业务-客服关系.
 * Created by qyhw on 10/16/15.
 */
public class BusiSeatMapping {

    private long id;
    private int busiId;
    private long seatId;

    private String busiName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getBusiId() {
        return busiId;
    }

    public void setBusiId(int busiId) {
        this.busiId = busiId;
    }

    public String getBusiName() {
        return busiName;
    }

    public void setBusiName(String busiName) {
        this.busiName = busiName;
    }

    public long getSeatId() {
        return seatId;
    }

    public void setSeatId(long seatId) {
        this.seatId = seatId;
    }
}

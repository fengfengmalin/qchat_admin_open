package com.qunar.qchat.admin.model;

/**
 * 组客服关系
 * Created by qyhw on 10/16/15.
 */
public class SeatGroupMapping {

    private long id;
    private long seatId;
    private int groupId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSeatId() {
        return seatId;
    }

    public void setSeatId(long seatId) {
        this.seatId = seatId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
}

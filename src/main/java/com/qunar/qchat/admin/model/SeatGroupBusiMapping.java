package com.qunar.qchat.admin.model;

/**
 * 组客服关系
 * Created by qyhw on 10/23/15.
 */
public class SeatGroupBusiMapping {

    private long seatId;
    private int groupId;
    private String groupName;
    private int busiId;

    public long getSeatId() {
        return seatId;
    }

    public void setSeatId(long seatId) {
        this.seatId = seatId;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getBusiId() {
        return busiId;
    }

    public void setBusiId(int busiId) {
        this.busiId = busiId;
    }
}

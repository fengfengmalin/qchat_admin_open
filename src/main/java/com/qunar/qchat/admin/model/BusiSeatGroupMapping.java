package com.qunar.qchat.admin.model;

/**
 * 业务-客服组关系.
 * Created by qyhw on 10/16/15.
 */
public class BusiSeatGroupMapping {

    private long id;
    private int busiId;
    private String busiName;
    private int groupId;

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

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
}

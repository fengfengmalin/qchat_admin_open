package com.qunar.qchat.admin.model;

/**
 * 组和商品关联关系
 * Created by qyhw on 5/11/16.
 */
public class GroupProductMapping {

    private int id;
    private int groupId;
    private String pid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}

package com.qunar.qchat.admin.vo;

import com.qunar.qchat.admin.model.SeatGroup;

import java.util.List;

/**
 * Created by qyhw on 1/19/16.
 */
public class SupplierGroupVO {

    private int suId;

    private String suName;

    private List<SeatGroup> groupList;

    public int getSuId() {
        return suId;
    }

    public void setSuId(int suId) {
        this.suId = suId;
    }

    public String getSuName() {
        return suName;
    }

    public void setSuName(String suName) {
        this.suName = suName;
    }

    public List<SeatGroup> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<SeatGroup> groupList) {
        this.groupList = groupList;
    }
}

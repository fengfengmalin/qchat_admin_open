package com.qunar.qchat.admin.util;

import java.util.List;

/**
 * Created by qyhw on 10/26/15.
 */
public class GroupQueryFilter {

    private String groupName;

    private int busiId;

    private long supplierId;
    private List<Long> suIdList;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getBusiId() {
        return busiId;
    }

    public void setBusiId(int busiId) {
        this.busiId = busiId;
    }

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }

    public List<Long> getSuIdList() {
        return suIdList;
    }

    public void setSuIdList(List<Long> suIdList) {
        this.suIdList = suIdList;
    }
}

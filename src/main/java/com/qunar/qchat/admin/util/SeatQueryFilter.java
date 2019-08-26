package com.qunar.qchat.admin.util;

import java.util.List;

/**
 * Created by qyhw on 10/23/15.
 */
public class SeatQueryFilter {

    private String qunarName;

    private String webName;

    private int busiId;

    private long supplierId;
    private List<Long> suIdList;

    private String bySort;

    public String getQunarName() {
        return qunarName;
    }

    public void setQunarName(String qunarName) {
        this.qunarName = qunarName;
    }

    public String getWebName() {
        return webName;
    }

    public void setWebName(String webName) {
        this.webName = webName;
    }

    public int getBusiId() {
        return busiId;
    }

    public void setBusiId(int busiId) {
        this.busiId = busiId;
    }

    public List<Long> getSuIdList() {
        return suIdList;
    }

    public void setSuIdList(List<Long> suIdList) {
        this.suIdList = suIdList;
    }

    public String getBySort() {
        return bySort;
    }

    public void setBySort(String bySort) {
        this.bySort = bySort;
    }

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }
}

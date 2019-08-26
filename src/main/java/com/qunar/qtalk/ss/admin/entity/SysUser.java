package com.qunar.qtalk.ss.admin.entity;

import java.util.Date;

public class SysUser {
    private long id;
    private String qunarName;
    private long supplierID;
    private Date createTime;
    private Date lastUpdateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getQunarName() {
        return qunarName;
    }

    public void setQunarName(String qunarName) {
        this.qunarName = qunarName;
    }

    public long getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(long supplierID) {
        this.supplierID = supplierID;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}

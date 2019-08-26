package com.qunar.qtalk.ss.sift.entity;

import java.util.Date;

public class BusiShopMapping {
    private long id;
    //客服的系统的商铺ID
    private long shopID;
    //业务线ID
    private int busiID;
    //业务线自己的商铺ID
    private String busiSupplierID;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //已经无用字段 为了跟数据库保持一致
    private String bsuIDType;
    // 操作员id 例如helen.liu@ejabhost1
    private String supplierOperator;
    // 操作员中文名
    private String operatorWebname;
    // 状态， 这个状态其实可以不用，这是关系表
    private int status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getShopID() {
        return shopID;
    }

    public void setShopID(long shopID) {
        this.shopID = shopID;
    }

    public int getBusiID() {
        return busiID;
    }

    public void setBusiID(int busiID) {
        this.busiID = busiID;
    }

    public String getBusiSupplierID() {
        return busiSupplierID;
    }

    public void setBusiSupplierID(String busiSupplierID) {
        this.busiSupplierID = busiSupplierID;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getBsuIDType() {
        return bsuIDType;
    }

    public void setBsuIDType(String bsuIDType) {
        this.bsuIDType = bsuIDType;
    }

    public String getSupplierOperator() {
        return supplierOperator;
    }

    public void setSupplierOperator(String supplierOperator) {
        this.supplierOperator = supplierOperator;
    }

    public String getOperatorWebname() {
        return operatorWebname;
    }

    public void setOperatorWebname(String operatorWebname) {
        this.operatorWebname = operatorWebname;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

package com.qunar.qtalk.ss.sift.entity;

import java.util.Date;

public class SeatGroup {
    private long id;
    private String name;
    //该字段已经不用了
    private int strategy;
    private long shopID;
    private Date createTime;
    private Date updateTime;
    //该字段已经不用了
    private int oldSupplierID;
    //该字段已经不用了
    private int oldGroupID;
    //该字段已经不用了
    private String oldKefus;
    //该字段已经不用了
    private String groupType;

    //1为默认分组 0不为默认分组
    private int defaultValue;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStrategy() {
        return strategy;
    }

    public void setStrategy(int strategy) {
        this.strategy = strategy;
    }

    public long getShopID() {
        return shopID;
    }

    public void setShopID(long shopID) {
        this.shopID = shopID;
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

    public int getOldSupplierID() {
        return oldSupplierID;
    }

    public void setOldSupplierID(int oldSupplierID) {
        this.oldSupplierID = oldSupplierID;
    }

    public int getOldGroupID() {
        return oldGroupID;
    }

    public void setOldGroupID(int oldGroupID) {
        this.oldGroupID = oldGroupID;
    }

    public String getOldKefus() {
        return oldKefus;
    }

    public void setOldKefus(String oldKefus) {
        this.oldKefus = oldKefus;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public int getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
    }
}

package com.qunar.qtalk.ss.sift.entity;

import java.util.Date;

public class Busi {
    //business表的ID
    private long id;
    //业务线中文名称
    private String name;
    //业务线创建时间
    private Date createTime;
    //业务线更新时间
    private Date updateTime;
    //英文名称，这个一般用来对应qconfig的表
    private String englishName;

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

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }
}

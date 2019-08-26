package com.qunar.qtalk.ss.sift.entity;

import com.qunar.qtalk.ss.utils.JID;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

public class RobotShopRelation {

    private long id;
    private String robotID;
    private long shopID;
    private int strategy;
    private String welcome;
    private Date createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRobotID() { return robotID; }

    public void setRobotID(String robotID) {
        this.robotID = robotID;
    }
    public long getShopID() {
        return shopID;
    }

    public void setShopID(long shopID) {
        this.shopID = shopID;
    }

    public int getStrategy() {
        return strategy;
    }

    public void setStrategy(int strategy) {
        this.strategy = strategy;
    }

    public String getWelcome() {
        return welcome;
    }

    public void setWelcome(String welcome) {
        this.welcome = welcome;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

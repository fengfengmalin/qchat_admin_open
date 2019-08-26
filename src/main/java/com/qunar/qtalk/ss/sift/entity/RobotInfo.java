package com.qunar.qtalk.ss.sift.entity;

import com.qunar.qtalk.ss.utils.JID;
import org.apache.commons.lang.StringUtils;
import java.util.Date;

public class RobotInfo {

    //robot_info的id
    private long id;
    //机器人的名字
    private String robotID;
    //业务线ID
    private long busiID;
    //机器人名称
    private String robotName;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //操作者
    private String operator;
    //状态
    private int status;
    //头像图片url
    private String imgUrl;

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


    public long getBusiID() {
        return busiID;
    }

    public void setBusiID(long busiID) {
        this.busiID = busiID;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
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

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}

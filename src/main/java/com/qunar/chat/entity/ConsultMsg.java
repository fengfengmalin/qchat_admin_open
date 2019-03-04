package com.qunar.chat.entity;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class ConsultMsg {
    private Long id;
    private String mFrom;
    private String mTo;
    private String mBody;
    private Date createTime;
    private Integer readFlag;
    private String msgId;
    private String fromHost;
    private String toHost;
    private String realFrom;
    private String realTo;
    private String msgType;
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getmFrom() {
        return mFrom;
    }

    public void setmFrom(String mFrom) {
        this.mFrom = StringUtils.trimToEmpty(mFrom);
    }

    public String getmTo() {
        return mTo;
    }

    public void setmTo(String mTo) {
        this.mTo = StringUtils.trimToEmpty(mTo);
    }

    public String getmBody() {
        return mBody;
    }

    public void setmBody(String mBody) {
        this.mBody = StringUtils.trimToEmpty(mBody);
    }


    public Integer getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(Integer readFlag) {
        this.readFlag = readFlag;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = StringUtils.trimToEmpty(msgId);
    }

    public String getFromHost() {
        return fromHost;
    }

    public void setFromHost(String fromHost) {
        this.fromHost = StringUtils.trimToEmpty(fromHost);
    }

    public String getToHost() {
        return toHost;
    }

    public void setToHost(String toHost) {
        this.toHost = StringUtils.trimToEmpty(toHost);
    }

    public String getRealFrom() {
        return realFrom;
    }

    public void setRealFrom(String realFrom) {
        this.realFrom = StringUtils.trimToEmpty(realFrom);
    }

    public String getRealTo() {
        return realTo;
    }

    public void setRealTo(String realTo) {
        this.realTo = StringUtils.trimToEmpty(realTo);
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = StringUtils.trimToEmpty(msgType);
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
}

package com.qunar.qchat.admin.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 会话
 * Created by qyhw on 12/2/15.
 */
public class SessionVO {

    private long id;

    private String visitorName;

    private String seatName;

    private long startTime;

    private long endTime;

    private int busiType;

    private String busiName;

    private String ip = "";
    private String city = "";
    private String province = "";

    private int msgNum;

    private int status; //  1 正常  2 漏答



    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("sessionId")
    public void setId(long id) {
        this.id = id;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getBusiType() {
        return busiType;
    }

    public void setBusiType(int busiType) {
        this.busiType = busiType;
    }

    public String getBusiName() {
        return busiName;
    }

    public void setBusiName(String busiName) {
        this.busiName = busiName;
    }

    @JsonProperty("ip")
    public String getIp() {
        return ip;
    }

    @JsonProperty("ipAddr")
    public void setIp(String ip) {
        this.ip = ip;
    }

    @JsonProperty("msgNum")
    public int getMsgNum() {
        return msgNum;
    }

    @JsonProperty("sessionNum")
    public void setMsgNum(int msgNum) {
        this.msgNum = msgNum;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}

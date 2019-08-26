package com.qunar.qchat.admin.service.query;

import com.qunar.qchat.admin.util.CovertToStringUtil;

import java.util.List;

/**
 * Created by qyhw on 12/2/15.
 */
public class SessionQueryFilter {

    private String visitorName;

    private String seatName;

    private long startTime;

    private long endTime;

    private int busiType;

    private int msgNum;

    private int status; //  1 正常  2 漏答

    private long supplierId;
    private List<Long> suIdList;

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

    public int getMsgNum() {
        return msgNum;
    }

    public void setMsgNum(int msgNum) {
        this.msgNum = msgNum;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String toString() {
        return CovertToStringUtil.convertClassAttrToString(this);
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

package com.qunar.qchat.admin.vo;

import java.util.List;

/**
 * 客服
 * Created by qyhw on 10/21/15.
 */
public class SeatBaseVO {
    private long id;
    private String qunarName;  // 用户中心登陆用户名
    private String webName;
    private String nickName;
    private String faceLink;
    private int priority;
    private long supplierId;
    private int businessId;
    private List<Long> suIdList;
    private String supplierName;
    private long createTime;
    private int serviceStatus;
    private int maxSessions;
    private boolean bindWx;
    private String host;

    public SeatBaseVO() {

    }

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

    public String getWebName() {
        return webName;
    }

    public void setWebName(String webName) {
        this.webName = webName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getFaceLink() {
        return faceLink;
    }

    public void setFaceLink(String faceLink) {
        this.faceLink = faceLink;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(int serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public int getBusinessId() {
        return businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    public int getMaxSessions(){
        return this.maxSessions;
    }

    public void setMaxSessions(int maxSessions){
        this.maxSessions = maxSessions;
    }

    public boolean isBindWx() {
        return bindWx;
    }

    public void setBindWx(boolean bindWx) {
        this.bindWx = bindWx;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}

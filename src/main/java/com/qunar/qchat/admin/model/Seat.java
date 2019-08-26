package com.qunar.qchat.admin.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qunar.qchat.admin.vo.OnlineStateJsonAdapter;
import lombok.Data;

import java.util.Date;

/**
 * 客服
 */
@Data
public class Seat {

    private Long id;
    private String qunarName;  // 用户中心登陆用户名
    private String webName; //展示名称
    private String nickName;
    private String faceLink;
    private Date createTime;

    private Integer priority;
    // 商铺ID
    private Long supplierId;
    // 产品ID
    private Integer businessId;
    private String supplierName;
    private boolean isrobot;

    private Integer serviceStatus; // 服务状态
    private String pid;

    private String customerName;  //顾客名称

    private OnlineState onlineState;    //  原始的在线信息

    private Integer maxSessions;// 最大接收多少个会话数
    private Integer curSessions; // 目前有个多少个会话数
    // 客服是否'绑定微信，0：未绑定，1：已绑定';
    private int bindWx;
    // 客服所对应的域
    private String host;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Integer getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(Integer serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public boolean isIsrobot() {
        return isrobot;
    }

    public void setIsrobot(boolean isrobot) {
        this.isrobot = isrobot;
    }

    @JsonSerialize(using = OnlineStateJsonAdapter.Serializer.class)
    public OnlineState getOnlineState() {
        return onlineState;
    }

    @JsonDeserialize(using = OnlineStateJsonAdapter.Deserializer.class)
    public void setOnlineState(OnlineState onlineState) {
        this.onlineState = onlineState;
    }

    public int getBindWx() {
        return bindWx;
    }

    public void setBindWx(int bindWx) {
        this.bindWx = bindWx;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}

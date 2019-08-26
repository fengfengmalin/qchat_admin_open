package com.qunar.qtalk.ss.sift.entity;

import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qtalk.ss.utils.JID;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * 客服
 * 该类对应seat表中的字段
 */
public class CSR {
    //客服表中的ID
    private Long id;
    // qunar用户中心的ID
    private String qunarName;
    private JID realQunarName;

    // qunar的外显名称
    private String webName;
    // 暂时没用到
    private String nickName;
    // 本意是头像链接 目前没用到，头像图片取的用户中心的头像
    private String faceLink;
    // 店铺ID
    private Long supplierID;
    // 优先级
    private Integer priority;
    // 客服创建时间
    private Date createTime;
    // 客服更新时间
    private Date updateTime;
    // 用不到了，但是为了跟数据库中的字段对应上，就加上了
    private Long oldSupplierID;
    // 用不到了，但是为了跟数据库中的字段对应上，就加上了
    private Long oldID;
    // 服务状态 客服的服务状态, 0:未设置  4:服务中  1:休息中
    private Integer serviceStatus;
    // 客服是否被删除 0为被删除，1为可用客服
    private Integer status;
    // 客服的最大服务数量
    private Integer maxServiceCount;
    // 客服所对应的客服组id
    private Long groupId;

    private String host;

    // 客服是否'绑定微信，0：未绑定，1：已绑定';
    private int bindWx;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JID getQunarName() {
        return realQunarName;
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

    public Long getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(Long supplierID) {
        this.supplierID = supplierID;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
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

    public Long getOldSupplierID() {
        return oldSupplierID;
    }

    public void setOldSupplierID(Long oldSupplierID) {
        this.oldSupplierID = oldSupplierID;
    }

    public Long getOldID() {
        return oldID;
    }

    public void setOldID(Long oldID) {
        this.oldID = oldID;
    }

    public Integer getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(Integer serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getMaxServiceCount() {
        return maxServiceCount;
    }

    public void setMaxServiceCount(Integer maxServiceCount) {
        this.maxServiceCount = maxServiceCount;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
        host = StringUtils.isNotEmpty(host) ? host :  QChatConstant.DEFAULT_HOST;
        if (StringUtils.isNotEmpty(qunarName)) {
            if (!qunarName.contains("@")) {
                qunarName = String.format("%s@%s", qunarName, host);
            }
            realQunarName = JID.parseAsJID(qunarName);
        }
    }

    public int getBindWx() {
        return bindWx;
    }

    public void setBindWx(int bindWx) {
        this.bindWx = bindWx;
    }
}

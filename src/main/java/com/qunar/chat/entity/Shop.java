package com.qunar.chat.entity;


import com.qunar.chat.common.util.JID;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * 对应数据库表中的supplier
 */
public class Shop {
    //supplier的ID字段
    private Long id;
    //商铺的中文名称
    private String name;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //无用字段
    private Long oldID;
    //无用字段，其实是想存商铺的logo图片的
    private String logoURL;
    //该商铺的欢迎语
    private String welcomes;
    //该商铺的状态：1为在线，0为下线
    private Integer status;
    //该店铺是否启用排队：1为启用排队，0为未启用排队
    private Integer openQueueStatus;
    //该店铺的分配策略
    private Integer assignStrategy;
    //没有客服情况下欢迎语
    private String noServiceWelcomes;

    private String hotline;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Long getOldID() {
        return oldID;
    }

    public void setOldID(Long oldID) {
        this.oldID = oldID;
    }

    public String getLogoURL() {
        return logoURL;
    }

    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    public String getWelcomes() {
        return welcomes;
    }

    public void setWelcomes(String welcomes) {
        this.welcomes = welcomes;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getOpenQueueStatus() {
        return openQueueStatus;
    }

    public void setOpenQueueStatus(Integer openQueueStatus) {
        this.openQueueStatus = openQueueStatus;
    }

    public Integer getAssignStrategy() {
        return assignStrategy;
    }

    public void setAssignStrategy(Integer assignStrategy) {
        this.assignStrategy = assignStrategy;
    }

    public String getNoServiceWelcomes() {
        return noServiceWelcomes;
    }

    public void setNoServiceWelcomes(String noServiceWelcomes) {
        this.noServiceWelcomes = noServiceWelcomes;
    }

    public String getHotline() {
        return hotline;
    }

    public void setHotline(String hotline) {
        this.hotline = hotline;
    }


    public static JID parseLong(long shopId, String host) {
        return JID.parseAsJID(String.format("shop_%d@%s", shopId, host));
    }

    public static long parseJIDToShopId(JID shopId) {
        if (shopId == null)
            return -1;

        String node = shopId.getNode();
        if (StringUtils.isNumeric(node)) {
            return Long.parseLong(node);
        } else if (node.startsWith("shop_")) {
            String tmp = node.replace("shop_", "");

            if (StringUtils.isNumeric(tmp))
                return Long.parseLong(tmp);
        }
        return -1;
    }
}

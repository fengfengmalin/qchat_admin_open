package com.qunar.qchat.admin.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 供应商
 * Created by qyhw on 10/16/15.
 */
@Data
public class Supplier implements Serializable {

    public static final String SHOPID_PREFIX = "shop_";
    public static final String SHOP = "shop";
    public static final String BNB = "bnb";//途家
    public static final String ROBOT = "robot";
    public static final String HOTEL_PRE_SALE = "hotel_pre_sale";

    private long id;

    private String name;

    private int bType;

    private String welcomes;

    private String shopId;

    private String logoUrl;

    private String busiSupplierId;

    private String busiName; // bType转换成name

    //1为开启排队 0为未开启排队
    private int bQueue;

    //1为可使用状态 0为下线
    private int status;

    private Date createDate;

    private int assignStrategy;

    private String noServiceWelcomes;

    private String hotline;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getbType() {
        return bType;
    }

    public void setbType(int bType) {
        this.bType = bType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWelcomes() {
        return welcomes;
    }

    public void setWelcomes(String welcomes) {
        this.welcomes = welcomes;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getBusiSupplierId() {
        return busiSupplierId;
    }

    public void setBusiSupplierId(String busiSupplierId) {
        this.busiSupplierId = busiSupplierId;
    }

    public String getBusiName() {
        return busiName;
    }

    public void setBusiName(String busiName) {
        this.busiName = busiName;
    }

    public int getBQueue() {
        return bQueue;
    }

    public void setBQueue(int bQueue) {
        this.bQueue = bQueue;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getNoServiceWelcomes() {
        return noServiceWelcomes;
    }

    public void setNoServiceWelcomes(String noServiceWelcomes) {
        this.noServiceWelcomes = noServiceWelcomes;
    }

    public int getAssignStrategy() {
        return assignStrategy;
    }

    public void setAssignStrategy(int assignStrategy) {
        this.assignStrategy = assignStrategy;
    }

    public String getHotline() {
        return hotline;
    }

    public void setHotline(String hotline) {
        this.hotline = hotline;
    }
}

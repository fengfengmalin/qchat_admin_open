package com.qunar.qchat.admin.vo;

import com.qunar.qchat.admin.model.Business;
import com.qunar.qchat.admin.model.GroupProductMapping;
import com.qunar.qchat.admin.model.Seat;

import java.util.List;

/**
 * 客服组
 * Created by qyhw on 10/26/15.
 */
public class SeatGroupVO{

    private int id;
    private String groupName;

    private long supplierId;
    private String supplierName;
    private List<Long> suIdList;
    private List<Business> busiList;  // 所属业务
    private List<Seat> seatList;  // 包含客服
    private List<String> productList;  // 关联产品编号
    private int defaultValue;
    private long createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public List<Long> getSuIdList() {
        return suIdList;
    }

    public void setSuIdList(List<Long> suIdList) {
        this.suIdList = suIdList;
    }

    public List<Business> getBusiList() {
        return busiList;
    }

    public void setBusiList(List<Business> busiList) {
        this.busiList = busiList;
    }

    public List<Seat> getSeatList() {
        return seatList;
    }

    public void setSeatList(List<Seat> seatList) {
        this.seatList = seatList;
    }

    public List<String> getProductList() {
        return productList;
    }

    public void setProductList(List<String> productList) {
        this.productList = productList;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
    }
}

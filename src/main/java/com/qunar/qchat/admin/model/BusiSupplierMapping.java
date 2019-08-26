package com.qunar.qchat.admin.model;

/**
 * 业务-供应商关系.
 * Created by qyhw on 10/16/15.
 */
public class BusiSupplierMapping {

    private long id;
    private long supplierId;
    private String busiSupplierId;  // 来自各个业务的供应商编号
    private int busiId;
    private String bSuIdAndType;  //  busiSupplierId +　busiId

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }

    public String getBusiSupplierId() {
        return busiSupplierId;
    }

    public void setBusiSupplierId(String busiSupplierId) {
        this.busiSupplierId = busiSupplierId;
    }

    public int getBusiId() {
        return busiId;
    }

    public void setBusiId(int busiId) {
        this.busiId = busiId;
    }

    public String getbSuIdAndType() {
        return bSuIdAndType;
    }

    public void setbSuIdAndType(String bSuIdAndType) {
        this.bSuIdAndType = bSuIdAndType;
    }
}

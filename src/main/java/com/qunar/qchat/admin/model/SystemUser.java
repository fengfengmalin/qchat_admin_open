package com.qunar.qchat.admin.model;

/**
 * 系统用户.
 * Created by qyhw on 10/16/15.
 */
public class SystemUser {

    private long id;
    private String qunarName;
    private long supplierId;

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

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }
}

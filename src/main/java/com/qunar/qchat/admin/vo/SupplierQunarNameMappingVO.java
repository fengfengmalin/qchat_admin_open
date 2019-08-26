package com.qunar.qchat.admin.vo;

/**
 * Author : mingxing.shao
 * Date : 15-10-30
 *
 */
public class SupplierQunarNameMappingVO {
    private String busiSupplierId;
    private String seatQunarName;

    public SupplierQunarNameMappingVO() {

    }

    public SupplierQunarNameMappingVO(String busiSupplierId, String seatQunarName) {
        this.busiSupplierId = busiSupplierId;
        this.seatQunarName = seatQunarName;
    }

    public String getBusiSupplierId() {
        return busiSupplierId;
    }

    public void setBusiSupplierId(String busiSupplierId) {
        this.busiSupplierId = busiSupplierId;
    }

    public String getSeatQunarName() {
        return seatQunarName;
    }

    public void setSeatQunarName(String seatQunarName) {
        this.seatQunarName = seatQunarName;
    }
}

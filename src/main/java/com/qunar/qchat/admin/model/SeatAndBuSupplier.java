package com.qunar.qchat.admin.model;

/**
 * Created by qyhw on 1/11/16.
 */
public class SeatAndBuSupplier {

    private long seatId;

    private String seatName;

    private int bType;

    private String bSuId;

    private String bSuIdAndTypeId;

    private long supplierId;

    private String supplierName;

    private String supplierLogo;

    public String getSupplierLogo() {
        return supplierLogo;
    }

    public void setSupplierLogo(String supplierLogo) {
        this.supplierLogo = supplierLogo;
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

    public long getSeatId() {
        return seatId;
    }

    public void setSeatId(long seatId) {
        this.seatId = seatId;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public int getbType() {
        return bType;
    }

    public void setbType(int bType) {
        this.bType = bType;
    }

    public String getbSuId() {
        return bSuId;
    }

    public void setbSuId(String bSuId) {
        this.bSuId = bSuId;
    }

    public String getbSuIdAndTypeId() {
        return bSuIdAndTypeId;
    }

    public void setbSuIdAndTypeId(String bSuIdAndTypeId) {
        this.bSuIdAndTypeId = bSuIdAndTypeId;
    }
}

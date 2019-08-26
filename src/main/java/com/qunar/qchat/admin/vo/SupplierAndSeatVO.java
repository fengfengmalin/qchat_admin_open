package com.qunar.qchat.admin.vo;

import com.qunar.qchat.admin.model.SeatSortStrategyEnum;

import java.util.List;

/**
 * Created by qyhw on 1/11/16.
 */
public class SupplierAndSeatVO implements Cloneable{

    private String bSuId;
    private int bType;
    private long suId;
    private String pId;
    private SeatSortStrategyEnum strategy;
    private List<SeatWithStateVO> seatWithStateVOList;

    private String sName;
    private String shopId;
    private String logoUrl;

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getbSuId() {
        return bSuId;
    }

    public void setbSuId(String bSuId) {
        this.bSuId = bSuId;
    }

    public int getbType() {
        return bType;
    }

    public void setbType(int bType) {
        this.bType = bType;
    }

    public long getSuId() {
        return suId;
    }

    public void setSuId(long suId) {
        this.suId = suId;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public SeatSortStrategyEnum getStrategy() {
        return strategy;
    }

    public void setStrategy(SeatSortStrategyEnum strategy) {
        this.strategy = strategy;
    }

    public List<SeatWithStateVO> getSeatWithStateVOList() {
        return seatWithStateVOList;
    }

    public void setSeatWithStateVOList(List<SeatWithStateVO> seatWithStateVOList) {
        this.seatWithStateVOList = seatWithStateVOList;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

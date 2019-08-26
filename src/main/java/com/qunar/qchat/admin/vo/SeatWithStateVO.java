package com.qunar.qchat.admin.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qunar.qchat.admin.model.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Author : mingxing.shao
 * Date : 15-10-16
 *
 */
public class SeatWithStateVO {

    @Getter @Setter
    private boolean switchOn;
    @Getter @Setter
    private Supplier supplier;
    @Getter @Setter
    private Seat seat;
    @Getter @Setter
    private Session session;
    private OnlineState onlineState;    // 在线信息可能会被服务状态修改

    @Getter @Setter
    private Date lastStartTime;


    public SeatWithStateVO() {

    }

    public SeatWithStateVO(SeatAndGroup seatAndGroup, OnlineState onlineState) {
        Seat seat = new Seat();
        seat.setId(seatAndGroup.getId());
        seat.setQunarName(seatAndGroup.getQunarName());
        seat.setWebName(seatAndGroup.getWebName());
        seat.setNickName(seatAndGroup.getNickName());
        seat.setFaceLink(seatAndGroup.getFaceLink());
        seat.setPriority(seatAndGroup.getPriority());
        seat.setSupplierId(seatAndGroup.getSupplierId());
        seat.setCustomerName(seatAndGroup.getCustomerName());
        seat.setMaxSessions(seatAndGroup.getMaxSessions());
        this.seat = seat;
        this.onlineState = onlineState;
        switchOn = false;
    }

    public SeatWithStateVO(SeatAndGroup seatAndGroup, OnlineState onlineState, boolean switchOn) {
        Seat seat = new Seat();
        seat.setId(seatAndGroup.getId());
        seat.setQunarName(seatAndGroup.getQunarName());
        seat.setWebName(seatAndGroup.getWebName());
        seat.setNickName(seatAndGroup.getNickName());
        seat.setFaceLink(seatAndGroup.getFaceLink());
        seat.setPriority(seatAndGroup.getPriority());
        seat.setSupplierId(seatAndGroup.getSupplierId());
        seat.setCustomerName(seatAndGroup.getCustomerName());
        seat.setServiceStatus(seatAndGroup.getServiceState());
        seat.setMaxSessions(seatAndGroup.getMaxSessions());
        this.seat = seat;
        this.onlineState = onlineState;
        this.switchOn = switchOn;
        if (switchOn) {
            Supplier supplier = new Supplier();
            if (seatAndGroup.getSupplierId() != null) {
                supplier.setId(seatAndGroup.getSupplierId());
                supplier.setName(seatAndGroup.getSupplierName());
                supplier.setShopId(Supplier.SHOPID_PREFIX + supplier.getId());
                supplier.setLogoUrl(seatAndGroup.getSupplierLogo());
                supplier.setBQueue(seatAndGroup.getSupplierExtFlag());
            }
            this.supplier = supplier;
        }
    }



    @JsonSerialize(using = OnlineStateJsonAdapter.Serializer.class)
    public OnlineState getOnlineState() {
        return onlineState;
    }

    @JsonDeserialize(using = OnlineStateJsonAdapter.Deserializer.class)
    public void setOnlineState(OnlineState onlineState) {
        this.onlineState = onlineState;
    }
}

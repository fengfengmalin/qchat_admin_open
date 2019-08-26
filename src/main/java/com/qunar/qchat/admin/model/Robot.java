package com.qunar.qchat.admin.model;

import lombok.Data;

import java.util.Date;

/**
 * Created by yinmengwang on 17-8-17.
 */
@Data
public class Robot {

    private long id;
    private String robotId;
    private String robotName;
    private int businessId;
    private Date createTime;
    private Date updateTime;
    private String operator;
    private int status;
    private int displayType;
    private String imageurl;

    public Seat toSeat(long supplierId) {
        if (this.status == 0) {
            return null;
        }
        Seat seat = new Seat();
        seat.setId(this.id);
        seat.setQunarName(this.robotId);
        seat.setWebName(this.robotName);
        seat.setBusinessId(this.businessId);
        seat.setSupplierId(supplierId);
        seat.setServiceStatus(ServiceStatusEnum.SUPER_MODE.getKey());
        seat.setOnlineState(OnlineState.ONLINE);
        seat.setIsrobot(true);
        return seat;
    }

    public Seat toSeatWithoutSupplierId() {
        if (this.status == 0) {
            return null;
        }
        Seat seat = new Seat();
        seat.setId(this.id);
        seat.setQunarName(this.robotId);
        seat.setWebName(this.robotName);
        seat.setBusinessId(this.businessId);
        seat.setServiceStatus(ServiceStatusEnum.SUPER_MODE.getKey());
        return seat;
    }
}

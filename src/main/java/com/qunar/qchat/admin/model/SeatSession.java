package com.qunar.qchat.admin.model;

import java.util.Date;

/**
 * Author : mingxing.shao
 * Date : 15-11-9
 *
 */
public class SeatSession {
    private Long id;
    private Long seatId;
    private Date lastStartTime;

    public SeatSession() {

    }

    public SeatSession(long seatId, Date lastStartTime) {
        this.seatId = seatId;
        this.lastStartTime = lastStartTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public Date getLastStartTime() {
        return lastStartTime;
    }

    public void setLastStartTime(Date lastStartTime) {
        this.lastStartTime = lastStartTime;
    }
}

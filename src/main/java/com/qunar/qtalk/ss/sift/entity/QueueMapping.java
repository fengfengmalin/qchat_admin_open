package com.qunar.qtalk.ss.sift.entity;

import java.sql.Timestamp;

public class QueueMapping {

    private Long id;
    private String customerName;
    private long shopId;
    private String productId;
    private String sessionId;
    private long seatId;
    private String seatName;
    private int status;
    private int requestCount;
    private Timestamp distributedTime;
    private Timestamp inqueueTime;
    private Timestamp lastAckTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public long getShopId() {
        return shopId;
    }

    public void setShopId(long shopId) {
        this.shopId = shopId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public Timestamp getDistributedTime() {
        return distributedTime;
    }

    public void setDistributedTime(Timestamp distributedTime) {
        this.distributedTime = distributedTime;
    }

    public Timestamp getInqueueTime() {
        return inqueueTime;
    }

    public void setInqueueTime(Timestamp inqueueTime) {
        this.inqueueTime = inqueueTime;
    }

    public Timestamp getLastAckTime() {
        return lastAckTime;
    }

    public void setLastAckTime(Timestamp lastAckTime) {
        this.lastAckTime = lastAckTime;
    }
}

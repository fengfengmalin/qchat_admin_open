package com.qunar.qtalk.ss.consult.entity;

public class DataStatisticsVO {
    private String requestUrl;
    private String requestId;
    private String businessName;
    private int requestCount;

    public DataStatisticsVO(){}

    public DataStatisticsVO(String requestUrl, String requestId, String businessName, int requestCount) {
        this.requestUrl = requestUrl;
        this.requestId = requestId;
        this.businessName = businessName;
        this.requestCount = requestCount;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
}

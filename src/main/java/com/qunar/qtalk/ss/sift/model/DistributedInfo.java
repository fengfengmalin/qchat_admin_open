package com.qunar.qtalk.ss.sift.model;

import com.qunar.qtalk.ss.sift.entity.CSR;

public class DistributedInfo {
    private CSR csr;
    private int queueStatus;
    private String noServiceWelcomes;

    public CSR getCsr() {
        return csr;
    }

    public void setCsr(CSR csr) {
        this.csr = csr;
    }

    public int getQueueStatus() {
        return queueStatus;
    }

    public void setQueueStatus(int queueStatus) {
        this.queueStatus = queueStatus;
    }

    public String getNoServiceWelcomes() {
        return noServiceWelcomes;
    }

    public void setNoServiceWelcomes(String noServiceWelcomes) {
        this.noServiceWelcomes = noServiceWelcomes;
    }
}

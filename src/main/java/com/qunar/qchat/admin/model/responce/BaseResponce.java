package com.qunar.qchat.admin.model.responce;

import lombok.Data;

@Data
public class BaseResponce {
    private boolean ret;

    public boolean isRet() {
        return ret;
    }

    public void setRet(boolean ret) {
        this.ret = ret;
    }
}

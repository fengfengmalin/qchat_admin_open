package com.qunar.qtalk.ss.consult.entity;

import com.qunar.qtalk.ss.utils.JID;

public class QtMessageLog {
    private JID user;
    private long shopId;
    private long time;
    private boolean isCustomerMsg;

    public void setUser(JID user) {
        this.user = user;
    }

    public JID getUser() {
        return user;
    }

    public void setShopId(long shopId) {
        this.shopId = shopId;
    }

    public long getShopId() {
        return shopId;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setIsCustomerMsg(boolean isCustomerMsg) {
        this.isCustomerMsg = isCustomerMsg;
    }

    public boolean getIsCustomerMsg() {
        return isCustomerMsg;
    }
}

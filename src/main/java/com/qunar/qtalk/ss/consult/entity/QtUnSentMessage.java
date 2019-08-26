package com.qunar.qtalk.ss.consult.entity;

import com.qunar.qtalk.ss.utils.JID;

public class QtUnSentMessage {
    private JID userName;
    private long shopId;
    private String message;
    private String messageId;

    public JID getUserName() {
        return userName;
    }

    public void setUserName(JID userName) {
        this.userName = userName;
    }

    public long getShopId() {
        return shopId;
    }

    public void setShopId(long shopId) {
        this.shopId = shopId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }
}

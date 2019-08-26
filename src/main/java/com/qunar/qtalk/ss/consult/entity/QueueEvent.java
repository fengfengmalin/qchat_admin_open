package com.qunar.qtalk.ss.consult.entity;

import com.qunar.qtalk.ss.utils.JID;

public class QueueEvent {

    private long shopId;
    private JID userId;
    private String productId;


    public QueueEvent(JID userId, long shopId, String productId) {
        this.userId = new JID(userId.toFullJID());
        this.shopId = shopId;
        this.productId = productId;
    }

    public QueueEvent(JID userId, long shopId) {
        this.userId = new JID(userId.toFullJID());
        this.shopId = shopId;
        this.productId = QtSessionItem.DEFAULT_PRODUCTID;
    }

//    public static QueueEvent buildFromKey(QtSessionKey key) {
//
//        return new QueueEvent(key.getUserName(), key.getShopId(), key.getProductId());
//
//    }

    public long getShopId() {
        return shopId;
    }

    public void setShopId(long shopId) {
        this.shopId = shopId;
    }

    public JID getUserId() {
        return userId;
    }

    public void setUserId(JID userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}

package com.qunar.qtalk.ss.consult.entity;

import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qtalk.ss.utils.JID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;

public class QueueUser {
    private static final Logger logger = LoggerFactory.getLogger(QueueUser.class);
    private JID jid;
    private String sessionId;
    private int requestCount;
    private Timestamp lastAckTime;
    private Timestamp inQueueTime;

    public static QueueUser asUser(JID userName) {
        QueueUser user = new QueueUser();
        user.setJid(userName);
        return user;
    }

    public void setJid(JID jid) {
        this.jid = jid;
    }

    public JID getJid() {
        return jid;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }


    @Override
    public boolean equals(Object obj) {

        if (obj instanceof QueueUser) {
            QueueUser user = (QueueUser) obj;
            if (this.getSessionId() == null) {
                logger.debug("local {} vs in param {}", JacksonUtil.obj2String(user), JacksonUtil.obj2String(this));

                return StringUtils.equalsIgnoreCase(user.getJid().toBareJID(), this.jid.toBareJID());
            }

            if (this.getJid() == null) {
                return StringUtils.equalsIgnoreCase(user.getSessionId(), this.sessionId);
            }

            return StringUtils.equalsIgnoreCase(user.getJid().toBareJID(), this.jid.toBareJID()) &&
                    StringUtils.equalsIgnoreCase(user.getSessionId(), this.sessionId);
        }
        return false;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setLastAckTime(Timestamp lastAckTime) {
        this.lastAckTime = lastAckTime;
    }

    public Timestamp getLastAckTime() {
        return lastAckTime;
    }

    public void setInQueueTime(Timestamp inQueueTime) {
        this.inQueueTime = inQueueTime;
    }

    public Timestamp getInQueueTime() {
        return inQueueTime;
    }
}

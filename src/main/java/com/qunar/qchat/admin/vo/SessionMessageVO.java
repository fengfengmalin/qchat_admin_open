package com.qunar.qchat.admin.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 会话详情
 * Created by qyhw on 12/2/15.
 */
public class SessionMessageVO {

    @JsonProperty(value = "m_from")
    private String visitorName;

    @JsonProperty(value = "m_to")
    private String seatName;

    @JsonProperty(value = "content")
    private String message;

    @JsonProperty(value = "chat_time")
    private String chatTime;

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChatTime() {
        return chatTime;
    }

    public void setChatTime(String chatTime) {
        this.chatTime = chatTime;
    }
}

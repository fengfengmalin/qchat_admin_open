package com.qunar.qchat.admin.model;

import lombok.Data;

@Data
public class IMMessage {
    private String from;
    private String to;
    private String body;
    private String createTime;
    private Integer readFlag;
    private String msgId;
    private String realFrom;
    private String realto;
    private String updateTime;
}

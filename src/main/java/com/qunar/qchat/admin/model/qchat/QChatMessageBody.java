package com.qunar.qchat.admin.model.qchat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by yinmengwang on 17-5-12.
 */
@Data
@Builder
public class QChatMessageBody {

    public static final String CHAT_TYPE = "chat";
    public static final String CONSULT_TYPE = "consult";

    public static final String MEG_TYPE = "1"; // 普通消息类型

    public static final String TO_KEY = "User"; // to的格式要求:"To":[{"User":"name1"},{"User":"name2"}]

    public static final String QCHAT_DOMAIN = "conference.ejabhost2";

    @JsonProperty(value = "From")
    private String from;
    @JsonProperty(value = "To")
    private List<Map<String, String>> to;
    @JsonProperty(value = "Body")
    private String body;
    @JsonProperty(value = "Msg_Type")
    private String msgType;
    @JsonProperty(value = "Extend_Info")
    private String extendInfo;
    @JsonProperty(value = "Type")
    private String type;
    @JsonProperty(value = "Host")
    private String host;
    @JsonProperty(value = "Domain")
    private String domain;
    @JsonProperty(value = "Carbon")
    private String carbon; // pc 手机是否同步
}

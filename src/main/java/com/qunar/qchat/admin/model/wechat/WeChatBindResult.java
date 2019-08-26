package com.qunar.qchat.admin.model.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by yinmengwang on 17-4-11.
 */
@Data
public class WeChatBindResult {

    private String qid;
    @JsonProperty(value = "wechatbindcount")
    private int wechatBindCount;
    @JsonProperty(value = "lastupdate")
    private String lastUpdate;
    private Object details;
}

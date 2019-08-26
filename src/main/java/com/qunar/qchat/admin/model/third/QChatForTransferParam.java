package com.qunar.qchat.admin.model.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by yinmengwang on 17-5-9.
 */
@Data
public class QChatForTransferParam {

    private String from;
    private String to;
    private String timestamp;
    private String direction;
    @JsonProperty(value = "limitnum")
    private int limitNum;
}

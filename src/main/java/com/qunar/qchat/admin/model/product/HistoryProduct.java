package com.qunar.qchat.admin.model.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by yinmengwang on 17-4-26.
 */
@Data
public class HistoryProduct {

    @JsonProperty(value = "tts_enid")
    private String ttsEnid;
    @JsonProperty(value = "tuid")
    private String tuId;
    @JsonProperty(value = "supplier_id")
    private String busiSupplierId;
    private int businessId;
    @JsonProperty(value = "kefu")
    private String seatQName;
    @JsonProperty(value = "chat_date")
    private String chatDate;

}

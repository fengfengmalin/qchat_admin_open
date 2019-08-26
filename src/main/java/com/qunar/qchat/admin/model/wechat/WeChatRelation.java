package com.qunar.qchat.admin.model.wechat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yinmengwang on 17-4-11.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeChatRelation {

    private String qunarName;
    private String webName;
    private long supplierId;
    private int wechatBindCount;
    private String lastUpdate;
    private Object details;
}

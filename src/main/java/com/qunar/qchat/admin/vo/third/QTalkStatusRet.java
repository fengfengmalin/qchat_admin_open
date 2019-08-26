package com.qunar.qchat.admin.vo.third;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yinmengwang on 17-2-13.
 */
@Data
@NoArgsConstructor
public class QTalkStatusRet {

    private boolean ret;
    private int errcode;
    private String errmsg;
    private QTalkStatus[] data;

}



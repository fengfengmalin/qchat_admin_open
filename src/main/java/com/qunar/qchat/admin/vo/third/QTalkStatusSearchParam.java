package com.qunar.qchat.admin.vo.third;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Created by yinmengwang on 17-2-13.
 */
@Data
@Builder
public class QTalkStatusSearchParam {

    private String domain;
    private List<String> users;
}

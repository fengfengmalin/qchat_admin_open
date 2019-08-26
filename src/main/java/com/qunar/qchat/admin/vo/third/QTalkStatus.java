package com.qunar.qchat.admin.vo.third;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Created by yinmengwang on 17-2-13.
 */
@Data
@NoArgsConstructor
public class QTalkStatus{
    private String domain;
    private List<Map<String,String>> ul;
}

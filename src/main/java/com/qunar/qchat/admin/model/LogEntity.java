package com.qunar.qchat.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by yinmengwang on 17-3-24.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntity {

    public static final String OPERATE_INSERT = "insert";
    public static final String OPERATE_UPDATE = "update";
    public static final String OPERATE_DELETE = "delete";
    public static final String OPERATOR_SYSTEM = "system";
    public static final String OPERATOR_ONESEAT = "oneseat";

    public static final String ITEM_SUPPLIER = "supplier";
    public static final String ITEM_SEAT = "seat";
    public static final String ITEM_GROUP = "group";
    public static final String ITEM_GROUP_PID = "group_pid";



    private String operateType;
    private String itemType;
    private Integer itemId;
    private String itemStr;
    private String operator;
    private Timestamp operateTime;
    private String content;
    private String operateTimeStr;
}

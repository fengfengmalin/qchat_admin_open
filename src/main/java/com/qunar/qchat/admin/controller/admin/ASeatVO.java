package com.qunar.qchat.admin.controller.admin;

import lombok.Data;

import java.sql.Timestamp;

/**
 * Created by yhw on 12/01/2016.
 */
@Data
public class ASeatVO {

    private Integer id;
    private String qunarName;
    private String webName;
    private Integer supplierId;
    private String supplierName;
    private Integer serviceStatus;
    private String serviceStatusStr;
    private Integer status;
    private String statusStr;
    private String loginStatus;
    private String terminalType;
    private Timestamp createTime;
    private Timestamp lastUpdateTime;

    private String createTimeText;
    private String lastUpdateTimeText;

    private String busiSupplierId;
    private Integer bType;

    private String buTypeText;

}


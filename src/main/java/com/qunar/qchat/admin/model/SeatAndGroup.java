package com.qunar.qchat.admin.model;

import lombok.Data;

/**
 * Author : mingxing.shao
 * Date : 15-10-20
 *
 */
@Data
public class SeatAndGroup {
    private Long id;//seatId
    private String qunarName;  // 用户中心登陆用户名
    private String webName;
    private String nickName;
    private String faceLink;
    private Long groupId;
    private String groupName;
    private Integer strategy;
    private Long supplierId;
    private String supplierName;
    private String supplierLogo;
    private String busiSupplierId;
    private int busiId;
    private int priority;
    private String customerName;
    private int serviceState;
    private int maxSessions;
    private int supplierExtFlag;
}

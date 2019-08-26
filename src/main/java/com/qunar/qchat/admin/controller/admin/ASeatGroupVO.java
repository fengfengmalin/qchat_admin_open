package com.qunar.qchat.admin.controller.admin;

import lombok.Data;

import java.sql.Timestamp;

/**
 * Created by yhw on 12/01/2016.
 */
@Data
public class ASeatGroupVO {

    private Integer id;
    private String qunarName;
    private Integer groupId;
    private String groupName;
    private Integer strategy;
    private String strategyText;

    private String pid;

}

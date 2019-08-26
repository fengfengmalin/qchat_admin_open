package com.qunar.qchat.admin.model.qchat;

import lombok.Data;

import java.sql.Timestamp;

/**
 * Created by yinmengwang on 17-4-11.
 */
@Data
public class LoginData {
    private String userName;
    private String platForm;
    private Timestamp loginTime;
    private String ip;
    private Timestamp logoutAt;
    private String loginTimeStr;
    private String logoutAtStr;
}

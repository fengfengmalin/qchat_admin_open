package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.model.SystemUser;

/**
 * Created by qyhw on 10/16/15.
 */
public interface ISystemUserService {

    @Deprecated   // 返回多个
    SystemUser getSystemUserByQunarName(String qunarName);

}

package com.qunar.qchat.admin.dao;

import com.qunar.qchat.admin.model.SystemUser;

import java.util.List;

/**
 * Created by qyhw on 10/19/15.
 */
public interface ISystemUserDao {

    long saveSystemUser(SystemUser sysUser);

    void saveSystemUserList(List<SystemUser> sysUserList);

    SystemUser getSystemUserByQunarName(String qunarName);

    SystemUser getSystemUser(String qunarName, long supplierId);

    int delSystemUserBySupplierId(long supplierId);

    int delSystemUserBySupplierIds(List<Long> supplierIds);

    int delSystemUser(String qunarName, long supplierId);
}

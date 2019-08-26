package com.qunar.qchat.admin.dao.impl;

import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import com.qunar.qchat.admin.dao.ISystemUserDao;
import com.qunar.qchat.admin.model.SystemUser;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qyhw on 10/19/15.
 */
@Repository("systemUserDao")
public class SystemUserDaoImpl extends BaseSqlSessionDao implements ISystemUserDao {

    @Override
    public long saveSystemUser(SystemUser sysUser) {
        this.getWriteSqlSession().insert("SystemUserMapping.saveSystemUser", sysUser);
        return sysUser.getId();
    }

    @Override
    public void saveSystemUserList(List<SystemUser> sysUserList) {
        if(CollectionUtils.isEmpty(sysUserList))
            return;
        this.getWriteSqlSession().insert("SystemUserMapping.saveSystemUserList", sysUserList);
    }

    @Override
    public SystemUser getSystemUserByQunarName(String qunarName) {
        // TODO
        return getReadSqlSession().selectOne("SystemUserMapping.getSystemUserByQunarName",qunarName);
    }

    @Override
    public int delSystemUserBySupplierId(long supplierId) {
        return this.getWriteSqlSession().delete("SystemUserMapping.delSystemUserBySupplierId",supplierId);
    }

    @Override
    public SystemUser getSystemUser(String qunarName, long supplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("qunarName", qunarName);
        map.put("supplierId", supplierId);
        return getWriteSqlSession().selectOne("SystemUserMapping.getSystemUser", map);
    }

    @Override
    public int delSystemUserBySupplierIds(List<Long> supplierIds) {
        return getWriteSqlSession().delete("SystemUserMapping.delSystemUserBySupplierIds", supplierIds);
    }

    @Override
    public int delSystemUser(String qunarName, long supplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("qunarName", qunarName);
        map.put("supplierId", supplierId);
        return getWriteSqlSession().delete("SystemUserMapping.delSystemUser", map);
    }
}

package com.qunar.qchat.admin.dao.impl;

import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import com.qunar.qchat.admin.dao.IIpLimitDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qyhw on 01/12/17.
 */
@Repository("limitDao")
public class IpLimitDaoImpl extends BaseSqlSessionDao implements IIpLimitDao {

    @Override
    public int ipLimitCount(String ip) {
        Map<String, Object> args = new HashMap<>();
        args.put("ip", ip);
        return getReadSqlSession().selectOne("IpLimitMapping.getIpLimitCount",args);
    }
}

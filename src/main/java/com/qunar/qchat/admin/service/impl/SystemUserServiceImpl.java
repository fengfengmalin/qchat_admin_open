package com.qunar.qchat.admin.service.impl;

import com.qunar.qchat.admin.dao.ISystemUserDao;
import com.qunar.qchat.admin.model.SystemUser;
import com.qunar.qchat.admin.service.ISystemUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Author : mingxing.shao
 * Date : 15-10-29
 *
 */
@Service("systemUserService")
public class SystemUserServiceImpl implements ISystemUserService{
//    private static final Logger LOGGER = LoggerFactory.getLogger(SystemUserServiceImpl.class);

    @Resource(name = "systemUserDao")
    private ISystemUserDao systemUserDao;

    @Override
    public SystemUser getSystemUserByQunarName(String qunarName) {
        if (StringUtils.isBlank(qunarName)) {
            return null;
        }

        return systemUserDao.getSystemUserByQunarName(qunarName);
    }
}

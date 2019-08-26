package com.qunar.qchat.admin.dao.impl;

import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import com.qunar.qchat.admin.dao.IBusinessDao;
import com.qunar.qchat.admin.dao.IUserDao;
import com.qunar.qchat.admin.model.Business;
import com.qunar.qchat.admin.model.UserSeatMapping;
import com.qunar.qchat.admin.vo.BusinessVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qyhw on 01/12/17.
 */
@Repository("userDao")
public class UserDaoImpl extends BaseSqlSessionDao implements IUserDao {

    @Override
    public UserSeatMapping getUserSeatMapping(String uname) {
        return getReadSqlSession().selectOne("userSeatMapping.getUserSeatMapping", uname);
    }

    @Override
    public long saveOrUpdateUserSeatMapping(UserSeatMapping sum) {
        if (sum == null || StringUtils.isEmpty(sum.getUname())) return 0;
        UserSeatMapping sumDB = getReadSqlSession().selectOne("userSeatMapping.getUserSeatMapping", sum.getUname());
        if (sumDB == null) {
            this.getWriteSqlSession().insert("userSeatMapping.insertUserSeatMapping", sum);
            return 0; //TODO
        }
        return updateUserSeatMapping(sum);
    }

    @Override
    public int updateUserSeatMapping(UserSeatMapping sum) {
        int num = this.getWriteSqlSession().update("userSeatMapping.updateUserSeatMapping", sum);
        return num;
    }
}

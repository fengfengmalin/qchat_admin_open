package com.qunar.qchat.admin.dao;

import com.qunar.qchat.admin.model.*;
import com.qunar.qchat.admin.util.SeatQueryFilter;

import java.util.List;

/**
 * Created by qyhw on 01/12/17.
 */
public interface IUserDao {

    UserSeatMapping getUserSeatMapping(String uname);

    long saveOrUpdateUserSeatMapping(UserSeatMapping sum);

    int updateUserSeatMapping(UserSeatMapping sum);

}

package com.qunar.qtalk.ss.admin.dao;

import com.qunar.qtalk.ss.admin.entity.SysUser;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysUserDao {

    List<SysUser> selectSysUserByQunarName(String userName);
}

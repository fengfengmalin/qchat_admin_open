package com.qunar.qchat.admin.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
/**
 * Created by qyhw on 10/15/15.
 */
@Repository
public class BaseSqlSessionDao{

    @Autowired
    @Qualifier("masterSqlSession")
    private SqlSessionTemplate masterSqlSession;

    @Autowired
    @Qualifier("slaverSqlSession")
    private SqlSessionTemplate slaveSqlSession;

    public SqlSessionTemplate getReadSqlSession(){
        return slaveSqlSession;
    }

    public SqlSessionTemplate getWriteSqlSession(){
        return masterSqlSession;
    }
}

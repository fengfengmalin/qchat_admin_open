package com.qunar.qtalk.ss.session.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface IReadMarkDao {
    public int updateSingleReadmark(
            @Param("updates") String updates);

    public int updateMucReadmark(
            @Param("time") Long time,
            @Param("muc") String muc,
            @Param("username") String username,
            @Param("host") String host
    );

    public int updateAllSingle(
            @Param("username") String username,
            @Param("host") String host,
            @Param("time") long time
            );

    public int updateAllMuc(
            @Param("username") String username,
            @Param("host") String host,
            @Param("time") long tume
    );

    public int updateNewSingleReadmark(
            @Param("read_flag") int read_flag,
            @Param("updates") String updates);


    public int updateNewAllSingle(
            @Param("username") String username,
            @Param("host") String host,
            @Param("realto") String realto,
            @Param("read_flag") int read_flag,
            @Param("time") long time
    );

}

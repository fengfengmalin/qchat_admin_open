package com.qunar.qtalk.ss.session.dao;

import com.qunar.qtalk.ss.session.dao.model.ConsultMsg;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by admin on 13/07/2017.
 */
@Repository
public interface IGetMsgDao {




    public List<ConsultMsg> selectHistory(
                @Param("user") String user,
                @Param("host") String host,
                @Param("time") double time,
                @Param("num") long num);


    public List<ConsultMsg> selectConsultMsgbyTime(
            @Param("from") String from,
            @Param("to") String to,
            @Param("virtual") String virtual,
            @Param("real") String real,
            @Param("direction") String direction,
            @Param("turn") String turn,
            @Param("num") long num,
            @Param("time") double time);

}

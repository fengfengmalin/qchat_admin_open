package com.qunar.chat.dao;


import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultMsgDao {
    void insertConsultMsg(@Param("m_from") String m_from,
                          @Param("from_host") String from_host,
                          @Param("m_to") String m_to,
                          @Param("to_host") String to_host,
                          @Param("msg") String msg,
                          @Param("msg_id") String msg_id,
                          @Param("time") double time,
                          @Param("realfrom") String realfrom,
                          @Param("realto") String realto,
                          @Param("type") String type
    );
}

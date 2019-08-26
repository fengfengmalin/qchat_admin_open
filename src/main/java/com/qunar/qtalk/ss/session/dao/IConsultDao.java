package com.qunar.qtalk.ss.session.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import com.qunar.qtalk.ss.session.dao.model.ConsultMsgInfo;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by admin on 13/07/2017.
 */
@Repository
public interface IConsultDao {

    public void insertConsultMsg(@Param("m_from") String m_from,
                                 @Param("from_host") String  from_host,
                                 @Param("m_to") String  m_to,
                                 @Param("to_host") String  to_host,
                                 @Param("msg") String  msg,
                                 @Param("msg_id") String  msg_id,
                                 @Param("time") double time,
                                 @Param("realfrom") String  realfrom,
                                 @Param("realto") String  realto,
                                 @Param("type") String type,
                                 @Param("qchatId") String qchatId
                                );

    List<ConsultMsgInfo> selectHistoryMsgByCondition(@Param("shopJid") String shopJid, @Param("realFrom") String realFrom,
                                                     @Param("realTo") String realTo, @Param("timestamp") Timestamp timestamp,
                                                     @Param("limit") int limit, @Param("direction") String direction);

}

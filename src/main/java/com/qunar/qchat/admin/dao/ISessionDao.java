package com.qunar.qchat.admin.dao;

import com.qunar.qchat.admin.model.SeatSessionsDetail;
import com.qunar.qchat.admin.model.Session;
import com.qunar.qchat.admin.model.SessionStateEnum;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ISessionDao {
    List<Session> getSessions(String fromtime, List<SessionStateEnum> sessionStateEnums);
    List<Session> getSessions(List<SessionStateEnum> sessionStateEnums);
    List<Session> getSessionsAsc(List<SessionStateEnum> sessionStateEnums);

    Map<String,SeatSessionsDetail> getSessionCounts(List<String> seatids, String shop_name, List<SessionStateEnum> sessionStateEnums);
    List<Session> getSession(@Param(value = "userName") String userName, @Param(value = "shopName") String shopName, @Param(value = "pid") String pid);
    void insertSession(Session session);
    void updateSession(Session session);

    void insertBusiSessionMapping(String busiSessionId,String sessionId);
    List<Session> getSessionOfBusiSessionId(String busiSessionId);
    public void closeInvaildSession(Session session,String shopId,String userName,String pid);

    List<Session> getSessionAndPid(@Param(value = "userName") String userName, @Param(value = "shopName") String shopName, @Param(value = "pid") String pid);
    public void updateSessionEsPid(Session session);

    List<Session> getSessionsByPidAndState(List<SessionStateEnum> sessionStateEnums,String shopName,String userName,String pid);

}

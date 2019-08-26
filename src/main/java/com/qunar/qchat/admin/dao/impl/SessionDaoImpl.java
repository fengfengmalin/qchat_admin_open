package com.qunar.qchat.admin.dao.impl;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import com.qunar.qchat.admin.dao.ISessionDao;
import com.qunar.qchat.admin.model.SeatSessionsDetail;
import com.qunar.qchat.admin.model.Session;
import com.qunar.qchat.admin.model.SessionStateEnum;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.EjabdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository(value = "sessionDao")
public class SessionDaoImpl extends BaseSqlSessionDao implements ISessionDao {
    @Override
    public List<Session> getSessions(String fromtime, List<SessionStateEnum> sessionStateEnums) {
        if (!CollectionUtil.isEmpty(sessionStateEnums)) {
            List<Integer> sessValueStates = Lists.transform(sessionStateEnums, new Function<SessionStateEnum, Integer>() {
                @Override
                public Integer apply(SessionStateEnum sessionStateEnum) {
                    return sessionStateEnum.getState();
                }
            });

            Map<String, Object> param = Maps.newHashMap();
            param.put("fromtime", fromtime);
            param.put("states", sessValueStates);

            return getReadSqlSession().selectList("SessionMapping.selectTargetStateSessionFromTimestame", param);

        } else {
            Map<String, Object> param = Maps.newHashMap();
            param.put("fromtime", fromtime);
            return getReadSqlSession().selectList("SessionMapping.selectSessionFromTimestame", param);
        }
    }

    @Override
    public List<Session> getSessions(List<SessionStateEnum> sessionStateEnums) {
        if (!CollectionUtil.isEmpty(sessionStateEnums)) {
            List<Integer> sessValueStates = Lists.transform(sessionStateEnums, new Function<SessionStateEnum, Integer>() {
                @Override
                public Integer apply(SessionStateEnum sessionStateEnum) {
                    return sessionStateEnum.getState();
                }
            });

            Map<String, Object> param = Maps.newHashMap();
            param.put("states", sessValueStates);

            return getReadSqlSession().selectList("SessionMapping.selectTargetStateSession", param);

        } else {
            return Lists.newArrayList();
        }
    }


    @Override
    public List<Session> getSessionsByPidAndState(List<SessionStateEnum> sessionStateEnums,
                                                  String shopName,String userName,String pid) {
        if (!CollectionUtil.isEmpty(sessionStateEnums)) {
            List<Integer> sessValueStates = Lists.transform(sessionStateEnums, new Function<SessionStateEnum, Integer>() {
                @Override
                public Integer apply(SessionStateEnum sessionStateEnum) {
                    return sessionStateEnum.getState();
                }
            });

            Map<String, Object> param = Maps.newHashMap();
            param.put("states", sessValueStates);
            param.put("pid", pid);
            param.put("shopName", shopName);
            param.put("userName", EjabdUtil.makeSureUserJid(userName, QChatConstant.DEFAULT_HOST));

            return getReadSqlSession().selectList("SessionMapping.selectTargetStateAndPidSession", param);

        } else {
            return Lists.newArrayList();
        }
    }

    @Override
    public List<Session> getSessionsAsc(List<SessionStateEnum> sessionStateEnums) {
        if (!CollectionUtil.isEmpty(sessionStateEnums)) {
            List<Integer> sessValueStates = Lists.transform(sessionStateEnums, new Function<SessionStateEnum, Integer>() {
                @Override
                public Integer apply(SessionStateEnum sessionStateEnum) {
                    return sessionStateEnum.getState();
                }
            });

            Map<String, Object> param = Maps.newHashMap();
            param.put("states", sessValueStates);

            return getReadSqlSession().selectList("SessionMapping.selectTargetStateSessionAsc", param);

        } else {
            return Lists.newArrayList();
        }
    }

    @Override
    public Map<String, SeatSessionsDetail> getSessionCounts(List<String> seatids, String shop_name, List<SessionStateEnum> sessionStateEnums) {
        if (CollectionUtil.isEmpty(seatids) || Strings.isNullOrEmpty(shop_name) || CollectionUtil.isEmpty(sessionStateEnums)) {
            return Maps.newHashMap();
        }

        Map<String, Object> param = Maps.newHashMap();
        param.put("seat_ids", seatids);
        param.put("shop_name", shop_name);
        param.put("states", Lists.transform(sessionStateEnums, new Function<SessionStateEnum, Integer>() {
            @Override
            public Integer apply(SessionStateEnum sessionStateEnum) {
                return sessionStateEnum.getState();
            }
        }));

        List<SeatSessionsDetail> result = getReadSqlSession().selectList("SessionMapping.selectSessionCounts", param);

        Map<String, SeatSessionsDetail> retMap = Maps.newHashMap();
        if (!CollectionUtil.isEmpty(result)) {
            for (SeatSessionsDetail i : result
                    ) {
                retMap.put(i.getSeat_name(), i);
            }
        }
        return retMap;
    }

    @Override
    public List<Session> getSession(String userName, String shopName, String pid) {
        Map<String, Object> param = Maps.newHashMap();
        param.put("userName", EjabdUtil.makeSureUserJid(userName, QChatConstant.DEFAULT_HOST));
        param.put("shopName", shopName);
        param.put("pid", pid);
        return getReadSqlSession().selectList("SessionMapping.getSession", param);
    }

    @Override
    public List<Session> getSessionAndPid(String userName, String shopName, String pid) {
        Map<String, Object> param = Maps.newHashMap();
        param.put("userName", EjabdUtil.makeSureUserJid(userName, QChatConstant.DEFAULT_HOST));
        param.put("shopName", shopName);
        param.put("pid", pid);
        return getReadSqlSession().selectList("SessionMapping.getSessionAndPid", param);
    }


    public Session getLastSession(String userName, String shopName, String pid) {
        Map<String, Object> param = Maps.newHashMap();
        param.put("userName", userName);
        param.put("shopName", shopName);
        param.put("pid", pid);
        return getReadSqlSession().selectOne("SessionMapping.getLastSession", param);
    }

    @Override
    public void insertSession(Session session) {
        session.setUser_name(EjabdUtil.makeSureUserJid(session.getUser_name(), QChatConstant.DEFAULT_HOST));
        session.setSeat_name(EjabdUtil.makeSureUserJid(session.getSeat_name(), QChatConstant.DEFAULT_HOST));
        getWriteSqlSession().insert("SessionMapping.insertSession", session);
    }

    @Override
    public void updateSessionEsPid(Session session) {
        session.setUser_name(EjabdUtil.makeSureUserJid(session.getUser_name(), QChatConstant.DEFAULT_HOST));
        session.setSeat_name(EjabdUtil.makeSureUserJid(session.getSeat_name(), QChatConstant.DEFAULT_HOST));
        getWriteSqlSession().insert("SessionMapping.updateSessionEsPid", session);
    }

    @Override
    public void updateSession(Session session) {
        session.setUser_name(EjabdUtil.makeSureUserJid(session.getUser_name(), QChatConstant.DEFAULT_HOST));
        session.setSeat_name(EjabdUtil.makeSureUserJid(session.getSeat_name(), QChatConstant.DEFAULT_HOST));

        Map<String, Object> param = Maps.newHashMap();
        param.put("session_id", session.getSession_id());
        Session ss = getReadSqlSession().selectOne("SessionMapping.getSessionBySessionId", param);

        if (null != ss) {

            getWriteSqlSession().update("SessionMapping.updateSession", session);

            if (!Objects.equals(ss.getSession_state(), session.getSession_state())) {
                Map<String, Object> param1 = Maps.newHashMap();
                param1.put("sessionid", ss.getSession_id());
                param1.put("oldstat", ss.getSession_state());
                param1.put("newstat", session.getSession_state());
                getWriteSqlSession().insert("SessionLogMapping.insertSessionLog", param1);
            }
        } else {
            insertSession(session);
        }

    }


    @Override
    public void closeInvaildSession(Session session,String shopId,String userName,String pid) {
        session.setUser_name(EjabdUtil.makeSureUserJid(session.getUser_name(), QChatConstant.DEFAULT_HOST));
        session.setSeat_name(EjabdUtil.makeSureUserJid(session.getSeat_name(), QChatConstant.DEFAULT_HOST));

//        Map<String, Object> param = Maps.newHashMap();
    //    param.put("session_id", session.getSession_id());
     //   Session ss = getReadSqlSession().selectOne("SessionMapping.getSessionBySessionId", param);

        Session ss = getLastSession(userName, shopId, pid);

        if (null != ss && ss.getSeat_name().equals(session.getSeat_name())) {

            getWriteSqlSession().update("SessionMapping.updateSession", session);
        }

    }

    @Override
    public void insertBusiSessionMapping(String busiSessionId, String sessionId) {
        if (Strings.isNullOrEmpty(busiSessionId) || Strings.isNullOrEmpty(sessionId))
            return;

        Map<String,String > param = Maps.newHashMap();
        param.put("busi_session_id",busiSessionId);
        param.put("session_id",sessionId);
        getWriteSqlSession().insert("SessionMapping.insertBusiSessionMapping", param);
    }

    @Override
    public List<Session> getSessionOfBusiSessionId(String busiSessionId) {
        if (Strings.isNullOrEmpty(busiSessionId))
            return null;

        Map<String, String> param = Maps.newHashMap();
        param.put("busi_session_id", busiSessionId);
        return getReadSqlSession().selectList("SessionMapping.selectSessonsOfBusiSession",param);
    }
}

package com.qunar.qchat.admin.util;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.constants.RedisConstants;
import com.qunar.qchat.admin.model.Session;
import com.qunar.qchat.admin.model.qchat.QChatConstant;

import java.util.concurrent.TimeUnit;

public class RedisBizUtil {
    public static String makeQCAdminKey(String qunarName, long supplierid) {

        return RedisConstants.KEY_GLOBAL_RRE + "_" + qunarName + "_" + QChatConstant.SEATSHOPPREFIX + "_" + supplierid;
    }

    public static String makeQCAdminKey(String qunarName, String shopid) {
        return RedisConstants.KEY_GLOBAL_RRE + "_" + qunarName + "_" + shopid;
    }

    public static void updateSession(Session session) {
        String key = makeQCAdminKey(EjabdUtil.makeSureUserJid(session.getUser_name(), QChatConstant.DEFAULT_HOST),
                EjabdUtil.makeSureUserJid(session.getShop_name(), QChatConstant.DEFAULT_HOST));

        RedisUtil.hPut(RedisConstants.KEY_QCADMIN_TABLE_ID, key, RedisConstants.KEY_QCADMIN_PID,
                Strings.isNullOrEmpty(session.getProduct_id()) ? "" : session.getProduct_id());
        RedisUtil.hPut(RedisConstants.KEY_QCADMIN_TABLE_ID, key, RedisConstants.KEY_QCADMIN_SEATID, EjabdUtil.makeSureUserJid(session.getSeat_name(), QChatConstant.DEFAULT_HOST));
        RedisUtil.hPut(RedisConstants.KEY_QCADMIN_TABLE_ID, key, RedisConstants.KEY_QCADMIN_SEAT_ISROBOT, null == session.getIsrobot_seat()?"0":String.valueOf(session.getIsrobot_seat()));
        RedisUtil.hPut(RedisConstants.KEY_QCADMIN_TABLE_ID, key, RedisConstants.KEY_QCADMIN_SEAT_SESSIONSEATE, String.valueOf(session.getSession_state()));
        if (!Strings.isNullOrEmpty(session.getSession_id()))
            RedisUtil.hPut(RedisConstants.KEY_QCADMIN_TABLE_ID, key, RedisConstants.KEY_QCADMIN_SEAT_SESSIONID, session.getSession_id());

        RedisUtil.expire(RedisConstants.KEY_QCADMIN_TABLE_ID, key, 24, TimeUnit.HOURS);
    }

    public static Session lastSession(String username, String shopName) {
        String key = makeQCAdminKey(EjabdUtil.makeSureUserJid(username, QChatConstant.DEFAULT_HOST),
                EjabdUtil.makeSureUserJid(shopName, QChatConstant.DEFAULT_HOST));

        Session session = new Session();
        if (RedisUtil.hGet(RedisConstants.KEY_QCADMIN_TABLE_ID, key, RedisConstants.KEY_QCADMIN_SEAT_SESSIONID) == null){
            return null;
        }
        session.setProduct_id(RedisUtil.hGet(RedisConstants.KEY_QCADMIN_TABLE_ID, key, RedisConstants.KEY_QCADMIN_PID));
        session.setSeat_name(RedisUtil.hGet(RedisConstants.KEY_QCADMIN_TABLE_ID, key, RedisConstants.KEY_QCADMIN_SEATID));
        try{
            session.setIsrobot_seat(Integer.valueOf(RedisUtil.hGet(RedisConstants.KEY_QCADMIN_TABLE_ID, key, RedisConstants.KEY_QCADMIN_SEAT_ISROBOT)));
        } catch (Exception e){
            session.setIsrobot_seat(0);
        }
        Object oldSessionState = RedisUtil.hGet(RedisConstants.KEY_QCADMIN_TABLE_ID, key, RedisConstants.KEY_QCADMIN_SEAT_SESSIONSEATE);
        session.setSession_state(oldSessionState == null ? 0:Integer.valueOf(oldSessionState.toString()));
        session.setUser_name(EjabdUtil.makeSureUserJid(username, QChatConstant.DEFAULT_HOST));
        session.setShop_name(EjabdUtil.makeSureUserid(shopName));
      //  Object oldSessionId = RedisUtil.hGet(RedisConstants.KEY_QCADMIN_TABLE_ID, key, RedisConstants.KEY_QCADMIN_SEAT_SESSIONID);

        //session.setSession_id(RedisUtil.hGet(RedisConstants.KEY_QCADMIN_TABLE_ID, key, RedisConstants.KEY_QCADMIN_SEAT_SESSIONID));
        session.setSession_id(RedisUtil.hGet(RedisConstants.KEY_QCADMIN_TABLE_ID, key, RedisConstants.KEY_QCADMIN_SEAT_SESSIONID));

        return session;
    }
    public static String makeBusiSessionId(String userName,String shopName,String pid){
        return  "busisession:"+userName+":"+shopName+":"+pid;
    }
}

package com.qunar.qtalk.ss.utils.common;

import com.alibaba.fastjson.JSON;
import com.qunar.qtalk.ss.utils.JID;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * create by hubo.hu (lex) at 2018/5/29
 */
public class UserHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserHelper.class);
    private static boolean isLog = false;

    /*
     * 获取是否在线
     * @param username  带domain userid
     * @return
     */
    public static boolean isUserOnline(JID username) {
        boolean isHasOtherPlatOnline = false;
        try {
            Map<String, String> result = RedisUtil.defaultRedis().hGetAll(7, "ejabberd:sm:other:" + username.toBareJID());

            if (isLog) LOGGER.info("UserHelper  isUserOnline result={}", result);

            if (result != null) {
                for (String value : result.values()) {
                    if (isLog) LOGGER.info("UserHelper  isUserOnline value={}", value);
                    Map<String, String> resouce = JSON.parseObject(value, Map.class);
                    String r = "";
                    String f = "";
                    if (resouce != null) {
                        if (resouce.containsKey("r")) {
                            r = resouce.get("r");
                        }
                        if (resouce.containsKey("f")) {
                            f = resouce.get("f");
                        }
                    }

                    if ("away".equalsIgnoreCase(f)) {
                        continue;
                    }
                    if ("normal".equalsIgnoreCase(f) || "online".equalsIgnoreCase(f)) {
//                                LOGGER.info("getMesaageToSend online value={}", value);
                        isHasOtherPlatOnline = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            return isHasOtherPlatOnline;
        }
        return isHasOtherPlatOnline;
    }


    public static boolean isHasPlatAway(JID username) {
        boolean isHasAnyAway = false;
        String key = "ejabberd:sm:other:" + username.toBareJID();
        Map<String, String> result = RedisUtil.defaultRedis().hGetAll(7, key);
        if (!CollectionUtils.isEmpty(result)) {
            for (String value : result.values()) {
                Map<String, String> resouce = JSON.parseObject(value, Map.class);
                String r = resouce.get("r").toString();
                String f = resouce.get("f").toString();
                if ("away".equalsIgnoreCase(f)) {
                    isHasAnyAway = true;
                    return isHasAnyAway;
                }
            }
        }
        return isHasAnyAway;
    }

    /**
     * @param username
     * @return Android_QTALK iOS PC32 PC64 Mac BigIM
     */
    public static Map<String, Boolean> getAllPlatOnlineStatus(JID username) {
        Map<String, Boolean> map = new HashMap<>();
        try {
            String key = "ejabberd:sm:other:" + username.toBareJID();
            Map<String, String> result = RedisUtil.defaultRedis().hGetAll(7, key);

            if (result != null) {
                for (String value : result.values()) {
                    Map<String, String> resouce = JSON.parseObject(value, Map.class);
                    String plat = "";
                    String r = "";
                    String f = "";
                    if (!CollectionUtils.isEmpty(resouce)) {
                        if (resouce.containsKey("r")) {
                            r = resouce.get("r");
                            String platKey = "_P[";
                            if (r.indexOf(platKey) != -1) {
                                String str = r.substring(r.indexOf(platKey) + platKey.length(), r.length() - 1);
                                if (str.indexOf("]") != -1) {
                                    plat = str.substring(0, str.indexOf("]"));
                                }
                            }
                            if (TextUtils.isEmpty(plat)) {
                                continue;
                            }
                        }
                        if (resouce.containsKey("f")) {
                            f = resouce.get("f");
                        }
                    }

                    if ("away".equalsIgnoreCase(f)) {
                        map.put(plat, false);
                    } else if ("normal".equalsIgnoreCase(f)
                            || "online".equalsIgnoreCase(f)
                            || "push".equalsIgnoreCase(f)) {
                        map.put(plat, true);
                    }
                }
            }
        } catch (Exception e) {
            return map;
        }
        return map;
    }
}

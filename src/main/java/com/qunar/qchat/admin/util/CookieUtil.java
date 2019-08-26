package com.qunar.qchat.admin.util;

import com.aliasi.util.Strings;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.constants.SessionConstants;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qtalk.ss.constants.Config;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by qyhw on 1/18/16.
 */
public class CookieUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CookieUtil.class);

    public static void setCookie(HttpServletResponse response, String key, String value, String path) {
        Cookie c = new Cookie(key, value);
        c.setPath(path);
        c.setDomain(Config.QCHAT_COOKIE_DOMAIN);
        response.addCookie(c);
    }

    public static void setCookie(String key, String value, HttpServletResponse response) {
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setDomain(Config.QCHAT_COOKIE_DOMAIN);
        cookie.setMaxAge(3600);
        response.addCookie(cookie);
    }

    public static String getCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (SessionConstants.QSESSION_ID.equalsIgnoreCase(cookie.getName())) {
                    return  cookie.getValue();
                }
            }
        }
        return null;
    }


    public static String getQunarName(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();

        if (null != cookies) {
            for (Cookie cookie : cookies) {

                if ("q_ckey".equalsIgnoreCase(cookie.getName())) {
                    String cookieqckey = cookie.getValue();
                    try {
                        Map<String,Object> kvs = Maps.newHashMap();
                        // t=1524626533&u=ykxuarx1597&k=DB59A14245F002B24F294F3CCE7547B2&d=ejabhost2
                        cookieqckey = Arrays.toString(Base64.decode(cookieqckey));
                        String[] split = Strings.split(cookieqckey, '&');
                        for (String s : split){
                            String[] kv = Strings.split(s,'=');
                            kvs.put(kv[0],kv[1]);
                        }

                        String domian = QChatConstant.DEFAULT_HOST;
                        if (kvs.containsKey("d"))
                            domian = kvs.get("d").toString();

                        if (kvs.containsKey("k")){
                            return EjabdUtil.makeSureUserJid(kvs.get("k").toString(),domian);
                        }

                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error("UnsupportedEncodingException", e);
                    }

                   return "";
                }
                String cookiePrefix = "U.";
                if ("_q".equalsIgnoreCase(cookie.getName())) {
                    String cookieV = cookie.getValue();
                    if (!TextUtils.isEmpty(cookieV) && 0 == cookieV.indexOf(cookiePrefix)) {
                        return cookieV.substring(cookiePrefix.length());
                    }
                }
            }
        }
        return "";
    }
}

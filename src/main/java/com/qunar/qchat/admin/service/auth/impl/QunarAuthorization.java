package com.qunar.qchat.admin.service.auth.impl;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class QunarAuthorization extends BaseAuthorization {
    private final static Logger logger = LoggerFactory.getLogger(QunarAuthorization.class);
    private static String LOGIN_PATH = "https://xxx/login.jsp?goback=1";

    @Override
    public String getUserName(HttpServletRequest request) {
        String username = null;
        if(request != null) {
            // TODO CookieUser
//            CookieUser cookieUser = UserClient.getUser(request);
//            username = cookieUser == null ? null : cookieUser.getUserName();
        }
//        if (StringUtils.trimToEmpty(username))
        username = StringUtils.trimToEmpty(username);

        logger.debug("通过同域方式获得用户：" + username);
        return username;
    }

    @Override
    public String getRedirect(HttpServletRequest request) {
        String host = request.getHeader("host");

        StringBuilder callbackUrl = new StringBuilder();
        String requestUri = request.getRequestURI();

        callbackUrl.append("http://").append(host).append(requestUri);

        if (!StringUtils.isBlank(request.getQueryString())) {
            callbackUrl.append("?").append(request.getQueryString());
        }

        String viewName = null;
        try {
            viewName = LOGIN_PATH + "&ret="
                    + URLEncoder.encode(callbackUrl.toString(), "utf8");
            return viewName;
        } catch (UnsupportedEncodingException e) {
            logger.error("UnsupportedEncodingException", e);
        }
        return viewName;
    }
}

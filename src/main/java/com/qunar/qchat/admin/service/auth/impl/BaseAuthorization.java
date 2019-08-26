package com.qunar.qchat.admin.service.auth.impl;

import com.qunar.qchat.admin.service.auth.IAuth;
import com.qunar.qchat.admin.util.CookieUtil;
import com.qunar.qchat.admin.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class BaseAuthorization implements IAuth {
    private static final Logger logger = LoggerFactory.getLogger(BaseAuthorization.class);

    @Override
    public String getUserName(HttpServletRequest request) {
        String cookieValue = CookieUtil.getCookieValue(request);
        if (StringUtils.isNotEmpty(cookieValue)) {
            String userName = RedisUtil.get(cookieValue, String.class);
            logger.info("BaseAuthorization username:{}", userName);
            return userName;
        }
        return null;
    }

    @Override
    public String getRedirect(HttpServletRequest request) {
        return null;
    }
}

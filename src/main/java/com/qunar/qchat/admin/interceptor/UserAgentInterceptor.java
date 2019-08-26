package com.qunar.qchat.admin.interceptor;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserAgentInterceptor  extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(UserAgentInterceptor.class);

    private String respMsg = "{\"errcode\":200006,\"errmsg\":\"ua禁止访问\",\"ret\":false}";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String useragent = request.getHeader("User-Agent");
        if (!Strings.isNullOrEmpty(useragent) && useragent.indexOf("Baiduspider")>0){
            logger.info("kill by UserAgentInterceptor : " + respMsg);
            ResponseUtil.print(respMsg, response);
            return false;
        }

        return true;
    }


}

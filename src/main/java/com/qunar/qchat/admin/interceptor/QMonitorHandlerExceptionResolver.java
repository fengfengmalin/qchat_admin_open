package com.qunar.qchat.admin.interceptor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 该类用于监控系统5xx错误
 * Created by qyhw on 10/14/15.
 */
public class QMonitorHandlerExceptionResolver implements HandlerExceptionResolver {
    private static final Logger logger = LoggerFactory.getLogger(QMonitorHandlerExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String userAgent = request.getHeader("User-Agent");
        logger.error("系统异常,requestURI:{},queryString:{},userAgent:{}", request.getRequestURI(), request.getQueryString(), userAgent, ex);
        return null;
    }
}

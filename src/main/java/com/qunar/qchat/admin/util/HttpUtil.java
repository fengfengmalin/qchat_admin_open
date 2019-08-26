package com.qunar.qchat.admin.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by qyhw on 12/29/15.
 */
public class HttpUtil {

    public static String getPathInfo(HttpServletRequest request) {
        return request == null ? null : request.getRequestURI().substring(request.getContextPath().length());
    }
}

package com.qunar.qchat.admin.service.auth;

import javax.servlet.http.HttpServletRequest;

public interface IAuth {
    public String getUserName(HttpServletRequest request);
    public String getRedirect(HttpServletRequest request);
}

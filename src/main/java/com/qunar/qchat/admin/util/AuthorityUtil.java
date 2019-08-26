package com.qunar.qchat.admin.util;

import com.qunar.qchat.admin.service.auth.AuthFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by qyhw on 10/19/15.
 */
@SuppressWarnings("deprecation")
public class AuthorityUtil {


    public static String getThirdPartyUserName(HttpServletRequest request){
		return AuthFactory.getAuth("").getUserName(request);
	}


    /**
     * 未登录用户登录之后重定向到当前的url
     */
    public static String redirect2CurrentUrl(HttpServletRequest request) {
        return AuthFactory.getAuth("").getRedirect(request);
    }

}

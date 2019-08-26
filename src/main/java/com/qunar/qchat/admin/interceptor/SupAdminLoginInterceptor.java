package com.qunar.qchat.admin.interceptor;

import com.qunar.qchat.admin.constants.UserFieldConstant;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qchat.admin.service.ISystemUserService;
import com.qunar.qchat.admin.util.AuthorityUtil;
import com.qunar.qchat.admin.util.JsonUtil;
import com.qunar.qchat.admin.util.SessionUtils;
import com.qunar.qchat.admin.vo.JsonResultVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录拦截器
 * Created by qyhw on 10/19/15.
 */
@Component
public class SupAdminLoginInterceptor extends AbstractLoginInterceptor {

    @Override
    // 超级管理员权限，暂时只开放静静，后续建表处理
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception{
        String qunarName = AuthorityUtil.getThirdPartyUserName(request);
        if (StringUtils.isEmpty(qunarName)) {
            printErrorInfo(response, UserFieldConstant.USER_NOT_LOGIN,"您未登录，请先登录再操作.");
            return false;
        }

        return true;
    }

}

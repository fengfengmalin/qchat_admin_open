package com.qunar.qchat.admin.interceptor;

import com.qunar.qchat.admin.annotation.MustLogin;
import com.qunar.qchat.admin.constants.SessionConstants;
import com.qunar.qchat.admin.constants.UserFieldConstant;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qchat.admin.service.ISystemUserService;
import com.qunar.qchat.admin.util.AuthorityUtil;
import com.qunar.qchat.admin.util.JsonUtil;
import com.qunar.qchat.admin.util.SessionUtils;
import com.qunar.qchat.admin.vo.JsonResultVO;
import com.qunar.qchat.admin.vo.SysUserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录拦截器
 * Created by qyhw on 08/19/16.
 */
public class AbstractLoginInterceptor extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AbstractLoginInterceptor.class);
    @Autowired
    ISupplierService supplierService;

    @Autowired
    ISystemUserService systemUserService;

    @Override
    public void afterCompletion(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse,
                                Object obj, Exception exception) throws Exception {
        //请求结束，清除  SessionUtil里的内容 ,防止一直保持引用，无法GC掉已不再登录的用户
        SessionUtils.setSession(null);
    }

    void printErrorInfo(HttpServletResponse response, int loginStatus, String message) {
        response.setContentType("application/json;charset=UTF-8");
        JsonResultVO o = new JsonResultVO();
        Map<String, Integer> map = new HashMap<>();
        map.put("loginStatus", loginStatus);
        o.setMsg(message);
        o.setRet(JsonResultVO.ResultStatus.FAILED);
        o.setData(map);
        JsonUtil.serializer(o, response);
    }

    /**
     * 判断是否必须登录才能操作
     **/
    boolean redirectLoginPage(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler) {
        MustLogin ma = handler.getMethodAnnotation(MustLogin.class);
        MustLogin ta = AnnotationUtils.findAnnotation(handler.getBeanType(), MustLogin.class);
        //默认全部允许
        if (ma == null && ta == null) return true;

        MustLogin.ViewType viewType = null;
        if (ma != null) {
            viewType = ma.value();
        }
        if (viewType == null) {
            viewType = ta.value();
        }

        if (MustLogin.ViewType.VM.equals(viewType)) {
            //跳转到登录
            try {
                response.sendRedirect(AuthorityUtil.redirect2CurrentUrl(request));
            } catch (IOException e) {
                logger.error("redirectLoginPage error", e);
            }
            return false;
        }

        printErrorInfo(response, UserFieldConstant.USER_NOT_LOGIN, "您未登录，请先登录再操作.");
        return false;

    }
}
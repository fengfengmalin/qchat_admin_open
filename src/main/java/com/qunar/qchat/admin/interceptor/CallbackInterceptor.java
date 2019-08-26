package com.qunar.qchat.admin.interceptor;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 跨域支持
 */
public class CallbackInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String callback = request.getParameter("callback");
        if (!StringUtils.isEmpty(callback)) {
            ServletOutputStream os = response.getOutputStream();
            os.write(callback.getBytes());
            os.write("(".getBytes());
        }
        return true;
    }

//    @Override
//    public void postHandle(HttpServletRequest request,
//                           HttpServletResponse response, Object handler,
//                           ModelAndView modelAndView) throws Exception {
//
//        String callback = request.getParameter("callback");
//        if (!StringUtils.isEmpty(callback)) {
//            ServletOutputStream os = response.getOutputStream();
//            os.write(")1".getBytes());
//            os.flush();
//        }
//    }

    // 补 ） 之所以所在 afterCompletion 而不做在 postHandle  是因为 正常的调用会走到 postHandle，afterCompletion 两个过程。
    // 当 preHandle 被触发后有异常产生，那么只会 走到 afterCompletion
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);

        String callback = request.getParameter("callback");
        if (!StringUtils.isEmpty(callback)) {
            ServletOutputStream os = response.getOutputStream();
            os.write(")".getBytes());
            os.flush();
            os.close();
        }
    }
}




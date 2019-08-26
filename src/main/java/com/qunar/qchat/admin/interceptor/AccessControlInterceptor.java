package com.qunar.qchat.admin.interceptor;

import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.util.*;
import com.qunar.qchat.admin.vo.AccessControlVO;
import com.qunar.qchat.admin.vo.UrlIpMappingVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 访问控制
 */
public class AccessControlInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AccessControlInterceptor.class);

    private String respMsg = "{\"errcode\":200005,\"errmsg\":\"IP禁止访问\",\"ret\":false}";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String accessControl = Config.getAccessControlConf();
        if(StringUtils.isEmpty(accessControl)){
            logger.error("AccessControlInterceptor --- QConfig file accessControl is empty.");
            return true;
        }

        AccessControlVO acVO;
        try {
            acVO = JacksonUtil.string2Obj(accessControl,AccessControlVO.class);
        } catch (Exception e) {
            logger.error("AccessControlInterceptor ---  fail to parse QConfig file accessControl.");
            return true;
        }

        if(acVO == null) {
            return true;
        }

        String reqIp = IPUtil.getUserIPString(request);

        // 黑名单判断
        boolean result = checkDenyIp(response, acVO.getDenyIp(), reqIp);
        if(!result) {
            return false;
        }
        // 白名单判断
        result = checkAllowIp(request, response, acVO.getAllowIp(), reqIp);
        return result;
    }

    private boolean checkAllowIp(HttpServletRequest request, HttpServletResponse response, List<UrlIpMappingVO> allowIpList, String reqIp) {
        if(CollectionUtils.isEmpty(allowIpList)) {
            return true;
        }
        String reqUrl = HttpUtil.getPathInfo(request);
        for (UrlIpMappingVO uim : allowIpList) {
            String url = uim.getUrl();
            if(reqUrl.startsWith(url)) {
                List<String> ipList = uim.getIpList();
                if (!ipList.contains(reqIp)) {
                    ResponseUtil.print(respMsg, response);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkDenyIp(HttpServletResponse response, List<String> denyIpList, String reqIp) {
        if(CollectionUtils.isEmpty(denyIpList)) {
            return true;
        }
        if (denyIpList.contains(reqIp)) {
            ResponseUtil.print(respMsg, response);
            return false;
        }
        return true;
    }
}




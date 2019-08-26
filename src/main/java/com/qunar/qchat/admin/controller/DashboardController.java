package com.qunar.qchat.admin.controller;

import com.qunar.qchat.admin.annotation.MustLogin;
import com.qunar.qchat.admin.constants.SessionConstants;
import com.qunar.qchat.admin.util.CookieUtil;
import com.qunar.qchat.admin.util.JsonResultUtil;
import com.qunar.qchat.admin.util.RedisUtil;
import com.qunar.qchat.admin.util.SessionUtils;
import com.qunar.qchat.admin.vo.JsonResultVO;
import com.qunar.qchat.admin.vo.SysUserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by yhw on 08/16/2016.
 */
@Controller
@RequestMapping(value = "/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);



    @ResponseBody
    @RequestMapping(value = "/setLoginUser.qunar")
    public JsonResultVO<?> setLoginUser(HttpServletRequest request, HttpServletResponse response) {
        String user = request.getParameter("username");
        String qSessionId = UUID.randomUUID().toString().replace("-", "");
        CookieUtil.setCookie(SessionConstants.QSESSION_ID, qSessionId, response);
        RedisUtil.set(qSessionId, user, 60, TimeUnit.MINUTES);
        Map<String, String> result = new HashMap<String, String>();
        result.put("username", user);
        return JsonResultUtil.buildSucceedJsonResult(result);
    }

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "/getUser.qunar")
    public JsonResultVO<?> getUser(HttpServletRequest request) {
        SysUserVO systemUser = (SysUserVO) SessionUtils.getAttribute(SessionConstants.SysUser);
        if (systemUser == null) {
            return JsonResultUtil.buildFailedJsonResult("未登录");
        }
        Map<String, String> result = new HashMap<String, String>();
        result.put("username", systemUser.getQunarName());
        return JsonResultUtil.buildSucceedJsonResult(result);
    }

    @MustLogin(value = MustLogin.ViewType.VM)
    @RequestMapping("/index.do")
    public ModelAndView index() {
        ModelAndView mav = new ModelAndView("page/supplier/statChatInfo");
        return mav;
    }

    @MustLogin(value = MustLogin.ViewType.VM)
    @RequestMapping("/sessionList.do")
    public ModelAndView sessionList() {
        ModelAndView mav = new ModelAndView("page/supplier/sessionList");
        return mav;
    }

}

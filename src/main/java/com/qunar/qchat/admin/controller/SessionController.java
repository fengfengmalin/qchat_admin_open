package com.qunar.qchat.admin.controller;

import com.qunar.qchat.admin.annotation.MustLogin;
import com.qunar.qchat.admin.service.ISessionService;
import com.qunar.qchat.admin.service.query.SessionQueryFilter;
import com.qunar.qchat.admin.util.CovertToStringUtil;
import com.qunar.qchat.admin.util.JsonResultUtil;
import com.qunar.qchat.admin.util.SessionUtils;
import com.qunar.qchat.admin.vo.BusiReturnResult;
import com.qunar.qchat.admin.vo.JsonResultVO;
import com.qunar.qchat.admin.vo.SessionMessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by qyhw on 12/2/15.
 */
@Controller
@RequestMapping("/session")
public class SessionController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

    @Autowired
    private ISessionService sessionService;

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "detailSession.qunar")
    public JsonResultVO<?> detailSession(
            @RequestParam(value = "visitorName", required = false, defaultValue = "") String visitorName,
            @RequestParam(value = "seatName", required = false, defaultValue = "") String seatName,
            @RequestParam(value = "startTime", required = false, defaultValue = "") String startTime,
            @RequestParam(value = "endTime", required = false, defaultValue = "") String endTime,
            @RequestParam(value = "timestamp", required = false, defaultValue = "") String timestamp,
            @RequestParam(value = "limitnum", required = false, defaultValue = "10") int limitnum,
            @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
            HttpServletRequest request) {
        String para = CovertToStringUtil.convertRequestParaToString(request);
        logger.info("获取会话详情接口，页面请求参数: {}", para);

        BusiReturnResult<List<SessionMessageVO>> re = sessionService.detailSession(
                SessionUtils.getLoginUser().getCurBuSuList(), visitorName, seatName, startTime, endTime, timestamp,
                limitnum, offset);
        return JsonResultUtil.buildJsonResult(re.isRet(), re.getCode(), re.getMsg(), re.getData());
    }

    private SessionQueryFilter buildQueryFilter(String visitorName, String seatName, long startTime, long endTime,
                                                int status, int busiType, List<Long> suIdList) {
        SessionQueryFilter filter = new SessionQueryFilter();
        filter.setSeatName(seatName);
        filter.setVisitorName(visitorName);
        filter.setStartTime(startTime);
        filter.setEndTime(endTime);
        filter.setStatus(status);
        filter.setBusiType(busiType);
        filter.setSuIdList(suIdList);
        return filter;
    }

}
package com.qunar.qchat.admin.controller;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.Robot;
import com.qunar.qchat.admin.service.IRobotService;
import com.qunar.qchat.admin.util.AuthorityUtil;
import com.qunar.qchat.admin.vo.conf.JsonData;

import com.qunar.qtalk.ss.utils.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by yinmengwang on 17-8-17.
 */
@Slf4j
@Controller
@RequestMapping(value = "/i/api/robot")
public class RobotApiController {

    @Resource
    private IRobotService robotService;

    @RequestMapping(value = "/save.json")
    @ResponseBody
    public JsonData saveRobot(Robot robot, HttpServletRequest request) {
        if (robot == null || Strings.isNullOrEmpty(robot.getRobotId()) || Strings.isNullOrEmpty(robot.getRobotName())) {
            return JsonData.error("机器人信息错误");
        }
        BusinessEnum businessEnum = BusinessEnum.of(robot.getBusinessId());
        if (businessEnum == null || businessEnum == BusinessEnum.EMPTY) {
            return JsonData.error("业务线id错误");
        }
        String operator = AuthorityUtil.getThirdPartyUserName(request);
        // todo 加入管理员角色后，增加权限校验，可去掉内网限制
        robot.setOperator(operator);
        try {
            robotService.saveRobot(robot);
            return JsonData.success("添加成功");
        } catch (Exception e) {
            log.error("添加机器人出错，robot:{}", JacksonUtils.obj2String(robot), e);
        }
        return JsonData.error("系统错误");
    }
}

package com.qunar.qchat.admin.controller.touch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by yinmengwang on 17-5-12.
 */
@Slf4j
@Controller
@RequestMapping("/touch/dashboard")
public class TouchDashboardController {

    @RequestMapping(value = "/detail.qunar")
    public ModelAndView detail() {
        return new ModelAndView("page/touch/dashboard/detail");
    }

    @RequestMapping(value = "/index.qunar")
    public ModelAndView index() {
        return new ModelAndView("page/touch/dashboard/index");
    }

    @RequestMapping(value = "/qchat.qunar")
    public ModelAndView qchat() {
        return new ModelAndView("page/touch/dashboard/qchat");
    }
}

package com.qunar.qchat.admin.controller.touch;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by yinmengwang on 17-5-17.
 */
@Controller
@RequestMapping(value = "/touch/qchat")
public class TouchQChatController {

    @RequestMapping(value = "/supplier.qunar")
    public ModelAndView supplier() {
        return new ModelAndView("page/touch/qchat/supplier");
    }
}

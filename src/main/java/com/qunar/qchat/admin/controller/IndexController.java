package com.qunar.qchat.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by hongwu.yang on 2015年10月14日.
 */
@Controller
@RequestMapping(value = "/")
public class IndexController {

    @RequestMapping(value = "/index.do", method = RequestMethod.GET)
    public String innerError(){
        return "";
    }

}

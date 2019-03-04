package com.qunar.chat.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.qunar.chat.service.TestService;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/v1")
public class TestController {

    @Autowired
    TestService testService;

    @RequestMapping(value = "/test.qunar")
    @ResponseBody
    public Map<String, Object> test(String test) {
        Map<String, Object> result = new HashMap<>();
        result.put("test", test);
        String test1 = testService.test();
        System.out.println(test1);
        return result;
    }
}

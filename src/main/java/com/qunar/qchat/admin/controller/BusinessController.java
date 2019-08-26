package com.qunar.qchat.admin.controller;

import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.model.Business;
import com.qunar.qchat.admin.service.impl.BusinessServiceImpl;
import com.qunar.qchat.admin.util.JsonResultUtil;
import com.qunar.qchat.admin.vo.JsonResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping(value = "/business")
public class BusinessController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessController.class);
    @Resource
    BusinessServiceImpl businessService;

    @RequestMapping("/getAllBusinesses.json")
    @ResponseBody
    public JsonResultVO<?> getBusiSupplier() {
        LOGGER.info("start get all businesses.");
        List<Business> businessList = null;
        try {
            businessList = businessService.getAllBusiness();
        } catch (Exception e) {
            LOGGER.error("get all businesses error");
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(),
                    BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
        }
        return JsonResultUtil.buildSucceedJsonResult(BusiResponseCodeEnum.SUCCESS.getCode(),
                BusiResponseCodeEnum.SUCCESS.getMsg(), businessList);
    }
}

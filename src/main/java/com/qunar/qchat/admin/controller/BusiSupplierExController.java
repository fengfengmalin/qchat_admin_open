package com.qunar.qchat.admin.controller;




import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.dao.supplier.SupplierNewDao;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.util.JsonResultUtil;
import com.qunar.qchat.admin.vo.BusiReturnResult;
import com.qunar.qchat.admin.vo.JsonResultVO;
import com.qunar.qchat.admin.vo.SupplierVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;


/**
 * Created by yinmengwang on 17-4-26.
 */
@Slf4j
@Controller
@RequestMapping(value = "/busiSupplierEx")
public class BusiSupplierExController {

    @Resource
    private ISupplierService supplierService;

    @Resource
    private SupplierNewDao supplierNewDao;

    @ResponseBody
    @RequestMapping(value = "/saveBusiSupplier.qunar")
    public JsonResultVO<?> saveBusiSupplier(@RequestParam(value = "p", required = true, defaultValue = "") String p) {
      //  logger.info("saveSupplier -- 请求参数 p: {}", p);
        SupplierVO s = JacksonUtil.string2Obj(p, SupplierVO.class);
        if (null == s) {
        //    logger.error("saveSupplier -- 添加供应商发生异常");
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(),
                    BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
        }

        try {
            BusiReturnResult result = supplierService.saveSupplierEx(s);
            return JsonResultUtil.buildSucceedJsonResult(result.getCode(), result.getMsg(), "");
        } catch (Exception e) {
          //  logger.error("saveSupplier -- 添加供应商发生异常", e);
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(),
                    BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
        }
    }


}

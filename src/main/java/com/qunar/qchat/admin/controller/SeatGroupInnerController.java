package com.qunar.qchat.admin.controller;

import com.google.common.base.Splitter;
import com.qunar.qchat.admin.common.IpLimitCacheService;
import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.service.ISeatGroupService;
import com.qunar.qchat.admin.service.impl.SeatGroupServiceImpl;
import com.qunar.qchat.admin.util.IPUtil;
import com.qunar.qchat.admin.util.JsonResultUtil;
import com.qunar.qchat.admin.vo.JsonResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qyhw on 10/26/15.
 */
@Controller
@RequestMapping("/group/i")
public class SeatGroupInnerController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SeatGroupInnerController.class);

    @Autowired
    private ISeatGroupService seatGroupService;
    @Autowired
    private IpLimitCacheService ipLimitCacheService;

    @ResponseBody
    @RequestMapping(value = "assignProducts.qunar")
    public JsonResultVO<?> assignProducts(
            @RequestParam(value = "groupId") Integer groupId
            , @RequestParam(value = "pIds") String pIds
            , HttpServletRequest request
    ) {
        String ip = IPUtil.getUserIPString(request);
        if (ipLimitCacheService.ipCheck(ip)) {
            List<String> pidArr = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(pIds);

            List<String> ids = new ArrayList<>();
            List<String> existProducts = seatGroupService.queryProductsInner(groupId);
            if (null != existProducts) {
                for (String pid : pidArr) {
                    if (!existProducts.contains(pid))
                        ids.add(pid);
                }
            } else {
                ids = pidArr;
            }
            SeatGroupServiceImpl.GROUPERRORCODE result = seatGroupService.assignProductsInner(groupId, ids);
            if (result.getCode()==0) {
                logger.info("assignProductsInner success");
                return JsonResultUtil.buildSucceedJsonResult("分配成功", null);
            } else {
                return JsonResultUtil.buildFailedJsonResult(result.getMsg());
            }
        }

        return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.IP_LIMIT.getCode(), BusiResponseCodeEnum.IP_LIMIT.getMsg());
    }

}
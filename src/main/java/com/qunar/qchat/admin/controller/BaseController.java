package com.qunar.qchat.admin.controller;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.Seat;
import com.qunar.qchat.admin.model.SeatSession;
import com.qunar.qchat.admin.service.ISeatService;
import com.qunar.qchat.admin.service.ISeatSessionService;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.JsonResultUtil;
import com.qunar.qchat.admin.vo.JsonResultVO;
import com.qunar.qchat.admin.vo.SeatVO;
import com.qunar.qchat.admin.vo.SupplierVO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by qyhw on 1/8/16.
 */
public class BaseController {
    private final static Logger logger = LoggerFactory.getLogger(BaseController.class);
    public static final String ERROR_MSG_KEY_SPRING = "error";

    @Resource(name = "seatService")
    protected ISeatService seatService;

    @Resource(name = "seatSessionService")
    protected ISeatSessionService seatSessionService;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value= HttpStatus.OK)
    public ModelAndView handleException(Exception ex, HttpServletRequest request) {
        JsonResultVO<?> result = JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(), BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
        return new ModelAndView().addObject(ERROR_MSG_KEY_SPRING, result);
    }


    protected List<Long> buildSuIdList(String suIds, List<SupplierVO> allSuList) {
        if (CollectionUtil.isEmpty(allSuList)) {
            return null;
        }

        List<Long> allSuIdList = new ArrayList<>(allSuList.size());
        for (SupplierVO su : allSuList) {
            allSuIdList.add(su.getId());
        }

        List<Long> suIdList = new ArrayList<>(allSuList.size());
        if(StringUtils.isNotEmpty(suIds)) {
            String[] suIdArr = suIds.split(",");
            for (String suId : suIdArr) {
                Long suIdv2 = Long.valueOf(suId);
                if(allSuIdList.contains(suIdv2)) {
                    suIdList.add(suIdv2);
                }
            }
        }

        if (CollectionUtil.isEmpty(suIdList)) {
            suIdList = allSuIdList;
        }
        return suIdList;
    }

    protected JsonResultVO<?> doSession(Long seatId, final String qunarName, final String uname) {
        if(seatId <= 0 && org.apache.commons.lang3.StringUtils.isEmpty(qunarName)) {
            return JsonResultUtil.buildFailedJsonResult("参数不合法");
        }

        // 优先匹配seatId
        if (seatId > 0) {
            Seat seat = seatService.getSeatBySeatId(seatId);
            if (seat != null) {
                seatSessionService.setSeatSessionTime(new SeatSession(seatId, new Date()));
            }
            return JsonResultUtil.buildSucceedJsonResult("success", null);
        }

        //
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(qunarName)) {
            List<SeatVO> sList = seatService.getSeatByQunarName(qunarName);
            if (CollectionUtil.isNotEmpty(sList)) {
                for (SeatVO s : sList) {
                    seatSessionService.setSeatSessionTime(new SeatSession(s.getId(), new Date()));
                }
            }
        }
        logger.debug("doSession success seatId:{} uname:{}", seatId, uname);
        return JsonResultUtil.buildSucceedJsonResult("success", null);
    }

    protected JsonResultVO<?> getAllSeatNamesBase(@RequestParam(value = "supplierIds", required = false, defaultValue = "") String supplierIdsStr, @RequestParam(value = "busiType") Integer busiType) {
        List<String> supplierIds = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(supplierIdsStr);
        String seatNames = seatService.getSeatNameList(supplierIds, BusinessEnum.of(busiType));
        Map<String,Object> result= Maps.newHashMap();
        result.put("qunarNames", org.apache.commons.lang3.StringUtils.isEmpty(seatNames) ? "" : seatNames);
        return JsonResultUtil.buildSucceedJsonResult("sucess", result);
    }
}

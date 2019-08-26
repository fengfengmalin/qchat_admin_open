package com.qunar.qchat.admin.service.impl;

import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.dao.ISeatDao;
import com.qunar.qchat.admin.service.ISessionService;
import com.qunar.qchat.admin.util.*;
import com.qunar.qchat.admin.vo.BusiReturnResult;
import com.qunar.qchat.admin.vo.SessionMessageVO;
import com.qunar.qchat.admin.vo.SupplierVO;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qyhw on 12/2/15.
 */
@Service("sessionService")
public class SessionServiceImpl implements ISessionService {

    private final Logger logger = LoggerFactory.getLogger(SessionServiceImpl.class);

    private final String SEAT_NAME_SEPARATOR = " ";

    @Resource(name = "seatDao")
    private ISeatDao seatDao;



    @Override
    public BusiReturnResult<List<SessionMessageVO>> detailSession(List<SupplierVO> suList, String visitorName,
            String seatName, String startTime, String endTime, String timestamp, int limitnum, int direction) {
        Map<String, String> formParams = buildQueryParamMap(visitorName, seatName, startTime, endTime, timestamp,
                limitnum, direction);
        String mStr = MapUtil.toString(formParams);
        logger.info("获取会话详情接口，输入参数: {}, 登录信息： {}", mStr, JacksonUtil.obj2String(suList));

        if (StringUtils.isEmpty(visitorName) && StringUtils.isEmpty(seatName)) {
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_PARAM_INVALID, false, null);
        }

        if (!checkSeatNameIsExist(suList, seatName) && StringUtils.isNotEmpty(seatName)) {
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_NOT_EXISTS, false, null);
        }

        List<SessionMessageVO> smList = null;
        try {
            String jsonRes = HttpClientUtils.post(Config.SESSION_DETAIL_URL, formParams);
            SessionDetailResult result = JacksonUtils.string2Obj(jsonRes, SessionDetailResult.class);
            if (result != null && result.isRet()) {
                smList = result.getData();
            }
        } catch (Exception e) {
            logger.error("获取会话详情接口，解析数据失败", e);
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION, false, smList);
        }

        return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.SUCCESS, true, smList);
    }

    private boolean checkSeatNameIsExist(List<SupplierVO> suList, String seatName) {
        if (CollectionUtil.isEmpty(suList)) {
            return false;
        }

        List<Long> suIdList = new ArrayList<>(suList.size());
        for (SupplierVO su : suList) {
            suIdList.add(su.getId());
        }
        String seatNameStr = seatDao.getAllSeatNameList(suIdList);
        if (StringUtils.isEmpty(seatNameStr) || StringUtils.isEmpty(seatName)) {
            return false;
        }

        String[] seatNameList = seatName.split(SEAT_NAME_SEPARATOR);
        for (String sName : seatNameList) {
            if (!seatNameStr.toLowerCase().contains(sName)) {
                return false;
            }
        }
        return true;
    }

    private Map<String, String> buildQueryParamMap(String visitorName, String seatName, String startTime,
            String endTime, String timestamp, int limitnum, int offset) {
        Map<String, String> formParams = new HashMap<>();
        if (StringUtils.isNotBlank(seatName)) {
            formParams.put("seat_name", seatName);
        }
        if (StringUtils.isNotBlank(visitorName)) {
            formParams.put("visitor_name", visitorName);
        }
        if (StringUtils.isNotBlank(timestamp)) {
            formParams.put("timestamp", timestamp);
        }
        if (StringUtils.isNotBlank(startTime)) {
            formParams.put("chat_begin_time", String.valueOf(startTime));
        }
        if (StringUtils.isNotBlank(endTime)) {
            formParams.put("chat_end_time", String.valueOf(endTime));
        }
        formParams.put("limit", String.valueOf(limitnum));
        formParams.put("offset", String.valueOf(offset));
        return formParams;
    }



}
@Data
class SessionDetailResult{
    private boolean ret;
    private List<SessionMessageVO> data;
}

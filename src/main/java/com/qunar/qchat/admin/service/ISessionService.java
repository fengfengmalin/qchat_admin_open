package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.vo.BusiReturnResult;
import com.qunar.qchat.admin.vo.SessionMessageVO;
import com.qunar.qchat.admin.vo.SupplierVO;

import java.util.List;

/**
 * Created by qyhw on 12/2/15.
 */
public interface ISessionService {

    /**
     * 分页获取会话列表
     * 
     * @param filter
     * @param pageNum
     * @param pageSize
     * @return
     */
//    BusiReturnResult<SessionListResultVO> pageQuerySessionList(SessionQueryFilter filter, int pageNum, int pageSize);

    /**
     * 获取会话详情
     * 
     * @param suList 供应商集合
     * @param visitorName 访客名称
     * @param seatName 客服名称
     * @param startTime 会话开始时间
     * @param endTime 会话结束时间
     * @param timestamp 时间戳 单位:秒 用来分页查询使用
     * @param limitnum 请求条数
     * @param offset
     * @return
     */
    BusiReturnResult<List<SessionMessageVO>> detailSession(List<SupplierVO> suList, String visitorName, String seatName,
            String timestamp, String startTime, String endTime, int limitnum, int offset);



}

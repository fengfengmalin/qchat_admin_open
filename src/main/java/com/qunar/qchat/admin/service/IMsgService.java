package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.vo.SeatWithStateVO;
import com.qunar.qchat.admin.vo.SeatsResultVO;

import java.util.List;
import java.util.Map;

/**
 * Created by yinmengwang on 17-4-19.
 */
public interface IMsgService {

    SeatsResultVO<SeatWithStateVO> getLastChatSeat(BusinessEnum businessEnum, String qunarName,
            List<SeatWithStateVO> ssList);

    String getLastChatSeat(String qunarName,List<String> shopids,List<String> seatIds,int since);

    String seatIntervalRobot(List<String> seatQNames, String userQName,List<String> shopids);

    List<Map<String,Object>> getLastConversationTime(String userid, List<String> seatids);
}

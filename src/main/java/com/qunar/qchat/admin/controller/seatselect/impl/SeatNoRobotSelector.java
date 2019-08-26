package com.qunar.qchat.admin.controller.seatselect.impl;

import com.qunar.qchat.admin.vo.SeatWithStateVO;
import com.qunar.qchat.admin.vo.SeatsResultVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service(value = "seatNoRobotSelector")
public class SeatNoRobotSelector extends SeatDefaultSelector {
   // private static final Logger logger = LoggerFactory.getLogger(SeatNoRobotSelector.class);


    /**
     * 不需要机器人分配
     *
     * @return
     */
    @Override
    protected SeatsResultVO<SeatWithStateVO> getPostRobotSeat() {
        return null;
    }


    /**
     * 不需要机器人分配
     *
     * @return
     */
    @Override
    protected SeatsResultVO<SeatWithStateVO> getPreRobotSeat() {
        return null;
    }


    /**
     * 没有机器人任何逻辑
     *
     * @param ssList
     * @param robotseat
     * @return
     */
    @Override
    protected SeatsResultVO<SeatWithStateVO> processOnPreRobotSelect(List<SeatWithStateVO> ssList, SeatsResultVO<SeatWithStateVO> robotseat) {
        return null;
    }

//    @Override
//    protected SeatsResultVO<SeatWithStateVO> processOnRealSeatSelect(List<SeatWithStateVO> ssList, Map<OnlineState, SeatWithStateVO> qosSeatList) {
//        return super.processOnRealSeatSelect(ssList, qosSeatList);
//    }
}




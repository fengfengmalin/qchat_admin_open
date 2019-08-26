package com.qunar.qchat.admin.controller.seatselect;

import com.qunar.qchat.admin.vo.SeatWithStateVO;
import com.qunar.qchat.admin.vo.SeatsResultVO;

public interface ISeatSelector {
    SeatsResultVO<SeatWithStateVO> getOneSeat(SelectorConfigration d);
}

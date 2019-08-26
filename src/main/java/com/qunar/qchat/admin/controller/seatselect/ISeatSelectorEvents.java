package com.qunar.qchat.admin.controller.seatselect;

import com.qunar.qchat.admin.vo.SeatWithStateVO;

public interface ISeatSelectorEvents {
    void onRealChanged(SelectorConfigration configration,SeatWithStateVO oldSeat, SeatWithStateVO newSeat);
    void onPreRobotSelectd(SelectorConfigration configration,SeatWithStateVO oldSeat, SeatWithStateVO newSeat);
    void onPostRobotSelect(SelectorConfigration configration,SeatWithStateVO seat);
    void onRealSeatSelect(SelectorConfigration configration,SeatWithStateVO seat);
    void onRealSeatNotSelect(SelectorConfigration configration);
    void onConversationHolded(SelectorConfigration configration,SeatWithStateVO seat);
    void onQueue(SelectorConfigration configration,SeatWithStateVO seat);
    void onLeaveMessage(SelectorConfigration configration);
}

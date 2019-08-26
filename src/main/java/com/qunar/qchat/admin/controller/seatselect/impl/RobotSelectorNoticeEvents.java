package com.qunar.qchat.admin.controller.seatselect.impl;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.controller.seatselect.ISeatSelectorEvents;
import com.qunar.qchat.admin.controller.seatselect.SelectorConfigration;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.service.third.INoticeService;
import com.qunar.qchat.admin.util.EjabdUtil;
import com.qunar.qchat.admin.vo.SeatWithStateVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service(value = "robotSelectorNoticeEvents")
public class RobotSelectorNoticeEvents implements ISeatSelectorEvents {
    private static final Logger logger = LoggerFactory.getLogger(RobotSelectorNoticeEvents.class);
    @Resource
    protected INoticeService noticeService;

    @Override
    public void onRealChanged(SelectorConfigration configration, SeatWithStateVO oldSeat, SeatWithStateVO newSeat) {
        // 给用户个提示，说谁给你服务。
        if (null != newSeat && null != newSeat.getSeat() && !Strings.isNullOrEmpty(newSeat.getSeat().getQunarName())){
            String showName = newSeat.getSeat().getWebName();
            if (Strings.isNullOrEmpty(showName))
                showName = newSeat.getSeat().getNickName();
            if (Strings.isNullOrEmpty(showName))
                showName = newSeat.getSeat().getQunarName();

            String virtualid = newSeat.getSupplier().getShopId();
            String from = EjabdUtil.makeSureUserJid(newSeat.getSeat().getQunarName(), QChatConstant.DEFAULT_HOST);
            String to = EjabdUtil.makeSureUserJid(configration.getQunarName(),QChatConstant.DEFAULT_HOST);

            boolean ret = noticeService.sendConsultMessage( String.format("您好，我是人工客服 %s，请问有什么可以帮您？",showName),
                    from,
                    from,
                    virtualid,
                    to,
                    ""
            );
            logger.debug("sendConsultMessage ret:{}", ret);
        }
    }

    @Override
    public void onPreRobotSelectd(SelectorConfigration configration, SeatWithStateVO oldSeat, SeatWithStateVO newSeat) {

    }

    @Override
    public void onPostRobotSelect(SelectorConfigration configration, SeatWithStateVO seat) {

    }

    @Override
    public void onRealSeatSelect(SelectorConfigration configration, SeatWithStateVO seat) {
        onRealChanged(configration,null,seat);
    }

    @Override
    public void onRealSeatNotSelect(SelectorConfigration configration) {
        // 当没有分配的时候，给用户，返回个提示
        if (null!=noticeService){
            noticeService.sendPromotConsultTextMessage(
                    "很抱歉，人工客服暂时无法提供服务，请把问题告诉小驼，小驼尝试帮您解决哦~",
                    configration.getLastSeatName(),
                    configration.getQunarName(),
                    "shop_"+configration.getSupplierId()
            );
        }
    }

    @Override
    public void onConversationHolded(SelectorConfigration configration, SeatWithStateVO seat) {
        onRealChanged(configration,null,seat);
    }

    @Override
    public void onQueue(SelectorConfigration configration, SeatWithStateVO seat) {

    }

    @Override
    public void onLeaveMessage(SelectorConfigration configration) {

    }
}

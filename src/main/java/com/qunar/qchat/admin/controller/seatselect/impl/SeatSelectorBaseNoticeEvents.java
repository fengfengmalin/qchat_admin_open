package com.qunar.qchat.admin.controller.seatselect.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.constants.RobotConfig;
import com.qunar.qchat.admin.controller.seatselect.ISeatSelectorEvents;
import com.qunar.qchat.admin.controller.seatselect.SelectorConfigration;
import com.qunar.qchat.admin.model.OnlineState;
import com.qunar.qchat.admin.model.ServiceStatusEnum;
import com.qunar.qchat.admin.service.third.INoticeService;
import com.qunar.qchat.admin.util.EjabdUtil;
import com.qunar.qchat.admin.vo.SeatWithStateVO;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service(value = "seatSelectorBaseNoticeEvents")
public class SeatSelectorBaseNoticeEvents implements ISeatSelectorEvents {

    @Resource
    protected INoticeService noticeService;

   // private static final Logger logger = LoggerFactory.getLogger(SeatSelectorBaseNoticeEvents.class);


    @Override
    public void onQueue(SelectorConfigration configration, SeatWithStateVO seat) {
      /*  String realfrom = "";
        if (seat == null){
            realfrom = EjabdUtil.makeShopName(configration.getSupplierId());
        }else{
            realfrom = seat.getSeat().getQunarName();
        }
        // 当没有分配的时候，给用户，返回个提示
        if (null!=noticeService){
            logger.info("send single  msg on queue ,qunarnme :{},getSupplierId{},realfrom:{}",
                    configration.getQunarName(),configration.getSupplierId());

            noticeService.sendSingelConsultPromoteMessageEx(
                    "很抱歉，所有客服忙，请稍后~",
                    EjabdUtil.makeSureUserJid(seat.getSeat().getQunarName(), QChatConstant.DEFAULT_HOST),
                    EjabdUtil.makeSureUserJid(EjabdUtil.makeShopName(configration.getSupplierId()),QChatConstant.DEFAULT_HOST),
                    EjabdUtil.makeSureUserJid(seat.getSeat().getQunarName(),QChatConstant.DEFAULT_HOST),
                    EjabdUtil.makeSureUserJid(configration.getQunarName(),QChatConstant.DEFAULT_HOST),
                    false
            );
        }*/
    }

    @Override
    public void onLeaveMessage(SelectorConfigration configration) {
        if (null!=noticeService){
            noticeService.sendSingelConsultPromoteMessage(
                    "很抱歉，客服均不在线，请留言~",
                    EjabdUtil.makeShopName(configration.getSupplierId()),
                    configration.getQunarName(),
                    "",
                    false
            );
        }
    }

    @Override
    public void onPostRobotSelect(SelectorConfigration configration, SeatWithStateVO seat) {

    }

    @Override
    public void onRealSeatSelect(SelectorConfigration configration,SeatWithStateVO seat) {

    }

    @Override
    public void onRealSeatNotSelect(SelectorConfigration configration) {

    }

    @Override
    public void onConversationHolded(SelectorConfigration configration,SeatWithStateVO seat) {

    }

    @Override
    public void onRealChanged(SelectorConfigration configration,SeatWithStateVO oldSeat, SeatWithStateVO newSeat) {
        // 分配变人了，由于当时oldseat不在线，或者某种原因，向原来的oldseat发送一条notice

        String to = null;
        if (null != oldSeat
                && null != oldSeat.getSeat()
                && null != oldSeat.getSeat().getQunarName()) {
            to = oldSeat.getSeat().getQunarName();
        }

        if (oldSeat != null && StringUtils.isNotEmpty(to)) {

            if (null != noticeService){

                Map<String, Object> servicestat = Maps.newHashMap();
                servicestat.put("sub_title", "服务模式");
                Integer serviceStatus = oldSeat.getSeat() != null && oldSeat.getSeat().getServiceStatus() != null ? oldSeat.getSeat().getServiceStatus() : 99;
                servicestat.put("sub_content", ServiceStatusEnum.getValue(serviceStatus));

                Map<String, Object> onlinestat = Maps.newHashMap();
                onlinestat.put("sub_title", "在线状态");
                onlinestat.put("sub_content", OnlineState.stringValue(oldSeat.getOnlineState()));

                List<Map<String, Object>> content = new ArrayList<>();
                content.add(onlinestat);
                content.add(servicestat);

                Map<String, Object> param = Maps.newHashMap();
                param.put("title", "座席未分配");
                param.put("content", content);
                param.put("operation_url", "");
                param.put("prompt", "由于在线状态／服务模式原因，咨询未分配");
                String body = JacksonUtils.obj2String(param);
                noticeService.sendSystemNotifyMessage(body, to);
            }
        }
    }

    @Override
    public void onPreRobotSelectd(SelectorConfigration configration,SeatWithStateVO oldSeat, SeatWithStateVO newSeat) {
        // 分配变人了，由于当时oldseat不在线，或超过了60分钟消息，分配给了机器人，发送一条消息

        String to = null;
        if (null != oldSeat && null != oldSeat.getSeat() && null != oldSeat.getSeat().getQunarName()) {
            to = oldSeat.getSeat().getQunarName();
        }
        if (null != oldSeat && !Strings.isNullOrEmpty(to)) {

            Map<String, Object> servicestat = Maps.newHashMap();
            servicestat.put("sub_title", "服务模式");
            Integer serviceStatus = oldSeat.getSeat() != null && oldSeat.getSeat().getServiceStatus() != null ? oldSeat.getSeat().getServiceStatus() : 99;
            servicestat.put("sub_content", ServiceStatusEnum.getValue(serviceStatus));

            Map<String, Object> onlinestat = Maps.newHashMap();
            onlinestat.put("sub_title", "在线状态");
            onlinestat.put("sub_content", OnlineState.stringValue(oldSeat.getOnlineState()));


            List<Map<String, Object>> content = new ArrayList<>();
            content.add(onlinestat);
            content.add(servicestat);

            Map<String, Object> param = Maps.newHashMap();
            param.put("title", "座席未分配");
            param.put("content", content);
            param.put("operation_url", "");
            param.put("prompt", "" + RobotConfig.ROBOT_ALLOCATION_INTERVAL_TIME_MIN + "分钟内，无与客人的会话记录，重新分配机器人");
            String body = JacksonUtils.obj2String(param);


            if (null != noticeService)
                noticeService.sendSystemNotifyMessage(body, to);
        }
    }
}

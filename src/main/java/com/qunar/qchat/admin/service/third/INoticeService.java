package com.qunar.qchat.admin.service.third;

import com.qunar.qchat.admin.model.qchat.ProductNoteArgs;
import com.qunar.qchat.admin.vo.third.ProductVO;

/**
 * Created by yinmengwang on 17-5-12.
 */
public interface INoticeService {



    boolean sendProductNote(ProductVO productVO, ProductNoteArgs args);

    boolean sendProductNoteBySeat(ProductVO productVO, ProductNoteArgs args);

    boolean sendConsultMessage(String message, String from, String realFrom, String to, String realTo,String backupinfo);

    boolean sendSystemNotifyMessage(String message,String to);

//    boolean sendChatMessage(String message, String from, String to, String seatHost);

    boolean sendPromotConsultTextMessage (String message,String from,String to ,String virtualid);

    boolean sendConversationNoticeMessage(String message,String from,String to ,String virtualid);

    boolean sendSingelConsultPromoteMessage(String message,String from,String to,String realfrom,boolean toSeat);

    boolean sendSingelConsultPromoteMessageEx(String message,String from,String to,String realfrom,String realto,boolean toSeat);


}


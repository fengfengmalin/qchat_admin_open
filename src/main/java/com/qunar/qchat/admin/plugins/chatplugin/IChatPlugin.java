package com.qunar.qchat.admin.plugins.chatplugin;

import com.qunar.qchat.admin.model.responce.SessionDetailResponce;
import com.qunar.qchat.admin.service.IMsgService;
import com.qunar.qchat.admin.service.query.SessionQueryFilter;
import com.qunar.qchat.admin.vo.BusiReturnResult;
import com.qunar.qchat.admin.vo.SeatOnlineState;
import com.qunar.qchat.admin.vo.SessionListResultVO;

import java.util.List;

public interface IChatPlugin  extends IMsgService{

    public List<SeatOnlineState> getUsersOnlineStatus(List<String> userids);

    public BusiReturnResult checkUserExist(String p);

    public SessionListResultVO getChatSessionList(SessionQueryFilter filter, int pageNum, int pageSize);

    public SessionDetailResponce getSessionDetail(String visitorName,
                                                  String seatName, String startTime, String endTime, String timestamp, int limitnum, int direction);
    public boolean sendThirdMessage(String from,String to ,String message);

    public boolean sendThirePresence(String from ,String to,String category,String body);
}

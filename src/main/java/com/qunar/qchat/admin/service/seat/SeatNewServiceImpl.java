package com.qunar.qchat.admin.service.seat;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.dao.seat.SeatNewDao;
import com.qunar.qchat.admin.model.Seat;
import com.qunar.qchat.admin.util.EjabdUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by yinmengwang on 17-7-3.
 */
@Service
public class SeatNewServiceImpl implements SeatNewService {

    @Resource
    private SeatNewDao seatNewDao;

    @Override
    public boolean isSeat(String qunarName, String seatHost) {
        if (Strings.isNullOrEmpty(qunarName)) {
            return false;
        }


        qunarName = EjabdUtil.makeSureUserid(qunarName);
//        // qtalk用户默认都是客服
//        if (!Strings.isNullOrEmpty(seatHost) && StringUtils.equals(seatHost, QChatConstant.QTALK_HOST)) {
//            return true;
//        }
        List<Seat> seats = seatNewDao.querySeatByQunarName(qunarName.toLowerCase());
        if (CollectionUtils.isEmpty(seats)) {
            return false;
        }
        return true;
    }
}

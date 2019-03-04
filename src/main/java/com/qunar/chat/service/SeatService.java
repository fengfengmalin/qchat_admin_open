package com.qunar.chat.service;


import com.qunar.chat.common.business.ServiceStatusEnum;
import com.qunar.chat.common.util.HttpClientUtils;
import com.qunar.chat.common.util.JID;
import com.qunar.chat.common.util.JacksonUtils;
import com.qunar.chat.config.Config;
import com.qunar.chat.dao.QueueMappingDao;
import com.qunar.chat.dao.SeatDao;
import com.qunar.chat.entity.Seat;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SeatService {
    private static final Logger logger = LoggerFactory.getLogger(SeatService.class);

    @Autowired
    SeatDao seatDao;
    @Autowired
    QueueMappingDao queueMappingDao;

    public Seat siftSeat(Long shopId) {
        List<Seat> seats = seatDao.selectOnlineSeatsByShopID(shopId, Config.QCHAT_DEFAULT_HOST);
        List<Seat> filterSeat = new ArrayList<>();
        seats.stream().forEach(seat -> {
            if (seat.getServiceStatus() == ServiceStatusEnum.SUPER_MODE.getKey()) {
                filterSeat.add(seat);
            } else if (seat.getServiceStatus() == ServiceStatusEnum.STANDARD_MODE.getKey()) {
//                if (judgeOnline(seat.getQunarName())) {// 标准服务模式下判断用户是否在线
                int serviceCount = queueMappingDao.selectSeatServiceCount(seat.getQunarName().toBareJID());
                if (serviceCount < seat.getMaxServiceCount())
                    filterSeat.add(seat);
//                }
            }
        });
        logger.info("SeatService siftSeat filterSeat", JacksonUtils.obj2String(filterSeat));
        return CollectionUtils.isNotEmpty(filterSeat) ? filterSeat.get((int) (Math.random() * filterSeat.size())) : null;
    }

    // 判断当前用户是否在线
    private boolean judgeOnline(JID seatName) {
        String s = HttpClientUtils.get("");
        Map<String, Object> mapper = JacksonUtils.string2Map(s);

        if (MapUtils.isNotEmpty(mapper)) {
            for (Map.Entry<String, Object> item : mapper.entrySet()) {
                if (item.getKey().contains("iOS") || item.getKey().contains("PC")
                        || item.getKey().contains("Mac") || item.getKey().contains("Andro"))
                    return true;
            }
        }
        return false;
    }

    public List<Seat> queryCsrByQunarNameAndShopId(String qunarName, long shopId, String host) {
        List<Seat> csrList = seatDao.selectSeatBySeatNameAndShopId(qunarName, shopId, host);
        return csrList;
    }
}

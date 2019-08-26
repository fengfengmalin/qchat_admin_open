package com.qunar.qchat.admin.service.impl;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.dao.seat.SeatNewDao;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.Robot;
import com.qunar.qchat.admin.model.SupplierWithRobot;
import com.qunar.qchat.admin.service.IRobotService;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qtalk.ss.utils.common.CacheHelper;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.utils.common.JsonUtil;
import com.qunar.qtalk.ss.consult.QtQueueDao;
import com.qunar.qtalk.ss.consult.QueueMappingDao;
import com.qunar.qtalk.ss.consult.entity.QtSessionItem;
import com.qunar.qtalk.ss.consult.entity.QtSessionKey;
import com.qunar.qtalk.ss.sift.entity.QueueMapping;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yinmengwang on 17-8-17.
 */
@Service("robotService")
public class RobotServiceImpl implements IRobotService {

    private static final Logger logger = LoggerFactory.getLogger(RobotServiceImpl.class);

    @Resource
    private SeatNewDao seatNewDao;
    @Autowired
    QueueMappingDao queueMappingDao;


    @Override
    public Robot getRobotByRobotid(String robotid) {
        if (Strings.isNullOrEmpty(robotid))
            return null;
        return seatNewDao.getRobotByRobotid(robotid);
    }

    @Override
    public Robot getRobotWithConfigById(String robotid) {
        if (Strings.isNullOrEmpty(robotid))
            return null;
        return seatNewDao.getRobotWithConfigById(robotid);
    }

    @Override
    public boolean saveRobot(Robot robot) {
        if (robot == null || robot.getBusinessId() <= 0 || Strings.isNullOrEmpty(robot.getRobotId())) {
            return false;
        }
        return seatNewDao.saveRobotInfo(robot) > 0;
    }

    @Override
    public Robot getRobotByBusiness(BusinessEnum businessEnum) {
        if (businessEnum == null || businessEnum == BusinessEnum.EMPTY) {
            return null;
        }
        return seatNewDao.getRobotByBusinessId(businessEnum.getId());
    }

    @Override
    public Robot getRobotById(String robotid) {
        if (Strings.isNullOrEmpty(robotid))
            return null;

        return seatNewDao.getRobotById(robotid);
    }

    @Override
    public boolean updateOrInsertSupplierRobotConfig(SupplierWithRobot supplierWithRobot) {
        if (null == supplierWithRobot || supplierWithRobot.getId() <= 0)
            return false;
        List<SupplierWithRobot> supplierWithRobotList
                = seatNewDao.qunarSupplierWithRobot(supplierWithRobot.getRobot_id(), supplierWithRobot.getId());
        if (CollectionUtil.isEmpty(supplierWithRobotList)) {
            return seatNewDao.insertSupplierRobotConfig(supplierWithRobot);
        } else {
            if (supplierWithRobot.getStrategy() == 3) {
                List<QueueMapping> queueMappings = queueMappingDao.selectMappingByShopId(supplierWithRobot.getId());
                if(CollectionUtils.isNotEmpty(queueMappings)) {
                    closeOldSession(queueMappings);
                }

            }
            return seatNewDao.updateSupplierRobotConfig(supplierWithRobot);
        }
    }

    @Override
    public SupplierWithRobot getRobotConfig(String robotname, long supplierid) {
        if ( supplierid <= 0)
            return null;
        List<SupplierWithRobot> supplierWithRobotList
                = seatNewDao.qunarSupplierWithRobot(robotname, supplierid);
        if (CollectionUtil.isEmpty(supplierWithRobotList))
            return null;
        return supplierWithRobotList.get(0);
    }

    private void closeOldSession(List<QueueMapping> queueMappings) {
        queueMappings.stream().forEach(queueMapping -> {
            JID userJid = JID.parseAsJID(queueMapping.getCustomerName());
            long shopId = queueMapping.getShopId();
            JID seatName = JID.parseAsJID(queueMapping.getSeatName());
            QtSessionKey sessionKey = QtQueueDao.getInstance().closeSession(userJid, shopId, seatName);
            logger.info("closeOldSession queueMapping {} {}", queueMapping.getCustomerName(), shopId);
            QtSessionItem var = QtSessionItem.parseFromRedis(sessionKey);

            if (var == null)
                logger.debug("impossable this is no session in my cache!{}", JacksonUtil.obj2String(sessionKey));
            else {
                CacheHelper.remove(CacheHelper.CacheType.SeatCache, sessionKey.getRedisKey());
                CacheHelper.remove(CacheHelper.CacheType.SeatCache, sessionKey.getReleasedKey());

                LinkedList<QtSessionKey> lists = QtSessionKey.parseFromRedisToLinkedList(String.format("fromtoMapping:%s", seatName.toBareJID()));

                logger.debug("close session, seatName={}, list is {}", JacksonUtil.obj2String(seatName), JacksonUtil.obj2String(lists));

                if (CollectionUtils.isNotEmpty(lists)) {
                    lists.remove(sessionKey);
                    CacheHelper.set(CacheHelper.CacheType.SeatCache, String.format("fromtoMapping:%s", seatName.toBareJID()), JsonUtil.obj2String(lists), 1, TimeUnit.DAYS);
                }
            }
        });
    }
}

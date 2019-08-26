package com.qunar.qtalk.ss.consult;

import com.google.common.eventbus.AsyncEventBus;
import com.qunar.qtalk.ss.constants.Config;
import com.qunar.qtalk.ss.consult.entity.*;
import com.qunar.qtalk.ss.utils.CheckJobUtil;
import com.qunar.qtalk.ss.utils.JID;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class QtSessionWatcher {

//    private Timer clearSessionTimer;
    private long clearSessionTime = 0;

//    private static final long COLLECTION_TIME_OUT = 60 * 60 * 1000 * 24;
//    private static final long TIME_OUT = 60 * 1000 * 15;
    private ScheduledExecutorService schedule = Executors.newScheduledThreadPool(2);

    private static final Logger logger = LoggerFactory.getLogger(QtSessionWatcher.class);
    public AsyncEventBus eventBus = new AsyncEventBus(QtSessionWatcher.class.getName(), Executors.newFixedThreadPool(3));

    public void reloadCache() {
        eventBus.post(new SessionAction(Action.RemoveAll));
        eventBus.post(new QueueAction(Action.RemoveAll));
        eventBus.post(new SessionTimeoutAction(Action.RemoveAll));

        eventBus.post(new ClearAction());

        Timestamp timedout = new Timestamp(System.currentTimeMillis() - Config.Consult_RecycledTime);

        Collection<QtSessionItem> sessionItems = QtQueueDao.getInstance().getValidSessions(timedout);

        eventBus.post(new SessionAction(Action.Add, sessionItems));

        Collection<QtQueueItem> queueItems = QtQueueDao.getInstance().getValidQueues(timedout);

        eventBus.post(new QueueAction(Action.Add, queueItems));
    }

    private static class Holder {
        private static final QtSessionWatcher INSTANCE = new QtSessionWatcher();
    }

    public static QtSessionWatcher getInstance() {
        return QtSessionWatcher.Holder.INSTANCE;
    }



    public void initialize() {
       // reloadCache();

        setupTimer();
        logger.info("QtSessionWatcher is initialized!");
    }


    public void setupTimer() {

        if (!CheckJobUtil.checkRun())
            return;

//        if (clearSessionTimer != null)
//            clearSessionTimer.cancel();
//
//
//        clearSessionTimer = new Timer();
        schedule.scheduleAtFixedRate(()-> {
//            @Override
//            public void run() {
                try {
                    long theTime = System.currentTimeMillis();
                    Timestamp timedout1 = new Timestamp(theTime);
                    Timestamp timedout2 = new Timestamp(theTime - (Config.Consult_CustomerNotifyTime + 60000));
                    logger.debug("begin to refrash timeout list, {} times, timeout is {} -> {}", clearSessionTime++, timedout2, timedout1);

                    Collection<QtSessionItem> validSessionList = QtQueueDao.getInstance().getValidSessions(timedout2, timedout1);

                    Timestamp timedout = new Timestamp(theTime - Config.Consult_RecycledTime);

                    logger.debug("close timeouted sessions, timeout is {}", timedout);

                    Collection<QtSessionItem> sessionList = QtQueueDao.getInstance().finishSessions(timedout);

                    if (CollectionUtils.isNotEmpty(sessionList)) {
                        QtQueueDao.getInstance().removeTimeoutSessions(sessionList);
                        eventBus.post(new SessionAction(Action.Remove, sessionList));
                    }
                    Long inQueue = QtQueueDao.getInstance().inQueue();
                    Collection<QtQueueItem> queueList = QtQueueDao.getInstance().getTimeoutQtQueueItem(timedout);

                    if (CollectionUtils.isNotEmpty(queueList)) {
                        eventBus.post(new QueueAction(Action.Remove, queueList));
                        QtQueueDao.getInstance().removeTimeoutQueues(queueList);
                    }

                    timedout = new Timestamp(theTime - Config.Consult_ReleasedTime);

                    logger.debug("close released sessions, timeout is {}", timedout);

                    Collection<QtSessionItem> timeoutdSessionList = QtQueueDao.getInstance().getTimeoutSessions(timedout);

                    if (CollectionUtils.isNotEmpty(timeoutdSessionList)) {
                        eventBus.post(new SessionTimeoutAction(Action.Remove, timeoutdSessionList));
                    }

                    logger.debug("I got {} session valid!", validSessionList.size());
                    for (QtSessionItem item : validSessionList) {
                        Timestamp now = new Timestamp(theTime);

                        long subvalue = now.getTime() - item.getTime().getTime();

                        if (subvalue > Config.Consult_SeatsNotifyTime && subvalue < (Config.Consult_SeatsNotifyTime + 60000) && item.getStatus() == QtQueueStatus.CustomerLast) {
                            //
                            // 2分钟逻辑
                            logger.debug("catch a 2 min message: {} - {} - {}", subvalue, Config.Consult_SeatsNotifyTime, item.getStatus());

                            String supplierId = SpringComponents.components.hotlineSupplierService.selectHotlineBySupplierId(item.getShopJid());
                            JID from = StringUtils.isNotEmpty(supplierId) ? JID.parseAsJID(supplierId) : item.getShopJid();
                            ConsultUtils.sendMessage(from, item.getSeatQunarName(), item.getUserName(), item.getSeatQunarName(), "您有用户消息未回复，请及时处理", true, true, false, true);
                        } else if (subvalue > Config.Consult_CustomerNotifyTime && subvalue < (Config.Consult_CustomerNotifyTime + 60000) && item.getStatus() == QtQueueStatus.SeatLast) {
                            //
                            // 4分钟逻辑
                            logger.debug("catch a 4 min message: {} - {} - {}", subvalue, Config.Consult_CustomerNotifyTime, item.getStatus());
                            String supplierId = SpringComponents.components.hotlineSupplierService.selectHotlineBySupplierId(item.getShopJid());
                            JID from = StringUtils.isNotEmpty(supplierId) ? JID.parseAsJID(supplierId) : item.getShopJid();
                            ConsultUtils.sendMessage(from, item.getUserName(), item.getSeatQunarName(), item.getUserName(), "小驼先去服务其他人喽，您有任何疑问，可以随时咨询，小驼会第一时间处理", false, false, true);
                        }
                    }
                } catch (Exception e) {
                    logger.error("time schedule crashed", e);
                    schedule.shutdown();
                    initialize();
                }

        }, 0, 60, TimeUnit.SECONDS);
    }
}

package com.qunar.chat.service;

import com.google.common.eventbus.AsyncEventBus;
import com.qunar.chat.common.business.*;
import com.qunar.chat.common.util.ConsultUtils;
import com.qunar.chat.common.util.JID;
import com.qunar.chat.config.Config;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class QtSessionWatcherService {

    private long clearSessionTime = 0;

    private ScheduledExecutorService schedule = Executors.newScheduledThreadPool(2);

    private static final Logger logger = LoggerFactory.getLogger(QtSessionWatcherService.class);
    public AsyncEventBus eventBus = new AsyncEventBus(QtSessionWatcherService.class.getName(), Executors.newFixedThreadPool(3));

    @Autowired
    public QueueMappingService queueMappingService;


    public void initialize() {
        reloadCache();
        setupTimer();
        logger.info("QtSessionWatcherService is initialized!");
    }

    private void reloadCache() {
        eventBus.post(new SessionAction(Action.RemoveAll));
        eventBus.post(new QueueAction(Action.RemoveAll));
        eventBus.post(new SessionTimeoutAction(Action.RemoveAll));

        eventBus.post(new ClearAction());

        Timestamp timedout = new Timestamp(System.currentTimeMillis() - Config.Consult_RecycledTime);

        Collection<QtSessionItem> sessionItems = queueMappingService.getValidSessions(timedout);

        eventBus.post(new SessionAction(Action.Add, sessionItems));

        Collection<QtQueueItem> queueItems = queueMappingService.getValidQueues(timedout);

        eventBus.post(new QueueAction(Action.Add, queueItems));
        logger.info("reloadCache success");
    }


    public void setupTimer() {

        schedule.scheduleAtFixedRate(()-> {
                try {
                    long theTime = System.currentTimeMillis();
                    Timestamp timedout1 = new Timestamp(theTime);
                    Timestamp timedout2 = new Timestamp(theTime - (Config.Consult_CustomerNotifyTime + 60000));
                    logger.debug("begin to refrash timeout list, {} times, timeout is {} -> {}", clearSessionTime++, timedout2, timedout1);

                    Collection<QtSessionItem> validSessionList = queueMappingService.getValidSessions(timedout2, timedout1);

                    Timestamp timedout = new Timestamp(theTime - Config.Consult_RecycledTime);

                    logger.debug("close timeouted sessions, timeout is {}", timedout);

                    Collection<QtSessionItem> sessionList = queueMappingService.finishSessions(timedout);

                    if (CollectionUtils.isNotEmpty(sessionList)) {
                        queueMappingService.removeTimeoutSessions(sessionList);
                        eventBus.post(new SessionAction(Action.Remove, sessionList));
                    }
                    Collection<QtQueueItem> queueList = queueMappingService.getTimeoutQtQueueItem(timedout);

                    if (CollectionUtils.isNotEmpty(queueList)) {
                        eventBus.post(new QueueAction(Action.Remove, queueList));
                        queueMappingService.removeTimeoutQueues(queueList);
                    }

                    timedout = new Timestamp(theTime - Config.Consult_ReleasedTime);

                    logger.debug("close released sessions, timeout is {}", timedout);

                    Collection<QtSessionItem> timeoutdSessionList = queueMappingService.getTimeoutSessions(timedout);

                    if (CollectionUtils.isNotEmpty(timeoutdSessionList)) {
                        eventBus.post(new SessionTimeoutAction(Action.Remove, timeoutdSessionList));
                    }

                    logger.debug("I got {} session valid!", validSessionList.size());
                    for (QtSessionItem item : validSessionList) {
                        Timestamp now = new Timestamp(theTime);

                        long subvalue = now.getTime() - item.getTime().getTime();

                        if (subvalue > Config.Consult_SeatsNotifyTime && subvalue < (Config.Consult_SeatsNotifyTime + 60000) && item.getStatus() == QtQueueStatus.CustomerLast) {

                            // 2分钟逻辑未回复提示客服
                            logger.debug("catch a 2 min message: {} - {} - {}", subvalue, Config.Consult_SeatsNotifyTime, item.getStatus());

                            JID from = item.getShopJid();
                            ConsultUtils.sendMessage(from, item.getSeatQunarName(), item.getUserName(), item.getSeatQunarName(), "您有用户消息未回复，请及时处理", true, true, false, true);
                        } else if (subvalue > Config.Consult_CustomerNotifyTime && subvalue < (Config.Consult_CustomerNotifyTime + 60000) && item.getStatus() == QtQueueStatus.SeatLast) {
                            //
                            // 4(线上15分钟)分钟逻辑 提示用户服务其他人
                            logger.debug("catch a 4 min message: {} - {} - {}", subvalue, Config.Consult_CustomerNotifyTime, item.getStatus());
                            JID from = item.getShopJid();
                            ConsultUtils.sendMessage(from, item.getUserName(), item.getSeatQunarName(), item.getUserName(), "小驼先去服务其他人喽，您有任何疑问，可以随时咨询，小驼会第一时间处理", false, false, true);
                        }
                    }
                } catch (Exception e) {
                    logger.error("time schedule crashed", e);
                    schedule.shutdown();
                    setupTimer();
                }

        }, 0, 60, TimeUnit.SECONDS);
    }
}

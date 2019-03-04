package com.qunar.chat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class CustomerListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    QtSessionWatcherService qtSessionWatcherService;
    @Autowired
    QueueManagerService queueManagerService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        qtSessionWatcherService.initialize();
        queueManagerService.initialize();
    }
}

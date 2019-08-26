package com.qunar.qtalk.ss.consult.entity;

import java.util.Collection;

public class QueueAction {
    private final Collection<QtQueueItem> queueList;
    private final Action action;

    public QueueAction(Action action, Collection<QtQueueItem> queueList) {
        this.queueList = queueList;
        this.action = action;
    }

    public QueueAction(Action action) {
        this.action = action;
        this.queueList = null;
    }

    public Collection<QtQueueItem> getQueueList() {
        return queueList;
    }

    public Action getAction() {
        return action;
    }
}

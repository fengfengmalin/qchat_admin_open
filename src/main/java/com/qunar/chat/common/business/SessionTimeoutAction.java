package com.qunar.chat.common.business;

import java.util.Collection;

public class SessionTimeoutAction {
    private final Action action;
    private final Collection<QtSessionItem> actionItems;

    public SessionTimeoutAction(Action action) {
        this.action = action;
        actionItems = null;
    }

    public SessionTimeoutAction(Action action, Collection<QtSessionItem> timeoutdSessionList) {
        this.action = action;
        actionItems = timeoutdSessionList;

    }

    public Collection<QtSessionItem> getActionItems() {
        return actionItems;
    }

    public Action getAction() {
        return action;
    }
}

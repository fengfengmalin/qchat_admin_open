package com.qunar.chat.common.business;

import java.util.Collection;

public class SessionAction {

    private final Collection<QtSessionItem> sessionIds;
    private final Action action;

    public SessionAction(Action action, Collection<QtSessionItem> sessionIds) {
        this.sessionIds = sessionIds;
        this.action = action;
    }

    public SessionAction(Action action) {
        this.action = action;
        sessionIds = null;
    }

    public Collection<QtSessionItem> getSessionIds() {
        return sessionIds;
    }

    public Action getAction() {
        return action;
    }
}

package com.qunar.qchat.admin.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qyhw on 12/6/15.
 */
public class SessionListResultVO {

    private int totalCount;

    private List<SessionVO> sessionList = new ArrayList<SessionVO>();

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<SessionVO> getSessionList() {
        return sessionList;
    }

    public void setSessionList(List<SessionVO> sessionList) {
        this.sessionList = sessionList;
    }
}

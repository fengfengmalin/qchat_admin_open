package com.qunar.qchat.admin.model;

import com.google.common.collect.Lists;
import com.qunar.qchat.admin.util.CollectionUtil;

import java.util.List;

public enum SessionStateEnum {
        UNKNOW_STATE(0,"未知"),
        STATE_PREASSIGN(1,"预分配"),
        STATE_LEAVEMESSAGE(2,"留言中"),
        STATE_QUEUE(3,"队列中"),
        STATE_ASSIGNED(4,"已经分配"),
        STATE_TRANSED(5,"已经转移"),
        STATE_STOPED(6,"已断开"),
        STATE_FINISHED(7,"已经完成")
    ;

    SessionStateEnum(int state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    private int state;
    private String desc;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public static   SessionStateEnum valueOf(int state){
        List<SessionStateEnum> valueList = getAllState();
        if (!CollectionUtil.isEmpty(valueList)){
            for (SessionStateEnum e : valueList){
                if (e.state == state)
                    return e;
            }
        }

        return UNKNOW_STATE;
    }
    private static List<SessionStateEnum>  getAllState(){
        List<SessionStateEnum> valueList = Lists.newArrayList();
        valueList.add(STATE_PREASSIGN);
        valueList.add(STATE_LEAVEMESSAGE);
        valueList.add(STATE_QUEUE);
        valueList.add(STATE_ASSIGNED);
        valueList.add(STATE_TRANSED);
        valueList.add(STATE_STOPED);
        valueList.add(STATE_FINISHED);
        return  valueList;
    }
}

package com.qunar.qchat.admin.model.responce;

import com.qunar.qchat.admin.vo.SessionMessageVO;

import java.util.List;

public class SessionDetailResponce extends BaseResponce{
    private List<SessionMessageVO> data;

    public List<SessionMessageVO> getData() {
        return data;
    }

    public void setData(List<SessionMessageVO> data) {
        this.data = data;
    }
}

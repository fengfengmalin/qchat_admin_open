package com.qunar.qchat.admin.vo;

import java.util.List;

/**
 * 客服
 * Created by qyhw on 10/21/15.
 */
public class SeatListSubVO extends SeatBaseVO{

    private List<BusinessVO> busiList;  // 所属业务

    public List<BusinessVO> getBusiList() {
        return busiList;
    }

    public void setBusiList(List<BusinessVO> busiList) {
        this.busiList = busiList;
    }
}

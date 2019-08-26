package com.qunar.qchat.admin.vo;

import java.util.List;

/**
 * 客服组
 * Created by qyhw on 10/21/15.
 */
public class SeatGroupListVO {
    private int totalCount;
    private int pageSise = 15;
    private int pageNum = 1;

    private List<SeatGroupVO> groupList;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPageSise() {
        return pageSise;
    }

    public void setPageSise(int pageSise) {
        this.pageSise = pageSise;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public List<SeatGroupVO> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<SeatGroupVO> groupList) {
        this.groupList = groupList;
    }
}

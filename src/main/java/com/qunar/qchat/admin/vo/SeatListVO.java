package com.qunar.qchat.admin.vo;

import java.util.List;

/**
 * 客服
 * Created by qyhw on 10/21/15.
 */
public class SeatListVO {
    private long totalCount;
    private int pageSise = 15;
    private int pageNum = 1;

    private List<SeatListSubVO> seatList;

    public SeatListVO() {

    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
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

    public List<SeatListSubVO> getSeatList() {
        return seatList;
    }

    public void setSeatList(List<SeatListSubVO> seatList) {
        this.seatList = seatList;
    }
}

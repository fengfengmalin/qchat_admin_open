package com.qunar.qchat.admin.vo;

import java.util.ArrayList;
import java.util.List;

public class SupplierListVO {
    private long totalCount = 0;
    private int pageSise = 15;
    private int pageNum = 1;
    private List<SupplierVO> supplierList = new ArrayList<>();

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

    public List<SupplierVO> getSupplierList() {
        return supplierList;
    }

    public void setSupplierList(List<SupplierVO> supplierList) {
        this.supplierList = supplierList;
    }
}

package com.qunar.qchat.admin.vo;

import com.qunar.qchat.admin.model.BusinessEnum;

import java.util.List;

/**
 * Created by qyhw on 1/14/16.
 */
public class SysUserVO {

    // 管理员名字
    private String qunarName;

    // 当前业务类型
    private BusinessEnum bType;

    // 管理员管理的所有供应商
    private List<SupplierVO> allSuList;

    // 管理员管理的当前业务下的所有供应商
    private List<SupplierVO> curBuSuList;

    public String getQunarName() {
        return qunarName;
    }

    public void setQunarName(String qunarName) {
        this.qunarName = qunarName;
    }

    public BusinessEnum getbType() {
        return bType;
    }

    public void setbType(BusinessEnum bType) {
        this.bType = bType;
    }

    public List<SupplierVO> getAllSuList() {
        return allSuList;
    }

    public void setAllSuList(List<SupplierVO> allSuList) {
        this.allSuList = allSuList;
    }

    public List<SupplierVO> getCurBuSuList() {
        return curBuSuList;
    }

    public void setCurBuSuList(List<SupplierVO> curBuSuList) {
        this.curBuSuList = curBuSuList;
    }
}

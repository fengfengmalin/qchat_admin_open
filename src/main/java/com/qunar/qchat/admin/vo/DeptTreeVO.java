package com.qunar.qchat.admin.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qyhw on 10/22/15.
 */
public class DeptTreeVO {

    private long id;

    @JsonProperty("D")
    private String name;

    @JsonProperty("SD")
    private List<DeptTreeVO> subDeptList = new ArrayList<DeptTreeVO>();

    @JsonProperty("UL")
    private List<DeptUserVO> userList = new ArrayList<DeptUserVO>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DeptTreeVO> getSubDeptList() {
        return subDeptList;
    }

    public void setSubDeptList(List<DeptTreeVO> subDeptList) {
        this.subDeptList = subDeptList;
    }

    public List<DeptUserVO> getUserList() {
        return userList;
    }

    public void setUserList(List<DeptUserVO> userList) {
        this.userList = userList;
    }
}

package com.qunar.qchat.admin.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qunar.qchat.admin.model.SeatGroup;

import java.util.List;

/**
 * Created by qyhw on 10/23/15.
 */
public class BusinessVO {

    @JsonProperty("busiId")
    private int id;

    @JsonProperty("busiName")
    private String name;

    private List<SeatGroup> groupList;

    public List<SeatGroup> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<SeatGroup> groupList) {
        this.groupList = groupList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

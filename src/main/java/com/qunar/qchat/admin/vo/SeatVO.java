package com.qunar.qchat.admin.vo;

import com.qunar.qchat.admin.model.Business;
import com.qunar.qchat.admin.model.SeatGroup;
import com.qunar.qchat.admin.util.CollectionUtil;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 客服
 * Created by qyhw on 10/21/15.
 */
public class SeatVO extends SeatBaseVO{

    private List<Business> busiList;  // 所属业务
    private List<SeatGroup> groupList;  // 所属组

    public SeatVO() {

    }

    public List<Business> getBusiList() {
        return busiList;
    }

    public void setBusiList(List<Business> busiList) {
        this.busiList = busiList;
    }

    public List<SeatGroup> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<SeatGroup> groupList) {
        this.groupList = groupList;
    }

    public String toString(){
        StringBuffer bf = new StringBuffer();
        bf.append("id: ");
        bf.append(this.getId());
        bf.append(",");
        bf.append("qunarName: ");
        bf.append(this.getQunarName());
        bf.append(",");
        bf.append("webName: ");
        bf.append(this.getWebName());

        if (CollectionUtil.isNotEmpty(groupList)) {
            bf.append(",");
            bf.append("groupList: (");
            for (SeatGroup sg : groupList) {
                bf.append(String.valueOf(sg.getId()));
                bf.append(" ");
            }
            bf.append(" )");
        }
        return bf.toString();
    }
}

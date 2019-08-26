package com.qunar.qchat.admin.vo;

import java.util.List;

/**
 * Created by qyhw on 12/29/15.
 */
public class UrlIpMappingVO {

    private String url;

    private List<String> ipList;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }
}

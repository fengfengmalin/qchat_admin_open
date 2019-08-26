package com.qunar.qchat.admin.vo;

import java.util.List;

/**
 * Created by qyhw on 12/29/15.
 */
public class AccessControlVO {

    /** 优先级最高  */
    private List<String> denyIp;

    private List<UrlIpMappingVO> allowIp;

    public List<String> getDenyIp() {
        return denyIp;
    }

    public void setDenyIp(List<String> denyIp) {
        this.denyIp = denyIp;
    }

    public List<UrlIpMappingVO> getAllowIp() {
        return allowIp;
    }

    public void setAllowIp(List<UrlIpMappingVO> allowIp) {
        this.allowIp = allowIp;
    }
}

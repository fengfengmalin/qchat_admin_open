package com.qunar.qchat.admin.model;

import java.util.Date;

/**
 * 模板
 * Created by qyhw on 11/25/15.
 */
public class PageTemplate {

    private int id;

    private String name;

    private String pageCss;

    private String pageHtml;

    private int busiType;

    private Date createTime;

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

    public String getPageCss() {
        return pageCss;
    }

    public void setPageCss(String pageCss) {
        this.pageCss = pageCss;
    }

    public String getPageHtml() {
        return pageHtml;
    }

    public void setPageHtml(String pageHtml) {
        this.pageHtml = pageHtml;
    }

    public int getBusiType() {
        return busiType;
    }

    public void setBusiType(int busiType) {
        this.busiType = busiType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

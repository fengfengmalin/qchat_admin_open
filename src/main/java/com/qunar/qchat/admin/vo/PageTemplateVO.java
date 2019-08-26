package com.qunar.qchat.admin.vo;

/**
 * 模板
 * Created by qyhw on 11/25/15.
 */
public class PageTemplateVO{

    private int id;

    private String name;

    private String pageCss;

    private String pageHtml;

    private String busiName;

    private int busiType;

    private long createTime;

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

    public String getBusiName() {
        return busiName;
    }

    public void setBusiName(String busiName) {
        this.busiName = busiName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getBusiType() {
        return busiType;
    }

    public void setBusiType(int busiType) {
        this.busiType = busiType;
    }
}

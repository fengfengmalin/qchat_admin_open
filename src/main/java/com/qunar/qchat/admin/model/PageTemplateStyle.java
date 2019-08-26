package com.qunar.qchat.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 页面模板.
 * Created by qyhw on 11/17/15.
 */
public class PageTemplateStyle {

    private int templateId;
    private String pageType;
    private String pageCss;
    private String pageHtml;

    @JsonProperty("busiType")
    private int busiId;

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public String getPageType() {
        return pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
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

    public int getBusiId() {
        return busiId;
    }

    public void setBusiId(int busiId) {
        this.busiId = busiId;
    }
}

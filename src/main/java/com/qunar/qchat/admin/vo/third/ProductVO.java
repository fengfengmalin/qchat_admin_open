package com.qunar.qchat.admin.vo.third;

/**
 * 封装各个业务线产品信息
 * Created by qyhw on 12/17/15.
 */
public class ProductVO {

    private String title = "";

    private String type = "";

    private String tag = "";

    private String imageUrl= "";

    private String price= "";
    private String marketPrice= "";

    private String appDtlUrl= "";

    private String touchDtlUrl= "";

    private String webDtlUrl= "";

    private ProductSupplierVO supplier;

    private String buTitle= "";

    private String pHtml= "";

    private String dep= "";  // 联运产品出发地  针对度假产品
    
    private String bu;

    private String productId;

    private boolean sendNoteSuccess;

    private String sendNoteMsg;

    public String getSendNoteMsg() {
        return sendNoteMsg;
    }

    public void setSendNoteMsg(String sendNoteMsg) {
        this.sendNoteMsg = sendNoteMsg;
    }

    public boolean isSendNoteSuccess() {
        return sendNoteSuccess;
    }

    public void setSendNoteSuccess(boolean sendNoteSuccess) {
        this.sendNoteSuccess = sendNoteSuccess;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAppDtlUrl() {
        return appDtlUrl;
    }

    public void setAppDtlUrl(String appDtlUrl) {
        this.appDtlUrl = appDtlUrl;
    }

    public String getTouchDtlUrl() {
        return touchDtlUrl;
    }

    public void setTouchDtlUrl(String touchDtlUrl) {
        this.touchDtlUrl = touchDtlUrl;
    }

    public String getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(String marketPrice) {
        this.marketPrice = marketPrice;
    }

    public String getWebDtlUrl() {
        return webDtlUrl;
    }

    public void setWebDtlUrl(String webDtlUrl) {
        this.webDtlUrl = webDtlUrl;
    }

    public ProductSupplierVO getSupplier() {
        return supplier;
    }

    public void setSupplier(ProductSupplierVO supplier) {
        this.supplier = supplier;
    }

    public String getBuTitle() {
        return buTitle;
    }

    public void setBuTitle(String buTitle) {
        this.buTitle = buTitle;
    }

    public String getpHtml() {
        return pHtml;
    }

    public void setpHtml(String pHtml) {
        this.pHtml = pHtml;
    }

    public String getDep() {
        return dep;
    }

    public void setDep(String dep) {
        this.dep = dep;
    }

    public String getBu() {
        return bu;
    }

    public void setBu(String bu) {
        this.bu = bu;
    }
}

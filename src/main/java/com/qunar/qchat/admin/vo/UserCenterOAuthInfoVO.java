package com.qunar.qchat.admin.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Author : mingxing.shao
 * Date : 15-10-22
 *
 */
public class UserCenterOAuthInfoVO {
    private String domian;
    @JsonProperty("oauthemail")
    private String oAuthEmail;
    @JsonProperty("oauthgender")
    private int oAuthGender;
    @JsonProperty("oauthimageurl")
    private String oAuthImageUrl;
    @JsonProperty("oauthmobile")
    private String oAuthMobile;
    @JsonProperty("oauthname")
    private String oAuthName;
    @JsonProperty("oauthnickname")
    private String oAuthNickname;
    @JsonProperty("oauthtmpnickname")
    private String oAuthTmpNickname;

    public String getDomian() {
        return domian;
    }

    public void setDomian(String domian) {
        this.domian = domian;
    }

    public String getoAuthEmail() {
        return oAuthEmail;
    }

    public void setoAuthEmail(String oAuthEmail) {
        this.oAuthEmail = oAuthEmail;
    }

    public String getoAuthImageUrl() {
        return oAuthImageUrl;
    }

    public void setoAuthImageUrl(String oAuthImageUrl) {
        this.oAuthImageUrl = oAuthImageUrl;
    }

    public int getoAuthGender() {
        return oAuthGender;
    }

    public void setoAuthGender(int oAuthGender) {
        this.oAuthGender = oAuthGender;
    }

    public String getoAuthMobile() {
        return oAuthMobile;
    }

    public void setoAuthMobile(String oAuthMobile) {
        this.oAuthMobile = oAuthMobile;
    }

    public String getoAuthName() {
        return oAuthName;
    }

    public void setoAuthName(String oAuthName) {
        this.oAuthName = oAuthName;
    }

    public String getoAuthTmpNickname() {
        return oAuthTmpNickname;
    }

    public void setoAuthTmpNickname(String oAuthTmpNickname) {
        this.oAuthTmpNickname = oAuthTmpNickname;
    }

    public String getoAuthNickname() {
        return oAuthNickname;
    }

    public void setoAuthNickname(String oAuthNickname) {
        this.oAuthNickname = oAuthNickname;
    }
}

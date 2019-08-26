package com.qunar.qchat.admin.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Author : mingxing.shao
 * Date : 15-10-22
 *
 */
public class UserCenterInfoVO {
    @JsonProperty("appflag")
    private int appFlag;
    private String birthday;
    private int credit;
    private String email;
    @JsonProperty("emailverified")
    private int emailVerified;
    private int gender;
    @JsonProperty("imageurl")
    private String imageUrl;
    @JsonProperty("loginip")
    private long loginIp;
    @JsonProperty("logintime")
    private long loginTime;
    private String mobile;
    @JsonProperty("mobileverified")
    private int mobileVerified;
    private String nickname;
    @JsonProperty("oauth")
    private UserCenterOAuthInfoVO oAuthInfo;
    private String prenum;
    private int quickFlag;
    private long regIp;
    private long regTime;
    private int status;
    @JsonProperty("tempnickname")
    private String tempNickname;
    private int type;
    private long uid;
    @JsonProperty("userName")
    private String userName;

    public int getAppFlag() {
        return appFlag;
    }

    public void setAppFlag(int appFlag) {
        this.appFlag = appFlag;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(int emailVerified) {
        this.emailVerified = emailVerified;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(long loginIp) {
        this.loginIp = loginIp;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getMobileVerified() {
        return mobileVerified;
    }

    public void setMobileVerified(int mobileVerified) {
        this.mobileVerified = mobileVerified;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public UserCenterOAuthInfoVO getoAuthInfo() {
        return oAuthInfo;
    }

    public void setoAuthInfo(UserCenterOAuthInfoVO oAuthInfo) {
        this.oAuthInfo = oAuthInfo;
    }

    public int getQuickFlag() {
        return quickFlag;
    }

    public void setQuickFlag(int quickFlag) {
        this.quickFlag = quickFlag;
    }

    public String getPrenum() {
        return prenum;
    }

    public void setPrenum(String prenum) {
        this.prenum = prenum;
    }

    public long getRegIp() {
        return regIp;
    }

    public void setRegIp(long regIp) {
        this.regIp = regIp;
    }

    public long getRegTime() {
        return regTime;
    }

    public void setRegTime(long regTime) {
        this.regTime = regTime;
    }

    public String getTempNickname() {
        return tempNickname;
    }

    public void setTempNickname(String tempNickname) {
        this.tempNickname = tempNickname;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

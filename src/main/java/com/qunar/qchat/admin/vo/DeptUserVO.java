package com.qunar.qchat.admin.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by qyhw on 10/22/15.
 */
public class DeptUserVO {

    @JsonProperty("N")
    private String nickName = "";

    @JsonProperty("U")
    private String qunarName = "";

    @JsonProperty("W")
    private String webName = "";

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getWebName() {
        return webName;
    }

    public void setWebName(String webName) {
        this.webName = webName;
    }

    public String getQunarName() {
        return qunarName;
    }

    public void setQunarName(String qunarName) {
        this.qunarName = qunarName;
    }

}

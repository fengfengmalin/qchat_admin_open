package com.qunar.qchat.admin.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qunar.qchat.admin.model.OnlineState;

/**
 * Author : mingxing.shao
 * Date : 15-10-16
 *
 */
public class SeatOnlineState {
    @JsonProperty("strid")
    private String strId;
    @JsonProperty("o")
    private OnlineState onlineState;
    @JsonProperty("rawo")
    private OnlineState rawo;

    private int servicestate;

    private String resource;

    public String getStrId() {
        return strId;
    }

    public void setStrId(String strId) {
        this.strId = strId;
    }

    @JsonSerialize(using = OnlineStateJsonAdapter.Serializer.class)
    public OnlineState getOnlineState() {
        return onlineState;
    }

    @JsonDeserialize(using = OnlineStateJsonAdapter.Deserializer.class)
    public void setOnlineState(OnlineState onlineState) {
        this.onlineState = onlineState;
    }

    @JsonSerialize(using = OnlineStateJsonAdapter.Serializer.class)
    public OnlineState getRawo() {
        return rawo;
    }

    @JsonDeserialize(using = OnlineStateJsonAdapter.Deserializer.class)
    public void setRawo(OnlineState rawo) {
        this.rawo = rawo;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public int getServicestate() {
        return servicestate;
    }

    public void setServicestate(int servicestate) {
        this.servicestate = servicestate;
    }
}

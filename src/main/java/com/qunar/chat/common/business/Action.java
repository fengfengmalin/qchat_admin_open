package com.qunar.chat.common.business;

public enum Action {
    Add("Add"),
    Remove("remove"),
    RemoveAll("removeall");

    private String action;

    Action(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
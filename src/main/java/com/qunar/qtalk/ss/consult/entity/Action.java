package com.qunar.qtalk.ss.consult.entity;

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
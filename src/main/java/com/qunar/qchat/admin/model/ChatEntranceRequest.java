package com.qunar.qchat.admin.model;

public class ChatEntranceRequest {
    private String username;
    private String qchatname;
    private String pid;
    private String vid;
    private String uid;
    private String gid;
    private String chatusername;
    private String type;


    public String getUsername() {
        if(username == null) {
            return "";
        }
        return username;
    }

    public void setUsername(String username) {
        if(username == null){
            this.username = "";
        } else {
            this.username = username;
        }
    }

    public String getQchatname() {
        if(qchatname == null) {
            return "";
        }
        return qchatname;
    }

    public void setQchatname(String qchatname) {
        if(qchatname == null) {
            this.qchatname = "";
        } else {
            this.qchatname = qchatname;
        }
    }

    public String getPid() {
        if(pid == null) {
            return "";
        }
        return pid;
    }

    public void setPid(String pid) {
        if(pid == null) {
            this.pid = "";
        } else {
            this.pid = pid;
        }
    }

    public String getVid() {
        if(vid == null) {
            return "";
        }
        return vid;
    }

    public void setVid(String vid) {
        if(vid == null) {
            this.vid = "";
        } else {
            this.vid = vid;
        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getChatusername() {
        if(chatusername == null) {
            return "";
        }
        return chatusername;
    }

    public void setChatusername(String chatusername) {
        if(chatusername == null) {
            this.chatusername = "";
        } else {
            this.chatusername = chatusername;
        }
    }

    public String getType() {
        if(type == null) {
            return "";
        }
        return type;
    }

    public void setType(String type) {
        if(type == null) {
            this.type = "";
        } else {
            this.type = type;
        }
    }

}

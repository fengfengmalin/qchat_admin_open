package com.qunar.qchat.admin.model;

/**
 * Created by yhw on 01/12/2017.
 */
public class UserSeatMapping {

    private Long id;
    private String uname;  // 用户的用户名
    private String sname;  // 客服的用户名

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

}

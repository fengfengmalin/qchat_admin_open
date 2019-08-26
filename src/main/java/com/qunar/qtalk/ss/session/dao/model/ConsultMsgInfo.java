package com.qunar.qtalk.ss.session.dao.model;

import java.util.Date;

public class ConsultMsgInfo {
    private String F;
    private String FH;
    private String T;
    private String TH;
    private String B;
    private String R;
    private Date createTime;

    public String getF() {
        return F;
    }

    public void setF(String f) {
        F = f;
    }

    public String getFH() {
        return FH;
    }

    public void setFH(String FH) {
        this.FH = FH;
    }

    public String getT() {
        return T;
    }

    public void setT(String t) {
        T = t;
    }

    public String getTH() {
        return TH;
    }

    public void setTH(String TH) {
        this.TH = TH;
    }

    public String getB() {
        return B;
    }

    public void setB(String b) {
        B = b;
    }

    public String getR() {
        return R;
    }

    public void setR(String r) {
        R = r;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        if (T.contains("@")) {
            T = T.split("@")[0];
        }
        if (F.contains("@")) {
            F = F.split("@")[0];
        }
        this.createTime = createTime;
    }
}

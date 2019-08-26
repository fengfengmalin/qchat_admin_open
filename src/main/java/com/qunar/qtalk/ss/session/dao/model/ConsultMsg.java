package com.qunar.qtalk.ss.session.dao.model;

/**
 * Created by admin on 17/07/2017.
 */
public class ConsultMsg {

    private String m_from;
    private String m_to;
    private String realfrom;
    private String realto;
    private String m_body;
    private String msg_id;
    private String create_time;
    private int read_flag;
    private String update_time;

    public String getM_from(){
        return this.m_from;
    }
    public void setM_from(String m_from){
        this.m_from = m_from;
    }

    public String getM_to(){
        return this.m_to;
    }
    public void setM_to(String m_to){
        this.m_to = m_to;
    }

    public String getRealfrom(){
        return this.realfrom;
    }

    public void setRealfrom(String realfrom){
        this.realfrom = realfrom;
    }

    public String getRealto(){
        return this.realto;
    }

    public void setRealto(String realto){
        this.realto = realto;
    }

    public String getM_body(){
        return this.m_body;
    }

    public void setM_body(String m_body){
        this.m_body = m_body;
    }

    public void setMsg_id(String msg_id){
        this.msg_id = msg_id;
    }

    public String getMsg_id(){
        return this.msg_id;
    }

    public String getUpdate_time(){
        return this.update_time;
    }

    public void setUpdate_time(String update_time){
        this.update_time = update_time;
    }

    public int getRead_flag(){
        return this.read_flag;
    }

    public void setRead_flag(int read_flag){
        this.read_flag = read_flag;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getCreate_time () {
        return this.create_time;
    }

}

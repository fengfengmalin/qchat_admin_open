package com.qunar.qchat.admin.vo;

import com.qunar.qchat.admin.model.Seat;

import java.util.List;

/**
 * 供应商
 * Created by qyhw on 10/19/15.
 */
public class SupplierVO {

    private long id;
    private String name;
    private List<String> qunarNameList;
    private int busiType;
    private String busiSupplierId;  // 业务线存储的供应商编号
    private List<Seat> seatList;
    private String robotName;
    private int robotStrategy;
    private String robotWebcome;
    private List<SupplierRobotVO> robots;
    private int ext_flag;
    private int status;
    private long createDate;
    private int assignStrategy;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getQunarNameList() {
        return qunarNameList;
    }

    public void setQunarNameList(List<String> qunarNameList) {
        this.qunarNameList = qunarNameList;
    }

    public int getBusiType() {
        return busiType;
    }

    public void setBusiType(int busiType) {
        this.busiType = busiType;
    }

    public String getBusiSupplierId() {
        return busiSupplierId;
    }

    public void setBusiSupplierId(String busiSupplierId) {
        this.busiSupplierId = busiSupplierId;
    }

    public List<Seat> getSeatList() {
        return seatList;
    }

    public void setSeatList(List<Seat> seatList) {
        this.seatList = seatList;
    }


    public int getRobotStrategy() {
        return robotStrategy;
    }

    public void setRobotStrategy(int robotStrategy) {
        this.robotStrategy = robotStrategy;
    }

    public String getRobotWebcome() {
        return robotWebcome;
    }

    public void setRobotWebcome(String robotWebcome) {
        this.robotWebcome = robotWebcome;
    }

    public List<SupplierRobotVO> getRobots() {
        return robots;
    }

    public void setRobots(List<SupplierRobotVO> robots) {
        this.robots = robots;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public void setStatus(int status){
        this.status = status;
    }
    public int getStatus(){
        return this.status;
    }

    public void setExt_flag(int ext_flag){
        this.ext_flag = ext_flag;
    }
    public int getExt_flag(){
        return this.ext_flag;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public int getAssignStrategy() {
        return assignStrategy;
    }
    public void setAssignStrategy(int assignStrategy) {
        this.assignStrategy = assignStrategy;
    }
}

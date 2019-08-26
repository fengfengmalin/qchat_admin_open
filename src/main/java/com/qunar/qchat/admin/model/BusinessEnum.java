package com.qunar.qchat.admin.model;

/**
 * 业务
 * Author : mingxing.shao
 * Date : 15-10-16
 *
 */
public enum BusinessEnum {
    EMPTY(0, "NaN", "无"),
    VACATION(1, "dujia", "度假供应商"),
    VACATION_TBS(101, "dujia-tbs", "度假驼博士"),
    VACATION_YXD(102, "dujia-yxd", "度假意向单"),
    VACATION_C2B(103, "dujia-c2b", "度假C2B"),
    FLIGHT(2, "flight", "机票供应商"),
    HOTEL(3, "hotel", "酒店供应商"),
    HOTEL_UGC(301, "hotel-ugc", "UGC酒店精包"),
    LOCAL(4, "local", "当地人"),
    MENPIAO(5, "menpiao", "门票供应商"),
    CHECHE(6, "cheche", "车车供应商"),
    JIJIU(7,"jijiu","机酒供应商"),
    HUICHANG(8,"huichang","会场"),
    INTERTRAIN(901,"interTrain","国际火车票"),
    INTERFLIGHTVISA(902,"interFlightVisa","国际机票签证"),
    QCHATHOME(1000,"QChat Home","QChat官方"),
    BUS(11,"bus","汽车票"),
    BUS_CHARTER(1101,"bus-charter","汽车票-包车"),
    BNB(1201,"bnb","客栈频道"),
    QTALK(2000,"QTalk","QTalk");

    private int id;
    private String enName;
    private String name;

    BusinessEnum(int id, String enName, String name) {
        this.enName = enName;
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEnName() {
        return enName;
    }

    public static BusinessEnum of(Integer id) {
        if (id == null) {
            return null;
        }
        for (BusinessEnum businessEnum : BusinessEnum.values()) {
            if (businessEnum.id == id) {
                return businessEnum;
            }
        }
        return null;
    }

    public static BusinessEnum of(String name) {
        for (BusinessEnum businessEnum : BusinessEnum.values()) {
            if (businessEnum.name.equals(name)) {
                return businessEnum;
            }
        }
        return null;
    }


    public static BusinessEnum ofByEnName(String name) {
        for (BusinessEnum businessEnum : BusinessEnum.values()) {
            if (businessEnum.enName.equalsIgnoreCase(name)) {
                return businessEnum;
            }
        }
        return null;
    }
}

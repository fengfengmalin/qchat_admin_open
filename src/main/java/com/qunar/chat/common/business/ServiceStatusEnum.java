package com.qunar.chat.common.business;



public enum ServiceStatusEnum {
    STANDARD_MODE(0, "标准模式"),
    DND_MODE(1, "勿扰模式"),
    SUPER_MODE(4, "超人模式"),
    UNKNOW_MODE(99, "未知模式");

    private int key;
    private String value;

    ServiceStatusEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return this.key;
    }

    public static ServiceStatusEnum of(int key){

        if (key == STANDARD_MODE.key)
            return STANDARD_MODE;
        if (key == DND_MODE.key)
            return DND_MODE;
        if (key == SUPER_MODE.key)
            return SUPER_MODE;
        return UNKNOW_MODE;
    }
    public static String getValue(int key){
        switch (key){
            case 0:
                return STANDARD_MODE.value;
            case 1:
                return DND_MODE.value;
            case 4:
                return SUPER_MODE.value;
            default:
                return UNKNOW_MODE.value;
        }
    }
}

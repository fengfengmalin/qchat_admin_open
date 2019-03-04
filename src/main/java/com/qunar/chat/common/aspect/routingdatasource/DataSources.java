package com.qunar.chat.common.aspect.routingdatasource;

/**
 * 数据源枚举
 * 如果添加了新的数据源，请修改这个枚举
 *
 * @author
 */
public enum DataSources {

    /**
     * TTS主库
     */
    MASTER("master"),SLAVE("slave");


    /**
     * Spring中配置的数据源的id
     */


    private String key;

    private DataSources(String key) {this.key = key;}

    public String key(){return key;}

}

package com.qunar.qtalk.ss.utils.common.database;

public class DbConfig {
    public static final String QtQueueMasterConfig = "qcadmin.master";
    public static final String QtQueueSlaveConfig = "qcadmin.slave";


    private String url;
    private String username;
    private String password;

    public static String getDbUrl(String configKey) {
        String defineKey = String.format("%s.dburl", configKey);
        String connectionUrl = PropertiesUtil.getDBConfig().getProperty(defineKey);
        return connectionUrl;
    }

    public static String getUser(String configKey) {
        String defineKey = String.format("%s.dbuser", configKey);
        String userName = PropertiesUtil.getDBConfig().getProperty(defineKey);
        return userName;
    }

    public static String getPassword(String configKey) {
        String defineKey = String.format("%s.dbpassword", configKey);
        String userPass = PropertiesUtil.getDBConfig().getProperty(defineKey);
        return userPass;
    }

    public static DbConfig MakeConfigWithKey(String qtQueueMasterConfig) {
        DbConfig config = new DbConfig();
        config.password = DbConfig.getPassword(qtQueueMasterConfig);
        config.username = DbConfig.getUser(qtQueueMasterConfig);
        config.url = DbConfig.getDbUrl(qtQueueMasterConfig);
        return config;
    }

    public String getUsername() {
        return username;
    }

    public String getUrl() {
        return url;
    }

    public String getPassword() {
        return password;
    }
}

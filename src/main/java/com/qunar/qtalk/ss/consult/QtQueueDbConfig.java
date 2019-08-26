package com.qunar.qtalk.ss.consult;

public class QtQueueDbConfig {

//    private static final Logger logger = LoggerFactory.getLogger(com.qunar.qtalk.ss.common.database.DbConfig.class);
//    public static final String QtQueueMasterConfig = "qcadmin.master";
//    protected PropertiesConfig propertiesConfig;
//
//    private static class Holder {
//        private static final QtQueueDbConfig INSTANCE = new QtQueueDbConfig();
//    }
//
//    public static QtQueueDbConfig getInstance() {
//        return Holder.INSTANCE;
//    }
//
//
//    protected QtQueueDbConfig() {
//        try {
//            propertiesConfig = new PropertiesConfig("pgsql.properties");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    static {
//
//
//    }
//
//
//    private String url;
//    private String username;
//    private String password;
//
//    public static String getDbUrl(String configKey) {
//        String defineKey = String.format("%s.dburl", configKey);
//        String connectionUrl = PropertiesUtil.getDBConfig().getProperty(defineKey);
//        return connectionUrl;
//    }
//
//    public static String getUser(String configKey) {
//        String defineKey = String.format("%s.dbuser", configKey);
//
//        String userName = PropertiesUtil.getDBConfig().getProperty(defineKey);
//        return userName;
//    }
//
//    public static String getPassword(String configKey) {
//        String defineKey = String.format("%s.dbpassword", configKey);
//        String userPass = PropertiesUtil.getDBConfig().getProperty(defineKey);
//        return userPass;
//    }
//
//    public static com.qunar.qtalk.ss.common.database.DbConfig MakeConfigWithKey(String qtQueueMasterConfig) {
//        com.qunar.qtalk.ss.common.database.DbConfig config = new com.qunar.qtalk.ss.common.database.DbConfig();
//        config.password = com.qunar.qtalk.ss.common.database.DbConfig.getPassword(qtQueueMasterConfig);
//        config.username = com.qunar.qtalk.ss.common.database.DbConfig.getUser(qtQueueMasterConfig);
//        config.url = com.qunar.qtalk.ss.common.database.DbConfig.getDbUrl(qtQueueMasterConfig);
//        return config;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public String getUrl() {
//        return url;
//    }
//
//    public String getPassword() {
//        return password;
//    }
}

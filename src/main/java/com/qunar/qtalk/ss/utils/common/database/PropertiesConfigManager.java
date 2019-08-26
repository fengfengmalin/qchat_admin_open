package com.qunar.qtalk.ss.utils.common.database;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PropertiesConfigManager {
    private static Map<String, PropertiesConfig> configMap = new ConcurrentHashMap<>();

    public static PropertiesConfig getConfig(String filename) throws IOException {
        PropertiesConfig config = configMap.get(filename);

        if (config == null) {
            config = new PropertiesConfig(filename);
            configMap.put(filename, config);
        }
        return config;
    }

    public static void main(String[] args) {
        try {
            PropertiesConfig config = PropertiesConfigManager.getConfig("pgsql.properties");

            if (config != null) {

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

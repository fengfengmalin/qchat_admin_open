package com.qunar.qtalk.ss.utils.common.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public abstract class PropertiesUtil {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static final String PGFILENAME = "pgsql.properties";

    private static Properties pgsqlProperties;

    static {
        try {
            pgsqlProperties = PropertiesConfig.loadHotFile(PGFILENAME);
        } catch (IOException e) {
            logger.error("*CONFIG FILE* {} file is missing", PGFILENAME, e);
        }
    }


    public static Properties getDBConfig() {
        return pgsqlProperties;
    }
}

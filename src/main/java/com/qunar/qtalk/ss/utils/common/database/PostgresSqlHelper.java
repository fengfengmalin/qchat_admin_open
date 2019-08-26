package com.qunar.qtalk.ss.utils.common.database;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by may on 2017/2/22.
 */
public class PostgresSqlHelper {

    private PostgresSqlHelper() {
    }

    private static class Holder {
        private static final PostgresSqlHelper INSTANCE = new PostgresSqlHelper();
    }

    public static PostgresSqlHelper getInstance() {
        return Holder.INSTANCE;
    }

    private static final Logger logger = LoggerFactory.getLogger(PostgresSqlHelper.class);

    private ConcurrentHashMap<String, GroupConnection> dbConnectionMapping = new ConcurrentHashMap<>();

    public GroupConnection getDbConnection(String domain) {
        if (StringUtils.isEmpty(domain))
            return null;

        synchronized (this) {
            GroupConnection groupConnection;
            groupConnection = dbConnectionMapping.get(domain);

            if (groupConnection == null) {
                groupConnection = new GroupConnection(domain);
                dbConnectionMapping.put(domain, groupConnection);
            }
            return groupConnection;
        }
    }
}
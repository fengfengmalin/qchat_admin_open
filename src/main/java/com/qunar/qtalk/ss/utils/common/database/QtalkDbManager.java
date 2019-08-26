package com.qunar.qtalk.ss.utils.common.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class QtalkDbManager {
    private static final Logger logger = LoggerFactory.getLogger(QtalkDbManager.class);

    private static final int connectionsCount = 5;
    private static long runCount = 0;

    private ConcurrentHashMap<String, Connection[]> dbConnectionMapping = new ConcurrentHashMap<>();

    public Connection getConnection(String dbSchema, String sql) throws SQLException {
        int pos = sql.hashCode() % connectionsCount;
        return getConnection(dbSchema, pos);
    }

    public Connection getConnection(String dbSchema, int thePos) throws SQLException {
        synchronized (this) {
            int pos = Math.abs(thePos);
            Connection connection;
            Connection[] connections = dbConnectionMapping.get(dbSchema);
            if (connections == null) {
                connections = new Connection[connectionsCount];
            }

            connection = connections[pos];

            if (connection == null || connection.isClosed()) {
                try {
                    connection = DatabaseHelper.MakeConnection(DbConfig.MakeConfigWithKey(DbConfig.QtQueueMasterConfig));
                } catch (SQLException e) {
                    SQLException ex = e.getNextException();
                    logger.error("getConnection failed.", ex == null ? e : ex);
                } catch (ClassNotFoundException e) {
                    logger.error("getConnection failed.", e);
                }
                connections[pos] = connection;
                dbConnectionMapping.put(dbSchema, connections);
            }

            return connection;
        }

    }

    public Connection getConnection(String dbSchema) throws SQLException {
        int pos = (int) ((runCount++) % connectionsCount);
        return getConnection(dbSchema, pos);

    }

    private static class Holder {
        private static final QtalkDbManager INSTANCE = new QtalkDbManager();
    }

    public static QtalkDbManager getInstance() {
        return QtalkDbManager.Holder.INSTANCE;
    }
}

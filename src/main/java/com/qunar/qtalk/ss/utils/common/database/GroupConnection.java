package com.qunar.qtalk.ss.utils.common.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class GroupConnection {

    private Connection groupConnection;
    private Connection messageConnetion;

    private String domain;

    private static final Logger logger = LoggerFactory.getLogger(GroupConnection.class);

    public GroupConnection(String domain) {
        this.domain = domain;
        try {
            groupConnection = takeConnection(domain, DBConnectionType.GroupConnection, groupConnection);
            messageConnetion = takeConnection(domain, DBConnectionType.MessageConnection, messageConnetion);
        } catch (SQLException e) {
            logger.error("init failed.", e);
        } catch (ClassNotFoundException e) {
            logger.error("init failed.", e);
        }
    }

    public Connection getGroupConnection() throws SQLException {
        try {
            return takeConnection(domain, DBConnectionType.MessageConnection, groupConnection);
        } catch (ClassNotFoundException e) {
            logger.error("getGroupConnection failed.", e);
        }
        return null;
    }

    public Connection getMessageConnetion() throws SQLException {
        try {
            return takeConnection(domain, DBConnectionType.MessageConnection, groupConnection);
        } catch (ClassNotFoundException e) {
            logger.error("getGroupConnection failed.", e);
        }
        return null;
    }

    private Connection takeConnection(String domain, DBConnectionType type, Connection connection) throws SQLException, ClassNotFoundException {

        Connection newConnection = null;

        synchronized (this) {


            if (connection != null) {
                if (connection.isClosed()) {
                    connection = null;
                } else {
                    newConnection = connection;
                }
            }
            if (connection == null) {
                String configNode = String.format("%s.%s", domain, (type == DBConnectionType.MessageConnection ? "message" : "group"));

                String connectionUrl = PropertiesUtil.getDBConfig().getProperty(String.format("%s.dburl", configNode));
                String userName = PropertiesUtil.getDBConfig().getProperty(String.format("%s.dbuser", configNode));
                String userPass = PropertiesUtil.getDBConfig().getProperty(String.format("%s.dbpassword", configNode));
                newConnection = DatabaseHelper.MakeConnection(connectionUrl, userName, userPass);


                logger.info("begin to init connection: {}-{}-{}", connectionUrl, userName, userPass);

                newConnection.setAutoCommit(true);
            }
        }

        return newConnection;
    }
}

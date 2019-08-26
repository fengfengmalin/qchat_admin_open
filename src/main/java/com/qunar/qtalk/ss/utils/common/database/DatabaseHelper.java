package com.qunar.qtalk.ss.utils.common.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;

public class DatabaseHelper {
    public static String antiSQLInjection(String str) {
        return str.replaceAll(".*([';]+|(--)+).*", "");
    }

    public static String makeInCollecitonSql(String sql, Collection list, ParameterdRunnable runnable) {
        StringBuilder sb = new StringBuilder();

        for (Object msg : list) {
            sb.append(String.format("'%s',", DatabaseHelper.antiSQLInjection(runnable.run(msg))));
        }

        String usersString = sb.substring(0, sb.length() - 1);

        return sql + usersString + ");";
    }


    public static final String PGDriver = "org.postgresql.Driver";

    public static Connection MakeConnection(String connectionUrl, String userName, String userPass) throws ClassNotFoundException, SQLException {
        return MakeConnection(PGDriver, connectionUrl, userName, userPass);
    }

    public static Connection MakeConnection(String driverName, String connectionUrl, String userName, String userPass) throws ClassNotFoundException, SQLException {
        Class.forName(driverName);
        return DriverManager.getConnection(connectionUrl, userName, userPass);
    }

    public static Connection MakeConnection(DbConfig dbConfig) throws SQLException, ClassNotFoundException {
        return MakeConnection(PGDriver, dbConfig.getUrl(), dbConfig.getUsername(), dbConfig.getPassword());
    }
}

package com.minesweeper.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String MYSQL_URL  = "jdbc:mysql://localhost:3306/minesweeper_db";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASS = "Hockun001$";

    public static Connection getConnection() throws SQLException {
        String testUrl = System.getProperty("TEST_DB_URL");
        if (testUrl != null) {
            String user = System.getProperty("TEST_DB_USER", "sa");
            String pass = System.getProperty("TEST_DB_PASS", "");
            return DriverManager.getConnection(testUrl, user, pass);
        }
        return DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
    }
}
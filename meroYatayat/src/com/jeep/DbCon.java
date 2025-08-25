package com.jeep;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DbCon {
    private static final String URL = "jdbc:mysql://localhost:3306/JeepTransport?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = ""; // change to your MySQL password

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found!", e);
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
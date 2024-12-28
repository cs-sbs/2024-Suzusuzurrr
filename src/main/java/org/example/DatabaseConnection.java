package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // 获取数据库连接
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/BookManager?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "123456";
        return DriverManager.getConnection(url, user, password);
    }
}

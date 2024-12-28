package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

public class UserService {
    //注冊

    public static boolean register(String username, String password, String email) {
        // 检查用户名是否已被注册
        if (isUsernameTaken(username)) {
            return false;  // 用户名已存在
        }

        // 将新用户插入到数据库
        try (Connection connection = DatabaseConnection.getConnection()) {  // 使用统一的数据库连接方法
            String sql = "INSERT INTO Users (username, password, email, role) VALUES (?, ?, ?, 'user')";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);  // 明文密码，可以在此使用哈希加密
                preparedStatement.setString(3, email);
                int rowsAffected = preparedStatement.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.out.println("数据库连接失败，无法注册用户：" + e.getMessage());
            return false;
        }
    }
    public static boolean isUsernameTaken(String username) {
        try (Connection connection = DatabaseConnection.getConnection()) {  // 使用统一的数据库连接方法
            String sql = "SELECT 1 FROM Users WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();
                return resultSet.next();  // 如果用户名存在，返回 true
            }
        } catch (SQLException e) {
            System.out.println("数据库连接失败，无法检查用户名：" + e.getMessage());
            return false;
        }
    }

    // 登录

    public static Optional<String> login(String username, String password) {
        try (Connection connection = DatabaseConnection.getConnection()) {  // 使用统一的数据库连接方法
            String sql = "SELECT role FROM Users WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);  // 直接使用明文密码

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(resultSet.getString("role"));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("数据库连接失败，无法登录：" + e.getMessage());
        }
        return Optional.empty();
    }

}

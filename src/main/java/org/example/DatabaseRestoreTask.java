package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.util.Arrays;

public class DatabaseRestoreTask {

    private static final int BATCH_SIZE = 1000; // 设置批量插入大小

    public static void restoreDatabase(String filePath) {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             Connection connection = DatabaseConnection.getConnection()) {

            // 清理目标表
            clearTable(connection, "books");
            clearTable(connection, "users");

            // Skip the header line
            if ((line = br.readLine()) != null) {
                int countBooks = 0, countUsers = 0;

                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue; // 跳过空行

                    String[] data = line.split(", ");
                    if (data.length < 2) continue; // 忽略无效行

                    String type = data[0];
                    switch (type) {
                        case "book":
                            if (data.length == 6) { // 包括 type 和 5 个字段
                                restoreBookRecord(connection, data);
                                countBooks++;
                            }
                            break;
                        case "user":
                            if (data.length == 5) { // 包括 type 和 4 个字段
                                restoreUserRecord(connection, data);
                                countUsers++;
                            }
                            break;
                        default:
                            System.err.println("未知类型: " + type);
                    }

                    // 批量处理
                    if (countBooks % BATCH_SIZE == 0 || countUsers % BATCH_SIZE == 0) {
                        System.out.println("Processed " + countBooks + " book records and " + countUsers + " user records...");
                    }
                }
                System.out.println("Restored " + countBooks + " book records and " + countUsers + " user records.");
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.err.println("恢复数据库失败: " + e.getMessage());
        }
    }

    private static void restoreBookRecord(Connection connection, String[] data) throws SQLException {
        String sql = "INSERT INTO books (id, title, author, price, publish_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(data[1]));
            pstmt.setString(2, data[2]);
            pstmt.setString(3, data[3]);
            pstmt.setDouble(4, Double.parseDouble(data[4]));

            // 确保日期格式正确并尝试转换
            try {
                pstmt.setDate(5, Date.valueOf(data[5].trim())); // 使用 java.sql.Date.valueOf
            } catch (IllegalArgumentException e) {
                System.err.println("输入日期格式错误: " + Arrays.toString(data));
                return;
            }

            pstmt.executeUpdate();
        }
    }

    private static void restoreUserRecord(Connection connection, String[] data) throws SQLException {
        String sql = "INSERT INTO users (id, username, email, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(data[1]));
            pstmt.setString(2, data[2]);
            pstmt.setString(3, data[3]);
            pstmt.setString(4, data[4]);

            pstmt.executeUpdate();
        }
    }

    private static void clearTable(Connection connection, String tableName) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM " + tableName)) {
            pstmt.executeUpdate();
            System.out.println("清空表: " + tableName);
        }
    }
}
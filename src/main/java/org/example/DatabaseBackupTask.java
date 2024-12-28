package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseBackupTask implements Runnable {
    private static final String RELATIVE_PATH = "backups/"; // 相对于项目根目录或当前工作目录

    @Override
    public void run() {
        try {
            backupDatabase();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.err.println("备份失败: " + e.getMessage());
        }
    }

    private void backupDatabase() throws IOException, SQLException {
        ensureDirectoryExists(RELATIVE_PATH);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "backup_" + timestamp + ".csv";
        String filePath = getFilePath(fileName);

        backupDatabase(filePath);
    }

    public static void backupDatabase(String filePath) throws IOException, SQLException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 写入表头
            writer.write("type,id,title,author,price,publish_date,username,email,role\n");

            // 备份书籍表
            backupTable(writer, "books", "book");
            // 备份用户表
            backupTable(writer, "users", "user");

            System.out.println("\n备份成功: 项目根目录/" + filePath + "\n");
        }
    }

    private static void backupTable(BufferedWriter writer, String tableName, String type) throws IOException, SQLException {
        String sql;
        if ("books".equals(tableName)) {
            sql = "SELECT id, title, author, price, publish_date FROM books";
        } else if ("users".equals(tableName)) {
            sql = "SELECT id, username, email, role FROM users";
        } else {
            throw new IllegalArgumentException("Unsupported table name: " + tableName);
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                StringBuilder line = new StringBuilder(type).append(",");
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    Object value = resultSet.getObject(i);
                    line.append(value != null ? escapeCsvValue(value.toString()) : "").append(",");
                }
                // 移除最后一个多余的逗号并添加换行符
                writer.write(line.substring(0, line.length() - 1).replace(",", ", ") + "\n");
            }
        }
    }

    private static void ensureDirectoryExists(String path) throws IOException {
        if (!Files.exists(Paths.get(path))) {
            Files.createDirectories(Paths.get(path));
        }
    }

    private static String getFilePath(String fileName) {
        return RELATIVE_PATH + (RELATIVE_PATH.endsWith("/") || RELATIVE_PATH.endsWith("\\") ? "" : System.getProperty("file.separator")) + fileName;
    }

    private static String escapeCsvValue(String value) {
        // Escape double quotes and wrap the value in double quotes if it contains commas or double quotes
        if (value.contains("\"") || value.contains(",")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
package org.example;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static boolean isAdmin = false;
    private static String currentUser = "";  // 添加 currentUser 变量来存储当前登录的用户名

    public static void main(String[] args) {
        // 启动备份调度器
        BackupScheduler.startBackupScheduler();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            displayMenu();
            int choice = getValidChoice(scanner);

            switch (choice) {
                case 1:
                    register(scanner);
                    break;
                case 2:
                    login(scanner);
                    break;
                case 3:
                    displayAllBooks();
                    break;
                case 4:
                    searchBooks(scanner);
                    break;
                case 5:
                    executeAdminAction(() -> addBook(scanner));
                    break;
                case 6:
                    executeAdminAction(() -> deleteBook(scanner));
                    break;
                case 7:
                    executeAdminAction(() -> updateBook(scanner));
                    break;
                case 8:
                    logout();
                    break;
                case 9:
                    System.out.println("正在退出程序...");
                    scanner.close();
                    System.exit(0);
                    break;
                case 10:
                    executeAdminAction(() -> restoreDatabase(scanner));
                    break;
                default:
                    System.out.println("非正确的选择，请重新输入。");
                    break;
            }
        }
    }

    // 显示菜单
    private static void displayMenu() {
        System.out.println("\n==== 欢迎使用图书管理系统 ====（管理员用户：root，密码：123456）");
        System.out.printf("%-5s %-40s\n", "1.", "注册");
        System.out.printf("%-5s %-40s\n", "2.", "登录");
        System.out.printf("%-5s %-40s\n", "3.", "查看图书");
        System.out.printf("%-5s %-40s\n", "4.", "搜索图书");
        System.out.printf("%-5s %-40s\n", "5.", "添加图书（仅管理员可用）");
        System.out.printf("%-5s %-40s\n", "6.", "删除图书（仅管理员可用）");
        System.out.printf("%-5s %-40s\n", "7.", "更新图书（仅管理员可用）");
        System.out.printf("%-5s %-40s\n", "8.", "注销");
        System.out.printf("%-5s %-40s\n", "9.", "退出（退出程序）");
        System.out.printf("%-5s %-40s\n", "10.", "恢复数据库（仅管理员可用）");
        System.out.print("请输入您的选择（1-10）：");
    }
    //檢測是否為管理員
    private static void executeAdminAction(Runnable action) {
        if (isAdmin) {
            action.run();
        } else {
            System.out.println("权限不足，仅管理员可执行此操作。");
        }
    }
    //获取有效的选择
    private static int getValidChoice(Scanner scanner) {
        int choice;
        while (true) {
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                if (choice >= 1 && choice <= 10) {
                    break;
                } else {
                    System.out.println("无效选择，请输入1到10之间的数字。");
                }
            } else {
                System.out.println("无效输入，请输入一个数字。");
                scanner.next();
            }
        }
        return choice;
    }

    //注册
    private static void register(Scanner scanner) {
        System.out.println("请输入用户名：");
        String username = scanner.nextLine();

        // 检查用户名是否已存在
        if ((UserService.isUsernameTaken(username))) {
            System.out.println("该用户名已被注册，请选择其他用户名。");
            return;
        }

        System.out.println("请输入密码：");
        String password = scanner.nextLine();

        System.out.println("请输入电子邮件(可选)：");
        String email = scanner.nextLine();

        // 调用 UserService 的 register 方法
        boolean success = UserService.register(username, password, email);
        if (success) {
            System.out.println("注册成功！欢迎加入图书管理系统。");
        } else {
            System.out.println("注册失败，请重试。");
        }
    }
    //登录
    public static void login(Scanner scanner) {
        System.out.println("请输入用户名：");
        String username = scanner.nextLine();

        System.out.println("请输入密码：");
        String password = scanner.nextLine();

        // 调用 UserService 登录方法
        Optional<String> role = UserService.login(username, password);

        if (role.isPresent()) {
            // 登录成功，设置当前用户名，并判断角色
            currentUser = username;  // 保存当前登录的用户名
            isAdmin = "admin".equals(role.get()); // 判断角色是否为管理员
            System.out.println("登录成功！欢迎 " + currentUser);
            if (isAdmin) {
                System.out.println("您是管理员。");
            }
        } else {
            // 登录失败
            System.out.println("登录失败：用户名或密码错误。");
        }
    }
    //注销
    private static void logout() {
        if (currentUser.isEmpty()) {
            System.out.println("您尚未登录，无需注销。");
        } else {
            // 清空当前用户和权限
            currentUser = "";
            isAdmin = false;
            System.out.println("注销成功！您已成功退出系统。");
        }
    }
    //查看图书
    private static void displayAllBooks() {
        // SQL 查询语句
        String sql = "SELECT * FROM books";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            // 打印表头，使用 printf 格式化输出
            System.out.printf("%-5s %-40s %-30s %-10s %-15s\n", "ID", "书名", "作者", "价格", "出版日期");
            System.out.println("---------------------------------------------------------------");

            // 迭代查询结果并打印每本书的详情
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                double price = resultSet.getDouble("price");
                Date publishDate = resultSet.getDate("publish_date");

                // 使用 printf 格式化输出每本书的信息
                System.out.printf("%-5d %-40s %-30s %-10.2f %-15s\n", id, title, author, price, publishDate);
            }
        } catch (SQLException e) {
            System.out.println("数据库连接失败，无法显示书籍：" + e.getMessage());
        }
    }
    //搜索图书
    private static void searchBooks(Scanner scanner) {
        System.out.println("请输入书名或作者进行搜索（可以部分匹配）：");
        String keyword = scanner.nextLine();

        // 使用 LIKE 模糊匹配进行查询
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                // 设置参数，进行模糊匹配
                preparedStatement.setString(1, "%" + keyword + "%");
                preparedStatement.setString(2, "%" + keyword + "%");

                // 执行查询
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    // 输出结果
                    boolean found = false;
                    while (resultSet.next()) {
                        found = true;
                        int id = resultSet.getInt("id");
                        String title = resultSet.getString("title");
                        String author = resultSet.getString("author");
                        double price = resultSet.getDouble("price");
                        Date publishDate = resultSet.getDate("publish_date");

                        System.out.println("ID: " + id + ", 书名: " + title + ", 作者: " + author +
                                ", 价格: " + price + ", 出版日期: " + publishDate);
                    }

                    if (!found) {
                        System.out.println("没有找到符合条件的图书。");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("数据库查询失败：" + e.getMessage());
        }
    }
    //添加图书
    private static void addBook(Scanner scanner) {
        // 只有管理员才能添加图书
        if (!isAdmin) {
            System.out.println("权限不足，仅管理员可以添加图书。");
            return;
        }

        System.out.println("请输入图书信息：");

        // 输入书名、作者、价格和出版日期
        System.out.print("书名: ");
        String title = scanner.nextLine();

        System.out.print("作者: ");
        String author = scanner.nextLine();

        System.out.print("价格: ");
        double price;
        while (true) {
            if (scanner.hasNextDouble()) {
                price = scanner.nextDouble();
                scanner.nextLine();  // consume the newline character after the number
                break;
            } else {
                System.out.println("无效的价格，请输入一个有效的数字。");
                scanner.next();  // consume the invalid input
            }
        }

        System.out.print("出版日期 (格式：yyyy-mm-dd): ");
        String publishDateStr = scanner.nextLine();
        java.sql.Date publishDate = java.sql.Date.valueOf(publishDateStr);

        // 将图书信息插入到数据库
        String sql = "INSERT INTO books (title, author, price, publish_date) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                // 设置查询参数
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, author);
                preparedStatement.setDouble(3, price);
                preparedStatement.setDate(4, publishDate);

                // 执行插入操作
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("图书添加成功！");
                } else {
                    System.out.println("图书添加失败，请重试。");
                }
            }
        } catch (SQLException e) {
            System.out.println("数据库连接失败，无法添加图书：" + e.getMessage());
        }
    }
    //删除图书
    private static void deleteBook(Scanner scanner) {
        // 只有管理员才能删除图书
        if (!isAdmin) {
            System.out.println("权限不足，仅管理员可以删除图书。");
            return;
        }

        System.out.println("请输入要删除的图书ID：");
        int bookId = scanner.nextInt();
        scanner.nextLine();  // consume newline character

        // 确认删除
        System.out.println("您确认要删除图书 ID 为 " + bookId + " 的图书吗？(y/n)");
        String confirmation = scanner.nextLine();

        if (!confirmation.equalsIgnoreCase("y")) {
            System.out.println("删除操作已取消。");
            return;
        }

        // 执行删除操作
        String sql = "DELETE FROM books WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, bookId);  // 设置要删除的图书 ID

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("图书删除成功！");
                } else {
                    System.out.println("未找到图书 ID " + bookId + "，删除失败。");
                }
            }
        } catch (SQLException e) {
            System.out.println("数据库连接失败，无法删除图书：" + e.getMessage());
        }
    }
    //更新图书
    private static void updateBook(Scanner scanner) {
        // 只有管理员才能更新图书信息
        if (!isAdmin) {
            System.out.println("权限不足，仅管理员可以更新图书信息。");
            return;
        }

        System.out.println("请输入要更新的图书ID：");
        int bookId = scanner.nextInt();
        scanner.nextLine();  // consume newline character

        // 提示管理员输入新的图书信息
        System.out.println("请输入新的图书标题（留空表示不更改）：");
        String newTitle = scanner.nextLine();

        System.out.println("请输入新的作者（留空表示不更改）：");
        String newAuthor = scanner.nextLine();

        System.out.println("请输入新的价格（留空表示不更改）：");
        String priceInput = scanner.nextLine();
        BigDecimal newPrice = null;
        if (!priceInput.isEmpty()) {
            try {
                newPrice = new BigDecimal(priceInput);
            } catch (NumberFormatException e) {
                System.out.println("无效的价格格式，更新失败。");
                return;
            }
        }

        System.out.println("请输入新的出版日期（格式：yyyy-MM-dd，留空表示不更改）：");
        String dateInput = scanner.nextLine();
        Date newPublishDate = null;
        if (!dateInput.isEmpty()) {
            try {
                newPublishDate = Date.valueOf(dateInput);  // 将字符串转换为 Date 类型
            } catch (IllegalArgumentException e) {
                System.out.println("无效的日期格式，更新失败。");
                return;
            }
        }

        // SQL 更新语句
        String sql = "UPDATE books SET title = COALESCE(?, title), author = COALESCE(?, author), price = COALESCE(?, price), publish_date = COALESCE(?, publish_date) WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, newTitle.isEmpty() ? null : newTitle);
                preparedStatement.setString(2, newAuthor.isEmpty() ? null : newAuthor);
                preparedStatement.setBigDecimal(3, newPrice);
                preparedStatement.setDate(4, newPublishDate);
                preparedStatement.setInt(5, bookId);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("图书信息更新成功！");
                } else {
                    System.out.println("未找到图书 ID " + bookId + "，更新失败。");
                }
            }
        } catch (SQLException e) {
            System.out.println("数据库连接失败，无法更新图书信息：" + e.getMessage());
        }
    }
    // 恢复数据库
    private static void restoreDatabase(Scanner scanner) {
        System.out.print("请输入备份文件路径: ");
        String filePath = scanner.nextLine();
        DatabaseRestoreTask.restoreDatabase(filePath);
    }

}

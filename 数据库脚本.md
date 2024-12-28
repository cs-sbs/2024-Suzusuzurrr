-- 创建数据库
CREATE DATABASE IF NOT EXISTS `bookmanager`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci
/*!80016 DEFAULT ENCRYPTION='N' */;

-- 使用刚创建的数据库
USE `bookmanager`;

-- 创建 users 表
CREATE TABLE IF NOT EXISTS `users` (
`id` int NOT NULL AUTO_INCREMENT,
`username` varchar(255) NOT NULL,
`password` varchar(255) DEFAULT NULL,
`email` varchar(255) DEFAULT NULL,
`role` enum('user','admin') DEFAULT 'user',
PRIMARY KEY (`id`),
UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建 books 表
CREATE TABLE IF NOT EXISTS `books` (
`id` int NOT NULL AUTO_INCREMENT,
`title` varchar(100) DEFAULT NULL,
`author` varchar(100) DEFAULT NULL,
`price` decimal(10,2) DEFAULT NULL,
`publish_date` date DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 插入 root 用户
INSERT INTO `users` (username, password, role)
VALUES ('root', '123456', 'admin')
ON DUPLICATE KEY UPDATE id=id; -- 如果 root 用户已经存在，则不插入新记录

-- 插入 10 个 books 记录
INSERT INTO `books` (title, author, price, publish_date) VALUES
('The Great Gatsby', 'F. Scott Fitzgerald', 12.99, '1925-04-10'),
('To Kill a Mockingbird', 'Harper Lee', 10.99, '1960-07-11'),
('1984', 'George Orwell', 9.99, '1949-06-08'),
('Pride and Prejudice', 'Jane Austen', 8.99, '1813-01-28'),
('The Catcher in the Rye', 'J.D. Salinger', 11.99, '1951-07-16'),
('Brave New World', 'Aldous Huxley', 10.49, '1932-07-01'),
('Moby Dick', 'Herman Melville', 14.99, '1851-10-18'),
('War and Peace', 'Leo Tolstoy', 16.99, '1869-01-01'),
('The Lord of the Rings', 'J.R.R. Tolkien', 18.99, '1954-07-29'),
('Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', 7.99, '1997-06-26');
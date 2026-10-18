-- ============================================================
-- 智能会议室管理系统 - 数据库初始化脚本
-- 使用方法：在 MySQL 中执行 source init.sql
-- 或：mysql -u root -p < init.sql
-- ============================================================

-- 强制本次连接使用 utf8mb4（否则客户端默认字符集不一致时，中文数据会插入失败）
SET NAMES utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS meeting_room DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE meeting_room;

-- ============================================================
-- 用户表
-- ============================================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id`         INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`   VARCHAR(50)  NOT NULL UNIQUE         COMMENT '用户名',
    `password`   VARCHAR(100) NOT NULL                COMMENT '密码',
    `role`       VARCHAR(20)  NOT NULL DEFAULT 'user' COMMENT '角色：user/admin',
    `created_at` DATETIME                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 会议室表
-- ============================================================
DROP TABLE IF EXISTS `meeting_rooms`;
CREATE TABLE `meeting_rooms` (
    `id`         INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`       VARCHAR(100) NOT NULL                COMMENT '会议室名称',
    `location`   VARCHAR(200)                         COMMENT '位置',
    `capacity`   INT                                   COMMENT '容纳人数',
    `status`     VARCHAR(20)  NOT NULL DEFAULT 'available' COMMENT '状态：available/occupied/maintenance',
    `created_at` DATETIME                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议室表';

-- ============================================================
-- 种子数据
-- ============================================================

-- 用户数据（密码暂用明文，第9-10周开发时再加密）
INSERT INTO `users` (`username`, `password`, `role`) VALUES
    ('admin', '123456', 'admin'),
    ('user1', '123456', 'user');

-- 会议室数据
INSERT INTO `meeting_rooms` (`name`, `location`, `capacity`, `status`) VALUES
    ('A101会议室', 'A座1楼', 10, 'available'),
    ('B201会议室', 'B座2楼', 20, 'available'),
    ('C301会议室', 'C座3楼', 30, 'available');

-- ============================================================
-- 验证数据
-- ============================================================
SELECT '--- 用户表数据 ---' AS info;
SELECT id, username, role FROM `users`;

SELECT '--- 会议室表数据 ---' AS info;
SELECT id, name, location, capacity, status FROM `meeting_rooms`;

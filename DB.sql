-- Paper Automatic Classification System
-- MySQL 8.x schema script

CREATE DATABASE IF NOT EXISTS `paper_system`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `paper_system`;

-- 用户表
CREATE TABLE IF NOT EXISTS `app_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 分类表
CREATE TABLE IF NOT EXISTS `category` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) DEFAULT NULL,
  `theme_color` VARCHAR(16) DEFAULT NULL,
  `description` VARCHAR(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 论文表
CREATE TABLE IF NOT EXISTS `paper` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) DEFAULT NULL,
  `file_path` VARCHAR(255) DEFAULT NULL,
  `code_url` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_paper_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 论文-分类关联表（多对多）
CREATE TABLE IF NOT EXISTS `paper_category` (
  `paper_id` BIGINT NOT NULL,
  `category_id` INT NOT NULL,
  PRIMARY KEY (`paper_id`, `category_id`),
  KEY `idx_paper_category_category_id` (`category_id`),
  CONSTRAINT `fk_paper_category_paper`
    FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`)
    ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_paper_category_category`
    FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
    ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 系统设置表（如研究方向等）
CREATE TABLE IF NOT EXISTS `app_setting` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `setting_key` VARCHAR(128) NOT NULL,
  `setting_value` VARCHAR(4000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_setting_setting_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


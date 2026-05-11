create database if not exists code_mind_agent;
use code_mind_agent;

-- 创建会话表
CREATE TABLE `sessions`
(
    `id`           BIGINT   NOT NULL COMMENT '主键id',
    `user_id`      BIGINT   NOT NULL COMMENT '用户ID',
    `repo_id`      int      NOT NULL COMMENT '关联的仓库ID（当前工作上下文）',
    `title`        VARCHAR(100)      DEFAULT NULL COMMENT '会话标题（可由第一条消息生成）',
    `status`       int      NOT NULL DEFAULT 0 COMMENT '状态 (0-ACTIVE / 1-ARCHIVED / 2-DELETED)',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活动时间',
    PRIMARY KEY (`id`)
) COMMENT ='会话表';


-- 创建消息表
CREATE TABLE `messages`
(
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `session_id` BIGINT   NOT NULL COMMENT '所属会话ID',
    `role`       int      NOT NULL COMMENT '角色 (0-SYSTEM / 1-USER / 2-ASSISTANT / 3-TOOL)',
    `content`    TEXT     NOT NULL COMMENT '消息内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) COMMENT ='消息表';

-- 创建用户表
CREATE TABLE `user`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
    `username`   VARCHAR(50)  NOT NULL COMMENT '登录账号，唯一',
    `phone`      VARCHAR(15)           DEFAULT NULL COMMENT '手机号',
    `password`   VARCHAR(255) NOT NULL COMMENT '加密密文',
    `email`      VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    `enabled`    TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用 (1:启用 0:禁用)',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- 用户仓库权限表
CREATE TABLE `user_repo_permissions`
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT      NOT NULL COMMENT '用户 ID',
    `repo_id`    BIGINT      NOT NULL COMMENT '仓库 ID',
    `permission` VARCHAR(20) NOT NULL COMMENT '权限级别 (READ / WRITE / ADMIN)',
    `granted_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户仓库权限表';



























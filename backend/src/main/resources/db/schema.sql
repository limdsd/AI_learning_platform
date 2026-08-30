-- =====================================================
-- AI 智能学习平台 建表脚本
-- 数据库: learning_platform (字符集 utf8mb4)
-- =====================================================

CREATE DATABASE IF NOT EXISTS learning_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE learning_platform;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `create_time` DATETIME     DEFAULT NULL,
    `update_time` DATETIME     DEFAULT NULL,
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- 题目表
CREATE TABLE IF NOT EXISTS `question` (
    `id`              BIGINT       NOT NULL COMMENT '主键',
    `type`            VARCHAR(20)  NOT NULL COMMENT '题型: single/multiple/judge/fill/short_answer',
    `content`         TEXT         NOT NULL COMMENT '题干',
    `options`         TEXT         DEFAULT NULL COMMENT '选项(JSON数组 [{key,text}])',
    `answer`          TEXT         NOT NULL COMMENT '答案',
    `analysis`        TEXT         DEFAULT NULL COMMENT '解析',
    `knowledge_point` VARCHAR(100) DEFAULT NULL COMMENT '知识点',
    `difficulty`      VARCHAR(20)  DEFAULT NULL COMMENT '难度: easy/medium/hard',
    `source`          VARCHAR(20)  DEFAULT 'manual' COMMENT '来源: AI/manual',
    `create_time`     DATETIME     DEFAULT NULL,
    `update_time`     DATETIME     DEFAULT NULL,
    `deleted`         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_kp` (`knowledge_point`),
    KEY `idx_type_diff` (`type`, `difficulty`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目';

-- 错题本
CREATE TABLE IF NOT EXISTS `wrong_question` (
    `id`          BIGINT   NOT NULL,
    `user_id`     BIGINT   NOT NULL,
    `question_id` BIGINT   NOT NULL,
    `wrong_count` INT      NOT NULL DEFAULT 1 COMMENT '错误次数',
    `mastered`    TINYINT  NOT NULL DEFAULT 0 COMMENT '是否已掌握 0否 1是',
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    `deleted`     TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本';

-- 刷题记录
CREATE TABLE IF NOT EXISTS `practice_record` (
    `id`           BIGINT   NOT NULL,
    `user_id`      BIGINT   NOT NULL,
    `question_id`  BIGINT   NOT NULL,
    `user_answer`  TEXT     DEFAULT NULL,
    `correct`      TINYINT  NOT NULL DEFAULT 0 COMMENT '是否正确 0否 1是',
    `cost_seconds` INT      DEFAULT NULL COMMENT '作答耗时(秒)',
    `create_time`  DATETIME DEFAULT NULL,
    `update_time`  DATETIME DEFAULT NULL,
    `deleted`      TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='刷题记录';

-- 试卷表
CREATE TABLE IF NOT EXISTS `exam` (
    `id`               BIGINT       NOT NULL,
    `name`             VARCHAR(100) NOT NULL COMMENT '试卷名称',
    `total_score`      INT          NOT NULL DEFAULT 100 COMMENT '总分',
    `duration_minutes` INT          NOT NULL DEFAULT 60 COMMENT '时长(分钟)',
    `create_type`      VARCHAR(20)  DEFAULT 'AI' COMMENT '组卷方式: AI/manual',
    `create_time`      DATETIME     DEFAULT NULL,
    `update_time`      DATETIME     DEFAULT NULL,
    `deleted`          TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷';

-- 试卷-题目关联
CREATE TABLE IF NOT EXISTS `exam_question` (
    `id`          BIGINT   NOT NULL,
    `exam_id`     BIGINT   NOT NULL,
    `question_id` BIGINT   NOT NULL,
    `score`       INT      NOT NULL DEFAULT 5 COMMENT '该题分值',
    `sort`        INT      NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    `deleted`     TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_exam` (`exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联';

-- 考试记录
CREATE TABLE IF NOT EXISTS `exam_record` (
    `id`               BIGINT      NOT NULL,
    `user_id`          BIGINT      NOT NULL,
    `exam_id`          BIGINT      NOT NULL,
    `status`           VARCHAR(20) NOT NULL DEFAULT 'ongoing' COMMENT '状态: ongoing/submitted',
    `objective_score`  INT         DEFAULT 0 COMMENT '客观题得分',
    `subjective_score` INT         DEFAULT 0 COMMENT '主观题得分',
    `total_score`      INT         DEFAULT 0 COMMENT '总分',
    `start_time`       DATETIME    DEFAULT NULL,
    `submit_time`      DATETIME    DEFAULT NULL,
    `create_time`      DATETIME    DEFAULT NULL,
    `update_time`      DATETIME    DEFAULT NULL,
    `deleted`          TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_exam` (`exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录';

-- 考试作答明细
CREATE TABLE IF NOT EXISTS `exam_answer` (
    `id`          BIGINT  NOT NULL,
    `record_id`   BIGINT  NOT NULL,
    `question_id` BIGINT  NOT NULL,
    `user_answer` TEXT    DEFAULT NULL,
    `correct`     TINYINT DEFAULT NULL COMMENT '客观题判定 1对 0错',
    `ai_score`    INT     DEFAULT NULL COMMENT '主观题 AI 得分',
    `ai_comment`  TEXT    DEFAULT NULL COMMENT '主观题 AI 评语',
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    `deleted`     TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_record` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试作答明细';

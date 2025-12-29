-- ============================================
-- 面试知识库系统数据库设计
-- MySQL 8.0+
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS interview_kb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE interview_kb;

-- ============================================
-- 1. 用户相关表
-- ============================================

-- 用户表
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `password` VARCHAR(255) DEFAULT NULL COMMENT '密码(加密)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `phone_verified` TINYINT DEFAULT 0 COMMENT '手机号是否验证: 0-否 1-是',
    `wechat_openid` VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID',
    `wechat_unionid` VARCHAR(100) DEFAULT NULL COMMENT '微信UnionID',
    `wechat_nickname` VARCHAR(100) DEFAULT NULL COMMENT '微信昵称',
    `wechat_avatar` VARCHAR(500) DEFAULT NULL COMMENT '微信头像',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `email_verified` TINYINT DEFAULT 0 COMMENT '邮箱是否验证: 0-否 1-是',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `login_count` INT DEFAULT 0 COMMENT '登录次数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_wechat_openid` (`wechat_openid`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户角色表
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 密码重置令牌表
CREATE TABLE `password_reset_token` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `token` VARCHAR(255) NOT NULL COMMENT '重置令牌',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `used` TINYINT DEFAULT 0 COMMENT '是否已使用: 0-否 1-是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_token` (`token`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='密码重置令牌表';

-- ============================================
-- 2. 分类相关表
-- ============================================

-- 知识分类表（支持多级分类）
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    `level` TINYINT DEFAULT 1 COMMENT '层级: 1-一级 2-二级 3-三级',
    `sort` INT DEFAULT 0 COMMENT '排序值',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `question_count` INT DEFAULT 0 COMMENT '题目数量（冗余字段，定期更新）',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识分类表';

-- ============================================
-- 3. 标签相关表
-- ============================================

-- 标签表
CREATE TABLE `tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `color` VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
    `question_count` INT DEFAULT 0 COMMENT '关联题目数量',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ============================================
-- 4. 题目相关表
-- ============================================

-- 题目表
CREATE TABLE `question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    `title` VARCHAR(500) NOT NULL COMMENT '题目标题',
    `content` TEXT COMMENT '题目内容(Markdown)',
    `answer` TEXT COMMENT '答案(Markdown)',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `difficulty` TINYINT DEFAULT 2 COMMENT '难度: 1-简单 2-中等 3-困难',
    `source` VARCHAR(100) DEFAULT NULL COMMENT '题目来源(如:阿里面试、腾讯面试)',
    `source_url` VARCHAR(500) DEFAULT NULL COMMENT '来源链接',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `collect_count` INT DEFAULT 0 COMMENT '收藏数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-草稿 1-发布 2-下架',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶: 0-否 1-是',
    `is_recommend` TINYINT DEFAULT 0 COMMENT '是否推荐: 0-否 1-是',
    `created_by` BIGINT NOT NULL COMMENT '创建人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_difficulty` (`difficulty`),
    KEY `idx_status` (`status`),
    KEY `idx_created_by` (`created_by`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_view_count` (`view_count`),
    FULLTEXT KEY `ft_title_content` (`title`, `content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

-- 题目标签关联表
CREATE TABLE `question_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_tag` (`question_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目标签关联表';

-- 题目点赞表
CREATE TABLE `question_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_user` (`question_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目点赞表';

-- ============================================
-- 5. 评论相关表
-- ============================================

-- 评论表（支持多级回复）
CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容(Markdown)',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID，0表示顶级评论',
    `reply_to_user_id` BIGINT DEFAULT NULL COMMENT '回复目标用户ID',
    `root_id` BIGINT DEFAULT 0 COMMENT '根评论ID（顶级评论的ID）',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `reply_count` INT DEFAULT 0 COMMENT '回复数',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-隐藏 1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (`id`),
    KEY `idx_question_id` (`question_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_root_id` (`root_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 评论点赞表
CREATE TABLE `comment_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `comment_id` BIGINT NOT NULL COMMENT '评论ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

-- ============================================
-- 6. 收藏相关表
-- ============================================

-- 收藏夹表
CREATE TABLE `favorite_folder` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏夹ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '收藏夹名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `cover` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    `is_public` TINYINT DEFAULT 0 COMMENT '是否公开: 0-私密 1-公开',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认: 0-否 1-是',
    `item_count` INT DEFAULT 0 COMMENT '收藏数量',
    `sort` INT DEFAULT 0 COMMENT '排序值',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_public` (`is_public`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏夹表';

-- 收藏记录表
CREATE TABLE `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `folder_id` BIGINT NOT NULL COMMENT '收藏夹ID',
    `note` VARCHAR(500) DEFAULT NULL COMMENT '收藏备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question_folder` (`user_id`, `question_id`, `folder_id`),
    KEY `idx_folder_id` (`folder_id`),
    KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏记录表';

-- ============================================
-- 7. 学习进度相关表
-- ============================================

-- 学习进度表
CREATE TABLE `learning_progress` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-未学 1-学习中 2-已掌握 3-需复习',
    `study_count` INT DEFAULT 0 COMMENT '学习次数',
    `last_study_time` DATETIME DEFAULT NULL COMMENT '最后学习时间',
    `total_study_duration` INT DEFAULT 0 COMMENT '累计学习时长(秒)',
    `note` TEXT COMMENT '个人笔记(Markdown)',
    `mastery_level` TINYINT DEFAULT 0 COMMENT '掌握程度: 0-25-50-75-100',
    `next_review_time` DATETIME DEFAULT NULL COMMENT '下次复习时间（用于间隔重复）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
    KEY `idx_status` (`status`),
    KEY `idx_last_study_time` (`last_study_time`),
    KEY `idx_next_review_time` (`next_review_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习进度表';

-- 学习记录表（用于统计）
CREATE TABLE `learning_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID（冗余）',
    `action` VARCHAR(50) NOT NULL COMMENT '动作: VIEW-查看 STUDY-学习 COMPLETE-完成',
    `duration` INT DEFAULT 0 COMMENT '学习时长(秒)',
    `study_date` DATE NOT NULL COMMENT '学习日期',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_question_id` (`question_id`),
    KEY `idx_study_date` (`study_date`),
    KEY `idx_user_date` (`user_id`, `study_date`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习记录表';

-- 每日学习统计表（汇总表，用于快速查询）
CREATE TABLE `daily_study_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `study_date` DATE NOT NULL COMMENT '学习日期',
    `question_count` INT DEFAULT 0 COMMENT '学习题目数',
    `new_count` INT DEFAULT 0 COMMENT '新学题目数',
    `review_count` INT DEFAULT 0 COMMENT '复习题目数',
    `mastered_count` INT DEFAULT 0 COMMENT '掌握题目数',
    `total_duration` INT DEFAULT 0 COMMENT '总学习时长(秒)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `study_date`),
    KEY `idx_study_date` (`study_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日学习统计表';

-- ============================================
-- 8. 学习计划相关表
-- ============================================

-- 学习计划表
CREATE TABLE `study_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '计划ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(200) NOT NULL COMMENT '计划名称',
    `description` TEXT COMMENT '计划描述',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NOT NULL COMMENT '结束日期',
    `daily_target` INT DEFAULT 5 COMMENT '每日目标题数',
    `total_questions` INT DEFAULT 0 COMMENT '总题目数',
    `completed_questions` INT DEFAULT 0 COMMENT '已完成题目数',
    `progress` DECIMAL(5,2) DEFAULT 0.00 COMMENT '进度百分比',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-暂停 1-进行中 2-已完成 3-已放弃',
    `remind_time` TIME DEFAULT NULL COMMENT '每日提醒时间',
    `remind_enabled` TINYINT DEFAULT 0 COMMENT '是否启用提醒: 0-否 1-是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_date` (`start_date`),
    KEY `idx_end_date` (`end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习计划表';

-- 计划题目关联表
CREATE TABLE `study_plan_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `plan_id` BIGINT NOT NULL COMMENT '计划ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `sort` INT DEFAULT 0 COMMENT '排序值',
    `is_completed` TINYINT DEFAULT 0 COMMENT '是否完成: 0-否 1-是',
    `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_plan_question` (`plan_id`, `question_id`),
    KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计划题目关联表';

-- 计划每日执行记录表
CREATE TABLE `study_plan_daily` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `plan_id` BIGINT NOT NULL COMMENT '计划ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `plan_date` DATE NOT NULL COMMENT '计划日期',
    `target_count` INT DEFAULT 0 COMMENT '目标题数',
    `completed_count` INT DEFAULT 0 COMMENT '完成题数',
    `is_completed` TINYINT DEFAULT 0 COMMENT '是否达标: 0-否 1-是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_plan_date` (`plan_id`, `plan_date`),
    KEY `idx_user_date` (`user_id`, `plan_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计划每日执行记录表';

-- ============================================
-- 9. 分享相关表
-- ============================================

-- 分享记录表
CREATE TABLE `share` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分享ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `share_code` VARCHAR(50) NOT NULL COMMENT '分享码',
    `share_type` TINYINT DEFAULT 1 COMMENT '分享类型: 1-链接 2-图片 3-海报',
    `platform` VARCHAR(50) DEFAULT NULL COMMENT '分享平台: wechat/weibo/qq等',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间，NULL表示永不过期',
    `view_count` INT DEFAULT 0 COMMENT '查看次数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_share_code` (`share_code`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享记录表';

-- ============================================
-- 10. 系统相关表
-- ============================================

-- 文件上传记录表
CREATE TABLE `file_upload` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '上传用户ID',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_name` VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_url` VARCHAR(500) NOT NULL COMMENT '访问URL',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `file_type` VARCHAR(100) DEFAULT NULL COMMENT '文件类型',
    `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    `storage_type` VARCHAR(50) DEFAULT 'local' COMMENT '存储类型: local/oss/cos',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件上传记录表';

-- 系统配置表
CREATE TABLE `sys_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(50) DEFAULT 'string' COMMENT '值类型: string/number/boolean/json',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 操作日志表
CREATE TABLE `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `module` VARCHAR(50) DEFAULT NULL COMMENT '模块',
    `operation` VARCHAR(100) DEFAULT NULL COMMENT '操作',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '方法',
    `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    `request_method` VARCHAR(20) DEFAULT NULL COMMENT '请求方法',
    `request_params` TEXT COMMENT '请求参数',
    `response_data` TEXT COMMENT '响应数据',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent',
    `execution_time` INT DEFAULT 0 COMMENT '执行时长(毫秒)',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-失败 1-成功',
    `error_msg` TEXT COMMENT '错误信息',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================
-- 初始化数据
-- ============================================

-- 初始化角色
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`) VALUES
('超级管理员', 'ROLE_ADMIN', '拥有所有权限'),
('普通用户', 'ROLE_USER', '普通注册用户'),
('VIP用户', 'ROLE_VIP', 'VIP会员用户');

-- 初始化管理员账号（密码: admin123，使用BCrypt加密）
INSERT INTO `sys_user` (`username`, `email`, `password`, `nickname`, `status`, `email_verified`) VALUES
('admin', 'admin@interview-kb.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1, 1);

-- 管理员角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 初始化知识分类
INSERT INTO `category` (`name`, `parent_id`, `level`, `sort`, `icon`, `description`) VALUES
('Java', 0, 1, 1, 'java', 'Java编程语言相关知识'),
('前端', 0, 1, 2, 'frontend', '前端开发相关知识'),
('数据库', 0, 1, 3, 'database', '数据库相关知识'),
('框架', 0, 1, 4, 'framework', '常用框架相关知识'),
('计算机基础', 0, 1, 5, 'computer', '计算机基础知识'),
('系统设计', 0, 1, 6, 'design', '系统设计相关知识');

-- Java子分类
INSERT INTO `category` (`name`, `parent_id`, `level`, `sort`, `description`) VALUES
('Java基础', 1, 2, 1, 'Java基础语法和特性'),
('集合框架', 1, 2, 2, 'Java集合框架'),
('并发编程', 1, 2, 3, 'Java并发和多线程'),
('JVM', 1, 2, 4, 'Java虚拟机'),
('Java8+新特性', 1, 2, 5, 'Java8及以上版本新特性');

-- 集合框架三级分类
INSERT INTO `category` (`name`, `parent_id`, `level`, `sort`, `description`) VALUES
('ArrayList', 8, 3, 1, 'ArrayList原理与应用'),
('HashMap', 8, 3, 2, 'HashMap原理与应用'),
('ConcurrentHashMap', 8, 3, 3, 'ConcurrentHashMap原理与应用');

-- 前端子分类
INSERT INTO `category` (`name`, `parent_id`, `level`, `sort`, `description`) VALUES
('HTML/CSS', 2, 2, 1, 'HTML和CSS基础'),
('JavaScript', 2, 2, 2, 'JavaScript语言'),
('TypeScript', 2, 2, 3, 'TypeScript语言'),
('Vue', 2, 2, 4, 'Vue框架'),
('React', 2, 2, 5, 'React框架');

-- 初始化标签
INSERT INTO `tag` (`name`, `color`, `description`) VALUES
('高频考点', '#E74C3C', '面试高频出现的知识点'),
('基础必会', '#3498DB', '必须掌握的基础知识'),
('进阶', '#9B59B6', '进阶知识点'),
('源码分析', '#1ABC9C', '涉及源码分析'),
('实战经验', '#F39C12', '实际工作中常用'),
('算法', '#2ECC71', '算法相关'),
('设计模式', '#34495E', '设计模式相关');

-- 初始化系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('site_name', '面试知识库', 'string', '网站名称'),
('site_description', '专业的面试知识学习平台', 'string', '网站描述'),
('site_keywords', '面试,Java,前端,后端,算法', 'string', '网站关键词'),
('register_enabled', 'true', 'boolean', '是否开放注册'),
('upload_max_size', '10485760', 'number', '上传文件最大大小(字节)'),
('upload_allowed_types', 'jpg,jpeg,png,gif,webp,md', 'string', '允许上传的文件类型');

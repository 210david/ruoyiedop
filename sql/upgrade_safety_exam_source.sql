-- =====================================================================
-- 安全生产模块-考试功能 增量升级：考生来源双模式
--   1) biz_safety_exam 增加 attendee_source（1=来自培训 2=手动添加）
--   2) 新增 biz_safety_exam_user 手动考试名单表
-- 依赖：safety_exam_all.sql 已执行
-- 幂等性：可重复执行；新增列通过 information_schema 判断
-- 执行库：ry-vue
-- =====================================================================

-- 一、biz_safety_exam 增加考生来源字段
SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'attendee_source');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN attendee_source char(1) NOT NULL DEFAULT ''1'' COMMENT ''考生来源（1=来自培训 2=手动添加）'' AFTER record_id',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 二、手动考试名单表
CREATE TABLE IF NOT EXISTS biz_safety_exam_user (
    exam_user_id       BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    exam_id            BIGINT       NOT NULL                 COMMENT '考试场次ID（biz_safety_exam）',
    user_id            BIGINT       NOT NULL                 COMMENT '系统用户ID（sys_user）',
    user_name          VARCHAR(64)                           COMMENT '用户显示名（冗余）',
    dept_id            BIGINT                                COMMENT '归属部门ID（数据权限）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    PRIMARY KEY (exam_user_id),
    UNIQUE KEY uk_exam_user (exam_id, user_id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试手动添加人员表';

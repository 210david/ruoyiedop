-- =============================================================
-- 检查标准库模块合并到风险点管理 - 数据库迁移脚本
-- 
-- 变更内容：
--   1. 为 biz_safety_risk_point 表增加重大隐患相关字段
--   2. 删除检查标准库的菜单及按钮权限
--   3. 删除检查标准库数据表
-- 执行顺序：先加字段 → 再删菜单 → 最后删表
-- =============================================================

USE `ry-vue`;

-- =============================================================
-- 1. 为风险点表增加重大隐患相关字段（从检查标准库合并）
--    使用存储过程安全添加列（兼容MySQL，避免列已存在时报错）
-- =============================================================

DROP PROCEDURE IF EXISTS add_risk_point_columns;
DELIMITER //
CREATE PROCEDURE add_risk_point_columns()
BEGIN
    -- is_major_hazard
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'biz_safety_risk_point' AND column_name = 'is_major_hazard'
    ) THEN
        ALTER TABLE biz_safety_risk_point
        ADD COLUMN is_major_hazard CHAR(1) DEFAULT '0' COMMENT '是否重大隐患（0=否,1=是）' AFTER check_frequency;
    END IF;

    -- major_hazard_rule
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'biz_safety_risk_point' AND column_name = 'major_hazard_rule'
    ) THEN
        ALTER TABLE biz_safety_risk_point
        ADD COLUMN major_hazard_rule TEXT COMMENT '重大隐患判定规则' AFTER is_major_hazard;
    END IF;

    -- compliance_file
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'biz_safety_risk_point' AND column_name = 'compliance_file'
    ) THEN
        ALTER TABLE biz_safety_risk_point
        ADD COLUMN compliance_file VARCHAR(500) COMMENT '合规文件链接' AFTER major_hazard_rule;
    END IF;
END //
DELIMITER ;
CALL add_risk_point_columns();
DROP PROCEDURE IF EXISTS add_risk_point_columns;

-- =============================================================
-- 2. 删除检查标准库菜单及按钮权限
-- =============================================================

-- 先删按钮权限（子菜单）
DELETE FROM sys_menu WHERE perms LIKE 'safety:library:%';

-- 删除角色-菜单关联中已不存在的菜单ID
DELETE FROM sys_role_menu WHERE menu_id NOT IN (SELECT menu_id FROM sys_menu);

-- =============================================================
-- 3. （可选）将标准库已有数据迁移到风险点表
--    仅迁移标准库中独有且风险点表中不存在的记录
-- =============================================================

-- 如果标准库表仍然存在且需要迁移数据，取消下面注释执行
-- INSERT INTO biz_safety_risk_point (
--     risk_code, risk_name, hazard_desc, accident_type,
--     l_value, e_value, c_value, d_value, risk_level,
--     check_standard, check_frequency,
--     is_major_hazard, major_hazard_rule, compliance_file,
--     status, create_by, create_time, remark
-- )
-- SELECT
--     CONCAT('MIG-', library_id), risk_point_name, hazard_desc, NULL,
--     l_value, e_value, c_value, d_value, risk_level,
--     check_standard, check_frequency,
--     is_major_hazard, major_hazard_rule, compliance_file,
--     status, create_by, create_time, remark
-- FROM biz_safety_std_library
-- WHERE del_flag = '0';

-- =============================================================
-- 4. 删除检查标准库数据表
-- =============================================================

DROP TABLE IF EXISTS biz_safety_std_library;

-- =============================================================
-- 验证
-- =============================================================
SELECT '=== 风险点表新增字段 ===' AS info;
SHOW COLUMNS FROM biz_safety_risk_point WHERE Field IN ('is_major_hazard', 'major_hazard_rule', 'compliance_file');

SELECT '=== 检查标准库菜单已删除 ===' AS info;
SELECT COUNT(*) AS remaining_library_menus FROM sys_menu WHERE perms LIKE 'safety:library:%';

SELECT '=== 检查标准库表已删除 ===' AS info;
SELECT COUNT(*) AS table_exists FROM information_schema.tables WHERE table_schema = 'ry-vue' AND table_name = 'biz_safety_std_library';

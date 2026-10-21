-- ============================================================
-- 财务模块 · D-13 死权限清理
--
-- 【背景】2026-10-09 权限三方比对发现 9 个菜单权限项
--   前后端均未引用（授权后无任何效果）。处置：
--
--   删除（真死权限，功能不存在或已由其他权限承担）：
--     905964 fin:ap:remove        应付删除 —— 设计上单据只作废不删除
--     905973 fin:ar:remove        应收删除 —— 同上
--     906010 fin:config:periodInit 期间初始化 —— 期间已预生成 24 期
--     905956 fin:cost:method      计价方法配置 —— 实际由 fin:config:edit 承担
--     905995 fin:gl:rule          记账规则配置 —— 实际由 fin:config:edit 承担
--     905996 fin:gl:close         期末结账   —— 实际由 fin:config:edit 承担
--
--   保留并转正（D-01 修复后已有功能使用）：
--     905951 fin:subject:import   科目导入   —— 已实现
--     905994 fin:gl:voucher:redflush 红字冲销 —— 已实现
--     906006 fin:report:export    报表导出   —— 已实现
--
-- 【注意】对全新部署：先执行 fin_02_menu.sql 再执行本脚本，
--   即可得到与既有环境一致的权限集。
--
-- 幂等：DELETE 天然幂等，可重复执行
-- 执行：mysql -uroot -p -D ry-vue < sql/fin/fin_13_dead_perms.sql
-- ============================================================

SET NAMES utf8mb4;

-- 一、清理角色-菜单关联（先删子表，避免孤儿）
DELETE FROM sys_role_menu
WHERE menu_id IN (
    SELECT menu_id FROM (
        SELECT menu_id FROM sys_menu
        WHERE menu_id IN (905964, 905973, 906010, 905956, 905995, 905996)
    ) t
);

-- 二、删除死权限菜单行
DELETE FROM sys_menu
WHERE menu_id IN (905964, 905973, 906010, 905956, 905995, 905996);

-- ------------------------------------------------------------
-- 验证：以下 6 项应为 0
-- ------------------------------------------------------------
SELECT COUNT(*) AS 残留死权限数
FROM sys_menu
WHERE menu_id IN (905964, 905973, 906010, 905956, 905995, 905996);

-- 转3项应仍存在（D-01 已实现）
SELECT perms, menu_name FROM sys_menu
WHERE perms IN ('fin:subject:import', 'fin:gl:voucher:redflush', 'fin:report:export');

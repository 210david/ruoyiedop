-- ============================================================
-- 薪酬管理功能完善升级脚本
-- 1. biz_hr_payslip 增加 batch_id（批次-明细联动）
-- 2. 新增核算/确认/发放/本人工资条菜单按钮权限
-- 执行前提：hr_all.sql 已执行
-- ============================================================

SET NAMES utf8mb4;

-- ---------- 1. 工资条表增加批次字段 ----------
ALTER TABLE `biz_hr_payslip`
    ADD COLUMN `batch_id` BIGINT DEFAULT NULL COMMENT '所属发放批次ID' AFTER `calc_status`,
    ADD KEY `idx_batch` (`batch_id`);

-- ---------- 2. 菜单按钮权限 ----------
-- 薪酬发放按钮：核算 / 确认 / 发放
SELECT @p := menu_id FROM sys_menu WHERE perms = 'hr:payroll:list' AND menu_type = 'C' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('薪酬核算', @p, 5, '#', '', 1, 0, 'F', '0', '0', 'hr:payroll:calc', '#', 'admin', sysdate(), ''),
('薪酬确认', @p, 6, '#', '', 1, 0, 'F', '0', '0', 'hr:payroll:confirm', '#', 'admin', sysdate(), ''),
('薪酬发放确认', @p, 7, '#', '', 1, 0, 'F', '0', '0', 'hr:payroll:pay', '#', 'admin', sysdate(), '');

-- 工资条按钮：明细查看（未授权者列表/详情金额脱敏）
SELECT @p2 := menu_id FROM sys_menu WHERE perms = 'hr:payslip:list' AND menu_type = 'C' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('工资条明细明文', @p2, 5, '#', '', 1, 0, 'F', '0', '0', 'hr:payslip:detail', '#', 'admin', sysdate(), '');

-- ---------- 3. 预置一个 2026 个税税率说明（若 hr_all.sql 已预置则跳过） ----------
-- hr_all.sql 已预置 7 级税率，无需重复执行

-- ---------- 4. 考勤月报自动生成按钮权限（2026-09-03 补充） ----------
SELECT @p3 := menu_id FROM sys_menu WHERE perms = 'hr:monthly:list' AND menu_type = 'C' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('考勤月报生成', @p3, 5, '#', '', 1, 0, 'F', '0', '0', 'hr:monthly:generate', '#', 'admin', sysdate(), ''));

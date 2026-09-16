-- =============================================================
-- 数据权限历史数据回填：dept_id 为空的业务数据按 create_by 反查用户部门回填
-- 适用 P0 批次表（已有 dept_id 字段）。可重复执行（仅补 NULL）。
-- 2026-09-13
-- =============================================================
USE `ry-vue`;

UPDATE mk_lead l JOIN sys_user u ON CONVERT(l.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET l.dept_id = u.dept_id WHERE l.dept_id IS NULL;
UPDATE mk_opportunity t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE mk_order t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE mk_contract t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE mk_activity t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;

UPDATE pms_purchase_order t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE pms_purchase_plan t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE pms_purchase_request t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE pms_inquiry t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;

UPDATE qms_doc t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE qms_gauge t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE qms_audit_item t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;

UPDATE biz_hr_attendance_monthly t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE biz_hr_business_trip t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE biz_hr_clock_repair t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE biz_hr_leave_balance t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE biz_hr_leave_request t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE biz_hr_outing t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE biz_hr_overtime t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;
UPDATE biz_hr_special_deduction t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;

UPDATE biz_safety_worker t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4) = CONVERT(u.user_name USING utf8mb4) SET t.dept_id = u.dept_id WHERE t.dept_id IS NULL;

-- =============================================================
-- 升级脚本: upgrade_20261114_certificate_external.sql
-- 主题: HR证书 employee_id 改可空（支持外部人员证书录入）
-- 说明: 方案 A —— employee_id 放开非空约束，配合服务层
--       「员工ID与姓名至少其一」校验；存量数据不受影响。
-- 幂等: MODIFY 重复执行无副作用
-- 目标库: RUOYI_CFSP
-- 执行: mysql -uroot -p --default-character-set=utf8mb4 RUOYI_CFSP < 本文件
-- =============================================================

-- employee_id 改可空（外部人员证书无 employee_id，按 employee_name 录入）
ALTER TABLE biz_hr_certificate MODIFY COLUMN employee_id BIGINT NULL COMMENT '员工ID（外部人员证书为空，按 employee_name 录入）';

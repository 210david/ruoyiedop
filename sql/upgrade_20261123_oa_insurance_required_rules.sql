-- ============================================================
-- 综合办公：车辆保险台账必填规则调整
-- 1) 保单号改为非必填（policy_no 允许 NULL）
-- 2) 车牌号改为必填，且须来源于车辆台账（service 层校验）
-- 3) 起保日期、到期日期改为必填（service 层 + domain 校验）
-- 执行库：按实际环境替换（开发库 ry-vue，其余环境同名库）
-- 执行日期：2026-11-23
-- 说明：仅放开 policy_no 的 NOT NULL 约束；plate_no/start_date/end_date
--       的必填由应用层（@NotBlank/@NotNull 与导入校验）保证，不强制加
--       数据库 NOT NULL，以免历史数据或外部写入失败。
-- ============================================================

USE `ruoyi_cfsp`;

-- ---------- 1. 保单号放开为可空 ----------
ALTER TABLE biz_oa_vehicle_insurance
    MODIFY COLUMN policy_no varchar(64) DEFAULT NULL COMMENT '保单号';

-- ---------- 2. 校验 ----------
SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'ruoyi_cfsp'
  AND TABLE_NAME = 'biz_oa_vehicle_insurance'
  AND COLUMN_NAME IN ('policy_no', 'plate_no', 'start_date', 'end_date');

-- 回滚（如需恢复保单号必填，先补全空值再收紧约束）：
-- UPDATE biz_oa_vehicle_insurance SET policy_no = CONCAT('AUTO-', insurance_id)
--   WHERE policy_no IS NULL OR policy_no = '';
-- ALTER TABLE biz_oa_vehicle_insurance
--     MODIFY COLUMN policy_no varchar(64) NOT NULL COMMENT '保单号';

-- ============================================================
-- 综合办公：车辆保险台账精简字段
-- 1) 去掉「投保单号」（与保单号重复）
-- 2) 去掉「保险类型」
-- 3) 去掉「保额(元)」
-- 执行库：按实际环境替换（开发库 ry-vue，其余环境同名库）
-- 执行日期：2026-11-23
-- 注意：需与新版本后端 jar 同时上线；旧 jar 仍会读写这三个列。
-- ============================================================

USE `ruoyi_lyjs`;

-- ---------- 1. 备份将被移除的字段数据 ----------
DROP TABLE IF EXISTS biz_oa_vehicle_insurance_bak_20261123;
CREATE TABLE biz_oa_vehicle_insurance_bak_20261123 AS
SELECT insurance_id, policy_no, insurance_no, insurance_type, insured_amount
FROM biz_oa_vehicle_insurance;

-- ---------- 2. 删除字段 ----------
ALTER TABLE biz_oa_vehicle_insurance
    DROP COLUMN insurance_no,
    DROP COLUMN insurance_type,
    DROP COLUMN insured_amount;

-- ---------- 3. 可选：清理「保险类型」字典 ----------
-- 该字典（oa_insurance_type）仅服务于已删除的保险类型字段，全库无其他引用
-- （已确认：flow_form.fields_json 无引用、代码中无其他读取点）。
-- 如确认不再需要，可取消下面两行注释；保留亦不影响业务。
-- DELETE FROM sys_dict_data WHERE dict_type = 'oa_insurance_type';
-- DELETE FROM sys_dict_type WHERE dict_type = 'oa_insurance_type';

-- ---------- 4. 校验 ----------
SELECT insurance_id, policy_no, plate_no, insurer, start_date, end_date, premium, status
FROM biz_oa_vehicle_insurance
WHERE del_flag = '0'
ORDER BY end_date ASC, insurance_id DESC;

-- 回滚（如需恢复字段内容，先加回字段再从备份表回填）：
-- ALTER TABLE biz_oa_vehicle_insurance
--     ADD COLUMN insurance_no   varchar(64)   DEFAULT NULL COMMENT '投保单号',
--     ADD COLUMN insurance_type varchar(20)   DEFAULT NULL COMMENT '保险类型(oa_insurance_type)',
--     ADD COLUMN insured_amount decimal(14,2) DEFAULT NULL COMMENT '保额(元)';
-- UPDATE biz_oa_vehicle_insurance t
--   JOIN biz_oa_vehicle_insurance_bak_20261123 b ON b.insurance_id = t.insurance_id
--   SET t.insurance_no = b.insurance_no,
--       t.insurance_type = b.insurance_type,
--       t.insured_amount = b.insured_amount;

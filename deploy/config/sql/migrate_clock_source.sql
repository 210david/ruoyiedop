-- =====================================================
-- 考勤打卡来源(clock_in_source / clock_out_source)扩展迁移
--
-- 原值：0=考勤机, 1=补卡, 2=手动(笼统)
-- 新值：0=考勤机, 1=补卡, 2=手动(保留兜底), 3=外出审批, 4=出差审批
--
-- 本脚本将已有的 source=2 记录根据 remark 区分更新为 3(外出) 或 4(出差)
-- =====================================================

-- 1. 外出审批生成的记录：source 2 → 3
UPDATE biz_hr_attendance_record
SET clock_in_source = '3'
WHERE clock_in_source = '2'
  AND remark LIKE '%外出%';

UPDATE biz_hr_attendance_record
SET clock_out_source = '3'
WHERE clock_out_source = '2'
  AND remark LIKE '%外出%';

-- 2. 出差审批生成的记录：source 2 → 4
UPDATE biz_hr_attendance_record
SET clock_in_source = '4'
WHERE clock_in_source = '2'
  AND remark LIKE '%出差%';

UPDATE biz_hr_attendance_record
SET clock_out_source = '4'
WHERE clock_out_source = '2'
  AND remark LIKE '%出差%';

-- 3. 验证：检查是否还有遗留的 source=2 记录（应为0或极少量无 remark 的）
SELECT COUNT(*) AS remaining_source_2 FROM biz_hr_attendance_record
WHERE clock_in_source = '2' OR clock_out_source = '2';

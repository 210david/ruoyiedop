-- ============================================================
-- 质量目标达成判定完善（2026-09-18）
--
-- 通用标准：仅当至少存在一组"目标值与实际值均已填写"的指标时
-- 才判定达成；无任何有效对比时为待定(P)，不默认达成。
-- ============================================================

ALTER TABLE qms_quality_target
    MODIFY COLUMN achievement CHAR(1) DEFAULT NULL COMMENT '达成情况（Y=达成 N=未达成 P=待定）';

-- 修正存量数据：无任何有效目标/实际对比的记录置为待定
UPDATE qms_quality_target
SET achievement = 'P'
WHERE (target_pass_rate IS NULL OR actual_pass_rate IS NULL)
  AND (target_ppm IS NULL OR actual_ppm IS NULL)
  AND (target_complaint_rate IS NULL OR actual_complaint_rate IS NULL)
  AND (target_capa_close_rate IS NULL OR actual_capa_close_rate IS NULL);

-- 核验
SELECT achievement, COUNT(*) FROM qms_quality_target GROUP BY achievement;

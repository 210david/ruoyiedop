-- ============================================================
-- WBS 任务增加工程量清单字段（供「上传工程量清单识别 WBS」使用）
-- 日期：2026-11-24
-- 说明：清单模式下 AI 提取的数量/单位/单价/合价直接落库，
--       支持后续按清单工程量做产值统计与进度计量。
-- 幂等：使用 IF NOT EXISTS 判断，可重复执行。
-- ============================================================

DROP PROCEDURE IF EXISTS pjm_wbs_add_col;
DELIMITER $$
CREATE PROCEDURE pjm_wbs_add_col()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pjm_wbs_task' AND COLUMN_NAME = 'quantity') THEN
        ALTER TABLE pjm_wbs_task ADD COLUMN quantity DECIMAL(18,4) NULL COMMENT '清单工程量' AFTER progress;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pjm_wbs_task' AND COLUMN_NAME = 'unit') THEN
        ALTER TABLE pjm_wbs_task ADD COLUMN unit VARCHAR(20) NULL COMMENT '计量单位' AFTER quantity;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pjm_wbs_task' AND COLUMN_NAME = 'unit_price') THEN
        ALTER TABLE pjm_wbs_task ADD COLUMN unit_price DECIMAL(18,4) NULL COMMENT '综合单价(元)' AFTER unit;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pjm_wbs_task' AND COLUMN_NAME = 'amount') THEN
        ALTER TABLE pjm_wbs_task ADD COLUMN amount DECIMAL(18,2) NULL COMMENT '清单合价(元)' AFTER unit_price;
    END IF;
END$$
DELIMITER ;
CALL pjm_wbs_add_col();
DROP PROCEDURE IF EXISTS pjm_wbs_add_col;

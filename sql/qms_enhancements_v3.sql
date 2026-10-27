-- =============================================
-- QMS 增量迁移 V3：检验任务表新增 inspect_method 字段
-- 说明：GB/T 2828.1 抽样检验中，检验水平（S-1~S-4, I~III）
--       决定批量→样本量字码的映射关系，是抽样计算的关键参数。
--       原表仅有 inspect_level（检验严格度：正常/加严/放宽），
--       缺少检验水平字段，导致小批量场景（如OQC出货检验）样本量计算错误。
--       本次新增 inspect_method 字段，OQC默认S-4，其他默认II。
-- =============================================

-- 1. 新增 inspect_method 列
ALTER TABLE qms_insp_task
    ADD COLUMN inspect_method VARCHAR(10) DEFAULT 'II'
        COMMENT '检验水平（S-1/S-2/S-3/S-4/I/II/III）'
        AFTER inspect_level;

-- 2. 将现有OQC类型任务的检验水平更新为S-4
UPDATE qms_insp_task SET inspect_method = 'S-4' WHERE task_type = 'OQC' AND del_flag = '0';

-- 3. 其他类型任务保持默认 II（无需更新）

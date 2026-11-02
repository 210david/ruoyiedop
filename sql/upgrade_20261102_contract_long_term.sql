-- =============================================================
-- 升级脚本：合同到期日期支持「长期有效」
-- 日期：2026-11-02
-- 说明：
--   1. mk_contract.expire_date 改为允许 NULL，NULL 表示长期有效；
--   2. 原为 NOT NULL，框架合同/年度框架协议无固定终止日时无法录入，
--      只能被迫填远期日期，导致到期提醒与报表失真。
--   3. 同步放宽：仅 expire_date，sign_date / effective_date 仍必填。
--
-- 影响面：
--   - 定时任务「合同过期标记」条件 expire_date < CURDATE()，
--     NULL 比较结果为 NULL（WHERE 中视为 false），长期有效合同不会被误标过期；
--   - 定时任务「合同临期提醒」使用 BETWEEN，NULL 不命中，不产生无效提醒。
--
-- 幂等：已允许 NULL 时重复执行无副作用。
-- 适用：已有数据的客户库增量升级（新服务器首装由全量脚本覆盖）
-- =============================================================

ALTER TABLE `mk_contract`
    MODIFY COLUMN `expire_date` DATE NULL COMMENT '到期日期（为空表示长期有效）';

-- 回退语句（注意：若库中已存在 NULL 到期日的合同，需先补齐到期日再执行）
-- ALTER TABLE `mk_contract`
--     MODIFY COLUMN `expire_date` DATE NOT NULL COMMENT '到期日期';

-- 校验查询
-- SELECT IS_NULLABLE, COLUMN_TYPE, COLUMN_COMMENT
--   FROM information_schema.COLUMNS
--  WHERE TABLE_SCHEMA = DATABASE()
--    AND TABLE_NAME = 'mk_contract'
--    AND COLUMN_NAME = 'expire_date';
-- 期望 IS_NULLABLE = YES

-- =============================================================
-- 2026-11-24 流程发起/审批热修复（幂等）
-- 背景：升级当日生产日志发现 4 类流程异常，本脚本覆盖其中 2 类库表问题：
--   1) flow_data_bus_use_apply 缺 scfj（随车附件）列 —— 用车申请表单
--      字段晚于建表新增，发起时报 Unknown column 'scfj'
--      （新 jar 已带发起前自动补列自愈，本段保证库表先行修复）
--   2) sealApply（用印申请）运行中实例缺 file_type 流程变量 ——
--      老单据审批至网关 Gateway_file 时表达式 ${file_type == ...}
--      取不到变量报 PropertyNotFoundException
-- 另 2 类为代码问题（借支金额等数字/日期字段空串写库失败 → 已改空值落 NULL；
-- 异常提示不友好 → GlobalExceptionHandler 已业务化），随新 jar 生效。
-- 幂等：可重复执行。
-- =============================================================

DROP PROCEDURE IF EXISTS sp_flow_hotfix_1124;
DELIMITER $$
CREATE PROCEDURE sp_flow_hotfix_1124()
BEGIN
  -- 1. 用车申请业务表补「随车附件」列
  IF EXISTS (
    SELECT 1 FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'flow_data_bus_use_apply'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'flow_data_bus_use_apply' AND COLUMN_NAME = 'scfj'
  ) THEN
    ALTER TABLE flow_data_bus_use_apply ADD COLUMN `scfj` varchar(2000) COMMENT '随车附件';
  END IF;

  -- 2. 用印申请（sealApply）运行中实例补 file_type 流程变量
  IF EXISTS (
    SELECT 1 FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'flow_data_17'
  ) THEN
    IF EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'flow_data_17' AND COLUMN_NAME = 'file_type'
    ) THEN
      -- 业务表已有文件类型列：取单据上的文件类型，缺值默认「其他」（网关走默认分支）
      -- 注：Flowable 7 的 ACT_RU_EXECUTION 主键为 ID_（实例执行行满足 PROC_INST_ID_ = ID_）
      INSERT INTO ACT_RU_VARIABLE
        (ID_, REV_, TYPE_, NAME_, EXECUTION_ID_, PROC_INST_ID_, TASK_ID_, BYTEARRAY_ID_,
         DOUBLE_, LONG_, TEXT_, TEXT2_)
      SELECT REPLACE(UUID(), '-', ''), 1, 'string', 'file_type',
             e.ID_, e.PROC_INST_ID_, NULL, NULL, NULL, NULL,
             COALESCE(d.file_type, '其他'), NULL
      FROM ACT_RU_EXECUTION e
      LEFT JOIN flow_data_17 d
        ON CONVERT(d.process_instance_id USING utf8mb4) COLLATE utf8mb4_general_ci
         = CONVERT(e.PROC_INST_ID_ USING utf8mb4) COLLATE utf8mb4_general_ci
      WHERE e.PROC_INST_ID_ = e.ID_
        AND e.PROC_DEF_ID_ LIKE 'sealApply:%'
        AND NOT EXISTS (
          SELECT 1 FROM ACT_RU_VARIABLE v
          WHERE v.PROC_INST_ID_ = e.PROC_INST_ID_ AND v.NAME_ = 'file_type'
        );
    ELSE
      -- 业务表无文件类型列：统一默认「其他」
      INSERT INTO ACT_RU_VARIABLE
        (ID_, REV_, TYPE_, NAME_, EXECUTION_ID_, PROC_INST_ID_, TASK_ID_, BYTEARRAY_ID_,
         DOUBLE_, LONG_, TEXT_, TEXT2_)
      SELECT REPLACE(UUID(), '-', ''), 1, 'string', 'file_type',
             e.ID_, e.PROC_INST_ID_, NULL, NULL, NULL, NULL,
             '其他', NULL
      FROM ACT_RU_EXECUTION e
      WHERE e.PROC_INST_ID_ = e.ID_
        AND e.PROC_DEF_ID_ LIKE 'sealApply:%'
        AND NOT EXISTS (
          SELECT 1 FROM ACT_RU_VARIABLE v
          WHERE v.PROC_INST_ID_ = e.PROC_INST_ID_ AND v.NAME_ = 'file_type'
        );
    END IF;
  END IF;
END$$
DELIMITER ;
CALL sp_flow_hotfix_1124();
DROP PROCEDURE IF EXISTS sp_flow_hotfix_1124;

-- 自检：无缺列、sealApply 运行实例均有 file_type 变量
SELECT 'H1.用车申请scfj列' AS check_item, COUNT(*) AS actual, '=1' AS expected,
       IF(COUNT(*) = 1, 'PASS', 'FAIL') AS result
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'flow_data_bus_use_apply' AND COLUMN_NAME = 'scfj'
UNION ALL
SELECT 'H2.用印运行实例缺file_type变量', COUNT(*), '=0', IF(COUNT(*) = 0, 'PASS', 'FAIL')
  FROM ACT_RU_EXECUTION e
 WHERE e.PROC_INST_ID_ = e.ID_
   AND e.PROC_DEF_ID_ LIKE 'sealApply:%'
   AND NOT EXISTS (
     SELECT 1 FROM ACT_RU_VARIABLE v
     WHERE v.PROC_INST_ID_ = e.PROC_INST_ID_ AND v.NAME_ = 'file_type'
   );

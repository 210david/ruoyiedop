-- =============================================================================
-- 数据库字符集统一脚本（utf8mb4 / utf8mb4_general_ci）
-- 背景：同一库内混用 utf8mb4_0900_ai_ci / utf8mb4_general_ci / utf8_bin，
--       跨表 JOIN 时触发 MySQL error 1267：
--       Illegal mix of collations (utf8mb4_general_ci,IMPLICIT) and
--       (utf8mb4_0900_ai_ci,IMPLICIT) for operation '='
--       典型表现：QMS IQC 自动生成定时任务（每日 01:00）失败。
-- 方案：库默认 + 全表 + 全列统一为 utf8mb4 / utf8mb4_general_ci
-- 适用：ry-vue（标准）/ ruoyi_lyjs / ruoyi_cfsp / ruoyi_dwx
-- 执行：先备份，再 mysql -uroot -p < fix_collation_unify.sql
-- 注意：409 张表转换需数分钟，请在业务低峰执行；脚本幂等，重复执行安全
-- =============================================================================

-- =============================================================================
-- 步骤 0：执行前体检（记录转换前状态，便于对比）
-- =============================================================================
SELECT '=== 转换前：表级排序规则分布 ===' AS stage;
SELECT table_collation, COUNT(*) AS table_count
  FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'
 GROUP BY table_collation ORDER BY table_count DESC;

SELECT '=== 转换前：列级排序规则分布 ===' AS stage;
SELECT collation_name, COUNT(*) AS column_count
  FROM information_schema.columns
 WHERE table_schema = DATABASE() AND collation_name IS NOT NULL
 GROUP BY collation_name ORDER BY column_count DESC;

-- =============================================================================
-- 步骤 1：动态生成全表转换语句并执行
-- 说明：对每张非 general_ci 的基表执行 CONVERT TO CHARACTER SET，
--       该语句会同时重写表默认字符集与所有字符型列的排序规则。
-- =============================================================================
-- 关键：Flowable 引擎表的字符集是分裂的（act_ge_*/act_hi_*/act_id_*/act_re_*/act_ru_*
-- 为 utf8_bin，其余 act_* 已是 utf8mb4_general_ci），且表间存在 65 个外键约束
-- （如 ACT_FK_BYTEARR_DEPL: act_ge_bytearray.DEPLOYMENT_ID_ -> act_re_deployment.ID_）。
-- 逐表转换时外键两端排序规则会瞬间不一致，触发 ERROR 3780。
-- 因此必须在同一会话内临时关闭外键检查，整批转换完成后再恢复。
SET @OLD_FK_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

DROP PROCEDURE IF EXISTS `sp_unify_collation`;
DELIMITER $$
CREATE PROCEDURE `sp_unify_collation`()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE tname VARCHAR(128);
    DECLARE tcoll VARCHAR(64);
    DECLARE cnt INT DEFAULT 0;
    DECLARE cur CURSOR FOR
        SELECT table_name, IFNULL(table_collation, '')
          FROM information_schema.tables
         WHERE table_schema = DATABASE()
           AND table_type = 'BASE TABLE'
           AND (table_collation IS NULL
                OR table_collation NOT IN ('utf8mb4_general_ci'));
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO tname, tcoll;
        IF done = 1 THEN
            LEAVE read_loop;
        END IF;
        SET @sql = CONCAT('ALTER TABLE `', tname,
                          '` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SET cnt = cnt + 1;
    END LOOP;
    CLOSE cur;

    SELECT CONCAT('已转换表数量: ', cnt) AS result;
END$$
DELIMITER ;

CALL `sp_unify_collation`();
DROP PROCEDURE IF EXISTS `sp_unify_collation`;

-- 恢复外键检查（整批转换完成后）
SET FOREIGN_KEY_CHECKS = @OLD_FK_CHECKS;

-- 外键完整性验证（应返回空结果集）
SELECT '=== 外键完整性验证（应为空）===' AS stage;
SELECT constraint_name, table_name
  FROM information_schema.table_constraints
 WHERE table_schema = DATABASE() AND constraint_type = 'FOREIGN KEY'
   AND constraint_name NOT IN (
       SELECT constraint_name FROM information_schema.key_column_usage
        WHERE table_schema = DATABASE() AND referenced_table_name IS NOT NULL);

-- =============================================================================
-- 步骤 2：视图重建（视图不随基表自动转换，需重建才能继承新排序规则）
-- 说明：若库中存在视图，其字符集由定义时的列决定；此处仅提示人工确认。
-- =============================================================================
SELECT '=== 视图清单（如有，请评估是否需要重建）===' AS stage;
SELECT table_name, table_collation
  FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_type = 'VIEW';

-- =============================================================================
-- 步骤 3：库默认字符集统一
-- =============================================================================
ALTER DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- =============================================================================
-- 步骤 4：转换后复核（应只剩 utf8mb4_general_ci 一种）
-- =============================================================================
SELECT '=== 转换后：表级排序规则分布 ===' AS stage;
SELECT table_collation, COUNT(*) AS table_count
  FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'
 GROUP BY table_collation ORDER BY table_count DESC;

SELECT '=== 转换后：列级排序规则分布 ===' AS stage;
SELECT collation_name, COUNT(*) AS column_count
  FROM information_schema.columns
 WHERE table_schema = DATABASE() AND collation_name IS NOT NULL
 GROUP BY collation_name ORDER BY column_count DESC;

SELECT '=== 转换后：库默认字符集 ===' AS stage;
SELECT default_character_set_name, default_collation_name
  FROM information_schema.schemata WHERE schema_name = DATABASE();

SELECT '=== 残留异常表（应为空）===' AS stage;
SELECT table_name, table_collation
  FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'
   AND (table_collation IS NULL OR table_collation <> 'utf8mb4_general_ci');

-- =============================================================================
-- 步骤 5：跨模块 JOIN 验证（应无 error 1267）
-- =============================================================================
SELECT '=== 跨模块 JOIN 验证 ===' AS stage;
SELECT COUNT(*) AS join_ok_count
  FROM qms_insp_task a
  JOIN wms_material b ON a.material_code = b.material_code;

-- =============================================================================
-- 回滚说明：字符集转换不可逆（原始排序规则信息已丢失）。
-- 如需回退，请使用执行前的全库备份恢复：
--   mysqldump -uroot -p 库名 > backup_before_collation.sql
-- =============================================================================

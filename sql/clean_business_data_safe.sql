-- ============================================================================
-- 安全清理业务数据存储过程（环境迁移/换环境时使用）
-- 用法：mysql> CALL sp_clean_business_data('RUOYI_CFSP');
-- 机制：动态遍历目标库业务表并 TRUNCATE，白名单内的基础配置/档案表永久排除，
--       未来新增模块表自动纳入清理范围且不会误清配置表。
-- 铁律：禁止再使用"按表名前缀批量 TRUNCATE"的临时脚本清理数据（2026-10-13 事故根源）
-- 清理后必须立即执行：source sql/verify_base_config.sql 校验（应无 FAIL 输出）
-- ============================================================================

USE `RUOYI_CFSP`;

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_clean_business_data$$

CREATE PROCEDURE sp_clean_business_data(IN in_db VARCHAR(64))
BEGIN
  DECLARE v_done INT DEFAULT 0;
  DECLARE v_t VARCHAR(64);
  DECLARE cur CURSOR FOR
    SELECT TABLE_NAME
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = in_db
      AND TABLE_TYPE = 'BASE TABLE'
      -- 清理范围：业务模块表 + 运营消息表
      AND ( TABLE_NAME REGEXP '^(bid_|biz_|dms_|fin_|mk_|mms_|pms_|qms_|wms_)'
            OR TABLE_NAME IN ('sys_message','sys_message_read','sys_notice','sys_notice_read') )
      -- ★ 白名单：基础配置/档案表，永远不清（新增配置表时同步维护此处与 verify_base_config.sql）★
      AND TABLE_NAME NOT IN (
        -- 编号规则（全局基础配置）
        'mk_number_rule','mk_number_rule_prefix',
        -- 财务基础配置
        'fin_subject','fin_fiscal_config','fin_period','fin_voucher_rule',
        'fin_cost_method_config','fin_ledger','fin_material_tax',
        -- 质量基础配置
        'qms_aql_plan','qms_defect_code','qms_material_attr','qms_quality_target',
        'qms_audit_check_template','qms_audit_check_template_item',
        'qms_workshop','qms_gauge','qms_insp_item',
        -- 生产组织与工艺档案
        'mms_code_rule','mms_factory','mms_workshop','mms_line','mms_station',
        'mms_shift','mms_process','mms_route','mms_route_process',
        'mms_bom','mms_bom_detail','mms_resource','mms_team',
        -- 设备基础档案
        'dms_data_collection_config','dms_equipment_category','dms_equipment',
        'dms_repair_faq','dms_inspection_route','dms_pm_plan','dms_spare_part',
        -- 人力基础档案
        'biz_hr_shift','biz_hr_workday_calendar','biz_hr_salary_item','biz_hr_employee',
        -- 主数据档案
        'mk_customer_tag','mk_customer','mk_material',
        'wms_warehouse','wms_supplier','wms_material',
        'biz_safety_area','pms_supplier_catalog'
      );
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO v_t;
    IF v_done = 1 THEN
      LEAVE read_loop;
    END IF;
    SET @s = CONCAT('TRUNCATE TABLE `', in_db, '`.`', v_t, '`');
    PREPARE st FROM @s;
    EXECUTE st;
    DEALLOCATE PREPARE st;
  END LOOP;
  CLOSE cur;

  SELECT CONCAT('清理完成。请立即执行 sql/verify_base_config.sql 校验基础配置表（应无 FAIL 行）。') AS result;
END$$

DELIMITER ;

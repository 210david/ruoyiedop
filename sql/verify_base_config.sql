-- ============================================================================
-- 基础配置表完整性校验脚本（环境迁移 / 清理业务数据 / 换库后必跑）
-- 用法：mysql -uroot -p --default-character-set=utf8mb4 < sql/verify_base_config.sql
-- 判定：底部"FAIL_TABLES"结果集为空 = 通过；有任何 FAIL 行 = 配置表被误清，
--       立即停止操作并执行基线恢复：
--         1. source sql/mk_number_rule_baseline.sql
--         2. source sql/base_config_baseline.sql
-- 换环境说明：若目标库不叫 RUOYI_CFSP，全局替换本文件中的 RUOYI_CFSP 为目标库名。
-- ============================================================================

SELECT TABLE_NAME, CF_CNT, CASE WHEN CF_CNT = 0 THEN 'FAIL-EMPTY' ELSE 'OK' END AS STATUS
FROM (
  SELECT 'mk_number_rule' TABLE_NAME, (SELECT COUNT(*) FROM `RUOYI_CFSP`.mk_number_rule) CF_CNT
  UNION ALL SELECT 'mk_number_rule_prefix', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mk_number_rule_prefix)
  UNION ALL SELECT 'fin_subject', (SELECT COUNT(*) FROM `RUOYI_CFSP`.fin_subject)
  UNION ALL SELECT 'fin_fiscal_config', (SELECT COUNT(*) FROM `RUOYI_CFSP`.fin_fiscal_config)
  UNION ALL SELECT 'fin_period', (SELECT COUNT(*) FROM `RUOYI_CFSP`.fin_period)
  UNION ALL SELECT 'fin_voucher_rule', (SELECT COUNT(*) FROM `RUOYI_CFSP`.fin_voucher_rule)
  UNION ALL SELECT 'fin_cost_method_config', (SELECT COUNT(*) FROM `RUOYI_CFSP`.fin_cost_method_config)
  UNION ALL SELECT 'fin_ledger', (SELECT COUNT(*) FROM `RUOYI_CFSP`.fin_ledger)
  UNION ALL SELECT 'fin_material_tax', (SELECT COUNT(*) FROM `RUOYI_CFSP`.fin_material_tax)
  UNION ALL SELECT 'qms_aql_plan', (SELECT COUNT(*) FROM `RUOYI_CFSP`.qms_aql_plan)
  UNION ALL SELECT 'qms_defect_code', (SELECT COUNT(*) FROM `RUOYI_CFSP`.qms_defect_code)
  UNION ALL SELECT 'qms_material_attr', (SELECT COUNT(*) FROM `RUOYI_CFSP`.qms_material_attr)
  UNION ALL SELECT 'qms_quality_target', (SELECT COUNT(*) FROM `RUOYI_CFSP`.qms_quality_target)
  UNION ALL SELECT 'qms_audit_check_template', (SELECT COUNT(*) FROM `RUOYI_CFSP`.qms_audit_check_template)
  UNION ALL SELECT 'qms_audit_check_template_item', (SELECT COUNT(*) FROM `RUOYI_CFSP`.qms_audit_check_template_item)
  UNION ALL SELECT 'qms_workshop', (SELECT COUNT(*) FROM `RUOYI_CFSP`.qms_workshop)
  UNION ALL SELECT 'qms_gauge', (SELECT COUNT(*) FROM `RUOYI_CFSP`.qms_gauge)
  UNION ALL SELECT 'qms_insp_item', (SELECT COUNT(*) FROM `RUOYI_CFSP`.qms_insp_item)
  UNION ALL SELECT 'mms_code_rule', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mms_code_rule)
  UNION ALL SELECT 'mms_factory', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mms_factory)
  UNION ALL SELECT 'mms_workshop', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mms_workshop)
  UNION ALL SELECT 'mms_line', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mms_line)
  UNION ALL SELECT 'mms_station', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mms_station)
  UNION ALL SELECT 'mms_shift', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mms_shift)
  UNION ALL SELECT 'mms_process', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mms_process)
  UNION ALL SELECT 'mms_route', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mms_route)
  UNION ALL SELECT 'mms_route_process', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mms_route_process)
  UNION ALL SELECT 'mms_bom', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mms_bom)
  UNION ALL SELECT 'mms_bom_detail', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mms_bom_detail)
  UNION ALL SELECT 'mms_resource', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mms_resource)
  UNION ALL SELECT 'mms_team', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mms_team)
  UNION ALL SELECT 'dms_data_collection_config', (SELECT COUNT(*) FROM `RUOYI_CFSP`.dms_data_collection_config)
  UNION ALL SELECT 'dms_equipment_category', (SELECT COUNT(*) FROM `RUOYI_CFSP`.dms_equipment_category)
  UNION ALL SELECT 'dms_equipment', (SELECT COUNT(*) FROM `RUOYI_CFSP`.dms_equipment)
  UNION ALL SELECT 'dms_repair_faq', (SELECT COUNT(*) FROM `RUOYI_CFSP`.dms_repair_faq)
  UNION ALL SELECT 'dms_inspection_route', (SELECT COUNT(*) FROM `RUOYI_CFSP`.dms_inspection_route)
  UNION ALL SELECT 'dms_pm_plan', (SELECT COUNT(*) FROM `RUOYI_CFSP`.dms_pm_plan)
  UNION ALL SELECT 'dms_spare_part', (SELECT COUNT(*) FROM `RUOYI_CFSP`.dms_spare_part)
  UNION ALL SELECT 'biz_hr_shift', (SELECT COUNT(*) FROM `RUOYI_CFSP`.biz_hr_shift)
  UNION ALL SELECT 'biz_hr_workday_calendar', (SELECT COUNT(*) FROM `RUOYI_CFSP`.biz_hr_workday_calendar)
  UNION ALL SELECT 'biz_hr_salary_item', (SELECT COUNT(*) FROM `RUOYI_CFSP`.biz_hr_salary_item)
  UNION ALL SELECT 'biz_hr_employee', (SELECT COUNT(*) FROM `RUOYI_CFSP`.biz_hr_employee)
  UNION ALL SELECT 'mk_customer_tag', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mk_customer_tag)
  UNION ALL SELECT 'mk_customer', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mk_customer)
  UNION ALL SELECT 'mk_material', (SELECT COUNT(*) FROM `RUOYI_CFSP`.mk_material)
  UNION ALL SELECT 'wms_warehouse', (SELECT COUNT(*) FROM `RUOYI_CFSP`.wms_warehouse)
  UNION ALL SELECT 'wms_supplier', (SELECT COUNT(*) FROM `RUOYI_CFSP`.wms_supplier)
  UNION ALL SELECT 'wms_material', (SELECT COUNT(*) FROM `RUOYI_CFSP`.wms_material)
  UNION ALL SELECT 'biz_safety_area', (SELECT COUNT(*) FROM `RUOYI_CFSP`.biz_safety_area)
  UNION ALL SELECT 'pms_supplier_catalog', (SELECT COUNT(*) FROM `RUOYI_CFSP`.pms_supplier_catalog)
) t
WHERE CF_CNT = 0;
-- ↑ 输出为空 = 校验通过

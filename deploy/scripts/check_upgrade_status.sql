-- =============================================================
-- 生产库升级状态自检脚本（只读，不改任何数据）
-- 日期：2026-11-14（补齐 C 段决策菜单检查项，与累积总入口配套）
-- 用途：核对累积升级总入口 upgrade_20261113_all.sql 三段是否执行到位：
--   A 段（20261021 基线后 43 段变更）/ B 段（20261113 增量）/ C 段（决策菜单修复）
-- 在【生产库】上执行：
--   mysql -uroot -p --default-character-set=utf8mb4 edop_jsly < check_upgrade_status.sql
-- 预期值来自本地开发库（已全量执行所有升级）；全部 PASS 即升级到位。
-- =============================================================
SELECT '20261109 BI模块表数' AS check_item, COUNT(*) AS actual, '>=5' AS expected
  FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name LIKE 'bi_%'
UNION ALL
SELECT '20261109 集成模块表数', COUNT(*), '>=2'
  FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name LIKE 'integration_%'
UNION ALL
SELECT '20261109 BI菜单数', COUNT(*), '>0'
  FROM sys_menu WHERE perms LIKE 'bi:%'
UNION ALL
SELECT '20261106 PJM项目表', COUNT(*), '>=1'
  FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='pjm_project'
UNION ALL
SELECT '20261107 PJM编号规则数', COUNT(*), '=5'
  FROM mk_number_rule WHERE rule_code LIKE 'PJM%'
UNION ALL
SELECT '20261107 项目列扩容(quality_standard=200)', CHARACTER_MAXIMUM_LENGTH, '=200'
  FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='pjm_project' AND column_name='quality_standard'
UNION ALL
SELECT '20261108 采购物料类型列', COUNT(*), '=1'
  FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='pjm_project_procurement' AND column_name='material_type'
UNION ALL
SELECT '20261109 编号规则模块列', COUNT(*), '=1'
  FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mk_number_rule' AND column_name='module'
UNION ALL
SELECT '20261110 部门领导三列', COUNT(*), '=3'
  FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='sys_dept' AND column_name IN ('leader_id','charge_leader_id','staff_count')
UNION ALL
SELECT '20261112 证书employee_id可空', IS_NULLABLE, '''YES'''
  FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_hr_certificate' AND column_name='employee_id'
UNION ALL
SELECT '20261113 安全worker补列', COUNT(*), '=3'
  FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_safety_worker' AND column_name IN ('review_date','remind_status','enterprise_id')
UNION ALL
SELECT '20261113 参训人员补列', COUNT(*), '=3'
  FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_safety_training_attendee' AND column_name IN ('exam_time','sign_in_method','enterprise_id')
UNION ALL
SELECT '20261113 考试模块表数', COUNT(*), '=8'
  FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name LIKE 'biz_safety_exam%'
UNION ALL
SELECT '20261113 考试菜单数', COUNT(*), '=25'
  FROM sys_menu WHERE perms LIKE 'safety:exam%'
UNION ALL
SELECT '20261113 题库种子数', COUNT(*), '>=29'
  FROM biz_safety_exam_question
UNION ALL
SELECT '20261113 风险点种子数', COUNT(*), '>=32'
  FROM biz_safety_risk_point WHERE engineering_category IS NOT NULL AND engineering_category<>''
UNION ALL
SELECT '编号规则菜单query格式(JSON)', COUNT(*), '=1'
  FROM sys_menu WHERE menu_name='编号规则' AND parent_id=2000 AND query LIKE '%module%'
UNION ALL
SELECT '20261113 统计分析分组为M(C段)', COUNT(*), '=11'
  FROM sys_menu WHERE menu_type='M' AND parent_id=(
    SELECT m2.menu_id FROM sys_menu m2 JOIN sys_menu bi ON m2.parent_id=bi.menu_id
    WHERE m2.menu_name='统计分析' AND m2.menu_type='M' AND bi.menu_name='决策支持' LIMIT 1)
UNION ALL
SELECT '20261113 计件报工按钮挂载(C段)', COUNT(*), '=4'
  FROM sys_menu f
  WHERE f.menu_type='F' AND f.menu_name IN ('计件报工查询','计件报工新增','计件报工修改','计件报工删除')
    AND f.parent_id=(SELECT menu_id FROM sys_menu WHERE menu_name='计件报工' AND menu_type='C' LIMIT 1)
ORDER BY check_item;

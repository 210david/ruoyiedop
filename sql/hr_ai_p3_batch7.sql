-- ============================================================
-- P3-批次七：人力轻量启用 + AI行业化预警（仅传丰CFSP环境）
-- 内容：1) hr模块启用(sys_module+菜单树)  2) hr角色配套
--       3) 健康证到期提醒定时任务        4) ai_tool行业化工具
-- 日期：2026-10-14
-- ============================================================

-- 1. hr模块启用（模块开关 V1.1：sys_module 状态 + 菜单整树恢复）
UPDATE sys_module SET status = '0', update_by = 'admin', update_time = NOW()
WHERE module_key = 'hr' AND status = '1';

DROP TEMPORARY TABLE IF EXISTS tmp_hr_menu_tree;
CREATE TEMPORARY TABLE tmp_hr_menu_tree AS
WITH RECURSIVE sub AS (
    SELECT menu_id FROM sys_menu WHERE menu_id = 905538
    UNION ALL
    SELECT m.menu_id FROM sys_menu m JOIN sub s ON m.parent_id = s.menu_id
) SELECT menu_id FROM sub;

UPDATE sys_menu SET status = '0' WHERE menu_id IN (SELECT menu_id FROM tmp_hr_menu_tree) AND status = '1';

-- 2. hr角色配套（人事专员：轻量包数据权限=本部门）
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES (700, '人事专员', 'hr_manager', 7, '1', 1, 1, '0', '0', 'admin', NOW(), 'P3人力轻量启用配套角色')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), status = '0', del_flag = '0';

DELETE FROM sys_role_menu WHERE role_id = 700;
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 700, menu_id FROM tmp_hr_menu_tree;

DROP TEMPORARY TABLE IF EXISTS tmp_hr_menu_tree;

-- 3. 健康证到期提醒定时任务（每天08:30）
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('健康证到期提醒', 'DEFAULT', 'hrCertAlertTask.pushAlerts()', '0 30 8 * * ?', '3', '1', '0', 'admin', NOW(),
        'P3：食品从业人员健康证临期每日提醒/过期提醒一次（同证书同状态去重，噪音受控）');

-- 4. AI行业化工具（NLQ工具路由直答）
INSERT INTO ai_tool (tool_name, description, param_schema, tool_type, sql_template, perm_key, status, create_by, create_time)
VALUES ('getExpiringBatchAdvice',
        '查询临期批次清单（FEFO出库顺序）。返回批次号、物料、批次数量、到期日期、剩余天数、状态。示例问句："哪些批次快到期"、"临期批次有哪些"、"过期批次清单"',
        '[{"name":"days","type":"number","desc":"临期天数阈值，默认30天"}]',
        'sql',
        'SELECT batch_code AS 批次号, material_name AS 物料, qty AS 批次数量, expire_date AS 到期日期, DATEDIFF(expire_date, CURDATE()) AS 剩余天数, batch_status AS 状态 FROM food_batch WHERE expire_date IS NOT NULL AND DATEDIFF(expire_date, CURDATE()) <= IFNULL(:days, 30) AND batch_status NOT IN (''2'', ''3'') ORDER BY expire_date ASC LIMIT 50',
        'food:batch:list', '0', 'admin', NOW());

INSERT INTO ai_tool (tool_name, description, param_schema, tool_type, sql_template, perm_key, status, create_by, create_time)
VALUES ('getLossRateAnomaly',
        '查询损耗率超阈值的生产工单（损耗率=(计划量-合格量)/计划量）。返回工单号、产品、计划量、合格量、不良量、损耗率。示例问句："哪些工单损耗异常"、"损耗率超过10%的工单"',
        '[{"name":"threshold","type":"number","desc":"损耗率阈值百分比，默认10"}]',
        'sql',
        'SELECT work_order_no AS 工单号, product_name AS 产品, plan_qty AS 计划量, qualified_qty AS 合格量, defect_qty AS 不良量, ROUND((plan_qty - IFNULL(qualified_qty, 0)) / plan_qty * 100, 2) AS 损耗率, status AS 工单状态 FROM mms_work_order WHERE plan_qty > 0 AND del_flag = ''0'' AND (plan_qty - IFNULL(qualified_qty, 0)) / plan_qty * 100 >= IFNULL(:threshold, 10) ORDER BY 损耗率 DESC LIMIT 30',
        'mms:workorder:list', '0', 'admin', NOW());

-- 5. 字典清洗核查（scope_plan P3要求"启用前完成字典字段去重清洗"——hr_ 字典30个均已存在且 dict_type 唯一，本脚本仅核查不改动）
SELECT dict_type, COUNT(*) AS items, COUNT(DISTINCT dict_label) AS distinct_labels
FROM sys_dict_data WHERE dict_type LIKE 'hr_%'
GROUP BY dict_type HAVING COUNT(DISTINCT dict_label) <> COUNT(*);

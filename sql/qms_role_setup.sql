-- =============================================
-- QMS 质量管理模块角色创建与权限分配脚本
-- 按照《质量管理角色权限设计方案》实施
-- 包含：3个业务角色创建 + 菜单权限分配
-- 设计：幂等可重复执行
-- =============================================

-- =============================================
-- 一、创建QMS业务角色
-- =============================================

-- 质量检验员（role_id=120, data_scope=4 本人数据）
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope,
  menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 120, '质量检验员', 'qms_inspector', 60, '4', 1, 1, '0', '0', 'admin', NOW(),
  '检验员角色：检验任务查看与录入、NCR登记、数据查看'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_id = 120);

-- 质量工程师（role_id=121, data_scope=3 本部门）
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope,
  menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 121, '质量工程师', 'qms_engineer', 61, '3', 1, 1, '0', '0', 'admin', NOW(),
  '工程师角色：不合格品评审、CAPA管理、供应商质量、客诉处理、追溯管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_id = 121);

-- 质量主管（role_id=122, data_scope=2 本部门及子部门）
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope,
  menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 122, '质量主管', 'qms_manager', 62, '2', 1, 1, '0', '0', 'admin', NOW(),
  '质量主管角色：全部质量管理权限，含NCR审批、质量目标、体系管理、报表导出'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_id = 122);


-- =============================================
-- 二、质量检验员（role_id=120）权限分配
-- 权限范围：所有QMS页面查看 + 查询按钮 + 检验录入 + NCR登记
-- 数据范围：仅本人数据（data_scope=4）
-- =============================================

-- 清除旧权限（确保幂等）
DELETE FROM sys_role_menu WHERE role_id = 120 AND menu_id IN (
  SELECT menu_id FROM sys_menu WHERE perms LIKE 'qms:%'
);

-- 2.1 质量管理根目录(4727)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 120, 4727
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 120 AND menu_id = 4727);

-- 2.2 所有子目录（M级菜单，parent_id=4727）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 120, m.menu_id FROM sys_menu m
WHERE m.parent_id = 4727 AND m.menu_type = 'M'
  AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 120);

-- 2.3 所有C级菜单页面（检验员需要能看到所有QMS页面）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 120, m.menu_id FROM sys_menu m
WHERE m.perms IN (
  'qms:std:list', 'qms:defect:list', 'qms:gauge:list',
  'qms:materialattr:list', 'qms:workshop:list', 'qms:aqlplan:list',
  'qms:task:list', 'qms:ncr:list', 'qms:capa:list',
  'qms:supplierEval:list', 'qms:sqmPushLog:list', 'qms:complaint:list',
  'qms:target:list', 'qms:report:list', 'qms:dashboard:view',
  'qms:trace:search', 'qms:genealogy:list', 'qms:trace:dashboard',
  'qms:trace:sn:list', 'qms:doc:list', 'qms:audit:list',
  'qms:mr:list', 'qms:esig:list'
)
AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 120);

-- 2.4 所有查询类按钮（query）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 120, menu_id FROM sys_menu
WHERE perms IN (
  'qms:std:query', 'qms:defect:query', 'qms:gauge:query',
  'qms:materialattr:query', 'qms:workshop:query', 'qms:aqlplan:query',
  'qms:task:query', 'qms:ncr:query', 'qms:capa:query',
  'qms:supplierEval:query', 'qms:complaint:query',
  'qms:target:query',
  'qms:genealogy:query', 'qms:trace:sn:query',
  'qms:doc:query', 'qms:audit:query',
  'qms:esig:query', 'qms:sqmPushLog:query'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 120);

-- 2.5 检验任务操作权限（add + edit + inspect）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 120, menu_id FROM sys_menu
WHERE perms IN ('qms:task:add', 'qms:task:edit', 'qms:task:inspect')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 120);

-- 2.6 NCR登记权限（add only，无审批/删除）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 120, menu_id FROM sys_menu
WHERE perms IN ('qms:ncr:add')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 120);

-- 2.7 追溯查询按钮
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 120, menu_id FROM sys_menu
WHERE perms IN ('qms:trace:forward', 'qms:trace:backward', 'qms:trace:report')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 120);


-- =============================================
-- 三、质量工程师（role_id=121）权限分配
-- 权限范围：CRUD + 导出 + CAPA/客诉关闭，无审批/目标管理/编码规则管理
-- 数据范围：本部门（data_scope=3）
-- =============================================

-- 清除旧权限
DELETE FROM sys_role_menu WHERE role_id = 121 AND menu_id IN (
  SELECT menu_id FROM sys_menu WHERE perms LIKE 'qms:%'
);

-- 3.1 质量管理根目录(4727) + 所有子目录(M级)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 121, 4727
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 121 AND menu_id = 4727);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 121, m.menu_id FROM sys_menu m
WHERE m.parent_id = 4727 AND m.menu_type = 'M'
  AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 121);

-- 3.2 所有C级菜单页面
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 121, m.menu_id FROM sys_menu m
WHERE m.perms IN (
  'qms:std:list', 'qms:defect:list', 'qms:gauge:list',
  'qms:materialattr:list', 'qms:workshop:list', 'qms:aqlplan:list',
  'qms:task:list', 'qms:ncr:list', 'qms:capa:list',
  'qms:supplierEval:list', 'qms:sqmPushLog:list', 'qms:complaint:list',
  'qms:target:list', 'qms:report:list', 'qms:dashboard:view',
  'qms:trace:search', 'qms:genealogy:list', 'qms:trace:dashboard',
  'qms:trace:sn:list', 'qms:doc:list', 'qms:audit:list', 'qms:mr:list',
  'qms:esig:list'
)
AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 121);

-- 3.3 质量主数据 CRUD + 导出
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 121, menu_id FROM sys_menu
WHERE perms IN (
  'qms:std:query', 'qms:std:add', 'qms:std:edit', 'qms:std:remove', 'qms:std:export',
  'qms:defect:query', 'qms:defect:add', 'qms:defect:edit', 'qms:defect:remove', 'qms:defect:export',
  'qms:gauge:query', 'qms:gauge:add', 'qms:gauge:edit', 'qms:gauge:remove', 'qms:gauge:export',
  'qms:materialattr:query', 'qms:materialattr:add', 'qms:materialattr:edit', 'qms:materialattr:remove', 'qms:materialattr:export',
  'qms:aqlplan:query', 'qms:aqlplan:add', 'qms:aqlplan:edit', 'qms:aqlplan:remove', 'qms:aqlplan:export'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 121);

-- 3.4 检验任务 CRUD + 导出 + 检验录入
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 121, menu_id FROM sys_menu
WHERE perms IN (
  'qms:task:query', 'qms:task:add', 'qms:task:edit', 'qms:task:export', 'qms:task:inspect'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 121);

-- 3.5 NCR CRUD + 导出（无审批）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 121, menu_id FROM sys_menu
WHERE perms IN (
  'qms:ncr:query', 'qms:ncr:add', 'qms:ncr:edit', 'qms:ncr:export'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 121);

-- 3.6 CAPA CRUD + 导出 + 关闭
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 121, menu_id FROM sys_menu
WHERE perms IN (
  'qms:capa:query', 'qms:capa:add', 'qms:capa:edit', 'qms:capa:remove', 'qms:capa:export', 'qms:capa:close'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 121);

-- 3.7 供应商质量评价 CRUD + 导出 + 推送日志
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 121, menu_id FROM sys_menu
WHERE perms IN (
  'qms:supplierEval:query', 'qms:supplierEval:add', 'qms:supplierEval:edit', 'qms:supplierEval:remove', 'qms:supplierEval:export',
  'qms:sqmPushLog:query', 'qms:sqmPushLog:export', 'qms:sqmPushLog:remove'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 121);

-- 3.8 客诉 CRUD + 导出 + 关闭
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 121, menu_id FROM sys_menu
WHERE perms IN (
  'qms:complaint:query', 'qms:complaint:add', 'qms:complaint:edit', 'qms:complaint:remove', 'qms:complaint:export', 'qms:complaint:close'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 121);

-- 3.9 质量目标（仅查询，无增删改）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 121, menu_id FROM sys_menu
WHERE perms IN ('qms:target:query')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 121);

-- 3.10 追溯全套
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 121, menu_id FROM sys_menu
WHERE perms IN (
  'qms:trace:forward', 'qms:trace:backward', 'qms:trace:report',
  'qms:genealogy:query', 'qms:genealogy:add', 'qms:genealogy:edit', 'qms:genealogy:remove', 'qms:genealogy:export',
  'qms:trace:sn:query', 'qms:trace:sn:add', 'qms:trace:sn:edit', 'qms:trace:sn:remove'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 121);

-- 3.11 体系管理（文档/内审 CRUD，管理评审查询+编辑）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 121, menu_id FROM sys_menu
WHERE perms IN (
  'qms:doc:query', 'qms:doc:add', 'qms:doc:edit',
  'qms:audit:query', 'qms:audit:add', 'qms:audit:edit',
  'qms:mr:query', 'qms:mr:add', 'qms:mr:edit'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 121);

-- 3.12 电子签名（查询+新增）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 121, menu_id FROM sys_menu
WHERE perms IN ('qms:esig:query', 'qms:esig:add')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 121);


-- =============================================
-- 四、质量主管（role_id=122）权限分配
-- 权限范围：全部QMS功能（含审批、目标管理、体系管理、编码规则、导出）
-- 数据范围：本部门及子部门（data_scope=2）
-- =============================================

-- 清除旧权限
DELETE FROM sys_role_menu WHERE role_id = 122 AND menu_id IN (
  SELECT menu_id FROM sys_menu WHERE perms LIKE 'qms:%'
);

-- 4.1 一次性分配全部QMS菜单（根目录+子目录+所有菜单项）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 122, m.menu_id FROM sys_menu m
WHERE (m.perms LIKE 'qms:%' OR m.menu_id = 4727 OR m.parent_id = 4727
  OR m.parent_id IN (SELECT menu_id FROM sys_menu WHERE parent_id = 4727))
  AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 122);

-- 4.2 补充编码规则
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 122, menu_id FROM sys_menu
WHERE perms IN ('qms:numberRule:query', 'qms:numberRule:list')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 122);


-- =============================================
-- 五、验证结果
-- =============================================

SELECT '===== QMS角色创建与权限分配完成 =====' AS result;

-- 角色验证
SELECT role_id, role_name, role_key, data_scope,
  CASE data_scope WHEN '1' THEN '全部数据' WHEN '2' THEN '本部门及子部门'
  WHEN '3' THEN '本部门' WHEN '4' THEN '本人数据' WHEN '5' THEN '自定义' END AS data_scope_desc
FROM sys_role WHERE role_id IN (120, 121, 122) ORDER BY role_id;

-- 各角色权限数量统计
SELECT r.role_id, r.role_name,
  COUNT(rm.menu_id) AS menu_count
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id
LEFT JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE r.role_id IN (120, 121, 122) AND (m.perms LIKE 'qms:%' OR m.menu_id = 4727 OR m.parent_id = 4727)
GROUP BY r.role_id, r.role_name
ORDER BY r.role_id;

-- 检验员关键权限验证
SELECT '检验员' AS role_name, m.perms,
  IF(rm.role_id IS NOT NULL, '✅已分配', '❌未分配') AS status
FROM sys_menu m
LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id AND rm.role_id = 120
WHERE m.perms IN (
  'qms:task:list', 'qms:task:query', 'qms:task:add', 'qms:task:edit', 'qms:task:inspect',
  'qms:ncr:list', 'qms:ncr:query', 'qms:ncr:add',
  'qms:std:query', 'qms:esig:query',
  'qms:ncr:approve', 'qms:capa:close', 'qms:target:add', 'qms:doc:remove'
) ORDER BY m.perms;

-- 工程师关键权限验证
SELECT '工程师' AS role_name, m.perms,
  IF(rm.role_id IS NOT NULL, '✅已分配', '❌未分配') AS status
FROM sys_menu m
LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id AND rm.role_id = 121
WHERE m.perms IN (
  'qms:std:export', 'qms:ncr:edit', 'qms:ncr:export',
  'qms:capa:add', 'qms:capa:close', 'qms:capa:export',
  'qms:complaint:close', 'qms:supplierEval:export',
  'qms:genealogy:export', 'qms:esig:add',
  'qms:ncr:approve', 'qms:target:add', 'qms:mr:edit'
) ORDER BY m.perms;

-- 主管关键权限验证
SELECT '主管' AS role_name, m.perms,
  IF(rm.role_id IS NOT NULL, '✅已分配', '❌未分配') AS status
FROM sys_menu m
LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id AND rm.role_id = 122
WHERE m.perms IN (
  'qms:ncr:approve', 'qms:target:add', 'qms:target:edit', 'qms:target:remove', 'qms:target:export',
  'qms:doc:remove', 'qms:audit:remove', 'qms:mr:remove', 'qms:mr:export',
  'qms:capa:close', 'qms:complaint:close', 'qms:esig:remove',
  'qms:numberRule:query'
) ORDER BY m.perms;

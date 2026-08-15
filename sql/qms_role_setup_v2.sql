-- =============================================
-- QMS 质量管理模块新增角色创建与权限分配脚本
-- 在原有3个角色（120-122）基础上新增4个角色（123-126）
-- 设计：幂等可重复执行
-- =============================================

-- =============================================
-- 一、新增QMS业务角色
-- =============================================

-- 质量总监（role_id=123, data_scope=1 全部数据）
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope,
  menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 123, '质量总监', 'qms_director', 63, '1', 1, 1, '0', '0', 'admin', NOW(),
  '质量总监角色：全公司质量决策，全部数据范围，含质量目标制定、管理评审主导、NCR终审'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_id = 123);

-- 供应商质量工程师（role_id=124, data_scope=3 本部门）
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope,
  menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 124, '供应商质量工程师', 'qms_sqe', 64, '3', 1, 1, '0', '0', 'admin', NOW(),
  'SQE角色：供应商质量评价、推送日志、客诉8D回复、来料检验标准维护'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_id = 124);

-- 质量体系专员（role_id=125, data_scope=3 本部门）
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope,
  menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 125, '质量体系专员', 'qms_doc_admin', 65, '3', 1, 1, '0', '0', 'admin', NOW(),
  '体系专员角色：质量文档管理、内部审核、管理评审组织、编码规则维护'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_id = 125);

-- 质量数据分析员（role_id=126, data_scope=1 全部数据）
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope,
  menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 126, '质量数据分析员', 'qms_analyst', 66, '1', 1, 1, '0', '0', 'admin', NOW(),
  '数据分析员角色：质量报表查看导出、质量看板、质量目标监控、追溯查询'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_id = 126);


-- =============================================
-- 二、质量总监（role_id=123）权限分配
-- 权限范围：全部QMS功能 + 全部数据（与主管相同，但数据范围=全部）
-- 特殊：质量目标制定/管理评审主导/编码规则管理
-- =============================================

-- 清除旧权限（确保幂等）
DELETE FROM sys_role_menu WHERE role_id = 123 AND menu_id IN (
  SELECT menu_id FROM sys_menu WHERE perms LIKE 'qms:%'
);

-- 2.1 一次性分配全部QMS菜单（根目录+子目录+所有菜单项）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 123, m.menu_id FROM sys_menu m
WHERE (m.perms LIKE 'qms:%' OR m.menu_id = 4727 OR m.parent_id = 4727
  OR m.parent_id IN (SELECT menu_id FROM sys_menu WHERE parent_id = 4727))
  AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 123);

-- 2.2 编码规则全部权限（含增删改）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 123, menu_id FROM sys_menu
WHERE perms IN ('qms:numberRule:query', 'qms:numberRule:list',
  'qms:numberRule:add', 'qms:numberRule:edit', 'qms:numberRule:remove', 'qms:numberRule:export')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 123);


-- =============================================
-- 三、供应商质量工程师/SQE（role_id=124）权限分配
-- 权限范围：供应商质量评价+推送日志+客诉+检验标准+追溯+电子签名
-- =============================================

-- 清除旧权限
DELETE FROM sys_role_menu WHERE role_id = 124 AND menu_id IN (
  SELECT menu_id FROM sys_menu WHERE perms LIKE 'qms:%'
);

-- 3.1 质量管理根目录(4727) + 子目录(M级)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 124, 4727
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 124 AND menu_id = 4727);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 124, m.menu_id FROM sys_menu m
WHERE m.parent_id = 4727 AND m.menu_type = 'M'
  AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);

-- 3.2 C级菜单页面（SQE需要看到的页面）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 124, m.menu_id FROM sys_menu m
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
AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);

-- 3.3 检验标准 CRUD + 导出（SQE维护来料检验标准）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 124, menu_id FROM sys_menu
WHERE perms IN (
  'qms:std:query', 'qms:std:add', 'qms:std:edit', 'qms:std:remove', 'qms:std:export'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);

-- 3.4 缺陷代码查询（SQE需要了解缺陷分类）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 124, menu_id FROM sys_menu
WHERE perms IN ('qms:defect:query', 'qms:defect:export')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);

-- 3.5 物料质量属性 CRUD + 导出（SQE维护供应商物料质量属性）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 124, menu_id FROM sys_menu
WHERE perms IN (
  'qms:materialattr:query', 'qms:materialattr:add', 'qms:materialattr:edit', 'qms:materialattr:remove', 'qms:materialattr:export'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);

-- 3.6 抽样方案查询
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 124, menu_id FROM sys_menu
WHERE perms IN ('qms:aqlplan:query', 'qms:aqlplan:export')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);

-- 3.7 检验任务查询+导出（SQE查看来料检验结果）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 124, menu_id FROM sys_menu
WHERE perms IN ('qms:task:query', 'qms:task:export')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);

-- 3.8 NCR查询+导出（SQE查看不合格品记录）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 124, menu_id FROM sys_menu
WHERE perms IN ('qms:ncr:query', 'qms:ncr:export')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);

-- 3.9 CAPA查询+编辑+导出（SQE参与CAPA分析和执行）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 124, menu_id FROM sys_menu
WHERE perms IN ('qms:capa:query', 'qms:capa:add', 'qms:capa:edit', 'qms:capa:export')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);

-- 3.10 供应商质量评价 CRUD + 导出（SQE核心功能）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 124, menu_id FROM sys_menu
WHERE perms IN (
  'qms:supplierEval:query', 'qms:supplierEval:add', 'qms:supplierEval:edit', 'qms:supplierEval:remove', 'qms:supplierEval:export'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);

-- 3.11 推送日志查询+导出+删除
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 124, menu_id FROM sys_menu
WHERE perms IN ('qms:sqmPushLog:query', 'qms:sqmPushLog:export', 'qms:sqmPushLog:remove')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);

-- 3.12 客诉 CRUD + 导出 + 关闭（SQE处理客诉8D回复）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 124, menu_id FROM sys_menu
WHERE perms IN (
  'qms:complaint:query', 'qms:complaint:add', 'qms:complaint:edit', 'qms:complaint:export', 'qms:complaint:close'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);

-- 3.13 追溯全套（SQE追溯来料质量）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 124, menu_id FROM sys_menu
WHERE perms IN (
  'qms:trace:forward', 'qms:trace:backward', 'qms:trace:report',
  'qms:genealogy:query', 'qms:genealogy:export',
  'qms:trace:sn:query', 'qms:trace:sn:add', 'qms:trace:sn:edit'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);

-- 3.14 质量目标查询（SQE查看供应商质量目标）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 124, menu_id FROM sys_menu
WHERE perms IN ('qms:target:query')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);

-- 3.15 电子签名（查询+新增）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 124, menu_id FROM sys_menu
WHERE perms IN ('qms:esig:query', 'qms:esig:add')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 124);


-- =============================================
-- 四、质量体系专员（role_id=125）权限分配
-- 权限范围：体系管理（文档/内审/管理评审）+编码规则+电子签名+其他查询
-- =============================================

-- 清除旧权限
DELETE FROM sys_role_menu WHERE role_id = 125 AND menu_id IN (
  SELECT menu_id FROM sys_menu WHERE perms LIKE 'qms:%'
);

-- 4.1 质量管理根目录(4727) + 子目录(M级)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 125, 4727
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 125 AND menu_id = 4727);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 125, m.menu_id FROM sys_menu m
WHERE m.parent_id = 4727 AND m.menu_type = 'M'
  AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 125);

-- 4.2 C级菜单页面（体系专员需要看到的页面）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 125, m.menu_id FROM sys_menu m
WHERE m.perms IN (
  'qms:std:list', 'qms:defect:list', 'qms:gauge:list',
  'qms:materialattr:list', 'qms:workshop:list', 'qms:aqlplan:list',
  'qms:task:list', 'qms:ncr:list', 'qms:capa:list',
  'qms:supplierEval:list', 'qms:sqmPushLog:list', 'qms:complaint:list',
  'qms:target:list', 'qms:report:list', 'qms:dashboard:view',
  'qms:trace:search', 'qms:genealogy:list', 'qms:trace:dashboard',
  'qms:trace:sn:list', 'qms:doc:list', 'qms:audit:list', 'qms:mr:list',
  'qms:esig:list', 'qms:numberRule:list'
)
AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 125);

-- 4.3 质量文档 CRUD + 导出（体系专员核心功能）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 125, menu_id FROM sys_menu
WHERE perms IN (
  'qms:doc:query', 'qms:doc:add', 'qms:doc:edit', 'qms:doc:remove', 'qms:doc:export'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 125);

-- 4.4 内部审核 CRUD + 导出（体系专员核心功能）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 125, menu_id FROM sys_menu
WHERE perms IN (
  'qms:audit:query', 'qms:audit:add', 'qms:audit:edit', 'qms:audit:remove', 'qms:audit:export'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 125);

-- 4.5 管理评审 CRUD + 导出（体系专员组织管理评审）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 125, menu_id FROM sys_menu
WHERE perms IN (
  'qms:mr:query', 'qms:mr:add', 'qms:mr:edit', 'qms:mr:remove', 'qms:mr:export'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 125);

-- 4.6 编码规则 CRUD + 导出（体系专员维护编码规则）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 125, menu_id FROM sys_menu
WHERE perms IN (
  'qms:numberRule:query', 'qms:numberRule:add', 'qms:numberRule:edit', 'qms:numberRule:remove', 'qms:numberRule:export'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 125);

-- 4.7 其他查询类权限（体系专员需要查看质量数据用于体系文件编写）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 125, menu_id FROM sys_menu
WHERE perms IN (
  'qms:std:query', 'qms:defect:query', 'qms:gauge:query',
  'qms:materialattr:query', 'qms:workshop:query', 'qms:aqlplan:query',
  'qms:task:query', 'qms:ncr:query', 'qms:capa:query',
  'qms:supplierEval:query', 'qms:complaint:query',
  'qms:target:query',
  'qms:genealogy:query', 'qms:trace:sn:query',
  'qms:sqmPushLog:query', 'qms:esig:query',
  'qms:trace:forward', 'qms:trace:backward', 'qms:trace:report'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 125);

-- 4.8 电子签名（查询+新增）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 125, menu_id FROM sys_menu
WHERE perms IN ('qms:esig:query', 'qms:esig:add')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 125);


-- =============================================
-- 五、质量数据分析员（role_id=126）权限分配
-- 权限范围：全部查询+导出+看板+报表+质量目标+追溯，无增删改
-- =============================================

-- 清除旧权限
DELETE FROM sys_role_menu WHERE role_id = 126 AND menu_id IN (
  SELECT menu_id FROM sys_menu WHERE perms LIKE 'qms:%'
);

-- 5.1 质量管理根目录(4727) + 子目录(M级)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 126, 4727
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 126 AND menu_id = 4727);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 126, m.menu_id FROM sys_menu m
WHERE m.parent_id = 4727 AND m.menu_type = 'M'
  AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 126);

-- 5.2 所有C级菜单页面
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 126, m.menu_id FROM sys_menu m
WHERE m.perms IN (
  'qms:std:list', 'qms:defect:list', 'qms:gauge:list',
  'qms:materialattr:list', 'qms:workshop:list', 'qms:aqlplan:list',
  'qms:task:list', 'qms:ncr:list', 'qms:capa:list',
  'qms:supplierEval:list', 'qms:sqmPushLog:list', 'qms:complaint:list',
  'qms:target:list', 'qms:report:list', 'qms:dashboard:view',
  'qms:trace:search', 'qms:genealogy:list', 'qms:trace:dashboard',
  'qms:trace:sn:list', 'qms:doc:list', 'qms:audit:list', 'qms:mr:list',
  'qms:esig:list', 'qms:numberRule:list'
)
AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 126);

-- 5.3 所有查询类按钮
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 126, menu_id FROM sys_menu
WHERE perms IN (
  'qms:std:query', 'qms:defect:query', 'qms:gauge:query',
  'qms:materialattr:query', 'qms:workshop:query', 'qms:aqlplan:query',
  'qms:task:query', 'qms:ncr:query', 'qms:capa:query',
  'qms:supplierEval:query', 'qms:complaint:query',
  'qms:target:query',
  'qms:genealogy:query', 'qms:trace:sn:query',
  'qms:doc:query', 'qms:audit:query', 'qms:mr:query',
  'qms:esig:query', 'qms:sqmPushLog:query', 'qms:numberRule:query'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 126);

-- 5.4 所有导出类按钮（数据分析员需要导出报表）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 126, menu_id FROM sys_menu
WHERE perms IN (
  'qms:std:export', 'qms:defect:export', 'qms:gauge:export',
  'qms:materialattr:export', 'qms:aqlplan:export',
  'qms:task:export', 'qms:ncr:export', 'qms:capa:export',
  'qms:supplierEval:export', 'qms:complaint:export',
  'qms:target:export',
  'qms:genealogy:export', 'qms:doc:export', 'qms:audit:export', 'qms:mr:export',
  'qms:sqmPushLog:export', 'qms:numberRule:export'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 126);

-- 5.5 追溯查询按钮
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 126, menu_id FROM sys_menu
WHERE perms IN ('qms:trace:forward', 'qms:trace:backward', 'qms:trace:report')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 126);


-- =============================================
-- 六、验证结果
-- =============================================

SELECT '===== QMS新增角色创建与权限分配完成 =====' AS result;

-- 角色验证
SELECT role_id, role_name, role_key, data_scope,
  CASE data_scope WHEN '1' THEN '全部数据' WHEN '2' THEN '本部门及子部门'
  WHEN '3' THEN '本部门' WHEN '4' THEN '本人数据' WHEN '5' THEN '自定义' END AS data_scope_desc
FROM sys_role WHERE role_id IN (123, 124, 125, 126) ORDER BY role_id;

-- 各角色权限数量统计
SELECT r.role_id, r.role_name, COUNT(rm.menu_id) AS menu_count
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id
LEFT JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE r.role_id IN (120, 121, 122, 123, 124, 125, 126)
  AND (m.perms LIKE 'qms:%' OR m.menu_id = 4727 OR m.parent_id = 4727)
GROUP BY r.role_id, r.role_name ORDER BY r.role_id;

-- 质量总监关键权限验证
SELECT '总监' AS role_name, m.perms,
  IF(rm.role_id IS NOT NULL, '✅已分配', '❌未分配') AS status
FROM sys_menu m
LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id AND rm.role_id = 123
WHERE m.perms IN (
  'qms:ncr:approve', 'qms:target:add', 'qms:target:edit', 'qms:target:remove', 'qms:target:export',
  'qms:mr:add', 'qms:mr:edit', 'qms:mr:remove',
  'qms:numberRule:add', 'qms:numberRule:edit', 'qms:numberRule:remove',
  'qms:doc:remove', 'qms:audit:remove'
) ORDER BY m.perms;

-- SQE关键权限验证
SELECT 'SQE' AS role_name, m.perms,
  IF(rm.role_id IS NOT NULL, '✅已分配', '❌未分配') AS status
FROM sys_menu m
LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id AND rm.role_id = 124
WHERE m.perms IN (
  'qms:supplierEval:add', 'qms:supplierEval:edit', 'qms:supplierEval:remove', 'qms:supplierEval:export',
  'qms:sqmPushLog:query', 'qms:sqmPushLog:export', 'qms:sqmPushLog:remove',
  'qms:complaint:close', 'qms:std:add', 'qms:std:edit',
  'qms:ncr:approve', 'qms:mr:edit', 'qms:doc:remove'
) ORDER BY m.perms;

-- 体系专员关键权限验证
SELECT '体系专员' AS role_name, m.perms,
  IF(rm.role_id IS NOT NULL, '✅已分配', '❌未分配') AS status
FROM sys_menu m
LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id AND rm.role_id = 125
WHERE m.perms IN (
  'qms:doc:add', 'qms:doc:edit', 'qms:doc:remove', 'qms:doc:export',
  'qms:audit:add', 'qms:audit:edit', 'qms:audit:remove', 'qms:audit:export',
  'qms:mr:add', 'qms:mr:edit', 'qms:mr:remove', 'qms:mr:export',
  'qms:numberRule:add', 'qms:numberRule:edit', 'qms:numberRule:remove',
  'qms:ncr:approve', 'qms:supplierEval:add', 'qms:capa:close'
) ORDER BY m.perms;

-- 数据分析员关键权限验证
SELECT '分析员' AS role_name, m.perms,
  IF(rm.role_id IS NOT NULL, '✅已分配', '❌未分配') AS status
FROM sys_menu m
LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id AND rm.role_id = 126
WHERE m.perms IN (
  'qms:report:list', 'qms:dashboard:view',
  'qms:target:query', 'qms:target:export',
  'qms:ncr:export', 'qms:capa:export', 'qms:task:export',
  'qms:std:export', 'qms:complaint:export', 'qms:supplierEval:export',
  'qms:trace:forward', 'qms:trace:backward', 'qms:trace:report',
  'qms:target:add', 'qms:ncr:approve', 'qms:doc:remove'
) ORDER BY m.perms;

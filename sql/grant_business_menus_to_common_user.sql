-- =====================================================
-- 给普通员工角色(role_id=100)分配所有业务模块【只读】权限
-- 
-- 权限策略：
--   ✅ 所有业务模块的目录(M)和菜单(C) —— 可查看所有菜单页面
--   ✅ 仅查询类按钮(:query) 和 导出类按钮(:export) —— 可查看和导出数据
--   ❌ 新增(:add)、修改(:edit)、删除(:remove)、导入(:import) —— 不可操作
--   ❌ 审批(:approve)、审核(:audit)、付款(:pay) 等流程类操作 —— 不可操作
--   ❌ 系统管理(1)、系统监控(2)、系统工具(3)、若依官网(4) —— 不分配
--
-- 业务模块范围：
--   WMS仓储管理(2000)、DMS设备管理(3000)、MK营销管理(4000)、PMS采购管理(4197)
--
-- 日期：2026-08-04
-- =====================================================

-- 0. 更新角色描述
UPDATE sys_role 
SET remark = '普通员工角色：所有业务模块只读权限（可查看菜单、查询数据、导出数据），不可新增/修改/删除'
WHERE role_id = 100;

-- 1. 清空普通员工角色现有的菜单权限
DELETE FROM sys_role_menu WHERE role_id = 100;

-- 2. 使用递归CTE查找所有业务模块菜单，并按只读策略筛选
--    MySQL 8.0+ 支持 WITH RECURSIVE
--    策略：M目录 + C菜单 全部授权；F按钮仅授权 :query 和 :export
INSERT INTO sys_role_menu (role_id, menu_id)
WITH RECURSIVE business_menu_tree AS (
    -- 基础：所有顶级业务模块目录（parent_id=0，类型为M目录，且不是系统管理/监控/工具/官网）
    SELECT menu_id, parent_id, menu_name, menu_type, perms
    FROM sys_menu
    WHERE parent_id = 0
      AND menu_type = 'M'
      AND menu_id NOT IN (1, 2, 3, 4)
      AND status = '0'

    UNION ALL

    -- 递归：所有业务模块的子菜单
    SELECT m.menu_id, m.parent_id, m.menu_name, m.menu_type, m.perms
    FROM sys_menu m
    INNER JOIN business_menu_tree bt ON m.parent_id = bt.menu_id
    WHERE m.status = '0'
)
SELECT 100, menu_id 
FROM business_menu_tree
WHERE 
    -- 目录(M)和菜单(C)：全部授权（用于查看菜单页面）
    menu_type IN ('M', 'C')
    OR 
    -- 按钮(F)：仅授权查询和导出类权限
    (menu_type = 'F' AND (
        perms LIKE '%:query' 
        OR perms LIKE '%:export'
    ));

-- 3. 补充基础权限：通知公告（仅查询，让员工能查看公司公告）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(100, 107),   -- 通知公告菜单
(100, 1035);  -- 公告查询按钮

-- =====================================================
-- 验证查询
-- =====================================================

-- 4. 验证：查看分配的菜单总数
SELECT '=== 普通员工角色只读权限统计 ===' AS info;
SELECT COUNT(*) AS total_menus FROM sys_role_menu WHERE role_id = 100;

-- 5. 验证：按菜单类型统计
SELECT '=== 按菜单类型统计 ===' AS info;
SELECT
    CASE m.menu_type
        WHEN 'M' THEN '目录(M)'
        WHEN 'C' THEN '菜单(C)'
        WHEN 'F' THEN '按钮(F)'
        ELSE m.menu_type
    END AS menu_type_name,
    COUNT(*) AS count
FROM sys_role_menu rm
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE rm.role_id = 100
GROUP BY m.menu_type
ORDER BY m.menu_type;

-- 6. 验证：确认没有分配修改/删除/新增等写操作权限
SELECT '=== 权限安全检查（应为0条记录）===' AS info;
SELECT m.menu_id, m.menu_name, m.perms, m.menu_type
FROM sys_role_menu rm
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE rm.role_id = 100
  AND m.menu_type = 'F'
  AND (
      m.perms LIKE '%:add'
      OR m.perms LIKE '%:edit'
      OR m.perms LIKE '%:remove'
      OR m.perms LIKE '%:import'
      OR m.perms LIKE '%:approve'
      OR m.perms LIKE '%:audit'
      OR m.perms LIKE '%:dispatch'
      OR m.perms LIKE '%:changeStatus'
      OR m.perms LIKE '%:resetPwd'
      OR m.perms LIKE '%:unlock'
      OR m.perms LIKE '%:forceLogout'
      OR m.perms LIKE '%:batchLogout'
      OR m.perms LIKE '%:receive'
      OR m.perms LIKE '%:assign'
      OR m.perms LIKE '%:claim'
      OR m.perms LIKE '%:merge'
      OR m.perms LIKE '%:convert'
      OR m.perms LIKE '%:status'
      OR m.perms LIKE '%:review'
      OR m.perms LIKE '%:copy'
      OR m.perms LIKE '%:issue'
      OR m.perms LIKE '%:void'
      OR m.perms LIKE '%:deliver'
      OR m.perms LIKE '%:cancel'
      OR m.perms LIKE '%:refund'
      OR m.perms LIKE '%:renew'
      OR m.perms LIKE '%:change'
      OR m.perms LIKE '%:pay'
      OR m.perms LIKE '%:confirm'
  )
ORDER BY m.menu_id;

-- 7. 验证：按顶级模块统计
SELECT '=== 各业务模块只读权限统计 ===' AS info;
SELECT
    top_menu.menu_name AS top_module,
    SUM(CASE WHEN m.menu_type = 'M' THEN 1 ELSE 0 END) AS dir_count,
    SUM(CASE WHEN m.menu_type = 'C' THEN 1 ELSE 0 END) AS menu_count,
    SUM(CASE WHEN m.menu_type = 'F' THEN 1 ELSE 0 END) AS button_count,
    COUNT(*) AS total
FROM sys_role_menu rm
JOIN sys_menu m ON rm.menu_id = m.menu_id
LEFT JOIN sys_menu top_menu ON (
    CASE
        WHEN m.parent_id = 0 THEN m.menu_id
        WHEN (SELECT parent_id FROM sys_menu WHERE menu_id = m.parent_id) = 0 THEN m.parent_id
        WHEN (SELECT parent_id FROM sys_menu WHERE menu_id = (SELECT parent_id FROM sys_menu WHERE menu_id = m.parent_id)) = 0
            THEN (SELECT parent_id FROM sys_menu WHERE menu_id = m.parent_id)
        ELSE 0
    END = top_menu.menu_id
)
WHERE rm.role_id = 100
GROUP BY top_menu.menu_name
ORDER BY top_menu.order_num;

-- 8. 列出所有分配的F按钮权限（确认只有query和export）
SELECT '=== 已分配的按钮权限明细 ===' AS info;
SELECT m.menu_id, m.menu_name, m.perms, 
       p.menu_name AS parent_menu
FROM sys_role_menu rm
JOIN sys_menu m ON rm.menu_id = m.menu_id
LEFT JOIN sys_menu p ON m.parent_id = p.menu_id
WHERE rm.role_id = 100
  AND m.menu_type = 'F'
ORDER BY m.perms;

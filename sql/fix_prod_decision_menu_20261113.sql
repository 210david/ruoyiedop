-- ============================================================
-- 生产环境修复脚本：决策支持>统计分析 子树 + 计件报工按钮挂载
-- 生成日期: 2026-11-13
-- 修复内容:
--   A. 统计分析下 设备管理/生产管控/产品设计/财务管理 生产库被建成 C(菜单)
--      且未挂子菜单 → 统一改回 M(目录) 并挂回各模块统计报表
--   B. 计件报工查询/新增/修改/删除 4 个按钮挂错到根目录 → 挂回"计件报工"菜单
-- 说明: 全部按名称+component 匹配，可重复执行（幂等）
-- ⚠️ 执行前先备份: CREATE TABLE sys_menu_bak_20261113 AS SELECT * FROM sys_menu;
-- ============================================================

-- 0. 备份（防呆，已存在则跳过）
CREATE TABLE IF NOT EXISTS sys_menu_bak_20261113 AS SELECT * FROM sys_menu WHERE 1=0;
INSERT INTO sys_menu_bak_20261113 SELECT * FROM sys_menu WHERE menu_id NOT IN (SELECT menu_id FROM sys_menu_bak_20261113);

-- 定位统计分析目录（决策支持下的那个）
SET @biStats := (SELECT m.menu_id FROM sys_menu m
                 JOIN sys_menu bi ON m.parent_id = bi.menu_id
                 WHERE m.menu_name='统计分析' AND m.menu_type='M' AND bi.menu_name='决策支持' LIMIT 1);
SELECT @biStats AS '统计分析目录ID(应为非NULL)';

-- ============================================================
-- A1. 统计分析下 11 个分组统一纠正为 M(目录)
-- ============================================================
UPDATE sys_menu SET menu_type='M', visible='0', status='0'
WHERE parent_id=@biStats AND menu_type<>'M'
  AND menu_name IN ('仓储管理','设备管理','营销管理','采购管理','安全生产管理',
                    '质量管理','生产管控','产品设计','人力资源管理','AI中心','财务管理');

-- 分组 ID 定位
SET @gWms   := (SELECT menu_id FROM sys_menu WHERE parent_id=@biStats AND menu_name='仓储管理' LIMIT 1);
SET @gDms   := (SELECT menu_id FROM sys_menu WHERE parent_id=@biStats AND menu_name='设备管理' LIMIT 1);
SET @gMk    := (SELECT menu_id FROM sys_menu WHERE parent_id=@biStats AND menu_name='营销管理' LIMIT 1);
SET @gPms   := (SELECT menu_id FROM sys_menu WHERE parent_id=@biStats AND menu_name='采购管理' LIMIT 1);
SET @gSafe  := (SELECT menu_id FROM sys_menu WHERE parent_id=@biStats AND menu_name='安全生产管理' LIMIT 1);
SET @gQms   := (SELECT menu_id FROM sys_menu WHERE parent_id=@biStats AND menu_name='质量管理' LIMIT 1);
SET @gMms   := (SELECT menu_id FROM sys_menu WHERE parent_id=@biStats AND menu_name='生产管控' LIMIT 1);
SET @gPdm   := (SELECT menu_id FROM sys_menu WHERE parent_id=@biStats AND menu_name='产品设计' LIMIT 1);
SET @gHr    := (SELECT menu_id FROM sys_menu WHERE parent_id=@biStats AND menu_name='人力资源管理' LIMIT 1);
SET @gAi    := (SELECT menu_id FROM sys_menu WHERE parent_id=@biStats AND menu_name='AI中心' LIMIT 1);
SET @gFin   := (SELECT menu_id FROM sys_menu WHERE parent_id=@biStats AND menu_name='财务管理' LIMIT 1);

-- ============================================================
-- A2. 各模块统计报表挂回对应分组（按 menu_name+component 匹配，幂等）
-- ============================================================
-- 设备管理: 库存流水报表(1) 设备统计分析(20) 设备数据大屏(21)
UPDATE sys_menu SET parent_id=@gDms, order_num=1,  menu_name='库存流水报表' WHERE component='dms/partstock/index'    AND menu_name='库存流水报表';
UPDATE sys_menu SET parent_id=@gDms, order_num=20 WHERE component='dms/dashboard/index'     AND menu_name='设备统计分析';
UPDATE sys_menu SET parent_id=@gDms, order_num=21 WHERE component='dms/equipment/screen'    AND menu_name='设备数据大屏';

-- 生产管控: OEE分析(1) 生产日报(2) 工单完工分析(3) 生产统计分析(27)
UPDATE sys_menu SET parent_id=@gMms, order_num=1  WHERE component='mms/oee/index'             AND menu_name='OEE分析';
UPDATE sys_menu SET parent_id=@gMms, order_num=2  WHERE component='mms/daily/index'           AND menu_name='生产日报';
UPDATE sys_menu SET parent_id=@gMms, order_num=3  WHERE component='mms/finishAnalysis/index'  AND menu_name='工单完工分析';
UPDATE sys_menu SET parent_id=@gMms, order_num=27 WHERE component='mms/dashboard/index'       AND menu_name='生产统计分析';

-- 产品设计: 统计分析(1)
UPDATE sys_menu SET parent_id=@gPdm, order_num=1 WHERE component='pdm/statistics/index' AND menu_name='统计分析';

-- 财务管理: 资产负债表(1) 利润表(2) 现金流量表(3) 往来对账(4) 账龄分析(5)
UPDATE sys_menu SET parent_id=@gFin, order_num=1 WHERE component='fin/report/index' AND menu_name='资产负债表';
UPDATE sys_menu SET parent_id=@gFin, order_num=2 WHERE component='fin/report/index' AND menu_name='利润表';
UPDATE sys_menu SET parent_id=@gFin, order_num=3 WHERE component='fin/report/index' AND menu_name='现金流量表';
UPDATE sys_menu SET parent_id=@gFin, order_num=4 WHERE component='fin/report/index' AND menu_name='往来对账';
UPDATE sys_menu SET parent_id=@gFin, order_num=5 WHERE component='fin/report/index' AND menu_name='账龄分析';

-- 采购/安全/质量/生产等看板挂载（与本地结构一致，若生产已有则幂等跳过）
UPDATE sys_menu SET parent_id=@gPms,  order_num=1, menu_name='采购统计分析' WHERE menu_id=4461;
UPDATE sys_menu SET parent_id=@gSafe, order_num=1 WHERE component='safety/training/hours/index' AND menu_name='学时统计';
UPDATE sys_menu SET parent_id=@gSafe, order_num=2 WHERE component='safety/dashboard/index'      AND menu_name='安全生产驾驶舱';
UPDATE sys_menu SET parent_id=@gQms,  order_num=1 WHERE component='qms/report/index'     AND menu_name='数据报表';
UPDATE sys_menu SET parent_id=@gQms,  order_num=2 WHERE component='qms/dashboard/index'  AND menu_name='质量统计分析';
UPDATE sys_menu SET parent_id=@gMms,  order_num=1 WHERE component='mms/oee/index'        AND menu_name='OEE分析';
UPDATE sys_menu SET parent_id=@gHr,   order_num=1 WHERE component='hr/statistics/index'  AND menu_name='统计报表';
UPDATE sys_menu SET parent_id=@gAi,   order_num=1 WHERE component='ai/usage/index'       AND menu_name='用量统计';

-- ============================================================
-- B. 计件报工 4 个按钮挂回"计件报工"菜单（按名称匹配，幂等）
-- ============================================================
-- 若生产缺"计件报工"菜单(C)则按本地结构补建（父级=薪资管理）
SET @xzgl := (SELECT menu_id FROM sys_menu WHERE menu_name='薪资管理' AND menu_type='M' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '计件报工', @xzgl, 6, 'jjbg', 'hr/jjbg/index', 1, 0, 'C', '0', '0', 'hr:jjbg:list', 'form', 'admin', NOW(), '计件报工菜单(生产修复补建)'
WHERE @xzgl IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='计件报工' AND menu_type='C');

SET @jjbg := (SELECT menu_id FROM sys_menu WHERE menu_name='计件报工' AND menu_type='C' LIMIT 1);
UPDATE sys_menu SET parent_id=@jjbg
WHERE menu_type='F' AND menu_name IN ('计件报工查询','计件报工新增','计件报工修改','计件报工删除')
  AND parent_id <> @jjbg;

-- ============================================================
-- C. 验收查询（执行后核对）
-- ============================================================
-- C1. 统计分析下 11 个分组应全为 M，且各分组子菜单数 > 0
SELECT m.menu_id, m.menu_name, m.menu_type,
       (SELECT COUNT(*) FROM sys_menu c WHERE c.parent_id=m.menu_id) AS child_cnt
FROM sys_menu m WHERE m.parent_id=(SELECT m2.menu_id FROM sys_menu m2
                 JOIN sys_menu bi ON m2.parent_id=bi.menu_id
                 WHERE m2.menu_name='统计分析' AND m2.menu_type='M' AND bi.menu_name='决策支持' LIMIT 1)
ORDER BY m.order_num;

-- C2. 计件报工按钮应挂在"计件报工"下
SELECT m.menu_id, m.menu_name, m.menu_type, m.parent_id, p.menu_name AS parent_name
FROM sys_menu m LEFT JOIN sys_menu p ON m.parent_id=p.menu_id
WHERE m.menu_name LIKE '计件报工%';

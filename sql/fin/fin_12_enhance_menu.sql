-- ============================================================
-- 财务模块 · P8 增强功能菜单（进项税抵扣台账 / 存货跌价准备）
--
-- 【背景】
--   后端接口已开发完成（FinInputTaxController），
--   但菜单缺入口导致功能不可达。本脚本补两个菜单页。
--
-- 【挂靠位置】
--   进项税抵扣台账 → 挂在「财务管理」(905938) 下，与应付管理平级
--   存货跌价准备 → 挂在「财务管理」(905938) 下，与存货成本平级
--
-- 【权限说明】
--   进项税：复用 fin:ap:inputTax（fin_02 已预置，本脚本不再新建）
--   跌价准备：复用 fin:cost:valuation（fin_02 已预置，本脚本不再新建）
--
-- 幂等：全部WHERE NOT EXISTS，可重复执行
-- 执行：mysql -uroot -p -D ry-vue < sql/fin/fin_12_enhance_menu.sql
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 一、查父菜单 ID（财务管理）
-- ------------------------------------------------------------
SET @parent_id := (
  SELECT parent_id FROM sys_menu WHERE route_name = 'FinAp' LIMIT 1
);

-- 兜底：若 FinAp 不存在，取所有财务顶级菜单的父ID
SET @parent_id := IFNULL(@parent_id, (
  SELECT parent_id FROM sys_menu WHERE menu_name = '财务管理' LIMIT 1
));

-- ------------------------------------------------------------
-- 二、进项税抵扣台账（fin/inputTax/index）
-- ------------------------------------------------------------
INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark
)
SELECT '进项税抵扣', @parent_id, '7', 'inputTax', 'fin/inputTax/index', 'FinInputTax',
       1, 0, 'C', '0', '0', 'fin:ap:inputTax', 'money', 'admin', sysdate(),
       '进项税额抵扣台账（认证期12个月）'
WHERE @parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name = 'FinInputTax');

-- ------------------------------------------------------------
-- 三、存货跌价准备（fin/provision/index）
-- ------------------------------------------------------------
INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark
)
SELECT '存货跌价准备', @parent_id, '8', 'provision', 'fin/provision/index', 'FinProvision',
       1, 0, 'C', '0', '0', 'fin:cost:valuation', 'warning', 'admin', sysdate(),
       '存货跌价准备计提与冲回'
WHERE @parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name = 'FinProvision');

-- ------------------------------------------------------------
-- 四、授权给相关角色
--   会计（fin_accountant）：进项税抵扣（日常税务）+ 跌价准备（期末）
--   财务经理（fin_manager）：全部
--   不给财务人员/出纳：出纳只管资金收付，不应碰税务与资产减值
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r, sys_menu m
WHERE r.role_key IN ('fin_manager', 'fin_accountant')
  AND m.route_name IN ('FinInputTax', 'FinProvision')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );

-- ------------------------------------------------------------
-- 验证
-- ------------------------------------------------------------
SELECT menu_id, menu_name, parent_id, path, component, route_name, perms
FROM sys_menu
WHERE route_name IN ('FinInputTax', 'FinProvision')
ORDER BY order_num;

SELECT r.role_key, COUNT(*) AS 授权菜单数
FROM sys_role_menu rm
JOIN sys_role r ON r.role_id = rm.role_id
JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.route_name IN ('FinInputTax', 'FinProvision')
GROUP BY r.role_key;

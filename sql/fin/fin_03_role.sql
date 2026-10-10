-- ============================================================
-- 财务管理模块 · 角色与权限分配
-- 工程：RuoYi-Vue3
--
-- 【对齐规范】unified-development-standards.md 3.4 菜单权限规范
--   + 财务内控红线（职责分离，钱账分离）
--
-- 【重要 · 不动既有角色】
--   现有 role_id=101「财务人员」(role_key=finance, role_category=system)
--   是工程早期建立的通用角色，仅授 11 项系统类菜单（字典/参数/公告/日志），
--   属其他模块在用，本脚本【不修改、不删除、不追加】该角色。
--
--   财务模块按业务模块惯例（如 safety 11x / qms 12x / dms 2xx / 600段）
--   新建独立角色段 700，role_category='fin'（字典已由 fin_06 补入）。
--
-- 【职责分离 · 三条红线】
--   1) 制单 ≠ 审核：会计可录入凭证，但【无】审核/反审核/结账权限
--   2) 钱账分离：出纳可付款/收款，但【无】凭证录入权限
--   3) 反审核仅财务经理，且属高危操作
--
-- 【幂等】全部 WHERE NOT EXISTS，可重复执行
--
-- 执行：mysql -uroot -p -D ry-vue < sql/fin/fin_03_role.sql
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 一、角色定义（700 段）
-- ------------------------------------------------------------

-- 1.1 财务经理（全权限 + 审核 + 结账 + 解锁）
INSERT INTO sys_role
    (role_name, role_key, role_sort, role_category, data_scope,
     menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT '财务经理', 'fin_manager', 70, 'fin', '1',
       1, 1, '0', '0', 'admin', sysdate(),
       '财务模块全权限：凭证录入+审核+反审核+结账+期间解锁。制单与审核可同人（中小企业现实约束），但反审核需留痕'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'fin_manager');

-- 1.2 会计（可制单，不可审核/结账）
INSERT INTO sys_role
    (role_name, role_key, role_sort, role_category, data_scope,
     menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT '会计', 'fin_accountant', 71, 'fin', '4',
       1, 1, '0', '0', 'admin', sysdate(),
       '财务会计：可录入/修改凭证、管理往来与应收应付、查看报表。【禁止】凭证审核、反审核、期末结账、期间解锁'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'fin_accountant');

-- 1.3 出纳（可收付款，不可记账）
INSERT INTO sys_role
    (role_name, role_key, role_sort, role_category, data_scope,
     menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT '出纳', 'fin_cashier', 72, 'fin', '5',
       1, 1, '0', '0', 'admin', sysdate(),
       '财务出纳：可登记收付款、办理银行对账、开票。【禁止】凭证录入与修改（钱账分离）'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'fin_cashier');

SET @finManagerId  = (SELECT role_id FROM sys_role WHERE role_key = 'fin_manager' LIMIT 1);
SET @finAccountant = (SELECT role_id FROM sys_role WHERE role_key = 'fin_accountant' LIMIT 1);
SET @finCashier    = (SELECT role_id FROM sys_role WHERE role_key = 'fin_cashier' LIMIT 1);

-- ------------------------------------------------------------
-- 二、财务经理 → 全部财务菜单与按钮
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @finManagerId, m.menu_id
FROM sys_menu m
WHERE (m.perms LIKE 'fin:%'
       OR m.route_name LIKE 'Fin%'
       OR m.path = 'fin'
       OR m.path = 'partner')
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = @finManagerId AND rm.menu_id = m.menu_id
  );

-- ------------------------------------------------------------
-- 三、会计 → 制单类权限（关键：排除 audit / cancel / close / periodUnlock）
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @finAccountant, m.menu_id
FROM sys_menu m
WHERE (
        m.perms LIKE 'fin:%'
        OR m.route_name LIKE 'Fin%'
        OR m.path = 'fin'
        OR m.path = 'partner'
      )
  -- 职责分离红线：以下高危权限一律不授予会计
  AND m.perms NOT IN (
        'fin:gl:voucher:audit',      -- 凭证审核
        'fin:gl:voucher:cancel',     -- 凭证反审核
        'fin:gl:voucher:redflush',   -- 红字冲销
        'fin:gl:close',              -- 期末结账
        'fin:config:periodUnlock',   -- 期间解锁
        'fin:config:edit',           -- 财务全局参数修改
        'fin:fund:add', 'fin:fund:edit', 'fin:fund:remove'  -- 资金账户维护
      )
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = @finAccountant AND rm.menu_id = m.menu_id
  );

-- ------------------------------------------------------------
-- 四、出纳 → 收付款与对账权限（关键：排除凭证录入）
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @finCashier, m.menu_id
FROM sys_menu m
WHERE m.perms IN (
        'fin:fund:list', 'fin:fund:query', 'fin:fund:flow', 'fin:fund:reconcile', 'fin:fund:export',
        'fin:ap:list', 'fin:ap:query', 'fin:ap:pay', 'fin:ap:writeoff', 'fin:ap:inputTax', 'fin:ap:export',
        'fin:ar:list', 'fin:ar:query', 'fin:ar:receipt', 'fin:ar:writeoff', 'fin:ar:invoice', 'fin:ar:export',
        'fin:partner:list', 'fin:partner:query',
        'fin:subject:list', 'fin:subject:query',
        'fin:report:index', 'fin:report:balance', 'fin:report:trial',
        'fin:report:ledger', 'fin:report:aging'
      )
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = @finCashier AND rm.menu_id = m.menu_id
  );

-- ------------------------------------------------------------
-- 五、验证 1：职责分离红线核查（这三条必须全部为空）
-- ------------------------------------------------------------
SELECT '会计是否被误授凭证审核权限（应为 0）' AS check_item, COUNT(*) AS cnt
FROM sys_role_menu rm JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE rm.role_id = @finAccountant AND m.perms = 'fin:gl:voucher:audit'
UNION ALL
SELECT '会计是否被误授期末结账权限（应为 0）', COUNT(*)
FROM sys_role_menu rm JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE rm.role_id = @finAccountant AND m.perms = 'fin:gl:close'
UNION ALL
SELECT '出纳是否被误授凭证录入权限（应为 0）', COUNT(*)
FROM sys_role_menu rm JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE rm.role_id = @finCashier AND m.perms = 'fin:gl:entry:add'
UNION ALL
SELECT '出纳是否被误授凭证修改权限（应为 0）', COUNT(*)
FROM sys_role_menu rm JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE rm.role_id = @finCashier AND m.perms = 'fin:gl:voucher:edit';

-- ------------------------------------------------------------
-- 六、验证 2：三角色授权数量
-- ------------------------------------------------------------
SELECT r.role_name, r.role_key, r.role_category, r.data_scope, COUNT(rm.menu_id) AS menu_cnt
FROM sys_role r
LEFT JOIN sys_role_menu rm ON rm.role_id = r.role_id
WHERE r.role_key IN ('fin_manager', 'fin_accountant', 'fin_cashier')
GROUP BY r.role_id, r.role_name, r.role_key, r.role_category, r.data_scope
ORDER BY r.role_sort;

-- ------------------------------------------------------------
-- 七、验证 3：既有 finance(101) 角色未被改动（应仍为 11 项系统类菜单）
-- ------------------------------------------------------------
SELECT r.role_id, r.role_name, COUNT(rm.menu_id) AS menu_cnt
FROM sys_role r
LEFT JOIN sys_role_menu rm ON rm.role_id = r.role_id
WHERE r.role_key = 'finance'
GROUP BY r.role_id, r.role_name;
-- 期望：仍为 11（本脚本未触碰）
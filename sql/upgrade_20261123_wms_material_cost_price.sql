-- ============================================================
-- 仓库管理：物料计价参考成本价 + 库存金额查看权限
--
-- 变更内容：
--   1) wms_material 新增 cost_price（计价参考成本价，不含税，2 位小数）
--      用途：仅作为财务入库计价在「入库明细单价缺失」时的兜底价。
--      不是采购价（采购价见 pms_supplier_catalog.unit_price），
--      也不是销售价（销售价见 mk_price_policy）。
--   2) 新增菜单权限点 wms:inventory:amount（库存金额查看）
--      金额属敏感数据，与 wms:inventory:list 分离；
--      OpenAPI / MCP 服务账号默认不授予，避免成本价对外泄露。
--
-- 执行库：按实际环境替换（开发库 ry-vue，其余环境同名库）
-- 执行日期：2026-11-23
-- 幂等性：字段存在性、权限存在性均先判断，可重复执行
-- 兼容性：需与新版本后端 jar 同时上线；旧 jar 不读该字段，可安全回滚（字段留空即可）
-- ============================================================


-- 强制 utf8mb4：Windows 下 mysql 客户端默认 gbk，会把中文注释写成乱码
SET NAMES utf8mb4;

-- ---------- 1. wms_material.cost_price ----------
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'wms_material'
      AND COLUMN_NAME = 'cost_price'
);

SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE wms_material ADD COLUMN cost_price DECIMAL(18,2) NULL DEFAULT NULL COMMENT ''计价参考成本价（不含税，入库计价兜底，非采购价/销售价）'' AFTER is_batch_manage',
    'SELECT ''cost_price 已存在，跳过'' AS note');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- 2. 库存金额查看权限点 ----------
-- 挂在「库存查询」菜单(2080)下，排序 4（避开既有 2082 query / 2083 export / 2123 remove）
SET @inv_menu_id := (SELECT menu_id FROM sys_menu WHERE perms = 'wms:inventory:list' LIMIT 1);

SET @perm_exists := (
    SELECT COUNT(*) FROM sys_menu WHERE perms = 'wms:inventory:amount'
);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame,
                      is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '库存金额查看', @inv_menu_id, 4, '', '', '', '', 1, 0, 'F', '0', '0',
       'wms:inventory:amount', '#', 'admin', sysdate(),
       '金额为敏感数据，单独授权；不授予则库存列表不返回金额字段'
FROM DUAL
WHERE @perm_exists = 0 AND @inv_menu_id IS NOT NULL;

-- 授予超级管理员（role_id=1），其余角色按需在角色管理中勾选
SET @new_perm_id := (SELECT menu_id FROM sys_menu WHERE perms = 'wms:inventory:amount' LIMIT 1);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, @new_perm_id FROM DUAL
WHERE @new_perm_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = @new_perm_id
  );

-- ---------- 3. 校验 ----------
-- 字段是否建好
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'wms_material'
  AND COLUMN_NAME = 'cost_price';

-- 权限点是否建好
SELECT menu_id, menu_name, parent_id, perms, menu_type
FROM sys_menu
WHERE perms = 'wms:inventory:amount';

-- 尚未维护兜底价的物料数（应为全量，随维护逐步下降）
SELECT COUNT(*) AS total_material,
       SUM(CASE WHEN cost_price IS NULL OR cost_price = 0 THEN 1 ELSE 0 END) AS no_cost_price
FROM wms_material
WHERE del_flag = '0';

-- ============================================================
-- 金蝶库存查询表 + 菜单
-- 日期：2026-11-14
-- 幂等：建表 IF NOT EXISTS、菜单/授权判重，可重复执行
-- ============================================================
CREATE TABLE IF NOT EXISTS `kd_inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `material_id` varchar(64) DEFAULT NULL COMMENT '金蝶商品ID',
  `material_number` varchar(64) DEFAULT NULL COMMENT '商品编号',
  `material_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `spec_model` varchar(255) DEFAULT NULL COMMENT '规格型号',
  `stock_id` varchar(64) DEFAULT NULL COMMENT '仓库ID',
  `stock_name` varchar(128) DEFAULT NULL COMMENT '仓库名称',
  `unit_name` varchar(64) DEFAULT NULL COMMENT '基本单位',
  `qty` decimal(18,4) DEFAULT NULL COMMENT '即时库存',
  `valid_qty` decimal(18,4) DEFAULT NULL COMMENT '可用库存',
  `available_out_qty` decimal(18,4) DEFAULT NULL COMMENT '可出库数量',
  `category_id` varchar(64) DEFAULT NULL COMMENT '分类ID',
  `category_name` varchar(128) DEFAULT NULL COMMENT '分类名称',
  `image_url` varchar(500) DEFAULT NULL COMMENT '商品图片地址',
  `sync_time` datetime DEFAULT NULL COMMENT '同步时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_stock` (`material_id`, `stock_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='金蝶库存查询';

-- 菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063140, '金蝶库存查询', 9063000, 5, 'inventory', 'kingdee/inventory/index', 1, 0, 'C', '0', '0', 'kingdee:inventory:list', 'shopping', 'admin', sysdate(), '金蝶库存查询'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063140);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063141, '库存查询', 9063140, 1, '', '', 1, 0, 'F', '0', '0', 'kingdee:inventory:list', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063141);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063142, '同步金蝶', 9063140, 2, '', '', 1, 0, 'F', '0', '0', 'kingdee:inventory:sync', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063142);

-- 集成页同步按钮菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063143, '库存同步', 9063000, 6, '', '', 1, 0, 'F', '0', '0', 'kingdee:sync:inventory', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063143);

-- 分配给admin角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, t.menu_id FROM (SELECT 9063140 AS menu_id UNION ALL SELECT 9063141 UNION ALL SELECT 9063142 UNION ALL SELECT 9063143) t
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = t.menu_id);

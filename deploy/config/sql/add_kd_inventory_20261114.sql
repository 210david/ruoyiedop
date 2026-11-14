-- ============================================================
-- 金蝶库存查询表 + 商品类别表 + 菜单
-- 日期：2026-11-14
-- 幂等：建表 IF NOT EXISTS、菜单/授权判重，可重复执行
-- ============================================================
CREATE TABLE IF NOT EXISTS `kd_category` (
  `id` varchar(64) NOT NULL COMMENT '金蝶类别ID',
  `number` varchar(64) DEFAULT NULL COMMENT '类别编码',
  `name` varchar(128) DEFAULT NULL COMMENT '类别名称',
  `parent_id` varchar(64) DEFAULT NULL COMMENT '上级类别ID',
  `level` int DEFAULT NULL COMMENT '层级',
  `sort` int DEFAULT NULL COMMENT '金蝶类别树顺序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='金蝶商品类别';

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
  `category_sort` int DEFAULT NULL COMMENT '商品类别排序(金蝶)',
  `root_category_id` varchar(64) DEFAULT NULL COMMENT '一级分类ID',
  `root_category_name` varchar(128) DEFAULT NULL COMMENT '一级分类名称',
  `root_category_sort` int DEFAULT NULL COMMENT '一级分类排序(金蝶)',
  `image_url` varchar(500) DEFAULT NULL COMMENT '商品图片地址',
  `sync_time` datetime DEFAULT NULL COMMENT '同步时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_stock` (`material_id`, `stock_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='金蝶库存查询';

-- 已存在表时补充商品类别排序列（金蝶商品类别展示顺序）
SET @has_cat_sort := (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'kd_inventory' AND column_name = 'category_sort'
);
SET @ddl_cat_sort := IF(@has_cat_sort = 0,
    'ALTER TABLE `kd_inventory` ADD COLUMN `category_sort` int DEFAULT NULL COMMENT ''商品类别排序(金蝶)'' AFTER `category_name`',
    'SELECT ''kd_inventory.category_sort 已存在，跳过'' AS msg');
PREPARE stmt_cat_sort FROM @ddl_cat_sort;
EXECUTE stmt_cat_sort;
DEALLOCATE PREPARE stmt_cat_sort;

-- 已存在表时补充一级分类列（金蝶两级分类）
SET @has_root_cat := (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'kd_inventory' AND column_name = 'root_category_id'
);
SET @ddl_root_cat := IF(@has_root_cat = 0,
    'ALTER TABLE `kd_inventory`
        ADD COLUMN `root_category_id` varchar(64) DEFAULT NULL COMMENT ''一级分类ID'' AFTER `category_sort`,
        ADD COLUMN `root_category_name` varchar(128) DEFAULT NULL COMMENT ''一级分类名称'' AFTER `root_category_id`,
        ADD COLUMN `root_category_sort` int DEFAULT NULL COMMENT ''一级分类排序(金蝶)'' AFTER `root_category_name`',
    'SELECT ''kd_inventory.root_category_* 已存在，跳过'' AS msg');
PREPARE stmt_root_cat FROM @ddl_root_cat;
EXECUTE stmt_root_cat;
DEALLOCATE PREPARE stmt_root_cat;

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

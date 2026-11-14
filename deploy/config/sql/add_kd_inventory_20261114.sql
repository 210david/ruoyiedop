-- 金蝶库存查询表
DROP TABLE IF EXISTS `kd_inventory`;
CREATE TABLE `kd_inventory` (
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
VALUES (9063140, '金蝶库存查询', 9063000, 5, 'inventory', 'kingdee/inventory/index', 1, 0, 'C', '0', '0', 'kingdee:inventory:list', 'shopping', 'admin', sysdate(), '金蝶库存查询');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (9063141, '库存查询', 9063140, 1, '', '', 1, 0, 'F', '0', '0', 'kingdee:inventory:list', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (9063142, '同步金蝶', 9063140, 2, '', '', 1, 0, 'F', '0', '0', 'kingdee:inventory:sync', '#', 'admin', sysdate(), '');

-- 集成页同步按钮菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (9063143, '库存同步', 9063000, 6, '', '', 1, 0, 'F', '0', '0', 'kingdee:sync:inventory', '#', 'admin', sysdate(), '');

-- 分配给admin角色
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 9063140);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 9063141);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 9063142);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 9063143);

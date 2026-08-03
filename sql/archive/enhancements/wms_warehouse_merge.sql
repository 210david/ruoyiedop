-- =====================================================
-- 仓库结构合并迁移脚本
-- 将 wms_warehouse + wms_warehouse_area + wms_warehouse_location
-- 合并为统一的树形结构表 wms_warehouse
-- =====================================================

-- 1. 修改 wms_warehouse 表，新增树形结构字段和合并字段
ALTER TABLE wms_warehouse
    ADD COLUMN parent_id        BIGINT       DEFAULT 0                COMMENT '父级ID' AFTER warehouse_id,
    ADD COLUMN ancestors        VARCHAR(500)                          COMMENT '祖级列表' AFTER parent_id,
    ADD COLUMN node_type        CHAR(1)      DEFAULT '1'              COMMENT '节点类型（1=仓库 2=仓区 3=仓位）' AFTER ancestors,
    ADD COLUMN area_type        VARCHAR(20)                           COMMENT '库区类型（仓区级）' AFTER address,
    ADD COLUMN location_type    VARCHAR(20)                           COMMENT '库位类型（仓位级）' AFTER area_type,
    ADD COLUMN capacity         DECIMAL(18,2) DEFAULT 0              COMMENT '库位容量（仓位级）' AFTER location_type,
    ADD COLUMN location_status  CHAR(1)      DEFAULT '0'              COMMENT '库位状态（仓位级，0空闲 1占用 2锁定）' AFTER capacity;

-- 2. 更新现有仓库数据：设置顶层节点属性
UPDATE wms_warehouse SET parent_id = 0, ancestors = '0', node_type = '1' WHERE parent_id IS NULL;

-- 3. 将 wms_warehouse_area 数据迁移到 wms_warehouse
INSERT INTO wms_warehouse (
    parent_id, ancestors, warehouse_code, warehouse_name, node_type,
    area_type, status, del_flag, create_by, create_time, update_by, update_time, remark
)
SELECT
    a.warehouse_id,
    CONCAT('0,', a.warehouse_id),
    a.area_code,
    a.area_name,
    '2',
    a.area_type,
    a.status,
    a.del_flag,
    a.create_by, a.create_time, a.update_by, a.update_time, a.remark
FROM wms_warehouse_area a;

-- 4. 将 wms_warehouse_location 数据迁移到 wms_warehouse
INSERT INTO wms_warehouse (
    parent_id, ancestors, warehouse_code, warehouse_name, node_type,
    area_type, location_type, capacity, location_status, status, del_flag,
    create_by, create_time, update_by, update_time, remark
)
SELECT
    l.area_id,
    CONCAT('0,', l.warehouse_id, ',', l.area_id),
    l.location_code,
    l.location_name,
    '3',
    NULL,
    l.location_type,
    l.capacity,
    l.location_status,
    l.status,
    l.del_flag,
    l.create_by, l.create_time, l.update_by, l.update_time, l.remark
FROM wms_warehouse_location l;

-- 5. 更新仓区的 ancestors（需要找到迁移后仓区的新ID）
-- 仓区的新ID是auto_increment自动生成的，需要通过编码关联
UPDATE wms_warehouse w
INNER JOIN wms_warehouse_area a ON w.warehouse_code = a.area_code AND w.node_type = '2'
SET w.parent_id = a.warehouse_id,
    w.ancestors = CONCAT('0,', a.warehouse_id);

-- 6. 更新仓位的 ancestors 和 parent_id（通过编码关联）
UPDATE wms_warehouse w
INNER JOIN wms_warehouse_location l ON w.warehouse_code = l.location_code AND w.node_type = '3'
SET w.parent_id = l.area_id,
    w.ancestors = CONCAT('0,', l.warehouse_id, ',', l.area_id);

-- 注意：仓位的 parent_id 指向的是旧的 area_id，
-- 需要更新为迁移后仓区在 wms_warehouse 中的新 warehouse_id
UPDATE wms_warehouse loc
INNER JOIN wms_warehouse_location ll ON loc.warehouse_code = ll.location_code AND loc.node_type = '3'
INNER JOIN wms_warehouse area ON area.warehouse_code = (
    SELECT a.area_code FROM wms_warehouse_area a WHERE a.area_id = ll.area_id
) AND area.node_type = '2'
SET loc.parent_id = area.warehouse_id,
    loc.ancestors = CONCAT(area.ancestors, ',', area.warehouse_id);

-- 7. 更新其他表中引用的 area_id 和 location_id
-- 这些表中的 area_id 和 location_id 仍然指向旧表的ID
-- 需要更新为 wms_warehouse 中的新 warehouse_id

-- 更新 wms_stock_take.area_id
UPDATE wms_stock_take st
INNER JOIN wms_warehouse_area a ON st.area_id = a.area_id
INNER JOIN wms_warehouse w ON w.warehouse_code = a.area_code AND w.node_type = '2'
SET st.area_id = w.warehouse_id;

-- 更新 wms_inventory.location_id
UPDATE wms_inventory i
INNER JOIN wms_warehouse_location l ON i.location_id = l.location_id
INNER JOIN wms_warehouse w ON w.warehouse_code = l.location_code AND w.node_type = '3'
SET i.location_id = w.warehouse_id;

-- 更新 wms_inventory_log.location_id
UPDATE wms_inventory_log il
INNER JOIN wms_warehouse_location l ON il.location_id = l.location_id
INNER JOIN wms_warehouse w ON w.warehouse_code = l.location_code AND w.node_type = '3'
SET il.location_id = w.warehouse_id;

-- 更新 wms_inbound_order_detail.location_id
UPDATE wms_inbound_order_detail d
INNER JOIN wms_warehouse_location l ON d.location_id = l.location_id
INNER JOIN wms_warehouse w ON w.warehouse_code = l.location_code AND w.node_type = '3'
SET d.location_id = w.warehouse_id;

-- 更新 wms_outbound_order_detail.location_id
UPDATE wms_outbound_order_detail d
INNER JOIN wms_warehouse_location l ON d.location_id = l.location_id
INNER JOIN wms_warehouse w ON w.warehouse_code = l.location_code AND w.node_type = '3'
SET d.location_id = w.warehouse_id;

-- 更新 wms_move_order.from_location_id
UPDATE wms_move_order m
INNER JOIN wms_warehouse_location l ON m.from_location_id = l.location_id
INNER JOIN wms_warehouse w ON w.warehouse_code = l.location_code AND w.node_type = '3'
SET m.from_location_id = w.warehouse_id;

-- 更新 wms_move_order.to_location_id
UPDATE wms_move_order m
INNER JOIN wms_warehouse_location l ON m.to_location_id = l.location_id
INNER JOIN wms_warehouse w ON w.warehouse_code = l.location_code AND w.node_type = '3'
SET m.to_location_id = w.warehouse_id;

-- 更新 wms_stock_take_detail.location_id
UPDATE wms_stock_take_detail d
INNER JOIN wms_warehouse_location l ON d.location_id = l.location_id
INNER JOIN wms_warehouse w ON w.warehouse_code = l.location_code AND w.node_type = '3'
SET d.location_id = w.warehouse_id;

-- 更新 wms_stock_take_snapshot.location_id
UPDATE wms_stock_take_snapshot s
INNER JOIN wms_warehouse_location l ON s.location_id = l.location_id
INNER JOIN wms_warehouse w ON w.warehouse_code = l.location_code AND w.node_type = '3'
SET s.location_id = w.warehouse_id;

-- 8. 删除旧表
DROP TABLE IF EXISTS wms_warehouse_location;
DROP TABLE IF EXISTS wms_warehouse_area;

-- 9. 添加索引
ALTER TABLE wms_warehouse ADD INDEX idx_warehouse_parent (parent_id);
ALTER TABLE wms_warehouse ADD INDEX idx_warehouse_node_type (node_type);

-- 10. 更新菜单：删除库区管理和库位管理菜单，保留仓库管理
-- 删除角色-菜单关联
DELETE FROM sys_role_menu WHERE menu_id IN (2040, 2041, 2042, 2043, 2044);
DELETE FROM sys_role_menu WHERE menu_id IN (2050, 2051, 2052, 2053, 2054);
-- 删除库区管理及其子菜单
DELETE FROM sys_menu WHERE menu_id IN (2040, 2041, 2042, 2043, 2044);
-- 删除库位管理及其子菜单
DELETE FROM sys_menu WHERE menu_id IN (2050, 2051, 2052, 2053, 2054);

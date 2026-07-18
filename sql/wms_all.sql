-- =============================================
-- WMS 仓库管理系统 - 全量SQL脚本
-- 包含：16张业务表 + 14个字典类型 + 菜单权限
-- =============================================

-- =============================================
-- 一、业务表 DDL（16张表）
-- =============================================

-- 1. 物料主数据
DROP TABLE IF EXISTS wms_material;
CREATE TABLE wms_material (
    material_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '物料ID',
    material_code    VARCHAR(64)  NOT NULL                 COMMENT '物料编码（唯一标识）',
    material_name    VARCHAR(255) NOT NULL                 COMMENT '物料名称',
    material_type    VARCHAR(20)  DEFAULT '0'              COMMENT '物料类型（字典 wms_material_type）',
    spec_model       VARCHAR(255)                          COMMENT '规格型号',
    unit             VARCHAR(20)  NOT NULL                 COMMENT '基本计量单位（字典 wms_unit）',
    barcode          VARCHAR(64)                           COMMENT '物料条码',
    shelf_life_days  INT                                   COMMENT '保质期天数',
    expiry_alert_days INT          DEFAULT 30               COMMENT '临期预警天数',
    safety_stock_min DECIMAL(18,2) DEFAULT 0               COMMENT '安全库存下限',
    safety_stock_max DECIMAL(18,2) DEFAULT 0               COMMENT '安全库存上限',
    is_expiry_manage CHAR(1)     DEFAULT '0'              COMMENT '是否效期管理（0否 1是）',
    is_batch_manage  CHAR(1)      DEFAULT '0'              COMMENT '是否批次管理（0否 1是）',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (material_id),
    UNIQUE KEY uk_material_code (material_code),
    KEY idx_material_type (material_type),
    KEY idx_material_name (material_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料主数据表';

-- 2. 供应商
DROP TABLE IF EXISTS wms_supplier;
CREATE TABLE wms_supplier (
    supplier_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '供应商ID',
    supplier_code    VARCHAR(64)  NOT NULL                 COMMENT '供应商编码',
    supplier_name    VARCHAR(255) NOT NULL                 COMMENT '供应商名称',
    contact_person   VARCHAR(64)                           COMMENT '联系人',
    contact_phone    VARCHAR(20)                           COMMENT '联系电话',
    address          VARCHAR(500)                          COMMENT '地址',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (supplier_id),
    UNIQUE KEY uk_supplier_code (supplier_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 3. 仓库结构表（仓库/仓区/仓位 树形结构）
DROP TABLE IF EXISTS wms_warehouse;
CREATE TABLE wms_warehouse (
    warehouse_id     BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    parent_id        BIGINT       DEFAULT 0                COMMENT '父级ID',
    ancestors        VARCHAR(500)                          COMMENT '祖级列表',
    warehouse_code   VARCHAR(64)  NOT NULL                 COMMENT '编码（自动生成）',
    warehouse_name   VARCHAR(128) NOT NULL                 COMMENT '名称',
    node_type        CHAR(1)      DEFAULT '1'              COMMENT '节点类型（1=仓库 2=仓区 3=仓位）',
    dept_id          BIGINT                                COMMENT '管理部门ID（仓库级）',
    manager_id       BIGINT                                COMMENT '负责人用户ID（仓库级）',
    address          VARCHAR(500)                          COMMENT '仓库地址（仓库级）',
    area_type        VARCHAR(20)                           COMMENT '库区类型（仓区级，字典 wms_area_type）',
    location_type    VARCHAR(20)                           COMMENT '库位类型（仓位级，字典 wms_location_type）',
    capacity         DECIMAL(18,2) DEFAULT 0              COMMENT '库位容量（仓位级）',
    location_status  CHAR(1)      DEFAULT '0'              COMMENT '库位状态（仓位级，0空闲 1占用 2锁定）',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (warehouse_id),
    UNIQUE KEY uk_warehouse_code (warehouse_code),
    KEY idx_warehouse_parent (parent_id),
    KEY idx_warehouse_node_type (node_type),
    KEY idx_warehouse_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库结构表（仓库/仓区/仓位）';

-- 6. 入库单头表
DROP TABLE IF EXISTS wms_inbound_order;
CREATE TABLE wms_inbound_order (
    order_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '入库单ID',
    order_no         VARCHAR(64)  NOT NULL                 COMMENT '入库单号',
    order_type       VARCHAR(20)  DEFAULT '0'              COMMENT '入库类型（字典 wms_inbound_type）',
    supplier_id      BIGINT                                COMMENT '供应商ID',
    warehouse_id     BIGINT       NOT NULL                 COMMENT '目标仓库ID',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0草稿 1待收货 2待上架 3已完成 4已作废）',
    inbound_date     DATETIME                              COMMENT '预计入库日期',
    complete_date    DATETIME                              COMMENT '实际完成日期',
    total_qty        DECIMAL(18,2) DEFAULT 0               COMMENT '总数量',
    total_amount     DECIMAL(18,2) DEFAULT 0               COMMENT '总金额',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_inbound_order_no (order_no),
    KEY idx_inbound_status (status),
    KEY idx_inbound_warehouse (warehouse_id),
    KEY idx_inbound_supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单头表';

-- 7. 入库单明细表
DROP TABLE IF EXISTS wms_inbound_order_detail;
CREATE TABLE wms_inbound_order_detail (
    detail_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    order_id         BIGINT       NOT NULL                 COMMENT '入库单ID',
    material_id      BIGINT       NOT NULL                 COMMENT '物料ID',
    plan_qty         DECIMAL(18,2) DEFAULT 0               COMMENT '计划数量',
    received_qty     DECIMAL(18,2) DEFAULT 0               COMMENT '已收数量',
    putaway_qty      DECIMAL(18,2) DEFAULT 0               COMMENT '已上架数量',
    batch_no         VARCHAR(64)                           COMMENT '批次号',
    production_date  DATE                                  COMMENT '生产日期',
    expiry_date      DATE                                  COMMENT '保质期至',
    location_id      BIGINT                                COMMENT '上架库位ID',
    unit_price       DECIMAL(18,2) DEFAULT 0               COMMENT '单价',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (detail_id),
    KEY idx_inbound_detail_order (order_id),
    KEY idx_inbound_detail_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单明细表';

-- 8. 波次主表
DROP TABLE IF EXISTS wms_wave;
CREATE TABLE wms_wave (
    wave_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '波次ID',
    wave_no          VARCHAR(64)  NOT NULL                 COMMENT '波次号',
    wave_name        VARCHAR(128)                          COMMENT '波次名称',
    warehouse_id     BIGINT       NOT NULL                 COMMENT '仓库ID',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0待拣货 1拣货中 2已完成 3已作废）',
    total_qty        DECIMAL(18,2) DEFAULT 0               COMMENT '总数量',
    order_count      INT          DEFAULT 0                COMMENT '包含订单数',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (wave_id),
    UNIQUE KEY uk_wave_no (wave_no),
    KEY idx_wave_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='波次主表';

-- 9. 出库单头表
DROP TABLE IF EXISTS wms_outbound_order;
CREATE TABLE wms_outbound_order (
    order_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '出库单ID',
    order_no         VARCHAR(64)  NOT NULL                 COMMENT '出库单号',
    order_type       VARCHAR(20)  DEFAULT '0'              COMMENT '出库类型（字典 wms_outbound_type）',
    warehouse_id     BIGINT       NOT NULL                 COMMENT '出库仓库ID',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0草稿 1待拣货 2复核中 3已完成 4已作废）',
    wave_id          BIGINT                                COMMENT '所属波次ID',
    outbound_date    DATETIME                              COMMENT '预计出库日期',
    complete_date    DATETIME                              COMMENT '实际完成日期',
    total_qty        DECIMAL(18,2) DEFAULT 0               COMMENT '总数量',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_outbound_order_no (order_no),
    KEY idx_outbound_status (status),
    KEY idx_outbound_warehouse (warehouse_id),
    KEY idx_outbound_wave (wave_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单头表';

-- 10. 出库单明细表
DROP TABLE IF EXISTS wms_outbound_order_detail;
CREATE TABLE wms_outbound_order_detail (
    detail_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    order_id         BIGINT       NOT NULL                 COMMENT '出库单ID',
    material_id      BIGINT       NOT NULL                 COMMENT '物料ID',
    batch_no         VARCHAR(64)                           COMMENT '指定批次号',
    plan_qty         DECIMAL(18,2) DEFAULT 0               COMMENT '计划数量',
    actual_qty       DECIMAL(18,2) DEFAULT 0               COMMENT '实际复核数量',
    location_id      BIGINT                                COMMENT '拣货库位ID',
    pick_qty         DECIMAL(18,2) DEFAULT 0               COMMENT '已拣货数量',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (detail_id),
    KEY idx_outbound_detail_order (order_id),
    KEY idx_outbound_detail_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单明细表';

-- 11. 库存表
DROP TABLE IF EXISTS wms_inventory;
CREATE TABLE wms_inventory (
    inventory_id     BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '库存ID',
    material_id      BIGINT       NOT NULL                 COMMENT '物料ID',
    warehouse_id     BIGINT       NOT NULL                 COMMENT '仓库ID',
    location_id      BIGINT       NOT NULL                 COMMENT '库位ID',
    batch_no         VARCHAR(64)  DEFAULT ''               COMMENT '批次号',
    qty              DECIMAL(18,2) DEFAULT 0               COMMENT '可用数量',
    lock_qty         DECIMAL(18,2) DEFAULT 0               COMMENT '锁定数量',
    version          INT          DEFAULT 0                COMMENT '乐观锁版本号',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (inventory_id),
    UNIQUE KEY uk_inventory (material_id, warehouse_id, location_id, batch_no),
    KEY idx_inventory_material (material_id),
    KEY idx_inventory_warehouse (warehouse_id),
    KEY idx_inventory_location (location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 12. 库存流水表
DROP TABLE IF EXISTS wms_inventory_log;
CREATE TABLE wms_inventory_log (
    log_id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '流水ID',
    material_id      BIGINT       NOT NULL                 COMMENT '物料ID',
    warehouse_id     BIGINT       NOT NULL                 COMMENT '仓库ID',
    location_id      BIGINT                                COMMENT '库位ID',
    batch_no         VARCHAR(64)                           COMMENT '批次号',
    change_type      VARCHAR(20)  NOT NULL                 COMMENT '变动类型（字典 wms_change_type）',
    change_qty       DECIMAL(18,2) NOT NULL                COMMENT '变动数量（正增负减）',
    before_qty       DECIMAL(18,2) DEFAULT 0               COMMENT '变动前数量',
    after_qty        DECIMAL(18,2) DEFAULT 0               COMMENT '变动后数量',
    ref_order_type   VARCHAR(20)                           COMMENT '关联单据类型',
    ref_order_no     VARCHAR(64)                           COMMENT '关联业务单据号',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '操作人',
    create_time      DATETIME                              COMMENT '操作时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (log_id),
    KEY idx_log_material (material_id),
    KEY idx_log_warehouse (warehouse_id),
    KEY idx_log_ref_order (ref_order_no),
    KEY idx_log_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- 13. 盘点单头表
DROP TABLE IF EXISTS wms_stock_take;
CREATE TABLE wms_stock_take (
    take_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '盘点单ID',
    take_no          VARCHAR(64)  NOT NULL                 COMMENT '盘点单号',
    warehouse_id     BIGINT       NOT NULL                 COMMENT '盘点仓库ID',
    area_id          BIGINT                                COMMENT '盘点库区ID',
    take_type        VARCHAR(20)  DEFAULT '0'              COMMENT '盘点类型（字典 wms_take_type）',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0待盘点 1盘点中 2待审批 3已完成 4已作废）',
    plan_date        DATE                                  COMMENT '计划盘点日期',
    start_time       DATETIME                              COMMENT '盘点开始时间',
    end_time         DATETIME                              COMMENT '盘点结束时间',
    approve_by       VARCHAR(64)                           COMMENT '审批人',
    approve_time     DATETIME                              COMMENT '审批时间',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (take_id),
    UNIQUE KEY uk_take_no (take_no),
    KEY idx_take_status (status),
    KEY idx_take_warehouse (warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单头表';

-- 14. 盘点单明细表
DROP TABLE IF EXISTS wms_stock_take_detail;
CREATE TABLE wms_stock_take_detail (
    detail_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    take_id          BIGINT       NOT NULL                 COMMENT '盘点单ID',
    material_id      BIGINT       NOT NULL                 COMMENT '物料ID',
    location_id      BIGINT       NOT NULL                 COMMENT '库位ID',
    batch_no         VARCHAR(64)                           COMMENT '批次号',
    book_qty         DECIMAL(18,2) DEFAULT 0               COMMENT '账面数量',
    actual_qty       DECIMAL(18,2)                          COMMENT '实盘数量',
    diff_qty         DECIMAL(18,2) DEFAULT 0               COMMENT '差异数量',
    diff_reason      VARCHAR(500)                          COMMENT '差异原因',
    status           CHAR(1)      DEFAULT '0'              COMMENT '明细状态（0待盘 1已盘 2已确认）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (detail_id),
    KEY idx_take_detail_order (take_id),
    KEY idx_take_detail_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单明细表';

-- 15. 盘点快照表
DROP TABLE IF EXISTS wms_stock_take_snapshot;
CREATE TABLE wms_stock_take_snapshot (
    snapshot_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '快照ID',
    take_id          BIGINT       NOT NULL                 COMMENT '盘点单ID',
    material_id      BIGINT       NOT NULL                 COMMENT '物料ID',
    location_id      BIGINT       NOT NULL                 COMMENT '库位ID',
    batch_no         VARCHAR(64)                           COMMENT '批次号',
    snapshot_qty     DECIMAL(18,2) DEFAULT 0               COMMENT '快照数量',
    snapshot_time    DATETIME                              COMMENT '快照时间',
    PRIMARY KEY (snapshot_id),
    KEY idx_snapshot_take (take_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点快照表';

-- 16. 移库单表
DROP TABLE IF EXISTS wms_move_order;
CREATE TABLE wms_move_order (
    move_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '移库单ID',
    move_no          VARCHAR(64)  NOT NULL                 COMMENT '移库单号',
    warehouse_id     BIGINT       NOT NULL                 COMMENT '仓库ID',
    material_id      BIGINT       NOT NULL                 COMMENT '物料ID',
    batch_no         VARCHAR(64)                           COMMENT '批次号',
    from_location_id BIGINT       NOT NULL                 COMMENT '源库位ID',
    to_location_id   BIGINT       NOT NULL                 COMMENT '目标库位ID',
    move_qty         DECIMAL(18,2) NOT NULL                COMMENT '移库数量',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0待审批 1已批准 2已完成 3已作废）',
    approve_by       VARCHAR(64)                           COMMENT '审批人',
    approve_time     DATETIME                              COMMENT '审批时间',
    complete_time    DATETIME                              COMMENT '完成时间',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (move_id),
    UNIQUE KEY uk_move_no (move_no),
    KEY idx_move_status (status),
    KEY idx_move_warehouse (warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移库单表';


-- =============================================
-- 二、字典数据（14个字典类型）
-- =============================================

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('物料类型', 'wms_material_type', '0', 'admin', sysdate(), '物料类型字典'),
('计量单位', 'wms_unit', '0', 'admin', sysdate(), '计量单位字典'),
('库区类型', 'wms_area_type', '0', 'admin', sysdate(), '库区类型字典'),
('库位类型', 'wms_location_type', '0', 'admin', sysdate(), '库位类型字典'),
('库位状态', 'wms_location_status', '0', 'admin', sysdate(), '库位状态字典'),
('入库类型', 'wms_inbound_type', '0', 'admin', sysdate(), '入库类型字典'),
('入库状态', 'wms_inbound_status', '0', 'admin', sysdate(), '入库状态字典'),
('出库类型', 'wms_outbound_type', '0', 'admin', sysdate(), '出库类型字典'),
('出库状态', 'wms_outbound_status', '0', 'admin', sysdate(), '出库状态字典'),
('波次状态', 'wms_wave_status', '0', 'admin', sysdate(), '波次状态字典'),
('库存变动类型', 'wms_change_type', '0', 'admin', sysdate(), '库存变动类型字典'),
('盘点类型', 'wms_take_type', '0', 'admin', sysdate(), '盘点类型字典'),
('盘点状态', 'wms_take_status', '0', 'admin', sysdate(), '盘点状态字典'),
('移库状态', 'wms_move_status', '0', 'admin', sysdate(), '移库状态字典');

-- 字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '原材料', '0', 'wms_material_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '半成品', '1', 'wms_material_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(3, '成品', '2', 'wms_material_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '辅料', '3', 'wms_material_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),

(1, '个', '0', 'wms_unit', '', '', 'Y', '0', 'admin', sysdate(), ''),
(2, '件', '1', 'wms_unit', '', '', 'N', '0', 'admin', sysdate(), ''),
(3, '箱', '2', 'wms_unit', '', '', 'N', '0', 'admin', sysdate(), ''),
(4, 'kg', '3', 'wms_unit', '', '', 'N', '0', 'admin', sysdate(), ''),
(5, '吨', '4', 'wms_unit', '', '', 'N', '0', 'admin', sysdate(), ''),
(6, '米', '5', 'wms_unit', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '存储区', '0', 'wms_area_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '收货区', '1', 'wms_area_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '发货区', '2', 'wms_area_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '退货区', '3', 'wms_area_type', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(5, '暂存区', '4', 'wms_area_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '货架位', '0', 'wms_location_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '地堆位', '1', 'wms_location_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(3, '暂存位', '2', 'wms_location_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),

(1, '空闲', '0', 'wms_location_status', '', 'success', 'Y', '0', 'admin', sysdate(), ''),
(2, '占用', '1', 'wms_location_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '锁定', '2', 'wms_location_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '采购入库', '0', 'wms_inbound_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '生产入库', '1', 'wms_inbound_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '退货入库', '2', 'wms_inbound_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '调拨入库', '3', 'wms_inbound_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '草稿', '0', 'wms_inbound_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '待收货', '1', 'wms_inbound_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '待上架', '2', 'wms_inbound_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '已完成', '3', 'wms_inbound_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(5, '已作废', '4', 'wms_inbound_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '销售出库', '0', 'wms_outbound_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '领料出库', '1', 'wms_outbound_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '调拨出库', '2', 'wms_outbound_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '退货出库', '3', 'wms_outbound_type', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '草稿', '0', 'wms_outbound_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '待拣货', '1', 'wms_outbound_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '复核中', '2', 'wms_outbound_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '已完成', '3', 'wms_outbound_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(5, '已作废', '4', 'wms_outbound_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '待拣货', '0', 'wms_wave_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '拣货中', '1', 'wms_wave_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '已完成', '2', 'wms_wave_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '已作废', '3', 'wms_wave_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '入库', '0', 'wms_change_type', '', 'success', 'Y', '0', 'admin', sysdate(), ''),
(2, '出库', '1', 'wms_change_type', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(3, '移库入', '2', 'wms_change_type', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(4, '移库出', '3', 'wms_change_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(5, '盘盈', '4', 'wms_change_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(6, '盘亏', '5', 'wms_change_type', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(7, '锁定', '6', 'wms_change_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(8, '解锁', '7', 'wms_change_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '全盘', '0', 'wms_take_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '抽盘', '1', 'wms_take_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '循环盘', '2', 'wms_take_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '待盘点', '0', 'wms_take_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '盘点中', '1', 'wms_take_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '待审批', '2', 'wms_take_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '已完成', '3', 'wms_take_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(5, '已作废', '4', 'wms_take_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '待审批', '0', 'wms_move_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '已批准', '1', 'wms_move_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '已完成', '2', 'wms_move_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '已作废', '3', 'wms_move_status', '', 'danger', 'N', '0', 'admin', sysdate(), '');


-- =============================================
-- 三、菜单权限（WMS菜单树）
-- =============================================

-- 顶级目录：WMS仓储管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2000, 'WMS仓储管理', 0, 5, 'wms', NULL, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), 'WMS仓储管理目录');

-- 二级目录：基础数据
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2001, '基础数据', 2000, 1, 'base', NULL, '', '', 1, 0, 'M', '0', '0', '', 'dict', 'admin', sysdate(), '');

-- 基础数据-菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2010, '物料管理', 2001, 1, 'material', 'wms/material/index', '', 'WmsMaterial', 1, 0, 'C', '0', '0', 'wms:material:list', 'component', 'admin', sysdate(), ''),
(2020, '供应商管理', 2001, 2, 'supplier', 'wms/supplier/index', '', 'WmsSupplier', 1, 0, 'C', '0', '0', 'wms:supplier:list', 'people', 'admin', sysdate(), ''),
(2030, '仓库管理', 2001, 3, 'warehouse', 'wms/warehouse/index', '', 'WmsWarehouse', 1, 0, 'C', '0', '0', 'wms:warehouse:list', 'build', 'admin', sysdate(), '');

-- 物料管理按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2011, '物料查询', 2010, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:material:query', '#', 'admin', sysdate(), ''),
(2012, '物料新增', 2010, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:material:add', '#', 'admin', sysdate(), ''),
(2013, '物料修改', 2010, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:material:edit', '#', 'admin', sysdate(), ''),
(2014, '物料删除', 2010, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:material:remove', '#', 'admin', sysdate(), ''),
(2015, '物料导出', 2010, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:material:export', '#', 'admin', sysdate(), '');

-- 供应商按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2021, '供应商查询', 2020, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:supplier:query', '#', 'admin', sysdate(), ''),
(2022, '供应商新增', 2020, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:supplier:add', '#', 'admin', sysdate(), ''),
(2023, '供应商修改', 2020, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:supplier:edit', '#', 'admin', sysdate(), ''),
(2024, '供应商删除', 2020, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:supplier:remove', '#', 'admin', sysdate(), ''),
(2025, '供应商导出', 2020, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:supplier:export', '#', 'admin', sysdate(), '');

-- 仓库按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2031, '仓库查询', 2030, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:warehouse:query', '#', 'admin', sysdate(), ''),
(2032, '仓库新增', 2030, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:warehouse:add', '#', 'admin', sysdate(), ''),
(2033, '仓库修改', 2030, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:warehouse:edit', '#', 'admin', sysdate(), ''),
(2034, '仓库删除', 2030, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:warehouse:remove', '#', 'admin', sysdate(), ''),
(2035, '仓库导出', 2030, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:warehouse:export', '#', 'admin', sysdate(), ''),
(2036, '仓区查询', 2030, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:area:list', '#', 'admin', sysdate(), ''),
(2037, '库位查询', 2030, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:location:list', '#', 'admin', sysdate(), '');

-- 二级目录：入库管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2002, '入库管理', 2000, 2, 'inbound', NULL, '', '', 1, 0, 'M', '0', '0', '', 'log-in', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2060, '入库单管理', 2002, 1, 'order', 'wms/inbound/index', '', 'WmsInboundOrder', 1, 0, 'C', '0', '0', 'wms:inbound:list', 'list', 'admin', sysdate(), ''),
(2061, '入库作业', 2002, 2, 'detail', 'wms/inbound/detail', '', 'WmsInboundDetail', 1, 0, 'C', '0', '0', 'wms:inbound:detail', 'edit', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2062, '入库单查询', 2060, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inbound:query', '#', 'admin', sysdate(), ''),
(2063, '入库单新增', 2060, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inbound:add', '#', 'admin', sysdate(), ''),
(2064, '入库单修改', 2060, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inbound:edit', '#', 'admin', sysdate(), ''),
(2065, '入库单删除', 2060, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inbound:remove', '#', 'admin', sysdate(), ''),
(2066, '入库单导出', 2060, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inbound:export', '#', 'admin', sysdate(), '');

-- 二级目录：出库管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2003, '出库管理', 2000, 3, 'outbound', NULL, '', '', 1, 0, 'M', '0', '0', '', 'log-out', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2070, '出库单管理', 2003, 1, 'order', 'wms/outbound/index', '', 'WmsOutboundOrder', 1, 0, 'C', '0', '0', 'wms:outbound:list', 'list', 'admin', sysdate(), ''),
(2071, '出库作业', 2003, 2, 'detail', 'wms/outbound/detail', '', 'WmsOutboundDetail', 1, 0, 'C', '0', '0', 'wms:outbound:detail', 'edit', 'admin', sysdate(), ''),
(2072, '扫码复核', 2003, 3, 'check', 'wms/outbound/check', '', 'WmsOutboundCheck', 1, 0, 'C', '1', '0', 'wms:outbound:check', 'scan', 'admin', sysdate(), ''),
(2073, '波次管理', 2003, 4, 'wave', 'wms/outbound/wave', '', 'WmsOutboundWave', 1, 0, 'C', '1', '0', 'wms:wave:list', 'group', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2074, '出库单查询', 2070, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:outbound:query', '#', 'admin', sysdate(), ''),
(2075, '出库单新增', 2070, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:outbound:add', '#', 'admin', sysdate(), ''),
(2076, '出库单修改', 2070, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:outbound:edit', '#', 'admin', sysdate(), ''),
(2077, '出库单删除', 2070, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:outbound:remove', '#', 'admin', sysdate(), ''),
(2078, '出库单导出', 2070, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:outbound:export', '#', 'admin', sysdate(), '');

-- 二级目录：库存管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2004, '库存管理', 2000, 4, 'inv', NULL, '', '', 1, 0, 'M', '0', '0', '', 'storage', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2080, '库存查询', 2004, 1, 'list', 'wms/inventory/index', '', 'WmsInventoryList', 1, 0, 'C', '0', '0', 'wms:inventory:list', 'list', 'admin', sysdate(), ''),
(2081, '库存流水', 2004, 2, 'log', 'wms/log/index', '', 'WmsInventoryLog', 1, 0, 'C', '0', '0', 'wms:log:list', 'time', 'admin', sysdate(), ''),
(2120, '库存预警', 2004, 3, 'alert', 'wms/stockAlert/index', '', 'WmsStockAlert', 1, 0, 'C', '0', '0', 'wms:stockAlert:list', 'bug', 'admin', sysdate(), '库存预警管理（库存不足/积压/临期/过期）');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2082, '库存查询', 2080, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inventory:query', '#', 'admin', sysdate(), ''),
(2083, '库存导出', 2080, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inventory:export', '#', 'admin', sysdate(), ''),
(2084, '流水查询', 2081, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:log:query', '#', 'admin', sysdate(), ''),
(2085, '流水导出', 2081, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:log:export', '#', 'admin', sysdate(), ''),
(2121, '预警查询', 2120, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stockAlert:query', '#', 'admin', sysdate(), ''),
(2122, '预警导出', 2120, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stockAlert:export', '#', 'admin', sysdate(), ''),
(2123, '库存删除', 2080, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inventory:remove', '#', 'admin', sysdate(), ''),
(2124, '流水删除', 2081, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:log:remove', '#', 'admin', sysdate(), ''),
(2125, '预警删除', 2120, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stockAlert:remove', '#', 'admin', sysdate(), '');

-- 二级目录：盘点管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2005, '盘点管理', 2000, 5, 'stocktake', NULL, '', '', 1, 0, 'M', '0', '0', '', 'eye-open', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2090, '盘点任务', 2005, 1, 'task', 'wms/stocktake/index', '', 'WmsStocktakeTask', 1, 0, 'C', '0', '0', 'wms:stocktake:list', 'list', 'admin', sysdate(), ''),
(2091, '盘点作业', 2005, 2, 'detail', 'wms/stocktake/detail', '', 'WmsStocktakeDetail', 1, 0, 'C', '0', '0', 'wms:stocktake:detail', 'edit', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2092, '盘点查询', 2090, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:query', '#', 'admin', sysdate(), ''),
(2093, '盘点新增', 2090, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:add', '#', 'admin', sysdate(), ''),
(2094, '盘点修改', 2090, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:edit', '#', 'admin', sysdate(), ''),
(2095, '盘点删除', 2090, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:remove', '#', 'admin', sysdate(), '');

-- 移库管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2100, '移库管理', 2000, 6, 'move', 'wms/move/index', '', 'WmsMove', 1, 0, 'C', '0', '0', 'wms:move:list', 'swap', 'admin', sysdate(), '');

-- 统计报表
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2110, '统计报表', 2000, 7, 'statistics', 'wms/statistics/index', '', 'WmsStatistics', 1, 0, 'C', '0', '0', 'wms:statistics:list', 'chart', 'admin', sysdate(), '仓库管理统计报表');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2111, '统计查询', 2110, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:statistics:query', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2101, '移库查询', 2100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:move:query', '#', 'admin', sysdate(), ''),
(2102, '移库新增', 2100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:move:add', '#', 'admin', sysdate(), ''),
(2103, '移库修改', 2100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:move:edit', '#', 'admin', sysdate(), ''),
(2104, '移库删除', 2100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:move:remove', '#', 'admin', sysdate(), ''),
(2105, '移库导出', 2100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:move:export', '#', 'admin', sysdate(), '');

-- 给admin角色（role_id=1）分配所有WMS菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 2000), (1, 2001), (1, 2002), (1, 2003), (1, 2004), (1, 2005),
(1, 2010), (1, 2011), (1, 2012), (1, 2013), (1, 2014), (1, 2015),
(1, 2020), (1, 2021), (1, 2022), (1, 2023), (1, 2024), (1, 2025),
(1, 2030), (1, 2031), (1, 2032), (1, 2033), (1, 2034), (1, 2035), (1, 2036), (1, 2037),
(1, 2060), (1, 2061), (1, 2062), (1, 2063), (1, 2064), (1, 2065), (1, 2066),
(1, 2070), (1, 2071), (1, 2072), (1, 2073), (1, 2074), (1, 2075), (1, 2076), (1, 2077), (1, 2078),
(1, 2080), (1, 2081), (1, 2082), (1, 2083), (1, 2084), (1, 2085), (1, 2123), (1, 2124),
(1, 2090), (1, 2091), (1, 2092), (1, 2093), (1, 2094), (1, 2095),
(1, 2100), (1, 2101), (1, 2102), (1, 2103), (1, 2104), (1, 2105),
(1, 2110), (1, 2111),
(1, 2120), (1, 2121), (1, 2122), (1, 2125);

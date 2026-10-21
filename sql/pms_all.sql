-- =============================================
-- PMS 采购管理子系统 - 全量SQL脚本
-- 包含：9张业务表 + 7个字典类型 + 菜单权限 + 编号规则
-- 依赖：wms_supplier（供应商）、wms_material（物料）共用表
-- =============================================

-- =============================================
-- 一、业务表 DDL
-- =============================================

-- 1. 采购申请表
DROP TABLE IF EXISTS pms_purchase_request;
CREATE TABLE pms_purchase_request (
    request_id       BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '申请ID',
    request_no       VARCHAR(64)   NOT NULL                 COMMENT '申请单号',
    title            VARCHAR(255)                           COMMENT '申请标题',
    status           VARCHAR(10)   DEFAULT '0'              COMMENT '状态（字典 pms_request_status）',
    priority         VARCHAR(10)   DEFAULT '0'              COMMENT '优先级（字典 pms_priority）',
    expect_date      DATE                                    COMMENT '期望交货日期',
    purpose          VARCHAR(500)                           COMMENT '采购用途',
    total_amount     DECIMAL(14,2) DEFAULT 0                COMMENT '预估总金额',
    requester_id     BIGINT                                  COMMENT '申请人ID',
    requester_name   VARCHAR(64)                             COMMENT '申请人姓名',
    dept_id          BIGINT                                  COMMENT '申请部门ID',
    dept_name        VARCHAR(128)                            COMMENT '申请部门名称',
    audit_by         VARCHAR(64)                             COMMENT '审批人',
    audit_time       DATETIME                                COMMENT '审批时间',
    audit_opinion    VARCHAR(500)                            COMMENT '审批意见',
    plan_id           BIGINT                                  COMMENT '关联采购计划ID',
    plan_no           VARCHAR(64)                             COMMENT '关联采购计划单号',
    del_flag         CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                                COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                                COMMENT '更新时间',
    remark           VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (request_id),
    UNIQUE KEY uk_request_no (request_no),
    KEY idx_request_status (status),
    KEY idx_request_requester (requester_id),
    KEY idx_request_dept (dept_id),
    KEY idx_request_plan (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购申请表';

-- 2. 采购申请明细表
DROP TABLE IF EXISTS pms_purchase_request_detail;
CREATE TABLE pms_purchase_request_detail (
    detail_id        BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    request_id       BIGINT        NOT NULL                 COMMENT '采购申请ID',
    material_id      BIGINT                                 COMMENT '物料ID',
    material_code    VARCHAR(64)                            COMMENT '物料编码（冗余）',
    material_name    VARCHAR(255)                           COMMENT '物料名称（冗余）',
    spec_model       VARCHAR(255)                           COMMENT '规格型号（冗余）',
    unit             VARCHAR(20)                            COMMENT '单位（冗余）',
    qty              DECIMAL(14,2)                          COMMENT '申请数量',
    estimated_price  DECIMAL(14,2)                          COMMENT '预估单价',
    estimated_amount DECIMAL(14,2)                          COMMENT '预估金额',
    del_flag         CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                                COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                                COMMENT '更新时间',
    remark           VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (detail_id),
    KEY idx_req_detail_request (request_id),
    KEY idx_req_detail_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购申请明细表';

-- 3. 采购订单表
DROP TABLE IF EXISTS pms_purchase_order;
CREATE TABLE pms_purchase_order (
    order_id            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '订单ID',
    order_no            VARCHAR(64)   NOT NULL                 COMMENT '采购单号',
    request_id          BIGINT                                 COMMENT '关联申请ID',
    request_no          VARCHAR(64)                            COMMENT '关联申请单号（冗余）',
    supplier_id         BIGINT                                 COMMENT '供应商ID',
    supplier_name       VARCHAR(255)                           COMMENT '供应商名称（冗余）',
    warehouse_id        BIGINT                                 COMMENT '目标仓库ID',
    status              VARCHAR(10)   DEFAULT '0'              COMMENT '状态（字典 pms_order_status）',
    order_date          DATE                                   COMMENT '采购日期',
    expect_delivery_date DATE                                   COMMENT '预计交货日期',
    actual_delivery_date DATE                                   COMMENT '实际交货日期',
    total_qty           DECIMAL(14,2) DEFAULT 0                COMMENT '总数量',
    total_amount        DECIMAL(14,2) DEFAULT 0                COMMENT '总金额',
    received_amount     DECIMAL(14,2) DEFAULT 0                COMMENT '已收金额',
    buyer_id            BIGINT                                 COMMENT '采购员ID',
    buyer_name          VARCHAR(64)                            COMMENT '采购员姓名（冗余）',
    dept_id             BIGINT                                 COMMENT '部门ID',
    dept_name           VARCHAR(128)                           COMMENT '部门名称（冗余）',
    payment_method      VARCHAR(20)   DEFAULT '0'              COMMENT '付款方式（字典 wms_payment_method）',
    payment_days        INT          DEFAULT 0                COMMENT '账期天数',
    settlement_type     VARCHAR(20)   DEFAULT '0'              COMMENT '结算方式（字典 wms_settlement_type）',
    currency            VARCHAR(10)   DEFAULT 'CNY'            COMMENT '结算币种（字典 wms_currency）',
    delivery_address    VARCHAR(500)                           COMMENT '交货地址',
    audit_by            VARCHAR(64)                            COMMENT '审批人',
    audit_time          DATETIME                               COMMENT '审批时间',
    audit_opinion       VARCHAR(500)                           COMMENT '审批意见',
    del_flag            CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by           VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time         DATETIME                               COMMENT '创建时间',
    update_by           VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time         DATETIME                               COMMENT '更新时间',
    remark              VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_status (status),
    KEY idx_order_supplier (supplier_id),
    KEY idx_order_buyer (buyer_id),
    KEY idx_order_request (request_id),
    KEY idx_order_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

-- 4. 采购订单明细表
DROP TABLE IF EXISTS pms_purchase_order_detail;
CREATE TABLE pms_purchase_order_detail (
    detail_id        BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    order_id         BIGINT        NOT NULL                 COMMENT '采购订单ID',
    request_detail_id BIGINT                                COMMENT '关联申请明细ID',
    material_id      BIGINT                                 COMMENT '物料ID',
    material_code    VARCHAR(64)                            COMMENT '物料编码（冗余）',
    material_name    VARCHAR(255)                           COMMENT '物料名称（冗余）',
    spec_model       VARCHAR(255)                           COMMENT '规格型号（冗余）',
    unit             VARCHAR(20)                            COMMENT '单位（冗余）',
    qty              DECIMAL(14,2)                          COMMENT '采购数量',
    unit_price       DECIMAL(14,2)                          COMMENT '单价',
    amount           DECIMAL(14,2)                          COMMENT '金额',
    received_qty     DECIMAL(14,2) DEFAULT 0                COMMENT '已收数量',
    del_flag         CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                                COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                                COMMENT '更新时间',
    remark           VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (detail_id),
    KEY idx_order_detail_order (order_id),
    KEY idx_order_detail_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表';

-- 5. 收货验收表
DROP TABLE IF EXISTS pms_receive;
CREATE TABLE pms_receive (
    receive_id       BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '收货ID',
    receive_no       VARCHAR(64)   NOT NULL                 COMMENT '收货单号',
    order_id         BIGINT                                 COMMENT '采购订单ID',
    order_no         VARCHAR(64)                            COMMENT '采购单号（冗余）',
    supplier_id      BIGINT                                 COMMENT '供应商ID',
    supplier_name    VARCHAR(255)                           COMMENT '供应商名称（冗余）',
    warehouse_id     BIGINT                                 COMMENT '入库仓库ID',
    status           VARCHAR(10)   DEFAULT '0'              COMMENT '状态（字典 pms_receive_status）',
    receive_date     DATE                                   COMMENT '收货日期',
    total_qty        DECIMAL(14,2) DEFAULT 0                COMMENT '应收总数量',
    qualified_qty    DECIMAL(14,2) DEFAULT 0                COMMENT '合格数量',
    unqualified_qty  DECIMAL(14,2) DEFAULT 0                COMMENT '不合格数量',
    inspector_id     BIGINT                                 COMMENT '验收人ID',
    inspector_name   VARCHAR(64)                            COMMENT '验收人姓名（冗余）',
    inbound_order_id BIGINT                                 COMMENT '关联入库单ID',
    audit_by         VARCHAR(64)                            COMMENT '审核人',
    audit_time       DATETIME                               COMMENT '审核时间',
    audit_opinion    VARCHAR(500)                           COMMENT '审核意见',
    del_flag         CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                                COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                                COMMENT '更新时间',
    remark           VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (receive_id),
    UNIQUE KEY uk_receive_no (receive_no),
    KEY idx_receive_status (status),
    KEY idx_receive_order (order_id),
    KEY idx_receive_supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货验收表';

-- 6.1 收货验收审核日志表
DROP TABLE IF EXISTS pms_receive_audit_log;
CREATE TABLE pms_receive_audit_log (
    log_id           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    receive_id       BIGINT        NOT NULL                 COMMENT '收货ID',
    audit_action     VARCHAR(1)    DEFAULT NULL             COMMENT '审核动作（1通过 2驳回）',
    audit_by         VARCHAR(64)   DEFAULT NULL             COMMENT '审核人',
    audit_time       DATETIME      DEFAULT NULL             COMMENT '审核时间',
    audit_remark     VARCHAR(500)  DEFAULT NULL             COMMENT '审核意见',
    PRIMARY KEY (log_id),
    KEY idx_audit_log_receive (receive_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货验收审核日志';

-- 6. 收货验收明细表
DROP TABLE IF EXISTS pms_receive_detail;
CREATE TABLE pms_receive_detail (
    detail_id        BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    receive_id       BIGINT        NOT NULL                 COMMENT '收货ID',
    order_detail_id  BIGINT                                 COMMENT '订单明细ID',
    material_id      BIGINT                                 COMMENT '物料ID',
    material_code    VARCHAR(64)                            COMMENT '物料编码（冗余）',
    material_name    VARCHAR(255)                           COMMENT '物料名称（冗余）',
    spec_model       VARCHAR(255)                           COMMENT '规格型号（冗余）',
    unit             VARCHAR(20)                            COMMENT '单位（冗余）',
    receive_qty      DECIMAL(14,2)                          COMMENT '应收数量',
    qualified_qty    DECIMAL(14,2)                          COMMENT '合格数量',
    unqualified_qty  DECIMAL(14,2)                          COMMENT '不合格数量',
    unit_price       DECIMAL(14,2)                          COMMENT '单价',
    amount           DECIMAL(14,2)                          COMMENT '金额',
    batch_no         VARCHAR(64)                            COMMENT '批次号',
    production_date  DATE                                   COMMENT '生产日期',
    expiry_date      DATE                                   COMMENT '保质期至',
    del_flag         CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                                COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                                COMMENT '更新时间',
    remark           VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (detail_id),
    KEY idx_recv_detail_receive (receive_id),
    KEY idx_recv_detail_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货验收明细表';

-- 7. 退货表
DROP TABLE IF EXISTS pms_return;
CREATE TABLE pms_return (
    return_id        BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '退货ID',
    return_no        VARCHAR(64)   NOT NULL                 COMMENT '退货单号',
    order_id         BIGINT                                 COMMENT '采购订单ID',
    order_no         VARCHAR(64)                            COMMENT '采购单号（冗余）',
    receive_id       BIGINT                                 COMMENT '关联收货ID',
    receive_no       VARCHAR(64)                            COMMENT '收货单号（冗余）',
    supplier_id      BIGINT                                 COMMENT '供应商ID',
    supplier_name    VARCHAR(255)                           COMMENT '供应商名称（冗余）',
    status           VARCHAR(10)   DEFAULT '0'              COMMENT '状态（字典 pms_return_status）',
    return_type      VARCHAR(10)   DEFAULT '0'              COMMENT '退货类型（字典 pms_return_type）',
    return_date      DATE                                   COMMENT '退货日期',
    total_qty        DECIMAL(14,2) DEFAULT 0                COMMENT '退货总数量',
    total_amount     DECIMAL(14,2) DEFAULT 0                COMMENT '退货总金额',
    reason           VARCHAR(500)                           COMMENT '退货原因',
    handler_id       BIGINT                                 COMMENT '经办人ID',
    handler_name     VARCHAR(64)                            COMMENT '经办人姓名（冗余）',
    audit_by         VARCHAR(64)                            COMMENT '审核人',
    audit_time       DATETIME                                COMMENT '审核时间',
    audit_opinion    VARCHAR(500)                           COMMENT '审核意见',
    outbound_order_id BIGINT                                 COMMENT '出库单ID',
    del_flag         CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                                COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                                COMMENT '更新时间',
    remark           VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (return_id),
    UNIQUE KEY uk_return_no (return_no),
    KEY idx_return_status (status),
    KEY idx_return_order (order_id),
    KEY idx_return_supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货表';

-- 8. 退货明细表
DROP TABLE IF EXISTS pms_return_detail;
CREATE TABLE pms_return_detail (
    detail_id        BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    return_id        BIGINT        NOT NULL                 COMMENT '退货ID',
    material_id      BIGINT                                 COMMENT '物料ID',
    material_code    VARCHAR(64)                            COMMENT '物料编码（冗余）',
    material_name    VARCHAR(255)                           COMMENT '物料名称（冗余）',
    spec_model       VARCHAR(255)                           COMMENT '规格型号（冗余）',
    unit             VARCHAR(20)                            COMMENT '单位（冗余）',
    qty              DECIMAL(14,2)                          COMMENT '退货数量',
    unit_price       DECIMAL(14,2)                          COMMENT '单价',
    amount           DECIMAL(14,2)                          COMMENT '退货金额',
    return_reason    VARCHAR(500)                           COMMENT '退货原因',
    del_flag         CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                                COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                                COMMENT '更新时间',
    remark           VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (detail_id),
    KEY idx_return_detail_return (return_id),
    KEY idx_return_detail_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货明细表';

-- 9. 发票结算表
DROP TABLE IF EXISTS pms_invoice;
CREATE TABLE pms_invoice (
    invoice_id       BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '发票ID',
    invoice_no       VARCHAR(64)   NOT NULL                 COMMENT '结算单号',
    order_id         BIGINT                                 COMMENT '采购订单ID',
    order_no         VARCHAR(64)                            COMMENT '采购单号（冗余）',
    supplier_id      BIGINT                                 COMMENT '供应商ID',
    supplier_name    VARCHAR(255)                           COMMENT '供应商名称（冗余）',
    status           VARCHAR(10)   DEFAULT '0'              COMMENT '状态（字典 pms_invoice_status）',
    invoice_type     VARCHAR(10)   DEFAULT '0'              COMMENT '发票类型（字典 pms_invoice_type）',
    invoice_number   VARCHAR(64)                            COMMENT '发票号码',
    invoice_date     DATE                                   COMMENT '开票日期',
    invoice_title    VARCHAR(255)                           COMMENT '发票抬头',
    tax_number       VARCHAR(64)                            COMMENT '税号',
    invoice_amount   DECIMAL(14,2) DEFAULT 0                COMMENT '不含税金额',
    tax_amount       DECIMAL(14,2) DEFAULT 0                COMMENT '税额',
    total_amount     DECIMAL(14,2) DEFAULT 0                COMMENT '价税合计',
    payment_amount   DECIMAL(14,2) DEFAULT 0                COMMENT '已付金额',
    payment_date     DATE                                   COMMENT '付款日期',
    payment_method   VARCHAR(20)   DEFAULT '0'              COMMENT '付款方式（字典 wms_payment_method）',
    bank_name        VARCHAR(128)                           COMMENT '开户银行',
    bank_account     VARCHAR(64)                            COMMENT '银行账号',
    del_flag         CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                                COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                                COMMENT '更新时间',
    remark           VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (invoice_id),
    UNIQUE KEY uk_invoice_no (invoice_no),
    KEY idx_invoice_status (status),
    KEY idx_invoice_order (order_id),
    KEY idx_invoice_supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票结算表';


-- =============================================
-- 二、字典数据（7个字典类型）
-- =============================================

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT * FROM (
    SELECT '采购申请状态' AS dict_name, 'pms_request_status' AS dict_type, '0' AS status, 'admin' AS create_by, sysdate() AS create_time, '采购申请状态字典' AS remark
    UNION ALL SELECT '采购订单状态', 'pms_order_status', '0', 'admin', sysdate(), '采购订单状态字典'
    UNION ALL SELECT '收货状态', 'pms_receive_status', '0', 'admin', sysdate(), '收货验收状态字典'
    UNION ALL SELECT '退货状态', 'pms_return_status', '0', 'admin', sysdate(), '退货状态字典'
    UNION ALL SELECT '退货类型', 'pms_return_type', '0', 'admin', sysdate(), '退货类型字典'
    UNION ALL SELECT '发票状态', 'pms_invoice_status', '0', 'admin', sysdate(), '发票结算状态字典'
    UNION ALL SELECT '发票类型', 'pms_invoice_type', '0', 'admin', sysdate(), '发票类型字典'
    UNION ALL SELECT '优先级', 'pms_priority', '0', 'admin', sysdate(), '采购优先级字典'
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type dt WHERE dt.dict_type = t.dict_type);

-- 字典数据
-- 采购申请状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '草稿', '0', 'pms_request_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_request_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '待审批', '1', 'pms_request_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_request_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已审批', '2', 'pms_request_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_request_status' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已驳回', '3', 'pms_request_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_request_status' AND dict_value='3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '已转采购', '4', 'pms_request_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_request_status' AND dict_value='4');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '已作废', '5', 'pms_request_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_request_status' AND dict_value='5');

-- 采购订单状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '草稿', '0', 'pms_order_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_order_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '待审批', '1', 'pms_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_order_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已审批', '2', 'pms_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_order_status' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已下单', '3', 'pms_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_order_status' AND dict_value='3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '部分到货', '4', 'pms_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_order_status' AND dict_value='4');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '已完成', '5', 'pms_order_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_order_status' AND dict_value='5');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 7, '已作废', '6', 'pms_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_order_status' AND dict_value='6');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 8, '已驳回', '7', 'pms_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_order_status' AND dict_value='7');

-- 收货状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待验收', '0', 'pms_receive_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_receive_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '部分验收', '1', 'pms_receive_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_receive_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已验收', '2', 'pms_receive_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_receive_status' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已入库', '3', 'pms_receive_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_receive_status' AND dict_value='3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '已作废', '4', 'pms_receive_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_receive_status' AND dict_value='4');

-- 退货状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '草稿', '0', 'pms_return_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_return_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '待审批', '1', 'pms_return_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_return_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已审批', '2', 'pms_return_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_return_status' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已退货', '3', 'pms_return_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_return_status' AND dict_value='3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '已作废', '4', 'pms_return_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_return_status' AND dict_value='4');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '已驳回', '5', 'pms_return_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_return_status' AND dict_value='5');

-- 退货类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '质量问题', '0', 'pms_return_type', '', 'danger', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_return_type' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '规格不符', '1', 'pms_return_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_return_type' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '多收', '2', 'pms_return_type', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_return_type' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '其他', '3', 'pms_return_type', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_return_type' AND dict_value='3');

-- 发票状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '草稿', '0', 'pms_invoice_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_invoice_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '待审核', '1', 'pms_invoice_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_invoice_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已审核', '2', 'pms_invoice_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_invoice_status' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已付款', '3', 'pms_invoice_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_invoice_status' AND dict_value='3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '已作废', '4', 'pms_invoice_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_invoice_status' AND dict_value='4');

-- 发票类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '增值税专用发票', '0', 'pms_invoice_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_invoice_type' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '增值税普通发票', '1', 'pms_invoice_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_invoice_type' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '收据', '2', 'pms_invoice_type', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_invoice_type' AND dict_value='2');

-- 优先级
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '普通', '0', 'pms_priority', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_priority' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '紧急', '1', 'pms_priority', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_priority' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '特急', '2', 'pms_priority', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_priority' AND dict_value='2');


-- =============================================
-- 三、菜单权限（参考客户管理结构，使用二级目录）
-- =============================================

-- 顶级目录：采购管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '采购管理', 0, 50, 'pms', NULL, '', 'Pms', 1, 0, 'M', '0', '0', '', 'shopping-cart', 'admin', sysdate(), '采购管理目录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0);

-- 获取采购管理目录ID
SET @pmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);

-- =============================================
-- 二级目录：采购业务（核心业务流程）
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '采购业务', @pmsParentId, 1, 'business', NULL, '', '', 1, 0, 'M', '0', '0', '', 'document', 'admin', sysdate(), '采购业务目录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='采购业务' AND parent_id=@pmsParentId);
SET @pmsBusinessId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购业务' AND parent_id=@pmsParentId LIMIT 1);

-- 采购申请（在采购业务下）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '采购申请', @pmsBusinessId, 1, 'request', 'pms/request/index', '', 'PmsRequest', 1, 0, 'C', '0', '0', 'pms:request:list', 'edit-pen', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='采购申请' AND parent_id=@pmsBusinessId);
SET @reqMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购申请' AND parent_id=@pmsBusinessId LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '申请查询', @reqMenuId, 1, '', 'F', '0', '0', 'pms:request:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:request:query' AND parent_id=@reqMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '申请新增', @reqMenuId, 2, '', 'F', '0', '0', 'pms:request:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:request:add' AND parent_id=@reqMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '申请修改', @reqMenuId, 3, '', 'F', '0', '0', 'pms:request:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:request:edit' AND parent_id=@reqMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '申请删除', @reqMenuId, 4, '', 'F', '0', '0', 'pms:request:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:request:remove' AND parent_id=@reqMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '申请导出', @reqMenuId, 5, '', 'F', '0', '0', 'pms:request:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:request:export' AND parent_id=@reqMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '申请审批', @reqMenuId, 6, '', 'F', '0', '0', 'pms:request:approve', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:request:approve' AND parent_id=@reqMenuId);

-- 采购订单（在采购业务下）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '采购订单', @pmsBusinessId, 2, 'order', 'pms/order/index', '', 'PmsOrder', 1, 0, 'C', '0', '0', 'pms:order:list', 'document-checked', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='采购订单' AND parent_id=@pmsBusinessId);
SET @ordMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购订单' AND parent_id=@pmsBusinessId LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '订单查询', @ordMenuId, 1, '', 'F', '0', '0', 'pms:order:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:order:query' AND parent_id=@ordMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '订单新增', @ordMenuId, 2, '', 'F', '0', '0', 'pms:order:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:order:add' AND parent_id=@ordMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '订单修改', @ordMenuId, 3, '', 'F', '0', '0', 'pms:order:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:order:edit' AND parent_id=@ordMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '订单删除', @ordMenuId, 4, '', 'F', '0', '0', 'pms:order:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:order:remove' AND parent_id=@ordMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '订单导出', @ordMenuId, 5, '', 'F', '0', '0', 'pms:order:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:order:export' AND parent_id=@ordMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '订单审批', @ordMenuId, 6, '', 'F', '0', '0', 'pms:order:approve', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:order:approve' AND parent_id=@ordMenuId);

-- 供应商管理（在采购业务下，复用WMS供应商表）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '供应商管理', @pmsBusinessId, 3, 'supplier', 'wms/supplier/index', '', 'PmsSupplier', 1, 0, 'C', '0', '0', 'wms:supplier:list', 'office-building', 'admin', sysdate(), '采购供应商管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='供应商管理' AND parent_id=@pmsBusinessId);
SET @pmsSupplierId = (SELECT menu_id FROM sys_menu WHERE menu_name='供应商管理' AND parent_id=@pmsBusinessId LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '供应商查询', @pmsSupplierId, 1, '', 'F', '0', '0', 'wms:supplier:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='wms:supplier:query' AND parent_id=@pmsSupplierId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '供应商新增', @pmsSupplierId, 2, '', 'F', '0', '0', 'wms:supplier:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='wms:supplier:add' AND parent_id=@pmsSupplierId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '供应商修改', @pmsSupplierId, 3, '', 'F', '0', '0', 'wms:supplier:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='wms:supplier:edit' AND parent_id=@pmsSupplierId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '供应商删除', @pmsSupplierId, 4, '', 'F', '0', '0', 'wms:supplier:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='wms:supplier:remove' AND parent_id=@pmsSupplierId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '供应商导出', @pmsSupplierId, 5, '', 'F', '0', '0', 'wms:supplier:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='wms:supplier:export' AND parent_id=@pmsSupplierId);

-- =============================================
-- 二级目录：仓储作业（收货退货）
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '仓储作业', @pmsParentId, 2, 'warehouse', NULL, '', '', 1, 0, 'M', '0', '0', '', 'box', 'admin', sysdate(), '仓储作业目录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='仓储作业' AND parent_id=@pmsParentId);
SET @pmsWarehouseId = (SELECT menu_id FROM sys_menu WHERE menu_name='仓储作业' AND parent_id=@pmsParentId LIMIT 1);

-- 收货验收（在仓储作业下）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '收货验收', @pmsWarehouseId, 1, 'receive', 'pms/receive/index', '', 'PmsReceive', 1, 0, 'C', '0', '0', 'pms:receive:list', 'circle-check', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='收货验收' AND parent_id=@pmsWarehouseId);
SET @recvMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name='收货验收' AND parent_id=@pmsWarehouseId LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '收货查询', @recvMenuId, 1, '', 'F', '0', '0', 'pms:receive:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:receive:query' AND parent_id=@recvMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '收货新增', @recvMenuId, 2, '', 'F', '0', '0', 'pms:receive:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:receive:add' AND parent_id=@recvMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '收货修改', @recvMenuId, 3, '', 'F', '0', '0', 'pms:receive:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:receive:edit' AND parent_id=@recvMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '收货删除', @recvMenuId, 4, '', 'F', '0', '0', 'pms:receive:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:receive:remove' AND parent_id=@recvMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '收货导出', @recvMenuId, 5, '', 'F', '0', '0', 'pms:receive:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:receive:export' AND parent_id=@recvMenuId);

-- 退货管理（在仓储作业下）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '退货管理', @pmsWarehouseId, 2, 'return', 'pms/return/index', '', 'PmsReturn', 1, 0, 'C', '0', '0', 'pms:return:list', 'refresh-left', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='退货管理' AND parent_id=@pmsWarehouseId);
SET @retMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name='退货管理' AND parent_id=@pmsWarehouseId LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '退货查询', @retMenuId, 1, '', 'F', '0', '0', 'pms:return:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:return:query' AND parent_id=@retMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '退货新增', @retMenuId, 2, '', 'F', '0', '0', 'pms:return:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:return:add' AND parent_id=@retMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '退货修改', @retMenuId, 3, '', 'F', '0', '0', 'pms:return:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:return:edit' AND parent_id=@retMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '退货删除', @retMenuId, 4, '', 'F', '0', '0', 'pms:return:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:return:remove' AND parent_id=@retMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '退货导出', @retMenuId, 5, '', 'F', '0', '0', 'pms:return:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:return:export' AND parent_id=@retMenuId);

-- =============================================
-- 二级目录：财务管理（发票结算）
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '财务管理', @pmsParentId, 3, 'finance', NULL, '', '', 1, 0, 'M', '0', '0', '', 'coin', 'admin', sysdate(), '财务管理目录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='财务管理' AND parent_id=@pmsParentId);
SET @pmsFinanceId = (SELECT menu_id FROM sys_menu WHERE menu_name='财务管理' AND parent_id=@pmsParentId LIMIT 1);

-- 发票结算（在财务管理下）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '发票结算', @pmsFinanceId, 1, 'invoice', 'pms/invoice/index', '', 'PmsInvoice', 1, 0, 'C', '0', '0', 'pms:invoice:list', 'money', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='发票结算' AND parent_id=@pmsFinanceId);
SET @invMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name='发票结算' AND parent_id=@pmsFinanceId LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '发票查询', @invMenuId, 1, '', 'F', '0', '0', 'pms:invoice:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:invoice:query' AND parent_id=@invMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '发票新增', @invMenuId, 2, '', 'F', '0', '0', 'pms:invoice:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:invoice:add' AND parent_id=@invMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '发票修改', @invMenuId, 3, '', 'F', '0', '0', 'pms:invoice:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:invoice:edit' AND parent_id=@invMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '发票删除', @invMenuId, 4, '', 'F', '0', '0', 'pms:invoice:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:invoice:remove' AND parent_id=@invMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '发票导出', @invMenuId, 5, '', 'F', '0', '0', 'pms:invoice:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:invoice:export' AND parent_id=@invMenuId);

-- =============================================
-- 二级目录：数据分析（采购看板）
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '数据分析', @pmsParentId, 4, 'analysis', NULL, '', '', 1, 0, 'M', '0', '0', '', 'data-line', 'admin', sysdate(), '数据分析目录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='数据分析' AND parent_id=@pmsParentId);
SET @pmsAnalysisId = (SELECT menu_id FROM sys_menu WHERE menu_name='数据分析' AND parent_id=@pmsParentId LIMIT 1);

-- 采购看板（在数据分析下）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '采购看板', @pmsAnalysisId, 1, 'dashboard', 'pms/dashboard/index', '', 'PmsDashboard', 1, 0, 'C', '0', '0', 'pms:dashboard:view', 'trend-charts', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='采购看板' AND parent_id=@pmsAnalysisId);

-- =============================================
-- 二级目录：系统配置（编号规则）
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '系统配置', @pmsParentId, 5, 'config', NULL, '', '', 1, 0, 'M', '0', '0', '', 'setting', 'admin', sysdate(), '系统配置目录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='系统配置' AND parent_id=@pmsParentId);
SET @pmsConfigId = (SELECT menu_id FROM sys_menu WHERE menu_name='系统配置' AND parent_id=@pmsParentId LIMIT 1);

-- 编号规则（在系统配置下）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '编号规则', @pmsConfigId, 1, 'numberRule', 'pms/numberRule/index', '', 'PmsNumberRule', 1, 0, 'C', '0', '0', 'pms:numberRule:list', 'tickets', 'admin', sysdate(), '采购管理编号规则'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='编号规则' AND parent_id=@pmsConfigId);
SET @pmsRuleMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id=@pmsConfigId LIMIT 1);

-- 编号规则按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '规则查询', @pmsRuleMenuId, 1, '', 'F', '0', '0', 'pms:numberRule:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:numberRule:query' AND parent_id=@pmsRuleMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '规则新增', @pmsRuleMenuId, 2, '', 'F', '0', '0', 'pms:numberRule:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:numberRule:add' AND parent_id=@pmsRuleMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '规则修改', @pmsRuleMenuId, 3, '', 'F', '0', '0', 'pms:numberRule:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:numberRule:edit' AND parent_id=@pmsRuleMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '规则导出', @pmsRuleMenuId, 4, '', 'F', '0', '0', 'pms:numberRule:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:numberRule:export' AND parent_id=@pmsRuleMenuId);

-- 给admin角色分配采购管理权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'pms:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);


-- =============================================
-- 四、编号规则
-- =============================================

INSERT INTO mk_number_rule (rule_code, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, status, create_by, create_time, remark)
SELECT 'pms_request', '采购申请单号', 'SQ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '采购申请单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pms_request');

INSERT INTO mk_number_rule (rule_code, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, status, create_by, create_time, remark)
SELECT 'pms_purchase', '采购订单号', 'CG', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '采购订单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pms_purchase');

INSERT INTO mk_number_rule (rule_code, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, status, create_by, create_time, remark)
SELECT 'pms_receive', '收货单号', 'SH', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '收货单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pms_receive');

INSERT INTO mk_number_rule (rule_code, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, status, create_by, create_time, remark)
SELECT 'pms_return', '退货单号', 'TH', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '退货单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pms_return');

INSERT INTO mk_number_rule (rule_code, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, status, create_by, create_time, remark)
SELECT 'pms_invoice', '结算单号', 'JS', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '发票结算单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pms_invoice');

-- 标记PMS模块编号规则
UPDATE mk_number_rule SET module='pms' WHERE rule_code LIKE 'pms_%' AND (module IS NULL OR module='');

-- =============================================
-- 执行完毕
-- =============================================

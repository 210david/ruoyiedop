-- =============================================
-- 询比价管理模块数据库表
-- =============================================

-- 询价单主表
CREATE TABLE IF NOT EXISTS pms_inquiry (
    inquiry_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '询价ID',
    inquiry_no VARCHAR(64) NOT NULL COMMENT '询价单号',
    title VARCHAR(200) NOT NULL COMMENT '询价标题',
    status CHAR(1) DEFAULT '0' COMMENT '状态（0-草稿 1-询价中 2-已截止 3-已比价 4-已定标）',
    inquiry_type CHAR(1) DEFAULT '0' COMMENT '询价方式（0-公开询价 1-邀请询价）',
    purchase_type CHAR(1) DEFAULT '0' COMMENT '采购类型（0-物资 1-服务 2-工程）',
    deadline DATETIME COMMENT '报价截止日期',
    estimate_amount DECIMAL(18,2) DEFAULT 0 COMMENT '预计采购金额',
    award_amount DECIMAL(18,2) DEFAULT 0 COMMENT '定标金额',
    award_supplier_id BIGINT COMMENT '定标供应商ID',
    award_supplier_name VARCHAR(200) COMMENT '定标供应商名称',
    description TEXT COMMENT '询价说明',
    inquirer_id BIGINT COMMENT '询价人ID',
    inquirer_name VARCHAR(64) COMMENT '询价人姓名',
    dept_id BIGINT COMMENT '询价部门ID',
    dept_name VARCHAR(64) COMMENT '询价部门名称',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0-正常 2-删除）',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (inquiry_id),
    UNIQUE KEY uk_inquiry_no (inquiry_no),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='询价单表';

-- 询价明细表
CREATE TABLE IF NOT EXISTS pms_inquiry_detail (
    detail_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    inquiry_id BIGINT NOT NULL COMMENT '询价ID',
    material_id BIGINT COMMENT '物料ID',
    material_code VARCHAR(64) COMMENT '物料编码',
    material_name VARCHAR(200) COMMENT '物料名称',
    specification VARCHAR(500) COMMENT '规格型号',
    unit VARCHAR(32) COMMENT '单位',
    quantity DECIMAL(18,2) DEFAULT 0 COMMENT '采购数量',
    max_price DECIMAL(18,2) COMMENT '最高限价',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0-正常 2-删除）',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (detail_id),
    KEY idx_inquiry_id (inquiry_id),
    KEY idx_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='询价明细表';

-- 供应商报价表
CREATE TABLE IF NOT EXISTS pms_quotation (
    quotation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '报价ID',
    inquiry_id BIGINT NOT NULL COMMENT '询价ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    supplier_name VARCHAR(200) COMMENT '供应商名称',
    contact_name VARCHAR(64) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    total_amount DECIMAL(18,2) DEFAULT 0 COMMENT '报价总金额',
    tax_rate DECIMAL(5,2) DEFAULT 0 COMMENT '税率（%）',
    delivery_days INT COMMENT '交货期（天）',
    payment_terms VARCHAR(200) COMMENT '付款方式',
    valid_date DATE COMMENT '报价有效期',
    status CHAR(1) DEFAULT '0' COMMENT '状态（0-待报价 1-已报价 2-已采纳 3-已拒绝）',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0-正常 2-删除）',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (quotation_id),
    KEY idx_inquiry_id (inquiry_id),
    KEY idx_supplier_id (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商报价表';

-- 报价明细表
CREATE TABLE IF NOT EXISTS pms_quotation_detail (
    detail_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    quotation_id BIGINT NOT NULL COMMENT '报价ID',
    inquiry_detail_id BIGINT COMMENT '询价明细ID',
    material_id BIGINT COMMENT '物料ID',
    material_code VARCHAR(64) COMMENT '物料编码',
    material_name VARCHAR(200) COMMENT '物料名称',
    specification VARCHAR(500) COMMENT '规格型号',
    unit VARCHAR(32) COMMENT '单位',
    quantity DECIMAL(18,2) DEFAULT 0 COMMENT '采购数量',
    price DECIMAL(18,2) DEFAULT 0 COMMENT '报价单价',
    amount DECIMAL(18,2) DEFAULT 0 COMMENT '报价金额',
    tax_rate DECIMAL(5,2) DEFAULT 0 COMMENT '税率（%）',
    tax_price DECIMAL(18,2) DEFAULT 0 COMMENT '含税单价',
    tax_amount DECIMAL(18,2) DEFAULT 0 COMMENT '含税金额',
    delivery_date VARCHAR(20) COMMENT '交货日期',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0-正常 2-删除）',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (detail_id),
    KEY idx_quotation_id (quotation_id),
    KEY idx_inquiry_detail_id (inquiry_detail_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报价明细表';

-- 添加字典数据
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '询价单状态', 'pms_inquiry_status', '0', 'admin', NOW(), '询价单状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pms_inquiry_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
VALUES
(1, '草稿', '0', 'pms_inquiry_status', '', 'info', 'N', '0', 'admin', NOW()),
(2, '询价中', '1', 'pms_inquiry_status', '', 'primary', 'N', '0', 'admin', NOW()),
(3, '已截止', '2', 'pms_inquiry_status', '', 'warning', 'N', '0', 'admin', NOW()),
(4, '比价中', '3', 'pms_inquiry_status', '', 'success', 'N', '0', 'admin', NOW()),
(5, '已定标', '4', 'pms_inquiry_status', '', 'success', 'N', '0', 'admin', NOW())
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '询价方式', 'pms_inquiry_type', '0', 'admin', NOW(), '询价方式'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pms_inquiry_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
VALUES
(1, '公开询价', '0', 'pms_inquiry_type', '', 'primary', 'Y', '0', 'admin', NOW()),
(2, '邀请询价', '1', 'pms_inquiry_type', '', 'primary', 'N', '0', 'admin', NOW())
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '采购类型', 'pms_purchase_type', '0', 'admin', NOW(), '采购类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pms_purchase_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
VALUES
(1, '物资', '0', 'pms_purchase_type', '', 'primary', 'Y', '0', 'admin', NOW()),
(2, '服务', '1', 'pms_purchase_type', '', 'primary', 'N', '0', 'admin', NOW()),
(3, '工程', '2', 'pms_purchase_type', '', 'primary', 'N', '0', 'admin', NOW())
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 添加编号规则
INSERT INTO mk_number_rule (rule_code, rule_name, module, prefix, date_format, seq_length, current_seq, status, create_by, create_time, remark)
SELECT 'PMS_INQUIRY', '询价单编号', 'pms', 'XJ', 'yyyyMMdd', 4, 0, '0', 'admin', NOW(), '询价单编号规则'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'PMS_INQUIRY');

SELECT '询比价管理模块表创建完成' AS result;

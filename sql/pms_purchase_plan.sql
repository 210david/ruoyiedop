-- =============================================
-- 采购计划管理模块数据库表
-- =============================================

-- 采购计划主表
CREATE TABLE IF NOT EXISTS pms_purchase_plan (
    plan_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '计划ID',
    plan_no VARCHAR(64) NOT NULL COMMENT '计划单号',
    title VARCHAR(200) NOT NULL COMMENT '计划标题',
    status CHAR(1) DEFAULT '0' COMMENT '状态（0-草稿 1-待审批 2-审批通过 3-执行中 4-已完成 5-已关闭 6-已驳回）',
    plan_type CHAR(1) DEFAULT '2' COMMENT '计划类型（0-年度 1-季度 2-月度 3-临时）',
    start_date DATE COMMENT '计划开始日期',
    end_date DATE COMMENT '计划结束日期',
    budget_amount DECIMAL(18,2) DEFAULT 0 COMMENT '预算总金额',
    actual_amount DECIMAL(18,2) DEFAULT 0 COMMENT '实际采购金额',
    description TEXT COMMENT '计划说明',
    planner_id BIGINT COMMENT '编制人ID',
    planner_name VARCHAR(64) COMMENT '编制人姓名',
    dept_id BIGINT COMMENT '编制部门ID',
    dept_name VARCHAR(64) COMMENT '编制部门名称',
    audit_by VARCHAR(64) COMMENT '审批人',
    audit_time DATETIME COMMENT '审批时间',
    audit_opinion VARCHAR(500) COMMENT '审批意见',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0-正常 2-删除）',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (plan_id),
    UNIQUE KEY uk_plan_no (plan_no),
    KEY idx_status (status),
    KEY idx_plan_type (plan_type),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购计划表';

-- 采购计划明细表
CREATE TABLE IF NOT EXISTS pms_purchase_plan_detail (
    detail_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    plan_id BIGINT NOT NULL COMMENT '计划ID',
    material_id BIGINT COMMENT '物料ID',
    material_code VARCHAR(64) COMMENT '物料编码',
    material_name VARCHAR(200) COMMENT '物料名称',
    specification VARCHAR(500) COMMENT '规格型号',
    unit VARCHAR(32) COMMENT '单位',
    plan_quantity DECIMAL(18,2) DEFAULT 0 COMMENT '计划数量',
    budget_price DECIMAL(18,2) DEFAULT 0 COMMENT '预算单价',
    budget_amount DECIMAL(18,2) DEFAULT 0 COMMENT '预算金额',
    actual_quantity DECIMAL(18,2) DEFAULT 0 COMMENT '实际采购数量',
    actual_amount DECIMAL(18,2) DEFAULT 0 COMMENT '实际采购金额',
    require_date VARCHAR(20) COMMENT '需求日期',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0-正常 2-删除）',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (detail_id),
    KEY idx_plan_id (plan_id),
    KEY idx_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购计划明细表';

-- 添加字典数据
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '采购计划状态', 'pms_plan_status', '0', 'admin', NOW(), '采购计划状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pms_plan_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
VALUES
(1, '草稿', '0', 'pms_plan_status', '', 'info', 'N', '0', 'admin', NOW()),
(2, '待审批', '1', 'pms_plan_status', '', 'warning', 'N', '0', 'admin', NOW()),
(3, '审批通过', '2', 'pms_plan_status', '', 'success', 'N', '0', 'admin', NOW()),
(4, '执行中', '3', 'pms_plan_status', '', 'primary', 'N', '0', 'admin', NOW()),
(5, '已完成', '4', 'pms_plan_status', '', 'success', 'N', '0', 'admin', NOW()),
(6, '已关闭', '5', 'pms_plan_status', '', 'info', 'N', '0', 'admin', NOW()),
(7, '已驳回', '6', 'pms_plan_status', '', 'danger', 'N', '0', 'admin', NOW())
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '采购计划类型', 'pms_plan_type', '0', 'admin', NOW(), '采购计划类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pms_plan_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
VALUES
(1, '年度', '0', 'pms_plan_type', '', 'primary', 'N', '0', 'admin', NOW()),
(2, '季度', '1', 'pms_plan_type', '', 'primary', 'N', '0', 'admin', NOW()),
(3, '月度', '2', 'pms_plan_type', '', 'primary', 'Y', '0', 'admin', NOW()),
(4, '临时', '3', 'pms_plan_type', '', 'warning', 'N', '0', 'admin', NOW())
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 添加编号规则
INSERT INTO mk_number_rule (rule_code, rule_name, module, prefix, date_format, seq_length, current_seq, status, create_by, create_time, remark)
SELECT 'PMS_PLAN', '采购计划编号', 'pms', 'PLAN', 'yyyyMM', 4, 0, '0', 'admin', NOW(), '采购计划编号规则'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'PMS_PLAN');

SELECT '采购计划管理模块表创建完成' AS result;

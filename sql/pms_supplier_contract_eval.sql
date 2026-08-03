-- =============================================
-- PMS 采购管理子系统 - 供应商管理增强SQL脚本
-- 包含：5张业务表 + 字典类型 + 菜单权限 + 编号规则
-- 功能：供应商管理(PMS入口)、供应商资质、供应商供货清单、采购合同、供应商评估
-- =============================================

-- =============================================
-- 一、业务表 DDL
-- =============================================

-- 1. 供应商资质表
DROP TABLE IF EXISTS pms_supplier_qualification;
CREATE TABLE pms_supplier_qualification (
    qual_id          BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '资质ID',
    supplier_id      BIGINT        NOT NULL                 COMMENT '供应商ID',
    supplier_name    VARCHAR(255)                           COMMENT '供应商名称（冗余）',
    qual_name        VARCHAR(255)  NOT NULL                 COMMENT '资质名称',
    qual_type        VARCHAR(50)   NOT NULL                 COMMENT '资质类型（字典 pms_qual_type）',
    qual_code        VARCHAR(100)                           COMMENT '资质编号',
    issue_date       DATE                                   COMMENT '颁发日期',
    expire_date      DATE                                   COMMENT '有效期至',
    status           VARCHAR(10)   DEFAULT '0'              COMMENT '状态（字典 pms_qual_status）',
    file_url         VARCHAR(500)                           COMMENT '资质文件路径',
    file_name        VARCHAR(200)                           COMMENT '资质文件名称',
    audit_by         VARCHAR(64)                            COMMENT '审核人',
    audit_time       DATETIME                               COMMENT '审核时间',
    audit_remark     VARCHAR(500)                           COMMENT '审核意见',
    del_flag         CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                               COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                               COMMENT '更新时间',
    remark           VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (qual_id),
    KEY idx_qual_supplier (supplier_id),
    KEY idx_qual_status (status),
    KEY idx_qual_expire (expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商资质表';

-- 2. 供应商供货清单表
DROP TABLE IF EXISTS pms_supplier_catalog;
CREATE TABLE pms_supplier_catalog (
    catalog_id            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '清单ID',
    supplier_id           BIGINT        NOT NULL                 COMMENT '供应商ID',
    supplier_name         VARCHAR(255)                           COMMENT '供应商名称（冗余）',
    material_id           BIGINT                                 COMMENT '物料ID',
    material_code          VARCHAR(64)                            COMMENT '物料编码',
    material_name         VARCHAR(255)  NOT NULL                 COMMENT '物料名称',
    spec_model            VARCHAR(255)                           COMMENT '规格型号',
    unit                  VARCHAR(20)                            COMMENT '计量单位',
    unit_price            DECIMAL(15,2) NOT NULL                 COMMENT '供货单价（不含税）',
    tax_rate              DECIMAL(5,2)  DEFAULT 13.00            COMMENT '税率',
    min_order_qty         DECIMAL(15,2) DEFAULT 1.00             COMMENT '最小起订量',
    delivery_cycle        INT                                    COMMENT '交货周期（天）',
    currency              VARCHAR(10)   DEFAULT 'CNY'            COMMENT '货币类型',
    price_effective_date  DATE                                   COMMENT '价格生效日期',
    price_expire_date     DATE                                   COMMENT '价格失效日期',
    status                VARCHAR(10)   DEFAULT '1'              COMMENT '状态（字典 pms_catalog_status）',
    del_flag              CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by             VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time           DATETIME                               COMMENT '创建时间',
    update_by             VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time           DATETIME                               COMMENT '更新时间',
    remark                VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (catalog_id),
    KEY idx_catalog_supplier (supplier_id),
    KEY idx_catalog_material (material_id),
    KEY idx_catalog_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商供货清单表';

-- 3. 采购合同表
DROP TABLE IF EXISTS pms_contract;
CREATE TABLE pms_contract (
    contract_id        BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '合同ID',
    contract_no        VARCHAR(64)   NOT NULL                 COMMENT '合同编号',
    contract_name      VARCHAR(255)                           COMMENT '合同名称',
    order_id           BIGINT                                 COMMENT '关联采购订单ID',
    order_no           VARCHAR(64)                            COMMENT '关联采购单号（冗余）',
    supplier_id        BIGINT                                 COMMENT '供应商ID',
    supplier_name      VARCHAR(255)                           COMMENT '供应商名称（冗余）',
    status             VARCHAR(10)   DEFAULT '0'              COMMENT '状态（字典 pms_contract_status）',
    contract_type      VARCHAR(10)   DEFAULT '0'              COMMENT '合同类型（字典 pms_contract_type）',
    sign_date          DATE                                   COMMENT '签订日期',
    effective_date     DATE                                   COMMENT '生效日期',
    expire_date        DATE                                   COMMENT '到期日期',
    contract_amount    DECIMAL(14,2) DEFAULT 0                COMMENT '合同金额',
    sign_by            VARCHAR(64)                            COMMENT '签订人',
    sign_department    VARCHAR(128)                           COMMENT '签订部门',
    party_a            VARCHAR(255)                           COMMENT '甲方',
    party_b            VARCHAR(255)                           COMMENT '乙方',
    file_url           VARCHAR(500)                           COMMENT '合同附件路径',
    file_name          VARCHAR(200)                           COMMENT '合同附件名称',
    change_count       INT          DEFAULT 0                COMMENT '变更次数',
    terminate_date     DATE                                   COMMENT '终止日期',
    terminate_reason   VARCHAR(500)                           COMMENT '终止原因',
    del_flag           CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by          VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                               COMMENT '创建时间',
    update_by          VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                               COMMENT '更新时间',
    remark             VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (contract_id),
    UNIQUE KEY uk_contract_no (contract_no),
    KEY idx_contract_status (status),
    KEY idx_contract_supplier (supplier_id),
    KEY idx_contract_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购合同表';

-- 4. 采购合同变更记录表
DROP TABLE IF EXISTS pms_contract_change;
CREATE TABLE pms_contract_change (
    change_id          BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '变更ID',
    contract_id        BIGINT        NOT NULL                 COMMENT '合同ID',
    change_no          VARCHAR(64)   NOT NULL                 COMMENT '变更编号',
    change_type        VARCHAR(10)   DEFAULT '0'              COMMENT '变更类型（字典 pms_contract_change_type）',
    change_content     TEXT                                   COMMENT '变更内容',
    original_content   TEXT                                   COMMENT '原内容',
    change_reason      VARCHAR(500)                           COMMENT '变更原因',
    change_date        DATE                                   COMMENT '变更日期',
    change_by          VARCHAR(64)                            COMMENT '变更人',
    audit_by           VARCHAR(64)                            COMMENT '审核人',
    audit_time         DATETIME                               COMMENT '审核时间',
    audit_status       VARCHAR(10)   DEFAULT '0'              COMMENT '审核状态（字典 pms_contract_change_status）',
    audit_remark       VARCHAR(500)                           COMMENT '审核意见',
    del_flag           CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                               COMMENT '创建时间',
    update_by          VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                               COMMENT '更新时间',
    remark             VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (change_id),
    KEY idx_change_contract (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购合同变更记录表';

-- 5. 供应商评估表
DROP TABLE IF EXISTS pms_supplier_eval;
CREATE TABLE pms_supplier_eval (
    eval_id            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '评估ID',
    supplier_id        BIGINT        NOT NULL                 COMMENT '供应商ID',
    supplier_name      VARCHAR(255)                           COMMENT '供应商名称（冗余）',
    eval_period        VARCHAR(20)                            COMMENT '评估周期（如2026-Q3）',
    eval_date          DATE                                   COMMENT '评估日期',
    delivery_score     DECIMAL(5,2)  DEFAULT 0                COMMENT '交货准时率评分',
    quality_score      DECIMAL(5,2)  DEFAULT 0                COMMENT '质量合格率评分',
    price_score        DECIMAL(5,2)  DEFAULT 0                COMMENT '价格竞争力评分',
    service_score      DECIMAL(5,2)  DEFAULT 0                COMMENT '服务评分',
    total_score        DECIMAL(5,2)  DEFAULT 0                COMMENT '综合评分',
    eval_level         VARCHAR(10)                            COMMENT '评估等级（字典 pms_eval_level）',
    evaluator_id       BIGINT                                 COMMENT '评估人ID',
    evaluator_name     VARCHAR(64)                            COMMENT '评估人姓名',
    del_flag           CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by          VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                               COMMENT '创建时间',
    update_by          VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                               COMMENT '更新时间',
    remark             VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (eval_id),
    KEY idx_eval_supplier (supplier_id),
    KEY idx_eval_period (eval_period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商评估表';


-- =============================================
-- 二、字典数据
-- =============================================

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT * FROM (
    SELECT '资质类型' AS dict_name, 'pms_qual_type' AS dict_type, '0' AS status, 'admin' AS create_by, sysdate() AS create_time, '供应商资质类型字典' AS remark
    UNION ALL SELECT '资质状态', 'pms_qual_status', '0', 'admin', sysdate(), '供应商资质状态字典'
    UNION ALL SELECT '供货清单状态', 'pms_catalog_status', '0', 'admin', sysdate(), '供货清单状态字典'
    UNION ALL SELECT '合同状态', 'pms_contract_status', '0', 'admin', sysdate(), '采购合同状态字典'
    UNION ALL SELECT '合同类型', 'pms_contract_type', '0', 'admin', sysdate(), '采购合同类型字典'
    UNION ALL SELECT '合同变更类型', 'pms_contract_change_type', '0', 'admin', sysdate(), '合同变更类型字典'
    UNION ALL SELECT '合同变更状态', 'pms_contract_change_status', '0', 'admin', sysdate(), '合同变更审核状态字典'
    UNION ALL SELECT '评估等级', 'pms_eval_level', '0', 'admin', sysdate(), '供应商评估等级字典'
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type dt WHERE dt.dict_type = t.dict_type);

-- 资质类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '营业执照', '0', 'pms_qual_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_qual_type' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '生产许可证', '1', 'pms_qual_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_qual_type' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '经营许可证', '2', 'pms_qual_type', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_qual_type' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, 'ISO认证', '3', 'pms_qual_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_qual_type' AND dict_value='3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '其他资质', '9', 'pms_qual_type', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_qual_type' AND dict_value='9');

-- 资质状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待审核', '0', 'pms_qual_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_qual_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '有效', '1', 'pms_qual_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_qual_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '审核不通过', '2', 'pms_qual_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_qual_status' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已过期', '3', 'pms_qual_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_qual_status' AND dict_value='3');

-- 供货清单状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '正常供货', '1', 'pms_catalog_status', '', 'success', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_catalog_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '暂停供货', '2', 'pms_catalog_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_catalog_status' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '停止供货', '3', 'pms_catalog_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_catalog_status' AND dict_value='3');

-- 合同状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '草稿', '0', 'pms_contract_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已签订', '1', 'pms_contract_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '执行中', '2', 'pms_contract_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_status' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已到期', '3', 'pms_contract_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_status' AND dict_value='3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '已终止', '4', 'pms_contract_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_status' AND dict_value='4');

-- 合同类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '框架协议', '0', 'pms_contract_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_type' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '采购合同', '1', 'pms_contract_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_type' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '补充协议', '2', 'pms_contract_type', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_type' AND dict_value='2');

-- 合同变更类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '金额变更', '0', 'pms_contract_change_type', '', 'warning', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_change_type' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '交期变更', '1', 'pms_contract_change_type', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_change_type' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '条款变更', '2', 'pms_contract_change_type', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_change_type' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '其他变更', '9', 'pms_contract_change_type', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_change_type' AND dict_value='9');

-- 合同变更审核状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待审核', '0', 'pms_contract_change_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_change_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已审核', '1', 'pms_contract_change_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_change_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已驳回', '2', 'pms_contract_change_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_contract_change_status' AND dict_value='2');

-- 评估等级
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '优秀', 'A', 'pms_eval_level', '', 'success', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_eval_level' AND dict_value='A');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '良好', 'B', 'pms_eval_level', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_eval_level' AND dict_value='B');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '合格', 'C', 'pms_eval_level', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_eval_level' AND dict_value='C');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '不合格', 'D', 'pms_eval_level', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_eval_level' AND dict_value='D');


-- =============================================
-- 三、菜单权限
-- =============================================

-- 获取采购管理目录ID
SET @pmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);

-- 供应商管理（PMS入口，复用WMS供应商API）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '供应商管理', @pmsParentId, 0, 'supplier', 'pms/supplier/index', '', 'PmsSupplier', 1, 0, 'C', '0', '0', 'wms:supplier:list', 'peoples', 'admin', sysdate(), '供应商管理（采购入口）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='供应商管理' AND parent_id=@pmsParentId);
SET @supMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name='供应商管理' AND parent_id=@pmsParentId LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '供应商查询', @supMenuId, 1, '', 'F', '0', '0', 'wms:supplier:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='wms:supplier:query' AND parent_id=@supMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '供应商新增', @supMenuId, 2, '', 'F', '0', '0', 'wms:supplier:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='wms:supplier:add' AND parent_id=@supMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '供应商修改', @supMenuId, 3, '', 'F', '0', '0', 'wms:supplier:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='wms:supplier:edit' AND parent_id=@supMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '供应商删除', @supMenuId, 4, '', 'F', '0', '0', 'wms:supplier:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='wms:supplier:remove' AND parent_id=@supMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '供应商导出', @supMenuId, 5, '', 'F', '0', '0', 'wms:supplier:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='wms:supplier:export' AND parent_id=@supMenuId);

-- 供应商资质管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '供应商资质', @pmsParentId, 7, 'qualification', 'pms/qualification/index', '', 'PmsQualification', 1, 0, 'C', '0', '0', 'pms:qualification:list', 'clipboard', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='供应商资质' AND parent_id=@pmsParentId);
SET @qualMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name='供应商资质' AND parent_id=@pmsParentId LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '资质查询', @qualMenuId, 1, '', 'F', '0', '0', 'pms:qualification:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:qualification:query' AND parent_id=@qualMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '资质新增', @qualMenuId, 2, '', 'F', '0', '0', 'pms:qualification:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:qualification:add' AND parent_id=@qualMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '资质修改', @qualMenuId, 3, '', 'F', '0', '0', 'pms:qualification:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:qualification:edit' AND parent_id=@qualMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '资质删除', @qualMenuId, 4, '', 'F', '0', '0', 'pms:qualification:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:qualification:remove' AND parent_id=@qualMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '资质导出', @qualMenuId, 5, '', 'F', '0', '0', 'pms:qualification:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:qualification:export' AND parent_id=@qualMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '资质审核', @qualMenuId, 6, '', 'F', '0', '0', 'pms:qualification:audit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:qualification:audit' AND parent_id=@qualMenuId);

-- 供应商供货清单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '供货清单', @pmsParentId, 8, 'catalog', 'pms/catalog/index', '', 'PmsCatalog', 1, 0, 'C', '0', '0', 'pms:catalog:list', 'list', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='供货清单' AND parent_id=@pmsParentId);
SET @catMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name='供货清单' AND parent_id=@pmsParentId LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '清单查询', @catMenuId, 1, '', 'F', '0', '0', 'pms:catalog:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:catalog:query' AND parent_id=@catMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '清单新增', @catMenuId, 2, '', 'F', '0', '0', 'pms:catalog:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:catalog:add' AND parent_id=@catMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '清单修改', @catMenuId, 3, '', 'F', '0', '0', 'pms:catalog:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:catalog:edit' AND parent_id=@catMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '清单删除', @catMenuId, 4, '', 'F', '0', '0', 'pms:catalog:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:catalog:remove' AND parent_id=@catMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '清单导出', @catMenuId, 5, '', 'F', '0', '0', 'pms:catalog:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:catalog:export' AND parent_id=@catMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '清单导入', @catMenuId, 6, '', 'F', '0', '0', 'pms:catalog:import', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:catalog:import' AND parent_id=@catMenuId);

-- 采购合同管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '采购合同', @pmsParentId, 9, 'contract', 'pms/contract/index', '', 'PmsContract', 1, 0, 'C', '0', '0', 'pms:contract:list', 'documentation', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='采购合同' AND parent_id=@pmsParentId);
SET @conMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购合同' AND parent_id=@pmsParentId LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '合同查询', @conMenuId, 1, '', 'F', '0', '0', 'pms:contract:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:contract:query' AND parent_id=@conMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '合同新增', @conMenuId, 2, '', 'F', '0', '0', 'pms:contract:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:contract:add' AND parent_id=@conMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '合同修改', @conMenuId, 3, '', 'F', '0', '0', 'pms:contract:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:contract:edit' AND parent_id=@conMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '合同删除', @conMenuId, 4, '', 'F', '0', '0', 'pms:contract:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:contract:remove' AND parent_id=@conMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '合同导出', @conMenuId, 5, '', 'F', '0', '0', 'pms:contract:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:contract:export' AND parent_id=@conMenuId);

-- 供应商评估
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '供应商评估', @pmsParentId, 10, 'eval', 'pms/eval/index', '', 'PmsEval', 1, 0, 'C', '0', '0', 'pms:eval:list', 'rate', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='供应商评估' AND parent_id=@pmsParentId);
SET @evalMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name='供应商评估' AND parent_id=@pmsParentId LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '评估查询', @evalMenuId, 1, '', 'F', '0', '0', 'pms:eval:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:eval:query' AND parent_id=@evalMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '评估新增', @evalMenuId, 2, '', 'F', '0', '0', 'pms:eval:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:eval:add' AND parent_id=@evalMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '评估修改', @evalMenuId, 3, '', 'F', '0', '0', 'pms:eval:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:eval:edit' AND parent_id=@evalMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '评估删除', @evalMenuId, 4, '', 'F', '0', '0', 'pms:eval:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:eval:remove' AND parent_id=@evalMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '评估导出', @evalMenuId, 5, '', 'F', '0', '0', 'pms:eval:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:eval:export' AND parent_id=@evalMenuId);

-- 给admin角色分配新增权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'pms:qualification%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'pms:catalog%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'pms:contract%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'pms:eval%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);
-- 供应商管理（PMS入口）的权限已在WMS模块中分配


-- =============================================
-- 四、编号规则
-- =============================================

INSERT INTO mk_number_rule (rule_code, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, status, create_by, create_time, remark)
SELECT 'pms_contract', '采购合同编号', 'HT', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '采购合同编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pms_contract');

INSERT INTO mk_number_rule (rule_code, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, status, create_by, create_time, remark)
SELECT 'pms_contract_change', '合同变更编号', 'BG', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '合同变更编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pms_contract_change');

-- 标记PMS模块编号规则
UPDATE mk_number_rule SET module='pms' WHERE rule_code LIKE 'pms_contract%' AND (module IS NULL OR module='');


-- =============================================
-- 执行完毕
-- =============================================
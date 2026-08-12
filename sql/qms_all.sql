-- =============================================
-- QMS 质量管理系统 - 全量SQL脚本
-- 包含：业务表DDL + 字典类型 + 菜单权限
-- 适用：P0 范围（质量主数据 + 质量检验 + NCR）
-- =============================================

-- =============================================
-- 一、业务表 DDL
-- =============================================

-- 1. 行业模板配置表
DROP TABLE IF EXISTS qms_industry_tpl;
CREATE TABLE qms_industry_tpl (
    tpl_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '模板ID',
    tpl_code       VARCHAR(64)  NOT NULL                 COMMENT '模板编码',
    tpl_name       VARCHAR(128) NOT NULL                 COMMENT '模板名称',
    industry_type  VARCHAR(32)                           COMMENT '行业类型（新能源材料/绿色铝加工/精细化工/先进装备/高原食品）',
    audit_force    CHAR(1)      DEFAULT '0'              COMMENT '是否强制审计追踪（0否 1是）',
    status         CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag       CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by      VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time    DATETIME                              COMMENT '创建时间',
    update_by      VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time    DATETIME                              COMMENT '更新时间',
    remark         VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (tpl_id),
    UNIQUE KEY uk_tpl_code (tpl_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行业模板配置表';

-- 2. 检验标准库表
DROP TABLE IF EXISTS qms_inspect_std;
CREATE TABLE qms_inspect_std (
    std_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '标准ID',
    std_code        VARCHAR(64)  NOT NULL                 COMMENT '标准编码',
    std_name        VARCHAR(128) NOT NULL                 COMMENT '检验项目名称',
    inspect_method  VARCHAR(255)                          COMMENT '检验方法',
    unit            VARCHAR(32)                           COMMENT '计量单位',
    val_min         DECIMAL(18,4)                         COMMENT '标准下限',
    val_max         DECIMAL(18,4)                         COMMENT '标准上限',
    target_val      DECIMAL(18,4)                         COMMENT '目标值',
    judge_rule      VARCHAR(10)  DEFAULT '1'              COMMENT '判定规则（1区间 2单值 3定性）',
    tpl_id          BIGINT                                COMMENT '行业模板ID',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (std_id),
    UNIQUE KEY uk_std_code (std_code),
    KEY idx_std_tpl (tpl_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验标准库表';

-- 3. 缺陷代码表
DROP TABLE IF EXISTS qms_defect_code;
CREATE TABLE qms_defect_code (
    defect_id       BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '缺陷ID',
    defect_code     VARCHAR(64)  NOT NULL                 COMMENT '缺陷代码',
    defect_name     VARCHAR(128) NOT NULL                 COMMENT '缺陷名称',
    defect_level    VARCHAR(10)  DEFAULT '3'              COMMENT '缺陷等级（1致命 2严重 3一般 4轻微）',
    defect_category VARCHAR(64)                           COMMENT '缺陷分类',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (defect_id),
    UNIQUE KEY uk_defect_code (defect_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缺陷代码表';

-- 4. 量检具台账表
DROP TABLE IF EXISTS qms_gauge;
CREATE TABLE qms_gauge (
    gauge_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '量检具ID',
    gauge_no        VARCHAR(64)  NOT NULL                 COMMENT '量检具编号',
    gauge_name      VARCHAR(128) NOT NULL                 COMMENT '量检具名称',
    gauge_type      VARCHAR(64)                           COMMENT '量检具类型',
    precision_val   VARCHAR(64)                           COMMENT '精度',
    measure_range   VARCHAR(128)                          COMMENT '测量范围',
    calib_cycle     INT                                   COMMENT '校准周期（月）',
    last_calib_date DATE                                  COMMENT '上次校准日期',
    next_calib_date DATE                                  COMMENT '下次校准日期',
    gauge_status    VARCHAR(10)  DEFAULT '0'              COMMENT '量检具状态（0在用 1停用 2待校准）',
    dept_id         BIGINT                                COMMENT '使用部门ID',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (gauge_id),
    UNIQUE KEY uk_gauge_no (gauge_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='量检具台账表';

-- 5. 物料质量属性表
DROP TABLE IF EXISTS qms_material_attr;
CREATE TABLE qms_material_attr (
    attr_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '属性ID',
    material_id     BIGINT       NOT NULL                 COMMENT '物料ID（关联WMS物料主数据）',
    material_code   VARCHAR(64)                           COMMENT '物料编码（冗余）',
    material_name   VARCHAR(255)                          COMMENT '物料名称（冗余）',
    inspect_types   VARCHAR(64)                           COMMENT '检验类型（多选逗号分隔：IQC,IPQC,FQC,OQC）',
    tpl_id          BIGINT                                COMMENT '行业模板ID',
    is_exempt       CHAR(1)      DEFAULT '0'              COMMENT '是否免检（0否 1是）',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (attr_id),
    UNIQUE KEY uk_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料质量属性表';

-- 6. 检验任务表
DROP TABLE IF EXISTS qms_insp_task;
CREATE TABLE qms_insp_task (
    task_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '任务ID',
    task_no         VARCHAR(64)  NOT NULL                 COMMENT '任务编号',
    task_type       VARCHAR(10)  NOT NULL                 COMMENT '检验类型（IQC/IPQC/FQC/OQC）',
    source_type     VARCHAR(32)                           COMMENT '来源类型（arrival/production/delivery/manual）',
    source_no       VARCHAR(64)                           COMMENT '来源单号',
    material_id     BIGINt                                COMMENT '物料ID',
    material_code   VARCHAR(64)                           COMMENT '物料编码（冗余）',
    material_name   VARCHAR(255)                          COMMENT '物料名称（冗余）',
    supplier_id     BIGINT                                COMMENT '供应商ID',
    supplier_name   VARCHAR(255)                          COMMENT '供应商名称（冗余）',
    batch_no        VARCHAR(64)                           COMMENT '批次号',
    inspect_qty     DECIMAL(18,4)                         COMMENT '送检数量',
    aql_level       VARCHAR(10)                           COMMENT 'AQL等级',
    inspect_level   VARCHAR(10)  DEFAULT '1'              COMMENT '检验严格度（1正常 2加严 3放宽）',
    sample_size     INT                                   COMMENT '样本量(n)',
    ac_val          INT                                   COMMENT '接收数(Ac)',
    re_val          INT                                   COMMENT '拒收数(Re)',
    task_status     VARCHAR(10)  DEFAULT '0'              COMMENT '任务状态（0待检 1检验中 2已判定 3已作废）',
    inspect_result  VARCHAR(10)                           COMMENT '检验结果（1合格 2不合格）',
    defect_level    VARCHAR(10)                           COMMENT '最高缺陷等级（1致命 2严重 3一般 4轻微）',
    inspector_id    BIGINT                                COMMENT '检验员ID',
    inspector_name  VARCHAR(64)                           COMMENT '检验员名称',
    inspect_time    DATETIME                              COMMENT '检验时间',
    judge_time      DATETIME                              COMMENT '判定时间',
    gauge_id        BIGINT                                COMMENT '使用量检具ID',
    gauge_name      VARCHAR(128)                          COMMENT '量检具名称（冗余）',
    is_recheck      CHAR(1)      DEFAULT '0'              COMMENT '是否复检（0否 1是）',
    origin_task_id  BIGINT                                COMMENT '原任务ID（复检时关联）',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (task_id),
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_task_type (task_type),
    KEY idx_task_status (task_status),
    KEY idx_task_material (material_id),
    KEY idx_task_source (source_type, source_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验任务表';

-- 7. 检验单项实测表
DROP TABLE IF EXISTS qms_insp_item;
CREATE TABLE qms_insp_item (
    item_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    task_id         BIGINT       NOT NULL                 COMMENT '检验任务ID',
    std_id          BIGINt                                COMMENT '检验标准ID',
    std_name        VARCHAR(128)                          COMMENT '检验项目名称（冗余）',
    sample_seq      INT                                   COMMENT '抽样序号',
    measured_val    VARCHAR(255)                          COMMENT '实测值',
    item_result     VARCHAR(10)                           COMMENT '单项判定（1合格 2不合格）',
    defect_code     VARCHAR(64)                           COMMENT '不良代码（不合格时）',
    defect_name     VARCHAR(128)                          COMMENT '不良名称（冗余）',
    defect_level    VARCHAR(10)                           COMMENT '缺陷等级（1致命 2严重 3一般 4轻微）',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    PRIMARY KEY (item_id),
    KEY idx_item_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验单项实测表';

-- 8. 不合格品报告(NCR)表
DROP TABLE IF EXISTS qms_ncr;
CREATE TABLE qms_ncr (
    ncr_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'NCR ID',
    ncr_no          VARCHAR(64)  NOT NULL                 COMMENT 'NCR编号',
    source_type     VARCHAR(32)                           COMMENT '来源类型（inspection/complaint/patrol）',
    source_id       BIGINT                                COMMENT '来源ID',
    source_no       VARCHAR(64)                           COMMENT '来源单号',
    task_id         BIGINT                                COMMENT '检验任务ID',
    material_id     BIGINt                                COMMENT '物料ID',
    material_code   VARCHAR(64)                           COMMENT '物料编码（冗余）',
    material_name   VARCHAR(255)                          COMMENT '物料名称（冗余）',
    supplier_id     BIGINT                                COMMENT '供应商ID',
    supplier_name   VARCHAR(255)                          COMMENT '供应商名称（冗余）',
    batch_no        VARCHAR(64)                           COMMENT '批次号',
    defect_qty      DECIMAL(18,4)                         COMMENT '不合格数量',
    defect_level    VARCHAR(10)  NOT NULL                 COMMENT '缺陷等级（1致命 2严重 3一般 4轻微）',
    defect_desc     VARCHAR(1000)                         COMMENT '缺陷描述',
    disposition     VARCHAR(10)                           COMMENT '处置方式（1退货 2挑选 3返工 4报废 5让步接收）',
    isolate_flag    CHAR(1)      DEFAULT '0'              COMMENT '是否隔离（0否 1是）',
    isolate_area    VARCHAR(128)                          COMMENT '隔离区域',
    ncr_status      VARCHAR(10)  DEFAULT '0'              COMMENT 'NCR状态（0已登记 1评审中 2处置中 3待验证 4已关闭 5已作废）',
    discoverer_id   BIGINT                                COMMENT '发现人ID',
    discoverer_name VARCHAR(64)                           COMMENT '发现人名称',
    discover_time   DATETIME                              COMMENT '发现时间',
    resp_dept_id    BIGINT                                COMMENT '责任部门ID',
    resp_dept_name  VARCHAR(128)                          COMMENT '责任部门名称',
    verify_result   VARCHAR(10)                           COMMENT '验证结果（1通过 2不通过）',
    verify_remark   VARCHAR(500)                          COMMENT '验证说明',
    verifier_id     BIGINT                                COMMENT '验证人ID',
    verifier_name   VARCHAR(64)                           COMMENT '验证人名称',
    verify_time     DATETIME                              COMMENT '验证时间',
    close_time      DATETIME                              COMMENT '关闭时间',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (ncr_id),
    UNIQUE KEY uk_ncr_no (ncr_no),
    KEY idx_ncr_status (ncr_status),
    KEY idx_ncr_material (material_id),
    KEY idx_ncr_source (source_type, source_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不合格品报告表';

-- 9. NCR审批记录表（MRB工作流）
DROP TABLE IF EXISTS qms_ncr_approve_log;
CREATE TABLE qms_ncr_approve_log (
    log_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    ncr_id          BIGINT       NOT NULL                 COMMENT 'NCR ID',
    node_seq        INT                                   COMMENT '审批节点序号',
    node_name       VARCHAR(64)                           COMMENT '审批节点名称',
    approver_role   VARCHAR(64)                           COMMENT '审批角色标识',
    action_type     VARCHAR(10)  NOT NULL                 COMMENT '操作类型（1提交评审 2审批通过 3审批驳回 4处置提交 5验证通过 6验证不通过 7关闭）',
    approve_opinion VARCHAR(500)                          COMMENT '审批意见',
    approver_id     BIGINT                                COMMENT '审批人ID',
    approver_name   VARCHAR(64)                           COMMENT '审批人名称',
    approve_time    DATETIME                              COMMENT '审批时间',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    PRIMARY KEY (log_id),
    KEY idx_approve_ncr_id (ncr_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NCR审批记录表';

-- 为NCR表增加工作流字段
ALTER TABLE qms_ncr ADD COLUMN current_node INT DEFAULT 0 COMMENT '当前审批节点（0=未提交）' AFTER ncr_status;
ALTER TABLE qms_ncr ADD COLUMN max_node INT DEFAULT 0 COMMENT '最大审批节点数' AFTER current_node;


-- =============================================
-- 二、字典类型与字典数据
-- =============================================

-- 检验类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('检验类型', 'qms_insp_type', '0', 'admin', sysdate(), '质量检验类型'),
('缺陷等级', 'qms_defect_level', '0', 'admin', sysdate(), '质量缺陷等级'),
('检验任务状态', 'qms_task_status', '0', 'admin', sysdate(), '检验任务状态'),
('检验结果', 'qms_insp_result', '0', 'admin', sysdate(), '检验判定结果'),
('量检具状态', 'qms_gauge_status', '0', 'admin', sysdate(), '量检具状态'),
('判定规则', 'qms_judge_rule', '0', 'admin', sysdate(), '检验判定规则'),
('NCR状态', 'qms_ncr_status', '0', 'admin', sysdate(), '不合格品报告状态'),
('NCR处置方式', 'qms_disposition', '0', 'admin', sysdate(), '不合格品处置方式'),
('检验严格度', 'qms_inspect_level', '0', 'admin', sysdate(), '检验严格度'),
('行业类型', 'qms_industry_type', '0', 'admin', sysdate(), '行业模板类型'),
('NCR审批操作', 'qms_approve_action', '0', 'admin', sysdate(), 'NCR审批操作类型');

-- 检验类型字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '来料检验', 'IQC', 'qms_insp_type', '', 'primary', 'N', '0', 'admin', sysdate()),
(2, '过程检验', 'IPQC', 'qms_insp_type', '', 'success', 'N', '0', 'admin', sysdate()),
(3, '成品检验', 'FQC', 'qms_insp_type', '', 'warning', 'N', '0', 'admin', sysdate()),
(4, '出货检验', 'OQC', 'qms_insp_type', '', 'danger', 'N', '0', 'admin', sysdate());

-- 缺陷等级字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '致命', '1', 'qms_defect_level', '', 'danger', 'N', '0', 'admin', sysdate()),
(2, '严重', '2', 'qms_defect_level', '', 'danger', 'N', '0', 'admin', sysdate()),
(3, '一般', '3', 'qms_defect_level', '', 'warning', 'N', '0', 'admin', sysdate()),
(4, '轻微', '4', 'qms_defect_level', '', 'info', 'N', '0', 'admin', sysdate());

-- 检验任务状态字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '待检', '0', 'qms_task_status', '', 'info', 'Y', '0', 'admin', sysdate()),
(2, '检验中', '1', 'qms_task_status', '', 'warning', 'N', '0', 'admin', sysdate()),
(3, '已判定', '2', 'qms_task_status', '', 'success', 'N', '0', 'admin', sysdate()),
(4, '已作废', '3', 'qms_task_status', '', 'danger', 'N', '0', 'admin', sysdate());

-- 检验结果字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '合格', '1', 'qms_insp_result', '', 'success', 'N', '0', 'admin', sysdate()),
(2, '不合格', '2', 'qms_insp_result', '', 'danger', 'N', '0', 'admin', sysdate());

-- 量检具状态字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '在用', '0', 'qms_gauge_status', '', 'success', 'Y', '0', 'admin', sysdate()),
(2, '停用', '1', 'qms_gauge_status', '', 'danger', 'N', '0', 'admin', sysdate()),
(3, '待校准', '2', 'qms_gauge_status', '', 'warning', 'N', '0', 'admin', sysdate());

-- 判定规则字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '区间判定', '1', 'qms_judge_rule', '', '', 'Y', '0', 'admin', sysdate()),
(2, '单值判定', '2', 'qms_judge_rule', '', '', 'N', '0', 'admin', sysdate()),
(3, '定性判定', '3', 'qms_judge_rule', '', '', 'N', '0', 'admin', sysdate());

-- NCR状态字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '已登记', '0', 'qms_ncr_status', '', 'info', 'Y', '0', 'admin', sysdate()),
(2, '评审中', '1', 'qms_ncr_status', '', 'warning', 'N', '0', 'admin', sysdate()),
(3, '处置中', '2', 'qms_ncr_status', '', 'warning', 'N', '0', 'admin', sysdate()),
(4, '待验证', '3', 'qms_ncr_status', '', 'primary', 'N', '0', 'admin', sysdate()),
(5, '已关闭', '4', 'qms_ncr_status', '', 'success', 'N', '0', 'admin', sysdate()),
(6, '已作废', '5', 'qms_ncr_status', '', 'danger', 'N', '0', 'admin', sysdate());

-- NCR处置方式字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '退货', '1', 'qms_disposition', '', 'danger', 'N', '0', 'admin', sysdate()),
(2, '挑选使用', '2', 'qms_disposition', '', 'warning', 'N', '0', 'admin', sysdate()),
(3, '返工/返修', '3', 'qms_disposition', '', 'warning', 'N', '0', 'admin', sysdate()),
(4, '报废', '4', 'qms_disposition', '', 'danger', 'N', '0', 'admin', sysdate()),
(5, '让步接收', '5', 'qms_disposition', '', 'primary', 'N', '0', 'admin', sysdate());

-- 检验严格度字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '正常', '1', 'qms_inspect_level', '', '', 'Y', '0', 'admin', sysdate()),
(2, '加严', '2', 'qms_inspect_level', '', '', 'N', '0', 'admin', sysdate()),
(3, '放宽', '3', 'qms_inspect_level', '', '', 'N', '0', 'admin', sysdate());

-- 行业类型字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '新能源材料', '1', 'qms_industry_type', '', '', 'N', '0', 'admin', sysdate()),
(2, '绿色铝加工', '2', 'qms_industry_type', '', '', 'N', '0', 'admin', sysdate()),
(3, '精细化工', '3', 'qms_industry_type', '', '', 'N', '0', 'admin', sysdate()),
(4, '先进装备', '4', 'qms_industry_type', '', '', 'N', '0', 'admin', sysdate()),
(5, '高原食品', '5', 'qms_industry_type', '', '', 'N', '0', 'admin', sysdate());

-- NCR审批操作字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '提交评审', '1', 'qms_approve_action', '', 'primary', 'N', '0', 'admin', sysdate()),
(2, '审批通过', '2', 'qms_approve_action', '', 'success', 'N', '0', 'admin', sysdate()),
(3, '审批驳回', '3', 'qms_approve_action', '', 'danger', 'N', '0', 'admin', sysdate()),
(4, '处置提交', '4', 'qms_approve_action', '', 'warning', 'N', '0', 'admin', sysdate()),
(5, '验证通过', '5', 'qms_approve_action', '', 'success', 'N', '0', 'admin', sysdate()),
(6, '验证不通过', '6', 'qms_approve_action', '', 'danger', 'N', '0', 'admin', sysdate());


-- =============================================
-- 三、菜单与权限
-- =============================================

-- 顶级目录：质量管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('质量管理', 0, 6, 'qms', NULL, '', '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '质量管理目录');
SET @qmsParentId = LAST_INSERT_ID();

-- =============================================
-- 1、质量主数据
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('质量主数据', @qmsParentId, 1, 'master', NULL, '', '', 1, 0, 'M', '0', '0', '', 'dict', 'admin', sysdate(), '质量主数据目录');
SET @qmsMasterDirId = LAST_INSERT_ID();

-- 检验标准库
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('检验标准库', @qmsMasterDirId, 1, 'std', 'qms/std/index', '', 'QmsStd', 1, 0, 'C', '0', '0', 'qms:std:list', 'edit', 'admin', sysdate(), '检验标准库管理');
SET @qmsStdId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('标准查询', @qmsStdId, 1, '', 'F', '0', '0', 'qms:std:query', 'admin', sysdate()),
('标准新增', @qmsStdId, 2, '', 'F', '0', '0', 'qms:std:add', 'admin', sysdate()),
('标准修改', @qmsStdId, 3, '', 'F', '0', '0', 'qms:std:edit', 'admin', sysdate()),
('标准删除', @qmsStdId, 4, '', 'F', '0', '0', 'qms:std:remove', 'admin', sysdate()),
('标准导出', @qmsStdId, 5, '', 'F', '0', '0', 'qms:std:export', 'admin', sysdate());

-- 缺陷代码
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('缺陷代码', @qmsMasterDirId, 2, 'defect', 'qms/defect/index', '', 'QmsDefect', 1, 0, 'C', '0', '0', 'qms:defect:list', 'warning', 'admin', sysdate(), '缺陷代码管理');
SET @qmsDefectId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('缺陷查询', @qmsDefectId, 1, '', 'F', '0', '0', 'qms:defect:query', 'admin', sysdate()),
('缺陷新增', @qmsDefectId, 2, '', 'F', '0', '0', 'qms:defect:add', 'admin', sysdate()),
('缺陷修改', @qmsDefectId, 3, '', 'F', '0', '0', 'qms:defect:edit', 'admin', sysdate()),
('缺陷删除', @qmsDefectId, 4, '', 'F', '0', '0', 'qms:defect:remove', 'admin', sysdate()),
('缺陷导出', @qmsDefectId, 5, '', 'F', '0', '0', 'qms:defect:export', 'admin', sysdate());

-- 量检具台账
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('量检具台账', @qmsMasterDirId, 3, 'gauge', 'qms/gauge/index', '', 'QmsGauge', 1, 0, 'C', '0', '0', 'qms:gauge:list', 'component', 'admin', sysdate(), '量检具台账管理');
SET @qmsGaugeId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('量检具查询', @qmsGaugeId, 1, '', 'F', '0', '0', 'qms:gauge:query', 'admin', sysdate()),
('量检具新增', @qmsGaugeId, 2, '', 'F', '0', '0', 'qms:gauge:add', 'admin', sysdate()),
('量检具修改', @qmsGaugeId, 3, '', 'F', '0', '0', 'qms:gauge:edit', 'admin', sysdate()),
('量检具删除', @qmsGaugeId, 4, '', 'F', '0', '0', 'qms:gauge:remove', 'admin', sysdate()),
('量检具导出', @qmsGaugeId, 5, '', 'F', '0', '0', 'qms:gauge:export', 'admin', sysdate());

-- 行业模板配置
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('行业模板', @qmsMasterDirId, 4, 'tpl', 'qms/tpl/index', '', 'QmsTpl', 1, 0, 'C', '0', '0', 'qms:tpl:list', 'dashboard', 'admin', sysdate(), '行业模板配置');
SET @qmsTplId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('模板查询', @qmsTplId, 1, '', 'F', '0', '0', 'qms:tpl:query', 'admin', sysdate()),
('模板新增', @qmsTplId, 2, '', 'F', '0', '0', 'qms:tpl:add', 'admin', sysdate()),
('模板修改', @qmsTplId, 3, '', 'F', '0', '0', 'qms:tpl:edit', 'admin', sysdate()),
('模板删除', @qmsTplId, 4, '', 'F', '0', '0', 'qms:tpl:remove', 'admin', sysdate());

-- 物料质量属性
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('物料质量属性', @qmsMasterDirId, 5, 'materialattr', 'qms/materialattr/index', '', 'QmsMaterialAttr', 1, 0, 'C', '0', '0', 'qms:materialattr:list', 'list', 'admin', sysdate(), '物料质量属性管理');
SET @qmsMatAttrId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('属性查询', @qmsMatAttrId, 1, '', 'F', '0', '0', 'qms:materialattr:query', 'admin', sysdate()),
('属性新增', @qmsMatAttrId, 2, '', 'F', '0', '0', 'qms:materialattr:add', 'admin', sysdate()),
('属性修改', @qmsMatAttrId, 3, '', 'F', '0', '0', 'qms:materialattr:edit', 'admin', sysdate()),
('属性删除', @qmsMatAttrId, 4, '', 'F', '0', '0', 'qms:materialattr:remove', 'admin', sysdate());

-- =============================================
-- 2、质量检验
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('质量检验', @qmsParentId, 2, 'inspect', NULL, '', '', 1, 0, 'M', '0', '0', '', 'form', 'admin', sysdate(), '质量检验目录');
SET @qmsInspDirId = LAST_INSERT_ID();

-- 检验任务
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('检验任务', @qmsInspDirId, 1, 'task', 'qms/task/index', '', 'QmsTask', 1, 0, 'C', '0', '0', 'qms:task:list', 'documentation', 'admin', sysdate(), '检验任务管理');
SET @qmsTaskId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('任务查询', @qmsTaskId, 1, '', 'F', '0', '0', 'qms:task:query', 'admin', sysdate()),
('任务新增', @qmsTaskId, 2, '', 'F', '0', '0', 'qms:task:add', 'admin', sysdate()),
('任务修改', @qmsTaskId, 3, '', 'F', '0', '0', 'qms:task:edit', 'admin', sysdate()),
('任务删除', @qmsTaskId, 4, '', 'F', '0', '0', 'qms:task:remove', 'admin', sysdate()),
('任务导出', @qmsTaskId, 5, '', 'F', '0', '0', 'qms:task:export', 'admin', sysdate()),
('检验录入', @qmsTaskId, 6, '', 'F', '0', '0', 'qms:task:inspect', 'admin', sysdate());

-- =============================================
-- 3、不合格品控制
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('不合格品控制', @qmsParentId, 3, 'ncr', NULL, '', '', 1, 0, 'M', '0', '0', '', 'bug', 'admin', sysdate(), '不合格品控制目录');
SET @qmsNcrDirId = LAST_INSERT_ID();

-- NCR台账
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('NCR台账', @qmsNcrDirId, 1, 'ncrlist', 'qms/ncr/index', '', 'QmsNcr', 1, 0, 'C', '0', '0', 'qms:ncr:list', 'clipboard', 'admin', sysdate(), '不合格品报告台账');
SET @qmsNcrListId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('NCR查询', @qmsNcrListId, 1, '', 'F', '0', '0', 'qms:ncr:query', 'admin', sysdate()),
('NCR登记', @qmsNcrListId, 2, '', 'F', '0', '0', 'qms:ncr:add', 'admin', sysdate()),
('NCR修改', @qmsNcrListId, 3, '', 'F', '0', '0', 'qms:ncr:edit', 'admin', sysdate()),
('NCR删除', @qmsNcrListId, 4, '', 'F', '0', '0', 'qms:ncr:remove', 'admin', sysdate()),
('NCR导出', @qmsNcrListId, 5, '', 'F', '0', '0', 'qms:ncr:export', 'admin', sysdate()),
('NCR审批', @qmsNcrListId, 6, '', 'F', '0', '0', 'qms:ncr:approve', 'admin', sysdate());

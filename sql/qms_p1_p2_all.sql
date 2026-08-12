-- =============================================
-- QMS P1+P2 全量SQL脚本
-- 包含：CAPA / SQM / 客诉 / 质量目标 / 报表中心 / 文控 / 内审 / 管理评审
-- =============================================

-- =============================================
-- 一、业务表 DDL
-- =============================================

-- 1. CAPA 纠正预防措施表（8D报告）
DROP TABLE IF EXISTS qms_capa;
CREATE TABLE qms_capa (
    capa_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'CAPA ID',
    capa_no         VARCHAR(64)  NOT NULL                 COMMENT 'CAPA编号',
    source_type     VARCHAR(20)                           COMMENT '来源类型（ncr=不合格品, complaint=客诉, audit=内审）',
    source_no       VARCHAR(64)                           COMMENT '来源编号',
    title           VARCHAR(200) NOT NULL                 COMMENT '问题标题',
    problem_desc    TEXT                                  COMMENT 'D2问题描述',
    team_members    VARCHAR(500)                          COMMENT 'D1团队成员',
    interim_action  TEXT                                  COMMENT 'D3临时措施',
    root_cause      TEXT                                  COMMENT 'D4根因分析',
    permanent_action TEXT                                 COMMENT 'D5永久措施',
    verify_result   VARCHAR(500)                          COMMENT 'D6验证结果',
    prevent_action  TEXT                                  COMMENT 'D7预防措施',
    close_remark    VARCHAR(500)                          COMMENT 'D8关闭备注',
    capa_status     CHAR(1)      DEFAULT '0'              COMMENT '状态（0草稿 1进行中 2验证中 3已关闭 4已驳回）',
    responsible_person VARCHAR(64)                        COMMENT '责任人',
    plan_close_time DATETIME                              COMMENT '计划关闭时间',
    close_time      DATETIME                              COMMENT '实际关闭时间',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (capa_id),
    UNIQUE KEY uk_capa_no (capa_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CAPA纠正预防措施表';

-- 2. 供应商质量评价表
DROP TABLE IF EXISTS qms_supplier_eval;
CREATE TABLE qms_supplier_eval (
    eval_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '评价ID',
    supplier_id     BIGINT                                COMMENT '供应商ID',
    supplier_name   VARCHAR(200)                          COMMENT '供应商名称',
    eval_period     VARCHAR(20)                           COMMENT '评价周期（如2026-08）',
    eval_type       VARCHAR(20)                           COMMENT '评价类型（monthly=月度, quarterly=季度）',
    incoming_total  INT          DEFAULT 0                COMMENT '来料总批次',
    incoming_pass   INT          DEFAULT 0                COMMENT '来料合格批次',
    ppm             DECIMAL(10,2)                         COMMENT 'PPM（百万分之不良数）',
    pass_rate       DECIMAL(5,2)                          COMMENT '合格率(%)',
    quality_score   DECIMAL(5,2)                          COMMENT '质量评分(0-100)',
    delivery_score  DECIMAL(5,2)                          COMMENT '交期评分(0-100)',
    service_score   DECIMAL(5,2)                          COMMENT '服务评分(0-100)',
    total_score     DECIMAL(5,2)                          COMMENT '综合评分(0-100)',
    grade           CHAR(1)                               COMMENT '等级（A≥90 B80-89 C70-79 D<70）',
    eval_status     CHAR(1)      DEFAULT '0'              COMMENT '状态（0草稿 1已发布）',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (eval_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商质量评价表';

-- 3. 供应商审核表
DROP TABLE IF EXISTS qms_supplier_audit;
CREATE TABLE qms_supplier_audit (
    audit_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '审核ID',
    audit_no        VARCHAR(64)  NOT NULL                 COMMENT '审核编号',
    supplier_id     BIGINT                                COMMENT '供应商ID',
    supplier_name   VARCHAR(200)                          COMMENT '供应商名称',
    audit_type      VARCHAR(20)                           COMMENT '审核类型（initial=初审, routine=例行, follow=跟踪）',
    audit_date      DATE                                  COMMENT '审核日期',
    auditor         VARCHAR(64)                           COMMENT '审核员',
    audit_scope     VARCHAR(500)                          COMMENT '审核范围',
    audit_result    VARCHAR(20)                           COMMENT '审核结论（pass=通过, conditional=有条件通过, fail=不通过）',
    nonconformity   TEXT                                  COMMENT '不符合项描述',
    audit_status    CHAR(1)      DEFAULT '0'              COMMENT '状态（0计划中 1已完成 2已关闭）',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (audit_id),
    UNIQUE KEY uk_audit_no (audit_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商审核表';

-- 4. 客诉表
DROP TABLE IF EXISTS qms_complaint;
CREATE TABLE qms_complaint (
    complaint_id    BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '客诉ID',
    complaint_no    VARCHAR(64)  NOT NULL                 COMMENT '客诉编号',
    customer_name   VARCHAR(200)                          COMMENT '客户名称',
    customer_code   VARCHAR(64)                           COMMENT '客户编码',
    material_name   VARCHAR(200)                          COMMENT '涉事物料名称',
    material_code   VARCHAR(64)                           COMMENT '物料编码',
    batch_no        VARCHAR(64)                           COMMENT '批次号',
    complaint_date  DATE                                  COMMENT '投诉日期',
    complaint_type  VARCHAR(20)                           COMMENT '投诉类型（quality=质量, delivery=交期, service=服务）',
    defect_desc     TEXT                                  COMMENT '缺陷描述',
    severity        CHAR(1)                               COMMENT '严重程度（1严重 2一般 3轻微）',
    return_qty      DECIMAL(18,4)                         COMMENT '退货数量',
    return_amt      DECIMAL(18,2)                         COMMENT '退货金额',
    claim_amt       DECIMAL(18,2)                         COMMENT '索赔金额',
    handle_desc     TEXT                                  COMMENT '处理描述（8D回复）',
    handle_result   VARCHAR(20)                           COMMENT '处理结果（return=退货, exchange=换货, compensate=赔偿, close=关闭）',
    complaint_status CHAR(1)     DEFAULT '0'              COMMENT '状态（0已登记 1处理中 2待确认 3已关闭）',
    close_time      DATETIME                              COMMENT '关闭时间',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (complaint_id),
    UNIQUE KEY uk_complaint_no (complaint_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客诉表';

-- 5. 质量目标表
DROP TABLE IF EXISTS qms_quality_target;
CREATE TABLE qms_quality_target (
    target_id       BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '目标ID',
    target_year     INT          NOT NULL                 COMMENT '年份',
    target_month    INT                                   COMMENT '月份（NULL=年度目标）',
    dimension       VARCHAR(50)                           COMMENT '维度（factory=工厂, line=产线, material=物料类别）',
    dimension_val   VARCHAR(100)                          COMMENT '维度值',
    target_pass_rate DECIMAL(5,2)                         COMMENT '目标合格率(%)',
    actual_pass_rate DECIMAL(5,2)                         COMMENT '实际合格率(%)',
    target_ppm      INT                                   COMMENT '目标PPM',
    actual_ppm      INT                                   COMMENT '实际PPM',
    target_complaint_rate DECIMAL(5,2)                    COMMENT '目标客诉率(%)',
    actual_complaint_rate DECIMAL(5,2)                    COMMENT '实际客诉率(%)',
    target_capa_close_rate DECIMAL(5,2)                   COMMENT '目标CAPA关闭率(%)',
    actual_capa_close_rate DECIMAL(5,2)                   COMMENT '实际CAPA关闭率(%)',
    achievement     CHAR(1)                               COMMENT '达成情况（Y=达成 N=未达成）',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量目标表';

-- 6. 质量文档表（文控）
DROP TABLE IF EXISTS qms_doc;
CREATE TABLE qms_doc (
    doc_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '文档ID',
    doc_no          VARCHAR(64)  NOT NULL                 COMMENT '文档编号',
    doc_title       VARCHAR(200) NOT NULL                 COMMENT '文档标题',
    doc_category    VARCHAR(20)                           COMMENT '分类（manual=质量手册, procedure=程序文件, sop=作业指导书, form=记录表单）',
    version_no      VARCHAR(20)  DEFAULT 'v1.0'           COMMENT '当前版本号',
    doc_status      Char(1)      DEFAULT '0'              COMMENT '状态（0草稿 1审批中 2生效 3作废）',
    dept_id         BIGINT                                COMMENT '归属部门ID',
    dept_name       VARCHAR(100)                          COMMENT '归属部门名称',
    tpl_id          BIGINT                                COMMENT '行业模板ID',
    file_url        VARCHAR(500)                          COMMENT '文件地址',
    publish_date    DATE                                  COMMENT '生效日期',
    obsolete_date   DATE                                  COMMENT '作废日期',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (doc_id),
    UNIQUE KEY uk_doc_no (doc_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量文档表';

-- 7. 内审计划表
DROP TABLE IF EXISTS qms_audit_plan;
CREATE TABLE qms_audit_plan (
    audit_plan_id   BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '审核计划ID',
    audit_plan_no   VARCHAR(64)  NOT NULL                 COMMENT '审核编号',
    audit_title     VARCHAR(200) NOT NULL                 COMMENT '审核标题',
    audit_year      INT                                   COMMENT '审核年度',
    audit_type      VARCHAR(20)                           COMMENT '类型（annual=年度计划, adhoc=临时审核）',
    audit_scope     VARCHAR(500)                          COMMENT '审核范围',
    lead_auditor    VARCHAR(64)                           COMMENT '主审',
    audit_date_from DATE                                  COMMENT '审核开始日期',
    audit_date_to   DATE                                  COMMENT '审核结束日期',
    audit_conclusion TEXT                                 COMMENT '审核结论',
    plan_status     Char(1)      DEFAULT '0'              COMMENT '状态（0计划中 1执行中 2已完成 3已关闭）',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (audit_plan_id),
    UNIQUE KEY uk_audit_plan_no (audit_plan_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内审计划表';

-- 8. 内审不符合项表
DROP TABLE IF EXISTS qms_audit_item;
CREATE TABLE qms_audit_item (
    item_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '不符合项ID',
    audit_plan_id   BIGINT                                COMMENT '审核计划ID',
    audit_plan_no   VARCHAR(64)                           COMMENT '审核编号',
    clause_ref      VARCHAR(100)                          COMMENT '条款引用',
    nc_desc         TEXT                                  COMMENT '不符合项描述',
    nc_type         Char(1)                               COMMENT '不符合类型（1严重 2一般 3观察项）',
    dept_id         BIGINT                                COMMENT '责任部门ID',
    dept_name       VARCHAR(100)                          COMMENT '责任部门',
    corrective_action TEXT                                COMMENT '纠正措施',
    capa_id         BIGINT                                COMMENT '关联CAPA ID',
    verify_result   VARCHAR(500)                          COMMENT '验证结果',
    item_status     Char(1)      DEFAULT '0'              COMMENT '状态（0待整改 1整改中 2已验证 3已关闭）',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内审不符合项表';

-- 9. 管理评审表
DROP TABLE IF EXISTS qms_mr;
CREATE TABLE qms_mr (
    mr_id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '评审ID',
    mr_no           VARCHAR(64)  NOT NULL                 COMMENT '评审编号',
    mr_title        VARCHAR(200) NOT NULL                 COMMENT '评审标题',
    mr_year         INT                                   COMMENT '评审年度',
    mr_date         DATE                                  COMMENT '评审日期',
    chairperson     VARCHAR(64)                           COMMENT '主持人',
    participants    VARCHAR(500)                          COMMENT '参会人员',
    input_summary   TEXT                                  COMMENT '输入汇总（目标/客诉/CAPA/内审/供应商）',
    resolution      TEXT                                  COMMENT '决议事项',
    action_items    TEXT                                  COMMENT '改进项',
    mr_status       Char(1)      DEFAULT '0'              COMMENT '状态（0准备中 1已完成 2跟踪中 3已关闭）',
    del_flag        Char(1)      DEFAULT '0'              COMMENT '删除标志',
    status          Char(1)      DEFAULT '0'              COMMENT '状态',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (mr_id),
    UNIQUE KEY uk_mr_no (mr_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理评审表';


-- =============================================
-- 二、字典类型与数据
-- =============================================

-- CAPA状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('CAPA状态', 'qms_capa_status', '0', 'admin', sysdate(), 'CAPA状态');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '草稿', '0', 'qms_capa_status', 'Y', '0', 'admin', sysdate()),
(2, '进行中', '1', 'qms_capa_status', 'N', '0', 'admin', sysdate()),
(3, '验证中', '2', 'qms_capa_status', 'N', '0', 'admin', sysdate()),
(4, '已关闭', '3', 'qms_capa_status', 'N', '0', 'admin', sysdate()),
(5, '已驳回', '4', 'qms_capa_status', 'N', '0', 'admin', sysdate());

-- CAPA来源类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('CAPA来源类型', 'qms_capa_source', '0', 'admin', sysdate(), 'CAPA来源类型');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '不合格品NCR', 'ncr', 'qms_capa_source', 'Y', '0', 'admin', sysdate()),
(2, '客诉', 'complaint', 'qms_capa_source', 'N', '0', 'admin', sysdate()),
(3, '内审不符合项', 'audit', 'qms_capa_source', 'N', '0', 'admin', sysdate());

-- 供应商等级
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('供应商质量等级', 'qms_supplier_grade', '0', 'admin', sysdate(), '供应商质量等级');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time) VALUES
(1, 'A级(优秀)', 'A', 'qms_supplier_grade', 'success', 'N', '0', 'admin', sysdate()),
(2, 'B级(良好)', 'B', 'qms_supplier_grade', 'primary', 'N', '0', 'admin', sysdate()),
(3, 'C级(一般)', 'C', 'qms_supplier_grade', 'warning', 'N', '0', 'admin', sysdate()),
(4, 'D级(较差)', 'D', 'qms_supplier_grade', 'danger', 'N', '0', 'admin', sysdate());

-- 供应商审核类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('供应商审核类型', 'qms_supplier_audit_type', '0', 'admin', sysdate(), '供应商审核类型');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '初审', 'initial', 'qms_supplier_audit_type', 'Y', '0', 'admin', sysdate()),
(2, '例行审核', 'routine', 'qms_supplier_audit_type', 'N', '0', 'admin', sysdate()),
(3, '跟踪审核', 'follow', 'qms_supplier_audit_type', 'N', '0', 'admin', sysdate());

-- 客诉类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('客诉类型', 'qms_complaint_type', '0', 'admin', sysdate(), '客诉类型');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '质量投诉', 'quality', 'qms_complaint_type', 'Y', '0', 'admin', sysdate()),
(2, '交期投诉', 'delivery', 'qms_complaint_type', 'N', '0', 'admin', sysdate()),
(3, '服务投诉', 'service', 'qms_complaint_type', 'N', '0', 'admin', sysdate());

-- 客诉状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('客诉状态', 'qms_complaint_status', '0', 'admin', sysdate(), '客诉状态');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '已登记', '0', 'qms_complaint_status', 'Y', '0', 'admin', sysdate()),
(2, '处理中', '1', 'qms_complaint_status', 'N', '0', 'admin', sysdate()),
(3, '待确认', '2', 'qms_complaint_status', 'N', '0', 'admin', sysdate()),
(4, '已关闭', '3', 'qms_complaint_status', 'N', '0', 'admin', sysdate());

-- 质量文档分类
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('质量文档分类', 'qms_doc_category', '0', 'admin', sysdate(), '质量文档分类');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '质量手册', 'manual', 'qms_doc_category', 'Y', '0', 'admin', sysdate()),
(2, '程序文件', 'procedure', 'qms_doc_category', 'N', '0', 'admin', sysdate()),
(3, '作业指导书', 'sop', 'qms_doc_category', 'N', '0', 'admin', sysdate()),
(4, '记录表单', 'form', 'qms_doc_category', 'N', '0', 'admin', sysdate());

-- 质量文档状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('质量文档状态', 'qms_doc_status', '0', 'admin', sysdate(), '质量文档状态');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '草稿', '0', 'qms_doc_status', 'Y', '0', 'admin', sysdate()),
(2, '审批中', '1', 'qms_doc_status', 'N', '0', 'admin', sysdate()),
(3, '生效', '2', 'qms_doc_status', 'N', '0', 'admin', sysdate()),
(4, '作废', '3', 'qms_doc_status', 'N', '0', 'admin', sysdate());

-- 内审状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('内审计划状态', 'qms_audit_plan_status', '0', 'admin', sysdate(), '内审计划状态');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '计划中', '0', 'qms_audit_plan_status', 'Y', '0', 'admin', sysdate()),
(2, '执行中', '1', 'qms_audit_plan_status', 'N', '0', 'admin', sysdate()),
(3, '已完成', '2', 'qms_audit_plan_status', 'N', '0', 'admin', sysdate()),
(4, '已关闭', '3', 'qms_audit_plan_status', 'N', '0', 'admin', sysdate());

-- 内审不符合类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('内审不符合类型', 'qms_audit_nc_type', '0', 'admin', sysdate(), '内审不符合类型');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '严重不符合', '1', 'qms_audit_nc_type', 'N', '0', 'admin', sysdate()),
(2, '一般不符合', '2', 'qms_audit_nc_type', 'Y', '0', 'admin', sysdate()),
(3, '观察项', '3', 'qms_audit_nc_type', 'N', '0', 'admin', sysdate());

-- 管理评审状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('管理评审状态', 'qms_mr_status', '0', 'admin', sysdate(), '管理评审状态');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '准备中', '0', 'qms_mr_status', 'Y', '0', 'admin', sysdate()),
(2, '已完成', '1', 'qms_mr_status', 'N', '0', 'admin', sysdate()),
(3, '跟踪中', '2', 'qms_mr_status', 'N', '0', 'admin', sysdate()),
(4, '已关闭', '3', 'qms_mr_status', 'N', '0', 'admin', sysdate());

-- 质量目标维度
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('质量目标维度', 'qms_target_dimension', '0', 'admin', sysdate(), '质量目标维度');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '工厂', 'factory', 'qms_target_dimension', 'Y', '0', 'admin', sysdate()),
(2, '产线', 'line', 'qms_target_dimension', 'N', '0', 'admin', sysdate()),
(3, '物料类别', 'material', 'qms_target_dimension', 'N', '0', 'admin', sysdate());


-- =============================================
-- 三、菜单与权限
-- =============================================
SET @qmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name = '质量管理' AND parent_id = 0);

-- P1-1: CAPA管理目录
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('CAPA管理', @qmsParentId, 5, 'capa', NULL, '', 'QmsCapaDir', 1, 0, 'M', '0', '0', '', 'tool', 'admin', sysdate(), 'CAPA管理目录');
SET @qmsCapaDirId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('CAPA台账', @qmsCapaDirId, 1, 'list', 'qms/capa/index', '', 'QmsCapa', 1, 0, 'C', '0', '0', 'qms:capa:list', 'edit', 'admin', sysdate(), 'CAPA台账');
SET @qmsCapaId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('CAPA查询', @qmsCapaId, 1, '', 'F', '0', '0', 'qms:capa:query', 'admin', sysdate()),
('CAPA新增', @qmsCapaId, 2, '', 'F', '0', '0', 'qms:capa:add', 'admin', sysdate()),
('CAPA修改', @qmsCapaId, 3, '', 'F', '0', '0', 'qms:capa:edit', 'admin', sysdate()),
('CAPA删除', @qmsCapaId, 4, '', 'F', '0', '0', 'qms:capa:remove', 'admin', sysdate()),
('CAPA导出', @qmsCapaId, 5, '', 'F', '0', '0', 'qms:capa:export', 'admin', sysdate()),
('CAPA关闭', @qmsCapaId, 6, '', 'F', '0', '0', 'qms:capa:close', 'admin', sysdate());

-- P1-2: 供应商质量管理目录
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('供应商质量管理', @qmsParentId, 6, 'sqm', NULL, '', 'QmsSqmDir', 1, 0, 'M', '0', '0', '', 'people', 'admin', sysdate(), '供应商质量管理目录');
SET @qmsSqmDirId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('供应商质量评价', @qmsSqmDirId, 1, 'eval', 'qms/supplierEval/index', '', 'QmsSupplierEval', 1, 0, 'C', '0', '0', 'qms:supplierEval:list', 'chart', 'admin', sysdate(), '供应商质量评价');
SET @qmsSupplierEvalId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('评价查询', @qmsSupplierEvalId, 1, '', 'F', '0', '0', 'qms:supplierEval:query', 'admin', sysdate()),
('评价新增', @qmsSupplierEvalId, 2, '', 'F', '0', '0', 'qms:supplierEval:add', 'admin', sysdate()),
('评价修改', @qmsSupplierEvalId, 3, '', 'F', '0', '0', 'qms:supplierEval:edit', 'admin', sysdate()),
('评价删除', @qmsSupplierEvalId, 4, '', 'F', '0', '0', 'qms:supplierEval:remove', 'admin', sysdate()),
('评价导出', @qmsSupplierEvalId, 5, '', 'F', '0', '0', 'qms:supplierEval:export', 'admin', sysdate());

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('供应商审核', @qmsSqmDirId, 2, 'audit', 'qms/supplierAudit/index', '', 'QmsSupplierAudit', 1, 0, 'C', '0', '0', 'qms:supplierAudit:list', 'view', 'admin', sysdate(), '供应商审核');
SET @qmsSupplierAuditId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('审核查询', @qmsSupplierAuditId, 1, '', 'F', '0', '0', 'qms:supplierAudit:query', 'admin', sysdate()),
('审核新增', @qmsSupplierAuditId, 2, '', 'F', '0', '0', 'qms:supplierAudit:add', 'admin', sysdate()),
('审核修改', @qmsSupplierAuditId, 3, '', 'F', '0', '0', 'qms:supplierAudit:edit', 'admin', sysdate()),
('审核删除', @qmsSupplierAuditId, 4, '', 'F', '0', '0', 'qms:supplierAudit:remove', 'admin', sysdate()),
('审核导出', @qmsSupplierAuditId, 5, '', 'F', '0', '0', 'qms:supplierAudit:export', 'admin', sysdate());

-- P1-3: 客诉管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('客诉管理', @qmsParentId, 7, 'complaint', NULL, '', 'QmsComplaintDir', 1, 0, 'M', '0', '0', '', 'message', 'admin', sysdate(), '客诉管理目录');
SET @qmsComplaintDirId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('客诉台账', @qmsComplaintDirId, 1, 'list', 'qms/complaint/index', '', 'QmsComplaint', 1, 0, 'C', '0', '0', 'qms:complaint:list', 'edit', 'admin', sysdate(), '客诉台账');
SET @qmsComplaintId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('客诉查询', @qmsComplaintId, 1, '', 'F', '0', '0', 'qms:complaint:query', 'admin', sysdate()),
('客诉新增', @qmsComplaintId, 2, '', 'F', '0', '0', 'qms:complaint:add', 'admin', sysdate()),
('客诉修改', @qmsComplaintId, 3, '', 'F', '0', '0', 'qms:complaint:edit', 'admin', sysdate()),
('客诉删除', @qmsComplaintId, 4, '', 'F', '0', '0', 'qms:complaint:remove', 'admin', sysdate()),
('客诉导出', @qmsComplaintId, 5, '', 'F', '0', '0', 'qms:complaint:export', 'admin', sysdate()),
('客诉关闭', @qmsComplaintId, 6, '', 'F', '0', '0', 'qms:complaint:close', 'admin', sysdate());

-- P1-4: 质量目标
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('质量目标', @qmsParentId, 8, 'target', 'qms/target/index', '', 'QmsTarget', 1, 0, 'C', '0', '0', 'qms:target:list', 'rate', 'admin', sysdate(), '质量目标管理');
SET @qmsTargetId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('目标查询', @qmsTargetId, 1, '', 'F', '0', '0', 'qms:target:query', 'admin', sysdate()),
('目标新增', @qmsTargetId, 2, '', 'F', '0', '0', 'qms:target:add', 'admin', sysdate()),
('目标修改', @qmsTargetId, 3, '', 'F', '0', '0', 'qms:target:edit', 'admin', sysdate()),
('目标删除', @qmsTargetId, 4, '', 'F', '0', '0', 'qms:target:remove', 'admin', sysdate());

-- P1-5: 报表中心
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('报表中心', @qmsParentId, 9, 'report', 'qms/report/index', '', 'QmsReport', 1, 0, 'C', '0', '0', 'qms:report:list', 'documentation', 'admin', sysdate(), '报表中心');

-- P2-1: 体系文档目录
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('体系管理', @qmsParentId, 10, 'system', NULL, '', 'QmsSystemDir', 1, 0, 'M', '0', '0', '', 'guide', 'admin', sysdate(), '体系管理目录');
SET @qmsSystemDirId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('质量文档', @qmsSystemDirId, 1, 'doc', 'qms/doc/index', '', 'QmsDoc', 1, 0, 'C', '0', '0', 'qms:doc:list', 'documentation', 'admin', sysdate(), '质量文档管理');
SET @qmsDocId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('文档查询', @qmsDocId, 1, '', 'F', '0', '0', 'qms:doc:query', 'admin', sysdate()),
('文档新增', @qmsDocId, 2, '', 'F', '0', '0', 'qms:doc:add', 'admin', sysdate()),
('文档修改', @qmsDocId, 3, '', 'F', '0', '0', 'qms:doc:edit', 'admin', sysdate()),
('文档删除', @qmsDocId, 4, '', 'F', '0', '0', 'qms:doc:remove', 'admin', sysdate()),
('文档导出', @qmsDocId, 5, '', 'F', '0', '0', 'qms:doc:export', 'admin', sysdate());

-- P2-2: 内部审核
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('内部审核', @qmsSystemDirId, 2, 'audit', 'qms/audit/index', '', 'QmsAudit', 1, 0, 'C', '0', '0', 'qms:audit:list', 'eye-open', 'admin', sysdate(), '内部审核管理');
SET @qmsAuditId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('审核查询', @qmsAuditId, 1, '', 'F', '0', '0', 'qms:audit:query', 'admin', sysdate()),
('审核新增', @qmsAuditId, 2, '', 'F', '0', '0', 'qms:audit:add', 'admin', sysdate()),
('审核修改', @qmsAuditId, 3, '', 'F', '0', '0', 'qms:audit:edit', 'admin', sysdate()),
('审核删除', @qmsAuditId, 4, '', 'F', '0', '0', 'qms:audit:remove', 'admin', sysdate()),
('审核导出', @qmsAuditId, 5, '', 'F', '0', '0', 'qms:audit:export', 'admin', sysdate());

-- P2-3: 管理评审
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('管理评审', @qmsSystemDirId, 3, 'mr', 'qms/mr/index', '', 'QmsMr', 1, 0, 'C', '0', '0', 'qms:mr:list', 'clipboard', 'admin', sysdate(), '管理评审');
SET @qmsMrId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('评审查询', @qmsMrId, 1, '', 'F', '0', '0', 'qms:mr:query', 'admin', sysdate()),
('评审新增', @qmsMrId, 2, '', 'F', '0', '0', 'qms:mr:add', 'admin', sysdate()),
('评审修改', @qmsMrId, 3, '', 'F', '0', '0', 'qms:mr:edit', 'admin', sysdate()),
('评审删除', @qmsMrId, 4, '', 'F', '0', '0', 'qms:mr:remove', 'admin', sysdate()),
('评审导出', @qmsMrId, 5, '', 'F', '0', '0', 'qms:mr:export', 'admin', sysdate());

-- 授权admin角色
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'qms:capa%%' OR perms LIKE 'qms:supplierEval%%' OR perms LIKE 'qms:supplierAudit%%' OR perms LIKE 'qms:complaint%%' OR perms LIKE 'qms:target%%' OR perms LIKE 'qms:report%%' OR perms LIKE 'qms:doc%%' OR perms LIKE 'qms:audit%%' OR perms LIKE 'qms:mr%%';
-- 目录菜单也需要授权
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_name IN ('CAPA管理','供应商质量管理','客诉管理','体系管理') AND parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '质量管理' AND parent_id = 0);

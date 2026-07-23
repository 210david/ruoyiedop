-- =============================================
-- MK 营销管理子系统 - 全量SQL脚本
-- 包含：14张业务表 + 20个字典类型 + 菜单权限 + 基础数据
-- =============================================

-- =============================================
-- 一、业务表 DDL
-- =============================================

-- 1. 企业客户表
DROP TABLE IF EXISTS mk_customer;
CREATE TABLE mk_customer (
    customer_id        BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '客户ID',
    customer_no        VARCHAR(32)   NOT NULL                 COMMENT '客户编号',
    customer_name      VARCHAR(200)  NOT NULL                 COMMENT '企业名称',
    credit_code        VARCHAR(32)                            COMMENT '统一社会信用代码',
    customer_level     VARCHAR(10)   DEFAULT '3'              COMMENT '客户等级（1关键大客户 2重要客户 3普通客户 4潜在客户）',
    customer_source    VARCHAR(10)   DEFAULT '0'              COMMENT '客户来源（字典）',
    industry           VARCHAR(50)                            COMMENT '所属行业（字典）',
    company_size       VARCHAR(10)                            COMMENT '企业规模（字典）',
    province           VARCHAR(50)                            COMMENT '省',
    city               VARCHAR(50)                            COMMENT '市',
    district           VARCHAR(50)                            COMMENT '区',
    address            VARCHAR(500)                           COMMENT '详细地址',
    website            VARCHAR(200)                           COMMENT '官网',
    customer_status    VARCHAR(10)   DEFAULT '0'              COMMENT '客户状态（0意向客户 1签约客户 2合作中 3暂停合作 4流失客户）',
    first_contact_date DATE                                   COMMENT '首次接触时间',
    cooperation_date   DATE                                   COMMENT '合作开始时间',
    total_amount       DECIMAL(14,2) DEFAULT 0                COMMENT '累计交易额',
    user_id            BIGINT                                 COMMENT '负责人ID',
    dept_id            BIGINT                                 COMMENT '部门ID',
    del_flag           CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by          VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                               COMMENT '创建时间',
    update_by          VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                               COMMENT '更新时间',
    remark             VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (customer_id),
    UNIQUE KEY uk_customer_no (customer_no),
    KEY idx_customer_name (customer_name),
    KEY idx_customer_user (user_id),
    KEY idx_customer_dept (dept_id),
    KEY idx_customer_level (customer_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业客户表';

-- 2. 联系人表
DROP TABLE IF EXISTS mk_contact;
CREATE TABLE mk_contact (
    contact_id    BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '联系人ID',
    customer_id   BIGINT        NOT NULL                 COMMENT '客户ID',
    name          VARCHAR(50)   NOT NULL                 COMMENT '姓名',
    gender        CHAR(1)       DEFAULT '0'              COMMENT '性别（0男 1女）',
    position      VARCHAR(100)                           COMMENT '职位',
    department    VARCHAR(100)                           COMMENT '所属部门',
    role_tag      VARCHAR(200)                           COMMENT '角色标签（多选逗号分隔）',
    phone         VARCHAR(20)   NOT NULL                 COMMENT '手机号',
    email         VARCHAR(100)                           COMMENT '邮箱',
    wechat        VARCHAR(50)                            COMMENT '微信号',
    qq            VARCHAR(20)                            COMMENT 'QQ号',
    is_key        CHAR(1)       DEFAULT '0'              COMMENT '是否关键联系人（0否 1是）',
    is_primary    CHAR(1)       DEFAULT '0'              COMMENT '是否主要联系人（0否 1是）',
    personal_trait VARCHAR(500)                           COMMENT '个人特点',
    del_flag      CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by     VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time   DATETIME                               COMMENT '创建时间',
    update_by     VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time   DATETIME                               COMMENT '更新时间',
    remark        VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (contact_id),
    KEY idx_contact_customer (customer_id),
    KEY idx_contact_phone (phone),
    KEY idx_contact_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联系人表';

-- 3. 互动记录表
DROP TABLE IF EXISTS mk_interaction;
CREATE TABLE mk_interaction (
    record_id        BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    customer_id      BIGINT        NOT NULL                 COMMENT '客户ID',
    contact_id       BIGINT                                 COMMENT '联系人ID',
    opportunity_id   BIGINT                                 COMMENT '商机ID',
    lead_id          BIGINT                                 COMMENT '线索ID',
    interact_type    VARCHAR(10)   NOT NULL                 COMMENT '互动类型（字典）',
    interact_time    DATETIME      NOT NULL                 COMMENT '互动时间',
    content          TEXT                                   COMMENT '互动内容',
    user_id          BIGINT                                 COMMENT '跟进人ID',
    next_time        DATETIME                               COMMENT '下次跟进时间',
    next_content     VARCHAR(500)                           COMMENT '下次跟进内容',
    attachment       VARCHAR(500)                           COMMENT '附件URL',
    del_flag         CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                               COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                               COMMENT '更新时间',
    PRIMARY KEY (record_id),
    KEY idx_interaction_customer (customer_id),
    KEY idx_interaction_lead (lead_id),
    KEY idx_interaction_opp (opportunity_id),
    KEY idx_interaction_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='互动记录表';

-- 4. 线索表
DROP TABLE IF EXISTS mk_lead;
CREATE TABLE mk_lead (
    lead_id               BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '线索ID',
    lead_no               VARCHAR(32)   NOT NULL                 COMMENT '线索编号',
    company_name          VARCHAR(200)  NOT NULL                 COMMENT '企业名称',
    contact_name          VARCHAR(50)   NOT NULL                 COMMENT '联系人姓名',
    contact_phone         VARCHAR(20)   NOT NULL                 COMMENT '联系人手机号',
    contact_email         VARCHAR(100)                           COMMENT '联系人邮箱',
    position              VARCHAR(100)                           COMMENT '职位',
    industry              VARCHAR(50)                            COMMENT '所属行业',
    company_size          VARCHAR(10)                            COMMENT '企业规模',
    address               VARCHAR(500)                           COMMENT '详细地址',
    lead_source           VARCHAR(10)   DEFAULT '0'              COMMENT '线索来源（字典）',
    activity_id           BIGINT                                 COMMENT '来源活动ID',
    requirement_desc      TEXT                                   COMMENT '需求描述',
    lead_score            INT           DEFAULT 0                COMMENT '线索评分（0-100）',
    lead_grade            VARCHAR(2)    DEFAULT 'D'              COMMENT '线索等级（A/B/C/D）',
    lead_status           VARCHAR(10)   DEFAULT '0'              COMMENT '线索状态（0新线索 1培育中 2MQL 3SQL 4已转化 5已无效）',
    is_public             CHAR(1)       DEFAULT '0'              COMMENT '是否在公海（0否 1是）',
    user_id               BIGINT                                 COMMENT '负责人ID',
    dept_id               BIGINT                                 COMMENT '部门ID',
    receive_time          DATETIME                               COMMENT '领取时间',
    receive_status        VARCHAR(10)   DEFAULT '0'              COMMENT '领取审批状态（0未申请 1待审批 2已批准 3已拒绝）',
    receive_remark        VARCHAR(500)                           COMMENT '领取审批备注',
    receive_apply_user_id BIGINT                                 COMMENT '领取申请人ID',
    receive_apply_time    DATETIME                               COMMENT '领取申请时间',
    last_follow_time      DATETIME                               COMMENT '最后跟进时间',
    ineffective_reason    VARCHAR(10)                            COMMENT '无效原因',
    ineffective_remark    VARCHAR(500)                           COMMENT '无效说明',
    convert_customer_id   BIGINT                                 COMMENT '转化后的客户ID',
    convert_time          DATETIME                               COMMENT '转化时间',
    del_flag              CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by             VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time           DATETIME                               COMMENT '创建时间',
    update_by             VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time           DATETIME                               COMMENT '更新时间',
    remark                VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (lead_id),
    UNIQUE KEY uk_lead_no (lead_no),
    KEY idx_lead_company (company_name),
    KEY idx_lead_phone (contact_phone),
    KEY idx_lead_status (lead_status),
    KEY idx_lead_user (user_id),
    KEY idx_lead_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线索表';

-- 5. 商机表
DROP TABLE IF EXISTS mk_opportunity;
CREATE TABLE mk_opportunity (
    opportunity_id      BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '商机ID',
    opportunity_no      VARCHAR(32)   NOT NULL                 COMMENT '商机编号',
    opportunity_name    VARCHAR(200)  NOT NULL                 COMMENT '商机名称',
    customer_id         BIGINT        NOT NULL                 COMMENT '客户ID',
    contact_id          BIGINT                                 COMMENT '主要联系人ID',
    lead_id             BIGINT                                 COMMENT '来源线索ID',
    opportunity_source  VARCHAR(10)   DEFAULT '0'              COMMENT '商机来源（字典）',
    expected_amount     DECIMAL(14,2) NOT NULL                 COMMENT '预计金额',
    expected_date       DATE          NOT NULL                 COMMENT '预计成交日期',
    stage_code          VARCHAR(20)   DEFAULT 'stage1'         COMMENT '销售阶段编码',
    stage_name          VARCHAR(50)                            COMMENT '销售阶段名称',
    win_rate            INT           DEFAULT 10               COMMENT '赢率（%）',
    weighted_amount     DECIMAL(14,2) DEFAULT 0                COMMENT '加权金额',
    opportunity_status  VARCHAR(10)   DEFAULT '0'              COMMENT '商机状态（0进行中 1已赢单 2已输单）',
    competitor          VARCHAR(200)                           COMMENT '竞争对手',
    pain_point          TEXT                                   COMMENT '客户痛点',
    solution            TEXT                                   COMMENT '解决方案',
    lost_reason         VARCHAR(10)                            COMMENT '输单原因',
    lost_remark         VARCHAR(500)                           COMMENT '输单说明',
    actual_amount       DECIMAL(14,2)                          COMMENT '实际成交金额',
    actual_date         DATE                                   COMMENT '实际成交日期',
    user_id             BIGINT                                 COMMENT '负责人ID',
    dept_id             BIGINT                                 COMMENT '部门ID',
    next_action         VARCHAR(500)                           COMMENT '下一步行动',
    next_time           DATETIME                               COMMENT '下次行动时间',
    stage_enter_time    DATETIME                               COMMENT '进入当前阶段时间',
    del_flag            CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by           VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time         DATETIME                               COMMENT '创建时间',
    update_by           VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time         DATETIME                               COMMENT '更新时间',
    remark              VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (opportunity_id),
    UNIQUE KEY uk_opportunity_no (opportunity_no),
    KEY idx_opp_customer (customer_id),
    KEY idx_opp_stage (stage_code),
    KEY idx_opp_status (opportunity_status),
    KEY idx_opp_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机表';

-- 6. 销售阶段配置表
DROP TABLE IF EXISTS mk_opportunity_stage;
CREATE TABLE mk_opportunity_stage (
    stage_id         BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '阶段ID',
    stage_code       VARCHAR(20)   NOT NULL                 COMMENT '阶段编码',
    stage_name       VARCHAR(50)   NOT NULL                 COMMENT '阶段名称',
    sort             INT           NOT NULL                 COMMENT '排序号',
    win_rate         INT           DEFAULT 0                COMMENT '赢率（%）',
    max_days         INT                                    COMMENT '最大停留天数',
    required_fields  VARCHAR(500)                           COMMENT '阶段推进必填字段',
    status           CHAR(1)       DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                               COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                               COMMENT '更新时间',
    remark           VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (stage_id),
    UNIQUE KEY uk_stage_code (stage_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售阶段配置表';

-- 7. 营销活动表
DROP TABLE IF EXISTS mk_activity;
CREATE TABLE mk_activity (
    activity_id         BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '活动ID',
    activity_no         VARCHAR(32)   NOT NULL                 COMMENT '活动编号',
    activity_name       VARCHAR(200)  NOT NULL                 COMMENT '活动名称',
    activity_type       VARCHAR(10)   NOT NULL                 COMMENT '活动类型（字典）',
    activity_form       CHAR(1)       DEFAULT '0'              COMMENT '活动形式（0线上 1线下）',
    activity_status     VARCHAR(10)   DEFAULT '0'              COMMENT '活动状态（0草稿 1报名中 2进行中 3已结束 4已取消）',
    start_time          DATETIME                               COMMENT '开始时间',
    end_time            DATETIME                               COMMENT '结束时间',
    location            VARCHAR(500)                           COMMENT '活动地点/链接',
    budget              DECIMAL(14,2) DEFAULT 0                COMMENT '活动预算',
    target_count        INT                                    COMMENT '目标人数',
    target_lead_count   INT                                    COMMENT '目标线索数',
    user_id             BIGINT                                 COMMENT '负责人ID',
    dept_id             BIGINT                                 COMMENT '部门ID',
    summary             VARCHAR(500)                           COMMENT '活动简介',
    content             TEXT                                   COMMENT '活动详情',
    actual_count        INT           DEFAULT 0                COMMENT '实际参与人数',
    lead_count          INT           DEFAULT 0                COMMENT '获取线索数',
    mql_count           INT           DEFAULT 0                COMMENT 'MQL数量',
    sql_count           INT           DEFAULT 0                COMMENT 'SQL数量',
    opportunity_count   INT           DEFAULT 0                COMMENT '转化商机数',
    deal_amount         DECIMAL(14,2) DEFAULT 0                COMMENT '成交金额',
    del_flag            CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by           VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time         DATETIME                               COMMENT '创建时间',
    update_by           VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time         DATETIME                               COMMENT '更新时间',
    remark              VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (activity_id),
    UNIQUE KEY uk_activity_no (activity_no),
    KEY idx_activity_type (activity_type),
    KEY idx_activity_status (activity_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营销活动表';

-- 8. 活动参与人表
DROP TABLE IF EXISTS mk_activity_participant;
CREATE TABLE mk_activity_participant (
    participant_id      BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '参与ID',
    activity_id         BIGINT        NOT NULL                 COMMENT '活动ID',
    lead_id             BIGINT                                 COMMENT '关联线索ID',
    company_name        VARCHAR(200)                           COMMENT '企业名称',
    contact_name        VARCHAR(50)   NOT NULL                 COMMENT '联系人姓名',
    contact_phone       VARCHAR(20)   NOT NULL                 COMMENT '联系人手机号',
    contact_email       VARCHAR(100)                           COMMENT '联系人邮箱',
    position            VARCHAR(100)                           COMMENT '职位',
    participate_status  CHAR(1)       DEFAULT '0'              COMMENT '参与状态（0报名 1已签到 2未出席）',
    sign_time           DATETIME                               COMMENT '签到时间',
    create_by           VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time         DATETIME                               COMMENT '创建时间',
    update_by           VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time         DATETIME                               COMMENT '更新时间',
    remark              VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (participant_id),
    KEY idx_participant_activity (activity_id),
    KEY idx_participant_lead (lead_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动参与人表';

-- 9. 合同表
DROP TABLE IF EXISTS mk_contract;
CREATE TABLE mk_contract (
    contract_id       BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '合同ID',
    contract_no       VARCHAR(32)   NOT NULL                 COMMENT '合同编号',
    contract_name     VARCHAR(200)  NOT NULL                 COMMENT '合同名称',
    contract_type     VARCHAR(10)   DEFAULT '0'              COMMENT '合同类型（0新签 1续签 2增购 3升级）',
    customer_id       BIGINT        NOT NULL                 COMMENT '客户ID',
    opportunity_id    BIGINT                                 COMMENT '关联商机ID',
    contract_amount   DECIMAL(14,2) NOT NULL                 COMMENT '合同金额',
    sign_date         DATE          NOT NULL                 COMMENT '签约日期',
    effective_date    DATE          NOT NULL                 COMMENT '生效日期',
    expire_date       DATE          NOT NULL                 COMMENT '到期日期',
    payment_method    CHAR(1)       DEFAULT '0'              COMMENT '付款方式（0一次性 1分期）',
    contract_status   VARCHAR(10)   DEFAULT '0'              COMMENT '合同状态（0草稿 1审批中 2已生效 3已过期 4已终止）',
    user_id           BIGINT                                 COMMENT '负责人ID',
    dept_id           BIGINT                                 COMMENT '部门ID',
    attachment        VARCHAR(500)                           COMMENT '合同附件',
    del_flag          CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by         VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time       DATETIME                               COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time       DATETIME                               COMMENT '更新时间',
    remark            VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (contract_id),
    UNIQUE KEY uk_contract_no (contract_no),
    KEY idx_contract_customer (customer_id),
    KEY idx_contract_status (contract_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同表';

-- 10. 订单表
DROP TABLE IF EXISTS mk_order;
CREATE TABLE mk_order (
    order_id            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '订单ID',
    order_no            VARCHAR(32)   NOT NULL                 COMMENT '订单编号',
    contract_id         BIGINT                                 COMMENT '关联合同ID',
    customer_id         BIGINT        NOT NULL                 COMMENT '客户ID',
    order_amount        DECIMAL(14,2) NOT NULL                 COMMENT '订单金额',
    order_status        VARCHAR(10)   DEFAULT '0'              COMMENT '订单状态（0待发货 1已发货 2已完成 3已取消）',
    logistics_company   VARCHAR(50)                            COMMENT '物流公司',
    tracking_no         VARCHAR(50)                            COMMENT '物流单号',
    deliver_time        DATETIME                               COMMENT '发货时间',
    finish_time         DATETIME                               COMMENT '完成时间',
    user_id             BIGINT                                 COMMENT '负责人ID',
    dept_id             BIGINT                                 COMMENT '部门ID',
    del_flag            CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by           VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time         DATETIME                               COMMENT '创建时间',
    update_by           VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time         DATETIME                               COMMENT '更新时间',
    remark              VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_customer (customer_id),
    KEY idx_order_contract (contract_id),
    KEY idx_order_status (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 11. 订单明细表
DROP TABLE IF EXISTS mk_order_item;
CREATE TABLE mk_order_item (
    item_id         BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    order_id        BIGINT        NOT NULL                 COMMENT '订单ID',
    product_name    VARCHAR(200)  NOT NULL                 COMMENT '商品名称',
    product_spec    VARCHAR(200)                           COMMENT '规格型号',
    unit            VARCHAR(20)                            COMMENT '单位',
    quantity        INT           NOT NULL                 COMMENT '数量',
    unit_price      DECIMAL(14,2) NOT NULL                 COMMENT '单价',
    subtotal        DECIMAL(14,2) NOT NULL                 COMMENT '小计',
    create_time     DATETIME                               COMMENT '创建时间',
    remark          VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (item_id),
    KEY idx_order_item_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 12. 回款计划表
DROP TABLE IF EXISTS mk_payment_plan;
CREATE TABLE mk_payment_plan (
    plan_id          BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '计划ID',
    contract_id      BIGINT        NOT NULL                 COMMENT '合同ID',
    customer_id      BIGINT        NOT NULL                 COMMENT '客户ID',
    period_no        INT           NOT NULL                 COMMENT '期次',
    plan_amount      DECIMAL(14,2) NOT NULL                 COMMENT '计划回款金额',
    plan_date        DATE          NOT NULL                 COMMENT '计划回款日期',
    actual_amount    DECIMAL(14,2) DEFAULT 0                COMMENT '实际回款金额',
    actual_date      DATE                                   COMMENT '实际回款日期',
    payment_status   VARCHAR(10)   DEFAULT '0'              COMMENT '回款状态（0待回款 1已回款 2已逾期）',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                               COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                               COMMENT '更新时间',
    remark           VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (plan_id),
    KEY idx_payment_contract (contract_id),
    KEY idx_payment_customer (customer_id),
    KEY idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回款计划表';

-- 13. 客户标签表
DROP TABLE IF EXISTS mk_customer_tag;
CREATE TABLE mk_customer_tag (
    tag_id      BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '标签ID',
    tag_name    VARCHAR(50)   NOT NULL                 COMMENT '标签名称',
    tag_type    CHAR(1)       DEFAULT '0'              COMMENT '标签类型（0手动 1自动）',
    tag_color   VARCHAR(20)                            COMMENT '标签颜色',
    sort        INT           DEFAULT 0                COMMENT '排序',
    status      CHAR(1)       DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    create_by   VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time DATETIME                               COMMENT '创建时间',
    update_by   VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time DATETIME                               COMMENT '更新时间',
    remark      VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户标签表';

-- 14. 客户标签关联表
DROP TABLE IF EXISTS mk_customer_tag_rel;
CREATE TABLE mk_customer_tag_rel (
    id          BIGINT   NOT NULL AUTO_INCREMENT  COMMENT '主键',
    customer_id BIGINT   NOT NULL                 COMMENT '客户ID',
    tag_id      BIGINT   NOT NULL                 COMMENT '标签ID',
    create_time DATETIME                          COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_customer_tag (customer_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户标签关联表';


-- =============================================
-- 二、字典数据
-- =============================================

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('客户等级', 'marketing_customer_level', '0', 'admin', sysdate(), '客户等级字典'),
('客户来源', 'marketing_customer_source', '0', 'admin', sysdate(), '客户来源字典'),
('行业分类', 'marketing_industry', '0', 'admin', sysdate(), '行业分类字典'),
('企业规模', 'marketing_company_size', '0', 'admin', sysdate(), '企业规模字典'),
('客户状态', 'marketing_customer_status', '0', 'admin', sysdate(), '客户状态字典'),
('联系人角色', 'marketing_contact_role', '0', 'admin', sysdate(), '联系人角色字典'),
('互动类型', 'marketing_interaction_type', '0', 'admin', sysdate(), '互动类型字典'),
('线索状态', 'marketing_lead_status', '0', 'admin', sysdate(), '线索状态字典'),
('线索等级', 'marketing_lead_grade', '0', 'admin', sysdate(), '线索等级字典'),
('无效原因', 'marketing_ineffective_reason', '0', 'admin', sysdate(), '线索无效原因字典'),
('商机来源', 'marketing_opportunity_source', '0', 'admin', sysdate(), '商机来源字典'),
('商机状态', 'marketing_opportunity_status', '0', 'admin', sysdate(), '商机状态字典'),
('输单原因', 'marketing_lost_reason', '0', 'admin', sysdate(), '输单原因字典'),
('活动类型', 'marketing_activity_type', '0', 'admin', sysdate(), '活动类型字典'),
('活动状态', 'marketing_activity_status', '0', 'admin', sysdate(), '活动状态字典'),
('合同类型', 'marketing_contract_type', '0', 'admin', sysdate(), '合同类型字典'),
('合同状态', 'marketing_contract_status', '0', 'admin', sysdate(), '合同状态字典'),
('订单状态', 'marketing_order_status', '0', 'admin', sysdate(), '订单状态字典'),
('回款状态', 'marketing_payment_status', '0', 'admin', sysdate(), '回款状态字典'),
('合同变更类型', 'marketing_contract_change_type', '0', 'admin', sysdate(), '合同变更类型字典'),
('合同变更状态', 'marketing_contract_change_status', '0', 'admin', sysdate(), '合同变更状态字典'),
('参与状态', 'marketing_participate_status', '0', 'admin', sysdate(), '活动参与状态字典');

-- 字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '关键大客户', '1', 'marketing_customer_level', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(2, '重要客户', '2', 'marketing_customer_level', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '普通客户', '3', 'marketing_customer_level', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(4, '潜在客户', '4', 'marketing_customer_level', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '线上广告', '0', 'marketing_customer_source', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '展会', '1', 'marketing_customer_source', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '转介绍', '2', 'marketing_customer_source', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '官网', '3', 'marketing_customer_source', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(5, '其他', '4', 'marketing_customer_source', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '互联网/IT', '0', 'marketing_industry', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '制造业', '1', 'marketing_industry', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '金融', '2', 'marketing_industry', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '教育', '3', 'marketing_industry', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(5, '医疗', '4', 'marketing_industry', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(6, '零售', '5', 'marketing_industry', '', '', 'N', '0', 'admin', sysdate(), ''),
(7, '物流', '6', 'marketing_industry', '', '', 'N', '0', 'admin', sysdate(), ''),
(8, '其他', '7', 'marketing_industry', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '大型', '0', 'marketing_company_size', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(2, '中型', '1', 'marketing_company_size', '', 'warning', 'Y', '0', 'admin', sysdate(), ''),
(3, '小型', '2', 'marketing_company_size', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(4, '微型', '3', 'marketing_company_size', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '意向客户', '0', 'marketing_customer_status', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '签约客户', '1', 'marketing_customer_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '合作中', '2', 'marketing_customer_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '暂停合作', '3', 'marketing_customer_status', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(5, '流失客户', '4', 'marketing_customer_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '决策人', '0', 'marketing_contact_role', '', 'danger', 'Y', '0', 'admin', sysdate(), ''),
(2, '经济购买者', '1', 'marketing_contact_role', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '使用者', '2', 'marketing_contact_role', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(4, '影响者', '3', 'marketing_contact_role', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(5, '守门人', '4', 'marketing_contact_role', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '电话', '0', 'marketing_interaction_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '拜访', '1', 'marketing_interaction_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '邮件', '2', 'marketing_interaction_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(4, '会议', '3', 'marketing_interaction_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(5, '微信', '4', 'marketing_interaction_type', '', '', 'N', '0', 'admin', sysdate(), ''),
(6, '其他', '5', 'marketing_interaction_type', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '新线索', '0', 'marketing_lead_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '培育中', '1', 'marketing_lead_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, 'MQL', '2', 'marketing_lead_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, 'SQL', '3', 'marketing_lead_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(5, '已转化', '4', 'marketing_lead_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(6, '已无效', '5', 'marketing_lead_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, 'A级(优质)', 'A', 'marketing_lead_grade', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(2, 'B级(良好)', 'B', 'marketing_lead_grade', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, 'C级(一般)', 'C', 'marketing_lead_grade', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, 'D级(较差)', 'D', 'marketing_lead_grade', '', 'info', 'Y', '0', 'admin', sysdate(), ''),

(1, '电话空号', '0', 'marketing_ineffective_reason', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(2, '需求不符', '1', 'marketing_ineffective_reason', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '无预算', '2', 'marketing_ineffective_reason', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(4, '其他', '3', 'marketing_ineffective_reason', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '线索转化', '0', 'marketing_opportunity_source', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '老客户增购', '1', 'marketing_opportunity_source', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '主动开发', '2', 'marketing_opportunity_source', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '转介绍', '3', 'marketing_opportunity_source', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '进行中', '0', 'marketing_opportunity_status', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '已赢单', '1', 'marketing_opportunity_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '已输单', '2', 'marketing_opportunity_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '价格高', '0', 'marketing_lost_reason', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(2, '竞品中标', '1', 'marketing_lost_reason', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(3, '需求变更', '2', 'marketing_lost_reason', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(4, '其他', '3', 'marketing_lost_reason', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '线上研讨会', '0', 'marketing_activity_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '展会', '1', 'marketing_activity_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '行业峰会', '2', 'marketing_activity_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '客户沙龙', '3', 'marketing_activity_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(5, '产品培训', '4', 'marketing_activity_type', '', '', 'N', '0', 'admin', sysdate(), ''),
(6, '广告投放', '5', 'marketing_activity_type', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(7, '其他', '6', 'marketing_activity_type', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '草稿', '0', 'marketing_activity_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '报名中', '1', 'marketing_activity_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '进行中', '2', 'marketing_activity_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '已结束', '3', 'marketing_activity_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(5, '已取消', '4', 'marketing_activity_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '新签', '0', 'marketing_contract_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '续签', '1', 'marketing_contract_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '增购', '2', 'marketing_contract_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '升级', '3', 'marketing_contract_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '草稿', '0', 'marketing_contract_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '审批中', '1', 'marketing_contract_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '已生效', '2', 'marketing_contract_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '已过期', '3', 'marketing_contract_status', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(5, '已终止', '4', 'marketing_contract_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(6, '变更审批中', '5', 'marketing_contract_status', '', 'warning', 'N', '0', 'admin', sysdate(), '变更审批中状态'),

(1, '金额变更', '1', 'marketing_contract_change_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '日期变更', '2', 'marketing_contract_change_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(3, '条款变更', '3', 'marketing_contract_change_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '其他', '4', 'marketing_contract_change_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),

(1, '待审批', '0', 'marketing_contract_change_status', '', 'warning', 'Y', '0', 'admin', sysdate(), ''),
(2, '已批准', '1', 'marketing_contract_change_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '已驳回', '2', 'marketing_contract_change_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(4, '已执行', '3', 'marketing_contract_change_status', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '待确认', '0', 'marketing_order_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '已确认', '1', 'marketing_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '已发货', '2', 'marketing_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '已签收', '3', 'marketing_order_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(5, '已完成', '4', 'marketing_order_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(6, '已取消', '5', 'marketing_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(7, '退货中', '6', 'marketing_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(8, '已退货', '7', 'marketing_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '待回款', '0', 'marketing_payment_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '已回款', '1', 'marketing_payment_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '已逾期', '2', 'marketing_payment_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(4, '部分回款', '3', 'marketing_payment_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),

(1, '报名', '0', 'marketing_participate_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '已签到', '1', 'marketing_participate_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '未出席', '2', 'marketing_participate_status', '', 'danger', 'N', '0', 'admin', sysdate(), '');


-- =============================================
-- 三、菜单权限
-- =============================================

-- 顶级目录：营销管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4000, '营销管理', 0, 7, 'mk', NULL, '', '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '营销管理目录');

-- 二级目录
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4001, '客户管理', 4000, 1, 'customer', NULL, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), ''),
(4002, '线索管理', 4000, 2, 'lead', NULL, '', '', 1, 0, 'M', '0', '0', '', 'guide', 'admin', sysdate(), ''),
(4003, '商机管理', 4000, 3, 'opportunity', NULL, '', '', 1, 0, 'M', '0', '0', '', 'star', 'admin', sysdate(), ''),
(4004, '营销活动', 4000, 4, 'activity', NULL, '', '', 1, 0, 'M', '0', '0', '', 'message', 'admin', sysdate(), ''),
(4005, '合同订单', 4000, 5, 'contract', NULL, '', '', 1, 0, 'M', '0', '0', '', 'documentation', 'admin', sysdate(), ''),
(4006, '数据分析', 4000, 6, 'report', NULL, '', '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '');

-- 客户管理菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4010, '企业客户', 4001, 1, 'list', 'mk/customer/index', '', 'MkCustomer', 1, 0, 'C', '0', '0', 'marketing:customer:list', 'peoples', 'admin', sysdate(), ''),
(4020, '联系人管理', 4001, 2, 'contact', 'mk/contact/index', '', 'MkContact', 1, 0, 'C', '0', '0', 'marketing:contact:list', 'user', 'admin', sysdate(), ''),
(4030, '互动记录', 4001, 3, 'interaction', 'mk/interaction/index', '', 'MkInteraction', 1, 0, 'C', '0', '0', 'marketing:interaction:list', 'phone', 'admin', sysdate(), '');

-- 企业客户按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4011, '客户查询', 4010, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:customer:query', '#', 'admin', sysdate(), ''),
(4012, '客户新增', 4010, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:customer:add', '#', 'admin', sysdate(), ''),
(4013, '客户修改', 4010, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:customer:edit', '#', 'admin', sysdate(), ''),
(4014, '客户删除', 4010, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:customer:remove', '#', 'admin', sysdate(), ''),
(4015, '客户导出', 4010, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:customer:export', '#', 'admin', sysdate(), '');

-- 联系人按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4021, '联系人查询', 4020, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contact:query', '#', 'admin', sysdate(), ''),
(4022, '联系人新增', 4020, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contact:add', '#', 'admin', sysdate(), ''),
(4023, '联系人修改', 4020, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contact:edit', '#', 'admin', sysdate(), ''),
(4024, '联系人删除', 4020, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contact:remove', '#', 'admin', sysdate(), ''),
(4025, '联系人导出', 4020, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contact:export', '#', 'admin', sysdate(), '');

-- 互动记录按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4031, '记录查询', 4030, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:interaction:query', '#', 'admin', sysdate(), ''),
(4032, '记录新增', 4030, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:interaction:add', '#', 'admin', sysdate(), ''),
(4033, '记录修改', 4030, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:interaction:edit', '#', 'admin', sysdate(), ''),
(4034, '记录删除', 4030, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:interaction:remove', '#', 'admin', sysdate(), '');

-- 线索管理菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4040, '线索列表', 4002, 1, 'list', 'mk/lead/index', '', 'MkLead', 1, 0, 'C', '0', '0', 'marketing:lead:list', 'list', 'admin', sysdate(), ''),
(4050, '公海池', 4002, 2, 'pool', 'mk/lead/pool', '', 'MkLeadPool', 1, 0, 'C', '0', '0', 'marketing:lead:pool', 'pool', 'admin', sysdate(), '');

-- 线索按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4041, '线索查询', 4040, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:query', '#', 'admin', sysdate(), ''),
(4042, '线索新增', 4040, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:add', '#', 'admin', sysdate(), ''),
(4043, '线索修改', 4040, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:edit', '#', 'admin', sysdate(), ''),
(4044, '线索删除', 4040, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:remove', '#', 'admin', sysdate(), ''),
(4045, '线索导出', 4040, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:export', '#', 'admin', sysdate(), ''),
(4046, '线索领取', 4040, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:receive', '#', 'admin', sysdate(), ''),
(4047, '线索分配', 4040, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:assign', '#', 'admin', sysdate(), '');

-- 商机管理菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4060, '商机列表', 4003, 1, 'list', 'mk/opportunity/index', '', 'MkOpportunity', 1, 0, 'C', '0', '0', 'marketing:opportunity:list', 'list', 'admin', sysdate(), ''),
(4070, '商机看板', 4003, 2, 'kanban', 'mk/opportunity/kanban', '', 'MkOpportunityKanban', 1, 0, 'C', '0', '0', 'marketing:opportunity:list', 'dashboard', 'admin', sysdate(), ''),
(4075, '阶段配置', 4003, 3, 'stage', 'mk/opportunity/stage', '', 'MkOpportunityStage', 1, 0, 'C', '0', '0', 'marketing:stage:list', 'setting', 'admin', sysdate(), '');

-- 商机按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4061, '商机查询', 4060, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:opportunity:query', '#', 'admin', sysdate(), ''),
(4062, '商机新增', 4060, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:opportunity:add', '#', 'admin', sysdate(), ''),
(4063, '商机修改', 4060, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:opportunity:edit', '#', 'admin', sysdate(), ''),
(4064, '商机删除', 4060, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:opportunity:remove', '#', 'admin', sysdate(), ''),
(4065, '商机导出', 4060, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:opportunity:export', '#', 'admin', sysdate(), '');

-- 营销活动菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4080, '活动管理', 4004, 1, 'list', 'mk/activity/index', '', 'MkActivity', 1, 0, 'C', '0', '0', 'marketing:activity:list', 'list', 'admin', sysdate(), '');

-- 活动按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4081, '活动查询', 4080, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:activity:query', '#', 'admin', sysdate(), ''),
(4082, '活动新增', 4080, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:activity:add', '#', 'admin', sysdate(), ''),
(4083, '活动修改', 4080, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:activity:edit', '#', 'admin', sysdate(), ''),
(4084, '活动删除', 4080, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:activity:remove', '#', 'admin', sysdate(), ''),
(4085, '活动导出', 4080, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:activity:export', '#', 'admin', sysdate(), '');

-- 合同订单菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4090, '合同管理', 4005, 1, 'contract', 'mk/contract/index', '', 'MkContract', 1, 0, 'C', '0', '0', 'marketing:contract:list', 'list', 'admin', sysdate(), ''),
(4100, '订单管理', 4005, 2, 'order', 'mk/order/index', '', 'MkOrder', 1, 0, 'C', '0', '0', 'marketing:order:list', 'list', 'admin', sysdate(), ''),
(4110, '回款管理', 4005, 3, 'payment', 'mk/payment/index', '', 'MkPayment', 1, 0, 'C', '0', '0', 'marketing:payment:list', 'money', 'admin', sysdate(), '');

-- 合同按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4091, '合同查询', 4090, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contract:query', '#', 'admin', sysdate(), ''),
(4092, '合同新增', 4090, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contract:add', '#', 'admin', sysdate(), ''),
(4093, '合同修改', 4090, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contract:edit', '#', 'admin', sysdate(), ''),
(4094, '合同删除', 4090, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contract:remove', '#', 'admin', sysdate(), ''),
(4095, '合同导出', 4090, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contract:export', '#', 'admin', sysdate(), '');

-- 订单按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4101, '订单查询', 4100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:order:query', '#', 'admin', sysdate(), ''),
(4102, '订单新增', 4100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:order:add', '#', 'admin', sysdate(), ''),
(4103, '订单修改', 4100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:order:edit', '#', 'admin', sysdate(), ''),
(4104, '订单删除', 4100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:order:remove', '#', 'admin', sysdate(), ''),
(4105, '订单导出', 4100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:order:export', '#', 'admin', sysdate(), ''),
(4106, '订单发货', 4100, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:order:deliver', '#', 'admin', sysdate(), '');

-- 回款按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4111, '回款查询', 4110, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:payment:query', '#', 'admin', sysdate(), ''),
(4112, '回款新增', 4110, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:payment:add', '#', 'admin', sysdate(), ''),
(4113, '回款修改', 4110, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:payment:edit', '#', 'admin', sysdate(), ''),
(4114, '回款删除', 4110, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:payment:remove', '#', 'admin', sysdate(), '');

-- 数据分析菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4120, '营销看板', 4006, 1, 'dashboard', 'mk/dashboard/index', '', 'MkDashboard', 1, 0, 'C', '0', '0', 'marketing:report:view', 'chart', 'admin', sysdate(), '');


-- =============================================
-- 四、给admin角色分配所有营销菜单权限
-- =============================================

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 4000), (1, 4001), (1, 4002), (1, 4003), (1, 4004), (1, 4005), (1, 4006),
(1, 4010), (1, 4011), (1, 4012), (1, 4013), (1, 4014), (1, 4015),
(1, 4020), (1, 4021), (1, 4022), (1, 4023), (1, 4024), (1, 4025),
(1, 4030), (1, 4031), (1, 4032), (1, 4033), (1, 4034),
(1, 4040), (1, 4041), (1, 4042), (1, 4043), (1, 4044), (1, 4045), (1, 4046), (1, 4047),
(1, 4050),
(1, 4060), (1, 4061), (1, 4062), (1, 4063), (1, 4064), (1, 4065),
(1, 4070), (1, 4075),
(1, 4080), (1, 4081), (1, 4082), (1, 4083), (1, 4084), (1, 4085),
(1, 4090), (1, 4091), (1, 4092), (1, 4093), (1, 4094), (1, 4095),
(1, 4100), (1, 4101), (1, 4102), (1, 4103), (1, 4104), (1, 4105),
(1, 4110), (1, 4111), (1, 4112), (1, 4113), (1, 4114),
(1, 4120);


-- =============================================
-- 五、销售阶段基础数据
-- =============================================

INSERT INTO mk_opportunity_stage (stage_id, stage_code, stage_name, sort, win_rate, max_days, status, create_by, create_time, remark) VALUES
(1, 'stage1', '初步接触', 1, 10, 30, '0', 'admin', sysdate(), '首次接触，建立联系'),
(2, 'stage2', '需求确认', 2, 25, 30, '0', 'admin', sysdate(), '确认客户需求和预算'),
(3, 'stage3', '方案制定', 3, 40, 45, '0', 'admin', sysdate(), '制定解决方案'),
(4, 'stage4', '方案演示', 4, 55, 30, '0', 'admin', sysdate(), '方案演示和答疑'),
(5, 'stage5', '商务谈判', 5, 75, 30, '0', 'admin', sysdate(), '价格和条款谈判'),
(6, 'stage6', '合同签订', 6, 90, 15, '0', 'admin', sysdate(), '合同审批和签订');


-- =============================================
-- 六、基础测试数据（每个模块5条）
-- =============================================

-- 客户数据（5条）
INSERT INTO mk_customer (customer_id, customer_no, customer_name, credit_code, customer_level, customer_source, industry, company_size, province, city, district, address, website, customer_status, first_contact_date, cooperation_date, total_amount, user_id, dept_id, del_flag, create_by, create_time, remark) VALUES
(1, 'CUS20260719001', '北京智云科技有限公司', '91110108MA01ABC123', '1', '0', '0', '0', '北京市', '北京市', '海淀区', '中关村软件园二期5号楼', 'www.zhiyun-tech.com', '2', '2026-01-15', '2026-02-01', 580000, 2, 103, '0', 'admin', sysdate(), '关键大客户，长期合作'),
(2, 'CUS20260719002', '上海宏远制造股份有限公司', '91310115MA01DEF456', '2', '1', '1', '0', '上海市', '上海市', '浦东新区', '张江高科技园区博云路2号', 'www.hongyuan-mfg.cn', '2', '2026-02-20', '2026-03-10', 320000, 2, 103, '0', 'admin', sysdate(), '重要客户，制造业龙头'),
(3, 'CUS20260719003', '深圳创新金融服务有限公司', '91440300MA01GHI789', '2', '2', '2', '1', '广东省', '深圳市', '南山区', '科技园南区高新南一道9号', 'www.cx-fin.com', '2', '2026-03-05', '2026-04-01', 150000, 2, 103, '0', 'admin', sysdate(), '金融行业客户'),
(4, 'CUS20260719004', '杭州云端教育科技有限公司', '91330106MA01JKL012', '3', '3', '3', '1', '浙江省', '杭州市', '余杭区', '未来科技城梦想小镇', 'www.yunduan-edu.com', '0', '2026-04-10', NULL, 50000, 2, 103, '0', 'admin', sysdate(), '教育行业新客户'),
(5, 'CUS20260719005', '广州速达物流有限公司', '91440101MA01MNO345', '4', '4', '6', '2', '广东省', '广州市', '天河区', '天河软件园建中路50号', 'www.suda-log.com', '0', '2026-05-15', NULL, 0, 2, 103, '0', 'admin', sysdate(), '潜在客户，待跟进');

-- 联系人数据（5条）
INSERT INTO mk_contact (contact_id, customer_id, name, gender, position, department, role_tag, phone, email, wechat, qq, is_key, is_primary, personal_trait, del_flag, create_by, create_time, remark) VALUES
(1, 1, '张伟', '0', '总经理', '管理层', '0', '13800138001', 'zhangwei@zhiyun-tech.com', 'zhangwei_wx', '88880001', '1', '1', '注重效率和结果，偏好数据驱动的决策', '0', 'admin', sysdate(), '关键决策人'),
(2, 1, '李娜', '1', '采购经理', '采购部', '1,3', '13900139001', 'lina@zhiyun-tech.com', 'lina_wx', '88880002', '1', '0', '细致严谨，关注合同条款和交付周期', '0', 'admin', sysdate(), '采购负责人'),
(3, 2, '王强', '0', 'CTO', '技术部', '0,3', '13700137001', 'wangqiang@hongyuan-mfg.cn', 'wangqiang_wx', '88880003', '1', '1', '技术导向，关注产品技术指标', '0', 'admin', sysdate(), '技术决策人'),
(4, 3, '刘洋', '0', '运营总监', '运营部', '0,1', '13600136001', 'liuyang@cx-fin.com', 'liuyang_wx', '88880004', '1', '1', '注重ROI和投入产出比', '0', 'admin', sysdate(), '运营负责人'),
(5, 4, '陈晨', '1', '市场经理', '市场部', '2,3', '13500135001', 'chenchen@yunduan-edu.com', 'chenchen_wx', '88880005', '0', '1', '年轻有活力，喜欢创新方案', '0', 'admin', sysdate(), '市场对接人');

-- 互动记录（5条）
INSERT INTO mk_interaction (record_id, customer_id, contact_id, opportunity_id, lead_id, interact_type, interact_time, content, user_id, next_time, next_content, del_flag, create_by, create_time) VALUES
(1, 1, 1, NULL, NULL, '1', '2026-07-10 10:00:00', '拜访智云科技张总，讨论下半年的系统升级方案，客户表示预算充足，预计Q3启动项目', 2, '2026-07-25 10:00:00', '发送详细方案报价', '0', 'admin', sysdate()),
(2, 1, 2, NULL, NULL, '0', '2026-07-12 14:30:00', '电话跟进采购经理李娜，确认采购流程和审批节点，对方表示需要走季度审批', 2, '2026-07-20 14:00:00', '准备采购流程文档', '0', 'admin', sysdate()),
(3, 2, 3, NULL, NULL, '3', '2026-07-08 09:00:00', '与宏远制造王总视频会议，演示MES系统对接方案，客户反馈积极', 2, '2026-07-18 10:00:00', '安排现场POC测试', '0', 'admin', sysdate()),
(4, 3, 4, NULL, NULL, '4', '2026-07-15 11:00:00', '微信沟通刘总，发送产品白皮书和案例参考，客户对金融行业案例感兴趣', 2, '2026-07-22 15:00:00', '安排金融行业案例分享会', '0', 'admin', sysdate()),
(5, 4, 5, NULL, NULL, '2', '2026-07-16 16:00:00', '发送方案邮件给陈晨，包含SaaS教育平台功能清单和定价方案', 2, '2026-07-23 10:00:00', '电话确认邮件接收情况', '0', 'admin', sysdate());

-- 线索数据（5条）
INSERT INTO mk_lead (lead_id, lead_no, company_name, contact_name, contact_phone, contact_email, position, industry, company_size, address, lead_source, activity_id, requirement_desc, lead_score, lead_grade, lead_status, is_public, user_id, dept_id, receive_time, last_follow_time, del_flag, create_by, create_time, remark) VALUES
(1, 'LEAD20260719001', '成都新视界科技有限公司', '赵敏', '13300133001', 'zhaomin@xinshijie.com', '市场总监', '0', '1', '四川省成都市高新区', '0', NULL, '需要一套营销自动化系统，团队规模50人，预算30万', 85, 'A', '3', '0', 2, 103, '2026-07-05 09:00:00', '2026-07-15 14:00:00', '0', 'admin', sysdate(), '高质量线索，意向明确'),
(2, 'LEAD20260719002', '武汉光谷生物医药有限公司', '孙磊', '13400134001', 'sunlei@guanggu-bio.com', 'IT经理', '4', '0', '湖北省武汉市洪山区', '1', NULL, '实验室信息管理系统需求，需GMP合规', 72, 'B', '2', '0', 2, 103, '2026-07-08 10:00:00', '2026-07-14 16:00:00', '0', 'admin', sysdate(), '医药行业线索'),
(3, 'LEAD20260719003', '南京紫金山智能制造有限公司', '周婷', '13200132001', 'zhouting@zjs-mfg.com', '项目经理', '1', '0', '江苏省南京市江宁区', '2', NULL, '智能工厂改造项目，需要设备联网和数据采集平台', 68, 'B', '1', '0', 2, 103, '2026-07-10 11:00:00', '2026-07-16 09:00:00', '0', 'admin', sysdate(), '转介绍线索'),
(4, 'LEAD20260719004', '西安丝路电商有限公司', '吴昊', '13100131001', 'wuhao@silkroad-ec.com', '创始人', '0', '2', '陕西省西安市雁塔区', '3', NULL, '需要跨境电商管理系统，预算10万以内', 45, 'C', '0', '1', NULL, NULL, NULL, NULL, '0', 'admin', sysdate(), '公海线索，待领取'),
(5, 'LEAD20260719005', '青岛海蓝新能源有限公司', '郑凯', '13000130001', 'zhengkai@hailan-energy.com', '技术总监', '4', '1', '山东省青岛市崂山区', '0', NULL, '储能监控系统需求，需对接现有SCADA', 52, 'C', '0', '1', NULL, NULL, NULL, NULL, '0', 'admin', sysdate(), '公海线索，待领取');

-- 商机数据（5条）
INSERT INTO mk_opportunity (opportunity_id, opportunity_no, opportunity_name, customer_id, contact_id, lead_id, opportunity_source, expected_amount, expected_date, stage_code, stage_name, win_rate, weighted_amount, opportunity_status, competitor, pain_point, solution, lost_reason, lost_remark, actual_amount, actual_date, user_id, dept_id, next_action, next_time, stage_enter_time, del_flag, create_by, create_time, remark) VALUES
(1, 'OPP20260719001', '智云科技数字化升级项目', 1, 1, NULL, '1', 580000, '2026-09-30', 'stage4', '方案演示', 55, 319000, '0', '金蝶、用友', '现有系统老旧，数据孤岛严重，需要统一数据平台', '提供一体化数字运营平台，含数据中台+业务系统', NULL, NULL, NULL, NULL, 2, 103, '安排POC测试环境', '2026-07-25 10:00:00', '2026-07-10 10:00:00', '0', 'admin', sysdate(), '重点商机'),
(2, 'OPP20260719002', '宏远制造MES系统项目', 2, 3, NULL, '2', 320000, '2026-08-31', 'stage3', '方案制定', 40, 128000, '0', '西门子', '生产数据手工记录，效率低，质量追溯困难', 'MES系统+设备联网+看板', NULL, NULL, NULL, NULL, 2, 103, '完善技术方案', '2026-07-20 14:00:00', '2026-07-08 09:00:00', '0', 'admin', sysdate(), '制造业MES需求'),
(3, 'OPP20260719003', '创新金融风控系统项目', 3, 4, NULL, '0', 150000, '2026-10-15', 'stage2', '需求确认', 25, 37500, '0', NULL, '风控模型老化，需要AI辅助决策', 'AI风控引擎+实时预警系统', NULL, NULL, NULL, NULL, 2, 103, '确认技术需求细节', '2026-07-22 15:00:00', '2026-07-05 10:00:00', '0', 'admin', sysdate(), '金融风控需求'),
(4, 'OPP20260719004', '云端教育SaaS平台项目', 4, 5, NULL, '3', 80000, '2026-08-15', 'stage5', '商务谈判', 75, 60000, '0', NULL, '缺乏在线教学管理工具', 'SaaS教育平台年费方案', NULL, NULL, NULL, NULL, 2, 103, '发送合同草案', '2026-07-23 10:00:00', '2026-07-12 14:00:00', '0', 'admin', sysdate(), '即将成交'),
(5, 'OPP20260719005', '速达物流TMS系统项目', 5, NULL, NULL, '2', 50000, '2026-11-30', 'stage1', '初步接触', 10, 5000, '0', NULL, '运输管理依赖人工调度，效率低', 'TMS运输管理系统', NULL, NULL, NULL, NULL, 2, 103, '首次拜访客户', '2026-07-28 09:00:00', '2026-07-15 10:00:00', '0', 'admin', sysdate(), '初步接触阶段');

-- 营销活动数据（5条）
INSERT INTO mk_activity (activity_id, activity_no, activity_name, activity_type, activity_form, activity_status, start_time, end_time, location, budget, target_count, target_lead_count, user_id, dept_id, summary, content, actual_count, lead_count, mql_count, sql_count, opportunity_count, deal_amount, del_flag, create_by, create_time, remark) VALUES
(1, 'ACT20260719001', '2026数字制造线上峰会', '0', '0', '3', '2026-06-15 14:00:00', '2026-06-15 17:00:00', 'https://meeting.zhiyun-tech.com/summit', 50000, 500, 100, 2, 103, '面向制造业CIO/CTO的数字化升级线上峰会', '邀请行业专家分享数字化转型案例，展示我司MES/数字平台产品', 320, 85, 45, 22, 8, 280000, '0', 'admin', sysdate(), '效果良好，ROI 5.6'),
(2, 'ACT20260719002', 'B2B营销自动化Webinar', '0', '0', '3', '2026-06-20 19:00:00', '2026-06-20 21:00:00', 'https://meeting.zhiyun-tech.com/webinar06', 15000, 300, 60, 2, 103, '营销自动化解决方案线上研讨会', '介绍营销自动化系统核心功能及最佳实践', 180, 55, 30, 15, 5, 120000, '0', 'admin', sysdate(), 'ROI 8.0'),
(3, 'ACT20260719003', '2026工业互联网展会', '1', '1', '2', '2026-07-18 09:00:00', '2026-07-20 17:00:00', '上海新国际博览中心N1馆', 120000, 2000, 200, 2, 103, '参展2026工业互联网博览会', '展位展示产品方案，收集线索，安排现场演示', 850, 180, 80, 40, 12, 450000, '0', 'admin', sysdate(), '线下大型展会'),
(4, 'ACT20260719004', '金融科技客户沙龙', '1', '1', '1', '2026-07-25 14:00:00', '2026-07-25 17:00:00', '北京国贸大酒店3层宴会厅', 35000, 50, 30, 2, 103, '面向金融行业客户的闭门沙龙', 'AI风控、智能投顾等金融科技话题分享', 0, 0, 0, 0, 0, 0, '0', 'admin', sysdate(), '报名中'),
(5, 'ACT20260719005', '搜索引擎广告投放-Q3', '5', '0', '2', '2026-07-01 00:00:00', '2026-09-30 23:59:59', '百度/360/搜狗', 80000, 0, 150, 2, 103, 'Q3季度搜索引擎广告投放计划', '关键词：MES系统、数字平台、营销自动化等', 5000, 120, 60, 25, 8, 380000, '0', 'admin', sysdate(), '持续投放中');

-- 合同数据（5条）
INSERT INTO mk_contract (contract_id, contract_no, contract_name, contract_type, customer_id, opportunity_id, contract_amount, sign_date, effective_date, expire_date, payment_method, contract_status, user_id, dept_id, attachment, del_flag, create_by, create_time, remark) VALUES
(1, 'CON20260719001', '智云科技数字平台年度服务合同', '0', 1, 1, 580000, '2026-03-01', '2026-03-01', '2027-02-28', '1', '2', 2, 103, '/upload/contract/con001.pdf', '0', 'admin', sysdate(), '分期付款，季度回款'),
(2, 'CON20260719002', '宏远制造MES系统实施合同', '0', 2, 2, 320000, '2026-03-15', '2026-04-01', '2027-03-31', '1', '2', 2, 103, '/upload/contract/con002.pdf', '0', 'admin', sysdate(), '分期付款，含实施服务费'),
(3, 'CON20260719003', '创新金融风控系统合同', '0', 3, 3, 150000, '2026-04-10', '2026-04-15', '2027-04-14', '0', '2', 2, 103, '/upload/contract/con003.pdf', '0', 'admin', sysdate(), '一次性付款'),
(4, 'CON20260719004', '云端教育SaaS平台年度合同', '0', 4, 4, 80000, '2026-05-20', '2026-06-01', '2027-05-31', '0', '2', 2, 103, NULL, '0', 'admin', sysdate(), '年费制'),
(5, 'CON20260719005', '智云科技系统增购合同', '2', 1, NULL, 120000, '2026-06-15', '2026-07-01', '2027-06-30', '0', '2', 2, 103, NULL, '0', 'admin', sysdate(), '增购AI分析模块');

-- 订单数据（5条）
INSERT INTO mk_order (order_id, order_no, contract_id, customer_id, order_amount, order_status, logistics_company, tracking_no, deliver_time, finish_time, user_id, dept_id, del_flag, create_by, create_time, remark) VALUES
(1, 'ORD20260719001', 1, 1, 145000, '2', NULL, NULL, NULL, '2026-06-01 00:00:00', 2, 103, '0', 'admin', sysdate(), 'Q1回款订单'),
(2, 'ORD20260719002', 1, 1, 145000, '2', NULL, NULL, NULL, '2026-06-01 00:00:00', 2, 103, '0', 'admin', sysdate(), 'Q2回款订单'),
(3, 'ORD20260719003', 2, 2, 160000, '1', '顺丰速运', 'SF1234567890', '2026-05-20 10:00:00', NULL, 2, 103, '0', 'admin', sysdate(), '首期实施费'),
(4, 'ORD20260719004', 3, 3, 150000, '2', NULL, NULL, NULL, '2026-05-01 00:00:00', 2, 103, '0', 'admin', sysdate(), '一次性付款订单'),
(5, 'ORD20260719005', 5, 1, 120000, '0', NULL, NULL, NULL, NULL, 2, 103, '0', 'admin', sysdate(), '增购订单待发货');

-- 订单明细数据
INSERT INTO mk_order_item (item_id, order_id, product_name, product_spec, unit, quantity, unit_price, subtotal, create_time, remark) VALUES
(1, 1, '数字平台基础版', 'V3.0', '套', 1, 100000, 100000, sysdate(), '基础平台授权'),
(2, 1, '数据中台模块', 'V3.0', '套', 1, 45000, 45000, sysdate(), '数据中台授权'),
(3, 2, '数字平台基础版', 'V3.0', '套', 1, 100000, 100000, sysdate(), 'Q2续费'),
(4, 2, 'AI分析模块', 'V3.0', '套', 1, 45000, 45000, sysdate(), 'AI模块授权'),
(5, 3, 'MES系统核心模块', 'V2.0', '套', 1, 120000, 120000, sysdate(), 'MES核心授权'),
(6, 3, '设备联网网关', 'GW-200', '台', 8, 5000, 40000, sysdate(), '设备联网'),
(7, 4, 'AI风控引擎', 'V1.0', '套', 1, 150000, 150000, sysdate(), '风控引擎授权'),
(8, 5, 'AI分析模块', 'V3.0', '套', 1, 80000, 80000, sysdate(), '增购AI模块'),
(9, 5, '数据可视化插件', 'V3.0', '套', 2, 20000, 40000, sysdate(), '可视化插件');

-- 回款计划数据（5条）
INSERT INTO mk_payment_plan (plan_id, contract_id, customer_id, period_no, plan_amount, plan_date, actual_amount, actual_date, payment_status, create_by, create_time, remark) VALUES
(1, 1, 1, 1, 145000, '2026-03-01', 145000, '2026-03-05', '1', 'admin', sysdate(), '首期款已回款'),
(2, 1, 1, 2, 145000, '2026-06-01', 145000, '2026-06-03', '1', 'admin', sysdate(), '二期款已回款'),
(3, 1, 1, 3, 145000, '2026-09-01', 0, NULL, '0', 'admin', sysdate(), '三期款待回款'),
(4, 1, 1, 4, 145000, '2026-12-01', 0, NULL, '0', 'admin', sysdate(), '尾款待回款'),
(5, 2, 2, 1, 160000, '2026-04-01', 160000, '2026-04-10', '1', 'admin', sysdate(), '首期款已回款');

-- 客户标签数据（5条）
INSERT INTO mk_customer_tag (tag_id, tag_name, tag_type, tag_color, sort, status, create_by, create_time, remark) VALUES
(1, '高价值', '0', '#f56c6c', 1, '0', 'admin', sysdate(), '高价值客户标签'),
(2, '制造业', '0', '#409eff', 2, '0', 'admin', sysdate(), '制造业客户标签'),
(3, '金融', '0', '#67c23a', 3, '0', 'admin', sysdate(), '金融行业标签'),
(4, '待激活', '0', '#e6a23c', 4, '0', 'admin', sysdate(), '待激活客户标签'),
(5, '续约中', '0', '#909399', 5, '0', 'admin', sysdate(), '续约中客户标签');

-- =============================================
-- BID 招投标管理子系统 - 全量SQL脚本
-- 包含：10张业务表 DDL + 字典类型/数据 + 菜单权限 + 编号规则
-- 依据：design/bid-招投标管理模块开发方案.md
-- =============================================

-- =============================================
-- 一、业务表 DDL
-- =============================================

-- 1. 采集源配置表
DROP TABLE IF EXISTS bid_crawl_source;
CREATE TABLE bid_crawl_source (
    source_id       BIGINT        NOT NULL AUTO_INCREMENT COMMENT '采集源ID',
    source_name     VARCHAR(100)  NOT NULL COMMENT '采集源名称',
    source_type     VARCHAR(10)   DEFAULT '1' COMMENT '采集源类型（字典 bid_source_type）',
    base_url        VARCHAR(500)  NOT NULL COMMENT '列表页/接口地址',
    parser_code     VARCHAR(50)   NOT NULL COMMENT '解析器标识（common-html/common-json/rss）',
    parse_config    TEXT          COMMENT '解析配置JSON（列表选择器、字段映射、翻页规则、请求头、参数模板）',
    crawl_interval  INT           DEFAULT 120 COMMENT '抓取间隔（分钟），最小30',
    enabled         CHAR(1)       DEFAULT '1' COMMENT '启用状态（字典 sys_normal_disable）',
    last_run_time   DATETIME      COMMENT '最近执行时间',
    last_run_status VARCHAR(10)   COMMENT '最近执行结果（字典 bid_crawl_status）',
    last_run_msg    VARCHAR(500)  COMMENT '最近执行说明',
    del_flag        CHAR(1)       DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
    create_by       VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time     DATETIME      COMMENT '创建时间',
    update_by       VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time     DATETIME      COMMENT '更新时间',
    remark          VARCHAR(500)  COMMENT '备注',
    PRIMARY KEY (source_id),
    KEY idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标-采集源配置表';

-- 2. 关键词订阅配置表
DROP TABLE IF EXISTS bid_crawl_keyword;
CREATE TABLE bid_crawl_keyword (
    keyword_id      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '关键词ID',
    keyword         VARCHAR(100)  NOT NULL COMMENT '关键词（主匹配词）',
    synonym_words   VARCHAR(500)  COMMENT '同义词（逗号分隔）',
    exclude_words   VARCHAR(500)  COMMENT '排除词（逗号分隔）',
    match_type      VARCHAR(10)   DEFAULT '1' COMMENT '匹配方式（字典 bid_match_type）',
    region_code     VARCHAR(20)   COMMENT '限定地区（行政区划码，空=不限）',
    industry        VARCHAR(10)   COMMENT '行业分类（字典 bid_industry，空=不限）',
    min_amount      DECIMAL(14,2) COMMENT '金额下限（元，空=不限）',
    notice_types    VARCHAR(100)  COMMENT '限定信息类型（逗号分隔bid_notice_type值，空=不限）',
    notify_enabled  CHAR(1)       DEFAULT '1' COMMENT '命中是否推送（1是 0否）',
    enabled         CHAR(1)       DEFAULT '1' COMMENT '启用状态（字典 sys_normal_disable）',
    del_flag        CHAR(1)       DEFAULT '0' COMMENT '删除标志',
    create_by       VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time     DATETIME      COMMENT '创建时间',
    update_by       VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time     DATETIME      COMMENT '更新时间',
    remark          VARCHAR(500)  COMMENT '备注',
    PRIMARY KEY (keyword_id),
    KEY idx_keyword (keyword(20)),
    KEY idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标-关键词订阅配置表';

-- 3. 订阅范围表
DROP TABLE IF EXISTS bid_keyword_scope;
CREATE TABLE bid_keyword_scope (
    scope_id        BIGINT        NOT NULL AUTO_INCREMENT COMMENT '范围ID',
    keyword_id      BIGINT        NOT NULL COMMENT '关键词ID',
    scope_type      CHAR(1)       NOT NULL COMMENT '范围类型（1按部门 2按用户）',
    dept_id         BIGINT        COMMENT '部门ID',
    user_id         BIGINT        COMMENT '用户ID',
    PRIMARY KEY (scope_id),
    KEY idx_keyword (keyword_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标-关键词订阅范围表';

-- 4. 招标公告库
DROP TABLE IF EXISTS bid_notice;
CREATE TABLE bid_notice (
    notice_id        BIGINT        NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    project_key      VARCHAR(100)  COMMENT '项目关联键（招标编号，同项目多公告串联）',
    title            VARCHAR(500)  NOT NULL COMMENT '公告标题',
    notice_type      VARCHAR(10)   DEFAULT '2' COMMENT '信息类型（字典 bid_notice_type）',
    source_id        BIGINT        COMMENT '采集源ID（手动录入为空）',
    source_name      VARCHAR(100)  COMMENT '来源站点名称（冗余）',
    tenderee         VARCHAR(200)  COMMENT '招标人/采购人',
    agent            VARCHAR(200)  COMMENT '代理机构',
    region_code      VARCHAR(20)   COMMENT '地区（行政区划码）',
    industry         VARCHAR(10)   COMMENT '行业分类（字典 bid_industry）',
    budget_amount    DECIMAL(14,2) COMMENT '预算/估算金额（元）',
    publish_time     DATETIME      COMMENT '公告发布时间',
    deadline_time    DATETIME      COMMENT '报名/递交截止时间',
    open_time        DATETIME      COMMENT '开标时间',
    contact_person   VARCHAR(64)   COMMENT '联系人',
    contact_phone    VARCHAR(32)   COMMENT '联系电话',
    source_url       VARCHAR(500)  COMMENT '原文链接',
    content          TEXT          COMMENT '公告正文（纯文本摘要）',
    attachment_url   VARCHAR(1000) COMMENT '招标附件（逗号分隔文件URL）',
    matched_keywords VARCHAR(500)  COMMENT '命中关键词（逗号分隔）',
    ai_extracted     CHAR(1)       DEFAULT '0' COMMENT '是否已AI抽取（0否 1是）',
    input_type       CHAR(1)       DEFAULT '0' COMMENT '录入方式（0手动 1采集）',
    del_flag         CHAR(1)       DEFAULT '0' COMMENT '删除标志',
    create_by        VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time      DATETIME      COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time      DATETIME      COMMENT '更新时间',
    remark           VARCHAR(500)  COMMENT '备注',
    PRIMARY KEY (notice_id),
    UNIQUE KEY uk_source_url (source_url(300)),
    KEY idx_project_key (project_key),
    KEY idx_notice_type (notice_type),
    KEY idx_region (region_code),
    KEY idx_deadline (deadline_time),
    KEY idx_publish (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标-招标公告库';

-- 5. 抓取执行日志表
DROP TABLE IF EXISTS bid_crawl_log;
CREATE TABLE bid_crawl_log (
    log_id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    source_id       BIGINT        NOT NULL COMMENT '采集源ID',
    source_name     VARCHAR(100)  COMMENT '采集源名称（冗余）',
    run_status      VARCHAR(10)   NOT NULL COMMENT '执行结果（字典 bid_crawl_status）',
    fetched_count   INT           DEFAULT 0 COMMENT '抓取条数',
    inserted_count  INT           DEFAULT 0 COMMENT '新增入库条数',
    matched_count   INT           DEFAULT 0 COMMENT '命中订阅条数',
    error_msg       VARCHAR(1000) COMMENT '错误信息',
    start_time      DATETIME      COMMENT '开始时间',
    end_time        DATETIME      COMMENT '结束时间',
    PRIMARY KEY (log_id),
    KEY idx_source (source_id),
    KEY idx_start (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标-抓取执行日志表';

-- 6. 投标项目台账表
DROP TABLE IF EXISTS bid_project;
CREATE TABLE bid_project (
    project_id      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '投标项目ID',
    bid_no          VARCHAR(32)   NOT NULL COMMENT '投标编号（TB+日期+序号）',
    notice_id       BIGINT        COMMENT '关联公告ID（商机来源）',
    project_name    VARCHAR(300)  NOT NULL COMMENT '投标项目名称',
    tenderee        VARCHAR(200)  COMMENT '招标人/采购人（冗余）',
    agent           VARCHAR(200)  COMMENT '代理机构',
    region_code     VARCHAR(20)   COMMENT '地区（行政区划码）',
    industry        VARCHAR(10)   COMMENT '行业分类（字典 bid_industry）',
    budget_amount   DECIMAL(14,2) COMMENT '项目预算（元）',
    bid_amount      DECIMAL(14,2) COMMENT '我方报价（元）',
    status          VARCHAR(10)   DEFAULT '0' COMMENT '投标状态（字典 bid_project_status）',
    join_type       VARCHAR(10)   DEFAULT '1' COMMENT '承接方式（字典 bid_join_type）',
    partner         VARCHAR(200)  COMMENT '联营合作方（联营时）',
    apply_deadline  DATETIME      COMMENT '报名截止时间',
    submit_deadline DATETIME      COMMENT '递交截止时间',
    open_time       DATETIME      COMMENT '开标时间',
    result_time     DATETIME      COMMENT '结果公示时间',
    fail_reason     VARCHAR(10)   COMMENT '未中标/废标原因（字典 bid_fail_reason）',
    owner_user_id   BIGINT        COMMENT '投标负责人ID',
    owner_name      VARCHAR(64)   COMMENT '负责人姓名（冗余）',
    dept_id         BIGINT        COMMENT '归属部门ID',
    del_flag        CHAR(1)       DEFAULT '0' COMMENT '删除标志',
    create_by       VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time     DATETIME      COMMENT '创建时间',
    update_by       VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time     DATETIME      COMMENT '更新时间',
    remark          VARCHAR(500)  COMMENT '备注',
    PRIMARY KEY (project_id),
    UNIQUE KEY uk_bid_no (bid_no),
    KEY idx_status (status),
    KEY idx_owner (owner_user_id),
    KEY idx_dept (dept_id),
    KEY idx_notice (notice_id),
    KEY idx_submit_deadline (submit_deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标-投标项目台账表';

-- 7. 投标进度节点表
DROP TABLE IF EXISTS bid_project_node;
CREATE TABLE bid_project_node (
    node_id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '节点ID',
    project_id      BIGINT        NOT NULL COMMENT '投标项目ID',
    node_type       VARCHAR(10)   NOT NULL COMMENT '节点类型（字典 bid_node_type）',
    node_name       VARCHAR(100)  COMMENT '节点名称（默认取字典label，可改）',
    plan_time       DATETIME      COMMENT '计划完成时间',
    actual_time     DATETIME      COMMENT '实际完成时间',
    handler_id      BIGINT        COMMENT '经办人ID',
    handler_name    VARCHAR(64)   COMMENT '经办人（冗余）',
    block_desc      VARCHAR(500)  COMMENT '卡点/问题说明',
    finish_flag     CHAR(1)       DEFAULT '0' COMMENT '是否完成（0未完成 1已完成）',
    sort_order      INT           DEFAULT 0 COMMENT '顺序',
    del_flag        CHAR(1)       DEFAULT '0' COMMENT '删除标志',
    create_by       VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time     DATETIME      COMMENT '创建时间',
    update_by       VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time     DATETIME      COMMENT '更新时间',
    remark          VARCHAR(500)  COMMENT '备注',
    PRIMARY KEY (node_id),
    KEY idx_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标-投标进度节点表';

-- 8. 保证金台账表
DROP TABLE IF EXISTS bid_deposit;
CREATE TABLE bid_deposit (
    deposit_id       BIGINT        NOT NULL AUTO_INCREMENT COMMENT '保证金ID',
    project_id       BIGINT        NOT NULL COMMENT '投标项目ID',
    bid_no           VARCHAR(32)   COMMENT '投标编号（冗余）',
    project_name     VARCHAR(300)  COMMENT '项目名称（冗余）',
    deposit_type     VARCHAR(10)   DEFAULT '1' COMMENT '保证金类型（字典 bid_deposit_type）',
    pay_type         VARCHAR(10)   DEFAULT '1' COMMENT '缴纳形式（字典 bid_pay_type）',
    amount           DECIMAL(14,2) NOT NULL COMMENT '缴纳金额（元）',
    pay_time         DATETIME      COMMENT '缴纳时间',
    expect_refund_time DATETIME    COMMENT '应退回时间',
    actual_refund_time DATETIME    COMMENT '实际退回时间',
    refund_status    VARCHAR(10)   DEFAULT '0' COMMENT '退回状态（字典 bid_refund_status）',
    voucher_url      VARCHAR(500)  COMMENT '缴纳凭证附件',
    dept_id          BIGINT        COMMENT '归属部门ID',
    del_flag         CHAR(1)       DEFAULT '0' COMMENT '删除标志',
    create_by        VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time      DATETIME      COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time      DATETIME      COMMENT '更新时间',
    remark           VARCHAR(500)  COMMENT '备注',
    PRIMARY KEY (deposit_id),
    KEY idx_project (project_id),
    KEY idx_refund_status (refund_status),
    KEY idx_dept (dept_id),
    KEY idx_expect_refund (expect_refund_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标-保证金台账表';

-- 9. 资质资料库表
DROP TABLE IF EXISTS bid_qualification;
CREATE TABLE bid_qualification (
    qualification_id BIGINT       NOT NULL AUTO_INCREMENT COMMENT '资质ID',
    qual_type        VARCHAR(10)  NOT NULL COMMENT '资料类型（字典 bid_qual_type）',
    name             VARCHAR(200) NOT NULL COMMENT '资质/证书/业绩名称',
    cert_no          VARCHAR(100) COMMENT '证书编号',
    qual_level       VARCHAR(50)  COMMENT '资质等级',
    owner_type       CHAR(1)      DEFAULT '1' COMMENT '归属（1企业 2人员）',
    owner_user_id    BIGINT       COMMENT '持有人ID（人员证书时）',
    owner_name       VARCHAR(64)  COMMENT '持有人姓名（冗余）',
    issue_date       DATE         COMMENT '发证日期',
    expire_date      DATE         COMMENT '有效期至（空=长期有效）',
    warn_days        INT          DEFAULT 60 COMMENT '到期提前预警天数',
    file_url         VARCHAR(500) COMMENT '证书扫描件',
    dept_id          BIGINT       COMMENT '归属部门ID',
    del_flag         CHAR(1)      DEFAULT '0' COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time      DATETIME     COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time      DATETIME     COMMENT '更新时间',
    remark           VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (qualification_id),
    KEY idx_qual_type (qual_type),
    KEY idx_expire (expire_date),
    KEY idx_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标-资质资料库表';

-- =============================================
-- 二、字典数据（WHERE NOT EXISTS 防重）
-- =============================================

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '招投标-采集源类型', 'bid_source_type', '0', 'admin', sysdate(), '采集源类型列表' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='bid_source_type');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '招投标-抓取执行结果', 'bid_crawl_status', '0', 'admin', sysdate(), '抓取执行结果列表' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='bid_crawl_status');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '招投标-公告信息类型', 'bid_notice_type', '0', 'admin', sysdate(), '招标公告信息类型列表' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='bid_notice_type');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '招投标-关键词匹配方式', 'bid_match_type', '0', 'admin', sysdate(), '关键词匹配方式列表' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='bid_match_type');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '招投标-行业分类', 'bid_industry', '0', 'admin', sysdate(), '工程行业分类列表' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='bid_industry');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '招投标-投标项目状态', 'bid_project_status', '0', 'admin', sysdate(), '投标项目状态列表' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='bid_project_status');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '招投标-承接方式', 'bid_join_type', '0', 'admin', sysdate(), '投标承接方式列表' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='bid_join_type');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '招投标-未中标原因', 'bid_fail_reason', '0', 'admin', sysdate(), '未中标/废标原因列表' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='bid_fail_reason');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '招投标-投标节点类型', 'bid_node_type', '0', 'admin', sysdate(), '投标进度节点类型列表' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='bid_node_type');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '招投标-资料类型', 'bid_qual_type', '0', 'admin', sysdate(), '资质资料类型列表' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='bid_qual_type');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '招投标-保证金类型', 'bid_deposit_type', '0', 'admin', sysdate(), '保证金类型列表' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='bid_deposit_type');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '招投标-缴纳形式', 'bid_pay_type', '0', 'admin', sysdate(), '保证金缴纳形式列表' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='bid_pay_type');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '招投标-退回状态', 'bid_refund_status', '0', 'admin', sysdate(), '保证金退回状态列表' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='bid_refund_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
SELECT 1 AS dict_sort,'HTML页面' AS dict_label,'1' AS dict_value,'bid_source_type' AS dict_type,'' AS css_class,'primary' AS list_class,'Y' AS is_default,'0' AS status,'admin' AS create_by,sysdate() AS create_time,'' AS remark
UNION ALL SELECT 2,'数据接口','2','bid_source_type','','primary','N','0','admin',sysdate(),''
UNION ALL SELECT 3,'RSS订阅','3','bid_source_type','','primary','N','0','admin',sysdate(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='bid_source_type' AND dict_value=t.dict_value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
SELECT 1 AS dict_sort,'成功' AS dict_label,'0' AS dict_value,'bid_crawl_status' AS dict_type,'' AS css_class,'success' AS list_class,'Y' AS is_default,'0' AS status,'admin' AS create_by,sysdate() AS create_time,'' AS remark
UNION ALL SELECT 2,'部分失败','1','bid_crawl_status','','warning','N','0','admin',sysdate(),''
UNION ALL SELECT 3,'失败','2','bid_crawl_status','','danger','N','0','admin',sysdate(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='bid_crawl_status' AND dict_value=t.dict_value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
SELECT 1 AS dict_sort,'招标预告' AS dict_label,'1' AS dict_value,'bid_notice_type' AS dict_type,'' AS css_class,'info' AS list_class,'N' AS is_default,'0' AS status,'admin' AS create_by,sysdate() AS create_time,'' AS remark
UNION ALL SELECT 2,'招标公告','2','bid_notice_type','','primary','Y','0','admin',sysdate(),''
UNION ALL SELECT 3,'变更公告','3','bid_notice_type','','warning','N','0','admin',sysdate(),''
UNION ALL SELECT 4,'答疑澄清','4','bid_notice_type','','warning','N','0','admin',sysdate(),''
UNION ALL SELECT 5,'中标候选人公示','5','bid_notice_type','','success','N','0','admin',sysdate(),''
UNION ALL SELECT 6,'中标结果公示','6','bid_notice_type','','success','N','0','admin',sysdate(),''
UNION ALL SELECT 7,'废标流标公告','7','bid_notice_type','','danger','N','0','admin',sysdate(),''
UNION ALL SELECT 8,'合同公告','8','bid_notice_type','','info','N','0','admin',sysdate(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='bid_notice_type' AND dict_value=t.dict_value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
SELECT 1 AS dict_sort,'标题匹配' AS dict_label,'1' AS dict_value,'bid_match_type' AS dict_type,'' AS css_class,'primary' AS list_class,'Y' AS is_default,'0' AS status,'admin' AS create_by,sysdate() AS create_time,'' AS remark
UNION ALL SELECT 2,'标题+正文匹配','2','bid_match_type','','primary','N','0','admin',sysdate(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='bid_match_type' AND dict_value=t.dict_value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
SELECT 1 AS dict_sort,'建筑工程' AS dict_label,'1' AS dict_value,'bid_industry' AS dict_type,'' AS css_class,'violet' AS list_class,'N' AS is_default,'0' AS status,'admin' AS create_by,sysdate() AS create_time,'' AS remark
UNION ALL SELECT 2,'市政工程','2','bid_industry','','violet','N','0','admin',sysdate(),''
UNION ALL SELECT 3,'水利水电','3','bid_industry','','violet','N','0','admin',sysdate(),''
UNION ALL SELECT 4,'装修装饰','4','bid_industry','','violet','N','0','admin',sysdate(),''
UNION ALL SELECT 5,'机电安装','5','bid_industry','','violet','N','0','admin',sysdate(),''
UNION ALL SELECT 6,'园林绿化','6','bid_industry','','violet','N','0','admin',sysdate(),''
UNION ALL SELECT 7,'其他','7','bid_industry','','gray','N','0','admin',sysdate(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='bid_industry' AND dict_value=t.dict_value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
SELECT 1 AS dict_sort,'草稿' AS dict_label,'0' AS dict_value,'bid_project_status' AS dict_type,'' AS css_class,'info' AS list_class,'Y' AS is_default,'0' AS status,'admin' AS create_by,sysdate() AS create_time,'' AS remark
UNION ALL SELECT 2,'线索确认','1','bid_project_status','','info','N','0','admin',sysdate(),''
UNION ALL SELECT 3,'已报名','2','bid_project_status','','warning','N','0','admin',sysdate(),''
UNION ALL SELECT 4,'已购标','3','bid_project_status','','warning','N','0','admin',sysdate(),''
UNION ALL SELECT 5,'标书编制中','4','bid_project_status','','warning','N','0','admin',sysdate(),''
UNION ALL SELECT 6,'已递交','5','bid_project_status','','primary','N','0','admin',sysdate(),''
UNION ALL SELECT 7,'已开标','6','bid_project_status','','primary','N','0','admin',sysdate(),''
UNION ALL SELECT 8,'中标','7','bid_project_status','','success','N','0','admin',sysdate(),''
UNION ALL SELECT 9,'未中标','8','bid_project_status','','info','N','0','admin',sysdate(),''
UNION ALL SELECT 10,'废标/流标','9','bid_project_status','','danger','N','0','admin',sysdate(),''
UNION ALL SELECT 11,'已放弃','10','bid_project_status','','danger','N','0','admin',sysdate(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='bid_project_status' AND dict_value=t.dict_value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
SELECT 1 AS dict_sort,'自营' AS dict_label,'1' AS dict_value,'bid_join_type' AS dict_type,'' AS css_class,'primary' AS list_class,'Y' AS is_default,'0' AS status,'admin' AS create_by,sysdate() AS create_time,'' AS remark
UNION ALL SELECT 2,'联营','2','bid_join_type','','warning','N','0','admin',sysdate(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='bid_join_type' AND dict_value=t.dict_value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
SELECT 1 AS dict_sort,'报价偏高' AS dict_label,'1' AS dict_value,'bid_fail_reason' AS dict_type,'' AS css_class,'danger' AS list_class,'N' AS is_default,'0' AS status,'admin' AS create_by,sysdate() AS create_time,'' AS remark
UNION ALL SELECT 2,'资质不符','2','bid_fail_reason','','danger','N','0','admin',sysdate(),''
UNION ALL SELECT 3,'技术方案失分','3','bid_fail_reason','','danger','N','0','admin',sysdate(),''
UNION ALL SELECT 4,'对手竞争','4','bid_fail_reason','','danger','N','0','admin',sysdate(),''
UNION ALL SELECT 5,'废标（资格性）','5','bid_fail_reason','','danger','N','0','admin',sysdate(),''
UNION ALL SELECT 6,'流标','6','bid_fail_reason','','danger','N','0','admin',sysdate(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='bid_fail_reason' AND dict_value=t.dict_value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
SELECT 1 AS dict_sort,'报名' AS dict_label,'1' AS dict_value,'bid_node_type' AS dict_type,'' AS css_class,'primary' AS list_class,'N' AS is_default,'0' AS status,'admin' AS create_by,sysdate() AS create_time,'' AS remark
UNION ALL SELECT 2,'购标','2','bid_node_type','','primary','N','0','admin',sysdate(),''
UNION ALL SELECT 3,'踏勘','3','bid_node_type','','primary','N','0','admin',sysdate(),''
UNION ALL SELECT 4,'标书编制','4','bid_node_type','','primary','N','0','admin',sysdate(),''
UNION ALL SELECT 5,'评审','5','bid_node_type','','primary','N','0','admin',sysdate(),''
UNION ALL SELECT 6,'递交','6','bid_node_type','','primary','N','0','admin',sysdate(),''
UNION ALL SELECT 7,'开标','7','bid_node_type','','primary','N','0','admin',sysdate(),''
UNION ALL SELECT 8,'结果确认','8','bid_node_type','','primary','N','0','admin',sysdate(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='bid_node_type' AND dict_value=t.dict_value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
SELECT 1 AS dict_sort,'企业资质' AS dict_label,'1' AS dict_value,'bid_qual_type' AS dict_type,'' AS css_class,'violet' AS list_class,'Y' AS is_default,'0' AS status,'admin' AS create_by,sysdate() AS create_time,'' AS remark
UNION ALL SELECT 2,'人员证书','2','bid_qual_type','','blue','N','0','admin',sysdate(),''
UNION ALL SELECT 3,'工程业绩','3','bid_qual_type','','green','N','0','admin',sysdate(),''
UNION ALL SELECT 4,'获奖荣誉','4','bid_qual_type','','amber','N','0','admin',sysdate(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='bid_qual_type' AND dict_value=t.dict_value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
SELECT 1 AS dict_sort,'投标保证金' AS dict_label,'1' AS dict_value,'bid_deposit_type' AS dict_type,'' AS css_class,'primary' AS list_class,'Y' AS is_default,'0' AS status,'admin' AS create_by,sysdate() AS create_time,'' AS remark
UNION ALL SELECT 2,'履约保证金','2','bid_deposit_type','','primary','N','0','admin',sysdate(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='bid_deposit_type' AND dict_value=t.dict_value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
SELECT 1 AS dict_sort,'电汇' AS dict_label,'1' AS dict_value,'bid_pay_type' AS dict_type,'' AS css_class,'primary' AS list_class,'Y' AS is_default,'0' AS status,'admin' AS create_by,sysdate() AS create_time,'' AS remark
UNION ALL SELECT 2,'保函','2','bid_pay_type','','primary','N','0','admin',sysdate(),''
UNION ALL SELECT 3,'电子保函','3','bid_pay_type','','primary','N','0','admin',sysdate(),''
UNION ALL SELECT 4,'支票','4','bid_pay_type','','primary','N','0','admin',sysdate(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='bid_pay_type' AND dict_value=t.dict_value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
SELECT 1 AS dict_sort,'在外' AS dict_label,'0' AS dict_value,'bid_refund_status' AS dict_type,'' AS css_class,'warning' AS list_class,'Y' AS is_default,'0' AS status,'admin' AS create_by,sysdate() AS create_time,'' AS remark
UNION ALL SELECT 2,'已退回','1','bid_refund_status','','success','N','0','admin',sysdate(),''
UNION ALL SELECT 3,'部分退回','2','bid_refund_status','','primary','N','0','admin',sysdate(),''
UNION ALL SELECT 4,'没收','3','bid_refund_status','','danger','N','0','admin',sysdate(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='bid_refund_status' AND dict_value=t.dict_value);

-- =============================================
-- 三、编号规则
-- =============================================
INSERT INTO mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, status, create_by, create_time, remark)
SELECT 'bid_project', 'bid', '投标编号', 'TB', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '投标项目编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='bid_project');

-- =============================================
-- 四、菜单权限
-- =============================================

-- 一级菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, icon, create_by, create_time)
SELECT '招投标管理', 0, 60, 'bid', NULL, '', 1, 0, 'M', '0', '0', 'form', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='招投标管理' AND parent_id=0);
SET @parentId = (SELECT menu_id FROM sys_menu WHERE menu_name='招投标管理' AND parent_id=0 LIMIT 1);
UPDATE sys_menu SET icon='form' WHERE menu_name='招投标管理' AND parent_id=0 AND icon='money';

-- 二级菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '招标公告库', @parentId, 1, 'notice', 'bid/notice/index', 'BidNotice', 1, 0, 'C', '0', '0', 'bid:notice:list', 'documentation', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='招标公告库' AND parent_id=@parentId);
SET @mNotice = (SELECT menu_id FROM sys_menu WHERE menu_name='招标公告库' AND parent_id=@parentId LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '投标项目台账', @parentId, 2, 'project', 'bid/project/index', 'BidProject', 1, 0, 'C', '0', '0', 'bid:project:list', 'job', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='投标项目台账' AND parent_id=@parentId);
SET @mProject = (SELECT menu_id FROM sys_menu WHERE menu_name='投标项目台账' AND parent_id=@parentId LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '保证金台账', @parentId, 3, 'deposit', 'bid/deposit/index', 'BidDeposit', 1, 0, 'C', '0', '0', 'bid:deposit:list', 'money', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='保证金台账' AND parent_id=@parentId);
SET @mDeposit = (SELECT menu_id FROM sys_menu WHERE menu_name='保证金台账' AND parent_id=@parentId LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '资质资料库', @parentId, 4, 'qualification', 'bid/qualification/index', 'BidQualification', 1, 0, 'C', '0', '0', 'bid:qualification:list', 'form', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='资质资料库' AND parent_id=@parentId);
SET @mQual = (SELECT menu_id FROM sys_menu WHERE menu_name='资质资料库' AND parent_id=@parentId LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '关键词订阅', @parentId, 5, 'keyword', 'bid/keyword/index', 'BidKeyword', 1, 0, 'C', '0', '0', 'bid:keyword:list', 'star', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='关键词订阅' AND parent_id=@parentId);
SET @mKeyword = (SELECT menu_id FROM sys_menu WHERE menu_name='关键词订阅' AND parent_id=@parentId LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '采集源管理', @parentId, 6, 'source', 'bid/source/index', 'BidSource', 1, 0, 'C', '0', '0', 'bid:source:list', 'build', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='采集源管理' AND parent_id=@parentId);
SET @mSource = (SELECT menu_id FROM sys_menu WHERE menu_name='采集源管理' AND parent_id=@parentId LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '抓取日志', @parentId, 7, 'crawlLog', 'bid/crawlLog/index', 'BidCrawlLog', 1, 0, 'C', '0', '0', 'bid:crawlLog:list', 'log', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='抓取日志' AND parent_id=@parentId);
SET @mLog = (SELECT menu_id FROM sys_menu WHERE menu_name='抓取日志' AND parent_id=@parentId LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '投标看板', @parentId, 8, 'dashboard', 'bid/dashboard/index', 'BidDashboard', 1, 0, 'C', '0', '0', 'bid:dashboard:view', 'chart', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='投标看板' AND parent_id=@parentId);

-- 按钮权限：招标公告库
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT * FROM (
SELECT '公告查询' AS menu_name, @mNotice AS parent_id, 1 AS order_num, '' AS path, 'F' AS menu_type, '0' AS visible, '0' AS status, 'bid:notice:query' AS perms, 'admin' AS create_by, sysdate() AS create_time
UNION ALL SELECT '公告新增', @mNotice, 2, '', 'F', '0', '0', 'bid:notice:add', 'admin', sysdate()
UNION ALL SELECT '公告修改', @mNotice, 3, '', 'F', '0', '0', 'bid:notice:edit', 'admin', sysdate()
UNION ALL SELECT '公告删除', @mNotice, 4, '', 'F', '0', '0', 'bid:notice:remove', 'admin', sysdate()
UNION ALL SELECT '公告导出', @mNotice, 5, '', 'F', '0', '0', 'bid:notice:export', 'admin', sysdate()
UNION ALL SELECT '转投标项目', @mNotice, 6, '', 'F', '0', '0', 'bid:notice:edit', 'admin', sysdate()
UNION ALL SELECT 'AI抽取', @mNotice, 7, '', 'F', '0', '0', 'bid:notice:edit', 'admin', sysdate()
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms=t.perms AND menu_name=t.menu_name AND parent_id=@mNotice);

-- 按钮权限：投标项目台账
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT * FROM (
SELECT '项目查询' AS menu_name, @mProject AS parent_id, 1 AS order_num, '' AS path, 'F' AS menu_type, '0' AS visible, '0' AS status, 'bid:project:query' AS perms, 'admin' AS create_by, sysdate() AS create_time
UNION ALL SELECT '项目新增', @mProject, 2, '', 'F', '0', '0', 'bid:project:add', 'admin', sysdate()
UNION ALL SELECT '项目修改', @mProject, 3, '', 'F', '0', '0', 'bid:project:edit', 'admin', sysdate()
UNION ALL SELECT '项目删除', @mProject, 4, '', 'F', '0', '0', 'bid:project:remove', 'admin', sysdate()
UNION ALL SELECT '项目导出', @mProject, 5, '', 'F', '0', '0', 'bid:project:export', 'admin', sysdate()
UNION ALL SELECT '状态流转', @mProject, 6, '', 'F', '0', '0', 'bid:project:edit', 'admin', sysdate()
UNION ALL SELECT '转立项', @mProject, 7, '', 'F', '0', '0', 'bid:project:edit', 'admin', sysdate()
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms=t.perms AND menu_name=t.menu_name AND parent_id=@mProject);

-- 按钮权限：保证金台账
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT * FROM (
SELECT '保证金查询' AS menu_name, @mDeposit AS parent_id, 1 AS order_num, '' AS path, 'F' AS menu_type, '0' AS visible, '0' AS status, 'bid:deposit:query' AS perms, 'admin' AS create_by, sysdate() AS create_time
UNION ALL SELECT '保证金新增', @mDeposit, 2, '', 'F', '0', '0', 'bid:deposit:add', 'admin', sysdate()
UNION ALL SELECT '保证金修改', @mDeposit, 3, '', 'F', '0', '0', 'bid:deposit:edit', 'admin', sysdate()
UNION ALL SELECT '保证金删除', @mDeposit, 4, '', 'F', '0', '0', 'bid:deposit:remove', 'admin', sysdate()
UNION ALL SELECT '保证金导出', @mDeposit, 5, '', 'F', '0', '0', 'bid:deposit:export', 'admin', sysdate()
UNION ALL SELECT '登记退回', @mDeposit, 6, '', 'F', '0', '0', 'bid:deposit:edit', 'admin', sysdate()
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms=t.perms AND menu_name=t.menu_name AND parent_id=@mDeposit);

-- 按钮权限：资质资料库
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT * FROM (
SELECT '资质查询' AS menu_name, @mQual AS parent_id, 1 AS order_num, '' AS path, 'F' AS menu_type, '0' AS visible, '0' AS status, 'bid:qualification:query' AS perms, 'admin' AS create_by, sysdate() AS create_time
UNION ALL SELECT '资质新增', @mQual, 2, '', 'F', '0', '0', 'bid:qualification:add', 'admin', sysdate()
UNION ALL SELECT '资质修改', @mQual, 3, '', 'F', '0', '0', 'bid:qualification:edit', 'admin', sysdate()
UNION ALL SELECT '资质删除', @mQual, 4, '', 'F', '0', '0', 'bid:qualification:remove', 'admin', sysdate()
UNION ALL SELECT '资质导出', @mQual, 5, '', 'F', '0', '0', 'bid:qualification:export', 'admin', sysdate()
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms=t.perms AND menu_name=t.menu_name AND parent_id=@mQual);

-- 按钮权限：关键词订阅
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT * FROM (
SELECT '订阅查询' AS menu_name, @mKeyword AS parent_id, 1 AS order_num, '' AS path, 'F' AS menu_type, '0' AS visible, '0' AS status, 'bid:keyword:query' AS perms, 'admin' AS create_by, sysdate() AS create_time
UNION ALL SELECT '订阅新增', @mKeyword, 2, '', 'F', '0', '0', 'bid:keyword:add', 'admin', sysdate()
UNION ALL SELECT '订阅修改', @mKeyword, 3, '', 'F', '0', '0', 'bid:keyword:edit', 'admin', sysdate()
UNION ALL SELECT '订阅删除', @mKeyword, 4, '', 'F', '0', '0', 'bid:keyword:remove', 'admin', sysdate()
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms=t.perms AND menu_name=t.menu_name AND parent_id=@mKeyword);

-- 按钮权限：采集源管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT * FROM (
SELECT '采集源查询' AS menu_name, @mSource AS parent_id, 1 AS order_num, '' AS path, 'F' AS menu_type, '0' AS visible, '0' AS status, 'bid:source:query' AS perms, 'admin' AS create_by, sysdate() AS create_time
UNION ALL SELECT '采集源新增', @mSource, 2, '', 'F', '0', '0', 'bid:source:add', 'admin', sysdate()
UNION ALL SELECT '采集源修改', @mSource, 3, '', 'F', '0', '0', 'bid:source:edit', 'admin', sysdate()
UNION ALL SELECT '采集源删除', @mSource, 4, '', 'F', '0', '0', 'bid:source:remove', 'admin', sysdate()
UNION ALL SELECT '立即执行', @mSource, 5, '', 'F', '0', '0', 'bid:source:run', 'admin', sysdate()
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms=t.perms AND menu_name=t.menu_name AND parent_id=@mSource);

-- 按钮权限：抓取日志（只读）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '日志查询', @mLog, 1, '', 'F', '0', '0', 'bid:crawlLog:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='bid:crawlLog:query' AND parent_id=@mLog);

-- admin 角色分配（超管角色 role_id=1 通配，兼容非通配环境显式授权）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m
WHERE (m.perms LIKE 'bid:%' OR m.menu_id IN (@parentId,@mNotice,@mProject,@mDeposit,@mQual,@mKeyword,@mSource,@mLog))
AND NOT EXISTS (SELECT 1 FROM sys_role_menu r WHERE r.role_id=1 AND r.menu_id=m.menu_id);

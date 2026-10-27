-- =============================================
-- QMS 增强功能 V2 全量SQL脚本
-- 包含：AQL方案管理/文控版本历史/文控分发借阅/CAPA有效性/根因分析/管理评审改进项/SN追溯菜单
-- =============================================

-- =============================================
-- 一、业务表 DDL
-- =============================================

-- 1. AQL抽样方案管理表
DROP TABLE IF EXISTS qms_aql_plan;
CREATE TABLE qms_aql_plan (
    plan_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '方案ID',
    aql_level       VARCHAR(10)  NOT NULL                 COMMENT 'AQL等级（如0.65,1.0,1.5,2.5,4.0）',
    code_letter     VARCHAR(10)  NOT NULL                 COMMENT '样本量字码（A-R）',
    sample_size     INT          NOT NULL                 COMMENT '样本量(n)',
    ac_val          INT          NOT NULL                 COMMENT '接收数(Ac)',
    re_val          INT          NOT NULL                 COMMENT '拒收数(Re)',
    inspect_level   VARCHAR(10)  DEFAULT '1'              COMMENT '检验严格度（1正常 2加严 3放宽）',
    batch_min       INT                                   COMMENT '批量下限',
    batch_max       INT                                   COMMENT '批量上限',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (plan_id),
    UNIQUE KEY uk_aql_code (aql_level, code_letter, inspect_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AQL抽样方案管理表';

-- 2. 文档版本历史表
DROP TABLE IF EXISTS qms_doc_version;
CREATE TABLE qms_doc_version (
    version_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '版本ID',
    doc_id          BIGINT       NOT NULL                 COMMENT '文档ID',
    doc_no          VARCHAR(64)                           COMMENT '文档编号',
    version_no      VARCHAR(20)  NOT NULL                 COMMENT '版本号',
    file_url        VARCHAR(500)                          COMMENT '文件地址',
    change_log      TEXT                                  COMMENT '变更说明',
    version_status  CHAR(1)      DEFAULT '0'              COMMENT '版本状态（0草稿 1生效 2作废）',
    publish_by      VARCHAR(64)                           COMMENT '发布人',
    publish_time    DATETIME                              COMMENT '发布时间',
    obsolete_by     VARCHAR(64)                           COMMENT '作废人',
    obsolete_time   DATETIME                              COMMENT '作废时间',
    esig_user       VARCHAR(64)                           COMMENT '电子签名用户',
    esig_meaning    VARCHAR(128)                          COMMENT '签名含义',
    esig_time       DATETIME                              COMMENT '签名时间',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (version_id),
    KEY idx_doc_id (doc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档版本历史表';

-- 3. 文档分发/借阅表
DROP TABLE IF EXISTS qms_doc_distribute;
CREATE TABLE qms_doc_distribute (
    distribute_id   BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '分发ID',
    doc_id          BIGINT       NOT NULL                 COMMENT '文档ID',
    doc_no          VARCHAR(64)                           COMMENT '文档编号',
    version_no      VARCHAR(20)                           COMMENT '版本号',
    distribute_type VARCHAR(10)  NOT NULL                 COMMENT '类型（distribute=分发, borrow=借阅）',
    dept_id         BIGINT                                COMMENT '分发部门ID',
    dept_name       VARCHAR(100)                          COMMENT '分发部门名称',
    user_id         BIGINT                                COMMENT '签收/借阅人ID',
    user_name       VARCHAR(64)                           COMMENT '签收/借阅人名称',
    receive_time    DATETIME                              COMMENT '签收时间',
    borrow_deadline DATETIME                              COMMENT '借阅到期时间',
    return_time     DATETIME                              COMMENT '归还时间',
    dist_status     CHAR(1)      DEFAULT '0'              COMMENT '状态（0待签收 1已签收 2已归还 3已失效）',
    watermark       VARCHAR(64)                           COMMENT '水印标识',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (distribute_id),
    KEY idx_doc_id (doc_id),
    KEY idx_dist_type (distribute_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档分发借阅表';

-- 4. CAPA有效性追踪表
DROP TABLE IF EXISTS qms_capa_effectiveness;
CREATE TABLE qms_capa_effectiveness (
    effectiveness_id BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '有效性追踪ID',
    capa_id          BIGINT      NOT NULL                 COMMENT 'CAPA ID',
    capa_no          VARCHAR(64)                           COMMENT 'CAPA编号',
    check_period     VARCHAR(20)                           COMMENT '检查周期（如30天/90天）',
    check_date       DATE                                  COMMENT '检查日期',
    checker          VARCHAR(64)                           COMMENT '检查人',
    check_method     VARCHAR(500)                          COMMENT '验证方法',
    check_result     VARCHAR(10)                           COMMENT '检查结果（1有效 2无效 3部分有效）',
    evidence         TEXT                                  COMMENT '证据描述',
    recurrence_flag  CHAR(1)      DEFAULT '0'              COMMENT '是否复发（0否 1是）',
    recurrence_desc  VARCHAR(500)                          COMMENT '复发描述',
    action_taken     TEXT                                  COMMENT '采取的额外措施',
    esig_user        VARCHAR(64)                           COMMENT '电子签名用户',
    esig_time       DATETIME                              COMMENT '签名时间',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0待检查 1已完成 2需再追踪）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (effectiveness_id),
    KEY idx_capa_id (capa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CAPA有效性追踪表';

-- 5. 根因分析(5Why/鱼骨图)表
DROP TABLE IF EXISTS qms_capa_root_cause;
CREATE TABLE qms_capa_root_cause (
    root_cause_id   BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '根因分析ID',
    capa_id         BIGINT       NOT NULL                 COMMENT 'CAPA ID',
    capa_no         VARCHAR(64)                           COMMENT 'CAPA编号',
    analysis_type   VARCHAR(10)  NOT NULL                 COMMENT '分析类型（5why=5Why, fishbone=鱼骨图）',
    -- 5Why字段
    why1            VARCHAR(500)                          COMMENT '为什么1',
    why2            VARCHAR(500)                          COMMENT '为什么2',
    why3            VARCHAR(500)                          COMMENT '为什么3',
    why4            VARCHAR(500)                          COMMENT '为什么4',
    why5            VARCHAR(500)                          COMMENT '为什么5',
    root_cause      VARCHAR(1000)                         COMMENT '根本原因',
    -- 鱼骨图字段（JSON存储）
    fishbone_data   TEXT                                  COMMENT '鱼骨图数据（JSON: man/machine/material/method/measure/environment）',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (root_cause_id),
    KEY idx_capa_id (capa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='根因分析表';

-- 6. 管理评审改进项表
DROP TABLE IF EXISTS qms_mr_action;
CREATE TABLE qms_mr_action (
    action_id       BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '改进项ID',
    mr_id           BIGINT       NOT NULL                 COMMENT '管理评审ID',
    mr_no           VARCHAR(64)                           COMMENT '评审编号',
    action_desc     VARCHAR(500) NOT NULL                 COMMENT '改进项描述',
    action_type     VARCHAR(20)                           COMMENT '类型（capa=转CAPA, task=转事项, doc=文控升版）',
    ref_id          BIGINT                                COMMENT '关联ID（CAPA ID等）',
    ref_no          VARCHAR(64)                           COMMENT '关联编号',
    resp_dept_id    BIGINT                                COMMENT '责任部门ID',
    resp_dept_name  VARCHAR(100)                          COMMENT '责任部门名称',
    responsible_person VARCHAR(64)                        COMMENT '责任人',
    plan_close_date DATE                                  COMMENT '计划完成日期',
    actual_close_date DATE                                COMMENT '实际完成日期',
    action_result   VARCHAR(500)                          COMMENT '完成结果',
    action_status   CHAR(1)      DEFAULT '0'              COMMENT '状态（0待处理 1进行中 2已完成 3已逾期）',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (action_id),
    KEY idx_mr_id (mr_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理评审改进项表';

-- =============================================
-- 二、字典数据
-- =============================================

-- CAPA有效性检查结果
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'CAPA有效性结果', 'qms_effectiveness_result', '0', 'admin', sysdate(), 'CAPA有效性追踪检查结果'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'qms_effectiveness_result');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time) VALUES
(1, '有效', '1', 'qms_effectiveness_result', 'success', 'N', '0', 'admin', sysdate()),
(2, '无效', '2', 'qms_effectiveness_result', 'danger', 'N', '0', 'admin', sysdate()),
(3, '部分有效', '3', 'qms_effectiveness_result', 'warning', 'N', '0', 'admin', sysdate());

-- 根因分析类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '根因分析类型', 'qms_root_cause_type', '0', 'admin', sysdate(), '根因分析方法类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'qms_root_cause_type');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '5Why分析', '5why', 'qms_root_cause_type', 'Y', '0', 'admin', sysdate()),
(2, '鱼骨图分析', 'fishbone', 'qms_root_cause_type', 'N', '0', 'admin', sysdate());

-- 文档分发类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '文档分发类型', 'qms_doc_dist_type', '0', 'admin', sysdate(), '文档分发借阅类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'qms_doc_dist_type');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '分发', 'distribute', 'qms_doc_dist_type', 'Y', '0', 'admin', sysdate()),
(2, '借阅', 'borrow', 'qms_doc_dist_type', 'N', '0', 'admin', sysdate());

-- 管理评审改进项状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '管理评审改进项状态', 'qms_mr_action_status', '0', 'admin', sysdate(), '管理评审改进项状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'qms_mr_action_status');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time) VALUES
(1, '待处理', '0', 'qms_mr_action_status', 'info', 'Y', '0', 'admin', sysdate()),
(2, '进行中', '1', 'qms_mr_action_status', 'warning', 'N', '0', 'admin', sysdate()),
(3, '已完成', '2', 'qms_mr_action_status', 'success', 'N', '0', 'admin', sysdate()),
(4, '已逾期', '3', 'qms_mr_action_status', 'danger', 'N', '0', 'admin', sysdate());

-- SN追溯状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'SN追溯状态', 'qms_sn_status', '0', 'admin', sysdate(), 'SN单件追溯状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'qms_sn_status');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time) VALUES
(1, '在制', '0', 'qms_sn_status', 'warning', 'Y', '0', 'admin', sysdate()),
(2, '入库', '1', 'qms_sn_status', 'primary', 'N', '0', 'admin', sysdate()),
(3, '发货', '2', 'qms_sn_status', 'success', 'N', '0', 'admin', sysdate()),
(4, '退货', '3', 'qms_sn_status', 'danger', 'N', '0', 'admin', sysdate());

-- =============================================
-- 三、菜单与权限
-- =============================================

SET @qmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name = '质量管理' AND parent_id = 0);
SET @qmsMasterDirId = (SELECT menu_id FROM sys_menu WHERE menu_name = '质量主数据' AND parent_id = @qmsParentId);

-- AQL抽样方案管理菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '抽样方案', @qmsMasterDirId, 7, 'aqlplan', 'qms/aqlplan/index', '', 'QmsAqlPlan', 1, 0, 'C', '0', '0', 'qms:aqlplan:list', 'tool', 'admin', sysdate(), 'AQL抽样方案管理'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '抽样方案');
SET @qmsAqlPlanId = (SELECT menu_id FROM sys_menu WHERE menu_name = '抽样方案' AND parent_id = @qmsMasterDirId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('方案查询', @qmsAqlPlanId, 1, '', 'F', '0', '0', 'qms:aqlplan:query', 'admin', sysdate()),
('方案新增', @qmsAqlPlanId, 2, '', 'F', '0', '0', 'qms:aqlplan:add', 'admin', sysdate()),
('方案修改', @qmsAqlPlanId, 3, '', 'F', '0', '0', 'qms:aqlplan:edit', 'admin', sysdate()),
('方案删除', @qmsAqlPlanId, 4, '', 'F', '0', '0', 'qms:aqlplan:remove', 'admin', sysdate()),
('方案导出', @qmsAqlPlanId, 5, '', 'F', '0', '0', 'qms:aqlplan:export', 'admin', sysdate());

-- SN追溯菜单
SET @qmsTraceDirId = (SELECT menu_id FROM sys_menu WHERE menu_name = '质量追溯' AND parent_id = @qmsParentId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 'SN追溯', @qmsTraceDirId, 4, 'sn', 'qms/trace/sn', '', 'QmsTraceSn', 1, 0, 'C', '0', '0', 'qms:trace:sn:list', 'scan', 'admin', sysdate(), 'SN单件追溯'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = 'SN追溯');
SET @qmsSnId = (SELECT menu_id FROM sys_menu WHERE menu_name = 'SN追溯' AND parent_id = @qmsTraceDirId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('SN查询', @qmsSnId, 1, '', 'F', '0', '0', 'qms:trace:sn:query', 'admin', sysdate()),
('SN新增', @qmsSnId, 2, '', 'F', '0', '0', 'qms:trace:sn:add', 'admin', sysdate()),
('SN修改', @qmsSnId, 3, '', 'F', '0', '0', 'qms:trace:sn:edit', 'admin', sysdate()),
('SN删除', @qmsSnId, 4, '', 'F', '0', '0', 'qms:trace:sn:remove', 'admin', sysdate());

-- 授权admin角色
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'qms:aqlplan%%' OR perms LIKE 'qms:trace:sn%%';

-- =============================================
-- 四、初始化AQL方案数据（从AqlCalculator内置表导入）
-- =============================================
INSERT INTO qms_aql_plan (aql_level, code_letter, sample_size, ac_val, re_val, inspect_level, batch_min, batch_max, status, create_by, create_time) VALUES
-- AQL 0.65
('0.65', 'G', 32, 0, 1, '1', 281, 500, '0', 'admin', sysdate()),
('0.65', 'H', 50, 1, 2, '1', 501, 1200, '0', 'admin', sysdate()),
('0.65', 'J', 80, 1, 2, '1', 1201, 3200, '0', 'admin', sysdate()),
('0.65', 'K', 125, 2, 3, '1', 3201, 10000, '0', 'admin', sysdate()),
('0.65', 'L', 200, 3, 4, '1', 10001, 35000, '0', 'admin', sysdate()),
('0.65', 'M', 315, 5, 6, '1', 35001, 150000, '0', 'admin', sysdate()),
('0.65', 'N', 500, 7, 8, '1', 150001, 500000, '0', 'admin', sysdate()),
('0.65', 'P', 800, 10, 11, '1', 500001, 999999999, '0', 'admin', sysdate()),
-- AQL 1.0
('1.0', 'G', 32, 1, 2, '1', 281, 500, '0', 'admin', sysdate()),
('1.0', 'H', 50, 1, 2, '1', 501, 1200, '0', 'admin', sysdate()),
('1.0', 'J', 80, 2, 3, '1', 1201, 3200, '0', 'admin', sysdate()),
('1.0', 'K', 125, 3, 4, '1', 3201, 10000, '0', 'admin', sysdate()),
('1.0', 'L', 200, 5, 6, '1', 10001, 35000, '0', 'admin', sysdate()),
('1.0', 'M', 315, 7, 8, '1', 35001, 150000, '0', 'admin', sysdate()),
('1.0', 'N', 500, 10, 11, '1', 150001, 500000, '0', 'admin', sysdate()),
-- AQL 1.5
('1.5', 'F', 20, 0, 1, '1', 151, 280, '0', 'admin', sysdate()),
('1.5', 'G', 32, 1, 2, '1', 281, 500, '0', 'admin', sysdate()),
('1.5', 'H', 50, 2, 3, '1', 501, 1200, '0', 'admin', sysdate()),
('1.5', 'J', 80, 3, 4, '1', 1201, 3200, '0', 'admin', sysdate()),
('1.5', 'K', 125, 5, 6, '1', 3201, 10000, '0', 'admin', sysdate()),
('1.5', 'L', 200, 7, 8, '1', 10001, 35000, '0', 'admin', sysdate()),
('1.5', 'M', 315, 10, 11, '1', 35001, 150000, '0', 'admin', sysdate()),
-- AQL 2.5
('2.5', 'E', 13, 0, 1, '1', 91, 150, '0', 'admin', sysdate()),
('2.5', 'F', 20, 1, 2, '1', 151, 280, '0', 'admin', sysdate()),
('2.5', 'G', 32, 2, 3, '1', 281, 500, '0', 'admin', sysdate()),
('2.5', 'H', 50, 3, 4, '1', 501, 1200, '0', 'admin', sysdate()),
('2.5', 'J', 80, 5, 6, '1', 1201, 3200, '0', 'admin', sysdate()),
('2.5', 'K', 125, 7, 8, '1', 3201, 10000, '0', 'admin', sysdate()),
('2.5', 'L', 200, 10, 11, '1', 10001, 35000, '0', 'admin', sysdate()),
('2.5', 'M', 315, 14, 15, '1', 35001, 150000, '0', 'admin', sysdate()),
-- AQL 4.0
('4.0', 'D', 8, 0, 1, '1', 51, 90, '0', 'admin', sysdate()),
('4.0', 'E', 13, 1, 2, '1', 91, 150, '0', 'admin', sysdate()),
('4.0', 'F', 20, 2, 3, '1', 151, 280, '0', 'admin', sysdate()),
('4.0', 'G', 32, 3, 4, '1', 281, 500, '0', 'admin', sysdate()),
('4.0', 'H', 50, 5, 6, '1', 501, 1200, '0', 'admin', sysdate()),
('4.0', 'J', 80, 7, 8, '1', 1201, 3200, '0', 'admin', sysdate()),
('4.0', 'K', 125, 10, 11, '1', 3201, 10000, '0', 'admin', sysdate());

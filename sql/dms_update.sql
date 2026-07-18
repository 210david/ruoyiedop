-- =============================================
-- DMS 设备管理系统 - 增量更新SQL脚本
-- 补充需求文档中缺失的功能模块
-- 包含：6张新表 + 新字典 + 新菜单权限
-- =============================================

-- =============================================
-- 一、新增业务表 DDL
-- =============================================

-- 6. 设备状态变更日志表（3.1.5 设备生命周期）
DROP TABLE IF EXISTS dms_equipment_log;
CREATE TABLE dms_equipment_log (
    log_id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    equipment_id     BIGINT       NOT NULL                 COMMENT '设备ID',
    equipment_code   VARCHAR(64)                           COMMENT '设备编号（冗余）',
    equipment_name   VARCHAR(255)                          COMMENT '设备名称（冗余）',
    change_type      VARCHAR(20)  NOT NULL                 COMMENT '变更类型（字典 dms_eq_change_type）',
    old_value        VARCHAR(500)                          COMMENT '原值',
    new_value        VARCHAR(500)                          COMMENT '新值',
    change_reason    VARCHAR(500)                          COMMENT '变更原因',
    operator_id      BIGINT                                COMMENT '操作人ID',
    operator_name    VARCHAR(64)                           COMMENT '操作人名称',
    create_time      DATETIME                              COMMENT '操作时间',
    PRIMARY KEY (log_id),
    KEY idx_eqlog_equipment (equipment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备状态变更日志表';

-- 7. 备件出入库记录表（3.4.2 出入库）
DROP TABLE IF EXISTS dms_spare_part_record;
CREATE TABLE dms_spare_part_record (
    record_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    part_id          BIGINT       NOT NULL                 COMMENT '备件ID',
    part_code        VARCHAR(64)                           COMMENT '备件编号（冗余）',
    part_name        VARCHAR(255)                          COMMENT '备件名称（冗余）',
    move_type        VARCHAR(10)  NOT NULL                 COMMENT '出入库类型（字典 dms_stock_move_type）',
    quantity         DECIMAL(18,2) NOT NULL                COMMENT '数量',
    before_stock     DECIMAL(18,2)                         COMMENT '变更前库存',
    after_stock      DECIMAL(18,2)                         COMMENT '变更后库存',
    ref_type         VARCHAR(20)                           COMMENT '关联单据类型（workorder/purchase/adjust）',
    ref_id           BIGINT                                COMMENT '关联单据ID',
    ref_no           VARCHAR(64)                           COMMENT '关联单据号',
    operator_id      BIGINT                                COMMENT '操作人ID',
    operator_name    VARCHAR(64)                           COMMENT '操作人名称',
    create_time      DATETIME                              COMMENT '操作时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (record_id),
    KEY idx_spr_part (part_id),
    KEY idx_spr_type (move_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备件出入库记录表';

-- 8. 点检路线表（3.6.1 巡检路线与点位）
DROP TABLE IF EXISTS dms_inspection_route;
CREATE TABLE dms_inspection_route (
    route_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '路线ID',
    route_name       VARCHAR(255) NOT NULL                 COMMENT '路线名称',
    route_code       VARCHAR(64)                           COMMENT '路线编码',
    dept_id          BIGINT                                COMMENT '归属部门ID',
    dept_name        VARCHAR(128)                          COMMENT '归属部门名称',
    cycle_type       VARCHAR(10)  DEFAULT '0'              COMMENT '周期类型（字典 dms_inspection_cycle）',
    cycle_value      INT          DEFAULT 1                COMMENT '周期值',
    equipment_ids    TEXT                                  COMMENT '巡检设备ID列表（JSON数组）',
    inspection_items TEXT                                  COMMENT '点检项（JSON数组，如[{item:"检查温度",type:"check"},{item:"记录压力",type:"number",unit:"MPa"}]）',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (route_id),
    UNIQUE KEY uk_route_code (route_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点检路线表';

-- 9. 点检任务表（3.6.2 点检表 + 3.6.3 PC端执行）
DROP TABLE IF EXISTS dms_inspection_task;
CREATE TABLE dms_inspection_task (
    task_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '任务ID',
    task_no          VARCHAR(64)  NOT NULL                 COMMENT '任务编号',
    route_id         BIGINT                                COMMENT '关联路线ID',
    route_name       VARCHAR(255)                          COMMENT '路线名称（冗余）',
    plan_date        DATE                                   COMMENT '计划日期',
    inspector_id     BIGINT                                COMMENT '点检人ID',
    inspector_name   VARCHAR(64)                           COMMENT '点检人名称',
    task_status      VARCHAR(10)  DEFAULT '0'              COMMENT '任务状态（字典 dms_inspection_status）',
    start_time       DATETIME                              COMMENT '开始时间',
    complete_time    DATETIME                              COMMENT '完成时间',
    abnormal_count   INT          DEFAULT 0                COMMENT '异常项数',
    result_data      TEXT                                  COMMENT '点检结果（JSON）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (task_id),
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_insptask_route (route_id),
    KEY idx_insptask_status (task_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点检任务表';

-- 10. 设备数据采集配置表（3.5 数据采集与监控）
DROP TABLE IF EXISTS dms_data_collection_config;
CREATE TABLE dms_data_collection_config (
    config_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '配置ID',
    equipment_id     BIGINT       NOT NULL                 COMMENT '设备ID',
    equipment_name   VARCHAR(255)                          COMMENT '设备名称（冗余）',
    collect_mode     VARCHAR(10)  DEFAULT '0'              COMMENT '采集模式（字典 dms_collect_mode：0人工 1自动）',
    collect_status   VARCHAR(10)  DEFAULT '1'              COMMENT '运行状态采集（0关闭 1开启）',
    collect_hours    VARCHAR(10)  DEFAULT '1'              COMMENT '运行小时数采集（0关闭 1开启）',
    collect_count    VARCHAR(10)  DEFAULT '0'              COMMENT '加工件数采集（0关闭 1开启）',
    param1_name      VARCHAR(64)                           COMMENT '自定义参数1名称',
    param1_unit      VARCHAR(20)                           COMMENT '自定义参数1单位',
    param1_alarm_min DECIMAL(18,2)                         COMMENT '参数1告警下限',
    param1_alarm_max DECIMAL(18,2)                         COMMENT '参数1告警上限',
    param2_name      VARCHAR(64)                           COMMENT '自定义参数2名称',
    param2_unit      VARCHAR(20)                           COMMENT '自定义参数2单位',
    param2_alarm_min DECIMAL(18,2)                         COMMENT '参数2告警下限',
    param2_alarm_max DECIMAL(18,2)                         COMMENT '参数2告警上限',
    param3_name      VARCHAR(64)                           COMMENT '自定义参数3名称',
    param3_unit      VARCHAR(20)                           COMMENT '自定义参数3单位',
    param3_alarm_min DECIMAL(18,2)                         COMMENT '参数3告警下限',
    param3_alarm_max DECIMAL(18,2)                         COMMENT '参数3告警上限',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (config_id),
    KEY idx_dcc_equipment (equipment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备数据采集配置表';

-- 11. 设备数据采集记录表（3.5.4 历史数据）
DROP TABLE IF EXISTS dms_data_record;
CREATE TABLE dms_data_record (
    record_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    equipment_id     BIGINT       NOT NULL                 COMMENT '设备ID',
    equipment_name   VARCHAR(255)                          COMMENT '设备名称（冗余）',
    run_status       VARCHAR(10)                           COMMENT '运行状态（0运行 1停机 2故障）',
    run_hours        DECIMAL(18,2)                         COMMENT '运行小时数',
    product_count    DECIMAL(18,2)                         COMMENT '加工件数',
    param1_value     DECIMAL(18,2)                         COMMENT '参数1值',
    param2_value     DECIMAL(18,2)                         COMMENT '参数2值',
    param3_value     DECIMAL(18,2)                         COMMENT '参数3值',
    collect_time     DATETIME                              COMMENT '采集时间',
    collect_mode     VARCHAR(10)                           COMMENT '采集方式（0人工 1自动）',
    create_by        VARCHAR(64)                           COMMENT '录入人',
    create_time      DATETIME                              COMMENT '创建时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (record_id),
    KEY idx_dr_equipment (equipment_id),
    KEY idx_dr_time (collect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备数据采集记录表';

-- 12. 维修知识FAQ表（9.3.6 常见问题管理）
DROP TABLE IF EXISTS dms_repair_faq;
CREATE TABLE dms_repair_faq (
    faq_id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'FAQ ID',
    question         VARCHAR(500) NOT NULL                 COMMENT '问题',
    answer           TEXT                                  COMMENT '标准答案',
    category_id      BIGINT                                COMMENT '关联设备分类ID',
    category_name    VARCHAR(128)                          COMMENT '分类名称',
    keywords         VARCHAR(500)                          COMMENT '关键词（逗号分隔）',
    view_count       INT          DEFAULT 0                COMMENT '查看次数',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (faq_id),
    KEY idx_faq_category (category_id),
    KEY idx_faq_keywords (keywords(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修知识FAQ表';


-- =============================================
-- 二、新增字典类型与字典数据
-- =============================================

-- 新增字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('设备变更类型', 'dms_eq_change_type', '0', 'admin', sysdate(), '设备状态/位置变更类型'),
('备件出入库类型', 'dms_stock_move_type', '0', 'admin', sysdate(), '备件出入库类型'),
('点检周期', 'dms_inspection_cycle', '0', 'admin', sysdate(), '点检巡检周期类型'),
('点检任务状态', 'dms_inspection_status', '0', 'admin', sysdate(), '点检任务状态'),
('采集模式', 'dms_collect_mode', '0', 'admin', sysdate(), '数据采集模式'),
('设备运行状态', 'dms_run_status', '0', 'admin', sysdate(), '设备运行状态');

-- 新增字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '状态变更', '0', 'dms_eq_change_type', '', 'warning', 'N', '0', 'admin', sysdate(), '设备状态变化'),
(2, '位置变更', '1', 'dms_eq_change_type', '', 'primary', 'N', '0', 'admin', sysdate(), '安装位置变化'),
(3, '部门变更', '2', 'dms_eq_change_type', '', 'info', 'N', '0', 'admin', sysdate(), '使用部门变化'),
(4, '责任人变更', '3', 'dms_eq_change_type', '', 'info', 'N', '0', 'admin', sysdate(), '责任人变化'),
(5, '调拨', '4', 'dms_eq_change_type', '', 'success', 'N', '0', 'admin', sysdate(), '设备调拨'),

(1, '入库', '0', 'dms_stock_move_type', '', 'success', 'N', '0', 'admin', sysdate(), '采购入库'),
(2, '出库-领用', '1', 'dms_stock_move_type', '', 'warning', 'N', '0', 'admin', sysdate(), '工单领用出库'),
(3, '出库-调拨', '2', 'dms_stock_move_type', '', 'info', 'N', '0', 'admin', sysdate(), '调拨出库'),
(4, '出库-报废', '3', 'dms_stock_move_type', '', 'danger', 'N', '0', 'admin', sysdate(), '报废出库'),
(5, '盘点调整', '4', 'dms_stock_move_type', '', 'primary', 'N', '0', 'admin', sysdate(), '盘点盈亏调整'),

(1, '日检', '0', 'dms_inspection_cycle', '', 'danger', 'Y', '0', 'admin', sysdate(), '每日巡检'),
(2, '周检', '1', 'dms_inspection_cycle', '', 'warning', 'N', '0', 'admin', sysdate(), '每周巡检'),
(3, '月检', '2', 'dms_inspection_cycle', '', 'primary', 'N', '0', 'admin', sysdate(), '每月巡检'),

(1, '待执行', '0', 'dms_inspection_status', '', 'info', 'Y', '0', 'admin', sysdate(), '待执行'),
(2, '执行中', '1', 'dms_inspection_status', '', 'warning', 'N', '0', 'admin', sysdate(), '执行中'),
(3, '已完成', '2', 'dms_inspection_status', '', 'success', 'N', '0', 'admin', sysdate(), '已完成'),
(4, '已逾期', '3', 'dms_inspection_status', '', 'danger', 'N', '0', 'admin', sysdate(), '已逾期'),

(1, '人工录入', '0', 'dms_collect_mode', '', 'primary', 'Y', '0', 'admin', sysdate(), '人工录入'),
(2, '自动采集', '1', 'dms_collect_mode', '', 'success', 'N', '0', 'admin', sysdate(), '设备自动采集'),

(1, '运行', '0', 'dms_run_status', '', 'success', 'Y', '0', 'admin', sysdate(), '正常运行'),
(2, '停机', '1', 'dms_run_status', '', 'info', 'N', '0', 'admin', sysdate(), '停机'),
(3, '故障', '2', 'dms_run_status', '', 'danger', 'N', '0', 'admin', sysdate(), '故障');


-- =============================================
-- 三、新增菜单权限
-- 菜单ID从3100开始
-- =============================================

-- 二级目录：点检巡检
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3004, '点检巡检', 3000, 4, 'inspection', NULL, '', '', 1, 0, 'M', '0', '0', '', 'eye-open', 'admin', sysdate(), '点检巡检管理');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3060, '巡检路线', 3004, 1, 'route', 'dms/inspection/route/index', '', 'DmsInspectionRoute', 1, 0, 'C', '0', '0', 'dms:inspection:route:list', 'tree-table', 'admin', sysdate(), ''),
(3070, '点检任务', 3004, 2, 'task', 'dms/inspection/task/index', '', 'DmsInspectionTask', 1, 0, 'C', '0', '0', 'dms:inspection:task:list', 'form', 'admin', sysdate(), '');

-- 巡检路线按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3061, '路线查询', 3060, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:inspection:route:query', '#', 'admin', sysdate(), ''),
(3062, '路线新增', 3060, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:inspection:route:add', '#', 'admin', sysdate(), ''),
(3063, '路线修改', 3060, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:inspection:route:edit', '#', 'admin', sysdate(), ''),
(3064, '路线删除', 3060, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:inspection:route:remove', '#', 'admin', sysdate(), '');

-- 点检任务按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3071, '任务查询', 3070, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:inspection:task:query', '#', 'admin', sysdate(), ''),
(3072, '任务新增', 3070, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:inspection:task:add', '#', 'admin', sysdate(), ''),
(3073, '任务修改', 3070, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:inspection:task:edit', '#', 'admin', sysdate(), ''),
(3074, '任务删除', 3070, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:inspection:task:remove', '#', 'admin', sysdate(), '');

-- 二级目录：数据采集
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3005, '数据采集', 3000, 5, 'data', NULL, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', sysdate(), '数据采集与监控');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3080, '采集配置', 3005, 1, 'config', 'dms/data/config/index', '', 'DmsDataConfig', 1, 0, 'C', '0', '0', 'dms:data:config:list', 'edit', 'admin', sysdate(), ''),
(3085, '数据记录', 3005, 2, 'record', 'dms/data/record/index', '', 'DmsDataRecord', 1, 0, 'C', '0', '0', 'dms:data:record:list', 'log', 'admin', sysdate(), '');

-- 采集配置按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3081, '配置查询', 3080, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:data:config:query', '#', 'admin', sysdate(), ''),
(3082, '配置新增', 3080, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:data:config:add', '#', 'admin', sysdate(), ''),
(3083, '配置修改', 3080, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:data:config:edit', '#', 'admin', sysdate(), ''),
(3084, '配置删除', 3080, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:data:config:remove', '#', 'admin', sysdate(), '');

-- 数据记录按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3086, '记录查询', 3085, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:data:record:query', '#', 'admin', sysdate(), ''),
(3087, '记录新增', 3085, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:data:record:add', '#', 'admin', sysdate(), ''),
(3088, '记录删除', 3085, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:data:record:remove', '#', 'admin', sysdate(), '');

-- 二级目录：报表看板
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3006, '报表看板', 3000, 6, 'dashboard', NULL, '', '', 1, 0, 'M', '0', '0', '', 'dashboard', 'admin', sysdate(), '报表与看板');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3090, '数据看板', 3006, 1, 'overview', 'dms/dashboard/index', '', 'DmsDashboard', 1, 0, 'C', '0', '0', 'dms:dashboard:list', 'chart', 'admin', sysdate(), 'KPI看板');

-- 二级目录：AI助手
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3007, 'AI助手', 3000, 7, 'ai', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', sysdate(), 'AI智能功能');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3100, '维修知识库', 3007, 1, 'faq', 'dms/ai/faq/index', '', 'DmsRepairFaq', 1, 0, 'C', '0', '0', 'dms:ai:faq:list', 'documentation', 'admin', sysdate(), '维修FAQ管理');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3101, 'FAQ查询', 3100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:ai:faq:query', '#', 'admin', sysdate(), ''),
(3102, 'FAQ新增', 3100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:ai:faq:add', '#', 'admin', sysdate(), ''),
(3103, 'FAQ修改', 3100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:ai:faq:edit', '#', 'admin', sysdate(), ''),
(3104, 'FAQ删除', 3100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:ai:faq:remove', '#', 'admin', sysdate(), '');

-- AI配置菜单（menu_id=3126，3105已被备件主数据占用）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3126, 'AI配置', 3007, 2, 'config', 'dms/ai/config', '', 'DmsAiConfig', 1, 0, 'C', '0', '0', 'dms:ai:config', 'setting', 'admin', sysdate(), 'AI大模型参数配置');


-- 给admin角色（role_id=1）分配所有新增DMS菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 3004), (1, 3005), (1, 3006), (1, 3007),
(1, 3060), (1, 3061), (1, 3062), (1, 3063), (1, 3064),
(1, 3070), (1, 3071), (1, 3072), (1, 3073), (1, 3074),
(1, 3080), (1, 3081), (1, 3082), (1, 3083), (1, 3084),
(1, 3085), (1, 3086), (1, 3087), (1, 3088),
(1, 3090),
(1, 3100), (1, 3101), (1, 3102), (1, 3103), (1, 3104),
(1, 3126);


-- =============================================
-- 四、增量字段：设备附件资料
-- =============================================
ALTER TABLE dms_equipment ADD COLUMN attachment_url VARCHAR(1000) DEFAULT NULL COMMENT '设备附件资料URL' AFTER equipment_image;

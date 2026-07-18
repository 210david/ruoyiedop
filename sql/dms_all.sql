-- =============================================
-- DMS 设备管理系统 - 全量SQL脚本
-- 包含：5张业务表 + 字典类型 + 菜单权限
-- =============================================

-- =============================================
-- 一、业务表 DDL（5张表）
-- =============================================

-- 1. 设备分类表（树形结构）
DROP TABLE IF EXISTS dms_equipment_category;
CREATE TABLE dms_equipment_category (
    category_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    parent_id        BIGINT       DEFAULT 0                COMMENT '父级ID',
    ancestors        VARCHAR(500)                          COMMENT '祖级列表',
    category_code    VARCHAR(64)  NOT NULL                 COMMENT '分类编码',
    category_name    VARCHAR(128) NOT NULL                 COMMENT '分类名称',
    category_level   INT          DEFAULT 1                COMMENT '分类层级（1-4级）',
    order_num        INT          DEFAULT 0                COMMENT '显示顺序',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (category_id),
    UNIQUE KEY uk_category_code (category_code),
    KEY idx_category_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备分类表（树形结构）';

-- 2. 设备台账表
DROP TABLE IF EXISTS dms_equipment;
CREATE TABLE dms_equipment (
    equipment_id     BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '设备ID',
    equipment_code   VARCHAR(64)  NOT NULL                 COMMENT '设备编号（唯一标识）',
    equipment_name   VARCHAR(255) NOT NULL                 COMMENT '设备名称',
    category_id      BIGINT                                COMMENT '设备分类ID',
    model            VARCHAR(128)                          COMMENT '型号',
    serial_number    VARCHAR(128)                          COMMENT '序列号',
    manufacturer     VARCHAR(255)                          COMMENT '制造商',
    supplier         VARCHAR(255)                          COMMENT '供应商',
    purchase_date    DATE                                  COMMENT '购置日期',
    original_value   DECIMAL(18,2)                         COMMENT '原值',
    dept_id          BIGINT                                COMMENT '使用部门ID',
    dept_name        VARCHAR(128)                          COMMENT '使用部门名称',
    install_location VARCHAR(255)                          COMMENT '安装位置',
    equipment_status VARCHAR(10)  DEFAULT '0'              COMMENT '设备状态（字典 dms_equipment_status）',
    responsible_id   BIGINT                                COMMENT '责任人ID',
    responsible_name VARCHAR(64)                           COMMENT '责任人名称',
equipment_image  VARCHAR(500)                          COMMENT '设备图片URL',
attachment_url   VARCHAR(1000)                         COMMENT '设备附件资料URL',
status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (equipment_id),
    UNIQUE KEY uk_equipment_code (equipment_code),
    KEY idx_equipment_category (category_id),
    KEY idx_equipment_status (equipment_status),
    KEY idx_equipment_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备台账表';

-- 3. 工单管理表
DROP TABLE IF EXISTS dms_work_order;
CREATE TABLE dms_work_order (
    order_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '工单ID',
    order_no         VARCHAR(64)  NOT NULL                 COMMENT '工单号',
    equipment_id     BIGINT       NOT NULL                 COMMENT '设备ID',
    equipment_code   VARCHAR(64)                           COMMENT '设备编号（冗余）',
    equipment_name   VARCHAR(255)                          COMMENT '设备名称（冗余）',
    order_type       VARCHAR(10)  DEFAULT '0'              COMMENT '工单类型（字典 dms_order_type）',
    fault_description VARCHAR(1000)                        COMMENT '故障现象/任务描述',
    reporter_id      BIGINT                                COMMENT '报修人ID',
    reporter_name    VARCHAR(64)                           COMMENT '报修人名称',
    report_time      DATETIME                              COMMENT '报修时间',
    priority         VARCHAR(10)  DEFAULT '2'              COMMENT '优先级（字典 dms_priority）',
    order_status     VARCHAR(10)  DEFAULT '0'              COMMENT '工单状态（字典 dms_order_status）',
    assignee_id      BIGINT                                COMMENT '维修人ID',
    assignee_name    VARCHAR(64)                           COMMENT '维修人名称',
    assign_time      DATETIME                              COMMENT '派工时间',
    arrive_time      DATETIME                              COMMENT '到达时间',
    fault_cause      VARCHAR(1000)                         COMMENT '故障原因',
    repair_measure   VARCHAR(1000)                         COMMENT '维修措施',
    spare_parts_used VARCHAR(500)                          COMMENT '更换备件',
    downtime_duration DECIMAL(10,2)                        COMMENT '停机时长（小时）',
    complete_time    DATETIME                              COMMENT '完工时间',
    complete_remark  VARCHAR(1000)                         COMMENT '完工说明',
    assigner_id      BIGINT                                COMMENT '派工人ID',
    assigner_name    VARCHAR(64)                           COMMENT '派工人名称',
    verifier_id      BIGINT                                COMMENT '验收人ID',
    verifier_name    VARCHAR(64)                           COMMENT '验收人名称',
    verify_time      DATETIME                              COMMENT '验收时间',
    verify_opinion   VARCHAR(500)                          COMMENT '验收意见',
    rating           INT                                   COMMENT '评价（1-5星）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_equipment (equipment_id),
    KEY idx_order_status (order_status),
    KEY idx_order_type (order_type),
    KEY idx_order_assignee (assignee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单管理表';

-- 4. 备件管理表
DROP TABLE IF EXISTS dms_spare_part;
CREATE TABLE dms_spare_part (
    part_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '备件ID',
    part_code        VARCHAR(64)  NOT NULL                 COMMENT '备件编号',
    part_name        VARCHAR(255) NOT NULL                 COMMENT '备件名称',
    spec             VARCHAR(255)                          COMMENT '规格',
    model            VARCHAR(128)                          COMMENT '型号',
    unit             VARCHAR(20)  DEFAULT '个'             COMMENT '单位',
    applicable_category VARCHAR(500)                       COMMENT '适用设备分类（JSON数组）',
    stock_min        DECIMAL(18,2) DEFAULT 0               COMMENT '库存下限',
    stock_max        DECIMAL(18,2) DEFAULT 0               COMMENT '库存上限',
    safety_stock     DECIMAL(18,2) DEFAULT 0               COMMENT '安全库存',
    current_stock    DECIMAL(18,2) DEFAULT 0               COMMENT '当前库存',
    storage_location VARCHAR(255)                          COMMENT '存放位置',
    supplier         VARCHAR(255)                          COMMENT '供应商',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (part_id),
    UNIQUE KEY uk_part_code (part_code),
    KEY idx_part_name (part_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备件管理表';

-- 5. PM预防性维护计划表
DROP TABLE IF EXISTS dms_pm_plan;
CREATE TABLE dms_pm_plan (
    plan_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '计划ID',
    plan_name        VARCHAR(255) NOT NULL                 COMMENT '计划名称',
    equipment_id     BIGINT                                COMMENT '设备ID（为空则按分类）',
    category_id      BIGINT                                COMMENT '设备分类ID',
    trigger_type     VARCHAR(10)  DEFAULT '0'              COMMENT '触发类型（字典 dms_pm_trigger_type）',
    cycle_value      INT                                   COMMENT '周期值',
    cycle_unit       VARCHAR(20)                           COMMENT '周期单位（天/月/小时/件）',
    task_list        TEXT                                  COMMENT '任务清单（JSON数组）',
    spare_part_list  TEXT                                  COMMENT '备件清单（JSON数组）',
    estimated_hours  DECIMAL(10,2)                         COMMENT '预估工时',
    advance_days     INT          DEFAULT 3                COMMENT '提前生成天数',
    auto_assign      CHAR(1)      DEFAULT '0'              COMMENT '是否自动派工（0否 1是）',
    last_execute_time DATETIME                             COMMENT '上次执行时间',
    next_execute_time DATETIME                             COMMENT '下次执行时间',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (plan_id),
    KEY idx_pm_equipment (equipment_id),
    KEY idx_pm_category (category_id),
    KEY idx_pm_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PM预防性维护计划表';


-- =============================================
-- 二、字典类型与字典数据
-- =============================================

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('设备状态', 'dms_equipment_status', '0', 'admin', sysdate(), '设备台账状态'),
('工单类型', 'dms_order_type', '0', 'admin', sysdate(), '维护工单类型'),
('工单状态', 'dms_order_status', '0', 'admin', sysdate(), '工单流转状态'),
('优先级', 'dms_priority', '0', 'admin', sysdate(), '工单优先级'),
('PM触发类型', 'dms_pm_trigger_type', '0', 'admin', sysdate(), '预防性维护触发类型');

-- 字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '在用', '0', 'dms_equipment_status', '', 'success', 'Y', '0', 'admin', sysdate(), '正常运行'),
(2, '闲置', '1', 'dms_equipment_status', '', 'info', 'N', '0', 'admin', sysdate(), '可用但未使用'),
(3, '维修中', '2', 'dms_equipment_status', '', 'warning', 'N', '0', 'admin', sysdate(), '正在处理工单'),
(4, '停机', '3', 'dms_equipment_status', '', 'danger', 'N', '0', 'admin', sysdate(), '因外部原因停机'),
(5, '故障', '4', 'dms_equipment_status', '', 'danger', 'N', '0', 'admin', sysdate(), '等待维修'),
(6, '报废', '5', 'dms_equipment_status', '', 'info', 'N', '0', 'admin', sysdate(), '已退出使用'),

(1, '故障报修', '0', 'dms_order_type', '', 'danger', 'Y', '0', 'admin', sysdate(), '操作员/主管发起'),
(2, 'PM维护', '1', 'dms_order_type', '', 'primary', 'N', '0', 'admin', sysdate(), '系统自动生成'),
(3, '点检整改', '2', 'dms_order_type', '', 'warning', 'N', '0', 'admin', sysdate(), '点检异常转入'),
(4, '临时任务', '3', 'dms_order_type', '', 'info', 'N', '0', 'admin', sysdate(), '管理员指派'),
(5, '改造安装', '4', 'dms_order_type', '', 'success', 'N', '0', 'admin', sysdate(), '专项项目'),

(1, '新建(待派)', '0', 'dms_order_status', '', 'info', 'Y', '0', 'admin', sysdate(), '新建待派工'),
(2, '已派工', '1', 'dms_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已派工待接单'),
(3, '已接单', '2', 'dms_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已接单待处理'),
(4, '处理中', '3', 'dms_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), '正在处理'),
(5, '已完成', '4', 'dms_order_status', '', 'success', 'N', '0', 'admin', sysdate(), '完工待验收'),
(6, '已验收', '5', 'dms_order_status', '', 'success', 'N', '0', 'admin', sysdate(), '验收通过'),
(7, '已撤销', '6', 'dms_order_status', '', 'info', 'N', '0', 'admin', sysdate(), '已撤销'),
(8, '驳回重做', '7', 'dms_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), '验收驳回'),

(1, '紧急', '0', 'dms_priority', '', 'danger', 'N', '0', 'admin', sysdate(), '影响安全/全线停机'),
(2, '高', '1', 'dms_priority', '', 'warning', 'N', '0', 'admin', sysdate(), '关键设备故障'),
(3, '中', '2', 'dms_priority', '', 'primary', 'Y', '0', 'admin', sysdate(), '一般故障'),
(4, '低', '3', 'dms_priority', '', 'info', 'N', '0', 'admin', sysdate(), '计划性/改进类'),

(1, '周期(时间)', '0', 'dms_pm_trigger_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '按固定时间间隔'),
(2, '周期(运行时长)', '1', 'dms_pm_trigger_type', '', 'warning', 'N', '0', 'admin', sysdate(), '累计运行小时数'),
(3, '周期(加工量)', '2', 'dms_pm_trigger_type', '', 'info', 'N', '0', 'admin', sysdate(), '累计加工件数');


-- =============================================
-- 三、菜单权限（DMS菜单树）
-- 菜单ID从3000开始（WMS使用2000-2122）
-- =============================================

-- 顶级目录：DMS设备管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3000, 'DMS设备管理', 0, 6, 'dms', NULL, '', '', 1, 0, 'M', '0', '0', '', 'server', 'admin', sysdate(), 'DMS设备管理目录');

-- 二级目录：基础数据
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3001, '基础数据', 3000, 1, 'base', NULL, '', '', 1, 0, 'M', '0', '0', '', 'dict', 'admin', sysdate(), '设备基础数据');

-- 基础数据-菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3010, '设备分类', 3001, 1, 'category', 'dms/category/index', '', 'DmsCategory', 1, 0, 'C', '0', '0', 'dms:category:list', 'tree', 'admin', sysdate(), ''),
(3020, '设备台账', 3001, 2, 'equipment', 'dms/equipment/index', '', 'DmsEquipment', 1, 0, 'C', '0', '0', 'dms:equipment:list', 'component', 'admin', sysdate(), ''),
(3030, '备件管理', 3001, 3, 'sparepart', 'dms/sparepart/index', '', 'DmsSparePart', 1, 0, 'C', '0', '0', 'dms:sparepart:list', 'shopping', 'admin', sysdate(), '');

-- 设备分类按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3011, '分类查询', 3010, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:category:query', '#', 'admin', sysdate(), ''),
(3012, '分类新增', 3010, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:category:add', '#', 'admin', sysdate(), ''),
(3013, '分类修改', 3010, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:category:edit', '#', 'admin', sysdate(), ''),
(3014, '分类删除', 3010, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:category:remove', '#', 'admin', sysdate(), '');

-- 设备台账按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3021, '设备查询', 3020, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:equipment:query', '#', 'admin', sysdate(), ''),
(3022, '设备新增', 3020, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:equipment:add', '#', 'admin', sysdate(), ''),
(3023, '设备修改', 3020, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:equipment:edit', '#', 'admin', sysdate(), ''),
(3024, '设备删除', 3020, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:equipment:remove', '#', 'admin', sysdate(), ''),
(3025, '设备导出', 3020, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:equipment:export', '#', 'admin', sysdate(), '');

-- 备件管理按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3031, '备件查询', 3030, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:sparepart:query', '#', 'admin', sysdate(), ''),
(3032, '备件新增', 3030, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:sparepart:add', '#', 'admin', sysdate(), ''),
(3033, '备件修改', 3030, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:sparepart:edit', '#', 'admin', sysdate(), ''),
(3034, '备件删除', 3030, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:sparepart:remove', '#', 'admin', sysdate(), ''),
(3035, '备件导出', 3030, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:sparepart:export', '#', 'admin', sysdate(), '');

-- 二级目录：工单管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3002, '工单管理', 3000, 2, 'workorder', NULL, '', '', 1, 0, 'M', '0', '0', '', 'edit', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3040, '工单列表', 3002, 1, 'list', 'dms/workorder/index', '', 'DmsWorkOrder', 1, 0, 'C', '0', '0', 'dms:workorder:list', 'list', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3041, '工单查询', 3040, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:query', '#', 'admin', sysdate(), ''),
(3042, '工单新增', 3040, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:add', '#', 'admin', sysdate(), ''),
(3043, '工单修改', 3040, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:edit', '#', 'admin', sysdate(), ''),
(3044, '工单删除', 3040, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:remove', '#', 'admin', sysdate(), ''),
(3045, '工单导出', 3040, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:export', '#', 'admin', sysdate(), '');

-- 二级目录：维护计划
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3003, '维护计划', 3000, 3, 'pm', NULL, '', '', 1, 0, 'M', '0', '0', '', 'date', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3050, 'PM计划', 3003, 1, 'plan', 'dms/pmplan/index', '', 'DmsPmPlan', 1, 0, 'C', '0', '0', 'dms:pmplan:list', 'plan', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3051, '计划查询', 3050, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:pmplan:query', '#', 'admin', sysdate(), ''),
(3052, '计划新增', 3050, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:pmplan:add', '#', 'admin', sysdate(), ''),
(3053, '计划修改', 3050, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:pmplan:edit', '#', 'admin', sysdate(), ''),
(3054, '计划删除', 3050, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:pmplan:remove', '#', 'admin', sysdate(), '');

-- 给admin角色（role_id=1）分配所有DMS菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 3000), (1, 3001), (1, 3002), (1, 3003),
(1, 3010), (1, 3011), (1, 3012), (1, 3013), (1, 3014),
(1, 3020), (1, 3021), (1, 3022), (1, 3023), (1, 3024), (1, 3025),
(1, 3030), (1, 3031), (1, 3032), (1, 3033), (1, 3034), (1, 3035),
(1, 3040), (1, 3041), (1, 3042), (1, 3043), (1, 3044), (1, 3045),
(1, 3050), (1, 3051), (1, 3052), (1, 3053), (1, 3054);


-- =============================================
-- 四、预置设备分类数据（依据GB/T国标，简化版）
-- =============================================

INSERT INTO dms_equipment_category (category_id, parent_id, ancestors, category_code, category_name, category_level, order_num, status, del_flag, create_by, create_time, remark) VALUES
(1, 0, '0', 'A', '金属切削机床', 1, 1, '0', '0', 'admin', sysdate(), '金属切削加工设备'),
(2, 1, '0,1', 'A01', '车床', 2, 1, '0', '0', 'admin', sysdate(), ''),
(3, 1, '0,1', 'A02', '铣床', 2, 2, '0', '0', 'admin', sysdate(), ''),
(4, 1, '0,1', 'A03', '钻床', 2, 3, '0', '0', 'admin', sysdate(), ''),
(5, 1, '0,1', 'A04', '磨床', 2, 4, '0', '0', 'admin', sysdate(), ''),
(6, 1, '0,1', 'A05', '数控机床', 2, 5, '0', '0', 'admin', sysdate(), ''),
(7, 0, '0', 'B', '锻压设备', 1, 2, '0', '0', 'admin', sysdate(), '锻压加工设备'),
(8, 7, '0,7', 'B01', '冲床', 2, 1, '0', '0', 'admin', sysdate(), ''),
(9, 7, '0,7', 'B02', '剪板机', 2, 2, '0', '0', 'admin', sysdate(), ''),
(10, 7, '0,7', 'B03', '折弯机', 2, 3, '0', '0', 'admin', sysdate(), ''),
(11, 0, '0', 'C', '动力设备', 1, 3, '0', '0', 'admin', sysdate(), '动力供应设备'),
(12, 11, '0,11', 'C01', '空压机', 2, 1, '0', '0', 'admin', sysdate(), ''),
(13, 11, '0,11', 'C02', '变压器', 2, 2, '0', '0', 'admin', sysdate(), ''),
(14, 11, '0,11', 'C03', '发电机', 2, 3, '0', '0', 'admin', sysdate(), ''),
(15, 0, '0', 'D', '起重运输设备', 1, 4, '0', '0', 'admin', sysdate(), '物料搬运设备'),
(16, 15, '0,15', 'D01', '行车', 2, 1, '0', '0', 'admin', sysdate(), ''),
(17, 15, '0,15', 'D02', '叉车', 2, 2, '0', '0', 'admin', sysdate(), ''),
(18, 15, '0,15', 'D03', '输送带', 2, 3, '0', '0', 'admin', sysdate(), ''),
(19, 0, '0', 'E', '检测仪器', 1, 5, '0', '0', 'admin', sysdate(), '检测测量设备'),
(20, 19, '0,19', 'E01', '三坐标测量仪', 2, 1, '0', '0', 'admin', sysdate(), ''),
(21, 19, '0,19', 'E02', '光谱仪', 2, 2, '0', '0', 'admin', sysdate(), ''),
(22, 0, '0', 'F', '其他设备', 1, 6, '0', '0', 'admin', sysdate(), '其他工业设备');

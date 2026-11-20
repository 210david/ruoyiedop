-- ============================================================
-- 综合办公模块（印章管理 / 车辆管理 / 司机管理 / 车辆保险管理）
-- 1. 字典  2. 台账表  3. 菜单与按钮
-- 执行库：ruoyi_lyjs
-- ============================================================

USE `ruoyi_lyjs`;

-- ------------------------------------------------------------
-- 1. 字典
-- ------------------------------------------------------------
DELETE FROM sys_dict_data WHERE dict_type IN ('oa_seal_type','oa_seal_status','oa_vehicle_type','oa_vehicle_status','oa_driver_status','oa_insurance_type','oa_insurance_status');
DELETE FROM sys_dict_type WHERE dict_type IN ('oa_seal_type','oa_seal_status','oa_vehicle_type','oa_vehicle_status','oa_driver_status','oa_insurance_type','oa_insurance_status');

INSERT INTO sys_dict_type (dict_name, dict_type, dict_category, status, create_by, create_time, remark) VALUES
('印章类型', 'oa_seal_type', 'system', '0', 'admin', sysdate(), '综合办公-印章类型'),
('印章状态', 'oa_seal_status', 'system', '0', 'admin', sysdate(), '综合办公-印章状态'),
('车辆类型', 'oa_vehicle_type', 'system', '0', 'admin', sysdate(), '综合办公-车辆类型'),
('车辆状态', 'oa_vehicle_status', 'system', '0', 'admin', sysdate(), '综合办公-车辆状态'),
('司机状态', 'oa_driver_status', 'system', '0', 'admin', sysdate(), '综合办公-司机状态'),
('保险类型', 'oa_insurance_type', 'system', '0', 'admin', sysdate(), '综合办公-保险类型'),
('保险状态', 'oa_insurance_status', 'system', '0', 'admin', sysdate(), '综合办公-保险状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '公章',       '1', 'oa_seal_type', '', 'primary', 'N', '0', 'admin', sysdate()),
(2, '合同专用章', '2', 'oa_seal_type', '', 'success', 'N', '0', 'admin', sysdate()),
(3, '财务专用章', '3', 'oa_seal_type', '', 'warning', 'N', '0', 'admin', sysdate()),
(4, '法人章',     '4', 'oa_seal_type', '', 'info',    'N', '0', 'admin', sysdate()),
(5, '部门章',     '5', 'oa_seal_type', '', 'info',    'N', '0', 'admin', sysdate()),
(6, '其他',       '6', 'oa_seal_type', '', 'info',    'N', '0', 'admin', sysdate()),
(1, '正常', '0', 'oa_seal_status', '', 'success', 'Y', '0', 'admin', sysdate()),
(2, '停用', '1', 'oa_seal_status', '', 'warning', 'N', '0', 'admin', sysdate()),
(3, '已注销', '2', 'oa_seal_status', '', 'danger', 'N', '0', 'admin', sysdate()),
(1, '轿车',     '1', 'oa_vehicle_type', '', 'primary', 'N', '0', 'admin', sysdate()),
(2, '商务车',   '2', 'oa_vehicle_type', '', 'success', 'N', '0', 'admin', sysdate()),
(3, '越野车',   '3', 'oa_vehicle_type', '', 'info',    'N', '0', 'admin', sysdate()),
(4, '客车',     '4', 'oa_vehicle_type', '', 'info',    'N', '0', 'admin', sysdate()),
(5, '货车',     '5', 'oa_vehicle_type', '', 'warning', 'N', '0', 'admin', sysdate()),
(6, '特种车辆', '6', 'oa_vehicle_type', '', 'warning', 'N', '0', 'admin', sysdate()),
(1, '可用',   '0', 'oa_vehicle_status', '', 'success', 'Y', '0', 'admin', sysdate()),
(2, '使用中', '1', 'oa_vehicle_status', '', 'primary', 'N', '0', 'admin', sysdate()),
(3, '维修中', '2', 'oa_vehicle_status', '', 'warning', 'N', '0', 'admin', sysdate()),
(4, '停用',   '3', 'oa_vehicle_status', '', 'info',    'N', '0', 'admin', sysdate()),
(5, '已报废', '4', 'oa_vehicle_status', '', 'danger',  'N', '0', 'admin', sysdate()),
(1, '在岗', '0', 'oa_driver_status', '', 'success', 'Y', '0', 'admin', sysdate()),
(2, '休假', '1', 'oa_driver_status', '', 'warning', 'N', '0', 'admin', sysdate()),
(3, '停用', '2', 'oa_driver_status', '', 'danger',  'N', '0', 'admin', sysdate()),
(1, '交强险',       '1', 'oa_insurance_type', '', 'primary', 'N', '0', 'admin', sysdate()),
(2, '商业险',       '2', 'oa_insurance_type', '', 'success', 'N', '0', 'admin', sysdate()),
(3, '车损险',       '3', 'oa_insurance_type', '', 'info',    'N', '0', 'admin', sysdate()),
(4, '第三者责任险', '4', 'oa_insurance_type', '', 'info',    'N', '0', 'admin', sysdate()),
(5, '座位险',       '5', 'oa_insurance_type', '', 'warning', 'N', '0', 'admin', sysdate()),
(6, '其他',         '6', 'oa_insurance_type', '', 'info',    'N', '0', 'admin', sysdate()),
(1, '有效',     '0', 'oa_insurance_status', '', 'success', 'Y', '0', 'admin', sysdate()),
(2, '即将到期', '1', 'oa_insurance_status', '', 'warning', 'N', '0', 'admin', sysdate()),
(3, '已过期',   '2', 'oa_insurance_status', '', 'danger',  'N', '0', 'admin', sysdate());

-- ------------------------------------------------------------
-- 2. 台账表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_oa_seal (
  seal_id        bigint       NOT NULL AUTO_INCREMENT COMMENT '印章ID',
  seal_name      varchar(100) NOT NULL COMMENT '印章名称',
  seal_code      varchar(64)  DEFAULT NULL COMMENT '印章编号',
  seal_type      varchar(20)  DEFAULT NULL COMMENT '印章类型(oa_seal_type)',
  keeper_name    varchar(50)  DEFAULT NULL COMMENT '保管人',
  keeper_dept    varchar(100) DEFAULT NULL COMMENT '保管部门',
  keeper_phone   varchar(20)  DEFAULT NULL COMMENT '联系电话',
  keep_place     varchar(200) DEFAULT NULL COMMENT '存放地点',
  enable_date    date         DEFAULT NULL COMMENT '启用日期',
  status         char(1)      DEFAULT '0' COMMENT '状态(0正常 1停用 2已注销)',
  flow_instance_id varchar(64) DEFAULT NULL COMMENT '关联流程实例ID(预留)',
  attachment     varchar(1000) DEFAULT NULL COMMENT '附件',
  del_flag       char(1)      DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  create_by      varchar(64)  DEFAULT '' COMMENT '创建者',
  create_time    datetime     DEFAULT NULL COMMENT '创建时间',
  update_by      varchar(64)  DEFAULT '' COMMENT '更新者',
  update_time    datetime     DEFAULT NULL COMMENT '更新时间',
  remark         varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (seal_id),
  KEY idx_oa_seal_name (seal_name),
  KEY idx_oa_seal_type (seal_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='印章台账';

CREATE TABLE IF NOT EXISTS biz_oa_vehicle (
  vehicle_id      bigint       NOT NULL AUTO_INCREMENT COMMENT '车辆ID',
  vehicle_code    varchar(64)  DEFAULT NULL COMMENT '车辆编号',
  plate_no        varchar(32)  NOT NULL COMMENT '车牌号',
  vehicle_type    varchar(20)  DEFAULT NULL COMMENT '车辆类型(oa_vehicle_type)',
  vehicle_model   varchar(100) DEFAULT NULL COMMENT '品牌型号',
  vehicle_use     varchar(100) DEFAULT NULL COMMENT '使用用途',
  seat_count      int          DEFAULT NULL COMMENT '座位数',
  owner_dept      varchar(100) DEFAULT NULL COMMENT '所属部门',
  driver_name     varchar(50)  DEFAULT NULL COMMENT '默认驾驶员',
  driver_phone    varchar(20)  DEFAULT NULL COMMENT '驾驶员电话',
  license_no      varchar(64)  DEFAULT NULL COMMENT '行驶证号',
  purchase_date   date         DEFAULT NULL COMMENT '购置日期',
  inspect_expire  date         DEFAULT NULL COMMENT '年检到期日',
  insurance_expire date        DEFAULT NULL COMMENT '保险到期日',
  status          char(1)      DEFAULT '0' COMMENT '状态(0可用 1使用中 2维修中 3停用 4已报废)',
  flow_instance_id varchar(64) DEFAULT NULL COMMENT '关联流程实例ID(预留)',
  attachment      varchar(1000) DEFAULT NULL COMMENT '附件',
  del_flag        char(1)      DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  create_by       varchar(64)  DEFAULT '' COMMENT '创建者',
  create_time     datetime     DEFAULT NULL COMMENT '创建时间',
  update_by       varchar(64)  DEFAULT '' COMMENT '更新者',
  update_time     datetime     DEFAULT NULL COMMENT '更新时间',
  remark          varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (vehicle_id),
  UNIQUE KEY uk_oa_vehicle_plate (plate_no),
  KEY idx_oa_vehicle_type (vehicle_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆台账';

CREATE TABLE IF NOT EXISTS biz_oa_driver (
  driver_id       bigint       NOT NULL AUTO_INCREMENT COMMENT '司机ID',
  driver_name     varchar(50)  NOT NULL COMMENT '司机姓名',
  driver_no       varchar(64)  DEFAULT NULL COMMENT '工号',
  phone           varchar(20)  DEFAULT NULL COMMENT '手机号',
  id_card         varchar(32)  DEFAULT NULL COMMENT '身份证号',
  license_no      varchar(64)  DEFAULT NULL COMMENT '驾驶证号',
  license_type    varchar(20)  DEFAULT NULL COMMENT '准驾车型',
  license_expire  date         DEFAULT NULL COMMENT '驾驶证有效期至',
  entry_date      date         DEFAULT NULL COMMENT '入职日期',
  owner_dept      varchar(100) DEFAULT NULL COMMENT '所属部门',
  driving_years   int          DEFAULT NULL COMMENT '驾龄(年)',
  status          char(1)      DEFAULT '0' COMMENT '状态(0在岗 1休假 2停用)',
  flow_instance_id varchar(64) DEFAULT NULL COMMENT '关联流程实例ID（公车使用申请流程写回）',
  attachment      varchar(1000) DEFAULT NULL COMMENT '附件',
  del_flag        char(1)      DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  create_by       varchar(64)  DEFAULT '' COMMENT '创建者',
  create_time     datetime     DEFAULT NULL COMMENT '创建时间',
  update_by       varchar(64)  DEFAULT '' COMMENT '更新者',
  update_time     datetime     DEFAULT NULL COMMENT '更新时间',
  remark          varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (driver_id),
  KEY idx_oa_driver_name (driver_name),
  KEY idx_oa_driver_no (driver_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机台账';

CREATE TABLE IF NOT EXISTS biz_oa_vehicle_insurance (
  insurance_id    bigint        NOT NULL AUTO_INCREMENT COMMENT '保险ID',
  policy_no       varchar(64)   NOT NULL COMMENT '保单号',
  insurance_no    varchar(64)   DEFAULT NULL COMMENT '投保单号',
  vehicle_id      bigint        DEFAULT NULL COMMENT '车辆ID',
  plate_no        varchar(32)   DEFAULT NULL COMMENT '车牌号',
  insurance_type  varchar(20)   DEFAULT NULL COMMENT '保险类型(oa_insurance_type)',
  insurer         varchar(100)  DEFAULT NULL COMMENT '保险公司',
  start_date      date          DEFAULT NULL COMMENT '起保日期',
  end_date        date          DEFAULT NULL COMMENT '到期日期',
  premium         decimal(12,2) DEFAULT NULL COMMENT '保费(元)',
  insured_amount  decimal(14,2) DEFAULT NULL COMMENT '保额(元)',
  contact_name    varchar(50)   DEFAULT NULL COMMENT '联系人',
  contact_phone   varchar(20)   DEFAULT NULL COMMENT '联系电话',
  status          char(1)       DEFAULT '0' COMMENT '状态(0有效 1即将到期 2已过期)',
  attachment      varchar(1000) DEFAULT NULL COMMENT '附件',
  del_flag        char(1)       DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  create_by       varchar(64)   DEFAULT '' COMMENT '创建者',
  create_time     datetime      DEFAULT NULL COMMENT '创建时间',
  update_by       varchar(64)   DEFAULT '' COMMENT '更新者',
  update_time     datetime      DEFAULT NULL COMMENT '更新时间',
  remark          varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (insurance_id),
  KEY idx_oa_ins_policy (policy_no),
  KEY idx_oa_ins_plate (plate_no),
  KEY idx_oa_ins_end (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆保险台账';

-- ------------------------------------------------------------
-- 3. 菜单（目录 + 4个功能菜单 + 按钮）
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 9064000 AND 9064499;
DELETE FROM sys_menu WHERE menu_id BETWEEN 9064000 AND 9064499;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(9064000, '综合办公', 0, 13, 'oa', NULL, NULL, 'Oa', 1, 0, 'M', '0', '0', NULL, 'office-building', 'admin', sysdate(), '综合办公目录');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(9064100, '印章管理', 9064000, 1, 'seal', 'oa/seal/index', NULL, 'OaSeal', 1, 0, 'C', '0', '0', 'oa:seal:list', 'shield', 'admin', sysdate(), '印章管理菜单'),
(9064200, '车辆管理', 9064000, 2, 'vehicle', 'oa/vehicle/index', NULL, 'OaVehicle', 1, 0, 'C', '0', '0', 'oa:vehicle:list', 'guide', 'admin', sysdate(), '车辆管理菜单'),
(9064300, '司机管理', 9064000, 3, 'driver', 'oa/driver/index', NULL, 'OaDriver', 1, 0, 'C', '0', '0', 'oa:driver:list', 'peoples', 'admin', sysdate(), '司机管理菜单'),
(9064400, '车辆保险管理', 9064000, 4, 'insurance', 'oa/insurance/index', NULL, 'OaInsurance', 1, 0, 'C', '0', '0', 'oa:insurance:list', 'documentation', 'admin', sysdate(), '车辆保险管理菜单');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(9064101, '印章查询', 9064100, 1, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:seal:query',  '#', 'admin', sysdate(), ''),
(9064102, '印章新增', 9064100, 2, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:seal:add',    '#', 'admin', sysdate(), ''),
(9064103, '印章修改', 9064100, 3, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:seal:edit',   '#', 'admin', sysdate(), ''),
(9064104, '印章删除', 9064100, 4, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:seal:remove', '#', 'admin', sysdate(), ''),
(9064105, '印章导出', 9064100, 5, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:seal:export', '#', 'admin', sysdate(), ''),
(9064106, '印章导入', 9064100, 6, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:seal:import', '#', 'admin', sysdate(), ''),
(9064201, '车辆查询', 9064200, 1, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:vehicle:query',  '#', 'admin', sysdate(), ''),
(9064202, '车辆新增', 9064200, 2, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:vehicle:add',    '#', 'admin', sysdate(), ''),
(9064203, '车辆修改', 9064200, 3, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:vehicle:edit',   '#', 'admin', sysdate(), ''),
(9064204, '车辆删除', 9064200, 4, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:vehicle:remove', '#', 'admin', sysdate(), ''),
(9064205, '车辆导出', 9064200, 5, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:vehicle:export', '#', 'admin', sysdate(), ''),
(9064206, '车辆导入', 9064200, 6, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:vehicle:import', '#', 'admin', sysdate(), ''),
(9064301, '司机查询', 9064300, 1, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:driver:query',  '#', 'admin', sysdate(), ''),
(9064302, '司机新增', 9064300, 2, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:driver:add',    '#', 'admin', sysdate(), ''),
(9064303, '司机修改', 9064300, 3, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:driver:edit',   '#', 'admin', sysdate(), ''),
(9064304, '司机删除', 9064300, 4, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:driver:remove', '#', 'admin', sysdate(), ''),
(9064305, '司机导出', 9064300, 5, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:driver:export', '#', 'admin', sysdate(), ''),
(9064306, '司机导入', 9064300, 6, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:driver:import', '#', 'admin', sysdate(), ''),
(9064401, '保险查询', 9064400, 1, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:insurance:query',  '#', 'admin', sysdate(), ''),
(9064402, '保险新增', 9064400, 2, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:insurance:add',    '#', 'admin', sysdate(), ''),
(9064403, '保险修改', 9064400, 3, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:insurance:edit',   '#', 'admin', sysdate(), ''),
(9064404, '保险删除', 9064400, 4, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:insurance:remove', '#', 'admin', sysdate(), ''),
(9064405, '保险导出', 9064400, 5, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:insurance:export', '#', 'admin', sysdate(), ''),
(9064406, '保险导入', 9064400, 6, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'oa:insurance:import', '#', 'admin', sysdate(), '');

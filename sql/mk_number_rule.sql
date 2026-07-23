-- =============================================
-- MK 营销管理 - 编号规则管理
-- 功能：活动编号、客户编号、线索编号、商机编号、订单编号自动生成
-- =============================================

-- =============================================
-- 一、创建编号规则表
-- =============================================
DROP TABLE IF EXISTS `mk_number_rule`;
CREATE TABLE `mk_number_rule` (
  `rule_id`         BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '规则ID',
  `rule_code`       VARCHAR(50)   NOT NULL                 COMMENT '规则编码（业务标识，如 activity/customer/lead/opportunity/order）',
  `rule_name`       VARCHAR(100)  NOT NULL                 COMMENT '规则名称',
  `prefix`          VARCHAR(20)   DEFAULT ''               COMMENT '编号前缀',
  `date_format`     VARCHAR(20)   DEFAULT ''               COMMENT '日期格式（yyyyMMdd=按天,yyyyMM=按月,yyyy=按年,空=不包含日期）',
  `reset_type`      CHAR(1)       DEFAULT '0'              COMMENT '序号重置类型（0=不重置,1=按天,2=按月,3=按年）',
  `seq_length`      INT(11)       DEFAULT 4                COMMENT '序列号长度（不足前补零）',
  `seq_start`       BIGINT(20)    DEFAULT 1                COMMENT '序列号起始值',
  `step`            INT(11)       DEFAULT 1                COMMENT '每次增长步长',
  `current_seq`     BIGINT(20)    DEFAULT 0                COMMENT '当前序列号',
  `current_date_str` VARCHAR(20)  DEFAULT ''               COMMENT '当前日期串（用于判断是否需要重置序列号）',
  `connector`       VARCHAR(5)    DEFAULT ''               COMMENT '前缀与日期之间的连接符',
  `status`          CHAR(1)       DEFAULT '0'              COMMENT '状态（0=正常,1=停用）',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME                               COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME                               COMMENT '更新时间',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`rule_id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编号规则表';

-- =============================================
-- 二、初始化默认编号规则
-- =============================================
INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`) VALUES
('activity',   '活动编号', 'HD',  'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '营销活动编号，每日重置'),
('customer',   '客户编号', 'CUS', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '企业客户编号，每日重置'),
('lead',       '线索编号', 'XS',  'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '线索编号，每日重置'),
('opportunity','商机编号', 'SJ',  'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '商机编号，每日重置'),
('order',      '订单编号', 'DD',  'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '订单编号，每日重置'),
('contract',   '合同编号', 'HT',  'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '合同编号，每日重置');

-- =============================================
-- 三、编号规则重置类型字典
-- =============================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('编号重置类型', 'mk_number_reset_type', '0', 'admin', sysdate(), '编号规则序号重置类型');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '不重置', '0', 'mk_number_reset_type', '', 'info',    'N', '0', 'admin', sysdate(), ''),
(2, '按天',   '1', 'mk_number_reset_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(3, '按月',   '2', 'mk_number_reset_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '按年',   '3', 'mk_number_reset_type', '', 'warning', 'N', '0', 'admin', sysdate(), '');

-- =============================================
-- 四、编号规则管理菜单（挂在营销管理目录下）
-- =============================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4140, '编号规则', 4000, 7, 'numberRule', 'mk/numberRule/index', '', 'MkNumberRule', 1, 0, 'C', '0', '0', 'marketing:numberRule:list', 'number', 'admin', sysdate(), '编号规则管理');

-- 编号规则按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4141, '规则查询', 4140, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:numberRule:query', '#', 'admin', sysdate(), ''),
(4142, '规则新增', 4140, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:numberRule:add', '#', 'admin', sysdate(), ''),
(4143, '规则修改', 4140, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:numberRule:edit', '#', 'admin', sysdate(), ''),
(4144, '规则删除', 4140, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:numberRule:remove', '#', 'admin', sysdate(), ''),
(4145, '规则导出', 4140, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:numberRule:export', '#', 'admin', sysdate(), '');

-- =============================================
-- 五、给admin角色分配编号规则菜单权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 4140), (1, 4141), (1, 4142), (1, 4143), (1, 4144), (1, 4145);

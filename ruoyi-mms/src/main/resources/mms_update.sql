-- =====================================================
-- MMS 生产管控模块 - 字典数据与菜单权限补充SQL
-- 执行时间: 2026-08-16
-- 说明: 补充生产管控模块所需的字典数据和业务操作菜单权限
-- =====================================================

-- =====================
-- 一、字典类型数据
-- =====================

-- 1. 工单类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('工单类型', 'mms_order_type', '0', 'admin', sysdate(), '生产工单类型')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 2. 优先级
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('优先级', 'mms_priority', '0', 'admin', sysdate(), '优先级')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 3. 工单状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('工单状态', 'mms_workorder_status', '0', 'admin', sysdate(), '生产工单状态')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 4. MPS计划状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('MPS计划状态', 'mms_mps_status', '0', 'admin', sysdate(), '主生产计划状态')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 5. 报工状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('报工状态', 'mms_report_status', '0', 'admin', sysdate(), '报工记录状态')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 6. 异常类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('异常类型', 'mms_abnormal_type', '0', 'admin', sysdate(), '生产异常类型')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 7. 异常严重等级
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('异常严重等级', 'mms_abnormal_severity', '0', 'admin', sysdate(), '异常严重等级')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 8. 异常状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('异常状态', 'mms_abnormal_status', '0', 'admin', sysdate(), '异常处理状态')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 9. 齐套检查状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('齐套检查状态', 'mms_kit_status', '0', 'admin', sysdate(), '齐套检查状态')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 10. 是否
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('是否', 'mms_yes_no', '0', 'admin', sysdate(), '是否')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);


-- =====================
-- 二、字典数据明细
-- =====================

-- 1. 工单类型明细
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '生产', '0', 'mms_order_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '标准生产'),
(2, '返工', '1', 'mms_order_type', '', 'warning', 'N', '0', 'admin', sysdate(), '返工生产'),
(3, '外协', '2', 'mms_order_type', '', 'info', 'N', '0', 'admin', sysdate(), '外协生产')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 2. 优先级明细
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '高', '0', 'mms_priority', '', 'danger', 'N', '0', 'admin', sysdate(), '高优先级'),
(2, '中', '1', 'mms_priority', '', 'warning', 'Y', '0', 'admin', sysdate(), '中优先级'),
(3, '低', '2', 'mms_priority', '', 'info', 'N', '0', 'admin', sysdate(), '低优先级')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 3. 工单状态明细
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '新建', '0', 'mms_workorder_status', '', 'info', 'Y', '0', 'admin', sysdate(), '新建状态'),
(2, '已下达', '1', 'mms_workorder_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已下达'),
(3, '执行中', '2', 'mms_workorder_status', '', 'primary', 'N', '0', 'admin', sysdate(), '执行中'),
(4, '报工中', '3', 'mms_workorder_status', '', 'primary', 'N', '0', 'admin', sysdate(), '报工中'),
(5, '待完工质检', '4', 'mms_workorder_status', '', 'warning', 'N', '0', 'admin', sysdate(), '待完工质检'),
(6, '完工入库', '5', 'mms_workorder_status', '', 'success', 'N', '0', 'admin', sysdate(), '完工入库'),
(7, '已关闭', '6', 'mms_workorder_status', '', 'success', 'N', '0', 'admin', sysdate(), '已关闭'),
(8, '已暂停', '7', 'mms_workorder_status', '', 'warning', 'N', '0', 'admin', sysdate(), '已暂停'),
(9, '已作废', '8', 'mms_workorder_status', '', 'danger', 'N', '0', 'admin', sysdate(), '已作废')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 4. MPS计划状态明细
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿', '0', 'mms_mps_status', '', 'info', 'Y', '0', 'admin', sysdate(), '草稿'),
(2, '已确认', '1', 'mms_mps_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已确认'),
(3, '已发布', '2', 'mms_mps_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已发布'),
(4, '已下达', '3', 'mms_mps_status', '', 'success', 'N', '0', 'admin', sysdate(), '已下达'),
(5, '已取消', '4', 'mms_mps_status', '', 'danger', 'N', '0', 'admin', sysdate(), '已取消')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 5. 报工状态明细
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待审核', '0', 'mms_report_status', '', 'warning', 'Y', '0', 'admin', sysdate(), '待审核'),
(2, '已审核', '1', 'mms_report_status', '', 'success', 'N', '0', 'admin', sysdate(), '已审核'),
(3, '已驳回', '2', 'mms_report_status', '', 'danger', 'N', '0', 'admin', sysdate(), '已驳回')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 6. 异常类型明细
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '设备', '0', 'mms_abnormal_type', '', 'danger', 'N', '0', 'admin', sysdate(), '设备异常'),
(2, '物料', '1', 'mms_abnormal_type', '', 'warning', 'N', '0', 'admin', sysdate(), '物料异常'),
(3, '质量', '2', 'mms_abnormal_type', '', 'danger', 'N', '0', 'admin', sysdate(), '质量异常'),
(4, '安全', '3', 'mms_abnormal_type', '', 'danger', 'N', '0', 'admin', sysdate(), '安全异常'),
(5, '其他', '4', 'mms_abnormal_type', '', 'info', 'N', '0', 'admin', sysdate(), '其他异常')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 7. 异常严重等级明细
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '一般', '0', 'mms_abnormal_severity', '', 'info', 'Y', '0', 'admin', sysdate(), '一般'),
(2, '严重', '1', 'mms_abnormal_severity', '', 'warning', 'N', '0', 'admin', sysdate(), '严重'),
(3, '紧急', '2', 'mms_abnormal_severity', '', 'danger', 'N', '0', 'admin', sysdate(), '紧急')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 8. 异常状态明细
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待响应', '0', 'mms_abnormal_status', '', 'danger', 'Y', '0', 'admin', sysdate(), '待响应'),
(2, '处理中', '1', 'mms_abnormal_status', '', 'warning', 'N', '0', 'admin', sysdate(), '处理中'),
(3, '已关闭', '2', 'mms_abnormal_status', '', 'success', 'N', '0', 'admin', sysdate(), '已关闭')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 9. 齐套检查状态明细
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待检查', '0', 'mms_kit_status', '', 'info', 'Y', '0', 'admin', sysdate(), '待检查'),
(2, '已通过', '1', 'mms_kit_status', '', 'success', 'N', '0', 'admin', sysdate(), '已通过'),
(3, '缺料', '2', 'mms_kit_status', '', 'danger', 'N', '0', 'admin', sysdate(), '缺料')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 10. 是否明细
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '是', '1', 'mms_yes_no', '', 'success', 'Y', '0', 'admin', sysdate(), '是'),
(2, '否', '0', 'mms_yes_no', '', 'danger', 'N', '0', 'admin', sysdate(), '否')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);


-- =====================
-- 三、菜单权限补充
-- 说明：在已有MMS菜单下补充业务操作按钮权限
-- =====================

-- 先查询MMS父菜单ID（假设已存在，菜单ID从2000开始）
-- 工单业务操作按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('工单下达', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:workorder:list' LIMIT 1) t), 10, '', '', 1, 0, 'F', '0', '0', 'mms:workorder:release', '#', 'admin', sysdate(), '工单下达')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('工单暂停', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:workorder:list' LIMIT 1) t), 11, '', '', 1, 0, 'F', '0', '0', 'mms:workorder:pause', '#', 'admin', sysdate(), '工单暂停')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('工单恢复', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:workorder:list' LIMIT 1) t), 12, '', '', 1, 0, 'F', '0', '0', 'mms:workorder:resume', '#', 'admin', sysdate(), '工单恢复')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('工单完工', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:workorder:list' LIMIT 1) t), 13, '', '', 1, 0, 'F', '0', '0', 'mms:workorder:finish', '#', 'admin', sysdate(), '工单完工')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('工单关闭', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:workorder:list' LIMIT 1) t), 14, '', '', 1, 0, 'F', '0', '0', 'mms:workorder:close', '#', 'admin', sysdate(), '工单关闭')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('工单作废', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:workorder:list' LIMIT 1) t), 15, '', '', 1, 0, 'F', '0', '0', 'mms:workorder:cancel', '#', 'admin', sysdate(), '工单作废')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('工单拆分', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:workorder:list' LIMIT 1) t), 16, '', '', 1, 0, 'F', '0', '0', 'mms:workorder:split', '#', 'admin', sysdate(), '工单拆分')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- MPS计划业务操作按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('计划确认', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:mps:list' LIMIT 1) t), 10, '', '', 1, 0, 'F', '0', '0', 'mms:mps:confirm', '#', 'admin', sysdate(), '计划确认')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('计划审批', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:mps:list' LIMIT 1) t), 11, '', '', 1, 0, 'F', '0', '0', 'mms:mps:approve', '#', 'admin', sysdate(), '计划审批')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('计划发布', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:mps:list' LIMIT 1) t), 12, '', '', 1, 0, 'F', '0', '0', 'mms:mps:release', '#', 'admin', sysdate(), '计划发布')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('计划取消', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:mps:list' LIMIT 1) t), 13, '', '', 1, 0, 'F', '0', '0', 'mms:mps:cancel', '#', 'admin', sysdate(), '计划取消')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 报工审核按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('报工审核', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:report:list' LIMIT 1) t), 10, '', '', 1, 0, 'F', '0', '0', 'mms:report:audit', '#', 'admin', sysdate(), '报工审核')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 异常响应/处理按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('异常响应', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:abnormal:list' LIMIT 1) t), 10, '', '', 1, 0, 'F', '0', '0', 'mms:abnormal:respond', '#', 'admin', sysdate(), '异常响应')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('异常处理关闭', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:abnormal:list' LIMIT 1) t), 11, '', '', 1, 0, 'F', '0', '0', 'mms:abnormal:resolve', '#', 'admin', sysdate(), '异常处理关闭')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 齐套检查执行按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('齐套检查执行', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:kit:list' LIMIT 1) t), 10, '', '', 1, 0, 'F', '0', '0', 'mms:kit:execute', '#', 'admin', sysdate(), '齐套检查执行')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 生产看板菜单权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('生产看板', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '生产管控' AND menu_type = 'M' LIMIT 1) t), 0, 'dashboard', 'mms/dashboard/index', 1, 0, 'C', '0', '0', 'mms:dashboard:view', 'dashboard', 'admin', sysdate(), '生产运营看板')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);


-- =====================
-- 四、数据库表字段补充
-- 为工单表添加 release_by 和 release_time 字段
-- =====================

-- 检查并添加 release_by 字段
SET @dbname = DATABASE();
SET @tablename = 'mms_work_order';
SET @columnname = 'release_by';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE table_schema = @dbname AND table_name = @tablename AND column_name = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(64) COMMENT ''下达人''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 release_time 字段
SET @columnname = 'release_time';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE table_schema = @dbname AND table_name = @tablename AND column_name = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DATETIME COMMENT ''下达时间''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

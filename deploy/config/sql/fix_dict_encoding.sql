-- 修复编码损坏的 MMS 字典数据
-- 某些旧 SQL 脚本执行时因编码问题导致 dict_label 存储为 ??
-- 此脚本删除坏数据并重新插入正确的 UTF-8 数据

USE `ry-vue`;
SET NAMES utf8mb4;

-- 先删除所有 label 为 ?? 的损坏数据
DELETE FROM sys_dict_data WHERE dict_type LIKE 'mms_%' AND dict_label REGEXP '^[?]+$';

-- 重新插入正确的数据

-- mms_workorder_status
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '新建',       '0', 'mms_workorder_status', '', 'info',    'Y', '0', 'admin', sysdate(), '新建状态'),
(2, '已下达',     '1', 'mms_workorder_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已下达'),
(3, '执行中',     '2', 'mms_workorder_status', '', 'primary', 'N', '0', 'admin', sysdate(), '执行中'),
(4, '报工中',     '3', 'mms_workorder_status', '', 'primary', 'N', '0', 'admin', sysdate(), '报工中'),
(5, '待完工质检', '4', 'mms_workorder_status', '', 'warning', 'N', '0', 'admin', sysdate(), '待完工质检'),
(6, '完工入库',   '5', 'mms_workorder_status', '', 'success', 'N', '0', 'admin', sysdate(), '完工入库'),
(7, '已关闭',     '6', 'mms_workorder_status', '', 'info',    'N', '0', 'admin', sysdate(), '已关闭'),
(8, '已暂停',     '7', 'mms_workorder_status', '', 'warning', 'N', '0', 'admin', sysdate(), '已暂停'),
(9, '已作废',     '8', 'mms_workorder_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已作废')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), list_class = VALUES(list_class);

-- mms_order_type
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '生产', '0', 'mms_order_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '标准生产'),
(2, '返工', '1', 'mms_order_type', '', 'warning', 'N', '0', 'admin', sysdate(), '返工生产'),
(3, '外协', '2', 'mms_order_type', '', 'info',    'N', '0', 'admin', sysdate(), '外协生产')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), list_class = VALUES(list_class);

-- mms_priority
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '高', '0', 'mms_priority', '', 'danger',  'N', '0', 'admin', sysdate(), '高优先级'),
(2, '中', '1', 'mms_priority', '', 'warning', 'Y', '0', 'admin', sysdate(), '中优先级'),
(3, '低', '2', 'mms_priority', '', 'info',    'N', '0', 'admin', sysdate(), '低优先级')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), list_class = VALUES(list_class);

-- mms_mps_status
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '0', 'mms_mps_status', '', 'info',    'Y', '0', 'admin', sysdate(), '草稿'),
(2, '已确认', '1', 'mms_mps_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已确认'),
(3, '已发布', '2', 'mms_mps_status', '', 'success', 'N', '0', 'admin', sysdate(), '已发布'),
(4, '已下达', '3', 'mms_mps_status', '', 'success', 'N', '0', 'admin', sysdate(), '已下达'),
(5, '已取消', '4', 'mms_mps_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已取消')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), list_class = VALUES(list_class);

-- mms_report_status
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待审核', '0', 'mms_report_status', '', 'info',    'Y', '0', 'admin', sysdate(), '待审核'),
(2, '已审核', '1', 'mms_report_status', '', 'success', 'N', '0', 'admin', sysdate(), '已审核'),
(3, '已驳回', '2', 'mms_report_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已驳回')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), list_class = VALUES(list_class);

-- mms_kit_status
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '已通过', '1', 'mms_kit_status', '', 'success', 'Y', '0', 'admin', sysdate(), '已通过'),
(2, '缺料',   '2', 'mms_kit_status', '', 'danger',  'N', '0', 'admin', sysdate(), '缺料')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), list_class = VALUES(list_class);

-- mms_abnormal_type
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '设备', '0', 'mms_abnormal_type', '', 'danger',  'Y', '0', 'admin', sysdate(), '设备异常'),
(2, '物料', '1', 'mms_abnormal_type', '', 'warning', 'N', '0', 'admin', sysdate(), '物料异常'),
(3, '质量', '2', 'mms_abnormal_type', '', 'danger',  'N', '0', 'admin', sysdate(), '质量异常'),
(4, '安全', '3', 'mms_abnormal_type', '', 'danger',  'N', '0', 'admin', sysdate(), '安全异常'),
(5, '其他', '4', 'mms_abnormal_type', '', 'info',    'N', '0', 'admin', sysdate(), '其他异常')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), list_class = VALUES(list_class);

-- mms_bom_type
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '标准BOM', '0', 'mms_bom_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '标准BOM'),
(2, '替代BOM', '1', 'mms_bom_type', '', 'info',    'N', '0', 'admin', sysdate(), '替代BOM'),
(3, '配方',   '2', 'mms_bom_type', '', 'success', 'N', '0', 'admin', sysdate(), '配方'),
(4, '临时BOM', '3', 'mms_bom_type', '', 'warning', 'N', '0', 'admin', sysdate(), '临时BOM')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), list_class = VALUES(list_class);

-- mms_demand_type
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '订单', '0', 'mms_demand_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '订单需求'),
(2, '预测', '1', 'mms_demand_type', '', 'info',    'N', '0', 'admin', sysdate(), '预测需求'),
(3, '补库', '2', 'mms_demand_type', '', 'success', 'N', '0', 'admin', sysdate(), '补库需求'),
(4, '插单', '3', 'mms_demand_type', '', 'danger',  'N', '0', 'admin', sysdate(), '插单需求')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), list_class = VALUES(list_class);

-- mms_process_type
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '加工', '0', 'mms_process_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '加工'),
(2, '装配', '1', 'mms_process_type', '', 'success', 'N', '0', 'admin', sysdate(), '装配'),
(3, '检验', '2', 'mms_process_type', '', 'warning', 'N', '0', 'admin', sysdate(), '检验'),
(4, '包装', '3', 'mms_process_type', '', 'info',    'N', '0', 'admin', sysdate(), '包装')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), list_class = VALUES(list_class);

-- mms_resource_type
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '单设备', '0', 'mms_resource_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '单设备'),
(2, '产线',   '1', 'mms_resource_type', '', 'success', 'N', '0', 'admin', sysdate(), '产线'),
(3, '设备组', '2', 'mms_resource_type', '', 'info',    'N', '0', 'admin', sysdate(), '设备组')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), list_class = VALUES(list_class);

-- mms_qc_type
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '首件', '0', 'mms_qc_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '首件检验'),
(2, '巡检', '1', 'mms_qc_type', '', 'success', 'N', '0', 'admin', sysdate(), '巡检'),
(3, '末件', '2', 'mms_qc_type', '', 'warning', 'N', '0', 'admin', sysdate(), '末件检验')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), list_class = VALUES(list_class);

-- mms_qc_result
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '合格', '0', 'mms_qc_result', '', 'success', 'Y', '0', 'admin', sysdate(), '合格'),
(2, '让步', '1', 'mms_qc_result', '', 'warning', 'N', '0', 'admin', sysdate(), '让步接收'),
(3, '拒收', '2', 'mms_qc_result', '', 'danger',  'N', '0', 'admin', sysdate(), '拒收')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), list_class = VALUES(list_class);

-- mms_yes_no
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '是', '1', 'mms_yes_no', '', 'success', 'Y', '0', 'admin', sysdate(), '是'),
(2, '否', '0', 'mms_yes_no', '', 'danger',  'N', '0', 'admin', sysdate(), '否')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), list_class = VALUES(list_class);

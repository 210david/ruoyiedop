-- ============================================================
-- MMS 生产管控模块 - 字典重复值修复脚本
-- 说明: 清理因多个SQL文件重复插入导致的字典数据重复，
--       统一各字典的 dict_label / list_class，删除废弃字典类型
-- 问题根因:
--   1. mms_menu_dict.sql        — 第一批字典定义（部分标签/值与后续版本不一致）
--   2. mms_production_control_part2/part3.sql — 第二批字典定义（固定 dict_code，无 ON DUPLICATE）
--   3. mms_dict_supplement.sql   — 第三批字典定义（带 ON DUPLICATE KEY UPDATE）
--   4. fix_dict_encoding.sql     — 修复编码损坏的补充脚本
--   以上脚本按不同顺序执行后，sys_dict_data 表中同一 (dict_type, dict_value)
--   可能存在多条记录，且 dict_label / list_class 不一致。
-- 执行时间: 2026-08-17
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 一、清理废弃的字典类型（前端已不使用）
-- ============================================================

-- 1. mms_severity — 已被 mms_abnormal_severity 替代
DELETE FROM sys_dict_data WHERE dict_type = 'mms_severity';
DELETE FROM sys_dict_type WHERE dict_type = 'mms_severity';

-- 2. mms_wo_status — 已被 mms_workorder_status 替代
DELETE FROM sys_dict_data WHERE dict_type = 'mms_wo_status';
DELETE FROM sys_dict_type WHERE dict_type = 'mms_wo_status';

-- 3. mms_wo_type — 已被 mms_order_type 替代
DELETE FROM sys_dict_data WHERE dict_type = 'mms_wo_type';
DELETE FROM sys_dict_type WHERE dict_type = 'mms_wo_type';

-- 4. mms_data_status — 前端统一使用 mms_status
DELETE FROM sys_dict_data WHERE dict_type = 'mms_data_status';
DELETE FROM sys_dict_type WHERE dict_type = 'mms_data_status';

-- ============================================================
-- 二、删除所有 MMS 字典数据后重新插入（确保数据唯一、一致）
-- ============================================================

-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 删除所有 mms_ 前缀的字典数据（保留字典类型，后面只更新数据）
DELETE FROM sys_dict_data WHERE dict_type LIKE 'mms_%';

-- ============================================================
-- 三、确保字典类型存在且名称统一
-- ============================================================

-- mms_bom_type
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'BOM类型', 'mms_bom_type', '0', 'admin', sysdate(), 'BOM清单类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_bom_type');
UPDATE sys_dict_type SET dict_name = 'BOM类型', remark = 'BOM清单类型' WHERE dict_type = 'mms_bom_type';

-- mms_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'MMS通用状态', 'mms_status', '0', 'admin', sysdate(), 'MMS模块通用启用停用状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_status');
UPDATE sys_dict_type SET dict_name = 'MMS通用状态', remark = 'MMS模块通用启用停用状态' WHERE dict_type = 'mms_status';

-- mms_demand_type
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '需求类型', 'mms_demand_type', '0', 'admin', sysdate(), '生产需求类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_demand_type');
UPDATE sys_dict_type SET dict_name = '需求类型', remark = '生产需求类型' WHERE dict_type = 'mms_demand_type';

-- mms_demand_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '需求状态', 'mms_demand_status', '0', 'admin', sysdate(), '生产需求状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_demand_status');
UPDATE sys_dict_type SET dict_name = '需求状态', remark = '生产需求状态' WHERE dict_type = 'mms_demand_status';

-- mms_mps_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'MPS计划状态', 'mms_mps_status', '0', 'admin', sysdate(), '主生产计划状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_mps_status');
UPDATE sys_dict_type SET dict_name = 'MPS计划状态', remark = '主生产计划状态' WHERE dict_type = 'mms_mps_status';

-- mms_kit_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '齐套状态', 'mms_kit_status', '0', 'admin', sysdate(), '齐套检查状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_kit_status');
UPDATE sys_dict_type SET dict_name = '齐套状态', remark = '齐套检查状态' WHERE dict_type = 'mms_kit_status';

-- mms_order_type
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '工单类型', 'mms_order_type', '0', 'admin', sysdate(), '生产工单类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_order_type');
UPDATE sys_dict_type SET dict_name = '工单类型', remark = '生产工单类型' WHERE dict_type = 'mms_order_type';

-- mms_workorder_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '工单状态', 'mms_workorder_status', '0', 'admin', sysdate(), '生产工单状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_workorder_status');
UPDATE sys_dict_type SET dict_name = '工单状态', remark = '生产工单状态' WHERE dict_type = 'mms_workorder_status';

-- mms_process_type
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '工序类型', 'mms_process_type', '0', 'admin', sysdate(), '工序类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_process_type');
UPDATE sys_dict_type SET dict_name = '工序类型', remark = '工序类型' WHERE dict_type = 'mms_process_type';

-- mms_resource_type
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '资源类型', 'mms_resource_type', '0', 'admin', sysdate(), '产能单元类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_resource_type');
UPDATE sys_dict_type SET dict_name = '资源类型', remark = '产能单元类型' WHERE dict_type = 'mms_resource_type';

-- mms_abnormal_type
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '异常类型', 'mms_abnormal_type', '0', 'admin', sysdate(), '生产异常类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_abnormal_type');
UPDATE sys_dict_type SET dict_name = '异常类型', remark = '生产异常类型' WHERE dict_type = 'mms_abnormal_type';

-- mms_abnormal_severity
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '异常严重等级', 'mms_abnormal_severity', '0', 'admin', sysdate(), '异常严重等级'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_abnormal_severity');
UPDATE sys_dict_type SET dict_name = '异常严重等级', remark = '异常严重等级' WHERE dict_type = 'mms_abnormal_severity';

-- mms_abnormal_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '异常状态', 'mms_abnormal_status', '0', 'admin', sysdate(), '异常处理状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_abnormal_status');
UPDATE sys_dict_type SET dict_name = '异常状态', remark = '异常处理状态' WHERE dict_type = 'mms_abnormal_status';

-- mms_qc_type
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '质检类型', 'mms_qc_type', '0', 'admin', sysdate(), '质检检验类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_qc_type');
UPDATE sys_dict_type SET dict_name = '质检类型', remark = '质检检验类型' WHERE dict_type = 'mms_qc_type';

-- mms_qc_result
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '质检结果', 'mms_qc_result', '0', 'admin', sysdate(), '质检结论'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_qc_result');
UPDATE sys_dict_type SET dict_name = '质检结果', remark = '质检结论' WHERE dict_type = 'mms_qc_result';

-- mms_report_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '报工状态', 'mms_report_status', '0', 'admin', sysdate(), '报工记录状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_report_status');
UPDATE sys_dict_type SET dict_name = '报工状态', remark = '报工记录状态' WHERE dict_type = 'mms_report_status';

-- mms_issue_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '领料状态', 'mms_issue_status', '0', 'admin', sysdate(), '领料退料状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_issue_status');
UPDATE sys_dict_type SET dict_name = '领料状态', remark = '领料退料状态' WHERE dict_type = 'mms_issue_status';

-- mms_priority
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '优先级', 'mms_priority', '0', 'admin', sysdate(), '生产优先级'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_priority');
UPDATE sys_dict_type SET dict_name = '优先级', remark = '生产优先级' WHERE dict_type = 'mms_priority';

-- mms_yes_no
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '是否', 'mms_yes_no', '0', 'admin', sysdate(), '是否'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_yes_no');
UPDATE sys_dict_type SET dict_name = '是否', remark = '是否' WHERE dict_type = 'mms_yes_no';

-- mms_shift_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '班次状态', 'mms_shift_status', '0', 'admin', sysdate(), '班次启用停用状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_shift_status');
UPDATE sys_dict_type SET dict_name = '班次状态', remark = '班次启用停用状态' WHERE dict_type = 'mms_shift_status';

-- mms_route_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '工艺路线状态', 'mms_route_status', '0', 'admin', sysdate(), '工艺路线状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_route_status');
UPDATE sys_dict_type SET dict_name = '工艺路线状态', remark = '工艺路线状态' WHERE dict_type = 'mms_route_status';

-- mms_dispatch_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '派工状态', 'mms_dispatch_status', '0', 'admin', sysdate(), '派工单状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_dispatch_status');
UPDATE sys_dict_type SET dict_name = '派工状态', remark = '派工单状态' WHERE dict_type = 'mms_dispatch_status';

-- mms_flowcard_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '流转卡状态', 'mms_flowcard_status', '0', 'admin', sysdate(), '流转卡状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_flowcard_status');
UPDATE sys_dict_type SET dict_name = '流转卡状态', remark = '流转卡状态' WHERE dict_type = 'mms_flowcard_status';

-- mms_calendar_daytype
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '日历日期类型', 'mms_calendar_daytype', '0', 'admin', sysdate(), '生产日历日期类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_calendar_daytype');
UPDATE sys_dict_type SET dict_name = '日历日期类型', remark = '生产日历日期类型' WHERE dict_type = 'mms_calendar_daytype';

-- mms_downtime_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '停机状态', 'mms_downtime_status', '0', 'admin', sysdate(), '设备停机状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_downtime_status');
UPDATE sys_dict_type SET dict_name = '停机状态', remark = '设备停机状态' WHERE dict_type = 'mms_downtime_status';

-- mms_outsource_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '外协状态', 'mms_outsource_status', '0', 'admin', sysdate(), '外协订单状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_outsource_status');
UPDATE sys_dict_type SET dict_name = '外协状态', remark = '外协订单状态' WHERE dict_type = 'mms_outsource_status';

-- ============================================================
-- 四、重新插入统一的字典数据（每个 dict_type + dict_value 唯一）
-- ============================================================

-- BOM类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '标准BOM', '0', 'mms_bom_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '标准BOM'),
(2, '替代BOM', '1', 'mms_bom_type', '', 'info',    'N', '0', 'admin', sysdate(), '替代BOM'),
(3, '配方',   '2', 'mms_bom_type', '', 'success', 'N', '0', 'admin', sysdate(), '配方'),
(4, '临时BOM', '3', 'mms_bom_type', '', 'warning', 'N', '0', 'admin', sysdate(), '临时BOM');

-- MMS通用状态（启用/停用）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '启用', '0', 'mms_status', '', 'success', 'Y', '0', 'admin', sysdate(), '启用'),
(2, '停用', '1', 'mms_status', '', 'danger',  'N', '0', 'admin', sysdate(), '停用');

-- 需求类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '订单', '0', 'mms_demand_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '订单需求'),
(2, '预测', '1', 'mms_demand_type', '', 'info',    'N', '0', 'admin', sysdate(), '预测需求'),
(3, '补库', '2', 'mms_demand_type', '', 'success', 'N', '0', 'admin', sysdate(), '补库需求'),
(4, '插单', '3', 'mms_demand_type', '', 'danger',  'N', '0', 'admin', sysdate(), '插单需求');

-- 需求状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '0', 'mms_demand_status', '', 'info',    'Y', '0', 'admin', sysdate(), '草稿'),
(2, '已确认', '1', 'mms_demand_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已确认'),
(3, '已排产', '2', 'mms_demand_status', '', 'success', 'N', '0', 'admin', sysdate(), '已排产'),
(4, '已取消', '3', 'mms_demand_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已取消');

-- MPS计划状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '0', 'mms_mps_status', '', 'info',    'Y', '0', 'admin', sysdate(), '草稿'),
(2, '已确认', '1', 'mms_mps_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已确认'),
(3, '已发布', '2', 'mms_mps_status', '', 'success', 'N', '0', 'admin', sysdate(), '已发布'),
(4, '已下达', '3', 'mms_mps_status', '', 'success', 'N', '0', 'admin', sysdate(), '已下达'),
(5, '已取消', '4', 'mms_mps_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已取消');

-- 齐套状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '已通过', '1', 'mms_kit_status', '', 'success', 'Y', '0', 'admin', sysdate(), '已通过'),
(2, '缺料',   '2', 'mms_kit_status', '', 'danger',  'N', '0', 'admin', sysdate(), '缺料');

-- 工单类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '生产', '0', 'mms_order_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '标准生产'),
(2, '返工', '1', 'mms_order_type', '', 'warning', 'N', '0', 'admin', sysdate(), '返工生产'),
(3, '外协', '2', 'mms_order_type', '', 'info',    'N', '0', 'admin', sysdate(), '外协生产');

-- 工单状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '新建',       '0', 'mms_workorder_status', '', 'info',    'Y', '0', 'admin', sysdate(), '新建状态'),
(2, '已下达',     '1', 'mms_workorder_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已下达'),
(3, '执行中',     '2', 'mms_workorder_status', '', 'success', 'N', '0', 'admin', sysdate(), '执行中'),
(4, '已完工',     '3', 'mms_workorder_status', '', 'success', 'N', '0', 'admin', sysdate(), '已完工'),
(5, '已关闭',     '4', 'mms_workorder_status', '', 'info',    'N', '0', 'admin', sysdate(), '已关闭'),
(6, '已暂停',     '5', 'mms_workorder_status', '', 'warning', 'N', '0', 'admin', sysdate(), '已暂停'),
(7, '已作废',     '6', 'mms_workorder_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已作废');

-- 工序类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '加工', '0', 'mms_process_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '加工'),
(2, '装配', '1', 'mms_process_type', '', 'success', 'N', '0', 'admin', sysdate(), '装配'),
(3, '检验', '2', 'mms_process_type', '', 'warning', 'N', '0', 'admin', sysdate(), '检验'),
(4, '包装', '3', 'mms_process_type', '', 'info',    'N', '0', 'admin', sysdate(), '包装');

-- 资源类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '单设备', '0', 'mms_resource_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '单设备'),
(2, '产线',   '1', 'mms_resource_type', '', 'success', 'N', '0', 'admin', sysdate(), '产线'),
(3, '设备组', '2', 'mms_resource_type', '', 'info',    'N', '0', 'admin', sysdate(), '设备组');

-- 异常类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '设备', '0', 'mms_abnormal_type', '', 'danger',  'Y', '0', 'admin', sysdate(), '设备异常'),
(2, '物料', '1', 'mms_abnormal_type', '', 'warning', 'N', '0', 'admin', sysdate(), '物料异常'),
(3, '质量', '2', 'mms_abnormal_type', '', 'danger',  'N', '0', 'admin', sysdate(), '质量异常'),
(4, '安全', '3', 'mms_abnormal_type', '', 'danger',  'N', '0', 'admin', sysdate(), '安全异常'),
(5, '其他', '4', 'mms_abnormal_type', '', 'info',    'N', '0', 'admin', sysdate(), '其他异常');

-- 异常严重等级
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '一般', '0', 'mms_abnormal_severity', '', 'info',    'Y', '0', 'admin', sysdate(), '一般'),
(2, '严重', '1', 'mms_abnormal_severity', '', 'warning', 'N', '0', 'admin', sysdate(), '严重'),
(3, '紧急', '2', 'mms_abnormal_severity', '', 'danger',  'N', '0', 'admin', sysdate(), '紧急');

-- 异常状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待响应', '0', 'mms_abnormal_status', '', 'danger',  'Y', '0', 'admin', sysdate(), '待响应'),
(2, '处理中', '1', 'mms_abnormal_status', '', 'warning', 'N', '0', 'admin', sysdate(), '处理中'),
(3, '已关闭', '2', 'mms_abnormal_status', '', 'success', 'N', '0', 'admin', sysdate(), '已关闭');

-- 质检类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '首件', '0', 'mms_qc_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '首件检验'),
(2, '巡检', '1', 'mms_qc_type', '', 'success', 'N', '0', 'admin', sysdate(), '巡检'),
(3, '末件', '2', 'mms_qc_type', '', 'warning', 'N', '0', 'admin', sysdate(), '末件检验');

-- 质检结果
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '合格', '0', 'mms_qc_result', '', 'success', 'Y', '0', 'admin', sysdate(), '合格'),
(2, '让步', '1', 'mms_qc_result', '', 'warning', 'N', '0', 'admin', sysdate(), '让步接收'),
(3, '拒收', '2', 'mms_qc_result', '', 'danger',  'N', '0', 'admin', sysdate(), '拒收');

-- 报工状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待审核', '0', 'mms_report_status', '', 'info',    'Y', '0', 'admin', sysdate(), '待审核'),
(2, '已审核', '1', 'mms_report_status', '', 'success', 'N', '0', 'admin', sysdate(), '已审核'),
(3, '已驳回', '2', 'mms_report_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已驳回');

-- 领料状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待领料', '0', 'mms_issue_status', '', 'info',    'Y', '0', 'admin', sysdate(), '待领料'),
(2, '已领料', '1', 'mms_issue_status', '', 'success', 'N', '0', 'admin', sysdate(), '已领料');

-- 优先级
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '高', '0', 'mms_priority', '', 'danger',  'N', '0', 'admin', sysdate(), '高优先级'),
(2, '中', '1', 'mms_priority', '', 'warning', 'Y', '0', 'admin', sysdate(), '中优先级'),
(3, '低', '2', 'mms_priority', '', 'info',    'N', '0', 'admin', sysdate(), '低优先级');

-- 是否
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '是', '1', 'mms_yes_no', '', 'success', 'Y', '0', 'admin', sysdate(), '是'),
(2, '否', '0', 'mms_yes_no', '', 'danger',  'N', '0', 'admin', sysdate(), '否');

-- 班次状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '0', 'mms_shift_status', '', 'info',    'Y', '0', 'admin', sysdate(), '草稿'),
(2, '已启用', '1', 'mms_shift_status', '', 'success', 'N', '0', 'admin', sysdate(), '已启用'),
(3, '已停用', '2', 'mms_shift_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已停用');

-- 工艺路线状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '0', 'mms_route_status', '', 'info',    'Y', '0', 'admin', sysdate(), '草稿'),
(2, '待审核', '1', 'mms_route_status', '', 'primary', 'N', '0', 'admin', sysdate(), '待审核'),
(3, '已审核', '2', 'mms_route_status', '', 'success', 'N', '0', 'admin', sysdate(), '已审核'),
(4, '已停用', '3', 'mms_route_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已停用');

-- 派工状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待开工', '0', 'mms_dispatch_status', '', 'warning', 'Y', '0', 'admin', sysdate(), '待开工'),
(2, '进行中', '1', 'mms_dispatch_status', '', 'primary', 'N', '0', 'admin', sysdate(), '进行中'),
(3, '已完成', '2', 'mms_dispatch_status', '', 'success', 'N', '0', 'admin', sysdate(), '已完成'),
(4, '已取消', '3', 'mms_dispatch_status', '', 'info',    'N', '0', 'admin', sysdate(), '已取消');

-- 流转卡状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '在制', '0', 'mms_flowcard_status', '', 'primary', 'Y', '0', 'admin', sysdate(), '在制'),
(2, '完工', '1', 'mms_flowcard_status', '', 'success', 'N', '0', 'admin', sysdate(), '完工'),
(3, '报废', '2', 'mms_flowcard_status', '', 'danger',  'N', '0', 'admin', sysdate(), '报废');

-- 日历日期类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '工作日', '0', 'mms_calendar_daytype', '', 'success', 'Y', '0', 'admin', sysdate(), '工作日'),
(2, '周末',   '1', 'mms_calendar_daytype', '', 'warning', 'N', '0', 'admin', sysdate(), '周末'),
(3, '节假日', '2', 'mms_calendar_daytype', '', 'danger',  'N', '0', 'admin', sysdate(), '节假日');

-- 停机状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '计划停机', '0', 'mms_downtime_status', '', 'info',    'Y', '0', 'admin', sysdate(), '计划停机'),
(2, '故障停机', '1', 'mms_downtime_status', '', 'danger',  'N', '0', 'admin', sysdate(), '故障停机'),
(3, '已恢复',   '2', 'mms_downtime_status', '', 'success', 'N', '0', 'admin', sysdate(), '已恢复');

-- 外协状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待外协', '0', 'mms_outsource_status', '', 'info',    'Y', '0', 'admin', sysdate(), '待外协'),
(2, '外协中', '1', 'mms_outsource_status', '', 'primary', 'N', '0', 'admin', sysdate(), '外协中'),
(3, '已完成', '2', 'mms_outsource_status', '', 'success', 'N', '0', 'admin', sysdate(), '已完成');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 五、验证：检查是否仍有重复
-- ============================================================
-- 查找同一 (dict_type, dict_value) 有多条记录的情况
-- SELECT dict_type, dict_value, COUNT(*) AS cnt
-- FROM sys_dict_data
-- WHERE dict_type LIKE 'mms_%'
-- GROUP BY dict_type, dict_value
-- HAVING COUNT(*) > 1
-- ORDER BY dict_type, dict_value;

-- 查看各字典类型的数据条数
-- SELECT dict_type, COUNT(*) AS cnt
-- FROM sys_dict_data
-- WHERE dict_type LIKE 'mms_%'
-- GROUP BY dict_type
-- ORDER BY dict_type;

-- ============================================================
-- 六、清除 Redis 字典缓存
-- 说明：RuoYi 默认会将字典缓存到 Redis，执行完以上 SQL 后
--       需要清除 Redis 中的 sys_dict:* 缓存，或在系统管理 >
--       字典管理页面点击"刷新缓存"按钮。
-- ============================================================
-- redis-cli KEYS "sys_dict:mms_*" | xargs redis-cli DEL
-- 或者直接清除所有字典缓存：
-- redis-cli KEYS "sys_dict:*" | xargs redis-cli DEL
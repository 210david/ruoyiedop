-- ============================================================
-- MMS 生产管控模块 - 字典数据 + 编号规则
-- Part 3
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- 字典数据（继续Part2的末尾）
INSERT INTO `sys_dict_data`(`dict_code`,`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
VALUES
-- BOM类型
(2001, 1, '标准BOM', '0', 'mms_bom_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, '标准BOM'),
(2002, 2, '替代BOM', '1', 'mms_bom_type', '', 'info', 'N', '0', 'admin', sysdate(), '', NULL, '替代BOM'),
(2003, 3, '配方',   '2', 'mms_bom_type', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, '配方'),
(2004, 4, '临时BOM', '3', 'mms_bom_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '临时BOM'),
-- 需求类型
(2011, 1, '订单', '0', 'mms_demand_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, '订单需求'),
(2012, 2, '预测', '1', 'mms_demand_type', '', 'info', 'N', '0', 'admin', sysdate(), '', NULL, '预测需求'),
(2013, 3, '补库', '2', 'mms_demand_type', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, '补库需求'),
(2014, 4, '插单', '3', 'mms_demand_type', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '插单需求'),
-- 需求状态
(2021, 1, '草稿',   '0', 'mms_demand_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', NULL, '草稿'),
(2022, 2, '已确认', '1', 'mms_demand_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', NULL, '已确认'),
(2023, 3, '已排产', '2', 'mms_demand_status', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, '已排产'),
(2024, 4, '已取消', '3', 'mms_demand_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '已取消'),
-- 计划状态
(2031, 1, '草稿',   '0', 'mms_mps_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', NULL, '草稿'),
(2032, 2, '已确认', '1', 'mms_mps_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', NULL, '已确认'),
(2033, 3, '已发布', '2', 'mms_mps_status', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, '已发布'),
(2034, 4, '已取消', '3', 'mms_mps_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '已取消'),
-- 齐套状态
(2041, 1, '已通过', '1', 'mms_kit_status', '', 'success', 'Y', '0', 'admin', sysdate(), '', NULL, '已通过'),
(2042, 2, '缺料',   '2', 'mms_kit_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '缺料'),
-- 工单类型
(2051, 1, '生产', '0', 'mms_order_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, '生产工单'),
(2052, 2, '返工', '1', 'mms_order_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '返工工单'),
(2053, 3, '外协', '2', 'mms_order_type', '', 'info', 'N', '0', 'admin', sysdate(), '', NULL, '外协工单'),
-- 工单状态
(2061, 1, '新建',       '0', 'mms_workorder_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', NULL, '新建'),
(2062, 2, '已下达',     '1', 'mms_workorder_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', NULL, '已下达'),
(2063, 3, '执行中',     '2', 'mms_workorder_status', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, '执行中'),
(2064, 4, '已完工',     '3', 'mms_workorder_status', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, '已完工'),
(2065, 5, '已关闭',     '4', 'mms_workorder_status', '', 'info', 'N', '0', 'admin', sysdate(), '', NULL, '已关闭'),
(2066, 6, '已暂停',     '5', 'mms_workorder_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '已暂停'),
(2067, 7, '已作废',     '6', 'mms_workorder_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '已作废'),
-- 工序类型
(2071, 1, '加工', '0', 'mms_process_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, '加工'),
(2072, 2, '装配', '1', 'mms_process_type', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, '装配'),
(2073, 3, '检验', '2', 'mms_process_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '检验'),
(2074, 4, '包装', '3', 'mms_process_type', '', 'info', 'N', '0', 'admin', sysdate(), '', NULL, '包装'),
-- 产能单元类型
(2081, 1, '单设备', '0', 'mms_resource_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, '单设备'),
(2082, 2, '产线',   '1', 'mms_resource_type', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, '产线'),
(2083, 3, '设备组', '2', 'mms_resource_type', '', 'info', 'N', '0', 'admin', sysdate(), '', NULL, '设备组'),
-- 异常类型
(2091, 1, '设备', '0', 'mms_abnormal_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, '设备异常'),
(2092, 2, '物料', '1', 'mms_abnormal_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '物料异常'),
(2093, 3, '质量', '2', 'mms_abnormal_type', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '质量异常'),
(2094, 4, '安全', '3', 'mms_abnormal_type', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '安全异常'),
(2095, 5, '其他', '4', 'mms_abnormal_type', '', 'info', 'N', '0', 'admin', sysdate(), '', NULL, '其他异常'),
-- 异常等级
(2101, 1, '一般', '0', 'mms_severity', '', 'info', 'Y', '0', 'admin', sysdate(), '', NULL, '一般'),
(2102, 2, '严重', '1', 'mms_severity', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '严重'),
(2103, 3, '紧急', '2', 'mms_severity', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '紧急'),
-- 质检类型
(2111, 1, '首件', '0', 'mms_qc_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, '首件检验'),
(2112, 2, '巡检', '1', 'mms_qc_type', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, '巡检'),
(2113, 3, '末件', '2', 'mms_qc_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '末件检验'),
-- 质检结论
(2121, 1, '合格', '0', 'mms_qc_result', '', 'success', 'Y', '0', 'admin', sysdate(), '', NULL, '合格'),
(2122, 2, '让步', '1', 'mms_qc_result', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '让步接收'),
(2123, 3, '拒收', '2', 'mms_qc_result', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '拒收'),
-- 报工状态
(2131, 1, '待审核', '0', 'mms_report_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', NULL, '待审核'),
(2132, 2, '已审核', '1', 'mms_report_status', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, '已审核'),
(2133, 3, '已驳回', '2', 'mms_report_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '已驳回'),
-- 领料状态
(2141, 1, '待领料', '0', 'mms_issue_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', NULL, '待领料'),
(2142, 2, '已领料', '1', 'mms_issue_status', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, '已领料'),
-- 优先级
(2151, 1, '高', '0', 'mms_priority', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '高优先级'),
(2152, 2, '中', '1', 'mms_priority', '', 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, '中优先级'),
(2153, 3, '低', '2', 'mms_priority', '', 'info', 'N', '0', 'admin', sysdate(), '', NULL, '低优先级'),
-- 数据状态
(2161, 1, '启用', '0', 'mms_data_status', '', 'success', 'Y', '0', 'admin', sysdate(), '', NULL, '启用'),
(2162, 2, '停用', '1', 'mms_data_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '停用');

-- ============================================================
-- 六、编号规则
-- ============================================================
INSERT INTO `mk_number_rule`(`rule_id`,`rule_code`,`rule_name`,`prefix`,`date_pattern`,`seq_length`,`reset_cycle`,`status`,`remark`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`)
VALUES
(NULL, 'mms_bom',       'BOM编号',       'BOM',  'yyyyMMdd', 4, 'day',   '0', 'BOM编号规则',   '0', 'admin', sysdate(), '', NULL),
(NULL, 'mms_route',     '工艺路线编号',   'RT',   'yyyyMMdd', 4, 'day',   '0', '工艺路线编号', '0', 'admin', sysdate(), '', NULL),
(NULL, 'mms_process',   '工序编号',       'PRC',  'yyyyMMdd', 4, 'day',   '0', '工序编号规则', '0', 'admin', sysdate(), '', NULL),
(NULL, 'mms_resource',  '产能单元编号',   'RES',  'yyyyMMdd', 4, 'day',   '0', '产能单元编号', '0', 'admin', sysdate(), '', NULL),
(NULL, 'mms_shift',     '班次编号',       'SFT',  'yyyyMMdd', 4, 'day',   '0', '班次编号规则', '0', 'admin', sysdate(), '', NULL),
(NULL, 'mms_demand',    '生产需求编号',   'DMD',  'yyyyMMdd', 4, 'day',   '0', '生产需求编号', '0', 'admin', sysdate(), '', NULL),
(NULL, 'mms_mps',       '主生产计划编号', 'MPS',  'yyyyMMdd', 4, 'day',   '0', '主计划编号',   '0', 'admin', sysdate(), '', NULL),
(NULL, 'mms_kit',       '齐套检查编号',   'KIT',  'yyyyMMdd', 4, 'day',   '0', '齐套检查编号', '0', 'admin', sysdate(), '', NULL),
(NULL, 'mms_workorder', '生产工单编号',   'WO',   'yyyyMMdd', 4, 'day',   '0', '工单编号规则', '0', 'admin', sysdate(), '', NULL),
(NULL, 'mms_issue',     '领料单编号',     'ISS',  'yyyyMMdd', 4, 'day',   '0', '领料单编号',   '0', 'admin', sysdate(), '', NULL),
(NULL, 'mms_return',    '退料单编号',     'RTN',  'yyyyMMdd', 4, 'day',   '0', '退料单编号',   '0', 'admin', sysdate(), '', NULL),
(NULL, 'mms_report',    '报工单编号',     'RPT',  'yyyyMMdd', 4, 'day',   '0', '报工单编号',   '0', 'admin', sysdate(), '', NULL),
(NULL, 'mms_qc',        '质检单编号',     'QC',   'yyyyMMdd', 4, 'day',   '0', '质检单编号',   '0', 'admin', sysdate(), '', NULL),
(NULL, 'mms_abnormal',  '异常单编号',     'ABN',  'yyyyMMdd', 4, 'day',   '0', '异常单编号',   '0', 'admin', sysdate(), '', NULL),
(NULL, 'mms_finish',    '完工入库编号',   'FIN',  'yyyyMMdd', 4, 'day',   '0', '完工入库编号', '0', 'admin', sysdate(), '', NULL);

-- ============================================================
-- 七、角色分类（追加生产管控）
-- ============================================================
INSERT INTO `sys_dict_data`(`dict_code`,`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
VALUES (33, 6, '生产管控', 'mms', 'sys_role_category', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, '生产管控模块角色');

SET FOREIGN_KEY_CHECKS = 1;

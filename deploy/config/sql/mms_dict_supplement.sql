-- ============================================================
-- MMS 生产管控模块 - 字典数据补充完善
-- 说明: 补充生产管控模块各页面使用的字典数据
--       修复页面字典显示为 ?? 的问题
-- 执行时间: 2026-08-16
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 一、缺失的字典类型 + 字典数据（全新补充）
-- ============================================================

-- 1. 班次状态字典（shift 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '班次状态', 'mms_shift_status', '0', 'admin', sysdate(), '班次启用停用状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_shift_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '0', 'mms_shift_status', '', 'info',    'Y', '0', 'admin', sysdate(), '草稿'),
(2, '已启用', '1', 'mms_shift_status', '', 'success', 'N', '0', 'admin', sysdate(), '已启用'),
(3, '已停用', '2', 'mms_shift_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已停用')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 2. 工艺路线状态字典（route 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '工艺路线状态', 'mms_route_status', '0', 'admin', sysdate(), '工艺路线状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_route_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '0', 'mms_route_status', '', 'info',    'Y', '0', 'admin', sysdate(), '草稿'),
(2, '待审核', '1', 'mms_route_status', '', 'primary', 'N', '0', 'admin', sysdate(), '待审核'),
(3, '已审核', '2', 'mms_route_status', '', 'success', 'N', '0', 'admin', sysdate(), '已审核'),
(4, '已停用', '3', 'mms_route_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已停用')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 3. MMS通用状态字典（bom/process/resource/finish 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'MMS通用状态', 'mms_status', '0', 'admin', sysdate(), 'MMS模块通用启用停用状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '启用', '0', 'mms_status', '', 'success', 'Y', '0', 'admin', sysdate(), '启用'),
(2, '停用', '1', 'mms_status', '', 'danger',  'N', '0', 'admin', sysdate(), '停用')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 4. 异常状态字典（abnormal 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '异常状态', 'mms_abnormal_status', '0', 'admin', sysdate(), '异常处理状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_abnormal_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待响应', '0', 'mms_abnormal_status', '', 'danger',  'Y', '0', 'admin', sysdate(), '待响应'),
(2, '处理中', '1', 'mms_abnormal_status', '', 'warning', 'N', '0', 'admin', sysdate(), '处理中'),
(3, '已关闭', '2', 'mms_abnormal_status', '', 'success', 'N', '0', 'admin', sysdate(), '已关闭')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 5. 异常严重等级字典（abnormal 页面使用，修正 mms_severity → mms_abnormal_severity）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '异常严重等级', 'mms_abnormal_severity', '0', 'admin', sysdate(), '异常严重等级'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_abnormal_severity');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '一般', '0', 'mms_abnormal_severity', '', 'info',    'Y', '0', 'admin', sysdate(), '一般'),
(2, '严重', '1', 'mms_abnormal_severity', '', 'warning', 'N', '0', 'admin', sysdate(), '严重'),
(3, '紧急', '2', 'mms_abnormal_severity', '', 'danger',  'N', '0', 'admin', sysdate(), '紧急')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 6. 是否字典（report/kit 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '是否', 'mms_yes_no', '0', 'admin', sysdate(), '是否'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_yes_no');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '是', '1', 'mms_yes_no', '', 'success', 'Y', '0', 'admin', sysdate(), '是'),
(2, '否', '0', 'mms_yes_no', '', 'danger',  'N', '0', 'admin', sysdate(), '否')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 7. 领料状态字典（issue/return 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '领料状态', 'mms_issue_status', '0', 'admin', sysdate(), '领料退料状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_issue_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待领料', '0', 'mms_issue_status', '', 'info',    'Y', '0', 'admin', sysdate(), '待领料'),
(2, '已领料', '1', 'mms_issue_status', '', 'success', 'N', '0', 'admin', sysdate(), '已领料'),
(3, '已退料', '2', 'mms_issue_status', '', 'warning', 'N', '0', 'admin', sysdate(), '已退料')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 8. 需求状态字典（demand 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '需求状态', 'mms_demand_status', '0', 'admin', sysdate(), '生产需求状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_demand_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '0', 'mms_demand_status', '', 'info',    'Y', '0', 'admin', sysdate(), '草稿'),
(2, '已确认', '1', 'mms_demand_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已确认'),
(3, '已排产', '2', 'mms_demand_status', '', 'success', 'N', '0', 'admin', sysdate(), '已排产'),
(4, '已取消', '3', 'mms_demand_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已取消')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 9. 需求类型字典（demand 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '需求类型', 'mms_demand_type', '0', 'admin', sysdate(), '生产需求类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_demand_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '订单', '0', 'mms_demand_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '订单需求'),
(2, '预测', '1', 'mms_demand_type', '', 'info',    'N', '0', 'admin', sysdate(), '预测需求'),
(3, '补库', '2', 'mms_demand_type', '', 'success', 'N', '0', 'admin', sysdate(), '补库需求'),
(4, '插单', '3', 'mms_demand_type', '', 'danger',  'N', '0', 'admin', sysdate(), '插单需求')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 10. BOM类型字典（bom 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'BOM类型', 'mms_bom_type', '0', 'admin', sysdate(), 'BOM清单类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_bom_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '标准BOM', '0', 'mms_bom_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '标准BOM'),
(2, '替代BOM', '1', 'mms_bom_type', '', 'info',    'N', '0', 'admin', sysdate(), '替代BOM'),
(3, '配方',   '2', 'mms_bom_type', '', 'success', 'N', '0', 'admin', sysdate(), '配方'),
(4, '临时BOM', '3', 'mms_bom_type', '', 'warning', 'N', '0', 'admin', sysdate(), '临时BOM')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 11. 工序类型字典（process 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '工序类型', 'mms_process_type', '0', 'admin', sysdate(), '工序类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_process_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '加工', '0', 'mms_process_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '加工'),
(2, '装配', '1', 'mms_process_type', '', 'success', 'N', '0', 'admin', sysdate(), '装配'),
(3, '检验', '2', 'mms_process_type', '', 'warning', 'N', '0', 'admin', sysdate(), '检验'),
(4, '包装', '3', 'mms_process_type', '', 'info',    'N', '0', 'admin', sysdate(), '包装')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 12. 资源类型字典（resource 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '资源类型', 'mms_resource_type', '0', 'admin', sysdate(), '产能单元类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_resource_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '单设备', '0', 'mms_resource_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '单设备'),
(2, '产线',   '1', 'mms_resource_type', '', 'success', 'N', '0', 'admin', sysdate(), '产线'),
(3, '设备组', '2', 'mms_resource_type', '', 'info',    'N', '0', 'admin', sysdate(), '设备组')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 13. 质检类型字典（qc 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '质检类型', 'mms_qc_type', '0', 'admin', sysdate(), '质检检验类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_qc_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '首件', '0', 'mms_qc_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '首件检验'),
(2, '巡检', '1', 'mms_qc_type', '', 'success', 'N', '0', 'admin', sysdate(), '巡检'),
(3, '末件', '2', 'mms_qc_type', '', 'warning', 'N', '0', 'admin', sysdate(), '末件检验')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 14. 质检结果字典（qc 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '质检结果', 'mms_qc_result', '0', 'admin', sysdate(), '质检结论'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_qc_result');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '合格', '0', 'mms_qc_result', '', 'success', 'Y', '0', 'admin', sysdate(), '合格'),
(2, '让步', '1', 'mms_qc_result', '', 'warning', 'N', '0', 'admin', sysdate(), '让步接收'),
(3, '拒收', '2', 'mms_qc_result', '', 'danger',  'N', '0', 'admin', sysdate(), '拒收')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 15. 异常类型字典（abnormal 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '异常类型', 'mms_abnormal_type', '0', 'admin', sysdate(), '生产异常类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_abnormal_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '设备', '0', 'mms_abnormal_type', '', 'danger',  'Y', '0', 'admin', sysdate(), '设备异常'),
(2, '物料', '1', 'mms_abnormal_type', '', 'warning', 'N', '0', 'admin', sysdate(), '物料异常'),
(3, '质量', '2', 'mms_abnormal_type', '', 'danger',  'N', '0', 'admin', sysdate(), '质量异常'),
(4, '安全', '3', 'mms_abnormal_type', '', 'danger',  'N', '0', 'admin', sysdate(), '安全异常'),
(5, '其他', '4', 'mms_abnormal_type', '', 'info',    'N', '0', 'admin', sysdate(), '其他异常')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 16. 报工状态字典（report 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '报工状态', 'mms_report_status', '0', 'admin', sysdate(), '报工记录状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_report_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待审核', '0', 'mms_report_status', '', 'info',    'Y', '0', 'admin', sysdate(), '待审核'),
(2, '已审核', '1', 'mms_report_status', '', 'success', 'N', '0', 'admin', sysdate(), '已审核'),
(3, '已驳回', '2', 'mms_report_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已驳回')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 17. 齐套状态字典（kit 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '齐套状态', 'mms_kit_status', '0', 'admin', sysdate(), '齐套检查状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_kit_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '已通过', '1', 'mms_kit_status', '', 'success', 'Y', '0', 'admin', sysdate(), '已通过'),
(2, '缺料',   '2', 'mms_kit_status', '', 'danger',  'N', '0', 'admin', sysdate(), '缺料')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 18. MPS计划状态字典（mps 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'MPS计划状态', 'mms_mps_status', '0', 'admin', sysdate(), '主生产计划状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_mps_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '0', 'mms_mps_status', '', 'info',    'Y', '0', 'admin', sysdate(), '草稿'),
(2, '已确认', '1', 'mms_mps_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已确认'),
(3, '已发布', '2', 'mms_mps_status', '', 'success', 'N', '0', 'admin', sysdate(), '已发布'),
(4, '已下达', '3', 'mms_mps_status', '', 'success', 'N', '0', 'admin', sysdate(), '已下达'),
(5, '已取消', '4', 'mms_mps_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已取消')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 19. 工单类型字典（workorder 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '工单类型', 'mms_order_type', '0', 'admin', sysdate(), '生产工单类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_order_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '生产', '0', 'mms_order_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '标准生产'),
(2, '返工', '1', 'mms_order_type', '', 'warning', 'N', '0', 'admin', sysdate(), '返工生产'),
(3, '外协', '2', 'mms_order_type', '', 'info',    'N', '0', 'admin', sysdate(), '外协生产')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 20. 工单状态字典（workorder/dashboard 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '工单状态', 'mms_workorder_status', '0', 'admin', sysdate(), '生产工单状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_workorder_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '新建',       '0', 'mms_workorder_status', '', 'info',    'Y', '0', 'admin', sysdate(), '新建状态'),
(2, '已下达',     '1', 'mms_workorder_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已下达'),
(3, '执行中',     '2', 'mms_workorder_status', '', 'success', 'N', '0', 'admin', sysdate(), '执行中'),
(4, '已完工',     '3', 'mms_workorder_status', '', 'success', 'N', '0', 'admin', sysdate(), '已完工'),
(5, '已关闭',     '4', 'mms_workorder_status', '', 'info',    'N', '0', 'admin', sysdate(), '已关闭'),
(6, '已暂停',     '5', 'mms_workorder_status', '', 'warning', 'N', '0', 'admin', sysdate(), '已暂停'),
(7, '已作废',     '6', 'mms_workorder_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已作废')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 21. 优先级字典（workorder/demand/mps 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '优先级', 'mms_priority', '0', 'admin', sysdate(), '生产优先级'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_priority');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '高', '0', 'mms_priority', '', 'danger',  'N', '0', 'admin', sysdate(), '高优先级'),
(2, '中', '1', 'mms_priority', '', 'warning', 'Y', '0', 'admin', sysdate(), '中优先级'),
(3, '低', '2', 'mms_priority', '', 'info',    'N', '0', 'admin', sysdate(), '低优先级')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- ============================================================
-- 二、清理 Redis 字典缓存（如有需要）
-- 说明：RuoYi 默认会将字典缓存到 Redis，执行完以上 SQL 后
--       需要清除 Redis 中的 sys_dict:* 缓存，或在系统管理 >
--       字典管理页面点击"刷新缓存"按钮。
-- ============================================================
-- 可选：通过 Redis CLI 清除字典缓存
-- redis-cli DEL sys_dict:mms_shift_status
-- redis-cli DEL sys_dict:mms_route_status
-- redis-cli DEL sys_dict:mms_status
-- redis-cli DEL sys_dict:mms_abnormal_status
-- redis-cli DEL sys_dict:mms_abnormal_severity
-- redis-cli DEL sys_dict:mms_yes_no
-- redis-cli DEL sys_dict:mms_issue_status
-- redis-cli DEL sys_dict:mms_demand_status
-- redis-cli DEL sys_dict:mms_demand_type
-- redis-cli DEL sys_dict:mms_bom_type
-- redis-cli DEL sys_dict:mms_process_type
-- redis-cli DEL sys_dict:mms_resource_type
-- redis-cli DEL sys_dict:mms_qc_type
-- redis-cli DEL sys_dict:mms_qc_result
-- redis-cli DEL sys_dict:mms_abnormal_type
-- redis-cli DEL sys_dict:mms_report_status
-- redis-cli DEL sys_dict:mms_kit_status
-- redis-cli DEL sys_dict:mms_mps_status
-- redis-cli DEL sys_dict:mms_order_type
-- redis-cli DEL sys_dict:mms_workorder_status
-- redis-cli DEL sys_dict:mms_priority
-- 或者直接清除所有字典缓存：
-- redis-cli KEYS "sys_dict:*" | xargs redis-cli DEL

-- ============================================================
-- 三、派工管理 & 流转卡字典补充
-- ============================================================

-- 22. 派工状态字典（dispatch 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '派工状态', 'mms_dispatch_status', '0', 'admin', sysdate(), '派工单状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_dispatch_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待开工', '0', 'mms_dispatch_status', '', 'warning', 'Y', '0', 'admin', sysdate(), '待开工'),
(2, '进行中', '1', 'mms_dispatch_status', '', 'primary', 'N', '0', 'admin', sysdate(), '进行中'),
(3, '已完成', '2', 'mms_dispatch_status', '', 'success', 'N', '0', 'admin', sysdate(), '已完成'),
(4, '已取消', '3', 'mms_dispatch_status', '', 'info',    'N', '0', 'admin', sysdate(), '已取消')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 23. 流转卡状态字典（flowcard 页面使用）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '流转卡状态', 'mms_flowcard_status', '0', 'admin', sysdate(), '流转卡状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_flowcard_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '在制', '0', 'mms_flowcard_status', '', 'primary', 'Y', '0', 'admin', sysdate(), '在制'),
(2, '完工', '1', 'mms_flowcard_status', '', 'success', 'N', '0', 'admin', sysdate(), '完工'),
(3, '报废', '2', 'mms_flowcard_status', '', 'danger',  'N', '0', 'admin', sysdate(), '报废')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 清除新增字典的 Redis 缓存
-- redis-cli DEL sys_dict:mms_dispatch_status
-- redis-cli DEL sys_dict:mms_flowcard_status

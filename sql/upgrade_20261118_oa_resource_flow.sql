-- ============================================================
-- 综合办公：资源台账与流程联动升级
-- 1) 公车使用申请「指派车辆」「指派驾驶员」改为台账选择器
-- 2) 司机、印章状态字典增加“占用”值（车辆复用已有的“使用中”）
-- 执行日期：2026-11-18
-- ============================================================

-- ---------- 1. 公车使用申请表单字段 ----------
-- 指派车辆：文本 -> 车辆台账选择器（oaVehicle）
UPDATE flow_form
SET fields_json = REPLACE(
        fields_json,
        '"field":"assigned_vehicle","label":"指派车辆","form":"text","dtype":"text","length":100,"scale":2,"table":"main","order":20,"placeholder":"调度节点填写，车牌号"',
        '"field":"assigned_vehicle","label":"指派车辆","form":"browse","browseType":"oaVehicle","browseMultiple":false,"dtype":"text","length":200,"scale":2,"table":"main","order":20,"placeholder":"请选择车辆"'
    )
WHERE form_key = 'bus_use_apply';

-- 指派驾驶员：系统用户选择器 -> 司机台账选择器（oaDriver）
UPDATE flow_form
SET fields_json = REPLACE(
        fields_json,
        '"field":"assigned_driver","label":"指派驾驶员","form":"browse","browseType":"user","browseMultiple":false,"dtype":"text","length":1000,"scale":2,"table":"main","order":21,"placeholder":"调度节点填写"',
        '"field":"assigned_driver","label":"指派驾驶员","form":"browse","browseType":"oaDriver","browseMultiple":false,"dtype":"text","length":200,"scale":2,"table":"main","order":21,"placeholder":"请选择司机"'
    )
WHERE form_key = 'bus_use_apply';

-- ---------- 2. 状态字典 ----------
-- 司机：出车中（占用）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
SELECT 4, '出车中', '3', 'oa_driver_status', '', 'primary', 'N', '0', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'oa_driver_status' AND dict_value = '3');

-- 印章：外带使用中（仅携出盖章占用）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
SELECT 4, '外带使用中', '3', 'oa_seal_status', '', 'primary', 'N', '0', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'oa_seal_status' AND dict_value = '3');

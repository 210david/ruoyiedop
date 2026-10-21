-- =====================================================
-- 修复 safety_hazard_status 字典数据
-- 问题：运行库中字典只有6条记录(0-5)，标签错位，
--       缺少"待审批"(value=1)和"审批驳回"(value=2)
--       导致：后端设置hazardStatus=4(整改中)，前端显示为"已闭环"
-- 日期：2026-08-11
-- =====================================================

-- 1. 先删除旧的（错误的）字典数据
DELETE FROM sys_dict_data WHERE dict_type = 'safety_hazard_status';

-- 2. 插入正确的8条字典数据（与后端状态码完全对应）
--    0=草稿, 1=待审批, 2=审批驳回, 3=待整改, 4=整改中, 5=待验收, 6=已闭环, 7=超期未整改
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',         '0', 'safety_hazard_status', '', 'info',    'N', '0', 'admin', NOW(), '草稿状态'),
(2, '待审批',       '1', 'safety_hazard_status', '', 'warning', 'N', '0', 'admin', NOW(), '已提交待审批'),
(3, '审批驳回',     '2', 'safety_hazard_status', '', 'danger',  'N', '0', 'admin', NOW(), '审批不通过'),
(4, '待整改',       '3', 'safety_hazard_status', '', 'primary', 'N', '0', 'admin', NOW(), '审批通过待整改'),
(5, '整改中',       '4', 'safety_hazard_status', '', 'primary', 'N', '0', 'admin', NOW(), '整改进行中/验收不通过退回'),
(6, '待验收',       '5', 'safety_hazard_status', '', 'primary', 'N', '0', 'admin', NOW(), '整改完成待验收'),
(7, '已闭环',       '6', 'safety_hazard_status', '', 'success', 'N', '0', 'admin', NOW(), '验收通过已闭环'),
(8, '超期未整改',   '7', 'safety_hazard_status', '', 'danger',  'N', '0', 'admin', NOW(), '超过整改期限');

-- 3. 修复现有隐患数据中状态值不正确的问题
--    （测试数据基于错误字典创建，状态码与实际业务含义不匹配）

-- 3.1 TEST-HZD-001: verifyResult=1(通过) 但 hazardStatus=4(整改中)，应为 6(已闭环)
UPDATE biz_safety_hazard SET hazard_status = '6' 
WHERE hazard_code = 'TEST-HZD-001' AND verify_result = '1' AND hazard_status = '4';

-- 3.2 TEST-HZD-002: 已有整改反馈(rectifyDesc不为空) 但 hazardStatus=3(待整改)，应为 5(待验收)
UPDATE biz_safety_hazard SET hazard_status = '5' 
WHERE hazard_code = 'TEST-HZD-002' AND rectify_desc IS NOT NULL AND rectify_desc != '' AND hazard_status = '3';

-- 3.3 TEST-HZD-003/004/005: verifyResult=2(不通过) 且 hazardStatus=4(整改中) — 这些是正确的，无需修复
--     （验收不通过 → 退回整改中，符合状态机设计）

-- 4. 验证修复结果
SELECT dict_value, dict_label FROM sys_dict_data 
WHERE dict_type = 'safety_hazard_status' ORDER BY dict_sort;

SELECT hazard_code, hazard_status, verify_result, rectify_desc, remark 
FROM biz_safety_hazard WHERE del_flag = '0' ORDER BY hazard_code;

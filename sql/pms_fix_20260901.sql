-- =====================================================
-- 采购管理模块（ruoyi-pms）缺陷修复 SQL  2026-09-01
-- 缺陷②：采购 6 角色(500~505)未授权消息中心菜单(905404)
-- 缺陷③：资质状态字典 pms_qual_status 缺少 0(待审核)/2(已驳回)
-- 配套代码修复：
--   PmsInquiryServiceImpl.compareInquiry 比价白名单加入状态3
--   PmsSupplierQualificationServiceImpl 新增默认状态0、审核目标校验1/2
--   ruoyi-ui qualification/index.vue 新增默认状态0、操作列审核入口
-- =====================================================

-- 1) 消息中心菜单(905404)及子按钮(905405 消息查询)授权给采购 6 角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (905404, 905405)
WHERE r.role_id IN (500, 501, 502, 503, 504, 505)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm
                  WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

-- 2) 资质状态字典补充：0=待审核、2=已驳回
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待审核', '0', 'pms_qual_status', '', 'warning', 'N', '0', 'admin', NOW(), '采购模块缺陷③修复：审核链路可达'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_qual_status' AND dict_value='0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已驳回', '2', 'pms_qual_status', '', 'danger', 'N', '0', 'admin', NOW(), '采购模块缺陷③修复：审核驳回结果'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_qual_status' AND dict_value='2');

-- 3) 存量待审核语义修正：将历史测试中经 API 以 status='0' 创建、审核已通过的记录保持不变；
--    不批量改写存量有效资质(状态1)，避免影响已生效数据。

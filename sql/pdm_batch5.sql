-- ============================================================
-- PDM 物料申请功能完整清理（pdm_batch5.sql）
-- 说明：物料统一在仓库管理，中小企业场景无独立的物料申请流程。
--       BOM 下发前置校验中"无待确认物料申请"条件已同步移除。
-- 执行时间：2026-10-19
-- ============================================================

-- 1. 删除菜单及角色关联（物料申请目录/页面/按钮）
DELETE FROM sys_role_menu WHERE menu_id IN (11300, 11301, 11302, 11303, 11304, 11305);
DELETE FROM sys_menu WHERE menu_id IN (11300, 11301, 11302, 11303, 11304, 11305);

-- 2. 删除字典（物料申请状态）
DELETE FROM sys_dict_data WHERE dict_type = 'pdm_material_apply_status';
DELETE FROM sys_dict_type WHERE dict_type = 'pdm_material_apply_status';

-- 3. 删除业务表
DROP TABLE IF EXISTS pdm_material_apply;

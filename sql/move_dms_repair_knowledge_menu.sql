-- ============================================================
-- 调整设备管理维修知识库菜单层级（2026-09-18）
--
-- 目标：将【维修知识库】从【AI助手】目录迁移为设备管理直属菜单，
--       放在【数据采集】前面；隐藏迁移后无可见子菜单的AI助手目录。
-- 注意：执行后用户需退出并重新登录，以重新加载动态路由。
-- ============================================================

-- 维修知识库：设备管理直属，排序6。
UPDATE sys_menu
SET parent_id = 3000,
    order_num = 6,
    path = 'repairKnowledge',
    route_name = 'DmsRepairFaq',
    remark = '设备维修知识库，设备管理直属菜单',
    update_by = 'admin',
    update_time = sysdate()
WHERE menu_id = 3100
   OR (component = 'dms/ai/faq/index' AND perms = 'dms:ai:faq:list');

-- 原有后续目录顺延，保持顺序：维修知识库 → 数据采集 → 报表看板。
UPDATE sys_menu SET order_num = 7, update_by = 'admin', update_time = sysdate()
WHERE menu_id = 3005 OR (parent_id = 3000 AND path = 'data');
UPDATE sys_menu SET order_num = 8, update_by = 'admin', update_time = sysdate()
WHERE menu_id = 3006 OR (parent_id = 3000 AND path = 'dashboard');

-- AI助手已无可见业务子菜单，保留历史目录但隐藏。
UPDATE sys_menu
SET order_num = 9,
    visible = '1',
    remark = '历史兼容目录：维修知识库已迁移至设备管理直属菜单，AI配置已统一至AI中心',
    update_by = 'admin',
    update_time = sysdate()
WHERE menu_id = 3007 OR (parent_id = 3000 AND path = 'ai' AND menu_type = 'M');

-- 核验设备管理直属菜单及维修知识库按钮权限层级。
SELECT menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms
FROM sys_menu
WHERE parent_id = 3000 OR menu_id IN (3000, 3100) OR parent_id = 3100
ORDER BY parent_id, order_num, menu_id;

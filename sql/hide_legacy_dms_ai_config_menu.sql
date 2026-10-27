-- ============================================================
-- 隐藏设备管理中的旧 AI 配置入口（2026-09-18）
--
-- 背景：全局模型、密钥、Embedding 与限额配置已统一迁移至
--       【AI中心 → AI配置】，DMS 旧入口功能重复且容易造成误解。
-- 策略：仅隐藏旧菜单，保留 DMS 兼容接口与页面代码，避免影响
--       仍在使用的 dms.ai.* 功能级开关及历史调用。
-- 注意：执行后用户需退出并重新登录，以重新加载动态路由。
-- ============================================================

UPDATE sys_menu
SET visible = '1',
    remark = '历史兼容入口：全局AI配置已统一迁移至AI中心，禁止从菜单展示',
    update_by = 'admin',
    update_time = sysdate()
WHERE component = 'dms/ai/config'
   OR (menu_name = 'AI配置' AND parent_id = 3007 AND perms = 'dms:ai:config');

-- 验证：DMS 旧入口应为 visible=1；AI中心正式入口应保持 visible=0。
SELECT menu_id, menu_name, parent_id, component, visible, status, perms, remark
FROM sys_menu
WHERE component IN ('dms/ai/config', 'ai/config/index')
ORDER BY component;

-- =====================================================
-- 修复脚本：AI配置 菜单 + 权限 + 默认配置项
-- 说明：菜单已存在于数据库(menu_id=3126)，此脚本用于
--       确保 route_name 正确、权限已分配、配置项已预置
-- 执行后必须重新登录才能生效（前端路由在登录时加载）
-- =====================================================

-- 1. 确保 AI配置 菜单存在且 route_name 正确
--    （menu_id=3126，挂在 AI助手目录 3007 下）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3126, 'AI配置', 3007, 2, 'config', 'dms/ai/config', '', 'DmsAiConfig', 1, 0, 'C', '0', '0', 'dms:ai:config', 'setting', 'admin', sysdate(), 'AI大模型参数配置')
ON DUPLICATE KEY UPDATE route_name = 'DmsAiConfig', component = 'dms/ai/config', perms = 'dms:ai:config', status = '0', visible = '0';

-- 2. 确保 admin 角色（role_id=1）拥有 AI配置 菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 3126);

-- 3. 确保 dms.ai.* 系列配置项已预置
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
('AI功能总开关', 'dms.ai.enabled', 'false', 'Y', 'admin', sysdate(), 'AI功能总开关，true开启/false关闭'),
('AI大模型API地址', 'dms.ai.api_url', '', 'Y', 'admin', sysdate(), '兼容OpenAI格式的API地址'),
('AI大模型API Key', 'dms.ai.api_key', '', 'Y', 'admin', sysdate(), '大模型API密钥'),
('AI模型名称', 'dms.ai.model_name', 'deepseek-chat', 'Y', 'admin', sysdate(), '模型名称'),
('智能报修助手开关', 'dms.ai.repair_assistant', 'false', 'Y', 'admin', sysdate(), '智能报修助手功能开关'),
('智能派工推荐开关', 'dms.ai.dispatch_recommend', 'false', 'Y', 'admin', sysdate(), '智能派工推荐功能开关'),
('维修知识问答开关', 'dms.ai.knowledge_qa', 'false', 'Y', 'admin', sysdate(), '维修知识问答功能开关'),
('AI月度调用上限', 'dms.ai.monthly_limit', '1000', 'Y', 'admin', sysdate(), 'AI功能月度调用次数上限')
ON DUPLICATE KEY UPDATE config_name = VALUES(config_name);

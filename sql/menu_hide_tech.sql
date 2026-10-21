-- =====================================================
-- 技术菜单隐藏（菜单分层改造）
-- 说明：将技术性菜单对非管理员用户隐藏，提升非IT人员使用体验
--       使用 sys_menu 表的 visible 字段控制（0=显示 1=隐藏）
-- =====================================================

-- 1. 隐藏"系统工具"目录（父菜单）
UPDATE sys_menu SET visible = '1' WHERE menu_id = 3;

-- 2. 隐藏"代码生成"及其所有子按钮
UPDATE sys_menu SET visible = '1' WHERE menu_id = 116;
UPDATE sys_menu SET visible = '1' WHERE menu_id IN (1055, 1056, 1057, 1058, 1059, 1060);

-- 3. 隐藏"表单构建"
UPDATE sys_menu SET visible = '1' WHERE menu_id = 115;

-- 4. 隐藏"定时任务"及其所有子按钮
UPDATE sys_menu SET visible = '1' WHERE menu_id = 110;
UPDATE sys_menu SET visible = '1' WHERE menu_id IN (1049, 1050, 1051, 1052, 1053, 1054);

-- 5. 隐藏"系统接口"（Swagger，仅开发环境使用）
UPDATE sys_menu SET visible = '1' WHERE menu_id = 117;

-- 6. 隐藏"数据监控"（Druid监控，技术性太强）
UPDATE sys_menu SET visible = '1' WHERE menu_id = 111;

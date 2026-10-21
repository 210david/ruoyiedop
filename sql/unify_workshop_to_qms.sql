-- =============================================
-- 统一车间管理：MMS工厂车间 → QMS车间管理
-- 将生产管控下的"工厂车间"菜单指向质量管理的车间管理页面
-- 两个菜单入口（生产管控/基础数据/工厂车间 + 质量管理/质量主数据/车间管理）共用同一页面
-- =============================================

-- 1. 更新MMS"工厂车间"菜单：component指向QMS车间页面，路径改为workshop（与QMS一致避免重复路由）
--    注意：path需唯一，用 mms-workshop 避免与 QMS 的 workshop 冲突
UPDATE sys_menu 
SET component = 'qms/workshop/index',
    perms = 'qms:workshop:list',
    icon = 'office-building',
    remark = '车间/产线/工位层级管理（共用QMS车间管理）'
WHERE menu_name = '工厂车间';

-- 2. 更新MMS工厂车间的按钮权限，统一指向QMS车间权限
UPDATE sys_menu SET perms = 'qms:workshop:query' WHERE menu_name = '工厂查询' AND parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '工厂车间') AS t);
UPDATE sys_menu SET perms = 'qms:workshop:add' WHERE menu_name = '工厂新增' AND parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '工厂车间') AS t);
UPDATE sys_menu SET perms = 'qms:workshop:edit' WHERE menu_name = '工厂修改' AND parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '工厂车间') AS t);
UPDATE sys_menu SET perms = 'qms:workshop:remove' WHERE menu_name = '工厂删除' AND parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '工厂车间') AS t);

-- 3. QMS车间表补充MMS需要的业务字段（产线类型、节拍、工位类型）
--    这些字段为可选字段，QMS原有功能不受影响
--    使用条件判断，可重复执行
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_workshop' AND COLUMN_NAME = 'line_type') = 0,
    'ALTER TABLE qms_workshop ADD COLUMN line_type VARCHAR(4) DEFAULT NULL COMMENT ''产线类型（0自动化 1半自动 2手工）'' AFTER node_type',
    'SELECT ''line_type already exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_workshop' AND COLUMN_NAME = 'cycle_time') = 0,
    'ALTER TABLE qms_workshop ADD COLUMN cycle_time INT DEFAULT NULL COMMENT ''节拍（秒）'' AFTER line_type',
    'SELECT ''cycle_time already exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_workshop' AND COLUMN_NAME = 'station_type') = 0,
    'ALTER TABLE qms_workshop ADD COLUMN station_type VARCHAR(4) DEFAULT NULL COMMENT ''工位类型（0加工 1检验 2包装 3暂存）'' AFTER cycle_time',
    'SELECT ''station_type already exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4. 修复车间节点类型字典数据（原始数据可能被错误覆盖）
UPDATE sys_dict_data SET dict_label = '车间' WHERE dict_type = 'qms_workshop_node_type' AND dict_value = '1';
UPDATE sys_dict_data SET dict_label = '产线' WHERE dict_type = 'qms_workshop_node_type' AND dict_value = '2';
UPDATE sys_dict_data SET dict_label = '工位' WHERE dict_type = 'qms_workshop_node_type' AND dict_value = '3';
UPDATE sys_dict_data SET dict_label = '检验点' WHERE dict_type = 'qms_workshop_node_type' AND dict_value = '4';

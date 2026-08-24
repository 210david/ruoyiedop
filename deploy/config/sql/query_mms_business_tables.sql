USE `ry-vue`;
-- 查看各表的数据量，确认哪些有数据需要清除
SELECT TABLE_NAME, TABLE_ROWS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'ry-vue' AND TABLE_NAME LIKE 'mms_%'
AND TABLE_NAME NOT IN (
  -- 基础数据表（保留）
  'mms_bom', 'mms_bom_detail',
  'mms_route', 'mms_route_process', 'mms_route_version_log', 'mms_route_audit_log',
  'mms_process', 'mms_resource', 'mms_shift', 'mms_team',
  'mms_factory_calendar', 'mms_factory', 'mms_line', 'mms_station', 'mms_workshop',
  'mms_code_rule'
)
ORDER BY TABLE_NAME;

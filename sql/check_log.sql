SELECT log_id, equipment_id, equipment_code, change_type, old_value, new_value, change_reason, operator_name, create_time
FROM dms_equipment_log
WHERE equipment_id = 5
ORDER BY log_id DESC
LIMIT 20;

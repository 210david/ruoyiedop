SELECT dict_type, HEX(dict_label) AS hex_label, dict_value FROM sys_dict_data WHERE dict_type IN ('mms_schedule_status','mms_schedule_strategy') ORDER BY dict_type, dict_sort;

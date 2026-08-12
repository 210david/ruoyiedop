SELECT rule_id, rule_code, rule_name, prefix, status FROM mk_number_rule WHERE rule_code='safety_training_plan';
SELECT COUNT(*) AS column_count FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule';

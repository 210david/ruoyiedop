UPDATE `pms_contract` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `pms_receive` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `pms_invoice` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `pms_return` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `qms_ncr` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `qms_mr` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `qms_complaint` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `qms_insp_task` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `qms_audit_plan` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `qms_capa` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_hr_employee` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_hr_leave` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_hr_regular` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_hr_transfer` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_hr_entry` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_hr_contract` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_hr_certificate` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_hr_health_check` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_hr_piecework_record` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_hr_social_security` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `wms_inbound_order` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `wms_outbound_order` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `wms_move_order` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `wms_stock_take` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `wms_wave` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `mms_work_order` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `mms_abnormal` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `mms_downtime` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `mms_qc` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `mms_issue` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `mms_return_material` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `mms_outsource` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `mms_finish_receipt` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `mms_work_report` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `mms_dispatch` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `mms_schedule_task` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `mms_mps` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `mms_demand` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `mms_kit_check` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_safety_risk_point` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_safety_incident` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_safety_drill` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_safety_emergency_plan` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_safety_emergency_material` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_safety_training_record` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_safety_training_plan` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
UPDATE `biz_safety_rectify` t JOIN sys_user u ON CONVERT(t.create_by USING utf8mb4)=CONVERT(u.user_name USING utf8mb4) SET t.dept_id=u.dept_id WHERE t.dept_id IS NULL;
DELIMITER $$
DROP TRIGGER IF EXISTS trg_pms_contract_dept;$$
CREATE TRIGGER trg_pms_contract_dept BEFORE INSERT ON `pms_contract` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_pms_receive_dept;$$
CREATE TRIGGER trg_pms_receive_dept BEFORE INSERT ON `pms_receive` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_pms_invoice_dept;$$
CREATE TRIGGER trg_pms_invoice_dept BEFORE INSERT ON `pms_invoice` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_pms_return_dept;$$
CREATE TRIGGER trg_pms_return_dept BEFORE INSERT ON `pms_return` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_qms_ncr_dept;$$
CREATE TRIGGER trg_qms_ncr_dept BEFORE INSERT ON `qms_ncr` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_qms_mr_dept;$$
CREATE TRIGGER trg_qms_mr_dept BEFORE INSERT ON `qms_mr` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_qms_complaint_dept;$$
CREATE TRIGGER trg_qms_complaint_dept BEFORE INSERT ON `qms_complaint` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_qms_insp_task_dept;$$
CREATE TRIGGER trg_qms_insp_task_dept BEFORE INSERT ON `qms_insp_task` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_qms_audit_plan_dept;$$
CREATE TRIGGER trg_qms_audit_plan_dept BEFORE INSERT ON `qms_audit_plan` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_qms_capa_dept;$$
CREATE TRIGGER trg_qms_capa_dept BEFORE INSERT ON `qms_capa` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_hr_employee_dept;$$
CREATE TRIGGER trg_biz_hr_employee_dept BEFORE INSERT ON `biz_hr_employee` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_hr_leave_dept;$$
CREATE TRIGGER trg_biz_hr_leave_dept BEFORE INSERT ON `biz_hr_leave` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_hr_regular_dept;$$
CREATE TRIGGER trg_biz_hr_regular_dept BEFORE INSERT ON `biz_hr_regular` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_hr_transfer_dept;$$
CREATE TRIGGER trg_biz_hr_transfer_dept BEFORE INSERT ON `biz_hr_transfer` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_hr_entry_dept;$$
CREATE TRIGGER trg_biz_hr_entry_dept BEFORE INSERT ON `biz_hr_entry` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_hr_contract_dept;$$
CREATE TRIGGER trg_biz_hr_contract_dept BEFORE INSERT ON `biz_hr_contract` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_hr_certificate_dept;$$
CREATE TRIGGER trg_biz_hr_certificate_dept BEFORE INSERT ON `biz_hr_certificate` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_hr_health_check_dept;$$
CREATE TRIGGER trg_biz_hr_health_check_dept BEFORE INSERT ON `biz_hr_health_check` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_hr_piecework_record_dept;$$
CREATE TRIGGER trg_biz_hr_piecework_record_dept BEFORE INSERT ON `biz_hr_piecework_record` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_hr_social_security_dept;$$
CREATE TRIGGER trg_biz_hr_social_security_dept BEFORE INSERT ON `biz_hr_social_security` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_wms_inbound_order_dept;$$
CREATE TRIGGER trg_wms_inbound_order_dept BEFORE INSERT ON `wms_inbound_order` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_wms_outbound_order_dept;$$
CREATE TRIGGER trg_wms_outbound_order_dept BEFORE INSERT ON `wms_outbound_order` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_wms_move_order_dept;$$
CREATE TRIGGER trg_wms_move_order_dept BEFORE INSERT ON `wms_move_order` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_wms_stock_take_dept;$$
CREATE TRIGGER trg_wms_stock_take_dept BEFORE INSERT ON `wms_stock_take` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_wms_wave_dept;$$
CREATE TRIGGER trg_wms_wave_dept BEFORE INSERT ON `wms_wave` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_mms_work_order_dept;$$
CREATE TRIGGER trg_mms_work_order_dept BEFORE INSERT ON `mms_work_order` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_mms_abnormal_dept;$$
CREATE TRIGGER trg_mms_abnormal_dept BEFORE INSERT ON `mms_abnormal` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_mms_downtime_dept;$$
CREATE TRIGGER trg_mms_downtime_dept BEFORE INSERT ON `mms_downtime` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_mms_qc_dept;$$
CREATE TRIGGER trg_mms_qc_dept BEFORE INSERT ON `mms_qc` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_mms_issue_dept;$$
CREATE TRIGGER trg_mms_issue_dept BEFORE INSERT ON `mms_issue` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_mms_return_material_dept;$$
CREATE TRIGGER trg_mms_return_material_dept BEFORE INSERT ON `mms_return_material` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_mms_outsource_dept;$$
CREATE TRIGGER trg_mms_outsource_dept BEFORE INSERT ON `mms_outsource` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_mms_finish_receipt_dept;$$
CREATE TRIGGER trg_mms_finish_receipt_dept BEFORE INSERT ON `mms_finish_receipt` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_mms_work_report_dept;$$
CREATE TRIGGER trg_mms_work_report_dept BEFORE INSERT ON `mms_work_report` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_mms_dispatch_dept;$$
CREATE TRIGGER trg_mms_dispatch_dept BEFORE INSERT ON `mms_dispatch` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_mms_schedule_task_dept;$$
CREATE TRIGGER trg_mms_schedule_task_dept BEFORE INSERT ON `mms_schedule_task` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_mms_mps_dept;$$
CREATE TRIGGER trg_mms_mps_dept BEFORE INSERT ON `mms_mps` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_mms_demand_dept;$$
CREATE TRIGGER trg_mms_demand_dept BEFORE INSERT ON `mms_demand` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_mms_kit_check_dept;$$
CREATE TRIGGER trg_mms_kit_check_dept BEFORE INSERT ON `mms_kit_check` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_safety_risk_point_dept;$$
CREATE TRIGGER trg_biz_safety_risk_point_dept BEFORE INSERT ON `biz_safety_risk_point` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_safety_incident_dept;$$
CREATE TRIGGER trg_biz_safety_incident_dept BEFORE INSERT ON `biz_safety_incident` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_safety_drill_dept;$$
CREATE TRIGGER trg_biz_safety_drill_dept BEFORE INSERT ON `biz_safety_drill` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_safety_emergency_plan_dept;$$
CREATE TRIGGER trg_biz_safety_emergency_plan_dept BEFORE INSERT ON `biz_safety_emergency_plan` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_safety_emergency_material_dept;$$
CREATE TRIGGER trg_biz_safety_emergency_material_dept BEFORE INSERT ON `biz_safety_emergency_material` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_safety_training_record_dept;$$
CREATE TRIGGER trg_biz_safety_training_record_dept BEFORE INSERT ON `biz_safety_training_record` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_safety_training_plan_dept;$$
CREATE TRIGGER trg_biz_safety_training_plan_dept BEFORE INSERT ON `biz_safety_training_plan` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DROP TRIGGER IF EXISTS trg_biz_safety_rectify_dept;$$
CREATE TRIGGER trg_biz_safety_rectify_dept BEFORE INSERT ON `biz_safety_rectify` FOR EACH ROW BEGIN IF NEW.dept_id IS NULL AND NEW.create_by IS NOT NULL THEN SET NEW.dept_id = (SELECT u.dept_id FROM sys_user u WHERE u.user_name = NEW.create_by LIMIT 1); END IF; END;$$
DELIMITER ;

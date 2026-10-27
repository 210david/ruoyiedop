-- MMS Production Control Module - Complete SQL
USE `ry-vue`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. BOM
DROP TABLE IF EXISTS mms_bom;
CREATE TABLE mms_bom (bom_id BIGINT(20) NOT NULL AUTO_INCREMENT,bom_no VARCHAR(50) NOT NULL,bom_name VARCHAR(200) NOT NULL,product_id BIGINT(20) NOT NULL,product_code VARCHAR(50) DEFAULT '',product_name VARCHAR(100) DEFAULT '',bom_type VARCHAR(20) DEFAULT '0',version VARCHAR(10) DEFAULT '1.0',base_qty DECIMAL(18,6) DEFAULT 1,base_unit VARCHAR(20) DEFAULT '',status VARCHAR(10) DEFAULT '0',effective_date DATE DEFAULT NULL,expire_date DATE DEFAULT NULL,remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(bom_id),UNIQUE KEY uk_bom_no(bom_no)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 2. BOM Detail
DROP TABLE IF EXISTS mms_bom_detail;
CREATE TABLE mms_bom_detail (detail_id BIGINT(20) NOT NULL AUTO_INCREMENT,bom_id BIGINT(20) NOT NULL,seq INT DEFAULT 0,material_id BIGINT(20) NOT NULL,material_code VARCHAR(50) DEFAULT '',material_name VARCHAR(100) DEFAULT '',spec_model VARCHAR(100) DEFAULT '',unit VARCHAR(20) DEFAULT '',usage_qty DECIMAL(18,6) NOT NULL DEFAULT 0,loss_rate DECIMAL(6,2) DEFAULT 0.00,is_key_material CHAR(1) DEFAULT '0',supply_type CHAR(1) DEFAULT '1',pick_store_id BIGINT(20) DEFAULT NULL,is_phantom CHAR(1) DEFAULT '0',bom_ref_id BIGINT(20) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,remark VARCHAR(500) DEFAULT NULL,PRIMARY KEY(detail_id),KEY idx_bom_id(bom_id),KEY idx_bom_ref_id(bom_ref_id)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 3. Route
DROP TABLE IF EXISTS mms_route;
CREATE TABLE mms_route (route_id BIGINT(20) NOT NULL AUTO_INCREMENT,route_no VARCHAR(50) NOT NULL,route_name VARCHAR(200) NOT NULL,product_id BIGINT(20) NOT NULL,product_code VARCHAR(50) DEFAULT '',product_name VARCHAR(100) DEFAULT '',version VARCHAR(10) DEFAULT '1.0',status VARCHAR(10) DEFAULT '0',remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(route_id),UNIQUE KEY uk_route_no(route_no)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 4. Process
DROP TABLE IF EXISTS mms_process;
CREATE TABLE mms_process (process_id BIGINT(20) NOT NULL AUTO_INCREMENT,process_code VARCHAR(50) NOT NULL,process_name VARCHAR(100) NOT NULL,process_type VARCHAR(20) DEFAULT '0',std_time DECIMAL(10,2) DEFAULT 0.00,prep_time DECIMAL(10,2) DEFAULT 0.00,is_key_process CHAR(1) DEFAULT '0',is_outsource CHAR(1) DEFAULT '0',workshop_name VARCHAR(100) DEFAULT '',line_name VARCHAR(100) DEFAULT '',status VARCHAR(10) DEFAULT '0',remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(process_id),UNIQUE KEY uk_process_code(process_code)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 5. Route Process
DROP TABLE IF EXISTS mms_route_process;
CREATE TABLE mms_route_process (id BIGINT(20) NOT NULL AUTO_INCREMENT,route_id BIGINT(20) NOT NULL,process_id BIGINT(20) NOT NULL,process_code VARCHAR(50) DEFAULT '',process_name VARCHAR(100) DEFAULT '',step_seq INT(4) NOT NULL,std_time DECIMAL(10,2) DEFAULT 0.00,is_key_process CHAR(1) DEFAULT '0',is_outsource CHAR(1) DEFAULT '0',del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(id),KEY idx_route_id(route_id)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 6. Resource
DROP TABLE IF EXISTS mms_resource;
CREATE TABLE mms_resource (resource_id BIGINT(20) NOT NULL AUTO_INCREMENT,resource_code VARCHAR(50) NOT NULL,resource_name VARCHAR(100) NOT NULL,resource_type VARCHAR(20) DEFAULT '0',rate DECIMAL(18,3) DEFAULT 0.000,rate_unit VARCHAR(20) DEFAULT 'pcs/h',batch_min DECIMAL(18,3) DEFAULT 0.000,batch_max DECIMAL(18,3) DEFAULT 0.000,efficiency DECIMAL(6,2) DEFAULT 100.00,workshop_name VARCHAR(100) DEFAULT '',line_name VARCHAR(100) DEFAULT '',status VARCHAR(10) DEFAULT '0',remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(resource_id),UNIQUE KEY uk_resource_code(resource_code)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 7. Shift
DROP TABLE IF EXISTS mms_shift;
CREATE TABLE mms_shift (shift_id BIGINT(20) NOT NULL AUTO_INCREMENT,shift_code VARCHAR(50) DEFAULT NULL COMMENT '班次编号',shift_name VARCHAR(50) NOT NULL,start_time VARCHAR(10) NOT NULL,end_time VARCHAR(10) NOT NULL,rest_minutes INT(4) DEFAULT 0,avail_hours DECIMAL(10,2) DEFAULT 8.00,status VARCHAR(10) DEFAULT '1',remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(shift_id)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 8. Demand
DROP TABLE IF EXISTS mms_demand;
CREATE TABLE mms_demand (demand_id BIGINT(20) NOT NULL AUTO_INCREMENT,demand_no VARCHAR(50) NOT NULL,demand_type VARCHAR(20) DEFAULT '0',source_order_no VARCHAR(50) DEFAULT '',product_id BIGINT(20) NOT NULL,product_code VARCHAR(50) DEFAULT '',product_name VARCHAR(100) DEFAULT '',spec_model VARCHAR(100) DEFAULT '',unit VARCHAR(20) DEFAULT '',demand_qty DECIMAL(18,3) NOT NULL,require_date DATE NOT NULL,priority VARCHAR(10) DEFAULT '1',customer_id BIGINT(20) DEFAULT NULL,customer_no VARCHAR(50) DEFAULT '',customer_name VARCHAR(100) DEFAULT '',status VARCHAR(10) DEFAULT '0',remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(demand_id),UNIQUE KEY uk_demand_no(demand_no)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 9. MPS
DROP TABLE IF EXISTS mms_mps;
CREATE TABLE mms_mps (mps_id BIGINT(20) NOT NULL AUTO_INCREMENT,mps_no VARCHAR(50) NOT NULL,demand_id BIGINT(20) DEFAULT NULL,demand_no VARCHAR(50) DEFAULT '',product_id BIGINT(20) NOT NULL,product_code VARCHAR(50) DEFAULT '',product_name VARCHAR(100) DEFAULT '',spec_model VARCHAR(100) DEFAULT '',unit VARCHAR(20) DEFAULT '',plan_qty DECIMAL(18,3) NOT NULL,period_start DATE NOT NULL,period_end DATE NOT NULL,resource_id BIGINT(20) DEFAULT NULL,resource_name VARCHAR(100) DEFAULT '',priority VARCHAR(10) DEFAULT '1',status VARCHAR(10) DEFAULT '0',audit_by VARCHAR(64) DEFAULT '',audit_time DATETIME DEFAULT NULL,audit_opinion VARCHAR(500) DEFAULT NULL,remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(mps_id),UNIQUE KEY uk_mps_no(mps_no)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 10. Kit Check
DROP TABLE IF EXISTS mms_kit_check;
CREATE TABLE mms_kit_check (kit_id BIGINT(20) NOT NULL AUTO_INCREMENT,kit_no VARCHAR(50) NOT NULL,work_order_id BIGINT(20) DEFAULT NULL,work_order_no VARCHAR(50) DEFAULT '',mps_id BIGINT(20) DEFAULT NULL,mps_no VARCHAR(50) DEFAULT '',bom_id BIGINT(20) DEFAULT NULL,check_time DATETIME DEFAULT NULL,kit_rate DECIMAL(10,2) DEFAULT 0.00,is_complete CHAR(1) DEFAULT '0',status VARCHAR(10) DEFAULT '0',remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(kit_id),UNIQUE KEY uk_kit_no(kit_no)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 11. Kit Check Detail
DROP TABLE IF EXISTS mms_kit_check_detail;
CREATE TABLE mms_kit_check_detail (detail_id BIGINT(20) NOT NULL AUTO_INCREMENT,kit_id BIGINT(20) NOT NULL,material_id BIGINT(20) NOT NULL,material_code VARCHAR(50) DEFAULT '',material_name VARCHAR(100) DEFAULT '',spec_model VARCHAR(100) DEFAULT '',unit VARCHAR(20) DEFAULT '',required_qty DECIMAL(18,3) NOT NULL,available_qty DECIMAL(18,3) DEFAULT 0.000,shortage_qty DECIMAL(18,3) DEFAULT 0.000,is_complete CHAR(1) DEFAULT '0',del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,PRIMARY KEY(detail_id),KEY idx_kit_id(kit_id)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 12. Work Order
DROP TABLE IF EXISTS mms_work_order;
CREATE TABLE mms_work_order (work_order_id BIGINT(20) NOT NULL AUTO_INCREMENT,work_order_no VARCHAR(50) NOT NULL,order_type VARCHAR(20) DEFAULT '0',mps_id BIGINT(20) DEFAULT NULL,mps_no VARCHAR(50) DEFAULT '',demand_no VARCHAR(50) DEFAULT '',product_id BIGINT(20) NOT NULL,product_code VARCHAR(50) DEFAULT '',product_name VARCHAR(100) DEFAULT '',spec_model VARCHAR(100) DEFAULT '',unit VARCHAR(20) DEFAULT '',plan_qty DECIMAL(18,3) NOT NULL,bom_id BIGINT(20) DEFAULT NULL,bom_no VARCHAR(50) DEFAULT '',route_id BIGINT(20) DEFAULT NULL,route_no VARCHAR(50) DEFAULT '',resource_id BIGINT(20) DEFAULT NULL,resource_name VARCHAR(100) DEFAULT '',plan_start DATETIME DEFAULT NULL,plan_finish DATETIME DEFAULT NULL,actual_start DATETIME DEFAULT NULL,actual_finish DATETIME DEFAULT NULL,finished_qty DECIMAL(18,3) DEFAULT 0.000,qualified_qty DECIMAL(18,3) DEFAULT 0.000,defect_qty DECIMAL(18,3) DEFAULT 0.000,priority VARCHAR(10) DEFAULT '1',status VARCHAR(10) DEFAULT '0',audit_by VARCHAR(64) DEFAULT '',audit_time DATETIME DEFAULT NULL,audit_opinion VARCHAR(500) DEFAULT NULL,pause_reason VARCHAR(200) DEFAULT '',close_remark VARCHAR(500) DEFAULT '',release_by VARCHAR(64) DEFAULT NULL COMMENT '下达人',release_time DATETIME DEFAULT NULL COMMENT '下达时间',remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(work_order_id),UNIQUE KEY uk_work_order_no(work_order_no)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 13. Work Order Audit Log
DROP TABLE IF EXISTS mms_work_order_audit_log;
CREATE TABLE mms_work_order_audit_log (log_id BIGINT(20) NOT NULL AUTO_INCREMENT,work_order_id BIGINT(20) NOT NULL,audit_by VARCHAR(64) DEFAULT '',audit_action VARCHAR(10) DEFAULT '',audit_remark VARCHAR(500) DEFAULT NULL,audit_time DATETIME DEFAULT NULL,PRIMARY KEY(log_id),KEY idx_work_order_id(work_order_id)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 14. Issue
DROP TABLE IF EXISTS mms_issue;
CREATE TABLE mms_issue (issue_id BIGINT(20) NOT NULL AUTO_INCREMENT,issue_no VARCHAR(50) NOT NULL,work_order_id BIGINT(20) NOT NULL,work_order_no VARCHAR(50) DEFAULT '',material_id BIGINT(20) NOT NULL,material_code VARCHAR(50) DEFAULT '',material_name VARCHAR(100) DEFAULT '',spec_model VARCHAR(100) DEFAULT '',unit VARCHAR(20) DEFAULT '',issue_qty DECIMAL(18,3) NOT NULL,batch_no VARCHAR(50) DEFAULT '',issue_time DATETIME DEFAULT NULL,issue_by VARCHAR(64) DEFAULT '',status VARCHAR(10) DEFAULT '0',remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(issue_id),UNIQUE KEY uk_issue_no(issue_no)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 15. Return Material
DROP TABLE IF EXISTS mms_return_material;
CREATE TABLE mms_return_material (return_id BIGINT(20) NOT NULL AUTO_INCREMENT,return_no VARCHAR(50) NOT NULL,work_order_id BIGINT(20) NOT NULL,work_order_no VARCHAR(50) DEFAULT '',material_id BIGINT(20) NOT NULL,material_code VARCHAR(50) DEFAULT '',material_name VARCHAR(100) DEFAULT '',spec_model VARCHAR(100) DEFAULT '',unit VARCHAR(20) DEFAULT '',return_qty DECIMAL(18,3) NOT NULL,return_reason VARCHAR(200) DEFAULT '',return_time DATETIME DEFAULT NULL,return_by VARCHAR(64) DEFAULT '',status VARCHAR(10) DEFAULT '0',remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(return_id),UNIQUE KEY uk_return_no(return_no)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 16. Work Report
DROP TABLE IF EXISTS mms_work_report;
CREATE TABLE mms_work_report (report_id BIGINT(20) NOT NULL AUTO_INCREMENT,report_no VARCHAR(50) NOT NULL,work_order_id BIGINT(20) NOT NULL,work_order_no VARCHAR(50) DEFAULT '',process_id BIGINT(20) DEFAULT NULL,process_name VARCHAR(100) DEFAULT '',resource_id BIGINT(20) DEFAULT NULL,resource_name VARCHAR(100) DEFAULT '',good_qty DECIMAL(18,3) NOT NULL DEFAULT 0,defect_qty DECIMAL(18,3) DEFAULT 0.000,work_hours DECIMAL(10,2) DEFAULT 0.00,report_time DATETIME DEFAULT NULL,report_by VARCHAR(64) DEFAULT '',team_name VARCHAR(50) DEFAULT '',shift_name VARCHAR(50) DEFAULT '',is_first_piece CHAR(1) DEFAULT '0',status VARCHAR(10) DEFAULT '0',audit_by VARCHAR(64) DEFAULT '',audit_time DATETIME DEFAULT NULL,audit_remark VARCHAR(500) DEFAULT '',remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(report_id),UNIQUE KEY uk_report_no(report_no)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 17. QC
DROP TABLE IF EXISTS mms_qc;
CREATE TABLE mms_qc (qc_id BIGINT(20) NOT NULL AUTO_INCREMENT,qc_no VARCHAR(50) NOT NULL,work_order_id BIGINT(20) NOT NULL,work_order_no VARCHAR(50) DEFAULT '',process_id BIGINT(20) DEFAULT NULL,process_name VARCHAR(100) DEFAULT '',qc_type VARCHAR(20) DEFAULT '0',inspect_qty INT(4) NOT NULL DEFAULT 1,defect_qty INT(4) DEFAULT 0,scrap_qty INT(4) DEFAULT 0,defect_type VARCHAR(100) DEFAULT '',qc_result VARCHAR(20) DEFAULT '0',qc_by VARCHAR(64) DEFAULT '',qc_time DATETIME DEFAULT NULL,remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(qc_id),UNIQUE KEY uk_qc_no(qc_no)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 18. Abnormal
DROP TABLE IF EXISTS mms_abnormal;
CREATE TABLE mms_abnormal (abnormal_id BIGINT(20) NOT NULL AUTO_INCREMENT,abnormal_no VARCHAR(50) NOT NULL,work_order_id BIGINT(20) DEFAULT NULL,work_order_no VARCHAR(50) DEFAULT '',resource_id BIGINT(20) DEFAULT NULL,resource_name VARCHAR(100) DEFAULT '',abnormal_type VARCHAR(30) NOT NULL,severity VARCHAR(10) DEFAULT '0',description TEXT,report_by VARCHAR(64) DEFAULT '',report_time DATETIME DEFAULT NULL,response_by VARCHAR(64) DEFAULT '',response_time DATETIME DEFAULT NULL,close_time DATETIME DEFAULT NULL,handle_result VARCHAR(500) DEFAULT '',status VARCHAR(10) DEFAULT '0',remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(abnormal_id),UNIQUE KEY uk_abnormal_no(abnormal_no)) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 19. Finish Receipt
DROP TABLE IF EXISTS mms_finish_receipt;
CREATE TABLE mms_finish_receipt (finish_id BIGINT(20) NOT NULL AUTO_INCREMENT,finish_no VARCHAR(50) NOT NULL,work_order_id BIGINT(20) NOT NULL,work_order_no VARCHAR(50) DEFAULT '',product_id BIGINT(20) DEFAULT NULL,product_code VARCHAR(50) DEFAULT '',product_name VARCHAR(100) DEFAULT '',unit VARCHAR(20) DEFAULT '',finish_qty DECIMAL(18,3) NOT NULL,qualified_qty DECIMAL(18,3) DEFAULT 0.000,batch_no VARCHAR(50) DEFAULT '',finish_time DATETIME DEFAULT NULL,finish_by VARCHAR(64) DEFAULT '',is_on_time CHAR(1) DEFAULT '1',status VARCHAR(10) DEFAULT '0',remark VARCHAR(500) DEFAULT NULL,del_flag CHAR(1) DEFAULT '0',create_by VARCHAR(64) DEFAULT '',create_time DATETIME DEFAULT NULL,update_by VARCHAR(64) DEFAULT '',update_time DATETIME DEFAULT NULL,PRIMARY KEY(finish_id),UNIQUE KEY uk_finish_no(finish_no)) ENGINE=InnoDB AUTO_INCREMENT=1;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- Dictionary data for BOM
-- ============================================================
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'mms_bom_status';
DELETE FROM `sys_dict_type` WHERE `dict_type` = 'mms_bom_status';
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES ('BOM状态', 'mms_bom_status', '0', 'admin', sysdate(), 'BOM状态：草稿/已发布/停用');
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '草稿',   '0', 'mms_bom_status', '', 'warning', 'Y', '0', 'admin', sysdate(), '草稿-可编辑'),
(2, '已发布', '1', 'mms_bom_status', '', 'success', 'N', '0', 'admin', sysdate(), '已发布-不可编辑'),
(3, '停用',   '2', 'mms_bom_status', '', 'info',    'N', '0', 'admin', sysdate(), '停用-不可被引用');

DELETE FROM `sys_dict_data` WHERE `dict_type` = 'mms_supply_type';
DELETE FROM `sys_dict_type` WHERE `dict_type` = 'mms_supply_type';
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES ('BOM供应方式', 'mms_supply_type', '0', 'admin', sysdate(), 'BOM子件供应方式');
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '直接领料', '1', 'mms_supply_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '按BOM明细直接领料'),
(2, '倒冲',     '2', 'mms_supply_type', '', 'success', 'N', '0', 'admin', sysdate(), '完工后按实际消耗倒冲库存'),
(3, '车间库存', '3', 'mms_supply_type', '', 'info',    'N', '0', 'admin', sysdate(), '从车间库存消耗');

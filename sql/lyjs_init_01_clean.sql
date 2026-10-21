-- ============================================================
-- 郎鹰金属（ruoyi_lyjs）业务测试数据清理脚本
-- 范围：营销(mk)/投标(bid)/采购(pms)/仓储(wms)/生产管控(mms)/设备(dms)/
--       质量(qms)/安全(biz_safety)/人力(biz_hr)/产品(pdm) 业务数据
-- 保留：系统基础数据（sys_menu/sys_role/sys_dict/编号规则 mk_number_rule/
--       mms_code_rule/qms_aql_plan/薪酬社保税率配置/班次/工作日历/商机阶段等）
-- 财务(fin)/工作流(act_/flw_)不在本次清理范围
-- 执行库：ruoyi_lyjs
-- ============================================================
USE ruoyi_lyjs;

-- ---------- 营销管理 mk（保留编号规则/渠道/客户标签/商机阶段配置） ----------
TRUNCATE mk_activity; TRUNCATE mk_activity_channel_link; TRUNCATE mk_activity_participant; TRUNCATE mk_activity_status_log;
TRUNCATE mk_contact; TRUNCATE mk_content; TRUNCATE mk_content_audit_log; TRUNCATE mk_content_package;
TRUNCATE mk_contract; TRUNCATE mk_contract_approve_log; TRUNCATE mk_contract_change_log;
TRUNCATE mk_customer; TRUNCATE mk_customer_tag_rel; TRUNCATE mk_interaction;
TRUNCATE mk_invoice; TRUNCATE mk_lead; TRUNCATE mk_lead_log; TRUNCATE mk_material;
TRUNCATE mk_opportunity; TRUNCATE mk_opportunity_stage_log;
TRUNCATE mk_order; TRUNCATE mk_order_audit_log; TRUNCATE mk_order_item;
TRUNCATE mk_order_return; TRUNCATE mk_order_return_approve_log; TRUNCATE mk_order_return_audit_log; TRUNCATE mk_order_return_detail; TRUNCATE mk_order_return_item;
TRUNCATE mk_payment_plan; TRUNCATE mk_payment_record; TRUNCATE mk_price_policy; TRUNCATE mk_promotion_data;
TRUNCATE mk_rebate_policy; TRUNCATE mk_rebate_settle; TRUNCATE mk_shipment; TRUNCATE mk_shipment_detail;

-- ---------- 投标管理 bid（保留采集源/关键词配置） ----------
TRUNCATE bid_notice; TRUNCATE bid_project; TRUNCATE bid_project_node; TRUNCATE bid_deposit; TRUNCATE bid_crawl_log; TRUNCATE bid_qualification;

-- ---------- 采购管理 pms ----------
TRUNCATE pms_contract; TRUNCATE pms_contract_audit_log; TRUNCATE pms_contract_change;
TRUNCATE pms_inquiry; TRUNCATE pms_inquiry_audit_log; TRUNCATE pms_inquiry_detail;
TRUNCATE pms_invoice; TRUNCATE pms_invoice_audit_log; TRUNCATE pms_invoice_detail; TRUNCATE pms_invoice_payment;
TRUNCATE pms_order_audit_log; TRUNCATE pms_plan_audit_log;
TRUNCATE pms_purchase_order; TRUNCATE pms_purchase_order_detail; TRUNCATE pms_purchase_plan; TRUNCATE pms_purchase_plan_detail;
TRUNCATE pms_purchase_request; TRUNCATE pms_purchase_request_detail;
TRUNCATE pms_quotation; TRUNCATE pms_quotation_detail;
TRUNCATE pms_receive; TRUNCATE pms_receive_audit_log; TRUNCATE pms_receive_detail; TRUNCATE pms_request_audit_log;
TRUNCATE pms_return; TRUNCATE pms_return_audit_log; TRUNCATE pms_return_detail;
TRUNCATE pms_supplier_catalog; TRUNCATE pms_supplier_eval; TRUNCATE pms_supplier_eval_audit_log; TRUNCATE pms_supplier_qualification;

-- ---------- 仓储物流 wms ----------
TRUNCATE wms_inbound_order; TRUNCATE wms_inbound_order_detail; TRUNCATE wms_inventory; TRUNCATE wms_inventory_log;
TRUNCATE wms_material; TRUNCATE wms_move_order; TRUNCATE wms_outbound_order; TRUNCATE wms_outbound_order_detail;
TRUNCATE wms_stock_take; TRUNCATE wms_stock_take_approve_log; TRUNCATE wms_stock_take_detail; TRUNCATE wms_stock_take_snapshot;
TRUNCATE wms_supplier; TRUNCATE wms_warehouse; TRUNCATE wms_wave;

-- ---------- 生产管控 mms（保留 mms_code_rule 编码配置） ----------
TRUNCATE mms_abnormal; TRUNCATE mms_batch_trace; TRUNCATE mms_bom; TRUNCATE mms_bom_detail; TRUNCATE mms_capacity_load;
TRUNCATE mms_demand; TRUNCATE mms_dispatch; TRUNCATE mms_downtime; TRUNCATE mms_factory; TRUNCATE mms_factory_calendar;
TRUNCATE mms_finish_receipt; TRUNCATE mms_issue; TRUNCATE mms_issue_detail; TRUNCATE mms_kit_check; TRUNCATE mms_kit_check_detail;
TRUNCATE mms_line; TRUNCATE mms_mps; TRUNCATE mms_mps_audit_log; TRUNCATE mms_oee_snapshot; TRUNCATE mms_outsource;
TRUNCATE mms_process; TRUNCATE mms_qc; TRUNCATE mms_resource; TRUNCATE mms_return_detail; TRUNCATE mms_return_material;
TRUNCATE mms_route; TRUNCATE mms_route_audit_log; TRUNCATE mms_route_process; TRUNCATE mms_route_version_log;
TRUNCATE mms_schedule; TRUNCATE mms_schedule_task; TRUNCATE mms_stat_daily; TRUNCATE mms_station; TRUNCATE mms_shift; TRUNCATE mms_team;
TRUNCATE mms_wo_bom_snapshot; TRUNCATE mms_wo_route_snapshot; TRUNCATE mms_work_order; TRUNCATE mms_work_order_audit_log;
TRUNCATE mms_work_order_bom; TRUNCATE mms_work_order_routing; TRUNCATE mms_work_report; TRUNCATE mms_workshop;

-- ---------- 设备管理 dms（保留数采配置 dms_data_collection_config） ----------
TRUNCATE dms_data_record; TRUNCATE dms_equipment; TRUNCATE dms_equipment_category; TRUNCATE dms_equipment_log;
TRUNCATE dms_inspection_route; TRUNCATE dms_inspection_task; TRUNCATE dms_pm_plan; TRUNCATE dms_repair_faq;
TRUNCATE dms_spare_part; TRUNCATE dms_spare_part_record; TRUNCATE dms_spare_part_stock; TRUNCATE dms_work_order; TRUNCATE dms_work_order_log;

-- ---------- 质量管理 qms（保留 AQL 抽样方案标准库 qms_aql_plan） ----------
TRUNCATE qms_audit_check_item; TRUNCATE qms_audit_check_template; TRUNCATE qms_audit_check_template_item; TRUNCATE qms_audit_item; TRUNCATE qms_audit_plan;
TRUNCATE qms_capa; TRUNCATE qms_capa_effectiveness; TRUNCATE qms_capa_root_cause; TRUNCATE qms_complaint; TRUNCATE qms_defect_code;
TRUNCATE qms_doc; TRUNCATE qms_doc_distribute; TRUNCATE qms_doc_version; TRUNCATE qms_esig_record; TRUNCATE qms_gauge;
TRUNCATE qms_insp_item; TRUNCATE qms_insp_task; TRUNCATE qms_inspect_std; TRUNCATE qms_lot_genealogy; TRUNCATE qms_material_attr;
TRUNCATE qms_mr; TRUNCATE qms_mr_action; TRUNCATE qms_ncr; TRUNCATE qms_ncr_approve_log; TRUNCATE qms_quality_target;
TRUNCATE qms_sqm_push_log; TRUNCATE qms_supplier_audit; TRUNCATE qms_supplier_eval; TRUNCATE qms_trace_sn; TRUNCATE qms_workshop;

-- ---------- 安全生产 biz_safety ----------
TRUNCATE biz_safety_area; TRUNCATE biz_safety_cert; TRUNCATE biz_safety_compliance_check; TRUNCATE biz_safety_drill;
TRUNCATE biz_safety_emergency_material; TRUNCATE biz_safety_emergency_plan; TRUNCATE biz_safety_enterprise; TRUNCATE biz_safety_equipment;
TRUNCATE biz_safety_hazard; TRUNCATE biz_safety_hazard_audit_log; TRUNCATE biz_safety_incident; TRUNCATE biz_safety_inspection_task;
TRUNCATE biz_safety_major_hazard; TRUNCATE biz_safety_material; TRUNCATE biz_safety_material_record; TRUNCATE biz_safety_material_supplier;
TRUNCATE biz_safety_rectify; TRUNCATE biz_safety_remind; TRUNCATE biz_safety_risk_point;
TRUNCATE biz_safety_training_attendee; TRUNCATE biz_safety_training_cert; TRUNCATE biz_safety_training_course; TRUNCATE biz_safety_training_plan; TRUNCATE biz_safety_training_record; TRUNCATE biz_safety_worker;

-- ---------- 人力资源 biz_hr（保留薪酬项/税率/薪酬方案/社保方案/班次/工作日历/排班模板配置） ----------
TRUNCATE biz_hr_attendance_monthly; TRUNCATE biz_hr_attendance_record; TRUNCATE biz_hr_business_trip; TRUNCATE biz_hr_business_trip_audit_log;
TRUNCATE biz_hr_certificate; TRUNCATE biz_hr_clock_repair; TRUNCATE biz_hr_clock_repair_audit_log; TRUNCATE biz_hr_contract;
TRUNCATE biz_hr_employee; TRUNCATE biz_hr_employee_education; TRUNCATE biz_hr_employee_family; TRUNCATE biz_hr_employee_qualification; TRUNCATE biz_hr_employee_work_exp;
TRUNCATE biz_hr_entry; TRUNCATE biz_hr_entry_audit_log; TRUNCATE biz_hr_health_check;
TRUNCATE biz_hr_leave; TRUNCATE biz_hr_leave_balance; TRUNCATE biz_hr_leave_balance_log; TRUNCATE biz_hr_leave_request; TRUNCATE biz_hr_leave_request_audit_log;
TRUNCATE biz_hr_outing; TRUNCATE biz_hr_outing_audit_log; TRUNCATE biz_hr_overtime; TRUNCATE biz_hr_overtime_audit_log;
TRUNCATE biz_hr_payroll_batch; TRUNCATE biz_hr_payslip; TRUNCATE biz_hr_piecework_plan; TRUNCATE biz_hr_piecework_record;
TRUNCATE biz_hr_regular; TRUNCATE biz_hr_regular_audit_log; TRUNCATE biz_hr_schedule; TRUNCATE biz_hr_social_security; TRUNCATE biz_hr_special_deduction;
TRUNCATE biz_hr_transfer; TRUNCATE biz_hr_transfer_audit_log;

-- ---------- 产品数据 pdm ----------
TRUNCATE pdm_change_audit_log; TRUNCATE pdm_change_order; TRUNCATE pdm_change_order_item; TRUNCATE pdm_design_bom; TRUNCATE pdm_design_bom_item;
TRUNCATE pdm_design_file; TRUNCATE pdm_doc_version; TRUNCATE pdm_mbom_release; TRUNCATE pdm_product; TRUNCATE pdm_product_category; TRUNCATE pdm_scheme; TRUNCATE pdm_scheme_formula;

-- ---------- 组织与用户（重建郎鹰组织，保留 admin 与岗位/角色定义） ----------
TRUNCATE sys_user_home_layout; TRUNCATE sys_user_role; TRUNCATE sys_user_post;
DELETE FROM sys_user WHERE user_id <> 1;
DELETE FROM sys_dept;

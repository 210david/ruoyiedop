-- 7. 安全培训（计划/课程/记录）
INSERT INTO biz_safety_training_plan (plan_code, plan_name, plan_type, plan_year, target_persons, total_hours, plan_status, status, del_flag, create_by, create_time, dept_id) VALUES
('PX2026001', '2026年度全员安全教育培训计划', '1', 2026, 19, 24, '1', '0', '0', 'admin', NOW(), 105),
('PX2026002', '有限空间作业专项培训计划', '1', 2026, 6, 8, '1', '0', '0', 'admin', NOW(), 105);
INSERT INTO biz_safety_training_course (course_code, course_name, course_type, course_form, hours, instructor, description, status, del_flag, create_by, create_time) VALUES
('KC001', '新员工三级安全教育', '1', '1', 8, '郑浩', '厂级/部门级/班组级三级安全教育，覆盖全员', '0', '0', 'admin', NOW()),
('KC002', '有限空间作业安全培训', '4', '1', 4, '郑浩', '有限空间辨识、气体检测、作业审批、应急救援（对应2026年处罚整改）', '0', '0', 'admin', NOW()),
('KC003', '用电安全与消防培训', '6', '2', 4, '邓超', '电气线路检查、灭火器使用实操', '0', '0', 'admin', NOW()),
('KC004', '机械安全操作培训', '6', '1', 2, '卢伟', '灌装封口机等设备安全操作规程', '0', '0', 'admin', NOW());
INSERT INTO biz_safety_training_record (plan_id, course_id, course_name, course_type, training_date, training_location, hours, trainer, enterprise_id, del_flag, create_by, create_time, dept_id) SELECT
(SELECT plan_id FROM biz_safety_training_plan WHERE plan_code='PX2026001'),
(SELECT course_id FROM biz_safety_training_course WHERE course_code='KC002'), '有限空间作业安全培训', '4', CURDATE(), '厂区会议室',
4, '郑浩', 1, '0', 'admin', NOW(), 105;

-- 8. 应急演练与应急预案
INSERT INTO biz_safety_drill (drill_code, drill_type, drill_name, drill_date, drill_location, drill_commander, drill_objective, participant_count, drill_desc, eval_result, problems, improvements, create_by, create_time, dept_id) VALUES
('YL2026001', '3', '有限空间中毒窒息应急救援演练', CURDATE(), '化粪池区域', '郑浩', '检验有限空间事故应急预案可操作性，杜绝盲目施救', 8, '模拟化粪池作业人员中毒，气体检测、通风、佩戴呼吸器救援全流程', '1', '个别人员呼吸器佩戴不熟练', '下月增加实操复训', 'admin', NOW(), 105);
INSERT INTO biz_safety_emergency_plan (plan_code, plan_name, plan_type, plan_level, version, reg_no, reg_date, effective_date, expire_date, status, del_flag, create_by, create_time, dept_id) VALUES
('YJ001', '生产安全事故综合应急预案', '1', '1', 'V1.0', NULL, CURDATE(), CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 YEAR), '0', '0', 'admin', NOW(), 105),
('YJ002', '有限空间作业事故专项应急预案', '4', '2', 'V1.0', NULL, CURDATE(), CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 YEAR), '0', '0', 'admin', NOW(), 105),
('YJ003', '火灾事故专项应急预案', '2', '2', 'V1.0', NULL, CURDATE(), CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 YEAR), '0', '0', 'admin', NOW(), 105);

-- 9. 应急物资
INSERT INTO biz_safety_emergency_material (material_code, material_name, material_type, spec_model, quantity, unit, storage_location, person_name, status, del_flag, create_by, create_time, dept_id) VALUES
('WZ001', '正压式空气呼吸器', '2', 'RHZK6.8/30', 2, '套', '应急物资柜', '郑浩', '0', '0', 'admin', NOW(), 105),
('WZ002', '便携式四合一气体检测仪', '2', 'O2/CO/H2S/可燃', 2, '台', '应急物资柜', '郑浩', '0', '0', 'admin', NOW(), 105),
('WZ003', '灭火器（干粉）', '4', 'MFZ/ABC4', 16, '具', '各车间/仓库', '邓超', '0', '0', 'admin', NOW(), 105),
('WZ004', '安全绳与三脚架', '2', '配套有限空间救援', 1, '套', '应急物资柜', '郑浩', '0', '0', 'admin', NOW(), 105),
('WZ005', '急救箱', '3', '标准配置', 3, '个', '办公区/车间/仓库', '吴娟', '0', '0', 'admin', NOW(), 105);

-- 10. 特种设备与安全设施台账
INSERT INTO biz_safety_equipment (equipment_code, equipment_name, spec_model, area_id, area_name, equipment_category, special_sub_type, person_name, status, del_flag, create_by, create_time, remark) VALUES
('TZ001', '燃气蒸汽锅炉', 'WNS1-1.0-Q', (SELECT area_id FROM biz_safety_area WHERE area_code='AQ0102'), '预处理车间', '1', '锅炉', '邓超', '0', '0', 'admin', NOW(), '特种设备，定期检验'),
('TZ002', '叉车', 'CPD20', (SELECT area_id FROM biz_safety_area WHERE area_code='AQ0104'), '成品库', '1', '场（厂）内专用机动车辆', '冯军', '0', '0', 'admin', NOW(), '特种设备，定期检验');

-- 11. 危化品/危废物台账（酿造企业常见少量危化品）
INSERT INTO biz_safety_material (material_code, material_name, hazard_class, max_storage, storage_unit, storage_area_id, storage_area_name, toxic_flag, current_stock, status, del_flag, create_by, create_time) VALUES
('WHP001', '氢氧化钠（液碱）', '8.2 碱性腐蚀品', 500, '公斤', (SELECT area_id FROM biz_safety_area WHERE area_code='AQ0102'), '预处理车间', '0', 200, '0', '0', 'admin', NOW()),
('WHP002', '次氯酸钠（消毒液）', '8.1 酸性腐蚀品', 300, '公斤', (SELECT area_id FROM biz_safety_area WHERE area_code='AQ0102'), '预处理车间', '0', 100, '0', '0', 'admin', NOW());

-- 12. 特种作业人员台账
INSERT INTO biz_safety_worker (worker_name, worker_no, dept_id, dept_name, position, is_special, job_type, valid_to, review_date, status, del_flag, create_by, create_time) VALUES
('邓超', 'EMP0017', 200, '生产部', '设备维修员', '1', '1', DATE_ADD(CURDATE(), INTERVAL 2 YEAR), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '0', '0', 'admin', NOW()),
('冯军', 'EMP0010', 201, '仓储组', '库管员', '1', '5', DATE_ADD(CURDATE(), INTERVAL 2 YEAR), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '0', '0', 'admin', NOW());

-- 13. 证照台账（安全相关证照到期提醒）
INSERT INTO biz_safety_cert (cert_name, cert_no, cert_type, issue_org, issue_date, expire_date, status, del_flag, create_by, create_time) VALUES
('食品生产许可证（SC）', 'SC10353032259802', '4', '曲靖市市场监督管理局', '2024-06-18', '2029-06-17', '0', '0', 'admin', NOW()),
('排污许可证', '91530322MA6K77KJ60001R', '4', '曲靖市生态环境局', '2025-09-19', '2030-09-18', '0', '0', 'admin', NOW());

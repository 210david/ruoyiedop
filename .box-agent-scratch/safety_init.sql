-- 滇味鲜安全生产模块初始化（依据分析报告，模板数据供用户核实调整）
-- 1. 企业安全档案
INSERT INTO biz_safety_enterprise (enterprise_name, credit_code, industry_type, is_high_risk, legal_person, safety_manager, safety_org, supervise_dept, status, del_flag, create_by, create_time) VALUES
('云南滇味鲜调味品有限公司', '91530322MA6K77KJ60', '5', '0', '郭芳艳', '郑浩', '综合管理部', '陆良县应急管理局', '0', '0', 'admin', NOW());

-- 2. 安全区域（厂区-车间-区域三级）
INSERT INTO biz_safety_area (parent_id, ancestors, area_code, area_name, node_type, area_type, safety_person_name, status, del_flag, create_by, create_time) VALUES
(0, '0', 'AQ01', '滇味鲜青山工业园区', '1', '1', '郑浩', '0', '0', 'admin', NOW());
SET @area_root = LAST_INSERT_ID();
INSERT INTO biz_safety_area (parent_id, ancestors, area_code, area_name, node_type, area_type, safety_person_name, status, del_flag, create_by, create_time) VALUES
(@area_root, CONCAT('0,',@area_root), 'AQ0101', '发酵车间', '2', '5', '卢伟', '0', '0', 'admin', NOW()),
(@area_root, CONCAT('0,',@area_root), 'AQ0102', '预处理车间', '2', '1', '卢伟', '0', '0', 'admin', NOW()),
(@area_root, CONCAT('0,',@area_root), 'AQ0103', '灌装车间', '2', '1', '卢伟', '0', '0', 'admin', NOW()),
(@area_root, CONCAT('0,',@area_root), 'AQ0104', '成品库', '2', '2', '冯军', '0', '0', 'admin', NOW()),
(@area_root, CONCAT('0,',@area_root), 'AQ0105', '化粪池区域', '2', '9', '郑浩', '0', '0', 'admin', NOW()),
(@area_root, CONCAT('0,',@area_root), 'AQ0106', '配电室', '2', '8', '邓超', '0', '0', 'admin', NOW()),
(@area_root, CONCAT('0,',@area_root), 'AQ0107', '污水处理站', '2', '9', '郑浩', '0', '0', 'admin', NOW()),
(@area_root, CONCAT('0,',@area_root), 'AQ0108', '办公区', '2', '6', '吴娟', '0', '0', 'admin', NOW());

-- 3. 风险点（LEC 评估，覆盖重点区域）
INSERT INTO biz_safety_risk_point (risk_code, risk_name, area_id, area_name, hazard_desc, accident_type, l_value, e_value, c_value, d_value, risk_level, control_level, measure_engine, measure_manage, measure_training, measure_ppe, measure_emergency, check_standard, check_frequency, is_major_hazard, person_name, evaluate_date, status, del_flag, create_by, create_time, dept_id) VALUES
('FX001', '化粪池有限空间作业', (SELECT area_id FROM biz_safety_area WHERE area_code='AQ0105'), '化粪池区域', '有限空间有毒气体积聚，作业时可能中毒窒息', '19', 3, 6, 15, 270, '2', '1', '通风检测后方可作业，配备气体检测仪', '作业审批制度，专人监护', '有限空间作业培训', '正压式呼吸器、安全绳', '应急救援预案，禁止盲目施救', '作业前气体检测记录、台账', '5', '1', '郑浩', CURDATE(), '0', '0', 'admin', NOW(), 105),
('FX002', '污水处理站有限空间', (SELECT area_id FROM biz_safety_area WHERE area_code='AQ0107'), '污水处理站', '清淤作业中毒窒息风险', '19', 2, 3, 15, 90, '3', '1', '先通风再检测后作业', '作业审批与监护制度', '有限空间作业培训', '气体检测仪、呼吸器', '应急预案，禁止盲目施救', '日常巡检与作业审批台账', '4', '1', '郑浩', CURDATE(), '0', '0', 'admin', NOW(), 105),
('FX003', '燃气蒸汽锅炉', (SELECT area_id FROM biz_safety_area WHERE area_code='AQ0102'), '预处理车间', '锅炉超压爆炸、蒸汽灼烫', '16', 1, 6, 15, 90, '3', '1', '安全阀、压力表定期校验', '持证上岗、定期巡检', '锅炉操作培训', '防护手套、防烫服', '应急预案', '特种设备定期检验', '4', '0', '邓超', CURDATE(), '0', '0', 'admin', NOW(), 200),
('FX004', '辣椒烘干线电气系统', (SELECT area_id FROM biz_safety_area WHERE area_code='AQ0102'), '预处理车间', '线路老化短路引发火灾', '8', 2, 6, 7, 84, '3', '2', '线路穿管保护、漏电保护装置', '每日巡检制度', '用电安全培训', '灭火器配置', '初期火灾处置方案', '电气线路、配电箱检查', '3', '0', '邓超', CURDATE(), '0', '0', 'admin', NOW(), 200),
('FX005', '灌装封口机机械传动', (SELECT area_id FROM biz_safety_area WHERE area_code='AQ0103'), '灌装车间', '机械伤害、夹伤', '3', 2, 6, 3, 36, '4', '3', '传动部位防护罩', '操作规程上墙', '设备安全操作培训', '工作服、防挤压手套', '现场应急处置卡', '防护装置完好性检查', '2', '0', '卢伟', CURDATE(), '0', '0', 'admin', NOW(), 200),
('FX006', '成品库堆垛', (SELECT area_id FROM biz_safety_area WHERE area_code='AQ0104'), '成品库', '堆垛坍塌、叉车碰撞', '10', 2, 6, 3, 36, '4', '3', '堆垛限高标识', '叉车持证操作', '叉车安全培训', '安全帽、反光背心', '现场应急处置卡', '堆垛规范与通道畅通检查', '3', '0', '冯军', CURDATE(), '0', '0', 'admin', NOW(), 201);

-- 4. 有限空间/重大危险源辨识台账（2026-08 行政处罚合规缺口，重点补充）
INSERT INTO biz_safety_major_hazard (hazard_code, hazard_name, unit_name, unit_desc, r_value, hazard_level, identification_basis, identification_date, next_identification_date, responsible_person, responsible_phone, area_id, area_name, status, del_flag, create_by, create_time) VALUES
('YXKJ001', '化粪池', '有限空间作业场所', '地下封闭空间，可能积聚硫化氢、甲烷等有毒气体，缺氧风险', 270, '2', '《工贸企业有限空间作业安全规定》（应急管理部13号令）辨识', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '郑浩', NULL, (SELECT area_id FROM biz_safety_area WHERE area_code='AQ0105'), '化粪池区域', '0', '0', 'admin', NOW()),
('YXKJ002', '污水处理调节池', '有限空间作业场所', '半地下池体，清淤作业存在中毒窒息风险', 90, '3', '《工贸企业有限空间作业安全规定》（应急管理部13号令）辨识', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '郑浩', NULL, (SELECT area_id FROM biz_safety_area WHERE area_code='AQ0107'), '污水处理站', '0', '0', 'admin', NOW()),
('YXKJ003', '发酵池（深度>1.2m）', '有限空间作业场所', '进入清理发酵池存在缺氧、有害气体积聚风险', 90, '3', '《工贸企业有限空间作业安全规定》（应急管理部13号令）辨识', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '卢伟', NULL, (SELECT area_id FROM biz_safety_area WHERE area_code='AQ0101'), '发酵车间', '0', '0', 'admin', NOW());

-- 5. 隐患台账（含整改闭环示例 + 直接对应本次处罚事项的整改完成记录）
INSERT INTO biz_safety_hazard (hazard_code, hazard_desc, hazard_type, hazard_level, discover_person, discover_time, source_type, dept_id, dept_name, person_name, rectify_req, rectify_deadline, hazard_status, rectify_desc, rectify_time, verify_person, verify_time, verify_result, create_by, create_time, remark) VALUES
('YH20261017001', '化粪池未进行有限空间辨识、未建立安全管理台账', '3', '2', '陆良县应急管理局执法检查', NOW(), '5', 105, '综合管理部', '郑浩', '完成化粪池有限空间辨识建档，建立有限空间作业安全管理制度与作业审批台账，设置警示标志', DATE_ADD(CURDATE(), INTERVAL 7 DAY), '4', '已完成辨识建档（YXKJ001），作业审批制度与台账已建立，警示标志已设置', NOW(), '郑浩', NOW(), '1', 'admin', NOW(), '对应〔陆〕应急罚〔2026〕执法004号处罚整改事项，整改完成待复查销号'),
('YH20261017002', '污水处理站防护围栏局部锈蚀', '2', '1', '郑浩', NOW(), '2', 105, '综合管理部', '郑浩', '更换锈蚀围栏并做防腐处理', DATE_ADD(CURDATE(), INTERVAL 15 DAY), '3', '已列入整改计划', NULL, NULL, NULL, NULL, 'admin', NOW(), '排查发现，待整改'),
('YH20261017003', '预处理车间配电箱前堆放杂物', '1', '1', '卢伟', NOW(), '1', 200, '生产部', '卢伟', '清理配电箱操作空间，划定禁放区并标识', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '3', '待整改', NULL, NULL, NULL, NULL, 'admin', NOW(), '日常巡检发现');

-- 6. 排查任务（近期计划 + 已完成）
INSERT INTO biz_safety_inspection_task (task_code, task_type, task_name, risk_point_id, plan_date, executor_name, task_status, inspect_result, hazard_count, inspect_time, create_by, create_time, remark) VALUES
('PC20261017001', '2', '化粪池有限空间专项排查', (SELECT risk_point_id FROM biz_safety_risk_point WHERE risk_code='FX001'), CURDATE(), '郑浩', '0', NULL, 0, NULL, 'admin', NOW(), '每周例行，作业前气体检测检查'),
('PC20261017002', '2', '车间电气安全排查', (SELECT risk_point_id FROM biz_safety_risk_point WHERE risk_code='FX004'), CURDATE(), '邓超', '0', NULL, 0, NULL, 'admin', NOW(), '每周例行'),
('PC20261017003', '3', '发酵车间有限空间专项检查', (SELECT risk_point_id FROM biz_safety_risk_point WHERE risk_code='FX002'), DATE_SUB(CURDATE(), INTERVAL 1 DAY), '卢伟', '2', '1', 1, NOW(), 'admin', NOW(), '发现配电箱堆物隐患（YH20261017003）');

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

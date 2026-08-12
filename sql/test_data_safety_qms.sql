-- =============================================
-- 安全管理(safety) + 质量管理(qms) 测试数据
-- 每个子功能页面 5 条测试数据，用于功能测试
-- 执行前请确保已执行 safety_all.sql / qms_all.sql / qms_p1_p2_all.sql / qms_enhancements.sql / qms_trace.sql
-- =============================================

-- =============================================
-- 一、安全管理模块 测试数据
-- =============================================

-- -----------------------------------------------
-- 1. 企业安全档案表 biz_safety_enterprise
-- -----------------------------------------------
INSERT INTO biz_safety_enterprise (enterprise_name, credit_code, industry_type, is_high_risk, legal_person, safety_manager, safety_org, supervise_dept, status, del_flag, create_by, create_time, remark) VALUES
('云南新材料科技有限公司', 'TEST91350100MA0001A', '1', '1', '张建国', '李安全', '安全管理部', '云南省应急管理局', '0', '0', 'admin', NOW(), '测试数据-新能源材料企业'),
('云南绿色铝业有限公司', 'TEST91350100MA0002B', '2', '1', '王志强', '陈安监', '安全环保部', '昆明市应急管理局', '0', '0', 'admin', NOW(), '测试数据-绿色铝精加工企业'),
('云南精细化工股份有限公司', 'TEST91350100MA0003C', '3', '1', '刘德明', '赵安全', 'EHS管理部', '曲靖市应急管理局', '0', '0', 'admin', NOW(), '测试数据-精细化工企业'),
('云南装备制造有限公司', 'TEST91350100MA0004D', '4', '0', '孙伟华', '周安全', '安全生产办', '玉溪市应急管理局', '0', '0', 'admin', NOW(), '测试数据-先进装备制造企业'),
('云南特色食品有限公司', 'TEST91350100MA0005E', '5', '0', '杨国平', '吴安全', '品质安全部', '大理州应急管理局', '0', '0', 'admin', NOW(), '测试数据-高原特色食品加工企业');

SET @entId1 = (SELECT enterprise_id FROM biz_safety_enterprise WHERE credit_code = 'TEST91350100MA0001A' LIMIT 1);
SET @entId2 = (SELECT enterprise_id FROM biz_safety_enterprise WHERE credit_code = 'TEST91350100MA0002B' LIMIT 1);

-- -----------------------------------------------
-- 2. 厂区/车间/区域档案表 biz_safety_area
-- -----------------------------------------------
INSERT INTO biz_safety_area (parent_id, ancestors, area_code, area_name, node_type, enterprise_id, area_type, safety_person_name, status, del_flag, create_by, create_time, remark) VALUES
(0, '0', 'TEST-AREA-001', '一号厂区', '1', @entId1, '1', '李安全', '0', '0', 'admin', NOW(), '测试数据-主厂区'),
(0, '0', 'TEST-AREA-002', '锂电池生产车间', '2', @entId1, '1', '张车间', '0', '0', 'admin', NOW(), '测试数据-生产车间'),
(0, '0', 'TEST-AREA-003', '危化品仓库', '3', @entId1, '3', '王保管', '0', '0', 'admin', NOW(), '测试数据-危化品仓储区'),
(0, '0', 'TEST-AREA-004', '动力配电区', '3', @entId1, '8', '刘电工', '0', '0', 'admin', NOW(), '测试数据-配电区域'),
(0, '0', 'TEST-AREA-005', '应急物资储备间', '3', @entId1, '6', '陈管理', '0', '0', 'admin', NOW(), '测试数据-物资存放区');

SET @areaId1 = (SELECT area_id FROM biz_safety_area WHERE area_code = 'TEST-AREA-001' LIMIT 1);
SET @areaId2 = (SELECT area_id FROM biz_safety_area WHERE area_code = 'TEST-AREA-002' LIMIT 1);
SET @areaId3 = (SELECT area_id FROM biz_safety_area WHERE area_code = 'TEST-AREA-003' LIMIT 1);
SET @areaId4 = (SELECT area_id FROM biz_safety_area WHERE area_code = 'TEST-AREA-004' LIMIT 1);
SET @areaId5 = (SELECT area_id FROM biz_safety_area WHERE area_code = 'TEST-AREA-005' LIMIT 1);

-- -----------------------------------------------
-- 3. 证照管理表 biz_safety_cert
-- -----------------------------------------------
INSERT INTO biz_safety_cert (cert_name, cert_no, cert_type, issue_org, issue_date, expire_date, remind_status, enterprise_id, status, del_flag, create_by, create_time, remark) VALUES
('安全生产许可证', 'TEST-CERT-2026-001', '1', '云南省应急管理厅', '2023-06-15', '2026-12-31', '0', @entId1, '0', '0', 'admin', NOW(), '测试数据-安全生产许可证'),
('危险化学品经营许可证', 'TEST-CERT-2026-002', '2', '昆明市应急管理局', '2024-01-20', '2027-01-19', '0', @entId1, '0', '0', 'admin', NOW(), '测试数据-危化品经营许可证'),
('排污许可证', 'TEST-CERT-2026-003', '3', '云南省生态环境厅', '2024-03-10', '2029-03-09', '0', @entId2, '0', '0', 'admin', NOW(), '测试数据-排污许可证'),
('安全生产标准化证书', 'TEST-CERT-2026-004', '9', '云南省应急管理厅', '2024-05-01', '2027-04-30', '0', @entId1, '0', '0', 'admin', NOW(), '测试数据-安全标准化二级'),
('营业执照', 'TEST-CERT-2026-005', '9', '昆明市市场监督管理局', '2022-08-15', '2042-08-14', '0', @entId2, '0', '0', 'admin', NOW(), '测试数据-营业执照');

-- -----------------------------------------------
-- 4. 特种作业人员台账表 biz_safety_worker
-- -----------------------------------------------
INSERT INTO biz_safety_worker (worker_name, worker_no, dept_name, position, phone, is_special, job_type, cert_no, cert_issue_date, valid_to, review_date, remind_status, enterprise_id, status, del_flag, create_by, create_time, remark) VALUES
('张电工', 'TEST-WK-001', '动力部', '电工', '13800001001', '1', '1', 'DLG-2024-001', '2024-03-15', '2027-03-14', '2027-01-15', '0', @entId1, '0', '0', 'admin', NOW(), '测试数据-电工特种作业人员'),
('李焊工', 'TEST-WK-002', '机修部', '焊工', '13800001002', '1', '2', 'HJG-2024-002', '2023-07-20', '2026-07-19', '2026-05-20', '0', @entId1, '0', '0', 'admin', NOW(), '测试数据-焊接作业人员'),
('王登高', 'TEST-WK-003', '工程部', '架子工', '13800001003', '1', '3', 'DGG-2024-003', '2024-01-10', '2027-01-09', '2026-11-10', '0', @entId1, '0', '0', 'admin', NOW(), '测试数据-高处作业人员'),
('赵叉车', 'TEST-WK-004', '仓储部', '叉车工', '13800001004', '1', '5', 'CCC-2024-004', '2024-06-05', '2027-06-04', '2027-04-05', '0', @entId2, '0', '0', 'admin', NOW(), '测试数据-厂内机动车作业人员'),
('孙有限', 'TEST-WK-005', '生产部', '有限空间作业', '13800001005', '1', '6', 'YXK-2024-005', '2024-09-01', '2027-08-31', '2027-06-01', '0', @entId1, '0', '0', 'admin', NOW(), '测试数据-有限空间作业人员');

-- -----------------------------------------------
-- 5. 设备设施台账表 biz_safety_equipment
-- -----------------------------------------------
INSERT INTO biz_safety_equipment (equipment_code, equipment_name, spec_model, area_id, area_name, equipment_category, special_sub_type, reg_cert_no, inspect_expire, remind_status, person_name, enterprise_id, status, del_flag, create_by, create_time, remark) VALUES
('TEST-EQP-001', '蒸汽锅炉', 'WNS4-1.25-Q', @areaId4, '动力配电区', '1', '1', 'TC-2024-001', '2026-12-31', '0', '刘电工', @entId1, '0', '0', 'admin', NOW(), '测试数据-4吨燃气蒸汽锅炉'),
('TEST-EQP-002', '压力容器储罐', 'CF-10000L', @areaId3, '危化品仓库', '1', '2', 'TC-2024-002', '2027-06-30', '0', '王保管', @entId1, '0', '0', 'admin', NOW(), '测试数据-液氨储罐'),
('TEST-EQP-003', '桥式起重机', 'QD-10T', @areaId2, '锂电池生产车间', '1', '4', 'TC-2024-003', '2026-10-15', '0', '张车间', @entId1, '0', '0', 'admin', NOW(), '测试数据-10吨双梁桥式起重机'),
('TEST-EQP-004', '可燃气体报警器', 'GT-988', @areaId3, '危化品仓库', '3', NULL, NULL, '2026-08-20', '0', '李安全', @entId1, '0', '0', 'admin', NOW(), '测试数据-可燃气体检测报警系统'),
('TEST-EQP-005', '叉车', 'CPD-30', @areaId1, '一号厂区', '1', '5', 'TC-2024-005', '2027-03-01', '0', '赵叉车', @entId2, '0', '0', 'admin', NOW(), '测试数据-3吨电动叉车');

-- -----------------------------------------------
-- 6. 物料/危化品台账表 biz_safety_material
-- -----------------------------------------------
INSERT INTO biz_safety_material (material_code, material_name, hazard_class, cas_no, max_storage, storage_unit, storage_area_id, storage_area_name, toxic_flag, current_stock, enterprise_id, status, del_flag, create_by, create_time, remark) VALUES
('TEST-MAT-001', '氢氧化锂', '8', '1310-65-2', 5000.00, 'kg', @areaId3, '危化品仓库', '0', 1200.00, @entId1, '0', '0', 'admin', NOW(), '测试数据-单水氢氧化锂原料'),
('TEST-MAT-002', '六氟磷酸锂', '8', '21324-40-3', 2000.00, 'kg', @areaId3, '危化品仓库', '0', 800.00, @entId1, '0', '0', 'admin', NOW(), '测试数据-电解液核心原料'),
('TEST-MAT-003', 'N-甲基吡咯烷酮', '5', '872-50-4', 10000.00, 'kg', @areaId3, '危化品仓库', '0', 3500.00, @entId1, '0', '0', 'admin', NOW(), '测试数据-NMP溶剂'),
('TEST-MAT-004', '液氨', '4', '7664-41-7', 20000.00, 'kg', @areaId3, '危化品仓库', '1', 8500.00, @entId2, '0', '0', 'admin', NOW(), '测试数据-制冷用液氨(剧毒)'),
('TEST-MAT-005', '硝酸', '8', '7697-37-2', 8000.00, 'kg', @areaId3, '危化品仓库', '2', 2100.00, @entId2, '0', '0', 'admin', NOW(), '测试数据-工业硝酸(易制爆)');

-- -----------------------------------------------
-- 7. 风险点管理表 biz_safety_risk_point
-- -----------------------------------------------
INSERT INTO biz_safety_risk_point (risk_code, risk_name, area_id, area_name, hazard_desc, accident_type, l_value, e_value, c_value, d_value, risk_level, control_level, measure_engine, measure_manage, measure_training, measure_ppe, measure_emergency, check_standard, check_frequency, person_name, evaluate_date, re_evaluate_date, enterprise_id, status, del_flag, create_by, create_time, remark) VALUES
('TEST-RISK-001', '液氨储罐区', @areaId3, '危化品仓库', '液氨储罐泄漏可能导致中毒和环境污染', '中毒窒息、化学灼伤', 3.00, 6.00, 15.00, 270.00, '1', '1', '安装气体泄漏报警器和自动喷淋系统', '严格执行危化品操作规程，定期巡检', '操作人员持证上岗，定期安全培训', '配备防毒面具、防护服、护目镜', '启动应急预案，疏散人员，切断泄漏源', '储罐无泄漏，报警器有效，喷淋系统正常', '2', '李安全', '2025-01-15', '2026-01-15', @entId1, '0', '0', 'admin', NOW(), '测试数据-重大风险点'),
('TEST-RISK-002', '锅炉房', @areaId4, '动力配电区', '锅炉超压运行可能导致爆炸', '锅炉爆炸、烫伤', 2.00, 6.00, 40.00, 480.00, '1', '1', '安装安全阀、压力表，设置联锁保护', '持证司炉工操作，定期检验', '司炉工培训考核', '防烫手套、防护面罩', '紧急停炉，疏散人员', '安全阀在校验期内，压力表指示正常', '2', '刘电工', '2025-02-01', '2026-02-01', @entId1, '0', '0', 'admin', NOW(), '测试数据-较大风险点'),
('TEST-RISK-003', 'NMP储罐区', @areaId3, '危化品仓库', 'NMP泄漏可能引起火灾和中毒', '火灾、中毒', 2.00, 3.00, 7.00, 42.00, '3', '2', '密闭储存，安装液位计和泄漏报警', '执行危化品管理制', '危化品安全培训', '防毒面具、防护手套', '启动应急回收程序', '储罐密封良好，液位正常', '3', '王保管', '2025-03-10', '2026-03-10', @entId1, '0', '0', 'admin', NOW(), '测试数据-一般风险点'),
('TEST-RISK-004', '起重机作业区', @areaId2, '锂电池生产车间', '起重机吊装作业可能导致重物坠落伤人', '起重伤害', 1.00, 3.00, 7.00, 21.00, '3', '3', '限位器、缓冲器齐全有效', '持证操作，十不吊规程', '起重机操作培训', '安全帽、安全鞋', '设置警戒区，紧急停机', '限位器灵敏，钢丝绳无磨损', '3', '张车间', '2025-04-01', '2026-04-01', @entId1, '0', '0', 'admin', NOW(), '测试数据-一般风险点'),
('TEST-RISK-005', '办公区消防通道', @areaId1, '一号厂区', '消防通道堵塞可能影响紧急疏散', '火灾', 1.00, 1.00, 7.00, 7.00, '4', '4', '设置应急照明和疏散指示标志', '定期检查消防通道畅通', '消防安全培训', NULL, '启动消防应急疏散', '消防通道畅通，应急照明正常', '4', '陈管理', '2025-05-15', '2026-05-15', @entId1, '0', '0', 'admin', NOW(), '测试数据-低风险点');

SET @riskId1 = (SELECT risk_point_id FROM biz_safety_risk_point WHERE risk_code = 'TEST-RISK-001' LIMIT 1);
SET @riskId2 = (SELECT risk_point_id FROM biz_safety_risk_point WHERE risk_code = 'TEST-RISK-002' LIMIT 1);
SET @riskId3 = (SELECT risk_point_id FROM biz_safety_risk_point WHERE risk_code = 'TEST-RISK-003' LIMIT 1);

-- -----------------------------------------------
-- 8. 隐患管理表 biz_safety_hazard
-- -----------------------------------------------
INSERT INTO biz_safety_hazard (hazard_code, risk_point_id, hazard_desc, hazard_type, hazard_level, discover_person, discover_time, source_type, dept_name, person_name, rectify_req, rectify_deadline, hazard_status, rectify_desc, rectify_time, verify_person, verify_time, verify_result, verify_comment, enterprise_id, del_flag, create_by, create_time, remark) VALUES
('TEST-HZD-001', @riskId1, '液氨储罐区可燃气体报警器灵敏度下降，未及时校准', '2', '2', '李安全', '2026-07-10 09:30:00', '2', '安全管理部', '王保管', '一周内完成报警器校准并出具报告', '2026-07-17', '4', '已更换传感器并完成校准，出具校准报告', '2026-07-14 15:00:00', '李安全', '2026-07-15 10:00:00', '1', '整改到位，报警器校准合格', @entId1, '0', 'admin', NOW(), '测试数据-重大隐患已闭环'),
('TEST-HZD-002', @riskId2, '锅炉安全阀校验过期，未及时送检', '3', '2', '刘电工', '2026-07-12 14:00:00', '1', '动力部', '刘电工', '立即停炉，送检安全阀', '2026-07-15', '3', '已拆下送检，等待检测结果', '2026-07-13 16:00:00', NULL, NULL, NULL, NULL, @entId1, '0', 'admin', NOW(), '测试数据-重大隐患待验收'),
('TEST-HZD-003', @riskId3, 'NMP储罐区防溢堤有裂缝，存在泄漏扩散风险', '2', '1', '王保管', '2026-07-15 10:20:00', '3', '仓储部', '陈管理', '修补防溢堤裂缝，做防腐处理', '2026-07-25', '2', NULL, NULL, NULL, NULL, NULL, NULL, @entId1, '0', 'admin', NOW(), '测试数据-一般隐患整改中'),
('TEST-HZD-004', @riskId2, '锅炉房操作记录不完整，缺少交接班记录', '3', '1', '张车间', '2026-07-18 08:00:00', '2', '动力部', '刘电工', '完善交接班记录制度', '2026-07-22', '1', NULL, NULL, NULL, NULL, NULL, NULL, @entId1, '0', 'admin', NOW(), '测试数据-一般隐患待整改'),
('TEST-HZD-005', @riskId1, '液氨储罐区操作人员未佩戴防毒面具', '1', '1', '李安全', '2026-07-20 11:00:00', '1', '生产部', '张车间', '立即佩戴防护用品，加强监督检查', '2026-07-21', '0', NULL, NULL, NULL, NULL, NULL, NULL, @entId1, '0', 'admin', NOW(), '测试数据-一般隐患草稿');

SET @hzdId1 = (SELECT hazard_id FROM biz_safety_hazard WHERE hazard_code = 'TEST-HZD-001' LIMIT 1);
SET @hzdId2 = (SELECT hazard_id FROM biz_safety_hazard WHERE hazard_code = 'TEST-HZD-002' LIMIT 1);
SET @hzdId3 = (SELECT hazard_id FROM biz_safety_hazard WHERE hazard_code = 'TEST-HZD-003' LIMIT 1);

-- -----------------------------------------------
-- 9. 隐患整改记录表 biz_safety_rectify
-- -----------------------------------------------
INSERT INTO biz_safety_rectify (hazard_id, rectify_type, rectify_plan, solution_desc, rectify_person, rectify_time, verify_person, verify_time, verify_result, verify_comment, del_flag, create_by, create_time, remark) VALUES
(@hzdId1, '1', '更换气体报警器传感器，重新校准', '已采购新传感器并完成更换，经第三方校准合格', '王保管', '2026-07-14 14:00:00', '李安全', '2026-07-15 10:00:00', '1', '校准报告编号JC-2026-0715，报警器灵敏度恢复正常', '0', 'admin', NOW(), '测试数据-隐患整改完成并验收通过'),
(@hzdId2, '2', '安全阀拆下送特检院校验', '安全阀已拆下，送云南省特种设备检验院校验中', '刘电工', '2026-07-13 16:00:00', NULL, NULL, NULL, NULL, '0', 'admin', NOW(), '测试数据-隐患整改进行中，待校验结果'),
(@hzdId3, '1', '修补防溢堤裂缝，做环氧防腐处理', '正在施工中，预计3天内完成', '陈管理', '2026-07-16 09:00:00', NULL, NULL, NULL, NULL, '0', 'admin', NOW(), '测试数据-隐患整改施工中'),
(@hzdId1, '3', '建立报警器定期校准制度', '已编制《气体报警器校准管理制度》并发布执行', '李安全', '2026-07-15 14:00:00', '李安全', '2026-07-16 09:00:00', '1', '制度已发布，执行到位', '0', 'admin', NOW(), '测试数据-管理类整改措施'),
(@hzdId2, '2', '锅炉安全阀更换备用阀', '已安装校验合格的备用安全阀，原阀送检', '刘电工', '2026-07-14 10:00:00', NULL, NULL, NULL, NULL, '0', 'admin', NOW(), '测试数据-临时整改措施');

-- -----------------------------------------------
-- 10. 排查任务表 biz_safety_inspection_task
-- -----------------------------------------------
INSERT INTO biz_safety_inspection_task (task_code, task_type, task_name, risk_point_id, plan_date, executor_name, task_status, inspect_result, hazard_count, inspect_time, enterprise_id, del_flag, create_by, create_time, remark) VALUES
('TEST-TASK-001', '1', '液氨储罐区日常排查', @riskId1, '2026-08-01', '李安全', '2', '1', 0, '2026-08-01 09:30:00', @entId1, '0', 'admin', NOW(), '测试数据-已完成排查，无隐患'),
('TEST-TASK-002', '1', '锅炉房安全排查', @riskId2, '2026-08-02', '刘电工', '2', '2', 1, '2026-08-02 10:00:00', @entId1, '0', 'admin', NOW(), '测试数据-已完成排查，发现1处隐患'),
('TEST-TASK-003', '2', 'NMP储罐区专项排查', @riskId3, '2026-08-05', '王保管', '1', NULL, NULL, NULL, @entId1, '0', 'admin', NOW(), '测试数据-排查进行中'),
('TEST-TASK-004', '3', '车间设备周排查', @riskId2, '2026-08-08', '张车间', '0', NULL, 0, NULL, @entId1, '0', 'admin', NOW(), '测试数据-待执行排查任务'),
('TEST-TASK-005', '1', '消防设施月度排查', @riskId1, '2026-08-10', '陈管理', '0', NULL, 0, NULL, @entId1, '0', 'admin', NOW(), '测试数据-待执行消防排查');

-- 关联隐患到排查任务：TEST-HZD-002（锅炉安全阀校验过期）关联到 TEST-TASK-002（锅炉房安全排查）
SET @taskId2 = (SELECT task_id FROM biz_safety_inspection_task WHERE task_code = 'TEST-TASK-002' LIMIT 1);
UPDATE biz_safety_hazard SET task_id = @taskId2, source_type = '2' WHERE hazard_code = 'TEST-HZD-002';

-- -----------------------------------------------
-- 11. 培训计划表 biz_safety_training_plan
-- -----------------------------------------------
INSERT INTO biz_safety_training_plan (plan_code, plan_name, plan_type, plan_year, target_persons, total_hours, plan_status, enterprise_id, status, del_flag, create_by, create_time, remark) VALUES
('TEST-TP-001', '2026年新员工三级安全教育', '1', '2026', '新入职员工', 24.00, '1', @entId1, '0', '0', 'admin', NOW(), '测试数据-新员工安全培训计划'),
('TEST-TP-002', '2026年特种作业人员复审培训', '2', '2026', '电工、焊工、叉车工', 16.00, '1', @entId1, '0', '0', 'admin', NOW(), '测试数据-特种作业复审培训'),
('TEST-TP-003', '2026年危化品安全管理培训', '5', '2026', '危化品操作及管理人员', 8.00, '1', @entId1, '0', '0', 'admin', NOW(), '测试数据-危化品专项培训'),
('TEST-TP-004', '2026年应急处置能力培训', '6', '2026', '全体员工', 12.00, '0', @entId1, '0', '0', 'admin', NOW(), '测试数据-应急培训计划'),
('TEST-TP-005', '2026年有限空间作业培训', '4', '2026', '有限空间作业人员', 8.00, '0', @entId1, '0', '0', 'admin', NOW(), '测试数据-有限空间专项培训');

SET @planId1 = (SELECT plan_id FROM biz_safety_training_plan WHERE plan_code = 'TEST-TP-001' LIMIT 1);
SET @planId2 = (SELECT plan_id FROM biz_safety_training_plan WHERE plan_code = 'TEST-TP-002' LIMIT 1);
SET @planId3 = (SELECT plan_id FROM biz_safety_training_plan WHERE plan_code = 'TEST-TP-003' LIMIT 1);

-- -----------------------------------------------
-- 12. 培训记录表 biz_safety_training_record
-- -----------------------------------------------
INSERT INTO biz_safety_training_record (plan_id, course_name, course_type, training_date, hours, trainer, persons, exam_score, is_pass, enterprise_id, del_flag, create_by, create_time, remark) VALUES
(@planId1, '公司级安全教育', '1', '2026-07-15', 8.00, '李安全', '张三、李四、王五', 95.00, '1', @entId1, '0', 'admin', NOW(), '测试数据-新员工公司级培训'),
(@planId2, '电工特种作业安全操作', '2', '2026-07-20', 8.00, '刘电工', '张电工', 88.00, '1', @entId1, '0', 'admin', NOW(), '测试数据-电工复审培训'),
(@planId3, '危化品基础知识与防护', '5', '2026-07-25', 4.00, '王保管', '张车间、陈管理', 92.00, '1', @entId1, '0', 'admin', NOW(), '测试数据-危化品安全培训'),
(@planId1, '车间级安全教育', '1', '2026-07-16', 8.00, '张车间', '张三、李四、王五', 85.00, '1', @entId1, '0', 'admin', NOW(), '测试数据-新员工车间级培训'),
(@planId2, '焊接作业安全规范', '2', '2026-07-22', 8.00, '赵安全', '李焊工', 55.00, '0', @entId1, '0', 'admin', NOW(), '测试数据-焊工培训不合格需补考');

-- -----------------------------------------------
-- 13. 应急预案表 biz_safety_emergency_plan
-- -----------------------------------------------
INSERT INTO biz_safety_emergency_plan (plan_code, plan_name, plan_type, plan_level, version, reg_no, reg_date, effective_date, expire_date, remind_status, content, enterprise_id, status, del_flag, create_by, create_time, remark) VALUES
('TEST-EP-001', '生产安全事故综合应急预案', '1', '1', 'V3.0', 'YN-2024-001', '2024-03-15', '2024-04-01', '2027-03-31', '0', '包括组织体系、运行机制、应急保障、监督管理等内容', @entId1, '0', '0', 'admin', NOW(), '测试数据-综合应急预案'),
('TEST-EP-002', '危险化学品泄漏专项应急预案', '2', '2', 'V2.1', 'YN-2024-002', '2024-05-20', '2024-06-01', '2027-05-31', '0', '针对液氨、NMP等危化品泄漏的应急处置方案', @entId1, '0', '0', 'admin', NOW(), '测试数据-危化品泄漏专项预案'),
('TEST-EP-003', '火灾爆炸事故专项应急预案', '2', '2', 'V2.0', 'YN-2024-003', '2024-06-10', '2024-07-01', '2027-06-30', '0', '火灾爆炸事故的应急响应和处置措施', @entId1, '0', '0', 'admin', NOW(), '测试数据-火灾爆炸专项预案'),
('TEST-EP-004', '锅炉房现场处置方案', '3', '3', 'V1.5', 'YN-2024-004', '2024-07-01', '2024-07-15', '2027-06-30', '0', '锅炉超压、爆炸等紧急情况的现场处置步骤', @entId1, '0', '0', 'admin', NOW(), '测试数据-锅炉房现场处置方案'),
('TEST-EP-005', '特种设备事故专项应急预案', '2', '2', 'V1.0', 'YN-2024-005', '2024-08-01', '2024-08-15', '2027-07-31', '0', '压力容器、起重机械等特种设备事故应急处置', @entId2, '0', '0', 'admin', NOW(), '测试数据-特种设备事故预案');

SET @epId1 = (SELECT plan_id FROM biz_safety_emergency_plan WHERE plan_code = 'TEST-EP-001' LIMIT 1);
SET @epId2 = (SELECT plan_id FROM biz_safety_emergency_plan WHERE plan_code = 'TEST-EP-002' LIMIT 1);

-- -----------------------------------------------
-- 14. 应急物资表 biz_safety_emergency_material
-- -----------------------------------------------
INSERT INTO biz_safety_emergency_material (material_code, material_name, material_type, spec_model, quantity, unit, storage_location, person_name, effective_date, remind_status, enterprise_id, status, del_flag, create_by, create_time, remark) VALUES
('TEST-EM-001', '正压式空气呼吸器', '防护装备', 'RHZKF-6.8', 4.00, '套', '应急物资储备间', '陈管理', '2028-06-30', '0', @entId1, '0', '0', 'admin', NOW(), '测试数据-正压式空气呼吸器'),
('TEST-EM-002', '防氨气面具', '防护装备', 'M-ZH', 10.00, '个', '危化品仓库旁', '王保管', '2027-12-31', '0', @entId1, '0', '0', 'admin', NOW(), '测试数据-防毒面具'),
('TEST-EM-003', '应急照明灯', '应急设备', 'LED-30W', 20.00, '个', '各楼层通道', '刘电工', '2028-01-31', '0', @entId1, '0', '0', 'admin', NOW(), '测试数据-应急照明灯'),
('TEST-EM-004', '干粉灭火器', '消防器材', 'MFZ/ABC8', 30.00, '个', '各区域消防点', '陈管理', '2026-12-31', '0', @entId1, '0', '0', 'admin', NOW(), '测试数据-8kg干粉灭火器'),
('TEST-EM-005', '应急药品箱', '医疗急救', '标准配置', 5.00, '箱', '门卫室、车间办公室', '李安全', '2026-09-30', '0', @entId1, '0', '0', 'admin', NOW(), '测试数据-急救药品箱');

-- -----------------------------------------------
-- 15. 演练记录表 biz_safety_drill
-- -----------------------------------------------
INSERT INTO biz_safety_drill (drill_code, plan_id, drill_type, drill_name, drill_date, participants, drill_desc, eval_result, problems, improvements, enterprise_id, del_flag, create_by, create_time, remark) VALUES
('TEST-DR-001', @epId1, '1', '2026年上半年综合应急演练', '2026-04-15', '全体员工(86人)', '模拟厂区发生火灾，启动综合应急预案，组织人员疏散、灭火、伤员救护', '演练整体效果良好，人员疏散时间3分20秒，达到预期目标', '部分新员工对疏散路线不熟悉；灭火器使用不熟练', '加强新员工消防培训；增加灭火器实操练习', @entId1, '0', 'admin', NOW(), '测试数据-综合应急演练'),
('TEST-DR-002', @epId2, '2', '液氨泄漏应急演练', '2026-05-20', '危化品区域人员(23人)', '模拟液氨储罐法兰泄漏，启动专项预案，穿戴防护服堵漏、喷水稀释', '堵漏操作规范，应急处置及时', '通信联络不够畅通，对讲机信号不稳定', '配备专用应急通信设备', @entId1, '0', 'admin', NOW(), '测试数据-液氨泄漏专项演练'),
('TEST-DR-003', @epId1, '3', '车间现场处置演练', '2026-06-10', '车间全体人员(35人)', '模拟锅炉超压紧急停炉操作', '操作人员反应迅速，处置步骤正确', '部分人员对阀门位置不熟悉', '制作阀门标识牌，加强现场培训', @entId1, '0', 'admin', NOW(), '测试数据-现场处置演练'),
('TEST-DR-004', @epId2, '2', 'NMP泄漏应急演练', '2026-07-05', '仓储部人员(12人)', '模拟NMP储罐管道泄漏，启动应急处置', '回收操作规范，未造成环境污染', '应急物资取用路径较长', '优化应急物资存放位置', @entId1, '0', 'admin', NOW(), '测试数据-NMP泄漏演练'),
('TEST-DR-005', @epId1, '1', '消防疏散演练', '2026-07-25', '全体员工(86人)', '模拟办公楼火灾，组织全员疏散至安全区域', '疏散时间2分50秒，较上次提升30秒', '消防通道有杂物堆放', '清理消防通道，加强日常检查', @entId1, '0', 'admin', NOW(), '测试数据-消防疏散演练');

-- -----------------------------------------------
-- 16. 事故管理表 biz_safety_incident
-- -----------------------------------------------
INSERT INTO biz_safety_incident (incident_code, incident_level, incident_date, incident_location, incident_desc, casualties, economic_loss, cause_analysis, four_not_let_go, rectify_measures, report_person, report_time, enterprise_id, del_flag, create_by, create_time, remark) VALUES
('TEST-INC-001', '3', '2026-06-15 14:30:00', '锂电池生产车间', '操作工在搬运电解液桶时，因地面湿滑摔倒，导致电解液桶倾斜溢出约5L', 0, 3500.00, '地面湿滑未及时清理；搬运操作不规范', '事故原因未查清不放过、责任人员未处理不放过、整改措施未落实不放过、有关人员未受到教育不放过', '加强地面清洁巡检；规范搬运操作流程；增加防滑垫', '张车间', '2026-06-15 15:00:00', @entId1, '0', 'admin', NOW(), '测试数据-一般未遂事故'),
('TEST-INC-002', '4', '2026-05-08 10:15:00', '危化品仓库', '保管员在盘点时发现液氨储罐阀门微漏，及时处置未造成伤亡', 0, 500.00, '阀门密封圈老化未及时更换', '已按四不放过原则处理', '建立阀门定期更换台账；加强日常巡检', '王保管', '2026-05-08 10:30:00', @entId1, '0', 'admin', NOW(), '测试数据-轻微泄漏事件'),
('TEST-INC-003', '2', '2026-03-20 16:00:00', '动力配电区', '锅炉房司炉工在操作时被蒸汽管道轻微烫伤手臂', 1, 2000.00, '蒸汽管道保温层破损；操作时未佩戴防护手套', '已按四不放过原则处理', '修复保温层；强制佩戴防护用品', '刘电工', '2026-03-20 16:30:00', @entId1, '0', 'admin', NOW(), '测试数据-较大工伤事故'),
('TEST-INC-004', '4', '2026-07-03 09:00:00', '一号厂区', '叉车在转弯时与货架轻微碰撞，造成货架变形', 0, 1500.00, '叉车速度过快；转弯未减速鸣笛', '已按四不放过原则处理', '限速5km/h；增设转弯凸面镜', '陈管理', '2026-07-03 09:20:00', @entId1, '0', 'admin', NOW(), '测试数据-设备损坏事故'),
('TEST-INC-005', '3', '2026-02-14 11:30:00', '锂电池生产车间', '起重机吊装时钢丝绳断裂，吊物坠落砸伤地面设备，无人员伤亡', 0, 12000.00, '钢丝绳磨损超标未及时更换；未做班前检查', '已按四不放过原则处理', '建立钢丝绳更换标准；强制班前检查', '张车间', '2026-02-14 12:00:00', @entId1, '0', 'admin', NOW(), '测试数据-设备损坏较大事故');

-- -----------------------------------------------
-- 17. 到期提醒表 biz_safety_remind
-- -----------------------------------------------
INSERT INTO biz_safety_remind (remind_type, related_name, remind_content, expire_date, remind_days, remind_status, notify_level, to_person_name, enterprise_id, del_flag, create_by, create_time, remark) VALUES
('1', '安全生产许可证(TEST-CERT-2026-001)', '安全生产许可证将于2026-12-31到期，请提前办理延期手续', '2026-12-31', 180, '0', '1', '李安全', @entId1, '0', 'admin', NOW(), '测试数据-证照到期提醒'),
('2', '张电工特种作业证(DLG-2024-001)', '电工特种作业证将于2027-03-14到期，请提前安排复审', '2027-03-14', 90, '0', '2', '张电工', @entId1, '0', 'admin', NOW(), '测试数据-特种作业证到期提醒'),
('3', '蒸汽锅炉(TEST-EQP-001)', '蒸汽锅炉检验有效期至2026-12-31，请提前送检', '2026-12-31', 60, '0', '1', '刘电工', @entId1, '0', 'admin', NOW(), '测试数据-设备检验到期提醒'),
('4', '干粉灭火器(TEST-EM-004)', '干粉灭火器有效期至2026-12-31，请提前更换', '2026-12-31', 30, '1', '2', '陈管理', @entId1, '0', 'admin', NOW(), '测试数据-应急物资过期提醒'),
('5', '隐患TEST-HZD-004', '隐患整改期限将至，请尽快完成整改', '2026-07-22', 3, '0', '2', '刘电工', @entId1, '0', 'admin', NOW(), '测试数据-隐患整改超期提醒');

-- -----------------------------------------------
-- 18. 检查标准库表 biz_safety_std_library
-- -----------------------------------------------
INSERT INTO biz_safety_std_library (industry_type, area_type, risk_point_name, hazard_desc, l_value, e_value, c_value, d_value, risk_level, check_standard, check_frequency, control_measures, is_major_hazard, major_hazard_rule, status, del_flag, create_by, create_time, remark) VALUES
('1', '3', '危化品储罐区', '危化品储罐泄漏导致中毒、火灾或爆炸', 3.00, 6.00, 15.00, 270.00, '1', '储罐无泄漏，安全附件有效，防溢堤完整', '2', '安装泄漏报警、自动喷淋、防溢堤', '1', '构成重大危险源的危化品储罐未设置安全监测系统判定为重大隐患', '0', '0', 'admin', NOW(), '测试数据-新能源材料行业风险库'),
('1', '1', '锂电池生产车间', '电解液泄漏遇水反应产生有害气体', 2.00, 6.00, 7.00, 84.00, '2', '车间通风良好，电解液储存规范，无水源', '2', '强制通风系统，防潮储存', '0', NULL, '0', '0', 'admin', NOW(), '测试数据-锂电池行业风险库'),
('2', '8', '铝加工配电区', '高压电气设备漏电导致触电事故', 3.00, 3.00, 15.00, 135.00, '1', '接地保护有效，绝缘手套在有效期，漏电保护器灵敏', '3', '接地保护，漏电保护，绝缘防护', '0', NULL, '0', '0', 'admin', NOW(), '测试数据-铝加工行业风险库'),
('3', '1', '化工反应釜区域', '反应釜超温超压导致物料喷出或爆炸', 3.00, 6.00, 40.00, 720.00, '1', '温度压力在控制范围内，安全阀灵敏，联锁有效', '2', '温度压力联锁，安全阀泄压，紧急冷却', '1', '反应釜未设置温度压力联锁保护判定为重大隐患', '0', '0', 'admin', NOW(), '测试数据-精细化工行业风险库'),
('4', '1', '装备组装车间', '起重机械吊装作业重物坠落伤人', 1.00, 3.00, 7.00, 21.00, '3', '钢丝绳无磨损，限位器灵敏，操作人员持证', '3', '限位保护，持证操作，警戒区设置', '0', NULL, '0', '0', 'admin', NOW(), '测试数据-装备制造行业风险库');


-- =============================================
-- 二、质量管理模块 测试数据
-- =============================================

-- -----------------------------------------------
-- 1. 行业模板配置表 qms_industry_tpl
-- -----------------------------------------------
INSERT INTO qms_industry_tpl (tpl_code, tpl_name, industry_type, audit_force, status, del_flag, create_by, create_time, remark) VALUES
('TEST-TPL-001', '新能源材料行业质量模板', '1', '1', '0', '0', 'admin', NOW(), '测试数据-新能源材料行业模板'),
('TEST-TPL-002', '绿色铝加工行业质量模板', '2', '1', '0', '0', 'admin', NOW(), '测试数据-铝加工行业模板'),
('TEST-TPL-003', '精细化工行业质量模板', '3', '1', '0', '0', 'admin', NOW(), '测试数据-精细化工行业模板'),
('TEST-TPL-004', '先进装备行业质量模板', '4', '0', '0', '0', 'admin', NOW(), '测试数据-装备制造行业模板'),
('TEST-TPL-005', '高原食品行业质量模板', '5', '0', '0', '0', 'admin', NOW(), '测试数据-食品加工行业模板');

SET @tplId1 = (SELECT tpl_id FROM qms_industry_tpl WHERE tpl_code = 'TEST-TPL-001' LIMIT 1);
SET @tplId2 = (SELECT tpl_id FROM qms_industry_tpl WHERE tpl_code = 'TEST-TPL-002' LIMIT 1);
SET @tplId3 = (SELECT tpl_id FROM qms_industry_tpl WHERE tpl_code = 'TEST-TPL-003' LIMIT 1);

-- -----------------------------------------------
-- 2. 检验标准库表 qms_inspect_std
-- -----------------------------------------------
INSERT INTO qms_inspect_std (std_code, std_name, inspect_method, unit, val_min, val_max, target_val, judge_rule, tpl_id, status, del_flag, create_by, create_time, remark) VALUES
('TEST-STD-001', '氢氧化锂主含量', '酸碱滴定法', '%', 56.5000, 57.5000, 57.0000, '1', @tplId1, '0', '0', 'admin', NOW(), '测试数据-氢氧化锂含量检验标准'),
('TEST-STD-002', '电解液水分含量', '卡尔费休法', 'ppm', NULL, 20.0000, 10.0000, '2', @tplId1, '0', '0', 'admin', NOW(), '测试数据-电解液水分上限标准'),
('TEST-STD-003', '铝锭化学成分Si', '光谱分析法', '%', NULL, 0.0800, 0.0500, '2', @tplId2, '0', '0', 'admin', NOW(), '测试数据-铝锭硅含量标准'),
('TEST-STD-004', '反应釜温度控制', '温度记录仪', '℃', 80.0000, 120.0000, 100.0000, '1', @tplId3, '0', '0', 'admin', NOW(), '测试数据-反应釜温度区间标准'),
('TEST-STD-005', '外观检验', '目视检查', NULL, NULL, NULL, NULL, '3', @tplId1, '0', '0', 'admin', NOW(), '测试数据-定性外观检验标准');

SET @stdId1 = (SELECT std_id FROM qms_inspect_std WHERE std_code = 'TEST-STD-001' LIMIT 1);
SET @stdId2 = (SELECT std_id FROM qms_inspect_std WHERE std_code = 'TEST-STD-002' LIMIT 1);
SET @stdId5 = (SELECT std_id FROM qms_inspect_std WHERE std_code = 'TEST-STD-005' LIMIT 1);

-- -----------------------------------------------
-- 3. 缺陷代码表 qms_defect_code
-- -----------------------------------------------
INSERT INTO qms_defect_code (defect_code, defect_name, defect_level, defect_category, status, del_flag, create_by, create_time, remark) VALUES
('TEST-DF-001', '主含量不达标', '2', '化学性能', '0', '0', 'admin', NOW(), '测试数据-主含量偏低或偏高'),
('TEST-DF-002', '水分超标', '2', '物理性能', '0', '0', 'admin', NOW(), '测试数据-水分含量超过标准上限'),
('TEST-DF-003', '外观有异物', '3', '外观', '0', '0', 'admin', NOW(), '测试数据-产品表面存在异物'),
('TEST-DF-004', '包装破损', '3', '包装', '0', '0', 'admin', NOW(), '测试数据-运输包装损坏'),
('TEST-DF-005', '批次混料', '1', '管理', '0', '0', 'admin', NOW(), '测试数据-不同批次物料混装');

-- -----------------------------------------------
-- 4. 量检具台账表 qms_gauge
-- -----------------------------------------------
INSERT INTO qms_gauge (gauge_no, gauge_name, gauge_type, precision_val, measure_range, calib_cycle, last_calib_date, next_calib_date, gauge_status, status, del_flag, create_by, create_time, remark) VALUES
('TEST-GG-001', '电子天平', '称量类', '0.0001g', '0-220g', 12, '2025-08-15', '2026-08-15', '0', '0', '0', 'admin', NOW(), '测试数据-万分之一电子天平'),
('TEST-GG-002', '卡尔费休水分仪', '分析类', '0.001%', '0.001%-100%', 12, '2025-06-20', '2026-06-20', '2', '0', '0', 'admin', NOW(), '测试数据-水分测定仪待校准'),
('TEST-GG-003', '数字卡尺', '量具类', '0.01mm', '0-150mm', 6, '2026-02-10', '2026-08-10', '0', '0', '0', 'admin', NOW(), '测试数据-数显卡尺'),
('TEST-GG-004', '光谱分析仪', '分析类', '0.001%', '0-100%', 12, '2025-09-01', '2026-09-01', '0', '0', '0', 'admin', NOW(), '测试数据-直读光谱仪'),
('TEST-GG-005', '温度记录仪', '监测类', '0.1℃', '-40~250℃', 12, '2025-11-15', '2026-11-15', '0', '0', '0', 'admin', NOW(), '测试数据-温度数据记录仪');

SET @gaugeId1 = (SELECT gauge_id FROM qms_gauge WHERE gauge_no = 'TEST-GG-001' LIMIT 1);
SET @gaugeId2 = (SELECT gauge_id FROM qms_gauge WHERE gauge_no = 'TEST-GG-002' LIMIT 1);

-- -----------------------------------------------
-- 5. 物料质量属性表 qms_material_attr
-- -----------------------------------------------
INSERT INTO qms_material_attr (material_id, material_code, material_name, inspect_types, tpl_id, is_exempt, status, del_flag, create_by, create_time, remark) VALUES
(9001, 'TEST-MAT-Q001', '氢氧化锂(电池级)', 'IQC,IPQC,FQC,OQC', @tplId1, '0', '0', '0', 'admin', NOW(), '测试数据-电池级氢氧化锂质量属性'),
(9002, 'TEST-MAT-Q002', '六氟磷酸锂', 'IQC,IPQC', @tplId1, '0', '0', '0', 'admin', NOW(), '测试数据-六氟磷酸锂质量属性'),
(9003, 'TEST-MAT-Q003', 'N-甲基吡咯烷酮', 'IQC', @tplId1, '0', '0', '0', 'admin', NOW(), '测试数据-NMP溶剂来料检验'),
(9004, 'TEST-MAT-Q004', '铝锭(A356)', 'IQC,IPQC,FQC', @tplId2, '0', '0', '0', 'admin', NOW(), '测试数据-铝锭质量属性'),
(9005, 'TEST-MAT-Q005', '包装材料(纸箱)', 'IQC', @tplId1, '1', '0', '0', 'admin', NOW(), '测试数据-免检包装材料');

-- -----------------------------------------------
-- 6. 检验任务表 qms_insp_task
-- -----------------------------------------------
INSERT INTO qms_insp_task (task_no, task_type, source_type, source_no, material_id, material_code, material_name, supplier_id, supplier_name, batch_no, inspect_qty, aql_level, inspect_level, sample_size, ac_val, re_val, task_status, inspect_result, defect_level, inspector_name, inspect_time, judge_time, gauge_id, gauge_name, is_recheck, status, del_flag, create_by, create_time, remark) VALUES
('TEST-IT-001', 'IQC', 'arrival', 'RK20260801001', 9001, 'TEST-MAT-Q001', '氢氧化锂(电池级)', 1001, '测试供应商A', 'BH20260801-01', 500.0000, 'II', '1', 50, 2, 3, '2', '1', NULL, '质检员王', '2026-08-01 14:00:00', '2026-08-01 16:00:00', @gaugeId1, '电子天平', '0', '0', '0', 'admin', NOW(), '测试数据-IQC来料检验合格'),
('TEST-IT-002', 'IQC', 'arrival', 'RK20260802001', 9002, 'TEST-MAT-Q002', '六氟磷酸锂', 1002, '测试供应商B', 'BH20260802-01', 200.0000, 'II', '1', 32, 1, 2, '2', '2', '2', '质检员李', '2026-08-02 10:00:00', '2026-08-02 12:00:00', @gaugeId2, '卡尔费休水分仪', '0', '0', '0', 'admin', NOW(), '测试数据-IQC来料检验不合格(水分超标)'),
('TEST-IT-003', 'IPQC', 'production', 'WO20260803001', 9001, 'TEST-MAT-Q001', '氢氧化锂(电池级)', NULL, NULL, 'PC20260803-01', 1000.0000, 'S-3', '1', 13, 1, 2, '1', NULL, NULL, '质检员王', '2026-08-03 14:30:00', NULL, @gaugeId1, '电子天平', '0', '0', '0', 'admin', NOW(), '测试数据-IPQC过程检验进行中'),
('TEST-IT-004', 'FQC', 'production', 'WO20260804001', 9004, 'TEST-MAT-Q004', '铝锭(A356)', NULL, NULL, 'PC20260804-01', 5000.0000, 'II', '2', 80, 3, 4, '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0', '0', '0', 'admin', NOW(), '测试数据-FQC成品检验待检'),
('TEST-IT-005', 'IQC', 'manual', NULL, 9003, 'TEST-MAT-Q003', 'N-甲基吡咯烷酮', 1003, '测试供应商C', 'BH20260805-01', 300.0000, 'II', '3', 20, 1, 2, '2', '1', NULL, '质检员李', '2026-08-05 09:00:00', '2026-08-05 11:00:00', @gaugeId2, '卡尔费休水分仪', '0', '0', '0', 'admin', NOW(), '测试数据-IQC来料检验放宽合格');

SET @taskId1 = (SELECT task_id FROM qms_insp_task WHERE task_no = 'TEST-IT-001' LIMIT 1);
SET @taskId2 = (SELECT task_id FROM qms_insp_task WHERE task_no = 'TEST-IT-002' LIMIT 1);

-- -----------------------------------------------
-- 7. 不合格品报告(NCR)表 qms_ncr
-- -----------------------------------------------
INSERT INTO qms_ncr (ncr_no, source_type, source_id, source_no, task_id, material_id, material_code, material_name, supplier_id, supplier_name, batch_no, defect_qty, defect_level, defect_desc, disposition, isolate_flag, isolate_area, ncr_status, current_node, max_node, discoverer_name, discover_time, resp_dept_name, verify_result, verify_remark, verifier_name, verify_time, close_time, status, del_flag, create_by, create_time, remark) VALUES
('TEST-NCR-001', 'inspection', NULL, 'TEST-IT-002', @taskId2, 9002, 'TEST-MAT-Q002', '六氟磷酸锂', 1002, '测试供应商B', 'BH20260802-01', 32.0000, '2', '来料检验发现水分含量25ppm，超出标准上限20ppm，判定不合格', '1', '1', '不合格品隔离区A', '4', 0, 3, '质检员李', '2026-08-02 12:00:00', '采购部', '1', '供应商已退货更换', '质量经理', '2026-08-05 15:00:00', '2026-08-05 16:00:00', '0', '0', 'admin', NOW(), '测试数据-NCR已关闭(退货)'),
('TEST-NCR-002', 'inspection', NULL, 'TEST-IT-001', @taskId1, 9001, 'TEST-MAT-Q001', '氢氧化锂(电池级)', 1001, '测试供应商A', 'BH20260801-01', 5.0000, '3', '抽样50件中发现5件外观有轻微变色', '5', '0', NULL, '3', 2, 3, '质检员王', '2026-08-01 16:30:00', '品质部', NULL, NULL, NULL, NULL, NULL, '0', '0', 'admin', NOW(), '测试数据-NCR待验证(让步接收)'),
('TEST-NCR-003', 'complaint', NULL, 'CS20260701', NULL, 9001, 'TEST-MAT-Q001', '氢氧化锂(电池级)', NULL, NULL, 'BH20260615-01', 20.0000, '2', '客户反馈产品主含量偏低，使用后影响电池性能', '2', '1', '客退品隔离区', '2', 1, 3, '客服部', '2026-07-01 10:00:00', '生产部', NULL, NULL, NULL, NULL, NULL, '0', '0', 'admin', NOW(), '测试数据-NCR处置中(挑选使用)'),
('TEST-NCR-004', 'patrol', NULL, NULL, NULL, 9004, 'TEST-MAT-Q004', '铝锭(A356)', NULL, NULL, 'PC20260720-01', 100.0000, '3', '巡检发现铝锭表面有气孔缺陷', '3', '1', '返工区', '1', 1, 3, '巡检员赵', '2026-07-20 14:00:00', '生产部', NULL, NULL, NULL, NULL, NULL, '0', '0', 'admin', NOW(), '测试数据-NCR评审中(返工)'),
('TEST-NCR-005', 'inspection', NULL, 'TEST-IT-002', @taskId2, 9002, 'TEST-MAT-Q002', '六氟磷酸锂', 1002, '测试供应商B', 'BH20260802-01', 8.0000, '4', '部分包装标签信息不完整', '5', '0', NULL, '0', 0, 3, '质检员李', '2026-08-02 13:00:00', '仓储部', NULL, NULL, NULL, NULL, NULL, '0', '0', 'admin', NOW(), '测试数据-NCR已登记(让步接收)');

-- -----------------------------------------------
-- 8. CAPA纠正预防措施表 qms_capa
-- -----------------------------------------------
INSERT INTO qms_capa (capa_no, source_type, source_no, title, problem_desc, team_members, interim_action, root_cause, permanent_action, verify_result, prevent_action, close_remark, capa_status, responsible_person, plan_close_time, close_time, del_flag, status, create_by, create_time, remark) VALUES
('TEST-CAPA-001', 'ncr', 'TEST-NCR-001', '六氟磷酸锂水分超标问题', '来料六氟磷酸锂批次BH20260802-01水分含量25ppm，超出标准上限20ppm', '质量经理(组长),采购经理,仓储主管,供应商质量工程师', '已隔离不合格品32kg，通知供应商退货', '供应商运输过程中干燥剂失效，包装密封性不足导致吸潮', '要求供应商更换防潮包装，增加双层干燥剂', '供应商更换包装后连续3批次水分≤15ppm，验证通过', '修订供应商准入标准，增加包装密封性要求', '问题已彻底解决，连续3批次合格', '3', '质量经理', '2026-08-10', '2026-08-08 16:00:00', '0', '0', 'admin', NOW(), '测试数据-CAPA已关闭'),
('TEST-CAPA-002', 'complaint', 'TEST-NCR-003', '客户反馈主含量偏低问题', '客户反馈批次BH20260615-01氢氧化锂主含量56.2%，低于标准下限56.5%', '品质经理(组长),生产主管,技术工程师', '已暂停发货，召回同批次产品20kg', '生产过程中反应温度控制偏差导致主含量偏低', '优化反应釜温度控制参数，增加过程检测频次', '调整参数后连续5批次主含量≥57.0%，验证通过', '建立温度控制预警机制，偏差±2℃自动报警', '整改有效，已恢复正常发货', '3', '品质经理', '2026-08-15', '2026-08-12 10:00:00', '0', '0', 'admin', NOW(), '测试数据-CAPA已关闭'),
('TEST-CAPA-003', 'ncr', 'TEST-NCR-004', '铝锭表面气孔缺陷问题', '巡检发现铝锭批次PC20260720-01表面存在气孔缺陷', '生产经理(组长),铸造工程师,质检主管', '已隔离100kg缺陷品进行返工处理', '铸造温度偏高导致气体溶解度变化产生气孔', '调整铸造温度参数，增加除气精炼工序', NULL, '建立铸造温度监控记录，每炉检测气体含量', NULL, '1', '生产经理', '2026-08-20', NULL, '0', '0', 'admin', NOW(), '测试数据-CAPA进行中'),
('TEST-CAPA-004', 'audit', 'TEST-AP-001', '内审发现检验记录不完整', '内审发现IPQC检验记录缺少实测值，判定依据不充分', '质量主管(组长),检验员,文控员', '补充缺失的检验记录', '检验人员对记录要求不明确，缺少培训', '修订检验记录规范，组织检验员培训', NULL, '将检验记录完整性纳入月度考核', NULL, '2', '质量主管', '2026-08-25', NULL, '0', '0', 'admin', NOW(), '测试数据-CAPA验证中'),
('TEST-CAPA-005', 'complaint', 'CS20260715', '客户投诉交期延误导致停产', '客户因交期延误2天导致产线停工，投诉要求赔偿', '销售经理(组长),计划主管,物流经理', '已紧急调货满足客户需求', '生产计划排产不合理，未考虑设备维护停机时间', '优化生产计划排产逻辑，预留设备维护窗口', NULL, '建立交期预警机制，提前7天预警', NULL, '0', '销售经理', '2026-08-30', NULL, '0', '0', 'admin', NOW(), '测试数据-CAPA草稿');

-- -----------------------------------------------
-- 9. 供应商质量评价表 qms_supplier_eval
-- -----------------------------------------------
INSERT INTO qms_supplier_eval (supplier_id, supplier_name, eval_period, eval_type, incoming_total, incoming_pass, ppm, pass_rate, quality_score, delivery_score, service_score, total_score, grade, eval_status, del_flag, status, create_by, create_time, remark) VALUES
(1001, '测试供应商A', '2026-07', 'monthly', 25, 24, 4000.00, 96.00, 92.00, 90.00, 88.00, 90.33, 'A', '1', '0', '0', 'admin', NOW(), '测试数据-供应商A月度评价A级'),
(1002, '测试供应商B', '2026-07', 'monthly', 18, 15, 16666.67, 83.33, 75.00, 82.00, 80.00, 78.50, 'C', '1', '0', '0', 'admin', NOW(), '测试数据-供应商B月度评价C级(水分超标)'),
(1003, '测试供应商C', '2026-07', 'monthly', 12, 12, 0.00, 100.00, 95.00, 93.00, 90.00, 93.00, 'A', '1', '0', '0', 'admin', NOW(), '测试数据-供应商C月度评价A级(全合格)'),
(1001, '测试供应商A', '2026-Q2', 'quarterly', 75, 72, 4000.00, 96.00, 91.00, 89.00, 87.00, 89.50, 'B', '1', '0', '0', 'admin', NOW(), '测试数据-供应商A季度评价B级'),
(1004, '测试供应商D', '2026-Q2', 'quarterly', 40, 30, 25000.00, 75.00, 65.00, 70.00, 68.00, 67.33, 'D', '1', '0', '0', 'admin', NOW(), '测试数据-供应商D季度评价D级');

-- -----------------------------------------------
-- 10. 供应商审核表 qms_supplier_audit
-- -----------------------------------------------
INSERT INTO qms_supplier_audit (audit_no, supplier_id, supplier_name, audit_type, audit_date, auditor, audit_scope, audit_result, nonconformity, audit_status, del_flag, status, create_by, create_time, remark) VALUES
('TEST-SA-001', 1001, '测试供应商A', 'initial', '2026-01-15', '审核员张', '质量管理体系、生产过程控制、检验能力', 'pass', '无不符合项，建议持续改进检验记录追溯性', '2', '0', '0', 'admin', NOW(), '测试数据-供应商A初审通过'),
('TEST-SA-002', 1002, '测试供应商B', 'routine', '2026-04-20', '审核员李', '质量管理体系、仓储管理、运输防护', 'conditional', '包装密封性不足，干燥剂配置不合理(一般不符合)', '2', '0', '0', 'admin', NOW(), '测试数据-供应商B例行审核有条件通过'),
('TEST-SA-003', 1002, '测试供应商B', 'follow', '2026-08-10', '审核员李', '包装改进措施验证', 'pass', '包装已更换为防潮双层包装，干燥剂增加至3包', '1', '0', '0', 'admin', NOW(), '测试数据-供应商B跟踪审核通过'),
('TEST-SA-004', 1003, '测试供应商C', 'initial', '2026-03-10', '审核员王', '质量管理体系、生产环境、检测设备', 'pass', '质量体系运行有效，检测设备校准齐全', '2', '0', '0', 'admin', NOW(), '测试数据-供应商C初审通过'),
('TEST-SA-005', 1004, '测试供应商D', 'routine', '2026-05-15', '审核员张', '质量管理体系、过程控制、不合格品管理', 'fail', '过程控制不充分，不合格品隔离措施不到位，检验记录不完整(严重不符合)', '2', '0', '0', 'admin', NOW(), '测试数据-供应商D审核不通过');

-- -----------------------------------------------
-- 11. 客诉表 qms_complaint
-- -----------------------------------------------
INSERT INTO qms_complaint (complaint_no, customer_name, customer_code, material_name, material_code, batch_no, complaint_date, complaint_type, defect_desc, severity, return_qty, return_amt, claim_amt, handle_desc, handle_result, complaint_status, close_time, del_flag, status, create_by, create_time, remark) VALUES
('TEST-CS-001', '宁德时代新能源', 'CUST001', '氢氧化锂(电池级)', 'TEST-MAT-Q001', 'BH20260615-01', '2026-07-01', 'quality', '客户反馈产品主含量56.2%，低于标准下限56.5%，影响电池性能', '2', 20.0000, 8000.00, 5000.00, '已启动8D分析，确认为生产过程温度控制偏差，已调整工艺参数，召回并更换合格产品', 'exchange', '3', '2026-08-12 10:00:00', '0', '0', 'admin', NOW(), '测试数据-质量投诉已关闭'),
('TEST-CS-002', '比亚迪股份', 'CUST002', '六氟磷酸锂', 'TEST-MAT-Q002', 'BH20260710-01', '2026-07-15', 'delivery', '订单交期延误2天，导致客户产线停工', '1', 0.0000, 0.00, 15000.00, '已赔偿客户停工损失，优化排产逻辑', 'compensate', '3', '2026-07-20 15:00:00', '0', '0', 'admin', NOW(), '测试数据-交期投诉已关闭'),
('TEST-CS-003', '亿纬锂能', 'CUST003', 'N-甲基吡咯烷酮', 'TEST-MAT-Q003', 'BH20260720-01', '2026-07-25', 'quality', '客户反馈NMP外观有轻微黄色，疑为品质异常', '3', 0.0000, 0.00, 0.00, '经技术分析为正常批次差异，不影响使用，已向客户解释并提供检测报告', 'close', '3', '2026-07-28 14:00:00', '0', '0', 'admin', NOW(), '测试数据-质量投诉已关闭(正常)'),
('TEST-CS-004', '国轩高科', 'CUST004', '氢氧化锂(电池级)', 'TEST-MAT-Q001', 'BH20260801-01', '2026-08-03', 'quality', '客户反馈包装标签信息不完整，缺少生产日期', '3', 0.0000, 0.00, 0.00, '已补发标签并指导客户贴标，改进标签打印流程', 'close', '1', NULL, '0', '0', 'admin', NOW(), '测试数据-质量投诉处理中'),
('TEST-CS-005', '蜂巢能源', 'CUST005', '铝锭(A356)', 'TEST-MAT-Q004', 'PC20260720-01', '2026-08-05', 'quality', '客户反馈铝锭表面有气孔缺陷，影响后续加工', '2', 100.0000, 5000.00, 2000.00, '已启动退货流程，进行根因分析和CAPA', 'return', '1', NULL, '0', '0', 'admin', NOW(), '测试数据-质量投诉处理中(退货)');

-- -----------------------------------------------
-- 12. 质量目标表 qms_quality_target
-- -----------------------------------------------
INSERT INTO qms_quality_target (target_year, target_month, dimension, dimension_val, target_pass_rate, actual_pass_rate, target_ppm, actual_ppm, target_complaint_rate, actual_complaint_rate, target_capa_close_rate, actual_capa_close_rate, achievement, del_flag, status, create_by, create_time, remark) VALUES
(2026, NULL, 'factory', '全厂', 98.00, 97.50, 2000, 2500, 0.50, 0.60, 90.00, 85.00, 'N', '0', '0', 'admin', NOW(), '测试数据-2026年度工厂级目标(未达成)'),
(2026, 7, 'factory', '全厂', 98.00, 98.20, 2000, 1800, 0.50, 0.40, 90.00, 92.00, 'Y', '0', '0', 'admin', NOW(), '测试数据-2026年7月工厂级目标(达成)'),
(2026, 7, 'line', '锂电池生产线A', 99.00, 98.50, 1000, 1500, 0.30, 0.50, 95.00, 90.00, 'N', '0', '0', 'admin', NOW(), '测试数据-2026年7月产线级目标(未达成)'),
(2026, 7, 'material', '氢氧化锂', 98.50, 99.00, 1500, 1000, 0.50, 0.00, 90.00, 100.00, 'Y', '0', '0', 'admin', NOW(), '测试数据-2026年7月物料级目标(达成)'),
(2026, 8, 'factory', '全厂', 98.00, 98.80, 2000, 1200, 0.50, 0.30, 90.00, 95.00, 'Y', '0', '0', 'admin', NOW(), '测试数据-2026年8月工厂级目标(达成)');

-- -----------------------------------------------
-- 13. 质量文档表 qms_doc
-- -----------------------------------------------
INSERT INTO qms_doc (doc_no, doc_title, doc_category, version_no, doc_status, dept_name, tpl_id, file_url, publish_date, obsolete_date, del_flag, status, create_by, create_time, remark) VALUES
('TEST-DOC-001', '质量管理手册(2026版)', 'manual', 'V4.0', '2', '品质部', @tplId1, '/upload/qms/质量手册V4.0.pdf', '2026-01-15', NULL, '0', '0', 'admin', NOW(), '测试数据-质量管理手册已生效'),
('TEST-DOC-002', '来料检验控制程序', 'procedure', 'V2.1', '2', '品质部', @tplId1, '/upload/qms/IQC程序V2.1.pdf', '2026-02-01', NULL, '0', '0', 'admin', NOW(), '测试数据-IQC检验程序文件'),
('TEST-DOC-003', '氢氧化锂检验作业指导书', 'sop', 'V1.5', '2', '品质部', @tplId1, '/upload/qms/SOP-LiOH-V1.5.pdf', '2026-03-10', NULL, '0', '0', 'admin', NOW(), '测试数据-氢氧化锂检验SOP'),
('TEST-DOC-004', '不合格品处理记录表', 'form', 'V1.0', '2', '品质部', NULL, '/upload/qms/NCR记录表V1.0.xlsx', '2026-01-01', NULL, '0', '0', 'admin', NOW(), '测试数据-NCR记录表单'),
('TEST-DOC-005', '过程检验控制程序(修订版)', 'procedure', 'V3.0', '1', '品质部', @tplId2, '/upload/qms/IPQC程序V3.0草案.pdf', NULL, NULL, '0', '0', 'admin', NOW(), '测试数据-IPQC程序文件审批中');

-- -----------------------------------------------
-- 14. 内审计划表 qms_audit_plan
-- -----------------------------------------------
INSERT INTO qms_audit_plan (audit_plan_no, audit_title, audit_year, audit_type, audit_scope, lead_auditor, audit_date_from, audit_date_to, audit_conclusion, plan_status, del_flag, status, create_by, create_time, remark) VALUES
('TEST-AP-001', '2026年上半年质量管理体系内审', 2026, 'annual', '全公司各部门质量管理体系运行情况', '主审张', '2026-06-15', '2026-06-17', '质量体系运行基本有效，发现3个一般不符合项，已制定整改计划', '2', '0', '0', 'admin', NOW(), '测试数据-上半年内审已完成'),
('TEST-AP-002', '2026年QMS文件专项审核', 2026, 'adhoc', '文件控制、记录管理、标识追溯', '主审李', '2026-07-10', '2026-07-10', '文件控制规范，部分记录表单需要更新版本', '2', '0', '0', 'admin', NOW(), '测试数据-文件专项审核完成'),
('TEST-AP-003', '2026年供应商质量管理专项审核', 2026, 'adhoc', '供应商评价、来料检验、不合格品处理', '主审王', '2026-08-15', '2026-08-16', NULL, '1', '0', '0', 'admin', NOW(), '测试数据-供应商专项审核执行中'),
('TEST-AP-004', '2026年下半年质量管理体系内审', 2026, 'annual', '全公司各部门质量管理体系运行情况', '主审张', '2026-12-10', '2026-12-12', NULL, '0', '0', '0', 'admin', NOW(), '测试数据-下半年内审计划中'),
('TEST-AP-005', '生产过程控制专项审核', 2026, 'adhoc', '生产工艺控制、过程检验、设备校准', '主审李', '2026-09-01', '2026-09-02', NULL, '0', '0', '0', 'admin', NOW(), '测试数据-生产过程审核计划中');

-- -----------------------------------------------
-- 15. 管理评审表 qms_mr
-- -----------------------------------------------
INSERT INTO qms_mr (mr_no, mr_title, mr_year, mr_date, chairperson, participants, input_summary, resolution, action_items, mr_status, del_flag, status, create_by, create_time, remark) VALUES
('TEST-MR-001', '2026年上半年管理评审', 2026, '2026-07-05', '总经理张建国', '总经理,质量经理,生产经理,采购经理,销售经理', '质量目标达成率97.5%(未达标)；客诉3件(已关闭2件)；CAPA完成率85%；内审发现3个一般不符合项', '1.持续改进质量目标管理 2.加强供应商质量管控 3.提升CAPA关闭率', '1.Q3质量目标提升至98.5% 2.建立供应商季度评价机制 3.CAPA责任人考核', '1', '0', '0', 'admin', NOW(), '测试数据-上半年管理评审已完成'),
('TEST-MR-002', '2026年质量体系适宜性评审', 2026, '2026-04-10', '总经理张建国', '管理团队全体成员', 'QMS文件评审结果；组织变更影响评估；资源配置评估', '1.更新质量手册至V4.0 2.增加新能源行业模板 3.配置电子签名系统', '1.6月完成手册换版 2.7月上线行业模板 3.8月完成电子签名部署', '3', '0', '0', 'admin', NOW(), '测试数据-体系适宜性评审已关闭'),
('TEST-MR-003', '2026年Q3质量专项评审', 2026, '2026-08-20', '总经理张建国', '各部门负责人', '7月质量数据回顾；客户满意度调查；供应商质量排名', NULL, NULL, '0', '0', '0', 'admin', NOW(), '测试数据-Q3专项评审准备中'),
('TEST-MR-004', '2025年度管理评审', 2025, '2026-01-20', '总经理张建国', '管理团队全体成员', '2025年度质量目标达成情况；客诉处理情况；CAPA统计；内审结果；供应商评价', '1.2026年质量目标提升 2.导入QMS信息化系统 3.强化过程控制', '1.制定2026年质量目标 2.启动QMS系统选型 3.增加过程检验频次', '3', '0', '0', 'admin', NOW(), '测试数据-2025年度管理评审已关闭'),
('TEST-MR-005', '2026年客诉专项评审', 2026, '2026-08-15', '质量经理', '质量部,销售部,生产部', '7月客诉2件；客户满意度下降5%；退货金额13000元', NULL, NULL, '2', '0', '0', 'admin', NOW(), '测试数据-客诉专项评审跟踪中');

-- -----------------------------------------------
-- 16. 车间/产线/工位表 qms_workshop (补充5条)
-- -----------------------------------------------
INSERT INTO qms_workshop (parent_id, workshop_code, workshop_name, node_type, order_num, status, del_flag, create_by, create_time, remark) VALUES
(0, 'TEST-WS-001', '测试-电解液车间', '1', 10, '0', '0', 'admin', NOW(), '测试数据-电解液制备车间'),
(0, 'TEST-WS-002', '测试-铝加工车间', '1', 11, '0', '0', 'admin', NOW(), '测试数据-铝锭铸造车间'),
(0, 'TEST-WS-003', '测试-精细化工车间', '1', 12, '0', '0', 'admin', NOW(), '测试数据-化工反应车间'),
(0, 'TEST-WS-004', '测试-包装车间', '1', 13, '0', '0', 'admin', NOW(), '测试数据-成品包装车间'),
(0, 'TEST-WS-005', '测试-检验中心', '1', 14, '0', '0', 'admin', NOW(), '测试数据-质量检验中心');

-- -----------------------------------------------
-- 17. 电子签名记录表 qms_esig_record
-- -----------------------------------------------
SET @ncrId1 = (SELECT ncr_id FROM qms_ncr WHERE ncr_no = 'TEST-NCR-001' LIMIT 1);
SET @ncrId3 = (SELECT ncr_id FROM qms_ncr WHERE ncr_no = 'TEST-NCR-003' LIMIT 1);

INSERT INTO qms_esig_record (biz_type, biz_id, biz_no, user_id, user_name, nick_name, sig_meaning, sig_opinion, sig_result, sig_ip, sig_time, create_by, create_time, remark) VALUES
('insp_judge', IFNULL(@taskId1, 0), 'TEST-IT-001', 1, 'admin', '质检员王', '检验员判定', '抽样50件全部合格，判定合格放行', 'pass', '192.168.1.100', '2026-08-01 16:00:00', 'admin', NOW(), '测试数据-IQC检验合格签名'),
('insp_judge', IFNULL(@taskId2, 0), 'TEST-IT-002', 1, 'admin', '质检员李', '检验员判定', '水分含量25ppm超标，判定不合格', 'fail', '192.168.1.101', '2026-08-02 12:00:00', 'admin', NOW(), '测试数据-IQC检验不合格签名'),
('ncr_approve', IFNULL(@ncrId1, 0), 'TEST-NCR-001', 1, 'admin', '质量经理', '质量经理审批', '同意退货处理，通知供应商更换', 'pass', '192.168.1.102', '2026-08-03 10:00:00', 'admin', NOW(), '测试数据-NCR审批通过签名'),
('ncr_disposition', IFNULL(@ncrId3, 0), 'TEST-NCR-003', 1, 'admin', '品质经理', '处置执行确认', '已执行挑选使用，隔离不合格品', 'pass', '192.168.1.103', '2026-08-04 14:00:00', 'admin', NOW(), '测试数据-NCR处置确认签名'),
('ncr_verify', IFNULL(@ncrId1, 0), 'TEST-NCR-001', 1, 'admin', '质量经理', 'NCR验证', '供应商已退货更换，连续3批次合格，验证通过', 'pass', '192.168.1.104', '2026-08-05 15:00:00', 'admin', NOW(), '测试数据-NCR验证通过签名');

-- -----------------------------------------------
-- 18. 批次谱系表 qms_lot_genealogy
-- -----------------------------------------------
INSERT INTO qms_lot_genealogy (parent_batch_no, child_batch_no, work_order_no, op_id, op_name, material_id, material_code, material_name, qty_in, qty_out, line_id, line_name, operator, supplier_id, supplier_name, source_type, trace_time, break_flag, break_reason, del_flag, status, create_by, create_time, remark) VALUES
('RM20260801-001', 'WIP20260801-001', 'WO20260801001', 'OP01', '投料工序', 9001, 'TEST-MAT-Q001', '氢氧化锂(电池级)', 500.0000, 495.0000, 'LINE001', '总装线A', '操作员张三', 1001, '测试供应商A', 'feeding', '2026-08-01 08:30:00', 0, NULL, '0', '0', 'admin', NOW(), '测试数据-原料投料谱系'),
('WIP20260801-001', 'WIP20260801-002', 'WO20260801001', 'OP02', '混合反应', 9001, 'TEST-MAT-Q001', '氢氧化锂(电池级)', 495.0000, 490.0000, 'LINE001', '总装线A', '操作员李四', NULL, NULL, 'report', '2026-08-01 12:00:00', 0, NULL, '0', '0', 'admin', NOW(), '测试数据-混合报工谱系'),
('WIP20260801-002', 'FG20260801-001', 'WO20260801001', 'OP03', '成品包装', 9001, 'TEST-MAT-Q001', '氢氧化锂(电池级)', 490.0000, 488.0000, 'LINE001', '总装线A', '操作员王五', NULL, NULL, 'complete', '2026-08-01 16:00:00', 0, NULL, '0', '0', 'admin', NOW(), '测试数据-完工入库谱系'),
('RM20260802-001', 'WIP20260802-001', 'WO20260802001', 'OP01', '投料工序', 9002, 'TEST-MAT-Q002', '六氟磷酸锂', 200.0000, NULL, 'LINE002', '总装线B', '操作员赵六', 1002, '测试供应商B', 'feeding', '2026-08-02 09:00:00', 1, '来料检验不合格，暂停投料等待退货更换', '0', '0', 'admin', NOW(), '测试数据-断点谱系(来料不合格)'),
('FG20260715-001', 'FG20260720-001', 'WO20260715001', 'OP04', '返工处理', 9001, 'TEST-MAT-Q001', '氢氧化锂(电池级)', 20.0000, 18.0000, 'LINE001', '总装线A', '操作员张三', NULL, NULL, 'rework', '2026-07-20 14:00:00', 0, NULL, '0', '0', 'admin', NOW(), '测试数据-客退返工谱系');

-- =============================================
-- 三、执行结果确认
-- =============================================
SELECT '安全管理-企业档案' AS module, COUNT(*) AS cnt FROM biz_safety_enterprise WHERE credit_code LIKE 'TEST%'
UNION ALL SELECT '安全管理-区域管理', COUNT(*) FROM biz_safety_area WHERE area_code LIKE 'TEST-AREA%'
UNION ALL SELECT '安全管理-证照管理', COUNT(*) FROM biz_safety_cert WHERE cert_no LIKE 'TEST-CERT%'
UNION ALL SELECT '安全管理-特种人员', COUNT(*) FROM biz_safety_worker WHERE worker_no LIKE 'TEST-WK%'
UNION ALL SELECT '安全管理-特种设备', COUNT(*) FROM biz_safety_equipment WHERE equipment_code LIKE 'TEST-EQP%'
UNION ALL SELECT '安全管理-危化品管理', COUNT(*) FROM biz_safety_material WHERE material_code LIKE 'TEST-MAT-00%'
UNION ALL SELECT '安全管理-风险点管理', COUNT(*) FROM biz_safety_risk_point WHERE risk_code LIKE 'TEST-RISK%'
UNION ALL SELECT '安全管理-隐患管理', COUNT(*) FROM biz_safety_hazard WHERE hazard_code LIKE 'TEST-HZD%'
UNION ALL SELECT '安全管理-隐患整改', COUNT(*) FROM biz_safety_rectify WHERE create_by = 'admin' AND remark LIKE '测试数据%'
UNION ALL SELECT '安全管理-排查任务', COUNT(*) FROM biz_safety_inspection_task WHERE task_code LIKE 'TEST-TASK%'
UNION ALL SELECT '安全管理-培训计划', COUNT(*) FROM biz_safety_training_plan WHERE plan_code LIKE 'TEST-TP%'
UNION ALL SELECT '安全管理-培训记录', COUNT(*) FROM biz_safety_training_record WHERE create_by = 'admin' AND remark LIKE '测试数据%'
UNION ALL SELECT '安全管理-应急预案', COUNT(*) FROM biz_safety_emergency_plan WHERE plan_code LIKE 'TEST-EP%'
UNION ALL SELECT '安全管理-应急物资', COUNT(*) FROM biz_safety_emergency_material WHERE material_code LIKE 'TEST-EM%'
UNION ALL SELECT '安全管理-演练记录', COUNT(*) FROM biz_safety_drill WHERE drill_code LIKE 'TEST-DR%'
UNION ALL SELECT '安全管理-事故管理', COUNT(*) FROM biz_safety_incident WHERE incident_code LIKE 'TEST-INC%'
UNION ALL SELECT '安全管理-到期提醒', COUNT(*) FROM biz_safety_remind WHERE create_by = 'admin' AND remark LIKE '测试数据%'
UNION ALL SELECT '安全管理-检查标准库', COUNT(*) FROM biz_safety_std_library WHERE create_by = 'admin' AND remark LIKE '测试数据%'
UNION ALL SELECT '质量管理-行业模板', COUNT(*) FROM qms_industry_tpl WHERE tpl_code LIKE 'TEST-TPL%'
UNION ALL SELECT '质量管理-检验标准库', COUNT(*) FROM qms_inspect_std WHERE std_code LIKE 'TEST-STD%'
UNION ALL SELECT '质量管理-缺陷代码', COUNT(*) FROM qms_defect_code WHERE defect_code LIKE 'TEST-DF%'
UNION ALL SELECT '质量管理-量检具台账', COUNT(*) FROM qms_gauge WHERE gauge_no LIKE 'TEST-GG%'
UNION ALL SELECT '质量管理-物料质量属性', COUNT(*) FROM qms_material_attr WHERE material_code LIKE 'TEST-MAT-Q%'
UNION ALL SELECT '质量管理-检验任务', COUNT(*) FROM qms_insp_task WHERE task_no LIKE 'TEST-IT%'
UNION ALL SELECT '质量管理-NCR台账', COUNT(*) FROM qms_ncr WHERE ncr_no LIKE 'TEST-NCR%'
UNION ALL SELECT '质量管理-CAPA台账', COUNT(*) FROM qms_capa WHERE capa_no LIKE 'TEST-CAPA%'
UNION ALL SELECT '质量管理-供应商评价', COUNT(*) FROM qms_supplier_eval WHERE supplier_name LIKE '测试供应商%'
UNION ALL SELECT '质量管理-供应商审核', COUNT(*) FROM qms_supplier_audit WHERE audit_no LIKE 'TEST-SA%'
UNION ALL SELECT '质量管理-客诉台账', COUNT(*) FROM qms_complaint WHERE complaint_no LIKE 'TEST-CS%'
UNION ALL SELECT '质量管理-质量目标', COUNT(*) FROM qms_quality_target WHERE create_by = 'admin' AND remark LIKE '测试数据%'
UNION ALL SELECT '质量管理-质量文档', COUNT(*) FROM qms_doc WHERE doc_no LIKE 'TEST-DOC%'
UNION ALL SELECT '质量管理-内审计划', COUNT(*) FROM qms_audit_plan WHERE audit_plan_no LIKE 'TEST-AP%'
UNION ALL SELECT '质量管理-管理评审', COUNT(*) FROM qms_mr WHERE mr_no LIKE 'TEST-MR%'
UNION ALL SELECT '质量管理-车间管理', COUNT(*) FROM qms_workshop WHERE workshop_code LIKE 'TEST-WS%'
UNION ALL SELECT '质量管理-电子签名', COUNT(*) FROM qms_esig_record WHERE remark LIKE '测试数据%'
UNION ALL SELECT '质量管理-批次谱系', COUNT(*) FROM qms_lot_genealogy WHERE remark LIKE '测试数据%';

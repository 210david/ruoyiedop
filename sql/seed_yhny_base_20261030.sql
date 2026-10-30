-- =============================================================================
-- YHNY 源禾农业 基础数据补齐（工艺路线/工序/工位/班次/班组/资源/需求/MPS/派工/
--   质量车间/检验项/物料属性/CAPA/客诉/器具/MR，采购供应商目录/评价/询价/合同/
--   收货/退货/发票，营销活动/内容/互动/素材），2026-10-30
-- =============================================================================

-- ---------------- 生产：工序（5） ----------------
INSERT INTO mms_process (process_code,process_name,create_by,create_time) VALUES
('YH-GX-001','原蜜过滤','admin',NOW()),
('YH-GX-002','真空浓缩','admin',NOW()),
('YH-GX-003','灌装','admin',NOW()),
('YH-GX-004','封盖','admin',NOW()),
('YH-GX-005','贴标包装','admin',NOW());

-- ---------------- 生产：工艺路线（5） ----------------
INSERT INTO mms_route (route_no,route_name,product_id,create_by,create_time)
SELECT 'YH-GY-001','380g土蜂蜜灌装工艺路线 V1.0',p.product_id,'admin',NOW() FROM pdm_product p WHERE p.product_code='YH-CP-001';
INSERT INTO mms_route (route_no,route_name,product_id,create_by,create_time)
SELECT 'YH-GY-002','蜂蜜膏加工工艺路线 V1.0',p.product_id,'admin',NOW() FROM pdm_product p WHERE p.product_code='YH-CP-002';
INSERT INTO mms_route (route_no,route_name,product_id,create_by,create_time)
SELECT 'YH-GY-003','蜂蜜礼盒组装工艺路线 V1.0',p.product_id,'admin',NOW() FROM pdm_product p WHERE p.product_code='YH-CP-003';
INSERT INTO mms_route (route_no,route_name,product_id,create_by,create_time)
SELECT 'YH-GY-004','鲜花酥外采加工路线 V1.0',p.product_id,'admin',NOW() FROM pdm_product p WHERE p.product_code='YH-CP-004';
INSERT INTO mms_route (route_no,route_name,product_id,create_by,create_time)
SELECT 'YH-GY-005','蜜上他饮品灌装工艺路线（规划）',p.product_id,'admin',NOW() FROM pdm_product p WHERE p.product_code='YH-CP-005';

-- ---------------- 生产：路线工序（主路线5步 + 路线2/3各3步） ----------------
INSERT INTO mms_route_process (route_id,process_id,process_code,process_name,workshop,line,step_seq,std_time,is_key_process,create_by,create_time)
SELECT r.route_id,p.process_id,p.process_code,p.process_name,'过滤加工车间','YH-L03',1,60,'1','admin',NOW()
FROM mms_route r, mms_process p WHERE r.route_no='YH-GY-001' AND p.process_code='YH-GX-001';
INSERT INTO mms_route_process (route_id,process_id,process_code,process_name,workshop,line,step_seq,std_time,is_key_process,create_by,create_time)
SELECT r.route_id,p.process_id,p.process_code,p.process_name,'过滤加工车间','YH-L03',2,45,'0','admin',NOW()
FROM mms_route r, mms_process p WHERE r.route_no='YH-GY-001' AND p.process_code='YH-GX-002';
INSERT INTO mms_route_process (route_id,process_id,process_code,process_name,workshop,line,step_seq,std_time,is_key_process,create_by,create_time)
SELECT r.route_id,p.process_id,p.process_code,p.process_name,'灌装包装车间','YH-L01',3,90,'1','admin',NOW()
FROM mms_route r, mms_process p WHERE r.route_no='YH-GY-001' AND p.process_code='YH-GX-003';
INSERT INTO mms_route_process (route_id,process_id,process_code,process_name,workshop,line,step_seq,std_time,is_key_process,create_by,create_time)
SELECT r.route_id,p.process_id,p.process_code,p.process_name,'灌装包装车间','YH-L01',4,30,'0','admin',NOW()
FROM mms_route r, mms_process p WHERE r.route_no='YH-GY-001' AND p.process_code='YH-GX-004';
INSERT INTO mms_route_process (route_id,process_id,process_code,process_name,workshop,line,step_seq,std_time,is_key_process,create_by,create_time)
SELECT r.route_id,p.process_id,p.process_code,p.process_name,'灌装包装车间','YH-L02',5,40,'0','admin',NOW()
FROM mms_route r, mms_process p WHERE r.route_no='YH-GY-001' AND p.process_code='YH-GX-005';
INSERT INTO mms_route_process (route_id,process_id,process_code,process_name,workshop,line,step_seq,std_time,is_key_process,create_by,create_time)
SELECT r.route_id,p.process_id,p.process_code,p.process_name,'过滤加工车间','YH-L03',1,60,'0','admin',NOW()
FROM mms_route r, mms_process p WHERE r.route_no='YH-GY-002' AND p.process_code='YH-GX-001';
INSERT INTO mms_route_process (route_id,process_id,process_code,process_name,workshop,line,step_seq,std_time,is_key_process,create_by,create_time)
SELECT r.route_id,p.process_id,p.process_code,p.process_name,'过滤加工车间','YH-L03',2,90,'1','admin',NOW()
FROM mms_route r, mms_process p WHERE r.route_no='YH-GY-002' AND p.process_code='YH-GX-002';
INSERT INTO mms_route_process (route_id,process_id,process_code,process_name,workshop,line,step_seq,std_time,is_key_process,create_by,create_time)
SELECT r.route_id,p.process_id,p.process_code,p.process_name,'灌装包装车间','YH-L01',3,80,'0','admin',NOW()
FROM mms_route r, mms_process p WHERE r.route_no='YH-GY-002' AND p.process_code='YH-GX-003';
INSERT INTO mms_route_process (route_id,process_id,process_code,process_name,workshop,line,step_seq,std_time,is_key_process,create_by,create_time)
SELECT r.route_id,p.process_id,p.process_code,p.process_name,'灌装包装车间','YH-L02',1,20,'0','admin',NOW()
FROM mms_route r, mms_process p WHERE r.route_no='YH-GY-003' AND p.process_code='YH-GX-005';
INSERT INTO mms_route_process (route_id,process_id,process_code,process_name,workshop,line,step_seq,std_time,is_key_process,create_by,create_time)
SELECT r.route_id,p.process_id,p.process_code,p.process_name,'灌装包装车间','YH-L02',2,20,'0','admin',NOW()
FROM mms_route r, mms_process p WHERE r.route_no='YH-GY-003' AND p.process_code='YH-GX-004';

-- ---------------- 生产：工位（5） ----------------
INSERT INTO mms_station (station_code,station_name,line_id,create_by,create_time) VALUES
('YH-GW-001','灌装工位1',(SELECT line_id FROM mms_line WHERE line_code='YH-L01'),'admin',NOW()),
('YH-GW-002','灌装工位2',(SELECT line_id FROM mms_line WHERE line_code='YH-L01'),'admin',NOW()),
('YH-GW-003','封盖工位',(SELECT line_id FROM mms_line WHERE line_code='YH-L01'),'admin',NOW()),
('YH-GW-004','贴标工位',(SELECT line_id FROM mms_line WHERE line_code='YH-L02'),'admin',NOW()),
('YH-GW-005','包装工位',(SELECT line_id FROM mms_line WHERE line_code='YH-L02'),'admin',NOW());

-- ---------------- 生产：班次（5） ----------------
INSERT INTO mms_shift (shift_name,start_time,end_time,create_by,create_time) VALUES
('早班','08:00','16:00','admin',NOW()),
('中班','16:00','24:00','admin',NOW()),
('晚班','00:00','08:00','admin',NOW()),
('采蜜班','06:00','14:00','admin',NOW()),
('机动班','09:00','18:00','admin',NOW());

-- ---------------- 生产：班组（5） ----------------
INSERT INTO mms_team (team_code,team_name,create_by,create_time) VALUES
('YH-BZ-001','灌装一组','admin',NOW()),
('YH-BZ-002','灌装二组','admin',NOW()),
('YH-BZ-003','包装组','admin',NOW()),
('YH-BZ-004','蜂场一组','admin',NOW()),
('YH-BZ-005','蜂场二组','admin',NOW());

-- ---------------- 生产：资源（5） ----------------
INSERT INTO mms_resource (resource_code,resource_name,create_by,create_time) VALUES
('YH-ZY-001','蜂蜜灌装线','admin',NOW()),
('YH-ZY-002','贴标包装线','admin',NOW()),
('YH-ZY-003','化验室','admin',NOW()),
('YH-ZY-004','货运车','admin',NOW()),
('YH-ZY-005','蜂场器具组','admin',NOW());

-- ---------------- 生产：需求（5） ----------------
INSERT INTO mms_demand (demand_no,product_id,demand_qty,require_date,create_by,create_time)
SELECT CONCAT('YH-XQ-2026-00',n),p.product_id,500*(6-n),DATE_ADD('2026-11-01', INTERVAL n-1 DAY),'admin',NOW()
FROM pdm_product p, (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) nums
WHERE p.product_code='YH-CP-001';

-- ---------------- 生产：MPS 主生产计划（5） ----------------
INSERT INTO mms_mps (mps_no,product_id,plan_qty,period_start,period_end,create_by,create_time)
SELECT 'YH-MPS-2026-001',p.product_id,800,'2026-11-01','2026-11-10','admin',NOW() FROM pdm_product p WHERE p.product_code='YH-CP-001';
INSERT INTO mms_mps (mps_no,product_id,plan_qty,period_start,period_end,create_by,create_time)
SELECT 'YH-MPS-2026-002',p.product_id,600,'2026-11-11','2026-11-20','admin',NOW() FROM pdm_product p WHERE p.product_code='YH-CP-001';
INSERT INTO mms_mps (mps_no,product_id,plan_qty,period_start,period_end,create_by,create_time)
SELECT 'YH-MPS-2026-003',p.product_id,400,'2026-11-21','2026-11-30','admin',NOW() FROM pdm_product p WHERE p.product_code='YH-CP-001';
INSERT INTO mms_mps (mps_no,product_id,plan_qty,period_start,period_end,create_by,create_time)
SELECT 'YH-MPS-2026-004',p.product_id,200,'2026-11-05','2026-11-15','admin',NOW() FROM pdm_product p WHERE p.product_code='YH-CP-002';
INSERT INTO mms_mps (mps_no,product_id,plan_qty,period_start,period_end,create_by,create_time)
SELECT 'YH-MPS-2026-005',p.product_id,150,'2026-11-15','2026-11-25','admin',NOW() FROM pdm_product p WHERE p.product_code='YH-CP-003';

-- ---------------- 生产：派工单（5，对应工单1的5道工序） ----------------
INSERT INTO mms_dispatch (dispatch_no,work_order_id,op_seq,create_by,create_time)
SELECT 'YH-PG-2026-001',w.work_order_id,1,'admin',NOW() FROM mms_work_order w WHERE w.work_order_no='YH-WO-2026-0001';
INSERT INTO mms_dispatch (dispatch_no,work_order_id,op_seq,create_by,create_time)
SELECT 'YH-PG-2026-002',w.work_order_id,2,'admin',NOW() FROM mms_work_order w WHERE w.work_order_no='YH-WO-2026-0001';
INSERT INTO mms_dispatch (dispatch_no,work_order_id,op_seq,create_by,create_time)
SELECT 'YH-PG-2026-003',w.work_order_id,3,'admin',NOW() FROM mms_work_order w WHERE w.work_order_no='YH-WO-2026-0001';
INSERT INTO mms_dispatch (dispatch_no,work_order_id,op_seq,create_by,create_time)
SELECT 'YH-PG-2026-004',w.work_order_id,4,'admin',NOW() FROM mms_work_order w WHERE w.work_order_no='YH-WO-2026-0001';
INSERT INTO mms_dispatch (dispatch_no,work_order_id,op_seq,create_by,create_time)
SELECT 'YH-PG-2026-005',w.work_order_id,5,'admin',NOW() FROM mms_work_order w WHERE w.work_order_no='YH-WO-2026-0001';

-- ---------------- 质量：车间（5） ----------------
INSERT INTO qms_workshop (workshop_code,workshop_name,node_type,create_by,create_time) VALUES
('YH-CJ-001','灌装包装车间','1','admin',NOW()),
('YH-CJ-002','过滤加工车间','1','admin',NOW()),
('YH-CJ-003','化验室','1','admin',NOW()),
('YH-CJ-004','原料仓库','1','admin',NOW()),
('YH-CJ-005','成品仓库','1','admin',NOW());

-- ---------------- 质量：检验项（5，对应检验任务1的标准项实测） ----------------
INSERT INTO qms_insp_item (task_id,std_id,std_name,sample_seq,measured_val,item_result,create_by,create_time)
SELECT t.task_id,s.std_id,s.std_name,1,42.3,'1','admin',NOW()
FROM qms_insp_task t, qms_inspect_std s WHERE t.task_no='YH-QT-2026-0001' AND s.std_code='YH-QS-001';
INSERT INTO qms_insp_item (task_id,std_id,std_name,sample_seq,measured_val,item_result,create_by,create_time)
SELECT t.task_id,s.std_id,s.std_name,2,65.0,'1','admin',NOW()
FROM qms_insp_task t, qms_inspect_std s WHERE t.task_no='YH-QT-2026-0001' AND s.std_code='YH-QS-002';
INSERT INTO qms_insp_item (task_id,std_id,std_name,sample_seq,measured_val,item_result,create_by,create_time)
SELECT t.task_id,s.std_id,s.std_name,3,50,'1','admin',NOW()
FROM qms_insp_task t, qms_inspect_std s WHERE t.task_no='YH-QT-2026-0001' AND s.std_code='YH-QS-003';
INSERT INTO qms_insp_item (task_id,std_id,std_name,sample_seq,measured_val,item_result,create_by,create_time)
SELECT t.task_id,s.std_id,s.std_name,4,80,'1','admin',NOW()
FROM qms_insp_task t, qms_inspect_std s WHERE t.task_no='YH-QT-2026-0001' AND s.std_code='YH-QS-004';
INSERT INTO qms_insp_item (task_id,std_id,std_name,sample_seq,measured_val,item_result,create_by,create_time)
SELECT t.task_id,s.std_id,s.std_name,5,19.0,'1','admin',NOW()
FROM qms_insp_task t, qms_inspect_std s WHERE t.task_no='YH-QT-2026-0001' AND s.std_code='YH-QS-005';

-- ---------------- 质量：物料检验属性（5） ----------------
INSERT INTO qms_material_attr (material_id,material_code,material_name,inspect_types,industry_type,is_exempt,status,del_flag,create_by,create_time)
SELECT m.material_id,m.material_code,m.material_name,'IQC,FQC','food','0','0','0','admin',NOW()
FROM wms_material m WHERE m.material_code IN ('YH-MAT-001','YH-MAT-002','YH-MAT-003','YH-MAT-020','YH-MAT-023');

-- ---------------- 质量：CAPA（5） ----------------
INSERT INTO qms_capa (capa_no,title,create_by,create_time) VALUES
('YH-CA-2026-001','针对2020年嗜渗酵母超标的纠正预防措施（灌装环节卫生强化）','admin',NOW()),
('YH-CA-2026-002','玻璃瓶到货破损供应商整改跟进','admin',NOW()),
('YH-CA-2026-003','标签印刷色差纠正（供应商对色标准）','admin',NOW()),
('YH-CA-2026-004','灌装液位偏差设备校准措施','admin',NOW()),
('YH-CA-2026-005','外采品效期验收入库规则优化','admin',NOW());

-- ---------------- 质量：客诉（5） ----------------
INSERT INTO qms_complaint (complaint_no,customer_name,material_name,material_code,batch_no,complaint_date,complaint_type,defect_desc,severity,complaint_status,create_by,create_time) VALUES
('YH-TS-2026-0001','曲靖本地商超连锁','滇品珍土蜂蜜380g','YH-MAT-010','YH-2026-1020','2026-10-12','1','客户反馈瓶盖密封不严漏蜜','2','1','admin',NOW()),
('YH-TS-2026-0002','企业团购客户（礼盒定制）','土蜂蜜礼盒1.5kg','YH-MAT-015','YH-2026-1018','2026-10-15','2','礼盒外箱压痕','3','1','admin',NOW()),
('YH-TS-2026-0003','京东平台自营渠道','滇品珍洋槐蜜380g','YH-MAT-012','YH-2026-1019','2026-10-18','1','物流破损1瓶','2','1','admin',NOW()),
('YH-TS-2026-0004','特产礼品店分销客户','蜂蜜膏500g','YH-MAT-014','YH-2026-1016','2026-10-20','1','标签起泡','3','1','admin',NOW()),
('YH-TS-2026-0005','昆明滇品珍经营部','鲜花酥256g','YH-MAT-030','YH-2026-1025','2026-10-24','3','口感偏甜（口味反馈）','3','1','admin',NOW());

-- ---------------- 质量：计量器具（5） ----------------
INSERT INTO qms_gauge (gauge_no,gauge_name,create_by,create_time) VALUES
('YH-QJ-001','电子天平（FA2004）','admin',NOW()),
('YH-QJ-002','波美计','admin',NOW()),
('YH-QJ-003','手持折光仪','admin',NOW()),
('YH-QJ-004','恒温干燥箱','admin',NOW()),
('YH-QJ-005','生物显微镜','admin',NOW());

-- ---------------- 质量：管理评审（5） ----------------
INSERT INTO qms_mr (mr_no,mr_title,mr_year,mr_date,chairperson,participants,mr_status,dept_id,create_by,create_time) VALUES
('YH-MR-2026-001','2026年Q1质量管理评审',2026,'2026-04-10','袁玉伟','杨春梅/周红亮/周晓','2',101,'admin',NOW()),
('YH-MR-2026-002','2026年Q2质量管理评审',2026,'2026-07-10','袁玉伟','杨春梅/周红亮/周晓','2',101,'admin',NOW()),
('YH-MR-2026-003','2026年Q3质量管理评审',2026,'2026-10-10','袁玉伟','杨春梅/周红亮/周晓','1',101,'admin',NOW()),
('YH-MR-2026-004','采蜜季食品安全专项评审',2026,'2026-09-20','袁玉伟','袁飞/周红亮','2',200,'admin',NOW()),
('YH-MR-2026-005','SC换证前内审评审',2026,'2026-12-20','袁玉伟','杨春梅/周红亮','1',101,'admin',NOW());

-- ---------------- 采购：供应商供货目录（5） ----------------
INSERT INTO pms_supplier_catalog (supplier_id,material_name,unit_price,create_by,create_time)
SELECT s.supplier_id,'土蜂蜜原蜜（波美度≥41.5）',80,'admin',NOW() FROM wms_supplier s WHERE s.supplier_code='YH-GYS-001';
INSERT INTO pms_supplier_catalog (supplier_id,material_name,unit_price,create_by,create_time)
SELECT s.supplier_id,'380ml蜂蜜玻璃瓶',0.6,'admin',NOW() FROM wms_supplier s WHERE s.supplier_code='YH-GYS-002';
INSERT INTO pms_supplier_catalog (supplier_id,material_name,unit_price,create_by,create_time)
SELECT s.supplier_id,'不干胶标签（滇品珍380g款）',0.2,'admin',NOW() FROM wms_supplier s WHERE s.supplier_code='YH-GYS-003';
INSERT INTO pms_supplier_catalog (supplier_id,material_name,unit_price,create_by,create_time)
SELECT s.supplier_id,'第三方检测服务（理化+微生物）',2000,'admin',NOW() FROM wms_supplier s WHERE s.supplier_code='YH-GYS-004';
INSERT INTO pms_supplier_catalog (supplier_id,material_name,unit_price,create_by,create_time)
SELECT s.supplier_id,'鲜花酥256g/盒',6,'admin',NOW() FROM wms_supplier s WHERE s.supplier_code='YH-GYS-006';

-- ---------------- 采购：供应商评价（5） ----------------
INSERT INTO pms_supplier_eval (supplier_id,create_by,create_time)
SELECT s.supplier_id,'admin',NOW() FROM wms_supplier s WHERE s.supplier_code IN ('YH-GYS-001','YH-GYS-002','YH-GYS-003','YH-GYS-004','YH-GYS-006');

-- ---------------- 采购：询比价（5） ----------------
INSERT INTO pms_inquiry (inquiry_no,title,create_by,create_time) VALUES
('YH-XBJ-2026-001','十月原蜜采购询价（1200kg）','admin',NOW()),
('YH-XBJ-2026-002','380ml/500ml玻璃瓶询价','admin',NOW()),
('YH-XBJ-2026-003','中秋礼盒外箱询价','admin',NOW()),
('YH-XBJ-2026-004','鲜花酥/月饼外采询价','admin',NOW()),
('YH-XBJ-2026-005','化验耗材年度供货询价','admin',NOW());

-- ---------------- 采购：采购合同（5） ----------------
INSERT INTO pms_contract (contract_no,contract_name,supplier_id,supplier_name,status,contract_type,sign_date,effective_date,expire_date,contract_amount,create_by,create_time)
SELECT 'YH-CGHT-2026-0001','原蜜年度采购合同',s.supplier_id,s.supplier_name,'2','1','2026-03-01','2026-03-01','2027-02-28',300000,'admin',NOW()
FROM wms_supplier s WHERE s.supplier_code='YH-GYS-001';
INSERT INTO pms_contract (contract_no,contract_name,supplier_id,supplier_name,status,contract_type,sign_date,effective_date,expire_date,contract_amount,create_by,create_time)
SELECT 'YH-CGHT-2026-0002','玻璃瓶包材供货合同',s.supplier_id,s.supplier_name,'2','1','2026-04-01','2026-04-01','2027-03-31',50000,'admin',NOW()
FROM wms_supplier s WHERE s.supplier_code='YH-GYS-002';
INSERT INTO pms_contract (contract_no,contract_name,supplier_id,supplier_name,status,contract_type,sign_date,effective_date,expire_date,contract_amount,create_by,create_time)
SELECT 'YH-CGHT-2026-0003','标签印刷供货合同',s.supplier_id,s.supplier_name,'2','1','2026-04-01','2026-04-01','2027-03-31',30000,'admin',NOW()
FROM wms_supplier s WHERE s.supplier_code='YH-GYS-003';
INSERT INTO pms_contract (contract_no,contract_name,supplier_id,supplier_name,status,contract_type,sign_date,effective_date,expire_date,contract_amount,create_by,create_time)
SELECT 'YH-CGHT-2026-0004','检测服务年度合同',s.supplier_id,s.supplier_name,'2','2','2026-01-01','2026-01-01','2026-12-31',24000,'admin',NOW()
FROM wms_supplier s WHERE s.supplier_code='YH-GYS-004';
INSERT INTO pms_contract (contract_no,contract_name,supplier_id,supplier_name,status,contract_type,sign_date,effective_date,expire_date,contract_amount,create_by,create_time)
SELECT 'YH-CGHT-2026-0005','鲜花酥采购合同（中秋备货）',s.supplier_id,s.supplier_name,'2','1','2026-08-01','2026-08-01','2026-12-31',45000,'admin',NOW()
FROM wms_supplier s WHERE s.supplier_code='YH-GYS-006';

-- ---------------- 采购：收货单（5，对应采购订单1-5） ----------------
INSERT INTO pms_receive (receive_no,order_id,order_no,supplier_id,supplier_name,warehouse_id,status,receive_date,total_qty,qualified_qty,unqualified_qty,inspector_name,dept_id,create_by,create_time)
SELECT 'YH-SH-2026-0001',o.order_id,o.order_no,o.supplier_id,o.supplier_name,w.warehouse_id,'3',o.actual_delivery_date,1200,1200,0,'周红亮',103,'admin',NOW()
FROM pms_purchase_order o, wms_warehouse w WHERE o.order_no='YH-CGDD-2026-0001' AND w.warehouse_code='YH-WH01';
INSERT INTO pms_receive (receive_no,order_id,order_no,supplier_id,supplier_name,warehouse_id,status,receive_date,total_qty,qualified_qty,unqualified_qty,inspector_name,dept_id,create_by,create_time)
SELECT 'YH-SH-2026-0002',o.order_id,o.order_no,o.supplier_id,o.supplier_name,w.warehouse_id,'3','2026-10-24',16000,15970,30,'周红亮',103,'admin',NOW()
FROM pms_purchase_order o, wms_warehouse w WHERE o.order_no='YH-CGDD-2026-0002' AND w.warehouse_code='YH-WH03';
INSERT INTO pms_receive (receive_no,order_id,order_no,supplier_id,supplier_name,warehouse_id,status,receive_date,total_qty,qualified_qty,unqualified_qty,inspector_name,dept_id,create_by,create_time)
SELECT 'YH-SH-2026-0003',o.order_id,o.order_no,o.supplier_id,o.supplier_name,w.warehouse_id,'3','2026-10-27',300,300,0,'周红亮',103,'admin',NOW()
FROM pms_purchase_order o, wms_warehouse w WHERE o.order_no='YH-CGDD-2026-0003' AND w.warehouse_code='YH-WH03';
INSERT INTO pms_receive (receive_no,order_id,order_no,supplier_id,supplier_name,warehouse_id,status,receive_date,total_qty,qualified_qty,unqualified_qty,inspector_name,dept_id,create_by,create_time)
SELECT 'YH-SH-2026-0004',o.order_id,o.order_no,o.supplier_id,o.supplier_name,w.warehouse_id,'3','2026-08-24',100,100,0,'周红亮',103,'admin',NOW()
FROM pms_purchase_order o, wms_warehouse w WHERE o.order_no='YH-CGDD-2026-0004' AND w.warehouse_code='YH-WH02';
INSERT INTO pms_receive (receive_no,order_id,order_no,supplier_id,supplier_name,warehouse_id,status,receive_date,total_qty,qualified_qty,unqualified_qty,inspector_name,dept_id,create_by,create_time)
SELECT 'YH-SH-2026-0005',o.order_id,o.order_no,o.supplier_id,o.supplier_name,w.warehouse_id,'2','2026-11-04',1,0,0,'周红亮',101,'admin',NOW()
FROM pms_purchase_order o, wms_warehouse w WHERE o.order_no='YH-CGDD-2026-0005' AND w.warehouse_code='YH-WH01';

-- ---------------- 采购：退货单（5） ----------------
INSERT INTO pms_return (return_no,order_id,order_no,supplier_id,supplier_name,status,return_type,return_date,total_qty,total_amount,reason,handler_name,create_by,create_time)
SELECT 'YH-TH-2026-0001',o.order_id,o.order_no,o.supplier_id,o.supplier_name,'2','1','2026-10-26',30,18,'玻璃瓶到货破损退货','周红亮','admin',NOW()
FROM pms_purchase_order o WHERE o.order_no='YH-CGDD-2026-0002';
INSERT INTO pms_return (return_no,order_id,order_no,supplier_id,supplier_name,status,return_type,return_date,total_qty,total_amount,reason,handler_name,create_by,create_time)
SELECT 'YH-TH-2026-0002',o.order_id,o.order_no,o.supplier_id,o.supplier_name,'2','2','2026-10-28',3,90,'鲜花酥临期退货','周红亮','admin',NOW()
FROM pms_purchase_order o WHERE o.order_no='YH-CGDD-2026-0004';
INSERT INTO pms_return (return_no,order_id,order_no,supplier_id,supplier_name,status,return_type,return_date,total_qty,total_amount,reason,handler_name,create_by,create_time)
SELECT 'YH-TH-2026-0003',o.order_id,o.order_no,o.supplier_id,o.supplier_name,'1','1','2026-10-20',50,30,'标签色差待定（部分退货）','周红亮','admin',NOW()
FROM pms_purchase_order o WHERE o.order_no='YH-CGDD-2026-0003';
INSERT INTO pms_return (return_no,order_id,order_no,supplier_id,supplier_name,status,return_type,return_date,total_qty,total_amount,reason,handler_name,create_by,create_time)
SELECT 'YH-TH-2026-0004',o.order_id,o.order_no,o.supplier_id,o.supplier_name,'1','3','2026-10-30',10,800,'原蜜波美度不足换货','周红亮','admin',NOW()
FROM pms_purchase_order o WHERE o.order_no='YH-CGDD-2026-0001';
INSERT INTO pms_return (return_no,order_id,order_no,supplier_id,supplier_name,status,return_type,return_date,total_qty,total_amount,reason,handler_name,create_by,create_time)
SELECT 'YH-TH-2026-0005',o.order_id,o.order_no,o.supplier_id,o.supplier_name,'1','2','2026-11-05',1,2000,'检测服务未按期执行退订','杨春梅','admin',NOW()
FROM pms_purchase_order o WHERE o.order_no='YH-CGDD-2026-0005';

-- ---------------- 采购：发票（5） ----------------
INSERT INTO pms_invoice (invoice_no,order_id,order_no,supplier_id,supplier_name,status,invoice_type,invoice_number,invoice_date,invoice_amount,tax_rate,total_amount,create_by,create_time)
SELECT 'YH-FP-2026-0001',o.order_id,o.order_no,o.supplier_id,o.supplier_name,'2','1','YHFP202610300001','2026-10-30',96000,0.09,104640,'admin',NOW()
FROM pms_purchase_order o WHERE o.order_no='YH-CGDD-2026-0001';
INSERT INTO pms_invoice (invoice_no,order_id,order_no,supplier_id,supplier_name,status,invoice_type,invoice_number,invoice_date,invoice_amount,tax_rate,total_amount,create_by,create_time)
SELECT 'YH-FP-2026-0002',o.order_id,o.order_no,o.supplier_id,o.supplier_name,'2','1','YHFP202610300002','2026-10-30',9000,0.13,10170,'admin',NOW()
FROM pms_purchase_order o WHERE o.order_no='YH-CGDD-2026-0002';
INSERT INTO pms_invoice (invoice_no,order_id,order_no,supplier_id,supplier_name,status,invoice_type,invoice_number,invoice_date,invoice_amount,tax_rate,total_amount,create_by,create_time)
SELECT 'YH-FP-2026-0003',o.order_id,o.order_no,o.supplier_id,o.supplier_name,'2','1','YHFP202610300003','2026-10-30',6000,0.13,6780,'admin',NOW()
FROM pms_purchase_order o WHERE o.order_no='YH-CGDD-2026-0003';
INSERT INTO pms_invoice (invoice_no,order_id,order_no,supplier_id,supplier_name,status,invoice_type,invoice_number,invoice_date,invoice_amount,tax_rate,total_amount,create_by,create_time)
SELECT 'YH-FP-2026-0004',o.order_id,o.order_no,o.supplier_id,o.supplier_name,'1','1','YHFP202610300004','2026-10-28',15000,0.09,16350,'admin',NOW()
FROM pms_purchase_order o WHERE o.order_no='YH-CGDD-2026-0004';
INSERT INTO pms_invoice (invoice_no,order_id,order_no,supplier_id,supplier_name,status,invoice_type,invoice_number,invoice_date,invoice_amount,tax_rate,total_amount,create_by,create_time)
SELECT 'YH-FP-2026-0005',o.order_id,o.order_no,o.supplier_id,o.supplier_name,'1','2','YHFP202610300005','2026-11-02',2000,0.06,2120,'admin',NOW()
FROM pms_purchase_order o WHERE o.order_no='YH-CGDD-2026-0005';

-- ---------------- 营销：活动（5） ----------------
INSERT INTO mk_activity (activity_no,activity_name,activity_type,start_time,end_time,location,budget,target_count,activity_status,dept_id,create_by,create_time) VALUES
('YH-HD-2026-001','中秋蜂蜜礼盒团购推广','1','2026-08-01','2026-09-30','曲靖/昆明',8000,50,'2',104,'admin',NOW()),
('YH-HD-2026-002','京东店铺双十一促销','2','2026-11-01','2026-11-11','线上',5000,100,'1',104,'admin',NOW()),
('YH-HD-2026-003','商超试吃推广活动','3','2026-10-15','2026-10-20','麒麟区商超',3000,200,'2',104,'admin',NOW()),
('YH-HD-2026-004','养蜂场开放日体验活动','1','2026-10-01','2026-10-03','一号生态养蜂场',2000,30,'2',104,'admin',NOW()),
('YH-HD-2026-005','春节年货节备货推广','2','2027-01-01','2027-02-10','线上+线下',6000,80,'1',104,'admin',NOW());

-- ---------------- 营销：内容（5） ----------------
INSERT INTO mk_content (content_no,title,content_type,plan_publish_date,audit_status,create_by,create_time) VALUES
('YH-NR-2026-001','滇品珍土蜂蜜源头溯源图文','1','2026-10-20','1','admin',NOW()),
('YH-NR-2026-002','养蜂场采蜜过程短视频','2','2026-10-25','1','admin',NOW()),
('YH-NR-2026-003','蜂蜜膏滋补功效科普文章','1','2026-11-01','0','admin',NOW()),
('YH-NR-2026-004','中秋礼盒产品详情页','3','2026-08-10','1','admin',NOW()),
('YH-NR-2026-005','双十一促销海报','4','2026-10-28','0','admin',NOW());

-- ---------------- 营销：互动记录（5） ----------------
INSERT INTO mk_interaction (customer_id,interact_type,interact_time,content,user_id,create_by,create_time)
SELECT k.customer_id,'1','2026-10-12','电话回访中秋礼盒需求，意向60盒',1,'admin',NOW() FROM mk_customer k WHERE k.customer_no='YH-KH-0004';
INSERT INTO mk_interaction (customer_id,interact_type,interact_time,content,user_id,create_by,create_time)
SELECT k.customer_id,'2','2026-10-15','微信发送产品画册与报价单',1,'admin',NOW() FROM mk_customer k WHERE k.customer_no='YH-KH-0003';
INSERT INTO mk_interaction (customer_id,interact_type,interact_time,content,user_id,create_by,create_time)
SELECT k.customer_id,'1','2026-10-18','上门拜访商超专柜洽谈铺货',1,'admin',NOW() FROM mk_customer k WHERE k.customer_no='YH-KH-0003';
INSERT INTO mk_interaction (customer_id,interact_type,interact_time,content,user_id,create_by,create_time)
SELECT k.customer_id,'3','2026-10-20','京东店铺消息回复咨询蜂蜜产地',1,'admin',NOW() FROM mk_customer k WHERE k.customer_no='YH-KH-0001';
INSERT INTO mk_interaction (customer_id,interact_type,interact_time,content,user_id,create_by,create_time)
SELECT k.customer_id,'1','2026-10-22','昆明经营部月度对账与要货沟通',1,'admin',NOW() FROM mk_customer k WHERE k.customer_no='YH-KH-0002';

-- ---------------- 营销：素材库（5） ----------------
INSERT INTO mk_material (material_no,material_type,title,tags,material_status,create_by,create_time) VALUES
('YH-SC-001','1','滇品珍产品画册（2026版）','画册/产品','0','admin',NOW()),
('YH-SC-002','2','第三方检测报告汇编（云南三正）','检测/资质','0','admin',NOW()),
('YH-SC-003','3','养蜂场采蜜宣传视频','视频/溯源','0','admin',NOW()),
('YH-SC-004','4','京东详情页设计稿','电商/详情页','0','admin',NOW()),
('YH-SC-005','5','中秋礼盒推广海报','海报/节日','0','admin',NOW());

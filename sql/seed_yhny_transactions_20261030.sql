-- =============================================================================
-- YHNY 源禾农业 各模块业务数据种子（每模块约5条，2026-10-30）
-- 前提：业务表已清理并重放 init_yhny_20261027.sql 基础数据
-- =============================================================================

-- ---------------- 营销：联系人（5） ----------------
INSERT INTO mk_contact (customer_id,name,phone,create_by,create_time)
SELECT customer_id,'京东店铺运营','13508748579','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0001';
INSERT INTO mk_contact (customer_id,name,phone,create_by,create_time)
SELECT customer_id,'周晓','13508748579','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0002';
INSERT INTO mk_contact (customer_id,name,phone,create_by,create_time)
SELECT customer_id,'李采购','13887001011','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0003';
INSERT INTO mk_contact (customer_id,name,phone,create_by,create_time)
SELECT customer_id,'王工会','13887001012','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0004';
INSERT INTO mk_contact (customer_id,name,phone,create_by,create_time)
SELECT customer_id,'张店主','13887001013','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0005';

-- ---------------- 营销：合同（5） ----------------
INSERT INTO mk_contract (contract_no,contract_name,customer_id,contract_amount,sign_date,effective_date,expire_date,create_by,create_time)
SELECT 'YH-HT-2026-0001','京东平台2026年度供货框架合同',customer_id,200000,'2026-03-01','2026-03-01','2027-02-28','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0001';
INSERT INTO mk_contract (contract_no,contract_name,customer_id,contract_amount,sign_date,effective_date,expire_date,create_by,create_time)
SELECT 'YH-HT-2026-0002','昆明经营部月度供货协议',customer_id,50000,'2026-04-01','2026-04-01','2027-03-31','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0002';
INSERT INTO mk_contract (contract_no,contract_name,customer_id,contract_amount,sign_date,effective_date,expire_date,create_by,create_time)
SELECT 'YH-HT-2026-0003','曲靖商超蜂产品专柜供货合同',customer_id,80000,'2026-05-10','2026-05-10','2027-05-09','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0003';
INSERT INTO mk_contract (contract_no,contract_name,customer_id,contract_amount,sign_date,effective_date,expire_date,create_by,create_time)
SELECT 'YH-HT-2026-0004','中秋蜂蜜礼盒团购合同',customer_id,60000,'2026-07-15','2026-07-15','2026-12-31','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0004';
INSERT INTO mk_contract (contract_no,contract_name,customer_id,contract_amount,sign_date,effective_date,expire_date,create_by,create_time)
SELECT 'YH-HT-2026-0005','特产店分销合作合同',customer_id,30000,'2026-08-01','2026-08-01','2027-07-31','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0005';

-- ---------------- 营销：销售订单（5）+ 明细 ----------------
INSERT INTO mk_order (order_no,customer_id,order_amount,order_status,remark,create_by,create_time)
SELECT 'YH-DD-2026-0001',customer_id,12000,'1','中秋礼盒预订单','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0004';
INSERT INTO mk_order (order_no,customer_id,order_amount,order_status,remark,create_by,create_time)
SELECT 'YH-DD-2026-0002',customer_id,8600,'2','380g土蜂蜜月度补单','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0001';
INSERT INTO mk_order (order_no,customer_id,order_amount,order_status,remark,create_by,create_time)
SELECT 'YH-DD-2026-0003',customer_id,5200,'2','昆明经营部要货单','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0002';
INSERT INTO mk_order (order_no,customer_id,order_amount,order_status,remark,create_by,create_time)
SELECT 'YH-DD-2026-0004',customer_id,7400,'4','商超专柜首批铺货','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0003';
INSERT INTO mk_order (order_no,customer_id,order_amount,order_status,remark,create_by,create_time)
SELECT 'YH-DD-2026-0005',customer_id,3800,'1','特产店试销订单','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0005';
INSERT INTO mk_order_item (order_id,product_name,quantity,unit_price,subtotal,create_time)
SELECT o.order_id,'滇品珍土蜂蜜380g',100,68,6800,NOW() FROM mk_order o WHERE o.order_no='YH-DD-2026-0001';
INSERT INTO mk_order_item (order_id,product_name,quantity,unit_price,subtotal,create_time)
SELECT o.order_id,'滇品珍土蜂蜜礼盒1.5kg',50,200,10000,NOW() FROM mk_order o WHERE o.order_no='YH-DD-2026-0001';
INSERT INTO mk_order_item (order_id,product_name,quantity,unit_price,subtotal,create_time)
SELECT o.order_id,'滇品珍土蜂蜜380g',86,68,5848,NOW() FROM mk_order o WHERE o.order_no='YH-DD-2026-0002';
INSERT INTO mk_order_item (order_id,product_name,quantity,unit_price,subtotal,create_time)
SELECT o.order_id,'滇品珍蜂蜜膏500g',40,88,3520,NOW() FROM mk_order o WHERE o.order_no='YH-DD-2026-0003';
INSERT INTO mk_order_item (order_id,product_name,quantity,unit_price,subtotal,create_time)
SELECT o.order_id,'滇品珍洋槐蜜380g',100,68,6800,NOW() FROM mk_order o WHERE o.order_no='YH-DD-2026-0004';
INSERT INTO mk_order_item (order_id,product_name,quantity,unit_price,subtotal,create_time)
SELECT o.order_id,'组合装蜂蜜礼盒380g×3',20,180,3600,NOW() FROM mk_order o WHERE o.order_no='YH-DD-2026-0005';

-- ---------------- 营销：商机（5） ----------------
INSERT INTO mk_opportunity (opportunity_no,opportunity_name,customer_id,expected_amount,expected_date,create_by,create_time)
SELECT 'YH-SJ-2026-0001','中秋礼盒团购（单位工会）',customer_id,60000,'2026-09-01','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0004';
INSERT INTO mk_opportunity (opportunity_no,opportunity_name,customer_id,expected_amount,expected_date,create_by,create_time)
SELECT 'YH-SJ-2026-0002','连锁药房蜂产品上架',customer_id,40000,'2026-10-01','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0005';
INSERT INTO mk_opportunity (opportunity_no,opportunity_name,customer_id,expected_amount,expected_date,create_by,create_time)
SELECT 'YH-SJ-2026-0003','烘焙用原蜜长期供货',customer_id,120000,'2026-11-01','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0003';
INSERT INTO mk_opportunity (opportunity_no,opportunity_name,customer_id,expected_amount,expected_date,create_by,create_time)
SELECT 'YH-SJ-2026-0004','电商代运营合作扩大铺货',customer_id,80000,'2026-12-01','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0001';
INSERT INTO mk_opportunity (opportunity_no,opportunity_name,customer_id,expected_amount,expected_date,create_by,create_time)
SELECT 'YH-SJ-2026-0005','昆明经营部春节年货备货',customer_id,50000,'2027-01-01','admin',NOW() FROM mk_customer WHERE customer_no='YH-KH-0002';

-- ---------------- 生产：工单（5） ----------------
INSERT INTO mms_work_order (work_order_no,order_type,product_id,product_code,product_name,spec_model,unit,plan_qty,plan_start,plan_finish,status,dept_id,create_by,create_time)
SELECT 'YH-WO-2026-0001',0,p.product_id,p.product_code,p.material_name,p.spec_model,p.unit,500,'2026-10-20','2026-10-22','1',200,'admin',NOW()
FROM pdm_product p WHERE p.product_code='YH-CP-001';
INSERT INTO mms_work_order (work_order_no,order_type,product_id,product_code,product_name,spec_model,unit,plan_qty,plan_start,plan_finish,status,dept_id,create_by,create_time)
SELECT 'YH-WO-2026-0002',0,p.product_id,p.product_code,p.material_name,p.spec_model,p.unit,300,'2026-10-24','2026-10-26','1',200,'admin',NOW()
FROM pdm_product p WHERE p.product_code='YH-CP-001';
INSERT INTO mms_work_order (work_order_no,order_type,product_id,product_code,product_name,spec_model,unit,plan_qty,plan_start,plan_finish,status,dept_id,create_by,create_time)
SELECT 'YH-WO-2026-0003',0,p.product_id,p.product_code,p.material_name,p.spec_model,p.unit,200,'2026-10-28','2026-10-30','3',200,'admin',NOW()
FROM pdm_product p WHERE p.product_code='YH-CP-001';
INSERT INTO mms_work_order (work_order_no,order_type,product_id,product_code,product_name,spec_model,unit,plan_qty,plan_start,plan_finish,status,dept_id,create_by,create_time)
SELECT 'YH-WO-2026-0004',0,p.product_id,p.product_code,p.material_name,p.spec_model,p.unit,150,'2026-11-02','2026-11-04','1',200,'admin',NOW()
FROM pdm_product p WHERE p.product_code='YH-CP-002';
INSERT INTO mms_work_order (work_order_no,order_type,product_id,product_code,product_name,spec_model,unit,plan_qty,plan_start,plan_finish,status,dept_id,create_by,create_time)
SELECT 'YH-WO-2026-0005',0,p.product_id,p.product_code,p.material_name,p.spec_model,p.unit,100,'2026-11-06','2026-11-08','1',200,'admin',NOW()
FROM pdm_product p WHERE p.product_code='YH-CP-003';

-- ---------------- 质量：检验任务（5，IQC原料验收 + 过程/成品） ----------------
INSERT INTO qms_insp_task (task_no,task_type,material_id,material_code,material_name,supplier_name,batch_no,inspect_qty,task_status,inspect_result,dept_id,create_by,create_time)
SELECT 'YH-QT-2026-0001','IQC',m.material_id,m.material_code,m.material_name,'曲靖本地蜂农合作社','YH-2026-1013',1200,'2','1',101,'admin',NOW()
FROM wms_material m WHERE m.material_code='YH-MAT-001';
INSERT INTO qms_insp_task (task_no,task_type,material_id,material_code,material_name,supplier_name,batch_no,inspect_qty,task_status,inspect_result,dept_id,create_by,create_time)
SELECT 'YH-QT-2026-0002','IQC',m.material_id,m.material_code,m.material_name,'曲靖本地蜂农合作社','YH-2026-1015',600,'2','1',101,'admin',NOW()
FROM wms_material m WHERE m.material_code='YH-MAT-002';
INSERT INTO qms_insp_task (task_no,task_type,material_id,material_code,material_name,supplier_name,batch_no,inspect_qty,task_status,inspect_result,dept_id,create_by,create_time)
SELECT 'YH-QT-2026-0003','IQC',m.material_id,m.material_code,m.material_name,'云南玻璃制品有限公司','YH-2026-1021',15000,'2','1',101,'admin',NOW()
FROM wms_material m WHERE m.material_code='YH-MAT-020';
INSERT INTO qms_insp_task (task_no,task_type,material_id,material_code,material_name,batch_no,inspect_qty,task_status,inspect_result,dept_id,create_by,create_time)
SELECT 'YH-QT-2026-0004','FQC',m.material_id,m.material_code,m.material_name,'YH-2026-1020',500,'2','1',101,'admin',NOW()
FROM wms_material m WHERE m.material_code='YH-MAT-010';
INSERT INTO qms_insp_task (task_no,task_type,material_id,material_code,material_name,batch_no,inspect_qty,task_status,inspect_result,dept_id,create_by,create_time)
SELECT 'YH-QT-2026-0005','FQC',m.material_id,m.material_code,m.material_name,'YH-2026-1022',300,'1',NULL,101,'admin',NOW()
FROM wms_material m WHERE m.material_code='YH-MAT-014';

-- ---------------- 质量：不合格品处理 NCR（5） ----------------
INSERT INTO qms_ncr (ncr_no,material_id,material_code,material_name,batch_no,defect_qty,defect_level,defect_desc,ncr_status,dept_id,create_by,create_time)
SELECT 'YH-NCR-2026-0001',m.material_id,m.material_code,m.material_name,'YH-2026-1013',2,'2','抽检发现2瓶波美度低于41.5，判定返工浓缩','1',101,'admin',NOW()
FROM wms_material m WHERE m.material_code='YH-MAT-001';
INSERT INTO qms_ncr (ncr_no,material_id,material_code,material_name,batch_no,defect_qty,defect_level,defect_desc,ncr_status,dept_id,create_by,create_time)
SELECT 'YH-NCR-2026-0002',m.material_id,m.material_code,m.material_name,'YH-2026-1021',30,'1','玻璃瓶到货破损30个，判定退货','1',103,'admin',NOW()
FROM wms_material m WHERE m.material_code='YH-MAT-020';
INSERT INTO qms_ncr (ncr_no,material_id,material_code,material_name,batch_no,defect_qty,defect_level,defect_desc,ncr_status,dept_id,create_by,create_time)
SELECT 'YH-NCR-2026-0003',m.material_id,m.material_code,m.material_name,'YH-2026-1001',1,'2','标签印刷色差，判定让步接收','2',101,'admin',NOW()
FROM wms_material m WHERE m.material_code='YH-MAT-023';
INSERT INTO qms_ncr (ncr_no,material_id,material_code,material_name,batch_no,defect_qty,defect_level,defect_desc,ncr_status,dept_id,create_by,create_time)
SELECT 'YH-NCR-2026-0004',m.material_id,m.material_code,m.material_name,'YH-2026-1020',5,'2','灌装液位偏差5瓶，判定返工','1',200,'admin',NOW()
FROM wms_material m WHERE m.material_code='YH-MAT-010';
INSERT INTO qms_ncr (ncr_no,material_id,material_code,material_name,batch_no,defect_qty,defect_level,defect_desc,ncr_status,dept_id,create_by,create_time)
SELECT 'YH-NCR-2026-0005',m.material_id,m.material_code,m.material_name,'YH-2026-1030',3,'1','鲜花酥到货临期（剩余保质期不足1/3），判定退货','1',103,'admin',NOW()
FROM wms_material m WHERE m.material_code='YH-MAT-030';

-- ---------------- 质量：质量目标（5，2026年分维度） ----------------
INSERT INTO qms_quality_target (target_year,target_month,dimension,dimension_val,target_pass_rate,actual_pass_rate,target_ppm,actual_ppm,target_complaint_rate,actual_complaint_rate,target_capa_close_rate,actual_capa_close_rate,achievement,status,del_flag,create_by,create_time) VALUES
(2026,10,'原料批次合格率','原蜜原料验收',99.0,98.5,500,600,1.0,0.8,95.0,92.0,'0','0','0','admin',NOW()),
(2026,10,'成品一次合格率','灌装成品出厂检验',99.5,99.2,300,350,0.5,0.4,98.0,95.0,'0','0','0','admin',NOW()),
(2026,10,'客户投诉率','客诉处理',NULL,NULL,NULL,NULL,0.5,0.6,95.0,90.0,'0','0','0','admin',NOW()),
(2026,10,'CAPA闭环率','纠正预防措施',NULL,NULL,NULL,NULL,NULL,NULL,100.0,95.0,'0','0','0','admin',NOW()),
(2026,10,'监督抽检合格率','省市监抽检',100.0,100.0,NULL,NULL,NULL,NULL,NULL,NULL,'1','0','0','admin',NOW());

-- ---------------- 采购：采购申请（5）+ 明细 ----------------
INSERT INTO pms_purchase_request (request_no,title,status,priority,expect_date,purpose,total_amount,requester_id,requester_name,dept_id,dept_name,create_by,create_time) VALUES
('YH-CGQD-2026-0001','原蜜采购申请（十月采蜜季）','1','1','2026-11-05','采蜜季原蜜补库，土蜂蜜1200kg',96000,1,'admin',103,'供应链部','admin',NOW()),
('YH-CGQD-2026-0002','玻璃瓶包材采购申请','1','2','2026-11-08','380g瓶10000个/500g瓶6000个',9000,1,'admin',103,'供应链部','admin',NOW()),
('YH-CGQD-2026-0003','标签及礼盒采购申请','1','2','2026-11-10','中秋礼盒外箱与标签备货',6000,1,'admin',103,'供应链部','admin',NOW()),
('YH-CGQD-2026-0004','外采品采购申请（中秋）','1','1','2026-08-20','鲜花酥/月饼礼盒团购备货',15000,1,'admin',103,'供应链部','admin',NOW()),
('YH-CGQD-2026-0005','化验耗材采购申请','1','3','2026-11-15','化验室试剂与培养基补充',2000,1,'admin',101,'质检化验部','admin',NOW());
INSERT INTO pms_purchase_request_detail (request_id,material_id,material_code,material_name,spec_model,unit,qty,estimated_price,estimated_amount,create_by,create_time)
SELECT r.request_id,m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,1200,80,96000,'admin',NOW()
FROM pms_purchase_request r, wms_material m WHERE r.request_no='YH-CGQD-2026-0001' AND m.material_code='YH-MAT-001';
INSERT INTO pms_purchase_request_detail (request_id,material_id,material_code,material_name,spec_model,unit,qty,estimated_price,estimated_amount,create_by,create_time)
SELECT r.request_id,m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,10000,0.6,6000,'admin',NOW()
FROM pms_purchase_request r, wms_material m WHERE r.request_no='YH-CGQD-2026-0002' AND m.material_code='YH-MAT-020';
INSERT INTO pms_purchase_request_detail (request_id,material_id,material_code,material_name,spec_model,unit,qty,estimated_price,estimated_amount,create_by,create_time)
SELECT r.request_id,m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,6000,0.5,3000,'admin',NOW()
FROM pms_purchase_request r, wms_material m WHERE r.request_no='YH-CGQD-2026-0002' AND m.material_code='YH-MAT-021';
INSERT INTO pms_purchase_request_detail (request_id,material_id,material_code,material_name,spec_model,unit,qty,estimated_price,estimated_amount,create_by,create_time)
SELECT r.request_id,m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,300,20,6000,'admin',NOW()
FROM pms_purchase_request r, wms_material m WHERE r.request_no='YH-CGQD-2026-0003' AND m.material_code='YH-MAT-024';
INSERT INTO pms_purchase_request_detail (request_id,material_id,material_code,material_name,spec_model,unit,qty,estimated_price,estimated_amount,create_by,create_time)
SELECT r.request_id,m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,100,150,15000,'admin',NOW()
FROM pms_purchase_request r, wms_material m WHERE r.request_no='YH-CGQD-2026-0004' AND m.material_code='YH-MAT-031';

-- ---------------- 采购：采购订单（5）+ 明细 ----------------
INSERT INTO pms_purchase_order (order_no,supplier_id,supplier_name,warehouse_id,status,order_date,expect_delivery_date,total_qty,total_amount,buyer_name,dept_id,dept_name,create_by,create_time)
SELECT 'YH-CGDD-2026-0001',s.supplier_id,s.supplier_name,w.warehouse_id,'5','2026-10-15','2026-10-20',1200,96000,'admin',103,'供应链部','admin',NOW()
FROM wms_supplier s, wms_warehouse w WHERE s.supplier_code='YH-GYS-001' AND w.warehouse_code='YH-WH01';
INSERT INTO pms_purchase_order (order_no,supplier_id,supplier_name,warehouse_id,status,order_date,expect_delivery_date,total_qty,total_amount,buyer_name,dept_id,dept_name,create_by,create_time)
SELECT 'YH-CGDD-2026-0002',s.supplier_id,s.supplier_name,w.warehouse_id,'5','2026-10-18','2026-10-25',16000,9000,'admin',103,'供应链部','admin',NOW()
FROM wms_supplier s, wms_warehouse w WHERE s.supplier_code='YH-GYS-002' AND w.warehouse_code='YH-WH03';
INSERT INTO pms_purchase_order (order_no,supplier_id,supplier_name,warehouse_id,status,order_date,expect_delivery_date,total_qty,total_amount,buyer_name,dept_id,dept_name,create_by,create_time)
SELECT 'YH-CGDD-2026-0003',s.supplier_id,s.supplier_name,w.warehouse_id,'5','2026-10-20','2026-10-28',300,6000,'admin',103,'供应链部','admin',NOW()
FROM wms_supplier s, wms_warehouse w WHERE s.supplier_code='YH-GYS-003' AND w.warehouse_code='YH-WH03';
INSERT INTO pms_purchase_order (order_no,supplier_id,supplier_name,warehouse_id,status,order_date,expect_delivery_date,total_qty,total_amount,buyer_name,dept_id,dept_name,create_by,create_time)
SELECT 'YH-CGDD-2026-0004',s.supplier_id,s.supplier_name,w.warehouse_id,'5','2026-08-10','2026-08-25',100,15000,'admin',103,'供应链部','admin',NOW()
FROM wms_supplier s, wms_warehouse w WHERE s.supplier_code='YH-GYS-006' AND w.warehouse_code='YH-WH02';
INSERT INTO pms_purchase_order (order_no,supplier_id,supplier_name,warehouse_id,status,order_date,expect_delivery_date,total_qty,total_amount,buyer_name,dept_id,dept_name,create_by,create_time)
SELECT 'YH-CGDD-2026-0005',s.supplier_id,s.supplier_name,w.warehouse_id,'5','2026-10-25','2026-11-05',1,2000,'admin',101,'质检化验部','admin',NOW()
FROM wms_supplier s, wms_warehouse w WHERE s.supplier_code='YH-GYS-004' AND w.warehouse_code='YH-WH01';
INSERT INTO pms_purchase_order_detail (order_id,material_id,material_code,material_name,spec_model,unit,qty,unit_price,amount,create_by,create_time)
SELECT o.order_id,m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,1200,80,96000,'admin',NOW()
FROM pms_purchase_order o, wms_material m WHERE o.order_no='YH-CGDD-2026-0001' AND m.material_code='YH-MAT-001';
INSERT INTO pms_purchase_order_detail (order_id,material_id,material_code,material_name,spec_model,unit,qty,unit_price,amount,create_by,create_time)
SELECT o.order_id,m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,10000,0.6,6000,'admin',NOW()
FROM pms_purchase_order o, wms_material m WHERE o.order_no='YH-CGDD-2026-0002' AND m.material_code='YH-MAT-020';
INSERT INTO pms_purchase_order_detail (order_id,material_id,material_code,material_name,spec_model,unit,qty,unit_price,amount,create_by,create_time)
SELECT o.order_id,m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,6000,0.5,3000,'admin',NOW()
FROM pms_purchase_order o, wms_material m WHERE o.order_no='YH-CGDD-2026-0002' AND m.material_code='YH-MAT-021';
INSERT INTO pms_purchase_order_detail (order_id,material_id,material_code,material_name,spec_model,unit,qty,unit_price,amount,create_by,create_time)
SELECT o.order_id,m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,300,20,6000,'admin',NOW()
FROM pms_purchase_order o, wms_material m WHERE o.order_no='YH-CGDD-2026-0003' AND m.material_code='YH-MAT-024';
INSERT INTO pms_purchase_order_detail (order_id,material_id,material_code,material_name,spec_model,unit,qty,unit_price,amount,create_by,create_time)
SELECT o.order_id,m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,100,150,15000,'admin',NOW()
FROM pms_purchase_order o, wms_material m WHERE o.order_no='YH-CGDD-2026-0004' AND m.material_code='YH-MAT-031';

-- ---------------- 仓储：入库单（5）+ 明细 ----------------
INSERT INTO wms_inbound_order (order_no,order_type,supplier_id,warehouse_id,status,inbound_date,total_qty,dept_id,create_by,create_time)
SELECT 'YH-RK-2026-0001',0,s.supplier_id,w.warehouse_id,'3','2026-10-16',1200,103,'admin',NOW()
FROM wms_supplier s, wms_warehouse w WHERE s.supplier_code='YH-GYS-001' AND w.warehouse_code='YH-WH01';
INSERT INTO wms_inbound_order (order_no,order_type,supplier_id,warehouse_id,status,inbound_date,total_qty,dept_id,create_by,create_time)
SELECT 'YH-RK-2026-0002',0,s.supplier_id,w.warehouse_id,'3','2026-10-19',16000,103,'admin',NOW()
FROM wms_supplier s, wms_warehouse w WHERE s.supplier_code='YH-GYS-002' AND w.warehouse_code='YH-WH03';
INSERT INTO wms_inbound_order (order_no,order_type,supplier_id,warehouse_id,status,inbound_date,total_qty,dept_id,create_by,create_time)
SELECT 'YH-RK-2026-0003',0,s.supplier_id,w.warehouse_id,'3','2026-10-22',300,103,'admin',NOW()
FROM wms_supplier s, wms_warehouse w WHERE s.supplier_code='YH-GYS-003' AND w.warehouse_code='YH-WH03';
INSERT INTO wms_inbound_order (order_no,order_type,supplier_id,warehouse_id,status,inbound_date,total_qty,dept_id,create_by,create_time)
SELECT 'YH-RK-2026-0004',0,s.supplier_id,w.warehouse_id,'3','2026-10-25',100,103,'admin',NOW()
FROM wms_supplier s, wms_warehouse w WHERE s.supplier_code='YH-GYS-006' AND w.warehouse_code='YH-WH02';
INSERT INTO wms_inbound_order (order_no,order_type,supplier_id,warehouse_id,status,inbound_date,total_qty,dept_id,create_by,create_time)
SELECT 'YH-RK-2026-0005',0,s.supplier_id,w.warehouse_id,'3','2026-10-26',400,103,'admin',NOW()
FROM wms_supplier s, wms_warehouse w WHERE s.supplier_code='YH-GYS-001' AND w.warehouse_code='YH-WH01';
INSERT INTO wms_inbound_order_detail (order_id,material_id,plan_qty,received_qty,putaway_qty,batch_no,production_date,expiry_date,create_by,create_time)
SELECT o.order_id,m.material_id,1200,1200,1200,'YH-2026-1013','2026-10-14','2028-10-13','admin',NOW()
FROM wms_inbound_order o, wms_material m WHERE o.order_no='YH-RK-2026-0001' AND m.material_code='YH-MAT-001';
INSERT INTO wms_inbound_order_detail (order_id,material_id,plan_qty,received_qty,putaway_qty,batch_no,production_date,create_by,create_time)
SELECT o.order_id,m.material_id,10000,10000,10000,NULL,'2026-10-18','admin',NOW()
FROM wms_inbound_order o, wms_material m WHERE o.order_no='YH-RK-2026-0002' AND m.material_code='YH-MAT-020';
INSERT INTO wms_inbound_order_detail (order_id,material_id,plan_qty,received_qty,putaway_qty,batch_no,production_date,create_by,create_time)
SELECT o.order_id,m.material_id,6000,6000,6000,NULL,'2026-10-18','admin',NOW()
FROM wms_inbound_order o, wms_material m WHERE o.order_no='YH-RK-2026-0002' AND m.material_code='YH-MAT-021';
INSERT INTO wms_inbound_order_detail (order_id,material_id,plan_qty,received_qty,putaway_qty,batch_no,production_date,create_by,create_time)
SELECT o.order_id,m.material_id,300,300,300,NULL,'2026-10-22','admin',NOW()
FROM wms_inbound_order o, wms_material m WHERE o.order_no='YH-RK-2026-0003' AND m.material_code='YH-MAT-024';
INSERT INTO wms_inbound_order_detail (order_id,material_id,plan_qty,received_qty,putaway_qty,batch_no,production_date,expiry_date,create_by,create_time)
SELECT o.order_id,m.material_id,100,100,100,'YH-2026-1025','2026-10-25','2027-04-24','admin',NOW()
FROM wms_inbound_order o, wms_material m WHERE o.order_no='YH-RK-2026-0004' AND m.material_code='YH-MAT-030';
INSERT INTO wms_inbound_order_detail (order_id,material_id,plan_qty,received_qty,putaway_qty,batch_no,production_date,expiry_date,create_by,create_time)
SELECT o.order_id,m.material_id,400,400,400,'YH-2026-1018','2026-10-18','2028-10-17','admin',NOW()
FROM wms_inbound_order o, wms_material m WHERE o.order_no='YH-RK-2026-0005' AND m.material_code='YH-MAT-002';

-- ---------------- 仓储：出库单（5）+ 明细 ----------------
INSERT INTO wms_outbound_order (order_no,order_type,warehouse_id,status,outbound_date,total_qty,dept_id,create_by,create_time) VALUES
('YH-CK-2026-0001',0,(SELECT warehouse_id FROM wms_warehouse WHERE warehouse_code='YH-WH02'),'3','2026-10-24',100,103,'admin',NOW()),
('YH-CK-2026-0002',0,(SELECT warehouse_id FROM wms_warehouse WHERE warehouse_code='YH-WH02'),'3','2026-10-26',90,103,'admin',NOW()),
('YH-CK-2026-0003',0,(SELECT warehouse_id FROM wms_warehouse WHERE warehouse_code='YH-WH02'),'3','2026-10-27',40,103,'admin',NOW()),
('YH-CK-2026-0004',0,(SELECT warehouse_id FROM wms_warehouse WHERE warehouse_code='YH-WH02'),'3','2026-10-28',100,103,'admin',NOW()),
('YH-CK-2026-0005',0,(SELECT warehouse_id FROM wms_warehouse WHERE warehouse_code='YH-WH03'),'3','2026-10-29',200,103,'admin',NOW());
INSERT INTO wms_outbound_order_detail (order_id,material_id,batch_no,plan_qty,actual_qty,create_by,create_time)
SELECT o.order_id,m.material_id,'YH-2026-1020',100,100,'admin',NOW()
FROM wms_outbound_order o, wms_material m WHERE o.order_no='YH-CK-2026-0001' AND m.material_code='YH-MAT-010';
INSERT INTO wms_outbound_order_detail (order_id,material_id,batch_no,plan_qty,actual_qty,create_by,create_time)
SELECT o.order_id,m.material_id,'YH-2026-1020',90,90,'admin',NOW()
FROM wms_outbound_order o, wms_material m WHERE o.order_no='YH-CK-2026-0002' AND m.material_code='YH-MAT-010';
INSERT INTO wms_outbound_order_detail (order_id,material_id,batch_no,plan_qty,actual_qty,create_by,create_time)
SELECT o.order_id,m.material_id,NULL,40,40,'admin',NOW()
FROM wms_outbound_order o, wms_material m WHERE o.order_no='YH-CK-2026-0003' AND m.material_code='YH-MAT-014';
INSERT INTO wms_outbound_order_detail (order_id,material_id,batch_no,plan_qty,actual_qty,create_by,create_time)
SELECT o.order_id,m.material_id,NULL,100,100,'admin',NOW()
FROM wms_outbound_order o, wms_material m WHERE o.order_no='YH-CK-2026-0004' AND m.material_code='YH-MAT-012';
INSERT INTO wms_outbound_order_detail (order_id,material_id,batch_no,plan_qty,actual_qty,create_by,create_time)
SELECT o.order_id,m.material_id,NULL,200,200,'admin',NOW()
FROM wms_outbound_order o, wms_material m WHERE o.order_no='YH-CK-2026-0005' AND m.material_code='YH-MAT-025';

-- ---------------- 人事：员工档案（5） ----------------
INSERT INTO biz_hr_employee (employee_no,employee_name,gender,phone,dept_id,position_name,employee_type,entry_date,employee_status,status,del_flag,create_by,create_time) VALUES
('YH-E-0001','袁玉伟','0','13508748579',100,'总经理','1','2013-11-07','1','0','0','admin',NOW()),
('YH-E-0002','杨春梅','1','13887001021',106,'财务负责人','1','2017-12-04','1','0','0','admin',NOW()),
('YH-E-0003','周晓','1','13887001022',105,'综合管理','1','2018-03-13','1','0','0','admin',NOW()),
('YH-E-0004','周红亮','0','13887001023',101,'化验员','1','2021-06-01','1','0','0','admin',NOW()),
('YH-E-0005','袁飞','0','13887001024',200,'蜂场技术员','1','2016-01-01','1','0','0','admin',NOW());

-- ---------------- 安全：从业人员（5） ----------------
INSERT INTO biz_safety_worker (worker_name,create_by,create_time) VALUES
('袁玉伟','admin',NOW()),('周红亮','admin',NOW()),('袁飞','admin',NOW()),('杨春梅','admin',NOW()),('周晓','admin',NOW());

-- ---------------- 安全：培训计划（5） ----------------
INSERT INTO biz_safety_training_plan (plan_code,plan_name,create_by,create_time) VALUES
('YH-AQ-PX-2026-001','食品安全法及SC许可要求培训','admin',NOW()),
('YH-AQ-PX-2026-002','十万级净化车间卫生规范培训','admin',NOW()),
('YH-AQ-PX-2026-003','养蜂场野外作业安全培训','admin',NOW()),
('YH-AQ-PX-2026-004','消防与用电安全培训','admin',NOW()),
('YH-AQ-PX-2026-005','蜂药使用规范与残留控制培训','admin',NOW());

-- ---------------- 安全：风险点（补足至5） ----------------
INSERT INTO biz_safety_risk_point (risk_code,risk_name,hazard_desc,accident_type,risk_level,area_name,dept_id,measure_engine,measure_manage,check_standard,check_frequency,status,del_flag,create_by,create_time) VALUES
('YH-AQ-002','浓缩加热设备高温作业','浓缩罐高温表面可能烫伤','burn','2','过滤加工车间',200,'设备加装隔热层，张贴警示','操作人员持证上岗，配发防护用品','隔热层完好，警示标识清晰','每班','0','0','admin',NOW()),
('YH-AQ-003','冷库低温作业','王浆花粉冷库低温冻伤风险','other','3','原料仓库',103,'库内作业限时并配防寒服','两人同入制度','防寒装备齐备，限时作业执行','每周','0','0','admin',NOW()),
('YH-AQ-004','货运车辆运输安全','配送车辆道路交通安全','traffic','2','厂区及配送线路',105,'车辆定期维保，GPS监控','司机持证上岗，禁止疲劳驾驶','车辆维保记录齐全，无违章','每月','0','0','admin',NOW()),
('YH-AQ-005','蜂场野外作业风险','野外蜂蜇伤及气候变化风险','other','3','各生态养蜂场',200,'配备防护服与应急药箱','转地放蜂前检查天气与场地','防护装备齐备，应急药品有效','每次转场','0','0','admin',NOW());

-- ---------------- 产品设计：补足至5（新增饮品新品，设计中） ----------------
INSERT INTO pdm_product (product_code,material_name,spec_model,unit,category_id,category_name,product_status,designer_id,designer_name,dept_id,del_flag,create_by,create_time)
SELECT 'YH-CP-005','蜜上他蜂蜜饮品','330ml/瓶','瓶',c.category_id,c.category_name,'0',1,'袁玉伟',200,'0','admin',NOW()
FROM pdm_product_category c WHERE c.category_code='YH-PC-04'
AND NOT EXISTS (SELECT 1 FROM pdm_product WHERE product_code='YH-CP-005');

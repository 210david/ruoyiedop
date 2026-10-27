-- =============================================================================
-- YHNY（曲靖源禾农业）业务数据初始化脚本
-- 日期：2026-10-27  目标库：ruoyi_yhny
-- 前提：业务表已 TRUNCATE，系统数据（用户/角色/权限/菜单/字典/编码规则）保留
-- 内容：组织岗位 / 仓储物料 / 供应商 / 客户线索 / 产品BOM / 检验标准 /
--       设备台账 / 厂房产线 / 渠道公告 / 门户首页
-- 注意：执行前请阅读文末"回滚说明"章节
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 一、组织架构（UPDATE 保留 dept_id 与用户引用，新增蜂场等部门）
-- -----------------------------------------------------------------------------
UPDATE `sys_dept` SET dept_name='曲靖源禾农业开发有限公司', leader='袁玉伟', phone='13508748579', email='1966168216@qq.com' WHERE dept_id=100;
UPDATE `sys_dept` SET dept_name='生产部', leader='袁玉伟', ancestors='0,100' WHERE dept_id=200;
UPDATE `sys_dept` SET dept_name='质检化验部', leader='杨春梅', ancestors='0,100' WHERE dept_id=101;
UPDATE `sys_dept` SET dept_name='供应链部', leader='周晓', ancestors='0,100' WHERE dept_id=103;
UPDATE `sys_dept` SET dept_name='销售部', leader='周晓', ancestors='0,100' WHERE dept_id=104;
UPDATE `sys_dept` SET dept_name='综合管理部', leader='杨春梅', ancestors='0,100' WHERE dept_id=105;
UPDATE `sys_dept` SET dept_name='财务部', leader='杨春梅', ancestors='0,100' WHERE dept_id=106;
UPDATE `sys_dept` SET dept_name='电商运营组', leader='周晓', ancestors='0,100,104' WHERE dept_id=107;
UPDATE `sys_dept` SET dept_name='蜂场管理部', leader='袁玉伟', ancestors='0,100' WHERE dept_id=200;
UPDATE `sys_dept` SET dept_name='昆明经营部', leader='周晓', ancestors='0,100' WHERE dept_id=102;
UPDATE `sys_dept` SET dept_name='一号生态养蜂场', leader='袁玉伟', ancestors='0,100,200' WHERE dept_id=101;
UPDATE `sys_dept` SET dept_name='二号生态养蜂场', leader='袁玉伟', ancestors='0,100,200' WHERE dept_id=108;
UPDATE `sys_dept` SET dept_name='三号生态养蜂场', leader='袁玉伟', ancestors='0,100,200' WHERE dept_id=109;

-- 岗位（UPDATE 保留 post_id 与用户引用）
UPDATE `sys_post` SET post_code='ceo', post_name='总经理' WHERE post_id=1;
UPDATE `sys_post` SET post_code='prod_mgr', post_name='生产负责人' WHERE post_id=2;
UPDATE `sys_post` SET post_code='qc', post_name='化验员/质检员' WHERE post_id=3;
UPDATE `sys_post` SET post_code='beekeeper', post_name='蜂场技术员' WHERE post_id=4;
UPDATE `sys_post` SET post_code='operator', post_name='车间操作工' WHERE post_id=5;
UPDATE `sys_post` SET post_code='driver', post_name='货运司机' WHERE post_id=6;

-- -----------------------------------------------------------------------------
-- 二、仓储：仓库 / 库区 / 仓位（node_type 1仓库 2库区 3仓位）
-- -----------------------------------------------------------------------------
INSERT INTO `wms_warehouse` (parent_id,ancestors,node_type,warehouse_code,warehouse_name,address,area_type,location_type,status,del_flag,create_by,create_time) VALUES
(0,'0',1,'YH-WH01','原料仓库','麒麟区沿江街道庄家圩社区一组','0','0','0','0','admin',NOW()),
(0,'0',1,'YH-WH02','成品仓库','麒麟区沿江街道庄家圩社区一组','0','0','0','0','admin',NOW()),
(0,'0',1,'YH-WH03','包材仓库','麒麟区沿江街道庄家圩社区一组','0','0','0','0','admin',NOW()),
(0,'0',1,'YH-WH04','蜂场周转仓','麒麟区各养蜂场','4','2','0','0','admin',NOW());
INSERT INTO `wms_warehouse` (parent_id,ancestors,node_type,warehouse_code,warehouse_name,area_type,location_type,status,del_flag,create_by,create_time)
SELECT warehouse_id,warehouse_id,2,CONCAT(warehouse_code,'A01'),'常温存储区','0','0','0','0','admin',NOW() FROM wms_warehouse WHERE warehouse_code LIKE 'YH-WH%';
INSERT INTO `wms_warehouse` (parent_id,ancestors,node_type,warehouse_code,warehouse_name,area_type,location_type,status,del_flag,create_by,create_time)
SELECT parent_id,CONCAT(ancestors,',',parent_id),3,CONCAT(warehouse_code,'L01'),'货架位01','0','0','0','0','admin',NOW() FROM wms_warehouse WHERE node_type=2;

-- -----------------------------------------------------------------------------
-- 三、物料（wms_material：0原材料 1半成品 2成品 3辅料）
-- -----------------------------------------------------------------------------
INSERT INTO `wms_material` (material_code,material_name,material_type,spec_model,unit,barcode,safety_stock_min,safety_stock_max,is_batch_manage,status,del_flag,create_by,create_time) VALUES
('YH-MAT-001','土蜂蜜（原蜜）','0','云南高原土蜂蜜 波美度≥41.5','kg',NULL,200,2000,'1','0','0','admin',NOW()),
('YH-MAT-002','洋槐蜜（原蜜）','0','云南洋槐蜜 波美度≥41','kg',NULL,150,1500,'1','0','0','admin',NOW()),
('YH-MAT-003','紫云英蜜（原蜜）','0','云南紫云英蜜 波美度≥41','kg',NULL,100,1000,'1','0','0','admin',NOW()),
('YH-MAT-004','蜂王浆','0','鲜活王浆 冷冻保存','kg',NULL,10,100,'1','0','0','admin',NOW()),
('YH-MAT-005','蜂花粉','0','油菜花粉','kg',NULL,10,100,'1','0','0','admin',NOW()),
('YH-MAT-010','土蜂蜜（成品）','2','380g/瓶 玻璃瓶','瓶',NULL,500,5000,'1','0','0','admin',NOW()),
('YH-MAT-011','土蜂蜜（成品）','2','500g/瓶 玻璃瓶','瓶',NULL,500,5000,'1','0','0','admin',NOW()),
('YH-MAT-012','洋槐蜜（成品）','2','380g/瓶 玻璃瓶','瓶',NULL,300,3000,'1','0','0','admin',NOW()),
('YH-MAT-013','紫云英蜜（成品）','2','380g/瓶 玻璃瓶','瓶',NULL,300,3000,'1','0','0','admin',NOW()),
('YH-MAT-014','蜂蜜膏','2','500g/瓶','瓶',NULL,200,2000,'1','0','0','admin',NOW()),
('YH-MAT-015','土蜂蜜礼盒','2','1.5kg礼盒装（土+洋+紫）','盒',NULL,100,1000,'1','0','0','admin',NOW()),
('YH-MAT-016','组合装蜂蜜礼盒','2','380g×3瓶组合装','盒',NULL,100,1000,'1','0','0','admin',NOW()),
('YH-MAT-020','玻璃瓶','3','380ml 蜂蜜专用瓶','个',NULL,5000,30000,'0','0','0','admin',NOW()),
('YH-MAT-021','玻璃瓶','3','500ml 蜂蜜专用瓶','个',NULL,5000,30000,'0','0','0','admin',NOW()),
('YH-MAT-022','瓶盖（按压式）','3','按压式储料瓶盖 专利品','个',NULL,5000,30000,'0','0','0','admin',NOW()),
('YH-MAT-023','标签（滇品珍）','3','不干胶标签 380g款','张',NULL,5000,30000,'0','0','0','admin',NOW()),
('YH-MAT-024','外包装礼盒','3','1.5kg蜂蜜礼盒 彩印','个',NULL,500,3000,'0','0','0','admin',NOW()),
('YH-MAT-025','快递纸箱','3','五层瓦楞 6瓶装','个',NULL,1000,10000,'0','0','0','admin',NOW()),
('YH-MAT-030','云南鲜花酥（外采）','2','256g/盒 开口笑鲜花酥','盒',NULL,200,2000,'1','0','0','admin',NOW()),
('YH-MAT-031','滇式月饼（外采/季节）','2','中秋月饼 礼盒装','盒',NULL,100,1000,'1','0','0','admin',NOW()),
('YH-MAT-032','宣威火腿（外采）','2','礼盒装','盒',NULL,50,500,'1','0','0','admin',NOW());

-- -----------------------------------------------------------------------------
-- 四、供应商（wms_supplier：0原材料 1设备 2服务 3贸易商；级别0-3）
-- -----------------------------------------------------------------------------
INSERT INTO `wms_supplier` (supplier_code,supplier_name,supplier_short_name,supplier_type,supplier_level,contact_person,contact_phone,address,status,del_flag,create_by,create_time) VALUES
('YH-GYS-001','曲靖本地蜂农合作社','本地蜂农社','0','2','袁师','13508748579','曲靖市麒麟区','0','0','admin',NOW()),
('YH-GYS-002','云南玻璃制品有限公司','云南玻璃','3','1','李经理','13887000001','昆明市','0','0','admin',NOW()),
('YH-GYS-003','曲靖彩印包装有限公司','曲靖彩印','3','1','王经理','13887000002','曲靖市麒麟区','0','0','admin',NOW()),
('YH-GYS-004','云南三正技术检测有限公司','三正检测','2','1','检测业务部','13887000003','昆明市','0','0','admin',NOW()),
('YH-GYS-005','云南商测质量检验技术服务有限公司','商测质检','2','2','检测业务部','13887000004','昆明市','0','0','admin',NOW()),
('YH-GYS-006','昆明鲜花饼食品有限公司','鲜花饼厂','3','2','刘经理','13887000005','昆明市','0','0','admin',NOW()),
('YH-GYS-007','灌装设备服务有限公司','灌装设备服务','1','2','张工','13887000006','曲靖市','0','0','admin',NOW());

-- -----------------------------------------------------------------------------
-- 五、客户与线索（mk_customer / mk_lead）
-- -----------------------------------------------------------------------------
INSERT INTO `mk_customer` (customer_no,customer_name,customer_level,customer_source,customer_status,industry,province,city,address,user_id,dept_id,del_flag,create_by,create_time) VALUES
('YH-KH-0001','京东平台自营渠道（曲靖源禾农业店）','1','0','0','电商平台','云南省','昆明市',NULL,1,104,'0','admin',NOW()),
('YH-KH-0002','昆明滇品珍经营部','1','1','0','食品经销','云南省','昆明市','盘龙区白云路金尚俊园',1,104,'0','admin',NOW()),
('YH-KH-0003','曲靖本地商超连锁','2','1','0','商超零售','云南省','曲靖市','麒麟区',1,104,'0','admin',NOW()),
('YH-KH-0004','企业团购客户（礼盒定制）','2','2','0','团购','云南省','曲靖市',NULL,1,104,'0','admin',NOW()),
('YH-KH-0005','特产礼品店分销客户','3','1','0','零售分销','云南省','曲靖市',NULL,1,104,'0','admin',NOW());

INSERT INTO `mk_lead` (lead_no,company_name,contact_name,contact_phone,position,industry,province,city,lead_source,requirement_desc,lead_status,del_flag,create_by,create_time) VALUES
('YH-XS-0001','曲靖某单位工会','李老师','13887001001','工会主席','机关单位','云南省','曲靖市','2','中秋蜂蜜礼盒团购，需定制包装','0','0','admin',NOW()),
('YH-XS-0002','昆明某连锁药房','王店长','13887001002','采购经理','医药零售','云南省','昆明市','1','蜂产品上架，关注蜂胶与蜂蜜膏','0','0','admin',NOW()),
('YH-XS-0003','曲靖某烘焙连锁','张老板','13887001003','店主','烘焙','云南省','曲靖市','3','烘焙用原蜜长期采购，月需500kg','0','0','admin',NOW()),
('YH-XS-0004','电商代运营公司','赵经理','13887001004','商务经理','电商服务','云南省','昆明市','0','希望承接京东店铺代运营','0','0','admin',NOW());

-- -----------------------------------------------------------------------------
-- 六、渠道（mk_channel：京东店铺等）
-- -----------------------------------------------------------------------------
INSERT INTO `mk_channel` (channel_type,account_name,account_id,account_url,fans_count,channel_status,del_flag,create_by,create_time) VALUES
('2','曲靖源禾农业店','16680641','https://mall.jd.com/index-16680641.html',0,'0','0','admin',NOW());

-- -----------------------------------------------------------------------------
-- 七、产品与分类（pdm_product_category / pdm_product）
-- -----------------------------------------------------------------------------
INSERT INTO `pdm_product_category` (parent_id,ancestors,category_code,category_name,order_num,status,del_flag,create_by,create_time) VALUES
(0,'0','YH-PC-01','蜂蜜制品',1,'0','0','admin',NOW()),
(0,'0','YH-PC-02','蜂产品系列',2,'0','0','admin',NOW()),
(0,'0','YH-PC-03','礼盒装',3,'0','0','admin',NOW()),
(0,'0','YH-PC-04','饮品/外采食品',4,'0','0','admin',NOW());
INSERT INTO `pdm_product` (product_code,material_id,material_name,spec_model,unit,category_id,category_name,product_status,designer_id,designer_name,dept_id,del_flag,create_by,create_time)
SELECT 'YH-CP-001',m.material_id,m.material_name,m.spec_model,m.unit,c.category_id,c.category_name,'1',1,'袁玉伟',200,'0','admin',NOW()
FROM wms_material m, pdm_product_category c WHERE m.material_code='YH-MAT-010' AND c.category_code='YH-PC-01';
INSERT INTO `pdm_product` (product_code,material_id,material_name,spec_model,unit,category_id,category_name,product_status,designer_id,designer_name,dept_id,del_flag,create_by,create_time)
SELECT 'YH-CP-002',m.material_id,m.material_name,m.spec_model,m.unit,c.category_id,c.category_name,'1',1,'袁玉伟',200,'0','admin',NOW()
FROM wms_material m, pdm_product_category c WHERE m.material_code='YH-MAT-014' AND c.category_code='YH-PC-02';
INSERT INTO `pdm_product` (product_code,material_id,material_name,spec_model,unit,category_id,category_name,product_status,designer_id,designer_name,dept_id,del_flag,create_by,create_time)
SELECT 'YH-CP-003',m.material_id,m.material_name,m.spec_model,m.unit,c.category_id,c.category_name,'0',1,'袁玉伟',200,'0','admin',NOW()
FROM wms_material m, pdm_product_category c WHERE m.material_code='YH-MAT-015' AND c.category_code='YH-PC-03';
INSERT INTO `pdm_product` (product_code,material_id,material_name,spec_model,unit,category_id,category_name,product_status,designer_id,designer_name,dept_id,del_flag,create_by,create_time)
SELECT 'YH-CP-004',m.material_id,m.material_name,m.spec_model,m.unit,c.category_id,c.category_name,'0',1,'袁玉伟',200,'0','admin',NOW()
FROM wms_material m, pdm_product_category c WHERE m.material_code='YH-MAT-030' AND c.category_code='YH-PC-04';

-- -----------------------------------------------------------------------------
-- 八、BOM（mms_bom / mms_bom_detail：成品 → 原蜜+包材）
-- -----------------------------------------------------------------------------
INSERT INTO `mms_bom` (bom_no,bom_name,product_id,product_code,product_name,bom_type,version,base_qty,base_unit,status,effective_date,create_by,create_time)
SELECT 'YH-BOM-001','380g土蜂蜜灌装BOM V1.0',p.product_id,p.product_code,p.material_name,'1','V1.0',1,'瓶','1',CURDATE(),'admin',NOW()
FROM pdm_product p WHERE p.product_code='YH-CP-001';
INSERT INTO `mms_bom_detail` (bom_id,seq,material_id,material_code,material_name,spec_model,unit,usage_qty,loss_rate,is_key_material,supply_type,create_by,create_time)
SELECT b.bom_id,1,m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,0.42,2.0,'1','1','admin',NOW()
FROM mms_bom b, wms_material m WHERE b.bom_no='YH-BOM-001' AND m.material_code='YH-MAT-001';
INSERT INTO `mms_bom_detail` (bom_id,seq,material_id,material_code,material_name,spec_model,unit,usage_qty,loss_rate,is_key_material,supply_type,create_by,create_time)
SELECT b.bom_id,2,m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,1,1.0,'1','1','admin',NOW()
FROM mms_bom b, wms_material m WHERE b.bom_no='YH-BOM-001' AND m.material_code='YH-MAT-020';
INSERT INTO `mms_bom_detail` (bom_id,seq,material_id,material_code,material_name,spec_model,unit,usage_qty,loss_rate,is_key_material,supply_type,create_by,create_time)
SELECT b.bom_id,3,m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,1,1.0,'1','1','admin',NOW()
FROM mms_bom b, wms_material m WHERE b.bom_no='YH-BOM-001' AND m.material_code='YH-MAT-022';
INSERT INTO `mms_bom_detail` (bom_id,seq,material_id,material_code,material_name,spec_model,unit,usage_qty,loss_rate,is_key_material,supply_type,create_by,create_time)
SELECT b.bom_id,4,m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,1,1.0,'0','1','admin',NOW()
FROM mms_bom b, wms_material m WHERE b.bom_no='YH-BOM-001' AND m.material_code='YH-MAT-023';

-- -----------------------------------------------------------------------------
-- 九、检验标准（qms_inspect_std：judge_rule 1区间 2≤上限 3≥下限）
-- -----------------------------------------------------------------------------
INSERT INTO `qms_inspect_std` (std_code,std_name,inspect_method,unit,val_min,val_max,target_val,judge_rule,industry_type,status,del_flag,create_by,create_time) VALUES
('YH-QS-001','波美度（原蜜）','波美计法','°Bé',41.5,NULL,42,'3','food','0','0','admin',NOW()),
('YH-QS-002','果糖和葡萄糖含量','GB 5009.8','g/100g',60,NULL,NULL,'2','food','0','0','admin',NOW()),
('YH-QS-003','嗜渗酵母计数','GB 4789.40平板计数法','CFU/g',NULL,200,NULL,'1','food','0','0','admin',NOW()),
('YH-QS-004','菌落总数','GB 4789.2','CFU/g',NULL,1000,NULL,'1','food','0','0','admin',NOW()),
('YH-QS-005','水分含量','干燥箱法','%',NULL,24,NULL,'1','food','0','0','admin',NOW()),
('YH-QS-006','淀粉酶值（蜂蜜活性）','GB 5009.14','mL/(g·h)',4,NULL,NULL,'3','food','0','0','admin',NOW()),
('YH-QS-007','蜂药残留（氯霉素）','液相色谱-串联质谱法','μg/kg',NULL,0.3,NULL,'1','food','0','0','admin',NOW()),
('YH-QS-008','铅含量','GB 5009.12','mg/kg',NULL,1,NULL,'1','food','0','0','admin',NOW());

-- -----------------------------------------------------------------------------
-- 十、设备（dms_equipment_category / dms_equipment）
-- -----------------------------------------------------------------------------
INSERT INTO `dms_equipment_category` (parent_id,ancestors,category_code,category_name,category_level,order_num,status,del_flag,create_by,create_time) VALUES
(0,'0','YH-DEV-01','生产设备',1,1,'0','0','admin',NOW()),
(0,'0','YH-DEV-02','化验检测设备',1,2,'0','0','admin',NOW()),
(0,'0','YH-DEV-03','养蜂器具',1,3,'0','0','admin',NOW()),
(0,'0','YH-DEV-04','车辆',1,4,'0','0','admin',NOW());
INSERT INTO `dms_equipment` (equipment_code,equipment_name,category_id,model,manufacturer,purchase_date,install_location,dept_id,dept_name,status,del_flag,create_by,create_time)
SELECT 'YH-DEV-001','蜂蜜灌装线',c.category_id,'GZX-2000','灌装设备服务商','2021-06-01','沿江工业园灌装车间',200,'生产部','0','0','admin',NOW()
FROM dms_equipment_category c WHERE c.category_code='YH-DEV-01';
INSERT INTO `dms_equipment` (equipment_code,equipment_name,category_id,model,manufacturer,purchase_date,install_location,dept_id,dept_name,status,del_flag,create_by,create_time)
SELECT 'YH-DEV-002','真空浓缩过滤设备',c.category_id,'GL-500','灌装设备服务商','2021-06-01','沿江工业园加工车间',200,'生产部','0','0','admin',NOW()
FROM dms_equipment_category c WHERE c.category_code='YH-DEV-01';
INSERT INTO `dms_equipment` (equipment_code,equipment_name,category_id,model,manufacturer,purchase_date,install_location,dept_id,dept_name,status,del_flag,create_by,create_time)
SELECT 'YH-DEV-010','电子天平',c.category_id,'FA2004','检测仪器厂','2022-03-01','化验室',101,'质检化验部','0','0','admin',NOW()
FROM dms_equipment_category c WHERE c.category_code='YH-DEV-02';
INSERT INTO `dms_equipment` (equipment_code,equipment_name,category_id,model,manufacturer,purchase_date,install_location,dept_id,dept_name,status,del_flag,create_by,create_time)
SELECT 'YH-DEV-011','波美计（一组）',c.category_id,'BM-1','检测仪器厂','2020-01-01','化验室',101,'质检化验部','0','0','admin',NOW()
FROM dms_equipment_category c WHERE c.category_code='YH-DEV-02';
INSERT INTO `dms_equipment` (equipment_code,equipment_name,category_id,model,manufacturer,purchase_date,install_location,dept_id,dept_name,status,del_flag,create_by,create_time)
SELECT 'YH-DEV-020','蜂箱群（一号场）',c.category_id,'标准十框箱','自制/合作','2015-01-01','一号生态养蜂场',101,'蜂场管理部','0','0','admin',NOW()
FROM dms_equipment_category c WHERE c.category_code='YH-DEV-03';
INSERT INTO `dms_equipment` (equipment_code,equipment_name,category_id,model,manufacturer,purchase_date,install_location,dept_id,dept_name,status,del_flag,create_by,create_time)
SELECT 'YH-DEV-030','货运车',c.category_id,'4.5吨厢式','东风','2019-05-01','厂区',105,'综合管理部','0','0','admin',NOW()
FROM dms_equipment_category c WHERE c.category_code='YH-DEV-04';

-- -----------------------------------------------------------------------------
-- 十一、工厂/车间/产线（mms_factory / mms_workshop / mms_line）
-- -----------------------------------------------------------------------------
INSERT INTO `mms_factory` (factory_code,factory_name,create_by,create_time) VALUES
('YH-CF01','源禾农业沿江工业园厂区','admin',NOW());
INSERT INTO `mms_workshop` (workshop_code,workshop_name,factory_id,create_by,create_time)
SELECT 'YH-W01','灌装包装车间',f.factory_id,'admin',NOW() FROM mms_factory f WHERE f.factory_code='YH-CF01';
INSERT INTO `mms_workshop` (workshop_code,workshop_name,factory_id,create_by,create_time)
SELECT 'YH-W02','过滤加工车间',f.factory_id,'admin',NOW() FROM mms_factory f WHERE f.factory_code='YH-CF01';
INSERT INTO `mms_line` (line_code,line_name,workshop_id,create_by,create_time)
SELECT 'YH-L01','蜂蜜灌装线',w.workshop_id,'admin',NOW() FROM mms_workshop w WHERE w.workshop_code='YH-W01';
INSERT INTO `mms_line` (line_code,line_name,workshop_id,create_by,create_time)
SELECT 'YH-L02','贴标包装线',w.workshop_id,'admin',NOW() FROM mms_workshop w WHERE w.workshop_code='YH-W01';
INSERT INTO `mms_line` (line_code,line_name,workshop_id,create_by,create_time)
SELECT 'YH-L03',CONCAT('过滤浓缩线'),w.workshop_id,'admin',NOW() FROM mms_workshop w WHERE w.workshop_code='YH-W02';

-- -----------------------------------------------------------------------------
-- 十二、库存初始化（wms_inventory：原蜜/成品/包材基础库存）
-- -----------------------------------------------------------------------------
INSERT INTO `wms_inventory` (material_id,warehouse_id,location_id,batch_no,production_date,expiry_date,qty,lock_qty,del_flag,create_by,create_time)
SELECT m.material_id,w.warehouse_id,l.warehouse_id,'YH-2026-1013',DATE_SUB(CURDATE(),INTERVAL 14 DAY),DATE_ADD(CURDATE(),INTERVAL 540 DAY),1200,0,'0','admin',NOW()
FROM wms_material m, wms_warehouse w, wms_warehouse l
WHERE m.material_code='YH-MAT-001' AND w.warehouse_code='YH-WH01' AND l.warehouse_code='YH-WH01A01L01';
INSERT INTO `wms_inventory` (material_id,warehouse_id,location_id,batch_no,production_date,expiry_date,qty,lock_qty,del_flag,create_by,create_time)
SELECT m.material_id,w.warehouse_id,l.warehouse_id,'YH-2026-1020',DATE_SUB(CURDATE(),INTERVAL 7 DAY),DATE_ADD(CURDATE(),INTERVAL 720 DAY),800,0,'0','admin',NOW()
FROM wms_material m, wms_warehouse w, wms_warehouse l
WHERE m.material_code='YH-MAT-010' AND w.warehouse_code='YH-WH02' AND l.warehouse_code='YH-WH02A01L01';
INSERT INTO `wms_inventory` (material_id,warehouse_id,location_id,batch_no,qty,lock_qty,del_flag,create_by,create_time)
SELECT m.material_id,w.warehouse_id,l.warehouse_id,NULL,15000,0,'0','admin',NOW()
FROM wms_material m, wms_warehouse w, wms_warehouse l
WHERE m.material_code='YH-MAT-020' AND w.warehouse_code='YH-WH03' AND l.warehouse_code='YH-WH03A01L01';

-- -----------------------------------------------------------------------------
-- 十三、安全生产（biz_safety_risk_point / biz_safety_worker）
-- -----------------------------------------------------------------------------
INSERT INTO `biz_safety_risk_point` (risk_code,risk_name,hazard_desc,accident_type,risk_level,area_name,dept_id,measure_engine,measure_manage,check_standard,check_frequency,status,del_flag,create_by,create_time)
SELECT 'YH-AQ-001','灌装车间电气设备','电气线路老化可能引发火灾触电','fire','1','灌装包装车间',200,'线路穿管敷设，设备接地','每月电气安全检查，配齐灭火器材','电气线路无破损老化，接地可靠','每月','0','0','admin',NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_safety_risk_point WHERE risk_code='YH-AQ-001');

-- -----------------------------------------------------------------------------
-- 十四、公告（sys_notice：公司级通知示例）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_notice` (notice_title,notice_type,notice_content,status,create_by,create_time,remark) VALUES
('曲靖源禾农业数字化运营平台上线通知','1','平台已完成初始化部署，覆盖设备管理、安全生产、产品设计、质量管理、生产管理、协同办公六大模块，请各部门及时登录使用。','0','admin',NOW(),'平台上线通知'),
('采蜜季生产计划安排','2','各养蜂场按统一迁移、统一放蜂、统一采蜜要求执行秋季采蜜计划，原蜜入厂须附批次记录并经化验室检验合格后入库。','0','admin',NOW(),'生产通知'),
('食品安全生产自查要求','2','按SC许可要求开展车间卫生自查，重点关注嗜渗酵母控制与灌装环节卫生，检查记录于平台留档。','0','admin',NOW(),'质量通知');

-- -----------------------------------------------------------------------------
-- 十五、模块开关：注册 pdm 模块并启用（产品设计）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_module` (`module_name`,`module_key`,`root_menu_id`,`status`,`is_core`,`order_num`,`create_by`,`remark`)
SELECT '产品设计','pdm',11000,'0','0',16,'admin','产品设计（PDM）模块'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_module` WHERE `module_key`='pdm');

-- 门户首页：admin 布局改为 YHNY 实际启用模块（含 pdm，剔除停用的 bid/fin/flow/hr/safety/dms）
UPDATE `sys_user_home_layout` SET module_codes='/system,/mms,/qms,/pms,/wms,/pdm,/mk,/ai', update_by='admin', update_time=NOW() WHERE user_id=1;

-- =============================================================================
-- 回滚说明（手工按需执行）：
-- 1) 业务数据回滚：重新 TRUNCATE 对应业务表后重跑本脚本
-- 2) 删除本脚本新增数据：
--    DELETE FROM wms_warehouse WHERE warehouse_code LIKE 'YH-%';
--    DELETE FROM wms_material  WHERE material_code LIKE 'YH-%';
--    DELETE FROM wms_supplier  WHERE supplier_code LIKE 'YH-%';
--    DELETE FROM mk_customer   WHERE customer_no LIKE 'YH-%';
--    DELETE FROM mk_lead       WHERE lead_no LIKE 'YH-%';
--    DELETE FROM mk_channel    WHERE account_name='曲靖源禾农业店';
--    DELETE FROM pdm_product   WHERE product_code LIKE 'YH-%';
--    DELETE FROM pdm_product_category WHERE category_code LIKE 'YH-%';
--    DELETE FROM mms_bom       WHERE bom_no='YH-BOM-001';
--    DELETE FROM mms_bom_detail WHERE bom_id NOT IN (SELECT bom_id FROM (SELECT bom_id FROM mms_bom) t);
--    DELETE FROM qms_inspect_std WHERE std_code LIKE 'YH-%';
--    DELETE FROM dms_equipment_category WHERE category_code LIKE 'YH-%';
--    DELETE FROM dms_equipment WHERE equipment_code LIKE 'YH-%';
--    DELETE FROM mms_factory WHERE factory_code='YH-CF01';
--    DELETE FROM mms_workshop WHERE workshop_code LIKE 'YH-%';
--    DELETE FROM mms_line WHERE line_code LIKE 'YH-%';
--    DELETE FROM wms_inventory WHERE batch_no LIKE 'YH-%';
--    DELETE FROM sys_notice WHERE notice_title IN ('曲靖源禾农业数字化运营平台上线通知','采蜜季生产计划安排','食品安全生产自查要求');
--    DELETE FROM sys_module WHERE module_key='pdm';
--    组织架构回滚：将 sys_dept/sys_post 各 UPDATE 字段恢复为原值（原值备份见执行前 SELECT 导出）
-- =============================================================================

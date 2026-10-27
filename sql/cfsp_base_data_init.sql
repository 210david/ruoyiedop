-- =====================================================================
-- 传丰食品 CFSP 业务基础数据初始化
-- 按食品(高原食品)行业特性，每块业务新增 10 条：
--   1. 物料主数据 wms_material (MAT012~MAT021)
--   2. 仓库地点 wms_warehouse (3库+4库区+3库位 树形)
--   3. 客户数据 mk_customer (KH202610150002~0011)
--   4. 供应商数据 wms_supplier (SUP0001~0010)
--   5. 质量标准 qms_inspect_std (ZLBZ001~010)
--   6. 工序 mms_process (乳品RP01~06 / 灌肠GC01~04)
--   7. BOM mms_bom + mms_bom_detail (BOM20260001~0010)
-- 附：新增成品 food_material_param 参数、编号规则 current_seq 同步
-- 执行库：RUOYI_CFSP
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 物料主数据（10条）：4原材料 + 2辅料添加剂 + 2包材 + 2成品
-- unit 取 wms_unit 字典值：3=kg 5=米 24=卷 0=个 20=盒
-- ---------------------------------------------------------------------
INSERT INTO wms_material
(material_code, material_name, material_type, spec_model, unit, barcode, safety_stock_min, safety_stock_max,
 is_batch_manage, is_expiry_manage, shelf_life_days, expiry_alert_days, status, del_flag, create_by, create_time, remark)
VALUES
('MAT012','白砂糖','0','一级白砂糖 50kg/袋','3','6901234500127',500,5000,'1','1',730,90,'0','0','admin',NOW(),'碳酸饮料/乳品/肉制品通用甜味原料，干燥通风储存'),
('MAT013','食用盐','0','精制加碘盐 50kg/袋','3','6901234500134',200,2000,'1','1',1095,180,'0','0','admin',NOW(),'腌制工序核心辅料，GB/T 5461'),
('MAT014','天然猪肠衣','0','盐渍肠衣 直径28-32mm','5','6901234500141',1000,10000,'1','1',180,30,'0','0','admin',NOW(),'火腿肠灌制用天然肠衣，需冷藏盐渍保存'),
('MAT015','猪分割肉','0','冷鲜四号肉（后腿肌肉）','3','6901234500158',500,3000,'1','1',3,1,'0','0','admin',NOW(),'火腿肠主原料，0~4℃冷链收货，进厂需索证索票'),
('MAT016','复合磷酸盐','3','食品级保水剂 25kg/桶','3','6901234500165',20,200,'1','1',1095,180,'0','0','admin',NOW(),'食品添加剂，按GB 2760限量使用，专柜上锁管理'),
('MAT017','异抗坏血酸钠','3','食品级护色剂 25kg/桶','3','6901234500172',20,200,'1','1',730,90,'0','0','admin',NOW(),'护色抗氧化剂，按GB 2760限量使用'),
('MAT018','PVDC包装膜','3','食品级热收缩膜 500m/卷','24','6901234500189',50,500,'0','0',NULL,30,'0','0','admin',NOW(),'直接接触食品的包装材料，需供应商提供QS/SC资质'),
('MAT019','瓦楞纸箱','3','五层加硬 40×30×20cm','0','6901234500196',500,5000,'0','0',NULL,60,'0','0','admin',NOW(),'外包装箱，印刷需含SC证号/生产日期喷码区'),
('MAT020','巴氏杀菌乳','2','巴氏鲜奶 250ml/盒','20','6901234500202',200,2000,'1','1',7,1,'0','0','admin',NOW(),'乳品车间产成品，2~6℃全程冷链，货架期7天'),
('MAT021','原味酸奶','2','发酵乳 100g/盒','20','6901234500219',200,2000,'1','1',21,3,'0','0','admin',NOW(),'乳品车间产成品，2~6℃冷链，货架期21天');

-- ---------------------------------------------------------------------
-- 2. 仓库地点（10条）：3仓库 + 4库区 + 3库位（树形，ancestors按sys_dept风格）
-- node_type: 1=仓库 2=库区 3=库位; area_type: 0存储 1收货 2发货; location_type: 0货架位 1地堆位
-- ---------------------------------------------------------------------
INSERT INTO wms_warehouse
(parent_id, ancestors, node_type, warehouse_code, warehouse_name, address, area_type, capacity, location_status, status, del_flag, create_by, create_time, remark)
VALUES
(0,'0','1','WH02','原料冷藏库','云南省曲靖市经济技术开发区传丰食品产业园1号库',NULL,0,'0','0','0','admin',NOW(),'生鲜原料低温库（0~4℃），含收货暂存与冷藏存储'),
(0,'0','1','WH03','成品常温仓','云南省曲靖市经济技术开发区传丰食品产业园2号库',NULL,0,'0','0','0','admin',NOW(),'乳品/肉制品成品常温周转库，直连发货月台'),
(0,'0','1','WH04','包材辅料仓','云南省曲靖市经济技术开发区传丰食品产业园3号库',NULL,0,'0','0','0','admin',NOW(),'包装材料及食品添加剂存放，添加剂专区上锁管理');

SET @wh02 := LAST_INSERT_ID();
SET @wh03 := @wh02 + 1;
SET @wh04 := @wh02 + 2;

INSERT INTO wms_warehouse
(parent_id, ancestors, node_type, warehouse_code, warehouse_name, area_type, capacity, location_status, status, del_flag, create_by, create_time, remark)
VALUES
(@wh02, CONCAT('0,',@wh02),'2','WH02-01','冷藏存储区','0',0,'0','0','0','admin',NOW(),'0~4℃冷藏品存储区，温湿度连续监控'),
(@wh02, CONCAT('0,',@wh02),'2','WH02-02','收货暂存区','1',0,'0','0','0','admin',NOW(),'原料到货验收/感官检查/索证索票暂存'),
(@wh03, CONCAT('0,',@wh03),'2','WH03-01','成品存储区','0',0,'0','0','0','admin',NOW(),'成品按批次分区码放，先进先出'),
(@wh03, CONCAT('0,',@wh03),'2','WH03-02','成品发货区','2',0,'0','0','0','admin',NOW(),'发货月台缓冲区，按订单波次拣货复核');

SET @kq01 := @wh02 + 3;
SET @kq03 := @wh02 + 5;

INSERT INTO wms_warehouse
(parent_id, ancestors, node_type, warehouse_code, warehouse_name, location_type, capacity, location_status, status, del_flag, create_by, create_time, remark)
VALUES
(@kq01, CONCAT('0,',@wh02,',',@kq01),'3','WH02-01-A01','冷藏货架位A01','0',500,'0','0','0','admin',NOW(),'货架位，限存放批检验合格的原料肉'),
(@kq01, CONCAT('0,',@wh02,',',@kq01),'3','WH02-01-A02','冷藏货架位A02','0',500,'0','0','0','admin',NOW(),'货架位，限存放批检验合格的原料肉'),
(@kq03, CONCAT('0,',@wh03,',',@kq03),'3','WH03-01-B01','常温地堆位B01','1',1000,'0','0','0','admin',NOW(),'地堆位，成品整托存放，≤5层码放');

-- ---------------------------------------------------------------------
-- 3. 客户数据（10条）：商超/经销/电商/餐饮/团餐（mk_customer）
-- 编号按规则22：KH+yyyyMMdd+4位（今日起始0002）
-- ---------------------------------------------------------------------
INSERT INTO mk_customer
(customer_no, customer_name, credit_code, customer_level, customer_source, industry, company_size,
 province, city, district, address, customer_status, first_contact_date, cooperation_date,
 total_amount, credit_limit, credit_days, credit_status, del_flag, create_by, create_time, remark)
VALUES
('KH202610150002','昆明鲜达商贸有限公司','91530100MA6K2F10XA','2','2','食品经销','50-99人','云南省','昆明市','官渡区','昆明市官渡区昌宏路东盟商贸港12栋','2','2025-03-12','2025-04-01',2860000.00,500000.00,30,'0','0','admin',NOW(),'核心地市经销商，代理火腿肠/酸奶全品系'),
('KH202610150003','沃尔玛（云南）商业零售有限公司','91530100MA6K3G20YB','1','1','商超零售','500人以上','云南省','昆明市','西山区','昆明市西山区前福路沃尔玛山姆会员店配送中心','2','2024-11-20','2025-01-05',8650000.00,1000000.00,45,'0','0','admin',NOW(),'KA商超，月度对账，需提供检验报告随货同行'),
('KH202610150004','盒马鲜生昆明区域采购中心','91530112MA6K4H30ZC','1','0','商超零售','100-499人','云南省','昆明市','盘龙区','昆明市盘龙区北京路欣都龙城3栋','2','2025-06-15','2025-07-01',4320000.00,800000.00,30,'0','0','admin',NOW(),'新零售渠道，冷链直配，日订单预测协同'),
('KH202610150005','贵阳惠民食品经销部','91520103MA6K5J40XD','3','2','食品经销','10-49人','贵州省','贵阳市','南明区','贵阳市南明区花果园食品批发市场B区28号','2','2025-08-10','2025-09-01',980000.00,200000.00,30,'0','0','admin',NOW(),'省外经销商，跨省冷链运输需签温控协议'),
('KH202610150006','四川川香餐饮管理有限公司','91510100MA6K6K50XE','2','1','餐饮服务','100-499人','四川省','成都市','武侯区','成都市武侯区天府三街新希望大厦B座','2','2025-05-20','2025-06-10',1540000.00,300000.00,45,'0','0','admin',NOW(),'连锁餐饮客户，供应分割肉及火腿原料'),
('KH202610150007','云南农垦供应链管理有限公司','91530000MA6K7L60XF','2','3','团餐供应','100-499人','云南省','昆明市','呈贡区','昆明市呈贡区春融街农垦大厦','2','2024-12-01','2025-01-15',3200000.00,500000.00,60,'0','0','admin',NOW(),'机构团餐集采平台，季度框架协议'),
('KH202610150008','京东超市西南仓','91530100MA6K8M70XG','1','0','电商平台','500人以上','云南省','昆明市','晋宁区','昆明市晋宁区宝峰工业园区京东云南仓','2','2025-02-14','2025-03-01',5760000.00,800000.00,30,'0','0','admin',NOW(),'电商渠道，入仓需贴仓单并同步效期数据'),
('KH202610150009','昆明学院后勤服务中心','91530100MA6K9N80XH','3','5','学校食堂','100-499人','云南省','昆明市','呈贡区','昆明市呈贡区大学城昆明学院后勤楼','2','2025-09-01','2025-09-15',420000.00,100000.00,15,'0','0','admin',NOW(),'高校食堂供应商，需提供批批检验合格证明'),
('KH202610150010','玉溪百信商贸有限公司','91530400MA6KA90XJ','3','2','食品经销','10-49人','云南省','玉溪市','红塔区','玉溪市红塔区南北大街商贸城6号','1','2026-09-28','2026-10-10',68000.00,150000.00,30,'0','0','admin',NOW(),'新签约经销商，首单试单合作'),
('KH202610150011','拼多多生鲜云贵运营中心','91530100MA6KB00XK','4','0','电商平台','500人以上','云南省','昆明市','官渡区','昆明市官渡区世纪城物流园区','1','2026-10-08','2026-10-12',32000.00,100000.00,15,'0','0','admin',NOW(),'潜在电商渠道，正在洽谈百亿补贴专供装');

-- ---------------------------------------------------------------------
-- 4. 供应商数据（10条）：原料/添加剂/包材/服务/设备（wms_supplier）
-- 编号按规则6：SUP+4位
-- ---------------------------------------------------------------------
INSERT INTO wms_supplier
(supplier_code, supplier_name, supplier_short_name, supplier_type, supplier_level, unified_credit_code,
 contact_person, contact_phone, email, address, legal_person, registered_capital, enterprise_nature, business_scope,
 established_date, bank_name, bank_account, tax_number, payment_method, payment_days, settlement_type, currency,
 delivery_cycle, min_order_amount, cooperation_date, qualification_status, status, del_flag, create_by, create_time, reeval_cycle_months, remark)
VALUES
('SUP0001','曲靖恒泰牧业有限公司','恒泰牧业','0','1','91530300MA6L1A001A','李国强','13887123401','liqg@hengtai-muye.cn','云南省曲靖市沾益区龙华街道畜牧产业园','李国强',20000000.00,'1','牲畜饲养销售；冷鲜肉分割、仓储及运输',DATE'2012-03-15','中国工商银行曲靖沾益支行','2510008109200112345','91530300MA6L1A001A','1',30,'0','CNY',2,20000.00,DATE'2023-05-10','1','0','0','admin',NOW(),12,'冷鲜四号肉主供应商，屠宰-分割一体化，提供动物检疫合格证明'),
('SUP0002','嵩明牧原奶牛养殖专业合作社','嵩明牧原','0','1','91530127MA6L2B002B','张世平','13887123402','zsp@songmingmy.com','云南省昆明市嵩明县小街镇牧原养殖基地','张世平',8000000.00,'5','奶牛养殖；生鲜乳收购与销售',DATE'2015-08-20','中国农业银行嵩明县支行','2406500104002212346','91530127MA6L2B002B','1',15,'0','CNY',1,10000.00,DATE'2024-01-08','1','0','0','admin',NOW(),12,'生鲜乳供应商，双证齐全（生乳检验单+运输温度记录），日供鲜奶8吨'),
('SUP0003','云南盐业曲靖有限公司','云南盐业','0','0','91530300MA6L3C003C','王志敏','13887123403','wzm@yunnan-salt.cn','云南省曲靖市麒麟区三江大道盐业大厦','王志敏',50000000.00,'0','食盐、工业盐批发；食品添加剂经营',DATE'2008-06-01','中国建设银行曲靖市分行','5300161863605112347','91530300MA6L3C003C','1',30,'0','CNY',3,5000.00,DATE'2022-03-18','1','0','0','admin',NOW(),12,'食用盐定点批发企业，粤盐/云盐品牌授权'),
('SUP0004','德宏康丰糖业有限公司','康丰糖业','0','2','91533100MA6L4D004D','段丽华','13887123404','dlh@kangfeng-sugar.com','云南省德宏州陇川县章凤镇糖业园区','段丽华',30000000.00,'1','甘蔗种植；白砂糖、赤砂糖生产销售',DATE'2010-11-25','中国工商银行德宏州分行','2512000109203312348','91533100MA6L4D004D','1',45,'0','CNY',5,30000.00,DATE'2023-09-20','1','0','0','admin',NOW(),12,'一级白砂糖供应商，批批附糖度与二氧化硫检测报告'),
('SUP0005','江苏如皋神肠衣业有限公司','如皋肠衣','0','2','91320682MA6L5E005E','沈建飞','13905123405','shjf@rugao-changyi.cn','江苏省如皋市经济开发区肠衣产业园3号','沈建飞',15000000.00,'1','盐渍肠衣、干制肠衣加工销售；畜产品收购',DATE'2009-04-12','中国银行如皋支行','4866512345678912349','91320682MA6L5E005E','1',45,'0','CNY',7,20000.00,DATE'2024-06-15','1','0','0','admin',NOW(),12,'天然猪肠衣供应商，出口级加工工艺，批附检疫与盐渍浓度报告'),
('SUP0006','郑州瑞康食品添加剂商贸有限公司','瑞康添加剂','3','2','91410100MA6L6F006F','赵鹏飞','13903712346','zpf@ruikang-add.com','河南省郑州市二七区食品添加剂专业市场A区12号','赵鹏飞',5000000.00,'1','食品添加剂（复合磷酸盐、护色剂等）销售',DATE'2014-07-30','中国建设银行郑州二七支行','4100152303605112350','91410100MA6L6F006F','1',30,'0','CNY',4,5000.00,DATE'2024-03-05','1','0','0','admin',NOW(),6,'食品添加剂指定供应商，逐批提供生产许可与检验报告，双人验收'),
('SUP0007','昆明彩印包装有限公司','昆明彩印','0','2','91530100MA6L7G007G','杨春梅','13888123407','ycm@km-packaging.cn','云南省昆明市嵩明杨林经济技术开发区包装园','杨春梅',12000000.00,'1','包装装潢印刷品印刷；瓦楞纸箱、彩盒制造',DATE'2011-09-16','中国农业银行昆明杨林支行','2403010104001212351','91530100MA6L7G007G','1',30,'0','CNY',5,10000.00,DATE'2022-11-20','1','0','0','admin',NOW(),12,'瓦楞纸箱供应商，印刷含SC证号版面，包材入库前抽检抗压强度'),
('SUP0008','浙江华塑功能膜业有限公司','华塑膜业','0','2','91330300MA6L8H008H','陈立群','13905712348','clq@huasu-film.com','浙江省温州市苍南县包装工业园区纬三路8号','陈立群',18000000.00,'4','PVDC复合膜、收缩膜生产销售；食品用塑料包装制品',DATE'2013-05-22','中国银行温州苍南支行','3789651234567812352','91330300MA6L8H008H','2',60,'1','CNY',7,50000.00,DATE'2023-04-10','1','0','0','admin',NOW(),12,'PVDC热收缩膜供应商，提供食品接触材料检测报告（GB 4806.7）'),
('SUP0009','云南鼎盛冷链物流有限公司','鼎盛冷链','2','1','91530100MA6L9I009I','马建波','13887123409','mjb@dingsheng-cold.cn','云南省昆明市官渡区空港经济区物流大道9号','马建波',25000000.00,'2','道路货物运输（冷链）；仓储服务（不含危化品）',DATE'2016-03-08','中国工商银行昆明空港支行','2516008109207712353','91530100MA6L9I009I','1',30,'0','CNY',1,0.00,DATE'2024-02-01','1','0','0','admin',NOW(),12,'冷链干线及城市配送服务商，全程温控记录可追溯，月度KPI考核'),
('SUP0010','山东天瑞食品机械有限公司','天瑞机械','1','2','91370100MA6LAJ010J','刘天成','13905312340','ltc@tianrui-mach.com','山东省济南市章丘区明水经济技术开发区机械园','刘天成',35000000.00,'2','食品加工机械设备制造、安装、维修；机械配件销售',DATE'2005-10-09','中国农业银行章丘支行','1553010104002912354','91370100MA6LAJ010J','2',60,'0','CNY',15,100000.00,DATE'2025-01-20','1','0','0','admin',NOW(),12,'灌肠/杀菌设备供应商，提供安装调试与一年质保，异地备件48小时到场');

-- ---------------------------------------------------------------------
-- 5. 质量标准（10条，qms_inspect_std）：肉制品+乳品检验标准
-- judge_rule: 1区间 2单值 3定性; industry_type 5=高原食品
-- ---------------------------------------------------------------------
INSERT INTO qms_inspect_std
(std_code, std_name, inspect_method, unit, val_min, val_max, target_val, judge_rule, industry_type, status, del_flag, create_by, create_time, remark)
VALUES
('ZLBZ001','蛋白质含量标准','GB 5009.5-2016 凯氏定氮法','%',10.0000,NULL,12.0000,'1','5','0','0','admin',NOW(),'火腿肠类蛋白质含量≥10%（GB/T 20712），内控目标12%'),
('ZLBZ002','水分含量标准','GB 5009.3-2016 直接干燥法','%',60.0000,75.0000,68.0000,'1','5','0','0','admin',NOW(),'火腿肠水分含量区间控制（60%~75%）'),
('ZLBZ003','亚硝酸盐残留标准','GB 5009.33-2016 分光光度法','mg/kg',NULL,30.0000,15.0000,'1','5','0','0','admin',NOW(),'腌腊肉制品亚硝酸盐残留≤30mg/kg（GB 2760），CCP关键限值'),
('ZLBZ004','菌落总数标准','GB 4789.2-2022 平板计数法','CFU/g',NULL,10000.0000,5000.0000,'1','5','0','0','admin',NOW(),'熟肉制品菌落总数 n=5 c=2 m=10⁴ M=10⁵，按m值内控'),
('ZLBZ005','大肠菌群标准','GB 4789.3-2016 MPN计数法','CFU/g',NULL,100.0000,10.0000,'1','5','0','0','admin',NOW(),'熟肉制品大肠菌群 n=5 c=2 m=10 M=100，按m值内控'),
('ZLBZ006','沙门氏菌检验标准','GB 4789.4-2016 生化鉴定法',NULL,NULL,NULL,0.0000,'3','5','0','0','admin',NOW(),'致病菌定性判定：0=未检出（合格），1=检出（不合格），n=5 c=0'),
('ZLBZ007','过氧化值标准','GB 5009.227-2016 滴定法','g/100g',NULL,0.5000,0.2000,'1','5','0','0','admin',NOW(),'腌腊肉制品过氧化值≤0.5g/100g，监控原料脂肪氧化'),
('ZLBZ008','酸价标准','GB 5009.229-2016 电位滴定法','mgKOH/g',NULL,4.0000,2.0000,'1','5','0','0','admin',NOW(),'腌腊肉制品酸价≤4.0mgKOH/g'),
('ZLBZ009','盐分（NaCl）标准','GB 5009.44-2016 火腿盐分测定','%',5.0000,8.0000,6.5000,'1','5','0','0','admin',NOW(),'干腌火腿盐分区间5%~8%，影响发酵成熟与风味'),
('ZLBZ010','非脂乳固体标准','GB 5413.39-2016 重量法','%',8.1000,NULL,8.5000,'1','5','0','0','admin',NOW(),'巴氏杀菌乳非脂乳固体≥8.1%（GB 19645）');

-- ---------------------------------------------------------------------
-- 6. 工序（10条，mms_process）：乳品线RP01~06 + 灌肠线GC01~04
-- process_type: 0加工 2检验 3包装; std_time单位小时
-- ---------------------------------------------------------------------
INSERT INTO mms_process
(process_code, process_name, process_type, std_time, prep_time, is_key_process, is_outsource, workshop_name, line_name, status, del_flag, create_by, create_time, remark)
VALUES
('RP01','生乳验收','2',2.00,0.50,'1','0','乳品车间','收奶线','0','0','admin',NOW(),'CCP1：抗生素/体细胞/微生物批批检验，合格方可过磅收奶'),
('RP02','净乳过滤','0',1.00,0.50,'0','0','乳品车间','预处理线','0','0','admin',NOW(),'离心净乳+双联过滤，去除杂质与体细胞残留'),
('RP03','标准化配料','0',1.50,0.50,'0','0','乳品车间','配料罐','0','0','admin',NOW(),'脂肪/蛋白标准化至目标值，配料记录双人复核'),
('RP04','巴氏杀菌','0',1.00,0.50,'1','0','乳品车间','杀菌线','0','0','admin',NOW(),'CCP2：85℃/15s巴氏杀菌，温度-时间自动记录并联锁'),
('RP05','无菌灌装','3',2.00,1.00,'1','0','乳品车间','灌装线','0','0','admin',NOW(),'CCP3：灌装间洁净度十万级，封口完整性在线抽检'),
('RP06','冷藏暂存','0',8.00,0.00,'0','0','乳品车间','成品暂存间','0','0','admin',NOW(),'2~6℃冷藏暂存，等待批检验合格后入库'),
('GC01','绞肉制馅','0',2.00,0.50,'1','0','灌肠车间','制馅线','0','0','admin',NOW(),'CCP1：金属探测+原料温度≤8℃，馅温≤12℃'),
('GC02','滚揉腌制','0',12.00,0.50,'0','0','灌肠车间','滚揉间','0','0','admin',NOW(),'真空滚揉间歇腌制，滚揉比按工艺卡执行'),
('GC03','灌结成型','0',3.00,0.50,'0','0','灌肠车间','灌装线','0','0','admin',NOW(),'天然肠衣灌制，扭结节长按规格自动控制'),
('GC04','蒸煮杀菌','0',1.50,0.50,'1','0','灌肠车间','杀菌釜','0','0','admin',NOW(),'CCP2：121℃高压蒸煮杀菌，F值达标自动放行');

-- ---------------------------------------------------------------------
-- 7. BOM（10条主档 + 明细，mms_bom / mms_bom_detail）
-- bom_type: 0标准 1替代 2配方 3临时; status: 0草稿 1已发布
-- ---------------------------------------------------------------------
SET @m_meat   := (SELECT material_id FROM wms_material WHERE material_code='MAT015' AND del_flag='0');
SET @m_sugar  := (SELECT material_id FROM wms_material WHERE material_code='MAT012' AND del_flag='0');
SET @m_salt   := (SELECT material_id FROM wms_material WHERE material_code='MAT013' AND del_flag='0');
SET @m_casing := (SELECT material_id FROM wms_material WHERE material_code='MAT014' AND del_flag='0');
SET @m_phos   := (SELECT material_id FROM wms_material WHERE material_code='MAT016' AND del_flag='0');
SET @m_vitc   := (SELECT material_id FROM wms_material WHERE material_code='MAT017' AND del_flag='0');
SET @m_film   := (SELECT material_id FROM wms_material WHERE material_code='MAT018' AND del_flag='0');
SET @m_box    := (SELECT material_id FROM wms_material WHERE material_code='MAT019' AND del_flag='0');
SET @m_ham    := (SELECT material_id FROM wms_material WHERE material_code='MAT011' AND del_flag='0');
SET @m_leg    := (SELECT material_id FROM wms_material WHERE material_code='MAT010' AND del_flag='0');
SET @m_milk   := (SELECT material_id FROM wms_material WHERE material_code='MAT001' AND del_flag='0');
SET @m_saus   := (SELECT material_id FROM wms_material WHERE material_code='MAT002' AND del_flag='0');

-- BOM1 火腿肠标准BOM（100支/箱）
INSERT INTO mms_bom (bom_no, bom_name, product_id, product_code, product_name, bom_type, version, base_qty, base_unit, status, effective_date, remark, del_flag, create_by, create_time)
VALUES ('BOM20260001','火腿肠标准BOM（100支/箱）',@m_saus,'MAT002','火腿肠',0,'1.0',100.000000,'2','1',DATE'2026-10-15','火腿肠40g/支标准配方，按箱（100支）投料','0','admin',NOW());
SET @b := LAST_INSERT_ID();
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark) VALUES
(@b,1,@m_meat,'MAT015','猪分割肉','冷鲜四号肉（后腿肌肉）','3',7.500000,2.00,'1','1','0','0','admin',NOW(),'主原料，绞肉制馅投料'),
(@b,2,@m_sugar,'MAT012','白砂糖','一级白砂糖 50kg/袋','3',0.300000,0.00,'0','2','0','0','admin',NOW(),'辅料，倒冲扣料'),
(@b,3,@m_salt,'MAT013','食用盐','精制加碘盐 50kg/袋','3',0.450000,0.00,'0','2','0','0','admin',NOW(),'辅料，倒冲扣料'),
(@b,4,@m_phos,'MAT016','复合磷酸盐','食品级保水剂 25kg/桶','3',0.100000,0.00,'1','2','0','0','admin',NOW(),'添加剂，GB 2760限量，双人复核称量'),
(@b,5,@m_vitc,'MAT017','异抗坏血酸钠','食品级护色剂 25kg/桶','3',0.050000,0.00,'0','2','0','0','admin',NOW(),'护色剂，倒冲扣料'),
(@b,6,@m_casing,'MAT014','天然猪肠衣','盐渍肠衣 直径28-32mm','5',100.000000,3.00,'1','1','0','0','admin',NOW(),'灌制直接领料，按节长损耗'),
(@b,7,@m_film,'MAT018','PVDC包装膜','食品级热收缩膜 500m/卷','24',0.500000,1.00,'0','1','0','0','admin',NOW(),'包装直接领料'),
(@b,8,@m_box,'MAT019','瓦楞纸箱','五层加硬 40×30×20cm','0',1.000000,0.00,'0','1','0','0','admin',NOW(),'外箱直接领料');

-- BOM2 火腿肠替代BOM（PVDC直充版）
INSERT INTO mms_bom (bom_no, bom_name, product_id, product_code, product_name, bom_type, version, base_qty, base_unit, status, effective_date, remark, del_flag, create_by, create_time)
VALUES ('BOM20260002','火腿肠替代BOM（PVDC直充版）',@m_saus,'MAT002','火腿肠',1,'1.0',100.000000,'2','1',DATE'2026-10-15','天然肠衣缺货时启用PVDC膜直充灌装替代方案','0','admin',NOW());
SET @b := LAST_INSERT_ID();
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark) VALUES
(@b,1,@m_meat,'MAT015','猪分割肉','冷鲜四号肉（后腿肌肉）','3',7.500000,2.00,'1','1','0','0','admin',NOW(),'与标准BOM一致'),
(@b,2,@m_sugar,'MAT012','白砂糖','一级白砂糖 50kg/袋','3',0.300000,0.00,'0','2','0','0','admin',NOW(),'倒冲扣料'),
(@b,3,@m_salt,'MAT013','食用盐','精制加碘盐 50kg/袋','3',0.450000,0.00,'0','2','0','0','admin',NOW(),'倒冲扣料'),
(@b,4,@m_phos,'MAT016','复合磷酸盐','食品级保水剂 25kg/桶','3',0.100000,0.00,'1','2','0','0','admin',NOW(),'限量添加剂'),
(@b,5,@m_vitc,'MAT017','异抗坏血酸钠','食品级护色剂 25kg/桶','3',0.050000,0.00,'0','2','0','0','admin',NOW(),'护色剂'),
(@b,6,@m_film,'MAT018','PVDC包装膜','食品级热收缩膜 500m/卷','24',1.200000,1.00,'1','1','0','0','admin',NOW(),'替代天然肠衣直充灌装，用量上调'),
(@b,7,@m_box,'MAT019','瓦楞纸箱','五层加硬 40×30×20cm','0',1.000000,0.00,'0','1','0','0','admin',NOW(),'外箱');

-- BOM3 火腿肠低盐配方BOM
INSERT INTO mms_bom (bom_no, bom_name, product_id, product_code, product_name, bom_type, version, base_qty, base_unit, status, effective_date, remark, del_flag, create_by, create_time)
VALUES ('BOM20260003','火腿肠低盐配方BOM（100支/箱）',@m_saus,'MAT002','火腿肠',2,'1.1',100.000000,'2','1',DATE'2026-10-15','儿童装低盐配方：食盐下调33%，白砂糖微调','0','admin',NOW());
SET @b := LAST_INSERT_ID();
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark) VALUES
(@b,1,@m_meat,'MAT015','猪分割肉','冷鲜四号肉（后腿肌肉）','3',7.500000,2.00,'1','1','0','0','admin',NOW(),'主原料'),
(@b,2,@m_sugar,'MAT012','白砂糖','一级白砂糖 50kg/袋','3',0.350000,0.00,'0','2','0','0','admin',NOW(),'低盐版糖量微调'),
(@b,3,@m_salt,'MAT013','食用盐','精制加碘盐 50kg/袋','3',0.300000,0.00,'0','2','0','0','admin',NOW(),'低盐版食盐下调'),
(@b,4,@m_phos,'MAT016','复合磷酸盐','食品级保水剂 25kg/桶','3',0.100000,0.00,'1','2','0','0','admin',NOW(),'限量添加剂'),
(@b,5,@m_vitc,'MAT017','异抗坏血酸钠','食品级护色剂 25kg/桶','3',0.050000,0.00,'0','2','0','0','admin',NOW(),'护色剂'),
(@b,6,@m_casing,'MAT014','天然猪肠衣','盐渍肠衣 直径28-32mm','5',100.000000,3.00,'1','1','0','0','admin',NOW(),'灌制领料'),
(@b,7,@m_film,'MAT018','PVDC包装膜','食品级热收缩膜 500m/卷','24',0.500000,1.00,'0','1','0','0','admin',NOW(),'包装领料'),
(@b,8,@m_box,'MAT019','瓦楞纸箱','五层加硬 40×30×20cm','0',1.000000,0.00,'0','1','0','0','admin',NOW(),'外箱');

-- BOM4 火腿肠试制临时BOM
INSERT INTO mms_bom (bom_no, bom_name, product_id, product_code, product_name, bom_type, version, base_qty, base_unit, status, effective_date, expire_date, remark, del_flag, create_by, create_time)
VALUES ('BOM20260004','火腿肠芝士风味试制临时BOM',@m_saus,'MAT002','火腿肠',3,'0.1',100.000000,'2','0',DATE'2026-10-15',DATE'2026-11-15','新品试制车间小批量打样，试制期一个月','0','admin',NOW());
SET @b := LAST_INSERT_ID();
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark) VALUES
(@b,1,@m_meat,'MAT015','猪分割肉','冷鲜四号肉（后腿肌肉）','3',7.200000,2.00,'1','1','0','0','admin',NOW(),'主原料'),
(@b,2,@m_sugar,'MAT012','白砂糖','一级白砂糖 50kg/袋','3',0.280000,0.00,'0','2','0','0','admin',NOW(),'辅料'),
(@b,3,@m_salt,'MAT013','食用盐','精制加碘盐 50kg/袋','3',0.420000,0.00,'0','2','0','0','admin',NOW(),'辅料'),
(@b,4,@m_phos,'MAT016','复合磷酸盐','食品级保水剂 25kg/桶','3',0.100000,0.00,'1','2','0','0','admin',NOW(),'限量添加剂'),
(@b,5,@m_casing,'MAT014','天然猪肠衣','盐渍肠衣 直径28-32mm','5',100.000000,3.00,'1','1','0','0','admin',NOW(),'灌制领料'),
(@b,6,@m_film,'MAT018','PVDC包装膜','食品级热收缩膜 500m/卷','24',0.500000,1.00,'0','1','0','0','admin',NOW(),'包装领料'),
(@b,7,@m_box,'MAT019','瓦楞纸箱','五层加硬 40×30×20cm','0',1.000000,0.00,'0','1','0','0','admin',NOW(),'外箱');

-- BOM5 干腌火腿标准BOM（1只）
INSERT INTO mms_bom (bom_no, bom_name, product_id, product_code, product_name, bom_type, version, base_qty, base_unit, status, effective_date, remark, del_flag, create_by, create_time)
VALUES ('BOM20260005','干腌火腿标准BOM（1只）',@m_ham,'MAT011','干腌火腿（整腿）',0,'1.0',1.000000,'3','1',DATE'2026-10-15','产出率模型配套BOM：1kg成品对应鲜腿1.32kg（产出率76%）','0','admin',NOW());
SET @b := LAST_INSERT_ID();
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark) VALUES
(@b,1,@m_leg,'MAT010','鲜猪腿（原料腿）','带骨鲜腿','3',1.320000,0.00,'1','1','0','0','admin',NOW(),'唯一主原料，产出率76%对应投料1.32kg');

-- BOM6 干腌火腿腌制配方BOM
INSERT INTO mms_bom (bom_no, bom_name, product_id, product_code, product_name, bom_type, version, base_qty, base_unit, status, effective_date, remark, del_flag, create_by, create_time)
VALUES ('BOM20260006','干腌火腿腌制配方BOM（1只）',@m_ham,'MAT011','干腌火腿（整腿）',2,'1.0',1.000000,'3','1',DATE'2026-10-15','含三次上盐腌制辅料用盐配比','0','admin',NOW());
SET @b := LAST_INSERT_ID();
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark) VALUES
(@b,1,@m_leg,'MAT010','鲜猪腿（原料腿）','带骨鲜腿','3',1.320000,0.00,'1','1','0','0','admin',NOW(),'主原料'),
(@b,2,@m_salt,'MAT013','食用盐','精制加碘盐 50kg/袋','3',0.060000,5.00,'0','2','0','0','admin',NOW(),'三次上盐用盐，搓盐损耗另计');

-- BOM7 巴氏杀菌乳标准BOM（1盒）
INSERT INTO mms_bom (bom_no, bom_name, product_id, product_code, product_name, bom_type, version, base_qty, base_unit, status, effective_date, remark, del_flag, create_by, create_time)
VALUES ('BOM20260007','巴氏杀菌乳标准BOM（250ml/盒）',(SELECT material_id FROM wms_material WHERE material_code='MAT020'),'MAT020','巴氏杀菌乳',0,'1.0',1.000000,'20','1',DATE'2026-10-15','巴氏鲜奶单盒配方，生乳投料按密度1.028折算','0','admin',NOW());
SET @b := LAST_INSERT_ID();
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark) VALUES
(@b,1,@m_milk,'MAT001','鲜牛奶','250ml/盒','3',0.257000,1.00,'1','1','0','0','admin',NOW(),'验收合格生乳，主管线直接领料'),
(@b,2,@m_film,'MAT018','PVDC包装膜','食品级热收缩膜 500m/卷','24',0.010000,1.00,'0','1','0','0','admin',NOW(),'屋顶盒包装膜');

-- BOM8 巴氏杀菌乳批次配方BOM（1000盒）
INSERT INTO mms_bom (bom_no, bom_name, product_id, product_code, product_name, bom_type, version, base_qty, base_unit, status, effective_date, remark, del_flag, create_by, create_time)
VALUES ('BOM20260008','巴氏杀菌乳批次配方BOM（1000盒）',(SELECT material_id FROM wms_material WHERE material_code='MAT020'),'MAT020','巴氏杀菌乳',2,'1.0',1000.000000,'20','1',DATE'2026-10-15','按班次批次投料口径，罐批追踪用','0','admin',NOW());
SET @b := LAST_INSERT_ID();
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark) VALUES
(@b,1,@m_milk,'MAT001','鲜牛奶','250ml/盒','3',259.600000,1.00,'1','1','0','0','admin',NOW(),'整罐批次投料，含灌装损耗1%'),
(@b,2,@m_film,'MAT018','PVDC包装膜','食品级热收缩膜 500m/卷','24',10.200000,1.00,'0','1','0','0','admin',NOW(),'整卷领用，含换卷损耗');

-- BOM9 原味酸奶标准BOM（1盒）
INSERT INTO mms_bom (bom_no, bom_name, product_id, product_code, product_name, bom_type, version, base_qty, base_unit, status, effective_date, remark, del_flag, create_by, create_time)
VALUES ('BOM20260009','原味酸奶标准BOM（100g/盒）',(SELECT material_id FROM wms_material WHERE material_code='MAT021'),'MAT021','原味酸奶',0,'1.0',1.000000,'20','1',DATE'2026-10-15','凝固型酸奶单盒配方，发酵菌种由车间领用不进BOM','0','admin',NOW());
SET @b := LAST_INSERT_ID();
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark) VALUES
(@b,1,@m_milk,'MAT001','鲜牛奶','250ml/盒','3',0.243000,1.00,'1','1','0','0','admin',NOW(),'标准化后生乳投料'),
(@b,2,@m_sugar,'MAT012','白砂糖','一级白砂糖 50kg/袋','3',0.025000,0.00,'0','2','0','0','admin',NOW(),'配料用糖'),
(@b,3,@m_film,'MAT018','PVDC包装膜','食品级热收缩膜 500m/卷','24',0.010000,1.00,'0','1','0','0','admin',NOW(),'灌装膜');

-- BOM10 原味酸奶批次配方BOM（1000盒）
INSERT INTO mms_bom (bom_no, bom_name, product_id, product_code, product_name, bom_type, version, base_qty, base_unit, status, effective_date, remark, del_flag, create_by, create_time)
VALUES ('BOM20260010','原味酸奶批次配方BOM（1000盒）',(SELECT material_id FROM wms_material WHERE material_code='MAT021'),'MAT021','原味酸奶',2,'1.0',1000.000000,'20','1',DATE'2026-10-15','按发酵罐批次投料口径，罐批追踪用','0','admin',NOW());
SET @b := LAST_INSERT_ID();
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark) VALUES
(@b,1,@m_milk,'MAT001','鲜牛奶','250ml/盒','3',245.400000,1.00,'1','1','0','0','admin',NOW(),'整罐批次投料，含损耗1%'),
(@b,2,@m_sugar,'MAT012','白砂糖','一级白砂糖 50kg/袋','3',25.300000,0.00,'0','2','0','0','admin',NOW(),'整袋折算投料'),
(@b,3,@m_film,'MAT018','PVDC包装膜','食品级热收缩膜 500m/卷','24',10.100000,1.00,'0','1','0','0','admin',NOW(),'整卷领用');

-- ---------------------------------------------------------------------
-- 附1：新增成品排产参数（food_material_param）
-- ---------------------------------------------------------------------
INSERT INTO food_material_param (material_id, material_code, material_name, yield_model, std_yield_rate, yield_alarm_rate, schedule_mode, del_flag, create_by, create_time, remark)
VALUES
((SELECT material_id FROM wms_material WHERE material_code='MAT020'),'MAT020','巴氏杀菌乳','2',NULL,NULL,'2','0','admin',NOW(),'巴氏杀菌乳：BOM配比配方，离散日排产，罐批追踪'),
((SELECT material_id FROM wms_material WHERE material_code='MAT021'),'MAT021','原味酸奶','2',NULL,NULL,'2','0','admin',NOW(),'原味酸奶：BOM配比配方，离散日排产，发酵罐批追踪');

-- ---------------------------------------------------------------------
-- 附2：编号规则 current_seq 同步（物料21/供应商10/仓库4/客户11）
-- ---------------------------------------------------------------------
UPDATE mk_number_rule SET current_seq=21 WHERE rule_code='wms_material';
UPDATE mk_number_rule SET current_seq=10 WHERE rule_code='wms_supplier';
UPDATE mk_number_rule SET current_seq=4  WHERE rule_code='wms_warehouse';
UPDATE mk_number_rule SET current_seq=11 WHERE rule_code='customer';

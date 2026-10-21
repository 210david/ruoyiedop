-- =====================================================================
-- 郎鹰金属初始化脚本 04：生产(mms)/设备(dms)/质量(qms)/安全(safety) + 编号规则回写
-- 依赖：01清理、02组织人员、03仓储营销投标采购 已执行
-- =====================================================================
USE ruoyi_lyjs;

-- ==================== 一、生产管控 mms ====================
-- 工厂
INSERT INTO mms_factory (factory_id,factory_code,factory_name,address,status,del_flag,create_by,create_time,remark) VALUES
(1,'F001','郎鹰金属构件生产基地','云南省曲靖经济技术开发区郎鹰路1号','0','0','admin','2026-10-21 09:00:00',NULL);
-- 车间
INSERT INTO mms_workshop (workshop_id,workshop_code,workshop_name,factory_id,leader_user_id,status,del_flag,create_by,create_time,remark) VALUES
(1,'WS001','钢构加工车间',1,13,'0','0','admin','2026-10-21 09:00:00','下料/焊接/涂装'),
(2,'WS002','轻钢龙骨车间',1,14,'0','0','admin','2026-10-21 09:00:00','龙骨辊压成型'),
(3,'WS003','装配车间',1,15,'0','0','admin','2026-10-21 09:00:00','组件装配与总装');
-- 产线
INSERT INTO mms_line (line_id,line_code,line_name,workshop_id,line_type,cycle_time,status,del_flag,create_by,create_time,remark) VALUES
(1,'LN001','激光切割线',1,'0',2700,'0','0','admin','2026-10-21 09:00:00','板材下料'),
(2,'LN002','焊接线',1,'0',3600,'0','0','admin','2026-10-21 09:00:00',NULL),
(3,'LN003','龙骨成型线',2,'0',90,'0','0','admin','2026-10-21 09:00:00','连续辊压'),
(4,'LN004','总装线',3,'0',4200,'0','0','admin','2026-10-21 09:00:00',NULL);
-- 工位
INSERT INTO mms_station (station_id,station_code,station_name,line_id,station_type,status,del_flag,create_by,create_time,remark) VALUES
(1,'ST0001','激光上料工位',1,'0','0','0','admin','2026-10-21 09:00:00',NULL),
(2,'ST0002','激光切割工位',1,'0','0','0','admin','2026-10-21 09:00:00',NULL),
(3,'ST0003','焊接工位A',2,'0','0','0','admin','2026-10-21 09:00:00',NULL),
(4,'ST0004','焊接工位B',2,'0','0','0','admin','2026-10-21 09:00:00',NULL),
(5,'ST0005','成型主机工位',3,'0','0','0','admin','2026-10-21 09:00:00',NULL),
(6,'ST0006','总装工位',4,'0','0','0','admin','2026-10-21 09:00:00',NULL);
-- 班组
INSERT INTO mms_team (team_id,team_code,team_name,resource_id,leader,phone,member_count,status,del_flag,create_by,create_time,remark) VALUES
(1,'TM0001','切割班组',1,'徐敏','15187450001',5,'0','0','admin','2026-10-21 09:00:00',NULL),
(2,'TM0002','焊接班组',2,'罗鹏','15187450002',6,'0','0','admin','2026-10-21 09:00:00',NULL),
(3,'TM0003','装配班组',4,'马亮','15187450003',7,'0','0','admin','2026-10-21 09:00:00',NULL);
-- 班次
INSERT INTO mms_shift (shift_id,shift_code,shift_name,start_time,end_time,rest_minutes,avail_hours,status,remark,del_flag,create_by,create_time) VALUES
(1,'SFT0001','白班','08:00','17:00',60,8.00,'0','标准白班','0','admin','2026-10-21 09:00:00'),
(2,'SFT0002','中班','16:00','24:00',60,7.00,'0','跨白夜中班','0','admin','2026-10-21 09:00:00');
-- 生产资源（产能单元）
INSERT INTO mms_resource (resource_id,resource_code,resource_name,resource_type,rate,rate_unit,batch_min,batch_max,efficiency,workshop_name,line_name,status,remark,del_flag,create_by,create_time) VALUES
(1,'RES0001','激光切割工作中心','0',60.000,'张/h',1.000,500.000,95.00,'钢构加工车间','激光切割线','0','GF-6020 6kW','0','admin','2026-10-21 09:00:00'),
(2,'RES0002','焊接工作中心','0',8.000,'件/h',1.000,100.000,90.00,'钢构加工车间','焊接线','0','焊工6人','0','admin','2026-10-21 09:00:00'),
(3,'RES0003','龙骨成型工作中心','0',300.000,'m/h',100.000,20000.000,92.00,'轻钢龙骨车间','龙骨成型线','0','LFT-89/LFT-50','0','admin','2026-10-21 09:00:00'),
(4,'RES0004','总装工作中心','0',10.000,'榀/h',1.000,60.000,88.00,'装配车间','总装线','0',NULL,'0','admin','2026-10-21 09:00:00'),
(5,'RES0005','剪折弯工作中心','0',40.000,'件/h',1.000,200.000,90.00,'钢构加工车间','激光切割线','0','剪板/折弯','0','admin','2026-10-21 09:00:00'),
(6,'RES0006','表面处理工作中心','0',15.000,'件/h',1.000,100.000,85.00,'钢构加工车间','焊接线','0','抛丸+喷涂','0','admin','2026-10-21 09:00:00');
-- 工序（std_time/prep_time 单位：小时）
INSERT INTO mms_process (process_id,process_code,process_name,process_type,std_time,prep_time,is_key_process,is_outsource,workshop_name,line_name,status,remark,del_flag,create_by,create_time) VALUES
(1,'GX001','激光切割下料','0',0.45,0.25,'1','0','钢构加工车间','激光切割线','0',NULL,'0','admin','2026-10-21 09:00:00'),
(2,'GX002','等离子切割下料','0',0.40,0.20,'1','0','钢构加工车间','激光切割线','0',NULL,'0','admin','2026-10-21 09:00:00'),
(3,'GX003','铣边开坡口','0',0.60,0.30,'0','0','钢构加工车间','焊接线','0',NULL,'0','admin','2026-10-21 09:00:00'),
(4,'GX004','组立定位焊','1',0.75,0.25,'1','0','钢构加工车间','焊接线','0',NULL,'0','admin','2026-10-21 09:00:00'),
(5,'GX005','气体保护焊','1',1.20,0.30,'1','0','钢构加工车间','焊接线','0','关键工序','0','admin','2026-10-21 09:00:00'),
(6,'GX006','焊后校正','0',0.50,0.15,'0','0','钢构加工车间','焊接线','0',NULL,'0','admin','2026-10-21 09:00:00'),
(7,'GX007','龙骨辊压成型','0',0.35,0.20,'0','0','轻钢龙骨车间','龙骨成型线','0',NULL,'0','admin','2026-10-21 09:00:00'),
(8,'GX008','数控钻孔','0',0.40,0.20,'0','0','钢构加工车间','焊接线','0',NULL,'0','admin','2026-10-21 09:00:00'),
(9,'GX009','抛丸除锈涂装','0',0.80,0.40,'0','0','钢构加工车间','焊接线','0',NULL,'0','admin','2026-10-21 09:00:00'),
(10,'GX010','终检包装','2',0.30,0.10,'1','0','装配车间','总装线','0','出厂检验','0','admin','2026-10-21 09:00:00');
-- 工艺路线
INSERT INTO mms_route (route_id,route_no,route_name,product_id,product_code,product_name,version,effective_date,status,default_workshop,default_line,total_std_time,process_count,remark,del_flag,create_by,create_time) VALUES
(1,'RT0001','非标钢构支架工艺路线',11,'M011','非标钢构支架','1.0','2026-01-01','2','钢构加工车间','焊接线',5.00,8,NULL,'0','admin','2026-10-21 09:00:00'),
(2,'RT0002','轻钢龙骨C89工艺路线',13,'M013','轻钢龙骨C型','1.0','2026-01-01','2','轻钢龙骨车间','龙骨成型线',1.15,3,NULL,'0','admin','2026-10-21 09:00:00'),
(3,'RT0003','装配式墙体组件工艺路线',15,'M015','装配式轻钢墙体组件','1.0','2026-01-01','2','装配车间','总装线',3.85,6,NULL,'0','admin','2026-10-21 09:00:00');
-- 工艺路线工序
INSERT INTO mms_route_process (id,route_id,process_id,process_code,process_name,process_type,workshop,line,step_seq,std_time,prep_time,is_key_process,is_outsource,resource_id,resource_name,remark,del_flag,create_by,create_time) VALUES
(1,1,1,'GX001','激光切割下料','0','钢构加工车间','激光切割线',10,0.45,0.25,'1','0',1,'激光切割工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(2,1,3,'GX003','铣边开坡口','0','钢构加工车间','焊接线',20,0.60,0.30,'0','0',2,'焊接工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(3,1,4,'GX004','组立定位焊','1','钢构加工车间','焊接线',30,0.75,0.25,'1','0',2,'焊接工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(4,1,5,'GX005','气体保护焊','1','钢构加工车间','焊接线',40,1.20,0.30,'1','0',2,'焊接工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(5,1,6,'GX006','焊后校正','0','钢构加工车间','焊接线',50,0.50,0.15,'0','0',2,'焊接工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(6,1,8,'GX008','数控钻孔','0','钢构加工车间','焊接线',60,0.40,0.20,'0','0',5,'剪折弯工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(7,1,9,'GX009','抛丸除锈涂装','0','钢构加工车间','焊接线',70,0.80,0.40,'0','0',6,'表面处理工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(8,1,10,'GX010','终检包装','2','装配车间','总装线',80,0.30,0.10,'1','0',4,'总装工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(9,2,7,'GX007','龙骨辊压成型','0','轻钢龙骨车间','龙骨成型线',10,0.35,0.20,'0','0',3,'龙骨成型工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(10,2,6,'GX006','焊后校正','0','轻钢龙骨车间','龙骨成型线',20,0.50,0.15,'0','0',3,'龙骨成型工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(11,2,10,'GX010','终检包装','2','装配车间','总装线',30,0.30,0.10,'1','0',4,'总装工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(12,3,2,'GX002','等离子切割下料','0','钢构加工车间','激光切割线',10,0.40,0.20,'1','0',1,'激光切割工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(13,3,4,'GX004','组立定位焊','1','钢构加工车间','焊接线',20,0.75,0.25,'1','0',2,'焊接工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(14,3,5,'GX005','气体保护焊','1','钢构加工车间','焊接线',30,1.20,0.30,'1','0',2,'焊接工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(15,3,8,'GX008','数控钻孔','0','钢构加工车间','焊接线',40,0.40,0.20,'0','0',5,'剪折弯工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(16,3,9,'GX009','抛丸除锈涂装','0','钢构加工车间','焊接线',50,0.80,0.40,'0','0',6,'表面处理工作中心',NULL,'0','admin','2026-10-21 09:00:00'),
(17,3,10,'GX010','终检包装','2','装配车间','总装线',60,0.30,0.10,'1','0',4,'总装工作中心',NULL,'0','admin','2026-10-21 09:00:00');
-- BOM
INSERT INTO mms_bom (bom_id,bom_no,bom_name,product_id,product_code,product_name,bom_type,version,base_qty,base_unit,status,effective_date,remark,del_flag,create_by,create_time) VALUES
(1,'BOM0001','非标钢构支架BOM',11,'M011','非标钢构支架','0','1.0',1.000000,'件','1','2026-01-01',NULL,'0','admin','2026-10-21 09:00:00'),
(2,'BOM0002','轻钢龙骨C89 BOM(100m)',13,'M013','轻钢龙骨C型','0','1.0',100.000000,'m','1','2026-01-01','按100米成品基准','0','admin','2026-10-21 09:00:00'),
(3,'BOM0003','装配式墙体组件BOM',15,'M015','装配式轻钢墙体组件','0','1.0',1.000000,'榀','1','2026-01-01',NULL,'0','admin','2026-10-21 09:00:00');
-- BOM明细
INSERT INTO mms_bom_detail (detail_id,bom_id,seq,material_id,material_code,material_name,spec_model,unit,usage_qty,loss_rate,is_key_material,supply_type,pick_store_id,bom_ref_id,remark,del_flag,create_by,create_time) VALUES
(1,1,10,2,'M002','Q355B钢板','δ10×1500×3000','张',0.050000,3.00,'1','1',1,NULL,NULL,'0','admin','2026-10-21 09:00:00'),
(2,1,20,1,'M001','Q235B钢板','δ6×1500×3000','张',0.030000,3.00,'0','1',1,NULL,NULL,'0','admin','2026-10-21 09:00:00'),
(3,1,30,5,'M005','Q235B角钢','50×50×5×6000','根',0.500000,2.00,'0','1',1,NULL,NULL,'0','admin','2026-10-21 09:00:00'),
(4,1,40,17,'M017','CO2气体保护焊丝','ER50-6 φ1.2','盘',0.040000,5.00,'1','1',1,NULL,NULL,'0','admin','2026-10-21 09:00:00'),
(5,1,50,19,'M019','工业二氧化碳气体','瓶装','瓶',0.010000,0.00,'0','1',1,NULL,NULL,'0','admin','2026-10-21 09:00:00'),
(6,1,60,20,'M020','环氧富锌底漆','H06-4','kg',0.250000,4.00,'0','1',1,NULL,NULL,'0','admin','2026-10-21 09:00:00'),
(7,1,70,21,'M021','聚氨酯面漆','S04-16','kg',0.150000,4.00,'0','1',1,NULL,NULL,'0','admin','2026-10-21 09:00:00'),
(8,2,10,7,'M007','热镀锌钢带','0.8×1000','卷',0.045000,3.00,'1','1',1,NULL,NULL,'0','admin','2026-10-21 09:00:00'),
(9,2,20,24,'M024','五金紧固件','混装','批',0.200000,0.00,'0','1',4,NULL,NULL,'0','admin','2026-10-21 09:00:00'),
(10,3,10,13,'M013','轻钢龙骨C型','C89×41×0.8','m',12.000000,2.00,'1','1',3,2,'引用龙骨BOM','0','admin','2026-10-21 09:00:00'),
(11,3,20,14,'M014','轻钢龙骨U型','U50×40×0.6','m',6.000000,2.00,'0','1',3,NULL,NULL,'0','admin','2026-10-21 09:00:00'),
(12,3,30,17,'M017','CO2气体保护焊丝','ER50-6 φ1.2','盘',0.020000,5.00,'0','1',1,NULL,NULL,'0','admin','2026-10-21 09:00:00'),
(13,3,40,23,'M023','高强度螺栓','M16×60 10.9级','套',24.000000,1.00,'1','1',4,NULL,NULL,'0','admin','2026-10-21 09:00:00'),
(14,3,50,24,'M024','五金紧固件','混装','批',1.000000,0.00,'0','1',4,NULL,NULL,'0','admin','2026-10-21 09:00:00');

-- ==================== 二、设备管理 dms ====================
-- 设备分类（树）
INSERT INTO dms_equipment_category (category_id,parent_id,ancestors,category_code,category_name,category_level,order_num,status,del_flag,create_by,create_time,remark) VALUES
(1,0,'0','JG01','金属切割设备',1,1,'0','0','admin','2026-10-21 09:00:00',NULL),
(2,0,'0','JG02','焊接设备',1,2,'0','0','admin','2026-10-21 09:00:00',NULL),
(3,0,'0','JG03','成型设备',1,3,'0','0','admin','2026-10-21 09:00:00',NULL),
(4,0,'0','JG04','起重设备',1,4,'0','0','admin','2026-10-21 09:00:00',NULL),
(5,0,'0','JG05','动力设备',1,5,'0','0','admin','2026-10-21 09:00:00',NULL),
(6,1,'0,1','JG0101','激光切割机',2,1,'0','0','admin','2026-10-21 09:00:00',NULL),
(7,1,'0,1','JG0102','等离子切割机',2,2,'0','0','admin','2026-10-21 09:00:00',NULL),
(8,2,'0,2','JG0201','电弧焊机',2,1,'0','0','admin','2026-10-21 09:00:00',NULL),
(9,3,'0,3','JG0301','龙骨成型机',2,1,'0','0','admin','2026-10-21 09:00:00',NULL),
(10,3,'0,3','JG0302','剪板折弯机',2,2,'0','0','admin','2026-10-21 09:00:00',NULL),
(11,4,'0,4','JG0401','桥式起重机',2,1,'0','0','admin','2026-10-21 09:00:00','特种设备');
-- 设备台账
INSERT INTO dms_equipment (equipment_id,equipment_code,equipment_name,asset_code,category_id,model,serial_number,manufacturer,supplier,purchase_date,install_date,original_value,dept_id,dept_name,install_location,equipment_status,equipment_level,responsible_id,responsible_name,status,del_flag,create_by,create_time,remark) VALUES
(1,'SB0001','数控光纤激光切割机','ZC-2023-001',6,'GF-6020(6kW)','DZ202305001','大族激光','大族激光','2023-05-10','2023-06-01',1680000.00,106,'生产部','钢构加工车间-切割区','0','0',14,'徐敏','0','0','admin','2026-10-21 09:00:00',NULL),
(2,'SB0002','数控等离子切割机','ZC-2022-002',7,'Hypertherm 65','HT202202002','海宝','曲靖华祥商贸','2022-02-18','2022-03-10',285000.00,106,'生产部','钢构加工车间-切割区','0','1',14,'徐敏','0','0','admin','2026-10-21 09:00:00',NULL),
(3,'SB0003','轻钢龙骨成型机','ZC-2021-003',9,'LFT-89','NH202103003','南华轻机','南华轻机','2021-03-15','2021-04-05',520000.00,106,'生产部','轻钢龙骨车间','0','0',18,'谢林','0','0','admin','2026-10-21 09:00:00','C89型'),
(4,'SB0004','轻钢龙骨成型机','ZC-2021-004',9,'LFT-50','NH202103004','南华轻机','南华轻机','2021-03-15','2021-04-05',430000.00,106,'生产部','轻钢龙骨车间','0','1',18,'谢林','0','0','admin','2026-10-21 09:00:00','U50型'),
(5,'SB0005','液压剪板机','ZC-2020-005',10,'QC12Y-6×2500','AM202005005','安徽中德机床','安徽中德','2020-05-20','2020-06-10',168000.00,106,'生产部','钢构加工车间','0','1',18,'谢林','0','0','admin','2026-10-21 09:00:00',NULL),
(6,'SB0006','液压折弯机','ZC-2020-006',10,'WC67Y-100/3200','AM202006006','安徽中德机床','安徽中德','2020-06-15','2020-07-01',196000.00,106,'生产部','钢构加工车间','0','1',18,'谢林','0','0','admin','2026-10-21 09:00:00',NULL),
(7,'SB0007','CO2气体保护焊机','ZC-2022-007',8,'NBC-350','TW202207007','时代焊接','昆明德威焊材','2022-07-10','2022-07-20',18500.00,106,'生产部','钢构加工车间-焊接区','0','1',15,'罗鹏','0','0','admin','2026-10-21 09:00:00',NULL),
(8,'SB0008','直流弧焊机','ZC-2022-008',8,'ZX7-400','TW202208008','时代焊接','昆明德威焊材','2022-08-15','2022-08-25',9800.00,106,'生产部','钢构加工车间-焊接区','0','2',15,'罗鹏','0','0','admin','2026-10-21 09:00:00',NULL),
(9,'SB0009','桥式起重机','ZC-2019-009',11,'LD5t-16.5m','HN201909009','河南卫华','河南卫华','2019-09-01','2019-10-15',320000.00,106,'生产部','装配车间','0','0',19,'马亮','0','0','admin','2026-10-21 09:00:00','特种设备，持证操作'),
(10,'SB0010','螺杆式空压机','ZC-2021-010',5,'LG-10/8','KA202110010','开山股份','曲靖恒力五金','2021-10-20','2021-11-01',86000.00,106,'生产部','动力房','0','1',13,'黄军','0','0','admin','2026-10-21 09:00:00',NULL);
-- 备件
INSERT INTO dms_spare_part (part_id,part_code,part_name,part_type,spec_model,unit,applicable_category,supplier_id,stock_min,stock_max,safety_stock,current_stock,storage_location,supplier,status,del_flag,create_by,create_time,remark) VALUES
(1,'BJ202609010001','激光切割嘴','易损件','单层 1.2mm','个','激光切割机',8,10,50,15,26,'备件库-K06','南华轻机','0','0','admin','2026-10-21 09:00:00',NULL),
(2,'BJ202609010002','聚焦保护镜片','易损件','D30×5','片','激光切割机',8,5,30,8,12,'备件库-K06','南华轻机','0','0','admin','2026-10-21 09:00:00',NULL),
(3,'BJ202609010003','等离子电极','易损件','120A','个','等离子切割机',5,10,60,20,34,'备件库-K06','德威焊材','0','0','admin','2026-10-21 09:00:00',NULL),
(4,'BJ202609010004','等离子喷嘴','易损件','120A','个','等离子切割机',5,10,60,20,40,'备件库-K06','德威焊材','0','0','admin','2026-10-21 09:00:00',NULL),
(5,'BJ202609010005','成型机轧辊','关键备件','89型 成套','套','龙骨成型机',8,1,3,1,2,'备件库-K06','南华轻机','0','0','admin','2026-10-21 09:00:00','定制品，交期15天'),
(6,'BJ202609010006','焊机导电嘴','易损件','φ1.2','个','电弧焊机',5,20,100,30,55,'备件库-K06','德威焊材','0','0','admin','2026-10-21 09:00:00',NULL);
-- 点检路线
INSERT INTO dms_inspection_route (route_id,route_name,route_code,dept_id,dept_name,cycle_type,cycle_value,equipment_ids,inspection_items,status,del_flag,create_by,create_time,remark) VALUES
(1,'切割设备日点检','R001',106,'生产部','0',1,'[1,2]','[{"item":"激光器运行声音","type":"check","unit":"","abnormalRequired":true},{"item":"切割气压MPa","type":"number","unit":"MPa","abnormalRequired":false},{"item":"冷却液位","type":"check","unit":"","abnormalRequired":true}]','0','0','admin','2026-10-21 09:00:00',NULL),
(2,'焊机周点检','R002',106,'生产部','1',1,'[7,8]','[{"item":"电缆接头温度","type":"check","unit":"","abnormalRequired":true},{"item":"气路密封性","type":"check","unit":"","abnormalRequired":true},{"item":"接地电阻","type":"number","unit":"Ω","abnormalRequired":false}]','0','0','admin','2026-10-21 09:00:00',NULL);
-- PM保养计划
INSERT INTO dms_pm_plan (plan_id,plan_name,equipment_id,category_id,trigger_type,cycle_value,cycle_unit,task_list,spare_part_list,estimated_hours,advance_days,auto_assign,assignee_id,assignee_name,last_execute_time,next_execute_time,status,del_flag,create_by,create_time,remark) VALUES
(1,'龙骨成型机月度保养',3,9,'0',1,'月','["清洁轧辊表面与导轨","加注润滑油","紧固电气端子","检查液压油位"]','[{"partId":6,"partName":"焊机导电嘴","qty":2,"unit":"个"}]',4.00,3,'1',18,'谢林','2026-09-28 00:00:00','2026-10-28 00:00:00','0','0','admin','2026-10-21 09:00:00',NULL),
(2,'激光切割机激光器季度维护',1,6,'0',3,'月','["镜片清洁与更换","光路校准","冷却水更换","切割精度检测"]','[{"partId":1,"partName":"激光切割嘴","qty":2,"unit":"个"},{"partId":2,"partName":"聚焦保护镜片","qty":2,"unit":"片"}]',6.00,5,'1',14,'徐敏','2026-08-05 00:00:00','2026-11-05 00:00:00','0','0','admin','2026-10-21 09:00:00',NULL);

-- ==================== 三、质量管理 qms ====================
-- 检验标准库（对齐 GB 50205）
INSERT INTO qms_inspect_std (std_id,std_code,std_name,inspect_method,unit,val_min,val_max,target_val,judge_rule,status,del_flag,create_by,create_time,remark) VALUES
(1,'STD001','钢板厚度偏差','千分尺测量','mm',-0.3000,0.3000,NULL,'1','0','0','admin','2026-10-21 09:00:00','GB/T 709'),
(2,'STD002','切割件对角线差','钢卷尺测量','mm',NULL,2.0000,NULL,'2','0','0','admin','2026-10-21 09:00:00',NULL),
(3,'STD003','焊缝外观质量','目视检查',NULL,NULL,NULL,NULL,'3','0','0','admin','2026-10-21 09:00:00','无裂纹/咬边/气孔'),
(4,'STD004','焊缝余高','焊缝检验尺','mm',0.0000,3.0000,NULL,'1','0','0','admin','2026-10-21 09:00:00',NULL),
(5,'STD005','焊缝内部质量','超声波探伤','级',NULL,2.0000,NULL,'2','0','0','admin','2026-10-21 09:00:00','GB 11345 B级检验Ⅱ级合格'),
(6,'STD006','涂层干膜厚度','涂层测厚仪','μm',100.0000,150.0000,125.0000,'1','0','0','admin','2026-10-21 09:00:00','两道涂层'),
(7,'STD007','龙骨断面高度','游标卡尺','mm',88.5000,89.5000,89.0000,'1','0','0','admin','2026-10-21 09:00:00','C89'),
(8,'STD008','龙骨弯曲度','拉线+钢板尺','mm/m',NULL,1.5000,NULL,'2','0','0','admin','2026-10-21 09:00:00',NULL),
(9,'STD009','螺栓紧固扭矩','扭矩扳手','N·m',40.0000,50.0000,45.0000,'1','0','0','admin','2026-10-21 09:00:00','M16 10.9级'),
(10,'STD010','构件垂直度','经纬仪测量','mm',NULL,5.0000,NULL,'2','0','0','admin','2026-10-21 09:00:00','H/1000且≤5mm');
-- 缺陷代码
INSERT INTO qms_defect_code (defect_id,defect_code,defect_name,defect_level,defect_category,status,del_flag,create_by,create_time,remark) VALUES
(1,'DF001','尺寸超差','2','尺寸','0','0','admin','2026-10-21 09:00:00',NULL),
(2,'DF002','焊缝气孔','2','焊接','0','0','admin','2026-10-21 09:00:00',NULL),
(3,'DF003','焊缝咬边','3','焊接','0','0','admin','2026-10-21 09:00:00',NULL),
(4,'DF004','构件变形','2','尺寸','0','0','admin','2026-10-21 09:00:00',NULL),
(5,'DF005','涂层流挂','3','涂装','0','0','admin','2026-10-21 09:00:00',NULL),
(6,'DF006','涂层漏涂','3','涂装','0','0','admin','2026-10-21 09:00:00',NULL);
-- 量检具
INSERT INTO qms_gauge (gauge_id,gauge_no,gauge_name,gauge_type,precision_val,measure_range,calib_cycle,last_calib_date,next_calib_date,gauge_status,dept_id,status,del_flag,create_by,create_time,remark) VALUES
(1,'Gauge001','钢卷尺','量具类','1mm','0-50m',12,'2026-03-15','2027-03-15','0',105,'0','0','admin','2026-10-21 09:00:00',NULL),
(2,'Gauge002','游标卡尺','量具类','0.02mm','0-150mm',12,'2026-03-15','2027-03-15','0',105,'0','0','admin','2026-10-21 09:00:00',NULL),
(3,'Gauge003','焊缝检验尺','专用量具','0.1mm','0-40mm',12,'2026-03-15','2027-03-15','0',105,'0','0','admin','2026-10-21 09:00:00',NULL),
(4,'Gauge004','涂层测厚仪','仪器类','1μm','0-1500μm',12,'2026-03-15','2027-03-15','0',105,'0','0','admin','2026-10-21 09:00:00',NULL),
(5,'Gauge005','超声波探伤仪','仪器类','数字式','0-5000mm',12,'2026-03-15','2027-03-15','0',105,'0','0','admin','2026-10-21 09:00:00',NULL),
(6,'Gauge006','框式水平仪','量具类','0.02mm/m','200mm',12,'2026-03-15','2027-03-15','0',105,'0','0','admin','2026-10-21 09:00:00',NULL);
-- 质量目标
INSERT INTO qms_quality_target (target_id,target_year,target_month,dimension,dimension_val,target_pass_rate,actual_pass_rate,target_complaint_rate,actual_complaint_rate,target_capa_close_rate,actual_capa_close_rate,achievement,del_flag,status,create_by,create_time,remark) VALUES
(1,2026,NULL,'factory','全厂',96.00,NULL,2.00,NULL,100.00,NULL,'P','0','0','admin','2026-10-21 09:00:00','年度目标'),
(2,2026,9,'factory','全厂',96.00,96.80,2.00,1.20,100.00,100.00,'Y','0','0','admin','2026-10-21 09:00:00',NULL),
(3,2026,10,'factory','全厂',96.00,97.20,2.00,0.80,100.00,100.00,'Y','0','0','admin','2026-10-21 09:00:00','10月至今累计');
-- 物料质量属性
INSERT INTO qms_material_attr (attr_id,material_id,material_code,material_name,inspect_types,industry_type,is_exempt,status,del_flag,create_by,create_time,remark) VALUES
(1,1,'M001','Q235B钢板','IQC',NULL,'0','0','0','admin','2026-10-21 09:00:00','到货必检质保书+复验'),
(2,2,'M002','Q355B钢板','IQC',NULL,'0','0','0','admin','2026-10-21 09:00:00',NULL),
(3,3,'M003','Q355B低合金钢板','IQC',NULL,'0','0','0','admin','2026-10-21 09:00:00',NULL),
(4,11,'M011','非标钢构支架','IPQC,FQC',NULL,'0','0','0','admin','2026-10-21 09:00:00','过程巡检+成品终检'),
(5,13,'M013','轻钢龙骨C型','IPQC,FQC',NULL,'0','0','0','admin','2026-10-21 09:00:00',NULL),
(6,17,'M017','CO2气体保护焊丝','IQC',NULL,'0','0','0','admin','2026-10-21 09:00:00',NULL);

-- ==================== 四、安全生产 safety ====================
-- 企业安全档案
INSERT INTO biz_safety_enterprise (enterprise_id,enterprise_name,credit_code,industry_type,is_high_risk,legal_person,safety_manager_id,safety_manager,safety_org,supervise_dept,status,del_flag,create_by,create_time,remark) VALUES
(1,'曲靖郎鹰金属构件有限公司','91530300MA6L1AB23X','金属制品业','0','李建华',12,'李海','安全生产领导小组','曲靖经济技术开发区应急管理局','0','0','admin','2026-10-21 09:00:00',NULL);
-- 厂区/区域（node_type 1厂区 2车间区域 3库房区域）
INSERT INTO biz_safety_area (area_id,parent_id,ancestors,area_code,area_name,node_type,enterprise_id,area_type,safety_person_id,safety_person_name,status,del_flag,create_by,create_time,remark) VALUES
(1,0,'0','QY001','郎鹰金属厂区','1',1,'1',12,'李海','0','0','admin','2026-10-21 09:00:00',NULL),
(2,1,'0,1','QY002','下料切割区','2',1,'1',14,'徐敏','0','0','admin','2026-10-21 09:00:00',NULL),
(3,1,'0,1','QY003','焊接作业区','2',1,'1',15,'罗鹏','0','0','admin','2026-10-21 09:00:00',NULL),
(4,1,'0,1','QY004','龙骨成型区','2',1,'1',14,'徐敏','0','0','admin','2026-10-21 09:00:00',NULL),
(5,1,'0,1','QY005','装配起重区','2',1,'1',19,'马亮','0','0','admin','2026-10-21 09:00:00',NULL),
(6,1,'0,1','QY006','油漆危化品库','3',1,'3',8,'孙梅','0','0','admin','2026-10-21 09:00:00','重点防火防爆区域'),
(7,1,'0,1','QY007','气瓶存放间','3',1,'3',8,'孙梅','0','0','admin','2026-10-21 09:00:00',NULL),
(8,1,'0,1','QY008','成品堆场','3',1,'3',9,'周超','0','0','admin','2026-10-21 09:00:00',NULL);
-- 风险点（LEC法：D=L×E×C；等级 1重大 2较大 3一般 4低）
INSERT INTO biz_safety_risk_point (risk_point_id,risk_code,risk_name,area_id,area_name,hazard_desc,accident_type,l_value,e_value,c_value,d_value,risk_level,control_level,measure_engine,measure_manage,measure_training,measure_ppe,measure_emergency,check_frequency,is_major_hazard,person_id,person_name,evaluate_date,re_evaluate_date,enterprise_id,dept_id,status,del_flag,create_by,create_time,remark) VALUES
(1,'FX2026090101','激光切割机光辐射与机械伤害',2,'下料切割区','激光切割机运行中激光辐射、运动部件卷入','机械伤害、灼伤',3.00,6.00,3.00,54.00,'3','3','设备联锁防护罩、光路封闭','专人操作、开机前点检','年度安全培训+岗前培训','防护眼镜、劳保用品','立即停机、切断电源、报告车间',3,'0',14,'徐敏','2026-09-01','2027-03-01',1,106,'0','0','admin','2026-10-21 09:00:00',NULL),
(2,'FX2026090102','焊接作业弧光与烟尘',3,'焊接作业区','电弧光辐射、焊接烟尘、打磨机械伤害','灼伤、尘肺',3.00,6.00,7.00,126.00,'2','2','移动式烟尘净化器、屏光挡板','动火作业审批、双人作业','焊工持证复训','焊接面罩、防尘口罩、阻燃服','切断电源、现场急救、送医',2,'0',15,'罗鹏','2026-09-01','2027-03-01',1,106,'0','0','admin','2026-10-21 09:00:00',NULL),
(3,'FX2026090103','起重吊装作业',5,'装配起重区','行车吊装构件过程脱钩、超载、站位不当','起重伤害',3.00,6.00,15.00,270.00,'1','1','限位器、超载保护、吊索具定期检验','吊装令审批、警戒隔离、持证操作','起重工Q2证复审培训','安全帽、反光背心','立即停吊、警戒疏散、启动应急预案',1,'0',19,'马亮','2026-09-01','2027-03-01',1,106,'0','0','admin','2026-10-21 09:00:00','厂级重大风险'),
(4,'FX2026090104','配电房触电',1,'郎鹰金属厂区','高低压配电设施绝缘老化、违章带电作业','触电',3.00,6.00,7.00,126.00,'2','2','漏电保护、绝缘垫、警示标识','电工持证作业、倒闸操作票','电气安全培训','绝缘手套、绝缘鞋','断电、心肺复苏、拨打120',2,'0',12,'李海','2026-09-01','2027-03-01',1,106,'0','0','admin','2026-10-21 09:00:00',NULL),
(5,'FX2026090105','油漆库火灾爆炸',6,'油漆危化品库','油漆稀释剂挥发气体聚集遇火源爆炸、静电','火灾、爆炸',3.00,6.00,40.00,720.00,'1','1','防爆电器、静电接地、可燃气体报警、通风','双人双锁、限量存放、每日巡查','消防应急演练季度开展','防静电服、防爆手电','报警、灭火、疏散、启动专项预案',1,'0',8,'孙梅','2026-09-01','2027-03-01',1,106,'0','0','admin','2026-10-21 09:00:00','厂级重大风险'),
(6,'FX2026090106','高处坠落',8,'成品堆场','登高检查、堆垛倒塌、临边作业','高处坠落、物体打击',2.00,3.00,7.00,42.00,'3','3','堆垛限高、登高梯具合格','堆场定置管理、2m以上作业审批','高处作业培训','安全带、防滑鞋','现场保护、急救送医',3,'0',9,'周超','2026-09-01','2027-03-01',1,106,'0','0','admin','2026-10-21 09:00:00',NULL);
-- 安全设备设施
INSERT INTO biz_safety_equipment (equipment_id,equipment_code,equipment_name,spec_model,area_id,area_name,equipment_category,special_sub_type,reg_cert_no,inspect_expire,person_id,person_name,enterprise_id,status,del_flag,create_by,create_time,remark) VALUES
(1,'SB001','桥式起重机','LD5t-16.5m',5,'装配起重区','1','4','TC-2026-001','2027-06-30',19,'马亮',1,'0','0','admin','2026-10-21 09:00:00','特种设备'),
(2,'SB002','储气罐','1m³/0.8MPa',1,'郎鹰金属厂区','1','2','TC-2026-002','2027-09-30',13,'黄军',1,'0','0','admin','2026-10-21 09:00:00','压力容器'),
(3,'SB003','低压配电柜','GGD3',1,'郎鹰金属厂区','3',NULL,NULL,NULL,12,'李海',1,'0','0','admin','2026-10-21 09:00:00',NULL),
(4,'SB004','可燃气体报警器','GT-QM200',6,'油漆危化品库','3',NULL,'JC-2026-004','2027-03-31',8,'孙梅',1,'0','0','admin','2026-10-21 09:00:00','油漆库防爆区域');
-- 应急预案
INSERT INTO biz_safety_emergency_plan (plan_id,plan_code,plan_name,plan_type,plan_level,version,reg_no,reg_date,effective_date,expire_date,content,enterprise_id,dept_id,status,del_flag,create_by,create_time,remark) VALUES
(1,'YA2026081101','生产安全事故综合应急预案','1','1','V1.0','QJJK-2026-001','2026-01-10','2026-01-01','2028-01-01','公司级综合预案：组织体系、响应分级、保障措施',1,106,'0','0','admin','2026-10-21 09:00:00',NULL),
(2,'YA2026081102','火灾爆炸事故专项应急预案','2','2','V1.0','QJJK-2026-002','2026-01-10','2026-01-01','2028-01-01','针对油漆库、气瓶间火灾爆炸场景',1,106,'0','0','admin','2026-10-21 09:00:00',NULL),
(3,'YA2026081103','机械伤害事故现场处置方案','3','3','V1.0',NULL,NULL,'2026-01-01','2028-01-01','下料/焊接/装配区域机械伤害现场处置',1,106,'0','0','admin','2026-10-21 09:00:00',NULL);
-- 培训课程库
INSERT INTO biz_safety_training_course (course_id,course_code,course_name,course_type,course_form,hours,instructor,description,enterprise_id,status,del_flag,create_by,create_time,remark) VALUES
(1,'KC2026081101','新员工三级安全教育','1','1',24.00,'李海','公司级+车间级+班组级安全教育',1,'0','0','admin','2026-10-21 09:00:00',NULL),
(2,'KC2026081102','焊工特种作业复训','2','1',8.00,'李海','熔化焊接与热切割作业证复审培训',1,'0','0','admin','2026-10-21 09:00:00',NULL),
(3,'KC2026081103','消防与应急演练','2','1',4.00,'李海','灭火器使用、疏散逃生、应急预案演练',1,'0','0','admin','2026-10-21 09:00:00',NULL);
-- 特种作业人员
INSERT INTO biz_safety_worker (worker_id,worker_name,worker_no,dept_id,dept_name,position,is_special,job_type,cert_no,cert_issue_date,valid_to,review_date,enterprise_id,status,del_flag,create_by,create_time,remark) VALUES
(1,'罗鹏','EMP0015',106,'生产部','焊接班组长','1','2','RZ53030020250015','2025-03-10','2027-03-09','2027-01-09',1,'0','0','admin','2026-10-21 09:00:00','熔化焊接与热切割作业'),
(2,'邓伟','EMP0017',106,'生产部','焊工','1','2','RZ53030020240017','2024-05-20','2027-05-19','2027-03-19',1,'0','0','admin','2026-10-21 09:00:00','熔化焊接与热切割作业'),
(3,'黄军','EMP0013',106,'生产部','生产主管','1','1','DL53030020240013','2024-08-15','2027-08-14','2027-06-14',1,'0','0','admin','2026-10-21 09:00:00','低压电工作业'),
(4,'马亮','EMP0019',106,'生产部','装配班组长','1','4','QZ53030020230019','2023-12-15','2026-12-15','2026-10-15',1,'0','0','admin','2026-10-21 09:00:00','起重机机械作业Q2，证书临期需复审'),
(5,'周超','EMP0009',104,'仓储部','库管员','1','5','CC53030020240009','2024-10-20','2027-10-19','2027-08-19',1,'0','0','admin','2026-10-21 09:00:00','叉车N1');
-- 企业证照
INSERT INTO biz_safety_cert (cert_id,cert_name,cert_no,cert_type,issue_org,issue_date,expire_date,enterprise_id,status,del_flag,create_by,create_time,remark) VALUES
(1,'安全生产标准化证书(三级)','BZIII-2024-0521','3','曲靖市应急管理局','2024-06-20','2027-06-19',1,'0','0','admin','2026-10-21 09:00:00',NULL),
(2,'排污许可证','91530300MA6L1AB23X001P','3','云南省生态环境厅','2024-02-18','2029-02-17',1,'0','0','admin','2026-10-21 09:00:00',NULL),
(3,'特种设备使用登记证','设闽SK-2026-0011','3','曲靖市市场监督管理局','2026-01-15','2027-01-14',1,'0','0','admin','2026-10-21 09:00:00','桥式起重机/储气罐');

-- ==================== 五、编号规则回写 ====================
-- mk 营销/投标
UPDATE mk_number_rule SET current_seq=8,  current_date_str='2026'      WHERE rule_code='customer';
UPDATE mk_number_rule SET current_seq=4,  current_date_str='2026'      WHERE rule_code='contract';
UPDATE mk_number_rule SET current_seq=1,  current_date_str='20261015'  WHERE rule_code='order';
UPDATE mk_number_rule SET current_seq=4,  current_date_str='20261018'  WHERE rule_code='lead';
UPDATE mk_number_rule SET current_seq=3,  current_date_str='20261018'  WHERE rule_code='opportunity';
UPDATE mk_number_rule SET current_seq=1,  current_date_str='20261015'  WHERE rule_code='bid_project';
-- pms 采购
UPDATE mk_number_rule SET current_seq=1,  current_date_str='20260901'  WHERE rule_code='pms_request';
UPDATE mk_number_rule SET current_seq=1,  current_date_str='20260905'  WHERE rule_code='pms_purchase';
UPDATE mk_number_rule SET current_seq=2,  current_date_str='20260901'  WHERE rule_code='pms_contract';
UPDATE mk_number_rule SET current_seq=1,  current_date_str='20260902'  WHERE rule_code='pms_receive';
UPDATE mk_number_rule SET current_seq=8,  current_date_str=''          WHERE rule_code='wms_supplier';
-- wms 仓储
UPDATE mk_number_rule SET current_seq=24, current_date_str=''          WHERE rule_code='wms_material';
UPDATE mk_number_rule SET current_seq=6,  current_date_str=''          WHERE rule_code='wms_warehouse';
-- mms 生产
UPDATE mk_number_rule SET current_seq=3,  current_date_str=''          WHERE rule_code='mms_bom';
UPDATE mk_number_rule SET current_seq=3,  current_date_str=''          WHERE rule_code='mms_route';
UPDATE mk_number_rule SET current_seq=10, current_date_str=''          WHERE rule_code='mms_process';
UPDATE mk_number_rule SET current_seq=6,  current_date_str=''          WHERE rule_code='mms_resource';
UPDATE mk_number_rule SET current_seq=2,  current_date_str=''          WHERE rule_code='mms_shift';
UPDATE mk_number_rule SET current_seq=3,  current_date_str=''          WHERE rule_code='mms_team';
-- dms 设备
UPDATE mk_number_rule SET current_seq=10, current_date_str=''          WHERE rule_code='dms_equipment';
UPDATE mk_number_rule SET current_seq=6,  current_date_str='20260901'  WHERE rule_code='dms_spare_part';
UPDATE mk_number_rule SET current_seq=2,  current_date_str='20260901'  WHERE rule_code='dms_inspection_route';
-- qms 质量
UPDATE mk_number_rule SET current_seq=10, current_date_str=''          WHERE rule_code='qms_std';
UPDATE mk_number_rule SET current_seq=6,  current_date_str=''          WHERE rule_code='qms_gauge';
UPDATE mk_number_rule SET current_seq=6,  current_date_str=''          WHERE rule_code='qms_defect';
-- safety 安全
UPDATE mk_number_rule SET current_seq=8,  current_date_str=''          WHERE rule_code='safety_area';
UPDATE mk_number_rule SET current_seq=6,  current_date_str='20260901'  WHERE rule_code='safety_risk_point';
UPDATE mk_number_rule SET current_seq=4,  current_date_str=''          WHERE rule_code='safety_equipment';
UPDATE mk_number_rule SET current_seq=3,  current_date_str='20260811'  WHERE rule_code='safety_emergency_plan';
UPDATE mk_number_rule SET current_seq=3,  current_date_str='20260811'  WHERE rule_code='safety_training_course';
-- hr 人力
UPDATE mk_number_rule SET current_seq=20, current_date_str=''          WHERE rule_code='hr_employee';

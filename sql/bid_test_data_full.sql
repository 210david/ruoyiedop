-- 招投标模块测试数据修复与丰富（UTF-8）
USE `ry-vue`;

-- 1. 修复乱码项目名
UPDATE bid_project SET project_name='冒烟测试-曲靖市某市政道路改造项目', tenderee='曲靖市麒麟区住房和城乡建设局' WHERE project_id=1;

-- 2. 补充跨月、多状态投标项目（覆盖各月趋势与状态分布）
INSERT INTO bid_project (bid_no, project_name, tenderee, region_code, industry, join_type, status, bid_amount, budget_amount, fail_reason, owner_name, create_time, del_flag) VALUES
('TB202605001','某县中医医院迁建项目监理服务','某县卫生健康局','云南','1','1','7',980000.00,1000000.00,NULL,'赵强',DATE_SUB(NOW(),INTERVAL 110 DAY),'0'),
('TB202605002','某区雨污分流管网改造工程','某区住房和城乡建设局','云南','2','1','8',5400000.00,5600000.00,'1','李雷',DATE_SUB(NOW(),INTERVAL 105 DAY),'0'),
('TB202606001','某学校运动场塑胶跑道工程','曲靖某学校','云南','1','1','7',1850000.00,2000000.00,NULL,'王芳',DATE_SUB(NOW(),INTERVAL 90 DAY),'0'),
('TB202606002','某单位办公楼物业管理服务采购','某机关事务管理局','云南','7','1','10',660000.00,700000.00,NULL,'韩梅',DATE_SUB(NOW(),INTERVAL 85 DAY),'0'),
('TB202606003','某水库灌区节水改造材料采购','某县水利局','云南','3','2','8',2750000.00,2900000.00,'2','李雷',DATE_SUB(NOW(),INTERVAL 80 DAY),'0'),
('TB202608003','某产业园标准化厂房建设工程','某工业园区开发公司','云南','1','1','6',12800000.00,13500000.00,NULL,'赵强',DATE_SUB(NOW(),INTERVAL 25 DAY),'0'),
('TB202609002','某医院检验科设备采购项目','某市人民医院','云南','5','1','5',3450000.00,3600000.00,NULL,'王芳',DATE_SUB(NOW(),INTERVAL 12 DAY),'0'),
('TB202609003','某镇人居环境提升改造工程','某镇人民政府','云南','6','1','4',2150000.00,2300000.00,NULL,'韩梅',DATE_SUB(NOW(),INTERVAL 7 DAY),'0'),
('TB202609004','某集团数字化车间智能化改造','曲靖某制造有限公司','云南','5','2','3',4980000.00,5200000.00,NULL,'赵强',DATE_SUB(NOW(),INTERVAL 4 DAY),'0'),
('TB202609005','某河道生态治理及景观工程','某县水务局','云南','6','1','2',7320000.00,7800000.00,NULL,'李雷',DATE_SUB(NOW(),INTERVAL 2 DAY),'0');

-- 3. 保证金台账（与项目关联）
INSERT INTO bid_deposit (project_id, bid_no, project_name, deposit_type, pay_type, amount, pay_time, expect_refund_time, refund_status, create_by, create_time) VALUES
(1,'TB202609210001','冒烟测试-曲靖市某市政道路改造项目','1','1',160000.00,DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_ADD(NOW(),INTERVAL 10 DAY),'0','admin',NOW()),
(6,'TB202608003','某产业园标准化厂房建设工程','1','1',250000.00,DATE_SUB(NOW(),INTERVAL 20 DAY),DATE_ADD(NOW(),INTERVAL 15 DAY),'0','admin',NOW()),
(8,'TB202609002','某医院检验科设备采购项目','2','2',68000.00,DATE_SUB(NOW(),INTERVAL 10 DAY),DATE_ADD(NOW(),INTERVAL 5 DAY),'0','admin',NOW()),
(2,'TB202608001','某县妇幼保健院设备采购项目','1','1',110000.00,DATE_SUB(NOW(),INTERVAL 38 DAY),DATE_SUB(NOW(),INTERVAL 5 DAY),'1','admin',DATE_SUB(NOW(),INTERVAL 38 DAY)),
(3,'TB202608002','曲靖某中学教学楼改造工程','1','1',24000.00,DATE_SUB(NOW(),INTERVAL 33 DAY),DATE_SUB(NOW(),INTERVAL 3 DAY),'2','admin',DATE_SUB(NOW(),INTERVAL 33 DAY)),
(4,'TB202607001','某工业园区道路硬化工程','1','1',170000.00,DATE_SUB(NOW(),INTERVAL 68 DAY),DATE_SUB(NOW(),INTERVAL 10 DAY),'3','admin',DATE_SUB(NOW(),INTERVAL 68 DAY));

-- 4. 资质资料库
INSERT INTO bid_qualification (qual_type, name, cert_no, qual_level, owner_type, owner_name, issue_date, expire_date, warn_days, create_by, create_time) VALUES
('1','建筑工程施工总承包一级','D353001111','一级','1','本公司',DATE_SUB(NOW(),INTERVAL 700 DAY),DATE_ADD(NOW(),INTERVAL 400 DAY),60,'admin',NOW()),
('1','市政公用工程施工总承包二级','D353002222','二级','1','本公司',DATE_SUB(NOW(),INTERVAL 500 DAY),DATE_ADD(NOW(),INTERVAL 25 DAY),60,'admin',NOW()),
('1','安全生产许可证','(滇)JZ安许2023-018','—','1','本公司',DATE_SUB(NOW(),INTERVAL 400 DAY),DATE_ADD(NOW(),INTERVAL 55 DAY),60,'admin',NOW()),
('2','一级建造师（市政）','云1350001','一级','2','赵强',DATE_SUB(NOW(),INTERVAL 900 DAY),DATE_ADD(NOW(),INTERVAL 250 DAY),60,'admin',NOW()),
('2','一级建造师（建筑）','云1350002','一级','2','李雷',DATE_SUB(NOW(),INTERVAL 800 DAY),DATE_ADD(NOW(),INTERVAL 45 DAY),60,'admin',NOW()),
('2','安全生产考核合格证（B本）','云建安B-2023-118','B本','2','王芳',DATE_SUB(NOW(),INTERVAL 350 DAY),DATE_ADD(NOW(),INTERVAL 130 DAY),60,'admin',NOW()),
('4','2025年度市级优质工程奖','YJ2025-036','市级','1','本公司',DATE_SUB(NOW(),INTERVAL 200 DAY),NULL,60,'admin',NOW()),
('1','水利水电工程施工总承包三级','D353003333','三级','1','本公司',DATE_SUB(NOW(),INTERVAL 600 DAY),DATE_ADD(NOW(),INTERVAL 15 DAY),60,'admin',NOW());

-- 5. 采集源
INSERT INTO bid_crawl_source (source_name, source_type, base_url, parser_code, crawl_interval, enabled, create_by, create_time) VALUES
('云南省政府采购网','1','https://www.yngp.com','common-html',120,'0','admin',NOW()),
('全国公共资源交易平台(云南)','1','https://ggzy.yn.gov.cn','common-html',180,'0','admin',NOW()),
('曲靖市公共资源交易中心','1','https://ggzy.qujing.gov.cn','common-html',240,'0','admin',NOW());

-- 6. 抓取日志（近几日，含成功/部分/失败）
INSERT INTO bid_crawl_log (source_id, source_name, run_status, fetched_count, new_count, hit_count, start_time, end_time, error_msg, create_time) VALUES
(1,'云南省政府采购网','1',126,8,3,DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 1 DAY),INTERVAL 4 MINUTE),NULL,NOW()),
(2,'全国公共资源交易平台(云南)','1',89,5,2,DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 1 DAY),INTERVAL 3 MINUTE),NULL,NOW()),
(3,'曲靖市公共资源交易中心','1',34,2,1,DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 1 DAY),INTERVAL 2 MINUTE),NULL,NOW()),
(1,'云南省政府采购网','2',97,4,1,DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 2 DAY),INTERVAL 4 MINUTE),'部分页面超时跳过',NOW()),
(2,'全国公共资源交易平台(云南)','0',0,0,0,DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 2 DAY),INTERVAL 1 MINUTE),'连接超时:Read timed out',NOW()),
(1,'云南省政府采购网','1',132,10,4,DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 3 DAY),INTERVAL 5 MINUTE),NULL,NOW());

-- 7. 关键词订阅
INSERT INTO bid_crawl_keyword (keyword, synonym_words, exclude_words, match_type, region_code, industry, min_amount, notice_types, notify_enabled, enabled, create_by, create_time) VALUES
('市政道路','市政工程,道路改造','养护',2,'云南','2',1000000.00,'1,2,3',1,'0','admin',NOW()),
('水利水电','水库,灌区,除险加固','',2,'云南','3',2000000.00,'1,2',1,'0','admin',NOW()),
('设备采购','检验科,医疗设备','耗材',1,'云南','5',500000.00,'2,5',1,'0','admin',NOW()),
('装修装饰','改造,翻新','家庭装修',1,'云南','4',NULL,'2',0,'1','admin',NOW());

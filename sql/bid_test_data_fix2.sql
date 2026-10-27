-- 抓取日志与关键词补充（字段名对齐实际表结构）
USE `ry-vue`;
INSERT INTO bid_crawl_log (source_id, source_name, run_status, fetched_count, inserted_count, matched_count, start_time, end_time, error_msg) VALUES
(1,'云南省政府采购网','1',126,8,3,DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 1 DAY),INTERVAL 4 MINUTE),NULL),
(2,'全国公共资源交易平台(云南)','1',89,5,2,DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 1 DAY),INTERVAL 3 MINUTE),NULL),
(3,'曲靖市公共资源交易中心','1',34,2,1,DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 1 DAY),INTERVAL 2 MINUTE),NULL),
(1,'云南省政府采购网','2',97,4,1,DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 2 DAY),INTERVAL 4 MINUTE),'部分页面超时跳过'),
(2,'全国公共资源交易平台(云南)','0',0,0,0,DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 2 DAY),INTERVAL 1 MINUTE),'连接超时:Read timed out'),
(1,'云南省政府采购网','1',132,10,4,DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 3 DAY),INTERVAL 5 MINUTE),NULL);

INSERT INTO bid_crawl_keyword (keyword, synonym_words, exclude_words, match_type, region_code, industry, min_amount, notice_types, notify_enabled, enabled, create_by, create_time) VALUES
('市政道路','市政工程,道路改造','养护',2,'云南','2',1000000.00,'1,2,3',1,'0','admin',NOW()),
('水利水电','水库,灌区,除险加固','',2,'云南','3',2000000.00,'1,2',1,'0','admin',NOW()),
('设备采购','检验科,医疗设备','耗材',1,'云南','5',500000.00,'2,5',1,'0','admin',NOW()),
('装修装饰','改造,翻新','家庭装修',1,'云南','4',NULL,'2',0,'1','admin',NOW());

-- =============================================================
-- 升级脚本: upgrade_20261117_qms_mk_data.sql
-- 主题: 质量检验任务/明细字段扩展 + 字典项补充 + 营销客户与合同、资质数据
-- 幂等: 所有 DDL 均先查 information_schema 再执行，字典/数据均带存在性判断，可重复执行
-- 目标库: ruoyi_ylsp
-- 执行: mysql -uroot -p --default-character-set=utf8mb4 ruoyi_ylsp < 本文件
-- 日期: 2026-11-17
-- =============================================================

SET NAMES utf8mb4;

-- -------------------------------------------------------------
-- 一、结构升级：qms_insp_task 新增 11 列
-- -------------------------------------------------------------
SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `qms_insp_task` ADD COLUMN `produce_date` date NULL COMMENT ''生产日期''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='qms_insp_task' AND column_name='produce_date');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `qms_insp_task` ADD COLUMN `report_date` date NULL COMMENT ''报告日期''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='qms_insp_task' AND column_name='report_date');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `qms_insp_task` ADD COLUMN `qty_unit` varchar(32) NULL COMMENT ''数量单位''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='qms_insp_task' AND column_name='qty_unit');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `qms_insp_task` ADD COLUMN `package_spec` varchar(64) NULL COMMENT ''包装规格''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='qms_insp_task' AND column_name='package_spec');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `qms_insp_task` ADD COLUMN `inspect_basis` varchar(500) NULL COMMENT ''检验依据''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='qms_insp_task' AND column_name='inspect_basis');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `qms_insp_task` ADD COLUMN `conclusion` varchar(500) NULL COMMENT ''检验结论''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='qms_insp_task' AND column_name='conclusion');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `qms_insp_task` ADD COLUMN `inspect_org` varchar(255) NULL COMMENT ''检验机构''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='qms_insp_task' AND column_name='inspect_org');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `qms_insp_task` ADD COLUMN `reviewer_name` varchar(64) NULL COMMENT ''复核人''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='qms_insp_task' AND column_name='reviewer_name');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `qms_insp_task` ADD COLUMN `report_no` varchar(64) NULL COMMENT ''第三方报告编号''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='qms_insp_task' AND column_name='report_no');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `qms_insp_task` ADD COLUMN `report_file_url` varchar(1000) NULL COMMENT ''检验报告附件URL(逗号分隔)''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='qms_insp_task' AND column_name='report_file_url');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `qms_insp_task` ADD COLUMN `report_file_name` varchar(1000) NULL COMMENT ''检验报告附件名称(逗号分隔)''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='qms_insp_task' AND column_name='report_file_name');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- -------------------------------------------------------------
-- 二、结构升级：qms_insp_item 新增 3 列
-- -------------------------------------------------------------
SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `qms_insp_item` ADD COLUMN `unit` varchar(32) NULL COMMENT ''单位''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='qms_insp_item' AND column_name='unit');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `qms_insp_item` ADD COLUMN `std_value` varchar(255) NULL COMMENT ''标准规定''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='qms_insp_item' AND column_name='std_value');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `qms_insp_item` ADD COLUMN `inspect_method` varchar(128) NULL COMMENT ''检验方法''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='qms_insp_item' AND column_name='inspect_method');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- -------------------------------------------------------------
-- 三、字典项补充
-- -------------------------------------------------------------
INSERT INTO sys_dict_data (dict_sort,dict_label,dict_value,dict_type,css_class,list_class,is_default,status,create_by,create_time,remark)
SELECT 0,'无缺陷','0','qms_defect_level','','success','N','0','admin',NOW(),'2026-11-17 缺陷等级补充'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qms_defect_level' AND dict_value='0');

INSERT INTO sys_dict_data (dict_sort,dict_label,dict_value,dict_type,css_class,list_class,is_default,status,create_by,create_time,remark)
SELECT 3,'试制','3','mms_order_type','','info','N','0','admin',NOW(),'2026-11-17 工单类型补充'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='mms_order_type' AND dict_value='3');

INSERT INTO sys_dict_data (dict_sort,dict_label,dict_value,dict_type,css_class,list_class,is_default,status,create_by,create_time,remark)
SELECT 7,'食品生产许可证','7','safety_license_type','','warning','N','0','admin',NOW(),'2026-11-17 证照类型补充'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='safety_license_type' AND dict_value='7');

-- -------------------------------------------------------------
-- 四、业务数据：客户 / 合同 / 企业档案 / 证照 / 招投标资质
-- -------------------------------------------------------------
-- 4.1 客户档案：上海滇胤（补地址/电话）、富源永胤（新增）

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/*!40000 ALTER TABLE `mk_customer` DISABLE KEYS */;
INSERT  IGNORE INTO `mk_customer` (`customer_id`, `customer_no`, `customer_name`, `credit_code`, `customer_level`, `customer_source`, `industry`, `company_size`, `province`, `city`, `district`, `address`, `website`, `customer_status`, `first_contact_date`, `cooperation_date`, `total_amount`, `user_id`, `dept_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `credit_limit`, `credit_days`, `credit_status`, `customer_type`, `parent_customer_id`, `contact_phone`) VALUES (9,'KH2026000009','上海滇胤贸易有限公司',NULL,'5','4','5',NULL,'上海市','上海市','宝山区','上海市宝山区克山路12号',NULL,'2','2026-01-14','2026-01-14',1501951.00,NULL,106,'0','admin','2026-11-13 19:10:52','admin','2026-11-17 10:09:24','销售清单导入',500000.00,30,'0','2',NULL,'15021109676');
INSERT  IGNORE INTO `mk_customer` (`customer_id`, `customer_no`, `customer_name`, `credit_code`, `customer_level`, `customer_source`, `industry`, `company_size`, `province`, `city`, `district`, `address`, `website`, `customer_status`, `first_contact_date`, `cooperation_date`, `total_amount`, `user_id`, `dept_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `credit_limit`, `credit_days`, `credit_status`, `customer_type`, `parent_customer_id`, `contact_phone`) VALUES (12,'KH2026000012','富源永胤商贸有限公司',NULL,'3','0','5',NULL,'云南省','曲靖市','富源县','富源县中安街道金城路280号',NULL,'0','2026-01-01','2026-01-01',0.00,NULL,106,'0','admin','2026-11-17 10:12:54','',NULL,'档案来源于《永胤-赢隆销售合同》（2026-01-01~2027-12-30）',0.00,30,'0','1',NULL,NULL);
/*!40000 ALTER TABLE `mk_customer` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


-- 4.2 营销合同：HT20260004 供货合同（滇胤）、HT20260005 销售合同（永胤）

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/*!40000 ALTER TABLE `mk_contract` DISABLE KEYS */;
INSERT  IGNORE INTO `mk_contract` (`contract_id`, `contract_no`, `contract_name`, `contract_type`, `customer_id`, `opportunity_id`, `contract_amount`, `sign_date`, `effective_date`, `expire_date`, `payment_method`, `contract_status`, `user_id`, `dept_id`, `attachment`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (13,'HT20260004','2025-2028年度供货合同（上海滇胤贸易有限公司）','0',9,NULL,0.00,'2025-11-10','2025-11-10','2028-11-09','0','2',NULL,106,'/profile/upload/2026/11/17/赢隆-滇胤供货合同_20261117100830A002.pdf','0','admin','2026-11-17 10:08:30','',NULL,'框架式供货合同，无固定总金额，按月度采购单对账结算；甲方收到增值税专用发票后30日内付款；授权甲方使用“鲜丰旺”“拌尚菌”商标；收款账户：中国工商银行富源县支行 2505065509200166192；期满未续签自动延长一年。');
INSERT  IGNORE INTO `mk_contract` (`contract_id`, `contract_no`, `contract_name`, `contract_type`, `customer_id`, `opportunity_id`, `contract_amount`, `sign_date`, `effective_date`, `expire_date`, `payment_method`, `contract_status`, `user_id`, `dept_id`, `attachment`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (14,'HT20260005','2026-2027年度销售合同（富源永胤商贸有限公司）','0',12,NULL,0.00,'2026-01-01','2026-01-01','2027-12-30','0','2',NULL,106,'/profile/upload/2026/11/17/永胤-赢隆销售合同_20261117101254A003.pdf','0','admin','2026-11-17 10:12:54','',NULL,'框架式销售合同，无固定总金额，按月度采购单对账结算；甲方收到增值税专用发票后30日内付款；期满未续签自动延长一年。注：正文写“期限为叁年”，但起止日期为2026-01-01至2027-12-30，以日期为准。');
/*!40000 ALTER TABLE `mk_contract` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


-- 4.3 企业档案（安全生产-企业信息）

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/*!40000 ALTER TABLE `biz_safety_enterprise` DISABLE KEYS */;
INSERT  IGNORE INTO `biz_safety_enterprise` (`enterprise_id`, `enterprise_name`, `credit_code`, `industry_type`, `is_high_risk`, `legal_person`, `safety_manager_id`, `safety_manager`, `safety_org`, `responsibility`, `supervise_dept`, `plan_image`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1,'云南赢隆食品加工有限公司','91530325MA7K3QA92L','5','0','丰孟',20,'邓楠','安全生产办公室','总经理（法定代表人）为本单位安全生产与食品安全第一责任人；设安全生产办公室并配备专职安全管理人员，实行“一岗双责”，各车间负责人为本区域直接责任人，逐级签订安全生产责任书。','富源县应急管理局',NULL,'0','0','admin','2026-11-17 09:40:11','',NULL,'统一社会信用代码 91530325MA7K3QA92L；类型：有限责任公司；注册资本壹仟万元整；成立日期 2022-03-10。经营范围：许可项目—食品生产、食品销售、牲畜饲养；一般项目—鲜肉批发，农产品的生产、销售、加工、运输、贮藏及其他相关服务，食用农产品批发，牲畜销售，畜禽收购。资料来源：《营业执照》（2024-04-03 换发）、食品生产许可证 SC10453032500205。');
/*!40000 ALTER TABLE `biz_safety_enterprise` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


-- 4.4 企业证照（营业执照 / 食品生产许可证）

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/*!40000 ALTER TABLE `biz_safety_cert` DISABLE KEYS */;
INSERT  IGNORE INTO `biz_safety_cert` (`cert_id`, `cert_name`, `cert_no`, `cert_type`, `issue_org`, `issue_date`, `expire_date`, `remind_status`, `enterprise_id`, `attachment`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1,'营业执照','91530325MA7K3QA92L','4','富源县行政审批局','2024-04-03','2099-12-31','0',1,'/profile/upload/2026/11/17/赢隆资质（营业执照、生产许可）.pdf','0','0','admin','2026-11-17 09:40:11','',NULL,'类型：有限责任公司；注册资本壹仟万元整；成立日期 2022-03-10；住所：云南省曲靖市富源县胜境街道后煤矿社区煤炭湾生活区；营业期限：长期（证照副本未标注终止日期）。');
INSERT  IGNORE INTO `biz_safety_cert` (`cert_id`, `cert_name`, `cert_no`, `cert_type`, `issue_org`, `issue_date`, `expire_date`, `remind_status`, `enterprise_id`, `attachment`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2,'食品生产许可证','SC10453032500205','7','曲靖市市场监督管理局','2025-07-18','2027-10-30','0',1,'/profile/upload/2026/11/17/赢隆资质（营业执照、生产许可）.pdf','0','0','admin','2026-11-17 09:40:11','',NULL,'食品类别：调味品、肉制品。品种明细：1. 调味品 0305 调味料—半固态(酱)调味料[复合调味酱]；2. 肉制品 0401 热加工熟肉制品—酱卤肉制品[酱卤肉类]、热加工肉灌制品[灌肠类]；3. 肉制品 0402 发酵肉制品—发酵灌制品；4. 肉制品 0404 腌腊肉制品—腌腊肉灌制品、腊肉制品、火腿制品。');
/*!40000 ALTER TABLE `biz_safety_cert` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


-- 4.5 招投标资质（与证照同一批扫描件）

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/*!40000 ALTER TABLE `bid_qualification` DISABLE KEYS */;
INSERT  IGNORE INTO `bid_qualification` (`qualification_id`, `qual_type`, `name`, `cert_no`, `qual_level`, `owner_type`, `owner_user_id`, `owner_name`, `issue_date`, `expire_date`, `warn_days`, `file_url`, `dept_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (11,'1','营业执照','91530325MA7K3QA92L','有限责任公司','1',NULL,NULL,'2024-04-03',NULL,60,'/profile/upload/2026/11/17/赢隆资质（营业执照、生产许可）.pdf',103,'0','admin','2026-11-17 09:46:01','',NULL,'登记机关：富源县行政审批局；注册资本壹仟万元整；成立日期 2022-03-10；住所：云南省曲靖市富源县胜境街道后煤矿社区煤炭湾生活区；法定代表人：丰孟。原件为《赢隆资质（营业执照、生产许可）.pdf》第 1 页。');
INSERT  IGNORE INTO `bid_qualification` (`qualification_id`, `qual_type`, `name`, `cert_no`, `qual_level`, `owner_type`, `owner_user_id`, `owner_name`, `issue_date`, `expire_date`, `warn_days`, `file_url`, `dept_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (12,'1','食品生产许可证（SC）','SC10453032500205','调味品、肉制品','1',NULL,NULL,'2025-07-18','2027-10-30',90,'/profile/upload/2026/11/17/赢隆资质（营业执照、生产许可）.pdf',103,'0','admin','2026-11-17 09:46:01','',NULL,'发证机关：曲靖市市场监督管理局。许可品种：0305 半固态(酱)调味料[复合调味酱]；0401 酱卤肉制品[酱卤肉类]、热加工肉灌制品[灌肠类]；0402 发酵灌制品；0404 腌腊肉灌制品、腊肉制品、火腿制品。原件为《赢隆资质（营业执照、生产许可）.pdf》第 2-4 页。');
/*!40000 ALTER TABLE `bid_qualification` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;



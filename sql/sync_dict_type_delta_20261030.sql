-- MySQL dump 10.13  Distrib 8.0.28, for Win64 (x86_64)
--
-- Host: localhost    Database: ruoyi_cfsp
-- ------------------------------------------------------
-- Server version	8.0.28

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

--
-- Dumping data for table `sys_dict_type`
--
-- WHERE:  dict_type IN ('food_adjust_status','food_adjust_type','food_batch_status','food_ccp_judge','food_ccp_limit_type','food_ccp_monitor_type','food_cert_type','food_expire_status','food_loss_type','food_mold_status','food_recall_level','food_recall_scope','food_recall_status','food_relation_type','food_reserve_status','food_schedule_mode','food_source_type','food_wip_status','food_yield_model','mk_bid_status','mk_credit_status','mk_price_type','mk_rebate_basis','mk_rebate_period','mk_rebate_settle_status')

LOCK TABLES `sys_dict_type` WRITE;
/*!40000 ALTER TABLE `sys_dict_type` DISABLE KEYS */;
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `dict_category`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (593,'调整单状态','food_adjust_status','system','0','admin','2026-10-13 19:13:16','',NULL,'批次调整单状态'),(592,'批次调整类型','food_adjust_type','system','0','admin','2026-10-13 19:13:16','',NULL,'批次调整单类型'),(588,'批次状态','food_batch_status','system','0','admin','2026-10-13 19:13:16','',NULL,'食品批次状态'),(605,'CCP判定结果','food_ccp_judge','system','0','admin','2026-10-14 15:48:18','',NULL,'CCP记录判定结果'),(604,'CCP限值类型','food_ccp_limit_type','system','0','admin','2026-10-14 15:48:18','',NULL,'关键限值校验方式'),(603,'CCP监控项类型','food_ccp_monitor_type','system','0','admin','2026-10-14 15:48:18','',NULL,'HACCP关键控制点监控项类型'),(606,'检验索证类型','food_cert_type','system','0','admin','2026-10-14 15:48:18','',NULL,'索证索票附件证件类型'),(589,'效期状态','food_expire_status','system','0','admin','2026-10-13 19:13:16','',NULL,'食品批次效期状态'),(600,'损耗类型','food_loss_type','system','0','admin','2026-10-14 11:16:14','',NULL,'食品生产损耗类型'),(599,'上霉状态','food_mold_status','system','0','admin','2026-10-14 11:16:14','',NULL,'干腌火腿上霉状态'),(596,'召回等级','food_recall_level','system','0','admin','2026-10-14 09:55:04','',NULL,'食品批次召回等级'),(595,'召回范围','food_recall_scope','system','0','admin','2026-10-14 09:55:04','',NULL,'食品批次召回范围'),(597,'召回单状态','food_recall_status','system','0','admin','2026-10-14 09:55:04','',NULL,'食品批次召回单状态'),(591,'批次谱系关系','food_relation_type','system','0','admin','2026-10-13 19:13:16','',NULL,'批次谱系关系类型'),(594,'批次预留状态','food_reserve_status','system','0','admin','2026-10-13 19:13:16','',NULL,'批次预留状态'),(601,'排产模式','food_schedule_mode','system','0','admin','2026-10-14 11:16:14','',NULL,'按物料/产线配置的排产模式'),(590,'批次来源','food_source_type','system','0','admin','2026-10-13 19:13:16','',NULL,'食品批次来源类型'),(598,'在制状态','food_wip_status','system','0','admin','2026-10-14 11:16:14','',NULL,'在制品台账状态'),(602,'配方模型','food_yield_model','system','0','admin','2026-10-14 11:16:14','',NULL,'产出率模型/配比配方双模型'),(607,'投标状态','mk_bid_status','system','0','admin','2026-10-14 18:45:27','',NULL,'P2批次六'),(612,'客户信用状态','mk_credit_status','system','0','admin','2026-10-14 18:47:10','',NULL,'P2批次六'),(608,'价格类型','mk_price_type','system','0','admin','2026-10-14 18:45:27','',NULL,'P2批次六'),(609,'返利考核口径','mk_rebate_basis','system','0','admin','2026-10-14 18:47:10','',NULL,'P2批次六'),(610,'返利周期类型','mk_rebate_period','system','0','admin','2026-10-14 18:47:10','',NULL,'P2批次六'),(611,'返利结算状态','mk_rebate_settle_status','system','0','admin','2026-10-14 18:47:10','',NULL,'P2批次六');
/*!40000 ALTER TABLE `sys_dict_type` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-30  9:58:57

-- MySQL dump 10.13  Distrib 8.0.28, for Win64 (x86_64)
--
-- Host: localhost    Database: ry-vue
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
-- Current Database: `ry-vue`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `ry-vue` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `ry-vue`;

--
-- Table structure for table `dms_data_collection_config`
--

DROP TABLE IF EXISTS `dms_data_collection_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dms_data_collection_config` (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_name` varchar(255) DEFAULT NULL COMMENT '设备名称（冗余）',
  `collect_mode` varchar(10) DEFAULT '0' COMMENT '采集模式（字典 dms_collect_mode：0人工 1自动）',
  `collect_status` varchar(10) DEFAULT '1' COMMENT '运行状态采集（0关闭 1开启）',
  `collect_hours` varchar(10) DEFAULT '1' COMMENT '运行小时数采集（0关闭 1开启）',
  `collect_count` varchar(10) DEFAULT '0' COMMENT '加工件数采集（0关闭 1开启）',
  `param1_name` varchar(64) DEFAULT NULL COMMENT '自定义参数1名称',
  `param1_unit` varchar(20) DEFAULT NULL COMMENT '自定义参数1单位',
  `param1_alarm_min` decimal(18,2) DEFAULT NULL COMMENT '参数1告警下限',
  `param1_alarm_max` decimal(18,2) DEFAULT NULL COMMENT '参数1告警上限',
  `param2_name` varchar(64) DEFAULT NULL COMMENT '自定义参数2名称',
  `param2_unit` varchar(20) DEFAULT NULL COMMENT '自定义参数2单位',
  `param2_alarm_min` decimal(18,2) DEFAULT NULL COMMENT '参数2告警下限',
  `param2_alarm_max` decimal(18,2) DEFAULT NULL COMMENT '参数2告警上限',
  `param3_name` varchar(64) DEFAULT NULL COMMENT '自定义参数3名称',
  `param3_unit` varchar(20) DEFAULT NULL COMMENT '自定义参数3单位',
  `param3_alarm_min` decimal(18,2) DEFAULT NULL COMMENT '参数3告警下限',
  `param3_alarm_max` decimal(18,2) DEFAULT NULL COMMENT '参数3告警上限',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`),
  KEY `idx_dcc_equipment` (`equipment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备数据采集配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dms_data_collection_config`
--

LOCK TABLES `dms_data_collection_config` WRITE;
/*!40000 ALTER TABLE `dms_data_collection_config` DISABLE KEYS */;
INSERT INTO `dms_data_collection_config` VALUES (1,6,'数控加工中心','0','1','1','1','温度','℃',40.00,90.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0','0',NULL,'2026-07-16 21:42:58','','2026-07-16 21:44:07',NULL),(2,1,NULL,'0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0','2',NULL,'2026-07-17 18:13:34','','2026-07-17 18:14:22',NULL);
/*!40000 ALTER TABLE `dms_data_collection_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dms_data_record`
--

DROP TABLE IF EXISTS `dms_data_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dms_data_record` (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_code` varchar(100) DEFAULT NULL COMMENT '????',
  `equipment_name` varchar(255) DEFAULT NULL COMMENT '设备名称（冗余）',
  `run_status` varchar(10) DEFAULT NULL COMMENT '运行状态（0运行 1停机 2故障）',
  `run_hours` decimal(18,2) DEFAULT NULL COMMENT '运行小时数',
  `product_count` decimal(18,2) DEFAULT NULL COMMENT '加工件数',
  `param1_value` decimal(18,2) DEFAULT NULL COMMENT '参数1值',
  `param2_value` decimal(18,2) DEFAULT NULL COMMENT '参数2值',
  `param3_value` decimal(18,2) DEFAULT NULL COMMENT '参数3值',
  `collect_time` datetime DEFAULT NULL COMMENT '采集时间',
  `collect_mode` varchar(10) DEFAULT NULL COMMENT '采集方式（0人工 1自动）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '录入人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '???',
  `update_time` datetime DEFAULT NULL COMMENT '????',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`record_id`),
  KEY `idx_dr_equipment` (`equipment_id`),
  KEY `idx_dr_time` (`collect_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备数据采集记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dms_data_record`
--

LOCK TABLES `dms_data_record` WRITE;
/*!40000 ALTER TABLE `dms_data_record` DISABLE KEYS */;
INSERT INTO `dms_data_record` VALUES (1,1,'ed200001','机床01','1',200.00,999.00,7.00,NULL,NULL,'2026-07-16 21:45:29','0',NULL,'2026-07-16 21:45:35','admin','2026-07-17 22:51:48',NULL);
/*!40000 ALTER TABLE `dms_data_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dms_equipment`
--

DROP TABLE IF EXISTS `dms_equipment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dms_equipment` (
  `equipment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `equipment_code` varchar(64) NOT NULL COMMENT '设备编号（唯一标识）',
  `equipment_name` varchar(255) NOT NULL COMMENT '设备名称',
  `category_id` bigint DEFAULT NULL COMMENT '设备分类ID',
  `model` varchar(128) DEFAULT NULL COMMENT '型号',
  `serial_number` varchar(128) DEFAULT NULL COMMENT '序列号',
  `manufacturer` varchar(255) DEFAULT NULL COMMENT '制造商',
  `supplier` varchar(255) DEFAULT NULL COMMENT '供应商',
  `purchase_date` date DEFAULT NULL COMMENT '购置日期',
  `original_value` decimal(18,2) DEFAULT NULL COMMENT '原值',
  `dept_id` bigint DEFAULT NULL COMMENT '使用部门ID',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '使用部门名称',
  `install_location` varchar(255) DEFAULT NULL COMMENT '安装位置',
  `equipment_status` varchar(10) DEFAULT '0' COMMENT '设备状态（字典 dms_equipment_status）',
  `responsible_id` bigint DEFAULT NULL COMMENT '责任人ID',
  `responsible_name` varchar(64) DEFAULT NULL COMMENT '责任人名称',
  `equipment_image` varchar(500) DEFAULT NULL COMMENT '设备图片URL',
  `attachment_url` varchar(1000) DEFAULT NULL COMMENT '设备附件资料URL',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`equipment_id`),
  UNIQUE KEY `uk_equipment_code` (`equipment_code`),
  KEY `idx_equipment_category` (`category_id`),
  KEY `idx_equipment_status` (`equipment_status`),
  KEY `idx_equipment_dept` (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备台账表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dms_equipment`
--

LOCK TABLES `dms_equipment` WRITE;
/*!40000 ALTER TABLE `dms_equipment` DISABLE KEYS */;
INSERT INTO `dms_equipment` VALUES (1,'ed200001','机床01',1,'222','22','22','22','2026-07-01',NULL,103,NULL,'3333','0',1,NULL,'/profile/upload/2026/07/15/Surface_Laptop_3_07_20260715152552A001.jpg','/profile/upload/2026/07/15/Surface_Laptop_3_06_20260715152557A002.jpg,/profile/upload/2026/07/15/河南郑许轨道交通有限公司内网优化项目需求_20260715152606A003.xlsx,/profile/upload/2026/07/15/河南郑许轨道交通有限公司内网优化项目需求书_20260715152609A004.docx,/profile/upload/2026/07/15/26527000000076026536_1(1)_20260715155515A005.pdf','0','0',NULL,'2026-07-15 15:26:41','','2026-07-15 15:55:17',NULL),(2,'EQ001','CNC数控车床',2,'CK6150','SN2024001','沈阳机床','若依科技','2024-01-15',150000.00,103,'研发部门','A车间-01工位','0',1,'张三',NULL,NULL,'0','0','admin',NULL,'',NULL,NULL),(3,'EQ002','立式铣床',3,'X5032','SN2024002','北京一机','若依科技','2024-02-20',85000.00,103,'研发部门','A车间-02工位','0',1,'张三',NULL,NULL,'0','0','admin',NULL,'',NULL,NULL),(4,'EQ003','摇臂钻床',4,'Z3050','SN2024003','中捷机床','若依科技','2024-03-10',68000.00,104,'市场部门','B车间-01工位','0',2,'李四',NULL,NULL,'0','0','admin',NULL,'',NULL,NULL),(5,'EQ004','平面磨床',5,'M7130','SN2024004','上海机床','若依科技','2024-04-05',120000.00,103,'研发部门','C车间-01工位','0',1,'张三',NULL,NULL,'0','0','admin',NULL,'',NULL,NULL),(6,'EQ005','数控加工中心',1,'VMC850','SN2024005','华中数控','若依科技','2024-05-15',320000.00,103,'研发部门','A车间-03工位','0',1,'张三',NULL,NULL,'0','0','admin',NULL,'',NULL,NULL),(7,'TEST-EQ-T','测试设备T',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0',NULL,NULL,NULL,NULL,'0','0',NULL,'2026-07-17 17:52:15','',NULL,NULL);
/*!40000 ALTER TABLE `dms_equipment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dms_equipment_category`
--

DROP TABLE IF EXISTS `dms_equipment_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dms_equipment_category` (
  `category_id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父级ID',
  `ancestors` varchar(500) DEFAULT NULL COMMENT '祖级列表',
  `category_code` varchar(64) NOT NULL COMMENT '分类编码',
  `category_name` varchar(128) NOT NULL COMMENT '分类名称',
  `category_level` int DEFAULT '1' COMMENT '分类层级（1-4级）',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_category_parent` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备分类表（树形结构）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dms_equipment_category`
--

LOCK TABLES `dms_equipment_category` WRITE;
/*!40000 ALTER TABLE `dms_equipment_category` DISABLE KEYS */;
INSERT INTO `dms_equipment_category` VALUES (1,0,'0','A','金属切削机床',1,1,'0','0','admin','2026-07-15 12:42:45','',NULL,'金属切削加工设备'),(2,1,'0,1','A01','车床',2,1,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(3,1,'0,1','A02','铣床',2,2,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(4,1,'0,1','A03','钻床',2,3,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(5,1,'0,1','A04','磨床',2,4,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(6,1,'0,1','A05','数控机床',2,5,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(7,0,'0','B','锻压设备',1,2,'0','0','admin','2026-07-15 12:42:45','',NULL,'锻压加工设备'),(8,7,'0,7','B01','冲床',2,1,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(9,7,'0,7','B02','剪板机',2,2,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(10,7,'0,7','B03','折弯机',2,3,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(11,0,'0','C','动力设备',1,3,'0','0','admin','2026-07-15 12:42:45','',NULL,'动力供应设备'),(12,11,'0,11','C01','空压机',2,1,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(13,11,'0,11','C02','变压器',2,2,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(14,11,'0,11','C03','发电机',2,3,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(15,0,'0','D','起重运输设备',1,4,'0','0','admin','2026-07-15 12:42:45','',NULL,'物料搬运设备'),(16,15,'0,15','D01','行车',2,1,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(17,15,'0,15','D02','叉车',2,2,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(18,15,'0,15','D03','输送带',2,3,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(19,0,'0','E','检测仪器',1,5,'0','0','admin','2026-07-15 12:42:45','',NULL,'检测测量设备'),(20,19,'0,19','E01','三坐标测量仪',2,1,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(21,19,'0,19','E02','光谱仪',2,2,'0','0','admin','2026-07-15 12:42:45','',NULL,''),(22,0,'0','F','其他设备',1,6,'0','0','admin','2026-07-15 12:42:45','',NULL,'其他工业设备'),(23,0,'0','h','hhhhh',1,0,'0','2',NULL,'2026-07-15 14:48:42','',NULL,NULL),(24,23,'0,23','hh2','h2',2,0,'0','2',NULL,'2026-07-15 14:48:53','',NULL,NULL),(26,0,'0','TEST-CAT-DB','TestFromDB',1,94,'0','2','admin','2026-07-17 18:04:46','',NULL,'test');
/*!40000 ALTER TABLE `dms_equipment_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dms_equipment_log`
--

DROP TABLE IF EXISTS `dms_equipment_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dms_equipment_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_code` varchar(64) DEFAULT NULL COMMENT '设备编号（冗余）',
  `equipment_name` varchar(255) DEFAULT NULL COMMENT '设备名称（冗余）',
  `change_type` varchar(20) NOT NULL COMMENT '变更类型（字典 dms_eq_change_type）',
  `old_value` varchar(500) DEFAULT NULL COMMENT '原值',
  `new_value` varchar(500) DEFAULT NULL COMMENT '新值',
  `change_reason` varchar(500) DEFAULT NULL COMMENT '变更原因',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人名称',
  `create_time` datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_eqlog_equipment` (`equipment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备状态变更日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dms_equipment_log`
--

LOCK TABLES `dms_equipment_log` WRITE;
/*!40000 ALTER TABLE `dms_equipment_log` DISABLE KEYS */;
INSERT INTO `dms_equipment_log` VALUES (1,8,'TEST-EQ-C','TestEqC','0','0','1',NULL,NULL,'admin','2026-07-17 18:08:14');
/*!40000 ALTER TABLE `dms_equipment_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dms_inspection_route`
--

DROP TABLE IF EXISTS `dms_inspection_route`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dms_inspection_route` (
  `route_id` bigint NOT NULL AUTO_INCREMENT COMMENT '路线ID',
  `route_name` varchar(255) NOT NULL COMMENT '路线名称',
  `route_code` varchar(64) DEFAULT NULL COMMENT '路线编码',
  `dept_id` bigint DEFAULT NULL COMMENT '归属部门ID',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '归属部门名称',
  `cycle_type` varchar(10) DEFAULT '0' COMMENT '周期类型（字典 dms_inspection_cycle）',
  `cycle_value` int DEFAULT '1' COMMENT '周期值',
  `equipment_ids` text COMMENT '巡检设备ID列表（JSON数组）',
  `inspection_items` text COMMENT '点检项（JSON数组，如[{item:"检查温度",type:"check"},{item:"记录压力",type:"number",unit:"MPa"}]）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`route_id`),
  UNIQUE KEY `uk_route_code` (`route_code`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点检路线表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dms_inspection_route`
--

LOCK TABLES `dms_inspection_route` WRITE;
/*!40000 ALTER TABLE `dms_inspection_route` DISABLE KEYS */;
INSERT INTO `dms_inspection_route` VALUES (1,'tttttttt1','ttttttt1',NULL,'cccc','0',1,'[6]','[{\"item\":\"温度检查\",\"type\":\"text\",\"unit\":\"\",\"abnormalRequired\":true},{\"item\":\"压力检查\",\"type\":\"text\",\"unit\":\"\",\"abnormalRequired\":false}]','0','2',NULL,'2026-07-17 14:24:49','',NULL,'tttttttttttttttt'),(2,'ffffffffffff','RT20260717001',104,'市场部门','0',1,'[6]','[{\"item\":\"dddddddddd\",\"type\":\"check\",\"unit\":\"\",\"abnormalRequired\":true}]','0','2',NULL,'2026-07-17 14:28:43','','2026-07-17 14:30:03',NULL),(9,'yyyy','RT20260717002',105,'测试部门','0',1,'[6]','[{\"item\":\"yyyyyyyyyyy\",\"type\":\"check\",\"unit\":\"\",\"abnormalRequired\":true},{\"item\":\"uuuuuuuuuu\",\"type\":\"number\",\"unit\":\"f\",\"abnormalRequired\":true},{\"item\":\"iiiiiiiii\",\"type\":\"text\",\"unit\":\"\",\"abnormalRequired\":true}]','0','2',NULL,'2026-07-17 14:50:30','',NULL,NULL),(10,'yyyyyyyyyyyyyyy','RT20260717003',105,'测试部门','0',1,'[5,3]','[{\"item\":\"yy11\",\"type\":\"number\",\"unit\":\"\",\"abnormalRequired\":true},{\"item\":\"yyy2\",\"type\":\"check\",\"unit\":\"\",\"abnormalRequired\":true}]','0','2',NULL,'2026-07-17 15:10:46','',NULL,NULL),(12,'日常巡检测试1','RT20260717004',103,'研发部门','1',2,'[6,5]','{\"common\":[{\"item\":\"环境卫生\",\"type\":\"text\",\"unit\":\"\",\"abnormalRequired\":true}],\"devices\":[{\"equipmentId\":6,\"equipmentName\":\"EQ005 - 数控加工中心\",\"items\":[{\"item\":\"温度\",\"type\":\"check\",\"unit\":\"\",\"abnormalRequired\":false}]},{\"equipmentId\":5,\"equipmentName\":\"EQ004 - 平面磨床\",\"items\":[{\"item\":\"刀口\",\"type\":\"text\",\"unit\":\"\",\"abnormalRequired\":true}]}]}','0','2',NULL,'2026-07-17 15:41:15','',NULL,'111111111'),(13,'TestRouteChanged','TEST-RT-C',NULL,NULL,NULL,NULL,NULL,NULL,'0','2',NULL,'2026-07-17 18:11:57','','2026-07-17 18:12:11',NULL),(14,'测试巡检路线1','RT20260717005',104,'市场部门','0',1,'[7]','{\"common\":[],\"devices\":[{\"equipmentId\":7,\"equipmentName\":\"TEST-EQ-T - 测试设备T\",\"items\":[{\"item\":\"温度检查\",\"type\":\"number\",\"unit\":\"度\",\"abnormalRequired\":true}]}]}','0','0',NULL,'2026-07-17 22:47:58','',NULL,NULL);
/*!40000 ALTER TABLE `dms_inspection_route` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dms_inspection_task`
--

DROP TABLE IF EXISTS `dms_inspection_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dms_inspection_task` (
  `task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_no` varchar(64) NOT NULL COMMENT '任务编号',
  `route_id` bigint DEFAULT NULL COMMENT '关联路线ID',
  `route_name` varchar(255) DEFAULT NULL COMMENT '路线名称（冗余）',
  `plan_date` date DEFAULT NULL COMMENT '计划日期',
  `inspector_id` bigint DEFAULT NULL COMMENT '点检人ID',
  `inspector_name` varchar(64) DEFAULT NULL COMMENT '点检人名称',
  `task_status` varchar(10) DEFAULT '0' COMMENT '任务状态（字典 dms_inspection_status）',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `abnormal_count` int DEFAULT '0' COMMENT '异常项数',
  `result_data` text COMMENT '点检结果（JSON）',
  `photo_urls` text COMMENT '点检照片URL（JSON数组）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`task_id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_insptask_route` (`route_id`),
  KEY `idx_insptask_status` (`task_status`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点检任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dms_inspection_task`
--

LOCK TABLES `dms_inspection_task` WRITE;
/*!40000 ALTER TABLE `dms_inspection_task` DISABLE KEYS */;
INSERT INTO `dms_inspection_task` VALUES (1,'INS20260717143453',2,'ffffffffffff','2026-07-17',2,'李华','2',NULL,'2026-07-17 14:37:12',0,'[{\"item\":\"dddddddddd\",\"type\":\"check\",\"unit\":\"\",\"result\":\"ok\",\"abnormal\":false,\"abnormalDesc\":\"\"}]',NULL,'2',NULL,'2026-07-17 14:34:53','','2026-07-17 14:37:12',NULL),(2,'INS20260717145057',9,'yyyy','2026-07-17',1,'系统管理员','2',NULL,'2026-07-17 14:58:38',2,'[{\"item\":\"yyyyyyyyyyy\",\"type\":\"check\",\"unit\":\"\",\"result\":\"ok\",\"abnormal\":false,\"abnormalDesc\":\"\"},{\"item\":\"uuuuuuuuuu\",\"type\":\"number\",\"unit\":\"f\",\"result\":\"ok\",\"abnormal\":true,\"abnormalDesc\":\"tttttt\"},{\"item\":\"iiiiiiiii\",\"type\":\"text\",\"unit\":\"\",\"result\":\"ok\",\"abnormal\":true,\"abnormalDesc\":\"tttttt\"}]','[\"/profile/upload/2026/07/17/Surface_Laptop_3_01_20260717145602A002.jpg\"]','2',NULL,'2026-07-17 14:50:57','','2026-07-17 14:58:38',NULL),(3,'INS20260717150035',9,'yyyy','2026-07-17',1,'系统管理员','2',NULL,'2026-07-17 15:07:15',2,'[{\"item\":\"yyyyyyyyyyy\",\"type\":\"check\",\"unit\":\"\",\"result\":\"ok\",\"abnormal\":false,\"abnormalDesc\":\"\"},{\"item\":\"uuuuuuuuuu\",\"type\":\"number\",\"unit\":\"f\",\"result\":\"ok\",\"value\":90,\"abnormal\":true,\"abnormalDesc\":\"ssssss1111111111\"},{\"item\":\"iiiiiiiii\",\"type\":\"text\",\"unit\":\"\",\"result\":\"ok\",\"value\":\"sssssssssss\",\"abnormal\":true,\"abnormalDesc\":\"sssssssssssssssss222222222\"}]','[\"/profile/upload/2026/07/17/Surface_Laptop_3_02_20260717150708A001.jpg\",\"/profile/upload/2026/07/17/Surface_Laptop_3_03_20260717150713A002.jpg\"]','2',NULL,'2026-07-17 15:00:35','','2026-07-17 15:07:14',NULL),(4,'INS20260717151104',10,'yyyyyyyyyyyyyyy','2026-07-17',100,'张涛','2',NULL,'2026-07-17 15:11:24',2,'[{\"item\":\"yy11\",\"type\":\"number\",\"unit\":\"\",\"result\":\"ok\",\"abnormal\":true,\"abnormalDesc\":\"222222222\"},{\"item\":\"yyy2\",\"type\":\"check\",\"unit\":\"\",\"result\":\"abnormal\",\"abnormal\":true,\"abnormalDesc\":\"333333333333\"}]','[\"/profile/upload/2026/07/17/Surface_Laptop_3_04_20260717151123A003.jpg\"]','2',NULL,'2026-07-17 15:11:04','','2026-07-17 15:11:24',NULL),(5,'INS20260717154128',12,'日常巡检测试1','2026-07-17',2,'李华','2',NULL,'2026-07-17 15:42:07',3,'{\"common\":[{\"item\":\"环境卫生\",\"type\":\"text\",\"unit\":\"\",\"result\":\"ok\",\"value\":\"6666666666666\",\"abnormal\":true,\"abnormalDesc\":\"777777777777\"}],\"devices\":[{\"equipmentId\":6,\"equipmentName\":\"EQ005 - 数控加工中心\",\"items\":[{\"item\":\"温度\",\"type\":\"check\",\"unit\":\"\",\"result\":\"abnormal\",\"abnormal\":true,\"abnormalDesc\":\"rrrrrr\"}]},{\"equipmentId\":5,\"equipmentName\":\"EQ004 - 平面磨床\",\"items\":[{\"item\":\"刀口\",\"type\":\"text\",\"unit\":\"\",\"result\":\"ok\",\"value\":\"ttttttttt\",\"abnormal\":true,\"abnormalDesc\":\"ttttttttttttttttttttt\"}]}]}','[\"/profile/upload/2026/07/17/Surface_Laptop_3_02_20260717154206A001.jpg\"]','2',NULL,'2026-07-17 15:41:28','','2026-07-17 15:42:07',NULL),(6,'INS20260717181228',1,NULL,NULL,NULL,NULL,'2',NULL,'2026-07-17 18:13:13',NULL,NULL,NULL,'2',NULL,'2026-07-17 18:12:28','','2026-07-17 18:13:13','TestDone'),(7,'INS20260717224819',14,'测试巡检路线1','2026-07-17',2,'李华','2',NULL,'2026-07-17 22:48:43',1,'{\"common\":[],\"devices\":[{\"equipmentId\":7,\"equipmentName\":\"TEST-EQ-T - 测试设备T\",\"items\":[{\"item\":\"温度检查\",\"type\":\"number\",\"unit\":\"度\",\"result\":\"ok\",\"value\":90,\"abnormal\":true,\"abnormalDesc\":\"过高温度\"}]}]}','[\"/profile/upload/2026/07/17/Surface_Laptop_3_02_20260717224841A001.jpg\"]','0',NULL,'2026-07-17 22:48:19','','2026-07-17 22:48:42',NULL),(8,'INS20260717225543',14,'测试巡检路线1','2026-07-17',1,'admin','2','2026-07-17 22:55:44','2026-07-17 23:02:06',0,'{\"common\":[],\"devices\":[{\"equipmentId\":7,\"equipmentName\":\"TEST-EQ-T - 测试设备T\",\"items\":[{\"item\":\"温度检查\",\"type\":\"number\",\"unit\":\"度\",\"result\":\"ok\",\"value\":2222,\"abnormal\":false,\"abnormalDesc\":\"\"}]}]}',NULL,'0',NULL,'2026-07-17 22:55:43','admin','2026-07-17 23:02:05',NULL);
/*!40000 ALTER TABLE `dms_inspection_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dms_pm_plan`
--

DROP TABLE IF EXISTS `dms_pm_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dms_pm_plan` (
  `plan_id` bigint NOT NULL AUTO_INCREMENT COMMENT '计划ID',
  `plan_name` varchar(255) NOT NULL COMMENT '计划名称',
  `equipment_id` bigint DEFAULT NULL COMMENT '设备ID（为空则按分类）',
  `category_id` bigint DEFAULT NULL COMMENT '设备分类ID',
  `trigger_type` varchar(10) DEFAULT '0' COMMENT '触发类型（字典 dms_pm_trigger_type）',
  `cycle_value` int DEFAULT NULL COMMENT '周期值',
  `cycle_unit` varchar(20) DEFAULT NULL COMMENT '周期单位（天/月/小时/件）',
  `task_list` text COMMENT '任务清单（JSON数组）',
  `spare_part_list` text COMMENT '备件清单（JSON数组）',
  `estimated_hours` decimal(10,2) DEFAULT NULL COMMENT '预估工时',
  `advance_days` int DEFAULT '3' COMMENT '提前生成天数',
  `auto_assign` char(1) DEFAULT '0' COMMENT '是否自动派工（0否 1是）',
  `assignee_id` bigint DEFAULT NULL COMMENT '默认派工人ID',
  `assignee_name` varchar(64) DEFAULT NULL COMMENT '默认派工人名称',
  `last_execute_time` datetime DEFAULT NULL COMMENT '上次执行时间',
  `next_execute_time` datetime DEFAULT NULL COMMENT '下次执行时间',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`plan_id`),
  KEY `idx_pm_equipment` (`equipment_id`),
  KEY `idx_pm_category` (`category_id`),
  KEY `idx_pm_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='PM预防性维护计划表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dms_pm_plan`
--

LOCK TABLES `dms_pm_plan` WRITE;
/*!40000 ALTER TABLE `dms_pm_plan` DISABLE KEYS */;
INSERT INTO `dms_pm_plan` VALUES (1,'ddddddddd111',6,1,'0',30,'天','fdddddddddddd','3eeeeeeeeeeeeeeeeeee',4.00,1,'0',NULL,NULL,'2026-07-17 21:36:17','2026-08-16 21:36:17','0','2',NULL,'2026-07-16 16:08:50','','2026-07-17 21:36:17',NULL),(2,'ddddddddddd',6,1,'0',30,'天','ddddddddddddd',NULL,NULL,3,'0',NULL,NULL,'2026-07-16 22:56:21','2026-08-15 22:56:21','0','2',NULL,'2026-07-16 16:43:21','','2026-07-16 22:56:21',NULL),(3,'xxxxxxxxxxxx1',5,5,'1',30,'天','[\"xxxxxxx1\",\"xxxxxxx2\"]','ssssss',5.00,3,'1',2,'李华','2026-07-17 21:54:15','2026-08-16 21:54:15','0','2',NULL,'2026-07-16 23:38:11','','2026-07-17 21:54:15',NULL),(4,'4',3,3,'0',30,'天','[\"清洁滤网。\"]','[{\"partId\":3,\"partName\":\"ddd\",\"qty\":1,\"unit\":\"0\"}]',NULL,3,'0',NULL,NULL,'2026-07-17 08:02:05','2026-08-16 08:02:05','0','2',NULL,'2026-07-16 23:44:12','','2026-07-17 08:02:04',NULL),(5,'TestPMPlanChanged',1,NULL,'0',60,'2','Check;Clean',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-09-15 18:10:44','0','2',NULL,'2026-07-17 18:10:30','','2026-07-17 18:10:43',NULL),(6,'测试计划001',6,1,'0',30,'天','[\"检查温度\",\"清洁滤网\"]','[{\"partId\":10,\"partName\":\"测试备件11\",\"qty\":1,\"unit\":\"9\"}]',5.00,3,'1',100,'张涛','2026-07-17 22:46:20','2026-08-16 22:46:20','0','0',NULL,'2026-07-17 22:15:05','','2026-07-17 22:46:20',NULL);
/*!40000 ALTER TABLE `dms_pm_plan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dms_repair_faq`
--

DROP TABLE IF EXISTS `dms_repair_faq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dms_repair_faq` (
  `faq_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'FAQ ID',
  `question` varchar(500) NOT NULL COMMENT '问题',
  `answer` text COMMENT '标准答案',
  `category_id` bigint DEFAULT NULL COMMENT '关联设备分类ID',
  `category_name` varchar(128) DEFAULT NULL COMMENT '分类名称',
  `keywords` varchar(500) DEFAULT NULL COMMENT '关键词（逗号分隔）',
  `view_count` int DEFAULT '0' COMMENT '查看次数',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`faq_id`),
  KEY `idx_faq_category` (`category_id`),
  KEY `idx_faq_keywords` (`keywords`(100))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='维修知识FAQ表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dms_repair_faq`
--

LOCK TABLES `dms_repair_faq` WRITE;
/*!40000 ALTER TABLE `dms_repair_faq` DISABLE KEYS */;
INSERT INTO `dms_repair_faq` VALUES (1,'TestQChanged','TestAChanged',NULL,NULL,NULL,0,'0','2',NULL,'2026-07-17 18:16:01','','2026-07-17 18:17:02',NULL),(2,'testst','ttttttttttttttttttttttttttttttttttttttttt',NULL,'tttttttttttt','tttttttttttttt',0,'0','0',NULL,'2026-07-17 22:00:54','',NULL,'tttttt');
/*!40000 ALTER TABLE `dms_repair_faq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dms_spare_part`
--

DROP TABLE IF EXISTS `dms_spare_part`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dms_spare_part` (
  `part_id` bigint NOT NULL AUTO_INCREMENT COMMENT '备件ID',
  `part_code` varchar(64) NOT NULL COMMENT '备件编号',
  `part_name` varchar(255) NOT NULL COMMENT '备件名称',
  `part_type` varchar(50) DEFAULT NULL COMMENT '备件类别',
  `spec_model` varchar(200) DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) DEFAULT '个' COMMENT '单位',
  `applicable_category` varchar(500) DEFAULT NULL COMMENT '适用设备分类（JSON数组）',
  `stock_min` decimal(18,2) DEFAULT '0.00' COMMENT '库存下限',
  `stock_max` decimal(18,2) DEFAULT '0.00' COMMENT '库存上限',
  `safety_stock` decimal(18,2) DEFAULT '0.00' COMMENT '安全库存',
  `current_stock` decimal(18,2) DEFAULT '0.00' COMMENT '当前库存',
  `storage_location` varchar(255) DEFAULT NULL COMMENT '存放位置',
  `supplier` varchar(255) DEFAULT NULL COMMENT '供应商',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`part_id`),
  UNIQUE KEY `uk_part_code` (`part_code`),
  KEY `idx_part_name` (`part_name`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='备件管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dms_spare_part`
--

LOCK TABLES `dms_spare_part` WRITE;
/*!40000 ALTER TABLE `dms_spare_part` DISABLE KEYS */;
INSERT INTO `dms_spare_part` VALUES (1,'BJ20260715001','bbbb','1','ddd','0',NULL,2.00,7.00,NULL,26.00,'备件库','惠州电池有限公司','0','0',NULL,'2026-07-15 16:55:39','','2026-07-16 10:35:34',NULL),(2,'BJ20260716001','cccc','4','cc','1',NULL,7.00,20.00,NULL,0.00,'备件库','上海金属材料有限公司','0','0',NULL,'2026-07-16 08:00:50','','2026-07-16 22:45:12',NULL),(3,'BJ20260716002','ddd','7','ddd','0',NULL,NULL,NULL,NULL,4.00,'备件库','深圳塑胶科技有限公司','0','0',NULL,'2026-07-16 08:01:48','','2026-07-16 10:34:19',NULL),(4,'BJ001','主轴轴承','10','6205-2RS','0','车床,铣床,加工中心',5.00,50.00,10.00,25.00,'仓库A-01货架','若依科技','0','0','admin',NULL,'','2026-07-16 21:53:32',NULL),(5,'BJ002','丝杠螺母','10','SFU2005-4','9','车床,铣床',3.00,30.00,5.00,12.00,'仓库A-02货架','若依科技','0','0','admin',NULL,'','2026-07-16 21:53:24',NULL),(6,'BJ003','电机皮带','9','B-1800','11','车床,钻床,磨床',10.00,100.00,20.00,45.00,'仓库B-01货架','若依科技','0','0','admin',NULL,'','2026-07-16 21:52:21',NULL),(7,'BJ004','液压油泵','5','VP-20','10','加工中心',2.00,20.00,3.00,6.00,'仓库C-01货架','若依科技','0','0','admin',NULL,'','2026-07-17 22:29:23',NULL),(8,'BJ005','数控刀具','切削工具','CNMG120408','盒','车床,加工中心',20.00,200.00,50.00,80.00,'仓库D-01货架','若依科技','0','2','admin',NULL,'',NULL,NULL),(9,'BJ20260716003','电机皮带','5','22222','0',NULL,6.00,10.00,NULL,205.00,'备件库','东莞电子科技有限公司','0','0',NULL,'2026-07-16 15:22:52','','2026-07-17 23:41:11',NULL),(10,'BJ20260716004','测试备件11','2','xxx','9',NULL,5.00,10.00,NULL,0.00,NULL,'东莞电子科技有限公司','0','0',NULL,'2026-07-16 22:09:37','',NULL,NULL),(11,'TEST-SP-C','TestSPChanged','0',NULL,'0',NULL,NULL,NULL,20.00,80.00,NULL,NULL,'0','2',NULL,'2026-07-17 18:11:15','','2026-07-17 18:11:33',NULL);
/*!40000 ALTER TABLE `dms_spare_part` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dms_spare_part_record`
--

DROP TABLE IF EXISTS `dms_spare_part_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dms_spare_part_record` (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `part_id` bigint NOT NULL COMMENT '备件ID',
  `document_code` varchar(64) DEFAULT NULL COMMENT '单据号',
  `source_type` varchar(10) DEFAULT NULL COMMENT '来源类型 0采购 1初始化',
  `target_type` varchar(10) DEFAULT NULL COMMENT '去向类型 1工单领用 2调拨 3报废 4盘点',
  `supplier_or_dept` varchar(255) DEFAULT NULL COMMENT '供应商或领用部门',
  `receiver` varchar(100) DEFAULT NULL COMMENT '领用人',
  `part_code` varchar(64) DEFAULT NULL COMMENT '备件编号（冗余）',
  `part_name` varchar(255) DEFAULT NULL COMMENT '备件名称（冗余）',
  `move_type` varchar(10) NOT NULL COMMENT '出入库类型（字典 dms_stock_move_type）',
  `quantity` decimal(18,2) NOT NULL COMMENT '数量',
  `before_stock` decimal(18,2) DEFAULT NULL COMMENT '变更前库存',
  `after_stock` decimal(18,2) DEFAULT NULL COMMENT '变更后库存',
  `ref_type` varchar(20) DEFAULT NULL COMMENT '关联单据类型（workorder/purchase/adjust）',
  `ref_id` bigint DEFAULT NULL COMMENT '关联单据ID',
  `ref_no` varchar(64) DEFAULT NULL COMMENT '关联单据号',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人名称',
  `operate_date` date DEFAULT NULL COMMENT '操作日期(入库/出库日期)',
  `storage_location` varchar(255) DEFAULT NULL COMMENT '存放位置(入库时填写)',
  `create_time` datetime DEFAULT NULL COMMENT '操作时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`record_id`),
  KEY `idx_spr_part` (`part_id`),
  KEY `idx_spr_type` (`move_type`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='备件出入库记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dms_spare_part_record`
--

LOCK TABLES `dms_spare_part_record` WRITE;
/*!40000 ALTER TABLE `dms_spare_part_record` DISABLE KEYS */;
INSERT INTO `dms_spare_part_record` VALUES (1,1,'BJIN20260715001','0','4','惠州电池有限公司',NULL,'BJ20260715001','bbbb','0',5.00,5.00,10.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-15 19:10:26',NULL),(2,1,'BJIN20260715002','1','4','惠州电池有限公司',NULL,'BJ20260715001','bbbb','0',5.00,10.00,15.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-15 19:10:45',NULL),(3,1,'BJIN20260715003','0','4','惠州电池有限公司',NULL,'BJ20260715001','bbbb','0',1.00,15.00,16.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-15 19:12:15',NULL),(4,1,'BJIN20260716001','0','4','惠州电池有限公司',NULL,'BJ20260715001','bbbb','0',4.00,16.00,20.00,NULL,NULL,NULL,NULL,'若依','2026-07-16','2222222222','2026-07-16 07:56:02',NULL),(5,1,'BJIN20260716002','0','4','惠州电池有限公司',NULL,'BJ20260715001','bbbb','0',5.00,20.00,25.00,NULL,NULL,NULL,NULL,'若依','2026-07-16','3333','2026-07-16 07:58:40',NULL),(6,3,'BJIN20260716003','0','4','深圳塑胶科技有限公司',NULL,'BJ20260716002','ddd','0',6.00,0.00,6.00,NULL,NULL,NULL,NULL,'若依','2026-07-16','备件库','2026-07-16 08:30:30',NULL),(7,2,'BJIN20260716004','1','4','上海金属材料有限公司',NULL,'BJ20260716001','cccc','0',2.00,0.00,2.00,NULL,NULL,NULL,NULL,'若依','2026-07-16','备件库','2026-07-16 08:31:39',NULL),(8,2,'BJOUT20260716001','1','1','若依科技','若依','BJ20260716001','cccc','1',2.00,2.00,0.00,NULL,NULL,NULL,NULL,'若依','2026-07-16',NULL,'2026-07-16 08:32:21',NULL),(9,2,'BJIN20260716005','0','4','上海金属材料有限公司',NULL,'BJ20260716001','cccc','0',10.00,0.00,10.00,NULL,NULL,NULL,NULL,'若依','2026-07-16','备件库','2026-07-16 08:41:34',NULL),(10,2,'BJIN20260716006','1','4','上海金属材料有限公司',NULL,'BJ20260716001','cccc','0',6.00,10.00,16.00,NULL,NULL,NULL,NULL,'若依','2026-07-16','备件库','2026-07-16 08:49:41',NULL),(11,2,'BJOUT20260716002','1','2','研发部门','若依','BJ20260716001','cccc','1',1.00,16.00,15.00,NULL,NULL,NULL,NULL,'若依','2026-07-16',NULL,'2026-07-16 08:50:13',NULL),(12,3,'BJOUT20260716003','1','2','研发部门','若依','BJ20260716002','ddd','1',2.00,6.00,4.00,NULL,NULL,NULL,NULL,'若依','2026-07-16',NULL,'2026-07-16 10:34:19',NULL),(14,2,'BJOUT20260716004',NULL,'1',NULL,'李华','BJ20260716001','cccc','1',15.00,15.00,0.00,'workorder',13,'WO202607160005',NULL,'系统管理员','2026-07-16',NULL,'2026-07-16 22:45:12','工单「WO202607160005」验收自动出库'),(15,7,'BJOUT20260717001',NULL,'1',NULL,'李华','BJ004','液压油泵','1',1.00,8.00,7.00,'workorder',11,'WO202607160003',NULL,'系统管理员','2026-07-17',NULL,'2026-07-17 09:16:56','工单「WO202607160003」验收自动出库'),(16,7,'BJOUT20260717002',NULL,'1',NULL,'张涛','BJ004','液压油泵','1',1.00,7.00,6.00,'workorder',27,'WO202607170019',NULL,'系统管理员','2026-07-17',NULL,'2026-07-17 22:29:23','工单「WO202607170019」验收自动出库'),(17,9,'BJIN20260717001','0','4','东莞电子科技有限公司',NULL,'BJ20260716003','电机皮带','0',5.00,0.00,5.00,NULL,NULL,NULL,NULL,'系统管理员','2026-07-17','备件库','2026-07-17 23:32:18',NULL),(18,9,'BJIN20260717002','1','4','东莞电子科技有限公司',NULL,'BJ20260716003','电机皮带','0',200.00,5.00,205.00,NULL,NULL,NULL,NULL,'系统管理员','2026-07-17','备件库','2026-07-17 23:41:11',NULL);
/*!40000 ALTER TABLE `dms_spare_part_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dms_work_order`
--

DROP TABLE IF EXISTS `dms_work_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dms_work_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `order_no` varchar(64) NOT NULL COMMENT '工单号',
  `equipment_id` bigint DEFAULT NULL COMMENT '设备ID（点检整改工单可为空）',
  `equipment_code` varchar(64) DEFAULT NULL COMMENT '设备编号（冗余）',
  `equipment_name` varchar(255) DEFAULT NULL COMMENT '设备名称（冗余）',
  `order_type` varchar(10) DEFAULT '0' COMMENT '工单类型（字典 dms_order_type）',
  `fault_description` varchar(1000) DEFAULT NULL COMMENT '故障现象/任务描述',
  `reporter_id` bigint DEFAULT NULL COMMENT '报修人ID',
  `reporter_name` varchar(64) DEFAULT NULL COMMENT '报修人名称',
  `report_time` datetime DEFAULT NULL COMMENT '报修时间',
  `priority` varchar(10) DEFAULT '2' COMMENT '优先级（字典 dms_priority）',
  `order_status` varchar(10) DEFAULT '0' COMMENT '工单状态（字典 dms_order_status）',
  `assignee_id` bigint DEFAULT NULL COMMENT '维修人ID',
  `assignee_name` varchar(64) DEFAULT NULL COMMENT '维修人名称',
  `assign_time` datetime DEFAULT NULL COMMENT '派工时间',
  `arrive_time` datetime DEFAULT NULL COMMENT '到达时间',
  `sla_response_deadline` datetime DEFAULT NULL COMMENT 'SLA响应截止时间（派工时效）',
  `sla_process_deadline` datetime DEFAULT NULL COMMENT 'SLA处理截止时间（处理时效）',
  `sla_timeout_status` varchar(10) DEFAULT '0' COMMENT 'SLA超时状态(0正常 1响应超时 2处理超时)',
  `sla_escalated` varchar(10) DEFAULT '0' COMMENT 'SLA是否已升级(0否 1是)',
  `fault_cause` varchar(1000) DEFAULT NULL COMMENT '故障原因',
  `repair_measure` varchar(1000) DEFAULT NULL COMMENT '维修措施',
  `spare_parts_used` varchar(500) DEFAULT NULL COMMENT '更换备件',
  `task_checklist` text COMMENT '任务清单（JSON数组，含完成状态）',
  `downtime_duration` decimal(10,2) DEFAULT NULL COMMENT '停机时长（小时）',
  `complete_time` datetime DEFAULT NULL COMMENT '完工时间',
  `complete_remark` varchar(1000) DEFAULT NULL COMMENT '完工说明',
  `deviation_reason` varchar(500) DEFAULT NULL COMMENT '偏差原因（PM工单未按时完成时记录）',
  `assigner_id` bigint DEFAULT NULL COMMENT '派工人ID',
  `assigner_name` varchar(64) DEFAULT NULL COMMENT '派工人名称',
  `verifier_id` bigint DEFAULT NULL COMMENT '验收人ID',
  `verifier_name` varchar(64) DEFAULT NULL COMMENT '验收人名称',
  `verify_time` datetime DEFAULT NULL COMMENT '验收时间',
  `verify_opinion` varchar(500) DEFAULT NULL COMMENT '验收意见',
  `rating` int DEFAULT NULL COMMENT '评价（1-5星）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_equipment` (`equipment_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_order_type` (`order_type`),
  KEY `idx_order_assignee` (`assignee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dms_work_order`
--

LOCK TABLES `dms_work_order` WRITE;
/*!40000 ALTER TABLE `dms_work_order` DISABLE KEYS */;
INSERT INTO `dms_work_order` VALUES (1,'WO202607160001',6,'EQ005','数控加工中心','0','ttttttttttttttttt',2,NULL,'2026-07-16 15:38:25','2','5',2,'','2026-07-16 15:59:01','2026-07-16 16:00:29',NULL,NULL,'0','0','6','66667','7777778',NULL,666.00,'2026-07-16 16:02:21','888',NULL,2,'',2,'','2026-07-16 16:02:30','5555555555555555555',4,'2',NULL,'2026-07-16 15:38:35','','2026-07-16 16:02:30',NULL),(10,'WO202607160002',5,'EQ004','平面磨床','0','111',100,'张涛','2026-07-16 16:29:30','2','6',NULL,NULL,NULL,NULL,NULL,NULL,'0','0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,'2','admin','2026-07-16 16:29:35','','2026-07-16 16:40:24',NULL),(11,'WO202607160003',6,'EQ005','数控加工中心','1','PM计划维护：ddddddddddd\n任务清单：ddddddddddddd',NULL,NULL,'2026-07-16 16:43:34','3','5',2,'李华','2026-07-17 09:16:13','2026-07-17 09:16:17',NULL,NULL,'0','0','tttttttttttttttttttttttttttt','ttttttttttttttt','液压油泵×1',NULL,2.00,'2026-07-17 09:16:51','ttttttttttttttttt','ttttttttttttttt',100,'张涛',2,'李华','2026-07-17 09:16:57','tttttttttt',5,'2',NULL,'2026-07-16 16:43:34','','2026-07-17 09:16:56','由PM计划[ddddddddddd]自动生成'),(12,'WO202607160004',6,'EQ005','数控加工中心','1','PM计划维护：ddddddddddd\n任务清单：ddddddddddddd',NULL,NULL,'2026-07-16 16:43:47','3','3',100,'张涛','2026-07-16 16:50:53','2026-07-16 22:13:41',NULL,NULL,'0','0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,NULL,NULL,NULL,'2',NULL,'2026-07-16 16:43:46','','2026-07-16 22:13:43','由PM计划[ddddddddddd]自动生成'),(13,'WO202607160005',6,'EQ005','数控加工中心','1','PM计划维护：ddddddddddd\n任务清单：ddddddddddddd',NULL,NULL,'2026-07-16 16:46:01','3','5',2,'李华','2026-07-16 16:46:34','2026-07-16 16:49:21',NULL,NULL,'0','0','2222','222','cccc×15',NULL,10.00,'2026-07-16 22:10:38','',NULL,NULL,'',2,'李华','2026-07-16 22:45:13','eeee',5,'2',NULL,'2026-07-16 16:46:00','admin','2026-07-16 22:45:12','由PM计划[ddddddddddd]自动生成'),(14,'WO202607160006',6,'EQ005','数控加工中心','1','PM计划维护：ddddddddddd\n任务清单：ddddddddddddd',NULL,NULL,'2026-07-16 22:56:21','3','0',NULL,NULL,NULL,NULL,NULL,NULL,'0','0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2',NULL,'2026-07-16 22:56:21','',NULL,'由PM计划[ddddddddddd]自动生成(PM计划ID:2)'),(15,'WO202607170007',3,'EQ002','立式铣床','1','PM计划维护：4\n任务清单：[\"清洁滤网。\"]',NULL,NULL,'2026-07-17 08:02:05','3','5',2,'李华','2026-07-17 08:02:30','2026-07-17 08:02:34',NULL,NULL,'0','0','ddd','ssssssssssssssssss','','[{\"text\":\"清洁滤网。\",\"done\":true}]',1.00,'2026-07-17 08:03:21','','',2,'李华',2,'李华','2026-07-17 08:03:31','',5,'2',NULL,'2026-07-17 08:02:04','','2026-07-17 08:03:30','由PM计划[4]自动生成(PM计划ID:4)'),(16,'WO202607170008',5,'EQ004','平面磨床','0','平面磨床设备今天没法正常使用。',2,'李华','2026-08-07 00:00:00','2','5',2,'李华','2026-07-17 09:21:01','2026-07-17 09:21:04','2026-08-07 04:00:00','2026-08-08 00:00:00','0','0','6','66666666666666666666666666666','',NULL,1.00,'2026-07-17 09:21:17','','',100,'',100,'张涛','2026-07-17 09:21:23','66666',5,'2','admin','2026-07-17 09:20:13','','2026-07-17 09:21:23',NULL),(17,'WO202607170009',NULL,NULL,NULL,'2','点检异常整改：路线[yyyy]，异常项数：2\n点检结果：[{\"item\":\"yyyyyyyyyyy\",\"type\":\"check\",\"unit\":\"\",\"result\":\"ok\",\"abnormal\":false,\"abnormalDesc\":\"\"},{\"item\":\"uuuuuuuuuu\",\"type\":\"number\",\"unit\":\"f\",\"result\":\"ok\",\"abnormal\":true,\"abnormalDesc\":\"tttttt\"},{\"item\":\"iiiiiiiii\",\"type\":\"text\",\"unit\":\"\",\"result\":\"ok\",\"abnormal\":true,\"abnormalDesc\":\"tttttt\"}]',1,'系统管理员','2026-07-17 14:58:38','1','0',NULL,NULL,NULL,NULL,'2026-07-17 15:58:38','2026-07-17 22:58:38','0','0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2','admin','2026-07-17 14:58:38','',NULL,'由点检任务[INS20260717145057]自动生成(点检任务ID:2)'),(18,'WO202607170010',NULL,NULL,'EQ005 - 数控加工中心','2','【点检异常整改】\n巡检路线：yyyy\n关联设备：EQ005 - 数控加工中心\n点检人：系统管理员\n异常项数：2\n\n异常明细：\n1. uuuuuuuuuu（记录值：90f）\n   → 异常说明：ssssss1111111111\n2. iiiiiiiii\n   → 异常说明：sssssssssssssssss222222222\n',1,'系统管理员','2026-07-17 15:07:15','1','0',NULL,NULL,NULL,NULL,'2026-07-17 16:07:15','2026-07-17 23:07:15','0','0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2','admin','2026-07-17 15:07:15','',NULL,'由点检任务[INS20260717150035]自动生成(点检任务ID:3)'),(19,'WO202607170011',NULL,NULL,'EQ004 - 平面磨床、EQ002 - 立式铣床','2','【点检异常整改】\n巡检路线：yyyyyyyyyyyyyyy\n关联设备：EQ004 - 平面磨床、EQ002 - 立式铣床\n点检人：张涛\n异常项数：2\n\n异常明细：\n1. yy11\n   → 异常说明：222222222\n2. yyy2\n   → 异常说明：333333333333\n',100,'张涛','2026-07-17 15:11:24','1','0',NULL,NULL,NULL,NULL,'2026-07-17 16:11:24','2026-07-17 23:11:24','0','0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2','admin','2026-07-17 15:11:24','',NULL,'由点检任务[INS20260717151104]自动生成(点检任务ID:4)'),(20,'WO202607170012',NULL,NULL,'EQ005 - 数控加工中心、EQ004 - 平面磨床','2','【点检异常整改】\n巡检路线：日常巡检测试1\n关联设备：EQ005 - 数控加工中心、EQ004 - 平面磨床\n点检人：李华\n异常项数：3\n\n异常明细：\n1. 环境卫生\n   → 异常说明：777777777777\n2. [EQ005 - 数控加工中心] 温度\n   → 异常说明：rrrrrr\n3. [EQ004 - 平面磨床] 刀口\n   → 异常说明：ttttttttttttttttttttt\n',2,'李华','2026-07-17 15:42:07','1','0',NULL,NULL,NULL,NULL,'2026-07-17 16:42:07','2026-07-17 23:42:07','1','1',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2','admin','2026-07-17 15:42:07','','2026-07-17 17:00:00','由点检任务[INS20260717154128]自动生成(点检任务ID:5)'),(21,'WO202607170013',1,'EQ001','空压机1','0','测试工单-自动测试',NULL,NULL,'2026-07-17 17:51:36','2','0',NULL,NULL,NULL,NULL,'2026-07-17 21:51:36','2026-07-18 17:51:36','0','0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2','admin','2026-07-17 17:51:35','',NULL,NULL),(22,'WO202607170014',1,'EQ001','AirCompressor1','0','UpdatedDesc',NULL,NULL,'2026-07-17 18:08:36','3','5',1,'admin','2026-07-17 18:09:02','2026-07-17 18:09:02','2026-07-17 22:08:36','2026-07-18 18:08:36','0','0',NULL,NULL,NULL,NULL,NULL,'2026-07-17 18:09:16',NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-17 18:09:17',NULL,NULL,'2','admin','2026-07-17 18:08:35','admin','2026-07-17 18:09:16',NULL),(23,'WO202607170015',1,'EQ001','AirComp1','0','TestWO2',NULL,NULL,'2026-07-17 18:09:33','2','6',NULL,NULL,NULL,NULL,'2026-07-17 22:09:33','2026-07-18 18:09:33','0','0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2','admin','2026-07-17 18:09:33','','2026-07-17 18:09:46',NULL),(24,'WO202607170016',6,'EQ005','数控加工中心','1','PM计划维护：ddddddddd111\n任务清单：fdddddddddddd',NULL,NULL,'2026-07-17 19:46:36','3','6',NULL,NULL,NULL,NULL,'2026-07-18 19:46:36','2026-07-20 19:46:36','0','0',NULL,NULL,NULL,'fdddddddddddd',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2',NULL,'2026-07-17 19:46:35','','2026-07-17 21:36:16','由PM计划[ddddddddd111]自动生成(PM计划ID:1)'),(25,'WO202607170017',6,'EQ005','数控加工中心','1','PM计划维护：ddddddddd111\n任务清单：fdddddddddddd',NULL,NULL,'2026-07-17 21:36:17','3','0',NULL,NULL,NULL,NULL,'2026-07-18 21:36:17','2026-07-20 21:36:17','0','0',NULL,NULL,NULL,'fdddddddddddd',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2',NULL,'2026-07-17 21:36:17','',NULL,'由PM计划[ddddddddd111]自动生成(PM计划ID:1)'),(26,'WO202607170018',5,'EQ004','平面磨床','1','PM计划维护：xxxxxxxxxxxx1\n任务清单：[\"xxxxxxx1\",\"xxxxxxx2\"]',NULL,NULL,'2026-07-17 21:54:15','3','1',2,'李华','2026-07-17 21:54:15',NULL,'2026-07-18 21:54:15','2026-07-20 21:54:15','0','0',NULL,NULL,NULL,'[\"xxxxxxx1\",\"xxxxxxx2\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2',NULL,'2026-07-17 21:54:15','','2026-07-17 21:54:15','由PM计划[xxxxxxxxxxxx1]自动生成(PM计划ID:3)'),(27,'WO202607170019',6,'EQ005','数控加工中心','0','总是发出异响。',2,'李华','2026-07-17 21:56:52','2','5',100,'张涛','2026-07-17 21:58:55','2026-07-17 21:58:58','2026-07-18 01:56:52','2026-07-18 21:56:52','0','0','ttttttttttttt','22222222222222222','液压油泵×1',NULL,5.00,'2026-07-17 22:29:14','222222222222222222','',2,'李华',2,'李华','2026-07-17 22:29:23','1111111111111',5,'0','admin','2026-07-17 21:56:56','','2026-07-17 22:29:23','111111111'),(28,'WO202607170020',6,'EQ005','数控加工中心','1','PM计划维护：测试计划001\n任务清单：[\"检查温度\",\"清洁滤网\"]',NULL,NULL,'2026-07-17 22:15:12','3','6',100,'张涛','2026-07-17 22:15:12',NULL,'2026-07-18 22:15:12','2026-07-20 22:15:12','0','0',NULL,NULL,NULL,'[\"检查温度\",\"清洁滤网\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2',NULL,'2026-07-17 22:15:12','','2026-07-17 22:38:34','由PM计划[测试计划001]自动生成(PM计划ID:6)'),(29,'WO202607170021',6,'EQ005','数控加工中心','1','PM计划维护：测试计划001\n任务清单：[\"检查温度\",\"清洁滤网\"]',NULL,NULL,'2026-07-17 22:38:34','3','6',100,'张涛','2026-07-17 22:38:34',NULL,'2026-07-18 22:38:34','2026-07-20 22:38:34','0','0',NULL,NULL,NULL,'[\"检查温度\",\"清洁滤网\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2',NULL,'2026-07-17 22:38:34','','2026-07-17 22:44:13','由PM计划[测试计划001]自动生成(PM计划ID:6)'),(30,'WO202607170022',6,'EQ005','数控加工中心','1','PM计划维护：测试计划001\n任务清单：[\"检查温度\",\"清洁滤网\"]',1,'admin','2026-07-17 22:44:14','3','1',100,'张涛','2026-07-17 22:44:14',NULL,'2026-07-18 22:44:14','2026-07-20 22:44:14','0','0',NULL,NULL,NULL,'[\"检查温度\",\"清洁滤网\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2','admin','2026-07-17 22:44:13','','2026-07-17 22:44:13','由PM计划[测试计划001]自动生成(PM计划ID:6)'),(31,'WO202607170023',6,'EQ005','数控加工中心','1','PM计划维护：测试计划001\n任务清单：[\"检查温度\",\"清洁滤网\"]',1,'admin','2026-07-17 22:46:20','3','2',100,'张涛','2026-07-17 22:46:20','2026-07-17 22:46:39','2026-07-18 22:46:20','2026-07-20 22:46:20','2','1',NULL,NULL,NULL,'[\"检查温度\",\"清洁滤网\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0','admin','2026-07-17 22:46:20','','2026-07-21 20:00:00','由PM计划[测试计划001]自动生成(PM计划ID:6)'),(32,'WO202607170024',NULL,NULL,'TEST-EQ-T - 测试设备T','2','【点检异常整改】\n巡检路线：测试巡检路线1\n关联设备：TEST-EQ-T - 测试设备T\n点检人：李华\n异常项数：1\n\n异常明细：\n1. [TEST-EQ-T - 测试设备T] 温度检查（记录值：90度）\n   → 异常说明：过高温度\n',2,'李华','2026-07-17 22:48:43','1','6',100,'张涛','2026-07-17 22:50:06',NULL,'2026-07-17 23:48:43','2026-07-18 06:48:43','0','0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,2,'李华',NULL,NULL,NULL,NULL,NULL,'0','admin','2026-07-17 22:48:42','','2026-07-17 22:50:11','由点检任务[INS20260717224819]自动生成(点检任务ID:7)');
/*!40000 ALTER TABLE `dms_work_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dms_work_order_log`
--

DROP TABLE IF EXISTS `dms_work_order_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dms_work_order_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `order_id` bigint NOT NULL COMMENT '工单ID',
  `order_no` varchar(64) DEFAULT NULL COMMENT '工单号',
  `from_status` varchar(10) DEFAULT NULL COMMENT '变更前状态',
  `to_status` varchar(10) DEFAULT NULL COMMENT '变更后状态',
  `action` varchar(64) DEFAULT NULL COMMENT '操作类型',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人名称',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单操作日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dms_work_order_log`
--

LOCK TABLES `dms_work_order_log` WRITE;
/*!40000 ALTER TABLE `dms_work_order_log` DISABLE KEYS */;
INSERT INTO `dms_work_order_log` VALUES (1,11,'WO202607160003','0','1','dispatch',NULL,'admin','派工给：李华','2026-07-17 09:16:13'),(2,11,'WO202607160003','1','2','accept',NULL,'admin','接单','2026-07-17 09:16:16'),(3,11,'WO202607160003','2','3','process',NULL,'admin','开始处理','2026-07-17 09:16:18'),(4,11,'WO202607160003','3','4','complete',NULL,'admin','完工：ttttttttttttttttt','2026-07-17 09:16:50'),(5,11,'WO202607160003','4','5','verify',NULL,'admin','验收通过，评分：5星','2026-07-17 09:16:56'),(6,16,'WO202607170008',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 09:20:13'),(7,16,'WO202607170008','0','1','dispatch',NULL,'admin','派工给：张涛','2026-07-17 09:20:53'),(8,16,'WO202607170008','1','1','reassign',NULL,'admin','改派给：李华（原维修人：张涛）','2026-07-17 09:21:01'),(9,16,'WO202607170008','1','2','accept',NULL,'admin','接单','2026-07-17 09:21:03'),(10,16,'WO202607170008','2','3','process',NULL,'admin','开始处理','2026-07-17 09:21:07'),(11,16,'WO202607170008','3','4','complete',NULL,'admin','完工：','2026-07-17 09:21:17'),(12,16,'WO202607170008','4','5','verify',NULL,'admin','验收通过，评分：5星','2026-07-17 09:21:23'),(13,17,'WO202607170009',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 14:58:38'),(14,18,'WO202607170010',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 15:07:15'),(15,19,'WO202607170011',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 15:11:24'),(16,20,'WO202607170012',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 15:42:07'),(17,21,'WO202607170013',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 17:51:35'),(18,22,'WO202607170014',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 18:08:35'),(19,22,'WO202607170014','0','1','dispatch',NULL,'admin','派工给：admin','2026-07-17 18:09:01'),(20,22,'WO202607170014','1','2','accept',NULL,'admin','接单','2026-07-17 18:09:01'),(21,22,'WO202607170014','2','3','process',NULL,'admin','开始处理','2026-07-17 18:09:01'),(22,22,'WO202607170014','3','4','complete',NULL,'admin','完工','2026-07-17 18:09:16'),(23,22,'WO202607170014','4','5','verify',NULL,'admin','验收通过','2026-07-17 18:09:16'),(24,23,'WO202607170015',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 18:09:33'),(25,23,'WO202607170015','0','6','cancel',NULL,'admin','撤销','2026-07-17 18:09:46'),(26,24,'WO202607170016',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 19:46:35'),(27,24,'WO202607170016','0','6','cancel',NULL,'admin','撤销','2026-07-17 21:36:16'),(28,25,'WO202607170017',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 21:36:17'),(29,26,'WO202607170018',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 21:54:15'),(30,27,'WO202607170019',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 21:56:56'),(31,27,'WO202607170019','0','1','dispatch',NULL,'admin','派工给：张涛','2026-07-17 21:58:54'),(32,27,'WO202607170019','1','2','accept',NULL,'admin','接单','2026-07-17 21:58:58'),(33,27,'WO202607170019','2','3','process',NULL,'admin','开始处理','2026-07-17 22:11:16'),(34,28,'WO202607170020',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 22:15:12'),(35,27,'WO202607170019','3','4','complete',NULL,'admin','完工：222222222222222222','2026-07-17 22:29:14'),(36,27,'WO202607170019','4','5','verify',NULL,'admin','验收通过，评分：5星','2026-07-17 22:29:23'),(37,28,'WO202607170020','1','6','cancel',NULL,'admin','撤销','2026-07-17 22:38:34'),(38,29,'WO202607170021',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 22:38:34'),(39,29,'WO202607170021','1','6','cancel',NULL,'admin','撤销','2026-07-17 22:44:13'),(40,30,'WO202607170022',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 22:44:13'),(41,31,'WO202607170023',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 22:46:20'),(42,31,'WO202607170023','1','2','accept',NULL,'admin','接单','2026-07-17 22:46:39'),(43,32,'WO202607170024',NULL,'0','create',NULL,'admin','工单创建','2026-07-17 22:48:42'),(44,32,'WO202607170024','0','1','dispatch',NULL,'admin','派工给：张涛','2026-07-17 22:50:05'),(45,32,'WO202607170024','1','6','cancel',NULL,'admin','撤销','2026-07-17 22:50:11'),(46,33,'WO202607210001',NULL,'0','create',NULL,'admin','工单创建','2026-07-21 22:41:13');
/*!40000 ALTER TABLE `dms_work_order_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gen_table`
--

DROP TABLE IF EXISTS `gen_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gen_table` (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) COLLATE utf8mb4_general_ci DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成功能作者',
  `form_col_num` int DEFAULT '1' COMMENT '表单布局（单列 双列 三列）',
  `gen_type` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='代码生成业务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gen_table`
--

LOCK TABLES `gen_table` WRITE;
/*!40000 ALTER TABLE `gen_table` DISABLE KEYS */;
/*!40000 ALTER TABLE `gen_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gen_table_column`
--

DROP TABLE IF EXISTS `gen_table_column`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gen_table_column` (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) COLLATE utf8mb4_general_ci DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
  `sort` int DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='代码生成业务表字段';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gen_table_column`
--

LOCK TABLES `gen_table_column` WRITE;
/*!40000 ALTER TABLE `gen_table_column` DISABLE KEYS */;
/*!40000 ALTER TABLE `gen_table_column` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_activity`
--

DROP TABLE IF EXISTS `mk_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_activity` (
  `activity_id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `activity_no` varchar(32) NOT NULL COMMENT '活动编号',
  `activity_name` varchar(200) NOT NULL COMMENT '活动名称',
  `activity_type` varchar(10) NOT NULL COMMENT '活动类型（字典）',
  `activity_form` char(1) DEFAULT '0' COMMENT '活动形式（0线上 1线下）',
  `activity_status` varchar(10) DEFAULT '0' COMMENT '活动状态（0草稿 1报名中 2进行中 3已结束 4已取消）',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `location` varchar(500) DEFAULT NULL COMMENT '活动地点/链接',
  `budget` decimal(14,2) DEFAULT '0.00' COMMENT '活动预算',
  `cost` decimal(12,2) DEFAULT '0.00' COMMENT '????',
  `target_count` int DEFAULT NULL COMMENT '目标人数',
  `target_lead_count` int DEFAULT NULL COMMENT '目标线索数',
  `user_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `summary` varchar(500) DEFAULT NULL COMMENT '活动简介',
  `content` text COMMENT '活动详情',
  `actual_count` int DEFAULT '0' COMMENT '实际参与人数',
  `lead_count` int DEFAULT '0' COMMENT '获取线索数',
  `mql_count` int DEFAULT '0' COMMENT 'MQL数量',
  `sql_count` int DEFAULT '0' COMMENT 'SQL数量',
  `opportunity_count` int DEFAULT '0' COMMENT '转化商机数',
  `deal_amount` decimal(14,2) DEFAULT '0.00' COMMENT '成交金额',
  `actual_roi` decimal(10,2) DEFAULT NULL COMMENT '??ROI(%)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`activity_id`),
  UNIQUE KEY `uk_activity_no` (`activity_no`),
  KEY `idx_activity_type` (`activity_type`),
  KEY `idx_activity_status` (`activity_status`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='营销活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_activity`
--

LOCK TABLES `mk_activity` WRITE;
/*!40000 ALTER TABLE `mk_activity` DISABLE KEYS */;
INSERT INTO `mk_activity` VALUES (1,'ACT20260719001','2026数字制造线上峰会','0','0','3','2026-06-15 14:00:00','2026-06-15 17:00:00','https://meeting.zhiyun-tech.com/summit',50000.00,0.00,500,100,2,103,'面向制造业CIO/CTO的数字化升级线上峰会','邀请行业专家分享数字化转型案例，展示我司MES/数字平台产品',320,85,45,22,8,280000.00,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'效果良好，ROI 5.6'),(2,'ACT20260719002','B2B营销自动化Webinar','0','0','3','2026-06-20 19:00:00','2026-06-20 21:00:00','https://meeting.zhiyun-tech.com/webinar06',15000.00,0.00,300,60,2,103,'营销自动化解决方案线上研讨会','介绍营销自动化系统核心功能及最佳实践',180,55,30,15,5,120000.00,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'ROI 8.0'),(3,'ACT20260719003','2026工业互联网展会','1','1','2','2026-07-18 09:00:00','2026-07-20 17:00:00','上海新国际博览中心N1馆',120000.00,0.00,2000,200,2,103,'参展2026工业互联网博览会','展位展示产品方案，收集线索，安排现场演示',850,180,80,40,12,450000.00,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'线下大型展会'),(4,'ACT20260719004','金融科技客户沙龙','1','1','2','2026-07-25 14:00:00','2026-07-25 17:00:00','北京国贸大酒店3层宴会厅',35000.00,0.00,50,30,2,103,'面向金融行业客户的闭门沙龙','AI风控、智能投顾等金融科技话题分享',0,0,0,0,0,0.00,NULL,'0','admin','2026-07-19 13:27:35','admin','2026-07-21 23:53:00','报名中'),(5,'ACT20260719005','搜索引擎广告投放-Q3','5','0','2','2026-07-01 00:00:00','2026-09-30 23:59:59','百度/360/搜狗',80000.00,0.00,0,150,2,103,'Q3季度搜索引擎广告投放计划','关键词：MES系统、数字平台、营销自动化等',5000,120,60,25,8,380000.00,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'持续投放中'),(6,'HD202607210001','????????1','0','0','0',NULL,NULL,NULL,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,0,0,0,0,0,0.00,NULL,'2',NULL,'2026-07-21 21:07:15','',NULL,NULL),(7,'HD202607210002','????????2','0','0','0',NULL,NULL,NULL,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,0,0,0,0,0,0.00,NULL,'2',NULL,'2026-07-21 21:07:15','',NULL,NULL),(8,'HD202607210003','???????3','0','0','0',NULL,NULL,NULL,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,0,0,0,0,0,0.00,NULL,'2',NULL,'2026-07-21 21:10:01','',NULL,NULL),(9,'HD202607001','fffffffffffffffff','0','0','1','2026-07-22 00:00:00','2026-07-29 00:00:00','fffffffffffffffffffffff',10000.00,0.00,111,22,2,105,'55555','5555555555555555555555555555555',0,0,0,0,0,0.00,NULL,'0',NULL,'2026-07-21 22:45:11','admin','2026-07-21 23:53:00',NULL),(10,'HD202607002','搜索引擎广告投放-Q3_副本','5','0','0',NULL,NULL,'百度/360/搜狗',80000.00,NULL,0,150,2,103,'Q3季度搜索引擎广告投放计划','关键词：MES系统、数字平台、营销自动化等',0,0,0,0,0,0.00,NULL,'0','admin','2026-07-21 23:53:00','',NULL,NULL);
/*!40000 ALTER TABLE `mk_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_activity_participant`
--

DROP TABLE IF EXISTS `mk_activity_participant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_activity_participant` (
  `participant_id` bigint NOT NULL AUTO_INCREMENT COMMENT '参与ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `lead_id` bigint DEFAULT NULL COMMENT '关联线索ID',
  `contact_id` bigint DEFAULT NULL COMMENT '?????ID',
  `company_name` varchar(200) DEFAULT NULL COMMENT '企业名称',
  `contact_name` varchar(50) NOT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系人手机号',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系人邮箱',
  `position` varchar(100) DEFAULT NULL COMMENT '职位',
  `participate_status` char(1) DEFAULT '0' COMMENT '参与状态（0报名 1已签到 2未出席）',
  `source` varchar(10) DEFAULT '0' COMMENT '??(0=?? 1=???? 2=??)',
  `sign_time` datetime DEFAULT NULL COMMENT '签到时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`participant_id`),
  KEY `idx_participant_activity` (`activity_id`),
  KEY `idx_participant_lead` (`lead_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动参与人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_activity_participant`
--

LOCK TABLES `mk_activity_participant` WRITE;
/*!40000 ALTER TABLE `mk_activity_participant` DISABLE KEYS */;
INSERT INTO `mk_activity_participant` VALUES (1,1,NULL,NULL,'北京智云科技有限公司','张伟','13800138001','zhangwei@zhiyun-tech.com','总经理','1','0','2026-06-15 14:05:00','admin','2026-07-19 13:30:00','',NULL,'线上峰会签到'),(2,1,NULL,NULL,'上海宏远制造股份有限公司','王强','13700137001','wangqiang@hongyuan-mfg.cn','CTO','1','0','2026-06-15 14:10:00','admin','2026-07-19 13:30:00','',NULL,'线上峰会签到'),(3,2,NULL,NULL,'深圳创新金融服务有限公司','刘洋','13600136001','liuyang@cx-fin.com','运营总监','1','0','2026-06-20 19:03:00','admin','2026-07-19 13:30:00','',NULL,'Webinar签到'),(4,3,NULL,NULL,'成都新视界科技有限公司','赵敏','13300133001','zhaomin@xinshijie.com','市场总监','1','0','2026-07-18 09:15:00','admin','2026-07-19 13:30:00','',NULL,'展会现场签到'),(5,3,NULL,NULL,'武汉光谷生物医药有限公司','孙磊','13400134001','sunlei@guanggu-bio.com','IT经理','0','0',NULL,'admin','2026-07-19 13:30:00','',NULL,'已报名未签到'),(6,9,7,NULL,'555','55','55','55','55','1','0','2026-07-21 23:53:15',NULL,'2026-07-21 22:45:27','admin','2026-07-21 23:53:16',NULL);
/*!40000 ALTER TABLE `mk_activity_participant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_activity_status_log`
--

DROP TABLE IF EXISTS `mk_activity_status_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_activity_status_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `activity_id` bigint NOT NULL COMMENT '??ID',
  `from_status` varchar(10) DEFAULT NULL COMMENT '?????',
  `to_status` varchar(10) NOT NULL COMMENT '?????',
  `operator_id` bigint DEFAULT NULL COMMENT '???ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '?????',
  `remark` varchar(500) DEFAULT NULL COMMENT '??',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`log_id`),
  KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='????????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_activity_status_log`
--

LOCK TABLES `mk_activity_status_log` WRITE;
/*!40000 ALTER TABLE `mk_activity_status_log` DISABLE KEYS */;
INSERT INTO `mk_activity_status_log` VALUES (1,9,'0','1',1,'admin','test','2026-07-21 23:53:00'),(2,4,'1','2',1,'admin',NULL,'2026-07-21 23:53:00');
/*!40000 ALTER TABLE `mk_activity_status_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_contact`
--

DROP TABLE IF EXISTS `mk_contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_contact` (
  `contact_id` bigint NOT NULL AUTO_INCREMENT COMMENT '联系人ID',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `gender` char(1) DEFAULT '0' COMMENT '性别（0男 1女）',
  `position` varchar(100) DEFAULT NULL COMMENT '职位',
  `department` varchar(100) DEFAULT NULL COMMENT '所属部门',
  `role_tag` varchar(200) DEFAULT NULL COMMENT '角色标签（多选逗号分隔）',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `wechat` varchar(50) DEFAULT NULL COMMENT '微信号',
  `qq` varchar(20) DEFAULT NULL COMMENT 'QQ号',
  `is_key` char(1) DEFAULT '0' COMMENT '是否关键联系人（0否 1是）',
  `is_primary` char(1) DEFAULT '0' COMMENT '是否主要联系人（0否 1是）',
  `owner_user_id` bigint DEFAULT NULL COMMENT '????ID',
  `last_contact_time` datetime DEFAULT NULL COMMENT '??????',
  `next_contact_time` datetime DEFAULT NULL COMMENT '??????',
  `personal_trait` varchar(500) DEFAULT NULL COMMENT '个人特点',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`contact_id`),
  KEY `idx_contact_customer` (`customer_id`),
  KEY `idx_contact_phone` (`phone`),
  KEY `idx_contact_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='联系人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_contact`
--

LOCK TABLES `mk_contact` WRITE;
/*!40000 ALTER TABLE `mk_contact` DISABLE KEYS */;
INSERT INTO `mk_contact` VALUES (1,1,'张伟','0','总经理','管理层','0','13800138001','zhangwei@zhiyun-tech.com','zhangwei_wx','88880001','1','1',NULL,NULL,NULL,'注重效率和结果，偏好数据驱动的决策','0','admin','2026-07-19 13:27:35','',NULL,'关键决策人'),(2,1,'李娜','1','采购经理','采购部','1,3','13900139001','lina@zhiyun-tech.com','lina_wx','88880002','1','0',NULL,NULL,NULL,'细致严谨，关注合同条款和交付周期','0','admin','2026-07-19 13:27:35','',NULL,'采购负责人'),(3,2,'王强','0','CTO','技术部','0,3','13700137001','wangqiang@hongyuan-mfg.cn','wangqiang_wx','88880003','1','1',NULL,NULL,NULL,'技术导向，关注产品技术指标','0','admin','2026-07-19 13:27:35','',NULL,'技术决策人'),(4,3,'刘洋','0','运营总监','运营部','0,1','13600136001','liuyang@cx-fin.com','liuyang_wx','88880004','1','1',NULL,NULL,NULL,'注重ROI和投入产出比','0','admin','2026-07-19 13:27:35','',NULL,'运营负责人'),(5,4,'陈晨','1','市场经理','市场部','2,3','13500135001','chenchen@yunduan-edu.com','chenchen_wx','88880005','1','1',2,NULL,NULL,'年轻有活力，喜欢创新方案','0','admin','2026-07-19 13:27:35','admin','2026-07-21 23:53:00','市场对接人');
/*!40000 ALTER TABLE `mk_contact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_contract`
--

DROP TABLE IF EXISTS `mk_contract`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_contract` (
  `contract_id` bigint NOT NULL AUTO_INCREMENT COMMENT '合同ID',
  `contract_no` varchar(32) NOT NULL COMMENT '合同编号',
  `contract_name` varchar(200) NOT NULL COMMENT '合同名称',
  `contract_type` varchar(10) DEFAULT '0' COMMENT '合同类型（0新签 1续签 2增购 3升级）',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `opportunity_id` bigint DEFAULT NULL COMMENT '关联商机ID',
  `contract_amount` decimal(14,2) NOT NULL COMMENT '合同金额',
  `sign_date` date NOT NULL COMMENT '签约日期',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `expire_date` date NOT NULL COMMENT '到期日期',
  `payment_method` char(1) DEFAULT '0' COMMENT '付款方式（0一次性 1分期）',
  `contract_status` varchar(10) DEFAULT '0' COMMENT '合同状态（0草稿 1审批中 2已生效 3已过期 4已终止）',
  `user_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `attachment` varchar(500) DEFAULT NULL COMMENT '合同附件',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`contract_id`),
  UNIQUE KEY `uk_contract_no` (`contract_no`),
  KEY `idx_contract_customer` (`customer_id`),
  KEY `idx_contract_status` (`contract_status`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_contract`
--

LOCK TABLES `mk_contract` WRITE;
/*!40000 ALTER TABLE `mk_contract` DISABLE KEYS */;
INSERT INTO `mk_contract` VALUES (1,'CON20260719001','智云科技数字平台年度服务合同','0',1,1,580000.00,'2026-03-01','2026-03-01','2027-02-28','1','2',2,103,'/upload/contract/con001.pdf','0','admin','2026-07-19 13:27:35','',NULL,'分期付款，季度回款'),(2,'CON20260719002','宏远制造MES系统实施合同','0',2,2,320000.00,'2026-03-15','2026-04-01','2027-03-31','1','2',2,103,'/upload/contract/con002.pdf','0','admin','2026-07-19 13:27:35','',NULL,'分期付款，含实施服务费'),(3,'CON20260719003','创新金融风控系统合同','0',3,3,150000.00,'2026-04-10','2026-04-15','2027-04-14','0','2',2,103,'/upload/contract/con003.pdf','0','admin','2026-07-19 13:27:35','',NULL,'一次性付款'),(4,'CON20260719004','云端教育SaaS平台年度合同','0',4,4,80000.00,'2026-05-20','2026-06-01','2027-05-31','0','2',2,103,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'年费制'),(5,'CON20260719005','智云科技系统增购合同','2',1,NULL,120000.00,'2026-06-15','2026-07-01','2027-06-30','0','2',2,103,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'增购AI分析模块'),(6,'fdf221221','fddsfdsf','0',5,NULL,0.00,'2026-07-20','2026-07-30','2026-08-06','0','2',2,105,'/profile/upload/2026/07/20/01-应急指挥信息系统项目建设方案V26_20260720122825A001.docx','0',NULL,'2026-07-20 12:27:49','','2026-07-20 13:21:11','2222\n【审批通过】wwwwwwww'),(7,'44444','44','0',5,NULL,0.00,'2026-07-20','2026-07-20','2026-07-28','0','2',100,103,'/profile/upload/2026/07/20/1_襄阳市公安局项目技术建议书 -养犬管理信息系统_20260720152817A001.docx','0',NULL,'2026-07-20 15:28:30','','2026-07-20 15:29:21',NULL);
/*!40000 ALTER TABLE `mk_contract` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_contract_approve_log`
--

DROP TABLE IF EXISTS `mk_contract_approve_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_contract_approve_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `contract_id` bigint NOT NULL COMMENT '合同ID',
  `action_type` varchar(10) NOT NULL COMMENT '操作类型（1提交审批 2审批通过 3审批驳回 4终止）',
  `approve_opinion` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `approve_by` varchar(64) DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_contract_id` (`contract_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同审批记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_contract_approve_log`
--

LOCK TABLES `mk_contract_approve_log` WRITE;
/*!40000 ALTER TABLE `mk_contract_approve_log` DISABLE KEYS */;
INSERT INTO `mk_contract_approve_log` VALUES (1,7,'1','提交审批','admin','2026-07-20 15:28:32'),(2,7,'3','2222222222','admin','2026-07-20 15:28:39'),(3,7,'1','提交审批','admin','2026-07-20 15:29:00'),(4,7,'2','5555555555555555555555555555','admin','2026-07-20 15:29:21');
/*!40000 ALTER TABLE `mk_contract_approve_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_customer`
--

DROP TABLE IF EXISTS `mk_customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_customer` (
  `customer_id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `customer_no` varchar(32) NOT NULL COMMENT '客户编号',
  `customer_name` varchar(200) NOT NULL COMMENT '企业名称',
  `credit_code` varchar(32) DEFAULT NULL COMMENT '统一社会信用代码',
  `customer_level` varchar(10) DEFAULT '3' COMMENT '客户等级（1KA 2重要 3普通 4潜在）',
  `customer_source` varchar(10) DEFAULT '0' COMMENT '客户来源（字典）',
  `industry` varchar(50) DEFAULT NULL COMMENT '所属行业（字典）',
  `company_size` varchar(10) DEFAULT NULL COMMENT '企业规模（字典）',
  `province` varchar(50) DEFAULT NULL COMMENT '省',
  `city` varchar(50) DEFAULT NULL COMMENT '市',
  `district` varchar(50) DEFAULT NULL COMMENT '区',
  `address` varchar(500) DEFAULT NULL COMMENT '详细地址',
  `website` varchar(200) DEFAULT NULL COMMENT '官网',
  `customer_status` varchar(10) DEFAULT '0' COMMENT '客户状态（0活跃 1沉默 2流失）',
  `first_contact_date` date DEFAULT NULL COMMENT '首次接触时间',
  `cooperation_date` date DEFAULT NULL COMMENT '合作开始时间',
  `total_amount` decimal(14,2) DEFAULT '0.00' COMMENT '累计交易额',
  `user_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `uk_customer_no` (`customer_no`),
  KEY `idx_customer_name` (`customer_name`),
  KEY `idx_customer_user` (`user_id`),
  KEY `idx_customer_dept` (`dept_id`),
  KEY `idx_customer_level` (`customer_level`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业客户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_customer`
--

LOCK TABLES `mk_customer` WRITE;
/*!40000 ALTER TABLE `mk_customer` DISABLE KEYS */;
INSERT INTO `mk_customer` VALUES (1,'CUS20260719001','北京智云科技有限公司','91110108MA01ABC123','1','0','0','0','北京市','北京市','海淀区','中关村软件园二期5号楼','www.zhiyun-tech.com','2','2026-01-15','2026-02-01',580000.00,2,103,'0','admin','2026-07-19 13:27:35','',NULL,'关键大客户，长期合作'),(2,'CUS20260719002','上海宏远制造股份有限公司','91310115MA01DEF456','2','1','1','0','上海市','上海市','浦东新区','张江高科技园区博云路2号','www.hongyuan-mfg.cn','2','2026-02-20','2026-03-10',320000.00,2,103,'0','admin','2026-07-19 13:27:35','',NULL,'重要客户，制造业龙头'),(3,'CUS20260719003','深圳创新金融服务有限公司','91440300MA01GHI789','2','2','2','1','广东省','深圳市','南山区','科技园南区高新南一道9号','www.cx-fin.com','2','2026-03-05','2026-04-01',150000.00,2,103,'0','admin','2026-07-19 13:27:35','',NULL,'金融行业客户'),(4,'CUS20260719004','杭州云端教育科技有限公司','91330106MA01JKL012','3','3','3','1','浙江省','杭州市','余杭区','未来科技城梦想小镇','www.yunduan-edu.com','0','2026-04-10',NULL,50000.00,2,103,'0','admin','2026-07-19 13:27:35','',NULL,'教育行业新客户'),(5,'CUS20260719005','广州速达物流有限公司','91440101MA01MNO345','4','4','6','2','广东省','广州市','天河区','天河软件园建中路50号','www.suda-log.com','0','2026-05-15',NULL,0.00,2,103,'0','admin','2026-07-19 13:27:35','',NULL,'潜在客户，待跟进'),(6,'CUS1784448681541','青岛海蓝新能源有限公司',NULL,'4','0','4','1','山东省','青岛市','崂山区',NULL,NULL,'0','2026-07-19',NULL,NULL,2,105,'0','admin','2026-07-19 16:11:21','','2026-07-19 16:12:30',NULL),(7,'CUS2026072100001','????A',NULL,'3',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0',NULL,NULL,NULL,NULL,NULL,'2',NULL,'2026-07-21 21:08:22','',NULL,NULL),(8,'CUS2026072100002','????B',NULL,'3',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0',NULL,NULL,NULL,NULL,NULL,'2',NULL,'2026-07-21 21:08:22','',NULL,NULL),(9,'CUS2026072100003','??????',NULL,'4',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0','2026-07-21',NULL,NULL,NULL,NULL,'2','admin','2026-07-21 21:10:33','',NULL,NULL);
/*!40000 ALTER TABLE `mk_customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_customer_tag`
--

DROP TABLE IF EXISTS `mk_customer_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_customer_tag` (
  `tag_id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
  `tag_type` char(1) DEFAULT '0' COMMENT '标签类型（0手动 1自动）',
  `tag_color` varchar(20) DEFAULT NULL COMMENT '标签颜色',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_customer_tag`
--

LOCK TABLES `mk_customer_tag` WRITE;
/*!40000 ALTER TABLE `mk_customer_tag` DISABLE KEYS */;
INSERT INTO `mk_customer_tag` VALUES (1,'高价值','0','#f56c6c',1,'0','admin','2026-07-19 13:27:35','',NULL,'高价值客户标签'),(2,'制造业','0','#409eff',2,'0','admin','2026-07-19 13:27:35','',NULL,'制造业客户标签'),(3,'金融','0','#67c23a',3,'0','admin','2026-07-19 13:27:35','',NULL,'金融行业标签'),(4,'待激活','0','#e6a23c',4,'0','admin','2026-07-19 13:27:35','',NULL,'待激活客户标签'),(5,'续约中','0','#909399',5,'0','admin','2026-07-19 13:27:35','',NULL,'续约中客户标签');
/*!40000 ALTER TABLE `mk_customer_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_customer_tag_rel`
--

DROP TABLE IF EXISTS `mk_customer_tag_rel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_customer_tag_rel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_tag` (`customer_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_customer_tag_rel`
--

LOCK TABLES `mk_customer_tag_rel` WRITE;
/*!40000 ALTER TABLE `mk_customer_tag_rel` DISABLE KEYS */;
/*!40000 ALTER TABLE `mk_customer_tag_rel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_interaction`
--

DROP TABLE IF EXISTS `mk_interaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_interaction` (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `contact_id` bigint DEFAULT NULL COMMENT '联系人ID',
  `opportunity_id` bigint DEFAULT NULL COMMENT '商机ID',
  `lead_id` bigint DEFAULT NULL COMMENT '线索ID',
  `interact_type` varchar(10) NOT NULL COMMENT '互动类型（字典）',
  `interact_time` datetime NOT NULL COMMENT '互动时间',
  `content` text COMMENT '互动内容',
  `user_id` bigint DEFAULT NULL COMMENT '跟进人ID',
  `next_time` datetime DEFAULT NULL COMMENT '下次跟进时间',
  `next_content` varchar(500) DEFAULT NULL COMMENT '下次跟进内容',
  `attachment` varchar(500) DEFAULT NULL COMMENT '附件URL',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`record_id`),
  KEY `idx_interaction_customer` (`customer_id`),
  KEY `idx_interaction_lead` (`lead_id`),
  KEY `idx_interaction_opp` (`opportunity_id`),
  KEY `idx_interaction_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='互动记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_interaction`
--

LOCK TABLES `mk_interaction` WRITE;
/*!40000 ALTER TABLE `mk_interaction` DISABLE KEYS */;
INSERT INTO `mk_interaction` VALUES (1,1,1,NULL,NULL,'1','2026-07-10 10:00:00','拜访智云科技张总，讨论下半年的系统升级方案，客户表示预算充足，预计Q3启动项目',2,'2026-07-25 10:00:00','发送详细方案报价',NULL,'0','admin','2026-07-19 13:27:35','',NULL),(2,1,2,NULL,NULL,'0','2026-07-12 14:30:00','电话跟进采购经理李娜，确认采购流程和审批节点，对方表示需要走季度审批',2,'2026-07-20 14:00:00','准备采购流程文档',NULL,'0','admin','2026-07-19 13:27:35','',NULL),(3,2,3,NULL,NULL,'3','2026-07-08 09:00:00','与宏远制造王总视频会议，演示MES系统对接方案，客户反馈积极',2,'2026-07-18 10:00:00','安排现场POC测试',NULL,'0','admin','2026-07-19 13:27:35','',NULL),(4,3,4,NULL,NULL,'4','2026-07-15 11:00:00','微信沟通刘总，发送产品白皮书和案例参考，客户对金融行业案例感兴趣',2,'2026-07-22 15:00:00','安排金融行业案例分享会',NULL,'0','admin','2026-07-19 13:27:35','',NULL),(5,4,5,NULL,NULL,'2','2026-07-16 16:00:00','发送方案邮件给陈晨，包含SaaS教育平台功能清单和定价方案',2,'2026-07-23 10:00:00','电话确认邮件接收情况',NULL,'0','admin','2026-07-19 13:27:35','',NULL);
/*!40000 ALTER TABLE `mk_interaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_lead`
--

DROP TABLE IF EXISTS `mk_lead`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_lead` (
  `lead_id` bigint NOT NULL AUTO_INCREMENT COMMENT '线索ID',
  `lead_no` varchar(32) NOT NULL COMMENT '线索编号',
  `company_name` varchar(200) NOT NULL COMMENT '企业名称',
  `contact_name` varchar(50) NOT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系人手机号',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系人邮箱',
  `position` varchar(100) DEFAULT NULL COMMENT '职位',
  `industry` varchar(50) DEFAULT NULL COMMENT '所属行业',
  `company_size` varchar(10) DEFAULT NULL COMMENT '企业规模',
  `province` varchar(50) DEFAULT NULL COMMENT '省',
  `city` varchar(50) DEFAULT NULL COMMENT '市',
  `district` varchar(50) DEFAULT NULL COMMENT '区',
  `address` varchar(500) DEFAULT NULL COMMENT '详细地址',
  `lead_source` varchar(10) DEFAULT '0' COMMENT '线索来源（字典）',
  `activity_id` bigint DEFAULT NULL COMMENT '来源活动ID',
  `requirement_desc` text COMMENT '需求描述',
  `lead_score` int DEFAULT '0' COMMENT '线索评分（0-100）',
  `lead_grade` varchar(2) DEFAULT 'D' COMMENT '线索等级（A/B/C/D）',
  `lead_status` varchar(10) DEFAULT '0' COMMENT '线索状态（0新线索 1培育中 2MQL 3SQL 4已转化 5已无效）',
  `is_public` char(1) DEFAULT '0' COMMENT '是否在公海（0否 1是）',
  `user_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `receive_time` datetime DEFAULT NULL COMMENT '领取时间',
  `last_follow_time` datetime DEFAULT NULL COMMENT '最后跟进时间',
  `ineffective_reason` varchar(10) DEFAULT NULL COMMENT '无效原因',
  `ineffective_remark` varchar(500) DEFAULT NULL COMMENT '无效说明',
  `convert_customer_id` bigint DEFAULT NULL COMMENT '转化后的客户ID',
  `convert_time` datetime DEFAULT NULL COMMENT '转化时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `receive_status` varchar(10) DEFAULT '0' COMMENT '领取审批状态',
  `receive_remark` varchar(500) DEFAULT NULL COMMENT '领取审批备注',
  `receive_apply_user_id` bigint DEFAULT NULL COMMENT '领取申请人ID',
  `receive_apply_time` datetime DEFAULT NULL COMMENT '领取申请时间',
  PRIMARY KEY (`lead_id`),
  UNIQUE KEY `uk_lead_no` (`lead_no`),
  KEY `idx_lead_company` (`company_name`),
  KEY `idx_lead_phone` (`contact_phone`),
  KEY `idx_lead_status` (`lead_status`),
  KEY `idx_lead_user` (`user_id`),
  KEY `idx_lead_public` (`is_public`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='线索表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_lead`
--

LOCK TABLES `mk_lead` WRITE;
/*!40000 ALTER TABLE `mk_lead` DISABLE KEYS */;
INSERT INTO `mk_lead` VALUES (1,'LEAD20260719001','成都新视界科技有限公司','赵敏','13300133001','zhaomin@xinshijie.com','市场总监','0','1','四川省','成都市','高新区','四川省成都市高新区','0',NULL,'需要一套营销自动化系统，团队规模50人，预算30万',85,'A','3','1',NULL,103,'2026-07-19 16:27:20','2026-07-15 14:00:00',NULL,NULL,NULL,NULL,'0','admin','2026-07-19 13:27:35','admin','2026-07-19 16:56:59','高质量线索，意向明确','1',NULL,1,'2026-07-19 16:56:59'),(2,'LEAD20260719002','武汉光谷生物医药有限公司','孙磊','13400134001','sunlei@guanggu-bio.com','IT经理','4','0','湖北省','武汉市','洪山区','湖北省武汉市洪山区','1',NULL,'实验室信息管理系统需求，需GMP合规',72,'B','2','1',NULL,103,'2026-07-08 10:00:00','2026-07-14 16:00:00',NULL,NULL,NULL,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'医药行业线索','0',NULL,NULL,NULL),(3,'LEAD20260719003','南京紫金山智能制造有限公司','周婷','13200132001','zhouting@zjs-mfg.com','项目经理','1','0','江苏省','南京市','江宁区','江苏省南京市江宁区','2',NULL,'智能工厂改造项目，需要设备联网和数据采集平台',68,'B','1','1',NULL,103,'2026-07-19 16:13:54','2026-07-16 09:00:00',NULL,NULL,NULL,NULL,'0','admin','2026-07-19 13:27:35','admin','2026-07-19 16:13:54','转介绍线索','0',NULL,NULL,NULL),(4,'LEAD20260719004','西安丝路电商有限公司','吴昊','13100131001','wuhao@silkroad-ec.com','创始人','0','2','陕西省','西安市','雁塔区','陕西省西安市雁塔区','3',NULL,'需要跨境电商管理系统，预算10万以内',45,'C','0','0',1,103,'2026-07-19 16:01:21',NULL,NULL,NULL,NULL,NULL,'0','admin','2026-07-19 13:27:35','admin','2026-07-19 16:01:21','公海线索，待领取','0',NULL,NULL,NULL),(5,'LEAD20260719005','青岛海蓝新能源有限公司','郑凯','13000130001','zhengkai@hailan-energy.com','技术总监','4','1','山东省','青岛市','崂山区','山东省青岛市崂山区','0',NULL,'储能监控系统需求，需对接现有SCADA',52,'C','4','0',2,105,'2026-07-19 13:47:48',NULL,NULL,NULL,6,'2026-07-19 16:11:21','0','admin','2026-07-19 13:27:35','admin','2026-07-19 16:12:11','公海线索，待领取','0',NULL,NULL,NULL),(6,'XS2026072100001','??????','?????','13800000001',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,'D','4','1',NULL,NULL,NULL,NULL,NULL,NULL,9,'2026-07-21 21:10:33','2',NULL,'2026-07-21 21:08:40','admin','2026-07-21 21:10:33',NULL,'0',NULL,NULL,NULL),(7,'XS20260721002','555','55','55','55','55',NULL,NULL,NULL,NULL,NULL,NULL,'活动',9,NULL,0,'D','0','1',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0','admin','2026-07-21 23:53:16','',NULL,NULL,'0',NULL,NULL,NULL);
/*!40000 ALTER TABLE `mk_lead` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_number_rule`
--

DROP TABLE IF EXISTS `mk_number_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_number_rule` (
  `rule_id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `rule_code` varchar(50) NOT NULL COMMENT '规则编码（业务标识，如 activity/customer/lead/opportunity/order）',
  `module` varchar(20) DEFAULT 'mk' COMMENT '所属模块(mk=营销,dms=设备,wms=仓储)',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `prefix` varchar(20) DEFAULT '' COMMENT '编号前缀',
  `date_format` varchar(20) DEFAULT '' COMMENT '日期格式（yyyyMMdd=按天,yyyyMM=按月,yyyy=按年,空=不包含日期）',
  `reset_type` char(1) DEFAULT '0' COMMENT '序号重置类型（0=不重置,1=按天,2=按月,3=按年）',
  `seq_length` int DEFAULT '4' COMMENT '序列号长度（不足前补零）',
  `seq_start` bigint DEFAULT '1' COMMENT '序列号起始值',
  `step` int DEFAULT '1' COMMENT '每次增长步长',
  `current_seq` bigint DEFAULT '0' COMMENT '当前序列号',
  `current_date_str` varchar(20) DEFAULT '' COMMENT '当前日期串（用于判断是否需要重置序列号）',
  `connector` varchar(5) DEFAULT '' COMMENT '前缀与日期之间的连接符',
  `status` char(1) DEFAULT '0' COMMENT '状态（0=正常,1=停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`rule_id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='编号规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_number_rule`
--

LOCK TABLES `mk_number_rule` WRITE;
/*!40000 ALTER TABLE `mk_number_rule` DISABLE KEYS */;
INSERT INTO `mk_number_rule` VALUES (1,'activity','mk','活动编号','HD','yyyyMM','2',3,1,1,2,'202607','','0','admin','2026-07-21 20:33:52','','2026-07-21 23:53:00','营销活动编号，每月重置'),(2,'customer','mk','客户编号','CUS','yyyy','3',3,1,1,3,'20260721','','0','admin','2026-07-21 20:33:52','','2026-07-21 21:34:42','企业客户编号，每年重置'),(3,'lead','mk','线索编号','XS','yyyyMMdd','1',3,1,1,2,'20260721','','0','admin','2026-07-21 20:33:52','','2026-07-21 23:53:16','线索编号，每日重置'),(4,'opportunity','mk','商机编号','SJ','yyyyMMdd','1',3,1,1,1,'20260721','','0','admin','2026-07-21 20:33:52','','2026-07-21 21:32:08','商机编号，每日重置'),(5,'order','mk','订单编号','DD','yyyyMMdd','1',3,1,1,1,'20260721','','0','admin','2026-07-21 20:33:52','','2026-07-21 21:32:19','订单编号，每日重置'),(7,'contract','mk','合同编号','HT','yyyy','3',3,1,1,0,'','','0','admin','2026-07-21 21:35:57','','2026-07-21 22:02:52','合同编号，每年重置'),(8,'dms_equipment','dms','设备编号','SB','yyyyMMdd','1',4,1,1,0,'','','0','admin','2026-07-21 22:17:54','','2026-07-21 22:40:23','设备编号，每日重置'),(9,'dms_spare_part','dms','备件编号','BJ','yyyyMMdd','1',4,1,1,0,'','','0','admin','2026-07-21 22:17:54','','2026-07-21 22:41:13','备件编号，每日重置'),(10,'dms_spare_in','dms','备件入库单据号','BJIN','yyyyMMdd','1',4,1,1,0,'','','0','admin','2026-07-21 22:17:54','',NULL,'备件入库单据号，每日重置'),(11,'dms_spare_out','dms','备件出库单据号','BJOUT','yyyyMMdd','1',4,1,1,0,'','','0','admin','2026-07-21 22:17:54','',NULL,'备件出库单据号，每日重置'),(12,'dms_work_order','dms','工单号','WO','yyyyMMdd','1',4,1,1,0,'','','0','admin','2026-07-21 22:17:54','','2026-07-21 22:41:13','维修工单号，每日重置'),(13,'dms_inspection_route','dms','巡检路线编号','RT','yyyyMMdd','1',4,1,1,0,'','','0','admin','2026-07-21 22:17:54','','2026-07-21 22:41:13','巡检路线编号，每日重置'),(14,'dms_inspection_task','dms','点检任务编号','INS','yyyyMMdd','1',4,1,1,0,'','','0','admin','2026-07-21 22:17:54','','2026-07-21 22:46:50','点检任务编号，每日重置'),(15,'wms_material','wms','物料编号','WL','yyyyMMdd','1',5,1,1,0,'','','0','admin','2026-07-21 22:17:54','','2026-07-21 22:41:50','物料编号，每日重置'),(16,'wms_warehouse','wms','仓库编码','WH','','0',3,1,1,10,'','','0','admin','2026-07-21 22:17:54','','2026-07-21 22:44:06','仓库编码，不重置'),(17,'wms_supplier','wms','供应商编码','GYS','yyyyMMdd','1',4,1,1,0,'','','0','admin','2026-07-21 22:17:54','','2026-07-21 22:41:50','供应商编码，每日重置'),(18,'wms_inbound','wms','入库单号','IN','yyyyMMdd','1',5,1,1,0,'','','0','admin','2026-07-21 22:17:54','','2026-07-21 22:41:50','入库单号，每日重置'),(19,'wms_outbound','wms','出库单号','OUT','yyyyMMdd','1',5,1,1,0,'','','0','admin','2026-07-21 22:17:54','','2026-07-21 22:45:35','出库单号，每日重置'),(20,'wms_move','wms','移库单号','MV','yyyyMMdd','1',5,1,1,0,'','','0','admin','2026-07-21 22:17:54','',NULL,'移库单号，每日重置'),(21,'wms_stocktake','wms','盘点单号','ST','yyyyMMdd','1',5,1,1,0,'','','0','admin','2026-07-21 22:17:54','','2026-07-21 22:45:35','盘点单号，每日重置');
/*!40000 ALTER TABLE `mk_number_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_opportunity`
--

DROP TABLE IF EXISTS `mk_opportunity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_opportunity` (
  `opportunity_id` bigint NOT NULL AUTO_INCREMENT COMMENT '商机ID',
  `opportunity_no` varchar(32) NOT NULL COMMENT '商机编号',
  `opportunity_name` varchar(200) NOT NULL COMMENT '商机名称',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `contact_id` bigint DEFAULT NULL COMMENT '主要联系人ID',
  `lead_id` bigint DEFAULT NULL COMMENT '来源线索ID',
  `opportunity_source` varchar(10) DEFAULT '0' COMMENT '商机来源（字典）',
  `expected_amount` decimal(14,2) NOT NULL COMMENT '预计金额',
  `expected_date` date NOT NULL COMMENT '预计成交日期',
  `stage_code` varchar(20) DEFAULT 'stage1' COMMENT '销售阶段编码',
  `stage_name` varchar(50) DEFAULT NULL COMMENT '销售阶段名称',
  `win_rate` int DEFAULT '10' COMMENT '赢率（%）',
  `weighted_amount` decimal(14,2) DEFAULT '0.00' COMMENT '加权金额',
  `opportunity_status` varchar(10) DEFAULT '0' COMMENT '商机状态（0进行中 1已赢单 2已输单）',
  `competitor` varchar(200) DEFAULT NULL COMMENT '竞争对手',
  `pain_point` text COMMENT '客户痛点',
  `solution` text COMMENT '解决方案',
  `lost_reason` varchar(10) DEFAULT NULL COMMENT '输单原因',
  `lost_remark` varchar(500) DEFAULT NULL COMMENT '输单说明',
  `actual_amount` decimal(14,2) DEFAULT NULL COMMENT '实际成交金额',
  `actual_date` date DEFAULT NULL COMMENT '实际成交日期',
  `user_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `next_action` varchar(500) DEFAULT NULL COMMENT '下一步行动',
  `next_time` datetime DEFAULT NULL COMMENT '下次行动时间',
  `stage_enter_time` datetime DEFAULT NULL COMMENT '进入当前阶段时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`opportunity_id`),
  UNIQUE KEY `uk_opportunity_no` (`opportunity_no`),
  KEY `idx_opp_customer` (`customer_id`),
  KEY `idx_opp_stage` (`stage_code`),
  KEY `idx_opp_status` (`opportunity_status`),
  KEY `idx_opp_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商机表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_opportunity`
--

LOCK TABLES `mk_opportunity` WRITE;
/*!40000 ALTER TABLE `mk_opportunity` DISABLE KEYS */;
INSERT INTO `mk_opportunity` VALUES (1,'OPP20260719001','智云科技数字化升级项目',1,1,NULL,'1',580000.00,'2026-09-30','stage4','方案演示',55,319000.00,'0','金蝶、用友','现有系统老旧，数据孤岛严重，需要统一数据平台','提供一体化数字运营平台，含数据中台+业务系统',NULL,NULL,NULL,NULL,2,103,'安排POC测试环境','2026-07-25 10:00:00','2026-07-10 10:00:00','0','admin','2026-07-19 13:27:35','',NULL,'重点商机'),(2,'OPP20260719002','宏远制造MES系统项目',2,3,NULL,'2',320000.00,'2026-08-31','stage3','方案制定',40,128000.00,'0','西门子','生产数据手工记录，效率低，质量追溯困难','MES系统+设备联网+看板',NULL,NULL,NULL,NULL,2,103,'完善技术方案','2026-07-20 14:00:00','2026-07-08 09:00:00','0','admin','2026-07-19 13:27:35','',NULL,'制造业MES需求'),(3,'OPP20260719003','创新金融风控系统项目',3,4,NULL,'0',150000.00,'2026-10-15','stage2','需求确认',25,37500.00,'0',NULL,'风控模型老化，需要AI辅助决策','AI风控引擎+实时预警系统',NULL,NULL,NULL,NULL,2,103,'确认技术需求细节','2026-07-22 15:00:00','2026-07-05 10:00:00','0','admin','2026-07-19 13:27:35','',NULL,'金融风控需求'),(4,'OPP20260719004','云端教育SaaS平台项目',4,5,NULL,'3',80000.00,'2026-08-15','stage5','商务谈判',75,60000.00,'0',NULL,'缺乏在线教学管理工具','SaaS教育平台年费方案',NULL,NULL,NULL,NULL,2,103,'发送合同草案','2026-07-23 10:00:00','2026-07-12 14:00:00','0','admin','2026-07-19 13:27:35','',NULL,'即将成交'),(5,'OPP20260719005','速达物流TMS系统项目',5,NULL,NULL,'2',50000.00,'2026-11-30','stage1','初步接触',10,5000.00,'0',NULL,'运输管理依赖人工调度，效率低','TMS运输管理系统',NULL,NULL,NULL,NULL,2,103,'首次拜访客户','2026-07-28 09:00:00','2026-07-15 10:00:00','0','admin','2026-07-19 13:27:35','',NULL,'初步接触阶段'),(6,'SJ2026072100001','????',7,NULL,NULL,NULL,10000.00,'2026-08-01','stage1','????',10,1000.00,'0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2',NULL,'2026-07-21 21:08:40','',NULL,NULL);
/*!40000 ALTER TABLE `mk_opportunity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_opportunity_stage`
--

DROP TABLE IF EXISTS `mk_opportunity_stage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_opportunity_stage` (
  `stage_id` bigint NOT NULL AUTO_INCREMENT COMMENT '阶段ID',
  `stage_code` varchar(20) NOT NULL COMMENT '阶段编码',
  `stage_name` varchar(50) NOT NULL COMMENT '阶段名称',
  `sort` int NOT NULL COMMENT '排序号',
  `win_rate` int DEFAULT '0' COMMENT '赢率（%）',
  `max_days` int DEFAULT NULL COMMENT '最大停留天数',
  `required_fields` varchar(500) DEFAULT NULL COMMENT '阶段推进必填字段',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`stage_id`),
  UNIQUE KEY `uk_stage_code` (`stage_code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售阶段配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_opportunity_stage`
--

LOCK TABLES `mk_opportunity_stage` WRITE;
/*!40000 ALTER TABLE `mk_opportunity_stage` DISABLE KEYS */;
INSERT INTO `mk_opportunity_stage` VALUES (1,'stage1','初步接触',1,10,30,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'首次接触，建立联系'),(2,'stage2','需求确认',2,25,30,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'确认客户需求和预算'),(3,'stage3','方案制定',3,40,45,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'制定解决方案'),(4,'stage4','方案演示',4,55,30,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'方案演示和答疑'),(5,'stage5','商务谈判',5,75,30,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'价格和条款谈判'),(6,'stage6','合同签订',6,90,15,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'合同审批和签订');
/*!40000 ALTER TABLE `mk_opportunity_stage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_order`
--

DROP TABLE IF EXISTS `mk_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单编号',
  `contract_id` bigint DEFAULT NULL COMMENT '关联合同ID',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `order_amount` decimal(14,2) NOT NULL COMMENT '订单金额',
  `order_status` varchar(10) DEFAULT '0' COMMENT '订单状态（0待发货 1已发货 2已完成 3已取消）',
  `logistics_company` varchar(50) DEFAULT NULL COMMENT '物流公司',
  `tracking_no` varchar(50) DEFAULT NULL COMMENT '物流单号',
  `deliver_time` datetime DEFAULT NULL COMMENT '发货时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `user_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_customer` (`customer_id`),
  KEY `idx_order_contract` (`contract_id`),
  KEY `idx_order_status` (`order_status`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_order`
--

LOCK TABLES `mk_order` WRITE;
/*!40000 ALTER TABLE `mk_order` DISABLE KEYS */;
INSERT INTO `mk_order` VALUES (1,'ORD20260719001',1,1,145000.00,'2',NULL,NULL,NULL,'2026-06-01 00:00:00',2,103,'0','admin','2026-07-19 13:27:35','',NULL,'Q1回款订单'),(2,'ORD20260719002',1,1,145000.00,'2',NULL,NULL,NULL,'2026-06-01 00:00:00',2,103,'0','admin','2026-07-19 13:27:35','',NULL,'Q2回款订单'),(3,'ORD20260719003',2,2,160000.00,'1','顺丰速运','SF1234567890','2026-05-20 10:00:00',NULL,2,103,'0','admin','2026-07-19 13:27:35','',NULL,'首期实施费'),(4,'ORD20260719004',3,3,150000.00,'2',NULL,NULL,NULL,'2026-05-01 00:00:00',2,103,'0','admin','2026-07-19 13:27:35','',NULL,'一次性付款订单'),(5,'ORD20260719005',5,1,120000.00,'0',NULL,NULL,NULL,NULL,2,103,'0','admin','2026-07-19 13:27:35','',NULL,'增购订单待发货'),(6,'DD2026072100001',NULL,7,5000.00,'0',NULL,NULL,NULL,NULL,NULL,NULL,'2',NULL,'2026-07-21 21:08:40','',NULL,NULL);
/*!40000 ALTER TABLE `mk_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_order_item`
--

DROP TABLE IF EXISTS `mk_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_order_item` (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称',
  `product_spec` varchar(200) DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `quantity` int NOT NULL COMMENT '数量',
  `unit_price` decimal(14,2) NOT NULL COMMENT '单价',
  `subtotal` decimal(14,2) NOT NULL COMMENT '小计',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`item_id`),
  KEY `idx_order_item_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_order_item`
--

LOCK TABLES `mk_order_item` WRITE;
/*!40000 ALTER TABLE `mk_order_item` DISABLE KEYS */;
INSERT INTO `mk_order_item` VALUES (1,1,'数字平台基础版','V3.0','套',1,100000.00,100000.00,'2026-07-19 13:27:35','基础平台授权'),(2,1,'数据中台模块','V3.0','套',1,45000.00,45000.00,'2026-07-19 13:27:35','数据中台授权'),(3,2,'数字平台基础版','V3.0','套',1,100000.00,100000.00,'2026-07-19 13:27:35','Q2续费'),(4,2,'AI分析模块','V3.0','套',1,45000.00,45000.00,'2026-07-19 13:27:35','AI模块授权'),(5,3,'MES系统核心模块','V2.0','套',1,120000.00,120000.00,'2026-07-19 13:27:35','MES核心授权'),(6,3,'设备联网网关','GW-200','台',8,5000.00,40000.00,'2026-07-19 13:27:35','设备联网'),(7,4,'AI风控引擎','V1.0','套',1,150000.00,150000.00,'2026-07-19 13:27:35','风控引擎授权'),(8,5,'AI分析模块','V3.0','套',1,80000.00,80000.00,'2026-07-19 13:27:35','增购AI模块'),(9,5,'数据可视化插件','V3.0','套',2,20000.00,40000.00,'2026-07-19 13:27:35','可视化插件');
/*!40000 ALTER TABLE `mk_order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mk_payment_plan`
--

DROP TABLE IF EXISTS `mk_payment_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mk_payment_plan` (
  `plan_id` bigint NOT NULL AUTO_INCREMENT COMMENT '计划ID',
  `contract_id` bigint NOT NULL COMMENT '合同ID',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `period_no` int NOT NULL COMMENT '期次',
  `plan_amount` decimal(14,2) NOT NULL COMMENT '计划回款金额',
  `plan_date` date NOT NULL COMMENT '计划回款日期',
  `actual_amount` decimal(14,2) DEFAULT '0.00' COMMENT '实际回款金额',
  `actual_date` date DEFAULT NULL COMMENT '实际回款日期',
  `payment_status` varchar(10) DEFAULT '0' COMMENT '回款状态（0待回款 1已回款 2已逾期）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`plan_id`),
  KEY `idx_payment_contract` (`contract_id`),
  KEY `idx_payment_customer` (`customer_id`),
  KEY `idx_payment_status` (`payment_status`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='回款计划表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mk_payment_plan`
--

LOCK TABLES `mk_payment_plan` WRITE;
/*!40000 ALTER TABLE `mk_payment_plan` DISABLE KEYS */;
INSERT INTO `mk_payment_plan` VALUES (1,1,1,1,145000.00,'2026-03-01',145000.00,'2026-03-05','1','admin','2026-07-19 13:27:35','',NULL,'首期款已回款'),(2,1,1,2,145000.00,'2026-06-01',145000.00,'2026-06-03','1','admin','2026-07-19 13:27:35','',NULL,'二期款已回款'),(3,1,1,3,145000.00,'2026-09-01',0.00,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'三期款待回款'),(4,1,1,4,145000.00,'2026-12-01',0.00,NULL,'0','admin','2026-07-19 13:27:35','',NULL,'尾款待回款'),(5,2,2,1,160000.00,'2026-04-01',160000.00,'2026-04-10','1','admin','2026-07-19 13:27:35','',NULL,'首期款已回款'),(8,6,5,1,10000.00,'2026-07-23',0.00,NULL,'0',NULL,'2026-07-20 12:28:27','',NULL,NULL),(9,6,5,2,20000.00,'2026-07-27',0.00,NULL,'0',NULL,'2026-07-20 12:28:27','',NULL,NULL);
/*!40000 ALTER TABLE `mk_payment_plan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `openapi_app`
--

DROP TABLE IF EXISTS `openapi_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `openapi_app` (
  `app_id` bigint NOT NULL AUTO_INCREMENT COMMENT '应用ID',
  `app_name` varchar(100) NOT NULL COMMENT '应用名称（对接方）',
  `app_key` varchar(128) NOT NULL COMMENT 'API Key',
  `allowed_modules` varchar(500) DEFAULT '*' COMMENT '允许访问的模块，逗号分隔如 wms,dms；* 表示全部',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`app_id`),
  UNIQUE KEY `uk_app_key` (`app_key`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='开放接口应用表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `openapi_app`
--

LOCK TABLES `openapi_app` WRITE;
/*!40000 ALTER TABLE `openapi_app` DISABLE KEYS */;
INSERT INTO `openapi_app` VALUES (1,'默认测试应用','test-api-key-2026','*','0','0','admin','2026-07-18 23:12:10','admin','2026-07-18 23:24:54','默认测试Key，生产环境请替换');
/*!40000 ALTER TABLE `openapi_app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_blob_triggers`
--

DROP TABLE IF EXISTS `qrtz_blob_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_blob_triggers` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `blob_data` blob COMMENT '存放持久化Trigger对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Blob类型的触发器表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_blob_triggers`
--

LOCK TABLES `qrtz_blob_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_blob_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_blob_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_calendars`
--

DROP TABLE IF EXISTS `qrtz_calendars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_calendars` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `calendar_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '日历名称',
  `calendar` blob NOT NULL COMMENT '存放持久化calendar对象',
  PRIMARY KEY (`sched_name`,`calendar_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='日历信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_calendars`
--

LOCK TABLES `qrtz_calendars` WRITE;
/*!40000 ALTER TABLE `qrtz_calendars` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_calendars` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_cron_triggers`
--

DROP TABLE IF EXISTS `qrtz_cron_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_cron_triggers` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `cron_expression` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cron表达式',
  `time_zone_id` varchar(80) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '时区',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Cron类型的触发器表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_cron_triggers`
--

LOCK TABLES `qrtz_cron_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_cron_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_cron_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_fired_triggers`
--

DROP TABLE IF EXISTS `qrtz_fired_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_fired_triggers` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `entry_id` varchar(95) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例id',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `instance_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例名',
  `fired_time` bigint NOT NULL COMMENT '触发的时间',
  `sched_time` bigint NOT NULL COMMENT '定时器制定的时间',
  `priority` int NOT NULL COMMENT '优先级',
  `state` varchar(16) COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
  `job_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务组名',
  `is_nonconcurrent` varchar(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否并发',
  `requests_recovery` varchar(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否接受恢复执行',
  PRIMARY KEY (`sched_name`,`entry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='已触发的触发器表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_fired_triggers`
--

LOCK TABLES `qrtz_fired_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_fired_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_fired_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_job_details`
--

DROP TABLE IF EXISTS `qrtz_job_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_job_details` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `job_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `description` varchar(250) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '相关介绍',
  `job_class_name` varchar(250) COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行任务类名称',
  `is_durable` varchar(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否持久化',
  `is_nonconcurrent` varchar(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否并发',
  `is_update_data` varchar(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否更新数据',
  `requests_recovery` varchar(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否接受恢复执行',
  `job_data` blob COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`job_name`,`job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务详细信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_job_details`
--

LOCK TABLES `qrtz_job_details` WRITE;
/*!40000 ALTER TABLE `qrtz_job_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_job_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_locks`
--

DROP TABLE IF EXISTS `qrtz_locks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_locks` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `lock_name` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '悲观锁名称',
  PRIMARY KEY (`sched_name`,`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='存储的悲观锁信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_locks`
--

LOCK TABLES `qrtz_locks` WRITE;
/*!40000 ALTER TABLE `qrtz_locks` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_locks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_paused_trigger_grps`
--

DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_paused_trigger_grps` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  PRIMARY KEY (`sched_name`,`trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='暂停的触发器表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_paused_trigger_grps`
--

LOCK TABLES `qrtz_paused_trigger_grps` WRITE;
/*!40000 ALTER TABLE `qrtz_paused_trigger_grps` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_paused_trigger_grps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_scheduler_state`
--

DROP TABLE IF EXISTS `qrtz_scheduler_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_scheduler_state` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `instance_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '实例名称',
  `last_checkin_time` bigint NOT NULL COMMENT '上次检查时间',
  `checkin_interval` bigint NOT NULL COMMENT '检查间隔时间',
  PRIMARY KEY (`sched_name`,`instance_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='调度器状态表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_scheduler_state`
--

LOCK TABLES `qrtz_scheduler_state` WRITE;
/*!40000 ALTER TABLE `qrtz_scheduler_state` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_scheduler_state` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_simple_triggers`
--

DROP TABLE IF EXISTS `qrtz_simple_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_simple_triggers` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `repeat_count` bigint NOT NULL COMMENT '重复的次数统计',
  `repeat_interval` bigint NOT NULL COMMENT '重复的间隔时间',
  `times_triggered` bigint NOT NULL COMMENT '已经触发的次数',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='简单触发器的信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_simple_triggers`
--

LOCK TABLES `qrtz_simple_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_simple_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_simple_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_simprop_triggers`
--

DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_simprop_triggers` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `str_prop_1` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第一个参数',
  `str_prop_2` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第二个参数',
  `str_prop_3` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第三个参数',
  `int_prop_1` int DEFAULT NULL COMMENT 'int类型的trigger的第一个参数',
  `int_prop_2` int DEFAULT NULL COMMENT 'int类型的trigger的第二个参数',
  `long_prop_1` bigint DEFAULT NULL COMMENT 'long类型的trigger的第一个参数',
  `long_prop_2` bigint DEFAULT NULL COMMENT 'long类型的trigger的第二个参数',
  `dec_prop_1` decimal(13,4) DEFAULT NULL COMMENT 'decimal类型的trigger的第一个参数',
  `dec_prop_2` decimal(13,4) DEFAULT NULL COMMENT 'decimal类型的trigger的第二个参数',
  `bool_prop_1` varchar(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Boolean类型的trigger的第一个参数',
  `bool_prop_2` varchar(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Boolean类型的trigger的第二个参数',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='同步机制的行锁表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_simprop_triggers`
--

LOCK TABLES `qrtz_simprop_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_simprop_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_simprop_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_triggers`
--

DROP TABLE IF EXISTS `qrtz_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_triggers` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器的名字',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器所属组的名字',
  `job_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details表job_name的外键',
  `job_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details表job_group的外键',
  `description` varchar(250) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '相关介绍',
  `next_fire_time` bigint DEFAULT NULL COMMENT '上一次触发时间（毫秒）',
  `prev_fire_time` bigint DEFAULT NULL COMMENT '下一次触发时间（默认为-1表示不触发）',
  `priority` int DEFAULT NULL COMMENT '优先级',
  `trigger_state` varchar(16) COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器状态',
  `trigger_type` varchar(8) COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器的类型',
  `start_time` bigint NOT NULL COMMENT '开始时间',
  `end_time` bigint DEFAULT NULL COMMENT '结束时间',
  `calendar_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '日程表名称',
  `misfire_instr` smallint DEFAULT NULL COMMENT '补偿执行的策略',
  `job_data` blob COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  KEY `sched_name` (`sched_name`,`job_name`,`job_group`),
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='触发器详细信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_triggers`
--

LOCK TABLES `qrtz_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_alarm_log`
--

DROP TABLE IF EXISTS `sys_alarm_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_alarm_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `alarm_type` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Alarm type',
  `alarm_level` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Alarm level (INFO/WARN/ERROR)',
  `alarm_title` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Alarm title',
  `alarm_detail` text COLLATE utf8mb4_general_ci COMMENT 'Alarm detail',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT 'Status (0=unprocessed 1=processed)',
  `create_time` datetime DEFAULT NULL COMMENT 'Create time',
  `handle_time` datetime DEFAULT NULL COMMENT 'Handle time',
  `handle_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Handler',
  PRIMARY KEY (`id`),
  KEY `idx_alarm_type` (`alarm_type`),
  KEY `idx_alarm_level` (`alarm_level`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='System alarm log';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_alarm_log`
--

LOCK TABLES `sys_alarm_log` WRITE;
/*!40000 ALTER TABLE `sys_alarm_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_alarm_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) COLLATE utf8mb4_general_ci DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` VALUES (1,'主框架页-默认皮肤样式名称','sys.index.skinName','skin-blue','Y','admin','2026-07-13 11:25:53','',NULL,'蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow'),(2,'用户管理-账号初始密码','sys.user.initPassword','123456','Y','admin','2026-07-13 11:25:53','',NULL,'初始化密码 123456'),(3,'主框架页-侧边栏主题','sys.index.sideTheme','theme-dark','Y','admin','2026-07-13 11:25:53','',NULL,'深色主题theme-dark，浅色主题theme-light'),(4,'账号自助-验证码开关','sys.account.captchaEnabled','true','Y','admin','2026-07-13 11:25:53','',NULL,'是否开启验证码功能（true开启，false关闭）'),(5,'账号自助-是否开启用户注册功能','sys.account.registerUser','false','Y','admin','2026-07-13 11:25:53','',NULL,'是否开启注册用户功能（true开启，false关闭）'),(6,'用户登录-黑名单列表','sys.login.blackIPList','','Y','admin','2026-07-13 11:25:53','',NULL,'设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）'),(7,'用户管理-初始密码修改策略','sys.account.initPasswordModify','1','Y','admin','2026-07-13 11:25:53','',NULL,'0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框'),(8,'用户管理-账号密码更新周期','sys.account.passwordValidateDays','0','Y','admin','2026-07-13 11:25:53','',NULL,'密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框'),(9,'用户管理-密码字符范围','sys.account.chrtype','0','Y','admin','2026-07-13 11:25:53','',NULL,'默认任意字符范围，0任意（密码可以输入任意字符），1数字（密码只能为0-9数字），2英文字母（密码只能为a-z和A-Z字母），3字母和数字（密码必须包含字母，数字）,4字母数字和特殊字符（目前支持的特殊字符包括：~!@#$%^&*()-=_+）'),(100,'AI功能总开关','dms.ai.enabled','true','Y','admin','2026-07-17 08:29:53','admin','2026-07-17 23:12:31','AI功能总开关(true/false)'),(101,'AI大模型API地址','dms.ai.api_url','admin','Y','admin','2026-07-17 08:29:53','admin','2026-07-17 23:12:31','大模型API地址'),(102,'AI大模型API Key','dms.ai.api_key','admin123','Y','admin','2026-07-17 08:29:53','admin','2026-07-17 23:12:31','大模型API密钥'),(103,'AI模型名称','dms.ai.model_name','deepseek-chat','Y','admin','2026-07-17 08:29:53','admin','2026-07-17 23:12:31','大模型名称'),(104,'AI智能报修开关','dms.ai.repair_assistant','false','Y','admin','2026-07-17 08:29:53','admin','2026-07-17 23:12:31','智能报修助手开关'),(105,'AI智能派工开关','dms.ai.dispatch_recommend','false','Y','admin','2026-07-17 08:29:53','admin','2026-07-17 23:12:31','智能派工推荐开关'),(106,'AI知识问答开关','dms.ai.knowledge_qa','true','Y','admin','2026-07-17 08:29:53','admin','2026-07-17 23:12:31','维修知识问答开关'),(107,'AI月度调用上限','dms.ai.monthly_limit','1000','Y','admin','2026-07-17 08:29:53','admin','2026-07-17 23:12:31','AI功能月度调用次数上限');
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_data_change_log`
--

DROP TABLE IF EXISTS `sys_data_change_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_data_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `entity_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '实体名称',
  `entity_id` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '实体ID',
  `field_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字段名称',
  `old_value` text COLLATE utf8mb4_general_ci COMMENT '旧值',
  `new_value` text COLLATE utf8mb4_general_ci COMMENT '新值',
  `change_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '变更类型 INSERT/UPDATE/DELETE',
  `operate_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_entity_name_id` (`entity_name`,`entity_id`),
  KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据变更日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_data_change_log`
--

LOCK TABLES `sys_data_change_log` WRITE;
/*!40000 ALTER TABLE `sys_data_change_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_data_change_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dept`
--

DROP TABLE IF EXISTS `sys_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dept` (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门id',
  `ancestors` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '部门名称',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `leader` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dept`
--

LOCK TABLES `sys_dept` WRITE;
/*!40000 ALTER TABLE `sys_dept` DISABLE KEYS */;
INSERT INTO `sys_dept` VALUES (100,0,'0','鼎泰科技有限公司',0,'若依','15888888888','ry@qq.com','0','0','admin','2026-07-13 11:25:52','admin','2026-07-20 11:26:12'),(101,100,'0,100','昆明总公司',1,'若依','15888888888','ry@qq.com','0','0','admin','2026-07-13 11:25:52','admin','2026-07-20 11:25:58'),(102,100,'0,100','长沙分公司',2,'若依','15888888888','ry@qq.com','0','2','admin','2026-07-13 11:25:52','',NULL),(103,101,'0,100,101','研发部门',1,'若依','15888888888','ry@qq.com','0','0','admin','2026-07-13 11:25:52','',NULL),(104,101,'0,100,101','市场部门',2,'若依','15888888888','ry@qq.com','0','0','admin','2026-07-13 11:25:52','',NULL),(105,101,'0,100,101','综合管理部',3,'若依','15888888888','ry@qq.com','0','0','admin','2026-07-13 11:25:52','admin','2026-07-20 11:26:51'),(106,101,'0,100,101','财务部门',4,'若依','15888888888','ry@qq.com','0','0','admin','2026-07-13 11:25:52','',NULL),(107,101,'0,100,101','运维部门',5,'若依','15888888888','ry@qq.com','0','0','admin','2026-07-13 11:25:52','',NULL),(108,102,'0,100,102','市场部门',1,'若依','15888888888','ry@qq.com','0','2','admin','2026-07-13 11:25:52','',NULL),(109,102,'0,100,102','财务部门',2,'若依','15888888888','ry@qq.com','0','2','admin','2026-07-13 11:25:52','',NULL);
/*!40000 ALTER TABLE `sys_dept` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict_data`
--

DROP TABLE IF EXISTS `sys_dict_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_data` (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int DEFAULT '0' COMMENT '字典排序',
  `dict_label` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) COLLATE utf8mb4_general_ci DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`)
) ENGINE=InnoDB AUTO_INCREMENT=461 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict_data`
--

LOCK TABLES `sys_dict_data` WRITE;
/*!40000 ALTER TABLE `sys_dict_data` DISABLE KEYS */;
INSERT INTO `sys_dict_data` VALUES (1,1,'男','0','sys_user_sex','','','Y','0','admin','2026-07-13 11:25:53','',NULL,'性别男'),(2,2,'女','1','sys_user_sex','','','N','0','admin','2026-07-13 11:25:53','',NULL,'性别女'),(3,3,'未知','2','sys_user_sex','','','N','0','admin','2026-07-13 11:25:53','',NULL,'性别未知'),(4,1,'显示','0','sys_show_hide','','primary','Y','0','admin','2026-07-13 11:25:53','',NULL,'显示菜单'),(5,2,'隐藏','1','sys_show_hide','','danger','N','0','admin','2026-07-13 11:25:53','',NULL,'隐藏菜单'),(6,1,'正常','0','sys_normal_disable','','primary','Y','0','admin','2026-07-13 11:25:53','',NULL,'正常状态'),(7,2,'停用','1','sys_normal_disable','','danger','N','0','admin','2026-07-13 11:25:53','',NULL,'停用状态'),(8,1,'正常','0','sys_job_status','','primary','Y','0','admin','2026-07-13 11:25:53','',NULL,'正常状态'),(9,2,'暂停','1','sys_job_status','','danger','N','0','admin','2026-07-13 11:25:53','',NULL,'停用状态'),(10,1,'默认','DEFAULT','sys_job_group','','','Y','0','admin','2026-07-13 11:25:53','',NULL,'默认分组'),(11,2,'系统','SYSTEM','sys_job_group','','','N','0','admin','2026-07-13 11:25:53','',NULL,'系统分组'),(12,1,'是','Y','sys_yes_no','','primary','Y','0','admin','2026-07-13 11:25:53','',NULL,'系统默认是'),(13,2,'否','N','sys_yes_no','','danger','N','0','admin','2026-07-13 11:25:53','',NULL,'系统默认否'),(14,1,'通知','1','sys_notice_type','','warning','Y','0','admin','2026-07-13 11:25:53','',NULL,'通知'),(15,2,'公告','2','sys_notice_type','','success','N','0','admin','2026-07-13 11:25:53','',NULL,'公告'),(16,1,'正常','0','sys_notice_status','','primary','Y','0','admin','2026-07-13 11:25:53','',NULL,'正常状态'),(17,2,'关闭','1','sys_notice_status','','danger','N','0','admin','2026-07-13 11:25:53','',NULL,'关闭状态'),(18,99,'其他','0','sys_oper_type','','info','N','0','admin','2026-07-13 11:25:53','',NULL,'其他操作'),(19,1,'新增','1','sys_oper_type','','info','N','0','admin','2026-07-13 11:25:53','',NULL,'新增操作'),(20,2,'修改','2','sys_oper_type','','info','N','0','admin','2026-07-13 11:25:53','',NULL,'修改操作'),(21,3,'删除','3','sys_oper_type','','danger','N','0','admin','2026-07-13 11:25:53','',NULL,'删除操作'),(22,4,'授权','4','sys_oper_type','','primary','N','0','admin','2026-07-13 11:25:53','',NULL,'授权操作'),(23,5,'导出','5','sys_oper_type','','warning','N','0','admin','2026-07-13 11:25:53','',NULL,'导出操作'),(24,6,'导入','6','sys_oper_type','','warning','N','0','admin','2026-07-13 11:25:53','',NULL,'导入操作'),(25,7,'强退','7','sys_oper_type','','danger','N','0','admin','2026-07-13 11:25:53','',NULL,'强退操作'),(26,8,'生成代码','8','sys_oper_type','','warning','N','0','admin','2026-07-13 11:25:53','',NULL,'生成操作'),(27,9,'清空数据','9','sys_oper_type','','danger','N','0','admin','2026-07-13 11:25:53','',NULL,'清空操作'),(28,1,'成功','0','sys_common_status','','primary','N','0','admin','2026-07-13 11:25:53','',NULL,'正常状态'),(29,2,'失败','1','sys_common_status','','danger','N','0','admin','2026-07-13 11:25:53','',NULL,'停用状态'),(163,1,'原材料','0','wms_material_type','','primary','Y','0','admin','2026-07-13 23:44:02','',NULL,''),(164,2,'半成品','1','wms_material_type','','info','N','0','admin','2026-07-13 23:44:02','',NULL,''),(165,3,'成品','2','wms_material_type','','success','N','0','admin','2026-07-13 23:44:02','',NULL,''),(166,4,'辅料','3','wms_material_type','','warning','N','0','admin','2026-07-13 23:44:02','',NULL,''),(167,1,'个','0','wms_unit','','','Y','0','admin','2026-07-13 23:44:02','',NULL,''),(168,2,'件','1','wms_unit','','','N','0','admin','2026-07-13 23:44:02','',NULL,''),(169,3,'箱','2','wms_unit','','','N','0','admin','2026-07-13 23:44:02','',NULL,''),(170,4,'kg','3','wms_unit','','','N','0','admin','2026-07-13 23:44:02','',NULL,''),(171,5,'吨','4','wms_unit','','','N','0','admin','2026-07-13 23:44:02','',NULL,''),(172,6,'米','5','wms_unit','','','N','0','admin','2026-07-13 23:44:02','',NULL,''),(173,1,'存储区','0','wms_area_type','','primary','Y','0','admin','2026-07-13 23:44:02','',NULL,''),(174,2,'收货区','1','wms_area_type','','success','N','0','admin','2026-07-13 23:44:02','',NULL,''),(175,3,'发货区','2','wms_area_type','','warning','N','0','admin','2026-07-13 23:44:02','',NULL,''),(176,4,'退货区','3','wms_area_type','','danger','N','0','admin','2026-07-13 23:44:02','',NULL,''),(177,5,'暂存区','4','wms_area_type','','info','N','0','admin','2026-07-13 23:44:02','',NULL,''),(178,1,'货架位','0','wms_location_type','','primary','Y','0','admin','2026-07-13 23:44:02','',NULL,''),(179,2,'地堆位','1','wms_location_type','','info','N','0','admin','2026-07-13 23:44:02','',NULL,''),(180,3,'暂存位','2','wms_location_type','','warning','N','0','admin','2026-07-13 23:44:02','',NULL,''),(181,1,'空闲','0','wms_location_status','','success','Y','0','admin','2026-07-13 23:44:02','',NULL,''),(182,2,'占用','1','wms_location_status','','warning','N','0','admin','2026-07-13 23:44:02','',NULL,''),(183,3,'锁定','2','wms_location_status','','danger','N','0','admin','2026-07-13 23:44:02','',NULL,''),(184,1,'采购入库','0','wms_inbound_type','','primary','Y','0','admin','2026-07-13 23:44:02','',NULL,''),(185,2,'生产入库','1','wms_inbound_type','','success','N','0','admin','2026-07-13 23:44:02','',NULL,''),(186,3,'退货入库','2','wms_inbound_type','','warning','N','0','admin','2026-07-13 23:44:02','',NULL,''),(187,4,'调拨入库','3','wms_inbound_type','','info','N','0','admin','2026-07-13 23:44:02','',NULL,''),(188,1,'草稿','0','wms_inbound_status','','info','Y','0','admin','2026-07-13 23:44:02','',NULL,''),(189,2,'待收货','1','wms_inbound_status','','primary','N','0','admin','2026-07-13 23:44:02','',NULL,''),(190,3,'待上架','2','wms_inbound_status','','warning','N','0','admin','2026-07-13 23:44:02','',NULL,''),(191,4,'已完成','3','wms_inbound_status','','success','N','0','admin','2026-07-13 23:44:02','',NULL,''),(192,5,'已作废','4','wms_inbound_status','','danger','N','0','admin','2026-07-13 23:44:02','',NULL,''),(193,1,'销售出库','0','wms_outbound_type','','primary','Y','0','admin','2026-07-13 23:44:02','',NULL,''),(194,2,'领料出库','1','wms_outbound_type','','success','N','0','admin','2026-07-13 23:44:02','',NULL,''),(195,3,'调拨出库','2','wms_outbound_type','','warning','N','0','admin','2026-07-13 23:44:02','',NULL,''),(196,4,'退货出库','3','wms_outbound_type','','danger','N','0','admin','2026-07-13 23:44:02','',NULL,''),(197,1,'草稿','0','wms_outbound_status','','info','Y','0','admin','2026-07-13 23:44:02','',NULL,''),(198,2,'待拣货','1','wms_outbound_status','','primary','N','0','admin','2026-07-13 23:44:02','',NULL,''),(199,3,'复核中','2','wms_outbound_status','','warning','N','0','admin','2026-07-13 23:44:02','',NULL,''),(200,4,'已完成','3','wms_outbound_status','','success','N','0','admin','2026-07-13 23:44:02','',NULL,''),(201,5,'已作废','4','wms_outbound_status','','danger','N','0','admin','2026-07-13 23:44:02','',NULL,''),(202,1,'待拣货','0','wms_wave_status','','info','Y','0','admin','2026-07-13 23:44:02','',NULL,''),(203,2,'拣货中','1','wms_wave_status','','warning','N','0','admin','2026-07-13 23:44:02','',NULL,''),(204,3,'已完成','2','wms_wave_status','','success','N','0','admin','2026-07-13 23:44:02','',NULL,''),(205,4,'已作废','3','wms_wave_status','','danger','N','0','admin','2026-07-13 23:44:02','',NULL,''),(206,1,'入库','0','wms_change_type','','success','Y','0','admin','2026-07-13 23:44:02','',NULL,''),(207,2,'出库','1','wms_change_type','','danger','N','0','admin','2026-07-13 23:44:02','',NULL,''),(208,3,'移库入','2','wms_change_type','','primary','N','0','admin','2026-07-13 23:44:02','',NULL,''),(209,4,'移库出','3','wms_change_type','','warning','N','0','admin','2026-07-13 23:44:02','',NULL,''),(210,5,'盘盈','4','wms_change_type','','success','N','0','admin','2026-07-13 23:44:02','',NULL,''),(211,6,'盘亏','5','wms_change_type','','danger','N','0','admin','2026-07-13 23:44:02','',NULL,''),(212,7,'锁定','6','wms_change_type','','info','N','0','admin','2026-07-13 23:44:02','',NULL,''),(213,8,'解锁','7','wms_change_type','','info','N','0','admin','2026-07-13 23:44:02','',NULL,''),(214,1,'全盘','0','wms_take_type','','primary','Y','0','admin','2026-07-13 23:44:02','',NULL,''),(215,2,'抽盘','1','wms_take_type','','warning','N','0','admin','2026-07-13 23:44:02','',NULL,''),(216,3,'循环盘','2','wms_take_type','','info','N','0','admin','2026-07-13 23:44:02','',NULL,''),(217,1,'待盘点','0','wms_take_status','','info','Y','0','admin','2026-07-13 23:44:02','',NULL,''),(218,2,'盘点中','1','wms_take_status','','primary','N','0','admin','2026-07-13 23:44:02','',NULL,''),(219,3,'待审批','2','wms_take_status','','warning','N','0','admin','2026-07-13 23:44:02','',NULL,''),(220,4,'已完成','3','wms_take_status','','success','N','0','admin','2026-07-13 23:44:02','',NULL,''),(221,5,'已作废','4','wms_take_status','','danger','N','0','admin','2026-07-13 23:44:02','',NULL,''),(222,1,'待审批','0','wms_move_status','','info','Y','0','admin','2026-07-13 23:44:02','',NULL,''),(223,2,'已批准','1','wms_move_status','','primary','N','0','admin','2026-07-13 23:44:02','',NULL,''),(224,3,'已完成','2','wms_move_status','','success','N','0','admin','2026-07-13 23:44:02','',NULL,''),(225,4,'已作废','3','wms_move_status','','danger','N','0','admin','2026-07-13 23:44:02','',NULL,''),(226,1,'在用','0','dms_equipment_status','','success','Y','0','admin','2026-07-15 12:42:45','',NULL,'正常运行'),(227,2,'闲置','1','dms_equipment_status','','info','N','0','admin','2026-07-15 12:42:45','',NULL,'可用但未使用'),(228,3,'维修中','2','dms_equipment_status','','warning','N','0','admin','2026-07-15 12:42:45','',NULL,'正在处理工单'),(229,4,'停机','3','dms_equipment_status','','danger','N','0','admin','2026-07-15 12:42:45','',NULL,'因外部原因停机'),(230,5,'故障','4','dms_equipment_status','','danger','N','0','admin','2026-07-15 12:42:45','',NULL,'等待维修'),(231,6,'报废','5','dms_equipment_status','','info','N','0','admin','2026-07-15 12:42:45','',NULL,'已退出使用'),(232,1,'故障报修','0','dms_order_type','','danger','Y','0','admin','2026-07-15 12:42:45','',NULL,'操作员/主管发起'),(233,2,'PM维护','1','dms_order_type','','primary','N','0','admin','2026-07-15 12:42:45','',NULL,'系统自动生成'),(234,3,'点检整改','2','dms_order_type','','warning','N','0','admin','2026-07-15 12:42:45','',NULL,'点检异常转入'),(235,4,'临时任务','3','dms_order_type','','info','N','0','admin','2026-07-15 12:42:45','',NULL,'管理员指派'),(236,5,'改造安装','4','dms_order_type','','success','N','0','admin','2026-07-15 12:42:45','',NULL,'专项项目'),(237,1,'新建(待派)','0','dms_order_status','','info','Y','0','admin','2026-07-15 12:42:45','',NULL,'新建待派工'),(238,2,'已派工','1','dms_order_status','','primary','N','0','admin','2026-07-15 12:42:45','',NULL,'已派工待接单'),(239,3,'已接单','2','dms_order_status','','primary','N','0','admin','2026-07-15 12:42:45','',NULL,'已接单待处理'),(240,4,'处理中','3','dms_order_status','','warning','N','0','admin','2026-07-15 12:42:45','',NULL,'正在处理'),(241,5,'已完成','4','dms_order_status','','success','N','0','admin','2026-07-15 12:42:45','',NULL,'完工待验收'),(242,6,'已验收','5','dms_order_status','','success','N','0','admin','2026-07-15 12:42:45','',NULL,'验收通过'),(243,7,'已撤销','6','dms_order_status','','info','N','0','admin','2026-07-15 12:42:45','',NULL,'已撤销'),(244,8,'驳回重做','7','dms_order_status','','danger','N','0','admin','2026-07-15 12:42:45','',NULL,'验收驳回'),(245,1,'紧急','0','dms_priority','','danger','N','0','admin','2026-07-15 12:42:45','',NULL,'影响安全/全线停机'),(246,2,'高','1','dms_priority','','warning','N','0','admin','2026-07-15 12:42:45','',NULL,'关键设备故障'),(247,3,'中','2','dms_priority','','primary','Y','0','admin','2026-07-15 12:42:45','',NULL,'一般故障'),(248,4,'低','3','dms_priority','','info','N','0','admin','2026-07-15 12:42:45','',NULL,'计划性/改进类'),(249,1,'周期(时间)','0','dms_pm_trigger_type','','primary','Y','0','admin','2026-07-15 12:42:45','',NULL,'按固定时间间隔'),(250,2,'周期(运行时长)','1','dms_pm_trigger_type','','warning','N','0','admin','2026-07-15 12:42:45','',NULL,'累计运行小时数'),(251,3,'周期(加工量)','2','dms_pm_trigger_type','','info','N','0','admin','2026-07-15 12:42:45','',NULL,'累计加工件数'),(252,1,'状态变更','0','dms_eq_change_type','','warning','N','0','admin','2026-07-15 13:28:28','',NULL,'设备状态变化'),(253,2,'位置变更','1','dms_eq_change_type','','primary','N','0','admin','2026-07-15 13:28:28','',NULL,'安装位置变化'),(254,3,'部门变更','2','dms_eq_change_type','','info','N','0','admin','2026-07-15 13:28:28','',NULL,'使用部门变化'),(255,4,'责任人变更','3','dms_eq_change_type','','info','N','0','admin','2026-07-15 13:28:28','',NULL,'责任人变化'),(256,5,'调拨','4','dms_eq_change_type','','success','N','0','admin','2026-07-15 13:28:28','',NULL,'设备调拨'),(257,1,'入库','0','dms_stock_move_type','','success','N','0','admin','2026-07-15 13:28:28','',NULL,'采购入库'),(258,2,'出库-领用','1','dms_stock_move_type','','warning','N','0','admin','2026-07-15 13:28:28','',NULL,'工单领用出库'),(259,3,'出库-调拨','2','dms_stock_move_type','','info','N','0','admin','2026-07-15 13:28:28','',NULL,'调拨出库'),(260,4,'出库-报废','3','dms_stock_move_type','','danger','N','0','admin','2026-07-15 13:28:28','',NULL,'报废出库'),(261,5,'盘点调整','4','dms_stock_move_type','','primary','N','0','admin','2026-07-15 13:28:28','',NULL,'盘点盈亏调整'),(262,1,'日检','0','dms_inspection_cycle','','danger','Y','0','admin','2026-07-15 13:28:28','',NULL,'每日巡检'),(263,2,'周检','1','dms_inspection_cycle','','warning','N','0','admin','2026-07-15 13:28:28','',NULL,'每周巡检'),(264,3,'月检','2','dms_inspection_cycle','','primary','N','0','admin','2026-07-15 13:28:28','',NULL,'每月巡检'),(265,1,'待执行','0','dms_inspection_status','','info','Y','0','admin','2026-07-15 13:28:28','',NULL,'待执行'),(266,2,'执行中','1','dms_inspection_status','','warning','N','0','admin','2026-07-15 13:28:28','',NULL,'执行中'),(267,3,'已完成','2','dms_inspection_status','','success','N','0','admin','2026-07-15 13:28:28','',NULL,'已完成'),(268,4,'已逾期','3','dms_inspection_status','','danger','N','0','admin','2026-07-15 13:28:28','',NULL,'已逾期'),(269,1,'人工录入','0','dms_collect_mode','','primary','Y','0','admin','2026-07-15 13:28:28','',NULL,'人工录入'),(270,2,'自动采集','1','dms_collect_mode','','success','N','0','admin','2026-07-15 13:28:28','',NULL,'设备自动采集'),(271,1,'运行','0','dms_run_status','','success','Y','0','admin','2026-07-15 13:28:28','',NULL,'正常运行'),(272,2,'停机','1','dms_run_status','','info','N','0','admin','2026-07-15 13:28:28','',NULL,'停机'),(274,1,'状态变更','0','dms_eq_change_type','','warning','N','0','admin','2026-07-15 13:28:44','',NULL,'设备状态变化'),(275,2,'位置变更','1','dms_eq_change_type','','primary','N','0','admin','2026-07-15 13:28:44','',NULL,'安装位置变化'),(276,3,'部门变更','2','dms_eq_change_type','','info','N','0','admin','2026-07-15 13:28:44','',NULL,'使用部门变化'),(277,4,'责任人变更','3','dms_eq_change_type','','info','N','0','admin','2026-07-15 13:28:44','',NULL,'责任人变化'),(278,5,'调拨','4','dms_eq_change_type','','success','N','0','admin','2026-07-15 13:28:44','',NULL,'设备调拨'),(279,1,'入库','0','dms_stock_move_type','','success','N','0','admin','2026-07-15 13:28:44','',NULL,'采购入库'),(280,2,'出库-领用','1','dms_stock_move_type','','warning','N','0','admin','2026-07-15 13:28:44','',NULL,'工单领用出库'),(281,3,'出库-调拨','2','dms_stock_move_type','','info','N','0','admin','2026-07-15 13:28:44','',NULL,'调拨出库'),(282,4,'出库-报废','3','dms_stock_move_type','','danger','N','0','admin','2026-07-15 13:28:44','',NULL,'报废出库'),(283,5,'盘点调整','4','dms_stock_move_type','','primary','N','0','admin','2026-07-15 13:28:44','',NULL,'盘点盈亏调整'),(295,3,'故障','2','dms_run_status','','danger','N','0','admin','2026-07-15 13:28:44','',NULL,'故障'),(296,1,'状态变更','0','dms_eq_change_type','','warning','N','0','admin','2026-07-15 13:36:55','',NULL,'设备状态变化'),(297,2,'位置变更','1','dms_eq_change_type','','primary','N','0','admin','2026-07-15 13:36:55','',NULL,'安装位置变化'),(298,3,'部门变更','2','dms_eq_change_type','','info','N','0','admin','2026-07-15 13:36:55','',NULL,'使用部门变化'),(299,4,'责任人变更','3','dms_eq_change_type','','info','N','0','admin','2026-07-15 13:36:55','',NULL,'责任人变化'),(300,5,'调拨','4','dms_eq_change_type','','success','N','0','admin','2026-07-15 13:36:55','',NULL,'设备调拨'),(301,1,'入库','0','dms_stock_move_type','','success','N','0','admin','2026-07-15 13:36:55','',NULL,'采购入库'),(302,2,'出库-领用','1','dms_stock_move_type','','warning','N','0','admin','2026-07-15 13:36:55','',NULL,'工单领用出库'),(303,3,'出库-调拨','2','dms_stock_move_type','','info','N','0','admin','2026-07-15 13:36:55','',NULL,'调拨出库'),(304,4,'出库-报废','3','dms_stock_move_type','','danger','N','0','admin','2026-07-15 13:36:55','',NULL,'报废出库'),(305,5,'盘点调整','4','dms_stock_move_type','','primary','N','0','admin','2026-07-15 13:36:55','',NULL,'盘点盈亏调整'),(318,1,'轴承','1','dms_part_type','','primary','N','0','admin','2026-07-15 16:33:11','',NULL,''),(319,2,'紧固件','2','dms_part_type','','success','N','0','admin','2026-07-15 16:33:11','',NULL,''),(320,3,'密封件','3','dms_part_type','','info','N','0','admin','2026-07-15 16:33:11','',NULL,''),(321,4,'传动件','4','dms_part_type','','warning','N','0','admin','2026-07-15 16:33:11','',NULL,''),(322,5,'电气元件','5','dms_part_type','','danger','N','0','admin','2026-07-15 16:33:11','',NULL,''),(323,6,'液压气动','6','dms_part_type','','primary','N','0','admin','2026-07-15 16:33:11','',NULL,''),(324,7,'刀具刃具','7','dms_part_type','','success','N','0','admin','2026-07-15 16:33:11','',NULL,''),(325,8,'润滑油品','8','dms_part_type','','info','N','0','admin','2026-07-15 16:33:11','',NULL,''),(326,9,'其他','9','dms_part_type','','info','N','0','admin','2026-07-15 16:33:11','',NULL,''),(327,1,'采购到货','0','dms_partin_type','','primary','Y','0','admin','2026-07-15 17:06:52','',NULL,''),(328,2,'初始化录入','1','dms_partin_type','','success','N','0','admin','2026-07-15 17:06:52','',NULL,''),(329,1,'工单领用','1','dms_partout_type','','primary','Y','0','admin','2026-07-15 17:06:52','',NULL,''),(330,2,'调拨','2','dms_partout_type','','info','N','0','admin','2026-07-15 17:06:52','',NULL,''),(331,3,'报废','3','dms_partout_type','','danger','N','0','admin','2026-07-15 17:06:52','',NULL,''),(332,4,'盘点调整','4','dms_partout_type','','warning','N','0','admin','2026-07-15 17:06:52','',NULL,''),(333,0,'把','6','wms_unit',NULL,'default','N','0','admin','2026-07-16 08:02:53','admin','2026-07-16 08:23:19',NULL),(334,6,'千米','7','wms_unit',NULL,'default','N','0','admin','2026-07-16 08:16:45','admin','2026-07-16 08:23:04',NULL),(335,5,'厘米','8','wms_unit',NULL,'default','N','0','admin','2026-07-16 08:18:03','admin','2026-07-16 08:23:09',NULL),(336,0,'套','9','wms_unit',NULL,'default','N','0','admin','2026-07-16 21:50:21','',NULL,NULL),(337,0,'台','10','wms_unit',NULL,'default','N','0','admin','2026-07-16 21:50:47','',NULL,NULL),(338,0,'条','11','wms_unit',NULL,'default','N','0','admin','2026-07-16 21:51:15','',NULL,NULL),(339,0,'机械配件','10','dms_part_type',NULL,'default','N','0','admin','2026-07-16 21:53:06','',NULL,NULL),(340,1,'关键大客户','1','marketing_customer_level','','danger','N','0','admin','2026-07-19 13:27:35','',NULL,''),(341,2,'重要客户','2','marketing_customer_level','','warning','N','0','admin','2026-07-19 13:27:35','',NULL,''),(342,3,'普通客户','3','marketing_customer_level','','primary','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(343,4,'潜在客户','4','marketing_customer_level','','info','N','0','admin','2026-07-19 13:27:35','',NULL,''),(344,1,'线上广告','0','marketing_customer_source','','primary','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(345,2,'展会','1','marketing_customer_source','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(346,3,'转介绍','2','marketing_customer_source','','warning','N','0','admin','2026-07-19 13:27:35','',NULL,''),(347,4,'官网','3','marketing_customer_source','','info','N','0','admin','2026-07-19 13:27:35','',NULL,''),(348,5,'其他','4','marketing_customer_source','','','N','0','admin','2026-07-19 13:27:35','',NULL,''),(349,1,'互联网/IT','0','marketing_industry','','primary','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(350,2,'制造业','1','marketing_industry','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(351,3,'金融','2','marketing_industry','','warning','N','0','admin','2026-07-19 13:27:35','',NULL,''),(352,4,'教育','3','marketing_industry','','info','N','0','admin','2026-07-19 13:27:35','',NULL,''),(353,5,'医疗','4','marketing_industry','','danger','N','0','admin','2026-07-19 13:27:35','',NULL,''),(354,6,'零售','5','marketing_industry','','','N','0','admin','2026-07-19 13:27:35','',NULL,''),(355,7,'物流','6','marketing_industry','','','N','0','admin','2026-07-19 13:27:35','',NULL,''),(356,8,'其他','7','marketing_industry','','','N','0','admin','2026-07-19 13:27:35','',NULL,''),(357,1,'大型','0','marketing_company_size','','danger','N','0','admin','2026-07-19 13:27:35','',NULL,''),(358,2,'中型','1','marketing_company_size','','warning','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(359,3,'小型','2','marketing_company_size','','info','N','0','admin','2026-07-19 13:27:35','',NULL,''),(360,4,'微型','3','marketing_company_size','','','N','0','admin','2026-07-19 13:27:35','',NULL,''),(364,1,'决策人','0','marketing_contact_role','','danger','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(365,2,'经济购买者','1','marketing_contact_role','','warning','N','0','admin','2026-07-19 13:27:35','',NULL,''),(366,3,'使用者','2','marketing_contact_role','','primary','N','0','admin','2026-07-19 13:27:35','',NULL,''),(367,4,'影响者','3','marketing_contact_role','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(368,5,'守门人','4','marketing_contact_role','','info','N','0','admin','2026-07-19 13:27:35','',NULL,''),(369,1,'电话','0','marketing_interaction_type','','primary','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(370,2,'拜访','1','marketing_interaction_type','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(371,3,'邮件','2','marketing_interaction_type','','info','N','0','admin','2026-07-19 13:27:35','',NULL,''),(372,4,'会议','3','marketing_interaction_type','','warning','N','0','admin','2026-07-19 13:27:35','',NULL,''),(373,5,'微信','4','marketing_interaction_type','','','N','0','admin','2026-07-19 13:27:35','',NULL,''),(374,6,'其他','5','marketing_interaction_type','','','N','0','admin','2026-07-19 13:27:35','',NULL,''),(375,1,'新线索','0','marketing_lead_status','','info','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(376,2,'培育中','1','marketing_lead_status','','primary','N','0','admin','2026-07-19 13:27:35','',NULL,''),(377,3,'MQL','2','marketing_lead_status','','warning','N','0','admin','2026-07-19 13:27:35','',NULL,''),(378,4,'SQL','3','marketing_lead_status','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(379,5,'已转化','4','marketing_lead_status','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(380,6,'已无效','5','marketing_lead_status','','danger','N','0','admin','2026-07-19 13:27:35','',NULL,''),(381,1,'A级(优质)','A','marketing_lead_grade','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(382,2,'B级(良好)','B','marketing_lead_grade','','primary','N','0','admin','2026-07-19 13:27:35','',NULL,''),(383,3,'C级(一般)','C','marketing_lead_grade','','warning','N','0','admin','2026-07-19 13:27:35','',NULL,''),(384,4,'D级(较差)','D','marketing_lead_grade','','info','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(385,1,'电话空号','0','marketing_ineffective_reason','','info','N','0','admin','2026-07-19 13:27:35','',NULL,''),(386,2,'需求不符','1','marketing_ineffective_reason','','warning','N','0','admin','2026-07-19 13:27:35','',NULL,''),(387,3,'无预算','2','marketing_ineffective_reason','','danger','N','0','admin','2026-07-19 13:27:35','',NULL,''),(388,4,'其他','3','marketing_ineffective_reason','','','N','0','admin','2026-07-19 13:27:35','',NULL,''),(389,1,'线索转化','0','marketing_opportunity_source','','primary','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(390,2,'老客户增购','1','marketing_opportunity_source','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(391,3,'主动开发','2','marketing_opportunity_source','','warning','N','0','admin','2026-07-19 13:27:35','',NULL,''),(392,4,'转介绍','3','marketing_opportunity_source','','info','N','0','admin','2026-07-19 13:27:35','',NULL,''),(393,1,'进行中','0','marketing_opportunity_status','','primary','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(394,2,'已赢单','1','marketing_opportunity_status','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(395,3,'已输单','2','marketing_opportunity_status','','danger','N','0','admin','2026-07-19 13:27:35','',NULL,''),(396,1,'价格高','0','marketing_lost_reason','','warning','N','0','admin','2026-07-19 13:27:35','',NULL,''),(397,2,'竞品中标','1','marketing_lost_reason','','danger','N','0','admin','2026-07-19 13:27:35','',NULL,''),(398,3,'需求变更','2','marketing_lost_reason','','info','N','0','admin','2026-07-19 13:27:35','',NULL,''),(399,4,'其他','3','marketing_lost_reason','','','N','0','admin','2026-07-19 13:27:35','',NULL,''),(400,1,'线上研讨会','0','marketing_activity_type','','primary','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(401,2,'展会','1','marketing_activity_type','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(402,3,'行业峰会','2','marketing_activity_type','','warning','N','0','admin','2026-07-19 13:27:35','',NULL,''),(403,4,'客户沙龙','3','marketing_activity_type','','info','N','0','admin','2026-07-19 13:27:35','',NULL,''),(404,5,'产品培训','4','marketing_activity_type','','','N','0','admin','2026-07-19 13:27:35','',NULL,''),(405,6,'广告投放','5','marketing_activity_type','','danger','N','0','admin','2026-07-19 13:27:35','',NULL,''),(406,7,'其他','6','marketing_activity_type','','','N','0','admin','2026-07-19 13:27:35','',NULL,''),(407,1,'草稿','0','marketing_activity_status','','info','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(408,2,'报名中','1','marketing_activity_status','','primary','N','0','admin','2026-07-19 13:27:35','',NULL,''),(409,3,'进行中','2','marketing_activity_status','','warning','N','0','admin','2026-07-19 13:27:35','',NULL,''),(410,4,'已结束','3','marketing_activity_status','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(411,5,'已取消','4','marketing_activity_status','','danger','N','0','admin','2026-07-19 13:27:35','',NULL,''),(412,1,'新签','0','marketing_contract_type','','primary','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(413,2,'续签','1','marketing_contract_type','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(414,3,'增购','2','marketing_contract_type','','warning','N','0','admin','2026-07-19 13:27:35','',NULL,''),(415,4,'升级','3','marketing_contract_type','','info','N','0','admin','2026-07-19 13:27:35','',NULL,''),(416,1,'草稿','0','marketing_contract_status','','info','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(417,2,'审批中','1','marketing_contract_status','','warning','N','0','admin','2026-07-19 13:27:35','',NULL,''),(418,3,'已生效','2','marketing_contract_status','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(419,4,'已过期','3','marketing_contract_status','','info','N','0','admin','2026-07-19 13:27:35','',NULL,''),(420,5,'已终止','4','marketing_contract_status','','danger','N','0','admin','2026-07-19 13:27:35','',NULL,''),(421,1,'待发货','0','marketing_order_status','','info','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(422,2,'已发货','1','marketing_order_status','','primary','N','0','admin','2026-07-19 13:27:35','',NULL,''),(423,3,'已完成','2','marketing_order_status','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(424,4,'已取消','3','marketing_order_status','','danger','N','0','admin','2026-07-19 13:27:35','',NULL,''),(425,1,'待回款','0','marketing_payment_status','','info','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(426,2,'已回款','1','marketing_payment_status','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(427,3,'已逾期','2','marketing_payment_status','','danger','N','0','admin','2026-07-19 13:27:35','',NULL,''),(428,1,'报名','0','marketing_participate_status','','info','Y','0','admin','2026-07-19 13:27:35','',NULL,''),(429,2,'已签到','1','marketing_participate_status','','success','N','0','admin','2026-07-19 13:27:35','',NULL,''),(430,3,'未出席','2','marketing_participate_status','','danger','N','0','admin','2026-07-19 13:27:35','',NULL,''),(437,1,'线上','0','marketing_activity_form','','primary','Y','0','admin','2026-07-19 13:30:00','',NULL,''),(438,2,'线下','1','marketing_activity_form','','success','N','0','admin','2026-07-19 13:30:00','',NULL,''),(439,1,'一次性','0','marketing_payment_method','','primary','Y','0','admin','2026-07-19 13:30:00','',NULL,''),(440,2,'分期','1','marketing_payment_method','','warning','N','0','admin','2026-07-19 13:30:00','',NULL,''),(441,1,'手动','0','marketing_tag_type','','primary','Y','0','admin','2026-07-19 13:30:00','',NULL,''),(442,2,'自动','1','marketing_tag_type','','success','N','0','admin','2026-07-19 13:30:00','',NULL,''),(448,1,'意向客户','0','marketing_customer_status','','primary','Y','0','admin','2026-07-19 14:04:47','',NULL,''),(449,2,'签约客户','1','marketing_customer_status','','success','N','0','admin','2026-07-19 14:04:47','',NULL,''),(450,3,'合作中','2','marketing_customer_status','','warning','N','0','admin','2026-07-19 14:04:47','',NULL,''),(451,4,'暂停合作','3','marketing_customer_status','','info','N','0','admin','2026-07-19 14:04:47','',NULL,''),(452,5,'流失客户','4','marketing_customer_status','','danger','N','0','admin','2026-07-19 14:04:47','',NULL,''),(453,1,'未申请','0','marketing_lead_receive_status','','info','Y','0','admin','2026-07-19 15:49:18','',NULL,''),(454,2,'待审批','1','marketing_lead_receive_status','','warning','N','0','admin','2026-07-19 15:49:18','',NULL,''),(455,3,'已批准','2','marketing_lead_receive_status','','success','N','0','admin','2026-07-19 15:49:18','',NULL,''),(456,4,'已拒绝','3','marketing_lead_receive_status','','danger','N','0','admin','2026-07-19 15:49:18','',NULL,''),(457,1,'不重置','0','mk_number_reset_type','','info','N','0','admin','2026-07-21 20:33:52','',NULL,''),(458,2,'按天','1','mk_number_reset_type','','primary','Y','0','admin','2026-07-21 20:33:52','',NULL,''),(459,3,'按月','2','mk_number_reset_type','','success','N','0','admin','2026-07-21 20:33:52','',NULL,''),(460,4,'按年','3','mk_number_reset_type','','warning','N','0','admin','2026-07-21 20:33:52','',NULL,'');
/*!40000 ALTER TABLE `sys_dict_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict_type`
--

DROP TABLE IF EXISTS `sys_dict_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_type` (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=197 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典类型表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict_type`
--

LOCK TABLES `sys_dict_type` WRITE;
/*!40000 ALTER TABLE `sys_dict_type` DISABLE KEYS */;
INSERT INTO `sys_dict_type` VALUES (1,'用户性别','sys_user_sex','0','admin','2026-07-13 11:25:53','',NULL,'用户性别列表'),(2,'菜单状态','sys_show_hide','0','admin','2026-07-13 11:25:53','',NULL,'菜单状态列表'),(3,'系统开关','sys_normal_disable','0','admin','2026-07-13 11:25:53','',NULL,'系统开关列表'),(4,'任务状态','sys_job_status','0','admin','2026-07-13 11:25:53','',NULL,'任务状态列表'),(5,'任务分组','sys_job_group','0','admin','2026-07-13 11:25:53','',NULL,'任务分组列表'),(6,'系统是否','sys_yes_no','0','admin','2026-07-13 11:25:53','',NULL,'系统是否列表'),(7,'通知类型','sys_notice_type','0','admin','2026-07-13 11:25:53','',NULL,'通知类型列表'),(8,'通知状态','sys_notice_status','0','admin','2026-07-13 11:25:53','',NULL,'通知状态列表'),(9,'操作类型','sys_oper_type','0','admin','2026-07-13 11:25:53','',NULL,'操作类型列表'),(10,'系统状态','sys_common_status','0','admin','2026-07-13 11:25:53','',NULL,'登录状态列表'),(128,'物料类型','wms_material_type','0','admin','2026-07-13 23:44:02','',NULL,'物料类型字典'),(129,'计量单位','wms_unit','0','admin','2026-07-13 23:44:02','',NULL,'计量单位字典'),(130,'库区类型','wms_area_type','0','admin','2026-07-13 23:44:02','',NULL,'库区类型字典'),(131,'库位类型','wms_location_type','0','admin','2026-07-13 23:44:02','',NULL,'库位类型字典'),(132,'库位状态','wms_location_status','0','admin','2026-07-13 23:44:02','',NULL,'库位状态字典'),(133,'入库类型','wms_inbound_type','0','admin','2026-07-13 23:44:02','',NULL,'入库类型字典'),(134,'入库状态','wms_inbound_status','0','admin','2026-07-13 23:44:02','',NULL,'入库状态字典'),(135,'出库类型','wms_outbound_type','0','admin','2026-07-13 23:44:02','',NULL,'出库类型字典'),(136,'出库状态','wms_outbound_status','0','admin','2026-07-13 23:44:02','',NULL,'出库状态字典'),(137,'批次状态','wms_wave_status','0','admin','2026-07-13 23:44:02','',NULL,'波次状态字典'),(138,'库存变动类型','wms_change_type','0','admin','2026-07-13 23:44:02','',NULL,'库存变动类型字典'),(139,'盘点类型','wms_take_type','0','admin','2026-07-13 23:44:02','',NULL,'盘点类型字典'),(140,'盘点状态','wms_take_status','0','admin','2026-07-13 23:44:02','',NULL,'盘点状态字典'),(141,'移库状态','wms_move_status','0','admin','2026-07-13 23:44:02','',NULL,'移库状态字典'),(143,'设备状态','dms_equipment_status','0','admin','2026-07-15 12:42:45','',NULL,'设备台账状态'),(144,'工单类型','dms_order_type','0','admin','2026-07-15 12:42:45','',NULL,'维护工单类型'),(145,'工单状态','dms_order_status','0','admin','2026-07-15 12:42:45','',NULL,'工单流转状态'),(146,'优先级','dms_priority','0','admin','2026-07-15 12:42:45','',NULL,'工单优先级'),(147,'PM触发类型','dms_pm_trigger_type','0','admin','2026-07-15 12:42:45','',NULL,'预防性维护触发类型'),(148,'设备变更类型','dms_eq_change_type','0','admin','2026-07-15 13:28:28','',NULL,'设备状态/位置变更类型'),(149,'备件出入库类型','dms_stock_move_type','0','admin','2026-07-15 13:28:28','',NULL,'备件出入库类型'),(150,'点检周期','dms_inspection_cycle','0','admin','2026-07-15 13:28:28','',NULL,'点检巡检周期类型'),(151,'点检任务状态','dms_inspection_status','0','admin','2026-07-15 13:28:28','',NULL,'点检任务状态'),(152,'采集模式','dms_collect_mode','0','admin','2026-07-15 13:28:28','',NULL,'数据采集模式'),(153,'设备运行状态','dms_run_status','0','admin','2026-07-15 13:28:28','',NULL,'设备运行状态'),(166,'备件类别','dms_part_type','0','admin','2026-07-15 16:32:51','',NULL,'备件类别'),(167,'入库类型','dms_partin_type','0','admin','2026-07-15 17:06:15','',NULL,'备件入库类型'),(168,'出库类型','dms_partout_type','0','admin','2026-07-15 17:06:15','',NULL,'备件出库类型'),(169,'客户等级','marketing_customer_level','0','admin','2026-07-19 13:27:35','',NULL,'客户等级字典'),(170,'客户来源','marketing_customer_source','0','admin','2026-07-19 13:27:35','',NULL,'客户来源字典'),(171,'行业分类','marketing_industry','0','admin','2026-07-19 13:27:35','',NULL,'行业分类字典'),(172,'企业规模','marketing_company_size','0','admin','2026-07-19 13:27:35','',NULL,'企业规模字典'),(173,'客户状态','marketing_customer_status','0','admin','2026-07-19 13:27:35','',NULL,'客户状态字典'),(174,'联系人角色','marketing_contact_role','0','admin','2026-07-19 13:27:35','',NULL,'联系人角色字典'),(175,'互动类型','marketing_interaction_type','0','admin','2026-07-19 13:27:35','',NULL,'互动类型字典'),(176,'线索状态','marketing_lead_status','0','admin','2026-07-19 13:27:35','',NULL,'线索状态字典'),(177,'线索等级','marketing_lead_grade','0','admin','2026-07-19 13:27:35','',NULL,'线索等级字典'),(178,'无效原因','marketing_ineffective_reason','0','admin','2026-07-19 13:27:35','',NULL,'线索无效原因字典'),(179,'商机来源','marketing_opportunity_source','0','admin','2026-07-19 13:27:35','',NULL,'商机来源字典'),(180,'商机状态','marketing_opportunity_status','0','admin','2026-07-19 13:27:35','',NULL,'商机状态字典'),(181,'输单原因','marketing_lost_reason','0','admin','2026-07-19 13:27:35','',NULL,'输单原因字典'),(182,'活动类型','marketing_activity_type','0','admin','2026-07-19 13:27:35','',NULL,'活动类型字典'),(183,'活动状态','marketing_activity_status','0','admin','2026-07-19 13:27:35','',NULL,'活动状态字典'),(184,'合同类型','marketing_contract_type','0','admin','2026-07-19 13:27:35','',NULL,'合同类型字典'),(185,'合同状态','marketing_contract_status','0','admin','2026-07-19 13:27:35','',NULL,'合同状态字典'),(186,'订单状态','marketing_order_status','0','admin','2026-07-19 13:27:35','',NULL,'订单状态字典'),(187,'回款状态','marketing_payment_status','0','admin','2026-07-19 13:27:35','',NULL,'回款状态字典'),(188,'参与状态','marketing_participate_status','0','admin','2026-07-19 13:27:35','',NULL,'活动参与状态字典'),(192,'活动形式','marketing_activity_form','0','admin','2026-07-19 13:30:00','',NULL,'活动形式字典'),(193,'付款方式','marketing_payment_method','0','admin','2026-07-19 13:30:00','',NULL,'付款方式字典'),(194,'标签类型','marketing_tag_type','0','admin','2026-07-19 13:30:00','',NULL,'客户标签类型字典'),(195,'领取审批状态','marketing_lead_receive_status','0','admin','2026-07-19 15:49:18','',NULL,'线索领取审批状态字典'),(196,'编号重置类型','mk_number_reset_type','0','admin','2026-07-21 20:33:52','',NULL,'编号规则序号重置类型');
/*!40000 ALTER TABLE `sys_dict_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job`
--

DROP TABLE IF EXISTS `sys_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job` (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) COLLATE utf8mb4_general_ci DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`,`job_name`,`job_group`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='定时任务调度表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job`
--

LOCK TABLES `sys_job` WRITE;
/*!40000 ALTER TABLE `sys_job` DISABLE KEYS */;
INSERT INTO `sys_job` VALUES (1,'系统默认（无参）','DEFAULT','ryTask.ryNoParams','0/10 * * * * ?','3','1','1','admin','2026-07-13 11:25:53','',NULL,''),(2,'系统默认（有参）','DEFAULT','ryTask.ryParams(\'ry\')','0/15 * * * * ?','3','1','1','admin','2026-07-13 11:25:53','',NULL,''),(3,'系统默认（多参）','DEFAULT','ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)','0/20 * * * * ?','3','1','1','admin','2026-07-13 11:25:53','',NULL,''),(100,'PM计划自动生成工单','DEFAULT','dmsTask.pmAutoGenerate','0 0 1 * * ?','3','1','0','admin','2026-07-16 23:25:39','',NULL,'每天凌晨1点扫描到期PM计划，提前N天自动生成工单'),(101,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','0 */30 * * * ?','3','1','0','admin','2026-07-17 08:29:53','',NULL,'每30分钟检查工单SLA超时并升级'),(102,'点检任务自动生成','DEFAULT','dmsTask.inspectionAutoGenerate','0 0 1 * * ?','3','1','0','admin','2026-07-17 13:55:12','',NULL,'每天凌晨1点根据巡检路线周期自动生成点检任务'),(103,'点检任务逾期标记','DEFAULT','dmsTask.inspectionOverdueCheck','0 0 2 * * ?','3','1','0','admin','2026-07-17 13:55:12','',NULL,'每天凌晨2点将过期未完成的点检任务标记为逾期');
/*!40000 ALTER TABLE `sys_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_log`
--

DROP TABLE IF EXISTS `sys_job_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_log` (
  `job_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '日志信息',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '异常信息',
  `start_time` datetime DEFAULT NULL COMMENT '执行开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '执行结束时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`),
  KEY `idx_sys_job_log_ctime` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='定时任务调度日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_log`
--

LOCK TABLES `sys_job_log` WRITE;
/*!40000 ALTER TABLE `sys_job_log` DISABLE KEYS */;
INSERT INTO `sys_job_log` VALUES (1,'系统默认（无参）','DEFAULT','ryTask.ryNoParams','系统默认（无参） 总共耗时：1毫秒','0','','2026-07-13 12:40:24','2026-07-13 12:40:24','2026-07-13 12:40:23'),(2,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：16毫秒','0','','2026-07-17 09:00:00','2026-07-17 09:00:00','2026-07-17 09:00:00'),(3,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：9毫秒','0','','2026-07-17 09:30:00','2026-07-17 09:30:00','2026-07-17 09:30:00'),(4,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：6毫秒','0','','2026-07-17 10:00:00','2026-07-17 10:00:00','2026-07-17 10:00:00'),(5,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：17毫秒','0','','2026-07-17 11:30:00','2026-07-17 11:30:00','2026-07-17 11:30:00'),(6,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：4毫秒','0','','2026-07-17 12:00:00','2026-07-17 12:00:00','2026-07-17 12:00:00'),(7,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：4毫秒','0','','2026-07-17 12:30:00','2026-07-17 12:30:00','2026-07-17 12:30:00'),(8,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：8毫秒','0','','2026-07-17 13:00:00','2026-07-17 13:00:00','2026-07-17 13:00:00'),(9,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：4毫秒','0','','2026-07-17 13:30:00','2026-07-17 13:30:00','2026-07-17 13:30:00'),(10,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：33毫秒','0','','2026-07-17 14:00:00','2026-07-17 14:00:00','2026-07-17 14:00:00'),(11,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：37毫秒','0','','2026-07-17 14:30:00','2026-07-17 14:30:00','2026-07-17 14:30:00'),(12,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：7毫秒','0','','2026-07-17 15:00:00','2026-07-17 15:00:00','2026-07-17 15:00:00'),(13,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：7毫秒','0','','2026-07-17 15:30:00','2026-07-17 15:30:00','2026-07-17 15:30:00'),(14,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：6毫秒','0','','2026-07-17 16:00:00','2026-07-17 16:00:00','2026-07-17 16:00:00'),(15,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：32毫秒','0','','2026-07-17 16:30:00','2026-07-17 16:30:00','2026-07-17 16:30:00'),(16,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：49毫秒','0','','2026-07-17 17:00:00','2026-07-17 17:00:00','2026-07-17 17:00:00'),(17,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：5毫秒','0','','2026-07-17 17:30:00','2026-07-17 17:30:00','2026-07-17 17:30:00'),(18,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：8毫秒','0','','2026-07-17 18:00:00','2026-07-17 18:00:00','2026-07-17 18:00:00'),(19,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：7毫秒','0','','2026-07-17 18:30:00','2026-07-17 18:30:00','2026-07-17 18:30:00'),(20,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：2毫秒','0','','2026-07-17 19:00:00','2026-07-17 19:00:00','2026-07-17 19:00:00'),(21,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：2毫秒','0','','2026-07-17 19:30:00','2026-07-17 19:30:00','2026-07-17 19:30:00'),(22,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：8毫秒','0','','2026-07-17 20:00:00','2026-07-17 20:00:00','2026-07-17 20:00:00'),(23,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：3毫秒','0','','2026-07-17 20:30:00','2026-07-17 20:30:00','2026-07-17 20:30:00'),(24,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：2毫秒','0','','2026-07-17 21:00:00','2026-07-17 21:00:00','2026-07-17 21:00:00'),(25,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：3毫秒','0','','2026-07-17 21:30:00','2026-07-17 21:30:00','2026-07-17 21:30:00'),(26,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：8毫秒','0','','2026-07-17 22:00:00','2026-07-17 22:00:00','2026-07-17 22:00:00'),(27,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：8毫秒','0','','2026-07-17 22:30:00','2026-07-17 22:30:00','2026-07-17 22:30:00'),(28,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：10毫秒','0','','2026-07-17 23:00:00','2026-07-17 23:00:00','2026-07-17 23:00:00'),(29,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：7毫秒','0','','2026-07-17 23:30:00','2026-07-17 23:30:00','2026-07-17 23:30:00'),(30,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：19毫秒','0','','2026-07-18 09:00:00','2026-07-18 09:00:00','2026-07-18 09:00:00'),(31,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：13毫秒','0','','2026-07-18 09:30:00','2026-07-18 09:30:00','2026-07-18 09:30:00'),(32,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：4毫秒','0','','2026-07-18 10:00:00','2026-07-18 10:00:00','2026-07-18 10:00:00'),(33,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：4毫秒','0','','2026-07-18 23:30:00','2026-07-18 23:30:00','2026-07-18 23:30:00'),(34,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：5毫秒','0','','2026-07-19 00:00:00','2026-07-19 00:00:00','2026-07-19 00:00:00'),(35,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：5毫秒','0','','2026-07-19 14:00:00','2026-07-19 14:00:00','2026-07-19 14:00:00'),(36,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：3毫秒','0','','2026-07-19 14:30:00','2026-07-19 14:30:00','2026-07-19 14:30:00'),(37,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：3毫秒','0','','2026-07-19 15:00:00','2026-07-19 15:00:00','2026-07-19 15:00:00'),(38,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：3毫秒','0','','2026-07-19 15:30:00','2026-07-19 15:30:00','2026-07-19 15:30:00'),(39,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：30毫秒','0','','2026-07-19 16:00:00','2026-07-19 16:00:00','2026-07-19 16:00:00'),(40,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：10毫秒','0','','2026-07-19 17:00:00','2026-07-19 17:00:00','2026-07-19 17:00:00'),(41,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：31毫秒','0','','2026-07-19 17:30:00','2026-07-19 17:30:00','2026-07-19 17:30:00'),(42,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：5毫秒','0','','2026-07-19 18:00:00','2026-07-19 18:00:00','2026-07-19 18:00:00'),(43,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：7毫秒','0','','2026-07-19 20:30:00','2026-07-19 20:30:00','2026-07-19 20:30:00'),(44,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：30毫秒','0','','2026-07-19 21:00:00','2026-07-19 21:00:00','2026-07-19 21:00:00'),(45,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：4毫秒','0','','2026-07-19 21:30:00','2026-07-19 21:30:00','2026-07-19 21:30:00'),(46,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：7毫秒','0','','2026-07-20 09:00:00','2026-07-20 09:00:00','2026-07-20 09:00:00'),(47,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：2毫秒','0','','2026-07-20 09:30:00','2026-07-20 09:30:00','2026-07-20 09:30:00'),(48,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：31毫秒','0','','2026-07-20 10:00:00','2026-07-20 10:00:00','2026-07-20 10:00:00'),(49,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：5毫秒','0','','2026-07-20 10:30:00','2026-07-20 10:30:00','2026-07-20 10:30:00'),(50,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：8毫秒','0','','2026-07-20 11:00:00','2026-07-20 11:00:00','2026-07-20 11:00:00'),(51,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：8毫秒','0','','2026-07-20 11:30:00','2026-07-20 11:30:00','2026-07-20 11:30:00'),(52,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：3毫秒','0','','2026-07-20 12:00:00','2026-07-20 12:00:00','2026-07-20 12:00:00'),(53,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：9毫秒','0','','2026-07-20 12:30:00','2026-07-20 12:30:00','2026-07-20 12:30:00'),(54,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：12毫秒','0','','2026-07-20 13:00:00','2026-07-20 13:00:00','2026-07-20 13:00:00'),(55,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：9毫秒','0','','2026-07-20 13:30:00','2026-07-20 13:30:00','2026-07-20 13:30:00'),(56,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：3毫秒','0','','2026-07-20 14:00:00','2026-07-20 14:00:00','2026-07-20 14:00:00'),(57,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：49毫秒','0','','2026-07-20 14:30:00','2026-07-20 14:30:00','2026-07-20 14:30:00'),(58,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：8毫秒','0','','2026-07-20 15:30:00','2026-07-20 15:30:00','2026-07-20 15:30:00'),(59,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：8毫秒','0','','2026-07-20 16:00:00','2026-07-20 16:00:00','2026-07-20 16:00:00'),(60,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：15毫秒','0','','2026-07-20 18:30:00','2026-07-20 18:30:00','2026-07-20 18:30:00'),(61,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：25毫秒','0','','2026-07-21 20:00:00','2026-07-21 20:00:00','2026-07-21 20:00:00'),(62,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：4毫秒','0','','2026-07-21 20:30:00','2026-07-21 20:30:00','2026-07-21 20:30:00'),(63,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：11毫秒','0','','2026-07-21 21:00:00','2026-07-21 21:00:00','2026-07-21 21:00:00'),(64,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：3毫秒','0','','2026-07-21 21:30:00','2026-07-21 21:30:00','2026-07-21 21:30:00'),(65,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：14毫秒','0','','2026-07-21 22:00:00','2026-07-21 22:00:00','2026-07-21 22:00:00'),(66,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：8毫秒','0','','2026-07-21 22:30:00','2026-07-21 22:30:00','2026-07-21 22:30:00'),(67,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：10毫秒','0','','2026-07-21 23:00:00','2026-07-21 23:00:00','2026-07-21 23:00:00'),(68,'SLA超时检查','DEFAULT','dmsTask.slaTimeoutCheck','SLA超时检查 总共耗时：12毫秒','0','','2026-07-21 23:30:00','2026-07-21 23:30:00','2026-07-21 23:30:00');
/*!40000 ALTER TABLE `sys_job_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_logininfor`
--

DROP TABLE IF EXISTS `sys_logininfor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_logininfor` (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作系统',
  `login_device` varchar(20) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '登录设备类型（PC/手机/平板）',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`),
  KEY `idx_sys_logininfor_s` (`status`),
  KEY `idx_sys_logininfor_lt` (`login_time`),
  KEY `idx_sys_logininfor_uname` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=402 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统访问记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_logininfor`
--

LOCK TABLES `sys_logininfor` WRITE;
/*!40000 ALTER TABLE `sys_logininfor` DISABLE KEYS */;
INSERT INTO `sys_logininfor` VALUES (100,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','','0','登录成功','2026-07-13 11:30:08'),(101,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','','1','验证码已失效','2026-07-13 11:50:42'),(102,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','','0','登录成功','2026-07-13 12:27:04'),(103,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','','1','验证码错误','2026-07-13 12:35:32'),(104,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','','1','验证码错误','2026-07-13 12:36:54'),(105,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','','0','登录成功','2026-07-13 12:37:31'),(106,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','用户不存在/密码错误','2026-07-13 23:09:12'),(107,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','用户不存在/密码错误','2026-07-13 23:09:13'),(108,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','用户不存在/密码错误','2026-07-13 23:09:20'),(109,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','验证码错误','2026-07-13 23:09:27'),(110,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','用户不存在/密码错误','2026-07-13 23:09:31'),(111,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','用户不存在/密码错误','2026-07-13 23:10:15'),(112,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','用户不存在/密码错误','2026-07-13 23:10:31'),(113,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','验证码错误','2026-07-13 23:10:34'),(114,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','用户不存在/密码错误','2026-07-13 23:11:07'),(115,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','验证码错误','2026-07-13 23:11:13'),(116,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','用户不存在/密码错误','2026-07-13 23:11:41'),(117,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-13 23:24:52'),(118,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-13 23:26:37'),(119,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-13 23:29:21'),(120,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','用户不存在/密码错误','2026-07-13 23:33:04'),(121,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-13 23:33:20'),(122,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 07:52:30'),(123,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-14 08:26:34'),(124,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 08:26:37'),(125,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-14 08:36:30'),(126,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 08:36:33'),(127,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-14 08:43:17'),(128,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 08:43:17'),(129,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','1','验证码已失效','2026-07-14 08:45:59'),(130,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','1','验证码已失效','2026-07-14 08:54:58'),(131,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-14 09:09:27'),(132,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 09:09:31'),(133,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 10:08:01'),(134,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-14 10:10:52'),(135,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 10:10:56'),(136,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-14 10:29:05'),(137,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 10:29:10'),(138,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-14 10:34:33'),(139,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 10:34:36'),(140,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-14 10:53:39'),(141,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-14 10:56:05'),(142,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','用户不存在/密码错误','2026-07-14 11:03:33'),(143,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','用户不存在/密码错误','2026-07-14 11:04:47'),(144,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','用户不存在/密码错误','2026-07-14 11:07:42'),(145,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-14 11:15:21'),(146,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-14 11:15:59'),(147,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-14 11:16:22'),(148,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-14 11:17:49'),(149,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 11:18:18'),(150,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-14 11:26:19'),(151,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 11:33:49'),(152,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-14 11:34:13'),(153,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 11:34:21'),(154,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-14 11:55:16'),(155,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 11:55:25'),(156,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-14 11:56:35'),(157,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 11:56:39'),(158,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-14 12:01:26'),(159,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 12:01:27'),(160,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 12:14:51'),(161,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-14 12:14:51'),(162,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-14 12:18:43'),(163,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 12:18:57'),(164,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 13:26:32'),(165,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-14 14:06:12'),(166,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 14:06:14'),(167,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 15:16:07'),(168,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-14 16:02:09'),(169,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 16:02:12'),(170,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 23:07:42'),(171,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-14 23:26:14'),(172,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','1','用户不存在/密码错误','2026-07-14 23:32:58'),(173,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','1','验证码错误','2026-07-14 23:33:00'),(174,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','1','验证码错误','2026-07-14 23:33:08'),(175,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','1','验证码已失效','2026-07-14 23:41:50'),(176,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','0','登录成功','2026-07-14 23:41:56'),(177,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','验证码错误','2026-07-14 23:59:45'),(178,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-14 23:59:59'),(179,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-15 09:23:54'),(180,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-15 09:35:41'),(181,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','退出成功','2026-07-15 09:35:56'),(182,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','验证码已失效','2026-07-15 09:54:05'),(183,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','验证码已失效','2026-07-15 10:01:36'),(184,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','验证码已失效','2026-07-15 10:01:40'),(185,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-15 10:04:14'),(186,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','退出成功','2026-07-15 10:04:19'),(187,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','用户不存在/密码错误','2026-07-15 10:04:26'),(188,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','验证码已失效','2026-07-15 10:04:37'),(189,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','验证码已失效','2026-07-15 10:04:53'),(190,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','验证码已失效','2026-07-15 10:04:58'),(191,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','用户不存在/密码错误','2026-07-15 10:06:22'),(192,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','验证码已失效','2026-07-15 10:06:33'),(193,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-15 10:06:50'),(194,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-15 10:08:50'),(195,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','退出成功','2026-07-15 10:08:53'),(196,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','用户不存在/密码错误','2026-07-15 10:08:57'),(197,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','验证码已失效','2026-07-15 10:09:07'),(198,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-15 10:09:12'),(199,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-15 10:13:15'),(200,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码错误','2026-07-15 10:13:32'),(201,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码错误','2026-07-15 10:19:19'),(202,'notexistuser','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码错误','2026-07-15 10:19:40'),(203,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码错误','2026-07-15 10:20:00'),(204,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-15 10:20:11'),(205,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','退出成功','2026-07-15 10:20:43'),(206,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','验证码错误','2026-07-15 10:20:50'),(207,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','验证码已失效','2026-07-15 10:21:03'),(208,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','用户不存在/密码错误','2026-07-15 10:21:09'),(209,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','验证码已失效','2026-07-15 10:21:14'),(210,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-15 10:21:20'),(211,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','退出成功','2026-07-15 10:21:24'),(212,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-15 10:21:54'),(213,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-15 10:25:08'),(214,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','用户不存在/密码错误','2026-07-15 10:25:53'),(215,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-15 10:26:40'),(216,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','用户不存在/密码错误','2026-07-15 10:28:11'),(217,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-15 10:28:42'),(218,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','退出成功','2026-07-15 10:28:45'),(219,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-15 10:28:49'),(220,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','退出成功','2026-07-15 10:36:31'),(221,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-15 10:36:38'),(222,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','退出成功','2026-07-15 10:36:51'),(223,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','验证码已失效','2026-07-15 10:40:30'),(224,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-15 10:40:33'),(225,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-15 12:45:27'),(226,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-15 12:45:38'),(227,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-15 12:45:48'),(228,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-15 12:46:25'),(229,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','用户不存在/密码错误','2026-07-15 12:46:48'),(230,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','用户不存在/密码错误','2026-07-15 12:47:35'),(231,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-15 12:48:18'),(232,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','用户不存在/密码错误','2026-07-15 12:49:37'),(233,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','用户不存在/密码错误','2026-07-15 12:49:40'),(234,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','用户不存在/密码错误','2026-07-15 12:49:46'),(235,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-15 12:50:02'),(236,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','用户不存在/密码错误','2026-07-15 12:50:11'),(237,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-15 12:51:07'),(238,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-15 12:51:59'),(239,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-15 12:52:12'),(240,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','1','验证码错误','2026-07-15 12:55:56'),(241,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','1','用户不存在/密码错误','2026-07-15 12:56:01'),(242,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','1','验证码已失效','2026-07-15 12:56:06'),(243,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','1','用户不存在/密码错误','2026-07-15 12:56:10'),(244,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','0','登录成功','2026-07-15 12:57:45'),(245,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','0','登录成功','2026-07-15 14:34:25'),(246,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','0','退出成功','2026-07-15 15:25:24'),(247,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','0','登录成功','2026-07-15 15:25:29'),(248,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-15 16:46:12'),(249,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','0','退出成功','2026-07-15 16:54:54'),(250,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','0','登录成功','2026-07-15 16:54:56'),(251,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-15 17:39:14'),(252,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-15 17:44:46'),(253,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-15 17:59:19'),(254,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','0','退出成功','2026-07-15 18:33:26'),(255,'admin','127.0.0.1','内网IP','Chrome 135','Windows10','PC','0','登录成功','2026-07-15 18:33:29'),(256,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-15 18:43:13'),(257,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','用户不存在/密码错误','2026-07-16 07:54:15'),(258,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','验证码已失效','2026-07-16 07:54:27'),(259,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-16 07:54:32'),(260,'admin','127.0.0.1','内网IP','TRAESOLOCN 1.107.1','Windows 10.0','PC','1','用户不存在/密码错误','2026-07-16 08:25:46'),(261,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-16 08:49:17'),(262,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-16 08:49:18'),(263,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-16 09:39:34'),(264,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-16 11:54:28'),(265,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-16 11:55:49'),(266,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-16 11:58:52'),(267,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-16 15:21:21'),(268,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-16 15:33:09'),(269,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-16 15:33:11'),(270,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-16 21:38:50'),(271,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 08:01:50'),(272,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 09:15:21'),(273,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-17 09:24:18'),(274,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 09:24:19'),(275,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-17 09:52:45'),(276,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 09:52:47'),(277,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-17 09:57:29'),(278,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 13:37:34'),(279,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-17 15:29:58'),(280,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 15:30:00'),(281,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 17:41:25'),(282,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 17:49:07'),(283,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 17:52:25'),(284,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','1','验证码已失效','2026-07-17 19:46:12'),(285,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 19:46:34'),(286,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 21:35:02'),(287,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 21:54:07'),(288,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','1','验证码已失效','2026-07-17 22:00:56'),(289,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:01:45'),(290,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:07:00'),(291,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:07:10'),(292,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 22:07:16'),(293,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:10:34'),(294,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:10:50'),(295,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 22:10:54'),(296,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','1','验证码已失效','2026-07-17 22:18:37'),(297,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','1','验证码已失效','2026-07-17 22:18:57'),(298,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','1','验证码已失效','2026-07-17 22:19:08'),(299,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:19:24'),(300,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:19:36'),(301,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 22:20:19'),(302,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','1','验证码已失效','2026-07-17 22:37:51'),(303,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:38:05'),(304,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:38:18'),(305,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:38:33'),(306,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:43:59'),(307,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:44:13'),(308,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 22:45:05'),(309,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','1','验证码已失效','2026-07-17 22:53:58'),(310,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:54:11'),(311,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:54:25'),(312,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:55:43'),(313,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 22:58:44'),(314,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 22:59:46'),(315,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 23:00:00'),(316,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 23:00:13'),(317,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 23:01:50'),(318,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 23:04:13'),(319,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-17 23:11:30'),(320,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 23:11:51'),(321,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','退出成功','2026-07-17 23:22:02'),(322,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 23:22:06'),(323,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-17 23:52:05'),(324,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-17 23:52:17'),(325,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-17 23:52:47'),(326,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-17 23:53:03'),(327,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-17 23:53:22'),(328,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-17 23:53:37'),(329,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-17 23:54:24'),(330,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-18 08:57:32'),(331,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-18 23:24:20'),(332,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','用户不存在/密码错误','2026-07-18 23:28:26'),(333,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','1','验证码已失效','2026-07-18 23:28:34'),(334,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-18 23:28:38'),(335,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-18 23:34:40'),(336,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-19 00:05:27'),(337,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-19 13:44:27'),(338,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-19 15:26:14'),(339,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-19 16:17:38'),(340,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-19 16:19:29'),(341,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-19 16:19:55'),(342,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-19 16:22:33'),(343,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-19 16:23:07'),(344,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-19 16:26:46'),(345,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-19 16:27:20'),(346,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-19 16:32:22'),(347,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-19 16:56:05'),(348,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-19 17:25:41'),(349,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-19 17:28:18'),(350,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-19 20:13:27'),(351,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-20 08:46:50'),(352,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-20 10:07:26'),(353,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','1','验证码已失效','2026-07-20 10:45:56'),(354,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-20 10:46:03'),(355,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-20 12:26:48'),(356,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-20 13:20:58'),(357,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-20 15:27:42'),(358,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-21 19:53:43'),(359,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-21 21:03:57'),(360,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 21:04:57'),(361,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-21 21:13:14'),(362,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 21:26:47'),(363,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 21:27:03'),(364,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-21 21:28:38'),(365,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-21 21:48:38'),(366,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 21:49:54'),(367,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 21:50:25'),(368,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 21:54:45'),(369,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 21:59:35'),(370,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 22:00:38'),(371,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 22:01:40'),(372,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-21 22:02:02'),(373,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 22:40:22'),(374,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 22:41:13'),(375,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 22:41:49'),(376,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 22:44:06'),(377,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-21 22:44:23'),(378,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 22:45:35'),(379,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 22:46:32'),(380,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 22:46:50'),(381,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-21 22:48:19'),(382,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-21 23:21:57'),(383,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-21 23:22:51'),(384,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-21 23:23:32'),(385,'admin','127.0.0.1','内网IP','Curl 8.9.1','','PC','0','登录成功','2026-07-21 23:24:48'),(386,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-21 23:27:03'),(387,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-21 23:33:16'),(388,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 23:34:08'),(389,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-21 23:34:24'),(390,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 23:34:42'),(391,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-21 23:34:48'),(392,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 23:35:15'),(393,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-21 23:35:28'),(394,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 23:53:00'),(395,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','0','登录成功','2026-07-21 23:53:15'),(396,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-21 23:53:17'),(397,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-22 11:37:47'),(398,'admin','127.0.0.1','内网IP','Chrome 150','Windows10','PC','0','登录成功','2026-07-22 11:39:38'),(399,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-22 11:40:38'),(400,'admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.19041.5486','Windows 10.0','PC','1','验证码已失效','2026-07-22 11:41:11'),(401,'admin','127.0.0.1','内网IP','Chrome 132','Windows >=10','PC','0','登录成功','2026-07-22 11:43:34');
/*!40000 ALTER TABLE `sys_logininfor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_menu`
--

DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `path` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '路由名称',
  `is_frame` int DEFAULT '1' COMMENT '是否为外链（0是 1否）',
  `is_cache` int DEFAULT '0' COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4155 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_menu`
--

LOCK TABLES `sys_menu` WRITE;
/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` VALUES (1,'系统管理',0,10,'system',NULL,'','',1,0,'M','0','0','','system','admin','2026-07-13 11:25:52','',NULL,'系统管理目录'),(2,'系统监控',1,15,'monitor',NULL,'','',1,0,'M','0','0','','monitor','admin','2026-07-13 11:25:52','admin','2026-07-18 09:01:17','系统监控目录'),(3,'系统工具',0,3,'tool',NULL,'','',1,0,'M','0','1','','tool','admin','2026-07-13 11:25:52','admin','2026-07-14 09:30:02','系统工具目录'),(4,'若依官网',0,4,'http://ruoyi.vip',NULL,'','',0,0,'M','0','1','','guide','admin','2026-07-13 11:25:52','admin','2026-07-14 09:26:45','若依官网地址'),(100,'用户管理',1,1,'user','system/user/index','','',1,0,'C','0','0','system:user:list','user','admin','2026-07-13 11:25:52','',NULL,'用户管理菜单'),(101,'角色管理',1,2,'role','system/role/index','','',1,0,'C','0','0','system:role:list','peoples','admin','2026-07-13 11:25:52','',NULL,'角色管理菜单'),(102,'菜单管理',1,3,'menu','system/menu/index','','',1,0,'C','0','0','system:menu:list','tree-table','admin','2026-07-13 11:25:52','',NULL,'菜单管理菜单'),(103,'部门管理',1,4,'dept','system/dept/index','','',1,0,'C','0','0','system:dept:list','tree','admin','2026-07-13 11:25:52','',NULL,'部门管理菜单'),(104,'岗位管理',1,5,'post','system/post/index','','',1,0,'C','0','0','system:post:list','post','admin','2026-07-13 11:25:52','',NULL,'岗位管理菜单'),(105,'字典管理',1,6,'dict','system/dict/index','','',1,0,'C','0','0','system:dict:list','dict','admin','2026-07-13 11:25:52','',NULL,'字典管理菜单'),(106,'参数设置',1,7,'config','system/config/index','','',1,0,'C','0','0','system:config:list','edit','admin','2026-07-13 11:25:52','',NULL,'参数设置菜单'),(107,'通知公告',1,8,'notice','system/notice/index','','',1,0,'C','0','0','system:notice:list','message','admin','2026-07-13 11:25:52','',NULL,'通知公告菜单'),(108,'日志管理',1,9,'log','','','',1,0,'M','0','0','','log','admin','2026-07-13 11:25:52','',NULL,'日志管理菜单'),(109,'在线用户',2,1,'online','monitor/online/index','','',1,0,'C','0','0','monitor:online:list','online','admin','2026-07-13 11:25:52','',NULL,'在线用户菜单'),(110,'定时任务',2,2,'job','monitor/job/index','','',1,0,'C','1','0','monitor:job:list','job','admin','2026-07-13 11:25:52','',NULL,'定时任务菜单'),(111,'数据监控',2,3,'druid','monitor/druid/index','','',1,0,'C','1','0','monitor:druid:list','druid','admin','2026-07-13 11:25:52','',NULL,'数据监控菜单'),(112,'服务监控',2,4,'server','monitor/server/index','','',1,0,'C','0','0','monitor:server:list','server','admin','2026-07-13 11:25:52','',NULL,'服务监控菜单'),(113,'缓存监控',2,5,'cache','monitor/cache/index','','',1,0,'C','0','0','monitor:cache:list','redis','admin','2026-07-13 11:25:52','',NULL,'缓存监控菜单'),(114,'缓存列表',2,6,'cacheList','monitor/cache/list','','',1,0,'C','0','0','monitor:cache:list','redis-list','admin','2026-07-13 11:25:52','',NULL,'缓存列表菜单'),(115,'表单构建',3,1,'build','tool/build/index','','',1,0,'C','0','0','tool:build:list','build','admin','2026-07-13 11:25:52','admin','2026-07-14 09:28:47','表单构建菜单'),(116,'代码生成',3,2,'gen','tool/gen/index','','',1,0,'C','0','0','tool:gen:list','code','admin','2026-07-13 11:25:52','admin','2026-07-14 09:28:54','代码生成菜单'),(117,'系统接口',3,3,'swagger','tool/swagger/index','','',1,0,'C','0','0','tool:swagger:list','swagger','admin','2026-07-13 11:25:52','admin','2026-07-14 09:29:01','系统接口菜单'),(118,'接口文档',1,13,'apidoc','tool/apidoc/index','','',1,0,'C','0','0','tool:apidoc:list','documentation','admin','2026-07-18 22:24:25','',NULL,'业务模块接口文档集中管理（免登录可访问 /apidoc）'),(119,'开放接口',1,9,'openapi','system/openapi/index','','',1,0,'C','0','0','system:openapi:list','international','admin','2026-07-18 23:12:10','',NULL,'开放接口应用Key管理'),(120,'开放接口文档',1,14,'openapidoc','tool/openapidoc/index','','',1,0,'C','0','0','tool:openapidoc:list','guide','admin','2026-07-19 00:09:27','',NULL,'开放接口(Open API)文档，供外部系统对接（免登录可访问 /openapi-doc）'),(500,'操作日志',108,1,'operlog','monitor/operlog/index','','',1,0,'C','0','0','monitor:operlog:list','form','admin','2026-07-13 11:25:52','',NULL,'操作日志菜单'),(501,'登录日志',108,2,'logininfor','monitor/logininfor/index','','',1,0,'C','0','0','monitor:logininfor:list','logininfor','admin','2026-07-13 11:25:52','',NULL,'登录日志菜单'),(1000,'用户查询',100,1,'','','','',1,0,'F','0','0','system:user:query','#','admin','2026-07-13 11:25:52','',NULL,''),(1001,'用户新增',100,2,'','','','',1,0,'F','0','0','system:user:add','#','admin','2026-07-13 11:25:52','',NULL,''),(1002,'用户修改',100,3,'','','','',1,0,'F','0','0','system:user:edit','#','admin','2026-07-13 11:25:52','',NULL,''),(1003,'用户删除',100,4,'','','','',1,0,'F','0','0','system:user:remove','#','admin','2026-07-13 11:25:52','',NULL,''),(1004,'用户导出',100,5,'','','','',1,0,'F','0','0','system:user:export','#','admin','2026-07-13 11:25:52','',NULL,''),(1005,'用户导入',100,6,'','','','',1,0,'F','0','0','system:user:import','#','admin','2026-07-13 11:25:53','',NULL,''),(1006,'重置密码',100,7,'','','','',1,0,'F','0','0','system:user:resetPwd','#','admin','2026-07-13 11:25:53','',NULL,''),(1007,'角色查询',101,1,'','','','',1,0,'F','0','0','system:role:query','#','admin','2026-07-13 11:25:53','',NULL,''),(1008,'角色新增',101,2,'','','','',1,0,'F','0','0','system:role:add','#','admin','2026-07-13 11:25:53','',NULL,''),(1009,'角色修改',101,3,'','','','',1,0,'F','0','0','system:role:edit','#','admin','2026-07-13 11:25:53','',NULL,''),(1010,'角色删除',101,4,'','','','',1,0,'F','0','0','system:role:remove','#','admin','2026-07-13 11:25:53','',NULL,''),(1011,'角色导出',101,5,'','','','',1,0,'F','0','0','system:role:export','#','admin','2026-07-13 11:25:53','',NULL,''),(1012,'菜单查询',102,1,'','','','',1,0,'F','0','0','system:menu:query','#','admin','2026-07-13 11:25:53','',NULL,''),(1013,'菜单新增',102,2,'','','','',1,0,'F','0','0','system:menu:add','#','admin','2026-07-13 11:25:53','',NULL,''),(1014,'菜单修改',102,3,'','','','',1,0,'F','0','0','system:menu:edit','#','admin','2026-07-13 11:25:53','',NULL,''),(1015,'菜单删除',102,4,'','','','',1,0,'F','0','0','system:menu:remove','#','admin','2026-07-13 11:25:53','',NULL,''),(1016,'部门查询',103,1,'','','','',1,0,'F','0','0','system:dept:query','#','admin','2026-07-13 11:25:53','',NULL,''),(1017,'部门新增',103,2,'','','','',1,0,'F','0','0','system:dept:add','#','admin','2026-07-13 11:25:53','',NULL,''),(1018,'部门修改',103,3,'','','','',1,0,'F','0','0','system:dept:edit','#','admin','2026-07-13 11:25:53','',NULL,''),(1019,'部门删除',103,4,'','','','',1,0,'F','0','0','system:dept:remove','#','admin','2026-07-13 11:25:53','',NULL,''),(1020,'岗位查询',104,1,'','','','',1,0,'F','0','0','system:post:query','#','admin','2026-07-13 11:25:53','',NULL,''),(1021,'岗位新增',104,2,'','','','',1,0,'F','0','0','system:post:add','#','admin','2026-07-13 11:25:53','',NULL,''),(1022,'岗位修改',104,3,'','','','',1,0,'F','0','0','system:post:edit','#','admin','2026-07-13 11:25:53','',NULL,''),(1023,'岗位删除',104,4,'','','','',1,0,'F','0','0','system:post:remove','#','admin','2026-07-13 11:25:53','',NULL,''),(1024,'岗位导出',104,5,'','','','',1,0,'F','0','0','system:post:export','#','admin','2026-07-13 11:25:53','',NULL,''),(1025,'字典查询',105,1,'#','','','',1,0,'F','0','0','system:dict:query','#','admin','2026-07-13 11:25:53','',NULL,''),(1026,'字典新增',105,2,'#','','','',1,0,'F','0','0','system:dict:add','#','admin','2026-07-13 11:25:53','',NULL,''),(1027,'字典修改',105,3,'#','','','',1,0,'F','0','0','system:dict:edit','#','admin','2026-07-13 11:25:53','',NULL,''),(1028,'字典删除',105,4,'#','','','',1,0,'F','0','0','system:dict:remove','#','admin','2026-07-13 11:25:53','',NULL,''),(1029,'字典导出',105,5,'#','','','',1,0,'F','0','0','system:dict:export','#','admin','2026-07-13 11:25:53','',NULL,''),(1030,'参数查询',106,1,'#','','','',1,0,'F','0','0','system:config:query','#','admin','2026-07-13 11:25:53','',NULL,''),(1031,'参数新增',106,2,'#','','','',1,0,'F','0','0','system:config:add','#','admin','2026-07-13 11:25:53','',NULL,''),(1032,'参数修改',106,3,'#','','','',1,0,'F','0','0','system:config:edit','#','admin','2026-07-13 11:25:53','',NULL,''),(1033,'参数删除',106,4,'#','','','',1,0,'F','0','0','system:config:remove','#','admin','2026-07-13 11:25:53','',NULL,''),(1034,'参数导出',106,5,'#','','','',1,0,'F','0','0','system:config:export','#','admin','2026-07-13 11:25:53','',NULL,''),(1035,'公告查询',107,1,'#','','','',1,0,'F','0','0','system:notice:query','#','admin','2026-07-13 11:25:53','',NULL,''),(1036,'公告新增',107,2,'#','','','',1,0,'F','0','0','system:notice:add','#','admin','2026-07-13 11:25:53','',NULL,''),(1037,'公告修改',107,3,'#','','','',1,0,'F','0','0','system:notice:edit','#','admin','2026-07-13 11:25:53','',NULL,''),(1038,'公告删除',107,4,'#','','','',1,0,'F','0','0','system:notice:remove','#','admin','2026-07-13 11:25:53','',NULL,''),(1039,'操作查询',500,1,'#','','','',1,0,'F','0','0','monitor:operlog:query','#','admin','2026-07-13 11:25:53','',NULL,''),(1040,'操作删除',500,2,'#','','','',1,0,'F','0','0','monitor:operlog:remove','#','admin','2026-07-13 11:25:53','',NULL,''),(1041,'日志导出',500,3,'#','','','',1,0,'F','0','0','monitor:operlog:export','#','admin','2026-07-13 11:25:53','',NULL,''),(1042,'登录查询',501,1,'#','','','',1,0,'F','0','0','monitor:logininfor:query','#','admin','2026-07-13 11:25:53','',NULL,''),(1043,'登录删除',501,2,'#','','','',1,0,'F','0','0','monitor:logininfor:remove','#','admin','2026-07-13 11:25:53','',NULL,''),(1044,'日志导出',501,3,'#','','','',1,0,'F','0','0','monitor:logininfor:export','#','admin','2026-07-13 11:25:53','',NULL,''),(1045,'账户解锁',501,4,'#','','','',1,0,'F','0','0','monitor:logininfor:unlock','#','admin','2026-07-13 11:25:53','',NULL,''),(1046,'在线查询',109,1,'#','','','',1,0,'F','0','0','monitor:online:query','#','admin','2026-07-13 11:25:53','',NULL,''),(1047,'批量强退',109,2,'#','','','',1,0,'F','0','0','monitor:online:batchLogout','#','admin','2026-07-13 11:25:53','',NULL,''),(1048,'单条强退',109,3,'#','','','',1,0,'F','0','0','monitor:online:forceLogout','#','admin','2026-07-13 11:25:53','',NULL,''),(1049,'任务查询',110,1,'#','','','',1,0,'F','1','0','monitor:job:query','#','admin','2026-07-13 11:25:53','',NULL,''),(1050,'任务新增',110,2,'#','','','',1,0,'F','1','0','monitor:job:add','#','admin','2026-07-13 11:25:53','',NULL,''),(1051,'任务修改',110,3,'#','','','',1,0,'F','1','0','monitor:job:edit','#','admin','2026-07-13 11:25:53','',NULL,''),(1052,'任务删除',110,4,'#','','','',1,0,'F','1','0','monitor:job:remove','#','admin','2026-07-13 11:25:53','',NULL,''),(1053,'状态修改',110,5,'#','','','',1,0,'F','1','0','monitor:job:changeStatus','#','admin','2026-07-13 11:25:53','',NULL,''),(1054,'任务导出',110,6,'#','','','',1,0,'F','1','0','monitor:job:export','#','admin','2026-07-13 11:25:53','',NULL,''),(1055,'生成查询',116,1,'#','','','',1,0,'F','1','0','tool:gen:query','#','admin','2026-07-13 11:25:53','',NULL,''),(1056,'生成修改',116,2,'#','','','',1,0,'F','1','0','tool:gen:edit','#','admin','2026-07-13 11:25:53','',NULL,''),(1057,'生成删除',116,3,'#','','','',1,0,'F','1','0','tool:gen:remove','#','admin','2026-07-13 11:25:53','',NULL,''),(1058,'导入代码',116,4,'#','','','',1,0,'F','1','0','tool:gen:import','#','admin','2026-07-13 11:25:53','',NULL,''),(1059,'预览代码',116,5,'#','','','',1,0,'F','1','0','tool:gen:preview','#','admin','2026-07-13 11:25:53','',NULL,''),(1060,'生成代码',116,6,'#','','','',1,0,'F','1','0','tool:gen:code','#','admin','2026-07-13 11:25:53','',NULL,''),(1200,'应用查询',119,1,'#','','','',1,0,'F','0','0','system:openapi:query','#','admin','2026-07-18 23:12:10','',NULL,''),(1201,'应用新增',119,2,'#','','','',1,0,'F','0','0','system:openapi:add','#','admin','2026-07-18 23:12:10','',NULL,''),(1202,'应用修改',119,3,'#','','','',1,0,'F','0','0','system:openapi:edit','#','admin','2026-07-18 23:12:10','',NULL,''),(1203,'应用删除',119,4,'#','','','',1,0,'F','0','0','system:openapi:remove','#','admin','2026-07-18 23:12:10','',NULL,''),(2000,'仓储管理',0,5,'wms',NULL,'','',1,0,'M','0','0','','scan','admin','2026-07-13 23:38:37','admin','2026-07-20 09:08:11','WMS仓储管理目录'),(2001,'基础数据',2000,1,'base',NULL,'','',1,0,'M','0','0','','dict','admin','2026-07-13 23:38:37','',NULL,''),(2002,'入库管理',2000,2,'inbound',NULL,'','',1,0,'M','0','0','','log-in','admin','2026-07-13 23:38:37','',NULL,''),(2003,'出库管理',2000,3,'outbound',NULL,'','',1,0,'M','0','0','','log-out','admin','2026-07-13 23:38:37','',NULL,''),(2004,'库存管理',2000,5,'inv',NULL,'','',1,0,'M','0','0','','storage','admin','2026-07-13 23:38:37','',NULL,''),(2005,'盘点管理',2000,6,'stocktake',NULL,'','',1,0,'M','0','0','','eye-open','admin','2026-07-13 23:38:37','',NULL,''),(2010,'物料管理',2001,1,'material','wms/material/index','','WmsMaterial',1,0,'C','0','0','wms:material:list','component','admin','2026-07-13 23:38:37','',NULL,''),(2011,'物料查询',2010,1,'','','','',1,0,'F','0','0','wms:material:query','#','admin','2026-07-13 23:38:37','',NULL,''),(2012,'物料新增',2010,2,'','','','',1,0,'F','0','0','wms:material:add','#','admin','2026-07-13 23:38:37','',NULL,''),(2013,'物料修改',2010,3,'','','','',1,0,'F','0','0','wms:material:edit','#','admin','2026-07-13 23:38:37','',NULL,''),(2014,'物料删除',2010,4,'','','','',1,0,'F','0','0','wms:material:remove','#','admin','2026-07-13 23:38:37','',NULL,''),(2015,'物料导出',2010,5,'','','','',1,0,'F','0','0','wms:material:export','#','admin','2026-07-13 23:38:37','',NULL,''),(2020,'供应商管理',2001,2,'supplier','wms/supplier/index','','WmsSupplier',1,0,'C','0','0','wms:supplier:list','people','admin','2026-07-13 23:38:37','',NULL,''),(2021,'供应商查询',2020,1,'','','','',1,0,'F','0','0','wms:supplier:query','#','admin','2026-07-13 23:38:37','',NULL,''),(2022,'供应商新增',2020,2,'','','','',1,0,'F','0','0','wms:supplier:add','#','admin','2026-07-13 23:38:37','',NULL,''),(2023,'供应商修改',2020,3,'','','','',1,0,'F','0','0','wms:supplier:edit','#','admin','2026-07-13 23:38:37','',NULL,''),(2024,'供应商删除',2020,4,'','','','',1,0,'F','0','0','wms:supplier:remove','#','admin','2026-07-13 23:38:37','',NULL,''),(2025,'供应商导出',2020,5,'','','','',1,0,'F','0','0','wms:supplier:export','#','admin','2026-07-13 23:38:37','',NULL,''),(2030,'仓库管理',2001,3,'warehouse','wms/warehouse/index','','WmsWarehouse',1,0,'C','0','0','wms:warehouse:list','build','admin','2026-07-13 23:38:37','',NULL,''),(2031,'仓库查询',2030,1,'','','','',1,0,'F','0','0','wms:warehouse:query','#','admin','2026-07-13 23:38:37','',NULL,''),(2032,'仓库新增',2030,2,'','','','',1,0,'F','0','0','wms:warehouse:add','#','admin','2026-07-13 23:38:37','',NULL,''),(2033,'仓库修改',2030,3,'','','','',1,0,'F','0','0','wms:warehouse:edit','#','admin','2026-07-13 23:38:37','',NULL,''),(2034,'仓库删除',2030,4,'','','','',1,0,'F','0','0','wms:warehouse:remove','#','admin','2026-07-13 23:38:37','',NULL,''),(2035,'仓库导出',2030,5,'','','','',1,0,'F','0','0','wms:warehouse:export','#','admin','2026-07-13 23:38:37','',NULL,''),(2036,'仓区查询',2030,6,'','','','',1,0,'F','0','0','wms:area:list','#','admin','2026-07-14 22:27:14','',NULL,''),(2037,'库位查询',2030,7,'','','','',1,0,'F','0','0','wms:location:list','#','admin','2026-07-14 22:27:14','',NULL,''),(2060,'入库单管理',2002,1,'order','wms/inbound/index','','WmsInboundOrder',1,0,'C','0','0','wms:inbound:list','list','admin','2026-07-13 23:38:37','',NULL,''),(2061,'入库作业',2002,2,'detail','wms/inbound/detail','','WmsInboundDetail',1,0,'C','0','0','wms:inbound:detail','edit','admin','2026-07-13 23:38:37','',NULL,''),(2062,'入库单查询',2060,1,'','','','',1,0,'F','0','0','wms:inbound:query','#','admin','2026-07-13 23:38:37','',NULL,''),(2063,'入库单新增',2060,2,'','','','',1,0,'F','0','0','wms:inbound:add','#','admin','2026-07-13 23:38:37','',NULL,''),(2064,'入库单修改',2060,3,'','','','',1,0,'F','0','0','wms:inbound:edit','#','admin','2026-07-13 23:38:37','',NULL,''),(2065,'入库单删除',2060,4,'','','','',1,0,'F','0','0','wms:inbound:remove','#','admin','2026-07-13 23:38:37','',NULL,''),(2066,'入库单导出',2060,5,'','','','',1,0,'F','0','0','wms:inbound:export','#','admin','2026-07-13 23:38:37','',NULL,''),(2070,'出库单管理',2003,1,'order','wms/outbound/index','','WmsOutboundOrder',1,0,'C','0','0','wms:outbound:list','list','admin','2026-07-13 23:38:37','',NULL,''),(2071,'出库作业',2003,2,'detail','wms/outbound/detail','','WmsOutboundDetail',1,0,'C','0','0','wms:outbound:detail','edit','admin','2026-07-13 23:38:37','',NULL,''),(2072,'扫码复核',2003,3,'check','wms/outbound/check','','WmsOutboundCheck',1,0,'C','1','1','wms:outbound:check','scan','admin','2026-07-13 23:38:37','admin','2026-07-14 14:27:44',''),(2073,'批次管理',2003,4,'wave','wms/outbound/wave','','WmsOutboundWave',1,0,'C','1','1','wms:wave:list','group','admin','2026-07-13 23:38:37','admin','2026-07-14 14:27:38',''),(2074,'出库单查询',2070,1,'','','','',1,0,'F','0','0','wms:outbound:query','#','admin','2026-07-13 23:38:37','',NULL,''),(2075,'出库单新增',2070,2,'','','','',1,0,'F','0','0','wms:outbound:add','#','admin','2026-07-13 23:38:37','',NULL,''),(2076,'出库单修改',2070,3,'','','','',1,0,'F','0','0','wms:outbound:edit','#','admin','2026-07-13 23:38:37','',NULL,''),(2077,'出库单删除',2070,4,'','','','',1,0,'F','0','0','wms:outbound:remove','#','admin','2026-07-13 23:38:37','',NULL,''),(2078,'出库单导出',2070,5,'','','','',1,0,'F','0','0','wms:outbound:export','#','admin','2026-07-13 23:38:37','',NULL,''),(2080,'库存查询',2004,1,'list','wms/inventory/index','','WmsInventoryList',1,0,'C','0','0','wms:inventory:list','list','admin','2026-07-13 23:38:37','',NULL,''),(2081,'库存流水',2004,2,'log','wms/log/index','','WmsInventoryLog',1,0,'C','0','0','wms:log:list','time','admin','2026-07-13 23:38:37','',NULL,''),(2082,'库存查询',2080,1,'','','','',1,0,'F','0','0','wms:inventory:query','#','admin','2026-07-13 23:38:37','',NULL,''),(2083,'库存导出',2080,2,'','','','',1,0,'F','0','0','wms:inventory:export','#','admin','2026-07-13 23:38:37','',NULL,''),(2084,'流水查询',2081,1,'','','','',1,0,'F','0','0','wms:log:query','#','admin','2026-07-13 23:38:37','',NULL,''),(2085,'流水导出',2081,2,'','','','',1,0,'F','0','0','wms:log:export','#','admin','2026-07-13 23:38:37','',NULL,''),(2090,'盘点任务',2005,1,'task','wms/stocktake/index','','WmsStocktakeTask',1,0,'C','0','0','wms:stocktake:list','list','admin','2026-07-13 23:38:37','',NULL,''),(2091,'盘点作业',2005,2,'detail','wms/stocktake/detail','','WmsStocktakeDetail',1,0,'C','0','0','wms:stocktake:detail','edit','admin','2026-07-13 23:38:37','',NULL,''),(2092,'盘点查询',2090,1,'','','','',1,0,'F','0','0','wms:stocktake:query','#','admin','2026-07-13 23:38:37','',NULL,''),(2093,'盘点新增',2090,2,'','','','',1,0,'F','0','0','wms:stocktake:add','#','admin','2026-07-13 23:38:37','',NULL,''),(2094,'盘点修改',2090,3,'','','','',1,0,'F','0','0','wms:stocktake:edit','#','admin','2026-07-13 23:38:37','',NULL,''),(2095,'盘点删除',2090,4,'','','','',1,0,'F','0','0','wms:stocktake:remove','#','admin','2026-07-13 23:38:37','',NULL,''),(2100,'移库管理',2000,4,'move','wms/move/index','','WmsMove',1,0,'C','0','0','wms:move:list','swap','admin','2026-07-13 23:38:37','',NULL,''),(2101,'移库查询',2100,1,'','','','',1,0,'F','0','0','wms:move:query','#','admin','2026-07-13 23:38:37','',NULL,''),(2102,'移库新增',2100,2,'','','','',1,0,'F','0','0','wms:move:add','#','admin','2026-07-13 23:38:37','',NULL,''),(2103,'移库修改',2100,3,'','','','',1,0,'F','0','0','wms:move:edit','#','admin','2026-07-13 23:38:37','',NULL,''),(2104,'移库删除',2100,4,'','','','',1,0,'F','0','0','wms:move:remove','#','admin','2026-07-13 23:38:37','',NULL,''),(2105,'移库导出',2100,5,'','','','',1,0,'F','0','0','wms:move:export','#','admin','2026-07-14 22:27:14','',NULL,''),(2110,'统计报表',2000,7,'statistics','wms/statistics/index','','WmsStatistics',1,0,'C','0','0','wms:statistics:list','chart','admin','2026-07-14 23:28:01','',NULL,'仓库管理统计报表'),(2111,'统计查询',2110,1,'','','','',1,0,'F','0','0','wms:statistics:query','#','admin','2026-07-14 23:28:01','',NULL,''),(2120,'库存预警',2004,3,'alert','wms/stockAlert/index','','WmsStockAlert',1,0,'C','0','0','wms:stockAlert:list','bug','admin','2026-07-14 23:52:01','',NULL,'库存预警管理（库存不足/积压/临期/过期）'),(2121,'预警查询',2120,1,'','','','',1,0,'F','0','0','wms:stockAlert:query','#','admin','2026-07-14 23:52:01','',NULL,''),(2122,'预警导出',2120,2,'','','','',1,0,'F','0','0','wms:stockAlert:export','#','admin','2026-07-14 23:52:01','',NULL,''),(2123,'库存删除',2080,3,'','','','',1,0,'F','0','0','wms:inventory:remove','#','admin','2026-07-16 11:21:32','',NULL,''),(2124,'流水删除',2081,3,'','','','',1,0,'F','0','0','wms:log:remove','#','admin','2026-07-16 11:21:33','',NULL,''),(2125,'预警删除',2120,3,'','','','',1,0,'F','0','0','wms:stockAlert:remove','#','admin','2026-07-16 11:21:33','',NULL,''),(3000,'设备管理',0,6,'dms',NULL,'','',1,0,'M','0','0','','server','admin','2026-07-15 12:42:45','admin','2026-07-20 09:06:54','DMS设备管理目录'),(3001,'基础数据',3000,1,'base',NULL,'','',1,0,'M','0','0','','dict','admin','2026-07-15 12:42:45','',NULL,'设备基础数据'),(3002,'工单管理',3000,2,'workorder',NULL,'','',1,0,'M','0','0','','edit','admin','2026-07-15 12:42:45','',NULL,''),(3003,'维护计划',3000,3,'pm',NULL,'','',1,0,'M','0','0','','date','admin','2026-07-15 12:42:45','',NULL,''),(3004,'点检巡检',3000,5,'inspection',NULL,'','',1,0,'M','0','0','','eye-open','admin','2026-07-15 13:28:28','',NULL,'点检巡检管理'),(3005,'数据采集',3000,6,'data',NULL,'','',1,0,'M','0','0','','monitor','admin','2026-07-15 13:28:28','',NULL,'数据采集与监控'),(3006,'报表看板',3000,7,'dashboard',NULL,'','',1,0,'M','0','0','','dashboard','admin','2026-07-15 13:28:28','',NULL,'报表与看板'),(3007,'AI助手',3000,8,'ai',NULL,'','',1,0,'M','0','0','','tool','admin','2026-07-15 13:28:28','',NULL,'AI智能功能'),(3010,'设备分类',3001,1,'category','dms/category/index','','DmsCategory',1,0,'C','0','0','dms:category:list','tree','admin','2026-07-15 12:42:45','',NULL,''),(3011,'分类查询',3010,1,'','','','',1,0,'F','0','0','dms:category:query','#','admin','2026-07-15 12:42:45','',NULL,''),(3012,'分类新增',3010,2,'','','','',1,0,'F','0','0','dms:category:add','#','admin','2026-07-15 12:42:45','',NULL,''),(3013,'分类修改',3010,3,'','','','',1,0,'F','0','0','dms:category:edit','#','admin','2026-07-15 12:42:45','',NULL,''),(3014,'分类删除',3010,4,'','','','',1,0,'F','0','0','dms:category:remove','#','admin','2026-07-15 12:42:45','',NULL,''),(3020,'设备台账',3001,2,'equipment','dms/equipment/index','','DmsEquipment',1,0,'C','0','0','dms:equipment:list','component','admin','2026-07-15 12:42:45','',NULL,''),(3021,'设备查询',3020,1,'','','','',1,0,'F','0','0','dms:equipment:query','#','admin','2026-07-15 12:42:45','',NULL,''),(3022,'设备新增',3020,2,'','','','',1,0,'F','0','0','dms:equipment:add','#','admin','2026-07-15 12:42:45','',NULL,''),(3023,'设备修改',3020,3,'','','','',1,0,'F','0','0','dms:equipment:edit','#','admin','2026-07-15 12:42:45','',NULL,''),(3024,'设备删除',3020,4,'','','','',1,0,'F','0','0','dms:equipment:remove','#','admin','2026-07-15 12:42:45','',NULL,''),(3025,'设备导出',3020,5,'','','','',1,0,'F','0','0','dms:equipment:export','#','admin','2026-07-15 12:42:45','',NULL,''),(3030,'备件管理',3000,4,'sparepart',NULL,'','',1,0,'M','0','0','dms:sparepart:list','shopping','admin','2026-07-15 12:42:45','',NULL,''),(3032,'备件新增',3105,2,'','','','',1,0,'F','0','0','dms:sparepart:add','#','admin','2026-07-15 12:42:45','',NULL,''),(3033,'备件修改',3105,3,'','','','',1,0,'F','0','0','dms:sparepart:edit','#','admin','2026-07-15 12:42:45','',NULL,''),(3034,'备件删除',3105,4,'','','','',1,0,'F','0','0','dms:sparepart:remove','#','admin','2026-07-15 12:42:45','',NULL,''),(3035,'备件导出',3105,5,'','','','',1,0,'F','0','0','dms:sparepart:export','#','admin','2026-07-15 12:42:45','',NULL,''),(3040,'工单列表',3002,1,'list','dms/workorder/index','','DmsWorkOrder',1,0,'C','0','0','dms:workorder:list','list','admin','2026-07-15 12:42:45','',NULL,''),(3041,'工单查询',3040,1,'','','','',1,0,'F','0','0','dms:workorder:query','#','admin','2026-07-15 12:42:45','',NULL,''),(3042,'工单新增',3040,2,'','','','',1,0,'F','0','0','dms:workorder:add','#','admin','2026-07-15 12:42:45','',NULL,''),(3043,'工单修改',3040,3,'','','','',1,0,'F','0','0','dms:workorder:edit','#','admin','2026-07-15 12:42:45','',NULL,''),(3044,'工单删除',3040,4,'','','','',1,0,'F','0','0','dms:workorder:remove','#','admin','2026-07-15 12:42:45','',NULL,''),(3045,'工单导出',3040,5,'','','','',1,0,'F','0','0','dms:workorder:export','#','admin','2026-07-15 12:42:45','',NULL,''),(3050,'PM计划',3003,1,'plan','dms/pmplan/index','','DmsPmPlan',1,0,'C','0','0','dms:pmplan:list','plan','admin','2026-07-15 12:42:45','',NULL,''),(3051,'计划查询',3050,1,'','','','',1,0,'F','0','0','dms:pmplan:query','#','admin','2026-07-15 12:42:45','',NULL,''),(3052,'计划新增',3050,2,'','','','',1,0,'F','0','0','dms:pmplan:add','#','admin','2026-07-15 12:42:45','',NULL,''),(3053,'计划修改',3050,3,'','','','',1,0,'F','0','0','dms:pmplan:edit','#','admin','2026-07-15 12:42:45','',NULL,''),(3054,'计划删除',3050,4,'','','','',1,0,'F','0','0','dms:pmplan:remove','#','admin','2026-07-15 12:42:45','',NULL,''),(3060,'巡检路线',3004,1,'route','dms/inspection/route/index','','DmsInspectionRoute',1,0,'C','0','0','dms:inspection:route:list','tree-table','admin','2026-07-15 13:28:28','',NULL,''),(3061,'路线查询',3060,1,'','','','',1,0,'F','0','0','dms:inspection:route:query','#','admin','2026-07-15 13:28:28','',NULL,''),(3062,'路线新增',3060,2,'','','','',1,0,'F','0','0','dms:inspection:route:add','#','admin','2026-07-15 13:28:28','',NULL,''),(3063,'路线修改',3060,3,'','','','',1,0,'F','0','0','dms:inspection:route:edit','#','admin','2026-07-15 13:28:28','',NULL,''),(3064,'路线删除',3060,4,'','','','',1,0,'F','0','0','dms:inspection:route:remove','#','admin','2026-07-15 13:28:28','',NULL,''),(3070,'点检任务',3004,2,'task','dms/inspection/task/index','','DmsInspectionTask',1,0,'C','0','0','dms:inspection:task:list','form','admin','2026-07-15 13:28:28','',NULL,''),(3071,'任务查询',3070,1,'','','','',1,0,'F','0','0','dms:inspection:task:query','#','admin','2026-07-15 13:28:28','',NULL,''),(3072,'任务新增',3070,2,'','','','',1,0,'F','0','0','dms:inspection:task:add','#','admin','2026-07-15 13:28:28','',NULL,''),(3073,'任务修改',3070,3,'','','','',1,0,'F','0','0','dms:inspection:task:edit','#','admin','2026-07-15 13:28:28','',NULL,''),(3074,'任务删除',3070,4,'','','','',1,0,'F','0','0','dms:inspection:task:remove','#','admin','2026-07-15 13:28:28','',NULL,''),(3080,'采集配置',3005,1,'config','dms/data/config/index','','DmsDataConfig',1,0,'C','0','0','dms:data:config:list','edit','admin','2026-07-15 13:28:28','',NULL,''),(3081,'配置查询',3080,1,'','','','',1,0,'F','0','0','dms:data:config:query','#','admin','2026-07-15 13:28:28','',NULL,''),(3082,'配置新增',3080,2,'','','','',1,0,'F','0','0','dms:data:config:add','#','admin','2026-07-15 13:28:28','',NULL,''),(3083,'配置修改',3080,3,'','','','',1,0,'F','0','0','dms:data:config:edit','#','admin','2026-07-15 13:28:28','',NULL,''),(3084,'配置删除',3080,4,'','','','',1,0,'F','0','0','dms:data:config:remove','#','admin','2026-07-15 13:28:28','',NULL,''),(3085,'数据记录',3005,2,'record','dms/data/record/index','','DmsDataRecord',1,0,'C','0','0','dms:data:record:list','log','admin','2026-07-15 13:28:28','',NULL,''),(3086,'记录查询',3085,1,'','','','',1,0,'F','0','0','dms:data:record:query','#','admin','2026-07-15 13:28:28','',NULL,''),(3087,'记录新增',3085,2,'','','','',1,0,'F','0','0','dms:data:record:add','#','admin','2026-07-15 13:28:28','',NULL,''),(3088,'记录删除',3085,3,'','','','',1,0,'F','0','0','dms:data:record:remove','#','admin','2026-07-15 13:28:28','',NULL,''),(3090,'数据看板',3006,1,'overview','dms/dashboard/index','','DmsDashboard',1,0,'C','0','0','dms:dashboard:list','chart','admin','2026-07-15 13:28:28','',NULL,'KPI看板'),(3100,'维修知识库',3007,1,'faq','dms/ai/faq/index','','DmsRepairFaq',1,0,'C','0','0','dms:ai:faq:list','documentation','admin','2026-07-15 13:28:28','',NULL,'维修FAQ管理'),(3101,'FAQ查询',3100,1,'','','','',1,0,'F','0','0','dms:ai:faq:query','#','admin','2026-07-15 13:28:28','',NULL,''),(3102,'FAQ新增',3100,2,'','','','',1,0,'F','0','0','dms:ai:faq:add','#','admin','2026-07-15 13:28:28','',NULL,''),(3103,'FAQ修改',3100,3,'','','','',1,0,'F','0','0','dms:ai:faq:edit','#','admin','2026-07-15 13:28:28','',NULL,''),(3104,'FAQ删除',3100,4,'','','','',1,0,'F','0','0','dms:ai:faq:remove','#','admin','2026-07-15 13:28:28','',NULL,''),(3105,'备件主数据',3001,3,'sparepart','dms/sparepart/index','','DmsSparePart',1,0,'C','0','0','dms:sparepart:list','shopping','admin','2026-07-15 17:04:25','',NULL,''),(3106,'备件入库',3030,1,'partin','dms/partin/index','','DmsPartIn',1,0,'C','0','0','dms:partin:list','log-in','admin','2026-07-15 17:04:25','admin','2026-07-15 18:45:16',''),(3107,'备件出库',3030,2,'partout','dms/partout/index','','DmsPartOut',1,0,'C','0','0','dms:partout:list','log-out','admin','2026-07-15 17:04:25','admin','2026-07-15 18:45:29',''),(3108,'库存流水报表',3006,2,'partflow','dms/partstock/index','','DmsPartFlow',1,0,'C','0','0','dms:partstock:list','chart','admin','2026-07-15 17:04:25','',NULL,''),(3109,'入库查询',3106,1,'','','','',1,0,'F','0','0','dms:partin:query','#','admin','2026-07-15 17:27:27','',NULL,''),(3110,'入库新增',3106,2,'','','','',1,0,'F','0','0','dms:partin:add','#','admin','2026-07-15 17:27:27','',NULL,''),(3111,'入库导出',3106,3,'','','','',1,0,'F','0','0','dms:partin:export','#','admin','2026-07-15 17:27:27','',NULL,''),(3112,'出库查询',3107,1,'','','','',1,0,'F','0','0','dms:partout:query','#','admin','2026-07-15 17:27:27','',NULL,''),(3113,'出库新增',3107,2,'','','','',1,0,'F','0','0','dms:partout:add','#','admin','2026-07-15 17:27:27','',NULL,''),(3114,'出库导出',3107,3,'','','','',1,0,'F','0','0','dms:partout:export','#','admin','2026-07-15 17:27:27','',NULL,''),(3115,'库存查询-query',3108,1,'','','','',1,0,'F','0','0','dms:partstock:query','#','admin','2026-07-15 17:27:27','',NULL,''),(3116,'库存导出',3108,2,'','','','',1,0,'F','0','0','dms:partstock:export','#','admin','2026-07-15 17:27:27','',NULL,''),(3121,'备件库存台账',3030,3,'partledger','dms/partledger/index','','DmsPartLedger',1,0,'C','0','0','dms:partledger:list','storage','admin','2026-07-15 18:19:42','',NULL,''),(3122,'台账查询',3121,1,'','','','',1,0,'F','0','0','dms:partledger:query','#','admin','2026-07-15 18:19:42','',NULL,''),(3123,'台账导出',3121,2,'','','','',1,0,'F','0','0','dms:partledger:export','#','admin','2026-07-15 18:19:42','',NULL,''),(3124,'记录删除',3108,99,'','','','',1,0,'F','0','0','dms:partstock:remove','#','admin','2026-07-16 11:22:02','',NULL,'备件出入库记录删除权限（入库/出库/流水共用）'),(3125,'预警删除',3128,3,'','','','',1,0,'F','0','0','dms:partalert:remove','#','admin','2026-07-16 11:29:42','',NULL,'备件预警删除权限'),(3126,'AI配置',3007,2,'config','dms/ai/config','','DmsAiConfig',1,0,'C','0','0','dms:ai:config','setting','admin','2026-07-17 09:41:59','',NULL,'AI大模型配置管理'),(3127,'记录修改',3085,4,'',NULL,NULL,'',1,0,'F','0','0','dms:data:record:edit','#','admin','2026-07-17 19:51:15','',NULL,''),(3128,'备件预警',3030,5,'partalert','dms/partalert/index',NULL,'',1,0,'C','0','0','dms:partalert:list','warning','admin','2026-07-17 19:51:15','',NULL,''),(3129,'预警查询',3128,1,'',NULL,NULL,'',1,0,'F','0','0','dms:partalert:query','#','admin','2026-07-17 19:51:15','',NULL,''),(3130,'预警导出',3128,2,'',NULL,NULL,'',1,0,'F','0','0','dms:partalert:export','#','admin','2026-07-17 19:51:15','',NULL,''),(3131,'备件查询',3105,1,'','','','',1,0,'F','0','0','dms:sparepart:query','#','admin','2026-07-23 16:10:30','',NULL,'备件主数据查询权限'),(4000,'营销管理',0,7,'mk',NULL,'','',1,0,'M','0','0','','money','admin','2026-07-19 13:27:35','admin','2026-07-19 16:03:42','营销管理目录'),(4001,'客户管理',4000,1,'customer',NULL,'','',1,0,'M','0','0','','peoples','admin','2026-07-19 13:27:35','',NULL,''),(4002,'线索管理',4000,2,'lead',NULL,'','',1,0,'M','0','0','','guide','admin','2026-07-19 13:27:35','',NULL,''),(4003,'商机管理',4000,3,'opportunity',NULL,'','',1,0,'M','0','0','','star','admin','2026-07-19 13:27:35','',NULL,''),(4004,'营销活动',4000,0,'activity',NULL,'','',1,0,'M','0','0','','message','admin','2026-07-19 13:27:35','',NULL,''),(4005,'合同订单',4000,5,'contract',NULL,'','',1,0,'M','0','0','','documentation','admin','2026-07-19 13:27:35','',NULL,''),(4006,'数据分析',4000,6,'report',NULL,'','',1,0,'M','0','0','','chart','admin','2026-07-19 13:27:35','',NULL,''),(4010,'企业客户',4001,1,'list','mk/customer/index','','MkCustomer',1,0,'C','0','0','marketing:customer:list','peoples','admin','2026-07-19 13:27:35','',NULL,''),(4011,'客户查询',4010,1,'','','','',1,0,'F','0','0','marketing:customer:query','#','admin','2026-07-19 13:27:35','',NULL,''),(4012,'客户新增',4010,2,'','','','',1,0,'F','0','0','marketing:customer:add','#','admin','2026-07-19 13:27:35','',NULL,''),(4013,'客户修改',4010,3,'','','','',1,0,'F','0','0','marketing:customer:edit','#','admin','2026-07-19 13:27:35','',NULL,''),(4014,'客户删除',4010,4,'','','','',1,0,'F','0','0','marketing:customer:remove','#','admin','2026-07-19 13:27:35','',NULL,''),(4015,'客户导出',4010,5,'','','','',1,0,'F','0','0','marketing:customer:export','#','admin','2026-07-19 13:27:35','',NULL,''),(4020,'联系人管理',4001,2,'contact','mk/contact/index','','MkContact',1,0,'C','0','0','marketing:contact:list','user','admin','2026-07-19 13:27:35','',NULL,''),(4021,'联系人查询',4020,1,'','','','',1,0,'F','0','0','marketing:contact:query','#','admin','2026-07-19 13:27:35','',NULL,''),(4022,'联系人新增',4020,2,'','','','',1,0,'F','0','0','marketing:contact:add','#','admin','2026-07-19 13:27:35','',NULL,''),(4023,'联系人修改',4020,3,'','','','',1,0,'F','0','0','marketing:contact:edit','#','admin','2026-07-19 13:27:35','',NULL,''),(4024,'联系人删除',4020,4,'','','','',1,0,'F','0','0','marketing:contact:remove','#','admin','2026-07-19 13:27:35','',NULL,''),(4025,'联系人导出',4020,5,'','','','',1,0,'F','0','0','marketing:contact:export','#','admin','2026-07-19 13:27:35','',NULL,''),(4030,'互动记录',4001,3,'interaction','mk/interaction/index','','MkInteraction',1,0,'C','0','0','marketing:interaction:list','phone','admin','2026-07-19 13:27:35','',NULL,''),(4031,'记录查询',4030,1,'','','','',1,0,'F','0','0','marketing:interaction:query','#','admin','2026-07-19 13:27:35','',NULL,''),(4032,'记录新增',4030,2,'','','','',1,0,'F','0','0','marketing:interaction:add','#','admin','2026-07-19 13:27:35','',NULL,''),(4033,'记录修改',4030,3,'','','','',1,0,'F','0','0','marketing:interaction:edit','#','admin','2026-07-19 13:27:35','',NULL,''),(4034,'记录删除',4030,4,'','','','',1,0,'F','0','0','marketing:interaction:remove','#','admin','2026-07-19 13:27:35','',NULL,''),(4036,'客户标签',4001,4,'tag','mk/tag/index','','MkCustomerTag',1,0,'C','0','0','marketing:tag:list','tag','admin','2026-07-19 13:30:00','',NULL,'客户标签管理'),(4037,'标签查询',4036,1,'','','','',1,0,'F','0','0','marketing:tag:query','#','admin','2026-07-19 13:30:00','',NULL,''),(4038,'标签新增',4036,2,'','','','',1,0,'F','0','0','marketing:tag:add','#','admin','2026-07-19 13:30:00','',NULL,''),(4039,'标签修改',4036,3,'','','','',1,0,'F','0','0','marketing:tag:edit','#','admin','2026-07-19 13:30:00','',NULL,''),(4040,'线索列表',4002,1,'list','mk/lead/index','','MkLead',1,0,'C','0','0','marketing:lead:list','list','admin','2026-07-19 16:08:56','',NULL,''),(4041,'线索查询',4040,1,'','','','',1,0,'F','0','0','marketing:lead:query','#','admin','2026-07-19 16:09:22','',NULL,''),(4042,'线索新增',4040,2,'','','','',1,0,'F','0','0','marketing:lead:add','#','admin','2026-07-19 13:27:35','',NULL,''),(4043,'线索修改',4040,3,'','','','',1,0,'F','0','0','marketing:lead:edit','#','admin','2026-07-19 13:27:35','',NULL,''),(4044,'线索删除',4040,4,'','','','',1,0,'F','0','0','marketing:lead:remove','#','admin','2026-07-19 13:27:35','',NULL,''),(4045,'线索导出',4040,5,'','','','',1,0,'F','0','0','marketing:lead:export','#','admin','2026-07-19 13:27:35','',NULL,''),(4046,'线索领取',4040,6,'','','','',1,0,'F','0','0','marketing:lead:receive','#','admin','2026-07-19 13:27:35','',NULL,''),(4047,'线索分配',4040,7,'','','','',1,0,'F','0','0','marketing:lead:assign','#','admin','2026-07-19 13:27:35','',NULL,''),(4048,'标签删除',4036,4,'','','','',1,0,'F','0','0','marketing:tag:remove','#','admin','2026-07-19 13:30:00','',NULL,''),(4049,'标签导出',4036,5,'','','','',1,0,'F','0','0','marketing:tag:export','#','admin','2026-07-19 13:30:00','',NULL,''),(4050,'线索公海',4002,2,'pool','mk/lead/pool','','MkLeadPool',1,0,'C','0','0','marketing:lead:pool','pool','admin','2026-07-19 13:27:35','',NULL,''),(4060,'商机列表',4003,1,'list','mk/opportunity/index','','MkOpportunity',1,0,'C','0','0','marketing:opportunity:list','list','admin','2026-07-19 13:27:35','',NULL,''),(4061,'商机查询',4060,1,'','','','',1,0,'F','0','0','marketing:opportunity:query','#','admin','2026-07-19 13:27:35','',NULL,''),(4062,'商机新增',4060,2,'','','','',1,0,'F','0','0','marketing:opportunity:add','#','admin','2026-07-19 13:27:35','',NULL,''),(4063,'商机修改',4060,3,'','','','',1,0,'F','0','0','marketing:opportunity:edit','#','admin','2026-07-19 13:27:35','',NULL,''),(4064,'商机删除',4060,4,'','','','',1,0,'F','0','0','marketing:opportunity:remove','#','admin','2026-07-19 13:27:35','',NULL,''),(4065,'商机导出',4060,5,'','','','',1,0,'F','0','0','marketing:opportunity:export','#','admin','2026-07-19 13:27:35','',NULL,''),(4070,'商机看板',4003,2,'kanban','mk/opportunity/kanban','','MkOpportunityKanban',1,0,'C','0','0','marketing:opportunity:list','dashboard','admin','2026-07-19 13:27:35','',NULL,''),(4075,'阶段配置',4003,3,'stage','mk/opportunity/stage','','MkOpportunityStage',1,0,'C','0','0','marketing:stage:list','setting','admin','2026-07-19 13:27:35','',NULL,''),(4080,'活动管理',4004,1,'list','mk/activity/index','','MkActivity',1,0,'C','0','0','marketing:activity:list','list','admin','2026-07-19 13:27:35','',NULL,''),(4081,'活动查询',4080,1,'','','','',1,0,'F','0','0','marketing:activity:query','#','admin','2026-07-19 13:27:35','',NULL,''),(4082,'活动新增',4080,2,'','','','',1,0,'F','0','0','marketing:activity:add','#','admin','2026-07-19 13:27:35','',NULL,''),(4083,'活动修改',4080,3,'','','','',1,0,'F','0','0','marketing:activity:edit','#','admin','2026-07-19 13:27:35','',NULL,''),(4084,'活动删除',4080,4,'','','','',1,0,'F','0','0','marketing:activity:remove','#','admin','2026-07-19 13:27:35','',NULL,''),(4085,'活动导出',4080,5,'','','','',1,0,'F','0','0','marketing:activity:export','#','admin','2026-07-19 13:27:35','',NULL,''),(4090,'合同管理',4005,1,'contract','mk/contract/index','','MkContract',1,0,'C','0','0','marketing:contract:list','list','admin','2026-07-19 13:27:35','',NULL,''),(4091,'合同查询',4090,1,'','','','',1,0,'F','0','0','marketing:contract:query','#','admin','2026-07-19 13:27:35','',NULL,''),(4092,'合同新增',4090,2,'','','','',1,0,'F','0','0','marketing:contract:add','#','admin','2026-07-19 13:27:35','',NULL,''),(4093,'合同修改',4090,3,'','','','',1,0,'F','0','0','marketing:contract:edit','#','admin','2026-07-19 13:27:35','',NULL,''),(4094,'合同删除',4090,4,'','','','',1,0,'F','0','0','marketing:contract:remove','#','admin','2026-07-19 13:27:35','',NULL,''),(4095,'合同导出',4090,5,'','','','',1,0,'F','0','0','marketing:contract:export','#','admin','2026-07-19 13:27:35','',NULL,''),(4096,'合同审批',4090,6,'','','','',1,0,'F','0','0','marketing:contract:approve','#','admin','2026-07-20 11:33:28','',NULL,''),(4100,'订单管理',4005,2,'order','mk/order/index','','MkOrder',1,0,'C','0','0','marketing:order:list','list','admin','2026-07-19 13:27:35','',NULL,''),(4101,'订单查询',4100,1,'','','','',1,0,'F','0','0','marketing:order:query','#','admin','2026-07-19 13:27:35','',NULL,''),(4102,'订单新增',4100,2,'','','','',1,0,'F','0','0','marketing:order:add','#','admin','2026-07-19 13:27:35','',NULL,''),(4103,'订单修改',4100,3,'','','','',1,0,'F','0','0','marketing:order:edit','#','admin','2026-07-19 13:27:35','',NULL,''),(4104,'订单删除',4100,4,'','','','',1,0,'F','0','0','marketing:order:remove','#','admin','2026-07-19 13:27:35','',NULL,''),(4105,'订单导出',4100,5,'','','','',1,0,'F','0','0','marketing:order:export','#','admin','2026-07-19 13:27:35','',NULL,''),(4106,'订单发货',4100,6,'','','','',1,0,'F','0','0','marketing:order:deliver','#','admin','2026-07-20 15:50:29','',NULL,''),(4110,'回款管理',4005,5,'payment','mk/payment/index','','MkPayment',1,0,'C','0','0','marketing:payment:list','money','admin','2026-07-19 13:27:35','',NULL,''),(4111,'回款查询',4110,1,'','','','',1,0,'F','0','0','marketing:payment:query','#','admin','2026-07-19 13:27:35','',NULL,''),(4112,'回款新增',4110,2,'','','','',1,0,'F','0','0','marketing:payment:add','#','admin','2026-07-19 13:27:35','',NULL,''),(4113,'回款修改',4110,3,'','','','',1,0,'F','0','0','marketing:payment:edit','#','admin','2026-07-19 13:27:35','',NULL,''),(4114,'回款删除',4110,4,'','','','',1,0,'F','0','0','marketing:payment:remove','#','admin','2026-07-19 13:27:35','',NULL,''),(4120,'营销看板',4006,1,'dashboard','mk/dashboard/index','','MkDashboard',1,0,'C','0','0','marketing:report:view','chart','admin','2026-07-19 13:27:35','',NULL,''),(4130,'活动参与人',4004,2,'participant','mk/participant/index','','MkParticipant',1,0,'C','0','0','marketing:participant:list','people','admin','2026-07-19 13:30:00','',NULL,'活动参与人管理'),(4131,'参与人查询',4130,1,'','','','',1,0,'F','0','0','marketing:participant:query','#','admin','2026-07-19 13:30:00','',NULL,''),(4132,'参与人新增',4130,2,'','','','',1,0,'F','0','0','marketing:participant:add','#','admin','2026-07-19 13:30:00','',NULL,''),(4133,'参与人修改',4130,3,'','','','',1,0,'F','0','0','marketing:participant:edit','#','admin','2026-07-19 13:30:00','',NULL,''),(4134,'参与人删除',4130,4,'','','','',1,0,'F','0','0','marketing:participant:remove','#','admin','2026-07-19 13:30:00','',NULL,''),(4135,'参与人导出',4130,5,'','','','',1,0,'F','0','0','marketing:participant:export','#','admin','2026-07-19 13:30:00','',NULL,''),(4140,'编号规则',4000,7,'numberRule','mk/numberRule/index','{\"module\":\"mk\"}','MkNumberRule',1,0,'C','0','0','marketing:numberRule:list','number','admin','2026-07-21 20:33:52','',NULL,'编号规则管理'),(4141,'规则查询',4140,1,'','','','',1,0,'F','0','0','marketing:numberRule:query','#','admin','2026-07-21 20:33:52','',NULL,''),(4142,'规则新增',4140,2,'','','','',1,0,'F','0','0','marketing:numberRule:add','#','admin','2026-07-21 20:33:52','',NULL,''),(4143,'规则修改',4140,3,'','','','',1,0,'F','0','0','marketing:numberRule:edit','#','admin','2026-07-21 20:33:52','',NULL,''),(4144,'规则删除',4140,4,'','','','',1,0,'F','0','0','marketing:numberRule:remove','#','admin','2026-07-21 20:33:52','',NULL,''),(4145,'规则导出',4140,5,'','','','',1,0,'F','0','0','marketing:numberRule:export','#','admin','2026-07-21 20:33:52','',NULL,''),(4146,'编号规则',3000,99,'dmsNumberRule','mk/numberRule/index','{\"module\":\"dms\"}','DmsNumberRule',1,0,'C','0','0','marketing:numberRule:list','number','admin','2026-07-21 22:49:00','',NULL,'设备管理编号规则'),(4147,'编号规则',2000,99,'wmsNumberRule','mk/numberRule/index','{\"module\":\"wms\"}','WmsNumberRule',1,0,'C','0','0','marketing:numberRule:list','number','admin','2026-07-21 22:49:00','',NULL,'仓储管理编号规则'),(4148,'活动状态变更',4080,10,'#','',NULL,'',1,0,'F','0','0','marketing:activity:status','#','admin','2026-07-21 23:29:58','',NULL,''),(4149,'活动复盘',4080,11,'#','',NULL,'',1,0,'F','0','0','marketing:activity:review','#','admin','2026-07-21 23:29:58','',NULL,''),(4150,'活动复制',4080,12,'#','',NULL,'',1,0,'F','0','0','marketing:activity:copy','#','admin','2026-07-21 23:29:58','',NULL,''),(4151,'联系人合并',4020,10,'#','',NULL,'',1,0,'F','0','0','marketing:contact:merge','#','admin','2026-07-21 23:29:58','',NULL,''),(4152,'联系人分配',4020,11,'#','',NULL,'',1,0,'F','0','0','marketing:contact:assign','#','admin','2026-07-21 23:29:58','',NULL,''),(4153,'联系人导入',4020,12,'#','',NULL,'',1,0,'F','0','0','marketing:contact:import','#','admin','2026-07-21 23:29:58','',NULL,''),(4154,'参与人转线索',4130,10,'#','',NULL,'',1,0,'F','0','0','marketing:participant:convert','#','admin','2026-07-21 23:29:58','',NULL,''),(4155,'客户公海',4001,5,'customerPool','mk/customer/pool','','MkCustomerPool',1,0,'C','0','0','marketing:customer:pool','peoples','admin','2026-07-22 22:49:17','',NULL,'客户公海池'),(4156,'销售人员分析',4006,2,'salesperson','mk/salespersonReport/index',NULL,'',1,0,'C','0','0','marketing:report:view','chart','admin','2026-07-23 00:48:59','',NULL,'销售人员多维度分析统计报表'),(4157,'发票管理',4005,13,'invoice','mk/invoice/index',NULL,'',1,0,'C','0','0','marketing:invoice:list','edit','admin','2026-07-23 07:34:11','',NULL,''),(4158,'订单退货',4005,4,'return','mk/returnOrder/index',NULL,'',1,0,'C','0','0','marketing:return:list','swap','admin','2026-07-23 07:34:11','',NULL,''),(4159,'发票查询',4157,1,'',NULL,NULL,'',1,0,'F','0','0','marketing:invoice:query','#','admin','2026-07-23 07:34:11','',NULL,''),(4160,'发票新增',4157,2,'',NULL,NULL,'',1,0,'F','0','0','marketing:invoice:add','#','admin','2026-07-23 07:34:11','',NULL,''),(4161,'发票修改',4157,3,'',NULL,NULL,'',1,0,'F','0','0','marketing:invoice:edit','#','admin','2026-07-23 07:34:11','',NULL,''),(4162,'发票删除',4157,4,'',NULL,NULL,'',1,0,'F','0','0','marketing:invoice:remove','#','admin','2026-07-23 07:34:11','',NULL,''),(4163,'发票导出',4157,5,'',NULL,NULL,'',1,0,'F','0','0','marketing:invoice:export','#','admin','2026-07-23 07:34:11','',NULL,''),(4164,'发票开票',4157,6,'',NULL,NULL,'',1,0,'F','0','0','marketing:invoice:issue','#','admin','2026-07-23 07:34:11','',NULL,''),(4165,'发票作废',4157,7,'',NULL,NULL,'',1,0,'F','0','0','marketing:invoice:void','#','admin','2026-07-23 07:34:11','',NULL,''),(4166,'退货查询',4158,1,'',NULL,NULL,'',1,0,'F','0','0','marketing:return:query','#','admin','2026-07-23 07:34:11','',NULL,''),(4167,'退货新增',4158,2,'',NULL,NULL,'',1,0,'F','0','0','marketing:return:add','#','admin','2026-07-23 07:34:11','',NULL,''),(4168,'退货删除',4158,3,'',NULL,NULL,'',1,0,'F','0','0','marketing:return:remove','#','admin','2026-07-23 07:34:11','',NULL,''),(4169,'退货导出',4158,4,'',NULL,NULL,'',1,0,'F','0','0','marketing:return:export','#','admin','2026-07-23 07:34:11','',NULL,''),(4170,'退货审批',4158,5,'',NULL,NULL,'',1,0,'F','0','0','marketing:return:approve','#','admin','2026-07-23 07:34:11','',NULL,''),(4171,'退货退款',4158,6,'',NULL,NULL,'',1,0,'F','0','0','marketing:return:refund','#','admin','2026-07-23 07:34:11','',NULL,''),(4172,'回款确认',4110,5,'',NULL,NULL,'',1,0,'F','0','0','marketing:payment:confirm','#','admin','2026-07-23 07:34:11','',NULL,''),(4173,'订单确认',4100,8,'',NULL,NULL,'',1,0,'F','0','0','marketing:order:confirm','#','admin','2026-07-23 07:34:11','',NULL,''),(4174,'订单签收',4100,9,'',NULL,NULL,'',1,0,'F','0','0','marketing:order:receive','#','admin','2026-07-23 07:34:11','',NULL,''),(4175,'订单取消',4100,7,'',NULL,NULL,'',1,0,'F','0','0','marketing:order:cancel','#','admin','2026-07-23 07:34:11','',NULL,''),(4176,'合同变更',4090,8,'',NULL,NULL,'',1,0,'F','0','0','marketing:contract:change','#','admin','2026-07-23 07:34:11','',NULL,''),(4177,'合同续签',4090,7,'',NULL,NULL,'',1,0,'F','0','0','marketing:contract:renew','#','admin','2026-07-23 07:34:11','',NULL,''),(4178,'客户导入',4010,6,'',NULL,NULL,'',1,0,'F','0','0','marketing:customer:import','#','admin','2026-07-23 14:57:43','',NULL,'客户批量导入'),(4181,'客户分配',4010,7,'',NULL,NULL,'',1,0,'F','0','0','marketing:customer:assign','#','admin','2026-07-23 14:57:43','',NULL,'客户转移分配'),(4184,'客户领取',4010,8,'',NULL,NULL,'',1,0,'F','0','0','marketing:customer:claim','#','admin','2026-07-23 14:57:43','',NULL,'从公海池领取客户'),(4187,'互动导出',4030,5,'',NULL,NULL,'',1,0,'F','0','0','marketing:interaction:export','#','admin','2026-07-23 14:57:43','',NULL,'互动记录导出'),(4188,'阶段查询',4075,1,'',NULL,NULL,'',1,0,'F','0','0','marketing:stage:query','#','admin','2026-07-23 14:57:43','',NULL,''),(4189,'阶段新增',4075,2,'',NULL,NULL,'',1,0,'F','0','0','marketing:stage:add','#','admin','2026-07-23 14:57:43','',NULL,''),(4190,'阶段修改',4075,3,'',NULL,NULL,'',1,0,'F','0','0','marketing:stage:edit','#','admin','2026-07-23 14:57:43','',NULL,''),(4191,'阶段删除',4075,4,'',NULL,NULL,'',1,0,'F','0','0','marketing:stage:remove','#','admin','2026-07-23 14:57:43','',NULL,''),(4192,'阶段导出',4075,5,'',NULL,NULL,'',1,0,'F','0','0','marketing:stage:export','#','admin','2026-07-23 14:57:43','',NULL,'');
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notice`
--

DROP TABLE IF EXISTS `sys_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice` (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob COMMENT '公告内容',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知公告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notice`
--

LOCK TABLES `sys_notice` WRITE;
/*!40000 ALTER TABLE `sys_notice` DISABLE KEYS */;
INSERT INTO `sys_notice` VALUES (1,'温馨提醒：2018-07-01 若依新版本发布啦','2',_binary '新版本内容','0','admin','2026-07-13 11:25:53','',NULL,'管理员'),(2,'维护通知：2018-07-01 若依系统凌晨维护','1',_binary '维护内容','0','admin','2026-07-13 11:25:53','',NULL,'管理员'),(3,'若依开源框架介绍','1',_binary '<p><span style=\"color: rgb(230, 0, 0);\">项目介绍</span></p><p><font color=\"#333333\">RuoYi开源项目是为企业用户定制的后台脚手架框架，为企业打造的一站式解决方案，降低企业开发成本，提升开发效率。主要包括用户管理、角色管理、部门管理、菜单管理、参数管理、字典管理、</font><span style=\"color: rgb(51, 51, 51);\">岗位管理</span><span style=\"color: rgb(51, 51, 51);\">、定时任务</span><span style=\"color: rgb(51, 51, 51);\">、</span><span style=\"color: rgb(51, 51, 51);\">服务监控、登录日志、操作日志、代码生成等功能。其中，还支持多数据源、数据权限、国际化、Redis缓存、Docker部署、滑动验证码、第三方认证登录、分布式事务、</span><font color=\"#333333\">分布式文件存储</font><span style=\"color: rgb(51, 51, 51);\">、分库分表处理等技术特点。</span></p><p><img src=\"https://foruda.gitee.com/images/1773931848342439032/a4d22313_1815095.png\" style=\"width: 64px;\"><br></p><p><span style=\"color: rgb(230, 0, 0);\">官网及演示</span></p><p><span style=\"color: rgb(51, 51, 51);\">若依官网地址：&nbsp;</span><a href=\"http://ruoyi.vip\" target=\"_blank\">http://ruoyi.vip</a><a href=\"http://ruoyi.vip\" target=\"_blank\"></a></p><p><span style=\"color: rgb(51, 51, 51);\">若依文档地址：&nbsp;</span><a href=\"http://doc.ruoyi.vip\" target=\"_blank\">http://doc.ruoyi.vip</a><br></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【不分离版】：&nbsp;</span><a href=\"http://demo.ruoyi.vip\" target=\"_blank\">http://demo.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【分离版本】：&nbsp;</span><a href=\"http://vue.ruoyi.vip\" target=\"_blank\">http://vue.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【微服务版】：&nbsp;</span><a href=\"http://cloud.ruoyi.vip\" target=\"_blank\">http://cloud.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【移动端版】：&nbsp;</span><a href=\"http://h5.ruoyi.vip\" target=\"_blank\">http://h5.ruoyi.vip</a></p><p><br style=\"color: rgb(48, 49, 51); font-family: &quot;Helvetica Neue&quot;, Helvetica, Arial, sans-serif; font-size: 12px;\"></p>','0','admin','2026-07-13 11:25:53','',NULL,'管理员'),(10,'PM维护提醒','1',_binary 'PM计划「4」已生成工单「WO202607170007」\n关联设备：立式铣床\n下次执行时间：Sun Aug 16 08:02:04 CST 2026','0','system','2026-07-17 08:02:04','',NULL,NULL),(11,'工单派工通知','1',_binary '工单「WO202607160003」已派工给您，设备：数控加工中心，请及时接单处理。','0','system','2026-07-17 09:16:13','',NULL,NULL),(12,'报修确认通知','1',_binary '您的报修工单「WO202607160003」已被维修员「李华」接单，正在处理中。','0','system','2026-07-17 09:16:16','',NULL,NULL),(13,'新工单通知','1',_binary '新工单「WO202607170008」已创建，设备：平面磨床，优先级：中，请尽快派工处理。','0','system','2026-07-17 09:20:13','',NULL,NULL),(14,'工单派工通知','1',_binary '工单「WO202607170008」已派工给您，设备：平面磨床，请及时接单处理。','0','system','2026-07-17 09:20:53','',NULL,NULL),(15,'工单改派通知','1',_binary '工单「WO202607170008」已改派给您，设备：平面磨床，请及时接单处理。','0','system','2026-07-17 09:21:01','',NULL,NULL),(16,'报修确认通知','1',_binary '您的报修工单「WO202607170008」已被维修员「李华」接单，正在处理中。','0','system','2026-07-17 09:21:03','',NULL,NULL),(17,'新工单通知','1',_binary '新工单「WO202607170009」已创建，优先级：高，请尽快派工处理。','0','system','2026-07-17 14:58:38','',NULL,NULL),(18,'新工单通知','1',_binary '新工单「WO202607170010」已创建，设备：EQ005 - 数控加工中心，优先级：高，请尽快派工处理。','0','system','2026-07-17 15:07:15','',NULL,NULL),(19,'新工单通知','1',_binary '新工单「WO202607170011」已创建，设备：EQ004 - 平面磨床、EQ002 - 立式铣床，优先级：高，请尽快派工处理。','0','system','2026-07-17 15:11:24','',NULL,NULL),(20,'新工单通知','1',_binary '新工单「WO202607170012」已创建，设备：EQ005 - 数控加工中心、EQ004 - 平面磨床，优先级：高，请尽快派工处理。','0','system','2026-07-17 15:42:07','',NULL,NULL),(21,'工单SLA超时升级通知','1',_binary '工单「WO202607170012」响应超时！，设备：EQ005 - 数控加工中心、EQ004 - 平面磨床，优先级：高，请立即处理。','0','system','2026-07-17 17:00:00','',NULL,NULL),(22,'新工单通知','1',_binary '新工单「WO202607170013」已创建，设备：空压机1，优先级：中，请尽快派工处理。','0','system','2026-07-17 17:51:35','',NULL,NULL),(23,'新工单通知','1',_binary '新工单「WO202607170014」已创建，设备：AirCompressor1，优先级：中，请尽快派工处理。','0','system','2026-07-17 18:08:35','',NULL,NULL),(24,'工单派工通知','1',_binary '工单「WO202607170014」已派工给您，设备：AirCompressor1，请及时接单处理。','0','system','2026-07-17 18:09:01','',NULL,NULL),(25,'报修确认通知','1',_binary '您的报修工单「WO202607170014」已被维修员「admin」接单，正在处理中。','0','system','2026-07-17 18:09:01','',NULL,NULL),(26,'新工单通知','1',_binary '新工单「WO202607170015」已创建，设备：AirComp1，优先级：中，请尽快派工处理。','0','system','2026-07-17 18:09:33','',NULL,NULL),(27,'新工单通知','1',_binary '新工单「WO202607170016」已创建，设备：数控加工中心，优先级：低，请尽快派工处理。','0','system','2026-07-17 19:46:35','',NULL,NULL),(28,'PM维护提醒','1',_binary 'PM计划「ddddddddd111」已生成工单「WO202607170016」\n关联设备：数控加工中心\n下次执行时间：Sun Aug 16 19:46:35 CST 2026','0','system','2026-07-17 19:46:35','',NULL,NULL),(29,'新工单通知','1',_binary '新工单「WO202607170017」已创建，设备：数控加工中心，优先级：低，请尽快派工处理。','0','system','2026-07-17 21:36:17','',NULL,NULL),(30,'PM维护提醒','1',_binary 'PM计划「ddddddddd111」已生成工单「WO202607170017」\n关联设备：数控加工中心\n下次执行时间：Sun Aug 16 21:36:17 CST 2026','0','system','2026-07-17 21:36:17','',NULL,NULL),(31,'新工单通知','1',_binary '新工单「WO202607170018」已创建，设备：平面磨床，优先级：低，请尽快派工处理。','0','system','2026-07-17 21:54:15','',NULL,NULL),(32,'PM维护提醒','1',_binary 'PM计划「xxxxxxxxxxxx1」已生成工单「WO202607170018」\n关联设备：平面磨床\n下次执行时间：Sun Aug 16 21:54:15 CST 2026','0','system','2026-07-17 21:54:15','',NULL,NULL),(33,'新工单通知','1',_binary '新工单「WO202607170019」已创建，设备：数控加工中心，优先级：中，请尽快派工处理。','0','system','2026-07-17 21:56:56','',NULL,NULL),(34,'工单派工通知','1',_binary '工单「WO202607170019」已派工给您，设备：数控加工中心，请及时接单处理。','0','system','2026-07-17 21:58:54','',NULL,NULL),(35,'报修确认通知','1',_binary '您的报修工单「WO202607170019」已被维修员「张涛」接单，正在处理中。','0','system','2026-07-17 21:58:58','',NULL,NULL),(36,'新工单通知','1',_binary '新工单「WO202607170020」已创建，设备：数控加工中心，优先级：低，请尽快派工处理。','0','system','2026-07-17 22:15:12','',NULL,NULL),(37,'PM维护提醒','1',_binary 'PM计划「测试计划001」已生成工单「WO202607170020」\n关联设备：数控加工中心\n下次执行时间：Sun Aug 16 22:15:12 CST 2026','0','system','2026-07-17 22:15:12','',NULL,NULL),(38,'新工单通知','1',_binary '新工单「WO202607170021」已创建，设备：数控加工中心，优先级：低，请尽快派工处理。','0','system','2026-07-17 22:38:34','',NULL,NULL),(39,'PM维护提醒','1',_binary 'PM计划「测试计划001」已生成工单「WO202607170021」\n关联设备：数控加工中心\n下次执行时间：Sun Aug 16 22:38:34 CST 2026','0','system','2026-07-17 22:38:34','',NULL,NULL),(40,'新工单通知','1',_binary '新工单「WO202607170022」已创建，设备：数控加工中心，优先级：低，请尽快派工处理。','0','system','2026-07-17 22:44:13','',NULL,NULL),(41,'PM维护提醒','1',_binary 'PM计划「测试计划001」已生成工单「WO202607170022」\n关联设备：数控加工中心\n下次执行时间：Sun Aug 16 22:44:13 CST 2026','0','system','2026-07-17 22:44:13','',NULL,NULL),(42,'新工单通知','1',_binary '新工单「WO202607170023」已创建，设备：数控加工中心，优先级：低，请尽快派工处理。','0','system','2026-07-17 22:46:20','',NULL,NULL),(43,'PM维护提醒','1',_binary 'PM计划「测试计划001」已生成工单「WO202607170023」\n关联设备：数控加工中心\n下次执行时间：Sun Aug 16 22:46:20 CST 2026','0','system','2026-07-17 22:46:20','',NULL,NULL),(44,'报修确认通知','1',_binary '您的报修工单「WO202607170023」已被维修员「张涛」接单，正在处理中。','0','system','2026-07-17 22:46:39','',NULL,NULL),(45,'新工单通知','1',_binary '新工单「WO202607170024」已创建，设备：TEST-EQ-T - 测试设备T，优先级：高，请尽快派工处理。','0','system','2026-07-17 22:48:42','',NULL,NULL),(46,'工单派工通知','1',_binary '工单「WO202607170024」已派工给您，设备：TEST-EQ-T - 测试设备T，请及时接单处理。','0','system','2026-07-17 22:50:05','',NULL,NULL),(47,'工单SLA超时升级通知','1',_binary '工单「WO202607170023」处理超时！，设备：数控加工中心，优先级：低，请立即处理。','0','system','2026-07-21 20:00:00','',NULL,NULL),(48,'新工单通知','1',_binary '新工单「WO202607210001」已创建，优先级：中，请尽快派工处理。','0','system','2026-07-21 22:41:13','',NULL,NULL);
/*!40000 ALTER TABLE `sys_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notice_read`
--

DROP TABLE IF EXISTS `sys_notice_read`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice_read` (
  `read_id` bigint NOT NULL AUTO_INCREMENT COMMENT '已读主键',
  `notice_id` int NOT NULL COMMENT '公告id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `read_time` datetime NOT NULL COMMENT '阅读时间',
  PRIMARY KEY (`read_id`),
  UNIQUE KEY `uk_user_notice` (`user_id`,`notice_id`) COMMENT '同一用户同一公告只记录一次'
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告已读记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notice_read`
--

LOCK TABLES `sys_notice_read` WRITE;
/*!40000 ALTER TABLE `sys_notice_read` DISABLE KEYS */;
INSERT INTO `sys_notice_read` VALUES (1,3,1,'2026-07-14 09:09:34'),(2,2,1,'2026-07-14 09:09:42'),(3,1,1,'2026-07-14 23:10:51'),(4,42,1,'2026-07-18 09:10:46'),(5,46,1,'2026-07-18 09:10:54'),(6,45,1,'2026-07-19 16:14:57'),(7,44,1,'2026-07-19 16:15:01'),(8,43,1,'2026-07-19 16:15:07');
/*!40000 ALTER TABLE `sys_notice_read` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_oper_log`
--

DROP TABLE IF EXISTS `sys_oper_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '模块标题',
  `business_type` int DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求方式',
  `operator_type` int DEFAULT '0' COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '返回参数',
  `status` int DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint DEFAULT '0' COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`),
  KEY `idx_sys_oper_log_bt` (`business_type`),
  KEY `idx_sys_oper_log_s` (`status`),
  KEY `idx_sys_oper_log_ot` (`oper_time`),
  KEY `idx_sys_oper_log_oname` (`oper_name`)
) ENGINE=InnoDB AUTO_INCREMENT=597 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_oper_log`
--

LOCK TABLES `sys_oper_log` WRITE;
/*!40000 ALTER TABLE `sys_oper_log` DISABLE KEYS */;
INSERT INTO `sys_oper_log` VALUES (100,'定时任务',2,'com.ruoyi.quartz.controller.SysJobController.run()','PUT',1,'admin','研发部门','/monitor/job/run','127.0.0.1','内网IP','{\"jobId\":1,\"misfirePolicy\":\"0\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-13 12:40:23',32),(101,'物料管理',5,'com.ruoyi.wms.controller.WmsMaterialController.export()','POST',1,'admin','研发部门','/wms/material/export','127.0.0.1','内网IP','{\"pageSize\":\"10\",\"pageNum\":\"1\"}',NULL,1,'\r\n### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'shelf_life_days\' in \'field list\'\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-wms-3.9.2.jar!/mapper/wms/WmsMaterialMapper.xml]\r\n### The error may involve com.ruoyi.wms.mapper.WmsMaterialMapper.selectMaterialList-Inline\r\n### The error occurred while setting parameters\r\n### SQL: SELECT material_id, material_code, material_name, material_type, spec_model, unit,                shelf_life_days, expiry_alert_days, production_date, expiry_date,                safety_stock_min, safety_stock_max, is_batch_manage, status, del_flag,                create_by, create_time, update_by, update_time, remark         FROM wms_material                WHERE del_flag = \'0\'          ORDER BY material_id DESC\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'shelf_life_days\' in \'field list\'\n; bad SQL grammar []','2026-07-14 09:21:25',166),(102,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2026-07-13 11:25:52\",\"icon\":\"guide\",\"isCache\":\"0\",\"isFrame\":\"0\",\"menuId\":4,\"menuName\":\"若依官网\",\"menuType\":\"M\",\"orderNum\":4,\"params\":{},\"parentId\":0,\"path\":\"http://ruoyi.vip\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"1\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 09:26:45',29),(103,'保存菜单排序',2,'com.ruoyi.web.controller.system.SysMenuController.updateSort()','PUT',1,'admin','研发部门','/system/menu/updateSort','127.0.0.1','内网IP','{\"menuIds\":\"1,2\",\"orderNums\":\"10,15\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 09:27:43',19),(104,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2026-07-13 11:25:52\",\"icon\":\"tool\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":3,\"menuName\":\"系统工具\",\"menuType\":\"M\",\"orderNum\":3,\"params\":{},\"parentId\":0,\"path\":\"tool\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 09:28:29',7),(105,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"component\":\"tool/build/index\",\"createTime\":\"2026-07-13 11:25:52\",\"icon\":\"build\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":115,\"menuName\":\"表单构建\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":3,\"path\":\"build\",\"perms\":\"tool:build:list\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 09:28:47',12),(106,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"component\":\"tool/gen/index\",\"createTime\":\"2026-07-13 11:25:52\",\"icon\":\"code\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":116,\"menuName\":\"代码生成\",\"menuType\":\"C\",\"orderNum\":2,\"params\":{},\"parentId\":3,\"path\":\"gen\",\"perms\":\"tool:gen:list\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 09:28:54',9),(107,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"component\":\"tool/swagger/index\",\"createTime\":\"2026-07-13 11:25:52\",\"icon\":\"swagger\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":117,\"menuName\":\"系统接口\",\"menuType\":\"C\",\"orderNum\":3,\"params\":{},\"parentId\":3,\"path\":\"swagger\",\"perms\":\"tool:swagger:list\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 09:29:01',12),(108,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2026-07-13 11:25:52\",\"icon\":\"tool\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":3,\"menuName\":\"系统工具\",\"menuType\":\"M\",\"orderNum\":3,\"params\":{},\"parentId\":0,\"path\":\"tool\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"1\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 09:30:02',7),(109,'物料管理',2,'com.ruoyi.wms.controller.WmsMaterialController.edit()','PUT',1,'admin','研发部门','/wms/material','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"createTime\":\"2026-07-14 09:43:11\",\"delFlag\":\"0\",\"expiryAlertDays\":30,\"isBatchManage\":\"0\",\"materialCode\":\"MAT20250007\",\"materialId\":7,\"materialName\":\"包装纸箱 400*300*250\",\"materialType\":\"3\",\"params\":{},\"remark\":\"成品外包装\",\"safetyStockMax\":5000,\"safetyStockMin\":200,\"specModel\":\"五层瓦楞 K=K\",\"status\":\"0\",\"unit\":\"2\",\"updateBy\":\"admin\",\"updateTime\":\"2026-07-14 09:43:11\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 11:19:10',27),(110,'仓库管理',1,'com.ruoyi.wms.controller.WmsWarehouseController.add()','POST',1,'admin','研发部门','/wms/warehouse','127.0.0.1','内网IP','{\"ancestors\":\"0\",\"areaType\":\"0\",\"capacity\":0,\"delFlag\":\"0\",\"locationType\":\"0\",\"nodeType\":\"1\",\"params\":{},\"parentId\":0,\"status\":\"0\",\"warehouseCode\":\"WH001\",\"warehouseId\":1,\"warehouseName\":\"成品库\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 11:34:47',48),(111,'仓库管理',1,'com.ruoyi.wms.controller.WmsWarehouseController.add()','POST',1,'admin','研发部门','/wms/warehouse','127.0.0.1','内网IP','{\"ancestors\":\"0,1\",\"areaType\":\"0\",\"capacity\":0,\"delFlag\":\"0\",\"locationType\":\"0\",\"nodeType\":\"2\",\"params\":{},\"parentId\":1,\"status\":\"0\",\"warehouseCode\":\"WH001A01\",\"warehouseId\":2,\"warehouseName\":\"东区\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 11:35:41',14),(112,'仓库管理',1,'com.ruoyi.wms.controller.WmsWarehouseController.add()','POST',1,'admin','研发部门','/wms/warehouse','127.0.0.1','内网IP','{\"ancestors\":\"0,1,2\",\"areaType\":\"0\",\"capacity\":0,\"delFlag\":\"0\",\"locationStatus\":\"0\",\"locationType\":\"0\",\"nodeType\":\"3\",\"params\":{},\"parentId\":2,\"status\":\"0\",\"warehouseCode\":\"WH001A01L01\",\"warehouseId\":3,\"warehouseName\":\"东区001\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 11:35:52',15),(113,'仓库管理',1,'com.ruoyi.wms.controller.WmsWarehouseController.add()','POST',1,'admin','研发部门','/wms/warehouse','127.0.0.1','内网IP','{\"ancestors\":\"0,1\",\"areaType\":\"0\",\"capacity\":0,\"delFlag\":\"0\",\"locationType\":\"0\",\"nodeType\":\"2\",\"params\":{},\"parentId\":1,\"status\":\"0\",\"warehouseCode\":\"WH001A02\",\"warehouseId\":4,\"warehouseName\":\"西区\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 11:36:03',15),(114,'仓库管理',1,'com.ruoyi.wms.controller.WmsWarehouseController.add()','POST',1,'admin','研发部门','/wms/warehouse','127.0.0.1','内网IP','{\"ancestors\":\"0,1,4\",\"areaType\":\"0\",\"capacity\":0,\"delFlag\":\"0\",\"locationStatus\":\"0\",\"locationType\":\"0\",\"nodeType\":\"3\",\"params\":{},\"parentId\":4,\"status\":\"0\",\"warehouseCode\":\"WH001A02L01\",\"warehouseId\":5,\"warehouseName\":\"西区01\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 11:36:12',12),(115,'仓库管理',1,'com.ruoyi.wms.controller.WmsWarehouseController.add()','POST',1,'admin','研发部门','/wms/warehouse','127.0.0.1','内网IP','{\"ancestors\":\"0\",\"areaType\":\"0\",\"capacity\":0,\"delFlag\":\"0\",\"locationStatus\":\"0\",\"locationType\":\"0\",\"nodeType\":\"3\",\"params\":{},\"parentId\":0,\"status\":\"0\",\"warehouseCode\":\"WH000A00L01\",\"warehouseId\":6,\"warehouseName\":\"ttest\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 11:38:15',7),(116,'仓库管理',3,'com.ruoyi.wms.controller.WmsWarehouseController.remove()','DELETE',1,'admin','研发部门','/wms/warehouse/6','127.0.0.1','内网IP','[6] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 11:38:24',12),(117,'入库管理',1,'com.ruoyi.wms.controller.WmsInboundOrderController.add()','POST',1,'admin','研发部门','/wms/inbound','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"detailList\":[{\"delFlag\":\"0\",\"orderId\":1,\"params\":{},\"planQty\":0,\"putawayQty\":0,\"receivedQty\":0,\"unitPrice\":0}],\"orderId\":1,\"orderNo\":\"IN1784001103147\",\"orderType\":\"3\",\"params\":{},\"status\":\"0\",\"totalQty\":0,\"warehouseId\":7} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Column \'material_id\' cannot be null\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-wms-3.9.2.jar!/mapper/wms/WmsInboundOrderMapper.xml]\r\n### The error may involve com.ruoyi.wms.mapper.WmsInboundOrderMapper.insertInboundDetail-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO wms_inbound_order_detail (order_id, material_id, plan_qty, received_qty, putaway_qty,             batch_no, production_date, expiry_date, location_id, unit_price, create_by, create_time, remark)         VALUES (?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, sysdate(), ?)\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Column \'material_id\' cannot be null\n; Column \'material_id\' cannot be null','2026-07-14 11:51:43',147),(118,'仓库管理',2,'com.ruoyi.wms.controller.WmsWarehouseController.edit()','PUT',1,'admin','研发部门','/wms/warehouse','127.0.0.1','内网IP','{\"ancestors\":\"0\",\"areaType\":\"0\",\"capacity\":0,\"createTime\":\"2026-07-14 11:34:47\",\"delFlag\":\"0\",\"deptId\":101,\"locationType\":\"0\",\"managerId\":2,\"nodeType\":\"1\",\"params\":{},\"parentId\":0,\"status\":\"0\",\"updateBy\":\"\",\"warehouseCode\":\"WH001\",\"warehouseId\":1,\"warehouseName\":\"成品库\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 11:55:09',8),(119,'入库管理',1,'com.ruoyi.wms.controller.WmsInboundOrderController.add()','POST',1,'admin','研发部门','/wms/inbound','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"detailList\":[{\"delFlag\":\"0\",\"materialId\":9,\"orderId\":2,\"params\":{},\"planQty\":10,\"productionDate\":\"2026-07-14\",\"putawayQty\":0,\"receivedQty\":0,\"unitPrice\":1},{\"batchNo\":\"0714001\",\"delFlag\":\"0\",\"materialId\":5,\"orderId\":2,\"params\":{},\"planQty\":20,\"productionDate\":\"2026-07-14\",\"putawayQty\":0,\"receivedQty\":0,\"unitPrice\":2.5}],\"orderId\":2,\"orderNo\":\"IN1784001957562\",\"orderType\":\"0\",\"params\":{},\"status\":\"0\",\"supplierId\":4,\"totalQty\":30,\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 12:05:57',12),(120,'入库提交',2,'com.ruoyi.wms.controller.WmsInboundOrderController.submit()','PUT',1,'admin','研发部门','/wms/inbound/submit/2','127.0.0.1','内网IP','2 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 12:14:59',39),(121,'入库收货',2,'com.ruoyi.wms.controller.WmsInboundOrderController.receive()','PUT',1,'admin','研发部门','/wms/inbound/receive/2/1','127.0.0.1','内网IP','{\"receivedQty\":\"10\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 12:15:27',17),(122,'入库收货',2,'com.ruoyi.wms.controller.WmsInboundOrderController.receive()','PUT',1,'admin','研发部门','/wms/inbound/receive/2/2','127.0.0.1','内网IP','{\"receivedQty\":\"20\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 12:15:30',17),(123,'入库上架',2,'com.ruoyi.wms.controller.WmsInboundOrderController.putaway()','PUT',1,'admin','研发部门','/wms/inbound/putaway/2/1','127.0.0.1','内网IP','{\"putawayQty\":\"5\",\"locationId\":\"3\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 12:15:40',25),(124,'入库上架',2,'com.ruoyi.wms.controller.WmsInboundOrderController.putaway()','PUT',1,'admin','研发部门','/wms/inbound/putaway/2/1','127.0.0.1','内网IP','{\"putawayQty\":\"5\",\"locationId\":\"5\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 12:15:47',15),(125,'入库上架',2,'com.ruoyi.wms.controller.WmsInboundOrderController.putaway()','PUT',1,'admin','研发部门','/wms/inbound/putaway/2/2','127.0.0.1','内网IP','{\"putawayQty\":\"20\",\"locationId\":\"5\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 12:16:05',19),(126,'入库管理',1,'com.ruoyi.wms.controller.WmsInboundOrderController.add()','POST',1,'admin','研发部门','/wms/inbound','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"detailList\":[{\"batchNo\":\"0714002\",\"delFlag\":\"0\",\"materialId\":5,\"orderId\":3,\"params\":{},\"planQty\":8,\"productionDate\":\"2026-07-14\",\"putawayQty\":0,\"receivedQty\":0,\"unitPrice\":1}],\"orderId\":3,\"orderNo\":\"IN1784002789234\",\"orderType\":\"0\",\"params\":{},\"status\":\"0\",\"supplierId\":3,\"totalQty\":8,\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 12:19:49',32),(127,'入库提交',2,'com.ruoyi.wms.controller.WmsInboundOrderController.submit()','PUT',1,'admin','研发部门','/wms/inbound/submit/3','127.0.0.1','内网IP','3 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 12:19:53',11),(128,'入库收货',2,'com.ruoyi.wms.controller.WmsInboundOrderController.receive()','PUT',1,'admin','研发部门','/wms/inbound/receive/3/3','127.0.0.1','内网IP','{\"receivedQty\":\"8\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 12:29:09',13),(129,'入库上架',2,'com.ruoyi.wms.controller.WmsInboundOrderController.putaway()','PUT',1,'admin','研发部门','/wms/inbound/putaway/3/3','127.0.0.1','内网IP','{\"putawayQty\":\"8\",\"locationId\":\"3\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 12:29:43',15),(130,'入库管理',1,'com.ruoyi.wms.controller.WmsInboundOrderController.add()','POST',1,'admin','研发部门','/wms/inbound','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"detailList\":[{\"batchNo\":\"0714002\",\"delFlag\":\"0\",\"materialId\":6,\"orderId\":4,\"params\":{},\"planQty\":6,\"productionDate\":\"2026-07-14\",\"putawayQty\":0,\"receivedQty\":0,\"unitPrice\":1}],\"orderId\":4,\"orderNo\":\"IN1784006864761\",\"orderType\":\"0\",\"params\":{},\"status\":\"0\",\"supplierId\":3,\"totalQty\":6,\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 13:27:44',41),(131,'入库提交',2,'com.ruoyi.wms.controller.WmsInboundOrderController.submit()','PUT',1,'admin','研发部门','/wms/inbound/submit/4','127.0.0.1','内网IP','4 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 13:27:48',15),(132,'入库收货',2,'com.ruoyi.wms.controller.WmsInboundOrderController.receive()','PUT',1,'admin','研发部门','/wms/inbound/receive/4/4','127.0.0.1','内网IP','{\"receivedQty\":\"6\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 13:28:03',12),(133,'入库上架',2,'com.ruoyi.wms.controller.WmsInboundOrderController.putaway()','PUT',1,'admin','研发部门','/wms/inbound/putaway/4/4','127.0.0.1','内网IP','{\"putawayQty\":\"6\",\"locationId\":\"3\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 13:28:13',18),(134,'入库管理',1,'com.ruoyi.wms.controller.WmsInboundOrderController.add()','POST',1,'admin','研发部门','/wms/inbound','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"detailList\":[{\"delFlag\":\"0\",\"materialId\":9,\"orderId\":5,\"params\":{},\"planQty\":6,\"productionDate\":\"2026-07-14\",\"putawayQty\":0,\"receivedQty\":0,\"unitPrice\":2}],\"orderId\":5,\"orderNo\":\"IN1784008039260\",\"orderType\":\"0\",\"params\":{},\"status\":\"0\",\"totalQty\":6,\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 13:47:19',170),(135,'入库提交',2,'com.ruoyi.wms.controller.WmsInboundOrderController.submit()','PUT',1,'admin','研发部门','/wms/inbound/submit/5','127.0.0.1','内网IP','5 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 13:47:22',10),(136,'入库收货',2,'com.ruoyi.wms.controller.WmsInboundOrderController.receive()','PUT',1,'admin','研发部门','/wms/inbound/receive/5/5','127.0.0.1','内网IP','{\"receivedQty\":\"6\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 13:47:32',17),(137,'入库上架',2,'com.ruoyi.wms.controller.WmsInboundOrderController.putaway()','PUT',1,'admin','研发部门','/wms/inbound/putaway/5/5','127.0.0.1','内网IP','{\"putawayQty\":\"6\",\"locationId\":\"3\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 13:47:38',22),(138,'出库管理',1,'com.ruoyi.wms.controller.WmsOutboundOrderController.add()','POST',1,'admin','研发部门','/wms/outbound','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"detailList\":[{\"actualQty\":0,\"batchNo\":\"0714002\",\"delFlag\":\"0\",\"locationCode\":\"WH001A01L01\",\"locationId\":3,\"materialCode\":\"MAT20250006\",\"materialId\":6,\"materialName\":\"无线蓝牙耳机\",\"orderId\":1,\"params\":{},\"pickQty\":0,\"planQty\":6,\"specModel\":\"白色 降噪版 Type-C充电\",\"unit\":\"1\"}],\"orderId\":1,\"orderNo\":\"OUT1784008950208\",\"orderType\":\"0\",\"params\":{},\"status\":\"0\",\"totalQty\":6,\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:02:30',262),(139,'出库提交',2,'com.ruoyi.wms.controller.WmsOutboundOrderController.submit()','PUT',1,'admin','研发部门','/wms/outbound/submit/1','127.0.0.1','内网IP','1 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:04:01',11),(140,'出库拣货',2,'com.ruoyi.wms.controller.WmsOutboundOrderController.pick()','PUT',1,'admin','研发部门','/wms/outbound/pick/1/1','127.0.0.1','内网IP','{\"pickQty\":\"6\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:04:12',25),(141,'出库复核',2,'com.ruoyi.wms.controller.WmsOutboundOrderController.check()','PUT',1,'admin','研发部门','/wms/outbound/check/1/1','127.0.0.1','内网IP','{\"actualQty\":\"6\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:07:46',12),(142,'出库管理',1,'com.ruoyi.wms.controller.WmsOutboundOrderController.add()','POST',1,'admin','研发部门','/wms/outbound','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"detailList\":[{\"actualQty\":0,\"batchNo\":\"0714002\",\"delFlag\":\"0\",\"locationCode\":\"WH001A01L01\",\"locationId\":3,\"materialCode\":\"MAT20250005\",\"materialId\":5,\"materialName\":\"智能手环 Pro\",\"orderId\":2,\"params\":{},\"pickQty\":0,\"planQty\":8,\"specModel\":\"黑色 硅胶表带 心率版\",\"unit\":\"1\"}],\"orderId\":2,\"orderNo\":\"OUT1784009899762\",\"orderType\":\"0\",\"params\":{},\"status\":\"0\",\"totalQty\":8,\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:18:19',10),(143,'出库提交',2,'com.ruoyi.wms.controller.WmsOutboundOrderController.submit()','PUT',1,'admin','研发部门','/wms/outbound/submit/2','127.0.0.1','内网IP','2 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:18:26',13),(144,'出库拣货',2,'com.ruoyi.wms.controller.WmsOutboundOrderController.pick()','PUT',1,'admin','研发部门','/wms/outbound/pick/2/2','127.0.0.1','内网IP','{\"pickQty\":\"8\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:24:39',11),(145,'出库复核',2,'com.ruoyi.wms.controller.WmsOutboundOrderController.check()','PUT',1,'admin','研发部门','/wms/outbound/check/2/2','127.0.0.1','内网IP','{\"actualQty\":\"8\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:25:14',7),(146,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"component\":\"wms/outbound/wave\",\"createTime\":\"2026-07-13 23:38:37\",\"icon\":\"group\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2073,\"menuName\":\"批次管理\",\"menuType\":\"C\",\"orderNum\":4,\"params\":{},\"parentId\":2003,\"path\":\"wave\",\"perms\":\"wms:wave:list\",\"query\":\"\",\"routeName\":\"WmsOutboundWave\",\"status\":\"1\",\"updateBy\":\"admin\",\"visible\":\"1\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:27:38',18),(147,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"component\":\"wms/outbound/check\",\"createTime\":\"2026-07-13 23:38:37\",\"icon\":\"scan\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2072,\"menuName\":\"扫码复核\",\"menuType\":\"C\",\"orderNum\":3,\"params\":{},\"parentId\":2003,\"path\":\"check\",\"perms\":\"wms:outbound:check\",\"query\":\"\",\"routeName\":\"WmsOutboundCheck\",\"status\":\"1\",\"updateBy\":\"admin\",\"visible\":\"1\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:27:44',9),(148,'盘点管理',1,'com.ruoyi.wms.controller.WmsStockTakeController.add()','POST',1,'admin','研发部门','/wms/stocktake','127.0.0.1','内网IP','{\"areaId\":2,\"delFlag\":\"0\",\"params\":{},\"planDate\":\"2026-07-14\",\"status\":\"0\",\"takeId\":1,\"takeNo\":\"ST1784010757236\",\"takeType\":\"0\",\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:32:37',18),(149,'开始盘点',2,'com.ruoyi.wms.controller.WmsStockTakeController.start()','PUT',1,'admin','研发部门','/wms/stocktake/start/1','127.0.0.1','内网IP','1 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:32:40',5),(150,'盘点管理',1,'com.ruoyi.wms.controller.WmsStockTakeController.add()','POST',1,'admin','研发部门','/wms/stocktake','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"params\":{},\"status\":\"0\",\"takeId\":2,\"takeNo\":\"ST1784010781572\",\"takeType\":\"0\",\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:33:01',12),(151,'开始盘点',2,'com.ruoyi.wms.controller.WmsStockTakeController.start()','PUT',1,'admin','研发部门','/wms/stocktake/start/2','127.0.0.1','内网IP','2 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:33:05',9),(152,'提交盘点明细',2,'com.ruoyi.wms.controller.WmsStockTakeController.submitDetail()','PUT',1,'admin','研发部门','/wms/stocktake/submit/2/6','127.0.0.1','内网IP','{\"actualQty\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 14:33:29',10),(153,'盘点审批',2,'com.ruoyi.wms.controller.WmsStockTakeController.approve()','PUT',1,'admin','研发部门','/wms/stocktake/approve/2','127.0.0.1','内网IP','2 ',NULL,1,'盘点单状态不正确，无法审批','2026-07-14 14:33:50',6),(154,'盘点审批',2,'com.ruoyi.wms.controller.WmsStockTakeController.approve()','PUT',1,'admin','研发部门','/wms/stocktake/approve/2','127.0.0.1','内网IP','2 ',NULL,1,'盘点单状态不正确，无法审批','2026-07-14 15:20:04',3),(155,'提交盘点明细',2,'com.ruoyi.wms.controller.WmsStockTakeController.submitDetail()','PUT',1,'admin','研发部门','/wms/stocktake/submit/2/6','127.0.0.1','内网IP','{\"actualQty\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 15:20:11',5),(156,'提交盘点明细',2,'com.ruoyi.wms.controller.WmsStockTakeController.submitDetail()','PUT',1,'admin','研发部门','/wms/stocktake/submit/2/7','127.0.0.1','内网IP','{\"actualQty\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 15:20:16',5),(157,'提交盘点明细',2,'com.ruoyi.wms.controller.WmsStockTakeController.submitDetail()','PUT',1,'admin','研发部门','/wms/stocktake/submit/2/8','127.0.0.1','内网IP','{\"actualQty\":\"20\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 15:20:19',5),(158,'提交盘点明细',2,'com.ruoyi.wms.controller.WmsStockTakeController.submitDetail()','PUT',1,'admin','研发部门','/wms/stocktake/submit/2/9','127.0.0.1','内网IP','{\"actualQty\":\"5\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 15:20:21',9),(159,'提交盘点明细',2,'com.ruoyi.wms.controller.WmsStockTakeController.submitDetail()','PUT',1,'admin','研发部门','/wms/stocktake/submit/2/10','127.0.0.1','内网IP','{\"actualQty\":\"11\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 15:20:22',10),(160,'提交盘点明细',2,'com.ruoyi.wms.controller.WmsStockTakeController.submitDetail()','PUT',1,'admin','研发部门','/wms/stocktake/submit/1/1','127.0.0.1','内网IP','{\"actualQty\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 15:21:39',5),(161,'提交盘点明细',2,'com.ruoyi.wms.controller.WmsStockTakeController.submitDetail()','PUT',1,'admin','研发部门','/wms/stocktake/submit/1/2','127.0.0.1','内网IP','{\"actualQty\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 15:21:42',5),(162,'盘点审批',2,'com.ruoyi.wms.controller.WmsStockTakeController.approve()','PUT',1,'admin','研发部门','/wms/stocktake/approve/1','127.0.0.1','内网IP','1 ',NULL,1,'盘点单状态不正确，无法审批','2026-07-14 15:21:46',2),(163,'盘点审批',2,'com.ruoyi.wms.controller.WmsStockTakeController.approve()','PUT',1,'admin','研发部门','/wms/stocktake/approve/2','127.0.0.1','内网IP','2 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 15:22:43',16),(164,'提交盘点明细',2,'com.ruoyi.wms.controller.WmsStockTakeController.submitDetail()','PUT',1,'admin','研发部门','/wms/stocktake/submit/1/1','127.0.0.1','内网IP','{\"actualQty\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 15:35:54',37),(165,'提交盘点明细',2,'com.ruoyi.wms.controller.WmsStockTakeController.submitDetail()','PUT',1,'admin','研发部门','/wms/stocktake/submit/1/3','127.0.0.1','内网IP','{\"actualQty\":\"20\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 15:35:58',12),(166,'提交盘点明细',2,'com.ruoyi.wms.controller.WmsStockTakeController.submitDetail()','PUT',1,'admin','研发部门','/wms/stocktake/submit/1/4','127.0.0.1','内网IP','{\"actualQty\":\"5\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 15:36:06',13),(167,'提交盘点明细',2,'com.ruoyi.wms.controller.WmsStockTakeController.submitDetail()','PUT',1,'admin','研发部门','/wms/stocktake/submit/1/5','127.0.0.1','内网IP','{\"actualQty\":\"11\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 15:36:08',13),(168,'盘点管理',1,'com.ruoyi.wms.controller.WmsStockTakeController.add()','POST',1,'admin','研发部门','/wms/stocktake','127.0.0.1','内网IP','{\"areaId\":2,\"delFlag\":\"0\",\"params\":{},\"planDate\":\"2026-07-14\",\"status\":\"0\",\"takeId\":3,\"takeNo\":\"ST1784016628291\",\"takeType\":\"0\",\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 16:10:28',36),(169,'盘点管理',1,'com.ruoyi.wms.controller.WmsStockTakeController.add()','POST',1,'admin','研发部门','/wms/stocktake','127.0.0.1','内网IP','{\"areaId\":2,\"delFlag\":\"0\",\"params\":{},\"status\":\"0\",\"takeId\":4,\"takeNo\":\"ST1784016661126\",\"takeType\":\"1\",\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 16:11:01',21),(170,'开始盘点',2,'com.ruoyi.wms.controller.WmsStockTakeController.start()','PUT',1,'admin','研发部门','/wms/stocktake/start/4','127.0.0.1','内网IP','4 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 16:11:07',6),(171,'盘点作废',2,'com.ruoyi.wms.controller.WmsStockTakeController.voidTake()','PUT',1,'admin','研发部门','/wms/stocktake/void/4','127.0.0.1','内网IP','4 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 16:15:53',28),(172,'盘点管理',1,'com.ruoyi.wms.controller.WmsStockTakeController.add()','POST',1,'admin','研发部门','/wms/stocktake','127.0.0.1','内网IP','{\"areaId\":2,\"cycleNo\":1,\"delFlag\":\"0\",\"params\":{},\"planDate\":\"2026-07-14\",\"sampleRatio\":30,\"status\":\"0\",\"takeId\":5,\"takeNo\":\"ST1784017919963\",\"takeType\":\"1\",\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 16:32:00',229),(173,'开始盘点',2,'com.ruoyi.wms.controller.WmsStockTakeController.start()','PUT',1,'admin','研发部门','/wms/stocktake/start/5','127.0.0.1','内网IP','5 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 16:32:04',15),(174,'盘点管理',1,'com.ruoyi.wms.controller.WmsStockTakeController.add()','POST',1,'admin','研发部门','/wms/stocktake','127.0.0.1','内网IP','{\"cycleNo\":1,\"delFlag\":\"0\",\"params\":{},\"sampleRatio\":30,\"status\":\"0\",\"takeId\":6,\"takeNo\":\"ST1784017955433\",\"takeType\":\"2\",\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 16:32:35',16),(175,'开始盘点',2,'com.ruoyi.wms.controller.WmsStockTakeController.start()','PUT',1,'admin','研发部门','/wms/stocktake/start/6','127.0.0.1','内网IP','6 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 16:32:38',6),(176,'提交盘点明细',2,'com.ruoyi.wms.controller.WmsStockTakeController.submitDetail()','PUT',1,'admin','研发部门','/wms/stocktake/submit/6/23','127.0.0.1','内网IP','{\"actualQty\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 16:32:50',15),(177,'提交盘点明细',2,'com.ruoyi.wms.controller.WmsStockTakeController.submitDetail()','PUT',1,'admin','研发部门','/wms/stocktake/submit/6/24','127.0.0.1','内网IP','{\"actualQty\":\"11\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 16:32:51',7),(178,'盘点审批',2,'com.ruoyi.wms.controller.WmsStockTakeController.approve()','PUT',1,'admin','研发部门','/wms/stocktake/approve/6','127.0.0.1','内网IP','6 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 16:33:24',13),(179,'保存菜单排序',2,'com.ruoyi.web.controller.system.SysMenuController.updateSort()','PUT',1,'admin','研发部门','/system/menu/updateSort','127.0.0.1','内网IP','{\"menuIds\":\"2004,2005,2100\",\"orderNums\":\"5,6,4\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 16:54:02',20),(180,'移库管理',1,'com.ruoyi.wms.controller.WmsMoveOrderController.add()','POST',1,'admin','研发部门','/wms/move','127.0.0.1','内网IP','{\"batchNo\":\"0714002\",\"delFlag\":\"0\",\"fromLocationCode\":\"WH001A01L01\",\"fromLocationId\":3,\"materialCode\":\"MAT20250005\",\"materialId\":5,\"materialName\":\"智能手环 Pro\",\"moveId\":1,\"moveNo\":\"MV1784019694035\",\"moveQty\":1,\"params\":{},\"status\":\"0\",\"toLocationId\":5,\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 17:01:34',22),(181,'移库审批',2,'com.ruoyi.wms.controller.WmsMoveOrderController.approve()','PUT',1,'admin','研发部门','/wms/move/approve/1','127.0.0.1','内网IP','1 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 17:01:42',12),(182,'执行移库',2,'com.ruoyi.wms.controller.WmsMoveOrderController.execute()','PUT',1,'admin','研发部门','/wms/move/execute/1','127.0.0.1','内网IP','1 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 17:01:47',28),(183,'移库管理',1,'com.ruoyi.wms.controller.WmsMoveOrderController.add()','POST',1,'admin','研发部门','/wms/move','127.0.0.1','内网IP','{\"batchNo\":\"0714002\",\"delFlag\":\"0\",\"fromLocationCode\":\"WH001A02L01\",\"fromLocationId\":5,\"fromLocationName\":\"西区01\",\"materialCode\":\"MAT20250005\",\"materialId\":5,\"materialName\":\"智能手环 Pro\",\"moveId\":2,\"moveNo\":\"MV1784021042965\",\"moveQty\":1,\"params\":{},\"status\":\"0\",\"toLocationCode\":\"WH001A01L01\",\"toLocationId\":3,\"toLocationName\":\"东区001\",\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 17:24:03',165),(184,'移库作废',2,'com.ruoyi.wms.controller.WmsMoveOrderController.voidOrder()','PUT',1,'admin','研发部门','/wms/move/void/2','127.0.0.1','内网IP','2 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 17:24:10',16),(185,'移库管理',3,'com.ruoyi.wms.controller.WmsMoveOrderController.remove()','DELETE',1,'admin','研发部门','/wms/move/2','127.0.0.1','内网IP','[2] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-14 17:24:22',6),(186,'设备分类管理',1,'com.ruoyi.dms.controller.DmsEquipmentCategoryController.add()','POST',1,'admin','研发部门','/dms/category','127.0.0.1','内网IP','{\"ancestors\":\"0\",\"categoryCode\":\"h\",\"categoryId\":23,\"categoryLevel\":1,\"categoryName\":\"hhhhh\",\"delFlag\":\"0\",\"orderNum\":0,\"params\":{},\"parentId\":0,\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-15 14:48:42',31),(187,'设备分类管理',1,'com.ruoyi.dms.controller.DmsEquipmentCategoryController.add()','POST',1,'admin','研发部门','/dms/category','127.0.0.1','内网IP','{\"ancestors\":\"0,23\",\"categoryCode\":\"hh2\",\"categoryId\":24,\"categoryLevel\":2,\"categoryName\":\"h2\",\"delFlag\":\"0\",\"orderNum\":0,\"params\":{},\"parentId\":23,\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-15 14:48:53',5),(188,'设备分类管理',3,'com.ruoyi.dms.controller.DmsEquipmentCategoryController.remove()','DELETE',1,'admin','研发部门','/dms/category/24','127.0.0.1','内网IP','[24] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-15 14:48:56',16),(189,'设备分类管理',3,'com.ruoyi.dms.controller.DmsEquipmentCategoryController.remove()','DELETE',1,'admin','研发部门','/dms/category/23','127.0.0.1','内网IP','[23] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-15 14:48:58',10),(190,'设备台账管理',1,'com.ruoyi.dms.controller.DmsEquipmentController.add()','POST',1,'admin','研发部门','/dms/equipment','127.0.0.1','内网IP','{\"attachmentUrl\":\"/profile/upload/2026/07/15/Surface_Laptop_3_06_20260715152557A002.jpg,/profile/upload/2026/07/15/河南郑许轨道交通有限公司内网优化项目需求_20260715152606A003.xlsx,/profile/upload/2026/07/15/河南郑许轨道交通有限公司内网优化项目需求书_20260715152609A004.docx\",\"categoryId\":1,\"delFlag\":\"0\",\"deptId\":103,\"equipmentCode\":\"ed200001\",\"equipmentId\":1,\"equipmentImage\":\"/profile/upload/2026/07/15/Surface_Laptop_3_07_20260715152552A001.jpg\",\"equipmentName\":\"机床01\",\"equipmentStatus\":\"0\",\"installLocation\":\"3333\",\"manufacturer\":\"22\",\"model\":\"222\",\"params\":{},\"purchaseDate\":\"2026-07-01\",\"responsibleId\":1,\"serialNumber\":\"22\",\"status\":\"0\",\"supplier\":\"22\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-15 15:26:41',26),(191,'设备台账管理',2,'com.ruoyi.dms.controller.DmsEquipmentController.edit()','PUT',1,'admin','研发部门','/dms/equipment','127.0.0.1','内网IP','{\"attachmentUrl\":\"/profile/upload/2026/07/15/Surface_Laptop_3_06_20260715152557A002.jpg,/profile/upload/2026/07/15/河南郑许轨道交通有限公司内网优化项目需求_20260715152606A003.xlsx,/profile/upload/2026/07/15/河南郑许轨道交通有限公司内网优化项目需求书_20260715152609A004.docx,/profile/upload/2026/07/15/26527000000076026536_1(1)_20260715155515A005.pdf\",\"categoryId\":1,\"categoryName\":\"金属切削机床\",\"createTime\":\"2026-07-15 15:26:41\",\"delFlag\":\"0\",\"deptId\":103,\"deptName\":\"研发部门\",\"equipmentCode\":\"ed200001\",\"equipmentId\":1,\"equipmentImage\":\"/profile/upload/2026/07/15/Surface_Laptop_3_07_20260715152552A001.jpg\",\"equipmentName\":\"机床01\",\"equipmentStatus\":\"0\",\"installLocation\":\"3333\",\"manufacturer\":\"22\",\"model\":\"222\",\"params\":{},\"purchaseDate\":\"2026-07-01\",\"responsibleId\":1,\"responsibleName\":\"若依\",\"serialNumber\":\"22\",\"status\":\"0\",\"supplier\":\"22\",\"updateBy\":\"\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-15 15:55:17',12),(192,'备件管理',1,'com.ruoyi.dms.controller.DmsSparePartController.add()','POST',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"currentStock\":5,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ20260715001\",\"partId\":1,\"partName\":\"bbbb\",\"partType\":\"1\",\"specModel\":\"ddd\",\"status\":\"0\",\"stockMax\":7,\"stockMin\":2,\"storageLocation\":\"xxxx\",\"supplier\":\"惠州电池有限公司\",\"unit\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-15 16:55:39',20),(193,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2026-07-15 12:42:45\",\"icon\":\"shopping\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":3030,\"menuName\":\"备件管理\",\"menuType\":\"M\",\"orderNum\":3,\"params\":{},\"parentId\":3000,\"path\":\"sparepart\",\"perms\":\"dms:sparepart:list\",\"query\":\"\",\"routeName\":\"DmsSparePart\",\"status\":\"0\",\"visible\":\"0\"} ','{\"msg\":\"修改菜单\'备件管理\'失败，路由名称或地址已存在\",\"code\":500}',0,NULL,'2026-07-15 18:06:06',27),(194,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2026-07-15 12:42:45\",\"icon\":\"shopping\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":3030,\"menuName\":\"备件管理\",\"menuType\":\"M\",\"orderNum\":3,\"params\":{},\"parentId\":3000,\"path\":\"sparepart\",\"perms\":\"dms:sparepart:list\",\"query\":\"\",\"routeName\":\"DmsSparePart\",\"status\":\"0\",\"visible\":\"0\"} ','{\"msg\":\"修改菜单\'备件管理\'失败，路由名称或地址已存在\",\"code\":500}',0,NULL,'2026-07-15 18:06:13',9),(195,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"component\":\"dms/partin/index\",\"createTime\":\"2026-07-15 17:04:25\",\"icon\":\"log-in\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":3106,\"menuName\":\"备件入库\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":3030,\"path\":\"partin\",\"perms\":\"dms:partin:list\",\"query\":\"\",\"routeName\":\"DmsPartIn\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-15 18:45:16',34),(196,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"component\":\"dms/partout/index\",\"createTime\":\"2026-07-15 17:04:25\",\"icon\":\"log-out\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":3107,\"menuName\":\"备件出库\",\"menuType\":\"C\",\"orderNum\":2,\"params\":{},\"parentId\":3030,\"path\":\"partout\",\"perms\":\"dms:partout:list\",\"query\":\"\",\"routeName\":\"DmsPartOut\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-15 18:45:29',12),(197,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":10.00,\"beforeStock\":5.00,\"documentCode\":\"BJIN20260715001\",\"moveType\":\"0\",\"params\":{},\"partCode\":\"BJ20260715001\",\"partId\":1,\"partName\":\"bbbb\",\"quantity\":5,\"recordId\":1,\"sourceType\":\"0\",\"supplierOrDept\":\"惠州电池有限公司\",\"targetType\":\"4\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-15 19:10:26',177),(198,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":15.00,\"beforeStock\":10.00,\"documentCode\":\"BJIN20260715002\",\"moveType\":\"0\",\"params\":{},\"partCode\":\"BJ20260715001\",\"partId\":1,\"partName\":\"bbbb\",\"quantity\":5,\"recordId\":2,\"sourceType\":\"1\",\"supplierOrDept\":\"惠州电池有限公司\",\"targetType\":\"4\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-15 19:10:45',14),(199,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":16.00,\"beforeStock\":15.00,\"documentCode\":\"BJIN20260715003\",\"moveType\":\"0\",\"params\":{},\"partCode\":\"BJ20260715001\",\"partId\":1,\"partName\":\"bbbb\",\"quantity\":1,\"recordId\":3,\"sourceType\":\"0\",\"supplierOrDept\":\"惠州电池有限公司\",\"targetType\":\"4\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-15 19:12:15',15),(200,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":20.00,\"beforeStock\":16.00,\"documentCode\":\"BJIN20260716001\",\"moveType\":\"0\",\"operateDate\":\"2026-07-16\",\"operatorName\":\"若依\",\"params\":{},\"partCode\":\"BJ20260715001\",\"partId\":1,\"partName\":\"bbbb\",\"quantity\":4,\"recordId\":4,\"sourceType\":\"0\",\"storageLocation\":\"2222222222\",\"supplierOrDept\":\"惠州电池有限公司\",\"targetType\":\"4\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 07:56:02',33),(201,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":25.00,\"beforeStock\":20.00,\"documentCode\":\"BJIN20260716002\",\"moveType\":\"0\",\"operateDate\":\"2026-07-16\",\"operatorName\":\"若依\",\"params\":{},\"partCode\":\"BJ20260715001\",\"partId\":1,\"partName\":\"bbbb\",\"quantity\":5,\"recordId\":5,\"sourceType\":\"0\",\"storageLocation\":\"3333\",\"supplierOrDept\":\"惠州电池有限公司\",\"targetType\":\"4\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 07:58:40',14),(202,'备件管理',1,'com.ruoyi.dms.controller.DmsSparePartController.add()','POST',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ20260716001\",\"partId\":2,\"partName\":\"cccc\",\"partType\":\"4\",\"specModel\":\"cc\",\"status\":\"0\",\"stockMax\":20,\"stockMin\":7,\"supplier\":\"上海金属材料有限公司\",\"unit\":\"1\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:00:50',15),(203,'备件管理',1,'com.ruoyi.dms.controller.DmsSparePartController.add()','POST',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ20260716002\",\"partId\":3,\"partName\":\"ddd\",\"partType\":\"7\",\"specModel\":\"ddd\",\"status\":\"0\",\"stockMax\":20,\"stockMin\":5,\"supplier\":\"深圳塑胶科技有限公司\",\"unit\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:01:48',4),(204,'字典数据',1,'com.ruoyi.web.controller.system.SysDictDataController.add()','POST',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"把\",\"dictSort\":0,\"dictType\":\"wms_unit\",\"dictValue\":\"5\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:02:53',20),(205,'字典数据',2,'com.ruoyi.web.controller.system.SysDictDataController.edit()','PUT',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"createTime\":\"2026-07-16 08:02:53\",\"default\":false,\"dictCode\":333,\"dictLabel\":\"把\",\"dictSort\":0,\"dictType\":\"wms_unit\",\"dictValue\":\"5\",\"isDefault\":\"N\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:03:11',13),(206,'字典数据',2,'com.ruoyi.web.controller.system.SysDictDataController.edit()','PUT',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"createTime\":\"2026-07-16 08:02:53\",\"default\":false,\"dictCode\":333,\"dictLabel\":\"把\",\"dictSort\":0,\"dictType\":\"wms_unit\",\"dictValue\":\"b\",\"isDefault\":\"N\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:03:16',8),(207,'字典数据',2,'com.ruoyi.web.controller.system.SysDictDataController.edit()','PUT',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"createTime\":\"2026-07-16 08:02:53\",\"default\":false,\"dictCode\":333,\"dictLabel\":\"把\",\"dictSort\":0,\"dictType\":\"wms_unit\",\"dictValue\":\"5\",\"isDefault\":\"N\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:03:21',14),(208,'字典数据',2,'com.ruoyi.web.controller.system.SysDictDataController.edit()','PUT',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"createTime\":\"2026-07-16 08:02:53\",\"default\":false,\"dictCode\":333,\"dictLabel\":\"把\",\"dictSort\":8,\"dictType\":\"wms_unit\",\"dictValue\":\"6\",\"isDefault\":\"N\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:04:38',7),(209,'字典数据',1,'com.ruoyi.web.controller.system.SysDictDataController.add()','POST',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"千米\",\"dictSort\":0,\"dictType\":\"wms_unit\",\"dictValue\":\"7\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:16:45',206),(210,'字典数据',2,'com.ruoyi.web.controller.system.SysDictDataController.edit()','PUT',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"createTime\":\"2026-07-16 08:16:45\",\"default\":false,\"dictCode\":334,\"dictLabel\":\"把\",\"dictSort\":0,\"dictType\":\"wms_unit\",\"dictValue\":\"7\",\"isDefault\":\"N\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:17:50',15),(211,'字典数据',2,'com.ruoyi.web.controller.system.SysDictDataController.edit()','PUT',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"createTime\":\"2026-07-16 08:16:45\",\"default\":false,\"dictCode\":334,\"dictLabel\":\"千米\",\"dictSort\":0,\"dictType\":\"wms_unit\",\"dictValue\":\"7\",\"isDefault\":\"N\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:17:56',12),(212,'字典数据',1,'com.ruoyi.web.controller.system.SysDictDataController.add()','POST',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"千米\",\"dictSort\":0,\"dictType\":\"wms_unit\",\"dictValue\":\"8\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:18:03',12),(213,'字典数据',2,'com.ruoyi.web.controller.system.SysDictDataController.edit()','PUT',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"createTime\":\"2026-07-16 08:18:03\",\"default\":false,\"dictCode\":335,\"dictLabel\":\"厘米\",\"dictSort\":0,\"dictType\":\"wms_unit\",\"dictValue\":\"8\",\"isDefault\":\"N\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:18:19',14),(214,'字典数据',2,'com.ruoyi.web.controller.system.SysDictDataController.edit()','PUT',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"createTime\":\"2026-07-16 08:16:45\",\"default\":false,\"dictCode\":334,\"dictLabel\":\"千米\",\"dictSort\":0,\"dictType\":\"wms_unit\",\"dictValue\":\"7\",\"isDefault\":\"N\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:22:43',78),(215,'字典数据',2,'com.ruoyi.web.controller.system.SysDictDataController.edit()','PUT',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"createTime\":\"2026-07-16 08:16:45\",\"default\":false,\"dictCode\":334,\"dictLabel\":\"千米\",\"dictSort\":6,\"dictType\":\"wms_unit\",\"dictValue\":\"7\",\"isDefault\":\"N\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:23:04',12),(216,'字典数据',2,'com.ruoyi.web.controller.system.SysDictDataController.edit()','PUT',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"createTime\":\"2026-07-16 08:18:03\",\"default\":false,\"dictCode\":335,\"dictLabel\":\"厘米\",\"dictSort\":5,\"dictType\":\"wms_unit\",\"dictValue\":\"8\",\"isDefault\":\"N\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:23:09',12),(217,'字典数据',2,'com.ruoyi.web.controller.system.SysDictDataController.edit()','PUT',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"createTime\":\"2026-07-16 08:02:53\",\"default\":false,\"dictCode\":333,\"dictLabel\":\"把\",\"dictSort\":0,\"dictType\":\"wms_unit\",\"dictValue\":\"6\",\"isDefault\":\"N\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:23:19',9),(218,'备件管理',5,'com.ruoyi.dms.controller.DmsSparePartController.export()','POST',1,'admin','研发部门','/dms/sparepart/export','127.0.0.1','内网IP','{\"pageSize\":\"10\",\"pageNum\":\"1\"}',NULL,0,NULL,'2026-07-16 08:25:31',865),(219,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":6,\"beforeStock\":0,\"documentCode\":\"BJIN20260716003\",\"moveType\":\"0\",\"operateDate\":\"2026-07-16\",\"operatorName\":\"若依\",\"params\":{},\"partCode\":\"BJ20260716002\",\"partId\":3,\"partName\":\"ddd\",\"quantity\":6,\"recordId\":6,\"sourceType\":\"0\",\"storageLocation\":\"备件库\",\"supplierOrDept\":\"深圳塑胶科技有限公司\",\"targetType\":\"4\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:30:30',213),(220,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":2,\"beforeStock\":0,\"documentCode\":\"BJIN20260716004\",\"moveType\":\"0\",\"operateDate\":\"2026-07-16\",\"operatorName\":\"若依\",\"params\":{},\"partCode\":\"BJ20260716001\",\"partId\":2,\"partName\":\"cccc\",\"quantity\":2,\"recordId\":7,\"sourceType\":\"1\",\"storageLocation\":\"备件库\",\"supplierOrDept\":\"上海金属材料有限公司\",\"targetType\":\"4\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:31:39',13),(221,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":0.00,\"beforeStock\":2.00,\"documentCode\":\"BJOUT20260716001\",\"moveType\":\"1\",\"operateDate\":\"2026-07-16\",\"operatorName\":\"若依\",\"params\":{},\"partCode\":\"BJ20260716001\",\"partId\":2,\"partName\":\"cccc\",\"quantity\":2,\"receiver\":\"若依\",\"recordId\":8,\"sourceType\":\"1\",\"supplierOrDept\":\"若依科技\",\"targetType\":\"1\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:32:21',13),(222,'设备台账管理',5,'com.ruoyi.dms.controller.DmsEquipmentController.export()','POST',1,'admin','研发部门','/dms/equipment/export','127.0.0.1','内网IP','{\"pageSize\":\"10\",\"pageNum\":\"1\"}',NULL,0,NULL,'2026-07-16 08:39:09',774),(223,'备件管理',5,'com.ruoyi.dms.controller.DmsSparePartController.export()','POST',1,'admin','研发部门','/dms/sparepart/export','127.0.0.1','内网IP','{\"pageSize\":\"10\",\"pageNum\":\"1\"}',NULL,0,NULL,'2026-07-16 08:39:34',95),(224,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":10.00,\"beforeStock\":0.00,\"documentCode\":\"BJIN20260716005\",\"moveType\":\"0\",\"operateDate\":\"2026-07-16\",\"operatorName\":\"若依\",\"params\":{},\"partCode\":\"BJ20260716001\",\"partId\":2,\"partName\":\"cccc\",\"quantity\":10,\"recordId\":9,\"sourceType\":\"0\",\"storageLocation\":\"备件库\",\"supplierOrDept\":\"上海金属材料有限公司\",\"targetType\":\"4\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:41:34',8),(225,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":16.00,\"beforeStock\":10.00,\"documentCode\":\"BJIN20260716006\",\"moveType\":\"0\",\"operateDate\":\"2026-07-16\",\"operatorName\":\"若依\",\"params\":{},\"partCode\":\"BJ20260716001\",\"partId\":2,\"partName\":\"cccc\",\"quantity\":6,\"recordId\":10,\"sourceType\":\"1\",\"storageLocation\":\"备件库\",\"supplierOrDept\":\"上海金属材料有限公司\",\"targetType\":\"4\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:49:41',39),(226,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":15.00,\"beforeStock\":16.00,\"documentCode\":\"BJOUT20260716002\",\"moveType\":\"1\",\"operateDate\":\"2026-07-16\",\"operatorName\":\"若依\",\"params\":{},\"partCode\":\"BJ20260716001\",\"partId\":2,\"partName\":\"cccc\",\"quantity\":1,\"receiver\":\"若依\",\"recordId\":11,\"sourceType\":\"1\",\"supplierOrDept\":\"研发部门\",\"targetType\":\"2\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 08:50:13',12),(227,'备件入库',5,'com.ruoyi.dms.controller.DmsSparePartController.partInExport()','POST',1,'admin','研发部门','/dms/sparepart/partin/export','127.0.0.1','内网IP','{\"pageSize\":\"10\",\"pageNum\":\"1\"}',NULL,0,NULL,'2026-07-16 09:39:46',620),(228,'备件出库',5,'com.ruoyi.dms.controller.DmsSparePartController.partOutExport()','POST',1,'admin','研发部门','/dms/sparepart/partout/export','127.0.0.1','内网IP','{\"pageSize\":\"10\",\"pageNum\":\"1\"}',NULL,0,NULL,'2026-07-16 09:40:01',85),(229,'备件管理',5,'com.ruoyi.dms.controller.DmsSparePartController.export()','POST',1,'admin','研发部门','/dms/sparepart/export','127.0.0.1','内网IP','{\"pageSize\":\"10\",\"pageNum\":\"1\"}',NULL,0,NULL,'2026-07-16 09:40:29',94),(230,'库存流水报表',5,'com.ruoyi.dms.controller.DmsSparePartController.partStockExport()','POST',1,'admin','研发部门','/dms/sparepart/partstock/export','127.0.0.1','内网IP','{\"pageSize\":\"10\",\"pageNum\":\"1\"}',NULL,0,NULL,'2026-07-16 09:40:43',101),(231,'入库管理',1,'com.ruoyi.wms.controller.WmsInboundOrderController.add()','POST',1,'admin','研发部门','/wms/inbound','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"detailList\":[{\"delFlag\":\"0\",\"materialId\":8,\"orderId\":6,\"params\":{},\"planQty\":5,\"putawayQty\":0,\"receivedQty\":0,\"unitPrice\":0}],\"inboundDate\":\"2026-07-01\",\"orderId\":6,\"orderNo\":\"IN1784166865317\",\"orderType\":\"0\",\"params\":{},\"status\":\"0\",\"supplierId\":4,\"totalQty\":5,\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 09:54:25',203),(232,'入库提交',2,'com.ruoyi.wms.controller.WmsInboundOrderController.submit()','PUT',1,'admin','研发部门','/wms/inbound/submit/6','127.0.0.1','内网IP','6 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 09:54:41',28),(233,'入库管理',3,'com.ruoyi.wms.controller.WmsInboundOrderController.remove()','DELETE',1,'admin','研发部门','/wms/inbound/6','127.0.0.1','内网IP','[6] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 09:56:29',12),(234,'入库管理',3,'com.ruoyi.wms.controller.WmsInboundOrderController.remove()','DELETE',1,'admin','研发部门','/wms/inbound/5','127.0.0.1','内网IP','[5] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 09:57:10',5),(235,'出库管理',3,'com.ruoyi.wms.controller.WmsOutboundOrderController.remove()','DELETE',1,'admin','研发部门','/wms/outbound/2','127.0.0.1','内网IP','[2] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 09:58:21',4),(236,'库存预警',5,'com.ruoyi.wms.controller.WmsStockAlertController.export()','POST',1,'admin','研发部门','/wms/stockAlert/export','127.0.0.1','内网IP','{\"pageSize\":\"10\",\"pageNum\":\"1\"}',NULL,0,NULL,'2026-07-16 10:25:54',1181),(237,'库存流水',5,'com.ruoyi.wms.controller.WmsInventoryLogController.export()','POST',1,'admin','研发部门','/wms/log/export','127.0.0.1','内网IP','{\"pageSize\":\"10\",\"pageNum\":\"1\"}',NULL,0,NULL,'2026-07-16 10:26:00',179),(238,'库存查询',5,'com.ruoyi.wms.controller.WmsInventoryController.export()','POST',1,'admin','研发部门','/wms/inventory/export','127.0.0.1','内网IP','{\"pageSize\":\"10\",\"pageNum\":\"1\"}',NULL,0,NULL,'2026-07-16 10:26:04',125),(239,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":4.00,\"beforeStock\":6.00,\"documentCode\":\"BJOUT20260716003\",\"moveType\":\"1\",\"operateDate\":\"2026-07-16\",\"operatorName\":\"若依\",\"params\":{},\"partCode\":\"BJ20260716002\",\"partId\":3,\"partName\":\"ddd\",\"quantity\":2,\"receiver\":\"若依\",\"recordId\":12,\"sourceType\":\"1\",\"supplierOrDept\":\"研发部门\",\"targetType\":\"2\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 10:34:19',17),(240,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":26.00,\"beforeStock\":25.00,\"documentCode\":\"BJIN20260716007\",\"moveType\":\"0\",\"operateDate\":\"2026-07-16\",\"operatorName\":\"若依\",\"params\":{},\"partCode\":\"BJ20260715001\",\"partId\":1,\"partName\":\"bbbb\",\"quantity\":1,\"recordId\":13,\"sourceType\":\"1\",\"storageLocation\":\"备件库\",\"supplierOrDept\":\"惠州电池有限公司\",\"targetType\":\"4\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 10:35:34',7),(241,'入库管理',3,'com.ruoyi.wms.controller.WmsInboundOrderController.remove()','DELETE',1,'admin','研发部门','/wms/inbound/4','127.0.0.1','内网IP','[4] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 12:00:53',25),(242,'出库管理',3,'com.ruoyi.wms.controller.WmsOutboundOrderController.remove()','DELETE',1,'admin','研发部门','/wms/outbound/1','127.0.0.1','内网IP','[1] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 12:00:54',6),(243,'盘点管理',3,'com.ruoyi.wms.controller.WmsStockTakeController.remove()','DELETE',1,'admin','研发部门','/wms/stocktake/6','127.0.0.1','内网IP','[6] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 12:00:54',4),(244,'库存查询',3,'com.ruoyi.wms.controller.WmsInventoryController.remove()','DELETE',1,'admin','研发部门','/wms/inventory/6','127.0.0.1','内网IP','[6] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 12:01:29',7),(245,'库存流水',3,'com.ruoyi.wms.controller.WmsInventoryLogController.remove()','DELETE',1,'admin','研发部门','/wms/log/11','127.0.0.1','内网IP','[11] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 12:01:29',10),(246,'库存预警',3,'com.ruoyi.wms.controller.WmsStockAlertController.remove()','POST',1,'admin','研发部门','/wms/stockAlert/remove','127.0.0.1','内网IP','{\"alertType\":\"low_stock\",\"materialId\":1,\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 12:01:29',26),(247,'备件出入库记录',3,'com.ruoyi.dms.controller.DmsSparePartController.removeRecord()','DELETE',1,'admin','研发部门','/dms/sparepart/record/13','127.0.0.1','内网IP','[13] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 12:01:29',6),(248,'库存预警',3,'com.ruoyi.dms.controller.DmsSparePartController.removeAlert()','DELETE',1,'admin','研发部门','/dms/sparepart/alert/3','127.0.0.1','内网IP','3 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 12:01:29',6),(249,'备件管理',3,'com.ruoyi.dms.controller.DmsSparePartController.remove()','DELETE',1,'admin','研发部门','/dms/sparepart/8','127.0.0.1','内网IP','[8] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 12:01:29',5),(250,'备件管理',1,'com.ruoyi.dms.controller.DmsSparePartController.add()','POST',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ20260716003\",\"partId\":9,\"partName\":\"电机皮带\",\"partType\":\"5\",\"specModel\":\"22222\",\"status\":\"0\",\"stockMax\":10,\"stockMin\":6,\"supplier\":\"东莞电子科技有限公司\",\"unit\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 15:22:52',17),(251,'用户管理',2,'com.ruoyi.web.controller.system.SysUserController.edit()','PUT',1,'admin','研发部门','/system/user','127.0.0.1','内网IP','{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2026-07-13 11:25:52\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,101\",\"children\":[],\"deptId\":105,\"deptName\":\"测试部门\",\"leader\":\"若依\",\"orderNum\":3,\"params\":{},\"parentId\":101,\"status\":\"0\"},\"deptId\":105,\"loginDate\":\"2026-07-13 11:25:52\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"李华\",\"params\":{},\"postIds\":[2],\"pwdUpdateDate\":\"2026-07-13 11:25:52\",\"remark\":\"测试员\",\"roleIds\":[2],\"roles\":[{\"admin\":false,\"dataScope\":\"2\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":2,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"roleSort\":2,\"status\":\"0\"}],\"sex\":\"1\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":2,\"userName\":\"ry\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 15:34:27',18),(252,'用户管理',1,'com.ruoyi.web.controller.system.SysUserController.add()','POST',1,'admin','研发部门','/system/user','127.0.0.1','内网IP','{\"admin\":false,\"createBy\":\"admin\",\"deptId\":103,\"nickName\":\"张涛\",\"params\":{},\"postIds\":[],\"roleIds\":[2],\"sex\":\"0\",\"status\":\"0\",\"userId\":100,\"userName\":\"zt\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 15:35:28',72),(253,'个人信息',2,'com.ruoyi.web.controller.system.SysProfileController.updateProfile()','PUT',1,'admin','研发部门','/system/user/profile','127.0.0.1','内网IP','{\"admin\":false,\"nickName\":\"系统管理员\",\"params\":{},\"sex\":\"1\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 15:35:51',7),(254,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"equipmentCode\":\"EQ005\",\"equipmentId\":6,\"equipmentName\":\"数控加工中心\",\"faultDescription\":\"ttttttttttttttttt\",\"orderId\":1,\"orderNo\":\"WO202607160001\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"rating\":0,\"reportTime\":\"2026-07-16 15:38:25\",\"reporterId\":2} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 15:38:35',17),(255,'工单派工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.dispatch()','PUT',1,'admin','研发部门','/dms/workorder/dispatch','127.0.0.1','内网IP','{\"assignTime\":\"2026-07-16 15:59:00\",\"assigneeId\":2,\"assigneeName\":\"\",\"assignerId\":2,\"assignerName\":\"\",\"equipmentName\":\"数控加工中心\",\"orderId\":1,\"orderNo\":\"WO202607160001\",\"orderStatus\":\"1\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 15:59:00',7),(256,'工单接单',2,'com.ruoyi.dms.controller.DmsWorkOrderController.accept()','PUT',1,'admin','研发部门','/dms/workorder/accept/1','127.0.0.1','内网IP','1 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:00:28',5),(257,'工单开始处理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.startProcess()','PUT',1,'admin','研发部门','/dms/workorder/process/1','127.0.0.1','内网IP','1 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:00:33',9),(258,'工单完工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.complete()','PUT',1,'admin','研发部门','/dms/workorder/complete','127.0.0.1','内网IP','{\"completeRemark\":\"ddd\",\"completeTime\":\"2026-07-16 16:01:59\",\"downtimeDuration\":555,\"faultCause\":\"3\",\"orderId\":1,\"orderNo\":\"WO202607160001\",\"orderStatus\":\"4\",\"params\":{},\"repairMeasure\":\"33333333333333\",\"sparePartsUsed\":\"33333333333333333333333333333333\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:02:00',9),(259,'工单驳回',2,'com.ruoyi.dms.controller.DmsWorkOrderController.reject()','PUT',1,'admin','研发部门','/dms/workorder/reject','127.0.0.1','内网IP','{\"orderId\":1,\"orderNo\":\"WO202607160001\",\"orderStatus\":\"7\",\"params\":{},\"verifyOpinion\":\"211111\",\"verifyTime\":\"2026-07-16 16:02:07\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:02:07',9),(260,'工单开始处理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.startProcess()','PUT',1,'admin','研发部门','/dms/workorder/process/1','127.0.0.1','内网IP','1 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:02:11',11),(261,'工单完工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.complete()','PUT',1,'admin','研发部门','/dms/workorder/complete','127.0.0.1','内网IP','{\"completeRemark\":\"888\",\"completeTime\":\"2026-07-16 16:02:21\",\"downtimeDuration\":666,\"faultCause\":\"6\",\"orderId\":1,\"orderNo\":\"WO202607160001\",\"orderStatus\":\"4\",\"params\":{},\"repairMeasure\":\"66667\",\"sparePartsUsed\":\"7777778\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:02:21',11),(262,'工单验收',2,'com.ruoyi.dms.controller.DmsWorkOrderController.verify()','PUT',1,'admin','研发部门','/dms/workorder/verify','127.0.0.1','内网IP','{\"orderId\":1,\"orderNo\":\"WO202607160001\",\"orderStatus\":\"5\",\"params\":{},\"rating\":4,\"verifierId\":2,\"verifierName\":\"\",\"verifyOpinion\":\"5555555555555555555\",\"verifyTime\":\"2026-07-16 16:02:30\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:02:30',10),(263,'工单管理',5,'com.ruoyi.dms.controller.DmsWorkOrderController.export()','POST',1,'admin','研发部门','/dms/workorder/export','127.0.0.1','内网IP','{\"pageSize\":\"10\",\"pageNum\":\"1\"}',NULL,0,NULL,'2026-07-16 16:03:10',534),(264,'工单管理',3,'com.ruoyi.dms.controller.DmsWorkOrderController.remove()','DELETE',1,'admin','研发部门','/dms/workorder/1','127.0.0.1','内网IP','[1] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:03:29',7),(265,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"equipmentCode\":\"EQ005\",\"equipmentId\":6,\"equipmentName\":\"数控加工中心\",\"faultDescription\":\"ddddddddddddddddddd\",\"orderNo\":\"WO202607160001\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"rating\":0,\"reportTime\":\"2026-07-16 00:00:00\",\"reporterId\":2,\"reporterName\":\"李华\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time, fault_cause, repair_measure,             spare_parts_used, downtime_duration, complete_time, complete_remark,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\n; Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'','2026-07-16 16:04:09',142),(266,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"equipmentCode\":\"EQ005\",\"equipmentId\":6,\"equipmentName\":\"数控加工中心\",\"faultDescription\":\"ddddddddddddddddddd\",\"orderNo\":\"WO202607160001\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"rating\":0,\"reportTime\":\"2026-07-16 00:00:00\",\"reporterId\":2,\"reporterName\":\"李华\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time, fault_cause, repair_measure,             spare_parts_used, downtime_duration, complete_time, complete_remark,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\n; Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'','2026-07-16 16:04:17',6),(267,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"equipmentCode\":\"EQ005\",\"equipmentId\":6,\"equipmentName\":\"数控加工中心\",\"faultDescription\":\"ddddddddddddddddddd\",\"orderNo\":\"WO202607160001\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"rating\":0,\"reportTime\":\"2026-07-16 00:00:00\",\"reporterId\":2,\"reporterName\":\"李华\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time, fault_cause, repair_measure,             spare_parts_used, downtime_duration, complete_time, complete_remark,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\n; Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'','2026-07-16 16:04:26',6),(268,'PM维护计划',1,'com.ruoyi.dms.controller.DmsPmPlanController.add()','POST',1,'admin','研发部门','/dms/pmplan','127.0.0.1','内网IP','{\"advanceDays\":1,\"autoAssign\":\"0\",\"categoryId\":1,\"cycleUnit\":\"天\",\"cycleValue\":30,\"delFlag\":\"0\",\"equipmentId\":6,\"estimatedHours\":4,\"nextExecuteTime\":\"2026-08-15 16:08:50\",\"params\":{},\"planId\":1,\"planName\":\"ddddddddd111\",\"sparePartList\":\"3eeeeeeeeeeeeeeeeeee\",\"status\":\"0\",\"taskList\":\"fdddddddddddd\",\"triggerType\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:08:50',14),(269,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/1','127.0.0.1','内网IP','1 ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time, fault_cause, repair_measure,             spare_parts_used, downtime_duration, complete_time, complete_remark,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\n; Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'','2026-07-16 16:08:59',14),(270,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"equipmentCode\":\"EQ005\",\"equipmentId\":6,\"equipmentName\":\"数控加工中心\",\"faultDescription\":\"无法启动\",\"orderNo\":\"WO202607160001\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"rating\":0,\"reportTime\":\"2026-07-16 16:19:34\",\"reporterId\":2,\"reporterName\":\"李华\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time, fault_cause, repair_measure,             spare_parts_used, downtime_duration, complete_time, complete_remark,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\n; Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'','2026-07-16 16:19:34',9),(271,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"equipmentCode\":\"EQ005\",\"equipmentId\":6,\"equipmentName\":\"数控加工中心\",\"faultDescription\":\"无法启动!\",\"orderNo\":\"WO202607160001\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"rating\":0,\"reportTime\":\"2026-07-16 16:19:55\",\"reporterId\":2,\"reporterName\":\"李华\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time, fault_cause, repair_measure,             spare_parts_used, downtime_duration, complete_time, complete_remark,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\n; Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'','2026-07-16 16:19:55',5),(272,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"equipmentCode\":\"EQ005\",\"equipmentId\":6,\"equipmentName\":\"数控加工中心\",\"faultDescription\":\"无法启动!\",\"orderNo\":\"WO202607160001\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"rating\":0,\"reportTime\":\"2026-07-16 16:20:10\",\"reporterId\":2,\"reporterName\":\"李华\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time, fault_cause, repair_measure,             spare_parts_used, downtime_duration, complete_time, complete_remark,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\n; Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'','2026-07-16 16:20:17',9),(273,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"equipmentCode\":\"EQ003\",\"equipmentId\":4,\"equipmentName\":\"摇臂钻床\",\"faultDescription\":\"dddddddddddddddddd\",\"orderNo\":\"WO202607160001\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"rating\":0,\"reportTime\":\"2026-07-16 16:20:53\",\"reporterId\":2,\"reporterName\":\"李华\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time, fault_cause, repair_measure,             spare_parts_used, downtime_duration, complete_time, complete_remark,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'\n; Duplicate entry \'WO202607160001\' for key \'dms_work_order.uk_order_no\'','2026-07-16 16:20:59',4),(274,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"delFlag\":\"0\",\"equipmentCode\":\"EQ004\",\"equipmentId\":5,\"equipmentName\":\"平面磨床\",\"faultDescription\":\"111\",\"orderId\":10,\"orderNo\":\"WO202607160002\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"rating\":0,\"reportTime\":\"2026-07-16 16:29:30\",\"reporterId\":100,\"reporterName\":\"张涛\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:29:35',188),(275,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/291','127.0.0.1','内网IP','[291] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:37:34',19),(276,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/313','127.0.0.1','内网IP','[313] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:37:36',18),(277,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/314','127.0.0.1','内网IP','[314] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:37:38',9),(278,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/292','127.0.0.1','内网IP','[292] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:37:40',11),(279,'工单撤销',2,'com.ruoyi.dms.controller.DmsWorkOrderController.cancel()','PUT',1,'admin','研发部门','/dms/workorder/cancel/10','127.0.0.1','内网IP','10 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:40:24',14),(280,'PM维护计划',1,'com.ruoyi.dms.controller.DmsPmPlanController.add()','POST',1,'admin','研发部门','/dms/pmplan','127.0.0.1','内网IP','{\"advanceDays\":3,\"autoAssign\":\"0\",\"categoryId\":1,\"cycleUnit\":\"天\",\"cycleValue\":30,\"delFlag\":\"0\",\"equipmentId\":6,\"nextExecuteTime\":\"2026-08-15 16:43:21\",\"params\":{},\"planId\":2,\"planName\":\"ddddddddddd\",\"status\":\"0\",\"taskList\":\"ddddddddddddd\",\"triggerType\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:43:21',16),(281,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/2','127.0.0.1','内网IP','2 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:43:34',31),(282,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/2','127.0.0.1','内网IP','2 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:43:46',14),(283,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/2','127.0.0.1','内网IP','2 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:46:00',10),(284,'工单派工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.dispatch()','PUT',1,'admin','研发部门','/dms/workorder/dispatch','127.0.0.1','内网IP','{\"assignTime\":\"2026-07-16 16:46:33\",\"assigneeId\":2,\"assigneeName\":\"李华\",\"assignerName\":\"\",\"equipmentName\":\"数控加工中心\",\"orderId\":13,\"orderNo\":\"WO202607160005\",\"orderStatus\":\"1\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:46:33',10),(285,'工单派工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.dispatch()','PUT',1,'admin','研发部门','/dms/workorder/dispatch','127.0.0.1','内网IP','{\"assigneeId\":100,\"assigneeName\":\"张涛\",\"assignerName\":\"\",\"equipmentName\":\"数控加工中心\",\"orderId\":13,\"orderNo\":\"WO202607160005\",\"params\":{}} ',NULL,1,'当前状态不允许派工','2026-07-16 16:46:42',4),(286,'工单派工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.dispatch()','PUT',1,'admin','研发部门','/dms/workorder/dispatch','127.0.0.1','内网IP','{\"assigneeId\":100,\"assigneeName\":\"张涛\",\"assignerName\":\"\",\"equipmentName\":\"数控加工中心\",\"orderId\":13,\"orderNo\":\"WO202607160005\",\"params\":{}} ',NULL,1,'当前状态不允许派工','2026-07-16 16:46:46',3),(287,'工单派工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.dispatch()','PUT',1,'admin','研发部门','/dms/workorder/dispatch','127.0.0.1','内网IP','{\"assigneeId\":100,\"assigneeName\":\"张涛\",\"assignerName\":\"\",\"equipmentName\":\"数控加工中心\",\"orderId\":13,\"orderNo\":\"WO202607160005\",\"params\":{}} ',NULL,1,'当前状态不允许派工','2026-07-16 16:46:57',2),(288,'工单派工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.dispatch()','PUT',1,'admin','研发部门','/dms/workorder/dispatch','127.0.0.1','内网IP','{\"assigneeId\":100,\"assigneeName\":\"张涛\",\"assignerName\":\"\",\"equipmentName\":\"数控加工中心\",\"orderId\":13,\"orderNo\":\"WO202607160005\",\"params\":{}} ',NULL,1,'当前状态不允许派工','2026-07-16 16:47:09',3),(289,'工单派工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.dispatch()','PUT',1,'admin','研发部门','/dms/workorder/dispatch','127.0.0.1','内网IP','{\"assignTime\":\"2026-07-16 16:47:15\",\"assigneeId\":2,\"assigneeName\":\"李华\",\"assignerName\":\"\",\"equipmentName\":\"数控加工中心\",\"orderId\":12,\"orderNo\":\"WO202607160004\",\"orderStatus\":\"1\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:47:15',11),(290,'工单派工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.dispatch()','PUT',1,'admin','研发部门','/dms/workorder/dispatch','127.0.0.1','内网IP','{\"assigneeId\":100,\"assigneeName\":\"张涛\",\"assignerName\":\"\",\"equipmentName\":\"数控加工中心\",\"orderId\":12,\"orderNo\":\"WO202607160004\",\"params\":{}} ',NULL,1,'当前状态不允许派工','2026-07-16 16:47:19',2),(291,'工单接单',2,'com.ruoyi.dms.controller.DmsWorkOrderController.accept()','PUT',1,'admin','研发部门','/dms/workorder/accept/13','127.0.0.1','内网IP','13 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:49:21',10),(292,'工单开始处理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.startProcess()','PUT',1,'admin','研发部门','/dms/workorder/process/13','127.0.0.1','内网IP','13 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:49:26',11),(293,'工单改派',2,'com.ruoyi.dms.controller.DmsWorkOrderController.reassign()','PUT',1,'admin','研发部门','/dms/workorder/reassign','127.0.0.1','内网IP','{\"assignTime\":\"2026-07-16 16:50:40\",\"assigneeId\":100,\"assigneeName\":\"张涛\",\"assignerName\":\"\",\"equipmentName\":\"数控加工中心\",\"orderId\":12,\"orderNo\":\"WO202607160004\",\"orderStatus\":\"1\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:50:40',9),(294,'工单改派',2,'com.ruoyi.dms.controller.DmsWorkOrderController.reassign()','PUT',1,'admin','研发部门','/dms/workorder/reassign','127.0.0.1','内网IP','{\"assignTime\":\"2026-07-16 16:50:45\",\"assigneeId\":2,\"assigneeName\":\"李华\",\"assignerName\":\"\",\"equipmentName\":\"数控加工中心\",\"orderId\":12,\"orderNo\":\"WO202607160004\",\"orderStatus\":\"1\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:50:45',6),(295,'工单改派',2,'com.ruoyi.dms.controller.DmsWorkOrderController.reassign()','PUT',1,'admin','研发部门','/dms/workorder/reassign','127.0.0.1','内网IP','{\"assignTime\":\"2026-07-16 16:50:53\",\"assigneeId\":100,\"assigneeName\":\"张涛\",\"assignerName\":\"\",\"equipmentName\":\"数控加工中心\",\"orderId\":12,\"orderNo\":\"WO202607160004\",\"orderStatus\":\"1\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 16:50:53',9),(296,'工单完工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.complete()','PUT',1,'admin','研发部门','/dms/workorder/complete','127.0.0.1','内网IP','{\"completeRemark\":\"\",\"completeTime\":\"2026-07-16 21:41:23\",\"downtimeDuration\":0,\"faultCause\":\"22\",\"orderId\":13,\"orderNo\":\"WO202607160005\",\"orderStatus\":\"4\",\"params\":{},\"repairMeasure\":\"33\",\"sparePartsUsed\":\"电机皮带×1，液压油泵×1，液压油泵×1，cccc×1，cccc×1\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:41:23',10),(297,'工单驳回',2,'com.ruoyi.dms.controller.DmsWorkOrderController.reject()','PUT',1,'admin','研发部门','/dms/workorder/reject','127.0.0.1','内网IP','{\"orderId\":13,\"orderNo\":\"WO202607160005\",\"orderStatus\":\"7\",\"params\":{},\"verifyOpinion\":\"333\",\"verifyTime\":\"2026-07-16 21:41:28\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:41:28',10),(298,'工单开始处理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.startProcess()','PUT',1,'admin','研发部门','/dms/workorder/process/13','127.0.0.1','内网IP','13 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:41:31',5),(299,'采集配置',1,'com.ruoyi.dms.controller.DmsDataCollectionConfigController.add()','POST',1,'admin','研发部门','/dms/data/config','127.0.0.1','内网IP','{\"collectCount\":\"1\",\"collectHours\":\"1\",\"collectMode\":\"0\",\"collectStatus\":\"1\",\"configId\":1,\"delFlag\":\"0\",\"equipmentId\":6,\"equipmentName\":\"数控加工中心\",\"param1AlarmMax\":5,\"param1AlarmMin\":5,\"params\":{},\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:42:58',12),(300,'采集配置',2,'com.ruoyi.dms.controller.DmsDataCollectionConfigController.edit()','PUT',1,'admin','研发部门','/dms/data/config','127.0.0.1','内网IP','{\"collectCount\":\"1\",\"collectHours\":\"1\",\"collectMode\":\"0\",\"collectStatus\":\"1\",\"configId\":1,\"createTime\":\"2026-07-16 21:42:58\",\"delFlag\":\"0\",\"equipmentId\":6,\"equipmentName\":\"数控加工中心\",\"param1AlarmMax\":90,\"param1AlarmMin\":40,\"param1Name\":\"温度\",\"param1Unit\":\"℃\",\"params\":{},\"status\":\"0\",\"updateBy\":\"\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:44:07',9),(301,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/317','127.0.0.1','内网IP','[317] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:44:49',6),(302,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/273','127.0.0.1','内网IP','[273] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:44:51',10),(303,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/316','127.0.0.1','内网IP','[316] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:44:53',7),(304,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/294','127.0.0.1','内网IP','[294] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:44:55',10),(305,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/315','127.0.0.1','内网IP','[315] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:44:59',12),(306,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/293','127.0.0.1','内网IP','[293] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:45:01',10),(307,'数据记录',1,'com.ruoyi.dms.controller.DmsDataRecordController.add()','POST',1,'admin','研发部门','/dms/data/record','127.0.0.1','内网IP','{\"collectMode\":\"0\",\"collectTime\":\"2026-07-16 21:45:29\",\"equipmentId\":6,\"equipmentName\":\"数控加工中心\",\"param1Value\":7,\"params\":{},\"productCount\":6,\"recordId\":1,\"runHours\":0,\"runStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:45:35',12),(308,'备件管理',2,'com.ruoyi.dms.controller.DmsSparePartController.edit()','PUT',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"applicableCategory\":\"加工中心\",\"createBy\":\"admin\",\"currentStock\":8,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ004\",\"partId\":7,\"partName\":\"液压油泵\",\"partType\":\"液压配件\",\"safetyStock\":3,\"specModel\":\"VP-20\",\"status\":\"0\",\"stockMax\":20,\"stockMin\":2,\"storageLocation\":\"仓库C-01货架\",\"supplier\":\"若依科技\",\"unit\":\"台\",\"updateBy\":\"\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:49:17',16),(309,'字典数据',1,'com.ruoyi.web.controller.system.SysDictDataController.add()','POST',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"套\",\"dictSort\":0,\"dictType\":\"wms_unit\",\"dictValue\":\"9\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:50:21',19),(310,'字典数据',1,'com.ruoyi.web.controller.system.SysDictDataController.add()','POST',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"台\",\"dictSort\":0,\"dictType\":\"wms_unit\",\"dictValue\":\"10\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:50:47',5),(311,'备件管理',2,'com.ruoyi.dms.controller.DmsSparePartController.edit()','PUT',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"applicableCategory\":\"加工中心\",\"createBy\":\"admin\",\"currentStock\":8,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ004\",\"partId\":7,\"partName\":\"液压油泵\",\"partType\":\"液压配件\",\"safetyStock\":3,\"specModel\":\"VP-20\",\"status\":\"0\",\"stockMax\":20,\"stockMin\":2,\"storageLocation\":\"仓库C-01货架\",\"supplier\":\"若依科技\",\"unit\":\"10\",\"updateBy\":\"\",\"updateTime\":\"2026-07-16 21:49:17\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:51:05',7),(312,'字典数据',1,'com.ruoyi.web.controller.system.SysDictDataController.add()','POST',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"条\",\"dictSort\":0,\"dictType\":\"wms_unit\",\"dictValue\":\"11\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:51:15',5),(313,'备件管理',2,'com.ruoyi.dms.controller.DmsSparePartController.edit()','PUT',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"applicableCategory\":\"车床,钻床,磨床\",\"createBy\":\"admin\",\"currentStock\":45,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ003\",\"partId\":6,\"partName\":\"电机皮带\",\"partType\":\"传动配件\",\"safetyStock\":20,\"specModel\":\"B-1800\",\"status\":\"0\",\"stockMax\":100,\"stockMin\":10,\"storageLocation\":\"仓库B-01货架\",\"supplier\":\"若依科技\",\"unit\":\"11\",\"updateBy\":\"\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:51:34',7),(314,'备件管理',2,'com.ruoyi.dms.controller.DmsSparePartController.edit()','PUT',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"applicableCategory\":\"车床,铣床\",\"createBy\":\"admin\",\"currentStock\":12,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ002\",\"partId\":5,\"partName\":\"丝杠螺母\",\"partType\":\"机械配件\",\"safetyStock\":5,\"specModel\":\"SFU2005-4\",\"status\":\"0\",\"stockMax\":30,\"stockMin\":3,\"storageLocation\":\"仓库A-02货架\",\"supplier\":\"若依科技\",\"unit\":\"9\",\"updateBy\":\"\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:51:42',9),(315,'备件管理',2,'com.ruoyi.dms.controller.DmsSparePartController.edit()','PUT',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"applicableCategory\":\"车床,铣床,加工中心\",\"createBy\":\"admin\",\"currentStock\":25,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ001\",\"partId\":4,\"partName\":\"主轴轴承\",\"partType\":\"机械配件\",\"safetyStock\":10,\"specModel\":\"6205-2RS\",\"status\":\"0\",\"stockMax\":50,\"stockMin\":5,\"storageLocation\":\"仓库A-01货架\",\"supplier\":\"若依科技\",\"unit\":\"0\",\"updateBy\":\"\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:51:47',7),(316,'备件管理',2,'com.ruoyi.dms.controller.DmsSparePartController.edit()','PUT',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"createTime\":\"2026-07-16 15:22:52\",\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ20260716003\",\"partId\":9,\"partName\":\"电机皮带\",\"partType\":\"5\",\"specModel\":\"22222\",\"status\":\"0\",\"stockMax\":10,\"stockMin\":6,\"supplier\":\"东莞电子科技有限公司\",\"unit\":\"0\",\"updateBy\":\"\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:52:00',4),(317,'备件管理',2,'com.ruoyi.dms.controller.DmsSparePartController.edit()','PUT',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"applicableCategory\":\"加工中心\",\"createBy\":\"admin\",\"currentStock\":8,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ004\",\"partId\":7,\"partName\":\"液压油泵\",\"partType\":\"6\",\"safetyStock\":3,\"specModel\":\"VP-20\",\"status\":\"0\",\"stockMax\":20,\"stockMin\":2,\"storageLocation\":\"仓库C-01货架\",\"supplier\":\"若依科技\",\"unit\":\"10\",\"updateBy\":\"\",\"updateTime\":\"2026-07-16 21:51:05\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:52:11',8),(318,'备件管理',2,'com.ruoyi.dms.controller.DmsSparePartController.edit()','PUT',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"applicableCategory\":\"车床,钻床,磨床\",\"createBy\":\"admin\",\"currentStock\":45,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ003\",\"partId\":6,\"partName\":\"电机皮带\",\"partType\":\"9\",\"safetyStock\":20,\"specModel\":\"B-1800\",\"status\":\"0\",\"stockMax\":100,\"stockMin\":10,\"storageLocation\":\"仓库B-01货架\",\"supplier\":\"若依科技\",\"unit\":\"11\",\"updateBy\":\"\",\"updateTime\":\"2026-07-16 21:51:34\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:52:21',4),(319,'字典数据',1,'com.ruoyi.web.controller.system.SysDictDataController.add()','POST',1,'admin','研发部门','/system/dict/data','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"机械配件\",\"dictSort\":0,\"dictType\":\"dms_part_type\",\"dictValue\":\"10\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:53:06',10),(320,'备件管理',2,'com.ruoyi.dms.controller.DmsSparePartController.edit()','PUT',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"applicableCategory\":\"车床,铣床\",\"createBy\":\"admin\",\"currentStock\":12,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ002\",\"partId\":5,\"partName\":\"丝杠螺母\",\"partType\":\"10\",\"safetyStock\":5,\"specModel\":\"SFU2005-4\",\"status\":\"0\",\"stockMax\":30,\"stockMin\":3,\"storageLocation\":\"仓库A-02货架\",\"supplier\":\"若依科技\",\"unit\":\"9\",\"updateBy\":\"\",\"updateTime\":\"2026-07-16 21:51:42\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:53:24',6),(321,'备件管理',2,'com.ruoyi.dms.controller.DmsSparePartController.edit()','PUT',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"applicableCategory\":\"车床,铣床,加工中心\",\"createBy\":\"admin\",\"currentStock\":25,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ001\",\"partId\":4,\"partName\":\"主轴轴承\",\"partType\":\"机械配件\",\"safetyStock\":10,\"specModel\":\"6205-2RS\",\"status\":\"0\",\"stockMax\":50,\"stockMin\":5,\"storageLocation\":\"仓库A-01货架\",\"supplier\":\"若依科技\",\"unit\":\"0\",\"updateBy\":\"\",\"updateTime\":\"2026-07-16 21:51:47\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:53:28',7),(322,'备件管理',2,'com.ruoyi.dms.controller.DmsSparePartController.edit()','PUT',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"applicableCategory\":\"车床,铣床,加工中心\",\"createBy\":\"admin\",\"currentStock\":25,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ001\",\"partId\":4,\"partName\":\"主轴轴承\",\"partType\":\"10\",\"safetyStock\":10,\"specModel\":\"6205-2RS\",\"status\":\"0\",\"stockMax\":50,\"stockMin\":5,\"storageLocation\":\"仓库A-01货架\",\"supplier\":\"若依科技\",\"unit\":\"0\",\"updateBy\":\"\",\"updateTime\":\"2026-07-16 21:53:28\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:53:32',8),(323,'工单完工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.complete()','PUT',1,'admin','研发部门','/dms/workorder/complete','127.0.0.1','内网IP','{\"completeRemark\":\"\",\"completeTime\":\"2026-07-16 21:55:33\",\"downtimeDuration\":0,\"faultCause\":\"dd\",\"orderId\":13,\"orderNo\":\"WO202607160005\",\"orderStatus\":\"4\",\"params\":{},\"repairMeasure\":\"dd\",\"sparePartsUsed\":\"\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:55:33',9),(324,'工单驳回',2,'com.ruoyi.dms.controller.DmsWorkOrderController.reject()','PUT',1,'admin','研发部门','/dms/workorder/reject','127.0.0.1','内网IP','{\"orderId\":13,\"orderNo\":\"WO202607160005\",\"orderStatus\":\"7\",\"params\":{},\"verifyOpinion\":\"dd\",\"verifyTime\":\"2026-07-16 21:55:38\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:55:38',9),(325,'工单开始处理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.startProcess()','PUT',1,'admin','研发部门','/dms/workorder/process/13','127.0.0.1','内网IP','13 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 21:55:41',4),(326,'工单完工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.complete()','PUT',1,'admin','研发部门','/dms/workorder/complete','127.0.0.1','内网IP','{\"completeRemark\":\"\",\"completeTime\":\"2026-07-16 22:04:02\",\"downtimeDuration\":0,\"faultCause\":\"22\",\"orderId\":13,\"orderNo\":\"WO202607160005\",\"orderStatus\":\"4\",\"params\":{},\"repairMeasure\":\"333\",\"sparePartsUsed\":\"电机皮带×7\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:04:02',8),(327,'工单驳回',2,'com.ruoyi.dms.controller.DmsWorkOrderController.reject()','PUT',1,'admin','研发部门','/dms/workorder/reject','127.0.0.1','内网IP','{\"orderId\":13,\"orderNo\":\"WO202607160005\",\"orderStatus\":\"7\",\"params\":{},\"verifyOpinion\":\"22\",\"verifyTime\":\"2026-07-16 22:04:06\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:04:06',5),(328,'工单开始处理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.startProcess()','PUT',1,'admin','研发部门','/dms/workorder/process/13','127.0.0.1','内网IP','13 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:04:08',4),(329,'备件管理',2,'com.ruoyi.dms.controller.DmsSparePartController.edit()','PUT',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"applicableCategory\":\"加工中心\",\"createBy\":\"admin\",\"currentStock\":8,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ004\",\"partId\":7,\"partName\":\"液压油泵\",\"partType\":\"5\",\"safetyStock\":3,\"specModel\":\"VP-20\",\"status\":\"0\",\"stockMax\":20,\"stockMin\":2,\"storageLocation\":\"仓库C-01货架\",\"supplier\":\"若依科技\",\"unit\":\"10\",\"updateBy\":\"\",\"updateTime\":\"2026-07-16 21:52:11\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:05:57',7),(330,'备件管理',1,'com.ruoyi.dms.controller.DmsSparePartController.add()','POST',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"currentStock\":0,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ20260716004\",\"partId\":10,\"partName\":\"测试备件11\",\"partType\":\"2\",\"specModel\":\"xxx\",\"status\":\"0\",\"stockMax\":10,\"stockMin\":5,\"supplier\":\"东莞电子科技有限公司\",\"unit\":\"9\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:09:37',4),(331,'工单完工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.complete()','PUT',1,'admin','研发部门','/dms/workorder/complete','127.0.0.1','内网IP','{\"completeRemark\":\"\",\"completeTime\":\"2026-07-16 22:10:38\",\"downtimeDuration\":10,\"faultCause\":\"2222\",\"orderId\":13,\"orderNo\":\"WO202607160005\",\"orderStatus\":\"4\",\"params\":{},\"repairMeasure\":\"222\",\"sparePartsUsed\":\"cccc×15\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:10:38',10),(332,'工单验收',2,'com.ruoyi.dms.controller.DmsWorkOrderController.verify()','PUT',1,'admin','研发部门','/dms/workorder/verify','127.0.0.1','内网IP','{\"orderId\":13,\"orderNo\":\"WO202607160005\",\"orderStatus\":\"5\",\"params\":{},\"rating\":5,\"verifierId\":2,\"verifierName\":\"李华\",\"verifyOpinion\":\"\",\"verifyTime\":\"2026-07-16 22:10:43\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:10:43',9),(333,'工单管理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.edit()','PUT',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"arriveTime\":\"2026-07-16 16:49:21\",\"assignTime\":\"2026-07-16 16:46:34\",\"assigneeId\":2,\"assigneeName\":\"李华\",\"assignerName\":\"\",\"completeRemark\":\"\",\"completeTime\":\"2026-07-16 22:10:38\",\"createTime\":\"2026-07-16 16:46:00\",\"delFlag\":\"0\",\"downtimeDuration\":10,\"equipmentCode\":\"EQ005\",\"equipmentId\":6,\"equipmentName\":\"数控加工中心\",\"faultCause\":\"2222\",\"faultDescription\":\"PM计划维护：ddddddddddd\\n任务清单：ddddddddddddd\",\"orderId\":13,\"orderNo\":\"WO202607160005\",\"orderStatus\":\"5\",\"orderType\":\"1\",\"params\":{},\"priority\":\"3\",\"rating\":5,\"remark\":\"由PM计划[ddddddddddd]自动生成\",\"repairMeasure\":\"222\",\"reportTime\":\"2026-07-16 16:46:01\",\"sparePartsUsed\":\"cccc×15\",\"updateBy\":\"admin\",\"updateTime\":\"2026-07-16 22:10:43\",\"verifierId\":2,\"verifierName\":\"李华\",\"verifyOpinion\":\"\",\"verifyTime\":\"2026-07-16 22:10:43\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:10:57',4),(334,'工单接单',2,'com.ruoyi.dms.controller.DmsWorkOrderController.accept()','PUT',1,'admin','研发部门','/dms/workorder/accept/12','127.0.0.1','内网IP','12 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:13:40',8),(335,'工单开始处理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.startProcess()','PUT',1,'admin','研发部门','/dms/workorder/process/12','127.0.0.1','内网IP','12 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:13:43',5),(336,'工单管理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.edit()','PUT',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"arriveTime\":\"2026-07-16 16:49:21\",\"assignTime\":\"2026-07-16 16:46:34\",\"assigneeId\":2,\"assigneeName\":\"李华\",\"assignerName\":\"\",\"completeRemark\":\"\",\"completeTime\":\"2026-07-16 22:10:38\",\"createTime\":\"2026-07-16 16:46:00\",\"delFlag\":\"0\",\"downtimeDuration\":10,\"equipmentCode\":\"EQ005\",\"equipmentId\":6,\"equipmentName\":\"数控加工中心\",\"faultCause\":\"2222\",\"faultDescription\":\"PM计划维护：ddddddddddd\\n任务清单：ddddddddddddd\",\"orderId\":13,\"orderNo\":\"WO202607160005\",\"orderStatus\":\"4\",\"orderType\":\"1\",\"params\":{},\"priority\":\"3\",\"rating\":5,\"remark\":\"由PM计划[ddddddddddd]自动生成\",\"repairMeasure\":\"222\",\"reportTime\":\"2026-07-16 16:46:01\",\"sparePartsUsed\":\"cccc×15\",\"updateBy\":\"admin\",\"updateTime\":\"2026-07-16 22:10:57\",\"verifierId\":2,\"verifierName\":\"李华\",\"verifyOpinion\":\"\",\"verifyTime\":\"2026-07-16 22:10:43\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:17:22',3),(337,'工单验收',2,'com.ruoyi.dms.controller.DmsWorkOrderController.verify()','PUT',1,'admin','研发部门','/dms/workorder/verify','127.0.0.1','内网IP','{\"orderId\":13,\"orderNo\":\"WO202607160005\",\"orderStatus\":\"5\",\"params\":{},\"rating\":5,\"sparePartsUsed\":\"cccc×15\",\"verifierId\":2,\"verifierName\":\"李华\",\"verifyOpinion\":\"rrrrrrrrrrrrr\",\"verifyTime\":\"2026-07-16 22:18:38\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:18:39',8),(338,'工单管理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.edit()','PUT',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"arriveTime\":\"2026-07-16 16:49:21\",\"assignTime\":\"2026-07-16 16:46:34\",\"assigneeId\":2,\"assigneeName\":\"李华\",\"assignerName\":\"\",\"completeRemark\":\"\",\"completeTime\":\"2026-07-16 22:10:38\",\"createTime\":\"2026-07-16 16:46:00\",\"delFlag\":\"0\",\"downtimeDuration\":10,\"equipmentCode\":\"EQ005\",\"equipmentId\":6,\"equipmentName\":\"数控加工中心\",\"faultCause\":\"2222\",\"faultDescription\":\"PM计划维护：ddddddddddd\\n任务清单：ddddddddddddd\",\"orderId\":13,\"orderNo\":\"WO202607160005\",\"orderStatus\":\"4\",\"orderType\":\"1\",\"params\":{},\"priority\":\"3\",\"rating\":5,\"remark\":\"由PM计划[ddddddddddd]自动生成\",\"repairMeasure\":\"222\",\"reportTime\":\"2026-07-16 16:46:01\",\"sparePartsUsed\":\"cccc×15\",\"updateBy\":\"admin\",\"updateTime\":\"2026-07-16 22:18:38\",\"verifierId\":2,\"verifierName\":\"李华\",\"verifyOpinion\":\"rrrrrrrrrrrrr\",\"verifyTime\":\"2026-07-16 22:18:39\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:20:46',8),(339,'工单验收',2,'com.ruoyi.dms.controller.DmsWorkOrderController.verify()','PUT',1,'admin','研发部门','/dms/workorder/verify','127.0.0.1','内网IP','{\"orderId\":13,\"orderNo\":\"WO202607160005\",\"orderStatus\":\"5\",\"params\":{},\"rating\":5,\"sparePartsUsed\":\"cccc×15\",\"verifierId\":2,\"verifierName\":\"李华\",\"verifyOpinion\":\"eeee\",\"verifyTime\":\"2026-07-16 22:45:12\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:45:12',238),(340,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/2','127.0.0.1','内网IP','2 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 22:56:21',273),(341,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/2','127.0.0.1','内网IP','2 ',NULL,1,'该计划已生成未完结的工单「WO202607160006」（当前状态：新建），请先完成或撤销该工单后再生成新工单','2026-07-16 22:56:23',14),(342,'PM自动生成',1,'com.ruoyi.dms.controller.DmsPmPlanController.autoGenerate()','POST',1,'admin','研发部门','/dms/pmplan/autoGenerate','127.0.0.1','内网IP','','{\"msg\":\"共自动生成 0 个工单\",\"code\":200}',0,NULL,'2026-07-16 23:35:40',25),(343,'PM维护计划',1,'com.ruoyi.dms.controller.DmsPmPlanController.add()','POST',1,'admin','研发部门','/dms/pmplan','127.0.0.1','内网IP','{\"advanceDays\":3,\"assigneeId\":2,\"assigneeName\":\"李华\",\"autoAssign\":\"1\",\"categoryId\":5,\"cycleUnit\":\"天\",\"cycleValue\":30,\"delFlag\":\"0\",\"equipmentId\":5,\"estimatedHours\":5,\"nextExecuteTime\":\"2026-08-15 23:38:11\",\"params\":{},\"planId\":3,\"planName\":\"xxxxxxxxxxxx1\",\"sparePartList\":\"ssssss\",\"status\":\"0\",\"taskList\":\"[\\\"xxxxxxx1\\\",\\\"xxxxxxx2\\\"]\",\"triggerType\":\"1\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 23:38:11',28),(344,'PM维护计划',1,'com.ruoyi.dms.controller.DmsPmPlanController.add()','POST',1,'admin','研发部门','/dms/pmplan','127.0.0.1','内网IP','{\"advanceDays\":3,\"autoAssign\":\"0\",\"categoryId\":3,\"cycleUnit\":\"天\",\"cycleValue\":30,\"delFlag\":\"0\",\"equipmentId\":3,\"nextExecuteTime\":\"2026-08-15 23:44:12\",\"params\":{},\"planId\":4,\"planName\":\"4\",\"sparePartList\":\"[{\\\"partId\\\":3,\\\"partName\\\":\\\"ddd\\\",\\\"qty\\\":1,\\\"unit\\\":\\\"0\\\"}]\",\"status\":\"0\",\"taskList\":\"[\\\"清洁滤网。\\\"]\",\"triggerType\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-16 23:44:12',4),(345,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/4','127.0.0.1','内网IP','4 ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLException: Column count doesn\'t match value count at row 1\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time, fault_cause, repair_measure,             spare_parts_used, downtime_duration, complete_time, complete_remark,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLException: Column count doesn\'t match value count at row 1\n; bad SQL grammar []','2026-07-16 23:45:08',240),(346,'PM自动生成',1,'com.ruoyi.dms.controller.DmsPmPlanController.autoGenerate()','POST',1,'admin','研发部门','/dms/pmplan/autoGenerate','127.0.0.1','内网IP','','{\"msg\":\"共自动生成 0 个工单\",\"code\":200}',0,NULL,'2026-07-16 23:45:16',3),(347,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/1','127.0.0.1','内网IP','1 ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLException: Column count doesn\'t match value count at row 1\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time, fault_cause, repair_measure,             spare_parts_used, downtime_duration, complete_time, complete_remark,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLException: Column count doesn\'t match value count at row 1\n; bad SQL grammar []','2026-07-16 23:45:20',23),(348,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/2','127.0.0.1','内网IP','2 ',NULL,1,'该计划已生成未完结的工单「WO202607160006」（当前状态：新建），请先完成或撤销该工单后再生成新工单','2026-07-16 23:45:23',12),(349,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/4','127.0.0.1','内网IP','4 ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLException: Column count doesn\'t match value count at row 1\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time, fault_cause, repair_measure,             spare_parts_used, downtime_duration, complete_time, complete_remark,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLException: Column count doesn\'t match value count at row 1\n; bad SQL grammar []','2026-07-16 23:50:53',11),(350,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/3','127.0.0.1','内网IP','3 ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLException: Column count doesn\'t match value count at row 1\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time, fault_cause, repair_measure,             spare_parts_used, downtime_duration, complete_time, complete_remark,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLException: Column count doesn\'t match value count at row 1\n; bad SQL grammar []','2026-07-16 23:50:59',14),(351,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/3','127.0.0.1','内网IP','3 ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLException: Column count doesn\'t match value count at row 1\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time, fault_cause, repair_measure,             spare_parts_used, downtime_duration, complete_time, complete_remark,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLException: Column count doesn\'t match value count at row 1\n; bad SQL grammar []','2026-07-16 23:51:15',9),(352,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/4','127.0.0.1','内网IP','4 ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLException: Column count doesn\'t match value count at row 1\r\n### The error may exist in URL [jar:nested:/E:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-admin/target/ruoyi-admin.jar/!BOOT-INF/lib/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time, fault_cause, repair_measure,             spare_parts_used, downtime_duration, complete_time, complete_remark,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLException: Column count doesn\'t match value count at row 1\n; bad SQL grammar []','2026-07-16 23:57:21',7),(353,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/4','127.0.0.1','内网IP','4 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 08:02:04',54),(354,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/4','127.0.0.1','内网IP','4 ',NULL,1,'该计划已生成未完结的工单「WO202607170007」（当前状态：新建），请先完成或撤销该工单后再生成新工单','2026-07-17 08:02:09',10),(355,'工单派工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.dispatch()','PUT',1,'admin','研发部门','/dms/workorder/dispatch','127.0.0.1','内网IP','{\"assignTime\":\"2026-07-17 08:02:30\",\"assigneeId\":2,\"assigneeName\":\"李华\",\"assignerId\":2,\"assignerName\":\"李华\",\"equipmentName\":\"立式铣床\",\"orderId\":15,\"orderNo\":\"WO202607170007\",\"orderStatus\":\"1\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 08:02:30',20),(356,'工单接单',2,'com.ruoyi.dms.controller.DmsWorkOrderController.accept()','PUT',1,'admin','研发部门','/dms/workorder/accept/15','127.0.0.1','内网IP','15 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 08:02:33',11),(357,'工单开始处理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.startProcess()','PUT',1,'admin','研发部门','/dms/workorder/process/15','127.0.0.1','内网IP','15 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 08:03:00',10),(358,'工单完工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.complete()','PUT',1,'admin','研发部门','/dms/workorder/complete','127.0.0.1','内网IP','{\"completeRemark\":\"\",\"completeTime\":\"2026-07-17 08:03:21\",\"deviationReason\":\"\",\"downtimeDuration\":1,\"faultCause\":\"ddd\",\"orderId\":15,\"orderNo\":\"WO202607170007\",\"orderStatus\":\"4\",\"orderType\":\"1\",\"params\":{},\"repairMeasure\":\"ssssssssssssssssss\",\"sparePartsUsed\":\"\",\"taskChecklist\":\"[{\\\"text\\\":\\\"清洁滤网。\\\",\\\"done\\\":true}]\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 08:03:21',12),(359,'工单验收',2,'com.ruoyi.dms.controller.DmsWorkOrderController.verify()','PUT',1,'admin','研发部门','/dms/workorder/verify','127.0.0.1','内网IP','{\"orderId\":15,\"orderNo\":\"WO202607170007\",\"orderStatus\":\"5\",\"params\":{},\"rating\":5,\"sparePartsUsed\":\"\",\"verifierId\":2,\"verifierName\":\"李华\",\"verifyOpinion\":\"\",\"verifyTime\":\"2026-07-17 08:03:30\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 08:03:30',10),(360,'工单派工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.dispatch()','PUT',1,'admin','研发部门','/dms/workorder/dispatch','127.0.0.1','内网IP','{\"assignTime\":\"2026-07-17 09:16:13\",\"assigneeId\":2,\"assigneeName\":\"李华\",\"assignerId\":100,\"assignerName\":\"张涛\",\"equipmentName\":\"数控加工中心\",\"orderId\":11,\"orderNo\":\"WO202607160003\",\"orderStatus\":\"1\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 09:16:13',44),(361,'工单接单',2,'com.ruoyi.dms.controller.DmsWorkOrderController.accept()','PUT',1,'admin','研发部门','/dms/workorder/accept/11','127.0.0.1','内网IP','11 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 09:16:16',14),(362,'工单开始处理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.startProcess()','PUT',1,'admin','研发部门','/dms/workorder/process/11','127.0.0.1','内网IP','11 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 09:16:18',13),(363,'工单完工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.complete()','PUT',1,'admin','研发部门','/dms/workorder/complete','127.0.0.1','内网IP','{\"completeRemark\":\"ttttttttttttttttt\",\"completeTime\":\"2026-07-17 09:16:50\",\"deviationReason\":\"ttttttttttttttt\",\"downtimeDuration\":2,\"faultCause\":\"tttttttttttttttttttttttttttt\",\"orderId\":11,\"orderNo\":\"WO202607160003\",\"orderStatus\":\"4\",\"orderType\":\"1\",\"params\":{},\"repairMeasure\":\"ttttttttttttttt\",\"sparePartsUsed\":\"液压油泵×1\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 09:16:50',13),(364,'工单验收',2,'com.ruoyi.dms.controller.DmsWorkOrderController.verify()','PUT',1,'admin','研发部门','/dms/workorder/verify','127.0.0.1','内网IP','{\"orderId\":11,\"orderNo\":\"WO202607160003\",\"orderStatus\":\"5\",\"params\":{},\"rating\":5,\"sparePartsUsed\":\"液压油泵×1\",\"verifierId\":2,\"verifierName\":\"李华\",\"verifyOpinion\":\"tttttttttt\",\"verifyTime\":\"2026-07-17 09:16:56\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 09:16:56',25),(365,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"delFlag\":\"0\",\"equipmentCode\":\"EQ004\",\"equipmentId\":5,\"equipmentName\":\"平面磨床\",\"faultDescription\":\"平面磨床设备今天没法正常使用。\",\"orderId\":16,\"orderNo\":\"WO202607170008\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"rating\":0,\"reportTime\":\"2026-08-07 00:00:00\",\"reporterId\":2,\"reporterName\":\"李华\",\"slaEscalated\":\"0\",\"slaProcessDeadline\":\"2026-08-08 00:00:00\",\"slaResponseDeadline\":\"2026-08-07 04:00:00\",\"slaTimeoutStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 09:20:13',13),(366,'工单派工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.dispatch()','PUT',1,'admin','研发部门','/dms/workorder/dispatch','127.0.0.1','内网IP','{\"assignTime\":\"2026-07-17 09:20:53\",\"assigneeId\":100,\"assigneeName\":\"张涛\",\"assignerId\":100,\"assignerName\":\"张涛\",\"equipmentName\":\"平面磨床\",\"orderId\":16,\"orderNo\":\"WO202607170008\",\"orderStatus\":\"1\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 09:20:53',8),(367,'工单改派',2,'com.ruoyi.dms.controller.DmsWorkOrderController.reassign()','PUT',1,'admin','研发部门','/dms/workorder/reassign','127.0.0.1','内网IP','{\"assignTime\":\"2026-07-17 09:21:01\",\"assigneeId\":2,\"assigneeName\":\"李华\",\"assignerName\":\"\",\"equipmentName\":\"平面磨床\",\"orderId\":16,\"orderNo\":\"WO202607170008\",\"orderStatus\":\"1\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 09:21:01',12),(368,'工单接单',2,'com.ruoyi.dms.controller.DmsWorkOrderController.accept()','PUT',1,'admin','研发部门','/dms/workorder/accept/16','127.0.0.1','内网IP','16 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 09:21:03',7),(369,'工单开始处理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.startProcess()','PUT',1,'admin','研发部门','/dms/workorder/process/16','127.0.0.1','内网IP','16 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 09:21:07',11),(370,'工单完工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.complete()','PUT',1,'admin','研发部门','/dms/workorder/complete','127.0.0.1','内网IP','{\"completeRemark\":\"\",\"completeTime\":\"2026-07-17 09:21:17\",\"deviationReason\":\"\",\"downtimeDuration\":1,\"faultCause\":\"6\",\"orderId\":16,\"orderNo\":\"WO202607170008\",\"orderStatus\":\"4\",\"orderType\":\"0\",\"params\":{},\"repairMeasure\":\"66666666666666666666666666666\",\"sparePartsUsed\":\"\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 09:21:17',16),(371,'工单验收',2,'com.ruoyi.dms.controller.DmsWorkOrderController.verify()','PUT',1,'admin','研发部门','/dms/workorder/verify','127.0.0.1','内网IP','{\"orderId\":16,\"orderNo\":\"WO202607170008\",\"orderStatus\":\"5\",\"params\":{},\"rating\":5,\"sparePartsUsed\":\"\",\"verifierId\":100,\"verifierName\":\"张涛\",\"verifyOpinion\":\"66666\",\"verifyTime\":\"2026-07-17 09:21:23\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 09:21:23',15),(372,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/308','127.0.0.1','内网IP','[308] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:14:52',46),(373,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/286','127.0.0.1','内网IP','[286] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:14:55',41),(374,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/307','127.0.0.1','内网IP','[307] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:14:57',44),(375,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/285','127.0.0.1','内网IP','[285] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:15:00',15),(376,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/306','127.0.0.1','内网IP','[306] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:15:02',41),(377,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/284','127.0.0.1','内网IP','[284] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:15:04',42),(378,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"cycleType\":\"0\",\"cycleValue\":1,\"delFlag\":\"0\",\"deptName\":\"cccc\",\"equipmentIds\":\"[6]\",\"inspectionItems\":\"[{\\\"item\\\":\\\"温度检查\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true},{\\\"item\\\":\\\"压力检查\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":false}]\",\"params\":{},\"remark\":\"tttttttttttttttt\",\"routeCode\":\"ttttttt1\",\"routeId\":1,\"routeName\":\"tttttttt1\",\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:24:49',20),(379,'巡检路线',3,'com.ruoyi.dms.controller.DmsInspectionRouteController.remove()','DELETE',1,'admin','研发部门','/dms/inspection/route/1','127.0.0.1','内网IP','[1] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:25:24',9),(380,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"cycleType\":\"0\",\"cycleValue\":1,\"delFlag\":\"0\",\"deptId\":104,\"deptName\":\"市场部门\",\"equipmentIds\":\"[6]\",\"inspectionItems\":\"[{\\\"item\\\":\\\"dddddddddd\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true}]\",\"params\":{},\"routeCode\":\"RT20260717001\",\"routeId\":2,\"routeName\":\"ffffffffffff\",\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:28:43',357),(381,'巡检路线',2,'com.ruoyi.dms.controller.DmsInspectionRouteController.edit()','PUT',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"createTime\":\"2026-07-17 14:28:43\",\"cycleType\":\"0\",\"cycleValue\":1,\"delFlag\":\"0\",\"deptId\":104,\"deptName\":\"市场部门\",\"equipmentIds\":\"[6]\",\"inspectionItems\":\"[{\\\"item\\\":\\\"dddddddddd\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true}]\",\"params\":{},\"routeCode\":\"RT20260717001\",\"routeId\":2,\"routeName\":\"ffffffffffff\",\"status\":\"0\",\"updateBy\":\"\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:30:03',39),(382,'点检任务',1,'com.ruoyi.dms.controller.DmsInspectionTaskController.add()','POST',1,'admin','研发部门','/dms/inspection/task','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"inspectorId\":2,\"inspectorName\":\"李华\",\"params\":{},\"planDate\":\"2026-07-17\",\"routeId\":2,\"routeName\":\"ffffffffffff\",\"taskId\":1,\"taskNo\":\"INS20260717143453\",\"taskStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:34:53',347),(383,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/312','127.0.0.1','内网IP','[312] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:35:37',76),(384,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/290','127.0.0.1','内网IP','[290] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:35:45',42),(385,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/311','127.0.0.1','内网IP','[311] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:35:48',42),(386,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/289','127.0.0.1','内网IP','[289] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:35:51',42),(387,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/310','127.0.0.1','内网IP','[310] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:35:54',15),(388,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/288','127.0.0.1','内网IP','[288] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:35:56',14),(389,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/309','127.0.0.1','内网IP','[309] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:35:58',40),(390,'字典类型',3,'com.ruoyi.web.controller.system.SysDictDataController.remove()','DELETE',1,'admin','研发部门','/system/dict/data/287','127.0.0.1','内网IP','[287] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:36:00',40),(391,'点检任务完成',2,'com.ruoyi.dms.controller.DmsInspectionTaskController.complete()','PUT',1,'admin','研发部门','/dms/inspection/task/complete','127.0.0.1','内网IP','{\"abnormalCount\":0,\"completeTime\":\"2026-07-17 14:37:12\",\"inspectorId\":2,\"inspectorName\":\"李华\",\"params\":{},\"resultData\":\"[{\\\"item\\\":\\\"dddddddddd\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"result\\\":\\\"ok\\\",\\\"abnormal\\\":false,\\\"abnormalDesc\\\":\\\"\\\"}]\",\"taskId\":1,\"taskNo\":\"INS20260717143453\",\"taskStatus\":\"2\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:37:12',31),(392,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"cycleType\":\"1\",\"cycleValue\":1,\"delFlag\":\"0\",\"deptId\":106,\"deptName\":\"财务部门\",\"equipmentIds\":\"[3,2]\",\"inspectionItems\":\"[{\\\"item\\\":\\\"wwwwww\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true},{\\\"item\\\":\\\"eeeeeeeeeeee\\\",\\\"type\\\":\\\"number\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true},{\\\"item\\\":\\\"rrrrrrrrrrrrrrr\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true}]\",\"params\":{},\"remark\":\"55555\",\"routeCode\":\"RT20260717001\",\"routeName\":\"ttttttttttt\",\"status\":\"0\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'\r\n### The error may exist in URL [jar:file:/C:/Users/Laptop/.m2/repository/com/ruoyi/ruoyi-dms/3.9.2/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsInspectionRouteMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsInspectionRouteMapper.insertRoute-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_inspection_route (             route_name, route_code, dept_id, dept_name, cycle_type, cycle_value,             equipment_ids, inspection_items, status, create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'\n; Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'','2026-07-17 14:38:10',226),(393,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"cycleType\":\"1\",\"cycleValue\":1,\"delFlag\":\"0\",\"deptId\":106,\"deptName\":\"财务部门\",\"equipmentIds\":\"[3,2]\",\"inspectionItems\":\"[{\\\"item\\\":\\\"wwwwww\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true},{\\\"item\\\":\\\"eeeeeeeeeeee\\\",\\\"type\\\":\\\"number\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true},{\\\"item\\\":\\\"rrrrrrrrrrrrrrr\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true}]\",\"params\":{},\"remark\":\"55555\",\"routeCode\":\"RT20260717001\",\"routeName\":\"ttttttttttt\",\"status\":\"0\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'\r\n### The error may exist in URL [jar:file:/C:/Users/Laptop/.m2/repository/com/ruoyi/ruoyi-dms/3.9.2/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsInspectionRouteMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsInspectionRouteMapper.insertRoute-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_inspection_route (             route_name, route_code, dept_id, dept_name, cycle_type, cycle_value,             equipment_ids, inspection_items, status, create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'\n; Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'','2026-07-17 14:38:29',15),(394,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"cycleType\":\"1\",\"cycleValue\":1,\"delFlag\":\"0\",\"deptId\":106,\"deptName\":\"财务部门\",\"equipmentIds\":\"[3,2]\",\"inspectionItems\":\"[{\\\"item\\\":\\\"wwwwww\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true}]\",\"params\":{},\"remark\":\"55555\",\"routeCode\":\"RT20260717001\",\"routeName\":\"ttttttttttt\",\"status\":\"0\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'\r\n### The error may exist in URL [jar:file:/C:/Users/Laptop/.m2/repository/com/ruoyi/ruoyi-dms/3.9.2/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsInspectionRouteMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsInspectionRouteMapper.insertRoute-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_inspection_route (             route_name, route_code, dept_id, dept_name, cycle_type, cycle_value,             equipment_ids, inspection_items, status, create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'\n; Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'','2026-07-17 14:38:33',35),(395,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"cycleType\":\"1\",\"cycleValue\":1,\"delFlag\":\"0\",\"deptId\":106,\"deptName\":\"财务部门\",\"equipmentIds\":\"[3]\",\"inspectionItems\":\"[{\\\"item\\\":\\\"wwwwww\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true}]\",\"params\":{},\"remark\":\"55555\",\"routeCode\":\"RT20260717001\",\"routeName\":\"ttttttttttt\",\"status\":\"0\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'\r\n### The error may exist in URL [jar:file:/C:/Users/Laptop/.m2/repository/com/ruoyi/ruoyi-dms/3.9.2/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsInspectionRouteMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsInspectionRouteMapper.insertRoute-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_inspection_route (             route_name, route_code, dept_id, dept_name, cycle_type, cycle_value,             equipment_ids, inspection_items, status, create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'\n; Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'','2026-07-17 14:38:35',38),(396,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"cycleType\":\"0\",\"cycleValue\":1,\"delFlag\":\"0\",\"deptId\":105,\"deptName\":\"测试部门\",\"equipmentIds\":\"[6]\",\"inspectionItems\":\"[{\\\"item\\\":\\\"yyyyyyyyyyy\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true},{\\\"item\\\":\\\"uuuuuuuuuu\\\",\\\"type\\\":\\\"number\\\",\\\"unit\\\":\\\"f\\\",\\\"abnormalRequired\\\":true},{\\\"item\\\":\\\"iiiiiiiii\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true}]\",\"params\":{},\"routeCode\":\"RT20260717001\",\"routeName\":\"yyyy\",\"status\":\"0\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'\r\n### The error may exist in URL [jar:file:/C:/Users/Laptop/.m2/repository/com/ruoyi/ruoyi-dms/3.9.2/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsInspectionRouteMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsInspectionRouteMapper.insertRoute-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_inspection_route (             route_name, route_code, dept_id, dept_name, cycle_type, cycle_value,             equipment_ids, inspection_items, status, create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'\n; Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'','2026-07-17 14:43:14',35),(397,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"cycleType\":\"0\",\"cycleValue\":1,\"delFlag\":\"0\",\"deptId\":105,\"deptName\":\"测试部门\",\"equipmentIds\":\"[6]\",\"inspectionItems\":\"[{\\\"item\\\":\\\"yyyyyyyyyyy\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true},{\\\"item\\\":\\\"uuuuuuuuuu\\\",\\\"type\\\":\\\"number\\\",\\\"unit\\\":\\\"f\\\",\\\"abnormalRequired\\\":true},{\\\"item\\\":\\\"iiiiiiiii\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true}]\",\"params\":{},\"routeCode\":\"RT20260717001\",\"routeName\":\"yyyy\",\"status\":\"0\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'\r\n### The error may exist in URL [jar:file:/C:/Users/Laptop/.m2/repository/com/ruoyi/ruoyi-dms/3.9.2/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsInspectionRouteMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsInspectionRouteMapper.insertRoute-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_inspection_route (             route_name, route_code, dept_id, dept_name, cycle_type, cycle_value,             equipment_ids, inspection_items, status, create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'\n; Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'','2026-07-17 14:45:21',14),(398,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"cycleType\":\"0\",\"cycleValue\":1,\"delFlag\":\"0\",\"deptId\":105,\"deptName\":\"测试部门\",\"equipmentIds\":\"[6]\",\"inspectionItems\":\"[{\\\"item\\\":\\\"yyyyyyyyyyy\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true},{\\\"item\\\":\\\"uuuuuuuuuu\\\",\\\"type\\\":\\\"number\\\",\\\"unit\\\":\\\"f\\\",\\\"abnormalRequired\\\":true},{\\\"item\\\":\\\"iiiiiiiii\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true}]\",\"params\":{},\"routeCode\":\"RT20260717002\",\"routeId\":9,\"routeName\":\"yyyy\",\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:50:30',303),(399,'点检任务',1,'com.ruoyi.dms.controller.DmsInspectionTaskController.add()','POST',1,'admin','研发部门','/dms/inspection/task','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"inspectorId\":1,\"inspectorName\":\"系统管理员\",\"params\":{},\"planDate\":\"2026-07-17\",\"routeId\":9,\"routeName\":\"yyyy\",\"taskId\":2,\"taskNo\":\"INS20260717145057\",\"taskStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:50:57',49),(400,'点检任务完成',2,'com.ruoyi.dms.controller.DmsInspectionTaskController.complete()','PUT',1,'admin','研发部门','/dms/inspection/task/complete','127.0.0.1','内网IP','{\"abnormalCount\":2,\"completeTime\":\"2026-07-17 14:56:03\",\"inspectorId\":1,\"inspectorName\":\"系统管理员\",\"params\":{},\"photoUrls\":\"[\\\"/profile/upload/2026/07/17/Surface_Laptop_3_01_20260717145602A002.jpg\\\"]\",\"resultData\":\"[{\\\"item\\\":\\\"yyyyyyyyyyy\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"result\\\":\\\"ok\\\",\\\"abnormal\\\":false,\\\"abnormalDesc\\\":\\\"\\\"},{\\\"item\\\":\\\"uuuuuuuuuu\\\",\\\"type\\\":\\\"number\\\",\\\"unit\\\":\\\"f\\\",\\\"result\\\":\\\"ok\\\",\\\"abnormal\\\":true,\\\"abnormalDesc\\\":\\\"tttttt\\\"},{\\\"item\\\":\\\"iiiiiiiii\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"result\\\":\\\"ok\\\",\\\"abnormal\\\":true,\\\"abnormalDesc\\\":\\\"tttttt\\\"}]\",\"taskId\":2,\"taskNo\":\"INS20260717145057\",\"taskStatus\":\"2\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Column \'equipment_id\' cannot be null\r\n### The error may exist in URL [jar:file:/C:/Users/Laptop/.m2/repository/com/ruoyi/ruoyi-dms/3.9.2/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsWorkOrderMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsWorkOrderMapper.insertWorkOrder-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_work_order (             order_no, equipment_id, equipment_code, equipment_name, order_type, fault_description,             reporter_id, reporter_name, report_time, priority, order_status,             assignee_id, assignee_name, assign_time, arrive_time,             sla_response_deadline, sla_process_deadline, sla_timeout_status, sla_escalated,             fault_cause, repair_measure,             spare_parts_used, task_checklist, downtime_duration, complete_time, complete_remark, deviation_reason,             assigner_id, assigner_name, verifier_id, verifier_name, verify_time, verify_opinion, rating,             create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?,             ?, ?, ?, ?,             ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?, ?,             ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Column \'equipment_id\' cannot be null\n; Column \'equipment_id\' cannot be null','2026-07-17 14:56:03',238),(401,'点检任务完成',2,'com.ruoyi.dms.controller.DmsInspectionTaskController.complete()','PUT',1,'admin','研发部门','/dms/inspection/task/complete','127.0.0.1','内网IP','{\"abnormalCount\":2,\"completeTime\":\"2026-07-17 14:58:38\",\"inspectorId\":1,\"inspectorName\":\"系统管理员\",\"params\":{},\"photoUrls\":\"[\\\"/profile/upload/2026/07/17/Surface_Laptop_3_01_20260717145602A002.jpg\\\"]\",\"resultData\":\"[{\\\"item\\\":\\\"yyyyyyyyyyy\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"result\\\":\\\"ok\\\",\\\"abnormal\\\":false,\\\"abnormalDesc\\\":\\\"\\\"},{\\\"item\\\":\\\"uuuuuuuuuu\\\",\\\"type\\\":\\\"number\\\",\\\"unit\\\":\\\"f\\\",\\\"result\\\":\\\"ok\\\",\\\"abnormal\\\":true,\\\"abnormalDesc\\\":\\\"tttttt\\\"},{\\\"item\\\":\\\"iiiiiiiii\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"result\\\":\\\"ok\\\",\\\"abnormal\\\":true,\\\"abnormalDesc\\\":\\\"tttttt\\\"}]\",\"taskId\":2,\"taskNo\":\"INS20260717145057\",\"taskStatus\":\"2\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 14:58:38',38),(402,'点检任务',1,'com.ruoyi.dms.controller.DmsInspectionTaskController.add()','POST',1,'admin','研发部门','/dms/inspection/task','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"inspectorId\":1,\"inspectorName\":\"系统管理员\",\"params\":{},\"planDate\":\"2026-07-17\",\"routeId\":9,\"routeName\":\"yyyy\",\"taskId\":3,\"taskNo\":\"INS20260717150035\",\"taskStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 15:00:35',37),(403,'点检任务',3,'com.ruoyi.dms.controller.DmsInspectionTaskController.remove()','DELETE',1,'admin','研发部门','/dms/inspection/task/1','127.0.0.1','内网IP','[1] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 15:03:01',12),(404,'点检任务完成',2,'com.ruoyi.dms.controller.DmsInspectionTaskController.complete()','PUT',1,'admin','研发部门','/dms/inspection/task/complete','127.0.0.1','内网IP','{\"abnormalCount\":2,\"completeTime\":\"2026-07-17 15:07:14\",\"inspectorId\":1,\"inspectorName\":\"系统管理员\",\"params\":{},\"photoUrls\":\"[\\\"/profile/upload/2026/07/17/Surface_Laptop_3_02_20260717150708A001.jpg\\\",\\\"/profile/upload/2026/07/17/Surface_Laptop_3_03_20260717150713A002.jpg\\\"]\",\"resultData\":\"[{\\\"item\\\":\\\"yyyyyyyyyyy\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"result\\\":\\\"ok\\\",\\\"abnormal\\\":false,\\\"abnormalDesc\\\":\\\"\\\"},{\\\"item\\\":\\\"uuuuuuuuuu\\\",\\\"type\\\":\\\"number\\\",\\\"unit\\\":\\\"f\\\",\\\"result\\\":\\\"ok\\\",\\\"value\\\":90,\\\"abnormal\\\":true,\\\"abnormalDesc\\\":\\\"ssssss1111111111\\\"},{\\\"item\\\":\\\"iiiiiiiii\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"result\\\":\\\"ok\\\",\\\"value\\\":\\\"sssssssssss\\\",\\\"abnormal\\\":true,\\\"abnormalDesc\\\":\\\"sssssssssssssssss222222222\\\"}]\",\"taskId\":3,\"taskNo\":\"INS20260717150035\",\"taskStatus\":\"2\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 15:07:15',460),(405,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"cycleType\":\"0\",\"cycleValue\":1,\"delFlag\":\"0\",\"deptId\":105,\"deptName\":\"测试部门\",\"equipmentIds\":\"[5,3]\",\"inspectionItems\":\"[{\\\"item\\\":\\\"yy11\\\",\\\"type\\\":\\\"number\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true},{\\\"item\\\":\\\"yyy2\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true}]\",\"params\":{},\"routeCode\":\"RT20260717003\",\"routeId\":10,\"routeName\":\"yyyyyyyyyyyyyyy\",\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 15:10:46',44),(406,'点检任务',1,'com.ruoyi.dms.controller.DmsInspectionTaskController.add()','POST',1,'admin','研发部门','/dms/inspection/task','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"inspectorId\":100,\"inspectorName\":\"张涛\",\"params\":{},\"planDate\":\"2026-07-17\",\"routeId\":10,\"routeName\":\"yyyyyyyyyyyyyyy\",\"taskId\":4,\"taskNo\":\"INS20260717151104\",\"taskStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 15:11:04',37),(407,'点检任务完成',2,'com.ruoyi.dms.controller.DmsInspectionTaskController.complete()','PUT',1,'admin','研发部门','/dms/inspection/task/complete','127.0.0.1','内网IP','{\"abnormalCount\":2,\"completeTime\":\"2026-07-17 15:11:24\",\"inspectorId\":100,\"inspectorName\":\"张涛\",\"params\":{},\"photoUrls\":\"[\\\"/profile/upload/2026/07/17/Surface_Laptop_3_04_20260717151123A003.jpg\\\"]\",\"resultData\":\"[{\\\"item\\\":\\\"yy11\\\",\\\"type\\\":\\\"number\\\",\\\"unit\\\":\\\"\\\",\\\"result\\\":\\\"ok\\\",\\\"abnormal\\\":true,\\\"abnormalDesc\\\":\\\"222222222\\\"},{\\\"item\\\":\\\"yyy2\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"result\\\":\\\"abnormal\\\",\\\"abnormal\\\":true,\\\"abnormalDesc\\\":\\\"333333333333\\\"}]\",\"taskId\":4,\"taskNo\":\"INS20260717151104\",\"taskStatus\":\"2\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 15:11:24',61),(408,'巡检路线',3,'com.ruoyi.dms.controller.DmsInspectionRouteController.remove()','DELETE',1,'admin','研发部门','/dms/inspection/route/10,9,2','127.0.0.1','内网IP','[10,9,2] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 15:17:00',38),(409,'点检任务',3,'com.ruoyi.dms.controller.DmsInspectionTaskController.remove()','DELETE',1,'admin','研发部门','/dms/inspection/task/4,3,2','127.0.0.1','内网IP','[4,3,2] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 15:17:04',35),(410,'工单管理',3,'com.ruoyi.dms.controller.DmsWorkOrderController.remove()','DELETE',1,'admin','研发部门','/dms/workorder/19,18,17','127.0.0.1','内网IP','[19,18,17] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 15:17:15',48),(411,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"cycleType\":\"1\",\"cycleValue\":2,\"delFlag\":\"0\",\"deptId\":103,\"deptName\":\"研发部门\",\"equipmentIds\":\"[6,5]\",\"inspectionItems\":\"{\\\"common\\\":[{\\\"item\\\":\\\"环境卫生\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true}],\\\"devices\\\":[{\\\"equipmentId\\\":6,\\\"equipmentName\\\":\\\"EQ005 - 数控加工中心\\\",\\\"items\\\":[{\\\"item\\\":\\\"温度\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":false}]},{\\\"equipmentId\\\":5,\\\"equipmentName\\\":\\\"EQ004 - 平面磨床\\\",\\\"items\\\":[{\\\"item\\\":\\\"刀口\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true}]}]}\",\"params\":{},\"remark\":\"111111111\",\"routeCode\":\"RT20260717001\",\"routeName\":\"日常巡检测试1\",\"status\":\"0\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'\r\n### The error may exist in URL [jar:file:/C:/Users/Laptop/.m2/repository/com/ruoyi/ruoyi-dms/3.9.2/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsInspectionRouteMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsInspectionRouteMapper.insertRoute-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_inspection_route (             route_name, route_code, dept_id, dept_name, cycle_type, cycle_value,             equipment_ids, inspection_items, status, create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'\n; Duplicate entry \'RT20260717001\' for key \'dms_inspection_route.uk_route_code\'','2026-07-17 15:31:44',127),(412,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"cycleType\":\"1\",\"cycleValue\":2,\"delFlag\":\"0\",\"deptId\":103,\"deptName\":\"研发部门\",\"equipmentIds\":\"[6,5]\",\"inspectionItems\":\"{\\\"common\\\":[{\\\"item\\\":\\\"环境卫生\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true}],\\\"devices\\\":[{\\\"equipmentId\\\":6,\\\"equipmentName\\\":\\\"EQ005 - 数控加工中心\\\",\\\"items\\\":[{\\\"item\\\":\\\"温度\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":false}]},{\\\"equipmentId\\\":5,\\\"equipmentName\\\":\\\"EQ004 - 平面磨床\\\",\\\"items\\\":[{\\\"item\\\":\\\"刀口\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"abnormalRequired\\\":true}]}]}\",\"params\":{},\"remark\":\"111111111\",\"routeCode\":\"RT20260717004\",\"routeId\":12,\"routeName\":\"日常巡检测试1\",\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 15:41:15',210),(413,'点检任务',1,'com.ruoyi.dms.controller.DmsInspectionTaskController.add()','POST',1,'admin','研发部门','/dms/inspection/task','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"inspectorId\":2,\"inspectorName\":\"李华\",\"params\":{},\"planDate\":\"2026-07-17\",\"routeId\":12,\"routeName\":\"日常巡检测试1\",\"taskId\":5,\"taskNo\":\"INS20260717154128\",\"taskStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 15:41:28',15),(414,'点检任务完成',2,'com.ruoyi.dms.controller.DmsInspectionTaskController.complete()','PUT',1,'admin','研发部门','/dms/inspection/task/complete','127.0.0.1','内网IP','{\"abnormalCount\":3,\"completeTime\":\"2026-07-17 15:42:07\",\"inspectorId\":2,\"inspectorName\":\"李华\",\"params\":{},\"photoUrls\":\"[\\\"/profile/upload/2026/07/17/Surface_Laptop_3_02_20260717154206A001.jpg\\\"]\",\"resultData\":\"{\\\"common\\\":[{\\\"item\\\":\\\"环境卫生\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"result\\\":\\\"ok\\\",\\\"value\\\":\\\"6666666666666\\\",\\\"abnormal\\\":true,\\\"abnormalDesc\\\":\\\"777777777777\\\"}],\\\"devices\\\":[{\\\"equipmentId\\\":6,\\\"equipmentName\\\":\\\"EQ005 - 数控加工中心\\\",\\\"items\\\":[{\\\"item\\\":\\\"温度\\\",\\\"type\\\":\\\"check\\\",\\\"unit\\\":\\\"\\\",\\\"result\\\":\\\"abnormal\\\",\\\"abnormal\\\":true,\\\"abnormalDesc\\\":\\\"rrrrrr\\\"}]},{\\\"equipmentId\\\":5,\\\"equipmentName\\\":\\\"EQ004 - 平面磨床\\\",\\\"items\\\":[{\\\"item\\\":\\\"刀口\\\",\\\"type\\\":\\\"text\\\",\\\"unit\\\":\\\"\\\",\\\"result\\\":\\\"ok\\\",\\\"value\\\":\\\"ttttttttt\\\",\\\"abnormal\\\":true,\\\"abnormalDesc\\\":\\\"ttttttttttttttttttttt\\\"}]}]}\",\"taskId\":5,\"taskNo\":\"INS20260717154128\",\"taskStatus\":\"2\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 15:42:07',51),(415,'设备分类管理',1,'com.ruoyi.dms.controller.DmsEquipmentCategoryController.add()','POST',1,'admin','研发部门','/dms/category','127.0.0.1','内网IP','{\"ancestors\":\"0\",\"categoryName\":\"测试分类T\",\"delFlag\":\"0\",\"orderNum\":99,\"params\":{},\"parentId\":0,\"status\":\"0\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Column \'category_code\' cannot be null\r\n### The error may exist in URL [jar:file:/C:/Users/Laptop/.m2/repository/com/ruoyi/ruoyi-dms/3.9.2/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsEquipmentCategoryMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsEquipmentCategoryMapper.insertCategory-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO dms_equipment_category (             parent_id, ancestors, category_code, category_name, category_level,             order_num, status, create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?,             ?, ?, ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Column \'category_code\' cannot be null\n; Column \'category_code\' cannot be null','2026-07-17 17:50:11',111),(416,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"delFlag\":\"0\",\"equipmentCode\":\"EQ001\",\"equipmentId\":1,\"equipmentName\":\"空压机1\",\"faultDescription\":\"测试工单-自动测试\",\"orderId\":21,\"orderNo\":\"WO202607170013\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"reportTime\":\"2026-07-17 17:51:35\",\"slaEscalated\":\"0\",\"slaProcessDeadline\":\"2026-07-18 17:51:35\",\"slaResponseDeadline\":\"2026-07-17 21:51:35\",\"slaTimeoutStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 17:51:35',34),(417,'设备分类管理',1,'com.ruoyi.dms.controller.DmsEquipmentCategoryController.add()','POST',1,'admin','研发部门','/dms/category','127.0.0.1','内网IP','{\"ancestors\":\"0\",\"categoryCode\":\"TEST-CAT-T\",\"categoryId\":25,\"categoryLevel\":1,\"categoryName\":\"测试分类T\",\"delFlag\":\"0\",\"orderNum\":99,\"params\":{},\"parentId\":0,\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 17:51:49',8),(418,'设备台账管理',1,'com.ruoyi.dms.controller.DmsEquipmentController.add()','POST',1,'admin','研发部门','/dms/equipment','127.0.0.1','内网IP','{\"categoryId\":1,\"delFlag\":\"0\",\"equipmentCode\":\"TEST-EQ-T\",\"equipmentId\":7,\"equipmentName\":\"测试设备T\",\"equipmentStatus\":\"0\",\"params\":{},\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 17:52:15',20),(419,'设备分类管理',1,'com.ruoyi.dms.controller.DmsEquipmentCategoryController.add()','POST',1,'admin','研发部门','/dms/category','127.0.0.1','内网IP','{\"ancestors\":\"0\",\"categoryCode\":\"TEST-CAT-W\",\"categoryId\":27,\"categoryLevel\":1,\"categoryName\":\"TestCatW\",\"delFlag\":\"0\",\"orderNum\":93,\"params\":{},\"parentId\":0,\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:06:15',4),(420,'设备分类管理',1,'com.ruoyi.dms.controller.DmsEquipmentCategoryController.add()','POST',1,'admin','研发部门','/dms/category','127.0.0.1','内网IP','{\"ancestors\":\"0\",\"categoryCode\":\"TEST-CAT-C\",\"categoryId\":28,\"categoryLevel\":1,\"categoryName\":\"TestCatC\",\"delFlag\":\"0\",\"orderNum\":92,\"params\":{},\"parentId\":0,\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:06:32',8),(421,'设备分类管理',1,'com.ruoyi.dms.controller.DmsEquipmentCategoryController.add()','POST',1,'admin','研发部门','/dms/category','127.0.0.1','内网IP','{\"ancestors\":\"0\",\"categoryCode\":\"TEST-CAT-D\",\"categoryId\":29,\"categoryLevel\":1,\"categoryName\":\"TestCatD\",\"delFlag\":\"0\",\"orderNum\":91,\"params\":{},\"parentId\":0,\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:06:44',8),(422,'设备台账管理',1,'com.ruoyi.dms.controller.DmsEquipmentController.add()','POST',1,'admin','研发部门','/dms/equipment','127.0.0.1','内网IP','{\"categoryId\":1,\"delFlag\":\"0\",\"equipmentCode\":\"TEST-EQ-C\",\"equipmentId\":8,\"equipmentName\":\"TestEqC\",\"equipmentStatus\":\"0\",\"params\":{},\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:07:18',9),(423,'设备台账管理',3,'com.ruoyi.dms.controller.DmsEquipmentController.remove()','DELETE',1,'admin','研发部门','/dms/equipment/8','127.0.0.1','内网IP','[8] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:07:49',9),(424,'设备台账管理',2,'com.ruoyi.dms.controller.DmsEquipmentController.edit()','PUT',1,'admin','研发部门','/dms/equipment','127.0.0.1','内网IP','{\"categoryId\":1,\"equipmentCode\":\"TEST-EQ-C\",\"equipmentId\":8,\"equipmentName\":\"TestEqChanged\",\"equipmentStatus\":\"1\",\"params\":{},\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:08:14',7),(425,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"delFlag\":\"0\",\"equipmentCode\":\"EQ001\",\"equipmentId\":1,\"equipmentName\":\"AirCompressor1\",\"faultDescription\":\"TestWorkOrder\",\"orderId\":22,\"orderNo\":\"WO202607170014\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"reportTime\":\"2026-07-17 18:08:35\",\"slaEscalated\":\"0\",\"slaProcessDeadline\":\"2026-07-18 18:08:35\",\"slaResponseDeadline\":\"2026-07-17 22:08:35\",\"slaTimeoutStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:08:35',11),(426,'工单派工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.dispatch()','PUT',1,'admin','研发部门','/dms/workorder/dispatch','127.0.0.1','内网IP','{\"assignTime\":\"2026-07-17 18:09:01\",\"assigneeId\":1,\"assigneeName\":\"admin\",\"orderId\":22,\"orderStatus\":\"1\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:09:01',9),(427,'工单接单',2,'com.ruoyi.dms.controller.DmsWorkOrderController.accept()','PUT',1,'admin','研发部门','/dms/workorder/accept/22','127.0.0.1','内网IP','22 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:09:01',9),(428,'工单开始处理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.startProcess()','PUT',1,'admin','研发部门','/dms/workorder/process/22','127.0.0.1','内网IP','22 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:09:01',8),(429,'工单完工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.complete()','PUT',1,'admin','研发部门','/dms/workorder/complete','127.0.0.1','内网IP','{\"completeTime\":\"2026-07-17 18:09:16\",\"orderId\":22,\"orderStatus\":\"4\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:09:16',11),(430,'工单验收',2,'com.ruoyi.dms.controller.DmsWorkOrderController.verify()','PUT',1,'admin','研发部门','/dms/workorder/verify','127.0.0.1','内网IP','{\"orderId\":22,\"orderStatus\":\"5\",\"params\":{},\"verifyTime\":\"2026-07-17 18:09:16\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:09:16',7),(431,'工单管理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.edit()','PUT',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"faultDescription\":\"UpdatedDesc\",\"orderId\":22,\"params\":{},\"priority\":\"3\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:09:16',2),(432,'工单管理',3,'com.ruoyi.dms.controller.DmsWorkOrderController.remove()','DELETE',1,'admin','研发部门','/dms/workorder/22','127.0.0.1','内网IP','[22] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:09:17',3),(433,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"delFlag\":\"0\",\"equipmentCode\":\"EQ001\",\"equipmentId\":1,\"equipmentName\":\"AirComp1\",\"faultDescription\":\"TestWO2\",\"orderId\":23,\"orderNo\":\"WO202607170015\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"reportTime\":\"2026-07-17 18:09:33\",\"slaEscalated\":\"0\",\"slaProcessDeadline\":\"2026-07-18 18:09:33\",\"slaResponseDeadline\":\"2026-07-17 22:09:33\",\"slaTimeoutStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:09:33',10),(434,'工单改派',2,'com.ruoyi.dms.controller.DmsWorkOrderController.reassign()','PUT',1,'admin','研发部门','/dms/workorder/reassign','127.0.0.1','内网IP','{\"assigneeId\":2,\"assigneeName\":\"ry\",\"orderId\":23,\"params\":{}} ',NULL,1,'当前状态不允许改派','2026-07-17 18:09:46',5),(435,'工单驳回',2,'com.ruoyi.dms.controller.DmsWorkOrderController.reject()','PUT',1,'admin','研发部门','/dms/workorder/reject','127.0.0.1','内网IP','{\"orderId\":23,\"params\":{}} ',NULL,1,'当前状态不允许驳回','2026-07-17 18:09:46',3),(436,'工单撤销',2,'com.ruoyi.dms.controller.DmsWorkOrderController.cancel()','PUT',1,'admin','研发部门','/dms/workorder/cancel/23','127.0.0.1','内网IP','23 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:09:46',7),(437,'工单管理',5,'com.ruoyi.dms.controller.DmsWorkOrderController.export()','POST',1,'admin','研发部门','/dms/workorder/export','127.0.0.1','内网IP','{\"params\":{}} ',NULL,0,NULL,'2026-07-17 18:10:08',499),(438,'PM维护计划',1,'com.ruoyi.dms.controller.DmsPmPlanController.add()','POST',1,'admin','研发部门','/dms/pmplan','127.0.0.1','内网IP','{\"cycleUnit\":\"2\",\"cycleValue\":30,\"delFlag\":\"0\",\"equipmentId\":1,\"nextExecuteTime\":\"2026-08-16 18:10:30\",\"params\":{},\"planId\":5,\"planName\":\"TestPMPlan\",\"status\":\"0\",\"taskList\":\"Check;Clean\",\"triggerType\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:10:30',14),(439,'PM维护计划',2,'com.ruoyi.dms.controller.DmsPmPlanController.edit()','PUT',1,'admin','研发部门','/dms/pmplan','127.0.0.1','内网IP','{\"cycleUnit\":\"2\",\"cycleValue\":60,\"equipmentId\":1,\"nextExecuteTime\":\"2026-09-15 18:10:43\",\"params\":{},\"planId\":5,\"planName\":\"TestPMPlanChanged\",\"status\":\"0\",\"triggerType\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:10:43',5),(440,'PM维护计划',3,'com.ruoyi.dms.controller.DmsPmPlanController.remove()','DELETE',1,'admin','研发部门','/dms/pmplan/5','127.0.0.1','内网IP','[5] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:10:43',3),(441,'备件管理',1,'com.ruoyi.dms.controller.DmsSparePartController.add()','POST',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"currentStock\":100,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"TEST-SP-C\",\"partId\":11,\"partName\":\"TestSparePartC\",\"partType\":\"0\",\"safetyStock\":20,\"status\":\"0\",\"unit\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:11:15',11),(442,'备件管理',2,'com.ruoyi.dms.controller.DmsSparePartController.edit()','PUT',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"currentStock\":80,\"params\":{},\"partCode\":\"TEST-SP-C\",\"partId\":11,\"partName\":\"TestSPChanged\",\"partType\":\"0\",\"safetyStock\":20,\"status\":\"0\",\"unit\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:11:33',8),(443,'备件管理',3,'com.ruoyi.dms.controller.DmsSparePartController.remove()','DELETE',1,'admin','研发部门','/dms/sparepart/11','127.0.0.1','内网IP','[11] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:11:33',4),(444,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"params\":{},\"routeCode\":\"TEST-RT-C\",\"routeId\":13,\"routeName\":\"TestRoute\",\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:11:57',4),(445,'巡检路线',2,'com.ruoyi.dms.controller.DmsInspectionRouteController.edit()','PUT',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"params\":{},\"routeCode\":\"TEST-RT-C\",\"routeId\":13,\"routeName\":\"TestRouteChanged\",\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:12:11',9),(446,'巡检路线',3,'com.ruoyi.dms.controller.DmsInspectionRouteController.remove()','DELETE',1,'admin','研发部门','/dms/inspection/route/13','127.0.0.1','内网IP','[13] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:12:11',6),(447,'点检任务',1,'com.ruoyi.dms.controller.DmsInspectionTaskController.add()','POST',1,'admin','研发部门','/dms/inspection/task','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"params\":{},\"routeId\":1,\"taskId\":6,\"taskNo\":\"INS20260717181228\",\"taskStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:12:28',9),(448,'点检任务',2,'com.ruoyi.dms.controller.DmsInspectionTaskController.edit()','PUT',1,'admin','研发部门','/dms/inspection/task','127.0.0.1','内网IP','{\"params\":{},\"routeId\":1,\"taskId\":6,\"taskStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:13:13',7),(449,'点检任务完成',2,'com.ruoyi.dms.controller.DmsInspectionTaskController.complete()','PUT',1,'admin','研发部门','/dms/inspection/task/complete','127.0.0.1','内网IP','{\"completeTime\":\"2026-07-17 18:13:13\",\"params\":{},\"remark\":\"TestDone\",\"taskId\":6,\"taskStatus\":\"2\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:13:13',5),(450,'点检任务',3,'com.ruoyi.dms.controller.DmsInspectionTaskController.remove()','DELETE',1,'admin','研发部门','/dms/inspection/task/6','127.0.0.1','内网IP','[6] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:13:13',3),(451,'采集配置',1,'com.ruoyi.dms.controller.DmsDataCollectionConfigController.add()','POST',1,'admin','研发部门','/dms/data/config','127.0.0.1','内网IP','{\"collectMode\":\"0\",\"configId\":2,\"delFlag\":\"0\",\"equipmentId\":1,\"params\":{},\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:13:34',12),(452,'采集配置',2,'com.ruoyi.dms.controller.DmsDataCollectionConfigController.edit()','PUT',1,'admin','研发部门','/dms/data/config','127.0.0.1','内网IP','{\"collectMode\":\"0\",\"configId\":2,\"equipmentId\":1,\"params\":{},\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:14:22',10),(453,'采集配置',3,'com.ruoyi.dms.controller.DmsDataCollectionConfigController.remove()','DELETE',1,'admin','研发部门','/dms/data/config/2','127.0.0.1','内网IP','[2] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:14:22',3),(454,'数据记录',1,'com.ruoyi.dms.controller.DmsDataRecordController.add()','POST',1,'admin','研发部门','/dms/data/record','127.0.0.1','内网IP','{\"equipmentId\":1,\"params\":{},\"recordId\":2} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:14:36',12),(455,'数据记录',2,'com.ruoyi.dms.controller.DmsDataRecordController.edit()','PUT',1,'admin','研发部门','/dms/data/record','127.0.0.1','内网IP','{\"params\":{},\"recordId\":2} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'update_time\' in \'field list\'\r\n### The error may exist in URL [jar:file:/C:/Users/Laptop/.m2/repository/com/ruoyi/ruoyi-dms/3.9.2/ruoyi-dms-3.9.2.jar!/mapper/dms/DmsDataRecordMapper.xml]\r\n### The error may involve com.ruoyi.dms.mapper.DmsDataRecordMapper.updateRecord-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE dms_data_record          SET update_time = sysdate()          WHERE record_id = ?\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'update_time\' in \'field list\'\n; bad SQL grammar []','2026-07-17 18:14:48',3),(456,'数据记录',3,'com.ruoyi.dms.controller.DmsDataRecordController.remove()','DELETE',1,'admin','研发部门','/dms/data/record/2','127.0.0.1','内网IP','[2] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:14:48',4),(457,'维修FAQ',1,'com.ruoyi.dms.controller.DmsAiController.addFaq()','POST',1,'admin','研发部门','/dms/ai/faq','127.0.0.1','内网IP','{\"answer\":\"TestA\",\"delFlag\":\"0\",\"faqId\":1,\"params\":{},\"question\":\"TestQ\",\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:16:01',8),(458,'维修FAQ',2,'com.ruoyi.dms.controller.DmsAiController.editFaq()','PUT',1,'admin','研发部门','/dms/ai/faq','127.0.0.1','内网IP','{\"answer\":\"TestAChanged\",\"faqId\":1,\"params\":{},\"question\":\"TestQChanged\",\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:17:02',9),(459,'维修FAQ',3,'com.ruoyi.dms.controller.DmsAiController.removeFaq()','DELETE',1,'admin','研发部门','/dms/ai/faq/1','127.0.0.1','内网IP','[1] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 18:17:02',4),(460,'设备台账管理',5,'com.ruoyi.dms.controller.DmsEquipmentController.export()','POST',1,'admin','研发部门','/dms/equipment/export','127.0.0.1','内网IP','{\"params\":{}} ',NULL,0,NULL,'2026-07-17 18:19:02',35),(461,'备件管理',5,'com.ruoyi.dms.controller.DmsSparePartController.export()','POST',1,'admin','研发部门','/dms/sparepart/export','127.0.0.1','内网IP','{\"params\":{}} ',NULL,0,NULL,'2026-07-17 18:19:02',36),(462,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/1','127.0.0.1','内网IP','1 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 19:46:35',16),(463,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/1','127.0.0.1','内网IP','1 ',NULL,1,'该计划已生成未完结的工单「WO202607170016」（当前状态：新建），请先完成或撤销该工单后再生成新工单','2026-07-17 21:35:16',38),(464,'数据记录',2,'com.ruoyi.dms.controller.DmsDataRecordController.edit()','PUT',1,'admin','研发部门','/dms/data/record','127.0.0.1','内网IP','{\"equipmentId\":1,\"equipmentName\":\"TestUpdate\",\"params\":{},\"productCount\":999,\"recordId\":1,\"runHours\":200,\"runStatus\":\"1\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 21:35:46',19),(465,'工单撤销',2,'com.ruoyi.dms.controller.DmsWorkOrderController.cancel()','PUT',1,'admin','研发部门','/dms/workorder/cancel/24','127.0.0.1','内网IP','24 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 21:36:16',15),(466,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/1','127.0.0.1','内网IP','1 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 21:36:17',30),(467,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/3','127.0.0.1','内网IP','3 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 21:54:15',33),(468,'工单管理',3,'com.ruoyi.dms.controller.DmsWorkOrderController.remove()','DELETE',1,'admin','研发部门','/dms/workorder/12,11,10','127.0.0.1','内网IP','[12,11,10] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 21:56:03',6),(469,'工单管理',3,'com.ruoyi.dms.controller.DmsWorkOrderController.remove()','DELETE',1,'admin','研发部门','/dms/workorder/26,25,24,23,21,20,16,15,14,13','127.0.0.1','内网IP','[26,25,24,23,21,20,16,15,14,13] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 21:56:05',3),(470,'PM维护计划',3,'com.ruoyi.dms.controller.DmsPmPlanController.remove()','DELETE',1,'admin','研发部门','/dms/pmplan/4,3,2,1','127.0.0.1','内网IP','[4,3,2,1] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 21:56:11',9),(471,'巡检路线',3,'com.ruoyi.dms.controller.DmsInspectionRouteController.remove()','DELETE',1,'admin','研发部门','/dms/inspection/route/12','127.0.0.1','内网IP','[12] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 21:56:18',8),(472,'点检任务',3,'com.ruoyi.dms.controller.DmsInspectionTaskController.remove()','DELETE',1,'admin','研发部门','/dms/inspection/task/5','127.0.0.1','内网IP','[5] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 21:56:21',4),(473,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"delFlag\":\"0\",\"equipmentCode\":\"EQ005\",\"equipmentId\":6,\"equipmentName\":\"数控加工中心\",\"faultDescription\":\"总是发出异响。\",\"orderId\":27,\"orderNo\":\"WO202607170019\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"rating\":0,\"remark\":\"111111111\",\"reportTime\":\"2026-07-17 21:56:52\",\"reporterId\":2,\"reporterName\":\"李华\",\"slaEscalated\":\"0\",\"slaProcessDeadline\":\"2026-07-18 21:56:52\",\"slaResponseDeadline\":\"2026-07-18 01:56:52\",\"slaTimeoutStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 21:56:56',30),(474,'工单派工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.dispatch()','PUT',1,'admin','研发部门','/dms/workorder/dispatch','127.0.0.1','内网IP','{\"assignTime\":\"2026-07-17 21:58:54\",\"assigneeId\":100,\"assigneeName\":\"张涛\",\"assignerId\":2,\"assignerName\":\"李华\",\"equipmentName\":\"数控加工中心\",\"orderId\":27,\"orderNo\":\"WO202607170019\",\"orderStatus\":\"1\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 21:58:54',14),(475,'工单接单',2,'com.ruoyi.dms.controller.DmsWorkOrderController.accept()','PUT',1,'admin','研发部门','/dms/workorder/accept/27','127.0.0.1','内网IP','27 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 21:58:58',14),(476,'维修FAQ',1,'com.ruoyi.dms.controller.DmsAiController.addFaq()','POST',1,'admin','研发部门','/dms/ai/faq','127.0.0.1','内网IP','{\"answer\":\"ttttttttttttttttttttttttttttttttttttttttt\",\"categoryName\":\"tttttttttttt\",\"delFlag\":\"0\",\"faqId\":2,\"keywords\":\"tttttttttttttt\",\"params\":{},\"question\":\"testst\",\"remark\":\"tttttt\",\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:00:54',17),(477,'工单开始处理',2,'com.ruoyi.dms.controller.DmsWorkOrderController.startProcess()','PUT',1,'admin','研发部门','/dms/workorder/process/27','127.0.0.1','内网IP','27 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:11:16',38),(478,'PM维护计划',1,'com.ruoyi.dms.controller.DmsPmPlanController.add()','POST',1,'admin','研发部门','/dms/pmplan','127.0.0.1','内网IP','{\"advanceDays\":3,\"assigneeId\":100,\"assigneeName\":\"张涛\",\"autoAssign\":\"1\",\"categoryId\":1,\"cycleUnit\":\"天\",\"cycleValue\":30,\"delFlag\":\"0\",\"equipmentId\":6,\"estimatedHours\":5,\"nextExecuteTime\":\"2026-08-16 22:15:05\",\"params\":{},\"planId\":6,\"planName\":\"测试计划001\",\"sparePartList\":\"[{\\\"partId\\\":10,\\\"partName\\\":\\\"测试备件11\\\",\\\"qty\\\":1,\\\"unit\\\":\\\"9\\\"}]\",\"status\":\"0\",\"taskList\":\"[\\\"检查温度\\\",\\\"清洁滤网\\\"]\",\"triggerType\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:15:05',17),(479,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/6','127.0.0.1','内网IP','6 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:15:12',42),(480,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/6','127.0.0.1','内网IP','6 ',NULL,1,'该计划已生成未完结的工单「WO202607170020」（当前状态：已派工），请先完成或撤销该工单后再生成新工单','2026-07-17 22:15:15',11),(481,'工单完工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.complete()','PUT',1,'admin','研发部门','/dms/workorder/complete','127.0.0.1','内网IP','{\"completeRemark\":\"222222222222222222\",\"completeTime\":\"2026-07-17 22:29:14\",\"deviationReason\":\"\",\"downtimeDuration\":5,\"faultCause\":\"ttttttttttttt\",\"orderId\":27,\"orderNo\":\"WO202607170019\",\"orderStatus\":\"4\",\"orderType\":\"0\",\"params\":{},\"repairMeasure\":\"22222222222222222\",\"sparePartsUsed\":\"液压油泵×1\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:29:14',18),(482,'工单验收',2,'com.ruoyi.dms.controller.DmsWorkOrderController.verify()','PUT',1,'admin','研发部门','/dms/workorder/verify','127.0.0.1','内网IP','{\"orderId\":27,\"orderNo\":\"WO202607170019\",\"orderStatus\":\"5\",\"params\":{},\"rating\":5,\"sparePartsUsed\":\"液压油泵×1\",\"verifierId\":2,\"verifierName\":\"李华\",\"verifyOpinion\":\"1111111111111\",\"verifyTime\":\"2026-07-17 22:29:23\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:29:23',23),(483,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/6','127.0.0.1','内网IP','6 ',NULL,1,'该计划已生成未完结的工单「WO202607170020」（当前状态：已派工），请先完成或撤销该工单后再生成新工单','2026-07-17 22:38:06',48),(484,'工单撤销',2,'com.ruoyi.dms.controller.DmsWorkOrderController.cancel()','PUT',1,'admin','研发部门','/dms/workorder/cancel/28','127.0.0.1','内网IP','28 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:38:34',21),(485,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/6','127.0.0.1','内网IP','6 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:38:34',35),(486,'工单撤销',2,'com.ruoyi.dms.controller.DmsWorkOrderController.cancel()','PUT',1,'admin','研发部门','/dms/workorder/cancel/29','127.0.0.1','内网IP','29 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:44:13',37),(487,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/6','127.0.0.1','内网IP','6 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:44:13',55),(488,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/6','127.0.0.1','内网IP','6 ',NULL,1,'该计划已生成未完结的工单「WO202607170022」（当前状态：已派工），请先完成或撤销该工单后再生成新工单','2026-07-17 22:46:02',12),(489,'工单管理',3,'com.ruoyi.dms.controller.DmsWorkOrderController.remove()','DELETE',1,'admin','研发部门','/dms/workorder/29','127.0.0.1','内网IP','[29] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:46:08',11),(490,'工单管理',3,'com.ruoyi.dms.controller.DmsWorkOrderController.remove()','DELETE',1,'admin','研发部门','/dms/workorder/30,28','127.0.0.1','内网IP','[30,28] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:46:15',9),(491,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/6','127.0.0.1','内网IP','6 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:46:20',35),(492,'工单接单',2,'com.ruoyi.dms.controller.DmsWorkOrderController.accept()','PUT',1,'admin','研发部门','/dms/workorder/accept/31','127.0.0.1','内网IP','31 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:46:39',14),(493,'PM生成工单',1,'com.ruoyi.dms.controller.DmsPmPlanController.generateWorkOrder()','POST',1,'admin','研发部门','/dms/pmplan/generate/6','127.0.0.1','内网IP','6 ',NULL,1,'该计划已生成未完结的工单「WO202607170023」（当前状态：已接单），请先完成或撤销该工单后再生成新工单','2026-07-17 22:47:02',5),(494,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"cycleType\":\"0\",\"cycleValue\":1,\"delFlag\":\"0\",\"deptId\":104,\"deptName\":\"市场部门\",\"equipmentIds\":\"[7]\",\"inspectionItems\":\"{\\\"common\\\":[],\\\"devices\\\":[{\\\"equipmentId\\\":7,\\\"equipmentName\\\":\\\"TEST-EQ-T - 测试设备T\\\",\\\"items\\\":[{\\\"item\\\":\\\"温度检查\\\",\\\"type\\\":\\\"number\\\",\\\"unit\\\":\\\"度\\\",\\\"abnormalRequired\\\":true}]}]}\",\"params\":{},\"routeCode\":\"RT20260717005\",\"routeId\":14,\"routeName\":\"测试巡检路线1\",\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:47:58',16),(495,'点检任务',1,'com.ruoyi.dms.controller.DmsInspectionTaskController.add()','POST',1,'admin','研发部门','/dms/inspection/task','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"inspectorId\":2,\"inspectorName\":\"李华\",\"params\":{},\"planDate\":\"2026-07-17\",\"routeId\":14,\"routeName\":\"测试巡检路线1\",\"taskId\":7,\"taskNo\":\"INS20260717224819\",\"taskStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:48:19',12),(496,'点检任务完成',2,'com.ruoyi.dms.controller.DmsInspectionTaskController.complete()','PUT',1,'admin','研发部门','/dms/inspection/task/complete','127.0.0.1','内网IP','{\"abnormalCount\":1,\"completeTime\":\"2026-07-17 22:48:42\",\"inspectorId\":2,\"inspectorName\":\"李华\",\"params\":{},\"photoUrls\":\"[\\\"/profile/upload/2026/07/17/Surface_Laptop_3_02_20260717224841A001.jpg\\\"]\",\"resultData\":\"{\\\"common\\\":[],\\\"devices\\\":[{\\\"equipmentId\\\":7,\\\"equipmentName\\\":\\\"TEST-EQ-T - 测试设备T\\\",\\\"items\\\":[{\\\"item\\\":\\\"温度检查\\\",\\\"type\\\":\\\"number\\\",\\\"unit\\\":\\\"度\\\",\\\"result\\\":\\\"ok\\\",\\\"value\\\":90,\\\"abnormal\\\":true,\\\"abnormalDesc\\\":\\\"过高温度\\\"}]}]}\",\"taskId\":7,\"taskNo\":\"INS20260717224819\",\"taskStatus\":\"2\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:48:42',22),(497,'工单派工',2,'com.ruoyi.dms.controller.DmsWorkOrderController.dispatch()','PUT',1,'admin','研发部门','/dms/workorder/dispatch','127.0.0.1','内网IP','{\"assignTime\":\"2026-07-17 22:50:05\",\"assigneeId\":100,\"assigneeName\":\"张涛\",\"assignerId\":2,\"assignerName\":\"李华\",\"equipmentName\":\"TEST-EQ-T - 测试设备T\",\"orderId\":32,\"orderNo\":\"WO202607170024\",\"orderStatus\":\"1\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:50:05',24),(498,'工单撤销',2,'com.ruoyi.dms.controller.DmsWorkOrderController.cancel()','PUT',1,'admin','研发部门','/dms/workorder/cancel/32','127.0.0.1','内网IP','32 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:50:11',12),(499,'数据记录',2,'com.ruoyi.dms.controller.DmsDataRecordController.edit()','PUT',1,'admin','研发部门','/dms/data/record','127.0.0.1','内网IP','{\"collectMode\":\"0\",\"collectTime\":\"2026-07-16 21:45:29\",\"createTime\":\"2026-07-16 21:45:35\",\"equipmentId\":1,\"equipmentName\":\"机床01\",\"param1Value\":7,\"params\":{},\"productCount\":999,\"recordId\":1,\"runHours\":200,\"runStatus\":\"1\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:51:48',15),(500,'点检任务开始',2,'com.ruoyi.dms.controller.DmsInspectionTaskController.start()','PUT',1,'admin','研发部门','/dms/inspection/task/start/7','127.0.0.1','内网IP','7 ',NULL,1,'当前状态不允许开始执行','2026-07-17 22:54:26',15),(501,'点检任务',1,'com.ruoyi.dms.controller.DmsInspectionTaskController.add()','POST',1,'admin','研发部门','/dms/inspection/task','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"inspectorId\":1,\"inspectorName\":\"admin\",\"params\":{},\"planDate\":\"2026-07-17\",\"routeId\":14,\"routeName\":\"测试巡检路线1\",\"taskId\":8,\"taskNo\":\"INS20260717225543\",\"taskStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:55:43',17),(502,'点检任务开始',2,'com.ruoyi.dms.controller.DmsInspectionTaskController.start()','PUT',1,'admin','研发部门','/dms/inspection/task/start/8','127.0.0.1','内网IP','8 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 22:55:43',10),(503,'点检任务开始',2,'com.ruoyi.dms.controller.DmsInspectionTaskController.start()','PUT',1,'admin','研发部门','/dms/inspection/task/start/8','127.0.0.1','内网IP','8 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 23:01:57',10),(504,'点检任务完成',2,'com.ruoyi.dms.controller.DmsInspectionTaskController.complete()','PUT',1,'admin','研发部门','/dms/inspection/task/complete','127.0.0.1','内网IP','{\"abnormalCount\":0,\"completeTime\":\"2026-07-17 23:02:05\",\"inspectorId\":1,\"inspectorName\":\"admin\",\"params\":{},\"resultData\":\"{\\\"common\\\":[],\\\"devices\\\":[{\\\"equipmentId\\\":7,\\\"equipmentName\\\":\\\"TEST-EQ-T - 测试设备T\\\",\\\"items\\\":[{\\\"item\\\":\\\"温度检查\\\",\\\"type\\\":\\\"number\\\",\\\"unit\\\":\\\"度\\\",\\\"result\\\":\\\"ok\\\",\\\"value\\\":2222,\\\"abnormal\\\":false,\\\"abnormalDesc\\\":\\\"\\\"}]}]}\",\"taskId\":8,\"taskNo\":\"INS20260717225543\",\"taskStatus\":\"2\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 23:02:05',20),(505,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":5.00,\"beforeStock\":0.00,\"documentCode\":\"BJIN20260717001\",\"moveType\":\"0\",\"operateDate\":\"2026-07-17\",\"operatorName\":\"系统管理员\",\"params\":{},\"partCode\":\"BJ20260716003\",\"partId\":9,\"partName\":\"电机皮带\",\"quantity\":5,\"recordId\":17,\"sourceType\":\"0\",\"storageLocation\":\"备件库\",\"supplierOrDept\":\"东莞电子科技有限公司\",\"targetType\":\"4\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 23:32:18',33),(506,'备件出入库',2,'com.ruoyi.dms.controller.DmsSparePartController.stockMove()','POST',1,'admin','研发部门','/dms/sparepart/stockMove','127.0.0.1','内网IP','{\"afterStock\":205.00,\"beforeStock\":5.00,\"documentCode\":\"BJIN20260717002\",\"moveType\":\"0\",\"operateDate\":\"2026-07-17\",\"operatorName\":\"系统管理员\",\"params\":{},\"partCode\":\"BJ20260716003\",\"partId\":9,\"partName\":\"电机皮带\",\"quantity\":200,\"recordId\":18,\"sourceType\":\"1\",\"storageLocation\":\"备件库\",\"supplierOrDept\":\"东莞电子科技有限公司\",\"targetType\":\"4\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-17 23:41:11',6),(507,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2026-07-13 11:25:52\",\"icon\":\"monitor\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2,\"menuName\":\"系统监控\",\"menuType\":\"M\",\"orderNum\":15,\"params\":{},\"parentId\":1,\"path\":\"monitor\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-18 09:01:17',29),(508,'开放接口应用',2,'com.ruoyi.web.controller.system.OpenApiAppController.edit()','PUT',1,'admin','研发部门','/system/openapi','127.0.0.1','内网IP','{\"allowedModules\":\"*\",\"appId\":1,\"appKey\":\"ak-1784388283197-579\",\"appName\":\"默认测试应用\",\"createBy\":\"admin\",\"createTime\":\"2026-07-18 23:12:10\",\"delFlag\":\"0\",\"params\":{},\"remark\":\"默认测试Key，生产环境请替换\",\"status\":\"0\",\"updateBy\":\"admin\",\"updateTime\":\"2026-07-18 23:24:53\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-18 23:24:53',14),(509,'线索领取',2,'com.ruoyi.mk.controller.MkLeadController.receive()','PUT',1,'admin','研发部门','/mk/lead/receive/5','127.0.0.1','内网IP','5 {\"deptId\":103,\"isPublic\":\"0\",\"leadId\":5,\"params\":{},\"receiveTime\":\"2026-07-19 13:47:48\",\"updateBy\":\"admin\",\"userId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-19 13:47:48',23),(510,'线索领取',2,'com.ruoyi.mk.controller.MkLeadController.receive()','PUT',1,'admin','研发部门','/mk/lead/receive/4','127.0.0.1','内网IP','4 {\"deptId\":103,\"isPublic\":\"0\",\"leadId\":4,\"params\":{},\"receiveTime\":\"2026-07-19 16:01:21\",\"updateBy\":\"admin\",\"userId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-19 16:01:21',37),(511,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2026-07-19 13:27:35\",\"icon\":\"money\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":4000,\"menuName\":\"营销管理\",\"menuType\":\"M\",\"orderNum\":7,\"params\":{},\"parentId\":0,\"path\":\"mk\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-19 16:03:42',21),(512,'保存菜单排序',2,'com.ruoyi.web.controller.system.SysMenuController.updateSort()','PUT',1,'admin','研发部门','/system/menu/updateSort','127.0.0.1','内网IP','{\"menuIds\":\"4004\",\"orderNums\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-19 16:04:10',5),(513,'菜单管理',3,'com.ruoyi.web.controller.system.SysMenuController.remove()','DELETE',1,'admin','研发部门','/system/menu/4130','127.0.0.1','内网IP','4130 ','{\"msg\":\"存在子菜单,不允许删除\",\"code\":601}',0,NULL,'2026-07-19 16:04:54',3),(514,'菜单管理',3,'com.ruoyi.web.controller.system.SysMenuController.remove()','DELETE',1,'admin','研发部门','/system/menu/4086','127.0.0.1','内网IP','4086 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-19 16:05:10',36),(515,'线索转化',2,'com.ruoyi.mk.controller.MkLeadController.convert()','PUT',1,'admin','研发部门','/mk/lead/convert/5','127.0.0.1','内网IP','5 {\"convertCustomerId\":6,\"leadId\":5,\"leadStatus\":\"4\",\"params\":{},\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-19 16:11:21',42),(516,'线索管理',2,'com.ruoyi.mk.controller.MkLeadController.edit()','PUT',1,'admin','研发部门','/mk/lead','127.0.0.1','内网IP','{\"city\":\"青岛市\",\"companyName\":\"青岛海蓝新能源有限公司\",\"companySize\":\"1\",\"contactEmail\":\"zhengkai@hailan-energy.com\",\"contactName\":\"郑凯\",\"contactPhone\":\"13000130001\",\"convertCustomerId\":6,\"convertTime\":\"2026-07-19 16:11:21\",\"createBy\":\"admin\",\"createTime\":\"2026-07-19 13:27:35\",\"delFlag\":\"0\",\"deptId\":105,\"deptName\":\"测试部门\",\"district\":\"崂山区\",\"industry\":\"4\",\"isPublic\":\"0\",\"leadGrade\":\"C\",\"leadId\":5,\"leadNo\":\"LEAD20260719005\",\"leadScore\":52,\"leadSource\":\"0\",\"leadStatus\":\"4\",\"params\":{},\"position\":\"技术总监\",\"province\":\"山东省\",\"receiveTime\":\"2026-07-19 13:47:48\",\"remark\":\"公海线索，待领取\",\"requirementDesc\":\"储能监控系统需求，需对接现有SCADA\",\"updateBy\":\"admin\",\"updateTime\":\"2026-07-19 16:11:21\",\"userId\":2,\"userName\":\"李华\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-19 16:12:11',10),(517,'企业客户',2,'com.ruoyi.mk.controller.MkCustomerController.edit()','PUT',1,'admin','研发部门','/mk/customer','127.0.0.1','内网IP','{\"city\":\"青岛市\",\"companySize\":\"1\",\"createBy\":\"admin\",\"createTime\":\"2026-07-19 16:11:21\",\"customerId\":6,\"customerLevel\":\"4\",\"customerName\":\"青岛海蓝新能源有限公司\",\"customerNo\":\"CUS1784448681541\",\"customerSource\":\"0\",\"customerStatus\":\"0\",\"delFlag\":\"0\",\"deptId\":105,\"deptName\":\"测试部门\",\"district\":\"崂山区\",\"firstContactDate\":\"2026-07-19\",\"industry\":\"4\",\"params\":{},\"province\":\"山东省\",\"updateBy\":\"\",\"userId\":2,\"userName\":\"李华\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-19 16:12:30',41),(518,'线索领取',2,'com.ruoyi.mk.controller.MkLeadController.receive()','PUT',1,'admin','研发部门','/mk/lead/receive/3','127.0.0.1','内网IP','3 {\"deptId\":103,\"isPublic\":\"0\",\"leadId\":3,\"params\":{},\"receiveTime\":\"2026-07-19 16:13:54\",\"updateBy\":\"admin\",\"userId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-19 16:13:54',34),(519,'线索领取',2,'com.ruoyi.mk.controller.MkLeadController.receive()','PUT',1,'admin','研发部门','/mk/lead/receive/1','127.0.0.1','内网IP','1 {\"deptId\":103,\"isPublic\":\"0\",\"leadId\":1,\"params\":{},\"receiveTime\":\"2026-07-19 16:27:20\",\"updateBy\":\"admin\",\"userId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-19 16:27:20',32),(520,'线索领取',2,'com.ruoyi.mk.controller.MkLeadController.receive()','PUT',1,'admin','研发部门','/mk/lead/receive/1','127.0.0.1','内网IP','1 {\"deptId\":103,\"leadId\":1,\"params\":{},\"receiveStatus\":\"1\",\"updateBy\":\"admin\",\"userId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-19 16:32:22',39),(521,'领取审批拒绝',2,'com.ruoyi.mk.controller.MkLeadController.rejectReceive()','PUT',1,'admin','研发部门','/mk/lead/reject/1','127.0.0.1','内网IP','1 {\"leadId\":1,\"params\":{},\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-19 16:56:56',47),(522,'线索领取',2,'com.ruoyi.mk.controller.MkLeadController.receive()','PUT',1,'admin','研发部门','/mk/lead/receive/1','127.0.0.1','内网IP','1 {\"deptId\":103,\"leadId\":1,\"params\":{},\"receiveStatus\":\"1\",\"updateBy\":\"admin\",\"userId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-19 16:56:59',40),(523,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2026-07-13 23:38:37\",\"icon\":\"shopping\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2000,\"menuName\":\"仓储管理\",\"menuType\":\"M\",\"orderNum\":5,\"params\":{},\"parentId\":0,\"path\":\"wms\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 09:06:47',23),(524,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2026-07-15 12:42:45\",\"icon\":\"server\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":3000,\"menuName\":\"设备管理\",\"menuType\":\"M\",\"orderNum\":6,\"params\":{},\"parentId\":0,\"path\":\"dms\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 09:06:54',10),(525,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2026-07-13 23:38:37\",\"icon\":\"scan\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2000,\"menuName\":\"仓储管理\",\"menuType\":\"M\",\"orderNum\":5,\"params\":{},\"parentId\":0,\"path\":\"wms\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 09:08:11',11),(526,'部门管理',3,'com.ruoyi.web.controller.system.SysDeptController.remove()','DELETE',1,'admin','研发部门','/system/dept/108','127.0.0.1','内网IP','108 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 11:25:19',10),(527,'部门管理',3,'com.ruoyi.web.controller.system.SysDeptController.remove()','DELETE',1,'admin','研发部门','/system/dept/109','127.0.0.1','内网IP','109 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 11:25:22',9),(528,'部门管理',3,'com.ruoyi.web.controller.system.SysDeptController.remove()','DELETE',1,'admin','研发部门','/system/dept/102','127.0.0.1','内网IP','102 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 11:25:25',9),(529,'部门管理',2,'com.ruoyi.web.controller.system.SysDeptController.edit()','PUT',1,'admin','研发部门','/system/dept','127.0.0.1','内网IP','{\"ancestors\":\"0,100\",\"children\":[],\"deptId\":101,\"deptName\":\"昆明总公司\",\"leader\":\"若依\",\"orderNum\":1,\"params\":{},\"parentId\":100,\"parentName\":\"若依科技\",\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 11:25:58',15),(530,'部门管理',2,'com.ruoyi.web.controller.system.SysDeptController.edit()','PUT',1,'admin','研发部门','/system/dept','127.0.0.1','内网IP','{\"ancestors\":\"0\",\"children\":[],\"deptId\":100,\"deptName\":\"鼎泰科技有限公司\",\"leader\":\"若依\",\"orderNum\":0,\"params\":{},\"parentId\":0,\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 11:26:12',6),(531,'部门管理',2,'com.ruoyi.web.controller.system.SysDeptController.edit()','PUT',1,'admin','研发部门','/system/dept','127.0.0.1','内网IP','{\"ancestors\":\"0,100,101\",\"children\":[],\"deptId\":105,\"deptName\":\"综合管理部\",\"leader\":\"若依\",\"orderNum\":3,\"params\":{},\"parentId\":101,\"parentName\":\"昆明总公司\",\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 11:26:51',11),(532,'合同管理',1,'com.ruoyi.mk.controller.MkContractController.add()','POST',1,'admin','研发部门','/mk/contract','127.0.0.1','内网IP','{\"contractAmount\":0,\"contractId\":6,\"contractName\":\"fddsfdsf\",\"contractNo\":\"fdf221221\",\"contractStatus\":\"0\",\"contractType\":\"0\",\"customerId\":5,\"customerName\":\"广州速达物流有限公司\",\"delFlag\":\"0\",\"deptId\":105,\"deptName\":\"综合管理部\",\"effectiveDate\":\"2026-07-30\",\"expireDate\":\"2026-08-06\",\"params\":{},\"paymentMethod\":\"0\",\"paymentPlanList\":[{\"actualAmount\":0,\"contractId\":6,\"customerId\":5,\"params\":{},\"paymentStatus\":\"0\",\"periodNo\":1,\"planAmount\":10000,\"planDate\":\"2026-07-23\",\"planId\":6},{\"actualAmount\":0,\"contractId\":6,\"customerId\":5,\"params\":{},\"paymentStatus\":\"0\",\"periodNo\":2,\"planAmount\":20000,\"planDate\":\"2026-07-27\",\"planId\":7}],\"remark\":\"2222\",\"signDate\":\"2026-07-20\",\"userId\":2,\"userName\":\"李华\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 12:27:49',55),(533,'合同管理',2,'com.ruoyi.mk.controller.MkContractController.submit()','PUT',1,'admin','研发部门','/mk/contract/submit/6','127.0.0.1','内网IP','6 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 12:27:55',11),(534,'合同管理',2,'com.ruoyi.mk.controller.MkContractController.reject()','PUT',1,'admin','研发部门','/mk/contract/reject/6','127.0.0.1','内网IP','6 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 12:28:10',10),(535,'合同管理',2,'com.ruoyi.mk.controller.MkContractController.edit()','PUT',1,'admin','研发部门','/mk/contract','127.0.0.1','内网IP','{\"attachment\":\"/profile/upload/2026/07/20/01-应急指挥信息系统项目建设方案V26_20260720122825A001.docx\",\"contractAmount\":0,\"contractId\":6,\"contractName\":\"fddsfdsf\",\"contractNo\":\"fdf221221\",\"contractStatus\":\"0\",\"contractType\":\"0\",\"createTime\":\"2026-07-20 12:27:49\",\"customerId\":5,\"customerName\":\"广州速达物流有限公司\",\"delFlag\":\"0\",\"deptId\":105,\"deptName\":\"综合管理部\",\"effectiveDate\":\"2026-07-30\",\"expireDate\":\"2026-08-06\",\"params\":{},\"paymentMethod\":\"0\",\"paymentPlanList\":[{\"actualAmount\":0,\"contractId\":6,\"contractNo\":\"fdf221221\",\"createTime\":\"2026-07-20 12:27:49\",\"customerId\":5,\"customerName\":\"广州速达物流有限公司\",\"params\":{},\"paymentStatus\":\"0\",\"periodNo\":1,\"planAmount\":10000,\"planDate\":\"2026-07-23\",\"planId\":8,\"updateBy\":\"\"},{\"actualAmount\":0,\"contractId\":6,\"contractNo\":\"fdf221221\",\"createTime\":\"2026-07-20 12:27:49\",\"customerId\":5,\"customerName\":\"广州速达物流有限公司\",\"params\":{},\"paymentStatus\":\"0\",\"periodNo\":2,\"planAmount\":20000,\"planDate\":\"2026-07-27\",\"planId\":9,\"updateBy\":\"\"}],\"remark\":\"2222\",\"signDate\":\"2026-07-20\",\"updateBy\":\"\",\"updateTime\":\"2026-07-20 12:28:10\",\"userId\":2,\"userName\":\"李华\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 12:28:27',13),(536,'合同管理',2,'com.ruoyi.mk.controller.MkContractController.submit()','PUT',1,'admin','研发部门','/mk/contract/submit/6','127.0.0.1','内网IP','6 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 12:28:30',10),(537,'合同管理',2,'com.ruoyi.mk.controller.MkContractController.approve()','PUT',1,'admin','研发部门','/mk/contract/approve/6','127.0.0.1','内网IP','{\"approveOpinion\":\"wwwwwwww\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 13:21:11',23),(538,'合同管理',1,'com.ruoyi.mk.controller.MkContractController.add()','POST',1,'admin','研发部门','/mk/contract','127.0.0.1','内网IP','{\"attachment\":\"/profile/upload/2026/07/20/1_襄阳市公安局项目技术建议书 -养犬管理信息系统_20260720152817A001.docx\",\"contractAmount\":0,\"contractId\":7,\"contractName\":\"44\",\"contractNo\":\"44444\",\"contractStatus\":\"0\",\"contractType\":\"0\",\"customerId\":5,\"customerName\":\"广州速达物流有限公司\",\"delFlag\":\"0\",\"deptId\":103,\"deptName\":\"研发部门\",\"effectiveDate\":\"2026-07-20\",\"expireDate\":\"2026-07-28\",\"params\":{},\"paymentMethod\":\"0\",\"paymentPlanList\":[],\"signDate\":\"2026-07-20\",\"userId\":100,\"userName\":\"张涛\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 15:28:30',37),(539,'合同管理',2,'com.ruoyi.mk.controller.MkContractController.submit()','PUT',1,'admin','研发部门','/mk/contract/submit/7','127.0.0.1','内网IP','7 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 15:28:32',14),(540,'合同管理',2,'com.ruoyi.mk.controller.MkContractController.reject()','PUT',1,'admin','研发部门','/mk/contract/reject/7','127.0.0.1','内网IP','{\"approveOpinion\":\"2222222222\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 15:28:39',14),(541,'合同管理',2,'com.ruoyi.mk.controller.MkContractController.submit()','PUT',1,'admin','研发部门','/mk/contract/submit/7','127.0.0.1','内网IP','7 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 15:29:00',11),(542,'合同管理',2,'com.ruoyi.mk.controller.MkContractController.approve()','PUT',1,'admin','研发部门','/mk/contract/approve/7','127.0.0.1','内网IP','{\"approveOpinion\":\"5555555555555555555555555555\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-20 15:29:21',11),(543,'编号规则',1,'com.ruoyi.mk.controller.MkNumberRuleController.add()','POST',1,'admin','研发部门','/mk/numberRule','127.0.0.1','内网IP','{\"connector\":\"\",\"currentDateStr\":\"\",\"currentSeq\":0,\"dateFormat\":\"yyyyMMdd\",\"params\":{},\"prefix\":\"HT\",\"remark\":\"??????\",\"resetType\":\"1\",\"ruleCode\":\"contract\",\"ruleId\":6,\"ruleName\":\"??????\",\"seqLength\":4,\"seqStart\":1,\"status\":\"0\",\"step\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:06:39',32),(544,'编号规则',2,'com.ruoyi.mk.controller.MkNumberRuleController.edit()','PUT',1,'admin','研发部门','/mk/numberRule','127.0.0.1','内网IP','{\"dateFormat\":\"yyyyMMdd\",\"params\":{},\"prefix\":\"HT\",\"remark\":\"??????\",\"resetType\":\"1\",\"ruleCode\":\"contract\",\"ruleId\":6,\"ruleName\":\"????(???)\",\"seqLength\":6,\"seqStart\":1,\"status\":\"0\",\"step\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:06:57',14),(545,'编号规则',3,'com.ruoyi.mk.controller.MkNumberRuleController.remove()','DELETE',1,'admin','研发部门','/mk/numberRule/6','127.0.0.1','内网IP','[6] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:06:58',8),(546,'营销活动',1,'com.ruoyi.mk.controller.MkActivityController.add()','POST',1,'admin','研发部门','/mk/activity','127.0.0.1','内网IP','{\"activityForm\":\"0\",\"activityId\":6,\"activityName\":\"????????1\",\"activityNo\":\"HD202607210001\",\"activityStatus\":\"0\",\"activityType\":\"0\",\"delFlag\":\"0\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:07:15',41),(547,'营销活动',1,'com.ruoyi.mk.controller.MkActivityController.add()','POST',1,'admin','研发部门','/mk/activity','127.0.0.1','内网IP','{\"activityForm\":\"0\",\"activityId\":7,\"activityName\":\"????????2\",\"activityNo\":\"HD202607210002\",\"activityStatus\":\"0\",\"activityType\":\"0\",\"delFlag\":\"0\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:07:16',13),(548,'企业客户',1,'com.ruoyi.mk.controller.MkCustomerController.add()','POST',1,'admin','研发部门','/mk/customer','127.0.0.1','内网IP','{\"customerId\":7,\"customerLevel\":\"3\",\"customerName\":\"????A\",\"customerNo\":\"CUS2026072100001\",\"customerStatus\":\"0\",\"delFlag\":\"0\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:08:22',25),(549,'企业客户',1,'com.ruoyi.mk.controller.MkCustomerController.add()','POST',1,'admin','研发部门','/mk/customer','127.0.0.1','内网IP','{\"customerId\":8,\"customerLevel\":\"3\",\"customerName\":\"????B\",\"customerNo\":\"CUS2026072100002\",\"customerStatus\":\"0\",\"delFlag\":\"0\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:08:22',11),(550,'线索管理',1,'com.ruoyi.mk.controller.MkLeadController.add()','POST',1,'admin','研发部门','/mk/lead','127.0.0.1','内网IP','{\"companyName\":\"??????\",\"contactName\":\"?????\",\"contactPhone\":\"13800000001\",\"delFlag\":\"0\",\"isPublic\":\"1\",\"leadGrade\":\"D\",\"leadId\":6,\"leadNo\":\"XS2026072100001\",\"leadScore\":0,\"leadStatus\":\"0\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:08:40',27),(551,'商机管理',1,'com.ruoyi.mk.controller.MkOpportunityController.add()','POST',1,'admin','研发部门','/mk/opportunity','127.0.0.1','内网IP','{\"customerId\":7,\"delFlag\":\"0\",\"expectedAmount\":10000,\"expectedDate\":\"2026-08-01\",\"opportunityId\":6,\"opportunityName\":\"????\",\"opportunityNo\":\"SJ2026072100001\",\"opportunityStatus\":\"0\",\"params\":{},\"stageCode\":\"stage1\",\"stageName\":\"????\",\"weightedAmount\":1000,\"winRate\":10} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:08:40',27),(552,'订单管理',1,'com.ruoyi.mk.controller.MkOrderController.add()','POST',1,'admin','研发部门','/mk/order','127.0.0.1','内网IP','{\"customerId\":7,\"customerName\":\"????A\",\"delFlag\":\"0\",\"itemList\":[],\"orderAmount\":5000,\"orderId\":6,\"orderNo\":\"DD2026072100001\",\"orderStatus\":\"0\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:08:40',22),(553,'营销活动',1,'com.ruoyi.mk.controller.MkActivityController.add()','POST',1,'admin','研发部门','/mk/activity','127.0.0.1','内网IP','{\"activityForm\":\"0\",\"activityId\":8,\"activityName\":\"???????3\",\"activityNo\":\"HD202607210003\",\"activityStatus\":\"0\",\"activityType\":\"0\",\"delFlag\":\"0\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:10:01',16),(554,'线索转化',2,'com.ruoyi.mk.controller.MkLeadController.convert()','PUT',1,'admin','研发部门','/mk/lead/convert/6','127.0.0.1','内网IP','6 {\"convertCustomerId\":9,\"leadId\":6,\"leadStatus\":\"4\",\"params\":{},\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:10:33',20),(555,'营销活动',3,'com.ruoyi.mk.controller.MkActivityController.remove()','DELETE',1,'admin','研发部门','/mk/activity/6,7,8','127.0.0.1','内网IP','[6,7,8] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:11:06',9),(556,'企业客户',3,'com.ruoyi.mk.controller.MkCustomerController.remove()','DELETE',1,'admin','研发部门','/mk/customer/7,8,9','127.0.0.1','内网IP','[7,8,9] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:11:07',5),(557,'线索管理',3,'com.ruoyi.mk.controller.MkLeadController.remove()','DELETE',1,'admin','研发部门','/mk/lead/6','127.0.0.1','内网IP','[6] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:11:07',4),(558,'商机管理',3,'com.ruoyi.mk.controller.MkOpportunityController.remove()','DELETE',1,'admin','研发部门','/mk/opportunity/6','127.0.0.1','内网IP','[6] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:11:07',5),(559,'订单管理',3,'com.ruoyi.mk.controller.MkOrderController.remove()','DELETE',1,'admin','研发部门','/mk/order/6','127.0.0.1','内网IP','[6] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:11:07',19),(560,'编号规则',2,'com.ruoyi.mk.controller.MkNumberRuleController.edit()','PUT',1,'admin','研发部门','/mk/numberRule','127.0.0.1','内网IP','{\"connector\":\"\",\"createBy\":\"admin\",\"createTime\":\"2026-07-21 20:33:52\",\"currentDateStr\":\"20260721\",\"currentSeq\":3,\"dateFormat\":\"yyyy\",\"params\":{},\"prefix\":\"CUS\",\"preview\":\"CUS2026072100001\",\"remark\":\"企业客户编号，每日重置\",\"resetType\":\"3\",\"ruleCode\":\"customer\",\"ruleId\":2,\"ruleName\":\"客户编号\",\"seqLength\":3,\"seqStart\":1,\"status\":\"0\",\"step\":1,\"updateBy\":\"\",\"updateTime\":\"2026-07-21 21:10:33\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:30:50',9),(561,'编号规则',2,'com.ruoyi.mk.controller.MkNumberRuleController.edit()','PUT',1,'admin','研发部门','/mk/numberRule','127.0.0.1','内网IP','{\"connector\":\"\",\"createBy\":\"admin\",\"createTime\":\"2026-07-21 20:33:52\",\"currentDateStr\":\"20260721\",\"currentSeq\":3,\"dateFormat\":\"yyyyMM\",\"params\":{},\"prefix\":\"HD\",\"preview\":\"HD202607210001\",\"remark\":\"营销活动编号，每日重置\",\"resetType\":\"2\",\"ruleCode\":\"activity\",\"ruleId\":1,\"ruleName\":\"活动编号\",\"seqLength\":3,\"seqStart\":1,\"status\":\"0\",\"step\":1,\"updateBy\":\"\",\"updateTime\":\"2026-07-21 21:10:01\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:31:51',5),(562,'编号规则',2,'com.ruoyi.mk.controller.MkNumberRuleController.edit()','PUT',1,'admin','研发部门','/mk/numberRule','127.0.0.1','内网IP','{\"connector\":\"\",\"createBy\":\"admin\",\"createTime\":\"2026-07-21 20:33:52\",\"currentDateStr\":\"20260721\",\"currentSeq\":1,\"dateFormat\":\"yyyyMMdd\",\"params\":{},\"prefix\":\"XS\",\"preview\":\"XS2026072100001\",\"remark\":\"线索编号，每日重置\",\"resetType\":\"1\",\"ruleCode\":\"lead\",\"ruleId\":3,\"ruleName\":\"线索编号\",\"seqLength\":3,\"seqStart\":1,\"status\":\"0\",\"step\":1,\"updateBy\":\"\",\"updateTime\":\"2026-07-21 21:08:40\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:32:00',7),(563,'编号规则',2,'com.ruoyi.mk.controller.MkNumberRuleController.edit()','PUT',1,'admin','研发部门','/mk/numberRule','127.0.0.1','内网IP','{\"connector\":\"\",\"createBy\":\"admin\",\"createTime\":\"2026-07-21 20:33:52\",\"currentDateStr\":\"20260721\",\"currentSeq\":1,\"dateFormat\":\"yyyyMMdd\",\"params\":{},\"prefix\":\"SJ\",\"preview\":\"SJ2026072100001\",\"remark\":\"商机编号，每日重置\",\"resetType\":\"1\",\"ruleCode\":\"opportunity\",\"ruleId\":4,\"ruleName\":\"商机编号\",\"seqLength\":3,\"seqStart\":1,\"status\":\"0\",\"step\":1,\"updateBy\":\"\",\"updateTime\":\"2026-07-21 21:08:40\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:32:08',9),(564,'编号规则',2,'com.ruoyi.mk.controller.MkNumberRuleController.edit()','PUT',1,'admin','研发部门','/mk/numberRule','127.0.0.1','内网IP','{\"connector\":\"\",\"createBy\":\"admin\",\"createTime\":\"2026-07-21 20:33:52\",\"currentDateStr\":\"20260721\",\"currentSeq\":1,\"dateFormat\":\"yyyyMMdd\",\"params\":{},\"prefix\":\"DD\",\"preview\":\"DD2026072100001\",\"remark\":\"订单编号，每日重置\",\"resetType\":\"1\",\"ruleCode\":\"order\",\"ruleId\":5,\"ruleName\":\"订单编号\",\"seqLength\":3,\"seqStart\":1,\"status\":\"0\",\"step\":1,\"updateBy\":\"\",\"updateTime\":\"2026-07-21 21:08:40\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:32:19',9),(565,'编号规则',2,'com.ruoyi.mk.controller.MkNumberRuleController.edit()','PUT',1,'admin','研发部门','/mk/numberRule','127.0.0.1','内网IP','{\"connector\":\"\",\"createBy\":\"admin\",\"createTime\":\"2026-07-21 20:33:52\",\"currentDateStr\":\"20260721\",\"currentSeq\":3,\"dateFormat\":\"yyyyMM\",\"params\":{},\"prefix\":\"HD\",\"preview\":\"HD202607001\",\"remark\":\"营销活动编号，每月重置\",\"resetType\":\"2\",\"ruleCode\":\"activity\",\"ruleId\":1,\"ruleName\":\"活动编号\",\"seqLength\":3,\"seqStart\":1,\"status\":\"0\",\"step\":1,\"updateBy\":\"\",\"updateTime\":\"2026-07-21 21:31:51\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:34:35',9),(566,'编号规则',2,'com.ruoyi.mk.controller.MkNumberRuleController.edit()','PUT',1,'admin','研发部门','/mk/numberRule','127.0.0.1','内网IP','{\"connector\":\"\",\"createBy\":\"admin\",\"createTime\":\"2026-07-21 20:33:52\",\"currentDateStr\":\"20260721\",\"currentSeq\":3,\"dateFormat\":\"yyyy\",\"params\":{},\"prefix\":\"CUS\",\"preview\":\"CUS2026001\",\"remark\":\"企业客户编号，每年重置\",\"resetType\":\"3\",\"ruleCode\":\"customer\",\"ruleId\":2,\"ruleName\":\"客户编号\",\"seqLength\":3,\"seqStart\":1,\"status\":\"0\",\"step\":1,\"updateBy\":\"\",\"updateTime\":\"2026-07-21 21:30:50\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:34:42',10),(567,'合同管理',1,'com.ruoyi.mk.controller.MkContractController.add()','POST',1,'admin','研发部门','/mk/contract','127.0.0.1','内网IP','{\"contractAmount\":10000,\"contractName\":\"????????\",\"contractStatus\":\"0\",\"contractType\":\"0\",\"customerId\":1,\"customerName\":\"????\",\"delFlag\":\"0\",\"effectiveDate\":\"2026-07-21\",\"expireDate\":\"2027-07-21\",\"params\":{},\"paymentMethod\":\"0\",\"signDate\":\"2026-07-21\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Column \'contract_no\' cannot be null\r\n### The error may exist in URL [jar:file:/C:/Users/Laptop/.m2/repository/com/ruoyi/ruoyi-mk/3.9.2/ruoyi-mk-3.9.2.jar!/mapper/mk/MkContractMapper.xml]\r\n### The error may involve com.ruoyi.mk.mapper.MkContractMapper.insertContract-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO mk_contract (contract_no, contract_name, contract_type, customer_id, opportunity_id,             contract_amount, sign_date, effective_date, expire_date, payment_method, contract_status,             user_id, dept_id, attachment, create_by, create_time, remark)         VALUES (?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, sysdate(), ?)\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Column \'contract_no\' cannot be null\n; Column \'contract_no\' cannot be null','2026-07-21 21:50:25',167),(568,'合同管理',1,'com.ruoyi.mk.controller.MkContractController.add()','POST',1,'admin','研发部门','/mk/contract','127.0.0.1','内网IP','{\"contractAmount\":10000,\"contractName\":\"????????\",\"contractStatus\":\"0\",\"contractType\":\"0\",\"customerId\":1,\"customerName\":\"????\",\"delFlag\":\"0\",\"effectiveDate\":\"2026-07-21\",\"expireDate\":\"2027-07-21\",\"params\":{},\"paymentMethod\":\"0\",\"signDate\":\"2026-07-21\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Column \'contract_no\' cannot be null\r\n### The error may exist in URL [jar:file:/C:/Users/Laptop/.m2/repository/com/ruoyi/ruoyi-mk/3.9.2/ruoyi-mk-3.9.2.jar!/mapper/mk/MkContractMapper.xml]\r\n### The error may involve com.ruoyi.mk.mapper.MkContractMapper.insertContract-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO mk_contract (contract_no, contract_name, contract_type, customer_id, opportunity_id,             contract_amount, sign_date, effective_date, expire_date, payment_method, contract_status,             user_id, dept_id, attachment, create_by, create_time, remark)         VALUES (?, ?, ?, ?, ?,             ?, ?, ?, ?, ?, ?,             ?, ?, ?, ?, sysdate(), ?)\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Column \'contract_no\' cannot be null\n; Column \'contract_no\' cannot be null','2026-07-21 21:54:46',140),(569,'合同管理',1,'com.ruoyi.mk.controller.MkContractController.add()','POST',1,'admin','研发部门','/mk/contract','127.0.0.1','内网IP','{\"contractAmount\":10000,\"contractId\":8,\"contractName\":\"????????\",\"contractNo\":\"HT2026072100001\",\"contractStatus\":\"0\",\"contractType\":\"0\",\"customerId\":1,\"customerName\":\"????\",\"delFlag\":\"0\",\"effectiveDate\":\"2026-07-21\",\"expireDate\":\"2027-07-21\",\"params\":{},\"paymentMethod\":\"0\",\"signDate\":\"2026-07-21\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 21:59:35',36),(570,'合同管理',1,'com.ruoyi.mk.controller.MkContractController.add()','POST',1,'admin','研发部门','/mk/contract','127.0.0.1','内网IP','{\"contractAmount\":20000,\"contractId\":9,\"contractName\":\"????????2\",\"contractNo\":\"HT2026072100002\",\"contractStatus\":\"0\",\"contractType\":\"0\",\"customerId\":1,\"customerName\":\"????\",\"delFlag\":\"0\",\"effectiveDate\":\"2026-07-21\",\"expireDate\":\"2027-07-21\",\"params\":{},\"paymentMethod\":\"0\",\"signDate\":\"2026-07-21\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:00:38',10),(571,'编号规则',2,'com.ruoyi.mk.controller.MkNumberRuleController.edit()','PUT',1,'admin','研发部门','/mk/numberRule','127.0.0.1','内网IP','{\"connector\":\"\",\"createBy\":\"admin\",\"createTime\":\"2026-07-21 21:35:57\",\"currentDateStr\":\"20260721\",\"currentSeq\":2,\"dateFormat\":\"yyyy\",\"params\":{},\"prefix\":\"HT\",\"preview\":\"HT2026072100001\",\"remark\":\"合同编号，每年重置\",\"resetType\":\"1\",\"ruleCode\":\"contract\",\"ruleId\":7,\"ruleName\":\"合同编号\",\"seqLength\":3,\"seqStart\":1,\"status\":\"0\",\"step\":1,\"updateBy\":\"\",\"updateTime\":\"2026-07-21 22:00:38\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:02:45',20),(572,'编号规则',2,'com.ruoyi.mk.controller.MkNumberRuleController.edit()','PUT',1,'admin','研发部门','/mk/numberRule','127.0.0.1','内网IP','{\"connector\":\"\",\"createBy\":\"admin\",\"createTime\":\"2026-07-21 21:35:57\",\"currentDateStr\":\"\",\"currentSeq\":0,\"dateFormat\":\"yyyy\",\"params\":{},\"prefix\":\"HT\",\"preview\":\"HT20260721001\",\"remark\":\"合同编号，每年重置\",\"resetType\":\"3\",\"ruleCode\":\"contract\",\"ruleId\":7,\"ruleName\":\"合同编号\",\"seqLength\":3,\"seqStart\":1,\"status\":\"0\",\"step\":1,\"updateBy\":\"\",\"updateTime\":\"2026-07-21 22:02:45\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:02:53',12),(573,'编号规则',2,'com.ruoyi.mk.controller.MkNumberRuleController.edit()','PUT',1,'admin','研发部门','/mk/numberRule','127.0.0.1','内网IP','{\"connector\":\"\",\"createBy\":\"admin\",\"createTime\":\"2026-07-21 20:33:52\",\"currentDateStr\":\"20260721\",\"currentSeq\":3,\"dateFormat\":\"yyyyMM\",\"params\":{},\"prefix\":\"HD\",\"preview\":\"HD202607001\",\"remark\":\"营销活动编号，每月重置\",\"resetType\":\"2\",\"ruleCode\":\"activity\",\"ruleId\":1,\"ruleName\":\"活动编号\",\"seqLength\":3,\"seqStart\":1,\"status\":\"0\",\"step\":1,\"updateBy\":\"\",\"updateTime\":\"2026-07-21 21:34:35\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:04:53',6),(574,'设备台账管理',1,'com.ruoyi.dms.controller.DmsEquipmentController.add()','POST',1,'admin','研发部门','/dms/equipment','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"equipmentCode\":\"SB202607210001\",\"equipmentId\":9,\"equipmentName\":\"TestEquipment\",\"equipmentStatus\":\"0\",\"params\":{}} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:40:23',40),(575,'备件管理',1,'com.ruoyi.dms.controller.DmsSparePartController.add()','POST',1,'admin','研发部门','/dms/sparepart','127.0.0.1','内网IP','{\"currentStock\":10,\"delFlag\":\"0\",\"params\":{},\"partCode\":\"BJ202607210001\",\"partId\":12,\"partName\":\"TestSparePart\",\"unit\":\"?\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:41:13',20),(576,'工单管理',1,'com.ruoyi.dms.controller.DmsWorkOrderController.add()','POST',1,'admin','研发部门','/dms/workorder','127.0.0.1','内网IP','{\"createBy\":\"admin\",\"delFlag\":\"0\",\"faultDescription\":\"Test fault\",\"orderId\":33,\"orderNo\":\"WO202607210001\",\"orderStatus\":\"0\",\"orderType\":\"0\",\"params\":{},\"priority\":\"2\",\"reportTime\":\"2026-07-21 22:41:13\",\"slaEscalated\":\"0\",\"slaProcessDeadline\":\"2026-07-22 22:41:13\",\"slaResponseDeadline\":\"2026-07-22 02:41:13\",\"slaTimeoutStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:41:13',43),(577,'巡检路线',1,'com.ruoyi.dms.controller.DmsInspectionRouteController.add()','POST',1,'admin','研发部门','/dms/inspection/route','127.0.0.1','内网IP','{\"cycleType\":\"0\",\"cycleValue\":1,\"delFlag\":\"0\",\"params\":{},\"routeCode\":\"RT202607210001\",\"routeId\":15,\"routeName\":\"TestRoute\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:41:13',20),(578,'物料管理',1,'com.ruoyi.wms.controller.WmsMaterialController.add()','POST',1,'admin','研发部门','/wms/material','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"materialCode\":\"WL2026072100001\",\"materialId\":11,\"materialName\":\"TestMaterial\",\"params\":{},\"unit\":\"?\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:41:50',21),(579,'供应商管理',1,'com.ruoyi.wms.controller.WmsSupplierController.add()','POST',1,'admin','研发部门','/wms/supplier','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"params\":{},\"supplierCode\":\"GYS202607210001\",\"supplierId\":6,\"supplierName\":\"TestSupplier\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:41:50',16),(580,'仓库管理',1,'com.ruoyi.wms.controller.WmsWarehouseController.add()','POST',1,'admin','研发部门','/wms/warehouse','127.0.0.1','内网IP','{\"ancestors\":\"0\",\"delFlag\":\"0\",\"nodeType\":\"1\",\"params\":{},\"parentId\":0,\"warehouseCode\":\"WH001\",\"warehouseName\":\"TestWarehouse\"} ',NULL,1,'\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WH001\' for key \'wms_warehouse.uk_warehouse_code\'\r\n### The error may exist in URL [jar:file:/C:/Users/Laptop/.m2/repository/com/ruoyi/ruoyi-wms/3.9.2/ruoyi-wms-3.9.2.jar!/mapper/wms/WmsWarehouseMapper.xml]\r\n### The error may involve com.ruoyi.wms.mapper.WmsWarehouseMapper.insertWarehouse-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO wms_warehouse (             parent_id, ancestors, warehouse_code, warehouse_name, node_type,             dept_id, manager_id, address, area_type, location_type,             capacity, location_status, status, create_by, create_time, remark         ) VALUES (             ?, ?, ?, ?, ?,             ?, ?, ?, ?, ?,             ?, ?, ?, ?, sysdate(), ?         )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'WH001\' for key \'wms_warehouse.uk_warehouse_code\'\n; Duplicate entry \'WH001\' for key \'wms_warehouse.uk_warehouse_code\'','2026-07-21 22:41:50',160),(581,'入库管理',1,'com.ruoyi.wms.controller.WmsInboundOrderController.add()','POST',1,'admin','研发部门','/wms/inbound','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"detailList\":[],\"orderId\":7,\"orderNo\":\"IN2026072100001\",\"params\":{},\"status\":\"0\",\"totalQty\":0,\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:41:50',17),(582,'仓库管理',1,'com.ruoyi.wms.controller.WmsWarehouseController.add()','POST',1,'admin','研发部门','/wms/warehouse','127.0.0.1','内网IP','{\"ancestors\":\"0\",\"delFlag\":\"0\",\"nodeType\":\"1\",\"params\":{},\"parentId\":0,\"warehouseCode\":\"WH011\",\"warehouseId\":13,\"warehouseName\":\"TestWarehouse\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:44:06',11),(583,'营销活动',1,'com.ruoyi.mk.controller.MkActivityController.add()','POST',1,'admin','研发部门','/mk/activity','127.0.0.1','内网IP','{\"activityForm\":\"0\",\"activityId\":9,\"activityName\":\"fffffffffffffffff\",\"activityNo\":\"HD202607001\",\"activityStatus\":\"0\",\"activityType\":\"0\",\"actualCount\":0,\"budget\":10000,\"content\":\"5555555555555555555555555555555\",\"dealAmount\":0,\"delFlag\":\"0\",\"deptId\":105,\"deptName\":\"综合管理部\",\"endTime\":\"2026-07-29 00:00:00\",\"leadCount\":0,\"location\":\"fffffffffffffffffffffff\",\"mqlCount\":0,\"opportunityCount\":0,\"params\":{},\"sqlCount\":0,\"startTime\":\"2026-07-22 00:00:00\",\"summary\":\"55555\",\"targetCount\":111,\"targetLeadCount\":22,\"userId\":2,\"userName\":\"李华\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:45:11',14),(584,'活动参与人',1,'com.ruoyi.mk.controller.MkActivityParticipantController.add()','POST',1,'admin','研发部门','/mk/participant','127.0.0.1','内网IP','{\"activityId\":9,\"companyName\":\"555\",\"contactEmail\":\"55\",\"contactName\":\"55\",\"contactPhone\":\"55\",\"params\":{},\"participantId\":6,\"participateStatus\":\"0\",\"position\":\"55\",\"signTime\":\"2026-07-21 00:00:00\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:45:27',11),(585,'出库管理',1,'com.ruoyi.wms.controller.WmsOutboundOrderController.add()','POST',1,'admin','研发部门','/wms/outbound','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"detailList\":[],\"orderId\":3,\"orderNo\":\"OUT2026072100001\",\"params\":{},\"status\":\"0\",\"totalQty\":0,\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:45:35',24),(586,'移库管理',1,'com.ruoyi.wms.controller.WmsMoveOrderController.add()','POST',1,'admin','研发部门','/wms/move','127.0.0.1','内网IP','{\"fromLocationId\":1,\"moveNo\":\"MV2026072100001\",\"moveQty\":1,\"params\":{},\"toLocationId\":2,\"warehouseId\":1} ',NULL,1,'源库位不存在该物料库存','2026-07-21 22:45:35',32),(587,'盘点管理',1,'com.ruoyi.wms.controller.WmsStockTakeController.add()','POST',1,'admin','研发部门','/wms/stocktake','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"params\":{},\"planDate\":\"2026-07-21\",\"status\":\"0\",\"takeId\":7,\"takeNo\":\"ST2026072100001\",\"takeType\":\"0\",\"warehouseId\":1} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:45:35',69),(588,'点检任务',1,'com.ruoyi.dms.controller.DmsInspectionTaskController.add()','POST',1,'admin','研发部门','/dms/inspection/task','127.0.0.1','内网IP','{\"delFlag\":\"0\",\"params\":{},\"planDate\":\"2026-07-21\",\"routeId\":1,\"routeName\":\"Test\",\"taskId\":9,\"taskNo\":\"INS202607210001\",\"taskStatus\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 22:46:50',19),(589,'活动状态流转',2,'com.ruoyi.mk.controller.MkActivityController.changeStatus()','PUT',1,'admin','研发部门','/mk/activity/changeStatus/9','127.0.0.1','内网IP','9 {\"toStatus\":\"1\",\"remark\":\"test\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 23:53:00',32),(590,'活动复制',1,'com.ruoyi.mk.controller.MkActivityController.copy()','POST',1,'admin','研发部门','/mk/activity/copy/5','127.0.0.1','内网IP','5 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 23:53:00',19),(591,'批量更新活动状态',2,'com.ruoyi.mk.controller.MkActivityController.batchChangeStatus()','PUT',1,'admin','研发部门','/mk/activity/batchChangeStatus','127.0.0.1','内网IP','{\"activityIds\":[4],\"status\":\"2\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 23:53:00',21),(592,'设为主要联系人',2,'com.ruoyi.mk.controller.MkContactController.setPrimary()','PUT',1,'admin','研发部门','/mk/contact/setPrimary/5','127.0.0.1','内网IP','5 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 23:53:00',17),(593,'批量设置关键联系人',2,'com.ruoyi.mk.controller.MkContactController.batchSetKey()','PUT',1,'admin','研发部门','/mk/contact/batchSetKey','127.0.0.1','内网IP','{\"contactIds\":[5],\"isKey\":\"1\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 23:53:00',6),(594,'联系人分配',2,'com.ruoyi.mk.controller.MkContactController.assign()','PUT',1,'admin','研发部门','/mk/contact/assign/5','127.0.0.1','内网IP','5 {\"ownerUserId\":2} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 23:53:00',5),(595,'参与人签到',2,'com.ruoyi.mk.controller.MkActivityParticipantController.signIn()','PUT',1,'admin','研发部门','/mk/participant/signIn/6','127.0.0.1','内网IP','6 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-07-21 23:53:16',5),(596,'参与人转线索',1,'com.ruoyi.mk.controller.MkActivityParticipantController.convertToLead()','POST',1,'admin','研发部门','/mk/participant/convertToLead/6','127.0.0.1','内网IP','6 ','{\"msg\":\"转线索成功\",\"code\":200,\"leadId\":7}',0,NULL,'2026-07-21 23:53:16',12);
/*!40000 ALTER TABLE `sys_oper_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_post`
--

DROP TABLE IF EXISTS `sys_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='岗位信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_post`
--

LOCK TABLES `sys_post` WRITE;
/*!40000 ALTER TABLE `sys_post` DISABLE KEYS */;
INSERT INTO `sys_post` VALUES (1,'ceo','董事长',1,'0','admin','2026-07-13 11:25:52','',NULL,''),(2,'se','项目经理',2,'0','admin','2026-07-13 11:25:52','',NULL,''),(3,'hr','人力资源',3,'0','admin','2026-07-13 11:25:52','',NULL,''),(4,'user','普通员工',4,'0','admin','2026-07-13 11:25:52','',NULL,'');
/*!40000 ALTER TABLE `sys_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) DEFAULT '1' COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) DEFAULT '1' COMMENT '部门树选择项是否关联显示',
  `status` char(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1,'超级管理员','admin',1,'1',1,1,'0','0','admin','2026-07-13 11:25:52','',NULL,'超级管理员'),(2,'普通角色','common',2,'2',1,1,'0','0','admin','2026-07-13 11:25:52','',NULL,'普通角色'),(100,'普通员工','common_user',3,'5',1,1,'0','0','admin','2026-07-13 11:25:54','',NULL,'普通员工角色模板：个人中心、公告查看'),(101,'财务人员','finance',4,'3',1,1,'0','0','admin','2026-07-13 11:25:54','',NULL,'财务人员角色模板：财务相关菜单权限'),(102,'部门负责人','dept_leader',5,'4',1,1,'0','0','admin','2026-07-13 11:25:54','',NULL,'部门负责人角色模板：部门管理、人员查看'),(103,'业务经理','business_manager',6,'2',1,1,'0','0','admin','2026-07-13 11:25:54','',NULL,'业务经理角色模板：业务相关权限');
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_dept`
--

DROP TABLE IF EXISTS `sys_role_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_dept` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色和部门关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_dept`
--

LOCK TABLES `sys_role_dept` WRITE;
/*!40000 ALTER TABLE `sys_role_dept` DISABLE KEYS */;
INSERT INTO `sys_role_dept` VALUES (2,100),(2,101),(2,105);
/*!40000 ALTER TABLE `sys_role_dept` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_menu`
--

DROP TABLE IF EXISTS `sys_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色和菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_menu`
--

LOCK TABLES `sys_role_menu` WRITE;
/*!40000 ALTER TABLE `sys_role_menu` DISABLE KEYS */;
INSERT INTO `sys_role_menu` VALUES (1,118),(1,119),(1,120),(1,1200),(1,1201),(1,1202),(1,1203),(1,2000),(1,2001),(1,2002),(1,2003),(1,2004),(1,2005),(1,2010),(1,2011),(1,2012),(1,2013),(1,2014),(1,2015),(1,2020),(1,2021),(1,2022),(1,2023),(1,2024),(1,2025),(1,2030),(1,2031),(1,2032),(1,2033),(1,2034),(1,2035),(1,2036),(1,2037),(1,2060),(1,2061),(1,2062),(1,2063),(1,2064),(1,2065),(1,2066),(1,2070),(1,2071),(1,2072),(1,2073),(1,2074),(1,2075),(1,2076),(1,2077),(1,2078),(1,2080),(1,2081),(1,2082),(1,2083),(1,2084),(1,2085),(1,2090),(1,2091),(1,2092),(1,2093),(1,2094),(1,2095),(1,2100),(1,2101),(1,2102),(1,2103),(1,2104),(1,2105),(1,2110),(1,2111),(1,2120),(1,2121),(1,2122),(1,2123),(1,2124),(1,2125),(1,3000),(1,3001),(1,3002),(1,3003),(1,3004),(1,3005),(1,3006),(1,3007),(1,3010),(1,3011),(1,3012),(1,3013),(1,3014),(1,3020),(1,3021),(1,3022),(1,3023),(1,3024),(1,3025),(1,3030),(1,3031),(1,3032),(1,3033),(1,3034),(1,3035),(1,3040),(1,3041),(1,3042),(1,3043),(1,3044),(1,3045),(1,3050),(1,3051),(1,3052),(1,3053),(1,3054),(1,3060),(1,3061),(1,3062),(1,3063),(1,3064),(1,3070),(1,3071),(1,3072),(1,3073),(1,3074),(1,3080),(1,3081),(1,3082),(1,3083),(1,3084),(1,3085),(1,3086),(1,3087),(1,3088),(1,3090),(1,3100),(1,3101),(1,3102),(1,3103),(1,3104),(1,3105),(1,3106),(1,3107),(1,3108),(1,3109),(1,3110),(1,3111),(1,3112),(1,3113),(1,3114),(1,3115),(1,3116),(1,3121),(1,3122),(1,3123),(1,3124),(1,3125),(1,3126),(1,3127),(1,3128),(1,3129),(1,3130),(1,3131),(1,4000),(1,4001),(1,4002),(1,4003),(1,4004),(1,4005),(1,4006),(1,4010),(1,4011),(1,4012),(1,4013),(1,4014),(1,4015),(1,4020),(1,4021),(1,4022),(1,4023),(1,4024),(1,4025),(1,4030),(1,4031),(1,4032),(1,4033),(1,4034),(1,4036),(1,4037),(1,4038),(1,4039),(1,4040),(1,4041),(1,4042),(1,4043),(1,4044),(1,4045),(1,4046),(1,4047),(1,4048),(1,4049),(1,4050),(1,4060),(1,4061),(1,4062),(1,4063),(1,4064),(1,4065),(1,4070),(1,4075),(1,4080),(1,4081),(1,4082),(1,4083),(1,4084),(1,4085),(1,4090),(1,4091),(1,4092),(1,4093),(1,4094),(1,4095),(1,4096),(1,4100),(1,4101),(1,4102),(1,4103),(1,4104),(1,4105),(1,4106),(1,4110),(1,4111),(1,4112),(1,4113),(1,4114),(1,4120),(1,4130),(1,4131),(1,4132),(1,4133),(1,4134),(1,4135),(1,4140),(1,4141),(1,4142),(1,4143),(1,4144),(1,4145),(1,4146),(1,4147),(1,4148),(1,4149),(1,4150),(1,4151),(1,4152),(1,4153),(1,4154),(1,4155),(1,4156),(1,4157),(1,4158),(1,4159),(1,4160),(1,4161),(1,4162),(1,4163),(1,4164),(1,4165),(1,4166),(1,4167),(1,4168),(1,4169),(1,4170),(1,4171),(1,4172),(1,4173),(1,4174),(1,4175),(1,4176),(1,4177),(1,4178),(1,4181),(1,4184),(1,4187),(1,4188),(1,4189),(1,4190),(1,4191),(1,4192),(2,1),(2,2),(2,3),(2,4),(2,100),(2,101),(2,102),(2,103),(2,104),(2,105),(2,106),(2,107),(2,108),(2,109),(2,110),(2,111),(2,112),(2,113),(2,114),(2,115),(2,116),(2,117),(2,500),(2,501),(2,1000),(2,1001),(2,1002),(2,1003),(2,1004),(2,1005),(2,1006),(2,1007),(2,1008),(2,1009),(2,1010),(2,1011),(2,1012),(2,1013),(2,1014),(2,1015),(2,1016),(2,1017),(2,1018),(2,1019),(2,1020),(2,1021),(2,1022),(2,1023),(2,1024),(2,1025),(2,1026),(2,1027),(2,1028),(2,1029),(2,1030),(2,1031),(2,1032),(2,1033),(2,1034),(2,1035),(2,1036),(2,1037),(2,1038),(2,1039),(2,1040),(2,1041),(2,1042),(2,1043),(2,1044),(2,1045),(2,1046),(2,1047),(2,1048),(2,1049),(2,1050),(2,1051),(2,1052),(2,1053),(2,1054),(2,1055),(2,1056),(2,1057),(2,1058),(2,1059),(2,1060),(100,107),(100,1035),(100,1036),(101,105),(101,106),(101,107),(101,500),(101,1025),(101,1030),(101,1035),(101,1036),(101,1039),(101,1041),(102,1),(102,100),(102,103),(102,104),(102,107),(102,500),(102,1000),(102,1004),(102,1016),(102,1017),(102,1018),(102,1019),(102,1020),(102,1024),(102,1035),(102,1036),(102,1039),(102,1041),(103,1),(103,100),(103,101),(103,103),(103,104),(103,105),(103,107),(103,108),(103,500),(103,501),(103,1000),(103,1001),(103,1002),(103,1004),(103,1007),(103,1016),(103,1020),(103,1025),(103,1035),(103,1036),(103,1037),(103,1038),(103,1039),(103,1040),(103,1041),(103,1042),(103,1044);
/*!40000 ALTER TABLE `sys_role_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_undo_log`
--

DROP TABLE IF EXISTS `sys_undo_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_undo_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `entity_type` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '实体类型（表名）',
  `entity_id` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '实体ID',
  `data_snapshot` longtext COLLATE utf8mb4_general_ci COMMENT '数据快照(JSON格式)',
  `operate_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作类型（INSERT/UPDATE/DELETE）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人',
  `create_time` datetime DEFAULT NULL COMMENT '操作时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间(默认保留24小时)',
  PRIMARY KEY (`id`),
  KEY `idx_entity` (`entity_type`,`entity_id`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作撤销日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_undo_log`
--

LOCK TABLES `sys_undo_log` WRITE;
/*!40000 ALTER TABLE `sys_undo_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_undo_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) COLLATE utf8mb4_general_ci DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '手机号码',
  `sex` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '密码',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`),
  KEY `idx_sys_user_status` (`status`),
  KEY `idx_sys_user_dept_id` (`dept_id`),
  KEY `idx_sys_user_phonenumber` (`phonenumber`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,103,'admin','系统管理员','00','ry@163.com','15888888888','1','','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0','127.0.0.1','2026-07-22 11:43:35','2026-07-13 11:25:52','admin','2026-07-13 11:25:52','','2026-07-16 15:35:51','管理员'),(2,105,'ry','李华','00','ry@qq.com','15666666666','1','','$2a$10$vX1/gbBupgoYzEzZtGQZjORkPljp7GaL6AuJu/AjW0dBip.pRTYNS','0','0','127.0.0.1','2026-07-13 11:25:52','2026-07-13 11:25:52','admin','2026-07-13 11:25:52','admin','2026-07-16 15:34:27','测试员'),(100,103,'zt','张涛','00','','','0','','$2a$10$ZOUxh5JboNpvMTTVttTwOugrMgGnpfgvgDemw.vt2euhf06oOIk7C','0','0','',NULL,NULL,'admin','2026-07-16 15:35:28','',NULL,NULL);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_post`
--

DROP TABLE IF EXISTS `sys_user_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_post` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户与岗位关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_post`
--

LOCK TABLES `sys_user_post` WRITE;
/*!40000 ALTER TABLE `sys_user_post` DISABLE KEYS */;
INSERT INTO `sys_user_post` VALUES (1,1),(2,2);
/*!40000 ALTER TABLE `sys_user_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户和角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES (1,1),(2,2),(100,2);
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wms_inbound_order`
--

DROP TABLE IF EXISTS `wms_inbound_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wms_inbound_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '入库单ID',
  `order_no` varchar(64) NOT NULL COMMENT '入库单号',
  `order_type` varchar(20) DEFAULT '0' COMMENT '入库类型（字典 wms_inbound_type）',
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID',
  `warehouse_id` bigint NOT NULL COMMENT '目标仓库ID',
  `status` char(1) DEFAULT '0' COMMENT '状态（0草稿 1待收货 2待上架 3已完成 4已作废）',
  `inbound_date` datetime DEFAULT NULL COMMENT '预计入库日期',
  `complete_date` datetime DEFAULT NULL COMMENT '实际完成日期',
  `total_qty` decimal(18,2) DEFAULT '0.00' COMMENT '总数量',
  `total_amount` decimal(18,2) DEFAULT '0.00' COMMENT '总金额',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_inbound_order_no` (`order_no`),
  KEY `idx_inbound_status` (`status`),
  KEY `idx_inbound_warehouse` (`warehouse_id`),
  KEY `idx_inbound_supplier` (`supplier_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入库单头表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wms_inbound_order`
--

LOCK TABLES `wms_inbound_order` WRITE;
/*!40000 ALTER TABLE `wms_inbound_order` DISABLE KEYS */;
INSERT INTO `wms_inbound_order` VALUES (2,'IN1784001957562','0',4,1,'3',NULL,'2026-07-14 12:16:06',30.00,NULL,'0',NULL,'2026-07-14 12:05:57','','2026-07-14 12:16:05',NULL),(3,'IN1784002789234','0',3,1,'3',NULL,'2026-07-14 12:29:43',8.00,NULL,'0',NULL,'2026-07-14 12:19:49','','2026-07-14 12:29:43',NULL),(4,'IN1784006864761','0',3,1,'3',NULL,'2026-07-14 13:28:14',6.00,NULL,'2',NULL,'2026-07-14 13:27:44','','2026-07-14 13:28:13',NULL),(5,'IN1784008039260','0',NULL,1,'3',NULL,'2026-07-14 13:47:38',6.00,NULL,'2',NULL,'2026-07-14 13:47:19','','2026-07-14 13:47:38',NULL),(6,'IN1784166865317','0',4,1,'1','2026-07-01 00:00:00',NULL,5.00,NULL,'2',NULL,'2026-07-16 09:54:25','','2026-07-16 09:54:41',NULL);
/*!40000 ALTER TABLE `wms_inbound_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wms_inbound_order_detail`
--

DROP TABLE IF EXISTS `wms_inbound_order_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wms_inbound_order_detail` (
  `detail_id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '入库单ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `plan_qty` decimal(18,2) DEFAULT '0.00' COMMENT '计划数量',
  `received_qty` decimal(18,2) DEFAULT '0.00' COMMENT '已收数量',
  `putaway_qty` decimal(18,2) DEFAULT '0.00' COMMENT '已上架数量',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `production_date` date DEFAULT NULL COMMENT '生产日期',
  `expiry_date` date DEFAULT NULL COMMENT '保质期至',
  `location_id` bigint DEFAULT NULL COMMENT '上架库位ID',
  `unit_price` decimal(18,2) DEFAULT '0.00' COMMENT '单价',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`detail_id`),
  KEY `idx_inbound_detail_order` (`order_id`),
  KEY `idx_inbound_detail_material` (`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入库单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wms_inbound_order_detail`
--

LOCK TABLES `wms_inbound_order_detail` WRITE;
/*!40000 ALTER TABLE `wms_inbound_order_detail` DISABLE KEYS */;
INSERT INTO `wms_inbound_order_detail` VALUES (1,2,9,10.00,10.00,10.00,NULL,'2026-07-14',NULL,5,1.00,'0',NULL,'2026-07-14 12:05:57','','2026-07-14 12:15:47',NULL),(2,2,5,20.00,20.00,20.00,'0714001','2026-07-14',NULL,5,2.50,'0',NULL,'2026-07-14 12:05:57','','2026-07-14 12:16:05',NULL),(3,3,5,8.00,8.00,8.00,'0714002','2026-07-14',NULL,3,1.00,'0',NULL,'2026-07-14 12:19:49','','2026-07-14 12:29:43',NULL),(4,4,6,6.00,6.00,6.00,'0714002','2026-07-14',NULL,3,1.00,'0',NULL,'2026-07-14 13:27:44','','2026-07-14 13:28:13',NULL),(5,5,9,6.00,6.00,6.00,NULL,'2026-07-14',NULL,3,2.00,'0',NULL,'2026-07-14 13:47:19','','2026-07-14 13:47:38',NULL),(6,6,8,5.00,0.00,0.00,NULL,NULL,NULL,NULL,0.00,'0',NULL,'2026-07-16 09:54:25','',NULL,NULL);
/*!40000 ALTER TABLE `wms_inbound_order_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wms_inventory`
--

DROP TABLE IF EXISTS `wms_inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wms_inventory` (
  `inventory_id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `batch_no` varchar(64) DEFAULT '' COMMENT '批次号',
  `production_date` date DEFAULT NULL,
  `expiry_date` date DEFAULT NULL,
  `qty` decimal(18,2) DEFAULT '0.00' COMMENT '可用数量',
  `lock_qty` decimal(18,2) DEFAULT '0.00' COMMENT '锁定数量',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`inventory_id`),
  UNIQUE KEY `uk_inventory` (`material_id`,`warehouse_id`,`location_id`,`batch_no`),
  KEY `idx_inventory_material` (`material_id`),
  KEY `idx_inventory_warehouse` (`warehouse_id`),
  KEY `idx_inventory_location` (`location_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wms_inventory`
--

LOCK TABLES `wms_inventory` WRITE;
/*!40000 ALTER TABLE `wms_inventory` DISABLE KEYS */;
INSERT INTO `wms_inventory` VALUES (1,9,1,3,'','2026-07-14','2029-07-13',11.00,0.00,1,'0','admin','2026-07-14 12:15:40','admin','2026-07-14 13:47:38',NULL),(2,9,1,5,'',NULL,NULL,5.00,0.00,0,'0','admin','2026-07-14 12:15:47','',NULL,NULL),(3,5,1,5,'0714001',NULL,NULL,20.00,0.00,0,'0','admin','2026-07-14 12:16:05','',NULL,NULL),(4,5,1,3,'0714002',NULL,NULL,0.00,0.00,3,'0','admin','2026-07-14 12:29:43','admin','2026-07-14 17:01:47',NULL),(5,6,1,3,'0714002','2026-07-14','2027-07-14',0.00,0.00,1,'0','admin','2026-07-14 13:28:13','admin','2026-07-14 14:04:12',NULL),(6,5,1,5,'0714002',NULL,NULL,1.00,0.00,0,'2','admin','2026-07-14 17:01:47','',NULL,NULL);
/*!40000 ALTER TABLE `wms_inventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wms_inventory_log`
--

DROP TABLE IF EXISTS `wms_inventory_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wms_inventory_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `location_id` bigint DEFAULT NULL COMMENT '库位ID',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `change_type` varchar(20) NOT NULL COMMENT '变动类型（字典 wms_change_type）',
  `change_qty` decimal(18,2) NOT NULL COMMENT '变动数量（正增负减）',
  `before_qty` decimal(18,2) DEFAULT '0.00' COMMENT '变动前数量',
  `after_qty` decimal(18,2) DEFAULT '0.00' COMMENT '变动后数量',
  `ref_order_type` varchar(20) DEFAULT NULL COMMENT '关联单据类型',
  `ref_order_no` varchar(64) DEFAULT NULL COMMENT '关联业务单据号',
  `create_by` varchar(64) DEFAULT '' COMMENT '操作人',
  `create_time` datetime DEFAULT NULL COMMENT '操作时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`log_id`),
  KEY `idx_log_material` (`material_id`),
  KEY `idx_log_warehouse` (`warehouse_id`),
  KEY `idx_log_ref_order` (`ref_order_no`),
  KEY `idx_log_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wms_inventory_log`
--

LOCK TABLES `wms_inventory_log` WRITE;
/*!40000 ALTER TABLE `wms_inventory_log` DISABLE KEYS */;
INSERT INTO `wms_inventory_log` VALUES (1,9,1,3,NULL,'0',5.00,0.00,5.00,'inbound','IN1784001957562','admin','2026-07-14 12:15:40',NULL),(2,9,1,5,NULL,'0',5.00,0.00,5.00,'inbound','IN1784001957562','admin','2026-07-14 12:15:47',NULL),(3,5,1,5,'0714001','0',20.00,0.00,20.00,'inbound','IN1784001957562','admin','2026-07-14 12:16:05',NULL),(4,5,1,3,'0714002','0',8.00,0.00,8.00,'inbound','IN1784002789234','admin','2026-07-14 12:29:43',NULL),(5,6,1,3,'0714002','0',6.00,0.00,6.00,'inbound','IN1784006864761','admin','2026-07-14 13:28:13',NULL),(6,9,1,3,NULL,'0',6.00,5.00,11.00,'inbound','IN1784008039260','admin','2026-07-14 13:47:38',NULL),(7,6,1,3,'0714002','1',-6.00,6.00,0.00,'outbound','OUT1784008950208','admin','2026-07-14 14:04:12',NULL),(8,5,1,3,'0714002','1',-8.00,8.00,0.00,'outbound','OUT1784009899762','admin','2026-07-14 14:24:39',NULL),(9,5,1,3,'0714002','4',1.00,0.00,1.00,'stocktake','ST1784010781572','admin','2026-07-14 15:22:43',NULL),(10,5,1,3,'0714002','3',-1.00,1.00,0.00,'move_out','MV1784019694035','admin','2026-07-14 17:01:47',NULL);
/*!40000 ALTER TABLE `wms_inventory_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wms_material`
--

DROP TABLE IF EXISTS `wms_material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wms_material` (
  `material_id` bigint NOT NULL AUTO_INCREMENT COMMENT '物料ID',
  `material_code` varchar(64) NOT NULL COMMENT '物料编码（唯一标识）',
  `material_name` varchar(255) NOT NULL COMMENT '物料名称',
  `material_type` varchar(20) DEFAULT '0' COMMENT '物料类型（字典 wms_material_type）',
  `spec_model` varchar(255) DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) NOT NULL COMMENT '基本计量单位（字典 wms_unit）',
  `barcode` varchar(64) DEFAULT NULL COMMENT '物料条码',
  `safety_stock_min` decimal(18,2) DEFAULT '0.00' COMMENT '安全库存下限',
  `safety_stock_max` decimal(18,2) DEFAULT '0.00' COMMENT '安全库存上限',
  `is_batch_manage` char(1) DEFAULT '0' COMMENT '是否批次管理（0否 1是）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `is_expiry_manage` char(1) DEFAULT '0' COMMENT '是否效期管理（0否 1是）',
  `shelf_life_days` int DEFAULT NULL COMMENT '保质期天数',
  `expiry_alert_days` int DEFAULT '30' COMMENT '临期预警天数',
  `production_date` date DEFAULT NULL,
  `expiry_date` date DEFAULT NULL,
  PRIMARY KEY (`material_id`),
  UNIQUE KEY `uk_material_code` (`material_code`),
  KEY `idx_material_type` (`material_type`),
  KEY `idx_material_name` (`material_name`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料主数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wms_material`
--

LOCK TABLES `wms_material` WRITE;
/*!40000 ALTER TABLE `wms_material` DISABLE KEYS */;
INSERT INTO `wms_material` VALUES (1,'MAT20250001','ABS塑料颗粒','0','PA-757 白色 25kg/袋','3','6901234567890',NULL,NULL,'1','0','0','admin','2026-07-14 09:43:11','admin','2026-07-14 09:43:11','注塑原材料','1',365,30,NULL,NULL),(2,'MAT20250002','304不锈钢板','0','1.2mm*1220mm*2440mm','3','6901234567891',50.00,2000.00,'0','0','0','admin','2026-07-14 09:43:11','admin','2026-07-14 09:43:11','钣金原材料','0',NULL,30,NULL,NULL),(3,'MAT20250003','PCBA主板组件','1','V2.3 带WiFi模组','1','6901234567892',20.00,500.00,'1','0','0','admin','2026-07-14 09:43:11','admin','2026-07-14 09:43:11','半成品需防静电','1',180,15,NULL,NULL),(4,'MAT20250004','锂电池组 48V20Ah','1','18650-13S4P 带BMS','1','6901234567893',10.00,200.00,'1','0','0','admin','2026-07-14 09:43:11','admin','2026-07-14 09:43:11','危险品批次管理','1',730,60,NULL,NULL),(5,'MAT20250005','智能手环 Pro','2','黑色 硅胶表带 心率版','1','6901234567894',50.00,1000.00,'1','0','0','admin','2026-07-14 09:43:11','admin','2026-07-14 09:43:11','旗舰成品','1',365,30,NULL,NULL),(6,'MAT20250006','无线蓝牙耳机','2','白色 降噪版 Type-C充电','1','6901234567895',100.00,2000.00,'1','0','0','admin','2026-07-14 09:43:11','admin','2026-07-14 09:43:11','热销成品','1',365,30,NULL,NULL),(7,'MAT20250007','包装纸箱 400*300*250','3','五层瓦楞 K=K','2','6901234567896',200.00,5000.00,'0','0','0','admin','2026-07-14 09:43:11','admin','2026-07-14 11:19:10','成品外包装','0',NULL,30,NULL,NULL),(8,'MAT20250008','防静电包装袋','3','200mm*300mm 自封袋','2','6901234567897',500.00,10000.00,'0','0','0','admin','2026-07-14 09:43:11','admin','2026-07-14 09:43:11','电子件辅料','0',NULL,30,NULL,NULL),(9,'MAT20250009','导热硅脂','3','HY510 灰色 1g/支','0','6901234567898',100.00,3000.00,'0','0','0','admin','2026-07-14 09:43:11','admin','2026-07-14 09:43:11','散热辅料','1',1095,90,NULL,NULL),(10,'MAT20250010','铝合金型材','0','6063-T5 20mm*20mm 6米/根','5','6901234567899',30.00,800.00,'0','1','0','admin','2026-07-14 09:43:11','admin','2026-07-14 09:43:11','已停用物料','0',NULL,30,NULL,NULL);
/*!40000 ALTER TABLE `wms_material` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wms_move_order`
--

DROP TABLE IF EXISTS `wms_move_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wms_move_order` (
  `move_id` bigint NOT NULL AUTO_INCREMENT COMMENT '移库单ID',
  `move_no` varchar(64) NOT NULL COMMENT '移库单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `from_location_id` bigint NOT NULL COMMENT '源库位ID',
  `to_location_id` bigint NOT NULL COMMENT '目标库位ID',
  `move_qty` decimal(18,2) NOT NULL COMMENT '移库数量',
  `status` char(1) DEFAULT '0' COMMENT '状态（0待审批 1已批准 2已完成 3已作废）',
  `approve_by` varchar(64) DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`move_id`),
  UNIQUE KEY `uk_move_no` (`move_no`),
  KEY `idx_move_status` (`status`),
  KEY `idx_move_warehouse` (`warehouse_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='移库单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wms_move_order`
--

LOCK TABLES `wms_move_order` WRITE;
/*!40000 ALTER TABLE `wms_move_order` DISABLE KEYS */;
INSERT INTO `wms_move_order` VALUES (1,'MV1784019694035',1,5,'0714002',3,5,1.00,'2','admin','2026-07-14 17:01:42','2026-07-14 17:01:47','0',NULL,'2026-07-14 17:01:34','','2026-07-14 17:01:47',NULL),(2,'MV1784021042965',1,5,'0714002',5,3,1.00,'3',NULL,NULL,NULL,'2',NULL,'2026-07-14 17:24:03','','2026-07-14 17:24:10',NULL);
/*!40000 ALTER TABLE `wms_move_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wms_outbound_order`
--

DROP TABLE IF EXISTS `wms_outbound_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wms_outbound_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '出库单ID',
  `order_no` varchar(64) NOT NULL COMMENT '出库单号',
  `order_type` varchar(20) DEFAULT '0' COMMENT '出库类型（字典 wms_outbound_type）',
  `warehouse_id` bigint NOT NULL COMMENT '出库仓库ID',
  `status` char(1) DEFAULT '0' COMMENT '状态（0草稿 1待拣货 2复核中 3已完成 4已作废）',
  `wave_id` bigint DEFAULT NULL COMMENT '所属波次ID',
  `outbound_date` datetime DEFAULT NULL COMMENT '预计出库日期',
  `complete_date` datetime DEFAULT NULL COMMENT '实际完成日期',
  `total_qty` decimal(18,2) DEFAULT '0.00' COMMENT '总数量',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_outbound_order_no` (`order_no`),
  KEY `idx_outbound_status` (`status`),
  KEY `idx_outbound_warehouse` (`warehouse_id`),
  KEY `idx_outbound_wave` (`wave_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库单头表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wms_outbound_order`
--

LOCK TABLES `wms_outbound_order` WRITE;
/*!40000 ALTER TABLE `wms_outbound_order` DISABLE KEYS */;
INSERT INTO `wms_outbound_order` VALUES (1,'OUT1784008950208','0',1,'3',NULL,NULL,'2026-07-14 14:07:46',6.00,'2',NULL,'2026-07-14 14:02:30','','2026-07-14 14:07:46',NULL),(2,'OUT1784009899762','0',1,'3',NULL,NULL,'2026-07-14 14:25:15',8.00,'2',NULL,'2026-07-14 14:18:19','','2026-07-14 14:25:14',NULL);
/*!40000 ALTER TABLE `wms_outbound_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wms_outbound_order_detail`
--

DROP TABLE IF EXISTS `wms_outbound_order_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wms_outbound_order_detail` (
  `detail_id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '出库单ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '指定批次号',
  `plan_qty` decimal(18,2) DEFAULT '0.00' COMMENT '计划数量',
  `actual_qty` decimal(18,2) DEFAULT '0.00' COMMENT '实际复核数量',
  `location_id` bigint DEFAULT NULL COMMENT '拣货库位ID',
  `pick_qty` decimal(18,2) DEFAULT '0.00' COMMENT '已拣货数量',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`detail_id`),
  KEY `idx_outbound_detail_order` (`order_id`),
  KEY `idx_outbound_detail_material` (`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wms_outbound_order_detail`
--

LOCK TABLES `wms_outbound_order_detail` WRITE;
/*!40000 ALTER TABLE `wms_outbound_order_detail` DISABLE KEYS */;
INSERT INTO `wms_outbound_order_detail` VALUES (1,1,6,'0714002',6.00,6.00,3,6.00,'0',NULL,'2026-07-14 14:02:30','','2026-07-14 14:07:46',NULL),(2,2,5,'0714002',8.00,8.00,3,8.00,'0',NULL,'2026-07-14 14:18:19','','2026-07-14 14:25:14',NULL);
/*!40000 ALTER TABLE `wms_outbound_order_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wms_stock_take`
--

DROP TABLE IF EXISTS `wms_stock_take`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wms_stock_take` (
  `take_id` bigint NOT NULL AUTO_INCREMENT COMMENT '盘点单ID',
  `take_no` varchar(64) NOT NULL COMMENT '盘点单号',
  `warehouse_id` bigint NOT NULL COMMENT '盘点仓库ID',
  `area_id` bigint DEFAULT NULL COMMENT '盘点库区ID',
  `take_type` varchar(20) DEFAULT '0' COMMENT '盘点类型（字典 wms_take_type）',
  `sample_ratio` int DEFAULT NULL COMMENT '抽样比例（百分比，仅抽盘）',
  `cycle_no` int DEFAULT NULL COMMENT '循环批次号（仅循环盘）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0待盘点 1盘点中 2待审批 3已完成 4已作废）',
  `plan_date` date DEFAULT NULL COMMENT '计划盘点日期',
  `start_time` datetime DEFAULT NULL COMMENT '盘点开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '盘点结束时间',
  `approve_by` varchar(64) DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`take_id`),
  UNIQUE KEY `uk_take_no` (`take_no`),
  KEY `idx_take_status` (`status`),
  KEY `idx_take_warehouse` (`warehouse_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='盘点单头表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wms_stock_take`
--

LOCK TABLES `wms_stock_take` WRITE;
/*!40000 ALTER TABLE `wms_stock_take` DISABLE KEYS */;
INSERT INTO `wms_stock_take` VALUES (1,'ST1784010757236',1,2,'0',NULL,NULL,'2','2026-07-14','2026-07-14 14:32:40','2026-07-14 15:36:08',NULL,NULL,'0',NULL,'2026-07-14 14:32:37','','2026-07-14 15:36:07',NULL),(2,'ST1784010781572',1,NULL,'0',NULL,NULL,'3',NULL,'2026-07-14 14:33:06','2026-07-14 15:20:23','admin','2026-07-14 15:22:43','0',NULL,'2026-07-14 14:33:01','','2026-07-14 15:22:43',NULL),(3,'ST1784016628291',1,2,'0',NULL,NULL,'0','2026-07-14',NULL,NULL,NULL,NULL,'0',NULL,'2026-07-14 16:10:28','',NULL,NULL),(4,'ST1784016661126',1,2,'1',NULL,NULL,'4',NULL,'2026-07-14 16:11:07','2026-07-14 16:15:54',NULL,NULL,'0',NULL,'2026-07-14 16:11:01','','2026-07-14 16:15:53',NULL),(5,'ST1784017919963',1,2,'1',30,1,'1','2026-07-14','2026-07-14 16:32:05',NULL,NULL,NULL,'0',NULL,'2026-07-14 16:32:00','','2026-07-14 16:32:04',NULL),(6,'ST1784017955433',1,NULL,'2',30,1,'3',NULL,'2026-07-14 16:32:38','2026-07-14 16:32:52','admin','2026-07-14 16:33:25','2',NULL,'2026-07-14 16:32:35','','2026-07-14 16:33:24',NULL);
/*!40000 ALTER TABLE `wms_stock_take` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wms_stock_take_detail`
--

DROP TABLE IF EXISTS `wms_stock_take_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wms_stock_take_detail` (
  `detail_id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `take_id` bigint NOT NULL COMMENT '盘点单ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `book_qty` decimal(18,2) DEFAULT '0.00' COMMENT '账面数量',
  `actual_qty` decimal(18,2) DEFAULT NULL COMMENT '实盘数量',
  `diff_qty` decimal(18,2) DEFAULT '0.00' COMMENT '差异数量',
  `diff_reason` varchar(500) DEFAULT NULL COMMENT '差异原因',
  `status` char(1) DEFAULT '0' COMMENT '明细状态（0待盘 1已盘 2已确认）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`detail_id`),
  KEY `idx_take_detail_order` (`take_id`),
  KEY `idx_take_detail_material` (`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='盘点单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wms_stock_take_detail`
--

LOCK TABLES `wms_stock_take_detail` WRITE;
/*!40000 ALTER TABLE `wms_stock_take_detail` DISABLE KEYS */;
INSERT INTO `wms_stock_take_detail` VALUES (1,1,6,3,'0714002',0.00,0.00,0.00,NULL,'1','0',NULL,'2026-07-14 14:32:37','','2026-07-14 15:35:54',NULL),(2,1,5,3,'0714002',0.00,1.00,1.00,NULL,'1','0',NULL,'2026-07-14 14:32:37','','2026-07-14 15:21:42',NULL),(3,1,5,5,'0714001',20.00,20.00,0.00,NULL,'1','0',NULL,'2026-07-14 14:32:37','','2026-07-14 15:35:58',NULL),(4,1,9,5,'',5.00,5.00,0.00,NULL,'1','0',NULL,'2026-07-14 14:32:37','','2026-07-14 15:36:06',NULL),(5,1,9,3,'',11.00,11.00,0.00,NULL,'1','0',NULL,'2026-07-14 14:32:37','','2026-07-14 15:36:07',NULL),(6,2,6,3,'0714002',0.00,0.00,0.00,NULL,'1','0',NULL,'2026-07-14 14:33:01','','2026-07-14 15:20:11',NULL),(7,2,5,3,'0714002',0.00,1.00,1.00,NULL,'2','0',NULL,'2026-07-14 14:33:01','','2026-07-14 15:22:43',NULL),(8,2,5,5,'0714001',20.00,20.00,0.00,NULL,'1','0',NULL,'2026-07-14 14:33:01','','2026-07-14 15:20:19',NULL),(9,2,9,5,'',5.00,5.00,0.00,NULL,'1','0',NULL,'2026-07-14 14:33:01','','2026-07-14 15:20:21',NULL),(10,2,9,3,'',11.00,11.00,0.00,NULL,'1','0',NULL,'2026-07-14 14:33:01','','2026-07-14 15:20:22',NULL),(11,3,6,3,'0714002',0.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-14 16:10:28','',NULL,NULL),(12,3,5,3,'0714002',1.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-14 16:10:28','',NULL,NULL),(13,3,5,5,'0714001',20.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-14 16:10:28','',NULL,NULL),(14,3,9,5,'',5.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-14 16:10:28','',NULL,NULL),(15,3,9,3,'',11.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-14 16:10:28','',NULL,NULL),(16,4,6,3,'0714002',0.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-14 16:11:01','',NULL,NULL),(17,4,5,3,'0714002',1.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-14 16:11:01','',NULL,NULL),(18,4,5,5,'0714001',20.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-14 16:11:01','',NULL,NULL),(19,4,9,5,'',5.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-14 16:11:01','',NULL,NULL),(20,4,9,3,'',11.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-14 16:11:01','',NULL,NULL),(21,5,9,3,'',11.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-14 16:32:00','',NULL,NULL),(22,5,6,3,'0714002',0.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-14 16:32:00','',NULL,NULL),(23,6,6,3,'0714002',0.00,0.00,0.00,NULL,'1','0',NULL,'2026-07-14 16:32:35','','2026-07-14 16:32:50',NULL),(24,6,9,3,'',11.00,11.00,0.00,NULL,'1','0',NULL,'2026-07-14 16:32:35','','2026-07-14 16:32:51',NULL),(25,7,6,3,'0714002',0.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-21 22:45:35','',NULL,NULL),(26,7,5,3,'0714002',0.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-21 22:45:35','',NULL,NULL),(27,7,5,5,'0714001',20.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-21 22:45:35','',NULL,NULL),(28,7,9,5,'',5.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-21 22:45:35','',NULL,NULL),(29,7,9,3,'',11.00,NULL,NULL,NULL,'0','0',NULL,'2026-07-21 22:45:35','',NULL,NULL);
/*!40000 ALTER TABLE `wms_stock_take_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wms_stock_take_snapshot`
--

DROP TABLE IF EXISTS `wms_stock_take_snapshot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wms_stock_take_snapshot` (
  `snapshot_id` bigint NOT NULL AUTO_INCREMENT COMMENT '快照ID',
  `take_id` bigint NOT NULL COMMENT '盘点单ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `snapshot_qty` decimal(18,2) DEFAULT '0.00' COMMENT '快照数量',
  `snapshot_time` datetime DEFAULT NULL COMMENT '快照时间',
  PRIMARY KEY (`snapshot_id`),
  KEY `idx_snapshot_take` (`take_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='盘点快照表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wms_stock_take_snapshot`
--

LOCK TABLES `wms_stock_take_snapshot` WRITE;
/*!40000 ALTER TABLE `wms_stock_take_snapshot` DISABLE KEYS */;
INSERT INTO `wms_stock_take_snapshot` VALUES (1,1,6,3,'0714002',0.00,'2026-07-14 14:32:37'),(2,1,5,3,'0714002',0.00,'2026-07-14 14:32:37'),(3,1,5,5,'0714001',20.00,'2026-07-14 14:32:37'),(4,1,9,5,'',5.00,'2026-07-14 14:32:37'),(5,1,9,3,'',11.00,'2026-07-14 14:32:37'),(6,2,6,3,'0714002',0.00,'2026-07-14 14:33:01'),(7,2,5,3,'0714002',0.00,'2026-07-14 14:33:01'),(8,2,5,5,'0714001',20.00,'2026-07-14 14:33:01'),(9,2,9,5,'',5.00,'2026-07-14 14:33:01'),(10,2,9,3,'',11.00,'2026-07-14 14:33:01'),(11,3,6,3,'0714002',0.00,'2026-07-14 16:10:28'),(12,3,5,3,'0714002',1.00,'2026-07-14 16:10:28'),(13,3,5,5,'0714001',20.00,'2026-07-14 16:10:28'),(14,3,9,5,'',5.00,'2026-07-14 16:10:28'),(15,3,9,3,'',11.00,'2026-07-14 16:10:28'),(16,4,6,3,'0714002',0.00,'2026-07-14 16:11:01'),(17,4,5,3,'0714002',1.00,'2026-07-14 16:11:01'),(18,4,5,5,'0714001',20.00,'2026-07-14 16:11:01'),(19,4,9,5,'',5.00,'2026-07-14 16:11:01'),(20,4,9,3,'',11.00,'2026-07-14 16:11:01'),(21,5,9,3,'',11.00,'2026-07-14 16:32:00'),(22,5,6,3,'0714002',0.00,'2026-07-14 16:32:00'),(23,6,6,3,'0714002',0.00,'2026-07-14 16:32:35'),(24,6,9,3,'',11.00,'2026-07-14 16:32:35'),(25,7,6,3,'0714002',0.00,'2026-07-21 22:45:35'),(26,7,5,3,'0714002',0.00,'2026-07-21 22:45:35'),(27,7,5,5,'0714001',20.00,'2026-07-21 22:45:35'),(28,7,9,5,'',5.00,'2026-07-21 22:45:35'),(29,7,9,3,'',11.00,'2026-07-21 22:45:35');
/*!40000 ALTER TABLE `wms_stock_take_snapshot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wms_supplier`
--

DROP TABLE IF EXISTS `wms_supplier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wms_supplier` (
  `supplier_id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  `supplier_code` varchar(64) NOT NULL COMMENT '供应商编码',
  `supplier_name` varchar(255) NOT NULL COMMENT '供应商名称',
  `contact_person` varchar(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(500) DEFAULT NULL COMMENT '地址',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`supplier_id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wms_supplier`
--

LOCK TABLES `wms_supplier` WRITE;
/*!40000 ALTER TABLE `wms_supplier` DISABLE KEYS */;
INSERT INTO `wms_supplier` VALUES (1,'SUP20250001','深圳塑胶科技有限公司','张三','13800138001','深圳市宝安区西乡街道科技园A栋','0','0','admin','2026-07-14 10:05:05','admin','2026-07-14 10:05:05','ABS塑料颗粒供应商'),(2,'SUP20250002','上海金属材料有限公司','李四','13800138002','上海市浦东新区张江高科技园区','0','0','admin','2026-07-14 10:05:05','admin','2026-07-14 10:05:05','不锈钢板及铝材供应商'),(3,'SUP20250003','东莞电子科技有限公司','王五','13800138003','东莞市松山湖高新技术开发区','0','0','admin','2026-07-14 10:05:05','admin','2026-07-14 10:05:05','PCBA及电子元器件供应商'),(4,'SUP20250004','惠州电池有限公司','赵六','13800138004','惠州市仲恺高新区工业园','0','0','admin','2026-07-14 10:05:05','admin','2026-07-14 10:05:05','锂电池组供应商'),(5,'SUP20250005','广州包装材料有限公司','孙七','13800138005','广州市白云区太和镇工业区','1','0','admin','2026-07-14 10:05:05','admin','2026-07-14 10:05:05','包装纸箱供应商（已停用）');
/*!40000 ALTER TABLE `wms_supplier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wms_warehouse`
--

DROP TABLE IF EXISTS `wms_warehouse`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wms_warehouse` (
  `warehouse_id` bigint NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父级ID',
  `ancestors` varchar(500) DEFAULT NULL COMMENT '祖级列表',
  `node_type` char(1) DEFAULT '1' COMMENT '节点类型（1=仓库 2=仓区 3=仓位）',
  `warehouse_code` varchar(64) NOT NULL COMMENT '仓库编码',
  `warehouse_name` varchar(128) NOT NULL COMMENT '仓库名称',
  `dept_id` bigint DEFAULT NULL COMMENT '管理部门ID',
  `manager_id` bigint DEFAULT NULL COMMENT '负责人用户ID',
  `address` varchar(500) DEFAULT NULL COMMENT '仓库地址',
  `area_type` varchar(20) DEFAULT NULL COMMENT '库区类型（仓区级）',
  `location_type` varchar(20) DEFAULT NULL COMMENT '库位类型（仓位级）',
  `capacity` decimal(18,2) DEFAULT '0.00' COMMENT '库位容量（仓位级）',
  `location_status` char(1) DEFAULT '0' COMMENT '库位状态（仓位级，0空闲 1占用 2锁定）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`warehouse_id`),
  UNIQUE KEY `uk_warehouse_code` (`warehouse_code`),
  KEY `idx_warehouse_dept` (`dept_id`),
  KEY `idx_warehouse_parent` (`parent_id`),
  KEY `idx_warehouse_node_type` (`node_type`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wms_warehouse`
--

LOCK TABLES `wms_warehouse` WRITE;
/*!40000 ALTER TABLE `wms_warehouse` DISABLE KEYS */;
INSERT INTO `wms_warehouse` VALUES (1,0,'0','1','WH001','成品库',101,2,NULL,'0','0',0.00,NULL,'0','0',NULL,'2026-07-14 11:34:47','','2026-07-14 11:55:09',NULL),(2,1,'0,1','2','WH001A01','东区',NULL,NULL,NULL,'0','0',0.00,NULL,'0','0',NULL,'2026-07-14 11:35:41','',NULL,NULL),(3,2,'0,1,2','3','WH001A01L01','东区001',NULL,NULL,NULL,'0','0',0.00,'0','0','0',NULL,'2026-07-14 11:35:52','',NULL,NULL),(4,1,'0,1','2','WH001A02','西区',NULL,NULL,NULL,'0','0',0.00,NULL,'0','0',NULL,'2026-07-14 11:36:03','',NULL,NULL),(5,4,'0,1,4','3','WH001A02L01','西区01',NULL,NULL,NULL,'0','0',0.00,'0','0','0',NULL,'2026-07-14 11:36:12','',NULL,NULL),(6,0,'0','3','WH000A00L01','ttest',NULL,NULL,NULL,'0','0',0.00,'0','0','2',NULL,'2026-07-14 11:38:15','',NULL,NULL),(7,0,NULL,'1','WH006','华南物流中心',100,NULL,'广州市南沙区物流大道88号','常温库','平面库',20000.00,'0','0','0','admin','2026-07-14 11:41:47','',NULL,'华南地区主要配送中心'),(8,0,NULL,'1','WH007','华北冷链仓库',100,NULL,'北京市顺义区冷链路66号','冷库','立体库',15000.00,'0','0','0','admin','2026-07-14 11:41:47','',NULL,'冷冻生鲜专用仓库'),(9,0,NULL,'1','WH008','华东分拨仓',100,NULL,'苏州市工业园区物流街22号','常温库','平面库',18000.00,'0','0','0','admin','2026-07-14 11:41:47','',NULL,'长三角区域分拨'),(10,0,NULL,'1','WH009','西南保税仓',100,NULL,'成都市双流区保税路55号','恒温库','立体库',12000.00,'0','0','0','admin','2026-07-14 11:41:47','',NULL,'跨境电商保税仓储'),(11,0,NULL,'1','WH010','华中快运仓',100,NULL,'武汉市东西湖区快运大道77号','常温库','平面库',25000.00,'0','0','0','admin','2026-07-14 11:41:47','',NULL,'中部地区快运枢纽');
/*!40000 ALTER TABLE `wms_warehouse` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wms_wave`
--

DROP TABLE IF EXISTS `wms_wave`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wms_wave` (
  `wave_id` bigint NOT NULL AUTO_INCREMENT COMMENT '波次ID',
  `wave_no` varchar(64) NOT NULL COMMENT '波次号',
  `wave_name` varchar(128) DEFAULT NULL COMMENT '波次名称',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `status` char(1) DEFAULT '0' COMMENT '状态（0待拣货 1拣货中 2已完成 3已作废）',
  `total_qty` decimal(18,2) DEFAULT '0.00' COMMENT '总数量',
  `order_count` int DEFAULT '0' COMMENT '包含订单数',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`wave_id`),
  UNIQUE KEY `uk_wave_no` (`wave_no`),
  KEY `idx_wave_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='波次主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wms_wave`
--

LOCK TABLES `wms_wave` WRITE;
/*!40000 ALTER TABLE `wms_wave` DISABLE KEYS */;
/*!40000 ALTER TABLE `wms_wave` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'ry-vue'
--

--
-- Dumping routines for database 'ry-vue'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-22 11:47:30

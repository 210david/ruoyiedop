-- ============================================================
-- 20261106 合并：同事功能合并增量 SQL（PJM项目管理 + 车辆台账 + 风险点完善 + 采购耗用登记）
-- 目标库：ruoyi_lyjs
-- 纪律：幂等可重跑（CREATE TABLE IF NOT EXISTS / INSERT IGNORE / WHERE NOT EXISTS / 幂等加列存储过程）
-- 注意：patch_20261104.sql 中车辆台账菜单 parent_id 4639（一企一档）→ 4638（安全生产管理）、
--       耗用登记菜单 parent_id 4441（采购订单执行）→ 4197（采购管理），
--       避免菜单挂错父目录（语义错位）。
-- ============================================================
SET NAMES utf8mb4;

-- §1 PMS/WMS 跨模块联动字段（幂等存储过程）
DROP PROCEDURE IF EXISTS p_add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE p_add_column_if_missing(IN p_table VARCHAR(64), IN p_col VARCHAR(64), IN p_def TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = p_col
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_def);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;
CALL p_add_column_if_missing('pms_purchase_request','purchase_type','`purchase_type` char(1) DEFAULT ''1'' COMMENT ''采购类型(1正常采购 2零散采购)''');
CALL p_add_column_if_missing('pms_purchase_request','source_project_id','`source_project_id` bigint DEFAULT NULL COMMENT ''来源项目ID''');
CALL p_add_column_if_missing('pms_purchase_request','source_project_no','`source_project_no` varchar(50) DEFAULT NULL COMMENT ''来源项目编号''');
CALL p_add_column_if_missing('pms_purchase_plan','source_project_id','`source_project_id` bigint DEFAULT NULL COMMENT ''来源项目ID''');
CALL p_add_column_if_missing('pms_purchase_plan','source_project_no','`source_project_no` varchar(50) DEFAULT NULL COMMENT ''来源项目编号''');
CALL p_add_column_if_missing('pms_purchase_order','purchase_type','`purchase_type` char(1) DEFAULT ''1'' COMMENT ''采购类型(1正常采购 2零散采购)''');
CALL p_add_column_if_missing('pms_purchase_order','source_project_id','`source_project_id` bigint DEFAULT NULL COMMENT ''来源项目ID''');
CALL p_add_column_if_missing('pms_purchase_order','source_project_no','`source_project_no` varchar(50) DEFAULT NULL COMMENT ''来源项目编号''');
CALL p_add_column_if_missing('pms_contract','source_project_id','`source_project_id` bigint DEFAULT NULL COMMENT ''来源项目ID''');
CALL p_add_column_if_missing('pms_contract','source_project_no','`source_project_no` varchar(50) DEFAULT NULL COMMENT ''来源项目编号''');
CALL p_add_column_if_missing('pms_inquiry','purchase_type','`purchase_type` char(1) DEFAULT ''0'' COMMENT ''采购类型（字典 pms_purchase_type）''');
CALL p_add_column_if_missing('wms_outbound_order','source_project_id','`source_project_id` bigint DEFAULT NULL COMMENT ''来源项目ID（PJM联动）''');
CALL p_add_column_if_missing('wms_outbound_order','source_project_no','`source_project_no` varchar(64) DEFAULT NULL COMMENT ''来源项目编号（PJM联动）''');
DROP PROCEDURE IF EXISTS p_add_column_if_missing;

-- §2 PJM 11 张建表

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_project` (
  `project_id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `project_no` varchar(50) NOT NULL COMMENT '项目编号(PJM前缀)',
  `project_name` varchar(200) NOT NULL COMMENT '项目名称',
  `project_type` varchar(20) DEFAULT '1' COMMENT '类型(1-研发 2-实施 3-运维 4-产品 5-投标 6-内部)',
  `engineering_category` varchar(20) DEFAULT NULL COMMENT '工程类别(pjm_engineering_category)',
  `priority` varchar(10) DEFAULT '2' COMMENT '优先级(1低 2中 3高 4紧急)',
  `status` varchar(20) DEFAULT '0' COMMENT '状态(0草稿 1待审 2通过 3执行 4完成 5关闭 6暂停 7驳回)',
  `progress` decimal(5,2) DEFAULT '0.00' COMMENT '整体进度%',
  `budget_amount` decimal(18,2) DEFAULT '0.00' COMMENT '项目预算总金额',
  `planned_purchase` decimal(18,2) DEFAULT '0.00' COMMENT '计划采购金额(=Σ采购申请)',
  `actual_purchase` decimal(18,2) DEFAULT '0.00' COMMENT '实际采购金额(=Σ采购订单)',
  `received_amount` decimal(18,2) DEFAULT '0.00' COMMENT '已收货金额',
  `invoice_amount` decimal(18,2) DEFAULT '0.00' COMMENT '已开票金额',
  `paid_amount` decimal(18,2) DEFAULT '0.00' COMMENT '已付款金额',
  `purchase_count` int DEFAULT '0' COMMENT '关联采购申请数量',
  `start_date` date DEFAULT NULL COMMENT '计划开始',
  `end_date` date DEFAULT NULL COMMENT '计划结束',
  `actual_start_date` date DEFAULT NULL COMMENT '实际开始',
  `actual_end_date` date DEFAULT NULL COMMENT '实际结束',
  `manager_id` bigint DEFAULT NULL COMMENT '项目经理ID',
  `manager_name` varchar(64) DEFAULT NULL COMMENT '项目经理姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '所属部门名称',
  `owner_unit` varchar(100) DEFAULT NULL COMMENT '建设单位',
  `build_unit` varchar(100) DEFAULT NULL COMMENT '施工单位',
  `supervisor_unit` varchar(100) DEFAULT NULL COMMENT '监理单位',
  `design_unit` varchar(100) DEFAULT NULL COMMENT '设计单位',
  `project_address` varchar(200) DEFAULT NULL COMMENT '工程地点',
  `build_scale` varchar(200) DEFAULT NULL COMMENT '建设规模',
  `contract_duration` int DEFAULT NULL COMMENT '合同工期(日历天)',
  `commence_date` date DEFAULT NULL COMMENT '开工日期',
  `completion_date` date DEFAULT NULL COMMENT '计划竣工日期',
  `quality_standard` varchar(50) DEFAULT NULL COMMENT '质量标准',
  `contract_amount` decimal(16,2) DEFAULT NULL COMMENT '合同总额(元)',
  `customer` varchar(200) DEFAULT NULL COMMENT '客户/甲方',
  `description` text COMMENT '项目描述',
  `scope` text COMMENT '项目范围',
  `deliverables` text COMMENT '交付物清单',
  `audit_by` varchar(64) DEFAULT NULL COMMENT '审批人',
  `audit_time` datetime DEFAULT NULL COMMENT '审批时间',
  `audit_opinion` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`project_id`),
  UNIQUE KEY `uk_project_no` (`project_no`)
) ENGINE=InnoDB AUTO_INCREMENT=9002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目立项主表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_project_attachment` (
  `attachment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `project_id` bigint NOT NULL COMMENT '关联项目ID',
  `attachment_type` char(1) DEFAULT '1' COMMENT '类型(1招标文件 2投标文件 3项目图纸 4项目保险)',
  `file_name` varchar(200) NOT NULL COMMENT '原始文件名',
  `file_url` varchar(500) NOT NULL COMMENT '上传路径/URL',
  `file_size` bigint DEFAULT '0' COMMENT '文件大小(字节)',
  `file_ext` varchar(20) DEFAULT NULL COMMENT '扩展名',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`attachment_id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目立项附件表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_project_procurement` (
  `procurement_id` bigint NOT NULL AUTO_INCREMENT COMMENT '清单ID',
  `project_id` bigint NOT NULL COMMENT '关联项目ID',
  `material_id` bigint DEFAULT NULL COMMENT '系统物料ID（关联wms_material）',
  `item_no` int DEFAULT '0' COMMENT '序号',
  `item_code` varchar(50) DEFAULT NULL COMMENT '物料编码',
  `item_name` varchar(200) NOT NULL COMMENT '物料名称',
  `spec_model` varchar(200) DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `qty` decimal(18,4) DEFAULT '0.0000' COMMENT '数量',
  `unit_price` decimal(18,2) DEFAULT '0.00' COMMENT '单价',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT '合计金额',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`procurement_id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目立项采购清单表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_project_member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `user_name` varchar(64) DEFAULT NULL,
  `dept_id` bigint DEFAULT NULL,
  `dept_name` varchar(100) DEFAULT NULL,
  `role_type` varchar(20) DEFAULT '3' COMMENT '1-项目经理 2-技术负责人 3-成员 4-外部协作',
  `join_date` date DEFAULT NULL,
  `leave_date` date DEFAULT NULL,
  `workload` decimal(5,2) DEFAULT '0.00' COMMENT '投入占比%',
  `task_count` int DEFAULT '0',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`member_id`),
  KEY `idx_project_user` (`project_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4010 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目团队成员表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_project_audit_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL,
  `audit_action` varchar(20) DEFAULT NULL COMMENT '1创建 2修改 3提交 4通过 5驳回 6关闭 7采购发起',
  `audit_by` varchar(64) DEFAULT NULL,
  `audit_time` datetime DEFAULT NULL,
  `audit_remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`log_id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7013 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目审核日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_wbs_task` (
  `task_id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL COMMENT '所属项目',
  `parent_task_id` bigint DEFAULT NULL COMMENT '父任务(自关联树)',
  `task_code` varchar(50) DEFAULT NULL COMMENT '编码(如 1.2.3)',
  `task_name` varchar(200) NOT NULL,
  `task_type` varchar(20) DEFAULT '1' COMMENT '1工作项 2里程碑 3交付物',
  `status` varchar(20) DEFAULT '0' COMMENT '0未开始 1进行中 2完成 3取消 4延期',
  `priority` varchar(10) DEFAULT '2',
  `progress` decimal(5,2) DEFAULT '0.00',
  `need_purchase` char(1) DEFAULT '0' COMMENT '0否 1是',
  `purchase_request_id` bigint DEFAULT NULL COMMENT '已关联采购申请ID',
  `owner_id` bigint DEFAULT NULL,
  `owner_name` varchar(64) DEFAULT NULL,
  `plan_start_date` date DEFAULT NULL,
  `plan_end_date` date DEFAULT NULL,
  `actual_start_date` date DEFAULT NULL,
  `actual_end_date` date DEFAULT NULL,
  `plan_hours` decimal(10,2) DEFAULT '0.00',
  `actual_hours` decimal(10,2) DEFAULT '0.00',
  `description` text,
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`task_id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_parent` (`parent_task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2403 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='WBS工作分解任务表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_task_worklog` (
  `worklog_id` bigint NOT NULL AUTO_INCREMENT COMMENT '工时ID',
  `worklog_no` varchar(50) DEFAULT NULL COMMENT '工时单号',
  `task_id` bigint NOT NULL COMMENT 'WBS任务ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `user_id` bigint DEFAULT NULL COMMENT '填报人ID',
  `user_name` varchar(64) DEFAULT NULL COMMENT '填报人',
  `work_date` date DEFAULT NULL COMMENT '工时日期',
  `work_hours` decimal(10,2) DEFAULT '0.00' COMMENT '工时（小时）',
  `work_content` varchar(500) DEFAULT NULL COMMENT '工作内容',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`worklog_id`),
  KEY `idx_task` (`task_id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='WBS任务工时记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_risk` (
  `risk_id` bigint NOT NULL AUTO_INCREMENT COMMENT '风险/问题ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `task_id` bigint DEFAULT NULL COMMENT '关联WBS任务ID',
  `risk_type` varchar(10) DEFAULT '1' COMMENT '类型（1风险 2问题）',
  `engineering_category` varchar(20) DEFAULT NULL COMMENT '工程类别(pjm_engineering_category)',
  `risk_point_id` bigint DEFAULT NULL COMMENT '关联国家标准风险点ID(biz_safety_risk_point)',
  `risk_std_code` varchar(128) DEFAULT NULL COMMENT '国家标准/规范条款号(GB xxxx-xxxx)',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `level` varchar(10) DEFAULT '2' COMMENT '等级（1低 2中 3高 4紧急）',
  `status` varchar(10) DEFAULT '0' COMMENT '状态（0开放 1处理中 2已解决 3已关闭）',
  `owner_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `owner_name` varchar(64) DEFAULT NULL COMMENT '负责人姓名',
  `occur_date` date DEFAULT NULL COMMENT '识别/发生日期',
  `resolve_date` date DEFAULT NULL COMMENT '解决日期',
  `solution` varchar(1000) DEFAULT NULL COMMENT '应对措施',
  `description` varchar(1000) DEFAULT NULL COMMENT '描述',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`risk_id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_task` (`task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6007 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目风险问题登记表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_milestone` (
  `milestone_id` bigint NOT NULL AUTO_INCREMENT COMMENT '里程碑ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `milestone_name` varchar(200) NOT NULL COMMENT '里程碑名称',
  `plan_date` date DEFAULT NULL COMMENT '计划日期',
  `actual_date` date DEFAULT NULL COMMENT '实际达成日期',
  `status` varchar(10) DEFAULT '0' COMMENT '状态（0未达成 1已达成 2延期）',
  `owner_id` bigint DEFAULT NULL COMMENT '负责人用户ID(sys_user.user_id)',
  `owner_name` varchar(64) DEFAULT NULL COMMENT '负责人',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`milestone_id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5012 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目里程碑表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_change` (
  `change_id` bigint NOT NULL AUTO_INCREMENT,
  `change_no` varchar(50) DEFAULT NULL,
  `project_id` bigint NOT NULL,
  `change_type` varchar(20) DEFAULT '1' COMMENT '1范围 2进度 3成本 4技术',
  `title` varchar(200) NOT NULL,
  `reason` text,
  `impact_analysis` text,
  `schedule_impact` int DEFAULT '0' COMMENT '影响工期(天)',
  `cost_impact` decimal(18,2) DEFAULT '0.00' COMMENT '影响金额',
  `status` varchar(20) DEFAULT '0' COMMENT '0草稿 1待审 2通过 3驳回 4实施',
  `requester_id` bigint DEFAULT NULL,
  `requester_name` varchar(64) DEFAULT NULL,
  `audit_by` varchar(64) DEFAULT NULL,
  `audit_time` datetime DEFAULT NULL,
  `audit_opinion` varchar(500) DEFAULT NULL,
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`change_id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目变更请求表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_payment` (
  `payment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '收款ID',
  `payment_no` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '收款单号',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `period_no` int DEFAULT NULL COMMENT '期次',
  `amount` decimal(16,2) NOT NULL COMMENT '收款金额(元)',
  `payment_date` date NOT NULL COMMENT '收款日期',
  `payer` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '付款方',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目收款记录';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;



-- §3 PJM 采购耗用登记建表

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pms_consumption` (
  `consumption_id` bigint NOT NULL AUTO_INCREMENT COMMENT '耗用ID',
  `consumption_no` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '耗用单号',
  `order_id` bigint DEFAULT NULL COMMENT '采购订单ID',
  `order_no` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '采购订单号',
  `receive_id` bigint DEFAULT NULL COMMENT '收货单ID',
  `receive_no` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收货单号',
  `supplier_name` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '供应商',
  `total_qty` decimal(14,2) DEFAULT '0.00' COMMENT '耗用总数量',
  `total_amount` decimal(16,2) DEFAULT '0.00' COMMENT '耗用总金额',
  `dept_id` bigint DEFAULT NULL COMMENT '使用部门ID',
  `dept_name` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '使用部门',
  `user_id` bigint DEFAULT NULL COMMENT '领用人ID',
  `user_name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '领用人',
  `use_date` date DEFAULT NULL COMMENT '使用日期',
  `usage_purpose` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用途说明',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态(0待登记 1已登记)',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`consumption_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_receive` (`receive_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='零散采购耗用登记';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pms_consumption_detail` (
  `detail_id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `consumption_id` bigint NOT NULL COMMENT '耗用ID',
  `material_id` bigint DEFAULT NULL COMMENT '物料ID',
  `material_code` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物料编码',
  `material_name` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物料名称',
  `spec_model` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '单位',
  `qty` decimal(14,2) DEFAULT '0.00' COMMENT '数量',
  `unit_price` decimal(16,4) DEFAULT NULL COMMENT '单价',
  `amount` decimal(16,2) DEFAULT '0.00' COMMENT '金额',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0',
  PRIMARY KEY (`detail_id`),
  KEY `idx_consumption` (`consumption_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='零散采购耗用明细';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;



-- §4 车辆台账建表+字典+菜单（同事原样，幂等）
-- ============================================================
-- 增量补丁：车辆台账 + 字典化改造（2026-11-04）
-- 适用：已部署本系统基线版本，新增加载车辆台账、项目资料、采购明细类型等功能
-- 说明：脚本幂等，可重复执行；不写死主键，避免与生产库主键冲突
-- 执行：mysql -uroot -p ry-vue < patch_20261104.sql
-- ============================================================

-- ---------- 1. 车辆台账表 ----------
CREATE TABLE IF NOT EXISTS `biz_safety_vehicle` (
  `vehicle_id` bigint NOT NULL AUTO_INCREMENT COMMENT '车辆ID',
  `vehicle_type` char(1) NOT NULL DEFAULT '1' COMMENT '车辆类型(1特种车辆 2公用车辆)',
  `vehicle_code` varchar(64) DEFAULT NULL COMMENT '车辆编号',
  `plate_no` varchar(32) NOT NULL COMMENT '车牌号',
  `vehicle_model` varchar(128) DEFAULT NULL COMMENT '品牌型号',
  `vehicle_use` varchar(64) DEFAULT NULL COMMENT '车辆用途(如叉车/起重机/罐车/通勤/公务)',
  `special_sub_type` varchar(64) DEFAULT NULL COMMENT '特种车辆子类(如叉车/桥式起重机/压力容器车)',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '驾驶员/操作员',
  `operator_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `license_no` varchar(64) DEFAULT NULL COMMENT '驾驶证/操作证号',
  `cert_expire` date DEFAULT NULL COMMENT '操作证有效期',
  `inspect_expire` date DEFAULT NULL COMMENT '检验/年检有效期',
  `insurance_expire` date DEFAULT NULL COMMENT '保险有效期',
  `enterprise_id` bigint DEFAULT NULL COMMENT '所属企业ID',
  `enterprise_name` varchar(128) DEFAULT NULL COMMENT '所属企业名称(冗余)',
  `status` char(1) DEFAULT '0' COMMENT '状态(0在用 1停用 2报废)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`vehicle_id`),
  UNIQUE KEY `uk_plate_no` (`plate_no`),
  KEY `idx_enterprise` (`enterprise_id`),
  KEY `idx_type` (`vehicle_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='车辆台账表(特种车辆/公用车辆)';

-- ---------- 2. 字典类型 ----------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '项目附件类型', 'pjm_project_attachment_type', '0', '项目立项附件类型' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'pjm_project_attachment_type');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '采购明细类型', 'pjm_purchase_detail_type', '0', '项目采购执行-明细分类' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'pjm_purchase_detail_type');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '车辆类型', 'safety_vehicle_type', '0', '一企一档-特种车辆/公用车辆分类' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'safety_vehicle_type');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '特种车辆子类', 'safety_special_vehicle_sub', '0', '特种车辆细分类型' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'safety_special_vehicle_sub');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '车辆状态', 'safety_vehicle_status', '0', '车辆在用状态' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'safety_vehicle_status');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '车辆所属企业', 'safety_vehicle_enterprise', '0', '车辆台账所属企业下拉，可在字典管理维护' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'safety_vehicle_enterprise');

-- ---------- 3. 字典数据（逐条判重，支持基线已有 4 项时补齐到 10 项） ----------
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'招标文件','1','pjm_project_attachment_type','','primary','Y','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'投标文件','2','pjm_project_attachment_type','','success','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='2');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'项目图纸','3','pjm_project_attachment_type','','warning','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='3');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 4,'项目保险','4','pjm_project_attachment_type','','danger','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='4');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 5,'合同协议','5','pjm_project_attachment_type','','primary','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='5');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 6,'技术方案','6','pjm_project_attachment_type','','primary','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='6');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 7,'会议纪要','7','pjm_project_attachment_type','','primary','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='7');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 8,'检查报告','8','pjm_project_attachment_type','','primary','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='8');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 9,'影像照片','9','pjm_project_attachment_type','','primary','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='9');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 10,'其他资料','10','pjm_project_attachment_type','','info','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='10');

INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'物料明细','1','pjm_purchase_detail_type','','primary','Y','0','走仓库库存/采购申请' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_purchase_detail_type' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'劳务外包','2','pjm_purchase_detail_type','','warning','N','0','人工外包费用' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_purchase_detail_type' AND `dict_value`='2');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'机械设备','3','pjm_purchase_detail_type','','success','N','0','机械租赁/设备采购' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_purchase_detail_type' AND `dict_value`='3');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 4,'其他','4','pjm_purchase_detail_type','','info','N','0','其他费用' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_purchase_detail_type' AND `dict_value`='4');

INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'特种车辆','1','safety_vehicle_type','','danger','Y','0','叉车/起重机/罐车等特种设备' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_type' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'公用车辆','2','safety_vehicle_type','','primary','N','0','通勤/公务/运输等内部车辆' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_type' AND `dict_value`='2');

INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'叉车','1','safety_special_vehicle_sub','','warning','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'桥式起重机','2','safety_special_vehicle_sub','','warning','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='2');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'门式起重机','3','safety_special_vehicle_sub','','warning','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='3');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 4,'压力容器车','4','safety_special_vehicle_sub','','danger','N','0','危化品运输' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='4');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 5,'罐车','5','safety_special_vehicle_sub','','danger','N','0','液体运输' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='5');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 6,'装载机','6','safety_special_vehicle_sub','','warning','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='6');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 7,'观光车','7','safety_special_vehicle_sub','','warning','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='7');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 8,'其他特种设备','99','safety_special_vehicle_sub','','info','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='99');

INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'在用','0','safety_vehicle_status','','success','Y','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_status' AND `dict_value`='0');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'停用','1','safety_vehicle_status','','info','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_status' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'报废','2','safety_vehicle_status','','danger','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_status' AND `dict_value`='2');

-- 车辆所属企业（dict_value = 企业档案中的企业ID，按现场实际企业补充/调整）
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'云南新材料科技有限公司','12','safety_vehicle_enterprise','','default','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_enterprise' AND `dict_value`='12');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'云南精细化工股份有限公司','14','safety_vehicle_enterprise','','default','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_enterprise' AND `dict_value`='14');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'测试安全科技有限公司','1','safety_vehicle_enterprise','','default','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_enterprise' AND `dict_value`='1');

-- ---------- 4. 菜单（父菜单 4639=一企一档，906202=项目管理，需基线已存在） ----------
INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`component`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`)
SELECT '车辆台账', 4639, 6, 'vehicle', 'safety/arch/vehicle/index', 'SafetyVehicle', 1, 0, 'C', '0', '0', 'safety:vehicle:list', 'vehicle', 'admin', NOW(), '特种车辆/公用车辆台账' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `route_name` = 'SafetyVehicle');

INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`component`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`)
SELECT '项目资料', 906202, 2, 'attachment', 'pjm/attachment/index', 'PjmAttachment', 1, 0, 'C', '0', '0', 'pjm:attachment:list', 'upload', 'admin', NOW(), '项目资料管理菜单' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `route_name` = 'PjmAttachment');

-- 已存在时同步图标
UPDATE `sys_menu` SET `icon` = 'vehicle', `update_time` = NOW() WHERE `route_name` = 'SafetyVehicle';

-- 耗用登记菜单（父菜单 4441=采购管理）
INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`component`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`)
SELECT '耗用登记', 4441, 9, 'consumption', 'pms/consumption/index', 'PmsConsumption', 1, 0, 'C', '0', '0', 'pms:consumption:list', 'shopping', 'admin', NOW(), '零散采购耗用登记' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `route_name` = 'PmsConsumption');

INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`)
SELECT '耗用查询', m.menu_id, 1, '#', 'F', '0', '0', 'pms:consumption:query', '#', 'admin', NOW() FROM `sys_menu` m
WHERE m.route_name='PmsConsumption' AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms`='pms:consumption:query');
INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`)
SELECT '耗用新增', m.menu_id, 2, '#', 'F', '0', '0', 'pms:consumption:add', '#', 'admin', NOW() FROM `sys_menu` m
WHERE m.route_name='PmsConsumption' AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms`='pms:consumption:add');
INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`)
SELECT '耗用修改', m.menu_id, 3, '#', 'F', '0', '0', 'pms:consumption:edit', '#', 'admin', NOW() FROM `sys_menu` m
WHERE m.route_name='PmsConsumption' AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms`='pms:consumption:edit');
INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`)
SELECT '耗用删除', m.menu_id, 4, '#', 'F', '0', '0', 'pms:consumption:remove', '#', 'admin', NOW() FROM `sys_menu` m
WHERE m.route_name='PmsConsumption' AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms`='pms:consumption:remove');

-- ---------- 5. 菜单按钮权限（若需分配给非 admin 角色，执行后到「角色管理」勾选对应菜单即可） ----------
-- 可选：INSERT INTO sys_role_menu SELECT 2, menu_id FROM sys_menu WHERE route_name IN ('SafetyVehicle','PjmAttachment','PmsConsumption');

-- §5 幂等加列：风险点工程类别 + PJM采购清单明细类型
DROP PROCEDURE IF EXISTS p_add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE p_add_column_if_missing(IN p_table VARCHAR(64), IN p_col VARCHAR(64), IN p_def TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = p_col
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_def);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;
CALL p_add_column_if_missing('biz_safety_risk_point','engineering_category','`engineering_category` varchar(20) DEFAULT NULL COMMENT ''工程类别（字典 pjm_engineering_category）''');
CALL p_add_column_if_missing('pjm_project_procurement','detail_type','`detail_type` varchar(10) NOT NULL DEFAULT ''1'' COMMENT ''明细类型：1物料明细 2劳务外包 3机械设备 4其他''');
DROP PROCEDURE IF EXISTS p_add_column_if_missing;

-- §6 菜单链（INSERT IGNORE 幂等转换）
-- ==============================================================================
-- PJS 项目管理 菜单 + 按钮权限 + 角色授权 + sys_module 注册
-- 主菜单已预先创建 (menu_id = 906202)，本脚本补全子菜单
-- ==============================================================================

-- ------------------------------
-- 二级菜单 1: 项目立项
-- ------------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (906203, '项目立项', 906202, 1, 'project', 'pjm/project/index', '', NULL, 1, 0, 'M', '0', '0', '', 'list', 'admin', sysdate(), '', NULL, '项目立项子菜单');

-- 项目立项 按钮权限 (F 类型)
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(906204, '项目查询',   906203, 1, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:project:query', '#', 'admin', sysdate()),
(906205, '项目新增',   906203, 2, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:project:add',   '#', 'admin', sysdate()),
(906206, '项目修改',   906203, 3, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:project:edit',  '#', 'admin', sysdate()),
(906207, '项目删除',   906203, 4, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:project:remove','#', 'admin', sysdate()),
(906208, '项目审批',   906203, 5, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:project:audit', '#', 'admin', sysdate()),
(906209, '项目导出',   906203, 6, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:project:export','#', 'admin', sysdate()),
(906210, '发起采购',   906203, 7, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:project:purchase','#', 'admin', sysdate()),
(906211, 'WBS任务创建', 906203, 8, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:task:add',  '#', 'admin', sysdate()),
(906212, 'WBS任务修改', 906203, 9, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:task:edit', '#', 'admin', sysdate()),
(906213, 'WBS任务删除', 906203, 10, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:task:remove','#', 'admin', sysdate());

-- ------------------------------
-- 二级菜单 2: WBS 任务 (独立目录)
-- ------------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906214, 'WBS任务分解', 906202, 2, 'task', 'pjm/task/index', '', NULL, 1, 0, 'M', '0', '0', '', 'tree', 'admin', sysdate(), 'WBS任务树');

-- ------------------------------
-- 二级菜单 3: 团队成员
-- ------------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906215, '项目团队', 906202, 3, 'member', 'pjm/member/index', '', NULL, 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '团队成员管理');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(906216, '成员新增', 906215, 1, '#', 'F', '0', '0', 'pjm:member:add',    '#', 'admin', sysdate()),
(906217, '成员删除', 906215, 2, '#', 'F', '0', '0', 'pjm:member:remove', '#', 'admin', sysdate());

-- ------------------------------
-- 二级菜单 4: 风险登记
-- ------------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906218, '风险登记', 906202, 4, 'risk', 'pjm/risk/index', '', NULL, 1, 0, 'M', '0', '0', '', 'bug', 'admin', sysdate(), '风险登记册');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(906219, '风险查询', 906218, 1, '#', 'F', '0', '0', 'pjm:risk:query',   '#', 'admin', sysdate()),
(906220, '风险新增', 906218, 2, '#', 'F', '0', '0', 'pjm:risk:add',     '#', 'admin', sysdate()),
(906221, '风险修改', 906218, 3, '#', 'F', '0', '0', 'pjm:risk:edit',    '#', 'admin', sysdate()),
(906222, '风险删除', 906218, 4, '#', 'F', '0', '0', 'pjm:risk:remove',  '#', 'admin', sysdate());

-- ------------------------------
-- 二级菜单 5: 变更管理
-- ------------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906223, '变更管理', 906202, 5, 'change', 'pjm/change/index', '', NULL, 1, 0, 'M', '0', '0', '', 'edit', 'admin', sysdate(), '变更请求管理');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(906224, '变更查询', 906223, 1, '#', 'F', '0', '0', 'pjm:change:query',  '#', 'admin', sysdate()),
(906225, '变更新增', 906223, 2, '#', 'F', '0', '0', 'pjm:change:add',    '#', 'admin', sysdate()),
(906226, '变更修改', 906223, 3, '#', 'F', '0', '0', 'pjm:change:edit',   '#', 'admin', sysdate()),
(906227, '变更审批', 906223, 4, '#', 'F', '0', '0', 'pjm:change:audit',  '#', 'admin', sysdate()),
(906228, '变更删除', 906223, 5, '#', 'F', '0', '0', 'pjm:change:remove', '#', 'admin', sysdate());

-- ------------------------------
-- 二级菜单 6: ★ 项目采购台账 (关联 PMS)
-- ------------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906229, '项目采购台账', 906202, 6, 'pms-ledger', 'pjm/pms/index', '', NULL, 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '★ 聚合 PMS 全链路采购数据');

-- ------------------------------
-- 二级菜单 7: 项目看板
-- ------------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906230, '项目看板', 906202, 7, 'dashboard', 'pjm/dashboard/index', '', NULL, 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '项目综合看板');

-- ------------------------------
-- sys_module 注册
-- ------------------------------
INSERT IGNORE INTO sys_module (module_name, module_key, root_menu_id, status, is_core, order_num, create_by, create_time, remark)
VALUES ('项目管理', 'pjm', 906202, '0', '0', 26, 'admin', sysdate(), '项目管理模块（业务主线）');

-- ------------------------------
-- 授权 admin 角色 (role_id=1)
-- ------------------------------
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 906203 AND 906230;

-- 补项目管理顶级目录（menu_id=906202，原 dump 缺失）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, route_name)
VALUES (906202, '项目管理', 0, 6, 'project', NULL, 'M', '0', '0', '', 'chart', 'admin', NOW(), '', NULL, '项目管理顶级目录', 'Pjm');

-- 给 admin(role_id=1) 授权
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 906202);

-- 补子菜单 route_name（之前规范是 Pjm*）
UPDATE sys_menu SET route_name='PjmProject' WHERE menu_id=906203 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmTask' WHERE menu_id=906214 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmMember' WHERE menu_id=906215 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmChange' WHERE menu_id=906223 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmPmsLedger' WHERE menu_id=906229 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmDashboard' WHERE menu_id=906230 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmMilestone' WHERE menu_id=906216 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmPayment' WHERE menu_id=906217 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmAttachment' WHERE menu_id=906219 AND (route_name IS NULL OR route_name='');

-- 补齐 PJM 下缺失的 4 个页面菜单（里程碑、付款、附件、工时），menu_id 避开 906544（当前最大）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark, route_name) VALUES
(906601, '里程碑管理', 906202, 6, 'milestone', 'pjm/milestone/index', 'C', '0', '0', '', 'date',   'admin', NOW(), '项目里程碑', 'PjmMilestone'),
(906602, '项目付款',   906202, 7, 'payment',   'pjm/payment/index',   'C', '0', '0', '', 'money',  'admin', NOW(), '项目付款',   'PjmPayment'),
(906603, '项目附件',   906202, 8, 'attachment','pjm/attachment/index','C', '0', '0', '', 'upload', 'admin', NOW(), '项目附件',   'PjmAttachment'),
(906604, '工时填报',   906202, 9, 'worklog',   'pjm/worklog/index',   'C', '0', '0', '', 'edit',   'admin', NOW(), '项目工时',   'PjmWorklog');

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 906601), (1, 906602), (1, 906603), (1, 906604);

-- 修 menu_type（带 component 的子菜单 C，不是 M）
UPDATE sys_menu SET menu_type='C' WHERE parent_id=906202 AND component IS NOT NULL AND component != '' AND menu_type='M';

-- 风险登记整个子树：之前已改为安全生产管理里的风险点管理，PJM 里删掉避免重复
-- menu_id: 906218 风险登记（页面）+ 其子 906219-906222（按钮权限）
DELETE FROM sys_role_menu WHERE menu_id IN (906218, 906219, 906220, 906221, 906222);
DELETE FROM sys_menu WHERE menu_id IN (906219, 906220, 906221, 906222);
DELETE FROM sys_menu WHERE menu_id = 906218;

-- 验证
SELECT menu_id, menu_name, parent_id, path, component, menu_type, route_name, visible FROM sys_menu WHERE menu_id=906202 OR parent_id=906202 ORDER BY parent_id, order_num;

-- §7 验证

SELECT CONCAT('VERIFY: pjm_project exists = ', COUNT(*)) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='pjm_project';
SELECT CONCAT('VERIFY: pjm tables = ', COUNT(*)) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name LIKE 'pjm%';
SELECT CONCAT('VERIFY: biz_safety_vehicle exists = ', COUNT(*)) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='biz_safety_vehicle';
SELECT CONCAT('VERIFY: pms_consumption exists = ', COUNT(*)) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='pms_consumption';
SELECT CONCAT('VERIFY: risk_point engineering_category = ', COUNT(*)) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_safety_risk_point' AND column_name='engineering_category';
SELECT CONCAT('VERIFY: pjm_project_procurement detail_type = ', COUNT(*)) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='pjm_project_procurement' AND column_name='detail_type';
SELECT CONCAT('VERIFY: request source_project_id = ', COUNT(*)) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='pms_purchase_request' AND column_name='source_project_id';
SELECT CONCAT('VERIFY: PJM menus = ', COUNT(*)) FROM sys_menu WHERE route_name LIKE 'Pjm%';
SELECT CONCAT('VERIFY: vehicle menu = ', COUNT(*)) FROM sys_menu WHERE route_name='SafetyVehicle';
SELECT CONCAT('VERIFY: consumption menu = ', COUNT(*)) FROM sys_menu WHERE route_name='PmsConsumption';

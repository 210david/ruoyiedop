-- =============================================
-- 需求二：客诉管理——增加供应商信息（源禾农业 2026-11）
-- 1) qms_complaint 增加供应商信息与责任类型字段
-- 2) 新建 qms_supplier_rectify 供应商整改单表
-- 3) 字典：qms_responsibility_type / qms_rectify_status
-- 4) 菜单：客诉管理下新增「供应商整改单」+ 按钮权限
-- 5) 定时任务：供应商整改逾期自动升级提醒（KR-03）
-- =============================================

-- ----------------------------
-- 1. 客诉单扩展字段
-- ----------------------------
ALTER TABLE `qms_complaint`
    ADD COLUMN `supplier_id` bigint NULL COMMENT '供应商ID（引用wms_supplier）',
    ADD COLUMN `supplier_name` varchar(128) NULL COMMENT '供应商名称',
    ADD COLUMN `supplier_contact` varchar(64) NULL COMMENT '供应商联系人',
    ADD COLUMN `supplier_phone` varchar(20) NULL COMMENT '供应商联系电话',
    ADD COLUMN `delivery_no` varchar(64) NULL COMMENT '供货批次号/送货单号',
    ADD COLUMN `supply_date` date NULL COMMENT '供货日期',
    ADD COLUMN `responsibility_type` char(1) NULL COMMENT '责任类型（qms_responsibility_type：0供应商责任 1自产责任 2物流责任 3客户原因 4待定）',
    ADD INDEX `idx_qms_complaint_supplier`(`supplier_id`);

-- ----------------------------
-- 2. 供应商整改单
-- ----------------------------
CREATE TABLE IF NOT EXISTS `qms_supplier_rectify` (
    `rectify_id` bigint NOT NULL AUTO_INCREMENT COMMENT '整改单ID',
    `rectify_no` varchar(64) NOT NULL COMMENT '整改单编号',
    `complaint_id` bigint NOT NULL COMMENT '客诉ID',
    `complaint_no` varchar(64) NULL COMMENT '客诉编号',
    `supplier_id` bigint NULL COMMENT '供应商ID（引用wms_supplier）',
    `supplier_name` varchar(128) NULL COMMENT '供应商名称',
    `material_name` varchar(128) NULL COMMENT '物料名称',
    `batch_no` varchar(64) NULL COMMENT '批次号',
    `rectify_requirement` text NULL COMMENT '整改要求',
    `evidence_attachment` varchar(500) NULL COMMENT '整改证据附件',
    `rectify_result` text NULL COMMENT '整改结果',
    `supplier_reply` text NULL COMMENT '供应商回复',
    `reply_time` datetime NULL COMMENT '供应商回复时间',
    `reply_deadline` date NULL COMMENT '供应商回复时限',
    `rectify_status` char(1) DEFAULT '0' COMMENT '状态（0待回复 1已回复 2逾期升级 3已关闭）',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注（含升级提醒说明）',
    PRIMARY KEY (`rectify_id`),
    KEY `idx_qms_rectify_complaint` (`complaint_id`),
    KEY `idx_qms_rectify_supplier` (`supplier_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='供应商整改单（客诉供应商责任处理）';

-- ----------------------------
-- 3. 字典
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('客诉责任类型', 'qms_responsibility_type', '0', 'admin', NOW(), '客诉供应商责任判定类型'),
('供应商整改状态', 'qms_rectify_status', '0', 'admin', NOW(), '供应商整改单状态');

DELETE FROM sys_dict_data WHERE dict_type = 'qms_responsibility_type';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '供应商责任', '0', 'qms_responsibility_type', '', 'danger',  'N', '0', 'admin', NOW(), ''),
(NULL, 2, '自产责任',   '1', 'qms_responsibility_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '物流责任',   '2', 'qms_responsibility_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '客户原因',   '3', 'qms_responsibility_type', '', 'info',    'N', '0', 'admin', NOW(), ''),
(NULL, 5, '待定',       '4', 'qms_responsibility_type', '', '',        'N', '0', 'admin', NOW(), '');

DELETE FROM sys_dict_data WHERE dict_type = 'qms_rectify_status';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '待供应商回复', '0', 'qms_rectify_status', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '已回复',       '1', 'qms_rectify_status', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '逾期升级',     '2', 'qms_rectify_status', '', 'danger',  'N', '0', 'admin', NOW(), ''),
(NULL, 4, '已关闭',       '3', 'qms_rectify_status', '', 'info',    'N', '0', 'admin', NOW(), '');

-- ----------------------------
-- 4. 菜单：客诉管理（parent_id=4800）下新增「供应商整改单」
-- ----------------------------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('供应商整改单', 4800, 5, 'rectify', 'qms/rectify/index', 1, 0, 'C', '0', '0', 'qms:rectify:list', 'link', 'admin', NOW(), '客诉供应商责任处理与整改跟踪');

SET @rectifyMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('整改单查询', @rectifyMenuId, 1, '', '', 1, 0, 'F', '0', '0', 'qms:rectify:query',  '#', 'admin', NOW(), ''),
('整改单新增', @rectifyMenuId, 2, '', '', 1, 0, 'F', '0', '0', 'qms:rectify:add',    '#', 'admin', NOW(), ''),
('整改单修改', @rectifyMenuId, 3, '', '', 1, 0, 'F', '0', '0', 'qms:rectify:edit',   '#', 'admin', NOW(), ''),
('整改单删除', @rectifyMenuId, 4, '', '', 1, 0, 'F', '0', '0', 'qms:rectify:remove', '#', 'admin', NOW(), ''),
('整改单导出', @rectifyMenuId, 5, '', '', 1, 0, 'F', '0', '0', 'qms:rectify:export', '#', 'admin', NOW(), '');

-- ----------------------------
-- 5. 定时任务：供应商整改逾期升级（KR-03）
-- ----------------------------
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('客诉供应商整改逾期升级提醒', 'DEFAULT', 'supplierRectifyOverdueTask.handle()', '0 0 8 * * ?', '3', '1', '0', 'admin', NOW(), '每天8点检查供应商整改单回复时限，逾期未回复自动升级提醒采购负责人（KR-03）');

-- 验证
SELECT '--- 客诉新增字段 ---' AS info;
SELECT column_name, column_type FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'qms_complaint' AND column_name IN ('supplier_id','supplier_name','supplier_contact','supplier_phone','delivery_no','supply_date','responsibility_type');
SELECT '--- 整改单表 ---' AS info;
SELECT COUNT(*) AS rectify_table_exists FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'qms_supplier_rectify';
SELECT '--- 菜单 ---' AS info;
SELECT menu_id, menu_name, parent_id, path, perms FROM sys_menu WHERE menu_name LIKE '%整改%';

-- ============================================================
-- MMS 生产管控模块 - 完整建表 + 菜单 + 字典 + 编号规则
-- Part 2: 异常表后续 + 菜单 + 字典 + 编号规则
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 18. 异常上报表 (EX-06) - 继续
DROP TABLE IF EXISTS `mms_abnormal`;
CREATE TABLE `mms_abnormal` (
  `abnormal_id`     BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '异常ID',
  `abnormal_no`     VARCHAR(50)   NOT NULL                 COMMENT '异常单号',
  `work_order_id`   BIGINT(20)    DEFAULT NULL             COMMENT '工单ID',
  `work_order_no`   VARCHAR(50)   DEFAULT ''               COMMENT '工单号',
  `resource_id`     BIGINT(20)    DEFAULT NULL             COMMENT '产能单元ID',
  `resource_name`   VARCHAR(100)  DEFAULT ''               COMMENT '产能单元名称',
  `abnormal_type`   VARCHAR(30)   NOT NULL                 COMMENT '异常类型(0设备 1物料 2质量 3安全 4其他)',
  `severity`        VARCHAR(10)   DEFAULT '0'              COMMENT '严重等级(0一般 1严重 2紧急)',
  `description`    TEXT                                   COMMENT '异常描述',
  `report_by`       VARCHAR(64)   DEFAULT ''               COMMENT '上报人',
  `report_time`     DATETIME      DEFAULT NULL             COMMENT '上报时间',
  `response_by`     VARCHAR(64)   DEFAULT ''               COMMENT '响应人',
  `response_time`   DATETIME      DEFAULT NULL             COMMENT '响应时间',
  `close_time`      DATETIME      DEFAULT NULL             COMMENT '关闭时间',
  `handle_result`  VARCHAR(500)  DEFAULT ''               COMMENT '处理结果',
  `status`          VARCHAR(10)   DEFAULT '0'              COMMENT '状态(0待响应 1处理中 2已关闭)',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`abnormal_id`),
  UNIQUE KEY `uk_abnormal_no` (`abnormal_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='异常上报表';

-- 19. 完工入库表 (WO-07)
DROP TABLE IF EXISTS `mms_finish_receipt`;
CREATE TABLE `mms_finish_receipt` (
  `finish_id`       BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '完工入库ID',
  `finish_no`       VARCHAR(50)   NOT NULL                 COMMENT '完工入库单号',
  `work_order_id`   BIGINT(20)    NOT NULL                 COMMENT '工单ID',
  `work_order_no`   VARCHAR(50)   DEFAULT ''               COMMENT '工单号',
  `product_id`      BIGINT(20)    DEFAULT NULL             COMMENT '产品ID',
  `product_code`    VARCHAR(50)   DEFAULT ''               COMMENT '产品编码',
  `product_name`    VARCHAR(100)  DEFAULT ''               COMMENT '产品名称',
  `unit`            VARCHAR(20)   DEFAULT ''               COMMENT '单位',
  `finish_qty`      DECIMAL(18,3) NOT NULL                 COMMENT '完工数量',
  `qualified_qty`   DECIMAL(18,3) DEFAULT 0.000            COMMENT '合格数量',
  `batch_no`        VARCHAR(50)   DEFAULT ''               COMMENT '批次号',
  `finish_time`     DATETIME      DEFAULT NULL             COMMENT '完工时间',
  `finish_by`       VARCHAR(64)   DEFAULT ''               COMMENT '完工人',
  `is_on_time`     CHAR(1)       DEFAULT '1'              COMMENT '是否按期(0否 1是)',
  `status`          VARCHAR(10)   DEFAULT '0'              COMMENT '状态(0待入库 1已入库)',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`finish_id`),
  UNIQUE KEY `uk_finish_no` (`finish_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='完工入库表';

-- ============================================================
-- 三、菜单数据 (菜单ID: 9000-9999)
-- ============================================================

-- 生产管控主目录
INSERT INTO `sys_menu`(`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
VALUES (9000, '生产管控', 0, 8, 'mms', NULL, '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', sysdate(), '', NULL, '生产管控目录');

-- 主数据管理子目录
INSERT INTO `sys_menu` VALUES (9010, '主数据管理', 9000, 1, 'master', NULL, '', 1, 0, 'M', '0', '0', '', 'tree', 'admin', sysdate(), '', NULL, '主数据管理目录');

-- 主数据菜单
INSERT INTO `sys_menu` VALUES (9011, 'BOM管理', 9010, 1, 'bom', 'mms/bom/index', '', 1, 0, 'C', '0', '0', 'mms:bom:list', 'tree-table', 'admin', sysdate(), '', NULL, 'BOM管理菜单');
INSERT INTO `sys_menu` VALUES (9012, '工艺路线', 9010, 2, 'route', 'mms/route/index', '', 1, 0, 'C', '0', '0', 'mms:route:list', 'tree-table', 'admin', sysdate(), '', NULL, '工艺路线菜单');
INSERT INTO `sys_menu` VALUES (9013, '工序定义', 9010, 3, 'process', 'mms/process/index', '', 1, 0, 'C', '0', '0', 'mms:process:list', 'edit', 'admin', sysdate(), '', NULL, '工序定义菜单');
INSERT INTO `sys_menu` VALUES (9014, '产能单元', 9010, 4, 'resource', 'mms/resource/index', '', 1, 0, 'C', '0', '0', 'mms:resource:list', 'server', 'admin', sysdate(), '', NULL, '产能单元菜单');
INSERT INTO `sys_menu` VALUES (9015, '班次管理', 9010, 5, 'shift', 'mms/shift/index', '', 1, 0, 'C', '0', '0', 'mms:shift:list', 'time', 'admin', sysdate(), '', NULL, '班次管理菜单');

-- 生产排产子目录
INSERT INTO `sys_menu` VALUES (9020, '生产排产', 9000, 2, 'plan', NULL, '', 1, 0, 'M', '0', '0', '', 'date', 'admin', sysdate(), '', NULL, '生产排产目录');

-- 排产菜单
INSERT INTO `sys_menu` VALUES (9021, '需求管理', 9020, 1, 'demand', 'mms/demand/index', '', 1, 0, 'C', '0', '0', 'mms:demand:list', 'form', 'admin', sysdate(), '', NULL, '生产需求管理菜单');
INSERT INTO `sys_menu` VALUES (9022, '主生产计划', 9020, 2, 'mps', 'mms/mps/index', '', 1, 0, 'C', '0', '0', 'mms:mps:list', 'post', 'admin', sysdate(), '', NULL, '主生产计划菜单');
INSERT INTO `sys_menu` VALUES (9023, '齐套检查', 9020, 3, 'kit', 'mms/kit/index', '', 1, 0, 'C', '0', '0', 'mms:kit:list', 'checkbox', 'admin', sysdate(), '', NULL, '齐套检查菜单');

-- 生产工单子目录
INSERT INTO `sys_menu` VALUES (9030, '生产工单', 9000, 3, 'order', NULL, '', 1, 0, 'M', '0', '0', '', 'documentation', 'admin', sysdate(), '', NULL, '生产工单目录');

-- 工单菜单
INSERT INTO `sys_menu` VALUES (9031, '工单管理', 9030, 1, 'workorder', 'mms/workorder/index', '', 1, 0, 'C', '0', '0', 'mms:workorder:list', 'form', 'admin', sysdate(), '', NULL, '工单管理菜单');

-- 生产执行子目录
INSERT INTO `sys_menu` VALUES (9040, '生产执行', 9000, 4, 'exec', NULL, '', 1, 0, 'M', '0', '0', '', 'clipboard', 'admin', sysdate(), '', NULL, '生产执行目录');

-- 执行菜单
INSERT INTO `sys_menu` VALUES (9041, '领料管理', 9040, 1, 'issue', 'mms/issue/index', '', 1, 0, 'C', '0', '0', 'mms:issue:list', 'shopping', 'admin', sysdate(), '', NULL, '领料管理菜单');
INSERT INTO `sys_menu` VALUES (9042, '退料管理', 9040, 2, 'return', 'mms/return/index', '', 1, 0, 'C', '0', '0', 'mms:return:list', 'reset', 'admin', sysdate(), '', NULL, '退料管理菜单');
INSERT INTO `sys_menu` VALUES (9043, '报工管理', 9040, 3, 'report', 'mms/report/index', '', 1, 0, 'C', '0', '0', 'mms:report:list', 'edit', 'admin', sysdate(), '', NULL, '报工管理菜单');
INSERT INTO `sys_menu` VALUES (9044, '过程质检', 9040, 4, 'qc', 'mms/qc/index', '', 1, 0, 'C', '0', '0', 'mms:qc:list', 'checkbox', 'admin', sysdate(), '', NULL, '过程质检菜单');
INSERT INTO `sys_menu` VALUES (9045, '异常管理', 9040, 5, 'abnormal', 'mms/abnormal/index', '', 1, 0, 'C', '0', '0', 'mms:abnormal:list', 'warning', 'admin', sysdate(), '', NULL, '异常管理菜单');
INSERT INTO `sys_menu` VALUES (9046, '完工入库', 9040, 6, 'finish', 'mms/finish/index', '', 1, 0, 'C', '0', '0', 'mms:finish:list', 'upload', 'admin', sysdate(), '', NULL, '完工入库菜单');

-- 看板报表
INSERT INTO `sys_menu` VALUES (9050, '生产看板', 9000, 5, 'dashboard', 'mms/dashboard/index', '', 1, 0, 'C', '0', '0', 'mms:dashboard:list', 'chart', 'admin', sysdate(), '', NULL, '生产看板菜单');

-- ============================================================
-- 四、按钮权限（各菜单的操作按钮）
-- ============================================================

-- BOM管理按钮
INSERT INTO `sys_menu` VALUES (901101, 'BOM查询', 9011, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:bom:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901102, 'BOM新增', 9011, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:bom:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901103, 'BOM修改', 9011, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:bom:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901104, 'BOM删除', 9011, 4, '', '', '', 1, 0, 'F', '0', '0', 'mms:bom:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901105, 'BOM导出', 9011, 5, '', '', '', 1, 0, 'F', '0', '0', 'mms:bom:export', '#', 'admin', sysdate(), '', NULL, '');

-- 工艺路线按钮
INSERT INTO `sys_menu` VALUES (901201, '路线查询', 9012, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:route:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901202, '路线新增', 9012, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:route:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901203, '路线修改', 9012, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:route:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901204, '路线删除', 9012, 4, '', '', '', 1, 0, 'F', '0', '0', 'mms:route:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901205, '路线导出', 9012, 5, '', '', '', 1, 0, 'F', '0', '0', 'mms:route:export', '#', 'admin', sysdate(), '', NULL, '');

-- 工序定义按钮
INSERT INTO `sys_menu` VALUES (901301, '工序查询', 9013, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:process:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901302, '工序新增', 9013, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:process:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901303, '工序修改', 9013, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:process:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901304, '工序删除', 9013, 4, '', '', '', 1, 0, 'F', '0', '0', 'mms:process:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901305, '工序导出', 9013, 5, '', '', '', 1, 0, 'F', '0', '0', 'mms:process:export', '#', 'admin', sysdate(), '', NULL, '');

-- 产能单元按钮
INSERT INTO `sys_menu` VALUES (901401, '产能单元查询', 9014, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:resource:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901402, '产能单元新增', 9014, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:resource:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901403, '产能单元修改', 9014, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:resource:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901404, '产能单元删除', 9014, 4, '', '', '', 1, 0, 'F', '0', '0', 'mms:resource:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901405, '产能单元导出', 9014, 5, '', '', '', 1, 0, 'F', '0', '0', 'mms:resource:export', '#', 'admin', sysdate(), '', NULL, '');

-- 班次按钮
INSERT INTO `sys_menu` VALUES (901501, '班次查询', 9015, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:shift:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901502, '班次新增', 9015, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:shift:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901503, '班次修改', 9015, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:shift:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (901504, '班次删除', 9015, 4, '', '', '', 1, 0, 'F', '0', '0', 'mms:shift:remove', '#', 'admin', sysdate(), '', NULL, '');

-- 需求管理按钮
INSERT INTO `sys_menu` VALUES (902101, '需求查询', 9021, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:demand:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (902102, '需求新增', 9021, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:demand:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (902103, '需求修改', 9021, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:demand:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (902104, '需求删除', 9021, 4, '', '', '', 1, 0, 'F', '0', '0', 'mms:demand:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (902105, '需求导出', 9021, 5, '', '', '', 1, 0, 'F', '0', '0', 'mms:demand:export', '#', 'admin', sysdate(), '', NULL, '');

-- 主生产计划按钮
INSERT INTO `sys_menu` VALUES (902201, '计划查询', 9022, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:mps:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (902202, '计划新增', 9022, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:mps:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (902203, '计划修改', 9022, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:mps:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (902204, '计划删除', 9022, 4, '', '', '', 1, 0, 'F', '0', '0', 'mms:mps:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (902205, '计划导出', 9022, 5, '', '', '', 1, 0, 'F', '0', '0', 'mms:mps:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (902206, '计划审批', 9022, 6, '', '', '', 1, 0, 'F', '0', '0', 'mms:mps:approve', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (902207, '计划发布', 9022, 7, '', '', '', 1, 0, 'F', '0', '0', 'mms:mps:release', '#', 'admin', sysdate(), '', NULL, '');

-- 齐套检查按钮
INSERT INTO `sys_menu` VALUES (902301, '齐套查询', 9023, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:kit:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (902302, '齐套新增', 9023, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:kit:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (902303, '齐套删除', 9023, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:kit:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (902304, '齐套导出', 9023, 4, '', '', '', 1, 0, 'F', '0', '0', 'mms:kit:export', '#', 'admin', sysdate(), '', NULL, '');

-- 工单管理按钮
INSERT INTO `sys_menu` VALUES (903101, '工单查询', 9031, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:workorder:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (903102, '工单新增', 9031, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:workorder:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (903103, '工单修改', 9031, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:workorder:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (903104, '工单删除', 9031, 4, '', '', '', 1, 0, 'F', '0', '0', 'mms:workorder:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (903105, '工单导出', 9031, 5, '', '', '', 1, 0, 'F', '0', '0', 'mms:workorder:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (903106, '工单下达', 9031, 6, '', '', '', 1, 0, 'F', '0', '0', 'mms:workorder:release', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (903107, '工单暂停', 9031, 7, '', '', '', 1, 0, 'F', '0', '0', 'mms:workorder:pause', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (903108, '工单关闭', 9031, 8, '', '', '', 1, 0, 'F', '0', '0', 'mms:workorder:close', '#', 'admin', sysdate(), '', NULL, '');

-- 领料管理按钮
INSERT INTO `sys_menu` VALUES (904101, '领料查询', 9041, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:issue:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904102, '领料新增', 9041, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:issue:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904103, '领料删除', 9041, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:issue:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904104, '领料导出', 9041, 4, '', '', '', 1, 0, 'F', '0', '0', 'mms:issue:export', '#', 'admin', sysdate(), '', NULL, '');

-- 退料管理按钮
INSERT INTO `sys_menu` VALUES (904201, '退料查询', 9042, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:return:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904202, '退料新增', 9042, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:return:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904203, '退料删除', 9042, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:return:remove', '#', 'admin', sysdate(), '', NULL, '');

-- 报工管理按钮
INSERT INTO `sys_menu` VALUES (904301, '报工查询', 9043, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:report:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904302, '报工新增', 9043, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:report:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904303, '报工删除', 9043, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:report:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904304, '报工审核', 9043, 4, '', '', '', 1, 0, 'F', '0', '0', 'mms:report:approve', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904305, '报工导出', 9043, 5, '', '', '', 1, 0, 'F', '0', '0', 'mms:report:export', '#', 'admin', sysdate(), '', NULL, '');

-- 过程质检按钮
INSERT INTO `sys_menu` VALUES (904401, '质检查询', 9044, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:qc:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904402, '质检新增', 9044, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:qc:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904403, '质检删除', 9044, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:qc:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904404, '质检导出', 9044, 4, '', '', '', 1, 0, 'F', '0', '0', 'mms:qc:export', '#', 'admin', sysdate(), '', NULL, '');

-- 异常管理按钮
INSERT INTO `sys_menu` VALUES (904501, '异常查询', 9045, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:abnormal:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904502, '异常新增', 9045, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:abnormal:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904503, '异常删除', 9045, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:abnormal:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904504, '异常处理', 9045, 4, '', '', '', 1, 0, 'F', '0', '0', 'mms:abnormal:handle', '#', 'admin', sysdate(), '', NULL, '');

-- 完工入库按钮
INSERT INTO `sys_menu` VALUES (904601, '入库查询', 9046, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:finish:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904602, '入库新增', 9046, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:finish:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (904603, '入库删除', 9046, 3, '', '', '', 1, 0, 'F', '0', '0', 'mms:finish:remove', '#', 'admin', sysdate(), '', NULL, '');

-- ============================================================
-- 五、字典类型 + 字典数据
-- ============================================================

-- 字典类型
INSERT INTO `sys_dict_type`(`dict_id`,`dict_name`,`dict_type`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
VALUES (200, 'MMS-BOM类型', 'mms_bom_type', '0', 'admin', sysdate(), '', NULL, 'BOM清单类型');
INSERT INTO `sys_dict_type` VALUES (201, 'MMS-需求类型', 'mms_demand_type', '0', 'admin', sysdate(), '', NULL, '生产需求类型');
INSERT INTO `sys_dict_type` VALUES (202, 'MMS-需求状态', 'mms_demand_status', '0', 'admin', sysdate(), '', NULL, '生产需求状态');
INSERT INTO `sys_dict_type` VALUES (203, 'MMS-计划状态', 'mms_mps_status', '0', 'admin', sysdate(), '', NULL, '主生产计划状态');
INSERT INTO `sys_dict_type` VALUES (204, 'MMS-齐套状态', 'mms_kit_status', '0', 'admin', sysdate(), '', NULL, '齐套检查状态');
INSERT INTO `sys_dict_type` VALUES (205, 'MMS-工单类型', 'mms_order_type', '0', 'admin', sysdate(), '', NULL, '工单类型');
INSERT INTO `sys_dict_type` VALUES (206, 'MMS-工单状态', 'mms_workorder_status', '0', 'admin', sysdate(), '', NULL, '工单状态');
INSERT INTO `sys_dict_type` VALUES (207, 'MMS-工序类型', 'mms_process_type', '0', 'admin', sysdate(), '', NULL, '工序类型');
INSERT INTO `sys_dict_type` VALUES (208, 'MMS-产能单元类型', 'mms_resource_type', '0', 'admin', sysdate(), '', NULL, '产能单元类型');
INSERT INTO `sys_dict_type` VALUES (209, 'MMS-异常类型', 'mms_abnormal_type', '0', 'admin', sysdate(), '', NULL, '异常类型');
INSERT INTO `sys_dict_type` VALUES (210, 'MMS-异常等级', 'mms_severity', '0', 'admin', sysdate(), '', NULL, '异常严重等级');
INSERT INTO `sys_dict_type` VALUES (211, 'MMS-质检类型', 'mms_qc_type', '0', 'admin', sysdate(), '', NULL, '质检检验类型');
INSERT INTO `sys_dict_type` VALUES (212, 'MMS-质检结论', 'mms_qc_result', '0', 'admin', sysdate(), '', NULL, '质检结论');
INSERT INTO `sys_dict_type` VALUES (213, 'MMS-报工状态', 'mms_report_status', '0', 'admin', sysdate(), '', NULL, '报工状态');
INSERT INTO `sys_dict_type` VALUES (214, 'MMS-领料状态', 'mms_issue_status', '0', 'admin', sysdate(), '', NULL, '领料状态');
INSERT INTO `sys_dict_type` VALUES (215, 'MMS-优先级', 'mms_priority', '0', 'admin', sysdate(), '', NULL, '生产优先级');
INSERT INTO `sys_dict_type` VALUES (216, 'MMS-数据状态', 'mms_data_status', '0', 'admin', sysdate(), '', NULL, '主数据启用停用');

-- 字典数据
INSERT INTO `sys_dict_data`(`dict_code`,`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default
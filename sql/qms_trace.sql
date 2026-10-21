-- =============================================
-- QMS 二期 · 质量追溯模块 SQL脚本
-- 包含：批次谱系表 + 字典 + 菜单 + 角色授权
-- =============================================

-- =============================================
-- 一、业务表 DDL
-- =============================================

-- 批次谱系表（核心）
DROP TABLE IF EXISTS qms_lot_genealogy;
CREATE TABLE qms_lot_genealogy (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    parent_batch_no VARCHAR(64)  NOT NULL                 COMMENT '投入批次号（原料/半成品）',
    child_batch_no  VARCHAR(64)                           COMMENT '产出批次号（半成品/成品，NULL=断点未登记）',
    work_order_no   VARCHAR(64)                           COMMENT '生产工单号',
    op_id           VARCHAR(32)                           COMMENT '工序ID',
    op_name         VARCHAR(100)                          COMMENT '工序名称',
    material_id     BIGINT                                COMMENT '物料ID（复用仓库主数据）',
    material_code   VARCHAR(64)                           COMMENT '物料编码',
    material_name   VARCHAR(200)                          COMMENT '物料名称',
    qty_in          DECIMAL(18,4)                         COMMENT '投入数量',
    qty_out         DECIMAL(18,4)                         COMMENT '产出数量',
    line_id         VARCHAR(32)                           COMMENT '产线ID',
    line_name       VARCHAR(100)                          COMMENT '产线名称',
    operator        VARCHAR(64)                           COMMENT '操作员',
    supplier_id     BIGINT                                COMMENT '供应商ID（原料批次关联）',
    supplier_name   VARCHAR(200)                          COMMENT '供应商名称',
    source_type     VARCHAR(20)  DEFAULT 'manual'         COMMENT '来源（feeding=投料, report=报工, complete=完工, transfer=调拨, rework=返工, merge=合并, split=拆分, manual=补录）',
    trace_time      DATETIME                              COMMENT '谱系时间',
    break_flag      TINYINT      DEFAULT 0                COMMENT '断点标记（0=正常 1=断点）',
    break_reason    VARCHAR(500)                          COMMENT '断点原因',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    status          CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (id),
    INDEX idx_parent_batch (parent_batch_no),
    INDEX idx_child_batch (child_batch_no),
    INDEX idx_work_order (work_order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批次谱系表';

-- SN级追溯预留表（本期仅建表，不实现UI）
DROP TABLE IF EXISTS qms_trace_sn;
CREATE TABLE qms_trace_sn (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    sn_code         VARCHAR(128) NOT NULL                 COMMENT '单件序列号',
    batch_no        VARCHAR(64)                           COMMENT '关联批次号',
    work_order_no   VARCHAR(64)                           COMMENT '工单号',
    material_id     BIGINT                                COMMENT '物料ID',
    material_code   VARCHAR(64)                           COMMENT '物料编码',
    status          VARCHAR(20)  DEFAULT '0'              COMMENT '状态（0在制 1入库 2发货 3退货）',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sn_code (sn_code),
    INDEX idx_batch (batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SN级追溯预留表';


-- =============================================
-- 二、字典数据
-- =============================================

-- 谱系来源类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('谱系来源类型', 'qms_genealogy_source', '0', 'admin', sysdate(), '谱系来源类型');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '投料', 'feeding', 'qms_genealogy_source', 'N', '0', 'admin', sysdate()),
(2, '报工', 'report', 'qms_genealogy_source', 'N', '0', 'admin', sysdate()),
(3, '完工', 'complete', 'qms_genealogy_source', 'N', '0', 'admin', sysdate()),
(4, '调拨', 'transfer', 'qms_genealogy_source', 'N', '0', 'admin', sysdate()),
(5, '返工', 'rework', 'qms_genealogy_source', 'N', '0', 'admin', sysdate()),
(6, '合并', 'merge', 'qms_genealogy_source', 'N', '0', 'admin', sysdate()),
(7, '拆分', 'split', 'qms_genealogy_source', 'N', '0', 'admin', sysdate()),
(8, '手工补录', 'manual', 'qms_genealogy_source', 'Y', '0', 'admin', sysdate());

-- 追溯方向
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('追溯方向', 'qms_trace_direction', '0', 'admin', sysdate(), '追溯方向');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1, '正向追溯', 'forward', 'qms_trace_direction', 'N', '0', 'admin', sysdate()),
(2, '反向追溯', 'backward', 'qms_trace_direction', 'Y', '0', 'admin', sysdate());


-- =============================================
-- 三、菜单与权限
-- =============================================
SET @qmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name = '质量管理' AND parent_id = 0);

-- 质量追溯目录
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('质量追溯', @qmsParentId, 11, 'trace', NULL, '', 'QmsTraceDir', 1, 0, 'M', '0', '0', '', 'cascader', 'admin', sysdate(), '质量追溯目录');
SET @qmsTraceDirId = LAST_INSERT_ID();

-- 追溯查询页面
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('追溯查询', @qmsTraceDirId, 1, 'search', 'qms/trace/index', '', 'QmsTraceSearch', 1, 0, 'C', '0', '0', 'qms:trace:search', 'search', 'admin', sysdate(), '正反向追溯查询');
SET @qmsTraceSearchId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('正向追溯', @qmsTraceSearchId, 1, '', 'F', '0', '0', 'qms:trace:forward', 'admin', sysdate()),
('反向追溯', @qmsTraceSearchId, 2, '', 'F', '0', '0', 'qms:trace:backward', 'admin', sysdate()),
('追溯报告', @qmsTraceSearchId, 3, '', 'F', '0', '0', 'qms:trace:report', 'admin', sysdate());

-- 谱系管理页面
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('谱系管理', @qmsTraceDirId, 2, 'genealogy', 'qms/trace/genealogy', '', 'QmsTraceGenealogy', 1, 0, 'C', '0', '0', 'qms:genealogy:list', 'tree-table', 'admin', sysdate(), '批次谱系管理与补录');
SET @qmsGenealogyId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('谱系查询', @qmsGenealogyId, 1, '', 'F', '0', '0', 'qms:genealogy:query', 'admin', sysdate()),
('谱系新增', @qmsGenealogyId, 2, '', 'F', '0', '0', 'qms:genealogy:add', 'admin', sysdate()),
('谱系修改', @qmsGenealogyId, 3, '', 'F', '0', '0', 'qms:genealogy:edit', 'admin', sysdate()),
('谱系删除', @qmsGenealogyId, 4, '', 'F', '0', '0', 'qms:genealogy:remove', 'admin', sysdate()),
('谱系导出', @qmsGenealogyId, 5, '', 'F', '0', '0', 'qms:genealogy:export', 'admin', sysdate());

-- 完整度看板
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('谱系完整度', @qmsTraceDirId, 3, 'dashboard', 'qms/trace/dashboard', '', 'QmsTraceDashboard', 1, 0, 'C', '0', '0', 'qms:trace:dashboard', 'dashboard', 'admin', sysdate(), '谱系完整度看板');

-- 授权admin角色
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'qms:trace%%' OR perms LIKE 'qms:genealogy%%';
-- 目录菜单授权
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, @qmsTraceDirId;

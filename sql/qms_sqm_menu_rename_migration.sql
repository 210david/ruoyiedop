-- =============================================
-- QMS 供应商质量管理 → 供应商质量管控 菜单改名迁移
-- 适用：已执行过 qms_p1_p2_all.sql 的存量数据库
-- 日期：2026-08-13
-- 说明：仅改菜单名称，不改变菜单ID/路径/权限，不影响已有数据
-- =============================================

-- 1. 目录菜单改名（先用变量存储parent_id，避免MySQL自表子查询限制）
SET @qmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name = '质量管理' AND parent_id = 0);
UPDATE sys_menu 
SET menu_name = '供应商质量管控', 
    remark = '供应商质量管控目录'
WHERE menu_name = '供应商质量管理' 
  AND parent_id = @qmsParentId;

-- 2. 验证改名结果
SELECT menu_id, menu_name, parent_id, order_num, path, menu_type, perms, remark
FROM sys_menu 
WHERE parent_id = @qmsParentId
  AND menu_name = '供应商质量管控';

-- =============================================
-- 3. 新增 SQM→PMS 推送日志表（FR-SQM-06）
-- =============================================
CREATE TABLE IF NOT EXISTS qms_sqm_push_log (
    log_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    supplier_id     BIGINT       NOT NULL                 COMMENT '供应商ID',
    supplier_name   VARCHAR(255)                           COMMENT '供应商名称（冗余）',
    eval_id         BIGINT                                COMMENT 'QMS质量评价ID',
    eval_period     VARCHAR(20)                           COMMENT '评价周期',
    quality_score   DECIMAL(5,2)                           COMMENT '质量评分',
    grade           VARCHAR(2)                            COMMENT '等级(A/B/C/D)',
    pass_rate       DECIMAL(5,2)                           COMMENT '合格率(%)',
    ppm             DECIMAL(10,2)                          COMMENT 'PPM',
    push_status     VARCHAR(10)  DEFAULT '0'              COMMENT '推送状态(0待推送 1成功 2失败)',
    push_time       DATETIME                              COMMENT '推送时间',
    retry_count     INT          DEFAULT 0                COMMENT '重试次数',
    error_msg       VARCHAR(500)                           COMMENT '失败原因',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志(0存在 2删除)',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    PRIMARY KEY (log_id),
    KEY idx_supplier (supplier_id),
    KEY idx_period (eval_period),
    KEY idx_status (push_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SQM→PMS 质量评分推送日志';

-- 4. 推送日志菜单（挂在供应商质量管控目录下）
SET @qmsSqmDirId = (SELECT menu_id FROM sys_menu WHERE menu_name = '供应商质量管控' 
    AND parent_id = @qmsParentId);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '推送日志', @qmsSqmDirId, 3, 'pushLog', 'qms/sqmPushLog/index', '', 'QmsSqmPushLog', 1, 0, 'C', '1', '0', 'qms:sqmPushLog:list', 'log', 'admin', sysdate(), 'SQM→PMS推送日志'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '推送日志' AND parent_id = @qmsSqmDirId);
SET @pushLogMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '推送日志查询', @pushLogMenuId, 1, '', 'F', '0', '0', 'qms:sqmPushLog:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'qms:sqmPushLog:query' AND parent_id = @pushLogMenuId);

-- 授权admin角色
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'qms:sqmPushLog%%';

-- 5. 验证最终菜单结构
SELECT m1.menu_name AS '一级', m2.menu_name AS '二级', m2.path, m2.component, m2.perms, m2.visible
FROM sys_menu m1
JOIN sys_menu m2 ON m2.parent_id = m1.menu_id
WHERE m1.menu_name = '供应商质量管控'
  AND m1.parent_id = @qmsParentId
ORDER BY m2.order_num;

-- ============================================================
-- 内部审核增强：检查表模板 + 审核检查项（2026-09-18）
-- 方案B：检查表模板 + 审核报告自动生成
-- ============================================================

-- 1. 检查表模板主表
CREATE TABLE IF NOT EXISTS qms_audit_check_template (
    template_id     BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '模板ID',
    template_name   VARCHAR(100) NOT NULL                 COMMENT '模板名称',
    standard        VARCHAR(100) DEFAULT 'ISO9001'        COMMENT '适用标准（ISO9001等）',
    remark          VARCHAR(500)                          COMMENT '备注',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    PRIMARY KEY (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内审检查表模板';

-- 2. 检查表模板检查项
CREATE TABLE IF NOT EXISTS qms_audit_check_template_item (
    tmpl_item_id    BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '模板项ID',
    template_id     BIGINT       NOT NULL                 COMMENT '模板ID',
    clause_ref      VARCHAR(100) NOT NULL                 COMMENT '条款引用（如ISO9001 8.5.1）',
    check_content   VARCHAR(1000) NOT NULL                COMMENT '检查内容',
    sort_no         INT          DEFAULT 0                COMMENT '排序',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    PRIMARY KEY (tmpl_item_id),
    KEY idx_tmpl (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内审检查表模板检查项';

-- 3. 审核检查项（审核计划执行时的实际检查记录）
CREATE TABLE IF NOT EXISTS qms_audit_check_item (
    check_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '检查项ID',
    audit_plan_id   BIGINT       NOT NULL                 COMMENT '审核计划ID',
    clause_ref      VARCHAR(100) NOT NULL                 COMMENT '条款引用',
    check_content   VARCHAR(1000) NOT NULL                COMMENT '检查内容',
    check_result    CHAR(1)      DEFAULT '0'              COMMENT '检查结果（0=符合 1=不符合 2=不适用）',
    evidence        VARCHAR(1000)                         COMMENT '客观证据/审核记录',
    sort_no         INT          DEFAULT 0                COMMENT '排序',
    del_flag        CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time     DATETIME                              COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time     DATETIME                              COMMENT '更新时间',
    PRIMARY KEY (check_id),
    KEY idx_plan (audit_plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内审检查项记录';

-- 4. 预置模板：ISO9001 全要素检查表
INSERT INTO qms_audit_check_template (template_name, standard, remark, del_flag, create_by, create_time)
SELECT 'ISO9001质量管理体系全要素检查表', 'ISO9001', '覆盖ISO9001:2015主要条款的通用内审检查表', '0', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM qms_audit_check_template WHERE template_name = 'ISO9001质量管理体系全要素检查表');

SET @tplId = (SELECT template_id FROM qms_audit_check_template WHERE template_name = 'ISO9001质量管理体系全要素检查表' LIMIT 1);

INSERT INTO qms_audit_check_template_item (template_id, clause_ref, check_content, sort_no, del_flag)
SELECT @tplId, t.clause_ref, t.check_content, t.sort_no, '0' FROM (
    SELECT '4.4'  AS clause_ref, '组织是否建立并保持质量管理体系所需的过程及其相互作用？' AS check_content, 1 AS sort_no UNION ALL
    SELECT '5.1', '最高管理者是否证实其领导作用并承诺对质量管理体系的有效性负责？', 2 UNION ALL
    SELECT '5.3', '组织的岗位、职责和权限是否得到分配、沟通和理解？', 3 UNION ALL
    SELECT '6.1', '是否识别并应对与质量管理体系有关的风险和机遇？', 4 UNION ALL
    SELECT '6.2', '质量目标是否可测量、与质量方针一致并得到监视？', 5 UNION ALL
    SELECT '7.1.5', '监视和测量资源是否适合所开展的监视和测量活动并得到维护？', 6 UNION ALL
    SELECT '7.2', '人员能力要求是否确定，并采取培训等措施获得所需能力？', 7 UNION ALL
    SELECT '7.5', '质量管理体系文件化信息是否得到创建、更新和控制？', 8 UNION ALL
    SELECT '8.1', '运行策划是否确定过程要求并按准则实施和控制？', 9 UNION ALL
    SELECT '8.3', '产品和服务的设计开发是否得到策划、控制和评审？', 10 UNION ALL
    SELECT '8.4', '外部提供的过程、产品和服务是否得到控制与评价？', 11 UNION ALL
    SELECT '8.5.1', '生产和服务提供的控制是否在受控条件下进行？', 12 UNION ALL
    SELECT '8.5.2', '是否在需要时识别并实施标识和可追溯性要求？', 13 UNION ALL
    SELECT '8.6', '产品和服务放行前是否验证满足要求并保留证据？', 14 UNION ALL
    SELECT '8.7', '不合格输出是否得到识别和控制？', 15 UNION ALL
    SELECT '9.1', '是否监视、测量、分析和评价质量管理体系的绩效？', 16 UNION ALL
    SELECT '9.2', '是否按计划实施内部审核并获得审核结果信息？', 17 UNION ALL
    SELECT '9.3', '管理评审输入输出是否完整并形成结论？', 18 UNION ALL
    SELECT '10.2', '不合格和纠正措施是否得到及时识别与实施？', 19 UNION ALL
    SELECT '10.3', '组织是否持续改进质量管理体系的适宜性、充分性和有效性？', 20
) t
WHERE NOT EXISTS (SELECT 1 FROM qms_audit_check_template_item WHERE template_id = @tplId);

-- 5. 按钮权限：生成检查表、生成审核报告
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '生成检查表', m.menu_id, 10, '', '', '', '', 1, 0, 'F', '0', '0', 'qms:audit:check', '#', 'admin', sysdate(), '内审检查表按钮权限'
FROM sys_menu m WHERE m.perms = 'qms:audit:list' AND m.menu_type = 'C'
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'qms:audit:check');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '生成审核报告', m.menu_id, 11, '', '', '', '', 1, 0, 'F', '0', '0', 'qms:audit:report', '#', 'admin', sysdate(), '内审报告按钮权限'
FROM sys_menu m WHERE m.perms = 'qms:audit:list' AND m.menu_type = 'C'
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'qms:audit:report');

-- admin角色授权
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms IN ('qms:audit:check', 'qms:audit:report');

-- ==================================================================
-- EDOP × WorkBuddy 连接器：全量幂等安装脚本
-- 文件：edop_connector_install.sql
--
-- 用途：一条命令完成连接器所需的全部数据库对象，可安全重复执行，
--       重复执行不会产生任何重复数据（幂等）。
--
-- 合并自：
--   edop_connector_grant.sql              （授权表 + 授权菜单）
--   edop_mcp_service_account.sql          （服务账号 + 只读角色 + 权限）
--   upgrade_20261109_mcp_new_tools_perms.sql （4 个新工具权限点）
--
-- 相对原脚本的修正（原脚本重复执行会产生重复菜单，已实测）：
--   1. 授权菜单 INSERT 增加 NOT EXISTS 保护，不再依赖 LAST_INSERT_ID()；
--      F 按钮的 parent_id 改为按 perms 动态查父菜单，重跑也不会挂错父级。
--   2. 服务账号 INSERT 的 NOT EXISTS 补上 del_flag 兼容，避免逻辑删除后重建失败。
--   3. 角色-菜单授权改为集合差集插入（INSERT ... SELECT ... NOT EXISTS），
--      一次覆盖标准 5 工具 + 新增 4 工具的父级菜单链，重复执行零副作用。
--   4. 所有校验查询只读，不影响数据。
--
-- 前置：MySQL 5.7+ / 8.0；库为 RuoYi 标准结构（sys_menu/sys_role/sys_user 等）
-- 执行：mysql --default-character-set=utf8mb4 -uroot -p <库名> < edop_connector_install.sql
--       （务必带 --default-character-set=utf8mb4，否则中文菜单名会乱码）
-- ==================================================================

SET NAMES utf8mb4;

-- ==================================================================
-- 0. 执行前快照（只读，便于对比执行前后）
-- ==================================================================
SELECT '=== 执行前快照 ===' AS section;
SELECT
    (SELECT COUNT(*) FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'edop_connector_grant') AS grant_table,
    (SELECT COUNT(*) FROM sys_user  WHERE user_name = 'edop-mcp')             AS svc_user,
    (SELECT COUNT(*) FROM sys_role  WHERE role_key  = 'edop_mcp_readonly')     AS svc_role,
    (SELECT COUNT(*) FROM sys_menu  WHERE perms LIKE 'edop:grant:%')           AS grant_menus,
    (SELECT COUNT(*) FROM sys_menu  WHERE perms = 'pjm:project:list')          AS pjm_perm;


-- ==================================================================
-- 1. 逐用户授权表
-- ==================================================================
CREATE TABLE IF NOT EXISTS `edop_connector_grant` (
  `grant_id`         BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '授权ID',
  `user_id`          BIGINT(20)   NOT NULL                COMMENT 'EDOP用户ID',
  `grant_token_hash` CHAR(64)     NOT NULL                COMMENT 'grant_token的SHA-256哈希',
  `phone_snapshot`   VARCHAR(20)  DEFAULT ''              COMMENT '授权时手机号快照(换绑检测)',
  `client_desc`      VARCHAR(100) DEFAULT ''              COMMENT '授权来源描述',
  `status`           CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态（0正常 1停用）',
  `expire_time`      DATETIME     DEFAULT NULL            COMMENT '过期时间(90天滚动续期)',
  `last_used_time`   DATETIME     DEFAULT NULL            COMMENT '最近使用时间(僵尸授权依据)',
  `create_by`        VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`      DATETIME     DEFAULT NULL            COMMENT '创建时间',
  `update_by`        VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`      DATETIME     DEFAULT NULL            COMMENT '更新时间',
  `remark`           VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `del_flag`         CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  PRIMARY KEY (`grant_id`),
  UNIQUE KEY `uk_token_hash` (`grant_token_hash`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='连接器逐用户授权表';


-- ==================================================================
-- 2. 授权管理菜单（系统管理 → 连接器授权）
--    幂等要点：C 菜单按 perms 判重；F 按钮的 parent_id 动态取 C 的 menu_id，
--    不再用 LAST_INSERT_ID()，因此重跑既不会新增，也不会挂到别的父级下。
-- ==================================================================

-- 2.1 菜单目录（C）
INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '连接器授权', 1, 12, 'connectorGrant', 'system/connectorGrant/index', NULL, '',
       1, 0, 'C', '0', '0', 'edop:grant:list', 'guide', 'admin', NOW(),
       'EDOP连接器逐用户授权管理菜单'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT menu_id FROM sys_menu WHERE perms = 'edop:grant:list') AS _m
);

-- 2.2 授权查询按钮（F）
INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '授权查询', p.menu_id, 1, '#', '', NULL, '',
       1, 0, 'F', '0', '0', 'edop:grant:query', '#', 'admin', NOW()
FROM (SELECT menu_id FROM sys_menu WHERE perms = 'edop:grant:list' ORDER BY menu_id LIMIT 1) p
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT menu_id FROM sys_menu WHERE perms = 'edop:grant:query') AS _q
);

-- 2.3 授权吊销按钮（F）
INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '授权吊销', p.menu_id, 2, '#', '', NULL, '',
       1, 0, 'F', '0', '0', 'edop:grant:revoke', '#', 'admin', NOW()
FROM (SELECT menu_id FROM sys_menu WHERE perms = 'edop:grant:list' ORDER BY menu_id LIMIT 1) p
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT menu_id FROM sys_menu WHERE perms = 'edop:grant:revoke') AS _r
);

-- 2.4 已存在 F 按钮但 parent_id 挂错/失效时，纠正到当前 C 菜单下（含被误删父级的情况）
UPDATE sys_menu f
JOIN (SELECT menu_id FROM sys_menu WHERE perms = 'edop:grant:list' ORDER BY menu_id LIMIT 1) p
SET f.parent_id = p.menu_id
WHERE f.perms IN ('edop:grant:query', 'edop:grant:revoke')
  AND f.parent_id <> p.menu_id;


-- ==================================================================
-- 3. MCP 只读服务账号 edop-mcp + 角色 edop_mcp_readonly
--    仅在需要 service 模式（x-api-key）或本机 stdio 自用时才用得上；
--    纯逐用户授权部署可跳过本段（脚本仍可安全执行）。
-- ==================================================================

-- 3.1 角色
INSERT INTO sys_role
  (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly,
   status, del_flag, create_by, create_time, remark)
SELECT 'EDOP-MCP只读服务', 'edop_mcp_readonly', 99, '1', 1, 1, '0', '0', 'admin', NOW(),
       'WorkBuddy 连接器专用只读服务账号角色（勿人工登录）'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT role_id FROM sys_role WHERE role_key = 'edop_mcp_readonly' AND del_flag = '0') AS _r
);

-- 3.2 服务账号（挂在第一个可用部门下）
--     密码哈希对应明文见交付说明；生产部署后建议立即改密并同步 profiles/*.json
INSERT INTO sys_user
  (dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar,
   password, status, del_flag, create_by, create_time, remark)
SELECT (SELECT dept_id FROM sys_dept WHERE status = '0' AND del_flag = '0'
        ORDER BY parent_id, order_num LIMIT 1),
       'edop-mcp', 'MCP服务账号', '00', '', '', '2', '',
       '$2a$10$bbMq9tKvDJT2Of744WRJgO/ipX7HZKgrSDzuQ0BbGXolRZQBxnBTO',
       '0', '0', 'admin', NOW(), 'WorkBuddy 连接器专用服务账号（勿人工登录）'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT user_id FROM sys_user WHERE user_name = 'edop-mcp' AND del_flag = '0') AS _u
);

-- 3.3 用户-角色关联
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM sys_user u
JOIN sys_role r ON r.role_key = 'edop_mcp_readonly' AND r.del_flag = '0'
WHERE u.user_name = 'edop-mcp' AND u.del_flag = '0'
  AND NOT EXISTS (
      SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id
  );


-- ==================================================================
-- 4. 缺失的权限点菜单（仅补缺失，已存在则跳过）
--    pjm:project:list 在部分分支原本没有权限字符，需补一个 F 按钮
-- ==================================================================
INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '项目列表查询',
       COALESCE((SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_id = 906203) AS _p), 906203),
       1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'pjm:project:list', '#', 'admin', NOW(),
       'MCP 只读服务账号权限（LYJS 分支）'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT menu_id FROM sys_menu WHERE perms = 'pjm:project:list') AS _x
);


-- ==================================================================
-- 5. 服务账号角色授权：12 个权限点 + 其完整父级菜单链
--    幂等要点：INSERT ... SELECT ... NOT EXISTS 做集合差集，
--    一次补全"缺的"，已有的不动，重复执行插入 0 行。
-- ==================================================================
INSERT INTO sys_role_menu (role_id, menu_id)
WITH RECURSIVE target_menus AS (
    -- 12 个工具权限点本身的菜单
    SELECT m.menu_id, m.parent_id
    FROM sys_menu m
    WHERE m.perms IN (
        'bi:dashboard:view', 'bi:metric:list',
        'wms:inventory:list', 'wms:statistics:list',
        'marketing:order:list', 'marketing:report:view',
        'pms:order:list', 'pms:dashboard:view',
        'pjm:project:list', 'safety:hazard:list',
        'qms:task:list', 'bid:project:list'
    )
    UNION
    -- 逐级向上带出父级菜单（RuoYi hasPermi 会校验菜单链）
    SELECT p.menu_id, p.parent_id
    FROM sys_menu p
    JOIN target_menus t ON p.menu_id = t.parent_id
    WHERE p.parent_id <> 0 OR p.menu_id = 0
)
SELECT r.role_id, tm.menu_id
FROM sys_role r
CROSS JOIN (SELECT DISTINCT menu_id FROM target_menus) tm
WHERE r.role_key = 'edop_mcp_readonly' AND r.del_flag = '0'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = tm.menu_id
  );


-- ==================================================================
-- 6. 业务角色授权（逐用户模式下用户本身要有的权限点）
--    说明：逐用户模式按用户 @DataScope 裁剪，用户所属角色需持有对应权限点。
--    本段按既有约定给管理类/采购类角色补 pjm:project:list 与 bid:project:list。
--    如客户不希望自动扩权，可注释掉本节（不影响其他部分幂等性）。
-- ==================================================================

-- 6.1 项目管理查询 → 管理类 4 角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.perms = 'pjm:project:list'
WHERE r.role_key IN ('common_user', 'dept_leader', 'business_manager', 'general_manager')
  AND r.del_flag = '0' AND r.status = '0'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );

-- 6.2 投标管理查询 → 采购类 5 角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.perms = 'bid:project:list'
WHERE r.role_key IN ('pms_director', 'pms_manager', 'pms_buyer', 'pms_receiver', 'pms_supplier_mgr')
  AND r.del_flag = '0' AND r.status = '0'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );


-- ==================================================================
-- 7. 执行后校验（只读）
-- ==================================================================

SELECT '=== 1. 授权表 ===' AS section;
SELECT COUNT(*) AS table_exists
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'edop_connector_grant';
-- 期望 1

SELECT '=== 2. 授权菜单（应各 1 条，重复执行不增长）===' AS section;
SELECT perms, COUNT(*) AS cnt, GROUP_CONCAT(menu_id ORDER BY menu_id) AS menu_ids
FROM sys_menu
WHERE perms LIKE 'edop:grant:%'
GROUP BY perms;
-- 期望每行 cnt = 1

SELECT '=== 3. 服务账号与角色 ===' AS section;
SELECT u.user_id, u.user_name, u.status, u.del_flag, r.role_key, r.data_scope
FROM sys_user u
LEFT JOIN sys_user_role ur ON ur.user_id = u.user_id
LEFT JOIN sys_role r ON r.role_id = ur.role_id AND r.role_key = 'edop_mcp_readonly'
WHERE u.user_name = 'edop-mcp';
-- 期望 1 行，status=0 del_flag=0，role_key 非空

SELECT '=== 4. 12 个权限点齐备性（缺失会导致对应工具 403）===' AS section;
SELECT p.perms,
       CASE WHEN m.menu_id IS NULL THEN '缺失' ELSE '存在' END AS status,
       m.menu_id
FROM (
    SELECT 'bi:dashboard:view' AS perms UNION ALL SELECT 'bi:metric:list'
    UNION ALL SELECT 'wms:inventory:list'     UNION ALL SELECT 'wms:statistics:list'
    UNION ALL SELECT 'marketing:order:list'   UNION ALL SELECT 'marketing:report:view'
    UNION ALL SELECT 'pms:order:list'         UNION ALL SELECT 'pms:dashboard:view'
    UNION ALL SELECT 'pjm:project:list'       UNION ALL SELECT 'safety:hazard:list'
    UNION ALL SELECT 'qms:task:list'          UNION ALL SELECT 'bid:project:list'
) p
LEFT JOIN sys_menu m ON m.perms = p.perms
ORDER BY p.perms;
-- 期望全部"存在"（同一权限点多菜单属正常，会多行）

SELECT '=== 5. 服务账号已授权菜单数 ===' AS section;
SELECT COUNT(*) AS menu_count
FROM sys_role_menu rm
JOIN sys_role r ON r.role_id = rm.role_id
WHERE r.role_key = 'edop_mcp_readonly';
-- 期望 >= 12（含父级链，通常 20~60）

SELECT '=== 6. 12 权限点是否都已授权给服务账号（应为 0 缺失）===' AS section;
SELECT COUNT(*) AS missing_count
FROM sys_menu m
WHERE m.perms IN (
        'bi:dashboard:view', 'bi:metric:list',
        'wms:inventory:list', 'wms:statistics:list',
        'marketing:order:list', 'marketing:report:view',
        'pms:order:list', 'pms:dashboard:view',
        'pjm:project:list', 'safety:hazard:list',
        'qms:task:list', 'bid:project:list'
      )
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      JOIN sys_role r ON r.role_id = rm.role_id
      WHERE rm.menu_id = m.menu_id AND r.role_key = 'edop_mcp_readonly'
  );
-- 期望 0

SELECT '=== 7. 当前授权记录数（生产首次部署应为 0）===' AS section;
SELECT COUNT(*) AS grant_total,
       SUM(CASE WHEN status = '0' AND (expire_time IS NULL OR expire_time > NOW())
                THEN 1 ELSE 0 END) AS active_grants
FROM edop_connector_grant;

SELECT '=== 安装完成 ===' AS section;

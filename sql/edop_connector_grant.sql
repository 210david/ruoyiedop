-- ==================================================================
-- EDOP 连接器逐用户授权（Phase 2-2.2）
-- 表：edop_connector_grant（grant_token 哈希入库，明文仅颁发时返回一次）
-- 菜单：系统管理 → 连接器授权（C）+ 查询/吊销按钮（F）
-- 规范：3.1/3.6 显式 COLLATE；3.4 M/C/F 三级
-- ==================================================================

-- 1. 授权表
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

-- 2. 菜单（挂在 系统管理=1 下）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('连接器授权', 1, 12, 'connectorGrant', 'system/connectorGrant/index', 1, 0, 'C', '0', '0', 'edop:grant:list', 'guide', 'admin', SYSDATE(), 'EDOP连接器逐用户授权管理菜单');

SET @grantMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES
('授权查询', @grantMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'edop:grant:query',  '#', 'admin', SYSDATE()),
('授权吊销', @grantMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'edop:grant:revoke', '#', 'admin', SYSDATE());

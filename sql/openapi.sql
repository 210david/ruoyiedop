-- ================================================================
-- 开放接口(Open API)管理 - 建表 + 菜单
-- 功能：管理对接方API Key，用于 /openapi/v1/** 开放接口鉴权
-- 执行：在 MySQL 中执行本脚本即可
-- ================================================================

-- 1. 开放接口应用表
DROP TABLE IF EXISTS openapi_app;
CREATE TABLE openapi_app (
  app_id          BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '应用ID',
  app_name        VARCHAR(100)  NOT NULL                 COMMENT '应用名称（对接方）',
  app_key         VARCHAR(128)  NOT NULL                 COMMENT 'API Key',
  allowed_modules VARCHAR(500) DEFAULT '*'              COMMENT '允许访问的模块，逗号分隔如 wms,dms；* 表示全部',
  status          CHAR(1)       DEFAULT '0'             COMMENT '状态（0正常 1停用）',
  del_flag        CHAR(1)       DEFAULT '0'             COMMENT '删除标志（0存在 1删除）',
  create_by       VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  create_time     DATETIME                              COMMENT '创建时间',
  update_by       VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  update_time     DATETIME                              COMMENT '更新时间',
  remark          VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (app_id),
  UNIQUE KEY uk_app_key (app_key)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='开放接口应用表';

-- 2. 插入一个默认测试应用
INSERT INTO openapi_app (app_name, app_key, allowed_modules, status, create_by, create_time, remark)
VALUES ('默认测试应用', 'test-api-key-2026', '*', '0', 'admin', sysdate(), '默认测试Key，生产环境请替换');

-- 3. 菜单：在"系统管理"下新增"开放接口管理" (menu_id=119)
INSERT INTO sys_menu VALUES
('119', '开放接口', '1', '9', 'openapi', 'system/openapi/index', '', '', 1, 0, 'C', '0', '0', 'system:openapi:list', 'international', 'admin', sysdate(), '', NULL, '开放接口应用Key管理');

-- 3.1 按钮权限
INSERT INTO sys_menu VALUES
('1200', '应用查询', '119', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:openapi:query',  '#', 'admin', sysdate(), '', NULL, ''),
('1201', '应用新增', '119', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:openapi:add',    '#', 'admin', sysdate(), '', NULL, ''),
('1202', '应用修改', '119', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:openapi:edit',   '#', 'admin', sysdate(), '', NULL, ''),
('1203', '应用删除', '119', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:openapi:remove', '#', 'admin', sysdate(), '', NULL, '');

-- 4. 为超管角色分配菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 119), (1, 1200), (1, 1201), (1, 1202), (1, 1203);

-- ================================================================
-- 回滚：
-- DELETE FROM sys_role_menu WHERE menu_id IN (119,1200,1201,1202,1203);
-- DELETE FROM sys_menu WHERE menu_id IN (119,1200,1201,1202,1203);
-- DROP TABLE IF EXISTS openapi_app;
-- ================================================================

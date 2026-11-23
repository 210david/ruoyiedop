-- ============================================================
-- AI 菜单直达：完整落地方案（P1 导航框架 + P2 文档-菜单绑定）
-- 适用：RuoYi-Vue3 / MySQL 8
-- 幂等：可重复执行
-- ============================================================

-- ------------------------------------------------------------
-- 1. AI导航意图表（问题 → 系统菜单）
--    核心设计：只存 menu_id，不存 path
--    路径由服务端按登录人权限菜单树实时计算，菜单改名/改路径/改权限均无需维护本表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_nav_intent (
  intent_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  intent_key   VARCHAR(64)  NOT NULL                COMMENT '意图标识（LLM选择用，唯一）',
  menu_id      BIGINT       NOT NULL                COMMENT '目标菜单ID（sys_menu.menu_id），路径由服务端计算',
  module_code  VARCHAR(32)  NOT NULL                COMMENT '模块编码（wms/dms/qms/pms/mms/mk/hr/safety/bid/fin/flow/ai），两级路由用',
  title        VARCHAR(100) NOT NULL                COMMENT '展示名称，如 入库单管理',
  aliases      VARCHAR(500) DEFAULT ''              COMMENT '别名/同义词，英文逗号分隔（规则命中用，需按客户口语补）',
  examples     VARCHAR(500) DEFAULT ''              COMMENT '示例问句，英文逗号分隔（给LLM做语义参考）',
  nav_query    VARCHAR(300) DEFAULT ''              COMMENT '跳转附加参数JSON，如 {"status":"0"}；页面未适配时为null',
  doc_id       BIGINT       DEFAULT NULL            COMMENT '绑定的知识文档ID（P2 文档直连，优先于语义路由）',
  sort_no      INT          DEFAULT 0               COMMENT '排序权重，越大越优先',
  status       CHAR(1)      DEFAULT '0'             COMMENT '状态（0正常 1停用）',
  create_by    VARCHAR(64)  DEFAULT '',
  create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_by    VARCHAR(64)  DEFAULT '',
  update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (intent_id),
  UNIQUE KEY uk_intent_key (intent_key),
  KEY idx_module (module_code),
  KEY idx_menu (menu_id),
  KEY idx_doc (doc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI导航意图表：用户问题 → 系统功能菜单';

-- ------------------------------------------------------------
-- 2. 知识文档关联功能入口（P2）
--    上传手册时选好归属页面，问答命中该文档即可确定性给出入口，不依赖模型猜测
-- ------------------------------------------------------------
-- MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，用存储过程保证幂等
DROP PROCEDURE IF EXISTS add_nav_menu_id_to_knowledge_doc;
DELIMITER $$
CREATE PROCEDURE add_nav_menu_id_to_knowledge_doc()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_knowledge_doc'
      AND COLUMN_NAME = 'nav_menu_id'
  ) THEN
    ALTER TABLE ai_knowledge_doc
      ADD COLUMN nav_menu_id BIGINT DEFAULT NULL
      COMMENT '关联功能入口菜单ID（sys_menu.menu_id），问答命中该文档时直接给出入口';
  END IF;
END$$
DELIMITER ;
CALL add_nav_menu_id_to_knowledge_doc();
DROP PROCEDURE IF EXISTS add_nav_menu_id_to_knowledge_doc;

-- ------------------------------------------------------------
-- 3. 配置开关（沿用 sys_config 体系）
-- ------------------------------------------------------------
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'AI导航开关', 'ai.nav.enabled', 'true', 'Y', 'admin', NOW(),
       'AI问答后是否在答案下方显示可点击的系统功能入口'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'ai.nav.enabled');

-- ------------------------------------------------------------
-- 4. 种子意图数据（使用真实 menu_id，已核对路径）
--    路径对照（由 sys_menu.parent_id/path 逐级拼接验证）：
--      2060 入库单管理 → /wms/inbound/order
--      2080 库存查询   → /wms/inv/list
--      2090 盘点任务   → /wms/stocktake/task
--      2010 物料管理   → /wms/base/material
--      905887 招标公告库 → /bid/notice
--      905825 流程发起   → /flow/launch
--      905940 往来单位管理 → /fin/base/info
--      905802 智能查询   → /ai/nlquery
-- ------------------------------------------------------------
INSERT INTO ai_nav_intent
(intent_key, menu_id, module_code, title, aliases, examples, sort_no, status) VALUES
('wms_inbound',   2060,   'wms',  '入库单管理',
 '入库,入库单,收货,收货单,到货登记,收料,验收入库',
 '入库单在哪里录入,怎么收货入库,入库流程怎么走,收料在哪登记', 20, '0'),
('wms_inventory', 2080,   'wms',  '库存查询',
 '库存,存货,库存查询,还有多少料,结存,现存量,库存量',
 '查库存,某个物料还有多少库存,库存还有多少', 20, '0'),
('wms_stocktake', 2090,   'wms',  '盘点任务',
 '盘点,盘库,清点,盘点单',
 '怎么发起盘点,盘点任务在哪', 15, '0'),
('wms_material',  2010,   'wms',  '物料管理',
 '物料,物料档案,物料新增,物料编码',
 '怎么新增物料,物料档案在哪维护', 15, '0'),
('bid_notice',    905887, 'bid',  '招标公告库',
 '招标,招标公告,招标信息,投标机会',
 '招标公告在哪看,哪里能看到招标信息', 15, '0'),
('flow_launch',   905825, 'flow', '流程发起',
 '发起流程,提流程,提审批,申请审批,审批流程,提申请,走流程',
 '怎么发起请假流程,审批怎么提交,我要提单,请假在哪申请', 20, '0'),
('fin_partner',   905940, 'fin',  '往来单位管理',
 '往来单位,客户档案,供应商档案,往来档案',
 '往来单位在哪维护,客户档案怎么建', 12, '0'),
('ai_nlquery',    905802, 'ai',   '智能查询',
 '智能查询,自然语言查询,问数据,AI查数',
 '怎么用AI查数据,智能查询在哪', 10, '0')
ON DUPLICATE KEY UPDATE
  menu_id = VALUES(menu_id),
  module_code = VALUES(module_code),
  title = VALUES(title),
  aliases = VALUES(aliases),
  examples = VALUES(examples),
  sort_no = VALUES(sort_no),
  status = VALUES(status);

-- ------------------------------------------------------------
-- 5. 菜单与权限（P2 管理界面用；不建则为纯接口维护）
--    AI中心下增加「菜单直达配置」菜单
-- ------------------------------------------------------------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '菜单直达配置', 905788, 8, 'nav', 'ai/nav/index', 1, 0,
       'C', '0', '0', 'ai:nav:list', 'guide', 'admin', NOW(),
       '维护「问题→系统菜单」意图：别名、示例问句、关联知识文档'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'nav' AND parent_id = 905788);

-- 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '菜单直达查询', m.menu_id, 1, '', NULL, 1, 0, 'F', '0', '0', 'ai:nav:query', '#', 'admin', NOW(), ''
FROM sys_menu m
WHERE m.path = 'nav' AND m.parent_id = 905788
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'ai:nav:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '菜单直达新增', m.menu_id, 2, '', NULL, 1, 0, 'F', '0', '0', 'ai:nav:add', '#', 'admin', NOW(), ''
FROM sys_menu m
WHERE m.path = 'nav' AND m.parent_id = 905788
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'ai:nav:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '菜单直达修改', m.menu_id, 3, '', NULL, 1, 0, 'F', '0', '0', 'ai:nav:edit', '#', 'admin', NOW(), ''
FROM sys_menu m
WHERE m.path = 'nav' AND m.parent_id = 905788
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'ai:nav:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '菜单直达删除', m.menu_id, 4, '', NULL, 1, 0, 'F', '0', '0', 'ai:nav:remove', '#', 'admin', NOW(), ''
FROM sys_menu m
WHERE m.path = 'nav' AND m.parent_id = 905788
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'ai:nav:remove');

-- 知识文档「关联功能入口」修改权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '关联功能入口', 905794, 5, '', NULL, 1, 0, 'F', '0', '0', 'ai:knowledge:nav', '#', 'admin', NOW(),
       '为知识文档设置关联的系统功能入口'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'ai:knowledge:nav');

-- 管理员角色直接授予新菜单权限（其他角色请到「角色管理」按需勾选）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, x.menu_id FROM sys_menu x
WHERE x.perms IN ('ai:nav:query','ai:nav:add','ai:nav:edit','ai:nav:remove','ai:knowledge:nav')
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = x.menu_id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, x.menu_id FROM sys_menu x
WHERE x.path = 'nav' AND x.parent_id = 905788
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = x.menu_id);

-- ------------------------------------------------------------
-- 6. 问答历史增加导航入口字段（历史回放可点击）
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_nav_actions_to_qa_history;
DELIMITER $$
CREATE PROCEDURE add_nav_actions_to_qa_history()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_qa_history'
      AND COLUMN_NAME = 'nav_actions_json'
  ) THEN
    ALTER TABLE ai_qa_history
      ADD COLUMN nav_actions_json VARCHAR(2000) DEFAULT ''
      COMMENT 'AI导航入口JSON（历史回放可点击）';
  END IF;
END$$
DELIMITER ;
CALL add_nav_actions_to_qa_history();
DROP PROCEDURE IF EXISTS add_nav_actions_to_qa_history;

-- ------------------------------------------------------------
-- 7. 数据查询历史增加导航入口字段
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_nav_actions_to_nlq_history;
DELIMITER $$
CREATE PROCEDURE add_nav_actions_to_nlq_history()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_nlq_history'
      AND COLUMN_NAME = 'nav_actions_json'
  ) THEN
    ALTER TABLE ai_nlq_history
      ADD COLUMN nav_actions_json VARCHAR(2000) DEFAULT ''
      COMMENT 'AI导航入口JSON（历史回放可点击）';
  END IF;
END$$
DELIMITER ;
CALL add_nav_actions_to_nlq_history();
DROP PROCEDURE IF EXISTS add_nav_actions_to_nlq_history;

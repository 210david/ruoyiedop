-- =====================================================================
-- 首页「常用功能」+「岗位新人清单」基础表与初始化数据
--
-- 设计要点：
-- 1. 常用功能：按用户访问频次自动沉淀（source='A'），同时允许用户手动固定
--    （source='M'，不受频次淘汰影响）；两者合并后取前 N 个展示。
-- 2. 新人清单：模板按「岗位角色」配置（role_id），判定方式先用 MENU_VISITED
--    （该菜单被访问过即视为完成），零业务侵入；后续可扩展业务表 count 判定。
-- 3. 表字符集显式声明 COLLATE=utf8mb4_general_ci（开发规范 3.1）。
-- 4. 本脚本幂等：可重复执行，不会产生重复模板（先按 role 清理再插入）。
--
-- 执行后需重启后端（Mapper 注解 SQL 无需额外配置，但实体类需重新编译）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 用户常用功能表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user_shortcut (
  id              bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id         bigint(20)   NOT NULL                COMMENT '用户ID',
  menu_path       varchar(200) NOT NULL                COMMENT '菜单路由（唯一标识）',
  menu_title      varchar(100) DEFAULT NULL            COMMENT '菜单名称',
  menu_icon       varchar(100) DEFAULT NULL            COMMENT '菜单图标',
  module_code     varchar(50)  DEFAULT NULL            COMMENT '所属模块编码',
  source          char(1)      NOT NULL DEFAULT 'A'    COMMENT '来源（A=按频次自动 M=用户手动固定）',
  visit_count     int(11)      NOT NULL DEFAULT 0      COMMENT '访问次数',
  last_visit_time datetime     DEFAULT NULL            COMMENT '最近访问时间',
  create_time     datetime     DEFAULT NULL            COMMENT '创建时间',
  update_time     datetime     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_path (user_id, menu_path),
  KEY idx_user_source (user_id, source, visit_count)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户常用功能（自动频次+手动固定）';

-- ---------------------------------------------------------------------
-- 2. 岗位新人任务模板表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_role_onboard_task (
  id           bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
  role_id      bigint(20)   NOT NULL                COMMENT '角色ID（岗位）',
  seq          int(11)      NOT NULL DEFAULT 0      COMMENT '步骤序号',
  title        varchar(200) NOT NULL                COMMENT '任务标题（业务动作，不是功能名）',
  description  varchar(500) DEFAULT NULL            COMMENT '完成标准说明',
  menu_path    varchar(200) DEFAULT NULL            COMMENT '任务落点菜单',
  check_type   varchar(30)  NOT NULL DEFAULT 'MENU_VISITED' COMMENT '判定方式（MENU_VISITED=访问过该菜单 MANUAL=手工确认）',
  check_param  varchar(200) DEFAULT NULL            COMMENT '判定参数（MENU_VISITED 为空时取 menu_path）',
  enabled      char(1)      NOT NULL DEFAULT '0'    COMMENT '是否启用（0启用 1停用）',
  create_time  datetime     DEFAULT NULL            COMMENT '创建时间',
  update_time  datetime     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_role_seq (role_id, seq)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='岗位新人任务模板';

-- ---------------------------------------------------------------------
-- 3. 用户新人任务进度表（仅 MANUAL 判定需要；MENU_VISITED 实时算不落库）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user_onboard_progress (
  id        bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id   bigint(20) NOT NULL                COMMENT '用户ID',
  task_id   bigint(20) NOT NULL                COMMENT '任务ID',
  done_time datetime   NOT NULL                COMMENT '完成时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_task (user_id, task_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户新人任务进度';

-- ---------------------------------------------------------------------
-- 3.1 用户新手引导开关（用户级；系统级开关走 sys_config）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user_onboard_setting (
  user_id     bigint(20) NOT NULL              COMMENT '用户ID',
  enabled     char(1)    NOT NULL DEFAULT '0'  COMMENT '用户开关（0开启 1关闭）',
  update_time datetime   DEFAULT NULL          COMMENT '更新时间',
  PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户新手引导开关';

-- ---------------------------------------------------------------------
-- 3.2 系统级开关（管理员在「系统管理 → 参数设置」统一关闭）
-- ---------------------------------------------------------------------
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '新手上路-全局开关', 'sys.onboard.enabled', 'true', 'Y', 'admin', NOW(),
       'true=全员显示；false=全平台关闭（管理员统一关闭后，用户个人开关不生效）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'sys.onboard.enabled');

-- =====================================================================
-- 4. 初始化岗位任务模板
--    说明：按 role_key 匹配，不硬编码 role_id，跨环境可复现。
--    如需给新岗位配置，复制对应段落改 role_key 即可。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 4.1 采购岗（采购经理 pms_manager / 采购员 pms_buyer）
--     主线：把一批货买回来并完成验收
-- ---------------------------------------------------------------------
DELETE t FROM sys_role_onboard_task t
  JOIN sys_role r ON r.role_id = t.role_id
 WHERE r.role_key IN ('pms_manager','pms_buyer');

INSERT INTO sys_role_onboard_task (role_id, seq, title, description, menu_path, check_type, check_param, enabled, create_time)
SELECT r.role_id, t.seq, t.title, t.description, t.menu_path, 'MENU_VISITED', NULL, '0', NOW()
  FROM sys_role r
  JOIN (
        SELECT 1 AS seq, '建立第一个供应商' AS title, '没有供应商，后续所有采购单据都开不出来' AS description, '/pms/supplier/info' AS menu_path
  UNION ALL SELECT 2, '维护供应商的供货清单', '把能供的物料挂到供应商下，采购时才能按供应商选料', '/pms/supplier/catalog'
  UNION ALL SELECT 3, '发起第一张采购申请', '从需求出发，走一次申请→审批的完整流程', '/pms/demand/request'
  UNION ALL SELECT 4, '生成并下达采购订单', '申请审批通过后转订单，把需求变成对供应商的承诺', '/pms/contract/order'
  UNION ALL SELECT 5, '完成一次收货验收', '货到验收，单据闭环，采购动作才算真正完成', '/pms/receive/receive'
       ) t
 WHERE r.role_key IN ('pms_manager','pms_buyer') AND r.del_flag = '0';

-- ---------------------------------------------------------------------
-- 4.2 仓储岗（仓库主管 wms_manager）
--     主线：货收得进来、放得对、查得到
-- ---------------------------------------------------------------------
DELETE t FROM sys_role_onboard_task t
  JOIN sys_role r ON r.role_id = t.role_id
 WHERE r.role_key IN ('wms_manager');

INSERT INTO sys_role_onboard_task (role_id, seq, title, description, menu_path, check_type, check_param, enabled, create_time)
SELECT r.role_id, t.seq, t.title, t.description, t.menu_path, 'MENU_VISITED', NULL, '0', NOW()
  FROM sys_role r
  JOIN (
        SELECT 1 AS seq, '看清仓库与货位结构' AS title, '先弄明白货该放哪，后面上架才不会乱' AS description, '/wms/base/warehouse' AS menu_path
  UNION ALL SELECT 2, '确认物料档案可用', '物料是库存的最小管理单位，没有档案就收不了货', '/wms/base/material'
  UNION ALL SELECT 3, '完成一次入库收货', '把一批货真正收进来，走完到货→验收→入库', '/wms/inbound/order'
  UNION ALL SELECT 4, '查一次库存明细', '确认收进来的货在系统里查得到、数量对得上', '/wms/inv/list'
  UNION ALL SELECT 5, '设置常用物料的安全库存', '低于安全库存自动预警，这是仓库主管最常用的一道防线', '/wms/inv/alert'
       ) t
 WHERE r.role_key IN ('wms_manager') AND r.del_flag = '0';

-- ---------------------------------------------------------------------
-- 4.3 销售岗（营销总监 mk_director / 销售经理 mk_sales_manager / 销售代表 mk_sales）
--     主线：从一条线索到一笔回款
-- ---------------------------------------------------------------------
DELETE t FROM sys_role_onboard_task t
  JOIN sys_role r ON r.role_id = t.role_id
 WHERE r.role_key IN ('mk_director','mk_sales_manager','mk_sales');

INSERT INTO sys_role_onboard_task (role_id, seq, title, description, menu_path, check_type, check_param, enabled, create_time)
SELECT r.role_id, t.seq, t.title, t.description, t.menu_path, 'MENU_VISITED', NULL, '0', NOW()
  FROM sys_role r
  JOIN (
        SELECT 1 AS seq, '录入第一条线索' AS title, '销售动作的起点，先学会把线索存进系统' AS description, '/mk/lead/list' AS menu_path
  UNION ALL SELECT 2, '把线索转成商机', '判断有没有戏，进商机池才算进入跟进流程', '/mk/opportunity/list'
  UNION ALL SELECT 3, '建立客户档案', '成交前后都要靠客户档案沉淀往来记录', '/mk/customer/list'
  UNION ALL SELECT 4, '签一份合同并下订单', '把商机变成收入承诺，走完合同→订单', '/mk/contract/contract'
  UNION ALL SELECT 5, '跟踪一次回款', '钱到账才是真正的闭环，回款状态决定提成', '/mk/order-execution/payment'
       ) t
 WHERE r.role_key IN ('mk_director','mk_sales_manager','mk_sales') AND r.del_flag = '0';

-- ---------------------------------------------------------------------
-- 4.4 经营岗（总经理 general_manager / 系统管理员 admin 不配）
--     主线：看清经营、盯住异常
-- ---------------------------------------------------------------------
DELETE t FROM sys_role_onboard_task t
  JOIN sys_role r ON r.role_id = t.role_id
 WHERE r.role_key IN ('general_manager');

INSERT INTO sys_role_onboard_task (role_id, seq, title, description, menu_path, check_type, check_param, enabled, create_time)
SELECT r.role_id, t.seq, t.title, t.description, t.menu_path, 'MENU_VISITED', NULL, '0', NOW()
  FROM sys_role r
  JOIN (
        SELECT 1 AS seq, '看一遍决策驾驶舱' AS title, '先知道自己每天要看哪几个数字' AS description, '/bi/dashboard' AS menu_path
  UNION ALL SELECT 2, '了解指标口径' AS title, '口径不清，看的数字就没法用来拍板', '/bi/metric'
  UNION ALL SELECT 3, '看一张业务主题看板', '销售/采购/库存/质量，挑最关心的一条业务线深入看一次', '/bi/board/board-sales'
  UNION ALL SELECT 4, '打开业务全景图' AS title, '搞清各模块之间数据怎么流，才知道异常该找谁', '/business-map'
       ) t
 WHERE r.role_key IN ('general_manager') AND r.del_flag = '0';

-- ---------------------------------------------------------------------
-- 4.5 通用岗（普通员工 common_user）— 最小可用三件套
-- ---------------------------------------------------------------------
DELETE t FROM sys_role_onboard_task t
  JOIN sys_role r ON r.role_id = t.role_id
 WHERE r.role_key IN ('common_user');

INSERT INTO sys_role_onboard_task (role_id, seq, title, description, menu_path, check_type, check_param, enabled, create_time)
SELECT r.role_id, t.seq, t.title, t.description, t.menu_path, 'MENU_VISITED', NULL, '0', NOW()
  FROM sys_role r
  JOIN (
        SELECT 1 AS seq, '先看一遍业务全景图' AS title, '3 分钟搞清系统里有哪些业务、数据怎么流' AS description, '/business-map' AS menu_path
       ) t
 WHERE r.role_key IN ('common_user') AND r.del_flag = '0';

-- =====================================================================
-- 5. 校验查询
-- =====================================================================
-- SELECT r.role_name, r.role_key, COUNT(t.id) AS task_cnt
--   FROM sys_role r LEFT JOIN sys_role_onboard_task t ON t.role_id = r.role_id
--  WHERE r.del_flag = '0' AND r.role_key IN ('pms_manager','pms_buyer','wms_manager','mk_sales','general_manager','common_user')
--  GROUP BY r.role_id;

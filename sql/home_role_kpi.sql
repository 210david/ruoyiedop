-- ---------------------------------------------------------------------------
-- 首页岗位化 KPI 配置（第二阶段 P0：KPI 按岗位取 4 张卡）
--
-- 设计取舍：
-- 1. 指标编码（metric_key）在后端注册，配置表只存"哪个岗位看哪几个指标"，
--    避免把取数 SQL 写进数据库（不可版本管理、易被误改）；
-- 2. 岗位用 role_key 关联 sys_role，和新人清单（sys_role_onboard_task）保持同一套岗位口径；
-- 3. 未配置岗位的角色（如 admin、普通员工）不返回 roleKpis，前端自动回退到原"按模块"卡片，
--    保证灰度上线期间老用户界面不变。
--
-- 执行：mysql -uroot -p ry-vue < sql/home_role_kpi.sql （幂等，可重复执行）
-- ---------------------------------------------------------------------------

-- 1. 岗位 KPI 配置表
CREATE TABLE IF NOT EXISTS sys_role_kpi (
  id            bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
  role_key      varchar(64)  NOT NULL                COMMENT '岗位角色标识（sys_role.role_key）',
  seq           int(4)       NOT NULL DEFAULT '1'    COMMENT '展示顺序',
  metric_key    varchar(64)  NOT NULL                COMMENT '指标编码（后端指标注册表）',
  title         varchar(50)  DEFAULT NULL            COMMENT '卡片标题（空则用注册表默认标题）',
  unit          varchar(20)  DEFAULT NULL            COMMENT '单位（空则用注册表默认单位）',
  target_titles varchar(200) DEFAULT NULL            COMMENT '点击跳转的菜单标题（逗号分隔）',
  target_path   varchar(200) DEFAULT NULL            COMMENT '兜底路由路径',
  enabled       char(1)      NOT NULL DEFAULT '0'    COMMENT '状态（0启用 1停用）',
  remark        varchar(255) DEFAULT NULL            COMMENT '备注',
  create_time   datetime     DEFAULT NULL            COMMENT '创建时间',
  update_time   datetime     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_metric (role_key, metric_key)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='首页岗位 KPI 配置';

-- 2. 补齐两个缺失岗位（综合办公室负责人 / 人力资源负责人）
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT '综合办公室负责人', 'office_manager', 7, '1', 1, 1, '0', '0', 'admin', NOW(), '行政后勤、车辆、印章、安全事务负责人（首页岗位 KPI 已配置）'
  FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'office_manager');

INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT '人力资源负责人', 'hr_manager', 8, '1', 1, 1, '0', '0', 'admin', NOW(), '人事、考勤、合同、薪酬负责人（首页岗位 KPI 已配置）'
  FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'hr_manager');

-- 3. 【已下线 2026-11-18】岗位差异化 KPI 暂不使用
--
--    首页顶部统一展示 __default__ 的五张固定卡（见 sql/home_kpi_default5.sql）。
--    后续将由「门户卡片自定义维护」在管理后台统一配置，届时另建配置模型；
--    这里的历史岗位配置仅作留存，不再执行写入，避免脚本重跑把已下线功能复活。
--    如需恢复岗位化，把下面注释块还原并执行即可。
-- -- 3. 四个岗位的 KPI 配置（每岗位 4 张卡，顺序即展示顺序）
-- INSERT INTO sys_role_kpi (role_key, seq, metric_key, target_titles, target_path, enabled, remark, create_time, update_time)
-- VALUES
--   -- 总经理：看经营结果 + 回款风险 + 全局异常 + 待批
--   ('general_manager', 1, 'FIN_AR_MONTH',            '应收管理',        '/fin/daily/ar',                '0', '本月开票收入', NOW(), NOW()),
--   ('general_manager', 2, 'FIN_AR_OVERDUE',          '应收管理',        '/fin/daily/ar',                '0', '逾期未收金额', NOW(), NOW()),
--   ('general_manager', 3, 'ALERT_TOTAL',             '库存预警',        '/wms/inv/alert',         '0', '全平台预警总数', NOW(), NOW()),
--   ('general_manager', 4, 'PMS_APPROVAL_PENDING',    '采购申请',        '/pms/request',           '0', '采购待审批单据', NOW(), NOW()),

--   -- 综合办公室负责人：行政资产 + 到期风险 + 安全兜底
--   ('office_manager', 1, 'OA_VEHICLE_DUE',           '车辆管理',        '/oa/vehicle',            '0', '车辆年检/保险 30 天内到期', NOW(), NOW()),
--   ('office_manager', 2, 'OA_VEHICLE_TOTAL',         '车辆管理',        '/oa/vehicle',            '0', '公务车辆总数', NOW(), NOW()),
--   ('office_manager', 3, 'OA_SEAL_TOTAL',            '印章管理',        '/oa/seal',               '0', '启用中的印章数', NOW(), NOW()),
--   ('office_manager', 4, 'SAFETY_HAZARD_OPEN',       '隐患管理,隐患排查', '/safety/dualcontrol/hazard', '0', '未闭环安全隐患', NOW(), NOW()),

--   -- 人力资源负责人：人头 + 转正 + 合同风险 + 待批
--   ('hr_manager', 1, 'HR_HEADCOUNT',                 '员工档案',        '/hr/org/employee',           '0', '在册人数（在职+试用）', NOW(), NOW()),
--   ('hr_manager', 2, 'HR_PROBATION',                 '员工档案',        '/hr/org/employee',           '0', '试用期人数', NOW(), NOW()),
--   ('hr_manager', 3, 'HR_CONTRACT_EXPIRE',           '劳动合同',        '/hr/contract-mgmt/contract',           '0', '30 天内到期合同', NOW(), NOW()),
--   ('hr_manager', 4, 'HR_LEAVE_PENDING',             '请假管理',        '/hr/attendance/leaveRequest',       '0', '待审批请假单', NOW(), NOW()),

--   -- 财务负责人：回款 + 付款 + 凭证 + 关账
--   ('fin_manager', 1, 'FIN_AR_OVERDUE',              '应收管理',        '/fin/daily/ar',                '0', '逾期未收金额', NOW(), NOW()),
--   ('fin_manager', 2, 'FIN_AP_UNPAID',               '应付管理',        '/fin/daily/ap',                '0', '应付未付金额', NOW(), NOW()),
--   ('fin_manager', 3, 'FIN_VOUCHER_DRAFT',           '总账凭证',        '/fin/daily/gl',                '0', '待审核凭证', NOW(), NOW()),
--   ('fin_manager', 4, 'FIN_PERIOD_OPEN',             '期末结账',        '/fin/daily/closing',           '0', '未结账会计期间', NOW(), NOW())
-- ON DUPLICATE KEY UPDATE
--   seq           = VALUES(seq),
--   target_titles = VALUES(target_titles),
--   target_path   = VALUES(target_path),
--   enabled       = VALUES(enabled),
--   remark        = VALUES(remark),
--   update_time   = NOW();

-- ---------------------------------------------------------------------------
-- 4. 四个岗位的新手清单（与 KPI 同一套岗位口径：按 role_key 关联，避免硬编码 role_id）
--
-- 判定方式沿用 MENU_VISITED（菜单被访问过即完成），零业务侵入；
-- 任务全部是"走通一次业务主线"的动作，不是"了解某功能"，且路径均为已核对的真实路由。
-- 幂等：先按岗位清理再插入。
-- ---------------------------------------------------------------------------
DELETE t FROM sys_role_onboard_task t
  JOIN sys_role r ON r.role_id = t.role_id
 WHERE r.role_key IN ('office_manager', 'hr_manager', 'fin_manager');

INSERT INTO sys_role_onboard_task (role_id, seq, title, description, menu_path, check_type, enabled, create_time)
SELECT r.role_id, v.seq, v.title, v.description, v.menu_path, 'MENU_VISITED', '0', NOW()
FROM sys_role r
JOIN (
  -- 财务负责人：看回款 → 记应付 → 审凭证 → 关账 → 看账龄
  SELECT 'fin_manager' AS role_key, 1 AS seq, '核对一遍应收台账' AS title, '看清本月开票与逾期明细，知道钱在哪' AS description, '/fin/daily/ar' AS menu_path
  UNION ALL SELECT 'fin_manager', 2, '登记第一张应付单', '把供应商发票挂上应付，形成付款依据', '/fin/daily/ap'
  UNION ALL SELECT 'fin_manager', 3, '审核一张总账凭证', '走完"草稿 → 审核"才算懂凭证流', '/fin/daily/gl'
  UNION ALL SELECT 'fin_manager', 4, '完成一次期末结账', '关账后本月数据才定版，报表才可信', '/fin/daily/closing'
  UNION ALL SELECT 'fin_manager', 5, '看一次账龄分析', '按账龄分布判断回款风险', '/fin/report/aging'

  -- 人力资源负责人：档案 → 入职 → 请假 → 转正 → 合同
  UNION ALL SELECT 'hr_manager', 1, '完善一名员工档案', '档案是全模块主数据，缺了后面流程都开不出来', '/hr/org/employee'
  UNION ALL SELECT 'hr_manager', 2, '办理一次入职', '从入职单到生成员工档案，走通一次', '/hr/lifecycle/entry'
  UNION ALL SELECT 'hr_manager', 3, '审批一张请假单', '审批流跑通才算会用考勤', '/hr/attendance/leaveRequest'
  UNION ALL SELECT 'hr_manager', 4, '处理一次转正', '试用期到转正的完整链路', '/hr/lifecycle/regular'
  UNION ALL SELECT 'hr_manager', 5, '核对一次合同到期清单', '30 天内到期合同要提前续签', '/hr/contract-mgmt/contract'

  -- 综合办公室负责人：车辆 → 印章 → 隐患 → 全景
  UNION ALL SELECT 'office_manager', 1, '核对公务车辆台账', '车辆、年检、保险三件事在一张表里', '/oa/vehicle'
  UNION ALL SELECT 'office_manager', 2, '维护印章台账', '用印责任到人，先确认台账准确', '/oa/seal'
  UNION ALL SELECT 'office_manager', 3, '跟进一条未闭环隐患', '综合办通常兼管安全事务，闭环一条试试', '/safety/dualcontrol/hazard'
  UNION ALL SELECT 'office_manager', 4, '打开业务全景图', '知道自己这条线在整条链路上的位置', '/business-map'
) v ON v.role_key = r.role_key;

-- =============================================================
-- 生产库升级总入口（2026-11-13 合并版）
-- 执行顺序：本文件已按序合并以下两个脚本，直接整文件执行即可
--   A. upgrade_20261113_production_fix.sql（PJM 编号规则 + 证书字段修复，幂等）
--   B. cleanup_seed_employee.sql（员工种子数据清理：核对→备份→删除→工号复位）
-- 执行：mysql -uroot -p --default-character-set=utf8mb4 <库名> < 本文件
-- 注意：B 段第 0 步为只读核对 SELECT，执行前请先查看其输出确认无业务引用
--       （0.3 节任何 ref_cnt>0 时，不要继续执行 B 段第 2 步物理删除）
-- 幂等：A 段可重复执行；B 段删除语句按 create_by+remark 精确匹配，重复执行空删除
-- =============================================================


-- ############ A 段：生产修复（幂等） ############

-- =============================================================
-- 生产库增量修复 SQL（合并幂等版）
-- 日期：2026-11-13
-- 现象：远程部署最新版后，项目管理 - 新增项目（立项）保存提示
--       "系统繁忙，请稍后重试"（GlobalExceptionHandler 兜底）。
-- 根因：生产库缺少 PJM 编号规则配置（mk_number_rule 无 PJM_PROJECT 行），
--       后端 PjmProjectServiceImpl.insertProject →
--       numberRuleService.generateNumber("PJM_PROJECT") 抛 ServiceException，
--       经 handleRuntimeException 拦截后前端显示"系统繁忙"。
--       同时补齐 20261112 证书升级（最新部署的 hr 证书模块依赖）。
-- 内容：
--   1. sql/pjm_number_rules.sql         —— PJM 4 条编号规则（直接修复报错）
--   2. sql/upgrade_20261112_certificate.sql —— biz_hr_certificate.employee_id 改可空
-- 幂等：可重复执行，无副作用。
-- 执行：mysql -uroot -p --default-character-set=utf8mb4 ruoyi_lyjs < 本文件
-- =============================================================

-- ============================================================
-- PJM 项目管理模块编号规则初始化（幂等：已存在则跳过，可重复执行）
-- 规则编码与生成器调用约定一致：
--   PjmProjectServiceImpl      -> PJM_PROJECT
--   PjmChangeServiceImpl       -> PJM_CHANGE
--   PjmPaymentServiceImpl      -> PJM_PAYMENT
--   PjmTaskWorklogServiceImpl  -> PJM_WORKLOG
-- 适用库：ruoyi_lyjs（本地开发库与客户库通用）
-- ============================================================

-- 回滚语句（如需移除本批规则时执行）：
-- DELETE FROM mk_number_rule WHERE rule_code IN ('PJM_PROJECT','PJM_CHANGE','PJM_PAYMENT','PJM_WORKLOG');

-- 项目编号（每日重置，如 XM202611070001）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'PJM_PROJECT', 'pjm', '项目编号', 'XM', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '项目编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='PJM_PROJECT');

-- 变更单编号（每日重置，如 BG202611070001）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'PJM_CHANGE', 'pjm', '变更单编号', 'BG', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '变更单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='PJM_CHANGE');

-- 付款单编号（每日重置，如 FK202611070001）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'PJM_PAYMENT', 'pjm', '付款单编号', 'FK', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '付款单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='PJM_PAYMENT');

-- 工作日志编号（每日重置，如 RZ202611070001）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'PJM_WORKLOG', 'pjm', '工作日志编号', 'RZ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '工作日志编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='PJM_WORKLOG');

-- =============================================================
-- 升级脚本: upgrade_20261112_certificate.sql
-- 主题: HR证书 employee_id 改可空（支持外部人员证书录入）
-- 说明: 方案 A —— employee_id 放开非空约束，配合服务层
--       「ID与姓名至少其一」校验；存量数据不受影响。
-- 幂等: MODIFY 重复执行无副作用
-- 目标库: ruoyi_lyjs
-- 执行: mysql -uroot -p --default-character-set=utf8mb4 ruoyi_lyjs < 本文件
-- =============================================================

-- 44. employee_id 改可空（外部人员证书无 employee_id）
ALTER TABLE biz_hr_certificate MODIFY COLUMN employee_id BIGINT NULL COMMENT '员工ID（外部人员证书为空，按 employee_name 录入）';
-- =============================================================
-- 升级脚本：项目管理 - 编号规则管理菜单（复用 mk 通用页面）
-- 日期：2026-11-13
-- 说明：pjm 模块此前缺少编号规则管理页（其他模块 fin/hr/mk/mms/pdm 均有），
--       导致 PJM 编号规则只能靠 SQL 初始化，生产库漏执行即引发
--       "新增项目保存提示系统繁忙"（generateNumber 抛 ServiceException）。
--       本脚本在项目管理（906202）下挂"编号规则"菜单，复用
--       views/mk/numberRule/index.vue 通用页面（query: module=pjm），
--       权限标识 pjm:numberRule:*（后端 MkNumberRuleController 已放行）。
-- 幂等：可重复执行。
-- =============================================================

-- 1. 菜单（C 类型，挂项目管理下）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '编号规则', 906202, 99, 'numberRule', 'mk/numberRule/index', '{"module":"pjm"}', 'PjmNumberRule', 1, 0, 'C', '0', '0', 'pjm:numberRule:list', 'number', 'admin', NOW(), '项目管理编号规则管理（复用 mk 通用页面，module=pjm）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:numberRule:list' AND menu_type = 'C');

SET @pjm_rule_menu := (SELECT menu_id FROM sys_menu WHERE perms = 'pjm:numberRule:list' AND menu_type = 'C' LIMIT 1);

-- 2. 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT t.menu_name, @pjm_rule_menu, t.ord, '', '', '', NULL, 1, 0, 'F', '0', '0', t.perms, '#', 'admin', NOW(), ''
FROM (
  SELECT '编号规则查询' AS menu_name, 1 AS ord, 'pjm:numberRule:query' AS perms UNION ALL
  SELECT '编号规则新增', 2, 'pjm:numberRule:add' UNION ALL
  SELECT '编号规则修改', 3, 'pjm:numberRule:edit' UNION ALL
  SELECT '编号规则删除', 4, 'pjm:numberRule:remove' UNION ALL
  SELECT '编号规则导出', 5, 'pjm:numberRule:export'
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.perms = t.perms AND m.menu_type = 'F');

-- 3. 授权给管理员角色（role_id=1 默认超管免权，此处授给普通管理角色可按需调整）
-- INSERT INTO sys_role_menu (role_id, menu_id)
-- SELECT r.role_id, m.menu_id FROM sys_role r, sys_menu m
-- WHERE m.perms LIKE 'pjm:numberRule%' AND NOT EXISTS
-- (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

-- =============================================================
-- 追加修复（2026-11-13 下午）：各模块编号规则菜单 query 参数格式修正
-- 现象：仓库/设备等模块的"编号规则"页面显示的是营销模块的规则列表。
-- 根因：前端 SidebarItem.vue 对菜单 query 做 JSON.parse，而历史脚本写入的
--       query='module=wms' 为裸字符串，解析失败导致 module 过滤失效，
--       页面回落到默认 module=mk（营销）。
-- 修复：统一改写为 JSON 格式 '{"module":"xxx"}'（幂等，可重复执行）。
-- =============================================================
UPDATE sys_menu SET query='{"module":"mk"}'  WHERE menu_name='编号规则' AND parent_id=4000 AND query <> '{"module":"mk"}';
UPDATE sys_menu SET query='{"module":"dms"}' WHERE menu_name='编号规则' AND parent_id=3000 AND query <> '{"module":"dms"}';
UPDATE sys_menu SET query='{"module":"wms"}' WHERE menu_name='编号规则' AND parent_id=2000 AND query <> '{"module":"wms"}';
-- 财务编码规则菜单（如存在且 query 为空，补 module=fin）
UPDATE sys_menu SET query='{"module":"fin"}' WHERE perms='fin:numberRule:list' AND menu_type='C' AND (query IS NULL OR query='');
-- 验证
SELECT menu_id, menu_name, parent_id, query FROM sys_menu
WHERE component='mk/numberRule/index' AND menu_type='C';

-- =============================================================
-- 追加修复（2026-11-13 晚）：特种人员保存报"系统繁忙"
-- 现象：安全生产 - 一企一档 - 特种人员，选择人员后保存提示系统繁忙。
-- 根因：生产库 biz_safety_worker 为旧版表结构，缺少 mapper 插入语句
--       引用的列（review_date / remind_status / enterprise_id 等），
--       INSERT 报 Unknown column → GlobalExceptionHandler 兜底。
--       upgrade_20261109_all.sql 未包含该表的建表/补列段。
-- 修复：按 safety_all.sql 的最新结构幂等补列（可重复执行）。
-- =============================================================

DROP PROCEDURE IF EXISTS p_add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE p_add_column_if_missing(IN p_table VARCHAR(64), IN p_col VARCHAR(64), IN p_def TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = p_col
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_def);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

-- biz_safety_worker 补列（列定义与 safety_all.sql 最新结构一致）
CALL p_add_column_if_missing('biz_safety_worker','worker_no',        '`worker_no` varchar(64) DEFAULT NULL COMMENT ''工号''');
CALL p_add_column_if_missing('biz_safety_worker','dept_id',          '`dept_id` bigint DEFAULT NULL COMMENT ''所属部门ID''');
CALL p_add_column_if_missing('biz_safety_worker','dept_name',        '`dept_name` varchar(128) DEFAULT NULL COMMENT ''所属部门名称''');
CALL p_add_column_if_missing('biz_safety_worker','position',         '`position` varchar(128) DEFAULT NULL COMMENT ''岗位''');
CALL p_add_column_if_missing('biz_safety_worker','phone',            '`phone` varchar(20) DEFAULT NULL COMMENT ''联系电话''');
CALL p_add_column_if_missing('biz_safety_worker','is_special',       '`is_special` char(1) DEFAULT ''0'' COMMENT ''是否特种作业人员''');
CALL p_add_column_if_missing('biz_safety_worker','job_type',         '`job_type` varchar(64) DEFAULT NULL COMMENT ''特种作业类别''');
CALL p_add_column_if_missing('biz_safety_worker','cert_no',          '`cert_no` varchar(64) DEFAULT NULL COMMENT ''特种作业证号''');
CALL p_add_column_if_missing('biz_safety_worker','cert_issue_date',  '`cert_issue_date` date DEFAULT NULL COMMENT ''证书发证日期''');
CALL p_add_column_if_missing('biz_safety_worker','valid_to',         '`valid_to` date DEFAULT NULL COMMENT ''证书有效期至''');
CALL p_add_column_if_missing('biz_safety_worker','review_date',      '`review_date` date DEFAULT NULL COMMENT ''换证/复审日期''');
CALL p_add_column_if_missing('biz_safety_worker','remind_status',    '`remind_status` char(1) DEFAULT ''0'' COMMENT ''提醒状态''');
CALL p_add_column_if_missing('biz_safety_worker','enterprise_id',    '`enterprise_id` bigint DEFAULT NULL COMMENT ''所属企业ID''');
CALL p_add_column_if_missing('biz_safety_worker','status',           '`status` char(1) DEFAULT ''0'' COMMENT ''状态''');
CALL p_add_column_if_missing('biz_safety_worker','del_flag',         '`del_flag` char(1) DEFAULT ''0'' COMMENT ''删除标志''');
CALL p_add_column_if_missing('biz_safety_worker','create_by',        '`create_by` varchar(64) DEFAULT '''' COMMENT ''创建者''');
CALL p_add_column_if_missing('biz_safety_worker','create_time',      '`create_time` datetime DEFAULT NULL COMMENT ''创建时间''');
CALL p_add_column_if_missing('biz_safety_worker','update_by',        '`update_by` varchar(64) DEFAULT '''' COMMENT ''更新者''');
CALL p_add_column_if_missing('biz_safety_worker','update_time',      '`update_time` datetime DEFAULT NULL COMMENT ''更新时间''');
CALL p_add_column_if_missing('biz_safety_worker','remark',           '`remark` varchar(500) DEFAULT NULL COMMENT ''备注''');

DROP PROCEDURE IF EXISTS p_add_column_if_missing;

-- 验证：应返回 22（biz_safety_worker 全部列数）
SELECT CONCAT('VERIFY biz_safety_worker columns = ', COUNT(*)) AS result
FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'biz_safety_worker';

-- =============================================================
-- 追加修复（2026-11-13 晚二）：其他人员选择保存同样报"系统繁忙"
-- 现象：安全生产 - 教育培训 - 参训人员等所有"选择人员后保存"的页面
--       均提示系统繁忙，与特种人员同一根因（生产库旧版表缺列，
--       upgrade_20261109_all.sql 未包含这些表的建表/补列段）。
-- 修复：对参训人员表、培训证书表按最新结构幂等补列。
-- =============================================================

DROP PROCEDURE IF EXISTS p_add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE p_add_column_if_missing(IN p_table VARCHAR(64), IN p_col VARCHAR(64), IN p_def TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = p_col
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_def);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

-- biz_safety_training_attendee（培训参训人员签到表）
CALL p_add_column_if_missing('biz_safety_training_attendee','record_id',      '`record_id` bigint NOT NULL COMMENT ''关联培训记录ID''');
CALL p_add_column_if_missing('biz_safety_training_attendee','user_id',        '`user_id` bigint DEFAULT NULL COMMENT ''参训人员ID（关联sys_user）''');
CALL p_add_column_if_missing('biz_safety_training_attendee','user_name',      '`user_name` varchar(128) DEFAULT NULL COMMENT ''参训人员姓名''');
CALL p_add_column_if_missing('biz_safety_training_attendee','dept_id',        '`dept_id` bigint DEFAULT NULL COMMENT ''所属部门ID''');
CALL p_add_column_if_missing('biz_safety_training_attendee','dept_name',      '`dept_name` varchar(128) DEFAULT NULL COMMENT ''所属部门名称''');
CALL p_add_column_if_missing('biz_safety_training_attendee','sign_in_status', '`sign_in_status` char(1) DEFAULT ''1'' COMMENT ''签到状态（1已签到 0未签到 2请假）''');
CALL p_add_column_if_missing('biz_safety_training_attendee','sign_in_time',   '`sign_in_time` datetime DEFAULT NULL COMMENT ''签到时间''');
CALL p_add_column_if_missing('biz_safety_training_attendee','sign_in_method', '`sign_in_method` varchar(20) DEFAULT NULL COMMENT ''签到方式（字典 safety_sign_in_method）''');
CALL p_add_column_if_missing('biz_safety_training_attendee','exam_score',     '`exam_score` decimal(5,2) DEFAULT NULL COMMENT ''考试分数''');
CALL p_add_column_if_missing('biz_safety_training_attendee','is_pass',        '`is_pass` char(1) DEFAULT ''0'' COMMENT ''是否合格（0不合格 1合格）''');
CALL p_add_column_if_missing('biz_safety_training_attendee','exam_time',      '`exam_time` datetime DEFAULT NULL COMMENT ''考试时间''');
CALL p_add_column_if_missing('biz_safety_training_attendee','enterprise_id',  '`enterprise_id` bigint DEFAULT NULL COMMENT ''所属企业ID''');
CALL p_add_column_if_missing('biz_safety_training_attendee','del_flag',       '`del_flag` char(1) DEFAULT ''0'' COMMENT ''删除标志''');
CALL p_add_column_if_missing('biz_safety_training_attendee','create_by',      '`create_by` varchar(64) DEFAULT '''' COMMENT ''创建者''');
CALL p_add_column_if_missing('biz_safety_training_attendee','create_time',    '`create_time` datetime DEFAULT NULL COMMENT ''创建时间''');
CALL p_add_column_if_missing('biz_safety_training_attendee','update_by',      '`update_by` varchar(64) DEFAULT '''' COMMENT ''更新者''');
CALL p_add_column_if_missing('biz_safety_training_attendee','update_time',    '`update_time` datetime DEFAULT NULL COMMENT ''更新时间''');
CALL p_add_column_if_missing('biz_safety_training_attendee','remark',         '`remark` varchar(500) DEFAULT NULL COMMENT ''备注''');

-- biz_safety_training_cert（培训证书表，证书管理保存依赖）
CALL p_add_column_if_missing('biz_safety_training_cert','cert_no',        '`cert_no` varchar(64) DEFAULT NULL COMMENT ''证书编号''');
CALL p_add_column_if_missing('biz_safety_training_cert','cert_name',      '`cert_name` varchar(128) DEFAULT NULL COMMENT ''证书名称''');
CALL p_add_column_if_missing('biz_safety_training_cert','cert_type',      '`cert_type` varchar(64) DEFAULT NULL COMMENT ''证书类型（字典 safety_cert_type）''');
CALL p_add_column_if_missing('biz_safety_training_cert','user_id',        '`user_id` bigint DEFAULT NULL COMMENT ''获证人员ID（外部人员为空）''');
CALL p_add_column_if_missing('biz_safety_training_cert','user_name',      '`user_name` varchar(128) DEFAULT NULL COMMENT ''获证人员姓名''');
CALL p_add_column_if_missing('biz_safety_training_cert','dept_id',        '`dept_id` bigint DEFAULT NULL COMMENT ''所属部门ID''');
CALL p_add_column_if_missing('biz_safety_training_cert','dept_name',      '`dept_name` varchar(128) DEFAULT NULL COMMENT ''所属部门名称''');
CALL p_add_column_if_missing('biz_safety_training_cert','record_id',      '`record_id` bigint DEFAULT NULL COMMENT ''关联培训记录ID''');
CALL p_add_column_if_missing('biz_safety_training_cert','course_id',      '`course_id` bigint DEFAULT NULL COMMENT ''关联课程ID''');
CALL p_add_column_if_missing('biz_safety_training_cert','issue_date',     '`issue_date` date DEFAULT NULL COMMENT ''发证日期''');
CALL p_add_column_if_missing('biz_safety_training_cert','expire_date',    '`expire_date` date DEFAULT NULL COMMENT ''有效期至''');
CALL p_add_column_if_missing('biz_safety_training_cert','issue_org',      '`issue_org` varchar(128) DEFAULT NULL COMMENT ''发证机构''');
CALL p_add_column_if_missing('biz_safety_training_cert','attachment',     '`attachment` varchar(500) DEFAULT NULL COMMENT ''证书附件''');
CALL p_add_column_if_missing('biz_safety_training_cert','remind_status',  '`remind_status` char(1) DEFAULT ''0'' COMMENT ''提醒状态''');
CALL p_add_column_if_missing('biz_safety_training_cert','enterprise_id',  '`enterprise_id` bigint DEFAULT NULL COMMENT ''所属企业ID''');
CALL p_add_column_if_missing('biz_safety_training_cert','status',         '`status` char(1) DEFAULT ''0'' COMMENT ''状态''');
CALL p_add_column_if_missing('biz_safety_training_cert','del_flag',       '`del_flag` char(1) DEFAULT ''0'' COMMENT ''删除标志''');
CALL p_add_column_if_missing('biz_safety_training_cert','create_by',      '`create_by` varchar(64) DEFAULT '''' COMMENT ''创建者''');
CALL p_add_column_if_missing('biz_safety_training_cert','create_time',    '`create_time` datetime DEFAULT NULL COMMENT ''创建时间''');
CALL p_add_column_if_missing('biz_safety_training_cert','update_by',      '`update_by` varchar(64) DEFAULT '''' COMMENT ''更新者''');
CALL p_add_column_if_missing('biz_safety_training_cert','update_time',    '`update_time` datetime DEFAULT NULL COMMENT ''更新时间''');
CALL p_add_column_if_missing('biz_safety_training_cert','remark',         '`remark` varchar(500) DEFAULT NULL COMMENT ''备注''');

DROP PROCEDURE IF EXISTS p_add_column_if_missing;

-- =============================================================
-- 追加（2026-11-13 晚）：部门管理"同步到人员"功能依赖的库表列
-- 现象：部署新版后部门管理页面报错（生产库 sys_dept 缺 leader_id 等列，
--       新版 mapper 查询 Unknown column）。
-- 内容：并入 upgrade_20261109_dept_user_fields.sql 与
--       upgrade_20261110_dept_leader_id.sql 的部门列部分（幂等）。
-- =============================================================

-- 1. sys_dept 新增 leader_id（部门领导人员ID）
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column leader_id bigint default null comment ''部门领导(人员ID,关联sys_user.user_id)'' after leader',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- 2. sys_dept 新增 charge_leader_id（分管领导人员ID）与 staff_count
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'charge_leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column charge_leader_id bigint default null comment ''分管领导(人员ID,关联sys_user.user_id)'' after dept_code',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'staff_count');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column staff_count int default null comment ''在职人员数量'' after charge_leader_id',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- 3. 按姓名回填 leader_id
update sys_dept d
set d.leader_id = (
    select u.user_id from sys_user u
    where u.nick_name = d.leader and u.del_flag = '0'
    order by u.user_id limit 1
)
where d.leader_id is null
  and d.leader is not null and d.leader <> '';

-- 验证：应返回 3（leader_id/charge_leader_id/staff_count）
SELECT CONCAT('VERIFY sys_dept new columns = ', COUNT(*)) AS result
FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'sys_dept'
  AND column_name IN ('leader_id','charge_leader_id','staff_count');

-- =============================================================
-- 追加（2026-11-13 晚三）：今天上游合并带来的其余全量升级
-- 1. 国家标准风险点种子（seed_national_risk_points.sql）
--    注意：该种子引用 gb_std 列，若生产库无此列则先补列（幂等）；
--    代码未使用 gb_std 字段（SafetyRiskPoint 无该属性），仅种子数据引用。
-- 2. 风险点种子数据（45 条国家标准风险点，幂等：先清空脏数据再插入）
-- =============================================================

-- 1. 补 gb_std 列（种子数据依赖；幂等）
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'biz_safety_risk_point' and column_name = 'gb_std');
set @sql := if(@col_exists = 0,
    'alter table biz_safety_risk_point add column gb_std varchar(100) default null comment ''国标依据'' after engineering_category',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- 2. 清理旧的空工程类别脏数据 + 插入国家标准风险点种子
DELETE FROM biz_safety_risk_point WHERE engineering_category IS NULL OR engineering_category = '';
-- （种子数据 45 条见 sql/seed_national_risk_points.sql，因含大批量 VALUES，
--   请单独执行：mysql ... ruoyi_lyjs < sql/seed_national_risk_points.sql）


-- ############ B 段：员工种子数据清理 ############

-- =====================================================================
-- 清理前期测试员工数据（从物理表删除）
-- 目标：lyjs_init_02_org_users.sql（20 人种子）+ hr_employee_profiles.sql（46 人种子）
--   两套 SQL 均写 biz_hr_employee，且 EMP0001~EMP0008 工号重叠（重复档案风险）
-- 说明：org_id 关联 sys_dept.dept_id（HrEmployeeMapper.xml:67），user_id 关联 sys_user.user_id
-- 使用前：请先执行"第 0 步：现状核对"，确认影响范围后再执行后续步骤
-- 兼容：MySQL 5.7+ / 8.0（CTE 仅在 8.0 分支使用，已标注）
-- =====================================================================

-- =====================================================================
-- 第 0 步：现状核对（只读，先执行确认影响范围）
-- =====================================================================

-- 0.1 核对种子数据现状：数量、是否重复、创建来源
SELECT employee_no, employee_name, org_id, user_id, create_by, create_time, remark
FROM biz_hr_employee
WHERE create_by = 'admin'
  AND remark LIKE '关联系统用户 user_id=%'
ORDER BY create_time, employee_no;

-- 0.2 核对重复工号（两套种子 SQL 工号重叠 EMP0001~EMP0008）
SELECT employee_no, COUNT(*) AS cnt, GROUP_CONCAT(employee_id) AS employee_ids
FROM biz_hr_employee
WHERE employee_no LIKE 'EMP%'
GROUP BY employee_no
HAVING COUNT(*) > 1;

-- 0.3 核对关联业务表引用（删除前必须确认没有业务数据挂在这些员工上）
SELECT 'leave' AS biz_table, COUNT(*) AS ref_cnt FROM biz_hr_leave          WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'attendance_record', COUNT(*) FROM biz_hr_attendance_record WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'attendance_monthly', COUNT(*) FROM biz_hr_attendance_monthly WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'leave_request',     COUNT(*) FROM biz_hr_leave_request     WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'leave_balance',     COUNT(*) FROM biz_hr_leave_balance     WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'overtime',          COUNT(*) FROM biz_hr_overtime          WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'outing',            COUNT(*) FROM biz_hr_outing            WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'business_trip',     COUNT(*) FROM biz_hr_business_trip     WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'contract',          COUNT(*) FROM biz_hr_contract          WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'certificate',       COUNT(*) FROM biz_hr_certificate       WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'clock_repair',      COUNT(*) FROM biz_hr_clock_repair      WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'payslip',           COUNT(*) FROM biz_hr_payslip           WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'social_security',   COUNT(*) FROM biz_hr_social_security   WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'regular',           COUNT(*) FROM biz_hr_regular           WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'transfer',          COUNT(*) FROM biz_hr_transfer          WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%')
UNION ALL SELECT 'workday_calendar',  COUNT(*) FROM biz_hr_workday_calendar  WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE create_by='admin' AND remark LIKE '关联系统用户%');

-- 若任何 ref_cnt > 0：先清理对应业务表数据，或改用逻辑删除（status/del_flag），勿直接执行物理删除

-- =====================================================================
-- 第 1 步：备份（执行清理前必须先跑）
-- =====================================================================

CREATE TABLE IF NOT EXISTS biz_hr_employee_bak_20261021 AS
SELECT * FROM biz_hr_employee
WHERE create_by = 'admin'
  AND remark LIKE '关联系统用户 user_id=%';

-- 两套种子 SQL 工号重叠，单独备份重复档案
CREATE TABLE IF NOT EXISTS biz_hr_employee_dup_bak_20261021 AS
SELECT e.* FROM biz_hr_employee e
WHERE e.employee_no LIKE 'EMP%'
  AND (SELECT COUNT(*) FROM biz_hr_employee e2 WHERE e2.employee_no = e.employee_no) > 1;

CREATE TABLE IF NOT EXISTS sys_user_seed_bak_20261021 AS
SELECT * FROM sys_user
WHERE user_id >= 2 AND user_id <= 21
  AND create_by = 'admin';

-- =====================================================================
-- 第 2 步：物理删除（核对 0.3 无业务引用后执行）
-- =====================================================================

-- 2.1 删除种子员工档案（按创建来源精确匹配，勿用工号范围，避免误删真实 EMP 档案）
DELETE FROM biz_hr_employee
WHERE create_by = 'admin'
  AND remark LIKE '关联系统用户 user_id=%';

-- 2.2 删除重复档案（0.2 查出的重复行，保留每组 employee_id 最小的一行）
-- MySQL 8.0：
-- DELETE e FROM biz_hr_employee e
-- JOIN (SELECT employee_no, MIN(employee_id) AS keep_id FROM biz_hr_employee WHERE employee_no LIKE 'EMP%' GROUP BY employee_no) k
--   ON e.employee_no = k.employee_no AND e.employee_id <> k.keep_id
-- WHERE e.employee_no LIKE 'EMP%';

-- 2.3 删除种子用户（与 lyjs_init_02_org_users.sql 对应，user_id 2~21，关联 biz_hr_employee.user_id）
DELETE FROM sys_user_role WHERE user_id >= 2 AND user_id <= 21;
DELETE FROM sys_user_post WHERE user_id >= 2 AND user_id <= 21;
DELETE FROM sys_user WHERE user_id >= 2 AND user_id <= 21 AND create_by = 'admin';

-- =====================================================================
-- 第 3 步：工号序号复位（清理后执行，否则新员工工号跳号）
-- =====================================================================

-- 3.1 清理后重新接续序号（按表内现存最大号复位，含两套种子工号 EMP0001~EMP0046）
-- mk_number_rule: rule_code='hr_employee', prefix='EMP', reset_type='0'(不复位), seq_length=4, current_seq=46
UPDATE mk_number_rule
SET current_seq = (
  SELECT COALESCE(MAX(CAST(SUBSTRING(employee_no, 4) AS UNSIGNED)), 0)
  FROM biz_hr_employee
  WHERE employee_no LIKE 'EMP%'
), update_by = 'admin'
WHERE rule_code = 'hr_employee';

-- 3.2 核对复位结果
SELECT rule_code, prefix, seq_length, current_seq
FROM mk_number_rule
WHERE rule_code = 'hr_employee';

-- =====================================================================
-- 第 4 步：清理后核对（只读）
-- =====================================================================

-- 4.1 核对种子数据已清除
SELECT COUNT(*) AS seed_left FROM biz_hr_employee
WHERE create_by = 'admin' AND remark LIKE '关联系统用户 user_id=%';

-- 4.2 核对工号无重复
SELECT employee_no, COUNT(*) AS cnt FROM biz_hr_employee
WHERE employee_no LIKE 'EMP%' GROUP BY employee_no HAVING COUNT(*) > 1;

-- 4.3 核对序号规则接续正确
SELECT rule_code, prefix, seq_length, current_seq FROM mk_number_rule WHERE rule_code = 'hr_employee';

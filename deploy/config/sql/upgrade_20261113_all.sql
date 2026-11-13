-- =============================================================
-- EDOP JSLY 生产库升级脚本（合并版）
-- 日期：2026-11-13
-- 说明：本文件合并 2026-11-13 全部升级内容，此后每次升级只执行
--       一个最新的合并 SQL 即可。
-- 幂等：整文件可重复执行，结果一致（建表 IF NOT EXISTS、加列
--       information_schema 判断、数据 NOT EXISTS 判重）。
-- 执行：mysql -uroot -p --default-character-set=utf8mb4 ruoyi_lyjs < 本文件
-- -------------------------------------------------------------
-- 合并内容（按序执行）：
--   1. 生产修复总入口（PJM编号规则+菜单/证书employee_id/安全模块
--      worker/attendee/cert补列/编号规则菜单query格式/部门领导三列）
--   2. 在线考试模块 P0（7张表+字典+菜单，含 DROP TABLE IF EXISTS，
--      ⚠️ 若生产已录入考试数据会清空，首次部署无影响）
--   3. 在线考试 P1 增量（补考字段等）
--   4. 在线考试来源字段
--   5. 食品行业题库种子（29题，幂等）
--   6. 国家标准风险点种子（32条，幂等）
-- 验证：文件末尾含各段 VERIFY 查询，执行后逐项核对。
-- =============================================================

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

-- =============================================================
-- 第 2 段：在线考试模块 P0 全量
-- =============================================================
-- ============================================================
-- 安全生产 · 在线考试模块 全量SQL脚本
-- 包含：7张业务表DDL + 字典类型/数据 + 菜单权限
-- 依据：《安全生产模块考试功能开发需求方案》v1.1
-- 规范：表前缀 biz_safety_*（与安全模块一致）、COLLATE=utf8mb4_general_ci（§3.6强制）、
--       字段注释字典标注（§3.3.1强制）、数据权限归属字段（§3.5强制）
-- 幂等：可重复执行（DROP IF EXISTS + DELETE 再 INSERT）
-- ============================================================

-- =============================================
-- 零、清理旧数据（防止重复执行报错）
-- =============================================
DELETE FROM sys_role_menu WHERE menu_id IN (
    SELECT menu_id FROM sys_menu WHERE perms LIKE 'safety:exam:%'
);
DELETE FROM sys_menu WHERE perms LIKE 'safety:exam:%';

DELETE FROM sys_dict_data WHERE dict_type LIKE 'safety_exam_%';
DELETE FROM sys_dict_type WHERE dict_type LIKE 'safety_exam_%';

-- =============================================
-- 一、业务表 DDL（7张）
-- =============================================

-- 1. 题库分类表（主数据/共享资源，豁免数据权限归属字段）
DROP TABLE IF EXISTS biz_safety_exam_category;
CREATE TABLE biz_safety_exam_category (
    category_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    parent_id          BIGINT       DEFAULT 0                COMMENT '父分类ID（支持两级）',
    category_name      VARCHAR(50)  NOT NULL                 COMMENT '分类名称',
    order_num          INT          DEFAULT 0                COMMENT '显示顺序',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (category_id),
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试题库分类表';

-- 2. 题库表（主数据/共享资源，豁免数据权限归属字段）
DROP TABLE IF EXISTS biz_safety_exam_question;
CREATE TABLE biz_safety_exam_question (
    question_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '题目ID',
    question_type      CHAR(1)      NOT NULL                 COMMENT '题型（字典 safety_exam_question_type）',
    content            TEXT         NOT NULL                 COMMENT '题干',
    options            TEXT         NOT NULL                 COMMENT '选项（JSON数组，如["A内容","B内容"]；判断题固定["正确","错误"]）',
    answer             VARCHAR(20)  NOT NULL                 COMMENT '正确答案（如 A / ABD / 对）',
    analysis           VARCHAR(500)                          COMMENT '答案解析',
    category_id        BIGINT                                COMMENT '题目分类ID',
    category_name      VARCHAR(50)                           COMMENT '分类名称（冗余）',
    difficulty         CHAR(1)      DEFAULT '2'              COMMENT '难度（字典 safety_exam_difficulty）',
    job_types          VARCHAR(200)                          COMMENT '适用工种（逗号分隔，字典 safety_job_type）',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态（0启用 1停用）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (question_id),
    KEY idx_category (category_id),
    KEY idx_type_diff (question_type, difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='安全生产考试题库表';

-- 3. 试卷表（主数据，发布后题目集合冻结）
DROP TABLE IF EXISTS biz_safety_exam_paper;
CREATE TABLE biz_safety_exam_paper (
    paper_id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '试卷ID',
    paper_name         VARCHAR(100) NOT NULL                 COMMENT '试卷名称',
    course_id          BIGINT                                COMMENT '关联培训课程ID（biz_safety_training_course）',
    course_name        VARCHAR(255)                          COMMENT '课程名称（冗余）',
    total_score        DECIMAL(5,1) DEFAULT 100.0            COMMENT '总分（按题目分值自动计算）',
    pass_score         DECIMAL(5,1) DEFAULT 60.0             COMMENT '及格分',
    duration           INT          DEFAULT 60               COMMENT '考试时长（分钟）',
    shuffle_question   CHAR(1)      DEFAULT 'N'              COMMENT '题目乱序（Y是 N否）',
    shuffle_option     CHAR(1)      DEFAULT 'N'              COMMENT '选项乱序（Y是 N否）',
    switch_limit       INT          DEFAULT 0                COMMENT '切屏限制次数（0不限）',
    show_analysis      CHAR(1)      DEFAULT 'Y'              COMMENT '交卷后展示解析（Y是 N否）',
    status             CHAR(1)      DEFAULT '0'              COMMENT '试卷状态（字典 safety_exam_paper_status）',
    publish_by         VARCHAR(64)                           COMMENT '发布人',
    publish_time       DATETIME                              COMMENT '发布时间',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (paper_id),
    KEY idx_course (course_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试试卷表';

-- 4. 试卷题目明细表（快照式：发布时冻结题干/选项/答案，判分依据）
DROP TABLE IF EXISTS biz_safety_exam_paper_question;
CREATE TABLE biz_safety_exam_paper_question (
    id                 BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    paper_id           BIGINT       NOT NULL                 COMMENT '试卷ID',
    question_id        BIGINT       NOT NULL                 COMMENT '来源题目ID（biz_safety_exam_question）',
    question_type      CHAR(1)      NOT NULL                 COMMENT '题型（字典 safety_exam_question_type）',
    content            TEXT         NOT NULL                 COMMENT '题干快照',
    options            TEXT         NOT NULL                 COMMENT '选项快照（JSON数组）',
    answer             VARCHAR(20)  NOT NULL                 COMMENT '答案快照（判分依据）',
    analysis           VARCHAR(500)                          COMMENT '解析快照',
    score              DECIMAL(4,1) DEFAULT 1.0              COMMENT '本题分值',
    sort_no            INT          DEFAULT 0                COMMENT '题目顺序号',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_paper (paper_id),
    KEY idx_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='试卷题目明细表（快照）';

-- 5. 考试场次表（事务单据：必须带 dept_id，接入 @DataScope）
DROP TABLE IF EXISTS biz_safety_exam;
CREATE TABLE biz_safety_exam (
    exam_id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '考试ID',
    exam_name          VARCHAR(100) NOT NULL                 COMMENT '考试名称',
    paper_id           BIGINT       NOT NULL                 COMMENT '试卷ID',
    paper_name         VARCHAR(100)                          COMMENT '试卷名称（冗余）',
    record_id          BIGINT                                COMMENT '关联培训记录ID（biz_safety_training_record，可空）',
    start_time         DATETIME     NOT NULL                 COMMENT '考试开始时间',
    end_time           DATETIME     NOT NULL                 COMMENT '考试结束时间（窗口截止）',
    exam_status        CHAR(1)      DEFAULT '0'              COMMENT '考试状态（字典 safety_exam_exam_status）',
    publish_by         VARCHAR(64)                           COMMENT '发布人',
    publish_time       DATETIME                              COMMENT '发布时间',
    dept_id            BIGINT                                COMMENT '归属部门ID（数据权限，创建时自动填充）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (exam_id),
    KEY idx_paper (paper_id),
    KEY idx_record (record_id),
    KEY idx_status (exam_status),
    KEY idx_start_end (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试场次表';

-- 6. 考生考试记录表（人员归属型：dept_id + user_id，接入 @DataScope）
DROP TABLE IF EXISTS biz_safety_exam_record;
CREATE TABLE biz_safety_exam_record (
    record_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '考试记录ID',
    exam_id            BIGINT       NOT NULL                 COMMENT '考试ID',
    paper_id           BIGINT       NOT NULL                 COMMENT '试卷ID',
    user_id            BIGINT       NOT NULL                 COMMENT '考生用户ID（关联sys_user）',
    user_name          VARCHAR(128)                          COMMENT '考生姓名（冗余）',
    worker_id          BIGINT                                COMMENT '关联员工ID（biz_safety_worker，可空）',
    attendee_id        BIGINT                                COMMENT '关联培训参训人ID（biz_safety_training_attendee，可空）',
    start_time         DATETIME                              COMMENT '本人开始答题时间',
    submit_time        DATETIME                              COMMENT '提交时间',
    used_seconds       INT                                   COMMENT '实际用时（秒）',
    submit_type        CHAR(1)                               COMMENT '提交方式（字典 safety_exam_submit_type）',
    total_score        DECIMAL(5,1)                          COMMENT '总得分',
    is_pass            CHAR(1)                               COMMENT '是否合格（0不合格 1合格）',
    judge_status       CHAR(1)      DEFAULT '0'              COMMENT '判分状态（0未交卷 1已自动判分）',
    switch_count       INT          DEFAULT 0                COMMENT '切屏次数',
    exam_result        CHAR(1)      DEFAULT '0'              COMMENT '考生状态（字典 safety_exam_result）',
    dept_id            BIGINT                                COMMENT '归属部门ID（数据权限）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (record_id),
    UNIQUE KEY uk_exam_user (exam_id, user_id),
    KEY idx_user (user_id),
    KEY idx_exam (exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考生考试记录表';

-- 7. 答题明细表（追溯数据，随考试记录权限）
DROP TABLE IF EXISTS biz_safety_exam_answer;
CREATE TABLE biz_safety_exam_answer (
    answer_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '答题明细ID',
    record_id          BIGINT       NOT NULL                 COMMENT '考试记录ID',
    paper_question_id  BIGINT       NOT NULL                 COMMENT '试卷题目明细ID',
    user_answer        VARCHAR(20)                           COMMENT '考生答案（如 A / ABD；空=未答）',
    is_correct         CHAR(1)                               COMMENT '是否正确（1正确 0错误）',
    score              DECIMAL(4,1) DEFAULT 0                COMMENT '本题得分',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (answer_id),
    UNIQUE KEY uk_record_question (record_id, paper_question_id),
    KEY idx_record (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试答题明细表';

-- =============================================
-- 二、字典类型
-- =============================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('考试题型', 'safety_exam_question_type', '0', 'admin', NOW(), '考试题目题型'),
('考试题目难度', 'safety_exam_difficulty', '0', 'admin', NOW(), '考试题目难度等级'),
('试卷状态', 'safety_exam_paper_status', '0', 'admin', NOW(), '考试试卷状态'),
('考试状态', 'safety_exam_exam_status', '0', 'admin', NOW(), '考试场次状态'),
('考生考试状态', 'safety_exam_result', '0', 'admin', NOW(), '考生在考试中的状态'),
('考试提交方式', 'safety_exam_submit_type', '0', 'admin', NOW(), '考生交卷方式');

-- =============================================
-- 三、字典数据
-- =============================================
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
-- 考试题型
(NULL, 1, '单选题', '1', 'safety_exam_question_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '多选题', '2', 'safety_exam_question_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '判断题', '3', 'safety_exam_question_type', '', 'success', 'N', '0', 'admin', NOW(), ''),

-- 考试题目难度
(NULL, 1, '简单', '1', 'safety_exam_difficulty', '', 'info', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '中等', '2', 'safety_exam_difficulty', '', 'primary', 'Y', '0', 'admin', NOW(), ''),
(NULL, 3, '困难', '3', 'safety_exam_difficulty', '', 'danger', 'N', '0', 'admin', NOW(), ''),

-- 试卷状态
(NULL, 1, '草稿', '0', 'safety_exam_paper_status', '', 'info', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '已发布', '1', 'safety_exam_paper_status', '', 'success', 'N', '0', 'admin', NOW(), '发布后题目集合冻结'),
(NULL, 3, '停用', '2', 'safety_exam_paper_status', '', 'danger', 'N', '0', 'admin', NOW(), ''),

-- 考试状态
(NULL, 1, '草稿', '0', 'safety_exam_exam_status', '', 'info', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '已发布', '1', 'safety_exam_exam_status', '', 'primary', 'N', '0', 'admin', NOW(), '未到开始时间'),
(NULL, 3, '考试中', '2', 'safety_exam_exam_status', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '已结束', '3', 'safety_exam_exam_status', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '已撤回', '4', 'safety_exam_exam_status', '', 'danger', 'N', '0', 'admin', NOW(), '仅未开始的场次可撤回'),

-- 考生考试状态
(NULL, 1, '未开始', '0', 'safety_exam_result', '', 'info', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '进行中', '1', 'safety_exam_result', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '已交卷', '2', 'safety_exam_result', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '缺考', '3', 'safety_exam_result', '', 'danger', 'N', '0', 'admin', NOW(), '窗口结束无答题记录'),
(NULL, 5, '已判分', '4', 'safety_exam_result', '', 'success', 'N', '0', 'admin', NOW(), '成绩已回写'),

-- 考试提交方式
(NULL, 1, '手动提交', '1', 'safety_exam_submit_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '超时自动提交', '2', 'safety_exam_submit_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '切屏强制交卷', '3', 'safety_exam_submit_type', '', 'danger', 'N', '0', 'admin', NOW(), '切屏超限自动交卷');

-- =============================================
-- 四、菜单权限（教育培训下新建「考试管理」二级目录，置于学时统计之前；权限域独立为 safety:exam:*）
-- =============================================

-- 获取教育培训目录ID
SELECT @trainingMenuId := menu_id FROM sys_menu WHERE menu_name = '教育培训' AND parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0);

-- ---- 「考试管理」二级目录（教育培训下排序6，位于学时统计之前） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '考试管理', @trainingMenuId, 6, 'exam', 'M', '0', '0', '', 'form', 'admin', NOW(), '考试功能子目录'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '考试管理' AND parent_id = @trainingMenuId AND menu_type = 'M');
SELECT @examDirMenuId := menu_id FROM sys_menu WHERE menu_name = '考试管理' AND parent_id = @trainingMenuId AND menu_type = 'M';

-- 学时统计顺移至考试管理之后（存在才更新）
UPDATE sys_menu SET order_num = 7 WHERE menu_name = '学时统计' AND parent_id = @trainingMenuId;

-- ---- 题库管理（考试管理下排序1） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('题库管理', @examDirMenuId, 1, 'examQuestion', 'safety/exam/question/index', 1, 0, 'C', '0', '0', 'safety:exam:question:list', 'education', 'admin', NOW(), '考试题库管理菜单');
SET @questionMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('题库查询', @questionMenuId, 1, '#', 'F', '0', '0', 'safety:exam:question:query',  'admin', NOW(), ''),
('题库新增', @questionMenuId, 2, '#', 'F', '0', '0', 'safety:exam:question:add',    'admin', NOW(), ''),
('题库修改', @questionMenuId, 3, '#', 'F', '0', '0', 'safety:exam:question:edit',   'admin', NOW(), ''),
('题库删除', @questionMenuId, 4, '#', 'F', '0', '0', 'safety:exam:question:remove', 'admin', NOW(), ''),
('题库导入', @questionMenuId, 5, '#', 'F', '0', '0', 'safety:exam:question:import', 'admin', NOW(), ''),
('题库导出', @questionMenuId, 6, '#', 'F', '0', '0', 'safety:exam:question:export', 'admin', NOW(), '');

-- ---- 试卷管理（考试管理下排序2） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('试卷管理', @examDirMenuId, 2, 'examPaper', 'safety/exam/paper/index', 1, 0, 'C', '0', '0', 'safety:exam:paper:list', 'form', 'admin', NOW(), '考试试卷管理菜单');
SET @paperMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('试卷查询', @paperMenuId, 1, '#', 'F', '0', '0', 'safety:exam:paper:query',   'admin', NOW(), ''),
('试卷新增', @paperMenuId, 2, '#', 'F', '0', '0', 'safety:exam:paper:add',     'admin', NOW(), ''),
('试卷修改', @paperMenuId, 3, '#', 'F', '0', '0', 'safety:exam:paper:edit',    'admin', NOW(), ''),
('试卷删除', @paperMenuId, 4, '#', 'F', '0', '0', 'safety:exam:paper:remove',  'admin', NOW(), ''),
('试卷发布', @paperMenuId, 5, '#', 'F', '0', '0', 'safety:exam:paper:publish', 'admin', NOW(), '发布后题目集合冻结');

-- ---- 考试场次（考试管理下排序3） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('考试场次', @examDirMenuId, 3, 'examManage', 'safety/exam/manage/index', 1, 0, 'C', '0', '0', 'safety:exam:list', 'date-range', 'admin', NOW(), '考试场次管理菜单');
SET @examMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('考试查询',     @examMenuId, 1, '#', 'F', '0', '0', 'safety:exam:query',    'admin', NOW(), ''),
('考试新增',     @examMenuId, 2, '#', 'F', '0', '0', 'safety:exam:add',      'admin', NOW(), ''),
('考试修改',     @examMenuId, 3, '#', 'F', '0', '0', 'safety:exam:edit',     'admin', NOW(), ''),
('考试删除',     @examMenuId, 4, '#', 'F', '0', '0', 'safety:exam:remove',   'admin', NOW(), ''),
('考试发布',     @examMenuId, 5, '#', 'F', '0', '0', 'safety:exam:publish',  'admin', NOW(), ''),
('考试强制结束', @examMenuId, 6, '#', 'F', '0', '0', 'safety:exam:forceEnd', 'admin', NOW(), ''),
('成绩导出',     @examMenuId, 7, '#', 'F', '0', '0', 'safety:exam:export',   'admin', NOW(), ''),
('成绩统计',     @examMenuId, 8, '#', 'F', '0', '0', 'safety:exam:stats',    'admin', NOW(), ''),
('成绩调分',     @examMenuId, 9, '#', 'F', '0', '0', 'safety:exam:score:edit', 'admin', NOW(), '人工调分留痕');

-- ---- 我的考试（考生端，考试管理下排序4，需授权给普通员工角色） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('我的考试', @examDirMenuId, 4, 'examTake', 'safety/exam/take/index', 1, 0, 'C', '0', '0', 'safety:exam:myList', 'checkbox', 'admin', NOW(), '考生端我的考试菜单');
SET @takeMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('在线答题', @takeMenuId, 1, '#', 'F', '0', '0', 'safety:exam:take', 'admin', NOW(), '进入考试/答题/提交/查分');

-- =============================================
-- 五、角色授权
-- =============================================
-- 管理员角色（role_id=2，与 safety_all.sql 口径一致）分配全部考试权限（含考试管理目录）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE perms LIKE 'safety:exam:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 2);

-- 普通员工角色如需在线考试，请在【系统管理→角色管理】为对应角色勾选
-- 「教育培训→我的考试」菜单及其「在线答题」按钮权限

-- =============================================
-- 六、定时任务（考试状态兜底：状态流转 + 缺考标记，幂等可重复）
-- =============================================
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '考试状态兜底任务', 'DEFAULT', 'safetyExamTask.run', '0 * * * * ?', '3', '1', '0', 'admin', NOW(), '场次状态流转与缺考标记（幂等），建议每分钟'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'safetyExamTask.run');

-- =============================================================
-- 第 3 段：在线考试 P1 增量
-- =============================================================
-- =====================================================================
-- 安全生产模块-考试功能 P1 增量升级脚本（FR-7/8/10/11/12/13）
-- 依赖：safety_exam_all.sql（P0 全量脚本）已执行
-- 幂等性：可重复执行；新增列通过 information_schema 判断
-- 执行库：ry-vue
-- =====================================================================

-- ---------------------------------------------------------------------
-- 一、biz_safety_exam 增加补考相关字段（FR-8 补考管理）
--   retake_from_id  补考来源场次ID（NULL=非补考）
--   retake_count    补考代数（0=首次考试，1=第1次补考...）
--   score_strategy  成绩覆盖策略（1=取最高 2=取最新，回写参训人时生效）
-- ---------------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'retake_from_id');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN retake_from_id bigint NULL COMMENT ''补考来源场次ID'' AFTER record_id',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'retake_count');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN retake_count int NOT NULL DEFAULT 0 COMMENT ''补考代数（0=首次考试）'' AFTER retake_from_id',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'score_strategy');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN score_strategy char(1) NULL DEFAULT ''2'' COMMENT ''成绩覆盖策略（1取最高 2取最新）'' AFTER retake_count',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 二、交卷方式字典补充「无操作锁卷」（FR-13 答题页基础防护）
-- ---------------------------------------------------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '无操作锁卷', '4', 'safety_exam_submit_type', NULL, 'warning', 'N', '0', 'admin', NOW(), 'FR-13：无操作超时自动锁卷交卷'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_exam_submit_type' AND dict_value = '4');

-- =============================================================
-- 第 4 段：在线考试来源字段
-- =============================================================
-- =====================================================================
-- 安全生产模块-考试功能 增量升级：考生来源双模式
--   1) biz_safety_exam 增加 attendee_source（1=来自培训 2=手动添加）
--   2) 新增 biz_safety_exam_user 手动考试名单表
-- 依赖：safety_exam_all.sql 已执行
-- 幂等性：可重复执行；新增列通过 information_schema 判断
-- 执行库：ry-vue
-- =====================================================================

-- 一、biz_safety_exam 增加考生来源字段
SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'attendee_source');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN attendee_source char(1) NOT NULL DEFAULT ''1'' COMMENT ''考生来源（1=来自培训 2=手动添加）'' AFTER record_id',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 二、手动考试名单表
CREATE TABLE IF NOT EXISTS biz_safety_exam_user (
    exam_user_id       BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    exam_id            BIGINT       NOT NULL                 COMMENT '考试场次ID（biz_safety_exam）',
    user_id            BIGINT       NOT NULL                 COMMENT '系统用户ID（sys_user）',
    user_name          VARCHAR(64)                           COMMENT '用户显示名（冗余）',
    dept_id            BIGINT                                COMMENT '归属部门ID（数据权限）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    PRIMARY KEY (exam_user_id),
    UNIQUE KEY uk_exam_user (exam_id, user_id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试手动添加人员表';

-- =============================================================
-- 第 5 段：食品行业题库种子
-- =============================================================
-- ============================================================
-- 食品行业安全考试题库种子数据（幂等，可重复执行）
-- 内容：1 个一级分类 + 5 个二级分类 + 29 道题（单选11/多选4/判断14）
-- 判重规则：分类按名称+父级，题目按题干 content
-- ============================================================

-- 1. 一级分类：食品安全生产
INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT 0, '食品安全生产', 1, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '食品安全生产' AND parent_id = 0 AND del_flag = '0');

SET @food_root := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '食品安全生产' AND parent_id = 0 AND del_flag = '0' LIMIT 1);

-- 2. 二级分类（幂等）
INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '食品安全法规', 1, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '食品安全法规' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '卫生与消毒规范', 2, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '卫生与消毒规范' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '仓储与物料管理', 3, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '仓储与物料管理' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '添加剂与质量管理', 4, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '添加剂与质量管理' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '消防与作业安全', 5, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '消防与作业安全' AND parent_id = @food_root AND del_flag = '0');

SET @cat_law := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '食品安全法规' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_hyg := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '卫生与消毒规范' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_wh  := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '仓储与物料管理' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_qa  := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '添加剂与质量管理' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_fire:= (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '消防与作业安全' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);

-- 3. 题目（幂等：按题干判重）
-- 语法：INSERT INTO ... SELECT ... FROM DUAL WHERE NOT EXISTS (...)

-- ============ 食品安全法规 ============
-- 单选1（简单）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '从事食品生产活动，应当依法取得（　）。',
'["营业执照","食品生产许可证","卫生登记证","安全生产许可证"]', 'B',
'《食品安全法》规定，从事食品生产活动应当依法取得食品生产许可证。',
@cat_law, '食品安全法规', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '从事食品生产活动，应当依法取得（　）。' AND del_flag = '0');

-- 单选2（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '食品生产许可证的有效期为（　）年。',
'["3","5","8","10"]', 'B',
'《食品生产许可管理办法》规定，食品生产许可证有效期为5年。',
@cat_law, '食品安全法规', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品生产许可证的有效期为（　）年。' AND del_flag = '0');

-- 单选3（困难）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '企业发现生产的食品不符合食品安全标准时，正确的做法是（　）。',
'["继续生产，降低损失","立即停止生产、召回问题食品并按规定报告","降价促销尽快处理","更换包装后继续销售"]', 'B',
'应立即停止生产、主动召回，并按规定向监管部门报告，防止危害扩大。',
@cat_law, '食品安全法规', '3', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '企业发现生产的食品不符合食品安全标准时，正确的做法是（　）。' AND del_flag = '0');

-- 单选4（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '发生食品安全事故后，涉事企业应当（　）。',
'["立即采取措施防止事故扩大，并按规定及时报告","隐瞒不报，内部消化处理","等事故平息后再向上级汇报","只通知相关客户即可"]', 'A',
'发生食品安全事故的单位应当立即处置、防止事故扩大，并按规定及时向监管部门报告，不得隐瞒。',
@cat_law, '食品安全法规', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '发生食品安全事故后，涉事企业应当（　）。' AND del_flag = '0');

-- 多选1（中等）ABCD
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '食品生产企业应当建立的食品安全管理制度包括（　）。',
'["食品安全自查制度","从业人员健康管理制度","进货查验记录制度","不安全食品召回制度"]', 'ABCD',
'以上四项均为《食品安全法》要求食品生产经营者建立的食品安全管理制度。',
@cat_law, '食品安全法规', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品生产企业应当建立的食品安全管理制度包括（　）。' AND del_flag = '0');

-- 判断1（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '从业人员应当取得健康证明后方可从事接触直接入口食品的工作。',
'["正确","错误"]', '对',
'《食品安全法》规定，从事接触直接入口食品工作的从业人员应当每年进行健康检查，取得健康证明后方可上岗。',
@cat_law, '食品安全法规', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '从业人员应当取得健康证明后方可从事接触直接入口食品的工作。' AND del_flag = '0');

-- ============ 卫生与消毒规范 ============
-- 单选5（简单）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '接触直接入口食品的人员正确的洗手消毒做法是（　）。',
'["用清水冲一下即可","按规程洗手后使用消毒液消毒并烘干双手","佩戴手套后可以不洗手","洗完手用衣袖擦干"]', 'B',
'应按规程洗手并使用消毒液消毒，防止手部污染直接入口食品。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '接触直接入口食品的人员正确的洗手消毒做法是（　）。' AND del_flag = '0');

-- 单选6（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '进入食品生产车间前的正确做法是（　）。',
'["直接进入车间开始作业","更换工作服、佩戴口罩和工作帽，洗手消毒后经消毒通道进入","穿便服快速进入","只需简单洗手即可进入"]', 'B',
'进入车间应严格执行更衣、戴口罩帽子、洗手消毒等卫生程序，防止人员带入污染。',
@cat_hyg, '卫生与消毒规范', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '进入食品生产车间前的正确做法是（　）。' AND del_flag = '0');

-- 单选7（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '生产车间使用清洗消毒剂的要求是（　）。',
'["任意采购价格便宜的即可","使用符合要求的食品级清洗消毒产品，按规定浓度配制并专柜存放、专人管理","与食品原料放在一起方便取用","浓度越高消毒效果越好，无需按比例配制"]', 'B',
'清洗消毒剂应符合相关标准，按规定浓度配制，专柜存放、专人管理，防止误用和污染食品。',
@cat_hyg, '卫生与消毒规范', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '生产车间使用清洗消毒剂的要求是（　）。' AND del_flag = '0');

-- 多选2（中等）ABCD
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '进入车间人员的个人卫生要求包括（　）。',
'["持有效健康证上岗","穿戴清洁的工作服、帽，头发不外露","不得佩戴首饰手表，不留长指甲、不涂指甲油","不得在车间内吸烟和饮食"]', 'ABCD',
'以上均为食品生产人员基本的个人卫生要求，目的是防止人员对食品造成污染。',
@cat_hyg, '卫生与消毒规范', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '进入车间人员的个人卫生要求包括（　）。' AND del_flag = '0');

-- 判断2（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '患有痢疾、伤寒、甲型病毒性肝炎等消化道传染病的人员，不得从事接触直接入口食品的工作。',
'["正确","错误"]', '对',
'《食品安全法》明确规定，患有国务院卫生行政部门规定的有碍食品安全疾病的人员，不得从事接触直接入口食品的工作。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '患有痢疾、伤寒、甲型病毒性肝炎等消化道传染病的人员，不得从事接触直接入口食品的工作。' AND del_flag = '0');

-- 判断3（简单）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '为了节省时间，工作人员可以穿着工作服直接进出卫生间。',
'["正确","错误"]', '错',
'进出卫生间必须脱去工作服，回来后重新洗手消毒，防止交叉污染。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '为了节省时间，工作人员可以穿着工作服直接进出卫生间。' AND del_flag = '0');

-- 判断4（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '直接接触食品的设备、工具和容器应按规程定期清洗消毒，并如实记录。',
'["正确","错误"]', '对',
'食品接触面应定期清洗消毒并记录，保证卫生状态可追溯。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '直接接触食品的设备、工具和容器应按规程定期清洗消毒，并如实记录。' AND del_flag = '0');

-- ============ 仓储与物料管理 ============
-- 单选8（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '仓库内物料码放一般要求离地面不低于（　）厘米、离墙面不低于（　）厘米。',
'["5、10","10、30","30、50","50、50"]', 'B',
'通行仓储卫生要求：物料离地不低于10cm、离墙不低于30cm，便于清洁、通风和检查。',
@cat_wh, '仓储与物料管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '仓库内物料码放一般要求离地面不低于（　）厘米、离墙面不低于（　）厘米。' AND del_flag = '0');

-- 多选3（中等）ABC
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '食品仓库日常管理的基本要求包括（　）。',
'["分区分类存放，离墙离地","定期检查记录温湿度","先进先出，账物相符","与有毒有害化学品混放，提高装卸效率"]', 'ABC',
'仓库应分区分类、离墙离地存放，定期记录温湿度，执行先进先出；严禁与有毒有害物品混放。',
@cat_wh, '仓储与物料管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品仓库日常管理的基本要求包括（　）。' AND del_flag = '0');

-- 判断5（简单）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '原料、辅料、包装材料可以与农药、清洁剂等化学品存放在同一仓库。',
'["正确","错误"]', '错',
'化学品必须单独存放并明显标识，与食品原料、辅料、包装材料严格隔离，防止化学污染。',
@cat_wh, '仓储与物料管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '原料、辅料、包装材料可以与农药、清洁剂等化学品存放在同一仓库。' AND del_flag = '0');

-- 判断6（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '物料发放应遵循先进先出的原则。',
'["正确","错误"]', '对',
'先进先出可防止原料过期变质，保证物料在保质期内使用。',
@cat_wh, '仓储与物料管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '物料发放应遵循先进先出的原则。' AND del_flag = '0');

-- 判断7（中等）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '仓库发现霉变或超过保质期的原料，应立即隔离存放并按规定处理，不得投入生产。',
'["正确","错误"]', '对',
'霉变、过期原料存在食品安全隐患，必须隔离标识并按规定处理，严禁流入生产环节。',
@cat_wh, '仓储与物料管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '仓库发现霉变或超过保质期的原料，应立即隔离存放并按规定处理，不得投入生产。' AND del_flag = '0');

-- ============ 添加剂与质量管理 ============
-- 单选9（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '食品添加剂的使用应当符合（　）的要求。',
'["GB 2760《食品安全国家标准 食品添加剂使用标准》","GB 14881《食品生产通用卫生规范》","GB 7718《预包装食品标签通则》","企业内部自定标准"]', 'A',
'GB 2760规定了食品添加剂的允许品种、使用范围和最大使用量，是添加剂使用的强制标准。',
@cat_qa, '添加剂与质量管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品添加剂的使用应当符合（　）的要求。' AND del_flag = '0');

-- 判断8（简单）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '为了改善产品口感，食品添加剂可以超范围、超限量使用。',
'["正确","错误"]', '错',
'食品添加剂必须严格按照GB 2760规定的品种、范围和限量使用，超范围超限量使用属于违法行为。',
@cat_qa, '添加剂与质量管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '为了改善产品口感，食品添加剂可以超范围、超限量使用。' AND del_flag = '0');

-- 判断9（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '企业应按规定对产品进行出厂检验，检验合格后方可出厂销售。',
'["正确","错误"]', '对',
'《食品安全法》要求食品生产企业对其产品进行检验，合格后方可出厂销售。',
@cat_qa, '添加剂与质量管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '企业应按规定对产品进行出厂检验，检验合格后方可出厂销售。' AND del_flag = '0');

-- 判断10（困难）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '食品生产过程的相关记录和凭证，保存期限不得少于产品保质期满后六个月；没有明确保质期的，保存期限不得少于二年。',
'["正确","错误"]', '对',
'《食品安全法》规定，记录和凭证保存期限不得少于产品保质期满后六个月；没有明确保质期的，保存期限不得少于二年。',
@cat_qa, '添加剂与质量管理', '3', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品生产过程的相关记录和凭证，保存期限不得少于产品保质期满后六个月；没有明确保质期的，保存期限不得少于二年。' AND del_flag = '0');

-- ============ 消防与作业安全 ============
-- 单选10（简单）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '使用干粉灭火器灭火时，应站在（　），将喷嘴对准火焰（　）喷射。',
'["上风方向、根部","下风方向、上部","上风方向、上部","任意方向、中部"]', 'A',
'应站在上风方向，对准火焰根部喷射，才能有效灭火并保障自身安全。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '使用干粉灭火器灭火时，应站在（　），将喷嘴对准火焰（　）喷射。' AND del_flag = '0');

-- 单选11（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '车间内驾驶叉车的安全要求，正确的是（　）。',
'["速度不限，尽快完成搬运","限速行驶，禁止载人，转弯减速鸣笛","可以顺路搭载同事","倒车时无需观察后方"]', 'B',
'叉车应限速行驶、禁止载人，转弯和通道口减速鸣笛，防止碰撞伤人。',
@cat_fire, '消防与作业安全', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '车间内驾驶叉车的安全要求，正确的是（　）。' AND del_flag = '0');

-- 多选4（简单）ABCD
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '灭火的基本方法包括（　）。',
'["冷却灭火法","窒息灭火法","隔离灭火法","化学抑制灭火法"]', 'ABCD',
'灭火的四种基本方法：冷却、窒息、隔离、化学抑制，实际扑救常组合使用。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '灭火的基本方法包括（　）。' AND del_flag = '0');

-- 判断11（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '灭火时应将灭火剂喷射到火焰根部，而不是火焰上部。',
'["正确","错误"]', '对',
'对准火焰根部喷射才能隔绝燃烧源头，喷向上部无法有效灭火。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '灭火时应将灭火剂喷射到火焰根部，而不是火焰上部。' AND del_flag = '0');

-- 判断12（中等）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '电动叉车可以停放在存放易燃易爆物品的仓库内充电。',
'["正确","错误"]', '错',
'充电过程可能产生电火花，易燃易爆场所严禁充电作业，应在专用充电区域进行。',
@cat_fire, '消防与作业安全', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '电动叉车可以停放在存放易燃易爆物品的仓库内充电。' AND del_flag = '0');

-- 判断13（中等）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '电气设备发生火灾时，应先切断电源，再使用干粉或二氧化碳灭火器扑救，严禁用水直接扑救。',
'["正确","错误"]', '对',
'带电灭火有触电危险，必须先断电；水导电，严禁用水扑救电气火灾。',
@cat_fire, '消防与作业安全', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '电气设备发生火灾时，应先切断电源，再使用干粉或二氧化碳灭火器扑救，严禁用水直接扑救。' AND del_flag = '0');

-- 判断14（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '员工发现食品安全隐患或安全事故隐患，应立即报告车间负责人或安全管理人员。',
'["正确","错误"]', '对',
'发现隐患及时报告是每名员工的职责，便于第一时间处置、防止事故扩大。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '员工发现食品安全隐患或安全事故隐患，应立即报告车间负责人或安全管理人员。' AND del_flag = '0');

-- =============================================================
-- 第 6 段：国家标准风险点种子
-- =============================================================
-- =============================================================
-- 国家标准风险点种子数据
-- 基于 GB/T 50841-2013 建设工程分类标准 + JGJ 建筑施工安全检查标准
-- 7 种工程类别 × 每种 5-8 条典型风险点 ≈ 45 条
-- 幂等：先清旧数据再插入（risk_code UK 冲突则跳过）
-- 用法：mysql -uroot -p ruoyi_lyjs < seed_national_risk_points.sql
-- =============================================================

-- （已移除硬编码 USE ruoyi_lyjs，库名由执行脚本的 -D 参数指定）

-- 先清之前那 6 条空 engineering_category 的脏数据
DELETE FROM biz_safety_risk_point WHERE engineering_category IS NULL OR engineering_category = '';

-- =============================================================
-- 1. 房屋建筑（house_）
-- =============================================================
INSERT INTO biz_safety_risk_point (risk_code, risk_name, area_name, engineering_category, gb_std, hazard_desc, accident_type, risk_level, control_level, measure_engine, measure_manage, measure_training, measure_ppe, measure_emergency) VALUES
('RF-HOUSE-001','高处坠落风险（临边洞口）','主体结构施工区','house_residential','JGJ59-2011 3.2','楼层临边、楼梯口、电梯井口未设防护栏杆或防护栏松动，作业人员易坠落','高处坠落','2','2','设置 1.2m 高双道防护栏杆，中杆 0.6m，立网封闭','班前检查防护设施，专人维护','高处作业专项安全教育，交底签字','安全帽、安全带、防滑鞋','立即停止作业，救治伤员，设置警戒隔离带'),
('RF-HOUSE-002','模板支撑体系失稳','模板支撑区','house_residential','JGJ59-2011 3.3','模板支撑架未按方案搭设，立杆间距过大、扫地杆缺失，浇筑时坍塌','坍塌','2','2','模板支撑按 JGJ130 专项方案搭设，立杆基础垫板','专项方案审批，搭设验收挂牌','模板工持证上岗，专项交底','安全帽','启动应急预案，警戒疏散'),
('RF-HOUSE-003','塔吊倾覆风险','塔吊作业区','house_public','GB5144-2012','塔吊基础沉降、附墙间距超规范、超载作业，导致倾覆','起重伤害','1','1','定期检测塔吊垂直度，附墙装置按方案安装','"十不吊"制度，专人指挥，持证上岗','塔吊司机信号工专项培训','安全帽、防滑鞋','紧急停工，切断电源，警戒隔离'),
('RF-HOUSE-004','深基坑坍塌','深基坑区域','house_factory','JGJ120-2012','基坑支护变形超限、未及时监测、周边堆载超载，导致坍塌','坍塌','1','1','基坑支护设计计算，布设监测点实时监控','编制基坑专项方案，专家论证','基坑作业专项安全教育','安全帽、救生衣','启动基坑应急预案，疏散人员'),
('RF-HOUSE-005','脚手架搭设不规范','外立面脚手架区','house_school','JGJ130-2011','脚手架立杆间距、步距超规范，连墙件设置不足，导致架体坍塌','坍塌','2','2','脚手架按专项方案搭设，连墙件按两步三跨设置','搭设前验收，使用中定期检查','架子工持证上岗，交底签字','安全帽、安全带','立即疏散，加固或拆除重建'),
('RF-HOUSE-006','施工用电触电','全场施工用电','house_hospital','JGJ46-2005','配电箱无漏电保护、私拉乱接电线、潮湿作业未用安全电压，导致触电','触电','1','2','TN-S 三相五线制，三级配电两级保护','电工持证，定期检查，配电箱上锁','全员安全用电教育','绝缘鞋、绝缘手套','立即断电，心肺复苏急救'),

-- =============================================================
-- 2. 市政工程（municipal_）
-- =============================================================
('RF-MUNI-001','道路施工交通导行','市政道路作业区','municipal_road','CJJ1-2008 10.1','交通导行标志不全、信号灯失效、作业人员未穿反光服，车辆撞击','车辆伤害','2','2','设置连续封闭围挡，夜间警示灯','专人交通指挥，导行方案审批','全员交通安全教育','反光背心、安全帽','立即救治伤员，保留现场'),
('RF-MUNI-002','顶管施工坍塌','顶管作业区','municipal_tunnel','GB50108-2008 7.3','顶管工作井支护失效、正面土体失稳，导致坍塌','坍塌','1','1','工作井专项设计，降水监测，及时支护','编制顶管专项方案，专家论证','特种作业人员持证','安全帽、逃生面具','启动顶管应急预案，紧急逃生'),
('RF-MUNI-003','燃气管道破坏','燃气管道保护区域','municipal_water','CJJ33-2005','不明地下管线位置野蛮施工，挖掘机挖破燃气管道','爆炸','1','1','施工前管线探测，人工开挖探槽','燃气管线保护方案，专人监护','全员管线保护教育','防毒面具、防爆工具','立即疏散，禁明火，通知燃气公司'),
('RF-MUNI-004','桥梁挂篮悬浇失稳','桥梁挂篮作业区','municipal_bridge','JTG/T 3650-2020 8','挂篮锚固不牢、前托梁超载，挂篮坠落','高处坠落','1','1','挂篮专项设计计算，加载试验','挂篮安装验收，施工方案审批','挂篮作业专项交底','安全帽、安全带','立即停工，疏散人员，加固检查'),
('RF-MUNI-005','有限空间作业中毒','污水处理池/泵站','municipal_sewage','AQ 7005-2007','未经检测进入有限空间，有毒有害气体积聚，人员中毒窒息','中毒窒息','1','1','作业前气体检测，强制通风，监护人员值守','有限空间作业许可制度','专项安全教育','防毒面具、安全带、救生索','启动有限空间应急预案，专业救援'),

-- =============================================================
-- 3. 机电安装（mep_）
-- =============================================================
('RF-MEP-001','高处安装坠落','机电安装高处作业区','mep_building','JGJ80-2016 6.2','高处作业吊篮未验收、安全带低挂高用，作业人员坠落','高处坠落','1','2','吊篮安装验收挂牌，安全带高挂低用','吊篮作业专项方案，每日检查','吊篮作业专项教育','安全帽、安全带、防滑鞋','立即救治，加固吊篮或重新搭设脚手架'),
('RF-MEP-002','动火作业火灾','焊接动火区域','mep_fire','GB50235-2010 6.1.1','动火前未清理易燃物、无灭火器材、无人监护，引发火灾','火灾','2','2','动火前清理易燃物，配备灭火器材，看火人全程监护','动火作业票审批制度','全员动火作业安全教育','灭火器、防火毯','立即扑救，拨打119，疏散人员'),
('RF-MEP-003','管道试压爆裂','管道试压区域','mep_industrial','GB50236-2011 7.5','试压超设计压力、管道固定不牢，管道爆裂伤人','物体打击','2','2','试压方案编制，压力分级缓慢升压','试压作业票，试压前安全检查','管道试压专项交底','安全帽、防护眼镜','立即卸压，救治伤员，隔离现场'),
('RF-MEP-004','电力电缆敷设挤伤','电缆敷设区域','mep_electric','GB50168-2018 5.2','电缆沟内作业空间狭窄、照明不足、沟通不畅，人员挤伤','机械伤害','3','3','电缆沟强制通风，足够照明，专人指挥','电缆敷设安全交底','全员电缆敷设安全教育','安全帽、手套','立即救治，疏散人员，加强照明'),
('RF-MEP-005','通风空调吊装坠落','空调机组吊装区域','mep_hvac','GB50274-2010 8.1','吊装索具磨损、指挥不当，机组坠落伤人','起重伤害','1','2','吊装前索具检查，专项方案，专人指挥','吊装作业审批，十不吊制度','吊装作业专项教育','安全帽、防滑鞋','立即疏散，吊装警戒，救治伤员'),

-- =============================================================
-- 4. 装饰装修（deco_）
-- =============================================================
('RF-DECO-001','幕墙安装高处坠落','幕墙施工区','deco_curtain','JGJ80-2016 6.3','幕墙安装时操作平台不牢、安全带未挂设，人员坠落','高处坠落','1','1','操作平台验收，安全带高挂低用','幕墙专项方案，每日班前检查','幕墙作业专项教育','安全帽、安全带、防滑鞋','立即救治，加固平台'),
('RF-DECO-002','室内装饰粉尘爆炸','装饰切割打磨区','deco_indoor','GB15577-2018','木屑粉尘积聚、电气设备不防爆，引发爆炸','爆炸','1','2','湿式作业降尘，防爆电器，每日清理粉尘','粉尘爆炸危险性告知','全员粉尘安全教育','防尘口罩、护目镜','立即疏散，切断电源，拨打119'),
('RF-DECO-003','油漆作业中毒','油漆喷涂区域','deco_smart','GBZ2.1-2019 甲苯二甲苯','密闭空间喷漆无通风，挥发性气体积聚中毒','中毒','2','2','强制通风换气，佩戴防毒面具','喷漆作业许可制度','专项安全教育','防毒面具、防护服','立即通风，救治伤员，送医'),
('RF-DECO-004','石材干挂脱落','干挂石材区域','deco_curtain','GB50210-2018 8.2','干挂件锈蚀、膨胀螺栓松动，石材脱落砸人','物体打击','2','2','定期检查干挂件，防腐处理，螺栓定期紧固','干挂专项验收，日常巡检','干挂施工交底','安全帽','立即设置警戒，加固或更换'),

-- =============================================================
-- 5. 园林绿化（landscape_）
-- =============================================================
('RF-LAND-001','大型树木吊装坠落','苗木吊装区','landscape_park','LY/T 2622-2016','大树吊装索具选择不当、树冠未绑扎，树木坠落','起重伤害','2','2','大树吊装专项方案，索具检查，树冠绑扎','吊装作业审批，专人指挥','吊装安全教育','安全帽','立即疏散，隔离警戒，救治伤员'),
('RF-LAND-002','农药喷洒中毒','园林喷药区域','landscape_park','GBZ/T 195-2007','高温时段喷药、未戴防护，药液经皮肤吸收中毒','中毒','3','2','早晚低温时段喷药，个人防护，划定警戒区','农药使用登记制度','农药安全使用教育','防护服、防毒口罩、手套','立即脱离现场，脱去污染衣物'),
('RF-LAND-003','绿化机械伤害','绿篱机/剪草机作业区','landscape_road','GB7251-2013','手持绿篱机操作不当、长发卷入、机械无防护罩','机械伤害','3','3','机械完好率检查，操作规范，防护罩齐全','机械定期保养制度','机械操作安全教育','防护眼镜、劳保手套','立即停机，救治伤员'),
('RF-LAND-004','生态修复边坡失稳','生态修复边坡区域','landscape_eco','GB50330-2013 5.2','边坡坡度太陡、排水不畅、植被未覆盖，边坡滑坡','坍塌','2','2','边坡分级放坡，土钉锚喷支护，完善排水','边坡专项设计，监测预警','边坡作业安全教育','安全帽','立即警戒，转移人员，启动应急预案'),

-- =============================================================
-- 6. 基础设施（infra_）
-- =============================================================
('RF-INFRA-001','电力设施触电','高压电力设施附近','infra_power','GB50059-2011 4.1','施工机械与高压带电体安全距离不足，感应电或直接触电','触电','1','1','电力设施专项保护方案，划定安全距离，专人监护','电力设施保护许可制度','全员电力安全教育','绝缘鞋、安全帽','立即断电，心肺复苏急救'),
('RF-INFRA-002','水利围堰漫顶','水利围堰区域','infra_water','SL 398-2007 5.2','围堰设计标准偏低、持续暴雨、泄洪不及时，漫顶溃决','淹没','1','1','围堰专项设计，实时水情监测，预设泄洪方案','应急预案演练，专人值守','水利作业安全教育','救生衣','启动防汛应急预案，疏散人员'),
('RF-INFRA-003','交通设施吊装伤害','交通信号/监控吊装区','infra_transport','JTG D81-2017 8.2','高空吊装交通设施时索具断裂、指挥失误，物体坠落','起重伤害','2','2','吊装前索具检查，专项方案，现场警戒','吊装作业审批，十不吊','吊装安全教育','安全帽','立即设置警戒，救治伤员'),
('RF-INFRA-004','通讯管线挖断','通讯管线保护区','infra_comm','GB50373-2006 5.3','不明管线位置野蛮施工，挖掘机挖断光纤或电缆','设备损坏','2','2','施工前管线探测报告，人工开挖探槽，管线标识','管线保护方案审批，专人监护','管线保护安全教育','对讲机、标记带','立即停止，通知运营方抢修'),

-- =============================================================
-- 7. EPC总承包（epc_）
-- =============================================================
('RF-EPC-001','交叉作业伤害','EPC 综合施工现场','epc_general','JGJ80-2016 6.5','多专业交叉作业无协调，上下层同时施工，坠物伤人','物体打击','2','2','交叉作业协调会，划定作业隔离区，设监护人','每日施工协调会制度','全员交叉作业安全教育','安全帽','立即设置警戒，救治伤员'),
('RF-EPC-002','临时用电火灾','EPC 综合施工现场','epc_general','JGJ46-2005 4.2','私拉乱接电线、超负荷用电、配电箱未设阻燃措施，引发火灾','火灾','1','2','TN-S 三相五线制，三级配电两级保护，阻燃配电','电工持证，定期巡检','全员安全用电教育','灭火器、消防水带','立即扑救，拨打119，疏散'),
('RF-EPC-003','大型设备安装坠落','EPC 设备安装区','epc_general','GB50231-2009 5.1','大型设备吊装索具磨损、就位时临时支撑不牢，设备坠落','起重伤害','1','1','吊装专项方案，索具检查，就位时临时支撑加固','吊装作业审批，专人指挥','起重作业专项教育','安全帽、防滑鞋','立即疏散，隔离警戒，救治伤员'),
('RF-EPC-004','雨季施工坍塌','EPC 深基坑/管沟区','epc_general','JGJ120-2012 4.8','暴雨后基坑支护变形、管沟边坡失稳，坍塌伤人','坍塌','1','1','雨季基坑专项方案，加密监测，设置支撑','应急预案演练，24h 值班','雨季施工安全教育','安全帽、逃生绳','立即疏散，启动应急预案')

ON DUPLICATE KEY UPDATE risk_name = VALUES(risk_name), risk_level = VALUES(risk_level), create_time = VALUES(create_time);

-- =============================================================
-- 验证
-- =============================================================
SELECT '=== 插入后各工程类别数量 ===' AS info;
SELECT engineering_category, COUNT(*) AS cnt 
FROM biz_safety_risk_point 
WHERE del_flag = '0' AND engineering_category IS NOT NULL AND engineering_category != ''
GROUP BY engineering_category ORDER BY cnt DESC;

SELECT '=== 总条数 ===' AS info;
SELECT COUNT(*) AS total_risk_points FROM biz_safety_risk_point WHERE del_flag = '0';

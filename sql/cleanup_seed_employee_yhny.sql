-- =====================================================================
-- 清理前期测试员工数据（YHNY 适配版，从物理表删除）
-- 原版：cleanup_seed_employee.sql（LYJS 版，表清单写死 16 张 HR 表）
-- 差异：YHNY 生产库 edop_yhny（edop_full.sql 2026-11-11 基线）部分
--       biz_hr_* 表尚无 employee_id 列，写死 UNION 会报 ERROR 1054；
--       且 user_id 2~21 在 YHNY 可能是真实用户，不能按号段删除。
-- 适配：0.3 关联核对改为 information_schema 动态探测（只查本库实际
--       存在且含 employee_id 的表）；2.3 只删除与种子员工档案关联的
--       系统用户（不按号段）；3.1 序号复位改为表存在时才执行。
-- 使用前：请先执行"第 0 步：现状核对"，确认影响范围后再执行后续步骤
-- 兼容：MySQL 5.7+ / 8.0（全部使用 PREPARE，无 CTE / 无 DELIMITER）
-- =====================================================================

-- 动态拼接 GROUP_CONCAT 上限调大，防止表多时被截断
SET SESSION group_concat_max_len = 65535;

-- =====================================================================
-- 第 0 步：现状核对（只读，先执行确认影响范围）
-- =====================================================================

-- 0.1 核对种子数据现状：数量、是否重复、创建来源
SELECT employee_no, employee_name, org_id, user_id, create_by, create_time, remark
FROM biz_hr_employee
WHERE create_by = 'admin'
  AND remark LIKE '关联系统用户 user_id=%'
ORDER BY create_time, employee_no;

-- 0.2 核对重复工号（种子工号 EMP0001~EMP0008 可能重叠）
SELECT employee_no, COUNT(*) AS cnt, GROUP_CONCAT(employee_id) AS employee_ids
FROM biz_hr_employee
WHERE employee_no LIKE 'EMP%'
GROUP BY employee_no
HAVING COUNT(*) > 1;

-- 0.3 核对关联业务表引用（动态探测本库实际存在且含 employee_id 的表，
--     删除前必须确认没有业务数据挂在这些员工上）
SET @seed_where = 'create_by=''admin'' AND remark LIKE ''关联系统用户%''';
SELECT GROUP_CONCAT(
         CONCAT('SELECT ''', c.TABLE_NAME, ''' AS biz_table, COUNT(*) AS ref_cnt FROM `',
                c.TABLE_NAME, '` WHERE employee_id IN (SELECT employee_id FROM biz_hr_employee WHERE ', @seed_where, ')')
         SEPARATOR ' UNION ALL ')
  INTO @ref_sql
FROM information_schema.TABLES t
JOIN information_schema.COLUMNS c
  ON c.TABLE_SCHEMA = t.TABLE_SCHEMA AND c.TABLE_NAME = t.TABLE_NAME
WHERE t.TABLE_SCHEMA = DATABASE()
  AND t.TABLE_TYPE = 'BASE TABLE'
  AND c.COLUMN_NAME = 'employee_id'
  AND t.TABLE_NAME <> 'biz_hr_employee';

SET @ref_sql = IF(@ref_sql IS NULL OR @ref_sql = '',
                  'SELECT ''(本库无其他含 employee_id 的业务表)'' AS biz_table, 0 AS ref_cnt',
                  @ref_sql);
PREPARE st_ref FROM @ref_sql;
EXECUTE st_ref;
DEALLOCATE PREPARE st_ref;

-- 若任何 ref_cnt > 0：先清理对应业务表数据，或改用逻辑删除（status/del_flag），勿直接执行物理删除

-- =====================================================================
-- 第 1 步：备份（执行清理前必须先跑）
-- =====================================================================

CREATE TABLE IF NOT EXISTS biz_hr_employee_bak_20261114 AS
SELECT * FROM biz_hr_employee
WHERE create_by = 'admin'
  AND remark LIKE '关联系统用户 user_id=%';

-- 种子工号可能重叠，单独备份重复档案
CREATE TABLE IF NOT EXISTS biz_hr_employee_dup_bak_20261114 AS
SELECT e.* FROM biz_hr_employee e
WHERE e.employee_no LIKE 'EMP%'
  AND (SELECT COUNT(*) FROM biz_hr_employee e2 WHERE e2.employee_no = e.employee_no) > 1;

-- 备份与种子员工档案关联的系统用户（动态范围，不按号段）
CREATE TABLE IF NOT EXISTS sys_user_seed_bak_20261114 AS
SELECT u.* FROM sys_user u
WHERE u.create_by = 'admin'
  AND u.user_id IN (SELECT user_id FROM biz_hr_employee
                    WHERE create_by = 'admin' AND remark LIKE '关联系统用户 user_id=%');

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

-- 2.3 删除种子用户（仅删除仍与种子员工档案关联、且由 admin 创建的用户；
--     不按 user_id 号段删除，避免误删 YHNY 真实用户）
DELETE FROM sys_user_role
WHERE user_id IN (SELECT user_id FROM biz_hr_employee_bak_20261114 WHERE user_id IS NOT NULL);
DELETE FROM sys_user_post
WHERE user_id IN (SELECT user_id FROM biz_hr_employee_bak_20261114 WHERE user_id IS NOT NULL);
DELETE FROM sys_user
WHERE create_by = 'admin'
  AND user_id IN (SELECT user_id FROM biz_hr_employee_bak_20261114 WHERE user_id IS NOT NULL);

-- =====================================================================
-- 第 3 步：工号序号复位（清理后执行，否则新员工工号跳号）
-- =====================================================================

-- 3.1 仅当 mk_number_rule 表存在时才复位（YHNY 库可能无该表）
SET @has_rule = (SELECT COUNT(*) FROM information_schema.TABLES
                 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule');
SET @rule_sql = IF(@has_rule > 0,
  'UPDATE mk_number_rule SET current_seq = (SELECT COALESCE(MAX(CAST(SUBSTRING(employee_no, 4) AS UNSIGNED)), 0) FROM biz_hr_employee WHERE employee_no LIKE ''EMP%''), update_by = ''admin'' WHERE rule_code = ''hr_employee''',
  'SELECT ''(mk_number_rule 表不存在，跳过序号复位)'' AS notice');
PREPARE st_rule FROM @rule_sql;
EXECUTE st_rule;
DEALLOCATE PREPARE st_rule;

-- =====================================================================
-- 第 4 步：清理后核对（只读）
-- =====================================================================

-- 4.1 核对种子数据已清除
SELECT COUNT(*) AS seed_left FROM biz_hr_employee
WHERE create_by = 'admin' AND remark LIKE '关联系统用户 user_id=%';

-- 4.2 核对工号无重复
SELECT employee_no, COUNT(*) AS cnt FROM biz_hr_employee
WHERE employee_no LIKE 'EMP%' GROUP BY employee_no HAVING COUNT(*) > 1;

-- 4.3 核对序号规则接续正确（表存在时才有结果）
SELECT rule_code, prefix, seq_length, current_seq FROM mk_number_rule WHERE rule_code = 'hr_employee';

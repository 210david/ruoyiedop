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

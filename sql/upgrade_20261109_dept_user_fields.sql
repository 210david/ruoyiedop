-- ============================================================
-- 升级脚本: upgrade_20261109_dept_user_fields.sql
-- 主题: 部门表字段调整(去掉联系电话/邮箱, 增加部门简称/部门编码/分管领导/在职人员数量)
--       人员表增加上级领导/分管领导(人员选择)
-- 幂等: 可重复执行
-- 目标库: ruoyi_lyjs
-- ============================================================

-- ---------- 1. sys_dept 新增字段 ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'dept_short_name');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column dept_short_name varchar(50) default null comment ''部门简称'' after leader',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'dept_code');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column dept_code varchar(50) default null comment ''部门编码'' after dept_short_name',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

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

-- ---------- 2. sys_dept 删除联系电话/邮箱 ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'phone');
set @sql := if(@col_exists > 0,
    'alter table sys_dept drop column phone',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'email');
set @sql := if(@col_exists > 0,
    'alter table sys_dept drop column email',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 3. sys_user 新增上级领导/分管领导 ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'superior_leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column superior_leader_id bigint default null comment ''上级领导(人员ID,关联sys_user.user_id)'' after remark',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'charge_leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column charge_leader_id bigint default null comment ''分管领导(人员ID,关联sys_user.user_id)'' after superior_leader_id',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 4. 回填 sys_dept.staff_count(在职人员数量 = 在职用户数) ----------
update sys_dept d
set d.staff_count = (
    select count(*) from sys_user u
    where u.dept_id = d.dept_id and u.status = '0' and u.del_flag = '0'
);

-- =============================================================
-- 升级脚本: upgrade_20261114c_sys_dept_cols.sql
-- 主题: sys_dept 补列（幂等）——修复生产选人弹框/系统用户部门树报错
-- 现象: 点击系统用户或任何"选择人员"弹框，左侧组织机构树显示
--       "暂无数据"并提示"系统繁忙"。根因: SysDeptMapper.selectDeptVo
--       查询 leader_id / dept_short_name / dept_code / charge_leader_id /
--       staff_count，生产 sys_dept 缺列导致 SQL 报错。
-- 内容:
--   1. sys_dept 幂等补列 5 个（information_schema 判断，缺才加）
--      leader_id / dept_short_name / dept_code / charge_leader_id / staff_count
--   2. leader_id 按领导姓名回填（幂等: 仅 leader_id 为空的行）
--   3. staff_count 按在职用户数回填（幂等: 全量重算）
-- 幂等: 可重复执行，结果一致。不修改/删除任何已有数据行。
-- 适用库: edop_cfsp
-- 执行: mysql -uroot -p --default-character-set=utf8mb4 edop_cfsp < 本文件
-- =============================================================

-- ---------- 1.1 leader_id ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column leader_id bigint default null comment ''部门领导(人员ID,关联sys_user.user_id)'' after leader',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 1.2 dept_short_name ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'dept_short_name');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column dept_short_name varchar(50) default null comment ''部门简称'' after leader',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 1.3 dept_code ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'dept_code');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column dept_code varchar(50) default null comment ''部门编码'' after dept_short_name',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 1.4 charge_leader_id ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'charge_leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column charge_leader_id bigint default null comment ''分管领导(人员ID,关联sys_user.user_id)'' after dept_code',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 1.5 staff_count ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'staff_count');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column staff_count int default null comment ''在职人员数量'' after charge_leader_id',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 2. leader_id 按领导姓名回填（幂等） ----------
update sys_dept d
set d.leader_id = (
    select u.user_id from sys_user u
    where u.nick_name = d.leader and u.del_flag = '0'
    order by u.user_id limit 1
)
where d.leader_id is null
  and d.leader is not null and d.leader <> '';

-- ---------- 3. staff_count 按在职用户数回填（幂等，全量重算） ----------
update sys_dept d
set d.staff_count = (
    select count(*) from sys_user u
    where u.dept_id = d.dept_id and u.del_flag = '0' and u.status = '0'
)
where d.staff_count is null
   or d.staff_count <> (
    select count(*) from sys_user u
    where u.dept_id = d.dept_id and u.del_flag = '0' and u.status = '0'
);

-- ---------- 自检（只读） ----------
-- 1) 5 列应全部存在
SELECT COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_dept'
   AND COLUMN_NAME IN ('leader_id','dept_short_name','dept_code','charge_leader_id','staff_count')
 ORDER BY ORDINAL_POSITION;
-- 2) 部门树查询应能执行（等价于 deptTree 的基础查询）
SELECT COUNT(*) AS dept_tree_ok FROM sys_dept WHERE del_flag = '0';

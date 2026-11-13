-- ============================================================
-- 升级脚本: upgrade_20261110_dept_leader_id.sql
-- 主题: 部门表「负责人」调整为「部门领导」, 增加 leader_id(人员ID) 用于回显与同步到人员
-- 幂等: 可重复执行
-- 目标库: ry-vue
-- ============================================================

-- ---------- 1. sys_dept 新增 leader_id ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column leader_id bigint default null comment ''部门领导(人员ID,关联sys_user.user_id)'' after leader',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 2. 按姓名回填 leader_id ----------
update sys_dept d
set d.leader_id = (
    select u.user_id from sys_user u
    where u.nick_name = d.leader and u.del_flag = '0'
    order by u.user_id limit 1
)
where d.leader_id is null
  and d.leader is not null and d.leader <> '';

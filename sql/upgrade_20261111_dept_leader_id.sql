-- ============================================================
-- 升级脚本: upgrade_20261111_dept_leader_id.sql
-- 主题: 部门表「负责人」调整为「部门领导」, 增加 leader_id(人员ID) 用于回显与同步到人员
--       (配套后端 ba5a7c8c, 2026-11-11 16:45 构建的 edop-yhny-v10.jar)
-- 幂等: 可重复执行
-- 目标库: edop_yhny
-- ============================================================

USE edop_yhny;

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

-- ---------- 3. 校验 ----------
SELECT 'LEADER_ID_COL' tag, COUNT(*) cnt FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'sys_dept' AND column_name = 'leader_id';
SELECT 'LEADER_BACKFILLED' tag, COUNT(*) cnt FROM sys_dept WHERE leader_id IS NOT NULL;

-- 回退: ALTER TABLE sys_dept DROP COLUMN leader_id;

-- ---------- 4. 危化品台账菜单path改名(修复与mms物料管理路由名Material冲突导致的404) ----------
-- 冲突: menu 4666(危化品台账, path=material) 与 mms物料管理(path=material) 生成的路由名同为 Material,
--       后注册者顶掉先注册者, 导致 /safety/hazmat/material 404。改名后路由名 HazmatMaterial 唯一。
UPDATE sys_menu SET path = 'hazmatMaterial'
WHERE menu_id = 4666 AND path = 'material';

-- 校验: 应返回 hazmatMaterial
SELECT 'HAZMAT_PATH' tag, path FROM sys_menu WHERE menu_id = 4666;

-- ---------- 5. 隐藏两个幽灵菜单(视图文件不存在, 点击即404) ----------
-- menu 4808 质量目标(qms/target/index): 后端控制器存在但前端视图从未入库
-- menu 5214 产能负荷(mms/capacity/index): 前后端均未实现, 生产库本已 visible=1(隐藏)
-- 处理: 统一 visible='1'(隐藏), 待后续开发页面后再放开
UPDATE sys_menu SET visible = '1', remark = CONCAT(IFNULL(remark,''), '[2026-11-11隐藏: 视图未实现]')
WHERE menu_id IN (4808, 5214) AND visible = '0';

-- 校验: 两菜单 visible 均应为 1
SELECT 'GHOST_HIDDEN' tag, menu_id, menu_name, visible FROM sys_menu WHERE menu_id IN (4808, 5214);

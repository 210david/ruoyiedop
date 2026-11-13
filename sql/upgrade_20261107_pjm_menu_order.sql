-- ============================================================
-- 升级脚本：项目管理菜单调整（隐藏重复菜单 + 报表类移至最后）
-- 日期：2026-11-07
-- 背景：
--   1) "项目附件"(906603) 与 "项目资料"(9062239) 完全重复（path 均 attachment、component 均 pjm/attachment/index），
--      同一页面挂了两个菜单——隐藏"项目附件"（visible='1'，不删除，角色授权数据保留可随时恢复）。
--   2) "项目采购台账"(906229) 与 "项目看板"(906230) 属报表类，应排业务菜单之后；
--      原有 order_num 存在重复（2/2、6/6、7/7），一并整理为唯一递增。
-- 适用库：ruoyi_lyjs（本地开发库与客户库通用，可重复执行）
-- 注意：RuoYi getRouters 实时查库，刷新页面即生效，无需重启。
-- ============================================================

-- 回滚语句（如需还原时执行）：
-- UPDATE sys_menu SET visible='0' WHERE menu_id=906603;
-- UPDATE sys_menu SET order_num=1  WHERE menu_id=906203;  -- 项目立项
-- UPDATE sys_menu SET order_num=2  WHERE menu_id=906214;  -- WBS任务分解
-- UPDATE sys_menu SET order_num=2  WHERE menu_id=9062239; -- 项目资料
-- UPDATE sys_menu SET order_num=3  WHERE menu_id=906215;  -- 项目团队
-- UPDATE sys_menu SET order_num=5  WHERE menu_id=906223;  -- 变更管理
-- UPDATE sys_menu SET order_num=6  WHERE menu_id=906229;  -- 项目采购台账
-- UPDATE sys_menu SET order_num=6  WHERE menu_id=906601;  -- 里程碑管理
-- UPDATE sys_menu SET order_num=7  WHERE menu_id=906230;  -- 项目看板
-- UPDATE sys_menu SET order_num=7  WHERE menu_id=906602;  -- 项目付款
-- UPDATE sys_menu SET order_num=8  WHERE menu_id=906603;  -- 项目附件
-- UPDATE sys_menu SET order_num=9  WHERE menu_id=906604;  -- 工时填报
-- UPDATE sys_menu SET menu_name='项目付款' WHERE menu_id=906602;  -- 项目回款（如需还原名称）

-- 1) 隐藏重复菜单"项目附件"（与"项目资料"同一页面 pjm/attachment/index）
UPDATE sys_menu SET visible='1' WHERE menu_id=906603;

-- 1.1) 菜单更名："项目付款" → "项目回款"（收款明细页面口径统一为"回款"）
UPDATE sys_menu SET menu_name='项目回款' WHERE menu_id=906602;

-- 2) 业务菜单在前（按原相对次序，消除 order_num 重复）
UPDATE sys_menu SET order_num=1  WHERE menu_id=906203;   -- 项目立项
UPDATE sys_menu SET order_num=2  WHERE menu_id=906214;   -- WBS任务分解
UPDATE sys_menu SET order_num=3  WHERE menu_id=9062239;  -- 项目资料
UPDATE sys_menu SET order_num=4  WHERE menu_id=906215;   -- 项目团队
UPDATE sys_menu SET order_num=5  WHERE menu_id=906223;   -- 变更管理
UPDATE sys_menu SET order_num=6  WHERE menu_id=906601;   -- 里程碑管理
UPDATE sys_menu SET order_num=7  WHERE menu_id=906602;   -- 项目付款
UPDATE sys_menu SET order_num=8  WHERE menu_id=906604;   -- 工时填报

-- 3) 报表类移至最后
UPDATE sys_menu SET order_num=9  WHERE menu_id=906229;   -- 项目采购台账（报表）
UPDATE sys_menu SET order_num=10 WHERE menu_id=906230;   -- 项目看板（报表）
UPDATE sys_menu SET order_num=11 WHERE menu_id=906603;   -- 项目附件（已隐藏）

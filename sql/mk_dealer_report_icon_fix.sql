-- =============================================
-- 修复：经销商专项报表菜单无图标
-- 根因：sys_menu.icon='report'，但前端 ruoyi-ui/src/assets/icons/svg/ 下没有 report.svg，
--       SvgIcon 渲染 #icon-report 引用未定义，侧边栏显示空白。
-- 处理：改用项目报表类通用图标 documentation（前端已有该 svg，全库 24 处报表菜单在用）。
-- 影响 2 条指向同一页面的入口：
--   4215     决策支持 > 统计分析 > 营销管理 > 经销商专项报表
--   9063046  营销管理 > 数据分析 > 经销商专项报表（Legacy 入口）
-- 仅改 icon，menu_id / path / component / route_name / perms / 角色授权均不变，
--   后端无需重启，刷新页面即生效。
-- 幂等：按菜单名 + 组件 + 旧图标定位，可重复执行。
-- =============================================

UPDATE sys_menu
SET icon        = 'documentation',
    update_by   = 'admin',
    update_time = sysdate()
WHERE menu_name = '经销商专项报表'
  AND menu_type = 'C'
  AND component = 'mk/dealerReport/index'
  AND (icon = 'report' OR icon IS NULL OR icon = '');

-- 复核：应返回 2 条，icon 均为 documentation
SELECT menu_id, menu_name, parent_id, icon, component, route_name
FROM sys_menu
WHERE component = 'mk/dealerReport/index' AND menu_type = 'C'
ORDER BY menu_id;

-- 回滚（如需恢复原图标名，须同时补齐前端 report.svg）：
-- UPDATE sys_menu SET icon='report' WHERE menu_name='经销商专项报表'
--   AND menu_type='C' AND component='mk/dealerReport/index';

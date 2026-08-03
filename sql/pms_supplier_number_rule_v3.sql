-- =============================================
-- 清理之前创建的供应商编号规则菜单
-- 目标：供应商编号规则统一到采购管理/编号规则(4462)页面管理
-- =============================================

-- 获取采购管理顶级目录ID
SET @pmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);

-- =============================================
-- 1. 删除供应商管理系统目录(4472)及其所有子菜单
-- =============================================
SET @supplierSystemId = (SELECT menu_id FROM sys_menu WHERE menu_name='供应商管理系统' AND parent_id=@pmsParentId LIMIT 1);

-- 删除供应商管理系统下的编号规则按钮权限
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM sys_menu WHERE parent_id IN (
    SELECT menu_id FROM sys_menu WHERE parent_id=@supplierSystemId AND menu_name='编号规则'
  )
);
-- 删除供应商管理系统下的编号规则菜单
DELETE FROM sys_menu WHERE parent_id IN (
  SELECT t.menu_id FROM (
    SELECT menu_id FROM sys_menu WHERE parent_id=@supplierSystemId AND menu_name='编号规则'
  ) t
);
-- 删除供应商管理系统下的编号规则菜单本身
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM sys_menu WHERE parent_id=@supplierSystemId AND menu_name='编号规则'
);
DELETE FROM sys_menu WHERE parent_id=@supplierSystemId AND menu_name='编号规则';

-- 删除供应商管理系统目录的权限
DELETE FROM sys_role_menu WHERE menu_id = @supplierSystemId;
-- 删除供应商管理系统目录
DELETE FROM sys_menu WHERE menu_id = @supplierSystemId;

-- =============================================
-- 2. 删除之前错误创建在供应商管理子目录下的编号规则菜单
-- =============================================
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id=4392
);
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM sys_menu WHERE parent_id IN (
    SELECT menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id=4392
  )
);
DELETE FROM sys_menu WHERE parent_id IN (
  SELECT t.menu_id FROM (
    SELECT menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id=4392
  ) t
);
DELETE FROM sys_menu WHERE menu_name='编号规则' AND parent_id=4392;

-- =============================================
-- 3. 更新采购管理/编号规则(4462)的query参数，支持查看所有采购相关编号规则
-- =============================================
UPDATE sys_menu SET 
  query = '{"module":"pms"}',
  remark = '采购管理编号规则（包含采购申请、采购订单、供应商等）'
WHERE menu_id = 4462;

-- 验证清理结果
SELECT '清理完成' AS result;
SELECT menu_id, menu_name, parent_id, menu_type, path, component, query, remark 
FROM sys_menu 
WHERE menu_id = 4462;
SELECT '采购管理下的编号规则菜单' AS info;
SELECT menu_id, menu_name, parent_id, path, query FROM sys_menu 
WHERE parent_id=@pmsParentId AND menu_name='编号规则';

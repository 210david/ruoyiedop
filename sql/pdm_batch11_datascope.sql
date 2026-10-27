-- ============================================================
-- 产品设计模块数据权限配置（pdm_batch11）
-- 1. 产品档案/图纸文件/设计BOM 三表补 dept_id（方案/变更单已有）
-- 2. 存量数据按创建人部门回填（无匹配回退 admin 部门）
-- 3. 设计工程师数据范围修正：5 仅本人 → 4 本部门及以下
--    （DataScopeAspect 无 userAlias 时"仅本人"会查空数据）
-- 配套代码：三实体 deptId 字段、Mapper XML dataScope、Service @DataScope
-- ============================================================

ALTER TABLE pdm_product ADD COLUMN dept_id BIGINT NULL COMMENT '归属部门（数据权限）' AFTER del_flag;
ALTER TABLE pdm_design_file ADD COLUMN dept_id BIGINT NULL COMMENT '归属部门（数据权限）' AFTER del_flag;
ALTER TABLE pdm_design_bom ADD COLUMN dept_id BIGINT NULL COMMENT '归属部门（数据权限）' AFTER del_flag;

-- 存量回填（pdm 表 create_by 为 utf8mb4_0900_ai_ci，sys_user.user_name 为 general_ci，需显式转换）
UPDATE pdm_product p LEFT JOIN sys_user u ON p.create_by = CONVERT(u.user_name USING utf8mb4) COLLATE utf8mb4_0900_ai_ci
  LEFT JOIN sys_user a ON 'admin' = CONVERT(a.user_name USING utf8mb4) COLLATE utf8mb4_0900_ai_ci
  SET p.dept_id = COALESCE(u.dept_id, a.dept_id) WHERE p.dept_id IS NULL;
UPDATE pdm_design_file p LEFT JOIN sys_user u ON p.create_by = CONVERT(u.user_name USING utf8mb4) COLLATE utf8mb4_0900_ai_ci
  LEFT JOIN sys_user a ON 'admin' = CONVERT(a.user_name USING utf8mb4) COLLATE utf8mb4_0900_ai_ci
  SET p.dept_id = COALESCE(u.dept_id, a.dept_id) WHERE p.dept_id IS NULL;
UPDATE pdm_design_bom p LEFT JOIN sys_user u ON p.create_by = CONVERT(u.user_name USING utf8mb4) COLLATE utf8mb4_0900_ai_ci
  LEFT JOIN sys_user a ON 'admin' = CONVERT(a.user_name USING utf8mb4) COLLATE utf8mb4_0900_ai_ci
  SET p.dept_id = COALESCE(u.dept_id, a.dept_id) WHERE p.dept_id IS NULL;

-- 方案/变更单表已有 dept_id 列，但存量数据为 NULL（早期测试数据未落归属），同样回填
UPDATE pdm_scheme p LEFT JOIN sys_user u ON p.create_by = CONVERT(u.user_name USING utf8mb4) COLLATE utf8mb4_0900_ai_ci
  LEFT JOIN sys_user a ON 'admin' = CONVERT(a.user_name USING utf8mb4) COLLATE utf8mb4_0900_ai_ci
  SET p.dept_id = COALESCE(u.dept_id, a.dept_id) WHERE p.dept_id IS NULL;
UPDATE pdm_change_order p LEFT JOIN sys_user u ON p.create_by = CONVERT(u.user_name USING utf8mb4) COLLATE utf8mb4_0900_ai_ci
  LEFT JOIN sys_user a ON 'admin' = CONVERT(a.user_name USING utf8mb4) COLLATE utf8mb4_0900_ai_ci
  SET p.dept_id = COALESCE(u.dept_id, a.dept_id) WHERE p.dept_id IS NULL;

-- 设计工程师数据范围修正
UPDATE sys_role SET data_scope = '4' WHERE role_key = 'pdm_engineer';

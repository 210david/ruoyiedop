-- ============================================================
-- 财务模块 · 角色分类字典补充
-- 执行前提：sys_role_category 字典已存在（工程既有，8 项）
--
-- 【背景】财务模块新增 3 个角色（fin_manager / fin_accountant / fin_cashier），
--         需在角色分类字典中登记「财务管理」，否则角色管理页无法按模块筛选。
--
-- 【已获批】2026-10-08 项目负责人确认，允许新增 fin 模块映射。
-- 【零侵入】仅 INSERT 新字典项，不 UPDATE/DELETE 任何既有字典项。
--
-- 幂等：WHERE NOT EXISTS，可重复执行
-- ============================================================

SET NAMES utf8mb4;

-- 字典项「财务管理」，dict_value=fin
INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
     is_default, status, create_by, create_time, remark)
SELECT 9, '财务管理', 'fin', 'sys_role_category', '', 'primary',
       'N', '0', 'admin', sysdate(),
       '财务模块角色分类（2026-10-08 新增，ruoyi-fin）'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data
    WHERE dict_type = 'sys_role_category' AND dict_value = 'fin'
);

-- ------------------------------------------------------------
-- 验证：确认 9 项（原 8 项 + fin），既有项未被改动
-- ------------------------------------------------------------
SELECT dict_sort, dict_label, dict_value, status
FROM sys_dict_data
WHERE dict_type = 'sys_role_category'
ORDER BY dict_sort;
-- 期望：9 行，dict_sort 1-8 为原有序项，9=财务管理/fin
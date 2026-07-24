-- =====================================================
-- 1. 删除刚才误建的"信息技术部"
-- 2. 将 admin 从任何部门移出，dept_id 设为 NULL
-- =====================================================

-- 1. 删除"信息技术部"（如果已创建）
DELETE FROM sys_dept WHERE dept_name = '信息技术部' AND del_flag = '0';

-- 2. admin 不归属任何部门
UPDATE sys_user SET dept_id = NULL, remark = '系统超级管理员（不归属任何部门）' WHERE user_id = 1;

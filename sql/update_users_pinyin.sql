-- =====================================================
-- 1. 修改非管理员用户名为姓名全拼
-- 2. 创建总经理角色
-- 3. 综合管理部新增3人（含总经理）
-- 4. 补全各部门负责人角色
-- =====================================================

-- ----------------------------
-- 1. 修改用户名为全拼（管理员除外）
-- ----------------------------
UPDATE sys_user SET user_name = 'lihua' WHERE user_name = 'ry' AND del_flag = '0';
UPDATE sys_user SET user_name = 'zhangtao' WHERE user_name = 'zt' AND del_flag = '0';

-- ----------------------------
-- 2. 创建总经理角色（role_id=104）
-- ----------------------------
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 104, '总经理', 'general_manager', 1, 1, 1, 1, '0', '0', 'admin', NOW(), '总经理角色：系统全模块查看与管理权限'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_id = 104);

-- 给总经理角色分配所有菜单权限（与超级管理员同权限范围）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 104, menu_id FROM sys_menu WHERE menu_id NOT IN (
    SELECT menu_id FROM sys_role_menu WHERE role_id = 104
);

-- ----------------------------
-- 3. 综合管理部新增3人（含总经理）
-- ----------------------------
SET @d_admin = (SELECT dept_id FROM sys_dept WHERE dept_name = '综合管理部' AND del_flag = '0' LIMIT 1);

INSERT INTO sys_user (dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, create_by, create_time, remark)
SELECT @d_admin, v.user_name, v.nick_name, '00', v.email, v.phonenumber, v.sex, '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', NOW(), v.remark
FROM (
  SELECT 'yanghua'  AS user_name, '杨华'  AS nick_name, 'yanghua@dt.com'  AS email, '13800003004' AS phonenumber, '0' AS sex, '总经理'     AS remark
  UNION ALL SELECT 'hejing',    '何静',    'hejing@dt.com',    '13800003005',          '1',          '行政文员'
  UNION ALL SELECT 'luoqiang',  '罗强',    'luoqiang@dt.com',  '13800003006',          '0',          '后勤主管'
) v
WHERE @d_admin IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name = v.user_name AND del_flag = '0');

-- 总经理角色分配
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 104 FROM sys_user u WHERE u.user_name = 'yanghua' AND u.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.user_id AND ur.role_id = 104);

-- 后勤主管 → 部门负责人
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 102 FROM sys_user u WHERE u.user_name = 'luoqiang' AND u.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.user_id AND ur.role_id = 102);

-- 行政文员 → 普通员工
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 100 FROM sys_user u WHERE u.user_name = 'hejing' AND u.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.user_id AND ur.role_id = 100);

SET @d_admin = NULL;

-- ----------------------------
-- 4. 补全部门负责人（研发部、市场部）
-- ----------------------------
-- 研发部：王伟 → 部门负责人
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 102 FROM sys_user u WHERE u.user_name = 'wangwei' AND u.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.user_id AND ur.role_id = 102);

-- 市场部：周伟 → 部门负责人
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 102 FROM sys_user u WHERE u.user_name = 'zhouwei' AND u.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.user_id AND ur.role_id = 102);

-- ----------------------------
-- 5. 同步更新 admin 用户备注中的引用（如有）
-- ----------------------------
-- 无需额外操作，admin 不变

-- =====================================================
-- 预置业务角色模板
-- 说明：为不同业务场景预置角色及菜单权限，插入前检查是否已存在
-- =====================================================

-- 1. 普通员工角色（个人中心、公告查看、基础查询）
INSERT IGNORE INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES (100, '普通员工', 'common_user', 3, '5', 1, 1, '0', '0', 'admin', sysdate(), '普通员工角色模板：个人中心、公告查看');

-- 普通员工 - 菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(100, 107),  -- 通知公告
(100, 1035), -- 公告查询
(100, 1036); -- 公告新增

-- 2. 财务人员角色（财务相关菜单权限）
INSERT IGNORE INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES (101, '财务人员', 'finance', 4, '3', 1, 1, '0', '0', 'admin', sysdate(), '财务人员角色模板：财务相关菜单权限');

-- 财务人员 - 菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(101, 105),  -- 字典管理
(101, 1025), -- 字典查询
(101, 106),  -- 参数设置
(101, 1030), -- 参数查询
(101, 107),  -- 通知公告
(101, 1035), -- 公告查询
(101, 1036), -- 公告新增
(101, 500),  -- 操作日志
(101, 1039), -- 操作查询
(101, 1041); -- 日志导出

-- 3. 部门负责人角色（部门管理、人员查看）
INSERT IGNORE INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES (102, '部门负责人', 'dept_leader', 5, '4', 1, 1, '0', '0', 'admin', sysdate(), '部门负责人角色模板：部门管理、人员查看');

-- 部门负责人 - 菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(102, 1),    -- 系统管理（目录）
(102, 100),  -- 用户管理
(102, 1000), -- 用户查询
(102, 1004), -- 用户导出
(102, 103),  -- 部门管理
(102, 1016), -- 部门查询
(102, 1017), -- 部门新增
(102, 1018), -- 部门修改
(102, 1019), -- 部门删除
(102, 104),  -- 岗位管理
(102, 1020), -- 岗位查询
(102, 1024), -- 岗位导出
(102, 107),  -- 通知公告
(102, 1035), -- 公告查询
(102, 1036), -- 公告新增
(102, 500),  -- 操作日志
(102, 1039), -- 操作查询
(102, 1041); -- 日志导出

-- 4. 业务经理角色（业务相关权限）
INSERT IGNORE INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES (103, '业务经理', 'business_manager', 6, '2', 1, 1, '0', '0', 'admin', sysdate(), '业务经理角色模板：业务相关权限');

-- 业务经理 - 菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(103, 1),    -- 系统管理（目录）
(103, 100),  -- 用户管理
(103, 1000), -- 用户查询
(103, 1001), -- 用户新增
(103, 1002), -- 用户修改
(103, 1004), -- 用户导出
(103, 101),  -- 角色管理
(103, 1007), -- 角色查询
(103, 103),  -- 部门管理
(103, 1016), -- 部门查询
(103, 104),  -- 岗位管理
(103, 1020), -- 岗位查询
(103, 105),  -- 字典管理
(103, 1025), -- 字典查询
(103, 107),  -- 通知公告
(103, 1035), -- 公告查询
(103, 1036), -- 公告新增
(103, 1037), -- 公告修改
(103, 1038), -- 公告删除
(103, 108),  -- 日志管理（目录）
(103, 500),  -- 操作日志
(103, 1039), -- 操作查询
(103, 1040), -- 操作删除
(103, 1041), -- 日志导出
(103, 501),  -- 登录日志
(103, 1042), -- 登录查询
(103, 1044); -- 日志导出

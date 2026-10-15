-- ============================================================
-- 传丰组织架构初始化：账号创建 + admin 密码统一
-- 执行日期：2026-10-13
-- 密码统一为 cf@123456（BCrypt）
-- ============================================================

-- 1. admin 密码改为 cf@123456
UPDATE sys_user SET password='$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2' WHERE user_id=1;

-- 2. 创建15个岗位账号，密码统一 cf@123456
INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, create_by, create_time, remark) VALUES
(185, 100, 'wuchuanfeng', '吴传丰', '00', '', '', '0', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号'),
(186, 105, 'lihua',       '李华',   '00', '', '', '0', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号'),
(187, 101, 'caoyan',      '曹燕',   '00', '', '', '1', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号'),
(188, 101, 'xiebin',      '谢斌',   '00', '', '', '0', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号'),
(189, 104, 'zhangmin',    '张敏',   '00', '', '', '1', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号'),
(190, 104, 'zhouwei',     '周伟',   '00', '', '', '0', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号'),
(191, 201, 'wuhao',       '吴昊',   '00', '', '', '0', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号'),
(192, 202, 'zhaolei',     '赵磊',   '00', '', '', '0', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号'),
(193, 200, 'majie',       '马杰',   '00', '', '', '0', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号'),
(194, 200, 'tangyu',      '唐宇',   '00', '', '', '0', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号'),
(195, 200, 'wangwei',     '王伟',   '00', '', '', '0', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号'),
(196, 200, 'liuyang',     '刘洋',   '00', '', '', '0', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号'),
(197, 105, 'wuxia',       '吴霞',   '00', '', '', '1', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号'),
(198, 105, 'zhengyu',     '郑宇',   '00', '', '', '0', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号'),
(199, 105, 'sunjing',     '孙静',   '00', '', '', '1', NULL, '$2a$10$C0elL6UPdAwwkyqkX45BKO0KgbOBznEIlmvJRl7KaXD1QHVVkFYL2', '0', '0', '', NULL, 'admin', NOW(), '组织架构初始化账号');

-- 3. 角色绑定
INSERT INTO sys_user_role (user_id, role_id) VALUES
(185, 104),
(186, 102), (186, 103),
(187, 122),
(188, 120),
(189, 302), (190, 302),
(191, 400), (191, 401), (191, 402), (191, 403),
(192, 502),
(193, 602),
(194, 601),
(195, 603), (196, 603),
(197, 100), (198, 100), (199, 100);

-- 4. 验证
SELECT u.user_id, u.user_name, u.nick_name, d.dept_name, GROUP_CONCAT(r.role_name ORDER BY r.role_id SEPARATOR ',') AS roles
FROM sys_user u
JOIN sys_dept d ON u.dept_id = d.dept_id
LEFT JOIN sys_user_role ur ON u.user_id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.role_id
WHERE u.del_flag = '0'
GROUP BY u.user_id, u.user_name, u.nick_name, d.dept_name
ORDER BY u.user_id;

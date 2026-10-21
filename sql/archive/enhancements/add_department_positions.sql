-- =====================================================
-- 补充完善人员岗位信息
-- 每个部门一个部门负责人，公司一个总经理在综合管理部
-- 系统管理员(admin)不分配任何岗位
-- =====================================================

START TRANSACTION;

-- 1. 新增岗位职位
INSERT INTO sys_post (post_code, post_name, post_sort, status, create_by, create_time, remark) VALUES
('general_manager', '总经理', 0, '0', 'system', NOW(), '公司整体运营管理'),
('dept_head', '部门负责人', 5, '0', 'system', NOW(), '各部门负责人');

-- 2. 移除系统管理员的岗位（admin不是普通用户，不需要业务岗位）
DELETE FROM sys_user_post WHERE user_id = 1;

-- 3. 为总经理和各部门负责人分配岗位
-- 总经理：杨华(综合管理部)
INSERT INTO sys_user_post (user_id, post_id)
SELECT u.user_id, p.post_id FROM sys_user u, sys_post p
WHERE u.user_name = 'yanghua' AND p.post_code = 'general_manager';

-- 各部门负责人统一使用"部门负责人"岗位
INSERT INTO sys_user_post (user_id, post_id)
SELECT u.user_id, p.post_id FROM sys_user u, sys_post p
WHERE u.user_name = 'zhangtao' AND p.post_code = 'dept_head';

INSERT INTO sys_user_post (user_id, post_id)
SELECT u.user_id, p.post_id FROM sys_user u, sys_post p
WHERE u.user_name = 'zhouwei' AND p.post_code = 'dept_head';

INSERT INTO sys_user_post (user_id, post_id)
SELECT u.user_id, p.post_id FROM sys_user u, sys_post p
WHERE u.user_name = 'zhengyu' AND p.post_code = 'dept_head';

INSERT INTO sys_user_post (user_id, post_id)
SELECT u.user_id, p.post_id FROM sys_user u, sys_post p
WHERE u.user_name = 'wuhao' AND p.post_code = 'dept_head';

INSERT INTO sys_user_post (user_id, post_id)
SELECT u.user_id, p.post_id FROM sys_user u, sys_post p
WHERE u.user_name = 'huying' AND p.post_code = 'dept_head';

INSERT INTO sys_user_post (user_id, post_id)
SELECT u.user_id, p.post_id FROM sys_user u, sys_post p
WHERE u.user_name = 'xuzhi' AND p.post_code = 'dept_head';

INSERT INTO sys_user_post (user_id, post_id)
SELECT u.user_id, p.post_id FROM sys_user u, sys_post p
WHERE u.user_name = 'xiebin' AND p.post_code = 'dept_head';

-- 4. 更新部门负责人信息（leader字段）
UPDATE sys_dept SET leader = '杨华' WHERE dept_id = 100;  -- 鼎泰科技有限公司
UPDATE sys_dept SET leader = '谢斌' WHERE dept_id = 101;  -- 质量部
UPDATE sys_dept SET leader = '张涛' WHERE dept_id = 103;  -- 研发部
UPDATE sys_dept SET leader = '周伟' WHERE dept_id = 104;  -- 市场部
UPDATE sys_dept SET leader = '郑宇' WHERE dept_id = 105;  -- 综合管理部
UPDATE sys_dept SET leader = '吴昊' WHERE dept_id = 106;  -- 财务部
UPDATE sys_dept SET leader = '胡颖' WHERE dept_id = 107;  -- 人事部
UPDATE sys_dept SET leader = '许智' WHERE dept_id = 200;  -- 生产部

COMMIT;

-- 5. 验证结果
-- 查看所有岗位
SELECT post_id, post_code, post_name, post_sort, status FROM sys_post ORDER BY post_sort;

-- 查看用户岗位关联
SELECT u.user_id, u.user_name, u.nick_name, d.dept_name, p.post_name
FROM sys_user u
LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
LEFT JOIN sys_user_post up ON u.user_id = up.user_id
LEFT JOIN sys_post p ON up.post_id = p.post_id
WHERE u.del_flag = '0' AND p.post_id IS NOT NULL
ORDER BY p.post_sort, u.user_id;

-- 查看部门负责人
SELECT dept_id, dept_name, leader, status FROM sys_dept WHERE del_flag = '0' ORDER BY order_num;

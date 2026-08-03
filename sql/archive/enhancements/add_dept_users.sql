-- =====================================================
-- 为每个部门批量增加 3~5 个用户（匹配当前部门名称）
-- 密码统一为 admin123
-- =====================================================

-- 0. 清理之前可能插入的测试用户
DELETE FROM `sys_user_role` WHERE `user_id` IN (
  SELECT `user_id` FROM `sys_user` WHERE `user_name` IN (
    'wangwei','liuyang','chenjie','zhangmin','zhaolei','sunli','zhouwei',
    'wuxia','zhengyu','fengling','qianwei','sunjing','wuhao',
    'linpeng','gaojie','xulei','huying',
    'liling','majie','tangyu','xuzhi','caoyan','dinghui','xiebin'
  )
);
DELETE FROM `sys_user` WHERE `user_name` IN (
  'wangwei','liuyang','chenjie','zhangmin','zhaolei','sunli','zhouwei',
  'wuxia','zhengyu','fengling','qianwei','sunjing','wuhao',
  'linpeng','gaojie','xulei','huying',
  'liling','majie','tangyu','xuzhi','caoyan','dinghui','xiebin'
);

-- 1. 研发部 —— 3 人
SET @d = (SELECT dept_id FROM sys_dept WHERE dept_name='研发部' AND del_flag='0' LIMIT 1);
INSERT INTO sys_user (dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, create_by, create_time, remark)
SELECT @d, v.user_name, v.nick_name, '00', v.email, v.phonenumber, v.sex, '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', NOW(), v.remark
FROM (SELECT 'wangwei' AS user_name,'王伟' AS nick_name,'wangwei@dt.com' AS email,'13800001001' AS phonenumber,'0' AS sex,'研发工程师' AS remark
UNION ALL SELECT 'liuyang','刘洋','liuyang@dt.com','13800001002','0','研发工程师'
UNION ALL SELECT 'chenjie','陈杰','chenjie@dt.com','13800001003','1','测试工程师') v
WHERE @d IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name=v.user_name AND del_flag='0');
SET @d = NULL;

-- 2. 市场部 —— 4 人
SET @d = (SELECT dept_id FROM sys_dept WHERE dept_name='市场部' AND del_flag='0' LIMIT 1);
INSERT INTO sys_user (dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, create_by, create_time, remark)
SELECT @d, v.user_name, v.nick_name, '00', v.email, v.phonenumber, v.sex, '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', NOW(), v.remark
FROM (SELECT 'zhangmin' AS user_name,'张敏' AS nick_name,'zhangmin@dt.com' AS email,'13800002001' AS phonenumber,'1' AS sex,'市场专员' AS remark
UNION ALL SELECT 'zhaolei','赵磊','zhaolei@dt.com','13800002002','0','市场专员'
UNION ALL SELECT 'sunli','孙丽','sunli@dt.com','13800002003','1','市场专员'
UNION ALL SELECT 'zhouwei','周伟','zhouwei@dt.com','13800002004','0','市场经理') v
WHERE @d IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name=v.user_name AND del_flag='0');
SET @d = NULL;

-- 3. 综合管理部 —— 3 人
SET @d = (SELECT dept_id FROM sys_dept WHERE dept_name='综合管理部' AND del_flag='0' LIMIT 1);
INSERT INTO sys_user (dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, create_by, create_time, remark)
SELECT @d, v.user_name, v.nick_name, '00', v.email, v.phonenumber, v.sex, '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', NOW(), v.remark
FROM (SELECT 'wuxia' AS user_name,'吴霞' AS nick_name,'wuxia@dt.com' AS email,'13800003001' AS phonenumber,'1' AS sex,'行政专员' AS remark
UNION ALL SELECT 'zhengyu','郑宇','zhengyu@dt.com','13800003002','0','行政主管'
UNION ALL SELECT 'fengling','冯玲','fengling@dt.com','13800003003','1','人事专员') v
WHERE @d IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name=v.user_name AND del_flag='0');
SET @d = NULL;

-- 4. 财务部 —— 3 人
SET @d = (SELECT dept_id FROM sys_dept WHERE dept_name='财务部' AND del_flag='0' LIMIT 1);
INSERT INTO sys_user (dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, create_by, create_time, remark)
SELECT @d, v.user_name, v.nick_name, '00', v.email, v.phonenumber, v.sex, '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', NOW(), v.remark
FROM (SELECT 'qianwei' AS user_name,'钱伟' AS nick_name,'qianwei@dt.com' AS email,'13800004001' AS phonenumber,'0' AS sex,'会计' AS remark
UNION ALL SELECT 'sunjing','孙静','sunjing@dt.com','13800004002','1','出纳'
UNION ALL SELECT 'wuhao','吴昊','wuhao@dt.com','13800004003','0','财务经理') v
WHERE @d IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name=v.user_name AND del_flag='0');
SET @d = NULL;

-- 5. 人事部 —— 4 人
SET @d = (SELECT dept_id FROM sys_dept WHERE dept_name='人事部' AND del_flag='0' LIMIT 1);
INSERT INTO sys_user (dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, create_by, create_time, remark)
SELECT @d, v.user_name, v.nick_name, '00', v.email, v.phonenumber, v.sex, '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', NOW(), v.remark
FROM (SELECT 'linpeng' AS user_name,'林鹏' AS nick_name,'linpeng@dt.com' AS email,'13800005001' AS phonenumber,'0' AS sex,'人事专员' AS remark
UNION ALL SELECT 'gaojie','高洁','gaojie@dt.com','13800005002','1','招聘专员'
UNION ALL SELECT 'xulei','徐磊','xulei@dt.com','13800005003','0','人事专员'
UNION ALL SELECT 'huying','胡颖','huying@dt.com','13800005004','1','人事主管') v
WHERE @d IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name=v.user_name AND del_flag='0');
SET @d = NULL;

-- 6. 生产部 —— 4 人
SET @d = (SELECT dept_id FROM sys_dept WHERE dept_name='生产部' AND del_flag='0' LIMIT 1);
INSERT INTO sys_user (dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, create_by, create_time, remark)
SELECT @d, v.user_name, v.nick_name, '00', v.email, v.phonenumber, v.sex, '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', NOW(), v.remark
FROM (SELECT 'liling' AS user_name,'李玲' AS nick_name,'liling@dt.com' AS email,'13800006001' AS phonenumber,'1' AS sex,'生产操作工' AS remark
UNION ALL SELECT 'majie','马杰','majie@dt.com','13800006002','0','生产操作工'
UNION ALL SELECT 'tangyu','唐宇','tangyu@dt.com','13800006003','0','生产班长'
UNION ALL SELECT 'xuzhi','许智','xuzhi@dt.com','13800006004','1','生产主管') v
WHERE @d IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name=v.user_name AND del_flag='0');
SET @d = NULL;

-- 7. 质量部 —— 3 人
SET @d = (SELECT dept_id FROM sys_dept WHERE dept_name='质量部' AND del_flag='0' LIMIT 1);
INSERT INTO sys_user (dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, create_by, create_time, remark)
SELECT @d, v.user_name, v.nick_name, '00', v.email, v.phonenumber, v.sex, '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', NOW(), v.remark
FROM (SELECT 'caoyan' AS user_name,'曹燕' AS nick_name,'caoyan@dt.com' AS email,'13800007001' AS phonenumber,'1' AS sex,'质检员' AS remark
UNION ALL SELECT 'dinghui','丁辉','dinghui@dt.com','13800007002','0','质检员'
UNION ALL SELECT 'xiebin','谢斌','xiebin@dt.com','13800007003','0','质量主管') v
WHERE @d IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name=v.user_name AND del_flag='0');
SET @d = NULL;

-- 8. 用户角色关联 (100=普通员工,101=财务人员,102=部门负责人,103=业务经理)
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 100 FROM sys_user u WHERE u.user_name IN ('wangwei','liuyang','chenjie','zhaolei','sunli','wuxia','fengling','linpeng','gaojie','xulei','liling','majie','caoyan','dinghui') AND u.del_flag='0'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id=u.user_id AND ur.role_id=100);
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 103 FROM sys_user u WHERE u.user_name IN ('zhangmin','zhouwei') AND u.del_flag='0'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id=u.user_id AND ur.role_id=103);
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 102 FROM sys_user u WHERE u.user_name IN ('zhengyu','wuhao','huying','tangyu','xuzhi','xiebin') AND u.del_flag='0'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id=u.user_id AND ur.role_id=102);
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 101 FROM sys_user u WHERE u.user_name IN ('qianwei','sunjing') AND u.del_flag='0'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id=u.user_id AND ur.role_id=101);

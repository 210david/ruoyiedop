-- ============================================================
-- EDOP - Merged SQL (Auto-generated, do not edit manually)
-- Generated: 2026-07-27 09:40:02
-- ============================================================

CREATE DATABASE IF NOT EXISTS `ry-vue` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ry-vue`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


-- ============================================================
-- File: ry_20260417.sql
-- ============================================================

-- ----------------------------
-- 1、部门表
-- ----------------------------
drop table if exists sys_dept;
create table sys_dept (
  dept_id           bigint(20)      not null auto_increment    comment '部门id',
  parent_id         bigint(20)      default 0                  comment '父部门id',
  ancestors         varchar(50)     default ''                 comment '祖级列表',
  dept_name         varchar(30)     default ''                 comment '部门名称',
  order_num         int(4)          default 0                  comment '显示顺序',
  leader            varchar(20)     default null               comment '负责人',
  phone             varchar(11)     default null               comment '联系电话',
  email             varchar(50)     default null               comment '邮箱',
  status            char(1)         default '0'                comment '部门状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (dept_id)
) engine=innodb auto_increment=200 comment = '部门表';

-- ----------------------------
-- 初始化-部门表数据
-- ----------------------------
insert into sys_dept values(100,  0,   '0',          '若依科技',   0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(101,  100, '0,100',      '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(102,  100, '0,100',      '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(103,  101, '0,100,101',  '研发部门',   1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(104,  101, '0,100,101',  '市场部门',   2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(105,  101, '0,100,101',  '测试部门',   3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(106,  101, '0,100,101',  '财务部门',   4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(107,  101, '0,100,101',  '运维部门',   5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(108,  102, '0,100,102',  '市场部门',   1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(109,  102, '0,100,102',  '财务部门',   2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);


-- ----------------------------
-- 2、用户信息表
-- ----------------------------
drop table if exists sys_user;
create table sys_user (
  user_id           bigint(20)      not null auto_increment    comment '用户ID',
  dept_id           bigint(20)      default null               comment '部门ID',
  user_name         varchar(30)     not null                   comment '用户账号',
  nick_name         varchar(30)     not null                   comment '用户昵称',
  user_type         varchar(2)      default '00'               comment '用户类型（00系统用户）',
  email             varchar(50)     default ''                 comment '用户邮箱',
  phonenumber       varchar(11)     default ''                 comment '手机号码',
  sex               char(1)         default '0'                comment '用户性别（0男 1女 2未知）',
  avatar            varchar(100)    default ''                 comment '头像地址',
  password          varchar(100)    default ''                 comment '密码',
  status            char(1)         default '0'                comment '账号状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  login_ip          varchar(128)    default ''                 comment '最后登录IP',
  login_date        datetime                                   comment '最后登录时间',
  pwd_update_date   datetime                                   comment '密码最后更新时间',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (user_id)
) engine=innodb auto_increment=100 comment = '用户信息表';

-- ----------------------------
-- 初始化-用户信息表数据
-- ----------------------------
insert into sys_user values(1,  103, 'admin', '若依', '00', 'ry@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '管理员');
insert into sys_user values(2,  105, 'ry',    '若依', '00', 'ry@qq.com',  '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '测试员');


-- ----------------------------
-- 3、岗位信息表
-- ----------------------------
drop table if exists sys_post;
create table sys_post
(
  post_id       bigint(20)      not null auto_increment    comment '岗位ID',
  post_code     varchar(64)     not null                   comment '岗位编码',
  post_name     varchar(50)     not null                   comment '岗位名称',
  post_sort     int(4)          not null                   comment '显示顺序',
  status        char(1)         not null                   comment '状态（0正常 1停用）',
  create_by     varchar(64)     default ''                 comment '创建者',
  create_time   datetime                                   comment '创建时间',
  update_by     varchar(64)     default ''			       comment '更新者',
  update_time   datetime                                   comment '更新时间',
  remark        varchar(500)    default null               comment '备注',
  primary key (post_id)
) engine=innodb comment = '岗位信息表';

-- ----------------------------
-- 初始化-岗位信息表数据
-- ----------------------------
insert into sys_post values(1, 'ceo',  '董事长',    1, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(2, 'se',   '项目经理',  2, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(3, 'hr',   '人力资源',  3, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(4, 'user', '普通员工',  4, '0', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 4、角色信息表
-- ----------------------------
drop table if exists sys_role;
create table sys_role (
  role_id              bigint(20)      not null auto_increment    comment '角色ID',
  role_name            varchar(30)     not null                   comment '角色名称',
  role_key             varchar(100)    not null                   comment '角色权限字符串',
  role_sort            int(4)          not null                   comment '显示顺序',
  role_category        varchar(50)     default 'system'          comment '角色分类（业务模块：system系统管理 dms经销商管理 pms采购管理 mk营销管理 wms仓储管理）',
  data_scope           char(1)         default '1'                comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  menu_check_strictly  tinyint(1)      default 1                  comment '菜单树选择项是否关联显示',
  dept_check_strictly  tinyint(1)      default 1                  comment '部门树选择项是否关联显示',
  status               char(1)         not null                   comment '角色状态（0正常 1停用）',
  del_flag             char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by            varchar(64)     default ''                 comment '创建者',
  create_time          datetime                                   comment '创建时间',
  update_by            varchar(64)     default ''                 comment '更新者',
  update_time          datetime                                   comment '更新时间',
  remark               varchar(500)    default null               comment '备注',
  primary key (role_id)
) engine=innodb auto_increment=100 comment = '角色信息表';

-- ----------------------------
-- 初始化-角色信息表数据
-- ----------------------------
insert into sys_role values('1', '超级管理员',  'admin',  1, 'system', 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '超级管理员');
insert into sys_role values('2', '普通角色',    'common', 2, 'system', 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, '普通角色');


-- ----------------------------
-- 5、菜单权限表
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu (
  menu_id           bigint(20)      not null auto_increment    comment '菜单ID',
  menu_name         varchar(50)     not null                   comment '菜单名称',
  parent_id         bigint(20)      default 0                  comment '父菜单ID',
  order_num         int(4)          default 0                  comment '显示顺序',
  path              varchar(200)    default ''                 comment '路由地址',
  component         varchar(255)    default null               comment '组件路径',
  query             varchar(255)    default null               comment '路由参数',
  route_name        varchar(50)     default ''                 comment '路由名称',
  is_frame          int(1)          default 1                  comment '是否为外链（0是 1否）',
  is_cache          int(1)          default 0                  comment '是否缓存（0缓存 1不缓存）',
  menu_type         char(1)         default ''                 comment '菜单类型（M目录 C菜单 F按钮）',
  visible           char(1)         default 0                  comment '菜单状态（0显示 1隐藏）',
  status            char(1)         default 0                  comment '菜单状态（0正常 1停用）',
  perms             varchar(100)    default null               comment '权限标识',
  icon              varchar(100)    default '#'                comment '菜单图标',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (menu_id)
) engine=innodb auto_increment=2000 comment = '菜单权限表';

-- ----------------------------
-- 初始化-菜单信息表数据
-- ----------------------------
-- 一级菜单
insert into sys_menu values('1', '系统管理', '0', '1', 'system',           null, '', '', 1, 0, 'M', '0', '0', '', 'system',   'admin', sysdate(), '', null, '系统管理目录');
insert into sys_menu values('2', '系统监控', '0', '2', 'monitor',          null, '', '', 1, 0, 'M', '0', '0', '', 'monitor',  'admin', sysdate(), '', null, '系统监控目录');
insert into sys_menu values('3', '系统工具', '0', '3', 'tool',             null, '', '', 1, 0, 'M', '0', '0', '', 'tool',     'admin', sysdate(), '', null, '系统工具目录');
insert into sys_menu values('4', '若依官网', '0', '4', 'http://ruoyi.vip', null, '', '', 0, 0, 'M', '0', '0', '', 'guide',    'admin', sysdate(), '', null, '若依官网地址');
-- 二级菜单
insert into sys_menu values('100',  '用户管理', '1',   '1', 'user',       'system/user/index',        '', '', 1, 0, 'C', '0', '0', 'system:user:list',        'user',          'admin', sysdate(), '', null, '用户管理菜单');
insert into sys_menu values('101',  '角色管理', '1',   '2', 'role',       'system/role/index',        '', '', 1, 0, 'C', '0', '0', 'system:role:list',        'peoples',       'admin', sysdate(), '', null, '角色管理菜单');
insert into sys_menu values('102',  '菜单管理', '1',   '3', 'menu',       'system/menu/index',        '', '', 1, 0, 'C', '0', '0', 'system:menu:list',        'tree-table',    'admin', sysdate(), '', null, '菜单管理菜单');
insert into sys_menu values('103',  '部门管理', '1',   '4', 'dept',       'system/dept/index',        '', '', 1, 0, 'C', '0', '0', 'system:dept:list',        'tree',          'admin', sysdate(), '', null, '部门管理菜单');
insert into sys_menu values('104',  '岗位管理', '1',   '5', 'post',       'system/post/index',        '', '', 1, 0, 'C', '0', '0', 'system:post:list',        'post',          'admin', sysdate(), '', null, '岗位管理菜单');
insert into sys_menu values('105',  '字典管理', '1',   '6', 'dict',       'system/dict/index',        '', '', 1, 0, 'C', '0', '0', 'system:dict:list',        'dict',          'admin', sysdate(), '', null, '字典管理菜单');
insert into sys_menu values('106',  '参数设置', '1',   '7', 'config',     'system/config/index',      '', '', 1, 0, 'C', '0', '0', 'system:config:list',      'edit',          'admin', sysdate(), '', null, '参数设置菜单');
insert into sys_menu values('107',  '通知公告', '1',   '8', 'notice',     'system/notice/index',      '', '', 1, 0, 'C', '0', '0', 'system:notice:list',      'message',       'admin', sysdate(), '', null, '通知公告菜单');
insert into sys_menu values('108',  '日志管理', '1',   '9', 'log',        '',                         '', '', 1, 0, 'M', '0', '0', '',                        'log',           'admin', sysdate(), '', null, '日志管理菜单');
insert into sys_menu values('109',  '在线用户', '2',   '1', 'online',     'monitor/online/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:online:list',     'online',        'admin', sysdate(), '', null, '在线用户菜单');
insert into sys_menu values('110',  '定时任务', '2',   '2', 'job',        'monitor/job/index',        '', '', 1, 0, 'C', '0', '0', 'monitor:job:list',        'job',           'admin', sysdate(), '', null, '定时任务菜单');
insert into sys_menu values('111',  '数据监控', '2',   '3', 'druid',      'monitor/druid/index',      '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list',      'druid',         'admin', sysdate(), '', null, '数据监控菜单');
insert into sys_menu values('112',  '服务监控', '2',   '4', 'server',     'monitor/server/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:server:list',     'server',        'admin', sysdate(), '', null, '服务监控菜单');
insert into sys_menu values('113',  '缓存监控', '2',   '5', 'cache',      'monitor/cache/index',      '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',      'redis',         'admin', sysdate(), '', null, '缓存监控菜单');
insert into sys_menu values('114',  '缓存列表', '2',   '6', 'cacheList',  'monitor/cache/list',       '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',      'redis-list',    'admin', sysdate(), '', null, '缓存列表菜单');
insert into sys_menu values('115',  '表单构建', '3',   '1', 'build',      'tool/build/index',         '', '', 1, 0, 'C', '0', '0', 'tool:build:list',         'build',         'admin', sysdate(), '', null, '表单构建菜单');
insert into sys_menu values('116',  '代码生成', '3',   '2', 'gen',        'tool/gen/index',           '', '', 1, 0, 'C', '0', '0', 'tool:gen:list',           'code',          'admin', sysdate(), '', null, '代码生成菜单');
insert into sys_menu values('117',  '系统接口', '3',   '3', 'swagger',    'tool/swagger/index',       '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list',       'swagger',       'admin', sysdate(), '', null, '系统接口菜单');
-- 三级菜单
insert into sys_menu values('500',  '操作日志', '108', '1', 'operlog',    'monitor/operlog/index',    '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list',    'form',          'admin', sysdate(), '', null, '操作日志菜单');
insert into sys_menu values('501',  '登录日志', '108', '2', 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor',    'admin', sysdate(), '', null, '登录日志菜单');
-- 用户管理按钮
insert into sys_menu values('1000', '用户查询', '100', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1001', '用户新增', '100', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1002', '用户修改', '100', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1003', '用户删除', '100', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1004', '用户导出', '100', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1005', '用户导入', '100', '6',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1006', '重置密码', '100', '7',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd',       '#', 'admin', sysdate(), '', null, '');
-- 角色管理按钮
insert into sys_menu values('1007', '角色查询', '101', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1008', '角色新增', '101', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1009', '角色修改', '101', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1010', '角色删除', '101', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1011', '角色导出', '101', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export',         '#', 'admin', sysdate(), '', null, '');
-- 菜单管理按钮
insert into sys_menu values('1012', '菜单查询', '102', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1013', '菜单新增', '102', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1014', '菜单修改', '102', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1015', '菜单删除', '102', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove',         '#', 'admin', sysdate(), '', null, '');
-- 部门管理按钮
insert into sys_menu values('1016', '部门查询', '103', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1017', '部门新增', '103', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1018', '部门修改', '103', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1019', '部门删除', '103', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove',         '#', 'admin', sysdate(), '', null, '');
-- 岗位管理按钮
insert into sys_menu values('1020', '岗位查询', '104', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1021', '岗位新增', '104', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1022', '岗位修改', '104', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1023', '岗位删除', '104', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1024', '岗位导出', '104', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export',         '#', 'admin', sysdate(), '', null, '');
-- 字典管理按钮
insert into sys_menu values('1025', '字典查询', '105', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1026', '字典新增', '105', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1027', '字典修改', '105', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1028', '字典删除', '105', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1029', '字典导出', '105', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export',         '#', 'admin', sysdate(), '', null, '');
-- 参数设置按钮
insert into sys_menu values('1030', '参数查询', '106', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1031', '参数新增', '106', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1032', '参数修改', '106', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1033', '参数删除', '106', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1034', '参数导出', '106', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export',       '#', 'admin', sysdate(), '', null, '');
-- 通知公告按钮
insert into sys_menu values('1035', '公告查询', '107', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1036', '公告新增', '107', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1037', '公告修改', '107', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1038', '公告删除', '107', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove',       '#', 'admin', sysdate(), '', null, '');
-- 操作日志按钮
insert into sys_menu values('1039', '操作查询', '500', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query',      '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1040', '操作删除', '500', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1041', '日志导出', '500', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export',     '#', 'admin', sysdate(), '', null, '');
-- 登录日志按钮
insert into sys_menu values('1042', '登录查询', '501', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1043', '登录删除', '501', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1044', '日志导出', '501', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1045', '账户解锁', '501', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock',  '#', 'admin', sysdate(), '', null, '');
-- 在线用户按钮
insert into sys_menu values('1046', '在线查询', '109', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1047', '批量强退', '109', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1048', '单条强退', '109', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', sysdate(), '', null, '');
-- 定时任务按钮
insert into sys_menu values('1049', '任务查询', '110', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1050', '任务新增', '110', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1051', '任务修改', '110', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1052', '任务删除', '110', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1053', '状态修改', '110', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1054', '任务导出', '110', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export',         '#', 'admin', sysdate(), '', null, '');
-- 代码生成按钮
insert into sys_menu values('1055', '生成查询', '116', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query',             '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1056', '生成修改', '116', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit',              '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1057', '生成删除', '116', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1058', '导入代码', '116', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1059', '预览代码', '116', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1060', '生成代码', '116', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code',              '#', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 6、用户和角色关联表  用户N-1角色
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role (
  user_id   bigint(20) not null comment '用户ID',
  role_id   bigint(20) not null comment '角色ID',
  primary key(user_id, role_id)
) engine=innodb comment = '用户和角色关联表';

-- ----------------------------
-- 初始化-用户和角色关联表数据
-- ----------------------------
insert into sys_user_role values ('1', '1');
insert into sys_user_role values ('2', '2');


-- ----------------------------
-- 7、角色和菜单关联表  角色1-N菜单
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu (
  role_id   bigint(20) not null comment '角色ID',
  menu_id   bigint(20) not null comment '菜单ID',
  primary key(role_id, menu_id)
) engine=innodb comment = '角色和菜单关联表';

-- ----------------------------
-- 初始化-角色和菜单关联表数据
-- ----------------------------
insert into sys_role_menu values ('2', '1');
insert into sys_role_menu values ('2', '2');
insert into sys_role_menu values ('2', '3');
insert into sys_role_menu values ('2', '4');
insert into sys_role_menu values ('2', '100');
insert into sys_role_menu values ('2', '101');
insert into sys_role_menu values ('2', '102');
insert into sys_role_menu values ('2', '103');
insert into sys_role_menu values ('2', '104');
insert into sys_role_menu values ('2', '105');
insert into sys_role_menu values ('2', '106');
insert into sys_role_menu values ('2', '107');
insert into sys_role_menu values ('2', '108');
insert into sys_role_menu values ('2', '109');
insert into sys_role_menu values ('2', '110');
insert into sys_role_menu values ('2', '111');
insert into sys_role_menu values ('2', '112');
insert into sys_role_menu values ('2', '113');
insert into sys_role_menu values ('2', '114');
insert into sys_role_menu values ('2', '115');
insert into sys_role_menu values ('2', '116');
insert into sys_role_menu values ('2', '117');
insert into sys_role_menu values ('2', '500');
insert into sys_role_menu values ('2', '501');
insert into sys_role_menu values ('2', '1000');
insert into sys_role_menu values ('2', '1001');
insert into sys_role_menu values ('2', '1002');
insert into sys_role_menu values ('2', '1003');
insert into sys_role_menu values ('2', '1004');
insert into sys_role_menu values ('2', '1005');
insert into sys_role_menu values ('2', '1006');
insert into sys_role_menu values ('2', '1007');
insert into sys_role_menu values ('2', '1008');
insert into sys_role_menu values ('2', '1009');
insert into sys_role_menu values ('2', '1010');
insert into sys_role_menu values ('2', '1011');
insert into sys_role_menu values ('2', '1012');
insert into sys_role_menu values ('2', '1013');
insert into sys_role_menu values ('2', '1014');
insert into sys_role_menu values ('2', '1015');
insert into sys_role_menu values ('2', '1016');
insert into sys_role_menu values ('2', '1017');
insert into sys_role_menu values ('2', '1018');
insert into sys_role_menu values ('2', '1019');
insert into sys_role_menu values ('2', '1020');
insert into sys_role_menu values ('2', '1021');
insert into sys_role_menu values ('2', '1022');
insert into sys_role_menu values ('2', '1023');
insert into sys_role_menu values ('2', '1024');
insert into sys_role_menu values ('2', '1025');
insert into sys_role_menu values ('2', '1026');
insert into sys_role_menu values ('2', '1027');
insert into sys_role_menu values ('2', '1028');
insert into sys_role_menu values ('2', '1029');
insert into sys_role_menu values ('2', '1030');
insert into sys_role_menu values ('2', '1031');
insert into sys_role_menu values ('2', '1032');
insert into sys_role_menu values ('2', '1033');
insert into sys_role_menu values ('2', '1034');
insert into sys_role_menu values ('2', '1035');
insert into sys_role_menu values ('2', '1036');
insert into sys_role_menu values ('2', '1037');
insert into sys_role_menu values ('2', '1038');
insert into sys_role_menu values ('2', '1039');
insert into sys_role_menu values ('2', '1040');
insert into sys_role_menu values ('2', '1041');
insert into sys_role_menu values ('2', '1042');
insert into sys_role_menu values ('2', '1043');
insert into sys_role_menu values ('2', '1044');
insert into sys_role_menu values ('2', '1045');
insert into sys_role_menu values ('2', '1046');
insert into sys_role_menu values ('2', '1047');
insert into sys_role_menu values ('2', '1048');
insert into sys_role_menu values ('2', '1049');
insert into sys_role_menu values ('2', '1050');
insert into sys_role_menu values ('2', '1051');
insert into sys_role_menu values ('2', '1052');
insert into sys_role_menu values ('2', '1053');
insert into sys_role_menu values ('2', '1054');
insert into sys_role_menu values ('2', '1055');
insert into sys_role_menu values ('2', '1056');
insert into sys_role_menu values ('2', '1057');
insert into sys_role_menu values ('2', '1058');
insert into sys_role_menu values ('2', '1059');
insert into sys_role_menu values ('2', '1060');

-- ----------------------------
-- 8、角色和部门关联表  角色1-N部门
-- ----------------------------
drop table if exists sys_role_dept;
create table sys_role_dept (
  role_id   bigint(20) not null comment '角色ID',
  dept_id   bigint(20) not null comment '部门ID',
  primary key(role_id, dept_id)
) engine=innodb comment = '角色和部门关联表';

-- ----------------------------
-- 初始化-角色和部门关联表数据
-- ----------------------------
insert into sys_role_dept values ('2', '100');
insert into sys_role_dept values ('2', '101');
insert into sys_role_dept values ('2', '105');


-- ----------------------------
-- 9、用户与岗位关联表  用户1-N岗位
-- ----------------------------
drop table if exists sys_user_post;
create table sys_user_post
(
  user_id   bigint(20) not null comment '用户ID',
  post_id   bigint(20) not null comment '岗位ID',
  primary key (user_id, post_id)
) engine=innodb comment = '用户与岗位关联表';

-- ----------------------------
-- 初始化-用户与岗位关联表数据
-- ----------------------------
insert into sys_user_post values ('1', '1');
insert into sys_user_post values ('2', '2');


-- ----------------------------
-- 10、操作日志记录
-- ----------------------------
drop table if exists sys_oper_log;
create table sys_oper_log (
  oper_id           bigint(20)      not null auto_increment    comment '日志主键',
  title             varchar(50)     default ''                 comment '模块标题',
  business_type     int(2)          default 0                  comment '业务类型（0其它 1新增 2修改 3删除）',
  method            varchar(200)    default ''                 comment '方法名称',
  request_method    varchar(10)     default ''                 comment '请求方式',
  operator_type     int(1)          default 0                  comment '操作类别（0其它 1后台用户 2手机端用户）',
  oper_name         varchar(50)     default ''                 comment '操作人员',
  dept_name         varchar(50)     default ''                 comment '部门名称',
  oper_url          varchar(255)    default ''                 comment '请求URL',
  oper_ip           varchar(128)    default ''                 comment '主机地址',
  oper_location     varchar(255)    default ''                 comment '操作地点',
  oper_param        varchar(2000)   default ''                 comment '请求参数',
  json_result       varchar(2000)   default ''                 comment '返回参数',
  status            int(1)          default 0                  comment '操作状态（0正常 1异常）',
  error_msg         varchar(2000)   default ''                 comment '错误消息',
  oper_time         datetime                                   comment '操作时间',
  cost_time         bigint(20)      default 0                  comment '消耗时间',
  primary key (oper_id),
  key idx_sys_oper_log_bt (business_type),
  key idx_sys_oper_log_s  (status),
  key idx_sys_oper_log_ot (oper_time)
) engine=innodb auto_increment=100 comment = '操作日志记录';


-- ----------------------------
-- 11、字典类型表
-- ----------------------------
drop table if exists sys_dict_type;
create table sys_dict_type
(
  dict_id          bigint(20)      not null auto_increment    comment '字典主键',
  dict_name        varchar(100)    default ''                 comment '字典名称',
  dict_type        varchar(100)    default ''                 comment '字典类型',
  status           char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (dict_id),
  unique (dict_type)
) engine=innodb auto_increment=100 comment = '字典类型表';

insert into sys_dict_type values(1,  '用户性别', 'sys_user_sex',        '0', 'admin', sysdate(), '', null, '用户性别列表');
insert into sys_dict_type values(2,  '菜单状态', 'sys_show_hide',       '0', 'admin', sysdate(), '', null, '菜单状态列表');
insert into sys_dict_type values(3,  '系统开关', 'sys_normal_disable',  '0', 'admin', sysdate(), '', null, '系统开关列表');
insert into sys_dict_type values(4,  '任务状态', 'sys_job_status',      '0', 'admin', sysdate(), '', null, '任务状态列表');
insert into sys_dict_type values(5,  '任务分组', 'sys_job_group',       '0', 'admin', sysdate(), '', null, '任务分组列表');
insert into sys_dict_type values(6,  '系统是否', 'sys_yes_no',          '0', 'admin', sysdate(), '', null, '系统是否列表');
insert into sys_dict_type values(7,  '通知类型', 'sys_notice_type',     '0', 'admin', sysdate(), '', null, '通知类型列表');
insert into sys_dict_type values(8,  '通知状态', 'sys_notice_status',   '0', 'admin', sysdate(), '', null, '通知状态列表');
insert into sys_dict_type values(9,  '操作类型', 'sys_oper_type',       '0', 'admin', sysdate(), '', null, '操作类型列表');
insert into sys_dict_type values(10, '系统状态', 'sys_common_status',   '0', 'admin', sysdate(), '', null, '登录状态列表');
insert into sys_dict_type values(11, '角色分类', 'sys_role_category',    '0', 'admin', sysdate(), '', null, '角色所属业务模块分类');


-- ----------------------------
-- 12、字典数据表
-- ----------------------------
drop table if exists sys_dict_data;
create table sys_dict_data
(
  dict_code        bigint(20)      not null auto_increment    comment '字典编码',
  dict_sort        int(4)          default 0                  comment '字典排序',
  dict_label       varchar(100)    default ''                 comment '字典标签',
  dict_value       varchar(100)    default ''                 comment '字典键值',
  dict_type        varchar(100)    default ''                 comment '字典类型',
  css_class        varchar(100)    default null               comment '样式属性（其他样式扩展）',
  list_class       varchar(100)    default null               comment '表格回显样式',
  is_default       char(1)         default 'N'                comment '是否默认（Y是 N否）',
  status           char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (dict_code)
) engine=innodb auto_increment=100 comment = '字典数据表';

insert into sys_dict_data values(1,  1,  '男',       '0',       'sys_user_sex',        '',   '',        'Y', '0', 'admin', sysdate(), '', null, '性别男');
insert into sys_dict_data values(2,  2,  '女',       '1',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '性别女');
insert into sys_dict_data values(3,  3,  '未知',     '2',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '性别未知');
insert into sys_dict_data values(4,  1,  '显示',     '0',       'sys_show_hide',       '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '显示菜单');
insert into sys_dict_data values(5,  2,  '隐藏',     '1',       'sys_show_hide',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '隐藏菜单');
insert into sys_dict_data values(6,  1,  '正常',     '0',       'sys_normal_disable',  '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(7,  2,  '停用',     '1',       'sys_normal_disable',  '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(8,  1,  '正常',     '0',       'sys_job_status',      '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(9,  2,  '暂停',     '1',       'sys_job_status',      '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(10, 1,  '默认',     'DEFAULT', 'sys_job_group',       '',   '',        'Y', '0', 'admin', sysdate(), '', null, '默认分组');
insert into sys_dict_data values(11, 2,  '系统',     'SYSTEM',  'sys_job_group',       '',   '',        'N', '0', 'admin', sysdate(), '', null, '系统分组');
insert into sys_dict_data values(12, 1,  '是',       'Y',       'sys_yes_no',          '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '系统默认是');
insert into sys_dict_data values(13, 2,  '否',       'N',       'sys_yes_no',          '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '系统默认否');
insert into sys_dict_data values(14, 1,  '通知',     '1',       'sys_notice_type',     '',   'warning', 'Y', '0', 'admin', sysdate(), '', null, '通知');
insert into sys_dict_data values(15, 2,  '公告',     '2',       'sys_notice_type',     '',   'success', 'N', '0', 'admin', sysdate(), '', null, '公告');
insert into sys_dict_data values(16, 1,  '正常',     '0',       'sys_notice_status',   '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(17, 2,  '关闭',     '1',       'sys_notice_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '关闭状态');
insert into sys_dict_data values(18, 99, '其他',     '0',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '其他操作');
insert into sys_dict_data values(19, 1,  '新增',     '1',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '新增操作');
insert into sys_dict_data values(20, 2,  '修改',     '2',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '修改操作');
insert into sys_dict_data values(21, 3,  '删除',     '3',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '删除操作');
insert into sys_dict_data values(22, 4,  '授权',     '4',       'sys_oper_type',       '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '授权操作');
insert into sys_dict_data values(23, 5,  '导出',     '5',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '导出操作');
insert into sys_dict_data values(24, 6,  '导入',     '6',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '导入操作');
insert into sys_dict_data values(25, 7,  '强退',     '7',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '强退操作');
insert into sys_dict_data values(26, 8,  '生成代码', '8',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '生成操作');
insert into sys_dict_data values(27, 9,  '清空数据', '9',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '清空操作');
insert into sys_dict_data values(28, 1,  '成功',     '0',       'sys_common_status',   '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(29, 2,  '失败',     '1',       'sys_common_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(30, 1,  '系统管理', 'system',  'sys_role_category',    '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '系统管理模块角色');
insert into sys_dict_data values(31, 2,  '经销商管理', 'dms',   'sys_role_category',    '',   'success', 'N', '0', 'admin', sysdate(), '', null, '经销商管理模块角色');
insert into sys_dict_data values(32, 3,  '采购管理', 'pms',    'sys_role_category',    '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '采购管理模块角色');
insert into sys_dict_data values(33, 4,  '营销管理', 'mk',     'sys_role_category',    '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '营销管理模块角色');
insert into sys_dict_data values(34, 5,  '仓储管理', 'wms',    'sys_role_category',    '',   'info',    'N', '0', 'admin', sysdate(), '', null, '仓储管理模块角色');


-- ----------------------------
-- 13、参数配置表
-- ----------------------------
drop table if exists sys_config;
create table sys_config (
  config_id         int(5)          not null auto_increment    comment '参数主键',
  config_name       varchar(100)    default ''                 comment '参数名称',
  config_key        varchar(100)    default ''                 comment '参数键名',
  config_value      varchar(500)    default ''                 comment '参数键值',
  config_type       char(1)         default 'N'                comment '系统内置（Y是 N否）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (config_id)
) engine=innodb auto_increment=100 comment = '参数配置表';

insert into sys_config values(1, '主框架页-默认皮肤样式名称',     'sys.index.skinName',               'skin-blue',     'Y', 'admin', sysdate(), '', null, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow' );
insert into sys_config values(2, '用户管理-账号初始密码',         'sys.user.initPassword',            '123456',        'Y', 'admin', sysdate(), '', null, '初始化密码 123456' );
insert into sys_config values(3, '主框架页-侧边栏主题',           'sys.index.sideTheme',              'theme-dark',    'Y', 'admin', sysdate(), '', null, '深色主题theme-dark，浅色主题theme-light' );
insert into sys_config values(4, '账号自助-验证码开关',           'sys.account.captchaEnabled',       'true',          'Y', 'admin', sysdate(), '', null, '是否开启验证码功能（true开启，false关闭）');
insert into sys_config values(5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser',         'false',         'Y', 'admin', sysdate(), '', null, '是否开启注册用户功能（true开启，false关闭）');
insert into sys_config values(6, '用户登录-黑名单列表',           'sys.login.blackIPList',            '',              'Y', 'admin', sysdate(), '', null, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
insert into sys_config values(7, '用户管理-初始密码修改策略',     'sys.account.initPasswordModify',   '1',             'Y', 'admin', sysdate(), '', null, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
insert into sys_config values(8, '用户管理-账号密码更新周期',     'sys.account.passwordValidateDays', '0',             'Y', 'admin', sysdate(), '', null, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
insert into sys_config values(9, '用户管理-密码字符范围',         'sys.account.chrtype',              '0',             'Y', 'admin', sysdate(), '', null, '默认任意字符范围，0任意（密码可以输入任意字符），1数字（密码只能为0-9数字），2英文字母（密码只能为a-z和A-Z字母），3字母和数字（密码必须包含字母，数字）,4字母数字和特殊字符（目前支持的特殊字符包括：~!@#$%^&*()-=_+）');


-- ----------------------------
-- 14、系统访问记录
-- ----------------------------
drop table if exists sys_logininfor;
create table sys_logininfor (
  info_id        bigint(20)     not null auto_increment   comment '访问ID',
  user_name      varchar(50)    default ''                comment '用户账号',
  ipaddr         varchar(128)   default ''                comment '登录IP地址',
  login_location varchar(255)   default ''                comment '登录地点',
  browser        varchar(50)    default ''                comment '浏览器类型',
  os             varchar(50)    default ''                comment '操作系统',
  status         char(1)        default '0'               comment '登录状态（0成功 1失败）',
  msg            varchar(255)   default ''                comment '提示消息',
  login_time     datetime                                 comment '访问时间',
  primary key (info_id),
  key idx_sys_logininfor_s  (status),
  key idx_sys_logininfor_lt (login_time)
) engine=innodb auto_increment=100 comment = '系统访问记录';


-- ----------------------------
-- 15、定时任务调度表
-- ----------------------------
drop table if exists sys_job;
create table sys_job (
  job_id              bigint(20)    not null auto_increment    comment '任务ID',
  job_name            varchar(64)   default ''                 comment '任务名称',
  job_group           varchar(64)   default 'DEFAULT'          comment '任务组名',
  invoke_target       varchar(500)  not null                   comment '调用目标字符串',
  cron_expression     varchar(255)  default ''                 comment 'cron执行表达式',
  misfire_policy      varchar(20)   default '3'                comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  concurrent          char(1)       default '1'                comment '是否并发执行（0允许 1禁止）',
  status              char(1)       default '0'                comment '状态（0正常 1暂停）',
  create_by           varchar(64)   default ''                 comment '创建者',
  create_time         datetime                                 comment '创建时间',
  update_by           varchar(64)   default ''                 comment '更新者',
  update_time         datetime                                 comment '更新时间',
  remark              varchar(500)  default ''                 comment '备注信息',
  primary key (job_id, job_name, job_group)
) engine=innodb auto_increment=100 comment = '定时任务调度表';

insert into sys_job values(1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams',        '0/10 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')',  '0/15 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)',  '0/20 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 16、定时任务调度日志表
-- ----------------------------
drop table if exists sys_job_log;
create table sys_job_log (
  job_log_id          bigint(20)     not null auto_increment    comment '任务日志ID',
  job_name            varchar(64)    not null                   comment '任务名称',
  job_group           varchar(64)    not null                   comment '任务组名',
  invoke_target       varchar(500)   not null                   comment '调用目标字符串',
  job_message         varchar(500)                              comment '日志信息',
  status              char(1)        default '0'                comment '执行状态（0正常 1失败）',
  exception_info      varchar(2000)  default ''                 comment '异常信息',
  start_time          datetime                                  comment '执行开始时间',
  end_time            datetime                                  comment '执行结束时间',
  create_time         datetime                                  comment '创建时间',
  primary key (job_log_id)
) engine=innodb comment = '定时任务调度日志表';


-- ----------------------------
-- 17、通知公告表
-- ----------------------------
drop table if exists sys_notice;
create table sys_notice (
  notice_id         int(4)          not null auto_increment    comment '公告ID',
  notice_title      varchar(50)     not null                   comment '公告标题',
  notice_type       char(1)         not null                   comment '公告类型（1通知 2公告）',
  notice_content    longblob        default null               comment '公告内容',
  status            char(1)         default '0'                comment '公告状态（0正常 1关闭）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(255)    default null               comment '备注',
  primary key (notice_id)
) engine=innodb auto_increment=10 comment = '通知公告表';

-- ----------------------------
-- 初始化-公告信息表数据
-- ----------------------------
insert into sys_notice values('1', '温馨提醒：2018-07-01 若依新版本发布啦', '2', '新版本内容', '0', 'admin', sysdate(), '', null, '管理员');
insert into sys_notice values('2', '维护通知：2018-07-01 若依系统凌晨维护', '1', '维护内容',   '0', 'admin', sysdate(), '', null, '管理员');
insert into sys_notice values('3', '若依开源框架介绍', '1', '<p><span style=\"color: rgb(230, 0, 0);\">项目介绍</span></p><p><font color=\"#333333\">RuoYi开源项目是为企业用户定制的后台脚手架框架，为企业打造的一站式解决方案，降低企业开发成本，提升开发效率。主要包括用户管理、角色管理、部门管理、菜单管理、参数管理、字典管理、</font><span style=\"color: rgb(51, 51, 51);\">岗位管理</span><span style=\"color: rgb(51, 51, 51);\">、定时任务</span><span style=\"color: rgb(51, 51, 51);\">、</span><span style=\"color: rgb(51, 51, 51);\">服务监控、登录日志、操作日志、代码生成等功能。其中，还支持多数据源、数据权限、国际化、Redis缓存、Docker部署、滑动验证码、第三方认证登录、分布式事务、</span><font color=\"#333333\">分布式文件存储</font><span style=\"color: rgb(51, 51, 51);\">、分库分表处理等技术特点。</span></p><p><img src=\"https://foruda.gitee.com/images/1773931848342439032/a4d22313_1815095.png\" style=\"width: 64px;\"><br></p><p><span style=\"color: rgb(230, 0, 0);\">官网及演示</span></p><p><span style=\"color: rgb(51, 51, 51);\">若依官网地址：&nbsp;</span><a href=\"http://ruoyi.vip\" target=\"_blank\">http://ruoyi.vip</a><a href=\"http://ruoyi.vip\" target=\"_blank\"></a></p><p><span style=\"color: rgb(51, 51, 51);\">若依文档地址：&nbsp;</span><a href=\"http://doc.ruoyi.vip\" target=\"_blank\">http://doc.ruoyi.vip</a><br></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【不分离版】：&nbsp;</span><a href=\"http://demo.ruoyi.vip\" target=\"_blank\">http://demo.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【分离版本】：&nbsp;</span><a href=\"http://vue.ruoyi.vip\" target=\"_blank\">http://vue.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【微服务版】：&nbsp;</span><a href=\"http://cloud.ruoyi.vip\" target=\"_blank\">http://cloud.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【移动端版】：&nbsp;</span><a href=\"http://h5.ruoyi.vip\" target=\"_blank\">http://h5.ruoyi.vip</a></p><p><br style=\"color: rgb(48, 49, 51); font-family: &quot;Helvetica Neue&quot;, Helvetica, Arial, sans-serif; font-size: 12px;\"></p>', '0', 'admin', sysdate(), '', null, '管理员');


-- ----------------------------
-- 18、公告已读记录表
-- ----------------------------
drop table if exists sys_notice_read;
create table sys_notice_read (
  read_id          bigint(20)       not null auto_increment    comment '已读主键',
  notice_id        int(4)           not null                   comment '公告id',
  user_id          bigint(20)       not null                   comment '用户id',
  read_time        datetime         not null                   comment '阅读时间',
  primary key (read_id),
  unique key uk_user_notice (user_id, notice_id)   comment '同一用户同一公告只记录一次'
) engine=innodb auto_increment=1 comment='公告已读记录表';


-- ----------------------------
-- 19、代码生成业务表
-- ----------------------------
drop table if exists gen_table;
create table gen_table (
  table_id          bigint(20)      not null auto_increment    comment '编号',
  table_name        varchar(200)    default ''                 comment '表名称',
  table_comment     varchar(500)    default ''                 comment '表描述',
  sub_table_name    varchar(64)     default null               comment '关联子表的表名',
  sub_table_fk_name varchar(64)     default null               comment '子表关联的外键名',
  class_name        varchar(100)    default ''                 comment '实体类名称',
  tpl_category      varchar(200)    default 'crud'             comment '使用的模板（crud单表操作 tree树表操作）',
  tpl_web_type      varchar(30)     default ''                 comment '前端模板类型（element-ui模版 element-plus模版）',
  package_name      varchar(100)                               comment '生成包路径',
  module_name       varchar(30)                                comment '生成模块名',
  business_name     varchar(30)                                comment '生成业务名',
  function_name     varchar(50)                                comment '生成功能名',
  function_author   varchar(50)                                comment '生成功能作者',
  form_col_num      int(1)          default 1                  comment '表单布局（单列 双列 三列）',
  gen_type          char(1)         default '0'                comment '生成代码方式（0zip压缩包 1自定义路径）',
  gen_path          varchar(200)    default '/'                comment '生成路径（不填默认项目路径）',
  options           varchar(1000)                              comment '其它生成选项',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (table_id)
) engine=innodb auto_increment=1 comment = '代码生成业务表';


-- ----------------------------
-- 20、代码生成业务表字段
-- ----------------------------
drop table if exists gen_table_column;
create table gen_table_column (
  column_id         bigint(20)      not null auto_increment    comment '编号',
  table_id          bigint(20)                                 comment '归属表编号',
  column_name       varchar(200)                               comment '列名称',
  column_comment    varchar(500)                               comment '列描述',
  column_type       varchar(100)                               comment '列类型',
  java_type         varchar(500)                               comment 'JAVA类型',
  java_field        varchar(200)                               comment 'JAVA字段名',
  is_pk             char(1)                                    comment '是否主键（1是）',
  is_increment      char(1)                                    comment '是否自增（1是）',
  is_required       char(1)                                    comment '是否必填（1是）',
  is_insert         char(1)                                    comment '是否为插入字段（1是）',
  is_edit           char(1)                                    comment '是否编辑字段（1是）',
  is_list           char(1)                                    comment '是否列表字段（1是）',
  is_query          char(1)                                    comment '是否查询字段（1是）',
  query_type        varchar(200)    default 'EQ'               comment '查询方式（等于、不等于、大于、小于、范围）',
  html_type         varchar(200)                               comment '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  dict_type         varchar(200)    default ''                 comment '字典类型',
  sort              int                                        comment '排序',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (column_id)
) engine=innodb auto_increment=1 comment = '代码生成业务表字段';


-- ============================================================
-- File: quartz.sql
-- ============================================================

DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS QRTZ_LOCKS;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;
DROP TABLE IF EXISTS QRTZ_CALENDARS;

-- ----------------------------
-- 1、存储每一个已配置的 jobDetail 的详细信息
-- ----------------------------
create table QRTZ_JOB_DETAILS (
    sched_name           varchar(120)    not null            comment '调度名称',
    job_name             varchar(200)    not null            comment '任务名称',
    job_group            varchar(200)    not null            comment '任务组名',
    description          varchar(250)    null                comment '相关介绍',
    job_class_name       varchar(250)    not null            comment '执行任务类名称',
    is_durable           varchar(1)      not null            comment '是否持久化',
    is_nonconcurrent     varchar(1)      not null            comment '是否并发',
    is_update_data       varchar(1)      not null            comment '是否更新数据',
    requests_recovery    varchar(1)      not null            comment '是否接受恢复执行',
    job_data             blob            null                comment '存放持久化job对象',
    primary key (sched_name, job_name, job_group)
) engine=innodb comment = '任务详细信息表';

-- ----------------------------
-- 2、 存储已配置的 Trigger 的信息
-- ----------------------------
create table QRTZ_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment '触发器的名字',
    trigger_group        varchar(200)    not null            comment '触发器所属组的名字',
    job_name             varchar(200)    not null            comment 'qrtz_job_details表job_name的外键',
    job_group            varchar(200)    not null            comment 'qrtz_job_details表job_group的外键',
    description          varchar(250)    null                comment '相关介绍',
    next_fire_time       bigint(13)      null                comment '上一次触发时间（毫秒）',
    prev_fire_time       bigint(13)      null                comment '下一次触发时间（默认为-1表示不触发）',
    priority             integer         null                comment '优先级',
    trigger_state        varchar(16)     not null            comment '触发器状态',
    trigger_type         varchar(8)      not null            comment '触发器的类型',
    start_time           bigint(13)      not null            comment '开始时间',
    end_time             bigint(13)      null                comment '结束时间',
    calendar_name        varchar(200)    null                comment '日程表名称',
    misfire_instr        smallint(2)     null                comment '补偿执行的策略',
    job_data             blob            null                comment '存放持久化job对象',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, job_name, job_group) references QRTZ_JOB_DETAILS(sched_name, job_name, job_group)
) engine=innodb comment = '触发器详细信息表';

-- ----------------------------
-- 3、 存储简单的 Trigger，包括重复次数，间隔，以及已触发的次数
-- ----------------------------
create table QRTZ_SIMPLE_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    repeat_count         bigint(7)       not null            comment '重复的次数统计',
    repeat_interval      bigint(12)      not null            comment '重复的间隔时间',
    times_triggered      bigint(10)      not null            comment '已经触发的次数',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = '简单触发器的信息表';

-- ----------------------------
-- 4、 存储 Cron Trigger，包括 Cron 表达式和时区信息
-- ---------------------------- 
create table QRTZ_CRON_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    cron_expression      varchar(200)    not null            comment 'cron表达式',
    time_zone_id         varchar(80)                         comment '时区',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = 'Cron类型的触发器表';

-- ----------------------------
-- 5、 Trigger 作为 Blob 类型存储(用于 Quartz 用户用 JDBC 创建他们自己定制的 Trigger 类型，JobStore 并不知道如何存储实例的时候)
-- ---------------------------- 
create table QRTZ_BLOB_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    blob_data            blob            null                comment '存放持久化Trigger对象',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = 'Blob类型的触发器表';

-- ----------------------------
-- 6、 以 Blob 类型存储存放日历信息， quartz可配置一个日历来指定一个时间范围
-- ---------------------------- 
create table QRTZ_CALENDARS (
    sched_name           varchar(120)    not null            comment '调度名称',
    calendar_name        varchar(200)    not null            comment '日历名称',
    calendar             blob            not null            comment '存放持久化calendar对象',
    primary key (sched_name, calendar_name)
) engine=innodb comment = '日历信息表';

-- ----------------------------
-- 7、 存储已暂停的 Trigger 组的信息
-- ---------------------------- 
create table QRTZ_PAUSED_TRIGGER_GRPS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    primary key (sched_name, trigger_group)
) engine=innodb comment = '暂停的触发器表';

-- ----------------------------
-- 8、 存储与已触发的 Trigger 相关的状态信息，以及相联 Job 的执行信息
-- ---------------------------- 
create table QRTZ_FIRED_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    entry_id             varchar(95)     not null            comment '调度器实例id',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    instance_name        varchar(200)    not null            comment '调度器实例名',
    fired_time           bigint(13)      not null            comment '触发的时间',
    sched_time           bigint(13)      not null            comment '定时器制定的时间',
    priority             integer         not null            comment '优先级',
    state                varchar(16)     not null            comment '状态',
    job_name             varchar(200)    null                comment '任务名称',
    job_group            varchar(200)    null                comment '任务组名',
    is_nonconcurrent     varchar(1)      null                comment '是否并发',
    requests_recovery    varchar(1)      null                comment '是否接受恢复执行',
    primary key (sched_name, entry_id)
) engine=innodb comment = '已触发的触发器表';

-- ----------------------------
-- 9、 存储少量的有关 Scheduler 的状态信息，假如是用于集群中，可以看到其他的 Scheduler 实例
-- ---------------------------- 
create table QRTZ_SCHEDULER_STATE (
    sched_name           varchar(120)    not null            comment '调度名称',
    instance_name        varchar(200)    not null            comment '实例名称',
    last_checkin_time    bigint(13)      not null            comment '上次检查时间',
    checkin_interval     bigint(13)      not null            comment '检查间隔时间',
    primary key (sched_name, instance_name)
) engine=innodb comment = '调度器状态表';

-- ----------------------------
-- 10、 存储程序的悲观锁的信息(假如使用了悲观锁)
-- ---------------------------- 
create table QRTZ_LOCKS (
    sched_name           varchar(120)    not null            comment '调度名称',
    lock_name            varchar(40)     not null            comment '悲观锁名称',
    primary key (sched_name, lock_name)
) engine=innodb comment = '存储的悲观锁信息表';

-- ----------------------------
-- 11、 Quartz集群实现同步机制的行锁表
-- ---------------------------- 
create table QRTZ_SIMPROP_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    str_prop_1           varchar(512)    null                comment 'String类型的trigger的第一个参数',
    str_prop_2           varchar(512)    null                comment 'String类型的trigger的第二个参数',
    str_prop_3           varchar(512)    null                comment 'String类型的trigger的第三个参数',
    int_prop_1           int             null                comment 'int类型的trigger的第一个参数',
    int_prop_2           int             null                comment 'int类型的trigger的第二个参数',
    long_prop_1          bigint          null                comment 'long类型的trigger的第一个参数',
    long_prop_2          bigint          null                comment 'long类型的trigger的第二个参数',
    dec_prop_1           numeric(13,4)   null                comment 'decimal类型的trigger的第一个参数',
    dec_prop_2           numeric(13,4)   null                comment 'decimal类型的trigger的第二个参数',
    bool_prop_1          varchar(1)      null                comment 'Boolean类型的trigger的第一个参数',
    bool_prop_2          varchar(1)      null                comment 'Boolean类型的trigger的第二个参数',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = '同步机制的行锁表';

commit;


-- ============================================================
-- File: performance_indexes.sql
-- ============================================================

-- ============================================
-- RuoYi 数据库性能索引优化
-- 针对 2核4G 服务器，增加常用查询字段索引
-- ============================================

-- 1、sys_user 表索引
CREATE INDEX idx_sys_user_status ON sys_user(status);
CREATE INDEX idx_sys_user_dept_id ON sys_user(dept_id);
CREATE INDEX idx_sys_user_phonenumber ON sys_user(phonenumber);

-- 2、sys_logininfor 表索引
CREATE INDEX idx_sys_logininfor_uname ON sys_logininfor(user_name);

-- 3、sys_oper_log 表索引
CREATE INDEX idx_sys_oper_log_oname ON sys_oper_log(oper_name);

-- 4、sys_job_log 表索引
CREATE INDEX idx_sys_job_log_ctime ON sys_job_log(create_time);



-- ============================================================
-- File: role_templates.sql
-- ============================================================

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



-- ============================================================
-- File: menu_hide_tech.sql
-- ============================================================

-- =====================================================
-- 技术菜单隐藏（菜单分层改造）
-- 说明：将技术性菜单对非管理员用户隐藏，提升非IT人员使用体验
--       使用 sys_menu 表的 visible 字段控制（0=显示 1=隐藏）
-- =====================================================

-- 1. 隐藏"系统工具"目录（父菜单）
UPDATE sys_menu SET visible = '1' WHERE menu_id = 3;

-- 2. 隐藏"代码生成"及其所有子按钮
UPDATE sys_menu SET visible = '1' WHERE menu_id = 116;
UPDATE sys_menu SET visible = '1' WHERE menu_id IN (1055, 1056, 1057, 1058, 1059, 1060);

-- 3. 隐藏"表单构建"
UPDATE sys_menu SET visible = '1' WHERE menu_id = 115;

-- 4. 隐藏"定时任务"及其所有子按钮
UPDATE sys_menu SET visible = '1' WHERE menu_id = 110;
UPDATE sys_menu SET visible = '1' WHERE menu_id IN (1049, 1050, 1051, 1052, 1053, 1054);

-- 5. 隐藏"系统接口"（Swagger，仅开发环境使用）
UPDATE sys_menu SET visible = '1' WHERE menu_id = 117;

-- 6. 隐藏"数据监控"（Druid监控，技术性太强）
UPDATE sys_menu SET visible = '1' WHERE menu_id = 111;



-- ============================================================
-- File: sys_data_change_log.sql
-- ============================================================

-- ----------------------------
-- 数据变更日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_data_change_log`;
CREATE TABLE `sys_data_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `entity_name` varchar(100) NOT NULL COMMENT '实体名称',
  `entity_id` varchar(50) NOT NULL COMMENT '实体ID',
  `field_name` varchar(100) DEFAULT NULL COMMENT '字段名称',
  `old_value` text COMMENT '旧值',
  `new_value` text COMMENT '新值',
  `change_type` varchar(20) NOT NULL COMMENT '变更类型 INSERT/UPDATE/DELETE',
  `operate_by` varchar(64) DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_entity_name_id` (`entity_name`,`entity_id`),
  KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据变更日志表';



-- ============================================================
-- File: sys_undo_log.sql
-- ============================================================

-- =====================================================
-- 操作撤销日志表（数据恢复支持）
-- 说明：在支持撤销的操作执行前保存数据快照，允许在有效期内撤销操作
-- =====================================================
drop table if exists sys_undo_log;
CREATE TABLE `sys_undo_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `entity_type` varchar(100) NOT NULL COMMENT '实体类型（表名）',
  `entity_id` varchar(50) NOT NULL COMMENT '实体ID',
  `data_snapshot` longtext COMMENT '数据快照(JSON格式)',
  `operate_type` varchar(20) NOT NULL COMMENT '操作类型（INSERT/UPDATE/DELETE）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '操作人',
  `create_time` datetime DEFAULT NULL COMMENT '操作时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间(默认保留24小时)',
  PRIMARY KEY (`id`),
  KEY `idx_entity` (`entity_type`,`entity_id`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_create_by` (`create_by`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='操作撤销日志表';



-- ============================================================
-- File: logininfor_upgrade.sql
-- ============================================================

-- ----------------------------
-- 登录日志表增加登录设备类型字段
-- ----------------------------
ALTER TABLE `sys_logininfor` ADD COLUMN `login_device` varchar(20) DEFAULT '' COMMENT '登录设备类型（PC/手机/平板）' AFTER `os`;



-- ============================================================
-- File: wms_all.sql
-- ============================================================

-- =============================================
-- WMS 仓库管理系统 - 全量SQL脚本
-- 包含：16张业务表 + 14个字典类型 + 菜单权限
-- =============================================

-- =============================================
-- 一、业务表 DDL（16张表）
-- =============================================

-- 1. 物料主数据
DROP TABLE IF EXISTS wms_material;
CREATE TABLE wms_material (
    material_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '物料ID',
    material_code    VARCHAR(64)  NOT NULL                 COMMENT '物料编码（唯一标识）',
    material_name    VARCHAR(255) NOT NULL                 COMMENT '物料名称',
    material_type    VARCHAR(20)  DEFAULT '0'              COMMENT '物料类型（字典 wms_material_type）',
    spec_model       VARCHAR(255)                          COMMENT '规格型号',
    unit             VARCHAR(20)  NOT NULL                 COMMENT '基本计量单位（字典 wms_unit）',
    barcode          VARCHAR(64)                           COMMENT '物料条码',
    shelf_life_days  INT                                   COMMENT '保质期天数',
    expiry_alert_days INT          DEFAULT 30               COMMENT '临期预警天数',
    safety_stock_min DECIMAL(18,2) DEFAULT 0               COMMENT '安全库存下限',
    safety_stock_max DECIMAL(18,2) DEFAULT 0               COMMENT '安全库存上限',
    is_expiry_manage CHAR(1)     DEFAULT '0'              COMMENT '是否效期管理（0否 1是）',
    is_batch_manage  CHAR(1)      DEFAULT '0'              COMMENT '是否批次管理（0否 1是）',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (material_id),
    UNIQUE KEY uk_material_code (material_code),
    KEY idx_material_type (material_type),
    KEY idx_material_name (material_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料主数据表';

-- 2. 供应商（WMS/PMS共用）
DROP TABLE IF EXISTS wms_supplier;
CREATE TABLE wms_supplier (
    supplier_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '供应商ID',
    supplier_code        VARCHAR(64)  NOT NULL                 COMMENT '供应商编码',
    supplier_name        VARCHAR(255) NOT NULL                 COMMENT '供应商名称',
    supplier_short_name  VARCHAR(128)                          COMMENT '供应商简称',
    supplier_type        VARCHAR(20)  DEFAULT '0'              COMMENT '供应商类型（字典 wms_supplier_type）',
    supplier_level       VARCHAR(10)                           COMMENT '供应商等级（字典 wms_supplier_level）',
    unified_credit_code  VARCHAR(64)                           COMMENT '统一社会信用代码',
    contact_person       VARCHAR(64)                           COMMENT '联系人',
    contact_phone        VARCHAR(20)                           COMMENT '联系电话',
    email                VARCHAR(128)                          COMMENT '邮箱',
    fax                  VARCHAR(20)                           COMMENT '传真',
    website              VARCHAR(255)                          COMMENT '网址',
    postcode             VARCHAR(10)                           COMMENT '邮政编码',
    address              VARCHAR(500)                          COMMENT '地址',
    legal_person         VARCHAR(64)                           COMMENT '法人代表',
    registered_capital   DECIMAL(18,2) DEFAULT 0               COMMENT '注册资本（万元）',
    enterprise_nature    VARCHAR(20)                           COMMENT '企业性质（字典 wms_enterprise_nature）',
    business_scope       VARCHAR(1000)                         COMMENT '经营范围',
    established_date     DATE                                  COMMENT '成立日期',
    bank_name            VARCHAR(128)                          COMMENT '开户银行',
    bank_branch          VARCHAR(128)                          COMMENT '开户支行',
    bank_account         VARCHAR(64)                           COMMENT '银行账号',
    tax_number           VARCHAR(64)                           COMMENT '税号',
    invoice_title        VARCHAR(255)                          COMMENT '发票抬头',
    invoice_address      VARCHAR(500)                          COMMENT '发票地址',
    invoice_phone        VARCHAR(20)                           COMMENT '发票联系电话',
    payment_method       VARCHAR(20)  DEFAULT '0'              COMMENT '付款方式（字典 wms_payment_method）',
    payment_days         INT          DEFAULT 0                COMMENT '账期天数',
    settlement_type      VARCHAR(20)  DEFAULT '0'              COMMENT '结算方式（字典 wms_settlement_type）',
    currency             VARCHAR(10)  DEFAULT 'CNY'            COMMENT '结算币种（字典 wms_currency）',
    delivery_cycle       INT                                   COMMENT '交货周期（天）',
    min_order_amount     DECIMAL(18,2) DEFAULT 0               COMMENT '最小订单金额',
    cooperation_date     DATE                                  COMMENT '合作开始日期',
    qualification_status CHAR(1)      DEFAULT '0'              COMMENT '资质状态（0未审核 1已审核 2审核不通过）',
    status               CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag             CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by            VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time          DATETIME                              COMMENT '创建时间',
    update_by            VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time          DATETIME                              COMMENT '更新时间',
    remark               VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (supplier_id),
    UNIQUE KEY uk_supplier_code (supplier_code),
    KEY idx_supplier_type (supplier_type),
    KEY idx_supplier_level (supplier_level),
    KEY idx_unified_credit_code (unified_credit_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表（WMS/PMS共用）';

-- 3. 仓库结构表（仓库/仓区/仓位 树形结构）
DROP TABLE IF EXISTS wms_warehouse;
CREATE TABLE wms_warehouse (
    warehouse_id     BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    parent_id        BIGINT       DEFAULT 0                COMMENT '父级ID',
    ancestors        VARCHAR(500)                          COMMENT '祖级列表',
    warehouse_code   VARCHAR(64)  NOT NULL                 COMMENT '编码（自动生成）',
    warehouse_name   VARCHAR(128) NOT NULL                 COMMENT '名称',
    node_type        CHAR(1)      DEFAULT '1'              COMMENT '节点类型（1=仓库 2=仓区 3=仓位）',
    dept_id          BIGINT                                COMMENT '管理部门ID（仓库级）',
    manager_id       BIGINT                                COMMENT '负责人用户ID（仓库级）',
    address          VARCHAR(500)                          COMMENT '仓库地址（仓库级）',
    area_type        VARCHAR(20)                           COMMENT '库区类型（仓区级，字典 wms_area_type）',
    location_type    VARCHAR(20)                           COMMENT '库位类型（仓位级，字典 wms_location_type）',
    capacity         DECIMAL(18,2) DEFAULT 0              COMMENT '库位容量（仓位级）',
    location_status  CHAR(1)      DEFAULT '0'              COMMENT '库位状态（仓位级，0空闲 1占用 2锁定）',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (warehouse_id),
    UNIQUE KEY uk_warehouse_code (warehouse_code),
    KEY idx_warehouse_parent (parent_id),
    KEY idx_warehouse_node_type (node_type),
    KEY idx_warehouse_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库结构表（仓库/仓区/仓位）';

-- 6. 入库单头表
DROP TABLE IF EXISTS wms_inbound_order;
CREATE TABLE wms_inbound_order (
    order_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '入库单ID',
    order_no         VARCHAR(64)  NOT NULL                 COMMENT '入库单号',
    order_type       VARCHAR(20)  DEFAULT '0'              COMMENT '入库类型（字典 wms_inbound_type）',
    supplier_id      BIGINT                                COMMENT '供应商ID',
    warehouse_id     BIGINT       NOT NULL                 COMMENT '目标仓库ID',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0草稿 1待收货 2待上架 3已完成 4已作废）',
    inbound_date     DATETIME                              COMMENT '预计入库日期',
    complete_date    DATETIME                              COMMENT '实际完成日期',
    total_qty        DECIMAL(18,2) DEFAULT 0               COMMENT '总数量',
    total_amount     DECIMAL(18,2) DEFAULT 0               COMMENT '总金额',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_inbound_order_no (order_no),
    KEY idx_inbound_status (status),
    KEY idx_inbound_warehouse (warehouse_id),
    KEY idx_inbound_supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单头表';

-- 7. 入库单明细表
DROP TABLE IF EXISTS wms_inbound_order_detail;
CREATE TABLE wms_inbound_order_detail (
    detail_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    order_id         BIGINT       NOT NULL                 COMMENT '入库单ID',
    material_id      BIGINT       NOT NULL                 COMMENT '物料ID',
    plan_qty         DECIMAL(18,2) DEFAULT 0               COMMENT '计划数量',
    received_qty     DECIMAL(18,2) DEFAULT 0               COMMENT '已收数量',
    putaway_qty      DECIMAL(18,2) DEFAULT 0               COMMENT '已上架数量',
    batch_no         VARCHAR(64)                           COMMENT '批次号',
    production_date  DATE                                  COMMENT '生产日期',
    expiry_date      DATE                                  COMMENT '保质期至',
    location_id      BIGINT                                COMMENT '上架库位ID',
    unit_price       DECIMAL(18,2) DEFAULT 0               COMMENT '单价',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (detail_id),
    KEY idx_inbound_detail_order (order_id),
    KEY idx_inbound_detail_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单明细表';

-- 8. 波次主表
DROP TABLE IF EXISTS wms_wave;
CREATE TABLE wms_wave (
    wave_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '波次ID',
    wave_no          VARCHAR(64)  NOT NULL                 COMMENT '波次号',
    wave_name        VARCHAR(128)                          COMMENT '波次名称',
    warehouse_id     BIGINT       NOT NULL                 COMMENT '仓库ID',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0待拣货 1拣货中 2已完成 3已作废）',
    total_qty        DECIMAL(18,2) DEFAULT 0               COMMENT '总数量',
    order_count      INT          DEFAULT 0                COMMENT '包含订单数',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (wave_id),
    UNIQUE KEY uk_wave_no (wave_no),
    KEY idx_wave_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='波次主表';

-- 9. 出库单头表
DROP TABLE IF EXISTS wms_outbound_order;
CREATE TABLE wms_outbound_order (
    order_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '出库单ID',
    order_no         VARCHAR(64)  NOT NULL                 COMMENT '出库单号',
    order_type       VARCHAR(20)  DEFAULT '0'              COMMENT '出库类型（字典 wms_outbound_type）',
    warehouse_id     BIGINT       NOT NULL                 COMMENT '出库仓库ID',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0草稿 1待拣货 2复核中 3已完成 4已作废）',
    wave_id          BIGINT                                COMMENT '所属波次ID',
    outbound_date    DATETIME                              COMMENT '预计出库日期',
    complete_date    DATETIME                              COMMENT '实际完成日期',
    total_qty        DECIMAL(18,2) DEFAULT 0               COMMENT '总数量',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_outbound_order_no (order_no),
    KEY idx_outbound_status (status),
    KEY idx_outbound_warehouse (warehouse_id),
    KEY idx_outbound_wave (wave_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单头表';

-- 10. 出库单明细表
DROP TABLE IF EXISTS wms_outbound_order_detail;
CREATE TABLE wms_outbound_order_detail (
    detail_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    order_id         BIGINT       NOT NULL                 COMMENT '出库单ID',
    material_id      BIGINT       NOT NULL                 COMMENT '物料ID',
    batch_no         VARCHAR(64)                           COMMENT '指定批次号',
    plan_qty         DECIMAL(18,2) DEFAULT 0               COMMENT '计划数量',
    actual_qty       DECIMAL(18,2) DEFAULT 0               COMMENT '实际复核数量',
    location_id      BIGINT                                COMMENT '拣货库位ID',
    pick_qty         DECIMAL(18,2) DEFAULT 0               COMMENT '已拣货数量',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (detail_id),
    KEY idx_outbound_detail_order (order_id),
    KEY idx_outbound_detail_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单明细表';

-- 11. 库存表
DROP TABLE IF EXISTS wms_inventory;
CREATE TABLE wms_inventory (
    inventory_id     BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '库存ID',
    material_id      BIGINT       NOT NULL                 COMMENT '物料ID',
    warehouse_id     BIGINT       NOT NULL                 COMMENT '仓库ID',
    location_id      BIGINT       NOT NULL                 COMMENT '库位ID',
    batch_no         VARCHAR(64)  DEFAULT ''               COMMENT '批次号',
    qty              DECIMAL(18,2) DEFAULT 0               COMMENT '可用数量',
    lock_qty         DECIMAL(18,2) DEFAULT 0               COMMENT '锁定数量',
    version          INT          DEFAULT 0                COMMENT '乐观锁版本号',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (inventory_id),
    UNIQUE KEY uk_inventory (material_id, warehouse_id, location_id, batch_no),
    KEY idx_inventory_material (material_id),
    KEY idx_inventory_warehouse (warehouse_id),
    KEY idx_inventory_location (location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 12. 库存流水表
DROP TABLE IF EXISTS wms_inventory_log;
CREATE TABLE wms_inventory_log (
    log_id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '流水ID',
    material_id      BIGINT       NOT NULL                 COMMENT '物料ID',
    warehouse_id     BIGINT       NOT NULL                 COMMENT '仓库ID',
    location_id      BIGINT                                COMMENT '库位ID',
    batch_no         VARCHAR(64)                           COMMENT '批次号',
    change_type      VARCHAR(20)  NOT NULL                 COMMENT '变动类型（字典 wms_change_type）',
    change_qty       DECIMAL(18,2) NOT NULL                COMMENT '变动数量（正增负减）',
    before_qty       DECIMAL(18,2) DEFAULT 0               COMMENT '变动前数量',
    after_qty        DECIMAL(18,2) DEFAULT 0               COMMENT '变动后数量',
    ref_order_type   VARCHAR(20)                           COMMENT '关联单据类型',
    ref_order_no     VARCHAR(64)                           COMMENT '关联业务单据号',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '操作人',
    create_time      DATETIME                              COMMENT '操作时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (log_id),
    KEY idx_log_material (material_id),
    KEY idx_log_warehouse (warehouse_id),
    KEY idx_log_ref_order (ref_order_no),
    KEY idx_log_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- 13. 盘点单头表
DROP TABLE IF EXISTS wms_stock_take;
CREATE TABLE wms_stock_take (
    take_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '盘点单ID',
    take_no          VARCHAR(64)  NOT NULL                 COMMENT '盘点单号',
    warehouse_id     BIGINT       NOT NULL                 COMMENT '盘点仓库ID',
    area_id          BIGINT                                COMMENT '盘点库区ID',
    take_type        VARCHAR(20)  DEFAULT '0'              COMMENT '盘点类型（字典 wms_take_type）',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0待盘点 1盘点中 2待审批 3已完成 4已作废）',
    plan_date        DATE                                  COMMENT '计划盘点日期',
    start_time       DATETIME                              COMMENT '盘点开始时间',
    end_time         DATETIME                              COMMENT '盘点结束时间',
    approve_by       VARCHAR(64)                           COMMENT '审批人',
    approve_time     DATETIME                              COMMENT '审批时间',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (take_id),
    UNIQUE KEY uk_take_no (take_no),
    KEY idx_take_status (status),
    KEY idx_take_warehouse (warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单头表';

-- 14. 盘点单明细表
DROP TABLE IF EXISTS wms_stock_take_detail;
CREATE TABLE wms_stock_take_detail (
    detail_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    take_id          BIGINT       NOT NULL                 COMMENT '盘点单ID',
    material_id      BIGINT       NOT NULL                 COMMENT '物料ID',
    location_id      BIGINT       NOT NULL                 COMMENT '库位ID',
    batch_no         VARCHAR(64)                           COMMENT '批次号',
    book_qty         DECIMAL(18,2) DEFAULT 0               COMMENT '账面数量',
    actual_qty       DECIMAL(18,2)                          COMMENT '实盘数量',
    diff_qty         DECIMAL(18,2) DEFAULT 0               COMMENT '差异数量',
    diff_reason      VARCHAR(500)                          COMMENT '差异原因',
    status           CHAR(1)      DEFAULT '0'              COMMENT '明细状态（0待盘 1已盘 2已确认）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (detail_id),
    KEY idx_take_detail_order (take_id),
    KEY idx_take_detail_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单明细表';

-- 15. 盘点快照表
DROP TABLE IF EXISTS wms_stock_take_snapshot;
CREATE TABLE wms_stock_take_snapshot (
    snapshot_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '快照ID',
    take_id          BIGINT       NOT NULL                 COMMENT '盘点单ID',
    material_id      BIGINT       NOT NULL                 COMMENT '物料ID',
    location_id      BIGINT       NOT NULL                 COMMENT '库位ID',
    batch_no         VARCHAR(64)                           COMMENT '批次号',
    snapshot_qty     DECIMAL(18,2) DEFAULT 0               COMMENT '快照数量',
    snapshot_time    DATETIME                              COMMENT '快照时间',
    PRIMARY KEY (snapshot_id),
    KEY idx_snapshot_take (take_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点快照表';

-- 16. 移库单表
DROP TABLE IF EXISTS wms_move_order;
CREATE TABLE wms_move_order (
    move_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '移库单ID',
    move_no          VARCHAR(64)  NOT NULL                 COMMENT '移库单号',
    warehouse_id     BIGINT       NOT NULL                 COMMENT '仓库ID',
    material_id      BIGINT       NOT NULL                 COMMENT '物料ID',
    batch_no         VARCHAR(64)                           COMMENT '批次号',
    from_location_id BIGINT       NOT NULL                 COMMENT '源库位ID',
    to_location_id   BIGINT       NOT NULL                 COMMENT '目标库位ID',
    move_qty         DECIMAL(18,2) NOT NULL                COMMENT '移库数量',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0待审批 1已批准 2已完成 3已作废）',
    approve_by       VARCHAR(64)                           COMMENT '审批人',
    approve_time     DATETIME                              COMMENT '审批时间',
    complete_time    DATETIME                              COMMENT '完成时间',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (move_id),
    UNIQUE KEY uk_move_no (move_no),
    KEY idx_move_status (status),
    KEY idx_move_warehouse (warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移库单表';


-- =============================================
-- 二、字典数据（21个字典类型）
-- =============================================

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('物料类型', 'wms_material_type', '0', 'admin', sysdate(), '物料类型字典'),
('计量单位', 'wms_unit', '0', 'admin', sysdate(), '计量单位字典'),
('库区类型', 'wms_area_type', '0', 'admin', sysdate(), '库区类型字典'),
('库位类型', 'wms_location_type', '0', 'admin', sysdate(), '库位类型字典'),
('库位状态', 'wms_location_status', '0', 'admin', sysdate(), '库位状态字典'),
('入库类型', 'wms_inbound_type', '0', 'admin', sysdate(), '入库类型字典'),
('入库状态', 'wms_inbound_status', '0', 'admin', sysdate(), '入库状态字典'),
('出库类型', 'wms_outbound_type', '0', 'admin', sysdate(), '出库类型字典'),
('出库状态', 'wms_outbound_status', '0', 'admin', sysdate(), '出库状态字典'),
('波次状态', 'wms_wave_status', '0', 'admin', sysdate(), '波次状态字典'),
('库存变动类型', 'wms_change_type', '0', 'admin', sysdate(), '库存变动类型字典'),
('盘点类型', 'wms_take_type', '0', 'admin', sysdate(), '盘点类型字典'),
('盘点状态', 'wms_take_status', '0', 'admin', sysdate(), '盘点状态字典'),
('移库状态', 'wms_move_status', '0', 'admin', sysdate(), '移库状态字典'),
('供应商类型', 'wms_supplier_type', '0', 'admin', sysdate(), '供应商类型字典'),
('供应商等级', 'wms_supplier_level', '0', 'admin', sysdate(), '供应商等级字典'),
('企业性质', 'wms_enterprise_nature', '0', 'admin', sysdate(), '企业性质字典'),
('付款方式', 'wms_payment_method', '0', 'admin', sysdate(), '付款方式字典'),
('结算方式', 'wms_settlement_type', '0', 'admin', sysdate(), '结算方式字典'),
('结算币种', 'wms_currency', '0', 'admin', sysdate(), '结算币种字典'),
('资质状态', 'wms_qualification_status', '0', 'admin', sysdate(), '供应商资质状态字典');

-- 字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '原材料', '0', 'wms_material_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '半成品', '1', 'wms_material_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(3, '成品', '2', 'wms_material_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '辅料', '3', 'wms_material_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),

(1, '个', '0', 'wms_unit', '', '', 'Y', '0', 'admin', sysdate(), ''),
(2, '件', '1', 'wms_unit', '', '', 'N', '0', 'admin', sysdate(), ''),
(3, '箱', '2', 'wms_unit', '', '', 'N', '0', 'admin', sysdate(), ''),
(4, 'kg', '3', 'wms_unit', '', '', 'N', '0', 'admin', sysdate(), ''),
(5, '吨', '4', 'wms_unit', '', '', 'N', '0', 'admin', sysdate(), ''),
(6, '米', '5', 'wms_unit', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '存储区', '0', 'wms_area_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '收货区', '1', 'wms_area_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '发货区', '2', 'wms_area_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '退货区', '3', 'wms_area_type', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(5, '暂存区', '4', 'wms_area_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '货架位', '0', 'wms_location_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '地堆位', '1', 'wms_location_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(3, '暂存位', '2', 'wms_location_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),

(1, '空闲', '0', 'wms_location_status', '', 'success', 'Y', '0', 'admin', sysdate(), ''),
(2, '占用', '1', 'wms_location_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '锁定', '2', 'wms_location_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '采购入库', '0', 'wms_inbound_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '生产入库', '1', 'wms_inbound_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '退货入库', '2', 'wms_inbound_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '调拨入库', '3', 'wms_inbound_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '草稿', '0', 'wms_inbound_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '待收货', '1', 'wms_inbound_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '待上架', '2', 'wms_inbound_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '已完成', '3', 'wms_inbound_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(5, '已作废', '4', 'wms_inbound_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '销售出库', '0', 'wms_outbound_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '领料出库', '1', 'wms_outbound_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '调拨出库', '2', 'wms_outbound_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '退货出库', '3', 'wms_outbound_type', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '草稿', '0', 'wms_outbound_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '待拣货', '1', 'wms_outbound_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '复核中', '2', 'wms_outbound_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '已完成', '3', 'wms_outbound_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(5, '已作废', '4', 'wms_outbound_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '待拣货', '0', 'wms_wave_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '拣货中', '1', 'wms_wave_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '已完成', '2', 'wms_wave_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '已作废', '3', 'wms_wave_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '入库', '0', 'wms_change_type', '', 'success', 'Y', '0', 'admin', sysdate(), ''),
(2, '出库', '1', 'wms_change_type', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(3, '移库入', '2', 'wms_change_type', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(4, '移库出', '3', 'wms_change_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(5, '盘盈', '4', 'wms_change_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(6, '盘亏', '5', 'wms_change_type', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(7, '锁定', '6', 'wms_change_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(8, '解锁', '7', 'wms_change_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '全盘', '0', 'wms_take_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '抽盘', '1', 'wms_take_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '循环盘', '2', 'wms_take_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '待盘点', '0', 'wms_take_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '盘点中', '1', 'wms_take_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '待审批', '2', 'wms_take_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '已完成', '3', 'wms_take_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(5, '已作废', '4', 'wms_take_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '待审批', '0', 'wms_move_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '已批准', '1', 'wms_move_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '已完成', '2', 'wms_move_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '已作废', '3', 'wms_move_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '厂商', '0', 'wms_supplier_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '经销商', '1', 'wms_supplier_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '代理商', '2', 'wms_supplier_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '服务商', '3', 'wms_supplier_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, 'A级（战略）', 'A', 'wms_supplier_level', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(2, 'B级（优秀）', 'B', 'wms_supplier_level', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, 'C级（合格）', 'C', 'wms_supplier_level', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, 'D级（待改进）', 'D', 'wms_supplier_level', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '国有企业', '0', 'wms_enterprise_nature', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(2, '民营企业', '1', 'wms_enterprise_nature', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '外资企业', '2', 'wms_enterprise_nature', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '合资企业', '3', 'wms_enterprise_nature', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(5, '个体工商户', '4', 'wms_enterprise_nature', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '现结', '0', 'wms_payment_method', '', 'success', 'Y', '0', 'admin', sysdate(), ''),
(2, '月结', '1', 'wms_payment_method', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '预付', '2', 'wms_payment_method', '', 'warning', 'N', '0', 'admin', sysdate(), ''),

(1, '银行转账', '0', 'wms_settlement_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '承兑汇票', '1', 'wms_settlement_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '现金', '2', 'wms_settlement_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),

(1, '人民币', 'CNY', 'wms_currency', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '美元', 'USD', 'wms_currency', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '欧元', 'EUR', 'wms_currency', '', 'warning', 'N', '0', 'admin', sysdate(), ''),

(1, '未审核', '0', 'wms_qualification_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '已审核', '1', 'wms_qualification_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '审核不通过', '2', 'wms_qualification_status', '', 'danger', 'N', '0', 'admin', sysdate(), '');


-- =============================================
-- 三、菜单权限（WMS菜单树）
-- =============================================

-- 顶级目录：WMS仓储管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2000, 'WMS仓储管理', 0, 5, 'wms', NULL, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), 'WMS仓储管理目录');

-- 二级目录：基础数据
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2001, '基础数据', 2000, 1, 'base', NULL, '', '', 1, 0, 'M', '0', '0', '', 'dict', 'admin', sysdate(), '');

-- 基础数据-菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2010, '物料管理', 2001, 1, 'material', 'wms/material/index', '', 'WmsMaterial', 1, 0, 'C', '0', '0', 'wms:material:list', 'component', 'admin', sysdate(), ''),
(2020, '供应商管理', 2001, 2, 'supplier', 'wms/supplier/index', '', 'WmsSupplier', 1, 0, 'C', '0', '0', 'wms:supplier:list', 'people', 'admin', sysdate(), ''),
(2030, '仓库管理', 2001, 3, 'warehouse', 'wms/warehouse/index', '', 'WmsWarehouse', 1, 0, 'C', '0', '0', 'wms:warehouse:list', 'build', 'admin', sysdate(), '');

-- 物料管理按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2011, '物料查询', 2010, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:material:query', '#', 'admin', sysdate(), ''),
(2012, '物料新增', 2010, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:material:add', '#', 'admin', sysdate(), ''),
(2013, '物料修改', 2010, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:material:edit', '#', 'admin', sysdate(), ''),
(2014, '物料删除', 2010, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:material:remove', '#', 'admin', sysdate(), ''),
(2015, '物料导出', 2010, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:material:export', '#', 'admin', sysdate(), '');

-- 供应商按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2021, '供应商查询', 2020, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:supplier:query', '#', 'admin', sysdate(), ''),
(2022, '供应商新增', 2020, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:supplier:add', '#', 'admin', sysdate(), ''),
(2023, '供应商修改', 2020, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:supplier:edit', '#', 'admin', sysdate(), ''),
(2024, '供应商删除', 2020, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:supplier:remove', '#', 'admin', sysdate(), ''),
(2025, '供应商导出', 2020, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:supplier:export', '#', 'admin', sysdate(), '');

-- 仓库按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2031, '仓库查询', 2030, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:warehouse:query', '#', 'admin', sysdate(), ''),
(2032, '仓库新增', 2030, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:warehouse:add', '#', 'admin', sysdate(), ''),
(2033, '仓库修改', 2030, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:warehouse:edit', '#', 'admin', sysdate(), ''),
(2034, '仓库删除', 2030, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:warehouse:remove', '#', 'admin', sysdate(), ''),
(2035, '仓库导出', 2030, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:warehouse:export', '#', 'admin', sysdate(), ''),
(2036, '仓区查询', 2030, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:area:list', '#', 'admin', sysdate(), ''),
(2037, '库位查询', 2030, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:location:list', '#', 'admin', sysdate(), '');

-- 二级目录：入库管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2002, '入库管理', 2000, 2, 'inbound', NULL, '', '', 1, 0, 'M', '0', '0', '', 'log-in', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2060, '入库单管理', 2002, 1, 'order', 'wms/inbound/index', '', 'WmsInboundOrder', 1, 0, 'C', '0', '0', 'wms:inbound:list', 'list', 'admin', sysdate(), ''),
(2061, '入库作业', 2002, 2, 'detail', 'wms/inbound/detail', '', 'WmsInboundDetail', 1, 0, 'C', '0', '0', 'wms:inbound:detail', 'edit', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2062, '入库单查询', 2060, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inbound:query', '#', 'admin', sysdate(), ''),
(2063, '入库单新增', 2060, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inbound:add', '#', 'admin', sysdate(), ''),
(2064, '入库单修改', 2060, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inbound:edit', '#', 'admin', sysdate(), ''),
(2065, '入库单删除', 2060, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inbound:remove', '#', 'admin', sysdate(), ''),
(2066, '入库单导出', 2060, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inbound:export', '#', 'admin', sysdate(), '');

-- 二级目录：出库管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2003, '出库管理', 2000, 3, 'outbound', NULL, '', '', 1, 0, 'M', '0', '0', '', 'log-out', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2070, '出库单管理', 2003, 1, 'order', 'wms/outbound/index', '', 'WmsOutboundOrder', 1, 0, 'C', '0', '0', 'wms:outbound:list', 'list', 'admin', sysdate(), ''),
(2071, '出库作业', 2003, 2, 'detail', 'wms/outbound/detail', '', 'WmsOutboundDetail', 1, 0, 'C', '0', '0', 'wms:outbound:detail', 'edit', 'admin', sysdate(), ''),
(2072, '扫码复核', 2003, 3, 'check', 'wms/outbound/check', '', 'WmsOutboundCheck', 1, 0, 'C', '1', '0', 'wms:outbound:check', 'scan', 'admin', sysdate(), ''),
(2073, '波次管理', 2003, 4, 'wave', 'wms/outbound/wave', '', 'WmsOutboundWave', 1, 0, 'C', '1', '0', 'wms:wave:list', 'group', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2074, '出库单查询', 2070, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:outbound:query', '#', 'admin', sysdate(), ''),
(2075, '出库单新增', 2070, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:outbound:add', '#', 'admin', sysdate(), ''),
(2076, '出库单修改', 2070, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:outbound:edit', '#', 'admin', sysdate(), ''),
(2077, '出库单删除', 2070, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:outbound:remove', '#', 'admin', sysdate(), ''),
(2078, '出库单导出', 2070, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:outbound:export', '#', 'admin', sysdate(), '');

-- 二级目录：库存管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2004, '库存管理', 2000, 4, 'inv', NULL, '', '', 1, 0, 'M', '0', '0', '', 'storage', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2080, '库存查询', 2004, 1, 'list', 'wms/inventory/index', '', 'WmsInventoryList', 1, 0, 'C', '0', '0', 'wms:inventory:list', 'list', 'admin', sysdate(), ''),
(2081, '库存流水', 2004, 2, 'log', 'wms/log/index', '', 'WmsInventoryLog', 1, 0, 'C', '0', '0', 'wms:log:list', 'time', 'admin', sysdate(), ''),
(2120, '库存预警', 2004, 3, 'alert', 'wms/stockAlert/index', '', 'WmsStockAlert', 1, 0, 'C', '0', '0', 'wms:stockAlert:list', 'bug', 'admin', sysdate(), '库存预警管理（库存不足/积压/临期/过期）');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2082, '库存查询', 2080, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inventory:query', '#', 'admin', sysdate(), ''),
(2083, '库存导出', 2080, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inventory:export', '#', 'admin', sysdate(), ''),
(2084, '流水查询', 2081, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:log:query', '#', 'admin', sysdate(), ''),
(2085, '流水导出', 2081, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:log:export', '#', 'admin', sysdate(), ''),
(2121, '预警查询', 2120, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stockAlert:query', '#', 'admin', sysdate(), ''),
(2122, '预警导出', 2120, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stockAlert:export', '#', 'admin', sysdate(), ''),
(2123, '库存删除', 2080, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inventory:remove', '#', 'admin', sysdate(), ''),
(2124, '流水删除', 2081, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:log:remove', '#', 'admin', sysdate(), ''),
(2125, '预警删除', 2120, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stockAlert:remove', '#', 'admin', sysdate(), '');

-- 二级目录：盘点管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2005, '盘点管理', 2000, 5, 'stocktake', NULL, '', '', 1, 0, 'M', '0', '0', '', 'eye-open', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2090, '盘点任务', 2005, 1, 'task', 'wms/stocktake/index', '', 'WmsStocktakeTask', 1, 0, 'C', '0', '0', 'wms:stocktake:list', 'list', 'admin', sysdate(), ''),
(2091, '盘点作业', 2005, 2, 'detail', 'wms/stocktake/detail', '', 'WmsStocktakeDetail', 1, 0, 'C', '0', '0', 'wms:stocktake:detail', 'edit', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2092, '盘点查询', 2090, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:query', '#', 'admin', sysdate(), ''),
(2093, '盘点新增', 2090, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:add', '#', 'admin', sysdate(), ''),
(2094, '盘点修改', 2090, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:edit', '#', 'admin', sysdate(), ''),
(2095, '盘点删除', 2090, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:remove', '#', 'admin', sysdate(), '');

-- 移库管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2100, '移库管理', 2000, 6, 'move', 'wms/move/index', '', 'WmsMove', 1, 0, 'C', '0', '0', 'wms:move:list', 'swap', 'admin', sysdate(), '');

-- 统计报表
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2110, '统计报表', 2000, 7, 'statistics', 'wms/statistics/index', '', 'WmsStatistics', 1, 0, 'C', '0', '0', 'wms:statistics:list', 'chart', 'admin', sysdate(), '仓库管理统计报表');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2111, '统计查询', 2110, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:statistics:query', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2101, '移库查询', 2100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:move:query', '#', 'admin', sysdate(), ''),
(2102, '移库新增', 2100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:move:add', '#', 'admin', sysdate(), ''),
(2103, '移库修改', 2100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:move:edit', '#', 'admin', sysdate(), ''),
(2104, '移库删除', 2100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:move:remove', '#', 'admin', sysdate(), ''),
(2105, '移库导出', 2100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:move:export', '#', 'admin', sysdate(), '');

-- 给admin角色（role_id=1）分配所有WMS菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 2000), (1, 2001), (1, 2002), (1, 2003), (1, 2004), (1, 2005),
(1, 2010), (1, 2011), (1, 2012), (1, 2013), (1, 2014), (1, 2015),
(1, 2020), (1, 2021), (1, 2022), (1, 2023), (1, 2024), (1, 2025),
(1, 2030), (1, 2031), (1, 2032), (1, 2033), (1, 2034), (1, 2035), (1, 2036), (1, 2037),
(1, 2060), (1, 2061), (1, 2062), (1, 2063), (1, 2064), (1, 2065), (1, 2066),
(1, 2070), (1, 2071), (1, 2072), (1, 2073), (1, 2074), (1, 2075), (1, 2076), (1, 2077), (1, 2078),
(1, 2080), (1, 2081), (1, 2082), (1, 2083), (1, 2084), (1, 2085), (1, 2123), (1, 2124),
(1, 2090), (1, 2091), (1, 2092), (1, 2093), (1, 2094), (1, 2095),
(1, 2100), (1, 2101), (1, 2102), (1, 2103), (1, 2104), (1, 2105),
(1, 2110), (1, 2111),
(1, 2120), (1, 2121), (1, 2122), (1, 2125);



-- ============================================================
-- File: wms_statistics_menu.sql
-- ============================================================

-- =============================================
-- WMS 统计报表 - 菜单权限SQL
-- 在 WMS仓储管理 目录下新增"统计报表"菜单
-- =============================================

-- 统计报表菜单（C类型，直接挂在WMS仓储管理目录下）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2110, '统计报表', 2000, 7, 'statistics', 'wms/statistics/index', '', 'WmsStatistics', 1, 0, 'C', '0', '0', 'wms:statistics:list', 'chart', 'admin', sysdate(), '仓库管理统计报表');

-- 统计报表按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2111, '统计查询', 2110, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:statistics:query', '#', 'admin', sysdate(), '');

-- 给admin角色（role_id=1）分配统计报表菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 2110), (1, 2111);



-- ============================================================
-- File: wms_stock_alert_menu.sql
-- ============================================================

-- =============================================
-- 库存预警菜单及权限
-- 菜单ID：2120（页面）、2121（查询权限）、2122（导出权限）
-- 父菜单：2004（库存管理）
-- =============================================

-- 删除已存在的记录（防止重复执行）
DELETE FROM sys_role_menu WHERE menu_id IN (2120, 2121, 2122);
DELETE FROM sys_menu WHERE menu_id IN (2120, 2121, 2122);

-- 库存预警菜单（页面）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2120, '库存预警', 2004, 3, 'alert', 'wms/stockAlert/index', '', 'WmsStockAlert', 1, 0, 'C', '0', '0', 'wms:stockAlert:list', 'bug', 'admin', sysdate(), '库存预警管理（库存不足/积压/临期/过期）');

-- 库存预警功能权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2121, '预警查询', 2120, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stockAlert:query', '#', 'admin', sysdate(), ''),
(2122, '预警导出', 2120, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stockAlert:export', '#', 'admin', sysdate(), '');

-- 给admin角色（role_id=1）分配权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 2120), (1, 2121), (1, 2122);



-- ============================================================
-- File: dms_all.sql
-- ============================================================

-- =============================================
-- DMS 设备管理系统 - 全量SQL脚本
-- 包含：5张业务表 + 字典类型 + 菜单权限
-- =============================================

-- =============================================
-- 一、业务表 DDL（5张表）
-- =============================================

-- 1. 设备分类表（树形结构）
DROP TABLE IF EXISTS dms_equipment_category;
CREATE TABLE dms_equipment_category (
    category_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    parent_id        BIGINT       DEFAULT 0                COMMENT '父级ID',
    ancestors        VARCHAR(500)                          COMMENT '祖级列表',
    category_code    VARCHAR(64)  NOT NULL                 COMMENT '分类编码',
    category_name    VARCHAR(128) NOT NULL                 COMMENT '分类名称',
    category_level   INT          DEFAULT 1                COMMENT '分类层级（1-4级）',
    order_num        INT          DEFAULT 0                COMMENT '显示顺序',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (category_id),
    UNIQUE KEY uk_category_code (category_code),
    KEY idx_category_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备分类表（树形结构）';

-- 2. 设备台账表
DROP TABLE IF EXISTS dms_equipment;
CREATE TABLE dms_equipment (
    equipment_id     BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '设备ID',
equipment_code   VARCHAR(64)  NOT NULL                 COMMENT '设备编号（唯一标识）',
equipment_name   VARCHAR(255) NOT NULL                 COMMENT '设备名称',
asset_code       VARCHAR(128)                          COMMENT '资产编号',
    category_id      BIGINT                                COMMENT '设备分类ID',
    model            VARCHAR(128)                          COMMENT '型号',
    serial_number    VARCHAR(128)                          COMMENT '序列号',
    manufacturer     VARCHAR(255)                          COMMENT '制造商',
    supplier         VARCHAR(255)                          COMMENT '供应商',
    purchase_date    DATE                                  COMMENT '购置日期',
    original_value   DECIMAL(18,2)                         COMMENT '原值',
    dept_id          BIGINT                                COMMENT '使用部门ID',
    dept_name        VARCHAR(128)                          COMMENT '使用部门名称',
    install_location VARCHAR(255)                          COMMENT '安装位置',
equipment_status VARCHAR(10)  DEFAULT '0'              COMMENT '设备状态（字典 dms_equipment_status）',
equipment_level  VARCHAR(10)                           COMMENT '设备等级（字典 dms_equipment_level）',
warranty_date    DATE                                  COMMENT '质保期限',
responsible_id   BIGINT                                COMMENT '责任人ID',
    responsible_name VARCHAR(64)                           COMMENT '责任人名称',
equipment_image  VARCHAR(500)                          COMMENT '设备图片URL',
attachment_url   VARCHAR(1000)                         COMMENT '设备附件资料URL',
status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (equipment_id),
    UNIQUE KEY uk_equipment_code (equipment_code),
    KEY idx_equipment_category (category_id),
    KEY idx_equipment_status (equipment_status),
    KEY idx_equipment_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备台账表';

-- 3. 工单管理表
DROP TABLE IF EXISTS dms_work_order;
CREATE TABLE dms_work_order (
    order_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '工单ID',
    order_no         VARCHAR(64)  NOT NULL                 COMMENT '工单号',
    equipment_id     BIGINT       NOT NULL                 COMMENT '设备ID',
    equipment_code   VARCHAR(64)                           COMMENT '设备编号（冗余）',
    equipment_name   VARCHAR(255)                          COMMENT '设备名称（冗余）',
    order_type       VARCHAR(10)  DEFAULT '0'              COMMENT '工单类型（字典 dms_order_type）',
    fault_description VARCHAR(1000)                        COMMENT '故障现象/任务描述',
    reporter_id      BIGINT                                COMMENT '报修人ID',
    reporter_name    VARCHAR(64)                           COMMENT '报修人名称',
    report_time      DATETIME                              COMMENT '报修时间',
    priority         VARCHAR(10)  DEFAULT '2'              COMMENT '优先级（字典 dms_priority）',
    order_status     VARCHAR(10)  DEFAULT '0'              COMMENT '工单状态（字典 dms_order_status）',
    assignee_id      BIGINT                                COMMENT '维修人ID',
    assignee_name    VARCHAR(64)                           COMMENT '维修人名称',
    assign_time      DATETIME                              COMMENT '派工时间',
    arrive_time      DATETIME                              COMMENT '到达时间',
    fault_cause      VARCHAR(1000)                         COMMENT '故障原因',
    repair_measure   VARCHAR(1000)                         COMMENT '维修措施',
    spare_parts_used VARCHAR(500)                          COMMENT '更换备件',
    repair_cost      DECIMAL(12,2)                         COMMENT '维修费用（元）',
    downtime_duration DECIMAL(10,2)                        COMMENT '停机时长（小时）',
    complete_time    DATETIME                              COMMENT '完工时间',
    complete_remark  VARCHAR(1000)                         COMMENT '完工说明',
    assigner_id      BIGINT                                COMMENT '派工人ID',
    assigner_name    VARCHAR(64)                           COMMENT '派工人名称',
    verifier_id      BIGINT                                COMMENT '验收人ID',
    verifier_name    VARCHAR(64)                           COMMENT '验收人名称',
    verify_time      DATETIME                              COMMENT '验收时间',
    verify_opinion   VARCHAR(500)                          COMMENT '验收意见',
    rating           INT                                   COMMENT '评价（1-5星）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_equipment (equipment_id),
    KEY idx_order_status (order_status),
    KEY idx_order_type (order_type),
    KEY idx_order_assignee (assignee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单管理表';

-- 4. 备件管理表
DROP TABLE IF EXISTS dms_spare_part;
CREATE TABLE dms_spare_part (
    part_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '备件ID',
    part_code        VARCHAR(64)  NOT NULL                 COMMENT '备件编号',
    part_name        VARCHAR(255) NOT NULL                 COMMENT '备件名称',
    spec             VARCHAR(255)                          COMMENT '规格',
    model            VARCHAR(128)                          COMMENT '型号',
    unit             VARCHAR(20)  DEFAULT '个'             COMMENT '单位',
    applicable_category VARCHAR(500)                       COMMENT '适用设备分类（JSON数组）',
    stock_min        DECIMAL(18,2) DEFAULT 0               COMMENT '库存下限',
    stock_max        DECIMAL(18,2) DEFAULT 0               COMMENT '库存上限',
    safety_stock     DECIMAL(18,2) DEFAULT 0               COMMENT '安全库存',
    current_stock    DECIMAL(18,2) DEFAULT 0               COMMENT '当前库存',
    storage_location VARCHAR(255)                          COMMENT '存放位置',
    supplier         VARCHAR(255)                          COMMENT '供应商',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (part_id),
    UNIQUE KEY uk_part_code (part_code),
    KEY idx_part_name (part_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备件管理表';

-- 5. PM预防性维护计划表
DROP TABLE IF EXISTS dms_pm_plan;
CREATE TABLE dms_pm_plan (
    plan_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '计划ID',
    plan_name        VARCHAR(255) NOT NULL                 COMMENT '计划名称',
    equipment_id     BIGINT                                COMMENT '设备ID（为空则按分类）',
    category_id      BIGINT                                COMMENT '设备分类ID',
    trigger_type     VARCHAR(10)  DEFAULT '0'              COMMENT '触发类型（字典 dms_pm_trigger_type）',
    cycle_value      INT                                   COMMENT '周期值',
    cycle_unit       VARCHAR(20)                           COMMENT '周期单位（天/月/小时/件）',
    task_list        TEXT                                  COMMENT '任务清单（JSON数组）',
    spare_part_list  TEXT                                  COMMENT '备件清单（JSON数组）',
    estimated_hours  DECIMAL(10,2)                         COMMENT '预估工时',
    advance_days     INT          DEFAULT 3                COMMENT '提前生成天数',
    auto_assign      CHAR(1)      DEFAULT '0'              COMMENT '是否自动派工（0否 1是）',
    last_execute_time DATETIME                             COMMENT '上次执行时间',
    next_execute_time DATETIME                             COMMENT '下次执行时间',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (plan_id),
    KEY idx_pm_equipment (equipment_id),
    KEY idx_pm_category (category_id),
    KEY idx_pm_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PM预防性维护计划表';


-- =============================================
-- 二、字典类型与字典数据
-- =============================================

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('设备状态', 'dms_equipment_status', '0', 'admin', sysdate(), '设备台账状态'),
('设备等级', 'dms_equipment_level', '0', 'admin', sysdate(), '设备重要等级'),
('工单类型', 'dms_order_type', '0', 'admin', sysdate(), '维护工单类型'),
('工单状态', 'dms_order_status', '0', 'admin', sysdate(), '工单流转状态'),
('优先级', 'dms_priority', '0', 'admin', sysdate(), '工单优先级'),
('PM触发类型', 'dms_pm_trigger_type', '0', 'admin', sysdate(), '预防性维护触发类型');

-- 字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '在用', '0', 'dms_equipment_status', '', 'success', 'Y', '0', 'admin', sysdate(), '正常运行'),
(2, '闲置', '1', 'dms_equipment_status', '', 'info', 'N', '0', 'admin', sysdate(), '可用但未使用'),
(3, '维修中', '2', 'dms_equipment_status', '', 'warning', 'N', '0', 'admin', sysdate(), '正在处理工单'),
(4, '停机', '3', 'dms_equipment_status', '', 'danger', 'N', '0', 'admin', sysdate(), '因外部原因停机'),
(5, '故障', '4', 'dms_equipment_status', '', 'danger', 'N', '0', 'admin', sysdate(), '等待维修'),
(6, '报废', '5', 'dms_equipment_status', '', 'info', 'N', '0', 'admin', sysdate(), '已退出使用'),

(1, '关键设备', '0', 'dms_equipment_level', '', 'danger', 'N', '0', 'admin', sysdate(), 'A类-关键设备，故障影响生产主线'),
(2, '重要设备', '1', 'dms_equipment_level', '', 'warning', 'N', '0', 'admin', sysdate(), 'B类-重要设备，故障影响部分生产'),
(3, '一般设备', '2', 'dms_equipment_level', '', 'success', 'Y', '0', 'admin', sysdate(), 'C类-一般设备，故障影响较小'),
(4, '辅助设备', '3', 'dms_equipment_level', '', 'info', 'N', '0', 'admin', sysdate(), 'D类-辅助设备，故障不影响生产'),

(1, '故障报修', '0', 'dms_order_type', '', 'danger', 'Y', '0', 'admin', sysdate(), '操作员/主管发起'),
(2, 'PM维护', '1', 'dms_order_type', '', 'primary', 'N', '0', 'admin', sysdate(), '系统自动生成'),
(3, '点检整改', '2', 'dms_order_type', '', 'warning', 'N', '0', 'admin', sysdate(), '点检异常转入'),
(4, '临时任务', '3', 'dms_order_type', '', 'info', 'N', '0', 'admin', sysdate(), '管理员指派'),
(5, '改造安装', '4', 'dms_order_type', '', 'success', 'N', '0', 'admin', sysdate(), '专项项目'),

(1, '新建(待派)', '0', 'dms_order_status', '', 'info', 'Y', '0', 'admin', sysdate(), '新建待派工'),
(2, '已派工', '1', 'dms_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已派工待接单'),
(3, '已接单', '2', 'dms_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已接单待处理'),
(4, '处理中', '3', 'dms_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), '正在处理'),
(5, '已完成', '4', 'dms_order_status', '', 'success', 'N', '0', 'admin', sysdate(), '完工待验收'),
(6, '已验收', '5', 'dms_order_status', '', 'success', 'N', '0', 'admin', sysdate(), '验收通过'),
(7, '已撤销', '6', 'dms_order_status', '', 'info', 'N', '0', 'admin', sysdate(), '已撤销'),
(8, '驳回重做', '7', 'dms_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), '验收驳回'),

(1, '紧急', '0', 'dms_priority', '', 'danger', 'N', '0', 'admin', sysdate(), '影响安全/全线停机'),
(2, '高', '1', 'dms_priority', '', 'warning', 'N', '0', 'admin', sysdate(), '关键设备故障'),
(3, '中', '2', 'dms_priority', '', 'primary', 'Y', '0', 'admin', sysdate(), '一般故障'),
(4, '低', '3', 'dms_priority', '', 'info', 'N', '0', 'admin', sysdate(), '计划性/改进类'),

(1, '周期(时间)', '0', 'dms_pm_trigger_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '按固定时间间隔'),
(2, '周期(运行时长)', '1', 'dms_pm_trigger_type', '', 'warning', 'N', '0', 'admin', sysdate(), '累计运行小时数'),
(3, '周期(加工量)', '2', 'dms_pm_trigger_type', '', 'info', 'N', '0', 'admin', sysdate(), '累计加工件数');


-- =============================================
-- 三、菜单权限（DMS菜单树）
-- 菜单ID从3000开始（WMS使用2000-2122）
-- =============================================

-- 顶级目录：DMS设备管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3000, 'DMS设备管理', 0, 6, 'dms', NULL, '', '', 1, 0, 'M', '0', '0', '', 'server', 'admin', sysdate(), 'DMS设备管理目录');

-- 二级目录：基础数据
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3001, '基础数据', 3000, 1, 'base', NULL, '', '', 1, 0, 'M', '0', '0', '', 'dict', 'admin', sysdate(), '设备基础数据');

-- 基础数据-菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3010, '设备分类', 3001, 1, 'category', 'dms/category/index', '', 'DmsCategory', 1, 0, 'C', '0', '0', 'dms:category:list', 'tree', 'admin', sysdate(), ''),
(3020, '设备台账', 3001, 2, 'equipment', 'dms/equipment/index', '', 'DmsEquipment', 1, 0, 'C', '0', '0', 'dms:equipment:list', 'component', 'admin', sysdate(), ''),
(3030, '备件管理', 3001, 3, 'sparepart', 'dms/sparepart/index', '', 'DmsSparePart', 1, 0, 'C', '0', '0', 'dms:sparepart:list', 'shopping', 'admin', sysdate(), '');

-- 设备分类按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3011, '分类查询', 3010, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:category:query', '#', 'admin', sysdate(), ''),
(3012, '分类新增', 3010, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:category:add', '#', 'admin', sysdate(), ''),
(3013, '分类修改', 3010, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:category:edit', '#', 'admin', sysdate(), ''),
(3014, '分类删除', 3010, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:category:remove', '#', 'admin', sysdate(), '');

-- 设备台账按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3021, '设备查询', 3020, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:equipment:query', '#', 'admin', sysdate(), ''),
(3022, '设备新增', 3020, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:equipment:add', '#', 'admin', sysdate(), ''),
(3023, '设备修改', 3020, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:equipment:edit', '#', 'admin', sysdate(), ''),
(3024, '设备删除', 3020, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:equipment:remove', '#', 'admin', sysdate(), ''),
(3025, '设备导出', 3020, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:equipment:export', '#', 'admin', sysdate(), ''),
(3026, '设备导入', 3020, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:equipment:import', '#', 'admin', sysdate(), '');

-- 备件管理按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3031, '备件查询', 3030, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:sparepart:query', '#', 'admin', sysdate(), ''),
(3032, '备件新增', 3030, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:sparepart:add', '#', 'admin', sysdate(), ''),
(3033, '备件修改', 3030, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:sparepart:edit', '#', 'admin', sysdate(), ''),
(3034, '备件删除', 3030, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:sparepart:remove', '#', 'admin', sysdate(), ''),
(3035, '备件导出', 3030, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:sparepart:export', '#', 'admin', sysdate(), '');

-- 二级目录：工单管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3002, '工单管理', 3000, 2, 'workorder', NULL, '', '', 1, 0, 'M', '0', '0', '', 'edit', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3040, '工单列表', 3002, 1, 'list', 'dms/workorder/index', '', 'DmsWorkOrder', 1, 0, 'C', '0', '0', 'dms:workorder:list', 'list', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3041, '工单查询', 3040, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:query', '#', 'admin', sysdate(), ''),
(3042, '工单新增', 3040, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:add', '#', 'admin', sysdate(), ''),
(3043, '工单修改', 3040, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:edit', '#', 'admin', sysdate(), ''),
(3044, '工单删除', 3040, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:remove', '#', 'admin', sysdate(), ''),
(3045, '工单导出', 3040, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:export', '#', 'admin', sysdate(), '');

-- 二级目录：维护计划
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3003, '维护计划', 3000, 3, 'pm', NULL, '', '', 1, 0, 'M', '0', '0', '', 'date', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3050, 'PM计划', 3003, 1, 'plan', 'dms/pmplan/index', '', 'DmsPmPlan', 1, 0, 'C', '0', '0', 'dms:pmplan:list', 'plan', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3051, '计划查询', 3050, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:pmplan:query', '#', 'admin', sysdate(), ''),
(3052, '计划新增', 3050, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:pmplan:add', '#', 'admin', sysdate(), ''),
(3053, '计划修改', 3050, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:pmplan:edit', '#', 'admin', sysdate(), ''),
(3054, '计划删除', 3050, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:pmplan:remove', '#', 'admin', sysdate(), '');

-- 给admin角色（role_id=1）分配所有DMS菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 3000), (1, 3001), (1, 3002), (1, 3003),
(1, 3010), (1, 3011), (1, 3012), (1, 3013), (1, 3014),
(1, 3020), (1, 3021), (1, 3022), (1, 3023), (1, 3024), (1, 3025),
(1, 3030), (1, 3031), (1, 3032), (1, 3033), (1, 3034), (1, 3035),
(1, 3040), (1, 3041), (1, 3042), (1, 3043), (1, 3044), (1, 3045),
(1, 3050), (1, 3051), (1, 3052), (1, 3053), (1, 3054);


-- =============================================
-- 四、预置设备分类数据（依据GB/T国标，简化版）
-- =============================================

INSERT INTO dms_equipment_category (category_id, parent_id, ancestors, category_code, category_name, category_level, order_num, status, del_flag, create_by, create_time, remark) VALUES
(1, 0, '0', 'A', '金属切削机床', 1, 1, '0', '0', 'admin', sysdate(), '金属切削加工设备'),
(2, 1, '0,1', 'A01', '车床', 2, 1, '0', '0', 'admin', sysdate(), ''),
(3, 1, '0,1', 'A02', '铣床', 2, 2, '0', '0', 'admin', sysdate(), ''),
(4, 1, '0,1', 'A03', '钻床', 2, 3, '0', '0', 'admin', sysdate(), ''),
(5, 1, '0,1', 'A04', '磨床', 2, 4, '0', '0', 'admin', sysdate(), ''),
(6, 1, '0,1', 'A05', '数控机床', 2, 5, '0', '0', 'admin', sysdate(), ''),
(7, 0, '0', 'B', '锻压设备', 1, 2, '0', '0', 'admin', sysdate(), '锻压加工设备'),
(8, 7, '0,7', 'B01', '冲床', 2, 1, '0', '0', 'admin', sysdate(), ''),
(9, 7, '0,7', 'B02', '剪板机', 2, 2, '0', '0', 'admin', sysdate(), ''),
(10, 7, '0,7', 'B03', '折弯机', 2, 3, '0', '0', 'admin', sysdate(), ''),
(11, 0, '0', 'C', '动力设备', 1, 3, '0', '0', 'admin', sysdate(), '动力供应设备'),
(12, 11, '0,11', 'C01', '空压机', 2, 1, '0', '0', 'admin', sysdate(), ''),
(13, 11, '0,11', 'C02', '变压器', 2, 2, '0', '0', 'admin', sysdate(), ''),
(14, 11, '0,11', 'C03', '发电机', 2, 3, '0', '0', 'admin', sysdate(), ''),
(15, 0, '0', 'D', '起重运输设备', 1, 4, '0', '0', 'admin', sysdate(), '物料搬运设备'),
(16, 15, '0,15', 'D01', '行车', 2, 1, '0', '0', 'admin', sysdate(), ''),
(17, 15, '0,15', 'D02', '叉车', 2, 2, '0', '0', 'admin', sysdate(), ''),
(18, 15, '0,15', 'D03', '输送带', 2, 3, '0', '0', 'admin', sysdate(), ''),
(19, 0, '0', 'E', '检测仪器', 1, 5, '0', '0', 'admin', sysdate(), '检测测量设备'),
(20, 19, '0,19', 'E01', '三坐标测量仪', 2, 1, '0', '0', 'admin', sysdate(), ''),
(21, 19, '0,19', 'E02', '光谱仪', 2, 2, '0', '0', 'admin', sysdate(), ''),
(22, 0, '0', 'F', '其他设备', 1, 6, '0', '0', 'admin', sysdate(), '其他工业设备');



-- ============================================================
-- File: mk_all.sql
-- ============================================================

-- =============================================
-- MK 营销管理子系统 - 全量SQL脚本
-- 包含：14张业务表 + 20个字典类型 + 菜单权限 + 基础数据
-- =============================================

-- =============================================
-- 一、业务表 DDL
-- =============================================

-- 1. 企业客户表
DROP TABLE IF EXISTS mk_customer;
CREATE TABLE mk_customer (
    customer_id        BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '客户ID',
    customer_no        VARCHAR(32)   NOT NULL                 COMMENT '客户编号',
    customer_name      VARCHAR(200)  NOT NULL                 COMMENT '企业名称',
    credit_code        VARCHAR(32)                            COMMENT '统一社会信用代码',
    customer_level     VARCHAR(10)   DEFAULT '3'              COMMENT '客户等级（1关键大客户 2重要客户 3普通客户 4潜在客户）',
    customer_source    VARCHAR(10)   DEFAULT '0'              COMMENT '客户来源（字典）',
    industry           VARCHAR(50)                            COMMENT '所属行业（字典）',
    company_size       VARCHAR(10)                            COMMENT '企业规模（字典）',
    province           VARCHAR(50)                            COMMENT '省',
    city               VARCHAR(50)                            COMMENT '市',
    district           VARCHAR(50)                            COMMENT '区',
    address            VARCHAR(500)                           COMMENT '详细地址',
    website            VARCHAR(200)                           COMMENT '官网',
    customer_status    VARCHAR(10)   DEFAULT '0'              COMMENT '客户状态（0意向客户 1签约客户 2合作中 3暂停合作 4流失客户）',
    first_contact_date DATE                                   COMMENT '首次接触时间',
    cooperation_date   DATE                                   COMMENT '合作开始时间',
    total_amount       DECIMAL(14,2) DEFAULT 0                COMMENT '累计交易额',
    user_id            BIGINT                                 COMMENT '负责人ID',
    dept_id            BIGINT                                 COMMENT '部门ID',
    del_flag           CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by          VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                               COMMENT '创建时间',
    update_by          VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                               COMMENT '更新时间',
    remark             VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (customer_id),
    UNIQUE KEY uk_customer_no (customer_no),
    KEY idx_customer_name (customer_name),
    KEY idx_customer_user (user_id),
    KEY idx_customer_dept (dept_id),
    KEY idx_customer_level (customer_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业客户表';

-- 2. 联系人表
DROP TABLE IF EXISTS mk_contact;
CREATE TABLE mk_contact (
    contact_id    BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '联系人ID',
    customer_id   BIGINT        NOT NULL                 COMMENT '客户ID',
    name          VARCHAR(50)   NOT NULL                 COMMENT '姓名',
    gender        CHAR(1)       DEFAULT '0'              COMMENT '性别（0男 1女）',
    position      VARCHAR(100)                           COMMENT '职位',
    department    VARCHAR(100)                           COMMENT '所属部门',
    role_tag      VARCHAR(200)                           COMMENT '角色标签（多选逗号分隔）',
    phone         VARCHAR(20)   NOT NULL                 COMMENT '手机号',
    email         VARCHAR(100)                           COMMENT '邮箱',
    wechat        VARCHAR(50)                            COMMENT '微信号',
    qq            VARCHAR(20)                            COMMENT 'QQ号',
    is_key        CHAR(1)       DEFAULT '0'              COMMENT '是否关键联系人（0否 1是）',
    is_primary    CHAR(1)       DEFAULT '0'              COMMENT '是否主要联系人（0否 1是）',
    personal_trait VARCHAR(500)                           COMMENT '个人特点',
    del_flag      CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by     VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time   DATETIME                               COMMENT '创建时间',
    update_by     VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time   DATETIME                               COMMENT '更新时间',
    remark        VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (contact_id),
    KEY idx_contact_customer (customer_id),
    KEY idx_contact_phone (phone),
    KEY idx_contact_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联系人表';

-- 3. 互动记录表
DROP TABLE IF EXISTS mk_interaction;
CREATE TABLE mk_interaction (
    record_id        BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    customer_id      BIGINT        NOT NULL                 COMMENT '客户ID',
    contact_id       BIGINT                                 COMMENT '联系人ID',
    opportunity_id   BIGINT                                 COMMENT '商机ID',
    lead_id          BIGINT                                 COMMENT '线索ID',
    interact_type    VARCHAR(10)   NOT NULL                 COMMENT '互动类型（字典）',
    interact_time    DATETIME      NOT NULL                 COMMENT '互动时间',
    content          TEXT                                   COMMENT '互动内容',
    user_id          BIGINT                                 COMMENT '跟进人ID',
    next_time        DATETIME                               COMMENT '下次跟进时间',
    next_content     VARCHAR(500)                           COMMENT '下次跟进内容',
    attachment       VARCHAR(500)                           COMMENT '附件URL',
    del_flag         CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                               COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                               COMMENT '更新时间',
    PRIMARY KEY (record_id),
    KEY idx_interaction_customer (customer_id),
    KEY idx_interaction_lead (lead_id),
    KEY idx_interaction_opp (opportunity_id),
    KEY idx_interaction_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='互动记录表';

-- 4. 线索表
DROP TABLE IF EXISTS mk_lead;
CREATE TABLE mk_lead (
    lead_id               BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '线索ID',
    lead_no               VARCHAR(32)   NOT NULL                 COMMENT '线索编号',
    company_name          VARCHAR(200)  NOT NULL                 COMMENT '企业名称',
    contact_name          VARCHAR(50)   NOT NULL                 COMMENT '联系人姓名',
    contact_phone         VARCHAR(20)   NOT NULL                 COMMENT '联系人手机号',
    contact_email         VARCHAR(100)                           COMMENT '联系人邮箱',
    position              VARCHAR(100)                           COMMENT '职位',
    industry              VARCHAR(50)                            COMMENT '所属行业',
    company_size          VARCHAR(10)                            COMMENT '企业规模',
    address               VARCHAR(500)                           COMMENT '详细地址',
    lead_source           VARCHAR(10)   DEFAULT '0'              COMMENT '线索来源（字典）',
    activity_id           BIGINT                                 COMMENT '来源活动ID',
    requirement_desc      TEXT                                   COMMENT '需求描述',
    lead_score            INT           DEFAULT 0                COMMENT '线索评分（0-100）',
    lead_grade            VARCHAR(2)    DEFAULT 'D'              COMMENT '线索等级（A/B/C/D）',
    lead_status           VARCHAR(10)   DEFAULT '0'              COMMENT '线索状态（0新线索 1培育中 2MQL 3SQL 4已转化 5已无效）',
    is_public             CHAR(1)       DEFAULT '0'              COMMENT '是否在公海（0否 1是）',
    user_id               BIGINT                                 COMMENT '负责人ID',
    dept_id               BIGINT                                 COMMENT '部门ID',
    receive_time          DATETIME                               COMMENT '领取时间',
    receive_status        VARCHAR(10)   DEFAULT '0'              COMMENT '领取审批状态（0未申请 1待审批 2已批准 3已拒绝）',
    receive_remark        VARCHAR(500)                           COMMENT '领取审批备注',
    receive_apply_user_id BIGINT                                 COMMENT '领取申请人ID',
    receive_apply_time    DATETIME                               COMMENT '领取申请时间',
    last_follow_time      DATETIME                               COMMENT '最后跟进时间',
    ineffective_reason    VARCHAR(10)                            COMMENT '无效原因',
    ineffective_remark    VARCHAR(500)                           COMMENT '无效说明',
    convert_customer_id   BIGINT                                 COMMENT '转化后的客户ID',
    convert_time          DATETIME                               COMMENT '转化时间',
    del_flag              CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by             VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time           DATETIME                               COMMENT '创建时间',
    update_by             VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time           DATETIME                               COMMENT '更新时间',
    remark                VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (lead_id),
    UNIQUE KEY uk_lead_no (lead_no),
    KEY idx_lead_company (company_name),
    KEY idx_lead_phone (contact_phone),
    KEY idx_lead_status (lead_status),
    KEY idx_lead_user (user_id),
    KEY idx_lead_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线索表';

-- 5. 商机表
DROP TABLE IF EXISTS mk_opportunity;
CREATE TABLE mk_opportunity (
    opportunity_id      BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '商机ID',
    opportunity_no      VARCHAR(32)   NOT NULL                 COMMENT '商机编号',
    opportunity_name    VARCHAR(200)  NOT NULL                 COMMENT '商机名称',
    customer_id         BIGINT        NOT NULL                 COMMENT '客户ID',
    contact_id          BIGINT                                 COMMENT '主要联系人ID',
    lead_id             BIGINT                                 COMMENT '来源线索ID',
    opportunity_source  VARCHAR(10)   DEFAULT '0'              COMMENT '商机来源（字典）',
    expected_amount     DECIMAL(14,2) NOT NULL                 COMMENT '预计金额',
    expected_date       DATE          NOT NULL                 COMMENT '预计成交日期',
    stage_code          VARCHAR(20)   DEFAULT 'stage1'         COMMENT '销售阶段编码',
    stage_name          VARCHAR(50)                            COMMENT '销售阶段名称',
    win_rate            INT           DEFAULT 10               COMMENT '赢率（%）',
    weighted_amount     DECIMAL(14,2) DEFAULT 0                COMMENT '加权金额',
    opportunity_status  VARCHAR(10)   DEFAULT '0'              COMMENT '商机状态（0进行中 1已赢单 2已输单）',
    competitor          VARCHAR(200)                           COMMENT '竞争对手',
    pain_point          TEXT                                   COMMENT '客户痛点',
    solution            TEXT                                   COMMENT '解决方案',
    lost_reason         VARCHAR(10)                            COMMENT '输单原因',
    lost_remark         VARCHAR(500)                           COMMENT '输单说明',
    actual_amount       DECIMAL(14,2)                          COMMENT '实际成交金额',
    actual_date         DATE                                   COMMENT '实际成交日期',
    user_id             BIGINT                                 COMMENT '负责人ID',
    dept_id             BIGINT                                 COMMENT '部门ID',
    next_action         VARCHAR(500)                           COMMENT '下一步行动',
    next_time           DATETIME                               COMMENT '下次行动时间',
    stage_enter_time    DATETIME                               COMMENT '进入当前阶段时间',
    del_flag            CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by           VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time         DATETIME                               COMMENT '创建时间',
    update_by           VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time         DATETIME                               COMMENT '更新时间',
    remark              VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (opportunity_id),
    UNIQUE KEY uk_opportunity_no (opportunity_no),
    KEY idx_opp_customer (customer_id),
    KEY idx_opp_stage (stage_code),
    KEY idx_opp_status (opportunity_status),
    KEY idx_opp_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机表';

-- 6. 销售阶段配置表
DROP TABLE IF EXISTS mk_opportunity_stage;
CREATE TABLE mk_opportunity_stage (
    stage_id         BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '阶段ID',
    stage_code       VARCHAR(20)   NOT NULL                 COMMENT '阶段编码',
    stage_name       VARCHAR(50)   NOT NULL                 COMMENT '阶段名称',
    sort             INT           NOT NULL                 COMMENT '排序号',
    win_rate         INT           DEFAULT 0                COMMENT '赢率（%）',
    max_days         INT                                    COMMENT '最大停留天数',
    required_fields  VARCHAR(500)                           COMMENT '阶段推进必填字段',
    status           CHAR(1)       DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                               COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                               COMMENT '更新时间',
    remark           VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (stage_id),
    UNIQUE KEY uk_stage_code (stage_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售阶段配置表';

-- 7. 营销活动表
DROP TABLE IF EXISTS mk_activity;
CREATE TABLE mk_activity (
    activity_id         BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '活动ID',
    activity_no         VARCHAR(32)   NOT NULL                 COMMENT '活动编号',
    activity_name       VARCHAR(200)  NOT NULL                 COMMENT '活动名称',
    activity_type       VARCHAR(10)   NOT NULL                 COMMENT '活动类型（字典）',
    activity_form       CHAR(1)       DEFAULT '0'              COMMENT '活动形式（0线上 1线下）',
    activity_status     VARCHAR(10)   DEFAULT '0'              COMMENT '活动状态（0草稿 1报名中 2进行中 3已结束 4已取消）',
    start_time          DATETIME                               COMMENT '开始时间',
    end_time            DATETIME                               COMMENT '结束时间',
    location            VARCHAR(500)                           COMMENT '活动地点/链接',
    budget              DECIMAL(14,2) DEFAULT 0                COMMENT '活动预算',
    target_count        INT                                    COMMENT '目标人数',
    target_lead_count   INT                                    COMMENT '目标线索数',
    user_id             BIGINT                                 COMMENT '负责人ID',
    dept_id             BIGINT                                 COMMENT '部门ID',
    summary             VARCHAR(500)                           COMMENT '活动简介',
    content             TEXT                                   COMMENT '活动详情',
    actual_count        INT           DEFAULT 0                COMMENT '实际参与人数',
    lead_count          INT           DEFAULT 0                COMMENT '获取线索数',
    mql_count           INT           DEFAULT 0                COMMENT 'MQL数量',
    sql_count           INT           DEFAULT 0                COMMENT 'SQL数量',
    opportunity_count   INT           DEFAULT 0                COMMENT '转化商机数',
    deal_amount         DECIMAL(14,2) DEFAULT 0                COMMENT '成交金额',
    del_flag            CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by           VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time         DATETIME                               COMMENT '创建时间',
    update_by           VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time         DATETIME                               COMMENT '更新时间',
    remark              VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (activity_id),
    UNIQUE KEY uk_activity_no (activity_no),
    KEY idx_activity_type (activity_type),
    KEY idx_activity_status (activity_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营销活动表';

-- 8. 活动参与人表
DROP TABLE IF EXISTS mk_activity_participant;
CREATE TABLE mk_activity_participant (
    participant_id      BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '参与ID',
    activity_id         BIGINT        NOT NULL                 COMMENT '活动ID',
    lead_id             BIGINT                                 COMMENT '关联线索ID',
    company_name        VARCHAR(200)                           COMMENT '企业名称',
    contact_name        VARCHAR(50)   NOT NULL                 COMMENT '联系人姓名',
    contact_phone       VARCHAR(20)   NOT NULL                 COMMENT '联系人手机号',
    contact_email       VARCHAR(100)                           COMMENT '联系人邮箱',
    position            VARCHAR(100)                           COMMENT '职位',
    participate_status  CHAR(1)       DEFAULT '0'              COMMENT '参与状态（0报名 1已签到 2未出席）',
    sign_time           DATETIME                               COMMENT '签到时间',
    create_by           VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time         DATETIME                               COMMENT '创建时间',
    update_by           VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time         DATETIME                               COMMENT '更新时间',
    remark              VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (participant_id),
    KEY idx_participant_activity (activity_id),
    KEY idx_participant_lead (lead_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动参与人表';

-- 9. 合同表
DROP TABLE IF EXISTS mk_contract;
CREATE TABLE mk_contract (
    contract_id       BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '合同ID',
    contract_no       VARCHAR(32)   NOT NULL                 COMMENT '合同编号',
    contract_name     VARCHAR(200)  NOT NULL                 COMMENT '合同名称',
    contract_type     VARCHAR(10)   DEFAULT '0'              COMMENT '合同类型（0新签 1续签 2增购 3升级）',
    customer_id       BIGINT        NOT NULL                 COMMENT '客户ID',
    opportunity_id    BIGINT                                 COMMENT '关联商机ID',
    contract_amount   DECIMAL(14,2) NOT NULL                 COMMENT '合同金额',
    sign_date         DATE          NOT NULL                 COMMENT '签约日期',
    effective_date    DATE          NOT NULL                 COMMENT '生效日期',
    expire_date       DATE          NOT NULL                 COMMENT '到期日期',
    payment_method    CHAR(1)       DEFAULT '0'              COMMENT '付款方式（0一次性 1分期）',
    contract_status   VARCHAR(10)   DEFAULT '0'              COMMENT '合同状态（0草稿 1审批中 2已生效 3已过期 4已终止）',
    user_id           BIGINT                                 COMMENT '负责人ID',
    dept_id           BIGINT                                 COMMENT '部门ID',
    attachment        VARCHAR(500)                           COMMENT '合同附件',
    del_flag          CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by         VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time       DATETIME                               COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time       DATETIME                               COMMENT '更新时间',
    remark            VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (contract_id),
    UNIQUE KEY uk_contract_no (contract_no),
    KEY idx_contract_customer (customer_id),
    KEY idx_contract_status (contract_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同表';

-- 10. 订单表
DROP TABLE IF EXISTS mk_order;
CREATE TABLE mk_order (
    order_id            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '订单ID',
    order_no            VARCHAR(32)   NOT NULL                 COMMENT '订单编号',
    contract_id         BIGINT                                 COMMENT '关联合同ID',
    customer_id         BIGINT        NOT NULL                 COMMENT '客户ID',
    order_amount        DECIMAL(14,2) NOT NULL                 COMMENT '订单金额',
    order_status        VARCHAR(10)   DEFAULT '0'              COMMENT '订单状态（0待发货 1已发货 2已完成 3已取消）',
    logistics_company   VARCHAR(50)                            COMMENT '物流公司',
    tracking_no         VARCHAR(50)                            COMMENT '物流单号',
    deliver_time        DATETIME                               COMMENT '发货时间',
    finish_time         DATETIME                               COMMENT '完成时间',
    user_id             BIGINT                                 COMMENT '负责人ID',
    dept_id             BIGINT                                 COMMENT '部门ID',
    del_flag            CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
    create_by           VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time         DATETIME                               COMMENT '创建时间',
    update_by           VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time         DATETIME                               COMMENT '更新时间',
    remark              VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_customer (customer_id),
    KEY idx_order_contract (contract_id),
    KEY idx_order_status (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 11. 订单明细表
DROP TABLE IF EXISTS mk_order_item;
CREATE TABLE mk_order_item (
    item_id         BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    order_id        BIGINT        NOT NULL                 COMMENT '订单ID',
    line_no         INT                                    COMMENT '行号',
    material_id     BIGINT                                 COMMENT '物料ID（关联wms_material）',
    product_name    VARCHAR(200)  NOT NULL                 COMMENT '商品名称',
    product_spec    VARCHAR(200)                           COMMENT '规格型号',
    unit            VARCHAR(20)                            COMMENT '单位',
    quantity        INT           NOT NULL                 COMMENT '数量',
    shipped_qty     DECIMAL(18,2) DEFAULT 0                COMMENT '已发货数量',
    unit_price      DECIMAL(14,2) NOT NULL                 COMMENT '单价',
    subtotal        DECIMAL(14,2) NOT NULL                 COMMENT '小计',
    create_time     DATETIME                               COMMENT '创建时间',
    remark          VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (item_id),
    KEY idx_order_item_order (order_id),
    KEY idx_order_item_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 12. 回款计划表
DROP TABLE IF EXISTS mk_payment_plan;
CREATE TABLE mk_payment_plan (
    plan_id          BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '计划ID',
    contract_id      BIGINT        NOT NULL                 COMMENT '合同ID',
    customer_id      BIGINT        NOT NULL                 COMMENT '客户ID',
    period_no        INT           NOT NULL                 COMMENT '期次',
    plan_amount      DECIMAL(14,2) NOT NULL                 COMMENT '计划回款金额',
    plan_date        DATE          NOT NULL                 COMMENT '计划回款日期',
    actual_amount    DECIMAL(14,2) DEFAULT 0                COMMENT '实际回款金额',
    actual_date      DATE                                   COMMENT '实际回款日期',
    payment_status   VARCHAR(10)   DEFAULT '0'              COMMENT '回款状态（0待回款 1已回款 2已逾期）',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                               COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                               COMMENT '更新时间',
    remark           VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (plan_id),
    KEY idx_payment_contract (contract_id),
    KEY idx_payment_customer (customer_id),
    KEY idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回款计划表';

-- 13. 客户标签表
DROP TABLE IF EXISTS mk_customer_tag;
CREATE TABLE mk_customer_tag (
    tag_id      BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '标签ID',
    tag_name    VARCHAR(50)   NOT NULL                 COMMENT '标签名称',
    tag_type    CHAR(1)       DEFAULT '0'              COMMENT '标签类型（0手动 1自动）',
    tag_color   VARCHAR(20)                            COMMENT '标签颜色',
    sort        INT           DEFAULT 0                COMMENT '排序',
    status      CHAR(1)       DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    create_by   VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time DATETIME                               COMMENT '创建时间',
    update_by   VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time DATETIME                               COMMENT '更新时间',
    remark      VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户标签表';

-- 14. 客户标签关联表
DROP TABLE IF EXISTS mk_customer_tag_rel;
CREATE TABLE mk_customer_tag_rel (
    id          BIGINT   NOT NULL AUTO_INCREMENT  COMMENT '主键',
    customer_id BIGINT   NOT NULL                 COMMENT '客户ID',
    tag_id      BIGINT   NOT NULL                 COMMENT '标签ID',
    create_time DATETIME                          COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_customer_tag (customer_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户标签关联表';


-- =============================================
-- 二、字典数据
-- =============================================

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('客户等级', 'marketing_customer_level', '0', 'admin', sysdate(), '客户等级字典'),
('客户来源', 'marketing_customer_source', '0', 'admin', sysdate(), '客户来源字典'),
('行业分类', 'marketing_industry', '0', 'admin', sysdate(), '行业分类字典'),
('企业规模', 'marketing_company_size', '0', 'admin', sysdate(), '企业规模字典'),
('客户状态', 'marketing_customer_status', '0', 'admin', sysdate(), '客户状态字典'),
('联系人角色', 'marketing_contact_role', '0', 'admin', sysdate(), '联系人角色字典'),
('互动类型', 'marketing_interaction_type', '0', 'admin', sysdate(), '互动类型字典'),
('线索状态', 'marketing_lead_status', '0', 'admin', sysdate(), '线索状态字典'),
('线索等级', 'marketing_lead_grade', '0', 'admin', sysdate(), '线索等级字典'),
('无效原因', 'marketing_ineffective_reason', '0', 'admin', sysdate(), '线索无效原因字典'),
('商机来源', 'marketing_opportunity_source', '0', 'admin', sysdate(), '商机来源字典'),
('商机状态', 'marketing_opportunity_status', '0', 'admin', sysdate(), '商机状态字典'),
('输单原因', 'marketing_lost_reason', '0', 'admin', sysdate(), '输单原因字典'),
('活动类型', 'marketing_activity_type', '0', 'admin', sysdate(), '活动类型字典'),
('活动状态', 'marketing_activity_status', '0', 'admin', sysdate(), '活动状态字典'),
('合同类型', 'marketing_contract_type', '0', 'admin', sysdate(), '合同类型字典'),
('合同状态', 'marketing_contract_status', '0', 'admin', sysdate(), '合同状态字典'),
('订单状态', 'marketing_order_status', '0', 'admin', sysdate(), '订单状态字典'),
('回款状态', 'marketing_payment_status', '0', 'admin', sysdate(), '回款状态字典'),
('合同变更类型', 'marketing_contract_change_type', '0', 'admin', sysdate(), '合同变更类型字典'),
('合同变更状态', 'marketing_contract_change_status', '0', 'admin', sysdate(), '合同变更状态字典'),
('参与状态', 'marketing_participate_status', '0', 'admin', sysdate(), '活动参与状态字典');

-- 字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '关键大客户', '1', 'marketing_customer_level', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(2, '重要客户', '2', 'marketing_customer_level', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '普通客户', '3', 'marketing_customer_level', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(4, '潜在客户', '4', 'marketing_customer_level', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '线上广告', '0', 'marketing_customer_source', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '展会', '1', 'marketing_customer_source', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '转介绍', '2', 'marketing_customer_source', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '官网', '3', 'marketing_customer_source', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(5, '其他', '4', 'marketing_customer_source', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '互联网/IT', '0', 'marketing_industry', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '制造业', '1', 'marketing_industry', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '金融', '2', 'marketing_industry', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '教育', '3', 'marketing_industry', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(5, '医疗', '4', 'marketing_industry', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(6, '零售', '5', 'marketing_industry', '', '', 'N', '0', 'admin', sysdate(), ''),
(7, '物流', '6', 'marketing_industry', '', '', 'N', '0', 'admin', sysdate(), ''),
(8, '其他', '7', 'marketing_industry', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '大型', '0', 'marketing_company_size', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(2, '中型', '1', 'marketing_company_size', '', 'warning', 'Y', '0', 'admin', sysdate(), ''),
(3, '小型', '2', 'marketing_company_size', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(4, '微型', '3', 'marketing_company_size', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '意向客户', '0', 'marketing_customer_status', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '签约客户', '1', 'marketing_customer_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '合作中', '2', 'marketing_customer_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '暂停合作', '3', 'marketing_customer_status', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(5, '流失客户', '4', 'marketing_customer_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '决策人', '0', 'marketing_contact_role', '', 'danger', 'Y', '0', 'admin', sysdate(), ''),
(2, '经济购买者', '1', 'marketing_contact_role', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '使用者', '2', 'marketing_contact_role', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(4, '影响者', '3', 'marketing_contact_role', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(5, '守门人', '4', 'marketing_contact_role', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '电话', '0', 'marketing_interaction_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '拜访', '1', 'marketing_interaction_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '邮件', '2', 'marketing_interaction_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(4, '会议', '3', 'marketing_interaction_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(5, '微信', '4', 'marketing_interaction_type', '', '', 'N', '0', 'admin', sysdate(), ''),
(6, '其他', '5', 'marketing_interaction_type', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '新线索', '0', 'marketing_lead_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '培育中', '1', 'marketing_lead_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, 'MQL', '2', 'marketing_lead_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, 'SQL', '3', 'marketing_lead_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(5, '已转化', '4', 'marketing_lead_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(6, '已无效', '5', 'marketing_lead_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, 'A级(优质)', 'A', 'marketing_lead_grade', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(2, 'B级(良好)', 'B', 'marketing_lead_grade', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, 'C级(一般)', 'C', 'marketing_lead_grade', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, 'D级(较差)', 'D', 'marketing_lead_grade', '', 'info', 'Y', '0', 'admin', sysdate(), ''),

(1, '电话空号', '0', 'marketing_ineffective_reason', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(2, '需求不符', '1', 'marketing_ineffective_reason', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '无预算', '2', 'marketing_ineffective_reason', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(4, '其他', '3', 'marketing_ineffective_reason', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '线索转化', '0', 'marketing_opportunity_source', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '老客户增购', '1', 'marketing_opportunity_source', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '主动开发', '2', 'marketing_opportunity_source', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '转介绍', '3', 'marketing_opportunity_source', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '进行中', '0', 'marketing_opportunity_status', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '已赢单', '1', 'marketing_opportunity_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '已输单', '2', 'marketing_opportunity_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '价格高', '0', 'marketing_lost_reason', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(2, '竞品中标', '1', 'marketing_lost_reason', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(3, '需求变更', '2', 'marketing_lost_reason', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(4, '其他', '3', 'marketing_lost_reason', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '线上研讨会', '0', 'marketing_activity_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '展会', '1', 'marketing_activity_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '行业峰会', '2', 'marketing_activity_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '客户沙龙', '3', 'marketing_activity_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(5, '产品培训', '4', 'marketing_activity_type', '', '', 'N', '0', 'admin', sysdate(), ''),
(6, '广告投放', '5', 'marketing_activity_type', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(7, '其他', '6', 'marketing_activity_type', '', '', 'N', '0', 'admin', sysdate(), ''),

(1, '草稿', '0', 'marketing_activity_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '报名中', '1', 'marketing_activity_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '进行中', '2', 'marketing_activity_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '已结束', '3', 'marketing_activity_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(5, '已取消', '4', 'marketing_activity_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '新签', '0', 'marketing_contract_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '续签', '1', 'marketing_contract_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '增购', '2', 'marketing_contract_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '升级', '3', 'marketing_contract_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '草稿', '0', 'marketing_contract_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '审批中', '1', 'marketing_contract_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '已生效', '2', 'marketing_contract_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '已过期', '3', 'marketing_contract_status', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(5, '已终止', '4', 'marketing_contract_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(6, '变更审批中', '5', 'marketing_contract_status', '', 'warning', 'N', '0', 'admin', sysdate(), '变更审批中状态'),

(1, '金额变更', '1', 'marketing_contract_change_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '日期变更', '2', 'marketing_contract_change_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(3, '条款变更', '3', 'marketing_contract_change_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '其他', '4', 'marketing_contract_change_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),

(1, '待审批', '0', 'marketing_contract_change_status', '', 'warning', 'Y', '0', 'admin', sysdate(), ''),
(2, '已批准', '1', 'marketing_contract_change_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '已驳回', '2', 'marketing_contract_change_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(4, '已执行', '3', 'marketing_contract_change_status', '', 'info', 'N', '0', 'admin', sysdate(), ''),

(1, '草稿', '0', 'marketing_order_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '待审核', '1', 'marketing_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '已发货', '2', 'marketing_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '已签收', '3', 'marketing_order_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(5, '已完成', '4', 'marketing_order_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(6, '已取消', '5', 'marketing_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(7, '退货中', '6', 'marketing_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(8, '已退货', '7', 'marketing_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(9, '已确认', '8', 'marketing_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(10, '已驳回', '9', 'marketing_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),

(1, '待回款', '0', 'marketing_payment_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '已回款', '1', 'marketing_payment_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '已逾期', '2', 'marketing_payment_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(4, '部分回款', '3', 'marketing_payment_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),

(1, '报名', '0', 'marketing_participate_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '已签到', '1', 'marketing_participate_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '未出席', '2', 'marketing_participate_status', '', 'danger', 'N', '0', 'admin', sysdate(), '');


-- =============================================
-- 三、菜单权限
-- =============================================

-- 顶级目录：营销管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4000, '营销管理', 0, 7, 'mk', NULL, '', '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '营销管理目录');

-- 二级目录
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4001, '客户管理', 4000, 1, 'customer', NULL, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), ''),
(4002, '线索管理', 4000, 2, 'lead', NULL, '', '', 1, 0, 'M', '0', '0', '', 'guide', 'admin', sysdate(), ''),
(4003, '商机管理', 4000, 3, 'opportunity', NULL, '', '', 1, 0, 'M', '0', '0', '', 'star', 'admin', sysdate(), ''),
(4004, '营销活动', 4000, 4, 'activity', NULL, '', '', 1, 0, 'M', '0', '0', '', 'message', 'admin', sysdate(), ''),
(4005, '合同订单', 4000, 5, 'contract', NULL, '', '', 1, 0, 'M', '0', '0', '', 'documentation', 'admin', sysdate(), ''),
(4006, '数据分析', 4000, 6, 'report', NULL, '', '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '');

-- 客户管理菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4010, '企业客户', 4001, 1, 'list', 'mk/customer/index', '', 'MkCustomer', 1, 0, 'C', '0', '0', 'marketing:customer:list', 'peoples', 'admin', sysdate(), ''),
(4020, '联系人管理', 4001, 2, 'contact', 'mk/contact/index', '', 'MkContact', 1, 0, 'C', '0', '0', 'marketing:contact:list', 'user', 'admin', sysdate(), ''),
(4030, '互动记录', 4001, 3, 'interaction', 'mk/interaction/index', '', 'MkInteraction', 1, 0, 'C', '0', '0', 'marketing:interaction:list', 'phone', 'admin', sysdate(), '');

-- 企业客户按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4011, '客户查询', 4010, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:customer:query', '#', 'admin', sysdate(), ''),
(4012, '客户新增', 4010, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:customer:add', '#', 'admin', sysdate(), ''),
(4013, '客户修改', 4010, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:customer:edit', '#', 'admin', sysdate(), ''),
(4014, '客户删除', 4010, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:customer:remove', '#', 'admin', sysdate(), ''),
(4015, '客户导出', 4010, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:customer:export', '#', 'admin', sysdate(), '');

-- 联系人按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4021, '联系人查询', 4020, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contact:query', '#', 'admin', sysdate(), ''),
(4022, '联系人新增', 4020, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contact:add', '#', 'admin', sysdate(), ''),
(4023, '联系人修改', 4020, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contact:edit', '#', 'admin', sysdate(), ''),
(4024, '联系人删除', 4020, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contact:remove', '#', 'admin', sysdate(), ''),
(4025, '联系人导出', 4020, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contact:export', '#', 'admin', sysdate(), '');

-- 互动记录按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4031, '记录查询', 4030, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:interaction:query', '#', 'admin', sysdate(), ''),
(4032, '记录新增', 4030, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:interaction:add', '#', 'admin', sysdate(), ''),
(4033, '记录修改', 4030, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:interaction:edit', '#', 'admin', sysdate(), ''),
(4034, '记录删除', 4030, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:interaction:remove', '#', 'admin', sysdate(), '');

-- 线索管理菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4040, '线索列表', 4002, 1, 'list', 'mk/lead/index', '', 'MkLead', 1, 0, 'C', '0', '0', 'marketing:lead:list', 'list', 'admin', sysdate(), ''),
(4050, '公海池', 4002, 2, 'pool', 'mk/lead/pool', '', 'MkLeadPool', 1, 0, 'C', '0', '0', 'marketing:lead:pool', 'pool', 'admin', sysdate(), '');

-- 线索按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4041, '线索查询', 4040, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:query', '#', 'admin', sysdate(), ''),
(4042, '线索新增', 4040, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:add', '#', 'admin', sysdate(), ''),
(4043, '线索修改', 4040, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:edit', '#', 'admin', sysdate(), ''),
(4044, '线索删除', 4040, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:remove', '#', 'admin', sysdate(), ''),
(4045, '线索导出', 4040, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:export', '#', 'admin', sysdate(), ''),
(4046, '线索领取', 4040, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:receive', '#', 'admin', sysdate(), ''),
(4047, '线索分配', 4040, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:assign', '#', 'admin', sysdate(), '');

-- 商机管理菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4060, '商机列表', 4003, 1, 'list', 'mk/opportunity/index', '', 'MkOpportunity', 1, 0, 'C', '0', '0', 'marketing:opportunity:list', 'list', 'admin', sysdate(), ''),
(4070, '商机看板', 4003, 2, 'kanban', 'mk/opportunity/kanban', '', 'MkOpportunityKanban', 1, 0, 'C', '0', '0', 'marketing:opportunity:list', 'dashboard', 'admin', sysdate(), ''),
(4075, '阶段配置', 4003, 3, 'stage', 'mk/opportunity/stage', '', 'MkOpportunityStage', 1, 0, 'C', '0', '0', 'marketing:stage:list', 'setting', 'admin', sysdate(), '');

-- 商机按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4061, '商机查询', 4060, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:opportunity:query', '#', 'admin', sysdate(), ''),
(4062, '商机新增', 4060, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:opportunity:add', '#', 'admin', sysdate(), ''),
(4063, '商机修改', 4060, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:opportunity:edit', '#', 'admin', sysdate(), ''),
(4064, '商机删除', 4060, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:opportunity:remove', '#', 'admin', sysdate(), ''),
(4065, '商机导出', 4060, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:opportunity:export', '#', 'admin', sysdate(), '');

-- 营销活动菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4080, '活动管理', 4004, 1, 'list', 'mk/activity/index', '', 'MkActivity', 1, 0, 'C', '0', '0', 'marketing:activity:list', 'list', 'admin', sysdate(), '');

-- 活动按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4081, '活动查询', 4080, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:activity:query', '#', 'admin', sysdate(), ''),
(4082, '活动新增', 4080, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:activity:add', '#', 'admin', sysdate(), ''),
(4083, '活动修改', 4080, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:activity:edit', '#', 'admin', sysdate(), ''),
(4084, '活动删除', 4080, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:activity:remove', '#', 'admin', sysdate(), ''),
(4085, '活动导出', 4080, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:activity:export', '#', 'admin', sysdate(), '');

-- 合同订单菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4090, '合同管理', 4005, 1, 'contract', 'mk/contract/index', '', 'MkContract', 1, 0, 'C', '0', '0', 'marketing:contract:list', 'list', 'admin', sysdate(), ''),
(4100, '订单管理', 4005, 2, 'order', 'mk/order/index', '', 'MkOrder', 1, 0, 'C', '0', '0', 'marketing:order:list', 'list', 'admin', sysdate(), ''),
(4110, '回款管理', 4005, 3, 'payment', 'mk/payment/index', '', 'MkPayment', 1, 0, 'C', '0', '0', 'marketing:payment:list', 'money', 'admin', sysdate(), '');

-- 合同按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4091, '合同查询', 4090, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contract:query', '#', 'admin', sysdate(), ''),
(4092, '合同新增', 4090, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contract:add', '#', 'admin', sysdate(), ''),
(4093, '合同修改', 4090, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contract:edit', '#', 'admin', sysdate(), ''),
(4094, '合同删除', 4090, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contract:remove', '#', 'admin', sysdate(), ''),
(4095, '合同导出', 4090, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:contract:export', '#', 'admin', sysdate(), '');

-- 订单按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4101, '订单查询', 4100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:order:query', '#', 'admin', sysdate(), ''),
(4102, '订单新增', 4100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:order:add', '#', 'admin', sysdate(), ''),
(4103, '订单修改', 4100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:order:edit', '#', 'admin', sysdate(), ''),
(4104, '订单删除', 4100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:order:remove', '#', 'admin', sysdate(), ''),
(4105, '订单导出', 4100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:order:export', '#', 'admin', sysdate(), ''),
(4106, '订单发货', 4100, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:order:deliver', '#', 'admin', sysdate(), '');

-- 回款按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4111, '回款查询', 4110, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:payment:query', '#', 'admin', sysdate(), ''),
(4112, '回款新增', 4110, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:payment:add', '#', 'admin', sysdate(), ''),
(4113, '回款修改', 4110, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:payment:edit', '#', 'admin', sysdate(), ''),
(4114, '回款删除', 4110, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:payment:remove', '#', 'admin', sysdate(), '');

-- 数据分析菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4120, '营销看板', 4006, 1, 'dashboard', 'mk/dashboard/index', '', 'MkDashboard', 1, 0, 'C', '0', '0', 'marketing:report:view', 'chart', 'admin', sysdate(), '');


-- =============================================
-- 四、给admin角色分配所有营销菜单权限
-- =============================================

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 4000), (1, 4001), (1, 4002), (1, 4003), (1, 4004), (1, 4005), (1, 4006),
(1, 4010), (1, 4011), (1, 4012), (1, 4013), (1, 4014), (1, 4015),
(1, 4020), (1, 4021), (1, 4022), (1, 4023), (1, 4024), (1, 4025),
(1, 4030), (1, 4031), (1, 4032), (1, 4033), (1, 4034),
(1, 4040), (1, 4041), (1, 4042), (1, 4043), (1, 4044), (1, 4045), (1, 4046), (1, 4047),
(1, 4050),
(1, 4060), (1, 4061), (1, 4062), (1, 4063), (1, 4064), (1, 4065),
(1, 4070), (1, 4075),
(1, 4080), (1, 4081), (1, 4082), (1, 4083), (1, 4084), (1, 4085),
(1, 4090), (1, 4091), (1, 4092), (1, 4093), (1, 4094), (1, 4095),
(1, 4100), (1, 4101), (1, 4102), (1, 4103), (1, 4104), (1, 4105),
(1, 4110), (1, 4111), (1, 4112), (1, 4113), (1, 4114),
(1, 4120);


-- =============================================
-- 五、销售阶段基础数据
-- =============================================

INSERT INTO mk_opportunity_stage (stage_id, stage_code, stage_name, sort, win_rate, max_days, status, create_by, create_time, remark) VALUES
(1, 'stage1', '初步接触', 1, 10, 30, '0', 'admin', sysdate(), '首次接触，建立联系'),
(2, 'stage2', '需求确认', 2, 25, 30, '0', 'admin', sysdate(), '确认客户需求和预算'),
(3, 'stage3', '方案制定', 3, 40, 45, '0', 'admin', sysdate(), '制定解决方案'),
(4, 'stage4', '方案演示', 4, 55, 30, '0', 'admin', sysdate(), '方案演示和答疑'),
(5, 'stage5', '商务谈判', 5, 75, 30, '0', 'admin', sysdate(), '价格和条款谈判'),
(6, 'stage6', '合同签订', 6, 90, 15, '0', 'admin', sysdate(), '合同审批和签订');


-- =============================================
-- 六、基础测试数据（每个模块5条）
-- =============================================

-- 客户数据（5条）
INSERT INTO mk_customer (customer_id, customer_no, customer_name, credit_code, customer_level, customer_source, industry, company_size, province, city, district, address, website, customer_status, first_contact_date, cooperation_date, total_amount, user_id, dept_id, del_flag, create_by, create_time, remark) VALUES
(1, 'CUS20260719001', '北京智云科技有限公司', '91110108MA01ABC123', '1', '0', '0', '0', '北京市', '北京市', '海淀区', '中关村软件园二期5号楼', 'www.zhiyun-tech.com', '2', '2026-01-15', '2026-02-01', 580000, 2, 103, '0', 'admin', sysdate(), '关键大客户，长期合作'),
(2, 'CUS20260719002', '上海宏远制造股份有限公司', '91310115MA01DEF456', '2', '1', '1', '0', '上海市', '上海市', '浦东新区', '张江高科技园区博云路2号', 'www.hongyuan-mfg.cn', '2', '2026-02-20', '2026-03-10', 320000, 2, 103, '0', 'admin', sysdate(), '重要客户，制造业龙头'),
(3, 'CUS20260719003', '深圳创新金融服务有限公司', '91440300MA01GHI789', '2', '2', '2', '1', '广东省', '深圳市', '南山区', '科技园南区高新南一道9号', 'www.cx-fin.com', '2', '2026-03-05', '2026-04-01', 150000, 2, 103, '0', 'admin', sysdate(), '金融行业客户'),
(4, 'CUS20260719004', '杭州云端教育科技有限公司', '91330106MA01JKL012', '3', '3', '3', '1', '浙江省', '杭州市', '余杭区', '未来科技城梦想小镇', 'www.yunduan-edu.com', '0', '2026-04-10', NULL, 50000, 2, 103, '0', 'admin', sysdate(), '教育行业新客户'),
(5, 'CUS20260719005', '广州速达物流有限公司', '91440101MA01MNO345', '4', '4', '6', '2', '广东省', '广州市', '天河区', '天河软件园建中路50号', 'www.suda-log.com', '0', '2026-05-15', NULL, 0, 2, 103, '0', 'admin', sysdate(), '潜在客户，待跟进');

-- 联系人数据（5条）
INSERT INTO mk_contact (contact_id, customer_id, name, gender, position, department, role_tag, phone, email, wechat, qq, is_key, is_primary, personal_trait, del_flag, create_by, create_time, remark) VALUES
(1, 1, '张伟', '0', '总经理', '管理层', '0', '13800138001', 'zhangwei@zhiyun-tech.com', 'zhangwei_wx', '88880001', '1', '1', '注重效率和结果，偏好数据驱动的决策', '0', 'admin', sysdate(), '关键决策人'),
(2, 1, '李娜', '1', '采购经理', '采购部', '1,3', '13900139001', 'lina@zhiyun-tech.com', 'lina_wx', '88880002', '1', '0', '细致严谨，关注合同条款和交付周期', '0', 'admin', sysdate(), '采购负责人'),
(3, 2, '王强', '0', 'CTO', '技术部', '0,3', '13700137001', 'wangqiang@hongyuan-mfg.cn', 'wangqiang_wx', '88880003', '1', '1', '技术导向，关注产品技术指标', '0', 'admin', sysdate(), '技术决策人'),
(4, 3, '刘洋', '0', '运营总监', '运营部', '0,1', '13600136001', 'liuyang@cx-fin.com', 'liuyang_wx', '88880004', '1', '1', '注重ROI和投入产出比', '0', 'admin', sysdate(), '运营负责人'),
(5, 4, '陈晨', '1', '市场经理', '市场部', '2,3', '13500135001', 'chenchen@yunduan-edu.com', 'chenchen_wx', '88880005', '0', '1', '年轻有活力，喜欢创新方案', '0', 'admin', sysdate(), '市场对接人');

-- 互动记录（5条）
INSERT INTO mk_interaction (record_id, customer_id, contact_id, opportunity_id, lead_id, interact_type, interact_time, content, user_id, next_time, next_content, del_flag, create_by, create_time) VALUES
(1, 1, 1, NULL, NULL, '1', '2026-07-10 10:00:00', '拜访智云科技张总，讨论下半年的系统升级方案，客户表示预算充足，预计Q3启动项目', 2, '2026-07-25 10:00:00', '发送详细方案报价', '0', 'admin', sysdate()),
(2, 1, 2, NULL, NULL, '0', '2026-07-12 14:30:00', '电话跟进采购经理李娜，确认采购流程和审批节点，对方表示需要走季度审批', 2, '2026-07-20 14:00:00', '准备采购流程文档', '0', 'admin', sysdate()),
(3, 2, 3, NULL, NULL, '3', '2026-07-08 09:00:00', '与宏远制造王总视频会议，演示MES系统对接方案，客户反馈积极', 2, '2026-07-18 10:00:00', '安排现场POC测试', '0', 'admin', sysdate()),
(4, 3, 4, NULL, NULL, '4', '2026-07-15 11:00:00', '微信沟通刘总，发送产品白皮书和案例参考，客户对金融行业案例感兴趣', 2, '2026-07-22 15:00:00', '安排金融行业案例分享会', '0', 'admin', sysdate()),
(5, 4, 5, NULL, NULL, '2', '2026-07-16 16:00:00', '发送方案邮件给陈晨，包含SaaS教育平台功能清单和定价方案', 2, '2026-07-23 10:00:00', '电话确认邮件接收情况', '0', 'admin', sysdate());

-- 线索数据（5条）
INSERT INTO mk_lead (lead_id, lead_no, company_name, contact_name, contact_phone, contact_email, position, industry, company_size, address, lead_source, activity_id, requirement_desc, lead_score, lead_grade, lead_status, is_public, user_id, dept_id, receive_time, last_follow_time, del_flag, create_by, create_time, remark) VALUES
(1, 'LEAD20260719001', '成都新视界科技有限公司', '赵敏', '13300133001', 'zhaomin@xinshijie.com', '市场总监', '0', '1', '四川省成都市高新区', '0', NULL, '需要一套营销自动化系统，团队规模50人，预算30万', 85, 'A', '3', '0', 2, 103, '2026-07-05 09:00:00', '2026-07-15 14:00:00', '0', 'admin', sysdate(), '高质量线索，意向明确'),
(2, 'LEAD20260719002', '武汉光谷生物医药有限公司', '孙磊', '13400134001', 'sunlei@guanggu-bio.com', 'IT经理', '4', '0', '湖北省武汉市洪山区', '1', NULL, '实验室信息管理系统需求，需GMP合规', 72, 'B', '2', '0', 2, 103, '2026-07-08 10:00:00', '2026-07-14 16:00:00', '0', 'admin', sysdate(), '医药行业线索'),
(3, 'LEAD20260719003', '南京紫金山智能制造有限公司', '周婷', '13200132001', 'zhouting@zjs-mfg.com', '项目经理', '1', '0', '江苏省南京市江宁区', '2', NULL, '智能工厂改造项目，需要设备联网和数据采集平台', 68, 'B', '1', '0', 2, 103, '2026-07-10 11:00:00', '2026-07-16 09:00:00', '0', 'admin', sysdate(), '转介绍线索'),
(4, 'LEAD20260719004', '西安丝路电商有限公司', '吴昊', '13100131001', 'wuhao@silkroad-ec.com', '创始人', '0', '2', '陕西省西安市雁塔区', '3', NULL, '需要跨境电商管理系统，预算10万以内', 45, 'C', '0', '1', NULL, NULL, NULL, NULL, '0', 'admin', sysdate(), '公海线索，待领取'),
(5, 'LEAD20260719005', '青岛海蓝新能源有限公司', '郑凯', '13000130001', 'zhengkai@hailan-energy.com', '技术总监', '4', '1', '山东省青岛市崂山区', '0', NULL, '储能监控系统需求，需对接现有SCADA', 52, 'C', '0', '1', NULL, NULL, NULL, NULL, '0', 'admin', sysdate(), '公海线索，待领取');

-- 商机数据（5条）
INSERT INTO mk_opportunity (opportunity_id, opportunity_no, opportunity_name, customer_id, contact_id, lead_id, opportunity_source, expected_amount, expected_date, stage_code, stage_name, win_rate, weighted_amount, opportunity_status, competitor, pain_point, solution, lost_reason, lost_remark, actual_amount, actual_date, user_id, dept_id, next_action, next_time, stage_enter_time, del_flag, create_by, create_time, remark) VALUES
(1, 'OPP20260719001', '智云科技数字化升级项目', 1, 1, NULL, '1', 580000, '2026-09-30', 'stage4', '方案演示', 55, 319000, '0', '金蝶、用友', '现有系统老旧，数据孤岛严重，需要统一数据平台', '提供一体化数字运营平台，含数据中台+业务系统', NULL, NULL, NULL, NULL, 2, 103, '安排POC测试环境', '2026-07-25 10:00:00', '2026-07-10 10:00:00', '0', 'admin', sysdate(), '重点商机'),
(2, 'OPP20260719002', '宏远制造MES系统项目', 2, 3, NULL, '2', 320000, '2026-08-31', 'stage3', '方案制定', 40, 128000, '0', '西门子', '生产数据手工记录，效率低，质量追溯困难', 'MES系统+设备联网+看板', NULL, NULL, NULL, NULL, 2, 103, '完善技术方案', '2026-07-20 14:00:00', '2026-07-08 09:00:00', '0', 'admin', sysdate(), '制造业MES需求'),
(3, 'OPP20260719003', '创新金融风控系统项目', 3, 4, NULL, '0', 150000, '2026-10-15', 'stage2', '需求确认', 25, 37500, '0', NULL, '风控模型老化，需要AI辅助决策', 'AI风控引擎+实时预警系统', NULL, NULL, NULL, NULL, 2, 103, '确认技术需求细节', '2026-07-22 15:00:00', '2026-07-05 10:00:00', '0', 'admin', sysdate(), '金融风控需求'),
(4, 'OPP20260719004', '云端教育SaaS平台项目', 4, 5, NULL, '3', 80000, '2026-08-15', 'stage5', '商务谈判', 75, 60000, '0', NULL, '缺乏在线教学管理工具', 'SaaS教育平台年费方案', NULL, NULL, NULL, NULL, 2, 103, '发送合同草案', '2026-07-23 10:00:00', '2026-07-12 14:00:00', '0', 'admin', sysdate(), '即将成交'),
(5, 'OPP20260719005', '速达物流TMS系统项目', 5, NULL, NULL, '2', 50000, '2026-11-30', 'stage1', '初步接触', 10, 5000, '0', NULL, '运输管理依赖人工调度，效率低', 'TMS运输管理系统', NULL, NULL, NULL, NULL, 2, 103, '首次拜访客户', '2026-07-28 09:00:00', '2026-07-15 10:00:00', '0', 'admin', sysdate(), '初步接触阶段');

-- 营销活动数据（5条）
INSERT INTO mk_activity (activity_id, activity_no, activity_name, activity_type, activity_form, activity_status, start_time, end_time, location, budget, target_count, target_lead_count, user_id, dept_id, summary, content, actual_count, lead_count, mql_count, sql_count, opportunity_count, deal_amount, del_flag, create_by, create_time, remark) VALUES
(1, 'ACT20260719001', '2026数字制造线上峰会', '0', '0', '3', '2026-06-15 14:00:00', '2026-06-15 17:00:00', 'https://meeting.zhiyun-tech.com/summit', 50000, 500, 100, 2, 103, '面向制造业CIO/CTO的数字化升级线上峰会', '邀请行业专家分享数字化转型案例，展示我司MES/数字平台产品', 320, 85, 45, 22, 8, 280000, '0', 'admin', sysdate(), '效果良好，ROI 5.6'),
(2, 'ACT20260719002', 'B2B营销自动化Webinar', '0', '0', '3', '2026-06-20 19:00:00', '2026-06-20 21:00:00', 'https://meeting.zhiyun-tech.com/webinar06', 15000, 300, 60, 2, 103, '营销自动化解决方案线上研讨会', '介绍营销自动化系统核心功能及最佳实践', 180, 55, 30, 15, 5, 120000, '0', 'admin', sysdate(), 'ROI 8.0'),
(3, 'ACT20260719003', '2026工业互联网展会', '1', '1', '2', '2026-07-18 09:00:00', '2026-07-20 17:00:00', '上海新国际博览中心N1馆', 120000, 2000, 200, 2, 103, '参展2026工业互联网博览会', '展位展示产品方案，收集线索，安排现场演示', 850, 180, 80, 40, 12, 450000, '0', 'admin', sysdate(), '线下大型展会'),
(4, 'ACT20260719004', '金融科技客户沙龙', '1', '1', '1', '2026-07-25 14:00:00', '2026-07-25 17:00:00', '北京国贸大酒店3层宴会厅', 35000, 50, 30, 2, 103, '面向金融行业客户的闭门沙龙', 'AI风控、智能投顾等金融科技话题分享', 0, 0, 0, 0, 0, 0, '0', 'admin', sysdate(), '报名中'),
(5, 'ACT20260719005', '搜索引擎广告投放-Q3', '5', '0', '2', '2026-07-01 00:00:00', '2026-09-30 23:59:59', '百度/360/搜狗', 80000, 0, 150, 2, 103, 'Q3季度搜索引擎广告投放计划', '关键词：MES系统、数字平台、营销自动化等', 5000, 120, 60, 25, 8, 380000, '0', 'admin', sysdate(), '持续投放中');

-- 合同数据（5条）
INSERT INTO mk_contract (contract_id, contract_no, contract_name, contract_type, customer_id, opportunity_id, contract_amount, sign_date, effective_date, expire_date, payment_method, contract_status, user_id, dept_id, attachment, del_flag, create_by, create_time, remark) VALUES
(1, 'CON20260719001', '智云科技数字平台年度服务合同', '0', 1, 1, 580000, '2026-03-01', '2026-03-01', '2027-02-28', '1', '2', 2, 103, '/upload/contract/con001.pdf', '0', 'admin', sysdate(), '分期付款，季度回款'),
(2, 'CON20260719002', '宏远制造MES系统实施合同', '0', 2, 2, 320000, '2026-03-15', '2026-04-01', '2027-03-31', '1', '2', 2, 103, '/upload/contract/con002.pdf', '0', 'admin', sysdate(), '分期付款，含实施服务费'),
(3, 'CON20260719003', '创新金融风控系统合同', '0', 3, 3, 150000, '2026-04-10', '2026-04-15', '2027-04-14', '0', '2', 2, 103, '/upload/contract/con003.pdf', '0', 'admin', sysdate(), '一次性付款'),
(4, 'CON20260719004', '云端教育SaaS平台年度合同', '0', 4, 4, 80000, '2026-05-20', '2026-06-01', '2027-05-31', '0', '2', 2, 103, NULL, '0', 'admin', sysdate(), '年费制'),
(5, 'CON20260719005', '智云科技系统增购合同', '2', 1, NULL, 120000, '2026-06-15', '2026-07-01', '2027-06-30', '0', '2', 2, 103, NULL, '0', 'admin', sysdate(), '增购AI分析模块');

-- 订单数据（5条）
INSERT INTO mk_order (order_id, order_no, contract_id, customer_id, order_amount, order_status, logistics_company, tracking_no, deliver_time, finish_time, user_id, dept_id, del_flag, create_by, create_time, remark) VALUES
(1, 'ORD20260719001', 1, 1, 145000, '2', NULL, NULL, NULL, '2026-06-01 00:00:00', 2, 103, '0', 'admin', sysdate(), 'Q1回款订单'),
(2, 'ORD20260719002', 1, 1, 145000, '2', NULL, NULL, NULL, '2026-06-01 00:00:00', 2, 103, '0', 'admin', sysdate(), 'Q2回款订单'),
(3, 'ORD20260719003', 2, 2, 160000, '1', '顺丰速运', 'SF1234567890', '2026-05-20 10:00:00', NULL, 2, 103, '0', 'admin', sysdate(), '首期实施费'),
(4, 'ORD20260719004', 3, 3, 150000, '2', NULL, NULL, NULL, '2026-05-01 00:00:00', 2, 103, '0', 'admin', sysdate(), '一次性付款订单'),
(5, 'ORD20260719005', 5, 1, 120000, '0', NULL, NULL, NULL, NULL, 2, 103, '0', 'admin', sysdate(), '增购订单待发货');

-- 订单明细数据
INSERT INTO mk_order_item (item_id, order_id, product_name, product_spec, unit, quantity, unit_price, subtotal, create_time, remark) VALUES
(1, 1, '数字平台基础版', 'V3.0', '套', 1, 100000, 100000, sysdate(), '基础平台授权'),
(2, 1, '数据中台模块', 'V3.0', '套', 1, 45000, 45000, sysdate(), '数据中台授权'),
(3, 2, '数字平台基础版', 'V3.0', '套', 1, 100000, 100000, sysdate(), 'Q2续费'),
(4, 2, 'AI分析模块', 'V3.0', '套', 1, 45000, 45000, sysdate(), 'AI模块授权'),
(5, 3, 'MES系统核心模块', 'V2.0', '套', 1, 120000, 120000, sysdate(), 'MES核心授权'),
(6, 3, '设备联网网关', 'GW-200', '台', 8, 5000, 40000, sysdate(), '设备联网'),
(7, 4, 'AI风控引擎', 'V1.0', '套', 1, 150000, 150000, sysdate(), '风控引擎授权'),
(8, 5, 'AI分析模块', 'V3.0', '套', 1, 80000, 80000, sysdate(), '增购AI模块'),
(9, 5, '数据可视化插件', 'V3.0', '套', 2, 20000, 40000, sysdate(), '可视化插件');

-- 回款计划数据（5条）
INSERT INTO mk_payment_plan (plan_id, contract_id, customer_id, period_no, plan_amount, plan_date, actual_amount, actual_date, payment_status, create_by, create_time, remark) VALUES
(1, 1, 1, 1, 145000, '2026-03-01', 145000, '2026-03-05', '1', 'admin', sysdate(), '首期款已回款'),
(2, 1, 1, 2, 145000, '2026-06-01', 145000, '2026-06-03', '1', 'admin', sysdate(), '二期款已回款'),
(3, 1, 1, 3, 145000, '2026-09-01', 0, NULL, '0', 'admin', sysdate(), '三期款待回款'),
(4, 1, 1, 4, 145000, '2026-12-01', 0, NULL, '0', 'admin', sysdate(), '尾款待回款'),
(5, 2, 2, 1, 160000, '2026-04-01', 160000, '2026-04-10', '1', 'admin', sysdate(), '首期款已回款');

-- 客户标签数据（5条）
INSERT INTO mk_customer_tag (tag_id, tag_name, tag_type, tag_color, sort, status, create_by, create_time, remark) VALUES
(1, '高价值', '0', '#f56c6c', 1, '0', 'admin', sysdate(), '高价值客户标签'),
(2, '制造业', '0', '#409eff', 2, '0', 'admin', sysdate(), '制造业客户标签'),
(3, '金融', '0', '#67c23a', 3, '0', 'admin', sysdate(), '金融行业标签'),
(4, '待激活', '0', '#e6a23c', 4, '0', 'admin', sysdate(), '待激活客户标签'),
(5, '续约中', '0', '#909399', 5, '0', 'admin', sysdate(), '续约中客户标签');



-- ============================================================
-- File: mk_supplement.sql
-- ============================================================

-- =============================================
-- MK 营销管理子系统 - 补充SQL脚本
-- 补充：缺失字典类型 + 缺失菜单 + 活动参与人测试数据
-- =============================================

-- =============================================
-- 一、补充缺失的字典类型
-- =============================================

-- 活动形式字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('活动形式', 'marketing_activity_form', '0', 'admin', sysdate(), '活动形式字典');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '线上', '0', 'marketing_activity_form', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '线下', '1', 'marketing_activity_form', '', 'success', 'N', '0', 'admin', sysdate(), '');

-- 付款方式字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('付款方式', 'marketing_payment_method', '0', 'admin', sysdate(), '付款方式字典');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '一次性', '0', 'marketing_payment_method', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '分期', '1', 'marketing_payment_method', '', 'warning', 'N', '0', 'admin', sysdate(), '');

-- 标签类型字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('标签类型', 'marketing_tag_type', '0', 'admin', sysdate(), '客户标签类型字典');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '手动', '0', 'marketing_tag_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '自动', '1', 'marketing_tag_type', '', 'success', 'N', '0', 'admin', sysdate(), '');


-- =============================================
-- 二、补充缺失的菜单
-- =============================================

-- 活动参与人菜单（挂在营销活动目录下）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4130, '活动参与人', 4004, 2, 'participant', 'mk/participant/index', '', 'MkParticipant', 1, 0, 'C', '0', '0', 'marketing:participant:list', 'people', 'admin', sysdate(), '活动参与人管理');

-- 活动参与人按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4131, '参与人查询', 4130, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:participant:query', '#', 'admin', sysdate(), ''),
(4132, '参与人新增', 4130, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:participant:add', '#', 'admin', sysdate(), ''),
(4133, '参与人修改', 4130, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:participant:edit', '#', 'admin', sysdate(), ''),
(4134, '参与人删除', 4130, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:participant:remove', '#', 'admin', sysdate(), ''),
(4135, '参与人导出', 4130, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:participant:export', '#', 'admin', sysdate(), '');

-- 客户标签菜单（挂在客户管理目录下）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4036, '客户标签', 4001, 4, 'tag', 'mk/tag/index', '', 'MkCustomerTag', 1, 0, 'C', '0', '0', 'marketing:tag:list', 'tag', 'admin', sysdate(), '客户标签管理');

-- 客户标签按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4037, '标签查询', 4036, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:tag:query', '#', 'admin', sysdate(), ''),
(4038, '标签新增', 4036, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:tag:add', '#', 'admin', sysdate(), ''),
(4039, '标签修改', 4036, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:tag:edit', '#', 'admin', sysdate(), ''),
(4048, '标签删除', 4036, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:tag:remove', '#', 'admin', sysdate(), ''),
(4049, '标签导出', 4036, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:tag:export', '#', 'admin', sysdate(), '');


-- =============================================
-- 三、给admin角色分配新增菜单权限
-- =============================================

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 4036), (1, 4037), (1, 4038), (1, 4039), (1, 4048), (1, 4049),
(1, 4130), (1, 4131), (1, 4132), (1, 4133), (1, 4134), (1, 4135);


-- =============================================
-- 四、活动参与人测试数据（5条）
-- =============================================

INSERT INTO mk_activity_participant (participant_id, activity_id, lead_id, company_name, contact_name, contact_phone, contact_email, position, participate_status, sign_time, create_by, create_time, remark) VALUES
(1, 1, NULL, '北京智云科技有限公司', '张伟', '13800138001', 'zhangwei@zhiyun-tech.com', '总经理', '1', '2026-06-15 14:05:00', 'admin', sysdate(), '线上峰会签到'),
(2, 1, NULL, '上海宏远制造股份有限公司', '王强', '13700137001', 'wangqiang@hongyuan-mfg.cn', 'CTO', '1', '2026-06-15 14:10:00', 'admin', sysdate(), '线上峰会签到'),
(3, 2, NULL, '深圳创新金融服务有限公司', '刘洋', '13600136001', 'liuyang@cx-fin.com', '运营总监', '1', '2026-06-20 19:03:00', 'admin', sysdate(), 'Webinar签到'),
(4, 3, NULL, '成都新视界科技有限公司', '赵敏', '13300133001', 'zhaomin@xinshijie.com', '市场总监', '1', '2026-07-18 09:15:00', 'admin', sysdate(), '展会现场签到'),
(5, 3, NULL, '武汉光谷生物医药有限公司', '孙磊', '13400134001', 'sunlei@guanggu-bio.com', 'IT经理', '0', NULL, 'admin', sysdate(), '已报名未签到');



-- ============================================================
-- File: mk_number_rule.sql
-- ============================================================

-- =============================================
-- MK 营销管理 - 编号规则管理
-- 功能：活动编号、客户编号、线索编号、商机编号、订单编号自动生成
-- =============================================

-- =============================================
-- 一、创建编号规则表
-- =============================================
DROP TABLE IF EXISTS `mk_number_rule`;
CREATE TABLE `mk_number_rule` (
  `rule_id`         BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '规则ID',
  `rule_code`       VARCHAR(50)   NOT NULL                 COMMENT '规则编码（业务标识，如 activity/customer/lead/opportunity/order）',
  `module`          VARCHAR(20)   DEFAULT 'mk'             COMMENT '所属模块(mk=营销,dms=设备,wms=仓储,pms=采购,safety=安全生产)',
  `rule_name`       VARCHAR(100)  NOT NULL                 COMMENT '规则名称',
  `prefix`          VARCHAR(20)   DEFAULT ''               COMMENT '编号前缀',
  `date_format`     VARCHAR(20)   DEFAULT ''               COMMENT '日期格式（yyyyMMdd=按天,yyyyMM=按月,yyyy=按年,空=不包含日期）',
  `reset_type`      CHAR(1)       DEFAULT '0'              COMMENT '序号重置类型（0=不重置,1=按天,2=按月,3=按年,4=按动态前缀）',
  `seq_length`      INT(11)       DEFAULT 4                COMMENT '序列号长度（不足前补零）',
  `seq_start`       BIGINT(20)    DEFAULT 1                COMMENT '序列号起始值',
  `step`            INT(11)       DEFAULT 1                COMMENT '每次增长步长',
  `current_seq`     BIGINT(20)    DEFAULT 0                COMMENT '当前序列号',
  `current_date_str` VARCHAR(20)  DEFAULT ''               COMMENT '当前日期串（用于判断是否需要重置序列号）',
  `connector`       VARCHAR(5)    DEFAULT ''               COMMENT '前缀与日期之间的连接符',
  `prefix_field`    VARCHAR(50)   DEFAULT NULL             COMMENT '动态前缀关联字段名',
  `prefix_field_dict_type` VARCHAR(100) DEFAULT NULL        COMMENT '动态前缀关联字典类型',
  `prefix_field_enabled` CHAR(1)  DEFAULT '0'              COMMENT '是否启用动态前缀(0=否,1=是)',
  `status`          CHAR(1)       DEFAULT '0'              COMMENT '状态（0=正常,1=停用）',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME                               COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME                               COMMENT '更新时间',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`rule_id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编号规则表';

-- 编号规则动态前缀映射表
CREATE TABLE IF NOT EXISTS `mk_number_rule_prefix` (
  `prefix_id`     BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `rule_id`       BIGINT(20)    NOT NULL                 COMMENT '规则ID',
  `field_value`   VARCHAR(50)   NOT NULL                 COMMENT '字段值（字典值）',
  `field_label`   VARCHAR(100)  DEFAULT ''               COMMENT '字段标签（字典标签）',
  `prefix`        VARCHAR(20)   DEFAULT ''               COMMENT '对应前缀',
  `enabled`       CHAR(1)       DEFAULT '1'              COMMENT '是否启用（0=否,1=是）',
  `current_seq`   BIGINT(20)    DEFAULT 0                COMMENT '当前序列号（按动态前缀重置时使用）',
  `current_date_str` VARCHAR(20) DEFAULT ''              COMMENT '当前日期串',
  `create_by`     VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`   DATETIME                               COMMENT '创建时间',
  `update_by`     VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`   DATETIME                               COMMENT '更新时间',
  PRIMARY KEY (`prefix_id`),
  KEY `idx_rule_id` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编号规则动态前缀映射表';

-- =============================================
-- 二、初始化默认编号规则
-- =============================================
INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`) VALUES
('activity',   '活动编号', 'HD',  'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '营销活动编号，每日重置'),
('customer',   '客户编号', 'CUS', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '企业客户编号，每日重置'),
('lead',       '线索编号', 'XS',  'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '线索编号，每日重置'),
('opportunity','商机编号', 'SJ',  'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '商机编号，每日重置'),
('order',      '订单编号', 'DD',  'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '订单编号，每日重置'),
('contract',   '合同编号', 'HT',  'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '合同编号，每日重置');

-- =============================================
-- 三、编号规则重置类型字典
-- =============================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('编号重置类型', 'mk_number_reset_type', '0', 'admin', sysdate(), '编号规则序号重置类型');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '不重置', '0', 'mk_number_reset_type', '', 'info',    'N', '0', 'admin', sysdate(), ''),
(2, '按天',   '1', 'mk_number_reset_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(3, '按月',   '2', 'mk_number_reset_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '按年',   '3', 'mk_number_reset_type', '', 'warning', 'N', '0', 'admin', sysdate(), '');

-- =============================================
-- 四、编号规则管理菜单（挂在营销管理目录下）
-- =============================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4140, '编号规则', 4000, 7, 'numberRule', 'mk/numberRule/index', '', 'MkNumberRule', 1, 0, 'C', '0', '0', 'marketing:numberRule:list', 'number', 'admin', sysdate(), '编号规则管理');

-- 编号规则按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4141, '规则查询', 4140, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:numberRule:query', '#', 'admin', sysdate(), ''),
(4142, '规则新增', 4140, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:numberRule:add', '#', 'admin', sysdate(), ''),
(4143, '规则修改', 4140, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:numberRule:edit', '#', 'admin', sysdate(), ''),
(4144, '规则删除', 4140, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:numberRule:remove', '#', 'admin', sysdate(), ''),
(4145, '规则导出', 4140, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:numberRule:export', '#', 'admin', sysdate(), '');

-- =============================================
-- 五、给admin角色分配编号规则菜单权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 4140), (1, 4141), (1, 4142), (1, 4143), (1, 4144), (1, 4145);



-- ============================================================
-- File: mk_contract_approve_log.sql
-- ============================================================

-- 合同审批记录表
CREATE TABLE IF NOT EXISTS mk_contract_approve_log (
    log_id          BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    contract_id     BIGINT        NOT NULL                 COMMENT '合同ID',
    action_type     VARCHAR(10)   NOT NULL                 COMMENT '操作类型（1提交审批 2审批通过 3审批驳回 4终止）',
    approve_opinion VARCHAR(500)                           COMMENT '审批意见',
    approve_by      VARCHAR(64)                            COMMENT '审批人',
    approve_time    DATETIME                               COMMENT '审批时间',
    PRIMARY KEY (log_id),
    KEY idx_contract_id (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同审批记录表';



-- ============================================================
-- File: openapi.sql
-- ============================================================

-- ================================================================
-- 开放接口(Open API)管理 - 建表 + 菜单
-- 功能：管理对接方API Key，用于 /openapi/v1/** 开放接口鉴权
-- 执行：在 MySQL 中执行本脚本即可
-- ================================================================

-- 1. 开放接口应用表
DROP TABLE IF EXISTS openapi_app;
CREATE TABLE openapi_app (
  app_id          BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '应用ID',
  app_name        VARCHAR(100)  NOT NULL                 COMMENT '应用名称（对接方）',
  app_key         VARCHAR(128)  NOT NULL                 COMMENT 'API Key',
  allowed_modules VARCHAR(500) DEFAULT '*'              COMMENT '允许访问的模块，逗号分隔如 wms,dms；* 表示全部',
  status          CHAR(1)       DEFAULT '0'             COMMENT '状态（0正常 1停用）',
  del_flag        CHAR(1)       DEFAULT '0'             COMMENT '删除标志（0存在 1删除）',
  create_by       VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  create_time     DATETIME                              COMMENT '创建时间',
  update_by       VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  update_time     DATETIME                              COMMENT '更新时间',
  remark          VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (app_id),
  UNIQUE KEY uk_app_key (app_key)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='开放接口应用表';

-- 2. 插入一个默认测试应用
INSERT INTO openapi_app (app_name, app_key, allowed_modules, status, create_by, create_time, remark)
VALUES ('默认测试应用', 'test-api-key-2026', '*', '0', 'admin', sysdate(), '默认测试Key，生产环境请替换');

-- 3. 菜单：在"系统管理"下新增"开放接口管理" (menu_id=119)
INSERT INTO sys_menu VALUES
('119', '开放接口', '1', '9', 'openapi', 'system/openapi/index', '', '', 1, 0, 'C', '0', '0', 'system:openapi:list', 'international', 'admin', sysdate(), '', NULL, '开放接口应用Key管理');

-- 3.1 按钮权限
INSERT INTO sys_menu VALUES
('1200', '应用查询', '119', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:openapi:query',  '#', 'admin', sysdate(), '', NULL, ''),
('1201', '应用新增', '119', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:openapi:add',    '#', 'admin', sysdate(), '', NULL, ''),
('1202', '应用修改', '119', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:openapi:edit',   '#', 'admin', sysdate(), '', NULL, ''),
('1203', '应用删除', '119', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:openapi:remove', '#', 'admin', sysdate(), '', NULL, '');

-- 4. 为超管角色分配菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 119), (1, 1200), (1, 1201), (1, 1202), (1, 1203);

-- ================================================================
-- 回滚：
-- DELETE FROM sys_role_menu WHERE menu_id IN (119,1200,1201,1202,1203);
-- DELETE FROM sys_menu WHERE menu_id IN (119,1200,1201,1202,1203);
-- DROP TABLE IF EXISTS openapi_app;
-- ================================================================



-- ============================================================
-- File: dms_wms_number_rules.sql
-- ============================================================

-- DMS和WMS编号规则初始化
-- 执行前请确保 mk_number_rule 表已存在

-- =============================================
-- 0. 增加 module 及动态前缀相关字段（兼容旧版数据库）
-- =============================================
ALTER TABLE `mk_number_rule` ADD COLUMN IF NOT EXISTS `module` VARCHAR(20) DEFAULT 'mk' COMMENT '所属模块(mk=营销,dms=设备,wms=仓储,pms=采购,safety=安全生产)' AFTER `rule_code`;
ALTER TABLE `mk_number_rule` ADD COLUMN IF NOT EXISTS `prefix_field` VARCHAR(50) DEFAULT NULL COMMENT '动态前缀关联字段名' AFTER `connector`;
ALTER TABLE `mk_number_rule` ADD COLUMN IF NOT EXISTS `prefix_field_dict_type` VARCHAR(100) DEFAULT NULL COMMENT '动态前缀关联字典类型' AFTER `prefix_field`;
ALTER TABLE `mk_number_rule` ADD COLUMN IF NOT EXISTS `prefix_field_enabled` CHAR(1) DEFAULT '0' COMMENT '是否启用动态前缀(0=否,1=是)' AFTER `prefix_field_dict_type`;

CREATE TABLE IF NOT EXISTS `mk_number_rule_prefix` (
  `prefix_id`     BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `rule_id`       BIGINT(20)    NOT NULL                 COMMENT '规则ID',
  `field_value`   VARCHAR(50)   NOT NULL                 COMMENT '字段值（字典值）',
  `field_label`   VARCHAR(100)  DEFAULT ''               COMMENT '字段标签（字典标签）',
  `prefix`        VARCHAR(20)   DEFAULT ''               COMMENT '对应前缀',
  `enabled`       CHAR(1)       DEFAULT '1'              COMMENT '是否启用（0=否,1=是）',
  `current_seq`   BIGINT(20)    DEFAULT 0                COMMENT '当前序列号（按动态前缀重置时使用）',
  `current_date_str` VARCHAR(20) DEFAULT ''              COMMENT '当前日期串',
  `create_by`     VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`   DATETIME                               COMMENT '创建时间',
  `update_by`     VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`   DATETIME                               COMMENT '更新时间',
  PRIMARY KEY (`prefix_id`),
  KEY `idx_rule_id` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编号规则动态前缀映射表';

-- 更新已有规则的模块归属
UPDATE `mk_number_rule` SET module='mk' WHERE rule_code IN ('activity','customer','lead','opportunity','order','contract');
UPDATE `mk_number_rule` SET module='dms' WHERE rule_code LIKE 'dms_%';
UPDATE `mk_number_rule` SET module='wms' WHERE rule_code LIKE 'wms_%';

-- DMS 设备管理模块编号规则
INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_equipment', '设备编号', 'SB', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '设备编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_equipment');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_spare_part', '备件编号', 'BJ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '备件编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_spare_part');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_spare_in', '备件入库单据号', 'BJIN', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '备件入库单据号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_spare_in');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_spare_out', '备件出库单据号', 'BJOUT', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '备件出库单据号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_spare_out');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_work_order', '工单号', 'WO', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '维修工单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_work_order');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_inspection_route', '巡检路线编号', 'RT', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '巡检路线编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_inspection_route');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_inspection_task', '点检任务编号', 'INS', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '点检任务编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_inspection_task');

-- WMS 仓库管理模块编号规则
INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material', '物料编号', 'WL', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '物料编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_warehouse', '仓库编码', 'WH', '', '0', 3, 1, 1, 0, '', '', '0', 'admin', sysdate(), '仓库编码，不重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_warehouse');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_supplier', '供应商编码', 'GYS', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '供应商编码，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_supplier');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_inbound', '入库单号', 'IN', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '入库单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_inbound');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_outbound', '出库单号', 'OUT', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '出库单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_outbound');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_move', '移库单号', 'MV', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '移库单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_move');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_stocktake', '盘点单号', 'ST', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '盘点单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_stocktake');

-- =============================================
-- 在设备管理、仓储管理菜单下添加编号规则入口
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '编号规则', 3000, 99, 'dmsNumberRule', 'mk/numberRule/index', '', 'DmsNumberRule', 1, 0, 'C', '0', '0', 'marketing:numberRule:list', 'number', 'admin', sysdate(), '设备管理编号规则'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='编号规则' AND parent_id=3000);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '编号规则', 2000, 99, 'wmsNumberRule', 'mk/numberRule/index', '', 'WmsNumberRule', 1, 0, 'C', '0', '0', 'marketing:numberRule:list', 'number', 'admin', sysdate(), '仓储管理编号规则'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='编号规则' AND parent_id=2000);

-- 给admin角色分配编号规则菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id IN (3000, 2000)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);

-- =============================================
-- 更新各模块编号规则菜单的query参数，传递module标识
-- query字段使用JSON格式（前端sidebar通过JSON.parse解析）
-- =============================================
UPDATE sys_menu SET query='{"module":"mk"}' WHERE menu_name='编号规则' AND parent_id=4000;
UPDATE sys_menu SET query='{"module":"dms"}' WHERE menu_name='编号规则' AND parent_id=3000;
UPDATE sys_menu SET query='{"module":"wms"}' WHERE menu_name='编号规则' AND parent_id=2000;

-- =============================================
-- 物料按类型分组编码规则（原材料/半成品/成品/辅料）
-- =============================================
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material_0', 'wms', '物料编号-原材料', 'YL', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '原材料编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material_0');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material_1', 'wms', '物料编号-半成品', 'BC', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '半成品编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material_1');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material_2', 'wms', '物料编号-成品', 'CP', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '成品编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material_2');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material_3', 'wms', '物料编号-辅料', 'FL', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '辅料编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material_3');



-- ============================================================
-- File: safety_number_rule.sql
-- 安全生产管理 - 编号规则种子数据
-- ============================================================

-- 安全生产管理模块编号规则（幂等插入）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_area', 'safety', '区域编码', 'QY', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '安全生产区域编码，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_area');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_equipment', 'safety', '特种设备编号', 'SB', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '特种设备编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_equipment');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_risk_point', 'safety', '风险点编号', 'FX', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '风险点编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_risk_point');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_inspection_task', 'safety', '巡检任务编号', 'XJ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '巡检任务编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_inspection_task');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_hazard', 'safety', '隐患编号', 'YH', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '隐患编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_hazard');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_drill', 'safety', '演练编号', 'YL', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '应急演练编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_drill');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_incident', 'safety', '事故编号', 'SG', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '事故编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_incident');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_material', 'safety', '危化品编号', 'WH', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '危化品编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_material');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_emergency_material', 'safety', '应急物资编码', 'WZ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '应急物资编码，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_emergency_material');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_emergency_plan', 'safety', '预案编号', 'YA', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '应急预案编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_emergency_plan');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_training_plan', 'safety', '培训计划编号', 'PX', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '培训计划编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_training_plan');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_training_course', 'safety', '课程编号', 'KC', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '培训课程编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_training_course');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_worker', 'safety', '工号', 'GY', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '安全人员工号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_worker');

-- 安全生产管理 - 编码规则菜单（挂在安全生产管理目录下）
SET @safety_parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0 LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '编码规则', @safety_parent_id, 99, 'numberRule', 'mk/numberRule/index', '{"module":"safety"}', 'SafetyNumberRule', 1, 0, 'C', '0', '0', 'safety:numberRule:list', 'number', 'admin', sysdate(), '安全生产编码规则管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '编码规则' AND parent_id = @safety_parent_id AND component = 'mk/numberRule/index');

SET @safety_rule_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '编码规则' AND parent_id = @safety_parent_id AND component = 'mk/numberRule/index' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则查询', @safety_rule_menu_id, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'safety:numberRule:query', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:numberRule:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则新增', @safety_rule_menu_id, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'safety:numberRule:add', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:numberRule:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则修改', @safety_rule_menu_id, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'safety:numberRule:edit', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:numberRule:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则删除', @safety_rule_menu_id, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'safety:numberRule:remove', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:numberRule:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则导出', @safety_rule_menu_id, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'safety:numberRule:export', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:numberRule:export');

-- 给admin角色(1)和普通角色(2)分配编码规则菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'safety:numberRule:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE perms LIKE 'safety:numberRule:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 2);



-- ============================================================
-- File: apidoc_menu.sql
-- ============================================================

-- ================================================================
-- 业务模块接口文档 - 菜单初始化脚本
-- 功能：在"系统管理"目录下新增"接口文档"子菜单，集中展示WMS和DMS全接口
-- 说明：该页面支持免登录独立访问（URL: /apidoc），同时也作为系统管理子菜单供登录用户使用
-- 执行：在 MySQL 中执行本脚本即可
-- ================================================================

-- 1. 新增接口文档菜单（挂在"系统管理"目录下，parent_id=1）
-- 使用 menu_id=118（116=代码生成、117=系统接口已占用）
-- path=apidoc，component=tool/apidoc/index，登录后访问路径为 /system/apidoc
INSERT INTO sys_menu VALUES
('118', '接口文档', '1', '8', 'apidoc', 'tool/apidoc/index', '', '', 1, 0, 'C', '0', '0', 'tool:apidoc:list', 'documentation', 'admin', sysdate(), '', NULL, '业务模块接口文档集中管理（免登录可访问 /apidoc）');

-- 2. 为超管角色(admin, role_id=1)分配该菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 118);

-- ================================================================
-- 如需回滚，执行以下语句：
-- DELETE FROM sys_role_menu WHERE menu_id = 118;
-- DELETE FROM sys_menu WHERE menu_id = 118;
-- ================================================================



-- ============================================================
-- File: openapidoc_menu.sql
-- ============================================================

-- ================================================================
-- 开放接口文档 - 菜单初始化脚本
-- 功能：在"系统管理"目录下新增"开放接口文档"子菜单，展示对外Open API接口文档
-- 说明：该页面支持免登录独立访问（URL: /openapi-doc），同时也作为系统管理子菜单供登录用户使用
-- 执行：在 MySQL 中执行本脚本即可
-- ================================================================

-- 1. 新增开放接口文档菜单（挂在"系统管理"目录下，parent_id=1）
-- 使用 menu_id=120（118=接口文档、119=开放接口(API Key管理)已占用）
-- path=openapidoc，component=tool/openapidoc/index，登录后访问路径为 /system/openapidoc
INSERT INTO sys_menu VALUES
('120', '开放接口文档', '1', '9', 'openapidoc', 'tool/openapidoc/index', '', '', 1, 0, 'C', '0', '0', 'tool:openapidoc:list', 'guide', 'admin', sysdate(), '', NULL, '开放接口(Open API)文档，供外部系统对接（免登录可访问 /openapi-doc）');

-- 2. 为超管角色(admin, role_id=1)分配该菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 120);

-- ================================================================
-- 如需回滚，执行以下语句：
-- DELETE FROM sys_role_menu WHERE menu_id = 120;
-- DELETE FROM sys_menu WHERE menu_id = 120;
-- ================================================================



-- ============================================================
-- File: add_contract_approve_menu.sql
-- ============================================================

-- 合同审批权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 4096, '合同审批', 4090, 6, '', '', '', 1, 0, 'F', '0', '0', 'marketing:contract:approve', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 4096);

-- 将审批权限分配给管理员角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 4096
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 4096);



-- ============================================================
-- End of merged SQL
-- ============================================================
SET FOREIGN_KEY_CHECKS = 1;

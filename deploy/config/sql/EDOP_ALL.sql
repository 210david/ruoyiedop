-- ============================================================
-- 企业数字化运营平台 - 数据库初始化脚本（合并版）
-- 包含: ry基础表 + quartz定时任务表 + wms仓库管理表 + dms设备管理表
-- 使用方法: mysql -u root -p < EDOP_ALL.sql
-- ============================================================

-- 创建数据库
DROP DATABASE IF EXISTS `ry-vue`;
CREATE DATABASE `ry-vue` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ry-vue`;

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
insert into sys_role values('1', '超级管理员',  'admin',  1, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '超级管理员');
insert into sys_role values('2', '普通角色',    'common', 2, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, '普通角色');


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
) engine=innodb auto_increment=1 comment = '代码生成业务表字段';DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
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

commit;-- =============================================
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

-- 2. 供应商
DROP TABLE IF EXISTS wms_supplier;
CREATE TABLE wms_supplier (
    supplier_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '供应商ID',
    supplier_code    VARCHAR(64)  NOT NULL                 COMMENT '供应商编码',
    supplier_name    VARCHAR(255) NOT NULL                 COMMENT '供应商名称',
    contact_person   VARCHAR(64)                           COMMENT '联系人',
    contact_phone    VARCHAR(20)                           COMMENT '联系电话',
    address          VARCHAR(500)                          COMMENT '地址',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (supplier_id),
    UNIQUE KEY uk_supplier_code (supplier_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

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
-- 二、字典数据（14个字典类型）
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
('移库状态', 'wms_move_status', '0', 'admin', sysdate(), '移库状态字典');

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
(4, '已作废', '3', 'wms_move_status', '', 'danger', 'N', '0', 'admin', sysdate(), '');


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
(2122, '预警导出', 2120, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stockAlert:export', '#', 'admin', sysdate(), '');

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
(1, 2080), (1, 2081), (1, 2082), (1, 2083), (1, 2084), (1, 2085),
(1, 2090), (1, 2091), (1, 2092), (1, 2093), (1, 2094), (1, 2095),
(1, 2100), (1, 2101), (1, 2102), (1, 2103), (1, 2104), (1, 2105),
(1, 2110), (1, 2111),
(1, 2120), (1, 2121), (1, 2122);
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
(3025, '设备导出', 3020, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:equipment:export', '#', 'admin', sysdate(), '');

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

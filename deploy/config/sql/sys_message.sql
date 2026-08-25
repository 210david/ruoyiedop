-- ============================================
-- 消息中心建表脚本（v2 - 2026-08-25）
-- 数据库: ry-vue
-- 1. 业务来源改为字典下拉（仓库管理、质量管理等）
-- 2. 状态改为已读/未读
-- 3. 去掉创建者字段、删除/修改/阅读用户功能
-- ============================================

-- ----------------------------
-- 1、消息中心表
-- ----------------------------
drop table if exists sys_message;
create table sys_message (
  message_id        bigint(20)      not null auto_increment    comment '消息ID',
  message_title     varchar(200)    not null                   comment '消息标题',
  message_type      varchar(2)      default '1'                comment '消息类型（1系统通知 2业务预警 3审批消息 4待办事项）',
  message_content   longtext                                   comment '消息内容',
  message_level     varchar(2)      default '1'                comment '消息级别（1普通 2重要 3紧急）',
  biz_source        varchar(50)     default null               comment '业务来源（字典：仓库管理、质量管理等）',
  biz_id            bigint(20)      default null               comment '业务ID（关联的业务单据ID）',
  redirect_url      varchar(500)   default null               comment '跳转链接（点击消息后跳转的前端路由）',
  recipient_id      bigint(20)      default null               comment '接收人ID（为空则全员可见）',
  status            char(1)         default '1'                comment '消息状态（0待发送 1已发送 2已撤回）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)   default null               comment '备注',
  primary key (message_id),
  key idx_recipient (recipient_id),
  key idx_status (status),
  key idx_create_time (create_time)
) engine=innodb auto_increment=1 comment='消息中心表';

-- ----------------------------
-- 2、消息已读记录表
-- ----------------------------
drop table if exists sys_message_read;
create table sys_message_read (
  read_id           bigint(20)      not null auto_increment    comment '已读主键',
  message_id        bigint(20)      not null                   comment '消息ID',
  user_id           bigint(20)      not null                   comment '用户ID',
  read_time         datetime        not null                   comment '阅读时间',
  primary key (read_id),
  unique key uk_user_message (user_id, message_id) comment '同一用户同一消息只记录一次'
) engine=innodb auto_increment=1 comment='消息已读记录表';

-- ----------------------------
-- 3、字典数据 - 消息类型
-- ----------------------------
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '消息类型', 'sys_message_type', '0', 'admin', sysdate(), '消息中心消息类型'
where not exists (select 1 from sys_dict_type where dict_type = 'sys_message_type');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '系统通知', '1', 'sys_message_type', '', 'primary', 'Y', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_type' and dict_value = '1');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '业务预警', '2', 'sys_message_type', '', 'danger', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_type' and dict_value = '2');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '审批消息', '3', 'sys_message_type', '', 'warning', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_type' and dict_value = '3');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 4, '待办事项', '4', 'sys_message_type', '', 'info', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_type' and dict_value = '4');

-- ----------------------------
-- 4、字典数据 - 消息级别
-- ----------------------------
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '消息级别', 'sys_message_level', '0', 'admin', sysdate(), '消息中心消息级别'
where not exists (select 1 from sys_dict_type where dict_type = 'sys_message_level');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '普通', '1', 'sys_message_level', '', 'info', 'Y', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_level' and dict_value = '1');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '重要', '2', 'sys_message_level', '', 'warning', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_level' and dict_value = '2');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '紧急', '3', 'sys_message_level', '', 'danger', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_level' and dict_value = '3');

-- ----------------------------
-- 5、字典数据 - 业务来源
-- ----------------------------
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '消息业务来源', 'sys_message_biz_source', '0', 'admin', sysdate(), '消息中心业务来源（各业务模块）'
where not exists (select 1 from sys_dict_type where dict_type = 'sys_message_biz_source');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '仓库管理', 'wms', 'sys_message_biz_source', '', 'primary', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_biz_source' and dict_value = 'wms');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '质量管理', 'qms', 'sys_message_biz_source', '', 'warning', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_biz_source' and dict_value = 'qms');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '生产管理', 'mms', 'sys_message_biz_source', '', 'success', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_biz_source' and dict_value = 'mms');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 4, '采购管理', 'pms', 'sys_message_biz_source', '', 'info', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_biz_source' and dict_value = 'pms');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 5, '设备管理', 'dms', 'sys_message_biz_source', '', 'danger', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_biz_source' and dict_value = 'dms');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 6, '营销管理', 'mk', 'sys_message_biz_source', '', 'primary', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_biz_source' and dict_value = 'mk');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 7, '安全生产', 'safety', 'sys_message_biz_source', '', 'danger', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_biz_source' and dict_value = 'safety');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 8, '系统管理', 'system', 'sys_message_biz_source', '', 'info', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_biz_source' and dict_value = 'system');

-- ----------------------------
-- 6、字典数据 - 阅读状态（已读/未读）
-- ----------------------------
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '消息阅读状态', 'sys_message_read_status', '0', 'admin', sysdate(), '消息中心阅读状态'
where not exists (select 1 from sys_dict_type where dict_type = 'sys_message_read_status');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '未读', '0', 'sys_message_read_status', '', 'info', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_read_status' and dict_value = '0');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '已读', '1', 'sys_message_read_status', '', 'success', 'N', '0', 'admin', sysdate(), null
where not exists (select 1 from sys_dict_data where dict_type = 'sys_message_read_status' and dict_value = '1');

-- ----------------------------
-- 7、清理旧字典 - 消息状态（不再使用）
-- ----------------------------
delete from sys_dict_data where dict_type = 'sys_message_status';
delete from sys_dict_type where dict_type = 'sys_message_status';

-- ----------------------------
-- 8、清理旧菜单权限 - 修改、删除
-- ----------------------------
delete from sys_menu where perms = 'system:message:edit';
delete from sys_menu where perms = 'system:message:remove';

-- ----------------------------
-- 9、菜单配置 - 消息中心（根目录，生产管控后面）
-- ----------------------------
insert into sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '消息中心', '0', '30', 'message', 'system/message/index', '', '', 1, 0, 'C', '0', '0', 'system:message:list', 'message', 'admin', sysdate(), '消息中心菜单'
where not exists (select 1 from sys_menu where menu_name = '消息中心' and parent_id = 0);

set @messageMenuId = (select menu_id from sys_menu where menu_name = '消息中心' and parent_id = 0 and path = 'message');

insert into sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '消息查询', @messageMenuId, '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:message:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where perms = 'system:message:query');
-- 消息新增权限已去掉（不再需要）
DELETE FROM sys_menu WHERE perms = 'system:message:add';

-- 清理系统管理下重复的消息中心菜单
DELETE FROM sys_menu WHERE menu_name = '消息中心' AND parent_id = 1;

-- ----------------------------
-- 10、插入测试消息数据
-- ----------------------------
insert into sys_message (message_title, message_type, message_content, message_level, biz_source, biz_id, redirect_url, status, create_by, create_time) values
('库存预警：物料M-1024低于安全库存', '2', '物料编码M-1024（轴承座）当前库存数量为15件，低于设定的安全库存20件，请及时补货。', '3', 'wms', 1024, '/wms/stockAlert', '1', 'admin', sysdate()),
('新工单待处理：WO-2026-0825-003', '4', '设备CNC-001日常维护工单已下达，请安排维修人员处理。工单编号：WO-2026-0825-003。', '2', 'mms', 256, '/dms/workorder', '1', 'admin', sysdate()),
('质量异常通知：批次BN-20260824', '2', '来料检验发现批次BN-20260824的尺寸偏差超标，已自动生成NCR单，请质量工程师跟进处理。', '2', 'qms', 128, '/qms/ncr', '1', 'admin', sysdate()),
('系统维护通知', '1', '系统将于本周六凌晨2:00-4:00进行例行维护升级，期间系统暂停使用，请提前安排相关工作。', '1', 'system', null, null, '1', 'admin', sysdate()),
('审批提醒：退货单RT-20260820-001', '3', '退货单RT-20260820-001待您审批，客户：XX科技有限公司，退货金额：￥12,500.00，请尽快处理。', '2', 'wms', 88, '/wms/return', '1', 'admin', sysdate()),
('采购订单审批：PO-2026-0825-009', '3', '采购订单PO-2026-0825-009（供应商：A供应商）已提交审批，采购金额：￥86,200.00，请尽快审批。', '2', 'pms', 512, '/pms/purchase', '1', 'admin', sysdate()),
('设备维护提醒：CNC-002', '4', '设备CNC-002已达保养周期（运行2000小时），请安排预防性维护。', '1', 'dms', 789, '/dms/equipment', '1', 'admin', sysdate()),
('安全演练通知', '1', '本月安全演练定于25日14:00进行，演练主题：紧急疏散，请全体员工准时参加。', '2', 'safety', 456, '/safety/drill', '1', 'admin', sysdate());

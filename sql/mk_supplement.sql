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

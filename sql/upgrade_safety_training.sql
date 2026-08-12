-- ============================================================
-- 安全教育培训模块 · 方案B功能补全
-- 1. 新建课程库表 biz_safety_training_course
-- 2. 改造培训记录表 biz_safety_training_record（去掉考试分数/是否合格/参训人员，增加课程ID/培训地点）
-- 3. 新建参训人员签到表 biz_safety_training_attendee（含考试分数/是否合格）
-- 4. 新建证书表 biz_safety_training_cert
-- ============================================================

-- ----------------------------------------------------------------
-- 1. 课程库表
-- ----------------------------------------------------------------
DROP TABLE IF EXISTS biz_safety_training_course;
CREATE TABLE biz_safety_training_course (
    course_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '课程ID',
    course_code        VARCHAR(64)                           COMMENT '课程编号',
    course_name        VARCHAR(255) NOT NULL                 COMMENT '课程名称',
    course_type        VARCHAR(64)                           COMMENT '课程类别（字典 safety_course_type）',
    course_form        VARCHAR(20)                           COMMENT '课程形式（字典 safety_course_form：内训/外训/视频）',
    hours              DECIMAL(10,2)                         COMMENT '标准课时',
    instructor         VARCHAR(128)                          COMMENT '默认讲师',
    description        TEXT                                  COMMENT '课程描述',
    material_url       VARCHAR(500)                          COMMENT '课件地址',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (course_id),
    UNIQUE KEY uk_course_code (course_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训课程库表';

-- ----------------------------------------------------------------
-- 2. 改造培训记录表
-- ----------------------------------------------------------------
-- 去掉 exam_score, is_pass, persons
-- 增加 course_id, training_location
ALTER TABLE biz_safety_training_record ADD COLUMN course_id BIGINT DEFAULT NULL COMMENT '关联课程ID' AFTER plan_id;
ALTER TABLE biz_safety_training_record ADD COLUMN training_location VARCHAR(255) DEFAULT NULL COMMENT '培训地点' AFTER training_date;
ALTER TABLE biz_safety_training_record DROP COLUMN exam_score;
ALTER TABLE biz_safety_training_record DROP COLUMN is_pass;
ALTER TABLE biz_safety_training_record DROP COLUMN persons;

-- ----------------------------------------------------------------
-- 3. 参训人员签到表（含考试成绩）
-- ----------------------------------------------------------------
DROP TABLE IF EXISTS biz_safety_training_attendee;
CREATE TABLE biz_safety_training_attendee (
    attendee_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '签到记录ID',
    record_id          BIGINT       NOT NULL                 COMMENT '关联培训记录ID',
    user_id            BIGINT                                COMMENT '参训人员ID（关联sys_user）',
    user_name          VARCHAR(128)                          COMMENT '参训人员姓名',
    dept_id            BIGINT                                COMMENT '所属部门ID',
    dept_name          VARCHAR(128)                          COMMENT '所属部门名称',
    sign_in_status     CHAR(1)      DEFAULT '1'              COMMENT '签到状态（1已签到 0未签到 2请假）',
    sign_in_time       DATETIME                              COMMENT '签到时间',
    sign_in_method     VARCHAR(20)                           COMMENT '签到方式（字典 safety_sign_in_method）',
    exam_score         DECIMAL(5,2)                          COMMENT '考试分数',
    is_pass            CHAR(1)      DEFAULT '0'              COMMENT '是否合格（0不合格 1合格）',
    exam_time          DATETIME                              COMMENT '考试时间',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (attendee_id),
    KEY idx_record_id (record_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训参训人员签到表';

-- ----------------------------------------------------------------
-- 4. 证书表
-- ----------------------------------------------------------------
DROP TABLE IF EXISTS biz_safety_training_cert;
CREATE TABLE biz_safety_training_cert (
    cert_id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '证书ID',
    cert_no            VARCHAR(64)  NOT NULL                 COMMENT '证书编号',
    cert_name          VARCHAR(255)                          COMMENT '证书名称',
    cert_type          VARCHAR(64)                           COMMENT '证书类型（字典 safety_cert_type）',
    user_id            BIGINT                                COMMENT '获证人员ID（关联sys_user）',
    user_name          VARCHAR(128)                          COMMENT '获证人员姓名',
    dept_id            BIGINT                                COMMENT '所属部门ID',
    dept_name          VARCHAR(128)                          COMMENT '所属部门名称',
    record_id          BIGINT                                COMMENT '关联培训记录ID',
    course_id          BIGINT                                COMMENT '关联课程ID',
    issue_date         DATE                                  COMMENT '发证日期',
    expire_date         DATE                                  COMMENT '有效期至',
    issue_org          VARCHAR(255)                          COMMENT '发证机构',
    attachment          VARCHAR(500)                          COMMENT '证书文件地址',
    remind_status     VARCHAR(20)                           COMMENT '提醒状态',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (cert_id),
    UNIQUE KEY uk_cert_no (cert_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训证书表';

-- ============================================================
-- 字典数据
-- ============================================================

-- 课程形式
DELETE FROM sys_dict_type WHERE dict_type = 'safety_course_form';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('培训课程形式', 'safety_course_form', '0', 'admin', NOW(), '培训课程形式');
DELETE FROM sys_dict_data WHERE dict_type = 'safety_course_form';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '内训', '1', 'safety_course_form', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '外训', '2', 'safety_course_form', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '视频', '3', 'safety_course_form', '', 'info', 'N', '0', 'admin', NOW(), '');

-- 签到方式
DELETE FROM sys_dict_type WHERE dict_type = 'safety_sign_in_method';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('签到方式', 'safety_sign_in_method', '0', 'admin', NOW(), '签到方式');
DELETE FROM sys_dict_data WHERE dict_type = 'safety_sign_in_method';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '扫码', '1', 'safety_sign_in_method', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '人脸', '2', 'safety_sign_in_method', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '手动', '3', 'safety_sign_in_method', '', 'info', 'N', '0', 'admin', NOW(), '');

-- 证书类型
DELETE FROM sys_dict_type WHERE dict_type = 'safety_cert_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('培训证书类型', 'safety_cert_type', '0', 'admin', NOW(), '培训证书类型');
DELETE FROM sys_dict_data WHERE dict_type = 'safety_cert_type';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '三级安全教育合格证', '1', 'safety_cert_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '特种作业操作证', '2', 'safety_cert_type', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '安全培训合格证', '3', 'safety_cert_type', '', 'info', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '其他', '4', 'safety_cert_type', '', 'warning', 'N', '0', 'admin', NOW(), '');

-- 培训计划类型
DELETE FROM sys_dict_type WHERE dict_type = 'safety_training_plan_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('培训计划类型', 'safety_training_plan_type', '0', 'admin', NOW(), '培训计划类型');
DELETE FROM sys_dict_data WHERE dict_type = 'safety_training_plan_type';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '三级安全教育', '1', 'safety_training_plan_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '特种作业培训', '2', 'safety_training_plan_type', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '粉尘防爆培训', '3', 'safety_training_plan_type', '', 'info', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '有限空间培训', '4', 'safety_training_plan_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '危化品培训', '5', 'safety_training_plan_type', '', 'danger', 'N', '0', 'admin', NOW(), ''),
(NULL, 6, '应急处置培训', '6', 'safety_training_plan_type', '', 'primary', 'N', '0', 'admin', NOW(), '');

-- ============================================================
-- 菜单权限
-- ============================================================

-- 获取教育培训目录ID
SELECT @trainingMenuId := menu_id FROM sys_menu WHERE menu_name = '教育培训' AND parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0);

-- ---- 课程管理 ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('课程管理', @trainingMenuId, 1, 'course', 'safety/training/course/index', 1, 0, 'C', '0', '0', 'safety:training:course:list', 'education', 'admin', NOW(), '课程管理菜单');
SET @courseMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('课程查询', @courseMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:training:course:query', '#', 'admin', NOW(), ''),
('课程新增', @courseMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:training:course:add', '#', 'admin', NOW(), ''),
('课程修改', @courseMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:training:course:edit', '#', 'admin', NOW(), ''),
('课程删除', @courseMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:training:course:remove', '#', 'admin', NOW(), ''),
('课程导出', @courseMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:training:course:export', '#', 'admin', NOW(), '');

-- 调整培训计划排序（原order=1改为2）
UPDATE sys_menu SET order_num = 2 WHERE menu_name = '培训计划' AND parent_id = @trainingMenuId;

-- 调整培训记录排序（原order=2改为3）并更新component路径
UPDATE sys_menu SET order_num = 3 WHERE menu_name = '培训记录' AND parent_id = @trainingMenuId;

-- ---- 参训人员/成绩 ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('参训人员', @trainingMenuId, 4, 'attendee', 'safety/training/attendee/index', 1, 0, 'C', '0', '0', 'safety:training:attendee:list', 'people', 'admin', NOW(), '参训人员及成绩菜单');
SET @attendeeMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('参训人员查询', @attendeeMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:training:attendee:query', '#', 'admin', NOW(), ''),
('参训人员新增', @attendeeMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:training:attendee:add', '#', 'admin', NOW(), ''),
('参训人员修改', @attendeeMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:training:attendee:edit', '#', 'admin', NOW(), ''),
('参训人员删除', @attendeeMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:training:attendee:remove', '#', 'admin', NOW(), ''),
('参训人员导出', @attendeeMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:training:attendee:export', '#', 'admin', NOW(), '');

-- ---- 证书管理 ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('证书管理', @trainingMenuId, 5, 'cert', 'safety/training/cert/index', 1, 0, 'C', '0', '0', 'safety:training:cert:list', 'documentation', 'admin', NOW(), '培训证书菜单');
SET @certMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('证书查询', @certMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:training:cert:query', '#', 'admin', NOW(), ''),
('证书新增', @certMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:training:cert:add', '#', 'admin', NOW(), ''),
('证书修改', @certMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:training:cert:edit', '#', 'admin', NOW(), ''),
('证书删除', @certMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:training:cert:remove', '#', 'admin', NOW(), ''),
('证书导出', @certMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:training:cert:export', '#', 'admin', NOW(), '');

-- ---- 学时统计 ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('学时统计', @trainingMenuId, 6, 'hours', 'safety/training/hours/index', 1, 0, 'C', '0', '0', 'safety:training:hours:list', 'chart', 'admin', NOW(), '学时统计菜单');

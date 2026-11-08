-- ============================================================
-- 安全生产 · 在线考试模块 全量SQL脚本
-- 包含：7张业务表DDL + 字典类型/数据 + 菜单权限
-- 依据：《安全生产模块考试功能开发需求方案》v1.1
-- 规范：表前缀 biz_safety_*（与安全模块一致）、COLLATE=utf8mb4_general_ci（§3.6强制）、
--       字段注释字典标注（§3.3.1强制）、数据权限归属字段（§3.5强制）
-- 幂等：可重复执行（DROP IF EXISTS + DELETE 再 INSERT）
-- ============================================================

-- =============================================
-- 零、清理旧数据（防止重复执行报错）
-- =============================================
DELETE FROM sys_role_menu WHERE menu_id IN (
    SELECT menu_id FROM sys_menu WHERE perms LIKE 'safety:exam:%'
);
DELETE FROM sys_menu WHERE perms LIKE 'safety:exam:%';

DELETE FROM sys_dict_data WHERE dict_type LIKE 'safety_exam_%';
DELETE FROM sys_dict_type WHERE dict_type LIKE 'safety_exam_%';

-- =============================================
-- 一、业务表 DDL（7张）
-- =============================================

-- 1. 题库分类表（主数据/共享资源，豁免数据权限归属字段）
DROP TABLE IF EXISTS biz_safety_exam_category;
CREATE TABLE biz_safety_exam_category (
    category_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    parent_id          BIGINT       DEFAULT 0                COMMENT '父分类ID（支持两级）',
    category_name      VARCHAR(50)  NOT NULL                 COMMENT '分类名称',
    order_num          INT          DEFAULT 0                COMMENT '显示顺序',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (category_id),
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试题库分类表';

-- 2. 题库表（主数据/共享资源，豁免数据权限归属字段）
DROP TABLE IF EXISTS biz_safety_exam_question;
CREATE TABLE biz_safety_exam_question (
    question_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '题目ID',
    question_type      CHAR(1)      NOT NULL                 COMMENT '题型（字典 safety_exam_question_type）',
    content            TEXT         NOT NULL                 COMMENT '题干',
    options            TEXT         NOT NULL                 COMMENT '选项（JSON数组，如["A内容","B内容"]；判断题固定["正确","错误"]）',
    answer             VARCHAR(20)  NOT NULL                 COMMENT '正确答案（如 A / ABD / 对）',
    analysis           VARCHAR(500)                          COMMENT '答案解析',
    category_id        BIGINT                                COMMENT '题目分类ID',
    category_name      VARCHAR(50)                           COMMENT '分类名称（冗余）',
    difficulty         CHAR(1)      DEFAULT '2'              COMMENT '难度（字典 safety_exam_difficulty）',
    job_types          VARCHAR(200)                          COMMENT '适用工种（逗号分隔，字典 safety_job_type）',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态（0启用 1停用）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (question_id),
    KEY idx_category (category_id),
    KEY idx_type_diff (question_type, difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='安全生产考试题库表';

-- 3. 试卷表（主数据，发布后题目集合冻结）
DROP TABLE IF EXISTS biz_safety_exam_paper;
CREATE TABLE biz_safety_exam_paper (
    paper_id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '试卷ID',
    paper_name         VARCHAR(100) NOT NULL                 COMMENT '试卷名称',
    course_id          BIGINT                                COMMENT '关联培训课程ID（biz_safety_training_course）',
    course_name        VARCHAR(255)                          COMMENT '课程名称（冗余）',
    total_score        DECIMAL(5,1) DEFAULT 100.0            COMMENT '总分（按题目分值自动计算）',
    pass_score         DECIMAL(5,1) DEFAULT 60.0             COMMENT '及格分',
    duration           INT          DEFAULT 60               COMMENT '考试时长（分钟）',
    shuffle_question   CHAR(1)      DEFAULT 'N'              COMMENT '题目乱序（Y是 N否）',
    shuffle_option     CHAR(1)      DEFAULT 'N'              COMMENT '选项乱序（Y是 N否）',
    switch_limit       INT          DEFAULT 0                COMMENT '切屏限制次数（0不限）',
    show_analysis      CHAR(1)      DEFAULT 'Y'              COMMENT '交卷后展示解析（Y是 N否）',
    status             CHAR(1)      DEFAULT '0'              COMMENT '试卷状态（字典 safety_exam_paper_status）',
    publish_by         VARCHAR(64)                           COMMENT '发布人',
    publish_time       DATETIME                              COMMENT '发布时间',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (paper_id),
    KEY idx_course (course_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试试卷表';

-- 4. 试卷题目明细表（快照式：发布时冻结题干/选项/答案，判分依据）
DROP TABLE IF EXISTS biz_safety_exam_paper_question;
CREATE TABLE biz_safety_exam_paper_question (
    id                 BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    paper_id           BIGINT       NOT NULL                 COMMENT '试卷ID',
    question_id        BIGINT       NOT NULL                 COMMENT '来源题目ID（biz_safety_exam_question）',
    question_type      CHAR(1)      NOT NULL                 COMMENT '题型（字典 safety_exam_question_type）',
    content            TEXT         NOT NULL                 COMMENT '题干快照',
    options            TEXT         NOT NULL                 COMMENT '选项快照（JSON数组）',
    answer             VARCHAR(20)  NOT NULL                 COMMENT '答案快照（判分依据）',
    analysis           VARCHAR(500)                          COMMENT '解析快照',
    score              DECIMAL(4,1) DEFAULT 1.0              COMMENT '本题分值',
    sort_no            INT          DEFAULT 0                COMMENT '题目顺序号',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_paper (paper_id),
    KEY idx_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='试卷题目明细表（快照）';

-- 5. 考试场次表（事务单据：必须带 dept_id，接入 @DataScope）
DROP TABLE IF EXISTS biz_safety_exam;
CREATE TABLE biz_safety_exam (
    exam_id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '考试ID',
    exam_name          VARCHAR(100) NOT NULL                 COMMENT '考试名称',
    paper_id           BIGINT       NOT NULL                 COMMENT '试卷ID',
    paper_name         VARCHAR(100)                          COMMENT '试卷名称（冗余）',
    record_id          BIGINT                                COMMENT '关联培训记录ID（biz_safety_training_record，可空）',
    start_time         DATETIME     NOT NULL                 COMMENT '考试开始时间',
    end_time           DATETIME     NOT NULL                 COMMENT '考试结束时间（窗口截止）',
    exam_status        CHAR(1)      DEFAULT '0'              COMMENT '考试状态（字典 safety_exam_exam_status）',
    publish_by         VARCHAR(64)                           COMMENT '发布人',
    publish_time       DATETIME                              COMMENT '发布时间',
    dept_id            BIGINT                                COMMENT '归属部门ID（数据权限，创建时自动填充）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (exam_id),
    KEY idx_paper (paper_id),
    KEY idx_record (record_id),
    KEY idx_status (exam_status),
    KEY idx_start_end (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试场次表';

-- 6. 考生考试记录表（人员归属型：dept_id + user_id，接入 @DataScope）
DROP TABLE IF EXISTS biz_safety_exam_record;
CREATE TABLE biz_safety_exam_record (
    record_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '考试记录ID',
    exam_id            BIGINT       NOT NULL                 COMMENT '考试ID',
    paper_id           BIGINT       NOT NULL                 COMMENT '试卷ID',
    user_id            BIGINT       NOT NULL                 COMMENT '考生用户ID（关联sys_user）',
    user_name          VARCHAR(128)                          COMMENT '考生姓名（冗余）',
    worker_id          BIGINT                                COMMENT '关联员工ID（biz_safety_worker，可空）',
    attendee_id        BIGINT                                COMMENT '关联培训参训人ID（biz_safety_training_attendee，可空）',
    start_time         DATETIME                              COMMENT '本人开始答题时间',
    submit_time        DATETIME                              COMMENT '提交时间',
    used_seconds       INT                                   COMMENT '实际用时（秒）',
    submit_type        CHAR(1)                               COMMENT '提交方式（字典 safety_exam_submit_type）',
    total_score        DECIMAL(5,1)                          COMMENT '总得分',
    is_pass            CHAR(1)                               COMMENT '是否合格（0不合格 1合格）',
    judge_status       CHAR(1)      DEFAULT '0'              COMMENT '判分状态（0未交卷 1已自动判分）',
    switch_count       INT          DEFAULT 0                COMMENT '切屏次数',
    exam_result        CHAR(1)      DEFAULT '0'              COMMENT '考生状态（字典 safety_exam_result）',
    dept_id            BIGINT                                COMMENT '归属部门ID（数据权限）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (record_id),
    UNIQUE KEY uk_exam_user (exam_id, user_id),
    KEY idx_user (user_id),
    KEY idx_exam (exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考生考试记录表';

-- 7. 答题明细表（追溯数据，随考试记录权限）
DROP TABLE IF EXISTS biz_safety_exam_answer;
CREATE TABLE biz_safety_exam_answer (
    answer_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '答题明细ID',
    record_id          BIGINT       NOT NULL                 COMMENT '考试记录ID',
    paper_question_id  BIGINT       NOT NULL                 COMMENT '试卷题目明细ID',
    user_answer        VARCHAR(20)                           COMMENT '考生答案（如 A / ABD；空=未答）',
    is_correct         CHAR(1)                               COMMENT '是否正确（1正确 0错误）',
    score              DECIMAL(4,1) DEFAULT 0                COMMENT '本题得分',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (answer_id),
    UNIQUE KEY uk_record_question (record_id, paper_question_id),
    KEY idx_record (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试答题明细表';

-- =============================================
-- 二、字典类型
-- =============================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('考试题型', 'safety_exam_question_type', '0', 'admin', NOW(), '考试题目题型'),
('考试题目难度', 'safety_exam_difficulty', '0', 'admin', NOW(), '考试题目难度等级'),
('试卷状态', 'safety_exam_paper_status', '0', 'admin', NOW(), '考试试卷状态'),
('考试状态', 'safety_exam_exam_status', '0', 'admin', NOW(), '考试场次状态'),
('考生考试状态', 'safety_exam_result', '0', 'admin', NOW(), '考生在考试中的状态'),
('考试提交方式', 'safety_exam_submit_type', '0', 'admin', NOW(), '考生交卷方式');

-- =============================================
-- 三、字典数据
-- =============================================
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
-- 考试题型
(NULL, 1, '单选题', '1', 'safety_exam_question_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '多选题', '2', 'safety_exam_question_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '判断题', '3', 'safety_exam_question_type', '', 'success', 'N', '0', 'admin', NOW(), ''),

-- 考试题目难度
(NULL, 1, '简单', '1', 'safety_exam_difficulty', '', 'info', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '中等', '2', 'safety_exam_difficulty', '', 'primary', 'Y', '0', 'admin', NOW(), ''),
(NULL, 3, '困难', '3', 'safety_exam_difficulty', '', 'danger', 'N', '0', 'admin', NOW(), ''),

-- 试卷状态
(NULL, 1, '草稿', '0', 'safety_exam_paper_status', '', 'info', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '已发布', '1', 'safety_exam_paper_status', '', 'success', 'N', '0', 'admin', NOW(), '发布后题目集合冻结'),
(NULL, 3, '停用', '2', 'safety_exam_paper_status', '', 'danger', 'N', '0', 'admin', NOW(), ''),

-- 考试状态
(NULL, 1, '草稿', '0', 'safety_exam_exam_status', '', 'info', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '已发布', '1', 'safety_exam_exam_status', '', 'primary', 'N', '0', 'admin', NOW(), '未到开始时间'),
(NULL, 3, '考试中', '2', 'safety_exam_exam_status', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '已结束', '3', 'safety_exam_exam_status', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '已撤回', '4', 'safety_exam_exam_status', '', 'danger', 'N', '0', 'admin', NOW(), '仅未开始的场次可撤回'),

-- 考生考试状态
(NULL, 1, '未开始', '0', 'safety_exam_result', '', 'info', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '进行中', '1', 'safety_exam_result', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '已交卷', '2', 'safety_exam_result', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '缺考', '3', 'safety_exam_result', '', 'danger', 'N', '0', 'admin', NOW(), '窗口结束无答题记录'),
(NULL, 5, '已判分', '4', 'safety_exam_result', '', 'success', 'N', '0', 'admin', NOW(), '成绩已回写'),

-- 考试提交方式
(NULL, 1, '手动提交', '1', 'safety_exam_submit_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '超时自动提交', '2', 'safety_exam_submit_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '切屏强制交卷', '3', 'safety_exam_submit_type', '', 'danger', 'N', '0', 'admin', NOW(), '切屏超限自动交卷');

-- =============================================
-- 四、菜单权限（教育培训下新建「考试管理」二级目录，置于学时统计之前；权限域独立为 safety:exam:*）
-- =============================================

-- 获取教育培训目录ID
SELECT @trainingMenuId := menu_id FROM sys_menu WHERE menu_name = '教育培训' AND parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0);

-- ---- 「考试管理」二级目录（教育培训下排序6，位于学时统计之前） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '考试管理', @trainingMenuId, 6, 'exam', 'M', '0', '0', '', 'form', 'admin', NOW(), '考试功能子目录'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '考试管理' AND parent_id = @trainingMenuId AND menu_type = 'M');
SELECT @examDirMenuId := menu_id FROM sys_menu WHERE menu_name = '考试管理' AND parent_id = @trainingMenuId AND menu_type = 'M';

-- 学时统计顺移至考试管理之后（存在才更新）
UPDATE sys_menu SET order_num = 7 WHERE menu_name = '学时统计' AND parent_id = @trainingMenuId;

-- ---- 题库管理（考试管理下排序1） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('题库管理', @examDirMenuId, 1, 'examQuestion', 'safety/exam/question/index', 1, 0, 'C', '0', '0', 'safety:exam:question:list', 'education', 'admin', NOW(), '考试题库管理菜单');
SET @questionMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('题库查询', @questionMenuId, 1, '#', 'F', '0', '0', 'safety:exam:question:query',  'admin', NOW(), ''),
('题库新增', @questionMenuId, 2, '#', 'F', '0', '0', 'safety:exam:question:add',    'admin', NOW(), ''),
('题库修改', @questionMenuId, 3, '#', 'F', '0', '0', 'safety:exam:question:edit',   'admin', NOW(), ''),
('题库删除', @questionMenuId, 4, '#', 'F', '0', '0', 'safety:exam:question:remove', 'admin', NOW(), ''),
('题库导入', @questionMenuId, 5, '#', 'F', '0', '0', 'safety:exam:question:import', 'admin', NOW(), ''),
('题库导出', @questionMenuId, 6, '#', 'F', '0', '0', 'safety:exam:question:export', 'admin', NOW(), '');

-- ---- 试卷管理（考试管理下排序2） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('试卷管理', @examDirMenuId, 2, 'examPaper', 'safety/exam/paper/index', 1, 0, 'C', '0', '0', 'safety:exam:paper:list', 'form', 'admin', NOW(), '考试试卷管理菜单');
SET @paperMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('试卷查询', @paperMenuId, 1, '#', 'F', '0', '0', 'safety:exam:paper:query',   'admin', NOW(), ''),
('试卷新增', @paperMenuId, 2, '#', 'F', '0', '0', 'safety:exam:paper:add',     'admin', NOW(), ''),
('试卷修改', @paperMenuId, 3, '#', 'F', '0', '0', 'safety:exam:paper:edit',    'admin', NOW(), ''),
('试卷删除', @paperMenuId, 4, '#', 'F', '0', '0', 'safety:exam:paper:remove',  'admin', NOW(), ''),
('试卷发布', @paperMenuId, 5, '#', 'F', '0', '0', 'safety:exam:paper:publish', 'admin', NOW(), '发布后题目集合冻结');

-- ---- 考试场次（考试管理下排序3） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('考试场次', @examDirMenuId, 3, 'examManage', 'safety/exam/manage/index', 1, 0, 'C', '0', '0', 'safety:exam:list', 'date-range', 'admin', NOW(), '考试场次管理菜单');
SET @examMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('考试查询',     @examMenuId, 1, '#', 'F', '0', '0', 'safety:exam:query',    'admin', NOW(), ''),
('考试新增',     @examMenuId, 2, '#', 'F', '0', '0', 'safety:exam:add',      'admin', NOW(), ''),
('考试修改',     @examMenuId, 3, '#', 'F', '0', '0', 'safety:exam:edit',     'admin', NOW(), ''),
('考试删除',     @examMenuId, 4, '#', 'F', '0', '0', 'safety:exam:remove',   'admin', NOW(), ''),
('考试发布',     @examMenuId, 5, '#', 'F', '0', '0', 'safety:exam:publish',  'admin', NOW(), ''),
('考试强制结束', @examMenuId, 6, '#', 'F', '0', '0', 'safety:exam:forceEnd', 'admin', NOW(), ''),
('成绩导出',     @examMenuId, 7, '#', 'F', '0', '0', 'safety:exam:export',   'admin', NOW(), ''),
('成绩统计',     @examMenuId, 8, '#', 'F', '0', '0', 'safety:exam:stats',    'admin', NOW(), ''),
('成绩调分',     @examMenuId, 9, '#', 'F', '0', '0', 'safety:exam:score:edit', 'admin', NOW(), '人工调分留痕');

-- ---- 我的考试（考生端，考试管理下排序4，需授权给普通员工角色） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('我的考试', @examDirMenuId, 4, 'examTake', 'safety/exam/take/index', 1, 0, 'C', '0', '0', 'safety:exam:myList', 'checkbox', 'admin', NOW(), '考生端我的考试菜单');
SET @takeMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('在线答题', @takeMenuId, 1, '#', 'F', '0', '0', 'safety:exam:take', 'admin', NOW(), '进入考试/答题/提交/查分');

-- =============================================
-- 五、角色授权
-- =============================================
-- 管理员角色（role_id=2，与 safety_all.sql 口径一致）分配全部考试权限（含考试管理目录）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE perms LIKE 'safety:exam:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 2);

-- 普通员工角色如需在线考试，请在【系统管理→角色管理】为对应角色勾选
-- 「教育培训→我的考试」菜单及其「在线答题」按钮权限

-- =============================================
-- 六、定时任务（考试状态兜底：状态流转 + 缺考标记，幂等可重复）
-- =============================================
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '考试状态兜底任务', 'DEFAULT', 'safetyExamTask.run', '0 * * * * ?', '3', '1', '0', 'admin', NOW(), '场次状态流转与缺考标记（幂等），建议每分钟'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'safetyExamTask.run');

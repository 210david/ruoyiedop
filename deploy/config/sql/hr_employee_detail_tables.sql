-- =====================================================
-- 员工档案明细子表：家庭成员、教育背景、工作经历、培训/职称/资格认证
-- =====================================================

-- 1. 家庭成员表
DROP TABLE IF EXISTS biz_hr_employee_family;
CREATE TABLE biz_hr_employee_family (
    family_id       BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    employee_id     BIGINT          NOT NULL                COMMENT '员工ID',
    member_name     VARCHAR(64)     DEFAULT ''              COMMENT '姓名',
    relationship    VARCHAR(32)     DEFAULT ''              COMMENT '关系',
    gender          VARCHAR(8)      DEFAULT ''              COMMENT '性别',
    birthday        DATE            DEFAULT NULL            COMMENT '出生日期',
    phone           VARCHAR(20)     DEFAULT ''              COMMENT '联系电话',
    occupation      VARCHAR(64)     DEFAULT ''              COMMENT '工作单位/职业',
    status          CHAR(1)         DEFAULT '0'             COMMENT '状态(0正常1停用)',
    create_by       VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME        DEFAULT NULL            COMMENT '创建时间',
    update_by       VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
    update_time     DATETIME        DEFAULT NULL            COMMENT '更新时间',
    remark          VARCHAR(500)    DEFAULT ''              COMMENT '备注',
    del_flag        CHAR(1)         DEFAULT '0'             COMMENT '删除标志',
    PRIMARY KEY (family_id),
    KEY idx_employee_id (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工家庭成员表';

-- 2. 教育背景表
DROP TABLE IF EXISTS biz_hr_employee_education;
CREATE TABLE biz_hr_employee_education (
    education_id    BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    employee_id     BIGINT          NOT NULL                COMMENT '员工ID',
    start_date      DATE            DEFAULT NULL            COMMENT '开始日期',
    end_date        DATE            DEFAULT NULL            COMMENT '结束日期',
    school_name     VARCHAR(128)    DEFAULT ''              COMMENT '学校名称',
    major           VARCHAR(64)     DEFAULT ''              COMMENT '专业',
    edu_level       VARCHAR(32)     DEFAULT ''              COMMENT '学历',
    degree          VARCHAR(32)     DEFAULT ''              COMMENT '学位',
    is_highest      CHAR(1)         DEFAULT '0'            COMMENT '是否最高学历',
    status          CHAR(1)         DEFAULT '0'             COMMENT '状态(0正常1停用)',
    create_by       VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME        DEFAULT NULL            COMMENT '创建时间',
    update_by       VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
    update_time     DATETIME        DEFAULT NULL            COMMENT '更新时间',
    remark          VARCHAR(500)    DEFAULT ''              COMMENT '备注',
    del_flag        CHAR(1)         DEFAULT '0'             COMMENT '删除标志',
    PRIMARY KEY (education_id),
    KEY idx_employee_id (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工教育背景表';

-- 3. 工作经历表
DROP TABLE IF EXISTS biz_hr_employee_work_exp;
CREATE TABLE biz_hr_employee_work_exp (
    work_exp_id     BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    employee_id     BIGINT          NOT NULL                COMMENT '员工ID',
    start_date      DATE            DEFAULT NULL            COMMENT '开始日期',
    end_date        DATE            DEFAULT NULL            COMMENT '结束日期',
    company_name    VARCHAR(128)    DEFAULT ''              COMMENT '工作单位',
    position        VARCHAR(64)     DEFAULT ''              COMMENT '职位',
    work_content    VARCHAR(500)    DEFAULT ''              COMMENT '工作内容',
    leave_reason    VARCHAR(200)    DEFAULT ''              COMMENT '离职原因',
    status          CHAR(1)         DEFAULT '0'             COMMENT '状态(0正常1停用)',
    create_by       VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME        DEFAULT NULL            COMMENT '创建时间',
    update_by       VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
    update_time     DATETIME        DEFAULT NULL            COMMENT '更新时间',
    remark          VARCHAR(500)    DEFAULT ''              COMMENT '备注',
    del_flag        CHAR(1)         DEFAULT '0'             COMMENT '删除标志',
    PRIMARY KEY (work_exp_id),
    KEY idx_employee_id (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工工作经历表';

-- 4. 培训/专业技术职称/权威机构资格认证表
DROP TABLE IF EXISTS biz_hr_employee_qualification;
CREATE TABLE biz_hr_employee_qualification (
    qualification_id BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    employee_id      BIGINT         NOT NULL                COMMENT '员工ID',
    qual_type        VARCHAR(32)    DEFAULT ''              COMMENT '类型(培训/职称/资格认证)',
    qual_name        VARCHAR(128)   DEFAULT ''              COMMENT '名称',
    qual_no          VARCHAR(64)    DEFAULT ''              COMMENT '证书编号',
    issue_org        VARCHAR(128)   DEFAULT ''              COMMENT '发证机构',
    issue_date       DATE           DEFAULT NULL            COMMENT '发证日期',
    valid_from       DATE           DEFAULT NULL            COMMENT '有效起始日',
    valid_to         DATE           DEFAULT NULL            COMMENT '有效期至',
    score            VARCHAR(32)    DEFAULT ''              COMMENT '成绩/等级',
    status           CHAR(1)        DEFAULT '0'             COMMENT '状态(0正常1停用)',
    create_by        VARCHAR(64)    DEFAULT ''              COMMENT '创建者',
    create_time      DATETIME       DEFAULT NULL            COMMENT '创建时间',
    update_by        VARCHAR(64)    DEFAULT ''              COMMENT '更新者',
    update_time      DATETIME       DEFAULT NULL            COMMENT '更新时间',
    remark           VARCHAR(500)   DEFAULT ''              COMMENT '备注',
    del_flag         CHAR(1)        DEFAULT '0'             COMMENT '删除标志',
    PRIMARY KEY (qualification_id),
    KEY idx_employee_id (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工培训/职称/资格认证表';

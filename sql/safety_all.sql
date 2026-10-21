-- =============================================
-- Safety 安全生产管理系统 - 全量SQL脚本
-- 包含：业务表DDL + 字典类型 + 菜单权限
-- 适用：中小企业安全生产管理全模块
-- =============================================

-- =============================================
-- 零、清理旧数据（防止重复执行报错）
-- =============================================

-- 删除旧的菜单和权限（包括目录菜单）
DELETE FROM sys_role_menu WHERE menu_id IN (
    SELECT menu_id FROM sys_menu WHERE perms LIKE 'safety:%' 
    OR (menu_name = '安全生产管理' AND parent_id = 0)
    OR (menu_name IN ('一企一档','双控机制','教育培训','应急管理','到期提醒','安全驾驶舱') AND parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0) AS tmp))
);
DELETE FROM sys_menu WHERE perms LIKE 'safety:%' 
    OR (menu_name = '安全生产管理' AND parent_id = 0)
    OR (menu_name IN ('一企一档','双控机制','教育培训','应急管理','到期提醒','安全驾驶舱') AND parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0) AS tmp));

-- 删除旧的字典数据
DELETE FROM sys_dict_data WHERE dict_type LIKE 'safety_%';
DELETE FROM sys_dict_type WHERE dict_type LIKE 'safety_%';

-- =============================================
-- 一、业务表 DDL
-- =============================================

-- 1. 企业安全档案表
DROP TABLE IF EXISTS biz_safety_enterprise;
CREATE TABLE biz_safety_enterprise (
    enterprise_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '企业ID',
    enterprise_name    VARCHAR(128) NOT NULL                 COMMENT '企业名称',
    credit_code        VARCHAR(18)  NOT NULL                 COMMENT '统一社会信用代码',
    industry_type      VARCHAR(32)  NOT NULL                 COMMENT '行业类别',
    is_high_risk       CHAR(1)      DEFAULT '0'              COMMENT '是否高危行业',
    legal_person       VARCHAR(64)                           COMMENT '法定代表人',
    safety_manager_id  BIGINT                                COMMENT '安全负责人ID',
    safety_manager     VARCHAR(64)                           COMMENT '安全负责人名称',
    safety_org         VARCHAR(255)                          COMMENT '安全管理机构',
    responsibility     TEXT                                  COMMENT '安全生产责任制',
    supervise_dept     VARCHAR(255)                          COMMENT '属地监管单位',
    plan_image         VARCHAR(500)                          COMMENT '厂区平面图',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (enterprise_id),
    UNIQUE KEY uk_credit_code (credit_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业安全档案表';

-- 2. 厂区/车间/区域档案表
DROP TABLE IF EXISTS biz_safety_area;
CREATE TABLE biz_safety_area (
    area_id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '区域ID',
    parent_id          BIGINT       DEFAULT 0                COMMENT '父级ID',
    ancestors          VARCHAR(500)                          COMMENT '祖级列表',
    area_code          VARCHAR(64)  NOT NULL                 COMMENT '区域编码',
    area_name          VARCHAR(128) NOT NULL                 COMMENT '区域名称',
    node_type          CHAR(1)      DEFAULT '1'              COMMENT '节点类型',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    area_type          VARCHAR(64)                           COMMENT '区域类型',
    safety_person_id   BIGINT                                COMMENT '安全责任人ID',
    safety_person_name VARCHAR(64)                           COMMENT '安全责任人名称',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (area_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='厂区/车间/区域档案表';

-- 3. 证照管理表
DROP TABLE IF EXISTS biz_safety_cert;
CREATE TABLE biz_safety_cert (
    cert_id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '证照ID',
    cert_name          VARCHAR(128) NOT NULL                 COMMENT '证照名称',
    cert_no            VARCHAR(64)  NOT NULL                 COMMENT '证照编号',
    cert_type          VARCHAR(64)                           COMMENT '证照类型',
    issue_org          VARCHAR(255)                          COMMENT '发证机关',
    issue_date         DATE                                  COMMENT '发证日期',
    expire_date        DATE         NOT NULL                 COMMENT '有效期至',
    remind_status      CHAR(1)      DEFAULT '0'              COMMENT '提醒状态',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    attachment         VARCHAR(500)                          COMMENT '附件扫描件',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (cert_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证照管理表';

-- 4. 特种作业人员台账表
DROP TABLE IF EXISTS biz_safety_worker;
CREATE TABLE biz_safety_worker (
    worker_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '人员ID',
    worker_name        VARCHAR(64)  NOT NULL                 COMMENT '姓名',
    worker_no          VARCHAR(64)                           COMMENT '工号',
    dept_id            BIGINT                                COMMENT '所属部门ID',
    dept_name          VARCHAR(128)                          COMMENT '所属部门名称',
    position           VARCHAR(128)                          COMMENT '岗位',
    phone              VARCHAR(20)                           COMMENT '联系电话',
    is_special         CHAR(1)      DEFAULT '0'              COMMENT '是否特种作业人员',
    job_type           VARCHAR(64)                           COMMENT '特种作业类别',
    cert_no            VARCHAR(64)                           COMMENT '特种作业证号',
    cert_issue_date    DATE                                  COMMENT '证书发证日期',
    valid_to           DATE                                  COMMENT '证书有效期至',
    review_date        DATE                                  COMMENT '换证/复审日期',
    remind_status      CHAR(1)      DEFAULT '0'              COMMENT '提醒状态',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (worker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='特种人员台账表';

-- 5. 设备设施台账表
DROP TABLE IF EXISTS biz_safety_equipment;
CREATE TABLE biz_safety_equipment (
    equipment_id       BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '设备ID',
    equipment_code     VARCHAR(64)  NOT NULL                 COMMENT '设备编号',
    equipment_name     VARCHAR(128) NOT NULL                 COMMENT '设备名称',
    spec_model         VARCHAR(255)                          COMMENT '型号规格',
    area_id            BIGINT                                COMMENT '所属区域ID',
    area_name          VARCHAR(128)                          COMMENT '所属区域名称',
    equipment_category VARCHAR(32)  NOT NULL                 COMMENT '设备类别',
    special_sub_type   VARCHAR(64)                           COMMENT '特种设备子类',
    reg_cert_no        VARCHAR(64)                           COMMENT '使用登记证号',
    inspect_expire     DATE                                  COMMENT '检验有效期至',
    remind_status      CHAR(1)      DEFAULT '0'              COMMENT '提醒状态',
    person_id          BIGINT                                COMMENT '责任人ID',
    person_name        VARCHAR(64)                           COMMENT '责任人名称',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (equipment_id),
    UNIQUE KEY uk_equipment_code (equipment_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备设施台账表';

-- 6. 物料/危化品台账表
DROP TABLE IF EXISTS biz_safety_material;
CREATE TABLE biz_safety_material (
    material_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '物料ID',
    material_code      VARCHAR(64)  NOT NULL                 COMMENT '危化品编号',
    material_name      VARCHAR(128) NOT NULL                 COMMENT '物料名称',
    hazard_class       VARCHAR(64)                           COMMENT '危化品分类',
    cas_no             VARCHAR(64)                           COMMENT 'CAS号/别名',
    max_storage        DECIMAL(18,2)                         COMMENT '最大储存量',
    storage_unit       VARCHAR(20)                           COMMENT '储存单位',
    storage_area_id    BIGINT                                COMMENT '储存位置区域ID',
    storage_area_name  VARCHAR(128)                          COMMENT '储存位置名称',
    sds_attachment     VARCHAR(500)                          COMMENT 'SDS安全技术说明书附件',
    toxic_flag         VARCHAR(20)  DEFAULT '0'              COMMENT '剧毒/易制爆标识',
    current_stock      DECIMAL(18,2)                         COMMENT '当前库存量',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (material_id),
    UNIQUE KEY uk_material_code (material_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料/危化品台账表';

-- 7. 风险点管理表
DROP TABLE IF EXISTS biz_safety_risk_point;
CREATE TABLE biz_safety_risk_point (
    risk_point_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '风险点ID',
    risk_code          VARCHAR(64)  NOT NULL                 COMMENT '风险点编号',
    risk_name          VARCHAR(128) NOT NULL                 COMMENT '风险点名称',
    area_id            BIGINT                                COMMENT '所属区域ID',
    area_name          VARCHAR(128)                          COMMENT '所属区域名称',
    hazard_desc        TEXT                                  COMMENT '危险源描述',
    accident_type      VARCHAR(255)                          COMMENT '事故类别',
    l_value            DECIMAL(10,2)                         COMMENT 'L值',
    e_value            DECIMAL(10,2)                         COMMENT 'E值',
    c_value            DECIMAL(10,2)                         COMMENT 'C值',
    d_value            DECIMAL(12,2)                         COMMENT 'D值',
    risk_level         VARCHAR(10)                           COMMENT '风险等级',
    control_level      VARCHAR(10)                           COMMENT '管控层级',
    measure_engine     TEXT                                  COMMENT '工程技术措施',
    measure_manage     TEXT                                  COMMENT '管理措施',
    measure_training   TEXT                                  COMMENT '培训教育措施',
    measure_ppe        TEXT                                  COMMENT '个体防护措施',
    measure_emergency  TEXT                                  COMMENT '应急处置措施',
    check_standard     TEXT                                  COMMENT '检查标准',
    check_frequency    VARCHAR(32)                           COMMENT '排查频次',
    x_coord            DECIMAL(10,2)                         COMMENT 'X坐标',
    y_coord            DECIMAL(10,2)                         COMMENT 'Y坐标',
    person_id          BIGINT                                COMMENT '责任人ID',
    person_name        VARCHAR(64)                           COMMENT '责任人名称',
    evaluate_date      DATE                                  COMMENT '评价日期',
    re_evaluate_date   DATE                                  COMMENT '复评日期',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (risk_point_id),
    UNIQUE KEY uk_risk_code (risk_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险点管理表';

-- 8. 隐患管理表
DROP TABLE IF EXISTS biz_safety_hazard;
CREATE TABLE biz_safety_hazard (
    hazard_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '隐患ID',
    hazard_code        VARCHAR(64)  NOT NULL                 COMMENT '隐患编号',
    risk_point_id      BIGINT                                COMMENT '关联风险点ID',
    hazard_desc        TEXT         NOT NULL                 COMMENT '隐患描述',
    hazard_type        VARCHAR(32)                           COMMENT '隐患类型',
    hazard_level       VARCHAR(10)                           COMMENT '隐患等级',
    photos             TEXT                                  COMMENT '隐患图片',
    discover_person_id BIGINT                                COMMENT '发现人ID',
    discover_person    VARCHAR(64)                           COMMENT '发现人',
    discover_time      DATETIME                              COMMENT '发现时间',
    source_type        VARCHAR(32)                           COMMENT '隐患来源',
    dept_id            BIGINT                                COMMENT '责任部门ID',
    dept_name          VARCHAR(128)                          COMMENT '责任部门',
    person_id          BIGINT                                COMMENT '责任人ID',
    person_name        VARCHAR(64)                           COMMENT '责任人',
    rectify_req        TEXT                                  COMMENT '整改要求',
    rectify_deadline   DATE                                  COMMENT '整改期限',
    hazard_status      VARCHAR(20)  DEFAULT '0'              COMMENT '隐患状态',
    rectify_desc       TEXT                                  COMMENT '整改说明',
    rectify_photos     TEXT                                  COMMENT '整改后图片',
    rectify_time       DATETIME                              COMMENT '整改时间',
    verify_person_id   BIGINT                                COMMENT '验收人ID',
    verify_person      VARCHAR(64)                           COMMENT '验收人',
    verify_time        DATETIME                              COMMENT '验收时间',
verify_result      VARCHAR(10)                           COMMENT '验收结果',
verify_comment     TEXT                                  COMMENT '验收意见',
approve_person_id  BIGINT                                COMMENT '审批人ID',
approve_person     VARCHAR(64)                           COMMENT '审批人',
approve_time       DATETIME                              COMMENT '审批时间',
approve_result     VARCHAR(10)                           COMMENT '审批结果',
approve_comment    TEXT                                  COMMENT '审批意见',
enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (hazard_id),
    UNIQUE KEY uk_hazard_code (hazard_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='隐患管理表';

-- 9. 隐患整改记录表
DROP TABLE IF EXISTS biz_safety_rectify;
CREATE TABLE biz_safety_rectify (
    rectify_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '整改记录ID',
    hazard_id          BIGINT       NOT NULL                 COMMENT '隐患ID',
    rectify_type       VARCHAR(20)                           COMMENT '整改类型',
    rectify_plan       TEXT                                  COMMENT '整改方案',
    solution_desc      TEXT                                  COMMENT '整改措施说明',
    before_photos      TEXT                                  COMMENT '整改前照片',
    after_photos       TEXT                                  COMMENT '整改后照片',
    rectify_person_id  BIGINT                                COMMENT '整改人ID',
    rectify_person     VARCHAR(64)                           COMMENT '整改人',
    rectify_time       DATETIME                              COMMENT '整改时间',
    verify_person_id   BIGINT                                COMMENT '验收人ID',
    verify_person      VARCHAR(64)                           COMMENT '验收人',
    verify_time        DATETIME                              COMMENT '验收时间',
    verify_result      VARCHAR(10)                           COMMENT '验收结果',
    verify_comment     TEXT                                  COMMENT '验收意见',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (rectify_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='隐患整改记录表';

-- 10. 排查任务表
DROP TABLE IF EXISTS biz_safety_inspection_task;
CREATE TABLE biz_safety_inspection_task (
    task_id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '任务ID',
    task_code          VARCHAR(64)  NOT NULL                 COMMENT '任务编号',
    task_type          VARCHAR(32)                           COMMENT '任务类型',
    task_name          VARCHAR(255)                          COMMENT '任务名称',
    risk_point_id      BIGINT                                COMMENT '关联风险点ID',
    plan_date          DATE                                  COMMENT '计划日期',
    executor_id        BIGINT                                COMMENT '执行人ID',
    executor_name      VARCHAR(64)                           COMMENT '执行人',
    task_status        VARCHAR(20)  DEFAULT '0'              COMMENT '任务状态',
    inspect_result     VARCHAR(10)                           COMMENT '检查结果',
    hazard_count       INT          DEFAULT 0                COMMENT '发现隐患数',
    inspect_time       DATETIME                              COMMENT '检查时间',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (task_id),
    UNIQUE KEY uk_task_code (task_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排查任务表';

-- 11. 培训计划表
DROP TABLE IF EXISTS biz_safety_training_plan;
CREATE TABLE biz_safety_training_plan (
    plan_id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '计划ID',
    plan_code          VARCHAR(64)  NOT NULL                 COMMENT '计划编号',
    plan_name          VARCHAR(255) NOT NULL                 COMMENT '计划名称',
    plan_type          VARCHAR(32)                           COMMENT '计划类型',
    plan_year          VARCHAR(10)                           COMMENT '年度',
    target_persons     TEXT                                  COMMENT '培训对象（JSON格式，存储用户ID、姓名、部门）',
    total_hours        DECIMAL(10,2)                         COMMENT '总课时',
    plan_status        VARCHAR(20)                           COMMENT '计划状态',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (plan_id),
    UNIQUE KEY uk_plan_code (plan_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训计划表';

-- 12. 培训记录表
DROP TABLE IF EXISTS biz_safety_training_record;
CREATE TABLE biz_safety_training_record (
    record_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    plan_id            BIGINT                                COMMENT '关联计划ID',
    course_name        VARCHAR(255)                          COMMENT '课程名称',
    course_type        VARCHAR(64)                           COMMENT '课程类别',
    training_date      DATE                                  COMMENT '培训日期',
    hours              DECIMAL(10,2)                         COMMENT '课时',
    trainer            VARCHAR(128)                          COMMENT '培训讲师',
    persons            TEXT                                  COMMENT '参训人员',
    exam_score         DECIMAL(5,2)                          COMMENT '考试分数',
    is_pass            CHAR(1)      DEFAULT '0'              COMMENT '是否合格',
    attachment         VARCHAR(500)                          COMMENT '附件',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训记录表';

-- 13. 应急预案表
DROP TABLE IF EXISTS biz_safety_emergency_plan;
CREATE TABLE biz_safety_emergency_plan (
    plan_id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '预案ID',
    plan_code          VARCHAR(64)  NOT NULL                 COMMENT '预案编号',
    plan_name          VARCHAR(255) NOT NULL                 COMMENT '预案名称',
    plan_type          VARCHAR(32)                           COMMENT '预案类型',
    plan_level         VARCHAR(32)                           COMMENT '预案级别',
    version            VARCHAR(20)                           COMMENT '版本号',
    reg_no             VARCHAR(64)                           COMMENT '备案号',
    reg_date           DATE                                  COMMENT '备案日期',
    effective_date     DATE                                  COMMENT '生效日期',
    expire_date        DATE                                  COMMENT '修订到期日期',
    remind_status      CHAR(1)      DEFAULT '0'              COMMENT '提醒状态',
    content            TEXT                                  COMMENT '预案内容',
    attachment         VARCHAR(500)                          COMMENT '附件',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (plan_id),
    UNIQUE KEY uk_plan_code (plan_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应急预案表';

-- 14. 应急物资表
DROP TABLE IF EXISTS biz_safety_emergency_material;
CREATE TABLE biz_safety_emergency_material (
    material_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '物资ID',
    material_code      VARCHAR(64)  NOT NULL                 COMMENT '物资编码',
    material_name      VARCHAR(128) NOT NULL                 COMMENT '物资名称',
    material_type      VARCHAR(64)                           COMMENT '物资类别',
    spec_model         VARCHAR(128)                          COMMENT '规格型号',
    quantity           DECIMAL(12,2)                         COMMENT '数量',
    unit               VARCHAR(20)                           COMMENT '单位',
    storage_location   VARCHAR(255)                          COMMENT '存放位置',
    person_id          BIGINT                                COMMENT '管理人ID',
    person_name        VARCHAR(64)                           COMMENT '管理人名称',
    effective_date     DATE                                  COMMENT '有效期至',
    remind_status      CHAR(1)      DEFAULT '0'              COMMENT '提醒状态',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (material_id),
    UNIQUE KEY uk_material_code (material_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应急物资表';

-- 15. 演练记录表
DROP TABLE IF EXISTS biz_safety_drill;
CREATE TABLE biz_safety_drill (
    drill_id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '演练ID',
    drill_code         VARCHAR(64)  NOT NULL                 COMMENT '演练编号',
    plan_id            BIGINT                                COMMENT '关联预案ID',
    drill_type         VARCHAR(32)                           COMMENT '演练类型',
    drill_name         VARCHAR(255)                          COMMENT '演练名称',
    drill_date         DATE                                  COMMENT '演练日期',
    participants       TEXT                                  COMMENT '参与人员',
    drill_desc         TEXT                                  COMMENT '演练过程描述',
    eval_result        TEXT                                  COMMENT '演练评估',
    problems           TEXT                                  COMMENT '发现的问题',
    improvements       TEXT                                  COMMENT '改进措施',
    attachments        TEXT                                  COMMENT '附件',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (drill_id),
    UNIQUE KEY uk_drill_code (drill_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='演练记录表';

-- 16. 事故管理表
DROP TABLE IF EXISTS biz_safety_incident;
CREATE TABLE biz_safety_incident (
    incident_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '事故ID',
    incident_code      VARCHAR(64)  NOT NULL                 COMMENT '事故编号',
    incident_level     VARCHAR(32)                           COMMENT '事故等级',
    incident_date      DATETIME                              COMMENT '事故发生时间',
    incident_location  VARCHAR(255)                          COMMENT '事故地点',
    incident_desc      TEXT                                  COMMENT '事故描述',
    casualties         INT          DEFAULT 0                COMMENT '伤亡人数',
    economic_loss      DECIMAL(18,2)                         COMMENT '经济损失',
    cause_analysis     TEXT                                  COMMENT '原因分析',
    four_not_let_go    TEXT                                  COMMENT '四不放过',
    rectify_measures   TEXT                                  COMMENT '整改措施',
    report_person_id   BIGINT                                COMMENT '报告人ID',
    report_person      VARCHAR(64)                           COMMENT '报告人',
    report_time        DATETIME                              COMMENT '报告时间',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (incident_id),
    UNIQUE KEY uk_incident_code (incident_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事故管理表';

-- 17. 到期提醒表
DROP TABLE IF EXISTS biz_safety_remind;
CREATE TABLE biz_safety_remind (
    remind_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '提醒ID',
    remind_type        VARCHAR(32)  NOT NULL                 COMMENT '提醒类型',
    related_id         BIGINT                                COMMENT '关联ID',
    related_name       VARCHAR(255)                          COMMENT '关联名称',
    remind_content     VARCHAR(500)                          COMMENT '提醒内容',
    expire_date        DATE                                  COMMENT '到期日期',
    remind_days        INT                                   COMMENT '提前提醒天数',
    remind_status      VARCHAR(20)  DEFAULT '0'              COMMENT '提醒状态',
    notify_level       VARCHAR(20)                           COMMENT '通知层级',
    to_person_id       BIGINT                                COMMENT '接收人ID',
    to_person_name     VARCHAR(64)                           COMMENT '接收人',
    read_time          DATETIME                              COMMENT '已读时间',
    handle_time        DATETIME                              COMMENT '处理时间',
    handle_by          VARCHAR(64)  DEFAULT ''               COMMENT '处理人',
    handle_result      VARCHAR(255)                          COMMENT '处理结果',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (remind_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='到期提醒表';

-- 18. 检查标准库表（行业预置风险库）
DROP TABLE IF EXISTS biz_safety_std_library;
CREATE TABLE biz_safety_std_library (
    library_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '库ID',
    industry_type      VARCHAR(32)  NOT NULL                 COMMENT '行业类型',
    area_type          VARCHAR(64)                           COMMENT '区域类型',
    risk_point_name    VARCHAR(255)                          COMMENT '风险点名称',
    hazard_desc        TEXT                                  COMMENT '危险源描述',
    l_value            DECIMAL(10,2)                         COMMENT 'L建议值',
    e_value            DECIMAL(10,2)                         COMMENT 'E建议值',
    c_value            DECIMAL(10,2)                         COMMENT 'C建议值',
    d_value            DECIMAL(12,2)                         COMMENT 'D建议值',
    risk_level         VARCHAR(10)                           COMMENT '风险等级建议值',
    check_standard     TEXT                                  COMMENT '检查标准',
    check_frequency    VARCHAR(32)                           COMMENT '排查频次',
    control_measures   TEXT                                  COMMENT '管控措施',
    is_major_hazard    CHAR(1)      DEFAULT '0'              COMMENT '是否重大隐患判定要点',
    major_hazard_rule  VARCHAR(500)                          COMMENT '重大隐患判定规则',
    compliance_file    VARCHAR(500)                          COMMENT '合规文件',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (library_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查标准库表';

-- =============================================
-- 二、字典类型
-- =============================================

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, '行业类别', 'safety_industry_type', '0', 'admin', NOW(), '安全生产行业类别'),
(NULL, '区域节点类型', 'safety_area_node_type', '0', 'admin', NOW(), '区域节点类型'),
(NULL, '区域类型', 'safety_area_type', '0', 'admin', NOW(), '区域功能性质分类'),
(NULL, '证照类型', 'safety_license_type', '0', 'admin', NOW(), '企业资质证照类型'),
(NULL, '设备类别', 'safety_equipment_category', '0', 'admin', NOW(), '设备类别'),
(NULL, '特种设备子类', 'safety_special_equipment', '0', 'admin', NOW(), '特种设备子类'),
(NULL, '危化品分类', 'safety_hazard_class', '0', 'admin', NOW(), '危化品分类'),
(NULL, '剧毒/易制爆标识', 'safety_toxic_flag', '0', 'admin', NOW(), '剧毒/易制爆标识'),
(NULL, '风险等级', 'safety_risk_level', '0', 'admin', NOW(), '风险等级'),
(NULL, '管控层级', 'safety_control_level', '0', 'admin', NOW(), '管控层级'),
(NULL, '排查频次', 'safety_check_frequency', '0', 'admin', NOW(), '排查频次'),
(NULL, '隐患类型', 'safety_hazard_type', '0', 'admin', NOW(), '隐患类型'),
(NULL, '隐患等级', 'safety_hazard_level', '0', 'admin', NOW(), '隐患等级'),
(NULL, '隐患状态', 'safety_hazard_status', '0', 'admin', NOW(), '隐患状态'),
(NULL, '隐患来源', 'safety_hazard_source', '0', 'admin', NOW(), '隐患来源'),
(NULL, '任务类型', 'safety_task_type', '0', 'admin', NOW(), '任务类型'),
(NULL, '任务状态', 'safety_task_status', '0', 'admin', NOW(), '任务状态'),
(NULL, '培训课程类别', 'safety_course_type', '0', 'admin', NOW(), '培训课程类别'),
(NULL, '预案类型', 'safety_plan_type', '0', 'admin', NOW(), '预案类型'),
(NULL, '预案级别', 'safety_plan_level', '0', 'admin', NOW(), '预案级别'),
(NULL, '演练类型', 'safety_drill_type', '0', 'admin', NOW(), '演练类型'),
(NULL, '事故等级', 'safety_incident_level', '0', 'admin', NOW(), '事故等级'),
(NULL, '提醒类型', 'safety_remind_type', '0', 'admin', NOW(), '提醒类型'),
(NULL, '提醒状态', 'safety_remind_status', '0', 'admin', NOW(), '提醒状态'),
(NULL, '特种作业类别', 'safety_job_type', '0', 'admin', NOW(), '特种作业人员作业类别'),
(NULL, '培训计划类型', 'safety_training_plan_type', '0', 'admin', NOW(), '培训计划类型');

-- =============================================
-- 三、字典数据
-- =============================================

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
-- 行业类别
(NULL, 1, '新能源材料', '1', 'safety_industry_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '绿色铝精加工', '2', 'safety_industry_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '高端精细化工', '3', 'safety_industry_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '先进装备制造', '4', 'safety_industry_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '高原特色食品加工', '5', 'safety_industry_type', '', '', 'N', '0', 'admin', NOW(), ''),

-- 区域节点类型
(NULL, 1, '厂区', '1', 'safety_area_node_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '车间', '2', 'safety_area_node_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '区域', '3', 'safety_area_node_type', '', '', 'N', '0', 'admin', NOW(), ''),

-- 区域类型
(NULL, 1, '生产区', '1', 'safety_area_type', '', '', 'N', '0', 'admin', NOW(), '主要生产作业区域'),
(NULL, 2, '仓储区', '2', 'safety_area_type', '', '', 'N', '0', 'admin', NOW(), '原料/成品仓库'),
(NULL, 3, '危化品区', '3', 'safety_area_type', '', '', 'N', '0', 'admin', NOW(), '存放、使用危险化学品区域'),
(NULL, 4, '动火作业区', '4', 'safety_area_type', '', '', 'N', '0', 'admin', NOW(), '允许动火作业区域'),
(NULL, 5, '受限空间区', '5', 'safety_area_type', '', '', 'N', '0', 'admin', NOW(), '受限空间作业区域'),
(NULL, 6, '办公区', '6', 'safety_area_type', '', '', 'N', '0', 'admin', NOW(), '行政办公区域'),
(NULL, 7, '生活区', '7', 'safety_area_type', '', '', 'N', '0', 'admin', NOW(), '宿舍、食堂等生活区域'),
(NULL, 8, '配电区', '8', 'safety_area_type', '', '', 'N', '0', 'admin', NOW(), '变配电设施区域'),
(NULL, 9, '污水处理区', '9', 'safety_area_type', '', '', 'N', '0', 'admin', NOW(), '环保设施区域'),
(NULL, 10, '装卸区', '10', 'safety_area_type', '', '', 'N', '0', 'admin', NOW(), '货物装卸作业区域'),
(NULL, 11, '其他', '99', 'safety_area_type', '', '', 'N', '0', 'admin', NOW(), '其他区域'),

-- 证照类型（企业资质证照）
(NULL, 1, '安全生产许可证', '1', 'safety_license_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '危化品经营许可证', '2', 'safety_license_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '排污许可证', '3', 'safety_license_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '营业执照', '4', 'safety_license_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '消防验收合格证', '5', 'safety_license_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 6, '安全生产标准化证书', '6', 'safety_license_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 7, '其他', '9', 'safety_license_type', '', '', 'N', '0', 'admin', NOW(), ''),

-- 设备类别
(NULL, 1, '特种设备', '1', 'safety_equipment_category', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '普通设备', '2', 'safety_equipment_category', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '安全设备', '3', 'safety_equipment_category', '', '', 'N', '0', 'admin', NOW(), ''),

-- 特种设备子类
(NULL, 1, '锅炉', '1', 'safety_special_equipment', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '压力容器', '2', 'safety_special_equipment', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '压力管道', '3', 'safety_special_equipment', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '起重机械', '4', 'safety_special_equipment', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '厂内机动车', '5', 'safety_special_equipment', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 6, '电梯', '6', 'safety_special_equipment', '', '', 'N', '0', 'admin', NOW(), ''),

-- 危化品分类
(NULL, 1, '第1类 爆炸品', '1', 'safety_hazard_class', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '第2.1类 易燃气体', '2', 'safety_hazard_class', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '第2.2类 非易燃无毒气体', '3', 'safety_hazard_class', 'info', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '第2.3类 毒性气体', '4', 'safety_hazard_class', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '第3类 易燃液体', '5', 'safety_hazard_class', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 6, '第4.1类 易燃固体', '6', 'safety_hazard_class', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 7, '第4.2类 自燃物品', '7', 'safety_hazard_class', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 8, '第4.3类 遇水放出易燃气体物质', '8', 'safety_hazard_class', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 9, '第5.1类 氧化性物质', '9', 'safety_hazard_class', 'primary', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 10, '第5.2类 有机过氧化物', '10', 'safety_hazard_class', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 11, '第6.1类 毒性物质', '11', 'safety_hazard_class', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 12, '第6.2类 感染性物质', '12', 'safety_hazard_class', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 13, '第7类 放射性物质', '13', 'safety_hazard_class', 'info', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 14, '第8类 腐蚀性物质', '14', 'safety_hazard_class', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 15, '第9类 杂项危险物质', '15', 'safety_hazard_class', 'info', '', 'N', '0', 'admin', NOW(), ''),

-- 剧毒/易制爆标识
(NULL, 1, '无', '0', 'safety_toxic_flag', 'info', '', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '剧毒', '1', 'safety_toxic_flag', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '易制爆', '2', 'safety_toxic_flag', 'warning', '', 'N', '0', 'admin', NOW(), ''),

-- 风险等级
(NULL, 1, '重大风险', '1', 'safety_risk_level', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '较大风险', '2', 'safety_risk_level', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '一般风险', '3', 'safety_risk_level', 'info', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '低风险', '4', 'safety_risk_level', 'primary', '', 'N', '0', 'admin', NOW(), ''),

-- 管控层级
(NULL, 1, '公司级', '1', 'safety_control_level', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '部门级', '2', 'safety_control_level', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '车间级', '3', 'safety_control_level', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '班组级', '4', 'safety_control_level', '', '', 'N', '0', 'admin', NOW(), ''),

-- 排查频次
(NULL, 1, '每班', '1', 'safety_check_frequency', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '每日', '2', 'safety_check_frequency', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '每周', '3', 'safety_check_frequency', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '每月', '4', 'safety_check_frequency', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '每季', '5', 'safety_check_frequency', '', '', 'N', '0', 'admin', NOW(), ''),

-- 隐患类型
(NULL, 1, '人的不安全行为', '1', 'safety_hazard_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '物的不安全状态', '2', 'safety_hazard_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '管理缺陷', '3', 'safety_hazard_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '环境因素', '4', 'safety_hazard_type', '', '', 'N', '0', 'admin', NOW(), ''),

-- 隐患等级
(NULL, 1, '一般隐患', '1', 'safety_hazard_level', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '重大隐患', '2', 'safety_hazard_level', 'danger', '', 'N', '0', 'admin', NOW(), ''),

-- 隐患状态
(NULL, 1, '草稿', '0', 'safety_hazard_status', 'info', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '待审批', '1', 'safety_hazard_status', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '审批驳回', '2', 'safety_hazard_status', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '待整改', '3', 'safety_hazard_status', 'primary', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '整改中', '4', 'safety_hazard_status', 'primary', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 6, '待验收', '5', 'safety_hazard_status', 'primary', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 7, '已闭环', '6', 'safety_hazard_status', 'success', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 8, '超期未整改', '7', 'safety_hazard_status', 'danger', '', 'N', '0', 'admin', NOW(), ''),

-- 隐患来源
(NULL, 1, '随手拍', '1', 'safety_hazard_source', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '计划排查', '2', 'safety_hazard_source', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '专项排查', '3', 'safety_hazard_source', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '举报', '4', 'safety_hazard_source', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '上级交办', '5', 'safety_hazard_source', '', '', 'N', '0', 'admin', NOW(), ''),

-- 任务类型
(NULL, 1, '日常排查', '1', 'safety_task_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '综合排查', '2', 'safety_task_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '专项排查', '3', 'safety_task_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '季节性排查', '4', 'safety_task_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '节假日排查', '5', 'safety_task_type', '', '', 'N', '0', 'admin', NOW(), ''),

-- 任务状态
(NULL, 1, '待执行', '0', 'safety_task_status', 'warning', '', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '执行中', '1', 'safety_task_status', 'primary', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '已完成', '2', 'safety_task_status', 'success', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '已作废', '3', 'safety_task_status', 'info', '', 'N', '0', 'admin', NOW(), ''),

-- 培训课程类别
(NULL, 1, '三级教育', '1', 'safety_course_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '特种作业', '2', 'safety_course_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '粉尘防爆', '3', 'safety_course_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '有限空间', '4', 'safety_course_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '危化品', '5', 'safety_course_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 6, '应急处置', '6', 'safety_course_type', '', '', 'N', '0', 'admin', NOW(), ''),

-- 预案类型
(NULL, 1, '综合预案', '1', 'safety_plan_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '专项预案', '2', 'safety_plan_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '现场处置方案', '3', 'safety_plan_type', '', '', 'N', '0', 'admin', NOW(), ''),

-- 预案级别
(NULL, 1, 'Ⅰ级（特别重大）', '1', 'safety_plan_level', '', 'danger', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, 'Ⅱ级（重大）', '2', 'safety_plan_level', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, 'Ⅲ级（较大）', '3', 'safety_plan_level', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, 'Ⅳ级（一般）', '4', 'safety_plan_level', '', 'info', 'N', '0', 'admin', NOW(), ''),

-- 演练类型
(NULL, 1, '综合演练', '1', 'safety_drill_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '专项演练', '2', 'safety_drill_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '现场处置演练', '3', 'safety_drill_type', '', '', 'N', '0', 'admin', NOW(), ''),

-- 提醒类型
(NULL, 1, '证照到期', '1', 'safety_remind_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '特种作业证到期', '2', 'safety_remind_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '设备检验到期', '3', 'safety_remind_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '应急物资过期', '4', 'safety_remind_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '隐患整改超期', '5', 'safety_remind_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 6, '预案修订到期', '6', 'safety_remind_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 7, '风险复评到期', '7', 'safety_remind_type', '', '', 'N', '0', 'admin', NOW(), ''),

-- 提醒状态
(NULL, 1, '待处理', '0', 'safety_remind_status', 'warning', '', 'Y', '0', 'admin', NOW(), '新增提醒待处理'),
(NULL, 2, '处理中', '1', 'safety_remind_status', 'primary', '', 'N', '0', 'admin', NOW(), '正在处理'),
(NULL, 3, '已处理', '2', 'safety_remind_status', 'success', '', 'N', '0', 'admin', NOW(), '已处理完成'),

-- 特种作业类别
(NULL, 1, '电工作业', '1', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '焊接与热切割作业', '2', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '高处作业', '3', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '制冷与空调作业', '4', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '起重机械作业', '5', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 6, '有限空间作业', '6', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 7, '危险化学品安全作业', '7', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 8, '煤矿安全作业', '8', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 9, '金属非金属矿山安全作业', '9', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 10, '石油天然气安全作业', '10', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 11, '冶金（有色）生产安全作业', '11', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 12, '烟花爆竹安全作业', '12', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),

-- 培训计划类型
(NULL, 1, '三级安全教育', '1', 'safety_training_plan_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '特种作业培训', '2', 'safety_training_plan_type', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '粉尘防爆培训', '3', 'safety_training_plan_type', '', 'info', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '有限空间培训', '4', 'safety_training_plan_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '危化品培训', '5', 'safety_training_plan_type', '', 'danger', 'N', '0', 'admin', NOW(), ''),
(NULL, 6, '应急处置培训', '6', 'safety_training_plan_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),

-- 事故等级
(NULL, 1, '特别重大事故', '1', 'safety_incident_level', 'danger', '', 'N', '0', 'admin', NOW(), '造成30人以上死亡，或100人以上重伤，或1亿元以上直接经济损失'),
(NULL, 2, '重大事故',     '2', 'safety_incident_level', 'danger', '', 'N', '0', 'admin', NOW(), '造成10人以上30人以下死亡，或50人以上100人以下重伤，或5000万元以上1亿元以下直接经济损失'),
(NULL, 3, '较大事故',     '3', 'safety_incident_level', 'warning', '', 'N', '0', 'admin', NOW(), '造成3人以上10人以下死亡，或10人以上50人以下重伤，或1000万元以上5000万元以下直接经济损失'),
(NULL, 4, '一般事故',     '4', 'safety_incident_level', 'info', '', 'Y', '0', 'admin', NOW(), '造成3人以下死亡，或10人以下重伤，或1000万元以下直接经济损失');

-- =============================================
-- 四、菜单权限（使用 LAST_INSERT_ID() 避免子查询引用目标表）
-- =============================================

-- 一级菜单：安全生产管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('安全生产管理', 0, 10, 'safety', NULL, 1, 0, 'M', '0', '0', NULL, 'shield', 'admin', NOW(), '安全生产管理目录');
SET @safetyMenuId = LAST_INSERT_ID();

-- 二级菜单：一企一档
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('一企一档', @safetyMenuId, 1, 'arch', NULL, 1, 0, 'M', '0', '0', NULL, 'folder', 'admin', NOW(), '一企一档目录');
SET @archMenuId = LAST_INSERT_ID();

-- 企业档案菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('企业档案', @archMenuId, 1, 'enterprise', 'safety/arch/enterprise/index', 1, 0, 'C', '0', '0', 'safety:enterprise:list', 'form', 'admin', NOW(), '企业档案菜单');
SET @enterpriseMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('企业档案查询', @enterpriseMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:enterprise:query', '#', 'admin', NOW(), ''),
('企业档案新增', @enterpriseMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:enterprise:add', '#', 'admin', NOW(), ''),
('企业档案修改', @enterpriseMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:enterprise:edit', '#', 'admin', NOW(), ''),
('企业档案删除', @enterpriseMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:enterprise:remove', '#', 'admin', NOW(), ''),
('企业档案导出', @enterpriseMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:enterprise:export', '#', 'admin', NOW(), '');

-- 区域管理菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('区域管理', @archMenuId, 2, 'area', 'safety/arch/area/index', 1, 0, 'C', '0', '0', 'safety:area:list', 'tree', 'admin', NOW(), '区域管理菜单');
SET @areaMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('区域查询', @areaMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:area:query', '#', 'admin', NOW(), ''),
('区域新增', @areaMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:area:add', '#', 'admin', NOW(), ''),
('区域修改', @areaMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:area:edit', '#', 'admin', NOW(), ''),
('区域删除', @areaMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:area:remove', '#', 'admin', NOW(), '');

-- 证照管理菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('证照管理', @archMenuId, 3, 'cert', 'safety/arch/cert/index', 1, 0, 'C', '0', '0', 'safety:cert:list', 'documentation', 'admin', NOW(), '证照管理菜单');
SET @certMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('证照查询', @certMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:cert:query', '#', 'admin', NOW(), ''),
('证照新增', @certMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:cert:add', '#', 'admin', NOW(), ''),
('证照修改', @certMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:cert:edit', '#', 'admin', NOW(), ''),
('证照删除', @certMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:cert:remove', '#', 'admin', NOW(), '');

-- 特种人员菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('特种人员', @archMenuId, 4, 'worker', 'safety/arch/worker/index', 1, 0, 'C', '0', '0', 'safety:worker:list', 'peoples', 'admin', NOW(), '特种人员菜单');
SET @workerMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('特种人员查询', @workerMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:worker:query', '#', 'admin', NOW(), ''),
('特种人员新增', @workerMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:worker:add', '#', 'admin', NOW(), ''),
('特种人员修改', @workerMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:worker:edit', '#', 'admin', NOW(), ''),
('特种人员删除', @workerMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:worker:remove', '#', 'admin', NOW(), '');

-- 特种设备菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('特种设备', @archMenuId, 5, 'equipment', 'safety/arch/equipment/index', 1, 0, 'C', '0', '0', 'safety:equipment:list', 'server', 'admin', NOW(), '特种设备台账菜单');
SET @equipmentMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('特种设备查询', @equipmentMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:equipment:query', '#', 'admin', NOW(), ''),
('特种设备新增', @equipmentMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:equipment:add', '#', 'admin', NOW(), ''),
('特种设备修改', @equipmentMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:equipment:edit', '#', 'admin', NOW(), ''),
('特种设备删除', @equipmentMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:equipment:remove', '#', 'admin', NOW(), '');

-- 危化品管理菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('危化品管理', @archMenuId, 6, 'material', 'safety/arch/material/index', 1, 0, 'C', '0', '0', 'safety:material:list', 'shopping', 'admin', NOW(), '危化品管理菜单');
SET @materialMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('危化品查询', @materialMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:material:query', '#', 'admin', NOW(), ''),
('危化品新增', @materialMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:material:add', '#', 'admin', NOW(), ''),
('危化品修改', @materialMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:material:edit', '#', 'admin', NOW(), ''),
('危化品删除', @materialMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:material:remove', '#', 'admin', NOW(), '');

-- 二级菜单：双控机制
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('双控机制', @safetyMenuId, 2, 'dualcontrol', NULL, 1, 0, 'M', '0', '0', NULL, 'chart', 'admin', NOW(), '双控机制目录');
SET @dualControlMenuId = LAST_INSERT_ID();

-- 风险点管理菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('风险点管理', @dualControlMenuId, 1, 'risk', 'safety/dualcontrol/risk/index', 1, 0, 'C', '0', '0', 'safety:risk:list', 'guide', 'admin', NOW(), '风险点管理菜单');
SET @riskMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('风险点查询', @riskMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:risk:query', '#', 'admin', NOW(), ''),
('风险点新增', @riskMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:risk:add', '#', 'admin', NOW(), ''),
('风险点修改', @riskMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:risk:edit', '#', 'admin', NOW(), ''),
('风险点删除', @riskMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:risk:remove', '#', 'admin', NOW(), ''),
('风险点导出', @riskMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:risk:export', '#', 'admin', NOW(), '');

-- 隐患管理菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('隐患管理', @dualControlMenuId, 2, 'hazard', 'safety/dualcontrol/hazard/index', 1, 0, 'C', '0', '0', 'safety:hazard:list', 'bug', 'admin', NOW(), '隐患管理菜单');
SET @hazardMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('隐患查询', @hazardMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:hazard:query', '#', 'admin', NOW(), ''),
('隐患新增', @hazardMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:hazard:add', '#', 'admin', NOW(), ''),
('隐患修改', @hazardMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:hazard:edit', '#', 'admin', NOW(), ''),
('隐患删除', @hazardMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:hazard:remove', '#', 'admin', NOW(), ''),
('隐患导出', @hazardMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:hazard:export', '#', 'admin', NOW(), ''),
('隐患提交', @hazardMenuId, 6, '#', '', 1, 0, 'F', '0', '0', 'safety:hazard:submit', '#', 'admin', NOW(), ''),
('隐患审批', @hazardMenuId, 7, '#', '', 1, 0, 'F', '0', '0', 'safety:hazard:approve', '#', 'admin', NOW(), ''),
('隐患验收', @hazardMenuId, 8, '#', '', 1, 0, 'F', '0', '0', 'safety:hazard:verify', '#', 'admin', NOW(), '');

-- 排查任务菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('排查任务', @dualControlMenuId, 3, 'task', 'safety/dualcontrol/task/index', 1, 0, 'C', '0', '0', 'safety:task:list', 'list', 'admin', NOW(), '排查任务菜单');
SET @taskMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('任务查询', @taskMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:task:query', '#', 'admin', NOW(), ''),
('任务新增', @taskMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:task:add', '#', 'admin', NOW(), ''),
('任务修改', @taskMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:task:edit', '#', 'admin', NOW(), ''),
('任务删除', @taskMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:task:remove', '#', 'admin', NOW(), '');

-- 二级菜单：教育培训
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('教育培训', @safetyMenuId, 3, 'training', NULL, 1, 0, 'M', '0', '0', NULL, 'education', 'admin', NOW(), '教育培训目录');
SET @trainingMenuId = LAST_INSERT_ID();

-- 培训计划菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('培训计划', @trainingMenuId, 1, 'plan', 'safety/training/plan/index', 1, 0, 'C', '0', '0', 'safety:training:plan:list', 'date-range', 'admin', NOW(), '培训计划菜单');
SET @planMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('计划查询', @planMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:training:plan:query', '#', 'admin', NOW(), ''),
('计划新增', @planMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:training:plan:add', '#', 'admin', NOW(), ''),
('计划修改', @planMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:training:plan:edit', '#', 'admin', NOW(), ''),
('计划删除', @planMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:training:plan:remove', '#', 'admin', NOW(), '');

-- 培训记录菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('培训记录', @trainingMenuId, 2, 'record', 'safety/training/record/index', 1, 0, 'C', '0', '0', 'safety:training:record:list', 'edit', 'admin', NOW(), '培训记录菜单');
SET @recordMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('记录查询', @recordMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:training:record:query', '#', 'admin', NOW(), ''),
('记录新增', @recordMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:training:record:add', '#', 'admin', NOW(), ''),
('记录修改', @recordMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:training:record:edit', '#', 'admin', NOW(), ''),
('记录删除', @recordMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:training:record:remove', '#', 'admin', NOW(), '');

-- 二级菜单：应急管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('应急管理', @safetyMenuId, 4, 'emergency', NULL, 1, 0, 'M', '0', '0', NULL, 'example', 'admin', NOW(), '应急管理目录');
SET @emergencyMenuId = LAST_INSERT_ID();

-- 应急预案菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('应急预案', @emergencyMenuId, 1, 'eplan', 'safety/emergency/plan/index', 1, 0, 'C', '0', '0', 'safety:emergency:plan:list', 'link', 'admin', NOW(), '应急预案菜单');
SET @emergencyPlanMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('预案查询', @emergencyPlanMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:plan:query', '#', 'admin', NOW(), ''),
('预案新增', @emergencyPlanMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:plan:add', '#', 'admin', NOW(), ''),
('预案修改', @emergencyPlanMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:plan:edit', '#', 'admin', NOW(), ''),
('预案删除', @emergencyPlanMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:plan:remove', '#', 'admin', NOW(), '');

-- 应急物资菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('应急物资', @emergencyMenuId, 2, 'ematerial', 'safety/emergency/material/index', 1, 0, 'C', '0', '0', 'safety:emergency:material:list', 'checkbox', 'admin', NOW(), '应急物资菜单');
SET @emergencyMaterialMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('物资查询', @emergencyMaterialMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:material:query', '#', 'admin', NOW(), ''),
('物资新增', @emergencyMaterialMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:material:add', '#', 'admin', NOW(), ''),
('物资修改', @emergencyMaterialMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:material:edit', '#', 'admin', NOW(), ''),
('物资删除', @emergencyMaterialMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:material:remove', '#', 'admin', NOW(), '');

-- 演练记录菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('演练记录', @emergencyMenuId, 3, 'drill', 'safety/emergency/drill/index', 1, 0, 'C', '0', '0', 'safety:emergency:drill:list', 'chart', 'admin', NOW(), '演练记录菜单');
SET @drillMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('演练查询', @drillMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:drill:query', '#', 'admin', NOW(), ''),
('演练新增', @drillMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:drill:add', '#', 'admin', NOW(), ''),
('演练修改', @drillMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:drill:edit', '#', 'admin', NOW(), ''),
('演练删除', @drillMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:drill:remove', '#', 'admin', NOW(), '');

-- 事故管理菜单及按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('事故管理', @emergencyMenuId, 4, 'incident', 'safety/emergency/incident/index', 1, 0, 'C', '0', '0', 'safety:emergency:incident:list', 'alert', 'admin', NOW(), '事故管理菜单');
SET @incidentMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('事故查询', @incidentMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:incident:query', '#', 'admin', NOW(), ''),
('事故新增', @incidentMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:incident:add', '#', 'admin', NOW(), ''),
('事故修改', @incidentMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:incident:edit', '#', 'admin', NOW(), ''),
('事故删除', @incidentMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:incident:remove', '#', 'admin', NOW(), '');

-- 二级菜单：到期提醒
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('到期提醒', @safetyMenuId, 5, 'remind', 'safety/remind/index', 1, 0, 'C', '0', '0', 'safety:remind:list', 'message', 'admin', NOW(), '到期提醒菜单');
SET @remindMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('提醒查询', @remindMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:remind:query', '#', 'admin', NOW(), ''),
('提醒处理', @remindMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:remind:handle', '#', 'admin', NOW(), '');

-- 二级菜单：安全驾驶舱
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('安全驾驶舱', @safetyMenuId, 6, 'dashboard', 'safety/dashboard/index', 1, 0, 'C', '0', '0', 'safety:dashboard:list', 'dashboard', 'admin', NOW(), '安全驾驶舱菜单');

-- =============================================
-- 五、给admin角色分配所有安全模块权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE perms LIKE 'safety:%' OR (menu_name = '安全生产管理' AND parent_id = 0);
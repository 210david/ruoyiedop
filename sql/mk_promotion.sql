-- =============================================
-- MK 营销推广侧功能 - SQL脚本
-- 包含：6张新表 + 2张表加法式ALTER + 5个字典 + 菜单权限 + 编号规则
-- 渠道 → 内容 → 引流 → 线索 → 成交 归因闭环
-- =============================================

-- =============================================
-- 一、业务表 DDL（6张新表）
-- =============================================

-- 1. 渠道账号表
DROP TABLE IF EXISTS mk_channel;
CREATE TABLE mk_channel (
    channel_id        BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '渠道ID',
    channel_type      VARCHAR(10)   NOT NULL                 COMMENT '渠道类型（字典 marketing_channel_type：0抖音 1视频号 2小红书 3公众号 4官网 5其他）',
    account_name      VARCHAR(100)  NOT NULL                 COMMENT '账号名称',
    account_id        VARCHAR(100)                           COMMENT '平台账号ID',
    account_url       VARCHAR(500)                           COMMENT '账号主页链接',
    fans_count        INT           DEFAULT 0                COMMENT '粉丝数',
    user_id           BIGINT                                 COMMENT '负责人ID',
    channel_status    VARCHAR(10)   DEFAULT '0'              COMMENT '渠道状态（0正常 1停用）',
    del_flag          CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by         VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time       DATETIME                               COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time       DATETIME                               COMMENT '更新时间',
    remark            VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (channel_id),
    KEY idx_channel_type (channel_type),
    KEY idx_channel_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道账号表';

-- 2. 素材表
DROP TABLE IF EXISTS mk_material;
CREATE TABLE mk_material (
    material_id       BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '素材ID',
    material_no       VARCHAR(32)   NOT NULL                 COMMENT '素材编号（MAT前缀）',
    material_type     VARCHAR(10)   NOT NULL                 COMMENT '素材类型（字典 marketing_material_type：0文案 1图片 2视频 3其他）',
    title             VARCHAR(200)  NOT NULL                 COMMENT '素材标题',
    content           TEXT                                   COMMENT '文案内容（文案型素材）',
    file_url          VARCHAR(500)                           COMMENT '文件URL（文件型素材）',
    file_name         VARCHAR(200)                           COMMENT '文件名',
    file_size         BIGINT                                 COMMENT '文件大小（字节）',
    tags              VARCHAR(200)                           COMMENT '标签（逗号分隔）',
    activity_id       BIGINT                                 COMMENT '关联活动ID（可选）',
    use_count         INT           DEFAULT 0                COMMENT '被引用次数',
    user_id           BIGINT                                 COMMENT '创建人ID',
    material_status   VARCHAR(10)   DEFAULT '0'              COMMENT '素材状态（0正常 1停用）',
    del_flag          CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by         VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time       DATETIME                               COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time       DATETIME                               COMMENT '更新时间',
    remark            VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (material_id),
    UNIQUE KEY uk_material_no (material_no),
    KEY idx_material_type (material_type),
    KEY idx_material_activity (activity_id),
    KEY idx_material_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材表';

-- 3. 内容主档表
DROP TABLE IF EXISTS mk_content;
CREATE TABLE mk_content (
    content_id        BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '内容ID',
    content_no        VARCHAR(32)   NOT NULL                 COMMENT '内容编号（CNT前缀）',
    title             VARCHAR(200)  NOT NULL                 COMMENT '内容标题',
    content_type      VARCHAR(10)   NOT NULL                 COMMENT '内容类型（字典 marketing_content_type：0活动宣传 1产品介绍 2案例故事 3干货知识 4品牌宣传 5其他）',
    theme             VARCHAR(500)                           COMMENT '创作主题/要点（AI生成输入）',
    activity_id       BIGINT                                 COMMENT '关联活动ID（可选）',
    plan_publish_date DATE                                   COMMENT '计划发布日期',
    audit_status      VARCHAR(10)   DEFAULT '0'              COMMENT '审核状态（0草稿 1待审核 2已通过 3已驳回）',
    audit_user_id     BIGINT                                 COMMENT '审核人ID',
    audit_time        DATETIME                               COMMENT '审核时间',
    audit_remark      VARCHAR(500)                           COMMENT '审核意见',
    user_id           BIGINT                                 COMMENT '创建人ID',
    del_flag          CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by         VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time       DATETIME                               COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time       DATETIME                               COMMENT '更新时间',
    remark            VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (content_id),
    UNIQUE KEY uk_content_no (content_no),
    KEY idx_content_type (content_type),
    KEY idx_content_activity (activity_id),
    KEY idx_content_audit_status (audit_status),
    KEY idx_content_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容主档表';

-- 4. 内容包表（内容×渠道）
DROP TABLE IF EXISTS mk_content_package;
CREATE TABLE mk_content_package (
    package_id        BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '内容包ID',
    content_id        BIGINT        NOT NULL                 COMMENT '内容ID',
    channel_id        BIGINT        NOT NULL                 COMMENT '渠道ID',
    channel_type      VARCHAR(10)                            COMMENT '渠道类型（冗余，字典 marketing_channel_type）',
    package_title     VARCHAR(200)                           COMMENT '内容包标题',
    package_body      TEXT                                   COMMENT '内容包正文',
    topic_tags        VARCHAR(200)                           COMMENT '话题标签（逗号分隔）',
    cover_url         VARCHAR(500)                           COMMENT '封面图URL',
    material_ids      VARCHAR(500)                           COMMENT '引用素材ID（逗号分隔）',
    package_status    VARCHAR(10)   DEFAULT '0'              COMMENT '内容包状态（字典 marketing_package_status：0待发布 1已发布 2已取消）',
    publish_url       VARCHAR(500)                           COMMENT '发布链接',
    publish_time      DATETIME                               COMMENT '发布时间',
    del_flag          CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by         VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time       DATETIME                               COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time       DATETIME                               COMMENT '更新时间',
    remark            VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (package_id),
    UNIQUE KEY uk_content_channel (content_id, channel_id),
    KEY idx_package_channel (channel_id),
    KEY idx_package_status (package_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容包表（内容×渠道）';

-- 5. 活动归因链接表（活动×渠道）
DROP TABLE IF EXISTS mk_activity_channel_link;
CREATE TABLE mk_activity_channel_link (
    link_id           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '链接ID',
    activity_id       BIGINT        NOT NULL                 COMMENT '活动ID',
    channel_id        BIGINT        NOT NULL                 COMMENT '渠道ID',
    channel_code      VARCHAR(32)   NOT NULL                 COMMENT '归因码（8位随机码）',
    click_count       INT           DEFAULT 0                COMMENT '点击数',
    signup_count      INT           DEFAULT 0                COMMENT '报名数',
    lead_count        INT           DEFAULT 0                COMMENT '线索数',
    del_flag          CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by         VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time       DATETIME                               COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time       DATETIME                               COMMENT '更新时间',
    remark            VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (link_id),
    UNIQUE KEY uk_channel_code (channel_code),
    UNIQUE KEY uk_activity_channel (activity_id, channel_id),
    KEY idx_link_channel (channel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动归因链接表';

-- 6. 推广效果数据表（按日）
DROP TABLE IF EXISTS mk_promotion_data;
CREATE TABLE mk_promotion_data (
    data_id           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '数据ID',
    package_id        BIGINT        NOT NULL                 COMMENT '内容包ID',
    data_date         DATE          NOT NULL                 COMMENT '数据日期',
    exposure_count    INT           DEFAULT 0                COMMENT '曝光量',
    read_count        INT           DEFAULT 0                COMMENT '阅读量',
    like_count        INT           DEFAULT 0                COMMENT '点赞量',
    comment_count     INT           DEFAULT 0                COMMENT '评论量',
    lead_count        INT           DEFAULT 0                COMMENT '带来线索量',
    del_flag          CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by         VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time       DATETIME                               COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time       DATETIME                               COMMENT '更新时间',
    remark            VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (data_id),
    UNIQUE KEY uk_package_date (package_id, data_date),
    KEY idx_data_date (data_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广效果数据表（按日）';

-- =============================================
-- 二、加法式ALTER（仅加 nullable 列 + 索引）
-- =============================================

-- mk_activity_participant 加报名来源渠道ID
ALTER TABLE mk_activity_participant ADD COLUMN channel_id BIGINT NULL COMMENT '报名来源渠道ID';
ALTER TABLE mk_activity_participant ADD INDEX idx_participant_channel (channel_id);

-- mk_lead 加归因渠道字段
ALTER TABLE mk_lead ADD COLUMN channel_id BIGINT NULL COMMENT '归因渠道账号ID';
ALTER TABLE mk_lead ADD COLUMN channel_type VARCHAR(10) NULL COMMENT '归因渠道类型';
ALTER TABLE mk_lead ADD INDEX idx_lead_channel (channel_id);

-- =============================================
-- 三、字典（WHERE NOT EXISTS 防重复）
-- =============================================

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '渠道类型', 'marketing_channel_type', '0', 'admin', sysdate(), '推广渠道类型字典' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'marketing_channel_type');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '素材类型', 'marketing_material_type', '0', 'admin', sysdate(), '素材类型字典' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'marketing_material_type');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '内容类型', 'marketing_content_type', '0', 'admin', sysdate(), '内容类型字典' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'marketing_content_type');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '内容审核状态', 'marketing_content_audit_status', '0', 'admin', sysdate(), '内容审核状态字典' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'marketing_content_audit_status');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '内容包状态', 'marketing_package_status', '0', 'admin', sysdate(), '内容包状态字典' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'marketing_package_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT t.sort, t.label, t.value, 'marketing_channel_type', '', t.cls, 'N', '0', 'admin', sysdate(), '' FROM (
    SELECT 1 AS sort, '抖音' AS label, '0' AS value, 'primary' AS cls UNION ALL
    SELECT 2, '视频号', '1', 'success' UNION ALL
    SELECT 3, '小红书', '2', 'warning' UNION ALL
    SELECT 4, '公众号', '3', 'info' UNION ALL
    SELECT 5, '官网', '4', 'primary' UNION ALL
    SELECT 6, '其他', '5', ''
) t WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'marketing_channel_type' AND dict_value = t.value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT t.sort, t.label, t.value, 'marketing_material_type', '', t.cls, 'N', '0', 'admin', sysdate(), '' FROM (
    SELECT 1 AS sort, '文案' AS label, '0' AS value, 'primary' AS cls UNION ALL
    SELECT 2, '图片', '1', 'success' UNION ALL
    SELECT 3, '视频', '2', 'warning' UNION ALL
    SELECT 4, '其他', '3', ''
) t WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'marketing_material_type' AND dict_value = t.value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT t.sort, t.label, t.value, 'marketing_content_type', '', t.cls, 'N', '0', 'admin', sysdate(), '' FROM (
    SELECT 1 AS sort, '活动宣传' AS label, '0' AS value, 'primary' AS cls UNION ALL
    SELECT 2, '产品介绍', '1', 'success' UNION ALL
    SELECT 3, '案例故事', '2', 'warning' UNION ALL
    SELECT 4, '干货知识', '3', 'info' UNION ALL
    SELECT 5, '品牌宣传', '4', 'danger' UNION ALL
    SELECT 6, '其他', '5', ''
) t WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'marketing_content_type' AND dict_value = t.value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT t.sort, t.label, t.value, 'marketing_content_audit_status', '', t.cls, 'N', '0', 'admin', sysdate(), '' FROM (
    SELECT 1 AS sort, '草稿' AS label, '0' AS value, 'info' AS cls UNION ALL
    SELECT 2, '待审核', '1', 'warning' UNION ALL
    SELECT 3, '已通过', '2', 'success' UNION ALL
    SELECT 4, '已驳回', '3', 'danger'
) t WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'marketing_content_audit_status' AND dict_value = t.value);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT t.sort, t.label, t.value, 'marketing_package_status', '', t.cls, 'N', '0', 'admin', sysdate(), '' FROM (
    SELECT 1 AS sort, '待发布' AS label, '0' AS value, 'info' AS cls UNION ALL
    SELECT 2, '已发布', '1', 'success' UNION ALL
    SELECT 3, '已取消', '2', 'danger'
) t WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'marketing_package_status' AND dict_value = t.value);

-- =============================================
-- 四、菜单权限（4552-4585 段，营销管理 4000 下）
-- =============================================

-- 推广管理目录（parent=4000）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4552, '推广管理', 4000, 7, 'promotion', NULL, '', '', 1, 0, 'M', '0', '0', '', 'online', 'admin', sysdate(), '营销推广管理目录');

-- 渠道账号
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4553, '渠道账号', 4552, 1, 'channel', 'mk/channel/index', '', 'MkChannel', 1, 0, 'C', '0', '0', 'marketing:channel:list', 'peoples', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4554, '渠道查询', 4553, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:channel:query', '#', 'admin', sysdate(), ''),
(4555, '渠道新增', 4553, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:channel:add', '#', 'admin', sysdate(), ''),
(4556, '渠道修改', 4553, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:channel:edit', '#', 'admin', sysdate(), ''),
(4557, '渠道删除', 4553, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:channel:remove', '#', 'admin', sysdate(), ''),
(4558, '渠道导出', 4553, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:channel:export', '#', 'admin', sysdate(), '');

-- 素材库
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4559, '素材库', 4552, 2, 'material', 'mk/material/index', '', 'MkMaterial', 1, 0, 'C', '0', '0', 'marketing:material:list', 'upload', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4560, '素材查询', 4559, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:material:query', '#', 'admin', sysdate(), ''),
(4561, '素材新增', 4559, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:material:add', '#', 'admin', sysdate(), ''),
(4562, '素材修改', 4559, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:material:edit', '#', 'admin', sysdate(), ''),
(4563, '素材删除', 4559, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:material:remove', '#', 'admin', sysdate(), ''),
(4564, '素材导出', 4559, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:material:export', '#', 'admin', sysdate(), '');

-- 内容发布
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4565, '内容发布', 4552, 3, 'content', 'mk/content/index', '', 'MkContent', 1, 0, 'C', '0', '0', 'marketing:content:list', 'documentation', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4566, '内容查询', 4565, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:content:query', '#', 'admin', sysdate(), ''),
(4567, '内容新增', 4565, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:content:add', '#', 'admin', sysdate(), ''),
(4568, '内容修改', 4565, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:content:edit', '#', 'admin', sysdate(), ''),
(4569, '内容删除', 4565, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:content:remove', '#', 'admin', sysdate(), ''),
(4570, '内容导出', 4565, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:content:export', '#', 'admin', sysdate(), ''),
(4571, '内容审核', 4565, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:content:audit', '#', 'admin', sysdate(), ''),
(4572, '内容发布', 4565, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:content:publish', '#', 'admin', sysdate(), '');

-- 归因链接
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4573, '归因链接', 4552, 4, 'channelLink', 'mk/channelLink/index', '', 'MkChannelLink', 1, 0, 'C', '0', '0', 'marketing:channelLink:list', 'link', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4574, '链接查询', 4573, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:channelLink:query', '#', 'admin', sysdate(), ''),
(4575, '链接生成', 4573, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:channelLink:generate', '#', 'admin', sysdate(), ''),
(4576, '链接删除', 4573, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:channelLink:remove', '#', 'admin', sysdate(), '');

-- 推广数据
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4577, '推广数据', 4552, 5, 'promotionData', 'mk/promotionData/index', '', 'MkPromotionData', 1, 0, 'C', '0', '0', 'marketing:promotionData:list', 'chart', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4578, '数据查询', 4577, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:promotionData:query', '#', 'admin', sysdate(), ''),
(4579, '数据新增', 4577, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:promotionData:add', '#', 'admin', sysdate(), ''),
(4580, '数据修改', 4577, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:promotionData:edit', '#', 'admin', sysdate(), ''),
(4581, '数据删除', 4577, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:promotionData:remove', '#', 'admin', sysdate(), ''),
(4582, '数据导入', 4577, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:promotionData:import', '#', 'admin', sysdate(), '');

-- 推广看板（parent=4006 数据分析）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4583, '推广看板', 4006, 2, 'promotion', 'mk/promotionReport/index', '', 'MkPromotionReport', 1, 0, 'C', '0', '0', 'marketing:promotionReport:view', 'dashboard', 'admin', sysdate(), '');

-- =============================================
-- 五、给admin角色分配推广菜单权限
-- =============================================

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 4552),
(1, 4553), (1, 4554), (1, 4555), (1, 4556), (1, 4557), (1, 4558),
(1, 4559), (1, 4560), (1, 4561), (1, 4562), (1, 4563), (1, 4564),
(1, 4565), (1, 4566), (1, 4567), (1, 4568), (1, 4569), (1, 4570), (1, 4571), (1, 4572),
(1, 4573), (1, 4574), (1, 4575), (1, 4576),
(1, 4577), (1, 4578), (1, 4579), (1, 4580), (1, 4581), (1, 4582),
(1, 4583);

-- =============================================
-- 六、编号规则（素材 MAT、内容 CNT）
-- =============================================

INSERT INTO mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled, status, create_by, create_time, remark)
SELECT 'material', 'mk', '素材编号', 'MAT', 'yyyyMMdd', '1', 3, 1, 1, 0, '', '', '0', '0', 'admin', sysdate(), '素材编号，每日重置' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'material');

INSERT INTO mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled, status, create_by, create_time, remark)
SELECT 'content', 'mk', '内容编号', 'CNT', 'yyyyMMdd', '1', 3, 1, 1, 0, '', '', '0', '0', 'admin', sysdate(), '内容编号，每日重置' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'content');

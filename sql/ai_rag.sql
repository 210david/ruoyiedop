-- =============================================
-- AI RAG知识库 - 全量SQL脚本
-- 包含：知识文档表 + 切片向量表 + embedding配置键 + 菜单权限
-- 遵循：业务零侵入——只新增
-- =============================================

-- =============================================
-- 一、业务表 DDL
-- =============================================

-- 1. AI知识库文档表
DROP TABLE IF EXISTS ai_knowledge_doc;
CREATE TABLE ai_knowledge_doc (
    doc_id        BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '文档ID',
    module        VARCHAR(32)   NOT NULL                 COMMENT '所属模块（dms/qms/safety/hr/platform等）',
    doc_name      VARCHAR(200)  NOT NULL                 COMMENT '文档名称',
    file_type     VARCHAR(10)                            COMMENT '文件类型（pdf/docx/md/txt）',
    file_path     VARCHAR(500)                           COMMENT '存储路径',
    status        CHAR(1)       DEFAULT '0'              COMMENT '状态（0已解析 1解析中 2解析失败）',
    chunk_count   INT           DEFAULT 0                COMMENT '切片数',
    create_by     VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time   DATETIME                               COMMENT '创建时间',
    update_by     VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time   DATETIME                               COMMENT '更新时间',
    remark        VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (doc_id),
    KEY idx_module (module),
    KEY idx_doc_name (doc_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库文档表';

-- 2. AI知识库切片向量表
DROP TABLE IF EXISTS ai_knowledge_chunk;
CREATE TABLE ai_knowledge_chunk (
    chunk_id      BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '切片ID',
    doc_id        BIGINT        NOT NULL                 COMMENT '文档ID',
    chunk_index   INT           NOT NULL                 COMMENT '切片序号',
    content       TEXT          NOT NULL                 COMMENT '切片文本（约500字/片，含50字重叠）',
    embedding     TEXT                                   COMMENT '向量JSON数组（未配置embedding服务时为空，走关键词检索）',
    create_time   DATETIME                               COMMENT '创建时间',
    PRIMARY KEY (chunk_id),
    KEY idx_doc (doc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库切片向量表';

-- =============================================
-- 二、embedding配置键（与对话模型解耦）
-- =============================================

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'AI Embedding API地址', 'ai.embedding_url', '', 'Y', 'admin', sysdate(), 'RAG向量化用，未配置时降级关键词检索'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key='ai.embedding_url');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'AI Embedding API密钥', 'ai.embedding_key', '', 'Y', 'admin', sysdate(), '留空时沿用对话模型密钥'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key='ai.embedding_key');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'AI Embedding模型名', 'ai.embedding_model', '', 'Y', 'admin', sysdate(), '如 text-embedding-v3 / embedding-3'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key='ai.embedding_model');

-- =============================================
-- 三、菜单权限（AI中心下新增"知识文档管理"+"知识问答"子菜单）
-- =============================================

-- 挂到AI中心一级菜单下
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '知识文档管理', m.menu_id, 4, 'knowledge', 'ai/knowledge/index', 'AiKnowledge',
    1, 0, 'C', '0', '0', 'ai:knowledge:list', 'documentation', 'admin', sysdate()
FROM sys_menu m WHERE m.menu_name='AI中心' AND m.parent_id=0
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='ai:knowledge:list');

SET @menuId = LAST_INSERT_ID();

-- 知识问答独立菜单页面
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '知识问答', m.menu_id, 5, 'qa', 'ai/knowledge/qa', 'AiKnowledgeQa',
    1, 0, 'C', '0', '0', 'ai:knowledge:qa', 'education', 'admin', sysdate()
FROM sys_menu m WHERE m.menu_name='AI中心' AND m.parent_id=0
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE component='ai/knowledge/qa');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('知识库查询', @menuId, 1, '', 'F', '0', '0', 'ai:knowledge:query',  'admin', sysdate()),
('知识库新增', @menuId, 2, '', 'F', '0', '0', 'ai:knowledge:add',    'admin', sysdate()),
('知识库删除', @menuId, 3, '', 'F', '0', '0', 'ai:knowledge:remove', 'admin', sysdate()),
('知识库问答', @menuId, 4, '', 'F', '0', '0', 'ai:knowledge:qa',     'admin', sysdate());

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'ai:knowledge%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);

-- 知识库直接命中开关
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '知识库直接命中开关', 'ai.knowledge.direct_hit', 'true', 'Y', 'admin', sysdate(), '高置信命中时直接返回知识库原文，跳过大模型（大幅提速）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key='ai.knowledge.direct_hit');

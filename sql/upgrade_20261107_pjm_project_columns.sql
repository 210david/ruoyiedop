-- ============================================================
-- 升级脚本：pjm_project 质量标准/单位类列扩容
-- 日期：2026-11-07
-- 背景：AI 解析填入的质量标准为完整标准句（如"符合《建筑工程施工质量验收统一标准》（GB50300-2013）…"），
--       原 quality_standard varchar(50) 超长导致保存失败（Data too long for column 'quality_standard'）；
--       单位类字段（建设单位/施工单位/监理单位/设计单位）全称含括号说明时也可能接近 100 上限，一并扩容。
-- 适用库：ruoyi_lyjs（本地开发库与客户库通用，可重复执行）
-- 注意：列上无索引，MODIFY 为无损扩容；重复执行无副作用。
-- ============================================================

-- 回滚语句（如需还原旧长度时执行，注意先确认现有数据不超过旧长度，否则回滚会失败）：
-- ALTER TABLE pjm_project MODIFY COLUMN quality_standard varchar(50) DEFAULT NULL COMMENT '质量标准';
-- ALTER TABLE pjm_project MODIFY COLUMN owner_unit varchar(100) DEFAULT NULL COMMENT '建设单位';
-- ALTER TABLE pjm_project MODIFY COLUMN build_unit varchar(100) DEFAULT NULL COMMENT '施工单位';
-- ALTER TABLE pjm_project MODIFY COLUMN supervisor_unit varchar(100) DEFAULT NULL COMMENT '监理单位';
-- ALTER TABLE pjm_project MODIFY COLUMN design_unit varchar(100) DEFAULT NULL COMMENT '设计单位';

-- 质量标准：50 -> 200（保存完整标准句）
ALTER TABLE pjm_project MODIFY COLUMN quality_standard varchar(200) DEFAULT NULL COMMENT '质量标准';

-- 建设单位/施工单位/监理单位/设计单位：100 -> 200（容纳单位全称）
ALTER TABLE pjm_project MODIFY COLUMN owner_unit varchar(200) DEFAULT NULL COMMENT '建设单位';
ALTER TABLE pjm_project MODIFY COLUMN build_unit varchar(200) DEFAULT NULL COMMENT '施工单位';
ALTER TABLE pjm_project MODIFY COLUMN supervisor_unit varchar(200) DEFAULT NULL COMMENT '监理单位';
ALTER TABLE pjm_project MODIFY COLUMN design_unit varchar(200) DEFAULT NULL COMMENT '设计单位';

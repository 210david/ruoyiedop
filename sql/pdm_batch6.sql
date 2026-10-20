-- ============================================================
-- PDM 设计方案配方明细挂接物料（pdm_batch6.sql）
-- 说明：配方明细「原料名称」改为物料弹框选择器（仅原材料），
--       「单位」由选择物料自动带出（wms_unit 字典值）。
-- 执行时间：2026-10-19
-- ============================================================

-- 配方明细挂接物料ID
ALTER TABLE pdm_scheme_formula ADD COLUMN material_id bigint NULL COMMENT '挂接物料ID（wms_material）' AFTER scheme_id;

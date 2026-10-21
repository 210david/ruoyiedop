-- =============================================================
-- qms_lot_genealogy 谱系表升级（MMS 谱系融合改造）
-- 日期：2026-09-19
-- 内容：
--   1. 新增 source_no 来源单据号（幂等键，防止业务事件重复写入谱系）
--   2. 新增 parent_batch_no / child_batch_no 索引（递归追溯性能）
--   3. 新增 source_type + source_no 联合索引（幂等检查）
-- 幂等键约定：source_type + source_no 唯一标识一条业务谱系事件
--   feeding: source_no = 领料单号-明细序号（如 PZ20260919001-1）
--   complete: source_no = 完工入库单号
-- =============================================================

ALTER TABLE qms_lot_genealogy
    ADD COLUMN source_no VARCHAR(64) DEFAULT NULL COMMENT '来源单据号（幂等键，如领料单号-明细序号/完工入库单号）' AFTER source_type;

CREATE INDEX idx_genealogy_parent_batch ON qms_lot_genealogy (parent_batch_no);
CREATE INDEX idx_genealogy_child_batch ON qms_lot_genealogy (child_batch_no);
CREATE INDEX idx_genealogy_source ON qms_lot_genealogy (source_type, source_no);

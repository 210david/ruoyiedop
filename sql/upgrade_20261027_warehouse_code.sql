-- ============================================================
-- 增量升级脚本：仓库编码必填修复 + 仓位树形对齐（配套版本）
-- 日期：2026-10-27
-- 适用环境：已有生产数据的环境（禁止全库重导）
-- 执行方式：在业务库（如 edop_cfsp）中执行本脚本
-- ============================================================

-- 1. 仓库编号规则修正：前缀 WH、2位序号（与生产现有 WH01~WH04 体系一致）、从1起、步长1
--    （原规则可能为其他前缀/位数，导致新仓库编码与现有编码体系不一致）
UPDATE mk_number_rule
SET prefix = 'WH',
    seq_length = 2,
    seq_start = 1,
    step = 1,
    reset_type = 0,
    connector = '',
    date_format = '',
    update_by = 'admin',
    update_time = now()
WHERE rule_code = 'wms_warehouse';

-- 2. 【必执行】将 current_seq 对齐现有最大仓库编码的数字部分
--    说明：
--    - 按编码中数字部分取最大（避免字符串排序 'WH999' > 'WH162519' 的误判）
--    - 不区分 del_flag：软删除记录仍占用编码（唯一索引不区分删除标记），
--      否则删除后重新新增会生成重复编码，触发 uk_warehouse_code 冲突
--    - 执行后下一个生成的编码 = WH + (最大序号+1)，与历史编码连续衔接
UPDATE mk_number_rule r
SET r.current_seq = (
    SELECT IFNULL(MAX(CAST(REGEXP_REPLACE(w.warehouse_code, '[^0-9]', '') AS UNSIGNED)), 0)
    FROM wms_warehouse w
    WHERE w.node_type = '1'
),
r.update_by = 'admin',
r.update_time = now()
WHERE r.rule_code = 'wms_warehouse';

-- 3. 验证（执行后检查下一个编码是否符合预期）
SELECT rule_code, prefix, seq_length, seq_start, step, current_seq,
       CONCAT(prefix, LPAD(current_seq + step, seq_length, '0')) AS next_code
FROM mk_number_rule
WHERE rule_code = 'wms_warehouse';
-- 预期：prefix=WH, seq_length=2, next_code 与现有 WH01~WH04 衔接（如 WH05）

-- 4. 【可选】若存在历史杂码（如 RG162519 等非 WH 体系的编码），
--    其数字部分会导致新编码序号跳大。如需排除历史杂码只按 WH 体系对齐，
--    改用以下语句代替第 2 步（二选一）：
-- UPDATE mk_number_rule r
-- SET r.current_seq = (
--     SELECT IFNULL(MAX(CAST(REGEXP_REPLACE(w.warehouse_code, '^WH', '') AS UNSIGNED)), 0)
--     FROM wms_warehouse w
--     WHERE w.node_type = '1' AND w.warehouse_code LIKE 'WH%'
-- ),
-- r.update_by = 'admin',
-- r.update_time = now()
-- WHERE r.rule_code = 'wms_warehouse';

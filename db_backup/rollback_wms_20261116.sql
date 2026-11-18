-- ============================================================
-- 回滚：10月库存台账导入系统（2026-11-16）
-- 目标库：ruoyi_ylsp
-- 用途：撤销本次导入的 121 个新建物料、149 行新增库存、65 个库存调整、153 条流水
-- 备份表：wms_material_bak20261116 / wms_inventory_bak20261116 / wms_inventory_log_bak20261116
--
-- 执行方式（任选其一）：
--   mysql -uroot -p ruoyi_ylsp < rollback_wms_20261116.sql
--   或在 Navicat / DBeaver 中整体执行
--
-- 注意：回滚会删除本次新建物料及其库存，请先确认这些物料尚未被业务单据引用。
--       若已被引用，请改为逐条处理，不要整段执行。
-- ============================================================

-- ---------- 执行前自检：确认备份表存在 ----------
SELECT '备份表检查' AS step,
       (SELECT COUNT(*) FROM wms_material_bak20261116)  AS material_bak_rows,
       (SELECT COUNT(*) FROM wms_inventory_bak20261116) AS inventory_bak_rows,
       (SELECT COUNT(*) FROM wms_inventory_log_bak20261116) AS log_bak_rows;

-- ---------- 自检：本次新建物料是否已被业务单据引用 ----------
SELECT '被引用的新建物料行数' AS step, COUNT(*) AS referenced_rows
  FROM wms_inbound_order_detail d
 WHERE d.material_id > 441
UNION ALL
SELECT '被出库单引用', COUNT(*)
  FROM wms_outbound_order_detail d
 WHERE d.material_id > 441;

-- 若上面两个数字都 > 0，请先评估，不要直接执行后面的删除语句。

START TRANSACTION;

-- ---------- 1. 删除本次导入产生的库存流水 ----------
DELETE FROM wms_inventory_log
 WHERE ref_order_type = 'LEDGER_IMPORT';

-- ---------- 2. 删除本次新增的库存行（备份表里不存在的 inventory_id） ----------
DELETE FROM wms_inventory
 WHERE inventory_id NOT IN (SELECT inventory_id FROM wms_inventory_bak20261116);

-- ---------- 3. 恢复被调整的库存行（数量、锁定数量、更新时间） ----------
UPDATE wms_inventory i
  JOIN wms_inventory_bak20261116 b ON i.inventory_id = b.inventory_id
   SET i.qty         = b.qty,
       i.lock_qty    = b.lock_qty,
       i.update_by   = b.update_by,
       i.update_time = b.update_time,
       i.remark      = b.remark,
       i.version     = b.version;

-- ---------- 4. 删除本次新建的物料 ----------
DELETE FROM wms_material
 WHERE material_id > 441
   AND remark LIKE '%由10月库存台账导入%';

COMMIT;

-- ---------- 回滚后校验 ----------
SELECT '物料总数（应回到 441）' AS chk, COUNT(*) AS n
  FROM wms_material WHERE del_flag = '0'
UNION ALL
SELECT '库存行数（应回到 493）', COUNT(*)
  FROM wms_inventory WHERE del_flag = '0'
UNION ALL
SELECT '残留导入流水（应为 0）', COUNT(*)
  FROM wms_inventory_log WHERE ref_order_type = 'LEDGER_IMPORT'
UNION ALL
SELECT '残留新建物料（应为 0）', COUNT(*)
  FROM wms_material WHERE material_id > 441;

-- ============================================================
-- 备选：整表恢复（仅当上面的精确回滚失败时使用，影响面更大）
-- ============================================================
-- START TRANSACTION;
-- DELETE FROM wms_inventory;
-- INSERT INTO wms_inventory SELECT * FROM wms_inventory_bak20261116;
-- DELETE FROM wms_material WHERE material_id > 441;
-- DELETE FROM wms_inventory_log WHERE ref_order_type = 'LEDGER_IMPORT';
-- COMMIT;

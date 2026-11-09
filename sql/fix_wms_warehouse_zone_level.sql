-- ============================================================
-- 修复：仓库结构树缺库区层级（仓库下直接挂仓位）
-- 库：ruoyi_lyjs（郎鹰）
-- 内容：
--   1. 每个仓库下补一个库区（node_type='2'，存储区，编码=仓库编码+A01）
--   2. 把直接挂在仓库下的仓位（node_type='3'）挪到对应库区下，重算 ancestors
-- 说明：
--   - 幂等：库区仅在“该仓库下无库区”时插入；重复执行不会产生重复数据
--   - 仓位主键 warehouse_id 与编码（K01W01 等）均不变，wms_inventory 的
--     location_id 引用不受影响；仅调整 parent_id / ancestors
--   - 库区 area_type='0'（存储区），dept/manager 沿用所属仓库
-- 日期：2026-11-06
-- ============================================================
USE ruoyi_lyjs;

-- 1. 补库区（幂等）
INSERT INTO wms_warehouse
    (parent_id, ancestors, node_type, warehouse_code, warehouse_name,
     dept_id, manager_id, area_type, status, del_flag, create_by, create_time)
SELECT w.warehouse_id,
       CONCAT(w.ancestors, ',', w.warehouse_id),
       '2',
       CONCAT(w.warehouse_code, 'A01'),
       CONCAT(w.warehouse_name, '-存储区'),
       w.dept_id, w.manager_id, '0', '0', '0', 'admin', NOW()
FROM wms_warehouse w
WHERE w.node_type = '1' AND w.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM wms_warehouse z
                  WHERE z.parent_id = w.warehouse_id
                    AND z.node_type = '2' AND z.del_flag = '0');

-- 2. 仓位挪到库区下（仅处理父节点仍是仓库的仓位；幂等）
UPDATE wms_warehouse loc
JOIN wms_warehouse p ON p.warehouse_id = loc.parent_id
                    AND p.node_type = '1' AND p.del_flag = '0'
JOIN wms_warehouse z ON z.parent_id = p.warehouse_id
                    AND z.node_type = '2' AND z.del_flag = '0'
SET loc.parent_id = z.warehouse_id,
    loc.ancestors = CONCAT(z.ancestors, ',', z.warehouse_id),
    loc.update_by = 'admin',
    loc.update_time = NOW()
WHERE loc.node_type = '3' AND loc.del_flag = '0';

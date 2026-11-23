-- ============================================================
-- 仓库管理：入库单头金额（total_amount）历史数据回填
--
-- 背景：
--   wms_inbound_order.total_amount 长期只有字段与 Mapper 映射，
--   全工程无写入点 → 31 张入库单该字段全为 0/NULL，
--   导致「入库按类型统计」报表金额恒为 0。
--
-- 本次回填口径（与代码口径一致）：
--   · status='3'（已完成）：实收金额 = Σ(received_qty × unit_price)
--     —— 已完成单据看真实入库金额，与报表口径对齐
--   · status 为 0/1/2（草稿/待收货/已收货未上架）：计划金额 = Σ(plan_qty × unit_price)
--     —— 未完成单据尚无可结算金额，先按计划金额占位
--
-- 注意：
--   · 单价为 0/NULL 的明细不计金额（与 calcOrderTotals / calcReceivedAmount 一致），
--     因此部分单据回填后仍可能为 0（历史无价明细），这是真实数据现状，不是脚本失败。
--   · 幂等：可重复执行，每次按当前明细重算
--   · 明细可能因跨库位上架被拆分多行，逐行相乘再汇总不会重复计数
--
-- 执行库：按实际环境替换（开发库 ry-vue）
-- 执行日期：2026-11-23
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ---------- 回填前：金额分布 ----------
SELECT '=== 回填前 ===' AS phase;
SELECT status,
       COUNT(*)                                        AS order_cnt,
       SUM(CASE WHEN total_amount = 0 OR total_amount IS NULL THEN 1 ELSE 0 END) AS zero_cnt
FROM wms_inbound_order
WHERE del_flag = '0'
GROUP BY status;

-- ---------- 回填 ----------
UPDATE wms_inbound_order o
JOIN (
    SELECT d.order_id,
           ROUND(SUM(CASE WHEN o2.status = '3'
                          THEN IFNULL(d.received_qty, 0) * IFNULL(d.unit_price, 0)
                          ELSE IFNULL(d.plan_qty, 0)     * IFNULL(d.unit_price, 0)
                     END), 2) AS calc_amount
    FROM wms_inbound_order_detail d
    JOIN wms_inbound_order o2 ON o2.order_id = d.order_id AND o2.del_flag = '0'
    WHERE d.del_flag = '0'
    GROUP BY d.order_id
) t ON t.order_id = o.order_id
SET o.total_amount = t.calc_amount,
    o.update_time  = sysdate()
WHERE o.del_flag = '0';

-- ---------- 回填后：校验 ----------
SELECT '=== 回填后 ===' AS phase;
SELECT status,
       COUNT(*)                                        AS order_cnt,
       SUM(CASE WHEN total_amount = 0 OR total_amount IS NULL THEN 1 ELSE 0 END) AS zero_cnt,
       SUM(total_amount)                               AS sum_amount
FROM wms_inbound_order
WHERE del_flag = '0'
GROUP BY status;

-- 逐单核对：头金额 vs 明细重算（应逐行相等）
SELECT '=== 逐单核对（不一致即异常） ===' AS phase;
SELECT o.order_no, o.status, o.total_amount AS head_amount, t.calc_amount AS detail_amount
FROM wms_inbound_order o
JOIN (
    SELECT d.order_id,
           ROUND(SUM(CASE WHEN o2.status = '3'
                          THEN IFNULL(d.received_qty, 0) * IFNULL(d.unit_price, 0)
                          ELSE IFNULL(d.plan_qty, 0)     * IFNULL(d.unit_price, 0)
                     END), 2) AS calc_amount
    FROM wms_inbound_order_detail d
    JOIN wms_inbound_order o2 ON o2.order_id = d.order_id AND o2.del_flag = '0'
    WHERE d.del_flag = '0'
    GROUP BY d.order_id
) t ON t.order_id = o.order_id
WHERE o.del_flag = '0'
  AND (o.total_amount IS NULL OR o.total_amount <> t.calc_amount);

-- 仍为 0 的单据（历史无价明细导致，属真实数据现状）
SELECT '=== 仍为 0 的单据（通常因明细无单价） ===' AS phase;
SELECT o.order_no, o.status, o.total_qty,
       COUNT(*)                          AS detail_cnt,
       SUM(CASE WHEN IFNULL(d.unit_price,0) > 0 THEN 1 ELSE 0 END) AS priced_detail_cnt
FROM wms_inbound_order o
JOIN wms_inbound_order_detail d ON d.order_id = o.order_id AND d.del_flag = '0'
WHERE o.del_flag = '0' AND (o.total_amount = 0 OR o.total_amount IS NULL)
GROUP BY o.order_id, o.order_no, o.status, o.total_qty
ORDER BY o.order_no;

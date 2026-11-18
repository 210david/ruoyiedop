-- ---------------------------------------------------------------------------
-- 首页顶部固定 5 张核心卡：物料 / 设备 / 销售订单 / 采购订单 / 工单
--
-- 背景：
--   岗位化 KPI（sys_role_kpi 按 role_key 配 4 张）指标口径尚未完善，
--   先统一为一套 5 张通用卡，所有用户看到同一排（一屏一行铺满）。
--
-- 实现：
--   用虚拟 role_key = '__default__' 存这套通用配置；后端 HomeKpiService 在
--   "岗位未命中配置"时自动回退到它，因此：
--     · 岗位差异化配置已于 2026-11-18 下线（四个岗位的数据已清理）；
--     · 后续改为「门户卡片自定义维护」，由管理员在后台统一管理，不在此表恢复岗位行；
--     · 要增删/替换这 5 张卡，改本表的 metric_key 即可，无需改代码。
--
-- 执行：mysql -uroot -p ry-vue < sql/home_kpi_default5.sql （幂等，可重复执行）
-- ---------------------------------------------------------------------------

-- 1. 清理岗位差异化配置，确保所有用户统一命中 __default__
--    （历史数据如需留档，请先单独备份，本脚本不保留岗位行）
DELETE FROM sys_role_kpi WHERE role_key <> '__default__';

-- 2. 通用 5 张卡（顺序即展示顺序）
INSERT INTO sys_role_kpi
  (role_key, seq, metric_key, title, unit, target_titles, target_path, enabled, remark, create_time, update_time)
VALUES
  ('__default__', 1, 'MATERIAL_TOTAL',        '物料',     '种', '物料管理',  '/wms/base/material',  '0', '物料档案数 + 低于安全库存种类数',   NOW(), NOW()),
  ('__default__', 2, 'EQUIPMENT_TOTAL',       '设备',     '台', '设备台账',  '/dms/base/equipment', '0', '设备台账数 + 在用/维修台数',        NOW(), NOW()),
  ('__default__', 3, 'SALES_ORDER_WAIT_SHIP', '销售订单', '单', '订单管理',  '/mk/contract/order',  '0', '待发货销售订单数 + 金额',           NOW(), NOW()),
  ('__default__', 4, 'PURCHASE_ORDER_OPEN',   '采购订单', '单', '采购订单',  '/pms/contract/order', '0', '在途采购订单数 + 待审批/金额',      NOW(), NOW()),
  ('__default__', 5, 'WORK_ORDER_OPEN',       '工单',     '张', '工单管理',  '/mms/workorder-group/workorder', '0', '未完工生产工单数 + 执行中/逾期',    NOW(), NOW())
ON DUPLICATE KEY UPDATE
  seq           = VALUES(seq),
  title         = VALUES(title),
  unit          = VALUES(unit),
  target_titles = VALUES(target_titles),
  target_path   = VALUES(target_path),
  enabled       = '0',
  remark        = VALUES(remark),
  update_time   = NOW();

-- 3. 校验
SELECT seq, metric_key, title, unit, enabled, target_path
  FROM sys_role_kpi
 WHERE role_key = '__default__'
 ORDER BY seq;

-- =============================================
-- 营销订单状态调整升级脚本
-- 版本: v4
-- 日期: 2026-08-02
-- 说明: 订单状态调整为 草稿/待审核/已审核/部分发货/已完成/已驳回/已作废
--       参考采购订单，支持多次发货（部分发货/已完成）
-- =============================================

-- 1. 为 mk_order_item 表增加已发货数量字段
-- 使用 prepared statement 条件处理，确保幂等执行
-- 先清理可能存在的旧列名 delivered_qty（旧版本遗留）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'mk_order_item' AND column_name = 'delivered_qty');
SET @sql = IF(@col_exists > 0, 'ALTER TABLE mk_order_item DROP COLUMN delivered_qty', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- 如果 shipped_qty 已存在则先删除再添加，确保类型为 DECIMAL(18,2)
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'mk_order_item' AND column_name = 'shipped_qty');
SET @sql = IF(@col_exists > 0, 'ALTER TABLE mk_order_item DROP COLUMN shipped_qty', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
ALTER TABLE `mk_order_item`
ADD COLUMN `shipped_qty` DECIMAL(18,2) DEFAULT 0 COMMENT '已发货数量' AFTER `quantity`;

-- 2. 更新字典数据：重建 marketing_order_status
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'marketing_order_status';
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1,  '草稿',    '0', 'marketing_order_status', '', 'info',    'Y', '0', 'admin', sysdate(), '新建订单初始状态'),
(2,  '待审核',  '1', 'marketing_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), '提交后等待审核'),
(3,  '已审核',  '2', 'marketing_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), '审核通过后的状态'),
(4,  '部分发货', '3', 'marketing_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), '部分商品已发货'),
(5,  '已完成',  '4', 'marketing_order_status', '', 'success', 'N', '0', 'admin', sysdate(), '全部商品已发货完成'),
(6,  '已驳回',  '5', 'marketing_order_status', '', 'danger',  'N', '0', 'admin', sysdate(), '审核驳回后的状态'),
(7,  '已作废',  '6', 'marketing_order_status', '', 'danger',  'N', '0', 'admin', sysdate(), '订单已作废');

-- 3. 迁移已有订单数据到新状态码（使用CASE避免状态码冲突）
-- 原 0=草稿   → 新 0=草稿（不变）
-- 原 1=待审核  → 新 1=待审核（不变）
-- 原 2=已发货  → 新 3=部分发货
-- 原 3=已签收  → 新 3=部分发货
-- 原 4=已完成  → 新 4=已完成（不变）
-- 原 5=已取消  → 新 6=已作废
-- 原 6=退货中  → 新 4=已完成
-- 原 7=已退货  → 新 4=已完成
-- 原 8=已确认  → 新 2=已审核
-- 原 9=已驳回  → 新 5=已驳回
UPDATE `mk_order` SET `order_status` = CASE `order_status`
    WHEN '0' THEN '0'
    WHEN '1' THEN '1'
    WHEN '2' THEN '3'
    WHEN '3' THEN '3'
    WHEN '4' THEN '4'
    WHEN '5' THEN '6'
    WHEN '6' THEN '4'
    WHEN '7' THEN '4'
    WHEN '8' THEN '2'
    WHEN '9' THEN '5'
    ELSE `order_status`
END
WHERE `order_status` IN ('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');

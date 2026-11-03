-- =============================================================
-- 升级脚本：销售订单状态字典补充「7 = 已退货」
-- 日期：2026-11-02
-- 说明：
--   1. 退货退款完成后，MkOrderReturnServiceImpl#refundReturn 会将
--      订单状态置为 '7'，但 marketing_order_status 字典只定义到 6，
--      导致状态列显示为空、状态筛选页签无此项、统计计数遗漏；
--   2. 本脚本向字典补充 value='7' 的「已退货」，排序值取 8
--      （排在「已作废(6, sort=7)」之后，符合业务递进关系）；
--   3. 若库中已存在该值则跳过，可重复执行。
--
-- 影响面：仅新增一条字典数据，不改动订单与既有字典。
-- 适用：已有数据的客户库增量升级（新服务器首装由全量脚本覆盖）
-- 执行：双击同目录 run_upgrade_20261102_return_status.bat，或
--       mysql -uroot -p --default-character-set=utf8mb4 库名 < 本文件
-- =============================================================

-- 前置校验：字典分类不存在时不插入（避免产生孤立字典项）
INSERT INTO `sys_dict_data`
    (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`,
     `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 8, '已退货', '7', 'marketing_order_status', '', 'danger', 'N', '0',
       'admin', sysdate(), '退货退款完成后由系统自动置为该状态'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data`
    WHERE `dict_type` = 'marketing_order_status' AND `dict_value` = '7'
)
AND EXISTS (
    SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'marketing_order_status'
);

-- 校验查询
-- SELECT dict_value, dict_label, dict_sort, list_class
--   FROM sys_dict_data
--  WHERE dict_type = 'marketing_order_status'
--  ORDER BY dict_sort;
-- 期望 8 行，含 value=7 已退货

-- 回退语句
-- DELETE FROM sys_dict_data
--  WHERE dict_type = 'marketing_order_status' AND dict_value = '7';
-- 注意：回退前需先将现存 order_status='7' 的订单改为其他状态，
--       否则这些订单的状态列会重新变为空白。

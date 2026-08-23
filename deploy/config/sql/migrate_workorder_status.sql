-- ============================================================
-- 工单状态机精简迁移脚本
-- 将旧状态码(9个)迁移到新状态码(7个)，删除"报工中""待完工质检""完工入库"中间态
-- 旧状态 → 新状态映射：
--   0(新建)        → 0(新建)        不变
--   1(已下达)      → 1(已下达)      不变
--   2(执行中)      → 2(执行中)      不变
--   3(报工中)      → 2(执行中)      合并
--   4(待完工质检)  → 3(已完工)      重编号
--   5(完工入库)    → 3(已完工)      合并
--   6(已关闭)      → 4(已关闭)      重编号
--   7(已暂停)      → 5(已暂停)      重编号
--   8(已作废)      → 6(已作废)      重编号
-- ============================================================

-- 1. 先迁移工单数据的状态码（从大到小，避免覆盖）
UPDATE mms_work_order SET status = '6' WHERE status = '8';  -- 已作废 8 → 6
UPDATE mms_work_order SET status = '5' WHERE status = '7';  -- 已暂停 7 → 5
UPDATE mms_work_order SET status = '4' WHERE status = '6';  -- 已关闭 6 → 4
UPDATE mms_work_order SET status = '3' WHERE status = '5';  -- 完工入库 5 → 3(已完工)
UPDATE mms_work_order SET status = '3' WHERE status = '4';  -- 待完工质检 4 → 3(已完工)
UPDATE mms_work_order SET status = '2' WHERE status = '3';  -- 报工中 3 → 2(执行中)

-- 2. 删除旧的字典数据（先删数据再删类型中不需要的）
DELETE FROM sys_dict_data WHERE dict_type = 'mms_workorder_status' AND dict_value IN ('3','4','5','6','7','8');

-- 3. 更新保留的字典数据
UPDATE sys_dict_data SET dict_label = '已完工', list_class = 'success', dict_sort = 4, remark = '已完工' WHERE dict_type = 'mms_workorder_status' AND dict_value = '3';
UPDATE sys_dict_data SET dict_label = '已关闭', list_class = 'info', dict_sort = 5, remark = '已关闭' WHERE dict_type = 'mms_workorder_status' AND dict_value = '4';
UPDATE sys_dict_data SET dict_label = '已暂停', list_class = 'warning', dict_sort = 6, remark = '已暂停' WHERE dict_type = 'mms_workorder_status' AND dict_value = '5';
UPDATE sys_dict_data SET dict_label = '已作废', list_class = 'danger', dict_sort = 7, remark = '已作废' WHERE dict_type = 'mms_workorder_status' AND dict_value = '6';

-- 4. 更新工单Excel导出注解（代码层已改，此处仅处理数据库已有数据无影响）

-- 5. 验证
SELECT dict_value, dict_label, list_class FROM sys_dict_data WHERE dict_type = 'mms_workorder_status' ORDER BY CAST(dict_value AS UNSIGNED);
SELECT status, COUNT(*) as cnt FROM mms_work_order WHERE del_flag = '0' GROUP BY status ORDER BY CAST(status AS UNSIGNED);

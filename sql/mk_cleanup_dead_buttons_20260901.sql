-- ============================================================
-- 营销模块权限配置清理与定时任务频率调整
-- 2026-09-01  依据：完整性测试报告第五节 + 死按钮专项排查（前端 v-hasPermi 引用与后端 @PreAuthorize 全量对照）
-- ============================================================

-- 一、删除 9 个死按钮（前端无 v-hasPermi 引用、后端无对应 @PreAuthorize 校验，功能由其他按钮承担）：
--   4046 线索领取(marketing:lead:receive)      → 由 4195 公海领取(marketing:lead:pool:receive)承担
--   4173 订单确认(marketing:order:confirm)    → 由 4534 发货确认(marketing:shipment:deliver)承担
--   4174 订单签收(marketing:order:receive)    → 由 4535 发货签收(marketing:shipment:receive)承担
--   4531 订单提交(marketing:order:submit)     → 前端提交按钮实际使用 marketing:order:edit
--   4532 订单审核(marketing:order:audit)      → 前端审核按钮实际使用 marketing:order:approve
--   4529 退货提交(marketing:return:submit)    → 前端提交按钮实际使用 marketing:return:edit
--   4530 退货审核(marketing:return:audit)     → 前端审批按钮实际使用 marketing:return:approve
--   4494 发货提交(marketing:shipment:submit)  → 发货单直接新增，无独立提交流程
--   4495 发货审核(marketing:shipment:audit)   → 发货单无独立审核流程
DELETE FROM sys_role_menu WHERE menu_id IN (4046,4173,4174,4494,4495,4529,4530,4531,4532);
DELETE FROM sys_menu     WHERE menu_id IN (4046,4173,4174,4494,4495,4529,4530,4531,4532);

-- 二、补齐缺失的按钮配置（后端 @PreAuthorize 有校验、前端有 v-hasPermi，但 sys_menu 无记录导致无法在界面授权）：
--   4550 回款导出 marketing:payment:export，挂在 4110 回款管理下
--   4551 退货修改 marketing:return:edit，  挂在 4158 订单退货下
--   （4536/4537、4539~4547 已被 WMS 等其他模块占用，故选用 4550/4551）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (4550, '回款导出', 4110, 9, '', NULL, '', '', 1, 0, 'F', '0', '0', 'marketing:payment:export', '#', 'admin', sysdate(), '测试排查补齐：后端有校验但菜单缺失'),
       (4551, '退货修改', 4158, 9, '', NULL, '', '', 1, 0, 'F', '0', '0', 'marketing:return:edit',    '#', 'admin', sysdate(), '测试排查补齐：后端有校验但菜单缺失');

-- 授权：回款导出给 300/301/302/303（均拥有 4110）；退货修改给 300/301/302（303 财务无退货新增/编辑职责）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
  (300,4550),(301,4550),(302,4550),(303,4550),
  (300,4551),(301,4551),(302,4551);

-- 三、定时任务频率调整：由低频观察配置（每 5~20 分钟）改为每日上午错峰执行（生产建议）
UPDATE sys_job SET cron_expression='0 0 9 * * ?',  update_by='admin', update_time=sysdate() WHERE invoke_target='mkContractTask.checkExpiringContracts()';
UPDATE sys_job SET cron_expression='0 5 9 * * ?',  update_by='admin', update_time=sysdate() WHERE invoke_target='mkContractTask.checkExpiredContracts()';
UPDATE sys_job SET cron_expression='0 10 9 * * ?', update_by='admin', update_time=sysdate() WHERE invoke_target='mkPaymentTask.checkOverduePayments()';
UPDATE sys_job SET cron_expression='0 15 9 * * ?', update_by='admin', update_time=sysdate() WHERE invoke_target='mkLeadFollowTask.checkOverdueFollowUps()';
UPDATE sys_job SET cron_expression='0 20 9 * * ?', update_by='admin', update_time=sysdate() WHERE invoke_target='mkOpportunityTask.checkStagnantOpportunities()';

-- 验证查询：
-- SELECT menu_id,menu_name,perms FROM sys_menu WHERE menu_id IN (4046,4173,4174,4494,4495,4529,4530,4531,4532); -- 应为空
-- SELECT job_id,job_name,cron_expression FROM sys_job WHERE job_id BETWEEN 112 AND 116;                          -- 新 cron

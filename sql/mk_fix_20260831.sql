-- ============================================================
-- 营销管理模块缺陷修复 SQL（2026-08-31 完整性测试报告对应）
-- 1. 菜单授权缺口：4487/4488/4489-4495/4533/4534/4535 仅授权给角色 1/100，
--    导致营销总监无法审核订单、订单→发货→签收链路断裂、财务专员路由无发票管理
-- 2. 定时任务注册：mk 模块已有 4 个提醒任务 bean（MkContractTask/MkPaymentTask/
--    MkLeadFollowTask/MkOpportunityTask），但 sys_job 未注册
-- ============================================================

-- ---------- 1. 营销总监(300)：补授销售订单执行目录、发货管理全链按钮、订单审批 ----------
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(300, 4487), -- 销售订单执行（目录）
(300, 4488), -- 发货管理
(300, 4489), -- 发货查询
(300, 4490), -- 发货新增
(300, 4491), -- 发货修改
(300, 4492), -- 发货删除
(300, 4493), -- 发货导出
(300, 4494), -- 发货提交
(300, 4495), -- 发货审核
(300, 4533), -- 订单审批（marketing:order:approve）
(300, 4534), -- 发货确认
(300, 4535); -- 发货签收

-- ---------- 2. 财务专员(303)：补授"销售订单执行"目录 ----------
-- 发票管理(4157)及其按钮此前已授权，但因父目录 4487 缺失导致路由树不显示
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (303, 4487);

-- ---------- 3. 注册营销提醒定时任务（任务 bean 已存在于 ruoyi-mk/task） ----------
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark) VALUES
('营销-合同临期提醒', 'DEFAULT', 'mkContractTask.checkExpiringContracts()', '0 0 9 * * ?', '3', '1', '0', 'admin', sysdate(), '每日检查7天内到期合同并推送消息提醒'),
('营销-合同过期标记', 'DEFAULT', 'mkContractTask.checkExpiredContracts()', '0 5 9 * * ?', '3', '1', '0', 'admin', sysdate(), '每日检查已过期合同并推送消息提醒'),
('营销-回款逾期检查', 'DEFAULT', 'mkPaymentTask.checkOverduePayments()', '0 10 9 * * ?', '3', '1', '0', 'admin', sysdate(), '每日检查逾期回款计划并推送消息提醒'),
('营销-跟进超期检查', 'DEFAULT', 'mkLeadFollowTask.checkOverdueFollowUps()', '0 15 9 * * ?', '3', '1', '0', 'admin', sysdate(), '每日检查线索/联系人跟进超期并推送消息提醒'),
('营销-商机停滞检查', 'DEFAULT', 'mkOpportunityTask.checkStagnantOpportunities()', '0 20 9 * * ?', '3', '1', '0', 'admin', sysdate(), '每日检查停滞商机并推送消息提醒');

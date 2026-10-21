-- =====================================================================
-- 全模块 AI 功能按钮权限统一配置
-- 原则：所有调用大模型的接口使用专用权限点（xxx:ai:xxx），按钮级控制
--      避免借用宽泛的 list/query 权限导致任意用户消耗大模型 token
-- 配套后端：各模块 AiAssistController 的 @PreAuthorize 已同步修改
-- =====================================================================

-- ---------- DMS 设备管理 ----------
-- 设备台账菜单(3020)下：单台设备健康分析
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI单台健康分析', 3020, 102, '', 'F', '0', '0', 'dms:ai:equipmentHealth', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='dms:ai:equipmentHealth');

-- 工单列表菜单(3040)下：完工报告生成 / 工单Agent
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI完工报告', 3040, 100, '', 'F', '0', '0', 'dms:ai:completeReport', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='dms:ai:completeReport');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI工单Agent', 3040, 101, '', 'F', '0', '0', 'dms:ai:agent', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='dms:ai:agent');

-- AI配置页菜单(3126)下：智能报修 / 派工推荐 / 知识问答（原完全无权限注解，最高风险项）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI智能报修', 3126, 10, '', 'F', '0', '0', 'dms:ai:repairAssistant', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='dms:ai:repairAssistant');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI派工推荐', 3126, 11, '', 'F', '0', '0', 'dms:ai:dispatchRecommend', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='dms:ai:dispatchRecommend');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI维修问答', 3126, 12, '', 'F', '0', '0', 'dms:ai:knowledgeQA', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='dms:ai:knowledgeQA');

-- ---------- WMS 仓库管理 ----------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI库存分析', 2080, 100, '', 'F', '0', '0', 'wms:ai:stockAnalysis', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='wms:ai:stockAnalysis');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI盘点差异分析', 2090, 100, '', 'F', '0', '0', 'wms:ai:stockTakeAnalysis', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='wms:ai:stockTakeAnalysis');

-- ---------- QMS 质量管理 ----------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI处置建议', 4766, 100, '', 'F', '0', '0', 'qms:ai:ncrAdvice', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='qms:ai:ncrAdvice');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI报告初稿', 4851, 100, '', 'F', '0', '0', 'qms:ai:reportDraft', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='qms:ai:reportDraft');

-- ---------- PMS 采购管理 ----------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI采购建议', 4419, 100, '', 'F', '0', '0', 'pms:ai:requestAdvice', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:ai:requestAdvice');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI比价摘要', 4486, 100, '', 'F', '0', '0', 'pms:ai:priceSummary', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:ai:priceSummary');

-- ---------- MMS 生产管控 ----------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI异常诊断', 5140, 100, '', 'F', '0', '0', 'mms:ai:abnormalAdvice', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mms:ai:abnormalAdvice');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI生产日报', 5216, 100, '', 'F', '0', '0', 'mms:ai:dailyReport', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mms:ai:dailyReport');

-- ---------- MK 营销管理 ----------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI流失预警', 4010, 100, '', 'F', '0', '0', 'mk:ai:churnReport', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mk:ai:churnReport');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI流失预测', 4010, 101, '', 'F', '0', '0', 'mk:ai:churnPrediction', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mk:ai:churnPrediction');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI销售周报', 4100, 100, '', 'F', '0', '0', 'mk:ai:weeklyReport', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mk:ai:weeklyReport');

-- ---------- HR 人力资源 ----------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI考勤摘要', 905606, 100, '', 'F', '0', '0', 'hr:ai:attendanceSummary', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='hr:ai:attendanceSummary');

-- ---------- Safety 安全生产 ----------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI隐患建议', 4678, 100, '', 'F', '0', '0', 'safety:ai:hazardAdvice', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='safety:ai:hazardAdvice');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI培训考题', 4697, 100, '', 'F', '0', '0', 'safety:ai:trainingQuiz', 'admin', sysdate()
FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='safety:ai:trainingQuiz');

-- ---------- 角色授权：拥有各父菜单的角色自动获得对应 AI 按钮权限 ----------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT srm.role_id, sm.menu_id
FROM sys_menu sm
JOIN sys_menu parent ON parent.menu_id = sm.parent_id
JOIN sys_role_menu srm ON srm.menu_id = parent.menu_id
WHERE sm.menu_type = 'F' AND sm.perms LIKE '%:ai:%'
  AND sm.perms NOT IN ('dms:ai:faq:list','dms:ai:faq:query','dms:ai:faq:add','dms:ai:faq:edit','dms:ai:faq:remove')
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = srm.role_id AND x.menu_id = sm.menu_id);

-- ---------- 验证 ----------
SELECT menu_id, menu_name, parent_id, perms FROM sys_menu
WHERE menu_type='F' AND perms LIKE '%:ai:%' ORDER BY parent_id, menu_id;

-- 修复: 将测试数据中的隐患关联到排查任务
-- 问题: TEST-TASK-002（锅炉房安全排查）的 inspect_result='2'（有隐患）、hazard_count=1，
--       但隐患表 biz_safety_hazard 中的测试数据没有设置 task_id，
--       导致查看排查任务详情时"关联隐患"区域不显示隐患信息。
-- 修复: 将 TEST-HZD-002（锅炉安全阀校验过期）关联到 TEST-TASK-002（锅炉房安全排查）

SET @taskId2 = (SELECT task_id FROM biz_safety_inspection_task WHERE task_code = 'TEST-TASK-002' LIMIT 1);
UPDATE biz_safety_hazard SET task_id = @taskId2, source_type = '2' WHERE hazard_code = 'TEST-HZD-002';

SELECT CONCAT('已将隐患 TEST-HZD-002 关联到排查任务 TEST-TASK-002 (task_id=', IFNULL(@taskId2, 'NULL'), ')') AS info;

-- =============================================================
-- 食品行业商机阶段初始化（cfsp 库）
-- 模型：线索报备→初步接洽→需求诊断→送样测试/验厂→报价与条款谈判→试点首单→签约合作
-- 幂等：按 stage_code 防重复
-- =============================================================
USE RUOYI_CFSP;

INSERT INTO mk_opportunity_stage (stage_code, stage_name, sort, win_rate, max_days, status, create_by, create_time, remark)
SELECT 'stage1', '线索报备', 1, 5, 15, '0', 'admin', NOW(), '糖酒会/展会获客、转介绍、地推扫街，报备待审核'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM mk_opportunity_stage WHERE stage_code = 'stage1');
INSERT INTO mk_opportunity_stage (stage_code, stage_name, sort, win_rate, max_days, status, create_by, create_time, remark)
SELECT 'stage2', '初步接洽', 2, 10, 20, '0', 'admin', NOW(), '建立联系，核验证照资质、资金与渠道覆盖能力'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM mk_opportunity_stage WHERE stage_code = 'stage2');
INSERT INTO mk_opportunity_stage (stage_code, stage_name, sort, win_rate, max_days, status, create_by, create_time, remark)
SELECT 'stage3', '需求诊断', 3, 25, 30, '0', 'admin', NOW(), '摸清经营品类、现有供应商与价盘、目标利润空间'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM mk_opportunity_stage WHERE stage_code = 'stage3');
INSERT INTO mk_opportunity_stage (stage_code, stage_name, sort, win_rate, max_days, status, create_by, create_time, remark)
SELECT 'stage4', '送样测试/验厂', 4, 45, 30, '0', 'admin', NOW(), '寄样试吃、工厂验厂、检测报告与资质审核（食品行业关键关卡）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM mk_opportunity_stage WHERE stage_code = 'stage4');
INSERT INTO mk_opportunity_stage (stage_code, stage_name, sort, win_rate, max_days, status, create_by, create_time, remark)
SELECT 'stage5', '报价与条款谈判', 5, 65, 30, '0', 'admin', NOW(), '进货价、返利政策、账期、退换货、区域保护'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM mk_opportunity_stage WHERE stage_code = 'stage5');
INSERT INTO mk_opportunity_stage (stage_code, stage_name, sort, win_rate, max_days, status, create_by, create_time, remark)
SELECT 'stage6', '试点首单', 6, 80, 45, '0', 'admin', NOW(), '首批铺货试销，考核铺市率与动销，再谈长期合作'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM mk_opportunity_stage WHERE stage_code = 'stage6');
INSERT INTO mk_opportunity_stage (stage_code, stage_name, sort, win_rate, max_days, status, create_by, create_time, remark)
SELECT 'stage7', '签约合作', 7, 95, 15, '0', 'admin', NOW(), '签年度经销合同、定任务量与返利阶梯，进入订单执行'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM mk_opportunity_stage WHERE stage_code = 'stage7');

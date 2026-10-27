-- =====================================================
-- 工艺路线串并行混合测试数据修复脚本
-- 将部分纯串行工序改为串并行混合，方便后续测试
-- 逻辑：step_seq 相同的工序为并行执行
-- =====================================================

-- 获取路线ID
SET @rt_sc1 = (SELECT route_id FROM mms_route WHERE route_no='RT-SC-001');
SET @rt_sc2 = (SELECT route_id FROM mms_route WHERE route_no='RT-SC-002');
SET @rt_sc3 = (SELECT route_id FROM mms_route WHERE route_no='RT-SC-003');
SET @rt_sc4 = (SELECT route_id FROM mms_route WHERE route_no='RT-SC-004');
SET @rt_sc5 = (SELECT route_id FROM mms_route WHERE route_no='RT-SC-005');
SET @rt_fp1 = (SELECT route_id FROM mms_route WHERE route_no='RT-FP-001');
SET @rt_fp2 = (SELECT route_id FROM mms_route WHERE route_no='RT-FP-002');
SET @rt_fp3 = (SELECT route_id FROM mms_route WHERE route_no='RT-FP-003');
SET @rt_fp4 = (SELECT route_id FROM mms_route WHERE route_no='RT-FP-004');
SET @rt_fp5 = (SELECT route_id FROM mms_route WHERE route_no='RT-FP-005');

-- =====================================================
-- RT-SC-001: PCBA主板组件 — 保持纯串行（对照组）
-- SMT(10) → 点胶(20) → 测试(30)
-- =====================================================
UPDATE mms_route_process SET step_seq = 10 WHERE route_id = @rt_sc1 AND process_code = 'PRC-SC-SMT';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_sc1 AND process_code = 'PRC-SC-GLU';
UPDATE mms_route_process SET step_seq = 30 WHERE route_id = @rt_sc1 AND process_code = 'PRC-SC-TST';

-- =====================================================
-- RT-SC-002: 锂电池组 — 点胶与测试并行
-- 焊接(10) → 点胶(20) + 测试(20) [并行]
-- =====================================================
UPDATE mms_route_process SET step_seq = 10 WHERE route_id = @rt_sc2 AND process_code = 'PRC-SC-WLD';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_sc2 AND process_code = 'PRC-SC-GLU';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_sc2 AND process_code = 'PRC-SC-TST';

-- =====================================================
-- RT-SC-003: 手环外壳组件 — 研磨与检验并行
-- 注塑(10) → 研磨(20) + 检验(20) [并行]
-- =====================================================
UPDATE mms_route_process SET step_seq = 10 WHERE route_id = @rt_sc3 AND process_code = 'PRC-SC-INJ';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_sc3 AND process_code = 'PRC20260819005';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_sc3 AND process_code = 'PRC20260819009';

-- =====================================================
-- RT-SC-004: 耳机外壳组件 — 研磨与点胶并行
-- 注塑(10) → 研磨(20) + 点胶(20) [并行] → 检验(30)
-- =====================================================
UPDATE mms_route_process SET step_seq = 10 WHERE route_id = @rt_sc4 AND process_code = 'PRC-SC-INJ';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_sc4 AND process_code = 'PRC20260819005';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_sc4 AND process_code = 'PRC-SC-GLU';
UPDATE mms_route_process SET step_seq = 30 WHERE route_id = @rt_sc4 AND process_code = 'PRC20260819009';

-- =====================================================
-- RT-SC-005: 耳机充电盒组件 — 注塑与SMT并行
-- 注塑(10) + SMT(10) [并行] → 装配(20) → 测试(30)
-- =====================================================
UPDATE mms_route_process SET step_seq = 10 WHERE route_id = @rt_sc5 AND process_code = 'PRC-SC-INJ';
UPDATE mms_route_process SET step_seq = 10 WHERE route_id = @rt_sc5 AND process_code = 'PRC-SC-SMT';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_sc5 AND process_code = 'PRC20260819006';
UPDATE mms_route_process SET step_seq = 30 WHERE route_id = @rt_sc5 AND process_code = 'PRC-SC-TST';

-- =====================================================
-- RT-FP-001: 智能手环Pro — 功能测试与老化并行
-- 组装(10) → 功能(20) + 老化(20) [并行] → 检验(30) → 包装(40)
-- =====================================================
UPDATE mms_route_process SET step_seq = 10 WHERE route_id = @rt_fp1 AND process_code = 'PRC-FP-ASM';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_fp1 AND process_code = 'PRC-FP-FNC';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_fp1 AND process_code = 'PRC-FP-AGE';
UPDATE mms_route_process SET step_seq = 30 WHERE route_id = @rt_fp1 AND process_code = 'PRC20260819009';
UPDATE mms_route_process SET step_seq = 40 WHERE route_id = @rt_fp1 AND process_code = 'PRC-FP-PKG';

-- =====================================================
-- RT-FP-002: 无线蓝牙耳机 — 老化与检验并行
-- 组装(10) → 功能(20) → 老化(30) + 检验(30) [并行] → 包装(40)
-- =====================================================
UPDATE mms_route_process SET step_seq = 10 WHERE route_id = @rt_fp2 AND process_code = 'PRC-FP-ASM';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_fp2 AND process_code = 'PRC-FP-FNC';
UPDATE mms_route_process SET step_seq = 30 WHERE route_id = @rt_fp2 AND process_code = 'PRC-FP-AGE';
UPDATE mms_route_process SET step_seq = 30 WHERE route_id = @rt_fp2 AND process_code = 'PRC20260819009';
UPDATE mms_route_process SET step_seq = 40 WHERE route_id = @rt_fp2 AND process_code = 'PRC-FP-PKG';

-- =====================================================
-- RT-FP-003: 手环表带替换装 — 保持纯串行（对照组）
-- 注塑(10) → 检验(20) → 包装(30)
-- =====================================================
UPDATE mms_route_process SET step_seq = 10 WHERE route_id = @rt_fp3 AND process_code = 'PRC-SC-INJ';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_fp3 AND process_code = 'PRC20260819009';
UPDATE mms_route_process SET step_seq = 30 WHERE route_id = @rt_fp3 AND process_code = 'PRC20260819010';

-- =====================================================
-- RT-FP-004: 蓝牙耳机套装 — 功能与老化并行
-- 组装(10) → 功能(20) + 老化(20) [并行] → 包装(30)
-- =====================================================
UPDATE mms_route_process SET step_seq = 10 WHERE route_id = @rt_fp4 AND process_code = 'PRC-FP-ASM';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_fp4 AND process_code = 'PRC-FP-FNC';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_fp4 AND process_code = 'PRC-FP-AGE';
UPDATE mms_route_process SET step_seq = 30 WHERE route_id = @rt_fp4 AND process_code = 'PRC-FP-PKG';

-- =====================================================
-- RT-FP-005: 智能手环Lite — 组装与功能并行
-- 组装(10) + 功能(10) [并行] → 检验(20) → 包装(30)
-- =====================================================
UPDATE mms_route_process SET step_seq = 10 WHERE route_id = @rt_fp5 AND process_code = 'PRC-FP-ASM';
UPDATE mms_route_process SET step_seq = 10 WHERE route_id = @rt_fp5 AND process_code = 'PRC-FP-FNC';
UPDATE mms_route_process SET step_seq = 20 WHERE route_id = @rt_fp5 AND process_code = 'PRC20260819009';
UPDATE mms_route_process SET step_seq = 30 WHERE route_id = @rt_fp5 AND process_code = 'PRC-FP-PKG';

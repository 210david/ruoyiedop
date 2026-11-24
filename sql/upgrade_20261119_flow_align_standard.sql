-- ============================================================
-- 与「标准仓」对齐：流程发起页差异修正
-- 日期：2026-11-19
-- 背景：2026-11-18 的流程配置同步脚本（upgrade_20261118_flow_config_data.sql）
--       同步后流程发起页与标准仓存在 3 处差异，本脚本逐项修正：
--   1) 流程分类错误：打卡补卡 / 外出申请 / 出差申请 被同步成 oa（行政办公），
--      标准仓为 hr（人事流程）→ 修正模型与已部署定义的两处分类快照
--   2) 费用报销活动版本错误：部署的 v2 未标记活动版本，发起页仍显示 v1 →
--      把活动版本切到 expenseReimburse:v2
--   3) 测试审批流程（leave_process）物理删除：模型、4 个部署与定义、
--      27 个流程实例及全部审批记录、测试表单(10) 与测试动态表
-- 说明：分类在部署时会快照到 act_re_procdef.CATEGORY_，仅改模型不生效，
--       故两处都要改；Flowable 引擎有部署缓存，执行后需重启后端。
-- ============================================================
SET NAMES utf8mb4;

-- ---------- 1. 分类修正：人事流程（hr） ----------
UPDATE act_re_model   SET CATEGORY_ = 'hr' WHERE KEY_ IN ('makeupApply', 'outingApply', 'travelApply');
UPDATE ACT_RE_PROCDEF SET CATEGORY_ = 'hr' WHERE KEY_ IN ('makeupApply', 'outingApply', 'travelApply');

-- ---------- 2. 费用报销活动版本：v2 ----------
UPDATE flow_procdef_ext SET active_flag = '0' WHERE procdef_id LIKE 'expenseReimburse%';
INSERT INTO flow_procdef_ext (procdef_id, remark, active_flag)
SELECT id, '同步标准仓：v2 设为活动版本', '1'
FROM (SELECT ID_ AS id
      FROM ACT_RE_PROCDEF WHERE KEY_ = 'expenseReimburse' AND VERSION_ = 2) t
WHERE NOT EXISTS (SELECT 1 FROM flow_procdef_ext e WHERE e.procdef_id = t.id);
UPDATE flow_procdef_ext SET active_flag = '1' WHERE procdef_id LIKE 'expenseReimburse:2:%';

-- ---------- 3. 测试审批流程物理删除 ----------
-- 注意：ACT_* 表的删除已通过 Flowable 引擎接口完成（先删实例、再删模型，
--       由引擎级联清理运行表/历史表/部署与字节阵列），纯 SQL 无法安全复现，
--       此处仅保留业务侧孤儿数据的幂等清理语句。
DELETE FROM flow_procdef_ext WHERE procdef_id LIKE 'leave_process%';
DELETE FROM flow_leave;
DELETE FROM flow_request  WHERE process_definition_key = 'leave_process';
DELETE FROM flow_form     WHERE form_id = 10;                    -- 测试流程申请表单
DROP TABLE IF EXISTS flow_data_10_dt2, flow_data_10_dt1, flow_data_10;

-- ---------- 核查（预期：oa=2 / fin=1 / hr=5，共 8 条） ----------
-- SELECT CATEGORY_, COUNT(DISTINCT KEY_) FROM ACT_RE_PROCDEF GROUP BY CATEGORY_;
-- SELECT COUNT(*) FROM ACT_HI_PROCINST;                            -- 预期 0
-- SELECT COUNT(*) FROM ACT_RE_PROCDEF WHERE KEY_ = 'leave_process'; -- 预期 0

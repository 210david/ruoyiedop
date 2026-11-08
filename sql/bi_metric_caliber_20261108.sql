-- =============================================
-- BI 指标字典 - 口径说明补全（统计周期 + 计算口径）
-- 日期：2026-11-08
-- 背景：看板卡片未区分「当月 / 全量累计 / 时点存量」，同屏混用易误读
--       （如线索转化率为建库至今累计，而非当月转化率）
-- 说明：复用既有 caliber_desc 字段（varchar(500)，字典维护页可编辑、变更留痕），
--       不改表结构；按各 StatsProvider 实际取数代码核定，非按 freq 字段推断
--       （freq 表示快照频率，不等于统计周期）
-- 幂等：全部为按 metric_code 的 UPDATE，可重复执行
-- =============================================

-- ---------- 营销销售 sales ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月回款登记金额合计（mk_payment_plan 实际回款金额，按实际回款日期归月；已回款/部分回款均计入）'
WHERE metric_code = 'MK_RCPT_AMT';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月生效合同金额合计（按合同签订日期归月）'
WHERE metric_code = 'MK_SIG_AMT';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：成交线索 / 线索总数，取建库至今全部线索，无日期过滤'
WHERE metric_code = 'MK_LEAD_CVR';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：赢单商机数 / 已关闭商机数（赢单+输单），取全部历史商机，无日期过滤'
WHERE metric_code = 'MK_WIN_RATE';

-- ---------- 投标管理 bid ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：中标项目 / 已出结果项目数（中标 + 未中标 + 废标流标），取全部历史项目，无日期过滤'
WHERE metric_code = 'BID_WIN_RATE';

-- ---------- 生产制造 production ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：已完工工单 / 有效工单总数（不含草稿、已作废），取全部历史工单，无日期过滤'
WHERE metric_code = 'MMS_WO_RATE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月新增异常工单数（全部状态，按上报时间归月）'
WHERE metric_code = 'MMS_EXCEPTION';

-- ---------- 设备管理 equipment ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：设备综合效率，估算口径（日运行 16 小时假设），按快照日期归月；接入实测采集后切换实测值'
WHERE metric_code = 'DMS_OEE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月故障工单停机时长合计（按报修时间归月）'
WHERE metric_code = 'DMS_DOWN_HRS';

-- ---------- 质量管理 quality ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：一次检验合格数 / 已完成检验数，取全部历史检验任务，无日期过滤'
WHERE metric_code = 'QMS_PASS_RATE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：时点存量｜口径：当前状态不等于“已关闭”的 NCR 数量（驳回亦视为未关闭），取当前快照时点'
WHERE metric_code = 'QMS_NCR_OPEN';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：已关闭 CAPA / CAPA 总数，取全部历史 CAPA 记录，无日期过滤'
WHERE metric_code = 'QMS_CAPA_RATE';

-- ---------- 采购供应 purchase ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：按期到货订单数 / 已到货订单数，“按期”以订单要求交期为基准，取全部已到货订单，无日期过滤'
WHERE metric_code = 'PMS_ONTIME_RATE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月采购金额 Top5 供应商金额合计 / 当月采购总额（仅统计订单日期在当月、有供应商且金额有效的订单）'
WHERE metric_code = 'PMS_TOP_SUP';

-- ---------- 仓储库存 inventory ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月出库成本（按入库单价取价）/ 库存金额；平均库存金额以统计日时点库存金额近似'
WHERE metric_code = 'WMS_TURNOVER';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：时点存量｜口径：低库存 + 积压 + 临期 + 过期预警数量合计，取当前快照时点'
WHERE metric_code = 'WMS_ALERT_CNT';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：时点存量｜口径：超 90 天无出库记录的库存金额（不含委外/在制），取当前快照时点'
WHERE metric_code = 'WMS_SLOW_AMT';

-- ---------- 人力资源 hr ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：时点存量｜口径：当前在职状态员工数，取当前快照时点'
WHERE metric_code = 'HR_HEADCOUNT';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月离职人数 / 期初在职人数（按离职日期归月）'
WHERE metric_code = 'HR_LEAVE_RATE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月人工成本 / 当月完工入库产值'
WHERE metric_code = 'HR_COST_RATIO';

-- ---------- 安全生产 safety ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：已整改隐患 / 应整改隐患总数（草稿、待审批、驳回不纳入分母），取全部历史隐患，无日期过滤'
WHERE metric_code = 'SAFETY_RECT_RATE';

-- ---------- 综合价值链 synthesis ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：合同签订到全额回清的平均天数；仅统计回清完成日期在当月的合同，未回清合同不纳入'
WHERE metric_code = 'OTC_CYCLE';

-- =============================================
-- 校验：确认 22 个指标均已标注统计周期
-- =============================================
-- SELECT metric_code, metric_name, domain, caliber_desc FROM bi_metric_dict
--  WHERE metric_code IN ('MK_RCPT_AMT','MK_SIG_AMT','MK_LEAD_CVR','MK_WIN_RATE','BID_WIN_RATE',
--        'MMS_WO_RATE','MMS_EXCEPTION','DMS_OEE','DMS_DOWN_HRS','QMS_PASS_RATE','QMS_NCR_OPEN',
--        'QMS_CAPA_RATE','PMS_ONTIME_RATE','PMS_TOP_SUP','WMS_TURNOVER','WMS_ALERT_CNT',
--        'WMS_SLOW_AMT','HR_HEADCOUNT','HR_LEAVE_RATE','HR_COST_RATIO','SAFETY_RECT_RATE','OTC_CYCLE')
--  ORDER BY domain, metric_code;

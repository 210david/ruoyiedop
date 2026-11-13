-- =============================================
-- BI 决策支持子系统 - 全量SQL脚本 (阶段一)
-- 包含：4张业务表 + 指标字典初始数据(22个指标) + 菜单权限
-- 依据：doc/plans/PRD-决策支持模块-功能需求方案.md v1.2
-- 规范：统一开发规范 3.1/3.4/3.5/7（显式 COLLATE、M/C/F 三级菜单、统计类数据权限豁免声明）
-- =============================================

-- =============================================
-- 一、业务表 DDL
-- 数据权限声明（规范 3.5.1）：以下 4 张表均属「统计/追溯类」，豁免归属字段自动填充；
-- 其中 bi_metric_snapshot 携带 dept_id 作为统计维度（非归属），驾驶舱按用户数据权限过滤该维度。
-- =============================================

-- 1. 指标字典表
DROP TABLE IF EXISTS bi_metric_dict;
CREATE TABLE bi_metric_dict (
    id              bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    metric_code     varchar(50)  NOT NULL COMMENT '指标编码（唯一）',
    metric_name     varchar(100) NOT NULL COMMENT '指标名称',
    domain          varchar(20)  NOT NULL COMMENT '所属域（sales销售/bid投标/production生产/quality质量/purchase采购/inventory库存/equipment设备/safety安全/hr人力/synthesis综合）',
    caliber_desc    varchar(500) NOT NULL COMMENT '口径定义（文字描述，最终解释以本字段为准）',
    calc_ref        varchar(100) DEFAULT NULL COMMENT '计算实现引用（如 MkStatsProvider）',
    refresh_type    varchar(20)  NOT NULL DEFAULT 'snapshot' COMMENT '刷新方式（snapshot快照/realtime实时）',
    freq            varchar(10)  NOT NULL DEFAULT 'day' COMMENT '统计频率（day日/month月）',
    threshold_red    decimal(18,4) DEFAULT NULL COMMENT '红线阈值（P1预警启用）',
    threshold_yellow decimal(18,4) DEFAULT NULL COMMENT '黄线阈值（P1预警启用）',
    threshold_dir   varchar(10)  DEFAULT NULL COMMENT '阈值方向（lte低于预警/gte高于预警）',
    owner           varchar(50)  DEFAULT NULL COMMENT '指标责任人',
    status          char(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    del_flag        char(1)      DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
    create_by       varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time     datetime     DEFAULT NULL COMMENT '创建时间',
    update_by       varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time     datetime     DEFAULT NULL COMMENT '更新时间',
    remark          varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_metric_code (metric_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='决策支持-指标字典';

-- 2. 指标字典变更留痕表（F1.1：谁改的口径、何时生效）
DROP TABLE IF EXISTS bi_metric_dict_log;
CREATE TABLE bi_metric_dict_log (
    id          bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    metric_code varchar(50) NOT NULL COMMENT '指标编码',
    change_type varchar(20) NOT NULL COMMENT '变更类型（create新增/update修改/disable停用）',
    field_name  varchar(50) DEFAULT NULL COMMENT '变更字段',
    old_value   varchar(500) DEFAULT NULL COMMENT '变更前内容',
    new_value   varchar(500) DEFAULT NULL COMMENT '变更后内容',
    change_by   varchar(64) DEFAULT '' COMMENT '变更人',
    change_time datetime    DEFAULT NULL COMMENT '变更时间',
    PRIMARY KEY (id),
    KEY idx_metric_code (metric_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='决策支持-指标字典变更留痕';

-- 3. 指标日快照表（F1.3：Quartz 每日 01:00 幂等写入，同日重跑先删后插）
DROP TABLE IF EXISTS bi_metric_snapshot;
CREATE TABLE bi_metric_snapshot (
    id            bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    metric_code   varchar(50) NOT NULL COMMENT '指标编码',
    metric_date   date        NOT NULL COMMENT '指标日期（快照粒度：日）',
    dept_id       bigint      NOT NULL DEFAULT 0 COMMENT '部门维度（sys_dept.dept_id；0=全部门汇总，避免 NULL 使唯一键失效）。统计维度列，非归属字段',
    metric_value  decimal(18,4) DEFAULT NULL COMMENT '指标值（无数据为 null，前端展示 —）',
    unit          varchar(20) DEFAULT NULL COMMENT '单位（元/个/%/小时）',
    data_flag     varchar(20) DEFAULT 'real' COMMENT '数据标记（real实测/estimate估算，如 DMS_OEE）',
    snapshot_time datetime    DEFAULT NULL COMMENT '快照时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_metric (metric_code, metric_date, dept_id),
    KEY idx_metric_date (metric_date),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='决策支持-指标日快照';

-- 4. 指标对账日志表（F1.5：每日快照后比对实时值与快照值）
DROP TABLE IF EXISTS bi_metric_check_log;
CREATE TABLE bi_metric_check_log (
    id             bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    metric_code    varchar(50) NOT NULL COMMENT '指标编码',
    check_date     date        NOT NULL COMMENT '对账日期',
    dept_id        bigint      DEFAULT NULL COMMENT '部门维度',
    snapshot_value decimal(18,4) DEFAULT NULL COMMENT '快照值',
    realtime_value decimal(18,4) DEFAULT NULL COMMENT '实时值',
    diff_value     decimal(18,4) DEFAULT NULL COMMENT '差异值',
    check_status   char(1)     NOT NULL DEFAULT '0' COMMENT '对账结果（0一致 1不一致）',
    create_time    datetime    DEFAULT NULL COMMENT '记录时间',
    PRIMARY KEY (id),
    KEY idx_check_date (check_date),
    KEY idx_metric_code (metric_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='决策支持-指标对账日志';

-- =============================================
-- 二、指标字典初始数据（22个，口径依据 PRD v1.2 §7 定稿）
-- 使用 WHERE NOT EXISTS 避免重复执行插入
-- =============================================

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MK_SIG_AMT','月签约额','sales','当月生效合同金额合计','MkStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MK_SIG_AMT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MK_RCPT_AMT','月回款额','sales','当月回款登记金额合计（mk_payment_plan 实际回款金额）','MkStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MK_RCPT_AMT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MK_WIN_RATE','商机赢单率','sales','赢单商机数 / 关闭商机数','MkStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MK_WIN_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MK_LEAD_CVR','线索转化率','sales','成交线索 / 线索总数','MkStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MK_LEAD_CVR');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'BID_WIN_RATE','中标率','bid','中标项目 / 参与项目','BidStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='BID_WIN_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MMS_WO_RATE','工单完成率','production','已完工工单 / 应完工工单（扣除取消）','MmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MMS_WO_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MMS_EXCEPTION','生产异常单数','production','当日新增异常工单数','MmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MMS_EXCEPTION');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'QMS_PASS_RATE','一次检验合格率','quality','一次合格数 / 已完成检验数（复用 QMS 看板 passRate 口径）','QmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='QMS_PASS_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'QMS_NCR_OPEN','未关闭NCR数','quality','状态≠关闭的 NCR 数量','QmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='QMS_NCR_OPEN');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'QMS_CAPA_RATE','CAPA按期关闭率','quality','按期关闭的 CAPA / 应关闭 CAPA（复用 QMS 看板 capaCloseRate 口径）','QmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='QMS_CAPA_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'PMS_ONTIME_RATE','到货及时率','purchase','按合同交期判定：按期到货订单行 / 总订单行（订单行粒度）【已确认 v1】','PmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='PMS_ONTIME_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'PMS_TOP_SUP','供应商集中度','purchase','Top5 供应商采购额占比','PmsStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='PMS_TOP_SUP');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'WMS_TURNOVER','库存周转率','inventory','出库成本（按入库单价取价）/ 平均库存金额【已确认 v1】','WmsStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='WMS_TURNOVER');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'WMS_ALERT_CNT','库存预警数','inventory','低库存+积压+临期+过期预警数量合计（复用 WMS 统一预警模型）','WmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='WMS_ALERT_CNT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'WMS_SLOW_AMT','呆滞库存金额','inventory','超 90 天无出库记录的库存金额（不含委外/在制）【已确认 v1】','WmsStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='WMS_SLOW_AMT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'DMS_OEE','设备综合效率','equipment','估算口径（日运行16h假设），data_flag=estimate，接入实测采集后切换【已确认 v1：接受估算上线】。数据源 mms_oee_snapshot 表归属 MMS 模块，由 MmsStatsProvider 提供','MmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='DMS_OEE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'DMS_DOWN_HRS','故障停机时长','equipment','当月故障工单停机时长合计','DmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='DMS_DOWN_HRS');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'SAFETY_RECT_RATE','隐患整改率','safety','已整改隐患 / 累计隐患','SafetyStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='SAFETY_RECT_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'HR_HEADCOUNT','在册人数','hr','在职状态员工数','HrStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='HR_HEADCOUNT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'HR_LEAVE_RATE','月离职率','hr','当月离职人数 / 期初在职人数','HrStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='HR_LEAVE_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'HR_COST_RATIO','人工成本占比','hr','当月人工成本 / 当月完工入库产值（MMS）【已确认 v1】','HrStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='HR_COST_RATIO');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'OTC_CYCLE','现金周期','synthesis','合同签订到全额回清的平均天数，未回清合同不纳入；回款取 mk_payment_plan 实际回款【已确认 v1】','MkStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='OTC_CYCLE');

-- =============================================
-- 三、菜单权限（规范 3.4：M/C/F 三级，权限标识 bi:*）
-- =============================================

-- 一级菜单（M=目录）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, icon, create_by, create_time)
VALUES ('决策支持', 0, 60, '/bi', NULL, '', 1, 0, 'M', '0', '0', 'chart', 'admin', sysdate());

SET @biId = LAST_INSERT_ID();

-- 二级菜单（C=菜单）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES ('决策驾驶舱', @biId, 1, 'dashboard', 'bi/dashboard/index', 'BiDashboard',
    1, 0, 'C', '0', '0', 'bi:dashboard:view', 'dashboard', 'admin', sysdate());

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES ('指标字典', @biId, 2, 'metric', 'bi/metric/index', 'BiMetric',
    1, 0, 'C', '0', '0', 'bi:metric:list', 'dict', 'admin', sysdate());

-- 三级按钮权限（F=按钮）
SET @metricMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('指标查询', @metricMenuId, 1, '', 'F', '0', '0', 'bi:metric:query',  'admin', sysdate()),
('指标新增', @metricMenuId, 2, '', 'F', '0', '0', 'bi:metric:add',    'admin', sysdate()),
('指标修改', @metricMenuId, 3, '', 'F', '0', '0', 'bi:metric:edit',   'admin', sysdate()),
('指标删除', @metricMenuId, 4, '', 'F', '0', '0', 'bi:metric:remove', 'admin', sysdate()),
('指标导出', @metricMenuId, 5, '', 'F', '0', '0', 'bi:metric:export', 'admin', sysdate()),
('对账查询', @metricMenuId, 6, '', 'F', '0', '0', 'bi:metric:check',  'admin', sysdate());

-- 给 admin 角色分配权限（含无 perms 的一级目录 M：RuoYi 菜单树按祖先过滤，
-- 一级目录未授权时其下所有 C 菜单都不会出现在侧边栏）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE (perms LIKE 'bi:%' OR menu_id = @biId)
  AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);

-- =============================================
-- 四、Quartz 定时任务（阶段一开发完成后在「系统监控-定时任务」界面配置）
-- 调用目标：biMetricSnapshotTask.snapshotMetrics   cron 建议：0 0 1 * * ?（每日 01:00）
-- 调用目标：biMetricSnapshotTask.checkMetrics      cron 建议：0 30 1 * * ?（每日 01:30 对账）
-- 幂等说明：任务内部同日重跑先删后插，可安全重复执行
-- =============================================

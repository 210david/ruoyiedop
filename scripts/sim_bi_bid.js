/**
 * BI 指标 + 招投标补全
 * BI：指标阈值/责任人/钻取配置、每日指标快照、指标预警、快照一致性核查、指标字典变更日志
 * 招投标：采集源、关键词、关键词可见范围、采集日志、招标公告、投标项目、项目节点、保证金、企业资质
 * 依据：真实生产/质量/采购/营销/财务数据形成指标序列
 */
const mysql = require('mysql2/promise');
const pad = (n, w = 3) => String(n).padStart(w, '0');
const fmt = d => {
  const p = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const dstr = d => fmt(d).slice(0, 10);
const addDays = (d, n) => new Date(new Date(d).getTime() + n * 86400000);
const MONEY = v => Math.round(Number(v) * 100) / 100;
// 稳定伪随机（同一指标同一天结果一致）
const rnd = (s, i) => {
  const x = Math.sin(s * 12.9898 + i * 78.233) * 43758.5453;
  return x - Math.floor(x);
};

(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  // ============================================================
  // 一、BI 指标
  // ============================================================
  for (const t of ['bi_metric_snapshot', 'bi_metric_alert', 'bi_metric_check_log', 'bi_metric_dict_log']) {
    await exec(`DELETE FROM \`${t}\``);
    await exec(`ALTER TABLE \`${t}\` AUTO_INCREMENT=1`);
  }

  // 指标配置：code → [单位, 基线, 波动, 小数, 方向, 黄线, 红线, 责任人, 钻取页面]
  const CFG = {
    BID_WIN_RATE: ['%', 33.5, 7, 2, 'up', 30, 25, '林晓', '/bid/project'],
    DMS_DOWN_HRS: ['h', 4.4, 1.4, 2, 'down', 6, 8, '陈志强', '/equipment/downtime'],
    DMS_OEE: ['%', 82.0, 3.2, 2, 'up', 80, 76, '陈志强', '/production/oee'],
    HR_COST_RATIO: ['%', 12.6, 0.8, 2, 'down', 13.5, 14.5, '罗丽波', '/hr/attendance'],
    HR_HEADCOUNT: ['人', 186, 2, 0, 'up', 180, 172, '罗丽波', '/hr/employee'],
    HR_LEAVE_RATE: ['%', 3.1, 1.2, 2, 'down', 4.5, 6, '罗丽波', '/hr/employee'],
    MK_LEAD_CVR: ['%', 18.6, 3.4, 2, 'up', 16, 13, '林晓', '/marketing/lead'],
    MK_RCPT_AMT: ['元', 486000, 90000, 2, 'up', 380000, 300000, '林晓', '/marketing/payment'],
    MK_SIG_AMT: ['元', 528000, 120000, 2, 'up', 400000, 320000, '林晓', '/marketing/order'],
    MK_WIN_RATE: ['%', 42.5, 6, 2, 'up', 38, 33, '林晓', '/marketing/opportunity'],
    MMS_EXCEPTION: ['单', 1.8, 1.6, 0, 'down', 4, 6, '周强', '/production/abnormal'],
    MMS_WO_RATE: ['%', 92.4, 3.6, 2, 'up', 90, 86, '周强', '/production/workorder'],
    OTC_CYCLE: ['天', 57.5, 5.5, 1, 'down', 65, 72, '张敏', '/finance/report'],
    PMS_ONTIME_RATE: ['%', 93.8, 2.8, 2, 'up', 92, 88, '李晨', '/purchase/order'],
    PMS_TOP_SUP: ['%', 29.4, 3.0, 2, 'down', 35, 40, '李晨', '/purchase/order'],
    QMS_CAPA_RATE: ['%', 88.5, 5.5, 2, 'up', 85, 80, '刘敏', '/quality/capa'],
    QMS_NCR_OPEN: ['单', 4.6, 2.0, 0, 'down', 7, 9, '刘敏', '/quality/ncr'],
    QMS_PASS_RATE: ['%', 98.2, 0.7, 2, 'up', 97.5, 96.5, '刘敏', '/quality/inspection'],
    SAFETY_RECT_RATE: ['%', 96.2, 2.6, 2, 'up', 94, 90, '周强', '/safety/hazard'],
    WMS_ALERT_CNT: ['项', 7.5, 3.2, 0, 'down', 12, 16, '宋春静', '/wms/stock'],
    WMS_SLOW_AMT: ['元', 86000, 12000, 2, 'down', 100000, 120000, '宋春静', '/wms/stock'],
    WMS_TURNOVER: ['次', 3.42, 0.28, 2, 'up', 3.1, 2.8, '宋春静', '/wms/stock']
  };
  const metricRows = await q(`SELECT id, metric_code, freq FROM bi_metric_dict WHERE del_flag='0' ORDER BY metric_code`);
  let cfgCnt = 0;
  for (const m of metricRows) {
    const c = CFG[m.metric_code];
    if (!c) continue;
    await exec(`UPDATE bi_metric_dict SET threshold_yellow=?, threshold_red=?, threshold_dir=?, owner=?,
        drill_url=?, notify_roles=?, update_by='admin', update_time=NOW(), remark=?
      WHERE id=?`,
      [c[5], c[6], c[4] === 'up' ? 'lt' : 'gt', c[7], c[8], '生产经理,质量经理,采购经理,销售总监', `${m.freq === 'day' ? '每日' : '每月'}快照，${c[4] === 'up' ? '低于黄线预警、低于红线告警' : '高于黄线预警、高于红线告警'}`, m.id]);
    cfgCnt++;
  }
  console.log(`BI 指标配置更新 ${cfgCnt} 个`);

  // 每日快照 30 天
  const DAYS = 30;
  const start = '2026-10-18';
  let snapCnt = 0;
  const series = {};   // code → [{date, value}]
  for (const [ci, m] of metricRows.entries()) {
    const c = CFG[m.metric_code];
    if (!c) continue;
    series[m.metric_code] = [];
    for (let d = 0; d < DAYS; d++) {
      const day = dstr(addDays(start, d));
      const dow = addDays(start, d).getDay();
      let v = c[1] + (rnd(ci + 1, d) - 0.5) * c[2] * 2;
      if (m.freq === 'month') v = v * (0.97 + d / DAYS * 0.06);       // 月度指标缓升
      if (dow === 0 || dow === 6) v = v * (m.metric_code.startsWith('MMS') || m.metric_code.startsWith('DMS') ? 0.55 : 0.7);
      // 末日区间刻意制造「近期恶化」，让驾驶舱的预警横幅与预警清单有真实来源
      if (d >= DAYS - 3) {
        if (m.metric_code === 'QMS_NCR_OPEN') v += 4;
        if (m.metric_code === 'WMS_ALERT_CNT') v += 7;
        if (m.metric_code === 'DMS_DOWN_HRS') v += 2.4;
        if (m.metric_code === 'MK_RCPT_AMT') v = v * 0.72;
        if (m.metric_code === 'QMS_CAPA_RATE') v -= 5.5;
      }
      v = Number(v.toFixed(c[3]));
      await exec(`INSERT INTO bi_metric_snapshot(metric_code,metric_date,dept_id,metric_value,unit,data_flag,snapshot_time)
        VALUES(?,?,0,?,?, 'real',?)`, [m.metric_code, day, v, m[0], `${day} 01:05:00`]);
      series[m.metric_code].push({ date: day, value: v });
      snapCnt++;
    }
  }
  console.log(`BI 指标快照 ${snapCnt} 行`);

  // 指标预警
  const alertDefs = [];
  for (const m of metricRows) {
    const c = CFG[m.metric_code];
    if (!c || !series[m.metric_code]) continue;
    for (const p of series[m.metric_code]) {
      const breach = c[4] === 'up' ? p.value < c[5] : p.value > c[5];
      if (!breach) continue;
      const red = c[4] === 'up' ? p.value < c[6] : p.value > c[6];
      alertDefs.push({ code: m.metric_code, name: null, date: p.date, value: p.value, threshold: red ? c[6] : c[5], red, dir: c[4], unit: c[0] });
    }
  }
  // 优先保留最近日期的预警，保证看板能看到当日告警
  alertDefs.sort((a, b) => (a.date === b.date ? (a.code < b.code ? -1 : 1) : (a.date < b.date ? 1 : -1)));
  const picked = alertDefs.slice(0, 26);
  const nameOf = {};
  for (const r of await q(`SELECT metric_code, metric_name FROM bi_metric_dict`)) nameOf[r.metric_code] = r.metric_name;
  for (const [i, a] of picked.entries()) {
    const msg = a.dir === 'up'
      ? `${nameOf[a.code]} ${a.value}${a.unit} 低于${a.red ? '红色' : '黄色'}阈值 ${a.threshold}${a.unit}，请关注`
      : `${nameOf[a.code]} ${a.value}${a.unit} 高于${a.red ? '红色' : '黄色'}阈值 ${a.threshold}${a.unit}，请关注`;
    const pushed = i < 20;
    const recovered = a.red ? 0 : (i % 3 === 0 ? 1 : 0);
    await exec(`INSERT INTO bi_metric_alert(metric_code,metric_date,dept_id,alert_level,metric_value,threshold_value,
        alert_message,push_status,push_time,push_count,push_remark,recover_time,status,create_by,create_time)
      VALUES(?,?,0,?,?,?,?,?,?,?,?,?,?, 'system',?)`,
      [a.code, a.date, a.red ? '1' : '2', a.value, a.threshold, msg,
        pushed ? '1' : '0', pushed ? `${a.date} 01:12:00` : null, pushed ? 1 + (i % 3) : 0,
        pushed ? `已推送至企业微信「经营日报」群，接收人：${CFG[a.code][7]}` : '待下一轮推送窗口',
        recovered ? `${dstr(addDays(a.date, 2))} 01:08:00` : null,
        recovered ? '1' : '0', `${a.date} 01:05:30`]);
  }
  console.log(`BI 指标预警 ${picked.length} 条（红 ${picked.filter(x => x.red).length} / 黄 ${picked.filter(x => !x.red).length}）`);

  // 快照一致性核查
  const checkCodes = ['MMS_WO_RATE', 'QMS_PASS_RATE', 'DMS_OEE', 'WMS_TURNOVER', 'PMS_ONTIME_RATE', 'MK_SIG_AMT'];
  let chkCnt = 0;
  for (const [ci, code] of checkCodes.entries()) {
    const c = CFG[code];
    for (let d = DAYS - 15; d < DAYS; d++) {
      const s = series[code] && series[code][d];
      if (!s) continue;
      const drift = (rnd(ci * 31 + 7, d) - 0.5) * c[2] * 0.25;
      const real = Number((s.value + drift).toFixed(c[3]));
      const diff = Number((real - s.value).toFixed(c[3]));
      const tol = c[3] === 0 ? 1 : Number((c[2] * 0.1).toFixed(2));
      const bad = Math.abs(diff) > tol;
      await exec(`INSERT INTO bi_metric_check_log(metric_code,check_date,dept_id,snapshot_value,realtime_value,diff_value,
          check_status,create_time)
        VALUES(?,?,0,?,?,?,?,?)`,
        [code, s.date, s.value, real, diff, bad ? '1' : '0', `${s.date} 02:10:00`]);
      chkCnt++;
    }
  }
  console.log(`快照一致性核查 ${chkCnt} 条`);

  // 指标字典变更日志
  const dictLogs = [
    ['BID_WIN_RATE', 'threshold', 'threshold_yellow', 'null', '30.00', '2026-09-28 10:12:00'],
    ['BID_WIN_RATE', 'threshold', 'threshold_red', 'null', '25.00', '2026-09-28 10:12:00'],
    ['QMS_PASS_RATE', 'threshold', 'threshold_yellow', '97.00', '97.50', '2026-10-09 09:30:00'],
    ['QMS_PASS_RATE', 'caliber', 'caliber_desc', '检验合格批次数/检验总批次数', '一次检验合格批次数/检验总批次数（不含返工复检）', '2026-10-09 09:35:00'],
    ['WMS_TURNOVER', 'freq', 'freq', 'day', 'month', '2026-10-12 14:20:00'],
    ['PMS_TOP_SUP', 'threshold', 'threshold_red', '45.00', '40.00', '2026-10-15 11:05:00'],
    ['DMS_OEE', 'owner', 'owner', '周强', '陈志强', '2026-10-20 16:40:00'],
    ['MK_SIG_AMT', 'caliber', 'calc_ref', 'MkStatsProvider', 'MkStatsProvider(含税口径)', '2026-10-22 08:50:00'],
    ['OTC_CYCLE', 'drill', 'drill_url', 'null', '/finance/report', '2026-10-26 15:10:00'],
    ['SAFETY_RECT_RATE', 'status', 'status', '1', '0', '2026-11-02 09:00:00'],
    ['MMS_EXCEPTION', 'threshold', 'threshold_yellow', '5', '4', '2026-11-06 10:30:00'],
    ['HR_LEAVE_RATE', 'notify', 'notify_roles', '人力资源经理', '人力资源经理,总经理', '2026-11-10 17:20:00']
  ];
  for (const l of dictLogs) {
    await exec(`INSERT INTO bi_metric_dict_log(metric_code,change_type,field_name,old_value,new_value,change_by,change_time)
      VALUES(?,?,?,?,?,?,?)`, [l[0], l[1], l[2], l[3], l[4], 'admin', l[5]]);
  }
  console.log(`指标字典变更 ${dictLogs.length} 条`);

  // ============================================================
  // 二、招投标
  // ============================================================
  for (const t of ['bid_crawl_source', 'bid_crawl_keyword', 'bid_keyword_scope', 'bid_crawl_log',
    'bid_notice', 'bid_project', 'bid_project_node', 'bid_deposit', 'bid_qualification']) {
    await exec(`DELETE FROM \`${t}\``);
    await exec(`ALTER TABLE \`${t}\` AUTO_INCREMENT=1`);
  }

  // 2.1 采集源
  const sources = [
    ['中国政府采购网', '1', 'https://www.ccgp.gov.cn', 'ccgp_list', 60],
    ['云南省公共资源交易中心', '1', 'https://www.ynzbw.com', 'ynzbw_list', 90],
    ['中国招标投标公共服务平台', '2', 'https://www.cebpubservice.com/api/notice', 'ceb_api', 120],
    ['全国公共资源交易平台', '2', 'https://deal.ggzy.gov.cn/api/v1/notice', 'ggzy_api', 120],
    ['采招网', '1', 'https://www.bidcenter.com.cn/list', 'bidcenter_list', 180],
    ['云南省农业农村厅采购公告', '3', 'https://nynct.yn.gov.cn/rss/notice.xml', 'ynrss', 240]
  ];
  const srcIds = [];
  for (const [i, s] of sources.entries()) {
    const ins = await exec(`INSERT INTO bid_crawl_source(source_name,source_type,base_url,parser_code,parse_config,
        crawl_interval,enabled,last_run_time,last_run_status,last_run_msg,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?, '1',?,?,?, '0','admin',?,?)`,
      [s[0], s[1], s[2], s[3],
        JSON.stringify({ list: '#list .item', title: '.title a', publishTime: '.date', url: '.title a@href', encoding: 'utf-8' }),
        s[4], '2026-11-16 08:00:00', i === 4 ? '1' : '0',
        i === 4 ? '部分条目缺少正文页，已跳过 3 条' : '采集成功，入库正常',
        fmt(new Date(2026, 10, 16, 8, 0, 0)), i === 4 ? '反爬限流，已降低采集频率' : '官方公告源']);
    srcIds.push({ id: ins.insertId, name: s[0] });
  }
  console.log(`招标采集源 ${srcIds.length} 个`);

  // 2.2 关键词与可见范围
  const keywords = [
    ['腊肉', '腌肉,腊味,烟熏肉', '腊肉炒饭调料', '2', '530000', '7', 200000],
    ['香肠', '腊肠,广味香肠,低温香肠', '香肠机', '2', '530000', '7', 150000],
    ['腌腊制品', '腌腊肉制品,风干肉', '腌制设备', '2', '530000', '7', 100000],
    ['食堂食材', '食材配送,食堂物资,副食品', '食堂设备', '2', '530000', '7', 300000],
    ['肉制品', '熟肉制品,酱卤肉,猪肉制品', '肉制品机械', '2', '530000', '7', 200000],
    ['冷链配送', '冷链运输,冷藏配送', '冷库建设', '1', '530000', '7', 200000],
    ['预制菜', '预制菜肴,方便菜,调理肉制品', '预制菜设备', '2', '530000', '7', 150000],
    ['年货礼盒', '春节礼盒,福利礼包,慰问品', '包装盒印刷', '2', '530000', '7', 100000],
    ['猪肉分割', '分割肉,白条猪,生鲜猪肉', '分割设备', '2', '530000', '7', 300000],
    ['职工福利', '节日慰问,工会福利,福利采购', '福利彩票', '1', '', '7', 80000]
  ];
  const kwIds = [];
  for (const [i, k] of keywords.entries()) {
    const ins = await exec(`INSERT INTO bid_crawl_keyword(keyword,synonym_words,exclude_words,match_type,region_code,industry,
        min_amount,notice_types,notify_enabled,enabled,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?, '1','1','0','admin',?,?)`,
      [k[0], k[1], k[2], k[3], k[4], k[5], k[6], '1,2,3,5', fmt(new Date(2026, 8, 20 + i, 9, 30, 0)),
        `命中「${k[0]}」类公告，自动推送至招投标专员`]);
    kwIds.push(ins.insertId);
  }
  let scopeCnt = 0;
  for (const [i, kid] of kwIds.entries()) {
    await exec(`INSERT INTO bid_keyword_scope(keyword_id,scope_type,dept_id,user_id) VALUES(?,?,?,?)`, [kid, '1', 103, null]);
    scopeCnt++;
    if (i % 2 === 0) {
      await exec(`INSERT INTO bid_keyword_scope(keyword_id,scope_type,dept_id,user_id) VALUES(?,?,?,?)`, [kid, '2', null, 7]);
      scopeCnt++;
    }
  }
  console.log(`招标关键词 ${kwIds.length} 个 / 可见范围 ${scopeCnt} 条`);

  // 2.3 采集日志
  let crawlCnt = 0;
  for (let d = 0; d < 12; d++) {
    for (const [si, s] of srcIds.entries()) {
      if (d % 2 === 1 && si > 3) continue;
      const st = addDays('2026-11-05', d);
      const begin = fmt(new Date(st.getFullYear(), st.getMonth(), st.getDate(), 7, 30, 0));
      const fetched = 40 + Math.round(rnd(si + 3, d) * 160);
      const inserted = Math.round(fetched * (0.6 + rnd(si + 9, d) * 0.35));
      const matched = Math.round(inserted * (0.12 + rnd(si + 15, d) * 0.2));
      const fail = si === 4 && d % 3 === 0;
      await exec(`INSERT INTO bid_crawl_log(source_id,source_name,run_status,fetched_count,inserted_count,matched_count,
          error_msg,start_time,end_time)
        VALUES(?,?,?,?,?,?,?,?,?)`,
        [s.id, s.name, fail ? '2' : '0', fail ? 0 : fetched, fail ? 0 : inserted, fail ? 0 : matched,
          fail ? 'HTTP 429 Too Many Requests：目标站点触发访问频率限制，已自动降频重试' : null,
          begin, fmt(new Date(new Date(begin).getTime() + (fail ? 3500 : 12000 + fetched * 45)))]);
      crawlCnt++;
    }
  }
  console.log(`采集日志 ${crawlCnt} 条`);

  // 2.4 招标公告
  const noticeDefs = [
    ['云南省曲靖市第一中学食堂食材（肉制品）采购项目招标公告', '2', '云南省曲靖市第一中学', '云南招标股份有限公司', 3200000, '2026-09-08', '2026-09-28', '2026-09-29', '香肠,肉制品'],
    ['宣威市人民政府机关食堂食材配送服务采购公告', '2', '宣威市机关事务管理局', '宣威市政府采购中心', 1850000, '2026-09-15', '2026-10-08', '2026-10-09', '食堂食材,肉制品'],
    ['中国邮政集团云南省分公司 2027 年职工福利腊味礼盒采购', '2', '中国邮政集团云南省分公司', '云南中招招标有限公司', 960000, '2026-09-22', '2026-10-12', '2026-10-13', '年货礼盒,腊肉'],
    ['昆明铁路局客运段餐料（腌腊制品）年度采购招标公告', '2', '中国铁路昆明局集团有限公司', '昆明局物资采购所', 2400000, '2026-10-06', '2026-10-26', '2026-10-27', '腌腊制品,香肠'],
    ['云南能投集团职工食堂食材年度供应商入围项目', '2', '云南省能源投资集团有限公司', '云南能投招标中心', 4200000, '2026-10-10', '2026-10-30', '2026-10-31', '食堂食材,冷链配送'],
    ['云南省农村信用社联合社春节慰问品采购公告', '2', '云南省农村信用社联合社', '云南国合招标有限公司', 1500000, '2026-10-18', '2026-11-07', '2026-11-08', '年货礼盒,职工福利'],
    ['曲靖市第一人民医院食堂食材配送服务（二次）招标公告', '2', '曲靖市第一人民医院', '曲靖市政府采购中心', 2100000, '2026-10-20', '2026-11-10', '2026-11-11', '食堂食材'],
    ['云南省监狱管理局服刑人员食堂物资采购项目', '2', '云南省监狱管理局', '云南新中招标有限公司', 3800000, '2026-10-24', '2026-11-14', '2026-11-15', '食堂食材,猪肉分割'],
    ['昆明市呈贡区中小学食堂大宗食材集中采购', '2', '昆明市呈贡区教育体育局', '昆明晨晟招标有限责任公司', 5600000, '2026-10-27', '2026-11-17', '2026-11-18', '食堂食材,肉制品'],
    ['大理州文旅集团酒店群食材（肉制品）供应商招标', '2', '大理州文化旅游投资集团', '大理公共资源交易中心', 1250000, '2026-10-29', '2026-11-19', '2026-11-20', '肉制品,冷链配送'],
    ['云南白药集团食堂食材配送服务采购公告', '2', '云南白药集团股份有限公司', '云南白药采购中心', 1680000, '2026-11-02', '2026-11-22', '2026-11-23', '食堂食材,冷链配送'],
    ['昭通市第一中学食堂食材采购（肉制品包）', '2', '昭通市第一中学', '昭通市政府采购中心', 980000, '2026-11-04', '2026-11-24', '2026-11-25', '肉制品,香肠'],
    ['云南省烟草公司曲靖市公司职工食堂食材采购', '2', '云南省烟草公司曲靖市公司', '云南烟草招标中心', 1350000, '2026-11-06', '2026-11-26', '2026-11-27', '食堂食材'],
    ['中国移动云南公司 2027 年春节福利礼包采购', '2', '中国移动通信集团云南有限公司', '云南移动采购共享中心', 2800000, '2026-11-09', '2026-11-29', '2026-11-30', '职工福利,年货礼盒'],
    ['红河州机关食堂食材集中配送服务招标', '2', '红河州机关事务管理局', '红河州公共资源交易中心', 1600000, '2026-11-11', '2026-12-01', '2026-12-02', '食堂食材,冷链配送'],
    ['云南建投集团项目部食堂食材年度采购', '2', '云南省建设投资控股集团', '云南建投招标有限公司', 2200000, '2026-11-13', '2026-12-03', '2026-12-04', '食堂食材,猪肉分割'],
    ['曲靖市第一中学食堂食材采购中标结果公示', '6', '云南省曲靖市第一中学', '云南招标股份有限公司', 3200000, '2026-10-12', '2026-10-12', '2026-10-12', '香肠,肉制品'],
    ['宣威市机关食堂食材配送服务中标候选人公示', '5', '宣威市机关事务管理局', '宣威市政府采购中心', 1850000, '2026-10-20', '2026-10-20', '2026-10-20', '食堂食材'],
    ['昆明铁路局餐料采购变更公告（延后开标）', '3', '中国铁路昆明局集团有限公司', '昆明局物资采购所', 2400000, '2026-10-18', '2026-11-02', '2026-11-03', '腌腊制品'],
    ['云南省农村信用社慰问品采购答疑澄清公告', '4', '云南省农村信用社联合社', '云南国合招标有限公司', 1500000, '2026-10-25', '2026-11-07', '2026-11-08', '年货礼盒'],
    ['某单位食堂食材采购项目废标公告', '7', '曲靖市第二人民医院', '曲靖市政府采购中心', 900000, '2026-09-28', '2026-09-28', '2026-09-28', '食堂食材'],
    ['云南能投集团食材供应商入围项目中标结果公告', '6', '云南省能源投资集团有限公司', '云南能投招标中心', 4200000, '2026-11-05', '2026-11-05', '2026-11-05', '食堂食材']
  ];
  const noticeIds = [];
  for (const [i, n] of noticeDefs.entries()) {
    const src = srcIds[i % srcIds.length];
    const ins = await exec(`INSERT INTO bid_notice(project_key,title,notice_type,source_id,source_name,tenderee,agent,region_code,
        industry,budget_amount,publish_time,deadline_time,open_time,contact_person,contact_phone,source_url,content,
        attachment_url,matched_keywords,ai_extracted,input_type,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '1','0','0','system',?,?)`,
      [`YN-${n[5].replace(/-/g, '')}-${pad(i + 1)}`, n[0], n[1], src.id, src.name, n[2], n[3],
        '530000', '7', n[4], `${n[5]} 09:00:00`, `${n[6]} 17:00:00`, `${n[7]} 09:30:00`,
        ['张工', '李工', '王工'][i % 3], `0871-6${(3100000 + i * 137).toString().slice(0, 6)}`,
        `${src.name.includes('政府采购') ? 'https://www.ccgp.gov.cn' : 'https://www.ynzbw.com'}/notice/${20260000 + i}.html`,
        `一、项目基本情况\n项目编号：YN-${2026}-${pad(i + 1)}\n预算金额：${(n[4] / 10000).toFixed(2)} 万元\n采购需求：${n[0].replace('招标公告', '').replace('采购公告', '')}，含供货、包装、冷链运输及售后。\n二、申请人资格要求\n1. 具备独立法人资格及食品生产/经营许可证；\n2. 近三年同类业绩不少于 2 项；\n3. 未被列入失信被执行人名单。\n三、获取招标文件\n时间：公告发布之日起 5 个工作日内。`,
        `/profile/bid/notice/${20260000 + i}.pdf`, n[8], fmt(new Date(n[5] + 'T09:12:00')),
        n[1] === '2' ? '关键词命中，已生成投标线索' : '结果类公告，用于更新投标项目状态']);
    noticeIds.push({ id: ins.insertId, def: n });
  }
  console.log(`招标公告 ${noticeIds.length} 条`);

  // 2.5 投标项目
  const projectDefs = [
    // noticeIndex, status, join_type, partner, bid_amount系数, owner
    [0, '7', '1', null, 0.92, '林晓'],
    [1, '8', '1', null, 0.95, '林晓'],
    [2, '7', '1', null, 0.90, '周强'],
    [3, '6', '2', '宣威市火腿产业园运营有限公司', 0.93, '林晓'],
    [4, '5', '2', '云南冷链物流集团有限公司', 0.91, '周强'],
    [5, '4', '1', null, 0.94, '林晓'],
    [7, '5', '1', null, 0.96, '周强'],
    [8, '3', '1', null, 0.92, '林晓'],
    [9, '2', '2', '大理州食品公司', 0.95, '周强'],
    [10, '2', '1', null, 0.93, '林晓'],
    [12, '1', '1', null, 1.0, '周强'],
    [13, '10', '1', null, 1.0, '林晓'],
    [14, '2', '1', null, 0.94, '周强'],
    [15, '1', '2', '云南建投物业', 0.97, '林晓']
  ];
  const statusTime = {
    '1': '2026-09-08', '2': '2026-09-12', '3': '2026-09-18', '4': '2026-09-25', '5': '2026-10-08',
    '6': '2026-10-12', '7': '2026-10-14', '8': '2026-10-14', '9': '2026-10-01', '10': '2026-09-20'
  };
  let projCnt = 0, nodeCnt = 0, depCnt = 0;
  const projects = [];
  for (const [i, p] of projectDefs.entries()) {
    const n = noticeIds[p[0]];
    if (!n) continue;
    const d = n.def;
    const budget = d[4];
    const bidAmount = MONEY(budget * p[4]);
    const st = p[1];
    const resultTime = ['6', '7', '8', '9'].includes(st) ? `${d[7]} 15:00:00` : null;
    const failReason = st === '8' ? ['2', '4', '1'][i % 3] : (st === '9' ? '5' : null);
    const ins = await exec(`INSERT INTO bid_project(bid_no,notice_id,project_name,tenderee,agent,region_code,industry,
        budget_amount,bid_amount,status,join_type,partner,apply_deadline,submit_deadline,open_time,result_time,
        fail_reason,owner_user_id,owner_name,dept_id,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,103,'0','admin',?,?)`,
      [`TB2026${pad(projCnt + 1)}`, n.id, d[0].replace('中标结果公示', '').replace('中标候选人公示', ''),
        d[2], d[3], '530000', '7', budget, bidAmount, st, p[2], p[3],
        `${d[6]} 17:00:00`, `${d[6]} 09:30:00`, `${d[7]} 09:30:00`, resultTime, failReason,
        7, p[5], fmt(new Date(d[5] + 'T10:00:00')),
        st === '7' ? '中标，已进入合同签订与履约保证金办理' : (st === '8' ? '未中标，已复盘失分项' : (st === '10' ? '商务条款不满足（账期要求 90 天），主动放弃' : '投标推进中'))]);
    projects.push({ id: ins.insertId, bid_no: `TB2026${pad(projCnt + 1)}`, status: st, budget, bidAmount, notice: d, start: d[5], open: d[7], owner: p[5] });
    projCnt++;

    // 节点
    const flow = [
      ['1', '报名', -2], ['2', '购标', 0], ['3', '踏勘', 3], ['4', '标书编制', 6],
      ['6', '递交', null], ['7', '开标', null], ['8', '结果确认', null]
    ];
    const doneUpTo = { '1': 0, '2': 1, '3': 2, '4': 4, '5': 5, '6': 6, '7': 7, '8': 7, '9': 6, '10': 1 }[st] ?? 0;
    for (const [ni, f] of flow.entries()) {
      const plan = f[2] === null
        ? (ni === 4 ? `${d[6].slice(0, 10)} 17:00:00` : (ni === 5 ? `${d[7]} 09:30:00` : `${d[7]} 16:00:00`))
        : `${dstr(addDays(d[5], f[2]))} 17:00:00`;
      const done = ni < doneUpTo;
      await exec(`INSERT INTO bid_project_node(project_id,node_type,node_name,plan_time,actual_time,handler_id,handler_name,
          block_desc,finish_flag,sort_order,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,?, '0','admin',?,?)`,
        [ins.insertId, f[0], f[1], plan, done ? fmt(new Date(new Date(plan).getTime() - 3600000)) : null,
          7, p[5],
          (!done && ni === doneUpTo && st === '4' && ni === 4) ? '技术方案中冷链温控章节待补充，需质量部确认' : null,
          done ? '1' : '0', ni + 1, fmt(new Date(d[5] + 'T10:05:00')), done ? '已完成' : '待办理']);
      nodeCnt++;
    }

    // 保证金
    if (!['1', '10'].includes(st)) {
      const depAmt = MONEY(budget * 0.015);
      const refunded = ['8', '9'].includes(st);
      await exec(`INSERT INTO bid_deposit(project_id,bid_no,project_name,deposit_type,pay_type,amount,refund_amount,
          pay_time,expect_refund_time,actual_refund_time,refund_status,voucher_url,dept_id,del_flag,create_by,create_time,remark)
        VALUES(?,?,?, '1',?,?,?,?,?,?,?,?,103,'0','admin',?,?)`,
        [ins.insertId, `TB2026${pad(projCnt)}`, d[0], i % 3 === 0 ? '2' : '1', depAmt, refunded ? depAmt : 0,
          `${dstr(addDays(d[5], 4))} 14:00:00`, `${d[7]} 17:00:00`,
          refunded ? `${dstr(addDays(d[10] || d[7], 15))} 10:00:00` : null,
          refunded ? '1' : '0', `/profile/bid/deposit/TB2026${pad(projCnt)}.pdf`,
          fmt(new Date(d[5] + 'T14:05:00')),
          refunded ? '未中标，保证金已按公告要求退回' : '投标保证金在途，按招标文件要求于中标通知书发出后 5 日内退还']);
      depCnt++;
      if (st === '7') {
        await exec(`INSERT INTO bid_deposit(project_id,bid_no,project_name,deposit_type,pay_type,amount,refund_amount,
            pay_time,expect_refund_time,actual_refund_time,refund_status,voucher_url,dept_id,del_flag,create_by,create_time,remark)
          VALUES(?,?,?, '2',?,?,?,?,?,NULL, '0',?,103,'0','admin',?,?)`,
          [ins.insertId, `TB2026${pad(projCnt)}`, d[0], '3', MONEY(budget * 0.05), 0,
            `${dstr(addDays(d[7], 10))} 11:00:00`, `${dstr(addDays(d[7], 380))} 17:00:00`,
            `/profile/bid/deposit/LY-TB2026${pad(projCnt)}.pdf`,
            fmt(new Date(addDays(d[7], 10))), '履约保证金（电子保函），履约期满无违约后退还']);
        depCnt++;
      }
    }
  }
  console.log(`投标项目 ${projCnt} 个 / 项目节点 ${nodeCnt} 条 / 保证金 ${depCnt} 笔`);

  // 2.6 企业资质
  const quals = [
    ['1', '食品生产许可证（SC）', 'SC10453038100126', '肉制品、腌腊制品', '2023-05-12', '2028-05-11', '自有工厂'],
    ['1', '食品经营许可证', 'JY15303810001234', '预包装食品销售', '2024-03-08', '2029-03-07', '自有工厂'],
    ['1', 'ISO 22000 食品安全管理体系认证', 'CN-ISO22000-2024-0871', '腌腊肉制品生产', '2024-06-18', '2027-06-17', '中食安认证'],
    ['1', 'ISO 9001 质量管理体系认证', 'CN-ISO9001-2024-1123', '肉制品生产与服务', '2024-06-18', '2027-06-17', '中质协认证'],
    ['1', 'HACCP 体系认证', 'CN-HACCP-2024-0455', '腌腊肉制品', '2024-08-20', '2027-08-19', '中食安认证'],
    ['1', '动物防疫条件合格证', 'YN-DWFY-2023-0186', '生猪屠宰、分割', '2023-09-01', '2027-08-31', '宣威市农业农村局'],
    ['1', '排污许可证', '91530381MA6KXXXX0P001Q', '肉制品加工', '2022-11-15', '2027-11-14', '曲靖市生态环境局'],
    ['4', '云南省农业产业化省级重点龙头企业', 'YN-LQ-2025-038', '省级', '2025-12-20', '2028-12-19', '云南省农业农村厅'],
    ['2', '食品安全管理员培训合格证', 'SPGL-2025-0731', '食品安全管理', '2025-04-10', '2028-04-09', '员工 刘敏'],
    ['2', '食品检验员职业资格证（高级）', 'SPJY-2024-1128', '理化与微生物检验', '2024-10-15', '2027-10-14', '员工 何静']
  ];
  for (const [i, qq] of quals.entries()) {
    const expire = qq[5];
    const warn = expire < '2026-12-31' ? 30 : 60;
    await exec(`INSERT INTO bid_qualification(qual_type,name,cert_no,qual_level,owner_type,owner_user_id,owner_name,
        issue_date,expire_date,warn_days,file_url,dept_id,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,103,'0','admin',?,?)`,
      [qq[0], qq[1], qq[2], qq[3], qq[0] === '2' ? '2' : '1', qq[0] === '2' ? 25 + i : null, qq[6],
        qq[4], expire, warn, `/profile/bid/qual/${qq[2].replace(/[^A-Za-z0-9]/g, '')}.pdf`,
        fmt(new Date(qq[4] + 'T09:00:00')),
        expire < '2027-01-01' ? '临近到期，已启动换证流程' : '有效期正常']);
  }
  console.log(`企业资质 ${quals.length} 项`);

  const st = await q(`SELECT 'snapshot' t,COUNT(*) c FROM bi_metric_snapshot UNION ALL
    SELECT 'alert',COUNT(*) FROM bi_metric_alert UNION ALL
    SELECT 'check',COUNT(*) FROM bi_metric_check_log UNION ALL
    SELECT 'notice',COUNT(*) FROM bid_notice UNION ALL
    SELECT 'project',COUNT(*) FROM bid_project UNION ALL
    SELECT 'node',COUNT(*) FROM bid_project_node UNION ALL
    SELECT 'deposit',COUNT(*) FROM bid_deposit`);
  console.table(st);
  const stat = await q(`SELECT status, COUNT(*) c FROM bid_project GROUP BY status ORDER BY status`);
  console.table(stat);
  await conn.end();
})().catch(e => { console.error('ERR', e.message); process.exit(1); });

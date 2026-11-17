// 生产主数据模拟：工厂/班次/班组/产线/工位/资源/工厂日历
const mysql = require('mysql2/promise');

(async () => {
  const conn = await mysql.createConnection({
    host: '127.0.0.1', user: 'root', password: 'ZBrj@2026',
    database: 'ruoyi_ylsp', dateStrings: true
  });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);

  // ---------- 1. 工厂 ----------
  const f = await q(`SELECT * FROM mms_factory`);
  if (!f.length) {
    await q(`INSERT INTO mms_factory(factory_code,factory_name,address,status,del_flag,create_by,create_time,remark)
             VALUES('FAC01','云南赢隆食品加工有限公司','云南省曲靖市宣威市工业园区','0','0','admin',NOW(),'主生产工厂')`);
  }

  // ---------- 2. 班次 ----------
  if ((await q(`SELECT COUNT(*) c FROM mms_shift`))[0].c === 0) {
    await q(`INSERT INTO mms_shift(shift_code,shift_name,start_time,end_time,rest_minutes,avail_hours,status,del_flag,create_by,create_time,remark) VALUES
      ('S01','白班','08:00','16:00',60,7.00,'1','0','admin',NOW(),'常规生产班'),
      ('S02','中班','16:00','24:00',60,7.00,'1','0','admin',NOW(),'常规生产班'),
      ('S03','夜班','00:00','08:00',60,7.00,'1','0','admin',NOW(),'腌制/发酵值守')`);
  }

  // ---------- 3. 班组 ----------
  if ((await q(`SELECT COUNT(*) c FROM mms_team`))[0].c === 0) {
    await q(`INSERT INTO mms_team(team_code,team_name,resource_id,leader,phone,member_count,status,del_flag,create_by,create_time,remark) VALUES
      ('TM01','屠宰分割甲班',NULL,'张伟','13887001201',14,'0','0','admin',NOW(),'屠宰分割车间'),
      ('TM02','分割精修乙班',NULL,'杨帆','13887001202',12,'0','0','admin',NOW(),'屠宰分割车间'),
      ('TM03','灌装班',NULL,'刘洋','13887001203',16,'0','0','admin',NOW(),'灌装车间'),
      ('TM04','蒸煮熏制班',NULL,'陈静','13887001204',10,'0','0','admin',NOW(),'腌制发酵车间'),
      ('TM05','酱卤班',NULL,'孙丽','13887001205',8,'0','0','admin',NOW(),'酱卤车间'),
      ('TM06','包装班',NULL,'赵雷','13887001206',18,'0','0','admin',NOW(),'包装车间')`);
  }

  // ---------- 4. 产线 ----------
  if ((await q(`SELECT COUNT(*) c FROM mms_line`))[0].c === 0) {
    await q(`INSERT INTO mms_line(line_code,line_name,workshop_id,line_type,cycle_time,status,del_flag,create_by,create_time,remark) VALUES
      ('LN01','分割线1号',1,'1',45,'0','0','admin',NOW(),'屠宰分割'),
      ('LN02','灌装线1号',2,'1',30,'0','0','admin',NOW(),'香肠灌装'),
      ('LN03','蒸煮线1号',4,'1',60,'0','0','admin',NOW(),'蒸煮熏制'),
      ('LN04','酱卤线1号',5,'1',50,'0','0','admin',NOW(),'酱卤'),
      ('LN05','包装线1号',6,'1',20,'0','0','admin',NOW(),'真空包装'),
      ('LN06','炼油线1号',3,'1',55,'0','0','admin',NOW(),'猪油炼制')`);
  }

  // ---------- 5. 工位 ----------
  if ((await q(`SELECT COUNT(*) c FROM mms_station`))[0].c === 0) {
    const stations = [
      ['ST0101', '分割台位', 1, '1'], ['ST0102', '修整台位', 1, '1'],
      ['ST0201', '灌装机台位', 2, '0'], ['ST0202', '扎线台位', 2, '1'],
      ['ST0301', '蒸煮锅台位', 3, '0'], ['ST0302', '烟熏炉台位', 3, '0'],
      ['ST0401', '卤制锅台位', 4, '0'], ['ST0402', '晾凉台位', 4, '1'],
      ['ST0501', '真空包装台位', 5, '0'], ['ST0502', '金检台位', 5, '0'],
      ['ST0503', '装箱台位', 5, '1'], ['ST0601', '炼油锅台位', 6, '0']
    ];
    for (const s of stations) {
      await q(`INSERT INTO mms_station(station_code,station_name,line_id,station_type,status,del_flag,create_by,create_time)
               VALUES(?,?,?,?,'0','0','admin',NOW())`, s);
    }
  }

  // ---------- 6. 资源（关键设备/工位能力） ----------
  if ((await q(`SELECT COUNT(*) c FROM mms_resource`))[0].c === 0) {
    const res = [
      ['RS01', '分割台组', '2', 850, 'kg/h', 200, 3000, 92, '屠宰分割车间', '分割线1号'],
      ['RS02', '绞肉机', '0', 600, 'kg/h', 100, 2000, 88, '灌装车间', '灌装线1号'],
      ['RS03', '滚揉机', '0', 400, 'kg/h', 200, 1500, 85, '腌制发酵车间', ''],
      ['RS04', '真空灌装机', '0', 1200, '袋/h', 50, 5000, 90, '灌装车间', '灌装线1号'],
      ['RS05', '蒸煮锅', '0', 500, 'kg/h', 200, 1200, 87, '腌制发酵车间', '蒸煮线1号'],
      ['RS06', '烟熏炉', '0', 350, 'kg/h', 200, 1000, 83, '腌制发酵车间', '蒸煮线1号'],
      ['RS07', '卤制锅', '0', 300, 'kg/h', 150, 800, 86, '酱卤车间', '酱卤线1号'],
      ['RS08', '真空包装机', '0', 1800, '袋/h', 50, 8000, 94, '包装车间', '包装线1号'],
      ['RS09', '金属探测仪', '0', 3000, '袋/h', 1, 99999, 99, '包装车间', '包装线1号'],
      ['RS10', '炼油锅', '0', 250, 'kg/h', 100, 600, 80, '炼油车间', '炼油线1号']
    ];
    for (const r of res) {
      await q(`INSERT INTO mms_resource(resource_code,resource_name,resource_type,rate,rate_unit,batch_min,batch_max,efficiency,workshop_name,line_name,status,del_flag,create_by,create_time)
               VALUES(?,?,?,?,?,?,?,?,?,?, '0','0','admin',NOW())`, r);
    }
    // 班组关联资源
    await q(`UPDATE mms_team SET resource_id=1 WHERE team_code IN ('TM01','TM02')`);
    await q(`UPDATE mms_team SET resource_id=4 WHERE team_code='TM03'`);
    await q(`UPDATE mms_team SET resource_id=5 WHERE team_code='TM04'`);
    await q(`UPDATE mms_team SET resource_id=7 WHERE team_code='TM05'`);
    await q(`UPDATE mms_team SET resource_id=8 WHERE team_code='TM06'`);
  }

  // ---------- 7. 工厂日历 ----------
  if ((await q(`SELECT COUNT(*) c FROM mms_factory_calendar`))[0].c === 0) {
    const shiftJson = JSON.stringify([{ shiftCode: 'S01', shiftName: '白班', hours: 7 }, { shiftCode: 'S02', shiftName: '中班', hours: 7 }]);
    let d = new Date('2026-09-01T00:00:00');
    const end = new Date('2026-12-31T00:00:00');
    const rows = [];
    while (d <= end) {
      const s = d.toISOString().slice(0, 10);
      const dow = d.getDay();
      let type = (dow === 0 || dow === 6) ? '1' : '0';
      if (s >= '2026-10-01' && s <= '2026-10-07') type = '2'; // 国庆
      rows.push([s, type, type === '0' ? shiftJson : null]);
      d.setDate(d.getDate() + 1);
    }
    for (const r of rows) {
      await q(`INSERT INTO mms_factory_calendar(work_date,day_type,shift_json) VALUES(?,?,?)`, r);
    }
  }

  console.log('主数据完成:', await q(`SELECT
    (SELECT COUNT(*) FROM mms_factory) 工厂,
    (SELECT COUNT(*) FROM mms_shift) 班次,
    (SELECT COUNT(*) FROM mms_team) 班组,
    (SELECT COUNT(*) FROM mms_line) 产线,
    (SELECT COUNT(*) FROM mms_station) 工位,
    (SELECT COUNT(*) FROM mms_resource) 资源,
    (SELECT COUNT(*) FROM mms_factory_calendar) 日历天`));
  await conn.end();
})();

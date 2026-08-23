const http = require("http");

function req(method, path, body, token) {
  return new Promise((resolve, reject) => {
    const data = body ? JSON.stringify(body) : "";
    const headers = { "Content-Type": "application/json" };
    if (data) headers["Content-Length"] = Buffer.byteLength(data);
    if (token) headers["Authorization"] = "Bearer " + token;

    const opt = {
      hostname: "localhost",
      port: 8081,
      path: path,
      method: method,
      headers: headers,
    };

    const r = http.request(opt, (res) => {
      let b = "";
      res.on("data", (c) => (b += c));
      res.on("end", () => {
        try {
          resolve({ status: res.statusCode, data: JSON.parse(b) });
        } catch (e) {
          resolve({ status: res.statusCode, data: b });
        }
      });
    });
    r.on("error", reject);
    if (data) r.write(data);
    r.end();
  });
}

async function main() {
  let pass = 0, fail = 0;
  function check(name, condition, detail) {
    if (condition) { pass++; console.log("  ✅ " + name + (detail ? " — " + detail : "")); }
    else { fail++; console.log("  ❌ " + name + (detail ? " — " + detail : "")); }
  }

  console.log("╔══════════════════════════════════════════════════════╗");
  console.log("║   异常管理 ↔ 停机记录 联动功能完整测试               ║");
  console.log("╚══════════════════════════════════════════════════════╝");

  // ===== 0. 登录 =====
  console.log("\n━━━ 0. 登录 ━━━");
  const login = await req("POST", "/login", { username: "admin", password: "admin123" });
  const token = login.data.token;
  check("登录成功", !!token, token ? "Token: " + token.substring(0, 20) + "..." : "FAILED");
  if (!token) { console.log("登录失败，终止测试"); return; }

  // ===== 1. 查询现有异常列表 =====
  console.log("\n━━━ 1. 查询现有异常列表 ━━━");
  const abListBefore = await req("GET", "/mms/abnormal/list?pageNum=1&pageSize=5", null, token);
  check("异常列表查询", abListBefore.status === 200, "总数: " + abListBefore.data.total);

  // ===== 2. 查询现有停机记录列表 =====
  console.log("\n━━━ 2. 查询现有停机记录列表 ━━━");
  const dtListBefore = await req("GET", "/mms/downtime/list?pageNum=1&pageSize=5", null, token);
  check("停机记录列表查询", dtListBefore.status === 200, "总数: " + dtListBefore.data.total);

  // ===== 3. 查询停机类型字典 =====
  console.log("\n━━━ 3. 查询停机类型字典 mms_downtime_type ━━━");
  const dictList = await req("GET", "/system/dict/data/type/mms_downtime_type", null, token);
  check("停机类型字典存在", dictList.status === 200 && Array.isArray(dictList.data.data) && dictList.data.data.length > 0,
    dictList.data.data ? dictList.data.data.map(d => d.dictLabel + "(" + d.dictValue + ")").join(", ") : "字典不存在");

  // ===== 4. 查询异常状态字典 =====
  console.log("\n━━━ 4. 查询异常状态字典 mms_abnormal_status ━━━");
  const abStatusDict = await req("GET", "/system/dict/data/type/mms_abnormal_status", null, token);
  check("异常状态字典存在", abStatusDict.status === 200 && Array.isArray(abStatusDict.data.data),
    abStatusDict.data.data ? abStatusDict.data.data.map(d => d.dictLabel + "(" + d.dictValue + ")").join(", ") : "字典不存在");

  // ===== 5. 创建异常记录（设备异常） =====
  console.log("\n━━━ 5. 创建异常记录（设备异常-严重） ━━━");
  const newAb1 = {
    workOrderNo: "WO-TEST-001",
    resourceName: "测试产线A",
    abnormalType: "0", // 设备异常
    severity: "2",     // 紧急
    description: "测试-注塑机温度异常停机",
    remark: "API自动化测试",
  };
  const createAb1 = await req("POST", "/mms/abnormal", newAb1, token);
  check("创建设备异常", createAb1.status === 200 && createAb1.data.code === 200, JSON.stringify(createAb1.data));

  // 查询刚创建的异常
  const myAb1List = await req("GET", "/mms/abnormal/list?pageNum=1&pageSize=1&workOrderNo=WO-TEST-001", null, token);
  const ab1 = myAb1List.data.rows && myAb1List.data.rows[0];
  check("查询到新创建的异常", !!ab1, ab1 ? "单号: " + ab1.abnormalNo + ", 状态: " + ab1.status : "NOT FOUND");
  if (!ab1) { console.log("无法创建异常，终止测试"); return; }

  check("异常单号已生成", !!ab1.abnormalNo && ab1.abnormalNo.length > 0);
  check("异常状态为待响应(0)", ab1.status === "0", "status=" + ab1.status);
  check("异常类型为设备(0)", ab1.abnormalType === "0");
  check("严重等级为紧急(2)", ab1.severity === "2");
  check("上报人已自动填充", !!ab1.reportBy);
  check("上报时间已自动填充", !!ab1.reportTime);

  const ab1Id = ab1.abnormalId;

  // ===== 6. 创建第二条异常（物料异常-一般） =====
  console.log("\n━━━ 6. 创建异常记录（物料异常-一般） ━━━");
  const newAb2 = {
    workOrderNo: "WO-TEST-002",
    resourceName: "测试产线B",
    abnormalType: "1", // 物料异常
    severity: "0",     // 一般
    description: "测试-原材料短缺",
  };
  const createAb2 = await req("POST", "/mms/abnormal", newAb2, token);
  check("创建物料异常", createAb2.status === 200 && createAb2.data.code === 200, JSON.stringify(createAb2.data));

  const myAb2List = await req("GET", "/mms/abnormal/list?pageNum=1&pageSize=1&workOrderNo=WO-TEST-002", null, token);
  const ab2 = myAb2List.data.rows && myAb2List.data.rows[0];
  check("查询到物料异常", !!ab2, ab2 ? "单号: " + ab2.abnormalNo : "NOT FOUND");
  const ab2Id = ab2 ? ab2.abnormalId : null;

  // ===== 7. 异常详情查询 =====
  console.log("\n━━━ 7. 异常详情查询 ━━━");
  const ab1Detail = await req("GET", "/mms/abnormal/" + ab1Id, null, token);
  check("异常详情查询", ab1Detail.status === 200 && ab1Detail.data.data != null);
  check("详情单号一致", ab1Detail.data.data && ab1Detail.data.data.abnormalNo === ab1.abnormalNo);

  // ===== 8. 异常修改 =====
  console.log("\n━━━ 8. 异常修改 ━━━");
  const updateBody = {
    abnormalId: ab1Id,
    abnormalNo: ab1.abnormalNo,
    workOrderNo: "WO-TEST-001",
    resourceName: "测试产线A-修改",
    abnormalType: "0",
    severity: "2",
    description: "测试-注塑机温度异常停机(已修改描述)",
    status: "0",
    remark: "修改后备注",
  };
  const updateRes = await req("PUT", "/mms/abnormal", updateBody, token);
  check("异常修改", updateRes.status === 200 && updateRes.data.code === 200, JSON.stringify(updateRes.data));

  const ab1AfterUpdate = await req("GET", "/mms/abnormal/" + ab1Id, null, token);
  check("修改后产能单元已更新", ab1AfterUpdate.data.data && ab1AfterUpdate.data.data.resourceName === "测试产线A-修改");
  check("修改后描述已更新", ab1AfterUpdate.data.data && ab1AfterUpdate.data.data.description.includes("已修改"));

  // ===== 9. 联动停机 - 设备异常 =====
  console.log("\n━━━ 9. 联动停机 - 设备异常 → 故障停机 ━━━");
  const linkRes1 = await req("POST", "/mms/abnormal/linkDowntime/" + ab1Id, null, token);
  check("联动停机成功", linkRes1.status === 200 && linkRes1.data.code === 200, JSON.stringify(linkRes1.data));

  // 验证停机记录已创建
  const dtAfterLink1 = await req("GET", "/mms/downtime/list?pageNum=1&pageSize=10", null, token);
  const linkedDt1 = dtAfterLink1.data.rows && dtAfterLink1.data.rows.find(d => d.abnormalNo === ab1.abnormalNo);
  check("联动停机记录已创建", !!linkedDt1, linkedDt1 ? "停机单号: " + linkedDt1.downtimeNo : "NOT FOUND");

  if (linkedDt1) {
    check("停机记录关联异常ID正确", linkedDt1.abnormalNo === ab1.abnormalNo);
    check("停机状态为停机中(0)", linkedDt1.status === "0", "status=" + linkedDt1.status);
    check("停机类型为故障停机(1)", linkedDt1.dtType === "1", "dtType=" + linkedDt1.dtType + " (期望1=故障停机)");
    check("停机原因包含[联动]", linkedDt1.reason && linkedDt1.reason.includes("[联动]"));
    check("停机原因包含异常描述", linkedDt1.reason && linkedDt1.reason.includes("注塑机温度"));
    check("停机开始时间已填充", !!linkedDt1.startTime);
    check("产能单元继承异常单", linkedDt1.resourceName === "测试产线A-修改");
  }

  // ===== 10. 重复联动停机（应失败） =====
  console.log("\n━━━ 10. 重复联动停机（应被拒绝） ━━━");
  const linkRes2 = await req("POST", "/mms/abnormal/linkDowntime/" + ab1Id, null, token);
  check("重复联动被拒绝", linkRes2.data.code === 500, JSON.stringify(linkRes2.data));

  // ===== 11. 联动停机 - 物料异常 → 物料停机 =====
  console.log("\n━━━ 11. 联动停机 - 物料异常 → 物料停机 ━━━");
  if (ab2Id) {
    const linkRes3 = await req("POST", "/mms/abnormal/linkDowntime/" + ab2Id, null, token);
    check("物料异常联动停机成功", linkRes3.status === 200 && linkRes3.data.code === 200, JSON.stringify(linkRes3.data));

    const dtAfterLink2 = await req("GET", "/mms/downtime/list?pageNum=1&pageSize=20", null, token);
    const linkedDt2 = dtAfterLink2.data.rows && dtAfterLink2.data.rows.find(d => d.abnormalNo === ab2.abnormalNo);
    check("物料联动停机记录已创建", !!linkedDt2);

    if (linkedDt2) {
      check("物料停机类型为物料停机(3)", linkedDt2.dtType === "3", "dtType=" + linkedDt2.dtType + " (期望3=物料停机)");
      check("物料停机状态为停机中(0)", linkedDt2.status === "0");
    }
  }

  // ===== 12. 异常响应 =====
  console.log("\n━━━ 12. 异常响应（待响应 → 处理中） ━━━");
  const respondRes = await req("PUT", "/mms/abnormal/respond/" + ab1Id + "?responseBy=admin", null, token);
  check("异常响应成功", respondRes.status === 200 && respondRes.data.code === 200, JSON.stringify(respondRes.data));

  const ab1AfterRespond = await req("GET", "/mms/abnormal/" + ab1Id, null, token);
  check("异常状态变为处理中(1)", ab1AfterRespond.data.data && ab1AfterRespond.data.data.status === "1",
    "status=" + ab1AfterRespond.data.data.status);
  check("响应人已记录", ab1AfterRespond.data.data && ab1AfterRespond.data.data.responseBy === "admin");
  check("响应时间已记录", ab1AfterRespond.data.data && !!ab1AfterRespond.data.data.responseTime);

  // ===== 13. 停机记录手动操作（修改+恢复） =====
  console.log("\n━━━ 13. 停机记录手动恢复测试 ━━━");
  // 先获取ab2关联的停机记录，手动恢复它（不通过异常关闭）
  if (ab2Id) {
    const dtListAll = await req("GET", "/mms/downtime/list?pageNum=1&pageSize=20", null, token);
    const dt2 = dtListAll.data.rows && dtListAll.data.rows.find(d => d.abnormalNo === ab2.abnormalNo);
    if (dt2) {
      // 手动修改停机记录为已恢复
      const updateDt = {
        downtimeId: dt2.downtimeId,
        downtimeNo: dt2.downtimeNo,
        abnormalId: dt2.abnormalId,
        abnormalNo: dt2.abnormalNo,
        resourceName: dt2.resourceName,
        // 不传 startTime，让后端从数据库取已有值
        // endTime 设为未来时间，确保停机时长 > 0
        endTime: "2026-08-23 23:59:00",
        dtType: dt2.dtType,
        reason: dt2.reason,
        status: "1", // 已恢复
        remark: "手动恢复测试",
      };
      const updateDtRes = await req("PUT", "/mms/downtime", updateDt, token);
      check("停机记录手动恢复", updateDtRes.status === 200 && updateDtRes.data.code === 200, JSON.stringify(updateDtRes.data));

      // 验证停机时长已计算
      const dt2After = await req("GET", "/mms/downtime/" + dt2.downtimeId, null, token);
      check("停机时长已自动计算", dt2After.data.data && dt2After.data.data.minutes != null && dt2After.data.data.minutes >= 0,
        "minutes=" + (dt2After.data.data ? dt2After.data.data.minutes : "?") + " (手动恢复时由后端根据开始/结束时间计算)");
      check("停机状态为已恢复(1)", dt2After.data.data && dt2After.data.data.status === "1");
      check("结束时间已填充", dt2After.data.data && !!dt2After.data.data.endTime);
    }
  }

  // ===== 14. 异常关闭（联动自动关闭停机） =====
  console.log("\n━━━ 14. 异常关闭（联动自动恢复停机记录） ━━━");
  const resolveRes = await req("PUT", "/mms/abnormal/resolve/" + ab1Id + "?handleResult=" + encodeURIComponent("设备已修复，温度恢复正常，更换加热圈"), null, token);
  check("异常关闭成功", resolveRes.status === 200 && resolveRes.data.code === 200, JSON.stringify(resolveRes.data));

  const ab1AfterResolve = await req("GET", "/mms/abnormal/" + ab1Id, null, token);
  check("异常状态变为已关闭(2)", ab1AfterResolve.data.data && ab1AfterResolve.data.data.status === "2",
    "status=" + ab1AfterResolve.data.data.status);
  check("处理结果已记录", ab1AfterResolve.data.data && ab1AfterResolve.data.data.handleResult && ab1AfterResolve.data.data.handleResult.includes("设备已修复"));
  check("关闭时间已记录", ab1AfterResolve.data.data && !!ab1AfterResolve.data.data.closeTime);

  // 验证关联的停机记录已自动恢复
  console.log("\n━━━ 15. 验证联动关闭停机记录 ━━━");
  const dtListAfterClose = await req("GET", "/mms/downtime/list?pageNum=1&pageSize=20", null, token);
  const dt1AfterClose = dtListAfterClose.data.rows && dtListAfterClose.data.rows.find(d => d.abnormalNo === ab1.abnormalNo);
  check("联动停机记录已自动恢复", dt1AfterClose && dt1AfterClose.status === "1",
    "status=" + (dt1AfterClose ? dt1AfterClose.status : "?") + " (期望1=已恢复)");

  if (dt1AfterClose) {
    check("停机结束时间已自动填充", !!dt1AfterClose.endTime);
    check("停机时长已自动计算", dt1AfterClose.minutes != null && dt1AfterClose.minutes >= 0,
      "minutes=" + dt1AfterClose.minutes);
    check("停机状态保持已恢复", dt1AfterClose.status === "1");
  }

  // ===== 16. 已关闭异常不可修改 =====
  console.log("\n━━━ 16. 已关闭异常不可修改 ━━━");
  const updateClosed = await req("PUT", "/mms/abnormal", updateBody, token);
  check("已关闭异常修改被拒绝", updateClosed.data.code === 500, JSON.stringify(updateClosed.data));

  // ===== 17. 已关闭异常不可删除 =====
  console.log("\n━━━ 17. 已关闭异常不可删除 ━━━");
  const deleteClosed = await req("DELETE", "/mms/abnormal/" + ab1Id, null, token);
  check("已关闭异常删除被拒绝", deleteClosed.data.code === 500, JSON.stringify(deleteClosed.data));

  // ===== 18. 异常列表状态统计 =====
  console.log("\n━━━ 18. 异常列表状态统计 ━━━");
  const abAllList = await req("GET", "/mms/abnormal/list?pageNum=1&pageSize=999", null, token);
  let countByStatus = { "0": 0, "1": 0, "2": 0 };
  if (abAllList.data.rows) {
    abAllList.data.rows.forEach(r => {
      const s = r.status || "0";
      countByStatus[s] = (countByStatus[s] || 0) + 1;
    });
  }
  console.log("  异常状态统计: 待响应(0)=" + countByStatus["0"] + ", 处理中(1)=" + countByStatus["1"] + ", 已关闭(2)=" + countByStatus["2"]);
  check("存在已关闭的异常", countByStatus["2"] > 0);

  // ===== 19. 停机记录列表关联展示 =====
  console.log("\n━━━ 19. 停机记录列表关联异常单号展示 ━━━");
  const dtAllList = await req("GET", "/mms/downtime/list?pageNum=1&pageSize=999", null, token);
  const linkedCount = dtAllList.data.rows ? dtAllList.data.rows.filter(d => d.abnormalNo).length : 0;
  check("存在关联异常的停机记录", linkedCount > 0, "关联数: " + linkedCount);
  if (dtAllList.data.rows) {
    console.log("  停机记录详情:");
    dtAllList.data.rows.slice(0, 5).forEach(d => {
      console.log("    " + d.downtimeNo + " | 异常单:" + (d.abnormalNo || "无") + " | 状态:" + d.status + " | 类型:" + d.dtType + " | 时长:" + (d.minutes || 0) + "分");
    });
  }

  // ===== 20. 生产看板数据验证 =====
  console.log("\n━━━ 20. 生产看板数据验证 ━━━");
  const overview = await req("GET", "/mms/dashboard/overview", null, token);
  check("看板接口正常", overview.status === 200 && overview.data.data != null);

  if (overview.data.data) {
    const d = overview.data.data;
    console.log("  看板数据:");
    console.log("    异常统计: " + JSON.stringify(d.abnormal));
    console.log("    未闭环异常: " + JSON.stringify(d.abnormalSummary));
    console.log("    今日停机: " + JSON.stringify(d.downtime));

    check("看板包含未闭环异常数据", d.abnormalSummary != null && d.abnormalSummary.unclosed !== undefined);
    check("看板包含今日停机数据", d.downtime != null && d.downtime.totalMinutes !== undefined);
    check("未闭环异常数 = 待响应+处理中",
      d.abnormalSummary.unclosed === (d.abnormal.pending + d.abnormal.processing),
      "unclosed=" + d.abnormalSummary.unclosed + ", pending+processing=" + (d.abnormal.pending + d.abnormal.processing));
    check("今日停机次数 >= 0", d.downtime.count >= 0, "count=" + d.downtime.count);
    check("今日停机总时长 >= 0", d.downtime.totalMinutes >= 0, "totalMinutes=" + d.downtime.totalMinutes);
  }

  // ===== 21. 创建第三条异常 - 完整流程后删除测试 =====
  console.log("\n━━━ 21. 异常删除测试（待响应状态可删除） ━━━");
  const newAb3 = {
    workOrderNo: "WO-DEL-TEST-001",
    resourceName: "测试产线C",
    abnormalType: "4", // 其他
    severity: "0",
    description: "测试-待删除的异常记录",
  };
  const createAb3 = await req("POST", "/mms/abnormal", newAb3, token);
  check("创建待删除异常", createAb3.data.code === 200);

  const myAb3List = await req("GET", "/mms/abnormal/list?pageNum=1&pageSize=1&workOrderNo=WO-DEL-TEST-001", null, token);
  const ab3 = myAb3List.data.rows && myAb3List.data.rows[0];
  if (ab3) {
    const deleteRes = await req("DELETE", "/mms/abnormal/" + ab3.abnormalId, null, token);
    check("待响应异常可删除", deleteRes.data.code === 200, JSON.stringify(deleteRes.data));

    // 验证已删除（软删除后 selectAbnormalById 不过滤 del_flag，但 selectAbnormalList 会过滤）
    const ab3ListCheck = await req("GET", "/mms/abnormal/list?pageNum=1&pageSize=999&workOrderNo=WO-DEL-TEST-001", null, token);
    check("删除后列表中不再出现", ab3ListCheck.data.rows == null || ab3ListCheck.data.rows.length === 0, "剩余记录数: " + (ab3ListCheck.data.rows ? ab3ListCheck.data.rows.length : 0));
  }

  // ===== 22. 停机记录独立CRUD测试 =====
  console.log("\n━━━ 22. 停机记录独立CRUD测试 ━━━");
  const newDt = {
    resourceName: "独立测试产线D",
    startTime: "2026-08-23 10:00:00",
    dtType: "2", // 换型停机
    reason: "独立创建的停机记录-产品换型",
    status: "0",
    remark: "独立CRUD测试",
  };
  const createDt = await req("POST", "/mms/downtime", newDt, token);
  check("独立创建停机记录", createDt.data.code === 200, JSON.stringify(createDt.data));

  const myDtList = await req("GET", "/mms/downtime/list?pageNum=1&pageSize=1&resourceName=" + encodeURIComponent("独立测试产线D"), null, token);
  const dt3 = myDtList.data.rows && myDtList.data.rows[0];
  if (dt3) {
    check("停机单号已生成", !!dt3.downtimeNo);
    check("停机状态为停机中(0)", dt3.status === "0");
    check("停机类型为换型停机(2)", dt3.dtType === "2");
    check("无关联异常单号", !dt3.abnormalNo, "abnormalNo=" + dt3.abnormalNo);

    // 修改
    const updateDt2 = {
      downtimeId: dt3.downtimeId,
      downtimeNo: dt3.downtimeNo,
      resourceName: "独立测试产线D-修改",
      // 不传 startTime，让后端从数据库取
      // endTime 设为未来时间，确保停机时长 > 0
      endTime: "2026-08-23 23:59:00",
      dtType: "2",
      reason: "修改后的原因",
      status: "1", // 已恢复
      remark: "修改测试",
    };
    const updateDtRes2 = await req("PUT", "/mms/downtime", updateDt2, token);
    check("停机记录修改", updateDtRes2.data.code === 200);

    const dt3After = await req("GET", "/mms/downtime/" + dt3.downtimeId, null, token);
    check("修改后产能单元已更新", dt3After.data.data && dt3After.data.data.resourceName === "独立测试产线D-修改");
    check("修改后状态为已恢复", dt3After.data.data && dt3After.data.data.status === "1");
    check("修改后停机时长已计算", dt3After.data.data && dt3After.data.data.minutes != null && dt3After.data.data.minutes > 0,
      "minutes=" + (dt3After.data.data ? dt3After.data.data.minutes : "?"));

    // 删除（已恢复状态应该允许删除？看代码逻辑没有限制）
    const deleteDtRes = await req("DELETE", "/mms/downtime/" + dt3.downtimeId, null, token);
    check("停机记录删除", deleteDtRes.data.code === 200, JSON.stringify(deleteDtRes.data));
  }

  // ===== 23. 异常状态流转验证（待响应→处理中→已关闭 完整链路） =====
  console.log("\n━━━ 23. 完整状态流转验证 ━━━");
  // 用ab2（物料异常，如果还没关闭的话）
  if (ab2Id) {
    const ab2Check = await req("GET", "/mms/abnormal/" + ab2Id, null, token);
    if (ab2Check.data.data) {
      const ab2Status = ab2Check.data.data.status;
      console.log("  物料异常当前状态: " + ab2Status);
      if (ab2Status === "0") {
        // 响应
        const r1 = await req("PUT", "/mms/abnormal/respond/" + ab2Id + "?responseBy=admin", null, token);
        check("物料异常响应", r1.data.code === 200);
      }
      const ab2Check2 = await req("GET", "/mms/abnormal/" + ab2Id, null, token);
      if (ab2Check2.data.data && ab2Check2.data.data.status === "1") {
        // 关闭
        const r2 = await req("PUT", "/mms/abnormal/resolve/" + ab2Id + "?handleResult=" + encodeURIComponent("物料已到位"), null, token);
        check("物料异常关闭", r2.data.code === 200);
      }
      const ab2Final = await req("GET", "/mms/abnormal/" + ab2Id, null, token);
      check("物料异常最终状态为已关闭(2)", ab2Final.data.data && ab2Final.data.data.status === "2",
        "status=" + ab2Final.data.data.status);
    }
  }

  // ===== 测试结果汇总 =====
  console.log("\n╔══════════════════════════════════════════════════════╗");
  console.log("║                    测试结果汇总                       ║");
  console.log("╠══════════════════════════════════════════════════════╣");
  console.log("║  ✅ 通过: " + pass + "  ❌ 失败: " + fail + "  总计: " + (pass + fail) + "                        ║");
  console.log("╚══════════════════════════════════════════════════════╝");

  if (fail > 0) {
    console.log("\n⚠️ 存在失败项，请检查上方标记为 ❌ 的测试");
  } else {
    console.log("\n🎉 全部测试通过！");
  }
}

main().catch((e) => console.error("测试出错:", e));

/**
 * LYJS 分支网关冒烟回归测试：node smoke-lyjs.mjs
 * 覆盖：stdio 模式（PROFILE=lyjs）握手 + 9 工具注册 + 4 个新工具实际调用 + 92 天限制拦截
 * 前置：后端 http://127.0.0.1:85 运行中；profiles/lyjs.json 已配置；权限 SQL 已执行
 */
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StdioClientTransport } from "@modelcontextprotocol/sdk/client/stdio.js";

const pass = 0, fail = 0;
const results = [];
const ok = (name, cond, extra = "") => {
  results.push(`${cond ? "PASS" : "FAIL"} ${name}${extra ? " | " + extra : ""}`);
  return cond;
};

const t = new StdioClientTransport({
  command: process.execPath,
  args: ["gateway.mjs"],
  env: { ...process.env, EDOP_PROFILE: "lyjs" },
});
const c = new Client({ name: "smoke-lyjs", version: "0.0.0" });
await c.connect(t);

const tools = await c.listTools();
const names = tools.tools.map(x => x.name).sort();
ok("握手+9工具注册", tools.tools.length === 9, names.join(","));

const expected = ["get_bi_metrics", "get_platform_overview", "query_bid_projects",
  "query_inventory", "query_projects", "query_purchase_orders",
  "query_quality_tasks", "query_safety_hazards", "query_sales_orders"];
ok("工具清单齐全（5标准+4新增）", JSON.stringify(names) === JSON.stringify(expected.slice().sort()));

// 4 个新工具实际调用（每工具取 1 条）
for (const name of ["query_projects", "query_safety_hazards", "query_quality_tasks", "query_bid_projects"]) {
  try {
    const r = await c.callTool({ name, arguments: { pageNum: 1, pageSize: 1 } });
    ok(name + " 实际调用", !r.isError, (r.content?.[0]?.text || "").slice(0, 80).replace(/\n/g, " "));
  } catch (e) {
    ok(name + " 实际调用", false, String(e).slice(0, 100));
  }
}

// 标准工具抽查 1 个
try {
  const r = await c.callTool({ name: "query_inventory", arguments: { pageNum: 1, pageSize: 1 } });
  ok("query_inventory（标准抽查）", !r.isError, (r.content?.[0]?.text || "").slice(0, 60).replace(/\n/g, " "));
} catch (e) {
  ok("query_inventory（标准抽查）", false, String(e).slice(0, 100));
}

// 92 天限制：仅对声明 dateRange 的工具生效（gateway.mjs:259）。query_projects 未声明日期范围，
// 同参数应正常放行；改用标准工具 query_sales_orders 验证 92 天拦截。
try {
  const r = await c.callTool({ name: "query_projects", arguments: { startDate: "2026-01-01", endDate: "2027-01-01" } });
  ok("未声明 dateRange 的工具不受 92 天约束", !r.isError);
} catch (e) {
  ok("未声明 dateRange 的工具不受 92 天约束", false, String(e).slice(0, 80));
}
try {
  const r = await c.callTool({ name: "query_sales_orders", arguments: { startDate: "2026-01-01", endDate: "2027-01-01" } });
  ok("92天限制拦截（dateRange 工具生效）", r.isError === true);
} catch (e) {
  ok("92天限制拦截（dateRange 工具生效）", true, "网关参数校验拒绝：" + String(e).slice(0, 80));
}

await c.close();
console.log(results.join("\n"));
const f = results.filter(x => x.startsWith("FAIL")).length;
console.log(`\n汇总: ${results.length - f}/${results.length} passed, ${f} failed`);
process.exit(f > 0 ? 1 : 0);

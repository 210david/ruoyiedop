/**
 * 网关冒烟回归测试：node smoke-test.mjs
 * 覆盖：stdio 模式（握手+调用）、http 模式（健康检查+API Key 鉴权+握手+调用+错误Key拒绝）
 */
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StdioClientTransport } from "@modelcontextprotocol/sdk/client/stdio.js";
import { StreamableHTTPClientTransport } from "@modelcontextprotocol/sdk/client/streamableHttp.js";
import { spawn } from "node:child_process";

const NODE = process.execPath;
let pass = 0, fail = 0;
const ok = (name, cond, extra = "") => {
  console.log(`${cond ? "PASS" : "FAIL"} ${name}${extra ? " | " + extra : ""}`);
  cond ? pass++ : fail++;
};

/* ---------- 1. stdio 模式 ---------- */
{
  const t = new StdioClientTransport({ command: NODE, args: ["gateway.mjs"] });
  const c = new Client({ name: "smoke-stdio", version: "0.0.0" });
  await c.connect(t);
  const tools = await c.listTools();
  ok("stdio 握手+5工具注册", tools.tools.length === 5, tools.tools.map(x => x.name).join(","));
  const r = await c.callTool({ name: "query_inventory", arguments: { pageSize: 1 } });
  ok("stdio query_inventory", !r.isError, r.content[0].text.slice(0, 60).replace(/\n/g, " "));
  const r2 = await c.callTool({ name: "query_sales_orders", arguments: { startDate: "2026-01-01", endDate: "2027-01-01" } });
  ok("stdio 92天限制拦截", r2.isError === true);
  await c.close();
}

/* ---------- 2. http 模式 ---------- */
const PORT = 3000; // 与 profiles/standard.json 的 httpPort 一致
const child = spawn(NODE, ["gateway.mjs"], {
  env: { ...process.env, EDOP_GATEWAY_MODE: "http", EDOP_PROFILE: "standard" },
  stdio: ["ignore", "ignore", "pipe"],
});
child.stderr.on("data", d => process.stderr.write(d));
// 等待端口就绪
await new Promise((resolve, reject) => {
  let tries = 0;
  const timer = setInterval(async () => {
    try {
      const res = await fetch(`http://127.0.0.1:${PORT}/health`);
      if (res.ok) { clearInterval(timer); resolve(); }
    } catch { if (++tries > 50) { clearInterval(timer); reject(new Error("http 启动超时")); } }
  }, 200);
});
try {
  const health = await fetch(`http://127.0.0.1:${PORT}/health`).then(r => r.json());
  ok("http /health", health.ok === true, `profile=${health.profile}`);

  const bad = await fetch(`http://127.0.0.1:${PORT}/mcp`, { method: "POST", headers: { "Content-Type": "application/json", "x-api-key": "wrong" }, body: JSON.stringify({ jsonrpc: "2.0", id: 1, method: "initialize" }) });
  ok("http 错误Key被401拒绝", bad.status === 401);

  const t = new StreamableHTTPClientTransport(new URL(`http://127.0.0.1:${PORT}/mcp`), { requestInit: { headers: { "x-api-key": "edop-local-dev-key" } } });
  const c = new Client({ name: "smoke-http", version: "0.0.0" });
  await c.connect(t);
  const tools = await c.listTools();
  ok("http 握手+5工具注册", tools.tools.length === 5);
  const r = await c.callTool({ name: "get_bi_metrics", arguments: { queryDict: true } });
  ok("http get_bi_metrics(字典)", !r.isError, r.content[0].text.slice(0, 60).replace(/\n/g, " "));
  await c.close();
} finally {
  child.kill();
}

console.log(`\n结果: ${pass} 通过, ${fail} 失败`);
process.exit(fail ? 1 : 0);

/**
 * EDOP × WorkBuddy MCP 网关 v2（Phase 2 三层架构，方案 2F/2H）
 *
 * 结构（纪律：分支仓禁改本文件，共性功能只回 main 改再同步）：
 *   profiles/<客户>.json              → 客户身份配置（PROFILE 环境变量选择）
 *   tool-manifests/<分支>.tools.json  → 工具接口映射（契约漂移改这里，见 CONTRACT.md）
 *   gateway.mjs                       → 标准引擎（本文件，单源）
 *
 * 模式：
 *   stdio（默认） → 本机自用，WorkBuddy 直接拉起
 *   http          → 客户交付，express + StreamableHTTP（stateless）+ API Key 鉴权
 */
import fs from "node:fs";
import path from "node:path";
import crypto from "node:crypto";
import https from "node:https";
import { AsyncLocalStorage } from "node:async_hooks";
import { fileURLToPath } from "node:url";
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { StreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import express from "express";
import { z } from "zod";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

/* ---------------- 配置加载（profiles 层） ---------------- */

const PROFILE = process.env.EDOP_PROFILE || "standard";
const profilePath = path.join(__dirname, "profiles", `${PROFILE}.json`);
const legacyPath = path.join(__dirname, "config.json");

let config;
if (fs.existsSync(profilePath)) {
  config = JSON.parse(fs.readFileSync(profilePath, "utf8"));
} else if (fs.existsSync(legacyPath)) {
  config = JSON.parse(fs.readFileSync(legacyPath, "utf8"));
  console.error(`[edop-gateway] 提示：profiles/${PROFILE}.json 不存在，已回退旧版 config.json（建议迁移到 profiles 层）`);
} else {
  console.error(`[edop-gateway] 缺少 profiles/${PROFILE}.json，无法启动`);
  process.exit(1);
}

const MODE = process.env.EDOP_GATEWAY_MODE || config.mode || "stdio";
const AUDIT_LOG = process.env.EDOP_AUDIT_FILE || path.join(__dirname, "audit.log");

function audit(tool, detail) {
  try {
    fs.appendFileSync(AUDIT_LOG, `[${new Date().toISOString()}] profile=${PROFILE} tool=${tool} ${detail}\n`);
  } catch {
    /* 审计失败不阻断业务 */
  }
}

/* ---------------- 工具映射加载（tool-manifests 层） ---------------- */

const manifestName = config.manifest || PROFILE;
const manifestPath = path.join(__dirname, "tool-manifests", `${manifestName}.tools.json`);
let manifest;
try {
  manifest = JSON.parse(fs.readFileSync(manifestPath, "utf8"));
} catch {
  console.error(`[edop-gateway] 工具映射缺失：${manifestPath}`);
  process.exit(1);
}

/* ---------------- EDOP 客户端（引擎） ---------------- */

let token = null;

/** 逐用户模式：每请求的鉴权上下文（HTTP 模式经 AsyncLocalStorage 绑定；stdio 恒为 service） */
const requestCtx = new AsyncLocalStorage();

/** grant_token → 业务 JWT 缓存（JWT 有效期 30min，取 25min 保守刷新） */
const jwtCache = new Map();
const JWT_TTL_MS = 25 * 60 * 1000;

function sha256Hex(input) {
  return crypto.createHash("sha256").update(input, "utf8").digest("hex");
}

/** 用 grant_token 换发业务 JWT（EDOP /edop/connector/auth/exchange） */
async function exchangeGrant(grant) {
  const res = await fetch(`${config.baseUrl}/edop/connector/auth/exchange`, {
    method: "POST",
    headers: { Authorization: `Bearer ${grant}` },
  });
  const data = await res.json().catch(() => null);
  if (!data || data.code !== 200 || !data.token) {
    throw new Error(`EDOP 授权校验失败: ${data ? data.msg : `HTTP ${res.status}`}（token 失效请重新在 EDOP 授权页授权）`);
  }
  audit("exchange", `user=${data.userName ?? "?"} ok`);
  return data.token;
}

async function resolveUserAuth(grant) {
  const key = sha256Hex(grant);
  const cached = jwtCache.get(key);
  if (cached && Date.now() < cached.expireAt) return cached.jwt;
  const jwt = await exchangeGrant(grant);
  jwtCache.set(key, { jwt, expireAt: Date.now() + JWT_TTL_MS });
  return jwt;
}

async function login() {
  const res = await fetch(`${config.baseUrl}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username: config.username, password: config.password }),
  });
  const data = await res.json().catch(() => null);
  if (!data || data.code !== 200 || !data.token) {
    throw new Error(`EDOP 登录失败: ${data ? data.msg : `HTTP ${res.status}`}`);
  }
  token = data.token;
  audit("login", `user=${config.username} ok`);
}

/**
 * 只允许 GET。鉴权优先级：
 *   请求上下文 user 模式（Bearer grant_token）→ 以该用户 JWT 调用（@DataScope 按人裁剪）
 *   service 模式（默认/stdio/x-api-key）        → 服务账号登录
 * 401 自动重换/重登一次
 */
async function apiGet(pathname, params = {}) {
  const ctx = requestCtx.getStore();
  const isUser = !!(ctx && ctx.mode === "user");
  let authHeader;
  if (isUser) {
    authHeader = { Authorization: `Bearer ${await resolveUserAuth(ctx.grant)}` };
  } else {
    if (!token) await login();
    authHeader = { Authorization: `Bearer ${token}` };
  }

  const url = new URL(config.baseUrl + pathname);
  for (const [k, v] of Object.entries(params)) {
    if (v !== undefined && v !== null && v !== "") url.searchParams.set(k, v);
  }
  let res = await fetch(url, { headers: authHeader });
  if (res.status === 401) {
    if (isUser) {
      jwtCache.delete(sha256Hex(ctx.grant));
      authHeader = { Authorization: `Bearer ${await resolveUserAuth(ctx.grant)}` };
    } else {
      token = null;
      await login();
      authHeader = { Authorization: `Bearer ${token}` };
    }
    res = await fetch(url, { headers: authHeader });
  }
  const data = await res.json().catch(() => null);
  if (!data) throw new Error(`接口响应解析失败: HTTP ${res.status} ${pathname}`);
  if (data.code !== undefined && data.code !== 200) {
    throw new Error(`EDOP 接口错误(${pathname}): ${data.msg}`);
  }
  return data;
}

/* ---------------- 入参校验与输出工具 ---------------- */

const DATE_RE = /^\d{4}-\d{2}-\d{2}$/;

function assertDateRange(startDate, endDate) {
  if (startDate && !DATE_RE.test(startDate)) throw new Error("startDate 格式须为 YYYY-MM-DD");
  if (endDate && !DATE_RE.test(endDate)) throw new Error("endDate 格式须为 YYYY-MM-DD");
  if (startDate && endDate) {
    const days = (new Date(endDate) - new Date(startDate)) / 86400000;
    if (days < 0) throw new Error("endDate 不能早于 startDate");
    if (days > 92) throw new Error("日期范围上限 92 天，请缩小范围");
  }
}

function pageParams(pageNum, pageSize) {
  return {
    pageNum: Math.min(Math.max(Number(pageNum) || 1, 1), 999),
    pageSize: Math.min(Math.max(Number(pageSize) || 20, 1), 100),
  };
}

/** 深度过滤：仅保留 JSON 文本中包含关键词的数组元素（BI 趋势按指标编码裁剪） */
function deepFilter(value, keyword) {
  if (Array.isArray(value)) {
    return value.map((item) => deepFilter(item, keyword)).filter((item) => item !== undefined);
  }
  if (value && typeof value === "object") {
    const out = {};
    let hit = false;
    for (const [k, v] of Object.entries(value)) {
      const fv = deepFilter(v, keyword);
      if (fv !== undefined) {
        out[k] = fv;
        hit = true;
      }
    }
    if (hit) return out;
    return JSON.stringify(value).includes(keyword) ? value : undefined;
  }
  return String(value).includes(keyword) ? value : undefined;
}

function jsonResult(tool, data) {
  audit(tool, "ok");
  return { content: [{ type: "text", text: JSON.stringify(data, null, 2) }] };
}
function errorResult(tool, err) {
  audit(tool, `fail ${err.message}`);
  return { content: [{ type: "text", text: `查询失败: ${err.message}` }], isError: true };
}

/* ---------------- MCP Server 构建（按 manifest 注册工具） ---------------- */

function buildMcpServer() {
  const server = new McpServer({ name: "edop-mcp-gateway", version: "2.0.0" });
  const T = manifest.tools;

  /* 1. BI 指标（专用逻辑：字典/总览双模式 + 趋势裁剪） */
  const bi = T.get_bi_metrics;
  server.registerTool("get_bi_metrics", {
    title: "EDOP BI 指标查询",
    description:
      "查询 EDOP 企业数字化运营平台的决策支持指标（BI 驾驶舱）。" +
      "默认返回最新快照总览（含各模块核心指标与预警横幅）；" +
      "传 metricCode 可按指标编码过滤趋势数据；传 queryDict=true 查询指标字典（编码、口径、单位）。" +
      "不知道指标编码时先用 queryDict=true 查字典。",
    inputSchema: {
      metricCode: z.string().optional().describe("指标编码，如 arrival_rate，可先查字典获取"),
      metricName: z.string().optional().describe("指标名称关键字（查字典时用）"),
      queryDict: z.boolean().optional().describe("true 时查询指标字典而非指标值"),
    },
  }, async ({ metricCode, metricName, queryDict }) => {
    try {
      if (queryDict) {
        const data = await apiGet(bi.endpoints.dict, { metricName: metricName || "", ...pageParams(1, 100) });
        return jsonResult("get_bi_metrics", { source: "dict", total: data.total, rows: data.rows });
      }
      const data = await apiGet(bi.endpoints.overview);
      const overview = data.data ?? data;
      if (!metricCode) return jsonResult("get_bi_metrics", overview);
      const filtered = deepFilter(overview, metricCode);
      if (!filtered) {
        return jsonResult("get_bi_metrics", {
          hint: `未在最新总览中找到指标 ${metricCode}，请用 queryDict=true 查询有效指标编码`,
        });
      }
      return jsonResult("get_bi_metrics", filtered);
    } catch (e) {
      return errorResult("get_bi_metrics", e);
    }
  });

  /* 2-4. 通用列表型工具（query_inventory / query_sales_orders / query_purchase_orders） */
  for (const name of Object.keys(T)) {
    const t = T[name];
    if (t.type !== "table") continue;

    const schema = {};
    for (const gk of Object.keys(t.params)) schema[gk] = z.string().optional();
    if (t.dateRange) {
      schema.startDate = z.string().optional().describe("起始日期 YYYY-MM-DD");
      schema.endDate = z.string().optional().describe("结束日期 YYYY-MM-DD，范围上限 92 天");
    }
    schema.pageNum = z.number().optional().describe("页码，默认 1");
    schema.pageSize = z.number().optional().describe("每页行数，默认 20，上限 100");

    server.registerTool(name, {
      title: t.title || name,
      description: t.description || `查询 EDOP 平台数据：${t.endpoint}`,
      inputSchema: schema,
    }, async (args) => {
      try {
        if (t.dateRange) assertDateRange(args.startDate, args.endDate);
        const params = {};
        for (const [gk, bk] of Object.entries(t.params)) {
          if (args[gk] !== undefined && args[gk] !== null && args[gk] !== "") params[bk] = args[gk];
        }
        Object.assign(params, pageParams(args.pageNum, args.pageSize));
        const data = await apiGet(t.endpoint, params);
        return jsonResult(name, { total: data.total, rows: data.rows });
      } catch (e) {
        return errorResult(name, e);
      }
    });
  }

  /* 5. 平台总览（聚合型：并行调用，单点失败降级） */
  const agg = T.get_platform_overview;
  server.registerTool("get_platform_overview", {
    title: "EDOP 平台经营总览",
    description:
      "聚合查询 EDOP 平台当日经营总览：BI 指标总览 + 仓库统计 + 营销看板 + 采购看板。" +
      '用户问"平台整体情况/今天经营怎么样"时优先使用。',
    inputSchema: {},
  }, async () => {
    try {
      const targets = Object.entries(agg.endpoints);
      const results = await Promise.allSettled(targets.map(([, p]) => apiGet(p)));
      const out = {};
      const errors = [];
      results.forEach((r, i) => {
        const [name] = targets[i];
        if (r.status === "fulfilled") out[name] = r.value.data ?? r.value;
        else errors.push(`${name}: ${r.reason.message}`);
      });
      if (errors.length) out._partialErrors = errors;
      return jsonResult("get_platform_overview", out);
    } catch (e) {
      return errorResult("get_platform_overview", e);
    }
  });

  return server;
}

/* ---------------- 授权确认页（仿 WorkBuddy 官方授权页样式，浅色主题） ---------------- */

function consentPageHtml(ctx) {
  const ctxJson = JSON.stringify(ctx).replace(/</g, "\\u003c").replace(/>/g, "\\u003e");
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>连接 EDOP 企业数字化运营平台</title>
<style>
  * { margin:0; padding:0; box-sizing:border-box; }
  body { font-family:-apple-system,"PingFang SC","Microsoft YaHei",sans-serif;
         background:#f5f6f7; color:#1f2329; display:flex; justify-content:center;
         padding:48px 16px; min-height:100vh; }
  .wrap { width:100%; max-width:420px; }
  .icons { display:flex; align-items:center; justify-content:center; gap:14px; margin-bottom:24px; }
  .icon { width:64px; height:64px; border-radius:16px; display:flex; align-items:center;
          justify-content:center; font-weight:700; font-size:15px; color:#fff;
          box-shadow:0 4px 12px rgba(0,0,0,.08); }
  .icon.edop { background:linear-gradient(135deg,#2563eb,#1e40af); }
  .icon.wb   { background:linear-gradient(135deg,#34d399,#059669); }
  .arrow { color:#9ca3af; font-size:20px; letter-spacing:2px; }
  h1 { text-align:center; font-size:20px; margin-bottom:10px; color:#1f2329; }
  .desc { text-align:center; font-size:13px; color:#6b7280; line-height:1.7; margin-bottom:22px; }
  .perm-card { background:#f0f1f3; border-radius:12px; padding:18px 20px; margin-bottom:22px; }
  .perm-card h3 { font-size:13px; color:#6b7280; font-weight:500; margin-bottom:14px; }
  .perm { display:flex; gap:12px; margin-bottom:14px; }
  .perm:last-child { margin-bottom:0; }
  .perm .ic { font-size:16px; line-height:1.4; }
  .perm b { font-size:14px; color:#1f2329; display:block; margin-bottom:2px; }
  .perm span { font-size:12px; color:#6b7280; line-height:1.6; }
  #goBtn { width:100%; padding:14px; background:#111827; color:#fff; border:none;
           border-radius:10px; font-size:15px; font-weight:600; cursor:pointer; }
  #goBtn:hover { background:#1f2937; }
  #loginForm { display:none; }
  #loginForm.show { display:block; }
  .field { margin-bottom:12px; }
  .field input { width:100%; padding:12px 14px; border:1px solid #d1d5db; border-radius:8px;
                 font-size:14px; outline:none; transition:border .15s; }
  .field input:focus { border-color:#2563eb; }
  #authBtn { width:100%; padding:13px; background:#111827; color:#fff; border:none;
             border-radius:10px; font-size:15px; font-weight:600; cursor:pointer; margin-top:4px; }
  #authBtn:disabled { background:#9ca3af; cursor:not-allowed; }
  #err { display:none; background:#fef2f2; color:#dc2626; border:1px solid #fecaca;
         border-radius:8px; padding:10px 14px; font-size:13px; margin-bottom:12px; }
  .foot { display:flex; align-items:center; justify-content:center; gap:6px;
          margin-top:22px; font-size:12px; color:#9ca3af; }
  .hint { text-align:center; font-size:12px; color:#9ca3af; margin-top:12px; }
</style>
</head>
<body>
<div class="wrap">
  <div class="icons">
    <div class="icon edop">EDOP</div>
    <div class="arrow">&raquo;&raquo;&raquo;</div>
    <div class="icon wb">WB</div>
  </div>
  <h1>连接 EDOP 企业数字化运营平台</h1>
  <p class="desc">授权后，WorkBuddy 将以<b>您的身份</b>读取 EDOP 业务数据，<br>为您提供智能查询与经营问答服务。</p>
  <div class="perm-card">
    <h3>即将获取以下权限</h3>
    <div class="perm"><div class="ic">&#128065;</div><div><b>查询业务数据（只读）</b><span>库存、销售订单、采购订单、BI 指标与平台经营总览</span></div></div>
    <div class="perm"><div class="ic">&#128274;</div><div><b>数据范围与您一致</b><span>仅能查询您账号权限范围内的数据，不做任何写入操作</span></div></div>
  </div>
  <button id="goBtn" onclick="showLogin()">立即前往授权</button>
  <div id="loginForm">
    <div id="err"></div>
    <div class="field"><input id="username" placeholder="EDOP 账号" autocomplete="username"></div>
    <div class="field"><input id="password" type="password" placeholder="EDOP 密码" autocomplete="current-password"></div>
    <button id="authBtn" onclick="doAuth()">授权并连接</button>
    <p class="hint">使用您平时登录 EDOP 平台的账号，密码不会留存</p>
  </div>
  <div class="foot">&#10003; WorkBuddy 仅在您执行 AI 任务时调用数据，严格保护隐私</div>
</div>
<script>
const CTX = ${ctxJson};
function showLogin(){
  document.getElementById('goBtn').style.display='none';
  document.getElementById('loginForm').classList.add('show');
  document.getElementById('username').focus();
}
async function doAuth(){
  const btn = document.getElementById('authBtn');
  const err = document.getElementById('err');
  err.style.display='none';
  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value;
  if(!username || !password){ err.textContent='请输入 EDOP 账号和密码'; err.style.display='block'; return; }
  btn.disabled = true; btn.textContent = '正在验证…';
  try {
    const r = await fetch('/oauth/authorize', {
      method:'POST', headers:{'Content-Type':'application/json'},
      body: JSON.stringify({ username, password, ctx: CTX })
    });
    const data = await r.json().catch(()=>null);
    if (data && data.code === 200 && data.redirect) {
      btn.textContent = '授权成功，正在返回 WorkBuddy…';
      window.location.href = data.redirect;
      return;
    }
    err.textContent = (data && data.msg) || '授权失败，请重试';
    err.style.display='block';
  } catch(e) {
    err.textContent = '网络异常，请稍后重试';
    err.style.display='block';
  }
  btn.disabled = false; btn.textContent = '授权并连接';
}
document.getElementById('password').addEventListener('keydown', e => { if(e.key==='Enter') doAuth(); });
</script>
</body>
</html>`;
}

/* ---------------- 自助授权页（小客户 HTTP 路线） ---------------- */

function grantPageHtml() {
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>EDOP 连接器授权</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: -apple-system, "Segoe UI", "Microsoft YaHei", sans-serif; background: #f5f6f8; color: #1a1a1a; display: flex; justify-content: center; padding: 48px 16px; min-height: 100vh; }
  .card { background: #fff; border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,.08); width: 420px; padding: 36px 32px; }
  .brand { display: flex; align-items: center; justify-content: center; gap: 12px; margin-bottom: 8px; }
  .logo { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 700; font-size: 15px; }
  .logo.edop { background: #1f3a5f; } .logo.wb { background: #111; }
  .arrow { color: #999; font-size: 18px; }
  h1 { font-size: 18px; text-align: center; margin-bottom: 6px; }
  .sub { font-size: 13px; color: #666; text-align: center; margin-bottom: 24px; line-height: 1.6; }
  label { display: block; font-size: 13px; color: #444; margin: 14px 0 6px; }
  input { width: 100%; padding: 11px 12px; border: 1px solid #d9dce3; border-radius: 8px; font-size: 14px; outline: none; }
  input:focus { border-color: #1f3a5f; }
  button { width: 100%; margin-top: 22px; padding: 12px; background: #111; color: #fff; border: none; border-radius: 8px; font-size: 15px; font-weight: 600; cursor: pointer; }
  button:disabled { opacity: .5; cursor: not-allowed; }
  .msg { font-size: 13px; margin-top: 12px; text-align: center; min-height: 18px; }
  .msg.err { color: #d33; } .msg.ok { color: #0a7d32; }
  .result { display: none; margin-top: 20px; background: #f0f7f1; border: 1px solid #bfe3c8; border-radius: 10px; padding: 16px; }
  .result .warn { font-size: 12px; color: #a05a00; background: #fff7e6; border-radius: 6px; padding: 6px 10px; margin-bottom: 12px; }
  .token { word-break: break-all; font-family: Consolas, monospace; font-size: 12px; background: #fff; border: 1px solid #d9dce3; border-radius: 6px; padding: 10px; margin: 8px 0 12px; }
  .copy { background: #1f3a5f; margin-top: 0; padding: 9px; font-size: 13px; }
  .snippet { font-family: Consolas, monospace; font-size: 11px; background: #141821; color: #d5e0ee; border-radius: 8px; padding: 12px; margin-top: 12px; white-space: pre; overflow-x: auto; }
  .foot { font-size: 11px; color: #999; text-align: center; margin-top: 20px; line-height: 1.6; }
</style>
</head>
<body>
<div class="card">
  <div class="brand">
    <div class="logo edop">EDOP</div><div class="arrow">⇄</div><div class="logo wb">WB</div>
  </div>
  <h1>连接 WorkBuddy 助手</h1>
  <div class="sub">使用您的 EDOP 账号登录，获取连接器专属令牌。<br/>数据范围与您本人在 EDOP 中的权限完全一致（只读查询）。</div>
  <form id="f">
    <label>EDOP 账号</label>
    <input id="u" autocomplete="username" placeholder="请输入登录账号" required />
    <label>密码</label>
    <input id="p" type="password" autocomplete="current-password" placeholder="请输入密码" required />
    <button id="btn" type="submit">获取连接令牌</button>
    <div class="msg" id="m"></div>
  </form>
    <div class="result" id="r">
    <div class="warn">⚠️ 以下配置含您的专属令牌，仅显示这一次，请立即复制保存（令牌有效期 90 天自动续期，管理员可随时吊销）</div>
    <div style="font-size:12px;color:#555;margin:12px 0 6px">使用方法：WorkBuddy → 自定义连接器 → 配置 MCP → 整段粘贴以下内容 → 保存启用</div>
    <div class="snippet" id="s"></div>
    <button class="copy" id="c" type="button">复制完整配置</button>
    <div style="font-size:12px;color:#555;margin-top:10px">（若编辑器中已有其他连接器条目，请只把 "edop-assistant": {...} 这一块合并进去）</div>
    <div style="font-size:12px;color:#555;margin-top:6px">您的专属令牌（仅用于向管理员报修时核对身份）</div>
    <div class="token" id="t"></div>
  </div>
  <div class="foot">EDOP 仅向您本人授权的数据范围提供只读查询能力，<br/>平台管理员与 AI 助手均不会索要您的账号密码。</div>
</div>
<script>
const f=document.getElementById('f'),m=document.getElementById('m'),btn=document.getElementById('btn');
f.addEventListener('submit',async(e)=>{
  e.preventDefault();
  btn.disabled=true;btn.textContent='验证中…';m.className='msg';m.textContent='';
  try{
    const r=await fetch('/grant/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username:u.value,password:p.value})});
    const j=await r.json();
    if(!r.ok||j.code!==200){m.className='msg err';m.textContent=j.msg||'登录失败';btn.disabled=false;btn.textContent='获取连接令牌';return;}
    m.className='msg ok';m.textContent='授权成功！';
    f.style.display='none';
    const rEl=document.getElementById('r');
    const cfg=JSON.stringify({mcpServers:{"edop-assistant":{type:"streamablehttp",url:location.protocol+"//"+location.host+"/mcp",headers:{Authorization:"Bearer "+j.grantToken}}}},null,2);
    document.getElementById('s').textContent=cfg;
    document.getElementById('t').textContent=j.grantToken;
    rEl.style.display='block';
    document.getElementById('c').onclick=()=>{navigator.clipboard.writeText(cfg);document.getElementById('c').textContent='✓ 已复制，去 WorkBuddy 粘贴即可';};
  }catch(err){m.className='msg err';m.textContent='网络异常，请重试';btn.disabled=false;btn.textContent='获取连接令牌';}
});
</script>
</body>
</html>`;
}

/* ---------------- 模式启动 ---------------- */

if (MODE === "http") {
  if (!config.apiKey) {
    console.error("[edop-gateway] http 模式必须在 profile 中配置 apiKey");
    process.exit(1);
  }
  const app = express();
  app.use(express.json({ limit: "2mb" }));
  app.use(express.urlencoded({ extended: false }));

  /* ---------------- IP 白名单（公网/云外网 IP 部署的硬底线） ----------------
   * 配置来源（优先级）：环境变量 EDOP_ALLOW_IPS > profile.allowedIps
   * 格式：逗号分隔，支持单 IP 与 CIDR 段（仅 IPv4），如 "1.2.3.4,10.0.0.0/8"
   * 未配置 = 不限制（仅限本机/可信内网开发使用）；/health 保持开放供探活
   */
  const ALLOWED_IPS = String(
    process.env.EDOP_ALLOW_IPS ||
    (Array.isArray(config.allowedIps) ? config.allowedIps.join(",") : "") ||
    ""
  )
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);

  function ipToLong(ip) {
    const m = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/.exec(String(ip).replace(/^::ffff:/, ""));
    if (!m) return null;
    const [a, b, c, d] = m.slice(1).map(Number);
    if ([a, b, c, d].some((n) => n > 255)) return null;
    return ((a << 24) | (b << 16) | (c << 8) | d) >>> 0;
  }

  function ipAllowed(ip) {
    if (ALLOWED_IPS.length === 0) return true;
    const bare = String(ip || "").replace(/^::ffff:/, "");
    for (const rule of ALLOWED_IPS) {
      if (rule.includes("/")) {
        const [net, bitsStr] = rule.split("/");
        const bits = parseInt(bitsStr, 10);
        const a = ipToLong(bare);
        const b = ipToLong(net);
        if (a !== null && b !== null && bits > 0 && bits <= 32 && ((a ^ b) >>> (32 - bits)) === 0) return true;
      } else if (bare === rule) {
        return true;
      }
    }
    return false;
  }

  app.use((req, res, next) => {
    if (req.path === "/health") return next();
    if (!ipAllowed(req.ip)) {
      audit("ip-guard", `rejected ip=${req.ip} path=${req.path}`);
      return res.status(403).json({ error: "forbidden", msg: "来源 IP 不在白名单内，请联系管理员" });
    }
    next();
  });

  app.get("/health", (_req, res) => res.json({ ok: true, profile: PROFILE, mode: "http" }));

  /* ---------------- OAuth 2.1 授权服务器（WorkBuddy 自动授权流） ----------------
   * 依据 open.workbuddy.cn/docs/connector「MCP OAuth flow」：
   *   401 → RFC 9728 元数据发现 → RFC 7591 动态注册 → 浏览器授权 → PKCE 换 token
   * access_token / refresh_token = EDOP grant_token（复用逐用户鉴权链路，后端零改动）
   */
  const oauthCodes = new Map();        // code → {grantToken, challenge, redirectUri, state, clientId, expireAt}
  const oauthRegistrations = new Map(); // clientId → {redirectUris, name}
  const loginThrottle = new Map();      // ip → {count, resetAt}
  const CODE_TTL_MS = 10 * 60 * 1000;
  const AUTHZ_SCOPE = "edop.read";

  function externalBase(req) {
    const proto = req.get("x-forwarded-proto") || req.protocol || "http";
    const host = req.get("host") || `127.0.0.1:${HTTP_PORT}`;
    return `${proto}://${host}`;
  }

  function pkceS256(verifier) {
    return crypto.createHash("sha256").update(String(verifier), "ascii").digest("base64url");
  }

  function redirectAllowed(clientId, redirectUri) {
    const reg = oauthRegistrations.get(clientId);
    if (reg && Array.isArray(reg.redirectUris) && reg.redirectUris.includes(redirectUri)) return true;
    // 兜底：WorkBuddy 私有 scheme 与本机回环地址
    if (redirectUri.startsWith("workbuddy://")) return true;
    if (/^http:\/\/127\.0\.0\.1:\d+/.test(redirectUri) || /^http:\/\/localhost:\d+/.test(redirectUri)) return true;
    return false;
  }

  function oauthErrorRedirect(redirectUri, state, error) {
    const u = new URL(redirectUri);
    if (error) u.searchParams.set("error", error);
    if (state) u.searchParams.set("state", state);
    return u.toString();
  }

  function buildRedirect(redirectUri, params) {
    const u = new URL(redirectUri);
    for (const [k, v] of Object.entries(params)) {
      if (v !== undefined && v !== null && v !== "") u.searchParams.set(k, v);
    }
    return u.toString();
  }

  function cleanupOauth() {
    const now = Date.now();
    for (const [k, v] of oauthCodes) if (v.expireAt < now) oauthCodes.delete(k);
    for (const [k, v] of loginThrottle) if (v.resetAt < now) loginThrottle.delete(k);
  }

  // RFC 9728 受保护资源元数据
  app.get("/.well-known/oauth-protected-resource", (req, res) => {
    res.json({
      resource: `${externalBase(req)}/mcp`,
      authorization_servers: [externalBase(req)],
      scopes_supported: [AUTHZ_SCOPE],
      bearer_methods: ["header"],
    });
  });

  // 授权服务器元数据（OAuth 2.1 Authorization Server Metadata）
  app.get("/.well-known/oauth-authorization-server", (req, res) => {
    const base = externalBase(req);
    res.json({
      issuer: base,
      authorization_endpoint: `${base}/oauth/authorize`,
      token_endpoint: `${base}/oauth/token`,
      registration_endpoint: `${base}/oauth/register`,
      response_types: ["code"],
      grant_types: ["authorization_code", "refresh_token"],
      code_challenge_methods: ["S256"],
      token_endpoint_auth_methods: ["none"],
      scopes_supported: [AUTHZ_SCOPE],
    });
  });

  // RFC 7591 动态客户端注册（公共客户端，回显 redirect_uris）
  app.post("/oauth/register", (req, res) => {
    const { redirect_uris, client_name } = req.body || {};
    if (!Array.isArray(redirect_uris) || redirect_uris.length === 0 || !redirect_uris.every((u) => typeof u === "string" && u)) {
      return res.status(400).json({ error: "invalid_client_metadata", error_description: "redirect_uris required" });
    }
    const clientId = `edop-${crypto.randomBytes(8).toString("hex")}`;
    oauthRegistrations.set(clientId, { redirectUris: redirect_uris, name: String(client_name || "WorkBuddy") });
    audit("oauth-register", `client=${clientId}`);
    res.status(201).json({
      client_id: clientId,
      client_id_issued_at: Math.floor(Date.now() / 1000),
      client_name: String(client_name || "WorkBuddy"),
      redirect_uris,
      grant_types: ["authorization_code", "refresh_token"],
      response_types: ["code"],
      token_endpoint_auth_method: "none",
    });
  });

  // 授权端点（GET）：校验参数后返回授权确认页（仿 WorkBuddy 官方授权页样式）
  app.get("/oauth/authorize", (req, res) => {
    const { response_type, client_id, redirect_uri, state, code_challenge, code_challenge_method, scope } = req.query;
    const deny = (error) => {
      if (redirect_uri && client_id && redirectAllowed(String(client_id), String(redirect_uri))) {
        return res.redirect(oauthErrorRedirect(String(redirect_uri), state, error));
      }
      return res.status(400).json({ error });
    };
    if (response_type !== "code") return deny("unsupported_response_type");
    if (!client_id || !redirect_uri) return res.status(400).json({ error: "invalid_request" });
    if (!redirectAllowed(String(client_id), String(redirect_uri))) return res.status(400).json({ error: "invalid_request", error_description: "redirect_uri not registered" });
    if (!code_challenge || code_challenge_method !== "S256") return deny("invalid_request", "PKCE S256 required");
    const ctx = { clientId: String(client_id), redirectUri: String(redirect_uri), state: String(state || ""), challenge: String(code_challenge), scope: String(scope || AUTHZ_SCOPE) };
    res.type("html").send(consentPageHtml(ctx));
  });

  // 授权端点（POST）：用户提交 EDOP 账号密码 → 换 grant_token → 生成一次性授权码 → 回跳
  app.post("/oauth/authorize", (req, res) => {
    cleanupOauth();
    const ip = req.ip || "unknown";
    const th = loginThrottle.get(ip) || { count: 0, resetAt: Date.now() + 10 * 60 * 1000 };
    th.count += 1;
    loginThrottle.set(ip, th);
    if (th.count > 10) return res.status(429).json({ error: "slow_down", msg: "尝试过于频繁，请稍后再试" });

    const { username, password, ctx } = req.body || {};
    if (!username || !password || !ctx || !ctx.clientId || !ctx.redirectUri || !ctx.challenge) {
      return res.status(400).json({ error: "invalid_request" });
    }
    if (!redirectAllowed(String(ctx.clientId), String(ctx.redirectUri))) {
      return res.status(400).json({ error: "invalid_request", msg: "redirect_uri 未注册" });
    }
    fetch(`${config.baseUrl}/edop/connector/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password, clientDesc: `WorkBuddy连接器(${ctx.clientId})` }),
    })
      .then((r) => r.json().catch(() => null))
      .then((data) => {
        if (!data || data.code !== 200 || !data.grantToken) {
          audit("oauth-login", `fail user=${username}`);
          return res.status(401).json({ error: "invalid_credentials", msg: (data && data.msg) || "账号或密码错误" });
        }
        const code = crypto.randomBytes(24).toString("base64url");
        oauthCodes.set(code, {
          grantToken: data.grantToken,
          challenge: String(ctx.challenge),
          redirectUri: String(ctx.redirectUri),
          state: String(ctx.state || ""),
          clientId: String(ctx.clientId),
          expireAt: Date.now() + CODE_TTL_MS,
        });
        audit("oauth-login", `ok user=${username} code_issued`);
        const redirect = buildRedirect(ctx.redirectUri, { code, state: ctx.state });
        res.json({ code: 200, redirect });
      })
      .catch((e) => {
        audit("oauth-login", `error user=${username} backend_unreachable ${e.message}`);
        res.status(502).json({ error: "server_error", msg: "EDOP 平台不可达，请联系管理员" });
      });
  });

  // Token 端点：authorization_code（PKCE S256 校验）+ refresh_token 两种 grant
  app.post("/oauth/token", (req, res) => {
    const { grant_type, code, code_verifier, redirect_uri, client_id, refresh_token } = req.body || {};
    if (grant_type === "authorization_code") {
      const rec = oauthCodes.get(String(code || ""));
      if (!rec) return res.status(400).json({ error: "invalid_grant", error_description: "code invalid/expired/used" });
      oauthCodes.delete(String(code)); // 一次性
      if (Date.now() > rec.expireAt) return res.status(400).json({ error: "invalid_grant" });
      if (client_id && client_id !== rec.clientId) return res.status(400).json({ error: "invalid_grant" });
      if (redirect_uri && redirect_uri !== rec.redirectUri) return res.status(400).json({ error: "invalid_grant" });
      if (!code_verifier || pkceS256(code_verifier) !== rec.challenge) {
        return res.status(400).json({ error: "invalid_grant", error_description: "PKCE verification failed" });
      }
      audit("oauth-token", `ok client=${rec.clientId} grant_issued`);
      return res.json({
        access_token: rec.grantToken,
        token_type: "Bearer",
        expires_in: 3600,
        refresh_token: rec.grantToken,
        scope: AUTHZ_SCOPE,
      });
    }
    if (grant_type === "refresh_token") {
      if (!refresh_token) return res.status(400).json({ error: "invalid_request" });
      // 用 EDOP exchange 校验授权是否仍有效（吊销/停用则拒绝刷新）
      fetch(`${config.baseUrl}/edop/connector/auth/exchange`, {
        method: "POST",
        headers: { Authorization: `Bearer ${refresh_token}` },
      })
        .then((r) => r.json().catch(() => null))
        .then((data) => {
          if (!data || data.code !== 200) {
            audit("oauth-refresh", `fail invalid_grant`);
            return res.status(400).json({ error: "invalid_grant", error_description: "authorization revoked or expired" });
          }
          audit("oauth-refresh", `ok user=${data.userName ?? "?"}`);
          res.json({
            access_token: refresh_token,
            token_type: "Bearer",
            expires_in: 3600,
            refresh_token,
            scope: AUTHZ_SCOPE,
          });
        })
        .catch(() => res.status(502).json({ error: "server_error" }));
      return;
    }
    res.status(400).json({ error: "unsupported_grant_type" });
  });

  /* ---------------- 自助授权页（小客户 HTTP 路线，无需证书） ----------------
   * GET  /grant       → 授权页（EDOP 账号密码登录 → 展示 grant_token，仅一次）
   * POST /grant/login → 代理后端 /edop/connector/auth/login（复用限流，密码不落日志）
   * 用途：客户内网纯 HTTP 部署时，用户手工把 token 粘贴进连接器 headers
   */
  app.get("/grant", (_req, res) => {
    res.type("html").send(grantPageHtml());
  });

  app.post("/grant/login", (req, res) => {
    cleanupOauth();
    const ip = req.ip || "unknown";
    const th = loginThrottle.get(ip) || { count: 0, resetAt: Date.now() + 10 * 60 * 1000 };
    th.count += 1;
    loginThrottle.set(ip, th);
    if (th.count > 10) return res.status(429).json({ msg: "尝试过于频繁，请稍后再试" });

    const { username, password } = req.body || {};
    if (!username || !password) return res.status(400).json({ msg: "请输入账号和密码" });
    fetch(`${config.baseUrl}/edop/connector/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password, clientDesc: "WorkBuddy连接器(手工授权)" }),
    })
      .then((r) => r.json().catch(() => null))
      .then((data) => {
        if (!data || data.code !== 200 || !data.grantToken) {
          audit("grant-login", `fail user=${username}`);
          return res.status(401).json({ msg: (data && data.msg) || "账号或密码错误" });
        }
        audit("grant-login", `ok user=${username} token_issued`);
        res.json({ code: 200, grantToken: data.grantToken, userName: data.userName || username });
      })
      .catch((e) => {
        audit("grant-login", `error user=${username} backend_unreachable ${e.message}`);
        res.status(502).json({ msg: "EDOP 平台不可达，请联系管理员" });
      });
  });

  /**
   * 双鉴权：
   *   x-api-key（或 Bearer+apiKey）            → service 模式（服务账号，全平台视角）
   *   Authorization: Bearer <grant_token>      → user 模式（逐用户，@DataScope 按人裁剪）
   *   无凭证 / 凭证无效                        → 401 + RFC 9728 WWW-Authenticate（触发 WorkBuddy OAuth 自动流）
   */
  const keyCheck = (req, res, next) => {
    const apiKey = req.header("x-api-key") || "";
    const rawAuth = req.header("authorization") || "";
    const bearer = rawAuth.replace(/^Bearer\s+/i, "");
    if (apiKey && apiKey === config.apiKey) {
      return requestCtx.run({ mode: "service" }, next);
    }
    if (bearer && bearer === config.apiKey) {
      return requestCtx.run({ mode: "service" }, next);
    }
    if (bearer && bearer.length >= 32) {
      return requestCtx.run({ mode: "user", grant: bearer }, next);
    }
    audit("http-auth", `rejected ip=${req.ip}`);
    const challenge = `Bearer resource_metadata="${externalBase(req)}/.well-known/oauth-protected-resource"${bearer ? ', error="invalid_token"' : ""}`;
    res.setHeader("WWW-Authenticate", challenge);
    return res.status(401).json({ error: "invalid credentials (OAuth 2.1 flow or x-api-key)" });
  };

  // stateless：每个请求独立 server + transport（官方推荐模式）
  app.post("/mcp", keyCheck, async (req, res) => {
    try {
      const server = buildMcpServer();
      const transport = new StreamableHTTPServerTransport({ sessionIdGenerator: undefined });
      res.on("close", () => {
        transport.close();
        server.close();
      });
      await server.connect(transport);
      await transport.handleRequest(req, res, req.body);
    } catch (e) {
      audit("http-mcp", `fail ${e.message}`);
      if (!res.headersSent) res.status(500).json({ error: "internal error" });
    }
  });
  const reject = (_req, res) => res.status(405).json({ error: "method not allowed (stateless mode)" });
  app.get("/mcp", reject);
  app.delete("/mcp", reject);

  const HTTP_PORT = Number(process.env.EDOP_HTTP_PORT) || config.httpPort || 3000;
  const HTTP_HOST = process.env.EDOP_HTTP_HOST || config.httpHost || "0.0.0.0";

  // HTTPS 模式（EDOP_HTTPS=1）：OAuth 端点官方要求 HTTPS；开发用自签证书 certs/{key,cert}.pem
  const useHttps = process.env.EDOP_HTTPS === "1" || !!config.https;
  if (useHttps) {
    const keyPath = process.env.EDOP_HTTPS_KEY || config.httpsKey || path.join(__dirname, "certs", "key.pem");
    const certPath = process.env.EDOP_HTTPS_CERT || config.httpsCert || path.join(__dirname, "certs", "cert.pem");
    const tlsOpts = { key: fs.readFileSync(keyPath), cert: fs.readFileSync(certPath) };
    https.createServer(tlsOpts, app).listen(HTTP_PORT, HTTP_HOST, () => {
      audit("startup", `https mode, profile=${PROFILE}, port=${HTTP_PORT}`);
      console.error(`[edop-gateway] https 模式已启动: https://${HTTP_HOST}:${HTTP_PORT}/mcp (profile=${PROFILE})`);
    });
  } else {
    app.listen(HTTP_PORT, HTTP_HOST, () => {
      audit("startup", `http mode, profile=${PROFILE}, port=${HTTP_PORT}`);
      console.error(`[edop-gateway] http 模式已启动: http://${HTTP_HOST}:${HTTP_PORT}/mcp (profile=${PROFILE})`);
    });
  }
} else {
  await buildMcpServer().connect(new StdioServerTransport());
  audit("startup", `stdio mode, profile=${PROFILE}, target=${config.baseUrl}`);
}

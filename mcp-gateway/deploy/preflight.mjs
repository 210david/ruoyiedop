/**
 * EDOP MCP Gateway 启动前置校验：node preflight.mjs
 * 退出码 0 = 通过，非 0 = 阻断启动
 *
 * 为什么要用 Node 而不是 bat 的 findstr 做校验：
 *   profiles/*.json 是 UTF-8，而 findstr 按控制台代码页（GBK）读取，
 *   中文占位符永远匹配不上，会导致占位符配置被放行启动。
 *
 * 通用性：所有分支仓共用本文件，按 EDOP_PROFILE 读取各自的 profiles/<name>.json，
 *        不写死任何客户名或端口。
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, "..");
const profilesDir = path.join(root, "profiles");

/**
 * 解析实际使用的 profile：
 *   1) 显式设置 EDOP_PROFILE -> 用它（找不到则报错，便于发现配置问题）
 *   2) 未设置 -> 若 profiles/ 下只有一个真实配置（排除 *.example.json）则自动采用，
 *      避免"未设环境变量就回退 standard、而分支仓根本没有 standard.json"的死角
 *   3) 未设置且有多个 -> 报错并列出，要求显式指定
 */
function resolveProfile() {
  const explicit = process.env.EDOP_PROFILE;
  if (explicit) return { name: explicit, explicit: true };

  let real = [];
  try {
    real = fs.readdirSync(profilesDir)
      .filter((f) => f.endsWith(".json") && !f.endsWith(".example.json"))
      .map((f) => f.slice(0, -".json".length));
  } catch { /* 目录不存在，交给后续校验报错 */ }

  if (real.length === 1) return { name: real[0], explicit: false };
  if (real.length > 1) return { name: null, explicit: false, candidates: real };
  return { name: "standard", explicit: false };
}

const resolved = resolveProfile();
const profileName = resolved.name;

// --print-profile：只输出解析到的 profile 名，供 bat 设置 EDOP_PROFILE 用
if (process.argv.includes("--print-profile")) {
  if (profileName) console.log(profileName);
  else console.error("profiles/ 下存在多个配置，请设置 EDOP_PROFILE 指定其中之一：" + resolved.candidates.join(", "));
  process.exit(profileName ? 0 : 1);
}

// --print-port：只输出实际生效的端口，供 bat 做端口探活（避免脚本写死端口与配置脱节）
if (process.argv.includes("--print-port")) {
  try {
    const c = JSON.parse(fs.readFileSync(path.join(profilesDir, `${profileName}.json`), "utf8"));
    console.log(Number(process.env.EDOP_HTTP_PORT) || c.httpPort || 3000);
    process.exit(0);
  } catch {
    console.log(3000);
    process.exit(0);
  }
}

if (resolved.candidates) {
  console.error("[preflight][FAIL] profiles/ 下存在多个配置，请用 EDOP_PROFILE 指定：" + resolved.candidates.join(", "));
  process.exit(1);
}

const profilePath = path.join(profilesDir, `${profileName}.json`);

const errors = [];
const warns = [];
const fail = (m) => errors.push(m);
const warn = (m) => warns.push(m);

/* ---------- 1. 配置文件存在且可解析 ---------- */
let cfg = null;
if (!fs.existsSync(profilePath)) {
  const example = path.join(root, "profiles", "standard.example.json");
  fail(`缺少配置文件 profiles/${profileName}.json`);
  if (fs.existsSync(example)) {
    console.error(`  提示: 从模板复制一份并填写 —— copy profiles\\standard.example.json profiles\\${profileName}.json`);
  }
  console.error(`  并设置环境变量 EDOP_PROFILE=${profileName}（或启动脚本内已设置）`);
} else {
  try {
    cfg = JSON.parse(fs.readFileSync(profilePath, "utf8"));
  } catch (e) {
    fail(`profiles/${profileName}.json 不是合法 JSON: ${e.message}`);
  }
}

/* ---------- 2. 关键字段检查 ---------- */
if (cfg) {
  const mode = process.env.EDOP_GATEWAY_MODE || cfg.mode || "stdio";
  const pw = String(cfg.password ?? "");
  const pwPlaceholder = !pw || pw.startsWith("__") || pw.includes("占位符") || pw.includes("填写") || pw.includes("placeholder");

  /**
   * 校验口径（依据 gateway.mjs 实际实现）：
   *   - apiKey：http 模式**必填**。gateway.mjs:517 会硬性检查，缺失直接退出。
   *     它开启的是 service 模式（持 key 者以服务账号视角访问全平台数据）。
   *   - password：只被 service 模式使用（config.username/config.password 登录）。
   *     逐用户模式（Authorization: Bearer <grant_token>）完全不用它：网关拿用户令牌
   *     调后端 /edop/connector/auth/exchange 换 JWT，权限由该用户 @DataScope 决定。
   *
   * 由于 http 模式必定存在 apiKey，service 模式在本部署下始终可达：
   *   - 若 apiKey 已发给真实用户/客户 -> 密码必须有效（否则 service 模式调用报"密码错误"）
   *   - 仅用于内部联调、对外只发 grant_token -> 密码可留占位符，降级为告警
   * stdio 模式（本机自用）恒为 service，密码必须有效。
   */
  const hasApiKey = !!cfg.apiKey;

  if (mode === "http") {
    if (!hasApiKey) fail("apiKey 为空（http 模式必需，gateway.mjs 会拒绝启动）");
    else if (String(cfg.apiKey).length < 24) fail(`apiKey 过短（${String(cfg.apiKey).length} 位，要求 ≥24 位）`);
    else if (/^(edop-local-dev-key|test|123456|admin|changeme)/i.test(String(cfg.apiKey))) {
      fail("apiKey 疑似开发/弱默认值，生产必须随机生成");
    }
  }

  if (pwPlaceholder) {
    if (mode === "http") {
      // http 部署下 service 模式可达（有 apiKey），但生产实际只发逐用户令牌时可容忍
      warn("password 为占位符：service 模式（x-api-key 访问）将报登录失败。"
        + "若本环境只向用户发放 grant_token（逐用户授权），可忽略此项");
    } else {
      fail(`password 仍是占位符（当前值 "${pw}"），请填入 edop-mcp 账号的真实密码（stdio 模式恒用服务账号）`);
    }
  }

  // baseUrl：必须指向后端
  if (!cfg.baseUrl) fail("baseUrl 为空");
  else if (!/^https?:\/\//.test(cfg.baseUrl)) fail(`baseUrl 格式非法: ${cfg.baseUrl}`);

  // manifest：文件必须存在且非空
  const manifest = cfg.manifest || profileName;
  const mf = path.join(root, "tool-manifests", `${manifest}.tools.json`);
  if (!fs.existsSync(mf)) {
    const avail = fs.existsSync(path.join(root, "tool-manifests"))
      ? fs.readdirSync(path.join(root, "tool-manifests")).filter((f) => f.endsWith(".tools.json")).join(", ")
      : "(目录不存在)";
    fail(`缺少工具映射 tool-manifests/${manifest}.tools.json（现有: ${avail}）`);
  } else {
    try {
      const m = JSON.parse(fs.readFileSync(mf, "utf8"));
      const n = Object.keys(m.tools || {}).length;
      if (!n) fail(`tool-manifests/${manifest}.tools.json 未定义任何工具`);
      else console.log(`[preflight] 工具映射: ${manifest}.tools.json (${n} 个工具)`);
    } catch (e) {
      fail(`tool-manifests/${manifest}.tools.json 解析失败: ${e.message}`);
    }
  }

  // 依赖：node_modules 必须随包
  const dep = path.join(root, "node_modules", "@modelcontextprotocol", "sdk");
  if (!fs.existsSync(dep)) fail("node_modules 缺失或不完整（@modelcontextprotocol/sdk 不存在），请执行 npm install 或随包分发");

  // 端口合法性
  const port = Number(process.env.EDOP_HTTP_PORT) || cfg.httpPort || 3000;
  if (!Number.isInteger(port) || port < 1 || port > 65535) fail(`httpPort 非法: ${cfg.httpPort}`);
  else console.log(`[preflight] 监听端口: ${port}`);

  // 公网暴露提醒（不阻断）
  const host = process.env.EDOP_HTTP_HOST || cfg.httpHost || "0.0.0.0";
  const allowIps = process.env.EDOP_ALLOW_IPS || cfg.allowedIps;
  if (mode === "http" && host === "0.0.0.0" && !allowIps) {
    warn("httpHost=0.0.0.0 且未配置 allowedIps —— 仅限可信内网；公网部署必须加白名单");
  }
}

/* ---------- 3. 输出结论 ---------- */
for (const w of warns) console.log(`[preflight][WARN] ${w}`);

if (errors.length) {
  console.error("");
  console.error("[preflight][FAIL] 配置校验未通过，已阻止启动：");
  for (const e of errors) console.error(`  - ${e}`);
  console.error("");
  console.error(`  配置文件: ${profilePath}`);
  process.exit(1);
}

console.log(`[preflight][OK] profile=${profileName} 校验通过`);
process.exit(0);

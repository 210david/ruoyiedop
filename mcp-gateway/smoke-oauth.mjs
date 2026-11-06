/**
 * OAuth 2.1 授权流冒烟测试（批次 2.2 增补：WorkBuddy 自动授权页模式）
 *
 * 覆盖：401 触发头 / RFC 9728 元数据 / 动态注册 / 授权页渲染 / 登录错误路径 / PKCE 错误路径
 * 不覆盖：真实账号授权成功路径（凭据纪律——由用户在浏览器完成验收）
 *
 * 运行：node smoke-oauth.mjs   （要求 EDOP 后端在运行，用于登录代理测试）
 */
import { spawn } from "node:child_process";
import crypto from "node:crypto";

const PORT = 3100;
const BASE = `http://127.0.0.1:${PORT}`;
const REDIRECT = "workbuddy://workbuddy/mcp/connector%3A/oauth/callback";

let pass = 0, fail = 0;
function check(name, ok, detail = "") {
  console.log(`${ok ? "✅" : "❌"} ${name}${detail ? "  " + detail : ""}`);
  ok ? pass++ : fail++;
}

// PKCE pair
const verifier = crypto.randomBytes(32).toString("base64url");
const challenge = crypto.createHash("sha256").update(verifier, "ascii").digest("base64url");

// 启动独立网关实例
const child = spawn(process.execPath, ["gateway.mjs"], {
  env: { ...process.env, EDOP_GATEWAY_MODE: "http", EDOP_HTTP_PORT: String(PORT) },
  stdio: ["ignore", "ignore", "pipe"],
});
child.stderr.on("data", () => {});

// 探活重试（最多 15 秒）
let up = false;
for (let i = 0; i < 30; i++) {
  try {
    const h = await fetch(`${BASE}/health`);
    if (h.ok) { up = true; break; }
  } catch { /* not yet */ }
  await new Promise((r) => setTimeout(r, 500));
}
if (!up) {
  console.error("❌ 网关子进程 15 秒内未就绪");
  child.kill("SIGTERM");
  process.exit(1);
}

try {
  // 1. 无凭证访问 /mcp → 401 + WWW-Authenticate 触发 OAuth 流
  const r1 = await fetch(`${BASE}/mcp`, { method: "POST", headers: { "Content-Type": "application/json" }, body: "{}" });
  const wa = r1.headers.get("www-authenticate") || "";
  check("无凭证 401 + WWW-Authenticate 触发头", r1.status === 401 && wa.includes("resource_metadata="), wa.slice(0, 80));

  // 2. RFC 9728 受保护资源元数据
  const r2 = await fetch(`${BASE}/.well-known/oauth-protected-resource`);
  const j2 = await r2.json();
  check("受保护资源元数据(RFC 9728)", r2.status === 200 && j2.resource === `${BASE}/mcp` && Array.isArray(j2.authorization_servers));

  // 3. 授权服务器元数据
  const r3 = await fetch(`${BASE}/.well-known/oauth-authorization-server`);
  const j3 = await r3.json();
  check(
    "授权服务器元数据(端点+S256)",
    r3.status === 200 && j3.authorization_endpoint === `${BASE}/oauth/authorize` && j3.token_endpoint === `${BASE}/oauth/token` && j3.code_challenge_methods.includes("S256")
  );

  // 4. 动态客户端注册（RFC 7591，必须回显 redirect_uris）
  const r4 = await fetch(`${BASE}/oauth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ redirect_uris: [REDIRECT], client_name: "WorkBuddy" }),
  });
  const j4 = await r4.json();
  check("动态客户端注册(RFC 7591)", r4.status === 201 && j4.client_id && Array.isArray(j4.redirect_uris) && j4.redirect_uris[0] === REDIRECT, `client=${j4.client_id}`);
  const clientId = j4.client_id;

  // 5. 授权端点渲染确认页（含仿官方样式的「立即前往授权」按钮）
  const authUrl = new URL(`${BASE}/oauth/authorize`);
  authUrl.searchParams.set("response_type", "code");
  authUrl.searchParams.set("client_id", clientId);
  authUrl.searchParams.set("redirect_uri", REDIRECT);
  authUrl.searchParams.set("state", "st-test-123");
  authUrl.searchParams.set("code_challenge", challenge);
  authUrl.searchParams.set("code_challenge_method", "S256");
  authUrl.searchParams.set("scope", "edop.read");
  const r5 = await fetch(authUrl, { redirect: "manual" });
  const html = await r5.text();
  check(
    "授权确认页渲染",
    r5.status === 200 && html.includes("立即前往授权") && html.includes("EDOP") && html.includes("只读"),
    `bytes=${html.length}`
  );
  // 从页面提取 ctx（验证下发的授权上下文含 challenge）
  check("授权上下文内嵌(PKCE challenge)", html.includes("code_challenge") || html.includes('"challenge"'), "");

  // 6. 错误密码 → 401 invalid_credentials（验证 EDOP 登录代理链路）
  const r6 = await fetch(`${BASE}/oauth/authorize`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username: "nonexistent-user-xyz", password: "wrong", ctx: { clientId, redirectUri: REDIRECT, state: "st-test-123", challenge } }),
  });
  const j6 = await r6.json().catch(() => null);
  check("错误密码拒绝(EDOP 代理链路)", r6.status === 401 && j6 && j6.error === "invalid_credentials", j6 ? j6.msg || "" : "");

  // 7. 未注册 redirect_uri → 400
  const authUrl2 = new URL(`${BASE}/oauth/authorize`);
  authUrl2.searchParams.set("response_type", "code");
  authUrl2.searchParams.set("client_id", clientId);
  authUrl2.searchParams.set("redirect_uri", "https://evil.example.com/cb");
  authUrl2.searchParams.set("code_challenge", challenge);
  authUrl2.searchParams.set("code_challenge_method", "S256");
  const r7 = await fetch(authUrl2, { redirect: "manual" });
  check("未注册 redirect_uri 拒绝(防授权码劫持)", r7.status === 400, "");

  // 8. 伪造授权码换 token → invalid_grant（PKCE/一次性校验路径）
  const r8 = await fetch(`${BASE}/oauth/token`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({ grant_type: "authorization_code", code: "fake-code", code_verifier: verifier, client_id: clientId, redirect_uri: REDIRECT }).toString(),
  });
  const j8 = await r8.json().catch(() => null);
  check("伪造授权码拒绝(invalid_grant)", r8.status === 400 && j8 && j8.error === "invalid_grant", "");
} finally {
  child.kill("SIGTERM");
}

console.log(`\n结果: ${pass} 通过 / ${fail} 失败`);
process.exit(fail ? 1 : 0);

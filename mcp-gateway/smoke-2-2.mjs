/**
 * 批次 2.2 专项验收脚本：逐用户鉴权（grant_token → JWT 换发 → 逐人数据）
 * 前提：EDOP 后端(:8081)运行中；测试账号 mcp-user1/mcp-user2（密码 admin123）存在
 * 运行：node smoke-2-2.mjs
 */
const BASE = process.env.EDOP_BASE || "http://127.0.0.1:8081";
const results = [];
const check = (name, pass, detail = "") => {
  results.push({ name, pass });
  console.log(`${pass ? "PASS" : "FAIL"} ${name}${detail ? " | " + detail : ""}`);
};

const USER = { "Content-Type": "application/json" };

async function grantFor(username) {
  const r = await fetch(`${BASE}/edop/connector/auth/login`, {
    method: "POST", headers: USER,
    body: JSON.stringify({ username, password: "admin123", clientDesc: "smoke-2-2" }),
  }).then(x => x.json());
  return r;
}

try {
  // 1. 两个用户分别授权
  const g1 = await grantFor("mcp-user1");
  check("1.1 user1 授权颁发 grant_token", g1.code === 200 && !!g1.grantToken, g1.grantToken ? g1.grantToken.slice(0, 8) + "..." : g1.msg);
  const g2 = await grantFor("mcp-user2");
  check("1.2 user2 授权颁发 grant_token", g2.code === 200 && !!g2.grantToken, g2.grantToken ? g2.grantToken.slice(0, 8) + "..." : g2.msg);

  // 1.3 错误密码应被拒
  const bad = await fetch(`${BASE}/edop/connector/auth/login`, {
    method: "POST", headers: USER,
    body: JSON.stringify({ username: "mcp-user1", password: "wrong-password" }),
  }).then(x => x.json());
  check("1.3 错误密码被拒绝", bad.code !== 200, bad.msg);

  // 2. grant_token 换发 JWT
  const e1 = await fetch(`${BASE}/edop/connector/auth/exchange`, {
    method: "POST", headers: { Authorization: `Bearer ${g1.grantToken}` },
  }).then(x => x.json());
  check("2.1 user1 exchange 换发 JWT", e1.code === 200 && !!e1.token, `user=${e1.userName} dept=${e1.deptName}`);
  const e2 = await fetch(`${BASE}/edop/connector/auth/exchange`, {
    method: "POST", headers: { Authorization: `Bearer ${g2.grantToken}` },
  }).then(x => x.json());
  check("2.2 user2 exchange 换发 JWT", e2.code === 200 && !!e2.token, `user=${e2.userName} dept=${e2.deptName}`);

  // 3. 用各自 JWT 查库存（逐用户身份调用）
  const inv1 = await fetch(`${BASE}/wms/inventory/list?pageNum=1&pageSize=5`, {
    headers: { Authorization: `Bearer ${e1.token}` },
  }).then(x => x.json());
  check("3.1 user1 JWT 查库存", inv1.code === 200, `rows=${inv1.total}`);
  const inv2 = await fetch(`${BASE}/wms/inventory/list?pageNum=1&pageSize=5`, {
    headers: { Authorization: `Bearer ${e2.token}` },
  }).then(x => x.json());
  check("3.2 user2 JWT 查库存", inv2.code === 200, `rows=${inv2.total}`);

  // 4. 吊销 user2 授权 → exchange 立即 401
  const adminLogin = await fetch(`${BASE}/login`, {
    method: "POST", headers: USER,
    body: JSON.stringify({ username: "admin", password: "admin123" }),
  }).then(x => x.json());
  const list2 = await fetch(`${BASE}/edop/connector/auth/admin/list?pageNum=1&pageSize=50`, {
    headers: { Authorization: `Bearer ${adminLogin.token}` },
  }).then(x => x.json());
  const g2row = (list2.rows || []).find(r => r.userName === "mcp-user2");
  check("4.1 管理员可见授权列表（含 user2）", !!g2row, g2row ? `grantId=${g2row.grantId}` : "not found");
  if (g2row) {
    const rv = await fetch(`${BASE}/edop/connector/auth/admin/revoke/${g2row.grantId}`, {
      method: "POST", headers: { Authorization: `Bearer ${adminLogin.token}` },
    }).then(x => x.json());
    check("4.2 管理员吊销 user2", rv.code === 200);
    const e2again = await fetch(`${BASE}/edop/connector/auth/exchange`, {
      method: "POST", headers: { Authorization: `Bearer ${g2.grantToken}` },
    }).then(x => x.json());
    check("4.3 吊销后 user2 exchange 立即 401", e2again.code === 401, e2again.msg);
    const e1again = await fetch(`${BASE}/edop/connector/auth/exchange`, {
      method: "POST", headers: { Authorization: `Bearer ${g1.grantToken}` },
    }).then(x => x.json());
    check("4.4 user1 不受影响", e1again.code === 200);
  }

  // 5. 停用 user1 → exchange 立即 401（停用即断权）
  const dis = await fetch(`${BASE}/system/user/changeStatus`, {
    method: "PUT", headers: { ...USER, Authorization: `Bearer ${adminLogin.token}` },
    body: JSON.stringify({ userId: 187, status: "1" }),
  }).then(x => x.json());
  check("5.1 停用 user1 账号", dis.code === 200);
  const e1dis = await fetch(`${BASE}/edop/connector/auth/exchange`, {
    method: "POST", headers: { Authorization: `Bearer ${g1.grantToken}` },
  }).then(x => x.json());
  check("5.2 停用后 user1 exchange 立即 401", e1dis.code === 401, e1dis.msg);
  // 恢复 user1
  await fetch(`${BASE}/system/user/changeStatus`, {
    method: "PUT", headers: { ...USER, Authorization: `Bearer ${adminLogin.token}` },
    body: JSON.stringify({ userId: 187, status: "0" }),
  });
  console.log("(user1 账号已恢复启用)");
} catch (err) {
  console.error("SCRIPT_ERROR:", err.message);
}

const pass = results.filter(r => r.pass).length;
console.log(`\n结果: ${pass} 通过, ${results.length - pass} 失败`);
process.exit(pass === results.length ? 0 : 1);

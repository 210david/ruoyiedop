# EDOP MCP 网关生产部署手册

> 适用：EDOP × WorkBuddy 连接器 · Phase 2（方案 v1.2 · 批次 2.3）
> 前置阅读：`doc/plans/EDOP-WorkBuddy连接器接入方案.md`（架构/权限/安全设计）、`mcp-gateway/CONTRACT.md`（接口契约）

---

## 0. 部署形态速查

| 形态 | 场景 | 网关位置 | 用户侧配置 | 本手册章节 |
|------|------|---------|-----------|-----------|
| A1 内部 stdio | 内部团队各自电脑用 | 用户本机进程 | 每人一份 mcp.json | §1 |
| A2 内网 HTTP | 内部统一服务 / 小客户试点 | 客户内网服务器 | 每人粘一段 URL JSON | §2 |
| B 企业交付（正式） | 多客户、多用户逐人鉴权 | 你方/客户服务器 | 企业控制台分发，零配置 | §2 + §4 |

---

## 1. 形态 A1：内部 stdio（每台电脑一份）

1. 生产库执行 `sql/edop_mcp_service_account.sql`（**密码必须与开发环境不同**，执行前改脚本）
2. 分发安装包（zip）：node 免安装版 + `mcp-gateway/` 整目录（**不含 profiles 真实凭据，只含 example 模板**）
3. 每个用户：
   - 复制 `profiles/standard.example.json` → `profiles/<自己名字>.json`，填分配给他的**个人低权限账号**（禁用共享 admin / edop-mcp）
   - 设置环境变量 `EDOP_PROFILE=<自己名字>`（不设则默认 standard）
   - WorkBuddy → 连接器 → 自定义连接器 → 粘贴 mcp.json（command 指向本机 node 与 gateway.mjs）→ 信任 → 启用
4. 验收：对话问一句业务查询，回复含真实数据；`audit.log` 出现 `tool=xxx ok`

---

## 2. 形态 A2 / B：服务器 HTTP 部署

### 2.1 Windows 服务器（bat 双击版）

> ⚠️ 重要：网关为常驻服务，**必须由人双击 bat 启动（资源管理器启动）或经计划任务/服务托管**。AI 助手沙箱内拉起的进程会随命令结束被清理，不能用于启动常驻服务——这是交付时给客户运维交代清楚的硬边界。

```
客户服务器
├─ mcp-gateway/          # 随分支代码拉取
│  ├─ gateway.mjs
│  ├─ tool-manifests/
│  └─ profiles/
│     ├─ dwx.json         # 部署时手工放置，永不入库
│     └─ ...
└─ deploy/
   ├─ start-http.bat      # 双击启动（最小化独立窗口，防闪退带 pause）
   └─ stop-http.bat       # 双击停止（netstat 定位 PID → taskkill）
```

步骤：
1. 拉取客户分支代码（网关随仓走，2F 交付模型）
2. 放置 `profiles/<客户>.json`，关键字段：
   ```json
   {
     "baseUrl": "http://127.0.0.1:8081",
     "username": "edop-mcp-dwx",
     "password": "<该客户专用密码>",
     "manifest": "standard",
     "mode": "http",
     "httpPort": 3000,
     "httpHost": "0.0.0.0",
     "apiKey": "<随机长 Key，客户发放>"
   }
   ```
   并设置环境变量 `EDOP_PROFILE=<客户>`（系统环境变量里配一次，bat 会继承）
3. 双击 `start-http.bat`，看到 `[OK] ... port 3000` 即成功
4. `netstat -ano | findstr ":3000"` 防火墙按需放行（**仅放行内网网段，禁公网直通**）

### 2.2 Linux 服务器（Docker）

```bash
cd mcp-gateway/deploy
# 1. 放置 profiles/<客户>.json（同上）
# 2. 改 docker-compose.yml：EDOP_PROFILE、端口映射（必要时绑内网网卡）
docker compose up -d --build
# 3. 验证
curl http://127.0.0.1:3000/health        # {"ok":true,...}
curl -o /dev/null -w "%{http_code}" -X POST http://127.0.0.1:3000/mcp \
  -H "Content-Type: application/json" -d '{}'   # 期望 401（无 Key 被拒）
```

日志持久化在 `deploy/logs/`（挂载卷），容器重建不丢。

---

## 3. 客户侧接入配置

### 3.1 手动粘贴（形态 A2 / 小客户试点）

用户在 WorkBuddy 连接器 → 自定义连接器 → 配置 MCP：

```json
{
  "mcpServers": {
    "edop-<客户名>": {
      "type": "streamablehttp",
      "url": "http://<客户内网IP>:3000/mcp",
      "headers": { "x-api-key": "<发放给该客户的 Key>" }
    }
  }
}
```

保存 → 信任 → 启用。详见 `deploy/客户接入指引.md`（可直接发给客户）。

### 3.2 企业控制台统一分发（形态 B，零配置）

腾讯云企业控制台 → 连接器管理 → 新建连接器：
- 类型：MCP（Streamable HTTP）；URL：`http(s)://<网关地址>/mcp`
- 认证：过渡期 API Key；正式期 OAuth 2.0 / MCP OAuth 2.1（批次 2.2 交付后启用逐用户手机号授权）
- 可见范围：全员或按部门
- 创建后组织成员连接器列表自动出现，点启用即用，无任何 JSON

---

## 4. 安全底线（每客户交付前逐条核对）

- [ ] API Key 必配且随机（≥24 位），禁止沿用 `edop-local-dev-key` 之类的开发值
- [ ] 服务账号（如 edop-mcp-dwx）**每客户独立**，权限按需收敛（角色数据权限可设本部门/自定）
- [ ] 网关仅内网可达：bat 部署确认防火墙网段限制；docker 端口映射绑内网网卡；nginx 反代时加 `allow <网段>; deny all;`
- [ ] 出内网（远程/多分支）场景必须加密：首选买域名 + Let's Encrypt；过渡用 ZeroSSL IP 证书（90 天续期）；或挂客户 VPN
- [ ] `profiles/*.json` 与生产密码永不进 git（.gitignore 已内置，分支仓合并时不得移除该规则）
- [ ] audit.log 落盘持久目录并定期归档（bat 部署建议用计划任务做日志轮转）
- [ ] 明确运维口径：**后端停机 = 连接器失效**；写入客户部署手册

### 4.1 云外网 IP 部署专章（小客户常见：无域名、无证书、公网 IP 直连）

客户系统部署在云服务器（外网 IP）且不上 HTTPS 时，**"内网隔离"前提不成立，以下三件套是硬底线，缺一不交付**：

| # | 防护层 | 配置 | 挡什么 |
|---|--------|------|--------|
| 1 | **云安全组限源**（最关键，零成本） | 云控制台安全组：3000 端口仅放行**客户公司出口 IP**（员工在 ip138 查"我的IP"即可获得）；8081 后端端口不对公网开放或同样限源 | 全网扫描器、非客户网络的一切访问 |
| 2 | **网关 IP 白名单**（代码层双保险） | profile 加 `"allowedIps": ["<公司出口IP>"]` 或环境变量 `EDOP_ALLOW_IPS=<IP>`；支持 CIDR（`1.2.3.0/24`）；未配置白名单时**禁止交付公网部署**（本机开发除外） | 安全组误配置、同云内其他租户 |
| 3 | **逐用户 grant_token + 审计** | 员工从 `http://IP:3000/grant` 自助登录取 token（一次 2 分钟，90 天续期）；audit.log 全留痕 | 令牌泄露后无止境滥用（可吊销）；事后追责 |

**注意**：即使三层齐全，HTTP 明文传输的 token 在客户公司出口 IP 到云服务器之间理论上仍可被链路嗅探——对小客户属于可接受残余风险（攻击者需先侵入运营商链路）；**客户数据敏感度提升或人数 >20 时，劝客户上 ZeroSSL 免费 IP 证书切 HTTPS 模式**（`EDOP_HTTPS=1`，证书放 `certs/`，已支持），上了 HTTPS 还能解锁 OAuth 自动授权页。

**公网部署验收增补**：
1. ☐ 安全组 3000 仅对客户出口 IP 开放（用自己电脑 curl 应超时/拒绝）
2. ☐ 白名单外 IP 访问 `/mcp` `/grant` 返回 403 且 audit 留痕 `ip-guard`
3. ☐ 客户办公网内员工完成 `/grant` 自助授权 → 连接器粘贴 token → 真实查询成功

---

## 5. 上线验收清单

1. ☐ `GET /health` 返回 `{"ok":true,...}` 且 profile 名正确
2. ☐ 无 Key / 错 Key 调 `/mcp` 返回 401（审计留痕）
3. ☐ 用正确 Key 从另一台内网电脑的 WorkBuddy 完成一次真实查询
4. ☐ `audit.log` 记录上述全部调用
5. ☐ 后端停机再启动，网关自动重登恢复（401 自愈机制）
6. ☐ 服务账号改密演练：改库 → 改 profile → 重启网关 → 恢复正常（SOP 见 §6）

## 6. 运维 SOP

| 事项 | 步骤 |
|------|------|
| 服务账号改密 | 改库密码 → 改 profile → 重启网关（bat: stop+start；docker: `docker compose restart`）→ 验收清单 §5.3 |
| 网关升级 | 新代码 merge 进该客户分支 → 部署机 git pull → 重启网关 → 冒烟（`node smoke-test.mjs`） |
| 接口契约变更 | 分支动到契约内接口：登记 `CONTRACT.md` 偏差表 → 同步该分支 tool-manifests → 回归 |
| 僵尸授权清理（2.2 后） | 定期按 `edop_connector_grant.last_used_time` 清理 90 天未用授权 |

## 7. 多客户运维台账

见 `deploy/运维台账.md` 模板。首家客户签约时启用，每交付一家登记一行。

# EDOP × WorkBuddy MCP 网关 v2

让 WorkBuddy 通过自然语言直接查询 EDOP 企业数字化运营平台的业务数据。

方案文档：`doc/plans/EDOP-WorkBuddy连接器接入方案.md`（v1.2）｜ 契约基准：`CONTRACT.md`

## 三层架构（Phase 2，方案 2F 定稿：连接器随分支仓走）

```
gateway.mjs                    # 标准引擎（单源，分支仓禁改）
tool-manifests/
  standard.tools.json          # 工具→接口映射（契约漂移改这里）
profiles/
  standard.json                # 客户身份配置（gitignore，永不入库）
  standard.example.json        # 配置模板
```

- **PROFILE 选择**：环境变量 `EDOP_PROFILE`（默认 standard）；未命中时回退旧版 `config.json`
- **模式选择**：`EDOP_GATEWAY_MODE` 或 profile 的 `mode` 字段——`stdio`（本机自用）/ `http`（客户交付）

## 双模式

| 模式 | 启动 | 用途 | 鉴权 |
|------|------|------|------|
| stdio | WorkBuddy 自动拉起（mcp.json 的 command） | 本机自用 | EDOP 服务账号 JWT |
| http | `EDOP_GATEWAY_MODE=http node gateway.mjs` | 客户交付，`POST /mcp`（stateless）+ `GET /health` | API Key（`x-api-key` 或 `Bearer`）+ EDOP 服务账号 JWT |

## 工具清单（全部只读，接口契约见 CONTRACT.md）

`get_bi_metrics`（BI 指标+字典）、`query_inventory`（库存）、`query_sales_orders`（销售订单）、`query_purchase_orders`（采购订单）、`get_platform_overview`（四模块总览聚合）

统一约束：日期范围 ≤ 92 天、分页 ≤ 100 行、仅 GET。

## 分支交付纪律（方案 2F 三条）

1. 分支仓**禁改 gateway.mjs**——共性功能只回 main 改再 merge
2. `profiles/*.json` 强制 .gitignore——仓库只提交 example 模板
3. 客户专属工具只进分支 `tool-manifests/<分支>.tools.json`——≥2 客户需要则提升回 main

## 运维

- 冒烟回归：`node smoke-test.mjs`（7 项：stdio×3 + http×4，改网关后必跑）
- 审计日志：`audit.log`（含 profile 标识、每次调用与登录、http 鉴权拒绝记录）
- 前提：EDOP 后端在运行（baseUrl 指向的目标）
- 服务账号初始化：`sql/edop_mcp_service_account.sql`

---

## OAuth 2.1 自动授权（WorkBuddy「连接即跳转授权页」模式）

依据 open.workbuddy.cn/docs/connector「MCP OAuth flow」实现，WorkBuddy 内置 OAuth 管理器自动完成客户端流程。

**流程**：连接时无凭证 → 网关 401 + `WWW-Authenticate: resource_metadata` → WorkBuddy 自动发现元数据 → 动态客户端注册（RFC 7591）→ **浏览器自动打开 EDOP 授权确认页** → 用户登录 EDOP 账号 → 授权码回调 → PKCE 换 token → 连接完成（绿点）。

**服务端端点**（网关 http 模式，后端 Java 零改动）：

| 端点 | 说明 |
|------|------|
| `GET /.well-known/oauth-protected-resource` | RFC 9728 资源元数据 |
| `GET /.well-known/oauth-authorization-server` | AS 元数据（端点 + S256） |
| `POST /oauth/register` | 动态客户端注册，回显 redirect_uris |
| `GET /oauth/authorize` | 授权确认页（仿 WorkBuddy 官方样式） |
| `POST /oauth/authorize` | EDOP 账号密码登录 → 一次性授权码（10min） |
| `POST /oauth/token` | authorization_code（PKCE S256）+ refresh_token |

**令牌映射**：`access_token` = `refresh_token` = EDOP `grant_token`（90 天，复用逐用户鉴权链路，@DataScope 按人裁剪）。

**用户接入（每人一次，无手工 token）**：
1. 连接器配置只填 `url`，**不要填 headers**：
   ```json
   { "mcpServers": { "edop-assistant": { "type": "streamablehttp", "url": "http://<服务器IP>:3000/mcp" } } }
   ```
2. 保存 → 启用 → 状态变「需要认证」→ 点「**连接**」→ 浏览器自动打开授权页
3. 登录自己的 EDOP 账号 → 自动回跳 → 绿点就绪

**测试**：`node smoke-oauth.mjs`（9 项，需后端在运行）。

**注意事项**：官方文档要求 OAuth 端点走 HTTPS；`http://127.0.0.1` 回环是否被 WorkBuddy 容忍、客户内网纯 IP + HTTP 是否可行，以实测为准——实测结论直接决定交付形态（详见 DEPLOY.md 2D 节）。

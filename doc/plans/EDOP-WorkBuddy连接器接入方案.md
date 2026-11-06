# EDOP × WorkBuddy 连接器接入方案

> 版本：v1.2 ｜ 日期：2026-11-06 ｜ 状态：Phase 1 已验收；Phase 2 开发中（2.1 批次）
> 目标：让 WorkBuddy 用户通过自然语言直接查询 EDOP 企业数字化运营平台的业务数据
>
> **变更记录**
> - v1.2（2026-11-06）：新增 2H「Phase 2 实施批次与验收标准」（补 v1.1 缺口）；Phase 2 开发启动
> - v1.1（2026-11-06）：Phase 2 增补「逐用户鉴权（手机号 OAuth）」设计与手机号生命周期治理（换绑/停用/复用等边界），作为 Phase 2 立项依据
> - v1.0（2026-11-06）：初版，环境分析/选型/工具设计/安全设计/Phase 1 实施，当日完成并验收通过

---

## 一、环境分析（已核实）

### 1.1 本工程（EDOP / RuoYi-Vue3）

| 项目 | 现状 | 对方案的影响 |
|------|------|-------------|
| 技术栈 | RuoYi 3.9.2 + Spring Boot **4.0.6** + Java 17 | Spring Boot 4 较新，Spring AI 生态兼容性存疑（见选型分析） |
| 业务模块 | wms / dms / mk / pms / qms / safety / mms / hr / bid / fin / pdm / bi / ai / flowable 共 14 个业务模块 | 数据面广，工具需按模块分批暴露 |
| 后端服务 | **8081 端口，当前正在运行**（PID 7276） | 网关可直接对接，无需额外部署 |
| 认证机制 | JWT（Authorization: Bearer），验证码已关闭（`captchaEnabled:false`，已实测 `/captchaImage` 返回 false） | ✅ 网关可用服务账号程序化调 `/login` 换取 token |
| 数据权限 | `@DataScope` 全平台强制（主规范 3.5） | 连接器查询必须走正常鉴权链路，自动继承部门数据边界 |
| 工程规范 | 主规范 1.2 新模块接入、6.1 依赖方向 common←system←mk←wms←dms/pms | 方案若改后端须遵守；本方案 Phase 1 **零后端改动** |
| BI 基础 | `bi_metric_snapshot` 快照表 + `IModuleStatsProvider` 统计体系已建成 | BI 指标查询工具可直接复用现有实现 |

### 1.2 WorkBuddy 侧

| 项目 | 现状 |
|------|------|
| 接入机制 | 官方支持自定义 MCP 连接器：连接器管理页「自定义连接器 → 配置 MCP」 |
| 本机配置 | `~/.workbuddy/mcp.json` 已存在（design-converter、rea，均 disabled），新连接器**合并写入、不得覆盖** |
| 支持模式 | stdio 本地模式（command 启动）/ streamablehttp 远程模式（url + headers） |
| 激活方式 | 配置后需在连接器管理页手动「信任 → 启用」 |
| 企业级能力 | 腾讯云企业控制台支持管理员统一创建连接器（OAuth 2.0 / API Key / MCP OAuth 2.1 / 自建应用免授权），按部门/成员设置可见范围 |

### 1.3 本机条件

- Node.js v22.22.2（`C:\Users\Laptop\.workbuddy\binaries\node\versions\22.22.2\node.exe`）✅
- 后端、前端、WorkBuddy 同机运行，Phase 1 验证条件齐备 ✅

---

## 二、方案选型

### 备选方案对比

| 维度 | 方案 A：Java 内嵌 MCP（Spring AI） | 方案 B：Node stdio 网关 | 方案 C：Node streamablehttp 网关 |
|------|------|------|------|
| 实现方式 | 后端集成 spring-ai-mcp-server | 独立 Node 脚本，调现有 REST API | 独立 Node 服务，与后端同机部署 |
| 后端改动 | **大**（加依赖+配置+端点） | **零** | **零**（仅加服务账号） |
| 最大风险 | Spring Boot 4.0.6 与 Spring AI 兼容性未验证，可能引发依赖冲突，波及全工程 20 个模块 | 工具逻辑写在 Node 侧，与 Java 业务代码双份维护 | 同 B，另需托管服务 |
| 部署形态 | 随后端 | 每台装 WorkBuddy 的机器本地跑 | 一台服务器服务多人 |
| 适用场景 | 深度集成 | **本机验证 / 单用户** | **客户企业多人交付** |

### 选型结论：B（Phase 1）→ C（Phase 2）

- **Phase 1 用方案 B**：零后端改动、零构建风险、当天可验证跑通，先把「能不能行、好不好用」验证掉。
- **Phase 2 升级方案 C**：验证通过后，把同一套工具逻辑从 stdio 搬到 streamablehttp 服务，走企业连接器交付客户。
- **方案 A 暂不采用**：Boot 4.0.6 + Spring AI 兼容性是未知数，为验证连接器价值去冒全工程依赖冲突的险，不划算。待 Spring AI 对 Boot 4 的支持稳定后再评估。

### 架构图（Phase 1）

```
WorkBuddy (本机)
    │  MCP stdio
    ▼
edop-mcp-gateway (Node 22, 本地进程)
    │  HTTP + Bearer JWT（服务账号）
    ▼
EDOP 后端 :8081  →  Spring Security 鉴权 → @DataScope 数据边界 → 各模块 Service
    (wms 库存 / mk 销售 / pms 采购 / bi 指标 ...)
```

---

## 三、工具设计（Phase 1 第一批，全部只读）

> 原则：不裸暴露数据库，按业务语义封装工具；先只读，写入类（建单/审批）留到后续批次。

| 工具名 | 数据来源（复用现有实现） | 说明 |
|--------|------------------------|------|
| `get_bi_metrics` | bi 模块：指标字典 + `bi_metric_snapshot` | 按日期/部门/指标编码查询指标值，支持列出全部可用指标 |
| `query_inventory` | wms 模块现有库存查询接口 | 按物料编码/名称/仓库/部门查询库存余量 |
| `query_sales_orders` | mk 模块现有销售订单接口 | 按客户/日期范围/状态查询，含金额汇总 |
| `query_purchase_orders` | pms 模块现有采购订单接口 | 按供应商/日期范围/状态查询，含金额汇总 |
| `get_platform_overview` | 聚合上述模块的当日看板数 | 一句话问"今天平台整体情况"时的兜底工具 |

每个工具统一入参约束：日期范围上限 92 天、分页上限 100 行、金额/数量带单位，输出纯结构化 JSON（WorkBuddy 负责汇总解读，网关不做二次加工）。

> 具体接口路径以各模块现有 Controller 实测为准，实现阶段逐一核对，不新写后端接口。

---

## 四、安全设计

| 项 | 措施 |
|----|------|
| 专用服务账号 | 新建 `edop-mcp` 账号 + 独立只读角色（菜单权限仅勾选上述查询页面），**不复用 admin** |
| 数据权限 | 走正常 Spring Security 登录链路，`@DataScope` 自动按该账号所属部门裁剪数据 |
| 凭据存放 | 服务账号密码放网关本地配置文件（`config.json`，加入 .gitignore），不入库不入仓库 |
| Token 管理 | 登录换 token 后本地缓存，401 自动重登；受 `user.password.onlineCount` 影响，用独立账号避免与真人互踢 |
| 只读约束 | 网关侧白名单只放行 GET 类接口，POST/PUT/DELETE 一律拒绝（双保险：账号权限 + 网关白名单） |
| 网络 | stdio 模式仅本机通信，不对局域网暴露端口；Phase 2 HTTP 模式再加 IP 白名单 + 独立 API Key |
| 审计 | 网关本地日志记录每次工具调用（时间/工具/参数摘要），便于事后追溯 |

---

## 五、实施计划

### Phase 1：本机验证（本次确认后开工）

| 步骤 | 内容 | 产出 |
|------|------|------|
| 1 | 新建 `mcp-gateway/` 目录（工程根目录下），搭 Node MCP stdio 骨架（`@modelcontextprotocol/sdk`） | 网关骨架可启动 |
| 2 | 置 `edop-mcp` 服务账号（SQL 脚本，账号+只读角色+菜单授权，放 `sql/` 目录，符合规范 7） | 服务账号可登录 |
| 3 | 实现 token 管理 + 逐个实现 5 个工具（先实测现有 Controller 接口路径与出入参再封装） | 5 个工具可调 |
| 4 | 合并写入 `~/.workbuddy/mcp.json`（保留现有条目），指导你完成「信任 → 启用」 | 连接器激活 |
| 5 | 真实对话验收："帮我查一下昨天的库存""BI 指标里到货及时率是多少" | 验收记录 |
| 6 | 更新本方案文档为实施结果版 | 文档归档 |

### Phase 2：客户交付形态（Phase 1 验收通过后另起方案）

- 同一工具逻辑升级为 streamablehttp 常驻服务 + IP 白名单 + API Key
- 走腾讯云企业控制台「连接器管理」统一分发给客户组织成员
- 补充多租户映射：每个客户企业的网关指向各自的 EDOP 后端

### Phase 2 权限设计主干：逐用户鉴权（手机号 OAuth）

> 立项背景（2026-11-06 与阿磊讨论确认）：Phase 1 的共享服务账号（edop-mcp）无法区分
> "当前是哪个用户在提问"——WorkBuddy MCP 协议不向连接器传递终端用户身份。
> 目标形态：**张三的连接器 = 张三的 EDOP 身份 = 张三的数据权限**，权限完全复用平台现有用户体系，不新建权限模型。

#### 2A. 鉴权流程

```
首次使用：用户启用连接器 → 跳转 EDOP OAuth 授权页 → 手机号+短信验证码登录 EDOP
         → 用户确认授权 → EDOP 颁发该用户专属 token（绑定 user_id，非手机号）
后续使用：每次工具调用自动携带该用户 token → 网关以「他本人」身份代理调用
         → @DataScope 按其部门/角色裁剪，@PreAuthorize 按其菜单权限拦截
登出/撤销：用户可在 EDOP 侧撤销授权，或管理员强制吊销
```

设计要点：
1. **身份锚点是 user_id，不是手机号**——token 与授权关系均绑定 user_id；手机号只是登录凭证之一。这是应对手机号变更的关键设计（见 2B）。
2. EDOP 侧新增 OAuth 授权端点（授权页 + token 颁发 + 校验），RuoYi 生态有现成参考实现；登录方式复用 sys_user.phonenumber + 短信验证码。
3. WorkBuddy 侧对接企业控制台连接器的 **OAuth 2.0 / MCP OAuth 2.1** 认证类型，授权流程由平台承载，网关只做 token 校验与代理。
4. 网关从 stdio 升级 HTTP 后，"固定账号"改为"按请求 token 代理调用"；Phase 1 的共享服务账号保留，作为"平台级查询"的后备身份（如管理员排查用）。

#### 2B. 手机号生命周期治理（边界场景全覆盖）

| 场景 | 处理机制 | 说明 |
|------|---------|------|
| **用户换绑手机号** | 已颁发 token 不受影响（绑 user_id）；授权记录保存"手机号快照 + 快照时间"，网关调用时可选校验"当前 EDOP 手机号 ≠ 快照 → 要求重新授权" | 换绑不停权：在线会话平滑过渡；如客户安全等级高，开启强制重授权开关（系统参数，默认关） |
| **用户修改密码** | 复用 RuoYi 现有 token 失效策略 | 无需额外开发 |
| **账号停用/离职** | 复用 RuoYi 用户状态校验：禁用后 token 校验自然失败，授权关系标记失效 | 天然兜底，连接器即时断权 |
| **手机号被运营商回收再分配**（新员工用旧号） | 授权绑定 user_id 不随号转移；旧用户的授权不因号码换人而延续；新用户需走完整授权流程 | 关键防线：验证码短信发往"当前该手机号在 EDOP 绑定的用户"，号码换人后验证码只会发给新用户 |
| **一人多设备/多 WorkBuddy 账号** | 同一 user_id 允许多个有效授权（按设备/授权方记录），共用同一套数据权限 | 授权表按 grant 维度管理而非全局唯一 |
| **一号多人**（夫妻共用手机号注册两个 EDOP 账号） | EDOP 侧对 sys_user.phonenumber 加唯一性约束（一个手机号仅绑一个启用账号） | 建表规范层面前置规避，接入新客户时作为初始化检查项 |
| **验证码防刷** | 图形验证码前置 + 发送频控（60s/次、日上限）+ IP 限流 | 复用 RuoYi kaptcha + 新增短信频控 |

#### 2C. 授权数据模型（草案）

```
edop_connector_grant
├─ id                  主键
├─ user_id             EDOP 用户（身份锚点）
├─ phone_snapshot      授权时手机号快照（换绑检测用，非鉴权依据）
├─ client_id           连接器/应用标识（区分来源）
├─ access_token_hash   token 哈希（不存明文）
├─ refresh_token_hash  刷新令牌哈希
├─ scope               授权范围（Phase 2 先固定只读）
├─ status              状态（有效/已撤销/已过期）
├─ last_used_time      最近调用时间（清理僵尸授权）
├─ create_time / expire_time
```

配套管理能力（企业控制台或 EDOP 后台页）：
- 用户侧：查看/撤销"我的授权"
- 管理员侧：全量授权列表、强制吊销、僵尸授权清理（90 天未使用自动过期）

#### 2D. 无域名环境（纯 IP）部署策略

客户生产环境普遍无域名、纯 IP 访问，按网络边界分两种打法：

**场景 A：客户内网部署（主流场景，无需域名）**
- 网关部署在客户内网，仅监听内网网卡：员工 WorkBuddy → `http://192.168.x.x:3000/mcp` → 内网调后端 :8081
- 明文补偿控制：API Key 必配、网关网段白名单、后端不新增暴露面、audit.log 全留痕
- 待验证项：WorkBuddy 自定义连接器是否接受 `http://`（非 https）的 streamablehttp 地址，Phase 2 实测

**场景 B：需出内网访问（远程/多分支/云端）**
- 纯 IP + 明文 HTTP 过公网不可接受；三条路：买域名（几十元/年 + 备案，推荐）、ZeroSSL IP 证书（免费但 90 天续期，过渡用）、客户现有 VPN 内挂网关

**底线约束（两场景通用）**：API Key 必配；后端不因连接器新增公网暴露面；每客户独立网关实例与独立 Key。

#### 2F. 多客户多系统扩展架构（dwx / lyjs 等分支场景）

> 背景（2026-11-06 与阿磊确认）：连接器当前基于标准版开发，后续存在多个客户、
> 多套分支代码仓（dwx、lyjs 等）并行交付的诉求。

**四大风险与对策**

| 风险 | 对策 |
|------|------|
| 接口契约漂移：客户分支改动 Controller 路径/字段/权限字符，导致工具静默失效（查空不报错） | 工具的接口映射抽为 manifest 配置；标准版维护《MCP 接口契约清单》，分支定制触碰契约必须登记 |
| 配置爆炸：N 客户 × baseUrl/账号/密码 | profile 配置实例化，一客户一份 JSON；部署用 PROFILE 环境变量选择 |
| 网关代码分叉：为某客户改代码导致多代码库 | 网关代码强制单源，客户差异只允许进 profiles / tool-manifests，禁止复制代码库 |
| 运维台账缺失 | 维护「客户×分支×网关地址×版本×契约偏差×Key前缀」登记表，每次升级回归后更新 |

**交付模型（2026-11-06 定稿：连接器随分支仓走）**

```
标准版 main 仓（单源）
└─ mcp-gateway/
   ├─ gateway.mjs                  # 共性引擎：登录/token/工具执行/审计/HTTP化
   ├─ tool-manifests/standard.tools.json
   └─ profiles/standard.example.json   # 配置模板（占位符）

客户分支仓（dwx / lyjs，独立）
└─ mcp-gateway/
   ├─ gateway.mjs                  # 只从 main merge，分支内禁改
   ├─ tool-manifests/
   │  ├─ standard.tools.json       # 随 merge 同步
   │  └─ dwx.tools.json            # 分支差异覆盖 + 客户专属工具
   └─ profiles/dwx.json            # 部署配置（.gitignore，不入库）
```

- 优势：客户系统+连接器一体化交付，客户服务器拉分支代码即可整体部署（docker-compose 加一个 service），无需独立网关镜像仓
- 同步流程：main 开发验证 → merge 到各分支 → **merge 冲突即契约漂移探测器**（分支动过契约内接口必然撞出，逐条核对契约清单）→ 分支补 tools/profile → 各客户服务器部署
- 部署选择：PROFILE 环境变量指定客户配置；连接器名按客户（edop-dwx / edop-lyjs）
- 客户专属需求 = 分支 tool-manifests 加专属工具；若 ≥2 客户需要同一工具，提升回 main 标准化

**三条纪律（模型成立的前提）**
1. 分支内禁止修改 gateway.mjs——共性功能只回 main 改再同步，保证 merge 无冲突
2. profiles/*.json 强制 .gitignore——仓库只提交 example 模板，真实凭据永不进 git
3. 客户专属工具只进分支 tool-manifests——通用化需求走"提升回 main"流程，防分支发散

**升级流水线**：main 发版 → 各分支 merge（冲突=契约核对）→ 各客户服务器 git pull 重启网关 → 台账登记版本

**行动项**：①Phase 2 重构时落地 engine/manifest/profiles 三层；②立即建立标准版《MCP 接口契约清单》（从现有 5 工具反推，一页纸）；③建立多客户运维台账；④mcp-gateway 的 .gitignore 补 profiles/*.json（保留 example）。

#### 2H. Phase 2 实施批次与验收标准（v1.2 补充）

**批次 2.1：网关三层重构 + HTTP 服务化 + API Key（本轮，零后端改动）**

| 步骤 | 内容 | 验收标准 |
|------|------|---------|
| 1 | profiles 层：`profiles/<客户>.json` 承载身份配置（baseUrl/账号/凭据/模式/端口/Key），PROFILE 环境变量选择，兼容旧 config.json | stdio 模式经 profile 启动，冒烟全通 |
| 2 | tool-manifests 层：`tool-manifests/<分支>.tools.json` 承载 5 工具的接口路径与参数映射 | 工具行为与重构前一致（回归冒烟） |
| 3 | 引擎单源化：gateway.mjs 重构为「构建 MCP Server + 按 manifest 注册工具」，stdio/http 双模式共用 | stdio 模式回归通过 |
| 4 | HTTP 服务化：express + StreamableHTTPServerTransport（stateless），`POST /mcp`，API Key 鉴权（x-api-key 或 Bearer），`GET /health` 健康检查 | SDK 客户端经 HTTP 完成 listTools + 工具调用；错误 Key 被 401 拒绝 |
| 5 | http:// 明文地址是否被 WorkBuddy 连接器接受 | 实测结论写入本文档 |

**批次 2.2：逐用户手机号 OAuth（下一批，需后端 Java 开发，另行评审）**

| 步骤 | 内容 |
|------|------|
| 1 | EDOP 后端 OAuth 授权端点（授权页 + 手机号验证码登录 + token 颁发/校验 + 撤销） |
| 2 | `edop_connector_grant` 授权表 + 管理页（用户自撤/管理员吊销/僵尸清理） |
| 3 | 网关按请求 token 代理调用（替换共享服务账号直连） |
| 验收 | 两个不同 EDOP 用户分别授权后，同一工具返回各自数据权限范围的结果 |

**批次 2.3：生产部署交付包（2.2 后）**

| 步骤 | 内容 |
|------|------|
| 1 | docker-compose 网关 service 模板 + 部署手册 DEPLOY.md（内网 IP / 域名两形态） |
| 2 | 客户接入指引（企业控制台连接器创建 + 成员启用，或手动粘贴 JSON） |
| 3 | 多客户运维台账模板 |

#### 2I. 明确不做（权限设计 Non-goals）

- ❌ 不在对话中让用户自报手机号做身份切换（无安全性，仅限内部演示，不入正式方案）
- ❌ Phase 2 不做写入类工具的逐用户授权（写入类整体属 Phase 3）
- ❌ 不新建独立于平台用户的权限体系（权限唯一来源 = EDOP 用户 + 角色 + @DataScope）

### Phase 3（远期，暂不承诺）

- 写入类操作（建单、审批）+ 审批流确认交互
- 评估 Spring AI 对 Spring Boot 4 的支持成熟度，决定是否收编进 Java 后端

---

## 六、Non-goals（本方案明确不做）

1. ❌ 不修改 EDOP 后端任何 Java 代码（Phase 1）
2. ❌ 不暴露写入/删除/审批类操作
3. ❌ 不做网关侧的数据缓存（保证查询实时一致）
4. ❌ 不接入 BI 之外的报表导出、文件下载能力（Phase 1）

---

## 七、风险与开放问题

| # | 风险/问题 | 应对 |
|---|----------|------|
| 1 | 各模块现有 Controller 出入参形态不统一，封装工作量可能超预期 | 实现时逐个实测，必要时按模块分批交付（先 wms+bi，后 mk+pms） |
| 2 | 服务账号密码明文存本机配置文件 | 仅本机使用可接受；Phase 2 改 API Key + 服务端托管 |
| 3 | `@modelcontextprotocol/sdk` npm 包需联网安装 | 用本机 Node 22 + npm，阿里镜像可达即可 |
| 4 | WorkBuddy 连接器信任流程 UI 可能有版本差异 | 以连接器管理页实际入口为准，卡住时按官方文档排查 |
| 5 | 长对话大查询可能产生较多 token 消耗 | 工具输出限行限页，明细类查询引导用户限定日期范围 |

---

## 八、评审确认项（请阿磊拍板）

1. **选型**：Phase 1 走 Node stdio 网关（零后端改动）→ Phase 2 升级 HTTP 服务。是否同意？
2. **第一批工具**：BI 指标 / 库存 / 销售订单 / 采购订单 / 平台概览，共 5 个只读工具。范围是否合适？要不要调整优先级（比如先只做 BI 指标验证）？
3. **服务账号**：新建 `edop-mcp` 独立账号 + 只读角色，可以吗？
4. **交付路径**：网关代码放工程内 `mcp-gateway/` 目录。可以吗？

## 九、授权入口集成进 EDOP 系统（2026-11-06 追加）

### 9.1 背景
网关自助授权页 `http://<gateway>:3000/grant` 要求用户记住地址，体验差。授权能力本就由后端 `/edop/connector/auth/*` 提供，故将入口直接集成进 EDOP 系统，零新依赖。

### 9.2 入口位置决策
| 候选 | 评估 | 结论 |
|---|---|---|
| 头像下拉菜单"AI 连接器授权" | 授权属个人行为，语义归属个人中心；全局可达、零记忆成本 | ✅ 采用 |
| 系统管理→连接器授权（已有菜单） | 管理员视角（全量列表+强制吊销，需 edop:grant:list） | 保留，定位不同 |
| 首页卡片 | 占版面、长期价值低 | 不采用 |
| AI 助手内嵌提示 | 后续增强：问答鉴权失败时提示跳转 | 备选 |

### 9.3 实现
- **前端页面**：`ruoyi-ui/src/views/system/connectorAuth/index.vue`（路由 `/user/connector-auth`，constantRoutes 登录即用、无需菜单权限）。功能：授权表单（二次验证账号密码）→ grant_token 一次性展示 → 一键生成 MCP 配置 JSON（含复制按钮）→ 我的授权列表/吊销（`/my`、`/revoke/{id}`）。风格复用全局 surface/filter-card。
- **入口**：`Navbar.vue` 头像下拉新增"AI 连接器授权"（个人中心之下）。
- **后端**：`EdopConnectorAuthController` 新增匿名 `GET /edop/connector/auth/config`，从 `application.yml` 的 `edop.connector.gateway-url`（默认 http://127.0.0.1:3000）下发网关地址；授权页据此生成客户端配置，**部署到客户环境只需改这一处配置**。
- **网关 /grant 页**：保留作为兜底（连接器配置流程中的用户仍可直接访问），非主入口。

### 9.4 生成的 MCP 配置结构（与网关页一致）
```json
{
  "mcpServers": {
    "edop-assistant": {
      "type": "streamablehttp",
      "url": "<gateway-url>/mcp",
      "headers": { "Authorization": "Bearer <grant_token>" }
    }
  }
}
```

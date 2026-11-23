# AI 问答 → 一键打开系统菜单：落地方案

> 工程：RuoYi-Vue3（ruoyi-ai / ruoyi-system / ruoyi-ui）
> 目标：用户在「AI 智能查询」或「知识库问答」后，答案下方出现可点击的系统菜单入口，点击直达目标页面。
> 状态：**已落地并通过真实接口验证**（详见第十一章）

---

## 一、范围界定（先说清做什么、不做什么）

### 本期做（P1）

| 能力 | 说明 |
|---|---|
| 数据查询后给入口 | 问"库存还有多少" → 表格下方出现【库存查询】卡片 |
| 知识问答后给入口 | 问"入库单怎么录" → 答案下方出现【入库单管理】卡片 |
| 点击直达 | `router.push` 到菜单全路径，AI 面板自动收起 |
| 权限严格隔离 | 只在用户有权的菜单里推荐；跳转前前端二次校验 |
| 规则优先省成本 | 别名/标题命中直接返回，不调模型；未命中才走 LLM |

### 本期不做（明确挡回去）

- **不做"AI 直接提交表单/发起流程"**：高风险。二期只做"预填 + 人工确认提交"。
- **不承诺"带业务条件直达"**（如某张入库单详情页）：目前只有 25 个页面消费 `route.query`，一期只保证打开到列表页。
- **不改业务页面**，不动 `sys_menu` 结构。

---

## 二、总体链路

```
用户提问
   │
   ├─(A) 数据查询 /ai/nlquery/query ──┐
   └─(B) 知识问答 /ai/knowledge/qa ───┤
                                      ▼
                        AiNavService.resolve(question, userId)
                                      │
              ① 取该用户有权菜单树 selectMenuTreeByUserId(userId)
                 → 建索引 {menuId → 全路径 + 面包屑}
              ② 取启用意图表 ai_nav_intent，按 menuId 是否在索引内过滤（=权限过滤）
              ③ 规则命中：别名/标题关键词匹配 → 直接返回（0 token）
              ④ 未命中：LLM 在候选意图里选 → {"k":"x","c":0.8}
              ⑤ 组装 actions[{title,path,query,breadcrumb,icon,confidence}]
                                      │
                                      ▼
                        响应体新增 actions 字段（主答案不变）
                                      │
                                      ▼
             前端 AiNavActions 卡片 → 点击 → 二次校验 → router.push
```

**安全红线**：`path` 永远由服务端按 `menu_id` + 权限菜单树实时算出，**不接受模型返回的路径字符串**。模型只能选 `intentKey`。这与现有"LLM 只能选工具名、不能写 SQL"的保护口径一致。

---

## 三、数据模型

### 3.1 建表 SQL（可直接执行，幂等）

```sql
DROP TABLE IF EXISTS ai_nav_intent;
CREATE TABLE ai_nav_intent (
  intent_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  intent_key   VARCHAR(64)  NOT NULL                COMMENT '意图标识（LLM选择用）',
  menu_id      BIGINT       NOT NULL                COMMENT '目标菜单ID，路径由服务端实时计算',
  module_code  VARCHAR(32)  NOT NULL                COMMENT '模块编码，两级路由用',
  title        VARCHAR(100) NOT NULL                COMMENT '展示名称，如 入库单管理',
  aliases      VARCHAR(500) DEFAULT ''              COMMENT '别名/同义词，逗号分隔',
  examples     VARCHAR(500) DEFAULT ''              COMMENT '示例问句，给模型做语义参考',
  nav_query    VARCHAR(300) DEFAULT ''              COMMENT '跳转附加参数JSON，如 {"status":"0"}',
  doc_id       BIGINT       DEFAULT NULL            COMMENT '早期设计的文档绑定字段（已不参与解析，见 11.2）',
  sort_no      INT          DEFAULT 0               COMMENT '排序权重（越大概率越高）',
  status       CHAR(1)      DEFAULT '0'             COMMENT '0正常 1停用',
  create_by    VARCHAR(64)  DEFAULT '',
  create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_by    VARCHAR(64)  DEFAULT '',
  update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (intent_id),
  UNIQUE KEY uk_intent_key (intent_key),
  KEY idx_module (module_code),
  KEY idx_menu (menu_id),
  KEY idx_doc (doc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI导航意图表：问题→系统菜单';
```

**关键设计**：只存 `menu_id`，**不存 path**。菜单改名、改路径、改权限时无需同步维护本表，从根本上避免"AI 推荐的路径 404"。

### 3.2 文档绑定字段（P2）

```sql
ALTER TABLE ai_knowledge_doc
  ADD COLUMN nav_menu_id BIGINT DEFAULT NULL
  COMMENT '关联功能入口菜单ID，问答命中该文档时直接给出入口';
```

> MySQL 8 不支持 `ADD COLUMN IF NOT EXISTS`，脚本里用存储过程判断 `information_schema` 保证幂等。

### 3.3 种子意图数据（真实 menu_id，已核对路径）

```sql
INSERT INTO ai_nav_intent
(intent_key, menu_id, module_code, title, aliases, examples, sort_no, status) VALUES
('wms_inbound',   2060,   'wms',  '入库单管理', '入库,入库单,收货,收货单,到货登记,收料,验收入库',
 '入库单在哪里录入,怎么收货入库,入库流程怎么走,收料在哪登记', 20, '0'),
('wms_inventory', 2080,   'wms',  '库存查询', '库存,存货,库存查询,还有多少料,结存,现存量,库存量',
 '查库存,某个物料还有多少库存,库存还有多少', 20, '0'),
('wms_stocktake', 2090,   'wms',  '盘点任务', '盘点,盘库,清点,盘点单',
 '怎么发起盘点,盘点任务在哪', 15, '0'),
('wms_material',  2010,   'wms',  '物料管理', '物料,物料档案,物料新增,物料编码',
 '怎么新增物料,物料档案在哪维护', 15, '0'),
('bid_notice',    905887, 'bid',  '招标公告库', '招标,招标公告,招标信息,投标机会',
 '招标公告在哪看,哪里能看到招标信息', 15, '0'),
('flow_launch',   905825, 'flow', '流程发起', '发起流程,提流程,提审批,申请审批,审批流程,提申请,走流程',
 '怎么发起请假流程,审批怎么提交,我要提单,请假在哪申请', 20, '0'),
('fin_partner',   905940, 'fin',  '往来单位管理', '往来单位,客户档案,供应商档案,往来档案',
 '往来单位在哪维护,客户档案怎么建', 12, '0'),
('ai_nlquery',    905802, 'ai',   '智能查询', '智能查询,自然语言查询,问数据,AI查数',
 '怎么用AI查数据,智能查询在哪', 10, '0')
ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id), aliases=VALUES(aliases), examples=VALUES(examples);
```

### 3.4 开关（沿用现有配置体系）

```sql
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'AI导航开关', 'ai.nav.enabled', 'true', 'Y', 'admin', NOW(), 'AI问答后是否显示系统菜单入口'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'ai.nav.enabled');
```

---

## 四、后端实现（ruoyi-ai 模块）

模块依赖已具备：`ruoyi-ai/pom.xml` 已依赖 `ruoyi-system`，可直接注入 `ISysMenuService`。Mapper 扫描范围 `com.ruoyi.**.mapper`（`ApplicationConfig.java:20`）已覆盖，无需改配置。

### 4.1 文件清单

| 文件 | 作用 |
|---|---|
| `domain/AiNavIntent.java` | 意图实体 |
| `mapper/AiNavIntentMapper.java` | 注解式 CRUD（不新建 XML） |
| `service/IAiNavService.java` | 接口 |
| `service/impl/AiNavServiceImpl.java` | 核心：菜单索引 + 规则匹配 + LLM 兜底 |
| `controller/AiNavController.java` | 管理/自查接口 |
| `ruoyi-ui/src/api/ai/nav.js` | 前端 API |
| `ruoyi-ui/src/components/AiNavActions/index.vue` | 入口卡片组件 |
| `ruoyi-ui/src/views/ai/nav/index.vue` | 配置页 |

### 4.2 核心 Service 的四个关键方法

**① 菜单索引 `currentUserMenuIndex()`**

```java
List<SysMenu> menus = menuService.selectMenuTreeByUserId(SecurityUtils.getUserId());
walk(menus, "", null, 0, index);   // 递归构建 menuId → {path,title,breadcrumb,icon,moduleCode}
```

路径拼接规则必须与 `SysMenuServiceImpl.getRouterPath` 一致：
- 一级目录：`"/" + menu.path`
- 子级节点：父路径 + `menu.path`
- 只收 **C（菜单）类型且配置了 component 的叶子节点**（目录/按钮/外链全部排除）

**② 权限过滤 `filterByPermission()`**

```java
for (AiNavIntent it : all) {
    if (it.getMenuId() != null && index.containsKey(it.getMenuId())) allowed.add(it);
}
```

菜单不在当前用户菜单树里 → 该意图不参与推荐，**不泄露菜单存在性**。

**③ 规则匹配 `matchByRule()`**（零模型成本）

- 标题命中 → 置信度 0.9
- 别名命中 → 置信度 0.8
- 别名长度要求 ≥2 字符（防"库"这类单字误命中）

**④ LLM 兜底 `routeByAi()`**

只在规则未命中时调用，提示词里只给候选的 `k(标识)/t(名称)/a(别名)/e(示例)`，
要求模型输出 `{"k":"功能标识","c":置信度}`，**不允许输出路径**。
置信度低于 `MIN_CONFIDENCE=0.55` 直接丢弃，避免带错路。

### 4.3 接入 Controller（各 4 行）

**`AiNlQueryController.query`**

```java
Map<String, Object> result = toolRouteService.query(question);
try {
    List<Map<String, Object>> actions = navService.resolve(question);
    if (!actions.isEmpty()) result.put("actions", actions);
} catch (Exception e) {
    logger.warn("AI导航解析失败（不影响查询）: {}", e.getMessage());
}
```

**`AiKnowledgeController.qa`**

```java
Map<String, Object> result = knowledgeService.answer(module, question);
try {
    List<Map<String, Object>> actions = navService.resolveBySources(question, srcs);
    if (!actions.isEmpty()) result.put("actions", actions);
} catch (Exception e) { /* 静默降级 */ }
```

**为什么放在 Controller 而不是 Service**：`resolve` 依赖 `SecurityUtils.getUserId()` 取登录人；
Controller 层有完整安全上下文，且不污染 `AiKnowledgeServiceImpl` 的纯检索逻辑。

---

## 五、前端实现

### 5.1 卡片组件 `AiNavActions/index.vue`

三件套：图标 + 菜单名 + **面包屑**（建立信任的关键，用户得知道会被带到哪）。

**点击前的二次校验**（安全设计）：

```js
function pathAllowed(path) {
  // 递归收集当前用户 sidebarRouters 的全部叶子路径，口径与 SidebarItem.resolvePath 一致
  // 服务端已过滤过一遍，但角色权限可能在会话期间被变更，这里再查一次
}
function go(a) {
  if (!pathAllowed(a.path)) { emit('navigate', { ...a, denied: true }); return }
  emit('navigate', a)
  router.push({ path: a.path, query: a.query || {} })
}
```

路径拼接必须复用项目既有工具，否则会与侧边栏口径不一致：

```js
import { isExternal } from '@/utils/validate'
import { getNormalPath } from '@/utils/ruoyi'
```

### 5.2 错误文案的语义分流（重要，见 11.2）

用户问「入库单在哪里录入」时，NL2SQL 必然判定"超出可查询范围"（它只懂查数据、不懂找功能）。
若显示"不在可查询范围"再配一个"入库单管理"入口，用户会困惑。

```js
function dataErrorText(m) {
  if (hasActions(m)) return '这个问题更适合直接打开对应功能页面，入口见下方。'
  return m.error
}
```

同时空数据提示也做了抑制：

```vue
<div v-else-if="!hasActions(m)" class="ai-data-norows">{{ m.emptyReason || '...' }}</div>
```

### 5.3 接入面板（3 处）

- 数据结果块末尾、知识答案块末尾各插一行 `<ai-nav-actions>`
- `handleAsk` 两个分支各加 `actions: d.actions || []`
- 历史回放 `replayHistory` 也从 `navActionsJson` 还原，否则回看历史看不到入口
- `onNavigate` 收到正常跳转时 `open.value = false`，收起浮层让用户看到目标页面

---

## 六、施工顺序（每步可独立验证）

| 步骤 | 内容 | 验证方式 | 预估 |
|---|---|---|---|
| **S1** | 执行建表 SQL + 种子 + 配置 | `SELECT count(*) FROM ai_nav_intent` = 8 | 10 min |
| **S2** | 建 `AiNavIntent` + Mapper | 启动无报错，list 接口返回 8 条 | 20 min |
| **S3** | 建 `AiNavServiceImpl` | 调 `/ai/nav/myMenus` 能看到菜单索引 | 2 h |
| **S4** | 接入两个 Controller | POST 查询响应里出现 `actions` | 20 min |
| **S5** | 前端组件 + 面板接入 | 提问后出现卡片，点击能跳转 | 1.5 h |
| **S6** | 联调：权限 / 空结果 / 降级 | 见第七章验收清单 | 1 h |

合计约 **1.5 人日**（不含 P2 管理界面）。

### 关键验证点（S3 必须先过）

1. **菜单索引路径正确**。用 `/ai/nav/myMenus` 抽查 3 条，特别核对：
   - `2080 库存查询` → `/wms/inv/list`（菜单 path 是 `inv/list`，但组件是 `wms/inventory/index`，两者不是一回事）
   - `905810 知识问答` → `/ai/qa`（**不是** `/ai/knowledge/qa`，后者是接口地址）
   - `905940 往来单位管理` → `/fin/base/info`
   这三点是最易错处，务必先核对再往下做。
2. **权限过滤生效**。用非 admin 账号登录，确认看不到 `fin`/`bid` 等未授权模块入口。
3. **降级安全**。断网或 AI 失败 → 问答仍返回答案，只是没有卡片（不报错、不白屏）。

---

## 七、验收清单（交付前逐条过）

- [ ] 提问"入库单在哪录入" → 出现【入库单管理】卡片，点击跳到 `/wms/inbound/order`
- [ ] 提问"库存还有多少" → 表格下方出现【库存查询】卡片（**这是"查数据+给入口"双模态，不是替换表格**）
- [ ] 非授权账号提问采购相关问题 → **不出现**采购菜单卡片（不泄露菜单存在性）
- [ ] 断网/AI 失败 → 正常返回答案，无卡片，无报错
- [ ] 同一问题不重复出现相同路径卡片
- [ ] 卡片面包屑显示正确（如"仓储管理 / 入库管理 / 入库单管理"）
- [ ] 点击后 AI 浮层自动收起
- [ ] `ai.nav.enabled` 改为 `false` → 卡片消失，其他功能不受影响
- [ ] 删除某菜单的用户授权后点击该卡片 → 提示"暂无权限"，不跳 404 白屏
- [ ] AI 月度限额用尽 → 问答降级正常，仅无卡片
- [ ] 知识文档绑定入口后，问答命中该文档 → `matchedBy=doc`、`confidence=1.0`

---

## 八、上线后怎么持续变好（决定成败的部分）

### 8.1 三类数据要沉淀

| 数据 | 用途 | 存放 |
|---|---|---|
| 推荐了什么、用户点没点 | 算命中率、找错配 | `ai_call_log` feature=`ai_nav_route`；点击单独上报 |
| 用户从哪进、进了几次 | 候选排序权重 | 复用 `sys_user_shortcut` 的 `visit_count` |
| 没匹配上的问题 | 补别名/补意图 | `actions` 为空的问答抽样进待办 |

**最小埋点**（前端 `onNavigate` 调已有接口 `/home/shortcuts/hit`）：

```js
function onNavigate(a) {
  if (a && a.denied) { /* 提示无权限 */ return }
  hitShortcut({ path: a.path, title: a.title, icon: a.icon, moduleCode: a.moduleCode }).catch(() => {})
  open.value = false
}
```

不需要新表，同时拿到"点击次数"和"常用功能排序"两个收益。

### 8.2 别把别名维护当技术活

`ai_nav_intent.aliases` 是**产品资产**，不是代码：

- 先由实施人员按客户实际叫法填第一批（设备叫"机台"、入库叫"收料"）
- 每次客户抱怨"AI 找不到"时只改这张表，不重启、不改代码
- 每月拉一次 `actions` 为空的问答清单，补 10~20 条别名

### 8.3 别名命中率的实测基线

用 8 条种子数据做 14 句真实问法的规则匹配测试：

| 问法 | 命中 | 依据 |
|---|---|---|
| 入库单在哪里录入 | 入库单管理 | 别名:入库 |
| 怎么收料 | 入库单管理 | 别名:收料 |
| 我要验收入库 | 入库单管理 | 别名:入库 |
| XX物料库存还有多少 | 库存查询 | 别名:库存 |
| 现存量查一下 | 库存查询 | 别名:现存量 |
| 怎么发起盘点 | 盘点任务 | 别名:盘点 |
| 物料编码在哪维护 | 物料管理 | 别名:物料 |
| 哪里能看到招标信息 | 招标公告库 | 别名:招标 |
| 我要提个审批流程 | 流程发起 | 别名:审批流程 |
| 客户档案怎么建 | 往来单位管理 | 别名:客户档案 |
| 怎么用AI查数据 | 智能查询 | 别名:AI查数 |
| 请假在哪申请 | **未命中** | 交 LLM |
| 今天天气怎么样 | 未命中 | 应判定不导航 |
| 上个月回款多少 | 未命中 | 这是查数据，不该导航 |

**结论**：规则命中约 **79%**，剩余约 21%（尤其语序变化、同义表达）必须靠 LLM 兜底。

两个直接用途：

1. **别指望规则全覆盖**。删掉 LLM 分支只留规则，会有两成问题找不到入口，体验明显断裂。
2. **别名要按客户实际叫法填**。"收料""现存量""提审批"不是产品词典里的标准词，是客户现场说法。

### 8.4 与知识库的联动（P2）

给 `ai_knowledge_doc` 加 `nav_menu_id`，上传表单加"关联功能入口"下拉。
每上传一份《入库操作手册》，问答命中该文档就能**确定性**给出【入库单管理】入口，不依赖模型。

这是客户最容易感知价值的地方——"AI 不但告诉我怎么做，还直接把我送到那个页面"。

---

## 九、风险与应对（必须让客户知道）

| 风险 | 表现 | 应对 |
|---|---|---|
| 菜单标题缺业务语义 | "台账/列表/管理"模型分不清 | 靠 `aliases` + `examples` 补，这是主要工作量 |
| 一词多义 | "设备故障怎么处理"可能查记录也可能去报修 | 置信度阈值 0.55；低于阈值给两个入口 |
| 跳转参数页面不接 | 带 `status=0` 过去了页面没筛选 | 一期只承诺打开列表页；`nav_query` 预留 |
| 权限漂移 | 推荐了用户已失去权限的菜单 | 服务端实时算 + 前端 `pathAllowed` 二次校验 |
| 提示词过长 | 菜单多了 token 飙升 | 超 40 条截断；P2 改两级路由（先选模块再选菜单） |
| 客户期望膨胀 | "点了能自动填好单据" | 方案里明确边界：本期只做"带到页面" |

---

## 十、给客户演示的话术（P0→P1 之间用）

建议先花半天做**纯前端假数据 Demo**，不写后端，直接在 `AiAssistant/index.vue` 硬编码 3 条 `actions`：

```js
actions: [{ title:'入库单管理', path:'/wms/inbound/order',
            breadcrumb:'仓储管理 / 入库管理', icon:'list', confidence:1 }]
```

理由很实际：客户对"卡片长什么样、点起来什么感觉"的判断，比看 20 页设计文档快 10 倍。
如果客户看了觉得"也就那样"，能省下 1.5 人日；如果说"对，就是这个"，再正式施工，返工率为零。

---

## 附录一：与现有代码的对应关系（便于 review）

| 本文档内容 | 现有可复用点 |
|---|---|
| 权限菜单树 | `SysMenuServiceImpl.java:139-151` `selectMenuTreeByUserId` |
| 路径拼接规则 | `SysMenuServiceImpl.java:459-479` `getRouterPath`、`:511-515` `isMenuFrame` |
| 前端路径规范化 | `SidebarItem.vue:79-98` `resolvePath` + `utils/ruoyi.js:228-237` `getNormalPath` |
| 菜单标题→路径索引 | `views/business-map/index.vue:270-327`（已在生产使用） |
| 权限过滤写法 | `AiToolRouteServiceImpl.java:198-215` |
| 降级不阻断主流程 | `AiToolRouteServiceImpl.java:155-161` |
| 配置开关 | `ISysConfigService.selectConfigByKey`，同 `ai.nl2query.enabled` |
| 点击埋点接口 | `HomePersonalController.java:80` `POST /home/shortcuts/hit` |
| AI 调用 | `AiClient.chatWithTimeout(module, feature, prompt, seconds)` |

## 附录二：已验证的真实菜单路径（种子数据对照用）

以下路径由 `sys_menu` 的 `parent_id/path` 逐级拼接得到，与前端 `resolvePath` 一致，已逐条核对：

| 菜单 | menu_id | 真实路径 | 组件 |
|---|---|---|---|
| 入库单管理 | 2060 | `/wms/inbound/order` | `wms/inbound/index` |
| 入库作业 | 2061 | `/wms/inbound/detail` | `wms/inbound/detail` |
| 物料管理 | 2010 | `/wms/base/material` | `wms/material/index` |
| 库存查询 | 2080 | `/wms/inv/list` | `wms/inventory/index` |
| 盘点任务 | 2090 | `/wms/stocktake/task` | `wms/stocktake/index` |
| 智能查询 | 905802 | `/ai/nlquery` | `ai/nlquery/index` |
| 知识问答 | 905810 | `/ai/qa` | `ai/knowledge/qa` |
| 流程发起 | 905825 | `/flow/launch` | `flowable/launch/index` |
| 招标公告库 | 905887 | `/bid/notice` | `bid/notice/index` |
| 往来单位管理 | 905940 | `/fin/base/info` | `fin/partner/index` |
| 设备台账 | 3020 | `/dms/base/equipment` | `dms/equipment/index` |

**两个易错点**：

1. **菜单 `path` 与组件路径不是一回事**。`库存查询` 的菜单 path 是 `inv/list`，组件是 `wms/inventory/index`。
2. **`知识问答` 页面路径是 `/ai/qa`，不是 `/ai/knowledge/qa`**（后者是后端接口地址）。
   手工写种子数据极易写错——这是必须用服务端算路径的核心原因。

---

## 十一、实施记录（2026-11-23 已落地并通过真实接口验证）

本节记录**实际交付状态**，与前文设计文档的差异也一并说明，避免开发照着过时设计返工。

### 11.1 已交付文件清单

**新增（后端 5 个类）**

| 文件 | 说明 |
|---|---|
| `ruoyi-ai/.../domain/AiNavIntent.java` | 意图实体 |
| `ruoyi-ai/.../mapper/AiNavIntentMapper.java` | 注解式 Mapper，无 XML |
| `ruoyi-ai/.../service/IAiNavService.java` | 接口 |
| `ruoyi-ai/.../service/impl/AiNavServiceImpl.java` | 核心：菜单索引 + 规则匹配 + LLM 兜底 |
| `ruoyi-ai/.../controller/AiNavController.java` | 管理接口 + 自查 |

**新增（前端 3 个）**

| 文件 | 说明 |
|---|---|
| `ruoyi-ui/src/api/ai/nav.js` | 意图管理 API |
| `ruoyi-ui/src/components/AiNavActions/index.vue` | 入口卡片组件（含权限二次校验） |
| `ruoyi-ui/src/views/ai/nav/index.vue` | 菜单直达配置页 |

**新增（数据库）**

| 文件 | 说明 |
|---|---|
| `sql/ai_nav_intent.sql` | 建表 + 8 条种子 + 字段升级 + 菜单权限，幂等可重复执行 |

**改造（既有文件）**

| 文件 | 改动 |
|---|---|
| `AiNlQueryController.java` | 查询后解析 actions；历史落 `nav_actions_json` |
| `AiKnowledgeController.java` | 问答后解析 actions；上传支持 navMenuId；新增 `PUT /ai/knowledge/nav/{docId}`；docInfo/docByName 返回 navMenuId |
| `AiQaHistory.java` + `AiQaHistoryMapper.xml` | 新增 `nav_actions_json` |
| `AiNlqHistory.java` + `AiNlqHistoryMapper.xml` | 新增 `nav_actions_json` |
| `AiKnowledgeDoc.java` + `AiKnowledgeDocMapper.java/.xml` | 新增 `nav_menu_id`；新增 `selectNavMenuIdByDocId` |
| `AiAssistant/index.vue` | 接入卡片；错误文案语义分流；历史回放带 actions |
| `views/ai/knowledge/index.vue` | 新增"功能入口"列 + 绑定弹窗 + 上传时可选入口 |

### 11.2 与设计文档的差异（重要）

**差异一：P2 的唯一事实来源是 `ai_knowledge_doc.nav_menu_id`，不是 `ai_nav_intent.doc_id`**

原始设计在两处都留了文档绑定字段，实测发现这会导致配置分散、行为不一致。
最终实现统一为：**`ai_knowledge_doc.nav_menu_id` 是文档绑定的唯一来源**。
`ai_nav_intent.doc_id` 字段保留但已不参与导航解析。

**差异二：查询报错与入口同时存在时的文案处理**

真实联调暴露：用户问「入库单在哪里录入」时，NL2SQL 一定判定"超出可查询范围"
（它只懂查数据、不懂找功能），此时若显示"不在可查询范围"再配一个"入库单管理"入口，用户会困惑。

处理方式（已实现 `dataErrorText`）：
- 有入口时，错误文案替换为「这个问题更适合直接打开对应功能页面，入口见下方。」
- 无入口时，保留原错误文案
- 同时抑制"没有查到匹配数据"的提示，避免双重负反馈

**差异三：数据查询历史用独立字段存入口**

原计划把 actions 塞进 `result_json`，实测再塞会明显增大体积且有截断风险
（参考 `sources_json` 只有 `varchar(2000)`）。
最终改为两处历史表各加独立字段 `nav_actions_json`。

### 11.3 真实接口验证结果（非模拟）

后端重启加载新代码后实际调用：

**路径正确性（用数据库中 1423 条真实菜单离线计算）**

11/11 条关键路径全部与前端 `resolvePath` 一致，包括最易错的两条：
- `库存查询` → `/wms/inv/list`（菜单 path `inv/list`，组件 `wms/inventory/index`）
- `知识问答` → `/ai/qa`（不是 `/ai/knowledge/qa`）

**端到端（真实 HTTP 调用）**

| 提问 | 返回入口 | 路径 | 来源 |
|---|---|---|---|
| 库存还有多少 | 库存查询 | `/wms/inv/list` | rule (0.8) |
| 入库单在哪里录入 | 入库单管理 | `/wms/inbound/order` | rule (0.8) |
| 我要提个审批流程 | 流程发起 | `/flow/launch` | rule (0.8) |
| 客户档案怎么建 | 往来单位管理 | `/fin/base/info` | rule (0.8) |
| CNC车床操作规程（知识问答） | 设备台账 | `/dms/base/equipment` | **doc (1.0)** |
| 今天天气怎么样 | 无入口 | — | — |

**权限隔离（安全红线）**

撤销测试角色对 `fin` 模块的菜单授权后，重新登录刷新权限缓存，
再次提问「客户档案怎么建」→ **无入口**，不泄露该菜单存在性。

**P2 文档直连**

给 `CNC车床操作规程.txt` 绑定菜单 3020（设备台账）后，
知识问答返回 `matchedBy: "doc"`、`confidence: 1.0`，走确定性映射，完全不依赖模型。

### 11.4 联调中发现的自身缺陷（已修复）

**缺陷 1：`docInfo`/`docByName` 接口漏返回 `navMenuId`**

绑定接口返回成功、数据库也确实写入了 3020，但前端拿到的 `navMenuId` 是 `null`。
原因是这两个接口手工组装 Map，新增字段后没同步补充。已修复。

**缺陷 2：`resolveByDocBinding` 读错了表**

原实现查 `ai_nav_intent.doc_id`，但 P2 绑定实际写在 `ai_knowledge_doc.nav_menu_id`，
导致"绑定成功但问答不返回入口"。已改为直接读 `ai_knowledge_doc.nav_menu_id`。

> 这两个缺陷都是**只有真实接口联调才能发现**的：
> 编译通过、离线算法验证通过，但字段没打通。
> 这也是本文档强调"必须真实联调"的原因。

### 11.5 尚未覆盖（明确留待后续）

| 项 | 现状 | 建议 |
|---|---|---|
| 带业务条件直达 | `nav_query` 字段预留，页面未适配 | 需改业务页面消费 `route.query` |
| 点击埋点 | 未接 | 在 `onNavigate` 调 `/home/shortcuts/hit` |
| 管理页面权限粒度 | 用 `ai:nav:*` 五个按钮权限 | 按需细化到模块级 |

### 11.6 全模块核心功能预置（2026-11-23 补充）

**背景**：初版只有 8 条种子意图，覆盖面太窄——大量模块的功能找不到入口。

**结果**：扩充到 **116 条**，覆盖 14 个业务模块的核心功能。

| 模块 | 条数 | 模块 | 条数 |
|---|---|---|---|
| 人力资源 hr | 12 | 生产管控 mms | 12 |
| 财务管理 fin | 11 | 安全生产 safety | 11 |
| 仓储管理 wms | 10 | 营销管理 mk | 10 |
| 设备管理 dms | 9 | 质量管理 qms | 9 |
| 采购管理 pms | 8 | 决策支持 bi | 7 |
| 产品设计 pdm | 5 | 流程中心 flow | 4 |
| 综合办公 oa | 4 | AI中心 ai | 4 |

**收录原则**（写脚本前定好的筛选标准）：

1. 只收「C 类型 + 配置了 component」的叶子菜单（目录/按钮/外链不可跳转）
2. 排除隐藏菜单（`visible=1`，如"报工管理""工厂日历"需从其他页面进入）
3. 排除纯配置类（编号规则、采集配置、参数设置）
4. 统计看板每模块只留模块级入口（如"销售看板"），不与业务菜单混淆
5. 别名按现场口语填（收料、现存量、提审批、机台、花名册）

**技术配套：两级路由（必须，否则功能失效）**

扩充到 116 条后，原有的单级路由会失效——`MAX_CANDIDATES=40` 会把候选截断，
排在第 40 名之后的意图**永远无法被选中**。

实现方案（`narrowByModule`）：
1. 候选 ≤ 40 条 → 原单级路由不变
2. 候选 > 40 条 → 先收敛模块：按问题的标题/别名命中情况给各模块打分，
   取命中数最高的 3 个模块（零模型成本）；关键词全不命中时用 LLM 选模块
   （模块只有 14 个，候选极小，成本可控）
3. 再在选中的模块内选具体功能

**实测验证**（116 条全量下）：

| 提问 | 收敛模块 | 候选数 |
|---|---|---|
| 入库单在哪里录入 | wms | 10 |
| 设备保养计划在哪 | dms | 9 |
| 员工请假怎么申请 | hr | 12 |
| 凭证在哪录入 | fin | 11 |
| 生产异常怎么上报 | mms | 12 |
| 客户投诉怎么处理 | mk + qms | 19 |
| 备件库存怎么查 | dms + wms | 19 |
| 供应商档案在哪维护 | wms + pms + fin | 29 |

全部收敛到 40 条以内，两级路由生效。

**已验证的跨模块命中**（真实接口）：

| 提问 | 命中 | 路径 | 来源 |
|---|---|---|---|
| 客户在哪维护 | 客户管理 | `/mk/customer/list` | rule |
| 销售订单在哪录 | 订单管理 | `/mk/contract/order` | rule |
| 采购申请怎么提 | 采购申请 | `/pms/demand/request` | rule |
| 隐患怎么上报 | 隐患管理 | `/safety/dualcontrol/hazard` | rule |
| 凭证在哪录 | 总账凭证 | `/fin/daily/gl` | rule |
| 请假在哪申请 | 请假管理 | `/hr/attendance/leaveRequest` | rule |

**一个值得记录的多模块重名问题**

"工单"在设备管理（`dms_3040` 工单列表）和生产管控（`mms_5090` 工单管理）中同名。
- 只说"工单在哪看" → 可能命中任一模块（业务上两者都有道理）
- 说"**设备维修工单**在哪看" → 正确命中 `dms/workorder/list`，走 LLM 语义路由（`matchedBy=ai`）

这验证了两级路由的设计价值：靠模块收敛把歧义交给模型在**小范围**内判断，
而不是在 116 条候选里盲选。

**使用建议**

种子数据里的别名是通用说法，上线后仍需按客户实际口语补充。
116 条是"默认打底"，不是"最终形态"——建议按第八章 8.2 的方法持续迭代。


### 11.7 上线前必做

1. **执行 `sql/ai_nav_intent.sql`**（幂等，可重复执行）
2. **给相关角色授权**：`ai:nav:*`、`ai:knowledge:nav`（脚本只自动授予 `role_id=1`）
3. **补别名**：种子数据按通用说法填写，上线后按客户实际口语补充
   （客户把设备叫"机台"、入库叫"收料"，这些不在任何产品词典里）
4. **重启后端**，前端 `npm run dev` 或重新构建
5. **验收**：任选 3 个模块提问，确认卡片出现且点击能正确跳转

### 11.8 已知环境问题（非本功能引入）

**权限装载的隐藏规则**（配置角色时必须注意）：

- `SecurityUtils.isAdmin` 是 `userId == 1` 的硬编码判断
- `SysPermissionService.getMenuPermission` 中，非 admin 用户挂 `admin` 角色时，
  会被 `role.isAdmin()` 跳过权限装载，导致该用户**权限为空**
- 因此给非 user_id=1 的账号配权限时，必须用**非 admin 角色**并显式勾选菜单

这是框架既有行为，与导航功能无关，但在配测试账号或新建管理员时容易踩。

**数据库连接**：`application-druid.yml` 里配置的是 Druid 加密串，
明文见同文件注释（`ZBrj@2026`）。执行 SQL 脚本需用明文密码连接。

**执行中文 SQL 时必须带字符集参数**，否则报 `Incorrect string value`：

```bash
mysql --default-character-set=utf8mb4 -uroot -p*** ry-vue < sql/ai_nav_intent.sql
```

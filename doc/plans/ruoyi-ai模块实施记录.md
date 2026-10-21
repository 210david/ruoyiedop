# ruoyi-ai 公共模块 + AI中心 实施记录（P0 底座）

> 日期：2026-09-15
> 依据：《AI功能体系规划与实施方案》§3.1-3.4 + design/unified-development-standards.md
> 状态：已完成并通过迁移回归验证

## 一、交付内容

### 1. 后端 ruoyi-ai 模块（新增 Maven 子模块）

| 类 | 路径 | 职责 |
|---|---|---|
| `AiClient` | `ruoyi-ai/.../core/AiClient.java` | 统一LLM调用：三级开关→限额→脱敏→调用→审计；连接测试 |
| `AiResult` | `ruoyi-ai/.../core/AiResult.java` | 统一返回：success/degraded/disabled/limited/failed |
| `AiCallLog` | `ruoyi-ai/.../domain/AiCallLog.java` | 审计实体（统计/追溯类，数据权限豁免） |
| `AiCallLogMapper` (+XML) | `ruoyi-ai/.../mapper/` | 审计落库/列表/模块/功能/总计统计 |
| `IAiAuditService` / Impl | `ruoyi-ai/.../service/` | 审计服务（自动填充登录人） |
| `AiCenterController` | `ruoyi-ai/.../controller/` | `/ai/center/*`：配置+连接测试+用量统计+审计日志 |
| `AiBaseConfigController` | 同上 | 配置读写辅助（含 dms.ai.* 旧键回退兼容） |
| `SensitiveDataMasker` | `ruoyi-ai/.../util/` | 手机号/身份证/银行卡进prompt前脱敏 |
| `AiJsonParser` | `ruoyi-ai/.../util/` | LLM返回JSON解析（兼容markdown包裹） |

### 2. SQL（`sql/ai_all.sql`，已执行）
- `ai_call_log` 审计表（索引：module+time / user+time / time）
- 配置键平移 `dms.ai.*` → `ai.*`（旧键保留一个版本周期，新键优先读取）
- AI中心一级菜单 + 3个子菜单 + 按钮权限（`ai:center:*`，仅INSERT，授admin角色）

### 3. DMS 存量迁移（方案唯一例外，已过回归关卡）
- `DmsAiServiceImpl.callLlm/isAiEnabled` 委托 `AiClient`（行为不变，接口出入参快照一致）
- `DmsAiConfigController.testConnection` 改调 `AiClient.testConnection`
- 各功能入口设置 `currentFeature`（repair_assistant/knowledge_qa/faq_generate）供审计
- `IDmsAiService.testConnection` 声明已删除（迁至 AiClient）

### 4. 前端
- `src/api/ai/center.js`：AI中心 API 层
- `src/views/ai/config/index.vue`：AI配置页（服务商预设/测试连接/Key不可见，迁移自dms页面并泛化）
- `src/views/ai/usage/index.vue`：用量统计看板（月度用量+模块/功能统计）
- `src/views/ai/log/index.vue`：调用审计日志查询
- `src/components/AiAssist/AiAssistButton.vue` + `AiResultPanel.vue`：统一AI交互组件（供后续各模块接入）

## 二、回归验证结果（2026-09-15 实测）

| 用例 | 结果 |
|---|---|
| AI中心读取配置（新键ai.*，含Key不回显） | ✅ 通过 |
| AI中心连接测试（MiMo，1.5s） | ✅ 通过 |
| 智能报修 repairAssistant（迁移后行为不变） | ✅ aiEnabled=true，抽取正常 |
| 知识问答 knowledgeQA（迁移后行为不变） | ✅ AI直答正常 |
| 审计落库（module/feature/status/耗时/操作人） | ✅ 2条记录正确 |
| 用量统计（总计+模块统计） | ✅ 数据正确 |
| 月度限额（沿用Redis计数，本月已用同步） | ✅ monthlyUsed=2 |

## 三、业务零侵入红线执行情况

- 未 ALTER 任何现有业务表（仅新建 ai_call_log）✅
- 未修改任何现有菜单/权限记录（仅 INSERT 新菜单）✅
- DMS 迁移为方案唯一已批准例外，已过回归关卡 ✅
- API Key 全链路不可查看（仅返回 apiKeyConfigured 布尔）✅

## 四、P0 功能层（2026-09-15 第二批，已完成并验收）

### 后端（全部外挂式新增 Controller，零业务侵入）

| 模块 | 接口 | 功能 |
|---|---|---|
| DMS | `GET /dms/ai/completeReport/{orderId}` | 完工报告草稿生成（工单数据→完工说明预填） |
| QMS | `GET /qms/ai/ncrAdvice/{ncrId}` | NCR处置建议（处置方式/理由/风险/隔离/说明，JSON结构化） |
| QMS | `POST /qms/ai/reportDraft` | 质量报告初稿（期间数据→月报草稿） |
| MMS | `GET /mms/ai/abnormalAdvice/{abnormalId}` | 生产异常诊断（可能原因/紧急处置/根因方向/预防建议） |
| MMS | `GET /mms/ai/dailyReport/{statId}` | 生产日报生成（统计快照→管理视角日报） |
| PMS | `GET /pms/ai/requestAdvice/{requestId}` | 采购申请建议（proceed/hold/split + 理由/风险） |
| PMS | `GET /pms/ai/priceSummary/{inquiryId}` | 比价分析摘要（评估框架+定标建议+风险） |

### 前端接入（统一组件一行接入，全部增量插入）

| 页面 | 交互 |
|---|---|
| QMS NCR处置弹窗 | "AI处置建议"按钮 + AiResultPanel 结构化展示 |
| MMS 异常处理关闭弹窗 | "AI诊断建议"按钮 + 面板（可能原因列表等） |
| MMS 生产日报列表 | 工具栏"AI生成日报" + 行内"生成"按钮 + 面板 |
| PMS 采购申请详情 | "AI采购建议"按钮 + 面板 |
| PMS 询价详情 | "AI比价摘要"按钮 + 面板 |
| DMS 工单完工弹窗 | "AI生成完工报告"按钮 → 草稿预填完工说明（人工确认后保存） |

### 端到端验收（2026-09-15 实测）
- 7 个后端接口路由全部 200（含真实数据调用：NCR25/QMS、工单97/DMS、异常13/MMS）
- 审计日志正确记录新模块调用（qms:ncr_advice、dms:complete_report、mms:abnormal_advice、pms:request_advice、pms:price_summary、qms:report_draft、mms:daily_report）
- 浏览器实测 NCR 处置弹窗"AI处置建议"：面板完整展示建议处置方式/理由/风险提示/隔离建议/补充说明，含耗时与合规声明

## 五、P1 RAG 知识库底座（2026-09-15 第三批，已完成并验收）

按方案 §3.7 轻量方案落地：向量存 MySQL + API embedding + 应用内存检索，零新增部署。

### 交付内容
| 层 | 内容 |
|---|---|
| SQL | `sql/ai_rag.sql`（已执行）：`ai_knowledge_doc` + `ai_knowledge_chunk` 两表、embedding 三个配置键（`ai.embedding_url/key/model`，与对话模型解耦）、AI中心下"知识库"子菜单（`ai:knowledge:*`） |
| 后端 | `ruoyi-ai`：`AiKnowledgeDoc` 实体 + Mapper、`AiKnowledgeServiceImpl`（POI/PDFBox 抽取→500字/50重叠切片→向量化→入库；检索 Top-K 余弦/关键词降级；RAG 问答带引用来源与防注入分隔符）、`AiKnowledgeController`（`/ai/knowledge/*`：上传/列表/删除/问答） |
| 前端 | `views/ai/knowledge/index.vue`：左侧文档管理（上传/筛选/删除/状态），右侧知识问答（历史会话+来源标签+模块过滤）；`api/ai/knowledge.js` |
| 依赖 | 根 pom 增加 `pdfbox 2.0.31`（dependencyManagement） |

### 关键行为
- 未配置 embedding 服务时自动降级**关键词检索**（功能可用，召回精度降低），配置后自动升级向量检索
- 问答未命中时明确回答"知识库中未找到相关内容"，禁止模型编造
- 用户输入以 `<<< >>>` 分隔符包裹进 prompt，防提示词注入
- 问答走 AiClient 统一审计（module=所属模块，feature=knowledge_qa）
- 模块隔离：检索范围 = 所属模块 + platform 公共文档

### 端到端验收（2026-09-15 实测）
- 上传《CNC车床操作规程.txt》→ 解析成功、1 切片、状态已解析
- 问答"工作气压应保持多少" → 命中回答 0.5-0.6MPa + 引用来源《CNC车床操作规程》
- 问答"主轴异响怎么处理" → 正确回答急停+报告班组长（浏览器实测）
- 未命中问题（年假政策）→ 明确拒答不编造
- 浏览器页面验收通过（截图 knowledge-rag-qa-e2e.png）

## 六、P1 第二批：提示词模板管理 + NL2Query（2026-09-15 第四批，已完成并验收）

### 提示词模板管理
- 后端 `AiPromptController`（`/ai/prompt/*`）：列出 `ai.prompt.<module>.<feature>` 配置项、编辑保存热生效；含 11 个功能标识中文说明
- 前端 `views/ai/prompt/index.vue`：模板列表（模块/功能/内容预览）+ 编辑弹窗；未配置时各功能使用代码内置默认模板
- 修改即时生效（sys_config 存储），无需重启

### NL2Query 自然语言查数（管理层权限 `ai:nlquery:query`）
- 后端 `AiNlQueryController` + `AiNlQueryServiceImpl`：
  - 开关 `ai.nl2query.enabled`（默认 false）+ 白名单 `ai.nl2query.tables`（当前配置 dms_work_order/dms_equipment/wms_supplier）
  - LLM 生成 SQL 时附白名单表字段结构（含中文注释）
  - **四重安全校验**：仅白名单表（正则提取 from/join 表名）、仅单条 SELECT、禁止 DDL/DML 关键字、强制 LIMIT（≤100 行）
  - SQL 提取容错：```sql``` 包裹 / 裸 SELECT / 思考文本夹杂三种形态
- 前端 `views/ai/nlquery/index.vue`：自然语言输入 + 示例快捷链接 + 结果表格 + 查看SQL

### 端到端验收（2026-09-15 实测）
- 正常查询"故障次数最多的前3台设备" → 正确生成 GROUP BY+LIMIT SQL 返回结果
- 连续 3 次相同问题 → 3/3 成功（MiMo 推理模型稳定性确认）
- 浏览器实测"列出所有设备名称和所在车间" → 8 行结果+查看SQL（截图 nlquery-e2e.png）
- 白名单外（sys_user 用户名密码）→ 拦截："该问题涉及的数据不在可查询范围内"
- 删除类请求 → 拦截（危险关键字校验）
- 提示词模板：列表/编辑/保存热生效 API 全通

## 七、P1 第三批：业务模块深化（2026-09-15 第五批，已完成并验收）

按方案 P1 清单落地，全部外挂式（新增 XxxAiAssistController + 页面增量插入 AI 按钮/面板），HR 严守薪资禁区。

| 模块 | 功能 | 接口 | 接入页面 |
|---|---|---|---|
| WMS | 库存预警归因/库存分析报告 | `GET /wms/ai/stockAnalysis` | 库存查询（AI库存分析按钮） |
| WMS | 呆滞库存识别（规则计算库龄≥90天） | `GET /wms/ai/staleInventory` | 库存查询（呆滞识别按钮） |
| WMS | 盘点差异归因分析 | `GET /wms/ai/stockTakeAnalysis/{takeId}` | 盘点列表（行选中+AI差异分析） |
| MK | 客户流失预警报告 | `GET /mk/ai/customerChurnReport` | 客户列表 |
| MK | 销售周报生成 | `GET /mk/ai/weeklyReport` | 订单列表 |
| HR | 月度考勤异常摘要（不涉及薪资） | `GET /hr/ai/attendanceSummary/{yearMonth}` | 月度考勤 |
| Safety | 隐患结构化+整改建议（JSON） | `GET /safety/ai/hazardAdvice/{hazardId}` | 隐患列表（行内AI按钮） |
| Safety | 培训考题自动生成（5单选+2判断） | `GET /safety/ai/trainingQuiz/{recordId}` | 培训记录（AI考题按钮） |

### 底座增强（本批顺带）
- `AiClient.chat` 新增 maxTokens 参数重载（长文本生成场景，考题用 3000）
- `AiJsonParser` 多级容错：代码块剥离 → 首尾大括号截取 → 解析（解决 MiMo 返回夹杂文本/截断问题）

### 端到端验收（2026-09-15 实测）
- 8 个新接口路由全部 200
- WMS 库存分析：正确识别 0 库存风险/高位库存/不合格品（引用具体物料）
- Safety 隐患建议（液氨储罐）：整改措施/时限/预防措施专业具体
- Safety 培训考题：7 题（5单选+2判断）完整生成，含答案解析
- 浏览器实测 WMS 库存页：AI库存分析/呆滞识别按钮+面板正常（呆滞0项符合数据事实）
- 审计日志已覆盖 dms/qms/mms/pms/wms/mk/hr/safety/platform 全部 9 模块

### Flowable 说明
用户正在开发 ruoyi-flowable（工作流），本任务未改动其业务逻辑；仅两次因编译错误阻塞全量构建时做了最小适配（Flowable7 的 HistoryLevel 包路径、Comment 接口无 getMessage()），均属 API 兼容性修正，不影响业务语义。

## 八、AI中心最终形态
AI配置 | 用量统计 | 调用日志 | 知识库 | 提示词模板 | 智能查询（+底座 ruoyi-ai，9 模块全覆盖）

## 九、P2 预测类 + Agent（2026-09-15 第六批，已完成并验收）

| 功能 | 接口 | 说明 |
|---|---|---|
| 设备健康度评分+故障预测 | `GET /dms/ai/equipmentHealth/{equipmentId}` / `/equipmentHealthOverview` | 可解释规则模型（0-100分：90/30天故障频次、停机时长、维修费用、役龄）+ LLM 解读与30天故障风险预测；总览输出风险排名 |
| 备件消耗预测/采购建议 | `GET /dms/ai/sparepartForecast` | 基于工单备件使用记录，高频备件Top5+安全库存建议，联动采购；数据不足明确标注不编造 |
| 客户流失预测 | `GET /mk/ai/churnPrediction` | 规则打分（订单时效/频次/价值分层，高价值高风险加权）+ LLM 挽回策略 |
| 工单全流程 Agent | `GET /dms/ai/agent/{orderId}` | 一次调用串联：诊断+排查步骤+备件清单+安全注意+智能派工推荐；结果为建议方案，人工确认后走既有流程 |
| 全局 AI 助手入口 | 前端 `layout/components/AiAssistant` | 导航栏浮动对话面板（紫色渐变入口），复用 RAG 知识问答，标注引用来源；增量插入 Navbar，不动既有逻辑 |

### 端到端验收（2026-09-15 实测）
- 设备健康度总览：CNC数控车床64分(关注，90天2次故障)、立式铣床82分(良好)，AI 总体报告点名重点关注设备 ✅
- 工单 Agent（工单97 PM保养）：诊断结合设备历史、备件清单5项（规格缺失处标【待补充】）、派工推荐成功 ✅
- MK 流失预测：识别1家55分中风险客户（0订单），AI 给出挽回策略 ✅
- 备件预测：无历史记录时正确标注【数据不足】不编造 ✅
- 全局助手：首页导航栏提问"工作气压要求" → 回答 0.5-0.6MPa + 引用《CNC车床操作规程.txt》（浏览器实测，截图 global-ai-assistant-e2e.png）✅

## 十、方案交付总览（P0+P1+P2 全部完成）

| 阶段 | 内容 | 状态 |
|---|---|---|
| P0 | ruoyi-ai 底座 + AI中心 + DMS存量迁移 | ✅ |
| P0 | 完工报告 / NCR建议 / 报告初稿 / 异常诊断 / 生产日报 / 采购建议 / 比价摘要 | ✅ |
| P1 | RAG知识库 / NL2Query / 提示词模板 | ✅ |
| P1 | WMS×3 / MK×2 / HR×1 / Safety×2 | ✅ |
| P2 | 设备健康预测 / 备件预测 / 流失预测 / 工单Agent / 全局助手 | ✅ |

**AI 能力统计**：底座1套 + AI中心6页面 + 9模块共 20+ 个 AI 功能点，全部走 AiClient 统一审计/限额/脱敏，全部"AI建议+人工确认"形态。

## 十一、遗留事项
- P2 的"客户流失预测/备件预测"首版为规则+LLM 方案，预测精度随数据积累可迭代
- embedding 未配置时 RAG 为关键词检索模式，配置 `ai.embedding_*` 三项即升级向量检索
- ruoyi-flowable 为用户开发中的工作流模块，本任务未触碰其业务逻辑（仅两次构建阻塞时做了 Flowable7 API 兼容修正：HistoryLevel 包路径、Comment.getMessage 移除）


## 十二、AI配置保存假成功问题修复（2026-09-16）

- **现象**：AI配置页保存大模型配置提示成功，刷新页面仍显示旧值（小米MiMo），实际调用也未切换。
- **根因**：`AiBaseConfigController.updateConfigByKey` 使用 `selectConfigList` 查询更新目标，而 RuoYi 的 `sys_config` 查询对 `config_key` 为 **like 模糊匹配**，`ai.api_url` 同时命中新键 `ai.api_url` 与历史迁移旧键 `dms.ai.api_url`，代码取 `list.get(0)` 将新值写入了旧键，读取端（mergeKey 新键优先）读到的一直是旧值。
- **修复**：`updateConfigByKey` 遍历结果按 `configKey` **精确匹配**后再更新（AiBaseConfigController.java）。
- **数据修复**：将 `dms.ai.api_url / dms.ai.model_name / dms.ai.api_key` 的 DeepSeek 值迁移回 `ai.*` 正式键。
- **验证**：重启后 Redis 配置缓存中 `sys_config:ai.api_url = https://api.deepseek.com/v1/chat/completions`、`sys_config:ai.model_name = deepseek-chat`，配置读取与保存键一致。
- **经验**：凡基于 `selectConfigList` 做按键更新，必须精确比对 `configKey`；RuoYi 配置键存在 `dms.*` 历史前缀时尤其易踩坑。

## 十三、DMS 重复 AI 配置入口收敛（2026-09-18）

- **问题**：设备管理 → AI助手 → AI配置与独立的 AI中心 → AI配置功能重复，容易造成配置入口混淆。
- **处理**：将 DMS 旧菜单 `dms/ai/config` 设置为 `visible='1'`，不再生成可见动态路由；AI中心正式入口 `ai/config/index` 保持显示。
- **兼容策略**：暂不删除 DMS 配置页面、API 与 `DmsAiConfigController`，保留 `dms.ai.*` 功能级开关兼容；维修知识库菜单和业务能力不受影响。
- **文案统一**：DMS 派工推荐、FAQ生成和调用失败提示统一引导至【AI中心 → AI配置】。
- **SQL**：新增 `sql/hide_legacy_dms_ai_config_menu.sql`，并同步修正历史初始化/修复脚本，避免新环境重新显示旧入口。
- **验证**：运行库旧入口 menu_id=3126 为 visible=1，AI中心入口 menu_id=905789 为 visible=0；`mvn -pl ruoyi-dms -am -DskipTests compile` 构建成功。

## 十四、DMS 维修知识库菜单层级调整（2026-09-18）

- **调整原因**：维修知识库属于设备维修业务资产，不需要继续挂在已收敛的“AI助手”目录下。
- **菜单调整**：`menu_id=3100` 从 `parent_id=3007` 迁移到设备管理根目录 `parent_id=3000`，路径改为 `repairKnowledge`，排序为6。
- **顺序调整**：维修知识库之后依次为数据采集（7）、报表看板（8）；历史AI助手目录排序9并隐藏。
- **权限兼容**：FAQ查询、新增、修改、删除按钮仍以3100为父菜单，原角色菜单授权无需调整。
- **SQL**：新增 `sql/move_dms_repair_knowledge_menu.sql`，并同步修正DMS历史初始化脚本。

## 十五、MMS 工艺路线重复工序校验（2026-09-18）

- **问题**：工艺路线编辑页可在同一步骤（含并行组）内重复添加同一工序（如两个“老化测试”），属于无效配置，正常业务不应出现。
- **后端修复**：`MmsRouteServiceImpl.insertRouteProcesses` 前置 `validateDuplicateProcess` 校验——同一工艺路线内按 `processId` 或 `processCode` 判重，重复即抛出 ServiceException，新增/修改/复制链路统一生效。
- **前端修复**：`submitForm` 提交前调用 `checkDuplicateProcess`，命中重复时直接提示并阻断提交，避免用户保存后才收到后端报错。
- **校验口径**：不区分串行或并行；只要同一工序在同一工艺路线中出现两次即拦截。

## 十六、MMS 工艺路线产品类型限制（2026-09-18）

- **问题**：工艺路线新增/编辑页的产品选择器未过滤物料类型，原材料也可被选为工艺路线产品；原材料不需要工艺加工，属于无效配置。
- **前端修复**：`route/index.vue` 的产品选择器增加 `:material-types="['1','2']"`，与 BOM、工单页保持一致，仅可选半成品+成品。
- **后端兜底**：`MmsRouteServiceImpl` 新增 `validateProductType`，新增/修改时查询 `wms_material.material_type`，非半成品(1)/成品(2)即抛出 ServiceException；`MmsRouteMapper` 新增 `selectMaterialTypeById`。
- **验证**：`mvn -pl ruoyi-mms -am -DskipTests compile` BUILD SUCCESS。

## 十八、质检执行数量逻辑校验（2026-09-18）

- **问题**：质检执行弹框中检验数量、不良数、报废数量无逻辑校验，可出现不良数大于检验数量、负数、有不良却未选缺陷类型等无效数据。
- **校验规则**（前端提交前+后端保存前双重拦截，口径一致）：
  1. 检验数量必须大于0；
  2. 不良数、报废数量不能为负数；
  3. 不良数、报废数量各自不能大于检验数量；
  4. 不良数+报废数量之和不能大于检验数量；
  5. 不良数或报废数量大于0时，缺陷类型必填。
- **位置**：前端 `qc/index.vue submitInspect` 新增 `checkQcQuantities`；后端 `MmsQcServiceImpl.inspectQc` 新增 `validateQcQuantities`（数量字段为 Integer）。
- **验证**：`mvn -pl ruoyi-mms -am -DskipTests compile` BUILD SUCCESS。

## 十九、外协管理日期逻辑校验（2026-09-18）

- **问题**：发料外协弹框中计划回厂日期可以早于或等于外协日期；回厂验收时实际回厂日期同样缺少校验，不符合业务时序。
- **前端**：`outsource/index.vue` 新增 `checkOutsourceDate` 共用校验——`submitSendOut` 校验计划回厂日期、`submitBackAccept` 校验实际回厂日期，均必须晚于外协日期。
- **后端**：`MmsOutsourceServiceImpl` 新增 `validateBackDateAfterOutDate`，sendOut 与 backAccept 链路兜底校验（按天截断比较，严格大于）。
- **验证**：`mvn -pl ruoyi-mms -am -DskipTests compile` BUILD SUCCESS。

## 二十一、客诉处理弹框按处理方式联动（2026-09-18）

- **问题**：客诉处理弹框中退货数量、退货金额、索赔金额无条件全部必填，质量改善类客诉也被迫填退货信息；且退货/索赔分组排在处理方式之前，交互顺序颠倒。
- **联动规则**（处理方式字典 qms_complaint_handle_result）：
  - 1=退货退款：退货数量、退货金额必填，索赔金额选填
  - 2=换货处理：退货数量必填，退货金额、索赔金额选填
  - 3=折扣补偿：均选填（如涉及部分退货可填）
  - 4=质量改善 / 5=其他处理：不涉及退货索赔，分组隐藏
- **前端**：`complaint/index.vue` 处理信息卡片移至退货/索赔之前；退货/索赔分组按处理方式显隐；`requiredByHandleResult` 驱动动态必填，切换方式时同步规则、清空不再必填字段的值并清除校验残留；业务说明同步更新。
- **后端**：`QmsComplaintServiceImpl.completeComplaint` 新增 `validateComplete` 兜底校验（处理描述/处理结果必填 + 按方式校验退货字段），与前端口径一致。
- **验证**：`mvn -pl ruoyi-qms -am -DskipTests compile` BUILD SUCCESS。

## 二十三、内部审核方案B：检查表模板与审核报告（2026-09-18）

- **背景**：内审模块仅有计划与不符合项台账，缺检查表和报告输出，实际内审仍在 Excel 中进行。
- **新增表**：`qms_audit_check_template`（模板）、`qms_audit_check_template_item`（模板检查项）、`qms_audit_check_item`（计划实际检查项）；SQL `sql/add_audit_checklist_and_report.sql` 含预置 ISO9001 全要素模板（20 项检查内容）与按钮权限（qms:audit:check / qms:audit:report）。
- **后端**：`QmsAuditCheckController`（/qms/auditCheck）提供模板列表、按模板覆盖式生成检查表、检查项查询/批量保存、Word 报告下载（POI XWPF：基本信息+检查表汇总+符合率统计+不符合项+结论）。
- **前端**：`audit/index.vue` 操作列增加「检查表」「报告」按钮；检查表弹窗支持选模板生成、逐项录入检查结果（符合/不符合/不适用）与客观证据并批量保存。
- **验证**：`mvn -pl ruoyi-qms -am -DskipTests compile` BUILD SUCCESS；前端 Vite 编译通过。

## 二十二、质量目标达成判定完善（2026-09-18）
- **原逻辑缺陷**：4 项指标（合格率、PPM、客诉率、CAPA关闭率）的目标与实际全部为空时，默认判定为"达成(Y)"，空目标等于无条件达标。
- **通用标准**（一票否决 + 有效性门槛）：
  1. 仅当至少存在一组"目标值与实际值均已填写"的指标时才判定；
  2. 任一已对比指标未达标即未达成(N)（合格率/CAPA关闭率低于目标，PPM/客诉率高于目标）；
  3. 全部已对比指标达标为达成(Y)；
  4. 无任何有效对比为待定(P)，不默认达成。
- **后端**：`QmsQualityTargetServiceImpl.checkAchievement` 重构为三态判定（Y/N/P）。
- **数据**：`qms_quality_target.achievement` 注释更新为三态；存量空判定数据修正为 P（本次实查无受影响行）；新增 `sql/fix_quality_target_achievement.sql`。
- **前端**：`target/index.vue` 达成徽标/详情三态展示（待定为灰色）、筛选下拉与统计 Tabs 增加"待定"，`achievementLabel/achievementClass` 统一映射。
- **验证**：`mvn -pl ruoyi-qms -am -DskipTests compile` BUILD SUCCESS。

## 二十、生产执行工单选择与工单状态筛选（2026-09-18）
- **工单状态口径**：0=草稿、1=已下达、2=执行中、3=已完工、4=已关闭、5=已暂停、6=已作废。
- **业务分析**：领料、外协均发生在生产进行中，仅已下达(1)/执行中(2)的工单可关联；草稿未下达、完工/关闭后生产结束、暂停暂不作业、作废无效，均不应选择。退料页选择的是领料单而非工单，工单有效性已由领料单保证，无需处理。
- **选择器修复**：外协、领料页工单选择器由排除单状态 `excludeStatus:'0'` 改为白名单 `statusList:['1','2']`；`MmsWorkOrderMapper` 新增 `params.statusList` 支持。
- **列表筛选**：外协、领料、退料三页顶部筛选增加「工单状态」下拉（按关联工单状态过滤），后端分别通过 join 工单表 + `params.workOrderStatus` 实现；外协列表 SQL 改为显式别名并 join 工单表，退料列表补 join 工单表。
- **验证**：`mvn -pl ruoyi-mms -am -DskipTests compile` BUILD SUCCESS。

## 十七、领料确认弹框增加备注字段（2026-09-18）
- **问题**：领料管理「确认领料」弹框只有领料人、领料时间和明细数量，缺少备注输入。
- **前端**：确认信息卡片新增「备注」textarea（选填，500字上限，带字数提示），随确认请求提交。
- **后端**：`MmsIssueServiceImpl.confirmIssue` 处理备注——前端传入时更新，未传保留原备注（updateIssue 的 if 判空动态更新语义一致）。
- **验证**：`mvn -pl ruoyi-mms -am -DskipTests compile` BUILD SUCCESS。

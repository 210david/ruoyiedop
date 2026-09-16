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


# 开发规范 —— DMS AI 配置页面交互规范

> 适用范围：设备管理模块（ruoyi-dms）AI 配置页 `ruoyi-ui/src/views/dms/ai/config.vue`，以及后续同类"外部服务配置"页面。
> 生效日期：2026-09-15。后续同类开发必须遵守本规范。

## 1. 大模型服务商预设（强制）

- AI 配置页必须提供"大模型服务商"下拉预设，内置主流 OpenAI 兼容服务商（DeepSeek、通义千问、智谱 GLM、Kimi、豆包、文心一言等），包含标准 `apiUrl` 与默认 `modelName`。
- 选择预设后自动填入 API 地址与模型名，字段仍保持可编辑；保存的 API 地址能匹配预设时，编辑页回显所选服务商。
- 新增服务商时在 `providerPresets` 数组中追加，格式：`{ name, url, model }`，禁止硬编码散落在表单各处。

## 2. 连接测试（强制）

- 所有需要配置外部 API 的页面，必须提供"测试连接"按钮，允许在**保存前**用当前表单值发起测试。
- 后端提供 `POST /dms/ai/config/test`（权限 `dms:ai:config`），入参 `apiUrl/apiKey/modelName`；apiKey 为空或含掩码时回退到已保存密钥。
- 测试结果需展示：成功（模型回复 + 耗时 ms）或失败（HTTP 状态 / 超时 / 异常摘要，截断至 300 字符）。
- 测试请求使用最小 token（max_tokens=20），避免无谓消耗配额。

## 3. API Key 保密原则（强制）

- API Key 一经保存，系统内**任何接口不得回显**（明文与掩码均不允许），仅返回 `apiKeyConfigured` 布尔标记。
- 前端 Key 输入框使用 `type="password"` 且**不提供查看功能**（不得加 `show-password`），placeholder 提示"已配置，留空则继续使用原密钥"。
- 更新配置时，Key 为空或为掩码时不得覆盖已保存密钥（后端 `updateConfig` 已做保护，前端不得绕过）。

## 4. 连接测试错误提示规范（强制）

- 测试失败信息必须给出**可操作的修复建议**，禁止直接抛原始 HTTP 状态 + 响应体（尤其禁止把网关 HTML 原文展示给用户）。
- 后端 `buildTestError` 按状态码分类：
  - 404：提示地址路径不完整/有误；URL 以 `/v1` 或 `/` 结尾时，直接拼接建议地址 `xxx/v1/chat/completions`
  - 401/403：提示 Key 无效、过期或无权限
  - 429：提示余额不足或频率超限
  - 400：优先提示模型名称不正确，并透出服务商返回的 `error.message`（截断120字）
  - 响应为 HTML：附加"返回的是网页而非API响应"说明，标签过滤后仅保留少量文本
- 网络异常分类：`UnknownHostException` 提示域名无法解析，`ConnectException` 提示无法建立连接，超时给出时长与排查方向。
- 推理类模型（如 MiMo）`content` 为空但 `choices` 存在时视为**连接成功**，回复给"连接正常（推理模型…）"说明，不得判为失败。

## 4. 测试结果弹窗样式规范（强制）

- 连接测试结果必须使用**结构化卡片弹窗**（ElMessageBox + VNode 渲染，参考 `showTestResult`），禁止使用原生 alert 展示原始文本。
- 弹窗结构：状态图标（成功绿勾/失败红叉，圆形渐变+投影）→ 状态标题与副文案（含格式化耗时：1秒内显示 ms，否则显示秒）→ 明细卡片（API 地址/模型名称/模型回复，键值左对齐，长地址 `word-break: break-all`）。
- 失败时在明细卡片上方增加"原因与建议"红色卡片（浅红底、左侧红字标题），内容用后端返回的 `error`（已含修复建议），禁止直接展示 HTML 响应原文。
- 弹窗样式类挂在 body 下，必须使用**非 scoped** 样式块（`<style>`），类名统一 `ai-test-` 前缀；弹窗宽度约 480px、圆角 14px，与系统 Element Plus 风格一致。

## 5. 相关代码位置

| 内容 | 文件 |
| --- | --- |
| 服务商预设 / 测试按钮 / Key 占位提示 | `ruoyi-ui/src/views/dms/ai/config.vue` |
| 测试接口前端封装 | `ruoyi-ui/src/api/dms/aiConfig.js` |
| 测试端点 / Key 不回显 | `ruoyi-dms/.../controller/DmsAiConfigController.java` |
| testConnection 实现 | `ruoyi-dms/.../service/impl/DmsAiServiceImpl.java` |

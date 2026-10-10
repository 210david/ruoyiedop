# EDOP DESIGN.md — 企业数字化运营平台设计系统

> 反推自 EDOP 生产代码（`variables.module.scss` / `fin-list-page.scss` / `mk-list-page.scss`）与真机截图。
> 参照体系：Linear（靛蓝主色 + 克制阴影 + slate 中性阶）、Stripe（数据表格密度与金额排版）。
> 适用范围：全部业务模块列表页 / 表单弹窗 / 报表页。Element Plus 组件的定制基线。

---

## 1. Visual Theme & Atmosphere（视觉主题与氛围）

**设计哲学**：企业级数据密集型工作台 —— 信息优先、装饰退后。界面是"会计的账桌"：
每个像素服务于可读性与可扫视性，情感化元素仅出现在品牌色带与状态语义上。

- **视觉基调**：冷灰底 + 靛蓝强调；克制的双层微阴影；零渐变、零毛玻璃
- **核心关键词**：`数据密度` `账目级精确` `低饱和靛蓝` `柔和硬边` `留白呼吸`
- **光影质感**：扁平卡片 + 1px 冷灰描边 + 双层 4% 透明度阴影（无扩散光晕）
- **明暗模式**：当前仅亮色模式（暗色变量已预留于 `variables.module.scss`，V1 不启用）
- **数字呈现铁律**：所有金额使用 `font-variant-numeric: tabular-nums`，右对齐

---

## 2. Color Palette & Roles（调色板与角色）

### 2.1 品牌主色 Brand（靛蓝 Indigo）

| Token | HEX | CSS 变量 | 用途 |
|---|---|---|---|
| brand-50 | `#EEF2FF` | `--brand-50` | 主色底纹、选中行背景 |
| brand-100 | `#E0E7FF` | `--brand-100` | hover 底、浅标签底 |
| brand-200 | `#C7D2FE` | `--brand-200` | 边框强调 |
| brand-500 | `#6366F1` | `--brand-500` | 图标、装饰圆点、次级强调 |
| **brand-600** | **`#4F46E5`** | `--brand-600` | **主操作色**：标题色带 glyph、主链接、筛选卡强调条 |
| brand-700 | `#4338CA` | `--brand-700` | hover 深化态 |

### 2.2 中性色 Ink（Slate 冷灰，界面主骨架）

| Token | HEX | 用途 |
|---|---|---|
| ink-50 | `#F8FAFC` | 页面底色、统计条背景 |
| ink-100 | `#F1F5F9` | 分隔线、代码底、hover 行 |
| ink-200 | `#E2E8F0` | **标准边框色**（卡片/输入框/表格线） |
| ink-300 | `#CBD5E1` | 禁用边框、占位图形 |
| ink-400 | `#94A3B8` | 辅助文字（form-tip、param-key） |
| ink-500 | `#64748B` | 次要文字（标签、摘要、divider-text） |
| ink-700 | `#334155` | **正文标准色**、区块标题 |
| ink-900 | `#0F172A` | **标题/主数值**（mini-value 20px/600） |

### 2.3 语义色 Semantic（统一 500/50/700 三阶）

| 语义 | 主色 | 底色 | 深色（文字） | 场景 |
|---|---|---|---|---|
| 成功/收入 | `#10B981` green-500 | `#ECFDF5` | `#047857` green-700 | 已收金额、贷方、启用态 |
| 危险/支出 | `#EF4444` red-500 | `#FEF2F2` | `#B91C1C` red-700 | 未收/未付金额、作废、删除 |
| 警告/逾期 | `#F59E0B` amber-500 | `#FFFBEB` | `#B45309` amber-700 | 逾期预警、FIFO 标签 |
| 信息/链接 | `#3B82F6` blue-500 | `#EFF6FF` | `#1D4ED8` blue-700 | 详情链接、流程中态 |
| 特殊/权益 | `#8B5CF6` violet-500 | `#F5F3FF` | — | 坏账、所有者权益分区 |

### 2.4 Element Plus 基线映射（历史兼容层）

```css
--el-color-primary: #409EFF;   /* el 组件默认蓝 —— 与 brand 靛蓝并存 */
--el-color-success: #67C23A;
--el-color-warning: #E6A23C;
--el-color-danger:  #F56C6C;
```
> ⚠️ **双主色现状**：el 原生蓝（#409EFF）与业务靛蓝（#4F46E5）并存。
> 新页面业务层一律用 `--brand-*` / `--ink-*`；el 蓝仅存在于未覆盖的原生组件内。
> 禁止在同一视图内同时引入两种主色的按钮。

### 2.5 明暗骨架（导航壳层）

```css
--sidebar-bg: #1A1F2E;   /* 暗色侧栏 */
--sidebar-text: rgba(255,255,255,.65);
--menu-active: #409EFF;
--navbar-bg: #FFFFFF;     /* 亮色顶栏 */
```

---

## 3. Typography Rules（排版规则）

**Font Family**（全局继承系统栈，两处等宽特例）：

```css
/* 正文 */
font-family: -apple-system, BlinkMacSystemFont, "Helvetica Neue",
             "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
/* 编码/参数/科目编码 */
font-family: ui-monospace, Menlo, Consolas, monospace;
```

### Type Scale（实际使用的 7 级，禁止越级）

| 级别 | Size | Weight | Line Height | 颜色 | 用途 |
|---|---|---|---|---|---|
| **Data Hero** | 20px | 600 | 1.2 | ink-900 | 汇总卡主数值（tabular-nums） |
| **Title** | 14px | 600 | 1.5 | ink-700 | 卡片标题、区块标题、表头 |
| **Body** | 14px | 400 | 1.5 | ink-900 | 表格正文、输入框内容 |
| **Body-Strong** | 14px | 500 | 1.5 | ink-700 | 表单 label、按钮文字 |
| **Caption** | 13px | 400/500 | 1.5 | ink-500 | 次要说明、统计条、divider-text |
| **Hint** | 12px | 400 | 1.6 | ink-500 | hint-text 提示条、表格辅助列 |
| **Nano** | 11px | 400 | 1.4 | ink-400 | form-tip、param-key（monospace） |

**设计哲学**：整套系统锚定 14px 正文 —— 企业屏（1080p+）一屏 30+ 行表格是第一诉求。
字号跨度压缩在 11-20px 之间，靠 **Weight（400/500/600）+ 颜色（ink-400→900）**
做层级，而不是放大字号。金额一律 tabular-nums 保证竖向对齐。

---

## 4. Component Stylings（组件样式）

### 4.1 Buttons（el-button 定制基线）

| 变体 | 背景 | 文字 | 边框 | 圆角 | Padding |
|---|---|---|---|---|---|
| Primary | `--brand-600` | `#FFF` | 无 | 6px | 8px 16px |
| Default | `#FFF` | ink-700 | 1px ink-200 | 6px | 8px 16px |
| Warning | `--amber-500` | `#FFF` | 无 | 6px | 8px 16px |
| Danger-Link | 透明 | `--red-500` | 无 | 4px | 4px 8px |
| Text-Link | 透明 | `--blue-500` | 无 | 4px | 4px 8px |

- hover：主色深化一阶（brand-600→brand-700），150ms `--ease-out`
- 图标按钮统一 `link` 型（表格行内），带 icon 前缀
- **权限门铁律**：写操作按钮必须挂 `v-hasPermi`，无权限即隐藏（非禁用）

### 4.2 Cards（卡片三形态）

```css
/* 标准内容卡 */
.surface {
  background: #FFF;
  border: 1px solid var(--ink-200);        /* 1px #E2E8F0 */
  border-radius: var(--r-md);              /* 10px */
  box-shadow: var(--shadow-card);          /* 0 1px 0 rgba(15,23,42,.04),
                                              0 1px 2px rgba(15,23,42,.04) */
  padding: 16px;
}
/* 汇总卡（summary-item）：白底同上，内部 label 12px ink-500 + 数值 20px/600 */
/* 分区卡（config-section）：同上，头部带 4px×14px brand-600 色带 glyph */
```

### 4.3 Inputs（筛选卡内联式为主）

```css
.control {
  height: 36px; padding: 0 12px; background: #FFF;
  border: 1px solid var(--ink-200); border-radius: var(--r-sm);  /* 6px */
  transition: border-color .15s var(--ease-out), box-shadow .15s var(--ease-out);
}
/* focus: border-color → brand-500 + box-shadow 0 0 0 2px rgba(99,102,241,.15) */
/* placeholder: ink-400; el-form-item__error 12px red-500 */
```

### 4.4 Navigation（面包屑 + Tab）

- 面包屑：`首页 / 财务管理 / 应付管理`，末级 ink-900 加粗，父级 ink-500
- 页内 Tab（el-tabs）：激活态 brand-600 下划线 2px + 文字 ink-900/600；未激活 ink-500
- 侧栏：暗色 #1A1F2E、宽 200px、激活项 #409EFF（壳层保留 el 蓝）

### 4.5 Badges / Tags（状态语义三件套）

```css
/* dir-badge（借贷方向）: 26×22px，圆角3px，12px/600 */
.is-debit  { background: var(--blue-50);  color: var(--blue-700); }   /* 借 */
.is-credit { background: var(--red-50);   color: var(--red-700); }    /* 贷 */
/* 状态 tag（el-tag effect=plain）: success=启用/已匹配  info=停用/未匹配
   warning=待处理  danger=逾期/失败；金额前缀图标色随语义 */
```

### 4.6 Modals / Dialogs

- 遮罩：`rgba(15,23,42,.5)`，无 blur
- 内容区：白底、圆角 `--r-lg`(14px)、宽度按内容 500-1000px 五档
- footer：**`dialog-footer` 必须右对齐**（全局样式待补，见 §7 Don'ts）
- 动画：入场 200ms `--ease-out`（opacity + translateY 8px），退场 150ms
- 弹窗内表单 tip 用 `.form-tip`（11px ink-400），危险操作警示用 `el-alert type=error`

---

## 5. Layout Principles（布局原则）

### Spacing System（4px 基数）

| Token | 值 | 用途 |
|---|---|---|
| space-1 | 4px | 图标与文字间隙 |
| space-2 | 8px | 组件内元素间距、dot 与标题 |
| space-3 | 12px | 卡片内边距、表格行 padding |
| **space-4** | **16px** | **标准卡片 padding、栅格列距** |
| space-5 | 20px | 区块间距 |
| space-6 | 24px | 页面级分区间距 |

### Grid & Container

- 筛选卡栅格：`grid-template-columns: repeat(4, minmax(0,1fr)); gap: 12px 16px`
- 列表页骨架：`筛选卡 → 工具栏(toolbar) → 表格(table-wrap) → 分页`，纵向间距 12px
- 容器：`app-container` 全宽 + 20px 页边距（企业后台不做 max-width 收窄）
- 响应式折叠：`@media (max-width: 720px)` 筛选栅格降为单列、toolbar 换行

### 留白哲学

数据表格区**不留白**（行高 40px 紧凑、密度优先）；表格外的说明性内容
（提示条、表单 tip、分区卡头部）**留足呼吸**（行高 1.6 + 上下 12px）。
"密处愈密、疏处愈疏" —— 密疏对比本身就是层级。

---

## 6. Depth & Elevation（深度与层级）

### Shadow System（仅两级，克制）

```css
--shadow-card:  0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);  /* 常态卡 */
--shadow-pop:   0 4px 16px rgba(15,23,42,.10);                             /* 弹窗/下拉（待补全局） */
/* 禁止：大于 .10 透明度的投影、彩色投影、多层光晕 */
```

### Surface Layers

```
页面底 ink-50(#F8FAFC) → 卡片 #FFF(+shadow-card) → 悬浮下拉 #FFF(+shadow-pop) → 弹窗遮罩 rgba(15,23,42,.5)
```

### Z-index Scale

| 层 | 值 | 内容 |
|---|---|---|
| base | 0 | 文档流 |
| sticky | 100 | 表头、面包屑 |
| dropdown | 1000 | el-select 下拉 |
| modal-mask | 2000 | 遮罩（el 默认） |
| message | 3000 | el-message 全局提示 |

### Backdrop Effects

**禁用毛玻璃**（`backdrop-filter` 全项目未使用，保持禁用）。
遮罩仅靠 50% 黑透明度区分层级。

---

## 7. Do's and Don'ts（设计规范与禁忌）

### Do's ✅

1. **金额一律 tabular-nums + 右对齐**，收/支用语义色（green-700 / red-700）
2. **每个列表页挂 `fin-list-page`（或对应模块）根类**，共享样式文件统一维护
3. **状态用 el-tag plain 三件套**（success/info/warning/danger），文案 2-4 字
4. **分区卡独立操作区**：每张配置卡自带保存/重置，重置仅还原本卡字段
5. **危险操作双层确认**：confirm 弹窗文案写清后果（如"删除后计价将报错"）
6. **表单 tip 翻译技术风险成业务语言**（如"批量设 13% 会导致免税物料成本失真"）
7. **代码类字段**（科目编码、物料编码、configKey）一律 monospace + ink-100 底
8. **空状态用 el-empty**，占位文本说明"为什么空 + 该做什么"

### Don'ts ❌

1. **禁止裸 `<` `<=` 出现在 Mapper XML**（运行时才爆，编译不查）—— 与视觉无关但同样致命
2. **禁止整表单重置** —— 重置只能还原本分区/本卡片字段
3. **禁止同一视图混用 el 蓝（#409EFF）与 brand 靛（#4F46E5）做主按钮**
4. **禁止无权限门的写操作按钮**（用 v-hasPermi 隐藏，而非 disabled）
5. **禁止 shadow 超过两级或透明度 > .10** —— 阴影只做"轻浮起"，不做"强立体"
6. **禁止 animation 超过 300ms** —— 企业工具的动效是反馈不是表演
7. **禁止行内 style 写死颜色** —— 一律走 CSS 变量（暗色模式预留的唯一通路）
8. **禁止未定义的"幽灵样式类"** —— 新 class 必须先在共享 scss 定义（dialog-footer 全项目缺定义的教训）

---

## 8. Responsive Behavior（响应式行为）

### Breakpoints

| 断点 | 宽度 | 目标设备 |
|---|---|---|
| mobile | < 720px | 平板竖持（后台系统不做手机端） |
| desktop | ≥ 720px | 1080p+ 办公屏（主战场） |
| wide | ≥ 1920px | 双屏/财务专用宽屏 |

### 折叠策略（已实现）

- **筛选栅格**：4 列 → 1 列（720px），gap 12px 16px 保持
- **toolbar**：`flex-wrap: wrap`，左右两组自然换行
- **表格**：固定列（操作列 fixed=right）在窄屏保持可见；列宽 min-width 触发横向滚动
- **分区卡头部**：`cs-head` 窄屏允许换行，操作按钮跟随标题流

### Font Scaling

**不随设备缩放**（企业固定屏场景）。密度调节通过浏览器缩放（110%-125% 是财务用户常态），
所有布局在 125% 缩放下经真机验证不破版。

### Touch Targets

后台鼠标场景不强制 44px；但行内 link 按钮 padding 4px 8px 保证 ≥ 28px 可点击高度。

---

## 9. Agent Prompt Guide（AI 代理提示指南）

### Quick Reference

```
技术栈：Vue3 setup + Element Plus + Vite + SCSS
样式根类：.fin-list-page（或 .mk-list-page 等，每模块一个共享 scss）
色板：--brand-*(靛蓝主) --ink-*(slate中性) --green/--red/--amber/--blue/--violet(语义)
圆角：--r-sm 6px / --r-md 10px / --r-lg 14px
阴影：--shadow-card 双层4%；禁强阴影
字号：11/12/13/14/20px，正文14，数值20/600/tabular-nums
```

### Component Prompts（可直接复制）

1. `生成一个 EDOP 列表页筛选卡：根类 fin-list-page，filter-card 内 4 列 grid 筛选栏
   （label 14px/500 ink-700 + 36px 输入控件），标题带 4×14px brand-600 色带`
2. `生成汇总卡组 summary-bar：flex wrap，4 个 summary-item（白底 ink-200 边框 r-md），
   label 12px ink-500 + 数值 20px/600 tabular-nums ink-900，逾期数值用 red-700`
3. `生成状态表格行：借贷方向用 dir-badge（借=blue-50/蓝700，贷=red-50/红700），
   金额列右对齐 tabular-nums，操作列 fixed right 链接按钮`
4. `生成分区配置卡 config-section：头部彩色 dot + 14px/600 标题 + 状态 tag，
   每卡独立"保存本组/重置本组"，重置仅还原本组（禁止整表单重置）`
5. `生成危险操作确认弹窗：el-alert type=error 写明业务后果，dialog-footer 右对齐，
   主按钮 danger`
6. `生成对账结果面板 recon-result：rr-row 上下分列（13px），分隔线 ink-100，
   差额行 is-diff 加粗，balanced=green / unbalanced=amber`

### Iteration Guide

1. 新页面先挂根类（fin-list-page），再写页面级覆盖 —— 不要从零写样式
2. 引入新 class 前先 `grep 共享 scss`，已存在的直接复用
3. 新增 CSS 变量必须同时在亮色（及预留暗色）两个作用域声明
4. 表格列 >10 个时优先"重要列前置 + fixed 操作列"，禁止横向挤压缩字号
5. 弹窗宽度按内容选五档：460 / 560 / 760 / 900 / 1000px
6. 所有金额字段组件统一走一个 `amount-cell` 类，改对齐只改一处
7. 语义色只用 §2.3 五组，新增语义色需先在此文档登记
8. 改完必须构建 + 真机看一眼（截图对比），样式类"看似生效"不一定入包
9. 权限指令、确认弹窗、空状态是三件套，写 CRUD 时一次配齐
10. 图标统一 element-plus 内置 icon 集，禁止混用外链图标库

---

*本文档由生产代码反推生成于 2026-10-09，Token 与代码一一对应。
修改样式时先改代码再回填本文档，保持单一事实源。*

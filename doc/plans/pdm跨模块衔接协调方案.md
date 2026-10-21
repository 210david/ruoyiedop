# 产品设计模块（ruoyi-pdm）跨模块衔接协调方案

> 版本：V1.1 ｜ 日期：2026-10-19 ｜ 状态：D1~D6 已确认（2026-10-19 用户确认按建议执行），进入实施
> 关联文档：《产品设计功能模块开发设计方案》V1.1（第五章"不侵入性保障方案"、第八章"与现有模块的功能衔接"）
> 目的：设计方案 5.3 节的三处跨模块衔接（BOM 下发、物料申请、变更通知）在编写时未基于现有代码实读，本章经对 mms / wms / mk / 消息中心代码的实读调查后，形成可执行的协调前置项、决策项与变更单清单，供评审确认后冻结接口口径。

---

## 一、代码事实基线（调查结论）

以下事实全部来自当前工程实读，是本方案所有决策的前提。

### 1.1 mms（生产管控）已有完整 BOM 体系，工单用料只认 mms_bom

| 事实 | 代码位置 |
|---|---|
| BOM 主表实体 `MmsBom`：bomId/bomNo/productId(物料ID)/productCode/bomType/version/status/effectiveDate/expireDate | `ruoyi-mms/.../domain/MmsBom.java` |
| bomType 取值：**0标准BOM 1替代BOM 2配方 3临时BOM**（无 EBOM/MBOM 之分，mms_bom 本身即制造侧清单） | `MmsBom.java` bomType 字段注释 |
| status 取值：**0草稿 1已发布 2停用** | `MmsBom.java` status 字段注释 |
| BOM 明细 `mms_bom_detail`：materialId/materialCode/materialName/specModel/unit/usageQty/lossRate/supplyType/pickStoreId/isPhantom/bomRefId | `ruoyi-mms/.../domain/MmsBomDetail.java` |
| **工单齐套检查取 BOM 逻辑：优先 `workOrder.getBomId()` 精确取，否则按 productId 自动查找"已发布"状态的 BOM 展开** | `MmsKitCheckServiceImpl.java` L293-L335 |
| 工单 BOM 快照表 `mms_wo_bom_snapshot`，领料/退料页面物料来源 | `MmsWorkOrderController.java` L250-L258（`GET /bomSnapshot/{workOrderId}`） |
| mms 跨模块引用 wms 的既有惯例：SQL JOIN `wms_material`（只读） | `MmsBomMapper.xml` L248-L260 |

**推论（决定性）**：设计方案 5.3 节设想"生产管控侧通过只读查询/开放接口从 pdm 获取 MBOM"不成立——mms 的齐套、领料、快照全链路只读 `mms_bom/mms_bom_detail`，若 pdm 不落 `mms_bom`，数据贯通是空话。**MBOM 必须落库到 `mms_bom`（bomType=0 标准BOM，status=1 已发布）**，问题只剩"由谁写入"（见衔接点一）。

**附带推论**：`mms_bom.productId` 是**物料 ID**（成品/半成品物料），即 pdm 产品档案必须挂接 `wms_material` 中的成品/半成品物料记录，否则 BOM 下发无产品可挂。

### 1.2 wms 物料主数据：编码自动生成，无"物料申请"机制

| 事实 | 代码位置 |
|---|---|
| `wms_material` 实体：materialCode/materialName/materialType/specModel/unit/status/delFlag | `ruoyi-wms/.../domain/WmsMaterial.java` L17-L72 |
| 新增物料时编码为空则自动生成：`IMkNumberRuleService.generateNumber("wms_material", params)`，**按 materialType 动态前缀** | `WmsMaterialServiceImpl.java` L48-L79 |
| **wms/pms 均无"物料申请/待确认物料"类机制**，需 pdm 侧新建 | 全模块检索确认 |
| 采购引用物料方式：跨模块调用 `WmsMaterialMapper.selectMaterialByCode`（只读） | `PmsSupplierCatalogServiceImpl.java` L123-L133 |
| 前端 `MaterialPicker` 支持 `materialTypes` 过滤、回传 materialId/materialCode/materialName/specModel/unit | `ruoyi-ui/src/components/MaterialPicker/index.vue` L83-L92、L180-L192 |

### 1.3 营销订单的产品 = 物料编码（mk 零独立产品表）

| 事实 | 代码位置 |
|---|---|
| 营销订单明细 `MkOrderItem` 直接持有 `materialCode`，产品即 `wms_material` 物料 | `ruoyi-mk/.../domain/MkOrderItem.java` L32 |

**推论**：设计方案 7.1 "以产品档案为编码主数据源"与现实冲突——营销、生产、仓库三端的产品编码全部来源于 `wms_material`。pdm 产品档案的定位应修正为：**产品编码 = 成品/半成品物料编码（wms 编号规则生成），pdm 档案是该物料的设计资料聚合入口（一物一档）**，档案创建时通过"选择物料/物料申请"挂接物料，不另设编码体系。

### 1.4 消息中心：工具类 + 字典驱动，接入零代码改动

| 事实 | 代码位置 |
|---|---|
| 标准发送入口 `MessageHelper.sendMessage(title, content, type, level, bizSource, bizId, redirectUrl, recipientRoleKey, bizStatus, bizEntryName)`；type：1系统通知/2业务预警/3审批消息/4待办事项；level：1普通/2重要/3紧急；发送失败不影响主业务；`markHandled(bizSource, bizId)` 支持业务状态流转后批量标记已处理 | `ruoyi-system/.../utils/MessageHelper.java` L41-L139 |
| bizSource 注释枚举为 wms/pms/mms/qms/safety/dms/mk/system，**尚无 pdm** | `MessageHelper.java` L17、L37 |
| **前端消息分类/筛选项完全由字典 `sys_message_biz_source` 驱动，无硬编码映射** | `ruoyi-ui/src/views/system/message/index.vue` L85、L385-L387；`DetailView.vue` L153-L177 |
| 消息表 `sys_message` 含 bizSource/bizId/redirectUrl/recipientRoleKey/bizStatus/bizEntryName，按 recipient_role_key 过滤权限 | `SysMessageMapper.xml` L43-L113、L178-L214 |

**推论**：pdm 接入消息中心 = ①`pdm_all.sql` 向 `sys_message_biz_source` 字典追加 `pdm` 项（WHERE NOT EXISTS 防重）②业务代码调用 `MessageHelper`（ruoyi-system 在 pdm 最小依赖集内）。**前后端零跨模块代码改动**。

---

## 二、衔接点一：设计BOM 下发转换为制造BOM（P0 决策项）

### 2.1 落库方式选项对比

| 选项 | 做法 | mms 改动 | pdm 侧代价 | 风险 |
|---|---|---|---|---|
| A. pdm 直写表 | pdm 事务内 INSERT `mms_bom`(status=1,bomType=0,version=pdm版本) + `mms_bom_detail`，同步记录 `pdm_mbom_release` | **零** | 直接写他模块表，SQL 级耦合 | mms 将来改表结构/校验逻辑时破坏 pdm；违反"不直写他模块业务表"精神 |
| B. mms 新增接收服务（推荐） | mms 新增 `receiveMbom(MmsBom)` 方法（纯新增，不动已有代码）；pdm 依赖 ruoyi-mms 调用；pdm 记录 `pdm_mbom_release` 映射 bomId | 新增 1 个 Service 方法 | pom 新增 1 条依赖 | pdm→mms 单向依赖，无环，符合依赖链规范；需修订方案 5.1 依赖声明 |
| C. mms 拉取模式 | mms 新增"从 pdm 拉取并落库"定时任务/按钮 | 新增拉取 Service + 任务 | pdm 只存 `pdm_mbom_release` | mms 改动最大；同步时效差；与"避免直接修改 mms 模块代码"的意图最远 |

### 2.2 推荐结论：选项 B

- 落库入口唯一（mms 自己控制校验与后续快照逻辑），职责边界最清晰；
- mms 改动为**纯新增方法**（不修改任何已有代码行），回归风险极低；
- 依赖方向 pdm→mms 保持单向无环，不破坏 common→system→mk→wms→qms→mms 链路；
- 若评审希望"首期零 mms 改动"，A 可作为过渡方案先行，B 的变更单并行评审，二期切换——两者数据落点相同，切换成本仅是替换 pdm 侧写入调用。

### 2.3 需发起的独立变更单（mms 侧，纯新增）

| 项 | 内容 |
|---|---|
| 变更范围 | `IMmsBomService` 新增方法 `Long receiveMbom(MmsBom bom)`；`MmsBomServiceImpl` 新增实现：校验 productId 物料有效 → 事务内 insert `mms_bom`（bomNo 按 mms 编号规则或入参携带、bomType=0、status=1、version=入参、source 标记 pdm）→ 批量 insert `mms_bom_detail` → 返回 bomId |
| 不改动 | mms 任何已有类、Mapper XML、前端页面 |
| 联调点 | pdm 下发事务：pdm 写 `pdm_mbom_release` 与调用 receiveMbom 必须同事务；失败整体回滚 |

### 2.4 下发数据映射口径（pdm EBOM → mms_bom）

| mms_bom 字段 | 来源 |
|---|---|
| productId / productCode / productName | pdm 产品档案挂接的 wms 物料（见衔接点二后回填的 materialId） |
| bomType | 固定 '0'（标准BOM） |
| version | pdm_design_bom 生效版本号（V1.0 格式） |
| status | 固定 '1'（已发布）——mms 齐套按 productId 自动找"已发布"BOM |
| baseQty / baseUnit | EBOM 产品用量/单位 |
| 明细 usageQty/lossRate | EBOM 明细同义字段直传 |
| 明细 materialId/materialCode | EBOM 明细回填后的物料（未确认编码的物料禁止下发，方案 7.4 规则已定） |

**升版重新下发口径**：同一产品再次下发时，mms 侧将旧 BOM 置为停用（status=2）或按 expireDate 失效，由变更单评审确定——**此项需 mms 侧在变更单中一并给出口径**（涉及工单已引用旧 bomId 的兼容处理：mms_wo_bom_snapshot 快照机制天然兼容在制工单）。

---

## 三、衔接点二：物料申请与编码确认（pdm 侧新建，零 wms/pms 改动）

### 3.1 现状结论

wms 物料编码由仓库岗在物料管理页新增时自动生成（按 materialType 前缀），不存在"申请-确认"流程。设计方案 5.3 设想"由仓库/采购岗在既有页面完成编码确认"——既有页面无此功能，若改造 wms 页面则违反不侵入红线。

### 3.2 推荐流程（pdm 侧闭环，wms 零改动）

```
设计员在 EBOM 中新增物料行（wms 中不存在）
  → 行上点击【物料申请】，填写 名称/规格/物料类型/建议单位，生成 pdm_material_apply 申请单（状态：待确认）
  → MessageHelper 推送仓库岗（type=4 待办事项，bizSource=pdm，redirectUrl=pdm 申请单页，recipientRoleKey=仓库确认角色）
  → 仓库岗收到消息，在 wms 物料管理页（既有功能，零改动）按申请信息新增正式物料（编码自动生成）
  → 仓库岗回到 pdm【物料申请确认】页（pdm 侧新页面，授权仓库/采购岗），内嵌 MaterialPicker 选择刚建物料 → 回填 materialId/materialCode → 申请单状态：已确认
  → pdm EBOM 对应行自动回填物料 → 申请单关闭后该 BOM 方满足下发条件
```

- **pdm 新增表**：`pdm_material_apply`（申请单：apply_no/apply_type(EBOM新增/档案新增)/material_name/material_type/spec_model/unit/apply_reason/status(0待确认 1已确认 2已关闭)/material_id/material_code/product_id/bom_id/…+BaseEntity 审计字段）——**需补入设计方案 4.2 表清单**。
- **pdm 新增页面**：物料申请列表 + 确认弹窗（菜单授权仓库/采购岗）。
- **采购侧**：新物料确认后由 pdm 消息通知采购岗（type=2 业务预警），采购在既有流程启用供应商与价格，pms 零改动。
- **BOM 下发校验**：下发前校验该 BOM 下不存在状态为"待确认"的物料申请，与方案 7.4 规则一致。

### 3.3 涉密说明

本衔接点不涉及方案 5.2 白名单外文件改动；pdm 侧全部新增。

---

## 四、衔接点三：变更通知 / 消息推送（零跨模块代码改动）

### 4.1 接入方式

| 事项 | 口径 |
|---|---|
| 字典 | `pdm_all.sql` 追加 `sys_message_biz_source` 字典项：值=`pdm`、标签=`产品设计`（WHERE NOT EXISTS 防重） |
| 调用 | 统一经 `MessageHelper.sendMessage(...)`，封装到 pdm 侧 `PdmMessageSupport`（私有组件） |
| 推送场景 | 变更生效→仓库/采购/质量/生产管控岗（type=2）；评审/审批待办→评审人（type=3）；物料申请待确认→仓库岗（type=4）；BOM 下发完成→生产管控岗（type=1） |
| redirectUrl | pdm 前端路由（如 `/pdm/change`、`/pdm/materialApply`），消息点击直达单据 |
| bizStatus/bizEntryName | 随单据状态推送（如"待执行"），单据关闭时调用 `markHandled("pdm", bizId)` 批量置已处理 |
| recipientRoleKey | 复用各模块既有角色权限字符（如仓库物料岗用 wms 物料管理对应角色），pdm 侧角色权限字符（pdm:change:audit 等）仅用于设计内部角色 |

### 4.2 前端消息中心

无任何改动（分类/图标/筛选全部字典驱动，已验证 `index.vue` L85、`DetailView.vue` L153）。

---

## 五、待决策事项清单（已全部确认 2026-10-19）

| 编号 | 决策项 | 选项与建议 | 影响 |
|---|---|---|---|
| D1 | **MBOM 落库方式** | ✅ 已确认 **B（mms 新增 receiveMbom，pdm 依赖 mms）**；A（pdm 直写表）为过渡备选 | 决定 mms 变更单是否发起、方案 5.1/5.3 修订 |
| D2 | **产品编码主数据源** | ✅ 已确认：产品编码 = 成品/半成品物料编码（wms 生成），pdm 产品档案挂接物料、不另设编码；方案 7.1 相应修订 | 产品档案表设计（material_id 必填）、营销/生产对齐 |
| D3 | **重复下发/升版的旧 BOM 处理** | ✅ 已确认：旧 BOM status 置 2 停用 + 新 BOM version 记录于 pdm_mbom_release；在制工单靠 mms_wo_bom_snapshot 快照兼容 | mms 变更单内容 |
| D4 | **仓库/采购确认岗角色** | ✅ 已确认：复用 wms 既有物料管理角色推送消息（recipientRoleKey=`wms:material:list`） | pdm 角色配置、消息 recipientRoleKey |
| D5 | **CAD/三维模型轻量化预览** | ✅ 已确认：首期不做（原件上传 + PDF/图片/Office 在线预览），作为二期增强 | 7.3 范围冻结 |
| D6 | **待办超时提醒定时任务** | ✅ 已确认：首期实现（Quartz 扫描超时评审任务并发 MessageHelper 提醒） | 7.7 是否含定时任务 |

---

## 六、设计方案修订清单（决策确认后同步更新 V1.1 → V1.2）

| 条款 | 修订内容 |
|---|---|
| 5.1 依赖声明 | "仅依赖 common/system/mk" → "依赖 common/system/mk，**按 D1 决策增加 ruoyi-mms**；对 wms 物料保持 SQL JOIN 只读" |
| 5.3 BOM 下发衔接 | 替换为本文 2.2/2.4 口径（mms 齐套只认 mms_bom 的事实修正） |
| 5.3 物料对照 | 替换为本文第三章流程（pdm 侧新建物料申请闭环） |
| 4.2 表清单 | 新增 `pdm_material_apply` |
| 7.1 产品档案 | 按 D2 修订编码主数据源表述；档案表增加 material_id/material_code 关联字段 |
| 6/7.8 编号规则 | 物料申请单编号规则 `pdm_material_apply`（如 WL+yyyyMMdd+4位序号，按天重置）补入规则清单 |
| 9 角色权限 | 按 D4 补充确认岗权限点 |
| 12 实施批次 | 物料申请页归入第一批（产品档案依赖挂接物料）；mms 变更单归入第三批前置 |

---

## 七、执行清单摘要（冻结后即可开工的顺序）

1. 评审确认 D1~D6 → 更新设计方案至 V1.2（本文件随附修订条款）。
2. 发起 mms 独立变更单（receiveMbom 方法 + 旧 BOM 停用口径），评审通过后实施并回归 mms 冒烟（登录/菜单/工单齐套）。
3. pdm 第一批开发将 `pdm_material_apply`、编号规则、消息字典项纳入 `pdm_all.sql` 四段式。
4. 第三批 BOM 下发联调按 2.4 映射口径执行，联调用例覆盖：下发→工单齐套取数→变更升版重新下发→旧版本停用→快照兼容。
5. 全部实施记录与回归报告归档 `doc/plans/`（沿用本目录 ai 模块文档惯例）。

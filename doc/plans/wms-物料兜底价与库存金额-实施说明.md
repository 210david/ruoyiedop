# 仓库管理：物料兜底价 + 库存金额（实施说明）

> 范围：第 **1-4 项**全部完成。
> 日期：2026-11-23 ｜ 开发库：`ry-vue`

---

## 一、这次改了什么（按你确认的三项）

### 1. 修 `wms_inbound_order.total_amount` 无写入点

**问题**：该字段有 DDL、有 Mapper 映射、有 getter/setter，但全仓**没有任何写入点**。
31 张入库单该字段全为 `0/NULL`，导致「入库按类型统计」的金额恒为 0。

**改法**（`WmsInboundOrderServiceImpl`）：

| 时机 | 口径 | 方法 |
|---|---|---|
| 新增/修改单据 | 计划金额 = `Σ(plan_qty × unit_price)` | `calcOrderTotals()` |
| 单据完成（status=3） | **实收金额** = `Σ(received_qty × unit_price)` | `calcReceivedAmount()` |

两者必须分开：单据创建时只能按计划数量算，而"已完成入库统计"看的是真实入库金额，
部分收货时差额显著。只修写入点、不区分口径，报表仍会失真。

### 2. 物料主数据新增「计价参考成本价」`wms_material.cost_price`

- 类型 `DECIMAL(18,2)`，**不含税**，非负校验，2 位小数规整（`normalizeCostPrice()`）
- 用途唯一：财务入库计价在**入库明细单价缺失时**的兜底价
- **不是**采购价（在 `pms_supplier_catalog.unit_price`）、**不是**销售价（在 `mk_price_policy`）

`priceInbound()` 改为三级取价，并新增 `fallback` 计数回传：

```
入库单价 unit_price > 0        → 正常计价（含税，需除税）
unit_price 缺失 + cost_price>0 → 兜底计价（不含税，不除税）
两者都没有                     → skip + 提示补录
```

> **关键修正**：兜底价本身不含税，早期实现误把它当含税价再除一次税，
> 会系统性低估成本（实测 12.50 的场景少计 352.33 元）。已改为直接取用。
> 实测验证：兜底价 30、数量 10 → `unit_cost=30.0000`、`total_cost=300.00`。

### 3. 库存页显示「结存金额」，三级降级 + 来源标注

**不在 `wms_inventory` 加金额列**。财务已确认旁路设计（`FinInventoryCost` 类注释：
「WMS 只管数量，财务只管金额」）。金额改为查询时回填。

| 优先级 | 来源 | 标注 | 口径 |
|---|---|---|---|
| 1 | `fin_inventory_cost.total_cost` | `cost` 成本 | 不含税、四维分账（物料+仓库+库位+批次） |
| 2 | 数量 × 物料入库加权均价 | `estimate` 估算 | 含税、按物料拉平 |
| 3 | 数量 × 物料兜底价 | `fallback` 兜底 | 不含税 |
| — | 三者皆无 | 不标注 | 显示 `-`，不臆造 0 成本 |

**每行必须显示来源标签**。实测同一物料：成本口径 `7079.65`，估算口径 `6878.23`，
两者不等是真实差异（含税、分账粒度不同），不标注会让财务无法解释对账差异。

---

## 二、第 4 项：盘点成本联动（本轮新增）

### 为什么这样修，而不是让 WMS 直接写台账

WMS 盘点审批只调库存数量，不碰 `fin_inventory_cost`，于是：

- 盘亏后台账 `qty` 不减、`total_cost` 虚高 → 期末存货计价偏高
- 盘盈后台账不增 → 存货被低估

**但 WMS 不能直接写 `fin_*` 表**：`ruoyi-fin` 依赖 `ruoyi-wms`，反向写会破坏依赖方向，
且财务结转流水/凭证口径必须由财务侧统一掌控。

因此沿用项目既有的**旁路同步**模式：WMS 只产出业务事实（盘点差异），
财务侧扫描后落自己的台账 + 记结转流水。

### 新增能力

`POST /fin/cost/adjustStockTake`（权限 `fin:cost:carry`，前端在「存货成本」页加「盘点调账」按钮）

扫描口径：`wms_stock_take.status='3'`（已完成）且 `diff_qty != 0`，
幂等键 `source_type='stocktake' + detail_id`。

取价优先级（三级，都取不到则计入 fail、不写 0 成本）：
```
该维度台账 unit_cost → 物料+仓库加权平均 → 入库均价除税
```

| 方向 | 台账动作 | 流水 |
|---|---|---|
| 盘盈 | 按单位成本增加数量与金额（无台账则建账） | `qty` 正、`target_type=STOCK_GAIN` |
| 盘亏 | 按当前单位成本冲减；**数量不足截断到 0** | `qty` 负、`target_type=STOCK_LOSS` |

### 为什么盘盈盘亏要用两个 target_type

`sumByPeriod` 按 `target_type` 分组汇总。若两者共用一个类型，
盘盈（正）与盘亏（负）会在汇总时**正负抵消**，财务看不出本月净影响方向。

### 移库为什么不需要改台账（结论，非遗漏）

`fin_inventory_cost` 按「物料+仓库+库位+批次」四维唯一，但：

- 期末存货计价 `sumValuation` **只按 material_id 分组**（`GROUP BY material_id, material_code`）
- 总成本 `sumTotalCost` 只做 `SUM(total_cost)`

移库改的是 `location_id`，物料与仓库未变，**两个汇总口径都不受影响**。
只要不按库位做成本分摊，移库不需要同步台账——这是有意的取舍，不是遗留缺陷。
若将来要按库位分摊成本，则移库必须同步，需另行设计。

### 顺带修掉一个既有 bug

`FinInventoryCostMapper.updateCost` 的 `<if>` 块写在 `update_time = sysdate(),` 之后，
当 `inbound_order_id`/`remark` 都为空时生成 `sysdate(), WHERE`，**SQL 语法错误**。
此前 `carryOutbound` 恰好不带 `remark` 所以没暴露；盘点调账传入 `remark` 才触发。
已改为「前置逗号」写法，两种分支都能生成合法 SQL。

---

## 三、架构约束（为什么这样设计）

### WMS 不能依赖财务模块

`ruoyi-fin/pom.xml` 依赖 `ruoyi-wms`。若 WMS 反向依赖财务，会形成 Maven 循环依赖。
因此金额取数采用：

- **只读表**：WMS 直接读 `fin_inventory_cost`，不引入 Java 依赖
- **启动探测**：`InventoryCostAvailability` 用 `information_schema` 探测表是否存在
- **缺表降级**：未部署财务模块的环境自动回退到估算口径，不报错

> 部署事实：主部署包 `deploy/config/sql/EDOP_ALL.sql`（40 张表）**不含任何 `fin_*` 表**，
> 财务表由独立的 `sql/fin/fin_01_schema.sql` 安装。硬依赖会让未上财务模块的库直接 SQL 报错。

### 金额不能顺着 OpenAPI 泄露

`selectInventoryList` 被 `OpenWmsController`（`/openapi/v1/wms/inventory/list`）
与 MCP 服务账号（`edop_mcp_readonly`）复用。因此：

- 金额回填放在 **Web Controller**（`WmsInventoryController`），不在 Service
- 独立权限点 `wms:inventory:amount`（与 `wms:inventory:list` 分离）
- 金额字段加 `@JsonInclude(NON_NULL)`，无值时连 key 都不出现

---

## 三、改动文件清单

**后端**
```
ruoyi-wms/domain/WmsMaterial.java                     +costPrice
ruoyi-wms/domain/WmsInventory.java                    +amount/amountSource/unitCost（只读回填）
ruoyi-wms/domain/vo/WmsInventoryCostVO.java           新增（台账只读视图，含四维键）
ruoyi-wms/domain/vo/WmsMaterialPriceVO.java           新增（取价依据）
ruoyi-wms/support/InventoryCostAvailability.java      新增（财务表探测）
ruoyi-wms/service/impl/WmsInventoryAmountService.java 新增（三级降级回填）
ruoyi-wms/service/impl/WmsMaterialServiceImpl.java    +normalizeCostPrice
ruoyi-wms/service/impl/WmsInboundOrderServiceImpl.java +calcOrderTotals/calcReceivedAmount
ruoyi-wms/controller/WmsInventoryController.java      金额按权限回填（列表/详情/导出）
ruoyi-wms/mapper/WmsInventoryMapper.java(+xml)        两个批量取数查询
ruoyi-wms/mapper/WmsMaterialMapper.xml                cost_price 映射
ruoyi-fin/domain/vo/InboundCostRefVO.java             +costPrice
ruoyi-fin/domain/vo/StockTakeDiffRefVO.java           新增（盘点差异只读视图）
ruoyi-fin/domain/FinCostCarryForward.java             +TYPE_STOCK_TAKE / STOCK_GAIN / STOCK_LOSS
ruoyi-fin/service/IFinInventoryCostService.java       +adjustStockTakeDiff
ruoyi-fin/service/impl/FinInventoryCostServiceImpl.java 兜底取价 + fallback 计数 + 盘点调账
ruoyi-fin/controller/FinInventoryCostController.java  +POST /fin/cost/adjustStockTake
ruoyi-fin/mapper/FinRefMapper.java(+xml)              盘点差异扫描 + 入库均价查询
ruoyi-fin/mapper/fin/FinInventoryCostMapper.xml       修复 updateCost 逗号 bug
```

**前端**
```
ruoyi-ui/views/wms/material/index.vue    表单输入项 + 列表列(默认隐藏) + 详情项
ruoyi-ui/views/wms/inventory/index.vue   金额列 + 来源标签 + 权限控制 + 导出
ruoyi-ui/views/fin/cost/index.vue        「盘点调账」按钮
ruoyi-ui/api/fin/cost.js                 +adjustStockTake
```

**SQL**
```
sql/upgrade_20261123_wms_material_cost_price.sql          cost_price 字段 + 权限点（幂等）
sql/upgrade_20261123_wms_inbound_total_amount_backfill.sql 历史金额回填（一次性运维脚本）
```

---

## 四、验证结果（真实环境实测）

| 验证项 | 结果 |
|---|---|
| 后端编译（wms+fin 及依赖链） | ✅ BUILD SUCCESS |
| 前端构建 | ✅ 2m21s 成功，产物含权限点与口径文案 |
| SFC 编译（两个改动页面） | ✅ template + script 均通过 |
| SQL 脚本执行 + 幂等 | ✅ 字段/权限各 1 条，重复执行不产生重复 |
| 权限隔离 | ✅ 无 `wms:inventory:amount` 的角色，响应中 **连 amount key 都不存在** |
| 缺表降级 | ✅ 临时改名财务表后接口仍 200，自动切 `estimate`；恢复后回到 `cost` |
| 兜底计价链路 | ✅ `priceInbound` 返回 `fallback:2`，`unit_cost=30.0000`（未除税） |
| 入库单金额写入 | ✅ 新建单据 `total_qty=14`、`total_amount=303.00`（10×25.50+4×12.00） |
| 历史回填 | ✅ 逐单核对头金额与明细重算**零不一致** |
| 盘点调账·盘盈 | ✅ 盘盈 1 件 → 新建台账 `qty=1.00`、`total_cost=9.65`；流水 `STOCK_GAIN +1` |
| 盘点调账·盘亏 | ✅ 盘亏 5 件 → `qty 100→95`、`total_cost 7079.65→6725.67`（差 353.98 = 5×70.7965） |
| 盘亏负结存保护 | ✅ 台账剩 5 件、盘亏 20 件 → **截断到 0**，无负数记录 |
| 盘点调账幂等 | ✅ 重复调用不重复调账，`total` 只返回未处理项 |
| 数据还原 | ✅ 测试数据全部清理，`cost_price` 全 NULL，台账回到 8 条、流水 8 条 |

---

## 五、上线步骤

```bash
# 1. 字段与权限
mysql -uroot -p -D ry-vue < sql/upgrade_20261123_wms_material_cost_price.sql

# 2. 历史金额回填（一次性，可重复执行）
mysql -uroot -p -D ry-vue < sql/upgrade_20261123_wms_inbound_total_amount_backfill.sql

# 3. 部署新 jar（必须与脚本同步上线，旧 jar 不读 cost_price）
# 4. 角色管理里给仓管/财务勾选「库存金额查看」
```

回填脚本执行前后都会打印校验结果，并列出无价明细导致的仍为 0 的单据——
那是真实数据现状，不是脚本失败。

盘点成本联动无需额外 SQL，上线后在「财务管理 → 存货成本」点「盘点调账」按钮即可；
建议流程：盘点审批通过 → 入库计价 → 出库结转 → **盘点调账**。

---

## 六、遗留项与已知问题

### 移库不联动成本（已评估，结论：不需要）

`WmsMoveOrderServiceImpl` 只改库位、不动台账。**这是有意的取舍**，理由：

- `sumValuation` 只 `GROUP BY material_id`，`sumTotalCost` 只 `SUM(total_cost)`
- 移库不改物料、不改仓库 → 两个汇总口径都不受影响
- 只有在**按库位分摊成本**时才需要同步；当前系统没有该口径

⚠️ 但由此产生一个使用约束：**「结存金额」不可按库位横向对比或求和后再与总账核对**，
因为同一批货在库位间的成本分布可能与实物分布不一致。按物料/仓库汇总则始终准确。

若将来要按库位分摊，需把移库纳入 `adjustStockTake` 同类的旁路同步。

### 盘点调账的已知边界

- 盘亏**未引用 `moveQty` 语义**，仅按 `diff_qty` 判断方向，符合业务定义
- 台账结存不足以覆盖盘亏时**截断到 0** 并记 warn，流水按实际冲减量记（非请求量）
- 盘盈且该维度无台账时按三级取价建账；**三级都取不到则计入 fail**，
  不写 0 成本（实测物料 37 因无税率档案而 fail，属受控拒绝）
- 调账是**手动按钮**，需财务在盘点审批完成后执行；未点则台账仍落后

### 既有数据问题（非本次引入）

- 物料 38（`RMAT162519`）已软删除（`del_flag=2`）却仍有 5 有效库存
  → 取价查询带 `del_flag='0'` 过滤，该行金额显示 `-`（**故意不取已删物料的价格**）
  → 建议清理该孤儿库存
- 53 条入库明细中仅 24 条有单价，9 张单金额为 0 全部因明细无价
  → 这是兜底价要长期解决的场景，需业务侧补录价格后重新执行 `priceInbound`
  （口径：`received_qty > 0` 的明细范围为 24/53；全表 100 条为 62/100）
- 物料 37（`TMAT155942`）无税率档案 → 其盘盈调账会持续 fail，需补 `fin_material_tax`

### 需关注

- `priceInbound` / `carryOutbound` / `adjustStockTake` 都是**手动按钮**，无定时任务
  → 成本台账会落后于实时库存，库存页金额的 `estimate` 占比取决于财务点击频率

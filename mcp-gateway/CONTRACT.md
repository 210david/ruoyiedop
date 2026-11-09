# MCP 接口契约清单（Contract）

> 版本：1.0 ｜ 基准：标准版 main（2026-11-06）
> 用途：网关 5 个工具依赖的后端接口契约。**客户分支定制凡触及本清单内的接口（改路径/改字段/改权限字符），必须在本文末尾「分支偏差登记表」登记**，并同步修改该分支的 tool-manifests。
> 规则：merge 冲突 = 契约漂移信号，逐条对照本清单处理。

---

## 0. 通用契约（所有工具共用）

| 项 | 契约 |
|----|------|
| 登录 | `POST /login` body `{"username","password"}` → `{"code":200,"token"}` |
| 鉴权头 | `Authorization: Bearer <token>`；HTTP 401 → 网关重登一次 |
| 分页 | 通用参数 `pageNum` / `pageSize`；响应 `TableDataInfo{total, rows[]}` |
| 业务响应 | `AjaxResult{code:200, msg, data}`；code≠200 视为业务错误 |
| 服务账号权限 | 8 个权限点见各工具；账号 `edop-mcp` + 角色 `edop_mcp_readonly`（`sql/edop_mcp_service_account.sql`） |
| 网关侧约束 | 仅 GET；日期范围 ≤92 天；pageSize ≤100 |

## 1. get_bi_metrics（BI 指标）

| 接口 | 契约 |
|------|------|
| `GET /bi/dashboard/overview` | 权限 `bi:dashboard:view`；无参；返回 `data{snapshotDate, alerts[{level,metricCode,message}], trend[{metricCode,metricDate,metricValue,unit,dataFlag,deptId}], metrics[{metricCode,metricName,domain,unit,value,dataFlag}]}` |
| `GET /bi/metric/list` | 权限 `bi:metric:list`；参数 `metricName`（模糊）+分页；rows 字段：`metricCode, metricName, caliberDesc, unit, calcRef` 等（BiMetricDict） |

## 2. query_inventory（库存）

| 接口 | 契约 |
|------|------|
| `GET /wms/inventory/list` | 权限 `wms:inventory:list`；查询参数（WmsInventory 绑定）：`materialCode`（精确）、`materialName`（模糊）、`warehouseName`（模糊）、`batchNo` +分页；rows 关键字段：`materialCode, materialName, specModel, warehouseName, locationName, areaName, batchNo, qty, lockQty, unit` |

## 3. query_sales_orders（销售订单）

| 接口 | 契约 |
|------|------|
| `GET /mk/order/list` | 权限 `marketing:order:list`；查询参数（MkOrder 绑定）：`orderNo`（模糊）、`customerName`（模糊）、`orderStatus`、`params[beginTime]` / `params[endTime]`（下单日期范围，格式 YYYY-MM-DD）+分页；rows 关键字段：`orderNo, customerName, orderAmount, orderStatus, createTime, userName` |

## 4. query_purchase_orders（采购订单）

| 接口 | 契约 |
|------|------|
| `GET /pms/order/list` | 权限 `pms:order:list`；查询参数（PmsPurchaseOrder 绑定）：`orderNo`（模糊）、`supplierName`（模糊）、`status`、`params[beginTime]` / `params[endTime]` +分页；rows 关键字段：`orderNo, supplierName, status, totalQty, totalAmount, receivedAmount, paidAmount, orderDate, buyerName` |

## 5. get_platform_overview（平台总览聚合）

并行调用 4 个看板接口，任一失败降级返回（`_partialErrors` 标注）：

| 接口 | 权限 | 返回 |
|------|------|------|
| `GET /bi/dashboard/overview` | `bi:dashboard:view` | BI 指标总览（同 1） |
| `GET /wms/statistics/overview` | `wms:statistics:list` | `data{count, qty, inboundQty, outboundQty, lockQty, ...}` |
| `GET /mk/dashboard/overview` | `marketing:report:view` | `data{customerCount, contractCount, contractAmount, orderCount, orderAmount, wonAmount, leadCount, opportunityCount, activityCount, ...}` |
| `GET /pms/dashboard/summary` | `pms:dashboard:view` | `data{reqTotal, reqApproved, ordTotal, ordCompleted, ordTotalAmount, ordReceivedAmount, contractTotal, contractTotalAmount, inquiryTotal, inquiryAwarded, ...}` |

---

## 分支偏差登记表

> 分支定制触及上述任何契约时，在此追加行，并同步修改该分支 `tool-manifests/<branch>.tools.json`。

| 分支 | 变更接口 | 变更内容 | 生效网关版本 | 登记日期 | 登记人 |
|------|---------|---------|-------------|---------|--------|
| （示例）dwx | GET /mk/order/list | 路径改为 /dwx/order/list | v1.1-manifest | | |
| lyjs | 新增 lyjs.tools.json | 分支工具清单（含标准 5 工具），PROFILE=lyjs 时加载 | v1.0 | 2026-11-09 | david |
| lyjs | GET /pjm/project/list | 新增工具 query_projects（项目管理列表：projectNo/projectName/status 过滤+分页） | v1.0 | 2026-11-09 | david |
| lyjs | GET /safety/hazard/list | 新增工具 query_safety_hazards（安全隐患列表：仅分页，无过滤参数） | v1.0 | 2026-11-09 | david |
| lyjs | GET /qms/task/list | 新增工具 query_quality_tasks（质量检验任务列表：仅分页，无过滤参数） | v1.0 | 2026-11-09 | david |
| lyjs | GET /bid/project/list | 新增工具 query_bid_projects（投标项目列表：projectNo/projectName/status 过滤+分页） | v1.0 | 2026-11-09 | david |

# 采购管理模块开发状态对照表

## 菜单结构（已按需求重构）

```
采购管理
├── 1、供应商管理
│   ├── 供应商信息 ✅ 已开发
│   ├── 供应商资质 ✅ 已开发
│   ├── 供货清单 ✅ 已开发
│   └── 供应商评价 ✅ 已开发
├── 2、采购需求管理
│   ├── 采购计划 ✅ 已开发
│   └── 采购申请 ✅ 已开发
├── 3、询比价管理 ✅ 已开发
├── 4、合同和订单管理
│   ├── 采购合同 ✅ 已开发
│   └── 采购订单 ✅ 已开发
├── 5、收货和发票管理
│   ├── 收货验收 ✅ 已开发
│   ├── 退货管理 ✅ 已开发
│   └── 发票结算 ✅ 已开发
├── 6、统计分析
│   └── 采购看板 ✅ 已开发
└── 7、编号规则 ✅ 已开发
```

### 1、供应商管理 ✅ 已完成

| 子模块 | 开发状态 | 页面路径 | visible |
|--------|----------|----------|---------|
| 供应商信息 | ✅ 已完成 | wms/supplier/index | 0 |
| 供应商资质管理 | ✅ 已完成 | pms/qualification/index | 0 |
| 供应商供货清单 | ✅ 已完成 | pms/catalog/index | 0 |
| 供应商评价 | ✅ 已完成 | pms/eval/index | 0 |

### 2、采购需求管理 ✅ 已完成

| 子模块 | 开发状态 | 页面路径 | visible |
|--------|----------|----------|---------|
| 采购计划管理 | ✅ 已完成 | pms/plan/index | 0 |
| 采购申请管理 | ✅ 已完成 | pms/request/index | 0 |

### 3、询比价管理 ✅ 已完成

| 子模块 | 开发状态 | 页面路径 | visible |
|--------|----------|----------|---------|
| 询比价管理 | ✅ 已完成 | pms/inquiry/index | 0 |

### 4、合同和订单管理 ✅ 已完成

| 子模块 | 开发状态 | 页面路径 | visible |
|--------|----------|----------|---------|
| 采购合同管理 | ✅ 已完成 | pms/contract/index | 0 |
| 采购订单管理 | ✅ 已完成 | pms/order/index | 0 |

### 5、收货和发票管理 ✅ 已完成

| 子模块 | 开发状态 | 页面路径 | visible |
|--------|----------|----------|---------|
| 收货验收 | ✅ 已完成 | pms/receive/index | 0 |
| 退货管理 | ✅ 已完成 | pms/return/index | 0 |
| 发票结算 | ✅ 已完成 | pms/invoice/index | 0 |

### 6、统计分析 ✅ 已完成

| 子模块 | 开发状态 | 页面路径 | visible |
|--------|----------|----------|---------|
| 采购看板 | ✅ 已完成 | pms/dashboard/index | 0 |

### 7、编号规则 ✅ 已完成

| 子模块 | 开发状态 | 页面路径 | visible |
|--------|----------|----------|---------|
| 编号规则 | ✅ 已完成 | pms/numberRule/index | 0 |

---

## 开发完成总结

**所有模块已全部开发完成！**

---

## 数据库表清单

### 已开发表

#### 供应商管理
- `wms_supplier` - 供应商信息（复用WMS）
- `pms_supplier_qualification` - 供应商资质
- `pms_supplier_catalog` - 供应商供货清单
- `pms_supplier_eval` - 供应商评价

#### 采购需求管理
- `pms_purchase_plan` - 采购计划 ⭐ 新增
- `pms_purchase_plan_detail` - 采购计划明细 ⭐ 新增
- `pms_purchase_request` - 采购申请
- `pms_purchase_request_detail` - 采购申请明细

#### 询比价管理 ⭐ 新增
- `pms_inquiry` - 询价单
- `pms_inquiry_detail` - 询价明细
- `pms_quotation` - 供应商报价
- `pms_quotation_detail` - 报价明细

#### 合同和订单管理
- `pms_purchase_contract` - 采购合同
- `pms_purchase_contract_detail` - 采购合同明细
- `pms_purchase_order` - 采购订单
- `pms_purchase_order_detail` - 采购订单明细

#### 收货和发票管理
- `pms_receive` - 收货验收
- `pms_receive_detail` - 收货明细
- `pms_return` - 退货管理
- `pms_return_detail` - 退货明细
- `pms_invoice` - 发票结算
- `pms_invoice_detail` - 发票明细

---

## 新增字典类型
- `pms_plan_status` - 采购计划状态
- `pms_plan_type` - 采购计划类型
- `pms_inquiry_status` - 询价单状态
- `pms_inquiry_type` - 询价方式
- `pms_purchase_type` - 采购类型

---

## 新增编号规则
- `PMS_PLAN` - 采购计划编号（前缀：PLAN）
- `PMS_INQUIRY` - 询价单编号（前缀：XJ）

---

## 文件清单

### 后端文件（ruoyi-pms模块）
```
src/main/java/com/ruoyi/pms/
├── domain/
│   ├── PmsPurchasePlan.java
│   ├── PmsPurchasePlanDetail.java
│   ├── PmsInquiry.java
│   ├── PmsInquiryDetail.java
│   ├── PmsQuotation.java
│   └── PmsQuotationDetail.java
├── mapper/
│   ├── PmsPurchasePlanMapper.java
│   └── PmsInquiryMapper.java
├── service/
│   ├── IPmsPurchasePlanService.java
│   ├── IPmsInquiryService.java
│   └── impl/
│       ├── PmsPurchasePlanServiceImpl.java
│       └── PmsInquiryServiceImpl.java
└── controller/
    ├── PmsPurchasePlanController.java
    └── PmsInquiryController.java

src/main/resources/mapper/pms/
├── PmsPurchasePlanMapper.xml
└── PmsInquiryMapper.xml
```

### 前端文件（ruoyi-ui）
```
src/api/pms/
├── plan.js
└── inquiry.js

src/views/pms/
├── plan/
│   └── index.vue
└── inquiry/
    └── index.vue
```

### SQL文件
```
sql/
├── pms_menu_new_structure.sql - 菜单结构重构
├── pms_purchase_plan.sql - 采购计划表
├── pms_inquiry.sql - 询比价表
└── pms_menu_visible_update.sql - 菜单可见性更新
```

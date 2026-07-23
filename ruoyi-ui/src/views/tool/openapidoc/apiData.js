/**
 * 开放接口(Open API) 详细文档数据
 * 每个接口包含完整说明、参数表、响应字段表、多语言调用示例
 */

export const API_BASE_INFO = {
  baseUrl: '/openapi/v1',
  authMethod: 'X-API-Key',
  authDesc: '在HTTP请求头中携带 X-API-Key 进行鉴权，无需登录Token。API Key由管理员在「系统管理 → 开放接口」中创建分配。',
  version: 'v1',
  updated: '2026-07-23'
}

const PP = [
  { name: 'pageNum', type: 'Integer', required: false, desc: '页码，默认1' },
  { name: 'pageSize', type: 'Integer', required: false, desc: '每页条数，默认10，最大100' }
]

function codes(method, path, query) {
  const url = `http://your-server:8081${path}${query ? '?' + query : ''}`
  return {
    curl: `curl -X ${method} -H "X-API-Key: YOUR_API_KEY" "${url}"`,
    java: `HttpClient client = HttpClient.newHttpClient();
HttpRequest req = HttpRequest.newBuilder()
    .uri(URI.create("${url}"))
    .header("X-API-Key", "YOUR_API_KEY")
    .GET().build();
HttpResponse<String> resp = client.send(req, BodyHandlers.ofString());
System.out.println(resp.body());`,
    python: `import requests
resp = requests.${method.toLowerCase()}("${url}",
    headers={"X-API-Key": "YOUR_API_KEY"})
print(resp.json())`,
    js: `const resp = await fetch("${url}", {
  headers: { "X-API-Key": "YOUR_API_KEY" }
});
const data = await resp.json();
console.log(data);`
  }
}

export const API_MODULES = [
  {
    name: 'WMS 仓库管理',
    code: 'wms',
    desc: '物料、供应商、仓库、出入库、库存、预警',
    apis: [
      {
        id: 'wms-material-list',
        category: '物料管理',
        method: 'GET',
        path: '/openapi/v1/wms/material/list',
        summary: '分页查询物料列表',
        desc: '查询物料主数据列表，支持按名称、编码、类型筛选，返回分页结构。物料是仓库管理的核心基础数据，包含编码、名称、规格型号、计量单位、安全库存、效期管理等完整信息。',
        params: [
          ...PP,
          { name: 'materialName', type: 'String', required: false, desc: '物料名称，模糊匹配' },
          { name: 'materialCode', type: 'String', required: false, desc: '物料编码，精确匹配' },
          { name: 'materialType', type: 'String', required: false, desc: '物料类型：0=原材料 1=半成品 2=成品 3=辅料' },
          { name: 'status', type: 'String', required: false, desc: '状态：0=正常 1=停用' }
        ],
        responseFields: [
          { name: 'materialId', type: 'Long', desc: '物料ID' },
          { name: 'materialCode', type: 'String', desc: '物料编码（系统唯一）' },
          { name: 'materialName', type: 'String', desc: '物料名称' },
          { name: 'materialType', type: 'String', desc: '类型：0=原材料 1=半成品 2=成品 3=辅料' },
          { name: 'specModel', type: 'String', desc: '规格型号' },
          { name: 'unit', type: 'String', desc: '计量单位：0=个 1=件 2=箱 3=kg 4=吨 5=米' },
          { name: 'safetyStockMin', type: 'BigDecimal', desc: '安全库存下限' },
          { name: 'safetyStockMax', type: 'BigDecimal', desc: '安全库存上限' },
          { name: 'isExpiryManage', type: 'String', desc: '效期管理：0=否 1=是' },
          { name: 'isBatchManage', type: 'String', desc: '批次管理：0=否 1=是' },
          { name: 'status', type: 'String', desc: '状态：0=正常 1=停用' }
        ],
        responseExample: `{
  "code": 200,
  "msg": "查询成功",
  "rows": [{
    "materialId": 1,
    "materialCode": "MAT20250001",
    "materialName": "冷轧钢板",
    "materialType": "0",
    "specModel": "1000×2000×3mm",
    "unit": "4",
    "safetyStockMin": 100,
    "safetyStockMax": 1000,
    "isExpiryManage": "0",
    "isBatchManage": "1",
    "status": "0"
  }],
  "total": 10
}`,
        codes: codes('GET', '/openapi/v1/wms/material/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'wms-material-get',
        category: '物料管理',
        method: 'GET',
        path: '/openapi/v1/wms/material/{materialCode}',
        summary: '按编码查询物料详情',
        desc: '根据物料编码查询单条物料完整信息。物料不存在时返回错误。',
        params: [{ name: 'materialCode', type: 'String', required: true, in: 'path', desc: '物料编码（URL路径参数）' }],
        responseExample: `{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "materialId": 1,
    "materialCode": "MAT20250001",
    "materialName": "冷轧钢板",
    "materialType": "0",
    "specModel": "1000×2000×3mm",
    "unit": "4",
    "status": "0"
  }
}`,
        codes: codes('GET', '/openapi/v1/wms/material/MAT20250001')
      },
      {
        id: 'wms-supplier-list',
        category: '供应商管理',
        method: 'GET',
        path: '/openapi/v1/wms/supplier/list',
        summary: '分页查询供应商列表',
        desc: '查询供应商主数据，支持按名称模糊筛选。供应商是采购入库业务的关联方。',
        params: [
          ...PP,
          { name: 'supplierName', type: 'String', required: false, desc: '供应商名称，模糊匹配' },
          { name: 'status', type: 'String', required: false, desc: '状态：0=正常 1=停用' }
        ],
        responseFields: [
          { name: 'supplierId', type: 'Long', desc: '供应商ID' },
          { name: 'supplierCode', type: 'String', desc: '供应商编码' },
          { name: 'supplierName', type: 'String', desc: '供应商名称' },
          { name: 'contactPerson', type: 'String', desc: '联系人' },
          { name: 'contactPhone', type: 'String', desc: '联系电话' },
          { name: 'address', type: 'String', desc: '地址' },
          { name: 'status', type: 'String', desc: '状态：0=正常 1=停用' }
        ],
        responseExample: `{
  "code": 200,
  "msg": "查询成功",
  "rows": [{
    "supplierId": 1,
    "supplierCode": "SUP001",
    "supplierName": "XX钢铁有限公司",
    "contactPerson": "张三",
    "contactPhone": "13800138000",
    "address": "上海市浦东新区XX路",
    "status": "0"
  }],
  "total": 5
}`,
        codes: codes('GET', '/openapi/v1/wms/supplier/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'wms-supplier-get',
        category: '供应商管理',
        method: 'GET',
        path: '/openapi/v1/wms/supplier/{supplierId}',
        summary: '查询供应商详情',
        desc: '根据供应商ID查询供应商完整信息。',
        params: [{ name: 'supplierId', type: 'Long', required: true, in: 'path', desc: '供应商ID' }],
        responseExample: `{ "code": 200, "msg": "操作成功", "data": { "supplierId": 1, "supplierCode": "SUP001", "supplierName": "XX钢铁有限公司" } }`,
        codes: codes('GET', '/openapi/v1/wms/supplier/1')
      },
      {
        id: 'wms-warehouse-list',
        category: '仓库结构',
        method: 'GET',
        path: '/openapi/v1/wms/warehouse/list',
        summary: '查询仓库结构列表',
        desc: '查询仓库/仓区/库位的树形结构数据，不分页。仓库采用三级结构：仓库→仓区→库位。',
        params: [{ name: 'warehouseName', type: 'String', required: false, desc: '仓库名称，模糊匹配' }],
        responseFields: [
          { name: 'warehouseId', type: 'Long', desc: '仓库节点ID' },
          { name: 'warehouseCode', type: 'String', desc: '仓库编码' },
          { name: 'warehouseName', type: 'String', desc: '仓库名称' },
          { name: 'nodeType', type: 'String', desc: '节点类型：1=仓库 2=仓区 3=库位' },
          { name: 'parentId', type: 'Long', desc: '父节点ID' },
          { name: 'status', type: 'String', desc: '状态：0=正常 1=停用' }
        ],
        responseExample: `{
  "code": 200,
  "msg": "操作成功",
  "data": [{
    "warehouseId": 1, "warehouseCode": "WH001",
    "warehouseName": "中心仓", "nodeType": "1", "parentId": 0
  }]
}`,
        codes: codes('GET', '/openapi/v1/wms/warehouse/list')
      },
      {
        id: 'wms-warehouse-get',
        category: '仓库结构',
        method: 'GET',
        path: '/openapi/v1/wms/warehouse/{warehouseId}',
        summary: '查询仓库详情',
        desc: '根据仓库节点ID查询单条仓库/仓区/库位信息。',
        params: [{ name: 'warehouseId', type: 'Long', required: true, in: 'path', desc: '仓库节点ID' }],
        responseExample: `{ "code": 200, "data": { "warehouseId": 1, "warehouseCode": "WH001", "warehouseName": "中心仓", "nodeType": "1" } }`,
        codes: codes('GET', '/openapi/v1/wms/warehouse/1')
      },
      {
        id: 'wms-inbound-list',
        category: '入库管理',
        method: 'GET',
        path: '/openapi/v1/wms/inbound/list',
        summary: '分页查询入库单列表',
        desc: '查询入库单据列表，支持按单号、状态、入库类型筛选。入库单记录物料从外部进入仓库的完整流程。',
        params: [
          ...PP,
          { name: 'orderCode', type: 'String', required: false, desc: '入库单号' },
          { name: 'orderStatus', type: 'String', required: false, desc: '状态：0=草稿 1=已提交 2=收货中 3=已完成' },
          { name: 'inboundType', type: 'String', required: false, desc: '入库类型：0=采购 1=生产 2=退货 3=调拨' }
        ],
        responseFields: [
          { name: 'orderId', type: 'Long', desc: '入库单ID' },
          { name: 'orderCode', type: 'String', desc: '入库单号' },
          { name: 'inboundType', type: 'String', desc: '入库类型：0=采购 1=生产 2=退货 3=调拨' },
          { name: 'orderStatus', type: 'String', desc: '状态：0=草稿 1=已提交 2=收货中 3=已完成' },
          { name: 'supplierId', type: 'Long', desc: '供应商ID' },
          { name: 'warehouseId', type: 'Long', desc: '目标仓库ID' },
          { name: 'totalQty', type: 'BigDecimal', desc: '总数量' },
          { name: 'createTime', type: 'Date', desc: '创建时间' }
        ],
        responseExample: `{
  "code": 200,
  "msg": "查询成功",
  "rows": [{
    "orderId": 1, "orderCode": "IN20260718001",
    "inboundType": "0", "orderStatus": "3",
    "supplierId": 1, "warehouseId": 1, "totalQty": 100
  }],
  "total": 8
}`,
        codes: codes('GET', '/openapi/v1/wms/inbound/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'wms-inbound-get',
        category: '入库管理',
        method: 'GET',
        path: '/openapi/v1/wms/inbound/{orderId}',
        summary: '查询入库单详情',
        desc: '根据入库单ID查询单条入库单完整信息，含明细行。',
        params: [{ name: 'orderId', type: 'Long', required: true, in: 'path', desc: '入库单ID' }],
        responseExample: `{ "code": 200, "data": { "orderId": 1, "orderCode": "IN20260718001", "orderStatus": "3", "details": [...] } }`,
        codes: codes('GET', '/openapi/v1/wms/inbound/1')
      },
      {
        id: 'wms-outbound-list',
        category: '出库管理',
        method: 'GET',
        path: '/openapi/v1/wms/outbound/list',
        summary: '分页查询出库单列表',
        desc: '查询出库单据列表，支持按单号、状态、出库类型筛选。出库单记录物料从仓库发出的完整流程。',
        params: [
          ...PP,
          { name: 'orderCode', type: 'String', required: false, desc: '出库单号' },
          { name: 'orderStatus', type: 'String', required: false, desc: '状态：0=草稿 1=已提交 2=拣货中 3=已复核 4=已出库' },
          { name: 'outboundType', type: 'String', required: false, desc: '出库类型：0=销售 1=生产领料 2=退货 3=调拨' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{ "orderId": 1, "orderCode": "OUT20260718001", "outboundType": "0", "orderStatus": "4" }],
  "total": 6
}`,
        codes: codes('GET', '/openapi/v1/wms/outbound/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'wms-outbound-get',
        category: '出库管理',
        method: 'GET',
        path: '/openapi/v1/wms/outbound/{orderId}',
        summary: '查询出库单详情',
        desc: '根据出库单ID查询单条出库单完整信息，含明细行。',
        params: [{ name: 'orderId', type: 'Long', required: true, in: 'path', desc: '出库单ID' }],
        responseExample: `{ "code": 200, "data": { "orderId": 1, "orderCode": "OUT20260718001", "details": [...] } }`,
        codes: codes('GET', '/openapi/v1/wms/outbound/1')
      },
      {
        id: 'wms-inventory-list',
        category: '库存管理',
        method: 'GET',
        path: '/openapi/v1/wms/inventory/list',
        summary: '分页查询库存列表',
        desc: '查询库存余额列表，支持按物料、仓库、批次、库位多维度筛选。库存余额=可用库存+锁定库存。',
        params: [
          ...PP,
          { name: 'materialCode', type: 'String', required: false, desc: '物料编码' },
          { name: 'warehouseId', type: 'Long', required: false, desc: '仓库ID' },
          { name: 'batchNo', type: 'String', required: false, desc: '批次号' }
        ],
        responseFields: [
          { name: 'inventoryId', type: 'Long', desc: '库存ID' },
          { name: 'warehouseName', type: 'String', desc: '仓库名称' },
          { name: 'materialCode', type: 'String', desc: '物料编码' },
          { name: 'materialName', type: 'String', desc: '物料名称' },
          { name: 'batchNo', type: 'String', desc: '批次号' },
          { name: 'totalQty', type: 'BigDecimal', desc: '总数量' },
          { name: 'availableQty', type: 'BigDecimal', desc: '可用数量' },
          { name: 'lockedQty', type: 'BigDecimal', desc: '锁定数量' },
          { name: 'unit', type: 'String', desc: '计量单位' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "inventoryId": 1, "warehouseName": "中心仓",
    "materialCode": "MAT20250001", "materialName": "冷轧钢板",
    "batchNo": "B20260718", "totalQty": 98,
    "availableQty": 50, "lockedQty": 48, "unit": "4"
  }],
  "total": 15
}`,
        codes: codes('GET', '/openapi/v1/wms/inventory/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'wms-inventory-get',
        category: '库存管理',
        method: 'GET',
        path: '/openapi/v1/wms/inventory/{inventoryId}',
        summary: '查询库存详情',
        desc: '根据库存ID查询单条库存余额详情。',
        params: [{ name: 'inventoryId', type: 'Long', required: true, in: 'path', desc: '库存ID' }],
        responseExample: `{ "code": 200, "data": { "inventoryId": 1, "totalQty": 98, "availableQty": 50 } }`,
        codes: codes('GET', '/openapi/v1/wms/inventory/1')
      },
      {
        id: 'wms-stockalert-list',
        category: '库存预警',
        method: 'GET',
        path: '/openapi/v1/wms/stockAlert/list',
        summary: '查询库存预警列表',
        desc: '查询库存预警数据，包括库存不足（低于安全库存下限）、库存积压（高于安全库存上限）、临期预警、过期预警四种类型。',
        params: [...PP],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "alertType": "low_stock", "materialCode": "MAT20250005",
    "materialName": "螺栓", "qty": 50, "safetyStockMin": 100
  }],
  "total": 3
}`,
        codes: codes('GET', '/openapi/v1/wms/stockAlert/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'wms-stockalert-summary',
        category: '库存预警',
        method: 'GET',
        path: '/openapi/v1/wms/stockAlert/summary',
        summary: '库存预警汇总',
        desc: '统计各类型预警数量，用于首页看板展示。',
        params: [],
        responseExample: `{
  "code": 200,
  "data": [
    { "alertType": "low_stock", "count": 15 },
    { "alertType": "overstock", "count": 8 },
    { "alertType": "near_expiry", "count": 3 },
    { "alertType": "expired", "count": 1 }
  ]
}`,
        codes: codes('GET', '/openapi/v1/wms/stockAlert/summary')
      }
    ]
  },
  {
    name: 'DMS 设备管理',
    code: 'dms',
    desc: '设备、分类、工单、备件、备件出入库、备件预警',
    apis: [
      {
        id: 'dms-equipment-list',
        category: '设备台账',
        method: 'GET',
        path: '/openapi/v1/dms/equipment/list',
        summary: '分页查询设备列表',
        desc: '查询设备台账列表，支持按设备名称、编码、分类、状态筛选。设备台账是设备管理的核心主数据。',
        params: [
          ...PP,
          { name: 'equipmentName', type: 'String', required: false, desc: '设备名称，模糊匹配' },
          { name: 'equipmentCode', type: 'String', required: false, desc: '设备编码' },
          { name: 'categoryId', type: 'Long', required: false, desc: '设备分类ID' },
          { name: 'equipmentStatus', type: 'String', required: false, desc: '状态：0=运行 1=空闲 2=维修 4=故障 5=报废' }
        ],
        responseFields: [
          { name: 'equipmentId', type: 'Long', desc: '设备ID' },
          { name: 'equipmentCode', type: 'String', desc: '设备编码' },
          { name: 'equipmentName', type: 'String', desc: '设备名称' },
          { name: 'categoryId', type: 'Long', desc: '分类ID' },
          { name: 'categoryName', type: 'String', desc: '分类名称' },
          { name: 'equipmentStatus', type: 'String', desc: '状态：0=运行 1=空闲 2=维修 4=故障 5=报废' },
          { name: 'model', type: 'String', desc: '规格型号' },
          { name: 'manufacturer', type: 'String', desc: '生产厂家' },
          { name: 'installLocation', type: 'String', desc: '安装位置' },
          { name: 'purchaseDate', type: 'Date', desc: '购置日期' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "equipmentId": 1, "equipmentCode": "EQ001",
    "equipmentName": "数控车床A", "categoryName": "金属切削机床",
    "equipmentStatus": "0", "model": "CK6140",
    "manufacturer": "济南机床厂", "installLocation": "A车间-01工位"
  }],
  "total": 7
}`,
        codes: codes('GET', '/openapi/v1/dms/equipment/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'dms-equipment-get',
        category: '设备台账',
        method: 'GET',
        path: '/openapi/v1/dms/equipment/{equipmentId}',
        summary: '查询设备详情',
        desc: '根据设备ID查询设备台账完整信息。',
        params: [{ name: 'equipmentId', type: 'Long', required: true, in: 'path', desc: '设备ID' }],
        responseExample: `{ "code": 200, "data": { "equipmentId": 1, "equipmentCode": "EQ001", "equipmentName": "数控车床A" } }`,
        codes: codes('GET', '/openapi/v1/dms/equipment/1')
      },
      {
        id: 'dms-category-list',
        category: '设备分类',
        method: 'GET',
        path: '/openapi/v1/dms/category/list',
        summary: '查询设备分类列表',
        desc: '查询设备分类树形结构数据，不分页。分类采用树形结构管理。',
        params: [{ name: 'categoryName', type: 'String', required: false, desc: '分类名称，模糊匹配' }],
        responseExample: `{
  "code": 200,
  "data": [{ "categoryId": 1, "categoryName": "金属切削机床", "parentId": 0 }]
}`,
        codes: codes('GET', '/openapi/v1/dms/category/list')
      },
      {
        id: 'dms-category-get',
        category: '设备分类',
        method: 'GET',
        path: '/openapi/v1/dms/category/{categoryId}',
        summary: '查询设备分类详情',
        desc: '根据分类ID查询单条设备分类信息。',
        params: [{ name: 'categoryId', type: 'Long', required: true, in: 'path', desc: '分类ID' }],
        responseExample: `{ "code": 200, "data": { "categoryId": 1, "categoryName": "金属切削机床" } }`,
        codes: codes('GET', '/openapi/v1/dms/category/1')
      },
      {
        id: 'dms-workorder-list',
        category: '工单管理',
        method: 'GET',
        path: '/openapi/v1/dms/workorder/list',
        summary: '分页查询工单列表',
        desc: '查询维修/保养工单列表，支持按工单号、类型、状态、设备筛选。工单记录设备维修保养的全生命周期。',
        params: [
          ...PP,
          { name: 'orderCode', type: 'String', required: false, desc: '工单编号' },
          { name: 'orderType', type: 'String', required: false, desc: '类型：0=维修 1=保养 2=巡检 3=安装' },
          { name: 'orderStatus', type: 'String', required: false, desc: '状态：0=待派工 1=已派工 2=已接单 3=处理中 4=已完工 5=已验收' },
          { name: 'equipmentId', type: 'Long', required: false, desc: '设备ID' }
        ],
        responseFields: [
          { name: 'orderId', type: 'Long', desc: '工单ID' },
          { name: 'orderCode', type: 'String', desc: '工单编号' },
          { name: 'orderType', type: 'String', desc: '类型：0=维修 1=保养 2=巡检 3=安装' },
          { name: 'orderStatus', type: 'String', desc: '状态：0=待派工 1=已派工 2=已接单 3=处理中 4=已完工 5=已验收' },
          { name: 'equipmentId', type: 'Long', desc: '设备ID' },
          { name: 'equipmentName', type: 'String', desc: '设备名称' },
          { name: 'faultDesc', type: 'String', desc: '故障描述' },
          { name: 'priority', type: 'String', desc: '优先级：0=低 1=中 2=高 3=紧急' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "orderId": 1, "orderCode": "WO20260718001",
    "orderType": "0", "orderStatus": "5",
    "equipmentName": "数控车床A", "faultDesc": "主轴异响",
    "priority": "2"
  }],
  "total": 12
}`,
        codes: codes('GET', '/openapi/v1/dms/workorder/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'dms-workorder-get',
        category: '工单管理',
        method: 'GET',
        path: '/openapi/v1/dms/workorder/{orderId}',
        summary: '查询工单详情',
        desc: '根据工单ID查询单条工单完整信息。',
        params: [{ name: 'orderId', type: 'Long', required: true, in: 'path', desc: '工单ID' }],
        responseExample: `{ "code": 200, "data": { "orderId": 1, "orderCode": "WO20260718001", "orderStatus": "5" } }`,
        codes: codes('GET', '/openapi/v1/dms/workorder/1')
      },
      {
        id: 'dms-sparepart-list',
        category: '备件管理',
        method: 'GET',
        path: '/openapi/v1/dms/sparepart/list',
        summary: '分页查询备件列表',
        desc: '查询备件主数据列表，支持按备件名称、编码筛选。备件是设备维修保养所需的零部件。',
        params: [
          ...PP,
          { name: 'partName', type: 'String', required: false, desc: '备件名称，模糊匹配' },
          { name: 'partCode', type: 'String', required: false, desc: '备件编码' }
        ],
        responseFields: [
          { name: 'partId', type: 'Long', desc: '备件ID' },
          { name: 'partCode', type: 'String', desc: '备件编码' },
          { name: 'partName', type: 'String', desc: '备件名称' },
          { name: 'specModel', type: 'String', desc: '规格型号' },
          { name: 'unit', type: 'String', desc: '计量单位' },
          { name: 'currentStock', type: 'BigDecimal', desc: '当前库存' },
          { name: 'safetyStock', type: 'BigDecimal', desc: '安全库存' },
          { name: 'unitPrice', type: 'BigDecimal', desc: '单价' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "partId": 1, "partCode": "SP001", "partName": "主轴轴承",
    "specModel": "7008C", "unit": "个",
    "currentStock": 50, "safetyStock": 20, "unitPrice": 350
  }],
  "total": 8
}`,
        codes: codes('GET', '/openapi/v1/dms/sparepart/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'dms-sparepart-get',
        category: '备件管理',
        method: 'GET',
        path: '/openapi/v1/dms/sparepart/{partId}',
        summary: '查询备件详情',
        desc: '根据备件ID查询单条备件完整信息。',
        params: [{ name: 'partId', type: 'Long', required: true, in: 'path', desc: '备件ID' }],
        responseExample: `{ "code": 200, "data": { "partId": 1, "partName": "主轴轴承", "currentStock": 50 } }`,
        codes: codes('GET', '/openapi/v1/dms/sparepart/1')
      },
      {
        id: 'dms-sparepart-record',
        category: '备件出入库',
        method: 'GET',
        path: '/openapi/v1/dms/sparepart/record/list',
        summary: '查询备件出入库记录',
        desc: '分页查询备件出入库流水记录，记录每次备件入库/出库的详细信息。',
        params: [
          ...PP,
          { name: 'partId', type: 'Long', required: false, desc: '备件ID' },
          { name: 'moveType', type: 'String', required: false, desc: '类型：0=入库 1=出库' }
        ],
        responseFields: [
          { name: 'recordId', type: 'Long', desc: '记录ID' },
          { name: 'partCode', type: 'String', desc: '备件编码' },
          { name: 'partName', type: 'String', desc: '备件名称' },
          { name: 'moveType', type: 'String', desc: '类型：0=入库 1=出库' },
          { name: 'quantity', type: 'BigDecimal', desc: '数量' },
          { name: 'beforeStock', type: 'BigDecimal', desc: '变动前库存' },
          { name: 'afterStock', type: 'BigDecimal', desc: '变动后库存' },
          { name: 'operateDate', type: 'Date', desc: '操作日期' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "recordId": 1, "partName": "主轴轴承", "moveType": "1",
    "quantity": 5, "beforeStock": 55, "afterStock": 50
  }],
  "total": 20
}`,
        codes: codes('GET', '/openapi/v1/dms/sparepart/record/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'dms-sparepart-alert',
        category: '备件库存预警',
        method: 'GET',
        path: '/openapi/v1/dms/sparepart/alert/list',
        summary: '查询备件库存预警',
        desc: '查询当前库存低于安全库存下限或高于上限的备件列表，用于备件补货提醒。',
        params: [...PP],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "partId": 5, "partCode": "SP005", "partName": "密封圈",
    "currentStock": 10, "stockMin": 20, "unit": "个"
  }],
  "total": 2
}`,
        codes: codes('GET', '/openapi/v1/dms/sparepart/alert/list', 'pageNum=1&pageSize=10')
      }
    ]
  },
  {
    name: 'MK 营销管理',
    code: 'mk',
    desc: '客户、线索、商机、合同、订单、联系人、活动、互动记录、统计看板、回款',
    apis: [
      {
        id: 'mk-customer-list',
        category: '客户管理',
        method: 'GET',
        path: '/openapi/v1/mk/customer/list',
        summary: '分页查询客户列表',
        desc: '查询企业客户列表，支持按客户名称、状态、行业、等级筛选。客户是营销管理的核心主数据。',
        params: [
          ...PP,
          { name: 'customerName', type: 'String', required: false, desc: '客户名称，模糊匹配' },
          { name: 'customerStatus', type: 'String', required: false, desc: '状态：0=潜在 1=活跃 2=流失 3=黑名单' },
          { name: 'industry', type: 'String', required: false, desc: '行业' },
          { name: 'customerLevel', type: 'String', required: false, desc: '等级：0=普通 1=重要 2=战略' }
        ],
        responseFields: [
          { name: 'customerId', type: 'Long', desc: '客户ID' },
          { name: 'customerCode', type: 'String', desc: '客户编码' },
          { name: 'customerName', type: 'String', desc: '客户名称' },
          { name: 'customerStatus', type: 'String', desc: '状态：0=潜在 1=活跃 2=流失 3=黑名单' },
          { name: 'industry', type: 'String', desc: '行业' },
          { name: 'customerLevel', type: 'String', desc: '等级：0=普通 1=重要 2=战略' },
          { name: 'contactPerson', type: 'String', desc: '联系人' },
          { name: 'contactPhone', type: 'String', desc: '联系电话' },
          { name: 'address', type: 'String', desc: '地址' },
          { name: 'ownerUserName', type: 'String', desc: '负责人姓名' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "customerId": 1, "customerCode": "CUS20260001",
    "customerName": "XX科技有限公司", "customerStatus": "1",
    "industry": "制造业", "customerLevel": "1",
    "contactPerson": "张三", "contactPhone": "13800138000",
    "ownerUserName": "李销售"
  }],
  "total": 50
}`,
        codes: codes('GET', '/openapi/v1/mk/customer/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'mk-customer-get',
        category: '客户管理',
        method: 'GET',
        path: '/openapi/v1/mk/customer/{customerId}',
        summary: '查询客户详情',
        desc: '根据客户ID查询客户完整信息，包含联系人、最近互动等关联数据。',
        params: [{ name: 'customerId', type: 'Long', required: true, in: 'path', desc: '客户ID' }],
        responseExample: `{
  "code": 200, "msg": "操作成功",
  "data": {
    "customerId": 1, "customerCode": "CUS20260001",
    "customerName": "XX科技有限公司", "customerStatus": "1",
    "industry": "制造业", "customerLevel": "1",
    "contactPerson": "张三", "contactPhone": "13800138000",
    "address": "北京市海淀区XX路", "ownerUserName": "李销售"
  }
}`,
        codes: codes('GET', '/openapi/v1/mk/customer/1')
      },
      {
        id: 'mk-lead-list',
        category: '线索管理',
        method: 'GET',
        path: '/openapi/v1/mk/lead/list',
        summary: '分页查询线索列表',
        desc: '查询销售线索列表，支持按公司名称、线索状态、来源筛选。线索是营销漏斗的入口数据。',
        params: [
          ...PP,
          { name: 'companyName', type: 'String', required: false, desc: '公司名称，模糊匹配' },
          { name: 'leadStatus', type: 'String', required: false, desc: '状态：0=待分配 1=跟进中 2=已转化 3=无效' },
          { name: 'leadSource', type: 'String', required: false, desc: '来源：0=官网 1=广告 2=活动 3=转介绍 4=其他' }
        ],
        responseFields: [
          { name: 'leadId', type: 'Long', desc: '线索ID' },
          { name: 'companyName', type: 'String', desc: '公司名称' },
          { name: 'contactName', type: 'String', desc: '联系人姓名' },
          { name: 'contactPhone', type: 'String', desc: '联系电话' },
          { name: 'leadStatus', type: 'String', desc: '状态：0=待分配 1=跟进中 2=已转化 3=无效' },
          { name: 'leadSource', type: 'String', desc: '来源：0=官网 1=广告 2=活动 3=转介绍 4=其他' },
          { name: 'ownerUserName', type: 'String', desc: '负责人姓名' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "leadId": 1, "companyName": "YY贸易公司",
    "contactName": "王五", "contactPhone": "13900139000",
    "leadStatus": "1", "leadSource": "0", "ownerUserName": "李销售"
  }],
  "total": 30
}`,
        codes: codes('GET', '/openapi/v1/mk/lead/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'mk-lead-get',
        category: '线索管理',
        method: 'GET',
        path: '/openapi/v1/mk/lead/{leadId}',
        summary: '查询线索详情',
        desc: '根据线索ID查询单条线索完整信息。',
        params: [{ name: 'leadId', type: 'Long', required: true, in: 'path', desc: '线索ID' }],
        responseExample: `{ "code": 200, "data": { "leadId": 1, "companyName": "YY贸易公司", "leadStatus": "1" } }`,
        codes: codes('GET', '/openapi/v1/mk/lead/1')
      },
      {
        id: 'mk-opportunity-list',
        category: '商机管理',
        method: 'GET',
        path: '/openapi/v1/mk/opportunity/list',
        summary: '分页查询商机列表',
        desc: '查询销售商机列表，支持按商机名称、状态、阶段筛选。商机是正在推进的销售机会。',
        params: [
          ...PP,
          { name: 'opportunityName', type: 'String', required: false, desc: '商机名称，模糊匹配' },
          { name: 'opportunityStatus', type: 'String', required: false, desc: '状态：0=进行中 1=赢单 2=输单 3=搁置' },
          { name: 'stageCode', type: 'String', required: false, desc: '阶段编码' }
        ],
        responseFields: [
          { name: 'opportunityId', type: 'Long', desc: '商机ID' },
          { name: 'opportunityName', type: 'String', desc: '商机名称' },
          { name: 'customerId', type: 'Long', desc: '客户ID' },
          { name: 'customerName', type: 'String', desc: '客户名称' },
          { name: 'opportunityStatus', type: 'String', desc: '状态：0=进行中 1=赢单 2=输单 3=搁置' },
          { name: 'stageCode', type: 'String', desc: '当前阶段编码' },
          { name: 'stageName', type: 'String', desc: '当前阶段名称' },
          { name: 'estimatedAmount', type: 'BigDecimal', desc: '预计金额' },
          { name: 'estimatedCloseDate', type: 'Date', desc: '预计成交日期' },
          { name: 'ownerUserName', type: 'String', desc: '负责人姓名' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "opportunityId": 1, "opportunityName": "XX系统采购",
    "customerName": "XX科技有限公司", "opportunityStatus": "0",
    "stageName": "方案报价", "estimatedAmount": 500000,
    "ownerUserName": "李销售"
  }],
  "total": 15
}`,
        codes: codes('GET', '/openapi/v1/mk/opportunity/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'mk-opportunity-get',
        category: '商机管理',
        method: 'GET',
        path: '/openapi/v1/mk/opportunity/{opportunityId}',
        summary: '查询商机详情',
        desc: '根据商机ID查询单条商机完整信息。',
        params: [{ name: 'opportunityId', type: 'Long', required: true, in: 'path', desc: '商机ID' }],
        responseExample: `{ "code": 200, "data": { "opportunityId": 1, "opportunityName": "XX系统采购", "opportunityStatus": "0" } }`,
        codes: codes('GET', '/openapi/v1/mk/opportunity/1')
      },
      {
        id: 'mk-contract-list',
        category: '合同管理',
        method: 'GET',
        path: '/openapi/v1/mk/contract/list',
        summary: '分页查询合同列表',
        desc: '查询合同列表，支持按合同名称、状态筛选。合同记录已签约的商业协议。',
        params: [
          ...PP,
          { name: 'contractName', type: 'String', required: false, desc: '合同名称，模糊匹配' },
          { name: 'contractStatus', type: 'String', required: false, desc: '状态：0=草稿 1=审批中 2=已生效 3=已到期 4=已终止' },
          { name: 'customerId', type: 'Long', required: false, desc: '客户ID' }
        ],
        responseFields: [
          { name: 'contractId', type: 'Long', desc: '合同ID' },
          { name: 'contractCode', type: 'String', desc: '合同编号' },
          { name: 'contractName', type: 'String', desc: '合同名称' },
          { name: 'customerId', type: 'Long', desc: '客户ID' },
          { name: 'customerName', type: 'String', desc: '客户名称' },
          { name: 'contractStatus', type: 'String', desc: '状态：0=草稿 1=审批中 2=已生效 3=已到期 4=已终止' },
          { name: 'contractAmount', type: 'BigDecimal', desc: '合同金额' },
          { name: 'signDate', type: 'Date', desc: '签订日期' },
          { name: 'expireDate', type: 'Date', desc: '到期日期' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "contractId": 1, "contractCode": "HT20260001",
    "contractName": "XX系统采购合同", "customerName": "XX科技有限公司",
    "contractStatus": "2", "contractAmount": 500000,
    "signDate": "2026-06-01", "expireDate": "2027-06-01"
  }],
  "total": 8
}`,
        codes: codes('GET', '/openapi/v1/mk/contract/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'mk-contract-get',
        category: '合同管理',
        method: 'GET',
        path: '/openapi/v1/mk/contract/{contractId}',
        summary: '查询合同详情',
        desc: '根据合同ID查询单条合同完整信息。',
        params: [{ name: 'contractId', type: 'Long', required: true, in: 'path', desc: '合同ID' }],
        responseExample: `{ "code": 200, "data": { "contractId": 1, "contractCode": "HT20260001", "contractStatus": "2" } }`,
        codes: codes('GET', '/openapi/v1/mk/contract/1')
      },
      {
        id: 'mk-order-list',
        category: '订单管理',
        method: 'GET',
        path: '/openapi/v1/mk/order/list',
        summary: '分页查询订单列表',
        desc: '查询销售订单列表，支持按订单编号、状态筛选。订单记录已确认的交易。',
        params: [
          ...PP,
          { name: 'orderCode', type: 'String', required: false, desc: '订单编号' },
          { name: 'orderStatus', type: 'String', required: false, desc: '状态：0=待确认 1=已确认 2=已发货 3=已签收 4=已完成 5=已取消' },
          { name: 'customerId', type: 'Long', required: false, desc: '客户ID' }
        ],
        responseFields: [
          { name: 'orderId', type: 'Long', desc: '订单ID' },
          { name: 'orderCode', type: 'String', desc: '订单编号' },
          { name: 'customerId', type: 'Long', desc: '客户ID' },
          { name: 'customerName', type: 'String', desc: '客户名称' },
          { name: 'orderStatus', type: 'String', desc: '状态：0=待确认 1=已确认 2=已发货 3=已签收 4=已完成 5=已取消' },
          { name: 'totalAmount', type: 'BigDecimal', desc: '订单总额' },
          { name: 'orderDate', type: 'Date', desc: '下单日期' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "orderId": 1, "orderCode": "DD20260001",
    "customerName": "XX科技有限公司", "orderStatus": "4",
    "totalAmount": 500000, "orderDate": "2026-06-15"
  }],
  "total": 12
}`,
        codes: codes('GET', '/openapi/v1/mk/order/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'mk-order-get',
        category: '订单管理',
        method: 'GET',
        path: '/openapi/v1/mk/order/{orderId}',
        summary: '查询订单详情',
        desc: '根据订单ID查询单条订单完整信息。',
        params: [{ name: 'orderId', type: 'Long', required: true, in: 'path', desc: '订单ID' }],
        responseExample: `{ "code": 200, "data": { "orderId": 1, "orderCode": "DD20260001", "orderStatus": "4" } }`,
        codes: codes('GET', '/openapi/v1/mk/order/1')
      },
      {
        id: 'mk-contact-list',
        category: '联系人管理',
        method: 'GET',
        path: '/openapi/v1/mk/contact/list',
        summary: '分页查询联系人列表',
        desc: '查询客户联系人列表，支持按姓名、客户ID筛选。',
        params: [
          ...PP,
          { name: 'contactName', type: 'String', required: false, desc: '联系人姓名，模糊匹配' },
          { name: 'customerId', type: 'Long', required: false, desc: '客户ID' }
        ],
        responseFields: [
          { name: 'contactId', type: 'Long', desc: '联系人ID' },
          { name: 'customerId', type: 'Long', desc: '客户ID' },
          { name: 'customerName', type: 'String', desc: '客户名称' },
          { name: 'contactName', type: 'String', desc: '联系人姓名' },
          { name: 'phone', type: 'String', desc: '电话' },
          { name: 'email', type: 'String', desc: '邮箱' },
          { name: 'position', type: 'String', desc: '职务' },
          { name: 'isPrimary', type: 'String', desc: '是否主要联系人：0=否 1=是' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "contactId": 1, "customerId": 1, "customerName": "XX科技有限公司",
    "contactName": "张三", "phone": "13800138000",
    "email": "zhangsan@xx.com", "position": "采购经理", "isPrimary": "1"
  }],
  "total": 25
}`,
        codes: codes('GET', '/openapi/v1/mk/contact/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'mk-contact-get',
        category: '联系人管理',
        method: 'GET',
        path: '/openapi/v1/mk/contact/{contactId}',
        summary: '查询联系人详情',
        desc: '根据联系人ID查询单条联系人完整信息。',
        params: [{ name: 'contactId', type: 'Long', required: true, in: 'path', desc: '联系人ID' }],
        responseExample: `{ "code": 200, "data": { "contactId": 1, "contactName": "张三", "phone": "13800138000" } }`,
        codes: codes('GET', '/openapi/v1/mk/contact/1')
      },
      {
        id: 'mk-activity-list',
        category: '营销活动',
        method: 'GET',
        path: '/openapi/v1/mk/activity/list',
        summary: '分页查询活动列表',
        desc: '查询营销活动列表，支持按活动名称、状态筛选。',
        params: [
          ...PP,
          { name: 'activityName', type: 'String', required: false, desc: '活动名称，模糊匹配' },
          { name: 'activityType', type: 'String', required: false, desc: '类型：0=展会 1=研讨会 2=网络直播 3=促销 4=其他' },
          { name: 'status', type: 'String', required: false, desc: '状态：0=草稿 1=进行中 2=已结束 3=已取消' }
        ],
        responseFields: [
          { name: 'activityId', type: 'Long', desc: '活动ID' },
          { name: 'activityName', type: 'String', desc: '活动名称' },
          { name: 'activityType', type: 'String', desc: '类型：0=展会 1=研讨会 2=网络直播 3=促销 4=其他' },
          { name: 'status', type: 'String', desc: '状态：0=草稿 1=进行中 2=已结束 3=已取消' },
          { name: 'startDate', type: 'Date', desc: '开始日期' },
          { name: 'endDate', type: 'Date', desc: '结束日期' },
          { name: 'location', type: 'String', desc: '活动地点' },
          { name: 'participantCount', type: 'Integer', desc: '参与人数' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "activityId": 1, "activityName": "2026工业自动化展",
    "activityType": "0", "status": "1",
    "startDate": "2026-07-01", "endDate": "2026-07-03",
    "location": "上海国家会展中心", "participantCount": 120
  }],
  "total": 5
}`,
        codes: codes('GET', '/openapi/v1/mk/activity/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'mk-activity-get',
        category: '营销活动',
        method: 'GET',
        path: '/openapi/v1/mk/activity/{activityId}',
        summary: '查询活动详情',
        desc: '根据活动ID查询单条活动完整信息。',
        params: [{ name: 'activityId', type: 'Long', required: true, in: 'path', desc: '活动ID' }],
        responseExample: `{ "code": 200, "data": { "activityId": 1, "activityName": "2026工业自动化展", "status": "1" } }`,
        codes: codes('GET', '/openapi/v1/mk/activity/1')
      },
      {
        id: 'mk-interaction-list',
        category: '互动记录',
        method: 'GET',
        path: '/openapi/v1/mk/interaction/list',
        summary: '分页查询互动记录列表',
        desc: '查询客户互动记录列表，支持按客户ID、互动类型筛选。记录每次客户拜访、电话、邮件等互动。',
        params: [
          ...PP,
          { name: 'customerId', type: 'Long', required: false, desc: '客户ID' },
          { name: 'interactionType', type: 'String', required: false, desc: '互动类型：0=电话 1=拜访 2=邮件 3=微信 4=会议 5=其他' }
        ],
        responseFields: [
          { name: 'recordId', type: 'Long', desc: '记录ID' },
          { name: 'customerId', type: 'Long', desc: '客户ID' },
          { name: 'customerName', type: 'String', desc: '客户名称' },
          { name: 'interactionType', type: 'String', desc: '互动类型：0=电话 1=拜访 2=邮件 3=微信 4=会议 5=其他' },
          { name: 'interactionContent', type: 'String', desc: '互动内容' },
          { name: 'interactionTime', type: 'Date', desc: '互动时间' },
          { name: 'ownerUserName', type: 'String', desc: '记录人' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "recordId": 1, "customerId": 1, "customerName": "XX科技有限公司",
    "interactionType": "1", "interactionContent": "上门拜访讨论系统方案",
    "interactionTime": "2026-07-15 14:00:00", "ownerUserName": "李销售"
  }],
  "total": 40
}`,
        codes: codes('GET', '/openapi/v1/mk/interaction/list', 'pageNum=1&pageSize=10')
      },
      {
        id: 'mk-dashboard-overview',
        category: '统计看板',
        method: 'GET',
        path: '/openapi/v1/mk/dashboard/overview',
        summary: '营销概览统计',
        desc: '获取营销管理驾驶舱概览数据，包含客户数、线索数、商机数、合同金额、订单金额等核心指标。',
        params: [],
        responseExample: `{
  "code": 200, "data": {
    "customerCount": 150, "leadCount": 80,
    "opportunityCount": 30, "contractAmount": 2500000,
    "orderAmount": 1800000, "receivedAmount": 1200000
  }
}`,
        codes: codes('GET', '/openapi/v1/mk/dashboard/overview')
      },
      {
        id: 'mk-dashboard-lead-funnel',
        category: '统计看板',
        method: 'GET',
        path: '/openapi/v1/mk/dashboard/leadFunnel',
        summary: '线索转化漏斗',
        desc: '获取线索转化漏斗数据，展示从线索到客户的各阶段转化情况。',
        params: [],
        responseExample: `{
  "code": 200, "data": [
    { "stage": "线索总数", "count": 80, "rate": 100 },
    { "stage": "已分配", "count": 65, "rate": 81.3 },
    { "stage": "跟进中", "count": 40, "rate": 50.0 },
    { "stage": "已转化", "count": 22, "rate": 27.5 }
  ]
}`,
        codes: codes('GET', '/openapi/v1/mk/dashboard/leadFunnel')
      },
      {
        id: 'mk-dashboard-opportunity-pipeline',
        category: '统计看板',
        method: 'GET',
        path: '/openapi/v1/mk/dashboard/opportunityPipeline',
        summary: '商机销售漏斗',
        desc: '获取商机销售漏斗数据，展示各阶段的商机数量和金额。',
        params: [],
        responseExample: `{
  "code": 200, "data": [
    { "stageCode": "S1", "stageName": "需求确认", "count": 30, "amount": 3000000 },
    { "stageCode": "S2", "stageName": "方案报价", "count": 20, "amount": 2000000 },
    { "stageCode": "S3", "stageName": "商务谈判", "count": 10, "amount": 1000000 }
  ]
}`,
        codes: codes('GET', '/openapi/v1/mk/dashboard/opportunityPipeline')
      },
      {
        id: 'mk-dashboard-sales-ranking',
        category: '统计看板',
        method: 'GET',
        path: '/openapi/v1/mk/dashboard/salesRanking',
        summary: '销售业绩排行',
        desc: '获取销售人员业绩排行榜，按合同金额或订单金额排名。',
        params: [],
        responseExample: `{
  "code": 200, "data": [
    { "userId": 1, "userName": "李销售", "contractAmount": 800000, "orderAmount": 600000, "customerCount": 15 },
    { "userId": 2, "userName": "王销售", "contractAmount": 500000, "orderAmount": 400000, "customerCount": 10 }
  ]
}`,
        codes: codes('GET', '/openapi/v1/mk/dashboard/salesRanking')
      },
      {
        id: 'mk-dashboard-revenue-trend',
        category: '统计看板',
        method: 'GET',
        path: '/openapi/v1/mk/dashboard/revenueTrend',
        summary: '收入月度趋势',
        desc: '获取合同和订单金额的月度趋势数据，用于分析收入走势。',
        params: [],
        responseExample: `{
  "code": 200, "data": [
    { "month": "2026-01", "contractAmount": 200000, "orderAmount": 150000 },
    { "month": "2026-02", "contractAmount": 300000, "orderAmount": 250000 },
    { "month": "2026-03", "contractAmount": 450000, "orderAmount": 400000 }
  ]
}`,
        codes: codes('GET', '/openapi/v1/mk/dashboard/revenueTrend')
      },
      {
        id: 'mk-payment-statistics',
        category: '回款管理',
        method: 'GET',
        path: '/openapi/v1/mk/payment/statistics',
        summary: '回款统计',
        desc: '获取回款汇总统计：总计划金额、已回款金额、待回款金额、逾期金额。',
        params: [],
        responseExample: `{
  "code": 200, "data": {
    "totalPlanAmount": 2000000, "receivedAmount": 1200000,
    "pendingAmount": 600000, "overdueAmount": 200000
  }
}`,
        codes: codes('GET', '/openapi/v1/mk/payment/statistics')
      },
      {
        id: 'mk-payment-list',
        category: '回款管理',
        method: 'GET',
        path: '/openapi/v1/mk/payment/list',
        summary: '分页查询回款计划列表',
        desc: '查询回款计划列表，支持按客户ID、计划状态筛选。',
        params: [
          ...PP,
          { name: 'customerId', type: 'Long', required: false, desc: '客户ID' },
          { name: 'planStatus', type: 'String', required: false, desc: '计划状态：0=待回款 1=部分回款 2=已回款 3=已逾期' }
        ],
        responseFields: [
          { name: 'planId', type: 'Long', desc: '回款计划ID' },
          { name: 'contractId', type: 'Long', desc: '关联合同ID' },
          { name: 'customerId', type: 'Long', desc: '客户ID' },
          { name: 'customerName', type: 'String', desc: '客户名称' },
          { name: 'planAmount', type: 'BigDecimal', desc: '计划回款金额' },
          { name: 'receivedAmount', type: 'BigDecimal', desc: '已回款金额' },
          { name: 'planDate', type: 'Date', desc: '计划回款日期' },
          { name: 'planStatus', type: 'String', desc: '状态：0=待回款 1=部分回款 2=已回款 3=已逾期' }
        ],
        responseExample: `{
  "code": 200, "msg": "查询成功",
  "rows": [{
    "planId": 1, "contractId": 1, "customerName": "XX科技有限公司",
    "planAmount": 250000, "receivedAmount": 250000,
    "planDate": "2026-06-30", "planStatus": "2"
  }],
  "total": 10
}`,
        codes: codes('GET', '/openapi/v1/mk/payment/list', 'pageNum=1&pageSize=10')
      }
    ]
  }
]

// 扁平化所有接口，用于导航
export const ALL_APIS = API_MODULES.flatMap(m =>
  m.apis.map(a => ({ ...a, moduleCode: m.code, moduleName: m.name }))
)

/**
 * 业务模块 API 接口文档数据
 * 涵盖仓库管理(WMS)与设备管理(DMS)全部接口
 * 每个接口包含：method、path、summary、permission、params、requestExample、responseExample、remark
 */

// 通用响应说明
const COMMON_RESPONSE = {
  success: `{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}`,
  table: `{
  "code": 200,
  "msg": "查询成功",
  "rows": [ ... ],
  "total": 100
}`,
  error: `{
  "code": 500,
  "msg": "操作失败，XXX不能为空"
}`
}

// 分页参数说明（通用）
const PAGE_PARAMS = [
  { name: 'pageNum', type: 'Integer', required: false, desc: '当前页码，默认 1' },
  { name: 'pageSize', type: 'Integer', required: false, desc: '每页条数，默认 10' },
  { name: 'orderByColumn', type: 'String', required: false, desc: '排序字段' },
  { name: 'isAsc', type: 'String', required: false, desc: '排序方向：asc/desc' }
]

// 通用 CRUD 接口生成器
function crud({
  basePath, entityName, idField, permPrefix, title,
  queryParams, bodyFields, exampleBody, exampleResponse,
  hasExport = true, exportPerm,
  getInfoPath
}) {
  const apis = []
  // list
  apis.push({
    method: 'GET',
    path: `${basePath}/list`,
    summary: `查询${title}列表`,
    permission: `${permPrefix}:list`,
    params: [...PAGE_PARAMS, ...(queryParams || [])],
    requestExample: `GET ${basePath}/list?pageNum=1&pageSize=10`,
    responseExample: exampleResponse || COMMON_RESPONSE.table,
    remark: `分页查询${title}列表，支持多条件筛选。返回 TableDataInfo 分页结构。`
  })
  // export
  if (hasExport) {
    apis.push({
      method: 'POST',
      path: `${basePath}/export`,
      summary: `导出${title}数据`,
      permission: exportPerm || `${permPrefix}:export`,
      params: queryParams || [],
      requestExample: `POST ${basePath}/export\n# 以 form 表单方式传递查询条件，响应为 Excel 文件流`,
      responseExample: `# 直接返回 application/vnd.openxmlformats-officedocument.spreadsheetml.sheet 文件流\n# 浏览器自动下载 ${title}数据.xlsx`,
      remark: `按查询条件导出 Excel，后台通过 ExcelUtil 生成。`
    })
  }
  // getInfo
  apis.push({
    method: 'GET',
    path: getInfoPath || `${basePath}/{${idField}}`,
    summary: `获取${title}详细信息`,
    permission: `${permPrefix}:query`,
    params: [{ name: idField, type: 'Long', required: true, desc: `${title}ID`, in: 'path' }],
    requestExample: `GET ${basePath}/1`,
    responseExample: `{
  "code": 200,
  "msg": "操作成功",
  "data": ${exampleBody || '{ ... }'}
}`,
    remark: `根据主键查询单条${title}详情。`
  })
  // add
  apis.push({
    method: 'POST',
    path: basePath,
    summary: `新增${title}`,
    permission: `${permPrefix}:add`,
    params: bodyFields ? bodyFields.map(f => ({ ...f, in: 'body' })) : [],
    requestExample: `POST ${basePath}\nContent-Type: application/json\n\n${exampleBody || '{ ... }'}`,
    responseExample: COMMON_RESPONSE.success,
    remark: `新增一条${title}记录，请求体为 JSON 对象。`
  })
  // edit
  apis.push({
    method: 'PUT',
    path: basePath,
    summary: `修改${title}`,
    permission: `${permPrefix}:edit`,
    params: bodyFields ? bodyFields.map(f => ({ ...f, in: 'body' })) : [],
    requestExample: `PUT ${basePath}\nContent-Type: application/json\n\n${exampleBody || '{ ... }'}`,
    responseExample: COMMON_RESPONSE.success,
    remark: `修改一条${title}记录，需携带主键 ${idField}。`
  })
  // remove
  apis.push({
    method: 'DELETE',
    path: `${basePath}/{${idField}s`,
    summary: `删除${title}`,
    permission: `${permPrefix}:remove`,
    params: [{ name: `${idField}s`, type: 'Long[]', required: true, desc: `${title}ID数组，逗号分隔`, in: 'path' }],
    requestExample: `DELETE ${basePath}/1,2,3`,
    responseExample: COMMON_RESPONSE.success,
    remark: `支持批量删除，主键以逗号分隔拼接在路径中。`
  })
  return apis
}

// ==================== 仓库管理模块 (WMS) ====================

const WMS_MODULES = [
  {
    name: '仓库结构管理',
    basePath: '/wms/warehouse',
    description: '仓库/仓区/仓位三级树形结构管理，支持自动编码生成。',
    apis: [
      ...crud({
        basePath: '/wms/warehouse',
        title: '仓库结构',
        idField: 'warehouseId',
        permPrefix: 'wms:warehouse',
        queryParams: [
          { name: 'warehouseCode', type: 'String', required: false, desc: '仓库编码' },
          { name: 'warehouseName', type: 'String', required: false, desc: '仓库名称' },
          { name: 'nodeType', type: 'String', required: false, desc: '节点类型：1=仓库 2=仓区 3=仓位' },
          { name: 'status', type: 'String', required: false, desc: '状态：0=正常 1=停用' }
        ],
        bodyFields: [
          { name: 'warehouseId', type: 'Long', required: false, desc: '仓库ID（修改时必填）' },
          { name: 'parentId', type: 'Long', required: false, desc: '父级ID' },
          { name: 'warehouseCode', type: 'String', required: true, desc: '仓库编码' },
          { name: 'warehouseName', type: 'String', required: true, desc: '仓库名称' },
          { name: 'nodeType', type: 'String', required: true, desc: '节点类型：1=仓库 2=仓区 3=仓位' },
          { name: 'deptId', type: 'Long', required: false, desc: '管理部门ID' },
          { name: 'managerId', type: 'Long', required: false, desc: '负责人用户ID' },
          { name: 'address', type: 'String', required: false, desc: '仓库地址' },
          { name: 'areaType', type: 'String', required: false, desc: '库区类型：0=存储区 1=收货区 2=发货区 3=退货区 4=暂存区' },
          { name: 'locationType', type: 'String', required: false, desc: '库位类型：0=货架位 1=地堆位 2=暂存位' },
          { name: 'capacity', type: 'BigDecimal', required: false, desc: '库位容量' },
          { name: 'status', type: 'String', required: false, desc: '状态：0=正常 1=停用' }
        ],
        exampleBody: `{
  "warehouseId": 1,
  "parentId": 0,
  "warehouseCode": "WH001",
  "warehouseName": "中心仓",
  "nodeType": "1",
  "deptId": 100,
  "managerId": 1,
  "address": "北京市朝阳区XX路1号",
  "status": "0"
}`,
        exampleResponse: `{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "warehouseId": 1,
      "warehouseCode": "WH001",
      "warehouseName": "中心仓",
      "nodeType": "1",
      "status": "0",
      "children": [
        {
          "warehouseId": 2,
          "warehouseCode": "WH001-A01",
          "warehouseName": "A区",
          "nodeType": "2"
        }
      ]
    }
  ],
  "total": 1
}`
      }),
      {
        method: 'GET',
        path: '/wms/warehouse/genCode',
        summary: '生成下一个仓库编码',
        permission: 'wms:warehouse:add',
        params: [
          { name: 'nodeType', type: 'String', required: true, desc: '节点类型：1=仓库 2=仓区 3=仓位' },
          { name: 'parentId', type: 'Long', required: false, desc: '父级ID（生成仓区/仓位时传入）' }
        ],
        requestExample: 'GET /wms/warehouse/genCode?nodeType=2&parentId=1',
        responseExample: `{
  "code": 200,
  "msg": "操作成功",
  "data": "WH001-A02"
}`,
        remark: '根据节点类型和父级自动生成下一个编码，遵循层级编码规则。'
      },
      {
        method: 'GET',
        path: '/wms/warehouse/warehouseList',
        summary: '查询仓库列表（仅仓库层级）',
        permission: 'wms:warehouse:list',
        params: PAGE_PARAMS,
        requestExample: 'GET /wms/warehouse/warehouseList?pageNum=1&pageSize=10',
        responseExample: COMMON_RESPONSE.table,
        remark: '固定过滤 nodeType=1，返回分页结构 TableDataInfo。'
      },
      {
        method: 'GET',
        path: '/wms/warehouse/area/list',
        summary: '查询仓区列表（仅仓区层级）',
        permission: 'wms:area:list',
        params: [
          ...PAGE_PARAMS,
          { name: 'warehouseId', type: 'Long', required: false, desc: '所属仓库ID（作为父级筛选条件）' }
        ],
        requestExample: 'GET /wms/warehouse/area/list?warehouseId=1&pageNum=1&pageSize=10',
        responseExample: COMMON_RESPONSE.table,
        remark: '固定过滤 nodeType=2，前端传 warehouseId 作为父仓库筛选条件。'
      },
      {
        method: 'GET',
        path: '/wms/warehouse/location/list',
        summary: '查询仓位列表（仅仓位层级）',
        permission: 'wms:location:list',
        params: [
          ...PAGE_PARAMS,
          { name: 'warehouseId', type: 'Long', required: false, desc: '所属仓库ID（通过 FIND_IN_SET 过滤祖级）' }
        ],
        requestExample: 'GET /wms/warehouse/location/list?warehouseId=1&pageNum=1&pageSize=10',
        responseExample: COMMON_RESPONSE.table,
        remark: '固定过滤 nodeType=3，仓位是仓库的孙节点，mapper 中通过 FIND_IN_SET(warehouseId, ancestors) 过滤。'
      }
    ]
  },

  {
    name: '物料管理',
    basePath: '/wms/material',
    description: '物料主数据维护，含编码、类型、计量单位、效期管理、安全库存等。',
    apis: crud({
      basePath: '/wms/material',
      title: '物料',
      idField: 'materialId',
      permPrefix: 'wms:material',
      queryParams: [
        { name: 'materialCode', type: 'String', required: false, desc: '物料编码' },
        { name: 'materialName', type: 'String', required: false, desc: '物料名称' },
        { name: 'materialType', type: 'String', required: false, desc: '物料类型：0=原材料 1=半成品 2=成品 3=辅料' },
        { name: 'status', type: 'String', required: false, desc: '状态：0=正常 1=停用' }
      ],
      bodyFields: [
        { name: 'materialId', type: 'Long', required: false, desc: '物料ID（修改时必填）' },
        { name: 'materialCode', type: 'String', required: true, desc: '物料编码' },
        { name: 'materialName', type: 'String', required: true, desc: '物料名称' },
        { name: 'materialType', type: 'String', required: true, desc: '物料类型：0=原材料 1=半成品 2=成品 3=辅料' },
        { name: 'specModel', type: 'String', required: false, desc: '规格型号' },
        { name: 'unit', type: 'String', required: true, desc: '计量单位：0=个 1=件 2=箱 3=kg 4=吨 5=米' },
        { name: 'shelfLifeDays', type: 'Integer', required: false, desc: '保质期天数' },
        { name: 'expiryAlertDays', type: 'Integer', required: false, desc: '临期预警天数' },
        { name: 'isExpiryManage', type: 'String', required: false, desc: '效期管理：0=否 1=是' },
        { name: 'safetyStockMin', type: 'BigDecimal', required: false, desc: '安全库存下限' },
        { name: 'safetyStockMax', type: 'BigDecimal', required: false, desc: '安全库存上限' },
        { name: 'isBatchManage', type: 'String', required: false, desc: '批次管理：0=否 1=是' },
        { name: 'status', type: 'String', required: false, desc: '状态：0=正常 1=停用' }
      ],
      exampleBody: `{
  "materialId": 1,
  "materialCode": "MAT001",
  "materialName": "钢板",
  "materialType": "0",
  "specModel": "1000×2000×5mm",
  "unit": "3",
  "isExpiryManage": "0",
  "safetyStockMin": 100,
  "safetyStockMax": 1000,
  "isBatchManage": "1",
  "status": "0"
}`
    })
  },

  {
    name: '供应商管理',
    basePath: '/wms/supplier',
    description: '供应商基础信息维护。',
    apis: crud({
      basePath: '/wms/supplier',
      title: '供应商',
      idField: 'supplierId',
      permPrefix: 'wms:supplier',
      queryParams: [
        { name: 'supplierCode', type: 'String', required: false, desc: '供应商编码' },
        { name: 'supplierName', type: 'String', required: false, desc: '供应商名称' },
        { name: 'status', type: 'String', required: false, desc: '状态：0=正常 1=停用' }
      ],
      bodyFields: [
        { name: 'supplierId', type: 'Long', required: false, desc: '供应商ID（修改时必填）' },
        { name: 'supplierCode', type: 'String', required: true, desc: '供应商编码' },
        { name: 'supplierName', type: 'String', required: true, desc: '供应商名称' },
        { name: 'contactPerson', type: 'String', required: false, desc: '联系人' },
        { name: 'contactPhone', type: 'String', required: false, desc: '联系电话' },
        { name: 'address', type: 'String', required: false, desc: '地址' },
        { name: 'status', type: 'String', required: false, desc: '状态：0=正常 1=停用' }
      ],
      exampleBody: `{
  "supplierId": 1,
  "supplierCode": "SUP001",
  "supplierName": "XX钢铁有限公司",
  "contactPerson": "张三",
  "contactPhone": "13800138000",
  "address": "上海市浦东新区XX路",
  "status": "0"
}`
    })
  },

  {
    name: '入库管理',
    basePath: '/wms/inbound',
    description: '入库单全流程管理：新增→提交→收货→上架，含明细行。',
    apis: [
      ...crud({
        basePath: '/wms/inbound',
        title: '入库单',
        idField: 'orderId',
        permPrefix: 'wms:inbound',
        queryParams: [
          { name: 'orderCode', type: 'String', required: false, desc: '入库单号' },
          { name: 'orderStatus', type: 'String', required: false, desc: '单据状态：0=草稿 1=已提交 2=收货中 3=已完成' },
          { name: 'inboundType', type: 'String', required: false, desc: '入库类型：0=采购入库 1=生产入库 2=退货入库 3=调拨入库' },
          { name: 'supplierId', type: 'Long', required: false, desc: '供应商ID' }
        ],
        bodyFields: [
          { name: 'orderId', type: 'Long', required: false, desc: '入库单ID（修改时必填）' },
          { name: 'orderCode', type: 'String', required: false, desc: '入库单号（系统自动生成）' },
          { name: 'inboundType', type: 'String', required: true, desc: '入库类型：0=采购入库 1=生产入库 2=退货入库 3=调拨入库' },
          { name: 'supplierId', type: 'Long', required: false, desc: '供应商ID' },
          { name: 'warehouseId', type: 'Long', required: true, desc: '目标仓库ID' },
          { name: 'remark', type: 'String', required: false, desc: '备注' },
          { name: 'details', type: 'Array', required: true, desc: '入库明细行数组' }
        ],
        exampleBody: `{
  "orderId": 1,
  "orderCode": "IN20260718001",
  "inboundType": "0",
  "supplierId": 1,
  "warehouseId": 1,
  "remark": "采购入库",
  "details": [
    {
      "materialId": 1,
      "planQty": 100,
      "batchNo": "B20260718",
      "productionDate": "2026-07-18"
    }
  ]
}`
      }),
      {
        method: 'PUT',
        path: '/wms/inbound/submit/{orderId}',
        summary: '提交入库单',
        permission: 'wms:inbound:edit',
        params: [{ name: 'orderId', type: 'Long', required: true, desc: '入库单ID', in: 'path' }],
        requestExample: 'PUT /wms/inbound/submit/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '将草稿状态的入库单提交，提交后可进行收货操作。'
      },
      {
        method: 'PUT',
        path: '/wms/inbound/receive/{orderId}/{detailId}',
        summary: '入库收货',
        permission: 'wms:inbound:edit',
        params: [
          { name: 'orderId', type: 'Long', required: true, desc: '入库单ID', in: 'path' },
          { name: 'detailId', type: 'Long', required: true, desc: '明细行ID', in: 'path' },
          { name: 'receivedQty', type: 'BigDecimal', required: true, desc: '实收数量', in: 'query' }
        ],
        requestExample: 'PUT /wms/inbound/receive/1/10?receivedQty=98',
        responseExample: COMMON_RESPONSE.success,
        remark: '对入库单某明细行进行收货登记，实收数量可与计划数量不一致。'
      },
      {
        method: 'PUT',
        path: '/wms/inbound/putaway/{orderId}/{detailId}',
        summary: '入库上架',
        permission: 'wms:inbound:edit',
        params: [
          { name: 'orderId', type: 'Long', required: true, desc: '入库单ID', in: 'path' },
          { name: 'detailId', type: 'Long', required: true, desc: '明细行ID', in: 'path' },
          { name: 'locationId', type: 'Long', required: true, desc: '目标仓位ID', in: 'query' },
          { name: 'putawayQty', type: 'BigDecimal', required: true, desc: '上架数量', in: 'query' }
        ],
        requestExample: 'PUT /wms/inbound/putaway/1/10?locationId=5&putawayQty=98',
        responseExample: COMMON_RESPONSE.success,
        remark: '将已收货物上架到指定仓位，上架后更新库存余额。'
      }
    ]
  },

  {
    name: '出库管理',
    basePath: '/wms/outbound',
    description: '出库单全流程管理：新增→提交→拣货→复核，含明细行。',
    apis: [
      ...crud({
        basePath: '/wms/outbound',
        title: '出库单',
        idField: 'orderId',
        permPrefix: 'wms:outbound',
        queryParams: [
          { name: 'orderCode', type: 'String', required: false, desc: '出库单号' },
          { name: 'orderStatus', type: 'String', required: false, desc: '单据状态：0=草稿 1=已提交 2=拣货中 3=已复核 4=已出库' },
          { name: 'outboundType', type: 'String', required: false, desc: '出库类型：0=销售出库 1=生产领料 2=退货出库 3=调拨出库' }
        ],
        bodyFields: [
          { name: 'orderId', type: 'Long', required: false, desc: '出库单ID（修改时必填）' },
          { name: 'orderCode', type: 'String', required: false, desc: '出库单号' },
          { name: 'outboundType', type: 'String', required: true, desc: '出库类型' },
          { name: 'warehouseId', type: 'Long', required: true, desc: '来源仓库ID' },
          { name: 'remark', type: 'String', required: false, desc: '备注' },
          { name: 'details', type: 'Array', required: true, desc: '出库明细行数组' }
        ],
        exampleBody: `{
  "orderId": 1,
  "orderCode": "OUT20260718001",
  "outboundType": "0",
  "warehouseId": 1,
  "remark": "销售出库",
  "details": [
    {
      "materialId": 1,
      "planQty": 50,
      "locationId": 5
    }
  ]
}`
      }),
      {
        method: 'PUT',
        path: '/wms/outbound/submit/{orderId}',
        summary: '提交出库单',
        permission: 'wms:outbound:edit',
        params: [{ name: 'orderId', type: 'Long', required: true, desc: '出库单ID', in: 'path' }],
        requestExample: 'PUT /wms/outbound/submit/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '将草稿状态的出库单提交，提交后可进行拣货操作。'
      },
      {
        method: 'PUT',
        path: '/wms/outbound/pick/{orderId}/{detailId}',
        summary: '出库拣货',
        permission: 'wms:outbound:edit',
        params: [
          { name: 'orderId', type: 'Long', required: true, desc: '出库单ID', in: 'path' },
          { name: 'detailId', type: 'Long', required: true, desc: '明细行ID', in: 'path' },
          { name: 'pickQty', type: 'BigDecimal', required: true, desc: '拣货数量', in: 'query' }
        ],
        requestExample: 'PUT /wms/outbound/pick/1/10?pickQty=48',
        responseExample: COMMON_RESPONSE.success,
        remark: '对出库单某明细行进行拣货登记，从指定仓位扣减可用库存。'
      },
      {
        method: 'PUT',
        path: '/wms/outbound/check/{orderId}/{detailId}',
        summary: '出库复核',
        permission: 'wms:outbound:edit',
        params: [
          { name: 'orderId', type: 'Long', required: true, desc: '出库单ID', in: 'path' },
          { name: 'detailId', type: 'Long', required: true, desc: '明细行ID', in: 'path' },
          { name: 'actualQty', type: 'BigDecimal', required: true, desc: '复核实际数量', in: 'query' }
        ],
        requestExample: 'PUT /wms/outbound/check/1/10?actualQty=48',
        responseExample: COMMON_RESPONSE.success,
        remark: '复核拣货数量与实际数量，复核通过后完成出库并扣减库存余额。'
      }
    ]
  },

  {
    name: '移库管理',
    basePath: '/wms/move',
    description: '移库单管理：新增→审批→执行→作废，实现库间调拨。',
    apis: [
      ...crud({
        basePath: '/wms/move',
        title: '移库单',
        idField: 'moveId',
        permPrefix: 'wms:move',
        queryParams: [
          { name: 'moveCode', type: 'String', required: false, desc: '移库单号' },
          { name: 'moveStatus', type: 'String', required: false, desc: '状态：0=草稿 1=已审批 2=已执行 3=已作废' }
        ],
        bodyFields: [
          { name: 'moveId', type: 'Long', required: false, desc: '移库单ID' },
          { name: 'moveCode', type: 'String', required: false, desc: '移库单号' },
          { name: 'fromLocationId', type: 'Long', required: true, desc: '源仓位ID' },
          { name: 'toLocationId', type: 'Long', required: true, desc: '目标仓位ID' },
          { name: 'materialId', type: 'Long', required: true, desc: '物料ID' },
          { name: 'moveQty', type: 'BigDecimal', required: true, desc: '移库数量' },
          { name: 'remark', type: 'String', required: false, desc: '备注' }
        ],
        exampleBody: `{
  "moveId": 1,
  "moveCode": "MV20260718001",
  "fromLocationId": 5,
  "toLocationId": 6,
  "materialId": 1,
  "moveQty": 20,
  "remark": "仓位调整"
}`
      }),
      {
        method: 'PUT',
        path: '/wms/move/approve/{moveId}',
        summary: '移库审批',
        permission: 'wms:move:edit',
        params: [{ name: 'moveId', type: 'Long', required: true, desc: '移库单ID', in: 'path' }],
        requestExample: 'PUT /wms/move/approve/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '审批通过移库申请，审批后可执行移库。'
      },
      {
        method: 'PUT',
        path: '/wms/move/execute/{moveId}',
        summary: '执行移库',
        permission: 'wms:move:edit',
        params: [{ name: 'moveId', type: 'Long', required: true, desc: '移库单ID', in: 'path' }],
        requestExample: 'PUT /wms/move/execute/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '执行移库操作，从源仓位扣减库存并增加到目标仓位。'
      },
      {
        method: 'PUT',
        path: '/wms/move/void/{moveId}',
        summary: '移库作废',
        permission: 'wms:move:edit',
        params: [{ name: 'moveId', type: 'Long', required: true, desc: '移库单ID', in: 'path' }],
        requestExample: 'PUT /wms/move/void/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '将移库单作废，已执行的移库单不可作废。'
      }
    ]
  },

  {
    name: '库存查询',
    basePath: '/wms/inventory',
    description: '库存余额查询，支持按仓库、物料、批次多维度查询。',
    apis: [
      {
        method: 'GET',
        path: '/wms/inventory/list',
        summary: '查询库存列表',
        permission: 'wms:inventory:list',
        params: [
          ...PAGE_PARAMS,
          { name: 'warehouseId', type: 'Long', required: false, desc: '仓库ID' },
          { name: 'materialId', type: 'Long', required: false, desc: '物料ID' },
          { name: 'materialCode', type: 'String', required: false, desc: '物料编码' },
          { name: 'batchNo', type: 'String', required: false, desc: '批次号' },
          { name: 'locationId', type: 'Long', required: false, desc: '仓位ID' }
        ],
        requestExample: 'GET /wms/inventory/list?warehouseId=1&materialCode=MAT001&pageNum=1&pageSize=10',
        responseExample: `{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "inventoryId": 1,
      "warehouseId": 1,
      "warehouseName": "中心仓",
      "locationId": 5,
      "locationName": "WH001-A01-L01",
      "materialId": 1,
      "materialCode": "MAT001",
      "materialName": "钢板",
      "batchNo": "B20260718",
      "totalQty": 98,
      "availableQty": 50,
      "lockedQty": 48,
      "unit": "kg"
    }
  ],
  "total": 1
}`,
        remark: '分页查询库存余额，totalQty=可用+锁定。'
      },
      {
        method: 'POST',
        path: '/wms/inventory/export',
        summary: '导出库存数据',
        permission: 'wms:inventory:export',
        params: [],
        requestExample: `POST /wms/inventory/export\n# 以 form 表单方式传递查询条件`,
        responseExample: `# 返回 Excel 文件流，浏览器自动下载 库存数据.xlsx`,
        remark: '按查询条件导出库存余额 Excel。'
      },
      {
        method: 'GET',
        path: '/wms/inventory/{inventoryId}',
        summary: '获取库存详情',
        permission: 'wms:inventory:query',
        params: [{ name: 'inventoryId', type: 'Long', required: true, desc: '库存ID', in: 'path' }],
        requestExample: 'GET /wms/inventory/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '根据库存ID查询单条库存余额详情。'
      },
      {
        method: 'DELETE',
        path: '/wms/inventory/{inventoryIds}',
        summary: '删除库存记录',
        permission: 'wms:inventory:remove',
        params: [{ name: 'inventoryIds', type: 'Long[]', required: true, desc: '库存ID数组', in: 'path' }],
        requestExample: 'DELETE /wms/inventory/1,2',
        responseExample: COMMON_RESPONSE.success,
        remark: '删除库存记录（通常用于数据修正）。'
      }
    ]
  },

  {
    name: '库存流水',
    basePath: '/wms/log',
    description: '库存变动流水日志查询，记录所有出入库变动明细。',
    apis: [
      {
        method: 'GET',
        path: '/wms/log/list',
        summary: '查询库存流水列表',
        permission: 'wms:log:list',
        params: [
          ...PAGE_PARAMS,
          { name: 'materialId', type: 'Long', required: false, desc: '物料ID' },
          { name: 'warehouseId', type: 'Long', required: false, desc: '仓库ID' },
          { name: 'businessType', type: 'String', required: false, desc: '业务类型：入库/出库/移库/盘点' },
          { name: 'orderCode', type: 'String', required: false, desc: '关联单据号' }
        ],
        requestExample: 'GET /wms/log/list?materialId=1&pageNum=1&pageSize=10',
        responseExample: COMMON_RESPONSE.table,
        remark: '分页查询库存流水，按时间倒序排列。'
      },
      {
        method: 'POST',
        path: '/wms/log/export',
        summary: '导出库存流水',
        permission: 'wms:log:export',
        params: [],
        requestExample: `POST /wms/log/export\n# form 表单传递查询条件`,
        responseExample: `# 返回 Excel 文件流`,
        remark: '导出库存流水 Excel。'
      },
      {
        method: 'DELETE',
        path: '/wms/log/{logIds}',
        summary: '删除库存流水',
        permission: 'wms:log:remove',
        params: [{ name: 'logIds', type: 'Long[]', required: true, desc: '流水ID数组', in: 'path' }],
        requestExample: 'DELETE /wms/log/1,2',
        responseExample: COMMON_RESPONSE.success,
        remark: '删除流水记录。'
      }
    ]
  },

  {
    name: '盘点管理',
    basePath: '/wms/stocktake',
    description: '盘点单全流程：新增→开始→明细提交→审批→作废，自动生成盘点快照。',
    apis: [
      ...crud({
        basePath: '/wms/stocktake',
        title: '盘点单',
        idField: 'takeId',
        permPrefix: 'wms:stocktake',
        hasExport: false,
        queryParams: [
          { name: 'takeCode', type: 'String', required: false, desc: '盘点单号' },
          { name: 'takeStatus', type: 'String', required: false, desc: '状态：0=草稿 1=进行中 2=已审批 3=已作废' }
        ],
        bodyFields: [
          { name: 'takeId', type: 'Long', required: false, desc: '盘点单ID' },
          { name: 'takeCode', type: 'String', required: false, desc: '盘点单号' },
          { name: 'warehouseId', type: 'Long', required: true, desc: '盘点仓库ID' },
          { name: 'takeType', type: 'String', required: true, desc: '盘点类型：0=全盘 1=抽盘' },
          { name: 'remark', type: 'String', required: false, desc: '备注' }
        ],
        exampleBody: `{
  "takeId": 1,
  "takeCode": "ST20260718001",
  "warehouseId": 1,
  "takeType": "0",
  "remark": "月度全盘"
}`
      }),
      {
        method: 'PUT',
        path: '/wms/stocktake/start/{takeId}',
        summary: '开始盘点',
        permission: 'wms:stocktake:edit',
        params: [{ name: 'takeId', type: 'Long', required: true, desc: '盘点单ID', in: 'path' }],
        requestExample: 'PUT /wms/stocktake/start/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '开始盘点，系统自动生成盘点快照（记录账面数量）。'
      },
      {
        method: 'PUT',
        path: '/wms/stocktake/submit/{takeId}/{detailId}',
        summary: '提交盘点明细',
        permission: 'wms:stocktake:edit',
        params: [
          { name: 'takeId', type: 'Long', required: true, desc: '盘点单ID', in: 'path' },
          { name: 'detailId', type: 'Long', required: true, desc: '明细行ID', in: 'path' },
          { name: 'actualQty', type: 'BigDecimal', required: true, desc: '实际盘点数量', in: 'query' },
          { name: 'diffReason', type: 'String', required: false, desc: '差异原因', in: 'query' }
        ],
        requestExample: 'PUT /wms/stocktake/submit/1/10?actualQty=95&diffReason=正常损耗',
        responseExample: COMMON_RESPONSE.success,
        remark: '提交某明细行盘点结果，系统自动计算差异（实际-账面）。'
      },
      {
        method: 'PUT',
        path: '/wms/stocktake/approve/{takeId}',
        summary: '盘点审批',
        permission: 'wms:stocktake:edit',
        params: [{ name: 'takeId', type: 'Long', required: true, desc: '盘点单ID', in: 'path' }],
        requestExample: 'PUT /wms/stocktake/approve/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '审批通过盘点单，系统自动根据差异调整库存余额。'
      },
      {
        method: 'PUT',
        path: '/wms/stocktake/void/{takeId}',
        summary: '盘点作废',
        permission: 'wms:stocktake:edit',
        params: [{ name: 'takeId', type: 'Long', required: true, desc: '盘点单ID', in: 'path' }],
        requestExample: 'PUT /wms/stocktake/void/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '作废盘点单，已审批的盘点单不可作废。'
      }
    ]
  },

  {
    name: '波次管理',
    basePath: '/wms/wave',
    description: '出库波次管理：合并多个出库单提高拣货效率。',
    apis: [
      ...crud({
        basePath: '/wms/wave',
        title: '波次',
        idField: 'waveId',
        permPrefix: 'wms:wave',
        hasExport: false,
        queryParams: [
          { name: 'waveCode', type: 'String', required: false, desc: '波次号' },
          { name: 'waveStatus', type: 'String', required: false, desc: '状态：0=待生成 1=已生成 2=已释放' }
        ],
        bodyFields: [
          { name: 'waveId', type: 'Long', required: false, desc: '波次ID' },
          { name: 'waveCode', type: 'String', required: false, desc: '波次号' },
          { name: 'warehouseId', type: 'Long', required: true, desc: '仓库ID' },
          { name: 'remark', type: 'String', required: false, desc: '备注' }
        ],
        exampleBody: `{
  "waveId": 1,
  "waveCode": "WAVE20260718001",
  "warehouseId": 1,
  "remark": "上午波次"
}`
      }),
      {
        method: 'PUT',
        path: '/wms/wave/generate/{waveId}',
        summary: '波次生成（关联出库单）',
        permission: 'wms:wave:edit',
        params: [
          { name: 'waveId', type: 'Long', required: true, desc: '波次ID', in: 'path' },
          { name: 'orderIds', type: 'Long[]', required: true, desc: '出库单ID数组', in: 'body' }
        ],
        requestExample: `PUT /wms/wave/generate/1\nContent-Type: application/json\n\n[101, 102, 103]`,
        responseExample: COMMON_RESPONSE.success,
        remark: '将多个出库单合并到波次中，生成拣货任务。'
      },
      {
        method: 'PUT',
        path: '/wms/wave/release/{waveId}',
        summary: '波次释放',
        permission: 'wms:wave:edit',
        params: [{ name: 'waveId', type: 'Long', required: true, desc: '波次ID', in: 'path' }],
        requestExample: 'PUT /wms/wave/release/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '释放波次，将关联的出库单恢复为独立状态。'
      },
      {
        method: 'GET',
        path: '/wms/wave/orders/{waveId}',
        summary: '查询波次关联的出库单',
        permission: 'wms:wave:query',
        params: [{ name: 'waveId', type: 'Long', required: true, desc: '波次ID', in: 'path' }],
        requestExample: 'GET /wms/wave/orders/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '查询波次下所有关联的出库单列表。'
      }
    ]
  },

  {
    name: '统计报表',
    basePath: '/wms/statistics',
    description: '仓库管理统计报表：概览、库存分布、出入库趋势、物料排行、库存预警。',
    apis: [
      {
        method: 'GET',
        path: '/wms/statistics/overview',
        summary: '概览统计',
        permission: 'wms:statistics:list',
        params: [],
        requestExample: 'GET /wms/statistics/overview',
        responseExample: `{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "totalMaterial": 156,
    "totalInventory": 12580,
    "totalWarehouse": 3,
    "todayInbound": 200,
    "todayOutbound": 150,
    "pendingInbound": 5,
    "pendingOutbound": 8
  }
}`,
        remark: '获取仓库管理首页概览统计数据。'
      },
      {
        method: 'GET',
        path: '/wms/statistics/inventoryByWarehouse',
        summary: '按仓库统计库存分布',
        permission: 'wms:statistics:list',
        params: [],
        requestExample: 'GET /wms/statistics/inventoryByWarehouse',
        responseExample: `{
  "code": 200,
  "data": [
    { "warehouseName": "中心仓", "totalQty": 8000, "materialCount": 120 },
    { "warehouseName": "华东仓", "totalQty": 4580, "materialCount": 80 }
  ]
}`,
        remark: '按仓库维度统计库存数量和物料种类。'
      },
      {
        method: 'GET',
        path: '/wms/statistics/inventoryByMaterialType',
        summary: '按物料类型统计库存分布',
        permission: 'wms:statistics:list',
        params: [],
        requestExample: 'GET /wms/statistics/inventoryByMaterialType',
        responseExample: `{
  "code": 200,
  "data": [
    { "materialType": "原材料", "totalQty": 5000, "amount": 250000 },
    { "materialType": "成品", "totalQty": 3000, "amount": 450000 }
  ]
}`,
        remark: '按物料类型维度统计库存分布。'
      },
      {
        method: 'GET',
        path: '/wms/statistics/inboundOutboundTrend',
        summary: '出入库趋势（按日统计）',
        permission: 'wms:statistics:list',
        params: [
          { name: 'startDate', type: 'String', required: false, desc: '开始日期 yyyy-MM-dd' },
          { name: 'endDate', type: 'String', required: false, desc: '结束日期 yyyy-MM-dd' }
        ],
        requestExample: 'GET /wms/statistics/inboundOutboundTrend?startDate=2026-07-01&endDate=2026-07-18',
        responseExample: `{
  "code": 200,
  "data": [
    { "date": "2026-07-18", "inboundQty": 200, "outboundQty": 150 },
    { "date": "2026-07-17", "inboundQty": 180, "outboundQty": 120 }
  ]
}`,
        remark: '按日统计出入库数量趋势，用于折线图展示。'
      },
      {
        method: 'GET',
        path: '/wms/statistics/inboundByType',
        summary: '入库按类型统计',
        permission: 'wms:statistics:list',
        params: [{ name: 'startDate', type: 'String', required: false, desc: '开始日期' }],
        requestExample: 'GET /wms/statistics/inboundByType?startDate=2026-07-01',
        responseExample: `{
  "code": 200,
  "data": [
    { "inboundType": "采购入库", "count": 25, "totalQty": 2000 },
    { "inboundType": "生产入库", "count": 10, "totalQty": 800 }
  ]
}`,
        remark: '按入库类型统计单据数量和入库总量。'
      },
      {
        method: 'GET',
        path: '/wms/statistics/outboundByType',
        summary: '出库按类型统计',
        permission: 'wms:statistics:list',
        params: [{ name: 'startDate', type: 'String', required: false, desc: '开始日期' }],
        requestExample: 'GET /wms/statistics/outboundByType?startDate=2026-07-01',
        responseExample: `{
  "code": 200,
  "data": [
    { "outboundType": "销售出库", "count": 20, "totalQty": 1500 }
  ]
}`,
        remark: '按出库类型统计单据数量和出库总量。'
      },
      {
        method: 'GET',
        path: '/wms/statistics/materialTop',
        summary: '物料库存排行 Top N',
        permission: 'wms:statistics:list',
        params: [{ name: 'count', type: 'Integer', required: false, desc: '返回条数，默认 10' }],
        requestExample: 'GET /wms/statistics/materialTop?count=10',
        responseExample: `{
  "code": 200,
  "data": [
    { "materialCode": "MAT001", "materialName": "钢板", "totalQty": 5000, "amount": 250000 }
  ]
}`,
        remark: '按库存金额/数量降序排列，取前 N 条。'
      },
      {
        method: 'GET',
        path: '/wms/statistics/stockAlert',
        summary: '库存预警（低于安全库存下限）',
        permission: 'wms:statistics:list',
        params: [],
        requestExample: 'GET /wms/statistics/stockAlert',
        responseExample: `{
  "code": 200,
  "data": [
    { "materialCode": "MAT005", "materialName": "螺栓", "currentQty": 50, "safetyStockMin": 100 }
  ]
}`,
        remark: '查询当前库存低于安全库存下限的物料列表。'
      }
    ]
  },

  {
    name: '库存预警',
    basePath: '/wms/stockAlert',
    description: '统一预警入口：库存不足、库存积压、临期预警、过期预警。',
    apis: [
      {
        method: 'GET',
        path: '/wms/stockAlert/list',
        summary: '库存预警列表',
        permission: 'wms:stockAlert:list',
        params: [
          ...PAGE_PARAMS,
          { name: 'alertType', type: 'String', required: false, desc: '预警类型：0=库存不足 1=库存积压 2=临期预警 3=过期预警' },
          { name: 'materialCode', type: 'String', required: false, desc: '物料编码' },
          { name: 'warehouseId', type: 'Long', required: false, desc: '仓库ID' }
        ],
        requestExample: 'GET /wms/stockAlert/list?alertType=0&pageNum=1&pageSize=10',
        responseExample: COMMON_RESPONSE.table,
        remark: '分页查询库存预警，支持按预警类型、物料、仓库筛选。'
      },
      {
        method: 'GET',
        path: '/wms/stockAlert/summary',
        summary: '库存预警汇总',
        permission: 'wms:stockAlert:list',
        params: [],
        requestExample: 'GET /wms/stockAlert/summary',
        responseExample: `{
  "code": 200,
  "data": [
    { "alertType": "库存不足", "count": 15 },
    { "alertType": "库存积压", "count": 8 },
    { "alertType": "临期预警", "count": 3 },
    { "alertType": "过期预警", "count": 1 }
  ]
}`,
        remark: '统计各类型预警数量。'
      },
      {
        method: 'POST',
        path: '/wms/stockAlert/export',
        summary: '导出库存预警数据',
        permission: 'wms:stockAlert:export',
        params: [],
        requestExample: `POST /wms/stockAlert/export\n# form 表单传递查询条件`,
        responseExample: `# 返回 Excel 文件流`,
        remark: '导出库存预警 Excel。'
      },
      {
        method: 'POST',
        path: '/wms/stockAlert/remove',
        summary: '删除库存预警',
        permission: 'wms:stockAlert:remove',
        params: [
          { name: 'alertType', type: 'String', required: true, desc: '预警类型', in: 'body' },
          { name: 'materialId', type: 'Long', required: false, desc: '物料ID', in: 'body' }
        ],
        requestExample: `POST /wms/stockAlert/remove\nContent-Type: application/json\n\n{
  "alertType": "0",
  "materialId": 5
}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '根据预警类型处理底层数据（如清除安全库存设置）。'
      }
    ]
  }
]

// ==================== 设备管理模块 (DMS) ====================

const DMS_MODULES = [
  {
    name: '设备分类管理',
    basePath: '/dms/category',
    description: '设备分类树形结构管理。',
    apis: crud({
      basePath: '/dms/category',
      title: '设备分类',
      idField: 'categoryId',
      permPrefix: 'dms:category',
      hasExport: false,
      queryParams: [
        { name: 'categoryName', type: 'String', required: false, desc: '分类名称' },
        { name: 'status', type: 'String', required: false, desc: '状态：0=正常 1=停用' }
      ],
      bodyFields: [
        { name: 'categoryId', type: 'Long', required: false, desc: '分类ID' },
        { name: 'parentId', type: 'Long', required: false, desc: '父分类ID' },
        { name: 'categoryName', type: 'String', required: true, desc: '分类名称' },
        { name: 'orderNum', type: 'Integer', required: false, desc: '显示排序' },
        { name: 'status', type: 'String', required: false, desc: '状态：0=正常 1=停用' }
      ],
      exampleBody: `{
  "categoryId": 1,
  "parentId": 0,
  "categoryName": "生产设备",
  "orderNum": 1,
  "status": "0"
}`
    })
  },

  {
    name: '设备台账管理',
    basePath: '/dms/equipment',
    description: '设备台账主数据管理，含状态变更履历、维修履历。',
    apis: [
      ...crud({
        basePath: '/dms/equipment',
        title: '设备台账',
        idField: 'equipmentId',
        permPrefix: 'dms:equipment',
        queryParams: [
          { name: 'equipmentCode', type: 'String', required: false, desc: '设备编码' },
          { name: 'equipmentName', type: 'String', required: false, desc: '设备名称' },
          { name: 'categoryId', type: 'Long', required: false, desc: '分类ID' },
          { name: 'equipmentStatus', type: 'String', required: false, desc: '状态：0=运行 1=空闲 2=维修 4=故障 5=报废' }
        ],
        bodyFields: [
          { name: 'equipmentId', type: 'Long', required: false, desc: '设备ID' },
          { name: 'equipmentCode', type: 'String', required: true, desc: '设备编码' },
          { name: 'equipmentName', type: 'String', required: true, desc: '设备名称' },
          { name: 'categoryId', type: 'Long', required: true, desc: '分类ID' },
          { name: 'equipmentStatus', type: 'String', required: false, desc: '状态：0=运行 1=空闲 2=维修 4=故障 5=报废' },
          { name: 'location', type: 'String', required: false, desc: '安装位置' },
          { name: 'specModel', type: 'String', required: false, desc: '规格型号' },
          { name: 'manufacturer', type: 'String', required: false, desc: '生产厂家' },
          { name: 'purchaseDate', type: 'Date', required: false, desc: '购置日期' },
          { name: 'remark', type: 'String', required: false, desc: '备注（状态变更时作为变更原因）' }
        ],
        exampleBody: `{
  "equipmentId": 1,
  "equipmentCode": "EQ001",
  "equipmentName": "数控车床A",
  "categoryId": 1,
  "equipmentStatus": "0",
  "location": "车间A-01工位",
  "specModel": "CK6140",
  "manufacturer": "济南机床厂",
  "purchaseDate": "2025-01-15",
  "remark": ""
}`
      }),
      {
        method: 'GET',
        path: '/dms/equipment/log/list',
        summary: '设备变更履历列表',
        permission: 'dms:equipment:query',
        params: [
          ...PAGE_PARAMS,
          { name: 'equipmentId', type: 'Long', required: false, desc: '设备ID' },
          { name: 'changeType', type: 'String', required: false, desc: '变更类型：0=状态变更 1=位置变更' }
        ],
        requestExample: 'GET /dms/equipment/log/list?equipmentId=1&pageNum=1&pageSize=10',
        responseExample: COMMON_RESPONSE.table,
        remark: '查询设备变更履历，含状态变更、位置变更等历史记录。修改设备状态时系统自动记录日志。'
      },
      {
        method: 'GET',
        path: '/dms/equipment/history/{equipmentId}',
        summary: '设备维修履历（关联工单）',
        permission: 'dms:equipment:query',
        params: [{ name: 'equipmentId', type: 'Long', required: true, desc: '设备ID', in: 'path' }],
        requestExample: 'GET /dms/equipment/history/1',
        responseExample: `{
  "code": 200,
  "data": [
    {
      "orderId": 101,
      "orderCode": "WO20260718001",
      "orderType": "0",
      "orderStatus": "5",
      "faultDesc": "主轴异响",
      "completeTime": "2026-07-15 10:30:00"
    }
  ]
}`,
        remark: '查询设备关联的所有维修工单，即设备维修履历。'
      }
    ]
  },

  {
    name: '工单管理',
    basePath: '/dms/workorder',
    description: '维修工单全生命周期：派工→接单→处理→完工→验收→驳回→撤销。',
    apis: [
      ...crud({
        basePath: '/dms/workorder',
        title: '工单',
        idField: 'orderId',
        permPrefix: 'dms:workorder',
        queryParams: [
          { name: 'orderCode', type: 'String', required: false, desc: '工单号' },
          { name: 'orderType', type: 'String', required: false, desc: '工单类型：0=维修 1=保养 2=巡检 3=安装' },
          { name: 'orderStatus', type: 'String', required: false, desc: '状态：0=待派工 1=已派工 2=已接单 3=处理中 4=已完工 5=已验收 7=已驳回' },
          { name: 'equipmentId', type: 'Long', required: false, desc: '设备ID' },
          { name: 'assigneeId', type: 'Long', required: false, desc: '执行人ID' }
        ],
        bodyFields: [
          { name: 'orderId', type: 'Long', required: false, desc: '工单ID' },
          { name: 'orderCode', type: 'String', required: false, desc: '工单号' },
          { name: 'orderType', type: 'String', required: true, desc: '工单类型：0=维修 1=保养 2=巡检 3=安装' },
          { name: 'equipmentId', type: 'Long', required: true, desc: '设备ID' },
          { name: 'faultDesc', type: 'String', required: false, desc: '故障描述' },
          { name: 'priority', type: 'String', required: false, desc: '优先级：0=低 1=中 2=高 3=紧急' },
          { name: 'planStartTime', type: 'Date', required: false, desc: '计划开始时间' },
          { name: 'planEndTime', type: 'Date', required: false, desc: '计划结束时间' },
          { name: 'remark', type: 'String', required: false, desc: '备注' }
        ],
        exampleBody: `{
  "orderId": 1,
  "orderCode": "WO20260718001",
  "orderType": "0",
  "equipmentId": 1,
  "faultDesc": "主轴异响，需停机检查",
  "priority": "2",
  "planStartTime": "2026-07-18 08:00:00",
  "planEndTime": "2026-07-18 12:00:00",
  "remark": "紧急维修"
}`
      }),
      {
        method: 'PUT',
        path: '/dms/workorder/dispatch',
        summary: '工单派工',
        permission: 'dms:workorder:edit',
        params: [
          { name: 'orderId', type: 'Long', required: true, desc: '工单ID', in: 'body' },
          { name: 'assigneeId', type: 'Long', required: true, desc: '执行人ID', in: 'body' }
        ],
        requestExample: `PUT /dms/workorder/dispatch\nContent-Type: application/json\n\n{
  "orderId": 1,
  "assigneeId": 10
}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '将工单派给指定维修人员，工单状态变为已派工。'
      },
      {
        method: 'PUT',
        path: '/dms/workorder/reassign',
        summary: '工单改派',
        permission: 'dms:workorder:edit',
        params: [
          { name: 'orderId', type: 'Long', required: true, desc: '工单ID', in: 'body' },
          { name: 'assigneeId', type: 'Long', required: true, desc: '新执行人ID', in: 'body' }
        ],
        requestExample: `PUT /dms/workorder/reassign\nContent-Type: application/json\n\n{
  "orderId": 1,
  "assigneeId": 11
}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '将工单改派给其他维修人员。'
      },
      {
        method: 'PUT',
        path: '/dms/workorder/accept/{orderId}',
        summary: '工单接单',
        permission: 'dms:workorder:edit',
        params: [{ name: 'orderId', type: 'Long', required: true, desc: '工单ID', in: 'path' }],
        requestExample: 'PUT /dms/workorder/accept/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '维修人员接单，工单状态变为已接单。'
      },
      {
        method: 'PUT',
        path: '/dms/workorder/process/{orderId}',
        summary: '开始处理',
        permission: 'dms:workorder:edit',
        params: [{ name: 'orderId', type: 'Long', required: true, desc: '工单ID', in: 'path' }],
        requestExample: 'PUT /dms/workorder/process/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '开始处理工单，记录实际开始时间，状态变为处理中。'
      },
      {
        method: 'PUT',
        path: '/dms/workorder/complete',
        summary: '工单完工',
        permission: 'dms:workorder:edit',
        params: [
          { name: 'orderId', type: 'Long', required: true, desc: '工单ID', in: 'body' },
          { name: 'repairDesc', type: 'String', required: false, desc: '维修说明', in: 'body' },
          { name: 'usedParts', type: 'Array', required: false, desc: '使用备件列表', in: 'body' }
        ],
        requestExample: `PUT /dms/workorder/complete\nContent-Type: application/json\n\n{
  "orderId": 1,
  "repairDesc": "更换主轴轴承，异响消除",
  "usedParts": [
    { "partId": 5, "usedQty": 2 }
  ]
}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '完工提交，记录维修说明和备件消耗，状态变为已完工待验收。'
      },
      {
        method: 'PUT',
        path: '/dms/workorder/verify',
        summary: '验收通过',
        permission: 'dms:workorder:edit',
        params: [
          { name: 'orderId', type: 'Long', required: true, desc: '工单ID', in: 'body' },
          { name: 'verifyRemark', type: 'String', required: false, desc: '验收意见', in: 'body' }
        ],
        requestExample: `PUT /dms/workorder/verify\nContent-Type: application/json\n\n{
  "orderId": 1,
  "verifyRemark": "维修质量合格"
}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '验收通过，工单状态变为已验收（终态）。'
      },
      {
        method: 'PUT',
        path: '/dms/workorder/reject',
        summary: '驳回重做',
        permission: 'dms:workorder:edit',
        params: [
          { name: 'orderId', type: 'Long', required: true, desc: '工单ID', in: 'body' },
          { name: 'rejectReason', type: 'String', required: true, desc: '驳回原因', in: 'body' }
        ],
        requestExample: `PUT /dms/workorder/reject\nContent-Type: application/json\n\n{
  "orderId": 1,
  "rejectReason": "故障未完全排除"
}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '验收不通过驳回重做，工单状态变为已驳回，维修人员需重新处理。'
      },
      {
        method: 'PUT',
        path: '/dms/workorder/cancel/{orderId}',
        summary: '工单撤销',
        permission: 'dms:workorder:edit',
        params: [{ name: 'orderId', type: 'Long', required: true, desc: '工单ID', in: 'path' }],
        requestExample: 'PUT /dms/workorder/cancel/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '撤销工单，已验收的工单不可撤销。'
      },
      {
        method: 'GET',
        path: '/dms/workorder/log/{orderId}',
        summary: '查询工单操作历史',
        permission: 'dms:workorder:query',
        params: [{ name: 'orderId', type: 'Long', required: true, desc: '工单ID', in: 'path' }],
        requestExample: 'GET /dms/workorder/log/1',
        responseExample: `{
  "code": 200,
  "data": [
    {
      "logId": 1,
      "operation": "派工",
      "operatorName": "管理员",
      "operateTime": "2026-07-18 08:30:00",
      "content": "派工给张三"
    }
  ]
}`,
        remark: '查询工单全生命周期操作日志。'
      }
    ]
  },

  {
    name: 'PM维护计划',
    basePath: '/dms/pmplan',
    description: '预防性维护计划管理，支持自动/手动生成工单和日历视图。',
    apis: [
      ...crud({
        basePath: '/dms/pmplan',
        title: 'PM维护计划',
        idField: 'planId',
        permPrefix: 'dms:pmplan',
        hasExport: false,
        queryParams: [
          { name: 'planName', type: 'String', required: false, desc: '计划名称' },
          { name: 'equipmentId', type: 'Long', required: false, desc: '设备ID' },
          { name: 'status', type: 'String', required: false, desc: '状态：0=启用 1=停用' }
        ],
        bodyFields: [
          { name: 'planId', type: 'Long', required: false, desc: '计划ID' },
          { name: 'planName', type: 'String', required: true, desc: '计划名称' },
          { name: 'equipmentId', type: 'Long', required: true, desc: '设备ID' },
          { name: 'cycleType', type: 'String', required: true, desc: '周期类型：0=日 1=周 2=月 3=季 4=年' },
          { name: 'cycleValue', type: 'Integer', required: true, desc: '周期值' },
          { name: 'nextExecuteDate', type: 'Date', required: false, desc: '下次执行日期' },
          { name: 'maintenanceContent', type: 'String', required: false, desc: '维护内容' },
          { name: 'status', type: 'String', required: false, desc: '状态：0=启用 1=停用' }
        ],
        exampleBody: `{
  "planId": 1,
  "planName": "数控车床月度保养",
  "equipmentId": 1,
  "cycleType": "2",
  "cycleValue": 1,
  "nextExecuteDate": "2026-08-01",
  "maintenanceContent": "润滑、紧固、清洁",
  "status": "0"
}`
      }),
      {
        method: 'POST',
        path: '/dms/pmplan/generate/{planId}',
        summary: '根据PM计划生成工单',
        permission: 'dms:pmplan:edit',
        params: [{ name: 'planId', type: 'Long', required: true, desc: '计划ID', in: 'path' }],
        requestExample: 'POST /dms/pmplan/generate/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '手动触发，根据PM计划立即生成一张维护工单。'
      },
      {
        method: 'POST',
        path: '/dms/pmplan/autoGenerate',
        summary: '手动触发PM自动生成',
        permission: 'dms:pmplan:edit',
        params: [],
        requestExample: 'POST /dms/pmplan/autoGenerate',
        responseExample: `{
  "code": 200,
  "msg": "共自动生成 3 个工单"
}`,
        remark: '扫描所有到期PM计划并批量生成工单，用于测试或补执行（定时任务也会自动调用）。'
      },
      {
        method: 'GET',
        path: '/dms/pmplan/calendar',
        summary: '日历视图数据',
        permission: 'dms:pmplan:list',
        params: [{ name: 'month', type: 'String', required: false, desc: '月份 yyyy-MM，默认当月' }],
        requestExample: 'GET /dms/pmplan/calendar?month=2026-07',
        responseExample: `{
  "code": 200,
  "data": [
    {
      "planId": 1,
      "planName": "数控车床月度保养",
      "executeDate": "2026-07-01",
      "equipmentName": "数控车床A"
    }
  ]
}`,
        remark: '返回指定月份的PM计划执行日历数据，用于前端日历组件展示。'
      }
    ]
  },

  {
    name: '巡检路线',
    basePath: '/dms/inspection/route',
    description: '巡检路线定义管理，含巡检点和顺序。',
    apis: crud({
      basePath: '/dms/inspection/route',
      title: '巡检路线',
      idField: 'routeId',
      permPrefix: 'dms:inspection:route',
      hasExport: false,
      queryParams: [
        { name: 'routeName', type: 'String', required: false, desc: '路线名称' },
        { name: 'status', type: 'String', required: false, desc: '状态：0=启用 1=停用' }
      ],
      bodyFields: [
        { name: 'routeId', type: 'Long', required: false, desc: '路线ID' },
        { name: 'routeName', type: 'String', required: true, desc: '路线名称' },
        { name: 'routeCode', type: 'String', required: false, desc: '路线编码' },
        { name: 'cycleType', type: 'String', required: false, desc: '巡检周期类型' },
        { name: 'points', type: 'Array', required: false, desc: '巡检点列表（设备+检查项）' },
        { name: 'status', type: 'String', required: false, desc: '状态：0=启用 1=停用' }
      ],
      exampleBody: `{
  "routeId": 1,
  "routeName": "车间A日巡检",
  "routeCode": "ROUTE001",
  "cycleType": "1",
  "points": [
    { "equipmentId": 1, "checkItem": "温度", "standard": "≤60℃" }
  ],
  "status": "0"
}`
    })
  },

  {
    name: '点检任务',
    basePath: '/dms/inspection/task',
    description: '点检任务管理：开始执行→完成（异常项自动转工单）。',
    apis: [
      ...crud({
        basePath: '/dms/inspection/task',
        title: '点检任务',
        idField: 'taskId',
        permPrefix: 'dms:inspection:task',
        hasExport: false,
        queryParams: [
          { name: 'taskCode', type: 'String', required: false, desc: '任务编号' },
          { name: 'routeId', type: 'Long', required: false, desc: '路线ID' },
          { name: 'taskStatus', type: 'String', required: false, desc: '状态：0=待执行 1=进行中 2=已完成' }
        ],
        bodyFields: [
          { name: 'taskId', type: 'Long', required: false, desc: '任务ID' },
          { name: 'taskCode', type: 'String', required: false, desc: '任务编号' },
          { name: 'routeId', type: 'Long', required: true, desc: '巡检路线ID' },
          { name: 'planExecuteDate', type: 'Date', required: false, desc: '计划执行日期' },
          { name: 'assigneeId', type: 'Long', required: false, desc: '执行人ID' }
        ],
        exampleBody: `{
  "taskId": 1,
  "taskCode": "TASK20260718001",
  "routeId": 1,
  "planExecuteDate": "2026-07-18",
  "assigneeId": 10
}`
      }),
      {
        method: 'PUT',
        path: '/dms/inspection/task/start/{taskId}',
        summary: '开始执行点检任务',
        permission: 'dms:inspection:task:edit',
        params: [{ name: 'taskId', type: 'Long', required: true, desc: '任务ID', in: 'path' }],
        requestExample: 'PUT /dms/inspection/task/start/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '开始执行点检任务，记录实际开始时间，状态变为进行中。'
      },
      {
        method: 'PUT',
        path: '/dms/inspection/task/complete',
        summary: '完成点检任务',
        permission: 'dms:inspection:task:edit',
        params: [
          { name: 'taskId', type: 'Long', required: true, desc: '任务ID', in: 'body' },
          { name: 'results', type: 'Array', required: true, desc: '检查结果列表', in: 'body' }
        ],
        requestExample: `PUT /dms/inspection/task/complete\nContent-Type: application/json\n\n{
  "taskId": 1,
  "results": [
    {
      "pointId": 10,
      "checkItem": "温度",
      "actualValue": "65℃",
      "isNormal": "1",
      "remark": "温度偏高"
    },
    {
      "pointId": 11,
      "checkItem": "振动",
      "actualValue": "正常",
      "isNormal": "0"
    }
  ]
}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '提交点检结果，异常项（isNormal=1）自动生成维修工单。'
      }
    ]
  },

  {
    name: '备件管理',
    basePath: '/dms/sparepart',
    description: '备件主数据、出入库、库存预警、流水报表一体化管理。',
    apis: [
      ...crud({
        basePath: '/dms/sparepart',
        title: '备件',
        idField: 'partId',
        permPrefix: 'dms:sparepart',
        queryParams: [
          { name: 'partCode', type: 'String', required: false, desc: '备件编码' },
          { name: 'partName', type: 'String', required: false, desc: '备件名称' },
          { name: 'categoryId', type: 'Long', required: false, desc: '分类ID' }
        ],
        bodyFields: [
          { name: 'partId', type: 'Long', required: false, desc: '备件ID' },
          { name: 'partCode', type: 'String', required: true, desc: '备件编码' },
          { name: 'partName', type: 'String', required: true, desc: '备件名称' },
          { name: 'specModel', type: 'String', required: false, desc: '规格型号' },
          { name: 'unit', type: 'String', required: false, desc: '计量单位' },
          { name: 'currentStock', type: 'BigDecimal', required: false, desc: '当前库存' },
          { name: 'safetyStock', type: 'BigDecimal', required: false, desc: '安全库存' },
          { name: 'unitPrice', type: 'BigDecimal', required: false, desc: '单价' }
        ],
        exampleBody: `{
  "partId": 1,
  "partCode": "SP001",
  "partName": "主轴轴承",
  "specModel": "7008C",
  "unit": "个",
  "currentStock": 50,
  "safetyStock": 20,
  "unitPrice": 350.00
}`
      }),
      {
        method: 'GET',
        path: '/dms/sparepart/genCode',
        summary: '自动生成备件编号',
        permission: 'dms:sparepart:add',
        params: [],
        requestExample: 'GET /dms/sparepart/genCode',
        responseExample: `{
  "code": 200,
  "msg": "生成成功",
  "data": "SP001"
}`,
        remark: '自动生成下一个备件编号。'
      },
      {
        method: 'GET',
        path: '/dms/sparepart/genInCode',
        summary: '自动生成入库单号',
        permission: 'dms:partin:add',
        params: [],
        requestExample: 'GET /dms/sparepart/genInCode',
        responseExample: `{
  "code": 200,
  "msg": "生成成功",
  "data": "PIN20260718001"
}`,
        remark: '自动生成备件入库单号。'
      },
      {
        method: 'GET',
        path: '/dms/sparepart/genOutCode',
        summary: '自动生成出库单号',
        permission: 'dms:partout:add',
        params: [],
        requestExample: 'GET /dms/sparepart/genOutCode',
        responseExample: `{
  "code": 200,
  "msg": "生成成功",
  "data": "POUT20260718001"
}`,
        remark: '自动生成备件出库单号。'
      },
      {
        method: 'POST',
        path: '/dms/sparepart/stockMove',
        summary: '备件出入库操作',
        permission: 'dms:sparepart:edit',
        params: [
          { name: 'partId', type: 'Long', required: true, desc: '备件ID', in: 'body' },
          { name: 'moveType', type: 'String', required: true, desc: '类型：0=入库 1=出库', in: 'body' },
          { name: 'moveQty', type: 'BigDecimal', required: true, desc: '数量', in: 'body' },
          { name: 'operateDate', type: 'Date', required: false, desc: '操作日期', in: 'body' },
          { name: 'remark', type: 'String', required: false, desc: '备注', in: 'body' }
        ],
        requestExample: `POST /dms/sparepart/stockMove\nContent-Type: application/json\n\n{
  "partId": 1,
  "moveType": "1",
  "moveQty": 5,
  "operateDate": "2026-07-18",
  "remark": "维修领用"
}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '备件出入库统一入口，入库 moveType=0，出库 moveType=1。'
      },
      {
        method: 'GET',
        path: '/dms/sparepart/record/list',
        summary: '出入库记录列表',
        permission: 'dms:sparepart:query',
        params: [
          ...PAGE_PARAMS,
          { name: 'partId', type: 'Long', required: false, desc: '备件ID' },
          { name: 'moveType', type: 'String', required: false, desc: '类型：0=入库 1=出库' }
        ],
        requestExample: 'GET /dms/sparepart/record/list?partId=1&pageNum=1&pageSize=10',
        responseExample: COMMON_RESPONSE.table,
        remark: '分页查询备件出入库流水记录。'
      },
      {
        method: 'DELETE',
        path: '/dms/sparepart/record/{recordIds}',
        summary: '删除出入库记录',
        permission: 'dms:partstock:remove',
        params: [{ name: 'recordIds', type: 'Long[]', required: true, desc: '记录ID数组', in: 'path' }],
        requestExample: 'DELETE /dms/sparepart/record/1,2',
        responseExample: COMMON_RESPONSE.success,
        remark: '删除备件出入库记录。'
      },
      {
        method: 'POST',
        path: '/dms/sparepart/partin/export',
        summary: '入库记录导出',
        permission: 'dms:partin:export',
        params: [],
        requestExample: `POST /dms/sparepart/partin/export\n# form 表单传递筛选条件`,
        responseExample: `# 返回 Excel 文件流`,
        remark: '导出备件入库记录 Excel（隐藏出库相关列）。'
      },
      {
        method: 'POST',
        path: '/dms/sparepart/partout/export',
        summary: '出库记录导出',
        permission: 'dms:partout:export',
        params: [],
        requestExample: `POST /dms/sparepart/partout/export`,
        responseExample: `# 返回 Excel 文件流`,
        remark: '导出备件出库记录 Excel（隐藏入库相关列）。'
      },
      {
        method: 'POST',
        path: '/dms/sparepart/partstock/export',
        summary: '库存流水报表导出',
        permission: 'dms:partstock:export',
        params: [],
        requestExample: `POST /dms/sparepart/partstock/export`,
        responseExample: `# 返回 Excel 文件流`,
        remark: '导出备件库存流水报表 Excel。'
      },
      {
        method: 'GET',
        path: '/dms/sparepart/alert/list',
        summary: '库存预警列表',
        permission: 'dms:partalert:query',
        params: [
          ...PAGE_PARAMS,
          { name: 'partCode', type: 'String', required: false, desc: '备件编码' },
          { name: 'partName', type: 'String', required: false, desc: '备件名称' }
        ],
        requestExample: 'GET /dms/sparepart/alert/list?pageNum=1&pageSize=10',
        responseExample: COMMON_RESPONSE.table,
        remark: '查询当前库存低于安全库存的备件列表。'
      },
      {
        method: 'POST',
        path: '/dms/sparepart/alert/export',
        summary: '库存预警导出',
        permission: 'dms:partalert:export',
        params: [
          { name: 'partCode', type: 'String', required: false, desc: '备件编码' },
          { name: 'partName', type: 'String', required: false, desc: '备件名称' }
        ],
        requestExample: 'POST /dms/sparepart/alert/export\n# form 表单传递筛选条件',
        responseExample: `# 返回 Excel 文件流`,
        remark: '导出库存预警备件列表 Excel。'
      },
      {
        method: 'DELETE',
        path: '/dms/sparepart/alert/{partId}',
        summary: '删除库存预警',
        permission: 'dms:partalert:remove',
        params: [{ name: 'partId', type: 'Long', required: true, desc: '备件ID', in: 'path' }],
        requestExample: 'DELETE /dms/sparepart/alert/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '清除备件的安全库存设置（即取消该备件的预警）。'
      }
    ]
  },

  {
    name: '采集配置',
    basePath: '/dms/data/config',
    description: '设备数据采集配置管理（IoT/SCADA参数采集规则）。',
    apis: crud({
      basePath: '/dms/data/config',
      title: '采集配置',
      idField: 'configId',
      permPrefix: 'dms:data:config',
      hasExport: false,
      queryParams: [
        { name: 'equipmentId', type: 'Long', required: false, desc: '设备ID' },
        { name: 'configName', type: 'String', required: false, desc: '配置名称' }
      ],
      bodyFields: [
        { name: 'configId', type: 'Long', required: false, desc: '配置ID' },
        { name: 'equipmentId', type: 'Long', required: true, desc: '设备ID' },
        { name: 'configName', type: 'String', required: true, desc: '配置名称' },
        { name: 'paramCode', type: 'String', required: false, desc: '参数编码' },
        { name: 'paramName', type: 'String', required: false, desc: '参数名称' },
        { name: 'collectInterval', type: 'Integer', required: false, desc: '采集间隔（秒）' },
        { name: 'status', type: 'String', required: false, desc: '状态：0=启用 1=停用' }
      ],
      exampleBody: `{
  "configId": 1,
  "equipmentId": 1,
  "configName": "主轴温度采集",
  "paramCode": "TEMP01",
  "paramName": "主轴温度",
  "collectInterval": 60,
  "status": "0"
}`
    })
  },

  {
    name: '数据记录',
    basePath: '/dms/data/record',
    description: '设备采集数据记录查询与管理。',
    apis: crud({
      basePath: '/dms/data/record',
      title: '数据记录',
      idField: 'recordId',
      permPrefix: 'dms:data:record',
      hasExport: false,
      queryParams: [
        { name: 'equipmentId', type: 'Long', required: false, desc: '设备ID' },
        { name: 'configId', type: 'Long', required: false, desc: '采集配置ID' },
        { name: 'startTime', type: 'String', required: false, desc: '开始时间' },
        { name: 'endTime', type: 'String', required: false, desc: '结束时间' }
      ],
      bodyFields: [
        { name: 'recordId', type: 'Long', required: false, desc: '记录ID' },
        { name: 'equipmentId', type: 'Long', required: true, desc: '设备ID' },
        { name: 'configId', type: 'Long', required: true, desc: '采集配置ID' },
        { name: 'paramValue', type: 'String', required: true, desc: '参数值' },
        { name: 'collectTime', type: 'Date', required: true, desc: '采集时间' }
      ],
      exampleBody: `{
  "recordId": 1,
  "equipmentId": 1,
  "configId": 1,
  "paramValue": "58.5",
  "collectTime": "2026-07-18 10:00:00"
}`
    })
  },

  {
    name: '看板统计',
    basePath: '/dms/dashboard',
    description: '设备管理驾驶舱统计数据：设备统计、工单统计、备件预警、PM计划统计。',
    apis: [
      {
        method: 'GET',
        path: '/dms/dashboard/stats',
        summary: '看板统计数据',
        permission: 'dms:dashboard:list',
        params: [],
        requestExample: 'GET /dms/dashboard/stats',
        responseExample: `{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "equipment": {
      "total": 50,
      "inUse": 35,
      "idle": 8,
      "repair": 4,
      "fault": 2,
      "scrap": 1
    },
    "workOrder": {
      "total": 120,
      "pending": 5,
      "processing": 8,
      "completed": 100,
      "verified": 7
    },
    "lowStockParts": [
      {
        "partCode": "SP005",
        "partName": "密封圈",
        "currentStock": 10,
        "safetyStock": 20,
        "unit": "个"
      }
    ],
    "lowStockCount": 1,
    "pmPlan": {
      "total": 15,
      "active": 12
    }
  }
}`,
        remark: '一次性返回看板所需的全部统计数据：设备状态分布、工单状态分布、备件库存预警列表、PM计划统计。'
      }
    ]
  },

  {
    name: 'AI配置管理',
    basePath: '/dms/ai/config',
    description: 'AI功能开关与模型参数配置（读写 sys_config 表 dms.ai.* 系列）。',
    apis: [
      {
        method: 'GET',
        path: '/dms/ai/config',
        summary: '获取AI配置',
        permission: 'dms:ai:config',
        params: [],
        requestExample: 'GET /dms/ai/config',
        responseExample: `{
  "code": 200,
  "data": {
    "enabled": true,
    "apiUrl": "https://api.example.com/v1",
    "apiKey": "sk-xxx",
    "modelName": "gpt-4",
    "repairAssistant": true,
    "dispatchRecommend": true,
    "knowledgeQa": true,
    "monthlyLimit": "10000"
  }
}`,
        remark: '获取AI功能配置，包括开关、模型地址、密钥、各子功能开关和月度调用限额。'
      },
      {
        method: 'PUT',
        path: '/dms/ai/config',
        summary: '更新AI配置',
        permission: 'dms:ai:config',
        params: [
          { name: 'enabled', type: 'Boolean', required: false, desc: 'AI总开关', in: 'body' },
          { name: 'apiUrl', type: 'String', required: false, desc: '模型API地址', in: 'body' },
          { name: 'apiKey', type: 'String', required: false, desc: 'API密钥', in: 'body' },
          { name: 'modelName', type: 'String', required: false, desc: '模型名称', in: 'body' },
          { name: 'repairAssistant', type: 'Boolean', required: false, desc: '智能报修助手开关', in: 'body' },
          { name: 'dispatchRecommend', type: 'Boolean', required: false, desc: '智能派工推荐开关', in: 'body' },
          { name: 'knowledgeQa', type: 'Boolean', required: false, desc: '维修知识问答开关', in: 'body' },
          { name: 'monthlyLimit', type: 'String', required: false, desc: '月度调用限额', in: 'body' }
        ],
        requestExample: `PUT /dms/ai/config\nContent-Type: application/json\n\n{
  "enabled": true,
  "apiUrl": "https://api.example.com/v1",
  "apiKey": "sk-newkey",
  "modelName": "gpt-4",
  "repairAssistant": true,
  "dispatchRecommend": true,
  "knowledgeQa": false,
  "monthlyLimit": "20000"
}`,
        responseExample: `{
  "code": 200,
  "msg": "配置保存成功"
}`,
        remark: '更新AI配置，写入 sys_config 表。'
      }
    ]
  },

  {
    name: 'AI智能服务',
    basePath: '/dms/ai',
    description: 'AI智能报修助手、派工推荐、维修知识问答，及FAQ知识库管理。',
    apis: [
      {
        method: 'GET',
        path: '/dms/ai/faq/list',
        summary: '查询维修FAQ列表',
        permission: 'dms:ai:faq:list',
        params: [
          ...PAGE_PARAMS,
          { name: 'question', type: 'String', required: false, desc: '问题关键词' }
        ],
        requestExample: 'GET /dms/ai/faq/list?pageNum=1&pageSize=10',
        responseExample: COMMON_RESPONSE.table,
        remark: '分页查询维修FAQ知识库。'
      },
      {
        method: 'GET',
        path: '/dms/ai/faq/{faqId}',
        summary: '获取FAQ详情',
        permission: 'dms:ai:faq:query',
        params: [{ name: 'faqId', type: 'Long', required: true, desc: 'FAQ ID', in: 'path' }],
        requestExample: 'GET /dms/ai/faq/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '根据ID查询FAQ详情。'
      },
      {
        method: 'POST',
        path: '/dms/ai/faq',
        summary: '新增FAQ',
        permission: 'dms:ai:faq:add',
        params: [
          { name: 'question', type: 'String', required: true, desc: '问题', in: 'body' },
          { name: 'answer', type: 'String', required: true, desc: '答案', in: 'body' },
          { name: 'category', type: 'String', required: false, desc: '分类', in: 'body' }
        ],
        requestExample: `POST /dms/ai/faq\nContent-Type: application/json\n\n{
  "question": "主轴异响如何处理？",
  "answer": "1.停机检查 2.检查轴承 3.更换润滑",
  "category": "机械故障"
}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '新增维修FAQ知识条目。'
      },
      {
        method: 'PUT',
        path: '/dms/ai/faq',
        summary: '修改FAQ',
        permission: 'dms:ai:faq:edit',
        params: [],
        requestExample: `PUT /dms/ai/faq\nContent-Type: application/json\n\n{
  "faqId": 1,
  "question": "主轴异响如何处理？",
  "answer": "更新后的答案内容"
}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '修改FAQ知识条目。'
      },
      {
        method: 'DELETE',
        path: '/dms/ai/faq/{faqIds}',
        summary: '删除FAQ',
        permission: 'dms:ai:faq:remove',
        params: [{ name: 'faqIds', type: 'Long[]', required: true, desc: 'FAQ ID数组', in: 'path' }],
        requestExample: 'DELETE /dms/ai/faq/1,2',
        responseExample: COMMON_RESPONSE.success,
        remark: '批量删除FAQ知识条目。'
      },
      {
        method: 'POST',
        path: '/dms/ai/repairAssistant',
        summary: 'AI智能报修助手',
        permission: '无需权限校验（登录即可）',
        params: [{ name: 'input', type: 'String', required: true, desc: '故障描述', in: 'body' }],
        requestExample: `POST /dms/ai/repairAssistant\nContent-Type: application/json\n\n{
  "input": "数控车床主轴有异响，温度偏高"
}`,
        responseExample: `{
  "code": 200,
  "data": {
    "possibleCauses": ["主轴轴承磨损", "润滑不足", "皮带松动"],
    "suggestions": ["检查主轴轴承状态", "补充润滑油", "调整皮带张力"],
    "severity": "中"
  }
}`,
        remark: 'AI根据故障描述分析可能原因并给出维修建议。'
      },
      {
        method: 'GET',
        path: '/dms/ai/dispatchRecommend/{orderId}',
        summary: 'AI智能派工推荐',
        permission: '无需权限校验（登录即可）',
        params: [{ name: 'orderId', type: 'Long', required: true, desc: '工单ID', in: 'path' }],
        requestExample: 'GET /dms/ai/dispatchRecommend/1',
        responseExample: `{
  "code": 200,
  "data": [
    { "userId": 10, "userName": "张三", "score": 95, "reason": "擅长机械维修，当前空闲" },
    { "userId": 11, "userName": "李四", "score": 80, "reason": "有相关经验" }
  ]
}`,
        remark: 'AI根据工单的设备、故障类型和维修人员技能/负荷推荐最佳派工人选。'
      },
      {
        method: 'POST',
        path: '/dms/ai/knowledgeQA',
        summary: 'AI维修知识问答',
        permission: '无需权限校验（登录即可）',
        params: [{ name: 'question', type: 'String', required: true, desc: '问题', in: 'body' }],
        requestExample: `POST /dms/ai/knowledgeQA\nContent-Type: application/json\n\n{
  "question": "液压系统漏油怎么排查？"
}`,
        responseExample: `{
  "code": 200,
  "data": {
    "answer": "液压系统漏油排查步骤：1.检查管接头...",
    "references": [
      { "faqId": 5, "question": "液压系统漏油排查" }
    ]
  }
}`,
        remark: 'AI基于维修FAQ知识库进行问答，返回答案和相关参考条目。'
      }
    ]
  }
]

// ==================== 营销管理模块 (MK) ====================

const MK_MODULES = [
  {
    name: '客户管理',
    basePath: '/mk/customer',
    description: '企业客户全生命周期管理：公海机制、分配/转移、状态管理、批量操作、导入导出。',
    apis: [
      ...crud({
        basePath: '/mk/customer',
        title: '企业客户',
        idField: 'customerId',
        permPrefix: 'marketing:customer',
        queryParams: [
          { name: 'customerName', type: 'String', required: false, desc: '客户名称，模糊匹配' },
          { name: 'customerStatus', type: 'String', required: false, desc: '状态：0=潜在 1=活跃 2=流失 3=黑名单' },
          { name: 'industry', type: 'String', required: false, desc: '行业' },
          { name: 'customerLevel', type: 'String', required: false, desc: '客户等级：0=普通 1=重要 2=战略' }
        ],
        bodyFields: [
          { name: 'customerId', type: 'Long', required: false, desc: '客户ID（修改时必填）' },
          { name: 'customerName', type: 'String', required: true, desc: '客户名称' },
          { name: 'customerCode', type: 'String', required: false, desc: '客户编码（系统自动生成）' },
          { name: 'industry', type: 'String', required: false, desc: '所属行业' },
          { name: 'customerLevel', type: 'String', required: false, desc: '客户等级：0=普通 1=重要 2=战略' },
          { name: 'customerStatus', type: 'String', required: false, desc: '状态：0=潜在 1=活跃 2=流失 3=黑名单' },
          { name: 'contactPhone', type: 'String', required: false, desc: '联系电话' },
          { name: 'address', type: 'String', required: false, desc: '地址' },
          { name: 'remark', type: 'String', required: false, desc: '备注' }
        ],
        exampleBody: `{\n  "customerId": 1,\n  "customerName": "XX科技有限公司",\n  "customerCode": "CUS20260001",\n  "industry": "信息技术",\n  "customerLevel": "1",\n  "customerStatus": "1",\n  "contactPhone": "13800138000",\n  "address": "北京市海淀区XX路1号"\n}`
      }),
      {
        method: 'GET',
        path: '/mk/customer/publicPool',
        summary: '公海客户列表',
        permission: 'marketing:customer:pool',
        params: [...PAGE_PARAMS, { name: 'customerName', type: 'String', required: false, desc: '客户名称' }],
        requestExample: 'GET /mk/customer/publicPool?pageNum=1&pageSize=10',
        responseExample: COMMON_RESPONSE.table,
        remark: '查询公海（无人负责）的客户列表，支持领取操作。'
      },
      {
        method: 'POST',
        path: '/mk/customer/importData',
        summary: '导入客户数据',
        permission: 'marketing:customer:import',
        params: [{ name: 'file', type: 'MultipartFile', required: true, desc: 'Excel文件', in: 'form' }, { name: 'updateSupport', type: 'Boolean', required: false, desc: '是否更新已存在数据', in: 'form' }],
        requestExample: 'POST /mk/customer/importData\nContent-Type: multipart/form-data\n\nfile: 客户数据.xlsx\nupdateSupport: true',
        responseExample: COMMON_RESPONSE.success,
        remark: '通过Excel批量导入客户数据。'
      },
      {
        method: 'POST',
        path: '/mk/customer/importTemplate',
        summary: '下载导入模板',
        permission: 'marketing:customer:import',
        params: [],
        requestExample: 'POST /mk/customer/importTemplate',
        responseExample: `# 返回 Excel 文件流`,
        remark: '下载客户导入Excel模板，包含表头和数据校验规则。'
      },
      {
        method: 'PUT',
        path: '/mk/customer/status/{customerId}',
        summary: '变更客户状态',
        permission: 'marketing:customer:edit',
        params: [
          { name: 'customerId', type: 'Long', required: true, desc: '客户ID', in: 'path' },
          { name: 'customerStatus', type: 'String', required: true, desc: '状态：0=潜在 1=活跃 2=流失 3=黑名单', in: 'query' }
        ],
        requestExample: 'PUT /mk/customer/status/1?customerStatus=2',
        responseExample: COMMON_RESPONSE.success,
        remark: '变更单个客户的状态。'
      },
      {
        method: 'PUT',
        path: '/mk/customer/batchStatus',
        summary: '批量变更客户状态',
        permission: 'marketing:customer:edit',
        params: [
          { name: 'customerIds', type: 'Long[]', required: true, desc: '客户ID数组', in: 'body' },
          { name: 'customerStatus', type: 'String', required: true, desc: '目标状态', in: 'body' }
        ],
        requestExample: `PUT /mk/customer/batchStatus\nContent-Type: application/json\n\n{\n  "customerIds": [1, 2, 3],\n  "customerStatus": "1"\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '批量变更多个客户的状态。'
      },
      {
        method: 'PUT',
        path: '/mk/customer/assign/{customerId}',
        summary: '分配客户负责人',
        permission: 'marketing:customer:assign',
        params: [
          { name: 'customerId', type: 'Long', required: true, desc: '客户ID', in: 'path' },
          { name: 'userId', type: 'Long', required: true, desc: '负责人用户ID', in: 'body' },
          { name: 'deptId', type: 'Long', required: false, desc: '部门ID', in: 'body' }
        ],
        requestExample: `PUT /mk/customer/assign/1\nContent-Type: application/json\n\n{\n  "userId": 10,\n  "deptId": 100\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '将客户分配给指定负责人。'
      },
      {
        method: 'PUT',
        path: '/mk/customer/batchAssign',
        summary: '批量分配客户',
        permission: 'marketing:customer:assign',
        params: [
          { name: 'customerIds', type: 'Long[]', required: true, desc: '客户ID数组', in: 'body' },
          { name: 'userId', type: 'Long', required: true, desc: '负责人用户ID', in: 'body' },
          { name: 'deptId', type: 'Long', required: false, desc: '部门ID', in: 'body' }
        ],
        requestExample: `PUT /mk/customer/batchAssign\nContent-Type: application/json\n\n{\n  "customerIds": [1, 2, 3],\n  "userId": 10,\n  "deptId": 100\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '批量将多个客户分配给同一负责人。'
      },
      {
        method: 'PUT',
        path: '/mk/customer/release/{customerId}',
        summary: '释放客户到公海',
        permission: 'marketing:customer:edit',
        params: [{ name: 'customerId', type: 'Long', required: true, desc: '客户ID', in: 'path' }],
        requestExample: 'PUT /mk/customer/release/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '将客户释放回公海，清除负责人。'
      },
      {
        method: 'PUT',
        path: '/mk/customer/claim/{customerId}',
        summary: '从公海领取客户',
        permission: 'marketing:customer:claim',
        params: [{ name: 'customerId', type: 'Long', required: true, desc: '客户ID', in: 'path' }],
        requestExample: 'PUT /mk/customer/claim/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '从公海领取客户，自动设置当前登录用户为负责人。'
      }
    ]
  },

  {
    name: '线索管理',
    basePath: '/mk/lead',
    description: '线索全生命周期：领取→审批→分配→转化→公海机制，含批量操作、查重、导入。',
    apis: [
      ...crud({
        basePath: '/mk/lead',
        title: '线索',
        idField: 'leadId',
        permPrefix: 'marketing:lead',
        queryParams: [
          { name: 'companyName', type: 'String', required: false, desc: '公司名称' },
          { name: 'contactPhone', type: 'String', required: false, desc: '联系电话' },
          { name: 'leadStatus', type: 'String', required: false, desc: '状态：0=新建 1=跟进中 2=已转化 3=无效 4=公海' },
          { name: 'leadSource', type: 'String', required: false, desc: '来源：0=官网 1=广告 2=活动 3=转介绍 4=其他' }
        ],
        bodyFields: [
          { name: 'leadId', type: 'Long', required: false, desc: '线索ID' },
          { name: 'companyName', type: 'String', required: true, desc: '公司名称' },
          { name: 'contactName', type: 'String', required: false, desc: '联系人姓名' },
          { name: 'contactPhone', type: 'String', required: false, desc: '联系电话' },
          { name: 'leadSource', type: 'String', required: false, desc: '来源：0=官网 1=广告 2=活动 3=转介绍 4=其他' },
          { name: 'leadStatus', type: 'String', required: false, desc: '状态：0=新建 1=跟进中 2=已转化 3=无效 4=公海' },
          { name: 'remark', type: 'String', required: false, desc: '备注' }
        ],
        exampleBody: `{\n  "leadId": 1,\n  "companyName": "YY科技有限公司",\n  "contactName": "王五",\n  "contactPhone": "13900139000",\n  "leadSource": "0",\n  "leadStatus": "0"\n}`
      }),
      {
        method: 'PUT',
        path: '/mk/lead/receive/{leadId}',
        summary: '领取线索',
        permission: 'marketing:lead:receive',
        params: [{ name: 'leadId', type: 'Long', required: true, desc: '线索ID', in: 'path' }],
        requestExample: 'PUT /mk/lead/receive/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '销售人员领取线索，提交后需审批。'
      },
      {
        method: 'PUT',
        path: '/mk/lead/approve/{leadId}',
        summary: '审批通过领取申请',
        permission: 'marketing:lead:assign',
        params: [{ name: 'leadId', type: 'Long', required: true, desc: '线索ID', in: 'path' }],
        requestExample: 'PUT /mk/lead/approve/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '主管审批通过销售人员的领取申请。'
      },
      {
        method: 'PUT',
        path: '/mk/lead/reject/{leadId}',
        summary: '拒绝领取申请',
        permission: 'marketing:lead:assign',
        params: [{ name: 'leadId', type: 'Long', required: true, desc: '线索ID', in: 'path' }],
        requestExample: 'PUT /mk/lead/reject/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '主管拒绝销售人员的领取申请。'
      },
      {
        method: 'PUT',
        path: '/mk/lead/assign/{leadId}',
        summary: '分配线索',
        permission: 'marketing:lead:assign',
        params: [
          { name: 'leadId', type: 'Long', required: true, desc: '线索ID', in: 'path' },
          { name: 'userId', type: 'Long', required: true, desc: '负责人ID', in: 'body' },
          { name: 'deptId', type: 'Long', required: false, desc: '部门ID', in: 'body' }
        ],
        requestExample: `PUT /mk/lead/assign/1\nContent-Type: application/json\n\n{\n  "userId": 10,\n  "deptId": 100\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '将线索直接分配给指定销售人员。'
      },
      {
        method: 'PUT',
        path: '/mk/lead/convert/{leadId}',
        summary: '线索转化为客户',
        permission: 'marketing:lead:edit',
        params: [{ name: 'leadId', type: 'Long', required: true, desc: '线索ID', in: 'path' }],
        requestExample: 'PUT /mk/lead/convert/1',
        responseExample: `{\n  "code": 200,\n  "msg": "转化成功",\n  "convertCustomerId": 5\n}`,
        remark: '将线索转化为企业客户，返回新创建的客户ID。'
      },
      {
        method: 'PUT',
        path: '/mk/lead/batchAssign',
        summary: '批量分配线索',
        permission: 'marketing:lead:assign',
        params: [
          { name: 'leadIds', type: 'Long[]', required: true, desc: '线索ID数组', in: 'body' },
          { name: 'userId', type: 'Long', required: true, desc: '负责人ID', in: 'body' },
          { name: 'deptId', type: 'Long', required: false, desc: '部门ID', in: 'body' }
        ],
        requestExample: `PUT /mk/lead/batchAssign\nContent-Type: application/json\n\n{\n  "leadIds": [1, 2, 3],\n  "userId": 10\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '批量将多条线索分配给同一负责人。'
      },
      {
        method: 'PUT',
        path: '/mk/lead/batchStatus',
        summary: '批量变更线索状态',
        permission: 'marketing:lead:edit',
        params: [
          { name: 'leadIds', type: 'Long[]', required: true, desc: '线索ID数组', in: 'body' },
          { name: 'leadStatus', type: 'String', required: true, desc: '目标状态', in: 'body' }
        ],
        requestExample: `PUT /mk/lead/batchStatus\nContent-Type: application/json\n\n{\n  "leadIds": [1, 2],\n  "leadStatus": "1"\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '批量变更多条线索的状态。'
      },
      {
        method: 'PUT',
        path: '/mk/lead/release/{leadId}',
        summary: '释放线索到公海',
        permission: 'marketing:lead:edit',
        params: [{ name: 'leadId', type: 'Long', required: true, desc: '线索ID', in: 'path' }],
        requestExample: 'PUT /mk/lead/release/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '将线索释放回公海。'
      },
      {
        method: 'POST',
        path: '/mk/lead/importData',
        summary: '导入线索数据',
        permission: 'marketing:lead:add',
        params: [{ name: 'file', type: 'MultipartFile', required: true, desc: 'Excel文件', in: 'form' }],
        requestExample: 'POST /mk/lead/importData\nContent-Type: multipart/form-data',
        responseExample: COMMON_RESPONSE.success,
        remark: '通过Excel批量导入线索数据。'
      },
      {
        method: 'POST',
        path: '/mk/lead/importTemplate',
        summary: '下载导入模板',
        permission: 'marketing:lead:add',
        params: [],
        requestExample: 'POST /mk/lead/importTemplate',
        responseExample: `# 返回 Excel 文件流`,
        remark: '下载线索导入Excel模板。'
      },
      {
        method: 'PUT',
        path: '/mk/lead/invalidate/{leadId}',
        summary: '标记线索无效',
        permission: 'marketing:lead:edit',
        params: [
          { name: 'leadId', type: 'Long', required: true, desc: '线索ID', in: 'path' },
          { name: 'invalidReason', type: 'String', required: false, desc: '无效原因', in: 'body' }
        ],
        requestExample: 'PUT /mk/lead/invalidate/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '将线索标记为无效状态。'
      },
      {
        method: 'PUT',
        path: '/mk/lead/followTime/{leadId}',
        summary: '更新最后跟进时间',
        permission: 'marketing:lead:edit',
        params: [{ name: 'leadId', type: 'Long', required: true, desc: '线索ID', in: 'path' }],
        requestExample: 'PUT /mk/lead/followTime/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '更新线索的最后跟进时间为当前时间。'
      },
      {
        method: 'GET',
        path: '/mk/lead/checkDuplicate',
        summary: '线索查重',
        permission: 'marketing:lead:list',
        params: [
          { name: 'companyName', type: 'String', required: false, desc: '公司名称', in: 'query' },
          { name: 'contactPhone', type: 'String', required: false, desc: '联系电话', in: 'query' }
        ],
        requestExample: 'GET /mk/lead/checkDuplicate?companyName=XX科技',
        responseExample: COMMON_RESPONSE.success,
        remark: '根据公司名称或电话检查是否存在重复线索。'
      }
    ]
  },

  {
    name: '商机管理',
    basePath: '/mk/opportunity',
    description: '商机全生命周期：阶段推进引擎、看板视图、赢单输单、停滞分析。',
    apis: [
      ...crud({
        basePath: '/mk/opportunity',
        title: '商机',
        idField: 'opportunityId',
        permPrefix: 'marketing:opportunity',
        queryParams: [
          { name: 'opportunityName', type: 'String', required: false, desc: '商机名称' },
          { name: 'customerId', type: 'Long', required: false, desc: '客户ID' },
          { name: 'stageCode', type: 'String', required: false, desc: '阶段编码' },
          { name: 'opportunityStatus', type: 'String', required: false, desc: '状态：0=进行中 1=赢单 2=输单' }
        ],
        bodyFields: [
          { name: 'opportunityId', type: 'Long', required: false, desc: '商机ID' },
          { name: 'opportunityName', type: 'String', required: true, desc: '商机名称' },
          { name: 'customerId', type: 'Long', required: true, desc: '客户ID' },
          { name: 'estimatedAmount', type: 'BigDecimal', required: false, desc: '预计金额' },
          { name: 'estimatedCloseDate', type: 'Date', required: false, desc: '预计成交日期' },
          { name: 'stageCode', type: 'String', required: false, desc: '阶段编码' },
          { name: 'remark', type: 'String', required: false, desc: '备注' }
        ],
        exampleBody: `{\n  "opportunityId": 1,\n  "opportunityName": "ERP系统采购",\n  "customerId": 1,\n  "estimatedAmount": 500000,\n  "estimatedCloseDate": "2026-09-30",\n  "stageCode": "S1"\n}`
      }),
      {
        method: 'GET',
        path: '/mk/opportunity/kanban',
        summary: '看板视图（按阶段分组）',
        permission: 'marketing:opportunity:list',
        params: [],
        requestExample: 'GET /mk/opportunity/kanban',
        responseExample: COMMON_RESPONSE.success,
        remark: '按销售阶段分组查询商机，用于看板视图展示。'
      },
      {
        method: 'PUT',
        path: '/mk/opportunity/advance/{opportunityId}',
        summary: '阶段推进',
        permission: 'marketing:opportunity:edit',
        params: [
          { name: 'opportunityId', type: 'Long', required: true, desc: '商机ID', in: 'path' },
          { name: 'stageCode', type: 'String', required: true, desc: '目标阶段编码', in: 'body' },
          { name: 'remark', type: 'String', required: false, desc: '备注', in: 'body' }
        ],
        requestExample: `PUT /mk/opportunity/advance/1\nContent-Type: application/json\n\n{\n  "stageCode": "S2",\n  "remark": "客户确认需求"\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '将商机的阶段推进到下一阶段，系统自动记录阶段变更日志。'
      },
      {
        method: 'PUT',
        path: '/mk/opportunity/retreat/{opportunityId}',
        summary: '阶段退回',
        permission: 'marketing:opportunity:edit',
        params: [
          { name: 'opportunityId', type: 'Long', required: true, desc: '商机ID', in: 'path' },
          { name: 'stageCode', type: 'String', required: true, desc: '退回到的阶段编码', in: 'body' },
          { name: 'remark', type: 'String', required: false, desc: '退回原因', in: 'body' }
        ],
        requestExample: `PUT /mk/opportunity/retreat/1\nContent-Type: application/json\n\n{\n  "stageCode": "S1",\n  "remark": "需重新确认需求"\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '将商机的阶段退回到前一阶段。'
      },
      {
        method: 'PUT',
        path: '/mk/opportunity/win/{opportunityId}',
        summary: '赢单关单',
        permission: 'marketing:opportunity:edit',
        params: [
          { name: 'opportunityId', type: 'Long', required: true, desc: '商机ID', in: 'path' },
          { name: 'winReason', type: 'String', required: false, desc: '赢单原因', in: 'body' },
          { name: 'actualAmount', type: 'BigDecimal', required: false, desc: '实际成交金额', in: 'body' }
        ],
        requestExample: `PUT /mk/opportunity/win/1\nContent-Type: application/json\n\n{\n  "winReason": "价格优势",\n  "actualAmount": 480000\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '商机赢单关单，记录赢单原因和实际成交金额。'
      },
      {
        method: 'PUT',
        path: '/mk/opportunity/lose/{opportunityId}',
        summary: '输单关单',
        permission: 'marketing:opportunity:edit',
        params: [
          { name: 'opportunityId', type: 'Long', required: true, desc: '商机ID', in: 'path' },
          { name: 'loseReason', type: 'String', required: false, desc: '输单原因', in: 'body' }
        ],
        requestExample: `PUT /mk/opportunity/lose/1\nContent-Type: application/json\n\n{\n  "loseReason": "竞争对手价格更低"\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '商机输单关单，记录输单原因。'
      },
      {
        method: 'PUT',
        path: '/mk/opportunity/reopen/{opportunityId}',
        summary: '重新打开商机',
        permission: 'marketing:opportunity:edit',
        params: [{ name: 'opportunityId', type: 'Long', required: true, desc: '商机ID', in: 'path' }],
        requestExample: 'PUT /mk/opportunity/reopen/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '重新打开已关单的商机。'
      },
      {
        method: 'GET',
        path: '/mk/opportunity/stageLog/{opportunityId}',
        summary: '查询阶段变更日志',
        permission: 'marketing:opportunity:query',
        params: [{ name: 'opportunityId', type: 'Long', required: true, desc: '商机ID', in: 'path' }],
        requestExample: 'GET /mk/opportunity/stageLog/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '查询商机的阶段变更历史日志。'
      },
      {
        method: 'GET',
        path: '/mk/opportunity/relations/{opportunityId}',
        summary: '查询商机关联数据',
        permission: 'marketing:opportunity:query',
        params: [{ name: 'opportunityId', type: 'Long', required: true, desc: '商机ID', in: 'path' }],
        requestExample: 'GET /mk/opportunity/relations/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '查询商机关联的互动记录、阶段日志、合同、订单等综合数据。'
      },
      {
        method: 'GET',
        path: '/mk/opportunity/stagnant',
        summary: '查询停滞商机列表',
        permission: 'marketing:opportunity:list',
        params: [...PAGE_PARAMS],
        requestExample: 'GET /mk/opportunity/stagnant?pageNum=1&pageSize=10',
        responseExample: COMMON_RESPONSE.table,
        remark: '查询在当前阶段停留时间超过阈值的商机列表。'
      }
    ]
  },

  {
    name: '合同管理',
    basePath: '/mk/contract',
    description: '合同全生命周期：草稿→审批→生效→到期/终止，含变更管理和续签。',
    apis: [
      ...crud({
        basePath: '/mk/contract',
        title: '合同',
        idField: 'contractId',
        permPrefix: 'marketing:contract',
        queryParams: [
          { name: 'contractCode', type: 'String', required: false, desc: '合同编号' },
          { name: 'contractName', type: 'String', required: false, desc: '合同名称' },
          { name: 'customerId', type: 'Long', required: false, desc: '客户ID' },
          { name: 'contractStatus', type: 'String', required: false, desc: '状态：0=草稿 1=审批中 2=已生效 3=已过期 4=已终止' }
        ],
        bodyFields: [
          { name: 'contractId', type: 'Long', required: false, desc: '合同ID' },
          { name: 'contractCode', type: 'String', required: false, desc: '合同编号（系统自动生成）' },
          { name: 'contractName', type: 'String', required: true, desc: '合同名称' },
          { name: 'customerId', type: 'Long', required: true, desc: '客户ID' },
          { name: 'opportunityId', type: 'Long', required: false, desc: '关联商机ID' },
          { name: 'contractAmount', type: 'BigDecimal', required: false, desc: '合同金额' },
          { name: 'signDate', type: 'Date', required: false, desc: '签订日期' },
          { name: 'effectiveDate', type: 'Date', required: false, desc: '生效日期' },
          { name: 'expireDate', type: 'Date', required: false, desc: '到期日期' },
          { name: 'remark', type: 'String', required: false, desc: '备注' }
        ],
        exampleBody: `{\n  "contractId": 1,\n  "contractName": "ERP系统采购合同",\n  "customerId": 1,\n  "contractAmount": 480000,\n  "signDate": "2026-07-18",\n  "effectiveDate": "2026-08-01",\n  "expireDate": "2027-07-31"\n}`
      }),
      {
        method: 'PUT',
        path: '/mk/contract/submit/{contractId}',
        summary: '提交审批',
        permission: 'marketing:contract:edit',
        params: [{ name: 'contractId', type: 'Long', required: true, desc: '合同ID', in: 'path' }],
        requestExample: 'PUT /mk/contract/submit/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '将草稿状态的合同提交审批，状态变为审批中。'
      },
      {
        method: 'PUT',
        path: '/mk/contract/approve/{contractId}',
        summary: '审批通过',
        permission: 'marketing:contract:approve',
        params: [
          { name: 'contractId', type: 'Long', required: true, desc: '合同ID', in: 'path' },
          { name: 'approveOpinion', type: 'String', required: false, desc: '审批意见', in: 'query' }
        ],
        requestExample: 'PUT /mk/contract/approve/1?approveOpinion=同意',
        responseExample: COMMON_RESPONSE.success,
        remark: '审批通过，合同状态变为已生效。'
      },
      {
        method: 'PUT',
        path: '/mk/contract/reject/{contractId}',
        summary: '审批驳回',
        permission: 'marketing:contract:approve',
        params: [
          { name: 'contractId', type: 'Long', required: true, desc: '合同ID', in: 'path' },
          { name: 'approveOpinion', type: 'String', required: false, desc: '驳回原因', in: 'query' }
        ],
        requestExample: 'PUT /mk/contract/reject/1?approveOpinion=金额需调整',
        responseExample: COMMON_RESPONSE.success,
        remark: '审批驳回，合同退回草稿状态。'
      },
      {
        method: 'PUT',
        path: '/mk/contract/terminate/{contractId}',
        summary: '终止合同',
        permission: 'marketing:contract:edit',
        params: [
          { name: 'contractId', type: 'Long', required: true, desc: '合同ID', in: 'path' },
          { name: 'terminateReason', type: 'String', required: false, desc: '终止原因', in: 'query' }
        ],
        requestExample: 'PUT /mk/contract/terminate/1?terminateReason=客户违约',
        responseExample: COMMON_RESPONSE.success,
        remark: '终止已生效的合同。'
      },
      {
        method: 'POST',
        path: '/mk/contract/renew/{contractId}',
        summary: '合同续签',
        permission: 'marketing:contract:renew',
        params: [
          { name: 'contractId', type: 'Long', required: true, desc: '原合同ID', in: 'path' },
          { name: 'newExpireDate', type: 'Date', required: true, desc: '新到期日期', in: 'query' },
          { name: 'newAmount', type: 'BigDecimal', required: false, desc: '新合同金额', in: 'query' }
        ],
        requestExample: 'POST /mk/contract/renew/1?newExpireDate=2028-07-31&newAmount=500000',
        responseExample: COMMON_RESPONSE.success,
        remark: '基于原合同创建续签合同。'
      },
      {
        method: 'GET',
        path: '/mk/contract/expiring/{days}',
        summary: '查询即将到期的合同',
        permission: 'marketing:contract:list',
        params: [{ name: 'days', type: 'Integer', required: true, desc: '天数（查询N天内到期的合同）', in: 'path' }],
        requestExample: 'GET /mk/contract/expiring/30',
        responseExample: COMMON_RESPONSE.success,
        remark: '查询指定天数内即将到期的合同列表。'
      },
      {
        method: 'POST',
        path: '/mk/contract/change',
        summary: '提交合同变更申请',
        permission: 'marketing:contract:change',
        params: [
          { name: 'contractId', type: 'Long', required: true, desc: '合同ID', in: 'body' },
          { name: 'changeType', type: 'String', required: true, desc: '变更类型', in: 'body' },
          { name: 'changeContent', type: 'String', required: false, desc: '变更内容', in: 'body' }
        ],
        requestExample: `POST /mk/contract/change\nContent-Type: application/json\n\n{\n  "contractId": 1,\n  "changeType": "amount",\n  "changeContent": "金额从48万调整为50万"\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '提交合同变更申请，需审批后生效。'
      },
      {
        method: 'GET',
        path: '/mk/contract/change/list/{contractId}',
        summary: '查询合同变更记录',
        permission: 'marketing:contract:list',
        params: [{ name: 'contractId', type: 'Long', required: true, desc: '合同ID', in: 'path' }],
        requestExample: 'GET /mk/contract/change/list/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '查询指定合同的所有变更记录。'
      },
      {
        method: 'PUT',
        path: '/mk/contract/change/approve/{logId}/{approved}',
        summary: '审批合同变更',
        permission: 'marketing:contract:change',
        params: [
          { name: 'logId', type: 'Long', required: true, desc: '变更记录ID', in: 'path' },
          { name: 'approved', type: 'Boolean', required: true, desc: '是否通过：true/false', in: 'path' },
          { name: 'opinion', type: 'String', required: false, desc: '审批意见', in: 'query' }
        ],
        requestExample: 'PUT /mk/contract/change/approve/1/true?opinion=同意变更',
        responseExample: COMMON_RESPONSE.success,
        remark: '审批合同变更申请，通过后自动更新合同信息。'
      }
    ]
  },

  {
    name: '订单管理',
    basePath: '/mk/order',
    description: '订单全流程：确认→发货→签收→完成/取消。',
    apis: [
      ...crud({
        basePath: '/mk/order',
        title: '订单',
        idField: 'orderId',
        permPrefix: 'marketing:order',
        queryParams: [
          { name: 'orderCode', type: 'String', required: false, desc: '订单编号' },
          { name: 'customerId', type: 'Long', required: false, desc: '客户ID' },
          { name: 'contractId', type: 'Long', required: false, desc: '合同ID' },
          { name: 'orderStatus', type: 'String', required: false, desc: '状态：0=待确认 1=已确认 2=已发货 3=已签收 4=已完成 5=已取消' }
        ],
        bodyFields: [
          { name: 'orderId', type: 'Long', required: false, desc: '订单ID' },
          { name: 'orderCode', type: 'String', required: false, desc: '订单编号（系统自动生成）' },
          { name: 'customerId', type: 'Long', required: true, desc: '客户ID' },
          { name: 'contractId', type: 'Long', required: false, desc: '关联合同ID' },
          { name: 'opportunityId', type: 'Long', required: false, desc: '关联商机ID' },
          { name: 'orderAmount', type: 'BigDecimal', required: false, desc: '订单金额' },
          { name: 'orderDate', type: 'Date', required: false, desc: '下单日期' },
          { name: 'remark', type: 'String', required: false, desc: '备注' }
        ],
        exampleBody: `{\n  "orderId": 1,\n  "orderCode": "ORD20260718001",\n  "customerId": 1,\n  "contractId": 1,\n  "orderAmount": 480000,\n  "orderDate": "2026-07-18"\n}`
      }),
      {
        method: 'PUT',
        path: '/mk/order/confirm/{orderId}',
        summary: '订单确认',
        permission: 'marketing:order:confirm',
        params: [{ name: 'orderId', type: 'Long', required: true, desc: '订单ID', in: 'path' }],
        requestExample: 'PUT /mk/order/confirm/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '确认订单，状态从待确认变为已确认。'
      },
      {
        method: 'PUT',
        path: '/mk/order/deliver',
        summary: '订单发货',
        permission: 'marketing:order:deliver',
        params: [
          { name: 'orderId', type: 'Long', required: true, desc: '订单ID', in: 'body' },
          { name: 'deliverDate', type: 'Date', required: false, desc: '发货日期', in: 'body' },
          { name: 'expressNo', type: 'String', required: false, desc: '物流单号', in: 'body' }
        ],
        requestExample: `PUT /mk/order/deliver\nContent-Type: application/json\n\n{\n  "orderId": 1,\n  "deliverDate": "2026-07-20",\n  "expressNo": "SF1234567890"\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '订单发货，记录发货信息和物流单号。'
      },
      {
        method: 'PUT',
        path: '/mk/order/receive/{orderId}',
        summary: '订单签收',
        permission: 'marketing:order:receive',
        params: [
          { name: 'orderId', type: 'Long', required: true, desc: '订单ID', in: 'path' },
          { name: 'receivePerson', type: 'String', required: false, desc: '签收人', in: 'query' }
        ],
        requestExample: 'PUT /mk/order/receive/1?receivePerson=张三',
        responseExample: COMMON_RESPONSE.success,
        remark: '订单签收确认。'
      },
      {
        method: 'PUT',
        path: '/mk/order/complete/{orderId}',
        summary: '订单完成',
        permission: 'marketing:order:edit',
        params: [{ name: 'orderId', type: 'Long', required: true, desc: '订单ID', in: 'path' }],
        requestExample: 'PUT /mk/order/complete/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '标记订单为已完成。'
      },
      {
        method: 'PUT',
        path: '/mk/order/cancel/{orderId}',
        summary: '订单取消',
        permission: 'marketing:order:cancel',
        params: [
          { name: 'orderId', type: 'Long', required: true, desc: '订单ID', in: 'path' },
          { name: 'cancelReason', type: 'String', required: false, desc: '取消原因', in: 'query' }
        ],
        requestExample: 'PUT /mk/order/cancel/1?cancelReason=客户取消',
        responseExample: COMMON_RESPONSE.success,
        remark: '取消订单，记录取消原因。'
      }
    ]
  },

  {
    name: '联系人管理',
    basePath: '/mk/contact',
    description: '联系人管理：主要联系人、去重合并、分配转移、导入、跟进提醒。',
    apis: [
      ...crud({
        basePath: '/mk/contact',
        title: '联系人',
        idField: 'contactId',
        permPrefix: 'marketing:contact',
        queryParams: [
          { name: 'contactName', type: 'String', required: false, desc: '联系人姓名' },
          { name: 'customerId', type: 'Long', required: false, desc: '客户ID' },
          { name: 'phone', type: 'String', required: false, desc: '电话' }
        ],
        bodyFields: [
          { name: 'contactId', type: 'Long', required: false, desc: '联系人ID' },
          { name: 'customerId', type: 'Long', required: true, desc: '客户ID' },
          { name: 'contactName', type: 'String', required: true, desc: '联系人姓名' },
          { name: 'phone', type: 'String', required: false, desc: '电话' },
          { name: 'email', type: 'String', required: false, desc: '邮箱' },
          { name: 'position', type: 'String', required: false, desc: '职务' },
          { name: 'isPrimary', type: 'String', required: false, desc: '是否主要联系人：0=否 1=是' },
          { name: 'isKey', type: 'String', required: false, desc: '是否关键联系人：0=否 1=是' },
          { name: 'remark', type: 'String', required: false, desc: '备注' }
        ],
        exampleBody: `{\n  "contactId": 1,\n  "customerId": 1,\n  "contactName": "张三",\n  "phone": "13800138000",\n  "email": "zhangsan@xx.com",\n  "position": "采购经理",\n  "isPrimary": "1"\n}`
      }),
      {
        method: 'PUT',
        path: '/mk/contact/setPrimary/{contactId}',
        summary: '设为主要联系人',
        permission: 'marketing:contact:edit',
        params: [{ name: 'contactId', type: 'Long', required: true, desc: '联系人ID', in: 'path' }],
        requestExample: 'PUT /mk/contact/setPrimary/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '将联系人设为主要联系人（同客户下唯一）。'
      },
      {
        method: 'PUT',
        path: '/mk/contact/batchSetKey',
        summary: '批量设置关键联系人',
        permission: 'marketing:contact:edit',
        params: [
          { name: 'contactIds', type: 'Long[]', required: true, desc: '联系人ID数组', in: 'body' },
          { name: 'isKey', type: 'String', required: true, desc: '是否关键：0=否 1=是', in: 'body' }
        ],
        requestExample: `PUT /mk/contact/batchSetKey\nContent-Type: application/json\n\n{\n  "contactIds": [1, 2, 3],\n  "isKey": "1"\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '批量设置联系人为关键联系人。'
      },
      {
        method: 'GET',
        path: '/mk/contact/checkDuplicate',
        summary: '检查重复联系人',
        permission: 'marketing:contact:list',
        params: [
          { name: 'phone', type: 'String', required: false, desc: '电话', in: 'query' },
          { name: 'email', type: 'String', required: false, desc: '邮箱', in: 'query' }
        ],
        requestExample: 'GET /mk/contact/checkDuplicate?phone=13800138000',
        responseExample: COMMON_RESPONSE.success,
        remark: '根据电话或邮箱检查是否存在重复联系人。'
      },
      {
        method: 'PUT',
        path: '/mk/contact/merge',
        summary: '合并联系人',
        permission: 'marketing:contact:merge',
        params: [
          { name: 'masterId', type: 'Long', required: true, desc: '主联系人ID（保留）', in: 'body' },
          { name: 'slaveId', type: 'Long', required: true, desc: '从联系人ID（合并后删除）', in: 'body' }
        ],
        requestExample: `PUT /mk/contact/merge\nContent-Type: application/json\n\n{\n  "masterId": 1,\n  "slaveId": 2\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '将两个重复联系人合并为一个，从联系人的关联数据转移到主联系人。'
      },
      {
        method: 'PUT',
        path: '/mk/contact/assign/{contactId}',
        summary: '分配/转移联系人',
        permission: 'marketing:contact:assign',
        params: [
          { name: 'contactId', type: 'Long', required: true, desc: '联系人ID', in: 'path' },
          { name: 'ownerUserId', type: 'Long', required: true, desc: '新负责人ID', in: 'body' }
        ],
        requestExample: `PUT /mk/contact/assign/1\nContent-Type: application/json\n\n{\n  "ownerUserId": 10\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '将联系人分配或转移给其他负责人。'
      },
      {
        method: 'PUT',
        path: '/mk/contact/release/{contactId}',
        summary: '释放联系人到公海',
        permission: 'marketing:contact:assign',
        params: [{ name: 'contactId', type: 'Long', required: true, desc: '联系人ID', in: 'path' }],
        requestExample: 'PUT /mk/contact/release/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '将联系人释放到公海。'
      },
      {
        method: 'POST',
        path: '/mk/contact/importData',
        summary: '导入联系人',
        permission: 'marketing:contact:import',
        params: [{ name: 'file', type: 'MultipartFile', required: true, desc: 'Excel文件', in: 'form' }],
        requestExample: 'POST /mk/contact/importData\nContent-Type: multipart/form-data',
        responseExample: COMMON_RESPONSE.success,
        remark: '通过Excel批量导入联系人数据。'
      },
      {
        method: 'POST',
        path: '/mk/contact/importTemplate',
        summary: '下载导入模板',
        permission: 'marketing:contact:import',
        params: [],
        requestExample: 'POST /mk/contact/importTemplate',
        responseExample: `# 返回 Excel 文件流`,
        remark: '下载联系人导入Excel模板。'
      },
      {
        method: 'GET',
        path: '/mk/contact/followUpReminders',
        summary: '查询待跟进联系人',
        permission: 'marketing:contact:list',
        params: [],
        requestExample: 'GET /mk/contact/followUpReminders',
        responseExample: COMMON_RESPONSE.success,
        remark: '查询当前登录用户需要跟进的联系人列表。'
      }
    ]
  },

  {
    name: '营销活动',
    basePath: '/mk/activity',
    description: '营销活动管理：状态流转、复盘、复制、批量操作、报名链接。',
    apis: [
      ...crud({
        basePath: '/mk/activity',
        title: '营销活动',
        idField: 'activityId',
        permPrefix: 'marketing:activity',
        queryParams: [
          { name: 'activityName', type: 'String', required: false, desc: '活动名称' },
          { name: 'activityType', type: 'String', required: false, desc: '活动类型：0=展会 1=研讨会 2=网络直播 3=促销 4=其他' },
          { name: 'status', type: 'String', required: false, desc: '状态：0=策划中 1=报名中 2=进行中 3=已结束 4=已取消' }
        ],
        bodyFields: [
          { name: 'activityId', type: 'Long', required: false, desc: '活动ID' },
          { name: 'activityName', type: 'String', required: true, desc: '活动名称' },
          { name: 'activityType', type: 'String', required: false, desc: '活动类型：0=展会 1=研讨会 2=网络直播 3=促销 4=其他' },
          { name: 'startTime', type: 'Date', required: false, desc: '开始时间' },
          { name: 'endTime', type: 'Date', required: false, desc: '结束时间' },
          { name: 'location', type: 'String', required: false, desc: '活动地点' },
          { name: 'budget', type: 'BigDecimal', required: false, desc: '预算' },
          { name: 'description', type: 'String', required: false, desc: '活动描述' },
          { name: 'status', type: 'String', required: false, desc: '状态：0=策划中 1=报名中 2=进行中 3=已结束 4=已取消' }
        ],
        exampleBody: `{\n  "activityId": 1,\n  "activityName": "2026年智能制造峰会",\n  "activityType": "0",\n  "startTime": "2026-09-01 09:00:00",\n  "endTime": "2026-09-01 17:00:00",\n  "location": "北京国际会议中心",\n  "budget": 100000,\n  "status": "0"\n}`
      }),
      {
        method: 'PUT',
        path: '/mk/activity/changeStatus/{activityId}',
        summary: '活动状态流转',
        permission: 'marketing:activity:status',
        params: [
          { name: 'activityId', type: 'Long', required: true, desc: '活动ID', in: 'path' },
          { name: 'toStatus', type: 'String', required: true, desc: '目标状态', in: 'body' },
          { name: 'remark', type: 'String', required: false, desc: '备注', in: 'body' }
        ],
        requestExample: `PUT /mk/activity/changeStatus/1\nContent-Type: application/json\n\n{\n  "toStatus": "1",\n  "remark": "开始报名"\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '变更活动状态，系统自动记录状态流转日志。'
      },
      {
        method: 'GET',
        path: '/mk/activity/statusLogs/{activityId}',
        summary: '查询活动状态日志',
        permission: 'marketing:activity:query',
        params: [{ name: 'activityId', type: 'Long', required: true, desc: '活动ID', in: 'path' }],
        requestExample: 'GET /mk/activity/statusLogs/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '查询活动状态流转历史日志。'
      },
      {
        method: 'GET',
        path: '/mk/activity/review/{activityId}',
        summary: '获取活动复盘数据',
        permission: 'marketing:activity:review',
        params: [{ name: 'activityId', type: 'Long', required: true, desc: '活动ID', in: 'path' }],
        requestExample: 'GET /mk/activity/review/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '获取活动复盘统计数据（参与人数、签到率、转化线索数等）。'
      },
      {
        method: 'PUT',
        path: '/mk/activity/saveReview',
        summary: '保存活动复盘',
        permission: 'marketing:activity:review',
        params: [
          { name: 'activityId', type: 'Long', required: true, desc: '活动ID', in: 'body' },
          { name: 'reviewResult', type: 'String', required: false, desc: '复盘结论', in: 'body' }
        ],
        requestExample: `PUT /mk/activity/saveReview\nContent-Type: application/json\n\n{\n  "activityId": 1,\n  "reviewResult": "活动效果良好，转化率15%"\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '保存活动复盘结论。'
      },
      {
        method: 'POST',
        path: '/mk/activity/copy/{activityId}',
        summary: '复制活动',
        permission: 'marketing:activity:copy',
        params: [{ name: 'activityId', type: 'Long', required: true, desc: '原活动ID', in: 'path' }],
        requestExample: 'POST /mk/activity/copy/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '复制一个已有活动，生成新的草稿活动。'
      },
      {
        method: 'PUT',
        path: '/mk/activity/batchChangeStatus',
        summary: '批量更新活动状态',
        permission: 'marketing:activity:status',
        params: [
          { name: 'activityIds', type: 'Long[]', required: true, desc: '活动ID数组', in: 'body' },
          { name: 'status', type: 'String', required: true, desc: '目标状态', in: 'body' }
        ],
        requestExample: `PUT /mk/activity/batchChangeStatus\nContent-Type: application/json\n\n{\n  "activityIds": [1, 2, 3],\n  "status": "3"\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '批量更新多个活动的状态。'
      },
      {
        method: 'PUT',
        path: '/mk/activity/batchUpdateOwner',
        summary: '批量更新活动负责人',
        permission: 'marketing:activity:edit',
        params: [
          { name: 'activityIds', type: 'Long[]', required: true, desc: '活动ID数组', in: 'body' },
          { name: 'userId', type: 'Long', required: true, desc: '负责人用户ID', in: 'body' },
          { name: 'userName', type: 'String', required: false, desc: '负责人姓名', in: 'body' },
          { name: 'deptId', type: 'Long', required: false, desc: '部门ID', in: 'body' }
        ],
        requestExample: `PUT /mk/activity/batchUpdateOwner\nContent-Type: application/json\n\n{\n  "activityIds": [1, 2, 3],\n  "userId": 10,\n  "userName": "张三",\n  "deptId": 100\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '批量更新多个活动的负责人。'
      },
      {
        method: 'GET',
        path: '/mk/activity/registerUrl/{activityId}',
        summary: '获取活动报名链接',
        permission: 'marketing:activity:query',
        params: [{ name: 'activityId', type: 'Long', required: true, desc: '活动ID', in: 'path' }],
        requestExample: 'GET /mk/activity/registerUrl/1',
        responseExample: `{\n  "code": 200,\n  "msg": "获取成功",\n  "url": "/h5/register/1?token=abc123",\n  "token": "abc123"\n}`,
        remark: '生成活动的报名链接和安全令牌，用于H5页面报名。'
      }
    ]
  },

  {
    name: '活动参与人',
    basePath: '/mk/participant',
    description: '活动参与人管理：签到、转线索。',
    apis: [
      ...crud({
        basePath: '/mk/participant',
        title: '活动参与人',
        idField: 'participantId',
        permPrefix: 'marketing:participant',
        queryParams: [
          { name: 'activityId', type: 'Long', required: false, desc: '活动ID' },
          { name: 'participantName', type: 'String', required: false, desc: '参与人姓名' },
          { name: 'signInStatus', type: 'String', required: false, desc: '签到状态：0=未签到 1=已签到' }
        ],
        bodyFields: [
          { name: 'participantId', type: 'Long', required: false, desc: '参与人ID' },
          { name: 'activityId', type: 'Long', required: true, desc: '活动ID' },
          { name: 'participantName', type: 'String', required: true, desc: '参与人姓名' },
          { name: 'phone', type: 'String', required: false, desc: '电话' },
          { name: 'email', type: 'String', required: false, desc: '邮箱' },
          { name: 'company', type: 'String', required: false, desc: '公司' }
        ],
        exampleBody: `{\n  "participantId": 1,\n  "activityId": 1,\n  "participantName": "李四",\n  "phone": "13900139000",\n  "company": "YY科技"\n}`
      }),
      {
        method: 'GET',
        path: '/mk/participant/byActivity/{activityId}',
        summary: '按活动查询参与人',
        permission: 'marketing:participant:list',
        params: [{ name: 'activityId', type: 'Long', required: true, desc: '活动ID', in: 'path' }],
        requestExample: 'GET /mk/participant/byActivity/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '查询指定活动的所有参与人列表。'
      },
      {
        method: 'PUT',
        path: '/mk/participant/signIn/{participantId}',
        summary: '参与人签到',
        permission: 'marketing:participant:edit',
        params: [{ name: 'participantId', type: 'Long', required: true, desc: '参与人ID', in: 'path' }],
        requestExample: 'PUT /mk/participant/signIn/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '对活动参与人进行签到登记。'
      },
      {
        method: 'POST',
        path: '/mk/participant/convertToLead/{participantId}',
        summary: '参与人转线索',
        permission: 'marketing:participant:convert',
        params: [{ name: 'participantId', type: 'Long', required: true, desc: '参与人ID', in: 'path' }],
        requestExample: 'POST /mk/participant/convertToLead/1',
        responseExample: `{\n  "code": 200,\n  "msg": "转线索成功",\n  "leadId": 5\n}`,
        remark: '将活动参与人转化为线索，返回新创建的线索ID。'
      }
    ]
  },

  {
    name: '互动记录',
    basePath: '/mk/interaction',
    description: '客户互动记录管理：电话、拜访、邮件等各类跟进记录。',
    apis: crud({
      basePath: '/mk/interaction',
      title: '互动记录',
      idField: 'recordId',
      permPrefix: 'marketing:interaction',
      queryParams: [
        { name: 'customerId', type: 'Long', required: false, desc: '客户ID' },
        { name: 'interactionType', type: 'String', required: false, desc: '互动类型：0=电话 1=拜访 2=邮件 3=微信 4=其他' },
        { name: 'startTime', type: 'String', required: false, desc: '开始时间' },
        { name: 'endTime', type: 'String', required: false, desc: '结束时间' }
      ],
      bodyFields: [
        { name: 'recordId', type: 'Long', required: false, desc: '记录ID' },
        { name: 'customerId', type: 'Long', required: true, desc: '客户ID' },
        { name: 'contactId', type: 'Long', required: false, desc: '联系人ID' },
        { name: 'interactionType', type: 'String', required: true, desc: '互动类型：0=电话 1=拜访 2=邮件 3=微信 4=其他' },
        { name: 'interactionTime', type: 'Date', required: false, desc: '互动时间' },
        { name: 'content', type: 'String', required: false, desc: '互动内容' },
        { name: 'nextFollowTime', type: 'Date', required: false, desc: '下次跟进时间' }
      ],
      exampleBody: `{\n  "recordId": 1,\n  "customerId": 1,\n  "contactId": 1,\n  "interactionType": "0",\n  "interactionTime": "2026-07-18 14:00:00",\n  "content": "电话沟通需求",\n  "nextFollowTime": "2026-07-25"\n}`
    })
  },

  {
    name: '发票管理',
    basePath: '/mk/invoice',
    description: '发票管理：开票、作废。',
    apis: [
      ...crud({
        basePath: '/mk/invoice',
        title: '发票',
        idField: 'invoiceId',
        permPrefix: 'marketing:invoice',
        queryParams: [
          { name: 'invoiceCode', type: 'String', required: false, desc: '发票号' },
          { name: 'customerId', type: 'Long', required: false, desc: '客户ID' },
          { name: 'invoiceStatus', type: 'String', required: false, desc: '状态：0=待开票 1=已开票 2=已作废' }
        ],
        bodyFields: [
          { name: 'invoiceId', type: 'Long', required: false, desc: '发票ID' },
          { name: 'invoiceCode', type: 'String', required: false, desc: '发票号' },
          { name: 'customerId', type: 'Long', required: true, desc: '客户ID' },
          { name: 'orderId', type: 'Long', required: false, desc: '关联订单ID' },
          { name: 'invoiceType', type: 'String', required: false, desc: '发票类型：0=增值税专用 1=增值税普通' },
          { name: 'invoiceAmount', type: 'BigDecimal', required: true, desc: '开票金额' },
          { name: 'invoiceTitle', type: 'String', required: false, desc: '发票抬头' },
          { name: 'taxNo', type: 'String', required: false, desc: '税号' },
          { name: 'invoiceStatus', type: 'String', required: false, desc: '状态：0=待开票 1=已开票 2=已作废' }
        ],
        exampleBody: `{\n  "invoiceId": 1,\n  "invoiceCode": "INV20260718001",\n  "customerId": 1,\n  "orderId": 1,\n  "invoiceType": "0",\n  "invoiceAmount": 480000,\n  "invoiceTitle": "XX科技有限公司",\n  "taxNo": "91110108XXXXXXXX"\n}`
      }),
      {
        method: 'PUT',
        path: '/mk/invoice/issue/{invoiceId}',
        summary: '开票',
        permission: 'marketing:invoice:issue',
        params: [{ name: 'invoiceId', type: 'Long', required: true, desc: '发票ID', in: 'path' }],
        requestExample: 'PUT /mk/invoice/issue/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '将待开票的发票标记为已开票。'
      },
      {
        method: 'PUT',
        path: '/mk/invoice/void/{invoiceId}',
        summary: '作废发票',
        permission: 'marketing:invoice:void',
        params: [{ name: 'invoiceId', type: 'Long', required: true, desc: '发票ID', in: 'path' }],
        requestExample: 'PUT /mk/invoice/void/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '将已开票的发票作废。'
      }
    ]
  },

  {
    name: '回款管理',
    basePath: '/mk/payment',
    description: '回款计划与回款记录管理：计划制定、多次回款登记、回款确认。',
    apis: [
      ...crud({
        basePath: '/mk/payment',
        title: '回款计划',
        idField: 'planId',
        permPrefix: 'marketing:payment',
        getInfoPath: '/mk/payment/detail/{planId}',
        queryParams: [
          { name: 'customerId', type: 'Long', required: false, desc: '客户ID' },
          { name: 'contractId', type: 'Long', required: false, desc: '合同ID' },
          { name: 'planStatus', type: 'String', required: false, desc: '状态：0=待回款 1=部分回款 2=已回款 3=逾期' }
        ],
        bodyFields: [
          { name: 'planId', type: 'Long', required: false, desc: '计划ID' },
          { name: 'customerId', type: 'Long', required: true, desc: '客户ID' },
          { name: 'contractId', type: 'Long', required: false, desc: '合同ID' },
          { name: 'planAmount', type: 'BigDecimal', required: true, desc: '计划回款金额' },
          { name: 'planDate', type: 'Date', required: true, desc: '计划回款日期' },
          { name: 'remark', type: 'String', required: false, desc: '备注' }
        ],
        exampleBody: `{\n  "planId": 1,\n  "customerId": 1,\n  "contractId": 1,\n  "planAmount": 240000,\n  "planDate": "2026-09-01"\n}`
      }),
      {
        method: 'GET',
        path: '/mk/payment/statistics',
        summary: '回款统计',
        permission: 'marketing:payment:list',
        params: [],
        requestExample: 'GET /mk/payment/statistics',
        responseExample: `{\n  "code": 200,\n  "data": {\n    "totalAmount": 1000000,\n    "receivedAmount": 600000,\n    "pendingAmount": 300000,\n    "overdueAmount": 100000\n  }\n}`,
        remark: '回款统计：总计划金额、已回款金额、待回款金额、逾期金额。'
      },
      {
        method: 'GET',
        path: '/mk/payment/record/list',
        summary: '回款记录列表',
        permission: 'marketing:payment:list',
        params: [...PAGE_PARAMS, { name: 'planId', type: 'Long', required: false, desc: '回款计划ID' }],
        requestExample: 'GET /mk/payment/record/list?planId=1&pageNum=1&pageSize=10',
        responseExample: COMMON_RESPONSE.table,
        remark: '分页查询回款记录列表。'
      },
      {
        method: 'GET',
        path: '/mk/payment/record/{recordId}',
        summary: '回款记录详情',
        permission: 'marketing:payment:query',
        params: [{ name: 'recordId', type: 'Long', required: true, desc: '记录ID', in: 'path' }],
        requestExample: 'GET /mk/payment/record/1',
        responseExample: COMMON_RESPONSE.success,
        remark: '查询单条回款记录详情。'
      },
      {
        method: 'POST',
        path: '/mk/payment/record',
        summary: '新增回款记录',
        permission: 'marketing:payment:edit',
        params: [
          { name: 'planId', type: 'Long', required: true, desc: '回款计划ID', in: 'body' },
          { name: 'receivedAmount', type: 'BigDecimal', required: true, desc: '本次回款金额', in: 'body' },
          { name: 'receivedDate', type: 'Date', required: true, desc: '回款日期', in: 'body' }
        ],
        requestExample: `POST /mk/payment/record\nContent-Type: application/json\n\n{\n  "planId": 1,\n  "receivedAmount": 120000,\n  "receivedDate": "2026-09-01"\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '登记一次回款记录，支持同一计划多次回款。'
      },
      {
        method: 'PUT',
        path: '/mk/payment/record',
        summary: '修改回款记录',
        permission: 'marketing:payment:edit',
        params: [
          { name: 'recordId', type: 'Long', required: true, desc: '记录ID', in: 'body' },
          { name: 'receivedAmount', type: 'BigDecimal', required: false, desc: '回款金额', in: 'body' },
          { name: 'receivedDate', type: 'Date', required: false, desc: '回款日期', in: 'body' }
        ],
        requestExample: `PUT /mk/payment/record\nContent-Type: application/json\n\n{\n  "recordId": 1,\n  "receivedAmount": 150000\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '修改回款记录信息。'
      },
      {
        method: 'DELETE',
        path: '/mk/payment/record/{recordIds}',
        summary: '删除回款记录',
        permission: 'marketing:payment:edit',
        params: [{ name: 'recordIds', type: 'Long[]', required: true, desc: '记录ID数组', in: 'path' }],
        requestExample: 'DELETE /mk/payment/record/1,2',
        responseExample: COMMON_RESPONSE.success,
        remark: '删除回款记录。'
      },
      {
        method: 'PUT',
        path: '/mk/payment/record/confirm/{recordId}/{confirmStatus}',
        summary: '确认回款记录',
        permission: 'marketing:payment:confirm',
        params: [
          { name: 'recordId', type: 'Long', required: true, desc: '记录ID', in: 'path' },
          { name: 'confirmStatus', type: 'String', required: true, desc: '确认状态：1=确认 2=拒绝', in: 'path' },
          { name: 'remark', type: 'String', required: false, desc: '备注', in: 'query' }
        ],
        requestExample: 'PUT /mk/payment/record/confirm/1/1?remark=已核实',
        responseExample: COMMON_RESPONSE.success,
        remark: '确认或拒绝回款记录，确认后更新回款计划状态。'
      }
    ]
  },

  {
    name: '订单退货',
    basePath: '/mk/return',
    description: '订单退货管理：退货审批、退款。',
    apis: [
      ...crud({
        basePath: '/mk/return',
        title: '订单退货',
        idField: 'returnId',
        permPrefix: 'marketing:return',
        hasExport: true,
        queryParams: [
          { name: 'returnCode', type: 'String', required: false, desc: '退货单号' },
          { name: 'orderId', type: 'Long', required: false, desc: '原订单ID' },
          { name: 'returnStatus', type: 'String', required: false, desc: '状态：0=待审批 1=已通过 2=已拒绝 3=已退款' }
        ],
        bodyFields: [
          { name: 'returnId', type: 'Long', required: false, desc: '退货ID' },
          { name: 'returnCode', type: 'String', required: false, desc: '退货单号' },
          { name: 'orderId', type: 'Long', required: true, desc: '原订单ID' },
          { name: 'returnAmount', type: 'BigDecimal', required: true, desc: '退货金额' },
          { name: 'returnReason', type: 'String', required: false, desc: '退货原因' },
          { name: 'remark', type: 'String', required: false, desc: '备注' }
        ],
        exampleBody: `{\n  "returnId": 1,\n  "returnCode": "RET20260718001",\n  "orderId": 1,\n  "returnAmount": 50000,\n  "returnReason": "产品规格不符"\n}`
      }),
      {
        method: 'PUT',
        path: '/mk/return/approve/{returnId}/{approved}',
        summary: '审批退货',
        permission: 'marketing:return:approve',
        params: [
          { name: 'returnId', type: 'Long', required: true, desc: '退货ID', in: 'path' },
          { name: 'approved', type: 'Boolean', required: true, desc: '是否通过', in: 'path' },
          { name: 'opinion', type: 'String', required: false, desc: '审批意见', in: 'query' }
        ],
        requestExample: 'PUT /mk/return/approve/1/true?opinion=同意退货',
        responseExample: COMMON_RESPONSE.success,
        remark: '审批退货申请。'
      },
      {
        method: 'PUT',
        path: '/mk/return/refund/{returnId}',
        summary: '退款',
        permission: 'marketing:return:refund',
        params: [
          { name: 'returnId', type: 'Long', required: true, desc: '退货ID', in: 'path' },
          { name: 'refundAmount', type: 'BigDecimal', required: true, desc: '退款金额', in: 'query' },
          { name: 'refundDate', type: 'String', required: true, desc: '退款日期 yyyy-MM-dd', in: 'query' }
        ],
        requestExample: 'PUT /mk/return/refund/1?refundAmount=50000&refundDate=2026-07-20',
        responseExample: COMMON_RESPONSE.success,
        remark: '对已通过审批的退货执行退款操作。'
      }
    ]
  },

  {
    name: '编号规则',
    basePath: '/mk/numberRule',
    description: '营销模块编号规则配置：客户、线索、商机、合同、订单等单据的自动编号规则。',
    apis: [
      ...crud({
        basePath: '/mk/numberRule',
        title: '编号规则',
        idField: 'ruleId',
        permPrefix: 'marketing:numberRule',
        queryParams: [
          { name: 'ruleCode', type: 'String', required: false, desc: '规则编码' },
          { name: 'ruleName', type: 'String', required: false, desc: '规则名称' }
        ],
        bodyFields: [
          { name: 'ruleId', type: 'Long', required: false, desc: '规则ID' },
          { name: 'ruleCode', type: 'String', required: true, desc: '规则编码（唯一）' },
          { name: 'ruleName', type: 'String', required: true, desc: '规则名称' },
          { name: 'prefix', type: 'String', required: false, desc: '前缀' },
          { name: 'datePattern', type: 'String', required: false, desc: '日期格式' },
          { name: 'seqLength', type: 'Integer', required: false, desc: '序列号长度' },
          { name: 'resetType', type: 'String', required: false, desc: '重置类型：0=不重置 1=按年 2=按月 3=按日' },
          { name: 'status', type: 'String', required: false, desc: '状态：0=启用 1=停用' }
        ],
        exampleBody: `{\n  "ruleId": 1,\n  "ruleCode": "CUSTOMER",\n  "ruleName": "客户编号规则",\n  "prefix": "CUS",\n  "datePattern": "yyyyMMdd",\n  "seqLength": 4,\n  "resetType": "2",\n  "status": "0"\n}`
      }),
      {
        method: 'GET',
        path: '/mk/numberRule/preview/{ruleCode}',
        summary: '预览编号（不消耗序列号）',
        permission: 'marketing:numberRule:query',
        params: [{ name: 'ruleCode', type: 'String', required: true, desc: '规则编码', in: 'path' }],
        requestExample: 'GET /mk/numberRule/preview/CUSTOMER',
        responseExample: `{\n  "code": 200,\n  "data": "CUS20260718-0001"\n}`,
        remark: '根据规则编码预览下一个编号，不实际消耗序列号。'
      }
    ]
  },

  {
    name: '客户标签',
    basePath: '/mk/tag',
    description: '客户标签管理：用于客户分类和精准营销。',
    apis: crud({
      basePath: '/mk/tag',
      title: '客户标签',
      idField: 'tagId',
      permPrefix: 'marketing:tag',
      queryParams: [
        { name: 'tagName', type: 'String', required: false, desc: '标签名称' },
        { name: 'tagType', type: 'String', required: false, desc: '标签类型' },
        { name: 'status', type: 'String', required: false, desc: '状态：0=启用 1=停用' }
      ],
      bodyFields: [
        { name: 'tagId', type: 'Long', required: false, desc: '标签ID' },
        { name: 'tagName', type: 'String', required: true, desc: '标签名称' },
        { name: 'tagType', type: 'String', required: false, desc: '标签类型' },
        { name: 'color', type: 'String', required: false, desc: '标签颜色' },
        { name: 'status', type: 'String', required: false, desc: '状态：0=启用 1=停用' }
      ],
      exampleBody: `{\n  "tagId": 1,\n  "tagName": "VIP客户",\n  "tagType": "level",\n  "color": "#FF0000",\n  "status": "0"\n}`
    })
  },

  {
    name: '销售阶段配置',
    basePath: '/mk/stage',
    description: '商机销售阶段配置：阶段定义、顺序、赢率。',
    apis: crud({
      basePath: '/mk/stage',
      title: '销售阶段',
      idField: 'stageId',
      permPrefix: 'marketing:stage',
      queryParams: [
        { name: 'stageName', type: 'String', required: false, desc: '阶段名称' },
        { name: 'status', type: 'String', required: false, desc: '状态：0=启用 1=停用' }
      ],
      bodyFields: [
        { name: 'stageId', type: 'Long', required: false, desc: '阶段ID' },
        { name: 'stageCode', type: 'String', required: true, desc: '阶段编码' },
        { name: 'stageName', type: 'String', required: true, desc: '阶段名称' },
        { name: 'orderNum', type: 'Integer', required: false, desc: '排序号' },
        { name: 'winRate', type: 'BigDecimal', required: false, desc: '赢率（百分比）' },
        { name: 'status', type: 'String', required: false, desc: '状态：0=启用 1=停用' }
      ],
      exampleBody: `{\n  "stageId": 1,\n  "stageCode": "S1",\n  "stageName": "需求确认",\n  "orderNum": 1,\n  "winRate": 20,\n  "status": "0"\n}`
    })
  },

  {
    name: '统计看板',
    basePath: '/mk/dashboard',
    description: '营销管理统计分析：概览、漏斗、排行、趋势、预测。',
    apis: [
      {
        method: 'GET',
        path: '/mk/dashboard/overview',
        summary: '概览统计',
        permission: 'marketing:report:view',
        params: [],
        requestExample: 'GET /mk/dashboard/overview',
        responseExample: `{\n  "code": 200,\n  "data": {\n    "customerTotal": 500,\n    "leadTotal": 1200,\n    "opportunityTotal": 300,\n    "contractTotal": 150,\n    "orderTotal": 200,\n    "contractAmount": 50000000,\n    "orderAmount": 45000000,\n    "receivedAmount": 35000000\n  }\n}`,
        remark: '营销管理驾驶舱概览统计数据。'
      },
      {
        method: 'GET',
        path: '/mk/dashboard/leadFunnel',
        summary: '线索转化漏斗',
        permission: 'marketing:report:view',
        params: [],
        requestExample: 'GET /mk/dashboard/leadFunnel',
        responseExample: COMMON_RESPONSE.success,
        remark: '线索→客户→商机→合同→订单的转化漏斗数据。'
      },
      {
        method: 'GET',
        path: '/mk/dashboard/opportunityPipeline',
        summary: '商机销售漏斗',
        permission: 'marketing:report:view',
        params: [],
        requestExample: 'GET /mk/dashboard/opportunityPipeline',
        responseExample: COMMON_RESPONSE.success,
        remark: '按销售阶段分组的商机数量和金额统计。'
      },
      {
        method: 'GET',
        path: '/mk/dashboard/customerByIndustry',
        summary: '客户行业分布',
        permission: 'marketing:report:view',
        params: [],
        requestExample: 'GET /mk/dashboard/customerByIndustry',
        responseExample: COMMON_RESPONSE.success,
        remark: '按行业统计客户数量分布。'
      },
      {
        method: 'GET',
        path: '/mk/dashboard/customerByLevel',
        summary: '客户等级分布',
        permission: 'marketing:report:view',
        params: [],
        requestExample: 'GET /mk/dashboard/customerByLevel',
        responseExample: COMMON_RESPONSE.success,
        remark: '按客户等级统计客户数量分布。'
      },
      {
        method: 'GET',
        path: '/mk/dashboard/salesRanking',
        summary: '销售业绩排行',
        permission: 'marketing:report:view',
        params: [],
        requestExample: 'GET /mk/dashboard/salesRanking',
        responseExample: COMMON_RESPONSE.success,
        remark: '销售人员业绩排行榜。'
      },
      {
        method: 'GET',
        path: '/mk/dashboard/revenueTrend',
        summary: '合同/订单月度趋势',
        permission: 'marketing:report:view',
        params: [],
        requestExample: 'GET /mk/dashboard/revenueTrend',
        responseExample: COMMON_RESPONSE.success,
        remark: '合同金额和订单金额的月度趋势数据。'
      },
      {
        method: 'GET',
        path: '/mk/dashboard/activityResult',
        summary: '活动效果统计',
        permission: 'marketing:report:view',
        params: [],
        requestExample: 'GET /mk/dashboard/activityResult',
        responseExample: COMMON_RESPONSE.success,
        remark: '营销活动效果统计（参与人数、签到率、转化率等）。'
      },
      {
        method: 'GET',
        path: '/mk/dashboard/followUpReminders',
        summary: '待跟进联系人提醒',
        permission: 'marketing:contact:list',
        params: [],
        requestExample: 'GET /mk/dashboard/followUpReminders',
        responseExample: COMMON_RESPONSE.success,
        remark: '查询当前用户的待跟进联系人列表。'
      },
      {
        method: 'GET',
        path: '/mk/dashboard/winLossAnalysis',
        summary: '赢单输单分析',
        permission: 'marketing:report:view',
        params: [],
        requestExample: 'GET /mk/dashboard/winLossAnalysis',
        responseExample: COMMON_RESPONSE.success,
        remark: '商机赢单输单原因分析统计。'
      },
      {
        method: 'GET',
        path: '/mk/dashboard/salesForecast',
        summary: '销售预测',
        permission: 'marketing:report:view',
        params: [],
        requestExample: 'GET /mk/dashboard/salesForecast',
        responseExample: COMMON_RESPONSE.success,
        remark: '基于商机管线数据的销售预测。'
      },
      {
        method: 'GET',
        path: '/mk/dashboard/salespersonOverview',
        summary: '销售人员综合统计',
        permission: 'marketing:report:view',
        params: [],
        requestExample: 'GET /mk/dashboard/salespersonOverview',
        responseExample: COMMON_RESPONSE.success,
        remark: '各销售人员的综合业绩统计。'
      },
      {
        method: 'GET',
        path: '/mk/dashboard/salespersonTrend',
        summary: '销售人员月度业绩趋势',
        permission: 'marketing:report:view',
        params: [{ name: 'userId', type: 'Long', required: true, desc: '用户ID', in: 'query' }],
        requestExample: 'GET /mk/dashboard/salespersonTrend?userId=10',
        responseExample: COMMON_RESPONSE.success,
        remark: '查询指定销售人员的月度业绩趋势。'
      }
    ]
  },

  {
    name: '活动公开接口',
    basePath: '/mk/public',
    description: '活动报名H5公开接口（免登录、限流），用于外部报名和签到。',
    apis: [
      {
        method: 'GET',
        path: '/mk/public/activity/{activityId}',
        summary: '获取活动报名信息（公开）',
        permission: '匿名访问（限流：60秒/30次）',
        params: [
          { name: 'activityId', type: 'Long', required: true, desc: '活动ID', in: 'path' },
          { name: 'token', type: 'String', required: true, desc: '安全令牌', in: 'query' }
        ],
        requestExample: 'GET /mk/public/activity/1?token=abc123',
        responseExample: COMMON_RESPONSE.success,
        remark: '公开接口，通过安全令牌验证后返回活动报名信息，用于H5报名页面展示。'
      },
      {
        method: 'POST',
        path: '/mk/public/register',
        summary: '提交报名（公开）',
        permission: '匿名访问（限流：60秒/5次）',
        params: [
          { name: 'token', type: 'String', required: true, desc: '安全令牌', in: 'query' },
          { name: 'participantName', type: 'String', required: true, desc: '报名人姓名', in: 'body' },
          { name: 'phone', type: 'String', required: true, desc: '电话', in: 'body' },
          { name: 'email', type: 'String', required: false, desc: '邮箱', in: 'body' },
          { name: 'company', type: 'String', required: false, desc: '公司', in: 'body' }
        ],
        requestExample: `POST /mk/public/register?token=abc123\nContent-Type: application/json\n\n{\n  "participantName": "王五",\n  "phone": "13900139000",\n  "email": "wangwu@xx.com",\n  "company": "YY科技"\n}`,
        responseExample: COMMON_RESPONSE.success,
        remark: '公开报名接口，通过安全令牌验证后提交报名。限流：同一IP每60秒最多5次。'
      },
      {
        method: 'POST',
        path: '/mk/public/signIn',
        summary: '签到码签到（公开）',
        permission: '匿名访问（限流：60秒/10次）',
        params: [{ name: 'signInCode', type: 'String', required: true, desc: '签到码', in: 'query' }],
        requestExample: 'POST /mk/public/signIn?signInCode=SIGN001',
        responseExample: COMMON_RESPONSE.success,
        remark: '公开签到接口，通过签到码完成现场签到。限流：同一IP每60秒最多10次。'
      }
    ]
  }
]

// ================================================================
// 开放接口 (Open API) —— 供外部系统对接，通过 X-API-Key 鉴权
// ================================================================
const OPEN_API_WMS_MODULE = {
  name: 'WMS 开放接口',
  moduleCode: 'openWms',
  basePath: '/openapi/v1/wms',
  description: '仓库管理模块对外只读接口，路径前缀 /openapi/v1/wms，请求头需携带 X-API-Key。',
  apis: [
    {
      method: 'GET', path: '/openapi/v1/wms/material/list', summary: '查询物料列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'materialName', type: 'String', required: false, desc: '物料名称' }, { name: 'materialCode', type: 'String', required: false, desc: '物料编码' }],
      requestExample: `GET /openapi/v1/wms/material/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询物料列表，返回 TableDataInfo。'
    },
    {
      method: 'GET', path: '/openapi/v1/wms/material/{materialCode}', summary: '按编码查询物料',
      permission: 'API Key',
      params: [{ name: 'materialCode', type: 'String', required: true, desc: '物料编码（路径参数）' }],
      requestExample: `GET /openapi/v1/wms/material/M001\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据物料编码查询单条物料详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/wms/supplier/list', summary: '查询供应商列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'supplierName', type: 'String', required: false, desc: '供应商名称' }],
      requestExample: `GET /openapi/v1/wms/supplier/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询供应商列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/wms/supplier/{supplierId}', summary: '查询供应商详情',
      permission: 'API Key',
      params: [{ name: 'supplierId', type: 'Long', required: true, desc: '供应商ID（路径参数）' }],
      requestExample: `GET /openapi/v1/wms/supplier/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询供应商详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/wms/warehouse/list', summary: '查询仓库结构列表',
      permission: 'API Key',
      params: [{ name: 'warehouseName', type: 'String', required: false, desc: '仓库名称' }],
      requestExample: `GET /openapi/v1/wms/warehouse/list\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '查询仓库/库区/库位结构，不分页。'
    },
    {
      method: 'GET', path: '/openapi/v1/wms/warehouse/{warehouseId}', summary: '查询仓库详情',
      permission: 'API Key',
      params: [{ name: 'warehouseId', type: 'Long', required: true, desc: '仓库ID（路径参数）' }],
      requestExample: `GET /openapi/v1/wms/warehouse/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询仓库详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/wms/inbound/list', summary: '查询入库单列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'orderCode', type: 'String', required: false, desc: '入库单号' }, { name: 'status', type: 'String', required: false, desc: '状态' }],
      requestExample: `GET /openapi/v1/wms/inbound/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询入库单列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/wms/inbound/{orderId}', summary: '查询入库单详情',
      permission: 'API Key',
      params: [{ name: 'orderId', type: 'Long', required: true, desc: '入库单ID（路径参数）' }],
      requestExample: `GET /openapi/v1/wms/inbound/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询入库单详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/wms/outbound/list', summary: '查询出库单列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'orderCode', type: 'String', required: false, desc: '出库单号' }, { name: 'status', type: 'String', required: false, desc: '状态' }],
      requestExample: `GET /openapi/v1/wms/outbound/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询出库单列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/wms/outbound/{orderId}', summary: '查询出库单详情',
      permission: 'API Key',
      params: [{ name: 'orderId', type: 'Long', required: true, desc: '出库单ID（路径参数）' }],
      requestExample: `GET /openapi/v1/wms/outbound/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询出库单详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/wms/inventory/list', summary: '查询库存列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'materialCode', type: 'String', required: false, desc: '物料编码' }, { name: 'warehouseId', type: 'Long', required: false, desc: '仓库ID' }],
      requestExample: `GET /openapi/v1/wms/inventory/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询库存列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/wms/inventory/{inventoryId}', summary: '查询库存详情',
      permission: 'API Key',
      params: [{ name: 'inventoryId', type: 'Long', required: true, desc: '库存ID（路径参数）' }],
      requestExample: `GET /openapi/v1/wms/inventory/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询库存详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/wms/stockAlert/list', summary: '查询库存预警列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS],
      requestExample: `GET /openapi/v1/wms/stockAlert/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询库存预警（低于安全库存的物料）。'
    },
    {
      method: 'GET', path: '/openapi/v1/wms/stockAlert/summary', summary: '库存预警汇总',
      permission: 'API Key',
      params: [],
      requestExample: `GET /openapi/v1/wms/stockAlert/summary\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '查询库存预警汇总统计信息。'
    }
  ]
}

const OPEN_API_DMS_MODULE = {
  name: 'DMS 开放接口',
  moduleCode: 'openDms',
  basePath: '/openapi/v1/dms',
  description: '设备管理模块对外只读接口，路径前缀 /openapi/v1/dms，请求头需携带 X-API-Key。',
  apis: [
    {
      method: 'GET', path: '/openapi/v1/dms/equipment/list', summary: '查询设备列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'equipmentName', type: 'String', required: false, desc: '设备名称' }, { name: 'equipmentCode', type: 'String', required: false, desc: '设备编码' }],
      requestExample: `GET /openapi/v1/dms/equipment/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询设备台账列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/dms/equipment/{equipmentId}', summary: '查询设备详情',
      permission: 'API Key',
      params: [{ name: 'equipmentId', type: 'Long', required: true, desc: '设备ID（路径参数）' }],
      requestExample: `GET /openapi/v1/dms/equipment/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询设备台账详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/dms/category/list', summary: '查询设备分类列表',
      permission: 'API Key',
      params: [{ name: 'categoryName', type: 'String', required: false, desc: '分类名称' }],
      requestExample: `GET /openapi/v1/dms/category/list\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '查询设备分类树/列表，不分页。'
    },
    {
      method: 'GET', path: '/openapi/v1/dms/category/{categoryId}', summary: '查询设备分类详情',
      permission: 'API Key',
      params: [{ name: 'categoryId', type: 'Long', required: true, desc: '分类ID（路径参数）' }],
      requestExample: `GET /openapi/v1/dms/category/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询设备分类详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/dms/workorder/list', summary: '查询工单列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'orderCode', type: 'String', required: false, desc: '工单编号' }, { name: 'status', type: 'String', required: false, desc: '工单状态' }],
      requestExample: `GET /openapi/v1/dms/workorder/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询维修/保养工单列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/dms/workorder/{orderId}', summary: '查询工单详情',
      permission: 'API Key',
      params: [{ name: 'orderId', type: 'Long', required: true, desc: '工单ID（路径参数）' }],
      requestExample: `GET /openapi/v1/dms/workorder/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询工单详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/dms/sparepart/list', summary: '查询备件列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'partName', type: 'String', required: false, desc: '备件名称' }, { name: 'partCode', type: 'String', required: false, desc: '备件编码' }],
      requestExample: `GET /openapi/v1/dms/sparepart/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询备件列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/dms/sparepart/{partId}', summary: '查询备件详情',
      permission: 'API Key',
      params: [{ name: 'partId', type: 'Long', required: true, desc: '备件ID（路径参数）' }],
      requestExample: `GET /openapi/v1/dms/sparepart/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询备件详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/dms/sparepart/record/list', summary: '查询备件出入库记录',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'partId', type: 'Long', required: false, desc: '备件ID' }],
      requestExample: `GET /openapi/v1/dms/sparepart/record/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询备件出入库记录。'
    },
    {
      method: 'GET', path: '/openapi/v1/dms/sparepart/alert/list', summary: '查询备件库存预警',
      permission: 'API Key',
      params: [...PAGE_PARAMS],
      requestExample: `GET /openapi/v1/dms/sparepart/alert/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询库存低于安全库存的备件列表。'
    }
  ]
}

const OPEN_API_MK_MODULE = {
  name: 'MK 开放接口',
  moduleCode: 'openMk',
  basePath: '/openapi/v1/mk',
  description: '营销管理模块对外只读接口，路径前缀 /openapi/v1/mk，请求头需携带 X-API-Key。',
  apis: [
    {
      method: 'GET', path: '/openapi/v1/mk/customer/list', summary: '查询客户列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'customerName', type: 'String', required: false, desc: '客户名称' }, { name: 'customerStatus', type: 'String', required: false, desc: '客户状态' }],
      requestExample: `GET /openapi/v1/mk/customer/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询企业客户列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/customer/{customerId}', summary: '查询客户详情',
      permission: 'API Key',
      params: [{ name: 'customerId', type: 'Long', required: true, desc: '客户ID（路径参数）' }],
      requestExample: `GET /openapi/v1/mk/customer/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询客户详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/lead/list', summary: '查询线索列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'companyName', type: 'String', required: false, desc: '公司名称' }, { name: 'leadStatus', type: 'String', required: false, desc: '线索状态' }],
      requestExample: `GET /openapi/v1/mk/lead/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询线索列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/lead/{leadId}', summary: '查询线索详情',
      permission: 'API Key',
      params: [{ name: 'leadId', type: 'Long', required: true, desc: '线索ID（路径参数）' }],
      requestExample: `GET /openapi/v1/mk/lead/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询线索详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/opportunity/list', summary: '查询商机列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'opportunityName', type: 'String', required: false, desc: '商机名称' }, { name: 'opportunityStatus', type: 'String', required: false, desc: '商机状态' }],
      requestExample: `GET /openapi/v1/mk/opportunity/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询商机列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/opportunity/{opportunityId}', summary: '查询商机详情',
      permission: 'API Key',
      params: [{ name: 'opportunityId', type: 'Long', required: true, desc: '商机ID（路径参数）' }],
      requestExample: `GET /openapi/v1/mk/opportunity/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询商机详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/contract/list', summary: '查询合同列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'contractName', type: 'String', required: false, desc: '合同名称' }, { name: 'contractStatus', type: 'String', required: false, desc: '合同状态' }],
      requestExample: `GET /openapi/v1/mk/contract/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询合同列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/contract/{contractId}', summary: '查询合同详情',
      permission: 'API Key',
      params: [{ name: 'contractId', type: 'Long', required: true, desc: '合同ID（路径参数）' }],
      requestExample: `GET /openapi/v1/mk/contract/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询合同详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/order/list', summary: '查询订单列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'orderCode', type: 'String', required: false, desc: '订单编号' }, { name: 'orderStatus', type: 'String', required: false, desc: '订单状态' }],
      requestExample: `GET /openapi/v1/mk/order/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询订单列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/order/{orderId}', summary: '查询订单详情',
      permission: 'API Key',
      params: [{ name: 'orderId', type: 'Long', required: true, desc: '订单ID（路径参数）' }],
      requestExample: `GET /openapi/v1/mk/order/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询订单详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/contact/list', summary: '查询联系人列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'contactName', type: 'String', required: false, desc: '联系人姓名' }, { name: 'customerId', type: 'Long', required: false, desc: '客户ID' }],
      requestExample: `GET /openapi/v1/mk/contact/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询联系人列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/contact/{contactId}', summary: '查询联系人详情',
      permission: 'API Key',
      params: [{ name: 'contactId', type: 'Long', required: true, desc: '联系人ID（路径参数）' }],
      requestExample: `GET /openapi/v1/mk/contact/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询联系人详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/activity/list', summary: '查询活动列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'activityName', type: 'String', required: false, desc: '活动名称' }, { name: 'status', type: 'String', required: false, desc: '活动状态' }],
      requestExample: `GET /openapi/v1/mk/activity/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询营销活动列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/activity/{activityId}', summary: '查询活动详情',
      permission: 'API Key',
      params: [{ name: 'activityId', type: 'Long', required: true, desc: '活动ID（路径参数）' }],
      requestExample: `GET /openapi/v1/mk/activity/1\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '根据ID查询活动详情。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/interaction/list', summary: '查询互动记录列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'customerId', type: 'Long', required: false, desc: '客户ID' }, { name: 'interactionType', type: 'String', required: false, desc: '互动类型' }],
      requestExample: `GET /openapi/v1/mk/interaction/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询客户互动记录列表。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/dashboard/overview', summary: '营销概览统计',
      permission: 'API Key',
      params: [],
      requestExample: `GET /openapi/v1/mk/dashboard/overview\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '营销管理概览统计数据（客户数、线索数、商机数、合同金额等）。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/dashboard/leadFunnel', summary: '线索转化漏斗',
      permission: 'API Key',
      params: [],
      requestExample: `GET /openapi/v1/mk/dashboard/leadFunnel\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '线索转化漏斗数据。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/dashboard/opportunityPipeline', summary: '商机销售漏斗',
      permission: 'API Key',
      params: [],
      requestExample: `GET /openapi/v1/mk/dashboard/opportunityPipeline\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '商机销售漏斗数据。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/dashboard/salesRanking', summary: '销售业绩排行',
      permission: 'API Key',
      params: [],
      requestExample: `GET /openapi/v1/mk/dashboard/salesRanking\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '销售人员业绩排行榜。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/dashboard/revenueTrend', summary: '收入月度趋势',
      permission: 'API Key',
      params: [],
      requestExample: `GET /openapi/v1/mk/dashboard/revenueTrend\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '合同和订单金额的月度趋势。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/payment/statistics', summary: '回款统计',
      permission: 'API Key',
      params: [],
      requestExample: `GET /openapi/v1/mk/payment/statistics\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.success,
      remark: '回款统计：总计划金额、已回款金额、待回款金额、逾期金额。'
    },
    {
      method: 'GET', path: '/openapi/v1/mk/payment/list', summary: '查询回款计划列表',
      permission: 'API Key',
      params: [...PAGE_PARAMS, { name: 'customerId', type: 'Long', required: false, desc: '客户ID' }, { name: 'planStatus', type: 'String', required: false, desc: '计划状态' }],
      requestExample: `GET /openapi/v1/mk/payment/list?pageNum=1&pageSize=10\nHeader: X-API-Key: ak-xxxx`,
      responseExample: COMMON_RESPONSE.table,
      remark: '分页查询回款计划列表。'
    }
  ]
}

const OPEN_API_MODULES = [OPEN_API_WMS_MODULE, OPEN_API_DMS_MODULE, OPEN_API_MK_MODULE]

export const API_MODULES = [
  {
    group: '仓库管理模块 (WMS)',
    groupCode: 'wms',
    description: '仓库管理(WMS)模块接口，涵盖仓库结构、物料、供应商、出入库、移库、盘点、库存、波次、统计、预警等全套功能。',
    modules: WMS_MODULES
  },
  {
    group: '设备管理模块 (DMS)',
    groupCode: 'dms',
    description: '设备管理(DMS)模块接口，涵盖设备台账、分类、工单、PM计划、巡检、备件、数据采集、看板、AI智能等全套功能。',
    modules: DMS_MODULES
  },
  {
    group: '营销管理模块 (MK)',
    groupCode: 'mk',
    description: '营销管理(MK)模块接口，涵盖客户、线索、商机、合同、订单、联系人、活动、互动记录、发票、回款、退货、编号规则、标签、销售阶段、统计看板等全套功能。',
    modules: MK_MODULES
  }
]

// 统计信息
export function getStats() {
  let moduleCount = 0
  let apiCount = 0
  const methodCount = { GET: 0, POST: 0, PUT: 0, DELETE: 0 }
  API_MODULES.forEach(g => {
    g.modules.forEach(m => {
      moduleCount++
      m.apis.forEach(a => {
        apiCount++
        if (methodCount[a.method]) methodCount[a.method]++
      })
    })
  })
  return { moduleCount, apiCount, methodCount }
}

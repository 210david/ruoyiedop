/**
 * 流程表单「浏览按钮」数据源注册表（统一的浏览按钮）
 *
 * 一个数据源 = 一个已有的弹框选择器组件 + 「打开时传什么」「确认后取什么」的口径。
 * 新增数据源只需在此登记，无需改表单渲染逻辑：
 * label 下拉/按钮上显示的中文名
 * component 弹框选择器组件（懒加载，避免首屏把所有 Picker 都打进来）
 * multiple 是否支持多选（配置面板据此决定「多选」开关是否可用）
 * alwaysMultiple 强制多选（如角色选择器本身只支持多选，开关置灰且恒为开）
 * buildProps (f) => 传给选择器的 props
 * openArgs (ids, f, names) => 调用选择器 open() 的参数数组
 * pick (item) => { id, name }，把各选择器回调对象归一化为「ID + 名称」
 *
 * 存储口径：字段值存「名称」（多选用「、」连接），可直接用于详情/打印/列表展示；
 * 同时在 ${field}__id 里存 ID（逗号分隔），用于再次打开弹框时回显高亮。
 * 两者由 FormBrowse 同时写入、同时清空。
 */
import { defineAsyncComponent } from 'vue';

/** 数字型 ID 转换（各 Picker 的 open() 回显用严格相等匹配，必须传数字） */
const num = (v) => {
  if (v === undefined || v === null || v === '') return undefined;
  const n = Number(v);
  return Number.isNaN(n) ? v : n;
};

/** 默认单选打开参数 */
const singleArg = (ids) => (ids.length ? [num(ids[0])] : []);
/** 默认多选打开参数 */
const multiArg = (ids) => [ids.map(num)];

export const BROWSE_SOURCES = {
  user: {
    label: '人员',
    multiple: true,
    component: defineAsyncComponent(() => import('@/components/UserPicker/index.vue')),
    buildProps: (f) => ({ title: '选择人员', multiple: !!f.browseMultiple, scope: 'all' }),
    openArgs: (ids, f) => (f.browseMultiple ? multiArg(ids) : singleArg(ids)),
    // 人员额外带回所属部门：表单里「部门」字段配「随人员字段带出」时直接取用，不再查接口
    pick: (u) => ({ id: u.userId, name: u.nickName || u.userName, deptId: u.deptId, deptName: u.deptName }),
  },
  dept: {
    label: '部门',
    multiple: true,
    component: defineAsyncComponent(() => import('@/components/DeptPicker/index.vue')),
    buildProps: (f) => ({ title: '选择部门', multiple: !!f.browseMultiple }),
    openArgs: (ids, f) => (f.browseMultiple ? multiArg(ids) : singleArg(ids)),
    pick: (d) => ({ id: d.deptId, name: d.deptName }),
  },
  post: {
    label: '岗位',
    multiple: true,
    component: defineAsyncComponent(() => import('@/components/PositionPicker/index.vue')),
    buildProps: (f) => ({ title: '选择岗位', multiple: !!f.browseMultiple }),
    openArgs: (ids, f) => (f.browseMultiple ? multiArg(ids) : singleArg(ids)),
    // 单选回调是 positionXxx，多选回调是 postXxx，两种都兼容
    pick: (p) => ({ id: p.positionId !== undefined ? p.positionId : p.postId, name: p.positionName || p.postName }),
  },
  role: {
    label: '角色',
    multiple: true,
    alwaysMultiple: true,
    component: defineAsyncComponent(() => import('@/components/RolePicker/index.vue')),
    buildProps: () => ({ title: '选择角色' }),
    openArgs: (ids) => multiArg(ids),
    pick: (r) => ({ id: r.roleId, name: r.roleName }),
  },
  customer: {
    label: '客户',
    component: defineAsyncComponent(() => import('@/components/CustomerPicker/index.vue')),
    buildProps: () => ({ title: '选择客户' }),
    openArgs: (ids, f, names) => (ids.length ? [num(ids[0]), names[0] || ''] : []),
    pick: (c) => ({ id: c.customerId, name: c.customerName }),
  },
  supplier: {
    label: '供应商',
    component: defineAsyncComponent(() => import('@/components/SupplierPicker/index.vue')),
    buildProps: () => ({ title: '选择供应商' }),
    openArgs: singleArg,
    pick: (s) => ({ id: s.supplierId, name: s.supplierName }),
  },
  material: {
    label: '物料',
    component: defineAsyncComponent(() => import('@/components/MaterialPicker/index.vue')),
    buildProps: () => ({ title: '选择物料' }),
    openArgs: singleArg,
    pick: (m) => ({ id: m.materialId, name: m.materialName }),
  },
  equipment: {
    label: '设备',
    multiple: true,
    component: defineAsyncComponent(() => import('@/components/EquipmentPicker/index.vue')),
    buildProps: (f) => ({ title: '选择设备', multiple: !!f.browseMultiple }),
    // 设备多选的回显参数是「已选行数组」而不是 ID 数组，这里用 ID + 名称拼出来
    openArgs: (ids, f, names) => {
      if (!f.browseMultiple) return singleArg(ids);
      const rows = ids.map((id, i) => ({ equipmentId: num(id), equipmentName: names[i] || '' }));
      return [null, rows];
    },
    pick: (e) => ({ id: e.equipmentId, name: e.equipmentName || e.equipmentCode }),
  },
  contract: {
    label: '合同',
    component: defineAsyncComponent(() => import('@/components/ContractPicker/index.vue')),
    buildProps: () => ({ title: '选择合同' }),
    openArgs: singleArg,
    pick: (c) => ({ id: c.contractId, name: c.contractName || c.contractNo }),
  },
  warehouse: {
    label: '仓库',
    component: defineAsyncComponent(() => import('@/components/WarehousePicker/index.vue')),
    buildProps: () => ({ title: '选择仓库' }),
    openArgs: singleArg,
    pick: (w) => ({ id: w.warehouseId, name: w.warehouseName }),
  },
  part: {
    label: '备件',
    component: defineAsyncComponent(() => import('@/components/PartPicker/index.vue')),
    buildProps: () => ({ title: '选择备件' }),
    openArgs: singleArg,
    pick: (p) => ({ id: p.partId, name: p.partName || p.partCode }),
  },
  product: {
    label: '产品',
    component: defineAsyncComponent(() => import('@/components/PdmProductPicker/index.vue')),
    buildProps: () => ({ title: '选择产品' }),
    openArgs: singleArg,
    pick: (p) => ({ id: p.productId, name: p.productName || p.productCode }),
  },
  project: {
    label: '项目',
    component: defineAsyncComponent(() => import('@/components/BidProjectPicker/index.vue')),
    buildProps: () => ({ title: '选择项目' }),
    openArgs: singleArg,
    pick: (p) => ({ id: p.projectId, name: p.projectName || p.bidNo }),
  },
};

/** 字段 → 数据源定义（未配置或已下线时返回 null，由组件降级为普通输入框） */
export function browseSourceOf(f) {
  return BROWSE_SOURCES[(f && f.browseType) || 'user'] || null;
}

/** 数据源 key → 中文名（配置面板下拉用） */
export const BROWSE_TYPES = Object.keys(BROWSE_SOURCES).reduce((m, k) => {
  m[k] = BROWSE_SOURCES[k].label;
  return m;
}, {});

/** 数据源是否支持多选 */
export function browseSupportsMultiple(type) {
  const s = BROWSE_SOURCES[type || 'user'];
  return !!(s && s.multiple);
}

/** 该数据源是否强制多选（配置面板的开关需置灰且恒为开） */
export function browseForceMultiple(type) {
  const s = BROWSE_SOURCES[type || 'user'];
  return !!(s && s.alwaysMultiple);
}

/** 浏览按钮默认占位文案 */
export function browsePlaceholder(f) {
  const s = browseSourceOf(f);
  return s ? '请选择' + s.label : '请选择数据';
}

/** 浏览按钮的数据源名称（设计器/属性面板展示用） */
export function browseLabel(f) {
  const s = browseSourceOf(f);
  return s ? s.label : '未配置数据源';
}

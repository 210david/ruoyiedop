<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="规则编码" prop="ruleCode">
        <el-input v-model="queryParams.ruleCode" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model="queryParams.ruleName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in sys_normal_disable" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:numberRule:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:numberRule:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:numberRule:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="规则编码" prop="ruleCode" width="160" />
      <el-table-column label="所属模块" prop="module" width="100" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.module === 'mk'" type="primary" size="small">营销管理</el-tag>
          <el-tag v-else-if="scope.row.module === 'dms'" type="warning" size="small">设备管理</el-tag>
          <el-tag v-else-if="scope.row.module === 'wms'" type="success" size="small">仓储管理</el-tag>
          <span v-else>{{ scope.row.module }}</span>
        </template>
      </el-table-column>
      <el-table-column label="规则名称" prop="ruleName" width="140" />
      <el-table-column label="前缀" prop="prefix" width="80" align="center">
        <template #default="scope">
          <span v-if="scope.row.prefix">{{ scope.row.prefix }}</span>
          <el-tag v-else-if="scope.row.prefixFieldEnabled === '1'" type="success" size="small">动态</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="日期格式" prop="dateFormat" width="120" align="center">
        <template #default="scope">
          <span v-if="scope.row.dateFormat">{{ scope.row.dateFormat }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="重置类型" prop="resetType" width="100" align="center">
        <template #default="scope"><dict-tag :options="mk_number_reset_type" :value="scope.row.resetType" /></template>
      </el-table-column>
      <el-table-column label="序列号长度" prop="seqLength" width="100" align="center" />
      <el-table-column label="当前序列号" prop="currentSeq" width="100" align="center" />
      <el-table-column label="预览编号" prop="preview" width="200">
        <template #default="scope">
          <el-tag type="primary">{{ scope.row.preview }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="80" align="center">
        <template #default="scope"><dict-tag :options="sys_normal_disable" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['marketing:numberRule:query']">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:numberRule:edit']">修改</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改/查看 对话框 -->
    <el-dialog :title="title" v-model="open" width="750px" append-to-body>
      <el-form ref="ruleRef" :model="form" :rules="rules" label-width="120px" :disabled="isView">
        <el-collapse v-model="activeNames">
          <!-- 分组一：基本信息 -->
          <el-collapse-item title="基本信息" name="basic">
            <el-row>
              <el-col :span="12">
                <el-form-item label="所属模块" prop="module">
                  <el-select v-model="form.module" placeholder="请选择" :disabled="form.ruleId != undefined" style="width: 100%">
                    <el-option label="营销管理" value="mk" />
                    <el-option label="设备管理" value="dms" />
                    <el-option label="仓储管理" value="wms" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="规则编码" prop="ruleCode">
                  <el-input v-model="form.ruleCode" placeholder="如：activity" :disabled="form.ruleId != undefined" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="规则名称" prop="ruleName">
                  <el-input v-model="form.ruleName" placeholder="如：活动编号" :disabled="form.ruleId != undefined" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status">
                    <el-radio v-for="d in sys_normal_disable" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>

          <!-- 分组二：编号格式 -->
          <el-collapse-item title="编号格式" name="format">
            <el-row>
              <el-col :span="24">
                <el-form-item label="序号重置类型" prop="resetType">
                  <el-radio-group v-model="form.resetType">
                    <el-radio v-for="d in mk_number_reset_type" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="编号前缀" prop="prefix">
                  <el-input v-model="form.prefix" placeholder="如：HD" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="连接符" prop="connector">
                  <el-input v-model="form.connector" placeholder="前缀与日期间连接符，如：-" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="日期格式" prop="dateFormat">
                  <el-select v-model="form.dateFormat" placeholder="请选择" :disabled="form.resetType !== '0' && form.resetType !== '4'" style="width: 100%">
                    <el-option label="不包含日期" value="" />
                    <el-option label="yyyy (按年)" value="yyyy" />
                    <el-option label="yyyyMM (按月)" value="yyyyMM" />
                    <el-option label="yyyyMMdd (按天)" value="yyyyMMdd" />
                  </el-select>
                  <span v-if="form.resetType !== '0' && form.resetType !== '4'" style="color: #909399; font-size: 12px;">由重置类型自动决定，不可手动修改</span>
                  <span v-if="form.resetType === '4'" style="color: #909399; font-size: 12px;">按动态前缀重置时可自由选择日期格式，每个前缀维护独立序号</span>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>

          <!-- 分组三：动态前缀配置 -->
          <el-collapse-item title="动态前缀配置" name="prefix">
            <el-row>
              <el-col :span="24">
                <el-form-item label="启用动态前缀" prop="prefixFieldEnabled">
                  <el-switch v-model="form.prefixFieldEnabled" active-value="1" inactive-value="0" />
                  <span style="margin-left: 10px; color: #909399; font-size: 12px;">启用后可根据业务字段值（如物料类型）自动设置不同前缀</span>
                </el-form-item>
              </el-col>
              <template v-if="form.prefixFieldEnabled === '1'">
                <el-col :span="12">
                  <el-form-item label="关联字段名" prop="prefixField">
                    <el-select v-model="form.prefixField" placeholder="请选择关联字段" style="width: 100%" @change="onPrefixFieldChange">
                      <el-option v-for="item in availablePrefixFields" :key="item.field" :label="`${item.label} (${item.field})`" :value="item.field" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="关联字典类型" prop="prefixFieldDictType">
                    <el-input v-model="form.prefixFieldDictType" placeholder="选择关联字段后自动带出" readonly />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="前缀映射">
                    <el-table :data="form.prefixList" border size="small" style="width: 100%">
                      <el-table-column label="字段值" prop="fieldValue" width="100" align="center">
                        <template #default="scope">
                          <span>{{ scope.row.fieldValue }}</span>
                        </template>
                      </el-table-column>
                      <el-table-column label="字段标签" prop="fieldLabel" width="120" align="center">
                        <template #default="scope">
                          <span>{{ scope.row.fieldLabel }}</span>
                        </template>
                      </el-table-column>
                      <el-table-column label="前缀" prop="prefix" align="center">
                        <template #default="scope">
                          <el-input v-model="scope.row.prefix" placeholder="如：YL" size="small" />
                        </template>
                      </el-table-column>
                      <el-table-column label="当前序号" prop="currentSeq" width="90" align="center">
                        <template #default="scope">
                          <span>{{ scope.row.currentSeq || 0 }}</span>
                        </template>
                      </el-table-column>
                      <el-table-column label="启用" prop="enabled" width="80" align="center">
                        <template #default="scope">
                          <el-switch v-model="scope.row.enabled" active-value="1" inactive-value="0" size="small" />
                        </template>
                      </el-table-column>
                    </el-table>
                  </el-form-item>
                </el-col>
              </template>
            </el-row>
          </el-collapse-item>

          <!-- 分组四：序列号设置 -->
          <el-collapse-item title="序列号设置" name="seq">
            <el-row>
              <el-col :span="12">
                <el-form-item label="序列号长度" prop="seqLength">
                  <el-input-number v-model="form.seqLength" :min="1" :max="10" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="序列号起始值" prop="seqStart">
                  <el-input-number v-model="form.seqStart" :min="1" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="增长步长" prop="step">
                  <el-input-number v-model="form.step" :min="1" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="当前序列号">
                  <el-input-number v-model="form.currentSeq" :min="0" controls-position="right" style="width: 100%" :disabled="true" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>

          <!-- 分组五：预览与备注 -->
          <el-collapse-item title="预览与备注" name="preview">
            <el-row>
              <el-col :span="24">
                <el-form-item label="预览编号">
                  <el-tag type="primary" size="large">{{ previewText }}</el-tag>
                  <el-button link type="primary" style="margin-left: 10px" @click="refreshPreview" :disabled="!form.ruleCode || isView">刷新预览</el-button>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="备注" prop="remark">
                  <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm" v-if="!isView">确 定</el-button>
        <el-button @click="cancel">{{ isView ? '关 闭' : '取 消' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkNumberRule">
import { useRoute } from 'vue-router'
import { listNumberRule, getNumberRule, addNumberRule, updateNumberRule } from '@/api/mk/numberRule'
import { getDicts } from '@/api/system/dict/data'

const route = useRoute()
const { proxy } = getCurrentInstance()
const { mk_number_reset_type, sys_normal_disable } = proxy.useDict('mk_number_reset_type', 'sys_normal_disable')

/** 从路由参数获取当前模块（mk/dms/wms），默认为 mk */
const currentModule = computed(() => route.query.module || 'mk')

/** 模块名称映射 */
const moduleNames = { mk: '营销管理', dms: '设备管理', wms: '仓储管理' }
const currentModuleName = computed(() => moduleNames[currentModule.value] || '编号规则')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const total = ref(0)
const title = ref('')
const previewText = ref('')
const isView = ref(false)
const activeNames = ref(['basic', 'format', 'prefix', 'seq', 'preview'])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, ruleCode: undefined, ruleName: undefined, status: undefined },
  rules: {
    module: [{ required: true, message: '请选择所属模块', trigger: 'change' }],
    ruleCode: [{ required: true, message: '规则编码不能为空', trigger: 'blur' }],
    ruleName: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
    prefix: [],
    seqLength: [{ required: true, message: '序列号长度不能为空', trigger: 'blur' }],
    resetType: [{ required: true, message: '请选择重置类型', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 设置页面标题 */
useHead()

function getList() {
  loading.value = true
  queryParams.value.module = currentModule.value
  listNumberRule(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value.module = currentModule.value
  handleQuery()
}
function handleSelectionChange(selection) { ids.value = selection.map(i => i.ruleId); single.value = selection.length !== 1 }

/** 根据表单数据生成本地预览编号 */
function buildLocalPreview() {
  const fixedPrefix = form.value.prefix || ''
  // 启用动态前缀时，取第一个启用的前缀作为预览示例
  let dynamicPrefix = ''
  if (form.value.prefixFieldEnabled === '1' && form.value.prefixList && form.value.prefixList.length > 0) {
    const firstEnabled = form.value.prefixList.find(p => p.enabled === '1' && p.prefix)
    if (firstEnabled) {
      dynamicPrefix = firstEnabled.prefix
    }
  }
  const connector = form.value.connector || ''
  // 拼装编号：固定前缀 + 连接符 + 动态前缀 + 连接符 + 日期 + 序列号
  let str = ''
  if (fixedPrefix) str += fixedPrefix
  if (dynamicPrefix) {
    if (fixedPrefix && connector) str += connector
    str += dynamicPrefix
  }
  let dateStr = ''
  const resetType = form.value.resetType || '0'
  const dateFormat = form.value.dateFormat || ''
  if (resetType === '1') dateStr = formatNow('yyyyMMdd')
  else if (resetType === '2') dateStr = formatNow('yyyyMM')
  else if (resetType === '3') dateStr = formatNow('yyyy')
  else if (dateFormat) dateStr = formatNow(dateFormat)
  if (dateStr) {
    if ((fixedPrefix || dynamicPrefix) && connector) str += connector
    str += dateStr
  }
  const seqLen = form.value.seqLength || 4
  const startVal = form.value.seqStart || 1
  str += String(startVal).padStart(seqLen, '0')
  previewText.value = str
}

function formatNow(pattern) {
  const now = new Date()
  const map = { yyyy: now.getFullYear(), MM: String(now.getMonth() + 1).padStart(2, '0'), dd: String(now.getDate()).padStart(2, '0') }
  return pattern.replace(/yyyy|MM|dd/g, m => map[m])
}

/** 刷新预览：始终用本地表单数据实时计算 */
function refreshPreview() {
  buildLocalPreview()
}

function reset() {
  form.value = { module: currentModule.value, ruleCode: undefined, ruleName: undefined, prefix: undefined, dateFormat: 'yyyyMMdd', resetType: '1', seqLength: 4, seqStart: 1, step: 1, connector: '', status: '0', prefixFieldEnabled: '0', prefixField: undefined, prefixFieldDictType: undefined, prefixList: [], remark: undefined }
  previewText.value = ''
  proxy.resetForm('ruleRef')
}

function useHead() {
  watchEffect(() => {
    document.title = `${currentModuleName.value} - 编号规则`
  })
}

function handleAdd() { reset(); isView.value = false; open.value = true; title.value = `新增${currentModuleName.value}编号规则` }
function handleUpdate(row) {
  reset()
  isView.value = false
  getNumberRule(row.ruleId || ids.value[0]).then(res => {
    form.value = res.data
    open.value = true
    title.value = `修改${res.data.ruleName}`
    nextTick(() => { buildLocalPreview() })
  })
}
function handleView(row) {
  reset()
  isView.value = true
  getNumberRule(row.ruleId).then(res => {
    form.value = res.data
    open.value = true
    title.value = `查看${res.data.ruleName}`
    nextTick(() => { buildLocalPreview() })
  })
}
function submitForm() {
  proxy.$refs['ruleRef'].validate(valid => {
    if (valid) {
      // 按动态前缀重置时，必须启用动态前缀并选择关联字段
      if (form.value.resetType === '4') {
        if (form.value.prefixFieldEnabled !== '1') {
          proxy.$modal.msgError('序号重置类型为"按动态前缀"时，必须启用动态前缀配置')
          return
        }
        if (!form.value.prefixField) {
          proxy.$modal.msgError('请选择动态前缀的关联字段名')
          return
        }
        if (!form.value.prefixList || form.value.prefixList.length === 0) {
          proxy.$modal.msgError('按动态前缀重置时，至少需要配置一个前缀映射')
          return
        }
      }
      if (form.value.ruleId != undefined) {
        updateNumberRule(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addNumberRule(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}
/** 规则编码 → 该业务表可选的字典字段列表 */
const ruleFieldMap = {
  // 物料：物料类型、计量单位
  wms_material: [
    { field: 'materialType', label: '物料类型', dictType: 'wms_material_type' },
    { field: 'unit', label: '计量单位', dictType: 'wms_unit' }
  ],
  // 仓库：仓库类型、库区类型、仓位类型
  wms_warehouse: [
    { field: 'areaType', label: '库区类型', dictType: 'wms_area_type' },
    { field: 'locationType', label: '仓位类型', dictType: 'wms_location_type' }
  ],
  // 供应商：供应商类型
  wms_supplier: [
    { field: 'supplierType', label: '供应商类型', dictType: 'wms_supplier_type' }
  ],
  // 入库单：入库类型
  wms_inbound: [
    { field: 'inboundType', label: '入库类型', dictType: 'wms_inbound_type' }
  ],
  // 出库单：出库类型
  wms_outbound: [
    { field: 'outboundType', label: '出库类型', dictType: 'wms_outbound_type' }
  ],
  // 设备：设备状态
  dms_equipment: [
    { field: 'equipmentStatus', label: '设备状态', dictType: 'dms_equipment_status' }
  ],
  // 备件：备件类型、计量单位
  dms_spare_part: [
    { field: 'partType', label: '备件类型', dictType: 'dms_part_type' },
    { field: 'unit', label: '计量单位', dictType: 'wms_unit' }
  ],
  // 工单：工单类型、优先级
  dms_work_order: [
    { field: 'orderType', label: '工单类型', dictType: 'dms_order_type' },
    { field: 'priority', label: '优先级', dictType: 'dms_priority' }
  ],
  // 备件入库：入库类型
  dms_spare_in: [
    { field: 'partinType', label: '入库类型', dictType: 'dms_partin_type' }
  ],
  // 备件出库：出库类型
  dms_spare_out: [
    { field: 'partoutType', label: '出库类型', dictType: 'dms_partout_type' }
  ],
  // 巡检路线：巡检周期
  dms_inspection_route: [
    { field: 'cycle', label: '巡检周期', dictType: 'dms_inspection_cycle' }
  ]
}

/** 根据当前规则编码计算可选的关联字段 */
const availablePrefixFields = computed(() => {
  if (!form.value.ruleCode) return []
  return ruleFieldMap[form.value.ruleCode] || []
})

/** 关联字段名变化时，自动设置字典类型并加载前缀映射 */
function onPrefixFieldChange(val) {
  // 根据选中的字段找到对应的字典类型
  const fieldConfig = availablePrefixFields.value.find(f => f.field === val)
  form.value.prefixFieldDictType = fieldConfig ? fieldConfig.dictType : ''
  // 自动加载前缀映射
  if (form.value.prefixFieldDictType) {
    loadDictToPrefixList()
  } else {
    form.value.prefixList = []
  }
}

/** 从字典加载选项到前缀映射列表 */
function loadDictToPrefixList() {
  if (!form.value.prefixFieldDictType) {
    return
  }
  getDicts(form.value.prefixFieldDictType).then(res => {
    const dictData = res.data || []
    const existingMap = {}
    if (form.value.prefixList) {
      form.value.prefixList.forEach(item => {
        existingMap[item.fieldValue] = item
      })
    }
    form.value.prefixList = dictData.map(d => {
      const existing = existingMap[d.dictValue]
      return {
        fieldValue: d.dictValue,
        fieldLabel: d.dictLabel,
        prefix: existing ? existing.prefix : '',
        enabled: existing ? existing.enabled : '1'
      }
    })
  })
}

function handleExport() { proxy.download('mk/numberRule/export', { ...queryParams.value }, `numberRule_${currentModule.value}_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset(); isView.value = false }

// 监听重置类型变化，自动设置日期格式
watch(() => form.value.resetType, (newVal) => {
  if (newVal === '0') {
    form.value.dateFormat = ''
  } else if (newVal === '1') {
    form.value.dateFormat = 'yyyyMMdd'
  } else if (newVal === '2') {
    form.value.dateFormat = 'yyyyMM'
  } else if (newVal === '3') {
    form.value.dateFormat = 'yyyy'
  } else if (newVal === '4') {
    // 按动态前缀重置：默认不包含日期，用户可自行选择
    form.value.dateFormat = ''
  }
})

// 监听表单变化以实时更新预览
watch(() => form.value, () => { buildLocalPreview() }, { deep: true })

// 监听路由参数变化，重新加载列表
watch(() => route.query.module, () => { getList() })

getList()
</script>

<template>
  <div class="app-container wms-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>政策编号</label>
          <div class="control">
            <el-input v-model="queryParams.policyNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>政策名称</label>
          <div class="control">
            <el-input v-model="queryParams.policyName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>价格类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.priceType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mk_price_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>客户名称</label>
          <div class="control">
            <el-input v-model="queryParams.customerName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:price:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['marketing:price:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['marketing:price:remove']">删除</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:price:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pricePolicy_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="policyList" border class="app-table" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 60)" resizable align="center" />
          <el-table-column label="政策编号" prop="policyNo" key="policyNo" :width="colWidth('policyNo', 150)" resizable v-if="columns.policyNo.visible">
            <template #default="scope"><span class="mono">{{ scope.row.policyNo }}</span></template>
          </el-table-column>
          <el-table-column label="政策名称" prop="policyName" key="policyName" :min-width="colWidth('policyName', 160)" resizable show-overflow-tooltip v-if="columns.policyName.visible" />
          <el-table-column label="价格类型" prop="priceType" key="priceType" :width="colWidth('priceType', 110)" resizable align="center" v-if="columns.priceType.visible">
            <template #default="scope">
              <span class="badge" :class="priceTypeBadgeClass(scope.row.priceType)"><span class="dot"></span>{{ dictLabel(mk_price_type, scope.row.priceType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="客户名称" prop="customerName" key="customerName" :width="colWidth('customerName', 160)" resizable show-overflow-tooltip v-if="columns.customerName.visible">
            <template #default="scope">{{ scope.row.customerName || '—' }}</template>
          </el-table-column>
          <el-table-column label="客户等级" prop="customerLevel" key="customerLevel" :width="colWidth('customerLevel', 100)" resizable align="center" v-if="columns.customerLevel.visible">
            <template #default="scope"><dict-tag v-if="scope.row.customerLevel" :options="marketing_customer_level" :value="scope.row.customerLevel" /><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="物料名称" prop="materialName" key="materialName" :min-width="colWidth('materialName', 150)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="政策单价" prop="unitPrice" key="unitPrice" :width="colWidth('unitPrice', 110)" resizable align="center" v-if="columns.unitPrice.visible">
            <template #default="scope"><span class="mono">{{ scope.row.unitPrice }}</span></template>
          </el-table-column>
          <el-table-column label="生效日期" prop="startDate" key="startDate" :width="colWidth('startDate', 110)" resizable align="center" v-if="columns.startDate.visible">
            <template #default="scope">{{ scope.row.startDate || '—' }}</template>
          </el-table-column>
          <el-table-column label="失效日期" prop="endDate" key="endDate" :width="colWidth('endDate', 110)" resizable align="center" v-if="columns.endDate.visible">
            <template #default="scope">{{ scope.row.endDate || '—' }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '启用' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="colWidth('操作', 140)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:price:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:price:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="table-pager">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- ===== 新增/修改弹框 ===== -->
    <el-dialog v-model="open" width="680px" append-to-body draggable class="rd-dialog" @closed="cancel">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="policyRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>政策信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="价格类型" prop="priceType">
                    <el-radio-group v-model="form.priceType" @change="onPriceTypeChange">
                      <el-radio value="1">客户专价</el-radio>
                      <el-radio value="2">等级价</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col v-if="form.priceType === '1'" :span="24">
                  <el-form-item label="客户" prop="customerId">
                    <el-input :model-value="form.customerName" readonly placeholder="点击选择客户（客户专价必选）" @click="openCustomerPicker">
                      <template v-if="form.customerId" #append><el-button icon="CircleClose" @click.stop="clearCustomer" /></template>
                      <template v-else #append><el-button icon="Search" @click="openCustomerPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col v-if="form.priceType === '2'" :span="24">
                  <el-form-item label="客户等级" prop="customerLevel">
                    <el-select v-model="form.customerLevel" placeholder="请选择客户等级（等级价必选）" clearable style="width: 100%">
                      <el-option v-for="d in marketing_customer_level" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="政策名称" prop="policyName">
                    <el-input v-model="form.policyName" placeholder="如：A客户专价/一级客户价" maxlength="100" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="政策单价" prop="unitPrice">
                    <el-input-number v-model="form.unitPrice" :precision="4" :min="0" :step="0.01" controls-position="right" style="width: 100%" placeholder="请输入政策单价" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>适用物料与有效期</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="物料" prop="materialId">
                    <el-input :model-value="form.materialName" readonly placeholder="点击选择物料" @click="openMaterialPicker">
                      <template v-if="form.materialId" #append><el-button icon="CircleClose" @click.stop="clearMaterial" /></template>
                      <template v-else #append><el-button icon="Search" @click="openMaterialPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="生效日期" prop="startDate">
                    <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择生效日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="失效日期" prop="endDate">
                    <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择失效日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="form.status">
                      <el-radio value="0">启用</el-radio>
                      <el-radio value="1">停用</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" maxlength="500" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 客户选择器 / 物料选择器 -->
    <customer-picker ref="customerPickerRef" title="选择客户" @confirm="onCustomerPickerConfirm" />
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />
  </div>
</template>

<script setup name="MkPricePolicy">
import { listPolicy, getPolicy, addPolicy, updatePolicy, delPolicy } from "@/api/mk/pricePolicy"
import CustomerPicker from '@/components/CustomerPicker/index.vue'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
const { collapsedCards, toggleCard } = useDetailCard(['c1', 'c2'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pricePolicy_index')
const { mk_price_type, marketing_customer_level } = proxy.useDict("mk_price_type", "marketing_customer_level")

// 列显隐配置
const defaultColumns = {
  policyNo: { label: '政策编号', visible: true },
  policyName: { label: '政策名称', visible: true },
  priceType: { label: '价格类型', visible: true },
  customerName: { label: '客户名称', visible: true },
  customerLevel: { label: '客户等级', visible: true },
  materialName: { label: '物料名称', visible: true },
  unitPrice: { label: '政策单价', visible: true },
  startDate: { label: '生效日期', visible: true },
  endDate: { label: '失效日期', visible: true },
  status: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pricePolicy_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())

const policyList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const customerPickerRef = ref(null)
const materialPickerRef = ref(null)
const statusOptions = [{ value: '0', label: '启用' }, { value: '1', label: '停用' }]

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, policyNo: undefined, policyName: undefined, priceType: undefined, customerName: undefined, status: undefined },
  rules: {
    priceType: [{ required: true, message: "请选择价格类型", trigger: "change" }],
    policyName: [{ required: true, message: "政策名称不能为空", trigger: "blur" }],
    materialId: [{ required: true, message: "请选择物料", trigger: "change" }],
    unitPrice: [{ required: true, message: "政策单价不能为空", trigger: "blur" }],
    startDate: [{ required: true, message: "生效日期不能为空", trigger: "change" }],
    endDate: [{ required: true, message: "失效日期不能为空", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.policyNo) count++
  if (queryParams.value.policyName) count++
  if (queryParams.value.priceType) count++
  if (queryParams.value.customerName) count++
  if (queryParams.value.status) count++
  return count
})

function getList() {
  loading.value = true
  listPolicy(queryParams.value).then(response => {
    policyList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.policyNo = undefined
  queryParams.value.policyName = undefined
  queryParams.value.priceType = undefined
  queryParams.value.customerName = undefined
  queryParams.value.status = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.policyId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 表单重置 */
function reset() {
  form.value = { policyId: null, policyNo: undefined, policyName: undefined, priceType: '1', customerId: null, customerName: undefined, customerLevel: undefined, materialId: null, materialName: undefined, unitPrice: undefined, startDate: undefined, endDate: undefined, status: '0', remark: undefined }
  proxy.resetForm("policyRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增价格政策"
}

function handleUpdate(row) {
  reset()
  const policyId = row ? row.policyId : ids.value[0]
  getPolicy(policyId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改价格政策"
  })
}

function cancel() {
  open.value = false
  reset()
}

/** 价格类型切换联动：清理另一类型的专属字段 */
function onPriceTypeChange(v) {
  if (v === '1') {
    form.value.customerLevel = undefined
  } else if (v === '2') {
    form.value.customerId = null
    form.value.customerName = undefined
  }
}

/** 客户选择 */
function openCustomerPicker() { customerPickerRef.value.open(form.value.customerId || undefined) }
function clearCustomer() { form.value.customerId = null; form.value.customerName = undefined }
function onCustomerPickerConfirm(customer) {
  form.value.customerId = customer.customerId
  form.value.customerName = customer.customerName
}

/** 物料选择 */
function openMaterialPicker() { materialPickerRef.value.open(form.value.materialId || undefined) }
function clearMaterial() { form.value.materialId = null; form.value.materialName = undefined }
function onMaterialPickerConfirm(material) {
  form.value.materialId = material.materialId
  form.value.materialName = material.materialName
}

function submitForm() {
  proxy.$refs["policyRef"].validate(valid => {
    if (valid) {
      if (form.value.priceType === '1' && !form.value.customerId) {
        proxy.$modal.msgWarning("客户专价必须选择客户")
        return
      }
      if (form.value.priceType === '2' && !form.value.customerLevel) {
        proxy.$modal.msgWarning("等级价必须选择客户等级")
        return
      }
      if (form.value.unitPrice == null || form.value.unitPrice <= 0) {
        proxy.$modal.msgWarning("政策单价必须大于0")
        return
      }
      if (form.value.startDate && form.value.endDate && form.value.endDate < form.value.startDate) {
        proxy.$modal.msgWarning("失效日期不能早于生效日期")
        return
      }
      if (form.value.policyId != null) {
        updatePolicy(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addPolicy(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const policyIds = row ? row.policyId : ids.value
  proxy.$modal.confirm('是否确认删除价格政策编号为"' + policyIds + '"的数据项？').then(() => {
    return delPolicy(policyIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("mk/pricePolicy/export", { ...queryParams.value }, `pricePolicy_${new Date().getTime()}.xlsx`)
}

/** 工具函数 */
function dictLabel(options, v) {
  if (v == null || v === "") return "-"
  const item = (options || []).find(d => d.value == v)
  return item ? item.label : v
}
function priceTypeBadgeClass(v) {
  const item = (mk_price_type.value || []).find(d => d.value == v)
  const map = { primary: 'blue', success: 'green', info: 'gray', warning: 'amber', danger: 'red' }
  return map[item ? item.listClass : ''] || 'gray'
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.mono { font-family: "JetBrains Mono", Consolas, monospace; font-size: 12px; }
</style>

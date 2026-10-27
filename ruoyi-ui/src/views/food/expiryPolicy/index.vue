<template>
  <div class="app-container wms-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>策略名称</label>
          <div class="control">
            <el-input v-model="queryParams.policyName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>FEFO</label>
          <div class="control is-select">
            <el-select v-model="queryParams.fefoEnabled" placeholder="全部" clearable @change="handleQuery">
              <el-option label="启用" value="1" />
              <el-option label="停用" value="0" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in sys_normal_disable" :key="d.value" :label="d.label" :value="d.value" />
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
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['food:policy:add']">新增</el-button>
          <el-button type="warning" plain icon="Refresh" @click="handleRefresh" v-hasPermi="['food:policy:edit']">刷新效期状态</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['food:policy:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['food:policy:remove']">删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="food_policy_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="policyList" border class="app-table" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 60)" resizable align="center" />
          <el-table-column label="策略ID" prop="policyId" key="policyId" :width="colWidth('policyId', 80)" resizable align="center" v-if="columns.policyId.visible" />
          <el-table-column label="策略名称" prop="policyName" key="policyName" :min-width="colWidth('policyName', 140)" resizable show-overflow-tooltip v-if="columns.policyName.visible" />
          <el-table-column label="匹配级别" prop="matchLevel" key="matchLevel" :width="colWidth('matchLevel', 110)" resizable align="center" v-if="columns.matchLevel.visible">
            <template #default="scope">
              <span v-if="scope.row.materialId > 0" class="badge green"><span class="dot"></span>物料级</span>
              <span v-else-if="scope.row.materialType" class="badge amber"><span class="dot"></span>类别级</span>
              <span v-else class="badge gray"><span class="dot"></span>默认级</span>
            </template>
          </el-table-column>
          <el-table-column label="适用范围" prop="scopeText" key="scopeText" :min-width="colWidth('scopeText', 150)" resizable show-overflow-tooltip v-if="columns.scopeText.visible">
            <template #default="scope">
              <span v-if="scope.row.materialId > 0">{{ scope.row.materialName }}</span>
              <span v-else-if="scope.row.materialType">类别：{{ scope.row.materialType }}</span>
              <span v-else style="color: #909399">全部物料</span>
            </template>
          </el-table-column>
          <el-table-column label="临期预警" prop="warnDays" key="warnDays" :width="colWidth('warnDays', 100)" resizable align="center" v-if="columns.warnDays.visible">
            <template #default="scope">{{ scope.row.warnDays }} 天</template>
          </el-table-column>
          <el-table-column label="过期冻结" prop="freezeDays" key="freezeDays" :width="colWidth('freezeDays', 100)" resizable align="center" v-if="columns.freezeDays.visible">
            <template #default="scope">{{ scope.row.freezeDays === 0 ? '过期即冻结' : scope.row.freezeDays + ' 天' }}</template>
          </el-table-column>
          <el-table-column label="最小发运" prop="minShipDays" key="minShipDays" :width="colWidth('minShipDays', 100)" resizable align="center" v-if="columns.minShipDays.visible">
            <template #default="scope">{{ scope.row.minShipDays }} 天</template>
          </el-table-column>
          <el-table-column label="FEFO" prop="fefoEnabled" key="fefoEnabled" :width="colWidth('fefoEnabled', 80)" resizable align="center" v-if="columns.fefoEnabled.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.fefoEnabled === '1' ? 'green' : 'gray'">{{ scope.row.fefoEnabled === '1' ? '启用' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 80)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ dictLabel(sys_normal_disable, scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="colWidth('操作', 170)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['food:policy:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['food:policy:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="open" width="560px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="policyRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <!-- 基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('p1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.p1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.p1">
              <el-form-item label="策略名称" prop="policyName">
                <el-input v-model="form.policyName" placeholder="请输入策略名称" />
              </el-form-item>
              <el-form-item label="适用物料" prop="materialId">
                <el-input :model-value="[form.materialCode, form.materialName].filter(Boolean).join(' - ')" readonly placeholder="点击选择物料（默认级请留空）" @click="openMaterialPicker">
                  <template v-if="form.materialCode" #append><el-button icon="CircleClose" @click.stop="clearMaterial" /></template>
                  <template v-else #append><el-button icon="Search" @click="openMaterialPicker" /></template>
                </el-input>
              </el-form-item>
              <el-form-item label="适用类别" prop="materialType">
                <el-input v-model="form.materialType" placeholder="物料类别编码，留空不限（物料级命中后不再看类别）" />
              </el-form-item>
              <el-form-item label="FEFO先进先出" prop="fefoEnabled">
                <el-radio-group v-model="form.fefoEnabled">
                  <el-radio value="1">启用</el-radio>
                  <el-radio value="0">停用</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="form.status">
                  <el-radio v-for="d in sys_normal_disable" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>
          </section>
          <!-- 效期参数 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('p2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg></span>效期参数</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.p2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.p2">
              <el-form-item label="临期预警天数" prop="warnDays">
                <el-input-number v-model="form.warnDays" :min="1" :max="3650" controls-position="right" />
                <span class="form-tip">到期前N天标记为临期</span>
              </el-form-item>
              <el-form-item label="过期冻结天数" prop="freezeDays">
                <el-input-number v-model="form.freezeDays" :min="0" :max="3650" controls-position="right" />
                <span class="form-tip">0表示过期立即冻结</span>
              </el-form-item>
              <el-form-item label="最小发运天数" prop="minShipDays">
                <el-input-number v-model="form.minShipDays" :min="0" :max="3650" controls-position="right" />
                <span class="form-tip">剩余效期不足N天不可发运（批次二启用）</span>
              </el-form-item>
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
              </el-form-item>
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

    <!-- 物料选择器 -->
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />
  </div>
</template>

<script setup name="FoodExpiryPolicy">
import { listPolicy, getPolicy, addPolicy, updatePolicy, delPolicy, refreshExpire } from "@/api/food/policy"
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
const { collapsedCards, toggleCard } = useDetailCard(['p1', 'p2'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('food_policy_index')
const { sys_normal_disable } = proxy.useDict("sys_normal_disable")

const defaultColumns = {
  policyId: { label: '策略ID', visible: true },
  policyName: { label: '策略名称', visible: true },
  matchLevel: { label: '匹配级别', visible: true },
  scopeText: { label: '适用范围', visible: true },
  warnDays: { label: '临期预警', visible: true },
  freezeDays: { label: '过期冻结', visible: true },
  minShipDays: { label: '最小发运', visible: true },
  fefoEnabled: { label: 'FEFO', visible: true },
  status: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('food_policy_columns')
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
const materialPickerRef = ref(null)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, policyName: undefined, fefoEnabled: undefined, status: undefined },
  rules: {
    policyName: [{ required: true, message: "策略名称不能为空", trigger: "blur" }],
    warnDays: [{ required: true, message: "临期预警天数不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.policyName) count++
  if (queryParams.value.fefoEnabled) count++
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
  queryParams.value.policyName = undefined
  queryParams.value.fefoEnabled = undefined
  queryParams.value.status = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.policyId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = { policyId: undefined, policyName: undefined, materialId: 0, materialCode: undefined, materialName: undefined, materialType: "", warnDays: 30, freezeDays: 0, minShipDays: 0, fefoEnabled: "1", status: "0", remark: undefined }
  proxy.resetForm("policyRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增效期策略"
}

function handleUpdate(row) {
  reset()
  const policyId = row.policyId || ids.value[0]
  getPolicy(policyId).then(response => {
    form.value = response.data
    if (!(form.value.materialId && form.value.materialId > 0)) form.value.materialId = 0
    open.value = true
    title.value = "修改效期策略"
  })
}

/** 取消按钮 */
function cancel()
{
  open.value = false
  reset()
}

function submitForm() {
  proxy.$refs["policyRef"].validate(valid => {
    if (valid) {
      if (form.value.materialId == null) {
        form.value.materialId = 0
      }
      if (form.value.policyId != undefined) {
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
  const deleteIds = row.policyId || ids.value
  proxy.$modal.confirm('是否确认删除所选策略？').then(() => delPolicy(deleteIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleRefresh() {
  proxy.$modal.confirm('确认刷新全部批次的效期状态吗？将按三级匹配策略重算临期/过期标记。').then(() => refreshExpire()).then(res => {
    proxy.$modal.msgSuccess(res.msg || "刷新成功")
  }).catch(() => {})
}

function openMaterialPicker() {
  materialPickerRef.value.open(form.value.materialId > 0 ? form.value.materialId : undefined)
}

function clearMaterial() {
  form.value.materialId = 0
  form.value.materialCode = undefined
  form.value.materialName = undefined
}

function onMaterialPickerConfirm(material) {
  form.value.materialId = material.materialId
  form.value.materialCode = material.materialCode
  form.value.materialName = material.materialName
}

/** 字典值转标签 */
function dictLabel(options, v) {
  if (v == null || v === "") return "-"
  const item = (options || []).find(d => d.value == v)
  return item ? item.label : v
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.form-tip { font-size: 12px; color: #909399; margin-left: 8px; }
</style>

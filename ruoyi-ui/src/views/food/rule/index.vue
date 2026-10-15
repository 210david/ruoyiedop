<template>
  <div class="app-container wms-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>规则名称</label>
          <div class="control">
            <el-input v-model="queryParams.ruleName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in food_rule_status" :key="d.value" :label="d.label" :value="d.value" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['food:rule:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['food:rule:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['food:rule:remove']">删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="food_rule_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="ruleList" border class="app-table" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 60)" resizable align="center" />
          <el-table-column label="规则ID" prop="ruleId" key="ruleId" :width="colWidth('ruleId', 80)" resizable align="center" v-if="columns.ruleId.visible" />
          <el-table-column label="规则名称" prop="ruleName" key="ruleName" :min-width="colWidth('ruleName', 140)" resizable show-overflow-tooltip v-if="columns.ruleName.visible" />
          <el-table-column label="适用物料" prop="ruleMaterial" key="ruleMaterial" :min-width="colWidth('ruleMaterial', 160)" resizable show-overflow-tooltip v-if="columns.ruleMaterial.visible">
            <template #default="scope">
              <span v-if="scope.row.materialId === 0" style="color: #909399">全部物料（默认）</span>
              <span v-else>{{ scope.row.materialName }}（{{ scope.row.materialId }}）</span>
            </template>
          </el-table-column>
          <el-table-column label="批次号模板" prop="rulePattern" key="rulePattern" :min-width="colWidth('rulePattern', 180)" resizable v-if="columns.rulePattern.visible">
            <template #default="scope">
              <el-tag effect="plain" type="info">{{ scope.row.rulePattern }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="当前流水" prop="currentSeq" key="currentSeq" :width="colWidth('currentSeq', 100)" resizable align="center" v-if="columns.currentSeq.visible" />
          <el-table-column label="流水日期" prop="seqDate" key="seqDate" :width="colWidth('seqDate', 110)" resizable align="center" v-if="columns.seqDate.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ dictLabel(food_rule_status, scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="colWidth('操作', 220)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handlePreview(scope.row)">试算</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['food:rule:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['food:rule:remove']">删除</el-button>
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
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="ruleRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 规则信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('r1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>规则信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.r1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.r1">
              <el-form-item label="规则名称" prop="ruleName">
                <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
              </el-form-item>
              <el-form-item label="适用物料" prop="materialId">
                <el-input :model-value="[form.materialCode, form.materialName].filter(Boolean).join(' - ')" readonly placeholder="点击选择物料（默认规则请留空）" @click="openMaterialPicker">
                  <template v-if="form.materialCode" #append><el-button icon="CircleClose" @click.stop="clearMaterial" /></template>
                  <template v-else #append><el-button icon="Search" @click="openMaterialPicker" /></template>
                </el-input>
                <div class="form-tip">不选或清空表示默认规则；物料规则优先于默认规则命中</div>
              </el-form-item>
              <el-form-item label="批次号模板" prop="rulePattern">
                <el-input v-model="form.rulePattern" placeholder="如：B{yyyyMMdd}{seq:4}" />
                <div class="form-tip">
                  占位符：<code>{yyyyMMdd}</code> <code>{yyMM}</code> <code>{yyyy}</code> <code>{materialCode}</code> <code>{seq:n}</code>（n为流水位数）
                </div>
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="form.status">
                  <el-radio v-for="d in food_rule_status" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                </el-radio-group>
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
          <el-button type="primary" @click="previewForm">试 算</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 物料选择器 -->
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />
  </div>
</template>

<script setup name="FoodRule">
import { listRule, getRule, addRule, updateRule, delRule, previewRule } from "@/api/food/rule"
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
const { collapsedCards, toggleCard } = useDetailCard(['r1'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('food_rule_index')
const { sys_normal_disable: food_rule_status } = proxy.useDict("sys_normal_disable")

// 列显隐配置
const defaultColumns = {
  ruleId: { label: '规则ID', visible: true },
  ruleName: { label: '规则名称', visible: true },
  ruleMaterial: { label: '适用物料', visible: true },
  rulePattern: { label: '批次号模板', visible: true },
  currentSeq: { label: '当前流水', visible: true },
  seqDate: { label: '流水日期', visible: true },
  status: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('food_rule_columns')
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

const ruleList = ref([])
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
  queryParams: { pageNum: 1, pageSize: 10, ruleName: undefined, status: undefined },
  rules: {
    ruleName: [{ required: true, message: "规则名称不能为空", trigger: "blur" }],
    rulePattern: [{ required: true, message: "批次号模板不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.ruleName) count++
  if (queryParams.value.status) count++
  return count
})

function getList() {
  loading.value = true
  listRule(queryParams.value).then(response => {
    ruleList.value = response.rows
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
  queryParams.value.ruleName = undefined
  queryParams.value.status = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.ruleId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = { ruleId: undefined, ruleName: undefined, materialId: 0, materialCode: undefined, materialName: undefined, rulePattern: "B{yyyyMMdd}{seq:4}", status: "0", remark: undefined }
  proxy.resetForm("ruleRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增批次号规则"
}

function handleUpdate(row) {
  reset()
  const ruleId = row.ruleId || ids.value[0]
  getRule(ruleId).then(response => {
    form.value = response.data
    if (!(form.value.materialId && form.value.materialId > 0)) form.value.materialId = 0
    open.value = true
    title.value = "修改批次号规则"
  })
}

/** 取消按钮 */
function cancel()
{
  open.value = false
  reset()
}

function submitForm() {
  proxy.$refs["ruleRef"].validate(valid => {
    if (valid) {
      if (form.value.materialId == null || form.value.materialId === undefined) {
        form.value.materialId = 0
      }
      if (form.value.ruleId != undefined) {
        updateRule(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addRule(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const deleteIds = row.ruleId || ids.value
  proxy.$modal.confirm('是否确认删除所选规则？').then(() => delRule(deleteIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function previewForm() {
  previewRule({ rulePattern: form.value.rulePattern, previewCode: form.value.materialCode }).then(res => {
    proxy.$modal.alert("试算结果：" + res.data)
  })
}

function handlePreview(row) {
  previewRule({ rulePattern: row.rulePattern }).then(res => {
    proxy.$modal.alert("规则【" + row.ruleName + "】试算：" + res.data)
  })
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
.form-tip { font-size: 12px; color: #909399; line-height: 1.6; }
.form-tip code { background: #f5f7fa; padding: 0 4px; border-radius: 3px; color: #c7254e; }
</style>

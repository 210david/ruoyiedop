<template>
  <div class="app-container qms-mattr-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>物料编码</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>物料名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>是否免检</label>
          <div class="control is-select">
            <el-select v-model="queryParams.isExempt" placeholder="全部" clearable @change="handleQuery">
              <el-option label="否" value="0" />
              <el-option label="是" value="1" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>备注</label>
          <div class="control"><el-input v-model="queryParams.remark" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:materialattr:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['qms:materialattr:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['qms:materialattr:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:materialattr:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_materialattr_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 140)" resizable show-overflow-tooltip v-if="columns.materialCode.visible" />
          <el-table-column label="物料名称" prop="materialName" key="materialName" :width="colWidth('materialName', 200)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="检验类型" prop="inspectTypes" key="inspectTypes" :width="colWidth('inspectTypes', 200)" resizable align="center" v-if="columns.inspectTypes.visible">
            <template #header><span>检验类型</span><el-tooltip content="检验类型包括IQC（来料检验）、IPQC（过程检验）、FQC（成品检验）、OQC（出货检验）等" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope">
              <span v-for="(t, i) in parseInspectTypes(scope.row.inspectTypes)" :key="i" class="badge blue" style="margin-right:4px"><span class="dot"></span>{{ inspTypeLabel(t) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="是否免检" prop="isExempt" key="isExempt" :width="colWidth('isExempt', 100)" resizable align="center" v-if="columns.isExempt.visible">
            <template #default="scope"><span class="badge" :class="scope.row.isExempt === '1' ? 'blue' : 'gray'"><span class="dot"></span>{{ scope.row.isExempt === '1' ? '是' : '否' }}</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 80)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope"><span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}</span></template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:materialattr:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:materialattr:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="mattrRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_basic">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="物料" prop="materialId">
                    <el-input v-model="materialDisplay" readonly placeholder="请选择物料" style="width: 100%" @click="openMaterialPicker">
                      <template #append>
                        <el-button icon="Search" @click="openMaterialPicker" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="form.materialName" class="clear-icon" @click.stop="clearMaterial"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_insp')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>检验配置</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_insp }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_insp">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item prop="inspectTypes">
                    <template #label><span>检验类型</span><el-tooltip content="检验类型包括IQC（来料检验）、IPQC（过程检验）、FQC（成品检验）、OQC（出货检验）等，可多选" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
                    <el-checkbox-group v-model="inspectTypeArr" class="insp-type-group">
                      <el-checkbox v-for="d in qms_insp_type" :key="d.value" :value="d.value">{{ d.label }}</el-checkbox>
                    </el-checkbox-group>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item prop="isExempt"><template #label><span>是否免检</span><el-tooltip content="勾选“是”表示该物料可跳过检验直接入库/投入使用，通常适用于低风险物料或有质量保证协议的供应商" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-radio-group v-model="form.isExempt"><el-radio value="0">否</el-radio><el-radio value="1">是</el-radio></el-radio-group></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_other')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_other">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- Material Picker -->
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <span class="rd-detail-header-title">物料质量属性详情</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_basic" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ viewData.materialCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">检验类型</span><div class="rd-value"><span v-for="(t, i) in parseInspectTypes(viewData.inspectTypes)" :key="i" class="badge blue" style="margin-right:4px"><span class="dot"></span>{{ inspTypeLabel(t) }}</span></div></div>
            <div class="rd-item"><span class="rd-label">是否免检</span><div class="rd-value">{{ viewData.isExempt === '1' ? '是' : '否' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value">{{ viewData.status === '0' ? '正常' : '停用' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('v_remark')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_remark }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_remark" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsMaterialAttr">
import { listMaterialAttr, getMaterialAttr, addMaterialAttr, updateMaterialAttr, delMaterialAttr } from '@/api/qms/materialattr'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { QuestionFilled, CircleClose } from '@element-plus/icons-vue'
import MaterialPicker from '@/components/MaterialPicker/index.vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_materialattr_index')
const { collapsedCards, toggleCard } = useDetailCard(['e_basic', 'e_insp', 'e_other', 'v_basic', 'v_remark'])
const { qms_insp_type } = proxy.useDict('qms_insp_type')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const defaultColumns = { materialCode: { label: '物料编码', visible: true }, materialName: { label: '物料名称', visible: true }, inspectTypes: { label: '检验类型', visible: true }, isExempt: { label: '是否免检', visible: true }, status: { label: '状态', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('qms_materialattr_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const inspectTypeArr = ref([])
const materialPickerRef = ref()
const materialDisplay = computed(() => {
  if (form.value.materialCode && form.value.materialName) {
    return form.value.materialCode + ' / ' + form.value.materialName
  }
  return form.value.materialName || form.value.materialCode || ''
})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, materialCode: undefined, materialName: undefined, isExempt: undefined, status: undefined, remark: undefined, params: {} },
  rules: {
    materialId: [{ required: true, message: '请选择物料', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.materialCode) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.status) count++
  if (queryParams.value.isExempt) count++
  if (queryParams.value.remark) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function parseInspectTypes(val) { if (!val) return []; return val.split(',') }
function inspTypeLabel(val) { const item = qms_insp_type.value.find(d => d.value == val); return item ? item.label : val }

function getList() { loading.value = true; listMaterialAttr(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.materialCode = undefined; queryParams.value.materialName = undefined; queryParams.value.isExempt = undefined; queryParams.value.status = undefined; queryParams.value.remark = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.attrId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { attrId: undefined, materialId: undefined, materialCode: undefined, materialName: undefined, inspectTypes: undefined, isExempt: '0', status: '0', remark: undefined }; inspectTypeArr.value = [] }
function openMaterialPicker() { materialPickerRef.value.open(form.value.materialId) }
function onMaterialPickerConfirm(material) { form.value.materialId = material.materialId; form.value.materialCode = material.materialCode; form.value.materialName = material.materialName }
function clearMaterial() { form.value.materialId = undefined; form.value.materialCode = undefined; form.value.materialName = undefined }
function handleAdd() { reset(); open.value = true; title.value = '添加物料质量属性' }
function handleUpdate(row) { reset(); getMaterialAttr(row.attrId || ids.value[0]).then(res => { form.value = res.data; inspectTypeArr.value = parseInspectTypes(res.data.inspectTypes); open.value = true; title.value = '修改物料质量属性' }) }
function handleView(row) { getMaterialAttr(row.attrId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function submitForm() { form.value.inspectTypes = inspectTypeArr.value.join(','); proxy.$refs['mattrRef'].validate(valid => { if (valid) { if (form.value.attrId != undefined) { updateMaterialAttr(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addMaterialAttr(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const attrIds = row.attrId || ids.value; proxy.$modal.confirm('确认删除编号为"' + attrIds + '"的数据？').then(() => delMaterialAttr(attrIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('qms/materialattr/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `materialattr_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

getList()
onActivated(() => { getList() })
</script>

<style scoped>
.qms-mattr-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-mattr-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-mattr-page .filter-card { padding:14px 20px 16px; }
.qms-mattr-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-mattr-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-mattr-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-mattr-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-mattr-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-mattr-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-mattr-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-mattr-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-mattr-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-mattr-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-mattr-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-mattr-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-mattr-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.qms-mattr-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-mattr-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-mattr-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-mattr-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-mattr-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-mattr-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-mattr-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.qms-mattr-page .field .control :deep(.el-select) { width:100%; }
.qms-mattr-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-mattr-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.qms-mattr-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-mattr-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-mattr-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-mattr-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-mattr-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-mattr-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-mattr-page .btn-soft .el-icon { font-size:14px; }
.qms-mattr-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-mattr-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-mattr-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-mattr-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-mattr-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-mattr-page .table-wrap { overflow-x:auto; }
.qms-mattr-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-mattr-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-mattr-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-mattr-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-mattr-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-mattr-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-mattr-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.qms-mattr-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.qms-mattr-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.qms-mattr-page .badge.green .dot { background:var(--green-500); }
.qms-mattr-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.qms-mattr-page .badge.gray .dot { background:var(--ink-400); }
.qms-mattr-page .badge.blue { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.qms-mattr-page .badge.blue .dot { background:var(--brand-500); }
.qms-mattr-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
.qms-mattr-page .field .control :deep(.el-date-editor) { width:100%; }
.qms-mattr-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.qms-mattr-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.qms-mattr-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
@media (max-width:1100px) { .qms-mattr-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-mattr-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.qms-mattr-page .rd-form-tip { margin-left: 4px; color: #c0c4cc; font-size: 14px; cursor: help; }
.qms-mattr-page .rd-form-tip:hover { color: #909399; }
.qms-mattr-page .clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.qms-mattr-page .clear-icon:hover { color: #909399; }
.qms-mattr-page .insp-type-group { display: flex; flex-wrap: nowrap; gap: 8px; }
</style>

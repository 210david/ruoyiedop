<template>
  <div class="app-container hr-ssplan-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div></div>
      <div class="filter-bar">
        <div class="field">
          <label>方案名称</label>
          <div class="control"><el-input v-model="queryParams.planName" placeholder="请输入方案名称" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>年度</label>
          <div class="control"><el-input v-model="queryParams.year" placeholder="如 2026" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control">
            <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 100%">
              <el-option label="正常" value="0" /><el-option label="停用" value="1" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">比例在方案中统一维护，员工仅在有差异时覆盖</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:ssPlan:add']">新增</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:ssPlan:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_ssplan_columns" /></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="planList" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="方案名称" prop="planName" key="planName" align="center" :min-width="colWidth('planName', 160)" show-overflow-tooltip resizable v-if="columns.planName.visible" />
          <el-table-column label="年度" prop="year" key="year" align="center" :width="colWidth('year', 90)" resizable v-if="columns.year.visible" />
          <el-table-column label="默认方案" prop="isDefault" key="isDefault" align="center" :width="colWidth('isDefault', 100)" resizable v-if="columns.isDefault.visible">
            <template #default="scope">
              <el-tooltip v-if="scope.row.isDefault === 1" content="该年度工资核算时的默认比例来源，同年度仅一个；不同年度各有一个默认" placement="top">
                <el-tag type="success" size="small">{{ scope.row.year }}默认</el-tag>
              </el-tooltip>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="基数下限" prop="baseLower" key="baseLower" align="center" :width="colWidth('baseLower', 110)" resizable v-if="columns.baseLower.visible" />
          <el-table-column label="基数上限" prop="baseUpper" key="baseUpper" align="center" :width="colWidth('baseUpper', 110)" resizable v-if="columns.baseUpper.visible" />
          <el-table-column label="养老(单位/个人)" prop="pension" key="pension" align="center" :width="colWidth('pension', 140)" resizable v-if="columns.pension.visible">
            <template #default="scope">{{ pct(scope.row.pensionEmployer) }} / {{ pct(scope.row.pensionEmployee) }}</template>
          </el-table-column>
          <el-table-column label="医疗(单位/个人)" prop="medical" key="medical" align="center" :width="colWidth('medical', 140)" resizable v-if="columns.medical.visible">
            <template #default="scope">{{ pct(scope.row.medicalEmployer) }} / {{ pct(scope.row.medicalEmployee) }}</template>
          </el-table-column>
          <el-table-column label="失业(单位/个人)" prop="unemployment" key="unemployment" align="center" :width="colWidth('unemployment', 140)" resizable v-if="columns.unemployment.visible">
            <template #default="scope">{{ pct(scope.row.unemploymentEmployer) }} / {{ pct(scope.row.unemploymentEmployee) }}</template>
          </el-table-column>
          <el-table-column label="工伤/生育(单位)" prop="injury" key="injury" align="center" :width="colWidth('injury', 140)" resizable v-if="columns.injury.visible">
            <template #default="scope">{{ pct(scope.row.injuryEmployer) }} / {{ pct(scope.row.maternityEmployer) }}</template>
          </el-table-column>
          <el-table-column label="公积金(单位/个人)" prop="fund" key="fund" align="center" :width="colWidth('fund', 140)" resizable v-if="columns.fund.visible">
            <template #default="scope">{{ pct(scope.row.fundEmployer) }} / {{ pct(scope.row.fundEmployee) }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" align="center" :width="colWidth('status', 90)" resizable v-if="columns.status.visible">
            <template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:ssPlan:edit']">修改</el-button>
              <el-button link type="primary" icon="CopyDocument" @click="handleCopy(scope.row)" v-hasPermi="['hr:ssPlan:add']">复制</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:ssPlan:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container"><pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" /></div>
    </div>

    <el-dialog v-model="open" width="912px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon><Coin /></el-icon></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <div class="rd-page">
        <el-form ref="planRef" :model="form" :rules="rules" label-width="110px">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title">基本信息</div></div>
            <div class="rd-card-body">
              <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px"
                title="基数下限/上限：社保缴费基数允许的最小/最大值。社保按员工缴费基数×比例计算，基数由员工上年月均工资核定，但不能低于当地公布的下限、不能高于上限（曲靖2026年度下限约4000+元/月，上限约20000元/月，请以当年人社局公布数为准）。低于下限按下限缴，高于上限按上限缴。" />
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="方案名称" prop="planName"><el-input v-model="form.planName" placeholder="如：曲靖标准方案" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="适用年度" prop="year">
                  <el-date-picker v-model="form.year" type="year" placeholder="选择年度" value-format="YYYY" style="width: 100%" />
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="基数下限" prop="baseLower"><el-input-number v-model="form.baseLower" :precision="2" :min="0" controls-position="right" style="width: 100%" placeholder="如 4000" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="基数上限" prop="baseUpper"><el-input-number v-model="form.baseUpper" :precision="2" :min="0" controls-position="right" style="width: 100%" placeholder="如 20000" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="设为默认" prop="isDefault"><el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title">缴费比例（%）</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="养老单位" prop="pensionEmployer"><el-input-number v-model="form.pensionEmployer" :precision="2" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="养老个人" prop="pensionEmployee"><el-input-number v-model="form.pensionEmployee" :precision="2" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="医疗单位" prop="medicalEmployer"><el-input-number v-model="form.medicalEmployer" :precision="2" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="医疗个人" prop="medicalEmployee"><el-input-number v-model="form.medicalEmployee" :precision="2" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="失业单位" prop="unemploymentEmployer"><el-input-number v-model="form.unemploymentEmployer" :precision="2" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="失业个人" prop="unemploymentEmployee"><el-input-number v-model="form.unemploymentEmployee" :precision="2" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工伤单位" prop="injuryEmployer"><el-input-number v-model="form.injuryEmployer" :precision="2" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="生育单位" prop="maternityEmployer"><el-input-number v-model="form.maternityEmployer" :precision="2" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="公积金单位" prop="fundEmployer"><el-input-number v-model="form.fundEmployer" :precision="2" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="公积金个人" prop="fundEmployee"><el-input-number v-model="form.fundEmployee" :precision="2" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" /></el-form-item>
            </div>
          </section>
        </el-form>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HrSsPlan">
import { listSsPlan, getSsPlan, addSsPlan, updateSsPlan, delSsPlan, copySsPlan } from '@/api/hr/ssPlan'
import { Coin } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_ssplan_index')

const planList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref('')
const open = ref(false)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, planName: undefined, year: undefined, status: undefined },
  rules: {
    planName: [{ required: true, message: '方案名称不能为空', trigger: 'blur' }],
    year: [{ required: true, message: '适用年度不能为空', trigger: 'blur' }],
    baseLower: [{ required: true, message: '基数下限不能为空（不参保可填 0）', trigger: 'blur' }],
    baseUpper: [{ required: true, message: '基数上限不能为空（不参保可填 0）', trigger: 'blur' }],
    pensionEmployer: [{ required: true, message: '请填写养老单位比例（不参保填 0）', trigger: 'blur' }],
    pensionEmployee: [{ required: true, message: '请填写养老个人比例（不参保填 0）', trigger: 'blur' }],
    medicalEmployer: [{ required: true, message: '请填写医疗单位比例（不参保填 0）', trigger: 'blur' }],
    medicalEmployee: [{ required: true, message: '请填写医疗个人比例（不参保填 0）', trigger: 'blur' }],
    unemploymentEmployer: [{ required: true, message: '请填写失业单位比例（不参保填 0）', trigger: 'blur' }],
    unemploymentEmployee: [{ required: true, message: '请填写失业个人比例（不参保填 0）', trigger: 'blur' }],
    injuryEmployer: [{ required: true, message: '请填写工伤单位比例（不参保填 0）', trigger: 'blur' }],
    maternityEmployer: [{ required: true, message: '请填写生育单位比例（不参保填 0）', trigger: 'blur' }],
    fundEmployer: [{ required: true, message: '请填写公积金单位比例（不参保填 0）', trigger: 'blur' }],
    fundEmployee: [{ required: true, message: '请填写公积金个人比例（不参保填 0）', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const defaultColumns = {
  planName: { label: '方案名称', visible: true },
  year: { label: '年度', visible: true },
  isDefault: { label: '默认', visible: true },
  baseLower: { label: '基数下限', visible: true },
  baseUpper: { label: '基数上限', visible: true },
  pension: { label: '养老', visible: true },
  medical: { label: '医疗', visible: true },
  unemployment: { label: '失业', visible: true },
  injury: { label: '工伤/生育', visible: true },
  fund: { label: '公积金', visible: true },
  status: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_ssplan_columns')
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

function pct(v) { return v != null ? (Number(v) * 100).toFixed(2).replace(/\.00$/, '') + '%' : '-' }

function getList() {
  loading.value = true
  listSsPlan(queryParams.value).then(res => {
    planList.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.planName = undefined; queryParams.value.year = undefined; queryParams.value.status = undefined; handleQuery() }

function reset() {
  form.value = { planId: undefined, planName: undefined, year: new Date().getFullYear(), isDefault: 0, baseLower: undefined, baseUpper: undefined, pensionEmployer: 16, pensionEmployee: 8, medicalEmployer: 8.5, medicalEmployee: 2, unemploymentEmployer: 0.5, unemploymentEmployee: 0.5, injuryEmployer: 0.9, maternityEmployer: 0.8, fundEmployer: 12, fundEmployee: 12, status: '0', remark: undefined }
  proxy.resetForm('planRef')
}

function handleAdd() { reset(); open.value = true; title.value = '新增社保方案' }
function handleUpdate(row) {
  reset()
  getSsPlan(row.planId).then(res => {
    const d = { ...res.data }
    // 数据库存的是小数（0.16），页面录入为百分数（16），回填需 ×100 转回
    const ratioKeys = ['pensionEmployer','pensionEmployee','medicalEmployer','medicalEmployee','unemploymentEmployer','unemploymentEmployee','injuryEmployer','maternityEmployer','fundEmployer','fundEmployee']
    ratioKeys.forEach(k => { if (d[k] != null) d[k] = Number((Number(d[k]) * 100).toFixed(4)) })
    // year 选择器需要字符串/Date，回填为字符串供 el-date-picker type=year 使用
    if (d.year != null) d.year = String(d.year)
    form.value = d
    open.value = true; title.value = '修改社保方案'
  })
}
function submitForm() {
  proxy.$refs['planRef'].validate(valid => {
    if (valid) {
      // 页面录入为百分数（如 16 表示 16%），存储为小数
      const f = { ...form.value }
      const ratioKeys = ['pensionEmployer','pensionEmployee','medicalEmployer','medicalEmployee','unemploymentEmployer','unemploymentEmployee','injuryEmployer','maternityEmployer','fundEmployer','fundEmployee']
      ratioKeys.forEach(k => { if (f[k] != null) f[k] = Number((f[k] / 100).toFixed(6)) })
      if (f.year != null) f.year = Number(f.year)
      if (f.planId != undefined) {
        updateSsPlan(f).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addSsPlan(f).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除方案「' + row.planName + '」？').then(() => delSsPlan(row.planId)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}

/** 复制方案到新年度：比例/上下限原样带出，默认标记不复制 */
function handleCopy(row) {
  const defaultYear = row.year + 1
  proxy.$prompt('将方案「' + row.planName + '」（' + row.year + '年度）复制到目标年度：', '复制到新年度', {
    confirmButtonText: '复制', cancelButtonText: '取消', inputValue: String(defaultYear),
    inputPattern: /^\d{4}$/, inputErrorMessage: '请输入4位年度'
  }).then(({ value }) => {
    return copySsPlan(row.planId, value)
  }).then(res => {
    proxy.$modal.msgSuccess('复制成功，请按新年度政策核对调整')
    getList()
  }).catch(() => {})
}
function handleExport() { proxy.download('hr/ssPlan/export', { ...queryParams.value }, `hrSsPlan_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

getList()
</script>

<style scoped>
.hr-ssplan-page { padding-top: 10px; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --brand-600:#4f46e5; --r-sm:6px; --r-lg:14px; color: var(--ink-900); }
.hr-ssplan-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:0 1px 2px rgba(15,23,42,.04); overflow:hidden; margin-bottom:8px; }
.hr-ssplan-page .filter-card { padding:14px 20px 16px; }
.hr-ssplan-page .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); margin-bottom:12px; }
.hr-ssplan-page .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-ssplan-page .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-ssplan-page .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-ssplan-page .filter-info { font-size:13px; color:var(--ink-500); }
.hr-ssplan-page .field { display:flex; flex-direction:column; gap:6px; }
.hr-ssplan-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.hr-ssplan-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; border:1px solid var(--ink-200); border-radius:var(--r-sm); background:#fff; }
.hr-ssplan-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-ssplan-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; height:34px; line-height:34px; }
.hr-ssplan-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-ssplan-page .field .control :deep(.el-select) { width:100%; }
.hr-ssplan-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-ssplan-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.hr-ssplan-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-ssplan-page .toolbar .left { display:flex; gap:8px; }
.hr-ssplan-page .app-table { --el-table-header-bg-color:var(--ink-50); }
.hr-ssplan-page .pagination-container { display:flex; justify-content:flex-end; padding:14px 20px; }
@media (max-width:1100px) { .hr-ssplan-page .filter-bar { grid-template-columns:repeat(2,1fr); } }
</style>

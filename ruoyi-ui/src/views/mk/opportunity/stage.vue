<template>
  <div class="app-container mk-list-page">
    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:stage:add']">新增</el-button>
        </div>
        <div class="right">
          <right-toolbar @queryTable="getList" :columns="columns" storageKey="mk_stage_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="阶段编码" prop="stageCode" key="stageCode" :width="colWidth('stageCode', 120)" resizable v-if="columns.stageCode.visible" />
          <el-table-column label="阶段名称" prop="stageName" key="stageName" :width="colWidth('stageName', 120)" resizable v-if="columns.stageName.visible" />
          <el-table-column label="排序" prop="sort" key="sort" :width="colWidth('sort', 80)" resizable align="center" v-if="columns.sort.visible" />
          <el-table-column label="赢率(%)" prop="winRate" key="winRate" :width="colWidth('winRate', 100)" resizable align="center" v-if="columns.winRate.visible" />
          <el-table-column label="最大停留天数" prop="maxDays" key="maxDays" :width="colWidth('maxDays', 120)" resizable align="center" v-if="columns.maxDays.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 80)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" key="remark" show-overflow-tooltip v-if="columns.remark.visible" />
          <el-table-column label="操作" width="180" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:stage:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:stage:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 新增/修改弹窗 -->
    <el-dialog v-model="open" width="720px" append-to-body draggable class="rd-dialog" top="5vh">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="stageRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('formBasic')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>
                基本信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.formBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.formBasic">
              <el-row>
                <el-col :span="12"><el-form-item label="阶段编码" prop="stageCode"><el-input v-model="form.stageCode" placeholder="请输入阶段编码" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="阶段名称" prop="stageName"><el-input v-model="form.stageName" placeholder="请输入阶段名称" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="排序" prop="sort"><el-input-number v-model="form.sort" :min="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 控制参数 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('formControl')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18.7 8l-5.1 5.2-2.8-2.7L7 14.3"/></svg></span>
                控制参数
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.formControl }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.formControl">
              <el-row>
                <el-col :span="12"><el-form-item label="赢率(%)" prop="winRate"><el-input-number v-model="form.winRate" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="最大停留天数" prop="maxDays"><el-input-number v-model="form.maxDays" :min="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('formOther')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>
                其他信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.formOther }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.formOther">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="viewOpen" width="720px" append-to-body draggable class="rd-dialog" top="5vh">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg></div>
          <span class="rd-detail-header-title">阶段详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.stageName">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ viewForm.stageName }}</span>
            <span class="badge" :class="badgeClass(viewForm.status)"><span class="dot"></span>{{ statusLabel(viewForm.status) }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewBasic')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>
              基本信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewBasic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">阶段编码</span><div class="rd-value">{{ viewForm.stageCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">阶段名称</span><div class="rd-value">{{ viewForm.stageName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">排序</span><div class="rd-value">{{ viewForm.sort || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="badgeClass(viewForm.status)"><span class="dot"></span>{{ statusLabel(viewForm.status) }}</span></div></div>
            </div>
          </div>
        </section>
        <!-- 控制参数 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewControl')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18.7 8l-5.1 5.2-2.8-2.7L7 14.3"/></svg></span>
              控制参数
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewControl }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewControl">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">赢率(%)</span><div class="rd-value">{{ viewForm.winRate != null ? viewForm.winRate + '%' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">最大停留天数</span><div class="rd-value">{{ viewForm.maxDays != null ? viewForm.maxDays + ' 天' : '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewOther')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>
              其他信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewOther }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewOther">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkOpportunityStage">
import { listStage, getStage, addStage, updateStage, delStage } from '@/api/mk/stage'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['formBasic', 'formControl', 'formOther', 'viewBasic', 'viewControl', 'viewOther'])

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict('sys_normal_disable')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_opportunity_stage')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const total = ref(0)
const title = ref('')
const viewForm = ref({})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 9999 },
  rules: {
    stageCode: [{ required: true, message: '阶段编码不能为空', trigger: 'blur' }],
    stageName: [{ required: true, message: '阶段名称不能为空', trigger: 'blur' }],
    sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  stageCode: { label: '阶段编码', visible: true },
  stageName: { label: '阶段名称', visible: true },
  sort: { label: '排序', visible: true },
  winRate: { label: '赢率(%)', visible: true },
  maxDays: { label: '最大停留天数', visible: true },
  status: { label: '状态', visible: true },
  remark: { label: '备注', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_stage_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

const columns = ref(loadColumnVisibility())

function badgeClass(status) { return status === '0' ? 'green' : 'gray' }
function statusLabel(status) { return status === '0' ? '正常' : '停用' }

function getList() { loading.value = true; listStage(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }).catch(() => { loading.value = false }) }
function handleQuery() { getList() }
function resetQuery() { handleQuery() }
function reset() { form.value = { stageCode: undefined, stageName: undefined, sort: 1, winRate: 0, maxDays: 30, status: '0', remark: undefined }; proxy.resetForm('stageRef') }
function handleAdd() { reset(); open.value = true; title.value = '新增销售阶段' }
function handleUpdate(row) { reset(); getStage(row.stageId).then(res => { form.value = res.data; open.value = true; title.value = '修改销售阶段' }) }
function handleView(row) {
  getStage(row.stageId).then(res => {
    viewForm.value = res.data
    Object.keys(collapsedCards).forEach(k => { collapsedCards[k] = false })
    viewOpen.value = true
  })
}
function submitForm() {
  proxy.$refs['stageRef'].validate(valid => {
    if (valid) {
      if (form.value.stageId != undefined) { updateStage(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addStage(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const stageIds = row.stageId; proxy.$modal.confirm('确认删除阶段"' + row.stageName + '"的数据？').then(() => delStage(stageIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
getList()
</script>

<style scoped>
.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
</style>

<!-- 非scoped样式：弹窗 append-to-body 后脱离 .mk-list-page 前缀，补充 rd-dialog 内徽章样式 -->
<style>
/* 状态徽章：弹窗 append-to-body 后脱离 mk-list-page 前缀，补充全局样式 */
.rd-dialog .badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
  border: 1px solid transparent;
}
.rd-dialog .badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.rd-dialog .badge.green { background: #ecfdf5; color: #047857; border-color: #a7f3d0; }
.rd-dialog .badge.green .dot { background: #10b981; }
.rd-dialog .badge.gray { background: #f1f5f9; color: #64748b; border-color: #e2e8f0; }
.rd-dialog .badge.gray .dot { background: #94a3b8; }
</style>

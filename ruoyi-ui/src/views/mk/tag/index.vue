<template>
  <div class="app-container mk-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>标签名称</label>
          <div class="control">
            <el-input v-model="queryParams.tagName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>标签类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.tagType" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_tag_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
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
      <!-- Status Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:tag:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:tag:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:tag:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:tag:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_tag_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="标签名称" prop="tagName" key="tagName" :width="colWidth('tagName', 180)" resizable v-if="columns.tagName.visible">
            <template #default="scope">
              <el-tag :color="scope.row.tagColor" effect="dark" v-if="scope.row.tagColor">{{ scope.row.tagName }}</el-tag>
              <span v-else>{{ scope.row.tagName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="标签类型" prop="tagType" key="tagType" :width="colWidth('tagType', 100)" resizable align="center" v-if="columns.tagType.visible">
            <template #default="scope">
              <span class="badge" :class="typeBadgeClass(scope.row.tagType)">
                <span class="dot"></span>{{ typeLabel(scope.row.tagType) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="颜色" prop="tagColor" key="tagColor" :width="colWidth('tagColor', 100)" resizable align="center" v-if="columns.tagColor.visible">
            <template #default="scope">
              <div v-if="scope.row.tagColor" :style="{ background: scope.row.tagColor, width: '30px', height: '20px', borderRadius: '4px', display: 'inline-block' }"></div>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="排序" prop="sort" key="sort" :width="colWidth('sort', 80)" resizable align="center" v-if="columns.sort.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="备注" prop="remark" key="remark" show-overflow-tooltip v-if="columns.remark.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:tag:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:tag:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="tagRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c_basic')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c_basic">
              <el-row>
                <el-col :span="12"><el-form-item label="标签名称" prop="tagName"><el-input v-model="form.tagName" placeholder="请输入标签名称" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="标签类型" prop="tagType">
                  <el-radio-group v-model="form.tagType">
                    <el-radio v-for="d in marketing_tag_type" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                  </el-radio-group>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="标签颜色" prop="tagColor">
                  <el-color-picker v-model="form.tagColor" />
                  <el-tag :color="form.tagColor" effect="dark" style="margin-left: 10px" v-if="form.tagColor">预览</el-tag>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="排序" prop="sort"><el-input-number v-model="form.sort" :min="0" controls-position="right" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status">
                    <el-radio v-for="d in sys_normal_disable" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                  </el-radio-group>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c_other')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c_other">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
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
    <el-dialog v-model="viewOpen" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></div>
          <span class="rd-detail-header-title">标签详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.tagName">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ viewForm.tagName }}</span>
            <dict-tag :options="sys_normal_disable" :value="viewForm.status" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_basic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">标签名称</span><div class="rd-value"><el-tag :color="viewForm.tagColor" effect="dark" v-if="viewForm.tagColor">{{ viewForm.tagName }}</el-tag>
                <span v-else>{{ viewForm.tagName }}</span></div></div>
              <div class="rd-item"><span class="rd-label">标签类型</span><div class="rd-value"><dict-tag :options="marketing_tag_type" :value="viewForm.tagType" /></div></div>
              <div class="rd-item"><span class="rd-label">标签颜色</span><div class="rd-value"><div v-if="viewForm.tagColor" :style="{ background: viewForm.tagColor, width: '60px', height: '24px', borderRadius: '4px', display: 'inline-block' }"></div>
                <span v-else>-</span></div></div>
              <div class="rd-item"><span class="rd-label">排序</span><div class="rd-value">{{ viewForm.sort }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="sys_normal_disable" :value="viewForm.status" /></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_other')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_other">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime }}</div></div>
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

<script setup name="MkCustomerTag">
import { listTag, getTag, addTag, updateTag, delTag } from '@/api/mk/tag'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['c_basic', 'c_other', 'v_basic', 'v_other'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_tag_index')
const { marketing_tag_type, sys_normal_disable } = proxy.useDict('marketing_tag_type', 'sys_normal_disable')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const viewForm = ref({})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, tagName: undefined, tagType: undefined, status: undefined, params: {} },
  rules: {
    tagName: [{ required: true, message: '标签名称不能为空', trigger: 'blur' }],
    tagType: [{ required: true, message: '请选择标签类型', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  tagName: { label: '标签名称', visible: true },
  tagType: { label: '标签类型', visible: true },
  tagColor: { label: '颜色', visible: true },
  sort: { label: '排序', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true },
  remark: { label: '备注', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_tag_columns')
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

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.tagName) count++
  if (queryParams.value.tagType) count++
  if (queryParams.value.status) count++
  return count
})

const showAdvanced = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0 })
const statusTabList = computed(() => sys_normal_disable.value)
function loadStatusCounts() {
  // 基于当前筛选条件（剔除状态与分页）拉取全量数据统计，避免仅统计当前页
  const query = { ...queryParams.value, pageNum: 1, pageSize: 9999, status: undefined, params: { ...queryParams.value.params } }
  listTag(query).then(res => {
    const counts = { all: 0, '0': 0, '1': 0 }
    const rows = res.rows || []
    rows.forEach(row => {
      const s = row.status
      if (counts[s] !== undefined) counts[s]++
    })
    counts.all = rows.length
    statusCounts.value = counts
  }).catch(() => {})
}
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === 'all' ? undefined : status; handleQuery() }
function badgeClass(status) { const map = { '0': 'green', '1': 'gray' }; return map[status] || 'gray' }
function statusLabel(status) { const item = sys_normal_disable.value.find(d => d.value == status); return item ? item.label : '-' }
function typeBadgeClass(type) { const map = { '1': 'blue', '2': 'green', '3': 'violet', '4': 'amber' }; return map[type] || 'gray' }
function typeLabel(type) { const item = marketing_tag_type.value.find(d => d.value == type); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '0': 'tab-approved', '1': 'tab-void' }; return map[value] || '' }

function getList() {
  loading.value = true
  listTag(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }).catch(() => { loading.value = false })
}
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.tagName = undefined; queryParams.value.tagType = undefined; queryParams.value.status = undefined; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.tagId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = { tagName: undefined, tagType: '0', tagColor: undefined, sort: 0, status: '0', remark: undefined }
  proxy.resetForm('tagRef')
}
function handleAdd() { reset(); Object.keys(collapsedCards).forEach(k => { if (k.startsWith('c_')) collapsedCards[k] = false }); open.value = true; title.value = '新增客户标签' }
function handleUpdate(row) { reset(); getTag(row.tagId || ids.value[0]).then(res => { form.value = res.data; Object.keys(collapsedCards).forEach(k => { if (k.startsWith('c_')) collapsedCards[k] = false }); open.value = true; title.value = '修改客户标签' }) }
function handleView(row) {
  getTag(row.tagId).then(res => {
    viewForm.value = res.data
    Object.keys(collapsedCards).forEach(k => { if (k.startsWith('v_')) collapsedCards[k] = false })
    viewOpen.value = true
  })
}
function submitForm() {
  proxy.$refs['tagRef'].validate(valid => {
    if (valid) {
      if (form.value.tagId != undefined) { updateTag(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addTag(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const tagIds = row.tagId || ids.value; proxy.$modal.confirm('确认删除编号为"' + tagIds + '"的数据？').then(() => delTag(tagIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('mk/tag/export', { ...queryParams.value }, `tag_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
getList()
</script>

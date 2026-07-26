<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="标签名称" prop="tagName">
        <el-input v-model="queryParams.tagName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="标签类型" prop="tagType">
        <el-select v-model="queryParams.tagType" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_tag_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
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
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:tag:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:tag:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:tag:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:tag:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="标签名称" prop="tagName" :width="colWidth('tagName', 180)" resizable>
        <template #default="scope">
          <el-tag :color="scope.row.tagColor" effect="dark" v-if="scope.row.tagColor">{{ scope.row.tagName }}</el-tag>
          <span v-else>{{ scope.row.tagName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="标签类型" prop="tagType" :width="colWidth('tagType', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_tag_type" :value="scope.row.tagType" /></template>
      </el-table-column>
      <el-table-column label="颜色" prop="tagColor" :width="colWidth('tagColor', 100)" resizable align="center">
        <template #default="scope">
          <div v-if="scope.row.tagColor" :style="{ background: scope.row.tagColor, width: '30px', height: '20px', borderRadius: '4px', display: 'inline-block' }"></div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sort" :width="colWidth('sort', 80)" resizable align="center" />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="sys_normal_disable" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable />
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:tag:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:tag:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

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
  queryParams: { pageNum: 1, pageSize: 10, tagName: undefined, tagType: undefined, status: undefined },
  rules: {
    tagName: [{ required: true, message: '标签名称不能为空', trigger: 'blur' }],
    tagType: [{ required: true, message: '请选择标签类型', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listTag(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
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

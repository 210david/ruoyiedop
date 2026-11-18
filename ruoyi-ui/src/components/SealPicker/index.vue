<template>
  <el-dialog v-model="visible" width="720px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="seal-picker">
      <!-- 搜索栏 -->
      <div class="seal-picker-search">
        <el-input v-model="queryParams.sealName" placeholder="印章名称" clearable size="small" style="width: 200px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 印章表格 -->
      <div class="seal-picker-table">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="list"
          highlight-current-row
          @row-click="onRowClick"
          @row-dblclick="onRowDblClick"
          height="360"
          size="small"
        >
          <el-table-column width="45" align="center">
            <template #default="{ row }">
              <el-radio :model-value="selectedId" :value="row.sealId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="印章名称" prop="sealName" min-width="150" show-overflow-tooltip />
          <el-table-column label="印章类型" prop="sealType" width="110" align="center">
            <template #default="scope"><dict-tag :options="oa_seal_type" :value="scope.row.sealType" /></template>
          </el-table-column>
          <el-table-column label="保管人" prop="keeperName" width="100" show-overflow-tooltip />
          <el-table-column label="状态" prop="status" width="80" align="center">
            <template #default="scope"><dict-tag :options="oa_seal_status" :value="scope.row.status" /></template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="seal-picker-pager">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          small
          @current-change="getList"
        />
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!selectedId">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { pickerSealList, getOaSeal } from '@/api/oa/seal'
import { Search } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { oa_seal_type, oa_seal_status } = proxy.useDict('oa_seal_type', 'oa_seal_status')

const props = defineProps({
  title: {
    type: String,
    default: '选择印章'
  }
})

const emit = defineEmits(['confirm'])

const visible = ref(false)
const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedId = ref(null)
const selectedRow = ref(null)
const tableRef = ref()

/** 打开时由父页面带入的当前选中印章，用于回显 */
let pending = { id: null, name: '' }

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  sealName: undefined
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.sealName = undefined
  queryParams.pageNum = 1
  selectedId.value = pending.id || null
  selectedRow.value = null
  // 修改场景：先回填当前已选印章，避免其不在当前页时确定按钮不可用
  if (pending.id) {
    if (pending.name) {
      selectedRow.value = { sealId: pending.id, sealName: pending.name }
    } else {
      // 调用方只传了ID：补拉详情，防止直接点确定时把印章名称写成空值
      getOaSeal(pending.id).then(res => {
        const d = res.data || {}
        if (selectedId.value === pending.id) {
          selectedRow.value = { sealId: d.sealId || pending.id, sealName: d.sealName }
        }
      }).catch(() => {})
    }
  }
  getList()
}

/** 加载印章列表 */
function getList() {
  loading.value = true
  pickerSealList(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    // 回显选中行：命中当前页时用行数据补全信息
    if (selectedId.value) {
      const hit = (list.value || []).find(r => r.sealId === selectedId.value)
      if (hit) {
        selectedRow.value = hit
      }
      nextTick(() => {
        if (tableRef.value && hit) {
          tableRef.value.setCurrentRow(hit)
        }
      })
    }
  }).catch(() => { loading.value = false })
}

/** 查询 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置 */
function resetQuery() {
  queryParams.sealName = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.sealId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.sealId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value || !selectedRow.value) return
  emit('confirm', {
    sealId: selectedRow.value.sealId,
    sealName: selectedRow.value.sealName
  })
  visible.value = false
}

/**
 * 打开弹窗
 * @param {Number|String} currentSealId 当前已选印章ID（回显用）
 * @param {String} currentSealName 当前已选印章名称（回显用，缺省时仅保留ID）
 */
function open(currentSealId, currentSealName) {
  pending = { id: currentSealId || null, name: currentSealName || '' }
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.seal-picker {
  display: flex;
  flex-direction: column;
}

.seal-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.seal-picker-table {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;

  :deep(.el-table__row) {
    cursor: pointer;

    &:hover > td {
      background: #f0f7ff;
    }
  }

  :deep(.el-table__row.is-current > td) {
    background: #e6f0fd;
  }
}

.seal-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>

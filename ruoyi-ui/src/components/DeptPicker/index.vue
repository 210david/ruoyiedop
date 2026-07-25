<template>
  <el-dialog v-model="visible" width="520px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 21h18"/>
            <path d="M5 21V7l8-4v18"/>
            <path d="M19 21V11l-6-4"/>
            <path d="M9 9v0"/>
            <path d="M9 12v0"/>
            <path d="M9 15v0"/>
            <path d="M9 18v0"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="dept-picker">
      <!-- 搜索栏 -->
      <div class="dept-picker-search">
        <el-input v-model="deptKeyword" placeholder="搜索部门名称" clearable size="small">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>

      <!-- 已选部门信息 -->
      <div class="dept-picker-selected" v-if="selectedDept">
        <el-tag type="success" effect="light" size="small" closable @close="clearSelection">
          <el-icon style="vertical-align: -1px; margin-right: 2px;"><OfficeBuilding /></el-icon>
          {{ selectedDept.label }}
        </el-tag>
      </div>

      <!-- 部门树 -->
      <div class="dept-picker-tree">
        <el-tree
          ref="deptTreeRef"
          :data="treeData"
          :props="{ label: 'label', children: 'children', disabled: 'disabled' }"
          node-key="id"
          :expand-on-click-node="false"
          :default-expand-all="true"
          :filter-node-method="filterDeptNode"
          highlight-current
          @node-click="onNodeClick"
        >
          <template #default="{ data }">
            <div
              class="dept-tree-node"
              :class="{ 'is-selected': data.id === selectedDeptId, 'is-disabled': data.disabled }"
              @dblclick="onNodeDblClick(data)"
            >
              <el-radio
                v-model="selectedDeptId"
                :value="data.id"
                :disabled="data.disabled"
                @click.stop
              >
                <span class="dept-tree-node-content">
                  <span class="node-icon-badge" :class="data.disabled ? 'node-icon-badge--company' : (data.children && data.children.length ? 'node-icon-badge--dept' : 'node-icon-badge--leaf')">
                    <el-icon><OfficeBuilding v-if="data.disabled" /><Folder v-else-if="data.children && data.children.length" /><Document v-else /></el-icon>
                  </span>
                  <span class="dept-tree-node-label" :title="data.label">{{ data.label }}</span>
                </span>
              </el-radio>
            </div>
          </template>
        </el-tree>
      </div>

      <div class="dept-picker-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>点击节点选中，双击节点确认选择</span>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!selectedDeptId">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { deptTreeSelect } from '@/api/system/user'

const props = defineProps({
  title: {
    type: String,
    default: '选择部门'
  },
  /** 需要禁用选择的部门ID列表（如根公司，显示但不可选） */
  disabledIds: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['confirm'])

const visible = ref(false)
const deptOptions = ref([])
const deptKeyword = ref('')
const selectedDeptId = ref(null)
const selectedDept = ref(null)

const deptTreeRef = ref()

/** 部门树数据：标记被禁用的节点 */
const treeData = computed(() => {
  const disableSet = new Set(props.disabledIds)
  function markTree(nodes) {
    return nodes.map(n => {
      const newNode = { ...n }
      if (disableSet.has(n.id)) {
        newNode.disabled = true
      }
      if (n.children && n.children.length) {
        newNode.children = markTree(n.children)
      }
      return newNode
    })
  }
  return markTree(deptOptions.value)
})

/** 过滤部门节点（搜索） */
function filterDeptNode(value, data) {
  if (!value) return true
  return data.label && data.label.indexOf(value) !== -1
}

/** 监听搜索关键词 */
watch(deptKeyword, (val) => {
  if (deptTreeRef.value) {
    deptTreeRef.value.filter(val)
  }
})

/** 弹窗打开时初始化 */
function onOpen() {
  deptKeyword.value = ''
  selectedDeptId.value = null
  selectedDept.value = null
  loadDeptTree()
}

/** 加载部门树 */
function loadDeptTree() {
  deptTreeSelect().then(res => {
    deptOptions.value = res.data
  })
}

/** 递归查找节点 */
function findNode(nodes, id) {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children && n.children.length) {
      const found = findNode(n.children, id)
      if (found) return found
    }
  }
  return null
}

/** 节点点击 - 选中（被禁用的节点不可选） */
function onNodeClick(data) {
  if (data.disabled) return
  selectedDeptId.value = data.id
}

/** 节点双击 - 确认（被禁用的节点不可选） */
function onNodeDblClick(data) {
  if (data.disabled) return
  selectedDeptId.value = data.id
  selectedDept.value = data
  handleConfirm()
}

/** 清除选择 */
function clearSelection() {
  selectedDeptId.value = null
  selectedDept.value = null
  if (deptTreeRef.value) {
    deptTreeRef.value.setCurrentKey(null)
  }
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedDeptId.value) return
  const node = findNode(treeData.value, selectedDeptId.value)
  if (node) {
    emit('confirm', {
      deptId: node.id,
      deptName: node.label
    })
  }
  visible.value = false
}

/** 打开弹窗 */
function open(currentDeptId) {
  visible.value = true
  if (currentDeptId) {
    nextTick(() => {
      selectedDeptId.value = currentDeptId
      if (deptTreeRef.value) {
        deptTreeRef.value.setCurrentKey(currentDeptId)
      }
    })
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.dept-picker {
  display: flex;
  flex-direction: column;
  height: 420px;
}

.dept-picker-search {
  margin-bottom: 10px;
}

.dept-picker-selected {
  margin-bottom: 10px;
  min-height: 24px;
}

.dept-picker-tree {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 8px;
  background: #fafbfc;

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 4px;
  }

  :deep(.el-tree-node__content) {
    height: 34px;
    border-radius: 6px;
    margin-bottom: 2px;

    &:hover {
      background: #f0f7ff;
    }
  }

  :deep(.el-tree-node.is-current > .el-tree-node__content) {
    background: #e6f0fd;
  }
}

.dept-tree-node {
  width: 100%;

  &.is-selected {
    :deep(.el-radio__label) {
      color: #409eff;
      font-weight: 600;
    }
  }

  &.is-disabled {
    cursor: not-allowed;
    opacity: 0.6;

    :deep(.el-radio__label) {
      color: #c0c4cc;
    }
  }

  :deep(.el-radio) {
    height: auto;
    margin-right: 0;
  }

  :deep(.el-radio__label) {
    padding-left: 6px;
  }
}

.dept-tree-node-content {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.node-icon-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 5px;
  flex-shrink: 0;

  .el-icon {
    font-size: 12px;
    color: #fff;
  }

  &--company {
    background: linear-gradient(135deg, #4facfe, #2f8eed);
  }

  &--dept {
    background: linear-gradient(135deg, #f5b454, #e6a23c);
  }

  &--leaf {
    background: linear-gradient(135deg, #67c23a, #529b2e);
  }
}

.dept-tree-node-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dept-picker-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;

  .el-icon {
    font-size: 14px;
  }
}
</style>

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
    <div class="org-picker">
      <!-- 搜索栏 -->
      <div class="org-picker-search">
        <el-input v-model="orgKeyword" placeholder="搜索组织名称" clearable size="small">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>

      <!-- 已选组织信息 -->
      <div class="org-picker-selected" v-if="selectedOrg">
        <el-tag type="success" effect="light" size="small" closable @close="clearSelection">
          <el-icon style="vertical-align: -1px; margin-right: 2px;"><OfficeBuilding /></el-icon>
          {{ selectedOrg.label }}
        </el-tag>
      </div>

      <!-- 组织树 -->
      <div class="org-picker-tree">
        <el-tree
          ref="orgTreeRef"
          :data="orgOptions"
          :props="{ label: 'label', children: 'children' }"
          node-key="id"
          :expand-on-click-node="false"
          :default-expand-all="true"
          :filter-node-method="filterOrgNode"
          highlight-current
          @node-click="onNodeClick"
        >
          <template #default="{ data }">
            <div
              class="org-tree-node"
              :class="{ 'is-selected': data.id === selectedOrgId }"
              @dblclick="onNodeDblClick(data)"
            >
              <el-radio
                v-model="selectedOrgId"
                :value="data.id"
                @click.stop
              >
                <span class="org-tree-node-content">
                  <span class="node-icon-badge" :class="data.children && data.children.length ? 'node-icon-badge--dept' : 'node-icon-badge--leaf'">
                    <el-icon><Folder v-if="data.children && data.children.length" /><Document v-else /></el-icon>
                  </span>
                  <span class="org-tree-node-label" :title="data.label">{{ data.label }}</span>
                </span>
              </el-radio>
            </div>
          </template>
        </el-tree>
      </div>

      <div class="org-picker-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>点击节点选中，双击节点确认选择</span>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!selectedOrgId">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listHrOrganization } from '@/api/hr/hrOrganization'

const props = defineProps({
  title: {
    type: String,
    default: '选择部门'
  }
})

const emit = defineEmits(['confirm'])

const visible = ref(false)
const orgOptions = ref([])
const orgKeyword = ref('')
const selectedOrgId = ref(null)
const selectedOrg = ref(null)

const orgTreeRef = ref()

/** 过滤组织节点（搜索） */
function filterOrgNode(value, data) {
  if (!value) return true
  return data.label && data.label.indexOf(value) !== -1
}

/** 监听搜索关键词 */
watch(orgKeyword, (val) => {
  if (orgTreeRef.value) {
    orgTreeRef.value.filter(val)
  }
})

/** 弹窗打开时初始化 */
function onOpen() {
  orgKeyword.value = ''
  selectedOrg.value = null
  loadOrgTree()
}

/** 加载组织树 */
function loadOrgTree() {
  listHrOrganization({ pageNum: 1, pageSize: 1000 }).then(res => {
    orgOptions.value = buildTree(res.rows || [])
    // 回显选中
    if (selectedOrgId.value) {
      nextTick(() => {
        if (orgTreeRef.value) {
          orgTreeRef.value.setCurrentKey(selectedOrgId.value)
        }
      })
    }
  })
}

/** 平铺列表转树（按 parentId 关联、orderNum 排序） */
function buildTree(rows) {
  const map = {}
  const roots = []
  const sorted = [...rows].sort((a, b) => (a.orderNum || 0) - (b.orderNum || 0))
  sorted.forEach(r => {
    map[r.orgId] = { id: r.orgId, label: r.orgName, code: r.orgCode, children: [] }
  })
  sorted.forEach(r => {
    if (r.parentId && map[r.parentId]) {
      map[r.parentId].children.push(map[r.orgId])
    } else {
      roots.push(map[r.orgId])
    }
  })
  return roots
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

/** 节点点击 - 选中 */
function onNodeClick(data) {
  selectedOrgId.value = data.id
}

/** 节点双击 - 确认 */
function onNodeDblClick(data) {
  selectedOrgId.value = data.id
  handleConfirm()
}

/** 清除选择 */
function clearSelection() {
  selectedOrgId.value = null
  selectedOrg.value = null
  if (orgTreeRef.value) {
    orgTreeRef.value.setCurrentKey(null)
  }
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedOrgId.value) return
  const node = findNode(orgOptions.value, selectedOrgId.value)
  if (node) {
    selectedOrg.value = node
    emit('confirm', {
      orgId: node.id,
      orgCode: node.code,
      orgName: node.label
    })
  }
  visible.value = false
}

/** 打开弹窗 */
function open(currentOrgId) {
  selectedOrgId.value = currentOrgId || null
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.org-picker {
  display: flex;
  flex-direction: column;
  height: 420px;
}

.org-picker-search {
  margin-bottom: 10px;
}

.org-picker-selected {
  margin-bottom: 10px;
  min-height: 24px;
}

.org-picker-tree {
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

.org-tree-node {
  width: 100%;

  &.is-selected {
    :deep(.el-radio__label) {
      color: #409eff;
      font-weight: 600;
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

.org-tree-node-content {
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

  &--dept {
    background: linear-gradient(135deg, #f5b454, #e6a23c);
  }

  &--leaf {
    background: linear-gradient(135deg, #67c23a, #529b2e);
  }
}

.org-tree-node-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.org-picker-tip {
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

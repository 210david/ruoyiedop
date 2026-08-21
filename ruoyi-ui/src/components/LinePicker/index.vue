<template>
  <el-dialog v-model="visible" width="520px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M2 20h20M4 20V8l8-5 8 5v12M9 20v-6h6v6"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="line-picker">
      <!-- 搜索栏 -->
      <div class="line-picker-search">
        <el-input v-model="keyword" placeholder="搜索产线/车间名称" clearable size="small">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>

      <!-- 已选产线信息 -->
      <div class="line-picker-selected" v-if="selectedNode">
        <el-tag type="success" effect="light" size="small" closable @close="clearSelection">
          <el-icon style="vertical-align: -1px; margin-right: 2px;"><Operation /></el-icon>
          {{ selectedNode.lineName }}
          <span class="tag-workshop" v-if="selectedNode.workshopName"> | {{ selectedNode.workshopName }}</span>
        </el-tag>
      </div>

      <!-- 车间-产线树 -->
      <div class="line-picker-tree">
        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="{ label: 'label', children: 'children' }"
          node-key="nodeKey"
          :expand-on-click-node="false"
          :default-expand-all="true"
          :filter-node-method="filterNode"
          highlight-current
          @node-click="onNodeClick"
        >
          <template #default="{ data }">
            <div
              class="tree-node"
              :class="{ 'is-selected': data.nodeKey === selectedNodeKey, 'is-workshop': data.nodeType === 'workshop' }"
              @dblclick="onNodeDblClick(data)"
            >
              <el-radio
                v-if="data.nodeType === 'line'"
                v-model="selectedNodeKey"
                :value="data.nodeKey"
                @click.stop
              >
                <span class="tree-node-content">
                  <span class="node-icon-badge" :class="data.nodeType === 'line' ? 'node-icon-badge--line' : 'node-icon-badge--workshop'">
                    <el-icon><Operation v-if="data.nodeType === 'line'" /><HomeFilled v-else /></el-icon>
                  </span>
                  <span class="tree-node-label" :title="data.label">{{ data.label }}</span>
                </span>
              </el-radio>
              <span v-else class="tree-node-content workshop-node">
                <span class="node-icon-badge node-icon-badge--workshop">
                  <el-icon><HomeFilled /></el-icon>
                </span>
                <span class="tree-node-label" :title="data.label">{{ data.label }}</span>
              </span>
            </div>
          </template>
        </el-tree>
      </div>

      <div class="line-picker-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>点击产线节点选中，双击产线节点确认选择</span>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!selectedNodeKey">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listWorkshop } from '@/api/qms/workshop'
import { Search, InfoFilled, Operation, HomeFilled } from '@element-plus/icons-vue'

const props = defineProps({
  title: {
    type: String,
    default: '选择产线'
  }
})

const emit = defineEmits(['confirm'])

const visible = ref(false)
const loading = ref(false)
const keyword = ref('')
const selectedNodeKey = ref(null)
const selectedNode = ref(null)
const allLines = ref([]) // 扁平化的产线列表（用于回显查找）

const treeRef = ref()

/** 构建车间→产线树形数据 */
const treeData = computed(() => {
  // allLines 已包含 workshopName，按 workshopName 分组构建树
  const workshopMap = {} // workshopName → { workshop info, lines: [] }
  allLines.value.forEach(line => {
    const wName = line.workshopName || '未分类'
    if (!workshopMap[wName]) {
      workshopMap[wName] = {
        nodeKey: 'workshop_' + wName,
        label: wName,
        nodeType: 'workshop',
        children: []
      }
    }
    workshopMap[wName].children.push({
      nodeKey: 'line_' + line.lineId,
      label: line.lineName,
      nodeType: 'line',
      lineId: line.lineId,
      lineName: line.lineName,
      workshopName: wName
    })
  })
  return Object.values(workshopMap)
})

/** 过滤节点（搜索） */
function filterNode(value, data) {
  if (!value) return true
  // 搜索时，车间节点如果自身匹配或子节点匹配则显示
  const kw = value.toLowerCase()
  if (data.label && data.label.toLowerCase().includes(kw)) return true
  // 对于车间节点，检查是否有子节点匹配
  if (data.children && data.children.length) {
    return data.children.some(child => filterNode(value, child))
  }
  return false
}

/** 监听搜索关键词 */
watch(keyword, (val) => {
  if (treeRef.value) {
    treeRef.value.filter(val)
  }
})

/** 弹窗打开时初始化 */
function onOpen() {
  keyword.value = ''
  selectedNodeKey.value = null
  selectedNode.value = null
  loadData()
}

/** 加载产线数据（从QMS车间树获取） */
function loadData() {
  loading.value = true
  listWorkshop({ status: '0' }).then(res => {
    const list = res.data || []
    // 构建车间映射
    const workshopMap = {}
    list.filter(n => n.nodeType === '1').forEach(n => {
      workshopMap[n.workshopId] = n.workshopName
    })
    // 提取产线节点
    allLines.value = list.filter(n => n.nodeType === '2').map(n => ({
      lineId: n.workshopId,
      lineName: n.workshopName,
      workshopName: workshopMap[n.parentId] || ''
    }))
  }).finally(() => {
    loading.value = false
  })
}

/** 节点点击 — 仅产线节点可选 */
function onNodeClick(data) {
  if (data.nodeType !== 'line') return
  selectedNodeKey.value = data.nodeKey
}

/** 节点双击 — 确认选择 */
function onNodeDblClick(data) {
  if (data.nodeType !== 'line') return
  selectedNodeKey.value = data.nodeKey
  selectedNode.value = data
  handleConfirm()
}

/** 清除选择 */
function clearSelection() {
  selectedNodeKey.value = null
  selectedNode.value = null
  if (treeRef.value) {
    treeRef.value.setCurrentKey(null)
  }
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedNodeKey.value) return
  // 在 treeData 中查找选中的节点
  const node = findNodeInTree(treeData.value, selectedNodeKey.value)
  if (node) {
    emit('confirm', {
      lineId: node.lineId,
      lineName: node.lineName,
      workshopName: node.workshopName
    })
  }
  visible.value = false
}

/** 递归查找节点 */
function findNodeInTree(nodes, key) {
  for (const n of nodes) {
    if (n.nodeKey === key) return n
    if (n.children && n.children.length) {
      const found = findNodeInTree(n.children, key)
      if (found) return found
    }
  }
  return null
}

/** 打开弹窗
 * @param currentLineName 当前选中的产线名称（用于回显）
 */
function open(currentLineName) {
  visible.value = true
  if (currentLineName) {
    nextTick(() => {
      // 在 allLines 中查找匹配的产线
      const matched = allLines.value.find(l => l.lineName === currentLineName)
      if (matched) {
        selectedNodeKey.value = 'line_' + matched.lineId
      }
    })
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.line-picker {
  display: flex;
  flex-direction: column;
  height: 420px;
}

.line-picker-search {
  margin-bottom: 10px;
}

.line-picker-selected {
  margin-bottom: 10px;
  min-height: 24px;

  .tag-workshop {
    color: #909399;
    font-size: 12px;
  }
}

.line-picker-tree {
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

.tree-node {
  width: 100%;

  &.is-selected {
    :deep(.el-radio__label) {
      color: #409eff;
      font-weight: 600;
    }
  }

  &.is-workshop {
    cursor: default;
  }

  :deep(.el-radio) {
    height: auto;
    margin-right: 0;
  }

  :deep(.el-radio__label) {
    padding-left: 6px;
  }
}

.tree-node-content {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.workshop-node {
  font-weight: 600;
  color: #303133;
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

  &--workshop {
    background: linear-gradient(135deg, #4facfe, #2f8eed);
  }

  &--line {
    background: linear-gradient(135deg, #67c23a, #529b2e);
  }
}

.tree-node-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.line-picker-tip {
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

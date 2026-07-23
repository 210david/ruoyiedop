<template>
  <div class="apidoc-container" :class="{ 'is-standalone': isStandalone }">
    <!-- 顶部标题栏 -->
    <div class="apidoc-header">
      <div class="header-left">
        <el-icon class="header-icon"><Document /></el-icon>
        <div class="header-text">
          <h1>业务模块接口文档</h1>
          <p>仓库管理(WMS) · 设备管理(DMS) 全接口集中管理</p>
        </div>
      </div>
      <div class="header-stats">
        <el-tag type="primary" effect="dark" size="large">模块 {{ stats.moduleCount }}</el-tag>
        <el-tag type="success" effect="dark" size="large">接口 {{ stats.apiCount }}</el-tag>
        <el-tag type="warning" effect="dark" size="large">GET {{ stats.methodCount.GET }}</el-tag>
        <el-tag type="danger" effect="dark" size="large">POST {{ stats.methodCount.POST }}</el-tag>
        <el-tag type="info" effect="dark" size="large">PUT {{ stats.methodCount.PUT }}</el-tag>
        <el-tag type="primary" effect="plain" size="large">DELETE {{ stats.methodCount.DELETE }}</el-tag>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="apidoc-search">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索接口路径、名称、权限标识..."
        size="large"
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filterMethod" placeholder="请求方式" clearable size="large" style="width: 140px" @change="handleSearch">
        <el-option label="GET" value="GET" />
        <el-option label="POST" value="POST" />
        <el-option label="PUT" value="PUT" />
        <el-option label="DELETE" value="DELETE" />
      </el-select>
      <el-select v-model="filterGroup" placeholder="所属模块组" clearable size="large" style="width: 200px" @change="handleSearch">
        <el-option v-for="g in API_MODULES" :key="g.groupCode" :label="g.group" :value="g.groupCode" />
      </el-select>
    </div>

    <div class="apidoc-body">
      <!-- 左侧导航 -->
      <div class="apidoc-sidebar">
        <div class="sidebar-title">接口导航</div>
        <el-scrollbar style="height: calc(100% - 46px)">
          <div v-for="group in filteredModules" :key="group.groupCode" class="sidebar-group">
            <div class="sidebar-group-title">{{ group.group }}</div>
            <div
              v-for="m in group.modules"
              :key="m.basePath"
              class="sidebar-item"
              :class="{ active: activeModule === m.basePath }"
              @click="scrollToModule(m.basePath)"
            >
              <span class="sidebar-item-name">{{ m.name }}</span>
              <el-badge :value="m.apis.length" class="sidebar-item-badge" type="primary" />
            </div>
          </div>
        </el-scrollbar>
      </div>

      <!-- 右侧内容区 -->
      <div class="apidoc-content">
        <el-scrollbar ref="contentScrollRef" style="height: 100%">
          <div v-for="group in filteredModules" :key="group.groupCode" class="content-group">
            <h2 class="group-title" :id="'group-' + group.groupCode">
              <el-icon><Folder /></el-icon>
              {{ group.group }}
            </h2>
            <p class="group-desc">{{ group.description }}</p>

            <div v-for="m in group.modules" :key="m.basePath" class="module-section" :id="'mod-' + m.basePath.replace(/\//g, '-')">
              <h3 class="module-title">
                <span class="module-name">{{ m.name }}</span>
                <el-tag size="small" type="info" effect="plain">{{ m.basePath }}</el-tag>
              </h3>
              <p class="module-desc">{{ m.description }}</p>

              <el-table
                :data="m.apis"
                border
                stripe
                :row-class-name="tableRowClassName"
                style="width: 100%"
                :default-expand-all="false"
              >
                <el-table-column type="expand">
                  <template #default="{ row }">
                    <div class="api-detail">
                      <!-- 接口说明 -->
                      <div class="detail-section">
                        <div class="detail-label">接口说明</div>
                        <div class="detail-content">{{ row.remark }}</div>
                      </div>

                      <!-- 权限标识 -->
                      <div class="detail-section">
                        <div class="detail-label">权限标识</div>
                        <div class="detail-content">
                          <el-tag size="small" type="warning">{{ row.permission }}</el-tag>
                        </div>
                      </div>

                      <!-- 请求参数 -->
                      <div v-if="row.params && row.params.length" class="detail-section">
                        <div class="detail-label">请求参数</div>
                        <el-table :data="row.params" size="small" border style="width: 100%">
                          <el-table-column prop="name" label="参数名" width="160" />
                          <el-table-column prop="type" label="类型" width="120" />
                          <el-table-column label="位置" width="80">
                            <template #default="{ row: p }">
                              <el-tag size="small" :type="paramPosType(p.in)">{{ p.in || 'query' }}</el-tag>
                            </template>
                          </el-table-column>
                          <el-table-column label="必填" width="70">
                            <template #default="{ row: p }">
                              <el-tag size="small" :type="p.required ? 'danger' : 'info'">{{ p.required ? '是' : '否' }}</el-tag>
                            </template>
                          </el-table-column>
                          <el-table-column prop="desc" label="说明" />
                        </el-table>
                      </div>

                      <!-- 请求示例 -->
                      <div class="detail-section">
                        <div class="detail-label">
                          请求示例
                          <el-button link size="small" @click="copyText(row.requestExample)">
                            <el-icon><CopyDocument /></el-icon> 复制
                          </el-button>
                        </div>
                        <pre class="code-block request-code"><code>{{ row.requestExample }}</code></pre>
                      </div>

                      <!-- 响应示例 -->
                      <div class="detail-section">
                        <div class="detail-label">
                          响应示例
                          <el-button link size="small" @click="copyText(row.responseExample)">
                            <el-icon><CopyDocument /></el-icon> 复制
                          </el-button>
                        </div>
                        <pre class="code-block response-code"><code>{{ row.responseExample }}</code></pre>
                      </div>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="方式" width="90" align="center">
                  <template #default="{ row }">
                    <el-tag :type="methodTagType(row.method)" effect="dark" size="small" style="font-weight: bold">{{ row.method }}</el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="接口路径" min-width="280">
                  <template #default="{ row }">
                    <span class="api-path">{{ row.path }}</span>
                  </template>
                </el-table-column>

                <el-table-column label="接口说明" min-width="220">
                  <template #default="{ row }">
                    <span class="api-summary">{{ row.summary }}</span>
                  </template>
                </el-table-column>

                <el-table-column label="权限" width="200">
                  <template #default="{ row }">
                    <el-tag size="small" type="warning">{{ row.permission }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>

          <!-- 空状态 -->
          <el-empty v-if="filteredModules.length === 0" description="未找到匹配的接口" />
        </el-scrollbar>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Document, Search, Folder, CopyDocument } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { API_MODULES, getStats } from './apiData'

const route = useRoute()
const stats = getStats()

// 是否独立访问模式（免登录直连，不套用 Layout）
const isStandalone = computed(() => !!route.meta.standalone)

const searchKeyword = ref('')
const filterMethod = ref('')
const filterGroup = ref('')
const activeModule = ref('')
const contentScrollRef = ref(null)

// 过滤后的模块数据
const filteredModules = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  const method = filterMethod.value
  const group = filterGroup.value

  return API_MODULES
    .filter(g => !group || g.groupCode === group)
    .map(g => {
      return {
        ...g,
        modules: g.modules
          .map(m => {
            return {
              ...m,
              apis: m.apis.filter(a => {
                // 方式过滤
                if (method && a.method !== method) return false
                // 关键词过滤
                if (keyword) {
                  const hay = (
                    a.path + ' ' +
                    a.summary + ' ' +
                    a.permission + ' ' +
                    m.name + ' ' +
                    m.basePath
                  ).toLowerCase()
                  if (!hay.includes(keyword)) return false
                }
                return true
              })
            }
          })
          .filter(m => m.apis.length > 0)
      }
    })
    .filter(g => g.modules.length > 0)
})

function handleSearch() {
  // 计算属性自动响应，无需额外处理
}

function methodTagType(method) {
  const map = { GET: 'success', POST: 'danger', PUT: 'warning', DELETE: 'info' }
  return map[method] || 'primary'
}

function paramPosType(pos) {
  const map = { path: 'danger', body: 'warning', query: 'primary' }
  return map[pos] || 'info'
}

function tableRowClassName({ row }) {
  return 'api-row-' + row.method.toLowerCase()
}

function scrollToModule(basePath) {
  activeModule.value = basePath
  const id = 'mod-' + basePath.replace(/\//g, '-')
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function copyText(text) {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}
</script>

<style lang="scss" scoped>
.apidoc-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 84px);
  background: #f0f2f5;

  // 独立访问模式（无 Layout 外框）占满整个视口
  &.is-standalone {
    height: 100vh;
  }
}

.apidoc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  flex-shrink: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .header-icon {
      font-size: 42px;
    }

    .header-text {
      h1 {
        margin: 0;
        font-size: 22px;
        font-weight: 600;
      }
      p {
        margin: 4px 0 0;
        font-size: 13px;
        opacity: 0.85;
      }
    }
  }

  .header-stats {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
}

.apidoc-search {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
}

.apidoc-body {
  display: flex;
  flex: 1;
  overflow: hidden;
  padding: 16px;
  gap: 16px;
}

.apidoc-sidebar {
  width: 260px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
  overflow: hidden;

  .sidebar-title {
    height: 46px;
    line-height: 46px;
    padding: 0 16px;
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    border-bottom: 1px solid #ebeef5;
    background: #fafafa;
  }

  .sidebar-group {
    margin-bottom: 8px;
  }

  .sidebar-group-title {
    padding: 8px 16px 4px;
    font-size: 12px;
    font-weight: 600;
    color: #909399;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  .sidebar-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 16px;
    cursor: pointer;
    transition: all 0.2s;
    border-left: 3px solid transparent;

    &:hover {
      background: #f5f7fa;
      border-left-color: #409eff;
    }

    &.active {
      background: #ecf5ff;
      border-left-color: #409eff;
    }

    .sidebar-item-name {
      font-size: 13px;
      color: #606266;
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .sidebar-item-badge {
      margin-left: 8px;
    }
  }
}

.apidoc-content {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.content-group {
  padding: 20px 24px;

  .group-title {
    margin: 0 0 8px;
    font-size: 20px;
    color: #303133;
    display: flex;
    align-items: center;
    gap: 8px;

    .el-icon {
      color: #667eea;
    }
  }

  .group-desc {
    margin: 0 0 20px;
    color: #909399;
    font-size: 13px;
    line-height: 1.6;
  }
}

.module-section {
  margin-bottom: 32px;
  scroll-margin-top: 20px;

  .module-title {
    margin: 0 0 6px;
    font-size: 16px;
    color: #303133;
    display: flex;
    align-items: center;
    gap: 10px;
    padding-bottom: 8px;
    border-bottom: 2px solid #f0f2f5;

    .module-name {
      font-weight: 600;
    }
  }

  .module-desc {
    margin: 0 0 14px;
    color: #909399;
    font-size: 13px;
  }
}

.api-path {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}

.api-summary {
  font-size: 13px;
  color: #606266;
}

// 接口详情展开区
.api-detail {
  padding: 16px 20px;
  background: #fafbfc;

  .detail-section {
    margin-bottom: 18px;

    &:last-child {
      margin-bottom: 0;
    }

    .detail-label {
      font-size: 13px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      gap: 8px;

      .el-button {
        margin-left: auto;
      }
    }

    .detail-content {
      font-size: 13px;
      color: #606266;
      line-height: 1.8;
    }
  }
}

.code-block {
  margin: 0;
  padding: 14px 16px;
  background: #282c34;
  border-radius: 6px;
  overflow-x: auto;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 12.5px;
  line-height: 1.6;

  code {
    color: #abb2bf;
    white-space: pre;
  }
}

.request-code {
  border-left: 4px solid #e6a23c;
}

.response-code {
  border-left: 4px solid #67c23a;
}

// 行样式
:deep(.api-row-get) {
  td {
    border-left: 3px solid #67c23a;
  }
}
:deep(.api-row-post) {
  td {
    border-left: 3px solid #f56c6c;
  }
}
:deep(.api-row-put) {
  td {
    border-left: 3px solid #e6a23c;
  }
}
:deep(.api-row-delete) {
  td {
    border-left: 3px solid #909399;
  }
}
</style>

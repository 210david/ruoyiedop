<template>
  <div class="openapi-doc" :class="{ 'is-standalone': isStandalone }">
    <!-- 顶栏 -->
    <div class="doc-header">
      <div class="header-left">
        <h1>🔧 开放接口文档</h1>
        <span class="version-tag">v{{ baseInfo.version }}</span>
      </div>
      <div class="header-right">
        <el-tag type="success">共 {{ allApis.length }} 个接口</el-tag>
        <el-tag type="primary">{{ apiModules.length }} 个模块</el-tag>
      </div>
    </div>

    <div class="doc-body">
      <!-- 左侧导航 -->
      <div class="doc-sidebar">
        <el-scrollbar style="height: 100%">
          <!-- 概览 -->
          <div class="nav-section" :class="{ active: currentApiId === '__overview__' }" @click="currentApiId = '__overview__'">
            <span class="nav-icon">📖</span> 接口概览
          </div>

          <div v-for="mod in apiModules" :key="mod.code" class="nav-module">
            <div class="nav-module-title">{{ mod.name }}</div>
            <div v-for="cat in getCategories(mod)" :key="cat" class="nav-category">
              <div class="nav-category-title">{{ cat }}</div>
              <div
                v-for="api in mod.apis.filter(a => a.category === cat)"
                :key="api.id"
                class="nav-item"
                :class="{ active: currentApiId === api.id }"
                @click="currentApiId = api.id"
              >
                <span class="method-badge" :class="api.method.toLowerCase()">{{ api.method }}</span>
                <span class="nav-item-text">{{ api.summary }}</span>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <!-- 右侧内容 -->
      <div class="doc-content">
        <el-scrollbar style="height: 100%">
          <!-- 概览页 -->
          <div v-if="currentApiId === '__overview__'" class="overview-page">
            <h2>📖 接口概览</h2>
            <el-alert type="info" :closable="false" class="auth-alert">
              <template #title>鉴权方式</template>
              <p>{{ baseInfo.authDesc }}</p>
            </el-alert>

            <h3>基础信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="基础路径">{{ baseInfo.baseUrl }}</el-descriptions-item>
              <el-descriptions-item label="鉴权方式">{{ baseInfo.authMethod }}</el-descriptions-item>
              <el-descriptions-item label="接口版本">{{ baseInfo.version }}</el-descriptions-item>
              <el-descriptions-item label="更新日期">{{ baseInfo.updated }}</el-descriptions-item>
              <el-descriptions-item label="数据格式">JSON / UTF-8</el-descriptions-item>
              <el-descriptions-item label="请求方式">RESTful (GET只读)</el-descriptions-item>
            </el-descriptions>

            <h3>快速开始</h3>
            <p>1. 联系系统管理员获取 API Key</p>
            <p>2. 在请求头中携带 <code>X-API-Key</code> 发起请求</p>
            <pre class="code-block">curl -H "X-API-Key: YOUR_API_KEY" http://your-server:8081/openapi/v1/wms/material/list</pre>

            <h3>响应结构</h3>
            <el-table :data="responseStructures" border>
              <el-table-column prop="code" label="HTTP状态码" width="120" />
              <el-table-column prop="desc" label="说明" />
              <el-table-column prop="example" label="示例" min-width="200" />
            </el-table>

            <h3>模块总览</h3>
            <el-table :data="apiModules" border>
              <el-table-column prop="name" label="模块名称" width="200" />
              <el-table-column prop="code" label="模块编码" width="100" />
              <el-table-column prop="desc" label="包含功能" />
              <el-table-column label="接口数" width="80">
                <template #default="{ row }">{{ row.apis.length }}</template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 接口详情页 -->
          <div v-else class="api-detail-page">
            <div class="api-header">
              <span class="method-tag" :class="currentApi.method.toLowerCase()">{{ currentApi.method }}</span>
              <code class="api-path">{{ currentApi.path }}</code>
            </div>
            <h2>{{ currentApi.summary }}</h2>
            <p class="api-desc">{{ currentApi.desc }}</p>

            <!-- 请求参数 -->
            <h3>请求参数</h3>
            <el-table :data="currentApi.params" border size="small">
              <el-table-column prop="name" label="参数名" width="180" />
              <el-table-column prop="type" label="类型" width="100" />
              <el-table-column label="必填" width="70">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.required ? 'danger' : 'info'">{{ row.required ? '是' : '否' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="位置" width="80">
                <template #default="{ row }">{{ row.in || 'query' }}</template>
              </el-table-column>
              <el-table-column prop="desc" label="说明" min-width="200" />
            </el-table>

            <!-- 响应字段 -->
            <template v-if="currentApi.responseFields && currentApi.responseFields.length">
              <h3>响应字段说明</h3>
              <el-table :data="currentApi.responseFields" border size="small">
                <el-table-column prop="name" label="字段名" width="180" />
                <el-table-column prop="type" label="类型" width="120" />
                <el-table-column prop="desc" label="说明" min-width="250" />
              </el-table>
            </template>

            <!-- 响应示例 -->
            <h3>响应示例</h3>
            <pre class="code-block json-block"><code>{{ currentApi.responseExample }}</code></pre>

            <!-- 调用示例 -->
            <h3>调用示例</h3>
            <el-tabs class="code-tabs">
              <el-tab-pane label="cURL">
                <pre class="code-block"><code>{{ currentApi.codes.curl }}</code></pre>
              </el-tab-pane>
              <el-tab-pane label="Java">
                <pre class="code-block"><code>{{ currentApi.codes.java }}</code></pre>
              </el-tab-pane>
              <el-tab-pane label="Python">
                <pre class="code-block"><code>{{ currentApi.codes.python }}</code></pre>
              </el-tab-pane>
              <el-tab-pane label="JavaScript">
                <pre class="code-block"><code>{{ currentApi.codes.js }}</code></pre>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-scrollbar>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { API_BASE_INFO, API_MODULES, ALL_APIS } from './apiData'

const route = useRoute()
const baseInfo = API_BASE_INFO
const apiModules = API_MODULES
const allApis = ALL_APIS

const isStandalone = computed(() => !!route.meta.standalone)

const currentApiId = ref('__overview__')

const currentApi = computed(() => allApis.find(a => a.id === currentApiId.value) || {})

const responseStructures = [
  { code: '200', desc: '操作成功', example: '{"code":200,"msg":"成功","data":{}}' },
  { code: '401', desc: 'API Key无效或已停用', example: '{"code":401,"msg":"无效的API Key"}' },
  { code: '403', desc: '无权访问该模块', example: '{"code":403,"msg":"无权访问模块"}' },
  { code: '500', desc: '服务器内部错误', example: '{"code":500,"msg":"操作失败"}' }
]

function getCategories(mod) {
  return [...new Set(mod.apis.map(a => a.category))]
}
</script>

<style lang="scss" scoped>
.openapi-doc {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 84px);
  background: #f5f7fa;

  &.is-standalone {
    height: 100vh;
  }
}

.doc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  color: #fff;
  flex-shrink: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    h1 { margin: 0; font-size: 20px; }
    .version-tag {
      background: rgba(255,255,255,0.2);
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 12px;
    }
  }
  .header-right {
    display: flex;
    gap: 8px;
  }
}

.doc-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.doc-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  overflow: hidden;

  .nav-section {
    padding: 12px 16px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    border-bottom: 1px solid #f0f0f0;
    &:hover { background: #f5f7fa; }
    &.active { background: #ecf5ff; color: #409eff; }
  }

  .nav-module {
    .nav-module-title {
      padding: 10px 16px 6px;
      font-size: 13px;
      font-weight: 700;
      color: #303133;
      border-top: 1px solid #f0f0f0;
    }
    .nav-category-title {
      padding: 4px 24px;
      font-size: 12px;
      color: #909399;
    }
    .nav-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 6px 16px 6px 36px;
      cursor: pointer;
      font-size: 13px;
      &:hover { background: #f5f7fa; }
      &.active { background: #ecf5ff; color: #409eff; border-right: 3px solid #409eff; }
    }
  }
}

.method-badge {
  display: inline-block;
  width: 38px;
  text-align: center;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 0;
  border-radius: 3px;
  flex-shrink: 0;
  &.get { background: #e8f5e9; color: #2e7d32; }
  &.post { background: #fce4ec; color: #c62828; }
  &.put { background: #fff3e0; color: #e65100; }
  &.delete { background: #ede7f6; color: #4527a0; }
}

.doc-content {
  flex: 1;
  overflow: hidden;
  padding: 24px 32px;
  background: #fff;
}

.overview-page, .api-detail-page {
  max-width: 900px;
  margin: 0 auto;

  h2 { margin-top: 0; color: #303133; }
  h3 { margin: 24px 0 12px; color: #303133; border-left: 4px solid #409eff; padding-left: 8px; }
  p { line-height: 1.8; color: #606266; }
  code { background: #f0f0f0; padding: 2px 6px; border-radius: 3px; font-family: Consolas, monospace; }
}

.api-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;

  .method-tag {
    font-size: 14px;
    font-weight: 700;
    padding: 4px 12px;
    border-radius: 4px;
    &.get { background: #e8f5e9; color: #2e7d32; }
    &.post { background: #fce4ec; color: #c62828; }
    &.put { background: #fff3e0; color: #e65100; }
    &.delete { background: #ede7f6; color: #4527a0; }
  }
  .api-path { font-size: 16px; font-weight: 600; color: #303133; }
}

.api-desc { color: #606266; font-size: 14px; line-height: 1.8; margin: 12px 0 24px; }

.code-block {
  background: #1e1e2e;
  color: #cdd6f4;
  padding: 16px;
  border-radius: 8px;
  font-family: Consolas, 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
  code { background: none; color: inherit; padding: 0; }
}

.json-block { max-height: 400px; overflow-y: auto; }

.code-tabs {
  :deep(.el-tabs__header) { margin-bottom: 0; }
  :deep(.el-tabs__content) { padding: 0; }
}

.auth-alert { margin: 16px 0; }
.nav-icon { margin-right: 4px; }
</style>

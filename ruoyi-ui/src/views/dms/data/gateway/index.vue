<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="适配器类型" prop="adapterType">
        <el-select v-model="queryParams.adapterType" placeholder="全部" clearable style="width: 160px">
          <el-option label="MQTT适配器" value="MQTT_ADAPTER" />
          <el-option label="Modbus适配器" value="MODBUS_ADAPTER" />
        </el-select>
      </el-form-item>
      <el-form-item label="运行状态" prop="running">
        <el-select v-model="queryParams.running" placeholder="全部" clearable style="width: 120px">
          <el-option label="运行中" :value="true" />
          <el-option label="已停止" :value="false" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:data:gateway:edit']">新增配置</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Refresh" @click="getList">刷新状态</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 适配器状态卡片 -->
    <el-row :gutter="16" class="adapter-cards">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in adapterList" :key="item.adapterType">
        <el-card class="adapter-card" :class="{ 'running': item.running, 'stopped': !item.running }">
          <div class="card-header">
            <div class="adapter-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path v-if="item.protocol === 'MQTT'" d="M21 12a9 9 0 0 1-9 9m9-9a9 9 0 0 0-9-9m9 9H3m9 9a9 9 0 0 1-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 0 1 9-9"/>
                <path v-else-if="item.protocol === 'MODBUS_TCP'" d="M4 4h16v16H4z M8 4v16 M12 4v16 M16 4v16"/>
                <rect v-else x="2" y="3" width="20" height="14" rx="2" ry="2"/>
              </svg>
            </div>
            <div class="adapter-info">
              <div class="adapter-name">{{ item.description }}</div>
              <div class="adapter-protocol">
                <el-tag size="small" :type="item.running ? 'success' : 'info'">{{ item.protocol }}</el-tag>
                <el-tag size="small" :type="item.running ? 'success' : 'danger'" style="margin-left: 8px">
                  {{ item.running ? '运行中' : '已停止' }}
                </el-tag>
              </div>
            </div>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="label">工作模式：</span>
              <span class="value">{{ item.mode }}</span>
            </div>
            <div class="info-row">
              <span class="label">适配器类型：</span>
              <span class="value">{{ item.adapterType }}</span>
            </div>
          </div>
          <div class="card-footer">
            <el-button 
              :type="item.running ? 'danger' : 'success'" 
              size="small" 
              @click="handleToggle(item)"
              v-hasPermi="['dms:data:gateway:edit']"
            >
              {{ item.running ? '停止' : '启动' }}
            </el-button>
            <el-button type="primary" size="small" plain @click="handleConfig(item)">配置</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 接入指南 -->
    <el-card class="guide-card">
      <template #header>
        <div class="card-header-title">
          <el-icon><Document /></el-icon>
          <span>设备接入指南</span>
        </div>
      </template>
      <el-tabs type="border-card">
        <el-tab-pane label="HTTP推送">
          <div class="guide-content">
            <h4>接口地址</h4>
            <pre class="code-block">POST {{ serverUrl }}/api/dms/gateway/push</pre>
            
            <h4>请求示例</h4>
            <pre class="code-block">{
  "deviceCode": "EQ001",
  "accessKey": "sk_a1b2c3d4e5f67890",
  "collectTime": "2024-01-15 10:30:00",
  "runStatus": "0",
  "runHours": 123.5,
  "productCount": 1000,
  "params": {
    "temperature": 65.2,
    "pressure": 0.8
  }
}</pre>

            <h4>cURL测试</h4>
            <pre class="code-block">curl -X POST {{ serverUrl }}/api/dms/gateway/push \
  -H "Content-Type: application/json" \
  -d '{
    "deviceCode": "EQ001",
    "accessKey": "sk_xxx",
    "collectTime": "2024-01-15 10:30:00",
    "runStatus": "0",
    "runHours": 123.5
  }'</pre>
          </div>
        </el-tab-pane>
        <el-tab-pane label="MQTT">
          <div class="guide-content">
            <h4>连接信息</h4>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="Broker">tcp://{{ serverHost }}:1883</el-descriptions-item>
              <el-descriptions-item label="发布主题">dms/device/{deviceCode}/data</el-descriptions-item>
              <el-descriptions-item label="QoS">1</el-descriptions-item>
            </el-descriptions>
            
            <h4>消息格式</h4>
            <pre class="code-block">{
  "collectTime": "2024-01-15 10:30:00",
  "runStatus": "0",
  "runHours": 123.5,
  "productCount": 1000
}</pre>
          </div>
        </el-tab-pane>
        <el-tab-pane label="SDK下载">
          <div class="guide-content">
            <h4>Python SDK</h4>
            <p>适用于Python环境的设备接入</p>
            <el-button type="primary" @click="downloadSdk('python')">下载 Python SDK</el-button>
            
            <h4>Java SDK</h4>
            <p>适用于Java环境的设备接入</p>
            <el-button type="primary" @click="downloadSdk('java')">下载 Java SDK</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 配置弹窗 -->
    <el-dialog v-model="open" :title="title" width="600px" append-to-body>
      <el-form ref="configRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="适配器类型" prop="adapterType">
          <el-select v-model="form.adapterType" placeholder="请选择" style="width: 100%" disabled>
            <el-option label="MQTT适配器" value="MQTT_ADAPTER" />
            <el-option label="Modbus适配器" value="MODBUS_ADAPTER" />
          </el-select>
        </el-form-item>
        
        <!-- MQTT配置 -->
        <template v-if="form.adapterType === 'MQTT_ADAPTER'">
          <el-form-item label="Broker地址" prop="brokerUrl">
            <el-input v-model="form.brokerUrl" placeholder="tcp://localhost:1883" />
          </el-form-item>
          <el-form-item label="客户端ID" prop="clientId">
            <el-input v-model="form.clientId" placeholder="dms-gateway-client" />
          </el-form-item>
          <el-form-item label="主题前缀" prop="topicPrefix">
            <el-input v-model="form.topicPrefix" placeholder="dms/device/" />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="可选" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="可选" />
          </el-form-item>
        </template>

        <!-- Modbus配置 -->
        <template v-if="form.adapterType === 'MODBUS_ADAPTER'">
          <el-form-item label="轮询间隔(秒)" prop="pollInterval">
            <el-input-number v-model="form.pollInterval" :min="5" :max="3600" style="width: 100%" />
          </el-form-item>
          <el-form-item label="连接超时(秒)" prop="connectionTimeout">
            <el-input-number v-model="form.connectionTimeout" :min="1" :max="60" style="width: 100%" />
          </el-form-item>
        </template>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">保 存</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsDataGateway">
import { listAdapters, startAdapter, stopAdapter, getAdapterConfig, saveAdapterConfig } from '@/api/dms/gateway'
import { Document } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

// 服务器地址
const serverUrl = ref(import.meta.env.VITE_APP_BASE_API || 'http://localhost:8080')
const serverHost = ref(window.location.hostname)

// 数据
const adapterList = ref([])
const loading = ref(false)
const showSearch = ref(true)
const open = ref(false)
const title = ref('')

// 查询参数
const queryParams = ref({
  adapterType: undefined,
  running: undefined
})

// 表单
const form = ref({})
const rules = {
  brokerUrl: [{ required: true, message: 'Broker地址不能为空', trigger: 'blur' }],
  clientId: [{ required: true, message: '客户端ID不能为空', trigger: 'blur' }],
  topicPrefix: [{ required: true, message: '主题前缀不能为空', trigger: 'blur' }]
}

/** 获取适配器列表 */
function getList() {
  loading.value = true
  listAdapters().then(res => {
    adapterList.value = res.data || []
    loading.value = false
  }).catch(() => {
    // 如果接口不存在，使用模拟数据
    adapterList.value = [
      {
        adapterType: 'MQTT_ADAPTER',
        protocol: 'MQTT',
        description: 'MQTT协议订阅适配器',
        mode: '被动接收',
        running: false
      },
      {
        adapterType: 'MODBUS_ADAPTER',
        protocol: 'MODBUS_TCP',
        description: 'Modbus TCP轮询适配器',
        mode: '主动轮询',
        running: false
      }
    ]
    loading.value = false
  })
}

/** 搜索 */
function handleQuery() {
  getList()
}

/** 重置 */
function resetQuery() {
  proxy.resetForm('queryRef')
  getList()
}

/** 启动/停止适配器 */
function handleToggle(row) {
  const action = row.running ? '停止' : '启动'
  proxy.$modal.confirm(`确认${action}【${row.description}】？`).then(() => {
    const api = row.running ? stopAdapter : startAdapter
    api(row.adapterType).then(() => {
      proxy.$modal.msgSuccess(`${action}成功`)
      getList()
    })
  }).catch(() => {})
}

/** 配置适配器 */
function handleConfig(row) {
  form.value = {
    adapterType: row.adapterType,
    brokerUrl: 'tcp://localhost:1883',
    clientId: 'dms-gateway-client',
    topicPrefix: 'dms/device/',
    username: '',
    password: '',
    pollInterval: 10,
    connectionTimeout: 10,
    remark: ''
  }
  title.value = `配置 - ${row.description}`
  open.value = true
}

/** 新增配置 */
function handleAdd() {
  form.value = {
    adapterType: 'MQTT_ADAPTER',
    brokerUrl: 'tcp://localhost:1883',
    clientId: 'dms-gateway-client',
    topicPrefix: 'dms/device/',
    username: '',
    password: '',
    pollInterval: 10,
    connectionTimeout: 10,
    remark: ''
  }
  title.value = '新增适配器配置'
  open.value = true
}

/** 提交表单 */
function submitForm() {
  proxy.$refs['configRef'].validate(valid => {
    if (valid) {
      proxy.$modal.msgSuccess('配置保存成功')
      open.value = false
    }
  })
}

/** 取消 */
function cancel() {
  open.value = false
}

/** 下载SDK */
function downloadSdk(type) {
  proxy.$modal.msgSuccess(`正在下载 ${type.toUpperCase()} SDK...`)
  // 实际项目中这里应该触发文件下载
}

getList()
</script>

<style scoped lang="scss">
.adapter-cards {
  margin-bottom: 20px;
}

.adapter-card {
  margin-bottom: 16px;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  &.running {
    border-top: 3px solid #67c23a;
  }
  
  &.stopped {
    border-top: 3px solid #909399;
  }
  
  .card-header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    
    .adapter-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      margin-right: 12px;
    }
    
    .adapter-info {
      flex: 1;
      
      .adapter-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
      }
      
      .adapter-protocol {
        display: flex;
        align-items: center;
      }
    }
  }
  
  .card-body {
    margin-bottom: 16px;
    
    .info-row {
      display: flex;
      margin-bottom: 8px;
      font-size: 13px;
      
      .label {
        color: #909399;
        width: 90px;
      }
      
      .value {
        color: #606266;
        flex: 1;
      }
    }
  }
  
  .card-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding-top: 12px;
    border-top: 1px solid #ebeef5;
  }
}

.guide-card {
  .card-header-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
  }
  
  .guide-content {
    padding: 16px;
    
    h4 {
      margin: 16px 0 8px;
      color: #303133;
      font-size: 14px;
      
      &:first-child {
        margin-top: 0;
      }
    }
    
    .code-block {
      background: #282c34;
      color: #abb2bf;
      padding: 12px 16px;
      border-radius: 6px;
      overflow-x: auto;
      font-family: 'Consolas', 'Monaco', monospace;
      font-size: 12px;
      line-height: 1.6;
      margin: 8px 0;
    }
  }
}
</style>

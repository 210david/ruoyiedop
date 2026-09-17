import request from '@/utils/request'

// ===== 提示词模板 =====
export function listPrompt() {
  return request({ url: '/ai/prompt/list', method: 'get' })
}
export function updatePrompt(data) {
  return request({ url: '/ai/prompt', method: 'put', data: data })
}
export function resetPrompt(configKey) {
  return request({ url: '/ai/prompt/' + configKey, method: 'delete' })
}
export function getPromptDefaults() {
  return request({ url: '/ai/prompt/defaults', method: 'get' })
}

// ===== NL2Query 智能查询 =====
export function nlQuery(question) {
  return request({ url: '/ai/nlquery/query', method: 'post', data: { question }, timeout: 90000 })
}

// 流式查询：POST + fetch ReadableStream 解析 SSE（EventSource 不支持 POST/自定义头）
export function nlQueryStream(question, onStage, onResult, signal) {
  const baseURL = import.meta.env.VITE_APP_BASE_API || ''
  const token = getToken()
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), 60000)
  let externalAbort = false
  if (signal) {
    signal.addEventListener('abort', () => { externalAbort = true; controller.abort() })
  }
  return fetch(baseURL + '/ai/nlquery/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
    body: JSON.stringify({ question }),
    signal: controller.signal
  }).then(async resp => {
    if (!resp.ok || !resp.body) throw new Error('stream unavailable')
    const reader = resp.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buf = ''
    for (;;) {
      const { done, value } = await reader.read()
      if (done) break
      buf += decoder.decode(value, { stream: true })
      let idx
      while ((idx = buf.indexOf('\n\n')) >= 0) {
        const block = buf.slice(0, idx)
        buf = buf.slice(idx + 2)
        let event = 'message', data = ''
        for (const line of block.split('\n')) {
          if (line.startsWith(':')) continue
          if (line.startsWith('event:')) event = line.slice(6).trim()
          else if (line.startsWith('data:')) data += line.slice(5).trim()
        }
        if (!data) continue
        try {
          const payload = JSON.parse(data)
          if (event === 'stage') onStage && onStage(payload.stage, payload.status)
          else if (event === 'result') onResult && onResult(payload)
        } catch (e) { /* 跳过解析失败的块 */ }
      }
    }
  }).finally(() => {
    clearTimeout(timer)
    // 60秒超时与外部取消通过错误名区分，交由调用方处理
    return Promise.reject(Object.assign(new Error('timeout'), { isTimeout: !externalAbort, isExternal: externalAbort }))
  })
}

// ===== WMS AI辅助 =====
export function aiStockAnalysis(limit = 50) {
  return request({ url: '/wms/ai/stockAnalysis', method: 'get', params: { limit }, timeout: 90000 })
}
export function aiStaleInventory() {
  return request({ url: '/wms/ai/staleInventory', method: 'get', timeout: 15000 })
}
export function aiStockTakeAnalysis(takeId) {
  return request({ url: '/wms/ai/stockTakeAnalysis/' + takeId, method: 'get', timeout: 90000 })
}

// ===== MK AI辅助 =====
export function aiCustomerChurnReport() {
  return request({ url: '/mk/ai/customerChurnReport', method: 'get', timeout: 90000 })
}
export function aiSalesWeeklyReport() {
  return request({ url: '/mk/ai/weeklyReport', method: 'get', timeout: 90000 })
}

// ===== HR AI辅助（不涉及薪资，已确认禁区） =====
export function aiAttendanceSummary(yearMonth) {
  return request({ url: '/hr/ai/attendanceSummary/' + yearMonth, method: 'get', timeout: 90000 })
}

// ===== Safety AI辅助 =====
export function aiHazardAdvice(hazardId) {
  return request({ url: '/safety/ai/hazardAdvice/' + hazardId, method: 'get', timeout: 90000 })
}
export function aiTrainingQuiz(recordId) {
  return request({ url: '/safety/ai/trainingQuiz/' + recordId, method: 'get', timeout: 90000 })
}

// ===== P2 预测类 =====
// 设备健康度评分+故障预测（单台）
export function aiEquipmentHealth(equipmentId) {
  return request({ url: '/dms/ai/equipmentHealth/' + equipmentId, method: 'get', timeout: 90000 })
}
// 设备健康度总览（风险排名）
export function aiEquipmentHealthOverview(limit = 10) {
  return request({ url: '/dms/ai/equipmentHealthOverview', method: 'get', params: { limit }, timeout: 90000 })
}
// 备件消耗预测/采购建议
export function aiSparepartForecast() {
  return request({ url: '/dms/ai/sparepartForecast', method: 'get', timeout: 90000 })
}
// 工单全流程 Agent（诊断→备件→派工）
export function aiWorkorderAgent(orderId) {
  return request({ url: '/dms/ai/agent/' + orderId, method: 'get', timeout: 120000 })
}
// MK 客户流失预测
export function aiChurnPrediction() {
  return request({ url: '/mk/ai/churnPrediction', method: 'get', timeout: 90000 })
}

// ===== AI配置分区保存：仅更新RAG向量检索三项 =====
export function updateAiEmbeddingConfig(data) {
  return request({ url: '/ai/center/config/embedding', method: 'put', data: data })
}

// ===== RAG embedding 连接测试 =====
export function testEmbedding(data) {
  return request({ url: '/ai/center/config/testEmbedding', method: 'post', data: data, timeout: 45000 })
}

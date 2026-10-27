import io
p=r'E:\AIcode\EDOP\RuoYi\RuoYi-Vue3\ruoyi-ui\src\views\mms\abnormal\index.vue'
s=io.open(p,encoding='utf-8').read()

# 1. 处理关闭弹窗 footer 前插入 AI按钮+面板
old='''      <template #footer><el-button type="primary" @click="submitResolve">确 定</el-button><el-button @click="resolveOpen = false">取 消</el-button></template>'''
new='''      <AiAssistButton button-text="AI诊断建议" :api="aiAbnormalAdvice" :params="[resolveForm.abnormalId]"
        style="margin-left: 16px" @result="onAbnormalAdvice" />
      <AiResultPanel :visible="abnormalAdviceVisible" :content="abnormalAdviceText" :latency-ms="abnormalAdviceLatency"
        @close="abnormalAdviceVisible = false" />
      <template #footer><el-button type="primary" @click="submitResolve">确 定</el-button><el-button @click="resolveOpen = false">取 消</el-button></template>'''
assert old in s, 'resolve footer not found'
s=s.replace(old,new)

# 2. import（找到第一个 api import 行附加）
import re
m=re.search(r"import \{[^}]*\} from '@/api/mms/abnormal'\n", s)
assert m, 'api import not found'
s=s.replace(m.group(0), m.group(0)+"import { aiAbnormalAdvice } from '@/api/ai/assist'\nimport AiAssistButton from '@/components/AiAssist/AiAssistButton.vue'\nimport AiResultPanel from '@/components/AiAssist/AiResultPanel.vue'\n")

# 3. script 追加状态与回调（追加在 handleResolve 函数后）
m=re.search(r'function handleResolve\(row\) \{.*?\n\}\n', s, re.S)
assert m, 'handleResolve not found'
addition='''
// ===== AI诊断建议（外挂式增量） =====
const abnormalAdviceVisible = ref(false)
const abnormalAdviceText = ref('')
const abnormalAdviceLatency = ref(0)

function onAbnormalAdvice(d) {
  const parsed = d.parsed || {}
  const lines = []
  if (Array.isArray(parsed.possibleCauses) && parsed.possibleCauses.length) {
    lines.push('可能原因（按可能性排序）：')
    parsed.possibleCauses.forEach((c, i) => lines.push('  ' + (i + 1) + '. ' + c))
  }
  if (parsed.urgentAction) lines.push('紧急处置建议：' + parsed.urgentAction)
  if (parsed.rootCauseHint) lines.push('根因排查方向：' + parsed.rootCauseHint)
  if (parsed.preventiveAdvice) lines.push('预防再发建议：' + parsed.preventiveAdvice)
  if (parsed.riskLevel) lines.push('建议关注等级：' + parsed.riskLevel)
  if (!lines.length) lines.push(d.content || d.error || 'AI未返回有效建议')
  abnormalAdviceText.value = lines.join('\\n')
  abnormalAdviceLatency.value = d.latencyMs || 0
  abnormalAdviceVisible.value = true
}
'''
s=s.replace(m.group(0), m.group(0)+addition)
io.open(p,'w',encoding='utf-8').write(s)
print('ok')

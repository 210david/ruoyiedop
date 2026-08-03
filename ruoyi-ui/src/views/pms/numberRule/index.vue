<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="24" :xs="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
          <el-form-item label="规则编码" prop="ruleCode"><el-input v-model="queryParams.ruleCode" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
          <el-form-item label="规则名称" prop="ruleName"><el-input v-model="queryParams.ruleName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
          <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 200px"><el-option v-for="d in sys_normal_disable" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
          <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:numberRule:add']">新增</el-button></el-col>
          <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:numberRule:edit']">修改</el-button></el-col>
          <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['pms:numberRule:export']">导出</el-button></el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="规则编码" prop="ruleCode" :width="colWidth('ruleCode', 160)" resizable />
          <el-table-column label="规则名称" prop="ruleName" :width="colWidth('ruleName', 140)" resizable />
          <el-table-column label="前缀" prop="prefix" :width="colWidth('prefix', 80)" resizable align="center">
            <template #default="scope">
              <span v-if="scope.row.prefix">{{ scope.row.prefix }}</span>
              <el-tag v-else-if="scope.row.prefixFieldEnabled === '1'" type="success" size="small">动态</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="日期格式" prop="dateFormat" width="120" align="center">
            <template #default="scope">
              <span v-if="scope.row.dateFormat">{{ scope.row.dateFormat }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="重置类型" prop="resetType" width="100" align="center">
            <template #default="scope"><dict-tag :options="mk_number_reset_type" :value="scope.row.resetType" /></template>
          </el-table-column>
          <el-table-column label="序列号长度" prop="seqLength" width="100" align="center" />
          <el-table-column label="当前序列号" prop="currentSeq" width="100" align="center" />
          <el-table-column label="预览编号" prop="preview" width="200">
            <template #default="scope">
              <el-tag type="primary">{{ scope.row.preview }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="80" align="center">
            <template #default="scope"><dict-tag :options="sys_normal_disable" :value="scope.row.status" /></template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" show-overflow-tooltip />
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['pms:numberRule:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pms:numberRule:edit']">修改</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </el-col>
    </el-row>

    <!-- 新增/修改/查看 对话框 -->
    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="9" x2="20" y2="9"/><line x1="4" y1="15" x2="20" y2="15"/><line x1="10" y1="3" x2="8" y2="21"/><line x1="16" y1="3" x2="14" y2="21"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.ruleCode">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编码：{{ form.ruleCode }}</span>
          </div>
        </div>
      </template>
      <el-form ref="ruleRef" :model="form" :rules="rules" label-width="110px" :disabled="isView">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
            <el-row>
              <el-col :span="12">
                <el-form-item label="所属模块">
                  <el-tag type="info">采购管理</el-tag>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="规则编码" prop="ruleCode">
                  <el-input v-model="form.ruleCode" placeholder="如：purchase_request" :disabled="form.ruleId != undefined" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="规则名称" prop="ruleName">
                  <el-input v-model="form.ruleName" placeholder="如：采购申请编号" :disabled="form.ruleId != undefined" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status">
                    <el-radio v-for="d in sys_normal_disable" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>

        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('format')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></span>编号格式</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.format }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.format">
            <el-row>
              <el-col :span="24">
                <el-form-item label="序号重置类型" prop="resetType">
                  <el-radio-group v-model="form.resetType">
                    <el-radio v-for="d in mk_number_reset_type" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="编号前缀" prop="prefix">
                  <el-input v-model="form.prefix" placeholder="如：CG" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="连接符" prop="connector">
                  <el-input v-model="form.connector" placeholder="前缀与日期间连接符，如：-" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="日期格式" prop="dateFormat">
                  <el-select v-model="form.dateFormat" placeholder="请选择" :disabled="form.resetType !== '0' && form.resetType !== '4'" style="width: 100%">
                    <el-option label="不包含日期" value="" />
                    <el-option label="yyyy (按年)" value="yyyy" />
                    <el-option label="yyyyMM (按月)" value="yyyyMM" />
                    <el-option label="yyyyMMdd (按天)" value="yyyyMMdd" />
                  </el-select>
                  <span v-if="form.resetType !== '0' && form.resetType !== '4'" style="color: #909399; font-size: 12px;">由重置类型自动决定，不可手动修改</span>
                  <span v-if="form.resetType === '4'" style="color: #909399; font-size: 12px;">按动态前缀重置时可自由选择日期格式，每个前缀维护独立序号</span>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>

        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('seq')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>序列号设置</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.seq }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.seq">
            <el-row>
              <el-col :span="12">
                <el-form-item label="序列号长度" prop="seqLength">
                  <el-input-number v-model="form.seqLength" :min="1" :max="10" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="序列号起始值" prop="seqStart">
                  <el-input-number v-model="form.seqStart" :min="1" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="增长步长" prop="step">
                  <el-input-number v-model="form.step" :min="1" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="当前序列号">
                  <el-input-number v-model="form.currentSeq" :min="0" controls-position="right" style="width: 100%" :disabled="true" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>

        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('preview')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg></span>预览与备注</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.preview }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.preview">
            <el-row>
              <el-col :span="24">
                <el-form-item label="预览编号">
                  <el-tag type="primary" size="large">{{ previewText }}</el-tag>
                  <el-button link type="primary" style="margin-left: 10px" @click="refreshPreview" :disabled="!form.ruleCode || isView">刷新预览</el-button>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="备注" prop="remark">
                  <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm" v-if="!isView">确 定</el-button>
          <el-button @click="cancel">{{ isView ? '关 闭' : '取 消' }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PmsNumberRule">
import { listNumberRule, getNumberRule, addNumberRule, updateNumberRule } from '@/api/mk/numberRule'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'

const { collapsedCards, toggleCard } = useDetailCard(["basic","format","seq","preview"])
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('pms_numberRule_index')
const { proxy } = getCurrentInstance()
const { mk_number_reset_type, sys_normal_disable } = proxy.useDict('mk_number_reset_type', 'sys_normal_disable')

/** 固定为采购管理模块 */
const MODULE = 'pms'

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const total = ref(0)
const title = ref('')
const previewText = ref('')
const isView = ref(false)
const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, module: MODULE, ruleCode: undefined, ruleName: undefined, status: undefined },
  rules: {
    ruleCode: [{ required: true, message: '规则编码不能为空', trigger: 'blur' }],
    ruleName: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
    prefix: [],
    seqLength: [{ required: true, message: '序列号长度不能为空', trigger: 'blur' }],
    resetType: [{ required: true, message: '请选择重置类型', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listNumberRule(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value.module = MODULE
  handleQuery()
}
function handleSelectionChange(selection) { ids.value = selection.map(i => i.ruleId); single.value = selection.length !== 1 }

/** 根据表单数据生成本地预览编号 */
function buildLocalPreview() {
  const fixedPrefix = form.value.prefix || ''
  const connector = form.value.connector || ''
  let str = ''
  if (fixedPrefix) str += fixedPrefix
  let dateStr = ''
  const resetType = form.value.resetType || '0'
  const dateFormat = form.value.dateFormat || ''
  if (resetType === '1') dateStr = formatNow('yyyyMMdd')
  else if (resetType === '2') dateStr = formatNow('yyyyMM')
  else if (resetType === '3') dateStr = formatNow('yyyy')
  else if (dateFormat) dateStr = formatNow(dateFormat)
  if (dateStr) {
    if (fixedPrefix && connector) str += connector
    str += dateStr
  }
  const seqLen = form.value.seqLength || 4
  const startVal = form.value.seqStart || 1
  str += String(startVal).padStart(seqLen, '0')
  previewText.value = str
}

function formatNow(pattern) {
  const now = new Date()
  const map = { yyyy: now.getFullYear(), MM: String(now.getMonth() + 1).padStart(2, '0'), dd: String(now.getDate()).padStart(2, '0') }
  return pattern.replace(/yyyy|MM|dd/g, m => map[m])
}

/** 刷新预览：始终用本地表单数据实时计算 */
function refreshPreview() {
  buildLocalPreview()
}

function reset() {
  form.value = { module: MODULE, ruleCode: undefined, ruleName: undefined, prefix: undefined, dateFormat: 'yyyyMMdd', resetType: '1', seqLength: 4, seqStart: 1, step: 1, connector: '', status: '0', remark: undefined }
  previewText.value = ''
  proxy.resetForm('ruleRef')
}

function handleAdd() { reset(); isView.value = false; open.value = true; title.value = '新增采购管理编号规则' }
function handleUpdate(row) {
  reset()
  isView.value = false
  getNumberRule(row.ruleId || ids.value[0]).then(res => {
    form.value = res.data
    open.value = true
    title.value = `修改${res.data.ruleName}`
    nextTick(() => { buildLocalPreview() })
  })
}
function handleView(row) {
  reset()
  isView.value = true
  getNumberRule(row.ruleId).then(res => {
    form.value = res.data
    open.value = true
    title.value = `查看${res.data.ruleName}`
    nextTick(() => { buildLocalPreview() })
  })
}
function submitForm() {
  proxy.$refs['ruleRef'].validate(valid => {
    if (valid) {
      // 强制设置模块为采购管理
      form.value.module = MODULE
      if (form.value.ruleId != undefined) {
        updateNumberRule(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addNumberRule(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}

function handleExport() { proxy.download('mk/numberRule/export', { ...queryParams.value }, `numberRule_${MODULE}_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset(); isView.value = false }

// 监听重置类型变化，自动设置日期格式
watch(() => form.value.resetType, (newVal) => {
  if (newVal === '0') {
    form.value.dateFormat = ''
  } else if (newVal === '1') {
    form.value.dateFormat = 'yyyyMMdd'
  } else if (newVal === '2') {
    form.value.dateFormat = 'yyyyMM'
  } else if (newVal === '3') {
    form.value.dateFormat = 'yyyy'
  } else if (newVal === '4') {
    form.value.dateFormat = ''
  }
})

// 监听表单变化以实时更新预览
watch(() => form.value, () => { buildLocalPreview() }, { deep: true })

getList()
onActivated(() => { getList() })
</script>

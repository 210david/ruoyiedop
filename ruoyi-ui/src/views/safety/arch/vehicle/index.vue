<template>
  <div class="app-container pjm-list-page">
    <!-- 筛选 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>车牌号</label>
          <div class="control">
            <el-input v-model="queryParams.plateNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>品牌型号</label>
          <div class="control">
            <el-input v-model="queryParams.vehicleModel" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>所属企业</label>
          <div class="control is-select">
            <el-select v-model="queryParams.enterpriseId" placeholder="全部" clearable filterable @change="handleQuery">
              <el-option v-for="e in enterpriseOptions" :key="e.enterpriseId" :label="e.enterpriseName" :value="e.enterpriseId" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced && activeTab !== '2'">
          <label>车辆子类</label>
          <div class="control is-select">
            <el-select v-model="queryParams.specialSubType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in specialSubTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in vehicleStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- 车辆类型页签：全部 / 特种车辆 / 公用车辆 -->
    <div class="surface status-tabs">
      <div class="tabs-track">
        <button class="status-tab" :class="{ 'is-active': activeTab === 'all' }" @click="onTabChange('all')">
          <span class="dot"></span><span>全部</span><span class="count">{{ typeCounts.all || 0 }}</span>
        </button>
        <button class="status-tab tab-special" :class="{ 'is-active': activeTab === '1' }" @click="onTabChange('1')">
          <span class="dot"></span><span>特种车辆</span><span class="count">{{ typeCounts['1'] || 0 }}</span>
        </button>
        <button class="status-tab tab-public" :class="{ 'is-active': activeTab === '2' }" @click="onTabChange('2')">
          <span class="dot"></span><span>公用车辆</span><span class="count">{{ typeCounts['2'] || 0 }}</span>
        </button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:vehicle:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:vehicle:remove']">删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_vehicle_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" :data="vehicleList" border v-loading="loading" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" align="center" resizable />
        <el-table-column label="车牌号" prop="plateNo" key="plateNo" :width="colWidth('plateNo', 110)" align="center" resizable v-if="columns.plateNo.visible">
          <template #default="{row}"><span class="col-mono">{{ row.plateNo }}</span></template>
        </el-table-column>
        <el-table-column label="品牌型号" prop="vehicleModel" key="vehicleModel" :min-width="colWidth('vehicleModel', 140)" show-overflow-tooltip resizable v-if="columns.vehicleModel.visible" />
        <el-table-column v-if="activeTab !== '2' && columns.specialSubType.visible" label="车辆子类" prop="specialSubType" key="specialSubType" :width="colWidth('specialSubType', 110)" align="center" resizable>
          <template #default="{row}">
            <span class="badge blue"><span class="dot"></span>{{ specialSubTypeLabel(row.specialSubType) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="activeTab !== '1' && columns.vehicleUse.visible" label="车辆用途" prop="vehicleUse" key="vehicleUse" :width="colWidth('vehicleUse', 120)" align="center" resizable>
          <template #default="{row}">{{ row.vehicleUse || '-' }}</template>
        </el-table-column>
        <el-table-column label="驾驶员" prop="operatorName" key="operatorName" :width="colWidth('operatorName', 100)" align="center" resizable v-if="columns.operatorName.visible">
          <template #default="{row}">{{ row.operatorName || '-' }}</template>
        </el-table-column>
        <el-table-column label="联系电话" prop="operatorPhone" key="operatorPhone" :width="colWidth('operatorPhone', 130)" align="center" resizable v-if="columns.operatorPhone.visible">
          <template #default="{row}"><span class="col-mono">{{ row.operatorPhone || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="所属企业" prop="enterpriseName" key="enterpriseName" :min-width="colWidth('enterpriseName', 160)" show-overflow-tooltip resizable v-if="columns.enterpriseName.visible" />
        <el-table-column label="检验有效期" prop="inspectExpire" key="inspectExpire" :width="colWidth('inspectExpire', 120)" align="center" resizable v-if="columns.inspectExpire.visible">
          <template #default="{row}">
            <span v-if="row.inspectExpire" class="badge" :class="expireBadgeClass(row.inspectExpire)"><span class="dot"></span>{{ row.inspectExpire }}</span>
            <span v-else class="muted-dash">—</span>
          </template>
        </el-table-column>
        <el-table-column label="保险有效期" prop="insuranceExpire" key="insuranceExpire" :width="colWidth('insuranceExpire', 120)" align="center" resizable v-if="columns.insuranceExpire.visible">
          <template #default="{row}">
            <span v-if="row.insuranceExpire" class="badge" :class="expireBadgeClass(row.insuranceExpire)"><span class="dot"></span>{{ row.insuranceExpire }}</span>
            <span v-else class="muted-dash">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" align="center" resizable v-if="columns.status.visible">
          <template #default="{row}">
            <span class="badge" :class="statusBadge(row.status)"><span class="dot"></span>{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
          <template #default="{row}">
            <div class="action-btn-row">
              <el-button link type="primary" icon="Edit" @click="handleEdit(row)" v-hasPermi="['safety:vehicle:edit']">编辑</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(row)" v-hasPermi="['safety:vehicle:remove']">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 18V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v11a1 1 0 0 0 1 1h2"/><path d="M15 18H9"/><path d="M19 18h2a1 1 0 0 0 1-1v-3.65a1 1 0 0 0-.22-.624l-3.48-4.35A1 1 0 0 0 17.52 8H14"/><circle cx="17" cy="18" r="2"/><circle cx="7" cy="18" r="2"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 18V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v11a1 1 0 0 0 1 1h2"/><path d="M15 18H9"/><path d="M19 18h2a1 1 0 0 0 1-1v-3.65a1 1 0 0 0-.22-.624l-3.48-4.35A1 1 0 0 0 17.52 8H14"/><circle cx="17" cy="18" r="2"/><circle cx="7" cy="18" r="2"/></svg></span>车辆基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="车辆类型">
                    <el-select v-model="form.vehicleType" :disabled="activeTab !== 'all'" style="width: 100%">
                      <el-option v-for="d in vehicleTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="车牌号" prop="plateNo">
                    <el-input v-model="form.plateNo" placeholder="请输入车牌号，如 沪A·12345" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="品牌型号" prop="vehicleModel">
                    <el-input v-model="form.vehicleModel" placeholder="请输入品牌型号，如 丰田叉车FD30" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所属企业" prop="enterpriseId">
                    <el-select v-model="form.enterpriseId" filterable placeholder="请选择所属企业" style="width: 100%" @change="onEnterpriseChange">
                      <el-option v-for="e in enterpriseOptions" :key="e.enterpriseId" :label="e.enterpriseName" :value="e.enterpriseId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 12l2 2 4-4"/><rect x="3" y="4" width="18" height="16" rx="2"/></svg></span>使用与证件信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col v-if="form.vehicleType === '1'" :span="12">
                  <el-form-item label="车辆子类" prop="specialSubType">
                    <el-select v-model="form.specialSubType" placeholder="请选择车辆子类" style="width: 100%">
                      <el-option v-for="d in specialSubTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col v-else :span="12">
                  <el-form-item label="车辆用途">
                    <el-input v-model="form.vehicleUse" placeholder="如 通勤/公务/运输" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="驾驶员/操作员" prop="operatorName">
                    <el-input v-model="form.operatorName" placeholder="请输入驾驶员/操作员姓名" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="联系电话" prop="operatorPhone">
                    <el-input v-model="form.operatorPhone" placeholder="请输入联系电话" />
                  </el-form-item>
                </el-col>
                <el-col v-if="form.vehicleType === '1'" :span="12">
                  <el-form-item label="操作证号" prop="licenseNo">
                    <el-input v-model="form.licenseNo" placeholder="请输入特种作业操作证号" />
                  </el-form-item>
                </el-col>
                <el-col v-else :span="12">
                  <el-form-item label="检验有效期" prop="inspectExpire">
                    <el-date-picker v-model="form.inspectExpire" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col v-if="form.vehicleType === '1'" :span="12">
                  <el-form-item label="操作证有效期" prop="certExpire">
                    <el-date-picker v-model="form.certExpire" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col v-if="form.vehicleType === '1'" :span="12">
                  <el-form-item label="检验有效期" prop="inspectExpire">
                    <el-date-picker v-model="form.inspectExpire" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="保险有效期" prop="insuranceExpire">
                    <el-date-picker v-model="form.insuranceExpire" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="form.status">
                      <el-radio v-for="d in vehicleStatusOptions" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="请输入备注" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="submitLoading" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, getCurrentInstance } from 'vue'
import { listVehicle, getVehicle, addVehicle, updateVehicle, delVehicle } from '@/api/safety/vehicle'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowDown } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { safety_vehicle_type, safety_vehicle_status, safety_special_vehicle_sub, safety_vehicle_enterprise } = proxy.useDict('safety_vehicle_type', 'safety_vehicle_status', 'safety_special_vehicle_sub', 'safety_vehicle_enterprise')
const vehicleTypeOptions = computed(() => (safety_vehicle_type.value || []).map(d => ({ ...d, value: String(d.value) })))
const vehicleStatusOptions = computed(() => (safety_vehicle_status.value || []).map(d => ({ ...d, value: String(d.value) })))
const specialSubTypeOptions = computed(() => (safety_special_vehicle_sub.value || []).map(d => ({ ...d, value: String(d.value) })))
const enterpriseOptions = computed(() => (safety_vehicle_enterprise.value || []).map(d => ({ enterpriseId: String(d.value), enterpriseName: d.label })))

const showSearch = ref(true)
const showAdvanced = ref(false)
const open = ref(false)
const loading = ref(false)
const submitLoading = ref(false)
const vehicleList = ref([])
const total = ref(0)
const ids = ref([])
const multiple = ref(true)
const activeTab = ref('all')
const typeCounts = ref({ all: 0, '1': 0, '2': 0 })
const title = ref('')
const formRef = ref()
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('safety_vehicle_index')
const { collapsedCards, toggleCard } = useDetailCard(['c0', 'c1', 'c2'])

const columns = reactive({
  plateNo: { label: '车牌号', visible: true },
  vehicleModel: { label: '品牌型号', visible: true },
  specialSubType: { label: '车辆子类', visible: true },
  vehicleUse: { label: '车辆用途', visible: true },
  operatorName: { label: '驾驶员', visible: true },
  operatorPhone: { label: '联系电话', visible: true },
  enterpriseName: { label: '所属企业', visible: true },
  inspectExpire: { label: '检验有效期', visible: true },
  insuranceExpire: { label: '保险有效期', visible: true },
  status: { label: '状态', visible: true }
})

const queryParams = reactive({ pageNum: 1, pageSize: 10, vehicleType: null, plateNo: null, vehicleModel: null, enterpriseId: null, status: null, specialSubType: null })

const form = reactive({ vehicleId: null, vehicleType: '1', plateNo: null, vehicleModel: null, specialSubType: null, vehicleUse: null, operatorName: null, operatorPhone: null, licenseNo: null, certExpire: null, inspectExpire: null, insuranceExpire: null, enterpriseId: null, enterpriseName: null, status: '0', remark: null })

const rules = {
  plateNo: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
  operatorName: [{ required: true, message: '请输入驾驶员/操作员', trigger: 'blur' }]
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.plateNo) count++
  if (queryParams.vehicleModel) count++
  if (queryParams.enterpriseId) count++
  if (queryParams.status) count++
  if (queryParams.specialSubType) count++
  return count
})

const specialSubTypeLabel = (v) => specialSubTypeOptions.value.find(x => x.value === String(v))?.label
const statusLabel = (v) => vehicleStatusOptions.value.find(x => x.value === String(v))?.label || '未知'
const statusBadge = (v) => ({ '0': 'green', '1': 'gray', '2': 'red' }[String(v)] || 'gray')

/** 到期标签颜色：30天内琥珀，过期红，否则绿 */
function expireBadgeClass(dateStr) {
  if (!dateStr) return 'gray'
  const now = new Date()
  const d = new Date(dateStr)
  const diff = Math.floor((d - now) / 86400000)
  if (diff < 0) return 'red'
  if (diff <= 30) return 'amber'
  return 'green'
}

function getList() {
  loading.value = true
  listVehicle(queryParams).then(res => { vehicleList.value = res.rows; total.value = res.total }).finally(() => loading.value = false)
}
function handleQuery() { showAdvanced.value = false; queryParams.pageNum = 1; getList() }
function resetQuery() {
  queryParams.plateNo = null; queryParams.vehicleModel = null; queryParams.enterpriseId = null
  queryParams.status = null; queryParams.specialSubType = null
  handleQuery()
}
function handleSelectionChange(sels) { ids.value = sels.map(s => s.vehicleId); multiple.value = !sels.length }

function onTabChange(tab) {
  activeTab.value = tab
  queryParams.vehicleType = tab === 'all' ? null : tab
  queryParams.specialSubType = null
  queryParams.pageNum = 1
  getList()
}

/** 统计各车辆类型数量（页签角标） */
function loadTypeCounts() {
  listVehicle({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total || 0, '1': 0, '2': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.vehicleType] !== undefined) counts[r.vehicleType]++ })
    typeCounts.value = counts
  }).catch(() => {})
}

function onEnterpriseChange(val) {
  const e = enterpriseOptions.value.find(x => x.enterpriseId === val)
  form.enterpriseName = e?.enterpriseName || null
}

function handleAdd() {
  const defaultType = activeTab.value === 'all' ? '1' : activeTab.value
  Object.assign(form, { vehicleId: null, vehicleType: defaultType, plateNo: null, vehicleModel: null, specialSubType: null, vehicleUse: null, operatorName: null, operatorPhone: null, licenseNo: null, certExpire: null, inspectExpire: null, insuranceExpire: null, enterpriseId: null, enterpriseName: null, status: '0', remark: null })
  collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false
  title.value = activeTab.value === '1' ? '新增特种车辆' : (activeTab.value === '2' ? '新增公用车辆' : '新增车辆')
  open.value = true
}
function handleEdit(row) {
  getVehicle(row.vehicleId).then(res => {
    Object.assign(form, res.data)
    form.enterpriseId = form.enterpriseId != null ? String(form.enterpriseId) : null
    collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false
    title.value = '编辑' + (form.vehicleType === '1' ? '特种车辆' : '公用车辆')
    open.value = true
  })
}
function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    const op = form.vehicleId ? updateVehicle(form) : addVehicle(form)
    op.then(() => {
      proxy.$modal.msgSuccess('保存成功')
      open.value = false
      getList()
      loadTypeCounts()
    }).finally(() => submitLoading.value = false)
  })
}
function handleDelete(row) {
  const idsToDel = row?.vehicleId ? [row.vehicleId] : ids.value
  if (!idsToDel.length) return
  proxy.$modal.confirm('确认删除选中的车辆？').then(() => {
    delVehicle(idsToDel).then(() => { proxy.$modal.msgSuccess('删除成功'); getList(); loadTypeCounts() })
  }).catch(() => {})
}

onMounted(() => {
  getList()
  loadTypeCounts()
})
</script>

<style scoped lang="scss">
.tab-label { display: inline-flex; align-items: center; gap: 6px; }
.tab-label :deep(.el-icon) { font-size: 15px; }
.tab-hint { font-size: 12px; color: #909399; margin-left: 8px; }
.muted-dash { color: #c0c4cc; }

/* 车辆类型页签（与设备台账等模块一致） */
.status-tabs { margin-bottom: 12px; padding: 12px 20px 0; }
.tabs-track { display: flex; align-items: center; gap: 4px; flex-wrap: wrap; }
.status-tab { display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px; border-radius: var(--r-sm); font-size: 14px; color: var(--ink-500); cursor: pointer; user-select: none; transition: all .15s var(--ease-out); white-space: nowrap; border: 1px solid transparent; background: transparent; }
.status-tab .dot { width: 6px; height: 6px; border-radius: 50%; background: var(--ink-300); }
.status-tab .count { font-size: 12px; font-weight: 600; padding: 1px 6px; border-radius: 999px; background: var(--ink-100); color: var(--ink-500); min-width: 18px; text-align: center; line-height: 1.4; font-feature-settings: "tnum" 1; }
.status-tab:hover { background: var(--ink-50); color: var(--ink-700); }
.status-tab.is-active { background: var(--brand-50); color: var(--brand-700); font-weight: 600; border-color: var(--brand-200); }
.status-tab.is-active .count { background: var(--brand-600); color: #fff; }
.status-tab.is-active .dot { background: var(--brand-500); }
.status-tab.tab-special .dot { background: var(--amber-500); }
.status-tab.tab-public .dot { background: var(--blue-500); }
</style>

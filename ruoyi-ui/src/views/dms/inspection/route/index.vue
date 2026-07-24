<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="路线名称" prop="routeName">
        <el-input v-model="queryParams.routeName" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="周期类型" prop="cycleType">
        <el-select v-model="queryParams.cycleType" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="d in dms_inspection_cycle" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:inspection:route:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:inspection:route:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:inspection:route:remove']">删除</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="路线名称" prop="routeName" min-width="140" show-overflow-tooltip />
      <el-table-column label="路线编码" prop="routeCode" min-width="160" show-overflow-tooltip />
      <el-table-column label="部门" prop="deptName" :width="colWidth('deptName', 120)" resizable />
      <el-table-column label="周期" width="100" align="center">
        <template #default="scope"><dict-tag :options="dms_inspection_cycle" :value="scope.row.cycleType" /> {{ scope.row.cycleValue }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="status" :width="colWidth('status', 80)" resizable align="center">
        <template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:inspection:route:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:inspection:route:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog v-model="open" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="routeRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
        <el-row>
          <el-col :span="12"><el-form-item label="路线名称" prop="routeName"><el-input v-model="form.routeName" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="路线编码" prop="routeCode"><el-input v-model="form.routeCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="部门" prop="deptId">
            <el-tree-select v-model="form.deptId" :data="deptOptions" :props="{ value: 'id', label: 'label', children: 'children' }" value-key="id" placeholder="请选择归属部门" clearable check-strictly style="width: 100%" @change="onDeptChange" />
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/></svg></span>巡检配置</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
        <el-row>
          <el-col :span="12"><el-form-item label="周期类型" prop="cycleType">
            <el-select v-model="form.cycleType" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dms_inspection_cycle" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="周期值" prop="cycleValue"><el-input-number v-model="form.cycleValue" :min="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="巡检设备" prop="equipmentIdList">
          <el-select v-model="form.equipmentIdList" multiple filterable clearable placeholder="请选择设备（可多选，按选择顺序为巡检顺序）" style="width: 100%" @change="onEquipmentChange">
            <el-option v-for="e in equipmentOptions" :key="e.equipmentId" :label="e.equipmentCode + ' - ' + e.equipmentName" :value="e.equipmentId" />
          </el-select>
        </el-form-item>

        <!-- 通用点检项 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>通用点检项（所有设备共用）</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
        <el-table :data="form.commonItems" border style="width: 100%" size="small" @header-dragend="onHeaderDragEnd">
          <el-table-column label="序号" width="60" align="center">
            <template #default="scope">{{ scope.$index + 1 }}</template>
          </el-table-column>
          <el-table-column label="检查项" min-width="200">
            <template #default="scope"><el-input v-model="scope.row.item" placeholder="如：设备周围卫生" size="small" /></template>
          </el-table-column>
          <el-table-column label="类型" width="120" align="center">
            <template #default="scope">
              <el-select v-model="scope.row.type" size="small" placeholder="类型">
                <el-option label="打勾" value="check" />
                <el-option label="数值" value="number" />
                <el-option label="文本" value="text" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="单位" width="80" align="center">
            <template #default="scope"><el-input v-model="scope.row.unit" placeholder="如℃/MPa" size="small" :disabled="scope.row.type !== 'number'" /></template>
          </el-table-column>
          <el-table-column label="异常必填说明" width="110" align="center">
            <template #default="scope"><el-switch v-model="scope.row.abnormalRequired" /></template>
          </el-table-column>
          <el-table-column label="操作" width="70" align="center">
            <template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="removeCommonItem(scope.$index)">删除</el-button></template>
          </el-table-column>
        </el-table>
        <el-button type="primary" plain icon="Plus" size="small" style="margin-top: 8px" @click="addCommonItem">添加通用项</el-button>

        <!-- 设备明细分组 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>设备点检项明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
        <div v-if="form.deviceItems.length === 0" style="text-align: center; color: #999; padding: 16px 0">请先选择巡检设备，然后为每台设备配置专属点检项</div>
        <el-collapse v-model="activeDeviceTabs" style="margin-top: 4px">
          <el-collapse-item v-for="(dev, di) in form.deviceItems" :key="dev.equipmentId" :name="dev.equipmentId">
            <template #title>
              <span style="font-weight: 600; font-size: 14px">{{ dev.equipmentName }}</span>
              <el-tag size="small" style="margin-left: 8px">{{ dev.items.length }}项</el-tag>
            </template>
            <el-table :data="dev.items" border style="width: 100%" size="small" @header-dragend="onHeaderDragEnd">
              <el-table-column label="序号" width="60" align="center">
                <template #default="scope">{{ scope.$index + 1 }}</template>
              </el-table-column>
              <el-table-column label="检查项" min-width="200">
                <template #default="scope"><el-input v-model="scope.row.item" :placeholder="'如：检查' + dev.equipmentName + '温度'" size="small" /></template>
              </el-table-column>
              <el-table-column label="类型" width="120" align="center">
                <template #default="scope">
                  <el-select v-model="scope.row.type" size="small" placeholder="类型">
                    <el-option label="打勾" value="check" />
                    <el-option label="数值" value="number" />
                    <el-option label="文本" value="text" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="单位" width="80" align="center">
                <template #default="scope"><el-input v-model="scope.row.unit" placeholder="如℃" size="small" :disabled="scope.row.type !== 'number'" /></template>
              </el-table-column>
              <el-table-column label="异常必填说明" width="110" align="center">
                <template #default="scope"><el-switch v-model="scope.row.abnormalRequired" /></template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="removeDeviceItem(di, scope.$index)">删除</el-button></template>
              </el-table-column>
            </el-table>
            <el-button type="primary" plain icon="Plus" size="small" style="margin-top: 8px" @click="addDeviceItem(di)">添加点检项</el-button>
          </el-collapse-item>
        </el-collapse>

        <el-form-item label="备注" prop="remark" style="margin-top: 12px"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item>
                </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsInspectionRoute">
import { listRoute, getRoute, addRoute, updateRoute, delRoute } from '@/api/dms/inspection'
import { listEquipment } from '@/api/dms/equipment'
import { deptTreeSelect } from '@/api/system/user'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c3","c2","c1","c0"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_inspection_route_index')
const { dms_inspection_cycle } = proxy.useDict('dms_inspection_cycle')

const list = ref([])
const equipmentOptions = ref([])
const deptOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const activeDeviceTabs = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, routeName: undefined, cycleType: undefined },
  rules: {
    routeName: [{ required: true, message: '路线名称不能为空', trigger: 'blur' }],
    cycleType: [{ required: true, message: '周期类型不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listRoute(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function getEquipmentOptions() { listEquipment({ pageNum: 1, pageSize: 9999 }).then(res => { equipmentOptions.value = res.rows }) }
function getDeptOptions() { deptTreeSelect().then(res => { deptOptions.value = res.data }) }
function onDeptChange(deptId) {
  const findDeptName = (nodes, id) => {
    for (const node of nodes) {
      if (node.id === id) return node.label
      if (node.children) { const r = findDeptName(node.children, id); if (r) return r }
    }
    return ''
  }
  form.value.deptName = findDeptName(deptOptions.value, deptId)
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.routeId); single.value = selection.length !== 1; multiple.value = !selection.length }

function reset() {
  form.value = {
    routeName: undefined, routeCode: undefined, deptId: undefined, deptName: undefined,
    cycleType: '0', cycleValue: 1,
    equipmentIds: undefined, equipmentIdList: [],
    inspectionItems: undefined,
    commonItems: [], deviceItems: [],
    status: '0', remark: undefined
  }
  activeDeviceTabs.value = []
  proxy.resetForm('routeRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增巡检路线' }

function handleUpdate(row) {
  reset()
  getRoute(row.routeId || ids.value[0]).then(res => {
    form.value = res.data
    if (form.value.equipmentIds) { try { form.value.equipmentIdList = JSON.parse(form.value.equipmentIds) } catch (e) { form.value.equipmentIdList = [] } }
    if (form.value.inspectionItems) {
      try {
        const parsed = JSON.parse(form.value.inspectionItems)
        // 兼容旧格式：如果是扁平数组，当作通用项处理
        if (Array.isArray(parsed)) {
          form.value.commonItems = parsed
          form.value.deviceItems = []
        } else {
          form.value.commonItems = parsed.common || []
          form.value.deviceItems = parsed.devices || []
        }
      } catch (e) { form.value.commonItems = []; form.value.deviceItems = [] }
    }
    // 确保选中的设备都有对应的设备明细组
    syncDeviceItems()
    open.value = true; title.value = '修改巡检路线'
  })
}

// 设备选择变化时，同步设备明细分组
function onEquipmentChange(selectedIds) {
  syncDeviceItems()
}
function syncDeviceItems() {
  const selectedIds = form.value.equipmentIdList || []
  // 移除已取消选择的设备分组
  form.value.deviceItems = form.value.deviceItems.filter(d => selectedIds.includes(d.equipmentId))
  // 为新选择的设备创建空分组
  selectedIds.forEach(eid => {
    if (!form.value.deviceItems.find(d => d.equipmentId === eid)) {
      const eq = equipmentOptions.value.find(e => e.equipmentId === eid)
      form.value.deviceItems.push({
        equipmentId: eid,
        equipmentName: eq ? eq.equipmentCode + ' - ' + eq.equipmentName : '设备' + eid,
        items: []
      })
    }
  })
  // 默认展开所有设备
  activeDeviceTabs.value = form.value.deviceItems.map(d => d.equipmentId)
}

// 通用项操作
function addCommonItem() { form.value.commonItems.push({ item: '', type: 'check', unit: '', abnormalRequired: false }) }
function removeCommonItem(index) { form.value.commonItems.splice(index, 1) }

// 设备明细项操作
function addDeviceItem(di) { form.value.deviceItems[di].items.push({ item: '', type: 'check', unit: '', abnormalRequired: false }) }
function removeDeviceItem(di, ii) { form.value.deviceItems[di].items.splice(ii, 1) }

function submitForm() {
  proxy.$refs['routeRef'].validate(valid => {
    if (valid) {
      form.value.equipmentIds = form.value.equipmentIdList && form.value.equipmentIdList.length > 0 ? JSON.stringify(form.value.equipmentIdList) : undefined
      // 序列化为嵌套JSON结构
      const inspectionData = {
        common: form.value.commonItems || [],
        devices: form.value.deviceItems || []
      }
      const hasItems = inspectionData.common.length > 0 || inspectionData.devices.some(d => d.items.length > 0)
      form.value.inspectionItems = hasItems ? JSON.stringify(inspectionData) : undefined
      if (form.value.routeId != undefined) { updateRoute(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addRoute(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const routeIds = row.routeId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delRoute(routeIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
getEquipmentOptions()
getDeptOptions()
getList()
</script>

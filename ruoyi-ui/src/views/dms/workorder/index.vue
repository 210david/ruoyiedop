<template>
  <div class="app-container dms-workorder-page">
    <!-- ===== Filter Card ===== -->
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
          <label>工单号</label>
          <div class="control">
            <el-input v-model="queryParams.orderNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>设备编号</label>
          <div class="control">
            <el-input v-model="queryParams.equipmentCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>设备名称</label>
          <div class="control">
            <el-input v-model="queryParams.equipmentName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工单状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.orderStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in dms_order_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>工单类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.orderType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in dms_order_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>优先级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.priority" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in dms_priority" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>报修人</label>
          <div class="control">
            <el-input v-model="queryParams.reporterName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>维修人</label>
          <div class="control">
            <el-input v-model="queryParams.assigneeName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>报修时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Status Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:workorder:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:workorder:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:workorder:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['dms:workorder:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_workorder_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="工单号" prop="orderNo" key="orderNo" :width="colWidth('orderNo', 160)" resizable v-if="columns.orderNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.orderNo }}</span></template>
          </el-table-column>
          <el-table-column label="设备名称" prop="equipmentName" key="equipmentName" :width="colWidth('equipmentName', 160)" resizable show-overflow-tooltip v-if="columns.equipmentName.visible" />
          <el-table-column label="工单类型" prop="orderType" key="orderType" :width="colWidth('orderType', 100)" resizable align="center" v-if="columns.orderType.visible">
            <template #default="scope"><span class="badge violet">{{ orderTypeLabel(scope.row.orderType) }}</span></template>
          </el-table-column>
          <el-table-column label="故障描述" prop="faultDescription" key="faultDescription" :width="colWidth('faultDescription', 220)" resizable show-overflow-tooltip v-if="columns.faultDescription.visible">
            <template #default="scope">
              <span class="text-truncate">{{ cleanFaultDescription(scope.row.faultDescription) || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="报修人" prop="reporterName" key="reporterName" :width="colWidth('reporterName', 100)" resizable v-if="columns.reporterName.visible" />
          <el-table-column label="优先级" prop="priority" key="priority" :width="colWidth('priority', 100)" resizable align="center" v-if="columns.priority.visible">
            <template #default="scope"><span class="badge amber">{{ priorityLabel(scope.row.priority) }}</span></template>
          </el-table-column>
          <el-table-column label="维修人" prop="assigneeName" key="assigneeName" :width="colWidth('assigneeName', 100)" resizable v-if="columns.assigneeName.visible" />
          <el-table-column label="工单状态" prop="orderStatus" key="orderStatus" :width="colWidth('orderStatus', 120)" resizable align="center" v-if="columns.orderStatus.visible">
            <template #default="scope">
              <span v-if="scope.row.slaTimeoutStatus === '2' && !isTerminalStatus(scope.row.orderStatus)" class="badge red"><span class="dot"></span>处理超时</span>
              <span class="badge" :class="orderStatusBadgeClass(scope.row.orderStatus)"><span class="dot"></span>{{ orderStatusLabel(scope.row.orderStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="报修时间" prop="reportTime" key="reportTime" :width="colWidth('reportTime', 160)" resizable align="center" v-if="columns.reportTime.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
        <template #default="scope">
          <div class="action-btn-row">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.orderStatus === '0'" v-hasPermi="['dms:workorder:edit']">修改</el-button>
            <!-- 状态流转按钮 -->
            <el-button v-if="scope.row.orderStatus === '0'" v-hasPermi="['dms:workorder:dispatch']" link type="primary" icon="Position" @click="handleDispatch(scope.row)">派工</el-button>
            <el-button v-if="scope.row.orderStatus === '1'" v-hasPermi="['dms:workorder:accept']" link type="success" icon="Select" @click="handleAccept(scope.row)">接单</el-button>
            <el-button v-if="scope.row.orderStatus === '1'" v-hasPermi="['dms:workorder:reassign']" link type="warning" icon="Switch" @click="handleReassign(scope.row)">改派</el-button>
            <el-button v-if="scope.row.orderStatus === '2' || scope.row.orderStatus === '7'" v-hasPermi="['dms:workorder:process']" link type="primary" icon="Tools" @click="handleProcess(scope.row)">处理</el-button>
            <el-button v-if="scope.row.orderStatus === '3'" v-hasPermi="['dms:workorder:complete']" link type="success" icon="CircleCheck" @click="handleComplete(scope.row)">完工</el-button>
            <el-button v-if="scope.row.orderStatus === '4'" v-hasPermi="['dms:workorder:verify']" link type="success" icon="Checked" @click="handleVerify(scope.row)">验收</el-button>
            <el-button v-if="scope.row.orderStatus === '4'" v-hasPermi="['dms:workorder:reject']" link type="danger" icon="CircleClose" @click="handleReject(scope.row)">驳回</el-button>
            <el-button v-if="scope.row.orderStatus === '0' || scope.row.orderStatus === '1'" v-hasPermi="['dms:workorder:cancel']" link type="info" icon="RefreshLeft" @click="handleCancel(scope.row)">撤销</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:workorder:remove']">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改工单弹窗 -->
    <el-dialog v-model="open" width="984px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.orderNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">工单号：{{ form.orderNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="workorderRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
        <!-- AI智能报修 -->
        <el-alert v-if="aiResult" type="success" :closable="true" @close="aiResult = null" style="margin-bottom: 12px">
          <template #title>AI识别结果</template>
          <div style="font-size: 13px; line-height: 1.8">
            故障描述：{{ aiResult.faultDescription }}<br/>
            优先级：{{ aiResult.priority }}<br/>
            发生时间：{{ aiResult.reportTime || '未识别' }}<br/>
            <span style="color: #999">{{ aiResult.tip }}</span>
          </div>
        </el-alert>
        <el-form-item v-if="form.orderId == undefined">
          <el-button type="primary" plain icon="MagicStick" @click="aiDialog = true" :loading="aiLoading">智能报修</el-button>
          <span style="color: #999; margin-left: 10px">输入自然语言描述，AI自动识别填充</span>
        </el-form-item>

        <!-- 分组一：工单信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('ed_order')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>工单信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ed_order }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.ed_order">
            <el-row>
              <el-col :span="12"><el-form-item label="工单号" prop="orderNo"><el-input v-model="form.orderNo" placeholder="自动生成" disabled /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="工单类型" prop="orderType">
                <el-select v-model="form.orderType" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in dms_order_type" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="优先级" prop="priority">
                <el-select v-model="form.priority" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in dms_priority" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12" v-if="!isAdd"><el-form-item label="工单状态" prop="orderStatus">
                <el-select v-model="form.orderStatus" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in dms_order_status" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="报修人" prop="reporterId"><el-input v-model="form.reporterName" readonly placeholder="请选择报修人" style="width: 100%" @click="openUserPicker('form', 'reporterId', 'reporterName')"><template #append><el-button icon="Search" @click="openUserPicker('form', 'reporterId', 'reporterName')" /></template><template #suffix><el-icon v-if="form.reporterName" class="clear-icon" @click.stop="form.reporterId = undefined; form.reporterName = undefined"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="报修时间" prop="reportTime"><el-date-picker v-model="form.reportTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>

        <!-- 分组二：设备信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('ed_equipment')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg></span>设备信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ed_equipment }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.ed_equipment">
            <el-row>
              <el-col :span="12"><el-form-item label="设备编号" prop="equipmentId"><el-input v-model="form.equipmentCode" readonly placeholder="请选择设备" style="width: 100%" @click="openEquipmentPicker"><template #append><el-button icon="Search" @click="openEquipmentPicker" /></template><template #suffix><el-icon v-if="form.equipmentCode" class="clear-icon" @click.stop="clearEquipment"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="设备名称"><el-input v-model="form.equipmentName" placeholder="选择设备后自动带出" disabled /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="故障描述" prop="faultDescription"><el-input v-model="form.faultDescription" type="textarea" :rows="3" placeholder="请输入故障现象/任务描述" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>

        <!-- 分组三：派工信息（仅修改时显示） -->
        <section class="rd-card" v-if="!isAdd">
          <div class="rd-card-header" @click="toggleCard('ed_dispatch')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>派工信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ed_dispatch }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.ed_dispatch">
            <el-row>
              <el-col :span="12"><el-form-item label="维修人" prop="assigneeId"><el-input v-model="form.assigneeName" readonly placeholder="请选择维修人" style="width: 100%" @click="openUserPicker('form', 'assigneeId', 'assigneeName')"><template #append><el-button icon="Search" @click="openUserPicker('form', 'assigneeId', 'assigneeName')" /></template><template #suffix><el-icon v-if="form.assigneeName" class="clear-icon" @click.stop="form.assigneeId = undefined; form.assigneeName = undefined"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="派工人" prop="assignerId"><el-input v-model="form.assignerName" readonly placeholder="请选择派工人" style="width: 100%" @click="openUserPicker('form', 'assignerId', 'assignerName')"><template #append><el-button icon="Search" @click="openUserPicker('form', 'assignerId', 'assignerName')" /></template><template #suffix><el-icon v-if="form.assignerName" class="clear-icon" @click.stop="form.assignerId = undefined; form.assignerName = undefined"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="到达时间" prop="arriveTime"><el-date-picker v-model="form.arriveTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>

        <!-- 分组四：维修处理（仅修改时显示） -->
        <section class="rd-card" v-if="!isAdd">
          <div class="rd-card-header" @click="toggleCard('ed_repair')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></span>维修处理</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ed_repair }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.ed_repair">
            <el-row>
              <el-col :span="24"><el-form-item label="故障原因" prop="faultCause"><el-input v-model="form.faultCause" type="textarea" :rows="2" placeholder="请输入故障原因" /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="维修措施" prop="repairMeasure"><el-input v-model="form.repairMeasure" type="textarea" :rows="2" placeholder="请输入维修措施" /></el-form-item></el-col>
              <el-col :span="24">
                <el-form-item label="更换备件" prop="sparePartsUsed">
                  <div style="width: 100%">
                    <el-table :data="formSparePartList" border size="small" style="margin-bottom: 8px" @header-dragend="onHeaderDragEnd">
                      <el-table-column label="备件名称" min-width="180">
                        <template #default="{ row }">
                          <el-select v-model="row.partName" filterable clearable placeholder="请选择备件" style="width: 100%">
                            <el-option v-for="p in getAvailableSpareParts(formSparePartList, row.partName)" :key="p.partId" :label="p.partName + (p.specModel ? ' (' + p.specModel + ')' : '')" :value="p.partName" />
                          </el-select>
                        </template>
                      </el-table-column>
                      <el-table-column label="库存" width="80" align="center">
                        <template #default="{ row }">{{ getSparePartStock(row.partName) != null ? getSparePartStock(row.partName) : '-' }}</template>
                      </el-table-column>
                      <el-table-column label="数量" width="120" align="center">
                        <template #default="{ row }">
                          <el-input-number v-model="row.quantity" :min="1" :max="getSparePartStock(row.partName) != null ? getSparePartStock(row.partName) : undefined" :precision="0" size="small" controls-position="right" style="width: 100px" />
                        </template>
                      </el-table-column>
                      <el-table-column label="单位" width="70" align="center">
                        <template #default="{ row }">{{ getSparePartUnit(row.partName) }}</template>
                      </el-table-column>
                      <el-table-column label="操作" width="70" align="center">
                        <template #default="{ $index }">
                          <el-button link type="danger" icon="Delete" @click="formSparePartList.splice($index, 1)"></el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                    <el-button type="primary" plain icon="Plus" size="small" @click="formSparePartList.push({ partName: undefined, quantity: 1 })">添加备件</el-button>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12"><el-form-item label="维修费用" prop="repairCost"><el-input-number v-model="form.repairCost" :min="0" :precision="2" :step="100" controls-position="right" style="width: 100%" /><span style="margin-left: 8px; color: #999">元</span></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="停机时长" prop="downtimeDuration"><el-input-number v-model="form.downtimeDuration" :min="0" :precision="2" controls-position="right" style="width: 100%" /><span style="margin-left: 8px; color: #999">小时</span></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="完工时间" prop="completeTime"><el-date-picker v-model="form.completeTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="完工说明" prop="completeRemark"><el-input v-model="form.completeRemark" type="textarea" :rows="2" placeholder="请输入完工说明" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>

        <!-- 分组五：验收信息（仅修改时显示） -->
        <section class="rd-card" v-if="!isAdd">
          <div class="rd-card-header" @click="toggleCard('ed_verify')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>验收信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ed_verify }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.ed_verify">
            <el-row>
              <el-col :span="12"><el-form-item label="验收人" prop="verifierId"><el-input v-model="form.verifierName" readonly placeholder="请选择验收人" style="width: 100%" @click="openUserPicker('form', 'verifierId', 'verifierName')"><template #append><el-button icon="Search" @click="openUserPicker('form', 'verifierId', 'verifierName')" /></template><template #suffix><el-icon v-if="form.verifierName" class="clear-icon" @click.stop="form.verifierId = undefined; form.verifierName = undefined"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="评价" prop="rating">
                <el-rate v-model="form.rating" :max="5" />
              </el-form-item></el-col>
              <el-col :span="24"><el-form-item label="验收意见" prop="verifyOpinion"><el-input v-model="form.verifyOpinion" type="textarea" :rows="2" placeholder="请输入验收意见" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>

        <!-- 分组六：其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('ed_other')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ed_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.ed_other">
            <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入内容" /></el-form-item>
          </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 派工/改派弹窗 -->
    <el-dialog v-model="dispatchOpen" width="780px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">{{ isReassign ? '工单改派' : '工单派工' }}</span>
          <div class="rd-detail-header-sub" v-if="dispatchForm.orderNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">工单号：{{ dispatchForm.orderNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 工单信息卡片 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dc_wo')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>工单信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dc_wo }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dc_wo" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ dispatchForm.orderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">设备编号</span><div class="rd-value">{{ dispatchForm.equipmentCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">设备名称</span><div class="rd-value">{{ dispatchForm.equipmentName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工单类型</span><div class="rd-value"><dict-tag :options="dms_order_type" :value="dispatchForm.orderType" /></div></div>
              <div class="rd-item"><span class="rd-label">优先级</span><div class="rd-value"><dict-tag :options="dms_priority" :value="dispatchForm.priority" /></div></div>
              <div class="rd-item"><span class="rd-label">报修人</span><div class="rd-value">{{ dispatchForm.reporterName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">报修时间</span><div class="rd-value">{{ dispatchForm.reportTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">故障描述</span><div class="rd-value desc-pre">{{ cleanFaultDescription(dispatchForm.faultDescription) || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 派工信息卡片 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dc_form')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>派工信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dc_form }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dc_form">
            <el-form ref="dispatchRef" :model="dispatchForm" label-width="100px">
              <el-form-item label-width="0">
                <el-button type="primary" plain icon="MagicStick" size="small" @click="loadRecommend" :loading="recommendLoading">智能推荐维修人</el-button>
              </el-form-item>
              <div v-if="recommendations.length > 0" style="margin-bottom: 12px">
                <div v-for="(rec, i) in recommendations" :key="i" style="padding: 8px 0; border-bottom: 1px solid #eee; cursor: pointer" @click="selectRecommendation(rec)">
                  <el-tag type="success" size="small">推荐{{ i + 1 }}</el-tag>
                  <span style="margin-left: 8px">{{ rec.assigneeName }}</span>
                  <span style="color: #999; margin-left: 8px">待处理：{{ rec.currentWorkload }}单</span>
                  <span v-if="rec.score" style="color: #409eff; margin-left: 8px">评分：{{ rec.score }}</span>
                  <div v-if="rec.reason" style="color: #67c23a; font-size: 12px; margin-top: 2px">{{ rec.reason }}</div>
                </div>
              </div>
              <el-form-item label="维修人" required>
                <el-input v-model="dispatchForm.assigneeName" readonly placeholder="请选择维修人" style="width: 100%" @click="openUserPicker('dispatchForm', 'assigneeId', 'assigneeName')"><template #append><el-button icon="Search" @click="openUserPicker('dispatchForm', 'assigneeId', 'assigneeName')" /></template><template #suffix><el-icon v-if="dispatchForm.assigneeName" class="clear-icon" @click.stop="dispatchForm.assigneeId = undefined; dispatchForm.assigneeName = undefined"><CircleClose /></el-icon></template></el-input>
              </el-form-item>
              <el-form-item label="派工人">
                <el-input v-model="dispatchForm.assignerName" readonly placeholder="请选择派工人" style="width: 100%" @click="openUserPicker('dispatchForm', 'assignerId', 'assignerName')"><template #append><el-button icon="Search" @click="openUserPicker('dispatchForm', 'assignerId', 'assignerName')" /></template><template #suffix><el-icon v-if="dispatchForm.assignerName" class="clear-icon" @click.stop="dispatchForm.assignerId = undefined; dispatchForm.assignerName = undefined"><CircleClose /></el-icon></template></el-input>
              </el-form-item>
              <el-form-item label="派工时间">
                <el-date-picker v-model="dispatchForm.assignTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
              </el-form-item>
            </el-form>
            <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px;">
              <el-icon><InfoFilled /></el-icon>
              派工后系统将根据优先级自动计算SLA响应截止时间，维修人需在截止时间前接单
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="dispatchOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitDispatch">{{ isReassign ? '确认改派' : '确认派工' }}</el-button>
      </template>
    </el-dialog>

    <!-- AI智能报修弹窗 -->
    <el-dialog v-model="aiDialog" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></div>
          <span class="rd-detail-header-title">智能报修助手</span>
        </div>
      </template>
      <el-form label-width="80px">
        <el-form-item label="描述">
          <el-input v-model="aiInput" type="textarea" :rows="4" placeholder="请用自然语言描述故障，如：3号车床主轴有异响，震动比较大，今天上午开始的，比较急" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="callAI" :loading="aiLoading">智能识别</el-button>
        <el-button @click="aiDialog = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 完工弹窗 -->
    <el-dialog v-model="completeOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">工单完工</span>
        </div>
      </template>
      <div class="rd-page">
      <el-form ref="completeRef" :model="completeForm" label-width="100px">
        <!-- 分组一：工单信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('cp_order')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>工单信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cp_order }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.cp_order" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ completeForm.orderNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">工单类型</span><div class="rd-value"><dict-tag :options="dms_order_type" :value="completeForm.orderType" /></div></div>
              <div class="rd-item"><span class="rd-label">设备编号</span><div class="rd-value">{{ completeForm.equipmentCode || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">设备名称</span><div class="rd-value">{{ completeForm.equipmentName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">报修人</span><div class="rd-value">{{ completeForm.reporterName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">报修时间</span><div class="rd-value">{{ completeForm.reportTime || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">故障描述</span><div class="rd-value desc-pre">{{ cleanFaultDescription(completeForm.faultDescription) || '—' }}</div></div>
            </div>
            <div v-if="completeTaskList.length > 0" class="task-checklist-block">
              <div class="task-table-toolbar">
                <span class="task-table-title">任务清单</span>
                <span class="task-table-summary">共 <b>{{ completeTaskList.length }}</b> 项任务，已填写完成情况 <b :class="{ 'task-summary-done': filledTaskCount === completeTaskList.length }">{{ filledTaskCount }}/{{ completeTaskList.length }}</b></span>
              </div>
              <el-table :data="completeTaskList" border size="small" @header-dragend="onHeaderDragEnd">
                <el-table-column label="序号" type="index" width="60" align="center" />
                <el-table-column label="任务" prop="text" min-width="220" show-overflow-tooltip />
                <el-table-column label="完成情况" width="150" align="center">
                  <template #default="{ row }">
                    <el-select v-model="row.result" placeholder="请选择" size="small" style="width: 100%">
                      <el-option label="已完成" value="done" />
                      <el-option label="未完成" value="undone" />
                      <el-option label="不适用" value="na" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="备注" prop="remark" min-width="260">
                  <template #default="{ row }">
                    <el-input v-model="row.remark" size="small" :placeholder="row.result === 'undone' ? '未完成原因（必填）' : '备注（选填）'" />
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </section>

        <!-- 分组二：维修处理 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('cp_repair')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></span>维修处理</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cp_repair }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.cp_repair">
            <el-row>
              <el-col :span="24"><el-form-item label="故障原因" required><el-input v-model="completeForm.faultCause" type="textarea" :rows="2" placeholder="请输入故障原因" /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="维修措施" required><el-input v-model="completeForm.repairMeasure" type="textarea" :rows="2" placeholder="请输入维修措施" /></el-form-item></el-col>
              <el-col :span="24">
                <el-form-item label="更换备件">
                  <div style="width: 100%">
                    <el-table :data="completeSparePartList" border size="small" style="margin-bottom: 8px" @header-dragend="onHeaderDragEnd">
                      <el-table-column label="备件名称" min-width="180">
                        <template #default="{ row }">
                          <el-select v-model="row.partName" filterable clearable placeholder="请选择备件" style="width: 100%">
                            <el-option v-for="p in getAvailableSpareParts(completeSparePartList, row.partName)" :key="p.partId" :label="p.partName + (p.specModel ? ' (' + p.specModel + ')' : '')" :value="p.partName" />
                          </el-select>
                        </template>
                      </el-table-column>
                      <el-table-column label="库存" width="80" align="center">
                        <template #default="{ row }">{{ getSparePartStock(row.partName) != null ? getSparePartStock(row.partName) : '-' }}</template>
                      </el-table-column>
                      <el-table-column label="数量" width="120" align="center">
                        <template #default="{ row }">
                          <el-input-number v-model="row.quantity" :min="1" :max="getSparePartStock(row.partName) != null ? getSparePartStock(row.partName) : undefined" :precision="0" size="small" controls-position="right" style="width: 100px" />
                        </template>
                      </el-table-column>
                      <el-table-column label="单位" width="70" align="center">
                        <template #default="{ row }">{{ getSparePartUnit(row.partName) }}</template>
                      </el-table-column>
                      <el-table-column label="操作" width="70" align="center">
                        <template #default="{ $index }">
                          <el-button link type="danger" icon="Delete" @click="completeSparePartList.splice($index, 1)"></el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                    <el-button type="primary" plain icon="Plus" size="small" @click="completeSparePartList.push({ partName: undefined, quantity: 1 })">添加备件</el-button>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12"><el-form-item label="维修费用"><el-input-number v-model="completeForm.repairCost" :min="0" :precision="2" :step="100" controls-position="right" style="width: 100%" /><span style="margin-left: 8px; color: #999">元</span></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="停机时长"><el-input-number v-model="completeForm.downtimeDuration" :min="0" :precision="2" controls-position="right" style="width: 100%" /><span style="margin-left: 8px; color: #999">小时</span></el-form-item></el-col>
            </el-row>
          </div>
        </section>

        <!-- 分组三：其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('cp_other')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cp_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.cp_other">
            <el-row>
              <el-col :span="24"><el-form-item label="完工说明" required><el-input v-model="completeForm.completeRemark" type="textarea" :rows="2" placeholder="请输入完工说明" /></el-form-item></el-col>
              <el-col :span="24" v-if="completeForm.orderType === '1'"><el-form-item label="偏差原因"><el-input v-model="completeForm.deviationReason" type="textarea" :rows="2" placeholder="如未按计划时间完成，请说明原因（选填）" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
      </el-form>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitComplete">确认完工</el-button>
        <el-button @click="completeOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 验收弹窗 -->
    <el-dialog v-model="verifyOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">工单验收</span>
          <div class="rd-detail-header-sub" v-if="verifyForm.orderNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">工单号：{{ verifyForm.orderNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
      <el-form ref="verifyRef" :model="verifyForm" label-width="100px">
        <!-- 分组一：工单信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vf_order')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>工单信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vf_order }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vf_order" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ verifyForm.orderNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">工单类型</span><div class="rd-value"><dict-tag :options="dms_order_type" :value="verifyForm.orderType" /></div></div>
              <div class="rd-item"><span class="rd-label">设备编号</span><div class="rd-value">{{ verifyForm.equipmentCode || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">设备名称</span><div class="rd-value">{{ verifyForm.equipmentName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">报修人</span><div class="rd-value">{{ verifyForm.reporterName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">报修时间</span><div class="rd-value">{{ verifyForm.reportTime || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">故障描述</span><div class="rd-value desc-pre">{{ cleanFaultDescription(verifyForm.faultDescription) || '—' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 分组二：维修信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vf_repair')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></span>维修信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vf_repair }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vf_repair" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">维修人</span><div class="rd-value">{{ verifyForm.assigneeName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">完工时间</span><div class="rd-value">{{ verifyForm.completeTime || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">故障原因</span><div class="rd-value">{{ verifyForm.faultCause || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">维修措施</span><div class="rd-value">{{ verifyForm.repairMeasure || '—' }}</div></div>
              <div class="rd-item" v-if="verifyForm.sparePartsUsed"><span class="rd-label">更换备件</span><div class="rd-value">{{ verifyForm.sparePartsUsed }}</div></div>
              <div class="rd-item" v-if="verifyForm.repairCost != null"><span class="rd-label">维修费用</span><div class="rd-value">{{ verifyForm.repairCost }} 元</div></div>
              <div class="rd-item" v-if="verifyForm.downtimeDuration != null"><span class="rd-label">停机时长</span><div class="rd-value">{{ verifyForm.downtimeDuration }} 小时</div></div>
            </div>
            <el-alert v-if="verifyForm.sparePartsUsed" type="warning" :closable="false" show-icon style="margin-top: 12px">
              <template #title>该工单使用了备件，验收通过后将自动创建出库单并扣减库存</template>
            </el-alert>
            <!-- 任务清单完成情况（完工时填写，供验收人逐项核对） -->
            <div v-if="verifyTaskList.length > 0" class="task-checklist-block">
              <div class="task-table-toolbar">
                <span class="task-table-title">任务清单完成情况</span>
                <span class="task-table-summary">已完成 <b class="task-summary-done">{{ verifyTaskList.filter(t => t.result === 'done').length }}</b> / 未完成 <b class="task-summary-undone">{{ verifyTaskList.filter(t => t.result === 'undone').length }}</b> / 不适用 <b>{{ verifyTaskList.filter(t => t.result === 'na').length }}</b></span>
              </div>
              <el-table :data="verifyTaskList" border size="small">
                <el-table-column label="序号" type="index" width="60" align="center" />
                <el-table-column label="任务" prop="text" min-width="200" show-overflow-tooltip />
                <el-table-column label="完成情况" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag v-if="row.result === 'done'" type="success" size="small">已完成</el-tag>
                    <el-tag v-else-if="row.result === 'undone'" type="danger" size="small">未完成</el-tag>
                    <el-tag v-else-if="row.result === 'na'" type="info" size="small">不适用</el-tag>
                    <span v-else class="text-muted">待填写</span>
                  </template>
                </el-table-column>
                <el-table-column label="备注" prop="remark" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">{{ row.remark || '-' }}</template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </section>
        <!-- 分组三：验收意见 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vf_verify')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>验收意见</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vf_verify }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vf_verify">
            <el-row>
              <el-col :span="12">
                <el-form-item label="验收人" required>
                  <el-input v-model="verifyForm.verifierName" readonly placeholder="请选择验收人" style="width: 100%" @click="openUserPicker('verifyForm', 'verifierId', 'verifierName')"><template #append><el-button icon="Search" @click="openUserPicker('verifyForm', 'verifierId', 'verifierName')" /></template><template #suffix><el-icon v-if="verifyForm.verifierName" class="clear-icon" @click.stop="verifyForm.verifierId = undefined; verifyForm.verifierName = undefined"><CircleClose /></el-icon></template></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="评价">
                  <el-rate v-model="verifyForm.rating" :max="5" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="验收意见">
                  <el-input v-model="verifyForm.verifyOpinion" type="textarea" :rows="3" placeholder="请输入验收意见" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
      </el-form>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitVerify">确认验收</el-button>
        <el-button @click="verifyOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewOpen" width="1080px" append-to-body draggable class="rd-dialog workorder-detail-dialog" top="5vh">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
          <span class="rd-detail-header-title">工单详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.orderNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">工单号：{{ viewForm.orderNo }}</span>
          </div>
          <div class="rd-detail-header-tags" v-if="viewForm.orderStatus != null">
            <el-tag v-if="viewForm.slaTimeoutStatus === '2' && !isTerminalStatus(viewForm.orderStatus)" type="danger" effect="dark" size="small">处理超时</el-tag>
            <el-tag :type="orderStatusTagType(viewForm.orderStatus)" effect="dark" size="small">{{ statusText(viewForm.orderStatus) }}</el-tag>
          </div>
        </div>
      </template>
      <div v-loading="viewLoading" class="rd-page">
        <el-tabs v-model="viewActiveTab" class="wo-detail-tabs">
        <!-- 页签一：工单详情 -->
        <el-tab-pane label="工单详情" name="detail">
        <!-- 状态进度条 -->
        <div class="status-progress">
          <div v-for="(step, i) in statusSteps" :key="i" class="sp-step" :class="{ 'is-done': isStepDone(i), 'is-active': isStepActive(i) }">
            <div class="sp-dot">
              <el-icon v-if="isStepDone(i)" size="12"><Check /></el-icon>
              <span v-else class="sp-num">{{ i + 1 }}</span>
            </div>
            <span class="sp-label">{{ step.label }}</span>
            <div v-if="i < statusSteps.length - 1" class="sp-connector" :class="{ 'is-passed': isStepDone(i) }">
              <span class="sp-line"></span>
              <span class="sp-arrow"></span>
            </div>
          </div>
        </div>
        <!-- 工单信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('wo_order')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>工单信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.wo_order }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.wo_order">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ viewForm.orderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工单类型</span><div class="rd-value"><dict-tag :options="dms_order_type" :value="viewForm.orderType" /></div></div>
              <div class="rd-item"><span class="rd-label">优先级</span><div class="rd-value"><dict-tag :options="dms_priority" :value="viewForm.priority" /></div></div>
              <div class="rd-item"><span class="rd-label">报修人</span><div class="rd-value">{{ viewForm.reporterName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">报修时间</span><div class="rd-value">{{ viewForm.reportTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工单状态</span><div class="rd-value"><dict-tag :options="dms_order_status" :value="viewForm.orderStatus" /></div></div>
            </div>
          </div>
        </section>
        <!-- 设备信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('wo_equipment')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg></span>设备信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.wo_equipment }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.wo_equipment">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">设备编号</span><div class="rd-value">{{ viewForm.equipmentCode || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">设备名称</span><div class="rd-value">{{ viewForm.equipmentName || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">故障描述</span><div class="rd-value desc-pre">{{ cleanFaultDescription(viewForm.faultDescription) || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 派工信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('wo_dispatch')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>派工信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.wo_dispatch }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.wo_dispatch">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">维修人</span><div class="rd-value">{{ viewForm.assigneeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">派工人</span><div class="rd-value">{{ viewForm.assignerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">派工时间</span><div class="rd-value">{{ viewForm.assignTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">到达时间</span><div class="rd-value">{{ viewForm.arriveTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">SLA响应截止</span><div class="rd-value" v-if="viewForm.slaResponseDeadline"><span :class="{ 'sla-warning': viewForm.slaTimeoutStatus === '1' }">{{ viewForm.slaResponseDeadline }}</span><el-tag v-if="viewForm.slaTimeoutStatus === '1'" type="danger" size="small" style="margin-left: 6px">已超时</el-tag></div><div class="rd-value text-muted" v-else>派工后自动生成</div></div>
              <div class="rd-item"><span class="rd-label">SLA处理截止</span><div class="rd-value"><span :class="{ 'sla-warning': viewForm.slaTimeoutStatus === '2' }">{{ viewForm.slaProcessDeadline || '-' }}</span><el-tag v-if="viewForm.slaTimeoutStatus === '2'" type="danger" size="small" style="margin-left: 6px">已超时</el-tag></div></div>
            </div>
          </div>
        </section>
        <!-- 维修处理 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('wo_repair')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></span>维修处理</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.wo_repair }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.wo_repair">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">故障原因</span><div class="rd-value">{{ viewForm.faultCause || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">维修措施</span><div class="rd-value">{{ viewForm.repairMeasure || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">更换备件</span><div class="rd-value">{{ viewForm.sparePartsUsed || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">维修费用</span><div class="rd-value">{{ viewForm.repairCost != null ? '¥' + viewForm.repairCost : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">停机时长</span><div class="rd-value">{{ viewForm.downtimeDuration != null ? viewForm.downtimeDuration + ' 小时' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">完工时间</span><div class="rd-value">{{ viewForm.completeTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">完工说明</span><div class="rd-value">{{ viewForm.completeRemark || '-' }}</div></div>
              <div class="rd-item rd-item--full" v-if="viewForm.deviationReason"><span class="rd-label">偏差原因</span><div class="rd-value">{{ viewForm.deviationReason }}</div></div>
            </div>
            <!-- 任务清单完成情况 -->
            <div v-if="viewForm.taskChecklist" class="task-checklist">
              <div class="task-checklist-title">任务清单完成情况</div>
              <el-table :data="parseTaskChecklist(viewForm.taskChecklist)" border size="small" @header-dragend="onHeaderDragEnd">
                <el-table-column label="序号" type="index" width="60" align="center" />
                <el-table-column label="任务" prop="text" min-width="200" show-overflow-tooltip />
                <el-table-column label="完成情况" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag v-if="row.result === 'done'" type="success" size="small">已完成</el-tag>
                    <el-tag v-else-if="row.result === 'undone'" type="danger" size="small">未完成</el-tag>
                    <el-tag v-else-if="row.result === 'na'" type="info" size="small">不适用</el-tag>
                    <span v-else class="text-muted">待填写</span>
                  </template>
                </el-table-column>
                <el-table-column label="备注" min-width="200" show-overflow-tooltip>
                  <template #default="{ row }">{{ row.remark || '-' }}</template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </section>
        <!-- 验收信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('wo_verify')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></span>验收信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.wo_verify }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.wo_verify">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">验收人</span><div class="rd-value">{{ viewForm.verifierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">验收时间</span><div class="rd-value">{{ viewForm.verifyTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">评价</span><div class="rd-value"><el-rate v-if="viewForm.rating != null" v-model="viewForm.rating" :max="5" disabled show-score :texts="['极差', '失望', '一般', '满意', '非常满意']" /><span v-else>-</span></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">验收意见</span><div class="rd-value">{{ viewForm.verifyOpinion || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 系统信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('wo_system')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>系统信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.wo_system }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.wo_system">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewForm.createBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">更新人</span><div class="rd-value">{{ viewForm.updateBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">更新时间</span><div class="rd-value">{{ viewForm.updateTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewForm.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        </el-tab-pane>

        <!-- 页签二：操作历史 -->
        <el-tab-pane label="操作历史" name="log">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('wo_log')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v5h5"/><path d="M3.05 13A9 9 0 1 0 6 5.3L3 8"/><polyline points="12 7 12 12 15 14"/></svg></span>操作历史</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.wo_log }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.wo_log" v-loading="logLoading">
              <el-timeline v-if="logList.length > 0">
                <el-timeline-item v-for="(item, i) in logList" :key="i" :timestamp="item.createTime" placement="top"
                  :type="item.action === 'create' ? 'primary' : (item.action === 'complete' || item.action === 'verify') ? 'success' : (item.action === 'reject' || item.action === 'cancel') ? 'danger' : 'info'">
                  <el-card shadow="never">
                    <h4>{{ actionText(item.action) }}：{{ statusText(item.fromStatus) }} → {{ statusText(item.toStatus) }}</h4>
                    <p style="color: #999; font-size: 13px">操作人：{{ item.operatorName || '-' }}</p>
                    <p v-if="item.remark" style="font-size: 13px">{{ item.remark }}</p>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
              <el-empty v-else description="暂无操作记录" />
            </div>
          </section>
        </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg></div>
          <span class="rd-detail-header-title">工单驳回</span>
          <div class="rd-detail-header-sub" v-if="rejectForm.orderNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">工单号：{{ rejectForm.orderNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
      <el-form ref="rejectRef" :model="rejectForm" label-width="100px">
        <!-- 分组一：工单信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rj_order')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>工单信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rj_order }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.rj_order" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ rejectForm.orderNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">工单类型</span><div class="rd-value"><dict-tag :options="dms_order_type" :value="rejectForm.orderType" /></div></div>
              <div class="rd-item"><span class="rd-label">设备编号</span><div class="rd-value">{{ rejectForm.equipmentCode || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">设备名称</span><div class="rd-value">{{ rejectForm.equipmentName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">报修人</span><div class="rd-value">{{ rejectForm.reporterName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">报修时间</span><div class="rd-value">{{ rejectForm.reportTime || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">故障描述</span><div class="rd-value desc-pre">{{ cleanFaultDescription(rejectForm.faultDescription) || '—' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 分组二：维修信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rj_repair')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></span>维修信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rj_repair }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.rj_repair" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">维修人</span><div class="rd-value">{{ rejectForm.assigneeName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">完工时间</span><div class="rd-value">{{ rejectForm.completeTime || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">故障原因</span><div class="rd-value">{{ rejectForm.faultCause || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">维修措施</span><div class="rd-value">{{ rejectForm.repairMeasure || '—' }}</div></div>
              <div class="rd-item" v-if="rejectForm.sparePartsUsed"><span class="rd-label">更换备件</span><div class="rd-value">{{ rejectForm.sparePartsUsed }}</div></div>
            </div>
            <!-- 任务清单完成情况（完工时填写，供驳回人对照定位问题任务） -->
            <div v-if="rejectTaskList.length > 0" class="task-checklist-block">
              <div class="task-table-toolbar">
                <span class="task-table-title">任务清单完成情况</span>
                <span class="task-table-summary">已完成 <b class="task-summary-done">{{ rejectTaskList.filter(t => t.result === 'done').length }}</b> / 未完成 <b class="task-summary-undone">{{ rejectTaskList.filter(t => t.result === 'undone').length }}</b> / 不适用 <b>{{ rejectTaskList.filter(t => t.result === 'na').length }}</b></span>
              </div>
              <el-table :data="rejectTaskList" border size="small">
                <el-table-column label="序号" type="index" width="60" align="center" />
                <el-table-column label="任务" prop="text" min-width="200" show-overflow-tooltip />
                <el-table-column label="完成情况" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag v-if="row.result === 'done'" type="success" size="small">已完成</el-tag>
                    <el-tag v-else-if="row.result === 'undone'" type="danger" size="small">未完成</el-tag>
                    <el-tag v-else-if="row.result === 'na'" type="info" size="small">不适用</el-tag>
                    <span v-else class="text-muted">待填写</span>
                  </template>
                </el-table-column>
                <el-table-column label="备注" prop="remark" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">{{ row.remark || '-' }}</template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </section>
        <!-- 分组三：驳回意见 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rj_reject')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg></span>驳回意见</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rj_reject }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.rj_reject">
            <el-row>
              <el-col :span="24">
                <el-form-item label="驳回原因" required>
                  <el-input v-model="rejectForm.verifyOpinion" type="textarea" :rows="3" placeholder="请输入驳回原因，以便维修人员了解问题并重新处理" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
      </el-form>
      </div>
      <template #footer>
        <el-button type="danger" @click="submitReject">确认驳回</el-button>
        <el-button @click="rejectOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="工单管理业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">新建</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">已派工</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已接单</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">处理中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完成</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已验收</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="info">新建/已派工</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已撤销</el-tag>
            <el-tag size="small" type="info">终态</el-tag>
          </div>
          <div class="flow-item" style="margin-left: 16px;">
            <el-tag type="success">已完成</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">驳回重做</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">处理中</el-tag>
            <el-tag size="small" type="info">重新维修</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="新建">报修后创建工单的初始状态，可编辑、删除、派工、撤销。支持AI智能报修自动识别故障和优先级</el-descriptions-item>
          <el-descriptions-item label="已派工">已分配维修人，维修人可接单或申请改派。系统自动记录SLA响应截止时间</el-descriptions-item>
          <el-descriptions-item label="已接单">维修人已确认接单，可开始处理。系统自动记录SLA处理截止时间</el-descriptions-item>
          <el-descriptions-item label="处理中">维修人正在维修，填写故障原因、维修措施、更换备件等信息后可提交完工</el-descriptions-item>
          <el-descriptions-item label="已完成">维修人完工提交，等待验收人确认。完工时需填写故障原因和维修措施</el-descriptions-item>
          <el-descriptions-item label="已验收">验收通过，工单流程结束。若使用了备件，系统自动创建备件出库单并扣减库存</el-descriptions-item>
          <el-descriptions-item label="已撤销">新建或已派工状态可撤销，工单终止</el-descriptions-item>
          <el-descriptions-item label="驳回重做">验收未通过，退回给维修人重新处理，回到处理中状态</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">SLA超时预警</div>
              <div class="highlight-card-body">系统根据工单优先级自动计算<strong>响应截止时间</strong>和处理截止时间，超时后在列表和详情中显示红色预警标签</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">备件自动出库</div>
              <div class="highlight-card-body">工单验收通过后，若使用了备件，系统<strong>自动创建备件出库单</strong>并扣减库存，无需手动操作</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">AI智能辅助</div>
              <div class="highlight-card-body">支持<strong>AI智能报修</strong>（自然语言描述自动识别故障和优先级）和<strong>AI智能派工推荐</strong>（根据工作负载和评分推荐维修人）</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">任务清单</div>
              <div class="highlight-card-body">PM计划生成的工单自带<strong>标准任务清单</strong>，维修人员在完工时需逐项填写完成情况与备注</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建工单：</strong>选择设备、填写故障描述、设置优先级和工单类型。支持AI智能报修辅助填写
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>派工：</strong>指派维修人，支持AI智能推荐。派工后系统自动计算SLA响应截止时间
          </el-timeline-item>
          <el-timeline-item type="primary" :hollow="true">
            <strong>接单与处理：</strong>维修人接单后开始维修，填写故障原因、维修措施，可记录更换备件
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>完工提交：</strong>填写完工说明、维修费用、停机时长，提交后等待验收
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>验收：</strong>验收人确认维修结果，通过则工单完成，驳回则退回重新维修。使用备件的工单验收后自动出库</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

<user-picker ref="userPickerRef" title="选择人员" @confirm="onUserPickerConfirm" />
<equipment-picker ref="equipmentPickerRef" title="选择设备" @confirm="onEquipmentPickerConfirm" />
</div>
</template>

<script setup name="DmsWorkOrder">
import { listWorkorder, getWorkorder, addWorkorder, updateWorkorder, delWorkorder,
  dispatchWorkorder, reassignWorkorder, acceptWorkorder, processWorkorder,
  completeWorkorder, verifyWorkorder, rejectWorkorder, cancelWorkorder, getWorkorderLog, countWorkorderStatus } from '@/api/dms/workorder'
import { repairAssistant, dispatchRecommend } from '@/api/dms/ai'
import { listUser } from '@/api/system/user'
import { listSparepart } from '@/api/dms/sparepart'
import UserPicker from '@/components/UserPicker/index.vue'
import EquipmentPicker from '@/components/EquipmentPicker/index.vue'
import useUserStore from '@/store/modules/user'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { ArrowDown, Search, Filter, RefreshLeft, Edit, Delete, Download } from '@element-plus/icons-vue'
const { collapsedCards, toggleCard } = useDetailCard(['wo_order', 'wo_equipment', 'wo_dispatch', 'wo_repair', 'wo_verify', 'wo_system', 'dc_wo', 'dc_form', 'ed_order', 'ed_equipment', 'ed_dispatch', 'ed_repair', 'ed_verify', 'ed_other', 'cp_order', 'cp_repair', 'cp_other', 'vf_order', 'vf_repair', 'vf_verify', 'rj_order', 'rj_repair', 'rj_reject'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_workorder_index')
const { dms_order_type, dms_order_status, dms_priority, wms_unit } = proxy.useDict('dms_order_type', 'dms_order_status', 'dms_priority', 'wms_unit')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const defaultColumns = {
  orderNo: { label: '工单号', visible: true },
  equipmentName: { label: '设备名称', visible: true },
  orderType: { label: '工单类型', visible: true },
  faultDescription: { label: '故障描述', visible: true },
  reporterName: { label: '报修人', visible: true },
  priority: { label: '优先级', visible: true },
  assigneeName: { label: '维修人', visible: true },
  orderStatus: { label: '工单状态', visible: true },
  reportTime: { label: '报修时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_workorder_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.orderNo) count++
  if (queryParams.value.equipmentCode) count++
  if (queryParams.value.equipmentName) count++
  if (queryParams.value.orderStatus) count++
  if (queryParams.value.orderType) count++
  if (queryParams.value.priority) count++
  if (queryParams.value.reporterName) count++
  if (queryParams.value.assigneeName) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})
function orderTypeLabel(val) { const item = dms_order_type.value.find(d => d.value == val); return item ? item.label : '-' }
function priorityLabel(val) { const item = dms_priority.value.find(d => d.value == val); return item ? item.label : '-' }
function orderStatusLabel(val) { const item = dms_order_status.value.find(d => d.value == val); return item ? item.label : '-' }
function orderStatusBadgeClass(status) {
  const map = { '0': 'amber', '1': 'blue', '2': 'blue', '3': 'blue', '4': 'green', '5': 'green', '6': 'red', '7': 'blue' }
  return map[status] || 'gray'
}

const dispatchOpen = ref(false)
const completeOpen = ref(false)
const verifyOpen = ref(false)
const rejectOpen = ref(false)
const isReassign = ref(false)
const viewOpen = ref(false)
const viewForm = ref({})
const viewLoading = ref(false)
const viewActiveTab = ref('detail')
const logList = ref([])
const logLoading = ref(false)
const aiDialog = ref(false)
const aiInput = ref('')
const aiLoading = ref(false)
const aiResult = ref(null)
const showStatusHelp = ref(false)
const recommendLoading = ref(false)
const recommendations = ref([])
// 状态页签
const activeStatusTab = ref('all')
const statusCounts = ref({})
const statusTabList = computed(() => {
  if (!dms_order_status.value || dms_order_status.value.length === 0) return []
  return dms_order_status.value.filter(d => d.value !== undefined && d.value !== null).map(d => ({ label: d.label, value: d.value }))
})
function statusTabClass(status) {
  const map = { '0': 'tab-amber', '1': 'tab-blue', '2': 'tab-blue', '3': 'tab-blue', '4': 'tab-green', '5': 'tab-green', '6': 'tab-red', '7': 'tab-blue' }
  return map[status] || 'tab-gray'
}
function handleStatusTabClick(status) {
  activeStatusTab.value = status
  if (status === 'all') {
    queryParams.value.orderStatus = undefined
  } else {
    queryParams.value.orderStatus = status
  }
  handleQuery()
}
function loadStatusCounts() {
  countWorkorderStatus().then(res => {
    statusCounts.value = res.data || {}
  }).catch(() => {})
}
const userOptions = ref([])
const currentPickerTarget = ref(null)
const currentPickerIdField = ref('')
const currentPickerNameField = ref('')
const sparePartOptions = ref([])
const completeSparePartList = ref([])
const completeTaskList = ref([])
const verifyTaskList = ref([])
const rejectTaskList = ref([])
const formSparePartList = ref([])
const isAdd = computed(() => form.value.orderId == undefined)

const data = reactive({
  form: {},
  dispatchForm: {},
  completeForm: {},
  verifyForm: {},
  rejectForm: {},
  queryParams: { pageNum: 1, pageSize: 10, orderNo: undefined, equipmentCode: undefined, equipmentName: undefined, orderStatus: undefined, orderType: undefined, priority: undefined, reporterName: undefined, assigneeName: undefined },
  rules: {
    equipmentId: [{ required: true, message: '设备不能为空', trigger: 'change' }],
    orderType: [{ required: true, message: '工单类型不能为空', trigger: 'change' }],
    faultDescription: [{ required: true, message: '故障描述不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)
const { dispatchForm, completeForm, verifyForm, rejectForm } = toRefs(data)

function getList() {
  loading.value = true
  listWorkorder(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() })
}
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value, 'ReportTime'); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.orderNo = undefined; queryParams.value.equipmentCode = undefined; queryParams.value.equipmentName = undefined; queryParams.value.orderType = undefined; queryParams.value.orderStatus = undefined; queryParams.value.priority = undefined; queryParams.value.reporterName = undefined; queryParams.value.assigneeName = undefined; dateRange.value = []; queryParams.value.params = {}; proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.orderId); single.value = selection.length !== 1; multiple.value = !selection.length }
function getUserList() {
  listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) })
}
function getSparePartList() {
  listSparepart({ pageNum: 1, pageSize: 9999, status: '0' }).then(res => { sparePartOptions.value = res.rows })
}
/** 根据备件名称获取单位（转换为字典标签） */
function getSparePartUnit(partName) {
  if (!partName) return ''
  const part = sparePartOptions.value.find(p => p.partName === partName)
  if (!part) return ''
  const dictItem = wms_unit.value.find(d => d.value === part.unit)
  return dictItem ? dictItem.label : part.unit
}
/** 根据备件名称获取库存数量 */
function getSparePartStock(partName) {
  if (!partName) return null
  const part = sparePartOptions.value.find(p => p.partName === partName)
  return part ? part.currentStock : null
}
/** 校验备件列表中数量不超库存 */
function validateSparePartStock(list) {
  for (const item of list) {
    if (!item.partName) continue
    const stock = getSparePartStock(item.partName)
    if (stock != null && item.quantity > stock) {
      proxy.$modal.msgError(`备件「${item.partName}」使用数量 ${item.quantity} 超过库存数量 ${stock}`)
      return false
    }
  }
  return true
}
/** 获取可选备件列表（排除当前表格中其他行已选的备件） */
function getAvailableSpareParts(list, currentName) {
  const selectedNames = list.filter(i => i.partName && i.partName !== currentName).map(i => i.partName)
  return sparePartOptions.value.filter(p => !selectedNames.includes(p.partName) && p.currentStock != null && p.currentStock > 0)
}
/** 备件列表序列化为字符串：轴承×2，密封圈×5 */
function sparePartsToString(list) {
  const valid = list.filter(i => i.partName)
  return valid.length > 0 ? valid.map(i => i.partName + '×' + (i.quantity || 1)).join('，') : ''
}
/** 字符串解析为备件列表 */
function parseSpareParts(str) {
  if (!str) return []
  return str.split('，').filter(s => s.trim()).map(s => {
    const idx = s.indexOf('×')
    if (idx > 0) {
      return { partName: s.substring(0, idx).trim(), quantity: parseInt(s.substring(idx + 1)) || 1 }
    }
    return { partName: s.trim(), quantity: 1 }
  })
}
/** 清洗故障描述：去除可能包含的JSON字段名、键值对格式、嵌入的JSON任务清单，只保留纯文本 */
function cleanFaultDescription(desc) {
  if (!desc) return ''
  let text = desc.trim()
  // 如果是JSON字符串（以{开头），尝试解析提取有意义的字段
  if (text.startsWith('{')) {
    try {
      const obj = JSON.parse(text)
      // 提取常见的描述字段
      const fields = ['faultDescription', 'description', 'desc', 'remark', 'content', 'text', 'message']
      for (const f of fields) {
        if (obj[f]) { text = String(obj[f]); break }
      }
      // 如果没找到，拼接所有非空值
      if (text.startsWith('{')) {
        const parts = Object.entries(obj).filter(([k, v]) => v && typeof v === 'string').map(([k, v]) => `${k}: ${v}`)
        text = parts.join('，') || text
      }
    } catch (e) {
      // JSON解析失败，按文本处理
    }
  }
  // 去除 "字段名:" 前缀模式（如 "faultDescription: xxx"）
  text = text.replace(/^(faultDescription|description|desc|remark|content|text|message)\s*[:：]\s*/i, '')
  // 去除嵌入的JSON任务清单（如 "...任务清单：[{...}]" 或 "...任务清单：[...]"）
  text = text.replace(/\s*任务清单[：:]\s*\[.*?\]\s*$/s, '')
  // 去除末尾的JSON数组（以 [{" 或 [{" 开头到结尾）
  text = text.replace(/\s+\[\{.*?\}\]\s*$/s, '')
  // 去除多余的 { } 括号
  text = text.replace(/^\{+\s*/, '').replace(/\s*\}+$/, '')
  return text.trim()
}
/** 解析任务清单JSON（明细结构 [{text,done,result,remark}]，兼容旧 [{text,done}] 与纯文本） */
function parseTaskChecklist(str) {
  if (!str) return []
  try {
    const arr = JSON.parse(str)
    if (Array.isArray(arr)) {
      return arr.map(item => {
        if (typeof item === 'string') return { text: item, done: false, result: '', remark: '' }
        const result = item.result || (item.done ? 'done' : '')
        return { text: item.text || item.task || item.name || '', done: result === 'done', result, remark: item.remark || '' }
      })
    }
  } catch (e) {
    return str.split('\n').filter(s => s.trim()).map(s => ({ text: s.trim(), done: false, result: '', remark: '' }))
  }
  return []
}
/** 完工弹窗：已填写完成情况的任务数 */
const filledTaskCount = computed(() => completeTaskList.value.filter(t => t.result).length)
/** 选择设备后自动带出设备编号和名称 */
/** 打开设备选择弹窗 */
function openEquipmentPicker() {
  proxy.$refs.equipmentPickerRef.open(form.value.equipmentId)
}
/** 设备选择确认回调 */
function onEquipmentPickerConfirm(eq) {
  form.value.equipmentId = eq.equipmentId
  form.value.equipmentCode = eq.equipmentCode
  form.value.equipmentName = eq.equipmentName
}
/** 清除设备 */
function clearEquipment() {
  form.value.equipmentId = undefined
  form.value.equipmentCode = undefined
  form.value.equipmentName = undefined
}
/** 打开人员选择弹窗（支持多个目标form） */
function openUserPicker(target, idField, nameField) {
  currentPickerTarget.value = target
  currentPickerIdField.value = idField
  currentPickerNameField.value = nameField
  const targetForm = target === 'form' ? form.value : (target === 'dispatchForm' ? dispatchForm.value : verifyForm.value)
  proxy.$refs.userPickerRef.open(targetForm[idField])
}
/** 人员选择确认回调 */
function onUserPickerConfirm(user) {
  if (!currentPickerTarget.value) return
  const targetForm = currentPickerTarget.value === 'form' ? form.value : (currentPickerTarget.value === 'dispatchForm' ? dispatchForm.value : verifyForm.value)
  targetForm[currentPickerIdField.value] = user.userId
  targetForm[currentPickerNameField.value] = user.nickName
}
function reset() {
  const userStore = useUserStore()
  const now = proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}')
  form.value = { orderType: '0', priority: '2', orderStatus: '0', faultDescription: undefined, equipmentId: undefined, equipmentCode: undefined, equipmentName: undefined,
    reporterId: userStore.id, reporterName: userStore.nickName, reportTime: now, assigneeId: undefined, assigneeName: undefined,
    assignerId: undefined, assignerName: undefined, arriveTime: undefined,
    faultCause: undefined, repairMeasure: undefined, sparePartsUsed: undefined, repairCost: undefined, downtimeDuration: undefined,
    completeTime: undefined, completeRemark: undefined, verifierId: undefined, verifierName: undefined, verifyOpinion: undefined, rating: 0, remark: undefined }
  proxy.resetForm('workorderRef')
}
function handleView(row) {
  viewLoading.value = true
  viewOpen.value = true
  viewActiveTab.value = 'detail'
  getWorkorder(row.orderId).then(res => {
    viewForm.value = res.data
    viewLoading.value = false
  }).catch(() => { viewLoading.value = false })
  // 同时加载操作历史
  logLoading.value = true
  getWorkorderLog(row.orderId).then(res => {
    logList.value = res.data || []
    logLoading.value = false
  }).catch(() => { logLoading.value = false })
}
function handleAdd() { reset(); open.value = true; title.value = '新增工单' }
function handleUpdate(row) { reset(); getWorkorder(row.orderId || ids.value[0]).then(res => { form.value = res.data; formSparePartList.value = parseSpareParts(form.value.sparePartsUsed); open.value = true; title.value = '修改工单' }) }
function submitForm() {
  proxy.$refs['workorderRef'].validate(valid => {
    if (valid) {
      if (!validateSparePartStock(formSparePartList.value)) return
      form.value.sparePartsUsed = sparePartsToString(formSparePartList.value) || undefined
      if (form.value.orderId != undefined) { updateWorkorder(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addWorkorder(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const orderIds = row.orderId || ids.value; proxy.$modal.confirm('确认删除编号为"' + orderIds + '"的数据？').then(() => delWorkorder(orderIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('dms/workorder/export', { ...queryParams.value }, `workorder_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// ===== 状态机操作 =====
function handleDispatch(row) {
  isReassign.value = false
  const userStore = useUserStore()
  const now = proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}')
  dispatchForm.value = { orderId: row.orderId, orderNo: row.orderNo, equipmentCode: row.equipmentCode, equipmentName: row.equipmentName, orderType: row.orderType, priority: row.priority, faultDescription: row.faultDescription, reporterName: row.reporterName, reportTime: row.reportTime, assigneeId: undefined, assigneeName: '', assignerId: userStore.id, assignerName: userStore.nickName, assignTime: now }
  dispatchOpen.value = true
}
function submitDispatch() {
  if (!dispatchForm.value.assigneeId) { proxy.$modal.msgError('请选择维修人'); return }
  if (isReassign.value) {
    reassignWorkorder(dispatchForm.value).then(() => { proxy.$modal.msgSuccess('改派成功'); dispatchOpen.value = false; getList() })
  } else {
    dispatchWorkorder(dispatchForm.value).then(() => { proxy.$modal.msgSuccess('派工成功'); dispatchOpen.value = false; getList() })
  }
}
function handleReassign(row) {
  isReassign.value = true
  const userStore = useUserStore()
  const now = proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}')
  dispatchForm.value = { orderId: row.orderId, orderNo: row.orderNo, equipmentCode: row.equipmentCode, equipmentName: row.equipmentName, orderType: row.orderType, priority: row.priority, faultDescription: row.faultDescription, reporterName: row.reporterName, reportTime: row.reportTime, assigneeId: undefined, assigneeName: '', assignerId: userStore.id, assignerName: userStore.nickName, assignTime: now }
  dispatchOpen.value = true
}
function handleAccept(row) {
  proxy.$modal.confirm('确认接单？').then(() => acceptWorkorder(row.orderId)).then(() => { proxy.$modal.msgSuccess('接单成功'); getList() }).catch(() => {})
}
function handleProcess(row) {
  proxy.$modal.confirm('确认开始处理？').then(() => processWorkorder(row.orderId)).then(() => { proxy.$modal.msgSuccess('已开始处理'); getList() }).catch(() => {})
}
function handleComplete(row) {
  completeForm.value = { orderId: row.orderId, orderNo: row.orderNo, orderType: row.orderType, equipmentCode: row.equipmentCode, equipmentName: row.equipmentName, reporterName: row.reporterName, reportTime: row.reportTime, faultDescription: row.faultDescription, faultCause: '', repairMeasure: '', sparePartsUsed: '', repairCost: 0, downtimeDuration: 0, completeRemark: '', deviationReason: '' }
  completeSparePartList.value = []
  completeTaskList.value = []
  
  // 获取工单详情，带出任务清单
  getWorkorder(row.orderId).then(res => {
    if (res.data.taskChecklist) {
      completeTaskList.value = parseTaskChecklist(res.data.taskChecklist)
    }
    // 补充设备信息和故障描述
    if (res.data.equipmentCode) {
      completeForm.value.equipmentCode = res.data.equipmentCode
    }
    if (res.data.equipmentName) {
      completeForm.value.equipmentName = res.data.equipmentName
    }
    if (res.data.reporterName) {
      completeForm.value.reporterName = res.data.reporterName
    }
    if (res.data.reportTime) {
      completeForm.value.reportTime = res.data.reportTime
    }
    if (res.data.faultDescription) {
      completeForm.value.faultDescription = res.data.faultDescription
    }
  })
  completeOpen.value = true
}
function submitComplete() {
  if (!completeForm.value.faultCause) { proxy.$modal.msgError('请输入故障原因'); return }
  if (!completeForm.value.repairMeasure) { proxy.$modal.msgError('请输入维修措施'); return }
  if (!completeForm.value.completeRemark) { proxy.$modal.msgError('请输入完工说明'); return }
  if (!validateSparePartStock(completeSparePartList.value)) return
  // 任务清单校验：每项必须选择完成情况，未完成任务必须填写原因备注
  if (completeTaskList.value.length > 0) {
    const unselected = completeTaskList.value.filter(t => !t.result)
    if (unselected.length > 0) { proxy.$modal.msgError(`任务清单还有 ${unselected.length} 项未选择完成情况，请逐项填写`); return }
    const noRemark = completeTaskList.value.filter(t => t.result === 'undone' && !t.remark)
    if (noRemark.length > 0) { proxy.$modal.msgError(`有 ${noRemark.length} 项未完成任务未填写备注原因，请补充`); return }
  }
  completeForm.value.sparePartsUsed = sparePartsToString(completeSparePartList.value)
  // 序列化任务清单完成情况明细（序号、任务、完成情况、备注）
  if (completeTaskList.value.length > 0) {
    completeForm.value.taskChecklist = JSON.stringify(completeTaskList.value.map(t => ({ text: t.text, done: t.result === 'done', result: t.result, remark: t.remark || '' })))
  }
  completeWorkorder(completeForm.value).then(() => { proxy.$modal.msgSuccess('完工成功'); completeOpen.value = false; getList() })
}
function handleVerify(row) {
  verifyForm.value = { orderId: row.orderId, orderNo: row.orderNo, equipmentCode: row.equipmentCode, equipmentName: row.equipmentName, orderType: row.orderType, faultDescription: row.faultDescription, reporterName: row.reporterName, reportTime: row.reportTime, assigneeName: row.assigneeName, verifierId: undefined, verifierName: '', rating: 5, verifyOpinion: '', sparePartsUsed: '', faultCause: '', repairMeasure: '', repairCost: null, downtimeDuration: null, completeTime: '' }
  // 获取工单详情，带出维修信息和备件信息
  getWorkorder(row.orderId).then(res => {
    const d = res.data
    verifyForm.value.sparePartsUsed = d.sparePartsUsed || ''
    verifyForm.value.faultCause = d.faultCause || ''
    verifyForm.value.repairMeasure = d.repairMeasure || ''
    verifyForm.value.repairCost = d.repairCost
    verifyForm.value.downtimeDuration = d.downtimeDuration
    verifyForm.value.completeTime = d.completeTime || ''
    if (d.equipmentName) verifyForm.value.equipmentName = d.equipmentName
    if (d.faultDescription) verifyForm.value.faultDescription = d.faultDescription
    // 解析任务清单完成情况明细（完工时填写，供验收人逐项核对）
    verifyTaskList.value = parseTaskChecklist(d.taskChecklist)
  })
  verifyOpen.value = true
}
function submitVerify() {
  if (!verifyForm.value.verifierId) { proxy.$modal.msgError('请选择验收人'); return }
  verifyWorkorder(verifyForm.value).then(() => {
    const msg = verifyForm.value.sparePartsUsed
      ? '验收成功，已自动创建备件出库单并扣减库存'
      : '验收成功'
    proxy.$modal.msgSuccess(msg)
    verifyOpen.value = false
    getList()
  })
}
function handleReject(row) {
  rejectForm.value = { orderId: row.orderId, orderNo: row.orderNo, equipmentCode: row.equipmentCode, equipmentName: row.equipmentName, orderType: row.orderType, faultDescription: row.faultDescription, reporterName: row.reporterName, reportTime: row.reportTime, assigneeName: row.assigneeName, sparePartsUsed: '', faultCause: '', repairMeasure: '', completeTime: '', verifyOpinion: '' }
  // 获取工单详情，带出维修信息
  getWorkorder(row.orderId).then(res => {
    const d = res.data
    rejectForm.value.sparePartsUsed = d.sparePartsUsed || ''
    rejectForm.value.faultCause = d.faultCause || ''
    rejectForm.value.repairMeasure = d.repairMeasure || ''
    rejectForm.value.completeTime = d.completeTime || ''
    if (d.equipmentName) rejectForm.value.equipmentName = d.equipmentName
    if (d.faultDescription) rejectForm.value.faultDescription = d.faultDescription
    // 解析任务清单完成情况明细（完工时填写，供驳回人对照定位问题任务）
    rejectTaskList.value = parseTaskChecklist(d.taskChecklist)
  })
  rejectOpen.value = true
}
function submitReject() {
  if (!rejectForm.value.verifyOpinion) { proxy.$modal.msgError('请输入驳回原因'); return }
  rejectWorkorder(rejectForm.value).then(() => { proxy.$modal.msgSuccess('已驳回'); rejectOpen.value = false; getList() })
}
function handleCancel(row) {
  proxy.$modal.confirm('确认撤销该工单？').then(() => cancelWorkorder(row.orderId)).then(() => { proxy.$modal.msgSuccess('已撤销'); getList() }).catch(() => {})
}

// ===== 工单操作历史 =====
function actionText(action) {
  const map = { create: '创建', dispatch: '派工', reassign: '改派', accept: '接单', process: '开始处理', complete: '完工', verify: '验收', reject: '驳回', cancel: '撤销' }
  return map[action] || action
}
function statusText(status) {
  if (!status) return '-'
  const map = { '0': '新建', '1': '已派工', '2': '已接单', '3': '处理中', '4': '已完成', '5': '已验收', '6': '已撤销', '7': '驳回重做' }
  return map[status] || status
}
/** 终态状态：已验收(5)、已撤销(6)，不再显示SLA超时标签 */
function isTerminalStatus(status) {
  return status === '5' || status === '6'
}

// ===== 工单详情页辅助 =====
const statusSteps = [
  { label: '新建', status: '0' },
  { label: '已派工', status: '1' },
  { label: '已接单', status: '2' },
  { label: '处理中', status: '3' },
  { label: '已完成', status: '4' },
  { label: '已验收', status: '5' }
]
function orderStatusTagType(status) {
  const map = { '0': 'info', '1': 'warning', '2': 'primary', '3': 'primary', '4': 'success', '5': 'success', '6': 'info', '7': 'danger' }
  return map[status] || 'info'
}
function isStepDone(index) {
  if (!viewForm.value.orderStatus) return false
  const current = parseInt(viewForm.value.orderStatus)
  const stepStatus = parseInt(statusSteps[index].status)
  // 已撤销(6)不算完成任何步骤
  if (current === 6) return false
  // 驳回重做(7)：已完成之前的步骤到处理中(3)
  if (current === 7) return stepStatus <= 3
  return stepStatus < current
}
function isStepActive(index) {
  if (!viewForm.value.orderStatus) return false
  const current = parseInt(viewForm.value.orderStatus)
  const stepStatus = parseInt(statusSteps[index].status)
  // 已撤销(6)：高亮新建
  if (current === 6) return index === 0
  // 驳回重做(7)：高亮处理中
  if (current === 7) return index === 3
  return stepStatus === current
}

// ===== AI 智能报修 =====
function callAI() {
  if (!aiInput.value) { proxy.$modal.msgError('请输入故障描述'); return }
  aiLoading.value = true
  repairAssistant(aiInput.value).then(res => {
    aiResult.value = res.data
    if (res.data.faultDescription) form.value.faultDescription = res.data.faultDescription
    if (res.data.priority) form.value.priority = res.data.priority
    aiDialog.value = false
    aiLoading.value = false
    proxy.$modal.msgSuccess('AI识别完成，请确认后提交')
  }).catch(() => { aiLoading.value = false })
}
// ===== AI 智能派工推荐 =====
function loadRecommend() {
  recommendLoading.value = true
  dispatchRecommend(dispatchForm.value.orderId).then(res => {
    recommendations.value = res.data.recommendations || []
    recommendLoading.value = false
    if (recommendations.value.length === 0) proxy.$modal.msgWarning('暂无推荐数据')
  }).catch(() => { recommendLoading.value = false })
}
/** AI推荐选择维修人 */
function selectRecommendation(rec) {
  dispatchForm.value.assigneeName = rec.assigneeName
  const user = userOptions.value.find(u => u.nickName === rec.assigneeName)
  if (user) {
    dispatchForm.value.assigneeId = user.userId
  }
}

getUserList()
getSparePartList()
getList()
loadStatusCounts()

// keep-alive 缓存页面重新激活时自动刷新列表
onActivated(() => {
  getList()
  loadStatusCounts()
})
</script>

<style scoped>
/* ===== 工单详情弹窗：头部标签 ===== */
.rd-detail-header-tags {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 故障描述：保留换行格式（点检生成的描述含多行明细） */
.desc-pre {
  white-space: pre-line;
  word-break: break-word;
  line-height: 1.7;
}

/* SLA 超时警告样式 */
.sla-warning {
  color: #f56c6c;
  font-weight: 600;
}

/* 任务清单明细表样式 */
.task-checklist {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #ebeef5;
}
.task-checklist-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
  font-size: 13px;
}
/* 完工弹窗：任务清单区块（铺满整行，与上方信息栅格对齐） */
.task-checklist-block {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed #ebeef5;
}
.task-table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.task-table-title {
  font-weight: 600;
  color: #303133;
  font-size: 13px;
}
.task-table-summary {
  font-size: 12px;
  color: #909399;
}
.task-table-summary b {
  color: #303133;
}
.task-table-summary b.task-summary-done {
  color: #67c23a;
}
.task-table-summary b.task-summary-undone {
  color: #f56c6c;
}

/* 弹窗内部滚动优化 - 使用非 scoped 样式块处理 */
</style>

<style>
.workorder-detail-dialog .el-dialog__body {
  max-height: 68vh;
  overflow-y: auto;
  padding: 16px 20px;
}
.workorder-detail-dialog .el-dialog__header {
  padding-bottom: 12px;
}

/* ===== 工单详情弹窗 Tabs（统一规范） ===== */
.wo-detail-tabs .el-tabs__header { margin-bottom: 14px; }
.wo-detail-tabs .el-tabs__nav-wrap::after { height: 1px; background-color: #e2e8f0; }
.wo-detail-tabs .el-tabs__item { font-size: 14px; font-weight: 600; color: #64748b; padding: 0 20px; }
.wo-detail-tabs .el-tabs__item.is-active { color: #4f46e5; }
.wo-detail-tabs .el-tabs__active-bar { background-color: #4f46e5; height: 2px; border-radius: 2px; }
.wo-detail-tabs .el-tab-pane { min-height: 200px; }
.wo-detail-tabs { width: 100%; }

/* ===== 状态进度条（紧凑横向，非scoped确保append-to-body弹窗内生效） ===== */
.status-progress {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  background: linear-gradient(135deg, #eef2ff 0%, #f0fdf4 100%);
  border: 1px solid #c7d2fe;
  border-radius: 10px;
  margin-bottom: 12px;
}
.sp-step {
  display: flex;
  align-items: center;
  gap: 7px;
  flex-shrink: 0;
}
.sp-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  background: #fff;
  color: #94a3b8;
  border: 2px solid #e2e8f0;
  transition: all 0.25s cubic-bezier(.16,.84,.44,1);
  flex-shrink: 0;
}
.sp-num {
  font-size: 11px;
  line-height: 1;
}
.sp-step.is-done .sp-dot {
  background: linear-gradient(135deg, #34d399, #10b981);
  color: #fff;
  border-color: #10b981;
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.35);
}
.sp-step.is-active .sp-dot {
  background: linear-gradient(135deg, #818cf8, #4f46e5);
  color: #fff;
  border-color: #4f46e5;
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.4);
}
.sp-label {
  font-size: 13px;
  color: #64748b;
  white-space: nowrap;
  transition: color 0.25s;
}
.sp-step.is-done .sp-label {
  color: #047857;
  font-weight: 600;
}
.sp-step.is-active .sp-label {
  color: #4338ca;
  font-weight: 600;
}
.sp-connector {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin: 0 2px;
}
.sp-line {
  width: 20px;
  height: 2px;
  background: #e2e8f0;
  transition: background 0.25s;
}
.sp-connector.is-passed .sp-line {
  background: linear-gradient(90deg, #34d399, #10b981);
}
.sp-arrow {
  width: 0;
  height: 0;
  border-left: 7px solid #cbd5e1;
  border-top: 5px solid transparent;
  border-bottom: 5px solid transparent;
  transition: border-left-color 0.25s;
}
.sp-connector.is-passed .sp-arrow {
  border-left-color: #10b981;
}
</style>

<style scoped>
/* ===== Design Tokens ===== */
.dms-workorder-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}
.dms-workorder-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.dms-workorder-page .filter-card { padding:14px 20px 16px; }
.dms-workorder-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-workorder-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-workorder-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-workorder-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.dms-workorder-page .filter-card .adv-link:hover { color:var(--brand-600); }
.dms-workorder-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.dms-workorder-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.dms-workorder-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-workorder-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-workorder-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-workorder-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-workorder-page .field { display:flex; flex-direction:column; gap:6px; }
.dms-workorder-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.dms-workorder-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.dms-workorder-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.dms-workorder-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.dms-workorder-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.dms-workorder-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.dms-workorder-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.dms-workorder-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.dms-workorder-page .field .control :deep(.el-select) { width:100%; }
.dms-workorder-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.dms-workorder-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.dms-workorder-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.dms-workorder-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.dms-workorder-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.dms-workorder-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.dms-workorder-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.dms-workorder-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.dms-workorder-page .btn-soft .el-icon { font-size:14px; }
.dms-workorder-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.dms-workorder-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.dms-workorder-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.dms-workorder-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.dms-workorder-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-workorder-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.dms-workorder-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }
.dms-workorder-page .table-wrap { overflow-x:auto; }
.dms-workorder-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-workorder-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-workorder-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-workorder-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-workorder-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-workorder-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-workorder-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-workorder-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-workorder-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-workorder-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-workorder-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.dms-workorder-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-workorder-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-workorder-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.dms-workorder-page .badge.amber .dot { background:var(--amber-500); }
.dms-workorder-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.dms-workorder-page .badge.blue .dot { background:var(--blue-500); }
.dms-workorder-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.dms-workorder-page .badge.violet .dot { background:var(--brand-500); }
.dms-workorder-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.dms-workorder-page .badge.green .dot { background:var(--green-500); }
.dms-workorder-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.dms-workorder-page .badge.red .dot { background:var(--red-500); }
.dms-workorder-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.dms-workorder-page .badge.gray .dot { background:var(--ink-400); }
.dms-workorder-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-workorder-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-workorder-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-workorder-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-workorder-page .pagination-container :deep(.el-pagination .btn-prev), .dms-workorder-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-workorder-page .pagination-container :deep(.el-pagination .btn-prev:hover), .dms-workorder-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.dms-workorder-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
.dms-workorder-page .clear-icon { cursor:pointer; color:#c0c4cc; font-size:14px; }
.dms-workorder-page .clear-icon:hover { color:#909399; }
/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行 */
.dms-workorder-page :deep(.col-action) { padding: 6px 4px !important; }
.dms-workorder-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.dms-workorder-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.dms-workorder-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.dms-workorder-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* 文本截断和静音色 */
.dms-workorder-page .text-truncate { display: inline-block; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.dms-workorder-page .text-muted { color: var(--ink-400); font-size: 13px; }

@media (max-width:1100px) { .dms-workorder-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-workorder-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-workorder-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* ===== Status Tabs ===== */
.dms-workorder-page .status-tabs { display: flex; align-items: center; gap: 12px; padding: 6px 10px 6px 12px; border-bottom: 1px solid var(--ink-200); background: #fff; }
.dms-workorder-page .tabs-track { display: flex; align-items: center; gap: 4px; flex: 1; min-width: 0; overflow-x: auto; scrollbar-width: none; }
.dms-workorder-page .tabs-track::-webkit-scrollbar { display: none; }
.dms-workorder-page .status-tab { display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px; border-radius: var(--r-sm); font-size: 14px; color: var(--ink-500); cursor: pointer; user-select: none; transition: all .15s var(--ease-out); white-space: nowrap; border: 1px solid transparent; background: transparent; }
.dms-workorder-page .status-tab .dot { width: 6px; height: 6px; border-radius: 50%; background: var(--ink-300); }
.dms-workorder-page .status-tab .count { font-size: 12px; font-weight: 600; padding: 1px 6px; border-radius: 999px; background: var(--ink-100); color: var(--ink-500); min-width: 18px; text-align: center; line-height: 1.4; font-feature-settings: "tnum" 1; }
.dms-workorder-page .status-tab:hover { background: var(--ink-50); color: var(--ink-700); }
.dms-workorder-page .status-tab.is-active { background: var(--brand-50); color: var(--brand-700); font-weight: 600; border-color: var(--brand-200); }
.dms-workorder-page .status-tab.is-active .count { background: var(--brand-600); color: #fff; }
.dms-workorder-page .status-tab.is-active .dot { background: var(--brand-500); }

/* 状态颜色映射 - 新建/草稿 */
.dms-workorder-page .status-tab.tab-amber .dot { background: var(--amber-500); }
.dms-workorder-page .status-tab.tab-amber .count { background: var(--amber-50); color: var(--amber-700); }
.dms-workorder-page .status-tab.is-active.tab-amber .count { background: var(--amber-500); color: #fff; }

/* 已派工/已接单/处理中/驳回重做 - 蓝色系 */
.dms-workorder-page .status-tab.tab-blue .dot { background: var(--blue-500); }
.dms-workorder-page .status-tab.tab-blue .count { background: var(--blue-50); color: var(--blue-700); }
.dms-workorder-page .status-tab.is-active.tab-blue .count { background: var(--blue-500); color: #fff; }

/* 已完成/已验收 - 绿色系 */
.dms-workorder-page .status-tab.tab-green .dot { background: var(--green-500); }
.dms-workorder-page .status-tab.tab-green .count { background: var(--green-50); color: var(--green-700); }
.dms-workorder-page .status-tab.is-active.tab-green .count { background: var(--green-500); color: #fff; }

/* 已撤销 - 灰色系 */
.dms-workorder-page .status-tab.tab-gray .dot { background: var(--ink-400); }
.dms-workorder-page .status-tab.tab-gray .count { background: var(--ink-100); color: var(--ink-500); }
.dms-workorder-page .status-tab.is-active.tab-gray .count { background: var(--ink-400); color: #fff; }

/* ===== Tip Pill ===== */
.dms-workorder-page .tip-pill { display: inline-flex; align-items: center; gap: 5px; height: 30px; padding: 0 10px; background: #fffaf0; border: 1px solid #fde68a; color: #92400e; border-radius: 999px; font-size: 13px; font-weight: 500; cursor: pointer; transition: all .15s var(--ease-out); flex-shrink: 0; white-space: nowrap; }
.dms-workorder-page .tip-pill:hover { background: var(--amber-50); border-color: var(--amber-500); color: #7c2d12; }
.dms-workorder-page .tip-pill .el-icon { font-size: 14px; color: var(--amber-700); }

/* ===== Status Help Dialog Content ===== */
.status-help-content {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 10px;
}
.status-help-content h4 {
  margin: 20px 0 12px 0;
  color: #303133;
  font-weight: 600;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}
.status-help-content h4:first-child {
  margin-top: 0;
}
.status-help-content .status-flow {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 8px;
  justify-content: flex-start;
}
.status-help-content .flow-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.status-help-content .flow-arrow {
  color: #909399;
  font-size: 16px;
}
.highlight-card {
  border-radius: 8px;
  padding: 16px;
  border: 1px solid;
}
.highlight-success { background-color: #f0f9ff; border-color: #b3e19d; }
.highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.highlight-success .highlight-card-title { color: #67c23a; }
.highlight-danger .highlight-card-title { color: #f56c6c; }
.highlight-primary .highlight-card-title { color: #409eff; }
.highlight-warning .highlight-card-title { color: #e6a23c; }
.highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
</style>

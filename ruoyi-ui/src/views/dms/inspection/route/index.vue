<template>
  <div class="app-container dms-inspection-route-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>路线名称</label>
          <div class="control">
            <el-input v-model="queryParams.routeName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>周期类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.cycleType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in dms_inspection_cycle" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
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

    <!-- ===== Table Section ===== -->
    <div class="surface">
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:inspection:route:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:inspection:route:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:inspection:route:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_inspection_route_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="路线名称" prop="routeName" key="routeName" :width="colWidth('routeName', 160)" resizable show-overflow-tooltip v-if="columns.routeName.visible" />
          <el-table-column label="路线编码" prop="routeCode" key="routeCode" :width="colWidth('routeCode', 180)" resizable show-overflow-tooltip v-if="columns.routeCode.visible" />
          <el-table-column label="责任部门" prop="deptName" key="deptName" :width="colWidth('deptName', 120)" resizable v-if="columns.deptName.visible" />
          <el-table-column label="周期" prop="cycleType" key="cycleType" :width="colWidth('cycleType', 140)" resizable align="center" v-if="columns.cycleType.visible">
            <template #default="scope">
              <span class="badge amber">{{ cycleTypeLabel(scope.row.cycleType) }}</span>
              <span style="margin-left: 4px; font-weight: 600; color: var(--ink-700);">{{ scope.row.cycleValue }}</span>
              <span style="color: var(--ink-400); font-size: 12px; margin-left: 2px;">天</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:inspection:route:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:inspection:route:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改弹窗 -->
    <el-dialog v-model="open" width="1080px" append-to-body draggable class="rd-dialog route-form-dialog" top="5vh">
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
              <el-col :span="12"><el-form-item label="责任部门" prop="deptId">
                <el-input v-model="form.deptName" readonly placeholder="请选择责任部门" style="width: 100%" @click="openDeptPicker">
                  <template #append><el-button icon="Search" @click="openDeptPicker" /></template>
                  <template #suffix><el-icon v-if="form.deptName" class="clear-icon" @click.stop="clearDept"><CircleClose /></el-icon></template>
                </el-input>
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
                <el-select v-model="form.cycleType" placeholder="请选择" style="width: 100%" @change="onCycleTypeChange">
                  <el-option v-for="d in dms_inspection_cycle" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="周期值" prop="cycleValue">
                <div class="cycle-composite">
                  <el-input-number v-model="form.cycleValue" :min="1" controls-position="right" style="width: 140px" :disabled="form.cycleType !== '3'" />
                  <span class="cycle-unit">天</span>
                  <span class="cycle-suffix" v-if="form.cycleType !== '3'">每 {{ form.cycleValue || '?' }} 天 执行一次</span>
                  <span class="cycle-suffix cycle-suffix-custom" v-else>自定义周期，请输入天数</span>
                </div>
              </el-form-item></el-col>
            </el-row>
            <el-form-item label="巡检设备" prop="equipmentIdList">
              <div style="width: 100%">
                <el-input v-model="equipmentDisplay" readonly placeholder="请选择设备（可多选，按选择顺序为巡检顺序）" style="width: 100%" @click="openEquipmentPicker">
                  <template #append><el-button icon="Search" @click="openEquipmentPicker" /></template>
                  <template #suffix><el-icon v-if="equipmentDisplay" class="clear-icon" @click.stop="clearEquipment"><CircleClose /></el-icon></template>
                </el-input>
                <div v-if="form.equipmentIdList && form.equipmentIdList.length > 0" style="margin-top: 8px">
                  <el-tag v-for="(eid, idx) in form.equipmentIdList" :key="eid" closable size="small" @close="removeEquipment(idx)" style="margin: 2px 4px 2px 0">
                    <span style="font-weight: 600; color: var(--brand-600)">{{ idx + 1 }}.</span> {{ getEquipmentLabel(eid) }}
                  </el-tag>
                </div>
              </div>
            </el-form-item>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>通用点检项（所有设备共用）</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
            <el-table :data="form.commonItems" border style="width: 100%" size="small" @header-dragend="onHeaderDragEnd">
              <el-table-column label="序号" width="60" align="center"><template #default="scope">{{ scope.$index + 1 }}</template></el-table-column>
              <el-table-column label="点检项目名称" min-width="360"><template #default="scope"><el-input v-model="scope.row.item" placeholder="如：设备周围卫生、安全防护装置完好" size="small" /></template></el-table-column>
              <el-table-column label="操作" width="70" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="removeCommonItem(scope.$index)">删除</el-button></template></el-table-column>
            </el-table>
            <el-button type="primary" plain icon="Plus" size="small" style="margin-top: 8px" @click="addCommonItem">添加通用项</el-button>
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
                  <el-table-column label="序号" width="60" align="center"><template #default="scope">{{ scope.$index + 1 }}</template></el-table-column>
                  <el-table-column label="点检项目名称" min-width="360"><template #default="scope"><el-input v-model="scope.row.item" :placeholder="'如：检查' + dev.equipmentName + '运行温度是否正常'" size="small" /></template></el-table-column>
                  <el-table-column label="操作" width="70" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="removeDeviceItem(di, scope.$index)">删除</el-button></template></el-table-column>
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

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewOpen" width="1080px" append-to-body draggable class="rd-dialog route-detail-dialog" top="5vh">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/></svg></div>
          <span class="rd-detail-header-title">巡检路线详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.routeCode">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">路线编码：{{ viewForm.routeCode }}</span>
          </div>
          <div class="rd-detail-header-tags" v-if="viewForm.status != null">
            <el-tag :type="viewForm.status === '0' ? 'success' : 'info'" effect="dark" size="small">{{ viewForm.status === '0' ? '正常' : '停用' }}</el-tag>
          </div>
        </div>
      </template>
      <div v-loading="viewLoading" class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>路线名称</span><div class="rd-value">{{ viewForm.routeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>路线编码</span><div class="rd-value">{{ viewForm.routeCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>责任部门</span><div class="rd-value">{{ viewForm.deptName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>状态</span><div class="rd-value"><el-tag :type="viewForm.status === '0' ? 'success' : 'info'" size="small">{{ viewForm.status === '0' ? '正常' : '停用' }}</el-tag></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/></svg></span>巡检配置</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>周期类型</span><div class="rd-value"><el-tag type="warning" size="small" effect="plain">{{ cycleTypeLabel(viewForm.cycleType) }}</el-tag></div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>周期值</span><div class="rd-value"><span style="font-weight: 600;">{{ viewForm.cycleValue || '-' }}</span> <span style="color: var(--ink-500); font-size: 13px;">天</span></div></div>
            </div>
            <div style="margin-top: 12px">
              <div class="rd-label" style="margin-bottom: 8px"><span class="rd-label-dot"></span>巡检设备（按顺序）</div>
              <div v-if="viewEquipmentList.length > 0" style="display: flex; flex-wrap: wrap; gap: 6px">
                <el-tag v-for="(eq, idx) in viewEquipmentList" :key="idx" type="info" size="small" effect="plain">
                  <span style="font-weight: 600; color: var(--brand-600)">{{ idx + 1 }}.</span> {{ eq.label }}
                </el-tag>
              </div>
              <span v-else style="color: #999">暂未配置巡检设备</span>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewItems.common.length > 0">
          <div class="rd-card-header" @click="toggleCard('v3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>通用点检项</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v3">
            <el-table :data="viewItems.common" border size="small">
              <el-table-column label="序号" type="index" width="60" align="center" />
              <el-table-column label="点检项目名称" prop="item" min-width="200" />
            </el-table>
          </div>
        </section>
        <section class="rd-card" v-for="(dg, di) in viewItems.devices" :key="'vd' + di">
          <div class="rd-card-header" @click="toggleCard('vd' + di)">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg></span>{{ dg.equipmentName }}</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards['vd' + di] }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards['vd' + di]">
            <el-table :data="dg.items" border size="small">
              <el-table-column label="序号" type="index" width="60" align="center" />
              <el-table-column label="点检项目名称" prop="item" min-width="200" />
            </el-table>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v4')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>系统信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v4">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>创建人</span><div class="rd-value">{{ viewForm.createBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>创建时间</span><div class="rd-value">{{ viewForm.createTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>更新人</span><div class="rd-value">{{ viewForm.updateBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>更新时间</span><div class="rd-value">{{ viewForm.updateTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label"><span class="rd-label-dot"></span>备注</span><div class="rd-value">{{ viewForm.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 部门选择弹窗 -->
    <dept-picker ref="deptPickerRef" title="选择责任部门" :disabled-ids="[100]" @confirm="onDeptPickerConfirm" />

    <!-- 设备选择弹窗（多选） -->
    <equipment-picker ref="equipmentPickerRef" title="选择巡检设备" :multiple="true" @confirm="onEquipmentPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="巡检路线业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务流程图</h4>
        <div class="status-flow">
          <div class="flow-item"><el-tag type="primary">创建巡检路线</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="info">选择巡检设备</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="warning">配置点检项</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="success">生成点检任务</el-tag></div>
        </div>
        <h4>二、点检项配置说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="通用点检项">所有巡检设备共用的检查项，如设备周围卫生、安全防护装置等</el-descriptions-item>
          <el-descriptions-item label="设备专属点检项">每台设备单独配置的检查项，如特定设备的温度、压力、振动等</el-descriptions-item>
          <el-descriptions-item label="点检项目名称">只需填写检查项名称，巡检执行时由点检人以文本形式填写实际巡检内容</el-descriptions-item>
        </el-descriptions>
        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12"><div class="highlight-card highlight-primary"><div class="highlight-card-title">巡检顺序</div><div class="highlight-card-body">设备选择顺序即为<strong>巡检执行顺序</strong>，点检人按此顺序依次巡检各设备</div></div></el-col>
          <el-col :span="12"><div class="highlight-card highlight-warning"><div class="highlight-card-title">周期配置</div><div class="highlight-card-body">设置巡检周期类型和值，系统据此<strong>自动生成点检任务</strong></div></div></el-col>
        </el-row>
        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>创建路线：</strong>填写路线名称、选择责任部门，设置巡检周期类型和值</el-timeline-item>
          <el-timeline-item type="info" :hollow="true"><strong>选择设备：</strong>多选巡检设备，选择顺序为巡检执行顺序</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>配置点检项：</strong>配置通用点检项（所有设备共用）和每台设备的专属点检项</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>保存路线：</strong>保存后在点检任务模块中创建任务，点检人按路线配置执行点检</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsInspectionRoute">
import { CircleClose } from '@element-plus/icons-vue'
import { listRoute, getRoute, addRoute, updateRoute, delRoute, countRouteByStatus } from '@/api/dms/inspection'
import { listEquipment } from '@/api/dms/equipment'
import DeptPicker from '@/components/DeptPicker/index.vue'
import EquipmentPicker from '@/components/EquipmentPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c3","c2","c1","c0","v1","v2","v3","v4"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_inspection_route_index')
const { dms_inspection_cycle } = proxy.useDict('dms_inspection_cycle')

const list = ref([])
const equipmentOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const activeDeviceTabs = ref([])
const showStatusHelp = ref(false)
const viewOpen = ref(false)
const viewForm = ref({})
const viewLoading = ref(false)
const viewItems = ref({ common: [], devices: [] })
const viewEquipmentList = ref([])
// 状态页签
const activeStatusTab = ref('all')
const statusCounts = ref({})
const statusTabList = ref([
  { label: '正常', value: '0' },
  { label: '停用', value: '1' }
])
function statusTabClass(status) {
  const map = { '0': 'tab-green', '1': 'tab-gray' }
  return map[status] || 'tab-gray'
}
function handleStatusTabClick(status) {
  activeStatusTab.value = status
  if (status === 'all') {
    queryParams.value.status = undefined
  } else {
    queryParams.value.status = status
  }
  handleQuery()
}
function loadStatusCounts() {
  countRouteByStatus().then(res => {
    statusCounts.value = res.data || {}
  }).catch(() => {})
}

const defaultColumns = {
  routeName: { label: '路线名称', visible: true },
  routeCode: { label: '路线编码', visible: true },
  deptName: { label: '责任部门', visible: true },
  cycleType: { label: '周期', visible: true },
  status: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_inspection_route_columns')
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
  if (queryParams.value.routeName) count++
  if (queryParams.value.cycleType !== undefined && queryParams.value.cycleType !== null && queryParams.value.cycleType !== '') count++
  if (queryParams.value.status !== undefined && queryParams.value.status !== null && queryParams.value.status !== '') count++
  return count
})

function cycleTypeLabel(val) {
  const item = dms_inspection_cycle.value.find(d => d.value == val)
  return item ? item.label : '-'
}

/** 周期类型联动默认值 */
const cycleDefaultMap = { '0': 1, '1': 7, '2': 30 }
function onCycleTypeChange(val) {
  if (val !== '3' && cycleDefaultMap[val] !== undefined) {
    form.value.cycleValue = cycleDefaultMap[val]
  }
}

/** 已选设备的显示文本 */
const equipmentDisplay = computed(() => {
  if (!form.value.equipmentIdList || form.value.equipmentIdList.length === 0) return ''
  return form.value.equipmentIdList.map(eid => getEquipmentLabel(eid)).join('、')
})
function getEquipmentLabel(eid) {
  const eq = equipmentOptions.value.find(e => e.equipmentId === eid)
  return eq ? eq.equipmentCode + ' - ' + eq.equipmentName : '设备' + eid
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, routeName: undefined, cycleType: undefined, status: undefined },
  rules: {
    routeName: [{ required: true, message: '路线名称不能为空', trigger: 'blur' }],
    cycleType: [{ required: true, message: '周期类型不能为空', trigger: 'change' }],
    cycleValue: [{ required: true, message: '周期值不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listRoute(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function getEquipmentOptions() { listEquipment({ pageNum: 1, pageSize: 9999 }).then(res => { equipmentOptions.value = res.rows }) }
/** 打开部门选择弹窗 */
function openDeptPicker() { proxy.$refs.deptPickerRef.open(form.value.deptId) }
/** 部门选择确认回调 */
function onDeptPickerConfirm(dept) { form.value.deptId = dept.deptId; form.value.deptName = dept.deptName }
/** 清除部门 */
function clearDept() { form.value.deptId = undefined; form.value.deptName = undefined }

/** 打开设备选择弹窗（多选） */
function openEquipmentPicker() {
  const selectedRows = (form.value.equipmentIdList || []).map(eid => {
    const eq = equipmentOptions.value.find(e => e.equipmentId === eid)
    return eq || { equipmentId: eid, equipmentCode: '', equipmentName: '设备' + eid }
  })
  proxy.$refs.equipmentPickerRef.open(null, selectedRows)
}
/** 设备多选确认回调 */
function onEquipmentPickerConfirm(equipmentList) {
  form.value.equipmentIdList = equipmentList.map(e => e.equipmentId)
  syncDeviceItems()
}
/** 清除全部已选设备 */
function clearEquipment() {
  form.value.equipmentIdList = []
  form.value.deviceItems = []
  activeDeviceTabs.value = []
}
/** 移除单个已选设备 */
function removeEquipment(idx) {
  form.value.equipmentIdList.splice(idx, 1)
  syncDeviceItems()
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
        if (Array.isArray(parsed)) {
          form.value.commonItems = parsed
          form.value.deviceItems = []
        } else {
          form.value.commonItems = parsed.common || []
          form.value.deviceItems = parsed.devices || []
        }
      } catch (e) { form.value.commonItems = []; form.value.deviceItems = [] }
    }
    syncDeviceItems()
    open.value = true; title.value = '修改巡检路线'
  })
}

/** 查看详情 */
function handleView(row) {
  viewLoading.value = true
  viewOpen.value = true
  getRoute(row.routeId).then(res => {
    viewForm.value = res.data
    viewItems.value = { common: [], devices: [] }
    viewEquipmentList.value = []
    // 解析设备列表
    if (res.data.equipmentIds) {
      try {
        const eids = JSON.parse(res.data.equipmentIds)
        viewEquipmentList.value = eids.map(eid => {
          const eq = equipmentOptions.value.find(e => e.equipmentId === eid)
          return { id: eid, label: eq ? eq.equipmentCode + ' - ' + eq.equipmentName : '设备' + eid }
        })
      } catch (e) {}
    }
    // 解析点检项
    if (res.data.inspectionItems) {
      try {
        const parsed = JSON.parse(res.data.inspectionItems)
        if (Array.isArray(parsed)) {
          viewItems.value = { common: parsed, devices: [] }
        } else {
          viewItems.value = { common: parsed.common || [], devices: parsed.devices || [] }
        }
      } catch (e) { viewItems.value = { common: [], devices: [] } }
    }
    // 系统信息为空时自动收缩
    collapsedCards.v4 = !viewForm.value.createBy && !viewForm.value.createTime && !viewForm.value.updateBy && !viewForm.value.updateTime && !viewForm.value.remark
    viewLoading.value = false
  }).catch(() => { viewLoading.value = false })
}

// 设备选择变化时，同步设备明细分组
function syncDeviceItems() {
  const selectedIds = form.value.equipmentIdList || []
  form.value.deviceItems = form.value.deviceItems.filter(d => selectedIds.includes(d.equipmentId))
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
  activeDeviceTabs.value = form.value.deviceItems.map(d => d.equipmentId)
}

function addCommonItem() { form.value.commonItems.push({ item: '' }) }
function removeCommonItem(index) { form.value.commonItems.splice(index, 1) }
function addDeviceItem(di) { form.value.deviceItems[di].items.push({ item: '' }) }
function removeDeviceItem(di, ii) { form.value.deviceItems[di].items.splice(ii, 1) }

function submitForm() {
  proxy.$refs['routeRef'].validate(valid => {
    if (valid) {
      form.value.equipmentIds = form.value.equipmentIdList && form.value.equipmentIdList.length > 0 ? JSON.stringify(form.value.equipmentIdList) : undefined
      const inspectionData = {
        common: form.value.commonItems || [],
        devices: form.value.deviceItems || []
      }
      const hasItems = inspectionData.common.length > 0 || inspectionData.devices.some(d => d.items.length > 0)
      form.value.inspectionItems = hasItems ? JSON.stringify(inspectionData) : undefined
      if (form.value.routeId != undefined) { updateRoute(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList(); loadStatusCounts() }) }
      else { addRoute(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList(); loadStatusCounts() }) }
    }
  })
}
function handleDelete(row) { const routeIds = row.routeId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delRoute(routeIds)).then(() => { getList(); loadStatusCounts(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
getEquipmentOptions()
getList()
loadStatusCounts()
</script>

<style scoped>
.dms-inspection-route-page {
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
.dms-inspection-route-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.dms-inspection-route-page .filter-card { padding:14px 20px 16px; }
.dms-inspection-route-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-inspection-route-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-inspection-route-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-inspection-route-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-inspection-route-page .filter-card .field { display:flex; flex-direction:column; gap:4px; }
.dms-inspection-route-page .filter-card .field > label { font-size:13px; font-weight:500; color:var(--ink-500); }
.dms-inspection-route-page .filter-card .field .control { width:100%; }
.dms-inspection-route-page .filter-card .field .control .el-input, .dms-inspection-route-page .filter-card .field .control .el-select { width:100%; }
.dms-inspection-route-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-inspection-route-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-inspection-route-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-inspection-route-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.dms-inspection-route-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.dms-inspection-route-page .tabs-track::-webkit-scrollbar { display:none; }
.dms-inspection-route-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.dms-inspection-route-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.dms-inspection-route-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.dms-inspection-route-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.dms-inspection-route-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.dms-inspection-route-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.dms-inspection-route-page .status-tab.is-active .dot { background:var(--brand-500); }
.dms-inspection-route-page .status-tab.tab-green .dot { background:var(--green-500); }
.dms-inspection-route-page .status-tab.tab-green .count { background:var(--green-50); color:var(--green-700); }
.dms-inspection-route-page .status-tab.is-active.tab-green .count { background:var(--green-500); color:#fff; }
.dms-inspection-route-page .status-tab.tab-gray .dot { background:var(--ink-400); }
.dms-inspection-route-page .status-tab.tab-gray .count { background:var(--ink-100); color:var(--ink-500); }
.dms-inspection-route-page .status-tab.is-active.tab-gray .count { background:var(--ink-400); color:#fff; }
.dms-inspection-route-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.dms-inspection-route-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.dms-inspection-route-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }
.dms-inspection-route-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:10px 16px; border-bottom:1px solid var(--ink-200); }
.dms-inspection-route-page .toolbar .left { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.dms-inspection-route-page .toolbar .right { display:flex; align-items:center; gap:8px; }
.dms-inspection-route-page .btn-soft { display:inline-flex; align-items:center; gap:4px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; cursor:pointer; transition:all .15s var(--ease-out); border:1px solid transparent; background:transparent; color:var(--ink-700); }
.dms-inspection-route-page .btn-soft.is-outline { border-color:var(--ink-300); background:#fff; }
.dms-inspection-route-page .btn-soft.is-outline:hover { border-color:var(--brand-400); color:var(--brand-600); background:var(--brand-50); }
.dms-inspection-route-page .btn-soft.is-danger-outline { border-color:#fecaca; background:var(--red-50); color:var(--red-700); }
.dms-inspection-route-page .btn-soft.is-danger-outline:hover { background:var(--red-500); color:#fff; border-color:var(--red-500); }
.dms-inspection-route-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-inspection-route-page .table-wrap { overflow-x:auto; }
.dms-inspection-route-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-inspection-route-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-inspection-route-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-inspection-route-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-inspection-route-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-inspection-route-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-inspection-route-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-inspection-route-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-inspection-route-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-inspection-route-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-inspection-route-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
/* 详情弹窗：头部标签容器（与工单详情页一致，紧跟副标题居左） */
.rd-detail-header-tags { display: flex; align-items: center; gap: 6px; }
.dms-inspection-route-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-inspection-route-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.dms-inspection-route-page .badge.amber .dot { background:var(--amber-500); }
.dms-inspection-route-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.dms-inspection-route-page .badge.green .dot { background:var(--green-500); }
.dms-inspection-route-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.dms-inspection-route-page .badge.gray .dot { background:var(--ink-400); }
.dms-inspection-route-page :deep(.col-action) { padding: 6px 4px !important; }
.dms-inspection-route-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.dms-inspection-route-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.dms-inspection-route-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.dms-inspection-route-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }
@media (max-width:1100px) { .dms-inspection-route-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-inspection-route-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-inspection-route-page .toolbar { flex-wrap:wrap; gap:10px; } }
.dms-inspection-route-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-inspection-route-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-inspection-route-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-inspection-route-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-inspection-route-page .pagination-container :deep(.el-pagination .btn-prev), .dms-inspection-route-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-inspection-route-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

.clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.clear-icon:hover { color: #909399; }

/* 周期组合控件 */
.cycle-composite { display: flex; align-items: center; gap: 0; width: 100%; }
.cycle-composite :deep(.el-input-number) { border-radius: 6px 0 0 6px; }
.cycle-composite :deep(.el-input-number .el-input__wrapper) { border-radius: 6px 0 0 6px; box-shadow: 0 0 0 1px var(--ink-200) inset !important; }
.cycle-composite :deep(.el-input-number.is-focused .el-input__wrapper) { box-shadow: 0 0 0 1px var(--brand-500) inset !important; }
.cycle-unit { display: inline-flex; align-items: center; justify-content: center; height: 32px; padding: 0 12px; background: var(--ink-50); border: 1px solid var(--ink-200); border-left: 0; border-radius: 0 6px 6px 0; font-size: 14px; font-weight: 500; color: var(--ink-700); }
.cycle-suffix { margin-left: 12px; font-size: 13px; color: var(--ink-500); font-weight: 500; white-space: nowrap; }
.cycle-suffix::before { content: ''; display: inline-block; width: 6px; height: 6px; border-radius: 50%; background: var(--brand-500); margin-right: 6px; vertical-align: middle; }
.cycle-suffix-custom { color: var(--amber-700); }
.cycle-suffix-custom::before { background: var(--amber-500); }

/* 详情弹窗滚动 */
:deep(.el-input.is-disabled .el-input__inner) { cursor: pointer; }
</style>

<style>
/* 本页弹窗 body 滚动（仅作用于本页两个弹窗，rd-* 规范类统一由全局 detail-page.scss 提供，页面不得重定义） */
.route-form-dialog .el-dialog__body { max-height: 72vh; overflow-y: auto; }
.route-detail-dialog .el-dialog__body { max-height: 68vh; overflow-y: auto; }

.status-help-content { max-height: 500px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-help-content .status-flow { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; padding: 16px; background-color: #f5f7fa; border-radius: 8px; margin-bottom: 8px; }
.status-help-content .flow-item { display: flex; align-items: center; gap: 8px; }
.status-help-content .flow-arrow { color: #909399; font-size: 16px; }
.highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
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
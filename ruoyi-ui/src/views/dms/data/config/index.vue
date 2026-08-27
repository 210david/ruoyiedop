<template>
  <div class="app-container dms-data-config-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>设备名称</label>
          <div class="control">
            <el-input v-model="queryParams.equipmentName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>采集模式</label>
          <div class="control is-select">
            <el-select v-model="queryParams.collectMode" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in dms_collect_mode" :key="d.value" :label="d.label" :value="d.value" />
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
      <!-- Status Tabs + Tip Pill -->
      <div class="status-tabs">
        <div class="tabs-track"></div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:data:config:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:data:config:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:data:config:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_data_config_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="设备名称" prop="equipmentName" key="equipmentName" :width="colWidth('equipmentName', 200)" resizable show-overflow-tooltip v-if="columns.equipmentName.visible" />
          <el-table-column label="采集模式" prop="collectMode" key="collectMode" :width="colWidth('collectMode', 120)" resizable align="center" v-if="columns.collectMode.visible">
            <template #default="scope">
              <span class="badge violet">{{ collectModeLabel(scope.row.collectMode) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="参数1" prop="param1Name" key="param1Name" :width="colWidth('param1Name', 140)" resizable v-if="columns.param1Name.visible">
            <template #default="scope">{{ scope.row.param1Name }} ({{ scope.row.param1Unit }})</template>
          </el-table-column>
          <el-table-column label="参数2" prop="param2Name" key="param2Name" :width="colWidth('param2Name', 140)" resizable v-if="columns.param2Name.visible">
            <template #default="scope">{{ scope.row.param2Name }} ({{ scope.row.param2Unit }})</template>
          </el-table-column>
          <el-table-column label="参数3" prop="param3Name" key="param3Name" :width="colWidth('param3Name', 140)" resizable v-if="columns.param3Name.visible">
            <template #default="scope">{{ scope.row.param3Name }} ({{ scope.row.param3Unit }})</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:data:config:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:data:config:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="1000px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="4" width="16" height="16" rx="2"/><rect x="9" y="9" width="6" height="6"/><line x1="9" y1="1" x2="9" y2="4"/><line x1="15" y1="1" x2="15" y2="4"/><line x1="9" y1="20" x2="9" y2="23"/><line x1="15" y1="20" x2="15" y2="23"/><line x1="20" y1="9" x2="23" y2="9"/><line x1="20" y1="14" x2="23" y2="14"/><line x1="1" y1="9" x2="4" y2="9"/><line x1="1" y1="14" x2="4" y2="14"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="configRef" :model="form" :rules="rules" label-width="90px">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="4" width="16" height="16" rx="2"/><rect x="9" y="9" width="6" height="6"/><line x1="9" y1="1" x2="9" y2="4"/><line x1="15" y1="1" x2="15" y2="4"/><line x1="9" y1="20" x2="9" y2="23"/><line x1="15" y1="20" x2="15" y2="23"/><line x1="20" y1="9" x2="23" y2="9"/><line x1="20" y1="14" x2="23" y2="14"/><line x1="1" y1="9" x2="4" y2="9"/><line x1="1" y1="14" x2="4" y2="14"/></svg></span>设备信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
        <el-row>
          <el-col :span="12"><el-form-item label="关联设备" prop="equipmentId">
            <el-input :model-value="form.equipmentCode" readonly placeholder="请选择设备" style="width: 100%" @click="openEquipmentPicker">
              <template #suffix><el-icon style="cursor: pointer" @click.stop="openEquipmentPicker"><Search /></el-icon></template>
            </el-input>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备名称" prop="equipmentName"><el-input v-model="form.equipmentName" placeholder="自动带出" disabled /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="采集模式" prop="collectMode">
            <el-select v-model="form.collectMode" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dms_collect_mode" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="8"><el-form-item label="状态采集" prop="collectStatus"><el-switch v-model="form.collectStatus" active-value="1" inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="工时采集" prop="collectHours"><el-switch v-model="form.collectHours" active-value="1" inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="产量采集" prop="collectCount"><el-switch v-model="form.collectCount" active-value="1" inactive-value="0" /></el-form-item></el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></span>自定义参数配置</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
        <el-row v-for="i in 3" :key="i">
          <el-col :span="6"><el-form-item :label="'参数'+i+'名称'" :prop="'param'+i+'Name'"><el-input v-model="form['param'+i+'Name']" placeholder="如：温度" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item :label="'单位'" :prop="'param'+i+'Unit'"><el-input v-model="form['param'+i+'Unit']" placeholder="如：℃" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item :label="'告警下限'" :prop="'param'+i+'AlarmMin'"><el-input-number v-model="form['param'+i+'AlarmMin']" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item :label="'告警上限'" :prop="'param'+i+'AlarmMax'"><el-input-number v-model="form['param'+i+'AlarmMax']" controls-position="right" style="width: 100%" /></el-form-item></el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
        <el-row>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item>
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
    <el-dialog v-model="viewOpen" width="936px" append-to-body draggable class="rd-dialog" top="5vh">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="4" width="16" height="16" rx="2"/><rect x="9" y="9" width="6" height="6"/><line x1="9" y1="1" x2="9" y2="4"/><line x1="15" y1="1" x2="15" y2="4"/><line x1="9" y1="20" x2="9" y2="23"/><line x1="15" y1="20" x2="15" y2="23"/><line x1="20" y1="9" x2="23" y2="9"/><line x1="20" y1="14" x2="23" y2="14"/><line x1="1" y1="9" x2="4" y2="9"/><line x1="1" y1="14" x2="4" y2="14"/></svg></div>
          <span class="rd-detail-header-title">采集配置详情</span>
          <div class="rd-detail-header-tags">
            <el-tag :type="viewForm.status === '0' ? 'success' : 'info'" effect="dark">{{ viewForm.status === '0' ? '正常' : '停用' }}</el-tag>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_equipment')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg></span>设备信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_equipment }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_equipment">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">设备编号</span><div class="rd-value">{{ viewForm.equipmentCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">设备名称</span><div class="rd-value">{{ viewForm.equipmentName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">采集模式</span><div class="rd-value"><dict-tag :options="dms_collect_mode" :value="viewForm.collectMode" /></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value">{{ viewForm.status === '0' ? '正常' : '停用' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态采集</span><div class="rd-value">{{ viewForm.collectStatus === '1' ? '开启' : '关闭' }}</div></div>
              <div class="rd-item"><span class="rd-label">工时采集</span><div class="rd-value">{{ viewForm.collectHours === '1' ? '开启' : '关闭' }}</div></div>
              <div class="rd-item"><span class="rd-label">产量采集</span><div class="rd-value">{{ viewForm.collectCount === '1' ? '开启' : '关闭' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_params')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></span>自定义参数配置</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_params }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_params">
            <el-table :data="viewParams" border size="small">
              <el-table-column label="参数" width="80" align="center"><template #default="scope">参数{{ scope.$index + 1 }}</template></el-table-column>
              <el-table-column label="参数名称" prop="name" min-width="140"><template #default="scope">{{ scope.row.name || '-' }}</template></el-table-column>
              <el-table-column label="单位" prop="unit" width="100" align="center"><template #default="scope">{{ scope.row.unit || '-' }}</template></el-table-column>
              <el-table-column label="告警下限" prop="alarmMin" width="110" align="center"><template #default="scope">{{ scope.row.alarmMin ?? '-' }}</template></el-table-column>
              <el-table-column label="告警上限" prop="alarmMax" width="110" align="center"><template #default="scope">{{ scope.row.alarmMax ?? '-' }}</template></el-table-column>
            </el-table>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_other')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_other">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewForm.remark || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 设备选择弹框 -->
    <equipment-picker ref="equipmentPickerRef" title="选择采集设备" @confirm="onEquipmentPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="采集配置业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务流程图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="primary">选择设备</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">配置采集模式</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">设置参数与告警</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">数据自动采集</el-tag>
          </div>
        </div>

        <h4>二、采集模式说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="手动采集">由操作人员定期手动录入设备运行数据</el-descriptions-item>
          <el-descriptions-item label="自动采集">通过IoT网关或PLC接口自动实时采集设备数据，无需人工干预</el-descriptions-item>
          <el-descriptions-item label="混合模式">同时支持自动采集和手动补录，适用于部分参数无法自动采集的场景</el-descriptions-item>
        </el-descriptions>

        <h4>三、采集开关说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="状态采集">采集设备运行状态（运行/停机/故障），用于设备稼动率分析</el-descriptions-item>
          <el-descriptions-item label="工时采集">采集设备运行工时，用于维护周期计算和成本核算</el-descriptions-item>
          <el-descriptions-item label="产量采集">采集设备产出数量，用于产能分析和效率统计</el-descriptions-item>
        </el-descriptions>

        <h4>四、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">自定义参数告警</div>
              <div class="highlight-card-body">每个自定义参数可设置<strong>告警上下限</strong>，采集数据超出阈值时自动触发告警</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">参数单位</div>
              <div class="highlight-card-body">为数值型参数设置单位（如℃、MPa、rpm），便于<strong>数据展示和分析</strong></div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">状态管理</div>
              <div class="highlight-card-body">配置状态为「正常」或「停用」，停用后该设备<strong>停止数据采集</strong></div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">最多3个自定义参数</div>
              <div class="highlight-card-body">每台设备最多配置3个自定义采集参数，满足大多数设备的数据采集需求</div>
            </div>
          </el-col>
        </el-row>

        <h4>五、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>选择设备：</strong>从设备列表中选择需要配置采集的设备
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>配置采集模式：</strong>选择手动、自动或混合采集模式，开启状态/工时/产量采集开关</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>设置参数：</strong>配置自定义参数名称、单位和告警上下限阈值</el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>保存配置：</strong>保存后系统按配置自动采集设备数据，超阈值时触发告警</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsDataConfig">
import { listConfig, getConfig, addConfig, updateConfig, delConfig } from '@/api/dms/data'
import { listEquipment } from '@/api/dms/equipment'
import EquipmentPicker from '@/components/EquipmentPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c2","c1","c0","v_equipment","v_params","v_other"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_data_config_index')
const { dms_collect_mode } = proxy.useDict('dms_collect_mode')

const list = ref([])
const equipmentOptions = ref([])
const equipmentPickerRef = ref()
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const showStatusHelp = ref(false)
const viewForm = ref({})
const viewParams = computed(() => {
  const f = viewForm.value || {}
  return [
    { name: f.param1Name, unit: f.param1Unit, alarmMin: f.param1AlarmMin, alarmMax: f.param1AlarmMax },
    { name: f.param2Name, unit: f.param2Unit, alarmMin: f.param2AlarmMin, alarmMax: f.param2AlarmMax },
    { name: f.param3Name, unit: f.param3Unit, alarmMin: f.param3AlarmMin, alarmMax: f.param3AlarmMax }
  ]
})

const defaultColumns = {
  equipmentName: { label: '设备名称', visible: true },
  collectMode: { label: '采集模式', visible: true },
  param1Name: { label: '参数1', visible: true },
  param2Name: { label: '参数2', visible: true },
  param3Name: { label: '参数3', visible: true },
  status: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_data_config_columns')
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
  if (queryParams.value.equipmentName) count++
  if (queryParams.value.collectMode !== undefined && queryParams.value.collectMode !== null && queryParams.value.collectMode !== '') count++
  if (queryParams.value.status !== undefined && queryParams.value.status !== null && queryParams.value.status !== '') count++
  return count
})

function collectModeLabel(val) {
  const item = dms_collect_mode.value.find(d => d.value == val)
  return item ? item.label : '-'
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, equipmentName: undefined, collectMode: undefined, status: undefined },
  rules: {
    equipmentId: [{ required: true, message: '请选择关联设备', trigger: 'change' }],
    collectMode: [{ required: true, message: '采集模式不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listConfig(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function getEquipmentOptions() { listEquipment({ pageNum: 1, pageSize: 9999 }).then(res => { equipmentOptions.value = res.rows }).catch(() => { equipmentOptions.value = [] }) }
/** 打开设备选择弹框 */
function openEquipmentPicker() { equipmentPickerRef.value.open(form.value.equipmentId) }
/** 设备选择确认：显示设备编号，自动带出名称 */
function onEquipmentPickerConfirm(equipment) {
  form.value.equipmentId = equipment.equipmentId
  form.value.equipmentCode = equipment.equipmentCode
  form.value.equipmentName = equipment.equipmentName
}
/** 根据设备ID回显编号（编辑/详情） */
function resolveEquipmentCode(equipmentId) {
  const equipment = equipmentOptions.value.find(e => e.equipmentId === equipmentId)
  return equipment ? equipment.equipmentCode : ''
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.configId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { equipmentId: undefined, equipmentCode: undefined, equipmentName: undefined, collectMode: '0', collectStatus: '1', collectHours: '1', collectCount: '0', param1Name: undefined, param1Unit: undefined, param1AlarmMin: undefined, param1AlarmMax: undefined, param2Name: undefined, param2Unit: undefined, param2AlarmMin: undefined, param2AlarmMax: undefined, param3Name: undefined, param3Unit: undefined, param3AlarmMin: undefined, param3AlarmMax: undefined, status: '0', remark: undefined }; proxy.resetForm('configRef') }
function handleAdd() { reset(); open.value = true; title.value = '新增采集配置' }
function handleUpdate(row) { reset(); getConfig(row.configId || ids.value[0]).then(res => { form.value = res.data; form.value.equipmentCode = resolveEquipmentCode(form.value.equipmentId); open.value = true; title.value = '修改采集配置' }) }
function handleView(row) { getConfig(row.configId).then(res => { viewForm.value = { ...res.data, equipmentCode: resolveEquipmentCode(res.data.equipmentId) }; viewOpen.value = true }) }
function submitForm() {
  proxy.$refs['configRef'].validate(valid => {
    if (valid) {
      const submitData = { ...form.value, equipmentCode: undefined }
      if (submitData.configId != undefined) { updateConfig(submitData).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addConfig(submitData).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const configIds = row.configId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delConfig(configIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
getEquipmentOptions()
getList()
</script>

<style scoped>
.dms-data-config-page {
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
.dms-data-config-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.dms-data-config-page .filter-card { padding:14px 20px 16px; }
.dms-data-config-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-data-config-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-data-config-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-data-config-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-data-config-page .filter-card .field { display:flex; flex-direction:column; gap:4px; }
.dms-data-config-page .filter-card .field > label { font-size:13px; font-weight:500; color:var(--ink-500); }
.dms-data-config-page .filter-card .field .control { width:100%; }
.dms-data-config-page .filter-card .field .control .el-input, .dms-data-config-page .filter-card .field .control .el-select { width:100%; }
.dms-data-config-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-data-config-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-data-config-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-data-config-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.dms-data-config-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.dms-data-config-page .tabs-track::-webkit-scrollbar { display:none; }
.dms-data-config-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.dms-data-config-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.dms-data-config-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }
.dms-data-config-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:10px 16px; border-bottom:1px solid var(--ink-200); }
.dms-data-config-page .toolbar .left { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.dms-data-config-page .toolbar .right { display:flex; align-items:center; gap:8px; }
.dms-data-config-page .toolbar-divider { width:1px; height:20px; background:var(--ink-200); margin:0 4px; }
.dms-data-config-page .btn-soft { display:inline-flex; align-items:center; gap:4px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; cursor:pointer; transition:all .15s var(--ease-out); border:1px solid transparent; background:transparent; color:var(--ink-700); }
.dms-data-config-page .btn-soft.is-outline { border-color:var(--ink-300); background:#fff; }
.dms-data-config-page .btn-soft.is-outline:hover { border-color:var(--brand-400); color:var(--brand-600); background:var(--brand-50); }
.dms-data-config-page .btn-soft.is-danger-outline { border-color:#fecaca; background:var(--red-50); color:var(--red-700); }
.dms-data-config-page .btn-soft.is-danger-outline:hover { background:var(--red-500); color:#fff; border-color:var(--red-500); }
.dms-data-config-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-data-config-page .table-wrap { overflow-x:auto; }
.dms-data-config-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-data-config-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-data-config-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-data-config-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-data-config-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-data-config-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-data-config-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-data-config-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-data-config-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-data-config-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-data-config-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-data-config-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-data-config-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.dms-data-config-page .badge.amber .dot { background:var(--amber-500); }
.dms-data-config-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.dms-data-config-page .badge.blue .dot { background:var(--blue-500); }
.dms-data-config-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.dms-data-config-page .badge.green .dot { background:var(--green-500); }
.dms-data-config-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.dms-data-config-page .badge.red .dot { background:var(--red-500); }
.dms-data-config-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.dms-data-config-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.dms-data-config-page .badge.gray .dot { background:var(--ink-400); }
/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行 */
.dms-data-config-page :deep(.col-action) { padding: 6px 4px !important; }
.dms-data-config-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.dms-data-config-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.dms-data-config-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.dms-data-config-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }

@media (max-width:1100px) { .dms-data-config-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-data-config-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-data-config-page .toolbar { flex-wrap:wrap; gap:10px; } }
.dms-data-config-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-data-config-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-data-config-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-data-config-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-data-config-page .pagination-container :deep(.el-pagination .btn-prev), .dms-data-config-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-data-config-page .pagination-container :deep(.el-pagination .btn-prev:hover), .dms-data-config-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.dms-data-config-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

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

<template>
  <div class="app-container bi-metric-page">
    <!-- ===== 筛选条件卡片 ===== -->
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
          <label>指标编码</label>
          <div class="control">
            <el-input v-model="queryParams.metricCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>指标名称</label>
          <div class="control">
            <el-input v-model="queryParams.metricName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Document /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>所属域</label>
          <div class="control is-select">
            <el-select v-model="queryParams.domain" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in domainOptions" :key="d.value" :label="d.label" :value="d.value" />
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
        <div class="field" v-show="showAdvanced">
          <label>统计频率</label>
          <div class="control is-select">
            <el-select v-model="queryParams.freq" placeholder="全部" clearable @change="handleQuery">
              <el-option label="日" value="day" />
              <el-option label="月" value="month" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>责任人</label>
          <div class="control">
            <el-input v-model="queryParams.owner" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>计算实现</label>
          <div class="control">
            <el-input v-model="queryParams.calcRef" placeholder="如 MkStatsProvider" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Cpu /></el-icon></template>
            </el-input>
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

    <!-- ===== 数据列表卡片 ===== -->
    <div class="surface">
      <!-- 状态页签 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button class="status-tab tab-approved" :class="{ 'is-active': activeStatusTab === '0' }" @click="handleStatusTabClick('0')">
            <span class="dot"></span><span>正常</span><span class="count">{{ statusCounts['0'] || 0 }}</span>
          </button>
          <button class="status-tab tab-void" :class="{ 'is-active': activeStatusTab === '1' }" @click="handleStatusTabClick('1')">
            <span class="dot"></span><span>停用</span><span class="count">{{ statusCounts['1'] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>指标管理说明</span>
        </button>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['bi:metric:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['bi:metric:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['bi:metric:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['bi:metric:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
          <button type="button" class="btn-soft is-outline" @click="openCheckLog" v-hasPermi="['bi:metric:check']">
            <el-icon><DocumentChecked /></el-icon> 对账日志
          </button>
          <button type="button" class="btn-soft is-outline" @click="openAlertList" v-hasPermi="['bi:metric:check']">
            <el-icon><Bell /></el-icon> 预警记录
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="bi_metric_columns" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="metricList" border
          @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="指标编码" prop="metricCode" key="metricCode" :width="colWidth('metricCode', 170)" resizable v-if="columns.metricCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.metricCode }}</span></template>
          </el-table-column>
          <el-table-column label="指标名称" prop="metricName" key="metricName" :min-width="colWidth('metricName', 150)" resizable show-overflow-tooltip v-if="columns.metricName.visible" />
          <el-table-column label="所属域" prop="domain" key="domain" :width="colWidth('domain', 90)" resizable align="center" v-if="columns.domain.visible">
            <template #default="scope">
              <span class="badge violet">{{ domainLabel(scope.row.domain) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="口径定义" prop="caliberDesc" key="caliberDesc" :min-width="colWidth('caliberDesc', 220)" resizable show-overflow-tooltip v-if="columns.caliberDesc.visible" />
          <el-table-column label="计算实现" prop="calcRef" key="calcRef" :width="colWidth('calcRef', 150)" resizable show-overflow-tooltip v-if="columns.calcRef.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.calcRef }}</span></template>
          </el-table-column>
          <el-table-column label="频率" prop="freq" key="freq" :width="colWidth('freq', 70)" resizable align="center" v-if="columns.freq.visible">
            <template #default="scope">{{ scope.row.freq === 'month' ? '月' : '日' }}</template>
          </el-table-column>
          <el-table-column label="阈值" prop="thresholdRed" key="thresholdRed" :width="colWidth('thresholdRed', 110)" resizable align="center" v-if="columns.thresholdRed.visible">
            <template #default="scope">
              <span v-if="scope.row.thresholdRed != null" class="col-mono">{{ scope.row.thresholdDir === 'lt' ? '＜' : '＞' }} {{ scope.row.thresholdRed }}</span>
              <span v-else class="text-muted">未配置</span>
            </template>
          </el-table-column>
          <el-table-column label="责任人" prop="owner" key="owner" :width="colWidth('owner', 100)" resizable align="center" v-if="columns.owner.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['bi:metric:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['bi:metric:edit']">修改</el-button>
                <el-button link type="primary" icon="Clock" @click="openChangeLog(scope.row)" v-hasPermi="['bi:metric:query']">留痕</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 新增/修改弹窗 ===== -->
    <el-dialog v-model="open" width="912px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="metricRef" :model="form" :rules="rules" label-width="100px">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基础信息</div></div>
          <div class="rd-card-body">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="指标编码" prop="metricCode">
                  <el-input v-model="form.metricCode" placeholder="如 MK_WIN_RATE" :disabled="form.id != null" maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="指标名称" prop="metricName">
                  <el-input v-model="form.metricName" placeholder="请输入指标名称" maxlength="100" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="所属域" prop="domain">
                  <el-select v-model="form.domain" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in domainOptions" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="计算实现" prop="calcRef">
                  <el-input v-model="form.calcRef" placeholder="如 MkStatsProvider" maxlength="100" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="口径定义" prop="caliberDesc">
                  <el-input v-model="form.caliberDesc" type="textarea" :rows="3" placeholder="指标口径的最终解释以本字段为准，变更将留痕" maxlength="500" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="刷新方式" prop="refreshType">
                  <el-select v-model="form.refreshType" style="width: 100%">
                    <el-option label="快照（每日任务）" value="snapshot" />
                    <el-option label="实时" value="realtime" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="统计频率" prop="freq">
                  <el-select v-model="form.freq" style="width: 100%">
                    <el-option label="日" value="day" />
                    <el-option label="月" value="month" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="责任人" prop="owner">
                  <el-input v-model="form.owner" readonly placeholder="请选择责任人" style="width: 100%" @click="openUserPicker">
                    <template #append>
                      <el-button v-if="form.owner" icon="CircleClose" @click.stop="clearOwner" />
                      <el-button v-else icon="Search" @click="openUserPicker" />
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status">
                    <el-radio label="0">正常</el-radio>
                    <el-radio label="1">停用</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="备注" prop="remark">
                  <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>阈值预警配置</div></div>
          <div class="rd-card-body">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="阈值方向" prop="thresholdDir">
                  <el-select v-model="form.thresholdDir" placeholder="配置阈值后必选" style="width: 100%" clearable>
                    <el-option label="低于预警（lt）" value="lt" />
                    <el-option label="超限预警（gt）" value="gt" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="红线阈值" prop="thresholdRed">
                  <el-input-number v-model="form.thresholdRed" :precision="2" :controls="false" placeholder="选填" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="黄线阈值" prop="thresholdYellow">
                  <el-input-number v-model="form.thresholdYellow" :precision="2" :controls="false" placeholder="选填" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <div class="threshold-hint">
              <el-icon><InfoFilled /></el-icon>
              阈值每日 01:10 快照后自动匹配：红线触发推送紧急消息、黄线推送重要消息；同级别 24 小时内不重复推送；数值回落或阈值移除后预警自动恢复。
            </div>
          </div>
        </section>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 人员选择弹框（责任人） -->
    <user-picker ref="userPickerRef" title="选择责任人" @confirm="onUserPickerConfirm" />

    <!-- ===== 指标详情弹窗（只读） ===== -->
    <el-dialog v-model="viewOpen" width="912px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">指标详情</div>
            <div class="rd-detail-header-sub">
              <span class="rd-detail-header-no">{{ viewForm.metricCode || '' }}</span>
              <span class="badge" :class="badgeClass(viewForm.status)"><span class="dot"></span>{{ viewForm.status === '0' ? '正常' : '停用' }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page" v-loading="viewLoading">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基础信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">指标编码</span><div class="rd-value col-mono">{{ viewForm.metricCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">指标名称</span><div class="rd-value">{{ viewForm.metricName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">所属域</span><div class="rd-value"><span class="badge violet">{{ domainLabel(viewForm.domain) }}</span></div></div>
              <div class="rd-item"><span class="rd-label">计算实现</span><div class="rd-value col-mono">{{ viewForm.calcRef || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">刷新方式</span><div class="rd-value">{{ viewForm.refreshType === 'realtime' ? '实时' : '快照（每日任务）' }}</div></div>
              <div class="rd-item"><span class="rd-label">统计频率</span><div class="rd-value">{{ viewForm.freq === 'month' ? '月' : '日' }}</div></div>
              <div class="rd-item"><span class="rd-label">责任人</span><div class="rd-value">{{ viewForm.owner || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">口径定义</span><div class="rd-value">{{ viewForm.caliberDesc || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewForm.remark || '暂无备注' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>阈值预警配置</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">阈值方向</span><div class="rd-value">{{ viewForm.thresholdDir ? (viewForm.thresholdDir === 'lt' ? '低于预警（lt）' : '超限预警（gt）') : '未配置' }}</div></div>
              <div class="rd-item"><span class="rd-label">红线阈值</span><div class="rd-value col-mono">{{ viewForm.thresholdRed != null ? viewForm.thresholdRed : '未配置' }}</div></div>
              <div class="rd-item"><span class="rd-label">黄线阈值</span><div class="rd-value col-mono">{{ viewForm.thresholdYellow != null ? viewForm.thresholdYellow : '未配置' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 口径变更留痕弹窗 ===== -->
    <el-dialog v-model="logOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div>
          <span class="rd-detail-header-title">口径变更留痕</span>
          <div class="rd-detail-header-sub"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ logMetricName }}</span></div>
        </div>
      </template>
      <el-table v-loading="logLoading" :data="logList" size="small" class="app-table">
        <el-table-column label="变更类型" width="90" align="center">
          <template #default="scope">
            <span class="badge" :class="changeTypeBadge(scope.row.changeType)">{{ scope.row.changeType }}</span>
          </template>
        </el-table-column>
        <el-table-column label="字段" prop="fieldName" width="130" align="center" />
        <el-table-column label="旧值" prop="oldValue" min-width="150" align="center" show-overflow-tooltip />
        <el-table-column label="新值" prop="newValue" min-width="150" align="center" show-overflow-tooltip />
        <el-table-column label="操作人" prop="changeBy" width="90" align="center" />
        <el-table-column label="变更时间" prop="changeTime" width="160" align="center" />
      </el-table>
    </el-dialog>

    <!-- ===== 对账日志弹窗 ===== -->
    <el-dialog v-model="checkOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">对账日志（快照值 vs 实时值）</span>
        </div>
      </template>
      <div class="dialog-filter-bar">
        <el-input v-model="checkQuery.metricCode" placeholder="指标编码" clearable style="width: 180px" @keyup.enter="getCheckLog" />
        <el-select v-model="checkQuery.checkStatus" placeholder="对账结果" clearable style="width: 130px">
          <el-option label="一致" value="0" />
          <el-option label="不一致" value="1" />
        </el-select>
        <el-button type="primary" icon="Search" @click="getCheckLog">查询</el-button>
      </div>
      <el-table v-loading="checkLoading" :data="checkList" size="small" class="app-table">
        <el-table-column label="指标编码" prop="metricCode" width="150" align="center">
          <template #default="scope"><span class="col-mono">{{ scope.row.metricCode }}</span></template>
        </el-table-column>
        <el-table-column label="对账日期" prop="checkDate" width="110" align="center">
          <template #default="scope">{{ parseTime(scope.row.checkDate, '{y}-{m}-{d}') }}</template>
        </el-table-column>
        <el-table-column label="快照值" prop="snapshotValue" width="110" align="center" />
        <el-table-column label="实时值" prop="realtimeValue" width="110" align="center" />
        <el-table-column label="差异" prop="diffValue" width="100" align="center" />
        <el-table-column label="结果" width="90" align="center">
          <template #default="scope">
            <span class="badge" :class="scope.row.checkStatus === '0' ? 'green' : 'red'">
              <span class="dot"></span>{{ scope.row.checkStatus === '0' ? '一致' : '不一致' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="记录时间" prop="createTime" width="160" align="center" />
      </el-table>
      <pagination v-show="checkTotal > 0" :total="checkTotal" v-model:page="checkQuery.pageNum" v-model:limit="checkQuery.pageSize" @pagination="getCheckLog" />
    </el-dialog>

    <!-- ===== 预警记录弹窗 ===== -->
    <el-dialog v-model="alertOpen" width="980px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg></div>
          <span class="rd-detail-header-title">阈值预警记录</span>
          <div class="rd-detail-header-sub"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">每日 01:10 快照后自动匹配</span></div>
        </div>
      </template>
      <div class="dialog-filter-bar">
        <el-input v-model="alertQuery.metricCode" placeholder="指标编码" clearable style="width: 180px" @keyup.enter="getAlertList" />
        <el-select v-model="alertQuery.alertLevel" placeholder="预警级别" clearable style="width: 130px">
          <el-option label="红线" value="2" />
          <el-option label="黄线" value="1" />
        </el-select>
        <el-select v-model="alertQuery.pushStatus" placeholder="推送状态" clearable style="width: 130px">
          <el-option label="未推送" value="0" />
          <el-option label="已推送" value="1" />
          <el-option label="抑制未推" value="2" />
        </el-select>
        <el-button type="primary" icon="Search" @click="getAlertList">查询</el-button>
      </div>
      <el-table v-loading="alertLoading" :data="alertList" size="small" class="app-table">
        <el-table-column label="数据日期" prop="metricDate" width="100" align="center">
          <template #default="scope">{{ parseTime(scope.row.metricDate, '{y}-{m}-{d}') }}</template>
        </el-table-column>
        <el-table-column label="指标" min-width="140" align="center" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.metricName || scope.row.metricCode }}</template>
        </el-table-column>
        <el-table-column label="级别" width="80" align="center">
          <template #default="scope">
            <span class="badge" :class="scope.row.alertLevel === '2' ? 'red' : 'amber'">
              <span class="dot"></span>{{ scope.row.alertLevel === '2' ? '红线' : '黄线' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="触发值" prop="metricValue" width="100" align="center" />
        <el-table-column label="阈值" prop="thresholdValue" width="100" align="center" />
        <el-table-column label="推送" width="90" align="center">
          <template #default="scope">
            <span class="badge" :class="pushBadge(scope.row.pushStatus)">{{ pushLabel(scope.row.pushStatus) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="推送时间" prop="pushTime" width="150" align="center" />
        <el-table-column label="次数" prop="pushCount" width="60" align="center" />
        <el-table-column label="未推原因" prop="pushRemark" min-width="150" align="center" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <span class="badge" :class="scope.row.status === '0' ? 'red' : 'green'">
              <span class="dot"></span>{{ scope.row.status === '0' ? '未恢复' : '已恢复' }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="alertTotal > 0" :total="alertTotal" v-model:page="alertQuery.pageNum" v-model:limit="alertQuery.pageSize" @pagination="getAlertList" />
    </el-dialog>

    <!-- ===== 指标管理说明弹窗（规范 list-page-style-guide 3.3 status-help-content） ===== -->
    <el-dialog v-model="showHelp" title="指标管理说明" width="640px" append-to-body>
      <div class="status-help-content">
        <h4>一、口径与留痕说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="指标字典">全部 22 项决策指标的口径定义以本字典为准，跨模块统计口径统一可追溯</el-descriptions-item>
          <el-descriptions-item label="口径变更留痕">口径字段的每一次变更都会逐字段留痕，行内「留痕」按钮可查变更前后值与操作人</el-descriptions-item>
          <el-descriptions-item label="计算实现">各指标由对应模块 StatsProvider 计算（列表「计算实现」列），bi 模块不写任何业务逻辑</el-descriptions-item>
        </el-descriptions>
        <h4>二、快照与对账</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>每日快照（01:00）：</strong>定时任务按字典口径落各指标快照，同日重跑幂等不重复</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>自动对账（01:30）：</strong>对快照值与实时值逐项比对，结果写入对账日志</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>不一致核查：</strong>对账不一致项在「对账日志」弹窗中留痕（含快照值/实时值），供数据质量核查</el-timeline-item>
        </el-timeline>
        <h4>三、阈值预警推送</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>配置阈值：</strong>编辑弹窗中配置阈值方向与红线/黄线，保存后每日 01:10 快照自动匹配触发</el-timeline-item>
          <el-timeline-item type="danger" :hollow="true"><strong>消息推送：</strong>红线触发推送紧急消息、黄线推送重要消息；同级别 24 小时内不重复推送，黄升红立即推送</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>自动恢复：</strong>数值回落至阈值内或阈值移除后，预警自动关闭并记录恢复时间；驾驶舱顶部横幅即时生效</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Metric">
import { listMetric, getMetric, addMetric, updateMetric, delMetric, listChangeLog, listCheckLog, listAlert } from '@/api/bi/metric'
import UserPicker from '@/components/UserPicker/index.vue'
import { ArrowDown, Search, Document, User, Cpu, Filter, WarningFilled, Edit, Delete, Download, DocumentChecked, Bell, InfoFilled, View } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('bi_metric_index')

const metricList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const showHelp = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const open = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0 })

// 指标详情（只读）
const viewOpen = ref(false)
const viewLoading = ref(false)
const viewForm = ref({})

// 口径变更留痕
const logOpen = ref(false)
const logLoading = ref(false)
const logMetricName = ref('')
const logList = ref([])

// 对账日志
const checkOpen = ref(false)
const checkLoading = ref(false)
const checkList = ref([])
const checkTotal = ref(0)
const checkQuery = ref({ pageNum: 1, pageSize: 10, metricCode: undefined, checkStatus: undefined })

// 预警记录
const alertOpen = ref(false)
const alertLoading = ref(false)
const alertList = ref([])
const alertTotal = ref(0)
const alertQuery = ref({ pageNum: 1, pageSize: 10, metricCode: undefined, alertLevel: undefined, pushStatus: undefined })

// 域选项（与指标字典 domain 枚举一致）
const domainOptions = [
  { value: 'sales', label: '营销' },
  { value: 'bid', label: '投标' },
  { value: 'production', label: '生产' },
  { value: 'quality', label: '质量' },
  { value: 'purchase', label: '采购' },
  { value: 'inventory', label: '库存' },
  { value: 'equipment', label: '设备' },
  { value: 'safety', label: '安全' },
  { value: 'hr', label: '人力' },
  { value: 'synthesis', label: '综合' }
]

// 列显隐配置
const defaultColumns = {
  metricCode: { label: '指标编码', visible: true },
  metricName: { label: '指标名称', visible: true },
  domain: { label: '所属域', visible: true },
  caliberDesc: { label: '口径定义', visible: true },
  calcRef: { label: '计算实现', visible: true },
  freq: { label: '频率', visible: true },
  thresholdRed: { label: '阈值', visible: true },
  owner: { label: '责任人', visible: true },
  status: { label: '状态', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('bi_metric_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, metricCode: undefined, metricName: undefined, domain: undefined, status: undefined, freq: undefined, owner: undefined, calcRef: undefined },
  rules: {
    metricCode: [{ required: true, message: '指标编码不能为空', trigger: 'blur' }],
    metricName: [{ required: true, message: '指标名称不能为空', trigger: 'blur' }],
    domain: [{ required: true, message: '所属域不能为空', trigger: 'change' }],
    caliberDesc: [{ required: true, message: '口径定义不能为空', trigger: 'blur' }],
    refreshType: [{ required: true, message: '刷新方式不能为空', trigger: 'change' }],
    freq: [{ required: true, message: '统计频率不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 已选筛选条件数
const activeFilterCount = computed(() => {
  const q = queryParams.value
  return ['metricCode', 'metricName', 'domain', 'status', 'freq', 'owner', 'calcRef'].filter(k => q[k] !== undefined && q[k] !== '').length
})

function domainLabel(domain) {
  const d = domainOptions.find(o => o.value === domain)
  return d ? d.label : domain
}

function badgeClass(status) {
  return status === '0' ? 'green' : 'gray'
}

function changeTypeBadge(type) {
  const map = { INSERT: 'green', UPDATE: 'amber', DELETE: 'red' }
  return map[type] || 'gray'
}

function pushBadge(pushStatus) {
  const map = { '0': 'red', '1': 'green', '2': 'gray' }
  return map[pushStatus] || 'gray'
}

function pushLabel(pushStatus) {
  const map = { '0': '未推送', '1': '已推送', '2': '抑制' }
  return map[pushStatus] || '-'
}

function getList() {
  loading.value = true
  listMetric(queryParams.value).then(res => {
    metricList.value = res.rows
    total.value = res.total
    loading.value = false
    loadStatusCounts()
    applySavedWidths()
  })
}

function loadStatusCounts() {
  listMetric({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, metricCode: undefined, metricName: undefined, domain: undefined, status: undefined, freq: undefined, owner: undefined, calcRef: undefined, params: {} }
  activeStatusTab.value = 'all'
  getList()
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.status = status === 'all' ? undefined : status
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    id: null, metricCode: null, metricName: null, domain: null, caliberDesc: null,
    calcRef: null, refreshType: 'snapshot', freq: 'day', thresholdRed: null,
    thresholdYellow: null, thresholdDir: null, owner: null, status: '0', remark: null
  }
  proxy.resetForm('metricRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增指标'
}

function handleView(row) {
  viewOpen.value = true
  viewLoading.value = true
  viewForm.value = {}
  getMetric(row.id).then(res => {
    viewForm.value = res.data
    viewLoading.value = false
  }).catch(() => { viewLoading.value = false })
}

// ===== 责任人人员选择（UserPicker 弹框组件，规范 picker-component-style-guide） =====
const userPickerRef = ref(null)

/** 打开人员选择弹窗 */
function openUserPicker() {
  userPickerRef.value.open()
}

/** 人员选择确认回调（owner 存姓名） */
function onUserPickerConfirm(user) {
  form.value.owner = user.nickName
}

/** 清除责任人 */
function clearOwner() {
  form.value.owner = undefined
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getMetric(id).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改指标（口径变更将逐字段留痕）'
  })
}

function submitForm() {
  proxy.$refs['metricRef'].validate(valid => {
    if (!valid) return
    if (form.value.id != null) {
      updateMetric(form.value).then(res => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList()
      })
    } else {
      addMetric(form.value).then(res => {
        proxy.$modal.msgSuccess('新增成功')
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const delIds = row.id ? [row.id] : ids.value
  proxy.$modal.confirm('是否确认删除所选指标？口径删除将留痕且不可恢复展示。').then(() => {
    return delMetric(delIds.join(','))
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('/bi/metric/export', { ...queryParams.value }, `metric_${Date.now()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

function openChangeLog(row) {
  logMetricName.value = row.metricName + '（' + row.metricCode + '）'
  logOpen.value = true
  logLoading.value = true
  listChangeLog(row.metricCode).then(res => {
    logList.value = res.data
    logLoading.value = false
  })
}

function openCheckLog() {
  checkOpen.value = true
  checkQuery.value.pageNum = 1
  getCheckLog()
}

function getCheckLog() {
  checkLoading.value = true
  listCheckLog(checkQuery.value).then(res => {
    checkList.value = res.rows
    checkTotal.value = res.total
    checkLoading.value = false
  })
}

function openAlertList() {
  alertOpen.value = true
  alertQuery.value.pageNum = 1
  getAlertList()
}

function getAlertList() {
  alertLoading.value = true
  listAlert(alertQuery.value).then(res => {
    alertList.value = res.rows
    alertTotal.value = res.total
    alertLoading.value = false
  })
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.bi-metric-page {
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

/* ===== Surface Card ===== */
.bi-metric-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.bi-metric-page .filter-card { padding:14px 20px 16px; }
.bi-metric-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.bi-metric-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.bi-metric-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.bi-metric-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.bi-metric-page .filter-card .adv-link:hover { color:var(--brand-600); }
.bi-metric-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.bi-metric-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.bi-metric-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.bi-metric-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.bi-metric-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.bi-metric-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.bi-metric-page .field { display:flex; flex-direction:column; gap:6px; }
.bi-metric-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.bi-metric-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.bi-metric-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.bi-metric-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.bi-metric-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.bi-metric-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.bi-metric-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.bi-metric-page .field .control :deep(.el-select) { width:100%; }
.bi-metric-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.bi-metric-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }

/* ===== Status Tabs ===== */
.bi-metric-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.bi-metric-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.bi-metric-page .tabs-track::-webkit-scrollbar { display:none; }
.bi-metric-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.bi-metric-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.bi-metric-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.bi-metric-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.bi-metric-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.bi-metric-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.bi-metric-page .status-tab.is-active .dot { background:var(--brand-500); }
.bi-metric-page .status-tab.tab-approved .dot { background:var(--green-500); }
.bi-metric-page .status-tab.tab-approved .count { background:var(--green-50); color:var(--green-700); }
.bi-metric-page .status-tab.is-active.tab-approved .count { background:var(--green-500); color:#fff; }
.bi-metric-page .status-tab.tab-void .dot { background:var(--ink-400); }

/* ===== Tip Pill ===== */
.bi-metric-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.bi-metric-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.bi-metric-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }

/* ===== Toolbar ===== */
.bi-metric-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.bi-metric-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.bi-metric-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.bi-metric-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Buttons ===== */
.bi-metric-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.bi-metric-page .btn-soft .el-icon { font-size:14px; }
.bi-metric-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.bi-metric-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.bi-metric-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.bi-metric-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.bi-metric-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.bi-metric-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }

/* ===== Table ===== */
.bi-metric-page .table-wrap { overflow-x:auto; }
.bi-metric-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.bi-metric-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.bi-metric-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.bi-metric-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.bi-metric-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.bi-metric-page .app-table :deep(.el-table__body td) { padding:13px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.bi-metric-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.bi-metric-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.bi-metric-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.bi-metric-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.bi-metric-page .app-table .text-muted { color:var(--ink-400); font-size:13px; }

/* ===== Badges ===== */
.bi-metric-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.bi-metric-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.bi-metric-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.bi-metric-page .badge.amber .dot { background:var(--amber-500); }
.bi-metric-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.bi-metric-page .badge.green .dot { background:var(--green-500); }
.bi-metric-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.bi-metric-page .badge.red .dot { background:var(--red-500); }
.bi-metric-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.bi-metric-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.bi-metric-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Responsive ===== */
@media (max-width:1100px) { .bi-metric-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .bi-metric-page .filter-card .filter-bar { grid-template-columns:1fr; } .bi-metric-page .toolbar { flex-wrap:wrap; gap:10px; } .bi-metric-page .status-tabs { padding:6px 8px; } }

/* ===== 操作列按钮对齐 ===== */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== 弹窗内筛选条 ===== */
.dialog-filter-bar { display:flex; gap:10px; align-items:center; margin-bottom:12px; }

/* ===== 阈值提示条 ===== */
.threshold-hint { display:flex; align-items:flex-start; gap:8px; margin:4px 0 0 4px; padding:10px 14px; background:var(--amber-50); border:1px solid #fde68a; border-radius:var(--r-md); font-size:13px; color:var(--amber-700); line-height:1.6; }
.threshold-hint .el-icon { margin-top:2px; flex-shrink:0; }

/* ===== 说明弹窗 ===== */
/* ===== 业务操作说明弹窗（规范 3.3） ===== */
.status-help-content { max-height: 500px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-help-content .el-timeline { padding-left: 4px; }
</style>

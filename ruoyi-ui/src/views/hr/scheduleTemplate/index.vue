<template>
  <div class="app-container hr-schedule-template-page">
    <!-- 筛选条件 -->
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
          <label>模板名称</label>
          <div class="control">
            <el-input v-model="queryParams.templateName" placeholder="请输入模板名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>模板编码</label>
          <div class="control">
            <el-input v-model="queryParams.templateCode" placeholder="请输入模板编码" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>轮转方向</label>
          <div class="control is-select">
            <el-select v-model="queryParams.rotationType" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正向轮转" value="0" />
              <el-option label="反向轮转" value="1" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="启用" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control is-select">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>模板说明</label>
          <div class="control">
            <el-input v-model="queryParams.description" placeholder="请输入模板说明" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- 列表 -->
    <div class="surface">
      <!-- 状态标签栏 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button class="status-tab" :class="['tab-green', { 'is-active': activeStatusTab === '0' }]" @click="handleStatusTabClick('0')">
            <span class="dot"></span><span>启用</span><span class="count">{{ statusCounts['0'] || 0 }}</span>
          </button>
          <button class="status-tab" :class="['tab-void', { 'is-active': activeStatusTab === '1' }]" @click="handleStatusTabClick('1')">
            <span class="dot"></span><span>停用</span><span class="count">{{ statusCounts['1'] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:scheduleTemplate:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:scheduleTemplate:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:scheduleTemplate:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:scheduleTemplate:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_schedule_template_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="templateList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="模板名称" prop="templateName" key="templateName" :min-width="colWidth('templateName', 200)" resizable align="center" show-overflow-tooltip v-if="columns.templateName.visible" />
          <el-table-column label="模板编码" prop="templateCode" key="templateCode" :width="colWidth('templateCode', 150)" resizable align="center" v-if="columns.templateCode.visible" />
          <el-table-column label="循环天数" prop="cycleDays" key="cycleDays" :width="colWidth('cycleDays', 100)" resizable align="center" sortable v-if="columns.cycleDays.visible" />
          <el-table-column label="班次序列" prop="shiftSequence" key="shiftSequence" :width="colWidth('shiftSequence', 320)" resizable align="center" v-if="columns.shiftSequence.visible">
            <template #default="scope">
              <div class="shift-sequence-preview">
                <span v-for="(item, idx) in parseSequence(scope.row.shiftSequence)" :key="idx" class="shift-chip" :class="item.shiftId > 0 ? 'shift-chip--work' : 'shift-chip--rest'">
                  {{ item.shiftName }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="轮转方向" prop="rotationType" key="rotationType" :width="colWidth('rotationType', 110)" resizable align="center" v-if="columns.rotationType.visible">
            <template #default="scope">
              <span class="badge violet">{{ scope.row.rotationType === '0' ? '正向' : '反向' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'">
                <span class="dot"></span>{{ scope.row.status === '0' ? '启用' : '停用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="说明" prop="description" key="description" :min-width="colWidth('description', 200)" resizable align="center" show-overflow-tooltip v-if="columns.description.visible" />
          <el-table-column label="创建人" prop="createBy" key="createBy" :width="colWidth('createBy', 120)" resizable align="center" v-if="columns.createBy.visible" />
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:scheduleTemplate:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:scheduleTemplate:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:scheduleTemplate:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <div class="rd-detail-header-main">
            <span class="rd-detail-header-title">{{ title }}</span>
          </div>
        </div>
      </template>
      <el-form ref="templateRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="模板名称" prop="templateName"><el-input v-model="form.templateName" placeholder="请输入模板名称" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="模板编码" prop="templateCode"><el-input v-model="form.templateCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="循环天数" prop="cycleDays"><el-input-number v-model="form.cycleDays" :min="1" :max="31" style="width: 100%" @change="onCycleDaysChange" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="轮转方向" prop="rotationType"><el-select v-model="form.rotationType" style="width: 100%"><el-option label="正向轮转" value="0" /><el-option label="反向轮转" value="1" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="状态" prop="status"><el-select v-model="form.status" style="width: 100%"><el-option label="启用" value="0" /><el-option label="停用" value="1" /></el-select></el-form-item></el-col>
                <el-col :span="24"><el-form-item label="模板说明" prop="description"><el-input v-model="form.description" placeholder="请输入模板说明（如：早班-晚班-休息 三班轮转）" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 班次序列配置 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>班次序列配置</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <div class="rd-section-desc">按天数顺序设置每天对应的班次，"休息"表示该天不排班。模板保存后，在排班管理中可使用此模板批量生成排班记录，系统将按此序列循环轮转。</div>
              <el-form-item label="班次序列" prop="shiftSequence" label-width="80px">
                <div class="sequence-builder">
                  <div class="sequence-row" v-for="(item, idx) in form.sequenceList" :key="idx">
                    <span class="sequence-day">第{{ idx + 1 }}天</span>
                    <el-select v-model="item.shiftId" placeholder="选择班次" style="width: 200px" @change="onSequenceShiftChange(idx)">
                      <el-option label="休息" :value="0" />
                      <el-option v-for="s in shiftOptions" :key="s.shiftId" :label="s.shiftName" :value="s.shiftId" />
                    </el-select>
                    <span class="sequence-name">{{ item.shiftName }}</span>
                  </div>
                </div>
              </el-form-item>
            </div>
          </section>

          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入备注" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">排班模板详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.templateCode">
              <span class="rd-detail-header-no">编码：{{ viewData.templateCode }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>模板名称</span><div class="rd-value">{{ viewData.templateName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>模板编码</span><div class="rd-value">{{ viewData.templateCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>循环天数</span><div class="rd-value">{{ viewData.cycleDays != null ? viewData.cycleDays + ' 天' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>轮转方向</span><div class="rd-value">{{ viewData.rotationType === '0' ? '正向轮转' : viewData.rotationType === '1' ? '反向轮转' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>状态</span><div class="rd-value"><span class="badge" :class="viewData.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ viewData.status === '0' ? '启用' : '停用' }}</span></div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>模板说明</span><div class="rd-value">{{ viewData.description || '-' }}</div></div>
            </div>
          </div>
        </section>

        <!-- 班次序列 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>班次序列</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c1">
            <div class="shift-sequence-detail">
              <div class="sequence-detail-item" v-for="(item, idx) in parseSequence(viewData.shiftSequence)" :key="idx">
                <span class="sequence-detail-day">第{{ idx + 1 }}天</span>
                <span class="shift-chip" :class="item.shiftId > 0 ? 'shift-chip--work' : 'shift-chip--rest'">{{ item.shiftName }}</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 其他信息 -->
        <section class="rd-card" v-if="viewData.remark || viewData.createBy || viewData.createTime || viewData.updateBy || viewData.updateTime">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c2">
            <div class="rd-grid">
              <div class="rd-item rd-item--full" v-if="viewData.remark"><span class="rd-label"><span class="rd-label-dot"></span>备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
              <div class="rd-item" v-if="viewData.createBy"><span class="rd-label"><span class="rd-label-dot"></span>创建人</span><div class="rd-value">{{ viewData.createBy }}</div></div>
              <div class="rd-item" v-if="viewData.createTime"><span class="rd-label"><span class="rd-label-dot"></span>创建时间</span><div class="rd-value">{{ viewData.createTime }}</div></div>
              <div class="rd-item" v-if="viewData.updateBy"><span class="rd-label"><span class="rd-label-dot"></span>更新人</span><div class="rd-value">{{ viewData.updateBy }}</div></div>
              <div class="rd-item" v-if="viewData.updateTime"><span class="rd-label"><span class="rd-label-dot"></span>更新时间</span><div class="rd-value">{{ viewData.updateTime }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 业务操作说明 -->
    <el-dialog v-model="showStatusHelp" title="排班模板业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、排班模板概述</h4>
        <div class="highlight-card highlight-info">
          <div class="highlight-card-title">什么是排班模板？</div>
          <div class="highlight-card-body">
            <strong>排班模板</strong>是排班管理的核心基础数据，用于定义周期性轮转的班次序列。通过模板可快速批量生成多天排班记录，避免逐日手动排班，极大提升排班效率。每个模板包含循环天数和对应天数的班次序列，系统按序列循环轮转生成排班。
          </div>
        </div>

        <h4>二、核心概念</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="模板编码">系统自动生成的唯一编码，遵循编码规则管理（前缀 RT + 4位流水号），无需手动填写</el-descriptions-item>
          <el-descriptions-item label="循环天数">一个完整轮转周期的天数（1~31天），如"做五休二"的循环天数为7天</el-descriptions-item>
          <el-descriptions-item label="班次序列">按天数顺序设置每天对应的班次，选择"休息"表示该天不排班</el-descriptions-item>
          <el-descriptions-item label="轮转方向">正向轮转从第1天开始按顺序执行；反向轮转从最后一天倒序执行</el-descriptions-item>
          <el-descriptions-item label="状态">启用状态的模板可在排班管理中使用；停用后不再出现在模板选择列表中</el-descriptions-item>
        </el-descriptions>

        <h4>三、典型应用场景</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">场景示例</div>
          <div class="highlight-card-body">
            <p>1. <strong>做五休二（5+2模式）：</strong>循环天数7天，序列为：早-早-早-早-早-休-休</p>
            <p>2. <strong>三班轮转（早-中-夜-休）：</strong>循环天数4天，序列为：早班-中班-夜班-休息</p>
            <p>3. <strong>白班常日：</strong>循环天数1天，序列为：白班（每天都是白班）</p>
            <p>4. <strong>两班倒：</strong>循环天数2天，序列为：早班-晚班</p>
          </div>
        </div>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建模板：</strong>点击「新增」，填写模板名称、循环天数，配置每天对应的班次序列，保存后编码自动生成
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>修改模板：</strong>在列表中点击「修改」，可调整班次序列和轮转方向。已用于排班的模板修改后不影响已生成的排班记录
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>应用模板：</strong>进入「排班管理」页面，选择员工和日期范围，选择模板进行批量排班，系统自动按序列循环生成排班记录
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>停用模板：</strong>不再使用的模板可设置为"停用"状态，停用后不会出现在排班管理的模板选择列表中
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>删除模板：</strong>点击「删除」可删除模板，删除为软删除不影响已生成的排班数据
          </el-timeline-item>
        </el-timeline>

        <h4>五、注意事项</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">使用提示</div>
          <div class="highlight-card-body">
            <p>• 模板编码由系统自动生成，不可手动修改</p>
            <p>• 班次序列中的"休息"表示该天不创建排班记录，系统自动识别为休息日</p>
            <p>• 修改循环天数后，班次序列会自动扩展或截断，新增天默认为"休息"</p>
            <p>• 建议在模板名称中体现排班模式（如"做五休二"、"三班轮转"等）便于识别</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HrScheduleTemplate">
import { listHrScheduleTemplate, getHrScheduleTemplate, addHrScheduleTemplate, updateHrScheduleTemplate, delHrScheduleTemplate } from '@/api/hr/hrScheduleTemplate'
import { listHrShift } from '@/api/hr/hrShift'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { ArrowDown, Search, Filter, RefreshLeft, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

/** 列宽拖拽持久化 */
const { tableRef, colWidth, onHeaderDragEnd, applySavedWidths } = useColumnResize('hr_schedule_template_index')

/** 详情弹窗卡片折叠 */
const { collapsedCards, toggleCard } = useDetailCard(['c0', 'c1', 'c2', 'e0', 'e1', 'e2'])

const templateList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const showStatusHelp = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const shiftOptions = ref([])
const dateRange = ref([])
const activeStatusTab = ref('all')

/** 列显隐配置 */
const defaultColumns = {
  templateName: { label: '模板名称', visible: true },
  templateCode: { label: '模板编码', visible: true },
  cycleDays: { label: '循环天数', visible: true },
  shiftSequence: { label: '班次序列', visible: true },
  rotationType: { label: '轮转方向', visible: true },
  status: { label: '状态', visible: true },
  description: { label: '说明', visible: true },
  createBy: { label: '创建人', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_schedule_template_columns')
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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, templateName: undefined, templateCode: undefined, rotationType: undefined, status: undefined, description: undefined, createBy: undefined },
  rules: {
    templateName: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }],
    cycleDays: [{ required: true, message: '循环天数不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.templateName) count++
  if (queryParams.value.templateCode) count++
  if (queryParams.value.rotationType) count++
  if (queryParams.value.status) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  if (queryParams.value.description) count++
  return count
})

const statusCounts = computed(() => {
  const counts = { all: templateList.value.length }
  templateList.value.forEach(item => {
    const key = item.status || '0'
    counts[key] = (counts[key] || 0) + 1
  })
  return counts
})

function parseSequence(json) {
  try { return JSON.parse(json) } catch (e) { return [] }
}

function getList() {
  loading.value = true
  proxy.addDateRange(queryParams.value, dateRange.value, 'CreateTime')
  listHrScheduleTemplate(queryParams.value).then(res => {
    templateList.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  activeStatusTab.value = 'all'
  getList()
}

function resetQuery() {
  queryParams.value.templateName = undefined
  queryParams.value.templateCode = undefined
  queryParams.value.rotationType = undefined
  queryParams.value.status = undefined
  queryParams.value.description = undefined
  dateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function handleStatusTabClick(tab) {
  activeStatusTab.value = tab
  if (tab === 'all') {
    queryParams.value.status = undefined
  } else {
    queryParams.value.status = tab
  }
  queryParams.value.pageNum = 1
  getList()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.templateId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function loadShiftOptions() {
  listHrShift({ pageNum: 1, pageSize: 100 }).then(res => { shiftOptions.value = res.rows || [] })
}

function reset() {
  form.value = {
    templateId: undefined, templateName: undefined, templateCode: undefined, cycleDays: 7,
    shiftSequence: undefined, sequenceList: [], rotationType: '0', description: undefined,
    status: '0', remark: undefined
  }
  proxy.resetForm('templateRef')
}

function onCycleDaysChange(val) {
  if (!form.value.sequenceList) form.value.sequenceList = []
  const current = form.value.sequenceList.length
  if (val > current) {
    for (let i = current; i < val; i++) {
      form.value.sequenceList.push({ shiftId: 0, shiftName: '休息', day: i + 1 })
    }
  } else if (val < current) {
    form.value.sequenceList = form.value.sequenceList.slice(0, val)
  }
}

function onSequenceShiftChange(idx) {
  const item = form.value.sequenceList[idx]
  if (item.shiftId === 0) {
    item.shiftName = '休息'
  } else {
    const s = shiftOptions.value.find(s => s.shiftId === item.shiftId)
    item.shiftName = s ? s.shiftName : ''
  }
}

function handleAdd() {
  reset()
  onCycleDaysChange(7)
  open.value = true
  title.value = '新增排班模板'
}

function handleUpdate(row) {
  reset()
  const templateId = row.templateId || ids.value[0]
  getHrScheduleTemplate(templateId).then(res => {
    form.value = res.data
    form.value.sequenceList = parseSequence(res.data.shiftSequence)
    if (!form.value.sequenceList || form.value.sequenceList.length === 0) {
      form.value.sequenceList = []
      onCycleDaysChange(form.value.cycleDays || 7)
    }
    open.value = true
    title.value = '修改排班模板'
  })
}

/** 查看 —— 使用独立查看弹窗 */
function handleView(row) {
  const templateId = row.templateId || ids.value[0]
  getHrScheduleTemplate(templateId).then(res => {
    viewData.value = res.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['templateRef'].validate(valid => {
    if (valid) {
      form.value.shiftSequence = JSON.stringify(form.value.sequenceList)
      if (form.value.templateId != undefined) {
        updateHrScheduleTemplate(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrScheduleTemplate(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const templateIds = row.templateId || ids.value
  proxy.$modal.confirm('是否确认删除选中的排班模板？').then(function() {
    return delHrScheduleTemplate(templateIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/scheduleTemplate/export', proxy.withExportIds({ ...queryParams.value }, ids.value, 'templateId'), `hrScheduleTemplate_${new Date().getTime()}.xlsx`)
}

function cancel() { open.value = false; reset() }

loadShiftOptions()
getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-schedule-template-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.hr-schedule-template-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-schedule-template-page .filter-card { padding:14px 20px 16px; }
.hr-schedule-template-page .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-schedule-template-page .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-schedule-template-page .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-schedule-template-page .adv-link { display:inline-flex; align-items:center; gap:4px; font-size:13px; color:var(--ink-500); cursor:pointer; user-select:none; transition:color .15s; }
.hr-schedule-template-page .adv-link:hover { color:var(--brand-600); }
.hr-schedule-template-page .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-schedule-template-page .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-schedule-template-page .filter-bar { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:12px 16px; }
.hr-schedule-template-page .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-schedule-template-page .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-schedule-template-page .filter-buttons { display:flex; gap:8px; }
.hr-schedule-template-page .field { display:flex; flex-direction:column; gap:6px; }
.hr-schedule-template-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.hr-schedule-template-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.hr-schedule-template-page .field .control.is-select { padding:0; }
.hr-schedule-template-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-schedule-template-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; height:34px; line-height:34px; }
.hr-schedule-template-page .field .control :deep(.el-select) { width:100%; }
.hr-schedule-template-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0 12px; height:34px; }
.hr-schedule-template-page .field .control :deep(.el-date-editor) { width:100%; }

/* ===== Status Tabs ===== */
.hr-schedule-template-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.hr-schedule-template-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.hr-schedule-template-page .tabs-track::-webkit-scrollbar { display:none; }
.hr-schedule-template-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.hr-schedule-template-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.hr-schedule-template-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.hr-schedule-template-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.hr-schedule-template-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.hr-schedule-template-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.hr-schedule-template-page .status-tab.is-active .dot { background:var(--brand-500); }
.hr-schedule-template-page .status-tab.tab-green .dot { background:var(--green-500); }
.hr-schedule-template-page .status-tab.tab-green .count { background:var(--green-50); color:var(--green-700); }
.hr-schedule-template-page .status-tab.is-active.tab-green .count { background:var(--green-500); color:#fff; }
.hr-schedule-template-page .status-tab.tab-void .dot { background:var(--ink-400); }
.hr-schedule-template-page .tip-pill { display:inline-flex; align-items:center; gap:6px; padding:4px 12px; border-radius:999px; font-size:13px; color:var(--ink-500); background:var(--ink-50); border:1px solid var(--ink-200); cursor:pointer; transition:all .15s; white-space:nowrap; }
.hr-schedule-template-page .tip-pill:hover { color:var(--brand-600); border-color:var(--brand-200); background:var(--brand-50); }

/* ===== Toolbar ===== */
.hr-schedule-template-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-schedule-template-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-schedule-template-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-schedule-template-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Table ===== */
.hr-schedule-template-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-schedule-template-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-schedule-template-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-schedule-template-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-schedule-template-page .table-wrap { overflow-x:auto; }

/* ===== Action Column ===== */
:deep(.col-action) { padding:6px 4px !important; }
:deep(.col-action .cell) { display:flex; justify-content:center; padding:0; }
.action-btn-row { display:inline-flex; flex-wrap:wrap; justify-content:center; gap:0; }
:deep(.col-action .el-button) { padding:2px 4px; margin:0 2px; white-space:nowrap; justify-content:center; }
:deep(.col-action .el-button + .el-button) { margin-left:2px; }

/* ===== Badge ===== */
.hr-schedule-template-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-schedule-template-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-schedule-template-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-schedule-template-page .badge.green .dot { background:var(--green-500); }
.hr-schedule-template-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-schedule-template-page .badge.gray .dot { background:var(--ink-400); }
.hr-schedule-template-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }

/* ===== Pagination ===== */
.hr-schedule-template-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; }

/* ===== Shift Sequence Preview ===== */
.shift-sequence-preview { display:flex; flex-wrap:wrap; gap:4px; justify-content:center; }
.shift-chip { display:inline-block; padding:2px 8px; border-radius:4px; font-size:12px; font-weight:500; }
.shift-chip--work { background:#e0e7ff; color:#4338ca; }
.shift-chip--rest { background:#f1f5f9; color:#64748b; }

/* ===== Detail Dialog (rd-dialog) ===== */
:deep(.rd-dialog .el-dialog__header) { padding:0; margin:0; border:none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top:16px; right:16px; z-index:10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color:#fff; font-size:20px; }
:deep(.rd-dialog .el-dialog__body) { padding:12px 16px 16px; background:linear-gradient(135deg,#f8fafc 0%,#f1f5f9 100%); }
:deep(.rd-dialog .rd-page) { max-width:none; }
.hr-schedule-template-page .rd-page { max-height:60vh; overflow-y:auto; }
.hr-schedule-template-page .rd-detail-header { display:flex; align-items:center; gap:12px; padding:12px 20px; background:linear-gradient(135deg,#1e3a8a 0%,#2563eb 60%,#3b82f6 100%); border-radius:12px 12px 0 0; position:relative; overflow:hidden; }
.hr-schedule-template-page .rd-detail-header::before { content:''; position:absolute; top:-30px; right:-15px; width:140px; height:140px; border-radius:50%; background:radial-gradient(circle,rgba(255,255,255,.15) 0%,transparent 70%); pointer-events:none; }
.hr-schedule-template-page .rd-detail-header-icon { width:40px; height:40px; border-radius:10px; background:rgba(255,255,255,.2); border:1px solid rgba(255,255,255,.25); display:flex; align-items:center; justify-content:center; color:#fff; flex-shrink:0; backdrop-filter:blur(4px); box-shadow:0 4px 12px rgba(0,0,0,.1); }
.hr-schedule-template-page .rd-detail-header-main { flex:1; min-width:0; padding-right:36px; }
.hr-schedule-template-page .rd-detail-header-title { font-size:18px; font-weight:700; color:#fff; letter-spacing:-0.02em; line-height:1.3; }
.hr-schedule-template-page .rd-detail-header-sub { display:flex; align-items:center; gap:12px; flex-wrap:wrap; margin-top:4px; }
.hr-schedule-template-page .rd-detail-header-no { font-size:13px; font-weight:500; color:rgba(255,255,255,.8); font-variant-numeric:tabular-nums; }

/* ===== rd-card (与 hr/shift 等页面统一) ===== */
.hr-schedule-template-page .rd-card { background:#fff; border:1px solid #ebeef5; border-radius:8px; margin-bottom:16px; overflow:hidden; transition:box-shadow .25s cubic-bezier(.4,0,.2,1); animation:rdFadeIn .4s ease-out forwards; }
.hr-schedule-template-page .rd-card:hover { box-shadow:0 4px 6px -1px rgba(0,0,0,.1),0 2px 4px -2px rgba(0,0,0,.1); }
.hr-schedule-template-page .rd-card:last-child { margin-bottom:0; }
.hr-schedule-template-page .rd-card-header { display:flex; align-items:center; justify-content:space-between; padding:8px 16px; background:linear-gradient(to right,#f9fafb,#fff); border-bottom:1px solid #f3f4f6; cursor:pointer; user-select:none; }
.hr-schedule-template-page .rd-card-title { display:flex; align-items:center; gap:8px; font-size:16px; font-weight:700; color:#111827; }
.hr-schedule-template-page .rd-card-icon { display:flex; align-items:center; justify-content:center; width:32px; height:32px; border-radius:8px; background:#fff; border:1px solid #e5e7eb; color:#2563eb; flex-shrink:0; }
.hr-schedule-template-page .rd-collapse-btn { border:none; background:transparent; cursor:pointer; padding:4px; display:flex; align-items:center; color:#9ca3af; transition:transform .2s; }
.hr-schedule-template-page .rd-collapse-btn.is-collapsed { transform:rotate(-90deg); }
.hr-schedule-template-page .rd-card-body { padding:16px; }

/* ===== rd-grid (详情弹窗信息网格) ===== */
.hr-schedule-template-page .rd-grid { display:grid; grid-template-columns:repeat(2,1fr); gap:12px 24px; }
.hr-schedule-template-page .rd-item { display:flex; flex-direction:column; gap:4px; }
.hr-schedule-template-page .rd-item--full { grid-column:1 / -1; }
.hr-schedule-template-page .rd-label { font-size:12px; color:#6b7280; font-weight:500; letter-spacing:.025em; display:flex; align-items:center; gap:6px; }
.hr-schedule-template-page .rd-label-dot { width:4px; height:4px; border-radius:50%; background:#d1d5db; flex-shrink:0; }
.hr-schedule-template-page .rd-value { font-size:14px; color:#111827; font-weight:500; padding-left:10px; border-left:2px solid #f3f4f6; }

/* ===== rd-section-desc (编辑弹窗内说明) ===== */
.hr-schedule-template-page .rd-section-desc { font-size:13px; color:#6b7280; margin-bottom:12px; padding:8px 12px; background:#f9fafb; border-radius:6px; border-left:3px solid #d1d5db; }

/* ===== Sequence Builder ===== */
.sequence-builder { width:100%; }
.sequence-row { display:flex; align-items:center; gap:12px; margin-bottom:8px; }
.sequence-day { width:60px; font-size:13px; color:#64748b; flex-shrink:0; }
.sequence-name { font-size:13px; color:#334155; }

/* ===== Shift Sequence Detail (详情弹窗) ===== */
.shift-sequence-detail { display:flex; flex-wrap:wrap; gap:10px; }
.sequence-detail-item { display:flex; flex-direction:column; align-items:center; gap:6px; }
.sequence-detail-day { font-size:12px; color:#6b7280; font-weight:500; }

/* ===== Status Help Dialog ===== */
.status-help-content h4 { margin:16px 0 8px; font-size:16px; font-weight:700; color:#111827; }
.status-help-content h4:first-child { margin-top:0; }
.status-help-content .highlight-card { padding:12px 16px; border-radius:8px; border:1px solid #e5e7eb; margin-bottom:12px; }
.status-help-content .highlight-card-title { font-size:14px; font-weight:700; margin-bottom:6px; }
.status-help-content .highlight-card-body { font-size:13px; color:#374151; line-height:1.8; }
.status-help-content .highlight-card-body p { margin:4px 0; }
.status-help-content .highlight-info { background:#eff6ff; border-color:#bfdbfe; }
.status-help-content .highlight-info .highlight-card-title { color:#1d4ed8; }
.status-help-content .highlight-success { background:#ecfdf5; border-color:#a7f3d0; }
.status-help-content .highlight-success .highlight-card-title { color:#047857; }
.status-help-content .highlight-warning { background:#fffbeb; border-color:#fde68a; }
.status-help-content .highlight-warning .highlight-card-title { color:#b45309; }

@keyframes rdFadeIn {
  from { opacity:0; transform:translateY(8px); }
  to { opacity:1; transform:translateY(0); }
}

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-schedule-template-page .filter-bar { grid-template-columns:repeat(2,1fr) !important; } }
@media (max-width:720px) { .hr-schedule-template-page .filter-bar { grid-template-columns:1fr !important; } .hr-schedule-template-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
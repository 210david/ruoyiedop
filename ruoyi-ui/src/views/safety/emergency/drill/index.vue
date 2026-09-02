<template>
  <div class="app-container safety-drill-page">
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
          <label>演练名称</label>
          <div class="control">
            <el-input v-model="queryParams.drillName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>演练类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.drillType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_drill_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>演练编号</label>
          <div class="control">
            <el-input v-model="queryParams.drillCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>演练日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>演练地点</label>
          <div class="control">
            <el-input v-model="queryParams.drillLocation" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>演练指挥</label>
          <div class="control">
            <el-input v-model="queryParams.drillCommander" placeholder="请输入" clearable @keyup.enter="handleQuery" />
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
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:emergency:drill:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:emergency:drill:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:emergency:drill:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_drill_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="drillList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="演练编号" prop="drillCode" key="drillCode" :width="colWidth('drillCode', 130)" resizable v-if="columns.drillCode.visible" />
          <el-table-column label="演练名称" prop="drillName" key="drillName" :width="colWidth('drillName', 200)" resizable show-overflow-tooltip v-if="columns.drillName.visible" />
          <el-table-column label="演练类型" prop="drillType" key="drillType" :width="colWidth('drillType', 110)" resizable align="center" v-if="columns.drillType.visible">
            <template #default="scope"><span class="badge violet">{{ drillTypeLabel(scope.row.drillType) }}</span></template>
          </el-table-column>
          <el-table-column label="演练日期" prop="drillDate" key="drillDate" :width="colWidth('drillDate', 120)" resizable align="center" sortable="custom" v-if="columns.drillDate.visible" />
          <el-table-column label="演练地点" prop="drillLocation" key="drillLocation" :width="colWidth('drillLocation', 150)" resizable show-overflow-tooltip v-if="columns.drillLocation.visible" />
          <el-table-column label="演练指挥" prop="drillCommander" key="drillCommander" :width="colWidth('drillCommander', 100)" resizable v-if="columns.drillCommander.visible" />
          <el-table-column label="参与人数" prop="participantCount" key="participantCount" :width="colWidth('participantCount', 90)" resizable align="center" v-if="columns.participantCount.visible" />
          <el-table-column label="参与人员" prop="participants" key="participants" :width="colWidth('participants', 150)" resizable show-overflow-tooltip v-if="columns.participants.visible" />
          <el-table-column label="演练评估" prop="evalResult" key="evalResult" :width="colWidth('evalResult', 150)" resizable show-overflow-tooltip v-if="columns.evalResult.visible" />
          <el-table-column label="发现问题" prop="problems" key="problems" :width="colWidth('problems', 120)" resizable show-overflow-tooltip v-if="columns.problems.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:emergency:drill:query']">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:emergency:drill:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:emergency:drill:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="drillRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="演练编号" prop="drillCode"><el-input v-model="form.drillCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="演练名称" prop="drillName"><el-input v-model="form.drillName" placeholder="请输入演练名称" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="演练类型" prop="drillType"><el-select v-model="form.drillType" placeholder="请选择" style="width: 100%"><el-option v-for="dict in safety_drill_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="演练日期" prop="drillDate"><el-date-picker v-model="form.drillDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>演练组织</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="演练地点" prop="drillLocation"><el-input v-model="form.drillLocation" placeholder="请输入演练地点" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="演练指挥" prop="drillCommander"><el-input v-model="form.drillCommander" placeholder="请输入演练指挥人姓名" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="参与人数" prop="participantCount"><el-input-number v-model="form.participantCount" :min="0" :max="9999" placeholder="请输入" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="参与人员" prop="participants"><el-input v-model="form.participants" placeholder="请输入参与人员范围或分组" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg></span>演练目的与过程</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="演练目的" prop="drillObjective"><el-input v-model="form.drillObjective" type="textarea" :rows="2" placeholder="请输入演练目的" /></el-form-item>
              <el-form-item label="过程描述" prop="drillDesc"><el-input v-model="form.drillDesc" type="textarea" :rows="3" placeholder="请输入演练过程描述" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></span>评估与改进</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-form-item label="演练评估" prop="evalResult"><el-input v-model="form.evalResult" type="textarea" :rows="2" placeholder="请输入演练评估" /></el-form-item>
              <el-form-item label="发现问题" prop="problems"><el-input v-model="form.problems" type="textarea" :rows="2" placeholder="请输入发现的问题" /></el-form-item>
              <el-form-item label="改进措施" prop="improvements"><el-input v-model="form.improvements" type="textarea" :rows="2" placeholder="请输入改进措施" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></span>附件与备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c4">
              <el-form-item label="附件" prop="attachments"><file-upload v-model="form.attachments" :file-size="50" /></el-form-item>
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg></div>
          <span class="rd-detail-header-title">应急演练详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.drillCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.drillCode }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">演练编号</span><div class="rd-value">{{ viewData.drillCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">演练名称</span><div class="rd-value">{{ viewData.drillName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">演练类型</span><div class="rd-value"><dict-tag :options="safety_drill_type" :value="viewData.drillType" /></div></div>
              <div class="rd-item"><span class="rd-label">演练日期</span><div class="rd-value">{{ viewData.drillDate || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>演练组织</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">演练地点</span><div class="rd-value">{{ viewData.drillLocation || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">演练指挥</span><div class="rd-value">{{ viewData.drillCommander || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">参与人数</span><div class="rd-value">{{ viewData.participantCount != null ? viewData.participantCount + ' 人' : '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">参与人员</span><div class="rd-value">{{ viewData.participants || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.drillObjective || viewData.drillDesc">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg></span>演练目的与过程</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">演练目的</span><div class="rd-value">{{ viewData.drillObjective || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">过程描述</span><div class="rd-value">{{ viewData.drillDesc || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.evalResult || viewData.problems || viewData.improvements">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></span>评估与改进</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">演练评估</span><div class="rd-value">{{ viewData.evalResult || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">发现问题</span><div class="rd-value">{{ viewData.problems || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">改进措施</span><div class="rd-value">{{ viewData.improvements || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></span>附件与备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc4" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">附件</span><div class="rd-value"><div class="rd-file-links" v-if="viewData.attachments">
<div class="rd-file-item" v-for="(url, idx) in String(viewData.attachments).split(',')" :key="idx">
<div class="rd-file-link" @click="handleFilePreview(url)">
<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
<span class="rd-file-name">{{ url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url }}</span>
</div>
<span class="rd-file-dl" @click="handleFileDownload(url)">
<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
下载
</span>
</div>
            </div><span v-if="!viewData.attachments">-</span></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
    <file-preview ref="filePreviewRef" />

  </div>
</template>

<script setup name="SafetyDrill">
import { listDrill, getDrill, addDrill, updateDrill, delDrill } from '@/api/safety/drill'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowDown } from '@element-plus/icons-vue'
import FilePreview from '@/components/FilePreview/index.vue'
import { downloadFile } from '@/utils/downloadFile'

const { proxy } = getCurrentInstance()
const { safety_drill_type } = proxy.useDict('safety_drill_type')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_drill_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","c4","vc0","vc1","vc2","vc3","vc4"])

const drillList = ref([])
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
const viewOpen = ref(false)
const baseUrl = import.meta.env.VITE_APP_BASE_API
const viewData = ref({})

const default_columns = {
  drillCode: { label: '演练编号', visible: true },
  drillName: { label: '演练名称', visible: true },
  drillType: { label: '演练类型', visible: true },
  drillDate: { label: '演练日期', visible: true },
  drillLocation: { label: '演练地点', visible: true },
  drillCommander: { label: '演练指挥', visible: true },
  participantCount: { label: '参与人数', visible: true },
  participants: { label: '参与人员', visible: false },
  evalResult: { label: '演练评估', visible: true },
  problems: { label: '发现问题', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_drill_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(default_columns).forEach(key => {
        result[key] = { label: default_columns[key].label, visible: parsed[key] !== undefined ? parsed[key] : default_columns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...default_columns }
}

const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, drillName: undefined, drillType: undefined, drillCode: undefined, drillLocation: undefined, drillCommander: undefined, params: {} },
  rules: {
    drillName: [{ required: true, message: '演练名称不能为空', trigger: 'blur' }],
    drillType: [{ required: true, message: '演练类型不能为空', trigger: 'change' }],
    drillDate: [{ required: true, message: '演练日期不能为空', trigger: 'change' }],
    drillLocation: [{ required: true, message: '演练地点不能为空', trigger: 'blur' }],
    drillCommander: [{ required: true, message: '演练指挥不能为空', trigger: 'blur' }],
    participants: [{ required: true, message: '参与人员不能为空', trigger: 'blur' }],
    drillDesc: [{ required: true, message: '过程描述不能为空', trigger: 'blur' }],
    evalResult: [{ required: true, message: '演练评估不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.drillName) count++
  if (queryParams.value.drillType) count++
  if (queryParams.value.drillCode) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  if (queryParams.value.drillLocation) count++
  if (queryParams.value.drillCommander) count++
  return count
})

function handleFilePreview(url) {
const name = url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url
proxy.$refs.filePreviewRef.open(url, name)
}
function handleFileDownload(url) {
downloadFile(url)
}

function getList() { loading.value = true; listDrill(queryParams.value).then(response => { drillList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths() }).catch(error => { console.error(error) }).finally(() => { loading.value = false }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.drillName = undefined; queryParams.value.drillType = undefined; queryParams.value.drillCode = undefined; queryParams.value.drillLocation = undefined; queryParams.value.drillCommander = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.drillId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; collapsedCards.c3 = false; collapsedCards.c4 = false; open.value = true; title.value = '添加演练记录' }
function handleView(row) { const id = row.drillId || ids.value[0]; getDrill(id).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function handleUpdate(row) { reset(); getDrill(row.drillId || ids.value[0]).then(response => { form.value = response.data; collapsedCards.c2 = !response.data.drillObjective && !response.data.drillDesc; collapsedCards.c3 = !response.data.evalResult && !response.data.problems && !response.data.improvements; collapsedCards.c4 = !response.data.attachments && !response.data.remark; open.value = true; title.value = '修改演练记录' }) }
function submitForm() {
  proxy.$refs['drillRef'].validate(valid => {
    if (valid) {
      if (form.value.drillId != undefined) { updateDrill(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addDrill(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const drillIds = row.drillId || ids.value; proxy.$modal.confirm('是否确认删除演练记录？').then(function() { return delDrill(drillIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('safety/emergency/drill/export', { ...queryParams.value }, `emergency_drill_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { drillId: undefined, drillCode: undefined, drillName: undefined, drillType: undefined, drillDate: undefined, drillLocation: undefined, drillCommander: undefined, drillObjective: undefined, participants: undefined, participantCount: undefined, drillDesc: undefined, evalResult: undefined, problems: undefined, improvements: undefined, attachments: undefined, remark: undefined }
  proxy.resetForm('drillRef')
}

function drillTypeLabel(type) { const item = safety_drill_type.value.find(d => d.value == type); return item ? item.label : '-' }

getList()
</script>

<style scoped>
.safety-drill-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-drill-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-drill-page .filter-card { padding:14px 20px 16px; }
.safety-drill-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-drill-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-drill-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-drill-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-drill-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-drill-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-drill-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-drill-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-drill-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-drill-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-drill-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-drill-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-drill-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-drill-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-drill-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-drill-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-drill-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-drill-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-drill-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-drill-page .field .control :deep(.el-select) { width:100%; }
.safety-drill-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-drill-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-drill-page .toolbar .left, .safety-drill-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-drill-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-drill-page .table-wrap { overflow-x:auto; }
.safety-drill-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-drill-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-drill-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-drill-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-drill-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-drill-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-drill-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
..safety-drill-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } ..safety-drill-page .badge.amber .dot { background:var(--amber-500); }
..safety-drill-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } ..safety-drill-page .badge.blue .dot { background:var(--blue-500); }
..safety-drill-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } ..safety-drill-page .badge.green .dot { background:var(--green-500); }
..safety-drill-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } ..safety-drill-page .badge.red .dot { background:var(--red-500); }
..safety-drill-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } ..safety-drill-page .badge.gray .dot { background:var(--ink-400); }
.safety-drill-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-drill-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-drill-page .filter-card .filter-bar { grid-template-columns:1fr; } }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>

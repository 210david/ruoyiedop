<template>
  <div class="app-container pjm-list-page pjm-project-page">
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
          <label>项目编号</label>
          <div class="control">
            <el-input v-model="queryParams.projectNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>项目名称</label>
          <div class="control">
            <el-input v-model="queryParams.projectName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>项目类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.projectType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in projectTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>优先级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.priority" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in priorityOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>项目经理</label>
          <div class="control is-select">
            <el-select v-model="queryParams.managerId" placeholder="全部" clearable filterable @change="handleQuery">
              <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName + (u.dept && u.dept.deptName ? ' · ' + u.dept.deptName : '')" :value="u.userId" />
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
      <!-- 状态标签 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="d in statusOptions" :key="d.value" class="status-tab"
                  :class="[statusTabClass(d.value), { 'is-active': activeStatusTab === d.value }]"
                  @click="handleStatusTabClick(d.value)">
            <span class="dot"></span><span>{{ d.label }}</span><span class="count">{{ statusCounts[d.value] || 0 }}</span>
          </button>
        </div>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pjm:project:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate()" v-hasPermi="['pjm:project:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete()" v-hasPermi="['pjm:project:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pjm:project:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pjm_project_columns" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="projectList" border
                  @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" resizable align="center" />
          <el-table-column label="项目编号" prop="projectNo" key="projectNo" :width="colWidth('projectNo', 160)" resizable v-if="columns.projectNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.projectNo }}</span></template>
          </el-table-column>
          <el-table-column label="项目名称" prop="projectName" key="projectName" :min-width="colWidth('projectName', 240)" resizable show-overflow-tooltip v-if="columns.projectName.visible">
            <template #default="{row}">
              <el-link type="primary" underline="never" @click="handleView(row)" v-hasPermi="['pjm:project:query']">{{ row.projectName }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="类型" prop="projectType" key="projectType" :width="colWidth('projectType', 100)" resizable align="center" v-if="columns.projectType.visible">
            <template #default="scope">
              <span class="badge violet">{{ projectTypeLabel(scope.row.projectType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 110)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadge(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="进度" prop="progress" key="progress" :width="colWidth('progress', 150)" resizable v-if="columns.progress.visible">
            <template #default="scope">
              <div class="pbar-wrap">
                <div class="pbar">
                  <div class="pbar-fill" :class="{ 'is-done': Number(scope.row.progress || 0) >= 100 }"
                       :style="{ width: Math.min(Number(scope.row.progress || 0), 100) + '%' }"></div>
                </div>
                <span class="pbar-txt">{{ Number(scope.row.progress || 0).toFixed(0) }}%</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="预算" prop="budgetAmount" key="budgetAmount" :width="colWidth('budgetAmount', 130)" resizable align="right" class-name="col-num" v-if="columns.budgetAmount.visible">
            <template #default="scope"><span class="col-amt">¥{{ Number(scope.row.budgetAmount || 0).toLocaleString() }}</span></template>
          </el-table-column>
          <el-table-column label="已采购" prop="actualPurchase" key="actualPurchase" :width="colWidth('actualPurchase', 130)" resizable align="right" class-name="col-num" v-if="columns.actualPurchase.visible">
            <template #default="scope"><span class="col-amt">¥{{ Number(scope.row.actualPurchase || 0).toLocaleString() }}</span></template>
          </el-table-column>
          <el-table-column label="已付款" prop="paidAmount" key="paidAmount" :width="colWidth('paidAmount', 130)" resizable align="right" class-name="col-num" v-if="columns.paidAmount.visible">
            <template #default="scope"><span class="col-amt">¥{{ Number(scope.row.paidAmount || 0).toLocaleString() }}</span></template>
          </el-table-column>
          <el-table-column label="项目经理" prop="managerName" key="managerName" :width="colWidth('managerName', 110)" resizable align="center" v-if="columns.managerName.visible">
            <template #default="scope">{{ scope.row.managerName || '-' }}</template>
          </el-table-column>
          <el-table-column label="建设单位" prop="ownerUnit" key="ownerUnit" :width="colWidth('ownerUnit', 180)" resizable show-overflow-tooltip v-if="columns.ownerUnit.visible">
            <template #default="scope">{{ scope.row.ownerUnit || '-' }}</template>
          </el-table-column>
          <el-table-column label="合同工期" prop="contractDuration" key="contractDuration" :width="colWidth('contractDuration', 100)" resizable align="center" v-if="columns.contractDuration.visible">
            <template #default="scope">{{ scope.row.contractDuration ? scope.row.contractDuration + ' 天' : '-' }}</template>
          </el-table-column>
          <el-table-column label="回款进度" prop="payRatio" key="payRatio" :width="colWidth('payRatio', 140)" resizable v-if="columns.payRatio.visible">
            <template #default="scope">
              <div class="pbar-wrap" v-if="payRatioOf(scope.row) != null">
                <div class="pbar">
                  <div class="pbar-fill" :class="{ 'is-done': payRatioOf(scope.row) >= 100 }" :style="{ width: payRatioOf(scope.row) + '%' }"></div>
                </div>
                <span class="pbar-txt">{{ payRatioOf(scope.row).toFixed(0) }}%</span>
              </div>
              <span v-else class="muted-dash">—</span>
            </template>
          </el-table-column>
          <el-table-column label="计划开始" prop="startDate" key="startDate" :width="colWidth('startDate', 120)" resizable align="center" v-if="columns.startDate.visible">
            <template #default="scope">{{ scope.row.startDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="250" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['pjm:project:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pjm:project:edit']">修改</el-button>
                <el-button link type="warning" icon="Promotion" v-if="scope.row.status === '0' || scope.row.status === '7'" @click="handleSubmitAudit(scope.row)" v-hasPermi="['pjm:project:edit']">提交审批</el-button>
                <el-button link type="success" icon="Check" v-if="scope.row.status === '1'" @click="handleAudit(scope.row)" v-hasPermi="['pjm:project:audit']">审批</el-button>
                <el-button link type="warning" icon="FolderChecked" v-if="scope.row.status === '3'" @click="handleClose(scope.row)" v-hasPermi="['pjm:project:close']">结项</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pjm:project:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改/详情弹框 -->
    <el-dialog v-model="open" width="calc(100vw - 172px)" top="3vh" append-to-body draggable class="rd-dialog pj-wide-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><FolderOpened /></el-icon></div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <template v-if="form.projectNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ form.projectNo }}</span>
          </template>
        </div>
      </template>
      <el-form ref="projectRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- ★ 外层总 Tab（基本信息 / 资料 / 采购 / 成员） -->
          <div class="detail-tabs">
            <el-tabs v-model="detailTab" class="project-detail-tabs">
              <!-- ===== 基本信息 ===== -->
              <el-tab-pane name="base">
                <template #label>
                  <div class="dtab-label">
                    <el-icon><Document /></el-icon>
                    <span>基本信息</span>
                  </div>
                </template>
                <div class="dtab-body">
                  <section class="rd-card">
                    <div class="rd-card-header" @click="toggleCard('c0')">
                      <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基础信息</div>
                      <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
                    </div>
                    <div class="rd-card-body" v-show="!collapsedCards.c0">
                    <el-row :gutter="20">
                      <el-col :span="12">
                        <el-form-item label="项目编号" prop="projectNo">
                          <el-input v-model="form.projectNo" placeholder="系统自动生成" disabled />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="项目名称" prop="projectName">
                          <el-input v-model="form.projectName" placeholder="请输入项目名称" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="项目类型" prop="projectType">
                          <el-select v-model="form.projectType" style="width:100%">
                            <el-option v-for="d in projectTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
                          </el-select>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="工程类别" prop="engineeringCategory">
                          <el-select v-model="form.engineeringCategory" placeholder="请先选择项目类型" style="width:100%" clearable>
                            <el-option v-for="d in filteredEngineeringCategoryOptions" :key="d.value" :label="d.label" :value="d.value" />
                          </el-select>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="优先级" prop="priority">
                          <el-select v-model="form.priority" style="width:100%">
                            <el-option v-for="d in priorityOptions" :key="d.value" :label="d.label" :value="d.value" />
                          </el-select>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="项目经理" prop="managerId">
                          <el-input v-model="form.managerName" readonly placeholder="请选择项目经理" style="width:100%" @click="openUserPicker">
                            <template #append>
                              <el-button icon="Search" @click="openUserPicker" />
                            </template>
                            <template #suffix>
                              <el-icon v-if="form.managerName" class="clear-icon" style="cursor:pointer" @click.stop="clearManager"><CircleClose /></el-icon>
                            </template>
                          </el-input>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="所属部门">
                          <el-input v-model="form.deptName" readonly placeholder="选择项目经理后自动带出" style="width:100%" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                    </div>
                  </section>

                  <section class="rd-card">
                    <div class="rd-card-header" @click="toggleCard('c1')">
                      <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>计划与预算</div>
                      <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
                    </div>
                    <div class="rd-card-body" v-show="!collapsedCards.c1">
                    <el-row :gutter="20">
                      <el-col :span="12">
                        <el-form-item label="预算金额" prop="budgetAmount">
                          <el-input-number v-model="form.budgetAmount" :min="0" :precision="2" controls-position="right" style="width:100%" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12"></el-col>
                      <el-col :span="12">
                        <el-form-item label="计划开始">
                          <el-date-picker v-model="form.startDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="计划结束">
                          <el-date-picker v-model="form.endDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                    </div>
                  </section>

                  <section class="rd-card">
                    <div class="rd-card-header" @click="toggleCard('c2')">
                      <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg></span>工程信息</div>
                      <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
                    </div>
                    <div class="rd-card-body" v-show="!collapsedCards.c2">
                    <el-row :gutter="20">
                      <el-col :span="12">
                        <el-form-item label="建设单位">
                          <el-input v-model="form.ownerUnit" placeholder="业主单位" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="施工单位">
                          <el-input v-model="form.buildUnit" placeholder="承建单位" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="监理单位">
                          <el-input v-model="form.supervisorUnit" placeholder="监理单位" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="设计单位">
                          <el-input v-model="form.designUnit" placeholder="设计单位" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="24">
                        <el-form-item label="工程地点">
                          <el-input v-model="form.projectAddress" placeholder="如：XX市XX区XX路" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="24">
                        <el-form-item label="建设规模">
                          <el-input v-model="form.buildScale" placeholder="如：总建筑面积 12 万㎡，地上 18 层" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="合同工期">
                          <el-input-number v-model="form.contractDuration" :min="0" :precision="0" controls-position="right" style="width:100%" />
                          <div class="field-hint">单位：日历天</div>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="质量标准">
                          <el-input v-model="form.qualityStandard" maxlength="200" placeholder="如：符合《建筑工程施工质量验收统一标准》（GB50300-2013），一次验收合格" style="width:100%" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="合同总额">
                          <el-input-number v-model="form.contractAmount" :min="0" :precision="2" controls-position="right" style="width:100%" />
                          <div class="field-hint">单位：元，用于计算回款进度</div>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="开工日期">
                          <el-date-picker v-model="form.commenceDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="计划竣工">
                          <el-date-picker v-model="form.completionDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                    </div>
                  </section>

                  <section class="rd-card">
                    <div class="rd-card-header" @click="toggleCard('c3')">
                      <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>项目描述</div>
                      <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
                    </div>
                    <div class="rd-card-body" v-show="!collapsedCards.c3">
                    <el-form-item label="项目描述">
                      <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
                    </el-form-item>
                    </div>
                  </section>
                </div>
              </el-tab-pane>

              <!-- ===== 资料清单 ===== -->
              <el-tab-pane name="attach">
                <template #label>
                  <div class="dtab-label">
                    <el-icon><Paperclip /></el-icon>
                    <span>资料清单</span>
                    <span v-if="attachmentCount > 0" class="dtab-badge">{{ attachmentCount }}</span>
                  </div>
                </template>
                <div class="dtab-body">
                  <div class="dtab-toolbar">
                    <span class="dtab-hint">上传招标文件、投标文件、图纸、保险等资料，可按类型分组管理</span>
                    <div style="display:flex; gap:8px;">
                      <el-button type="warning" plain icon="MagicStick" size="small" :loading="aiParseLoading" @click="openAiParseDialog()">🤖 AI 自动解析</el-button>
                    </div>
                  </div>
                  <el-tabs v-model="aiParseCurrentType" type="border-card" class="attach-inner-tabs">
                    <el-tab-pane v-for="t in attachmentTypeOptions" :key="t.value" :label="t.label" :name="t.value">
                      <UploadFile
                        :model-value="getAttUrlList(t.value)"
                        @update:model-value="v => syncAttFromUpload(t.value, v)"
                        :limit="20" :fileSize="50" :fileType="['pdf', 'docx', 'doc', 'txt', 'md', 'xlsx', 'xls']" />
                    </el-tab-pane>
                  </el-tabs>
                  <div style="margin-top:12px; color:var(--ink-400); font-size:12px;">
                    💡 提示：上传后点击右上角「AI 自动解析」按钮，可从文本自动识别项目信息；PDF/图片类扫描件建议先手动粘贴文本摘要。
                  </div>
                </div>
              </el-tab-pane>

              <!-- ===== 采购清单 ===== -->
              <el-tab-pane name="proc">
                <template #label>
                  <div class="dtab-label">
                    <el-icon><Goods /></el-icon>
                    <span>采购清单</span>
                    <span v-if="procurementTotal !== '0.00'" class="dtab-badge amount">¥{{ procurementTotal }}</span>
                  </div>
                </template>
                <div class="dtab-body">
                  <div class="dtab-toolbar">
                    <div class="proc-tabs">
                      <button v-for="(t, ti) in procTypeOptions" :key="t.value" type="button"
                        :class="['proc-tab', 't' + (ti % 6), { 'is-active': procActiveType === t.value }]"
                        @click="procActiveType = t.value">
                        <span class="dot"></span>
                        <span>{{ t.label }}</span>
                        <span class="count">{{ procTypeCounts[t.value] || 0 }}</span>
                      </button>
                    </div>
                    <div style="display:flex; gap:8px;">
                      <el-tooltip content="请先保存项目后再发起采购" :disabled="!!form.projectId" placement="top">
                        <span>
                          <el-button type="warning" plain icon="ShoppingCart" size="small" :disabled="!form.projectId" @click="openPurchaseExecute">发起采购/出库</el-button>
                        </span>
                      </el-tooltip>
                      <el-button type="primary" plain icon="Plus" size="small" @click="addProcurementRow">新增一行</el-button>
                    </div>
                  </div>
                  <el-table :data="procRows" border size="small" class="app-table" :empty-text="'暂无' + procActiveTypeLabel + '明细，可点击右上角「新增一行」添加'">
                    <el-table-column type="index" label="#" width="48" align="center" />
                    <el-table-column label="物料" min-width="200">
                      <template #default="{row}">
                        <el-input :model-value="row.itemCode ? (row.itemName ? row.itemCode + ' - ' + row.itemName : row.itemCode) : ''" readonly size="small" placeholder="选择物料" style="cursor:pointer" @click="openMaterialPicker(row)">
                          <template #append>
                            <el-button v-if="row.materialId" icon="CircleClose" size="small" @click.stop="clearMaterial(row)" />
                            <el-button v-else icon="Search" size="small" @click.stop="openMaterialPicker(row)" />
                          </template>
                        </el-input>
                      </template>
                    </el-table-column>
                    <el-table-column label="名称/内容" prop="itemName" min-width="180">
                      <template #default="{row}"><el-input v-model="row.itemName" size="small" placeholder="必填" :disabled="!!row.materialId" /></template>
                    </el-table-column>
                    <el-table-column label="物料类型" width="110">
                      <template #default="{row}">
                        <el-select v-model="row.materialType" size="small" style="width:100%" :disabled="!!row.materialId" placeholder="类型">
                          <el-option v-for="d in wms_material_type" :key="d.value" :label="d.label" :value="String(d.value)" />
                        </el-select>
                      </template>
                    </el-table-column>
                    <el-table-column label="规格型号" prop="specModel" min-width="140">
                      <template #default="{row}"><el-input v-model="row.specModel" size="small" :disabled="!!row.materialId" /></template>
                    </el-table-column>
                    <el-table-column label="单位" prop="unit" width="100">
                      <template #default="{row}">
                        <el-tag v-if="row.materialId && row.unit != null" size="small" type="primary" effect="light">{{ unitDictLabel(row.unit) }}</el-tag>
                        <el-input v-else v-model="row.unit" size="small" placeholder="手动填写" />
                      </template>
                    </el-table-column>
                    <el-table-column label="数量" prop="qty" width="110" align="right">
                      <template #default="{row}">
                        <el-input-number v-model="row.qty" :min="0" :precision="2" controls-position="right" size="small" style="width:100%" @change="recalcProcurementAmount(row)" />
                      </template>
                    </el-table-column>
                    <el-table-column label="单价(元)" prop="unitPrice" width="120" align="right">
                      <template #default="{row}">
                        <el-input-number v-model="row.unitPrice" :min="0" :precision="2" controls-position="right" size="small" style="width:100%" @change="recalcProcurementAmount(row)" />
                      </template>
                    </el-table-column>
                    <el-table-column label="合计(元)" prop="amount" width="120" align="right">
                      <template #default="{row}">
                        <span class="col-amt" v-if="row.qty != null && row.unitPrice != null">¥{{ (Number(row.qty) * Number(row.unitPrice)).toFixed(2) }}</span>
                        <span class="muted-dash" v-else>—</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="72" align="center" fixed="right">
                      <template #default="{row}">
                        <el-button link type="danger" icon="Delete" size="small" @click="removeProcurementRow(row)">删除</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div style="display:flex; justify-content:flex-end; gap:20px; margin-top:10px; font-size:13px;">
                    <span>
                      <span style="color:var(--ink-400); margin-right:8px;">{{ procActiveTypeLabel }}合计：</span>
                      <span class="col-amt" style="font-weight:600;">¥{{ procActiveTotal }}</span>
                    </span>
                    <span>
                      <span style="color:var(--ink-400); margin-right:8px;">全部合计：</span>
                      <span class="col-amt">¥{{ procurementTotal }}</span>
                    </span>
                  </div>
                </div>
              </el-tab-pane>

              <!-- ===== 项目成员 ===== -->
              <el-tab-pane name="team">
                <template #label>
                  <div class="dtab-label">
                    <el-icon><User /></el-icon>
                    <span>项目成员</span>
                    <span v-if="(form.memberList || []).length > 0" class="dtab-badge">{{ (form.memberList || []).length }}</span>
                  </div>
                </template>
                <div class="dtab-body">
                  <div class="dtab-toolbar">
                    <span class="dtab-hint">可通过 AI 解析自动识别填入，或点击"添加成员"从系统用户中选择</span>
                    <div style="display:flex; gap:8px;">
                      <el-button type="primary" plain size="small" icon="Plus" @click="addMemberRow">添加成员</el-button>
                    </div>
                  </div>
                  <el-table :data="form.memberList || []" border size="small" class="app-table" empty-text="暂无项目团队成员：可点击右上角添加成员，或通过 AI 解析附件自动识别填入">
                    <el-table-column type="index" label="#" width="48" align="center" />
                    <el-table-column label="姓名" prop="userName" min-width="140">
                      <template #default="{row, $index}">
                        <div style="display:flex; gap:6px; align-items:center;">
                          <el-input v-model="row.userName" size="small" placeholder="必填" style="flex:1;" />
                          <el-button link type="primary" size="small" icon="Search" @click="openMemberUserPicker($index)" title="从系统用户选择" />
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column label="部门" prop="deptName" min-width="160">
                      <template #default="{row}">
                        <el-input v-model="row.deptName" size="small" placeholder="选填（AI/选择用户后自动回填）" />
                      </template>
                    </el-table-column>
                    <el-table-column label="角色" prop="roleType" width="140" align="center">
                      <template #default="{row}">
                        <el-select v-model="row.roleType" size="small" placeholder="选择角色">
                          <el-option v-for="r in teamRoleOptions" :key="r.value" :label="r.label" :value="r.value" />
                        </el-select>
                      </template>
                    </el-table-column>
                    <el-table-column label="加入日期" prop="joinDate" width="160" align="center">
                      <template #default="{row}">
                        <el-date-picker v-model="row.joinDate" type="date" value-format="YYYY-MM-DD" size="small" placeholder="选填" style="width:100%" />
                      </template>
                    </el-table-column>
                    <el-table-column label="系统用户" prop="userId" width="100" align="center">
                      <template #default="{row}">
                        <el-tag v-if="row.userId" type="success" size="small" effect="light">已关联</el-tag>
                        <el-tag v-else type="info" size="small" effect="plain">仅文本</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="72" align="center" fixed="right">
                      <template #default="{ $index }">
                        <el-button link type="danger" icon="Delete" size="small" @click="removeMemberRow($index)">删除</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div style="margin-top:8px; font-size:12px; color:var(--ink-400); line-height:1.6;">
                    💡 提示：AI 解析识别的成员若未自动关联到系统用户，会保存为"仅文本"模式；点 <el-icon style="vertical-align:middle;"><Search /></el-icon> 图标可改选系统用户。
                  </div>
                </div>
              </el-tab-pane>

              <!-- ===== WBS 任务分解 ===== -->
              <el-tab-pane name="wbs">
                <template #label>
                  <div class="dtab-label">
                    <el-icon><List /></el-icon>
                    <span>WBS 任务分解</span>
                    <span v-if="(form.wbsTaskList || []).length > 0" class="dtab-badge">{{ (form.wbsTaskList || []).length }}</span>
                  </div>
                </template>
                <div class="dtab-body">
                  <div class="dtab-toolbar">
                    <span class="dtab-hint">立项阶段先建立 WBS 任务骨架，详细分解可到「项目任务分解」菜单继续编辑</span>
                    <div style="display:flex; gap:8px;">
                      <el-button type="primary" plain size="small" icon="Plus" @click="addWbsTaskRow">新增任务</el-button>
                    </div>
                  </div>
                  <el-table :data="form.wbsTaskList || []" border size="small" class="app-table" empty-text="暂无 WBS 任务，点击「新增任务」开始建立任务分解结构">
                    <el-table-column type="index" label="#" width="48" align="center" />
                    <el-table-column label="任务名称" prop="taskName" min-width="180">
                      <template #default="{row}">
                        <el-input v-model="row.taskName" size="small" placeholder="必填" />
                      </template>
                    </el-table-column>
                    <el-table-column label="任务类型" prop="taskType" width="120" align="center">
                      <template #default="{row}">
                        <el-select v-model="row.taskType" size="small" style="width:100%">
                          <el-option v-for="d in wbsTaskTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
                        </el-select>
                      </template>
                    </el-table-column>
                    <el-table-column label="负责人" prop="ownerName" width="130">
                      <template #default="{row}">
                        <el-input v-model="row.ownerName" size="small" placeholder="选填" />
                      </template>
                    </el-table-column>
                    <el-table-column label="计划开始" prop="planStartDate" width="140" align="center">
                      <template #default="{row}">
                        <el-date-picker v-model="row.planStartDate" type="date" value-format="YYYY-MM-DD" size="small" placeholder="" style="width:100%" />
                      </template>
                    </el-table-column>
                    <el-table-column label="计划结束" prop="planEndDate" width="140" align="center">
                      <template #default="{row}">
                        <el-date-picker v-model="row.planEndDate" type="date" value-format="YYYY-MM-DD" size="small" placeholder="" style="width:100%" />
                      </template>
                    </el-table-column>
                    <el-table-column label="进度%" prop="progress" width="100" align="right">
                      <template #default="{row}">
                        <el-input-number v-model="row.progress" :min="0" :max="100" :precision="0" controls-position="right" size="small" style="width:100%" />
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="72" align="center" fixed="right">
                      <template #default="{ $index }">
                        <el-button link type="danger" icon="Delete" size="small" @click="removeWbsTaskRow($index)">删除</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>
      </el-form>

      <!-- ★ AI 解析确认弹窗（规范：rd-detail-header 蓝色横幅使白色关闭按钮可见；draggable 支持拖动标题移动；宽度 880px） -->
      <el-dialog v-model="aiParseDialogOpen" width="880px" append-to-body draggable class="rd-dialog">
        <template #header>
          <div class="rd-detail-header">
            <div class="rd-detail-header-icon"><el-icon :size="18"><MagicStick /></el-icon></div>
            <span class="rd-detail-header-title">AI 自动解析</span>
          </div>
        </template>
        <el-form label-width="110px">
          <el-form-item label="附件类型">
            <el-select v-model="aiParseCurrentType" style="width:100%">
              <el-option v-for="t in attachmentTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="解析来源">
            <el-radio-group v-model="aiParseSource">
              <el-radio value="file" :disabled="currentTypeUploadedFiles.length === 0">
                从已上传附件解析
                <el-tag v-if="currentTypeUploadedFiles.length > 0" size="small" type="success" effect="light" style="margin-left:4px;">
                  {{ currentTypeUploadedFiles.length }} 个附件
                </el-tag>
                <span v-else style="font-size:12px; color:var(--ink-400);">（当前类型暂无附件）</span>
              </el-radio>
              <el-radio value="text">粘贴文本摘要</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="aiParseSource === 'file' && currentTypeUploadedFiles.length > 0" label="将解析附件">
            <div style="font-size:12px; color:var(--ink-600); line-height:1.8;">
              <div v-for="(f, i) in currentTypeUploadedFiles" :key="i" style="display:flex; align-items:center; gap:6px;">
                <el-icon><Document /></el-icon>
                <span>{{ f.name || f.fileName || ('附件 ' + (i+1)) }}</span>
              </div>
            </div>
          </el-form-item>
          <el-form-item v-if="aiParseSource === 'text'" label="文本摘要">
            <el-input v-model="aiParseManualText" type="textarea" :rows="6" placeholder="粘贴招标文件/投标文件/合同中的关键文本片段（包含建设单位、工程名称、金额、工期、采购清单、项目人员名单等信息），AI 将自动识别填入" />
          </el-form-item>
        </el-form>
        <!-- ★ 解析结果二次确认区（结构化展示 + 勾选） -->
        <div v-if="aiParseResult" class="ai-confirm-area">
          <div class="ai-confirm-head">
            <span>📋 AI 识别结果（请核对并勾选需要填入的内容）</span>
            <el-button link type="info" size="small" @click="aiShowRawJson = !aiShowRawJson">
              {{ aiShowRawJson ? '隐藏原始JSON' : '查看原始JSON' }}
            </el-button>
          </div>
          <pre v-if="aiShowRawJson" style="max-height:180px; overflow:auto; background:#f7f8fa; border-radius:6px; padding:8px 12px; font-size:11px; white-space:pre-wrap; word-break:break-all;">{{ JSON.stringify(aiParseResult, null, 2) }}</pre>

          <!-- ① 工程概况 -->
          <div v-if="aiProjectFields.length > 0" class="ai-section">
            <div class="ai-section-title">
              <el-checkbox v-model="aiApplyProject">覆盖填入「工程概况」字段（{{ aiProjectFields.length }} 项）</el-checkbox>
            </div>
            <el-table :data="aiProjectFields" border size="small" class="app-table">
              <el-table-column label="字段" prop="label" width="130" />
              <el-table-column label="AI 识别值" prop="value" min-width="220">
                <template #default="{row}"><span style="color:#2563eb; font-weight:500;">{{ row.value }}</span></template>
              </el-table-column>
              <el-table-column label="当前表单值" min-width="180">
                <template #default="{row}"><span style="color:var(--ink-400);">{{ row.current || '（空）' }}</span></template>
              </el-table-column>
            </el-table>
          </div>

          <!-- ② 采购清单 -->
          <div v-if="Array.isArray(aiParseResult.procurement) && aiParseResult.procurement.length > 0" class="ai-section">
            <div class="ai-section-title">
              🛒 采购清单（勾选要填入的行，已选 {{ aiSelectedProcurement.length }}/{{ aiParseResult.procurement.length }}）
            </div>
            <el-table ref="aiProcTableRef" :data="aiParseResult.procurement" border size="small" class="app-table"
                      @selection-change="aiSelectedProcurement = $event">
              <el-table-column type="selection" width="45" align="center" />
              <el-table-column type="index" label="#" width="45" align="center" />
              <el-table-column label="物料名称" prop="itemName" min-width="140" />
              <el-table-column label="规格型号" prop="specModel" min-width="110" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="60" align="center" />
              <el-table-column label="数量" prop="qty" width="80" align="right" />
              <el-table-column label="单价(元)" prop="unitPrice" width="100" align="right" />
            </el-table>
          </div>

          <!-- ③ 项目团队 -->
          <div v-if="Array.isArray(aiParseResult.team) && aiParseResult.team.length > 0" class="ai-section">
            <div class="ai-section-title">
              👥 项目团队（勾选要填入的成员，已选 {{ aiSelectedTeam.length }}/{{ aiParseResult.team.length }}）
            </div>
            <el-table ref="aiTeamTableRef" :data="aiParseResult.team" border size="small" class="app-table"
                      @selection-change="aiSelectedTeam = $event">
              <el-table-column type="selection" width="45" align="center" />
              <el-table-column type="index" label="#" width="45" align="center" />
              <el-table-column label="姓名" prop="userName" min-width="100" />
              <el-table-column label="角色" width="110" align="center">
                <template #default="{row}">
                  <el-tag size="small" effect="light">{{ teamRoleLabel(row.roleType) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="部门" prop="deptName" min-width="130" show-overflow-tooltip />
              <el-table-column label="加入日期" prop="joinDate" width="100" align="center">
                <template #default="{row}">{{ row.joinDate || '—' }}</template>
              </el-table-column>
            </el-table>
          </div>

          <div v-if="aiProjectFields.length === 0 && (!aiParseResult.procurement || aiParseResult.procurement.length === 0) && (!aiParseResult.team || aiParseResult.team.length === 0)"
               style="padding:20px; text-align:center; color:var(--ink-400); font-size:13px;">
            AI 未能从附件中识别出有效信息，请尝试粘贴更完整的文本摘要
          </div>
        </div>
        <template #footer>
          <el-button @click="aiParseDialogOpen = false">取 消</el-button>
          <el-button type="primary" :loading="aiParseLoading" @click="doAiParse">开始解析</el-button>
          <el-button v-if="aiParseResult" type="success" @click="applyAiParseResult">一键填入表单</el-button>
        </template>
      </el-dialog>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看详情弹框（规范第十节：查看与编辑弹窗分离，只读布局 rd-page + rd-card + rd-grid） -->
    <el-dialog v-model="viewOpen" width="calc(100vw - 172px)" top="3vh" append-to-body draggable class="rd-dialog pj-wide-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><FolderOpened /></el-icon></div>
          <span class="rd-detail-header-title">项目详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.projectNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ viewData.projectNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <el-tabs v-model="viewTab" class="project-detail-tabs">
          <el-tab-pane name="base">
            <template #label>
              <div class="dtab-label"><el-icon><Document /></el-icon><span>基本信息</span></div>
            </template>
            <div class="dtab-body" style="padding-top: 12px;">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">项目编号</span><div class="rd-value">{{ viewData.projectNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">项目名称</span><div class="rd-value">{{ viewData.projectName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">项目类型</span><div class="rd-value">{{ dictLabel(projectTypeOptions, viewData.projectType) || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工程类别</span><div class="rd-value">{{ dictLabel(engineeringCategoryOptions, viewData.engineeringCategory) || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">优先级</span><div class="rd-value">{{ dictLabel(priorityOptions, viewData.priority) || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">项目经理</span><div class="rd-value">{{ viewData.managerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>计划与预算</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">计划开始</span><div class="rd-value">{{ viewData.startDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划结束</span><div class="rd-value">{{ viewData.endDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">预算金额</span><div class="rd-value rd-amount">{{ viewData.budgetAmount != null ? '¥' + viewData.budgetAmount : '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg></span>工程信息</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">建设单位</span><div class="rd-value">{{ viewData.ownerUnit || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">施工单位</span><div class="rd-value">{{ viewData.buildUnit || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">监理单位</span><div class="rd-value">{{ viewData.supervisorUnit || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">设计单位</span><div class="rd-value">{{ viewData.designUnit || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">工程地点</span><div class="rd-value">{{ viewData.projectAddress || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">建设规模</span><div class="rd-value">{{ viewData.buildScale || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">合同工期</span><div class="rd-value">{{ viewData.contractDuration != null ? viewData.contractDuration + ' 日历天' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">质量标准</span><div class="rd-value">{{ viewData.qualityStandard || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">合同总额</span><div class="rd-value rd-amount">{{ viewData.contractAmount != null ? '¥' + viewData.contractAmount : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">开工日期</span><div class="rd-value">{{ viewData.commenceDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划竣工</span><div class="rd-value">{{ viewData.completionDate || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.description">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>项目描述</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-value">{{ viewData.description }}</div>
          </div>
        </section>
            </div>
          </el-tab-pane>

          <el-tab-pane name="attach">
            <template #label>
              <div class="dtab-label"><el-icon><Paperclip /></el-icon><span>资料清单</span><span v-if="(viewData.attachmentList || []).length > 0" class="dtab-badge">{{ (viewData.attachmentList || []).length }}</span></div>
            </template>
            <div class="dtab-body">
              <!-- 按附件类型分类显示（与新增页一致的内层 border-card 页签） -->
              <el-tabs v-model="viewAttachType" type="border-card" class="attach-inner-tabs">
                <el-tab-pane v-for="t in attachmentTypeOptions" :key="t.value" :label="t.label" :name="t.value">
                  <el-table :data="(viewData.attachmentList || []).filter(a => String(a.attachmentType) === t.value)" border size="small" class="app-table" :empty-text="'暂无' + t.label">
                    <el-table-column type="index" label="#" width="48" align="center" />
                    <el-table-column label="文件名" prop="fileName" min-width="320" show-overflow-tooltip />
                    <el-table-column label="大小" width="110" align="right">
                      <template #default="{row}">{{ row.fileSize != null ? (row.fileSize / 1024).toFixed(0) + ' KB' : '-' }}</template>
                    </el-table-column>
                    <el-table-column label="操作" width="140" align="center">
                      <template #default="{row}">
                        <el-button link type="primary" icon="View" @click="handleDetailPreview(row)">预览</el-button>
                        <el-button link type="success" icon="Download" @click="handleDetailDownload(row)">下载</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
              </el-tabs>
            </div>
          </el-tab-pane>

          <el-tab-pane name="proc">
            <template #label>
              <div class="dtab-label"><el-icon><Goods /></el-icon><span>采购清单</span><span v-if="(viewData.procurementList || []).length > 0" class="dtab-badge">{{ (viewData.procurementList || []).length }}</span></div>
            </template>
            <div class="dtab-body">
              <el-table :data="viewData.procurementList || []" border size="small" class="app-table" empty-text="暂无采购明细">
                <el-table-column type="index" label="#" width="48" align="center" />
                <el-table-column label="物料类型" width="120">
                  <template #default="{row}">{{ (wms_material_type.find(d => d.value === String(row.materialType)) || {}).label || '未分类' }}</template>
                </el-table-column>
                <el-table-column label="物料名称" prop="itemName" min-width="180" show-overflow-tooltip />
                <el-table-column label="规格型号" prop="specModel" min-width="120" show-overflow-tooltip />
                <el-table-column label="单位" prop="unit" width="80" align="center" />
                <el-table-column label="数量" prop="qty" width="90" align="right" />
                <el-table-column label="单价(元)" width="110" align="right">
                  <template #default="{row}">{{ row.unitPrice != null ? Number(row.unitPrice).toLocaleString() : '-' }}</template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane name="team">
            <template #label>
              <div class="dtab-label"><el-icon><User /></el-icon><span>项目成员</span><span v-if="(viewData.memberList || []).length > 0" class="dtab-badge">{{ (viewData.memberList || []).length }}</span></div>
            </template>
            <div class="dtab-body">
              <el-table :data="viewData.memberList || []" border size="small" class="app-table" empty-text="暂无项目团队成员">
                <el-table-column type="index" label="#" width="48" align="center" />
                <el-table-column label="姓名" prop="userName" min-width="120" />
                <el-table-column label="角色" width="140" align="center">
                  <template #default="{row}">{{ teamRoleLabel(row.roleType) }}</template>
                </el-table-column>
                <el-table-column label="所属部门" prop="deptName" min-width="140" show-overflow-tooltip />
                <el-table-column label="加入日期" prop="joinDate" width="120" align="center" />
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane name="wbs">
            <template #label>
              <div class="dtab-label"><el-icon><List /></el-icon><span>WBS 任务分解</span><span v-if="(viewData.wbsTaskList || []).length > 0" class="dtab-badge">{{ (viewData.wbsTaskList || []).length }}</span></div>
            </template>
            <div class="dtab-body">
              <el-table :data="viewData.wbsTaskList || []" border size="small" class="app-table" empty-text="暂无 WBS 任务">
                <el-table-column type="index" label="#" width="48" align="center" />
                <el-table-column label="任务名称" prop="taskName" min-width="200" show-overflow-tooltip />
                <el-table-column label="负责人" prop="ownerName" width="110" />
                <el-table-column label="计划开始" prop="planStartDate" width="110" align="center" />
                <el-table-column label="计划结束" prop="planEndDate" width="110" align="center" />
                <el-table-column label="进度%" prop="progress" width="90" align="right" />
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 附件预览（全站统一 FilePreview 组件） -->
    <file-preview ref="detailFilePreviewRef" />

    <!-- 审批弹框 -->
    <el-dialog v-model="auditOpen" width="880px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><Stamp /></el-icon></div>
          <span class="rd-detail-header-title">项目审批</span>
          <template v-if="auditProjectData.projectNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ auditProjectData.projectNo }} {{ auditProjectData.projectName }}</span>
          </template>
        </div>
      </template>
      <div class="rd-page">
        <!-- 项目信息（审批人核对用，getProject 全量只读） -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>项目信息</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">项目编号</span><div class="rd-value">{{ auditProjectData.projectNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">项目名称</span><div class="rd-value">{{ auditProjectData.projectName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">项目类型</span><div class="rd-value">{{ dictLabel(projectTypeOptions, auditProjectData.projectType) || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工程类别</span><div class="rd-value">{{ dictLabel(engineeringCategoryOptions, auditProjectData.engineeringCategory) || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">优先级</span><div class="rd-value">{{ dictLabel(priorityOptions, auditProjectData.priority) || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">项目经理</span><div class="rd-value">{{ auditProjectData.managerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ auditProjectData.deptName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">建设单位</span><div class="rd-value">{{ auditProjectData.ownerUnit || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>计划与金额</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">预算金额</span><div class="rd-value rd-amount">{{ auditProjectData.budgetAmount != null ? '¥' + auditProjectData.budgetAmount : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">合同总额</span><div class="rd-value rd-amount">{{ auditProjectData.contractAmount != null ? '¥' + auditProjectData.contractAmount : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">合同工期</span><div class="rd-value">{{ auditProjectData.contractDuration != null ? auditProjectData.contractDuration + ' 日历天' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">质量标准</span><div class="rd-value">{{ auditProjectData.qualityStandard || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">工程地点</span><div class="rd-value">{{ auditProjectData.projectAddress || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">建设规模</span><div class="rd-value">{{ auditProjectData.buildScale || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 审批意见（通过/驳回由底部按钮决定，驳回必填意见） -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>审批意见</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <el-form label-width="90px">
              <el-form-item label="审批意见">
                <el-input v-model="auditOpinion" type="textarea" :rows="3" placeholder="通过时可填写审批备注；驳回时必填驳回原因" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" @click="submitAudit(true)">通 过</el-button>
        <el-button type="danger" @click="submitAudit(false)">驳 回</el-button>
        <el-button @click="auditOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ★ 项目采购执行弹窗（有库存走出库 / 无库存走采购） -->
    <el-dialog v-model="peOpen" width="1180px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><ShoppingCart /></el-icon></div>
          <span class="rd-detail-header-title">项目采购执行</span>
          <span class="rd-detail-header-divider"></span>
          <span class="rd-detail-header-no">{{ form.projectNo }} {{ form.projectName }}</span>
        </div>
      </template>
      <div v-loading="peLoading">
        <el-alert type="info" :closable="false" style="margin-bottom:12px;">
          <template #title>
            按采购清单逐项检查库存：<b>有库存</b>的数量默认走出库申请（生成 WMS 领料出库单），<b>无库存</b>的数量走采购申请。
            可调整出库数量，剩余数量自动计入采购。
          </template>
        </el-alert>
        <el-table :data="peItems" border size="small" class="app-table">
          <el-table-column type="index" label="#" width="46" align="center" />
          <el-table-column label="物料类型" width="120" align="center">
            <template #default="{row}">
              <el-select v-model="row.materialType" size="small" style="width:100px" @change="onPeMaterialTypeChange(row)">
                <el-option v-for="d in wms_material_type" :key="d.value" :label="d.label" :value="String(d.value)" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="物料名称/内容" min-width="250" show-overflow-tooltip>
            <template #default="{row, $index}">
              <el-input v-if="row.materialId" :model-value="row.itemName ? (row.itemCode ? row.itemCode + ' - ' + row.itemName : row.itemName) : ''" readonly size="small" placeholder="点击浏览选择物料" style="cursor:pointer" @click="openPeMaterialPicker($index)">
                <template #append>
                  <el-button v-if="row.materialId" icon="CircleClose" size="small" @click.stop="clearPeMaterial($index)" />
                  <el-button v-else icon="Search" size="small" @click.stop="openPeMaterialPicker($index)" />
                </template>
              </el-input>
              <el-input v-else v-model="row.itemName" size="small" placeholder="输入内容（如劳务/设备/其他）" />
            </template>
          </el-table-column>
          <el-table-column label="物料编码" prop="itemCode" width="110">
            <template #default="{row}"><span class="col-mono">{{ row.itemCode || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="规格型号" prop="specModel" min-width="120" show-overflow-tooltip>
            <template #default="{row}">{{ row.specModel || '-' }}</template>
          </el-table-column>
          <el-table-column label="单位" width="80" align="center">
            <template #default="{row}">{{ row.materialId ? unitDictLabel(row.unit) : (row.unit || '-') }}</template>
          </el-table-column>
          <el-table-column label="需求数量" prop="needQty" width="100" align="right" class-name="col-num" />
          <el-table-column label="可用库存" width="110" align="right" class-name="col-num">
            <template #default="{row}">
              <span v-if="!row.materialId" class="muted-dash">—</span>
              <span v-else-if="!row.materialId" class="muted-dash">未关联物料</span>
              <el-tag v-else :type="row.availQty > 0 ? 'success' : 'info'" size="small" effect="light">{{ row.availQty }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="出库数量" width="130" align="center">
            <template #default="{row}">
              <el-input-number v-if="row.materialId" v-model="row.outboundQty" :min="0" :max="Math.min(row.needQty, row.availQty)" :precision="2"
                :disabled="!row.materialId || row.availQty <= 0" controls-position="right" size="small" style="width:100%"
                @change="onPeOutboundChange(row)" />
              <span v-else class="muted-dash">—</span>
            </template>
          </el-table-column>
          <el-table-column label="出库仓库" width="130" align="center">
            <template #default="{row}">
              <span v-if="!row.materialId || Number(row.outboundQty) <= 0" class="muted-dash">—</span>
              <el-tooltip v-else :content="peAllocText(row)" placement="top" :disabled="!row.outboundAlloc || !row.outboundAlloc.length">
                <span style="font-size:12px;color:#409eff;">{{ row.outboundAlloc && row.outboundAlloc.length ? '自动 ' + row.outboundAlloc.length + ' 个仓' : '自动分配' }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="采购数量" width="100" align="right" class-name="col-num">
            <template #default="{row}">
              <span :style="row.purchaseQty > 0 ? 'color:#e6a23c;font-weight:600;' : ''">{{ row.purchaseQty }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="110" align="right" class-name="col-num">
            <template #default="{row}">
              <el-input-number v-model="row.unitPrice" :min="0" :precision="2" controls-position="right" size="small" style="width:100%" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="70" align="center" fixed="right">
            <template #default="{ $index }">
              <el-button link type="danger" icon="Delete" size="small" @click="removePeItem($index)">删</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top:10px;">
          <el-button type="primary" plain icon="Plus" size="small" @click="addPeItem('')">+ 新增一行</el-button>
          <span style="margin-left:12px;color:#909399;font-size:12px;">提示：物料明细行可从系统物料库浏览选择；劳务/设备/其他为非物料行，直接手填内容</span>
        </div>

        <!-- 出库设置 -->
        <div v-if="peHasOutbound" class="pe-section">
          <div class="pe-section-title">📦 出库设置（生成 WMS 领料出库单，草稿状态）</div>
          <el-form label-width="110px" inline>
            <el-form-item label="出库仓库">
              <span class="pe-section-hint">系统自动按各仓库可用库存分配：优先单个仓库足量出库，单仓不足时自动跨仓库拆分出库（每个仓库生成一张出库单），无需手动选择仓库</span>
            </el-form-item>
          </el-form>
        </div>

        <!-- 采购设置 -->
        <div v-if="peHasPurchase" class="pe-section">
          <div class="pe-section-title">🛒 采购设置（生成 PMS 采购申请，草稿状态）</div>
          <el-form label-width="100px">
            <el-form-item label="采购模式" required>
              <el-radio-group v-model="peMode">
                <el-radio value="1">
                  <b>入库后再出库</b>（正常采购：仓库收货入库 → 再从仓库出库到项目）
                </el-radio>
                <el-radio value="2">
                  <b>直送项目现场</b>（零散采购：不进仓库，收货验收后自动登记耗用）
                </el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="申请标题">
              <el-input v-model="peTitle" style="width:420px" placeholder="默认：项目名称 - 物料采购" />
            </el-form-item>
            <el-form-item label="期望交货">
              <el-date-picker v-model="peExpectDate" type="date" value-format="YYYY-MM-DD" placeholder="选填" style="width:180px" />
            </el-form-item>
          </el-form>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" icon="Promotion" :loading="peExecuting" :disabled="peSubmitDisabled" @click="submitPurchaseExecute">确认执行</el-button>
          <el-button @click="peOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ★ 出库仓库分配弹窗（同一物料跨多仓库出库） -->
    <el-dialog v-model="peAllocOpen" width="660px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><Box /></el-icon></div>
          <span class="rd-detail-header-title">出库仓库分配</span>
          <span class="rd-detail-header-divider"></span>
          <span class="rd-detail-header-no">{{ peAllocItemName }}</span>
        </div>
      </template>
      <div>
        <el-alert type="info" :closable="false" style="margin-bottom:10px;">
          <template #title>
            该物料在下列仓库有可用库存：需求 <b>{{ peAllocNeedQty }}</b>，已分配 <b>{{ peAllocTotal }}</b>，剩余 <b>{{ peAllocRemain }}</b>。
            可同时从多个仓库出库，每个仓库生成一张 WMS 领料出库单（草稿）。
          </template>
        </el-alert>
        <el-table :data="peAllocRows" border size="small" class="app-table" max-height="320">
          <el-table-column type="index" label="#" width="46" align="center" />
          <el-table-column prop="warehouseName" label="仓库名称" min-width="180" show-overflow-tooltip />
          <el-table-column label="可用库存" width="110" align="right" class-name="col-num">
            <template #default="{ row }">{{ row.availQty }}</template>
          </el-table-column>
          <el-table-column label="本次出库数量" width="160" align="center">
            <template #default="{ row }">
              <el-input-number v-model="row.qty" :min="0" :max="row.availQty" :precision="2" controls-position="right" size="small" style="width:100%" />
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button plain icon="MagicStick" @click="autoAllocPe">自动分配</el-button>
          <el-button @click="peAllocOpen = false">取 消</el-button>
          <el-button type="primary" @click="confirmPeAlloc">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 项目经理选择弹窗（组织架构） -->
    <user-picker ref="userPickerRef" title="选择项目经理" @confirm="onUserPickerConfirm" />

    <!-- ★ 系统物料选择弹窗（复用全局组件） -->
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />

    <!-- ★ 项目团队成员选择弹窗 -->
    <user-picker ref="memberUserPickerRef" title="选择团队成员" @confirm="onMemberUserPickerConfirm" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, getCurrentInstance, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listProject, getProject, addProject, updateProject, delProject, exportProject, auditProject, submitProject, closeProject, aiParseProject, stockCheckProjectPurchase, executeProjectPurchase } from '@/api/pjm/project'
import { summaryPayment } from '@/api/pjm/payment'
import { listAllUsers } from '@/api/pjm/member'
import UserPicker from '@/components/UserPicker/index.vue'
import UploadFile from '@/components/FileUpload/index.vue'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { CircleClose } from '@element-plus/icons-vue'
import { downloadFile } from '@/utils/downloadFile'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()

// ★ 字典：wms_unit 单位字典 + pjm_engineering_category 工程类别字典 + pjm_purchase_detail_type 采购明细类型 + pjm_project_attachment_type 附件类型
const { wms_unit, wms_material_type, pjm_engineering_category, pjm_purchase_detail_type, pjm_project_attachment_type } = proxy.useDict('wms_unit', 'wms_material_type', 'pjm_engineering_category', 'pjm_purchase_detail_type', 'pjm_project_attachment_type')
// ★ 工程类别：按已选项目类型级联过滤
const allEngineeringCategoryOptions = pjm_engineering_category
const filteredEngineeringCategoryOptions = computed(() => {
  const all = allEngineeringCategoryOptions.value || []
  const prefix = projectTypeToEngCatPrefix[form.projectType]
  if (!prefix) return all   // 没选项目类型时显示全部
  return all.filter(d => String(d.value).startsWith(prefix))
})
// 兼容旧引用（详情/审批只读展示用全部，不影响）
const engineeringCategoryOptions = allEngineeringCategoryOptions
const peDetailTypeOptions = computed(() => (pjm_purchase_detail_type.value || []).map(d => ({ ...d, value: String(d.value) })))
// ★ 采购清单页签：按物料类型（wms_material_type 字典）动态分类统计，替代写死的明细类型
const procTypeOptions = computed(() => {
  const opts = (wms_material_type.value || []).map(d => ({ value: String(d.value), label: d.label }))
  if ((form.procurementList || []).some(p => p.materialType == null || p.materialType === '')) {
    opts.push({ value: '', label: '未分类' })
  }
  return opts
})
const procTypeKey = (p) => (p.materialType == null || p.materialType === '') ? '' : String(p.materialType)
const engCatLabel = (v) => engineeringCategoryOptions.find(x => String(x.value) === String(v))?.label || v || '-'
const unitDictLabel = (v) => {
  if (v == null || v === '') return ''
  const item = wms_unit.value?.find(d => String(d.value) === String(v))
  return item ? item.label : String(v)
}
const { collapsedCards, toggleCard } = useDetailCard(['c0', 'c1', 'c2', 'c3', 'c4', 'c5', 'c6', 'c7'])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pjm_project_index')

const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const projectList = ref([])
const userOptions = ref([])
const open = ref(false)
const auditOpen = ref(false)
const auditProjectData = ref({})      // 审批弹窗的项目全量信息（getProject 拉取）
const viewOpen = ref(false)
const viewData = ref({})
const viewTab = ref('base')            // 详情弹窗当前页签
const viewAttachType = ref('1')        // 详情弹窗资料清单内层分类页签
const detailFilePreviewRef = ref(null) // 详情弹窗附件预览（全站统一 FilePreview 组件）
const auditStatus = ref('2')
const auditOpinion = ref('')
const title = ref('')
const activeStatusTab = ref('all')
const statusCounts = ref({})

// ★ AI 解析相关
const aiParseLoading = ref(false)
const aiParseResult = ref(null)  // { project: {...}, procurement: [...], team: [...] }
const aiParseDialogOpen = ref(false)
const aiParseManualText = ref('')
const aiParseCurrentType = ref('1')
const aiParseSource = ref('file')  // 'file'=从已上传附件解析 / 'text'=粘贴文本摘要
// ★ AI 结果二次确认
const aiShowRawJson = ref(false)
const aiApplyProject = ref(true)           // 是否覆盖工程概况
const aiSelectedProcurement = ref([])      // 勾选的采购清单行
const aiSelectedTeam = ref([])             // 勾选的项目团队成员
const aiProcTableRef = ref()
const aiTeamTableRef = ref()
// 附件按 type 分组的临时文件列表（UI 层用，类型由字典 pjm_project_attachment_type 驱动）
const attFileGroups = reactive({})

// ★ 物料选择相关
const materialPickerRef = ref()
const currentProcurementRow = ref(null)  // 当前选中物料的采购清单行
const procActiveType = ref('1')          // 采购清单当前页签（明细类型）
const detailTab = ref('base')            // 详情区外层 Tabs 当前页签

// ★ 项目团队相关
const teamRoleOptions = [
  { value: '1', label: '项目经理' }, { value: '2', label: '技术负责人' },
  { value: '3', label: '施工员' },   { value: '4', label: '安全员' },
  { value: '5', label: '质量员' },   { value: '6', label: '资料员' },
  { value: '7', label: '监理' }
]
const wbsTaskTypeOptions = [
  { value: '1', label: '单项工程' }, { value: '2', label: '单位工程' },
  { value: '3', label: '分部工程' }, { value: '4', label: '分项工程' },
  { value: '5', label: '检验批' }
]
const memberUserPickerRef = ref()
const currentMemberIndex = ref(-1)  // 当前要替换成员的行下标

const columns = ref({
  projectNo: { label: '项目编号', visible: true },
  projectName: { label: '项目名称', visible: true },
  projectType: { label: '类型', visible: true },
  status: { label: '状态', visible: true },
  progress: { label: '进度', visible: true },
  budgetAmount: { label: '预算', visible: true },
  actualPurchase: { label: '已采购', visible: true },
  paidAmount: { label: '已付款', visible: true },
  managerName: { label: '项目经理', visible: true },
  ownerUnit: { label: '建设单位', visible: true },
  contractDuration: { label: '合同工期', visible: true },
  payRatio: { label: '回款进度', visible: true },
  startDate: { label: '计划开始', visible: true }
})

const queryParams = reactive({ pageNum: 1, pageSize: 10, projectNo: null, projectName: null, projectType: null, status: null, priority: null, managerId: null })
const form = reactive({})
const rules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  projectType: [{ required: true, message: '请选择项目类型', trigger: 'change' }]
}

const statusOptions = [
  { value: '0', label: '草稿' }, { value: '1', label: '待审批' },
  { value: '2', label: '审批通过' }, { value: '3', label: '执行中' },
  { value: '4', label: '已完成' }, { value: '5', label: '已关闭' },
  { value: '6', label: '暂停' }, { value: '7', label: '驳回' }
]
const projectTypeOptions = [
  { value: '1', label: '房屋建筑' }, { value: '2', label: '市政工程' },
  { value: '3', label: '机电安装' }, { value: '4', label: '装饰装修' },
  { value: '5', label: '园林绿化' }, { value: '6', label: '基础设施' },
  { value: '7', label: 'EPC总承包' }
]
// ★ 项目类型 → 工程类别字典 value 前缀映射（用于级联过滤）
const projectTypeToEngCatPrefix = {
  '1': 'house_',      // 房屋建筑 → house_residential / house_public / ...
  '2': 'municipal_',  // 市政工程 → municipal_road / municipal_bridge / ...
  '3': 'mep_',        // 机电安装 → mep_industrial / mep_building / ...
  '4': 'deco_',       // 装饰装修 → deco_indoor / deco_curtain / ...
  '5': 'landscape_',  // 园林绿化 → landscape_park / landscape_road / ...
  '6': 'infra_',      // 基础设施 → infra_transport / infra_water / ...
  '7': 'epc_'         // EPC总承包 → epc_general
}
const qualityOptions = [
  { value: '合格', label: '合格' }, { value: '优良', label: '优良' },
  { value: '省优', label: '省优' }, { value: '国优', label: '国优' },
  { value: '鲁班奖', label: '鲁班奖' }
]
const priorityOptions = [
  { value: '1', label: '低' }, { value: '2', label: '中' },
  { value: '3', label: '高' }, { value: '4', label: '紧急' }
]

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.projectNo) count++
  if (queryParams.projectName) count++
  if (queryParams.projectType) count++
  if (queryParams.status) count++
  if (queryParams.priority) count++
  if (queryParams.managerId) count++
  return count
})

const projectTypeLabel = (v) => projectTypeOptions.find(x => x.value === v)?.label || '-'
const statusLabel = (v) => statusOptions.find(x => x.value === v)?.label || '-'
const statusBadge = (v) => ({ '0': 'amber', '1': 'blue', '2': 'green', '3': 'violet', '4': 'green', '5': 'gray', '6': 'amber', '7': 'red' }[v] || 'gray')
const statusTabClass = (v) => ({ '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-approved', '3': 'tab-partial', '4': 'tab-done', '5': 'tab-void', '6': 'tab-draft', '7': 'tab-reject' }[v] || '')

function getList() {
  loading.value = true
  listProject(queryParams).then(res => {
    projectList.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
    loadPaymentSummary()
  }).catch(() => { loading.value = false })
}

/** 回款汇总：按项目ID建立映射，用于列表「回款进度」列 */
const paymentSummaryMap = ref({})
function loadPaymentSummary() {
  summaryPayment({ pageNum: 1, pageSize: 999 }).then(res => {
    const map = {}
    ;(res.rows || []).forEach(r => { map[r.projectId] = r })
    paymentSummaryMap.value = map
  }).catch(() => {})
}
/** 计算回款比例；无合同额时返回 null */
function payRatioOf(row) {
  const s = paymentSummaryMap.value[row.projectId]
  const contract = Number(s?.contractAmount ?? row.contractAmount ?? 0)
  if (!contract) return null
  return Math.min(Number(s?.receivedAmount || 0) / contract * 100, 100)
}

/** 状态标签数字：统计全部项目 */
function loadStatusCounts() {
  listProject({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total }
    statusOptions.forEach(d => { counts[d.value] = 0 })
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

function handleQuery() { queryParams.pageNum = 1; getList(); loadStatusCounts() }

function resetQuery() {
  queryParams.projectNo = null; queryParams.projectName = null; queryParams.projectType = null
  queryParams.status = null; queryParams.priority = null; queryParams.managerId = null
  activeStatusTab.value = 'all'
  handleQuery()
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.status = status === 'all' ? null : status
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(i => i.projectId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  title.value = '新增项目'
  Object.assign(form, { projectId: null, projectNo: null, projectName: null, projectType: '1', priority: '2', managerId: null, managerName: null, deptId: null, deptName: null, budgetAmount: 0, startDate: null, endDate: null, ownerUnit: null, buildUnit: null, supervisorUnit: null, designUnit: null, projectAddress: null, buildScale: null, contractDuration: null, commenceDate: null, completionDate: null, qualityStandard: null, contractAmount: null, description: null })
  // ★ 初始化附件分类列表 + 采购清单 + 项目团队
  form.attachmentList = []
  form.procurementList = []
  form.memberList = []
  form.wbsTaskList = []
  procActiveType.value = '0'
  detailTab.value = 'base'
  aiParseResult.value = null
  aiParseLoading.value = false
  open.value = true
}

/** 打开项目经理选择弹窗 */
const userPickerRef = ref()
function openUserPicker() { userPickerRef.value.open(form.managerId) }

/** 项目经理选择确认 — 自动带出所在部门 */
function onUserPickerConfirm(user) {
  form.managerId = user.userId
  form.managerName = user.nickName
  if (user.deptId) {
    form.deptId = user.deptId
    form.deptName = user.deptName
  }
}

/** 清除项目经理 */
function clearManager() { form.managerId = null; form.managerName = null; form.deptId = null; form.deptName = null }

function handleUpdate(row) {
  title.value = '修改项目'
  const projectId = row?.projectId || ids.value[0]
  getProject(projectId).then(res => {
    Object.assign(form, res.data)
    handleUpdateAttachmentsAndProcurement(res.data)
    open.value = true
  })
}

function handleView(row) {
  getProject(row.projectId).then(res => {
    viewData.value = res.data || {}
    viewTab.value = 'base'
    viewAttachType.value = '1'
    viewOpen.value = true
  })
}

// 字典值转标签（查看弹窗用）
function dictLabel(options, value) {
  if (value == null || value === '') return ''
  const hit = (options.value || options || []).find(d => String(d.value) === String(value))
  return hit ? hit.label : String(value)
}

/** 附件安全访问 URL（落盘文件为加密存储，经后端 /common/file/access 解密输出，与 FileUpload 组件预览同通道） */
/** 详情弹窗附件预览（全站统一 FilePreview 组件：office 转 PDF/图片/文本在线预览，弹窗内自带下载按钮） */
function handleDetailPreview(row) {
  detailFilePreviewRef.value?.open(row.fileUrl, row.fileName)
}

/** 详情弹窗附件下载（全站统一 downloadFile 工具） */
function handleDetailDownload(row) {
  downloadFile(row.fileUrl)
}

function cancel() { open.value = false }

function handleDelete(row) {
  const projectIds = row?.projectId ? [row.projectId] : ids.value
  ElMessageBox.confirm('确认删除选中的项目?', '提示', { type: 'warning' }).then(() => {
    delProject(projectIds.join(',')).then(() => { ElMessage.success('删除成功'); handleQuery() })
  }).catch(() => {})
}

function handleExport() {
  exportProject(queryParams).then(blob => {
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a'); a.href = url; a.download = '项目立项.xlsx'; a.click()
    window.URL.revokeObjectURL(url)
  })
}

function handleAudit(row) {
  getProject(row.projectId).then(res => {
    auditProjectData.value = res.data || {}
    form.projectId = row.projectId
    form.projectNo = auditProjectData.value.projectNo
    form.projectName = auditProjectData.value.projectName
    auditOpen.value = true; auditStatus.value = '2'; auditOpinion.value = ''
  })
}

/** 草稿/驳回 → 提交审批 */
function handleSubmitAudit(row) {
  ElMessageBox.confirm(`确认将项目「${row.projectName}」提交审批?`, '提示', { type: 'info' }).then(() => {
    submitProject(row.projectId).then(() => { ElMessage.success('已提交，等待审批'); handleQuery() })
  }).catch(() => {})
}

function submitAudit(pass) {
  const status = pass ? '2' : '7'
  if (!pass && !auditOpinion.value.trim()) {
    ElMessage.warning('驳回时请填写审批意见，说明驳回原因')
    return
  }
  auditProject(form.projectId, status, auditOpinion.value).then(() => {
    ElMessage.success(pass ? '审批通过' : '已驳回，项目退回为草稿状态'); auditOpen.value = false; handleQuery()
  })
}

function handleClose(row) {
  ElMessageBox.confirm(`确认将项目「${row.projectName}」结项归档？结项后进度置为 100%，状态变为已完成。`, '项目结项', { type: 'warning' }).then(() => {
    closeProject(row.projectId).then(() => {
      ElMessage.success('结项成功'); handleQuery()
    })
  }).catch(() => {})
}

onMounted(() => { getList(); loadStatusCounts(); loadUsers() })

/** 加载系统用户（组织架构） */
function loadUsers() { listAllUsers().then(res => { userOptions.value = res.rows || [] }) }

// ==================== ★ AI 解析 + 附件 + 采购清单 方法 ====================

/** 附件类型选项：读取字典 pjm_project_attachment_type，可在字典管理中维护 */
const attachmentTypeOptions = computed(() => (pjm_project_attachment_type.value || []).map(d => ({ value: String(d.value), label: d.label })))

/** 保证字典中的每个附件类型都有一个上传分组 */
function ensureAttachmentGroups() {
  attachmentTypeOptions.value.forEach(t => {
    if (!Array.isArray(attFileGroups[t.value])) attFileGroups[t.value] = []
  })
}
watch(attachmentTypeOptions, ensureAttachmentGroups, { immediate: true })

// ★ 级联：项目类型变化时清空已选工程类别（避免残留旧类型的选项）
watch(() => form.projectType, (newType, oldType) => {
  if (newType !== oldType && form.engineeringCategory) {
    const prefix = projectTypeToEngCatPrefix[newType]
    if (prefix && !String(form.engineeringCategory).startsWith(prefix)) {
      form.engineeringCategory = ''
    }
  }
})

/** 从 attFileGroups 中抽取 url 字符串数组给 UploadFile 组件（它只认字符串/逗号串） */
function getAttUrlList(type) {
  const arr = attFileGroups[type] || []
  return arr.map(f => typeof f === 'string' ? f : (f.url || f.name || ''))
}

/** UploadFile emit 回 url 数组后，合并回对象数组（保留已有的 attachmentId/name；新增的从 url 末段取文件名） */
function syncAttFromUpload(type, urlList) {
  // UploadFile 组件 emit 的是逗号拼接字符串（listToString 结果），须先归一化为数组，否则 .map 会抛错并中断组件的 closeLoading
  const urls = Array.isArray(urlList)
    ? urlList
    : String(urlList || '').split(',').map(u => u.trim()).filter(Boolean)
  const arr = attFileGroups[type] || []
  // 旧表按 url 建索引
  const oldMap = new Map()
  arr.forEach(f => {
    const u = typeof f === 'string' ? f : f.url
    if (u) oldMap.set(u, f)
  })
  attFileGroups[type] = urls.map(url => {
    if (oldMap.has(url)) {
      const old = oldMap.get(url)
      return typeof old === 'string'
        ? { url, name: url.split('/').pop() || url }
        : old
    }
    // 新增文件：name 从 url 末段取；没有 attachmentId 就是刚上传的新文件
    return { url, name: url.split('/').pop() || url }
  })
}

/** 把 UploadFile v-model 的文件列表转换为后端 attachmentList 格式 */
function buildAttachmentList() {
  const list = []
  for (const t of attachmentTypeOptions.value) {
    const files = attFileGroups[t.value] || []
    files.forEach((f, i) => {
      list.push({
        attachmentId: f.attachmentId || null,
        attachmentType: t.value,
        fileName: f.name || f.originalFilename || f.fileName,
        fileUrl: f.url,
        fileSize: f.size || f.fileSize || null,
        fileExt: (f.name || '').split('.').pop() || '',
        remark: t.label
      })
    })
  }
  return list
}

/** 编辑时从后端 attachmentList 回填到 UI 分组 */
function syncAttachmentFromBackend(backendList) {
  ensureAttachmentGroups()
  Object.keys(attFileGroups).forEach(key => { attFileGroups[key] = [] })
  ;(backendList || []).forEach(att => {
    const type = att.attachmentType || (attachmentTypeOptions.value[0]?.value ?? '1')
    if (!attFileGroups[type]) attFileGroups[type] = []
    attFileGroups[type].push({
      attachmentId: att.attachmentId,
      name: att.fileName,
      url: att.fileUrl,
      size: att.fileSize,
      uid: att.attachmentId || Date.now() + Math.random()
    })
  })
}

/** AI 解析：打开弹窗 */
function openAiParseDialog(type) {
  aiParseCurrentType.value = type || '1'
  aiParseManualText.value = ''
  aiParseResult.value = null
  // 默认：当前 Tab 有附件则优先从附件解析，否则粘贴文本
  const files = attFileGroups[aiParseCurrentType.value] || []
  aiParseSource.value = files.length > 0 ? 'file' : 'text'
  aiParseDialogOpen.value = true
}

/** 当前选中附件类型下已上传的文件列表 */
const currentTypeUploadedFiles = computed(() => {
  return attFileGroups[aiParseCurrentType.value] || []
})

/** 工程概况字段中英文映射 + 与当前表单值对比 */
const aiProjectFieldLabels = {
  projectName: '项目名称', ownerUnit: '建设单位', buildUnit: '施工单位',
  supervisorUnit: '监理单位', designUnit: '设计单位', customer: '客户/甲方',
  projectAddress: '工程地点', buildScale: '建设规模', contractAmount: '合同总额(元)',
  budgetAmount: '预算金额(元)', contractDuration: '合同工期(天)', startDate: '计划开工日期',
  endDate: '计划竣工日期', qualityStandard: '质量标准', description: '项目描述', projectType: '项目类型'
}
const aiProjectFields = computed(() => {
  const p = aiParseResult.value?.project
  if (!p) return []
  return Object.keys(aiProjectFieldLabels)
    .filter(k => p[k] != null && p[k] !== '')
    .map(k => ({
      key: k,
      label: aiProjectFieldLabels[k],
      value: k === 'projectType' ? projectTypeLabel(String(p[k])) : String(p[k]),
      current: form[k] != null && form[k] !== '' ? String(form[k]) : ''
    }))
})
function teamRoleLabel(v) {
  return teamRoleOptions.find(r => String(r.value) === String(v))?.label || '施工员'
}

/** 从文件对象提取 URL（UploadFile 组件可能存 url 或 response.url） */
function extractFileUrl(f) {
  return f?.url || f?.response?.url || f?.response?.data?.url || ''
}

function doAiParse() {
  const formData = new FormData()
  formData.append('attachmentType', aiParseCurrentType.value)

  if (aiParseSource.value === 'file') {
    // 从已上传附件解析：传 fileUrls
    const urls = currentTypeUploadedFiles.value.map(extractFileUrl).filter(u => u)
    if (urls.length === 0) {
      ElMessage.warning('当前类型暂无有效附件 URL，请改用文本摘要模式')
      return
    }
    formData.append('fileUrls', urls.join(','))
  } else {
    // 文本摘要模式
    if (!aiParseManualText.value.trim()) {
      ElMessage.warning('请粘贴附件中的关键文本摘要')
      return
    }
    formData.append('manualText', aiParseManualText.value.trim())
  }

  aiParseLoading.value = true
  aiParseProject(formData).then(res => {
    if (res.code === 200) {
      aiParseResult.value = res.data
      aiApplyProject.value = true
      aiShowRawJson.value = false
      // 默认全选采购清单和团队成员行（延迟确保表格渲染完成）
      nextTick(() => {
        setTimeout(() => {
          if (Array.isArray(res.data?.procurement) && res.data.procurement.length > 0) {
            aiProcTableRef.value?.toggleAllSelection?.()
            aiSelectedProcurement.value = [...res.data.procurement]
          }
          if (Array.isArray(res.data?.team) && res.data.team.length > 0) {
            aiTeamTableRef.value?.toggleAllSelection?.()
            aiSelectedTeam.value = [...res.data.team]
          }
        }, 100)
      })
      ElMessage.success('AI 解析完成，请核对勾选后一键填入')
    } else {
      ElMessage.error(res.msg || 'AI 解析失败')
    }
  }).catch(err => {
    const msg = err?.message || ''
    if (msg.includes('timeout')) {
      ElMessage.error('解析耗时超过 2 分钟仍未返回，可能是网络波动或 AI 服务繁忙，请点击「开始解析」重试')
    } else if (msg.includes('Network Error') || msg.includes('NetworkError')) {
      ElMessage.error('网络连接异常，请检查网络后点击「开始解析」重试')
    } else {
      ElMessage.error('AI 解析服务暂时不可用，请点击「开始解析」重试；若多次失败请联系管理员')
    }
  }).finally(() => { aiParseLoading.value = false })
}

/** 一键填入表单 */
function applyAiParseResult() {
  if (!aiParseResult.value) return
  const d = aiParseResult.value
  let appliedCount = 0
  // ① 工程概况：用户勾选"覆盖填入"才应用
  if (aiApplyProject.value && d.project) {
    const p = d.project
    if (p.projectName) form.projectName = p.projectName
    if (p.ownerUnit) form.ownerUnit = p.ownerUnit
    if (p.buildUnit) form.buildUnit = p.buildUnit
    if (p.supervisorUnit) form.supervisorUnit = p.supervisorUnit
    if (p.designUnit) form.designUnit = p.designUnit
    if (p.projectAddress) form.projectAddress = p.projectAddress
    if (p.buildScale) form.buildScale = p.buildScale
    if (p.contractAmount != null) {
      form.contractAmount = Number(p.contractAmount)
    } else if (p.budgetAmount != null) {
      // 招标文件通常只有预算金额/最高限价，无合同金额（签约时才确定）；招标阶段默认取预算金额
      form.contractAmount = Number(p.budgetAmount)
    }
    if (p.budgetAmount != null) form.budgetAmount = Number(p.budgetAmount)
    if (p.contractDuration != null) form.contractDuration = Number(p.contractDuration)
    if (p.startDate) form.startDate = p.startDate
    if (p.endDate) form.endDate = p.endDate
    if (p.qualityStandard) form.qualityStandard = p.qualityStandard
    if (p.description) form.description = p.description
    if (p.projectType) form.projectType = String(p.projectType)
    appliedCount += aiProjectFields.value.length
  }
  // ② 采购清单：仅填入用户勾选的行
  if (aiSelectedProcurement.value.length > 0) {
    form.procurementList = aiSelectedProcurement.value.map(x => ({
      detailType: '1',
      materialType: x.materialType != null ? String(x.materialType) : '',
      itemCode: x.itemCode || '',
      itemName: x.itemName || '',
      specModel: x.specModel || '',
      unit: x.unit || '',
      qty: x.qty != null ? Number(x.qty) : null,
      unitPrice: x.unitPrice != null ? Number(x.unitPrice) : null
    }))
    appliedCount += aiSelectedProcurement.value.length
  }
  // ③ 项目团队：仅填入用户勾选的成员
  if (aiSelectedTeam.value.length > 0) {
    form.memberList = aiSelectedTeam.value.map(x => ({
      userName: x.userName || '',
      roleType: x.roleType != null ? String(x.roleType) : '3',
      deptName: x.deptName || '',
      joinDate: x.joinDate || null,
      userId: null  // 后端保存时按姓名自动匹配 sys_user
    })).filter(m => m.userName)
    appliedCount += form.memberList.length
  }
  if (appliedCount === 0) {
    ElMessage.warning('请至少勾选一项要填入的内容')
    return
  }
  aiParseDialogOpen.value = false
  // 填入成功后自动切换到基本信息页签，避免用户手动翻找核对
  detailTab.value = 'base'
  ElMessage.success(`已填入 ${appliedCount} 项内容，请核对后提交`)
}

/** 编辑时从后端回填附件 */
function handleUpdateAttachmentsAndProcurement(resData) {
  syncAttachmentFromBackend(resData.attachmentList || [])
  form.procurementList = (resData.procurementList || []).map(p => ({ ...p, detailType: p.detailType || '1', materialType: p.materialType != null ? String(p.materialType) : '' }))
  // 默认定位到第一个有数据的物料类型页签
  const firstHasData = procTypeOptions.value.find(t => form.procurementList.some(p => procTypeKey(p) === t.value))
  procActiveType.value = firstHasData ? firstHasData.value : (procTypeOptions.value[0]?.value ?? '0')
  form.memberList = resData.memberList || []
  form.wbsTaskList = resData.wbsTaskList || []
}

/** 添加项目团队成员（空行） */
function addMemberRow() {
  if (!form.memberList) form.memberList = []
  form.memberList.push({ userName: '', roleType: '3', deptName: '', joinDate: null, userId: null })
}

/** 移除项目团队成员 */
function removeMemberRow(idx) {
  form.memberList.splice(idx, 1)
}

/** WBS 任务分解：新增/删除 */
function addWbsTaskRow() {
  if (!form.wbsTaskList) form.wbsTaskList = []
  form.wbsTaskList.push({ taskName: '', taskType: '1', ownerName: '', planStartDate: null, planEndDate: null, progress: 0 })
}
function removeWbsTaskRow(idx) {
  form.wbsTaskList.splice(idx, 1)
}

/** 打开成员系统用户选择弹窗 */
function openMemberUserPicker(index) {
  currentMemberIndex.value = index
  const row = form.memberList[index]
  memberUserPickerRef.value?.open(row?.userId)
}

/** 成员系统用户选择确认 — 自动回填 userId/userName/deptId/deptName */
function onMemberUserPickerConfirm(user) {
  const idx = currentMemberIndex.value
  if (idx < 0 || !form.memberList[idx]) return
  const row = form.memberList[idx]
  row.userId = user.userId
  row.userName = user.nickName || user.userName
  if (user.deptId) {
    row.deptId = user.deptId
    if (user.deptName) row.deptName = user.deptName
  }
  ElMessage.success(`已关联系统用户：${row.userName}`)
}

/** 附件总数（用于页签角标） */
const attachmentCount = computed(() => (form.attachmentList || []).length)

/** 采购清单合计 */
const procurementTotal = computed(() => {
  let sum = 0
  ;(form.procurementList || []).forEach(p => {
    if (p.qty != null && p.unitPrice != null) {
      sum += Number(p.qty) * Number(p.unitPrice)
    }
  })
  return sum.toFixed(2)
})

/** 采购清单当前页签的明细行（按物料类型过滤） */
const procRows = computed(() => (form.procurementList || []).filter(p => procTypeKey(p) === procActiveType.value))

/** 各物料类型的条数 */
const procTypeCounts = computed(() => {
  const counts = {}
  procTypeOptions.value.forEach(t => { counts[t.value] = 0 })
  ;(form.procurementList || []).forEach(p => {
    const k = procTypeKey(p)
    if (counts[k] !== undefined) counts[k]++
  })
  return counts
})

/** 当前页签合计 */
const procActiveTotal = computed(() => {
  let sum = 0
  procRows.value.forEach(p => {
    if (p.qty != null && p.unitPrice != null) sum += Number(p.qty) * Number(p.unitPrice)
  })
  return sum.toFixed(2)
})

/** 当前页签名称 */
const procActiveTypeLabel = computed(() => {
  const t = procTypeOptions.value.find(d => d.value === procActiveType.value)
  return t ? t.label : '明细'
})

function addProcurementRow() {
  if (!form.procurementList) form.procurementList = []
  form.procurementList.push({ detailType: '1', materialType: procActiveType.value || '', itemCode: '', itemName: '', specModel: '', unit: '', qty: null, unitPrice: null })
}

function removeProcurementRow(row) {
  const idx = (form.procurementList || []).indexOf(row)
  if (idx > -1) form.procurementList.splice(idx, 1)
}

/** 重算某行合计 */
function recalcProcurementAmount(row) {
  if (row.qty != null && row.unitPrice != null) {
    row.amount = (Number(row.qty) * Number(row.unitPrice)).toFixed(2)
  }
}

/** 打开物料选择弹窗（采购清单某行） */
function openMaterialPicker(row) {
  currentProcurementRow.value = row || null
  materialPickerRef.value?.open(row?.materialId)
}

/** 物料选择确认 — 自动回填编码/名称/规格/单位（peItems 和 procurementList 共用） */
function onMaterialPickerConfirm(material) {
  // ★ 优先判断是否是 peItems 场景（peCurrentPickerIndex >= 0）
  if (peCurrentPickerIndex.value >= 0) {
    onPeMaterialPickerConfirm(material)
    peCurrentPickerIndex.value = -1
    ElMessage.success(`已关联物料：${material.materialCode} ${material.materialName}`)
    return
  }
  const row = currentProcurementRow.value
  if (!row) return
  // 回填：优先用系统物料的标准值
  row.materialId = material.materialId
  row.itemCode = material.materialCode
  row.itemName = material.materialName
  row.specModel = material.specModel
  row.unit = material.unit
  row.materialType = material.materialType != null ? String(material.materialType) : row.materialType
  ElMessage.success(`已关联物料：${material.materialCode} ${material.materialName}`)
}

/** 清除已关联的物料（解除 materialId 绑定，保留手动填写的字段值） */
function clearMaterial(row) {
  if (!row) return
  row.materialId = null
  ElMessage.info('已解除物料关联，可手动编辑字段')
}

/** 重写 submitForm：在提交前组装 attachmentList */
function submitForm() {
  proxy.$refs.projectRef.validate(valid => {
    if (!valid) return
    // ★ 组装后端需要的 attachmentList
    form.attachmentList = buildAttachmentList()
    if (form.projectId != null) {
      updateProject(form).then(() => { ElMessage.success('修改成功'); open.value = false; handleQuery() })
    } else {
      addProject(form).then(() => { ElMessage.success('新增成功'); open.value = false; handleQuery() })
    }
  })
}

// ==================== ★ 项目采购执行（有库存走出库 / 无库存走采购） ====================
const peOpen = ref(false)
const peLoading = ref(false)
const peExecuting = ref(false)
const peItems = ref([])           // {detailType,materialId,itemCode,itemName,specModel,unit,needQty,availQty,outboundQty,purchaseQty,unitPrice}
const peCurrentPickerIndex = ref(-1) // 当前正在选择物料的行索引
const peStockRows = ref([])       // stockCheck 原始返回 [{materialId,warehouseId,warehouseName,availQty}]
const peWarehouseId = ref(null)
const peMode = ref('1')           // 采购模式：1入库后再出库 2直送项目现场
const peTitle = ref('')
const peExpectDate = ref(null)

const peHasOutbound = computed(() => peItems.value.some(r => Number(r.outboundQty) > 0))
const peHasPurchase = computed(() => peItems.value.some(r => Number(r.purchaseQty) > 0))
/** 存在出库行但未做仓库分配（将回退到默认仓库） */
const peHasUnallocated = computed(() => peItems.value.some(r => Number(r.outboundQty) > 0 && (!r.outboundAlloc || !r.outboundAlloc.length)))
const peSubmitDisabled = computed(() => {
  if (peExecuting.value) return true
  if (!peHasOutbound.value && !peHasPurchase.value) return true
  if (peHasUnallocated.value && !peWarehouseId.value) return true
  return false
})

/** 仓库下拉选项：聚合有库存的仓库，并标记是否对所有出库物料库存充足 */
const peWarehouseOptions = computed(() => {
  const map = new Map()
  peStockRows.value.forEach(s => {
    if (!map.has(s.warehouseId)) map.set(s.warehouseId, { warehouseId: s.warehouseId, warehouseName: s.warehouseName || ('仓库' + s.warehouseId) })
  })
  const outboundRows = peItems.value.filter(r => Number(r.outboundQty) > 0 && r.materialId)
  return Array.from(map.values()).map(w => {
    const sufficient = outboundRows.every(r => warehouseAvail(r.materialId, w.warehouseId) >= Number(r.outboundQty))
    return { ...w, sufficient }
  })
})

/** 某物料在某仓库的可用量 */
function warehouseAvail(materialId, warehouseId) {
  return peStockRows.value
    .filter(s => s.materialId === materialId && s.warehouseId === warehouseId)
    .reduce((sum, s) => sum + Number(s.availQty || 0), 0)
}

// ========== ★ 出库仓库分配（同一物料跨多仓库） ==========
const peAllocOpen = ref(false)
const peAllocRowIndex = ref(-1)
const peAllocRows = ref([])       // [{warehouseId,warehouseName,availQty,qty}]
const peAllocTarget = computed(() => peItems.value[peAllocRowIndex.value] || {})
const peAllocItemName = computed(() => peAllocTarget.value.itemName || '出库物料')
const peAllocNeedQty = computed(() => Number(peAllocTarget.value.needQty) || 0)
const peAllocTotal = computed(() => peAllocRows.value.reduce((s, r) => s + (Number(r.qty) || 0), 0))
const peAllocRemain = computed(() => Math.round((peAllocNeedQty.value - peAllocTotal.value) * 100) / 100)
/** 出库仓库展示文本 */
function peAllocText(row) {
  if (!row.outboundAlloc || !row.outboundAlloc.length) return ''
  return row.outboundAlloc.map(a => `${a.warehouseName} × ${a.qty}`).join('；')
}
/** 打开分配弹窗：列出该物料所有有库存仓库 */
function openPeAlloc(index) {
  const row = peItems.value[index]
  if (!row || !row.materialId) return
  peAllocRowIndex.value = index
  const whRows = peStockRows.value
    .filter(s => s.materialId === row.materialId)
    .reduce((arr, s) => {
      const hit = arr.find(x => x.warehouseId === s.warehouseId)
      if (hit) hit.availQty += Number(s.availQty || 0)
      else arr.push({ warehouseId: s.warehouseId, warehouseName: s.warehouseName || ('仓库' + s.warehouseId), availQty: Number(s.availQty || 0), qty: 0 })
      return arr
    }, [])
  // 已有分配则回填；否则把当前出库数量默认放到可用库存最大的仓库
  if (row.outboundAlloc && row.outboundAlloc.length) {
    whRows.forEach(w => {
      const hit = row.outboundAlloc.find(a => a.warehouseId === w.warehouseId)
      w.qty = hit ? Number(hit.qty) : 0
    })
  } else if (Number(row.outboundQty) > 0 && whRows.length) {
    const best = whRows.reduce((a, b) => (b.availQty > a.availQty ? b : a), whRows[0])
    best.qty = Math.min(Number(row.outboundQty), best.availQty)
  }
  peAllocRows.value = whRows
  peAllocOpen.value = true
}
/** ★ 出库仓库自动分配：按各仓库可用库存贪心拆分（单仓足量优先，不足自动跨仓累加） */
function autoAllocPe(row) {
  const rows = peStockRows.value
    .filter(s => s.materialId === row.materialId)
    .reduce((arr, s) => {
      const hit = arr.find(x => x.warehouseId === s.warehouseId)
      if (hit) hit.availQty += Number(s.availQty || 0)
      else arr.push({ warehouseId: s.warehouseId, warehouseName: s.warehouseName || ('仓库' + s.warehouseId), availQty: Number(s.availQty || 0), qty: 0 })
      return arr
    }, [])
    .sort((a, b) => (Number(b.availQty) || 0) - (Number(a.availQty) || 0))
  let remain = Number(row.outboundQty) || 0
  rows.forEach(w => {
    const take = Math.min(remain, Number(w.availQty) || 0)
    w.qty = Math.max(0, Math.round(take * 100) / 100)
    remain = Math.round((remain - w.qty) * 100) / 100
  })
  row.outboundAlloc = rows.filter(w => Number(w.qty) > 0)
    .map(w => ({ warehouseId: w.warehouseId, warehouseName: w.warehouseName, qty: Number(w.qty) }))
}

/** 确认分配 → 回写该行出库数量与仓库明细 */
function confirmPeAlloc() {
  const row = peItems.value[peAllocRowIndex.value]
  if (!row) return
  const alloc = peAllocRows.value
    .filter(w => Number(w.qty) > 0)
    .map(w => ({ warehouseId: w.warehouseId, warehouseName: w.warehouseName, qty: Number(w.qty) }))
  const total = alloc.reduce((s, a) => s + a.qty, 0)
  if (total > peAllocNeedQty.value) {
    ElMessage.warning('分配数量合计不能超过需求数量')
    return
  }
  row.outboundAlloc = alloc.length ? alloc : null
  row.outboundQty = Math.round(total * 100) / 100
  row.purchaseQty = Math.round((Number(row.needQty) - row.outboundQty) * 100) / 100
  peAllocOpen.value = false
}

/** 打开采购执行弹窗：提取有效清单行 → 库存检查 → 默认拆分（优先出库） */
function openPurchaseExecute() {
  const rows = (form.procurementList || []).filter(p => p.itemName && Number(p.qty) > 0)
  if (rows.length === 0) {
    ElMessage.warning('采购清单为空或数量均为 0，请先完善采购清单')
    return
  }
  peItems.value = rows.map(p => ({
    detailType: p.detailType || '1',
    materialType: p.materialType != null ? String(p.materialType) : '',
    materialId: p.materialId || null,
    itemCode: p.itemCode, itemName: p.itemName, specModel: p.specModel, unit: p.unit,
    needQty: Number(p.qty), availQty: 0, outboundQty: 0, purchaseQty: Number(p.qty),
    unitPrice: p.unitPrice
  }))
  // ★ 出库仓库自动分配：库存加载后按各仓库可用库存自动拆分
  peItems.value.forEach(r => { if (Number(r.outboundQty) > 0 && r.materialId) autoAllocPe(r) })
  peMode.value = '1'
  // ★ 申请标题默认值：项目名称-采购申请-日期（可手动修改）
  const _d = new Date()
  const _today = _d.getFullYear() + '-' + String(_d.getMonth() + 1).padStart(2, '0') + '-' + String(_d.getDate()).padStart(2, '0')
  peTitle.value = form.projectName ? `${form.projectName}-采购申请-${_today}` : ''
  peExpectDate.value = null
  peStockRows.value = []
  peOpen.value = true

  const materialIds = [...new Set(rows.filter(r => r.materialId).map(r => r.materialId))]
  if (materialIds.length === 0) return
  peLoading.value = true
  stockCheckProjectPurchase({ materialIds }).then(res => {
    peStockRows.value = res.data || []
    // 默认拆分：有库存优先出库（出库 = min(需求, 总可用)）
    peItems.value.forEach(r => {
      if (!r.materialId) return
      r.availQty = peStockRows.value
        .filter(s => s.materialId === r.materialId)
        .reduce((sum, s) => sum + Number(s.availQty || 0), 0)
      r.outboundQty = Math.min(r.needQty, r.availQty)
      r.purchaseQty = Math.round((r.needQty - r.outboundQty) * 100) / 100
    })
    // 默认选中库存充足的仓库
    const opt = peWarehouseOptions.value
    const best = opt.find(w => w.sufficient) || opt[0]
    if (best) peWarehouseId.value = best.warehouseId
  }).finally(() => { peLoading.value = false })
}

/** 出库数量变化 → 采购数量 = 需求 - 出库；清空分配后按各仓库可用库存自动重新分配 */
function onPeOutboundChange(row) {
  const max = Math.min(row.needQty, row.availQty)
  if (Number(row.outboundQty) > max) row.outboundQty = max
  if (Number(row.outboundQty) < 0 || row.outboundQty == null) row.outboundQty = 0
  row.outboundAlloc = null
  row.purchaseQty = Math.round((row.needQty - Number(row.outboundQty)) * 100) / 100
  autoAllocPe(row)
}

// ====== ★ 采购明细类型相关函数 ======
/** 明细类型切换 — 物料明细才查库存/走出库；其他 3 类一律只走采购 */
function onPeMaterialTypeChange(row) {
  row.outboundAlloc = null
  if (row.materialId) {
    // 类型切换后解除物料关联，需重新选择对应类型的物料
    row.materialId = null; row.itemCode = ''; row.specModel = ''; row.unit = ''
    row.availQty = 0; row.outboundQty = 0
    ElMessage.info('物料类型已切换，请重新选择物料')
  }
  row.purchaseQty = Number(row.needQty) || 0
}
/** 打开物料浏览框（peItems 某行） */
function openPeMaterialPicker(index) {
  peCurrentPickerIndex.value = index
  proxy.$refs.materialPickerRef?.open?.() || (() => { proxy.$refs.materialPickerRef.visible = true })()
}
/** 清除 peItems 某行已关联的物料（库存/出库数据同步归零，数量全部计入采购） */
function clearPeMaterial(index) {
  const row = peItems.value[index]
  if (!row) return
  row.materialId = null
  row.itemCode = ''
  row.itemName = ''
  row.specModel = ''
  row.unit = ''
  row.availQty = 0
  row.outboundQty = 0
  row.outboundAlloc = null
  row.purchaseQty = Math.round((Number(row.needQty) || 0) * 100) / 100
  ElMessage.info('已解除物料关联')
}
/** 物料浏览框确认 — 回填 peItems 当前行 */
function onPeMaterialPickerConfirm(material) {
  const idx = peCurrentPickerIndex.value
  if (idx < 0 || !peItems.value[idx]) return
  const row = peItems.value[idx]
  row.materialId = material.materialId
  row.itemCode = material.materialCode
  row.itemName = material.materialName
  row.specModel = material.specModel
  row.unit = material.unit
  row.materialType = material.materialType != null ? String(material.materialType) : row.materialType
  row.outboundAlloc = null
  // 刷新库存（重新拉取该物料所有仓库库存，确保跨仓库分配可用）
  if (material.materialId) {
    stockCheckProjectPurchase({ materialIds: [material.materialId] }).then(res => {
      const rows = res.data || []
      peStockRows.value = peStockRows.value.filter(s => s.materialId !== material.materialId).concat(rows)
      row.availQty = rows.reduce((sum, s) => sum + Number(s.availQty || 0), 0)
      row.outboundQty = Math.min(Number(row.needQty) || 0, row.availQty)
      row.purchaseQty = Math.round(((row.needQty || 0) - row.outboundQty) * 100) / 100
    })
  }
}
/** 新增一行明细 */
function addPeItem(materialType) {
  peItems.value.push({
    detailType: '1', materialType: materialType || '', materialId: null,
    itemCode: '', itemName: '', specModel: '', unit: '',
    needQty: 1, availQty: 0, outboundQty: 0, purchaseQty: 1,
    unitPrice: 0
  })
}
/** 删除一行 */
function removePeItem(index) {
  peItems.value.splice(index, 1)
}

/** 提交执行 */
function submitPurchaseExecute() {
  // 出库明细：按仓库分配展开，未分配的行回退到默认仓库
  const outboundItems = []
  for (const r of peItems.value) {
    if (Number(r.outboundQty) <= 0) continue
    const base = { materialId: r.materialId, materialCode: r.itemCode, materialName: r.itemName, specModel: r.specModel, unit: r.unit }
    if (r.outboundAlloc && r.outboundAlloc.length) {
      r.outboundAlloc.forEach(a => {
        if (Number(a.qty) > 0) outboundItems.push({ ...base, qty: a.qty, warehouseId: a.warehouseId })
      })
    }
  }
  const purchaseItems = peItems.value.filter(r => Number(r.purchaseQty) > 0).map(r => ({
    materialId: r.materialId, materialCode: r.itemCode, materialName: r.itemName,
    specModel: r.specModel, unit: r.unit, qty: r.purchaseQty, estimatedPrice: r.unitPrice
  }))
  if (purchaseItems.length > 0 && !peMode.value) {
    ElMessage.warning('请选择采购模式')
    return
  }
  peExecuting.value = true
  executeProjectPurchase({
    projectId: form.projectId,
    outboundItems,
    purchaseMode: peMode.value,
    purchaseTitle: peTitle.value || null,
    expectDate: peExpectDate.value,
    purchaseItems
  }).then(res => {
    const d = res.data || {}
    let html = ''
    const orders = d.outboundOrders || []
    if (orders.length) {
      html += `<p>✅ 已生成 WMS 领料出库单（草稿）共 <b>${orders.length}</b> 张：</p>`
      orders.forEach(o => { html += `<p style="margin:2px 0 2px 12px;">· ${o.warehouseName}：<b>${o.outboundOrderNo}</b></p>` })
      html += `<p style="font-size:12px;color:#909399;">请前往「仓库管理 → 出库管理」提交并安排拣货</p>`
    } else if (d.outboundOrderNo) {
      html += `<p>✅ 已生成 WMS 出库单（领料出库，草稿）：<b>${d.outboundOrderNo}</b></p>`
    }
    if (d.requestNo) html += `<p>✅ 已生成 PMS 采购申请（草稿）：<b>${d.requestNo}</b><br><span style="font-size:12px;color:#909399;">${peMode.value === '2' ? '直送项目现场：收货验收后自动登记耗用，不进入仓库' : '入库后再出库：采购到货入仓库后，再从仓库出库到项目'}；请前往「采购管理 → 采购申请」提交审批</span></p>`
    ElMessageBox.alert(html || '执行成功', '执行成功', { dangerouslyUseHTMLString: true, confirmButtonText: '知道了' })
    peOpen.value = false
  }).finally(() => { peExecuting.value = false })
}
</script>

<style scoped>
.pjm-project-page .rd-card-body :deep(.el-form-item) { margin-bottom: 22px; }
.pe-section-hint { margin-left: 12px; font-size: 12px; color: #909399; }
.pjm-project-page .rd-card-body :deep(.el-row:last-child .el-form-item) { margin-bottom: 0; }
.pjm-project-page .field-hint { font-size: 12px; color: var(--ink-400); line-height: 1.6; }

/* ★ AI 解析二次确认区 */
.ai-confirm-area { max-height: 420px; overflow-y: auto; border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px 14px; background: #fafbfc; }
.ai-confirm-head { display: flex; justify-content: space-between; align-items: center; font-weight: 600; font-size: 13px; color: var(--ink-600); margin-bottom: 10px; }
.ai-section { margin-bottom: 16px; }
.ai-section:last-child { margin-bottom: 0; }
.ai-section-title { font-size: 13px; font-weight: 600; color: var(--ink-600); margin-bottom: 8px; }
.ai-section :deep(.el-table) { font-size: 12px; }

/* ★ 项目采购执行弹窗 */
.pe-section { margin-top: 14px; border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px 14px 0; background: #fafbfc; }
.pe-section-title { font-size: 13px; font-weight: 600; color: var(--ink-600); margin-bottom: 10px; }

/* ★ 采购清单明细类型页签 */
.proc-tabs { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; margin-bottom: 10px; }
.proc-tab { display: inline-flex; align-items: center; gap: 6px; height: 30px; padding: 0 12px; border: 1px solid #e5e7eb; border-radius: 999px; background: #fff; font-size: 13px; color: #6b7280; cursor: pointer; transition: .2s ease; }
.proc-tab .dot { width: 6px; height: 6px; border-radius: 50%; background: #cbd5e1; }
.proc-tab .count { min-width: 18px; height: 18px; padding: 0 5px; border-radius: 9px; background: #f1f5f9; color: #64748b; font-size: 12px; line-height: 18px; text-align: center; }
.proc-tab:hover { border-color: #c7d2fe; color: #4338ca; }
.proc-tab.is-active { border-color: #a5b4fc; background: #eef2ff; color: #4338ca; font-weight: 600; }
.proc-tab.is-active .count { background: #fff; color: #4f46e5; }
.proc-tab.t0 .dot { background: #6366f1; }
.proc-tab.t1 .dot { background: #f59e0b; }
.proc-tab.t2 .dot { background: #10b981; }
.proc-tab.t3 .dot { background: #94a3b8; }
.proc-tab.t4 .dot { background: #ec4899; }
.proc-tab.t5 .dot { background: #14b8a6; }

/* ★ 详情区外层 el-tabs（工单详情式页签） */
.detail-tabs {
  margin-top: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  overflow: hidden;
}
.project-detail-tabs { border: 0; }
.project-detail-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 16px;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
  border-bottom: 1px solid #e5e7eb;
}
.project-detail-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; background: #e5e7eb; }
.project-detail-tabs :deep(.el-tabs__item) {
  height: 40px;
  line-height: 40px;
  font-size: 14px;
  font-weight: 500;
  color: var(--ink-500);
  padding: 0 12px;
  margin: 6px 6px 0;
  min-width: 140px;
  text-align: center;
  border-radius: 8px 8px 0 0;
  transition: background .2s, color .2s;
}
.project-detail-tabs :deep(.el-tabs__item:hover) { color: var(--brand-600); background: #f5f8ff; }
/* 激活页签：浅蓝底 + 品牌蓝字 + 底部强调线，轻量突出（新增/详情弹窗共用） */
.project-detail-tabs :deep(.el-tabs__item.is-active) {
  color: var(--brand-600);
  font-weight: 600;
  background: #e8f1ff;
  border-bottom: 2px solid var(--brand-600);
}
.project-detail-tabs :deep(.el-tabs__item.is-active:hover) { color: var(--brand-600); background: #e8f1ff; }
.project-detail-tabs :deep(.el-tabs__item.is-active .dtab-badge) { background: #fff; color: var(--brand-600); }
.project-detail-tabs :deep(.el-tabs__active-bar) { display: none; }
.project-detail-tabs :deep(.el-tabs__content) { padding: 0; }
.project-detail-tabs :deep(.el-tab-pane) { padding: 0; }

.dtab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.dtab-label .el-icon { font-size: 14px; }
.dtab-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background: #eef2ff;
  color: #4f46e5;
  font-size: 11px;
  font-weight: 600;
  line-height: 20px;
  text-align: center;
}
.dtab-badge.amount {
  background: #fff7ed;
  color: #c2410c;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.3px;
}

.dtab-body { padding: 16px 20px; }

/* 基本信息 tab 内的分区块 */
.form-section {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 14px 18px 2px;
  margin-bottom: 14px;
  background: #fafbfc;
}
.form-section:last-child { margin-bottom: 0; }
.form-section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-600);
}
.form-section-title .el-icon { color: var(--brand-600); font-size: 15px; }

.dtab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #e5e7eb;
}
.dtab-hint { font-size: 12px; color: var(--ink-400); line-height: 1.6; }

/* 内部附件 border-card tabs 修正 */
.attach-inner-tabs :deep(.el-tabs__item) { height: 34px; line-height: 32px; font-size: 13px; }
/* 附件上传区：内容留白，按钮/提示不再顶着左边缘 */
.attach-inner-tabs :deep(.el-tabs__content) { padding: 16px 18px; }
.attach-inner-tabs :deep(.el-tab-pane) { padding: 2px 2px 4px; }
.attach-inner-tabs :deep(.upload-file-uploader) { margin-bottom: 12px; }
.attach-inner-tabs :deep(.el-upload__tip) {
  padding: 8px 12px;
  background: #f8fafc;
  border: 1px dashed #e2e8f0;
  border-radius: 6px;
  line-height: 1.8;
  margin-bottom: 12px;
}
.attach-inner-tabs :deep(.upload-file-list .el-upload-list__item) { padding: 6px 10px; border-radius: 6px; }
</style>

<style>
/* 项目立项：新增/详情弹窗自适应铺满内容区（append-to-body 弹窗需非 scoped 样式）
   左侧定位在侧边栏（约152px）右侧，右侧贴近视口边缘，宽度 calc(100vw - 172px) */
.pj-wide-dialog {
  margin-left: 164px !important;
  margin-right: 8px !important;
}
.pj-wide-dialog .el-dialog__body {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}
</style>

<template>
  <div class="app-container qms-std-page">
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
          <label>标准编码</label>
          <div class="control">
            <el-input v-model="queryParams.stdCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>检验项目</label>
          <div class="control">
            <el-input v-model="queryParams.stdName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>判定规则</label>
          <div class="control is-select">
            <el-select v-model="queryParams.judgeRule" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in qms_judge_rule" :key="d.value" :label="d.label" :value="d.value" />
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
          <label>检验方法</label>
          <div class="control">
            <el-input v-model="queryParams.inspectMethod" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>备注</label>
          <div class="control">
            <el-input v-model="queryParams.remark" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Status Tabs + Tip Pill -->
      <div class="status-tabs">
        <div class="tabs-track"></div>
        <button class="tip-pill" @click="showHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:std:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['qms:std:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['qms:std:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:std:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_std_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
<el-table-column type="selection" width="55" align="center" />
<el-table-column type="index" label="序号" width="85" align="center" />
<el-table-column label="标准编码" prop="stdCode" key="stdCode" :width="colWidth('stdCode', 140)" resizable show-overflow-tooltip v-if="columns.stdCode.visible" />          <el-table-column label="检验项目" prop="stdName" key="stdName" :width="colWidth('stdName', 180)" resizable show-overflow-tooltip v-if="columns.stdName.visible" />
          <el-table-column label="检验方法" prop="inspectMethod" key="inspectMethod" :width="colWidth('inspectMethod', 200)" resizable show-overflow-tooltip v-if="columns.inspectMethod.visible" />
          <el-table-column label="计量单位" prop="unit" key="unit" :width="colWidth('unit', 100)" resizable align="center" v-if="columns.unit.visible" />
          <el-table-column label="标准下限" prop="valMin" key="valMin" :width="colWidth('valMin', 100)" resizable align="center" v-if="columns.valMin.visible" />
          <el-table-column label="标准上限" prop="valMax" key="valMax" :width="colWidth('valMax', 100)" resizable align="center" v-if="columns.valMax.visible" />
          <el-table-column label="目标值" prop="targetVal" key="targetVal" :width="colWidth('targetVal', 100)" resizable align="center" v-if="columns.targetVal.visible" />
          <el-table-column label="判定规则" prop="judgeRule" key="judgeRule" :width="colWidth('judgeRule', 100)" resizable align="center" v-if="columns.judgeRule.visible">
            <template #default="scope"><span class="badge violet">{{ judgeRuleLabel(scope.row.judgeRule) }}</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 80)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope"><span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}</span></template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:std:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:std:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="stdRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="15" x2="15" y2="15"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_basic">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="标准编码" prop="stdCode"><el-input v-model="form.stdCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="检验项目" prop="stdName"><el-input v-model="form.stdName" placeholder="请输入检验项目" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="检验方法" prop="inspectMethod"><el-input v-model="form.inspectMethod" placeholder="请输入检验方法" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计量单位" prop="unit"><el-input v-model="form.unit" placeholder="如：mm、kg、℃、%等" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_std')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>标准值</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_std }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_std">
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="标准下限" prop="valMin"><el-input-number v-model="form.valMin" :precision="4" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="标准上限" prop="valMax"><el-input-number v-model="form.valMax" :precision="4" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="目标值" prop="targetVal"><el-input-number v-model="form.targetVal" :precision="4" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_judge')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>判定配置</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_judge }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_judge">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="判定规则" prop="judgeRule"><el-select v-model="form.judgeRule" style="width: 100%"><el-option v-for="d in qms_judge_rule" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_other')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_other">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <span class="rd-detail-header-title">检验标准详情</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="15" x2="15" y2="15"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_basic" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">标准编码</span><div class="rd-value">{{ viewData.stdCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">检验项目</span><div class="rd-value">{{ viewData.stdName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">检验方法</span><div class="rd-value">{{ viewData.inspectMethod || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">计量单位</span><div class="rd-value">{{ viewData.unit || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">判定规则</span><div class="rd-value"><dict-tag :options="qms_judge_rule" :value="viewData.judgeRule" /></div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_std')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>标准值</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_std }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_std" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">标准下限</span><div class="rd-value">{{ viewData.valMin != null ? viewData.valMin : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">标准上限</span><div class="rd-value">{{ viewData.valMax != null ? viewData.valMax : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">目标值</span><div class="rd-value">{{ viewData.targetVal != null ? viewData.targetVal : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value">{{ viewData.status === '0' ? '正常' : '停用' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('v_remark')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_remark }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_remark" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div></div></div>
        </section>
      </div>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showHelp" title="检验标准库业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <!-- 一、检验标准库释义 -->
        <h4>一、检验标准库释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是检验标准库？</div>
          <div class="highlight-card-body">
            <strong>检验标准库</strong>是质量管理系统的核心主数据之一，用于统一定义和维护各检验项目的技术标准、检验方法和判定规则。每条标准包含检验项目名称、检验方法、计量单位、标准值（上限/下限/目标值）及判定规则，是IQC来料检验、IPQC过程检验、FQC成品检验和OQC出货检验的标准依据。<br/><br/>
            检验标准库的设计原则是<strong>统一维护、集中管理、自动引用</strong>——检验任务中选择物料后，系统根据物料质量属性自动带出对应检验标准与项目，确保各环节执行一致的检验标准，杜绝标准散落Excel/纸质、执行不一致的问题。
          </div>
        </div>

        <!-- 二、新增/修改表单填写指南 -->
        <h4>二、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>标准编码：</strong>检验标准的唯一标识编号。保存后由系统自动生成，无需手动输入</p>
            <p>• <strong>检验项目：</strong>必填项，输入检验项目名称，如"水分含量"、"拉伸强度"、"外观检查"等。命名应清晰、准确，便于检索和引用</p>
            <p>• <strong>检验方法：</strong>描述具体的检验操作方法，如"GB/T 5009.3-2016"、"目视检查"、"千分尺测量"等。建议引用国标或企业标准编号</p>
            <p>• <strong>计量单位：</strong>文本输入，如 mm、kg、℃、% 等。定性判定项目可不填</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">标准值区</div>
          <div class="highlight-card-body">
            <p>标准值区用于定义检验项目的合格范围或目标值，配合判定规则使用。不同判定规则需要填写的字段不同：</p>
            <p>• <strong>标准下限：</strong>合格范围的最小值，区间判定时必填。实测值 ≥ 下限方为合格</p>
            <p>• <strong>标准上限：</strong>合格范围的最大值，区间判定时必填。实测值 ≤ 上限方为合格</p>
            <p>• <strong>目标值：</strong>单值判定时的标准目标值，实测值需等于目标值方为合格</p>
            <p style="padding-left: 16px;">— <strong>区间判定：</strong>需设置标准下限和标准上限</p>
            <p style="padding-left: 16px;">— <strong>单值判定：</strong>需设置目标值</p>
            <p style="padding-left: 16px;">— <strong>定性判定：</strong>不需设置标准值，合格/不合格由人工判定</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">判定配置区</div>
          <div class="highlight-card-body">
            <p>• <strong>判定规则：</strong>选择检验结果的判定方式</p>
            <p style="padding-left: 16px;">— <strong>区间判定：</strong>实测值需在「标准下限」和「标准上限」之间方为合格</p>
            <p style="padding-left: 16px;">— <strong>单值判定：</strong>实测值需等于「目标值」方为合格</p>
            <p style="padding-left: 16px;">— <strong>定性判定：</strong>用于定性检验项目，如外观检查，合格/不合格由人工判定，不需设置标准值</p>
            <p>• <strong>状态：</strong>正常状态下可被检验任务引用，停用后不可引用。不再使用的标准建议设为"停用"而非删除</p>
          </div>
        </div>

        <!-- 三、各判定规则说明 -->
        <h4>三、各判定规则说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="区间判定">适用于有明确合格范围的检验项目，如尺寸（φ10±0.05mm）、重量（50±0.5kg）、温度（20~25℃）等。需设置标准下限和标准上限，实测值在区间内方为合格</el-descriptions-item>
          <el-descriptions-item label="单值判定">适用于有精确目标值的检验项目，如pH值（=7.0）、配比比例等。需设置目标值，实测值等于目标值方为合格</el-descriptions-item>
          <el-descriptions-item label="定性判定">适用于无法量化的检验项目，如外观色泽、表面光洁度、包装完好性等。不需设置标准值，检验员根据经验标准判定合格/不合格</el-descriptions-item>
        </el-descriptions>

        <!-- 四、业务操作流程 -->
        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新增标准：</strong>点击「新增」按钮，填写检验项目、检验方法、计量单位，根据判定规则设置标准值，保存后系统自动生成标准编码
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>修改标准：</strong>选中一条记录点击「修改」，可编辑检验标准信息。已被检验任务引用的标准建议谨慎修改，避免影响历史检验数据
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>引用使用：</strong>检验任务中选择物料后，系统根据物料质量属性自动带出对应检验标准与项目，检验员按标准执行检验并录入实测值
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>停用标准：</strong>不再使用的标准可设为"停用"状态。停用后检验任务不再引用该标准，但已有检验记录不受影响
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>删除标准：</strong>未被检验任务引用的标准可删除，删除后不可恢复。已被引用的标准需先停用，确认无引用后方可删除
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsStd">
import { listStd, getStd, addStd, updateStd, delStd } from '@/api/qms/std'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_std_index')
const { collapsedCards, toggleCard } = useDetailCard(['e_basic', 'e_std', 'e_judge', 'e_other', 'v_basic', 'v_std', 'v_remark'])
const { qms_judge_rule } = proxy.useDict('qms_judge_rule')

function judgeRuleLabel(rule) {
  const item = qms_judge_rule.value.find(d => d.value == rule)
  return item ? item.label : '-'
}

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const showHelp = ref(false)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const defaultColumns = { stdCode: { label: '标准编码', visible: true }, stdName: { label: '检验项目', visible: true }, inspectMethod: { label: '检验方法', visible: true }, unit: { label: '计量单位', visible: true }, valMin: { label: '标准下限', visible: true }, valMax: { label: '标准上限', visible: true }, targetVal: { label: '目标值', visible: true }, judgeRule: { label: '判定规则', visible: true }, status: { label: '状态', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('qms_std_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, stdCode: undefined, stdName: undefined, judgeRule: undefined, status: undefined, inspectMethod: undefined, remark: undefined, params: {} },
  rules: {
    stdName: [{ required: true, message: '检验项目不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.stdCode) count++
  if (queryParams.value.stdName) count++
  if (queryParams.value.judgeRule) count++
  if (queryParams.value.status) count++
  if (queryParams.value.inspectMethod) count++
  if (queryParams.value.remark) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() { loading.value = true; listStd(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.stdCode = undefined; queryParams.value.stdName = undefined; queryParams.value.judgeRule = undefined; queryParams.value.status = undefined; queryParams.value.inspectMethod = undefined; queryParams.value.remark = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.stdId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { stdId: undefined, stdCode: undefined, stdName: undefined, inspectMethod: undefined, unit: undefined, valMin: undefined, valMax: undefined, targetVal: undefined, judgeRule: '1', status: '0', remark: undefined }; proxy.resetForm('stdRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加检验标准' }
function handleUpdate(row) { reset(); getStd(row.stdId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改检验标准' }) }
function handleView(row) { getStd(row.stdId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function submitForm() { proxy.$refs['stdRef'].validate(valid => { if (valid) { if (form.value.stdId != undefined) { updateStd(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addStd(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const stdIds = row.stdId || ids.value; proxy.$modal.confirm('确认删除编号为"' + stdIds + '"的数据？').then(() => delStd(stdIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('qms/std/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `std_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

getList()
onActivated(() => { getList() })
</script>

<style scoped>
.qms-std-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --violet-50:#f5f3ff; --violet-500:#8b5cf6; --violet-700:#6d28d9; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-std-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-std-page .filter-card { padding:14px 20px 16px; }
.qms-std-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-std-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-std-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-std-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-std-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-std-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-std-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-std-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-std-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-std-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-std-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-std-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-std-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.qms-std-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-std-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-std-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-std-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-std-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-std-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-std-page .field .control :deep(.el-select) { width:100%; }
.qms-std-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-std-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-std-page .field .control :deep(.el-date-editor) { width:100%; }
.qms-std-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.qms-std-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.qms-std-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.qms-std-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-std-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-std-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-std-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-std-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-std-page .btn-soft .el-icon { font-size:14px; }
.qms-std-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-std-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-std-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-std-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-std-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-std-page .table-wrap { overflow-x:auto; }
.qms-std-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-std-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-std-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-std-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-std-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-std-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-std-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.qms-std-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.qms-std-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.qms-std-page .badge.green .dot { background:var(--green-500); }
.qms-std-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.qms-std-page .badge.gray .dot { background:var(--ink-400); }
.qms-std-page .badge.violet { background:var(--violet-50); color:var(--violet-700); border-color:#ddd6fe; }
.qms-std-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.qms-std-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.qms-std-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.qms-std-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
/* ===== Status Tabs + Tip Pill ===== */
.qms-std-page .status-tabs { display:flex; align-items:center; justify-content:space-between; padding:10px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-std-page .status-tabs .tabs-track { display:flex; align-items:center; gap:8px; }
.qms-std-page .status-tabs .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.qms-std-page .status-tabs .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }
/* ===== 业务操作说明对话框 ===== */
.status-help-content { max-height: 520px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-help-content .status-flow { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; padding: 16px; background-color: #f5f7fa; border-radius: 8px; margin-bottom: 8px; }
.status-help-content .flow-item { display: flex; align-items: center; gap: 8px; }
.status-help-content .flow-arrow { color: #909399; font-size: 16px; }
.status-help-content .highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.status-help-content .highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.status-help-content .highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
.status-help-content .highlight-card-body p { margin: 4px 0; }
.status-help-content .highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color: #409eff; }
.status-help-content .highlight-success { background-color: #f0f9eb; border-color: #b3e19d; }
.status-help-content .highlight-success .highlight-card-title { color: #67c23a; }
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }
.status-help-content .highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.status-help-content .highlight-danger .highlight-card-title { color: #f56c6c; }
@media (max-width:1100px) { .qms-std-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-std-page .filter-card .filter-bar { grid-template-columns:1fr; } .qms-std-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>

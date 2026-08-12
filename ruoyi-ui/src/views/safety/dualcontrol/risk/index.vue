<template>
  <div class="app-container safety-risk-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>风险点名称</label>
          <div class="control">
            <el-input v-model="queryParams.riskName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>风险点编号</label>
          <div class="control">
            <el-input v-model="queryParams.riskCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>风险等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.riskLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_risk_level" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>管控层级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.controlLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_control_level" :key="dict.value" :label="dict.label" :value="dict.value" />
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
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:risk:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:risk:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Upload" @click="handleExport" v-hasPermi="['safety:risk:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_risk_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="riskList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="风险点编号" prop="riskCode" key="riskCode" :width="colWidth('riskCode', 130)" resizable v-if="columns.riskCode.visible" />
          <el-table-column label="风险点名称" prop="riskName" key="riskName" :width="colWidth('riskName', 180)" resizable show-overflow-tooltip v-if="columns.riskName.visible" />
          <el-table-column label="所属区域" prop="areaName" key="areaName" :width="colWidth('areaName', 130)" resizable show-overflow-tooltip v-if="columns.areaName.visible" />
          <el-table-column label="事故类别" prop="accidentType" key="accidentType" :width="colWidth('accidentType', 120)" resizable align="center" v-if="columns.accidentType.visible">
            <template #default="scope">
              <span v-if="scope.row.accidentType">{{ accidentTypeLabel(scope.row.accidentType) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="L值" prop="lValue" key="lValue" :width="colWidth('lValue', 70)" resizable align="center" v-if="columns.lValue.visible">
            <template #header><span>L值</span><el-tooltip content="LEC法中的L值，表示事故发生的可能性（Likelihood），取值范围0.1~10，数值越大表示事故发生的可能性越大" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="E值" prop="eValue" key="eValue" :width="colWidth('eValue', 70)" resizable align="center" v-if="columns.eValue.visible">
            <template #header><span>E值</span><el-tooltip content="LEC法中的E值，表示人员暴露于危险环境中的频繁程度（Exposure），取值范围0.5~10，数值越大表示暴露越频繁" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="C值" prop="cValue" key="cValue" :width="colWidth('cValue', 70)" resizable align="center" v-if="columns.cValue.visible">
            <template #header><span>C值</span><el-tooltip content="LEC法中的C值，表示事故产生的后果严重程度（Consequence），取值范围1~100，数值越大表示后果越严重" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="D值" prop="dValue" key="dValue" :width="colWidth('dValue', 70)" resizable align="center" v-if="columns.dValue.visible">
            <template #header><span>D值</span><el-tooltip content="LEC法中的D值，即危险性分值（Danger），D = L × E × C。D值≥320为重大风险，160~319为较大风险，70~159为一般风险，<70为低风险" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="风险等级" prop="riskLevel" key="riskLevel" :width="colWidth('riskLevel', 110)" resizable align="center" sortable="custom" v-if="columns.riskLevel.visible">
            <template #header><span>风险等级</span><el-tooltip content="根据LEC法计算出的D值自动评定：D≥320为重大风险（红），160≤D<320为较大风险（橙），70≤D<160为一般风险（蓝），D<70为低风险（绿）" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope">
              <span class="badge" :class="riskBadgeClass(scope.row.riskLevel)">
                <span class="dot"></span>{{ riskLevelLabel(scope.row.riskLevel) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="管控层级" prop="controlLevel" key="controlLevel" :width="colWidth('controlLevel', 100)" resizable align="center" v-if="columns.controlLevel.visible">
            <template #header><span>管控层级</span><el-tooltip content="安全风险分级管控层级，通常分为公司级、部门级、班组级和岗位级，不同等级的风险由不同层级进行管控" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope"><span class="badge blue">{{ controlLevelLabel(scope.row.controlLevel) }}</span></template>
          </el-table-column>
          <el-table-column label="责任人" prop="personName" key="personName" :width="colWidth('personName', 100)" resizable v-if="columns.personName.visible" />
          <el-table-column label="复评日期" prop="reEvaluateDate" key="reEvaluateDate" :width="colWidth('reEvaluateDate', 120)" resizable align="center" v-if="columns.reEvaluateDate.visible" />
          <el-table-column label="操作" width="240" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:risk:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:risk:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:risk:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="riskRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="风险点编号" prop="riskCode"><el-input v-model="form.riskCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="风险点名称" prop="riskName"><el-input v-model="form.riskName" placeholder="请输入风险点名称" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="所属区域" prop="areaId">
                  <el-select v-model="form.areaId" placeholder="请选择所属区域" filterable clearable style="width: 100%" @change="onAreaChange">
                    <el-option v-for="item in areaOptions" :key="item.areaId" :label="item.areaName" :value="item.areaId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="事故类别" prop="accidentType">
                  <el-select v-model="form.accidentType" placeholder="请选择事故类别" filterable clearable style="width: 100%">
                    <el-option v-for="dict in safety_accident_type" :key="dict.value" :label="dict.label" :value="dict.value" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
              <el-form-item label="危险源描述" prop="hazardDesc"><el-input v-model="form.hazardDesc" type="textarea" :rows="2" placeholder="请输入危险源描述" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>LEC风险评价</div><el-tooltip content="LEC评价法是一种半定量风险评价方法，通过L（可能性）、E（暴露频率）、C（后果严重度）三个因素计算D值（危险性），公式为D=L×E×C" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="6"><el-form-item prop="lValue"><template #label><span>L值</span><el-tooltip content="LEC法中的L值，表示事故发生的可能性（Likelihood），取值范围0.1~10，数值越大表示事故发生的可能性越大" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input-number v-model="form.lValue" :precision="2" :step="0.1" :min="0" :controls="false" style="width: 100%" @change="calcD" /></el-form-item></el-col>
                <el-col :span="6"><el-form-item prop="eValue"><template #label><span>E值</span><el-tooltip content="LEC法中的E值，表示人员暴露于危险环境中的频繁程度（Exposure），取值范围0.5~10，数值越大表示暴露越频繁" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input-number v-model="form.eValue" :precision="2" :step="0.5" :min="0" :controls="false" style="width: 100%" @change="calcD" /></el-form-item></el-col>
                <el-col :span="6"><el-form-item prop="cValue"><template #label><span>C值</span><el-tooltip content="LEC法中的C值，表示事故产生的后果严重程度（Consequence），取值范围1~100，数值越大表示后果越严重" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input-number v-model="form.cValue" :precision="2" :step="1" :min="0" :controls="false" style="width: 100%" @change="calcD" /></el-form-item></el-col>
                <el-col :span="6"><el-form-item prop="dValue"><template #label><span>D值</span><el-tooltip content="LEC法中的D值，即危险性分值（Danger），D = L × E × C。D值≥320为重大风险，160~319为较大风险，70~159为一般风险，<70为低风险" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input-number v-model="form.dValue" :precision="2" :min="0" :controls="false" style="width: 100%" disabled /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item prop="riskLevel"><template #label><span>风险等级</span><el-tooltip content="根据LEC法计算出的D值自动评定：D≥320为重大风险（红），160≤D<320为较大风险（橙），70≤D<160为一般风险（蓝），D<70为低风险（绿）" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-select v-model="form.riskLevel" placeholder="自动评定" style="width: 100%"><el-option v-for="dict in safety_risk_level" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item prop="controlLevel"><template #label><span>管控层级</span><el-tooltip content="安全风险分级管控层级，通常分为公司级、部门级、班组级和岗位级，不同等级的风险由不同层级进行管控" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-select v-model="form.controlLevel" placeholder="请选择" style="width: 100%"><el-option v-for="dict in safety_control_level" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></span>管控措施</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="工程技术" prop="measureEngine"><el-input v-model="form.measureEngine" type="textarea" :rows="2" placeholder="请输入工程技术措施" /></el-form-item>
              <el-form-item label="管理措施" prop="measureManage"><el-input v-model="form.measureManage" type="textarea" :rows="2" placeholder="请输入管理措施" /></el-form-item>
              <el-form-item label="培训教育" prop="measureTraining"><el-input v-model="form.measureTraining" type="textarea" :rows="2" placeholder="请输入培训教育措施" /></el-form-item>
              <el-form-item label="个体防护" prop="measurePpe"><el-input v-model="form.measurePpe" type="textarea" :rows="2" placeholder="请输入个体防护措施" /></el-form-item>
              <el-form-item label="应急处置" prop="measureEmergency"><el-input v-model="form.measureEmergency" type="textarea" :rows="2" placeholder="请输入应急处置措施" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>排查信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="排查频次" prop="checkFrequency"><el-select v-model="form.checkFrequency" placeholder="请选择" style="width: 100%"><el-option v-for="dict in safety_check_frequency" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="责任人" prop="personId">
                  <el-input v-model="form.personName" readonly placeholder="请选择责任人" style="width: 100%" @click="openUserPicker">
                    <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                    <template #suffix><el-icon v-if="form.personName" class="clear-icon" @click.stop="clearPerson"><CircleClose /></el-icon></template>
                  </el-input>
                </el-form-item></el-col>
              </el-row>
              <el-form-item label="检查标准" prop="checkStandard"><el-input v-model="form.checkStandard" type="textarea" :rows="2" placeholder="请输入检查标准" /></el-form-item>
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item prop="evaluateDate"><template #label><span>评价日期</span><el-tooltip content="LEC风险评价的执行日期，修改L/E/C值时自动更新为当天" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-date-picker v-model="form.evaluateDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" @change="calcReEvaluateDate" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item prop="reEvaluateDate"><template #label><span>复评日期</span><el-tooltip content="根据风险等级自动计算：红橙级（重大/较大风险）为评价日期+1年，黄蓝级（一般/低风险）为评价日期+3年" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-date-picker v-model="form.reEvaluateDate" type="date" placeholder="自动计算" value-format="YYYY-MM-DD" style="width: 100%" disabled /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c4">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
          <span class="rd-detail-header-title">风险点详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.riskCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.riskCode }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">风险点编号</span><div class="rd-value">{{ viewData.riskCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">风险点名称</span><div class="rd-value">{{ viewData.riskName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">所属区域</span><div class="rd-value">{{ viewData.areaName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">事故类别</span><div class="rd-value">{{ viewData.accidentType ? accidentTypeLabel(viewData.accidentType) : '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">危险源描述</span><div class="rd-value">{{ viewData.hazardDesc || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>LEC风险评价</div><el-tooltip content="LEC评价法是一种半定量风险评价方法，通过L（可能性）、E（暴露频率）、C（后果严重度）三个因素计算D值（危险性），公式为D=L×E×C" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">L值<el-tooltip content="LEC法中的L值，表示事故发生的可能性（Likelihood），取值范围0.1~10，数值越大表示事故发生的可能性越大" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.lValue != null ? viewData.lValue : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">E值<el-tooltip content="LEC法中的E值，表示人员暴露于危险环境中的频繁程度（Exposure），取值范围0.5~10，数值越大表示暴露越频繁" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.eValue != null ? viewData.eValue : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">C值<el-tooltip content="LEC法中的C值，表示事故产生的后果严重程度（Consequence），取值范围1~100，数值越大表示后果越严重" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.cValue != null ? viewData.cValue : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">D值<el-tooltip content="LEC法中的D值，即危险性分值（Danger），D = L × E × C。D值≥320为重大风险，160~319为较大风险，70~159为一般风险，<70为低风险" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value rd-value--large rd-amount">{{ viewData.dValue != null ? viewData.dValue : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">风险等级<el-tooltip content="根据LEC法计算出的D值自动评定：D≥320为重大风险（红），160≤D<320为较大风险（橙），70≤D<160为一般风险（蓝），D<70为低风险（绿）" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value"><span class="badge" :class="riskBadgeClass(viewData.riskLevel)"><span class="dot"></span>{{ riskLevelLabel(viewData.riskLevel) }}</span></div></div>
              <div class="rd-item"><span class="rd-label">管控层级<el-tooltip content="安全风险分级管控层级，通常分为公司级、部门级、班组级和岗位级，不同等级的风险由不同层级进行管控" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value"><dict-tag :options="safety_control_level" :value="viewData.controlLevel" /></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.measureEngine || viewData.measureManage || viewData.measureTraining || viewData.measurePpe || viewData.measureEmergency">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></span>管控措施</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full" v-if="viewData.measureEngine"><span class="rd-label">工程技术</span><div class="rd-value">{{ viewData.measureEngine }}</div></div>
              <div class="rd-item rd-item--full" v-if="viewData.measureManage"><span class="rd-label">管理措施</span><div class="rd-value">{{ viewData.measureManage }}</div></div>
              <div class="rd-item rd-item--full" v-if="viewData.measureTraining"><span class="rd-label">培训教育</span><div class="rd-value">{{ viewData.measureTraining }}</div></div>
              <div class="rd-item rd-item--full" v-if="viewData.measurePpe"><span class="rd-label">个体防护</span><div class="rd-value">{{ viewData.measurePpe }}</div></div>
              <div class="rd-item rd-item--full" v-if="viewData.measureEmergency"><span class="rd-label">应急处置</span><div class="rd-value">{{ viewData.measureEmergency }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.checkFrequency || viewData.personName || viewData.checkStandard || viewData.evaluateDate">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>排查信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">排查频次</span><div class="rd-value"><dict-tag :options="safety_check_frequency" :value="viewData.checkFrequency" /></div></div>
              <div class="rd-item"><span class="rd-label">责任人</span><div class="rd-value">{{ viewData.personName || '-' }}</div></div>
              <div class="rd-item rd-item--full" v-if="viewData.checkStandard"><span class="rd-label">检查标准</span><div class="rd-value">{{ viewData.checkStandard }}</div></div>
              <div class="rd-item"><span class="rd-label">评价日期<el-tooltip content="LEC风险评价的执行日期，修改L/E/C值时自动更新为当天" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.evaluateDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">复评日期<el-tooltip content="根据风险等级自动计算：红橙级（重大/较大风险）为评价日期+1年，黄蓝级（一般/低风险）为评价日期+3年" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.reEvaluateDate || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc4" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 责任人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择责任人" @confirm="onUserPickerConfirm" />
  </div>
</template>

<script setup name="SafetyRiskPoint">
import { listRiskPoint, getRiskPoint, addRiskPoint, updateRiskPoint, delRiskPoint } from '@/api/safety/risk'
import { listArea } from '@/api/safety/area'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, CircleClose, QuestionFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { safety_risk_level, safety_control_level, safety_check_frequency, safety_accident_type } = proxy.useDict('safety_risk_level', 'safety_control_level', 'safety_check_frequency', 'safety_accident_type')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_risk_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","c4","vc0","vc1","vc2","vc3","vc4"])

const riskList = ref([])
const areaOptions = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const defaultColumns = {
  riskCode: { label: '风险点编号', visible: true },
  riskName: { label: '风险点名称', visible: true },
  areaName: { label: '所属区域', visible: true },
  accidentType: { label: '事故类别', visible: true },
  lValue: { label: 'L值', visible: true },
  eValue: { label: 'E值', visible: true },
  cValue: { label: 'C值', visible: true },
  dValue: { label: 'D值', visible: true },
  riskLevel: { label: '风险等级', visible: true },
  controlLevel: { label: '管控层级', visible: true },
  personName: { label: '责任人', visible: true },
  reEvaluateDate: { label: '复评日期', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_risk_columns')
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
  queryParams: { pageNum: 1, pageSize: 10, riskName: undefined, riskCode: undefined, riskLevel: undefined, controlLevel: undefined, params: {} },
  rules: {
    riskName: [{ required: true, message: '风险点名称不能为空', trigger: 'blur' }],
    hazardDesc: [{ required: true, message: '危险源描述不能为空', trigger: 'blur' }],
    areaId: [{ required: true, message: '所属区域不能为空', trigger: 'change' }],
    lValue: [{ required: true, message: 'L值不能为空', trigger: 'change' }],
    eValue: [{ required: true, message: 'E值不能为空', trigger: 'change' }],
    cValue: [{ required: true, message: 'C值不能为空', trigger: 'change' }],
    personId: [{ required: true, message: '责任人不能为空', trigger: 'change' }],
    evaluateDate: [{ required: true, message: '评价日期不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.riskName) count++
  if (queryParams.value.riskCode) count++
  if (queryParams.value.riskLevel) count++
  if (queryParams.value.controlLevel) count++
  return count
})

function getList() { loading.value = true; listRiskPoint(queryParams.value).then(response => { riskList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.riskName = undefined; queryParams.value.riskCode = undefined; queryParams.value.riskLevel = undefined; queryParams.value.controlLevel = undefined; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.riskPointId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; collapsedCards.c3 = false; collapsedCards.c4 = false; const today = new Date(); const y = today.getFullYear(); const m = String(today.getMonth() + 1).padStart(2, '0'); const d = String(today.getDate()).padStart(2, '0'); form.value.evaluateDate = `${y}-${m}-${d}`; open.value = true; title.value = '添加风险点' }
function handleView(row) { const riskPointId = row.riskPointId || ids.value[0]; getRiskPoint(riskPointId).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function handleUpdate(row) { reset(); getRiskPoint(row.riskPointId || ids.value[0]).then(response => { form.value = response.data; collapsedCards.c2 = !response.data.measureEngine && !response.data.measureManage && !response.data.measureTraining && !response.data.measurePpe && !response.data.measureEmergency; collapsedCards.c3 = !response.data.checkFrequency && !response.data.personName && !response.data.checkStandard && !response.data.evaluateDate; collapsedCards.c4 = !response.data.remark; open.value = true; title.value = '修改风险点' }) }

function calcD() {
  const l = parseFloat(form.value.lValue) || 0
  const e = parseFloat(form.value.eValue) || 0
  const c = parseFloat(form.value.cValue) || 0
  const d = Math.round(l * e * c * 100) / 100
  form.value.dValue = d
  if (d >= 320) form.value.riskLevel = '1'
  else if (d >= 160) form.value.riskLevel = '2'
  else if (d >= 70) form.value.riskLevel = '3'
  else form.value.riskLevel = '4'
  // LEC变更视为重新评价，更新评价日期为当天
  const today = new Date()
  const y = today.getFullYear()
  const m = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  form.value.evaluateDate = `${y}-${m}-${day}`
  // 自动计算复评日期
  calcReEvaluateDate()
}

/** 根据风险等级和评价日期自动计算复评日期 */
function calcReEvaluateDate() {
  const level = form.value.riskLevel
  const evalDate = form.value.evaluateDate
  if (!level || !evalDate) return
  const date = new Date(evalDate)
  if (isNaN(date.getTime())) return
  if (level === '1' || level === '2') {
    date.setFullYear(date.getFullYear() + 1)
  } else {
    date.setFullYear(date.getFullYear() + 3)
  }
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  form.value.reEvaluateDate = `${y}-${m}-${day}`
}

function submitForm() {
  proxy.$refs['riskRef'].validate(valid => {
    if (valid) {
      if (form.value.riskPointId != undefined) { updateRiskPoint(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addRiskPoint(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const riskPointIds = row.riskPointId || ids.value; proxy.$modal.confirm('是否确认删除风险点？').then(function() { return delRiskPoint(riskPointIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('safety/risk/export', { ...queryParams.value }, `risk_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { riskPointId: undefined, riskCode: undefined, riskName: undefined, areaId: undefined, areaName: undefined, hazardDesc: undefined, accidentType: undefined, lValue: undefined, eValue: undefined, cValue: undefined, dValue: undefined, riskLevel: undefined, controlLevel: undefined, measureEngine: undefined, measureManage: undefined, measureTraining: undefined, measurePpe: undefined, measureEmergency: undefined, checkStandard: undefined, checkFrequency: undefined, personId: undefined, personName: undefined, evaluateDate: undefined, reEvaluateDate: undefined, remark: undefined }
  proxy.resetForm('riskRef')
}
/** 打开责任人选择弹窗 */
function openUserPicker() { proxy.$refs.userPickerRef.open(form.value.personId) }
/** 责任人选择确认回调 */
function onUserPickerConfirm(user) { form.value.personId = user.userId; form.value.personName = user.nickName }
/** 清除责任人 */
function clearPerson() { form.value.personId = undefined; form.value.personName = undefined }

/** 区域选择变更回调 */
function onAreaChange(val) { const item = areaOptions.value.find(a => a.areaId === val); form.value.areaName = item ? item.areaName : undefined }
/** 加载区域列表 */
function loadAreas() { listArea({ pageNum: 1, pageSize: 1000 }).then(response => { areaOptions.value = response.rows }) }
/** 事故类别字典翻译 */
function accidentTypeLabel(val) { const item = safety_accident_type.value.find(d => d.value == val); return item ? item.label : '-' }
function riskLevelLabel(level) { const item = safety_risk_level.value.find(d => d.value == level); return item ? item.label : '-' }
function riskBadgeClass(level) { const map = { '1': 'red', '2': 'amber', '3': 'blue', '4': 'green' }; return map[level] || 'gray' }
function controlLevelLabel(level) { const item = safety_control_level.value.find(d => d.value == level); return item ? item.label : '-' }

loadAreas()
getList()
</script>

<style scoped>
.safety-risk-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-risk-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-risk-page .filter-card { padding:14px 20px 16px; }
.safety-risk-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-risk-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-risk-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-risk-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-risk-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-risk-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-risk-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-risk-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-risk-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-risk-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-risk-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-risk-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-risk-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-risk-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-risk-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-risk-page .field .control :deep(.el-select) { width:100%; }
.safety-risk-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-risk-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-risk-page .toolbar .left, .safety-risk-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-risk-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-risk-page .table-wrap { overflow-x:auto; }
.safety-risk-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-risk-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-risk-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-risk-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-risk-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-risk-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-risk-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-risk-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .safety-risk-page .badge.amber .dot { background:var(--amber-500); }
.safety-risk-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .safety-risk-page .badge.blue .dot { background:var(--blue-500); }
.safety-risk-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-risk-page .badge.green .dot { background:var(--green-500); }
.safety-risk-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-risk-page .badge.red .dot { background:var(--red-500); }
.safety-risk-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-risk-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-risk-page .badge.gray .dot { background:var(--ink-400); }
.safety-risk-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
.safety-risk-page .rd-form-tip { margin-left: 4px; color: #c0c4cc; font-size: 14px; cursor: help; }
.safety-risk-page .rd-form-tip:hover { color: #909399; }
@media (max-width:1100px) { .safety-risk-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-risk-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.clear-icon:hover { color: #909399; }
</style>

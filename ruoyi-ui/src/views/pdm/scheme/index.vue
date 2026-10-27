<template>
  <div class="app-container pdm-scheme-page">
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
          <label>方案编号</label>
          <div class="control">
            <el-input v-model="queryParams.schemeNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>方案名称</label>
          <div class="control">
            <el-input v-model="queryParams.schemeName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>产品名称</label>
          <div class="control">
            <el-input v-model="queryParams.productName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>产品编码</label>
          <div class="control">
            <el-input v-model="queryParams.productCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 支持回车快速搜索
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
          <button v-for="d in pdm_scheme_status" :key="d.value" class="status-tab" :class="[statusTabClass(d.value), { 'is-active': activeStatusTab === d.value }]" @click="handleStatusTabClick(d.value)">
            <span class="dot"></span><span>{{ d.label }}</span><span class="count">{{ statusCounts[d.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pdm:scheme:add']">新增</el-button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['pdm:scheme:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pdm:scheme:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pdm_scheme_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="schemeList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" resizable align="center" />
          <el-table-column label="方案编号" prop="schemeNo" key="schemeNo" :width="colWidth('schemeNo', 170)" resizable sortable="custom" v-if="columns.schemeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.schemeNo }}</span></template>
          </el-table-column>
          <el-table-column label="方案名称" prop="schemeName" key="schemeName" :min-width="colWidth('schemeName', 200)" resizable show-overflow-tooltip sortable="custom" v-if="columns.schemeName.visible" />
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 150)" resizable v-if="columns.productCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.productCode }}</span></template>
          </el-table-column>
          <el-table-column label="产品名称" prop="productName" key="productName" :min-width="colWidth('productName', 170)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="方案状态" prop="schemeStatus" key="schemeStatus" :width="colWidth('schemeStatus', 100)" resizable align="center" sortable="custom" v-if="columns.schemeStatus.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.schemeStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.schemeStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="定稿人" prop="finalBy" key="finalBy" :width="colWidth('finalBy', 100)" resizable align="center" v-if="columns.finalBy.visible">
            <template #default="scope">{{ scope.row.finalBy || '-' }}</template>
          </el-table-column>
          <el-table-column label="定稿时间" prop="finalTime" key="finalTime" :width="colWidth('finalTime', 160)" resizable align="center" v-if="columns.finalTime.visible">
            <template #default="scope">{{ scope.row.finalTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" :width="colWidth('操作', 190)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Stamp" @click="handleEffective(scope.row)" v-if="scope.row.schemeStatus === '0'" v-hasPermi="['pdm:scheme:effective']">定稿</el-button>
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.schemeStatus !== '2'" v-hasPermi="['pdm:scheme:edit']">修改</el-button>
                <el-button link type="warning" icon="CircleClose" @click="handleDisable(scope.row)" v-if="scope.row.schemeStatus === '1'" v-hasPermi="['pdm:scheme:disable']">停用</el-button>
                <el-button link type="success" icon="RefreshLeft" @click="handleEnable(scope.row)" v-if="scope.row.schemeStatus === '2'" v-hasPermi="['pdm:scheme:enable']">恢复</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.schemeStatus !== '1' && scope.row.schemeStatus !== '2'" v-hasPermi="['pdm:scheme:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="1032px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="schemeRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div>
            </div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="方案名称" prop="schemeName">
                    <el-input v-model="form.schemeName" placeholder="请输入方案名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="产品档案" prop="productId">
                    <el-input :model-value="form.productName" placeholder="请选择产品档案" readonly style="width: 100%" @click="openProductPicker">
                      <template #append>
                        <el-button icon="Search" @click="openProductPicker" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="form.productName" class="clear-icon" @click.stop="clearProduct"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="设计目标" prop="designGoal">
                    <el-input v-model="form.designGoal" type="textarea" :rows="2" placeholder="请输入设计目标" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="主要参数" prop="mainParams">
                    <el-input v-model="form.mainParams" type="textarea" :rows="3" placeholder="请输入主要参数说明" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="16">
                  <el-form-item label="方案正文" prop="fileUrl">
                    <file-upload v-model="form.fileUrl" :fileSize="50" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
              </el-form-item>
            </div>
          </section>
          <section class="rd-card" v-hasPermi="['pdm:scheme:formula']">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>配方配比明细（涉密）</div>
            </div>
            <div class="rd-card-body">
              <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 12px" title="配方配比属于涉密等级最高的设计资料，仅授权角色可见，查看与导出单独留痕" />
              <el-table :data="form.formulaList" border size="small">
                <el-table-column type="index" label="序号" width="55" align="center" />
                <el-table-column label="原料名称" min-width="200">
                  <template #default="scope">
                    <el-input :model-value="scope.row.itemName" readonly placeholder="请选择原料物料" @click="openFormulaPicker(scope.$index)">
                      <template #append>
                        <el-button v-if="!scope.row.itemName" icon="Search" @click="openFormulaPicker(scope.$index)" />
                        <el-button v-else icon="CircleClose" @click.stop="clearFormulaMaterial(scope.row)" />
                      </template>
                    </el-input>
                  </template>
                </el-table-column>
                <el-table-column label="配比" width="120">
                  <template #default="scope"><el-input v-model="scope.row.ratio" placeholder="如 25%" /></template>
                </el-table-column>
                <el-table-column label="单位" width="90" align="center">
                  <template #default="scope"><span class="formula-unit">{{ unitLabel(scope.row.unit) }}</span></template>
                </el-table-column>
                <el-table-column label="允差范围" width="130">
                  <template #default="scope"><el-input v-model="scope.row.tolerance" placeholder="如 ±0.5" /></template>
                </el-table-column>
                <el-table-column label="备注" min-width="140">
                  <template #default="scope"><el-input v-model="scope.row.remark" /></template>
                </el-table-column>
                <el-table-column label="操作" width="70" align="center">
                  <template #default="scope">
                    <el-button link type="danger" icon="Delete" @click="form.formulaList.splice(scope.$index, 1)" />
                  </template>
                </el-table-column>
              </el-table>
              <el-button type="primary" plain icon="Plus" size="small" style="margin-top: 10px" @click="addFormulaRow">添加原料</el-button>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog v-model="viewOpen" width="1032px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">方案详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.schemeNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ viewData.schemeNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">方案编号</span><div class="rd-value col-mono">{{ viewData.schemeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">方案名称</span><div class="rd-value">{{ viewData.schemeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value col-mono">{{ viewData.productCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">方案状态</span><div class="rd-value"><dict-tag :options="pdm_scheme_status" :value="viewData.schemeStatus" /></div></div>
              <div class="rd-item"><span class="rd-label">定稿时间</span><div class="rd-value">{{ viewData.finalTime || '-' }}{{ viewData.finalBy ? '（' + viewData.finalBy + '）' : '' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">设计目标</span><div class="rd-value">{{ viewData.designGoal || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">主要参数</span><div class="rd-value">{{ viewData.mainParams || '-' }}</div></div>
              <div class="rd-item rd-item--full" v-if="viewData.fileUrl"><span class="rd-label">方案正文</span><div class="rd-value">
                <div class="rd-file-item" @click="handleFilePreview(viewData.fileUrl)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                  <span>{{ fileNameOf(viewData.fileUrl) }}</span>
                </div>
              </div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-hasPermi="['pdm:scheme:formula']">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>配方配比明细（涉密）</div>
          </div>
          <div class="rd-card-body">
            <el-table :data="viewData.formulaList || []" border size="small" empty-text="未录入配方明细">
              <el-table-column type="index" label="序号" width="55" align="center" />
              <el-table-column label="原料名称" prop="itemName" min-width="160" />
              <el-table-column label="配比" prop="ratio" width="110" align="center" />
              <el-table-column label="单位" prop="unit" width="90" align="center">
                <template #default="scope">{{ unitLabel(scope.row.unit) }}</template>
              </el-table-column>
              <el-table-column label="允差范围" prop="tolerance" width="120" align="center" />
              <el-table-column label="备注" prop="remark" min-width="120" />
            </el-table>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>版本时间轴</div>
          </div>
          <div class="rd-card-body">
            <el-table :data="versionList" border size="small">
              <el-table-column label="版本号" prop="versionNo" width="90" align="center">
                <template #default="scope">
                  <span class="badge" :class="scope.row.effectiveFlag === '1' ? 'green' : 'gray'">{{ scope.row.versionNo }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="90" align="center">
                <template #default="scope">
                  <span class="badge" :class="scope.row.effectiveFlag === '1' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.effectiveFlag === '1' ? '生效' : '历史' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="版本说明" prop="versionDesc" min-width="160" show-overflow-tooltip />
              <el-table-column label="生效依据" width="110" align="center">
                <template #default="scope"><dict-tag :options="pdm_version_effect_basis" :value="scope.row.effectBasis" /></template>
              </el-table-column>
              <el-table-column label="依据单号" prop="effectRef" width="160" show-overflow-tooltip />
              <el-table-column label="生效时间" prop="effectiveTime" width="160" align="center" />
              <el-table-column label="操作人" prop="createBy" width="90" align="center" />
            </el-table>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 定稿对话框 -->
    <el-dialog v-model="effectiveOpen" width="520px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/></svg></div>
          <span class="rd-detail-header-title">方案定稿</span>
        </div>
      </template>
      <el-form ref="effectiveRef" :model="effectiveForm" :rules="effectiveRules" label-width="100px">
        <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px" title="定稿将生成生效版本并触发产品档案「已定型」联动；同一方案重复定稿请先升版" />
        <el-form-item label="定稿说明" prop="remark">
          <el-input v-model="effectiveForm.remark" type="textarea" :rows="2" placeholder="审批依据说明（定稿评审结论等）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitEffective">确认定稿</el-button>
        <el-button @click="effectiveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 产品档案选择器 -->
    <PdmProductPicker ref="productPickerRef" title="选择产品档案" @confirm="onProductConfirm" />
    <!-- 配方原料物料选择器（仅原材料） -->
    <MaterialPicker ref="formulaPickerRef" title="选择原料物料（仅原材料）" :material-types="['0']" @confirm="onFormulaMaterialConfirm" />
    <!-- 文件预览 -->
    <file-preview ref="filePreviewRef" />
    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="设计方案业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="primary">编制中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已定稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">已停用</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="编制中">方案创建后的初始状态，可编辑关联产品、配方明细、方案正文，支持定稿升版</el-descriptions-item>
          <el-descriptions-item label="已定稿">方案经定稿评审后受控生效，生成正式版本并纳入版本管理；后续修订需通过技术变更执行升版</el-descriptions-item>
          <el-descriptions-item label="已停用">方案不再适用（如产品停产、被新方案替代）时在列表行点「停用」进入此状态，不走变更单；保留用于历史追溯，误停用可在列表行点「恢复」回到已定稿</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">配方物料联动</div>
              <div class="highlight-card-body">配方明细的原料通过<strong>物料选择器</strong>选取（仅限原材料），计量单位自动带出，确保配方与仓库物料编码一致</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">定稿后受控</div>
              <div class="highlight-card-body">已定稿方案<strong>不可直接修改</strong>，任何修订须发起技术变更，经审批后执行升版并生成新版本</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">版本全程留痕</div>
              <div class="highlight-card-body">定稿升版自动记录<strong>版本号、生效依据、生效时间</strong>，版本记录弹窗可查看完整历史轨迹</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">正文文件管理</div>
              <div class="highlight-card-body">方案正文以文件形式上传（Word/PDF），详情弹窗点击文件名即可<strong>在线预览</strong></div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建方案：</strong>点击「新增」，通过放大镜选择产品档案，填写方案名称与设计人员，上传方案正文
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>配置配方：</strong>在配方配比明细中添加原料行，通过物料选择器选取原材料（单位自动带出），填写配比与允差范围
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>定稿升版：</strong>方案评审通过后点击「定稿」生成正式版本；后续修订走技术变更流程升版
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PdmScheme">
import { listScheme, getScheme, addScheme, updateScheme, delScheme, effectiveScheme, disableScheme, enableScheme } from '@/api/pdm/scheme'
import { listVersion } from '@/api/pdm/docVersion'
import { fetchAllPages, downloadXlsx } from '@/utils/csvExport'
import { useColumnResize } from '@/composables/useColumnResize'
import PdmProductPicker from '@/components/PdmProductPicker/index.vue'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import FilePreview from '@/components/FilePreview/index.vue'
import { ArrowDown, ArrowRight, CircleClose, Delete, Download, RefreshLeft, Search, Filter, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pdm_scheme_index')
const { pdm_scheme_status, pdm_version_effect_basis, wms_unit } = proxy.useDict('pdm_scheme_status', 'pdm_version_effect_basis', 'wms_unit')

const schemeList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const versionList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showStatusHelp = ref(false)
const showAdvanced = ref(false)
const dateRange = ref([])
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0 })
const productPickerRef = ref()
const filePreviewRef = ref()
const effectiveOpen = ref(false)
const effectiveForm = ref({ schemeId: undefined, remark: undefined })
const effectiveRules = { remark: [{ required: true, message: '请填写定稿说明（审批依据）', trigger: 'blur' }] }

const defaultColumns = {
  schemeNo: { label: '方案编号', visible: true },
  schemeName: { label: '方案名称', visible: true },
  productCode: { label: '产品编码', visible: true },
  productName: { label: '产品名称', visible: true },
  schemeStatus: { label: '方案状态', visible: true },
  finalBy: { label: '定稿人', visible: true },
  finalTime: { label: '定稿时间', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pdm_scheme_columns')
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
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    schemeNo: undefined,
    schemeName: undefined,
    productName: undefined,
    productCode: undefined,
    schemeStatus: undefined,
    params: {}
  },
  rules: {
    schemeName: [{ required: true, message: '请输入方案名称', trigger: 'blur' }],
    productId: [{ required: true, message: '请选择产品档案', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.schemeNo) count++
  if (queryParams.value.schemeName) count++
  if (queryParams.value.productName) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() {
  loading.value = true
  listScheme(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    schemeList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params = queryParams.value.params || {}
    queryParams.value.params.orderByColumn = column.prop
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.value.params = queryParams.value.params || {}
    queryParams.value.params.orderByColumn = undefined
    queryParams.value.params.isAsc = undefined
  }
  getList()
}

function statusLabel(status) { const item = pdm_scheme_status.value.find(d => d.value == status); return item ? item.label : '-' }
function statusBadgeClass(status) { const item = pdm_scheme_status.value.find(d => d.value == status); const map = { 'info': 'gray', 'success': 'green', 'danger': 'red', 'warning': 'amber', 'primary': 'blue' }; return map[item ? item.listClass : ''] || 'gray' }
function statusTabClass(value) { return value === '0' ? 'tab-draft' : (value === '1' ? 'tab-final' : 'tab-stopped') }

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
  loadStatusCounts()
}

function resetQuery() {
  queryParams.value.schemeNo = undefined
  queryParams.value.schemeName = undefined
  queryParams.value.productName = undefined
  queryParams.value.productCode = undefined
  queryParams.value.schemeStatus = undefined
  queryParams.value.params = {}
  dateRange.value = []
  activeStatusTab.value = 'all'
  handleQuery()
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.schemeStatus = status === 'all' ? undefined : status
  handleQuery()
}

function loadStatusCounts() {
  listScheme({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total };
    (pdm_scheme_status.value || []).forEach(d => { counts[d.value] = 0 });
    (res.rows || []).forEach(r => { if (counts[r.schemeStatus] !== undefined) counts[r.schemeStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.schemeId)
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    schemeId: undefined,
    schemeName: undefined,
    productId: undefined,
    productName: undefined,
    designGoal: undefined,
    mainParams: undefined,
    fileUrl: undefined,
    remark: undefined,
    formulaList: []
  }
  proxy.resetForm('schemeRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加设计方案'
}

function handleUpdate(row) {
  reset()
  const schemeId = row.schemeId || ids.value[0]
  getScheme(schemeId).then(response => {
    form.value = response.data
    if (!form.value.formulaList) form.value.formulaList = []
    open.value = true
    title.value = '修改设计方案'
  })
}

function handleView(row) {
  getScheme(row.schemeId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
    loadVersions(row.schemeId)
  })
}

function loadVersions(schemeId) {
  listVersion('1', schemeId).then(res => { versionList.value = res.data || [] })
}

function handleEffective(row) {
  // 版本由后端自动生成（首定稿生成 V1.0，重复定稿取最新未生效版本），无需用户选择
  effectiveForm.value = { schemeId: row.schemeId, remark: undefined }
  effectiveOpen.value = true
}

function submitEffective() {
  proxy.$refs['effectiveRef'].validate(valid => {
    if (valid) {
      effectiveScheme(effectiveForm.value).then(() => {
        proxy.$modal.msgSuccess('定稿成功，产品档案已联动定型')
        effectiveOpen.value = false
        getList(); loadStatusCounts()
      })
    }
  })
}

function openProductPicker() {
  productPickerRef.value.open(form.value.productId)
}

function onProductConfirm(product) {
  form.value.productId = product.productId
  form.value.productName = product.productName
  proxy.$refs['schemeRef'].clearValidate(['productId'])
}

/** 清除产品档案 */
function clearProduct() {
  form.value.productId = undefined
  form.value.productName = undefined
  proxy.$refs['schemeRef'].clearValidate(['productId'])
}

function addFormulaRow() {
  form.value.formulaList.push({ materialId: undefined, itemName: undefined, ratio: undefined, unit: undefined, tolerance: undefined, remark: undefined })
}

/** 配方明细-原料物料选择器 */
const formulaPickerRef = ref(null)
const formulaEditIndex = ref(-1)
function openFormulaPicker(index) {
  formulaEditIndex.value = index
  formulaPickerRef.value.open()
}
/** 选择原料物料后自动带出名称与单位 */
function onFormulaMaterialConfirm(material) {
  const row = form.value.formulaList[formulaEditIndex.value]
  if (row) {
    row.materialId = material.materialId
    row.itemName = material.materialName
    row.unit = material.unit
  }
}
/** 清除原料物料（名称与单位一并清除） */
function clearFormulaMaterial(row) {
  row.materialId = undefined
  row.itemName = undefined
  row.unit = undefined
}
/** 单位字典文本 */
function unitLabel(v) {
  if (v === null || v === undefined || v === '') return '-'
  const item = wms_unit.value.find(d => d.value == v)
  return item ? item.label : v
}

function fileNameOf(url) {
  return url && url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url
}

function handleFilePreview(url) {
  filePreviewRef.value.open(url, fileNameOf(url))
}

function submitForm() {
  proxy.$refs['schemeRef'].validate(valid => {
    if (valid) {
      if (form.value.schemeId != undefined) {
        updateScheme(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList(); loadStatusCounts()
        })
      } else {
        addScheme(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功，已自动创建 V1.0 版本记录')
          open.value = false
          getList(); loadStatusCounts()
        })
      }
    }
  })
}

function handleDelete(row) {
  const schemeIds = row.schemeId || ids.value
  proxy.$modal.confirm('是否确认删除选中的设计方案？已定稿方案不允许删除。').then(function () {
    return delScheme(schemeIds)
  }).then(() => {
    getList(); loadStatusCounts()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 停用方案（生命周期终止动作，不走变更单；可恢复） */
function handleDisable(row) {
  proxy.$modal.confirm(`确定停用方案【${row.schemeNo}】？停用后方案不再适用于变更与业务引用（可随时恢复）`).then(() => {
    return disableScheme(row.schemeId, '手动停用')
  }).then(() => {
    getList(); loadStatusCounts()
    proxy.$modal.msgSuccess('停用成功')
  }).catch(() => {})
}

/** 恢复方案（已停用 → 已定稿） */
function handleEnable(row) {
  proxy.$modal.confirm(`确定恢复方案【${row.schemeNo}】？恢复后回到已定稿状态，可正常参与变更与业务引用`).then(() => {
    return enableScheme(row.schemeId)
  }).then(() => {
    getList(); loadStatusCounts()
    proxy.$modal.msgSuccess('恢复成功')
  }).catch(() => {})
}

async function handleExport() {
  let rows = await fetchAllPages(listScheme, proxy.addDateRange(queryParams.value, dateRange.value))
  if (ids.value && ids.value.length) { rows = rows.filter(i => ids.value.includes(i.schemeId)) }
  if (!rows.length) { proxy.$modal.msgWarning('当前筛选下无数据可导出'); return }
  const headers = ['方案编号', '方案名称', '产品编码', '产品名称', '方案状态', '定稿人', '定稿时间', '创建时间']
  downloadXlsx(`pdm_scheme_${new Date().getTime()}`, headers, rows.map(i => [
    i.schemeNo || '', i.schemeName || '', i.productCode || '', i.productName || '',
    statusLabel(i.schemeStatus), i.finalBy || '', i.finalTime || '', i.createTime || ''
  ]))
}

function cancel() {
  open.value = false
  reset()
}

getList()
loadStatusCounts()
</script>

<style src="@/composables/mms-list-styles.scss" lang="scss" scoped></style>
<style scoped>
/* 配方明细单位（带出只读） */
.pdm-scheme-page .formula-unit { color: var(--ink-700); font-weight: 600; }

.pdm-scheme-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.pdm-scheme-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.pdm-scheme-page .filter-card { padding:14px 20px 16px; }
.pdm-scheme-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pdm-scheme-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pdm-scheme-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pdm-scheme-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pdm-scheme-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pdm-scheme-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pdm-scheme-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pdm-scheme-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pdm-scheme-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pdm-scheme-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pdm-scheme-page .filter-card .filter-buttons { display:flex; gap:8px; }
.pdm-scheme-page .field { display:flex; flex-direction:column; gap:6px; }
.pdm-scheme-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.pdm-scheme-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pdm-scheme-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.pdm-scheme-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pdm-scheme-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; }
.pdm-scheme-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pdm-scheme-page .field .control :deep(.el-date-editor) { width:100%; }
.pdm-scheme-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; }
.pdm-scheme-page .field .control :deep(.el-date-editor .el-range-separator), .pdm-scheme-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.pdm-scheme-page .status-tabs { display:flex; align-items:center; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pdm-scheme-page .tabs-track { display:flex; align-items:center; gap:4px; flex-wrap:wrap; flex:1; min-width:0; }
.pdm-scheme-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pdm-scheme-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pdm-scheme-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; }
.pdm-scheme-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pdm-scheme-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pdm-scheme-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pdm-scheme-page .status-tab.is-active .dot { background:var(--brand-500); }
.pdm-scheme-page .status-tab.tab-draft .dot { background:var(--blue-500); }
.pdm-scheme-page .status-tab.is-active.tab-draft .count { background:var(--blue-500); color:#fff; }
.pdm-scheme-page .status-tab.tab-final .dot { background:var(--green-500); }
.pdm-scheme-page .status-tab.is-active.tab-final .count { background:var(--green-500); color:#fff; }
.pdm-scheme-page .status-tab.tab-stopped .dot { background:var(--red-500); }
.pdm-scheme-page .status-tab.is-active.tab-stopped .count { background:var(--red-500); color:#fff; }
.pdm-scheme-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pdm-scheme-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pdm-scheme-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pdm-scheme-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.pdm-scheme-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pdm-scheme-page .btn-soft .el-icon { font-size:14px; }
.pdm-scheme-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pdm-scheme-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pdm-scheme-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pdm-scheme-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pdm-scheme-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pdm-scheme-page .table-wrap { overflow-x:auto; }
.pdm-scheme-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pdm-scheme-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pdm-scheme-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pdm-scheme-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pdm-scheme-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pdm-scheme-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pdm-scheme-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pdm-scheme-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pdm-scheme-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.pdm-scheme-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pdm-scheme-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pdm-scheme-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pdm-scheme-page .badge.green .dot { background:var(--green-500); }
.pdm-scheme-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pdm-scheme-page .badge.gray .dot { background:var(--ink-400); }
.pdm-scheme-page .clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.pdm-scheme-page .clear-icon:hover { color: #909399; }
.pdm-scheme-page .form-tip { font-size:12px; color:var(--ink-500); line-height:1.5; margin-top:2px; width: 100%; }
.pdm-scheme-page .rd-file-item { display:inline-flex; align-items:center; gap:6px; color:var(--blue-700); cursor:pointer; font-size:14px; }
.pdm-scheme-page .rd-file-item:hover { text-decoration:underline; }
.pdm-scheme-page :deep(.col-action) { padding: 6px 4px !important; }
.pdm-scheme-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.pdm-scheme-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.pdm-scheme-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.pdm-scheme-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }
@media (max-width:1100px) { .pdm-scheme-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pdm-scheme-page .filter-card .filter-bar { grid-template-columns:1fr; } .pdm-scheme-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>

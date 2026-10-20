<template>
  <div class="app-container pdm-designfile-page">
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
          <label>资料编号</label>
          <div class="control">
            <el-input v-model="queryParams.fileNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>资料名称</label>
          <div class="control">
            <el-input v-model="queryParams.fileName" placeholder="请输入" clearable @keyup.enter="handleQuery">
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
          <label>资料类别</label>
          <div class="control control--select">
            <el-select v-model="queryParams.fileCategory" placeholder="全部" clearable style="width: 100%" @change="handleQuery">
              <el-option v-for="d in pdm_file_category" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
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
          <button v-for="d in pdm_file_status" :key="d.value" class="status-tab" :class="[statusTabClass(d.value), { 'is-active': activeStatusTab === d.value }]" @click="handleStatusTabClick(d.value)">
            <span class="dot"></span><span>{{ d.label }}</span><span class="count">{{ statusCounts[d.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pdm:designFile:add']">新增</el-button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['pdm:designFile:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pdm:designFile:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pdm_designfile_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="fileList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" resizable align="center" />
          <el-table-column label="资料编号" prop="fileNo" key="fileNo" :width="colWidth('fileNo', 170)" resizable sortable="custom" v-if="columns.fileNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.fileNo }}</span></template>
          </el-table-column>
          <el-table-column label="资料名称" prop="fileName" key="fileName" :min-width="colWidth('fileName', 200)" resizable show-overflow-tooltip sortable="custom" v-if="columns.fileName.visible" />
          <el-table-column label="资料类别" prop="fileCategory" key="fileCategory" :width="colWidth('fileCategory', 100)" resizable align="center" v-if="columns.fileCategory.visible">
            <template #default="scope">
              <span class="badge" :class="categoryBadgeClass(scope.row.fileCategory)">{{ categoryLabel(scope.row.fileCategory) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 150)" resizable v-if="columns.productCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.productCode }}</span></template>
          </el-table-column>
          <el-table-column label="产品名称" prop="productName" key="productName" :min-width="colWidth('productName', 160)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="关联方案" prop="schemeNo" key="schemeNo" :width="colWidth('schemeNo', 150)" resizable v-if="columns.schemeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.schemeNo || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="受控状态" prop="fileStatus" key="fileStatus" :width="colWidth('fileStatus', 110)" resizable align="center" sortable="custom" v-if="columns.fileStatus.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.fileStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.fileStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" :width="colWidth('操作', 190)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Stamp" @click="handleEffective(scope.row)" v-if="scope.row.fileStatus === '0'" v-hasPermi="['pdm:designFile:effective']">生效</el-button>
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pdm:designFile:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.fileStatus !== '1'" v-hasPermi="['pdm:designFile:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="fileRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div>
            </div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="资料名称" prop="fileName">
                    <el-input v-model="form.fileName" placeholder="请输入资料名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="资料类别" prop="fileCategory">
                    <el-select v-model="form.fileCategory" placeholder="请选择资料类别" style="width: 100%">
                      <el-option v-for="d in pdm_file_category" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg></span>关联对象</div>
            </div>
            <div class="rd-card-body">
              <el-row :gutter="20">
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
                <el-col :span="12">
                  <el-form-item label="关联方案" prop="schemeId">
                    <el-select v-model="form.schemeId" placeholder="请先选择产品档案" clearable filterable style="width: 100%" :disabled="!form.productId" @change="onSchemeChange">
                      <el-option v-for="s in schemeOptions" :key="s.schemeId" :label="s.schemeNo + ' ' + s.schemeName" :value="s.schemeId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg></span>文件与备注</div>
            </div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="20">
                  <el-form-item label="资料文件" prop="fileUrl">
                    <file-upload v-model="form.fileUrl" :fileSize="50" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
              </el-form-item>
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
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">资料详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.fileNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ viewData.fileNo }}</span>
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
              <div class="rd-item"><span class="rd-label">资料编号</span><div class="rd-value col-mono">{{ viewData.fileNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">资料名称</span><div class="rd-value">{{ viewData.fileName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">资料类别</span><div class="rd-value">{{ categoryLabel(viewData.fileCategory) }}</div></div>
              <div class="rd-item"><span class="rd-label">受控状态</span><div class="rd-value"><dict-tag :options="pdm_file_status" :value="viewData.fileStatus" /></div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg></span>关联对象</div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value col-mono">{{ viewData.productCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关联方案</span><div class="rd-value col-mono">{{ viewData.schemeNo || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg></span>文件与备注</div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item rd-item--full" v-if="viewData.fileUrl"><span class="rd-label">资料文件</span><div class="rd-value">
                <div class="rd-file-item" @click="handleFilePreview(viewData.fileUrl)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                  <span>{{ fileNameOf(viewData.fileUrl) }}</span>
                </div>
              </div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
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

    <!-- 生效对话框 -->
    <el-dialog v-model="effectiveOpen" width="520px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/></svg></div>
          <span class="rd-detail-header-title">文件生效</span>
        </div>
      </template>
      <el-form ref="effectiveRef" :model="effectiveForm" :rules="effectiveRules" label-width="100px">
        <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px" title="生效后文件转为受控状态，旧版本自动转历史；同一资料重复生效请先升版" />
        <el-form-item label="生效说明" prop="remark">
          <el-input v-model="effectiveForm.remark" type="textarea" :rows="2" placeholder="审批依据说明（图纸会签/评审结论等）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitEffective">确认生效</el-button>
        <el-button @click="effectiveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 产品档案选择器 -->
    <PdmProductPicker ref="productPickerRef" title="选择产品档案" @confirm="onProductConfirm" />
    <!-- 文件预览 -->
    <file-preview ref="filePreviewRef" />
    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="图纸文件业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">未生效</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">受控生效</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="未生效">图纸上传后的初始状态，可编辑文件信息、替换文件，执行「生效」操作后进入受控状态</el-descriptions-item>
          <el-descriptions-item label="受控生效">图纸正式受控发布，纳入版本管理；此状态下不可直接修改，修订替换时系统自动升版并保留历史版本</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">编号自动生成</div>
              <div class="highlight-card-body">图纸编号由系统按<strong>编码规则</strong>自动生成（前缀 TZ），无需手工填写，确保编号唯一规范</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">替换自动升版</div>
              <div class="highlight-card-body">受控图纸替换文件时<strong>自动生成新版本</strong>，历史版本完整保留，可随时追溯查看</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">产品方案双关联</div>
              <div class="highlight-card-body">图纸可同时关联<strong>产品档案与设计方案</strong>，实现按产品维度的设计资料归集</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">在线预览</div>
              <div class="highlight-card-body">支持 Word、PDF、DWG 等格式上传，详情弹窗点击文件名即可<strong>在线预览</strong>，无需下载</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>上传图纸：</strong>点击「新增」，填写文件名称、文件类型，通过放大镜选择关联产品（可同时选择关联方案），上传图纸文件
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>审核生效：</strong>图纸核对无误后点击「生效」，进入受控生效状态并纳入版本管理
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>升版管理：</strong>资料修订时替换上传新文件，系统自动升版并保留历史版本，可在版本记录中查看全轨迹
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PdmDesignFile">
import { listDesignFile, getDesignFile, addDesignFile, updateDesignFile, delDesignFile, effectiveDesignFile } from '@/api/pdm/designFile'
import { listScheme } from '@/api/pdm/scheme'
import { listVersion } from '@/api/pdm/docVersion'
import { fetchAllPages, downloadXlsx } from '@/utils/csvExport'
import { useColumnResize } from '@/composables/useColumnResize'
import PdmProductPicker from '@/components/PdmProductPicker/index.vue'
import FilePreview from '@/components/FilePreview/index.vue'
import { ArrowDown, ArrowRight, CircleClose, Delete, Download, RefreshLeft, Search, Filter, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pdm_designfile_index')
const { pdm_file_category, pdm_file_status, pdm_version_effect_basis } = proxy.useDict('pdm_file_category', 'pdm_file_status', 'pdm_version_effect_basis')

const fileList = ref([])
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
const schemeOptions = ref([])
const effectiveOpen = ref(false)
const effectiveForm = ref({ fileId: undefined, remark: undefined })
const effectiveRules = { remark: [{ required: true, message: '请填写生效说明（审批依据）', trigger: 'blur' }] }

const defaultColumns = {
  fileNo: { label: '资料编号', visible: true },
  fileName: { label: '资料名称', visible: true },
  fileCategory: { label: '资料类别', visible: true },
  productCode: { label: '产品编码', visible: true },
  productName: { label: '产品名称', visible: true },
  schemeNo: { label: '关联方案', visible: true },
  fileStatus: { label: '受控状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pdm_designfile_columns')
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
    fileNo: undefined,
    fileName: undefined,
    productName: undefined,
    productCode: undefined,
    fileCategory: undefined,
    fileStatus: undefined,
    params: {}
  },
  rules: {
    fileName: [{ required: true, message: '请输入资料名称', trigger: 'blur' }],
    fileCategory: [{ required: true, message: '请选择资料类别', trigger: 'change' }],
    fileUrl: [{ required: true, message: '请上传资料文件', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listDesignFile(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    fileList.value = response.rows
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

function categoryLabel(category) { const item = pdm_file_category.value.find(d => d.value == category); return item ? item.label : '-' }
function categoryBadgeClass(category) {
  const map = { '1': 'blue', '2': 'violet', '3': 'green', '4': 'amber', '5': 'gray' }
  return map[category] || 'gray'
}
function statusLabel(status) { const item = pdm_file_status.value.find(d => d.value == status); return item ? item.label : '-' }
function statusBadgeClass(status) { const item = pdm_file_status.value.find(d => d.value == status); const map = { 'info': 'gray', 'success': 'green', 'danger': 'red', 'warning': 'amber', 'primary': 'blue' }; return map[item ? item.listClass : ''] || 'gray' }
function statusTabClass(value) { return value === '0' ? 'tab-draft' : 'tab-final' }

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
  loadStatusCounts()
}

function resetQuery() {
  queryParams.value.fileNo = undefined
  queryParams.value.fileName = undefined
  queryParams.value.productName = undefined
  queryParams.value.productCode = undefined
  queryParams.value.fileCategory = undefined
  queryParams.value.fileStatus = undefined
  queryParams.value.params = {}
  dateRange.value = []
  activeStatusTab.value = 'all'
  handleQuery()
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.fileStatus = status === 'all' ? undefined : status
  handleQuery()
}

function loadStatusCounts() {
  listDesignFile({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total };
    (pdm_file_status.value || []).forEach(d => { counts[d.value] = 0 });
    (res.rows || []).forEach(r => { if (counts[r.fileStatus] !== undefined) counts[r.fileStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.fileId)
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    fileId: undefined,
    fileName: undefined,
    fileCategory: undefined,
    productId: undefined,
    productName: undefined,
    schemeId: undefined,
    schemeNo: undefined,
    fileUrl: undefined,
    remark: undefined
  }
  schemeOptions.value = []
  proxy.resetForm('fileRef')
}

/** 按产品加载可选方案 */
function loadSchemeOptions(productId) {
  schemeOptions.value = []
  if (!productId) return
  listScheme({ productId, pageNum: 1, pageSize: 999 }).then(res => { schemeOptions.value = res.rows || [] })
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '上传图纸技术文件'
}

function handleUpdate(row) {
  reset()
  const fileId = row.fileId || ids.value[0]
  getDesignFile(fileId).then(response => {
    form.value = response.data
    if (form.value.productId) loadSchemeOptions(form.value.productId)
    open.value = true
    title.value = '修改图纸技术文件'
  })
}

function handleView(row) {
  getDesignFile(row.fileId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
    loadVersions(row.fileId)
  })
}

function loadVersions(fileId) {
  listVersion('2', fileId).then(res => { versionList.value = res.data || [] })
}

function handleEffective(row) {
  // 版本由后端自动生成（首次生效生成 V1.0，重复生效取最新未生效版本），无需用户选择
  effectiveForm.value = { fileId: row.fileId, remark: undefined }
  effectiveOpen.value = true
}

function submitEffective() {
  proxy.$refs['effectiveRef'].validate(valid => {
    if (valid) {
      effectiveDesignFile(effectiveForm.value).then(() => {
        proxy.$modal.msgSuccess('生效成功，文件已纳入受控管理')
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
  // 产品变更后清空原方案选择
  if (form.value.schemeId) {
    form.value.schemeId = undefined
    form.value.schemeNo = undefined
  }
  loadSchemeOptions(product.productId)
  proxy.$refs['fileRef'].clearValidate(['productId'])
}

/** 清除产品档案（联动清空关联方案） */
function clearProduct() {
  form.value.productId = undefined
  form.value.productName = undefined
  if (form.value.schemeId) {
    form.value.schemeId = undefined
    form.value.schemeNo = undefined
  }
  proxy.$refs['fileRef'].clearValidate(['productId'])
}

function onSchemeChange(schemeId) {
  const scheme = schemeOptions.value.find(s => s.schemeId === schemeId)
  if (scheme) {
    form.value.schemeNo = scheme.schemeNo
    // 方案携带产品信息时回填
    if (scheme.productId && !form.value.productId) {
      form.value.productId = scheme.productId
      form.value.productName = scheme.productName
      loadSchemeOptions(scheme.productId)
    }
  } else {
    form.value.schemeNo = undefined
  }
}

function fileNameOf(url) {
  return url && url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url
}

function handleFilePreview(url) {
  filePreviewRef.value.open(url, fileNameOf(url))
}

function submitForm() {
  proxy.$refs['fileRef'].validate(valid => {
    if (valid) {
      if (form.value.fileId != undefined) {
        updateDesignFile(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功，文件替换已自动升版')
          open.value = false
          getList(); loadStatusCounts()
        })
      } else {
        addDesignFile(form.value).then(() => {
          proxy.$modal.msgSuccess('上传成功，已自动创建 V1.0 版本记录')
          open.value = false
          getList(); loadStatusCounts()
        })
      }
    }
  })
}

function handleDelete(row) {
  const fileIds = row.fileId || ids.value
  proxy.$modal.confirm('是否确认删除选中的图纸技术文件？受控生效文件不允许删除。').then(function () {
    return delDesignFile(fileIds)
  }).then(() => {
    getList(); loadStatusCounts()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

async function handleExport() {
  let rows = await fetchAllPages(listDesignFile, proxy.addDateRange(queryParams.value, dateRange.value))
  if (ids.value && ids.value.length) { rows = rows.filter(i => ids.value.includes(i.fileId)) }
  if (!rows.length) { proxy.$modal.msgWarning('当前筛选下无数据可导出'); return }
  const headers = ['资料编号', '资料名称', '资料类别', '产品编码', '产品名称', '关联方案', '受控状态', '创建时间']
  downloadXlsx(`pdm_design_file_${new Date().getTime()}`, headers, rows.map(i => [
    i.fileNo || '', i.fileName || '', categoryLabel(i.fileCategory), i.productCode || '', i.productName || '',
    i.schemeNo || '', statusLabel(i.fileStatus), i.createTime || ''
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
.pdm-designfile-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --violet-700:#6d28d9; --amber-50:#fffbeb; --amber-700:#b45309; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.pdm-designfile-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.pdm-designfile-page .filter-card { padding:14px 20px 16px; }
.pdm-designfile-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pdm-designfile-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pdm-designfile-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pdm-designfile-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pdm-designfile-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pdm-designfile-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pdm-designfile-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pdm-designfile-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pdm-designfile-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pdm-designfile-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pdm-designfile-page .filter-card .filter-buttons { display:flex; gap:8px; }
.pdm-designfile-page .field { display:flex; flex-direction:column; gap:6px; }
.pdm-designfile-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.pdm-designfile-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pdm-designfile-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.pdm-designfile-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pdm-designfile-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; }
.pdm-designfile-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pdm-designfile-page .field .control--select { padding:0 6px; }
.pdm-designfile-page .field .control--select :deep(.el-select__wrapper) { box-shadow:none !important; background:transparent !important; min-height:34px; padding:0; }
.pdm-designfile-page .status-tabs { display:flex; align-items:center; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pdm-designfile-page .tabs-track { display:flex; align-items:center; gap:4px; flex-wrap:wrap; flex:1; min-width:0; }
.pdm-designfile-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pdm-designfile-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pdm-designfile-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; }
.pdm-designfile-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pdm-designfile-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pdm-designfile-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pdm-designfile-page .status-tab.is-active .dot { background:var(--brand-500); }
.pdm-designfile-page .status-tab.tab-draft .dot { background:var(--blue-500); }
.pdm-designfile-page .status-tab.is-active.tab-draft .count { background:var(--blue-500); color:#fff; }
.pdm-designfile-page .status-tab.tab-final .dot { background:var(--green-500); }
.pdm-designfile-page .status-tab.is-active.tab-final .count { background:var(--green-500); color:#fff; }
.pdm-designfile-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pdm-designfile-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pdm-designfile-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pdm-designfile-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.pdm-designfile-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pdm-designfile-page .btn-soft .el-icon { font-size:14px; }
.pdm-designfile-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pdm-designfile-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pdm-designfile-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pdm-designfile-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pdm-designfile-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pdm-designfile-page .table-wrap { overflow-x:auto; }
.pdm-designfile-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pdm-designfile-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pdm-designfile-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pdm-designfile-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pdm-designfile-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pdm-designfile-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pdm-designfile-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pdm-designfile-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pdm-designfile-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.pdm-designfile-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pdm-designfile-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pdm-designfile-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.pdm-designfile-page .badge.blue .dot { background:var(--blue-500); }
.pdm-designfile-page .badge.violet { background:var(--violet-50); color:var(--violet-700); border-color:#ddd6fe; }
.pdm-designfile-page .badge.violet .dot { background:#8b5cf6; }
.pdm-designfile-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pdm-designfile-page .badge.green .dot { background:var(--green-500); }
.pdm-designfile-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.pdm-designfile-page .badge.amber .dot { background:#f59e0b; }
.pdm-designfile-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pdm-designfile-page .badge.gray .dot { background:var(--ink-400); }
.pdm-designfile-page .clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.pdm-designfile-page .clear-icon:hover { color: #909399; }
.pdm-designfile-page .form-tip { font-size:12px; color:var(--ink-500); line-height:1.5; margin-top:2px; width: 100%; }
.pdm-designfile-page .rd-file-item { display:inline-flex; align-items:center; gap:6px; color:var(--blue-700); cursor:pointer; font-size:14px; }
.pdm-designfile-page .rd-file-item:hover { text-decoration:underline; }
.pdm-designfile-page :deep(.col-action) { padding: 6px 4px !important; }
.pdm-designfile-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.pdm-designfile-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.pdm-designfile-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.pdm-designfile-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }
@media (max-width:1100px) { .pdm-designfile-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pdm-designfile-page .filter-card .filter-bar { grid-template-columns:1fr; } .pdm-designfile-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>

<template>
  <div class="app-container pdm-product-page">
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
          <label>产品编码</label>
          <div class="control">
            <el-input v-model="queryParams.productCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>产品名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>产品分类</label>
          <div class="control is-select">
            <el-tree-select
              v-model="queryParams.categoryId"
              :data="categoryOptions"
              :props="{ value: 'categoryId', label: 'categoryName', children: 'children' }"
              value-key="categoryId"
              placeholder="全部"
              clearable
              check-strictly
              @change="handleQuery"
            />
          </div>
        </div>
        <div class="field">
          <label>责任设计员</label>
          <div class="control">
            <el-input v-model="queryParams.designerName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>规格型号</label>
          <div class="control">
            <el-input v-model="queryParams.specModel" placeholder="请输入" clearable @keyup.enter="handleQuery">
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
      <!-- Status Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="d in pdm_product_status" :key="d.value" class="status-tab" :class="[statusTabClass(d.value), { 'is-active': activeStatusTab === d.value }]" @click="handleStatusTabClick(d.value)">
            <span class="dot"></span><span>{{ d.label }}</span><span class="count">{{ statusCounts[d.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
      </div>
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pdm:product:add']">新增</el-button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['pdm:product:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pdm:product:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pdm_product_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="productList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 170)" resizable sortable="custom" v-if="columns.productCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.productCode }}</span></template>
          </el-table-column>
          <el-table-column label="产品名称" prop="materialName" key="materialName" :min-width="colWidth('materialName', 220)" resizable show-overflow-tooltip sortable="custom" v-if="columns.materialName.visible" />
          <el-table-column label="规格型号" prop="specModel" key="specModel" :min-width="colWidth('specModel', 180)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible">
            <template #default="scope">
              <span class="badge blue">{{ unitLabel(scope.row.unit) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="产品分类" prop="categoryName" key="categoryName" :width="colWidth('categoryName', 140)" resizable show-overflow-tooltip v-if="columns.categoryName.visible">
            <template #default="scope"><span class="badge violet">{{ scope.row.categoryName || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="产品状态" prop="productStatus" key="productStatus" :width="colWidth('productStatus', 110)" resizable align="center" sortable="custom" v-if="columns.productStatus.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.productStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.productStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="责任设计员" prop="designerName" key="designerName" :width="colWidth('designerName', 120)" resizable align="center" v-if="columns.designerName.visible">
            <template #default="scope">{{ scope.row.designerName || '-' }}</template>
          </el-table-column>
          <el-table-column label="定型日期" prop="finalDate" key="finalDate" :width="colWidth('finalDate', 120)" resizable align="center" v-if="columns.finalDate.visible">
            <template #default="scope">{{ scope.row.finalDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" :width="colWidth('操作', 140)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.productStatus !== '2'" v-hasPermi="['pdm:product:edit']">修改</el-button>
                <el-button link type="warning" icon="CircleClose" @click="handleDiscontinue(scope.row)" v-if="scope.row.productStatus === '1'" v-hasPermi="['pdm:product:discontinue']">停产</el-button>
                <el-button link type="success" icon="RefreshLeft" @click="handleResume(scope.row)" v-if="scope.row.productStatus === '2'" v-hasPermi="['pdm:product:resume']">复产</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.productStatus === '0'" v-hasPermi="['pdm:product:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="864px" style="--el-dialog-width: 864px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="productRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>挂接物料
                <el-tooltip content="产品编码取自仓库物料编码（一物一档），仅可选择物料类型为「成品」的物料，每个物料仅可挂接一个产品档案；选择物料后自动带出产品编码、规格型号与计量单位" placement="top">
                  <el-icon class="title-info-icon"><InfoFilled /></el-icon>
                </el-tooltip>
              </div>
            </div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="产品物料" prop="materialId">
                    <el-input :model-value="form.materialName" placeholder="请选择物料" readonly style="width: 100%" @click="openMaterialPicker">
                      <template #append>
                        <el-button icon="Search" @click="openMaterialPicker" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="form.materialName" class="clear-icon" @click.stop="clearMaterial"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="产品编码" prop="productCode">
                    <el-input v-model="form.productCode" placeholder="取自物料编码" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="规格型号" prop="specModel">
                    <el-input v-model="form.specModel" placeholder="选物料后自动带入，可修改" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="计量单位" prop="unit">
                    <el-select v-model="form.unit" placeholder="选物料后自动带入" style="width: 100%">
                      <el-option v-for="dict in wms_unit" :key="dict.value" :label="dict.label" :value="dict.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div>
            </div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="产品分类" prop="categoryId">
                    <el-tree-select
                      v-model="form.categoryId"
                      :data="categoryOptions"
                      :props="{ value: 'categoryId', label: 'categoryName', children: 'children' }"
                      value-key="categoryId"
                      placeholder="请选择"
                      clearable
                      check-strictly
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="责任设计员" prop="designerName">
                    <el-input :model-value="form.designerName" placeholder="请选择设计员" readonly style="width: 100%" @click="openUserPicker">
                      <template #append>
                        <el-button icon="Search" @click="openUserPicker" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="form.designerName" class="clear-icon" @click.stop="clearDesigner"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20" v-if="form.productId != undefined && form.productStatus !== '1'">
                <el-col :span="12">
                  <el-form-item label="产品状态" prop="productStatus">
                    <el-radio-group v-model="form.productStatus">
                      <el-radio value="0">设计中</el-radio>
                      <el-radio value="2">已停产</el-radio>
                    </el-radio-group>
                    <div class="form-tip">产品状态"已定型"须由设计方案定稿流程触发</div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></span>附件资料</div>
            </div>
            <div class="rd-card-body">
              <el-form-item label="附件" prop="attach">
                <file-upload v-model="form.attach" :fileSize="50" />
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
    <el-dialog v-model="viewOpen" width="1032px" style="--el-dialog-width: 1032px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">产品详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.productCode">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编码：{{ viewData.productCode }}</span>
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
              <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value col-mono">{{ viewData.productCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计量单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div>
              <div class="rd-item"><span class="rd-label">产品分类</span><div class="rd-value">{{ viewData.categoryName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品状态</span><div class="rd-value"><dict-tag :options="pdm_product_status" :value="viewData.productStatus" /></div></div>
              <div class="rd-item"><span class="rd-label">责任设计员</span><div class="rd-value">{{ viewData.designerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">定型日期</span><div class="rd-value">{{ viewData.finalDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime || '-' }}</div></div>
              <div class="rd-item rd-item--full" v-if="viewData.attach"><span class="rd-label">附件</span><div class="rd-value">
                <div class="rd-file-item" v-for="(f, i) in (viewData.attach || '').split(',')" :key="i" @click="handleFilePreview(f)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                  <span>{{ fileNameOf(f) }}</span>
                </div>
              </div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>版本记录</div>
          </div>
          <div class="rd-card-body">
            <el-table v-if="productVersionList.length" :data="productVersionList" border size="small">
              <el-table-column label="资料类型" width="100" align="center">
                <template #default="scope">
                  <span class="badge" :class="bizTypeBadgeClass(scope.row.bizType)">{{ bizTypeLabel(scope.row.bizType) }}</span>
                </template>
              </el-table-column>
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
              <el-table-column label="版本说明" prop="versionDesc" min-width="150" show-overflow-tooltip />
              <el-table-column label="生效依据" width="110" align="center">
                <template #default="scope"><dict-tag :options="pdm_version_effect_basis" :value="scope.row.effectBasis" /></template>
              </el-table-column>
              <el-table-column label="依据单号" prop="effectRef" width="160" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.effectRef || '-' }}</template>
              </el-table-column>
              <el-table-column label="生效时间" prop="effectiveTime" width="160" align="center">
                <template #default="scope">{{ scope.row.effectiveTime || '-' }}</template>
              </el-table-column>
              <el-table-column label="操作人" prop="createBy" width="90" align="center" />
            </el-table>
            <el-empty v-else description="设计方案、图纸、设计BOM定稿后自动记录版本" :image-size="60" />
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 物料选择器 -->
    <MaterialPicker ref="materialPickerRef" title="选择产品物料（仅成品）" :material-types="['2']" @confirm="onMaterialConfirm" />
    <!-- 人员选择器 -->
    <UserPicker ref="userPickerRef" title="选择责任设计员" @confirm="onDesignerConfirm" />
    <!-- 文件预览 -->
    <file-preview ref="filePreviewRef" />
    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="产品档案业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="primary">设计中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已定型</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">已停产</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="设计中">产品档案建档后的初始状态，可编辑基础信息、上传附件，并开展方案、图纸、BOM 等设计工作</el-descriptions-item>
          <el-descriptions-item label="已定型">产品通过定型评审后的状态，相关定稿资料受控管理，后续修改需走技术变更流程</el-descriptions-item>
          <el-descriptions-item label="已停产">产品不再安排新业务时在列表行点「停产」进入此状态（仅「已定型」产品可停产，不走变更单）；档案保留用于历史追溯，产品复产可在列表行点「复产」回到已定型</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">一物一档</div>
              <div class="highlight-card-body">产品编码取自<strong>仓库成品物料编码</strong>，每个物料仅可挂接一个产品档案，重复挂接系统自动拦截</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">信息自动带出</div>
              <div class="highlight-card-body">选择物料后自动带出<strong>产品编码、规格型号、计量单位</strong>，只需补全产品分类、责任设计员等信息</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">设计产出统一挂接</div>
              <div class="highlight-card-body">产品的<strong>设计方案、图纸文件、设计BOM、技术变更</strong>均挂在产品档案下，实现全生命周期资料追溯</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">多维度定位</div>
              <div class="highlight-card-body">支持<strong>状态标签页</strong>快速切换，提供编码、名称、分类、设计员、创建时间等多条件组合筛选</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>选择物料建档：</strong>点击「新增」，通过输入框后的放大镜按钮选择仓库成品物料（仅限成品类型），系统自动带出产品编码、规格型号、计量单位
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>补全档案信息：</strong>填写产品分类、责任设计员、定型日期，上传产品图纸、检验报告等附件资料
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>开展设计工作：</strong>在方案、图纸、BOM 模块关联该产品；产品定型评审通过后更新为「已定型」状态
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PdmProduct">
import { listProduct, getProduct, addProduct, updateProduct, delProduct, discontinueProduct, resumeProduct } from '@/api/pdm/product'
import { listProductVersions } from '@/api/pdm/docVersion'
import { getCategoryTree } from '@/api/pdm/category'
import { fetchAllPages, downloadXlsx } from '@/utils/csvExport'
import { useColumnResize } from '@/composables/useColumnResize'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import UserPicker from '@/components/UserPicker/index.vue'
import FilePreview from '@/components/FilePreview/index.vue'
import { ArrowDown, ArrowRight, CircleClose, Edit, Delete, Download, RefreshLeft, Search, Filter, InfoFilled, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pdm_product_index')
const { pdm_product_status, wms_unit, pdm_version_biz_type, pdm_version_effect_basis } = proxy.useDict('pdm_product_status', 'wms_unit', 'pdm_version_biz_type', 'pdm_version_effect_basis')

const productList = ref([])
const categoryOptions = ref([])
const productVersionList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
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
const materialPickerRef = ref()
const userPickerRef = ref()
const filePreviewRef = ref()

// 列显隐配置
const defaultColumns = {
  productCode: { label: '产品编码', visible: true },
  materialName: { label: '产品名称', visible: true },
  specModel: { label: '规格型号', visible: true },
  unit: { label: '单位', visible: true },
  categoryName: { label: '产品分类', visible: true },
  productStatus: { label: '产品状态', visible: true },
  designerName: { label: '责任设计员', visible: true },
  finalDate: { label: '定型日期', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pdm_product_columns')
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
    productCode: undefined,
    materialName: undefined,
    categoryId: undefined,
    designerName: undefined,
    specModel: undefined,
    productStatus: undefined,
    params: {}
  },
  rules: {
    materialId: [{ required: true, message: '请选择产品对应的物料', trigger: 'change' }],
    categoryId: [{ required: true, message: '请选择产品分类', trigger: 'change' }],
    designerId: [{ required: true, message: '请选择责任设计员', trigger: 'change' }],
    specModel: [{ required: true, message: '规格型号不能为空（选择物料后自动带出，可修改）', trigger: 'blur' }],
    unit: [{ required: true, message: '计量单位不能为空（选择物料后自动带出）', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.productCode) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.categoryId) count++
  if (queryParams.value.designerName) count++
  if (queryParams.value.specModel) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() {
  loading.value = true
  listProduct(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    productList.value = response.rows
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

function statusLabel(status) { const item = pdm_product_status.value.find(d => d.value == status); return item ? item.label : '-' }
function statusBadgeClass(status) { const item = pdm_product_status.value.find(d => d.value == status); const map = { 'info': 'gray', 'success': 'green', 'danger': 'red', 'warning': 'amber', 'primary': 'blue' }; return map[item ? item.listClass : ''] || 'gray' }
function unitLabel(unit) { const item = wms_unit.value.find(d => d.value == unit); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '0': 'tab-design', '1': 'tab-final', '2': 'tab-stopped' }; return map[value] || '' }

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
  loadStatusCounts()
}

function resetQuery() {
  queryParams.value.productCode = undefined
  queryParams.value.materialName = undefined
  queryParams.value.categoryId = undefined
  queryParams.value.designerName = undefined
  queryParams.value.specModel = undefined
  queryParams.value.productStatus = undefined
  queryParams.value.params = {}
  dateRange.value = []
  activeStatusTab.value = 'all'
  handleQuery()
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.productStatus = status === 'all' ? undefined : status
  handleQuery()
}

function loadStatusCounts() {
  listProduct({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total };
    (pdm_product_status.value || []).forEach(d => { counts[d.value] = 0 });
    (res.rows || []).forEach(r => { if (counts[r.productStatus] !== undefined) counts[r.productStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

function loadCategoryTree() {
  getCategoryTree().then(response => {
    categoryOptions.value = response.data || []
  })
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.productId)
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    productId: undefined,
    productCode: undefined,
    materialId: undefined,
    materialName: undefined,
    specModel: undefined,
    unit: undefined,
    categoryId: undefined,
    categoryName: undefined,
    productStatus: '0',
    designerId: undefined,
    designerName: undefined,
    attach: undefined,
    remark: undefined
  }
  proxy.resetForm('productRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加产品档案'
}

function handleUpdate(row) {
  reset()
  const productId = row.productId || ids.value[0]
  getProduct(productId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改产品档案'
  })
}

function handleView(row) {
  getProduct(row.productId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
    listProductVersions(row.productId).then(res => { productVersionList.value = res.data || [] }).catch(() => { productVersionList.value = [] })
  })
}

/** 版本记录资料类型 */
function bizTypeLabel(type) { const item = pdm_version_biz_type.value.find(d => d.value == type); return item ? item.label : '-' }
function bizTypeBadgeClass(type) { const item = pdm_version_biz_type.value.find(d => d.value == type); const map = { 'primary': 'blue', 'info': 'gray', 'success': 'green', 'warning': 'amber', 'danger': 'red' }; return map[item ? item.listClass : ''] || 'gray' }

/** 打开物料选择器 */
function openMaterialPicker() {
  materialPickerRef.value.open(form.value.materialId)
}

/** 物料选择确认：产品编码=物料编码 */
function onMaterialConfirm(material) {
  form.value.materialId = material.materialId
  form.value.materialName = material.materialName
  form.value.productCode = material.materialCode
  form.value.specModel = material.specModel
  form.value.unit = material.unit
  // 仅清除物料字段校验态，避免 resetFields 将带出字段一并清空
  proxy.$refs.productRef?.clearValidate(['materialId', 'productCode', 'specModel', 'unit'])
}

/** 打开设计员选择器 */
function openUserPicker() {
  userPickerRef.value.open(form.value.designerId)
}

/** 设计员选择确认 */
function onDesignerConfirm(user) {
  form.value.designerId = user.userId
  form.value.designerName = user.nickName
}

/** 清除挂接物料 */
function clearMaterial() {
  form.value.materialId = undefined
  form.value.materialName = undefined
  form.value.productCode = undefined
}

/** 清除责任设计员 */
function clearDesigner() {
  form.value.designerId = undefined
  form.value.designerName = undefined
}

/** 附件文件名 */
function fileNameOf(url) {
  return url && url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url
}

/** 附件预览 */
function handleFilePreview(url) {
  filePreviewRef.value.open(url, fileNameOf(url))
}

function submitForm() {
  proxy.$refs['productRef'].validate(valid => {
    if (valid) {
      // 回填分类名称冗余
      const cat = findCategory(categoryOptions.value, form.value.categoryId)
      form.value.categoryName = cat ? cat.categoryName : undefined
      if (form.value.productId != undefined) {
        updateProduct(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList(); loadStatusCounts()
        })
      } else {
        addProduct(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList(); loadStatusCounts()
        })
      }
    }
  })
}

/** 在树中查找分类 */
function findCategory(nodes, categoryId) {
  if (!nodes || categoryId == undefined) return null
  for (const n of nodes) {
    if (n.categoryId === categoryId) return n
    const hit = findCategory(n.children || [], categoryId)
    if (hit) return hit
  }
  return null
}

function handleDelete(row) {
  const productIds = row.productId || ids.value
  proxy.$modal.confirm('是否确认删除选中的产品档案？仅"设计中"产品允许删除。').then(function () {
    return delProduct(productIds)
  }).then(() => {
    getList(); loadStatusCounts()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 产品停产（生命周期终止动作，不走变更单；可复产） */
function handleDiscontinue(row) {
  proxy.$modal.confirm(`确定停产产品【${row.productName || row.productCode}】？停产后再审查看与追溯，但不再安排新业务（可随时复产）`).then(() => {
    return discontinueProduct(row.productId, '手动停产')
  }).then(() => {
    getList(); loadStatusCounts()
    proxy.$modal.msgSuccess('停产成功')
  }).catch(() => {})
}

/** 产品复产（已停产 → 已定型） */
function handleResume(row) {
  proxy.$modal.confirm(`确定复产产品【${row.productName || row.productCode}】？复产后回到已定型状态，可正常参与业务`).then(() => {
    return resumeProduct(row.productId)
  }).then(() => {
    getList(); loadStatusCounts()
    proxy.$modal.msgSuccess('复产成功')
  }).catch(() => {})
}

/** 导出产品档案（与列表口径一致：含筛选条件，导出全部页数据） */
async function handleExport() {
  let rows = await fetchAllPages(listProduct, proxy.addDateRange(queryParams.value, dateRange.value))
  if (ids.value && ids.value.length) { rows = rows.filter(i => ids.value.includes(i.productId)) }
  if (!rows.length) { proxy.$modal.msgWarning('当前筛选下无数据可导出'); return }
  const headers = ['产品编码', '产品名称', '规格型号', '单位', '产品分类', '产品状态', '责任设计员', '定型日期', '创建时间']
  downloadXlsx(`pdm_product_${new Date().getTime()}`, headers, rows.map(i => [
    i.productCode || '', i.materialName || '', i.specModel || '', unitLabel(i.unit), i.categoryName || '',
    statusLabel(i.productStatus), i.designerName || '', i.finalDate || '', i.createTime || ''
  ]))
}

function cancel() {
  open.value = false
  reset()
}

getList()
loadStatusCounts()
loadCategoryTree()
</script>

<style src="@/composables/mms-list-styles.scss" lang="scss" scoped></style>
<style scoped>
/* ===== Design Tokens ===== */
.pdm-product-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --amber-50:#fffbeb; --amber-700:#b45309;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.pdm-product-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.pdm-product-page .filter-card { padding:14px 20px 16px; }
.pdm-product-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pdm-product-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pdm-product-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pdm-product-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pdm-product-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pdm-product-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pdm-product-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pdm-product-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pdm-product-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pdm-product-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pdm-product-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.pdm-product-page .field { display:flex; flex-direction:column; gap:6px; }
.pdm-product-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.pdm-product-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pdm-product-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.pdm-product-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pdm-product-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.pdm-product-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.pdm-product-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pdm-product-page .field .control :deep(.el-select), .pdm-product-page .field .control :deep(.el-tree-select) { width:100%; }
.pdm-product-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.pdm-product-page .field .control :deep(.el-tree-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.pdm-product-page .field .control :deep(.el-date-editor) { width:100%; }
.pdm-product-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.pdm-product-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.pdm-product-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Status Tabs ===== */
.pdm-product-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pdm-product-page .tabs-track { display:flex; align-items:center; gap:4px; flex-wrap:wrap; flex:1; min-width:0; }
.pdm-product-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pdm-product-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pdm-product-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.pdm-product-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pdm-product-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pdm-product-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pdm-product-page .status-tab.is-active .dot { background:var(--brand-500); }
.pdm-product-page .status-tab.tab-design .dot { background:var(--blue-500); }
.pdm-product-page .status-tab.tab-design .count { background:var(--blue-50); color:var(--blue-700); }
.pdm-product-page .status-tab.is-active.tab-design .count { background:var(--blue-500); color:#fff; }
.pdm-product-page .status-tab.tab-final .dot { background:var(--green-500); }
.pdm-product-page .status-tab.tab-final .count { background:var(--green-50); color:var(--green-700); }
.pdm-product-page .status-tab.is-active.tab-final .count { background:var(--green-500); color:#fff; }
.pdm-product-page .status-tab.tab-stopped .dot { background:var(--red-500); }
.pdm-product-page .status-tab.tab-stopped .count { background:var(--red-50); color:var(--red-700); }
.pdm-product-page .status-tab.is-active.tab-stopped .count { background:var(--red-500); color:#fff; }

/* ===== Toolbar ===== */
.pdm-product-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pdm-product-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pdm-product-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pdm-product-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Buttons ===== */
.pdm-product-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pdm-product-page .btn-soft .el-icon { font-size:14px; }
.pdm-product-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pdm-product-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pdm-product-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pdm-product-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pdm-product-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }

/* ===== Table ===== */
.pdm-product-page .table-wrap { overflow-x:auto; }
.pdm-product-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pdm-product-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pdm-product-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pdm-product-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pdm-product-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pdm-product-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pdm-product-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pdm-product-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pdm-product-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badges ===== */
.pdm-product-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pdm-product-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pdm-product-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.pdm-product-page .badge.blue .dot { background:var(--blue-500); }
.pdm-product-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pdm-product-page .badge.green .dot { background:var(--green-500); }
.pdm-product-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.pdm-product-page .badge.red .dot { background:var(--red-500); }
.pdm-product-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.pdm-product-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pdm-product-page .badge.gray .dot { background:var(--ink-400); }
.pdm-product-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.pdm-product-page .badge.amber .dot { background:#f59e0b; }

/* ===== 表单辅助 ===== */
.pdm-product-page .clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.pdm-product-page .clear-icon:hover { color: #909399; }
.pdm-product-page .rd-card-title .title-info-icon { margin-left: 6px; color: #c0c4cc; font-size: 14px; cursor: help; vertical-align: middle; }
.pdm-product-page .rd-card-title .title-info-icon:hover { color: var(--el-color-primary, #409eff); }
.pdm-product-page .form-tip { font-size:12px; color:var(--ink-500); line-height:1.5; margin-top:2px; }
.pdm-product-page .rd-file-item { display:inline-flex; align-items:center; gap:6px; color:var(--blue-700); cursor:pointer; font-size:14px; margin-right:16px; }
.pdm-product-page .rd-file-item:hover { text-decoration:underline; }

/* ===== 操作列 ===== */
.pdm-product-page :deep(.col-action) { padding: 6px 4px !important; }
.pdm-product-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.pdm-product-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.pdm-product-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.pdm-product-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }

@media (max-width:1100px) { .pdm-product-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pdm-product-page .filter-card .filter-bar { grid-template-columns:1fr; } .pdm-product-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>

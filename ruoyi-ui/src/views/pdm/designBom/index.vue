<template>
  <div class="app-container pdm-designbom-page">
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
          <label>BOM编号</label>
          <div class="control">
            <el-input v-model="queryParams.bomNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>BOM名称</label>
          <div class="control">
            <el-input v-model="queryParams.bomName" placeholder="请输入" clearable @keyup.enter="handleQuery">
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
          <button v-for="d in pdm_bom_status" :key="d.value" class="status-tab" :class="[statusTabClass(d.value), { 'is-active': activeStatusTab === d.value }]" @click="handleStatusTabClick(d.value)">
            <span class="dot"></span><span>{{ d.label }}</span><span class="count">{{ statusCounts[d.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pdm:designBom:add']">新增</el-button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['pdm:designBom:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pdm:designBom:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pdm_designbom_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="bomList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" resizable align="center" />
          <el-table-column label="BOM编号" prop="bomNo" key="bomNo" :width="colWidth('bomNo', 170)" resizable sortable="custom" v-if="columns.bomNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.bomNo }}</span></template>
          </el-table-column>
          <el-table-column label="BOM名称" prop="bomName" key="bomName" :min-width="colWidth('bomName', 180)" resizable show-overflow-tooltip sortable="custom" v-if="columns.bomName.visible" />
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 150)" resizable v-if="columns.productCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.productCode }}</span></template>
          </el-table-column>
          <el-table-column label="产品名称" prop="productName" key="productName" :min-width="colWidth('productName', 160)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="单台用量" prop="baseQty" key="baseQty" :width="colWidth('baseQty', 110)" resizable align="right" v-if="columns.baseQty.visible">
            <template #default="scope">{{ scope.row.baseQty != null ? scope.row.baseQty + ' ' + unitLabel(scope.row.baseUnit) : '-' }}</template>
          </el-table-column>
          <el-table-column label="明细数" prop="itemCount" key="itemCount" :width="colWidth('itemCount', 90)" resizable align="center" v-if="columns.itemCount.visible">
            <template #default="scope"><span class="badge blue">{{ scope.row.itemCount || 0 }}</span></template>
          </el-table-column>
          <el-table-column label="BOM状态" prop="bomStatus" key="bomStatus" :width="colWidth('bomStatus', 100)" resizable align="center" sortable="custom" v-if="columns.bomStatus.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.bomStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.bomStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" :width="colWidth('操作', 270)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Stamp" @click="handleEffective(scope.row)" v-if="scope.row.bomStatus === '0'" v-hasPermi="['pdm:designBom:effective']">生效</el-button>
                <el-tooltip v-if="scope.row.bomStatus === '1' && scope.row.currentVersionNo && scope.row.currentVersionNo === scope.row.releasedVersionNo" content="当前版本已下发至制造端，BOM升版后可再次下发" placement="top">
                  <span style="margin-right: 12px"><el-button link type="info" icon="Position" disabled v-hasPermi="['pdm:designBom:release']">下发MBOM</el-button></span>
                </el-tooltip>
                <el-button v-else-if="scope.row.bomStatus === '1'" link type="success" icon="Position" @click="handleRelease(scope.row)" v-hasPermi="['pdm:designBom:release']">下发MBOM</el-button>
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pdm:designBom:edit']">修改</el-button>
                <el-button link type="primary" icon="List" @click="handleReleaseRecord(scope.row)" v-if="scope.row.bomStatus === '1'" v-hasPermi="['pdm:designBom:list']">记录</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.bomStatus === '0'" v-hasPermi="['pdm:designBom:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="bomRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div>
            </div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="BOM名称" prop="bomName">
                    <el-input v-model="form.bomName" placeholder="请输入BOM名称" />
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
                <el-col :span="12">
                  <el-form-item label="单台用量" prop="baseQty">
                    <el-input-number v-model="form.baseQty" :min="0" :precision="4" :controls="false" placeholder="单台成品用量" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="产品单位" prop="baseUnit">
                    <el-select v-model="form.baseUnit" placeholder="请选择单位" clearable style="width: 100%">
                      <el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
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
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>BOM明细</div>
            </div>
            <div class="rd-card-body">
              <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px" title="父项仅可选择上方已录入的行，形成层级结构；顶级件父项选择“顶级”" />
              <el-table :data="form.itemList" border size="small">
                <el-table-column type="index" label="序号" width="55" align="center" />
                <el-table-column label="父项" width="150">
                  <template #default="scope">
                    <el-select v-model="scope.row.parentIndex" size="small" style="width: 100%">
                      <el-option label="顶级" :value="-1" />
                      <el-option v-for="(r, i) in form.itemList.slice(0, scope.$index)" :key="i" :label="(i + 1) + '. ' + (r.materialCode || '未选物料')" :value="i" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="物料" min-width="220">
                  <template #default="scope">
                    <el-input :model-value="scope.row.materialCode ? scope.row.materialCode + ' ' + scope.row.materialName : ''" placeholder="请选择物料" readonly size="small" @click="openMaterialPicker(scope.$index)">
                      <template #append>
                        <el-button icon="Search" size="small" @click="openMaterialPicker(scope.$index)" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="scope.row.materialCode" class="clear-icon" @click.stop="clearItemMaterial(scope.$index)"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </template>
                </el-table-column>
                <el-table-column label="规格型号" width="130">
                  <template #default="scope"><span class="cell-text">{{ scope.row.specModel || '-' }}</span></template>
                </el-table-column>
                <el-table-column label="单位" width="80" align="center">
                  <template #default="scope">{{ unitLabel(scope.row.unit) }}</template>
                </el-table-column>
                <el-table-column label="用量" width="120">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.usageQty" :min="0" :precision="4" :controls="false" size="small" style="width: 100%" placeholder="用量" />
                  </template>
                </el-table-column>
                <el-table-column label="损耗率(%)" width="110">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.lossRate" :min="0" :max="100" :precision="2" :controls="false" size="small" style="width: 100%" placeholder="0" />
                  </template>
                </el-table-column>
                <el-table-column label="备注" min-width="120">
                  <template #default="scope"><el-input v-model="scope.row.remark" size="small" /></template>
                </el-table-column>
                <el-table-column label="操作" width="70" align="center">
                  <template #default="scope">
                    <el-button link type="danger" icon="Delete" @click="removeItemRow(scope.$index)" />
                  </template>
                </el-table-column>
              </el-table>
              <el-button type="primary" plain icon="Plus" size="small" style="margin-top: 10px" @click="addItemRow">添加明细</el-button>
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
    <el-dialog v-model="viewOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">BOM详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.bomNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ viewData.bomNo }}</span>
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
              <div class="rd-item"><span class="rd-label">BOM编号</span><div class="rd-value col-mono">{{ viewData.bomNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">BOM名称</span><div class="rd-value">{{ viewData.bomName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value col-mono">{{ viewData.productCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">单台用量</span><div class="rd-value">{{ viewData.baseQty != null ? viewData.baseQty + ' ' + unitLabel(viewData.baseUnit) : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">BOM状态</span><div class="rd-value"><dict-tag :options="pdm_bom_status" :value="viewData.bomStatus" /></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>BOM明细（{{ (viewData.itemList || []).length }} 项）</div>
          </div>
          <div class="rd-card-body">
            <el-table :data="viewItemTree" border size="small" row-key="itemId" :tree-props="{ children: 'children' }" default-expand-all empty-text="未录入BOM明细">
              <el-table-column label="物料编码" prop="materialCode" min-width="160">
                <template #default="scope"><span class="col-mono">{{ scope.row.materialCode }}</span></template>
              </el-table-column>
              <el-table-column label="物料名称" prop="materialName" min-width="160" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" width="130" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.specModel || '-' }}</template>
              </el-table-column>
              <el-table-column label="单位" prop="unit" width="80" align="center">
                <template #default="scope">{{ unitLabel(scope.row.unit) }}</template>
              </el-table-column>
              <el-table-column label="用量" prop="usageQty" width="110" align="right" />
              <el-table-column label="损耗率(%)" prop="lossRate" width="100" align="right">
                <template #default="scope">{{ scope.row.lossRate != null ? scope.row.lossRate : 0 }}</template>
              </el-table-column>
              <el-table-column label="备注" prop="remark" min-width="110" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.remark || '-' }}</template>
              </el-table-column>
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

    <!-- 生效对话框 -->
    <el-dialog v-model="effectiveOpen" width="520px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/></svg></div>
          <span class="rd-detail-header-title">BOM生效</span>
        </div>
      </template>
      <el-form ref="effectiveRef" :model="effectiveForm" :rules="effectiveRules" label-width="100px">
        <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px" title="生效后BOM转为已生效状态，旧版本自动转历史；已生效BOM修订保存时自动升版" />
        <el-form-item label="生效说明" prop="remark">
          <el-input v-model="effectiveForm.remark" type="textarea" :rows="2" placeholder="审批依据说明（BOM评审结论等）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitEffective">确认生效</el-button>
        <el-button @click="effectiveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 下发记录对话框 -->
    <el-dialog v-model="releaseOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">MBOM下发记录</span>
          <div class="rd-detail-header-sub" v-if="releaseBom.bomNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ releaseBom.bomNo }}</span>
          </div>
        </div>
      </template>
      <el-table v-loading="releaseLoading" :data="releaseList" border size="small" empty-text="该BOM暂无下发记录">
        <el-table-column label="下发时间" prop="releaseTime" width="160" align="center" />
        <el-table-column label="下发人" prop="releaseBy" width="90" align="center" />
        <el-table-column label="EBOM版本" prop="ebomVersion" width="100" align="center">
          <template #default="scope"><span class="badge blue">{{ scope.row.ebomVersion || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="制造BOM编号" prop="mmsBomNo" width="160">
          <template #default="scope"><span class="col-mono">{{ scope.row.mmsBomNo || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="MBOM版本" prop="mmsVersion" width="100" align="center" />
        <el-table-column label="明细行数" prop="itemCount" width="85" align="center">
          <template #default="scope"><span class="badge gray">{{ scope.row.itemCount || 0 }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope"><dict-tag :options="pdm_release_status" :value="scope.row.releaseStatus" /></template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="150" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.remark || '-' }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="releaseOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 产品档案选择器 -->
    <PdmProductPicker ref="productPickerRef" title="选择产品档案" @confirm="onProductConfirm" />
    <!-- 物料选择器 -->
    <MaterialPicker ref="materialPickerRef" title="选择物料" @confirm="onMaterialConfirm" />
    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="设计BOM业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="primary">编制中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已生效</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">已停用</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="编制中">BOM 创建后的初始状态，可编辑产品关联、树形明细（挂接子件物料、单机用量），执行「生效」后进入受控状态</el-descriptions-item>
          <el-descriptions-item label="已生效">BOM 受控发布并纳入版本管理，可执行「下发MBOM」将清单推送至仓库模块生成仓库 BOM；此状态下不可直接修改</el-descriptions-item>
          <el-descriptions-item label="已停用">BOM 被新版本替代或产品停产时停用，保留用于历史追溯，已下发的仓库 BOM 同步停用</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">树形结构管理</div>
              <div class="highlight-card-body">BOM 以<strong>树形结构</strong>表达产品组成：父件挂接子件，逐层展开；每个明细行关联仓库物料编码与单机用量</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">下发前置校验</div>
              <div class="highlight-card-body">明细物料未确认仓库编码的 BOM <strong>不允许下发</strong>；下发时自动校验 BOM 必须处于已生效状态</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">新旧版无缝衔接</div>
              <div class="highlight-card-body">重复下发时<strong>旧版仓库 BOM 自动停用</strong>（保留历史与替代关系），新版即时生效，仓库备料不中断</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">版本受控</div>
              <div class="highlight-card-body">同一产品可存在多个 BOM 版本，<strong>同一时间仅一个版本生效</strong>；修订须走技术变更流程升版</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建BOM：</strong>点击「新增」，通过放大镜选择产品档案，填写 BOM 名称并保存
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>构建明细：</strong>在编辑弹窗中逐层添加子件物料（通过物料选择器选取），填写单机用量，确认明细物料编码与仓库一致
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>生效下发：</strong>BOM 评审通过后生效，点击「下发MBOM」推送至仓库模块，仓库即可按 BOM 备料生产
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PdmDesignBom">
import { listDesignBom, getDesignBom, addDesignBom, updateDesignBom, delDesignBom, effectiveDesignBom, releaseMbom, listRelease } from '@/api/pdm/designBom'
import { listVersion } from '@/api/pdm/docVersion'
import { fetchAllPages, downloadXlsx } from '@/utils/csvExport'
import { useColumnResize } from '@/composables/useColumnResize'
import PdmProductPicker from '@/components/PdmProductPicker/index.vue'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { ArrowDown, ArrowRight, CircleClose, Delete, Download, RefreshLeft, Search, Filter, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pdm_designbom_index')
const { pdm_bom_status, pdm_version_effect_basis, wms_unit, pdm_release_status } = proxy.useDict('pdm_bom_status', 'pdm_version_effect_basis', 'wms_unit', 'pdm_release_status')

const bomList = ref([])
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
const materialPickerRef = ref()
const materialEditIndex = ref(-1)
const effectiveOpen = ref(false)
const effectiveForm = ref({ bomId: undefined, remark: undefined })
const effectiveRules = { remark: [{ required: true, message: '请填写生效说明（审批依据）', trigger: 'blur' }] }
const releaseOpen = ref(false)
const releaseLoading = ref(false)
const releaseBom = ref({})
const releaseList = ref([])

const defaultColumns = {
  bomNo: { label: 'BOM编号', visible: true },
  bomName: { label: 'BOM名称', visible: true },
  productCode: { label: '产品编码', visible: true },
  productName: { label: '产品名称', visible: true },
  baseQty: { label: '单台用量', visible: true },
  itemCount: { label: '明细数', visible: true },
  bomStatus: { label: 'BOM状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pdm_designbom_columns')
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
    bomNo: undefined,
    bomName: undefined,
    productName: undefined,
    productCode: undefined,
    bomStatus: undefined,
    params: {}
  },
  rules: {
    bomName: [{ required: true, message: '请输入BOM名称', trigger: 'blur' }],
    productId: [{ required: true, message: '请选择产品档案', trigger: 'change' }],
    baseQty: [{ required: true, message: '请输入单台用量', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查看弹窗的明细树 */
const viewItemTree = computed(() => buildItemTree(viewData.value.itemList || []))

function getList() {
  loading.value = true
  listDesignBom(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    bomList.value = response.rows
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

function unitLabel(unit) { const item = wms_unit.value.find(d => d.value == unit); return item ? item.label : (unit || '-') }
function statusLabel(status) { const item = pdm_bom_status.value.find(d => d.value == status); return item ? item.label : '-' }
function statusBadgeClass(status) { const item = pdm_bom_status.value.find(d => d.value == status); const map = { 'info': 'gray', 'success': 'green', 'danger': 'red', 'warning': 'amber', 'primary': 'blue' }; return map[item ? item.listClass : ''] || 'gray' }
function statusTabClass(value) { return value === '0' ? 'tab-draft' : (value === '1' ? 'tab-final' : 'tab-stopped') }

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
  loadStatusCounts()
}

function resetQuery() {
  queryParams.value.bomNo = undefined
  queryParams.value.bomName = undefined
  queryParams.value.productName = undefined
  queryParams.value.productCode = undefined
  queryParams.value.bomStatus = undefined
  queryParams.value.params = {}
  dateRange.value = []
  activeStatusTab.value = 'all'
  handleQuery()
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.bomStatus = status === 'all' ? undefined : status
  handleQuery()
}

function loadStatusCounts() {
  listDesignBom({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total };
    (pdm_bom_status.value || []).forEach(d => { counts[d.value] = 0 });
    (res.rows || []).forEach(r => { if (counts[r.bomStatus] !== undefined) counts[r.bomStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.bomId)
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    bomId: undefined,
    bomName: undefined,
    productId: undefined,
    productName: undefined,
    baseQty: 1,
    baseUnit: undefined,
    remark: undefined,
    itemList: []
  }
  proxy.resetForm('bomRef')
}

/** 平铺明细转树（按 parentId 组装 children） */
function buildItemTree(items) {
  const nodeMap = {}
  const roots = []
  items.forEach(it => { nodeMap[it.itemId] = { ...it, children: [] } })
  items.forEach(it => {
    const node = nodeMap[it.itemId]
    if (it.parentId && nodeMap[it.parentId]) {
      nodeMap[it.parentId].children.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

/** 已有明细的 parentId 转回前端编辑用的 parentIndex */
function deriveParentIndex(itemList) {
  const idxOf = {}
  itemList.forEach((it, i) => { idxOf[it.itemId] = i })
  itemList.forEach(it => {
    it.parentIndex = (it.parentId && idxOf[it.parentId] !== undefined) ? idxOf[it.parentId] : -1
  })
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加设计BOM'
}

function handleUpdate(row) {
  reset()
  const bomId = row.bomId || ids.value[0]
  getDesignBom(bomId).then(response => {
    form.value = response.data
    if (!form.value.itemList) form.value.itemList = []
    deriveParentIndex(form.value.itemList)
    open.value = true
    title.value = '修改设计BOM'
  })
}

function handleView(row) {
  getDesignBom(row.bomId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
    loadVersions(row.bomId)
  })
}

function loadVersions(bomId) {
  listVersion('3', bomId).then(res => { versionList.value = res.data || [] })
}

function handleEffective(row) {
  // 版本由后端自动生成（首次生效生成 V1.0，重复生效取最新未生效版本），无需用户选择
  effectiveForm.value = { bomId: row.bomId, remark: undefined }
  effectiveOpen.value = true
}

function submitEffective() {
  proxy.$refs['effectiveRef'].validate(valid => {
    if (valid) {
      effectiveDesignBom(effectiveForm.value).then(() => {
        proxy.$modal.msgSuccess('生效成功，BOM已纳入受控管理')
        effectiveOpen.value = false
        getList(); loadStatusCounts()
      })
    }
  })
}

/** 下发MBOM（仅生效BOM；同产品旧制造BOM自动停用替代） */
function handleRelease(row) {
  proxy.$modal.confirm('将BOM "' + (row.bomNo || '') + '" 下发转换为制造BOM（生产模块接收）？产品存在待确认物料申请时将不允许下发。').then(() => {
    return releaseMbom(row.bomId)
  }).then(res => {
    const bomNo = res.data || ''
    proxy.$modal.msgSuccess(bomNo ? '下发成功，制造BOM已生成：' + bomNo : '下发成功')
    getList()
    releaseBom.value = { bomId: row.bomId, bomNo: row.bomNo }
    loadReleaseList(row.bomId)
    releaseOpen.value = true
  }).catch(() => {})
}

/** 查看下发记录 */
function handleReleaseRecord(row) {
  releaseBom.value = { bomId: row.bomId, bomNo: row.bomNo }
  loadReleaseList(row.bomId)
  releaseOpen.value = true
}

function loadReleaseList(bomId) {
  releaseLoading.value = true
  listRelease({ pageNum: 1, pageSize: 50, bomId: bomId }).then(res => {
    releaseList.value = res.rows || []
    releaseLoading.value = false
  }).catch(() => { releaseLoading.value = false })
}

function openProductPicker() {
  productPickerRef.value.open(form.value.productId)
}

function onProductConfirm(product) {
  form.value.productId = product.productId
  form.value.productName = product.productName
  if (!form.value.baseUnit) form.value.baseUnit = product.unit
  proxy.$refs['bomRef'].clearValidate(['productId', 'baseQty', 'baseUnit'])
}

/** 清除产品档案（联动清除产品单位） */
function clearProduct() {
  form.value.productId = undefined
  form.value.productName = undefined
  form.value.baseUnit = undefined
  proxy.$refs['bomRef'].clearValidate(['productId'])
}

/** 清除明细行物料 */
function clearItemMaterial(index) {
  const row = form.value.itemList[index]
  if (row) {
    row.materialId = undefined
    row.materialCode = undefined
    row.materialName = undefined
    row.specModel = undefined
    row.unit = undefined
  }
}

function addItemRow() {
  form.value.itemList.push({
    parentIndex: -1,
    materialId: undefined,
    materialCode: undefined,
    materialName: undefined,
    specModel: undefined,
    unit: undefined,
    usageQty: undefined,
    lossRate: undefined,
    remark: undefined
  })
}

function removeItemRow(index) {
  form.value.itemList.splice(index, 1)
  // 被删行之后的行若引用了受影响的父下标，需要修正
  form.value.itemList.forEach(it => {
    if (it.parentIndex === index) it.parentIndex = -1
    else if (it.parentIndex > index) it.parentIndex = it.parentIndex - 1
  })
}

function openMaterialPicker(index) {
  materialEditIndex.value = index
  materialPickerRef.value.open()
}

function onMaterialConfirm(material) {
  const row = form.value.itemList[materialEditIndex.value]
  if (row) {
    row.materialId = material.materialId
    row.materialCode = material.materialCode
    row.materialName = material.materialName
    row.specModel = material.specModel
    row.unit = material.unit
  }
}

function submitForm() {
  proxy.$refs['bomRef'].validate(valid => {
    if (!valid) return
    const items = form.value.itemList || []
    if (!items.length) {
      proxy.$modal.msgWarning('请至少录入一条BOM明细')
      return
    }
    for (let i = 0; i < items.length; i++) {
      const it = items[i]
      if (!it.materialId) { proxy.$modal.msgWarning('第 ' + (i + 1) + ' 行请选择物料'); return }
      if (it.usageQty == null || it.usageQty === '' || Number(it.usageQty) <= 0) { proxy.$modal.msgWarning('第 ' + (i + 1) + ' 行请填写大于0的用量'); return }
    }
    if (form.value.bomId != undefined) {
      updateDesignBom(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList(); loadStatusCounts()
      })
    } else {
      addDesignBom(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功，已自动创建 V1.0 版本记录')
        open.value = false
        getList(); loadStatusCounts()
      })
    }
  })
}

function handleDelete(row) {
  const bomIds = row.bomId || ids.value
  proxy.$modal.confirm('是否确认删除选中的设计BOM？仅编制中状态允许删除。').then(function () {
    return delDesignBom(bomIds)
  }).then(() => {
    getList(); loadStatusCounts()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

async function handleExport() {
  let rows = await fetchAllPages(listDesignBom, proxy.addDateRange(queryParams.value, dateRange.value))
  if (ids.value && ids.value.length) { rows = rows.filter(i => ids.value.includes(i.bomId)) }
  if (!rows.length) { proxy.$modal.msgWarning('当前筛选下无数据可导出'); return }
  const headers = ['BOM编号', 'BOM名称', '产品编码', '产品名称', '单台用量', '明细数', 'BOM状态', '创建时间']
  downloadXlsx(`pdm_design_bom_${new Date().getTime()}`, headers, rows.map(i => [
    i.bomNo || '', i.bomName || '', i.productCode || '', i.productName || '',
    i.baseQty != null ? i.baseQty + ' ' + unitLabel(i.baseUnit) : '', i.itemCount || 0, statusLabel(i.bomStatus), i.createTime || ''
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
.pdm-designbom-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.pdm-designbom-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.pdm-designbom-page .filter-card { padding:14px 20px 16px; }
.pdm-designbom-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pdm-designbom-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pdm-designbom-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pdm-designbom-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pdm-designbom-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pdm-designbom-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pdm-designbom-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pdm-designbom-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pdm-designbom-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pdm-designbom-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pdm-designbom-page .filter-card .filter-buttons { display:flex; gap:8px; }
.pdm-designbom-page .field { display:flex; flex-direction:column; gap:6px; }
.pdm-designbom-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.pdm-designbom-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pdm-designbom-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.pdm-designbom-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pdm-designbom-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; }
.pdm-designbom-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pdm-designbom-page .status-tabs { display:flex; align-items:center; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pdm-designbom-page .tabs-track { display:flex; align-items:center; gap:4px; flex-wrap:wrap; flex:1; min-width:0; }
.pdm-designbom-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pdm-designbom-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pdm-designbom-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; }
.pdm-designbom-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pdm-designbom-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pdm-designbom-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pdm-designbom-page .status-tab.is-active .dot { background:var(--brand-500); }
.pdm-designbom-page .status-tab.tab-draft .dot { background:var(--blue-500); }
.pdm-designbom-page .status-tab.is-active.tab-draft .count { background:var(--blue-500); color:#fff; }
.pdm-designbom-page .status-tab.tab-final .dot { background:var(--green-500); }
.pdm-designbom-page .status-tab.is-active.tab-final .count { background:var(--green-500); color:#fff; }
.pdm-designbom-page .status-tab.tab-stopped .dot { background:var(--red-500); }
.pdm-designbom-page .status-tab.is-active.tab-stopped .count { background:var(--red-500); color:#fff; }
.pdm-designbom-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pdm-designbom-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pdm-designbom-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pdm-designbom-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.pdm-designbom-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pdm-designbom-page .btn-soft .el-icon { font-size:14px; }
.pdm-designbom-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pdm-designbom-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pdm-designbom-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pdm-designbom-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pdm-designbom-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pdm-designbom-page .table-wrap { overflow-x:auto; }
.pdm-designbom-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pdm-designbom-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pdm-designbom-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pdm-designbom-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pdm-designbom-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pdm-designbom-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pdm-designbom-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pdm-designbom-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pdm-designbom-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.pdm-designbom-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pdm-designbom-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pdm-designbom-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.pdm-designbom-page .badge.blue .dot { background:var(--blue-500); }
.pdm-designbom-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pdm-designbom-page .badge.green .dot { background:var(--green-500); }
.pdm-designbom-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pdm-designbom-page .badge.gray .dot { background:var(--ink-400); }
.pdm-designbom-page .clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.pdm-designbom-page .clear-icon:hover { color: #909399; }
.pdm-designbom-page .form-tip { font-size:12px; color:var(--ink-500); line-height:1.5; margin-top:2px; width: 100%; }
.pdm-designbom-page .cell-text { font-size:13px; color:var(--ink-500); }
.pdm-designbom-page :deep(.col-action) { padding: 6px 4px !important; }
.pdm-designbom-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.pdm-designbom-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.pdm-designbom-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.pdm-designbom-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }
@media (max-width:1100px) { .pdm-designbom-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pdm-designbom-page .filter-card .filter-bar { grid-template-columns:1fr; } .pdm-designbom-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>

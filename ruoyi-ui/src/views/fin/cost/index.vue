<template>
  <div class="app-container fin-cost-page">
    <!-- 计价总览（stat-card 样式，对齐应收管理页面；结转流水/物料税率维护页不显示——统计为成本台账数据，与流水、税率档案无关） -->
    <el-row :gutter="12" class="mb8" v-if="!isIndependent || (tab !== 'tax' && tab !== 'carry')">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">存货总成本（元）</div>
          <div class="stat-value">{{ formatAmount(totalCost) }}</div>
          <div class="stat-hint">资产负债表「存货」科目取数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">台账记录数</div>
          <div class="stat-value">{{ total }}</div>
          <div class="stat-hint">物料 × 仓库 × 库位 × 批次 四维</div>
        </div>
      </el-col>
    </el-row>

    <!-- 筛选条件卡片（独立菜单模式下按页面拆分：税率页无筛选不显示，且不再显示卡片标题头） -->
    <div class="surface filter-card" v-show="showSearch && (!isIndependent || tab !== 'tax')">
      <div class="filter-head" v-if="!isIndependent">
        <div class="filter-title">
          <span class="glyph"></span> 筛选条件
        </div>
      </div>
      <div class="filter-bar">
        <div class="field" v-if="!isIndependent || tab === 'cost'">
          <label>物料编码</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="请输入编码" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-if="!isIndependent || tab === 'cost'">
          <label>物料名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-if="!isIndependent || tab === 'carry'">
          <label>结转类型</label>
          <div class="control is-select">
            <el-select v-model="carryQuery.sourceType" placeholder="全部类型" clearable @change="getCarryList">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option label="入库" value="inbound" />
              <el-option label="出库" value="outbound" />
            </el-select>
          </div>
        </div>
        <div class="field" v-if="!isIndependent || tab === 'carry'">
          <label>结转日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
                            range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"
                            @change="getCarryList" style="width: 100%" />
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

    <!-- 数据列表卡片 -->
    <div class="surface">
      <div class="toolbar" v-if="!isIndependent || tab !== 'tax'">
        <div class="toolbar-left">
          <el-button v-if="!isIndependent || tab === 'cost'" v-hasPermi="['fin:cost:carry']" type="primary" icon="Download" @click="handlePriceInbound">入库计价</el-button>
          <el-button v-if="!isIndependent || tab === 'cost'" v-hasPermi="['fin:cost:carry']" type="warning" icon="Upload" @click="handleCarryOutbound">出库结转</el-button>
          <el-button v-if="!isIndependent || tab === 'cost'" v-hasPermi="['fin:cost:carry']" type="success" icon="Edit" @click="handleInit">录入期初成本</el-button>
          <el-button v-if="!isIndependent" icon="List" @click="tab = 'carry'">结转流水</el-button>
          <el-button v-if="!isIndependent || tab === 'cost'" icon="DataAnalysis" @click="showValuation = true">存货计价表</el-button>
          <el-button v-hasPermi="['fin:cost:export']" icon="Download" @click="handleExport">导出</el-button>
        </div>
        <div class="toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" :columns="columns" storageKey="fin_cost_ledger_columns" @queryTable="handleQuery" />
          <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
        </div>
      </div>

      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>
          <span class="hint-text">
            入库单价统一按<b>含税</b>录入，系统自动除税得不含税成本（默认 13%）；
            零税率物料须在「物料税率维护」中显式维护。入库单价为 0 的明细会被<b>跳过</b>，请先在 WMS 补录。
          </span>
        </template>
      </el-alert>

      <el-tabs v-model="tab" :class="{ 'cost-tabs-solo': isIndependent }">
        <!-- ========== 成本台账 ========== -->
        <el-tab-pane v-if="!isIndependent || tab === 'cost'" label="成本台账" name="cost">
          <div class="table-wrap">
            <el-table v-loading="loading" :data="costList" border
                      :ref="costCol.tableRef" @header-dragend="costCol.onHeaderDragEnd">
              <el-table-column type="index" label="序号" width="85" align="center" />
              <el-table-column prop="materialCode" key="materialCode" label="物料编码" class-name="col-mono"
                               :width="costCol.colWidth('materialCode', 140)" resizable align="center" v-if="columns.materialCode.visible" />
              <el-table-column prop="materialName" key="materialName" label="物料名称"
                               :min-width="costCol.colWidth('materialName', 180)" show-overflow-tooltip
                               resizable align="center" v-if="columns.materialName.visible" />
              <el-table-column prop="warehouseId" key="warehouseId" label="仓库"
                               :width="costCol.colWidth('warehouseId', 110)" resizable align="center" v-if="columns.warehouseId.visible">
                <template #default="{ row }">{{ warehouseLabel(row.warehouseId) }}</template>
              </el-table-column>
              <el-table-column prop="locationId" key="locationId" label="库位"
                               :width="costCol.colWidth('locationId', 110)" resizable align="center" v-if="columns.locationId.visible">
                <template #default="{ row }">{{ warehouseLabel(row.locationId) }}</template>
              </el-table-column>
              <el-table-column prop="batchNo" key="batchNo" label="批次号"
                               :width="costCol.colWidth('batchNo', 130)" resizable align="center" v-if="columns.batchNo.visible">
                <template #default="{ row }">
                  <span v-if="row.batchNo">{{ row.batchNo }}</span>
                  <span v-else class="text-muted">—</span>
                </template>
              </el-table-column>
              <el-table-column prop="qty" key="qty" label="结存数量"
                               :width="costCol.colWidth('qty', 120)" resizable align="center" v-if="columns.qty.visible">
                <template #default="{ row }">{{ formatAmount(row.qty) }}</template>
              </el-table-column>
              <el-table-column prop="unitCost" key="unitCost" label="单位成本(不含税)"
                               :width="costCol.colWidth('unitCost', 150)" resizable align="center" v-if="columns.unitCost.visible">
                <template #default="{ row }">
                  <span class="amount-cell">{{ formatAmount(row.unitCost) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="totalCost" key="totalCost" label="结存金额"
                               :width="costCol.colWidth('totalCost', 150)" resizable align="center" v-if="columns.totalCost.visible">
                <template #default="{ row }">
                  <span class="amount-cell strong">{{ formatAmount(row.totalCost) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="costMethod" key="costMethod" label="计价方法"
                               :width="costCol.colWidth('costMethod', 120)" resizable align="center" v-if="columns.costMethod.visible">
                <template #default="{ row }">
                  <span class="badge" :class="row.costMethod === 'FIFO' ? 'blue' : 'violet'">
                    {{ row.costMethod === 'FIFO' ? '先进先出' : '加权平均' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="version" key="version" label="版本"
                               :width="costCol.colWidth('version', 70)" resizable align="center" v-if="columns.version.visible">
                <template #default="{ row }">
                  <span class="text-muted">v{{ row.version }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="queryParams.pageNum"
              v-model:page-size="queryParams.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper" />
          </div>
        </el-tab-pane>

        <!-- ========== 结转流水 ========== -->
        <el-tab-pane v-if="!isIndependent || tab === 'carry'" label="结转流水" name="carry">
          <div class="table-wrap">
            <el-table v-loading="carryLoading" :data="carryList" border
                      :ref="carryCol.tableRef" @header-dragend="carryCol.onHeaderDragEnd">
              <el-table-column type="index" label="序号" width="85" align="center" />
              <el-table-column prop="cfNo" key="cfNo" label="流水号" class-name="col-mono"
                               :width="carryCol.colWidth('cfNo', 150)" resizable align="center" v-if="carryColumns.cfNo.visible" />
              <el-table-column prop="costDate" key="costDate" label="结转日期"
                               :width="carryCol.colWidth('costDate', 110)" resizable align="center" v-if="carryColumns.costDate.visible" />
              <el-table-column prop="period" key="period" label="会计期间"
                               :width="carryCol.colWidth('period', 90)" resizable align="center" v-if="carryColumns.period.visible" />
              <el-table-column prop="sourceType" key="sourceType" label="类型"
                               :width="carryCol.colWidth('sourceType', 80)" resizable align="center" v-if="carryColumns.sourceType.visible">
                <template #default="{ row }">
                  <span class="badge" :class="row.sourceType === 'inbound' ? 'green' : 'amber'">
                    {{ row.sourceType === 'inbound' ? '入库' : '出库' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="materialCode" key="materialCode" label="物料编码" class-name="col-mono"
                               :width="carryCol.colWidth('materialCode', 140)" resizable align="center" v-if="carryColumns.materialCode.visible" />
              <el-table-column prop="qty" key="qty" label="数量"
                               :width="carryCol.colWidth('qty', 120)" resizable align="center" v-if="carryColumns.qty.visible">
                <template #default="{ row }">{{ formatAmount(row.qty) }}</template>
              </el-table-column>
              <el-table-column prop="unitCost" key="unitCost" label="单位成本"
                               :width="carryCol.colWidth('unitCost', 120)" resizable align="center" v-if="carryColumns.unitCost.visible">
                <template #default="{ row }">
                  <span class="amount-cell">{{ formatAmount(row.unitCost) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="totalCost" key="totalCost" label="总成本"
                               :width="carryCol.colWidth('totalCost', 130)" resizable align="center" v-if="carryColumns.totalCost.visible">
                <template #default="{ row }">
                  <span class="amount-cell strong">{{ formatAmount(row.totalCost) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="targetLabel" key="targetLabel" label="结转去向"
                               :min-width="carryCol.colWidth('targetLabel', 160)" show-overflow-tooltip
                               resizable align="center" v-if="carryColumns.targetLabel.visible" />
              <el-table-column prop="targetKey" key="targetKey" label="去向单号" class-name="col-mono"
                               :width="carryCol.colWidth('targetKey', 130)" resizable align="center" v-if="carryColumns.targetKey.visible">
                <template #default="{ row }">
                  <span v-if="row.targetKey">{{ row.targetKey }}</span>
                  <span v-else class="text-muted">—</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="carryQuery.pageNum"
              v-model:page-size="carryQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="carryTotal"
              layout="total, sizes, prev, pager, next, jumper" />
          </div>
        </el-tab-pane>

        <!-- ========== 物料税率维护 ========== -->
        <el-tab-pane v-if="!isIndependent || tab === 'tax'" label="物料税率维护" name="tax">
          <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
            <template #title>
              <span class="hint-text">
                共 {{ taxCount }} 个物料已建档，
                <span v-if="untaxedList.length">仍有 <b>{{ untaxedList.length }}</b> 个待建档物料，请及时维护</span>
                <span v-else>全部物料已建档</span>
              </span>
            </template>
          </el-alert>

          <div class="toolbar">
            <div class="toolbar-left">
              <el-button icon="Refresh" @click="getTaxList">刷新</el-button>
              <el-button v-hasPermi="['fin:cost:tax']" type="primary" plain icon="Plus" @click="handleAddTax">新增档案</el-button>
              <el-button icon="Warning" @click="loadUntaxed">待建档物料</el-button>
              <el-button v-hasPermi="['fin:cost:tax']" type="success" icon="Finished" @click="showBatchInit = true">批量初始化</el-button>
            </div>
          </div>

          <div class="table-wrap">
            <el-table v-loading="taxLoading" :data="taxList" border
                      :ref="taxCol.tableRef" @header-dragend="taxCol.onHeaderDragEnd">
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column type="index" label="序号" width="85" align="center" />
              <el-table-column prop="materialCode" key="materialCode" label="物料编码" class-name="col-mono"
                               :width="taxCol.colWidth('materialCode', 140)" resizable align="center" v-if="taxColumns.materialCode.visible" />
              <el-table-column prop="materialName" key="materialName" label="物料名称"
                               :min-width="taxCol.colWidth('materialName', 180)" show-overflow-tooltip
                               resizable align="center" v-if="taxColumns.materialName.visible" />
              <el-table-column prop="taxRate" key="taxRate" label="税率(%)"
                               :width="taxCol.colWidth('taxRate', 90)" resizable align="center" v-if="taxColumns.taxRate.visible">
                <template #default="{ row }">{{ row.taxRate }}</template>
              </el-table-column>
              <el-table-column prop="isTax" key="isTax" label="是否含税"
                               :width="taxCol.colWidth('isTax', 100)" resizable align="center" v-if="taxColumns.isTax.visible">
                <template #default="{ row }">
                  <span class="badge" :class="row.isTax === 'Y' ? 'green' : 'gray'">{{ row.isTax === 'Y' ? '是' : '否' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="effectiveDate" key="effectiveDate" label="生效日期"
                               :width="taxCol.colWidth('effectiveDate', 110)" resizable align="center" v-if="taxColumns.effectiveDate.visible" />
              <el-table-column prop="expireDate" key="expireDate" label="失效日期"
                               :width="taxCol.colWidth('expireDate', 110)" resizable align="center" v-if="taxColumns.expireDate.visible">
                <template #default="{ row }">
                  <span v-if="row.expireDate">{{ row.expireDate }}</span>
                  <span v-else class="text-muted">长期</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" key="status" label="是否生效"
                               :width="taxCol.colWidth('status', 100)" resizable align="center" v-if="taxColumns.status.visible">
                <template #default="{ row }">
                  <span class="badge" :class="isEffective(row) ? 'green dot' : 'gray dot'">
                    <span class="dot"></span>{{ isEffective(row) ? '生效中' : '已失效' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="remark" key="remark" label="备注"
                               :min-width="taxCol.colWidth('remark', 140)" show-overflow-tooltip
                               resizable align="center" v-if="taxColumns.remark.visible">
                <template #default="{ row }">
                  <span v-if="row.remark">{{ row.remark }}</span>
                  <span v-else class="text-muted">—</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
                <template #default="{ row }">
                  <div class="action-btn-row">
                    <el-button v-hasPermi="['fin:cost:tax']" link type="primary" size="small" @click="handleViewTax(row)">详情</el-button>
                    <el-button v-hasPermi="['fin:cost:tax']" link type="primary" size="small" @click="handleEditTax(row)">修改</el-button>
                    <el-button v-hasPermi="['fin:cost:tax']" link type="danger" size="small" @click="handleDelTax(row)">删除</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 计价结果弹窗 -->
    <el-dialog v-model="showResult" width="640px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 14l2 2 4-4M12 3l7 4v5c0 4.5-3 8.5-7 9-4-.5-7-4.5-7-9V7l7-4z"/>
          </svg>
          <span class="rd-detail-title">入库计价结果</span>
        </div>
      </template>
      <el-alert v-if="priceResult && priceResult.skipped && priceResult.skipped.length" type="warning" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>{{ priceResult.skipped.length }} 条单价为 0 的明细被跳过</template>
      </el-alert>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="入库数量">{{ formatAmount(priceResult && priceResult.qty) }}</el-descriptions-item>
        <el-descriptions-item label="含税金额">{{ formatAmount(priceResult && priceResult.amountTax) }}</el-descriptions-item>
        <el-descriptions-item label="不含税金额">{{ formatAmount(priceResult && priceResult.amount) }}</el-descriptions-item>
        <el-descriptions-item label="单位成本(不含税)">
          <span class="amount-cell strong">{{ formatAmount(priceResult && priceResult.unitCost) }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="showResult = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 存货计价表弹窗 -->
    <el-dialog v-model="showValuation" width="760px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 3v18h18"/><path d="M7 15l4-5 3 3 5-7"/>
          </svg>
          <span class="rd-detail-title">存货计价表</span>
        </div>
      </template>
      <div class="table-wrap">
        <el-table :data="valuationList" border max-height="420">
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column prop="materialCode" label="物料编码" width="140" align="center" />
          <el-table-column prop="materialName" label="物料名称" min-width="160" show-overflow-tooltip align="center" />
          <el-table-column prop="qty" label="结存数量" width="120" align="center">
            <template #default="{ row }">{{ formatAmount(row.qty) }}</template>
          </el-table-column>
          <el-table-column prop="unitCost" label="单位成本" width="130" align="center">
            <template #default="{ row }">
              <span class="amount-cell">{{ formatAmount(row.unitCost) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalCost" label="结存金额" width="130" align="center">
            <template #default="{ row }">
              <span class="amount-cell strong">{{ formatAmount(row.totalCost) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showValuation = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 期初成本录入弹窗 -->
    <el-dialog v-model="showInit" width="680px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/>
          </svg>
          <span class="rd-detail-title">录入期初成本</span>
        </div>
      </template>
      <el-form :model="initForm" label-width="120px">
        <el-form-item label="物料" required>
          <el-input v-model="initMaterialText" readonly placeholder="请选择物料" style="width: 100%" @click="openInitMaterialPicker">
            <template #append><el-button icon="Search" @click="openInitMaterialPicker" /></template>
            <template #suffix>
              <el-icon v-if="initMaterialText" class="clear-icon" @click.stop="clearInitMaterial"><CircleClose /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="仓库" required>
          <el-input v-model="initForm.warehouseName" readonly placeholder="请选择仓库" style="width: 100%" @click="openInitWarehousePicker">
            <template #append><el-button icon="Search" @click="openInitWarehousePicker" /></template>
            <template #suffix>
              <el-icon v-if="initForm.warehouseName" class="clear-icon" @click.stop="clearInitWarehouse"><CircleClose /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="库位">
          <el-input v-model="initForm.locationName" readonly placeholder="请先选择仓库，再选择库位" style="width: 100%"
                    :disabled="!initForm.warehouseId" @click="openInitLocationPicker">
            <template #append><el-button icon="Search" :disabled="!initForm.warehouseId" @click="openInitLocationPicker" /></template>
            <template #suffix>
              <el-icon v-if="initForm.locationName" class="clear-icon" @click.stop="clearInitLocation"><CircleClose /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="initForm.batchNo" placeholder="留空表示不区分批次" />
        </el-form-item>
        <el-form-item label="期初数量">
          <el-input-number v-model="initForm.qty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="含税总金额">
          <el-input-number v-model="initForm.amountTax" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="税率">
          <el-input-number v-model="initForm.taxRate" :min="0" :max="1" :step="0.01" :precision="2" style="width: 100%" />
          <div class="form-tip">如 0.13 表示 13%</div>
        </el-form-item>
        <div class="form-tip">系统自动除税得不含税单位成本，写入台账 version=1 记录</div>
      </el-form>
      <template #footer>
        <el-button @click="showInit = false">取 消</el-button>
        <el-button type="primary" @click="submitInit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 税率编辑弹窗（规范 rd-dialog） -->
    <el-dialog v-model="showTaxEdit" class="rd-dialog" width="560px" append-to-body>
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 14l2 2 4-4M12 3l7 4v5c0 4.5-3 8.5-7 9-4-.5-7-4.5-7-9V7l7-4z"/>
            </svg>
          </div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">{{ taxForm.id ? '修改物料税率档案' : '新增物料税率档案' }}</div>
            <div class="rd-detail-header-sub" v-if="taxForm.id">
              <span class="rd-detail-header-no">物料编码：{{ taxForm.materialCode }}</span>
            </div>
            <div class="rd-detail-header-sub" v-else>
              <span class="rd-detail-header-placeholder">完善物料税率档案信息</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <div class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/></svg></span>基本信息</div>
          </div>
          <div class="rd-card-body">
            <el-form :model="taxForm" label-width="110px">
              <el-form-item label="物料" required>
                <el-input v-model="taxMaterialText" readonly :placeholder="taxForm.id ? '' : '请选择物料'"
                          :disabled="!!taxForm.id" style="width: 100%"
                          @click="!taxForm.id && openTaxMaterialPicker()">
                  <template v-if="!taxForm.id" #append><el-button icon="Search" @click="openTaxMaterialPicker" /></template>
                </el-input>
              </el-form-item>
            </el-form>
          </div>
        </div>
        <div class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="5" x2="5" y2="19"/><circle cx="6.5" cy="6.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></svg></span>税率信息</div>
          </div>
          <div class="rd-card-body">
            <el-form :model="taxForm" label-width="110px">
              <el-form-item label="税率" required>
                <el-input-number v-model="taxForm.taxRate" :min="0" :max="1" :step="0.01" :precision="2" style="width: 100%" />
                <div class="form-tip">如 0.13 表示 13%</div>
              </el-form-item>
              <el-form-item label="是否含税">
                <el-radio-group v-model="taxForm.isTax">
                  <el-radio value="Y">是</el-radio>
                  <el-radio value="N">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item label="生效日期">
                    <el-date-picker v-model="taxForm.effectiveDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="失效日期">
                    <el-date-picker v-model="taxForm.expireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="备注">
                <el-input v-model="taxForm.remark" type="textarea" :rows="2" placeholder="选填" />
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showTaxEdit = false">取 消</el-button>
        <el-button type="primary" @click="submitTax">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 税率档案详情弹窗（规范只读详情） -->
    <el-dialog v-model="viewTaxOpen" class="rd-dialog" width="560px" append-to-body>
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
            </svg>
          </div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">物料税率档案详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.materialCode">
              <span class="rd-detail-header-no">物料编码：{{ viewData.materialCode }}</span>
              <span class="badge" :class="isEffective(viewData) ? 'green dot' : 'gray dot'" style="margin-left: 8px">
                <span class="dot"></span>{{ isEffective(viewData) ? '生效中' : '已失效' }}
              </span>
            </div>
            <div class="rd-detail-header-sub" v-else>
              <span class="rd-detail-header-placeholder">查看详细信息</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <div class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/></svg></span>基本信息</div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item">
                <div class="rd-label">物料编码</div>
                <div class="rd-value">{{ viewData.materialCode || '-' }}</div>
              </div>
              <div class="rd-item">
                <div class="rd-label">物料名称</div>
                <div class="rd-value">{{ viewData.materialName || '-' }}</div>
              </div>
            </div>
          </div>
        </div>
        <div class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="5" x2="5" y2="19"/><circle cx="6.5" cy="6.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></svg></span>税率信息</div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item">
                <div class="rd-label">税率</div>
                <div class="rd-value rd-amount">{{ formatTaxRate(viewData.taxRate) }}</div>
              </div>
              <div class="rd-item">
                <div class="rd-label">是否含税</div>
                <div class="rd-value">{{ viewData.isTax === 'Y' ? '是' : (viewData.isTax === 'N' ? '否' : '-') }}</div>
              </div>
              <div class="rd-item">
                <div class="rd-label">生效日期</div>
                <div class="rd-value">{{ viewData.effectiveDate || '-' }}</div>
              </div>
              <div class="rd-item">
                <div class="rd-label">失效日期</div>
                <div class="rd-value">{{ viewData.expireDate || '长期' }}</div>
              </div>
              <div class="rd-item rd-item--full">
                <div class="rd-label">备注</div>
                <div class="rd-value">{{ viewData.remark || '-' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="viewTaxOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 待建档物料弹窗 -->
    <el-dialog v-model="showUntaxed" width="640px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><path d="M12 9v4"/><path d="M12 17h.01"/>
          </svg>
          <span class="rd-detail-title">待建档物料</span>
        </div>
      </template>
      <el-alert v-if="untaxedList.length" type="warning" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>以下 {{ untaxedList.length }} 个物料尚未维护税率档案，入库计价将按默认 13% 处理</template>
      </el-alert>
      <el-empty v-else description="全部物料已建档" :image-size="80" />
      <div class="table-wrap" v-if="untaxedList.length">
        <el-table :data="untaxedList" border max-height="360">
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column prop="code" label="物料编码" width="150" align="center" />
          <el-table-column prop="name" label="物料名称" min-width="160" show-overflow-tooltip align="center" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showUntaxed = false">关 闭</el-button>
        <el-button v-if="untaxedList.length" type="primary" icon="Finished" @click="quickAddTax">一键建档(默认13%)</el-button>
      </template>
    </el-dialog>

    <!-- 批量初始化弹窗 -->
    <el-dialog v-model="showBatchInit" width="480px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
          </svg>
          <span class="rd-detail-title">批量初始化税率档案</span>
        </div>
      </template>
      <el-form label-width="110px">
        <el-form-item label="默认税率">
          <el-input-number v-model="batchInitForm.taxRate" :min="0" :max="1" :step="0.01" :precision="2" style="width: 100%" />
          <div class="form-tip">如 0.13 表示 13%</div>
        </el-form-item>
        <el-form-item label="是否含税">
          <el-radio-group v-model="batchInitForm.isTax">
            <el-radio value="Y">是</el-radio>
            <el-radio value="N">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <div class="form-tip">仅为未建档物料创建税率档案，已存在档案的物料不受影响</div>
      </el-form>
      <template #footer>
        <el-button @click="showBatchInit = false">取 消</el-button>
        <el-button type="primary" @click="submitBatchInit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 物料 / 仓库 / 库位 弹框选择器（picker 规范） -->
    <material-picker ref="initMaterialPickerRef" title="选择物料" @confirm="onInitMaterialConfirm" />
    <warehouse-picker ref="initWarehousePickerRef" title="选择仓库" @confirm="onInitWarehouseConfirm" />
    <location-picker ref="initLocationPickerRef" title="选择库位" @confirm="onInitLocationConfirm" />
    <material-picker ref="taxMaterialPickerRef" title="选择物料" @confirm="onTaxMaterialConfirm" />
    <el-dialog v-model="showStatusHelp" title="成本台账业务操作说明" width="820px" append-to-body>
      <div class="status-help-content"><h4>一、状态流转图</h4><div class="status-flow"><el-tag type="primary">录入期初</el-tag><span class="flow-arrow">→</span><el-tag type="warning">入库计价</el-tag><span class="flow-arrow">→</span><el-tag type="success">出库结转</el-tag></div><h4>二、各状态说明</h4><el-descriptions :column="1" border><el-descriptions-item label="录入期初">新启用物料/仓库时录入期初成本数据，作为计价的基础</el-descriptions-item><el-descriptions-item label="入库计价">按物料计价方法计算入库单价并更新库存成本</el-descriptions-item><el-descriptions-item label="出库结转">按计价方法结转出库成本，生成结转单号（CB 前缀）</el-descriptions-item></el-descriptions><h4>三、重点业务规则</h4><el-row :gutter="16"><el-col :span="12"><div class="highlight-card highlight-primary"><div class="highlight-card-title">两级计价配置</div><div class="highlight-card-body">成本方法支持<strong>全局默认与指定物料</strong>两级配置，指定物料优先级高于全局默认</div></div></el-col><el-col :span="12"><div class="highlight-card highlight-warning"><div class="highlight-card-title">加权平均</div><div class="highlight-card-body">按加权平均法计算：出库单价 =（期初金额 + 入库金额）/（期初数量 + 入库数量）</div></div></el-col></el-row><el-row :gutter="16" style="margin-top: 12px;"><el-col :span="12"><div class="highlight-card highlight-success"><div class="highlight-card-title">先进先出</div><div class="highlight-card-body">按先进先出（FIFO）结转：<strong>最早批次成本先行结转</strong>，批次维度跟踪结转金额</div></div></el-col><el-col :span="12"><div class="highlight-card highlight-danger"><div class="highlight-card-title">结转流水可追溯</div><div class="highlight-card-body">每次入库计价与出库结转均生成结转流水，<strong>成本来源与去向全程可追溯</strong></div></div></el-col></el-row><h4>四、业务操作流程</h4><el-timeline><el-timeline-item type="primary" :hollow="true"><strong>录入期初：</strong>新启用时点击「录入期初成本」，录入各物料/仓库的期初数量与金额</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>入库计价：</strong>点击「入库计价」，系统按物料计价方法计算入库成本</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>出库结转：</strong>点击「出库结转」，按计价方法结转出库成本并生成结转单</el-timeline-item><el-timeline-item type="info" :hollow="true"><strong>流水追溯：</strong>在结转流水页签查看历次计价与结转明细</el-timeline-item></el-timeline></div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="Cost">
import { WarningFilled } from '@element-plus/icons-vue'
const showStatusHelp = ref(false)
import { listCost, priceInbound, carryOutbound, getValuation, getTotalCost, initCost, listCarry } from "@/api/fin/cost";
import { listMaterialTax, addMaterialTax, updateMaterialTax, delMaterialTax, listUntaxedMaterials, countMaterialTax, batchInitTax } from "@/api/fin/materialTax";
import { useColumnResize } from "@/composables/useColumnResize";
import MaterialPicker from '@/components/MaterialPicker/index.vue';
import WarehousePicker from '@/components/WarehousePicker/index.vue';
import LocationPicker from '@/components/LocationPicker/index.vue';
import { listWarehouseTree } from '@/api/wms/warehouse';

const { proxy } = getCurrentInstance();
const tab = ref("cost");

/** 菜单重构（路径A）：成本台账/结转流水/物料税率维护独立菜单，按路由 path 激活对应页签 */
const route = useRoute();
const PATH_TABS = [["/ledger", "cost"], ["/carry", "carry"], ["/tax", "tax"]];
const matchedTab = PATH_TABS.find(([p]) => route.path.endsWith(p));
if (matchedTab) tab.value = matchedTab[1];
/** 独立菜单模式下仅展示对应分区并隐藏页签栏 */
const isIndependent = computed(() => PATH_TABS.some(([p]) => route.path.endsWith(p)));

/* ---------- 列宽管理（每页签独立） ---------- */
const costCol = useColumnResize("fin_cost_ledger_index");
const carryCol = useColumnResize("fin_cost_carry_index");
const taxCol = useColumnResize("fin_cost_tax_index");
const columns = reactive({
  materialCode: { visible: true },
  materialName: { visible: true },
  warehouseId: { visible: true },
  locationId: { visible: true },
  batchNo: { visible: true },
  qty: { visible: true },
  unitCost: { visible: true },
  totalCost: { visible: true },
  costMethod: { visible: true },
  version: { visible: true },
});
const carryColumns = reactive({
  cfNo: { visible: true },
  costDate: { visible: true },
  period: { visible: true },
  sourceType: { visible: true },
  materialCode: { visible: true },
  qty: { visible: true },
  unitCost: { visible: true },
  totalCost: { visible: true },
  targetLabel: { visible: true },
  targetKey: { visible: true },
});
const taxColumns = reactive({
  materialCode: { visible: true },
  materialName: { visible: true },
  taxRate: { visible: true },
  isTax: { visible: true },
  effectiveDate: { visible: true },
  expireDate: { visible: true },
  status: { visible: true },
  remark: { visible: true },
});
function loadColumnVisibility() {
  // 列显隐默认全开；宽度由 useColumnResize 持久化（localStorage + sys_table_config）
}

/* ---------- 查询参数 ---------- */
const showSearch = ref(true);
const dateRange = ref([]);
const activeFilterCount = computed(() => {
  let c = 0;
  if (queryParams.materialCode) c++;
  if (queryParams.materialName) c++;
  if (carryQuery.sourceType) c++;
  if (dateRange.value && dateRange.value.length === 2) c++;
  return c;
});
const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  materialCode: null,
  materialName: null,
});
const carryQuery = reactive({
  pageNum: 1,
  pageSize: 20,
  sourceType: null,
});

/* ---------- 成本台账 ---------- */
const loading = ref(false);
const costList = ref([]);
const total = ref(0);

function getList() {
  loading.value = true;
  listCost(queryParams).then(response => {
    costList.value = response.rows;
    total.value = response.total;
    costCol.applySavedWidths();
  }).finally(() => {
    loading.value = false;
  });
}

/* ---------- 结转流水 ---------- */
const carryLoading = ref(false);
const carryList = ref([]);
const carryTotal = ref(0);

function getCarryList() {
  carryLoading.value = true;
  const params = { ...carryQuery };
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginDate = dateRange.value[0];
    params.endDate = dateRange.value[1];
  }
  listCarry(params).then(response => {
    carryList.value = response.rows;
    carryTotal.value = response.total;
    carryCol.applySavedWidths();
  }).finally(() => {
    carryLoading.value = false;
  });
}

/* ---------- 物料税率 ---------- */
const taxLoading = ref(false);
const taxList = ref([]);
const taxCount = ref(0);
const untaxedList = ref([]);
const showTaxEdit = ref(false);
const viewTaxOpen = ref(false);
const viewData = ref({});
const showUntaxed = ref(false);
const showBatchInit = ref(false);
const taxForm = reactive({});

function getTaxList() {
  taxLoading.value = true;
  listMaterialTax({ pageNum: 1, pageSize: 200 }).then(response => {
    taxList.value = response.rows;
    taxCol.applySavedWidths();
  }).finally(() => {
    taxLoading.value = false;
  });
  countMaterialTax().then(res => { taxCount.value = res.data || 0; });
}
function loadUntaxed() {
  listUntaxedMaterials().then(res => {
    untaxedList.value = res.data || [];
    showUntaxed.value = true;
  });
}
function onMaterialChange() {}
function handleAddTax() {
  Object.keys(taxForm).forEach(k => delete taxForm[k]);
  taxForm.taxRate = 0.13;
  taxForm.isTax = "Y";
  showTaxEdit.value = true;
}
function quickAddTax() {
  batchInitTax(0.13).then(() => {
    proxy.$modal.msgSuccess("批量建档完成");
    showUntaxed.value = false;
    getTaxList();
  });
}
function handleEditTax(row) {
  Object.keys(taxForm).forEach(k => delete taxForm[k]);
  Object.assign(taxForm, row);
  showTaxEdit.value = true;
}
function handleViewTax(row) {
  viewData.value = { ...row };
  viewTaxOpen.value = true;
}
function submitTax() {
  if (!taxForm.materialCode || taxForm.taxRate == null) {
    proxy.$modal.msgWarning("请完善物料编码与税率");
    return;
  }
  const req = taxForm.id ? updateMaterialTax(taxForm) : addMaterialTax(taxForm);
  req.then(() => {
    proxy.$modal.msgSuccess(taxForm.id ? "修改成功" : "新增成功");
    showTaxEdit.value = false;
    getTaxList();
  });
}
function handleDelTax(row) {
  proxy.$modal.confirm(`确认删除物料「${row.materialCode}」的税率档案吗？`).then(() => {
    return delMaterialTax(row.id);
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功");
    getTaxList();
  }).catch(() => {});
}
const batchInitForm = reactive({ taxRate: 0.13, isTax: "Y" });
function submitBatchInit() {
  batchInitTax(batchInitForm.taxRate).then(res => {
    proxy.$modal.msgSuccess(`已初始化 ${res.data || 0} 个物料档案`);
    showBatchInit.value = false;
    getTaxList();
  });
}

/* ---------- 仓库结构映射（仓库/库位 ID → 名称，取仓库管理数据） ---------- */
const warehouseMap = ref({});
function loadWarehouseMap() {
  listWarehouseTree({ pageNum: 1, pageSize: 500 }).then(res => {
    const map = {};
    (res.rows || res.data || []).forEach(n => { map[n.warehouseId] = n.warehouseName || n.name; });
    warehouseMap.value = map;
  }).catch(() => {});
}
function warehouseLabel(id) {
  if (id == null) return '—';
  return warehouseMap.value[id] || String(id);
}

/* ---------- 期初成本：物料/仓库/库位 弹框选择（picker 规范） ---------- */
const initMaterialPickerRef = ref(null);
const initWarehousePickerRef = ref(null);
const initLocationPickerRef = ref(null);
const taxMaterialPickerRef = ref(null);
const initMaterialText = computed(() =>
  initForm.materialCode ? (initForm.materialCode + (initForm.materialName ? ' - ' + initForm.materialName : '')) : ''
);
const taxMaterialText = computed(() =>
  taxForm.materialCode ? (taxForm.materialCode + (taxForm.materialName ? ' - ' + taxForm.materialName : '')) : ''
);
function openInitMaterialPicker() { initMaterialPickerRef.value.open(initForm.materialId); }
function onInitMaterialConfirm(m) {
  initForm.materialId = m.materialId;
  initForm.materialCode = m.materialCode;
  initForm.materialName = m.materialName;
}
function clearInitMaterial() {
  initForm.materialId = undefined;
  initForm.materialCode = undefined;
  initForm.materialName = undefined;
}
function openInitWarehousePicker() { initWarehousePickerRef.value.open(initForm.warehouseId); }
function onInitWarehouseConfirm(w) {
  if (initForm.warehouseId && initForm.warehouseId !== w.warehouseId) {
    initForm.locationId = undefined;
    initForm.locationName = undefined;
  }
  initForm.warehouseId = w.warehouseId;
  initForm.warehouseName = w.warehouseName;
}
function clearInitWarehouse() {
  initForm.warehouseId = undefined;
  initForm.warehouseName = undefined;
  initForm.locationId = undefined;
  initForm.locationName = undefined;
}
function openInitLocationPicker() {
  if (!initForm.warehouseId) {
    proxy.$modal.msgWarning('请先选择仓库');
    return;
  }
  initLocationPickerRef.value.open(initForm.warehouseId, initForm.locationId);
}
function onInitLocationConfirm(l) {
  initForm.locationId = l.locationId;
  initForm.locationName = l.locationName;
}
function clearInitLocation() {
  initForm.locationId = undefined;
  initForm.locationName = undefined;
}
function openTaxMaterialPicker() { taxMaterialPickerRef.value.open(taxForm.materialId); }
function onTaxMaterialConfirm(m) {
  taxForm.materialId = m.materialId;
  taxForm.materialCode = m.materialCode;
  taxForm.materialName = m.materialName;
}

/* ---------- 计价 / 结转 / 期初 ---------- */
const showResult = ref(false);
const priceResult = ref(null);
const showValuation = ref(false);
const valuationList = ref([]);
const showInit = ref(false);
const initForm = reactive({});

function loadTotal() {
  getTotalCost().then(res => { totalCost.value = res.data || 0; });
}
function handleQuery() {
  queryParams.pageNum = 1;
  carryQuery.pageNum = 1;
  getList();
  getCarryList();
}
function resetQuery() {
  queryParams.pageNum = 1;
  queryParams.pageSize = 20;
  queryParams.materialCode = null;
  queryParams.materialName = null;
  carryQuery.pageNum = 1;
  carryQuery.sourceType = null;
  dateRange.value = [];
  getList();
  getCarryList();
}
function handlePriceInbound() {
  priceInbound({}).then(res => {
    priceResult.value = res.data;
    showResult.value = true;
    getList();
    loadTotal();
  });
}
function handleCarryOutbound() {
  proxy.$modal.confirm("确认执行本期出库结转吗？").then(() => {
    return carryOutbound({});
  }).then(() => {
    proxy.$modal.msgSuccess("结转完成");
    getList();
    getCarryList();
    loadTotal();
  }).catch(() => {});
}
function loadValuation() {
  getValuation({}).then(res => { valuationList.value = res.data || []; });
}
function handleInit() {
  Object.keys(initForm).forEach(k => delete initForm[k]);
  initForm.warehouseId = 1;
  initForm.locationId = 1;
  initForm.taxRate = 0.13;
  showInit.value = true;
}
function submitInit() {
  if (!initForm.materialCode || !initForm.qty) {
    proxy.$modal.msgWarning("请完善物料与期初数量");
    return;
  }
  initCost(initForm).then(() => {
    proxy.$modal.msgSuccess("期初成本录入成功");
    showInit.value = false;
    getList();
    loadTotal();
  });
}
function handleExport() {
  proxy.download("fin/cost/export", {}, `cost_${new Date().getTime()}.xlsx`);
}

function formatAmount(v) {
  if (v == null) return "-";
  return Number(v).toLocaleString("zh-CN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
function isEffective(row) {
  if (row.status !== undefined && row.status !== null) return row.status === "0" || row.status === 0;
  const now = new Date().toISOString().slice(0, 10);
  return (!row.effectiveDate || row.effectiveDate <= now) && (!row.expireDate || row.expireDate >= now);
}
function percent(v) { return v != null ? v + "%" : "-"; }
/** 税率小数（0.13）→ 百分比显示（13%） */
function formatTaxRate(v) {
  if (v == null || v === '') return '-';
  return (Number(v) * 100).toFixed(0) + '%';
}

/* ---------- 初始化 ---------- */
getList();
getTaxList();
loadTotal();
getCarryList();
loadValuation();
loadWarehouseMap();
</script>

<style scoped>
/* ===== 7.1 Design Tokens ===== */
.fin-cost-page {
  --page-bg: #f5f7fa;
  --card-bg: #ffffff;
  --border-color: #e4e7ed;
  --border-light: #ebeef5;
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --primary-color: #409eff;
  --primary-light: #ecf5ff;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --shadow-card: 0 1px 4px rgba(0, 21, 41, 0.08);

  padding: 16px;
  background: var(--page-bg);
  min-height: calc(100vh - 84px);
}

/* ===== stat-card（对齐应收管理页面） ===== */
.fin-cost-page .stat-card {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: var(--shadow-card);
  padding: 16px 18px;
  text-align: center;
  transition: all .15s cubic-bezier(.16,.84,.44,1);
}
.fin-cost-page .stat-card:hover {
  border-color: #c7d2fe;
  box-shadow: 0 4px 12px -2px rgba(99,102,241,.12);
}
.fin-cost-page .stat-card .stat-label { font-size: 13px; color: var(--text-secondary); margin-bottom: 8px; }
.fin-cost-page .stat-card .stat-value { font-size: 22px; font-weight: 700; color: var(--text-primary); font-variant-numeric: tabular-nums; }
.fin-cost-page .stat-card .stat-hint { font-size: 12px; color: var(--text-secondary); margin-top: 6px; }

/* 菜单重构：独立菜单模式下隐藏页签栏（页签已拆为独立菜单） */
.fin-cost-page .cost-tabs-solo > :deep(.el-tabs__header) { display: none; }

/* ===== 卡片容器 ===== */
.fin-cost-page .surface {
  background: var(--card-bg);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-card);
  padding: 16px;
  margin-bottom: 16px;
}

/* ===== 筛选条件卡片（对齐应收管理页面 / fin-list-page 共享规范） ===== */
.fin-cost-page .filter-card {
  padding: 14px 20px 16px;
}
.fin-cost-page .filter-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.fin-cost-page .filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-700, #334155);
}
.fin-cost-page .filter-title .glyph {
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background: var(--primary-color);
}
.fin-cost-page .filter-bar {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px 16px;
}
.fin-cost-page .filter-bar .field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.fin-cost-page .filter-bar .field label {
  font-size: 14px;
  font-weight: 500;
  color: var(--ink-700, #334155);
  display: flex;
  align-items: center;
  gap: 6px;
}
.fin-cost-page .filter-bar .field .control {
  display: flex;
  align-items: center;
  height: 36px;
  padding: 0 12px;
  background: #fff;
  border: 1px solid var(--ink-200, #e2e8f0);
  border-radius: 6px;
  transition: border-color .15s ease-out, box-shadow .15s ease-out;
}
.fin-cost-page .filter-bar .field .control.is-select {
  padding: 0 8px;
}
.fin-cost-page .filter-bar .field .control:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, .15);
}
.fin-cost-page .filter-bar .field .control :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent !important;
  padding: 0;
  height: 34px;
}
.fin-cost-page .filter-bar .field .control :deep(.el-input__inner) {
  border: 0;
  background: transparent;
  font-size: 14px;
  color: var(--ink-900, #0f172a);
  height: 34px;
  line-height: 34px;
}
.fin-cost-page .filter-bar .field .control :deep(.el-input__inner::placeholder) {
  color: var(--ink-400, #94a3b8);
}
.fin-cost-page .filter-bar .field .control :deep(.el-input__prefix) {
  color: var(--ink-400, #94a3b8);
  margin-right: 4px;
}
.fin-cost-page .filter-bar .field .control :deep(.el-select) {
  width: 100%;
}
.fin-cost-page .filter-bar .field .control :deep(.el-select .el-select__wrapper) {
  box-shadow: none !important;
  background: transparent !important;
  padding: 0;
  min-height: 34px;
  height: 34px;
}
.fin-cost-page .filter-bar .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) {
  font-size: 14px;
  color: var(--ink-900, #0f172a);
}
.fin-cost-page .filter-bar .field .control :deep(.el-date-editor) {
  width: 100%;
}
.fin-cost-page .filter-bar .field .control :deep(.el-date-editor .el-range-input) {
  background: transparent;
  border: 0;
  font-size: 14px;
  color: var(--ink-900, #0f172a);
}
.fin-cost-page .filter-bar .field .control :deep(.el-date-editor .el-range-separator) {
  color: var(--ink-400, #94a3b8);
}
.fin-cost-page .filter-bar .field .control :deep(.el-date-editor .el-range__icon) {
  color: var(--ink-400, #94a3b8);
}
.fin-cost-page .filter-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px dashed var(--ink-200, #e2e8f0);
  margin-top: 14px;
  padding-top: 14px;
}
.fin-cost-page .filter-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--ink-500, #64748b);
}
.fin-cost-page .filter-buttons {
  display: flex;
  gap: 8px;
}

/* ===== 工具栏 ===== */
.fin-cost-page .toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.fin-cost-page .toolbar-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0;
}
.fin-cost-page .toolbar-right {
  display: flex;
  align-items: center;
}

/* ===== 表格容器 ===== */
.fin-cost-page .clear-icon { color: var(--text-secondary); cursor: pointer; }
.fin-cost-page .clear-icon:hover { color: var(--danger-color); }
.fin-cost-page .table-wrap {
  width: 100%;
}
.fin-cost-page :deep(.el-table) {
  --el-table-border-color: var(--border-light);
  font-size: 13px;
}
.fin-cost-page :deep(.el-table th.el-table__cell) {
  background: #f8f9fb;
  color: var(--text-primary);
  font-weight: 600;
}
.fin-cost-page :deep(.el-table .col-mono .cell) {
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 12px;
}
.fin-cost-page .pagination-container {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}

/* ===== 徽章 ===== */
.fin-cost-page .badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 18px;
  white-space: nowrap;
}
.fin-cost-page .badge .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}
.fin-cost-page .badge.blue { background: #ecf5ff; color: #409eff; }
.fin-cost-page .badge.green { background: #f0f9eb; color: #67c23a; }
.fin-cost-page .badge.amber { background: #fdf6ec; color: #e6a23c; }
.fin-cost-page .badge.red { background: #fef0f0; color: #f56c6c; }
.fin-cost-page .badge.violet { background: #f4f0ff; color: #7c5cff; }
.fin-cost-page .badge.gray { background: #f4f4f5; color: #909399; }

/* ===== 操作列（5.5.3） ===== */
.fin-cost-page :deep(.col-action) {
  padding: 6px 4px !important;
}
.fin-cost-page :deep(.col-action .cell) {
  display: flex;
  justify-content: center;
  padding: 0;
}
.fin-cost-page .action-btn-row {
  display: inline-flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0;
}
.fin-cost-page :deep(.col-action .el-button) {
  padding: 2px 4px;
  margin: 0 2px;
  white-space: nowrap;
  justify-content: center;
}
.fin-cost-page :deep(.col-action .el-button + .el-button) {
  margin-left: 2px;
}
</style>

<style scoped>
/* ===== 汇总条 ===== */
.fin-cost-page .summary-bar {
  display: flex;
  gap: 48px;
}
.fin-cost-page .summary-item {
  min-width: 180px;
}
.fin-cost-page .summary-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.fin-cost-page .summary-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}
.fin-cost-page .summary-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

/* ===== 金额与状态文字 ===== */
.fin-cost-page .amount-cell {
  font-variant-numeric: tabular-nums;
  color: var(--text-primary);
}
.fin-cost-page .amount-cell.strong {
  font-weight: 600;
  color: var(--primary-color);
}
.fin-cost-page .text-muted { color: var(--text-secondary); }
.fin-cost-page .text-success { color: var(--success-color); }
.fin-cost-page .text-warning { color: var(--warning-color); }
.fin-cost-page .text-danger { color: var(--danger-color); }
.fin-cost-page .form-tip {
  font-size: 12px;
  color: var(--text-secondary);
  padding-left: 120px;
  margin-top: -4px;
}
.fin-cost-page .hint-text { font-size: 12px; font-weight: normal; }

/* rd- 弹窗体系样式统一走全局规范（detail-page.scss），此处禁止本地重定义：
   :deep() 选择器会覆盖全局样式，导致卡片/头部与参考页面（partner 等）视觉不一致；本页仅保留下方页面独有样式 */

/* 分组卡片内表单留白压缩（避免单字段卡片底部大空白） */
.fin-cost-page .rd-card-body .el-form-item { margin-bottom: 14px; }
.fin-cost-page .rd-card-body .el-form-item:last-child { margin-bottom: 0; }

/* ===== 响应式 ===== */
@media (max-width: 1100px) {
  .fin-cost-page .filter-bar {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 720px) {
  .fin-cost-page .filter-bar {
    grid-template-columns: 1fr;
  }
  .fin-cost-page .toolbar {
    flex-wrap: wrap;
    gap: 10px;
  }
  .fin-cost-page .rd-grid {
    grid-template-columns: 1fr;
  }
  .fin-cost-page .summary-bar {
    gap: 24px;
    flex-wrap: wrap;
  }
}
</style>

<template>
  <div class="app-container wms-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>投标项目名称</label>
          <div class="control">
            <el-input v-model="queryParams.bidName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>招标编号</label>
          <div class="control">
            <el-input v-model="queryParams.tenderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>客户名称</label>
          <div class="control">
            <el-input v-model="queryParams.customerName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.bidStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mk_bid_status" :key="d.value" :label="d.label" :value="d.value" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:bid:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" @click="handleUpdate" v-hasPermi="['marketing:bid:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" @click="handleDelete" v-hasPermi="['marketing:bid:remove']">删除</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:bid:export']">导出</el-button>
          <el-button type="info" plain icon="Trophy" @click="handleMarkResult" v-hasPermi="['marketing:bid:win']">标记结果</el-button>
          <el-button type="primary" plain icon="Document" @click="handleCreateContract" v-hasPermi="['marketing:bid:contract']">生成合同</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="bid_columns_show" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="bidList" border class="app-table" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 60)" resizable align="center" />
          <el-table-column label="投标编号" prop="bidNo" key="bidNo" :width="colWidth('bidNo', 150)" resizable v-if="columns.bidNo.visible">
            <template #default="scope"><span class="mono">{{ scope.row.bidNo }}</span></template>
          </el-table-column>
          <el-table-column label="投标项目名称" prop="bidName" key="bidName" :min-width="colWidth('bidName', 180)" resizable show-overflow-tooltip v-if="columns.bidName.visible" />
          <el-table-column label="招标编号" prop="tenderNo" key="tenderNo" :width="colWidth('tenderNo', 140)" resizable show-overflow-tooltip v-if="columns.tenderNo.visible" />
          <el-table-column label="招标机构" prop="tenderOrg" key="tenderOrg" :width="colWidth('tenderOrg', 150)" resizable show-overflow-tooltip v-if="columns.tenderOrg.visible" />
          <el-table-column label="客户名称" prop="customerName" key="customerName" :min-width="colWidth('customerName', 150)" resizable show-overflow-tooltip v-if="columns.customerName.visible" />
          <el-table-column label="投标日期" prop="bidDate" key="bidDate" :width="colWidth('bidDate', 110)" resizable align="center" v-if="columns.bidDate.visible">
            <template #default="scope">{{ parseTime(scope.row.bidDate, '{y}-{m}-{d}') || '—' }}</template>
          </el-table-column>
          <el-table-column label="开标日期" prop="openDate" key="openDate" :width="colWidth('openDate', 110)" resizable align="center" v-if="columns.openDate.visible">
            <template #default="scope">{{ parseTime(scope.row.openDate, '{y}-{m}-{d}') || '—' }}</template>
          </el-table-column>
          <el-table-column label="投标金额" prop="bidAmount" key="bidAmount" :width="colWidth('bidAmount', 110)" resizable align="right" v-if="columns.bidAmount.visible">
            <template #default="scope"><span class="mono" v-if="scope.row.bidAmount != null">{{ fmtMoney(scope.row.bidAmount) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="保证金" prop="deposit" key="deposit" :width="colWidth('deposit', 100)" resizable align="right" v-if="columns.deposit.visible">
            <template #default="scope"><span class="mono" v-if="scope.row.deposit != null">{{ fmtMoney(scope.row.deposit) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="bidStatus" key="bidStatus" :width="colWidth('bidStatus', 100)" resizable align="center" v-if="columns.bidStatus.visible">
            <template #default="scope">
              <span v-if="scope.row.bidStatus" class="badge" :class="statusBadgeClass(scope.row.bidStatus)"><span class="dot"></span>{{ dictLabel(mk_bid_status, scope.row.bidStatus) }}</span>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="中标金额" prop="winAmount" key="winAmount" :width="colWidth('winAmount', 110)" resizable align="right" v-if="columns.winAmount.visible">
            <template #default="scope"><span class="mono" v-if="scope.row.winAmount != null">{{ fmtMoney(scope.row.winAmount) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="关联合同" prop="contractNo" key="contractNo" :width="colWidth('contractNo', 140)" resizable show-overflow-tooltip v-if="columns.contractNo.visible">
            <template #default="scope"><span class="mono">{{ scope.row.contractNo || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="结果日期" prop="resultDate" key="resultDate" :width="colWidth('resultDate', 110)" resizable align="center" v-if="columns.resultDate.visible">
            <template #default="scope">{{ parseTime(scope.row.resultDate, '{y}-{m}-{d}') || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" :width="colWidth('操作', 90)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="table-pager">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- ===== 新增/修改弹框 ===== -->
    <el-dialog v-model="open" width="1080px" append-to-body draggable class="rd-dialog" @closed="cancel">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="bidRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="投标编号" prop="bidNo">
                    <el-input v-model="form.bidNo" placeholder="保存后自动生成" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="项目名称" prop="bidName">
                    <el-input v-model="form.bidName" placeholder="请输入投标项目名称" maxlength="100" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="招标编号" prop="tenderNo">
                    <el-input v-model="form.tenderNo" placeholder="请输入招标编号" maxlength="50" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="招标机构" prop="tenderOrg">
                    <el-input v-model="form.tenderOrg" placeholder="请输入招标机构" maxlength="100" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="客户" prop="customerName">
                    <el-input :model-value="form.customerName" readonly placeholder="点击选择客户">
                      <template v-if="form.customerId" #append><el-button icon="CircleClose" @click.stop="clearCustomer" /></template>
                      <template v-else #append><el-button icon="Search" @click="openCustomerPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="投标日期" prop="bidDate">
                    <el-date-picker v-model="form.bidDate" type="date" value-format="YYYY-MM-DD" placeholder="选择投标日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="开标日期" prop="openDate">
                    <el-date-picker v-model="form.openDate" type="date" value-format="YYYY-MM-DD" placeholder="选择开标日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="投标金额" prop="bidAmount">
                    <el-input-number v-model="form.bidAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" placeholder="请输入投标金额" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="保证金" prop="deposit">
                    <el-input-number v-model="form.deposit" :precision="2" :min="0" controls-position="right" style="width: 100%" placeholder="请输入保证金" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="附件" prop="attachment">
                    <el-input v-model="form.attachment" placeholder="附件地址（选填）" maxlength="255" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>投标明细</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <div class="item-toolbar">
                <el-button type="primary" plain icon="Plus" size="small" @click="addItemRow">添加行</el-button>
                <span class="form-tip">可点击搜索按钮选择物料自动带出，也可直接手工录入；小计 = 数量 × 单价</span>
              </div>
              <el-table :data="form.itemList" border size="small" max-height="320" show-summary :summary-method="getSummaries">
                <el-table-column type="index" label="行号" width="50" align="center" />
                <el-table-column label="物料编码" width="180">
                  <template #default="scope">
                    <div class="cell-flex">
                      <el-input v-model="scope.row.materialCode" size="small" placeholder="物料编码" maxlength="50" />
                      <el-button size="small" icon="Search" @click="openItemMaterial(scope.$index)" />
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="品名" min-width="140">
                  <template #default="scope">
                    <el-input v-model="scope.row.productName" size="small" placeholder="品名" maxlength="100" />
                  </template>
                </el-table-column>
                <el-table-column label="规格型号" width="130">
                  <template #default="scope">
                    <el-input v-model="scope.row.productSpec" size="small" placeholder="规格" maxlength="100" />
                  </template>
                </el-table-column>
                <el-table-column label="单位" width="80">
                  <template #default="scope">
                    <el-input v-model="scope.row.unit" size="small" placeholder="单位" maxlength="20" />
                  </template>
                </el-table-column>
                <el-table-column label="数量" width="130">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.quantity" size="small" :precision="0" :min="0" controls-position="right" style="width: 100%" />
                  </template>
                </el-table-column>
                <el-table-column label="单价" width="140">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.unitPrice" size="small" :precision="2" :min="0" controls-position="right" style="width: 100%" />
                  </template>
                </el-table-column>
                <el-table-column label="小计" width="110" align="right">
                  <template #default="scope"><span class="mono">{{ fmtMoney(rowSubtotal(scope.row)) }}</span></template>
                </el-table-column>
                <el-table-column label="备注" min-width="120">
                  <template #default="scope">
                    <el-input v-model="scope.row.remark" size="small" placeholder="备注" maxlength="200" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="70" align="center" fixed="right">
                  <template #default="scope">
                    <el-button link type="danger" icon="Delete" @click="removeItemRow(scope.$index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
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

    <!-- ===== 客户选择器 ===== -->
    <el-dialog v-model="customerOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">选择客户</span>
        </div>
      </template>
      <div class="customer-picker">
        <div class="customer-picker-search">
          <el-input v-model="customerQuery.customerName" placeholder="客户名称" clearable size="small" style="width: 200px" @keyup.enter="handleCustomerQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleCustomerQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetCustomerQuery">重置</el-button>
        </div>
        <div class="customer-picker-table">
          <el-table v-loading="customerLoading" :data="customerList" highlight-current-row @row-click="onCustomerRowClick" @row-dblclick="onCustomerRowDblClick" height="360" size="small">
            <el-table-column width="45" align="center">
              <template #default="{ row }">
                <el-radio :model-value="selectedCustomerId" :value="row.customerId" @click.stop="onCustomerRowClick(row)"><span /></el-radio>
              </template>
            </el-table-column>
            <el-table-column label="客户编号" prop="customerNo" width="140" show-overflow-tooltip />
            <el-table-column label="客户名称" prop="customerName" min-width="180" show-overflow-tooltip />
            <el-table-column label="行业" prop="industry" width="120" show-overflow-tooltip />
            <el-table-column label="负责人" prop="userName" width="90" align="center" />
          </el-table>
        </div>
        <div class="customer-picker-pager">
          <el-pagination v-model:current-page="customerQuery.pageNum" v-model:page-size="customerQuery.pageSize" :total="customerTotal" layout="total, prev, pager, next" small @current-change="getCustomerList" />
        </div>
      </div>
      <template #footer>
        <el-button @click="customerOpen = false">取 消</el-button>
        <el-button type="primary" @click="handleCustomerConfirm" :disabled="!selectedCustomerId">确 定</el-button>
      </template>
    </el-dialog>

    <!-- ===== 开标结果弹框 ===== -->
    <el-dialog v-model="resultOpen" width="520px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8 21h8"/><path d="M12 17v4"/><path d="M7 4h10v6a5 5 0 0 1-10 0z"/><path d="M17 5h3a1 1 0 0 1 1 1c0 2.5-2 4-4 4"/><path d="M7 5H4a1 1 0 0 0-1 1c0 2.5 2 4 4 4"/></svg></div>
          <span class="rd-detail-header-title">开标结果</span>
        </div>
      </template>
      <el-form ref="resultRef" :model="resultForm" :rules="resultRules" label-width="100px">
        <el-form-item label="投标项目">
          <span class="mono">{{ resultForm.bidName || '—' }}</span>
        </el-form-item>
        <el-form-item label="开标结果" prop="bidStatus">
          <el-radio-group v-model="resultForm.bidStatus">
            <el-radio value="2">中标</el-radio>
            <el-radio value="3">未中标</el-radio>
            <el-radio value="4">废标</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="resultForm.bidStatus === '2'" label="中标金额" prop="winAmount">
          <el-input-number v-model="resultForm.winAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" placeholder="请输入中标金额" />
        </el-form-item>
        <el-form-item label="结果日期" prop="resultDate">
          <el-date-picker v-model="resultForm.resultDate" type="date" value-format="YYYY-MM-DD" placeholder="选择结果日期" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitResult">确 定</el-button>
          <el-button @click="resultOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 详情弹框 ===== -->
    <el-dialog v-model="detailOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">招投标档案详情</span>
        </div>
      </template>
      <div v-loading="detailLoading">
        <div class="rd-detail-grid">
          <div class="rd-item"><span class="rd-label">投标编号</span><div class="rd-value mono">{{ detail.bidNo || '—' }}</div></div>
          <div class="rd-item"><span class="rd-label">投标项目名称</span><div class="rd-value">{{ detail.bidName || '—' }}</div></div>
          <div class="rd-item"><span class="rd-label">招标编号</span><div class="rd-value mono">{{ detail.tenderNo || '—' }}</div></div>
          <div class="rd-item"><span class="rd-label">招标机构</span><div class="rd-value">{{ detail.tenderOrg || '—' }}</div></div>
          <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ detail.customerName || '—' }}</div></div>
          <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="detail.bidStatus" class="badge" :class="statusBadgeClass(detail.bidStatus)"><span class="dot"></span>{{ dictLabel(mk_bid_status, detail.bidStatus) }}</span><span v-else class="text-muted">—</span></div></div>
          <div class="rd-item"><span class="rd-label">投标日期</span><div class="rd-value">{{ parseTime(detail.bidDate, '{y}-{m}-{d}') || '—' }}</div></div>
          <div class="rd-item"><span class="rd-label">开标日期</span><div class="rd-value">{{ parseTime(detail.openDate, '{y}-{m}-{d}') || '—' }}</div></div>
          <div class="rd-item"><span class="rd-label">投标金额</span><div class="rd-value mono">{{ detail.bidAmount != null ? fmtMoney(detail.bidAmount) : '—' }}</div></div>
          <div class="rd-item"><span class="rd-label">保证金</span><div class="rd-value mono">{{ detail.deposit != null ? fmtMoney(detail.deposit) : '—' }}</div></div>
          <div class="rd-item"><span class="rd-label">中标金额</span><div class="rd-value mono">{{ detail.winAmount != null ? fmtMoney(detail.winAmount) : '—' }}</div></div>
          <div class="rd-item"><span class="rd-label">结果日期</span><div class="rd-value">{{ parseTime(detail.resultDate, '{y}-{m}-{d}') || '—' }}</div></div>
          <div class="rd-item"><span class="rd-label">关联合同</span><div class="rd-value mono">{{ detail.contractNo || '—' }}</div></div>
          <div class="rd-item"><span class="rd-label">附件</span><div class="rd-value">{{ detail.attachment || '—' }}</div></div>
          <div class="rd-item"><span class="rd-label">备注</span><div class="rd-value">{{ detail.remark || '—' }}</div></div>
        </div>
        <div class="detail-items-title">投标明细</div>
        <el-table :data="detail.itemList || []" border size="small" max-height="300" show-summary :summary-method="getDetailSummaries">
          <el-table-column label="行号" type="index" width="50" align="center" />
          <el-table-column label="物料编码" prop="materialCode" width="140" show-overflow-tooltip><template #default="scope"><span class="mono">{{ scope.row.materialCode || '—' }}</span></template></el-table-column>
          <el-table-column label="品名" prop="productName" min-width="130" show-overflow-tooltip />
          <el-table-column label="规格型号" prop="productSpec" width="120" show-overflow-tooltip />
          <el-table-column label="单位" prop="unit" width="70" align="center" />
          <el-table-column label="数量" prop="quantity" width="90" align="right" />
          <el-table-column label="单价" prop="unitPrice" width="100" align="right"><template #default="scope"><span class="mono">{{ scope.row.unitPrice != null ? fmtMoney(scope.row.unitPrice) : '—' }}</span></template></el-table-column>
          <el-table-column label="小计" prop="subtotal" width="110" align="right"><template #default="scope"><span class="mono">{{ scope.row.subtotal != null ? fmtMoney(scope.row.subtotal) : '—' }}</span></template></el-table-column>
          <el-table-column label="备注" prop="remark" min-width="110" show-overflow-tooltip />
        </el-table>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 物料选择器（明细行选择物料） -->
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />
  </div>
</template>

<script setup name="MkBid">
import { listBid, getBid, addBid, updateBid, delBid, markBidResult, createBidContract } from "@/api/mk/bid"
import { listCustomer } from "@/api/mk/customer"
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
const { collapsedCards, toggleCard } = useDetailCard(['c1', 'c2'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('bid_columns')
const { mk_bid_status } = proxy.useDict("mk_bid_status")

// 列显隐配置
const defaultColumns = {
  bidNo: { label: '投标编号', visible: true },
  bidName: { label: '投标项目名称', visible: true },
  tenderNo: { label: '招标编号', visible: true },
  tenderOrg: { label: '招标机构', visible: true },
  customerName: { label: '客户名称', visible: true },
  bidDate: { label: '投标日期', visible: true },
  openDate: { label: '开标日期', visible: true },
  bidAmount: { label: '投标金额', visible: true },
  deposit: { label: '保证金', visible: true },
  bidStatus: { label: '状态', visible: true },
  winAmount: { label: '中标金额', visible: true },
  contractNo: { label: '关联合同', visible: true },
  resultDate: { label: '结果日期', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('bid_columns_show')
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

const bidList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const bidNos = ref([])
const singleRow = ref(null)
const total = ref(0)
const title = ref("")
const detailOpen = ref(false)
const detailLoading = ref(false)
const detail = ref({})

// 客户选择器
const customerOpen = ref(false)
const customerLoading = ref(false)
const customerList = ref([])
const customerTotal = ref(0)
const selectedCustomerId = ref(null)
const selectedCustomerRow = ref(null)
const customerQuery = reactive({ pageNum: 1, pageSize: 10, customerName: undefined })

// 物料选择器（明细行）
const materialPickerRef = ref(null)
const pickingRowIndex = ref(-1)

// 开标结果
const resultOpen = ref(false)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, bidName: undefined, tenderNo: undefined, customerName: undefined, bidStatus: undefined },
  rules: {
    bidName: [{ required: true, message: "投标项目名称不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const resultData = reactive({
  resultForm: { bidId: null, bidName: undefined, bidStatus: '2', winAmount: undefined, resultDate: undefined },
  resultRules: {
    bidStatus: [{ required: true, message: "请选择开标结果", trigger: "change" }],
    resultDate: [{ required: true, message: "结果日期不能为空", trigger: "blur" }]
  }
})
const { resultForm, resultRules } = toRefs(resultData)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.bidName) count++
  if (queryParams.value.tenderNo) count++
  if (queryParams.value.customerName) count++
  if (queryParams.value.bidStatus) count++
  return count
})

/** 明细行合计金额 */
const totalAmount = computed(() => {
  return (form.value.itemList || []).reduce((sum, row) => sum + rowSubtotal(row), 0)
})

function getList() {
  loading.value = true
  listBid(queryParams.value).then(response => {
    bidList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.bidName = undefined
  queryParams.value.tenderNo = undefined
  queryParams.value.customerName = undefined
  queryParams.value.bidStatus = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.bidId)
  bidNos.value = selection.map(item => item.bidNo)
  singleRow.value = selection.length === 1 ? selection[0] : null
}

/** 表单重置 */
function reset() {
  form.value = {
    bidId: null, bidNo: undefined, bidName: undefined, tenderNo: undefined, tenderOrg: undefined,
    customerId: null, customerName: undefined, bidDate: undefined, openDate: undefined,
    bidAmount: undefined, deposit: undefined, bidStatus: '0', winAmount: undefined,
    contractId: null, contractNo: undefined, resultDate: undefined, attachment: undefined,
    remark: undefined, itemList: []
  }
  proxy.resetForm("bidRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增招投标档案"
}

function handleUpdate() {
  if (ids.value.length !== 1) {
    proxy.$modal.msgWarning("请选择一条要修改的记录")
    return
  }
  getBid(ids.value[0]).then(response => {
    const record = response.data
    record.itemList = record.itemList || []
    form.value = record
    open.value = true
    title.value = "修改招投标档案"
  })
}

function cancel() {
  open.value = false
  reset()
}

function submitForm() {
  proxy.$refs["bidRef"].validate(valid => {
    if (valid) {
      const items = (form.value.itemList || [])
        .filter(r => r.materialCode || r.productName || r.quantity != null || r.unitPrice != null)
        .map((r, i) => ({ ...r, lineNo: i + 1, subtotal: rowSubtotal(r) }))
      const payload = { ...form.value, itemList: items }
      if (form.value.bidId != null) {
        updateBid(payload).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        delete payload.bidNo
        addBid(payload).then(() => {
          proxy.$modal.msgSuccess("新增成功，投标编号已自动生成")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除 */
function handleDelete() {
  if (ids.value.length === 0) {
    proxy.$modal.msgWarning("请选择要删除的记录")
    return
  }
  proxy.$modal.confirm('是否确认删除投标编号为"' + bidNos.value.join('、') + '"的数据项？').then(() => delBid(ids.value.join(','))).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 详情 */
function handleDetail(row) {
  detailOpen.value = true
  detailLoading.value = true
  detail.value = {}
  getBid(row.bidId).then(response => {
    detail.value = response.data || {}
    detailLoading.value = false
  }).catch(() => { detailLoading.value = false })
}

/** 导出 */
function handleExport() {
  proxy.download("mk/bid/export", { ...queryParams.value }, `bid_${new Date().getTime()}.xlsx`)
}

/** 标记开标结果 */
function handleMarkResult() {
  if (ids.value.length !== 1) {
    proxy.$modal.msgWarning("请选择一条投标记录")
    return
  }
  proxy.resetForm("resultRef")
  resultData.resultForm = {
    bidId: singleRow.value.bidId,
    bidName: singleRow.value.bidName,
    bidStatus: '2',
    winAmount: singleRow.value.winAmount != null ? singleRow.value.winAmount : undefined,
    resultDate: proxy.parseTime(new Date(), '{y}-{m}-{d}')
  }
  resultOpen.value = true
}

function submitResult() {
  proxy.$refs["resultRef"].validate(valid => {
    if (valid) {
      if (resultForm.value.bidStatus === '2' && resultForm.value.winAmount == null) {
        proxy.$modal.msgWarning("中标时必须填写中标金额")
        return
      }
      markBidResult({ ...resultForm.value }).then(() => {
        proxy.$modal.msgSuccess("开标结果登记成功")
        resultOpen.value = false
        getList()
      })
    }
  })
}

/** 由中标投标生成合同草稿 */
function handleCreateContract() {
  if (ids.value.length !== 1) {
    proxy.$modal.msgWarning("请选择一条投标记录")
    return
  }
  if (singleRow.value.bidStatus !== '2') {
    proxy.$modal.msgWarning("仅中标状态的投标可生成合同")
    return
  }
  proxy.$modal.confirm('将根据该投标生成合同草稿，是否继续？').then(() => createBidContract(singleRow.value.bidId)).then(res => {
    const tip = (typeof res.data === 'string' && res.data) || res.msg || "合同草稿生成成功"
    proxy.$modal.msgSuccess(tip)
    getList()
  }).catch(() => {})
}

/** ===== 客户选择器 ===== */
function openCustomerPicker() {
  customerOpen.value = true
  selectedCustomerId.value = form.value.customerId || null
  handleCustomerQuery()
}
function clearCustomer() {
  form.value.customerId = null
  form.value.customerName = undefined
}
function getCustomerList() {
  customerLoading.value = true
  listCustomer(customerQuery).then(res => {
    customerList.value = res.rows || []
    customerTotal.value = res.total || 0
    customerLoading.value = false
    if (selectedCustomerId.value) {
      nextTick(() => {
        const row = customerList.value.find(r => r.customerId === selectedCustomerId.value)
        if (row) selectedCustomerRow.value = row
      })
    }
  }).catch(() => { customerLoading.value = false })
}
function handleCustomerQuery() {
  customerQuery.pageNum = 1
  getCustomerList()
}
function resetCustomerQuery() {
  customerQuery.customerName = undefined
  handleCustomerQuery()
}
function onCustomerRowClick(row) {
  selectedCustomerId.value = row.customerId
  selectedCustomerRow.value = row
}
function onCustomerRowDblClick(row) {
  onCustomerRowClick(row)
  handleCustomerConfirm()
}
function handleCustomerConfirm() {
  if (!selectedCustomerId.value) return
  form.value.customerId = selectedCustomerRow.value.customerId
  form.value.customerName = selectedCustomerRow.value.customerName
  customerOpen.value = false
}

/** ===== 投标明细行 ===== */
function addItemRow() {
  form.value.itemList.push({ itemId: null, lineNo: null, materialId: null, materialCode: undefined, productName: undefined, productSpec: undefined, unit: undefined, quantity: undefined, unitPrice: undefined, subtotal: undefined, remark: undefined })
}
function removeItemRow(index) {
  form.value.itemList.splice(index, 1)
}
function openItemMaterial(index) {
  pickingRowIndex.value = index
  materialPickerRef.value.open(form.value.itemList[index].materialId || undefined)
}
function onMaterialPickerConfirm(material) {
  const idx = pickingRowIndex.value
  if (idx < 0 || !form.value.itemList[idx]) return
  const row = form.value.itemList[idx]
  row.materialId = material.materialId
  row.materialCode = material.materialCode
  row.productName = material.materialName
  row.productSpec = material.specModel
  row.unit = material.unit
  pickingRowIndex.value = -1
}
function rowSubtotal(row) {
  if (!row || row.quantity == null || row.unitPrice == null) return 0
  return Math.round(Number(row.quantity) * Number(row.unitPrice) * 100) / 100
}

/** 明细合计行（编辑弹框） */
function getSummaries(param) {
  const { columns } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 1) sums[index] = '合计'
    else if (column.label === '小计') sums[index] = fmtMoney(totalAmount.value)
    else sums[index] = ''
  })
  return sums
}

/** 明细合计行（详情弹框，基于后端 subtotal） */
function getDetailSummaries(param) {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 1) sums[index] = '合计'
    else if (column.property === 'subtotal') {
      const sum = (data || []).reduce((s, r) => s + Number(r.subtotal || 0), 0)
      sums[index] = fmtMoney(Math.round(sum * 100) / 100)
    } else sums[index] = ''
  })
  return sums
}

/** 工具函数 */
function dictLabel(options, v) {
  if (v == null || v === "") return "-"
  const item = (options || []).find(d => d.value == v)
  return item ? item.label : v
}
/** 状态徽章色：0编制info 1已投标primary 2中标success 3未中标warning 4废标danger */
function statusBadgeClass(v) {
  return { '0': 'gray', '1': 'blue', '2': 'green', '3': 'amber', '4': 'red' }[v] || 'gray'
}
function fmtMoney(v) {
  if (v == null || v === '') return '—'
  return Number(v).toFixed(2)
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.form-tip { font-size: 12px; color: #909399; line-height: 1.6; }
.mono { font-family: "JetBrains Mono", Consolas, monospace; font-size: 12px; }
.item-toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.cell-flex { display: flex; align-items: center; gap: 4px; }
.detail-items-title { font-size: 14px; font-weight: 600; color: #303133; margin: 14px 0 8px; }
.customer-picker { display: flex; flex-direction: column; }
.customer-picker-search { display: flex; align-items: center; margin-bottom: 12px; }
.customer-picker-table { border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; }
.customer-picker-table :deep(.el-table__row) { cursor: pointer; }
.customer-picker-table :deep(.el-table__row:hover > td) { background: #f0f7ff; }
.customer-picker-table :deep(.el-table__row.is-current > td) { background: #e6f0fd; }
.customer-picker-pager { margin-top: 8px; display: flex; justify-content: flex-end; }
</style>

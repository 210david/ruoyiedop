<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="订单编号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="订单状态" prop="orderStatus">
        <el-select v-model="queryParams.orderStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_order_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:order:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:order:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:order:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订单编号" prop="orderNo" :width="colWidth('orderNo', 150)" resizable />
      <el-table-column label="合同编号" prop="contractNo" :width="colWidth('contractNo', 150)" resizable />
      <el-table-column label="客户名称" prop="customerName" show-overflow-tooltip />
      <el-table-column label="订单金额" prop="orderAmount" :width="colWidth('orderAmount', 120)" resizable align="right" />
      <el-table-column label="订单状态" prop="orderStatus" :width="colWidth('orderStatus', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_order_status" :value="scope.row.orderStatus" /></template>
      </el-table-column>
      <el-table-column label="物流公司" prop="logisticsCompany" :width="colWidth('logisticsCompany', 120)" resizable />
      <el-table-column label="物流单号" prop="trackingNo" :width="colWidth('trackingNo', 150)" resizable />
      <el-table-column label="负责人" prop="userName" :width="colWidth('userName', 100)" resizable />
      <el-table-column label="操作" width="360" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <!-- 待确认：确认 -->
          <el-button v-if="scope.row.orderStatus === '0'" link type="success" icon="Check" @click="handleConfirm(scope.row)" v-hasPermi="['marketing:order:confirm']">确认</el-button>
          <!-- 已确认：发货 -->
          <el-button v-if="scope.row.orderStatus === '1'" link type="warning" icon="Promotion" @click="handleDeliver(scope.row)" v-hasPermi="['marketing:order:deliver']">发货</el-button>
          <!-- 已发货：签收 -->
          <el-button v-if="scope.row.orderStatus === '2'" link type="success" icon="Box" @click="handleReceive(scope.row)" v-hasPermi="['marketing:order:receive']">签收</el-button>
          <!-- 已签收：完成 -->
          <el-button v-if="scope.row.orderStatus === '3'" link type="success" icon="CircleCheck" @click="handleComplete(scope.row)" v-hasPermi="['marketing:order:edit']">完成</el-button>
          <!-- 待确认/已确认：修改 -->
          <el-button v-if="scope.row.orderStatus === '0' || scope.row.orderStatus === '1'" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:order:edit']">修改</el-button>
          <!-- 待确认/已确认：取消 -->
          <el-button v-if="scope.row.orderStatus === '0' || scope.row.orderStatus === '1'" link type="danger" icon="CircleClose" @click="handleCancel(scope.row)" v-hasPermi="['marketing:order:cancel']">取消</el-button>
          <!-- 已签收/已完成：退货 -->
          <el-button v-if="scope.row.orderStatus === '3' || scope.row.orderStatus === '4'" link type="danger" icon="RefreshLeft" @click="handleReturn(scope.row)" v-hasPermi="['marketing:return:add']">退货</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="1000px" append-to-body draggable class="order-form-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 11H3a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h18a1 1 0 0 0 1-1V3a1 1 0 0 0-1-1h-7"/>
              <path d="M9 3V1h4v2"/>
              <path d="M9 3v6h4V3"/>
              <path d="M3 15h6v3H3z"/>
              <path d="M15 15h6v3h-6z"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ form.orderNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="orderRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 订单基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('basic')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                </span>
                订单基本信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.basic">
              <el-row>
                <el-col :span="8"><el-form-item label="订单编号" prop="orderNo"><el-input v-model="form.orderNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="关联合同" prop="contractId">
                  <el-select v-model="form.contractId" filterable clearable placeholder="请选择合同" style="width: 100%" @change="onContractChange">
                    <el-option v-for="c in contractOptions" :key="c.contractId" :label="c.contractNo + ' - ' + c.contractName" :value="c.contractId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="8"><el-form-item label="关联客户" prop="customerId">
                  <el-select v-model="form.customerId" filterable clearable placeholder="请选择客户" style="width: 100%" @change="onCustomerChange">
                    <el-option v-for="c in customerOptions" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="8"><el-form-item label="订单金额" prop="orderAmount"><el-input-number v-model="form.orderAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="订单状态" prop="orderStatus">
                  <el-select v-model="form.orderStatus" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in marketing_order_status" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 订单明细 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('items')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="8" y1="6" x2="21" y2="6"/>
                    <line x1="8" y1="12" x2="21" y2="12"/>
                    <line x1="8" y1="18" x2="21" y2="18"/>
                    <line x1="3" y1="6" x2="3.01" y2="6"/>
                    <line x1="3" y1="12" x2="3.01" y2="12"/>
                    <line x1="3" y1="18" x2="3.01" y2="18"/>
                  </svg>
                </span>
                订单明细
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.items }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.items">
              <el-row :gutter="10" class="mb8">
                <el-col :span="1.5"><el-button type="primary" plain icon="Plus" size="small" @click="handleAddItem">添加明细</el-button></el-col>
              </el-row>
              <el-table border :data="form.itemList" size="small">
                <el-table-column label="行号" prop="lineNo" width="70" align="center" />
                <el-table-column label="商品名称" min-width="180" align="center">
                  <template #default="scope">
                    <el-select v-model="scope.row.materialId" filterable clearable size="small" placeholder="请选择商品" style="width: 100%" @change="(val) => onMaterialChange(val, scope.$index)">
                      <el-option v-for="m in materialOptions" :key="m.materialId" :label="m.materialName" :value="m.materialId" :disabled="selectedMaterialIds.includes(m.materialId) && scope.row.materialId !== m.materialId" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="规格型号" width="140" align="center">
                  <template #default="scope"><span>{{ scope.row.productSpec }}</span></template>
                </el-table-column>
                <el-table-column label="单位" width="90" align="center">
                  <template #default="scope">
                    <el-select v-model="scope.row.unit" size="small" placeholder="单位" style="width: 100%">
                      <el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="数量" width="100" align="center">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.quantity" :min="1" :controls="false" size="small" style="width: 100%" @change="calcSubtotal(scope.$index)" />
                  </template>
                </el-table-column>
                <el-table-column label="单价" width="120" align="center">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.unitPrice" :min="0" :precision="2" :controls="false" size="small" style="width: 100%" @change="calcSubtotal(scope.$index)" />
                  </template>
                </el-table-column>
                <el-table-column label="小计" width="120" align="center">
                  <template #default="scope">{{ formatAmount(scope.row.subtotal) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="80" align="center">
                  <template #default="scope">
                    <el-button link type="danger" icon="Delete" size="small" @click="handleDeleteItem(scope.$index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </section>

          <!-- 负责信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('owner')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                  </svg>
                </span>
                负责信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.owner }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.owner">
              <el-row>
                <el-col :span="12"><el-form-item label="负责人" prop="userId">
                  <el-select v-model="form.userId" filterable clearable placeholder="请选择负责人" style="width: 100%" @change="onUserChange">
                    <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="所属部门" prop="deptId">
                  <el-tree-select v-model="form.deptId" :data="deptOptions" :props="{ value: 'id', label: 'label', children: 'children' }" value-key="id" placeholder="请选择部门" check-strictly clearable style="width: 100%" />
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('other')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="16" x2="12" y2="12"/>
                    <line x1="12" y1="8" x2="12.01" y2="8"/>
                  </svg>
                </span>
                其他信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.other">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewOpen" width="1000px" append-to-body draggable class="order-detail-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 11H3a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h18a1 1 0 0 0 1-1V3a1 1 0 0 0-1-1h-7"/>
              <path d="M9 3V1h4v2"/>
              <path d="M9 3v6h4V3"/>
              <path d="M3 15h6v3H3z"/>
              <path d="M15 15h6v3h-6z"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">订单详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewForm.orderNo }}</span>
            <dict-tag :options="marketing_order_status" :value="viewForm.orderStatus" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
              </span>
              基本信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">订单编号</span><div class="rd-value">{{ viewForm.orderNo }}</div></div>
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ viewForm.contractNo }}</div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ viewForm.customerName }}</div></div>
              <div class="rd-item"><span class="rd-label">订单金额</span><div class="rd-value rd-value--large rd-amount">￥{{ formatAmount(viewForm.orderAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">订单状态</span><div class="rd-value"><dict-tag :options="marketing_order_status" :value="viewForm.orderStatus" /></div></div>
            </div>
          </div>
        </section>

        <!-- 订单明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('items')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="8" y1="6" x2="21" y2="6"/>
                  <line x1="8" y1="12" x2="21" y2="12"/>
                  <line x1="8" y1="18" x2="21" y2="18"/>
                  <line x1="3" y1="6" x2="3.01" y2="6"/>
                  <line x1="3" y1="12" x2="3.01" y2="12"/>
                  <line x1="3" y1="18" x2="3.01" y2="18"/>
                </svg>
              </span>
              订单明细
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.items }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.items">
            <el-table border :data="viewForm.itemList" size="small" v-if="viewForm.itemList && viewForm.itemList.length > 0">
              <el-table-column label="行号" prop="lineNo" width="70" align="center" />
              <el-table-column label="商品名称" prop="productName" show-overflow-tooltip align="center" />
              <el-table-column label="规格型号" prop="productSpec" width="140" align="center" />
              <el-table-column label="单位" width="80" align="center">
                <template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template>
              </el-table-column>
              <el-table-column label="数量" prop="quantity" width="80" align="center" />
              <el-table-column label="单价" width="100" align="center">
                <template #default="scope">{{ formatAmount(scope.row.unitPrice) }}</template>
              </el-table-column>
              <el-table-column label="小计" width="120" align="center">
                <template #default="scope">{{ formatAmount(scope.row.subtotal) }}</template>
              </el-table-column>
            </el-table>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
              <p class="rd-empty-text">暂无订单明细</p>
            </div>
          </div>
        </section>

        <!-- 物流信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('logistics')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="1" y="3" width="15" height="13"/>
                  <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/>
                  <circle cx="5.5" cy="18.5" r="2.5"/>
                  <circle cx="18.5" cy="18.5" r="2.5"/>
                </svg>
              </span>
              物流信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.logistics }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.logistics">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">物流公司</span><div class="rd-value">{{ viewForm.logisticsCompany }}</div></div>
              <div class="rd-item"><span class="rd-label">物流单号</span><div class="rd-value">{{ viewForm.trackingNo }}</div></div>
              <div class="rd-item"><span class="rd-label">发货时间</span><div class="rd-value">{{ viewForm.deliverTime }}</div></div>
              <div class="rd-item"><span class="rd-label">签收时间</span><div class="rd-value">{{ viewForm.receiveTime }}</div></div>
              <div class="rd-item"><span class="rd-label">签收人</span><div class="rd-value">{{ viewForm.receivePerson }}</div></div>
              <div class="rd-item"><span class="rd-label">完成时间</span><div class="rd-value">{{ viewForm.finishTime }}</div></div>
            </div>
          </div>
        </section>

        <!-- 负责信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('owner')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
              </span>
              负责信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.owner }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.owner">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ viewForm.userName }}</div></div>
              <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ viewForm.deptName }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime }}</div></div>
            </div>
          </div>
        </section>

        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('other')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="16" x2="12" y2="12"/>
                  <line x1="12" y1="8" x2="12.01" y2="8"/>
                </svg>
              </span>
              其他信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.other">
            <div class="rd-grid">
              <div class="rd-item rd-item--full">
                <span class="rd-label">备注</span>
                <div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无备注' }}</div>
              </div>
              <div class="rd-item rd-item--full" v-if="viewForm.cancelReason">
                <span class="rd-label">取消原因</span>
                <div class="rd-value">{{ viewForm.cancelReason }}</div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog v-model="deliverOpen" width="700px" append-to-body draggable class="order-deliver-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="1" y="3" width="15" height="13"/>
              <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/>
              <circle cx="5.5" cy="18.5" r="2.5"/>
              <circle cx="18.5" cy="18.5" r="2.5"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">订单发货</span>
          <div class="rd-detail-header-sub" v-if="deliverForm.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ deliverForm.orderNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="deliverRef" :model="deliverForm" :rules="deliverRules" label-width="100px">
        <div class="rd-page">
          <!-- 订单信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('deliverOrder')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                </span>
                订单信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.deliverOrder }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.deliverOrder">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">订单编号</span><div class="rd-value">{{ deliverForm.orderNo }}</div></div>
                <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ deliverForm.customerName }}</div></div>
              </div>
            </div>
          </section>

          <!-- 物流信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('deliverLogistics')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="1" y="3" width="15" height="13"/>
                    <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/>
                    <circle cx="5.5" cy="18.5" r="2.5"/>
                    <circle cx="18.5" cy="18.5" r="2.5"/>
                  </svg>
                </span>
                物流信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.deliverLogistics }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.deliverLogistics">
              <el-row>
                <el-col :span="12"><el-form-item label="物流公司" prop="logisticsCompany"><el-input v-model="deliverForm.logisticsCompany" placeholder="请输入物流公司" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="物流单号" prop="trackingNo"><el-input v-model="deliverForm.trackingNo" placeholder="请输入物流单号" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="发货时间" prop="deliverTime"><el-date-picker v-model="deliverForm.deliverTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择发货时间" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('deliverOther')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="16" x2="12" y2="12"/>
                    <line x1="12" y1="8" x2="12.01" y2="8"/>
                  </svg>
                </span>
                其他信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.deliverOther }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.deliverOther">
              <el-form-item label="备注"><el-input v-model="deliverForm.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitDeliver">确认发货</el-button>
        <el-button @click="deliverOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 签收对话框 -->
    <el-dialog v-model="receiveOpen" width="600px" append-to-body draggable class="order-receive-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 8V21H3V8"/>
              <path d="M1 3h22v5H1z"/>
              <path d="M10 12h4"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">订单签收</span>
          <div class="rd-detail-header-sub" v-if="receiveForm.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ receiveForm.orderNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="receiveRef" :model="receiveForm" label-width="100px">
        <div class="rd-page">
          <!-- 订单信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('receiveOrder')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                </span>
                订单信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.receiveOrder }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.receiveOrder">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">订单编号</span><div class="rd-value">{{ receiveForm.orderNo }}</div></div>
                <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ receiveForm.customerName }}</div></div>
              </div>
            </div>
          </section>

          <!-- 签收信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('receiveInfo')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                    <polyline points="22 4 12 14.01 9 11.01"/>
                  </svg>
                </span>
                签收信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.receiveInfo }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.receiveInfo">
              <el-form-item label="签收人" prop="receivePerson"><el-input v-model="receiveForm.receivePerson" placeholder="请输入签收人" style="width: 100%" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="success" @click="submitReceive">确认签收</el-button>
        <el-button @click="receiveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 取消订单对话框 -->
    <el-dialog v-model="cancelOpen" width="600px" append-to-body draggable class="order-cancel-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">取消订单</span>
          <div class="rd-detail-header-sub" v-if="cancelForm.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ cancelForm.orderNo }}</span>
          </div>
        </div>
      </template>
      <el-form label-width="100px">
        <div class="rd-page">
          <!-- 订单信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cancelOrder')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                </span>
                订单信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cancelOrder }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cancelOrder">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">订单编号</span><div class="rd-value">{{ cancelForm.orderNo }}</div></div>
              </div>
            </div>
          </section>

          <!-- 取消信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cancelInfo')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="15" y1="9" x2="9" y2="15"/>
                    <line x1="9" y1="9" x2="15" y2="15"/>
                  </svg>
                </span>
                取消信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cancelInfo }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cancelInfo">
              <el-form-item label="取消原因">
                <el-input v-model="cancelForm.cancelReason" type="textarea" :rows="3" placeholder="请输入取消原因" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="submitCancel">确认取消</el-button>
        <el-button @click="cancelOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 退货对话框 -->
    <el-dialog v-model="returnOpen" width="700px" append-to-body draggable class="order-return-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
              <path d="M3 3v5h5"/>
              <path d="M9 12h6"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">订单退货</span>
          <div class="rd-detail-header-sub" v-if="returnForm.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ returnForm.orderNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="returnRef" :model="returnForm" :rules="returnRules" label-width="100px">
        <div class="rd-page">
          <!-- 订单信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('returnOrder')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                </span>
                订单信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.returnOrder }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.returnOrder">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">订单编号</span><div class="rd-value">{{ returnForm.orderNo }}</div></div>
                <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ returnForm.customerName }}</div></div>
                <div class="rd-item"><span class="rd-label">订单金额</span><div class="rd-value rd-value--large rd-amount">￥{{ formatAmount(returnForm.orderAmount) }}</div></div>
              </div>
            </div>
          </section>

          <!-- 退货信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('returnInfo')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
                    <path d="M3 3v5h5"/>
                    <path d="M9 12h6"/>
                  </svg>
                </span>
                退货信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.returnInfo }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.returnInfo">
              <el-row>
                <el-col :span="12"><el-form-item label="退货金额" prop="returnAmount"><el-input-number v-model="returnForm.returnAmount" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="退货原因" prop="returnReason"><el-input v-model="returnForm.returnReason" type="textarea" :rows="3" placeholder="请输入退货原因" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="submitReturn">提交退货</el-button>
        <el-button @click="returnOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkOrder">
import { listOrder, getOrder, addOrder, updateOrder, delOrder, deliverOrder, confirmOrder, receiveOrder, completeOrder, cancelOrder } from '@/api/mk/order'
import { listContract } from '@/api/mk/contract'
import { listCustomer } from '@/api/mk/customer'
import { listMaterial } from '@/api/wms/material'
import { addReturn } from '@/api/mk/returnOrder'
import { listUser, deptTreeSelect } from '@/api/system/user'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_order_index')
const { marketing_order_status, wms_unit } = proxy.useDict('marketing_order_status', 'wms_unit')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const deliverOpen = ref(false)
const receiveOpen = ref(false)
const cancelOpen = ref(false)
const returnOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const userOptions = ref([])
const deptOptions = ref([])
const customerOptions = ref([])
const contractOptions = ref([])
const materialOptions = ref([])
const viewForm = ref({})
const deliverForm = ref({})
const receiveForm = ref({})
const cancelForm = ref({})
const returnForm = ref({})
const collapsedCards = reactive({ basic: false, logistics: false, owner: false, items: false, other: false, deliverOrder: false, deliverLogistics: false, deliverOther: false, returnOrder: false, returnInfo: false, receiveOrder: false, receiveInfo: false, cancelOrder: false, cancelInfo: false })
function toggleCard(name) { collapsedCards[name] = !collapsedCards[name] }
function formatAmount(val) { if (val == null || val === '') return '-'; return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }

const deliverRules = {
  logisticsCompany: [{ required: true, message: '请输入物流公司', trigger: 'blur' }],
  trackingNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }],
  deliverTime: [{ required: true, message: '请选择发货时间', trigger: 'change' }]
}

const returnRules = {
  returnAmount: [{ required: true, message: '退货金额不能为空', trigger: 'blur' }],
  returnReason: [{ required: true, message: '请输入退货原因', trigger: 'blur' }]
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, orderNo: undefined, customerName: undefined, orderStatus: undefined },
  rules: {
    customerId: [{ required: true, message: '请选择关联客户', trigger: 'change' }],
    orderAmount: [{ required: true, message: '订单金额不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const selectedMaterialIds = computed(() => {
  if (!form.value.itemList) return []
  return form.value.itemList.map(i => i.materialId).filter(id => id != null)
})

function getList() {
  loading.value = true
  listOrder(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function getUserOptions() { listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) }) }
function getDeptTree() { deptTreeSelect().then(res => { deptOptions.value = res.data }) }
function getCustomerOptions() { listCustomer({ pageNum: 1, pageSize: 9999 }).then(res => { customerOptions.value = res.rows }) }
function getContractOptions() { listContract({ pageNum: 1, pageSize: 9999 }).then(res => { contractOptions.value = res.rows }) }
function getMaterialOptions() { listMaterial({ pageNum: 1, pageSize: 9999 }).then(res => { materialOptions.value = res.rows }) }

function onUserChange(userId) {
  if (userId) {
    const user = userOptions.value.find(u => u.userId === userId)
    if (user) { form.value.userName = user.nickName; if (user.deptId) { form.value.deptId = user.deptId; form.value.deptName = user.dept ? user.dept.deptName : undefined } }
  } else { form.value.userName = undefined }
}
function onCustomerChange(customerId) {
  if (customerId) { const customer = customerOptions.value.find(c => c.customerId === customerId); if (customer) { form.value.customerName = customer.customerName } } else { form.value.customerName = undefined }
}
function onContractChange(contractId) {
  if (contractId) { const contract = contractOptions.value.find(c => c.contractId === contractId); if (contract) { form.value.contractNo = contract.contractNo; if (contract.customerId) { form.value.customerId = contract.customerId; form.value.customerName = contract.customerName } } } else { form.value.contractNo = undefined }
}
function handleAddItem() { if (!form.value.itemList) { form.value.itemList = [] }; const lineNo = (form.value.itemList.length + 1) * 10; form.value.itemList.push({ lineNo, materialId: undefined, productName: undefined, productSpec: undefined, unit: undefined, quantity: 1, unitPrice: 0, subtotal: 0 }) }
function handleDeleteItem(index) { form.value.itemList.splice(index, 1); form.value.itemList.forEach((item, idx) => { item.lineNo = (idx + 1) * 10 }) }
function onMaterialChange(materialId, index) { const material = materialOptions.value.find(m => m.materialId === materialId); if (material) { form.value.itemList[index].productName = material.materialName; form.value.itemList[index].productSpec = material.specModel; form.value.itemList[index].unit = material.unit } }
function calcSubtotal(index) { const item = form.value.itemList[index]; if (item && item.quantity && item.unitPrice) { item.subtotal = (item.quantity * item.unitPrice).toFixed(2) } else { item.subtotal = 0 }; const total = form.value.itemList.reduce((sum, i) => sum + (parseFloat(i.subtotal) || 0), 0); form.value.orderAmount = total.toFixed(2) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.orderId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { orderNo: undefined, contractId: undefined, contractNo: undefined, customerId: undefined, customerName: undefined, orderAmount: 0, orderStatus: '0', logisticsCompany: undefined, trackingNo: undefined, deliverTime: undefined, finishTime: undefined, userId: undefined, userName: undefined, deptId: undefined, deptName: undefined, itemList: [], remark: undefined }; collapsedCards.basic = false; collapsedCards.logistics = false; collapsedCards.owner = false; collapsedCards.items = false; collapsedCards.other = false; proxy.resetForm('orderRef') }
function handleAdd() { reset(); open.value = true; title.value = '新增订单' }
function handleUpdate(row) { reset(); getOrder(row.orderId || ids.value[0]).then(res => { form.value = res.data; if (!form.value.itemList) { form.value.itemList = [] }; open.value = true; title.value = '修改订单' }) }
function handleView(row) { getOrder(row.orderId).then(res => { viewForm.value = res.data; collapsedCards.basic = false; collapsedCards.logistics = !res.data.logisticsCompany && !res.data.trackingNo && !res.data.deliverTime && !res.data.receiveTime && !res.data.receivePerson && !res.data.finishTime; collapsedCards.owner = false; collapsedCards.items = !res.data.itemList || res.data.itemList.length === 0; collapsedCards.other = !res.data.remark && !res.data.cancelReason; viewOpen.value = true }) }
function submitForm() { proxy.$refs['orderRef'].validate(valid => { if (valid) { if (form.value.orderId != undefined) { updateOrder(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addOrder(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const orderIds = row.orderId || ids.value; proxy.$modal.confirm('确认删除编号为"' + orderIds + '"的数据？').then(() => delOrder(orderIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }

function handleConfirm(row) { proxy.$modal.confirm('确认订单【' + row.orderNo + '】？').then(() => confirmOrder(row.orderId)).then(() => { getList(); proxy.$modal.msgSuccess('订单已确认') }).catch(() => {}) }

function handleDeliver(row) { deliverForm.value = { orderId: row.orderId, orderNo: row.orderNo, customerName: row.customerName, logisticsCompany: undefined, trackingNo: undefined, deliverTime: undefined, remark: undefined }; deliverOpen.value = true }
function submitDeliver() { proxy.$refs['deliverRef'].validate(valid => { if (valid) { deliverOrder(deliverForm.value).then(() => { proxy.$modal.msgSuccess('发货成功'); deliverOpen.value = false; getList() }) } }) }

function handleReceive(row) { receiveForm.value = { orderId: row.orderId, orderNo: row.orderNo, customerName: row.customerName, receivePerson: undefined }; receiveOpen.value = true }
function submitReceive() { receiveOrder(receiveForm.value.orderId, receiveForm.value.receivePerson).then(() => { proxy.$modal.msgSuccess('签收成功'); receiveOpen.value = false; getList() }) }

function handleComplete(row) { proxy.$modal.confirm('确认完成订单【' + row.orderNo + '】？').then(() => completeOrder(row.orderId)).then(() => { getList(); proxy.$modal.msgSuccess('订单已完成') }).catch(() => {}) }

function handleCancel(row) { cancelForm.value = { orderId: row.orderId, orderNo: row.orderNo, cancelReason: '' }; cancelOpen.value = true }
function submitCancel() { if (!cancelForm.value.cancelReason) { proxy.$modal.msgWarning('请输入取消原因'); return }; cancelOrder(cancelForm.value.orderId, cancelForm.value.cancelReason).then(() => { proxy.$modal.msgSuccess('订单已取消'); cancelOpen.value = false; getList() }) }

function handleReturn(row) { returnForm.value = { orderId: row.orderId, orderNo: row.orderNo, customerId: row.customerId, customerName: row.customerName, orderAmount: row.orderAmount, returnAmount: row.orderAmount, returnReason: '' }; returnOpen.value = true }
function submitReturn() { proxy.$refs['returnRef'].validate(valid => { if (valid) { addReturn(returnForm.value).then(() => { proxy.$modal.msgSuccess('退货申请已提交'); returnOpen.value = false; getList() }) } }) }

function handleExport() { proxy.download('mk/order/export', { ...queryParams.value }, `order_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
getUserOptions(); getDeptTree(); getCustomerOptions(); getContractOptions(); getMaterialOptions(); getList()
</script>

<style scoped>
/* ===== 订单页面卡片式样式（遵循 detail-page-style-guide 规范） ===== */
:deep(.order-cancel-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.order-cancel-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.order-cancel-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.order-cancel-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.order-cancel-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

:deep(.order-receive-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.order-receive-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.order-receive-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.order-receive-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.order-receive-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

:deep(.order-return-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.order-return-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.order-return-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.order-return-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.order-return-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

:deep(.order-deliver-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.order-deliver-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.order-deliver-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.order-deliver-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.order-deliver-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

:deep(.order-form-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.order-form-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.order-form-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.order-form-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.order-form-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

:deep(.order-detail-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.order-detail-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.order-detail-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.order-detail-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.order-detail-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

.rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 2px 8px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; letter-spacing: -0.02em; white-space: nowrap; }
.rd-detail-header-sub { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); flex-shrink: 0; }
.rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); font-variant-numeric: tabular-nums; white-space: nowrap; }

.rd-page { max-width: 940px; margin: 0 auto; }

.rd-card { background: #fff; border-radius: 12px; border: 1px solid #e5e7eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); overflow: hidden; margin-bottom: 8px; transition: box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1); animation: rdFadeIn 0.4s ease-out forwards; }
.rd-card:hover { box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1); }
.rd-card:last-child { margin-bottom: 0; }
.rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; background: linear-gradient(to right, #f9fafb, #fff); border-bottom: 1px solid #f3f4f6; cursor: pointer; user-select: none; }
.rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: #111827; }
.rd-card-icon { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 8px; background: #fff; border: 1px solid #e5e7eb; color: #2563eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); }
.rd-card-body { padding: 14px 16px; }

.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-label-dot { display: none; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-value--large { font-size: 18px; font-weight: 700; }
.rd-value--muted { color: #9ca3af; font-style: italic; }
.rd-amount { font-variant-numeric: tabular-nums; font-weight: 700; color: #111827; }

.rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border-radius: 6px; border: none; background: transparent; color: #9ca3af; cursor: pointer; transition: all 0.15s ease; flex-shrink: 0; }
.rd-collapse-btn:hover { background: #f3f4f6; color: #4b5563; }
.rd-collapse-btn svg { transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.rd-collapse-btn.is-collapsed svg { transform: rotate(-90deg); }

.rd-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 20px; text-align: center; color: #9ca3af; background: #f9fafb; border-radius: 12px; border: 1px dashed #e5e7eb; }
.rd-empty-icon { margin-bottom: 4px; color: #d1d5db; }
.rd-empty-text { font-size: 14px; font-weight: 500; margin: 0; }

@keyframes rdFadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.rd-card:nth-child(2) { animation-delay: 0.06s; }
.rd-card:nth-child(3) { animation-delay: 0.12s; }
.rd-card:nth-child(4) { animation-delay: 0.18s; }

@media (max-width: 768px) { .rd-grid { grid-template-columns: 1fr; } .rd-card-header { padding: 8px 12px; } .rd-card-body { padding: 12px; } }
</style>

<template>
  <div class="app-container">
    <!-- ===== 搜索表单（默认简洁模式） ===== -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="供应商编码" prop="supplierCode">
        <el-input v-model="queryParams.supplierCode" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="供应商名称" prop="supplierName">
        <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="供应商类型" prop="supplierType">
        <el-select v-model="queryParams.supplierType" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="d in wms_supplier_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="d in sys_normal_disable" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="简称" prop="supplierShortName" v-show="showAdvanced">
        <el-input v-model="queryParams.supplierShortName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="等级" prop="supplierLevel" v-show="showAdvanced">
        <el-select v-model="queryParams.supplierLevel" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="d in wms_supplier_level" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="统一信用代码" prop="unifiedCreditCode" v-show="showAdvanced">
        <el-input v-model="queryParams.unifiedCreditCode" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="联系人" prop="contactPerson" v-show="showAdvanced">
        <el-input v-model="queryParams.contactPerson" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="联系电话" prop="contactPhone" v-show="showAdvanced">
        <el-input v-model="queryParams.contactPhone" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="法人代表" prop="legalPerson" v-show="showAdvanced">
        <el-input v-model="queryParams.legalPerson" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="企业性质" prop="enterpriseNature" v-show="showAdvanced">
        <el-select v-model="queryParams.enterpriseNature" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="d in wms_enterprise_nature" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" v-show="showAdvanced">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button type="info" plain :icon="showAdvanced ? 'ArrowUp' : 'ArrowDown'" @click="toggleAdvanced">{{ showAdvanced ? '收起' : '更多' }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:supplier:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['wms:supplier:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:supplier:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['wms:supplier:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['wms:supplier:import']">导入</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- ===== 列表表格 ===== -->
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="供应商编码" prop="supplierCode" :width="colWidth('supplierCode', 150)" resizable sortable="custom" />
      <el-table-column label="供应商名称" prop="supplierName" :width="colWidth('supplierName', 220)" resizable show-overflow-tooltip />
      <el-table-column label="简称" prop="supplierShortName" :width="colWidth('supplierShortName', 120)" resizable show-overflow-tooltip />
      <el-table-column label="类型" prop="supplierType" :width="colWidth('supplierType', 90)" resizable align="center">
        <template #default="scope"><dict-tag :options="wms_supplier_type" :value="scope.row.supplierType" /></template>
      </el-table-column>
      <el-table-column label="等级" prop="supplierLevel" :width="colWidth('supplierLevel', 90)" resizable align="center" sortable="custom">
        <template #default="scope"><dict-tag :options="wms_supplier_level" :value="scope.row.supplierLevel" /></template>
      </el-table-column>
      <el-table-column label="统一信用代码" prop="unifiedCreditCode" :width="colWidth('unifiedCreditCode', 180)" resizable show-overflow-tooltip />
      <el-table-column label="联系人" prop="contactPerson" :width="colWidth('contactPerson', 100)" resizable />
      <el-table-column label="联系电话" prop="contactPhone" :width="colWidth('contactPhone', 130)" resizable />
      <el-table-column label="法人代表" prop="legalPerson" :width="colWidth('legalPerson', 100)" resizable />
      <el-table-column label="企业性质" prop="enterpriseNature" :width="colWidth('enterpriseNature', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="wms_enterprise_nature" :value="scope.row.enterpriseNature" /></template>
      </el-table-column>
      <el-table-column label="状态" prop="status" :width="colWidth('status', 80)" resizable align="center" sortable="custom">
        <template #default="scope"><dict-tag :options="sys_normal_disable" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['wms:supplier:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:supplier:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- ===== 导入对话框 ===== -->
    <excel-import-dialog
      ref="importRef"
      title="供应商导入"
      action="/wms/supplier/importData"
      template-action="/wms/supplier/importTemplate"
      template-file-name="supplier_template"
      update-support-label="是否更新已经存在的供应商数据"
      :tips="importTips"
      :update-key-options="updateKeyOptions"
      default-update-key="supplierName"
      @success="getList"
    />

    <!-- ===== 添加/修改对话框 ===== -->
    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="supplierRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <!-- 基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="供应商编码" prop="supplierCode"><el-input v-model="form.supplierCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="供应商名称" prop="supplierName">
                  <el-input v-model="form.supplierName" placeholder="请输入企业全称" />
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="供应商简称" prop="supplierShortName"><el-input v-model="form.supplierShortName" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="供应商类型" prop="supplierType">
                  <el-select v-model="form.supplierType" placeholder="请选择" style="width: 100%"><el-option v-for="d in wms_supplier_type" :key="d.value" :label="d.label" :value="d.value" /></el-select>
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="供应商等级" prop="supplierLevel">
                  <el-select v-model="form.supplierLevel" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in wms_supplier_level" :key="d.value" :label="d.label" :value="d.value" /></el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="供应商类型" prop="supplierType">
                  <el-select v-model="form.supplierType" placeholder="请选择" style="width: 100%"><el-option v-for="d in wms_supplier_type" :key="d.value" :label="d.label" :value="d.value" /></el-select>
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="统一信用代码" prop="unifiedCreditCode">
                  <div style="display: flex; gap: 8px; width: 100%">
                    <el-input v-model="form.unifiedCreditCode" placeholder="请输入18位统一社会信用代码" @keyup.enter="handleEnterpriseLookup" style="flex: 1" />
                    <el-tooltip content="输入统一社会信用代码后点击查询，自动获取企业名称、法人等信息" placement="top">
                      <el-button type="primary" plain icon="Search" :loading="enterpriseLoading" @click="handleEnterpriseLookup">查询企业</el-button>
                    </el-tooltip>
                  </div>
                  <div style="color: #999; font-size: 12px; margin-top: 4px;">提示：输入18位统一社会信用代码，点击查询按钮自动获取企业信息</div>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 联系信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg></span>联系信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="联系人" prop="contactPerson"><el-input v-model="form.contactPerson" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="联系电话" prop="contactPhone"><el-input v-model="form.contactPhone" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="邮箱" prop="email"><el-input v-model="form.email" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="传真" prop="fax"><el-input v-model="form.fax" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="网址" prop="website"><el-input v-model="form.website" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="邮编" prop="postcode"><el-input v-model="form.postcode" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="地址" prop="address"><el-input v-model="form.address" type="textarea" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 企业信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18M3 10h18M5 6l7-3 7 3M4 10v11M20 10v11M8 14v3M12 14v3M16 14v3"/></svg></span>企业信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="法人代表" prop="legalPerson"><el-input v-model="form.legalPerson" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="企业性质" prop="enterpriseNature">
                  <el-select v-model="form.enterpriseNature" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in wms_enterprise_nature" :key="d.value" :label="d.label" :value="d.value" /></el-select>
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="注册资本(万)" prop="registeredCapital"><el-input-number v-model="form.registeredCapital" :precision="2" :min="0" placeholder="请输入" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="成立日期" prop="establishedDate">
                  <el-date-picker v-model="form.establishedDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" />
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="经营范围" prop="businessScope"><el-input v-model="form.businessScope" type="textarea" :rows="2" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 财务/开票信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c4')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></span>财务/开票信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c4">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="开户银行" prop="bankName"><el-input v-model="form.bankName" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="开户支行" prop="bankBranch"><el-input v-model="form.bankBranch" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="银行账号" prop="bankAccount"><el-input v-model="form.bankAccount" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="税号" prop="taxNumber"><el-input v-model="form.taxNumber" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="发票抬头" prop="invoiceTitle"><el-input v-model="form.invoiceTitle" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="发票电话" prop="invoicePhone"><el-input v-model="form.invoicePhone" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="发票地址" prop="invoiceAddress"><el-input v-model="form.invoiceAddress" type="textarea" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group>
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看对话框 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></div>
          <span class="rd-detail-header-title">供应商详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.supplierCode">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编码：{{ viewData.supplierCode }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">供应商编码</span><div class="rd-value">{{ viewData.supplierCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商名称</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商简称</span><div class="rd-value">{{ viewData.supplierShortName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商类型</span><div class="rd-value"><dict-tag :options="wms_supplier_type" :value="viewData.supplierType" /></div></div>
              <div class="rd-item"><span class="rd-label">供应商等级</span><div class="rd-value"><dict-tag :options="wms_supplier_level" :value="viewData.supplierLevel" /></div></div>
              <div class="rd-item"><span class="rd-label">统一信用代码</span><div class="rd-value">{{ viewData.unifiedCreditCode || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 联系信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg></span>联系信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ viewData.contactPerson || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">联系电话</span><div class="rd-value">{{ viewData.contactPhone || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">邮箱</span><div class="rd-value">{{ viewData.email || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">传真</span><div class="rd-value">{{ viewData.fax || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">网址</span><div class="rd-value">{{ viewData.website || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">邮编</span><div class="rd-value">{{ viewData.postcode || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">地址</span><div class="rd-value">{{ viewData.address || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 企业信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18M3 10h18M5 6l7-3 7 3M4 10v11M20 10v11M8 14v3M12 14v3M16 14v3"/></svg></span>企业信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v3">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">法人代表</span><div class="rd-value">{{ viewData.legalPerson || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">企业性质</span><div class="rd-value"><dict-tag :options="wms_enterprise_nature" :value="viewData.enterpriseNature" /></div></div>
              <div class="rd-item"><span class="rd-label">注册资本(万元)</span><div class="rd-value rd-amount">{{ formatAmount(viewData.registeredCapital) }}</div></div>
              <div class="rd-item"><span class="rd-label">成立日期</span><div class="rd-value">{{ viewData.establishedDate || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">经营范围</span><div class="rd-value">{{ viewData.businessScope || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 财务/开票信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v4')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></span>财务/开票信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v4">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">开户银行</span><div class="rd-value">{{ viewData.bankName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">开户支行</span><div class="rd-value">{{ viewData.bankBranch || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">银行账号</span><div class="rd-value">{{ viewData.bankAccount || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">税号</span><div class="rd-value">{{ viewData.taxNumber || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发票抬头</span><div class="rd-value">{{ viewData.invoiceTitle || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发票电话</span><div class="rd-value">{{ viewData.invoicePhone || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">发票地址</span><div class="rd-value">{{ viewData.invoiceAddress || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="sys_normal_disable" :value="viewData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="WmsSupplier">
import { listSupplier, getSupplier, addSupplier, updateSupplier, delSupplier } from '@/api/wms/supplier'
import { lookupEnterprise } from '@/api/pms/enterprise'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount } from '@/composables/useDetailCard'
import ExcelImportDialog from '@/components/ExcelImportDialog'

const { proxy } = getCurrentInstance()
const { wms_supplier_type, wms_supplier_level, wms_enterprise_nature, sys_normal_disable } = proxy.useDict(
  'wms_supplier_type', 'wms_supplier_level', 'wms_enterprise_nature', 'sys_normal_disable'
)

const { collapsedCards, toggleCard } = useDetailCard(["c1","c2","c3","c4","c0","v1","v2","v3","v4","v0"])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_supplier_index')

// ===== 导入功能配置 =====
const importTips = [
  '供应商名称为必填字段，不能为空',
  '供应商编码由系统自动生成，无需填写',
  '供应商类型填：厂商/经销商/代理商/服务商（或留空）',
  '供应商等级、企业性质等字典字段请参考系统中已有数据填写',
  '如勾选「更新已存在数据」，需选择匹配字段：按名称、统一信用代码或编码匹配',
  '导入结果将显示每条数据的处理情况，失败数据可修正后重新导入'
]
const updateKeyOptions = [
  { value: 'supplierName', label: '供应商名称' },
  { value: 'unifiedCreditCode', label: '统一社会信用代码' },
  { value: 'supplierCode', label: '供应商编码' }
]

const list = ref([])
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
const enterpriseLoading = ref(false)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    supplierCode: undefined,
    supplierName: undefined,
    supplierType: undefined,
    status: undefined,
    supplierShortName: undefined,
    supplierLevel: undefined,
    unifiedCreditCode: undefined,
    contactPerson: undefined,
    contactPhone: undefined,
    legalPerson: undefined,
    enterpriseNature: undefined,
    params: {}
  },
  rules: {
    supplierName: [{ required: true, message: '供应商名称不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const showAdvanced = ref(false)
const dateRange = ref([])

function getList() {
  loading.value = true
  listSupplier(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function toggleAdvanced() { showAdvanced.value = !showAdvanced.value }
function handleQuery() {
  queryParams.value.pageNum = 1
  proxy.addDateRange(queryParams.value, dateRange.value, 'CreateTime')
  showAdvanced.value = false
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value.params = {}
  dateRange.value = []
  handleQuery()
}

function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.value.params.orderByColumn = undefined
    queryParams.value.params.isAsc = undefined
  }
  getList()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(i => i.supplierId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    supplierId: undefined,
    supplierCode: undefined,
    supplierName: undefined,
    supplierShortName: undefined,
    supplierType: '0',
    supplierLevel: undefined,
    unifiedCreditCode: undefined,
    contactPerson: undefined,
    contactPhone: undefined,
    email: undefined,
    fax: undefined,
    website: undefined,
    postcode: undefined,
    address: undefined,
    legalPerson: undefined,
    registeredCapital: undefined,
    enterpriseNature: undefined,
    businessScope: undefined,
    establishedDate: undefined,
    bankName: undefined,
    bankBranch: undefined,
    bankAccount: undefined,
    taxNumber: undefined,
    invoiceTitle: undefined,
    invoiceAddress: undefined,
    invoicePhone: undefined,
    status: '0',
    remark: undefined
  }
  proxy.resetForm('supplierRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加供应商'
}

function handleUpdate(row) {
  reset()
  getSupplier(row.supplierId || ids.value[0]).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改供应商'
  })
}

function handleView(row) {
  getSupplier(row.supplierId).then(res => {
    viewData.value = res.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['supplierRef'].validate(valid => {
    if (valid) {
      if (form.value.supplierId != undefined) {
        updateSupplier(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addSupplier(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const supplierIds = row.supplierId || ids.value
  proxy.$modal.confirm('确认删除编号为"' + supplierIds + '"的数据？').then(() => delSupplier(supplierIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('wms/supplier/export', { ...queryParams.value }, `supplier_${new Date().getTime()}.xlsx`)
}

function handleImport() {
  proxy.$refs['importRef'].open()
}
function cancel() {
  open.value = false
  reset()
}

// ===== 企业信息查询 =====
function handleEnterpriseLookup() {
  const keyword = form.value.unifiedCreditCode?.trim()
  if (!keyword) {
    proxy.$modal.msgWarning('请先输入统一社会信用代码')
    return
  }
  // 校验统一社会信用代码格式（18位）
  if (keyword.length !== 18) {
    proxy.$modal.msgError('统一社会信用代码应为18位，请检查输入')
    return
  }
  enterpriseLoading.value = true
  lookupEnterprise(keyword).then(res => {
    const info = res.data
    if (!info || !info.enterpriseName || info.enterpriseName.startsWith('【未找到】')) {
      const errorMsg = info?.enterpriseStatus || '未查询到企业信息，请确认统一社会信用代码正确'
      proxy.$modal.msgError(errorMsg)
      return
    }
    // 自动回填字段（仅填充当前为空的字段，避免覆盖已填内容）
    const filledFields = []
    if (!form.value.supplierName && info.enterpriseName) {
      form.value.supplierName = info.enterpriseName
      filledFields.push('企业名称')
    }
    if (!form.value.legalPerson && info.legalPerson) {
      form.value.legalPerson = info.legalPerson
      filledFields.push('法人代表')
    }
    if (!form.value.registeredCapital && info.registeredCapital) {
      form.value.registeredCapital = info.registeredCapital
      filledFields.push('注册资本')
    }
    if (!form.value.establishedDate && info.establishedDate) {
      form.value.establishedDate = info.establishedDate
      filledFields.push('成立日期')
    }
    if (!form.value.enterpriseNature && info.enterpriseNature) {
      form.value.enterpriseNature = info.enterpriseNature
      filledFields.push('企业性质')
    }
    if (!form.value.address && info.address) {
      form.value.address = info.address
      filledFields.push('地址')
    }
    if (!form.value.businessScope && info.businessScope) {
      form.value.businessScope = info.businessScope
      filledFields.push('经营范围')
    }
    const fillMsg = filledFields.length > 0 ? `已自动回填：${filledFields.join('、')}` : '企业信息查询成功'
    proxy.$modal.msgSuccess(`${fillMsg}（${info.dataSource || '查询成功'}）`)
  }).catch(err => {
    const errorMsg = err?.message || err?.msg || '企业信息查询失败，请检查网络或API配置'
    proxy.$modal.msgError(errorMsg)
  }).finally(() => {
    enterpriseLoading.value = false
  })
}
getList()
</script>

<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="线索编号" prop="leadNo"><el-input v-model="queryParams.leadNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="企业名称" prop="companyName"><el-input v-model="queryParams.companyName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="线索状态" prop="leadStatus">
        <el-select v-model="queryParams.leadStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_lead_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="线索等级" prop="leadGrade">
        <el-select v-model="queryParams.leadGrade" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_lead_grade" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="线索来源" prop="leadSource">
        <el-select v-model="queryParams.leadSource" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_customer_source" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:lead:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['marketing:lead:add']">导入</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:lead:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:lead:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="primary" plain icon="User" :disabled="multiple" @click="handleBatchAssign" v-hasPermi="['marketing:lead:assign']">批量分配</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="RefreshRight" :disabled="multiple" @click="handleBatchStatus" v-hasPermi="['marketing:lead:edit']">批量改状态</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:lead:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="线索编号" prop="leadNo" :width="colWidth('leadNo', 150)" resizable />
      <el-table-column label="企业名称" prop="companyName" :width="colWidth('companyName', 200)" resizable show-overflow-tooltip>
        <template #default="scope">
          <el-button link type="primary" @click="handleView(scope.row)">{{ scope.row.companyName }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="联系人" prop="contactName" :width="colWidth('contactName', 100)" resizable />
      <el-table-column label="手机号" prop="contactPhone" :width="colWidth('contactPhone', 130)" resizable />
      <el-table-column label="线索来源" prop="leadSource" :width="colWidth('leadSource', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_customer_source" :value="scope.row.leadSource" /></template>
      </el-table-column>
      <el-table-column label="等级" prop="leadGrade" :width="colWidth('leadGrade', 80)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_lead_grade" :value="scope.row.leadGrade" /></template>
      </el-table-column>
      <el-table-column label="评分" prop="leadScore" :width="colWidth('leadScore', 80)" resizable align="center" />
      <el-table-column label="状态" prop="leadStatus" :width="colWidth('leadStatus', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_lead_status" :value="scope.row.leadStatus" /></template>
      </el-table-column>
      <el-table-column label="负责人" prop="userName" :width="colWidth('userName', 100)" resizable>
        <template #default="scope">
          <span v-if="scope.row.userName">{{ scope.row.userName }}</span>
          <el-tag v-else type="warning" size="small">公海</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后跟进" prop="lastFollowTime" :width="colWidth('lastFollowTime', 160)" resizable />
      <el-table-column label="操作" width="320" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="primary" icon="ChatDotRound" @click="handleFollowUp(scope.row)" v-if="scope.row.leadStatus !== '4' && scope.row.leadStatus !== '5'" v-hasPermi="['marketing:lead:edit']">跟进</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:lead:edit']">修改</el-button>
          <el-dropdown @command="(cmd) => handleCommand(cmd, scope.row)" trigger="click" v-hasPermi="['marketing:lead:edit']">
            <el-button link type="primary" icon="ArrowDown">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="convert" icon="Promotion" v-if="scope.row.leadStatus !== '4' && scope.row.leadStatus !== '5'" v-hasPermi="['marketing:lead:edit']">转化为客户</el-dropdown-item>
                <el-dropdown-item command="assign" icon="User" v-if="scope.row.userId && scope.row.leadStatus !== '4' && scope.row.leadStatus !== '5'" v-hasPermi="['marketing:lead:assign']">转移分配</el-dropdown-item>
                <el-dropdown-item command="release" icon="Coin" v-if="scope.row.userId && scope.row.leadStatus !== '4' && scope.row.leadStatus !== '5'">退回公海</el-dropdown-item>
                <el-dropdown-item command="invalidate" icon="CircleClose" v-if="scope.row.leadStatus !== '4' && scope.row.leadStatus !== '5'" divided>标记无效</el-dropdown-item>
                <el-dropdown-item command="delete" icon="Delete" v-hasPermi="['marketing:lead:remove']" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="leadRef" :model="form" :rules="rules" label-width="100px">
        <el-collapse v-model="activeNames">
          <el-collapse-item title="线索信息" name="lead">
            <el-row>
              <el-col :span="12"><el-form-item label="线索编号" prop="leadNo"><el-input v-model="form.leadNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="企业名称" prop="companyName"><el-input v-model="form.companyName" placeholder="请输入企业名称" @blur="onCompanyBlur" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="联系人" prop="contactName"><el-input v-model="form.contactName" placeholder="请输入联系人姓名" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="手机号" prop="contactPhone"><el-input v-model="form.contactPhone" placeholder="请输入手机号" @blur="onPhoneBlur" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="邮箱" prop="contactEmail"><el-input v-model="form.contactEmail" placeholder="请输入邮箱" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="职位" prop="position"><el-input v-model="form.position" placeholder="请输入职位" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="企业信息" name="company">
            <el-row>
              <el-col :span="12"><el-form-item label="所属行业" prop="industry">
                <el-select v-model="form.industry" placeholder="请选择" style="width: 100%"><el-option v-for="d in marketing_industry" :key="d.value" :label="d.label" :value="d.value" /></el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="企业规模" prop="companySize">
                <el-select v-model="form.companySize" placeholder="请选择" style="width: 100%"><el-option v-for="d in marketing_company_size" :key="d.value" :label="d.label" :value="d.value" /></el-select>
              </el-form-item></el-col>
              <el-col :span="24"><el-form-item label="详细地址" prop="address"><el-input v-model="form.address" placeholder="请输入详细地址" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="需求与评分" name="requirement">
            <el-row>
              <el-col :span="12"><el-form-item label="线索来源" prop="leadSource">
                <el-select v-model="form.leadSource" placeholder="请选择" style="width: 100%"><el-option v-for="d in marketing_customer_source" :key="d.value" :label="d.label" :value="d.value" /></el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="线索评分" prop="leadScore"><el-input-number v-model="form.leadScore" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="线索等级" prop="leadGrade">
                <el-select v-model="form.leadGrade" placeholder="请选择" style="width: 100%"><el-option v-for="d in marketing_lead_grade" :key="d.value" :label="d.label" :value="d.value" /></el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="线索状态" prop="leadStatus">
                <el-select v-model="form.leadStatus" placeholder="请选择" style="width: 100%"><el-option v-for="d in marketing_lead_status" :key="d.value" :label="d.label" :value="d.value" /></el-select>
              </el-form-item></el-col>
              <el-col :span="24"><el-form-item label="需求描述" prop="requirementDesc"><el-input v-model="form.requirementDesc" type="textarea" :rows="3" placeholder="请输入需求描述" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="负责信息" name="owner">
            <el-row>
              <el-col :span="12"><el-form-item label="负责人" prop="userId">
                <el-select v-model="form.userId" filterable clearable placeholder="请选择负责人" style="width: 100%" @change="onUserChange">
                  <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="所属部门" prop="deptId">
                <el-tree-select v-model="form.deptId" :data="deptOptions" :props="{ value: 'id', label: 'label', children: 'children' }" value-key="id" placeholder="请选择部门" check-strictly clearable style="width: 100%" />
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="是否公海" prop="isPublic">
                <el-radio-group v-model="form.isPublic"><el-radio value="0">否</el-radio><el-radio value="1">是</el-radio></el-radio-group>
              </el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="其他信息" name="other">
            <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 分配弹窗 -->
    <el-dialog v-model="assignOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">分配线索</span>
        </div>
      </template>
      <el-form label-width="80px">
        <el-form-item label="负责人">
          <el-select v-model="assignUserId" filterable clearable placeholder="请选择负责人" style="width: 100%">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmAssign">确 定</el-button>
        <el-button @click="assignOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 批量分配弹窗 -->
    <el-dialog v-model="batchAssignOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">批量分配线索</span>
        </div>
      </template>
      <el-alert :title="'已选中 ' + ids.length + ' 条线索'" type="info" :closable="false" class="mb8" />
      <el-form label-width="80px">
        <el-form-item label="负责人">
          <el-select v-model="batchAssignUserId" filterable clearable placeholder="请选择负责人" style="width: 100%">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmBatchAssign">确 定</el-button>
        <el-button @click="batchAssignOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 批量变更状态弹窗 -->
    <el-dialog v-model="batchStatusOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg></div>
          <span class="rd-detail-header-title">批量变更线索状态</span>
        </div>
      </template>
      <el-alert :title="'已选中 ' + ids.length + ' 条线索'" type="info" :closable="false" class="mb8" />
      <el-form label-width="80px">
        <el-form-item label="线索状态">
          <el-select v-model="batchStatusValue" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in marketing_lead_status" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmBatchStatus">确 定</el-button>
        <el-button @click="batchStatusOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 转化确认弹窗 -->
    <el-dialog v-model="convertOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg></div>
          <span class="rd-detail-header-title">线索转化</span>
        </div>
      </template>
      <el-alert type="info" :closable="false" class="mb8">
        <template #title>
          确认将线索"<b>{{ convertForm.companyName }}</b>"转化为客户？<br/>
          系统将自动创建客户档案，并根据线索联系人信息自动创建客户联系人。
        </template>
      </el-alert>
      <el-descriptions :column="2" border size="small" class="mb8">
        <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ convertForm.companyName }}</div></div>
        <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ convertForm.contactName }}</div></div>
        <div class="rd-item"><span class="rd-label">手机号</span><div class="rd-value">{{ convertForm.contactPhone }}</div></div>
        <div class="rd-item"><span class="rd-label">线索来源</span><div class="rd-value"><dict-tag :options="marketing_customer_source" :value="convertForm.leadSource" /></div></div>
      </el-descriptions>
      <template #footer>
        <el-button type="success" @click="confirmConvert">确认转化</el-button>
        <el-button @click="convertOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 无效标记弹窗 -->
    <el-dialog v-model="invalidateOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg></div>
          <span class="rd-detail-header-title">标记线索无效</span>
        </div>
      </template>
      <el-form ref="invalidateRef" :model="invalidateForm" label-width="80px">
        <el-form-item label="无效原因" prop="ineffectiveReason">
          <el-select v-model="invalidateForm.ineffectiveReason" placeholder="请选择无效原因" style="width: 100%">
            <el-option label="电话空号/停机" value="phone_invalid" />
            <el-option label="客户明确拒绝" value="rejected" />
            <el-option label="需求不匹配" value="no_match" />
            <el-option label="重复线索" value="duplicate" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="无效说明" prop="ineffectiveRemark">
          <el-input v-model="invalidateForm.ineffectiveRemark" type="textarea" :rows="3" placeholder="请输入无效说明（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="confirmInvalidate">确认标记无效</el-button>
        <el-button @click="invalidateOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 快速跟进弹窗 -->
    <el-dialog v-model="followOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></div>
          <span class="rd-detail-header-title">快速跟进</span>
        </div>
      </template>
      <el-form ref="followRef" :model="followForm" :rules="followRules" label-width="100px">
        <el-descriptions :column="2" border size="small" class="mb8">
          <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ followForm.companyName }}</div></div>
          <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ followForm.contactName }}</div></div>
        </div>
        <el-form-item label="互动类型" prop="interactType">
          <el-select v-model="followForm.interactType" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in marketing_interaction_type" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="互动时间" prop="interactTime">
          <el-date-picker v-model="followForm.interactTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="跟进内容" prop="content">
          <el-input v-model="followForm.content" type="textarea" :rows="3" placeholder="请输入跟进内容" />
        </el-form-item>
        <el-form-item label="下次跟进">
          <el-date-picker v-model="followForm.nextTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间（可选）" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmFollowUp">确 定</el-button>
        <el-button @click="followOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 导入弹窗 -->
    <el-dialog v-model="importOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg></div>
          <span class="rd-detail-header-title">线索导入</span>
        </div>
      </template>
      <el-upload ref="uploadRef" :limit="1" accept=".xlsx, .xls" :auto-upload="false" :action="importUrl" :headers="headers" :data="{ updateSupport: importUpdateSupport }" :on-success="handleImportSuccess" :on-error="handleImportError" drag>
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <span>仅允许导入xls、xlsx格式文件。</span>
            <el-button type="primary" link @click="handleDownloadTemplate">下载模板</el-button>
          </div>
        </template>
      </el-upload>
      <div class="mt16">
        <el-checkbox v-model="importUpdateSupport">如果已存在相同企业名称，则更新数据</el-checkbox>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitImport">确 定</el-button>
        <el-button @click="importOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 重复线索弹窗 -->
    <el-dialog v-model="dupOpen" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg></div>
          <span class="rd-detail-header-title">疑似重复线索</span>
        </div>
      </template>
      <el-alert v-if="dupList.length > 0" title="系统检测到以下线索可能与当前输入重复，请确认是否继续" type="warning" :closable="false" class="mb8" />
      <el-table :data="dupList" border size="small">
        <el-table-column label="线索编号" prop="leadNo" width="150" />
        <el-table-column label="企业名称" prop="companyName" show-overflow-tooltip />
        <el-table-column label="联系人" prop="contactName" width="100" />
        <el-table-column label="手机号" prop="contactPhone" width="130" />
        <el-table-column label="状态" prop="leadStatus" width="80" align="center">
          <template #default="scope"><dict-tag :options="marketing_lead_status" :value="scope.row.leadStatus" /></template>
        </el-table-column>
      </el-table>
      <el-empty v-if="dupList.length === 0" description="未检测到重复线索" />
      <template #footer>
        <el-button @click="dupOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkLead">
import { useRouter } from 'vue-router'
import { UploadFilled } from '@element-plus/icons-vue'
import { listLead, getLead, addLead, updateLead, delLead, convertLead, assignLead, releaseLeadToPool, batchAssignLead, batchUpdateLeadStatus, invalidateLead, updateFollowTime, checkLeadDuplicate } from '@/api/mk/lead'
import { addInteraction } from '@/api/mk/interaction'
import { listUser, deptTreeSelect } from '@/api/system/user'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard([])
import { getToken } from '@/utils/auth'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_lead_index')
const { marketing_lead_status, marketing_lead_grade, marketing_customer_source, marketing_industry, marketing_company_size, marketing_interaction_type } = proxy.useDict('marketing_lead_status', 'marketing_lead_grade', 'marketing_customer_source', 'marketing_industry', 'marketing_company_size', 'marketing_interaction_type')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const userOptions = ref([])
const deptOptions = ref([])
const activeNames = ref(['lead', 'company', 'requirement', 'owner', 'other'])

// 分配相关
const assignOpen = ref(false)
const assignUserId = ref(null)
const assignLeadId = ref(null)

// 批量分配相关
const batchAssignOpen = ref(false)
const batchAssignUserId = ref(null)

// 批量变更状态相关
const batchStatusOpen = ref(false)
const batchStatusValue = ref(null)

// 转化相关
const convertOpen = ref(false)
const convertForm = ref({})

// 无效标记
const invalidateOpen = ref(false)
const invalidateForm = ref({})
const invalidateLeadId = ref(null)

// 快速跟进
const followOpen = ref(false)
const followForm = ref({})
const followRules = {
  interactType: [{ required: true, message: '请选择互动类型', trigger: 'change' }],
  interactTime: [{ required: true, message: '请选择互动时间', trigger: 'change' }],
  content: [{ required: true, message: '请输入跟进内容', trigger: 'blur' }]
}

// 导入相关
const importOpen = ref(false)
const importUpdateSupport = ref(false)
const importUrl = ref(import.meta.env.VITE_APP_BASE_API + '/mk/lead/importData')
const headers = ref({ Authorization: 'Bearer ' + getToken() })

// 查重
const dupOpen = ref(false)
const dupList = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, leadNo: undefined, companyName: undefined, leadStatus: undefined, leadGrade: undefined, leadSource: undefined, isPublic: '0' },
  rules: {
    companyName: [{ required: true, message: '企业名称不能为空', trigger: 'blur' }],
    contactName: [{ required: true, message: '联系人不能为空', trigger: 'blur' }],
    contactPhone: [{ required: true, message: '手机号不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listLead(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function getUserOptions() { listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) }) }
function getDeptTree() { deptTreeSelect().then(res => { deptOptions.value = res.data }) }
function onUserChange(userId) {
  if (userId) { const user = userOptions.value.find(u => u.userId === userId); if (user) { form.value.userName = user.nickName; if (user.deptId) { form.value.deptId = user.deptId; form.value.deptName = user.dept ? user.dept.deptName : undefined } } }
  else { form.value.userName = undefined }
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.leadId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = { leadNo: undefined, companyName: undefined, contactName: undefined, contactPhone: undefined, contactEmail: undefined, position: undefined, industry: undefined, companySize: undefined, address: undefined, leadSource: undefined, activityId: undefined, requirementDesc: undefined, leadScore: 0, leadGrade: 'D', leadStatus: '0', isPublic: '1', userId: undefined, userName: undefined, deptId: undefined, deptName: undefined, remark: undefined }
  proxy.resetForm('leadRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增线索' }
function handleUpdate(row) { reset(); getLead(row.leadId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改线索' }) }
function handleView(row) { router.push('/mk/lead-detail/' + row.leadId) }
function submitForm() {
  proxy.$refs['leadRef'].validate(valid => {
    if (valid) {
      if (form.value.leadId != undefined) { updateLead(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addLead(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const leadIds = row.leadId || ids.value; proxy.$modal.confirm('确认删除编号为"' + leadIds + '"的数据？').then(() => delLead(leadIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('mk/lead/export', { ...queryParams.value }, `lead_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// ====== 更多操作下拉菜单 ======
function handleCommand(cmd, row) {
  if (cmd === 'convert') handleConvert(row)
  else if (cmd === 'assign') handleAssignSingle(row)
  else if (cmd === 'release') handleRelease(row)
  else if (cmd === 'invalidate') handleInvalidate(row)
  else if (cmd === 'delete') handleDelete(row)
}

// 转化
function handleConvert(row) {
  convertForm.value = { ...row }
  convertOpen.value = true
}
function confirmConvert() {
  convertLead(convertForm.value.leadId, {}).then(res => {
    proxy.$modal.msgSuccess('转化成功，已自动创建客户和联系人')
    convertOpen.value = false
    getList()
    // 跳转到新创建的客户详情页
    if (res.convertCustomerId) {
      router.push('/mk/customer-detail/' + res.convertCustomerId)
    }
  })
}

// 分配
function handleAssignSingle(row) {
  assignLeadId.value = row.leadId
  assignUserId.value = row.userId || null
  assignOpen.value = true
}
function confirmAssign() {
  const data = { userId: assignUserId.value }
  const user = userOptions.value.find(u => u.userId === assignUserId.value)
  if (user) {
    data.deptId = user.deptId
    data.userName = user.nickName
    if (user.dept) data.deptName = user.dept.deptName
  }
  assignLead(assignLeadId.value, data).then(() => {
    proxy.$modal.msgSuccess('分配成功')
    assignOpen.value = false
    getList()
  })
}

// 退回公海
function handleRelease(row) {
  proxy.$modal.confirm('确认将"' + row.companyName + '"退回公海？').then(() => {
    releaseLeadToPool(row.leadId).then(() => {
      proxy.$modal.msgSuccess('已退回公海')
      getList()
    })
  }).catch(() => {})
}

// 标记无效
function handleInvalidate(row) {
  invalidateLeadId.value = row.leadId
  invalidateForm.value = { ineffectiveReason: undefined, ineffectiveRemark: undefined }
  invalidateOpen.value = true
}
function confirmInvalidate() {
  if (!invalidateForm.value.ineffectiveReason) {
    proxy.$modal.msgWarning('请选择无效原因')
    return
  }
  invalidateLead(invalidateLeadId.value, invalidateForm.value).then(() => {
    proxy.$modal.msgSuccess('已标记为无效')
    invalidateOpen.value = false
    getList()
  })
}

// 快速跟进
function handleFollowUp(row) {
  followForm.value = {
    leadId: row.leadId,
    companyName: row.companyName,
    contactName: row.contactName,
    interactType: undefined,
    interactTime: undefined,
    content: undefined,
    userId: row.userId,
    userName: row.userName,
    nextTime: undefined
  }
  followOpen.value = true
}
function confirmFollowUp() {
  proxy.$refs['followRef'].validate(valid => {
    if (valid) {
      addInteraction(followForm.value).then(() => {
        proxy.$modal.msgSuccess('跟进记录添加成功')
        // 更新线索最后跟进时间
        updateFollowTime(followForm.value.leadId).then(() => {})
        followOpen.value = false
        getList()
      })
    }
  })
}

// 批量分配
function handleBatchAssign() {
  batchAssignUserId.value = null
  batchAssignOpen.value = true
}
function confirmBatchAssign() {
  if (!batchAssignUserId.value) {
    proxy.$modal.msgWarning('请选择负责人')
    return
  }
  const user = userOptions.value.find(u => u.userId === batchAssignUserId.value)
  const data = {
    leadIds: ids.value,
    userId: batchAssignUserId.value,
    deptId: user ? user.deptId : undefined,
    userName: user ? user.nickName : undefined,
    deptName: user && user.dept ? user.dept.deptName : undefined
  }
  batchAssignLead(data).then(() => {
    proxy.$modal.msgSuccess('批量分配成功')
    batchAssignOpen.value = false
    getList()
  })
}

// 批量变更状态
function handleBatchStatus() {
  batchStatusValue.value = null
  batchStatusOpen.value = true
}
function confirmBatchStatus() {
  if (!batchStatusValue.value) {
    proxy.$modal.msgWarning('请选择线索状态')
    return
  }
  batchUpdateLeadStatus({ leadIds: ids.value, leadStatus: batchStatusValue.value }).then(() => {
    proxy.$modal.msgSuccess('批量变更状态成功')
    batchStatusOpen.value = false
    getList()
  })
}

// 导入
function handleImport() { importOpen.value = true }
function handleDownloadTemplate() { proxy.download('mk/lead/importTemplate', {}, `lead_template_${new Date().getTime()}.xlsx`) }
function submitImport() { proxy.$refs['uploadRef'].submit() }
function handleImportSuccess(res) {
  if (res.code === 200) {
    proxy.$modal.msgSuccess(res.msg)
    importOpen.value = false
    getList()
  } else {
    proxy.$modal.msgError(res.msg)
  }
}
function handleImportError() { proxy.$modal.msgError('导入失败') }

// 查重
function onCompanyBlur() { if (form.value.companyName && !form.value.leadId) doCheckDup() }
function onPhoneBlur() { if (form.value.contactPhone && !form.value.leadId) doCheckDup() }
function doCheckDup() {
  checkLeadDuplicate(form.value.companyName, form.value.contactPhone).then(res => {
    if (res.data && res.data.length > 0) {
      dupList.value = res.data
      dupOpen.value = true
    }
  })
}

getUserOptions()
getDeptTree()
getList()
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.ml5 { margin-left: 5px; }
.mt16 { margin-top: 16px; }
.text-center { text-align: center; }
</style>

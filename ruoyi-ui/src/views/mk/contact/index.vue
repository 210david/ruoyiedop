<template>
  <div class="app-container mk-list-page">
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
          <label>姓名</label>
          <div class="control">
            <el-input v-model="queryParams.name" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>手机号</label>
          <div class="control">
            <el-input v-model="queryParams.phone" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>所属客户</label>
          <div class="control">
            <el-input v-model="queryParams.customerName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>职位</label>
          <div class="control">
            <el-input v-model="queryParams.position" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>邮箱</label>
          <div class="control">
            <el-input v-model="queryParams.email" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>关键联系人</label>
          <div class="control is-select">
            <el-select v-model="queryParams.isKey" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option label="是" value="1" />
              <el-option label="否" value="0" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>销售人员</label>
          <div class="control">
            <el-input v-model="queryParams.ownerUserName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
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
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:contact:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" @click="handleImport" v-hasPermi="['marketing:contact:import']">
            <el-icon><Upload /></el-icon> 导入
          </button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:contact:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:contact:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" :disabled="multiple" @click="handleBatchSetKey" v-hasPermi="['marketing:contact:edit']">
            <el-icon><Connection /></el-icon> 批量设为关键
          </button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleAssign" v-hasPermi="['marketing:contact:assign']">
            <el-icon><User /></el-icon> 分配
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:contact:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_contact_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="所属客户" prop="customerName" key="customerName" :width="colWidth('customerName', 180)" resizable show-overflow-tooltip v-if="columns.customerName.visible">
            <template #default="scope">
              <span>{{ scope.row.customerName }}</span>
              <el-badge v-if="isOverdue(scope.row)" is-dot type="danger" class="ml5" />
            </template>
          </el-table-column>
          <el-table-column label="姓名" prop="name" key="name" :width="colWidth('name', 100)" resizable v-if="columns.name.visible">
            <template #default="scope">
              <el-button link type="primary" @click="handleDetail(scope.row)">{{ scope.row.name }}</el-button>
            </template>
          </el-table-column>
          <el-table-column label="性别" prop="gender" key="gender" :width="colWidth('gender', 80)" resizable align="center" v-if="columns.gender.visible">
            <template #default="scope">{{ scope.row.gender === '0' ? '男' : '女' }}</template>
          </el-table-column>
          <el-table-column label="职位" prop="position" key="position" :width="colWidth('position', 120)" resizable v-if="columns.position.visible" />
          <el-table-column label="手机号" prop="phone" key="phone" :width="colWidth('phone', 130)" resizable v-if="columns.phone.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.phone }}</span></template>
          </el-table-column>
          <el-table-column label="邮箱" prop="email" key="email" :width="colWidth('email', 180)" resizable show-overflow-tooltip v-if="columns.email.visible" />
          <el-table-column label="关键联系人" prop="isKey" key="isKey" :width="colWidth('isKey', 100)" resizable align="center" v-if="columns.isKey.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.isKey === '1' ? 'red' : 'gray'">
                <span class="dot"></span>{{ scope.row.isKey === '1' ? '是' : '否' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="主要联系人" prop="isPrimary" key="isPrimary" :width="colWidth('isPrimary', 100)" resizable align="center" v-if="columns.isPrimary.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.isPrimary === '1' ? 'green' : 'gray'">
                <span class="dot"></span>{{ scope.row.isPrimary === '1' ? '是' : '否' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="销售人员" prop="ownerUserName" key="ownerUserName" :width="colWidth('ownerUserName', 100)" resizable v-if="columns.ownerUserName.visible" />
          <el-table-column label="下次联系时间" prop="nextContactTime" key="nextContactTime" :width="colWidth('nextContactTime', 160)" resizable sortable="custom" v-if="columns.nextContactTime.visible">
            <template #default="scope">
              <span :class="{ 'text-danger': isOverdue(scope.row) }">{{ scope.row.nextContactTime }}</span>
            </template>
          </el-table-column>
      <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
        <template #default="scope">
          <div class="action-btn-row">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:contact:edit']">修改</el-button>
          <el-dropdown @command="(cmd) => handleCommand(cmd, scope.row)" trigger="click" v-hasPermi="['marketing:contact:edit']">
            <el-button link type="primary" icon="ArrowDown">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="setPrimary" icon="Star" v-if="scope.row.isPrimary !== '1'">设为主要联系人</el-dropdown-item>
                <el-dropdown-item command="checkDup" icon="CopyDocument" v-hasPermi="['marketing:contact:list']">查重</el-dropdown-item>
                <el-dropdown-item command="delete" icon="Delete" v-hasPermi="['marketing:contact:remove']" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          </div>
            </template>
      </el-table-column>
    </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="contactRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('formBasic')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.formBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.formBasic">
              <el-row>
                <el-col :span="12"><el-form-item label="所属客户" prop="customerId">
                  <el-input v-model="form.customerName" readonly placeholder="请选择客户" style="width: 100%" @click="openCustomerPicker">
                    <template #append><el-button icon="Search" @click="openCustomerPicker" /></template>
                    <template #suffix>
                      <el-icon v-if="form.customerName" class="clear-icon" @click.stop="clearCustomer"><CircleClose /></el-icon>
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="姓名" prop="name"><el-input v-model="form.name" placeholder="请输入姓名" @blur="onNameBlur" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="form.gender"><el-radio value="0">男</el-radio><el-radio value="1">女</el-radio></el-radio-group>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="职位" prop="position"><el-input v-model="form.position" placeholder="请输入职位" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('formContact')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg></span>联系方式</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.formContact }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.formContact">
              <el-row>
                <el-col :span="12"><el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" placeholder="请输入手机号" @blur="onPhoneBlur" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="邮箱" prop="email"><el-input v-model="form.email" placeholder="请输入邮箱" @blur="onEmailBlur" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="微信号" prop="wechat"><el-input v-model="form.wechat" placeholder="请输入微信号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="QQ号" prop="qq"><el-input v-model="form.qq" placeholder="请输入QQ号" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('formRole')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>角色与归属</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.formRole }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.formRole">
              <el-row>
                <el-col :span="12"><el-form-item label="所属部门" prop="department"><el-input v-model="form.department" placeholder="请输入所属部门" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="角色标签" prop="roleTag">
                  <el-select v-model="form.roleTag" multiple placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in marketing_contact_role" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="关键联系人" prop="isKey">
                  <el-radio-group v-model="form.isKey"><el-radio value="0">否</el-radio><el-radio value="1">是</el-radio></el-radio-group>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="主要联系人" prop="isPrimary">
                  <el-radio-group v-model="form.isPrimary"><el-radio value="0">否</el-radio><el-radio value="1">是</el-radio></el-radio-group>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="销售人员" prop="ownerUserId">
                  <el-input v-model="form.ownerUserName" readonly placeholder="请选择销售人员" style="width: 100%" @click="openFormUserPicker">
                    <template #append>
                      <el-button icon="Search" @click="openFormUserPicker" />
                    </template>
                    <template #suffix>
                      <el-icon v-if="form.ownerUserName" class="clear-icon" @click.stop="clearFormUser"><CircleClose /></el-icon>
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="下次联系时间" prop="nextContactTime">
                  <el-date-picker v-model="form.nextContactTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
                </el-form-item></el-col>
                <el-col :span="24"><el-form-item label="个人特点" prop="personalTrait"><el-input v-model="form.personalTrait" type="textarea" :rows="2" placeholder="请输入个人特征" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('formOther')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.formOther }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.formOther">
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

    <!-- 重复联系人弹窗 -->
    <el-dialog v-model="dupOpen" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg></div>
          <span class="rd-detail-header-title">疑似重复联系人</span>
        </div>
      </template>
      <el-alert v-if="dupList.length > 0" title="系统检测到以下联系人可能与当前输入重复，请确认是否继续或选择合并" type="warning" :closable="false" class="mb8" />
      <el-table :data="dupList" border size="small">
        <el-table-column label="姓名" prop="name" width="100" />
        <el-table-column label="手机号" prop="phone" width="130" />
        <el-table-column label="邮箱" prop="email" />
        <el-table-column label="所属客户" prop="customerName" />
        <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleMerge(scope.row)">合并到此</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="dupList.length === 0" description="未检测到重复联系人" />
      <template #footer>
        <el-button @click="dupOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 分配弹窗 -->
    <el-dialog v-model="assignOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">分配联系人</span>
        </div>
      </template>
      <el-form label-width="80px">
        <el-form-item label="销售人员">
          <el-input v-model="assignUserName" readonly placeholder="请选择（留空释放到公海）" style="width: 100%" @click="openAssignUserPicker">
            <template #append>
              <el-button icon="Search" @click="openAssignUserPicker" />
            </template>
            <template #suffix>
              <el-icon v-if="assignUserName" class="clear-icon" @click.stop="clearAssignUser"><CircleClose /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmAssign">确 定</el-button>
        <el-button @click="assignOpen = false">取 消</el-button>
      </template>
    </el-dialog>

        <!-- 导入弹窗 -->
    <el-dialog v-model="importOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg></div>
          <span class="rd-detail-header-title">联系人导入</span>
        </div>
      </template>
      <el-upload ref="uploadRef" :limit="1" accept=".xlsx, .xls" :auto-upload="false" :action="importUrl" :headers="headers" :on-success="handleImportSuccess" :on-error="handleImportError" drag>
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
        <el-checkbox v-model="importUpdateSupport">如果已存在相同手机号/邮箱，则更新数据</el-checkbox>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitImport">确 定</el-button>
        <el-button @click="importOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 客户选择弹窗 -->
    <el-dialog v-model="customerPickerOpen" width="860px" append-to-body draggable class="rd-dialog" @open="onCustomerPickerOpen">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18M3 7l1.5-4h15L21 7M3 7v14M21 7v14M9 21v-7h6v7"/></svg></div>
          <span class="rd-detail-header-title">选择所属客户</span>
        </div>
      </template>
      <div class="customer-picker">
        <div class="customer-picker-search">
          <el-input v-model="customerPickerQuery.customerName" placeholder="企业名称" clearable size="small" style="width: 200px" @keyup.enter="handleCustomerPickerQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-input v-model="customerPickerQuery.creditCode" placeholder="信用代码" clearable size="small" style="width: 200px; margin-left: 8px" @keyup.enter="handleCustomerPickerQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleCustomerPickerQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetCustomerPickerQuery">重置</el-button>
        </div>
        <div class="customer-picker-table">
          <el-table ref="customerTableRef" v-loading="customerPickerLoading" :data="customerPickerList" highlight-current-row @row-click="onCustomerRowClick" @row-dblclick="onCustomerRowDblClick" height="360" size="small">
            <el-table-column width="45" align="center">
              <template #default="{ row }">
                <el-radio :model-value="customerPickerSelectedId" :value="row.customerId" @click.stop="onCustomerRowClick(row)"><span /></el-radio>
              </template>
            </el-table-column>
            <el-table-column label="企业名称" prop="customerName" min-width="180" show-overflow-tooltip />
            <el-table-column label="信用代码" prop="creditCode" width="160" show-overflow-tooltip />
            <el-table-column label="客户等级" width="100" align="center">
              <template #default="scope">{{ levelLabel(scope.row.customerLevel) }}</template>
            </el-table-column>
            <el-table-column label="客户状态" width="100" align="center">
              <template #default="scope">{{ statusLabel(scope.row.customerStatus) }}</template>
            </el-table-column>
          </el-table>
        </div>
        <div class="customer-picker-pager">
          <el-pagination v-model:current-page="customerPickerQuery.pageNum" v-model:page-size="customerPickerQuery.pageSize" :total="customerPickerTotal" layout="total, prev, pager, next" small @current-change="getCustomerPickerList" />
        </div>
      </div>
      <template #footer>
        <el-button @click="customerPickerOpen = false">取 消</el-button>
        <el-button type="primary" @click="confirmCustomerPicker" :disabled="!customerPickerSelectedId">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 负责人选择弹窗 -->
    <user-picker ref="formUserPickerRef" title="选择销售人员" @confirm="onFormUserPickerConfirm" />
    <user-picker ref="assignUserPickerRef" title="选择销售人员" @confirm="onAssignUserPickerConfirm" />

    <!-- 详情弹窗 - Tab页 -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog" @open="loadContactRelations">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          </div>
          <span class="rd-detail-header-title">联系人详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.name">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ viewForm.name }}</span>
            <span v-if="viewForm.isKey === '1'" class="badge red"><span class="dot"></span>关键联系人</span>
            <span v-if="viewForm.isPrimary === '1'" class="badge green"><span class="dot"></span>主要联系人</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <el-tabs v-model="detailTab" @tab-change="onDetailTabChange">
          <el-tab-pane label="基本信息" name="basic">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewBasic')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
                  </span>
                  基本信息
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewBasic }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewBasic">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">所属客户</span><div class="rd-value">{{ viewForm.customerName }}</div></div>
                  <div class="rd-item"><span class="rd-label">姓名</span><div class="rd-value">{{ viewForm.name }}</div></div>
                  <div class="rd-item"><span class="rd-label">性别</span><div class="rd-value">{{ viewForm.gender === '0' ? '男' : '女' }}</div></div>
                  <div class="rd-item"><span class="rd-label">职位</span><div class="rd-value">{{ viewForm.position }}</div></div>
                  <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ viewForm.department }}</div></div>
                  <div class="rd-item"><span class="rd-label">角色标签</span><div class="rd-value"><dict-tag :options="marketing_contact_role" :value="viewForm.roleTag" /></div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewContact')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
                  </span>
                  联系方式
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewContact }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewContact">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">手机号</span><div class="rd-value">{{ viewForm.phone }}</div></div>
                  <div class="rd-item"><span class="rd-label">邮箱</span><div class="rd-value">{{ viewForm.email }}</div></div>
                  <div class="rd-item"><span class="rd-label">微信号</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.wechat }">{{ viewForm.wechat || '暂无' }}</div></div>
                  <div class="rd-item"><span class="rd-label">QQ号</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.qq }">{{ viewForm.qq || '暂无' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewOwner')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  </span>
                  归属与跟进
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewOwner }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewOwner">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">关键联系人</span><div class="rd-value"><span class="badge" :class="viewForm.isKey === '1' ? 'red' : 'gray'"><span class="dot"></span>{{ viewForm.isKey === '1' ? '是' : '否' }}</span></div></div>
                  <div class="rd-item"><span class="rd-label">主要联系人</span><div class="rd-value"><span class="badge" :class="viewForm.isPrimary === '1' ? 'green' : 'gray'"><span class="dot"></span>{{ viewForm.isPrimary === '1' ? '是' : '否' }}</span></div></div>
                  <div class="rd-item"><span class="rd-label">销售人员</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.ownerUserName }">{{ viewForm.ownerUserName || '未分配' }}</div></div>
                  <div class="rd-item"><span class="rd-label">最后联系</span><div class="rd-value">{{ viewForm.lastContactTime }}</div></div>
                  <div class="rd-item"><span class="rd-label">下次联系</span><div class="rd-value">{{ viewForm.nextContactTime }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewOther')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
                  </span>
                  补充信息
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewOther }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewOther">
                <div class="rd-grid">
                  <div class="rd-item rd-item--full"><span class="rd-label">个人特点</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.personalTrait }">{{ viewForm.personalTrait || '暂无' }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无' }}</div></div>
                </div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`互动记录 (${interactionList.length})`" name="interactions">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewInteractions')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg>
                  </span>
                  互动记录
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewInteractions }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewInteractions">
                <div style="margin-bottom: 12px" v-hasPermi="['marketing:interaction:add']">
                  <el-button type="primary" plain icon="Plus" size="small" @click="handleAddInteraction">新增互动</el-button>
                </div>
                <div class="rd-timeline" v-if="interactionList.length > 0">
                  <div class="rd-timeline-item" v-for="item in interactionList" :key="item.recordId">
                    <div class="rd-timeline-dot rd-timeline-dot--success"></div>
                    <div class="rd-timeline-content">
                      <div class="rd-timeline-header">
                        <span class="rd-timeline-title"><el-tag size="small" type="info">{{ getInteractTypeLabel(item.interactType) }}</el-tag> {{ item.userName }}</span>
                        <span class="rd-timeline-time">{{ item.interactTime }}</span>
                      </div>
                      <div class="rd-timeline-comment">{{ item.content }}</div>
                      <div v-if="item.nextTime" style="margin-top: 6px; font-size: 12px; color: #e6a23c">
                        下次跟进: {{ item.nextTime }} {{ item.nextContent ? '- ' + item.nextContent : '' }}
                      </div>
                    </div>
                  </div>
                </div>
                <div class="rd-empty" v-else>
                  <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  <p class="rd-empty-text">暂无互动记录</p>
                </div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`参与活动 (${activityParticipants.length})`" name="activities">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewActivities')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  </span>
                  参与活动
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewActivities }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewActivities">
                <el-table ref="activitiesTableRef" :data="activityParticipants" border size="small" v-if="activityParticipants.length > 0">
                  <el-table-column label="活动名称" prop="activityName" show-overflow-tooltip />
                  <el-table-column label="企业名称" prop="companyName" width="180" show-overflow-tooltip />
                  <el-table-column label="参与状态" prop="participateStatus" width="100" align="center">
                    <template #default="scope"><dict-tag :options="marketing_participate_status" :value="scope.row.participateStatus" /></template>
                  </el-table-column>
                  <el-table-column label="签到时间" prop="signTime" width="160" />
                </el-table>
                <div class="rd-empty" v-else>
                  <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  <p class="rd-empty-text">暂无活动参与记录</p>
                </div>
              </div>
            </section>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增互动弹窗 -->
    <el-dialog v-model="interactionOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></div>
          <span class="rd-detail-header-title">新增互动记录</span>
        </div>
      </template>
      <el-form ref="interactionRef" :model="interactionForm" :rules="interactionRules" label-width="100px">
        <el-form-item label="互动类型" prop="interactType">
          <el-select v-model="interactionForm.interactType" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in marketing_interaction_type" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="互动时间" prop="interactTime">
          <el-date-picker v-model="interactionForm.interactTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="互动内容" prop="content"><el-input v-model="interactionForm.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="下次跟进时间"><el-date-picker v-model="interactionForm.nextTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item>
        <el-form-item label="下次跟进内容"><el-input v-model="interactionForm.nextContent" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitInteraction">确 定</el-button>
        <el-button @click="interactionOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="联系人管理业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务流程图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">新增联系人</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">设为关键/主要</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">分配销售人员</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">跟进记录</el-tag>
          </div>
        </div>

        <h4>二、重点业务规则</h4>
        <div class="highlight-card">
          <p>• <strong>关键联系人：</strong>标记为客户的重要决策人，同客户可多个</p>
          <p>• <strong>主要联系人：</strong>每个客户仅一个主要联系人，设置时自动替换原主要联系人</p>
          <p>• <strong>查重功能：</strong>新增时系统自动检测重复手机号/邮箱，可合并重复记录</p>
          <p>• <strong>分配归属：</strong>可将联系人分配给指定销售人员跟进</p>
          <p>• <strong>跟进提醒：</strong>下次联系时间到期会红标提醒，避免遗漏跟进</p>
          <p>• <strong>批量操作：</strong>支持批量设为关键联系人、批量分配</p>
          <p>• <strong>联系人导入：</strong>支持通过Excel批量导入联系人数据</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkContact">
import { UploadFilled, CircleClose, ArrowRight, ArrowDown, QuestionFilled, Search, Filter, Edit, Delete, Download, User, Upload, Connection } from '@element-plus/icons-vue'
import { listContact, getContact, addContact, updateContact, delContact, checkDuplicate, mergeContacts, setPrimary, batchSetKey, assignContact } from '@/api/mk/contact'
import { listInteraction, addInteraction } from '@/api/mk/interaction'
import { listParticipant } from '@/api/mk/participant'
import { listCustomer } from '@/api/mk/customer'
import { listUser } from '@/api/system/user'
import { getToken } from '@/utils/auth'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import UserPicker from '@/components/UserPicker/index.vue'
const { collapsedCards, toggleCard } = useDetailCard(['formBasic', 'formContact', 'formRole', 'formOther', 'viewBasic', 'viewContact', 'viewOwner', 'viewOther', 'viewInteractions', 'viewActivities'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_contact_index')
const { marketing_contact_role, marketing_interaction_type, marketing_participate_status, marketing_customer_level, marketing_customer_status } = proxy.useDict('marketing_contact_role', 'marketing_interaction_type', 'marketing_participate_status', 'marketing_customer_level', 'marketing_customer_status')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const userOptions = ref([])
const dupOpen = ref(false)
const dupList = ref([])
const currentForm = ref({})
const assignOpen = ref(false)
const assignUserId = ref(null)
const assignUserName = ref(null)
const assignContactId = ref(null)
const importOpen = ref(false)
const importUpdateSupport = ref(false)
const importUrl = ref(import.meta.env.VITE_APP_BASE_API + '/mk/contact/importData')
const headers = ref({ Authorization: 'Bearer ' + getToken() })
const viewOpen = ref(false)
const viewForm = ref({})
const detailTab = ref('basic')
const interactionList = ref([])
const activityParticipants = ref([])
const interactionOpen = ref(false)
const interactionForm = ref({})
const interactionRules = {
  interactType: [{ required: true, message: '请选择互动类型', trigger: 'change' }],
  interactTime: [{ required: true, message: '请选择互动时间', trigger: 'change' }],
  content: [{ required: true, message: '请输入互动内容', trigger: 'blur' }]
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, name: undefined, phone: undefined, customerName: undefined, position: undefined, email: undefined, isKey: undefined, ownerUserName: undefined, params: {} },
  rules: {
    customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
    name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
    phone: [{ required: true, message: '手机号不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  customerName: { label: '所属客户', visible: true },
  name: { label: '姓名', visible: true },
  gender: { label: '性别', visible: true },
  position: { label: '职位', visible: true },
  phone: { label: '手机号', visible: true },
  email: { label: '邮箱', visible: true },
  isKey: { label: '关键联系人', visible: true },
  isPrimary: { label: '主要联系人', visible: true },
  ownerUserName: { label: '销售人员', visible: true },
  nextContactTime: { label: '下次联系时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_contact_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

const columns = ref(loadColumnVisibility())

const activeFilterCount = computed(() => {
  let count = 0
if (queryParams.value.name) count++
if (queryParams.value.phone) count++
if (queryParams.value.customerName) count++
if (queryParams.value.position) count++
if (queryParams.value.email) count++
if (queryParams.value.isKey) count++
if (queryParams.value.ownerUserName) count++
return count
})

function getList() { loading.value = true; listContact(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }).catch(() => { loading.value = false }) }
function getUserOptions() { listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) }) }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() {
queryParams.value.name = undefined; queryParams.value.phone = undefined; queryParams.value.customerName = undefined; queryParams.value.position = undefined; queryParams.value.email = undefined; queryParams.value.isKey = undefined; queryParams.value.ownerUserName = undefined; queryParams.value.params = {}; handleQuery()
}
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.contactId); single.value = selection.length !== 1; multiple.value = !selection.length }
function isOverdue(row) { return row.nextContactTime && new Date(row.nextContactTime) <= new Date(Date.now() + 86400000) }
function reset() {
  form.value = { customerId: undefined, customerName: undefined, name: undefined, gender: '0', position: undefined, department: undefined, roleTag: undefined, phone: undefined, email: undefined, wechat: undefined, qq: undefined, isKey: '0', isPrimary: '0', ownerUserId: undefined, ownerUserName: undefined, nextContactTime: undefined, personalTrait: undefined, remark: undefined }
  proxy.resetForm('contactRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增联系人' }
function handleUpdate(row) { reset(); getContact(row.contactId || ids.value[0]).then(res => { form.value = res.data; if (form.value.roleTag) { form.value.roleTag = form.value.roleTag.split(',') } open.value = true; title.value = '修改联系人' }) }
function handleDetail(row) {
  getContact(row.contactId).then(res => {
    viewForm.value = res.data
    detailTab.value = 'basic'
    Object.keys(collapsedCards).forEach(k => { collapsedCards[k] = false })
    viewOpen.value = true
  })
}
function loadContactRelations() {
  if (!viewForm.value.contactId) return
  listInteraction({ contactId: viewForm.value.contactId, pageNum: 1, pageSize: 999 }).then(res => {
    interactionList.value = res.rows || []
  })
  listParticipant({ contactPhone: viewForm.value.phone, pageNum: 1, pageSize: 999 }).then(res => {
    activityParticipants.value = (res.rows || []).filter(p => p.contactId == viewForm.value.contactId || p.contactPhone === viewForm.value.phone)
    nextTick(() => {
      if (activitiesTableRef.value) activitiesTableRef.value.doLayout()
    })
  })
}
const activitiesTableRef = ref(null)
function onDetailTabChange(name) {
  if (name === 'activities') {
    nextTick(() => {
      if (activitiesTableRef.value) activitiesTableRef.value.doLayout()
    })
  }
}
function handleAddInteraction() {
  interactionForm.value = {
    contactId: viewForm.value.contactId,
    customerId: viewForm.value.customerId,
    contactName: viewForm.value.name,
    interactTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
    userId: undefined
  }
  interactionOpen.value = true
}
function submitInteraction() {
  proxy.$refs['interactionRef'].validate(valid => {
    if (valid) {
      addInteraction(interactionForm.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        interactionOpen.value = false
        loadContactRelations()
      })
    }
  })
}
function getInteractTypeLabel(type) {
  const item = marketing_interaction_type.value?.find(d => d.value === type)
  return item ? item.label : type
}
function submitForm() {
  proxy.$refs['contactRef'].validate(valid => {
    if (valid) {
      const submitData = { ...form.value }
      if (submitData.roleTag && Array.isArray(submitData.roleTag)) { submitData.roleTag = submitData.roleTag.join(',') }
      if (form.value.contactId != undefined) { updateContact(submitData).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addContact(submitData).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const contactIds = row.contactId || ids.value; proxy.$modal.confirm('确认删除编号为"' + contactIds + '"的数据？').then(() => delContact(contactIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('mk/contact/export', { ...queryParams.value }, `contact_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// 操作下拉菜单
function handleCommand(cmd, row) {
  if (cmd === 'setPrimary') handleSetPrimary(row)
  else if (cmd === 'checkDup') handleCheckDup(row)
  else if (cmd === 'delete') handleDelete(row)
}

// P1-7: 设为主要联系人
function handleSetPrimary(row) {
  proxy.$modal.confirm('确认将"' + row.name + '"设为主要联系人？（同客户下仅一个主要联系人）').then(() => setPrimary(row.contactId)).then(() => { proxy.$modal.msgSuccess('设置成功'); getList() }).catch(() => {})
}
// P1-7: 批量设为关键联系人
function handleBatchSetKey() {
  proxy.$modal.confirm('确认将选中的' + ids.value.length + '个联系人设为关键联系人？').then(() => batchSetKey({ contactIds: ids.value, isKey: '1' })).then(() => { proxy.$modal.msgSuccess('设置成功'); getList() }).catch(() => {})
}

// P2-8: 查重
function onPhoneBlur() { if (form.value.phone && !form.value.contactId) doCheckDup() }
function onEmailBlur() { if (form.value.email && !form.value.contactId) doCheckDup() }
function onNameBlur() { /* name blur - no auto check */ }
function doCheckDup() {
  checkDuplicate(form.value.phone, form.value.email).then(res => {
    if (res.data && res.data.length > 0) {
      currentForm.value = { ...form.value }
      dupList.value = res.data
      dupOpen.value = true
    }
  })
}
function handleCheckDup(row) {
  checkDuplicate(row.phone, row.email).then(res => {
    dupList.value = (res.data || []).filter(c => c.contactId !== row.contactId)
    currentForm.value = row
    dupOpen.value = true
  })
}
function handleMerge(slaveRow) {
  const masterId = currentForm.value.contactId
  if (!masterId) { proxy.$modal.msgWarning('新增时无法合并，请先保存'); return }
  proxy.$modal.confirm('确认将"' + slaveRow.name + '"合并到当前联系人？合并后从记录将被删除，其互动/活动记录将转移到主记录。').then(() => {
    mergeContacts({ masterId: masterId, slaveId: slaveRow.contactId }).then(() => {
      proxy.$modal.msgSuccess('合并成功')
      dupOpen.value = false
      getList()
    })
  }).catch(() => {})
}

// P2-10: 分配
function handleAssign() {
  assignContactId.value = ids.value[0]
  assignUserId.value = null
  assignUserName.value = null
  assignOpen.value = true
}
/** 打开表单销售人员选择弹窗 */
function openFormUserPicker() {
  proxy.$refs.formUserPickerRef.open(form.value.ownerUserId)
}
/** 表单销售人员选择确认回调 */
function onFormUserPickerConfirm(user) {
  form.value.ownerUserId = user.userId
  form.value.ownerUserName = user.nickName
}
/** 清除表单销售人员 */
function clearFormUser() {
  form.value.ownerUserId = undefined
  form.value.ownerUserName = undefined
}
/** 打开分配销售人员选择弹窗 */
function openAssignUserPicker() {
  proxy.$refs.assignUserPickerRef.open(assignUserId.value)
}
/** 分配销售人员选择确认回调 */
function onAssignUserPickerConfirm(user) {
  assignUserId.value = user.userId
  assignUserName.value = user.nickName
}
/** 清除分配销售人员 */
function clearAssignUser() {
  assignUserId.value = null
  assignUserName.value = null
}
function confirmAssign() {
  assignContact(assignContactId.value, { ownerUserId: assignUserId.value }).then(() => {
    proxy.$modal.msgSuccess('分配成功')
    assignOpen.value = false
    getList()
  })
}

// P3-13: 导入
function handleImport() { importOpen.value = true }
function handleDownloadTemplate() { proxy.download('mk/contact/importTemplate', {}, `contact_template_${new Date().getTime()}.xlsx`) }
function submitImport() {
  proxy.$refs['uploadRef'].submit()
}
function handleImportSuccess(res) {
  if (res.code === 200) {
    proxy.$modal.msgSuccess(res.msg)
    importOpen.value = false
    getList()
  } else {
    proxy.$modal.msgError(res.msg)
  }
}
function handleImportError() {
  proxy.$modal.msgError('导入失败')
}

getUserOptions()
getList()

// ===== 客户选择弹窗 =====
const customerPickerOpen = ref(false)
const customerPickerLoading = ref(false)
const customerPickerList = ref([])
const customerPickerTotal = ref(0)
const customerPickerSelectedId = ref(null)
const customerPickerSelectedRow = ref(null)
const customerTableRef = ref(null)
const customerPickerQuery = reactive({ pageNum: 1, pageSize: 10, customerName: undefined, creditCode: undefined, customerStatus: '0' })

/** 打开客户选择弹窗 */
function openCustomerPicker() {
  customerPickerOpen.value = true
}
/** 弹窗打开时初始化 */
function onCustomerPickerOpen() {
  customerPickerQuery.customerName = undefined
  customerPickerQuery.creditCode = undefined
  customerPickerQuery.pageNum = 1
  customerPickerSelectedId.value = form.value.customerId || null
  customerPickerSelectedRow.value = null
  getCustomerPickerList()
}
/** 加载客户列表 */
function getCustomerPickerList() {
  customerPickerLoading.value = true
  listCustomer(customerPickerQuery).then(res => {
    customerPickerList.value = res.rows
    customerPickerTotal.value = res.total
    customerPickerLoading.value = false
    if (customerPickerSelectedId.value) {
      nextTick(() => {
        if (customerTableRef.value) {
          customerTableRef.value.setCurrentRow(customerPickerList.value.find(r => r.customerId === customerPickerSelectedId.value))
        }
      })
    }
  }).catch(() => { customerPickerLoading.value = false })
}
/** 弹窗查询 */
function handleCustomerPickerQuery() {
  customerPickerQuery.pageNum = 1
  getCustomerPickerList()
}
/** 弹窗重置 */
function resetCustomerPickerQuery() {
  customerPickerQuery.customerName = undefined
  customerPickerQuery.creditCode = undefined
  handleCustomerPickerQuery()
}
/** 行点击选中 */
function onCustomerRowClick(row) {
  customerPickerSelectedId.value = row.customerId
  customerPickerSelectedRow.value = row
}
/** 行双击确认 */
function onCustomerRowDblClick(row) {
  onCustomerRowClick(row)
  confirmCustomerPicker()
}
/** 确认选择 */
function confirmCustomerPicker() {
  if (!customerPickerSelectedRow.value) {
    proxy.$modal.msgWarning('请选择一个客户')
    return
  }
  form.value.customerId = customerPickerSelectedRow.value.customerId
  form.value.customerName = customerPickerSelectedRow.value.customerName
  customerPickerOpen.value = false
}
/** 清除所属客户 */
function clearCustomer() {
  form.value.customerId = undefined
  form.value.customerName = undefined
}
/** 客户等级标签 */
function levelLabel(level) {
  const item = marketing_customer_level.value?.find(d => d.value == level)
  return item ? item.label : '-'
}
/** 客户状态标签 */
function statusLabel(status) {
  const item = marketing_customer_status.value?.find(d => d.value == status)
  return item ? item.label : '-'
}

const showStatusHelp = ref(false)
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.ml5 { margin-left: 5px; }
.mt16 { margin-top: 16px; }
.text-danger { color: #f56c6c; }
.text-center { text-align: center; }
.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
.clear-icon:hover {
  color: #909399;
}
:deep(.el-input.is-disabled .el-input__inner) {
  cursor: pointer;
}
/* 客户选择弹窗 */
.customer-picker { display: flex; flex-direction: column; }
.customer-picker-search { display: flex; align-items: center; margin-bottom: 12px; flex-wrap: wrap; gap: 4px; }
.customer-picker-table { border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; }
.customer-picker-table :deep(.el-table__row) { cursor: pointer; }
.customer-picker-table :deep(.el-table__row:hover > td) { background: #f0f7ff; }
.customer-picker-table :deep(.el-table__row.is-current > td) { background: #e6f0fd; }
.customer-picker-pager { margin-top: 8px; display: flex; justify-content: flex-end; }

.status-help-content {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 10px;
}
.status-help-content h4 {
  margin: 20px 0 12px 0;
  color: #303133;
  font-weight: 600;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}
.status-help-content h4:first-child {
  margin-top: 0;
}
.status-help-content .status-flow {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 8px;
}
.status-help-content .flow-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.status-help-content .flow-arrow {
  color: #909399;
  font-size: 16px;
}
.status-help-content .highlight-card {
  background-color: #ecf5ff;
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid #409eff;
}
.status-help-content .highlight-card p {
  margin: 6px 0;
  line-height: 1.6;
  font-size: 13px;
  color: #606266;
}
</style>

<!-- 非scoped样式：详情弹窗 append-to-body 后脱离 .mk-list-page 前缀，补充 rd-dialog 内徽章与 tabs 样式 -->
<style>
/* 状态徽章：弹窗 append-to-body 后脱离 mk-list-page 前缀，补充全局样式 */
.rd-dialog .badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
  border: 1px solid transparent;
}
.rd-dialog .badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.rd-dialog .badge.amber { background: #fffbeb; color: #b45309; border-color: #fde68a; }
.rd-dialog .badge.amber .dot { background: #f59e0b; }
.rd-dialog .badge.blue { background: #eff6ff; color: #1d4ed8; border-color: #bfdbfe; }
.rd-dialog .badge.blue .dot { background: #3b82f6; }
.rd-dialog .badge.green { background: #ecfdf5; color: #047857; border-color: #a7f3d0; }
.rd-dialog .badge.green .dot { background: #10b981; }
.rd-dialog .badge.red { background: #fef2f2; color: #b91c1c; border-color: #fecaca; }
.rd-dialog .badge.red .dot { background: #ef4444; }
.rd-dialog .badge.gray { background: #f1f5f9; color: #64748b; border-color: #e2e8f0; }
.rd-dialog .badge.gray .dot { background: #94a3b8; }
/* 详情弹窗内 tabs 下的表格宽度塌陷修复：切换 tab 时让表格重算布局 */
.rd-dialog .el-tabs__content {
  width: 100%;
}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>

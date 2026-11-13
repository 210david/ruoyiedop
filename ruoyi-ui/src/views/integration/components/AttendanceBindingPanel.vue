<template>
  <div class="app-container integration-attendance-page">
    <!-- 筛选条件卡片 -->
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
          <label>{{ connectorLabel }}姓名</label>
          <div class="control">
            <el-input v-model="queryParams.externalName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>手机号</label>
          <div class="control">
            <el-input v-model="queryParams.mobile" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>{{ externalNoLabel }}</label>
          <div class="control">
            <el-input v-model="queryParams.externalJobNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>平台工号</label>
          <div class="control">
            <el-input v-model="queryParams.employeeNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>匹配状态</label>
          <div class="control is-select">
            <el-select v-model="matchedFilter" placeholder="全部" clearable @change="handleQuery">
              <el-option label="已匹配" value="yes" />
              <el-option label="未匹配" value="no" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>匹配方式</label>
          <div class="control is-select">
            <el-select v-model="queryParams.matchType" placeholder="全部" clearable @change="handleQuery">
              <el-option label="自动·手机号" value="auto_mobile" />
              <el-option label="自动·工号" value="auto_jobNumber" />
              <el-option label="人工绑定" value="manual" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>最后同步时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- 绑定关系列表卡片 -->
    <div class="surface">
      <!-- 匹配状态页签 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button type="button" class="status-tab" :class="{ 'is-active': !matchedFilter }" @click="handleStatusTabClick(undefined)">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button type="button" class="status-tab tab-matched" :class="{ 'is-active': matchedFilter === 'yes' }" @click="handleStatusTabClick('yes')">
            <span class="dot"></span><span>已匹配</span><span class="count">{{ statusCounts.matched }}</span>
          </button>
          <button type="button" class="status-tab tab-unmatched" :class="{ 'is-active': matchedFilter === 'no' }" @click="handleStatusTabClick('no')">
            <span class="dot"></span><span>未匹配</span><span class="count">{{ statusCounts.unmatched }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="helpOpen = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Refresh" :loading="syncingContacts" @click="handleSyncContacts" v-hasPermi="['integration:attendance:sync']">同步通讯录</el-button>
          <button type="button" class="btn-soft is-outline" @click="openSyncDialog" v-hasPermi="['integration:attendance:sync']">
            <el-icon><Calendar /></el-icon> 打卡同步
          </button>
          <template v-if="configVisible">
            <div class="toolbar-divider"></div>
            <button type="button" class="btn-soft is-outline" @click="openConfig">
              <el-icon><Setting /></el-icon> 集成配置
            </button>
            <span class="badge" :class="configForm.mockEnabled ? 'amber' : (configForm.enabled ? 'green' : 'red')" style="margin-left:4px"><span class="dot"></span>{{ configForm.mockEnabled ? '模拟模式' : (configForm.enabled ? '已启用' : '未启用') }}</span>
          </template>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" :storageKey="columnStorageKey"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="bindingList" border class="app-table" :row-class-name="rowClassName" @header-dragend="onHeaderDragEnd">
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" align="center" resizable />
          <el-table-column :label="connectorLabel + '姓名'" prop="externalName" key="externalName" :min-width="colWidth('externalName', 140)" align="center" show-overflow-tooltip resizable v-if="columns.externalName.visible" />
          <el-table-column :label="connectorLabel + '手机号'" prop="mobile" key="mobile" :width="colWidth('mobile', 170)" align="center" resizable v-if="columns.mobile.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.mobile || '-' }}</span></template>
          </el-table-column>
          <el-table-column :label="externalNoLabel" prop="externalJobNo" key="externalJobNo" :width="colWidth('externalJobNo', 150)" align="center" resizable v-if="columns.externalJobNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.externalJobNo || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="平台工号" prop="employeeNo" key="employeeNo" :width="colWidth('employeeNo', 130)" align="center" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="平台员工" prop="employeeName" key="employeeName" :min-width="colWidth('employeeName', 130)" align="center" show-overflow-tooltip resizable v-if="columns.employeeName.visible">
            <template #default="scope">{{ scope.row.employeeName || '-' }}</template>
          </el-table-column>
          <el-table-column label="匹配方式" prop="matchType" key="matchType" :width="colWidth('matchType', 170)" align="center" resizable v-if="columns.matchType.visible">
            <template #default="scope">
              <span class="badge" :class="matchBadgeClass(scope.row)"><span class="dot"></span>{{ matchLabel(scope.row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最后同步时间" prop="syncTime" key="syncTime" :width="colWidth('syncTime', 170)" align="center" show-overflow-tooltip resizable v-if="columns.syncTime.visible" />
          <el-table-column label="操作" align="center" width="180" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="row-actions">
                <el-button v-if="!scope.row.employeeId" link type="primary" @click="handleBind(scope.row)" v-hasPermi="['integration:attendance:sync']">绑定</el-button>
                <template v-else>
                  <el-button link type="primary" @click="handleBind(scope.row)" v-hasPermi="['integration:attendance:sync']">重新绑定</el-button>
                  <el-button link type="danger" @click="handleUnbind(scope.row)" v-hasPermi="['integration:attendance:sync']">解绑</el-button>
                </template>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 打卡同步弹窗 -->
    <el-dialog v-model="syncOpen" :title="connectorLabel + '打卡同步'" width="480px" append-to-body draggable class="rd-dialog">
      <el-form ref="syncRef" :model="syncForm" :rules="syncRules" label-width="90px">
        <el-form-item label="同步范围" prop="dateRange">
          <el-date-picker v-model="syncForm.dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-alert type="info" :closable="false" show-icon>
          <p>单次范围不超过 {{ syncMaxSpanDays }} 天；将拉取范围内已匹配{{ connectorLabel }}用户的打卡结果，按平台班次规则重新计算考勤状态，已存在的记录会被覆盖更新。</p>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="syncOpen = false">取 消</el-button>
        <el-button type="primary" :loading="syncingAttendance" @click="submitSync">开始同步</el-button>
      </template>
    </el-dialog>

    <!-- 人工绑定弹窗 -->
    <el-dialog v-model="bindOpen" width="540px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ bindForm.originalEmployeeNo ? '变更绑定' : '人工绑定' }}</span>
        </div>
      </template>
      <div class="bind-dialog-body">
        <div class="bind-source-card">
          <div class="bind-source-title">{{ connectorLabel }}用户（数据来源）</div>
          <div class="bind-source-body">
            <div class="bind-kv"><span class="k">姓名</span><span class="v">{{ bindForm.externalName || '-' }}</span></div>
            <div class="bind-kv"><span class="k">手机号</span><span class="v col-mono">{{ bindForm.mobile || '-' }}</span></div>
            <div class="bind-kv"><span class="k">{{ externalNoLabel }}</span><span class="v col-mono">{{ bindForm.externalJobNo || '-' }}</span></div>
          </div>
        </div>
        <el-form ref="bindRef" :model="bindForm" :rules="bindRules" label-width="100px">
          <el-form-item label="平台员工" prop="employeeNo">
            <el-input :model-value="bindEmployeeLabel" readonly placeholder="点击选择平台员工" @click="openBindEmployeePicker">
              <template #append>
                <el-button icon="Search" @click.stop="openBindEmployeePicker" />
              </template>
              <template #suffix>
                <el-icon v-if="bindForm.employeeNo" class="clear-icon" @click.stop="clearBindEmployee"><CircleClose /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item v-if="bindForm.employeeNo && (bindForm.orgName || bindForm.positionName)" label="部门岗位">
            <div class="bind-meta">
              <span class="bind-meta-item">{{ bindForm.orgName || '-' }}</span>
              <span class="bind-meta-divider"></span>
              <span class="bind-meta-item">{{ bindForm.positionName || '-' }}</span>
            </div>
          </el-form-item>
          <el-form-item v-if="occupant" label=" ">
            <div class="bind-occupied">
              <div class="bind-occupied-title">
                <el-icon class="bind-occupied-icon"><WarningFilled /></el-icon>
                该员工已被{{ connectorLabel }}用户「{{ occupant.externalName }}」绑定<template v-if="occupant.mobile">（{{ occupant.mobile }}）</template>
              </div>
              <div class="bind-occupied-desc">一个员工只能绑定一个{{ connectorLabel }}用户，否则打卡数据会互相覆盖。请先在列表中解绑该{{ connectorLabel }}用户，或改选其他员工。</div>
            </div>
          </el-form-item>
        </el-form>
        <div class="bind-tip">
          <template v-if="bindForm.originalEmployeeNo">变更后该{{ connectorLabel }}用户的打卡将改为写入新选员工的考勤记录；原员工已同步的历史记录不会删除。一个员工只能绑定一个{{ connectorLabel }}用户。</template>
          <template v-else>绑定后该{{ connectorLabel }}用户的打卡数据将写入所选员工的考勤记录。一个员工只能绑定一个{{ connectorLabel }}用户。</template>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" :loading="binding" :disabled="!bindForm.employeeNo || !!occupant || checkingOccupant" @click="submitBind">绑 定</el-button>
        <el-button @click="bindOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 平台员工选择器 -->
    <employee-picker ref="employeePickerRef" title="选择平台员工" @confirm="onBindEmployeeConfirm" />

    <!-- 集成配置弹窗（钉钉 / 企业微信） -->
    <el-dialog v-model="configOpen" width="806px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/></svg></div>
          <span class="rd-detail-header-title">{{ connectorLabel }}集成配置</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/></svg></span>应用凭据</div>
          </div>
          <div class="rd-card-body">
            <el-alert :type="configForm.mockEnabled ? 'info' : (configForm.enabled ? 'success' : 'warning')" :closable="false" show-icon style="margin-bottom:18px">
              <template #title>
                <template v-if="configForm.mockEnabled">当前为模拟数据模式：不调用{{ connectorLabel }}接口，同步使用内置模拟数据，仅用于无凭据环境联调验证</template>
                <template v-else>{{ configForm.enabled ? '集成已启用，定时任务与页面同步功能可正常拉取' + connectorLabel + '数据' : '集成未启用，同步操作将被拒绝；填写凭据后开启「启用集成」' }}</template>
              </template>
            </el-alert>
            <el-form label-width="100px">
              <el-form-item :label="accountLabel" :required="!configForm.mockEnabled">
                <el-input v-model="configForm.appKey" :placeholder="accountPlaceholder" clearable maxlength="64" :disabled="configForm.mockEnabled" />
              </el-form-item>
              <el-form-item :label="secretLabel" :required="!configForm.mockEnabled">
                <el-input v-model="configForm.appSecret" type="password" show-password autocomplete="new-password"
                  :placeholder="configForm.hasSecret ? '已配置，留空则不修改' : '请输入 ' + secretLabel" clearable maxlength="128" :disabled="configForm.mockEnabled" />
              </el-form-item>
              <el-form-item label="启用集成">
                <el-switch v-model="configForm.enabled" active-text="启用" inactive-text="停用" inline-prompt />
              </el-form-item>
              <template v-if="isWecom">
                <el-form-item label="回调Token">
                  <el-input v-model="configForm.callbackToken" placeholder="企业微信后台「接收消息」配置页的 Token" clearable maxlength="64" :disabled="configForm.mockEnabled" />
                </el-form-item>
                <el-form-item label="回调AESKey">
                  <el-input v-model="configForm.callbackAesKey" type="password" show-password autocomplete="new-password"
                    :placeholder="configForm.hasCallbackAesKey ? '已配置，留空则不修改' : '企业微信后台的 EncodingAESKey（43位）'" clearable maxlength="64" :disabled="configForm.mockEnabled" />
                  <div class="config-tip" style="margin-top:4px">
                    <p v-if="configForm.callbackUrl">回调URL（填到企业微信后台「接收消息服务器URL」）：http://服务器IP:端口{{ configForm.callbackUrl }}</p>
                  </div>
                </el-form-item>
              </template>
              <el-form-item label="模拟数据模式">
                <el-switch v-model="configForm.mockEnabled" active-text="开启" inactive-text="关闭" inline-prompt />
                <span style="margin-left:10px;font-size:13px;color:#94a3b8">开启后无需{{ accountLabel }}/{{ secretLabel }}，使用内置模拟通讯录与打卡数据</span>
              </el-form-item>
            </el-form>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></span>同步策略</div>
          </div>
          <div class="rd-card-body">
            <el-form label-width="100px">
              <el-form-item label="匹配策略">
                <el-radio-group v-model="configForm.matchType">
                  <el-radio-button value="mobile">按手机号</el-radio-button>
                  <el-radio-button value="jobNumber">按工号</el-radio-button>
                  <el-radio-button value="auto">自动</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label=" ">
                <div class="config-tip">
                  <p v-if="isWecom">· 默认「按手机号」为手机号优先，未命中时自动用工号兜底（工号取自企业微信「别名 alias」或「账号 userid」）。</p>
                  <p v-else>· 匹配策略决定通讯录同步时{{ connectorLabel }}用户如何关联平台员工：自动 = 工号优先、手机号兜底。</p>
                  <p v-if="isWecom">· 企业微信 2022-06-20 起新建自建应用调用成员详情接口不再返回手机号，如自动匹配大面积未命中，请在成员「别名」中维护工号，或改用「按工号」策略。</p>
                  <p>· 切换策略只影响尚未匹配的绑定，已匹配记录不会被重跑。</p>
                  <p>· {{ secretLabel }} 保存后不再回显；配置实时生效，无需重启。</p>
                </div>
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="primary" :loading="savingConfig" @click="handleSaveConfig">确 定</el-button>
        <el-button @click="configOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 业务操作说明 -->
    <el-dialog v-model="helpOpen" :title="connectorLabel + '集成业务操作说明'" width="760px" append-to-body class="rd-dialog status-help-dialog">
      <div class="status-help-content">
        <h4>一、功能定位</h4>
        <p>本页用于将{{ connectorLabel }}的<b>考勤打卡数据</b>同步到平台人力资源考勤模块，并在{{ connectorLabel }}用户与平台员工之间建立对应关系。同步取{{ connectorLabel }}的<b>原始打卡时间</b>与<b>上下班类型</b>，迟到、早退、缺卡等判定由平台按自身班次规则计算，避免两套口径冲突。</p>

        <h4>二、使用步骤</h4>
        <div class="help-steps">
          <div class="help-step"><span class="help-step-no">1</span><div><b>配置集成</b>：点击「集成配置」，填写{{ accountLabel }} / {{ secretLabel }}，并打开「启用集成」。</div></div>
          <div class="help-step"><span class="help-step-no">2</span><div><b>同步通讯录</b>：点击「同步通讯录」拉取{{ connectorLabel }}组织人员，系统按匹配策略自动关联平台员工。未匹配人员以橙色高亮显示。</div></div>
          <div class="help-step"><span class="help-step-no">3</span><div><b>人工绑定</b>：自动匹配失败的记录，点该行右侧的「绑定」按钮，通过人员选择弹框选定平台员工完成关联；已绑定的记录可随时「重新绑定」或「解绑」。</div></div>
          <div class="help-step"><span class="help-step-no">4</span><div><b>打卡同步</b>：点击「打卡同步」选择日期范围拉取打卡数据，结果写入考勤记录，来源标记为「{{ connectorLabel }}同步」。</div></div>
        </div>

        <h4>三、匹配策略说明</h4>
        <template v-if="isWecom">
          <p><b>按手机号</b>（默认）：{{ connectorLabel }}手机号 = 员工档案手机号；未命中时自动用<b>工号兜底</b>（工号取自成员「别名 alias」或「账号 userid」）。<br/>
          <b>按工号</b>：先按别名 / userid 对员工工号匹配，未命中再按手机号兜底。<br/>
          <b>自动</b>：同「按手机号」，手机号优先、工号兜底。<br/>
          注：企业微信 2022-06-20 起新建自建应用调用成员详情接口不再返回手机号，若自动匹配大面积未命中，请在成员「别名」中维护工号或改用「按工号」。</p>
        </template>
        <template v-else>
          <p><b>按手机号</b>：钉钉手机号 = 员工档案手机号（默认，要求最低）。<br/>
          <b>按工号</b>：钉钉工号 = 平台员工工号（要求钉钉通讯录已维护工号）。<br/>
          <b>自动</b>：先按工号匹配，未命中再按手机号兜底。<br/>
          策略切换只影响尚未匹配的记录，已绑定记录不会被覆盖；每条记录的匹配方式可在列表中查看。</p>
        </template>

        <h4>四、注意事项</h4>
        <p>· 单次打卡同步日期范围不超过 {{ syncMaxSpanDays }} 天；已存在的考勤记录会被覆盖更新（以{{ connectorLabel }}数据为准）。<br/>
        · 定时任务「{{ connectorLabel }}通讯录同步」「{{ connectorLabel }}考勤同步」默认暂停，需在系统监控-定时任务中启用后才会自动运行（{{ isWecom ? '建议每日凌晨 01:20 / 01:40' : '建议每日凌晨 01:10 / 01:30' }}）。<br/>
        · {{ secretLabel }} 保存后不再回显，如需更换直接填写新值即可。<br/>
        · 解绑或变更绑定只解除对应关系，不会删除已写入 HR 的考勤记录；解绑后该{{ connectorLabel }}用户在下次同步时按「未匹配」跳过。<br/>
        · <b>一个员工只能绑定一个{{ connectorLabel }}用户</b>：所选员工若已被其他{{ connectorLabel }}用户占用，系统会拦截并提示占用方，需先解绑原绑定；自动匹配同样遵守该约束，会跳过已被占用的员工。<br/>
        · {{ isWecom ? '企业微信打卡接口单次最多 100 人、时间跨度 30 天，系统会自动分批分段拉取；「外出打卡」不参与上下班归位，不会被当成正常上下班卡。' : '钉钉打卡接口单次最多 50 人、时间跨度 7 天，系统会自动分批分段拉取。' }}</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="helpOpen = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="IntegrationAttendancePanel">
import { listIntegrationBinding, syncIntegrationContacts, syncIntegrationAttendance, bindIntegrationUser, unbindIntegrationUser, checkEmployeeOccupant, getIntegrationConfig, saveIntegrationConfig, getBindingStats } from '@/api/integration/attendance'
import { Calendar, Setting, ArrowDown, WarningFilled, Search, Filter, CircleClose } from '@element-plus/icons-vue'
import EmployeePicker from '@/components/EmployeePicker'
import { useColumnResize } from '@/composables/useColumnResize'
import { useColumnDrag } from '@/composables/useColumnDrag'
import auth from '@/plugins/auth'

const { proxy } = getCurrentInstance()

/** ===== 连接器（由宿主页面固定传入：钉钉页面 dingtalk / 企业微信页面 wecom）===== */
const props = defineProps({
  connectorType: { type: String, default: 'dingtalk' }
})
/** 当前连接器：dingtalk / wecom */
const connectorType = computed(() => (props.connectorType === 'wecom' ? 'wecom' : 'dingtalk'))
const isWecom = computed(() => connectorType.value === 'wecom')
/** 连接器中文名，用于页面文案 */
const connectorLabel = computed(() => (isWecom.value ? '企业微信' : '钉钉'))
/** 外部平台"工号"字段名称：钉钉有工号字段，企业微信无工号字段，用「别名」承载工号 */
const externalNoLabel = computed(() => (isWecom.value ? '企业微信别名' : '钉钉工号'))
/** 凭据字段名称：钉钉 AppKey/AppSecret，企业微信 CorpId/Secret */
const accountLabel = computed(() => (isWecom.value ? 'CorpId' : 'AppKey'))
const secretLabel = computed(() => (isWecom.value ? 'Secret' : 'AppSecret'))
const accountPlaceholder = computed(() => (isWecom.value ? '企业微信 CorpId（我的企业 → 企业信息）' : '钉钉开放平台应用 AppKey'))
/** 单次打卡同步可跨天数（钉钉系统侧限制 92 天；企业微信接口限制 30 天，系统按 365 天分段拉取） */
const syncMaxSpanDays = computed(() => (isWecom.value ? 365 : 92))

/** 列显隐默认配置（label 随连接器变化） */
function buildDefaultColumns() {
  return {
    externalName: { label: connectorLabel.value + '姓名', visible: true },
    mobile: { label: connectorLabel.value + '手机号', visible: true },
    externalJobNo: { label: externalNoLabel.value, visible: true },
    employeeNo: { label: '平台工号', visible: true },
    employeeName: { label: '平台员工', visible: true },
    matchType: { label: '匹配方式', visible: true },
    syncTime: { label: '最后同步时间', visible: true }
  }
}

/** 列宽拖拽持久化（localStorage + 后端 sys_table_config 双写，个人 > 全局 > 默认）
 *  钉钉沿用原有 tableKey，保留用户已保存的列宽配置；企业微信独立 key，两侧互不影响 */
const tableKey = isWecom.value ? 'integration_wecom_index' : 'integration_attendance_index'
const columnStorageKey = isWecom.value ? 'integration_wecom_columns' : 'integration_attendance_columns'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize(tableKey)
/** 列顺序拖拽持久化（拖表头文字换位，不影响原生列宽拖拽） */
const { initSortable } = useColumnDrag(tableKey, ['externalName', 'mobile', 'externalJobNo', 'employeeNo', 'employeeName', 'matchType', 'syncTime'], tableRef)

/** 读取本地缓存的列显隐状态（label 按当前连接器实时生成） */
function loadColumnVisibility() {
  const defaults = buildDefaultColumns()
  try {
    const saved = localStorage.getItem(columnStorageKey)
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaults).forEach(key => {
        result[key] = { label: defaults[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaults[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaults }
}
const columns = ref(loadColumnVisibility())
/** 列显隐变化后重建列顺序拖拽实例，避免 th 索引错位 */
watch(columns, () => nextTick(initSortable), { deep: true })

const bindingList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const helpOpen = ref(false)
const syncingContacts = ref(false)
const syncingAttendance = ref(false)
const binding = ref(false)
const syncOpen = ref(false)
const bindOpen = ref(false)
/** 所选员工已被其他钉钉用户占用时的占用者信息（一一绑定约束） */
const occupant = ref(null)
const checkingOccupant = ref(false)
const total = ref(0)
const matchedFilter = ref(undefined)
/** 匹配状态页签计数 */
const statusCounts = ref({ all: 0, matched: 0, unmatched: 0 })
const savingConfig = ref(false)
const configVisible = auth.hasPermi('integration:attendance:config')
const configOpen = ref(false)
const configForm = reactive({
  appKey: '',
  appSecret: '',
  enabled: false,
  matchType: 'mobile',
  hasSecret: false,
  mockEnabled: false,
  callbackToken: '',
  callbackAesKey: '',
  hasCallbackAesKey: false,
  callbackUrl: ''
})

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, externalName: undefined, mobile: undefined, externalJobNo: undefined, employeeNo: undefined, matchType: undefined, params: {} },
  syncForm: { dateRange: [] },
  bindForm: { bindingId: undefined, externalName: '', mobile: '', externalJobNo: '', employeeNo: '', employeeName: '', orgName: '', positionName: '', originalEmployeeNo: '' }
})
const { queryParams, syncForm, bindForm } = toRefs(data)

const employeePickerRef = ref(null)

/** 已选员工展示文本（只读输入框，禁止手工输入） */
const bindEmployeeLabel = computed(() => {
  const f = bindForm.value
  if (!f.employeeNo) return ''
  return f.employeeName ? `${f.employeeName}（${f.employeeNo}）` : f.employeeNo
})

const syncRules = {
  dateRange: [{ required: true, message: '请选择同步日期范围', trigger: 'change' }]
}
const bindRules = {
  employeeNo: [{ required: true, message: '请选择平台员工', trigger: 'change' }]
}

/** 未匹配行高亮 */
function rowClassName({ row }) {
  return row.employeeId ? '' : 'row-unmatched'
}

/** 匹配方式徽章 */
function matchLabel(row) {
  if (!row.employeeId) return '未匹配'
  if (row.matchType === 'manual') return '人工绑定'
  if (row.matchType === 'auto_jobNumber') return '自动·工号'
  return '自动·手机号'
}
function matchBadgeClass(row) {
  if (!row.employeeId) return 'red'
  return row.matchType === 'manual' ? 'amber' : 'green'
}

/** 查询绑定列表（按当前连接器） */
function getList() {
  loading.value = true
  const params = { ...queryParams.value, connectorType: connectorType.value }
  if (matchedFilter.value) params.params = { matched: matchedFilter.value }
  listIntegrationBinding(params).then(res => {
    bindingList.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
    loadStatusCounts()
  })
}

/** 加载匹配状态页签计数 */
function loadStatusCounts() {
  getBindingStats(connectorType.value).then(res => {
    statusCounts.value = {
      all: res.all || 0,
      matched: res.matched || 0,
      unmatched: res.unmatched || 0
    }
  }).catch(() => {})
}

/** 匹配状态页签切换（与高级筛选中的「匹配状态」共用同步变量，双向联动） */
function handleStatusTabClick(value) {
  matchedFilter.value = value
  handleQuery()
}

/** 已选筛选条件数量（规范 2.2.4：与 queryParams 初始字段保持一致） */
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.externalName) count++
  if (queryParams.value.mobile) count++
  if (queryParams.value.externalJobNo) count++
  if (queryParams.value.employeeNo) count++
  if (matchedFilter.value) count++
  if (queryParams.value.matchType) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.params = {}
  if (matchedFilter.value) queryParams.value.params.matched = matchedFilter.value
  proxy.addDateRange(queryParams.value, dateRange.value, 'SyncTime')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, externalName: undefined, mobile: undefined, externalJobNo: undefined, employeeNo: undefined, matchType: undefined, params: {} }
  matchedFilter.value = undefined
  dateRange.value = []
  showAdvanced.value = false
  getList()
}

/** 同步通讯录并自动匹配 */
function handleSyncContacts() {
  proxy.$modal.confirm('将拉取' + connectorLabel.value + '通讯录全量用户并按配置策略自动匹配员工档案，是否继续？').then(() => {
    syncingContacts.value = true
    syncIntegrationContacts(connectorType.value).then(res => {
      proxy.$modal.msgSuccess(res.msg || '同步完成')
      getList()
    }).finally(() => {
      syncingContacts.value = false
    })
  }).catch(() => {})
}

/** 打卡同步弹窗 */
function openSyncDialog() {
  syncForm.value.dateRange = []
  syncOpen.value = true
}
function submitSync() {
  proxy.$refs.syncRef.validate(valid => {
    if (!valid) return
    const [beginDate, endDate] = syncForm.value.dateRange
    syncingAttendance.value = true
    syncIntegrationAttendance(beginDate, endDate, connectorType.value).then(res => {
      proxy.$modal.msgSuccess(res.msg || '同步完成')
      syncOpen.value = false
    }).finally(() => {
      syncingAttendance.value = false
    })
  })
}

/** 人工绑定（由列表行「绑定 / 重新绑定」触发，已绑定的记录进入变更绑定） */
function handleBind(row) {
  if (!row || !row.bindingId) return
  const target = row
  bindForm.value = {
    bindingId: target.bindingId,
    externalName: target.externalName,
    mobile: target.mobile,
    externalJobNo: target.externalJobNo,
    employeeNo: target.employeeNo || '',
    employeeName: target.employeeName || '',
    orgName: '',
    positionName: '',
    originalEmployeeNo: target.employeeNo || ''
  }
  occupant.value = null
  bindOpen.value = true
  nextTick(() => proxy.$refs.bindRef && proxy.$refs.bindRef.clearValidate())
}
/** 解绑：解除外部平台用户与平台员工的关联（不影响已同步的考勤记录） */
function handleUnbind(row) {
  const employee = row.employeeName || row.employeeNo || ''
  proxy.$modal.confirm('确认解除「' + row.externalName + '」与平台员工「' + employee + '」的绑定关系？解绑后该' + connectorLabel.value + '用户的打卡不再写入该员工考勤，已同步的历史记录不会删除。').then(() => {
    return unbindIntegrationUser(row.bindingId)
  }).then(res => {
    proxy.$modal.msgSuccess(res.msg || '解绑成功')
    getList()
  }).catch(() => {})
}
/** 打开平台员工选择弹框 */
function openBindEmployeePicker() {
  employeePickerRef.value.open()
}
/** 员工选择确认：仅取工号提交，姓名/部门/岗位用于回显 */
function onBindEmployeeConfirm(emp) {
  const f = bindForm.value
  f.employeeNo = emp.employeeNo
  f.employeeName = emp.employeeName
  f.orgName = emp.orgName
  f.positionName = emp.positionName
  nextTick(() => proxy.$refs.bindRef && proxy.$refs.bindRef.clearValidate('employeeNo'))
  checkEmployeeOccupied()
}
/** 一一绑定校验：所选员工已被其他外部平台用户绑定则拦截提交 */
function checkEmployeeOccupied() {
  const f = bindForm.value
  occupant.value = null
  if (!f.employeeNo) return
  checkingOccupant.value = true
  checkEmployeeOccupant({ employeeNo: f.employeeNo, bindingId: f.bindingId, connectorType: connectorType.value }).then(res => {
    if (res && res.occupied) {
      occupant.value = {
        externalName: res.externalName || '-',
        mobile: res.mobile || '',
        bindingId: res.bindingId
      }
      proxy.$modal.msgWarning('该员工已被' + connectorLabel.value + '用户「' + occupant.value.externalName + '」绑定，不能重复绑定')
    }
  }).finally(() => {
    checkingOccupant.value = false
  })
}
/** 清除已选员工 */
function clearBindEmployee() {
  const f = bindForm.value
  f.employeeNo = ''
  f.employeeName = ''
  f.orgName = ''
  f.positionName = ''
  occupant.value = null
}
function submitBind() {
  proxy.$refs.bindRef.validate(valid => {
    if (!valid) return
    // 变更绑定场景：所选员工与当前绑定一致时提示改选，避免误操作把自动匹配改成人工绑定
    if (bindForm.value.originalEmployeeNo && bindForm.value.originalEmployeeNo === bindForm.value.employeeNo) {
      proxy.$modal.msgWarning('所选员工与当前绑定一致，如需变更请重新选择')
      return
    }
    // 一一绑定兜底：前端已提示占用，此处再拦一次，避免提示延迟时误提交
    if (occupant.value) {
      proxy.$modal.msgWarning('该员工已被' + connectorLabel.value + '用户「' + occupant.value.externalName + '」绑定，请先解绑后再绑定')
      return
    }
    binding.value = true
    bindIntegrationUser({ bindingId: bindForm.value.bindingId, employeeNo: bindForm.value.employeeNo }).then(res => {
      proxy.$modal.msgSuccess(res.msg || '绑定成功')
      bindOpen.value = false
      getList()
    }).finally(() => {
      binding.value = false
    })
  })
}

/** 查询集成配置（凭据字段按连接器解析：钉钉 appKey / 企业微信 corpId） */
function getConfigInfo() {
  getIntegrationConfig(connectorType.value).then(res => {
    configForm.appKey = (isWecom.value ? res.corpId : res.appKey) || ''
    configForm.enabled = res.enabled === true
    configForm.matchType = res.matchType || 'mobile'
    configForm.hasSecret = res.hasSecret === true
    configForm.mockEnabled = res.mockEnabled === true
    configForm.appSecret = ''
    if (isWecom.value) {
      configForm.callbackToken = res.callbackToken || ''
      configForm.hasCallbackAesKey = res.hasCallbackAesKey === true
      configForm.callbackAesKey = ''
      configForm.callbackUrl = res.callbackUrl || ''
    }
  })
}

/** 打开集成配置弹窗（每次打开重新拉取最新配置） */
function openConfig() {
  getConfigInfo()
  configOpen.value = true
}

/** 保存集成配置（按连接器提交对应凭据字段） */
function handleSaveConfig() {
  const accLabel = accountLabel.value
  const secLabel = secretLabel.value
  // 模拟数据模式无需真实凭据，跳过凭据校验
  if (!configForm.mockEnabled && !configForm.appKey) {
    proxy.$modal.msgWarning('请填写 ' + accLabel)
    return
  }
  if (!configForm.mockEnabled && configForm.enabled && !configForm.hasSecret && !configForm.appSecret) {
    proxy.$modal.msgWarning('启用集成前请先填写 ' + secLabel)
    return
  }
  const payload = {
    connectorType: connectorType.value,
    enabled: configForm.enabled ? 'true' : 'false',
    matchType: configForm.matchType,
    mockEnabled: configForm.mockEnabled ? 'true' : 'false'
  }
  if (isWecom.value) {
    payload.corpId = configForm.appKey
    payload.secret = configForm.appSecret
    payload.callbackToken = configForm.callbackToken
    payload.callbackEncodingAesKey = configForm.callbackAesKey
  } else {
    payload.appKey = configForm.appKey
    payload.appSecret = configForm.appSecret
  }
  savingConfig.value = true
  saveIntegrationConfig(payload).then(res => {
    proxy.$modal.msgSuccess(res.msg || '保存成功')
    configForm.appSecret = ''
    configForm.callbackAesKey = ''
    configOpen.value = false
    getConfigInfo()
  }).finally(() => {
    savingConfig.value = false
  })
}

if (configVisible) {
  getConfigInfo()
}
getList()
</script>

<style scoped>
/* ===== 设计令牌（页面级作用域，与 qms-std-page / fin-list-page 同源）===== */
.integration-attendance-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --violet-50:#f5f3ff; --violet-500:#8b5cf6; --violet-700:#6d28d9; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color:var(--ink-900); }

/* ===== 卡片容器（筛选卡与列表卡的分层基础）===== */
.integration-attendance-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== 筛选卡片 ===== */
.integration-attendance-page .filter-card { padding:14px 20px 16px; }
.integration-attendance-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.integration-attendance-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.integration-attendance-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; display:inline-block; }
.integration-attendance-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.integration-attendance-page .filter-card .adv-link:hover { color:var(--brand-600); }
.integration-attendance-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.integration-attendance-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.integration-attendance-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.integration-attendance-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.integration-attendance-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.integration-attendance-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== 字段：label 在上 + 36px 控件容器（规范 2.3）===== */
.integration-attendance-page .field { display:flex; flex-direction:column; gap:6px; }
.integration-attendance-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.integration-attendance-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.integration-attendance-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.integration-attendance-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.integration-attendance-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.integration-attendance-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.integration-attendance-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.integration-attendance-page .field .control :deep(.el-select) { width:100%; }
.integration-attendance-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.integration-attendance-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.integration-attendance-page .field .control :deep(.el-date-editor) { width:100%; }
.integration-attendance-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.integration-attendance-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.integration-attendance-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== 匹配状态页签 + 说明入口（规范 3.1）===== */
.integration-attendance-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.integration-attendance-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.integration-attendance-page .tabs-track::-webkit-scrollbar { display:none; }
.integration-attendance-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.integration-attendance-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.integration-attendance-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.integration-attendance-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.integration-attendance-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.integration-attendance-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.integration-attendance-page .status-tab.is-active .dot { background:var(--brand-500); }
.integration-attendance-page .status-tab.tab-matched .dot { background:var(--green-500); }
.integration-attendance-page .status-tab.tab-matched .count { background:var(--green-50); color:var(--green-700); }
.integration-attendance-page .status-tab.is-active.tab-matched .count { background:var(--green-500); color:#fff; }
.integration-attendance-page .status-tab.tab-unmatched .dot { background:var(--amber-500); }
.integration-attendance-page .status-tab.tab-unmatched .count { background:var(--amber-50); color:var(--amber-700); }
.integration-attendance-page .status-tab.is-active.tab-unmatched .count { background:var(--amber-500); color:#fff; }
.integration-attendance-page .status-tabs .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.integration-attendance-page .status-tabs .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }

/* ===== 工具栏 ===== */
.integration-attendance-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.integration-attendance-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.integration-attendance-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.integration-attendance-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
/* 连接器切换（钉钉 / 企业微信） */

/* ===== 表格 ===== */
.integration-attendance-page .table-wrap { overflow-x:auto; }
.integration-attendance-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.integration-attendance-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.integration-attendance-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
/* 表头不折行：连接器名称较长（如「企业微信手机号」），避免被挤成两行 */
.integration-attendance-page .app-table :deep(.el-table__header th .cell) { white-space:nowrap; }
.integration-attendance-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.integration-attendance-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.integration-attendance-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.integration-attendance-page :deep(.el-table .row-unmatched) { background:#fff7ed; }
.integration-attendance-page :deep(.el-table .row-unmatched:hover > td) { background:#fef3e2 !important; }
.integration-attendance-page .row-actions { display:inline-flex; align-items:center; gap:2px; white-space:nowrap; }
.integration-attendance-page .row-actions :deep(.el-button) { padding:2px 4px; margin:0; }
.col-mono { font-family: 'JetBrains Mono', Consolas, monospace; font-size:13px; }

/* ===== 徽章 ===== */
.integration-attendance-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; white-space:nowrap; }
.integration-attendance-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.integration-attendance-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.integration-attendance-page .badge.green .dot { background:var(--green-500); }
.integration-attendance-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.integration-attendance-page .badge.gray .dot { background:var(--ink-400); }
.integration-attendance-page .badge.violet { background:var(--violet-50); color:var(--violet-700); border-color:#ddd6fe; }
.integration-attendance-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.integration-attendance-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.integration-attendance-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.integration-attendance-page .badge.red .dot { background:var(--red-500); }

/* ===== 分页 ===== */
.integration-attendance-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
.integration-attendance-page :deep(.el-pagination) { justify-content:flex-end; }

/* ===== 工具栏按钮 ===== */
.integration-attendance-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.integration-attendance-page .btn-soft .el-icon { font-size:14px; }
.integration-attendance-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.integration-attendance-page .btn-soft.is-outline:hover:not(:disabled) { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.integration-attendance-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.integration-attendance-page .btn-soft.is-danger-outline:hover:not(:disabled) { background:var(--red-50); border-color:var(--red-500); }
.integration-attendance-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }

/* ===== 响应式 ===== */
@media (max-width:1100px) { .integration-attendance-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .integration-attendance-page .filter-card .filter-bar { grid-template-columns:1fr; } .integration-attendance-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* 配置弹窗内提示文字（弹窗 append-to-body，不能用页面级令牌变量） */
.config-tip { font-size:12px; color:#94a3b8; line-height:2; }
.config-tip p { margin:0; }

/* 人工绑定弹窗 */
.bind-dialog-body { padding: 2px 2px 0; }
.bind-source-card { border:1px solid #e2e8f0; border-radius:10px; background:#f8fafc; padding:12px 14px; margin-bottom:18px; }
.bind-source-title { display:flex; align-items:center; gap:6px; font-size:13px; font-weight:600; color:#334155; margin-bottom:10px; }
.bind-source-title::before { content:''; width:3px; height:12px; border-radius:2px; background:#4f46e5; }
.bind-source-body { display:flex; flex-wrap:wrap; gap:6px 26px; }
.bind-kv { display:flex; align-items:center; gap:8px; font-size:13px; line-height:1.9; }
.bind-kv .k { color:#94a3b8; flex-shrink:0; }
.bind-kv .v { color:#0f172a; font-weight:500; }
.bind-meta { display:flex; align-items:center; gap:10px; font-size:13px; color:#64748b; }
.bind-meta-divider { width:1px; height:12px; background:#dcdfe6; }
.bind-tip { margin-top:2px; padding:10px 12px; background:#eff6ff; border:1px solid #bfdbfe; border-radius:8px; font-size:12px; line-height:1.7; color:#1d4ed8; }
.bind-occupied { padding:10px 12px; background:#fef2f2; border:1px solid #fecaca; border-radius:8px; line-height:1.7; }
.bind-occupied-title { display:flex; align-items:center; gap:6px; font-size:13px; font-weight:600; color:#b91c1c; }
.bind-occupied-icon { font-size:14px; }
.bind-occupied-desc { margin-top:2px; font-size:12px; color:#dc2626; }
.bind-dialog-body :deep(.el-input__suffix) { margin-right: 6px; }
.bind-dialog-body :deep(.clear-icon) { cursor:pointer; color:#c0c4cc; font-size:14px; }
.bind-dialog-body :deep(.clear-icon:hover) { color:#909399; }

/* 业务操作说明弹窗 */
.status-help-content { max-height:520px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 10px; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }
.status-help-content p { margin:0 0 10px; font-size:14px; line-height:1.8; color:#4b5563; }
.help-steps { display:flex; flex-direction:column; gap:10px; margin-bottom:8px; }
.help-step { display:flex; gap:10px; padding:12px 14px; background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; font-size:14px; line-height:1.7; color:#374151; }
.help-step-no { flex-shrink:0; width:22px; height:22px; border-radius:50%; background:linear-gradient(135deg,#6366f1,#4f46e5); color:#fff; font-size:12px; font-weight:700; display:flex; align-items:center; justify-content:center; }
</style>

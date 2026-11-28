<template>
  <div class="app-container flowable-monitor-page">
    <div class="task-layout">
      <!-- ===== 左侧：流程分类树 ===== -->
      <div class="surface tree-card" :class="{ 'is-collapsed': !treeExpanded }">
        <div class="tree-head">
          <template v-if="treeExpanded">
            <div class="filter-title"><span class="glyph"></span> 流程分类</div>
          </template>
          <el-tooltip :content="treeExpanded ? '收起侧栏' : '展开侧栏'" placement="right">
            <el-icon class="tree-toggle" @click="toggleTree"><Fold v-if="treeExpanded" /><Expand v-else /></el-icon>
          </el-tooltip>
        </div>
        <template v-if="treeExpanded">
          <div class="tree-search">
            <el-input v-model="treeKeyword" placeholder="搜索流程类型" clearable>
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
          <div class="tree-body">
            <el-tree
              ref="treeRef"
              :data="treeData"
              :key="treeRenderKey"
              node-key="id"
              :props="{ label: 'label', children: 'children' }"
              :default-expand-all="treeExpanded"
              :filter-node-method="filterNode"
              highlight-current
              :expand-on-click-node="false"
              @node-click="handleNodeClick">
              <template #default="{ data }">
                <span class="tree-node" :class="{ 'is-leaf': !data.children }">
                  <el-icon v-if="data.children" class="tree-icon"><Folder /></el-icon>
                  <el-icon v-else class="tree-icon leaf"><Document /></el-icon>
                  <span class="tree-label">{{ data.label }}</span>
                  <span class="tree-count" :class="{ 'is-zero': !data.count }">{{ data.count || 0 }}</span>
                </span>
              </template>
            </el-tree>
            <div class="tree-empty" v-if="!treeLoading && !treeData.length">暂无流程数据</div>
          </div>
        </template>
      </div>

      <!-- ===== 右侧：流程监控列表 ===== -->
      <div class="task-main">
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
            <div class="field"><div class="control">
                <el-input v-model="queryParams.title" placeholder="流程标题" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><Document /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field"><div class="control">
                <el-input v-model="queryParams.processDefinitionName" placeholder="流程名称" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field"><div class="control">
                <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="创建时间起"
                  end-placeholder="创建时间止" value-format="YYYY-MM-DD" style="width:100%" />
              </div>
            </div>
            <div class="field"><div class="control">
                <el-input v-model="queryParams.currentActivityName" placeholder="当前节点" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><Guide /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field" v-show="showAdvanced"><div class="control">
                <el-input v-model="queryParams.startUserName" placeholder="创建人" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><User /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field" v-show="showAdvanced"><div class="control">
                <el-input v-model="queryParams.processNo" placeholder="流程编号" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><CollectionTag /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field" v-show="showAdvanced"><div class="control">
                <el-input v-model="queryParams.requestId" placeholder="流程请求ID" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><Tickets /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field" v-show="showAdvanced"><div class="control is-select">
                <el-select v-model="queryParams.status" placeholder="当前状态" clearable @change="syncStatusTab">
                  <el-option label="进行中" value="running" />
                  <el-option label="已结束" value="completed" />
                  <el-option label="已撤销" value="canceled" />
                </el-select>
              </div>
            </div>
          </div>
          <div class="filter-actions">
            <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索；查询范围：全部流程实例（管理员视角）</div>
            <div class="filter-buttons">
              <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            </div>
          </div>
        </div>

        <!-- 数据列表 -->
        <div class="surface">
          <div class="status-tabs">
            <div class="tabs-track">
              <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
                <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
              </button>
              <button class="status-tab tab-audit" :class="{ 'is-active': activeStatusTab === 'running' }" @click="handleStatusTabClick('running')">
                <span class="dot"></span><span>进行中</span><span class="count">{{ statusCounts.running }}</span>
              </button>
              <button class="status-tab tab-approved" :class="{ 'is-active': activeStatusTab === 'completed' }" @click="handleStatusTabClick('completed')">
                <span class="dot"></span><span>已结束</span><span class="count">{{ statusCounts.completed }}</span>
              </button>
              <button class="status-tab tab-void" :class="{ 'is-active': activeStatusTab === 'canceled' }" @click="handleStatusTabClick('canceled')">
                <span class="dot"></span><span>已撤销</span><span class="count">{{ statusCounts.canceled }}</span>
              </button>
            </div>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="flowable_monitor_columns" />
          </div>

          <div class="table-wrap">
            <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table" @header-dragend="onHeaderDragEnd">
              <el-table-column type="index" label="序号" :width="colWidth('序号', 85)" align="center" />
              <el-table-column v-if="columns.title.visible" label="流程标题" prop="title" key="title" :min-width="colWidth('title', 210)" show-overflow-tooltip>
                <template #default="scope">
                  <span v-if="urgencyBadge(scope.row.urgency)" class="badge" :class="urgencyBadge(scope.row.urgency).cls">{{ urgencyBadge(scope.row.urgency).text }}</span>
                  <span class="col-strong link-like" role="link" tabindex="0" @click="handleView(scope.row)" @keydown.enter="handleView(scope.row)">{{ scope.row.title || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column v-if="columns.processDefinitionName.visible" label="流程名称" prop="processDefinitionName" key="processDefinitionName" :min-width="colWidth('processDefinitionName', 150)" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.processDefinitionName || '-' }}</template>
              </el-table-column>
              <el-table-column v-if="columns.startUserName.visible" label="创建人" prop="startUserName" key="startUserName" :width="colWidth('startUserName', 110)" align="center" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.startUserName || '-' }}</template>
              </el-table-column>
              <el-table-column v-if="columns.startTime.visible" label="创建时间" prop="startTime" key="startTime" :width="colWidth('startTime', 170)" align="center" />
              <el-table-column v-if="columns.currentActivityName.visible" label="当前节点" prop="currentActivityName" key="currentActivityName" :min-width="colWidth('currentActivityName', 130)" show-overflow-tooltip>
                <template #default="scope">
                  <span class="badge" :class="scope.row.status === 'running' ? 'blue' : 'gray'"><span class="dot"></span>{{ scope.row.currentActivityName || (scope.row.status === 'completed' ? '流程结束' : '已中断') }}</span>
                </template>
              </el-table-column>
              <el-table-column v-if="columns.status.visible" label="状态" prop="status" key="status" :width="colWidth('status', 100)" align="center">
                <template #default="scope">
                  <span class="badge" :class="statusBadge(scope.row)"><span class="dot"></span>{{ statusLabel(scope.row) }}</span>
                </template>
              </el-table-column>
              <el-table-column v-if="columns.processNo.visible" label="流程编号" prop="processNo" key="processNo" :min-width="colWidth('processNo', 150)" show-overflow-tooltip>
                <template #default="scope"><span class="col-mono">{{ scope.row.processNo || '-' }}</span></template>
              </el-table-column>
              <el-table-column v-if="columns.requestId.visible" label="流程请求ID" prop="requestId" key="requestId" :width="colWidth('requestId', 110)" align="center" show-overflow-tooltip>
                <template #default="scope"><span class="col-mono">{{ scope.row.requestId || '-' }}</span></template>
              </el-table-column>
              <el-table-column label="操作" width="220" align="center" fixed="right" class-name="col-action">
                <template #default="scope">
                  <div class="action-btn-row">
                    <el-button v-if="scope.row.status === 'running'" link type="warning" icon="Position"
                      v-hasPermi="['flowable:monitor:intervene']" @click="openIntervene(scope.row)">干预</el-button>
                    <el-button v-if="scope.row.status !== 'running'" link type="success" icon="RefreshRight"
                      v-hasPermi="['flowable:monitor:restart']" @click="confirmRestart(scope.row)">重新发起</el-button>
                    <el-dropdown trigger="click" @command="cmd => handleCommand(cmd, scope.row)">
                      <el-button link type="primary" icon="MoreFilled">更多</el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item v-if="scope.row.status === 'running'" command="recall"
                            v-hasPermi="['flowable:monitor:intervene']">强制收回</el-dropdown-item>
                          <el-dropdown-item v-if="scope.row.status === 'running'" command="assign"
                            v-hasPermi="['flowable:monitor:intervene']">改派办理人</el-dropdown-item>
                          <el-dropdown-item v-if="scope.row.status === 'running'" command="end" divided
                            v-hasPermi="['flowable:monitor:end']">强制结束</el-dropdown-item>
                          <el-dropdown-item v-if="scope.row.status === 'running'" command="cancel"
                            v-hasPermi="['flowable:monitor:end']">撤销流程</el-dropdown-item>
                          <el-dropdown-item v-if="scope.row.status !== 'running'" command="revive" divided
                            v-hasPermi="['flowable:monitor:revive']">流程复活</el-dropdown-item>
                          <el-dropdown-item command="remove" divided
                            v-hasPermi="['flowable:monitor:remove']">删除流程</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="pagination-container">
            <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
              v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" @change="getList" />
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 流程干预 / 流程复活弹窗：跳到指定节点（运行中=干预，已结束=复活） ===== -->
    <el-dialog v-model="intervene.open" width="860px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="21" x2="4" y2="14"/><line x1="4" y1="10" x2="4" y2="3"/><line x1="12" y1="21" x2="12" y2="12"/><line x1="12" y1="8" x2="12" y2="3"/><line x1="20" y1="21" x2="20" y2="16"/><line x1="20" y1="12" x2="20" y2="3"/><line x1="1" y1="14" x2="7" y2="14"/><line x1="9" y1="8" x2="15" y2="8"/><line x1="17" y1="16" x2="23" y2="16"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ isReviveMode ? '流程复活' : '流程干预' }}</span>
          <div class="rd-detail-header-sub" v-if="intervene.title">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ intervene.title }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page monitor-dlg" v-loading="intervene.loading">
        <!-- 流程信息摘要卡片 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>
              流程信息
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">流程标题</span><span class="rd-value">{{ intervene.title || '-' }}</span></div>
              <div class="rd-item"><span class="rd-label">流程名称</span><span class="rd-value">{{ intervene.processName || '-' }}</span></div>
              <div class="rd-item"><span class="rd-label">流程编号</span><span class="rd-value col-mono">{{ intervene.processNo || '-' }}</span></div>
              <div class="rd-item rd-item--full"><span class="rd-label">当前节点</span>
                <span class="rd-value">
                  <span v-if="!intervene.currentNames.length" class="muted">{{ isReviveMode ? '流程已结束（复活后重新生成待办）' : '流程已结束' }}</span>
                  <span v-for="(n, i) in intervene.currentNames" :key="i" class="badge blue"><span class="dot"></span>{{ n }}</span>
                </span>
              </div>
              <div class="rd-item rd-item--full" v-if="intervene.currentTasks.length"><span class="rd-label">当前办理人</span>
                <span class="rd-value">
                  <span v-for="t in intervene.currentTasks" :key="t.taskId" class="task-chip">{{ t.taskName }}：{{ t.assigneeName || '未指派（候选池）' }}</span>
                </span>
              </div>
            </div>
          </div>
        </section>
        <!-- 干预/复活设置卡片 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="4 14 10 14 10 20"/><polyline points="20 10 14 10 14 4"/><line x1="14" y1="10" x2="21" y2="3"/><line x1="3" y1="21" x2="10" y2="14"/></svg></span>
              {{ isReviveMode ? '复活设置' : '干预设置' }}
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <el-form label-width="92px" class="monitor-form">
              <el-form-item label="目标节点" required>
                <el-select v-model="intervene.form.targetActivityId" :placeholder="isReviveMode ? '请选择复活后停留的节点' : '请选择要强制跳转到的节点'" filterable style="width:100%">
                  <el-option v-for="n in intervene.nodes" :key="n.id" :label="n.name" :value="n.id">
                    <span>{{ n.name }}</span>
                    <span class="opt-tags">
                      <span v-if="n.current" class="opt-tag current">当前</span>
                      <span v-if="n.multiInstance" class="opt-tag multi">会签</span>
                    </span>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="指定办理人">
                <el-input :model-value="intervene.form.assigneeName" readonly placeholder="不选则由节点「操作人员」配置自动派发">
                  <template #append>
                    <el-button @click="pickAssignee('intervene')">选择</el-button>
                  </template>
                </el-input>
                <div class="form-tip">仅对普通审批节点生效；会签/串签节点由配置的集合自动派发。</div>
              </el-form-item>
              <el-form-item :label="isReviveMode ? '复活原因' : '干预意见'">
                <el-input v-model="intervene.form.comment" type="textarea" :rows="3"
                  :placeholder="isReviveMode ? '填写复活原因，将写入流程流转意见' : '填写干预原因，将写入流程流转意见'" />
              </el-form-item>
            </el-form>
            <div class="monitor-warn">
              <el-icon><WarningFilled /></el-icon>
              <template v-if="isReviveMode">
                复活会以原单据新建一个运行实例并停在所选节点，业务数据与流程编号不变，原实例历史保留可追溯；被跳过的节点不会生成待办。若流程中有自动执行的服务节点/通知，复活时会再次触发。
              </template>
              <template v-else>
                干预会跳过中间审批节点，被跳过的环节不会再生成待办；操作会记录到流程流转意见与系统操作日志。
              </template>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="intervene.open = false">取 消</el-button>
        <el-button type="warning" :loading="intervene.submitting" @click="submitIntervene">{{ isReviveMode ? '确认复活' : '确认干预' }}</el-button>
      </template>
    </el-dialog>

    <!-- ===== 改派办理人弹窗 ===== -->
    <el-dialog v-model="assign.open" width="860px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg>
          </div>
          <span class="rd-detail-header-title">改派办理人</span>
          <div class="rd-detail-header-sub" v-if="assign.title">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ assign.title }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page monitor-dlg" v-loading="assign.loading">
        <!-- 流程信息摘要卡片 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>
              流程信息
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">流程标题</span><span class="rd-value">{{ assign.title || '-' }}</span></div>
              <div class="rd-item"><span class="rd-label">流程编号</span><span class="rd-value col-mono">{{ assign.processNo || '-' }}</span></div>
              <div class="rd-item"><span class="rd-label">当前环节</span>
                <span class="rd-value">
                  <span v-if="assign.tasks.length <= 1" class="cur-task">{{ assign.tasks[0] ? assign.tasks[0].taskName + '（' + (assign.tasks[0].assigneeName || '未指派（候选池）') + '）' : '-' }}</span>
                  <span v-else class="muted">多个待办任务，请在下方选择</span>
                </span>
              </div>
            </div>
          </div>
        </section>
        <!-- 改派设置卡片 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>
              改派设置
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <el-form label-width="92px" class="monitor-form">
              <el-form-item label="待办任务" required v-if="assign.tasks.length > 1">
                <el-select v-model="assign.taskId" placeholder="请选择要改派的任务" style="width:100%">
                  <el-option v-for="t in assign.tasks" :key="t.taskId" :label="t.taskName + '（' + (t.assigneeName || '未指派') + '）'" :value="t.taskId" />
                </el-select>
              </el-form-item>
              <el-form-item label="新办理人" required>
                <el-input :model-value="assign.targetUserName" readonly placeholder="请选择新的办理人">
                  <template #append>
                    <el-button @click="pickAssignee('assign')">选择</el-button>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="改派说明">
                <el-input v-model="assign.comment" type="textarea" :rows="2" placeholder="填写改派原因（可选）" />
              </el-form-item>
            </el-form>
            <div class="monitor-warn">
              <el-icon><WarningFilled /></el-icon>
              改派后原办理人不再收到该待办，新办理人将立即收到待办任务；操作会记录到系统操作日志。
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="assign.open = false">取 消</el-button>
        <el-button type="primary" :loading="assign.submitting" @click="submitAssign">确认改派</el-button>
      </template>
    </el-dialog>

    <!-- ===== 强制结束 / 撤销流程弹窗 ===== -->
    <el-dialog v-model="endDlg.open" width="860px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg v-if="endDlg.action === 'end'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><rect x="9" y="9" width="6" height="6"/></svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ endDlg.action === 'end' ? '强制结束' : '撤销流程' }}</span>
          <div class="rd-detail-header-sub" v-if="endDlg.title">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ endDlg.title }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page monitor-dlg">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>
              操作说明
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="end-desc">{{ endDlg.action === 'end' ? '强制结束后流程立即终止，所有待办任务将失效，已办理环节保留可追溯。' : '撤销后流程终止且不可恢复，记录为「已撤销」，已办理环节保留可追溯。' }}</div>
            <el-form label-width="92px" class="monitor-form">
              <el-form-item :label="endDlg.action === 'end' ? '结束原因' : '撤销原因'" required>
                <el-input v-model="endDlg.reason" type="textarea" :rows="3" maxlength="200" show-word-limit
                  :placeholder="'请填写' + (endDlg.action === 'end' ? '强制结束' : '撤销') + '原因（将写入流转意见）'" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="endDlg.open = false">取 消</el-button>
        <el-button type="danger" :loading="endDlg.submitting" @click="submitEnd">{{ endDlg.action === 'end' ? '确认强制结束' : '确认撤销' }}</el-button>
      </template>
    </el-dialog>

    <!-- ===== 重新发起弹窗 ===== -->
    <el-dialog v-model="restart.open" width="860px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
          </div>
          <span class="rd-detail-header-title">重新发起</span>
          <div class="rd-detail-header-sub" v-if="restart.title">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ restart.title }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page monitor-dlg">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>
              原流程信息
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">原流程标题</span><span class="rd-value">{{ restart.title || '-' }}</span></div>
              <div class="rd-item"><span class="rd-label">流程编号</span><span class="rd-value col-mono">{{ restart.processNo || '-' }}</span></div>
              <div class="rd-item"><span class="rd-label">发起说明</span><span class="rd-value">以原单据表单数据新建流程</span></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg></span>
              发起设置
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <el-form label-width="92px" class="monitor-form">
              <el-form-item label="新流程标题" required>
                <el-input v-model="restart.newTitle" maxlength="100" show-word-limit placeholder="请输入新流程标题" />
              </el-form-item>
            </el-form>
            <div class="monitor-warn">
              <el-icon><WarningFilled /></el-icon>
              将以原单据的表单数据发起一条新流程（新单、新流程编号），与原流程互不影响。
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="restart.open = false">取 消</el-button>
        <el-button type="primary" :loading="restart.submitting" @click="submitRestart">确认发起</el-button>
      </template>
    </el-dialog>

    <!-- 人员选择（干预指定办理人 / 改派） -->
    <UserPicker ref="userPickerRef" :multiple="false" title="选择办理人" @confirm="onUserPicked" />
  </div>
</template>

<script setup name="FlowMonitor">
import {
  listMonitorProcess, countMonitorProcess, getMonitorNodes,
  interveneProcess, recallMonitorProcess, assignMonitorTask, endMonitorProcess, deleteMonitorProcess,
  reviveMonitorProcess, restartMonitorProcess
} from '@/api/flowable/monitor'
import { getProcessTree } from '@/api/flowable/flow'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useRouter } from 'vue-router'
import { Search, Filter, Document, Guide, User, CollectionTag, ArrowDown, Folder, Fold, Expand, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const router = useRouter()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('flowable_monitor_index')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, running: 0, completed: 0, canceled: 0 })
const dateRange = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1, pageSize: 10, title: undefined, processDefinitionName: undefined,
    startUserName: undefined, currentActivityName: undefined, processNo: undefined, requestId: undefined,
    processDefinitionKey: undefined, status: undefined
  }
})
const { queryParams } = toRefs(data)

/** 紧急程度徽标：1普通（不显示）/ 2紧急 / 3特急 */
function urgencyBadge(v) {
  if (v === '3' || v === 3) return { text: '特急', cls: 'red' }
  if (v === '2' || v === 2) return { text: '紧急', cls: 'amber' }
  return null
}

/** 状态徽标：强制结束与撤销都用中性色，仅文案区分 */
function statusBadge(row) {
  if (row.status === 'running') return 'blue'
  if (row.status === 'completed') return 'green'
  return row.endType === 'forceEnd' ? 'amber' : 'gray'
}
function statusLabel(row) {
  if (row.status === 'running') return '进行中'
  if (row.status === 'completed') return '已结束'
  return row.endType === 'forceEnd' ? '强制结束' : '已撤销'
}

// ===== 左侧流程树 =====
const treeRef = ref(null)
const treeData = ref([])
const treeLoading = ref(false)
const treeKeyword = ref('')
const treeExpanded = ref(true)
const treeRenderKey = ref(0)

watch(treeKeyword, val => { treeRef.value && treeRef.value.filter(val) })

function toggleTree() {
  treeExpanded.value = !treeExpanded.value
  treeRenderKey.value++
}
function filterNode(value, nodeData) {
  if (!value) return true
  return (nodeData.label || '').includes(value)
}
function loadTree() {
  treeLoading.value = true
  // 数量口径与右侧「流程监控」列表一致（含状态页签），避免左右数据不一致
  const status = queryParams.value.status || undefined
  getProcessTree('monitor', status).then(res => {
    const groups = res.data || []
    const totalCount = groups.reduce((s, g) => s + (g.count || 0), 0)
    treeData.value = [{ id: '__all__', label: '全部流程', count: totalCount, children: groups }].filter(g => groups.length || g.children)
    treeLoading.value = false
  }).catch(() => { treeLoading.value = false })
}
function handleNodeClick(node) {
  queryParams.value.processDefinitionKey = node.children || !node.key ? undefined : node.key
  handleQuery()
}

// ===== 列显隐 =====
const defaultColumns = {
  title: { label: '流程标题', visible: true },
  processDefinitionName: { label: '流程名称', visible: true },
  startUserName: { label: '创建人', visible: true },
  startTime: { label: '创建时间', visible: true },
  currentActivityName: { label: '当前节点', visible: true },
  status: { label: '状态', visible: true },
  processNo: { label: '流程编号', visible: true },
  requestId: { label: '流程请求ID', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('flowable_monitor_columns')
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

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.title) count++
  if (queryParams.value.processDefinitionName) count++
  if (queryParams.value.startUserName) count++
  if (queryParams.value.currentActivityName) count++
  if (queryParams.value.processNo) count++
  if (queryParams.value.requestId) count++
  if (queryParams.value.status) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  const params = proxy.addDateRange(queryParams.value, dateRange.value)
  listMonitorProcess(params).then(res => {
    list.value = res.rows || []
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

function loadStatusCounts() {
  countMonitorProcess().then(res => {
    const d = res.data || {}
    statusCounts.value = {
      all: d.all || 0, running: d.running || 0,
      completed: d.completed || 0, canceled: d.canceled || 0
    }
  }).catch(() => { statusCounts.value = { all: 0, running: 0, completed: 0, canceled: 0 } })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.title = undefined
  queryParams.value.processDefinitionName = undefined
  queryParams.value.startUserName = undefined
  queryParams.value.currentActivityName = undefined
  queryParams.value.processNo = undefined
  queryParams.value.requestId = undefined
  queryParams.value.processDefinitionKey = undefined
  queryParams.value.status = undefined
  dateRange.value = []
  activeStatusTab.value = 'all'
  showAdvanced.value = false
  if (treeRef.value) treeRef.value.setCurrentKey(null)
  handleQuery()
}
function handleStatusTabClick(s) {
  activeStatusTab.value = s
  queryParams.value.status = s === 'all' ? undefined : s
  handleQuery()
  loadTree()
}
/** 高级筛选里的状态下拉与页签联动 */
function syncStatusTab() {
  const s = queryParams.value.status
  activeStatusTab.value = ['running', 'completed', 'canceled'].includes(s) ? s : 'all'
  handleQuery()
  loadTree()
}

function refreshAll() { getList(); loadStatusCounts() }

/** 查看流程详情：新标签页打开独立页面 */
function handleView(row) {
  const href = router.resolve({
    path: '/flow/detail/create',
    query: { instanceId: row.instanceId || '', defName: row.processDefinitionName || row.title || '' }
  }).href
  window.open(href, '_blank')
}

// ===== 人员选择（干预指定办理人 / 改派共用） =====
const userPickerRef = ref(null)
const pickTarget = ref('intervene')
function pickAssignee(target) {
  pickTarget.value = target
  if (userPickerRef.value) userPickerRef.value.open()
}
function onUserPicked(users) {
  const user = (Array.isArray(users) ? users : [users]).filter(u => u && u.userId)[0]
  if (!user) return
  const name = user.nickName || user.userName
  if (pickTarget.value === 'intervene') {
    intervene.form.assigneeId = user.userId
    intervene.form.assigneeName = name
  } else {
    assign.targetUserId = user.userId
    assign.targetUserName = name
  }
}

// ===== 流程干预 / 流程复活（同一个弹窗，mode 区分） =====
const intervene = reactive({
  mode: 'intervene',
  open: false, loading: false, submitting: false,
  instanceId: '', title: '', processName: '', processNo: '',
  currentNames: [], currentTasks: [], nodes: [],
  form: { targetActivityId: '', assigneeId: null, assigneeName: '', comment: '' }
})
const isReviveMode = computed(() => intervene.mode === 'revive')

function openIntervene(row, mode = 'intervene') {
  intervene.open = true
  intervene.mode = mode
  intervene.loading = true
  intervene.submitting = false
  intervene.instanceId = row.instanceId
  intervene.title = row.title
  intervene.processName = row.processDefinitionName
  intervene.processNo = row.processNo
  intervene.currentNames = []
  intervene.currentTasks = []
  intervene.nodes = []
  intervene.form = { targetActivityId: '', assigneeId: null, assigneeName: '', comment: '' }
  getMonitorNodes(row.instanceId).then(res => {
    const d = res.data || {}
    intervene.title = d.title || row.title
    intervene.processName = d.processDefinitionName || row.processDefinitionName
    intervene.currentNames = d.currentActivityNames || []
    intervene.currentTasks = d.currentTasks || []
    intervene.nodes = d.nodes || []
    intervene.loading = false
  }).catch(() => { intervene.loading = false })
}

function submitIntervene() {
  const f = intervene.form
  const revive = isReviveMode.value
  if (!f.targetActivityId) {
    proxy.$modal.msgWarning('请选择目标节点')
    return
  }
  const target = intervene.nodes.find(n => n.id === f.targetActivityId)
  const targetName = target ? target.name : f.targetActivityId
  const tip = revive
    ? '确认复活流程「' + intervene.title + '」并停留在「' + targetName + '」？将以原单据新建运行实例，原实例历史保留可追溯。'
    : '确认把流程强制干预到「' + targetName + '」？跳过的中间节点不会再生成待办。'
  proxy.$modal.confirm(tip).then(() => {
    intervene.submitting = true
    const params = {
      targetActivityId: f.targetActivityId,
      assigneeId: f.assigneeId || undefined,
      comment: f.comment || undefined
    }
    return revive ? reviveMonitorProcess(intervene.instanceId, params) : interveneProcess(intervene.instanceId, params)
  }).then(res => {
    const d = (res && res.data) || {}
    if (revive) {
      proxy.$modal.msgSuccess('流程已复活至「' + (d.targetActivityName || targetName) + '」')
    } else {
      proxy.$modal.msgSuccess('流程已干预至「' + targetName + '」')
    }
    intervene.open = false
    refreshAll()
  }).catch(e => {
    if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError((revive ? '复活失败：' : '干预失败：') + (e?.message || e))
  }).finally(() => { intervene.submitting = false })
}

/** 重新发起：复制原单据的表单数据新建一条流程（新单、新流程编号） */
const restart = reactive({
  open: false, submitting: false,
  instanceId: '', title: '', processNo: '', newTitle: ''
})

function confirmRestart(row) {
  restart.open = true
  restart.submitting = false
  restart.instanceId = row.instanceId
  restart.title = row.title
  restart.processNo = row.processNo
  restart.newTitle = (row.title || '流程') + '（重新发起）'
}

function submitRestart() {
  if (!restart.newTitle || !restart.newTitle.trim()) {
    proxy.$modal.msgWarning('请输入新流程标题')
    return
  }
  restart.submitting = true
  restartMonitorProcess(restart.instanceId, restart.newTitle.trim()).then(res => {
    const d = (res && res.data) || {}
    proxy.$modal.msgSuccess('已重新发起：' + (d.title || '') + (d.processNo ? '（编号 ' + d.processNo + '）' : ''))
    restart.open = false
    refreshAll()
  }).catch(e => {
    proxy.$modal.msgError('重新发起失败：' + (e?.message || e))
  }).finally(() => { restart.submitting = false })
}

// ===== 改派办理人 =====
const assign = reactive({
  open: false, loading: false, submitting: false,
  instanceId: '', title: '', processNo: '', tasks: [], taskId: '',
  targetUserId: null, targetUserName: '', comment: ''
})

function openAssign(row) {
  assign.open = true
  assign.loading = true
  assign.submitting = false
  assign.instanceId = row.instanceId
  assign.title = row.title
  assign.processNo = row.processNo
  assign.tasks = []
  assign.taskId = ''
  assign.targetUserId = null
  assign.targetUserName = ''
  assign.comment = ''
  getMonitorNodes(row.instanceId).then(res => {
    const d = res.data || {}
    assign.tasks = d.currentTasks || []
    assign.taskId = assign.tasks.length === 1 ? assign.tasks[0].taskId : ''
    assign.loading = false
  }).catch(() => { assign.loading = false })
}

function submitAssign() {
  if (!assign.taskId) {
    proxy.$modal.msgWarning('请选择待改派的任务')
    return
  }
  if (!assign.targetUserId) {
    proxy.$modal.msgWarning('请选择新的办理人')
    return
  }
  assign.submitting = true
  assignMonitorTask(assign.taskId, assign.targetUserId, assign.comment || undefined).then(() => {
    proxy.$modal.msgSuccess('已改派给「' + assign.targetUserName + '」')
    assign.open = false
    refreshAll()
  }).catch(e => {
    proxy.$modal.msgError('改派失败：' + (e?.message || e))
  }).finally(() => { assign.submitting = false })
}

// ===== 更多操作：强制收回 / 改派 / 强制结束 / 撤销 / 删除 =====
function handleCommand(cmd, row) {
  if (cmd === 'recall') confirmRecall(row)
  else if (cmd === 'assign') openAssign(row)
  else if (cmd === 'end') confirmEnd(row, 'end')
  else if (cmd === 'cancel') confirmEnd(row, 'cancel')
  else if (cmd === 'revive') openIntervene(row, 'revive')
  else if (cmd === 'remove') confirmRemove(row)
}

/** 强制收回：回退到上一节点，由上一节点原办理人重新办理（设计口径：不填意见、流程记录不留痕） */
function confirmRecall(row) {
  const cur = row.currentActivityName ? '「' + row.currentActivityName + '」' : '当前节点'
  proxy.$modal.confirm('确认把流程「' + row.title + '」从' + cur + '强制收回到上一节点？收回后上一节点的原办理人将重新收到待办，当前待办作废；该操作不写流转意见，也不在流程记录中留痕。', '强制收回', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    return recallMonitorProcess(row.instanceId)
  }).then(res => {
    const d = (res && res.data) || {}
    proxy.$modal.msgSuccess('已收回到「' + (d.prevActivityName || '上一节点') + '」'
      + (d.prevAssigneeName ? '，办理人：' + d.prevAssigneeName : ''))
    refreshAll()
  }).catch(e => {
    if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('强制收回失败：' + (e?.message || e))
  })
}

/** 强制结束 / 撤销流程：统一弹窗（action=end/cancel），原因必填写入流转意见 */
const endDlg = reactive({
  open: false, submitting: false,
  action: 'end', instanceId: '', title: '', reason: ''
})

function confirmEnd(row, action) {
  endDlg.open = true
  endDlg.submitting = false
  endDlg.action = action
  endDlg.instanceId = row.instanceId
  endDlg.title = row.title
  endDlg.reason = ''
}

function submitEnd() {
  if (!endDlg.reason || endDlg.reason.trim().length < 2) {
    proxy.$modal.msgWarning('请填写至少 2 个字的原因')
    return
  }
  endDlg.submitting = true
  endMonitorProcess(endDlg.instanceId, endDlg.action, endDlg.reason.trim()).then(() => {
    proxy.$modal.msgSuccess(endDlg.action === 'end' ? '流程已强制结束' : '流程已撤销')
    endDlg.open = false
    refreshAll()
  }).catch(e => {
    proxy.$modal.msgError('操作失败：' + (e?.message || e))
  }).finally(() => { endDlg.submitting = false })
}

function confirmRemove(row) {
  proxy.$modal.confirm('确认删除流程「' + row.title + '」？删除后流程实例与全部审批记录将被彻底清除，不可恢复。').then(() => {
    return deleteMonitorProcess(row.instanceId)
  }).then(() => {
    proxy.$modal.msgSuccess('流程已删除')
    refreshAll()
  }).catch(e => {
    if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('删除失败：' + (e?.message || e))
  })
}

/** 跨标签页刷新：办理页/详情页在其它标签页处理完流程后写入该标记，本页监听到后自动刷新 */
const FLOW_TASK_CHANGED_KEY = 'flow:task:changed'

/** 刷新列表 + 状态页签数量 + 左侧流程树 */
function reloadAll() {
  refreshAll()
  loadTree()
}

/** visibilitychange 与 storage 通知可能同时触发，短时间去重，避免重复请求 */
let lastAutoRefreshAt = 0
function autoRefresh() {
  // 页面不可见时不发请求，切回本页时会由 visibilitychange 再刷新
  if (document.hidden) return
  const now = Date.now()
  if (now - lastAutoRefreshAt < 500) return
  lastAutoRefreshAt = now
  reloadAll()
}

/** 其它标签页写入标记 → 本页自动刷新 */
function onStorageChange(e) {
  if (e.key === FLOW_TASK_CHANGED_KEY) autoRefresh()
}

/** 切回本标签页时自动刷新（兜底：其它页面直接关闭、或推送标记丢失的情况） */
function onVisibilityChange() {
  if (!document.hidden) autoRefresh()
}

/** 监听只在页面激活时绑定，离开（keep-alive 缓存）时解绑，避免在其它页面误触发刷新 */
let autoRefreshBound = false
function bindAutoRefresh() {
  if (autoRefreshBound) return
  autoRefreshBound = true
  window.addEventListener('storage', onStorageChange)
  document.addEventListener('visibilitychange', onVisibilityChange)
}
function unbindAutoRefresh() {
  if (!autoRefreshBound) return
  autoRefreshBound = false
  window.removeEventListener('storage', onStorageChange)
  document.removeEventListener('visibilitychange', onVisibilityChange)
}

getList()
loadStatusCounts()
loadTree()
onActivated(() => { refreshAll(); loadTree(); bindAutoRefresh() })
onMounted(bindAutoRefresh)
onDeactivated(unbindAutoRefresh)
onUnmounted(unbindAutoRefresh)
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

.flowable-monitor-page {
  /* ===== 左树右列表布局（对齐流程查询页） ===== */
  .task-layout {
    display: flex;
    gap: 6px;
    align-items: stretch;
  }
  .task-main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
  }
  .filter-card {
    padding: 12px 16px 14px;
    margin-bottom: 6px;
    .filter-head { margin-bottom: 10px; }
    .filter-bar { gap: 10px 14px; }
    .filter-actions { margin-top: 10px; padding-top: 10px; }
    .field .control { height: 32px; padding: 0 10px; }
    .field .control :deep(.el-input__wrapper) { height: 30px; }
    .field .control :deep(.el-input__inner) { height: 30px; line-height: 30px; }
    .field .control :deep(.el-range-editor.el-input__wrapper) { height: 30px; padding: 0 8px; }
  }
  .table-wrap { padding-top: 6px; }
  .table-wrap + .pagination-container { padding-top: 0; }
  .tree-card {
    width: 236px;
    flex-shrink: 0;
    align-self: flex-start;
    margin-bottom: 6px;
    padding: 12px 10px 10px;
    transition: width .2s ease;
    &.is-collapsed {
      width: 44px;
      padding: 14px 6px 10px;
      .tree-head {
        justify-content: center;
        border-bottom: none;
        margin-bottom: 0;
        padding-bottom: 0;
      }
    }
    .tree-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 6px 10px;
      border-bottom: 1px dashed var(--rd-border, #e5e7eb);
      margin-bottom: 8px;
      .tree-toggle {
        cursor: pointer;
        color: var(--rd-muted, #6b7280);
        &:hover { color: var(--el-color-primary); }
      }
    }
    .tree-search { padding: 0 2px 8px; }
    .tree-body { max-height: calc(100vh - 300px); overflow: auto; }
    .tree-node {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      overflow: hidden;
      .tree-icon { color: var(--el-color-primary); flex-shrink: 0; }
      .tree-icon.leaf { color: #10b981; }
      .tree-label { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
      .tree-count {
        margin-left: auto;
        flex-shrink: 0;
        font-size: 12px;
        line-height: 1;
        padding: 3px 7px;
        border-radius: 9px;
        background: var(--el-color-primary-light-9, #ecf5ff);
        color: var(--el-color-primary);
        &.is-zero { background: #f3f4f6; color: #9ca3af; }
      }
    }
    .tree-empty {
      text-align: center;
      color: var(--rd-muted, #9ca3af);
      font-size: 13px;
      padding: 24px 0;
    }
    :deep(.el-tree-node__content) { height: 32px; border-radius: 8px; }
  }
}

/* ===== 干预 / 改派 / 结束撤销 / 重新发起弹窗（append-to-body 挂 body 下，样式作用于本组件渲染节点） ===== */
.monitor-dlg {
  .badge {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    padding: 3px 9px;
    border-radius: 999px;
    font-size: 13px;
    font-weight: 600;
    line-height: 1;
    border: 1px solid transparent;
    &.blue { background: #eff6ff; color: #1d4ed8; border-color: #bfdbfe; }
    .dot { width: 6px; height: 6px; border-radius: 50%; background: #3b82f6; }
  }
  .muted { color: #9ca3af; }
  .cur-task { font-size: 13px; color: #374151; }
  .end-desc {
    font-size: 13px;
    line-height: 1.7;
    color: #4b5563;
    background: #f8fafc;
    border: 1px solid #eef2f7;
    border-radius: 8px;
    padding: 10px 14px;
    margin-bottom: 14px;
  }
  .task-chip {
    background: #fff;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
    padding: 1px 8px;
    font-size: 12px;
    color: #4b5563;
  }
  .col-mono { font-family: 'JetBrains Mono', Consolas, monospace; font-size: 13px; }
}
.monitor-form {
  .form-tip { font-size: 12px; color: #9ca3af; line-height: 1.6; }
}
.monitor-warn {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 12px;
  line-height: 1.7;
  color: #b45309;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 8px;
  padding: 8px 12px;
  .el-icon { margin-top: 2px; flex-shrink: 0; }
}
.opt-tags {
  float: right;
  .opt-tag {
    margin-left: 6px;
    font-size: 11px;
    padding: 1px 6px;
    border-radius: 8px;
    background: #f3f4f6;
    color: #6b7280;
    &.current { background: #eff6ff; color: #2563eb; }
    &.multi { background: #fef3c7; color: #b45309; }
  }
}
</style>

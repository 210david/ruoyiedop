<template>
  <div class="app-container fin-config-page">
      <!-- ========== 期末结账 ========== -->
      <div v-if="tab === 'closing'">
        <div class="surface filter-card" v-show="showSearch">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span>筛选条件</div>
          </div>
          <div class="filter-bar">
            <div class="field">
              <label>会计期间</label>
              <div class="control">
                <el-input v-model="closingQuery.periodName" placeholder="如 2026年09期" clearable @keyup.enter="handleClosingQuery" />
              </div>
            </div>
            <div class="field">
              <label>年月</label>
              <div class="control">
                <el-input v-model="closingQuery.yearMonth" placeholder="如 2026-09" clearable @keyup.enter="handleClosingQuery" />
              </div>
            </div>
            <div class="field">
              <label>结账状态</label>
              <div class="control">
                <el-select v-model="closingQuery.status" placeholder="全部" clearable style="width: 100%" @change="handleClosingQuery">
                  <el-option label="未结账" value="OPEN" />
                  <el-option label="已结账" value="CLOSED" />
                </el-select>
              </div>
            </div>
            <div class="field">
              <label>结账人</label>
              <div class="control">
                <el-input v-model="closingQuery.closeBy" placeholder="输入结账人" clearable @keyup.enter="handleClosingQuery" />
              </div>
            </div>
          </div>
          <div class="filter-actions">
            <div class="filter-info">
              <el-icon><Filter /></el-icon> 已选 {{ closingActiveCount }} 个条件，支持回车快速搜索
            </div>
            <div class="filter-buttons">
              <el-button icon="RefreshLeft" @click="resetClosingQuery">重置</el-button>
              <el-button type="primary" icon="Search" @click="handleClosingQuery">搜索</el-button>
            </div>
          </div>
          <el-alert class="cfg-tip" type="warning" :closable="false" show-icon>
            <template #title>
              <span class="hint-text">
                结账流程：<b>前置检查 → 结转损益 → 生成账簿 → 封期</b>。
                结账后该期间不可再新增或修改凭证，需先反结账。
              </span>
            </template>
          </el-alert>
        </div>

        <div class="surface">
          <div class="toolbar">
            <div class="toolbar-left">
              <el-button icon="Refresh" @click="getPeriods">刷新</el-button>
              <el-button type="primary" icon="CircleCheck" :disabled="!selectedPeriod" @click="handleCheck">
                结账预检
              </el-button>
              <el-button
                v-hasPermi="['fin:config:edit']"
                type="success" icon="Lock" :disabled="!selectedPeriod || selectedPeriod.status !== 'OPEN'"
                @click="handleClose">
                执行结账
              </el-button>
              <el-button
                v-hasPermi="['fin:config:periodUnlock']"
                type="warning" icon="Unlock" :disabled="!selectedPeriod || selectedPeriod.status !== 'CLOSED'"
                @click="handleUnlock">
                反结账
              </el-button>
              </div>
            <div class="toolbar-right">
              <right-toolbar v-model:showSearch="showSearch" :columns="closingColumns" storageKey="fin_config_closing_columns" @queryTable="getPeriods" />
              <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
            </div>
          </div>

          <div class="table-wrap">
            <el-table ref="closingTableRef" v-loading="loading" :data="filteredPeriodList" border highlight-current
                      @current-change="handleCurrentChange" @header-dragend="closingResize.onHeaderDragEnd">
              <el-table-column type="index" label="序号" width="85" align="center" fixed="left" />
              <el-table-column key="periodName" prop="periodName" label="会计期间" :width="colWidth('periodName', 140)" resizable align="center" v-if="closingColumns.periodName.visible" />
              <el-table-column key="yearMonth" prop="yearMonth" label="年月" :width="colWidth('yearMonth', 100)" resizable align="center" v-if="closingColumns.yearMonth.visible" />
              <el-table-column key="status" prop="status" label="结账状态" :width="colWidth('status', 100)" resizable align="center" v-if="closingColumns.status.visible">
                <template #default="scope">
                  <span class="badge" :class="closingStatusBadge(scope.row.status)">
                    <span class="dot"></span>{{ scope.row.status === 'CLOSED' ? '已结账' : '未结账' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column key="closeBy" prop="closeBy" label="结账人" :width="colWidth('closeBy', 110)" resizable align="center" v-if="closingColumns.closeBy.visible">
                <template #default="scope">{{ scope.row.closeBy || '—' }}</template>
              </el-table-column>
              <el-table-column key="closeTime" prop="closeTime" label="结账时间" :width="colWidth('closeTime', 170)" resizable align="center" v-if="closingColumns.closeTime.visible">
                <template #default="scope">{{ scope.row.closeTime || '—' }}</template>
              </el-table-column>
              <el-table-column key="unlockBy" prop="unlockBy" label="最近反结账" :width="colWidth('unlockBy', 130)" resizable align="center" v-if="closingColumns.unlockBy.visible">
                <template #default="scope">
                  <span v-if="scope.row.unlockBy" class="text-warn">{{ scope.row.unlockBy }}</span>
                  <span v-else class="text-muted">—</span>
                </template>
              </el-table-column>
              <el-table-column key="unlockReason" prop="unlockReason" label="反结账原因" :min-width="colWidth('unlockReason', 180)" resizable align="center" show-overflow-tooltip v-if="closingColumns.unlockReason.visible">
                <template #default="scope">{{ scope.row.unlockReason || '—' }}</template>
              </el-table-column>
              <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
                <template #default="scope">
                  <div class="action-btn-row">
                    <el-button link type="primary" icon="View" v-hasPermi="['fin:config:query']" @click="showRecords(scope.row)">检查记录</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        </div>

      <!-- ========== 凭证规则 ========== -->
      <div v-if="tab === 'rule'">
        <div class="surface filter-card" v-show="showSearch">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span>筛选条件</div>
            <a class="adv-link" :class="{ 'is-open': ruleShowAdvanced }" @click.prevent="ruleShowAdvanced = !ruleShowAdvanced">
              <span>{{ ruleShowAdvanced ? '收起' : '高级筛选' }}</span>
              <el-icon class="chev"><ArrowDown /></el-icon>
            </a>
          </div>
          <div class="filter-bar">
            <div class="field">
              <label>规则名称</label>
              <div class="control">
                <el-input v-model="ruleQuery.ruleName" placeholder="如 采购发票审核-应付账款" clearable @keyup.enter="handleRuleQuery" />
              </div>
            </div>
            <div class="field">
              <label>规则键</label>
              <div class="control">
                <el-input v-model="ruleQuery.ruleKey" placeholder="如 pms_invoice_payable" clearable @keyup.enter="handleRuleQuery" />
              </div>
            </div>
            <div class="field">
              <label>来源</label>
              <div class="control">
                <el-select v-model="ruleQuery.sourceType" placeholder="全部" clearable style="width: 100%" @change="handleRuleQuery">
                  <el-option label="采购发票" value="invoice" />
                  <el-option label="销售发货" value="shipment" />
                  <el-option label="薪酬发放" value="payroll" />
                </el-select>
              </div>
            </div>
            <div class="field">
              <label>状态</label>
              <div class="control">
                <el-select v-model="ruleQuery.status" placeholder="全部" clearable style="width: 100%" @change="handleRuleQuery">
                  <el-option label="启用" value="0" />
                  <el-option label="停用" value="1" />
                </el-select>
              </div>
            </div>
            <div class="field" v-show="ruleShowAdvanced">
              <label>科目编码</label>
              <div class="control">
                <el-input v-model="ruleQuery.accountCode" placeholder="如 2202" clearable @keyup.enter="handleRuleQuery" />
              </div>
            </div>
            <div class="field" v-show="ruleShowAdvanced">
              <label>借贷方向</label>
              <div class="control">
                <el-select v-model="ruleQuery.direction" placeholder="全部" clearable style="width: 100%" @change="handleRuleQuery">
                  <el-option label="借" value="DEBIT" />
                  <el-option label="贷" value="CREDIT" />
                </el-select>
              </div>
            </div>
          </div>
          <div class="filter-actions">
            <div class="filter-info">
              <el-icon><Filter /></el-icon> 已选 {{ ruleActiveCount }} 个条件，支持回车快速搜索
            </div>
            <div class="filter-buttons">
              <el-button icon="RefreshLeft" @click="resetRuleQuery">重置</el-button>
              <el-button type="primary" icon="Search" @click="handleRuleQuery">搜索</el-button>
            </div>
          </div>
          <el-alert class="cfg-tip" type="info" :closable="false" show-icon>
            <template #title>
              <span class="hint-text">
                规则决定业务单据如何自动生成凭证。
                <b>引擎有两道防线</b>：借贷平衡校验 + 科目方向合理性校验，任一不过则拒绝生成。
                生成的凭证为<b>草稿</b>，须人工审核。
              </span>
            </template>
          </el-alert>
        </div>

        <div class="surface">
          <div class="toolbar">
            <div class="toolbar-left">
              <el-button icon="Refresh" @click="getRules">刷新</el-button>
              <el-button v-hasPermi="['fin:gl:entry:add']" type="primary" icon="MagicStick"
                         @click="handleGenerateInvoice">生成采购凭证</el-button>
              <el-button v-hasPermi="['fin:gl:entry:add']" type="success" icon="MagicStick"
                         @click="handleGenerateShipment">生成销售凭证</el-button>
              </div>
            <div class="toolbar-right">
              <right-toolbar v-model:showSearch="showSearch" :columns="ruleColumns" storageKey="fin_config_rule_columns" @queryTable="getRules" />
              <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
            </div>
          </div>

          <div class="table-wrap">
            <el-table ref="ruleTableRef" v-loading="ruleLoading" :data="filteredRules" border row-key="ruleKey"
                      @header-dragend="ruleResize.onHeaderDragEnd">
              <el-table-column type="expand">
                <template #default="{ row }">
                  <div class="rule-detail">
                    <el-descriptions :column="2" border size="small">
                      <el-descriptions-item label="来源模块">{{ srcModuleLabel(row.sourceModule) }}</el-descriptions-item>
                      <el-descriptions-item label="来源类型">{{ srcTypeLabel(row.sourceType) }}</el-descriptions-item>
                      <el-descriptions-item label="触发状态">
                        业务单据状态为 {{ row.triggerStatus }} 时触发
                      </el-descriptions-item>
                      <el-descriptions-item label="优先级">{{ row.priority }}</el-descriptions-item>
                      <el-descriptions-item label="辅助核算">{{ auxLabel(row.auxType) }}</el-descriptions-item>
                      <el-descriptions-item label="摘要模板">{{ row.summaryTpl || '—' }}</el-descriptions-item>
                      <el-descriptions-item label="备注" :span="2">{{ row.remark || '—' }}</el-descriptions-item>
                    </el-descriptions>
                  </div>
                </template>
              </el-table-column>
              <el-table-column type="index" label="序号" width="85" align="center" fixed="left" />
              <el-table-column key="ruleName" prop="ruleName" label="规则名称" :min-width="colWidth('ruleName', 200)" resizable align="center" show-overflow-tooltip v-if="ruleColumns.ruleName.visible" />
              <el-table-column key="ruleKey" prop="ruleKey" label="规则键" :width="colWidth('ruleKey', 180)" resizable align="center" v-if="ruleColumns.ruleKey.visible">
                <template #default="scope">
                  <code class="rule-key">{{ scope.row.ruleKey }}</code>
                </template>
              </el-table-column>
              <el-table-column key="sourceType" prop="sourceType" label="来源" :width="colWidth('sourceType', 140)" resizable align="center" v-if="ruleColumns.sourceType.visible">
                <template #default="scope">{{ srcTypeLabel(scope.row.sourceType) }}</template>
              </el-table-column>
              <el-table-column key="accountCode" prop="accountCode" label="科目编码" :width="colWidth('accountCode', 110)" resizable align="center" v-if="ruleColumns.accountCode.visible" />
              <el-table-column key="direction" prop="direction" label="方向" :width="colWidth('direction', 90)" resizable align="center" v-if="ruleColumns.direction.visible">
                <template #default="scope">
                  <span class="dir-badge" :class="scope.row.direction === 'DEBIT' ? 'is-debit' : 'is-credit'">
                    {{ scope.row.direction === 'DEBIT' ? '借' : '贷' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column key="amountExpr" prop="amountExpr" label="金额表达式" :width="colWidth('amountExpr', 130)" resizable align="center" v-if="ruleColumns.amountExpr.visible">
                <template #default="scope">{{ exprLabel(scope.row.amountExpr) }}</template>
              </el-table-column>
              <el-table-column key="status" prop="status" label="状态" :width="colWidth('status', 100)" resizable align="center" v-if="ruleColumns.status.visible">
                <template #default="scope">
                  <span class="badge" :class="ruleStatusBadge(scope.row.status)">
                    <span class="dot"></span>{{ scope.row.status === '0' ? '启用' : '停用' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
                <template #default="scope">
                  <div class="action-btn-row">
                    <el-button link type="primary" icon="Edit" v-hasPermi="['fin:config:edit']" @click="handleEditRule(scope.row)">修改</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        </div>
      <!-- ========== 财务参数 ========== -->
      <div v-if="tab === 'fiscal'">
        <div class="surface filter-card">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span>筛选条件</div>
            <button class="tip-pill" style="margin-left: auto;" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
          </div>
          <el-alert type="info" :closable="false" show-icon>
            <template #title>
              <span class="hint-text">
                财务参数按<b>功能域分组</b>，每个分组独立保存，互不影响。
                内置参数不可删除，只能修改取值。
              </span>
            </template>
          </el-alert>
        </div>

        <div class="surface">
          <div v-for="grp in fiscalGroups" :key="grp.key" class="config-section">
            <div class="cs-head">
              <div class="cs-title">
                <span class="dot" :style="{ background: grp.color }" />
                {{ grp.label }}
              </div>
              <el-tag :type="grp.configs.length ? 'success' : 'info'" effect="plain" size="small">
                {{ grp.configs.length ? `已配置 ${grp.configs.length} 项` : '未配置' }}
              </el-tag>
            </div>
            <div class="cs-body">
              <el-form label-width="140px" label-position="left">
                <el-form-item v-for="c in grp.configs" :key="c.configKey" :label="c.configName">
                  <div class="param-row">
                    <el-input
                      v-if="c.editable"
                      v-model="c.configValue"
                      :placeholder="c.placeholder || '请输入'"
                      style="width: 320px"
                    />
                    <el-tag v-else type="info" effect="plain" size="small">
                      {{ c.configValue }}
                    </el-tag>
                    <span class="param-key">{{ c.configKey }}</span>
                    <span class="form-tip">{{ c.remark || '—' }}</span>
                  </div>
                </el-form-item>
                <el-empty v-if="!grp.configs.length" description="该分组暂无配置项" :image-size="60" />
              </el-form>
            </div>
            <div v-if="grp.configs.some(c => c.editable)" class="cs-actions">
              <el-button size="small" @click="resetGroup(grp)">重置</el-button>
              <el-button
                v-hasPermi="['fin:config:edit']"
                type="primary" size="small" icon="Check"
                @click="saveGroup(grp)">
                保存{{ grp.label }}
              </el-button>
            </div>
          </div>
        </div>
        </div>

      <!-- ========== 成本方法 ========== -->
      <div v-if="tab === 'method'">
        <div class="surface filter-card" v-show="showSearch">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span>筛选条件</div>
            <a class="adv-link" :class="{ 'is-open': methodShowAdvanced }" @click.prevent="methodShowAdvanced = !methodShowAdvanced">
              <span>{{ methodShowAdvanced ? '收起' : '高级筛选' }}</span>
              <el-icon class="chev"><ArrowDown /></el-icon>
            </a>
          </div>
          <div class="filter-bar">
            <div class="field">
              <label>物料编码</label>
              <div class="control">
                <el-input v-model="methodQuery.materialCode" placeholder="输入物料编码" clearable @keyup.enter="handleMethodQuery" />
              </div>
            </div>
            <div class="field">
              <label>物料名称</label>
              <div class="control">
                <el-input v-model="methodQuery.materialName" placeholder="输入物料名称" clearable @keyup.enter="handleMethodQuery" />
              </div>
            </div>
            <div class="field">
              <label>计价方法</label>
              <div class="control">
                <el-select v-model="methodQuery.costMethod" placeholder="全部" clearable style="width: 100%" @change="handleMethodQuery">
                  <el-option label="加权平均" value="WEIGHTED_AVG" />
                  <el-option label="先进先出" value="FIFO" />
                </el-select>
              </div>
            </div>
            <div class="field">
              <label>状态</label>
              <div class="control">
                <el-select v-model="methodQuery.status" placeholder="全部" clearable style="width: 100%" @change="handleMethodQuery">
                  <el-option label="启用" value="0" />
                  <el-option label="停用" value="1" />
                </el-select>
              </div>
            </div>
            <div class="field" v-show="methodShowAdvanced">
              <label>入库单价含税</label>
              <div class="control">
                <el-select v-model="methodQuery.taxIncluded" placeholder="全部" clearable style="width: 100%" @change="handleMethodQuery">
                  <el-option label="是" value="Y" />
                  <el-option label="否" value="N" />
                </el-select>
              </div>
            </div>
            <div class="field" v-show="methodShowAdvanced">
              <label>生效日期</label>
              <div class="control">
                <el-date-picker v-model="methodDateRange" type="daterange" range-separator="-"
                                start-placeholder="开始日期" end-placeholder="结束日期"
                                value-format="YYYY-MM-DD" style="width: 100%" />
              </div>
            </div>
          </div>
          <div class="filter-actions">
            <div class="filter-info">
              <el-icon><Filter /></el-icon> 已选 {{ methodActiveCount }} 个条件，支持回车快速搜索
            </div>
            <div class="filter-buttons">
              <el-button icon="RefreshLeft" @click="resetMethodQuery">重置</el-button>
              <el-button type="primary" icon="Search" @click="handleMethodQuery">搜索</el-button>
            </div>
          </div>
          <el-alert class="cfg-tip" type="warning" :closable="false" show-icon>
            <template #title>
              <span class="hint-text">
                计价方法优先级：<b>物料级配置 &gt; 全局默认配置 &gt; 系统默认</b>。
                全局默认配置<b>不允许删除</b>（否则所有物料失去计价依据）。
              </span>
            </template>
          </el-alert>
        </div>

        <div class="surface">
          <div class="toolbar">
            <div class="toolbar-left">
              <el-button icon="Refresh" @click="getCostMethods">刷新</el-button>
              <el-button v-hasPermi="['fin:config:edit']" type="primary" plain icon="Plus"
                         @click="handleAddMethod">新增配置</el-button>
              <span class="form-tip">已配置 {{ materialMethodCount }} 个物料</span>
            </div>
            <div class="toolbar-right">
              <right-toolbar v-model:showSearch="showSearch" :columns="methodColumns" storageKey="fin_config_method_columns" @queryTable="getCostMethods" />
              <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
            </div>
          </div>

          <div class="table-wrap">
            <el-table ref="methodTableRef" v-loading="methodLoading" :data="filteredCostMethods" border
                      @header-dragend="methodResize.onHeaderDragEnd">
              <el-table-column type="index" label="序号" width="85" align="center" fixed="left" />
              <el-table-column key="scope" prop="scope" label="适用范围" :width="colWidth('scope', 150)" resizable align="center" v-if="methodColumns.scope.visible">
                <template #default="scope">
                  <span v-if="!scope.row.materialId" class="badge badge-amber"><span class="dot"></span>全局默认</span>
                  <span v-else class="text-muted">{{ scope.row.materialCode }}</span>
                </template>
              </el-table-column>
              <el-table-column key="materialName" prop="materialName" label="物料名称" :min-width="colWidth('materialName', 180)" resizable align="center" show-overflow-tooltip v-if="methodColumns.materialName.visible">
                <template #default="scope">{{ scope.row.materialName || '—' }}</template>
              </el-table-column>
              <el-table-column key="costMethod" prop="costMethod" label="计价方法" :width="colWidth('costMethod', 130)" resizable align="center" v-if="methodColumns.costMethod.visible">
                <template #default="scope">{{ scope.row.costMethod === 'FIFO' ? '先进先出' : '加权平均' }}</template>
              </el-table-column>
              <el-table-column key="taxIncluded" prop="taxIncluded" label="入库单价含税" :width="colWidth('taxIncluded', 130)" resizable align="center" v-if="methodColumns.taxIncluded.visible">
                <template #default="scope">{{ scope.row.taxIncluded === 'Y' ? '是' : '否' }}</template>
              </el-table-column>
              <el-table-column key="effectiveDate" prop="effectiveDate" label="生效日期" :width="colWidth('effectiveDate', 120)" resizable align="center" v-if="methodColumns.effectiveDate.visible" />
              <el-table-column key="status" prop="status" label="状态" :width="colWidth('status', 100)" resizable align="center" v-if="methodColumns.status.visible">
                <template #default="scope">
                  <span class="badge" :class="methodStatusBadge(scope.row.status)">
                    <span class="dot"></span>{{ scope.row.status === '0' ? '启用' : '停用' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column key="remark" prop="remark" label="备注" :min-width="colWidth('remark', 160)" resizable align="center" show-overflow-tooltip v-if="methodColumns.remark.visible">
                <template #default="scope">{{ scope.row.remark || '—' }}</template>
              </el-table-column>
              <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
                <template #default="scope">
                  <div class="action-btn-row">
                    <el-button v-hasPermi="['fin:config:edit']" link type="primary" icon="View"
                               @click="handleViewMethod(scope.row)">详情</el-button>
                    <el-button v-hasPermi="['fin:config:edit']" link type="primary" icon="Edit"
                               @click="handleEditMethod(scope.row)">修改</el-button>
                    <el-button v-if="scope.row.materialId" v-hasPermi="['fin:config:edit']"
                               link type="danger" icon="Delete"
                               @click="handleDelMethod(scope.row)">删除</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        </div>

    <!-- 结账预检结果 -->
    <el-dialog v-model="showCheck" width="560px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 11l3 3 8-8"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
          </div>
          <div>
            <div class="rd-detail-header-title">结账前置检查</div>
            <div class="rd-detail-header-sub">检查通过后方可执行结账</div>
          </div>
        </div>
      </template>
      <el-table :data="checks" size="small" border>
        <el-table-column label="检查项" min-width="200" align="left">
          <template #default="{ row }">{{ row.name }}</template>
        </el-table-column>
        <el-table-column label="结果" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.pass ? 'success' : 'danger'" size="small">
              {{ row.pass ? '通过' : '未通过' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="说明" min-width="180" align="left">
          <template #default="{ row }">
            <span :class="row.pass ? 'text-muted' : 'text-danger'">{{ row.detail || '—' }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-alert v-if="allPass" type="success" :closable="false" show-icon
                title="全部检查通过，可以执行结账" style="margin-top: 12px" />
      <el-alert v-else type="error" :closable="false" show-icon
                title="存在未通过的检查项，无法结账" style="margin-top: 12px" />
      <template #footer>
        <el-button @click="showCheck = false">关 闭</el-button>
        <el-button v-if="allPass" v-hasPermi="['fin:config:edit']" type="primary"
                   @click="showCheck = false; handleClose()">去结账</el-button>
      </template>
    </el-dialog>

    <!-- 反结账 -->
    <el-dialog v-model="showUnlock" width="480px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
          </div>
          <div>
            <div class="rd-detail-header-title">反结账</div>
            <div class="rd-detail-header-sub">删除该期间账簿与结转损益凭证</div>
          </div>
        </div>
      </template>
      <el-alert type="error" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>
          反结账会<b>删除该期间的账簿与结转损益凭证</b>，请谨慎操作。操作将留痕。
        </template>
      </el-alert>
      <el-form label-width="90px">
        <el-form-item label="会计期间">
          <el-input :model-value="selectedPeriod?.periodName" disabled />
        </el-form-item>
        <el-form-item label="反结账原因" required>
          <el-input v-model="unlockReason" type="textarea" :rows="3"
                    placeholder="必填，将记入操作日志" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="danger" @click="submitUnlock">确认反结账</el-button>
          <el-button @click="showUnlock = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 规则修改 -->
    <el-dialog v-model="showRuleEdit" width="560px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>
          </div>
          <div>
            <div class="rd-detail-header-title">修改凭证规则</div>
            <div class="rd-detail-header-sub">调整科目、方向与金额表达式</div>
          </div>
        </div>
      </template>
      <el-form label-width="100px">
        <el-form-item label="规则名称">
          <el-input v-model="editRule.ruleName" placeholder="如 采购发票审核-应付账款" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="科目编码">
              <el-input v-model="editRule.accountCode" placeholder="如 2202" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="借贷方向">
              <el-radio-group v-model="editRule.direction">
                <el-radio-button value="DEBIT">借</el-radio-button>
                <el-radio-button value="CREDIT">贷</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="金额表达式">
              <el-select v-model="editRule.amountExpr" style="width: 100%">
                <el-option label="价税合计" value="total_amount" />
                <el-option label="不含税金额" value="amount_excl_tax" />
                <el-option label="税额" value="tax_amount" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-input-number v-model="editRule.priority" :min="1" :max="99"
                               controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="摘要模板">
              <el-input v-model="editRule.summaryTpl" placeholder="如 采购入库结算" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="editRule.status">
                <el-radio value="0">启用</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="editRule.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitRule">确 定</el-button>
          <el-button @click="showRuleEdit = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 结账检查记录 -->
    <el-dialog v-model="showRecord" width="720px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8Z"/><path d="M14 2v6h6"/><path d="M16 13H8"/><path d="M16 17H8"/></svg>
          </div>
          <div>
            <div class="rd-detail-header-title">结账检查记录</div>
            <div class="rd-detail-header-sub">检查项执行留痕</div>
          </div>
        </div>
      </template>
      <el-table :data="records" size="small" border>
        <el-table-column prop="checkName" label="检查项" min-width="180" align="left" />
        <el-table-column label="结果" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.checkResult === '1' ? 'success' : 'danger'" size="small">
              {{ row.checkResult === '1' ? '通过' : '未通过' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkDetail" label="详情" min-width="180" align="left">
          <template #default="{ row }">{{ row.checkDetail || '—' }}</template>
        </el-table-column>
        <el-table-column prop="checkBy" label="检查人" width="100" align="center" />
        <el-table-column prop="checkTime" label="检查时间" width="160" align="center" />
      </el-table>
      <template #empty><span class="text-muted">暂无检查记录</span></template>
    </el-dialog>
    <!-- 成本方法编辑 -->
    <el-dialog v-model="showMethodEdit" width="672px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>
          </div>
          <div>
            <div class="rd-detail-header-title">{{ methodForm.configId ? '修改成本方法配置' : '新增成本方法配置' }}</div>
            <div class="rd-detail-header-sub">配置物料计价方法与含税口径</div>
          </div>
        </div>
      </template>
      <el-form ref="methodFormRef" :model="methodForm" :rules="methodRules" label-width="110px">
        <!-- 卡片一：基本信息（规范：表单字段必须按业务模块用 rd-card 分组） -->
        <section class="rd-card form-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M3 10h18"/></svg></span>
              基本信息
            </div>
          </div>
          <div class="rd-card-body">
            <el-form-item label="配置范围" prop="scope">
              <el-radio-group v-model="methodForm.scope">
                <el-radio value="global">全局默认</el-radio>
                <el-radio value="material">指定物料</el-radio>
              </el-radio-group>
              <div class="form-tip">
                全局默认对所有物料生效；指定物料仅对该物料生效（优先级更高）
              </div>
            </el-form-item>
            <el-form-item v-if="methodForm.scope === 'material'" label="物料" prop="materialCode">
              <el-input v-model="methodMaterialText" readonly placeholder="请选择物料" style="width: 100%"
                        @click="openMethodMaterialPicker">
                <template #append><el-button icon="Search" @click="openMethodMaterialPicker" /></template>
              </el-input>
              <div class="form-tip">
                <span>{{ methodForm.materialName || '从仓库管理物料档案中选择' }}</span>
                <el-button v-if="methodForm.materialId" link type="danger" size="small" @click="clearMethodMaterial">清除</el-button>
              </div>
            </el-form-item>
            <el-form-item label="计价方法" prop="costMethod">
              <el-radio-group v-model="methodForm.costMethod">
                <el-radio value="WEIGHTED_AVG">加权平均</el-radio>
                <el-radio value="FIFO">先进先出</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
        </section>
        <!-- 卡片二：生效设置 -->
        <section class="rd-card form-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg></span>
              生效设置
            </div>
          </div>
          <div class="rd-card-body">
            <el-form-item label="入库单价含税">
          <el-radio-group v-model="methodForm.taxIncluded">
              <el-radio value="Y">是</el-radio>
              <el-radio value="N">否</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="生效日期">
                <el-date-picker v-model="methodForm.effectiveDate" type="date" value-format="YYYY-MM-DD"
                                style="width: 100%" placeholder="默认今日" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="状态">
                <el-radio-group v-model="methodForm.status">
                  <el-radio value="0">启用</el-radio>
                  <el-radio value="1">停用</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="备注">
            <el-input v-model="methodForm.remark" type="textarea" :rows="2"
                      placeholder="选填，如：辅料采用先进先出" />
          </el-form-item>
        </div>
        </section>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitMethod">确 定</el-button>
          <el-button @click="showMethodEdit = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <material-picker ref="methodMaterialPickerRef" title="选择物料" @confirm="onMethodMaterialConfirm" />

    <!-- 成本方法配置详情（只读，查看与编辑弹窗分离——规范 10.2） -->
    <el-dialog v-model="viewMethodOpen" width="744px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>
          </div>
          <div>
            <div class="rd-detail-header-title">成本方法配置详情</div>
            <div class="rd-detail-header-sub">配置物料计价方法与含税口径</div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">基本信息</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">配置范围</span>
                <div class="rd-value">{{ viewMethodData.scope === 'material' ? '指定物料' : '全局默认' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">物料编码</span>
                <div class="rd-value">{{ viewMethodData.materialCode || '—' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">物料名称</span>
                <div class="rd-value">{{ viewMethodData.materialName || '—' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">计价方法</span>
                <div class="rd-value">{{ viewMethodData.costMethod === 'FIFO' ? '先进先出' : '加权平均' }}</div>
              </div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">生效设置</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">入库单价含税</span>
                <div class="rd-value">{{ viewMethodData.taxIncluded === 'N' ? '否' : '是' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">生效日期</span>
                <div class="rd-value">{{ viewMethodData.effectiveDate || '—' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">状态</span>
                <div class="rd-value">
                  <el-tag :type="viewMethodData.status === '1' ? 'info' : 'success'" size="small">{{ viewMethodData.status === '1' ? '停用' : '启用' }}</el-tag>
                </div>
              </div>
              <div class="rd-item rd-item--full">
                <span class="rd-label">备注</span>
                <div class="rd-value">{{ viewMethodData.remark || '—' }}</div>
              </div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewMethodOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="showStatusHelp" :title="helpTitle" width="820px" append-to-body>
      <div class="status-help-content">
        <!-- 凭证规则 -->
        <template v-if="tab === 'rule'"><h4>一、凭证规则释义</h4><div class="highlight-card highlight-primary"><div class="highlight-card-title">凭证规则核心说明</div><div class="highlight-card-body">定义业务单据自动生成凭证时的借贷分录：按来源类型配置科目编码、借贷方向、金额表达式与优先级。业务单据触发时系统按规则自动生成记账凭证（PV 前缀，草稿状态须人工审核）。</div></div><h4>二、状态流转图</h4><div class="status-flow"><el-tag type="success">规则启用</el-tag><span class="flow-arrow">→</span><el-tag type="primary">单据触发</el-tag><span class="flow-arrow">→</span><el-tag type="info">生成草稿凭证</el-tag></div><h4>三、关键项说明</h4><el-descriptions :column="1" border><el-descriptions-item label="来源类型">采购发票 / 销售发货 / 薪酬发放，决定规则在哪种单据上生效</el-descriptions-item><el-descriptions-item label="科目编码">生成分录使用的会计科目，必须为明细科目</el-descriptions-item><el-descriptions-item label="借贷方向">借（DEBIT）或贷（CREDIT），须与科目性质匹配</el-descriptions-item><el-descriptions-item label="金额表达式">取值口径：价税合计 / 不含税金额 / 税额</el-descriptions-item><el-descriptions-item label="优先级">同一单据多条规则时按优先级决定生成顺序</el-descriptions-item></el-descriptions><h4>四、重点业务规则</h4><el-row :gutter="16"><el-col :span="12"><div class="highlight-card highlight-primary"><div class="highlight-card-title">自动生成草稿凭证</div><div class="highlight-card-body">业务单据触发后系统按规则生成<strong>草稿凭证（PV 前缀）</strong>，须人工审核后方可入账</div></div></el-col><el-col :span="12"><div class="highlight-card highlight-danger"><div class="highlight-card-title">两道防线校验</div><div class="highlight-card-body">生成凭证时执行<strong>借贷平衡校验 + 科目方向合理性校验</strong>，任一不过则拒绝生成</div></div></el-col></el-row><el-row :gutter="16" style="margin-top: 12px;"><el-col :span="12"><div class="highlight-card highlight-warning"><div class="highlight-card-title">引擎两道防线</div><div class="highlight-card-body">规则配置错误不会直接入账——草稿状态提供人工复核兜底，<strong>确认无误后再审核</strong></div></div></el-col><el-col :span="12"><div class="highlight-card highlight-success"><div class="highlight-card-title">多规则协同</div><div class="highlight-card-body">同一单据可命中多条规则（如应付账款、进项税、原材料分录），<strong>按优先级依次生成</strong></div></div></el-col></el-row><h4>五、业务操作流程</h4><el-timeline><el-timeline-item type="primary" :hollow="true"><strong>新增/编辑规则：</strong>填写规则名称、科目编码、借贷方向与金额表达式，保存规则</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>配置分录：</strong>一条单据场景可配置多条规则分录，按优先级决定生成顺序</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>启用规则：</strong>规则启用后对应业务单据过账时自动生成草稿凭证</el-timeline-item><el-timeline-item type="info" :hollow="true"><strong>人工审核：</strong>在总账凭证中审核自动生成的草稿凭证，审核通过后入账</el-timeline-item></el-timeline></template>
        <!-- 期末结账 -->
        <template v-else-if="tab === 'closing'"><h4>一、期末结账释义</h4><div class="highlight-card highlight-primary"><div class="highlight-card-title">期末结账核心说明</div><div class="highlight-card-body">按会计期间执行的月度核算动作：结账前系统自动执行前置检查（凭证是否全部入账、期间是否平衡等），检查通过后方可结账，结账后该期间单据不再允许修改。</div></div><h4>二、状态流转图</h4><div class="status-flow"><el-tag type="info">未结账</el-tag><span class="flow-arrow">→</span><el-tag type="primary">前置检查通过</el-tag><span class="flow-arrow">→</span><el-tag type="success">已结账</el-tag><span class="flow-arrow">→</span><el-tag type="warning">反结账解锁</el-tag></div><h4>三、关键项说明</h4><el-descriptions :column="1" border><el-descriptions-item label="未结账">期间正常开放，可新增/修改该期间的单据与凭证</el-descriptions-item><el-descriptions-item label="前置检查通过">结账前置检查（凭证入账、借贷平衡等）全部通过，允许执行结账</el-descriptions-item><el-descriptions-item label="已结账">期间封账，该期间单据与凭证不再允许修改</el-descriptions-item><el-descriptions-item label="反结账解锁">确需修改已结账期间数据时填写原因解锁，解锁记录留痕</el-descriptions-item></el-descriptions><h4>四、重点业务规则</h4><el-row :gutter="16"><el-col :span="12"><div class="highlight-card highlight-primary"><div class="highlight-card-title">前置检查兜底</div><div class="highlight-card-body">结账前自动检查凭证入账与平衡状态，<strong>检查不通过无法结账</strong></div></div></el-col><el-col :span="12"><div class="highlight-card highlight-danger"><div class="highlight-card-title">结账后封账</div><div class="highlight-card-body">已结账期间的单据与凭证<strong>不再允许修改</strong>，保护账务数据</div></div></el-col></el-row><el-row :gutter="16" style="margin-top: 12px;"><el-col :span="12"><div class="highlight-card highlight-warning"><div class="highlight-card-title">反结账需原因</div><div class="highlight-card-body">解锁已结账期间必须填写反结账原因，<strong>操作留痕可审计</strong></div></div></el-col><el-col :span="12"><div class="highlight-card highlight-success"><div class="highlight-card-title">权限控制</div><div class="highlight-card-body">反结账使用独立权限（fin:config:periodUnlock），<strong>仅授权人员可操作</strong></div></div></el-col></el-row><h4>五、业务操作流程</h4><el-timeline><el-timeline-item type="primary" :hollow="true"><strong>前置检查：</strong>选择会计期间点击「结账预检」，确认凭证入账与平衡状态</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>执行结账：</strong>检查通过后点击「执行结账」，期间进入已结账状态</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>反结账：</strong>确需调整时填写原因执行反结账，解锁期间</el-timeline-item><el-timeline-item type="info" :hollow="true"><strong>重新结账：</strong>调整完成后重新执行前置检查与结账</el-timeline-item></el-timeline></template>
        <!-- 财务参数 -->
        <template v-else-if="tab === 'fiscal'"><h4>一、财务参数释义</h4><div class="highlight-card highlight-primary"><div class="highlight-card-title">财务参数核心说明</div><div class="highlight-card-body">财务模块的系统级开关配置（如是否启用往来核销、坏账自动计提等），按功能域分组独立保存，互不影响。</div></div><h4>二、重点业务规则</h4><el-row :gutter="16"><el-col :span="12"><div class="highlight-card highlight-primary"><div class="highlight-card-title">按功能域分组</div><div class="highlight-card-body">参数按功能域分组展示，<strong>每组独立保存</strong>，修改一组不影响其他组</div></div></el-col><el-col :span="12"><div class="highlight-card highlight-warning"><div class="highlight-card-title">内置参数保护</div><div class="highlight-card-body">内置参数<strong>不可删除</strong>，只能修改取值，保证系统基线配置完整</div></div></el-col></el-row><el-row :gutter="16" style="margin-top: 12px;"><el-col :span="12"><div class="highlight-card highlight-success"><div class="highlight-card-title">立即生效</div><div class="highlight-card-body">参数保存后财务模块<strong>立即按新参数运行</strong></div></div></el-col><el-col :span="12"><div class="highlight-card highlight-danger"><div class="highlight-card-title">谨慎调整</div><div class="highlight-card-body">部分参数影响核算口径（如坏账计提开关），<strong>调整前确认业务影响</strong></div></div></el-col></el-row><h4>三、业务操作流程</h4><el-timeline><el-timeline-item type="primary" :hollow="true"><strong>定位分组：</strong>在配置分组列表中找到目标功能域</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>调整参数：</strong>修改开关/取值，该分组独立保存</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>确认生效：</strong>保存后财务模块按新参数运行</el-timeline-item></el-timeline></template>
        <!-- 成本方法 -->
        <template v-else><h4>一、成本方法释义</h4><div class="highlight-card highlight-primary"><div class="highlight-card-title">成本方法核心说明</div><div class="highlight-card-body">定义物料的计价方法（加权平均/先进先出）与入库单价含税口径。支持全局默认与指定物料两级配置，指定物料的配置优先级高于全局默认。</div></div><h4>二、状态流转图</h4><div class="status-flow"><el-tag type="primary">全局默认</el-tag><span class="flow-arrow">→</span><el-tag type="warning">指定物料</el-tag><span class="flow-arrow">→</span><el-tag type="success">入库计价应用</el-tag></div><h4>三、关键项说明</h4><el-descriptions :column="1" border><el-descriptions-item label="全局默认">对所有物料生效的基础计价配置</el-descriptions-item><el-descriptions-item label="指定物料">仅对所选物料生效，<strong>优先级高于全局默认</strong></el-descriptions-item><el-descriptions-item label="加权平均">出库单价 =（期初金额+入库金额）/（期初数量+入库数量）</el-descriptions-item><el-descriptions-item label="先进先出">FIFO：最早批次成本先行结转，批次维度跟踪</el-descriptions-item></el-descriptions><h4>四、重点业务规则</h4><el-row :gutter="16"><el-col :span="12"><div class="highlight-card highlight-primary"><div class="highlight-card-title">物料弹框选择</div><div class="highlight-card-body">指定物料时通过<strong>物料选择弹框</strong>从仓库物料档案中选择，自动回填编码与名称</div></div></el-col><el-col :span="12"><div class="highlight-card highlight-warning"><div class="highlight-card-title">含税口径</div><div class="highlight-card-body">入库单价含税口径（是/否）决定计价基数，<strong>须与采购开票口径一致</strong></div></div></el-col></el-row><el-row :gutter="16" style="margin-top: 12px;"><el-col :span="12"><div class="highlight-card highlight-success"><div class="highlight-card-title">生效日期</div><div class="highlight-card-body">配置支持生效日期，<strong>自生效日起应用于入库计价</strong></div></div></el-col><el-col :span="12"><div class="highlight-card highlight-danger"><div class="highlight-card-title">删除限制</div><div class="highlight-card-body">被入库计价引用的配置<strong>不可随意删除</strong>，避免成本断链</div></div></el-col></el-row><h4>五、业务操作流程</h4><el-timeline><el-timeline-item type="primary" :hollow="true"><strong>新增配置：</strong>点击「新增配置」，选择配置范围（全局默认/指定物料）</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>选择物料：</strong>指定物料时通过物料选择弹框从仓库物料档案选择</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>应用计价：</strong>入库计价/出库结转时按配置的计价方法计算成本</el-timeline-item></el-timeline></template>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="FinConfig">
const showStatusHelp = ref(false)
const showSearch = ref(true)
/** 帮助弹框标题与内容按当前页签动态显示 */
const helpTitle = computed(() => ({ rule: '凭证规则业务操作说明', closing: '期末结账业务操作说明', fiscal: '财务参数业务操作说明', method: '成本方法业务操作说明' }[tab.value] || '财务配置业务操作说明'))
import {
  listPeriods, closingCheck, closePeriod, unlockPeriod, closingRecords,
  listVoucherRules, updateVoucherRule, generateBatch
} from '@/api/fin/config'
import {
  listFiscal, updateFiscal, listCostMethod, addCostMethod,
  updateCostMethod, delCostMethod
} from '@/api/fin/configExt'

const { proxy } = getCurrentInstance()
import { useColumnResize } from '@/composables/useColumnResize'
import MaterialPicker from '@/components/MaterialPicker/index.vue'

const closingResize = useColumnResize('fin_config_closing_index')
const ruleResize = useColumnResize('fin_config_rule_index')
const methodResize = useColumnResize('fin_config_costmethod_index')
const { colWidth } = closingResize

const closingColumns = ref({
  periodName: { label: '会计期间', visible: true },
  yearMonth: { label: '年月', visible: true },
  status: { label: '结账状态', visible: true },
  closeBy: { label: '结账人', visible: true },
  closeTime: { label: '结账时间', visible: true },
  unlockBy: { label: '最近反结账', visible: true },
  unlockReason: { label: '反结账原因', visible: true }
})
const ruleColumns = ref({
  ruleName: { label: '规则名称', visible: true },
  ruleKey: { label: '规则键', visible: true },
  sourceType: { label: '来源', visible: true },
  accountCode: { label: '科目编码', visible: true },
  direction: { label: '方向', visible: true },
  amountExpr: { label: '金额表达式', visible: true },
  status: { label: '状态', visible: true }
})
const methodColumns = ref({
  scope: { label: '适用范围', visible: true },
  materialName: { label: '物料名称', visible: true },
  costMethod: { label: '计价方法', visible: true },
  taxIncluded: { label: '入库单价含税', visible: true },
  effectiveDate: { label: '生效日期', visible: true },
  status: { label: '状态', visible: true },
  remark: { label: '备注', visible: true }
})

function loadColumnVisibility() {
  const load = (key, target) => {
    try {
      const saved = JSON.parse(localStorage.getItem(`${key}_cols`) || 'null')
      if (saved && typeof saved === 'object') {
        Object.keys(target.value).forEach(k => {
          if (saved[k] === false) target.value[k].visible = false
        })
      }
    } catch (e) { /* ignore */ }
  }
  load('fin_config_closing_index', closingColumns)
  load('fin_config_rule_index', ruleColumns)
  load('fin_config_costmethod_index', methodColumns)
}

function closingStatusBadge(s) { return s === 'CLOSED' ? 'badge-green' : 'badge-amber' }
function ruleStatusBadge(s) { return s === '0' ? 'badge-green' : 'badge-gray' }
function methodStatusBadge(s) { return s === '0' ? 'badge-green' : 'badge-gray' }

/** 四个分组已拆为独立菜单，无页签交互；按路由 path 确定本页渲染的分区（未匹配时默认期末结账） */
const route = useRoute()
const tab = computed(() => {
  const matched = [['/closing', 'closing'], ['/rule', 'rule'], ['/param', 'fiscal'], ['/method', 'method']]
    .find(([p]) => route.path.endsWith(p))
  return matched ? matched[1] : 'closing'
})
const loading = ref(false)
const ruleLoading = ref(false)
const periodList = ref([])
const rules = ref([])
const selectedPeriod = ref(null)
const checks = ref([])
const records = ref([])
const unlockReason = ref('')

const showCheck = ref(false)
const showUnlock = ref(false)
const showRuleEdit = ref(false)
const showRecord = ref(false)
const editRule = reactive({})

const allPass = computed(() => checks.value.length > 0 && checks.value.every(c => c.pass))

function srcModuleLabel(m) {
  return { pms: '采购管理', mk: '营销管理', hr: '人力资源', fin: '财务' }[m] || m
}

function srcTypeLabel(t) {
  return { invoice: '采购发票', shipment: '销售发货', payroll: '薪酬发放' }[t] || t
}

function auxLabel(t) {
  return { CUSTOMER: '客户', SUPPLIER: '供应商', DEPT: '部门' }[t] || '无'
}

function exprLabel(e) {
  return { total_amount: '价税合计', amount_excl_tax: '不含税金额', tax_amount: '税额' }[e] || e
}

function getPeriods() {
  loading.value = true
  listPeriods().then(res => {
    periodList.value = res.rows || []
    loading.value = false
    closingResize.applySavedWidths()
  })
}

function getRules() {
  ruleLoading.value = true
  listVoucherRules().then(res => {
    rules.value = res.data || []
    ruleLoading.value = false
    ruleResize.applySavedWidths()
  })
}

function handleCurrentChange(row) { selectedPeriod.value = row }

// ==================== 筛选条件（list guide 2.x） ====================
const like = (v, s) => !v || (s || '').toLowerCase().includes(String(v).toLowerCase())

// ---- 期末结账 ----
const closingQuery = reactive({ periodName: '', yearMonth: '', status: '', closeBy: '' })
const closingApplied = reactive({ periodName: '', yearMonth: '', status: '', closeBy: '' })
const closingActiveCount = computed(() => Object.values(closingApplied).filter(Boolean).length)
const filteredPeriodList = computed(() => periodList.value.filter(p =>
  like(closingApplied.periodName, p.periodName) &&
  like(closingApplied.yearMonth, p.yearMonth) &&
  (!closingApplied.status || p.status === closingApplied.status) &&
  like(closingApplied.closeBy, p.closeBy)
))
function handleClosingQuery() {
  Object.assign(closingApplied, { ...closingQuery })
}
function resetClosingQuery() {
  Object.assign(closingQuery, { periodName: '', yearMonth: '', status: '', closeBy: '' })
  handleClosingQuery()
}

// ---- 凭证规则 ----
const ruleQuery = reactive({ ruleName: '', ruleKey: '', sourceType: '', status: '', accountCode: '', direction: '' })
const ruleApplied = reactive({ ruleName: '', ruleKey: '', sourceType: '', status: '', accountCode: '', direction: '' })
const ruleShowAdvanced = ref(false)
const ruleActiveCount = computed(() => Object.values(ruleApplied).filter(Boolean).length)
const filteredRules = computed(() => rules.value.filter(r =>
  like(ruleApplied.ruleName, r.ruleName) &&
  like(ruleApplied.ruleKey, r.ruleKey) &&
  (!ruleApplied.sourceType || r.sourceType === ruleApplied.sourceType) &&
  (!ruleApplied.status || r.status === ruleApplied.status) &&
  like(ruleApplied.accountCode, r.accountCode) &&
  (!ruleApplied.direction || r.direction === ruleApplied.direction)
))
function handleRuleQuery() {
  ruleShowAdvanced.value = false
  Object.assign(ruleApplied, { ...ruleQuery })
}
function resetRuleQuery() {
  Object.assign(ruleQuery, { ruleName: '', ruleKey: '', sourceType: '', status: '', accountCode: '', direction: '' })
  ruleShowAdvanced.value = false
  handleRuleQuery()
}

// ---- 成本方法 ----
const methodQuery = reactive({ materialCode: '', materialName: '', costMethod: '', status: '', taxIncluded: '' })
const methodApplied = reactive({ materialCode: '', materialName: '', costMethod: '', status: '', taxIncluded: '' })
const methodDateRange = ref([])
const methodShowAdvanced = ref(false)
const methodActiveCount = computed(() => {
  return Object.values(methodApplied).filter(Boolean).length + (methodApplied.beginDate ? 1 : 0)
})
const filteredCostMethods = computed(() => costMethods.value.filter(m => {
  if (methodApplied.beginDate) {
    const d = m.effectiveDate ? String(m.effectiveDate).slice(0, 10) : ''
    if (!d || d < methodApplied.beginDate || d > methodApplied.endDate) return false
  }
  return like(methodApplied.materialCode, m.materialCode) &&
    like(methodApplied.materialName, m.materialName) &&
    (!methodApplied.costMethod || m.costMethod === methodApplied.costMethod) &&
    (!methodApplied.status || m.status === methodApplied.status) &&
    (!methodApplied.taxIncluded || m.taxIncluded === methodApplied.taxIncluded)
}))
function handleMethodQuery() {
  methodShowAdvanced.value = false
  Object.assign(methodApplied, { ...methodQuery })
  const dr = methodDateRange.value || []
  methodApplied.beginDate = dr[0] || ''
  methodApplied.endDate = dr[1] || ''
}
function resetMethodQuery() {
  Object.assign(methodQuery, { materialCode: '', materialName: '', costMethod: '', status: '', taxIncluded: '' })
  methodDateRange.value = []
  methodShowAdvanced.value = false
  handleMethodQuery()
}

function handleCheck() {
  if (!selectedPeriod.value) {
    proxy.$modal.msgWarning('请先选择会计期间')
    return
  }
  closingCheck(selectedPeriod.value.yearMonth).then(res => {
    checks.value = res.data || []
    showCheck.value = true
  })
}

function handleClose() {
  if (!selectedPeriod.value) {
    proxy.$modal.msgWarning('请先选择会计期间')
    return
  }
  const p = selectedPeriod.value
  proxy.$modal.confirm(
    `确认对「${p.periodName}」执行结账？` +
    `系统将依次执行：结转损益 → 生成账簿 → 封期。结账后该期间不可再修改凭证。`
  ).then(() => {
    return closePeriod(p.yearMonth)
  }).then(res => {
    const d = res.data || {}
    proxy.$modal.msgSuccess(
      `结账完成：${d.ledgerRows || 0} 个科目已生成账簿` +
      (d.carryForwardVoucherId ? `，结转损益凭证已生成` : '（本期无损益发生额）')
    )
    getPeriods()
  }).catch(() => {})
}

function handleUnlock() {
  if (!selectedPeriod.value) {
    proxy.$modal.msgWarning('请先选择会计期间')
    return
  }
  unlockReason.value = ''
  showUnlock.value = true
}

function submitUnlock() {
  if (!unlockReason.value || !unlockReason.value.trim()) {
    proxy.$modal.msgError('反结账原因必须填写')
    return
  }
  unlockPeriod(selectedPeriod.value.yearMonth, unlockReason.value.trim()).then(() => {
    proxy.$modal.msgSuccess('反结账成功，该期间已解锁')
    showUnlock.value = false
    getPeriods()
  })
}

function showRecords(row) {
  closingRecords(row.yearMonth).then(res => {
    records.value = res.data || []
    showRecord.value = true
  })
}

function handleEditRule(row) {
  Object.assign(editRule, row)
  showRuleEdit.value = true
}

function submitRule() {
  updateVoucherRule(editRule).then(() => {
    proxy.$modal.msgSuccess('规则修改成功')
    showRuleEdit.value = false
    getRules()
  })
}

function handleGenerateInvoice() {
  proxy.$modal.confirm('将扫描已审核的采购发票，按规则生成记账凭证（草稿），是否继续？').then(() => {
    return generateBatch('pms', 'invoice', 50)
  }).then(res => showGenerateResult(res.data)).catch(() => {})
}

function handleGenerateShipment() {
  proxy.$modal.confirm('将扫描已审核的发货单，按规则生成记账凭证（草稿），是否继续？').then(() => {
    return generateBatch('mk', 'shipment', 50)
  }).then(res => showGenerateResult(res.data)).catch(() => {})
}

function showGenerateResult(d) {
  const r = d || {}
  let msg = `共 ${r.total || 0} 张单据，成功生成 ${r.success || 0} 张`
  if (r.skip > 0) {
    msg += `，跳过 ${r.skip} 张`
    if (r.errors && r.errors.length) {
      proxy.$modal.msgWarning(`${msg}\n首个失败原因：${r.errors[0]}`)
      return
    }
  }
  proxy.$modal.msgSuccess(msg)
}

// ==================== 财务参数配置 ====================

// 分组定义：与后端 config_group 对应
const FISCAL_GROUPS = [
  { key: 'common', label: '基础设置', color: 'var(--brand-500)' },
  { key: 'cost', label: '成本设置', color: 'var(--amber-500)' },
  { key: 'tax', label: '税务设置', color: 'var(--red-500)' },
  { key: 'bad_debt', label: '坏账计提', color: 'var(--violet-500)' },
  { key: 'voucher', label: '凭证设置', color: 'var(--green-500)' },
  { key: 'custom', label: '自定义', color: 'var(--ink-400)' }
]

// 各配置项的编辑提示（避免用户填错格式）
const FISCAL_HINTS = {
  output_tax_rate: { placeholder: '如 0.13（13%）', editable: true },
  ar_aging_ratio: {
    placeholder: '如 {"1-30":0.05,"31-60":0.10,"61-90":0.30,"90+":0.50}',
    editable: true
  },
  auto_voucher: { placeholder: 'true / false', editable: true },
  cost_method: { placeholder: 'WEIGHTED_AVG 或 FIFO', editable: true },
  default_currency: { placeholder: 'CNY', editable: false }
}

// 分组原始值（用于「重置」仅还原本分组字段—— 规范第5条）
const fiscalOriginal = {}

const fiscalGroups = ref([])

function getFiscal() {
  listFiscal({ pageNum: 1, pageSize: 100 }).then(res => {
    const rows = res.rows || []
    const byGroup = {}
    FISCAL_GROUPS.forEach(g => { byGroup[g.key] = [] })
    rows.forEach(r => {
      const hint = FISCAL_HINTS[r.configKey] || { editable: true }
      const item = {
        ...r,
        editable: hint.editable,
        placeholder: hint.placeholder
      }
      const g = byGroup[r.configGroup] || byGroup['custom']
      g.push(item)
      fiscalOriginal[r.configKey] = r.configValue
    })
    // 补齐预设但库里没有的配置项，让用户能直接填
    FISCAL_GROUPS.forEach(g => {
      if (g.key === 'custom') return
      byGroup[g.key].forEach(item => {
        if (fiscalOriginal[item.configKey] === undefined) {
          fiscalOriginal[item.configKey] = item.configValue
        }
      })
    })
    fiscalGroups.value = FISCAL_GROUPS.map(g => ({ ...g, configs: byGroup[g.key] }))
  })
}

/** 保存单个分组（独立端点，不影响其他分组） */
function saveGroup(grp) {
  const targets = grp.configs.filter(c => c.editable)
  if (!targets.length) {
    proxy.$modal.msgWarning('该分组没有可编辑项')
    return
  }
  proxy.$modal.confirm(`确认保存「${grp.label}」分组的 ${targets.length} 项配置？`).then(() => {
    return Promise.all(targets.map(c => updateFiscal(c.configKey, c.configValue)))
  }).then(() => {
    proxy.$modal.msgSuccess(`${grp.label} 配置已保存`)
    getFiscal()
  }).catch(() => {})
}

/** 重置仅还原本分组字段（禁止整表单重置） */
function resetGroup(grp) {
  grp.configs.forEach(c => {
    if (c.editable && fiscalOriginal[c.configKey] !== undefined) {
      c.configValue = fiscalOriginal[c.configKey]
    }
  })
  proxy.$modal.msgInfo(`已还原本分组（${grp.label}）的修改，未保存`)
}

// ==================== 成本方法配置 ====================

const methodLoading = ref(false)
const costMethods = ref([])
const showMethodEdit = ref(false)
const methodForm = reactive({ scope: 'global' })
/** 编辑弹窗校验规则（物料编码按配置范围条件必填） */
const methodRules = {
  scope: [{ required: true, message: '请选择配置范围', trigger: 'change' }],
  materialCode: [{ required: true, message: '物料编码不能为空', trigger: 'blur' }],
  costMethod: [{ required: true, message: '请选择计价方法', trigger: 'change' }]
}
/** 详情弹窗（只读，查看与编辑弹窗分离——规范 10.2） */
const viewMethodOpen = ref(false)
const viewMethodData = ref({})
const materialMethodCount = computed(() => costMethods.value.filter(m => m.materialId).length)

function getCostMethods() {
  methodLoading.value = true
  listCostMethod({ pageNum: 1, pageSize: 200 }).then(res => {
    costMethods.value = res.rows || []
    methodLoading.value = false
    methodResize.applySavedWidths()
  })
}

function handleAddMethod() {
  Object.assign(methodForm, {
    configId: undefined, scope: 'global',
    materialCode: '', materialId: '', materialName: '',
    costMethod: 'WEIGHTED_AVG', taxIncluded: 'Y', status: '0',
    effectiveDate: new Date().toISOString().slice(0, 10), remark: ''
  })
  showMethodEdit.value = true
}

function handleEditMethod(row) {
  Object.assign(methodForm, {
    configId: row.configId,
    scope: row.materialId ? 'material' : 'global',
    materialCode: row.materialCode || '',
    materialId: row.materialId || '',
    materialName: row.materialName || '',
    costMethod: row.costMethod,
    taxIncluded: row.taxIncluded || 'Y',
    status: row.status || '0',
    effectiveDate: row.effectiveDate ? String(row.effectiveDate).slice(0, 10) : '',
    remark: row.remark || ''
  })
  showMethodEdit.value = true
}

/** 查看详情（独立只读弹窗，规范 10.2） */
function handleViewMethod(row) {
  viewMethodData.value = { ...row }
  viewMethodOpen.value = true
}

/** 物料弹框选择（picker 规范：调取仓库管理物料档案） */
const methodMaterialPickerRef = ref(null)
const methodMaterialText = computed(() =>
  methodForm.materialCode ? (methodForm.materialCode + (methodForm.materialName ? ' - ' + methodForm.materialName : '')) : ''
)
function openMethodMaterialPicker() { methodMaterialPickerRef.value.open(methodForm.materialId) }
function onMethodMaterialConfirm(m) {
  methodForm.materialId = m.materialId
  methodForm.materialCode = m.materialCode
  methodForm.materialName = m.materialName
}
function clearMethodMaterial() {
  methodForm.materialId = undefined
  methodForm.materialCode = undefined
  methodForm.materialName = undefined
}

function submitMethod() {
  proxy.$refs['methodFormRef'].validate(valid => {
    if (!valid) return
    // 兜底校验：指定物料时物料编码必填
    if (methodForm.scope === 'material' && !(methodForm.materialCode || '').trim()) {
      proxy.$modal.msgError('配置范围为"指定物料"时，物料编码不能为空')
      return
    }
    const payload = {
      configId: methodForm.configId,
      materialId: methodForm.scope === 'global' ? null : (methodForm.materialId || null),
      costMethod: methodForm.costMethod,
      taxIncluded: methodForm.taxIncluded,
      effectiveDate: methodForm.effectiveDate,
      status: methodForm.status,
      remark: methodForm.remark
    }
    const req = methodForm.configId ? updateCostMethod : addCostMethod
    req(payload).then(() => {
      proxy.$modal.msgSuccess(methodForm.configId ? '修改成功' : '新增成功')
      showMethodEdit.value = false
      getCostMethods()
    })
  })
}

function handleDelMethod(row) {
  proxy.$modal.confirm(
    `是否确认删除「${row.materialCode || '全局默认'}」的成本方法配置？`
  ).then(() => {
    return delCostMethod(row.configId)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getCostMethods()
  }).catch(() => {})
}

getFiscal()
getCostMethods()

getPeriods()
getRules()
loadColumnVisibility()
</script>

<style scoped>
/* ===== design tokens（list guide 7.1） ===== */
.fin-config-page {
  --page-bg: #f5f7fa;
  --card-bg: #ffffff;
  --border-color: #e4e7ed;
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-tertiary: #909399;
  --brand-primary: #2563eb;
  --brand-primary-light: #eff6ff;
  --ink-black: #1f2937;
  --amber-600: #d97706;
  --amber-50: #fffbeb;
  --blue-600: #2563eb;
  --blue-50: #eff6ff;
  --green-600: #16a34a;
  --green-50: #f0fdf4;
  --red-600: #dc2626;
  --red-50: #fef2f2;
  --violet-50: #f5f3ff;
  --r-md: 8px;
  --r-lg: 12px;
  --shadow-card: 0 1px 3px rgba(0, 0, 0, 0.06);
  --ease-out: cubic-bezier(0.16, 1, 0.3, 1);
  padding: 16px;
  background: var(--page-bg);
  min-height: calc(100vh - 84px);
}

/* ===== surface ===== */
.fin-config-page .surface {
  background: var(--card-bg);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-card);
  padding: 16px;
  margin-bottom: 16px;
}
/* ===== filter-card ===== */
.filter-card { padding-bottom: 12px; }
.filter-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: var(--text-primary); }
.filter-title .glyph { width: 3px; height: 14px; border-radius: 2px; background: var(--brand-primary); display: inline-block; }
.filter-actions { display: flex; align-items: center; gap: 8px; }
.filter-bar {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px 16px;
  margin-bottom: 4px;
}
.filter-bar .field { display: flex; flex-direction: column; gap: 6px; }
.filter-bar .field label { font-size: 12px; font-weight: 500; color: var(--text-secondary); display: flex; align-items: center; gap: 6px; }
.filter-bar .field .control {
  display: flex; align-items: center;
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: var(--r-md);
  transition: border-color .15s var(--ease-out), box-shadow .15s var(--ease-out);
}
.filter-bar .field .control:focus-within {
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, .12);
}
.filter-bar .field .control :deep(.el-input__wrapper),
.filter-bar .field .control :deep(.el-select__wrapper) { box-shadow: none !important; background: transparent; }
.filter-actions {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 14px; padding-top: 12px;
  border-top: 1px dashed var(--border-color);
}
.filter-info { display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--text-tertiary); }
.filter-buttons { display: flex; align-items: center; gap: 8px; }
.adv-link {
  font-size: 13px; color: var(--text-secondary); text-decoration: none;
  display: flex; align-items: center; gap: 4px; cursor: pointer;
  transition: color .15s;
}
.adv-link:hover, .adv-link.is-open { color: var(--brand-primary); }
.adv-link .chev { transition: transform .2s var(--ease-out); }
.adv-link.is-open .chev { transform: rotate(180deg); }
.cfg-tip { margin-top: 10px; }
.cfg-tip :deep(.el-alert__title) { font-weight: normal; }

@media (max-width: 1100px) {
  .filter-bar { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 720px) {
  .filter-bar { grid-template-columns: 1fr; }
}

/* ===== toolbar ===== */
.toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.toolbar-left { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.toolbar-right { display: flex; align-items: center; }

/* ===== table ===== */
.table-wrap { width: 100%; }
.pagination-container { display: flex; justify-content: flex-end; margin-top: 12px; }

/* ===== status badge（dot + 文字） ===== */
.badge { display: inline-flex; align-items: center; gap: 6px; padding: 2px 10px; border-radius: 999px; font-size: 12px; line-height: 20px; white-space: nowrap; }
.badge .dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; flex-shrink: 0; }
.badge-blue { background: var(--blue-50); color: var(--blue-600); }
.badge-green { background: var(--green-50); color: var(--green-600); }
.badge-amber { background: var(--amber-50); color: var(--amber-600); }
.badge-red { background: var(--red-50); color: var(--red-600); }
.badge-gray { background: #f3f4f6; color: var(--text-tertiary); }
.badge-violet { background: var(--violet-50); color: #7c3aed; }

/* ===== 操作列（5.5.3） ===== */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0 !important; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== 页面特有 ===== */
.hint-text { font-size: 12px; }
.text-muted { color: var(--text-tertiary); }
.text-danger { color: var(--red-600); }
.text-warn { color: var(--amber-600); }

.rule-detail { padding: 8px 16px; background: var(--page-bg); }
.rule-key {
  font-family: ui-monospace, Menlo, Consolas, monospace;
  font-size: 12px;
  background: var(--page-bg);
  padding: 2px 6px;
  border-radius: 3px;
}

.dir-badge { display: inline-block; width: 26px; height: 22px; line-height: 22px; text-align: center; border-radius: 3px; font-size: 12px; font-weight: 600; }
.dir-badge.is-debit { background: var(--blue-50); color: var(--blue-600); }
.dir-badge.is-credit { background: var(--red-50); color: var(--red-600); }

.config-section { border: 1px solid var(--border-color); border-radius: var(--r-md); margin-bottom: 12px; overflow: hidden; }
.cs-head { display: flex; align-items: center; justify-content: space-between; padding: 10px 16px; background: var(--page-bg); }
.cs-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 13px; color: var(--text-primary); }
.cs-title .dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.cs-body { padding: 8px 16px 0; }
.cs-actions { display: flex; justify-content: flex-end; gap: 8px; padding: 8px 16px 12px; }
.param-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.param-key { font-family: ui-monospace, Menlo, Consolas, monospace; font-size: 12px; color: var(--text-tertiary); }
.form-tip { font-size: 12px; color: var(--text-tertiary); line-height: 1.6; }

/* ===== rd- 弹窗体系：样式统一走全局规范（detail-page.scss），此处禁止本地重定义 =====
   原因：append-to-body 弹窗内 scoped 属性选择器（.rd-card[data-v-xxx]）会覆盖全局样式，
   导致卡片背景/圆角/阴影与参考页面（partner 等）不一致；本页仅保留页面独有样式（form-tip 等）。 */

.rdFadeIn-enter-active { animation: rd-fade-in 0.28s var(--ease-out); }
@keyframes rd-fade-in { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 900px) {
  .rd-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
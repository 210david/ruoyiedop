<template>
  <div class="app-container fin-config-page">
    <el-tabs v-model="tab" class="cfg-tabs">
      <!-- ========== 期末结账 ========== -->
      <el-tab-pane label="期末结账" name="closing">
        <div class="surface filter-card">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span>筛选条件</div>
          </div>
          <el-alert type="warning" :closable="false" show-icon>
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
              <right-toolbar :search="false" @queryTable="getPeriods" />
            </div>
          </div>

          <div class="table-wrap">
            <el-table ref="closingTableRef" v-loading="loading" :data="periodList" border highlight-current
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
      </el-tab-pane>

      <!-- ========== 凭证规则 ========== -->
      <el-tab-pane label="凭证规则" name="rule">
        <div class="surface filter-card">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span>筛选条件</div>
          </div>
          <el-alert type="info" :closable="false" show-icon>
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
              <right-toolbar :search="false" @queryTable="getRules" />
            </div>
          </div>

          <div class="table-wrap">
            <el-table ref="ruleTableRef" v-loading="ruleLoading" :data="rules" border row-key="ruleKey"
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
      </el-tab-pane>
      <!-- ========== 财务参数 ========== -->
      <el-tab-pane label="财务参数" name="fiscal">
        <div class="surface filter-card">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span>筛选条件</div>
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
      </el-tab-pane>

      <!-- ========== 成本方法 ========== -->
      <el-tab-pane label="成本方法" name="method">
        <div class="surface filter-card">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span>筛选条件</div>
          </div>
          <el-alert type="warning" :closable="false" show-icon>
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
              <right-toolbar :search="false" @queryTable="getCostMethods" />
            </div>
          </div>

          <div class="table-wrap">
            <el-table ref="methodTableRef" v-loading="methodLoading" :data="costMethods" border
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
      </el-tab-pane>
    </el-tabs>
  </div>

    <!-- 结账预检结果 -->
    <el-dialog v-model="showCheck" width="560px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-header-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 11l3 3 8-8"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
          </div>
          <div>
            <div class="rd-header-title">结账前置检查</div>
            <div class="rd-header-sub">检查通过后方可执行结账</div>
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
          <div class="rd-header-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
          </div>
          <div>
            <div class="rd-header-title">反结账</div>
            <div class="rd-header-sub">删除该期间账簿与结转损益凭证</div>
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
          <div class="rd-header-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>
          </div>
          <div>
            <div class="rd-header-title">修改凭证规则</div>
            <div class="rd-header-sub">调整科目、方向与金额表达式</div>
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
          <div class="rd-header-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8Z"/><path d="M14 2v6h6"/><path d="M16 13H8"/><path d="M16 17H8"/></svg>
          </div>
          <div>
            <div class="rd-header-title">结账检查记录</div>
            <div class="rd-header-sub">检查项执行留痕</div>
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
    <el-dialog v-model="showMethodEdit" width="560px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-header-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>
          </div>
          <div>
            <div class="rd-header-title">{{ methodForm.configId ? '修改成本方法配置' : '新增成本方法配置' }}</div>
            <div class="rd-header-sub">配置物料计价方法与含税口径</div>
          </div>
        </div>
      </template>
      <el-form label-width="110px">
        <el-form-item label="配置范围">
          <el-radio-group v-model="methodForm.scope">
            <el-radio value="global">全局默认</el-radio>
            <el-radio value="material">指定物料</el-radio>
          </el-radio-group>
          <div class="form-tip">
            全局默认对所有物料生效；指定物料仅对该物料生效（优先级更高）
          </div>
        </el-form-item>
        <el-form-item v-if="methodForm.scope === 'material'" label="物料" required>
          <el-input v-model="methodForm.materialCode" placeholder="输入物料编码"
                    @blur="onMethodMaterialBlur" />
          <div class="form-tip">{{ methodForm.materialName || '输入后自动带出名称' }}</div>
        </el-form-item>
        <el-form-item v-if="methodForm.scope === 'material'" label="物料ID">
          <el-input v-model="methodForm.materialId" placeholder="物料ID（可留空，仅影响名称回显）" />
        </el-form-item>
        <el-form-item label="计价方法" required>
          <el-radio-group v-model="methodForm.costMethod">
            <el-radio value="WEIGHTED_AVG">加权平均</el-radio>
            <el-radio value="FIFO">先进先出</el-radio>
          </el-radio-group>
        </el-form-item>
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
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitMethod">确 定</el-button>
          <el-button @click="showMethodEdit = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FinConfig">
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

const tab = ref('closing')
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

function onMethodMaterialBlur() {
  const code = (methodForm.materialCode || '').trim()
  if (!code) return
  const hit = costMethods.value.find(m => m.materialCode === code)
  if (hit) {
    methodForm.materialId = hit.materialId
    methodForm.materialName = hit.materialName
  } else {
    methodForm.materialName = ''
  }
}

function submitMethod() {
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
.fin-config-page .cfg-tabs > .el-tabs__header {
  background: var(--card-bg);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-card);
  padding: 0 16px;
  margin-bottom: 12px;
}
.fin-config-page .cfg-tabs > .el-tabs__content { padding: 0; }

/* ===== filter-card ===== */
.filter-card { padding-bottom: 12px; }
.filter-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: var(--text-primary); }
.filter-title .glyph { width: 3px; height: 14px; border-radius: 2px; background: var(--brand-primary); display: inline-block; }
.filter-actions { display: flex; align-items: center; gap: 8px; }

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

/* ===== rd- 弹窗体系（detail guide 第五章） ===== */
.rd-dialog { border-radius: var(--r-lg); overflow: hidden; }
.rd-dialog .el-dialog__header { padding: 0; margin: 0; }
.rd-dialog .el-dialog__body { padding: 20px 24px; }
.rd-detail-header { display: flex; align-items: center; gap: 12px; padding: 16px 24px; background: linear-gradient(135deg, var(--blue-600) 0%, #1d4ed8 100%); color: #fff; }
.rd-header-icon { width: 40px; height: 40px; border-radius: var(--r-md); background: rgba(255, 255, 255, 0.18); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.rd-header-title { font-size: 16px; font-weight: 600; line-height: 1.4; }
.rd-header-sub { font-size: 12px; opacity: 0.85; margin-top: 2px; }

.rd-page { display: flex; flex-direction: column; gap: 16px; }
.rd-card { background: var(--card-bg); border: 1px solid var(--border-color); border-radius: var(--r-md); padding: 16px; }
.rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: var(--text-primary); margin-bottom: 12px; }
.rd-card-title::before { content: ''; width: 3px; height: 14px; border-radius: 2px; background: var(--brand-primary); }
.rd-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px 20px; }
.rd-item { min-width: 0; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { font-size: 12px; color: var(--text-tertiary); margin-bottom: 4px; }
.rd-value { font-size: 13px; color: var(--text-primary); line-height: 1.6; word-break: break-all; }
.rd-value--muted { color: var(--text-tertiary); }
.rd-empty { color: var(--text-tertiary); font-size: 13px; }

.rdFadeIn-enter-active { animation: rd-fade-in 0.28s var(--ease-out); }
@keyframes rd-fade-in { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 900px) {
  .rd-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
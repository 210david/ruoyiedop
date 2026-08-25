<template>
  <div class="app-container wms-list-page">
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
          <label>规则编码</label>
          <div class="control">
            <el-input v-model="queryParams.ruleCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>规则名称</label>
          <div class="control">
            <el-input v-model="queryParams.ruleName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>重置类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.resetType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mk_number_reset_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in sys_normal_disable" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
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
      <div class="status-tabs">
        <div class="tabs-track">
          <span class="status-tab is-active"><span class="dot"></span><span>编号规则</span></span>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:numberRule:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:numberRule:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['mms:numberRule:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_numberRule_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="规则编码" prop="ruleCode" key="ruleCode" :width="colWidth('ruleCode', 180)" resizable v-if="columns.ruleCode.visible" />
          <el-table-column label="规则名称" prop="ruleName" key="ruleName" :width="colWidth('ruleName', 140)" resizable v-if="columns.ruleName.visible" />
          <el-table-column label="前缀" prop="prefix" key="prefix" :width="colWidth('prefix', 80)" resizable align="center" v-if="columns.prefix.visible">
            <template #default="scope">
              <span v-if="scope.row.prefix">{{ scope.row.prefix }}</span>
              <span class="badge green" v-else-if="scope.row.prefixFieldEnabled === '1'">动态</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="日期格式" prop="dateFormat" key="dateFormat" :width="colWidth('dateFormat', 120)" resizable align="center" v-if="columns.dateFormat.visible">
            <template #default="scope">{{ scope.row.dateFormat || '—' }}</template>
          </el-table-column>
          <el-table-column label="重置类型" prop="resetType" key="resetType" :width="colWidth('resetType', 100)" resizable align="center" v-if="columns.resetType.visible">
            <template #default="scope">
              <span v-if="scope.row.resetType" class="badge amber">{{ resetTypeLabel(scope.row.resetType) }}</span><span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="序列号长度" prop="seqLength" key="seqLength" :width="colWidth('seqLength', 110)" resizable align="center" v-if="columns.seqLength.visible" />
          <el-table-column label="当前序列号" prop="currentSeq" key="currentSeq" :width="colWidth('currentSeq', 110)" resizable align="center" v-if="columns.currentSeq.visible" />
          <el-table-column label="预览编号" prop="preview" key="preview" :width="colWidth('preview', 220)" resizable v-if="columns.preview.visible">
            <template #default="scope">{{ scope.row.preview }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span v-if="scope.row.status" class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" key="remark" :width="colWidth('remark', 200)" resizable show-overflow-tooltip v-if="columns.remark.visible" />
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['mms:numberRule:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:numberRule:edit']">修改</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改/查看 对话框 -->
    <el-dialog v-model="open" width="1032px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="9" x2="20" y2="9"/><line x1="4" y1="15" x2="20" y2="15"/><line x1="10" y1="3" x2="8" y2="21"/><line x1="16" y1="3" x2="14" y2="21"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.ruleCode">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编码：{{ form.ruleCode }}</span>
          </div>
        </div>
      </template>
      <el-form ref="ruleRef" :model="form" :rules="rules" label-width="110px" :disabled="isView">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
            <el-row>
              <el-col :span="12">
                <el-form-item label="所属模块">
                  <el-tag type="info">生产管控</el-tag>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="规则编码" prop="ruleCode">
                  <el-input v-model="form.ruleCode" placeholder="如：mms_work_order" :disabled="form.ruleId != undefined" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="规则名称" prop="ruleName">
                  <el-input v-model="form.ruleName" placeholder="如：工单编号" :disabled="form.ruleId != undefined" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status">
                    <el-radio v-for="d in sys_normal_disable" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>

        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('format')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></span>编号格式</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.format }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.format">
            <el-row>
              <el-col :span="24">
                <el-form-item label="序号重置类型" prop="resetType">
                  <el-radio-group v-model="form.resetType">
                    <el-radio v-for="d in mk_number_reset_type" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="编号前缀" prop="prefix">
                  <el-input v-model="form.prefix" placeholder="如：WO" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="连接符" prop="connector">
                  <el-input v-model="form.connector" placeholder="前缀与日期间连接符，如：-" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="日期格式" prop="dateFormat">
                  <el-select v-model="form.dateFormat" placeholder="请选择" :disabled="form.resetType !== '0' && form.resetType !== '4'" style="width: 100%">
                    <el-option label="不包含日期" value="" />
                    <el-option label="yyyy (按年)" value="yyyy" />
                    <el-option label="yyyyMM (按月)" value="yyyyMM" />
                    <el-option label="yyyyMMdd (按天)" value="yyyyMMdd" />
                  </el-select>
                  <span v-if="form.resetType !== '0' && form.resetType !== '4'" style="color: #909399; font-size: 12px;">由重置类型自动决定，不可手动修改</span>
                  <span v-if="form.resetType === '4'" style="color: #909399; font-size: 12px;">按动态前缀重置时可自由选择日期格式（默认按天），每个动态前缀维护独立序号并按日期重置</span>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>

        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('prefix')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>动态前缀配置</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.prefix }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.prefix">
            <el-row>
              <el-col :span="24">
                <el-form-item label="启用动态前缀" prop="prefixFieldEnabled">
                  <el-switch v-model="form.prefixFieldEnabled" active-value="1" inactive-value="0" />
                  <span style="margin-left: 10px; color: #909399; font-size: 12px;">启用后可根据业务字段值（如工单类型）自动设置不同前缀</span>
                </el-form-item>
              </el-col>
              <template v-if="form.prefixFieldEnabled === '1'">
                <el-col :span="12">
                  <el-form-item label="关联字段名" prop="prefixField">
                    <el-select v-model="form.prefixField" placeholder="请选择关联字段" style="width: 100%" @change="onPrefixFieldChange">
                      <el-option v-for="item in availablePrefixFields" :key="item.field" :label="`${item.label} (${item.field})`" :value="item.field" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="关联字典类型" prop="prefixFieldDictType">
                    <el-input v-model="form.prefixFieldDictType" placeholder="选择关联字段后自动带出" readonly />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="前缀映射">
                    <el-table :data="form.prefixList" border size="small" style="width: 100%">
                      <el-table-column label="字段值" prop="fieldValue" width="100" align="center">
                        <template #default="scope"><span>{{ scope.row.fieldValue }}</span></template>
                      </el-table-column>
                      <el-table-column label="字段标签" prop="fieldLabel" width="120" align="center">
                        <template #default="scope"><span>{{ scope.row.fieldLabel }}</span></template>
                      </el-table-column>
                      <el-table-column label="前缀" prop="prefix" align="center">
                        <template #default="scope"><el-input v-model="scope.row.prefix" placeholder="如：WO" size="small" /></template>
                      </el-table-column>
                      <el-table-column label="当前序号" prop="currentSeq" width="90" align="center">
                        <template #default="scope"><span>{{ scope.row.currentSeq || 0 }}</span></template>
                      </el-table-column>
                      <el-table-column label="启用" prop="enabled" width="80" align="center">
                        <template #default="scope"><el-switch v-model="scope.row.enabled" active-value="1" inactive-value="0" size="small" /></template>
                      </el-table-column>
                    </el-table>
                  </el-form-item>
                </el-col>
              </template>
            </el-row>
          </div>
        </section>

        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('seq')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>序列号设置</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.seq }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.seq">
            <el-row>
              <el-col :span="12">
                <el-form-item label="序列号长度" prop="seqLength">
                  <el-input-number v-model="form.seqLength" :min="1" :max="10" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="序列号起始值" prop="seqStart">
                  <el-input-number v-model="form.seqStart" :min="1" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="增长步长" prop="step">
                  <el-input-number v-model="form.step" :min="1" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="当前序列号">
                  <el-input-number v-model="form.currentSeq" :min="0" controls-position="right" style="width: 100%" :disabled="true" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>

        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('preview')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg></span>预览与备注</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.preview }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.preview">
            <el-row>
              <el-col :span="24">
                <el-form-item label="预览编号">
                  <el-tag type="primary" size="large">{{ previewText }}</el-tag>
                  <el-button link type="primary" style="margin-left: 10px" @click="refreshPreview" :disabled="!form.ruleCode || isView">刷新预览</el-button>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="备注" prop="remark">
                  <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm" v-if="!isView">确 定</el-button>
        <el-button @click="cancel">{{ isView ? '关 闭' : '取 消' }}</el-button>
      </template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="编号规则业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、编号规则释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是编号规则？</div>
          <div class="highlight-card-body">
            <strong>编号规则（Number Rule）</strong>是生产管控中定义各类业务单据自动编号规则的主数据。编号规则通过前缀、日期格式、序列号长度等配置，自动生成唯一的单据编号，确保编号的规范性和可追溯性。<br/><br/>
            编号规则遵循 <strong>系统编号管理规范</strong>，支持按规则编码区分不同业务单据类型，重置类型支持按年/月/日/永不重置，序列号长度确保编号的唯一性和可读性。
          </div>
        </div>
        <h4>二、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>创建规则：</strong>点击「新增」创建编号规则，填写规则编码、名称和前缀</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>配置参数：</strong>设置日期格式、序列号长度和重置类型</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>预览生效：</strong>预览生成的编号格式，启用后系统自动按规则生成编号</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="MmsNumberRule">
import { listCodeRule, getCodeRule, addCodeRule, updateCodeRule } from '@/api/mms/coderule'
import { getDicts } from '@/api/system/dict/data'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
import { ArrowDown, Filter, Search, Edit, Download, ArrowRight, WarningFilled } from '@element-plus/icons-vue'

const { collapsedCards, toggleCard } = useDetailCard(["basic","format","prefix","seq","preview"])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_numberRule_index')
const { proxy } = getCurrentInstance()
const { mk_number_reset_type, sys_normal_disable } = proxy.useDict('mk_number_reset_type', 'sys_normal_disable')

/** 固定为生产管控模块 */
const MODULE = 'mms'

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const total = ref(0)
const title = ref('')
const previewText = ref('')
const isView = ref(false)
const showStatusHelp = ref(false)

const defaultColumns = {
  ruleCode: { label: '规则编码', visible: true },
  ruleName: { label: '规则名称', visible: true },
  prefix: { label: '前缀', visible: true },
  dateFormat: { label: '日期格式', visible: true },
  resetType: { label: '重置类型', visible: true },
  seqLength: { label: '序列号长度', visible: true },
  currentSeq: { label: '当前序列号', visible: true },
  preview: { label: '预览编号', visible: true },
  status: { label: '状态', visible: true },
  remark: { label: '备注', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mms_numberRule_columns')
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
  if (queryParams.value.ruleCode) count++
  if (queryParams.value.ruleName) count++
  if (queryParams.value.resetType) count++
  if (queryParams.value.status !== undefined && queryParams.value.status !== null && queryParams.value.status !== '') count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function resetTypeLabel(type) { if (type === null || type === undefined || type === '') return '—'; const item = mk_number_reset_type.value.find(d => d.value == type); return item ? item.label : '—' }
function statusLabel(status) { if (status === null || status === undefined || status === '') return '—'; const item = sys_normal_disable.value.find(d => d.value == status); return item ? item.label : '—' }

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, module: MODULE, ruleCode: undefined, ruleName: undefined, resetType: undefined, status: undefined, params: {} },
  rules: {
    ruleCode: [{ required: true, message: '规则编码不能为空', trigger: 'blur' }],
    ruleName: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
    seqLength: [{ required: true, message: '序列号长度不能为空', trigger: 'blur' }],
    resetType: [{ required: true, message: '请选择重置类型', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  queryParams.value.module = MODULE
  proxy.addDateRange(queryParams.value, dateRange.value)
  listCodeRule(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }).catch(() => { loading.value = false })
}
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.ruleCode = undefined; queryParams.value.ruleName = undefined; queryParams.value.resetType = undefined; queryParams.value.status = undefined; dateRange.value = []; queryParams.value.params = {}
  queryParams.value.module = MODULE
  handleQuery()
}
function handleSelectionChange(selection) { ids.value = selection.map(i => i.ruleId); single.value = selection.length !== 1 }

/** 根据表单数据生成本地预览编号 */
function buildLocalPreview() {
  const fixedPrefix = form.value.prefix || ''
  let dynamicPrefix = ''
  if (form.value.prefixFieldEnabled === '1' && form.value.prefixList && form.value.prefixList.length > 0) {
    const firstEnabled = form.value.prefixList.find(p => p.enabled === '1' && p.prefix)
    if (firstEnabled) { dynamicPrefix = firstEnabled.prefix }
  }
  const connector = form.value.connector || ''
  let str = ''
  if (fixedPrefix) str += fixedPrefix
  if (dynamicPrefix) { if (fixedPrefix && connector) str += connector; str += dynamicPrefix }
  let dateStr = ''
  const resetType = form.value.resetType || '0'
  const dateFormat = form.value.dateFormat || ''
  if (resetType === '1') dateStr = formatNow('yyyyMMdd')
  else if (resetType === '2') dateStr = formatNow('yyyyMM')
  else if (resetType === '3') dateStr = formatNow('yyyy')
  else if (dateFormat) dateStr = formatNow(dateFormat)
  if (dateStr) { if ((fixedPrefix || dynamicPrefix) && connector) str += connector; str += dateStr }
  const seqLen = form.value.seqLength || 4
  const startVal = form.value.seqStart || 1
  str += String(startVal).padStart(seqLen, '0')
  previewText.value = str
}

function formatNow(pattern) {
  const now = new Date()
  const map = { yyyy: now.getFullYear(), MM: String(now.getMonth() + 1).padStart(2, '0'), dd: String(now.getDate()).padStart(2, '0') }
  return pattern.replace(/yyyy|MM|dd/g, m => map[m])
}

function refreshPreview() { buildLocalPreview() }

function reset() {
  form.value = { module: MODULE, ruleCode: undefined, ruleName: undefined, prefix: undefined, dateFormat: 'yyyyMMdd', resetType: '1', seqLength: 4, seqStart: 1, step: 1, connector: '-', status: '0', prefixFieldEnabled: '0', prefixField: undefined, prefixFieldDictType: undefined, prefixList: [], remark: undefined }
  previewText.value = ''
  proxy.resetForm('ruleRef')
}

function handleAdd() { reset(); isView.value = false; open.value = true; title.value = '新增生产管控编号规则' }
function handleUpdate(row) {
  reset()
  isView.value = false
  getCodeRule(row.ruleId || ids.value[0]).then(res => {
    form.value = res.data
    open.value = true
    title.value = `修改${res.data.ruleName}`
    nextTick(() => { buildLocalPreview() })
  })
}
function handleView(row) {
  reset()
  isView.value = true
  getCodeRule(row.ruleId).then(res => {
    form.value = res.data
    open.value = true
    title.value = `查看${res.data.ruleName}`
    nextTick(() => { buildLocalPreview() })
  })
}
function submitForm() {
  proxy.$refs['ruleRef'].validate(valid => {
    if (valid) {
      form.value.module = MODULE
      if (form.value.resetType === '4') {
        if (form.value.prefixFieldEnabled !== '1') { proxy.$modal.msgError('序号重置类型为"按动态前缀"时，必须启用动态前缀配置'); return }
        if (!form.value.prefixField) { proxy.$modal.msgError('请选择动态前缀的关联字段名'); return }
        if (!form.value.prefixList || form.value.prefixList.length === 0) { proxy.$modal.msgError('按动态前缀重置时，至少需要配置一个前缀映射'); return }
      }
      if (form.value.ruleId != undefined) {
        updateCodeRule(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addCodeRule(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}

/** 规则编码 → 该业务表可选的字典字段列表 */
const ruleFieldMap = {
  mms_work_order: [
    { field: 'orderType', label: '工单类型', dictType: 'mms_order_type' },
    { field: 'priority', label: '优先级', dictType: 'mms_priority' }
  ],
  mms_mps: [
    { field: 'planType', label: '计划类型', dictType: 'mms_plan_type' }
  ],
  mms_demand: [
    { field: 'demandType', label: '需求类型', dictType: 'mms_demand_type' }
  ],
  mms_issue: [
    { field: 'issueType', label: '发料类型', dictType: 'mms_issue_type' }
  ],
  mms_return_material: [
    { field: 'returnType', label: '退料类型', dictType: 'mms_return_type' }
  ],
  mms_qc: [
    { field: 'qcType', label: '质检类型', dictType: 'mms_qc_type' }
  ],
  mms_abnormal: [
    { field: 'abnormalType', label: '异常类型', dictType: 'mms_abnormal_type' },
    { field: 'abnormalLevel', label: '异常等级', dictType: 'mms_abnormal_level' }
  ],
  mms_finish_receipt: [
    { field: 'receiptType', label: '入库类型', dictType: 'mms_receipt_type' }
  ],
  mms_kit_check: [
    { field: 'checkType', label: '检查类型', dictType: 'mms_check_type' }
  ],
  mms_work_report: [
    { field: 'reportType', label: '报工类型', dictType: 'mms_report_type' }
  ],
  mms_process: [
    { field: 'processType', label: '工序类型', dictType: 'mms_process_type' }
  ]
}

const availablePrefixFields = computed(() => {
  if (!form.value.ruleCode) return []
  return ruleFieldMap[form.value.ruleCode] || []
})

function onPrefixFieldChange(val) {
  const fieldConfig = availablePrefixFields.value.find(f => f.field === val)
  form.value.prefixFieldDictType = fieldConfig ? fieldConfig.dictType : ''
  if (form.value.prefixFieldDictType) { loadDictToPrefixList() } else { form.value.prefixList = [] }
}

function loadDictToPrefixList() {
  if (!form.value.prefixFieldDictType) { return }
  getDicts(form.value.prefixFieldDictType).then(res => {
    const dictData = res.data || []
    const existingMap = {}
    if (form.value.prefixList) {
      form.value.prefixList.forEach(item => { existingMap[item.fieldValue] = item })
    }
    form.value.prefixList = dictData.map(d => {
      const existing = existingMap[d.dictValue]
      return {
        fieldValue: d.dictValue,
        fieldLabel: d.dictLabel,
        prefix: existing ? existing.prefix : '',
        enabled: existing ? existing.enabled : '1'
      }
    })
  })
}

function handleExport() { proxy.download('mms/numberRule/export', { ...queryParams.value }, `numberRule_${MODULE}_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset(); isView.value = false }

// 监听重置类型变化，自动设置日期格式
watch(() => form.value.resetType, (newVal) => {
  if (newVal === '0') { form.value.dateFormat = '' }
  else if (newVal === '1') { form.value.dateFormat = 'yyyyMMdd' }
  else if (newVal === '2') { form.value.dateFormat = 'yyyyMM' }
  else if (newVal === '3') { form.value.dateFormat = 'yyyy' }
  else if (newVal === '4') { form.value.dateFormat = 'yyyyMMdd' }
})

// 监听表单变化以实时更新预览
watch(() => form.value, () => { buildLocalPreview() }, { deep: true })

getList()
onActivated(() => { getList() })
</script>
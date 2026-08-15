<template>
  <div class="app-container mk-number-rule-page">
    <el-alert v-if="presetRuleCode" :title="presetAlertText" type="info" :closable="false" show-icon style="margin-bottom: 8px" />
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
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:numberRule:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:numberRule:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:numberRule:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_number_rule_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="规则编码" prop="ruleCode" key="ruleCode" :width="colWidth('ruleCode', 160)" resizable v-if="columns.ruleCode.visible" />
          <el-table-column label="所属模块" prop="module" key="module" :width="colWidth('module', 100)" resizable align="center" v-if="columns.module.visible">
            <template #default="scope">
              <span class="badge violet">{{ moduleLabel(scope.row.module) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="规则名称" prop="ruleName" key="ruleName" :width="colWidth('ruleName', 140)" resizable v-if="columns.ruleName.visible" />
          <el-table-column label="前缀" prop="prefix" key="prefix" :width="colWidth('prefix', 80)" resizable align="center" v-if="columns.prefix.visible">
            <template #default="scope">
              <span v-if="scope.row.prefix">{{ scope.row.prefix }}</span>
              <span class="badge green" v-else-if="scope.row.prefixFieldEnabled === '1'">动态</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="日期格式" prop="dateFormat" key="dateFormat" :width="colWidth('dateFormat', 120)" resizable align="center" v-if="columns.dateFormat.visible">
            <template #default="scope">{{ scope.row.dateFormat || '-' }}</template>
          </el-table-column>
          <el-table-column label="重置类型" prop="resetType" key="resetType" :width="colWidth('resetType', 100)" resizable align="center" v-if="columns.resetType.visible">
            <template #default="scope">
              <span class="badge amber">{{ resetTypeLabel(scope.row.resetType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="序列号长度" prop="seqLength" key="seqLength" :width="colWidth('seqLength', 100)" resizable align="center" v-if="columns.seqLength.visible" />
          <el-table-column label="当前序列号" prop="currentSeq" key="currentSeq" :width="colWidth('currentSeq', 100)" resizable align="center" v-if="columns.currentSeq.visible" />
          <el-table-column label="预览编号" prop="preview" key="preview" :width="colWidth('preview', 200)" resizable v-if="columns.preview.visible">
            <template #default="scope">{{ scope.row.preview }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" key="remark" :width="colWidth('remark', 180)" resizable show-overflow-tooltip v-if="columns.remark.visible" />
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['marketing:numberRule:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:numberRule:edit']">修改</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改/查看 对话框 -->
    <el-dialog v-model="open" width="900px" append-to-body draggable class="rd-dialog">
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
      <el-form ref="ruleRef" :model="form" :rules="rules" label-width="120px" :disabled="isView">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
            <el-row>
              <el-col :span="12">
                <el-form-item label="所属模块" prop="module">
                  <el-select v-model="form.module" placeholder="请选择" :disabled="form.ruleId != undefined" style="width: 100%">
                    <el-option label="营销管理" value="mk" />
                    <el-option label="设备管理" value="dms" />
                    <el-option label="仓储管理" value="wms" />
                    <el-option label="采购管理" value="pms" />
                    <el-option label="安全生产管理" value="safety" />
                    <el-option label="质量管理" value="qms" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="规则编码" prop="ruleCode">
                  <el-input v-model="form.ruleCode" placeholder="如：activity" :disabled="form.ruleId != undefined" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="规则名称" prop="ruleName">
                  <el-input v-model="form.ruleName" placeholder="如：活动编号" :disabled="form.ruleId != undefined" />
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
                  <el-input v-model="form.prefix" placeholder="如：HD" />
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
                  <span style="margin-left: 10px; color: #909399; font-size: 12px;">启用后可根据业务字段值（如物料类型）自动设置不同前缀</span>
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
                        <template #default="scope">
                          <span>{{ scope.row.fieldValue }}</span>
                        </template>
                      </el-table-column>
                      <el-table-column label="字段标签" prop="fieldLabel" width="120" align="center">
                        <template #default="scope">
                          <span>{{ scope.row.fieldLabel }}</span>
                        </template>
                      </el-table-column>
                      <el-table-column label="前缀" prop="prefix" align="center">
                        <template #default="scope">
                          <el-input v-model="scope.row.prefix" placeholder="如：YL" size="small" />
                        </template>
                      </el-table-column>
                      <el-table-column label="当前序号" prop="currentSeq" width="90" align="center">
                        <template #default="scope">
                          <span>{{ scope.row.currentSeq || 0 }}</span>
                        </template>
                      </el-table-column>
                      <el-table-column label="启用" prop="enabled" width="80" align="center">
                        <template #default="scope">
                          <el-switch v-model="scope.row.enabled" active-value="1" inactive-value="0" size="small" />
                        </template>
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
  </div>
</template>

<script setup name="MkNumberRule">
import { useRoute } from 'vue-router'
import { listNumberRule, getNumberRule, addNumberRule, updateNumberRule } from '@/api/mk/numberRule'
import { getDicts } from '@/api/system/dict/data'
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, Filter, RefreshLeft, ArrowDown } from '@element-plus/icons-vue'

const route = useRoute()
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["basic","format","prefix","seq","preview"])
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_number_rule_index')
const { mk_number_reset_type, sys_normal_disable } = proxy.useDict('mk_number_reset_type', 'sys_normal_disable')

/** 从路由参数获取当前模块（mk/dms/wms），默认为 mk */
const currentModule = computed(() => route.query.module || 'mk')

/** 从路由参数获取预设规则编码（用于从特定业务模块跳转过来时预过滤） */
const presetRuleCode = computed(() => route.query.ruleCode || '')

/** 模块名称映射 */
const moduleNames = { mk: '营销管理', dms: '设备管理', wms: '仓储管理', pms: '采购管理', safety: '安全生产管理', qms: '质量管理' }
const currentModuleName = computed(() => moduleNames[currentModule.value] || '编号规则')

/** 预设规则提示文案 */
const presetAlertText = computed(() => {
  const rule = list.value.find(r => r.ruleCode === presetRuleCode.value)
  const ruleName = rule ? rule.ruleName : presetRuleCode.value
  return `当前显示「${ruleName}」的编号规则配置，清除规则编码筛选可查看全部${currentModuleName.value}编号规则`
})

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

const defaultColumns = {
  ruleCode: { label: '规则编码', visible: true },
  module: { label: '所属模块', visible: true },
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
    const saved = localStorage.getItem('mk_number_rule_columns')
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

function moduleLabel(val) {
  const map = { mk: '营销管理', dms: '设备管理', wms: '仓储管理', pms: '采购管理', safety: '安全生产管理' }
  return map[val] || val || '-'
}
function resetTypeLabel(val) {
  const item = mk_number_reset_type.value.find(d => d.value == val)
  return item ? item.label : '-'
}
function statusLabel(val) {
  const item = sys_normal_disable.value.find(d => d.value == val)
  return item ? item.label : '-'
}
const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, ruleCode: undefined, ruleName: undefined, resetType: undefined, status: undefined, params: {} },
  rules: {
    module: [{ required: true, message: '请选择所属模块', trigger: 'change' }],
    ruleCode: [{ required: true, message: '规则编码不能为空', trigger: 'blur' }],
    ruleName: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
    prefix: [],
    seqLength: [{ required: true, message: '序列号长度不能为空', trigger: 'blur' }],
    resetType: [{ required: true, message: '请选择重置类型', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 设置页面标题 */
useHead()

function getList() {
  loading.value = true
  queryParams.value.module = currentModule.value
  if (presetRuleCode.value) {
    queryParams.value.ruleCode = presetRuleCode.value
  }
  proxy.addDateRange(queryParams.value, dateRange.value)
  listNumberRule(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }).catch(() => { loading.value = false })
}
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.ruleCode = undefined; queryParams.value.ruleName = undefined; queryParams.value.resetType = undefined; queryParams.value.status = undefined; dateRange.value = []; queryParams.value.params = {}
  queryParams.value.module = currentModule.value
  if (presetRuleCode.value) {
    queryParams.value.ruleCode = presetRuleCode.value
  }
  handleQuery()
}
function handleSelectionChange(selection) { ids.value = selection.map(i => i.ruleId); single.value = selection.length !== 1 }

/** 根据表单数据生成本地预览编号 */
function buildLocalPreview() {
  const fixedPrefix = form.value.prefix || ''
  // 启用动态前缀时，取第一个启用的前缀作为预览示例
  let dynamicPrefix = ''
  if (form.value.prefixFieldEnabled === '1' && form.value.prefixList && form.value.prefixList.length > 0) {
    const firstEnabled = form.value.prefixList.find(p => p.enabled === '1' && p.prefix)
    if (firstEnabled) {
      dynamicPrefix = firstEnabled.prefix
    }
  }
  const connector = form.value.connector || ''
  // 拼装编号：固定前缀 + 连接符 + 动态前缀 + 连接符 + 日期 + 序列号
  let str = ''
  if (fixedPrefix) str += fixedPrefix
  if (dynamicPrefix) {
    if (fixedPrefix && connector) str += connector
    str += dynamicPrefix
  }
  let dateStr = ''
  const resetType = form.value.resetType || '0'
  const dateFormat = form.value.dateFormat || ''
  if (resetType === '1') dateStr = formatNow('yyyyMMdd')
  else if (resetType === '2') dateStr = formatNow('yyyyMM')
  else if (resetType === '3') dateStr = formatNow('yyyy')
  else if (dateFormat) dateStr = formatNow(dateFormat)
  if (dateStr) {
    if ((fixedPrefix || dynamicPrefix) && connector) str += connector
    str += dateStr
  }
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

/** 刷新预览：始终用本地表单数据实时计算 */
function refreshPreview() {
  buildLocalPreview()
}

function reset() {
  form.value = { module: currentModule.value, ruleCode: undefined, ruleName: undefined, prefix: undefined, dateFormat: 'yyyyMMdd', resetType: '1', seqLength: 4, seqStart: 1, step: 1, connector: '', status: '0', prefixFieldEnabled: '0', prefixField: undefined, prefixFieldDictType: undefined, prefixList: [], remark: undefined }
  previewText.value = ''
  proxy.resetForm('ruleRef')
}

function useHead() {
  watchEffect(() => {
    document.title = `${currentModuleName.value} - 编号规则`
  })
}

function handleAdd() { reset(); isView.value = false; open.value = true; title.value = `新增${currentModuleName.value}编号规则` }
function handleUpdate(row) {
  reset()
  isView.value = false
  getNumberRule(row.ruleId || ids.value[0]).then(res => {
    form.value = res.data
    open.value = true
    title.value = `修改${res.data.ruleName}`
    nextTick(() => { buildLocalPreview() })
  })
}
function handleView(row) {
  reset()
  isView.value = true
  getNumberRule(row.ruleId).then(res => {
    form.value = res.data
    open.value = true
    title.value = `查看${res.data.ruleName}`
    nextTick(() => { buildLocalPreview() })
  })
}
function submitForm() {
  proxy.$refs['ruleRef'].validate(valid => {
    if (valid) {
      // 按动态前缀重置时，必须启用动态前缀并选择关联字段
      if (form.value.resetType === '4') {
        if (form.value.prefixFieldEnabled !== '1') {
          proxy.$modal.msgError('序号重置类型为"按动态前缀"时，必须启用动态前缀配置')
          return
        }
        if (!form.value.prefixField) {
          proxy.$modal.msgError('请选择动态前缀的关联字段名')
          return
        }
        if (!form.value.prefixList || form.value.prefixList.length === 0) {
          proxy.$modal.msgError('按动态前缀重置时，至少需要配置一个前缀映射')
          return
        }
      }
      if (form.value.ruleId != undefined) {
        updateNumberRule(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addNumberRule(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}
/** 规则编码 → 该业务表可选的字典字段列表 */
const ruleFieldMap = {
  // 物料：物料类型、计量单位
  wms_material: [
    { field: 'materialType', label: '物料类型', dictType: 'wms_material_type' },
    { field: 'unit', label: '计量单位', dictType: 'wms_unit' }
  ],
  // 仓库：仓库类型、库区类型、仓位类型
  wms_warehouse: [
    { field: 'areaType', label: '库区类型', dictType: 'wms_area_type' },
    { field: 'locationType', label: '仓位类型', dictType: 'wms_location_type' }
  ],
  // 供应商：供应商类型
  wms_supplier: [
    { field: 'supplierType', label: '供应商类型', dictType: 'wms_supplier_type' }
  ],
  // 入库单：入库类型
  wms_inbound: [
    { field: 'inboundType', label: '入库类型', dictType: 'wms_inbound_type' }
  ],
  // 出库单：出库类型
  wms_outbound: [
    { field: 'outboundType', label: '出库类型', dictType: 'wms_outbound_type' }
  ],
  // 设备：设备状态
  dms_equipment: [
    { field: 'equipmentStatus', label: '设备状态', dictType: 'dms_equipment_status' }
  ],
  // 备件：备件类型、计量单位
  dms_spare_part: [
    { field: 'partType', label: '备件类型', dictType: 'dms_part_type' },
    { field: 'unit', label: '计量单位', dictType: 'wms_unit' }
  ],
  // 工单：工单类型、优先级
  dms_work_order: [
    { field: 'orderType', label: '工单类型', dictType: 'dms_order_type' },
    { field: 'priority', label: '优先级', dictType: 'dms_priority' }
  ],
  // 备件入库：入库类型
  dms_spare_in: [
    { field: 'partinType', label: '入库类型', dictType: 'dms_partin_type' }
  ],
  // 备件出库：出库类型
  dms_spare_out: [
    { field: 'partoutType', label: '出库类型', dictType: 'dms_partout_type' }
  ],
  // 巡检路线：巡检周期
  dms_inspection_route: [
    { field: 'cycle', label: '巡检周期', dictType: 'dms_inspection_cycle' }
  ],
  // 安全区域：区域类型
  safety_area: [
    { field: 'areaType', label: '区域类型', dictType: 'safety_area_type' }
  ],
  // 特种设备：设备类别
  safety_equipment: [
    { field: 'equipmentCategory', label: '设备类别', dictType: 'safety_equipment_category' }
  ],
  // 风险点：风险等级
  safety_risk_point: [
    { field: 'riskLevel', label: '风险等级', dictType: 'safety_risk_level' }
  ],
  // 隐患：隐患等级
  safety_hazard: [
    { field: 'hazardLevel', label: '隐患等级', dictType: 'safety_hazard_level' },
    { field: 'hazardType', label: '隐患类型', dictType: 'safety_hazard_type' }
  ],
  // 应急演练：演练类型
  safety_drill: [
    { field: 'drillType', label: '演练类型', dictType: 'safety_drill_type' }
  ],
  // 事故：事故等级
  safety_incident: [
    { field: 'incidentLevel', label: '事故等级', dictType: 'safety_incident_level' }
  ],
  // 应急物资：物资类型
  safety_emergency_material: [
    { field: 'materialType', label: '物资类型', dictType: 'safety_material_type' }
  ],
  // 应急预案：预案类型
  safety_emergency_plan: [
    { field: 'planType', label: '预案类型', dictType: 'safety_plan_type' }
  ],
  // 培训计划：培训类型
  safety_training_plan: [
    { field: 'planType', label: '培训类型', dictType: 'safety_training_type' }
  ]
}

/** 根据当前规则编码计算可选的关联字段 */
const availablePrefixFields = computed(() => {
  if (!form.value.ruleCode) return []
  return ruleFieldMap[form.value.ruleCode] || []
})

/** 关联字段名变化时，自动设置字典类型并加载前缀映射 */
function onPrefixFieldChange(val) {
  // 根据选中的字段找到对应的字典类型
  const fieldConfig = availablePrefixFields.value.find(f => f.field === val)
  form.value.prefixFieldDictType = fieldConfig ? fieldConfig.dictType : ''
  // 自动加载前缀映射
  if (form.value.prefixFieldDictType) {
    loadDictToPrefixList()
  } else {
    form.value.prefixList = []
  }
}

/** 从字典加载选项到前缀映射列表 */
function loadDictToPrefixList() {
  if (!form.value.prefixFieldDictType) {
    return
  }
  getDicts(form.value.prefixFieldDictType).then(res => {
    const dictData = res.data || []
    const existingMap = {}
    if (form.value.prefixList) {
      form.value.prefixList.forEach(item => {
        existingMap[item.fieldValue] = item
      })
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

function handleExport() { proxy.download('mk/numberRule/export', { ...queryParams.value }, `numberRule_${currentModule.value}_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset(); isView.value = false }

// 监听重置类型变化，自动设置日期格式
watch(() => form.value.resetType, (newVal) => {
  if (newVal === '0') {
    form.value.dateFormat = ''
  } else if (newVal === '1') {
    form.value.dateFormat = 'yyyyMMdd'
  } else if (newVal === '2') {
    form.value.dateFormat = 'yyyyMM'
  } else if (newVal === '3') {
    form.value.dateFormat = 'yyyy'
  } else if (newVal === '4') {
    // 按动态前缀重置：默认不包含日期，用户可自行选择
    form.value.dateFormat = ''
  }
})

// 监听表单变化以实时更新预览
watch(() => form.value, () => { buildLocalPreview() }, { deep: true })

// 监听路由参数变化，重新加载列表
watch([() => route.query.module, () => route.query.ruleCode], () => { getList() })

getList()
</script>

<style scoped>
.mk-number-rule-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}
.mk-number-rule-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.mk-number-rule-page .filter-card { padding:14px 20px 16px; }
.mk-number-rule-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.mk-number-rule-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.mk-number-rule-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.mk-number-rule-page .filter-card .adv-link { display:inline-flex; align-items:center; gap:4px; font-size:13px; color:var(--ink-500); cursor:pointer; user-select:none; transition:color .15s var(--ease-out); }
.mk-number-rule-page .filter-card .adv-link:hover { color:var(--brand-600); }
.mk-number-rule-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.mk-number-rule-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.mk-number-rule-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.mk-number-rule-page .filter-card .field { display:flex; flex-direction:column; gap:4px; }
.mk-number-rule-page .filter-card .field > label { font-size:13px; font-weight:500; color:var(--ink-500); }
.mk-number-rule-page .filter-card .field .control { width:100%; }
.mk-number-rule-page .filter-card .field .control .el-input, .mk-number-rule-page .filter-card .field .control .el-select { width:100%; }
.mk-number-rule-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.mk-number-rule-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.mk-number-rule-page .filter-card .filter-buttons { display:flex; gap:8px; }
.mk-number-rule-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:10px 16px; border-bottom:1px solid var(--ink-200); }
.mk-number-rule-page .toolbar .left { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.mk-number-rule-page .toolbar .right { display:flex; align-items:center; gap:8px; }
.mk-number-rule-page .toolbar-divider { width:1px; height:20px; background:var(--ink-200); margin:0 4px; }
.mk-number-rule-page .btn-soft { display:inline-flex; align-items:center; gap:4px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; cursor:pointer; transition:all .15s var(--ease-out); border:1px solid transparent; background:transparent; color:var(--ink-700); }
.mk-number-rule-page .btn-soft.is-outline { border-color:var(--ink-300); background:#fff; }
.mk-number-rule-page .btn-soft.is-outline:hover { border-color:var(--brand-400); color:var(--brand-600); background:var(--brand-50); }
.mk-number-rule-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.mk-number-rule-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }
.mk-number-rule-page .table-wrap { overflow-x:auto; }
.mk-number-rule-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.mk-number-rule-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.mk-number-rule-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.mk-number-rule-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.mk-number-rule-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.mk-number-rule-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.mk-number-rule-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.mk-number-rule-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.mk-number-rule-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.mk-number-rule-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.mk-number-rule-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.mk-number-rule-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.mk-number-rule-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.mk-number-rule-page .badge.amber .dot { background:var(--amber-500); }
.mk-number-rule-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.mk-number-rule-page .badge.blue .dot { background:var(--blue-500); }
.mk-number-rule-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.mk-number-rule-page .badge.green .dot { background:var(--green-500); }
.mk-number-rule-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.mk-number-rule-page .badge.red .dot { background:var(--red-500); }
.mk-number-rule-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.mk-number-rule-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.mk-number-rule-page .badge.gray .dot { background:var(--ink-400); }
@media (max-width:1100px) { .mk-number-rule-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .mk-number-rule-page .filter-card .filter-bar { grid-template-columns:1fr; } .mk-number-rule-page .toolbar { flex-wrap:wrap; gap:10px; } }
.mk-number-rule-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.mk-number-rule-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.mk-number-rule-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.mk-number-rule-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.mk-number-rule-page .pagination-container :deep(.el-pagination .btn-prev), .mk-number-rule-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.mk-number-rule-page .pagination-container :deep(.el-pagination .btn-prev:hover), .mk-number-rule-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.mk-number-rule-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
</style>

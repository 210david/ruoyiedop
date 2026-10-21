<template>
  <div class="ffe-wrap">
    <!-- 主表/明细表页签 -->
    <div class="field-tab-bar">
      <el-tabs v-model="activeTableTab" type="card" class="table-tabs" @tab-remove="removeDetailTable">
        <el-tab-pane name="main" :closable="false">
          <template #label><span>主表 <span class="tab-tbl">( {{ mainTableName }} )</span></span></template>
        </el-tab-pane>
        <el-tab-pane v-for="dt in detailTables" :key="dt" :name="dt" :closable="true">
          <template #label><span>明细表{{ dt.slice(2) }} <span class="tab-tbl">( {{ mainTableName + '_' + dt }} )</span></span></template>
        </el-tab-pane>
        <el-tab-pane name="__add" :closable="false">
          <template #label><span class="tab-add">＋</span></template>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 字段工具栏 -->
    <div class="field-toolbar">
      <el-button size="small" type="primary" plain icon="Plus" @click="openAddField">添加字段</el-button>
      <el-button size="small" type="primary" plain icon="DocumentAdd" @click="openBatchAdd">批量添加</el-button>
      <el-button size="small" plain icon="Delete" :disabled="!checkedFields.length" @click="removeCheckedFields">删除选中</el-button>
      <el-button size="small" type="warning" plain icon="Check" :loading="saving" @click="saveFields">保存字段设计</el-button>
      <span class="field-tip">当前表：{{ activeTableTab === 'main' ? mainTableName : mainTableName + '_' + activeTableTab }}<template v-if="activeTableTab !== 'main'">（明细表通过 mainid 字段关联主表主键，生成数据表时自动创建）</template></span>
    </div>

    <!-- 字段表格（前端分页：字段多时翻页显示，避免长列表不友好） -->
    <el-table ref="fieldTableRef" :data="pagedFields" border size="small" row-key="rowKey" @selection-change="onFieldSelChange" class="field-table" max-height="380">
      <el-table-column width="36" align="center"><template #default><span class="drag-handle" title="拖动排序">☰</span></template></el-table-column>
      <el-table-column type="selection" width="45" align="center" reserve-selection :selectable="row => !row.sysField" />
      <el-table-column label="字段显示名" min-width="130" align="center"><template #default="scope"><el-input v-model="scope.row.label" placeholder="如：合同名称" size="small" /></template></el-table-column>
      <el-table-column label="数据库字段名称" min-width="130" align="center"><template #default="scope"><el-input v-model="scope.row.field" placeholder="小写英文，如 htmc" size="small" /></template></el-table-column>
      <el-table-column label="表现形式" width="130" align="center">
        <template #default="scope">
          <el-select v-model="scope.row.form" size="small" @change="onFormChange(scope.row)">
            <el-option-group label="选择框">
              <el-option v-for="(v, k) in SELECT_FORM_OPTIONS" :key="k" :label="v" :value="k" />
            </el-option-group>
            <el-option v-for="(v, k) in FORM_OPTIONS" :key="k" :label="v" :value="k" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="110" align="center">
        <template #default="scope">
          <span v-if="isDtypeLocked(scope.row.form)" style="color:#909399">{{ DTYPE_OPTIONS[scope.row.dtype] || scope.row.dtype }}（自动）</span>
          <el-select v-else v-model="scope.row.dtype" size="small">
            <el-option v-for="(v, k) in dtypeOptions(scope.row.form)" :key="k" :label="v" :value="k" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="文本长度" width="100" align="center">
        <template #default="scope"><el-input-number v-if="scope.row.dtype === 'text' && !isLengthLocked(scope.row)" v-model="scope.row.length" :min="1" :max="8000" :controls="false" size="small" style="width:100%" /><span v-else style="color:#cbd5e1">自动</span></template>
      </el-table-column>
      <el-table-column label="格式/配置" min-width="150" align="center">
        <template #default="scope">
          <el-input-number v-if="scope.row.form === 'textarea'" v-model="scope.row.rows" :min="1" :max="20" :controls="false" size="small" style="width:80px" />
          <el-select v-else-if="scope.row.form === 'browse'" v-model="scope.row.browseType" size="small" style="width:100%">
            <el-option v-for="(v, k) in BROWSE_TYPES" :key="k" :label="v" :value="k" />
          </el-select>
          <div v-else-if="scope.row.form === 'attach'" style="display:flex;gap:4px">
            <el-select v-model="scope.row.attachType" size="small" style="flex:1">
              <el-option v-for="(v, k) in ATTACH_TYPES" :key="k" :label="v" :value="k" />
            </el-select>
            <el-input-number v-model="scope.row.attachLimit" :min="1" :max="20" :controls="false" size="small" style="width:64px" />
          </div>
          <el-button v-else-if="isSelForm(scope.row.form)" link type="primary" size="small" @click="openSelConfig(scope.row)">配置</el-button>
          <span v-else style="color:#cbd5e1">—</span>
        </template>
      </el-table-column>
      <el-table-column label="小数位数" width="95" align="center">
        <template #default="scope"><el-input-number v-if="scope.row.dtype === 'float'" v-model="scope.row.scale" :min="1" :max="6" :controls="false" size="small" style="width:100%" /><span v-else style="color:#cbd5e1">—</span></template>
      </el-table-column>
      <el-table-column label="填写提示" min-width="120" align="center"><template #default="scope"><el-input v-model="scope.row.placeholder" placeholder="如：请输入合同名称" size="small" /></template></el-table-column>
      <el-table-column label="显示顺序" width="100" align="center"><template #default="scope"><el-input-number v-model="scope.row.order" :min="1" :max="999" :controls="false" size="small" style="width:100%" /></template></el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="fieldPage"
      v-model:page-size="fieldPageSize"
      :total="activeFields.length"
      :page-sizes="[10, 20, 50, 100]"
      small
      layout="total, sizes, prev, pager, next"
      class="field-pagination"
      style="margin-top:8px;justify-content:flex-end"
    />
  </div>

  <!-- 添加字段弹窗 -->
  <el-dialog v-model="addFieldOpen" width="520px" append-to-body draggable class="rd-dialog" title="添加字段" :close-on-click-modal="false">
    <el-form ref="addFieldRef" :model="newField" :rules="fieldRules" label-width="120px" size="small">
      <el-form-item label="字段显示名" prop="label"><el-input v-model="newField.label" placeholder="如：合同名称" @input="autoFillFieldName(newField, newField.label)" /></el-form-item>
      <el-form-item label="数据库字段名称" prop="field">
        <el-input v-model="newField.field" placeholder="小写英文，如 htmc（自动取首字母，可修改）" @input="newField.autoField = false" />
        <div class="form-tip">仅允许小写字母开头的字母/数字/下划线</div>
      </el-form-item>
      <el-form-item label="字段位置">
        <el-select v-model="newField.table" style="width:100%">
          <el-option label="主表" value="main" />
          <el-option v-for="dt in detailTables" :key="dt" :label="'明细表' + dt.slice(2) + '（' + mainTableName + '_' + dt + '）'" :value="dt" />
        </el-select>
      </el-form-item>
      <el-form-item label="显示顺序"><el-input-number v-model="newField.order" :min="1" :max="999" :controls="false" style="width:160px" /></el-form-item>
      <el-form-item label="表现形式">
        <el-select v-model="newField.form" style="width:100%" @change="onNewFormChange">
          <el-option-group label="选择框">
            <el-option v-for="(v, k) in SELECT_FORM_OPTIONS" :key="k" :label="v" :value="k" />
          </el-option-group>
          <el-option v-for="(v, k) in FORM_OPTIONS" :key="k" :label="v" :value="k" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型">
        <span v-if="isDtypeLocked(newField.form)" style="color:#909399">{{ DTYPE_OPTIONS[newField.dtype] || newField.dtype }}（随表现形式自动）</span>
        <el-select v-else v-model="newField.dtype" style="width:100%">
          <el-option v-for="(v, k) in dtypeOptions(newField.form)" :key="k" :label="v" :value="k" />
        </el-select>
      </el-form-item>
      <el-form-item label="文本长度" v-if="newField.dtype === 'text' && !isLengthLocked(newField)"><el-input-number v-model="newField.length" :min="1" :max="8000" :controls="false" style="width:160px" /></el-form-item>
      <el-form-item label="多行行数" v-if="newField.form === 'textarea'"><el-input-number v-model="newField.rows" :min="1" :max="20" :controls="false" style="width:160px" /><div class="form-tip">默认4行，可自定义1-20行</div></el-form-item>
      <el-form-item label="小数位数" v-if="newField.dtype === 'float'"><el-input-number v-model="newField.scale" :min="1" :max="6" :controls="false" style="width:160px" /></el-form-item>
      <el-form-item label="浏览数据源" v-if="newField.form === 'browse'">
        <el-select v-model="newField.browseType" style="width:100%">
          <el-option v-for="(v, k) in BROWSE_TYPES" :key="k" :label="v" :value="k" />
        </el-select>
        <div class="form-tip">人员/部门/岗位/物料/客户/供应商等主数据，选择"自定义"可在数据表扩展</div>
      </el-form-item>
      <el-form-item label="附件类型" v-if="newField.form === 'attach'">
        <div style="display:flex;gap:8px;width:100%">
          <el-select v-model="newField.attachType" style="flex:1">
            <el-option v-for="(v, k) in ATTACH_TYPES" :key="k" :label="v" :value="k" />
          </el-select>
          <el-input-number v-model="newField.attachLimit" :min="1" :max="20" :controls="false" style="width:100px" />
        </div>
        <div class="form-tip">类型约束+最大数量</div>
      </el-form-item>
      <el-form-item label="填写提示">
        <el-input v-model="newField.placeholder" placeholder="如：请输入合同名称（为空时显示系统默认提示）" />
        <div class="form-tip">作为输入框背景占位文字，用户输入后自动消失</div>
      </el-form-item>
      <el-form-item label="选项配置" v-if="isSelForm(newField.form)">
        <el-button type="primary" plain icon="Setting" size="small" @click="openSelConfig(newField)">配置选项</el-button>
        <span class="form-tip" style="margin-left:8px">设置显示值/实际值及排序，{{ SELECT_FORM_OPTIONS[newField.form] }}</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="confirmAddField">确 定</el-button>
      <el-button @click="addFieldOpen = false">取 消</el-button>
    </template>
  </el-dialog>

  <!-- 批量添加字段弹窗 -->
  <el-dialog v-model="batchOpen" width="860px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
        </div>
        <span class="rd-detail-header-title">批量添加字段：{{ activeTableTab === 'main' ? '主表（' + mainTableName + '）' : '明细表' + activeTableTab.slice(2) + '（' + mainTableName + '_' + activeTableTab + '）' }}</span>
      </div>
    </template>
    <div class="batch-toolbar">
      <el-button size="small" type="primary" plain icon="Plus" @click="addBatchRow">再加一行</el-button>
      <span class="field-tip">直接在表格中逐行填写，确定后一次性追加到当前表；字段显示名与数据库字段名称为必填</span>
    </div>
    <el-table :data="batchRows" border size="small" row-key="batchKey" max-height="360" class="field-table">
      <el-table-column type="index" label="序号" width="55" align="center" />
      <el-table-column label="字段显示名" min-width="130" align="center"><template #default="scope"><el-input v-model="scope.row.label" placeholder="如：合同名称" size="small" @input="autoFillFieldName(scope.row, scope.row.label)" /></template></el-table-column>
      <el-table-column label="数据库字段名称" min-width="130" align="center"><template #default="scope"><el-input v-model="scope.row.field" placeholder="小写英文，如 htmc（自动取首字母，可修改）" size="small" @input="scope.row.autoField = false" /></template></el-table-column>
      <el-table-column label="表现形式" width="125" align="center">
        <template #default="scope">
          <el-select v-model="scope.row.form" size="small" @change="onBatchFormChange(scope.row)">
            <el-option-group label="选择框">
              <el-option v-for="(v, k) in SELECT_FORM_OPTIONS" :key="k" :label="v" :value="k" />
            </el-option-group>
            <el-option v-for="(v, k) in FORM_OPTIONS" :key="k" :label="v" :value="k" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="105" align="center">
        <template #default="scope">
          <span v-if="isDtypeLocked(scope.row.form)" style="color:#909399">{{ DTYPE_OPTIONS[scope.row.dtype] || scope.row.dtype }}（自动）</span>
          <el-select v-else v-model="scope.row.dtype" size="small">
            <el-option v-for="(v, k) in dtypeOptions(scope.row.form)" :key="k" :label="v" :value="k" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="文本长度" width="90" align="center">
        <template #default="scope"><el-input-number v-if="scope.row.dtype === 'text' && !isLengthLocked(scope.row)" v-model="scope.row.length" :min="1" :max="8000" :controls="false" size="small" style="width:100%" /><span v-else style="color:#cbd5e1">自动</span></template>
      </el-table-column>
      <el-table-column label="格式/配置" min-width="130" align="center">
        <template #default="scope">
          <el-input-number v-if="scope.row.form === 'textarea'" v-model="scope.row.rows" :min="1" :max="20" :controls="false" size="small" style="width:72px" />
          <el-select v-else-if="scope.row.form === 'browse'" v-model="scope.row.browseType" size="small" style="width:100%">
            <el-option v-for="(v, k) in BROWSE_TYPES" :key="k" :label="v" :value="k" />
          </el-select>
          <el-button v-else-if="isSelForm(scope.row.form)" link type="primary" size="small" @click="openSelConfig(scope.row)">配置</el-button>
          <span v-else style="color:#cbd5e1">—</span>
        </template>
      </el-table-column>
      <el-table-column label="填写提示" min-width="120" align="center"><template #default="scope"><el-input v-model="scope.row.placeholder" placeholder="背景提示文字" size="small" /></template></el-table-column>
      <el-table-column label="操作" width="60" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="removeBatchRow(scope.$index)" /></template></el-table-column>
    </el-table>
    <template #footer>
      <el-button type="primary" @click="confirmBatchAdd">确 定</el-button>
      <el-button @click="batchOpen = false">取 消</el-button>
    </template>
  </el-dialog>

  <!-- 选择框选项配置弹窗 -->
  <el-dialog v-model="selCfgOpen" width="620px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="6" x2="20" y2="6"/><line x1="4" y1="12" x2="20" y2="12"/><line x1="4" y1="18" x2="20" y2="18"/></svg>
        </div>
        <span class="rd-detail-header-title">选项配置：{{ selCfgField ? selCfgField.label || '（未命名字段）' : '' }}{{ selCfgField ? '（' + (SELECT_FORM_OPTIONS[selCfgField.form] || '') + '）' : '' }}</span>
      </div>
    </template>
    <div class="batch-toolbar">
      <el-button size="small" type="primary" plain icon="Plus" @click="selCfgAddRow">再加一行</el-button>
      <span class="field-tip">界面显示中文，数据库存储实际值；可上下移动调整显示顺序</span>
    </div>
    <el-table :data="selCfgRows" border size="small" row-key="cfgKey" max-height="320" class="field-table">
      <el-table-column type="index" label="序号" width="55" align="center" />
      <el-table-column label="显示值" min-width="140" align="center"><template #default="scope"><el-input v-model="scope.row.label" placeholder="如：解除" size="small" /></template></el-table-column>
      <el-table-column label="实际值" min-width="140" align="center"><template #default="scope"><el-input v-model="scope.row.value" placeholder="留空则存显示值" size="small" /></template></el-table-column>
      <el-table-column label="默认值" width="80" align="center">
        <template #default="scope">
          <el-checkbox v-if="selCfgField && selCfgField.form === 'checkbox'" :model-value="defaultList.includes(scope.row.value)" @change="v => toggleMultiDefault(scope.row, v)" />
          <el-radio v-else v-model="selCfgDefault" :value="scope.row.value">&nbsp;</el-radio>
        </template>
      </el-table-column>
      <el-table-column label="排序" width="90" align="center">
        <template #default="scope">
          <el-button link size="small" icon="Top" @click="selCfgMove(scope.$index, -1)" />
          <el-button link size="small" icon="Bottom" @click="selCfgMove(scope.$index, 1)" />
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button type="primary" @click="confirmSelConfig">确 定</el-button>
      <el-button @click="selCfgOpen = false">取 消</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="FormFieldsEditor">
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { getForm, updateForm } from '@/api/flowable/form'
import { urgencyOptions } from '@/api/flowable/urgency'
import { getCurrentInstance } from 'vue'
import { pinyin } from 'pinyin-pro'
import Sortable from 'sortablejs'

const props = defineProps({
  /** 关联表单ID（切换后自动重新加载字段） */
  formId: { type: [Number, String], default: null }
})
const emit = defineEmits(['saved'])

const { proxy } = getCurrentInstance()

/** 中文名称 → 拼音首字母字段名（小写字母开头，仅含字母/数字/下划线） */
function toFieldInitials(label) {
  if (!label) return ''
  const s = pinyin(String(label), { pattern: 'first', toneType: 'none', type: 'array', nonZh: 'concurrent' }).join('')
  return s.toLowerCase().replace(/[^a-z0-9_]/g, '').replace(/^[^a-z]+/, '').slice(0, 30)
}

/** 输入显示名时自动填充数据库字段名称：仅在字段为空或之前是自动生成的情况下覆盖 */
function autoFillFieldName(target, label) {
  const gen = toFieldInitials(label)
  if (target.autoField || !target.field) {
    target.field = gen || undefined
    target.autoField = !!gen
  } else if (gen && target.field === gen) {
    target.autoField = true
  }
}

// 表现形式（参照泛微e9）：选择框组 + 其他独立表现形式
const SELECT_FORM_OPTIONS = { select: '下拉框', radio: '点选单选', checkbox: '复选勾选' }
const FORM_OPTIONS = {
  text: '单行文本框', textarea: '多行文本框', browse: '浏览按钮', check: 'Check框',
  attach: '附件上传', date: '日期框', datetime: '日期时间框',
  number: '数字框', switch: '开关框', rate: '评分框'
}
/** 选择框类表现形式（下拉/单选/复选共用选项配置与键值分离逻辑） */
const isSelForm = f => ['select', 'radio', 'checkbox'].includes(f)
/** 数据类型（dtype）：决定数据库列类型 */
const DTYPE_OPTIONS = { text: '文本', date: '日期', datetime: '日期时间', float: '浮点数', int: '整数' }
/** 浏览框数据源（主数据类型，支持自定义扩展） */
const BROWSE_TYPES = {
  user: '人员', dept: '部门', post: '岗位', role: '角色',
  material: '物料', customer: '客户', supplier: '供应商', contract: '合同', custom: '自定义'
}
/** 附件类型约束 */
const ATTACH_TYPES = { all: '不限类型', image: '图片(image/*)', doc: '文档(doc/pdf/xls…)', custom: '自定义后缀' }
/** 表现形式→默认dtype/长度/默认配置 */
const FORM_DEFAULTS = {
  text:     { dtype: 'text', length: 500 },
  textarea: { dtype: 'text', rows: 4, length: 4000 },
  browse:   { dtype: 'text', browseType: 'user', length: 1000 },
  check:    { dtype: 'text', length: 10 },
  select:   { dtype: 'text', length: 100, selectOptions: [] },
  attach:   { dtype: 'text', length: 2000, attachType: 'all', attachLimit: 5 },
  date:     { dtype: 'date' },
  datetime: { dtype: 'datetime' },
  number:   { dtype: 'float', scale: 2 },
  radio:    { dtype: 'text', length: 100, selectOptions: [] },
  checkbox: { dtype: 'text', length: 200, selectOptions: [] },
  switch:   { dtype: 'text', length: 10 },
  rate:     { dtype: 'int' }
}

/** 紧急程度可选档位（来自 flow_urgency_level 配置表；为空时回退 SYSTEM_FIELDS 内置默认） */
const urgencyOptionList = ref([])

/** 加载紧急程度配置（配置表驱动，失败静默回退内置档位） */
async function loadUrgencyOptions() {
  try {
    const res = await urgencyOptions()
    const list = (res.data || []).map(o => ({
      label: o.customName || o.defaultName,
      value: o.levelValue
    }))
    urgencyOptionList.value = list
  } catch (e) {
    urgencyOptionList.value = []
  }
}

/** 系统内置字段（参照泛微：每个流程默认都有，用户无需添加，不可删除）
 *  - 标题：流程标题，发起时自动带出
 *  - 紧急程度：档位由 flow_urgency_level 配置表驱动（默认 1普通/2紧急/3特急）
 *  - 签字意见：节点办理意见
 *  - 打印次数：表单打印计数 */
const SYSTEM_FIELDS = [
  { field: 'title',        label: '标题',     form: 'text',   dtype: 'text',   length: 200,  scale: 2, table: 'main', order: -4, sysField: true },
  { field: 'urgency',      label: '紧急程度', form: 'select', dtype: 'text',   length: 20,   scale: 2, table: 'main', order: -3, sysField: true,
    options: '1:普通,2:紧急,3:特急', selectOptions: [ { label: '普通', value: '1' }, { label: '紧急', value: '2' }, { label: '特急', value: '3' } ], defaultValue: '1' },
  { field: 'signOpinion',  label: '签字意见', form: 'textarea', dtype: 'text', length: 2000, scale: 2, table: 'main', order: -2, sysField: true, rows: 3 },
  { field: 'printCount',   label: '打印次数', form: 'number', dtype: 'int',    length: 11,   scale: 0, table: 'main', order: -1, sysField: true }
]

const formDetail = ref({})
const fields = ref([])
const detailTables = ref([])
const activeTableTab = ref('main')
const newField = ref({})
const addFieldOpen = ref(false)
const addFieldRef = ref(null)
const checkedFields = ref([])
const saving = ref(false)

const fieldRules = {
  label: [{ required: true, message: '字段显示名不能为空', trigger: 'blur' }],
  field: [
    { required: true, message: '数据库字段名称不能为空', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]{0,39}$/, message: '仅允许小写字母开头的字母/数字/下划线', trigger: 'blur' }
  ]
}

const mainTableName = computed(() => formDetail.value.formKey ? 'flow_data_' + formDetail.value.formKey : 'flow_data_{表单Key}')

const activeFields = computed(() => fields.value.filter(f => (f.table || 'main') === activeTableTab.value))

// ===== 字段列表分页（前端分页：字段多时翻页显示；拖拽排序/批量删除按当前页映射全局索引） =====
const fieldPage = ref(1)
const fieldPageSize = ref(10)
const pagedFields = computed(() => {
  const start = (fieldPage.value - 1) * fieldPageSize.value
  return activeFields.value.slice(start, start + fieldPageSize.value)
})
// 页码修正：切换表页签回到第 1 页；删除/批量删除后当前页超界时回退到最大页
watch(activeTableTab, () => { fieldPage.value = 1 })
watch(() => activeFields.value.length, len => {
  const maxPage = Math.max(1, Math.ceil(len / fieldPageSize.value))
  if (fieldPage.value > maxPage) fieldPage.value = maxPage
})

/** 加载关联表单详情并解析字段 */
async function loadFormFields() {
  if (!props.formId) {
    formDetail.value = {}
    fields.value = []
    detailTables.value = []
    activeTableTab.value = 'main'
    return
  }
  try {
    await loadUrgencyOptions()
    const res = await getForm(props.formId)
    formDetail.value = res.data || {}
    fields.value = ensureSystemFields(normalizeFields(safeParse(res.data.fieldsJson)))
    rebuildDetailTables()
    activeTableTab.value = 'main'
  } catch (e) {
    proxy.$modal.msgError('加载表单字段失败：' + (e.message || e))
  }
}

watch(() => props.formId, () => loadFormFields())
onMounted(() => loadFormFields())

function safeParse(json) {
  try { return JSON.parse(json || '[]') } catch (e) { return [] }
}

function rebuildDetailTables() {
  const set = new Set(fields.value.map(f => f.table).filter(t => t && t !== 'main'))
  detailTables.value = [...set].sort()
}

/**
 * 补齐系统内置字段（参照泛微：每个流程默认都有这 4 个字段，用户无需添加）
 * - 已存在的系统字段沿用其现有配置（不覆盖用户改动）
 * - 缺失的系统字段按 SYSTEM_FIELDS 定义补到主表最前
 */
function ensureSystemFields(list) {
  const arr = Array.isArray(list) ? [...list] : []
  // 紧急程度：已存在字段也按配置表刷新选项，保证与后台配置一致
  if (urgencyOptionList.value.length) {
    arr.forEach(f => {
      if (f.field === 'urgency') f.selectOptions = urgencyOptionList.value.map(o => ({ ...o }))
    })
  }
  const existFields = new Set(arr.map(f => f.field))
  const missing = SYSTEM_FIELDS.filter(sys => !existFields.has(sys.field))
  if (!missing.length) {
    // 已全部存在：仅补 sysField 标记，保证不可删除
    arr.forEach(f => { if (SYSTEM_FIELDS.some(s => s.field === f.field)) f.sysField = true })
    return arr
  }
  const built = missing.map(sys => {
    const item = { ...sys, rowKey: 'r' + Math.random().toString(36).slice(2, 8) }
    if (item.field === 'urgency' && urgencyOptionList.value.length) {
      item.selectOptions = urgencyOptionList.value.map(o => ({ ...o }))
    }
    return item
  })
  const merged = [...built, ...arr]
  // 重排主表字段的 order：系统字段在最前
  let mainIdx = 0
  merged.forEach(f => { if ((f.table || 'main') === 'main') f.order = ++mainIdx })
  return merged
}

/** 兼容旧结构 {type:text/textarea/number/date/select} → {form,dtype,length,scale,table} */
function normalizeFields(arr) {
  return (arr || []).map(f => {
    const n = { ...f, rowKey: f.rowKey || 'r' + Math.random().toString(36).slice(2, 8) }
    if (!n.form) {
      if (n.type === 'textarea') n.form = 'textarea'
      else if (n.type === 'select') n.form = 'select'
      else n.form = 'text'
      if (n.type === 'number') n.dtype = 'float'
      else if (n.type === 'date') n.dtype = 'date'
      else n.dtype = n.dtype || 'text'
    }
    n.dtype = n.dtype || 'text'
    n.length = n.length || 500
    n.scale = n.scale || 2
    n.table = n.table || 'main'
    n.order = n.order || 1
    if (isSelForm(n.form) && !n.selectOptions && n.options) {
      n.selectOptions = String(n.options).split(/[,，]/).map(s => s.trim()).filter(Boolean).map(s => ({ label: s, value: s }))
    }
    if (n.form === 'textarea') n.rows = n.rows || 4
    if (n.form === 'browse') n.browseType = n.browseType || 'user'
    if (n.form === 'attach') { n.attachType = n.attachType || 'all'; n.attachLimit = n.attachLimit || 5 }
    return n
  })
}

// ===== 字段列表拖动排序（参照泛微：拖拽手柄列触发，拖动后按新顺序重写显示顺序） =====
const fieldTableRef = ref(null)
function initFieldDragSort() {
  const table = fieldTableRef.value && fieldTableRef.value.$el
  const tbody = table && table.querySelector('.el-table__body-wrapper tbody')
  if (!tbody || tbody._dragInited) return
  tbody._dragInited = true
  Sortable.create(tbody, {
    handle: '.drag-handle',
    animation: 150,
    ghostClass: 'drag-ghost',
    onEnd: onFieldDragEnd
  })
}
function onFieldDragEnd(evt) {
  const { oldIndex, newIndex, item } = evt
  if (oldIndex == null || newIndex == null || oldIndex === newIndex) return
  // 还原 DOM 位置，交给 Vue 数据驱动重排，避免 el-table 行 DOM 与数据不一致
  const tbody = item.parentNode
  if (tbody) {
    if (newIndex > oldIndex) {
      const anchor = tbody.children[oldIndex]
      if (anchor) tbody.insertBefore(item, anchor)
    } else {
      const anchor = tbody.children[oldIndex + 1]
      if (anchor) tbody.insertBefore(item, anchor)
      else tbody.appendChild(item)
    }
  }
  // 仅当前表页签范围内重排（分页后 oldIndex/newIndex 为当前页内索引，映射为全局索引），并按新顺序重写显示顺序
  const pageStart = (fieldPage.value - 1) * fieldPageSize.value
  const from = pageStart + oldIndex
  const to = pageStart + newIndex
  const list = activeFields.value.slice()
  if (from >= list.length || to >= list.length) return
  const moved = list.splice(from, 1)[0]
  list.splice(to, 0, moved)
  const others = fields.value.filter(f => (f.table || 'main') !== activeTableTab.value)
  fields.value = others.concat(list)
  list.forEach((f, i) => { f.order = i + 1 })
}
watch([activeTableTab, () => fields.value.length], () => nextTick(initFieldDragSort), { flush: 'post' })

function onFormChange(row) { applyFormDefaults(row) }

/** 应用表现形式默认配置（保留用户已填项；切换类型后默认值失效清空） */
function applyFormDefaults(row) {
  const d = FORM_DEFAULTS[row.form] || {}
  row.dtype = d.dtype || row.dtype
  row.defaultValue = undefined
  if (d.length) row.length = d.length
  if (d.rows) row.rows = d.rows
  if (d.browseType) row.browseType = d.browseType
  if (d.attachType) row.attachType = d.attachType
  if (d.attachLimit) row.attachLimit = d.attachLimit
  if (d.selectOptions) row.selectOptions = d.selectOptions
  if (d.scale) row.scale = d.scale
  if (row.form === 'textarea') { row.dtype = 'text'; row.length = Math.max(row.length || 4000, 4000) }
  if (row.form === 'date') row.dtype = 'date'
  if (row.form === 'datetime') row.dtype = 'datetime'
  if (row.form === 'number') row.dtype = row.dtype === 'int' ? 'int' : 'float'
}

/** 数据类型是否随表现形式锁定：选项类、附件、日期、数字类均不允许改dtype */
function isDtypeLocked(form) { return ['date', 'datetime', 'number', 'textarea', 'check', 'select', 'radio', 'checkbox', 'attach'].includes(form) }

/** 自由表现形式下可选的数据类型：日期/日期时间只能通过表现形式选择 */
function dtypeOptions(form) {
  if (isDtypeLocked(form)) return DTYPE_OPTIONS
  const { date, datetime, ...rest } = DTYPE_OPTIONS
  return rest
}

/** 长度是否自动推导（browse/attach/check 等不需要用户填长度） */
function isLengthLocked(row) { return ['browse', 'attach', 'check', 'switch'].includes(row.form) }

// ===== 选择框选项配置 =====
const selCfgOpen = ref(false)
const selCfgField = ref(null)
const selCfgRows = ref([])
const selCfgDefault = ref(undefined)
const defaultList = computed(() => String((selCfgField.value && selCfgField.value.defaultValue) || '').split(',').filter(Boolean))
function openSelConfig(row) {
  selCfgField.value = row
  selCfgRows.value = (row.selectOptions || []).map(o => ({ cfgKey: 'c' + Math.random().toString(36).slice(2, 8), label: o.label, value: o.value }))
  if (!selCfgRows.value.length) selCfgAddRow()
  selCfgDefault.value = row.defaultValue || undefined
  selCfgOpen.value = true
}
function selCfgAddRow() { selCfgRows.value.push({ cfgKey: 'c' + Math.random().toString(36).slice(2, 8), label: undefined, value: undefined }) }
function selCfgRemoveRow(i) { selCfgRows.value.splice(i, 1) }
function toggleMultiDefault(opt, on) {
  const row = selCfgField.value
  if (!row) return
  const cur = String(row.defaultValue || '').split(',').filter(Boolean)
  const next = on ? cur.concat(opt.value).filter((v, i, a) => a.indexOf(v) === i) : cur.filter(v => v !== opt.value)
  row.defaultValue = next.join(',')
}
function selCfgMove(i, dir) {
  const arr = selCfgRows.value
  const j = i + dir
  if (j < 0 || j >= arr.length) return
  const tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp
}
function confirmSelConfig() {
  const row = selCfgField.value
  if (!row) return
  row.selectOptions = selCfgRows.value
    .map(o => ({ label: (o.label || '').trim(), value: (o.value === undefined || o.value === null || o.value === '') ? (o.label || '').trim() : o.value.trim() }))
    .filter(o => o.label)
  if (row.selectOptions.length) {
    row.length = Math.max(...row.selectOptions.map(o => String(o.value || '').length), 10) * 2 + 20
  }
  if (row.form === 'checkbox') {
    row.defaultValue = String(row.defaultValue || '').split(',').filter(v => row.selectOptions.some(o => o.value === v.trim())).map(v => v.trim()).join(',')
  } else {
    const d = selCfgDefault.value
    row.defaultValue = d && row.selectOptions.some(o => o.value === d) ? d : undefined
  }
  selCfgOpen.value = false
}

function onNewFormChange() { applyFormDefaults(newField.value) }
function onBatchFormChange(row) { applyFormDefaults(row) }

// ===== 添加字段 =====
function openAddField() {
  newField.value = { label: undefined, field: undefined, autoField: true, table: activeTableTab.value === '__add' ? 'main' : activeTableTab.value, order: fields.value.length + 1, form: 'text', dtype: 'text', length: 500, scale: 2, options: undefined, placeholder: undefined, required: false }
  addFieldOpen.value = true
  nextTick(() => addFieldRef.value && addFieldRef.value.clearValidate())
}
function confirmAddField() {
  addFieldRef.value.validate(valid => {
    if (!valid) return
    const dup = fields.value.some(f => f.field === newField.value.field)
    if (dup) { proxy.$modal.msgWarning('数据库字段名称已存在：' + newField.value.field); return }
    fields.value.push({ rowKey: 'r' + Math.random().toString(36).slice(2, 8), ...newField.value })
    if (newField.value.table !== 'main' && !detailTables.value.includes(newField.value.table)) {
      detailTables.value.push(newField.value.table)
      detailTables.value.sort()
    }
    activeTableTab.value = newField.value.table
    addFieldOpen.value = false
  })
}
function onFieldSelChange(rows) { checkedFields.value = rows }

// ===== 批量添加字段 =====
const batchOpen = ref(false)
const batchRows = ref([])
function blankBatchRow() {
  return { batchKey: 'b' + Math.random().toString(36).slice(2, 8), label: undefined, field: undefined, autoField: true, form: 'text', dtype: 'text', length: 500, scale: 2, options: undefined, required: false }
}
function openBatchAdd() { batchRows.value = [blankBatchRow()]; batchOpen.value = true }
function addBatchRow() { batchRows.value.push(blankBatchRow()) }
function removeBatchRow(idx) { batchRows.value.splice(idx, 1) }
function confirmBatchAdd() {
  const rows = batchRows.value.filter(r => (r.label || '').trim() || (r.field || '').trim())
  if (!rows.length) { proxy.$modal.msgWarning('请至少填写一行字段'); return }
  let order = fields.value.length
  for (const r of rows) {
    if (!r.label || !r.field) { proxy.$modal.msgWarning('存在未填写完整的行（字段显示名/数据库字段名称）'); return }
    if (!/^[a-z][a-z0-9_]{0,39}$/.test(r.field)) { proxy.$modal.msgWarning('字段名[' + r.field + ']不合法：小写字母开头的字母/数字/下划线'); return }
  }
  const table = activeTableTab.value === '__add' ? 'main' : activeTableTab.value
  const seen = new Set(fields.value.filter(f => (f.table || 'main') === table).map(f => f.field))
  for (const r of rows) {
    if (seen.has(r.field)) { proxy.$modal.msgWarning('字段名[' + r.field + ']在当前表已存在或本批重复，请修改后再添加'); return }
    seen.add(r.field)
  }
  rows.forEach(r => {
    order++
    fields.value.push({ rowKey: 'r' + Math.random().toString(36).slice(2, 8), table, order, ...r })
  })
  if (table !== 'main' && !detailTables.value.includes(table)) {
    detailTables.value.push(table)
    detailTables.value.sort()
  }
  proxy.$modal.msgSuccess('已批量添加 ' + rows.length + ' 个字段')
  batchOpen.value = false
}

function removeCheckedFields() {
  // 系统内置字段（标题/紧急程度/签字意见/打印次数）不允许删除
  const keys = checkedFields.value.filter(r => !r.sysField).map(r => r.rowKey)
  if (!keys.length) { proxy.$modal.msgWarning('系统内置字段不可删除'); return }
  fields.value = fields.value.filter(f => !keys.includes(f.rowKey))
  rebuildDetailTables()
  if (activeTableTab.value !== 'main' && !detailTables.value.includes(activeTableTab.value)) {
    activeTableTab.value = 'main'
  }
}

function removeDetailTable(name) {
  fields.value = fields.value.filter(f => f.table !== name)
  detailTables.value = detailTables.value.filter(t => t !== name)
  activeTableTab.value = 'main'
}

// ===== 保存字段设计（仅更新 fieldsJson，其余表单字段沿用原值） =====
function saveFields() {
  if (!props.formId) return
  for (const f of fields.value) {
    if (!f.label || !f.field) { proxy.$modal.msgWarning('存在未填写完整（显示名/字段名）的字段'); return }
    if (!/^[a-z][a-z0-9_]{0,39}$/.test(f.field)) { proxy.$modal.msgWarning('字段名[' + f.field + ']不合法：小写字母开头的字母/数字/下划线'); return }
    if (f.dtype === 'text' && (!f.length || f.length < 1)) { proxy.$modal.msgWarning('字段[' + f.label + ']请填写文本长度'); return }
  }
  const seen = {}
  for (const f of fields.value) {
    const key = (f.table || 'main') + '.' + f.field
    if (seen[key]) { proxy.$modal.msgWarning('字段名[' + f.field + ']在' + (f.table === 'main' || !f.table ? '主表' : '明细表 ' + f.table + ' ') + '中重复出现，请修改后再保存'); return }
    seen[key] = f.label
  }
  saving.value = true
  updateForm({
    formId: props.formId,
    formKey: formDetail.value.formKey,
    formName: formDetail.value.formName,
    category: formDetail.value.category || undefined,
    formDesc: formDetail.value.formDesc || undefined,
    processKey: formDetail.value.processKey || undefined,
    fieldsJson: JSON.stringify(fields.value.map(({ rowKey, ...rest }) => rest)),
    layoutJson: formDetail.value.layoutJson || undefined
  }).then(() => {
    saving.value = false
    proxy.$modal.msgSuccess('字段设计已保存')
    emit('saved')
  }).catch(() => { saving.value = false })
}
</script>

<style lang="scss" scoped>
.ffe-wrap { padding: 4px 0; }
.field-tab-bar { margin: 0 12px; }
.field-tab-bar .tab-tbl { font-size: 12px; color: var(--ink-500, #64748b); }
.field-tab-bar .tab-add { font-weight: 700; color: var(--brand-600, #4f46e5); }
.field-toolbar { display: flex; align-items: center; gap: 8px; margin: 10px 12px; }
.field-toolbar .field-tip { font-size: 12px; color: #64748b; }
.field-table { margin: 0 12px 8px; }
.field-table .drag-handle { cursor: move; color: var(--ink-400, #94a3b8); font-size: 14px; user-select: none; }
.field-table .drag-handle:hover { color: var(--brand-600, #4f46e5); }
.field-table .drag-ghost { opacity: 0.6; background: var(--brand-50, #eef2ff); }
.batch-toolbar { display: flex; align-items: center; gap: 8px; margin: 0 12px 10px; }
.batch-toolbar .field-tip { font-size: 12px; color: #64748b; }
.form-tip { font-size: 12px; color: #94a3b8; line-height: 1.5; }
</style>

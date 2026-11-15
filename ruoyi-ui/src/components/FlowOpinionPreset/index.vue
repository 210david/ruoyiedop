<template>
  <el-dialog v-model="open" title="常用意见设置" width="640px" append-to-body :close-on-click-modal="false"
    class="rd-dialog" @open="load">
    <div class="fp-body" v-loading="loading">
      <p class="fp-tip">
        这里维护的是<b>你本人</b>办理流程时的快捷短语，只对自己生效，保存后立即生效。
        不设置时使用系统默认短语（由管理员在字典「流程签字意见常用语」中统一维护）。
      </p>

      <div class="fp-list" v-if="draft.length">
        <div class="fp-row" v-for="(item, i) in draft" :key="i">
          <span class="fp-no">{{ String(i + 1).padStart(2, '0') }}</span>
          <el-input v-model="draft[i]" size="small" maxlength="200" show-word-limit
            placeholder="如：情况属实，同意办理" />
          <div class="fp-ops">
            <el-tooltip content="上移" placement="top">
              <el-button link size="small" :disabled="i === 0" @click="move(i, -1)">
                <el-icon><Top /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="下移" placement="top">
              <el-button link size="small" :disabled="i === draft.length - 1" @click="move(i, 1)">
                <el-icon><Bottom /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="danger" size="small" @click="remove(i)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </div>
      <div class="fp-empty" v-else-if="!loading">还没有常用意见，点左下「添加一条」开始配置</div>
    </div>

    <template #footer>
      <div class="fp-footer">
        <div class="fp-footer-left">
          <el-button size="small" icon="Plus" :disabled="draft.length >= MAX_COUNT" @click="add">添加一条</el-button>
          <el-button size="small" icon="RefreshLeft" @click="restore">恢复系统默认</el-button>
        </div>
        <div class="fp-footer-right">
          <el-button size="small" @click="open = false">取 消</el-button>
          <el-button size="small" type="primary" :loading="saving" @click="submit">保 存</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="FlowOpinionPreset">
import { listOpinionPresets, saveOpinionPresets, resetOpinionPresets } from '@/api/flowable/opinionPreset'
import { Top, Bottom, Delete } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const props = defineProps({
  /** 弹窗显隐（v-model） */
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'saved'])

/** 每人最多短语条数（与后端 FlowOpinionPresetServiceImpl.MAX_COUNT 保持一致） */
const MAX_COUNT = 30

const open = computed({
  get: () => props.modelValue,
  set: v => emit('update:modelValue', v)
})

/** 编辑中的短语（保存时整体覆盖后端数据） */
const draft = ref([])
/** 系统默认短语（「恢复系统默认」直接落库并回填） */
const systemDefaults = ref([])
const loading = ref(false)
const saving = ref(false)

/** 打开弹窗时拉取当前生效短语 */
function load() {
  loading.value = true
  listOpinionPresets().then(res => {
    const d = res.data || {}
    systemDefaults.value = d.defaults || []
    const current = (d.current && d.current.length) ? d.current : (d.defaults || [])
    draft.value = current.slice()
  }).catch(() => {
    draft.value = []
  }).finally(() => { loading.value = false })
}

function add() {
  if (draft.value.length >= MAX_COUNT) return
  draft.value.push('')
}

function remove(i) {
  draft.value.splice(i, 1)
}

/** 上移 / 下移：顺序即办理页 chips 的展示顺序 */
function move(i, delta) {
  const j = i + delta
  if (j < 0 || j >= draft.value.length) return
  const arr = draft.value.slice()
  const tmp = arr[i]
  arr[i] = arr[j]
  arr[j] = tmp
  draft.value = arr
}

/** 恢复系统默认：清空个人配置（影响下次打开，需二次确认） */
function restore() {
  proxy.$modal.confirm('恢复后你自定义的常用意见会被清空，改用系统默认短语，确认？').then(() => {
    return resetOpinionPresets().then(res => {
      systemDefaults.value = res.data || systemDefaults.value
      draft.value = (res.data || []).slice()
      proxy.$modal.msgSuccess('已恢复系统默认短语')
      emit('saved', draft.value.slice())
    })
  }).catch(() => {})
}

function submit() {
  const list = draft.value.map(s => String(s || '').trim()).filter(Boolean)
  if (!list.length) {
    proxy.$modal.msgWarning('请至少保留一条常用意见')
    return
  }
  saving.value = true
  saveOpinionPresets(list).then(res => {
    const saved = res.data && res.data.length ? res.data : list
    draft.value = saved.slice()
    proxy.$modal.msgSuccess('常用意见已保存')
    emit('saved', saved.slice())
    open.value = false
  }).catch(() => {}).finally(() => { saving.value = false })
}
</script>

<style lang="scss" scoped>
.fp-body { min-height: 120px; }

.fp-tip {
  margin: 0 0 12px;
  padding: 8px 12px;
  font-size: 12px;
  line-height: 1.7;
  color: #64748b;
  background: #f6f9fd;
  border: 1px solid #e6eef8;
  border-radius: 6px;

  b { color: #334155; font-weight: 600; }
}

.fp-list { display: flex; flex-direction: column; gap: 8px; max-height: 46vh; overflow-y: auto; }

.fp-row { display: flex; align-items: center; gap: 8px; }

.fp-no {
  flex: none;
  width: 22px;
  font-size: 12px;
  color: #94a3b8;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.fp-row :deep(.el-input) { flex: 1 1 auto; }

.fp-ops {
  flex: none;
  display: flex;
  align-items: center;
  gap: 2px;

  :deep(.el-button + .el-button) { margin-left: 0; }
}

.fp-empty {
  padding: 26px 0;
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
}

.fp-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.fp-footer-left,
.fp-footer-right {
  display: flex;
  align-items: center;
  gap: 8px;

  :deep(.el-button + .el-button) { margin-left: 0; }
}
</style>

<template>
  <section class="shortcut-bar" v-if="visible">
    <div class="shortcut-head">
      <span class="shortcut-title">
        <svg-icon icon-class="star" class="shortcut-title__icon" />
        常用功能
      </span>
      <span class="shortcut-tip" v-if="autoCount > 0 && !loading">按你最近的使用频率自动排列</span>
      <el-button link type="primary" size="small" class="shortcut-edit" @click="openDialog">
        <el-icon><Setting /></el-icon>
        自定义
      </el-button>
    </div>

    <div class="shortcut-list">
      <button
        v-for="item in visibleList"
        :key="item.menuPath"
        class="shortcut-item"
        :class="{ 'is-pinned': item.source === 'M' }"
        :title="item.menuTitle"
        @click="go(item)"
      >
        <span class="shortcut-item__icon">
          <svg-icon :icon-class="item.menuIcon || 'grid'" />
        </span>
        <span class="shortcut-item__text">{{ item.menuTitle }}</span>
        <span class="shortcut-item__pin" v-if="item.source === 'M'">固定</span>
      </button>

      <button v-if="visibleList.length === 0" class="shortcut-item shortcut-item--empty" @click="openDialog">
        <el-icon><Plus /></el-icon>
        <span class="shortcut-item__text">添加常用功能</span>
      </button>
    </div>

    <!-- 自定义常用功能 -->
    <el-dialog
      v-model="dialog.visible"
      width="1014px"
      append-to-body
      draggable
      :close-on-click-modal="false"
      class="shortcut-dialog"
    >
      <template #header>
        <div class="sd-head">
          <span class="sd-head__icon"><el-icon><Grid /></el-icon></span>
          <div class="sd-head__text">
            <p class="sd-head__title">自定义常用功能</p>
            <p class="sd-head__desc">勾选高频功能固定到工作台，其余位置由系统按使用频率自动补齐</p>
          </div>
          <span class="sd-head__drag">
            <el-icon><Rank /></el-icon>
            按住标题拖动
          </span>
        </div>
      </template>

      <div class="sd-notice">
        <el-icon><InfoFilled /></el-icon>
        <span>最多固定 <b>{{ maxPinned }}</b> 项，当前已选 <b>{{ dialog.checked.length }}</b> 项；仅展示已开通模块的功能</span>
      </div>

      <!-- 新手上路开关：系统级关闭时整行不展示，避免用户看到无效开关 -->
      <div class="sd-switch" v-if="onboardGlobalEnabled">
        <span class="sd-switch__icon"><el-icon><Compass /></el-icon></span>
        <div class="sd-switch__text">
          <p class="sd-switch__title">显示「新手上路」引导</p>
          <p class="sd-switch__desc">关闭后工作台不再显示岗位任务清单，可随时在这里重新开启</p>
        </div>
        <el-switch :model-value="onboardEnabled" @change="val => emit('toggle-onboard', val)" />
      </div>

      <el-input
        v-model="dialog.keyword"
        placeholder="搜索功能名称，如 入库、采购申请"
        clearable
        size="large"
        class="sd-search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
        <template #suffix>
          <span class="sd-search__count">{{ filteredCount }} 项</span>
        </template>
      </el-input>

      <div class="sd-body">
        <section class="sd-group" v-for="group in groupedMenus" :key="group.module">
          <header class="sd-group__head">
            <span class="sd-group__title">{{ group.title }}</span>
            <span class="sd-group__count">{{ group.menus.length }}</span>
            <span class="sd-group__picked" v-if="groupPicked(group) > 0">已选 {{ groupPicked(group) }}</span>
          </header>
          <el-checkbox-group v-model="dialog.checked" class="sd-grid">
            <el-checkbox
              v-for="menu in group.menus"
              :key="menu.path"
              :label="menu.path"
              border
              class="sd-check"
            >
              <span class="sd-check__text" :title="menu.title">{{ menu.title }}</span>
            </el-checkbox>
          </el-checkbox-group>
        </section>

        <el-empty
          v-if="groupedMenus.length === 0"
          description="没有匹配的功能，换个关键词试试"
          :image-size="72"
        />
      </div>

      <template #footer>
        <div class="sd-foot">
          <span class="sd-foot__counter">
            已选 <b>{{ dialog.checked.length }}</b> / {{ maxPinned }}
          </span>
          <div class="sd-foot__actions">
            <el-button @click="dialog.visible = false">取 消</el-button>
            <el-button type="primary" :loading="dialog.saving" @click="save">保 存</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup name="ShortcutBar">
import { ref, reactive, computed, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Setting, Plus, Search, InfoFilled, Grid, Compass, Rank } from '@element-plus/icons-vue'
import { getShortcuts, savePinnedShortcuts } from '@/api/home'

const props = defineProps({
  /** 当前用户可访问的叶子菜单：{ path, title, icon, module, moduleTitle } */
  menus: {
    type: Array,
    default: () => []
  },
  /** 显示条数 */
  limit: {
    type: Number,
    default: 8
  },
  /** 后台「模块开关」中已停用的模块标识，用于过滤指向已关模块的固定项 */
  disabledModules: {
    type: Array,
    default: () => []
  },
  /** 用户级新手引导开关是否开启 */
  onboardEnabled: {
    type: Boolean,
    default: true
  },
  /** 系统级新手引导开关是否开启（关闭时不展示用户的开关项） */
  onboardGlobalEnabled: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['toggle-onboard'])

const router = useRouter()
const list = ref([])
const loading = ref(false)
const maxPinned = 20

const visible = computed(() => props.menus.length > 0)

/**
 * 已停用模块的常用功能不展示：
 * 模块在后台被关闭后，其菜单会级联停用，但 sys_user_shortcut 里仍有历史记录，
 * 若照常渲染就会点出 404。路径首段默认等于 module_key（/wms/xxx → wms）。
 */
const MODULE_PATH_ALIAS = { message: 'msg' }

function isModuleOff(path) {
  const off = props.disabledModules || []
  if (off.length === 0 || !path) return false
  const seg = String(path).split('?')[0].split('/').filter(Boolean)[0]
  const code = MODULE_PATH_ALIAS[seg] || seg
  return !!code && off.includes(code)
}

/** 实际渲染的常用功能（过滤停用模块） */
const visibleList = computed(() => list.value.filter(i => !isModuleOff(i.menuPath)))

/** 自动沉淀的条数（用于提示文案） */
const autoCount = computed(() => visibleList.value.filter(i => i.source !== 'M').length)

const dialog = reactive({
  visible: false,
  keyword: '',
  checked: [],
  saving: false
})

/** 按模块分组 + 关键词过滤 */
const groupedMenus = computed(() => {
  const kw = dialog.keyword.trim().toLowerCase()
  const source = kw ? props.menus.filter(m => (m.title || '').toLowerCase().includes(kw)) : props.menus
  const map = new Map()
  source.forEach(m => {
    const key = m.module || 'other'
    if (!map.has(key)) {
      map.set(key, { module: key, title: m.moduleTitle || '其他功能', menus: [] })
    }
    map.get(key).menus.push(m)
  })
  return Array.from(map.values())
})

/** 当前搜索结果的功能总数 */
const filteredCount = computed(() => groupedMenus.value.reduce((n, g) => n + g.menus.length, 0))

/** 某个分组内已勾选的数量 */
function groupPicked(group) {
  return group.menus.filter(m => dialog.checked.includes(m.path)).length
}

function load() {
  loading.value = true
  getShortcuts(props.limit)
    .then(res => {
      const raw = Array.isArray(res.data) ? res.data : []
      // 过滤历史脏数据：路径必须以单个 / 开头且标题非空，否则会出现"只有图标、点击 404"的按钮
      list.value = raw.filter(i => i && typeof i.menuPath === 'string'
        && i.menuPath.startsWith('/') && !i.menuPath.startsWith('//')
        && i.menuTitle)
    })
    .catch(() => {
      list.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

function go(item) {
  if (!item || !item.menuPath) return
  router.push(item.menuPath).catch(() => {})
}

function openDialog() {
  dialog.keyword = ''
  // 初始勾选 = 当前已手动固定的项
  dialog.checked = list.value.filter(i => i.source === 'M').map(i => i.menuPath)
  dialog.visible = true
}

function save() {
  if (dialog.checked.length > maxPinned) {
    ElMessage.warning(`最多固定 ${maxPinned} 个常用功能`)
    return
  }
  const pathMap = new Map(props.menus.map(m => [m.path, m]))
  const items = dialog.checked
    .map(path => pathMap.get(path))
    .filter(Boolean)
    .map(m => ({ path: m.path, title: m.title, icon: m.icon, module: m.module }))

  dialog.saving = true
  savePinnedShortcuts(items)
    .then(() => {
      dialog.visible = false
      ElMessage.success('常用功能已更新')
      load()
    })
    .catch(() => {
      ElMessage.error('保存失败，请稍后重试')
    })
    .finally(() => {
      dialog.saving = false
    })
}

onMounted(load)
// 返回首页时刷新（刚访问过的页面应立刻出现在常用功能里）
onActivated(load)
</script>

<style scoped>
.shortcut-bar {
  background: var(--hp-card, #fff);
  border: 1px solid var(--hp-border, #e4e7f1);
  border-radius: 14px;
  padding: 14px 18px;
  animation: fadeUp 0.6s 0.2s cubic-bezier(0.22, 1, 0.36, 1) both;
}

.shortcut-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.shortcut-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--hp-foreground, #0d111f);
}

.shortcut-title__icon {
  font-size: 15px;
  color: #f0a020;
}

.shortcut-tip {
  font-size: 12px;
  color: var(--hp-muted-fg, #5c6477);
}

.shortcut-edit {
  margin-left: auto;
  font-size: 12px;
}

.shortcut-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.shortcut-item {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 34px;
  padding: 0 12px;
  border: 1px solid var(--hp-border, #e4e7f1);
  border-radius: 9px;
  background: #fbfcff;
  cursor: pointer;
  transition: all 0.18s ease;
  max-width: 200px;
}

.shortcut-item:hover {
  border-color: color-mix(in srgb, var(--hp-primary, #4f46e5) 40%, #e4e7f1);
  background: color-mix(in srgb, var(--hp-primary, #4f46e5) 5%, #fff);
  transform: translateY(-1px);
}

.shortcut-item__icon {
  display: inline-flex;
  font-size: 14px;
  color: var(--hp-primary, #4f46e5);
}

.shortcut-item__text {
  font-size: 13px;
  color: #2c3444;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.shortcut-item__pin {
  font-size: 10px;
  line-height: 1;
  padding: 2px 4px;
  border-radius: 3px;
  color: var(--hp-primary, #4f46e5);
  background: color-mix(in srgb, var(--hp-primary, #4f46e5) 10%, #fff);
}

.shortcut-item--empty {
  border-style: dashed;
  color: var(--hp-muted-fg, #5c6477);
  background: transparent;
}

.shortcut-item--empty .shortcut-item__text {
  color: var(--hp-muted-fg, #5c6477);
}

/* ===== 弹窗外壳（teleport 到 body，用 :global 限定在 .shortcut-dialog 内） ===== */
:global(.shortcut-dialog) {
  /* 宽屏 1014px（原 780px +30%），窄屏自动收敛到 94vw 避免溢出 */
  max-width: 94vw;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 24px 64px rgba(16, 30, 54, 0.18);
}

/* 标题栏可拖动：抓手光标 + 禁止误选中文字 */
:global(.shortcut-dialog .el-dialog__header) {
  padding: 20px 24px 0;
  margin-right: 0;
  cursor: move;
  user-select: none;
}

:global(.shortcut-dialog .el-dialog__headerbtn) {
  top: 16px;
  right: 16px;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  transition: background 0.16s ease;
}

:global(.shortcut-dialog .el-dialog__headerbtn:hover) {
  background: #f2f4fa;
}

:global(.shortcut-dialog .el-dialog__body) {
  padding: 16px 24px 6px;
}

:global(.shortcut-dialog .el-dialog__footer) {
  padding: 14px 24px 18px;
  border-top: 1px solid #eef1f7;
}

/* ===== 弹窗头部 ===== */
.sd-head {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

/* 右侧拖动提示（避开右上角关闭按钮） */
.sd-head__drag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin: 4px 34px 0 auto;
  padding: 3px 8px;
  border-radius: 7px;
  font-size: 11px;
  line-height: 1;
  color: #9aa3b2;
  background: #f5f7fb;
  white-space: nowrap;
  transition: all 0.16s ease;
}

.sd-head__drag .el-icon {
  font-size: 12px;
}

:global(.shortcut-dialog .el-dialog__header:hover) .sd-head__drag {
  color: #6b7488;
  background: #eef1f8;
}

.sd-head__icon {
  flex: 0 0 auto;
  width: 38px;
  height: 38px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #fff;
  background: linear-gradient(135deg, #4f46e5, #7c8cff);
  box-shadow: 0 6px 14px rgba(79, 70, 229, 0.26);
}

.sd-head__text {
  flex: 1;
  min-width: 0;
}

.sd-head__title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.2px;
  color: #1c2431;
}

.sd-head__desc {
  margin: 3px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #8a94a3;
}

/* ===== 提示条 ===== */
.sd-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  margin-bottom: 12px;
  border: 1px solid #e2e8ff;
  border-radius: 9px;
  background: #f3f6ff;
  font-size: 12px;
  color: #4a5675;
}

.sd-notice .el-icon {
  color: #4f46e5;
  font-size: 14px;
}

.sd-notice b {
  color: #3f3ac9;
  font-weight: 600;
}

/* ===== 新手上路开关 ===== */
.sd-switch {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  margin-bottom: 12px;
  border: 1px solid #edf0f7;
  border-radius: 11px;
  background: #fbfcff;
}

.sd-switch__icon {
  flex: 0 0 auto;
  width: 30px;
  height: 30px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  color: #e8900f;
  background: #fff6e8;
}

.sd-switch__text {
  flex: 1;
  min-width: 0;
}

.sd-switch__title {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: #1f2637;
}

.sd-switch__desc {
  margin: 3px 0 0;
  font-size: 12px;
  color: #8a91a3;
}

/* ===== 搜索 ===== */
.sd-search {
  margin-bottom: 14px;
}

.sd-search__count {
  padding-right: 2px;
  font-size: 12px;
  color: #a3acb9;
}

:global(.shortcut-dialog .el-input__wrapper) {
  border-radius: 10px;
}

/* ===== 分组滚动区 ===== */
.sd-body {
  max-height: 54vh;
  overflow-y: auto;
  padding-right: 6px;
}

.sd-body::-webkit-scrollbar {
  width: 6px;
}

.sd-body::-webkit-scrollbar-thumb {
  background: #dfe4ee;
  border-radius: 3px;
}

.sd-body::-webkit-scrollbar-thumb:hover {
  background: #c8d0de;
}

.sd-group {
  margin-bottom: 18px;
}

.sd-group:last-child {
  margin-bottom: 6px;
}

.sd-group__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 9px;
  padding-bottom: 7px;
  border-bottom: 1px solid #f1f3f8;
}

.sd-group__title {
  font-size: 13px;
  font-weight: 600;
  color: #2c3444;
}

.sd-group__count {
  padding: 3px 7px;
  border-radius: 9px;
  background: #eef1f8;
  font-size: 11px;
  line-height: 1;
  color: #6b7488;
}

.sd-group__picked {
  margin-left: auto;
  font-size: 11px;
  font-weight: 500;
  color: #4f46e5;
}

/* ===== 勾选网格 ===== */
.sd-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 8px;
}

:global(.shortcut-dialog .el-checkbox.is-bordered.sd-check) {
  margin: 0;
  height: 34px;
  min-width: 0;
  padding: 0 10px;
  border: 1px solid #e6eaf3;
  border-radius: 8px;
  background: #fbfcff;
  transition: all 0.16s ease;
}

:global(.shortcut-dialog .el-checkbox.is-bordered.sd-check:hover) {
  border-color: #c7d2fe;
  background: #f7f9ff;
}

:global(.shortcut-dialog .el-checkbox.is-bordered.sd-check.is-checked) {
  border-color: #4f46e5;
  background: #f4f5ff;
}

:global(.shortcut-dialog .sd-check.is-checked .el-checkbox__label) {
  color: #3f3ac9;
  font-weight: 500;
}

:global(.shortcut-dialog .sd-check .el-checkbox__label) {
  min-width: 0;
  padding-left: 6px;
  font-size: 12.5px;
  color: #4a5265;
}

.sd-check__text {
  display: block;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* ===== 底部 ===== */
.sd-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sd-foot__counter {
  font-size: 12px;
  color: #8a91a3;
}

.sd-foot__counter b {
  font-size: 14px;
  font-weight: 600;
  color: #4f46e5;
}

.sd-foot__actions {
  display: flex;
  gap: 10px;
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

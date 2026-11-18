<template>
  <!-- 已完成状态：收成一行，避免完成后的卡片继续占版面 -->
  <section v-if="enabled && finished" class="onboard onboard--finished">
    <span class="onboard-done__icon">
      <svg-icon icon-class="validCode" />
    </span>
    <span class="onboard-done__text">
      新手上路已完成（{{ visibleTasks.length }} / {{ visibleTasks.length }} 步）· 后续可在「业务全景图」随时回顾
    </span>
    <el-button link type="primary" size="small" @click="emit('close')">不再显示</el-button>
  </section>

  <section v-else-if="enabled && visibleTasks.length > 0" class="onboard">
    <div class="onboard-head">
      <span class="onboard-head__icon" @click="toggleCollapse">
        <svg-icon icon-class="guide" />
      </span>
      <div class="onboard-head__main" @click="toggleCollapse">
        <div class="onboard-head__row">
          <h3 class="onboard-head__title">新手上路</h3>
          <span class="onboard-head__role" v-if="roleName">按「{{ roleName }}」岗位匹配 {{ visibleTasks.length }} 步</span>
        </div>
        <p class="onboard-head__desc" v-if="!collapsed">
          这些是你上手这份工作必须走通的一条主线，按顺序做，做完自动打勾。
        </p>
      </div>
      <div class="onboard-head__progress" @click="toggleCollapse">
        <span class="onboard-progress__num">{{ doneCount }}<em>/{{ visibleTasks.length }}</em></span>
        <span class="onboard-progress__bar">
          <span class="onboard-progress__fill" :style="{ width: percent + '%' }"></span>
        </span>
      </div>
      <button class="onboard-head__close" title="关闭新手上路（可在常用功能的自定义里重新开启）" @click.stop="emit('close')">
        <el-icon><Close /></el-icon>
      </button>
    </div>

    <ol class="onboard-list" v-show="!collapsed">
      <li
        v-for="(task, idx) in visibleTasks"
        :key="task.id"
        class="onboard-item"
        :class="{ 'is-done': task.done, 'is-next': idx === nextIndex }"
        @click="goTask(task)"
      >
        <span class="onboard-item__seq">
          <svg-icon v-if="task.done" icon-class="validCode" />
          <template v-else>{{ idx + 1 }}</template>
        </span>
        <div class="onboard-item__body">
          <p class="onboard-item__title">
            {{ task.title }}
            <span class="onboard-item__badge" v-if="idx === nextIndex && !task.done">建议先做这步</span>
          </p>
          <p class="onboard-item__desc" v-if="task.description">{{ task.description }}</p>
        </div>
        <span class="onboard-item__action">
          {{ task.done ? '已完成' : '去完成' }}
          <el-icon v-if="!task.done"><Right /></el-icon>
        </span>
      </li>
    </ol>
  </section>
</template>

<script setup name="OnboardCard">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Close, Right } from '@element-plus/icons-vue'

const props = defineProps({
  /** 岗位任务列表（由首页统一拉取） */
  tasks: {
    type: Array,
    default: () => []
  },
  /** 命中模板的岗位名称 */
  roleName: {
    type: String,
    default: ''
  },
  /** 当前用户可访问的叶子菜单，用于过滤掉无权访问的任务 */
  menus: {
    type: Array,
    default: () => []
  },
  /** 双层开关合并后的可见性（系统级 && 用户级） */
  enabled: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['close'])

const COLLAPSE_KEY = 'edop_onboard_collapsed'

const router = useRouter()
const collapsed = ref(localStorage.getItem(COLLAPSE_KEY) === '1')

/** 只保留用户有菜单权限的任务，避免出现"点不进去的任务" */
const visibleTasks = computed(() => {
  if (props.menus.length === 0) return props.tasks
  const accessible = new Set(props.menus.map(m => m.path))
  return props.tasks.filter(t => {
    if (!t.menuPath) return true
    if (t.menuPath === '/business-map') return true
    if (t.menuPath && t.menuPath.indexOf('/business-map/') === 0) return true
    return accessible.has(t.menuPath)
  })
})

const doneCount = computed(() => visibleTasks.value.filter(t => t.done).length)
const percent = computed(() => (visibleTasks.value.length ? Math.round((doneCount.value / visibleTasks.value.length) * 100) : 0))
const finished = computed(() => visibleTasks.value.length > 0 && doneCount.value === visibleTasks.value.length)
const nextIndex = computed(() => visibleTasks.value.findIndex(t => !t.done))

function toggleCollapse() {
  collapsed.value = !collapsed.value
  localStorage.setItem(COLLAPSE_KEY, collapsed.value ? '1' : '0')
}

function goTask(task) {
  if (!task || !task.menuPath) return
  // 归一化路径：历史数据里存在 '//xxx' 这类异常写法，直接 push 会落到 404
  const path = task.menuPath.replace(/\/{2,}/g, '/')
  router.push(path).catch(() => {})
}
</script>

<style scoped>
.onboard {
  background: linear-gradient(135deg, #f7f8ff 0%, #ffffff 45%);
  border: 1px solid var(--hp-border, #e4e7f1);
  border-radius: 14px;
  padding: 14px 18px 6px;
  animation: fadeUp 0.6s 0.12s cubic-bezier(0.22, 1, 0.36, 1) both;
}

.onboard-head {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding-bottom: 10px;
}

.onboard-head__icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: color-mix(in srgb, var(--hp-primary, #4f46e5) 10%, #fff);
  flex-shrink: 0;
  cursor: pointer;
}

.onboard-head__icon .svg-icon {
  font-size: 15px;
  color: var(--hp-primary, #4f46e5);
}

.onboard-head__main {
  min-width: 0;
  flex: 1;
  cursor: pointer;
}

.onboard-head__row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.onboard-head__title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--hp-foreground, #0d111f);
}

.onboard-head__role {
  font-size: 11px;
  color: var(--hp-primary, #4f46e5);
  background: color-mix(in srgb, var(--hp-primary, #4f46e5) 9%, #fff);
  border-radius: 4px;
  padding: 2px 7px;
  white-space: nowrap;
}

.onboard-head__desc {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--hp-muted-fg, #5c6477);
  line-height: 1.5;
}

.onboard-head__progress {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  padding-top: 4px;
  cursor: pointer;
}

.onboard-progress__num {
  font-size: 15px;
  font-weight: 700;
  color: var(--hp-primary, #4f46e5);
  font-variant-numeric: tabular-nums;
}

.onboard-progress__num em {
  font-style: normal;
  font-size: 11px;
  font-weight: 400;
  color: #a3a9ba;
}

.onboard-progress__bar {
  display: block;
  width: 68px;
  height: 4px;
  border-radius: 2px;
  background: #e9ebf4;
  overflow: hidden;
}

.onboard-progress__fill {
  display: block;
  height: 100%;
  border-radius: 2px;
  background: linear-gradient(90deg, #6366f1, #4f46e5);
  transition: width 0.4s ease;
}

.onboard-head__close {
  width: 22px;
  height: 22px;
  margin-top: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 5px;
  background: transparent;
  color: #a3a9ba;
  cursor: pointer;
  transition: all 0.16s;
  flex-shrink: 0;
}

.onboard-head__close:hover {
  background: #eceff7;
  color: #5c6477;
}

.onboard-head__close .el-icon {
  font-size: 13px;
}

.onboard-list {
  list-style: none;
  margin: 0;
  padding: 0 0 8px;
  border-top: 1px dashed #e6e9f3;
}

.onboard-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 8px;
  margin: 0 -8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.16s;
}

.onboard-item:hover {
  background: #f6f7fd;
}

.onboard-item.is-next {
  background: color-mix(in srgb, var(--hp-primary, #4f46e5) 4%, #fff);
}

.onboard-item__seq {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  color: #7b8398;
  background: #eef0f8;
  flex-shrink: 0;
  margin-top: 1px;
}

.onboard-item.is-done .onboard-item__seq {
  background: #e8f7ee;
  color: #1aab5c;
}

.onboard-item__seq .svg-icon {
  font-size: 12px;
}

.onboard-item__body {
  flex: 1;
  min-width: 0;
}

.onboard-item__title {
  margin: 0;
  font-size: 13px;
  font-weight: 500;
  color: #1f2637;
  display: flex;
  align-items: center;
  gap: 6px;
}

.onboard-item__badge {
  font-size: 10px;
  font-weight: 400;
  color: #fff;
  background: var(--hp-primary, #4f46e5);
  border-radius: 3px;
  padding: 1px 5px;
}

.onboard-item__desc {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--hp-muted-fg, #5c6477);
  line-height: 1.5;
}

.onboard-item.is-done .onboard-item__title {
  color: #9aa1b2;
  text-decoration: line-through;
  text-decoration-color: #d6dae6;
}

.onboard-item.is-done .onboard-item__desc {
  color: #b3b9c7;
}

.onboard-item__action {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: var(--hp-primary, #4f46e5);
  flex-shrink: 0;
  margin-top: 2px;
  opacity: 0;
  transition: opacity 0.16s;
}

.onboard-item:hover .onboard-item__action {
  opacity: 1;
}

.onboard-item.is-done .onboard-item__action {
  color: #1aab5c;
  opacity: 1;
}

/* 已完成态 */
.onboard--finished {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  background: #f5fbf7;
  border-color: #d9f0e2;
}

.onboard-done__icon .svg-icon {
  font-size: 15px;
  color: #1aab5c;
}

.onboard-done__text {
  flex: 1;
  font-size: 12px;
  color: #3f7f5c;
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

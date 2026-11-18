<template>
  <el-dialog v-model="visible" :title="title" width="94%" top="4vh" draggable class="oa-cal-dialog">
    <div class="oa-cal" v-loading="loading">
      <div class="oa-cal-toolbar">
        <div class="oa-cal-nav">
          <el-button size="small" icon="ArrowLeft" circle @click="moveMonth(-1)" />
          <span class="oa-cal-month">{{ monthLabel }}</span>
          <el-button size="small" icon="ArrowRight" circle @click="moveMonth(1)" />
          <el-button size="small" @click="goToday">今天</el-button>
        </div>
        <div class="oa-cal-legend">
          <span class="legend-dot is-active"></span>占用中
          <span class="legend-dot is-done"></span>已完成
          <span class="legend-dot is-cancel"></span>已撤销
          <span class="legend-dot is-conflict"></span>时间冲突
          <span class="oa-cal-total">共 {{ usages.length }} 条占用记录</span>
        </div>
      </div>

      <div class="oa-cal-grid" v-if="resources.length">
        <div class="oa-cal-row oa-cal-head">
          <div class="oa-cal-name">资源</div>
          <div class="oa-cal-track">
            <span v-for="d in days" :key="d.date" class="oa-cal-day" :class="{ weekend: d.weekend, today: d.today }">
              <b>{{ d.day }}</b><em>{{ d.week }}</em>
            </span>
          </div>
        </div>
        <div class="oa-cal-row" v-for="r in rows" :key="r.resourceId" :class="{ 'is-selected': selectedResource === r.resourceId }">
          <div class="oa-cal-name">
            <button type="button" class="resource-link" :class="{ 'is-active': selectedResource === r.resourceId }" @click="toggleResource(r)">{{ r.resourceName }}</button>
            <span class="resource-sub">{{ resourceStatusText(r) }}</span>
          </div>
          <div class="oa-cal-track">
            <span v-for="d in days" :key="d.date" class="oa-cal-cell" :class="{ weekend: d.weekend, today: d.today }"></span>
            <div
              v-for="(b, bi) in r.blocks"
              :key="bi"
              class="oa-cal-block"
              :class="[b.cls, { 'is-conflict': b.conflict }]"
              :style="{ left: b.left + '%', width: b.width + '%' }"
              :title="blockTitle(b)"
              @click="openFlow(b)"
            >
              {{ b.label }}
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else-if="!loading" description="暂无资源台账数据" :image-size="70" />

      <div class="oa-cal-list">
        <div class="oa-cal-list-title">
          {{ monthLabel }}占用明细
          <span v-if="selectedResourceName" class="oa-cal-filter">当前资源：{{ selectedResourceName }}
            <el-link type="primary" :underline="false" @click="clearResource">查看全部</el-link>
          </span>
        </div>
        <el-table :data="sortedUsages" size="small" border class="app-table" empty-text="本月暂无占用记录">
          <el-table-column prop="flowTitle" label="流程标题" min-width="200" show-overflow-tooltip>
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="openFlow(scope.row)">{{ scope.row.flowTitle || '查看流程' }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="resourceName" label="资源" width="150" />
          <el-table-column prop="applicant" label="申请人" width="120" />
          <el-table-column prop="startTime" label="开始时间" width="160" />
          <el-table-column prop="endTime" label="结束时间" width="160" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <span class="oa-cal-tag" :class="rowStatusClass(scope.row.procStatus)">{{ flowStatusText(scope.row.procStatus) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </el-dialog>
</template>

<script setup name="OaResourceCalendar">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getResourceCalendar } from '@/api/oa/resourceCalendar'

const props = defineProps({
  type: { type: String, required: true },
  title: { type: String, default: '资源使用日历' }
})

const router = useRouter()
const visible = ref(false)
const loading = ref(false)
const month = ref(firstDayOfMonth(new Date()))
const resources = ref([])
const usages = ref([])
const selectedResource = ref(null)

const selectedResourceName = computed(() => {
  if (selectedResource.value === null) return ''
  const hit = resources.value.find(r => r.resourceId === selectedResource.value)
  return hit ? hit.resourceName : ''
})

const weekNames = ['日', '一', '二', '三', '四', '五', '六']
const statusTextMap = {
  vehicle: { '0': '可用', '1': '使用中', '2': '维修中', '3': '停用', '4': '已报废' },
  driver: { '0': '在岗', '1': '休假', '2': '停用', '3': '出车中' },
  seal: { '0': '正常', '1': '停用', '2': '已注销', '3': '使用中' }
}

const monthLabel = computed(() => `${month.value.getFullYear()}年${month.value.getMonth() + 1}月`)

const days = computed(() => {
  const year = month.value.getFullYear()
  const monthIndex = month.value.getMonth()
  const total = new Date(year, monthIndex + 1, 0).getDate()
  const today = formatDate(new Date())
  return Array.from({ length: total }, (_, i) => {
    const d = new Date(year, monthIndex, i + 1)
    return { day: i + 1, date: formatDate(d), week: weekNames[d.getDay()], weekend: d.getDay() === 0 || d.getDay() === 6, today: formatDate(d) === today }
  })
})

const rows = computed(() => {
  const list = resources.value.map(r => ({ ...r, blocks: [] }))
  const dayCount = days.value.length
  if (!dayCount) return list
  const first = days.value[0].date
  const last = days.value[dayCount - 1].date
  const spans = []
  const rowMap = new Map(list.map(r => [r.resourceName, r]))
  usages.value.forEach(u => {
    const row = rowMap.get(u.resourceName)
    if (!row) return
    const rawStart = (u.startTime || '').substring(0, 10)
    if (!rawStart) return
    const rawEnd = (u.endTime || '').substring(0, 10) || rawStart
    const startDate = rawStart < first ? first : rawStart
    const endDate = rawEnd > last ? last : rawEnd
    if (endDate < first || startDate > last) return
    const startIndex = days.value.findIndex(d => d.date === startDate)
    const endIndex = days.value.findIndex(d => d.date === endDate)
    if (startIndex < 0 || endIndex < 0) return
    const item = {
      ...u,
      left: (startIndex / dayCount) * 100,
      width: ((endIndex - startIndex + 1) / dayCount) * 100,
      cls: blockClass(u.procStatus),
      conflict: false,
      label: u.flowTitle || '占用'
    }
    spans.push({ row, item, startIndex, endIndex })
    row.blocks.push(item)
  })
  for (let i = 0; i < spans.length; i++) {
    for (let j = i + 1; j < spans.length; j++) {
      if (spans[i].row === spans[j].row && spans[i].startIndex <= spans[j].endIndex && spans[j].startIndex <= spans[i].endIndex) {
        spans[i].item.conflict = true
        spans[j].item.conflict = true
      }
    }
  }
  return list
})

const sortedUsages = computed(() => {
  const name = selectedResourceName.value
  return usages.value
    .filter(u => !name || u.resourceName === name)
    .sort((a, b) => String(a.startTime).localeCompare(String(b.startTime)))
})

function formatDate(d) {
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

function firstDayOfMonth(d) {
  return new Date(d.getFullYear(), d.getMonth(), 1)
}

function blockClass(procStatus) {
  const s = String(procStatus || '')
  if (s === '2') return 'is-done'
  if (s === '3') return 'is-cancel'
  return 'is-active'
}

function rowStatusClass(procStatus) {
  return blockClass(procStatus)
}

function flowStatusText(procStatus) {
  const s = String(procStatus || '')
  return { '1': '审批中', '2': '已完成', '3': '已撤销' }[s] || '审批中'
}

function resourceStatusText(row) {
  const map = statusTextMap[props.type] || {}
  return map[String(row.resourceStatus)] || ''
}

function blockTitle(row) {
  const time = [row.startTime, row.endTime].filter(Boolean).join(' ~ ')
  return [row.flowTitle, row.resourceName, row.applicant, time].filter(Boolean).join('｜')
}

function load() {
  if (!days.value.length) return
  loading.value = true
  getResourceCalendar({ type: props.type, startDate: days.value[0].date, endDate: days.value[days.value.length - 1].date })
    .then(res => {
      const data = res.data || {}
      resources.value = data.resources || []
      usages.value = data.usages || []
    })
    .finally(() => { loading.value = false })
}

function moveMonth(step) {
  month.value = new Date(month.value.getFullYear(), month.value.getMonth() + step, 1)
  load()
}

function goToday() {
  month.value = firstDayOfMonth(new Date())
  load()
}

function openFlow(row) {
  if (!row.flowInstanceId) return
  const href = router.resolve({
    path: '/flow/detail/create',
    query: { instanceId: row.flowInstanceId, defName: row.flowTitle || '' }
  }).href
  window.open(href, '_blank')
}

function toggleResource(row) {
  selectedResource.value = selectedResource.value === row.resourceId ? null : row.resourceId
}

function clearResource() {
  selectedResource.value = null
}

function open() {
  visible.value = true
  selectedResource.value = null
  month.value = firstDayOfMonth(new Date())
  load()
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.oa-cal {
  --cal-name-width: 150px;
  --cal-min-track: 960px;
}
.oa-cal-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  gap: 12px;
  flex-wrap: wrap;
}
.oa-cal-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}
.oa-cal-month {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  min-width: 96px;
  text-align: center;
}
.oa-cal-legend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
}
.legend-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  display: inline-block;
  margin-left: 8px;
}
.legend-dot.is-active { background: #3b82f6; }
.legend-dot.is-done { background: #10b981; }
.legend-dot.is-cancel { background: #9ca3af; }
.legend-dot.is-conflict { background: #ef4444; }
.oa-cal-total { margin-left: 12px; color: #9ca3af; }

.oa-cal-grid {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  overflow: auto;
  max-height: 56vh;
}
.oa-cal-row {
  display: grid;
  grid-template-columns: var(--cal-name-width) 1fr;
  border-bottom: 1px solid #eef0f3;
}
.oa-cal-row:last-child { border-bottom: none; }
.oa-cal-row.oa-cal-head {
  position: sticky;
  top: 0;
  z-index: 3;
  background: #fafbfc;
}
.oa-cal-name {
  padding: 8px 12px;
  background: #fafbfc;
  border-right: 1px solid #eef0f3;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
  position: sticky;
  left: 0;
  z-index: 2;
}
.oa-cal-head .oa-cal-name {
  font-size: 12px;
  color: #6b7280;
}
.resource-link {
  background: none;
  border: none;
  padding: 0;
  text-align: left;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  color: #2563eb;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.resource-link:hover,
.resource-link.is-active {
  color: #1d4ed8;
  text-decoration: underline;
}
.oa-cal-row.is-selected { background: #f5f9ff; }
.oa-cal-row.is-selected .oa-cal-name { background: #eff6ff; }
.oa-cal-filter {
  margin-left: 10px;
  font-weight: 400;
  font-size: 12px;
  color: #6b7280;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.resource-sub {
  font-size: 11px;
  color: #9ca3af;
}
.oa-cal-track {
  position: relative;
  display: flex;
  min-height: 46px;
  min-width: var(--cal-min-track);
}
.oa-cal-head .oa-cal-track { min-height: 36px; }
.oa-cal-cell {
  flex: 1;
  min-width: 0;
  border-right: 1px solid #f3f4f6;
}
.oa-cal-cell:last-child { border-right: none; }
.oa-cal-cell.weekend { background: #fbfbfd; }
.oa-cal-cell.today { background: #eff6ff; }
.oa-cal-day {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #eef0f3;
  line-height: 1.2;
}
.oa-cal-day:last-child { border-right: none; }
.oa-cal-day b { font-size: 12px; font-weight: 600; color: #111827; }
.oa-cal-day em { font-style: normal; font-size: 10px; color: #9ca3af; }
.oa-cal-day.today b { color: #2563eb; }
.oa-cal-day.weekend b { color: #6b7280; }
.oa-cal-block {
  position: absolute;
  top: 8px;
  bottom: 8px;
  margin-left: 2px;
  border-radius: 6px;
  padding: 0 6px;
  font-size: 12px;
  line-height: 30px;
  color: #fff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  opacity: 0.92;
  transition: opacity 0.15s;
}
.oa-cal-block:hover { opacity: 1; }
.oa-cal-block.is-active { background: #3b82f6; }
.oa-cal-block.is-done { background: #10b981; }
.oa-cal-block.is-cancel { background: #9ca3af; }
.oa-cal-block.is-conflict { background: #ef4444; }

.oa-cal-list { margin-top: 16px; }
.oa-cal-list-title {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}
.oa-cal-tag {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 18px;
  color: #fff;
}
.oa-cal-tag.is-active { background: #3b82f6; }
.oa-cal-tag.is-done { background: #10b981; }
.oa-cal-tag.is-cancel { background: #9ca3af; }
</style>

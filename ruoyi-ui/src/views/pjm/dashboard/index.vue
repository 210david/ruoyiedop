<template>
  <div class="app-container pjm-list-page pjm-dashboard-page">
    <!-- ===== 项目选择 ===== -->
    <div class="surface filter-card">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 项目看板</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>选择项目</label>
          <div class="control">
            <el-input :model-value="selectedProjectLabel" placeholder="点击选择项目" readonly style="cursor:pointer" @click="openProjectPicker">
              <template #append><el-button icon="Search" @click.stop="openProjectPicker" /></template>
            </el-input>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 选择项目后查看项目进度、里程碑与风险/问题概览
        </div>
        <div class="filter-buttons">
          <button type="button" class="btn-soft is-outline" @click="loadAll">
            <el-icon><Refresh /></el-icon> 刷新
          </button>
          <el-button type="primary" icon="Search" @click="loadAll">搜索</el-button>
        </div>
      </div>
    </div>

    <div v-if="!selectedProjectId" class="surface">
      <el-empty description="请先选择一个项目" />
    </div>

    <template v-else>
      <!-- ===== 概览 ===== -->
      <div class="stat-grid">
        <div class="stat-card">
          <div class="stat-label">项目进度</div>
          <div class="stat-value">{{ Number(project?.progress || 0).toFixed(0) }}%</div>
          <div class="stat-sub">计划：{{ project?.startDate || '-' }} ~ {{ project?.endDate || '-' }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">项目状态</div>
          <div class="stat-value">{{ statusLabel(project?.status) || '-' }}</div>
          <div class="stat-sub">{{ project?.managerName || '未指定经理' }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">预算金额</div>
          <div class="stat-value">¥{{ Number(project?.budgetAmount || 0).toLocaleString() }}</div>
          <div class="stat-sub">计划采购 ¥{{ Number(project?.plannedPurchase || 0).toLocaleString() }}</div>
        </div>
        <div class="stat-card is-danger">
          <div class="stat-label">开放风险/问题</div>
          <div class="stat-value">{{ openRiskCount }}</div>
          <div class="stat-sub">共 {{ riskList.length }} 条记录</div>
        </div>
      </div>

      <!-- ===== 里程碑 ===== -->
      <div class="surface">
        <div class="toolbar">
          <div class="filter-title"><span class="glyph"></span>里程碑</div>
          <div class="right">
            <span class="stat-sub">共 {{ milestoneList.length }} 项</span>
          </div>
        </div>
        <div class="panel-body">
          <el-timeline v-if="milestoneList.length > 0">
            <el-timeline-item v-for="m in milestoneList" :key="m.milestoneId"
                              :type="milestoneTimelineType(m.status)"
                              :hollow="m.status !== '1'" :timestamp="m.planDate">
              <span class="ms-name">{{ m.milestoneName }}</span>
              <span class="badge" :class="milestoneBadge(m.status)">
                <span class="dot"></span>{{ milestoneLabel(m.status) }}
              </span>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无里程碑" :image-size="60" />
        </div>
      </div>

      <!-- ===== 风险 / 问题 ===== -->
      <div class="surface">
        <div class="toolbar">
          <div class="filter-title"><span class="glyph"></span>风险 / 问题</div>
          <div class="right">
            <span class="stat-sub">共 {{ riskList.length }} 条</span>
          </div>
        </div>
        <div class="table-wrap">
          <el-table :data="riskList" border class="app-table" max-height="360">
            <el-table-column label="类型" width="90" align="center">
              <template #default="scope">
                <span class="badge" :class="scope.row.riskType === '1' ? 'amber' : 'red'">{{ scope.row.riskType === '1' ? '风险' : '问题' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="标题" prop="title" show-overflow-tooltip min-width="180" />
            <el-table-column label="等级" width="100" align="center">
              <template #default="scope">
                <span class="badge" :class="riskLevelBadge(scope.row.level)">{{ riskLevelLabel(scope.row.level) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="110" align="center">
              <template #default="scope">
                <span class="badge" :class="riskStatusBadge(scope.row.status)">
                  <span class="dot"></span>{{ riskStatusLabel(scope.row.status) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </template>
    <ProjectPicker ref="projectPickerRef" @confirm="onProjectPickerConfirm" />
  </div>
</template>

<script setup name="PjmDashboard">
import { ref, computed, onMounted } from 'vue'
import { listProject, getProject } from '@/api/pjm/project'
import { listMilestone } from '@/api/pjm/milestone'
import { listRisk } from '@/api/pjm/risk'
import ProjectPicker from '@/components/ProjectPicker/index.vue'

const selectedProjectId = ref(null)
const projectOptions = ref([])
const projectPickerRef = ref()
const selectedProjectLabel = computed(() => {
  const p = projectOptions.value.find(x => String(x.projectId) === String(selectedProjectId.value))
  return p ? p.projectNo + ' ' + p.projectName : ''
})
function openProjectPicker() { projectPickerRef.value?.open(selectedProjectId.value) }
function onProjectPickerConfirm(p) {
  if (!p || !p.projectId) return
  if (!projectOptions.value.some(x => String(x.projectId) === String(p.projectId))) projectOptions.value.push(p)
  selectedProjectId.value = p.projectId
  loadAll()
}
const project = ref(null)
const milestoneList = ref([])
const riskList = ref([])

const openRiskCount = computed(() => riskList.value.filter(r => r.status === '0' || r.status === '1').length)

const statusLabel = (v) => ({
  '0': '草稿', '1': '待审批', '2': '审批通过', '3': '执行中',
  '4': '已完成', '5': '已关闭', '6': '暂停', '7': '驳回'
}[v] || v)

const milestoneLabel = (v) => ({ '0': '未达成', '1': '已达成', '2': '已延期' }[v] || v)
const milestoneBadge = (v) => ({ '0': 'gray', '1': 'green', '2': 'red' }[v] || 'gray')
const milestoneTimelineType = (v) => (v === '1' ? 'success' : v === '2' ? 'danger' : 'primary')

const riskLevelLabel = (v) => ({ '1': '低', '2': '中', '3': '高', '4': '紧急' }[v] || v || '-')
const riskLevelBadge = (v) => ({ '1': 'gray', '2': 'blue', '3': 'amber', '4': 'red' }[v] || 'gray')
const riskStatusLabel = (v) => ({ '0': '开放', '1': '处理中', '2': '已解决', '3': '已关闭' }[v] || v || '-')
const riskStatusBadge = (v) => ({ '0': 'red', '1': 'amber', '2': 'green', '3': 'gray' }[v] || 'gray')

function loadAll() {
  if (!selectedProjectId.value) return
  getProject(selectedProjectId.value).then(res => { project.value = res.data })
  listMilestone({ pageNum: 1, pageSize: 50, projectId: selectedProjectId.value }).then(res => { milestoneList.value = res.rows || [] })
  listRisk({ pageNum: 1, pageSize: 50, projectId: selectedProjectId.value }).then(res => { riskList.value = res.rows || [] })
}

onMounted(() => {
  listProject({ pageNum: 1, pageSize: 200 }).then(res => { projectOptions.value = res.rows || [] })
})
</script>

<style scoped>
/* 看板工具栏标题（共享样式仅覆盖 filter-card 内的 filter-title） */
.pjm-dashboard-page .toolbar .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pjm-dashboard-page .toolbar .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
/* 看板面板内容区 */
.pjm-dashboard-page .panel-body { padding:16px 20px 4px; }
.pjm-dashboard-page .panel-body :deep(.el-timeline) { padding-left:4px; }
.pjm-dashboard-page .panel-body :deep(.el-timeline-item__timestamp) { color:var(--ink-400); }
.pjm-dashboard-page .ms-name { font-size:14px; font-weight:600; color:var(--ink-900); margin-right:8px; }
</style>

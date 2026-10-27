<template>
  <el-dialog v-model="visible" width="816px" append-to-body draggable class="rd-dialog">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
        <span class="rd-detail-header-title">备件详情</span>
        <div class="rd-detail-header-sub" v-if="info.partCode">
          <div class="rd-detail-header-divider"></div>
          <span class="rd-detail-header-no">编号：{{ info.partCode }}</span>
        </div>
      </div>
    </template>
    <div v-loading="loading" class="rd-page">
      <!-- 基本信息 -->
      <section class="rd-card">
        <div class="rd-card-header" @click="toggleCard('v2')">
          <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.v2">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">备件编号</span><div class="rd-value">{{ info.partCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">备件名称</span><div class="rd-value">{{ info.partName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">备件类别</span><div class="rd-value"><dict-tag :options="dms_part_type" :value="info.partType" /></div></div>
            <div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ info.specModel || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="info.unit" /></div></div>
            <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ info.supplier || '-' }}</div></div>
          </div>
        </div>
      </section>
      <!-- 库存控制 -->
      <section class="rd-card">
        <div class="rd-card-header" @click="toggleCard('v1')">
          <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="21" x2="4" y2="14"/><line x1="4" y1="10" x2="4" y2="3"/><line x1="12" y1="21" x2="12" y2="12"/><line x1="12" y1="8" x2="12" y2="3"/><line x1="20" y1="21" x2="20" y2="16"/><line x1="20" y1="12" x2="20" y2="3"/><line x1="1" y1="14" x2="7" y2="14"/><line x1="9" y1="8" x2="15" y2="8"/><line x1="17" y1="16" x2="23" y2="16"/></svg></span>库存控制</div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.v1">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">库存下限</span><div class="rd-value">{{ info.stockMin != null ? info.stockMin : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">库存上限</span><div class="rd-value">{{ info.stockMax != null ? info.stockMax : '-' }}</div></div>
          </div>
        </div>
      </section>
      <!-- 其他信息 -->
      <section class="rd-card">
        <div class="rd-card-header" @click="toggleCard('v0')">
          <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.v0">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">备件状态</span><div class="rd-value"><el-tag :type="info.status === '0' ? 'success' : 'danger'">{{ info.status === '0' ? '正常' : '停用' }}</el-tag></div></div>
            <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ parseTime(info.createTime) || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ info.remark || '-' }}</div></div>
          </div>
        </div>
      </section>
    </div>
    <template #footer>
      <el-button @click="visible = false">关 闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="DmsSparePartViewDrawer">
import { ref, reactive, nextTick } from 'vue'
import { getSparepart } from '@/api/dms/sparepart'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['v2', 'v1', 'v0'])

const { proxy } = getCurrentInstance()
const { wms_unit, dms_part_type } = proxy.useDict('wms_unit', 'dms_part_type')

const visible = ref(false)
const loading = ref(false)
const info = reactive({})

const open = async (partId) => {
  visible.value = true
  loading.value = true
  try {
    const res = await getSparepart(partId)
    Object.keys(info).forEach(k => { info[k] = undefined })
    Object.assign(info, res.data)
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>

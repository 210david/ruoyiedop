<template>
  <!-- 打印专用视图：屏幕上不显示，仅 window.print() 时作为唯一内容输出（A4 纵向）
       必须 Teleport 到 body：#app 在打印时整体隐藏，留在里面会被一起隐藏 -->
  <Teleport to="body">
    <div id="flow-print-root" class="fp-root">
    <div class="fp-sheet">
      <header class="fp-head">
        <div class="fp-head-main">
          <div class="fp-process">{{ meta.processName || '流程审批单' }}</div>
          <div class="fp-title">{{ meta.title || '—' }}</div>
        </div>
        <div class="fp-head-side">
          <div class="fp-kv"><span>当前节点</span><b>{{ meta.nodeName || '—' }}</b></div>
          <div class="fp-kv"><span>实例编号</span><b>{{ shortId(meta.instanceId) }}</b></div>
        </div>
      </header>

      <div class="fp-bar">
        <span class="fp-chip" :class="{ 'is-tpl': !!templateName }">
          {{ templateName ? '打印模板：' + templateName : '打印模板：当前节点显示模板' }}
        </span>
        <span class="fp-chip">打印人：{{ meta.printUser || '—' }}</span>
        <span class="fp-chip">打印时间：{{ printTime }}</span>
      </div>

      <div class="fp-body">
        <FormGridRender v-if="grid" :grid="grid" :fields="fields" :values="values" :editable="false" />
        <table class="fp-plain" v-else-if="fields.length">
          <tbody>
            <tr v-for="f in fields" :key="f.field">
              <th>{{ f.label || f.field }}</th>
              <td>{{ displayValue(f) }}</td>
            </tr>
          </tbody>
        </table>
        <div class="fp-empty" v-else>该节点未配置表单内容</div>
      </div>

      <footer class="fp-foot">
        <span>本页由系统自动生成，打印内容以系统记录为准</span>
        <span>{{ meta.printUser || '' }} · {{ printTime }}</span>
      </footer>
    </div>
    </div>
  </Teleport>
</template>

<script setup name="FlowPrintView">
import FormGridRender from '@/components/FormGridRender.vue'

const props = defineProps({
  /** 打印抬头信息：{ processName, title, nodeName, instanceId, printUser } */
  meta: { type: Object, default: () => ({}) },
  /** 表单字段定义（打印模板快照，或当前节点显示模板） */
  fields: { type: Array, default: () => [] },
  /** 网格布局（同上，为空时回退字段列表） */
  grid: { type: Object, default: null },
  /** 表单数据 */
  values: { type: Object, default: () => ({}) },
  /** 打印模板名称；为空表示用的是「当前节点显示模板」 */
  templateName: { type: String, default: '' }
})

/** 打印时间：挂载即定格，避免打印瞬间与预览不一致 */
const printTime = ref(formatNow())

function formatNow() {
  const d = new Date()
  const p = n => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + ' ' + p(d.getHours()) + ':' + p(d.getMinutes())
}

/** 实例编号太长时只留首尾，打印件上主要给人眼核对 */
function shortId(id) {
  const s = String(id || '')
  return s.length > 18 ? s.slice(0, 8) + '…' + s.slice(-6) : (s || '—')
}

/** 无网格布局时的纯文本回显：明细表只显示条数，避免打印件被撑爆 */
function displayValue(f) {
  const v = props.values ? props.values[f.field] : ''
  if (Array.isArray(v)) return v.length ? v.length + ' 条明细' : '—'
  if (v === null || v === undefined || v === '') return '—'
  return String(v)
}

/* 打印期间给 body 打标记：隐藏 #app，只输出本视图 */
onMounted(() => document.body.classList.add('fp-printing'))
onBeforeUnmount(() => document.body.classList.remove('fp-printing'))
</script>

<style lang="scss" scoped>
.fp-root {
  display: none;
  color: #1f2937;
  font-size: 13px;
  line-height: 1.6;
  background: #fff;

  .fp-sheet {
    width: 100%;
    margin: 0 auto;
  }

  .fp-head {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 16px;
    padding-bottom: 10px;
    border-bottom: 2px solid #1f2937;

    .fp-process {
      font-size: 13px;
      color: #4b5563;
      letter-spacing: 2px;
    }

    .fp-title {
      margin-top: 2px;
      font-size: 22px;
      font-weight: 700;
      letter-spacing: 2px;
    }

    .fp-head-side {
      text-align: right;
      white-space: nowrap;
    }

    .fp-kv {
      font-size: 12px;
      color: #6b7280;

      b {
        margin-left: 6px;
        color: #1f2937;
        font-weight: 600;
      }
    }
  }

  .fp-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 6px 10px;
    padding: 8px 0;

    .fp-chip {
      padding: 1px 8px;
      border: 1px solid #d1d5db;
      border-radius: 3px;
      font-size: 12px;
      color: #4b5563;

      &.is-tpl {
        border-color: #93c5fd;
        color: #1d4ed8;
        background: #eff6ff;
      }
    }
  }

  .fp-body {
    padding-top: 4px;

    /* 打印件的表格：整宽铺开、允许内容换行，避免长文本被裁掉 */
    :deep(.fgr-wrap) {
      overflow: visible !important;
    }

    :deep(.fgr-table) {
      width: 100% !important;
      table-layout: fixed;
      border-collapse: collapse;
    }

    :deep(.fgr-table col) {
      width: auto !important;
    }

    :deep(.fgr-cell) {
      word-break: break-word;
      white-space: normal !important;
    }

    :deep(.fgr-title) {
      font-size: 16px;
      font-weight: 700;
    }

    :deep(.fgr-dtable) {
      width: 100% !important;

      th,
      td {
        word-break: break-word;
        white-space: normal !important;
      }
    }
  }

  .fp-plain {
    width: 100%;
    border-collapse: collapse;

    th,
    td {
      border: 1px solid #d1d5db;
      padding: 6px 10px;
      text-align: left;
      vertical-align: top;
      font-weight: 400;
    }

    th {
      width: 22%;
      background: #f9fafb;
      color: #4b5563;
      white-space: nowrap;
    }
  }

  .fp-empty {
    padding: 24px 0;
    text-align: center;
    color: #9ca3af;
  }

  .fp-foot {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    margin-top: 18px;
    padding-top: 8px;
    border-top: 1px solid #e5e7eb;
    font-size: 11px;
    color: #9ca3af;
  }
}
</style>

<style lang="scss">
/* 打印输出：只保留打印视图，页面其余部分（导航、按钮、页签）全部不进纸面 */
@media print {
  @page {
    size: A4 portrait;
    margin: 14mm 12mm;
  }

  body.fp-printing #app,
  body.fp-printing .el-overlay {
    display: none !important;
  }

  body.fp-printing #flow-print-root {
    display: block !important;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }

  body.fp-printing #flow-print-root .fp-head {
    break-inside: avoid;
  }
}
</style>

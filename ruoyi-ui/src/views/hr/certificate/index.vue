<template>
  <div class="app-container hr-certificate-page">
    <!-- 筛选条件卡片 -->
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
          <label>员工姓名</label>
          <div class="control">
            <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工号</label>
          <div class="control">
            <el-input v-model="queryParams.employeeNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>证书类别</label>
          <div class="control">
            <el-select v-model="queryParams.certType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in certTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>证书名称</label>
          <div class="control">
            <el-input v-model="queryParams.certName" placeholder="请输入证书名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>证书编号</label>
          <div class="control">
            <el-input v-model="queryParams.certNo" placeholder="请输入证书编号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>发证日期</label>
          <div class="control">
            <el-date-picker v-model="issueDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>有效起始日</label>
          <div class="control">
            <el-date-picker v-model="validFromRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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

    <!-- 数据列表卡片 -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:certificate:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:certificate:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:certificate:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:certificate:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_certificate_index"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrCertificateList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" :width="colWidth('序号', 85)" align="center" />
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :width="colWidth('employeeName', 110)" resizable v-if="columns.employeeName.visible" />
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 110)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="证书类别" prop="certType" key="certType" align="center" :width="colWidth('certType', 120)" resizable v-if="columns.certType.visible">
            <template #default="scope">
              <span v-if="scope.row.certType" class="badge" :class="dictBadgeClass(certTypeOptions, scope.row.certType, 'blue')"><span class="dot"></span>{{ dictLabel(certTypeOptions, scope.row.certType) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="证书名称" prop="certName" key="certName" align="center" :width="colWidth('certName', 180)" resizable show-overflow-tooltip v-if="columns.certName.visible" />
          <el-table-column label="证书编号" prop="certNo" key="certNo" align="center" :width="colWidth('certNo', 150)" resizable v-if="columns.certNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.certNo }}</span></template>
          </el-table-column>
          <el-table-column label="发证日期" prop="issueDate" key="issueDate" align="center" :width="colWidth('issueDate', 120)" resizable v-if="columns.issueDate.visible" />
          <el-table-column label="有效起始日" prop="validFrom" key="validFrom" align="center" :width="colWidth('validFrom', 120)" resizable v-if="columns.validFrom.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:certificate:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:certificate:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:certificate:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrCertificateRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="姓名" prop="employeeId">
                  <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
                    <template #append>
                      <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
                      <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12">
                  <el-form-item label="工号" prop="employeeNo">
                    <el-input v-model="form.employeeNo" placeholder="请输入工号" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="证书类别" prop="certType">
                    <el-select v-model="form.certType" placeholder="请选择" clearable style="width: 100%">
                      <el-option v-for="d in certTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="证书名称" prop="certName">
                    <el-input v-model="form.certName" placeholder="请输入证书名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="证书编号" prop="certNo">
                    <el-input v-model="form.certNo" placeholder="自动生成" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>有效期信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="发证日期" prop="issueDate">
                    <el-date-picker v-model="form.issueDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="有效起始日" prop="validFrom">
                    <el-date-picker v-model="form.validFrom" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="有效期至" prop="validTo">
                    <el-date-picker v-model="form.validTo" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="复审日期" prop="reviewDate">
                    <el-date-picker v-model="form.reviewDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="发证机构" prop="issueOrg">
                    <el-input v-model="form.issueOrg" placeholder="请输入发证机构" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="预警状态" prop="remindStatus">
                    <el-select v-model="form.remindStatus" placeholder="请选择" clearable style="width: 100%">
                      <el-option v-for="d in remindStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="附件URL" prop="attachmentUrl">
                    <el-input v-model="form.attachmentUrl" placeholder="请输入附件URL" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">证书台账详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.certNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">证书编号：{{ viewData.certNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.vc0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">证书类别</span><div class="rd-value"><dict-tag :options="certTypeOptions" :value="viewData.certType" /></div></div>
              <div class="rd-item"><span class="rd-label">证书名称</span><div class="rd-value">{{ viewData.certName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">证书编号</span><div class="rd-value">{{ viewData.certNo || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>有效期信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.vc1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">发证日期</span><div class="rd-value">{{ viewData.issueDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">有效起始日</span><div class="rd-value">{{ viewData.validFrom || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">有效期至</span><div class="rd-value">{{ viewData.validTo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">复审日期</span><div class="rd-value">{{ viewData.reviewDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">预警状态</span><div class="rd-value"><dict-tag :options="remindStatusOptions" :value="viewData.remindStatus" /></div></div>
              <div class="rd-item"><span class="rd-label">发证机构</span><div class="rd-value">{{ viewData.issueOrg || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.vc2">
            <div class="rd-grid">
              <div class="rd-item rd-item--full" v-if="viewData.attachmentUrl">
                <span class="rd-label">证照附件</span>
                <div class="rd-value">
                  <div class="rd-file-links">
                    <div class="rd-file-item" v-for="(url, idx) in String(viewData.attachmentUrl).split(',')" :key="idx">
                      <div class="rd-file-link" @click="handleFilePreview(url)">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                        <span class="rd-file-name">{{ url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url }}</span>
                      </div>
                      <span class="rd-file-dl" @click="handleFileDownload(url)">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                        下载
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />

    <file-preview ref="filePreviewRef" />
  </div>
</template>

<script setup name="HrCertificate">
import { listHrCertificate, getHrCertificate, addHrCertificate, updateHrCertificate, delHrCertificate } from '@/api/hr/hrCertificate'
import { useColumnResize } from '@/composables/useColumnResize'
import FilePreview from '@/components/FilePreview/index.vue'
import EmployeePicker from '@/components/EmployeePicker';
import { downloadFile } from '@/utils/downloadFile'

const { proxy } = getCurrentInstance()

const { hr_cert_type: certTypeOptions } = proxy.useDict('hr_cert_type')
const { hr_remind_status: remindStatusOptions } = proxy.useDict('hr_remind_status')

const employeePickerRef = ref(null)

const hrCertificateList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const title = ref('')
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const showAdvanced = ref(false)
const issueDateRange = ref([])
const validFromRange = ref([])
const filePreviewRef = ref(null)
const collapsedCards = reactive({ vc0: false, vc1: false, vc2: false, e0: false, e1: false, e2: false })

/** 列显隐与列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_certificate_index')

const defaultColumns = {
  employeeName: { label: '员工姓名', visible: true },
  employeeNo: { label: '工号', visible: true },
  certType: { label: '证书类别', visible: true },
  certName: { label: '证书名称', visible: true },
  certNo: { label: '证书编号', visible: true },
  issueDate: { label: '发证日期', visible: true },
  validFrom: { label: '有效起始日', visible: true },
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_certificate_index')
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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, employeeName: undefined, employeeNo: undefined, certType: undefined, certName: undefined, certNo: undefined },
  rules: {
    employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }],
    certType: [{ required: true, message: '证书类别不能为空', trigger: 'change' }],
    certName: [{ required: true, message: '证书名称不能为空', trigger: 'blur' }],
    validTo: [{ required: true, message: '有效期至不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.employeeName) count++
  if (queryParams.value.employeeNo) count++
  if (queryParams.value.certType) count++
  if (queryParams.value.certName) count++
  if (queryParams.value.certNo) count++
  if (issueDateRange.value && issueDateRange.value.length === 2) count++
  if (validFromRange.value && validFromRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  listHrCertificate(queryParams.value).then(response => {
    hrCertificateList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, issueDateRange.value, 'IssueDate')
  proxy.addDateRange(queryParams.value, validFromRange.value, 'ValidFrom')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  queryParams.value.certType = undefined
  queryParams.value.certName = undefined
  queryParams.value.certNo = undefined
  issueDateRange.value = []
  validFromRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

/** 字典展示辅助：标签与徽章颜色（按字典 elTagType 映射） */
function dictLabel(options, v) {
  if (v === undefined || v === null || v === '') return '-'
  const d = (options || []).find(o => String(o.value) === String(v))
  return d ? d.label : String(v)
}
function dictBadgeClass(options, v, fallback = 'gray') {
  const d = (options || []).find(o => String(o.value) === String(v))
  const map = { success: 'green', primary: 'blue', info: 'gray', warning: 'amber', danger: 'red' }
  return map[d && d.elTagType] || fallback
}

/** ===== 人员弹框选择 ===== */
function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId); }
function onEmployeePickerConfirm(emp) {
  form.value.employeeId = emp.employeeId;
  form.value.employeeName = emp.employeeName;
}
function onEmployeeFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.employeeId) { form.value.employeeId = null; form.value.employeeName = ''; }
    else { openEmployeePicker(); }
    return;
  }
  openEmployeePicker();
}

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function handleFilePreview(url) {
  const name = url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url
  proxy.$refs.filePreviewRef.open(url, name)
}

function handleFileDownload(url) {
  downloadFile(url)
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.certId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    certType: undefined,
    certName: undefined,
    certNo: undefined,
    issueDate: undefined,
    validFrom: undefined,
    validTo: undefined,
    reviewDate: undefined,
    issueOrg: undefined,
    attachmentUrl: undefined,
    remindStatus: undefined
  }
  proxy.resetForm('hrCertificateRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增证书台账'
}

function handleUpdate(row) {
  reset()
  const certId = row.certId || ids.value[0]
  getHrCertificate(certId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改证书台账'
  })
}

function handleView(row) {
  const certId = row.certId
  getHrCertificate(certId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['hrCertificateRef'].validate(valid => {
    if (valid) {
      if (form.value.certId != undefined) {
        updateHrCertificate(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrCertificate(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const certIds = row.certId || ids.value
  proxy.$modal.confirm('是否确认删除选中的证书台账？').then(function() {
    return delHrCertificate(certIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/certificate/export', { ...queryParams.value }, `hrCertificate_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-certificate-page {
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

/* ===== Surface Card ===== */
.hr-certificate-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-certificate-page .filter-card { padding:14px 20px 16px; }
.hr-certificate-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-certificate-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-certificate-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-certificate-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-certificate-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-certificate-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-certificate-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-certificate-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-certificate-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-certificate-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-certificate-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-certificate-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-certificate-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-certificate-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-certificate-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-certificate-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-certificate-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-certificate-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-certificate-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-certificate-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-certificate-page .field .control :deep(.el-select) { width:100%; }
.hr-certificate-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-certificate-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-certificate-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-certificate-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-certificate-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-certificate-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-certificate-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-certificate-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-certificate-page .btn-soft .el-icon { font-size:14px; }
.hr-certificate-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-certificate-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-certificate-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-certificate-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-certificate-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-certificate-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-certificate-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-certificate-page .table-wrap { overflow-x:auto; }
.hr-certificate-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-certificate-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-certificate-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-certificate-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-certificate-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-certificate-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-certificate-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-certificate-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-certificate-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-certificate-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-certificate-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-certificate-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-certificate-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-certificate-page .badge.amber .dot { background:var(--amber-500); }
.hr-certificate-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-certificate-page .badge.blue .dot { background:var(--blue-500); }
.hr-certificate-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-certificate-page .badge.green .dot { background:var(--green-500); }
.hr-certificate-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-certificate-page .badge.red .dot { background:var(--red-500); }
.hr-certificate-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-certificate-page .badge.violet .dot { background:var(--brand-500); }
.hr-certificate-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-certificate-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-certificate-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-certificate-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-certificate-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-certificate-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-certificate-page .pagination-container :deep(.el-pagination .btn-prev), .hr-certificate-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-certificate-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-certificate-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-certificate-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 查看详情弹窗（rd-*) ===== */
.hr-certificate-page .rd-page {
  max-height: 60vh;
  overflow-y: auto;
}
.hr-certificate-page .rd-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
  animation: rdFadeIn 0.4s ease-out forwards;
}
.hr-certificate-page .rd-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f5f7fa;
  cursor: pointer;
  user-select: none;
}
.hr-certificate-page .rd-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.hr-certificate-page .rd-card-icon {
  display: flex;
  align-items: center;
  color: #409eff;
}
.hr-certificate-page .rd-collapse-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  color: #909399;
  transition: transform 0.2s;
}
.hr-certificate-page .rd-collapse-btn.is-collapsed {
  transform: rotate(-90deg);
}
.hr-certificate-page .rd-card-body {
  padding: 16px;
}
.hr-certificate-page .rd-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 24px;
}
.hr-certificate-page .rd-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.hr-certificate-page .rd-item--full {
  grid-column: 1 / -1;
}
.hr-certificate-page .rd-label {
  font-size: 12px;
  color: #909399;
}
.hr-certificate-page .rd-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.hr-certificate-page .rd-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  color: #c0c4cc;
}
.hr-certificate-page .rd-empty-icon {
  margin-bottom: 8px;
}
.hr-certificate-page .rd-empty-text {
  font-size: 13px;
  margin: 0;
}
.hr-certificate-page .rd-detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.hr-certificate-page .rd-detail-header-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
}
.hr-certificate-page .rd-detail-header-main {
  flex: 1;
}
.hr-certificate-page .rd-detail-header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.hr-certificate-page .rd-detail-header-sub {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}
.hr-certificate-page .rd-detail-header-divider {
  width: 1px;
  height: 12px;
  background: #dcdfe6;
}
.hr-certificate-page .rd-detail-header-no {
  font-size: 13px;
  color: #909399;
}

/* ===== rd-dialog header 覆盖（全局 detail-page.scss 兜底） ===== */
:deep(.rd-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top: 16px; right: 16px; z-index: 10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }

@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-certificate-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-certificate-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-certificate-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>

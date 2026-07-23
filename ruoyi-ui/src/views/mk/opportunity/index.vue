<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="商机编号" prop="opportunityNo"><el-input v-model="queryParams.opportunityNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="商机名称" prop="opportunityName"><el-input v-model="queryParams.opportunityName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="销售阶段" prop="stageCode">
        <el-select v-model="queryParams.stageCode" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="s in stageOptions" :key="s.stageCode" :label="s.stageName" :value="s.stageCode" />
        </el-select>
      </el-form-item>
      <el-form-item label="商机状态" prop="opportunityStatus">
        <el-select v-model="queryParams.opportunityStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_opportunity_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:opportunity:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:opportunity:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:opportunity:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:opportunity:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="商机编号" prop="opportunityNo" :width="colWidth('opportunityNo', 150)" resizable />
      <el-table-column label="商机名称" prop="opportunityName" show-overflow-tooltip />
      <el-table-column label="客户名称" prop="customerName" show-overflow-tooltip />
      <el-table-column label="销售阶段" prop="stageName" :width="colWidth('stageName', 100)" resizable align="center" />
      <el-table-column label="预计金额" prop="expectedAmount" :width="colWidth('expectedAmount', 120)" resizable align="right" />
      <el-table-column label="赢率" prop="winRate" :width="colWidth('winRate', 80)" resizable align="center">
        <template #default="scope">{{ scope.row.winRate }}%</template>
      </el-table-column>
      <el-table-column label="加权金额" prop="weightedAmount" :width="colWidth('weightedAmount', 120)" resizable align="right" />
      <el-table-column label="商机状态" prop="opportunityStatus" :width="colWidth('opportunityStatus', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_opportunity_status" :value="scope.row.opportunityStatus" /></template>
      </el-table-column>
      <el-table-column label="负责人" prop="userName" :width="colWidth('userName', 100)" resizable />
      <el-table-column label="操作" width="320" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:opportunity:edit']">修改</el-button>
          <el-dropdown v-if="scope.row.opportunityStatus === '0'" @command="(cmd) => handleAction(cmd, scope.row)" style="margin-left: 8px">
            <el-button link type="primary">更多<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="advance">推进阶段</el-dropdown-item>
                <el-dropdown-item command="retreat">退回阶段</el-dropdown-item>
                <el-dropdown-item command="win" divided style="color: #67c23a">赢单关单</el-dropdown-item>
                <el-dropdown-item command="lose" style="color: #f56c6c">输单关单</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button v-if="scope.row.opportunityStatus !== '0'" link type="warning" @click="handleReopen(scope.row)">重开</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改弹窗 -->
    <el-dialog :title="title" v-model="open" width="850px" append-to-body>
      <el-form ref="opportunityRef" :model="form" :rules="rules" label-width="110px">
        <el-collapse v-model="activeNames">
          <el-collapse-item title="基本信息" name="basic">
            <el-row>
              <el-col :span="12"><el-form-item label="商机编号" prop="opportunityNo"><el-input v-model="form.opportunityNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="商机名称" prop="opportunityName"><el-input v-model="form.opportunityName" placeholder="请输入商机名称" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="所属客户" prop="customerId">
                <el-select v-model="form.customerId" filterable clearable placeholder="请选择客户" style="width: 100%" @change="onCustomerChange">
                  <el-option v-for="c in customerOptions" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="主要联系人" prop="contactId">
                <el-select v-model="form.contactId" filterable clearable placeholder="请选择联系人" style="width: 100%">
                  <el-option v-for="c in contactOptions" :key="c.contactId" :label="c.name" :value="c.contactId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="商机来源" prop="opportunitySource">
                <el-select v-model="form.opportunitySource" placeholder="请选择" style="width: 100%"><el-option v-for="d in marketing_opportunity_source" :key="d.value" :label="d.label" :value="d.value" /></el-select>
              </el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="金额与阶段" name="amount">
            <el-row>
              <el-col :span="12"><el-form-item label="预计金额" prop="expectedAmount"><el-input-number v-model="form.expectedAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="预计成交日期" prop="expectedDate"><el-date-picker v-model="form.expectedDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="销售阶段" prop="stageCode">
                <el-select v-model="form.stageCode" placeholder="请选择" style="width: 100%" @change="onStageChange">
                  <el-option v-for="s in stageOptions" :key="s.stageCode" :label="s.stageName" :value="s.stageCode" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="赢率" prop="winRate"><el-input-number v-model="form.winRate" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="商机状态" prop="opportunityStatus">
                <el-select v-model="form.opportunityStatus" placeholder="请选择" style="width: 100%"><el-option v-for="d in marketing_opportunity_status" :key="d.value" :label="d.label" :value="d.value" /></el-select>
              </el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="竞争与方案" name="solution">
            <el-row>
              <el-col :span="12"><el-form-item label="竞争对手" prop="competitor"><el-input v-model="form.competitor" placeholder="请输入竞争对手" /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="客户痛点" prop="painPoint"><el-input v-model="form.painPoint" type="textarea" :rows="2" placeholder="请输入客户痛点" /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="解决方案" prop="solution"><el-input v-model="form.solution" type="textarea" :rows="2" placeholder="请输入解决方案" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="负责与跟进" name="owner">
            <el-row>
              <el-col :span="12"><el-form-item label="负责人" prop="userId">
                <el-select v-model="form.userId" filterable clearable placeholder="请选择负责人" style="width: 100%" @change="onUserChange">
                  <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="所属部门" prop="deptId">
                <el-tree-select v-model="form.deptId" :data="deptOptions" :props="{ value: 'id', label: 'label', children: 'children' }" value-key="id" placeholder="请选择部门" check-strictly clearable style="width: 100%" />
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="下一步行动" prop="nextAction"><el-input v-model="form.nextAction" placeholder="请输入下一步行动" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="下次行动时间" prop="nextTime"><el-date-picker v-model="form.nextTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="其他信息" name="other">
            <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 详情弹窗 - Tab页 -->
    <el-dialog title="商机详情" v-model="viewOpen" width="900px" append-to-body @open="loadRelations">
      <el-tabs v-model="detailTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-collapse v-model="viewActiveNames">
            <el-collapse-item title="基本信息" name="basic">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="商机编号">{{ viewForm.opportunityNo }}</el-descriptions-item>
                <el-descriptions-item label="商机名称">{{ viewForm.opportunityName }}</el-descriptions-item>
                <el-descriptions-item label="所属客户">{{ viewForm.customerName }}</el-descriptions-item>
                <el-descriptions-item label="主要联系人">{{ viewForm.contactName }}</el-descriptions-item>
                <el-descriptions-item label="商机来源"><dict-tag :options="marketing_opportunity_source" :value="viewForm.opportunitySource" /></el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>
            <el-collapse-item title="金额与阶段" name="amount">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="销售阶段">{{ viewForm.stageName }}</el-descriptions-item>
                <el-descriptions-item label="预计金额">¥{{ viewForm.expectedAmount }}</el-descriptions-item>
                <el-descriptions-item label="预计成交">{{ viewForm.expectedDate }}</el-descriptions-item>
                <el-descriptions-item label="赢率">{{ viewForm.winRate }}%</el-descriptions-item>
                <el-descriptions-item label="加权金额">¥{{ viewForm.weightedAmount }}</el-descriptions-item>
                <el-descriptions-item label="商机状态"><dict-tag :options="marketing_opportunity_status" :value="viewForm.opportunityStatus" /></el-descriptions-item>
                <el-descriptions-item v-if="viewForm.actualAmount" label="实际成交金额">¥{{ viewForm.actualAmount }}</el-descriptions-item>
                <el-descriptions-item v-if="viewForm.actualDate" label="实际成交日期">{{ viewForm.actualDate }}</el-descriptions-item>
                <el-descriptions-item v-if="viewForm.lostReason" label="输单原因">{{ viewForm.lostReason }}</el-descriptions-item>
                <el-descriptions-item v-if="viewForm.lostRemark" label="输单说明">{{ viewForm.lostRemark }}</el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>
            <el-collapse-item title="竞争与方案" name="solution">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="竞争对手">{{ viewForm.competitor }}</el-descriptions-item>
                <el-descriptions-item label="客户痛点" :span="2">{{ viewForm.painPoint }}</el-descriptions-item>
                <el-descriptions-item label="解决方案" :span="2">{{ viewForm.solution }}</el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>
            <el-collapse-item title="负责与跟进" name="owner">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="负责人">{{ viewForm.userName }}</el-descriptions-item>
                <el-descriptions-item label="所属部门">{{ viewForm.deptName }}</el-descriptions-item>
                <el-descriptions-item label="下一步行动">{{ viewForm.nextAction }}</el-descriptions-item>
                <el-descriptions-item label="下次行动时间">{{ viewForm.nextTime }}</el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>
            <el-collapse-item title="其他信息" name="other">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="创建时间">{{ viewForm.createTime }}</el-descriptions-item>
                <el-descriptions-item label="备注" :span="2">{{ viewForm.remark }}</el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>
          </el-collapse>
        </el-tab-pane>
        <el-tab-pane :label="`跟进记录 (${interactionList.length})`" name="interactions">
          <el-timeline v-if="interactionList.length > 0">
            <el-timeline-item v-for="item in interactionList" :key="item.recordId" :timestamp="item.interactTime" placement="top">
              <el-card>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px">
                  <el-tag size="small" type="info">{{ getInteractTypeLabel(item.interactType) }}</el-tag>
                  <span style="color: #909399; font-size: 12px">{{ item.userName }}</span>
                </div>
                <p style="margin: 0">{{ item.content }}</p>
                <div v-if="item.nextTime" style="margin-top: 8px; font-size: 12px; color: #e6a23c">
                  下次跟进: {{ item.nextTime }} {{ item.nextContent ? '- ' + item.nextContent : '' }}
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无跟进记录" />
        </el-tab-pane>
        <el-tab-pane :label="`阶段历史 (${stageLogList.length})`" name="stageLog">
          <el-timeline v-if="stageLogList.length > 0">
            <el-timeline-item v-for="log in stageLogList" :key="log.logId" :timestamp="log.operateTime" placement="top" :type="getLogTagType(log.actionType)">
              <el-card>
                <div style="display: flex; justify-content: space-between; align-items: center">
                  <div>
                    <el-tag size="small" :type="getLogTagType(log.actionType)">{{ getActionTypeLabel(log.actionType) }}</el-tag>
                    <span style="margin-left: 8px">
                      <span v-if="log.fromStageName">{{ log.fromStageName }}</span>
                      <span v-if="log.toStageName"> → {{ log.toStageName }}</span>
                    </span>
                  </div>
                  <span style="color: #909399; font-size: 12px">{{ log.operatorName }}</span>
                </div>
                <p v-if="log.remark" style="margin: 8px 0 0; color: #606266; font-size: 13px">{{ log.remark }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无阶段变更记录" />
        </el-tab-pane>
        <el-tab-pane :label="`关联合同 (${contractList.length})`" name="contracts">
          <el-table :data="contractList" border size="small" v-if="contractList.length > 0">
            <el-table-column label="合同编号" prop="contractNo" width="150" />
            <el-table-column label="合同名称" prop="contractName" show-overflow-tooltip />
            <el-table-column label="合同金额" prop="contractAmount" width="120" align="right" />
            <el-table-column label="签约日期" prop="signDate" width="120" />
            <el-table-column label="状态" prop="contractStatus" width="80" align="center">
              <template #default="scope"><dict-tag :options="marketing_contract_status" :value="scope.row.contractStatus" /></template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无关联合同" />
        </el-tab-pane>
        <el-tab-pane :label="`关联订单 (${orderList.length})`" name="orders">
          <el-table :data="orderList" border size="small" v-if="orderList.length > 0">
            <el-table-column label="订单编号" prop="orderNo" width="150" />
            <el-table-column label="客户名称" prop="customerName" show-overflow-tooltip />
            <el-table-column label="订单金额" prop="orderAmount" width="120" align="right" />
            <el-table-column label="状态" prop="orderStatus" width="80" align="center">
              <template #default="scope"><dict-tag :options="marketing_order_status" :value="scope.row.orderStatus" /></template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无关联订单" />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 阶段推进/赢单/输单弹窗 -->
    <el-dialog :title="actionTitle" v-model="actionOpen" width="620px" append-to-body>
      <el-form :model="actionForm" label-width="120px">
        <el-form-item label="商机名称">
          <span>{{ actionForm.opportunityName }}</span>
        </el-form-item>
        <el-form-item label="当前阶段">
          <el-tag type="info">{{ actionForm.currentStageName }}</el-tag>
        </el-form-item>
        <el-form-item label="目标阶段" v-if="actionType === 'advance' || actionType === 'retreat'">
          <el-select v-model="actionForm.toStageCode" placeholder="请选择目标阶段" style="width: 100%">
            <el-option v-for="s in targetStageOptions" :key="s.stageCode" :label="s.stageName + ' (' + s.winRate + '%)'" :value="s.stageCode" />
          </el-select>
        </el-form-item>
        <template v-if="actionType === 'win'">
          <el-form-item label="实际成交金额" required>
            <el-input-number v-model="actionForm.actualAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="实际成交日期" required>
            <el-date-picker v-model="actionForm.actualDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </template>
        <template v-if="actionType === 'lose'">
          <el-form-item label="输单原因" required>
            <el-select v-model="actionForm.lostReason" placeholder="请选择输单原因" style="width: 100%">
              <el-option label="价格过高" value="价格过高" />
              <el-option label="竞品胜出" value="竞品胜出" />
              <el-option label="需求不匹配" value="需求不匹配" />
              <el-option label="决策人变化" value="决策人变化" />
              <el-option label="项目取消" value="项目取消" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="输单说明">
            <el-input v-model="actionForm.lostRemark" type="textarea" :rows="3" placeholder="请输入输单说明" />
          </el-form-item>
        </template>
        <el-form-item label="备注" v-if="actionType === 'advance' || actionType === 'retreat'">
          <el-input v-model="actionForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAction">确 定</el-button>
        <el-button @click="actionOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkOpportunity">
import { listOpportunity, getOpportunity, addOpportunity, updateOpportunity, delOpportunity, advanceOpportunity, retreatOpportunity, winOpportunity, loseOpportunity, reopenOpportunity, getOpportunityRelations } from '@/api/mk/opportunity'
import { listStage } from '@/api/mk/stage'
import { listCustomer } from '@/api/mk/customer'
import { listContact } from '@/api/mk/contact'
import { listUser, deptTreeSelect } from '@/api/system/user'
import { useColumnResize } from '@/composables/useColumnResize'
import { ArrowDown } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_opportunity_index')
const { marketing_opportunity_status, marketing_opportunity_source, marketing_contract_status, marketing_order_status, marketing_interaction_type } = proxy.useDict('marketing_opportunity_status', 'marketing_opportunity_source', 'marketing_contract_status', 'marketing_order_status', 'marketing_interaction_type')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const customerOptions = ref([])
const contactOptions = ref([])
const userOptions = ref([])
const deptOptions = ref([])
const stageOptions = ref([])
const viewForm = ref({})
const activeNames = ref(['basic', 'amount', 'solution', 'owner', 'other'])
const viewActiveNames = ref(['basic', 'amount', 'solution', 'owner', 'other'])
const detailTab = ref('basic')
const interactionList = ref([])
const stageLogList = ref([])
const contractList = ref([])
const orderList = ref([])
const actionOpen = ref(false)
const actionTitle = ref('')
const actionType = ref('')
const actionForm = ref({})
const targetStageOptions = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, opportunityNo: undefined, opportunityName: undefined, stageCode: undefined, opportunityStatus: undefined },
  rules: {
    opportunityName: [{ required: true, message: '商机名称不能为空', trigger: 'blur' }],
    customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
    expectedAmount: [{ required: true, message: '预计金额不能为空', trigger: 'blur' }],
    expectedDate: [{ required: true, message: '请选择预计成交日期', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listOpportunity(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function getCustomerOptions() { listCustomer({ pageNum: 1, pageSize: 9999 }).then(res => { customerOptions.value = res.rows }) }
function getUserOptions() { listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) }) }
function getDeptTree() { deptTreeSelect().then(res => { deptOptions.value = res.data }) }
function getStageOptions() { listStage({ pageNum: 1, pageSize: 9999, status: '0' }).then(res => { stageOptions.value = res.rows }) }
function onCustomerChange(customerId) {
  form.value.contactId = undefined
  if (customerId) { listContact({ customerId: customerId, pageNum: 1, pageSize: 9999 }).then(res => { contactOptions.value = res.rows }) }
  else { contactOptions.value = [] }
}
function onStageChange(stageCode) {
  const stage = stageOptions.value.find(s => s.stageCode === stageCode)
  if (stage) { form.value.stageName = stage.stageName; form.value.winRate = stage.winRate }
}
function onUserChange(userId) {
  if (userId) { const user = userOptions.value.find(u => u.userId === userId); if (user) { form.value.userName = user.nickName; if (user.deptId) { form.value.deptId = user.deptId; form.value.deptName = user.dept ? user.dept.deptName : undefined } } }
  else { form.value.userName = undefined }
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.opportunityId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = { opportunityNo: undefined, opportunityName: undefined, customerId: undefined, contactId: undefined, leadId: undefined, opportunitySource: undefined, expectedAmount: 0, expectedDate: undefined, stageCode: 'stage1', stageName: '初步接触', winRate: 10, weightedAmount: 0, opportunityStatus: '0', competitor: undefined, painPoint: undefined, solution: undefined, userId: undefined, userName: undefined, deptId: undefined, deptName: undefined, nextAction: undefined, nextTime: undefined, remark: undefined }
  contactOptions.value = []
  proxy.resetForm('opportunityRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增商机' }
function handleUpdate(row) {
  reset()
  getOpportunity(row.opportunityId || ids.value[0]).then(res => {
    form.value = res.data
    if (form.value.customerId) { listContact({ customerId: form.value.customerId, pageNum: 1, pageSize: 9999 }).then(r => { contactOptions.value = r.rows }) }
    open.value = true; title.value = '修改商机'
  })
}
function handleView(row) { getOpportunity(row.opportunityId).then(res => { viewForm.value = res.data; detailTab.value = 'basic'; viewOpen.value = true }) }
function loadRelations() {
  if (!viewForm.value.opportunityId) return
  getOpportunityRelations(viewForm.value.opportunityId).then(res => {
    const data = res.data || {}
    interactionList.value = data.interactions || []
    stageLogList.value = data.stageLogs || []
    contractList.value = data.contracts || []
    orderList.value = data.orders || []
  })
}
function submitForm() {
  proxy.$refs['opportunityRef'].validate(valid => {
    if (valid) {
      if (form.value.opportunityId != undefined) { updateOpportunity(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addOpportunity(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const opportunityIds = row.opportunityId || ids.value; proxy.$modal.confirm('确认删除编号为"' + opportunityIds + '"的数据？').then(() => delOpportunity(opportunityIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('mk/opportunity/export', { ...queryParams.value }, `opportunity_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// ===== 阶段推进/赢单/输单 =====
function handleAction(cmd, row) {
  actionType.value = cmd
  actionForm.value = { opportunityId: row.opportunityId, opportunityName: row.opportunityName, currentStageCode: row.stageCode, currentStageName: row.stageName, toStageCode: undefined, actualAmount: undefined, actualDate: undefined, lostReason: undefined, lostRemark: undefined, remark: undefined }
  if (cmd === 'advance') {
    actionTitle.value = '推进阶段'
    // 只能推进到排序更大的阶段
    const currentSort = stageOptions.value.find(s => s.stageCode === row.stageCode)?.sort || 0
    targetStageOptions.value = stageOptions.value.filter(s => s.sort > currentSort)
    if (targetStageOptions.value.length === 0) { proxy.$modal.msgWarning('已是最后阶段，无法继续推进'); return }
    actionOpen.value = true
  } else if (cmd === 'retreat') {
    actionTitle.value = '退回阶段'
    // 只能退回到排序更小的阶段
    const currentSort = stageOptions.value.find(s => s.stageCode === row.stageCode)?.sort || 0
    targetStageOptions.value = stageOptions.value.filter(s => s.sort < currentSort)
    if (targetStageOptions.value.length === 0) { proxy.$modal.msgWarning('已是第一阶段，无法继续退回'); return }
    actionOpen.value = true
  } else if (cmd === 'win') {
    actionTitle.value = '赢单关单'
    actionForm.value.actualAmount = row.expectedAmount
    actionForm.value.actualDate = new Date().toISOString().slice(0, 10)
    actionOpen.value = true
  } else if (cmd === 'lose') {
    actionTitle.value = '输单关单'
    actionOpen.value = true
  }
}
function submitAction() {
  const id = actionForm.value.opportunityId
  if (actionType.value === 'advance') {
    if (!actionForm.value.toStageCode) { proxy.$modal.msgError('请选择目标阶段'); return }
    advanceOpportunity(id, { stageCode: actionForm.value.toStageCode, remark: actionForm.value.remark }).then(() => { proxy.$modal.msgSuccess('阶段推进成功'); actionOpen.value = false; getList() })
  } else if (actionType.value === 'retreat') {
    if (!actionForm.value.toStageCode) { proxy.$modal.msgError('请选择目标阶段'); return }
    retreatOpportunity(id, { stageCode: actionForm.value.toStageCode, remark: actionForm.value.remark }).then(() => { proxy.$modal.msgSuccess('阶段退回成功'); actionOpen.value = false; getList() })
  } else if (actionType.value === 'win') {
    if (!actionForm.value.actualAmount) { proxy.$modal.msgError('请填写实际成交金额'); return }
    if (!actionForm.value.actualDate) { proxy.$modal.msgError('请选择实际成交日期'); return }
    winOpportunity(id, { actualAmount: actionForm.value.actualAmount, actualDate: actionForm.value.actualDate, remark: actionForm.value.remark }).then(() => { proxy.$modal.msgSuccess('赢单关单成功'); actionOpen.value = false; getList() })
  } else if (actionType.value === 'lose') {
    if (!actionForm.value.lostReason) { proxy.$modal.msgError('请选择输单原因'); return }
    loseOpportunity(id, { lostReason: actionForm.value.lostReason, lostRemark: actionForm.value.lostRemark }).then(() => { proxy.$modal.msgSuccess('输单关单成功'); actionOpen.value = false; getList() })
  }
}
function handleReopen(row) {
  proxy.$modal.confirm('确认重新打开商机"' + row.opportunityName + '"？').then(() => reopenOpportunity(row.opportunityId)).then(() => { proxy.$modal.msgSuccess('重新打开成功'); getList() }).catch(() => {})
}

function getInteractTypeLabel(type) {
  const item = marketing_interaction_type.value?.find(d => d.value === type)
  return item ? item.label : type
}
function getActionTypeLabel(type) {
  const map = { advance: '推进', retreat: '退回', win: '赢单', lose: '输单', reopen: '重开' }
  return map[type] || type
}
function getLogTagType(type) {
  const map = { advance: 'success', retreat: 'warning', win: 'success', lose: 'danger', reopen: 'info' }
  return map[type] || 'primary'
}

getCustomerOptions()
getUserOptions()
getDeptTree()
getStageOptions()
getList()
</script>

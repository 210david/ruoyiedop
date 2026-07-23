<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="工单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="设备名称" prop="equipmentName">
        <el-input v-model="queryParams.equipmentName" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="工单类型" prop="orderType">
        <el-select v-model="queryParams.orderType" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="d in dms_order_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="工单状态" prop="orderStatus">
        <el-select v-model="queryParams.orderStatus" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="d in dms_order_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-select v-model="queryParams.priority" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="d in dms_priority" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:workorder:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:workorder:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:workorder:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['dms:workorder:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="工单号" prop="orderNo" :width="colWidth('orderNo', 160)" resizable />
      <el-table-column label="设备名称" prop="equipmentName" :width="colWidth('equipmentName', 160)" resizable show-overflow-tooltip />
      <el-table-column label="工单类型" prop="orderType" :width="colWidth('orderType', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="dms_order_type" :value="scope.row.orderType" /></template>
      </el-table-column>
      <el-table-column label="故障描述" prop="faultDescription" :width="colWidth('faultDescription', 220)" resizable show-overflow-tooltip />
      <el-table-column label="报修人" prop="reporterName" :width="colWidth('reporterName', 80)" resizable />
      <el-table-column label="优先级" prop="priority" :width="colWidth('priority', 80)" resizable align="center">
        <template #default="scope"><dict-tag :options="dms_priority" :value="scope.row.priority" /></template>
      </el-table-column>
      <el-table-column label="维修人" prop="assigneeName" :width="colWidth('assigneeName', 80)" resizable />
      <el-table-column label="工单状态" prop="orderStatus" :width="colWidth('orderStatus', 110)" resizable align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.slaTimeoutStatus === '1'" type="danger" size="small">响应超时</el-tag>
          <el-tag v-else-if="scope.row.slaTimeoutStatus === '2'" type="danger" size="small">处理超时</el-tag>
          <dict-tag :options="dms_order_status" :value="scope.row.orderStatus" />
        </template>
      </el-table-column>
      <el-table-column label="报修时间" prop="reportTime" :width="colWidth('reportTime', 160)" resizable align="center" />
      <el-table-column label="操作" width="320" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" icon="List" @click="handleLog(scope.row)">历史</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:workorder:edit']">修改</el-button>
          <!-- 状态流转按钮 -->
          <el-button v-if="scope.row.orderStatus === '0'" link type="primary" @click="handleDispatch(scope.row)">派工</el-button>
          <el-button v-if="scope.row.orderStatus === '1'" link type="success" @click="handleAccept(scope.row)">接单</el-button>
          <el-button v-if="scope.row.orderStatus === '1'" link type="warning" @click="handleReassign(scope.row)">改派</el-button>
          <el-button v-if="scope.row.orderStatus === '2' || scope.row.orderStatus === '7'" link type="primary" @click="handleProcess(scope.row)">开始处理</el-button>
          <el-button v-if="scope.row.orderStatus === '3'" link type="success" @click="handleComplete(scope.row)">完工</el-button>
          <el-button v-if="scope.row.orderStatus === '4'" link type="success" @click="handleVerify(scope.row)">验收</el-button>
          <el-button v-if="scope.row.orderStatus === '4'" link type="danger" @click="handleReject(scope.row)">驳回</el-button>
          <el-button v-if="scope.row.orderStatus === '0' || scope.row.orderStatus === '1'" link type="info" @click="handleCancel(scope.row)">撤销</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:workorder:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改工单弹窗 -->
    <el-dialog :title="title" v-model="open" width="780px" append-to-body>
      <el-form ref="workorderRef" :model="form" :rules="rules" label-width="100px">
        <!-- AI智能报修 -->
        <el-alert v-if="aiResult" type="success" :closable="true" @close="aiResult = null" style="margin-bottom: 12px">
          <template #title>AI识别结果</template>
          <div style="font-size: 13px; line-height: 1.8">
            故障描述：{{ aiResult.faultDescription }}<br/>
            优先级：{{ aiResult.priority }}<br/>
            发生时间：{{ aiResult.reportTime || '未识别' }}<br/>
            <span style="color: #999">{{ aiResult.tip }}</span>
          </div>
        </el-alert>
        <el-form-item v-if="form.orderId == undefined">
          <el-button type="primary" plain icon="MagicStick" @click="aiDialog = true" :loading="aiLoading">智能报修</el-button>
          <span style="color: #999; margin-left: 10px">输入自然语言描述，AI自动识别填充</span>
        </el-form-item>

        <el-collapse v-model="activeSectionNames" class="workorder-collapse">
        <!-- 分组一：工单信息 -->
        <el-collapse-item title="工单信息" name="order">
        <el-row>
          <el-col :span="12"><el-form-item label="工单号" prop="orderNo"><el-input v-model="form.orderNo" placeholder="自动生成" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="工单类型" prop="orderType">
            <el-select v-model="form.orderType" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dms_order_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="优先级" prop="priority">
            <el-select v-model="form.priority" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dms_priority" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12" v-if="!isAdd"><el-form-item label="工单状态" prop="orderStatus">
            <el-select v-model="form.orderStatus" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dms_order_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="报修人" prop="reporterId"><el-select v-model="form.reporterId" filterable clearable placeholder="请选择报修人" style="width: 100%" @change="(uid) => onUserSelect(form, 'reporterId', 'reporterName', uid)"><el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="报修时间" prop="reportTime"><el-date-picker v-model="form.reportTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        </el-collapse-item>

        <!-- 分组二：设备信息 -->
        <el-collapse-item title="设备信息" name="equipment">
        <el-row>
          <el-col :span="12"><el-form-item label="设备" prop="equipmentId"><el-select v-model="form.equipmentId" filterable clearable placeholder="请选择设备" style="width: 100%" @change="onEquipmentChange"><el-option v-for="e in equipmentOptions" :key="e.equipmentId" :label="e.equipmentCode + ' - ' + e.equipmentName" :value="e.equipmentId" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备名称"><el-input v-model="form.equipmentName" placeholder="选择设备后自动带出" disabled /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="故障描述" prop="faultDescription"><el-input v-model="form.faultDescription" type="textarea" :rows="3" placeholder="请输入故障现象/任务描述" /></el-form-item></el-col>
        </el-row>
        </el-collapse-item>

        <!-- 分组三：派工信息（仅修改时显示） -->
        <el-collapse-item v-if="!isAdd" title="派工信息" name="dispatch">
        <el-row>
          <el-col :span="12"><el-form-item label="维修人" prop="assigneeId"><el-select v-model="form.assigneeId" filterable clearable placeholder="请选择维修人" style="width: 100%" @change="(uid) => onUserSelect(form, 'assigneeId', 'assigneeName', uid)"><el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="派工人" prop="assignerId"><el-select v-model="form.assignerId" filterable clearable placeholder="请选择派工人" style="width: 100%" @change="(uid) => onUserSelect(form, 'assignerId', 'assignerName', uid)"><el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="到达时间" prop="arriveTime"><el-date-picker v-model="form.arriveTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        </el-collapse-item>

        <!-- 分组四：维修处理（仅修改时显示） -->
        <el-collapse-item v-if="!isAdd" title="维修处理" name="repair">
        <el-row>
          <el-col :span="24"><el-form-item label="故障原因" prop="faultCause"><el-input v-model="form.faultCause" type="textarea" :rows="2" placeholder="请输入故障原因" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="维修措施" prop="repairMeasure"><el-input v-model="form.repairMeasure" type="textarea" :rows="2" placeholder="请输入维修措施" /></el-form-item></el-col>
          <el-col :span="24">
        <el-form-item label="更换备件" prop="sparePartsUsed">
          <div style="width: 100%">
            <el-table :data="formSparePartList" border size="small" style="margin-bottom: 8px" @header-dragend="onHeaderDragEnd">
              <el-table-column label="备件名称" min-width="180">
                <template #default="{ row }">
                  <el-select v-model="row.partName" filterable clearable placeholder="请选择备件" style="width: 100%">
                    <el-option v-for="p in getAvailableSpareParts(formSparePartList, row.partName)" :key="p.partId" :label="p.partName + (p.specModel ? ' (' + p.specModel + ')' : '')" :value="p.partName" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="库存" width="80" align="center">
                <template #default="{ row }">{{ getSparePartStock(row.partName) != null ? getSparePartStock(row.partName) : '-' }}</template>
              </el-table-column>
              <el-table-column label="数量" width="120" align="center">
                <template #default="{ row }">
                  <el-input-number v-model="row.quantity" :min="1" :max="getSparePartStock(row.partName) != null ? getSparePartStock(row.partName) : undefined" :precision="0" size="small" controls-position="right" style="width: 100px" />
                </template>
              </el-table-column>
              <el-table-column label="单位" width="70" align="center">
                <template #default="{ row }">{{ getSparePartUnit(row.partName) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" icon="Delete" @click="formSparePartList.splice($index, 1)"></el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" plain icon="Plus" size="small" @click="formSparePartList.push({ partName: undefined, quantity: 1 })">添加备件</el-button>
          </div>
        </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="停机时长" prop="downtimeDuration"><el-input-number v-model="form.downtimeDuration" :min="0" :precision="2" controls-position="right" style="width: 100%" /><span style="margin-left: 8px; color: #999">小时</span></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="完工时间" prop="completeTime"><el-date-picker v-model="form.completeTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="完工说明" prop="completeRemark"><el-input v-model="form.completeRemark" type="textarea" :rows="2" placeholder="请输入完工说明" /></el-form-item></el-col>
        </el-row>
        </el-collapse-item>

        <!-- 分组五：验收信息（仅修改时显示） -->
        <el-collapse-item v-if="!isAdd" title="验收信息" name="verify">
        <el-row>
          <el-col :span="12"><el-form-item label="验收人" prop="verifierId"><el-select v-model="form.verifierId" filterable clearable placeholder="请选择验收人" style="width: 100%" @change="(uid) => onUserSelect(form, 'verifierId', 'verifierName', uid)"><el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="评价" prop="rating">
            <el-rate v-model="form.rating" :max="5" />
          </el-form-item></el-col>
          <el-col :span="24"><el-form-item label="验收意见" prop="verifyOpinion"><el-input v-model="form.verifyOpinion" type="textarea" :rows="2" placeholder="请输入验收意见" /></el-form-item></el-col>
        </el-row>
        </el-collapse-item>

        <!-- 分组六：其他信息 -->
        <el-collapse-item title="其他信息" name="other">
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入内容" /></el-form-item>
        </el-collapse-item>
        </el-collapse>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 派工/改派弹窗 -->
    <el-dialog :title="isReassign ? '工单改派' : '工单派工'" v-model="dispatchOpen" width="560px" append-to-body>
      <el-form ref="dispatchRef" :model="dispatchForm" label-width="100px">
        <el-form-item label="工单号"><el-input :model-value="dispatchForm.orderNo" disabled /></el-form-item>
        <el-form-item label="设备名称"><el-input :model-value="dispatchForm.equipmentName" disabled /></el-form-item>
        <el-form-item>
          <el-button type="primary" plain icon="MagicStick" @click="loadRecommend" :loading="recommendLoading">智能推荐维修人</el-button>
        </el-form-item>
        <div v-if="recommendations.length > 0" style="margin-bottom: 12px">
        <div v-for="(rec, i) in recommendations" :key="i" style="padding: 8px 0; border-bottom: 1px solid #eee; cursor: pointer" @click="selectRecommendation(rec)">
          <el-tag type="success" size="small">推荐{{ i + 1 }}</el-tag>
          <span style="margin-left: 8px">{{ rec.assigneeName }}</span>
          <span style="color: #999; margin-left: 8px">待处理：{{ rec.currentWorkload }}单</span>
          <span v-if="rec.score" style="color: #409eff; margin-left: 8px">评分：{{ rec.score }}</span>
          <div v-if="rec.reason" style="color: #67c23a; font-size: 12px; margin-top: 2px">{{ rec.reason }}</div>
        </div>
        </div>
        <el-form-item label="维修人" required>
          <el-select v-model="dispatchForm.assigneeId" filterable clearable placeholder="请选择维修人" style="width: 100%" @change="(uid) => onUserSelect(dispatchForm, 'assigneeId', 'assigneeName', uid)">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="派工人">
          <el-select v-model="dispatchForm.assignerId" filterable clearable placeholder="请选择派工人" style="width: 100%" @change="(uid) => onUserSelect(dispatchForm, 'assignerId', 'assignerName', uid)">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitDispatch">{{ isReassign ? '确认改派' : '确认派工' }}</el-button>
        <el-button @click="dispatchOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- AI智能报修弹窗 -->
    <el-dialog title="智能报修助手" v-model="aiDialog" width="500px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="描述">
          <el-input v-model="aiInput" type="textarea" :rows="4" placeholder="请用自然语言描述故障，如：3号车床主轴有异响，震动比较大，今天上午开始的，比较急" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="callAI" :loading="aiLoading">智能识别</el-button>
        <el-button @click="aiDialog = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 完工弹窗 -->
    <el-dialog title="工单完工" v-model="completeOpen" width="780px" append-to-body>
      <el-form ref="completeRef" :model="completeForm" label-width="100px">
        <el-form-item label="工单号"><el-input :model-value="completeForm.orderNo" disabled /></el-form-item>
        <!-- 任务清单 -->
        <el-form-item v-if="completeTaskList.length > 0" label="任务清单">
          <div style="width: 100%">
            <el-alert type="info" :closable="false" show-icon style="margin-bottom: 8px">
              <template #title>请逐项确认任务完成情况</template>
            </el-alert>
            <div v-for="(task, index) in completeTaskList" :key="index" style="display: flex; align-items: center; margin-bottom: 8px; padding: 6px 12px; border: 1px solid #ebeef5; border-radius: 4px">
              <el-checkbox v-model="task.done" style="margin-right: 8px" />
              <span :style="task.done ? 'text-decoration: line-through; color: #999' : ''">{{ task.text }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="故障原因" required>
          <el-input v-model="completeForm.faultCause" type="textarea" :rows="2" placeholder="请输入故障原因" />
        </el-form-item>
        <el-form-item label="维修措施" required>
          <el-input v-model="completeForm.repairMeasure" type="textarea" :rows="2" placeholder="请输入维修措施" />
        </el-form-item>
        <el-form-item label="更换备件">
          <div style="width: 100%">
            <el-table :data="completeSparePartList" border size="small" style="margin-bottom: 8px" @header-dragend="onHeaderDragEnd">
              <el-table-column label="备件名称" min-width="180">
                <template #default="{ row }">
                  <el-select v-model="row.partName" filterable clearable placeholder="请选择备件" style="width: 100%">
                    <el-option v-for="p in getAvailableSpareParts(completeSparePartList, row.partName)" :key="p.partId" :label="p.partName + (p.specModel ? ' (' + p.specModel + ')' : '')" :value="p.partName" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="库存" width="80" align="center">
                <template #default="{ row }">{{ getSparePartStock(row.partName) != null ? getSparePartStock(row.partName) : '-' }}</template>
              </el-table-column>
              <el-table-column label="数量" width="120" align="center">
                <template #default="{ row }">
                  <el-input-number v-model="row.quantity" :min="1" :max="getSparePartStock(row.partName) != null ? getSparePartStock(row.partName) : undefined" :precision="0" size="small" controls-position="right" style="width: 100px" />
                </template>
              </el-table-column>
              <el-table-column label="单位" width="70" align="center">
                <template #default="{ row }">{{ getSparePartUnit(row.partName) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" icon="Delete" @click="completeSparePartList.splice($index, 1)"></el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" plain icon="Plus" size="small" @click="completeSparePartList.push({ partName: undefined, quantity: 1 })">添加备件</el-button>
          </div>
        </el-form-item>
        <el-form-item label="停机时长">
          <el-input-number v-model="completeForm.downtimeDuration" :min="0" :precision="2" controls-position="right" /> <span style="margin-left: 8px; color: #999">小时</span>
        </el-form-item>
        <el-form-item label="完工说明">
          <el-input v-model="completeForm.completeRemark" type="textarea" :rows="2" placeholder="请输入完工说明" />
        </el-form-item>
        <!-- 偏差原因（PM工单） -->
        <el-form-item v-if="completeForm.orderType === '1'" label="偏差原因">
          <el-input v-model="completeForm.deviationReason" type="textarea" :rows="2" placeholder="如未按计划时间完成，请说明原因（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitComplete">确认完工</el-button>
        <el-button @click="completeOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 验收弹窗 -->
    <el-dialog title="工单验收" v-model="verifyOpen" width="560px" append-to-body>
      <el-form ref="verifyRef" :model="verifyForm" label-width="100px">
        <el-form-item label="工单号"><el-input :model-value="verifyForm.orderNo" disabled /></el-form-item>
        <el-alert v-if="verifyForm.sparePartsUsed" type="warning" :closable="false" show-icon style="margin-bottom: 16px">
          <template #title>该工单使用了备件，验收通过后将自动创建出库单并扣减库存</template>
          <div style="margin-top: 4px">更换备件：{{ verifyForm.sparePartsUsed }}</div>
        </el-alert>
        <el-form-item label="验收人" required>
          <el-select v-model="verifyForm.verifierId" filterable clearable placeholder="请选择验收人" style="width: 100%" @change="(uid) => onUserSelect(verifyForm, 'verifierId', 'verifierName', uid)">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="评价">
          <el-rate v-model="verifyForm.rating" :max="5" />
        </el-form-item>
        <el-form-item label="验收意见">
          <el-input v-model="verifyForm.verifyOpinion" type="textarea" :rows="3" placeholder="请输入验收意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitVerify">确认验收</el-button>
        <el-button @click="verifyOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewOpen" width="1120px" append-to-body class="workorder-detail-dialog" top="5vh">
      <template #header>
        <div class="detail-dialog-header">
          <div class="header-left">
            <el-icon class="header-icon"><Tickets /></el-icon>
            <span class="header-title">工单详情</span>
            <el-tag v-if="viewForm.orderNo" size="small" effect="plain" class="header-order-no">{{ viewForm.orderNo }}</el-tag>
          </div>
          <div class="header-right">
            <el-tag v-if="viewForm.slaTimeoutStatus === '1'" type="danger" effect="dark" size="small">响应超时</el-tag>
            <el-tag v-if="viewForm.slaTimeoutStatus === '2'" type="danger" effect="dark" size="small">处理超时</el-tag>
            <el-tag v-if="viewForm.orderStatus != null" :type="orderStatusTagType(viewForm.orderStatus)" effect="dark">{{ statusText(viewForm.orderStatus) }}</el-tag>
          </div>
        </div>
      </template>

      <div class="detail-content">
        <!-- 状态流程条 -->
        <div class="status-flow">
          <div v-for="(step, i) in statusSteps" :key="i" class="status-step" :class="{ active: isStepActive(i), done: isStepDone(i) }">
            <div class="step-icon">
              <el-icon v-if="isStepDone(i)"><Check /></el-icon>
              <span v-else>{{ i + 1 }}</span>
            </div>
            <span class="step-label">{{ step.label }}</span>
            <el-icon v-if="i < statusSteps.length - 1" class="step-arrow"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 工单信息 -->
        <div class="detail-section section-blue">
          <div class="section-header">
            <el-icon><Document /></el-icon>
            <span>工单信息</span>
          </div>
          <div class="section-body">
            <el-descriptions :column="3" border :label-style="descLabelStyle" :content-style="descContentStyle">
              <el-descriptions-item label="工单号">{{ viewForm.orderNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="工单类型"><dict-tag :options="dms_order_type" :value="viewForm.orderType" /></el-descriptions-item>
              <el-descriptions-item label="优先级"><dict-tag :options="dms_priority" :value="viewForm.priority" /></el-descriptions-item>
              <el-descriptions-item label="报修人">{{ viewForm.reporterName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="报修时间">{{ viewForm.reportTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="工单状态"><dict-tag :options="dms_order_status" :value="viewForm.orderStatus" /></el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <!-- 设备信息 -->
        <div class="detail-section section-cyan">
          <div class="section-header">
            <el-icon><Monitor /></el-icon>
            <span>设备信息</span>
          </div>
          <div class="section-body">
            <el-descriptions :column="3" border :label-style="descLabelStyle" :content-style="descContentStyle">
              <el-descriptions-item label="设备编号">{{ viewForm.equipmentCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="设备名称" :span="2">{{ viewForm.equipmentName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="故障描述" :span="3">{{ viewForm.faultDescription || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <!-- 派工信息 -->
        <div class="detail-section section-purple">
          <div class="section-header">
            <el-icon><UserFilled /></el-icon>
            <span>派工信息</span>
          </div>
          <div class="section-body">
            <el-descriptions :column="3" border :label-style="descLabelStyle" :content-style="descContentStyle">
              <el-descriptions-item label="维修人">{{ viewForm.assigneeName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="派工人">{{ viewForm.assignerName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="派工时间">{{ viewForm.assignTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="到达时间">{{ viewForm.arriveTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="SLA响应截止">
                <span :class="{ 'sla-warning': viewForm.slaTimeoutStatus === '1' }">{{ viewForm.slaResponseDeadline || '-' }}</span>
                <el-tag v-if="viewForm.slaTimeoutStatus === '1'" type="danger" size="small" style="margin-left: 6px">已超时</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="SLA处理截止">
                <span :class="{ 'sla-warning': viewForm.slaTimeoutStatus === '2' }">{{ viewForm.slaProcessDeadline || '-' }}</span>
                <el-tag v-if="viewForm.slaTimeoutStatus === '2'" type="danger" size="small" style="margin-left: 6px">已超时</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <!-- 维修处理 -->
        <div class="detail-section section-orange">
          <div class="section-header">
            <el-icon><Tools /></el-icon>
            <span>维修处理</span>
          </div>
          <div class="section-body">
            <el-descriptions :column="3" border :label-style="descLabelStyle" :content-style="descContentStyle">
              <el-descriptions-item label="故障原因" :span="3">{{ viewForm.faultCause || '-' }}</el-descriptions-item>
              <el-descriptions-item label="维修措施" :span="3">{{ viewForm.repairMeasure || '-' }}</el-descriptions-item>
              <el-descriptions-item label="更换备件">{{ viewForm.sparePartsUsed || '-' }}</el-descriptions-item>
              <el-descriptions-item label="停机时长">{{ viewForm.downtimeDuration != null ? viewForm.downtimeDuration + ' 小时' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="完工时间">{{ viewForm.completeTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="完工说明" :span="3">{{ viewForm.completeRemark || '-' }}</el-descriptions-item>
              <el-descriptions-item v-if="viewForm.deviationReason" label="偏差原因" :span="3">{{ viewForm.deviationReason }}</el-descriptions-item>
            </el-descriptions>
            <!-- 任务清单完成情况 -->
            <div v-if="viewForm.taskChecklist" class="task-checklist">
              <div class="task-checklist-title">任务清单完成情况</div>
              <div class="task-checklist-body">
                <div v-for="(task, index) in parseTaskChecklist(viewForm.taskChecklist)" :key="index" class="task-item" :class="{ 'task-done': task.done }">
                  <el-icon v-if="task.done" class="task-icon-done"><CircleCheckFilled /></el-icon>
                  <el-icon v-else class="task-icon-pending"><RemoveFilled /></el-icon>
                  <span :class="{ 'task-text-done': task.done }">{{ task.text }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 验收信息 -->
        <div class="detail-section section-green">
          <div class="section-header">
            <el-icon><CircleCheck /></el-icon>
            <span>验收信息</span>
          </div>
          <div class="section-body">
            <el-descriptions :column="3" border :label-style="descLabelStyle" :content-style="descContentStyle">
              <el-descriptions-item label="验收人">{{ viewForm.verifierName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="验收时间">{{ viewForm.verifyTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="评价">
                <el-rate v-if="viewForm.rating != null" v-model="viewForm.rating" :max="5" disabled show-score :texts="['极差', '失望', '一般', '满意', '非常满意']" />
                <span v-else>-</span>
              </el-descriptions-item>
              <el-descriptions-item label="验收意见" :span="3">{{ viewForm.verifyOpinion || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <!-- 系统信息 -->
        <div class="detail-section section-gray">
          <div class="section-header">
            <el-icon><InfoFilled /></el-icon>
            <span>系统信息</span>
          </div>
          <div class="section-body">
            <el-descriptions :column="3" border :label-style="descLabelStyle" :content-style="descContentStyle">
              <el-descriptions-item label="创建人">{{ viewForm.createBy || '-' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ viewForm.createTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="更新人">{{ viewForm.updateBy || '-' }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ viewForm.updateTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">{{ viewForm.remark || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 驳回弹窗 -->
    <el-dialog title="工单驳回" v-model="rejectOpen" width="500px" append-to-body>
      <el-form ref="rejectRef" :model="rejectForm" label-width="100px">
        <el-form-item label="工单号"><el-input :model-value="rejectForm.orderNo" disabled /></el-form-item>
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectForm.verifyOpinion" type="textarea" :rows="3" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="submitReject">确认驳回</el-button>
        <el-button @click="rejectOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 操作历史弹窗 -->
    <el-dialog title="工单操作历史" v-model="logOpen" width="700px" append-to-body>
      <el-timeline>
        <el-timeline-item v-for="(item, i) in logList" :key="i" :timestamp="item.createTime" placement="top"
          :type="item.action === 'create' ? 'primary' : (item.action === 'complete' || item.action === 'verify') ? 'success' : (item.action === 'reject' || item.action === 'cancel') ? 'danger' : 'info'">
          <el-card shadow="never">
            <h4>{{ actionText(item.action) }}：{{ statusText(item.fromStatus) }} → {{ statusText(item.toStatus) }}</h4>
            <p style="color: #999; font-size: 13px">操作人：{{ item.operatorName || '-' }}</p>
            <p v-if="item.remark" style="font-size: 13px">{{ item.remark }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="logList.length === 0" description="暂无操作记录" />
      <template #footer>
        <el-button @click="logOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsWorkOrder">
import { listWorkorder, getWorkorder, addWorkorder, updateWorkorder, delWorkorder,
  dispatchWorkorder, reassignWorkorder, acceptWorkorder, processWorkorder,
  completeWorkorder, verifyWorkorder, rejectWorkorder, cancelWorkorder, getWorkorderLog } from '@/api/dms/workorder'
import { repairAssistant, dispatchRecommend } from '@/api/dms/ai'
import { listUser } from '@/api/system/user'
import { listEquipment } from '@/api/dms/equipment'
import { listSparepart } from '@/api/dms/sparepart'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_workorder_index')
const { dms_order_type, dms_order_status, dms_priority, wms_unit } = proxy.useDict('dms_order_type', 'dms_order_status', 'dms_priority', 'wms_unit')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const dispatchOpen = ref(false)
const completeOpen = ref(false)
const verifyOpen = ref(false)
const rejectOpen = ref(false)
const isReassign = ref(false)
const viewOpen = ref(false)
const viewForm = ref({})
const viewActiveNames = ref(['order', 'equipment', 'dispatch', 'repair', 'verify', 'system'])
const logOpen = ref(false)
const logList = ref([])
const aiDialog = ref(false)
const aiInput = ref('')
const aiLoading = ref(false)
const aiResult = ref(null)
const recommendLoading = ref(false)
const recommendations = ref([])
const userOptions = ref([])
const equipmentOptions = ref([])
const sparePartOptions = ref([])
const completeSparePartList = ref([])
const completeTaskList = ref([])
const formSparePartList = ref([])
const activeSectionNames = ref(['order', 'equipment', 'other'])
const isAdd = computed(() => form.value.orderId == undefined)

const data = reactive({
  form: {},
  dispatchForm: {},
  completeForm: {},
  verifyForm: {},
  rejectForm: {},
  queryParams: { pageNum: 1, pageSize: 10, orderNo: undefined, equipmentName: undefined, orderType: undefined, orderStatus: undefined, priority: undefined },
  rules: {
    equipmentId: [{ required: true, message: '设备不能为空', trigger: 'change' }],
    orderType: [{ required: true, message: '工单类型不能为空', trigger: 'change' }],
    faultDescription: [{ required: true, message: '故障描述不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)
const { dispatchForm, completeForm, verifyForm, rejectForm } = toRefs(data)

function getList() {
  loading.value = true
  listWorkorder(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.orderId); single.value = selection.length !== 1; multiple.value = !selection.length }
function getUserList() {
  listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) })
}
function getEquipmentList() {
  listEquipment({ pageNum: 1, pageSize: 9999 }).then(res => { equipmentOptions.value = res.rows })
}
function getSparePartList() {
  listSparepart({ pageNum: 1, pageSize: 9999, status: '0' }).then(res => { sparePartOptions.value = res.rows })
}
/** 根据备件名称获取单位（转换为字典标签） */
function getSparePartUnit(partName) {
  if (!partName) return ''
  const part = sparePartOptions.value.find(p => p.partName === partName)
  if (!part) return ''
  const dictItem = wms_unit.value.find(d => d.value === part.unit)
  return dictItem ? dictItem.label : part.unit
}
/** 根据备件名称获取库存数量 */
function getSparePartStock(partName) {
  if (!partName) return null
  const part = sparePartOptions.value.find(p => p.partName === partName)
  return part ? part.currentStock : null
}
/** 校验备件列表中数量不超库存 */
function validateSparePartStock(list) {
  for (const item of list) {
    if (!item.partName) continue
    const stock = getSparePartStock(item.partName)
    if (stock != null && item.quantity > stock) {
      proxy.$modal.msgError(`备件「${item.partName}」使用数量 ${item.quantity} 超过库存数量 ${stock}`)
      return false
    }
  }
  return true
}
/** 获取可选备件列表（排除当前表格中其他行已选的备件） */
function getAvailableSpareParts(list, currentName) {
  const selectedNames = list.filter(i => i.partName && i.partName !== currentName).map(i => i.partName)
  return sparePartOptions.value.filter(p => !selectedNames.includes(p.partName) && p.currentStock != null && p.currentStock > 0)
}
/** 备件列表序列化为字符串：轴承×2，密封圈×5 */
function sparePartsToString(list) {
  const valid = list.filter(i => i.partName)
  return valid.length > 0 ? valid.map(i => i.partName + '×' + (i.quantity || 1)).join('，') : ''
}
/** 字符串解析为备件列表 */
function parseSpareParts(str) {
  if (!str) return []
  return str.split('，').filter(s => s.trim()).map(s => {
    const idx = s.indexOf('×')
    if (idx > 0) {
      return { partName: s.substring(0, idx).trim(), quantity: parseInt(s.substring(idx + 1)) || 1 }
    }
    return { partName: s.trim(), quantity: 1 }
  })
}
/** 解析任务清单JSON */
function parseTaskChecklist(str) {
  if (!str) return []
  try {
    const arr = JSON.parse(str)
    if (Array.isArray(arr)) {
      return arr.map(item => {
        if (typeof item === 'string') return { text: item, done: false }
        return { text: item.text || item.task || item.name || '', done: !!item.done }
      })
    }
  } catch (e) {
    return str.split('\n').filter(s => s.trim()).map(s => ({ text: s.trim(), done: false }))
  }
  return []
}
/** 选择设备后自动带出设备编号和名称 */
function onEquipmentChange(equipmentId) {
  if (equipmentId) {
    const eq = equipmentOptions.value.find(e => e.equipmentId === equipmentId)
    if (eq) {
      form.value.equipmentId = eq.equipmentId
      form.value.equipmentCode = eq.equipmentCode
      form.value.equipmentName = eq.equipmentName
    }
  } else {
    form.value.equipmentId = undefined
    form.value.equipmentCode = undefined
    form.value.equipmentName = undefined
  }
}
/** 通用：选择人员后同步ID和姓名（模板中传入的form已是解包后的响应式对象） */
function onUserSelect(formObj, idField, nameField, userId) {
  if (userId) {
    const user = userOptions.value.find(u => u.userId === userId)
    if (user) {
      formObj[idField] = userId
      formObj[nameField] = user.nickName
    }
  } else {
    formObj[idField] = undefined
    formObj[nameField] = undefined
  }
}
function reset() {
  form.value = { orderType: '0', priority: '2', orderStatus: '0', faultDescription: undefined, equipmentId: undefined, equipmentCode: undefined, equipmentName: undefined,
    reporterId: undefined, reporterName: undefined, reportTime: undefined, assigneeId: undefined, assigneeName: undefined,
    assignerId: undefined, assignerName: undefined, arriveTime: undefined,
    faultCause: undefined, repairMeasure: undefined, sparePartsUsed: undefined, downtimeDuration: undefined,
    completeTime: undefined, completeRemark: undefined, verifierId: undefined, verifierName: undefined, verifyOpinion: undefined, rating: 0, remark: undefined }
  proxy.resetForm('workorderRef')
}
function handleView(row) {
  getWorkorder(row.orderId).then(res => {
    viewForm.value = res.data
    viewOpen.value = true
  })
}
function handleAdd() { reset(); activeSectionNames.value = ['order', 'equipment', 'other']; open.value = true; title.value = '新增工单' }
function handleUpdate(row) { reset(); getWorkorder(row.orderId || ids.value[0]).then(res => { form.value = res.data; formSparePartList.value = parseSpareParts(form.value.sparePartsUsed); activeSectionNames.value = ['order', 'equipment']; open.value = true; title.value = '修改工单' }) }
function submitForm() {
  proxy.$refs['workorderRef'].validate(valid => {
    if (valid) {
      if (!validateSparePartStock(formSparePartList.value)) return
      form.value.sparePartsUsed = sparePartsToString(formSparePartList.value) || undefined
      if (form.value.orderId != undefined) { updateWorkorder(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addWorkorder(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const orderIds = row.orderId || ids.value; proxy.$modal.confirm('确认删除编号为"' + orderIds + '"的数据？').then(() => delWorkorder(orderIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('dms/workorder/export', { ...queryParams.value }, `workorder_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// ===== 状态机操作 =====
function handleDispatch(row) {
  isReassign.value = false
  dispatchForm.value = { orderId: row.orderId, orderNo: row.orderNo, equipmentName: row.equipmentName, assigneeId: undefined, assigneeName: '', assignerId: undefined, assignerName: '' }
  dispatchOpen.value = true
}
function submitDispatch() {
  if (!dispatchForm.value.assigneeId) { proxy.$modal.msgError('请选择维修人'); return }
  if (isReassign.value) {
    reassignWorkorder(dispatchForm.value).then(() => { proxy.$modal.msgSuccess('改派成功'); dispatchOpen.value = false; getList() })
  } else {
    dispatchWorkorder(dispatchForm.value).then(() => { proxy.$modal.msgSuccess('派工成功'); dispatchOpen.value = false; getList() })
  }
}
function handleReassign(row) {
  isReassign.value = true
  dispatchForm.value = { orderId: row.orderId, orderNo: row.orderNo, equipmentName: row.equipmentName, assigneeId: undefined, assigneeName: '', assignerId: undefined, assignerName: '' }
  dispatchOpen.value = true
}
function handleAccept(row) {
  proxy.$modal.confirm('确认接单？').then(() => acceptWorkorder(row.orderId)).then(() => { proxy.$modal.msgSuccess('接单成功'); getList() }).catch(() => {})
}
function handleProcess(row) {
  proxy.$modal.confirm('确认开始处理？').then(() => processWorkorder(row.orderId)).then(() => { proxy.$modal.msgSuccess('已开始处理'); getList() }).catch(() => {})
}
function handleComplete(row) {
  completeForm.value = { orderId: row.orderId, orderNo: row.orderNo, orderType: row.orderType, faultCause: '', repairMeasure: '', sparePartsUsed: '', downtimeDuration: 0, completeRemark: '', deviationReason: '' }
  completeSparePartList.value = []
  completeTaskList.value = []
  // 获取工单详情，带出任务清单
  getWorkorder(row.orderId).then(res => {
    if (res.data.taskChecklist) {
      completeTaskList.value = parseTaskChecklist(res.data.taskChecklist)
    }
  })
  completeOpen.value = true
}
function submitComplete() {
  if (!completeForm.value.faultCause) { proxy.$modal.msgError('请输入故障原因'); return }
  if (!completeForm.value.repairMeasure) { proxy.$modal.msgError('请输入维修措施'); return }
  if (!validateSparePartStock(completeSparePartList.value)) return
  completeForm.value.sparePartsUsed = sparePartsToString(completeSparePartList.value)
  // 序列化任务清单完成状态
  if (completeTaskList.value.length > 0) {
    completeForm.value.taskChecklist = JSON.stringify(completeTaskList.value.map(t => ({ text: t.text, done: t.done })))
  }
  completeWorkorder(completeForm.value).then(() => { proxy.$modal.msgSuccess('完工成功'); completeOpen.value = false; getList() })
}
function handleVerify(row) {
  verifyForm.value = { orderId: row.orderId, orderNo: row.orderNo, verifierId: undefined, verifierName: '', rating: 5, verifyOpinion: '', sparePartsUsed: '' }
  // 获取工单详情，带出备件信息
  getWorkorder(row.orderId).then(res => {
    verifyForm.value.sparePartsUsed = res.data.sparePartsUsed || ''
  })
  verifyOpen.value = true
}
function submitVerify() {
  if (!verifyForm.value.verifierId) { proxy.$modal.msgError('请选择验收人'); return }
  verifyWorkorder(verifyForm.value).then(() => {
    const msg = verifyForm.value.sparePartsUsed
      ? '验收成功，已自动创建备件出库单并扣减库存'
      : '验收成功'
    proxy.$modal.msgSuccess(msg)
    verifyOpen.value = false
    getList()
  })
}
function handleReject(row) {
  rejectForm.value = { orderId: row.orderId, orderNo: row.orderNo, verifyOpinion: '' }
  rejectOpen.value = true
}
function submitReject() {
  if (!rejectForm.value.verifyOpinion) { proxy.$modal.msgError('请输入驳回原因'); return }
  rejectWorkorder(rejectForm.value).then(() => { proxy.$modal.msgSuccess('已驳回'); rejectOpen.value = false; getList() })
}
function handleCancel(row) {
  proxy.$modal.confirm('确认撤销该工单？').then(() => cancelWorkorder(row.orderId)).then(() => { proxy.$modal.msgSuccess('已撤销'); getList() }).catch(() => {})
}

// ===== 工单操作历史 =====
function handleLog(row) {
  getWorkorderLog(row.orderId).then(res => {
    logList.value = res.data || []
    logOpen.value = true
  })
}
function actionText(action) {
  const map = { create: '创建', dispatch: '派工', reassign: '改派', accept: '接单', process: '开始处理', complete: '完工', verify: '验收', reject: '驳回', cancel: '撤销' }
  return map[action] || action
}
function statusText(status) {
  if (!status) return '-'
  const map = { '0': '新建', '1': '已派工', '2': '已接单', '3': '处理中', '4': '已完成', '5': '已验收', '6': '已撤销', '7': '驳回重做' }
  return map[status] || status
}

// ===== 工单详情页辅助 =====
const descLabelStyle = { width: '110px', color: '#909399', fontSize: '13px', fontWeight: 'normal', textAlign: 'right', whiteSpace: 'nowrap' }
const descContentStyle = { color: '#303133', fontSize: '13px', whiteSpace: 'nowrap' }
const statusSteps = [
  { label: '新建', status: '0' },
  { label: '已派工', status: '1' },
  { label: '已接单', status: '2' },
  { label: '处理中', status: '3' },
  { label: '已完成', status: '4' },
  { label: '已验收', status: '5' }
]
function orderStatusTagType(status) {
  const map = { '0': 'info', '1': 'warning', '2': 'primary', '3': 'primary', '4': 'success', '5': 'success', '6': 'info', '7': 'danger' }
  return map[status] || 'info'
}
function isStepDone(index) {
  if (!viewForm.value.orderStatus) return false
  const current = parseInt(viewForm.value.orderStatus)
  const stepStatus = parseInt(statusSteps[index].status)
  // 已撤销(6)不算完成任何步骤
  if (current === 6) return false
  // 驳回重做(7)：已完成之前的步骤到处理中(3)
  if (current === 7) return stepStatus <= 3
  return stepStatus < current
}
function isStepActive(index) {
  if (!viewForm.value.orderStatus) return false
  const current = parseInt(viewForm.value.orderStatus)
  const stepStatus = parseInt(statusSteps[index].status)
  // 已撤销(6)：高亮新建
  if (current === 6) return index === 0
  // 驳回重做(7)：高亮处理中
  if (current === 7) return index === 3
  return stepStatus === current
}

// ===== AI 智能报修 =====
function callAI() {
  if (!aiInput.value) { proxy.$modal.msgError('请输入故障描述'); return }
  aiLoading.value = true
  repairAssistant(aiInput.value).then(res => {
    aiResult.value = res.data
    if (res.data.faultDescription) form.value.faultDescription = res.data.faultDescription
    if (res.data.priority) form.value.priority = res.data.priority
    aiDialog.value = false
    aiLoading.value = false
    proxy.$modal.msgSuccess('AI识别完成，请确认后提交')
  }).catch(() => { aiLoading.value = false })
}
// ===== AI 智能派工推荐 =====
function loadRecommend() {
  recommendLoading.value = true
  dispatchRecommend(dispatchForm.value.orderId).then(res => {
    recommendations.value = res.data.recommendations || []
    recommendLoading.value = false
    if (recommendations.value.length === 0) proxy.$modal.msgWarning('暂无推荐数据')
  }).catch(() => { recommendLoading.value = false })
}
/** AI推荐选择维修人 */
function selectRecommendation(rec) {
  dispatchForm.value.assigneeName = rec.assigneeName
  const user = userOptions.value.find(u => u.nickName === rec.assigneeName)
  if (user) {
    dispatchForm.value.assigneeId = user.userId
  }
}

getUserList()
getEquipmentList()
getSparePartList()
getList()

// keep-alive 缓存页面重新激活时自动刷新列表
onActivated(() => {
  getList()
})
</script>

<style scoped>
.workorder-collapse :deep(.el-collapse-item__header) {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  background-color: #f5f7fa;
  padding-left: 12px;
  border-radius: 4px;
  margin-bottom: 8px;
}
.workorder-collapse :deep(.el-collapse-item__wrap) {
  border-bottom: none;
}
.workorder-collapse :deep(.el-collapse-item__content) {
  padding-bottom: 0;
}

/* ===== 工单详情弹窗样式 ===== */
.detail-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.detail-dialog-header .header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.detail-dialog-header .header-icon {
  font-size: 20px;
  color: #409eff;
}
.detail-dialog-header .header-title {
  font-size: 17px;
  font-weight: 700;
  color: #303133;
}
.detail-dialog-header .header-order-no {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  margin-left: 4px;
}
.detail-dialog-header .header-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 状态流程条 */
.status-flow {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 8px 20px;
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
  border-radius: 8px;
  margin-bottom: 16px;
}
.status-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex-shrink: 0;
  margin: 0 22px;
}
.status-step .step-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  background: #e4e7ed;
  color: #c0c4cc;
  border: 2px solid #e4e7ed;
  transition: all 0.3s;
}
.status-step.done .step-icon {
  background: #67c23a;
  color: #fff;
  border-color: #67c23a;
}
.status-step.active .step-icon {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.2);
}
.status-step .step-label {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}
.status-step.active .step-label {
  color: #409eff;
  font-weight: 600;
}
.status-step.done .step-label {
  color: #67c23a;
  font-weight: 500;
}
.status-step .step-arrow {
  position: absolute;
  right: -20px;
  top: 8px;
  color: #dcdfe6;
  font-size: 14px;
}

/* 详情分区卡片 */
.detail-section {
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
  border: 1px solid #ebeef5;
  background: #fff;
}
.detail-section .section-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
 font-size: 14px;
  font-weight: 600;
  color: #303133;
  background: #f5f7fa;
  border-left: 4px solid #dcdfe6;
}
.detail-section .section-header .el-icon {
  font-size: 16px;
}
.detail-section .section-body {
  padding: 16px 20px 4px;
}

/* 分区颜色主题 */
.section-blue .section-header { border-left-color: #409eff; color: #409eff; background: #ecf5ff; }
.section-cyan .section-header { border-left-color: #13c2c2; color: #13c2c2; background: #e6fffb; }
.section-purple .section-header { border-left-color: #722ed1; color: #722ed1; background: #f9f0ff; }
.section-orange .section-header { border-left-color: #fa8c16; color: #fa8c16; background: #fff7e6; }
.section-green .section-header { border-left-color: #52c41a; color: #52c41a; background: #f6ffed; }
.section-gray .section-header { border-left-color: #909399; color: #909399; background: #f4f4f5; }

/* SLA 超时警告样式 */
.sla-warning {
  color: #f56c6c;
  font-weight: 600;
}

/* 详情描述列表：字段名后加冒号，列宽等分，内容不换行 */
.detail-section :deep(.el-descriptions__label)::after {
  content: '：';
}
.detail-section :deep(.el-descriptions__table) {
  table-layout: fixed;
}
.detail-section :deep(.el-descriptions__content) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 任务清单样式 */
.task-checklist {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #ebeef5;
}
.task-checklist-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
  font-size: 13px;
}
.task-checklist-body {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.task-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 6px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  font-size: 13px;
}
.task-item.task-done {
  background: #f0f9eb;
  border-color: #e1f3d8;
}
.task-icon-done {
  color: #67c23a;
  font-size: 16px;
}
.task-icon-pending {
  color: #c0c4cc;
  font-size: 16px;
}
.task-text-done {
  text-decoration: line-through;
  color: #c0c4cc;
}

/* 弹窗内部滚动优化 - 使用非 scoped 样式块处理 */
</style>

<style>
.workorder-detail-dialog .el-dialog__body {
  max-height: 68vh;
  overflow-y: auto;
  padding: 16px 20px;
}
.workorder-detail-dialog .el-dialog__header {
  padding-bottom: 12px;
}
</style>

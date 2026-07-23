<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="活动编号" prop="activityNo">
        <el-input v-model="queryParams.activityNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="活动名称" prop="activityName">
        <el-input v-model="queryParams.activityName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="活动类型" prop="activityType">
        <el-select v-model="queryParams.activityType" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_activity_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="活动状态" prop="activityStatus">
        <el-select v-model="queryParams.activityStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_activity_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:activity:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:activity:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:activity:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="Promotion" :disabled="multiple" @click="handleBatchPublish" v-hasPermi="['marketing:activity:status']">批量发布</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="FolderOpened" :disabled="multiple" @click="handleBatchArchive" v-hasPermi="['marketing:activity:status']">批量归档</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="User" :disabled="multiple" @click="handleBatchOwner" v-hasPermi="['marketing:activity:edit']">批量改负责人</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:activity:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="活动编号" prop="activityNo" :width="colWidth('activityNo', 150)" resizable />
      <el-table-column label="活动名称" prop="activityName" show-overflow-tooltip>
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)">{{ scope.row.activityName }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="类型" prop="activityType" :width="colWidth('activityType', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_activity_type" :value="scope.row.activityType" /></template>
      </el-table-column>
      <el-table-column label="形式" prop="activityForm" :width="colWidth('activityForm', 80)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_activity_form" :value="scope.row.activityForm" /></template>
      </el-table-column>
      <el-table-column label="状态" prop="activityStatus" :width="colWidth('activityStatus', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_activity_status" :value="scope.row.activityStatus" /></template>
      </el-table-column>
      <el-table-column label="开始时间" prop="startTime" :width="colWidth('startTime', 160)" resizable />
      <el-table-column label="结束时间" prop="endTime" :width="colWidth('endTime', 160)" resizable />
      <el-table-column label="预算" prop="budget" :width="colWidth('budget', 100)" resizable align="right" />
      <el-table-column label="报名" prop="signupCount" :width="colWidth('signupCount', 70)" resizable align="center" />
      <el-table-column label="签到" prop="signedCount" :width="colWidth('signedCount', 70)" resizable align="center" />
      <el-table-column label="线索" prop="convertedLeadCount" :width="colWidth('convertedLeadCount', 70)" resizable align="center" />
      <el-table-column label="成交额" prop="dealAmount" :width="colWidth('dealAmount', 100)" resizable align="right" />
      <el-table-column label="负责人" prop="userName" :width="colWidth('userName', 100)" resizable />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:activity:edit']">修改</el-button>
          <el-dropdown @command="(cmd) => handleCommand(cmd, scope.row)" trigger="click">
            <el-button link type="primary" icon="ArrowDown">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <!-- 状态流转 -->
                <el-dropdown-item command="publish" icon="Promotion" v-if="scope.row.activityStatus === '0'" v-hasPermi="['marketing:activity:status']">发布</el-dropdown-item>
                <el-dropdown-item command="start" icon="VideoPlay" v-if="scope.row.activityStatus === '1'" v-hasPermi="['marketing:activity:status']">开始</el-dropdown-item>
                <el-dropdown-item command="finish" icon="CircleClose" v-if="scope.row.activityStatus === '2'" v-hasPermi="['marketing:activity:status']">结束</el-dropdown-item>
                <el-dropdown-item command="settle" icon="DataAnalysis" v-if="['3','4'].includes(scope.row.activityStatus)" v-hasPermi="['marketing:activity:edit']">活动结算</el-dropdown-item>
                <el-dropdown-item command="archive" icon="FolderOpened" v-if="scope.row.activityStatus === '3'" v-hasPermi="['marketing:activity:status']">归档</el-dropdown-item>
                <el-dropdown-item command="cancel" icon="CircleClose" v-if="['1','2'].includes(scope.row.activityStatus)" v-hasPermi="['marketing:activity:status']" divided>取消活动</el-dropdown-item>
                <!-- 业务操作 -->
                <el-dropdown-item command="copy" icon="CopyDocument" v-hasPermi="['marketing:activity:copy']" divided>复制活动</el-dropdown-item>
                <el-dropdown-item command="promote" icon="Link" v-if="scope.row.activityForm === '0'">报名推广</el-dropdown-item>
                <el-dropdown-item command="delete" icon="Delete" v-hasPermi="['marketing:activity:remove']" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="850px" append-to-body>
      <el-form ref="activityRef" :model="form" :rules="rules" label-width="110px">
        <el-collapse v-model="activeNames">
          <el-collapse-item title="基本信息" name="basic">
            <el-row>
              <el-col :span="12"><el-form-item label="活动编号" prop="activityNo"><el-input v-model="form.activityNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="活动名称" prop="activityName"><el-input v-model="form.activityName" placeholder="请输入活动名称" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="活动类型" prop="activityType">
                <el-select v-model="form.activityType" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_activity_type" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="活动形式" prop="activityForm">
                <el-select v-model="form.activityForm" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_activity_form" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="活动状态" prop="activityStatus">
                <el-select v-model="form.activityStatus" placeholder="请选择" style="width: 100%" :disabled="form.activityId != null">
                  <el-option v-for="d in marketing_activity_status" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="活动地点/链接" prop="location"><el-input v-model="form.location" placeholder="线下地点或线上链接" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="时间与预算" name="schedule">
            <el-row>
              <el-col :span="12"><el-form-item label="开始时间" prop="startTime"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择开始时间" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="结束时间" prop="endTime"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择结束时间" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="活动预算" prop="budget"><el-input-number v-model="form.budget" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="实际花费" prop="cost"><el-input-number v-model="form.cost" :min="0" :precision="2" controls-position="right" style="width: 100%" :disabled="!isSettlementPhase" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="目标人数" prop="targetCount"><el-input-number v-model="form.targetCount" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="目标线索数" prop="targetLeadCount"><el-input-number v-model="form.targetLeadCount" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="负责信息" name="owner">
            <el-row>
              <el-col :span="12"><el-form-item label="负责人" prop="userId">
                <el-select v-model="form.userId" filterable clearable placeholder="请选择负责人" style="width: 100%" @change="onUserChange">
                  <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="联系方式" prop="ownerPhone"><el-input v-model="form.ownerPhone" placeholder="手机号/微信号" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="所属部门" prop="deptId">
                <el-tree-select v-model="form.deptId" :data="deptOptions" :props="{ value: 'id', label: 'label', children: 'children' }" value-key="id" placeholder="请选择部门" check-strictly clearable style="width: 100%" />
              </el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="活动详情" name="detail">
            <el-form-item label="活动简介" prop="summary"><el-input v-model="form.summary" type="textarea" :rows="2" placeholder="请输入活动简介" /></el-form-item>
            <el-form-item label="活动详情" prop="content"><el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入活动详情" /></el-form-item>
            <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
          </el-collapse-item>
          <el-collapse-item title="附件资料" name="attachment">
            <el-form-item label="附件上传">
              <el-upload
                multiple
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :before-upload="handleBeforeAttachmentUpload"
                :on-success="handleAttachmentSuccess"
                :on-error="handleAttachmentError"
                :limit="10"
                :on-exceed="() => proxy.$modal.msgError('最多上传10个文件')"
              >
                <el-button type="primary" icon="Upload">上传文件</el-button>
                <template #tip>
                  <div class="el-upload__tip">支持 doc/xls/ppt/pdf/txt/图片/压缩包(zip/rar/7z) 等格式，单文件不超过 20MB</div>
                </template>
              </el-upload>
              <!-- 自定义文件列表 -->
              <div class="attachment-list" v-if="attachmentFileList.length > 0">
                <div v-for="(file, index) in attachmentFileList" :key="index" class="attachment-list-item">
                  <el-link :underline="false" @click="handleAttachmentPreview(file)" type="primary">
                    <el-icon style="margin-right: 4px"><Document /></el-icon>
                    <span class="attachment-name">{{ file.name }}</span>
                  </el-link>
                  <el-link :underline="false" @click="handleAttachmentDelete(index)" type="danger">删除</el-link>
                </div>
              </div>
            </el-form-item>
          </el-collapse-item>
          <el-collapse-item name="result">
            <template #title>
              <span>活动效果（结算）</span>
              <el-tag v-if="!isSettlementPhase" type="info" size="small" style="margin-left: 8px">活动结束后填写</el-tag>
            </template>
            <el-alert v-if="!isSettlementPhase" title="活动效果数据需在活动结束后进行结算填写" type="info" :closable="false" show-icon style="margin-bottom: 16px" />
            <el-row>
              <el-col :span="8"><el-form-item label="实际参与" prop="actualCount"><el-input-number v-model="form.actualCount" :min="0" controls-position="right" style="width: 100%" :disabled="!isSettlementPhase" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="获取线索" prop="leadCount"><el-input-number v-model="form.leadCount" :min="0" controls-position="right" style="width: 100%" :disabled="!isSettlementPhase" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item prop="mqlCount">
                <template #label><span>MQL阶段</span><el-tooltip content="MQL（Marketing Qualified Lead）营销合格线索：经过营销团队初步筛选，符合目标客户画像，有进一步培育价值的线索" placement="top"><el-icon style="margin-left: 2px; vertical-align: middle; color: #909399"><QuestionFilled /></el-icon></el-tooltip></template>
                <el-input-number v-model="form.mqlCount" :min="0" controls-position="right" style="width: 100%" :disabled="!isSettlementPhase" />
              </el-form-item></el-col>
              <el-col :span="8"><el-form-item prop="sqlCount">
                <template #label><span>SQL阶段</span><el-tooltip content="SQL（Sales Qualified Lead）销售合格线索：经过销售团队确认，有明确购买意向和预算，可进入销售跟进阶段的线索" placement="top"><el-icon style="margin-left: 2px; vertical-align: middle; color: #909399"><QuestionFilled /></el-icon></el-tooltip></template>
                <el-input-number v-model="form.sqlCount" :min="0" controls-position="right" style="width: 100%" :disabled="!isSettlementPhase" />
              </el-form-item></el-col>
              <el-col :span="8"><el-form-item label="转化商机" prop="opportunityCount"><el-input-number v-model="form.opportunityCount" :min="0" controls-position="right" style="width: 100%" :disabled="!isSettlementPhase" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="成交金额" prop="dealAmount"><el-input-number v-model="form.dealAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" :disabled="!isSettlementPhase" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 活动结算弹窗 -->
    <el-dialog title="活动结算" v-model="settleOpen" width="700px" append-to-body>
      <el-alert title="请填写活动实际效果数据，完成结算后可归档活动" type="info" :closable="false" show-icon style="margin-bottom: 20px" />
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="活动名称">{{ settleForm.activityName }}</el-descriptions-item>
        <el-descriptions-item label="活动时间">{{ settleForm.startTime }} ~ {{ settleForm.endTime }}</el-descriptions-item>
        <el-descriptions-item label="活动预算">{{ settleForm.budget }} 元</el-descriptions-item>
        <el-descriptions-item label="目标人数">{{ settleForm.targetCount }} 人</el-descriptions-item>
      </el-descriptions>
      <el-form ref="settleRef" :model="settleForm" label-width="100px">
        <el-row>
          <el-col :span="12"><el-form-item label="实际花费" prop="cost">
            <el-input-number v-model="settleForm.cost" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            <div class="settle-field-tip">活动实际支出的总费用</div>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="实际参与" prop="actualCount">
            <el-input-number v-model="settleForm.actualCount" :min="0" controls-position="right" style="width: 100%" />
            <div class="settle-field-tip">实际到场参与人数</div>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="获取线索" prop="leadCount">
            <el-input-number v-model="settleForm.leadCount" :min="0" controls-position="right" style="width: 100%" />
            <div class="settle-field-tip">活动收集到的线索总数</div>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item prop="mqlCount">
            <template #label><span>MQL阶段</span><el-tooltip content="MQL（Marketing Qualified Lead）营销合格线索：经过营销团队初步筛选，符合目标客户画像，有进一步培育价值的线索" placement="top"><el-icon style="margin-left: 2px; vertical-align: middle; color: #909399"><QuestionFilled /></el-icon></el-tooltip></template>
            <el-input-number v-model="settleForm.mqlCount" :min="0" controls-position="right" style="width: 100%" />
            <div class="settle-field-tip">营销合格线索数量</div>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item prop="sqlCount">
            <template #label><span>SQL阶段</span><el-tooltip content="SQL（Sales Qualified Lead）销售合格线索：经过销售团队确认，有明确购买意向和预算，可进入销售跟进阶段的线索" placement="top"><el-icon style="margin-left: 2px; vertical-align: middle; color: #909399"><QuestionFilled /></el-icon></el-tooltip></template>
            <el-input-number v-model="settleForm.sqlCount" :min="0" controls-position="right" style="width: 100%" />
            <div class="settle-field-tip">销售合格线索数量</div>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="转化商机" prop="opportunityCount">
            <el-input-number v-model="settleForm.opportunityCount" :min="0" controls-position="right" style="width: 100%" />
            <div class="settle-field-tip">转化为商机的数量</div>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="成交金额" prop="dealAmount">
            <el-input-number v-model="settleForm.dealAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            <div class="settle-field-tip">活动带来的成交总金额</div>
          </el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmSettle">确认结算</el-button>
        <el-button @click="settleOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 文件在线预览 -->
    <file-preview ref="filePreviewRef" />

    <!-- 取消活动弹窗 -->
    <el-dialog title="取消活动" v-model="cancelOpen" width="500px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="取消原因"><el-input v-model="cancelRemark" type="textarea" :rows="3" placeholder="请输入取消原因" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="confirmCancel">确认取消</el-button>
        <el-button @click="cancelOpen = false">返 回</el-button>
      </template>
    </el-dialog>

    <!-- 批量改负责人弹窗 -->
    <el-dialog title="批量修改负责人" v-model="ownerOpen" width="500px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="负责人">
          <el-select v-model="batchOwnerUserId" filterable placeholder="请选择负责人" style="width: 100%" @change="onBatchUserChange">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmBatchOwner">确 定</el-button>
        <el-button @click="ownerOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- P3-15: 报名推广中心 -->
    <el-dialog title="报名推广中心" v-model="promoteOpen" width="600px" append-to-body v-loading="promoteLoading">
      <div v-if="promoteData.url" class="promote-center">
        <!-- 活动名称 -->
        <div class="promote-activity-name">{{ promoteData.activityName }}</div>

        <!-- 二维码区域 -->
        <div class="promote-qr-section">
          <canvas ref="qrCanvasRef" class="promote-qr-canvas"></canvas>
          <div class="promote-qr-tip">扫描二维码进入报名页面</div>
        </div>

        <!-- 链接区域 -->
        <div class="promote-url-section">
          <div class="promote-url-label">报名链接</div>
          <el-input :model-value="promoteData.url" readonly>
            <template #append>
              <el-button @click="copyPromoteUrl">复制链接</el-button>
            </template>
          </el-input>
        </div>

        <!-- 操作按钮 -->
        <div class="promote-actions">
          <el-button type="primary" icon="Download" @click="downloadQrCode">下载二维码</el-button>
          <el-button type="success" icon="Promotion" @click="openPromotePage">预览报名页</el-button>
        </div>

        <!-- 业务说明 -->
        <el-divider content-position="left">推广说明</el-divider>
        <div class="promote-guide">
          <p><b>1. 线上推广</b>：复制链接，通过微信、邮件、短信发送给客户</p>
          <p><b>2. 线下推广</b>：下载二维码，印制在宣传海报、易拉宝、桌牌上</p>
          <p><b>3. 现场签到</b>：客户报名成功后获得签到码，活动当天扫码或输入签到码完成签到</p>
          <p><b>4. 线索转化</b>：签到后的参与人可一键转化为营销线索，进入销售漏斗</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="MkActivity">
import { useRouter } from 'vue-router'
import QRCode from 'qrcode'
import { getToken } from '@/utils/auth'
import { listActivity, getActivity, addActivity, updateActivity, delActivity, changeActivityStatus, copyActivity, batchChangeStatus, batchUpdateOwner, getRegisterUrl } from '@/api/mk/activity'
import { listUser, deptTreeSelect } from '@/api/system/user'
import { useColumnResize } from '@/composables/useColumnResize'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_activity_index')
const { marketing_activity_type, marketing_activity_form, marketing_activity_status } = proxy.useDict('marketing_activity_type', 'marketing_activity_form', 'marketing_activity_status')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const userOptions = ref([])
const deptOptions = ref([])
const activeNames = ref(['basic', 'schedule', 'owner', 'detail', 'attachment'])
const cancelOpen = ref(false)
const cancelRemark = ref('')
const cancelRow = ref({})
// 活动结算
const settleOpen = ref(false)
const settleForm = ref({})
const ownerOpen = ref(false)
const batchOwnerUserId = ref(null)
const batchOwnerName = ref('')
const batchOwnerDeptId = ref(null)
const batchOwnerPhone = ref('')
// P3-15: 报名推广中心
const promoteOpen = ref(false)
const promoteLoading = ref(false)
const promoteData = ref({ url: '', token: '', activityName: '' })
const qrCanvasRef = ref(null)

const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/common/upload'
const uploadHeaders = { Authorization: 'Bearer ' + getToken() }
const filePreviewRef = ref(null)

// 活动效果字段仅在活动结束后可填写
const isSettlementPhase = computed(() => form.value.activityId != null && ['3', '4'].includes(form.value.activityStatus))

// 附件文件列表（从 form.attachments 逗号分隔字符串解析）
const attachmentFileList = computed(() => {
  if (!form.value.attachments) return []
  return form.value.attachments.split(',').filter(url => url).map(url => ({
    name: url.substring(url.lastIndexOf('/') + 1),
    url: url
  }))
})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, activityNo: undefined, activityName: undefined, activityType: undefined, activityStatus: undefined },
  rules: {
    activityName: [{ required: true, message: '活动名称不能为空', trigger: 'blur' }],
    activityType: [{ required: true, message: '活动类型不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listActivity(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function getUserOptions() {
  listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) })
}
function getDeptTree() {
  deptTreeSelect().then(res => { deptOptions.value = res.data })
}
function onUserChange(userId) {
  if (userId) {
    const user = userOptions.value.find(u => u.userId === userId)
    if (user) {
      form.value.userName = user.nickName
      if (user.phonenumber) form.value.ownerPhone = user.phonenumber
      if (user.deptId) {
        form.value.deptId = user.deptId
        form.value.deptName = user.dept ? user.dept.deptName : undefined
      }
    }
  } else {
    form.value.userName = undefined
    form.value.ownerPhone = undefined
  }
}
function onBatchUserChange(userId) {
  if (userId) {
    const user = userOptions.value.find(u => u.userId === userId)
    if (user) {
      batchOwnerName.value = user.nickName
      batchOwnerDeptId.value = user.deptId
      batchOwnerPhone.value = user.phonenumber || ''
    }
  }
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.activityId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = { activityNo: undefined, activityName: undefined, activityType: undefined, activityForm: undefined, activityStatus: '0', startTime: undefined, endTime: undefined, location: undefined, budget: 0, cost: 0, targetCount: 0, targetLeadCount: 0, actualCount: 0, leadCount: 0, mqlCount: 0, sqlCount: 0, opportunityCount: 0, dealAmount: 0, userId: undefined, userName: undefined, ownerPhone: undefined, deptId: undefined, deptName: undefined, summary: undefined, content: undefined, attachments: undefined, remark: undefined }
  proxy.resetForm('activityRef')
}
function handleAdd() { reset(); activeNames.value = ['basic', 'schedule', 'owner', 'detail', 'attachment']; open.value = true; title.value = '新增营销活动' }
function handleUpdate(row) { reset(); getActivity(row.activityId || ids.value[0]).then(res => { form.value = res.data; activeNames.value = ['basic', 'schedule', 'owner', 'detail', 'attachment']; if (['3', '4'].includes(res.data.activityStatus)) activeNames.value.push('result'); open.value = true; title.value = '修改营销活动' }) }
function handleDetail(row) { router.push('/mk/activity-detail/' + row.activityId) }
function submitForm() {
  proxy.$refs['activityRef'].validate(valid => {
    if (valid) {
      if (form.value.activityId != undefined) { updateActivity(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addActivity(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const activityIds = row.activityId || ids.value; proxy.$modal.confirm('确认删除编号为"' + activityIds + '"的数据？').then(() => delActivity(activityIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('mk/activity/export', { ...queryParams.value }, `activity_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// P0-1: 状态流转
function handleChangeStatus(row, toStatus) {
  proxy.$modal.confirm('确认将活动"' + row.activityName + '"状态变更为[' + statusLabel(toStatus) + ']？').then(() => {
    changeActivityStatus(row.activityId, { toStatus, remark: '' }).then(() => {
      proxy.$modal.msgSuccess('状态变更成功')
      getList()
      // 结束活动后提示是否立即结算
      if (toStatus === '3') {
        setTimeout(() => {
          proxy.$modal.confirm('活动已结束，是否立即填写活动效果进行结算？').then(() => {
            handleSettle(row)
          }).catch(() => {})
        }, 500)
      }
    })
  }).catch(() => {})
}

// 活动结算
function handleSettle(row) {
  getActivity(row.activityId).then(res => {
    settleForm.value = res.data
    settleOpen.value = true
  })
}
function confirmSettle() {
  updateActivity(settleForm.value).then(() => {
    proxy.$modal.msgSuccess('结算成功')
    settleOpen.value = false
    getList()
  })
}
function handleCancel(row) {
  cancelRow.value = row
  cancelRemark.value = ''
  cancelOpen.value = true
}
function confirmCancel() {
  if (!cancelRemark.value) { proxy.$modal.msgError('请填写取消原因'); return }
  changeActivityStatus(cancelRow.value.activityId, { toStatus: '5', remark: cancelRemark.value }).then(() => {
    proxy.$modal.msgSuccess('已取消')
    cancelOpen.value = false
    getList()
  })
}
function statusLabel(val) {
  const map = { '0': '草稿', '1': '已发布', '2': '进行中', '3': '已结束', '4': '已归档', '5': '已取消' }
  return map[val] || val
}

// P2-11: 活动复制
function handleCopy(row) {
  proxy.$modal.confirm('确认复制活动"' + row.activityName + '"？').then(() => copyActivity(row.activityId)).then(() => {
    proxy.$modal.msgSuccess('复制成功')
    getList()
  }).catch(() => {})
}

// 操作下拉菜单统一分发
function handleCommand(cmd, row) {
  switch (cmd) {
    case 'publish': handleChangeStatus(row, '1'); break
    case 'start': handleChangeStatus(row, '2'); break
    case 'finish': handleChangeStatus(row, '3'); break
    case 'archive': handleChangeStatus(row, '4'); break
    case 'settle': handleSettle(row); break
    case 'cancel': handleCancel(row); break
    case 'copy': handleCopy(row); break
    case 'promote': handleRegisterUrl(row); break
    case 'delete': handleDelete(row); break
  }
}

// P3-12: 批量操作
function handleBatchPublish() {
  proxy.$modal.confirm('确认批量发布选中的' + ids.value.length + '个活动？').then(() => {
    batchChangeStatus({ activityIds: ids.value, status: '1' }).then(() => {
      proxy.$modal.msgSuccess('批量发布成功')
      getList()
    })
  }).catch(() => {})
}
function handleBatchArchive() {
  proxy.$modal.confirm('确认批量归档选中的' + ids.value.length + '个活动？').then(() => {
    batchChangeStatus({ activityIds: ids.value, status: '4' }).then(() => {
      proxy.$modal.msgSuccess('批量归档成功')
      getList()
    })
  }).catch(() => {})
}
function handleBatchOwner() {
  batchOwnerUserId.value = null
  batchOwnerName.value = ''
  ownerOpen.value = true
}
function confirmBatchOwner() {
  if (!batchOwnerUserId.value) { proxy.$modal.msgError('请选择负责人'); return }
  batchUpdateOwner({ activityIds: ids.value, userId: batchOwnerUserId.value, userName: batchOwnerName.value, deptId: batchOwnerDeptId.value, ownerPhone: batchOwnerPhone.value }).then(() => {
    proxy.$modal.msgSuccess('批量修改成功')
    ownerOpen.value = false
    getList()
  })
}

// P3-15: 报名推广中心
async function handleRegisterUrl(row) {
  promoteOpen.value = true
  promoteLoading.value = true
  promoteData.value = { url: '', token: '', activityName: row.activityName }
  try {
    const res = await getRegisterUrl(row.activityId)
    const fullUrl = window.location.origin + res.url
    promoteData.value = { url: fullUrl, token: res.token, activityName: row.activityName }
    // 生成二维码
    await nextTick()
    if (qrCanvasRef.value) {
      await QRCode.toCanvas(qrCanvasRef.value, fullUrl, {
        width: 240,
        margin: 2,
        color: { dark: '#1a1a2e', light: '#ffffff' }
      })
    }
  } finally {
    promoteLoading.value = false
  }
}
function copyPromoteUrl() {
  navigator.clipboard.writeText(promoteData.value.url).then(() => {
    proxy.$modal.msgSuccess('链接已复制到剪贴板')
  }).catch(() => {
    // fallback
    const input = document.createElement('input')
    input.value = promoteData.value.url
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    proxy.$modal.msgSuccess('链接已复制到剪贴板')
  })
}
function downloadQrCode() {
  if (!qrCanvasRef.value) return
  const link = document.createElement('a')
  link.download = '活动报名二维码_' + promoteData.value.activityName + '.png'
  link.href = qrCanvasRef.value.toDataURL('image/png')
  link.click()
}
function openPromotePage() {
  window.open(promoteData.value.url, '_blank')
}

// 附件上传相关
function handleBeforeAttachmentUpload(file) {
  const allowedTypes = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'pdf', 'txt', 'png', 'jpg', 'jpeg', 'gif', 'bmp', 'zip', 'rar', '7z', 'gz', 'tar']
  const ext = file.name.split('.').pop().toLowerCase()
  if (!allowedTypes.includes(ext)) {
    proxy.$modal.msgError('不支持的文件格式：' + ext)
    return false
  }
  if (file.size / 1024 / 1024 > 20) {
    proxy.$modal.msgError('文件大小不能超过 20MB')
    return false
  }
  proxy.$modal.loading('正在上传文件，请稍候...')
  return true
}
function handleAttachmentSuccess(res) {
  proxy.$modal.closeLoading()
  if (res.code === 200) {
    const url = res.fileName
    const current = form.value.attachments ? form.value.attachments.split(',').filter(u => u) : []
    current.push(url)
    form.value.attachments = current.join(',')
    proxy.$modal.msgSuccess('上传成功')
  } else {
    proxy.$modal.msgError(res.msg || '上传失败')
  }
}
function handleAttachmentError() {
  proxy.$modal.closeLoading()
  proxy.$modal.msgError('上传失败')
}
function handleAttachmentDelete(index) {
  const current = form.value.attachments ? form.value.attachments.split(',').filter(u => u) : []
  current.splice(index, 1)
  form.value.attachments = current.length > 0 ? current.join(',') : undefined
}
function handleAttachmentPreview(file) {
  filePreviewRef.value?.open(file.url, file.name)
}

getUserOptions()
getDeptTree()
getList()
</script>

<style scoped>
.promote-center {
  text-align: center;
}
.promote-activity-name {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 20px;
}
.promote-qr-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}
.promote-qr-canvas {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.promote-qr-tip {
  margin-top: 10px;
  font-size: 13px;
  color: #909399;
}
.promote-url-section {
  margin-bottom: 16px;
}
.promote-url-label {
  text-align: left;
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}
.promote-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}
.promote-guide {
  text-align: left;
  font-size: 13px;
  color: #606266;
  line-height: 2;
}
.promote-guide p {
  margin: 0;
}
.attachment-list {
  margin-top: 12px;
  width: 100%;
}
.attachment-list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 8px;
  transition: background 0.2s;
}
.attachment-list-item:hover {
  background: #f5f7fa;
}
.attachment-name {
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
  vertical-align: middle;
}
.settle-field-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  margin-top: 2px;
}
</style>

<template>
  <div class="app-container wms-list-page">
    <el-tabs v-model="activeTab" class="rebate-tabs">
      <!-- ==================== 页签一：返利政策 ==================== -->
      <el-tab-pane label="返利政策" name="policy">
        <!-- ===== Filter Card ===== -->
        <div class="surface filter-card" v-show="policyShowSearch">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
          </div>
          <div class="filter-bar">
            <div class="field">
              <label>客户名称</label>
              <div class="control">
                <el-input v-model="policyQuery.customerName" placeholder="请输入" clearable @keyup.enter="handlePolicyQuery" />
              </div>
            </div>
            <div class="field">
              <label>状态</label>
              <div class="control is-select">
                <el-select v-model="policyQuery.status" placeholder="全部" clearable @change="handlePolicyQuery">
                  <el-option label="启用" value="0" />
                  <el-option label="停用" value="1" />
                </el-select>
              </div>
            </div>
          </div>
          <div class="filter-actions">
            <div class="filter-info">
              <el-icon><Filter /></el-icon> 已选 {{ policyFilterCount }} 个条件，支持回车快速搜索
            </div>
            <div class="filter-buttons">
              <el-button icon="RefreshLeft" @click="resetPolicyQuery">重置</el-button>
              <el-button type="primary" icon="Search" @click="handlePolicyQuery">搜索</el-button>
            </div>
          </div>
        </div>

        <!-- ===== Table Section ===== -->
        <div class="surface">
          <div class="toolbar">
            <div class="left">
              <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:rebate:add']">新增</el-button>
              <el-button type="success" plain icon="Edit" @click="handleUpdate()" v-hasPermi="['marketing:rebate:edit']">修改</el-button>
              <el-button type="danger" plain icon="Delete" @click="handleDelete()" v-hasPermi="['marketing:rebate:remove']">删除</el-button>
            </div>
            <div class="right">
              <right-toolbar v-model:showSearch="policyShowSearch" @queryTable="getPolicyList" :columns="policyColumns" storageKey="rebate_policy_visible" />
            </div>
          </div>

          <div class="table-wrap">
            <el-table ref="tableRef" v-loading="policyLoading" :data="policyList" border class="app-table" @selection-change="handlePolicySelectionChange" @header-dragend="onHeaderDragEnd">
              <el-table-column type="selection" width="50" align="center" />
              <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 60)" resizable align="center" />
              <el-table-column label="政策编号" prop="policyNo" key="policyNo" :width="colWidth('policyNo', 140)" resizable v-if="policyColumns.policyNo.visible">
                <template #default="scope"><span class="mono">{{ scope.row.policyNo }}</span></template>
              </el-table-column>
              <el-table-column label="政策名称" prop="policyName" key="policyName" :min-width="colWidth('policyName', 150)" resizable show-overflow-tooltip v-if="policyColumns.policyName.visible" />
              <el-table-column label="客户名称" prop="customerName" key="customerName" :width="colWidth('customerName', 160)" resizable show-overflow-tooltip v-if="policyColumns.customerName.visible">
                <template #default="scope">
                  <span v-if="scope.row.customerName">{{ scope.row.customerName }}</span>
                  <span v-else class="text-muted">全部客户</span>
                </template>
              </el-table-column>
              <el-table-column label="考核口径" prop="basis" key="basis" :width="colWidth('basis', 100)" resizable align="center" v-if="policyColumns.basis.visible">
                <template #default="scope">
                  <span class="badge" :class="scope.row.basis === '2' ? 'green' : 'blue'"><span class="dot"></span>{{ dictLabel(mk_rebate_basis, scope.row.basis) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="周期类型" prop="periodType" key="periodType" :width="colWidth('periodType', 100)" resizable align="center" v-if="policyColumns.periodType.visible">
                <template #default="scope">
                  <span class="badge" :class="periodBadgeClass(scope.row.periodType)"><span class="dot"></span>{{ dictLabel(mk_rebate_period, scope.row.periodType) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="阶梯数" prop="tierCount" key="tierCount" :width="colWidth('tierCount', 80)" resizable align="center" v-if="policyColumns.tierCount.visible">
                <template #default="scope"><span class="mono">{{ (scope.row.tiers || []).length }}档</span></template>
              </el-table-column>
              <el-table-column label="生效日期" prop="startDate" key="startDate" :width="colWidth('startDate', 110)" resizable align="center" v-if="policyColumns.startDate.visible">
                <template #default="scope">{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }}</template>
              </el-table-column>
              <el-table-column label="失效日期" prop="endDate" key="endDate" :width="colWidth('endDate', 110)" resizable align="center" v-if="policyColumns.endDate.visible">
                <template #default="scope">{{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</template>
              </el-table-column>
              <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="policyColumns.status.visible">
                <template #default="scope">
                  <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '启用' : '停用' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" :width="colWidth('操作', 130)" align="center" fixed="right" class-name="col-action">
                <template #default="scope">
                  <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:rebate:edit']">修改</el-button>
                  <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:rebate:remove']">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="table-pager">
            <pagination v-show="policyTotal > 0" :total="policyTotal" v-model:page="policyQuery.pageNum" v-model:limit="policyQuery.pageSize" @pagination="getPolicyList" />
          </div>
        </div>
      </el-tab-pane>

      <!-- ==================== 页签二：返利结算 ==================== -->
      <el-tab-pane label="返利结算" name="settle">
        <!-- ===== Filter Card ===== -->
        <div class="surface filter-card" v-show="settleShowSearch">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
          </div>
          <div class="filter-bar">
            <div class="field">
              <label>客户名称</label>
              <div class="control">
                <el-input v-model="settleQuery.customerName" placeholder="请输入" clearable @keyup.enter="handleSettleQuery" />
              </div>
            </div>
            <div class="field">
              <label>期间</label>
              <div class="control is-date">
                <el-date-picker v-model="settleQuery.period" type="month" value-format="YYYYMM" placeholder="选择期间" style="width: 100%" clearable @change="handleSettleQuery" />
              </div>
            </div>
            <div class="field">
              <label>状态</label>
              <div class="control is-select">
                <el-select v-model="settleQuery.status" placeholder="全部" clearable @change="handleSettleQuery">
                  <el-option v-for="d in mk_rebate_settle_status" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </div>
            </div>
          </div>
          <div class="filter-actions">
            <div class="filter-info">
              <el-icon><Filter /></el-icon> 已选 {{ settleFilterCount }} 个条件
            </div>
            <div class="filter-buttons">
              <el-button icon="RefreshLeft" @click="resetSettleQuery">重置</el-button>
              <el-button type="primary" icon="Search" @click="handleSettleQuery">搜索</el-button>
            </div>
          </div>
        </div>

        <!-- ===== Table Section ===== -->
        <div class="surface">
          <div class="toolbar">
            <div class="left">
              <el-button type="primary" plain icon="Money" @click="handleSettle" v-hasPermi="['marketing:rebate:settle']">核算返利</el-button>
            </div>
            <div class="right">
              <right-toolbar v-model:showSearch="settleShowSearch" @queryTable="getSettleList" :columns="settleColumns" storageKey="rebate_settle_visible" />
            </div>
          </div>

          <div class="table-wrap">
            <el-table v-loading="settleLoading" :data="settleList" border class="app-table" @header-dragend="onHeaderDragEnd">
              <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 60)" resizable align="center" />
              <el-table-column label="结算单号" prop="settleNo" key="settleNo" :width="colWidth('settleNo', 160)" resizable v-if="settleColumns.settleNo.visible">
                <template #default="scope"><span class="mono">{{ scope.row.settleNo }}</span></template>
              </el-table-column>
              <el-table-column label="客户名称" prop="customerName" key="customerName" :min-width="colWidth('customerName', 150)" resizable show-overflow-tooltip v-if="settleColumns.customerName.visible" />
              <el-table-column label="期间" prop="period" key="period" :width="colWidth('period', 90)" resizable align="center" v-if="settleColumns.period.visible">
                <template #default="scope"><span class="mono">{{ scope.row.period }}</span></template>
              </el-table-column>
              <el-table-column label="政策编号" prop="policyNo" key="policyNo" :width="colWidth('policyNo', 140)" resizable v-if="settleColumns.policyNo.visible">
                <template #default="scope"><span class="mono">{{ scope.row.policyNo }}</span></template>
              </el-table-column>
              <el-table-column label="政策名称" prop="policyName" key="policyName" :min-width="colWidth('policyName', 140)" resizable show-overflow-tooltip v-if="settleColumns.policyName.visible" />
              <el-table-column label="达成额" prop="achieveAmount" key="achieveAmount" :width="colWidth('achieveAmount', 120)" resizable align="right" v-if="settleColumns.achieveAmount.visible">
                <template #default="scope"><span class="mono">{{ scope.row.achieveAmount }}</span></template>
              </el-table-column>
              <el-table-column label="比例" prop="rebateRate" key="rebateRate" :width="colWidth('rebateRate', 80)" resizable align="center" v-if="settleColumns.rebateRate.visible">
                <template #default="scope"><span class="mono">{{ scope.row.rebateRate }}%</span></template>
              </el-table-column>
              <el-table-column label="返利额" prop="rebateAmount" key="rebateAmount" :width="colWidth('rebateAmount', 110)" resizable align="right" v-if="settleColumns.rebateAmount.visible">
                <template #default="scope"><span class="mono">{{ scope.row.rebateAmount }}</span></template>
              </el-table-column>
              <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="settleColumns.status.visible">
                <template #default="scope">
                  <span class="badge" :class="scope.row.status === '1' ? 'green' : 'amber'"><span class="dot"></span>{{ dictLabel(mk_rebate_settle_status, scope.row.status) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="兑现方式" prop="redeemType" key="redeemType" :width="colWidth('redeemType', 100)" resizable align="center" v-if="settleColumns.redeemType.visible">
                <template #default="scope">
                  <span v-if="scope.row.redeemType">{{ scope.row.redeemType }}</span>
                  <span v-else class="text-muted">—</span>
                </template>
              </el-table-column>
              <el-table-column label="兑现时间" prop="redeemDate" key="redeemDate" :width="colWidth('redeemDate', 150)" resizable align="center" v-if="settleColumns.redeemDate.visible">
                <template #default="scope">{{ parseTime(scope.row.redeemDate) || '—' }}</template>
              </el-table-column>
              <el-table-column label="操作" :width="colWidth('操作', 90)" align="center" fixed="right" class-name="col-action">
                <template #default="scope">
                  <el-button v-if="scope.row.status === '0'" link type="primary" icon="Money" @click="handleRedeem(scope.row)" v-hasPermi="['marketing:rebate:redeem']">兑现</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="table-pager">
            <pagination v-show="settleTotal > 0" :total="settleTotal" v-model:page="settleQuery.pageNum" v-model:limit="settleQuery.pageSize" @pagination="getSettleList" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ===== 政策弹框（新增/修改） ===== -->
    <el-dialog v-model="policyOpen" width="780px" append-to-body draggable class="rd-dialog" @closed="cancelPolicy">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ policyTitle }}</span>
        </div>
      </template>
      <el-form ref="policyRef" :model="policyForm" :rules="policyRules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.83z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="政策名称" prop="policyName">
                    <el-input v-model="policyForm.policyName" placeholder="请输入政策名称" maxlength="100" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="客户" prop="customerId">
                    <el-input :model-value="policyForm.customerName" readonly placeholder="点击选择客户，留空=全部客户" @click="openPolicyCustomerPicker">
                      <template v-if="policyForm.customerId" #append><el-button icon="CircleClose" @click.stop="clearPolicyCustomer" /></template>
                      <template v-else #append><el-button icon="Search" @click="openPolicyCustomerPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="考核口径" prop="basis">
                    <el-radio-group v-model="policyForm.basis">
                      <el-radio value="1">按金额</el-radio>
                      <el-radio value="2">按数量</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="周期类型" prop="periodType">
                    <el-radio-group v-model="policyForm.periodType">
                      <el-radio value="1">月</el-radio>
                      <el-radio value="2">季</el-radio>
                      <el-radio value="3">年</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="生效日期" prop="startDate">
                    <el-date-picker v-model="policyForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择生效日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="失效日期" prop="endDate">
                    <el-date-picker v-model="policyForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择失效日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="policyForm.status">
                      <el-radio value="0">启用</el-radio>
                      <el-radio value="1">停用</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg></span>阶梯明细</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <div class="tier-bar">
                <el-button type="primary" plain icon="Plus" size="small" @click="addTier">添加阶梯</el-button>
                <span class="form-tip">阶梯上限留空表示不封顶；各档区间不可重叠</span>
              </div>
              <el-table :data="policyForm.tiers" border size="small">
                <el-table-column type="index" label="序号" width="55" align="center" />
                <el-table-column label="阶梯下限（含）" min-width="150">
                  <template #default="{ row }">
                    <el-input-number v-model="row.tierMin" :min="0" :precision="2" controls-position="right" style="width: 100%" placeholder="必填" />
                  </template>
                </el-table-column>
                <el-table-column label="阶梯上限（含）" min-width="150">
                  <template #default="{ row }">
                    <el-input-number v-model="row.tierMax" :min="0" :precision="2" controls-position="right" style="width: 100%" placeholder="不封顶" />
                  </template>
                </el-table-column>
                <el-table-column label="返利比例%" min-width="130">
                  <template #default="{ row }">
                    <el-input-number v-model="row.rebateRate" :min="0" :max="100" :precision="2" controls-position="right" style="width: 100%" placeholder="返利比例" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="70" align="center">
                  <template #default="{ $index }">
                    <el-button link type="danger" icon="Delete" @click="removeTier($index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitPolicy">确 定</el-button>
          <el-button @click="cancelPolicy">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 核算返利弹框 ===== -->
    <el-dialog v-model="settleOpen" width="560px" append-to-body draggable class="rd-dialog" @closed="cancelSettle">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 6v12"/><path d="M15 9.5c0-.83-1.34-1.5-3-1.5s-3 .67-3 1.5 1.34 1.5 3 1.5 3 .67 3 1.5-1.34 1.5-3 1.5-3-.67-3-1.5"/></svg></div>
          <span class="rd-detail-header-title">核算返利</span>
        </div>
      </template>
      <el-form ref="settleRef" :model="settleForm" :rules="settleRules" label-width="100px">
        <el-form-item label="客户" prop="customerId">
          <el-input :model-value="settleForm.customerName" readonly placeholder="点击选择客户" @click="openSettleCustomerPicker">
            <template v-if="settleForm.customerId" #append><el-button icon="CircleClose" @click.stop="clearSettleCustomer" /></template>
            <template v-else #append><el-button icon="Search" @click="openSettleCustomerPicker" /></template>
          </el-input>
        </el-form-item>
        <el-form-item label="核算期间" prop="period">
          <el-date-picker v-model="settleForm.period" type="month" value-format="YYYYMM" placeholder="选择核算期间" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitSettle">开始核算</el-button>
          <el-button @click="cancelSettle">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 核算结果弹框 ===== -->
    <el-dialog v-model="resultOpen" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><polyline points="9 15 11 17 15 13"/></svg></div>
          <span class="rd-detail-header-title">核算结果</span>
        </div>
      </template>
      <div class="form-tip" style="margin-bottom: 10px">本次核算生成结算单 <b>{{ settleResultList.length }}</b> 张</div>
      <el-table :data="settleResultList" border size="small" max-height="380">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="结算单号" prop="settleNo" width="160">
          <template #default="scope"><span class="mono">{{ scope.row.settleNo }}</span></template>
        </el-table-column>
        <el-table-column label="政策名称" prop="policyName" min-width="140" show-overflow-tooltip />
        <el-table-column label="达成额" prop="achieveAmount" width="120" align="right">
          <template #default="scope"><span class="mono">{{ scope.row.achieveAmount }}</span></template>
        </el-table-column>
        <el-table-column label="比例" prop="rebateRate" width="80" align="center">
          <template #default="scope"><span class="mono">{{ scope.row.rebateRate }}%</span></template>
        </el-table-column>
        <el-table-column label="返利额" prop="rebateAmount" width="110" align="right">
          <template #default="scope"><span class="mono">{{ scope.row.rebateAmount }}</span></template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resultOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 客户选择器 -->
    <customer-picker ref="customerPickerRef" title="选择客户" @confirm="onCustomerPickerConfirm" />
  </div>
</template>

<script setup name="MkRebate">
import { listPolicyGroups, addPolicyGroup, updatePolicyGroup, delPolicyGroup, listSettles, settleRebate, redeemSettle } from "@/api/mk/rebate"
import CustomerPicker from '@/components/CustomerPicker/index.vue'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
const { collapsedCards, toggleCard } = useDetailCard(['c1', 'c2'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('rebate_columns')
const { mk_rebate_basis, mk_rebate_period, mk_rebate_settle_status } = proxy.useDict("mk_rebate_basis", "mk_rebate_period", "mk_rebate_settle_status")

const activeTab = ref('policy')
const customerPickerRef = ref(null)
const pickerTarget = ref('policy')

// ===== 返利政策 =====
const policyList = ref([])
const policyLoading = ref(true)
const policyShowSearch = ref(true)
const policyIds = ref([])
const policyTotal = ref(0)
const policyOpen = ref(false)
const policyTitle = ref("")
// 列显隐配置（RightToolbar 按 storageKey 自动恢复/持久化）
const policyColumns = ref({
  policyNo: { label: '政策编号', visible: true },
  policyName: { label: '政策名称', visible: true },
  customerName: { label: '客户名称', visible: true },
  basis: { label: '考核口径', visible: true },
  periodType: { label: '周期类型', visible: true },
  tierCount: { label: '阶梯数', visible: true },
  startDate: { label: '生效日期', visible: true },
  endDate: { label: '失效日期', visible: true },
  status: { label: '状态', visible: true }
})

// ===== 返利结算 =====
const settleList = ref([])
const settleLoading = ref(true)
const settleShowSearch = ref(true)
const settleTotal = ref(0)
const settleOpen = ref(false)
const resultOpen = ref(false)
const settleResultList = ref([])
const settleColumns = ref({
  settleNo: { label: '结算单号', visible: true },
  customerName: { label: '客户名称', visible: true },
  period: { label: '期间', visible: true },
  policyNo: { label: '政策编号', visible: true },
  policyName: { label: '政策名称', visible: true },
  achieveAmount: { label: '达成额', visible: true },
  rebateRate: { label: '比例', visible: true },
  rebateAmount: { label: '返利额', visible: true },
  status: { label: '状态', visible: true },
  redeemType: { label: '兑现方式', visible: true },
  redeemDate: { label: '兑现时间', visible: true }
})

const data = reactive({
  policyQuery: { pageNum: 1, pageSize: 10, customerName: undefined, status: undefined },
  settleQuery: { pageNum: 1, pageSize: 10, customerName: undefined, period: undefined, status: undefined },
  policyForm: {},
  settleForm: {}
})
const { policyQuery, settleQuery, policyForm, settleForm } = toRefs(data)

const policyRules = {
  policyName: [{ required: true, message: "政策名称不能为空", trigger: "blur" }],
  basis: [{ required: true, message: "请选择考核口径", trigger: "change" }],
  periodType: [{ required: true, message: "请选择周期类型", trigger: "change" }],
  startDate: [{ required: true, message: "生效日期不能为空", trigger: "change" }],
  endDate: [{ required: true, message: "失效日期不能为空", trigger: "change" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }]
}
const settleRules = {
  customerId: [{ required: true, message: "请选择客户", trigger: "change" }],
  period: [{ required: true, message: "核算期间不能为空", trigger: "change" }]
}

const policyFilterCount = computed(() => (policyQuery.value.customerName ? 1 : 0) + (policyQuery.value.status ? 1 : 0))
const settleFilterCount = computed(() => (settleQuery.value.customerName ? 1 : 0) + (settleQuery.value.period ? 1 : 0) + (settleQuery.value.status ? 1 : 0))

// ==================== 返利政策 ====================
function getPolicyList() {
  policyLoading.value = true
  listPolicyGroups(policyQuery.value).then(response => {
    policyList.value = response.rows
    policyTotal.value = response.total
    policyLoading.value = false
    applySavedWidths()
  })
}

function handlePolicyQuery() {
  policyQuery.value.pageNum = 1
  getPolicyList()
}

function resetPolicyQuery() {
  policyQuery.value.customerName = undefined
  policyQuery.value.status = undefined
  handlePolicyQuery()
}

function handlePolicySelectionChange(selection) {
  policyIds.value = selection.map(item => item.policyNo)
}

/** 表单重置 */
function resetPolicy() {
  data.policyForm = {
    policyNo: null, policyName: undefined, customerId: null, customerName: undefined,
    basis: '1', periodType: '1', startDate: undefined, endDate: undefined, status: '0',
    tiers: [{ tierMin: undefined, tierMax: undefined, rebateRate: undefined }]
  }
  proxy.resetForm("policyRef")
}

function handleAdd() {
  resetPolicy()
  policyTitle.value = "新增返利政策"
  policyOpen.value = true
}

function handleUpdate(row) {
  let target = row
  if (!target) {
    if (policyIds.value.length === 0) {
      proxy.$modal.msgWarning("请选择一条政策记录")
      return
    }
    if (policyIds.value.length > 1) {
      proxy.$modal.msgWarning("只能选择一条政策记录进行修改")
      return
    }
    target = policyList.value.find(g => g.policyNo === policyIds.value[0])
  }
  if (!target) return
  resetPolicy()
  data.policyForm = {
    policyNo: target.policyNo,
    policyName: target.policyName,
    customerId: target.customerId,
    customerName: target.customerName,
    basis: target.basis || '1',
    periodType: target.periodType || '1',
    startDate: target.startDate,
    endDate: target.endDate,
    status: target.status || '0',
    tiers: (target.tiers || []).map(t => ({ tierMin: t.tierMin, tierMax: t.tierMax, rebateRate: t.rebateRate }))
  }
  policyTitle.value = "修改返利政策"
  policyOpen.value = true
}

function cancelPolicy() {
  policyOpen.value = false
  resetPolicy()
}

function handleDelete(row) {
  const nos = row ? [row.policyNo] : policyIds.value
  if (!nos || nos.length === 0) {
    proxy.$modal.msgWarning("请选择要删除的政策")
    return
  }
  proxy.$modal.confirm('是否确认删除政策编号为"' + nos.join('、') + '"的返利政策（整组）？').then(() => {
    return Promise.all(nos.map(no => delPolicyGroup(no)))
  }).then(() => {
    getPolicyList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 阶梯行操作 */
function addTier() {
  policyForm.value.tiers.push({ tierMin: undefined, tierMax: undefined, rebateRate: undefined })
}
function removeTier(index) {
  policyForm.value.tiers.splice(index, 1)
}

/** 阶梯校验：min<=max、区间不重叠 */
function validateTiers() {
  const tiers = policyForm.value.tiers || []
  if (tiers.length === 0) {
    proxy.$modal.msgWarning("请至少添加一条返利阶梯")
    return false
  }
  for (let i = 0; i < tiers.length; i++) {
    const t = tiers[i]
    if (t.tierMin == null) {
      proxy.$modal.msgWarning("第" + (i + 1) + "行阶梯下限不能为空")
      return false
    }
    if (t.rebateRate == null) {
      proxy.$modal.msgWarning("第" + (i + 1) + "行返利比例不能为空")
      return false
    }
    if (t.tierMax != null && Number(t.tierMin) > Number(t.tierMax)) {
      proxy.$modal.msgWarning("第" + (i + 1) + "行阶梯下限不能大于上限")
      return false
    }
  }
  const sorted = [...tiers].sort((a, b) => Number(a.tierMin) - Number(b.tierMin))
  for (let i = 1; i < sorted.length; i++) {
    const prev = sorted[i - 1]
    const curr = sorted[i]
    if (prev.tierMax == null || Number(curr.tierMin) <= Number(prev.tierMax)) {
      proxy.$modal.msgWarning("阶梯区间存在重叠，请调整各档上下限")
      return false
    }
  }
  return true
}

function submitPolicy() {
  proxy.$refs["policyRef"].validate(valid => {
    if (!valid) return
    if (!validateTiers()) return
    const head = policyForm.value
    const payload = head.tiers.map(t => ({
      policyNo: head.policyNo || undefined,
      policyName: head.policyName,
      customerId: head.customerId,
      customerName: head.customerName,
      basis: head.basis,
      periodType: head.periodType,
      tierMin: t.tierMin,
      tierMax: t.tierMax == null ? null : t.tierMax,
      rebateRate: t.rebateRate,
      startDate: head.startDate,
      endDate: head.endDate,
      status: head.status
    }))
    const request = head.policyNo != null ? updatePolicyGroup(payload) : addPolicyGroup(payload)
    request.then(() => {
      proxy.$modal.msgSuccess(head.policyNo != null ? "修改成功" : "新增成功")
      policyOpen.value = false
      getPolicyList()
    })
  })
}

// ==================== 返利结算 ====================
function getSettleList() {
  settleLoading.value = true
  listSettles(settleQuery.value).then(response => {
    settleList.value = response.rows
    settleTotal.value = response.total
    settleLoading.value = false
    applySavedWidths()
  })
}

function handleSettleQuery() {
  settleQuery.value.pageNum = 1
  getSettleList()
}

function resetSettleQuery() {
  settleQuery.value.customerName = undefined
  settleQuery.value.period = undefined
  settleQuery.value.status = undefined
  handleSettleQuery()
}

/** 核算返利 */
function handleSettle() {
  data.settleForm = { customerId: null, customerName: undefined, period: undefined }
  proxy.resetForm("settleRef")
  settleOpen.value = true
}

function cancelSettle() {
  settleOpen.value = false
  data.settleForm = { customerId: null, customerName: undefined, period: undefined }
  proxy.resetForm("settleRef")
}

function submitSettle() {
  proxy.$refs["settleRef"].validate(valid => {
    if (!valid) return
    settleRebate({ customerId: settleForm.value.customerId, period: settleForm.value.period }).then(res => {
      settleOpen.value = false
      proxy.$modal.msgSuccess(res.msg || "核算完成")
      settleResultList.value = res.data || []
      resultOpen.value = true
      getSettleList()
    })
  })
}

/** 兑现（仅待兑现） */
function handleRedeem(row) {
  proxy.$prompt('请输入兑现方式（如：抵货款/现金）', '返利兑现', {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    inputPlaceholder: "兑现方式（必填）",
    inputPattern: /\S+/,
    inputErrorMessage: "兑现方式不能为空"
  }).then(({ value }) => {
    return redeemSettle(row.settleId, value.trim())
  }).then(() => {
    proxy.$modal.msgSuccess("兑现成功")
    getSettleList()
  }).catch(() => {})
}

// ==================== 客户选择器 ====================
function openPolicyCustomerPicker() {
  pickerTarget.value = 'policy'
  customerPickerRef.value.open(policyForm.value.customerId || undefined)
}
function openSettleCustomerPicker() {
  pickerTarget.value = 'settle'
  customerPickerRef.value.open(settleForm.value.customerId || undefined)
}
function clearPolicyCustomer() {
  policyForm.value.customerId = null
  policyForm.value.customerName = undefined
}
function clearSettleCustomer() {
  settleForm.value.customerId = null
  settleForm.value.customerName = undefined
}
function onCustomerPickerConfirm(customer) {
  if (pickerTarget.value === 'settle') {
    settleForm.value.customerId = customer.customerId
    settleForm.value.customerName = customer.customerName
  } else {
    policyForm.value.customerId = customer.customerId
    policyForm.value.customerName = customer.customerName
  }
}

/** 工具函数 */
function dictLabel(options, v) {
  if (v == null || v === "") return "-"
  const item = (options || []).find(d => d.value == v)
  return item ? item.label : v
}
function periodBadgeClass(v) {
  if (v === '2') return 'green'
  if (v === '3') return 'amber'
  return 'blue'
}

onMounted(() => {
  getPolicyList()
  getSettleList()
})
</script>

<style scoped>
.rebate-tabs :deep(.el-tabs__header) { margin-bottom: 12px; }
.tier-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.form-tip { font-size: 12px; color: #909399; line-height: 1.6; }
.mono { font-family: "JetBrains Mono", Consolas, monospace; font-size: 12px; }
</style>

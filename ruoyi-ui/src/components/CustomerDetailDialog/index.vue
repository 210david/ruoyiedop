<template>
  <el-dialog v-model="visible" width="900px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
        <span class="rd-detail-header-title">客户详情</span>
        <div class="rd-detail-header-sub" v-if="viewForm.customerNo">
          <span class="rd-detail-header-divider"></span>
          <span class="rd-detail-header-no">编号：{{ viewForm.customerNo }}</span>
          <dict-tag :options="marketing_customer_status" :value="viewForm.customerStatus" />
        </div>
      </div>
    </template>
    <div class="rd-page">
      <el-tabs v-model="detailTab">
        <el-tab-pane label="基本信息" name="basic">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cd_basic')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>客户信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cd_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cd_basic">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">客户编号</span><div class="rd-value">{{ viewForm.customerNo }}</div></div>
                <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ viewForm.customerName }}</div></div>
                <div class="rd-item"><span class="rd-label">信用代码</span><div class="rd-value">{{ viewForm.creditCode }}</div></div>
                <div class="rd-item"><span class="rd-label">客户等级</span><div class="rd-value"><dict-tag :options="marketing_customer_level" :value="viewForm.customerLevel" /></div></div>
                <div class="rd-item"><span class="rd-label">客户来源</span><div class="rd-value"><dict-tag :options="marketing_customer_source" :value="viewForm.customerSource" /></div></div>
                <div class="rd-item"><span class="rd-label">客户状态</span><div class="rd-value"><dict-tag :options="marketing_customer_status" :value="viewForm.customerStatus" /></div></div>
                <div class="rd-item rd-item--full"><span class="rd-label">客户标签</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.tagNames }">{{ viewForm.tagNames || '暂无' }}</div></div>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cd_company')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/></svg></span>企业信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cd_company }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cd_company">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">所属行业</span><div class="rd-value"><dict-tag :options="marketing_industry" :value="viewForm.industry" /></div></div>
                <div class="rd-item"><span class="rd-label">企业规模</span><div class="rd-value"><dict-tag :options="marketing_company_size" :value="viewForm.companySize" /></div></div>
                <div class="rd-item"><span class="rd-label">官网</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.website }">{{ viewForm.website || '暂无' }}</div></div>
                <div class="rd-item rd-item--full"><span class="rd-label">详细地址</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.address }">{{ viewForm.address || '暂无' }}</div></div>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cd_business')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg></span>业务信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cd_business }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cd_business">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">首次接触</span><div class="rd-value">{{ viewForm.firstContactDate }}</div></div>
                <div class="rd-item"><span class="rd-label">合作开始</span><div class="rd-value">{{ viewForm.cooperationDate }}</div></div>
                <div class="rd-item"><span class="rd-label">累计交易额</span><div class="rd-value rd-value--large rd-amount" :class="{ 'rd-amount--negative': viewForm.totalAmount > 0 }">￥{{ formatAmount(viewForm.totalAmount) }}</div></div>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cd_owner')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>负责信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cd_owner }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cd_owner">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ viewForm.userName || '未分配（公海）' }}</div></div>
                <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.deptName }">{{ viewForm.deptName || '暂无' }}</div></div>
                <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime }}</div></div>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cd_other')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cd_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cd_other">
              <div class="rd-grid">
                <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无' }}</div></div>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cd_action')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg></span>状态与分配</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cd_action }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cd_action">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">当前状态</span><div class="rd-value"><dict-tag :options="marketing_customer_status" :value="viewForm.customerStatus" /></div></div>
                <div class="rd-item rd-item--full"><span class="rd-label">状态操作</span><div class="rd-value"><el-button size="small" type="primary" plain @click="handleStatusChange('1')" v-if="viewForm.customerStatus === '0'" v-hasPermi="['marketing:customer:edit']">转为签约</el-button>
                  <el-button size="small" type="success" plain @click="handleStatusChange('2')" v-if="viewForm.customerStatus === '1'" v-hasPermi="['marketing:customer:edit']">转为合作中</el-button>
                  <el-button size="small" type="warning" plain @click="handleStatusChange('3')" v-if="viewForm.customerStatus === '2'" v-hasPermi="['marketing:customer:edit']">暂停合作</el-button>
                  <el-button size="small" type="success" plain @click="handleStatusChange('2')" v-if="viewForm.customerStatus === '3'" v-hasPermi="['marketing:customer:edit']">恢复合作</el-button>
                  <el-button size="small" type="danger" plain @click="handleStatusChange('4')" v-if="['0','1','2','3'].includes(viewForm.customerStatus)" v-hasPermi="['marketing:customer:edit']">标记流失</el-button>
                  <el-button size="small" type="primary" plain @click="handleStatusChange('0')" v-if="viewForm.customerStatus === '4'" v-hasPermi="['marketing:customer:edit']">重新激活</el-button></div></div>
                <div class="rd-item rd-item--full"><span class="rd-label">分配操作</span><div class="rd-value"><el-button size="small" type="primary" plain @click="handleAssign" v-if="viewForm.userId" v-hasPermi="['marketing:customer:assign']">转移分配</el-button>
                  <el-button size="small" type="warning" plain @click="handleRelease" v-if="viewForm.userId" v-hasPermi="['marketing:customer:edit']">释放到公海</el-button>
                  <el-button size="small" type="success" plain @click="handleClaim" v-if="!viewForm.userId" v-hasPermi="['marketing:customer:claim']">领取客户</el-button></div></div>
              </div>
            </div>
          </section>
        </el-tab-pane>
        <el-tab-pane :label="`联系人 (${contacts.length})`" name="contacts">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cd_contacts')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>联系人</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cd_contacts }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cd_contacts">
              <el-table :data="contacts" border size="small" v-if="contacts.length > 0">
                <el-table-column label="姓名" prop="name" width="100" />
                <el-table-column label="职位" prop="position" width="120" show-overflow-tooltip />
                <el-table-column label="手机号" prop="phone" width="130" />
                <el-table-column label="邮箱" prop="email" show-overflow-tooltip />
                <el-table-column label="关键联系人" prop="isKey" width="100" align="center"><template #default="scope"><el-tag :type="scope.row.isKey === '1' ? 'danger' : 'info'" size="small">{{ scope.row.isKey === '1' ? '是' : '否' }}</el-tag></template></el-table-column>
                <el-table-column label="主要联系人" prop="isPrimary" width="100" align="center"><template #default="scope"><el-tag :type="scope.row.isPrimary === '1' ? 'success' : 'info'" size="small">{{ scope.row.isPrimary === '1' ? '是' : '否' }}</el-tag></template></el-table-column>
              </el-table>
              <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg><p class="rd-empty-text">暂无联系人</p></div>
            </div>
          </section>
        </el-tab-pane>
        <el-tab-pane :label="`商机 (${opportunities.length})`" name="opportunities">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cd_opportunities')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg></span>商机</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cd_opportunities }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cd_opportunities">
              <el-table :data="opportunities" border size="small" v-if="opportunities.length > 0">
                <el-table-column label="商机编号" prop="opportunityNo" width="150" />
                <el-table-column label="商机名称" prop="opportunityName" show-overflow-tooltip />
                <el-table-column label="销售阶段" prop="stageName" width="100" align="center" />
                <el-table-column label="预计金额" prop="expectedAmount" width="120" align="center"><template #default="scope">￥{{ formatAmount(scope.row.expectedAmount) }}</template></el-table-column>
                <el-table-column label="商机状态" prop="opportunityStatus" width="100" align="center"><template #default="scope"><dict-tag :options="marketing_opportunity_status" :value="scope.row.opportunityStatus" /></template></el-table-column>
              </el-table>
              <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/></svg><p class="rd-empty-text">暂无商机</p></div>
            </div>
          </section>
        </el-tab-pane>
        <el-tab-pane :label="`合同 (${contracts.length})`" name="contracts">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cd_contracts')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>合同</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cd_contracts }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cd_contracts">
              <el-table :data="contracts" border size="small" v-if="contracts.length > 0">
                <el-table-column label="合同编号" prop="contractNo" width="150" />
                <el-table-column label="合同名称" prop="contractName" show-overflow-tooltip />
                <el-table-column label="合同金额" prop="contractAmount" width="120" align="center"><template #default="scope">￥{{ formatAmount(scope.row.contractAmount) }}</template></el-table-column>
                <el-table-column label="签订日期" prop="signDate" width="120" />
                <el-table-column label="合同状态" prop="contractStatus" width="100" align="center"><template #default="scope"><dict-tag :options="marketing_contract_status" :value="scope.row.contractStatus" /></template></el-table-column>
              </el-table>
              <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg><p class="rd-empty-text">暂无合同</p></div>
            </div>
          </section>
        </el-tab-pane>
        <el-tab-pane :label="`订单 (${orders.length})`" name="orders">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cd_orders')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg></span>订单</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cd_orders }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cd_orders">
              <el-table :data="orders" border size="small" v-if="orders.length > 0">
                <el-table-column label="订单编号" prop="orderNo" width="150" />
                <el-table-column label="订单名称" prop="orderName" show-overflow-tooltip />
                <el-table-column label="订单金额" prop="orderAmount" width="120" align="center"><template #default="scope">￥{{ formatAmount(scope.row.orderAmount) }}</template></el-table-column>
                <el-table-column label="下单日期" prop="orderDate" width="120" />
                <el-table-column label="订单状态" prop="orderStatus" width="100" align="center"><template #default="scope"><dict-tag :options="marketing_order_status" :value="scope.row.orderStatus" /></template></el-table-column>
              </el-table>
              <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg><p class="rd-empty-text">暂无订单</p></div>
            </div>
          </section>
        </el-tab-pane>
        <el-tab-pane :label="`互动记录 (${interactions.length})`" name="interactions">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cd_interactions')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg></span>互动记录</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cd_interactions }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cd_interactions">
              <div class="rd-timeline" v-if="interactions.length > 0">
                <div class="rd-timeline-item" v-for="item in interactions" :key="item.recordId">
                  <div class="rd-timeline-dot rd-timeline-dot--success"></div>
                  <div class="rd-timeline-content">
                    <div class="rd-timeline-header">
                      <span class="rd-timeline-title"><dict-tag :options="marketing_interaction_type" :value="item.interactType" /></span>
                      <span class="rd-timeline-time">{{ item.interactTime }}</span>
                    </div>
                    <div class="rd-timeline-comment">{{ item.content }}</div>
                    <div v-if="item.nextTime" style="margin-top: 6px; font-size: 12px; color: #e6a23c">下次跟进: {{ item.nextTime }} {{ item.nextContent ? '- ' + item.nextContent : '' }}</div>
                  </div>
                </div>
              </div>
              <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg><p class="rd-empty-text">暂无互动记录</p></div>
            </div>
          </section>
        </el-tab-pane>
        <el-tab-pane :label="`参与活动 (${activities.length})`" name="activities">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cd_activities')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>参与活动</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cd_activities }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cd_activities">
              <el-table :data="activities" border size="small" v-if="activities.length > 0">
                <el-table-column label="活动名称" prop="activityName" show-overflow-tooltip />
                <el-table-column label="联系人" prop="contactName" width="100" />
                <el-table-column label="企业名称" prop="companyName" width="180" show-overflow-tooltip />
                <el-table-column label="参与状态" prop="participateStatus" width="100" align="center"><template #default="scope"><dict-tag :options="marketing_participate_status" :value="scope.row.participateStatus" /></template></el-table-column>
                <el-table-column label="签到时间" prop="signTime" width="160" />
              </el-table>
              <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/></svg><p class="rd-empty-text">暂无活动参与记录</p></div>
            </div>
          </section>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-dialog>
</template>

<script setup>
import { getCustomer, changeCustomerStatus, assignCustomer, releaseToPool, claimCustomer } from '@/api/mk/customer'
import { listContact } from '@/api/mk/contact'
import { listOpportunity } from '@/api/mk/opportunity'
import { listContract } from '@/api/mk/contract'
import { listOrder } from '@/api/mk/order'
import { listInteraction } from '@/api/mk/interaction'
import { listParticipant } from '@/api/mk/participant'
import { listUser } from '@/api/system/user'
import { useDetailCard } from '@/composables/useDetailCard'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  customerId: { type: [Number, String], default: null }
})
const emit = defineEmits(['update:modelValue'])

const { proxy } = getCurrentInstance()
const { collapsedCards, toggleCard } = useDetailCard(["cd_basic", "cd_company", "cd_business", "cd_owner", "cd_other", "cd_action", "cd_contacts", "cd_opportunities", "cd_contracts", "cd_orders", "cd_interactions", "cd_activities"])
const { marketing_customer_level, marketing_customer_source, marketing_customer_status, marketing_industry, marketing_company_size, marketing_opportunity_status, marketing_contract_status, marketing_order_status, marketing_interaction_type, marketing_participate_status } = proxy.useDict('marketing_customer_level', 'marketing_customer_source', 'marketing_customer_status', 'marketing_industry', 'marketing_company_size', 'marketing_opportunity_status', 'marketing_contract_status', 'marketing_order_status', 'marketing_interaction_type', 'marketing_participate_status')

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})
const detailTab = ref('basic')
const viewForm = ref({})
const contacts = ref([])
const opportunities = ref([])
const contracts = ref([])
const orders = ref([])
const interactions = ref([])
const activities = ref([])
const userOptions = ref([])

function formatAmount(val) {
  if (val == null || val === '') return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function statusLabel(val) {
  return { '0': '意向客户', '1': '签约客户', '2': '合作中', '3': '暂停合作', '4': '流失客户' }[val] || ''
}

function onOpen() {
  if (!props.customerId) return
  detailTab.value = 'basic'
  contacts.value = []; opportunities.value = []; contracts.value = []; orders.value = []; interactions.value = []; activities.value = []
  Object.keys(collapsedCards).forEach(k => { collapsedCards[k] = false })
  getCustomer(props.customerId).then(res => { viewForm.value = res.data })
  loadRelations()
  listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) })
}

function loadRelations() {
  const cid = props.customerId
  if (!cid) return
  listContact({ customerId: cid, pageNum: 1, pageSize: 999 }).then(res => { contacts.value = res.rows || [] })
  listOpportunity({ customerId: cid, pageNum: 1, pageSize: 999 }).then(res => { opportunities.value = res.rows || [] })
  listContract({ customerId: cid, pageNum: 1, pageSize: 999 }).then(res => { contracts.value = res.rows || [] })
  listOrder({ customerId: cid, pageNum: 1, pageSize: 999 }).then(res => { orders.value = res.rows || [] })
  listInteraction({ customerId: cid, pageNum: 1, pageSize: 999 }).then(res => { interactions.value = res.rows || [] })
  listParticipant({ customerId: cid, pageNum: 1, pageSize: 999 }).then(res => { activities.value = res.rows || [] })
}

function refreshView() {
  getCustomer(props.customerId).then(res => { viewForm.value = res.data })
}

function handleStatusChange(status) {
  proxy.$modal.confirm('确认将客户状态变更为"' + statusLabel(status) + '"？').then(() => {
    changeCustomerStatus(props.customerId, status).then(() => {
      proxy.$modal.msgSuccess('状态变更成功')
      refreshView()
    })
  }).catch(() => {})
}

function handleAssign() {
  const userId = viewForm.value.userId || null
  proxy.$modal.confirm('确认转移分配该客户？').then(() => {
    // 简化：直接弹出选择
  }).catch(() => {})
  // 使用简单的 prompt 方式
  if (userOptions.value.length === 0) {
    listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) })
  }
}

function handleRelease() {
  proxy.$modal.confirm('确认将该客户释放到公海？释放后其他销售人员可以领取。').then(() => {
    releaseToPool(props.customerId).then(() => {
      proxy.$modal.msgSuccess('已释放到公海')
      refreshView()
    })
  }).catch(() => {})
}

function handleClaim() {
  proxy.$modal.confirm('确认领取该客户？领取后您将成为该客户的负责人。').then(() => {
    claimCustomer(props.customerId).then(() => {
      proxy.$modal.msgSuccess('领取成功')
      refreshView()
    })
  }).catch(() => {})
}
</script>

<template>
  <div class="app-container">
    <div class="detail-header-bar mb16">
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
        <span class="rd-detail-header-title">{{ customer.customerName || '客户详情' }}</span>
        <div class="rd-detail-header-sub" v-if="customer.customerNo">
          <div class="rd-detail-header-divider"></div>
          <span class="rd-detail-header-no">编号：{{ customer.customerNo }}</span>
          <el-tag v-if="customer.customerLevel" :type="levelTagType(customer.customerLevel)" size="small" effect="dark">{{ levelLabel(customer.customerLevel) }}</el-tag>
          <el-tag v-if="customer.customerStatus" :type="statusTagType(customer.customerStatus)" size="small" effect="dark">{{ statusLabel(customer.customerStatus) }}</el-tag>
          <el-tag v-if="!customer.userId" type="warning" size="small" effect="dark">公海客户</el-tag>
        </div>
      </div>
      <div class="detail-header-actions">
        <el-button type="primary" @click="handleEdit" v-hasPermi="['marketing:customer:edit']">编辑</el-button>
        <el-button @click="goBack">返回列表</el-button>
      </div>
    </div>

    <div class="rd-page" v-loading="loading">
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d_basic')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>客户信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d_basic">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">客户编号</span><div class="rd-value">{{ customer.customerNo }}</div></div>
                <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ customer.customerName }}</div></div>
                <div class="rd-item"><span class="rd-label">信用代码</span><div class="rd-value">{{ customer.creditCode }}</div></div>
                <div class="rd-item"><span class="rd-label">客户等级</span><div class="rd-value"><dict-tag :options="marketing_customer_level" :value="customer.customerLevel" /></div></div>
                <div class="rd-item"><span class="rd-label">客户来源</span><div class="rd-value"><dict-tag :options="marketing_customer_source" :value="customer.customerSource" /></div></div>
                <div class="rd-item"><span class="rd-label">客户状态</span><div class="rd-value"><dict-tag :options="marketing_customer_status" :value="customer.customerStatus" /></div></div>
                <div class="rd-item rd-item--full"><span class="rd-label">客户标签</span><div class="rd-value" :class="{ 'rd-value--muted': !customer.tagNames }">{{ customer.tagNames || '暂无' }}</div></div>
              </div>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d_company')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/><line x1="9" y1="9" x2="9" y2="9.01"/><line x1="9" y1="12" x2="9" y2="12.01"/><line x1="9" y1="15" x2="9" y2="15.01"/><line x1="9" y1="18" x2="9" y2="18.01"/></svg></span>企业信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d_company }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d_company">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">所属行业</span><div class="rd-value"><dict-tag :options="marketing_industry" :value="customer.industry" /></div></div>
                <div class="rd-item"><span class="rd-label">企业规模</span><div class="rd-value"><dict-tag :options="marketing_company_size" :value="customer.companySize" /></div></div>
                <div class="rd-item"><span class="rd-label">官网</span><div class="rd-value" :class="{ 'rd-value--muted': !customer.website }">{{ customer.website || '暂无' }}</div></div>
                <div class="rd-item rd-item--full"><span class="rd-label">详细地址</span><div class="rd-value" :class="{ 'rd-value--muted': !customer.address }">{{ customer.address || '暂无' }}</div></div>
              </div>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d_business')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg></span>业务信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d_business }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d_business">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">首次接触</span><div class="rd-value">{{ customer.firstContactDate }}</div></div>
                <div class="rd-item"><span class="rd-label">合作开始</span><div class="rd-value">{{ customer.cooperationDate }}</div></div>
                <div class="rd-item"><span class="rd-label">累计交易额</span><div class="rd-value rd-value--large rd-amount" :class="{ 'rd-amount--negative': customer.totalAmount > 0 }">￥{{ formatAmount(customer.totalAmount) }}</div></div>
              </div>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d_owner')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>负责信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d_owner }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d_owner">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ customer.userName || '未分配（公海）' }}</div></div>
                <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value" :class="{ 'rd-value--muted': !customer.deptName }">{{ customer.deptName || '暂无' }}</div></div>
                <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ customer.createTime }}</div></div>
              </div>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d_remark')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>补充信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d_remark }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d_remark">
              <div class="rd-grid">
                <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !customer.remark }">{{ customer.remark || '暂无' }}</div></div>
              </div>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d_action')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg></span>状态与分配</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d_action }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d_action">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">当前状态</span><div class="rd-value"><dict-tag :options="marketing_customer_status" :value="customer.customerStatus" /></div></div>
                <div class="rd-item rd-item--full"><span class="rd-label">状态操作</span><div class="rd-value"><el-button size="small" type="primary" plain @click="handleStatusChange('1')" v-if="customer.customerStatus === '0'" v-hasPermi="['marketing:customer:edit']">转为签约</el-button>
                  <el-button size="small" type="success" plain @click="handleStatusChange('2')" v-if="customer.customerStatus === '1'" v-hasPermi="['marketing:customer:edit']">转为合作中</el-button>
                  <el-button size="small" type="warning" plain @click="handleStatusChange('3')" v-if="customer.customerStatus === '2'" v-hasPermi="['marketing:customer:edit']">暂停合作</el-button>
                  <el-button size="small" type="success" plain @click="handleStatusChange('2')" v-if="customer.customerStatus === '3'" v-hasPermi="['marketing:customer:edit']">恢复合作</el-button>
                  <el-button size="small" type="danger" plain @click="handleStatusChange('4')" v-if="['0','1','2','3'].includes(customer.customerStatus)" v-hasPermi="['marketing:customer:edit']">标记流失</el-button>
                  <el-button size="small" type="primary" plain @click="handleStatusChange('0')" v-if="customer.customerStatus === '4'" v-hasPermi="['marketing:customer:edit']">重新激活</el-button></div></div>
                <div class="rd-item rd-item--full"><span class="rd-label">分配操作</span><div class="rd-value"><el-button size="small" type="primary" plain @click="handleAssign" v-if="customer.userId" v-hasPermi="['marketing:customer:assign']">转移分配</el-button>
                  <el-button size="small" type="warning" plain @click="handleRelease" v-if="customer.userId" v-hasPermi="['marketing:customer:edit']">释放到公海</el-button>
                  <el-button size="small" type="success" plain @click="handleClaim" v-if="!customer.userId" v-hasPermi="['marketing:customer:claim']">领取客户</el-button></div></div>
              </div>
            </div>
          </section>
        </el-tab-pane>

        <!-- 联系人列表 -->
        <el-tab-pane name="contacts">
          <template #label>联系人<el-badge :value="contacts.length" :hidden="contacts.length === 0" class="ml5" /></template>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d_contacts')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>联系人</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d_contacts }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d_contacts">
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

        <!-- 商机列表 -->
        <el-tab-pane name="opportunities">
          <template #label>商机<el-badge :value="opportunities.length" :hidden="opportunities.length === 0" class="ml5" /></template>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d_opportunities')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg></span>商机</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d_opportunities }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d_opportunities">
              <el-table :data="opportunities" border size="small" v-if="opportunities.length > 0">
                <el-table-column label="商机编号" prop="opportunityNo" width="150" />
                <el-table-column label="商机名称" prop="opportunityName" show-overflow-tooltip />
                <el-table-column label="销售阶段" prop="stageName" width="100" align="center" />
                <el-table-column label="预计金额" prop="expectedAmount" width="120" align="right"><template #default="scope">￥{{ formatAmount(scope.row.expectedAmount) }}</template></el-table-column>
                <el-table-column label="赢率" prop="winRate" width="80" align="center"><template #default="scope">{{ scope.row.winRate }}%</template></el-table-column>
                <el-table-column label="商机状态" prop="opportunityStatus" width="100" align="center"><template #default="scope"><dict-tag :options="marketing_opportunity_status" :value="scope.row.opportunityStatus" /></template></el-table-column>
              </el-table>
              <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/></svg><p class="rd-empty-text">暂无商机</p></div>
            </div>
          </section>
        </el-tab-pane>

        <!-- 合同列表 -->
        <el-tab-pane name="contracts">
          <template #label>合同<el-badge :value="contracts.length" :hidden="contracts.length === 0" class="ml5" /></template>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d_contracts')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>合同</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d_contracts }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d_contracts">
              <el-table :data="contracts" border size="small" v-if="contracts.length > 0">
                <el-table-column label="合同编号" prop="contractNo" width="150" />
                <el-table-column label="合同名称" prop="contractName" show-overflow-tooltip />
                <el-table-column label="合同金额" prop="contractAmount" width="120" align="right"><template #default="scope">￥{{ formatAmount(scope.row.contractAmount) }}</template></el-table-column>
                <el-table-column label="签订日期" prop="signDate" width="120" />
                <el-table-column label="合同状态" prop="contractStatus" width="100" align="center"><template #default="scope"><dict-tag :options="marketing_contract_status" :value="scope.row.contractStatus" /></template></el-table-column>
              </el-table>
              <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg><p class="rd-empty-text">暂无合同</p></div>
            </div>
          </section>
        </el-tab-pane>

        <!-- 订单列表 -->
        <el-tab-pane name="orders">
          <template #label>订单<el-badge :value="orders.length" :hidden="orders.length === 0" class="ml5" /></template>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d_orders')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg></span>订单</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d_orders }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d_orders">
              <el-table :data="orders" border size="small" v-if="orders.length > 0">
                <el-table-column label="订单编号" prop="orderNo" width="150" />
                <el-table-column label="订单名称" prop="orderName" show-overflow-tooltip />
                <el-table-column label="订单金额" prop="orderAmount" width="120" align="right"><template #default="scope">￥{{ formatAmount(scope.row.orderAmount) }}</template></el-table-column>
                <el-table-column label="下单日期" prop="orderDate" width="120" />
                <el-table-column label="订单状态" prop="orderStatus" width="100" align="center"><template #default="scope"><dict-tag :options="marketing_order_status" :value="scope.row.orderStatus" /></template></el-table-column>
              </el-table>
              <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg><p class="rd-empty-text">暂无订单</p></div>
            </div>
          </section>
        </el-tab-pane>

        <!-- 互动记录 -->
        <el-tab-pane name="interactions">
          <template #label>互动记录<el-badge :value="interactions.length" :hidden="interactions.length === 0" class="ml5" /></template>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d_interactions')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg></span>互动记录</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d_interactions }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d_interactions">
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

        <!-- 参与活动 -->
        <el-tab-pane name="activities">
          <template #label>参与活动<el-badge :value="activities.length" :hidden="activities.length === 0" class="ml5" /></template>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d_activities')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>参与活动</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d_activities }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d_activities">
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

    <!-- 分配弹窗 -->
    <el-dialog v-model="assignOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">分配客户</span>
        </div>
      </template>
      <el-form label-width="80px">
        <el-form-item label="负责人">
          <el-select v-model="assignUserId" filterable clearable placeholder="请选择（留空释放到公海）" style="width: 100%">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmAssign">确 定</el-button>
        <el-button @click="assignOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkCustomerDetail">
import { useRoute, useRouter } from 'vue-router'
import { getCustomer, changeCustomerStatus, assignCustomer, releaseToPool, claimCustomer } from '@/api/mk/customer'
import { listContact } from '@/api/mk/contact'
import { listOpportunity } from '@/api/mk/opportunity'
import { listContract } from '@/api/mk/contract'
import { listOrder } from '@/api/mk/order'
import { listInteraction } from '@/api/mk/interaction'
import { listParticipant } from '@/api/mk/participant'
import { listUser } from '@/api/system/user'
import { useDetailCard } from '@/composables/useDetailCard'

const route = useRoute()
const router = useRouter()
const { collapsedCards, toggleCard } = useDetailCard(["d_basic", "d_company", "d_business", "d_owner", "d_remark", "d_action", "d_contacts", "d_opportunities", "d_contracts", "d_orders", "d_interactions", "d_activities"])
const { proxy } = getCurrentInstance()
const {
  marketing_customer_level, marketing_customer_source, marketing_customer_status,
  marketing_industry, marketing_company_size,
  marketing_opportunity_status, marketing_contract_status, marketing_order_status,
  marketing_interaction_type, marketing_participate_status
} = proxy.useDict(
  'marketing_customer_level', 'marketing_customer_source', 'marketing_customer_status',
  'marketing_industry', 'marketing_company_size',
  'marketing_opportunity_status', 'marketing_contract_status', 'marketing_order_status',
  'marketing_interaction_type', 'marketing_participate_status'
)

const customerId = route.params && route.params.customerId
const loading = ref(true)
const activeTab = ref('basic')
const customer = ref({})
const contacts = ref([])
const opportunities = ref([])
const contracts = ref([])
const orders = ref([])
const interactions = ref([])
const activities = ref([])
const userOptions = ref([])
const assignOpen = ref(false)
const assignUserId = ref(null)

function levelTagType(val) {
  return { '1': 'danger', '2': 'warning', '3': '', '4': 'info' }[val] || ''
}
function levelLabel(val) {
  return { '1': '关键大客户', '2': '重要客户', '3': '普通客户', '4': '潜在客户' }[val] || ''
}
function statusTagType(val) {
  return { '0': 'primary', '1': 'success', '2': 'success', '3': 'warning', '4': 'info' }[val] || ''
}
function statusLabel(val) {
  return { '0': '意向客户', '1': '签约客户', '2': '合作中', '3': '暂停合作', '4': '流失客户' }[val] || ''
}
function formatAmount(val) {
  if (val == null || val === '') return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getCustomerData() {
  loading.value = true
  getCustomer(customerId).then(res => {
    customer.value = res.data
    loading.value = false
  })
}

function getContacts() {
  listContact({ customerId: customerId, pageNum: 1, pageSize: 999 }).then(res => {
    contacts.value = res.rows || []
  })
}

function getOpportunities() {
  listOpportunity({ customerId: customerId, pageNum: 1, pageSize: 999 }).then(res => {
    opportunities.value = res.rows || []
  })
}

function getContracts() {
  listContract({ customerId: customerId, pageNum: 1, pageSize: 999 }).then(res => {
    contracts.value = res.rows || []
  })
}

function getOrders() {
  listOrder({ customerId: customerId, pageNum: 1, pageSize: 999 }).then(res => {
    orders.value = res.rows || []
  })
}

function getInteractions() {
  listInteraction({ customerId: customerId, pageNum: 1, pageSize: 999 }).then(res => {
    interactions.value = res.rows || []
  })
}

function getActivities() {
  listParticipant({ customerId: customerId, pageNum: 1, pageSize: 999 }).then(res => {
    activities.value = res.rows || []
  })
}

function getUserOptions() {
  listUser({ pageNum: 1, pageSize: 9999 }).then(res => {
    userOptions.value = res.rows.filter(u => u.userId !== 1)
  })
}

function goBack() { router.push('/mk/customer/list') }
function handleEdit() { router.push('/mk/customer/list') }

// 状态流转
function handleStatusChange(status) {
  const statusText = statusLabel(status)
  proxy.$modal.confirm('确认将客户状态变更为"' + statusText + '"？').then(() => {
    changeCustomerStatus(customerId, status).then(() => {
      proxy.$modal.msgSuccess('状态变更成功')
      getCustomerData()
    })
  }).catch(() => {})
}

// 分配/转移
function handleAssign() {
  assignUserId.value = customer.value.userId || null
  assignOpen.value = true
}
function confirmAssign() {
  const data = { userId: assignUserId.value }
  const user = userOptions.value.find(u => u.userId === assignUserId.value)
  if (user) {
    data.deptId = user.deptId
    data.userName = user.nickName
  }
  assignCustomer(customerId, data).then(() => {
    proxy.$modal.msgSuccess('分配成功')
    assignOpen.value = false
    getCustomerData()
  })
}

// 释放到公海
function handleRelease() {
  proxy.$modal.confirm('确认将该客户释放到公海？释放后其他销售人员可以领取。').then(() => {
    releaseToPool(customerId).then(() => {
      proxy.$modal.msgSuccess('已释放到公海')
      getCustomerData()
    })
  }).catch(() => {})
}

// 领取客户
function handleClaim() {
  proxy.$modal.confirm('确认领取该客户？领取后您将成为该客户的负责人。').then(() => {
    claimCustomer(customerId).then(() => {
      proxy.$modal.msgSuccess('领取成功')
      getCustomerData()
    })
  }).catch(() => {})
}

// 监听tab切换时加载对应数据
watch(activeTab, (val) => {
  if (val === 'contacts' && contacts.value.length === 0) getContacts()
  if (val === 'opportunities' && opportunities.value.length === 0) getOpportunities()
  if (val === 'contracts' && contracts.value.length === 0) getContracts()
  if (val === 'orders' && orders.value.length === 0) getOrders()
  if (val === 'interactions' && interactions.value.length === 0) getInteractions()
  if (val === 'activities' && activities.value.length === 0) getActivities()
})

getCustomerData()
getUserOptions()
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
.ml5 { margin-left: 5px; }

/* 独立详情页标题栏 */
.detail-header-bar {
  display: flex;
  align-items: stretch;
  gap: 0;
}
.detail-header-bar .rd-detail-header {
  flex: 1;
  border-radius: 12px;
}
.detail-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
}
</style>

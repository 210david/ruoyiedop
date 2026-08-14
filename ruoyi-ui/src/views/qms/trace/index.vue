<template>
  <div class="app-container qms-trace-search-page">
    <!-- 追溯查询区 -->
    <el-card shadow="never" class="mb10">
      <template #header>
        <div class="card-header">
          <span>质量追溯查询</span>
          <button type="button" class="tip-pill" @click="showHelp = true">
            <el-icon><WarningFilled /></el-icon>
            <span>业务说明</span>
          </button>
        </div>
      </template>
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="80px">
        <el-form-item label="追溯方向" prop="direction">
          <el-radio-group v-model="queryParams.direction">
            <el-radio-button label="backward">反向追溯（成品→原料）</el-radio-button>
            <el-radio-button label="forward">正向追溯（原料→成品）</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="queryParams.batchNo" placeholder="请输入批次号" clearable style="width: 240px" @keyup.enter="handleTrace" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleTrace" :loading="loading">开始追溯</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 降级追溯结果 -->
    <el-card shadow="never" v-if="fallbackData" class="mb10">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>
            <el-tag type="warning" size="small" effect="dark" class="mr10">降级追溯</el-tag>
            辅助追溯结果
          </span>
          <el-button type="primary" plain size="small" icon="Connection" @click="goGenealogy">去补录谱系</el-button>
        </div>
      </template>

      <!-- 提示消息 -->
      <el-alert :title="fallbackData.message" type="warning" :closable="false" show-icon class="mb15" />

      <!-- 统计信息 -->
      <el-descriptions :column="4" border size="small" class="mb15">
        <el-descriptions-item label="追溯批次">{{ fallbackData.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="追溯方向">{{ fallbackData.direction === 'forward' ? '正向（原料→成品）' : '反向（成品→原料）' }}</el-descriptions-item>
        <el-descriptions-item label="谱系节点数">{{ fallbackData.genealogyNodeCount }}</el-descriptions-item>
        <el-descriptions-item label="追溯时间">{{ fallbackData.traceTime }}</el-descriptions-item>
      </el-descriptions>

      <!-- 多维度追溯信息 -->
      <el-tabs v-model="fallbackTab" class="fallback-tabs">
        <!-- 检验记录 -->
        <el-tab-pane :label="`检验记录 (${fallbackData.inspectTasks?.length || 0})`" name="insp">
          <el-table :data="fallbackData.inspectTasks || []" border size="small">
            <el-table-column label="任务编号" prop="taskNo" min-width="120" />
            <el-table-column label="检验类型" prop="taskType" min-width="80">
              <template #default="s">
                <el-tag :type="inspTypeColor(s.row.taskType)" size="small">{{ s.row.taskType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="来源单号" prop="sourceNo" min-width="120" />
            <el-table-column label="物料" min-width="160">
              <template #default="s">{{ s.row.materialCode }} / {{ s.row.materialName }}</template>
            </el-table-column>
            <el-table-column label="供应商" prop="supplierName" min-width="120" />
            <el-table-column label="判定结果" min-width="80">
              <template #default="s">
                <el-tag :type="s.row.inspectResult === '1' ? 'success' : 'danger'" size="small">{{ s.row.inspectResult === '1' ? '合格' : '不合格' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="检验员" prop="inspectorName" min-width="80" />
            <el-table-column label="检验时间" prop="inspectTime" min-width="150" />
          </el-table>
          <el-empty v-if="!fallbackData.inspectTasks?.length" description="无检验记录" :image-size="60" />
        </el-tab-pane>

        <!-- NCR记录 -->
        <el-tab-pane :label="`NCR记录 (${fallbackData.ncrList?.length || 0})`" name="ncr">
          <el-table :data="fallbackData.ncrList || []" border size="small">
            <el-table-column label="NCR编号" prop="ncrNo" min-width="120" />
            <el-table-column label="来源单号" prop="sourceNo" min-width="120" />
            <el-table-column label="物料" min-width="160">
              <template #default="s">{{ s.row.materialCode }} / {{ s.row.materialName }}</template>
            </el-table-column>
            <el-table-column label="供应商" prop="supplierName" min-width="120" />
            <el-table-column label="缺陷等级" min-width="80">
              <template #default="s">
                <el-tag :type="parseInt(s.row.defectLevel) <= 2 ? 'danger' : 'warning'" size="small">
                  {{ { '1': '致命', '2': '严重', '3': '一般', '4': '轻微' }[s.row.defectLevel] || '-' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="缺陷描述" prop="defectDesc" min-width="200" show-overflow-tooltip />
            <el-table-column label="处置" min-width="80">
              <template #default="s">{{ { '1': '退货', '2': '挑选', '3': '返工', '4': '报废', '5': '让步' }[s.row.disposition] || '-' }}</template>
            </el-table-column>
            <el-table-column label="状态" prop="ncrStatus" min-width="80" />
            <el-table-column label="发现时间" prop="discoverTime" min-width="150" />
          </el-table>
          <el-empty v-if="!fallbackData.ncrList?.length" description="无NCR记录" :image-size="60" />
        </el-tab-pane>

        <!-- 仓库库存 -->
        <el-tab-pane :label="`库存快照 (${fallbackData.inventoryList?.length || 0})`" name="inv">
          <el-table :data="fallbackData.inventoryList || []" border size="small">
            <el-table-column label="物料编码" prop="materialCode" min-width="120" />
            <el-table-column label="物料名称" prop="materialName" min-width="150" />
            <el-table-column label="仓库" prop="warehouseName" min-width="120" />
            <el-table-column label="库位" prop="locationName" min-width="120" />
            <el-table-column label="批次号" prop="batchNo" min-width="120" />
            <el-table-column label="可用数量" prop="qty" min-width="90" align="center" />
            <el-table-column label="锁定数量" prop="lockQty" min-width="90" align="center" />
          </el-table>
          <el-empty v-if="!fallbackData.inventoryList?.length" description="无库存记录" :image-size="60" />
        </el-tab-pane>

        <!-- 仓库流水 -->
        <el-tab-pane :label="`库存流水 (${fallbackData.inventoryLogs?.length || 0})`" name="invLog">
          <el-table :data="fallbackData.inventoryLogs || []" border size="small">
            <el-table-column label="物料编码" prop="materialCode" min-width="120" />
            <el-table-column label="物料名称" prop="materialName" min-width="150" />
            <el-table-column label="仓库" prop="warehouseName" min-width="120" />
            <el-table-column label="变动类型" min-width="80">
              <template #default="s">
                <el-tag :type="invChangeTypeColor(s.row.changeType)" size="small">
                  {{ { '0': '入库', '1': '出库', '2': '移库入', '3': '移库出', '4': '盘盈', '5': '盘亏', '6': '锁定', '7': '解锁' }[s.row.changeType] || s.row.changeType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="变动数量" prop="changeQty" min-width="90" align="center" />
            <el-table-column label="变动后数量" prop="afterQty" min-width="90" align="center" />
            <el-table-column label="关联单据" prop="refOrderNo" min-width="120" />
            <el-table-column label="操作人" prop="createBy" min-width="80" />
            <el-table-column label="操作时间" prop="createTime" min-width="150" />
          </el-table>
          <el-empty v-if="!fallbackData.inventoryLogs?.length" description="无库存流水记录" :image-size="60" />
        </el-tab-pane>

        <!-- 采购收货 -->
        <el-tab-pane :label="`采购收货 (${fallbackData.receiveList?.length || 0})`" name="recv">
          <el-table :data="fallbackData.receiveList || []" border size="small">
            <el-table-column label="收货单号" prop="receiveNo" min-width="120" />
            <el-table-column label="采购订单" prop="orderNo" min-width="120" />
            <el-table-column label="供应商" prop="supplierName" min-width="120" />
            <el-table-column label="物料" min-width="160">
              <template #default="s">{{ s.row.materialCode }} / {{ s.row.materialName }}</template>
            </el-table-column>
            <el-table-column label="批次号" prop="batchNo" min-width="120" />
            <el-table-column label="收货数量" prop="receiveQty" min-width="90" align="center" />
            <el-table-column label="合格" prop="qualifiedQty" min-width="80" align="center" />
            <el-table-column label="不合格" prop="unqualifiedQty" min-width="80" align="center" />
            <el-table-column label="生产日期" prop="productionDate" min-width="100" />
            <el-table-column label="保质期至" prop="expiryDate" min-width="100" />
          </el-table>
          <el-empty v-if="!fallbackData.receiveList?.length" description="无采购收货记录" :image-size="60" />
        </el-tab-pane>

        <!-- 客诉记录 -->
        <el-tab-pane :label="`客诉记录 (${fallbackData.complaintList?.length || 0})`" name="comp">
          <el-table :data="fallbackData.complaintList || []" border size="small">
            <el-table-column label="客诉编号" prop="complaintNo" min-width="120" />
            <el-table-column label="客户名称" prop="customerName" min-width="120" />
            <el-table-column label="物料" min-width="160">
              <template #default="s">{{ s.row.materialCode }} / {{ s.row.materialName }}</template>
            </el-table-column>
            <el-table-column label="批次号" prop="batchNo" min-width="120" />
            <el-table-column label="投诉日期" prop="complaintDate" min-width="100" />
            <el-table-column label="严重程度" min-width="80">
              <template #default="s">
                <el-tag :type="severityColor(s.row.severity)" size="small">{{ s.row.severity }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="投诉类型" prop="complaintType" min-width="100" />
            <el-table-column label="缺陷描述" prop="defectDesc" min-width="200" show-overflow-tooltip />
            <el-table-column label="状态" prop="complaintStatus" min-width="80" />
          </el-table>
          <el-empty v-if="!fallbackData.complaintList?.length" description="无客诉记录" :image-size="60" />
        </el-tab-pane>

        <!-- 销售发货记录 -->
        <el-tab-pane :label="`销售发货 (${fallbackData.shipmentList?.length || 0})`" name="ship">
          <el-table :data="fallbackData.shipmentList || []" border size="small">
            <el-table-column label="发货单号" prop="shipmentNo" min-width="120" />
            <el-table-column label="订单编号" prop="orderNo" min-width="120" />
            <el-table-column label="客户名称" prop="customerName" min-width="120" />
            <el-table-column label="物料" min-width="160">
              <template #default="s">{{ s.row.materialCode }} / {{ s.row.materialName }}</template>
            </el-table-column>
            <el-table-column label="批次号" prop="batchNo" min-width="120" />
            <el-table-column label="发货数量" prop="shipQty" min-width="90" align="center" />
            <el-table-column label="出库单号" prop="outboundOrderNo" min-width="120" />
            <el-table-column label="发货日期" prop="shipmentDate" min-width="100" />
            <el-table-column label="状态" min-width="80">
              <template #default="s">
                <el-tag :type="shipmentStatusColor(s.row.status)" size="small">{{ shipmentStatusLabel(s.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="收货人" prop="receiverName" min-width="80" />
            <el-table-column label="物流公司" prop="logisticsCompany" min-width="100" />
            <el-table-column label="物流单号" prop="trackingNo" min-width="120" />
          </el-table>
          <el-empty v-if="!fallbackData.shipmentList?.length" description="无销售发货记录" :image-size="60" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 追溯结果 -->
    <el-card shadow="never" v-if="traceData">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>追溯结果{{ traceData.remark ? '（' + traceData.remark + '）' : '' }}</span>
          <el-button type="warning" plain icon="Printer" @click="handlePrint" v-hasPermi="['qms:trace:report']">打印/导出追溯报告</el-button>
        </div>
      </template>

      <!-- 统计信息 -->
      <el-descriptions :column="4" border size="small" class="mb15">
        <el-descriptions-item label="追溯批次">{{ queryParams.direction === 'forward' ? traceData.parentBatchNo : traceData.childBatchNo }}</el-descriptions-item>
        <el-descriptions-item label="追溯方向">{{ queryParams.direction === 'forward' ? '正向（原料→成品）' : '反向（成品→原料）' }}</el-descriptions-item>
        <el-descriptions-item label="关联节点数">{{ countNodes(traceData) }}</el-descriptions-item>
        <el-descriptions-item label="追溯时间">{{ traceData.traceTime }}</el-descriptions-item>
      </el-descriptions>

      <!-- 追溯树 -->
      <el-table :data="traceData.children || []" row-key="id" :tree-props="{ children: 'children' }" border default-expand-all>
        <el-table-column :label="queryParams.direction === 'forward' ? '产出批次' : '投入批次'" prop="queryParams.direction === 'forward' ? childBatchNo : parentBatchNo'" min-width="160">
          <template #default="scope">
            <el-tag :type="scope.row.breakFlag === 1 ? 'danger' : 'success'" size="small">
              {{ queryParams.direction === 'forward' ? (scope.row.childBatchNo || '断点') : (scope.row.parentBatchNo || '断点') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="批次号" min-width="140">
          <template #default="scope">
            {{ queryParams.direction === 'forward' ? (scope.row.parentBatchNo || '-') : (scope.row.childBatchNo || '-') }}
          </template>
        </el-table-column>
        <el-table-column label="物料" min-width="180">
          <template #default="scope">
            <span v-if="scope.row.materialCode">{{ scope.row.materialCode }} / {{ scope.row.materialName }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="工单号" prop="workOrderNo" min-width="120" />
        <el-table-column label="工序" prop="opName" min-width="100" />
        <el-table-column label="供应商" prop="supplierName" min-width="140" />
        <el-table-column label="投入数量" prop="qtyIn" min-width="90" align="center" />
        <el-table-column label="产出数量" prop="qtyOut" min-width="90" align="center" />
        <el-table-column label="来源" prop="sourceType" min-width="80">
          <template #default="scope">
            <dict-tag :options="qms_genealogy_source" :value="scope.row.sourceType" />
          </template>
        </el-table-column>
        <el-table-column label="操作员" prop="operator" min-width="80" />
        <el-table-column label="谱系时间" prop="traceTime" min-width="150" />
        <el-table-column label="断点" min-width="60" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.breakFlag === 1" type="danger" size="small">断点</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="关联记录" min-width="120" align="center">
          <template #default="scope">
            <el-badge :value="(scope.row.inspectTasks?.length || 0) + (scope.row.ncrList?.length || 0)" :hidden="!scope.row.inspectTasks?.length && !scope.row.ncrList?.length" type="primary">
              <el-button text size="small" @click="handleViewDetail(scope.row)">详情</el-button>
            </el-badge>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 空提示 -->
    <el-empty v-else-if="!loading && !traceData && !fallbackData" description="请输入批次号并选择追溯方向后开始追溯" />

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailOpen" title="关联质量记录" width="800px" append-to-body>
      <el-tabs v-model="detailTab">
        <el-tab-pane label="检验记录" name="insp">
          <el-table :data="currentDetail.inspectTasks || []" border size="small">
            <el-table-column label="任务编号" prop="taskNo" min-width="120" />
            <el-table-column label="检验类型" prop="taskType" min-width="80" />
            <el-table-column label="物料" prop="materialName" min-width="150" />
            <el-table-column label="批次" prop="batchNo" min-width="120" />
            <el-table-column label="判定结果" min-width="80">
              <template #default="s">
                <el-tag :type="s.row.inspectResult === '1' ? 'success' : 'danger'" size="small">{{ s.row.inspectResult === '1' ? '合格' : '不合格' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="检验时间" prop="judgeTime" min-width="150" />
          </el-table>
          <el-empty v-if="!currentDetail.inspectTasks?.length" description="无检验记录" :image-size="60" />
        </el-tab-pane>
        <el-tab-pane label="NCR记录" name="ncr">
          <el-table :data="currentDetail.ncrList || []" border size="small">
            <el-table-column label="NCR编号" prop="ncrNo" min-width="120" />
            <el-table-column label="物料" prop="materialName" min-width="150" />
            <el-table-column label="缺陷等级" min-width="80">
              <template #default="s">
                <el-tag :type="parseInt(s.row.defectLevel) <= 2 ? 'danger' : 'warning'" size="small">
                  {{ { '1': '致命', '2': '严重', '3': '一般', '4': '轻微' }[s.row.defectLevel] || '-' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="处置" min-width="80">
              <template #default="s">{{ { '1': '退货', '2': '挑选', '3': '返工', '4': '报废', '5': '让步' }[s.row.disposition] || '-' }}</template>
            </el-table-column>
            <el-table-column label="状态" prop="ncrStatus" min-width="80" />
            <el-table-column label="发现时间" prop="discoverTime" min-width="150" />
          </el-table>
          <el-empty v-if="!currentDetail.ncrList?.length" description="无NCR记录" :image-size="60" />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- ===== 业务说明弹窗 ===== -->
    <el-dialog v-model="showHelp" title="追溯查询业务操作说明" width="780px" append-to-body>
      <div class="help-content">
        <h4>一、质量追溯概述</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">什么是质量追溯</div>
              <div class="highlight-card-body">质量追溯是基于<strong>批次谱系数据</strong>（投入批次↔工单↔产出批次的关联链），实现物料批次间正反双向关系查询的能力，是质量管控与合规审计的核心功能</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">为什么需要追溯</div>
              <div class="highlight-card-body">当发生<strong>客诉、质量异常、召回</strong>时，需快速定位问题批次的全链路：原料来源、生产过程、检验记录、流向客户，满足 HACCP / IATF16949 监管要求</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">正向追溯</div>
              <div class="highlight-card-body">由<strong>原料/半成品批次</strong>向下追查至成品批次 → 发货单 → 销售订单 → 客户。回答"这批原料用在了哪些产品中，发给了哪些客户"</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-info">
              <div class="highlight-card-title">反向追溯</div>
              <div class="highlight-card-body">由<strong>成品批次</strong>向上追查至半成品 → 原料批次 → 供应商 → 检验记录 → NCR。回答"这个产品用了哪些原料，来自哪个供应商"</div>
            </div>
          </el-col>
        </el-row>

        <h4>二、查询操作说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="追溯方向">选择追溯方向：<strong>反向追溯</strong>（成品→原料）用于客诉定位上游原料；<strong>正向追溯</strong>（原料→成品）用于原料问题定位下游成品和客户</el-descriptions-item>
          <el-descriptions-item label="批次号">输入需要追溯的物料批次号，支持原料、半成品或成品批次。批次号需与谱系表中登记的批次号一致</el-descriptions-item>
          <el-descriptions-item label="开始追溯">点击"开始追溯"按钮执行查询，系统将基于谱系数据递归查询所有关联节点（最大深度 ≤10 层）</el-descriptions-item>
          <el-descriptions-item label="重置">清空当前查询条件及追溯结果，重新输入批次号进行追溯</el-descriptions-item>
        </el-descriptions>

        <h4>三、追溯结果说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="追溯批次">当前追溯的起始批次号，根据追溯方向显示为投入批次（正向）或产出批次（反向）</el-descriptions-item>
          <el-descriptions-item label="追溯方向">显示当前追溯方向：正向（原料→成品）或反向（成品→原料）</el-descriptions-item>
          <el-descriptions-item label="关联节点数">追溯链路中查找到的谱系关联节点总数，反映该批次的谱系链路深度</el-descriptions-item>
          <el-descriptions-item label="谱系时间">追溯查询执行的时间</el-descriptions-item>
        </el-descriptions>

        <h4>四、追溯链表格字段</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="投入产出批次">谱系链中的批次号，正向追溯显示产出批次，反向追溯显示投入批次。<strong>红色标签</strong>表示断点（产出批次未登记）</el-descriptions-item>
          <el-descriptions-item label="批次号">谱系记录中对端的批次号，与追溯方向相反的批次</el-descriptions-item>
          <el-descriptions-item label="物料">节点关联的物料编码与名称</el-descriptions-item>
          <el-descriptions-item label="工单号">该谱系关联的生产工单编号</el-descriptions-item>
          <el-descriptions-item label="工序">谱系关联的生产工序名称</el-descriptions-item>
          <el-descriptions-item label="供应商">原料批次关联的供应商名称（反向追溯时重点关注）</el-descriptions-item>
          <el-descriptions-item label="投入产出数量">该节点投入和产出的物料数量</el-descriptions-item>
          <el-descriptions-item label="来源">谱系数据来源类型：投料/报工/完工/调拨/返工/合并/拆分/补录</el-descriptions-item>
          <el-descriptions-item label="断点">该节点是否为谱系断点，断点会导致追溯链中断</el-descriptions-item>
          <el-descriptions-item label="关联记录">该节点关联的检验记录和NCR数量，点击"详情"可查看具体记录</el-descriptions-item>
        </el-descriptions>

        <h4>五、关联质量记录</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="检验记录">该批次关联的<strong>IQC/IPQC/FQC/OQC</strong>检验任务，包含任务编号、检验类型、物料、批次、判定结果（合格/不合格）、检验时间</el-descriptions-item>
          <el-descriptions-item label="NCR记录">该批次关联的<strong>不合格品报告</strong>，包含NCR编号、物料、缺陷等级（致命/严重/一般/轻微）、处置方式（退货/挑选/返工/报废/让步）、状态、发现时间</el-descriptions-item>
        </el-descriptions>

        <h4>六、降级追溯（辅助追溯）</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">什么是降级追溯</div>
              <div class="highlight-card-body">当谱系表中<strong>未建立该批次的投入-产出关联</strong>时，系统自动切换到降级追溯模式，从已有业务单据（检验、NCR、仓库、采购、客诉、发货）中按批次号关联查询，提供辅助追溯信息</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-info">
              <div class="highlight-card-title">触发条件</div>
              <div class="highlight-card-body">点击"开始追溯"后，若谱系表查询结果为空（无子节点），系统将<strong>自动触发</strong>降级追溯，页面下方出现橙色"降级追溯"卡片，无需手动操作</div>
            </div>
          </el-col>
        </el-row>
        <el-descriptions :column="1" border style="margin-top: 12px;">
          <el-descriptions-item label="检验记录">从 <strong>qms_insp_task</strong> 中按批次号查询该批次的所有检验任务（IQC/IPQC/FQC/OQC），展示任务编号、检验类型、物料、供应商、判定结果、检验员、检验时间</el-descriptions-item>
          <el-descriptions-item label="NCR记录">从 <strong>qms_ncr</strong> 中按批次号查询该批次关联的不合格品报告，展示NCR编号、缺陷等级、缺陷描述、处置方式、状态</el-descriptions-item>
          <el-descriptions-item label="库存快照">从 <strong>wms_inventory</strong> 中按批次号查询当前库存余额，展示物料、仓库、库位、可用数量、锁定数量</el-descriptions-item>
          <el-descriptions-item label="库存流水">从 <strong>wms_inventory_log</strong> 中按批次号查询库存变动记录（入库/出库/移库/盘点），展示变动类型、变动数量、关联单据、操作人</el-descriptions-item>
          <el-descriptions-item label="采购收货">从 <strong>pms_receive_detail</strong> 中按批次号查询采购收货记录，展示收货单号、采购订单、供应商、收货数量、合格/不合格数量、生产日期、保质期</el-descriptions-item>
          <el-descriptions-item label="客诉记录">从 <strong>qms_complaint</strong> 中按批次号查询客户投诉记录，展示客诉编号、客户名称、投诉类型、严重程度、缺陷描述、状态</el-descriptions-item>
          <el-descriptions-item label="销售发货">从 <strong>mk_shipment</strong> 关联 <strong>wms_outbound_order_detail</strong> 按批次号查询发货记录，展示发货单号、订单编号、客户名称、出库单号、发货数量、发货日期、状态、收货人、物流信息</el-descriptions-item>
        </el-descriptions>
        <el-alert type="warning" :closable="false" show-icon style="margin-top: 12px;">
          <template #title>
            <strong>操作建议：</strong>降级追溯仅作为谱系数据缺失时的临时辅助手段，其数据维度独立、无法展示批次间的投入-产出链路关系。建议在降级追溯结果中确认批次有业务数据后，及时前往<strong>谱系管理</strong>页面补录投入-产出关联，以获得完整的追溯链路。
          </template>
        </el-alert>

        <h4>七、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>选择方向：</strong>根据追溯需求选择正向（原料→成品）或反向（成品→原料）追溯方向
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>输入批次号：</strong>输入需要追溯的物料批次号，支持回车快速提交
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>查看链路：</strong>追溯结果以树形表格展示完整谱系链，断点节点以红色标记，关注断点位置的链路中断
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>查看质量记录：</strong>点击节点"详情"查看关联的检验记录和NCR记录，定位质量异常
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>降级追溯（自动触发）：</strong>若谱系无数据，系统自动从检验/NCR/仓库/采购/客诉/发货等业务单据中按批次号辅助查询，结果展示在页面下方橙色卡片中，切换Tab页查看各维度信息
          </el-timeline-item>
          <el-timeline-item type="primary" :hollow="true">
            <strong>补录谱系（可选）：</strong>降级追溯结果中点击"去补录谱系"按钮，前往谱系管理页面补录投入-产出关联，后续追溯即可使用完整谱系链
          </el-timeline-item>
          <el-timeline-item type="primary" :hollow="true">
            <strong>导出报告：</strong>点击"打印/导出追溯报告"生成PDF追溯报告，用于审计或客诉应对
          </el-timeline-item>
        </el-timeline>

        <h4>八、典型应用场景</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="客诉应对">客户投诉某成品批次质量问题时，使用<strong>反向追溯</strong>定位该批次使用的原料、供应商和检验记录</el-descriptions-item>
          <el-descriptions-item label="原料问题">发现某原料批次存在质量问题时，使用<strong>正向追溯</strong>定位该原料用在了哪些成品批次中，发给了哪些客户</el-descriptions-item>
          <el-descriptions-item label="监管审计">应对 HACCP / IATF16949 审计时，导出完整谱系链和检验记录报告，证明追溯能力</el-descriptions-item>
          <el-descriptions-item label="召回管理">发现质量问题时，通过正向追溯快速确定受影响的产品批次和客户范围，支持精准召回</el-descriptions-item>
          <el-descriptions-item label="谱系未建立时的应急追溯">当某批次尚未建立谱系关联（新产线/历史数据），但业务单据中已有批次记录时，系统自动降级追溯，从检验记录、库存流水、采购收货、销售发货等维度提供该批次的流转信息，辅助定位问题</el-descriptions-item>
        </el-descriptions>

        <h4>九、专业词汇说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="谱系(Genealogy)"><strong>物料批次间的投入-产出关联关系链</strong>，是实现质量追溯的基础数据结构</el-descriptions-item>
          <el-descriptions-item label="正向追溯">由原料/半成品批次向下追查至成品、订单、客户的过程</el-descriptions-item>
          <el-descriptions-item label="反向追溯">由成品批次/客诉向上追查至原料、供应商、工序、检验记录的过程</el-descriptions-item>
          <el-descriptions-item label="断点">投入或产出批次未建立关联，导致追溯链中断的节点</el-descriptions-item>
          <el-descriptions-item label="降级追溯">当谱系表无该批次数据时，系统自动从业务单据（检验/NCR/仓库/采购/客诉/发货）中按批次号查询关联记录，作为谱系追溯的<strong>临时辅助手段</strong>。降级追溯结果按维度独立展示，无法替代完整的谱系投入-产出链</el-descriptions-item>
          <el-descriptions-item label="IQC">Incoming Quality Control（来料质量控制），对供应商交付的原材料进行入库前的质量检验</el-descriptions-item>
          <el-descriptions-item label="IPQC">In-Process Quality Control（过程质量控制），对生产过程中的半成品进行质量检验</el-descriptions-item>
          <el-descriptions-item label="FQC">Final Quality Control（成品质量控制），对完工入库的成品进行最终质量检验</el-descriptions-item>
          <el-descriptions-item label="OQC">Outgoing Quality Control（出货质量控制），对发货前的成品进行出货检验</el-descriptions-item>
          <el-descriptions-item label="NCR">Non-Conformance Report（不合格品报告），记录质量异常并跟踪处理闭环</el-descriptions-item>
          <el-descriptions-item label="HACCP">Hazard Analysis and Critical Control Points（危害分析与关键控制点），食品安全管理体系</el-descriptions-item>
          <el-descriptions-item label="IATF16949">汽车行业质量管理体系标准，要求完整的产品追溯能力</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button type="primary" @click="showHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsTraceSearch">
import { ref, reactive, getCurrentInstance } from 'vue'
import { forwardTrace, backwardTrace, fallbackTrace } from '@/api/qms/trace'
import { useRouter } from 'vue-router'

const { proxy } = getCurrentInstance()
const { qms_genealogy_source } = proxy.useDict('qms_genealogy_source')
const router = useRouter()

const loading = ref(false)
const traceData = ref(null)
const fallbackData = ref(null)
const fallbackTab = ref('insp')
const detailOpen = ref(false)
const detailTab = ref('insp')
const currentDetail = ref({})
const showHelp = ref(false)
const queryParams = reactive({ direction: 'backward', batchNo: '' })

function handleTrace() {
  if (!queryParams.batchNo) { proxy.$modal.msgWarning('请输入批次号'); return }
  loading.value = true
  traceData.value = null
  fallbackData.value = null
  const api = queryParams.direction === 'forward' ? forwardTrace : backwardTrace
  api(queryParams.batchNo).then(res => {
    traceData.value = res.data
    if (!res.data.children?.length) {
      // 谱系无数据，自动触发降级追溯
      proxy.$modal.msgWarning('未找到谱系数据，正在从业务单据中辅助追溯...')
      fallbackTrace(queryParams.batchNo, queryParams.direction).then(fbRes => {
        fallbackData.value = fbRes.data
      })
    }
  }).finally(() => { loading.value = false })
}

function goGenealogy() {
  router.push('/qms/trace/genealogy')
}

function inspTypeColor(type) {
  return { 'IQC': 'warning', 'IPQC': 'primary', 'FQC': 'success', 'OQC': 'info' }[type] || ''
}

function invChangeTypeColor(type) {
  return { '0': 'success', '1': 'danger', '2': 'primary', '3': 'info', '4': 'success', '5': 'danger', '6': 'warning', '7': 'info' }[type] || ''
}

function severityColor(severity) {
  return { '严重': 'danger', '重要': 'warning', '一般': 'info', '轻微': 'info' }[severity] || 'info'
}

function shipmentStatusColor(status) {
  return { '0': 'warning', '1': 'primary', '2': 'success', '3': 'info' }[status] || 'info'
}

function shipmentStatusLabel(status) {
  return { '0': '待发货', '1': '已发货', '2': '已签收', '3': '已取消' }[status] || '-'
}

function resetQuery() { queryParams.batchNo = ''; traceData.value = null }

function countNodes(node) {
  let count = 0
  if (node.children) { for (const c of node.children) { count++; count += countNodes(c) } }
  return count
}

function handleViewDetail(row) { currentDetail.value = row; detailTab.value = 'insp'; detailOpen.value = true }

function handlePrint() { proxy.$modal.msgSuccess('追溯报告打印功能（PDF导出待对接文件服务）') }
</script>

<style scoped>
.qms-trace-search-page .card-header {
  display: flex; align-items: center; justify-content: space-between;
}
.qms-trace-search-page .tip-pill {
  display: inline-flex; align-items: center; gap: 6px; height: 30px; padding: 0 12px;
  font-size: 14px; font-weight: 500; border-radius: 6px;
  border: 1px solid #fde68a; background: #fffbeb; color: #b45309;
  cursor: pointer; transition: all .15s; white-space: nowrap;
}
.qms-trace-search-page .tip-pill:hover { background: #fef3c7; border-color: #f59e0b; }
.qms-trace-search-page .fallback-tabs .el-tabs__item { font-weight: 500; }
.qms-trace-search-page .mr10 { margin-right: 10px; }

/* ===== Help Dialog ===== */
.help-content { max-height: 500px; overflow-y: auto; padding-right: 10px; }
.help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.help-content h4:first-child { margin-top: 0; }
.help-content .highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.help-content .highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.help-content .highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
.help-content .highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.help-content .highlight-primary .highlight-card-title { color: #409eff; }
.help-content .highlight-success { background-color: #f0f9eb; border-color: #b3e19d; }
.help-content .highlight-success .highlight-card-title { color: #67c23a; }
.help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.help-content .highlight-warning .highlight-card-title { color: #e6a23c; }
.help-content .highlight-info { background-color: #f4f4f5; border-color: #d3d4d6; }
.help-content .highlight-info .highlight-card-title { color: #909399; }
</style>

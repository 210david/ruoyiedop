<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!--查询条件-->
      <el-col :span="24" :xs="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
          <el-form-item label="物料编码" prop="materialCode">
            <el-input v-model="queryParams.materialCode" placeholder="请输入物料编码" clearable style="width: 200px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="物料名称" prop="materialName">
            <el-input v-model="queryParams.materialName" placeholder="请输入物料名称" clearable style="width: 600px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="物料类型" prop="materialType">
            <el-select v-model="queryParams.materialType" placeholder="请选择" clearable style="width: 200px">
              <el-option v-for="dict in wms_material_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 200px">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:material:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['wms:material:edit']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:material:remove']">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['wms:material:export']">导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table ref="tableRef" border v-loading="loading" :data="materialList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <!-- 基本信息 -->
          <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 120)" resizable />
          <el-table-column label="物料名称" prop="materialName" :width="colWidth('materialName', 224)" resizable show-overflow-tooltip />
          <el-table-column label="物料类型" prop="materialType" :width="colWidth('materialType', 100)" resizable align="center">
            <template #default="scope">
              <dict-tag :options="wms_material_type" :value="scope.row.materialType" />
            </template>
          </el-table-column>
          <el-table-column label="规格型号" prop="specModel" :width="colWidth('specModel', 225)" resizable show-overflow-tooltip />
          <el-table-column label="单位" prop="unit" :width="colWidth('unit', 80)" resizable align="center">
            <template #default="scope">
              <dict-tag :options="wms_unit" :value="scope.row.unit" />
            </template>
          </el-table-column>
          <el-table-column label="批次管理" prop="isBatchManage" :width="colWidth('isBatchManage', 90)" resizable align="center">
            <template #default="scope">
              <el-tag :type="scope.row.isBatchManage === '1' ? 'success' : 'info'">{{ scope.row.isBatchManage === '1' ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <!-- 效期管理 -->
          <el-table-column label="效期管理" prop="isExpiryManage" :width="colWidth('isExpiryManage', 90)" resizable align="center">
            <template #default="scope">
              <el-tag :type="scope.row.isExpiryManage === '1' ? 'success' : 'info'">{{ scope.row.isExpiryManage === '1' ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="保质期(天)" prop="shelfLifeDays" :width="colWidth('shelfLifeDays', 100)" resizable align="center">
            <template #default="scope">
              {{ scope.row.shelfLifeDays != null ? scope.row.shelfLifeDays : '-' }}
            </template>
          </el-table-column>
          <!-- 库存控制 -->
          <el-table-column label="安全库存下限" prop="safetyStockMin" :width="colWidth('safetyStockMin', 120)" resizable align="right" />
          <el-table-column label="安全库存上限" prop="safetyStockMax" :width="colWidth('safetyStockMax', 120)" resizable align="right" />
          <!-- 其他 -->
          <el-table-column label="状态" prop="status" :width="colWidth('status', 80)" resizable align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" />
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['wms:material:edit']">修改</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:material:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </el-col>
    </el-row>

    <!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="materialRef" :model="form" :rules="rules" label-width="100px" class="material-form">
                <!-- 分组一：基本信息 -->
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物料编码" prop="materialCode">
              <el-input v-model="form.materialCode" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料名称" prop="materialName">
              <el-input v-model="form.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物料类型" prop="materialType">
              <el-select v-model="form.materialType" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in wms_material_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计量单位" prop="unit">
              <el-select v-model="form.unit" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in wms_unit" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规格型号" prop="specModel">
              <el-input v-model="form.specModel" placeholder="请输入规格型号" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 分组二：效期管理 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>效期管理</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="效期管理" prop="isExpiryManage">
              <el-radio-group v-model="form.isExpiryManage">
                <el-radio value="0">否</el-radio>
                <el-radio value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次管理" prop="isBatchManage">
              <el-radio-group v-model="form.isBatchManage">
                <el-radio value="0">否</el-radio>
                <el-radio value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="保质期天数" prop="shelfLifeDays">
              <el-input-number v-model="form.shelfLifeDays" :min="0" :precision="0" controls-position="right" placeholder="天" style="width: 50%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="临期预警" prop="expiryAlertDays">
              <el-input-number v-model="form.expiryAlertDays" :min="0" :precision="0" controls-position="right" placeholder="天" style="width: 50%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 分组三：库存控制 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>库存控制</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="安全库存下限" prop="safetyStockMin">
              <el-input-number v-model="form.safetyStockMin" :precision="2" :min="0" style="width: 50%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安全库存上限" prop="safetyStockMax">
              <el-input-number v-model="form.safetyStockMax" :precision="2" :min="0" style="width: 50%" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 分组四：其他信息 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio value="0">正常</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
                </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog v-model="viewOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">物料详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.materialCode">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编码：{{ viewData.materialCode }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v3">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ viewData.materialCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料类型</span><div class="rd-value"><dict-tag :options="wms_material_type" :value="viewData.materialType" /></div></div>
              <div class="rd-item"><span class="rd-label">计量单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 效期管理 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>效期管理</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">效期管理</span><div class="rd-value"><el-tag :type="viewData.isExpiryManage === '1' ? 'success' : 'info'">{{ viewData.isExpiryManage === '1' ? '是' : '否' }}</el-tag></div></div>
              <div class="rd-item"><span class="rd-label">批次管理</span><div class="rd-value"><el-tag :type="viewData.isBatchManage === '1' ? 'success' : 'info'">{{ viewData.isBatchManage === '1' ? '是' : '否' }}</el-tag></div></div>
              <div class="rd-item"><span class="rd-label">保质期天数</span><div class="rd-value">{{ viewData.shelfLifeDays != null ? viewData.shelfLifeDays + ' 天' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">临期预警</span><div class="rd-value">{{ viewData.expiryAlertDays != null ? viewData.expiryAlertDays + ' 天' : '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 库存控制 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>库存控制</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">安全库存下限</span><div class="rd-value">{{ viewData.safetyStockMin != null ? viewData.safetyStockMin : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">安全库存上限</span><div class="rd-value">{{ viewData.safetyStockMax != null ? viewData.safetyStockMax : '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><el-tag :type="viewData.status === '0' ? 'success' : 'danger'">{{ viewData.status === '0' ? '正常' : '停用' }}</el-tag></div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="WmsMaterial">
import { listMaterial, getMaterial, addMaterial, updateMaterial, delMaterial } from '@/api/wms/material'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c3","c2","c1","c0","v3","v2","v1","v0"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_material_index')
const { wms_material_type, wms_unit } = proxy.useDict('wms_material_type', 'wms_unit')

const materialList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    materialCode: undefined,
    materialName: undefined,
    materialType: undefined,
    status: undefined
  },
rules: {
materialName: [{ required: true, message: '物料名称不能为空', trigger: 'blur' }],
    unit: [{ required: true, message: '计量单位不能为空', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listMaterial(queryParams.value).then(response => {
    materialList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.materialId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    materialId: undefined,
    materialCode: undefined,
    materialName: undefined,
    materialType: '0',
    specModel: undefined,
    unit: undefined,
    shelfLifeDays: undefined,
    expiryAlertDays: 30,
    isExpiryManage: '0',
    safetyStockMin: 0,
    safetyStockMax: 0,
    isBatchManage: '0',
    status: '0',
    remark: undefined
  }
  proxy.resetForm('materialRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加物料'
}

function handleUpdate(row) {
  reset()
  const materialId = row.materialId || ids.value[0]
  getMaterial(materialId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改物料'
  })
}

function handleView(row) {
  getMaterial(row.materialId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['materialRef'].validate(valid => {
    if (valid) {
      if (form.value.materialId != undefined) {
        updateMaterial(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addMaterial(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const materialIds = row.materialId || ids.value
  proxy.$modal.confirm('是否确认删除物料编号为"' + materialIds + '"的数据项？').then(function () {
    return delMaterial(materialIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('wms/material/export', { ...queryParams.value }, `material_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

getList()
</script>

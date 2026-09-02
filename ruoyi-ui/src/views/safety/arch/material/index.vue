<template>
  <div class="app-container safety-archmaterial-page">
    <!-- ===== Filter Card ===== -->
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
          <label>危化品编号</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>危化品名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>危化品分类</label>
          <div class="control is-select">
            <el-select v-model="queryParams.hazardClass" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_hazard_class" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>CAS号</label>
          <div class="control">
            <el-input v-model="queryParams.casNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>剧毒/易制爆</label>
          <div class="control is-select">
            <el-select v-model="queryParams.toxicFlag" placeholder="全部" clearable @change="handleQuery">
              <el-option label="无" value="0" />
              <el-option label="剧毒" value="1" />
              <el-option label="易制爆" value="2" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>储存位置</label>
          <div class="control">
            <el-input v-model="queryParams.storageAreaName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
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

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:material:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:material:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:material:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_archmaterial_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="materialList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="危化品编号" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 130)" resizable sortable="custom" show-overflow-tooltip v-if="columns.materialCode.visible" />
          <el-table-column label="危化品名称" prop="materialName" key="materialName" :width="colWidth('materialName', 180)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="危化品分类" prop="hazardClass" key="hazardClass" :width="colWidth('hazardClass', 140)" resizable v-if="columns.hazardClass.visible">
            <template #header><span>危化品分类</span><el-tooltip content="依据《危险化学品目录》对危险化学品进行的分类编号，如第2.1类易燃气体、第3类易燃液体等" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope">
              <span class="badge" :class="hazardClassBadgeClass(scope.row.hazardClass)">
                <span class="dot"></span>{{ hazardClassLabel(scope.row.hazardClass) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="CAS号" prop="casNo" key="casNo" :width="colWidth('casNo', 130)" resizable v-if="columns.casNo.visible">
            <template #header><span>CAS号</span><el-tooltip content="CAS号是化学物质在美国化学文摘社（CAS）数据库中的唯一标识编号，如：7732-18-5（水）" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="最大储存量" prop="maxStorage" key="maxStorage" :width="colWidth('maxStorage', 110)" resizable align="center" v-if="columns.maxStorage.visible" />
          <el-table-column label="单位" prop="storageUnit" key="storageUnit" :width="colWidth('storageUnit', 80)" resizable align="center" v-if="columns.storageUnit.visible" />
          <el-table-column label="储存位置" prop="storageAreaName" key="storageAreaName" :width="colWidth('storageAreaName', 130)" resizable show-overflow-tooltip v-if="columns.storageAreaName.visible" />
          <el-table-column label="当前库存" prop="currentStock" key="currentStock" :width="colWidth('currentStock', 120)" resizable align="center" v-if="columns.currentStock.visible">
            <template #default="scope">
              <span :style="{ color: isStockAlert(scope.row) ? '#ef4444' : '', fontWeight: isStockAlert(scope.row) ? '700' : '400' }">{{ scope.row.currentStock != null ? scope.row.currentStock : '-' }}</span>
              <el-tooltip v-if="isStockAlert(scope.row)" content="库存预警" placement="top"><el-icon style="margin-left:4px;color:#ef4444"><Warning /></el-icon></el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="剧毒/易制爆" prop="toxicFlag" key="toxicFlag" :width="colWidth('toxicFlag', 110)" resizable align="center" v-if="columns.toxicFlag.visible">
            <template #header><span>剧毒/易制爆</span><el-tooltip content="标识该危化品是否属于剧毒化学品或易制爆危险化学品。剧毒化学品指具有非常剧烈毒性作用的化学品；易制爆化学品指可用于制造爆炸物的化学品" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope">
              <span class="badge" :class="scope.row.toxicFlag === '1' ? 'red' : scope.row.toxicFlag === '2' ? 'amber' : 'gray'">
                <span class="dot"></span>{{ scope.row.toxicFlag === '1' ? '剧毒' : scope.row.toxicFlag === '2' ? '易制爆' : '无' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:material:query']">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:material:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:material:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="materialRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></span>危化品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="危化品编号" prop="materialCode"><el-input v-model="form.materialCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="危化品名称" prop="materialName"><el-input v-model="form.materialName" placeholder="请输入危化品名称" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item prop="hazardClass"><template #label><span>危化品分类</span><el-tooltip content="依据《危险化学品目录》对危险化学品进行的分类编号，如第2.1类易燃气体、第3类易燃液体等" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-select v-model="form.hazardClass" placeholder="请选择危化品分类" style="width: 100%"><el-option v-for="dict in safety_hazard_class" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item prop="casNo"><template #label><span>CAS号</span><el-tooltip content="CAS号是化学物质在美国化学文摘社（CAS）数据库中的唯一标识编号，如：7732-18-5（水）" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.casNo" placeholder="请输入CAS号" /></el-form-item></el-col>
              </el-row>
              <el-form-item prop="toxicFlag"><template #label><span>剧毒/易制爆</span><el-tooltip content="标识该危化品是否属于剧毒化学品或易制爆危险化学品。剧毒化学品指具有非常剧烈毒性作用的化学品；易制爆化学品指可用于制造爆炸物的化学品" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-select v-model="form.toxicFlag" style="width: 100%"><el-option label="无" value="0" /><el-option label="剧毒" value="1" /><el-option label="易制爆" value="2" /></el-select></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg></span>储存信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="最大储存量" prop="maxStorage"><el-input-number v-model="form.maxStorage" :min="0" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="当前库存" prop="currentStock"><el-input-number v-model="form.currentStock" :min="0" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="安全库存下限" prop="safetyStockMin"><el-input-number v-model="form.safetyStockMin" :min="0" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="安全库存上限" prop="safetyStockMax"><el-input-number v-model="form.safetyStockMax" :min="0" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="单位" prop="storageUnit"><el-select v-model="form.storageUnit" placeholder="请选择单位" style="width: 100%"><el-option v-for="dict in wms_unit" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item>
              <el-form-item label="储存位置" prop="storageAreaId"><el-tree-select v-model="form.storageAreaId" :data="areaOptions" :props="{ value: 'areaId', label: 'areaName', children: 'children' }" value-key="areaId" placeholder="请选择储存位置" check-strictly style="width: 100%" @change="onAreaChange" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>附件与备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item prop="sdsAttachment"><template #label><span>SDS附件</span><el-tooltip content="SDS（Safety Data Sheet）即化学品安全技术说明书，是化学品生产商和进口商用来阐明化学品的理化特性、危险性以及对使用者健康可能产生危害的综合性文件" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><file-upload v-model="form.sdsAttachment" /></el-form-item>
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>SDS关键参数</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="闪点(℃)" prop="flashPoint"><el-input v-model="form.flashPoint" placeholder="如：-40" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="爆炸极限(%V/V)" prop="explosionLimit"><el-input v-model="form.explosionLimit" placeholder="如：2.5-12.7" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="危险性概述" prop="hazardSummary"><el-input v-model="form.hazardSummary" type="textarea" :rows="2" placeholder="请输入危险性概述" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.29 1.51 4.04 3 5.5l7 7Z"/></svg></span>SDS详细信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c4">
              <el-form-item label="理化特性" prop="physicalProperties"><el-input v-model="form.physicalProperties" type="textarea" :rows="2" placeholder="外观、气味、密度、熔沸点等" /></el-form-item>
              <el-form-item label="急救措施" prop="firstAidMeasures"><el-input v-model="form.firstAidMeasures" type="textarea" :rows="2" placeholder="请输入急救措施" /></el-form-item>
              <el-form-item label="灭火方法" prop="fireFighting"><el-input v-model="form.fireFighting" type="textarea" :rows="2" placeholder="请输入灭火方法与灭火剂" /></el-form-item>
              <el-form-item label="泄漏应急处理" prop="leakHandling"><el-input v-model="form.leakHandling" type="textarea" :rows="2" placeholder="请输入泄漏应急处理" /></el-form-item>
              <el-form-item label="储存注意事项" prop="storagePrecaution"><el-input v-model="form.storagePrecaution" type="textarea" :rows="2" placeholder="请输入储存注意事项" /></el-form-item>
              <el-form-item label="个人防护(PPE)" prop="exposureControl"><el-input v-model="form.exposureControl" type="textarea" :rows="2" placeholder="请输入接触控制/个人防护装备" /></el-form-item>
              <el-form-item label="稳定性和反应性" prop="stabilityReactivity"><el-input v-model="form.stabilityReactivity" type="textarea" :rows="2" placeholder="请输入稳定性和反应性" /></el-form-item>
              <el-form-item label="毒理学信息" prop="toxicologicalInfo"><el-input v-model="form.toxicologicalInfo" type="textarea" :rows="2" placeholder="请输入毒理学信息" /></el-form-item>
              <el-form-item label="生态学信息" prop="ecologicalInfo"><el-input v-model="form.ecologicalInfo" type="textarea" :rows="2" placeholder="请输入生态学信息" /></el-form-item>
              <el-form-item label="废弃处置" prop="disposalInfo"><el-input v-model="form.disposalInfo" type="textarea" :rows="2" placeholder="请输入废弃处置" /></el-form-item>
              <el-form-item label="运输信息" prop="transportInfo"><el-input v-model="form.transportInfo" type="textarea" :rows="2" placeholder="请输入运输信息" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></div>
          <span class="rd-detail-header-title">危化品详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.materialCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.materialCode }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></span>危化品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">危化品编号</span><div class="rd-value">{{ viewData.materialCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">危化品名称</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">危化品分类</span><div class="rd-value"><dict-tag :options="safety_hazard_class" :value="viewData.hazardClass" /></div></div>
              <div class="rd-item"><span class="rd-label">CAS号</span><div class="rd-value">{{ viewData.casNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">剧毒/易制爆</span><div class="rd-value"><span class="badge" :class="viewData.toxicFlag === '1' ? 'red' : viewData.toxicFlag === '2' ? 'amber' : 'gray'"><span class="dot"></span>{{ viewData.toxicFlag === '1' ? '剧毒' : viewData.toxicFlag === '2' ? '易制爆' : '无' }}</span></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.maxStorage != null || viewData.storageUnit || viewData.storageAreaName || viewData.currentStock != null">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg></span>储存信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">最大储存量</span><div class="rd-value">{{ viewData.maxStorage != null ? viewData.maxStorage : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">当前库存</span><div class="rd-value">{{ viewData.currentStock != null ? viewData.currentStock : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.storageUnit" /></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">储存位置</span><div class="rd-value">{{ viewData.storageAreaName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.sdsAttachment || viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>附件与备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">SDS附件</span><div class="rd-value"><div class="rd-file-links" v-if="viewData.sdsAttachment">
<div class="rd-file-item" v-for="(url, idx) in String(viewData.sdsAttachment).split(',')" :key="idx">
<div class="rd-file-link" @click="handleFilePreview(url)">
<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
<span class="rd-file-name">{{ url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url }}</span>
</div>
<span class="rd-file-dl" @click="handleFileDownload(url)">
<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
下载
</span>
</div>
            </div></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
    <file-preview ref="filePreviewRef" />
  </div>
</template>

<script setup name="SafetyMaterial">
import { listMaterial, getMaterial, addMaterial, updateMaterial, delMaterial, getStockAlert } from '@/api/safety/material'
import { listArea } from '@/api/safety/area'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, QuestionFilled, ArrowDown, Warning } from '@element-plus/icons-vue'
import FilePreview from '@/components/FilePreview/index.vue'
import { downloadFile } from '@/utils/downloadFile'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_archmaterial_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","c4","vc0","vc1","vc2","vc3","vc4"])
const { safety_hazard_class, wms_unit } = proxy.useDict('safety_hazard_class', 'wms_unit')

const materialList = ref([])
const areaOptions = ref([])
const stockAlertIds = ref([])
const baseUrl = import.meta.env.VITE_APP_BASE_API
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const defaultColumns = {
  materialCode: { label: '危化品编号', visible: true },
  materialName: { label: '危化品名称', visible: true },
  hazardClass: { label: '危化品分类', visible: true },
  casNo: { label: 'CAS号', visible: true },
  maxStorage: { label: '最大储存量', visible: true },
  storageUnit: { label: '单位', visible: true },
  storageAreaName: { label: '储存位置', visible: true },
  currentStock: { label: '当前库存', visible: true },
  toxicFlag: { label: '剧毒/易制爆', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_archmaterial_columns')
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
  queryParams: { pageNum: 1, pageSize: 10, materialCode: undefined, materialName: undefined, hazardClass: undefined, casNo: undefined, toxicFlag: undefined, status: undefined, storageAreaName: undefined, params: {} },
  rules: { materialName: [{ required: true, message: '危化品名称不能为空', trigger: 'blur' }] }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.materialCode) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.hazardClass) count++
  if (queryParams.value.casNo) count++
  if (queryParams.value.toxicFlag) count++
  if (queryParams.value.status) count++
  if (queryParams.value.storageAreaName) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function handleFilePreview(url) {
const name = url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url
proxy.$refs.filePreviewRef.open(url, name)
}
function handleFileDownload(url) {
downloadFile(url)
}

function getList() { loading.value = true; listMaterial(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { materialList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStockAlerts() }).catch(error => { console.error(error) }).finally(() => { loading.value = false }) }
function loadStockAlerts() { getStockAlert().then(response => { stockAlertIds.value = (response.data || []).map(m => m.materialId) }) }
function isStockAlert(row) { return stockAlertIds.value.includes(row.materialId) }
function getAreaTree() { listArea({ pageNum: 1, pageSize: 9999 }).then(response => { areaOptions.value = proxy.handleTree(response.rows, 'areaId') }) }
function onAreaChange(areaId) { const node = findAreaNode(areaOptions.value, areaId); form.value.storageAreaName = node ? node.areaName : undefined }
function findAreaNode(nodes, id) { for (const n of nodes) { if (n.areaId === id) return n; if (n.children && n.children.length) { const found = findAreaNode(n.children, id); if (found) return found } } return null }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.materialCode = undefined; queryParams.value.materialName = undefined; queryParams.value.hazardClass = undefined; queryParams.value.casNo = undefined; queryParams.value.toxicFlag = undefined; queryParams.value.status = undefined; queryParams.value.storageAreaName = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.materialId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; collapsedCards.c3 = false; collapsedCards.c4 = false; open.value = true; title.value = '添加危化品' }
function handleView(row) { const materialId = row.materialId || ids.value[0]; getMaterial(materialId).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function handleUpdate(row) { reset(); getMaterial(row.materialId || ids.value[0]).then(response => { form.value = response.data; collapsedCards.c1 = !response.data.maxStorage && !response.data.storageAreaName; collapsedCards.c2 = !response.data.sdsAttachment && !response.data.remark; collapsedCards.c3 = !response.data.flashPoint && !response.data.hazardSummary; collapsedCards.c4 = !response.data.physicalProperties && !response.data.firstAidMeasures; open.value = true; title.value = '修改危化品' }) }
function submitForm() {
  proxy.$refs['materialRef'].validate(valid => {
    if (valid) {
      if (form.value.materialId != undefined) { updateMaterial(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addMaterial(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const materialIds = row.materialId || ids.value; proxy.$modal.confirm('是否确认删除危化品？').then(function() { return delMaterial(materialIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('safety/material/export', { ...queryParams.value }, `material_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { materialId: undefined, materialCode: undefined, materialName: undefined, hazardClass: undefined, casNo: undefined, maxStorage: undefined, storageUnit: undefined, storageAreaId: undefined, storageAreaName: undefined, sdsAttachment: undefined, toxicFlag: '0', currentStock: undefined, safetyStockMin: undefined, safetyStockMax: undefined, flashPoint: undefined, explosionLimit: undefined, physicalProperties: undefined, hazardSummary: undefined, firstAidMeasures: undefined, fireFighting: undefined, leakHandling: undefined, storagePrecaution: undefined, exposureControl: undefined, stabilityReactivity: undefined, toxicologicalInfo: undefined, ecologicalInfo: undefined, disposalInfo: undefined, transportInfo: undefined, remark: undefined }
  proxy.resetForm('materialRef')
}

function hazardClassLabel(value) { const item = safety_hazard_class.value.find(d => d.value == value); return item ? item.label : '-' }
function hazardClassBadgeClass(value) {
  const item = safety_hazard_class.value.find(d => d.value == value)
  if (!item) return 'gray'
  const tagType = item.elTagType || item.elTagClass || ''
  const map = { 'danger': 'red', 'warning': 'amber', 'info': 'blue', 'primary': 'violet', 'success': 'green' }
  return map[tagType] || 'gray'
}

getAreaTree()
getList()
</script>

<style scoped>
.safety-archmaterial-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-archmaterial-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-archmaterial-page .filter-card { padding:14px 20px 16px; }
.safety-archmaterial-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-archmaterial-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-archmaterial-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-archmaterial-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-archmaterial-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-archmaterial-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-archmaterial-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-archmaterial-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-archmaterial-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-archmaterial-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-archmaterial-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-archmaterial-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-archmaterial-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-archmaterial-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-archmaterial-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-archmaterial-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-archmaterial-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-archmaterial-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-archmaterial-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-archmaterial-page .field .control :deep(.el-select) { width:100%; }
.safety-archmaterial-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-archmaterial-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-archmaterial-page .toolbar .left, .safety-archmaterial-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-archmaterial-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-archmaterial-page .table-wrap { overflow-x:auto; }
.safety-archmaterial-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-archmaterial-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-archmaterial-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-archmaterial-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-archmaterial-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-archmaterial-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-archmaterial-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-archmaterial-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .safety-archmaterial-page .badge.amber .dot { background:var(--amber-500); }
.safety-archmaterial-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .safety-archmaterial-page .badge.blue .dot { background:var(--blue-500); }
.safety-archmaterial-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-archmaterial-page .badge.green .dot { background:var(--green-500); }
.safety-archmaterial-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-archmaterial-page .badge.red .dot { background:var(--red-500); }
.safety-archmaterial-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-archmaterial-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-archmaterial-page .badge.gray .dot { background:var(--ink-400); }
.safety-archmaterial-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
.safety-archmaterial-page .rd-form-tip { margin-left: 4px; color: #c0c4cc; font-size: 14px; cursor: help; }
.safety-archmaterial-page .rd-form-tip:hover { color: #909399; }
@media (max-width:1100px) { .safety-archmaterial-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-archmaterial-page .filter-card .filter-bar { grid-template-columns:1fr; } }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>

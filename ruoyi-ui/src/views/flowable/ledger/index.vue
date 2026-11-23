<template>
  <div class="app-container flowable-ledger-page">
    <!-- ===== 筛选条件 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>台账名称</label>
          <div class="control">
            <el-input v-model="queryParams.ledgerName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>绑定表单</label>
          <div class="control is-select">
            <el-select v-model="queryParams.formId" placeholder="全部" clearable filterable @change="handleQuery">
              <el-option v-for="f in forms" :key="f.formId" :label="f.formName" :value="f.formId" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="启用" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 支持按名称、表单、状态筛选</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== 列表 ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['flowable:ledger:add']">新增台账</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="flowable_ledger_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table" @header-dragend="onHeaderDragEnd">
          <template v-for="colKey in sortedColumnKeys" :key="colKey">
            <el-table-column v-if="colKey === '序号'" type="index" label="序号" key="序号" :width="colWidth('序号', 70)" align="center" resizable />
            <el-table-column v-else-if="colKey === 'ledgerName' && columns.ledgerName.visible" label="台账名称" prop="ledgerName" key="ledgerName" :min-width="colWidth('ledgerName', 180)" show-overflow-tooltip resizable>
              <template #default="scope"><span class="col-strong">{{ scope.row.ledgerName }}</span></template>
            </el-table-column>
            <el-table-column v-else-if="colKey === 'formName' && columns.formName.visible" label="绑定表单" prop="formName" key="formName" :min-width="colWidth('formName', 180)" show-overflow-tooltip resizable>
              <template #default="scope">{{ scope.row.formName || scope.row.formKey || '—' }}</template>
            </el-table-column>
            <el-table-column v-else-if="colKey === 'processKey' && columns.processKey.visible" label="流程Key" prop="processKey" key="processKey" :min-width="colWidth('processKey', 160)" show-overflow-tooltip resizable>
              <template #default="scope"><span class="col-mono">{{ scope.row.processKey || '—' }}</span></template>
            </el-table-column>
            <el-table-column v-else-if="colKey === 'tableName' && columns.tableName.visible" label="数据表" prop="tableName" key="tableName" :min-width="colWidth('tableName', 180)" show-overflow-tooltip resizable>
              <template #default="scope"><span class="col-mono">{{ scope.row.tableName || '—' }}</span></template>
            </el-table-column>
            <el-table-column v-else-if="colKey === 'exportFlag' && columns.exportFlag.visible" label="允许导出" prop="exportFlag" key="exportFlag" :width="colWidth('exportFlag', 110)" align="center" resizable>
              <template #default="scope">
                <span class="badge" :class="scope.row.exportFlag === '1' ? 'green' : 'gray'">
                  <span class="dot"></span>{{ scope.row.exportFlag === '1' ? '允许' : '禁止' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column v-else-if="colKey === 'status' && columns.status.visible" label="状态" prop="status" key="status" :width="colWidth('status', 100)" align="center" resizable>
              <template #default="scope">
                <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'">
                  <span class="dot"></span>{{ scope.row.status === '0' ? '启用' : '停用' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column v-else-if="colKey === 'orderNum' && columns.orderNum.visible" label="排序" prop="orderNum" key="orderNum" :width="colWidth('orderNum', 80)" align="center" resizable />
          </template>
          <el-table-column label="操作" width="230" align="center" fixed="right">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['flowable:ledger:edit']">编辑</el-button>
                <el-button link type="primary" icon="Key" @click="openGrant(scope.row)" v-hasPermi="['flowable:ledger:grant']">授权</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['flowable:ledger:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-container">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
          v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" @change="getList" />
      </div>
    </div>

    <!-- ===== 台账配置向导 ===== -->
    <el-dialog v-model="open" width="1100px" top="6vh" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon><Grid /></el-icon></div>
          <span class="rd-detail-header-title">{{ form.ledgerId ? ('编辑台账 · ' + (form.ledgerName || '')) : '新增台账' }}</span>
        </div>
      </template>

      <!-- 顶部步骤条：编辑时可直接点击任意步骤切换 -->
      <div class="wizard-steps">
        <template v-for="(s, i) in steps" :key="s.key">
          <div class="wizard-step"
            :class="{ active: wizardStep === i, done: wizardStep > i, locked: i > 0 && !form.ledgerId }"
            @click="jumpStep(i)">
            <span class="step-no">
              <el-icon v-if="wizardStep > i"><Check /></el-icon>
              <template v-else>{{ i + 1 }}</template>
            </span>
            <span class="step-name">{{ s.title }}</span>
          </div>
          <div v-if="i < steps.length - 1" class="step-line" :class="{ done: wizardStep > i }"></div>
        </template>
      </div>

      <div class="wizard-body">
        <!-- 步骤1：基本信息 -->
        <div v-show="wizardStep === 0">
          <el-form ref="ledgerRef" :model="form" :rules="rules" label-width="100px" size="small">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="台账名称" prop="ledgerName">
                  <el-input v-model="form.ledgerName" placeholder="如：费用报销台账" maxlength="100" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="绑定表单" prop="formId">
                  <el-select v-model="form.formId" placeholder="请选择流程表单" filterable style="width:100%" @change="onFormChange">
                    <el-option v-for="f in forms" :key="f.formId" :label="f.formName + '（' + (f.formKey || '') + '）'" :value="f.formId" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="流程分类" prop="category">
                  <el-select v-model="form.category" placeholder="请选择" clearable style="width:100%">
                    <el-option v-for="d in categoryOptions" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="显示顺序" prop="orderNum">
                  <el-input-number v-model="form.orderNum" :min="0" :max="999" controls-position="right" style="width:160px" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="允许导出">
                  <el-switch v-model="form.exportFlag" active-value="1" inactive-value="0" />
                  <span class="form-tip" style="margin-left:12px">关闭后所有授权均不可导出</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态">
                  <el-radio-group v-model="form.status">
                    <el-radio value="0">启用</el-radio>
                    <el-radio value="1">停用</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="台账说明">
                  <el-input v-model="form.ledgerDesc" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="台账用途说明（选填）" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <div v-if="form.ledgerId" class="base-info-strip">
            <div class="info-cell"><span class="k">台账标识</span><span class="v mono">{{ form.ledgerKey || '—' }}</span></div>
            <div class="info-cell"><span class="k">流程Key</span><span class="v mono">{{ form.processKey || '—' }}</span></div>
            <div class="info-cell"><span class="k">数据表</span><span class="v mono">{{ form.tableName || '—' }}</span></div>
          </div>
          <div class="step-tip">保存基本信息后即可继续配置显示列、排序、查询条件与数据权限；编辑时可直接点击顶部步骤任意切换。</div>
        </div>

        <!-- 步骤2：显示列 -->
        <div v-show="wizardStep === 1">
          <div class="transfer transfer-lg">
            <div class="panel">
              <div class="panel-head">
                <span>待选字段 <span class="count">{{ availableFields.length }}</span></span>
                <el-input v-model="fieldKeyword" size="small" placeholder="搜索字段" clearable style="width:150px">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
              <div class="panel-body">
                <div v-for="f in availableFields" :key="f.source + ':' + f.field" class="field-item" @click="addField(f)">
                  <span class="f-label">{{ f.label }}</span>
                  <span class="mono">{{ f.field }}</span>
                  <el-tag size="small" :type="f.source === 'sys' ? 'info' : 'success'">{{ f.source === 'sys' ? '系统' : '业务' }}</el-tag>
                </div>
                <div v-if="!availableFields.length" class="empty-tip">{{ form.formId ? '字段已全部添加' : '请先选择绑定表单' }}</div>
              </div>
            </div>
            <div class="panel">
              <div class="panel-head">
                <span>已选列 <span class="count">{{ editColumns.length }}</span></span>
                <div class="head-ops">
                  <span class="head-tip">拖动排序</span>
                  <el-button link type="primary" size="small" icon="RefreshLeft" @click="resetDefaultColumns">恢复默认列</el-button>
                </div>
              </div>
              <div class="panel-body">
                <div ref="selectedColListRef" class="drag-list">
                  <div v-for="(c, i) in editColumns" :key="c.source + ':' + c.field" class="selected-item">
                    <el-icon class="drag-handle" title="按住拖动调整顺序"><Rank /></el-icon>
                    <span class="idx">{{ i + 1 }}</span>
                    <span class="f-label">{{ c.label }}</span>
                    <span class="mono">{{ c.field }}</span>
                    <el-button link type="danger" size="small" icon="Delete" class="row-del" @click="editColumns.splice(i, 1)" />
                  </div>
                </div>
                <div v-if="!editColumns.length" class="empty-tip">从左侧点击字段添加到台账列</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 步骤3：数据排序 -->
        <div v-show="wizardStep === 2">
          <div class="transfer transfer-lg">
            <div class="panel">
              <div class="panel-head">
                <span>待选字段 <span class="count">{{ unsortableColumns.length }}</span></span>
                <el-input v-model="sortFieldKeyword" size="small" placeholder="搜索字段" clearable style="width:150px">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
              <div class="panel-body">
                <div v-for="c in unsortableColumns" :key="c.source + ':' + c.field" class="field-item" @click="addSortField(c)">
                  <span class="f-label">{{ c.label || c.field }}</span>
                  <span class="mono">{{ c.field }}</span>
                  <el-icon class="add-icon"><Plus /></el-icon>
                </div>
                <div v-if="!unsortableColumns.length" class="empty-tip">{{ editColumns.length ? '字段已全部添加' : '请先在「显示列」中配置台账列' }}</div>
              </div>
            </div>
            <div class="panel">
              <div class="panel-head">
                <span>已选排序 <span class="count">{{ sortableColumns.length }}</span></span>
                <div class="head-ops">
                  <span class="head-tip">拖动排序</span>
                  <el-button v-if="sortableColumns.length" link type="primary" size="small" @click="clearSortFields">清空</el-button>
                </div>
              </div>
              <div class="panel-body">
                <div ref="sortSelectedListRef" class="drag-list">
                  <div v-for="(c, i) in sortableColumns" :key="c.source + ':' + c.field" class="selected-item">
                    <el-icon class="drag-handle" title="按住拖动调整顺序"><Rank /></el-icon>
                    <span class="idx">{{ i + 1 }}</span>
                    <span class="f-label">{{ c.label || c.field }}</span>
                    <span class="mono">{{ c.field }}</span>
                    <el-tag v-if="i === 0" size="small" type="primary" effect="plain" class="default-tag">默认</el-tag>
                    <el-select v-model="c.sortOrder" size="small" class="dir-select">
                      <el-option label="正序" value="asc" />
                      <el-option label="倒序" value="desc" />
                    </el-select>
                    <el-button link type="danger" size="small" icon="Delete" class="row-del" @click="removeSortField(c)" />
                  </div>
                </div>
                <div v-if="!sortableColumns.length" class="empty-tip">从左侧点击字段添加排序</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 步骤4：查询条件 -->
        <div v-show="wizardStep === 3">
          <div class="transfer transfer-lg">
            <div class="panel">
              <div class="panel-head">
                <span>待选字段 <span class="count">{{ unfilterableColumns.length }}</span></span>
                <el-input v-model="filterFieldKeyword" size="small" placeholder="搜索字段" clearable style="width:150px">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
              <div class="panel-body">
                <div v-for="c in unfilterableColumns" :key="c.source + ':' + c.field" class="field-item" @click="addFilterField(c)">
                  <span class="f-label">{{ c.label || c.field }}</span>
                  <span class="mono">{{ c.field }}</span>
                  <el-icon class="add-icon"><Plus /></el-icon>
                </div>
                <div v-if="!unfilterableColumns.length" class="empty-tip">{{ editColumns.length ? '字段已全部添加' : '请先在「显示列」中配置台账列' }}</div>
              </div>
            </div>
            <div class="panel">
              <div class="panel-head">
                <span>已选筛选 <span class="count">{{ filterColumns.length }}</span></span>
                <div class="head-ops">
                  <span class="head-tip">拖动排序</span>
                  <el-button v-if="filterColumns.length" link type="primary" size="small" @click="clearFilterFields">清空</el-button>
                </div>
              </div>
              <div class="panel-body">
                <div ref="filterSelectedListRef" class="drag-list">
                  <div v-for="(c, i) in filterColumns" :key="c.source + ':' + c.field" class="selected-item">
                    <el-icon class="drag-handle" title="按住拖动调整顺序"><Rank /></el-icon>
                    <span class="idx">{{ i + 1 }}</span>
                    <span class="f-label">{{ c.label || c.field }}</span>
                    <span class="mono">{{ c.field }}</span>
                    <el-tag v-if="i < 4" size="small" type="success" effect="plain" class="default-tag">前台显示</el-tag>
                    <el-button link type="danger" size="small" icon="Delete" class="row-del" @click="removeFilterField(c)" />
                  </div>
                </div>
                <div v-if="!filterColumns.length" class="empty-tip">从左侧点击字段添加筛选条件</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 步骤5：数据权限 -->
        <div v-show="wizardStep === 4">
          <div class="step-section-title">数据查询权限</div>
          <div class="toolbar grant-toolbar">
            <div class="left">
              <el-button type="primary" plain size="small" icon="Plus" :disabled="!form.ledgerId" @click="openGrantForm()">新增授权</el-button>
            </div>
            <div class="right"><span class="form-tip">多条授权的数据范围取并集；未配置授权时仅超级管理员可见</span></div>
          </div>
          <el-table :data="grants" border size="small" v-loading="grantLoading" max-height="330">
            <el-table-column label="授权对象" min-width="180">
              <template #default="scope">
                <el-tag size="small" :type="grantTypeTag(scope.row.grantType)">{{ grantTypeLabel(scope.row.grantType) }}</el-tag>
                <span style="margin-left:6px">{{ scope.row.grantName || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="数据范围" min-width="180">
              <template #default="scope">{{ scopeLabel(scope.row) }}</template>
            </el-table-column>
            <el-table-column label="允许导出" width="100" align="center">
              <template #default="scope">
                <span class="badge" :class="scope.row.canExport === '1' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.canExport === '1' ? '允许' : '禁止' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="130" align="center">
              <template #default="scope">
                <el-button link type="primary" icon="Edit" @click="openGrantForm(scope.row)">编辑</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDeleteGrant(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!form.ledgerId" class="step-tip">请先在「基本信息」中保存台账，再配置授权。</div>
        </div>

        <!-- 步骤6：设置完成 -->
        <div v-show="wizardStep === 5">
          <div class="finish-card">
            <div class="finish-title">配置完成</div>
            <div class="finish-desc">请检查以下配置，确认无误后点击「完成并保存」。</div>
            <div class="summary-grid">
              <div class="summary-item"><span class="k">台账名称</span><span class="v">{{ form.ledgerName || '—' }}</span></div>
              <div class="summary-item"><span class="k">绑定表单</span><span class="v">{{ formFormName }}</span></div>
              <div class="summary-item"><span class="k">可排序</span><span class="v">{{ sortableColumns.length }} 列</span></div>
              <div class="summary-item"><span class="k">可筛选</span><span class="v">{{ filterColumns.length }} 列</span></div>
              <div class="summary-item"><span class="k">授权数量</span><span class="v">{{ grantsLoaded ? grants.length + ' 条' : '—' }}</span></div>
            </div>
            <div class="preview-tags">
              <span class="k">列顺序预览</span>
              <el-tag v-for="c in editColumns" :key="c.source + ':' + c.field" effect="plain" size="small">
                {{ c.label || c.field }}
              </el-tag>
              <span v-if="!editColumns.length" class="empty-inline">尚未配置台账列</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="wizard-footer">
          <el-button @click="open = false">取 消</el-button>
          <div class="right">
            <el-button v-if="wizardStep > 0" icon="ArrowLeft" @click="goStep(wizardStep - 1)">上一步</el-button>
            <el-button v-if="wizardStep > 0 && wizardStep < steps.length - 1" type="primary" plain @click="nextStep">下一步</el-button>
            <el-button v-if="!form.ledgerId && wizardStep === 0" type="primary" plain :loading="saving" @click="submitForm(true)">保存并进入详细设置</el-button>
            <el-button v-if="wizardStep < steps.length - 1" type="primary" :loading="saving" @click="submitForm(false)">保 存</el-button>
            <el-button v-else type="primary" :loading="saving" @click="submitForm(false)">完成并保存</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 列表快捷授权弹窗 ===== -->
    <el-dialog v-model="grantOpen" width="860px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon><Key /></el-icon></div>
          <span class="rd-detail-header-title">数据查询权限 · {{ grantLedger.ledgerName }}</span>
        </div>
      </template>
      <div class="toolbar grant-toolbar">
        <div class="left">
          <el-button type="primary" plain size="small" icon="Plus" @click="openGrantForm()">新增授权</el-button>
        </div>
        <div class="right"><span class="form-tip">多条授权的数据范围取并集</span></div>
      </div>
      <el-table :data="grants" border size="small" v-loading="grantLoading" max-height="360">
        <el-table-column label="授权对象" min-width="180">
          <template #default="scope">
            <el-tag size="small" :type="grantTypeTag(scope.row.grantType)">{{ grantTypeLabel(scope.row.grantType) }}</el-tag>
            <span style="margin-left:6px">{{ scope.row.grantName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数据范围" min-width="180">
          <template #default="scope">{{ scopeLabel(scope.row) }}</template>
        </el-table-column>
        <el-table-column label="允许导出" width="100" align="center">
          <template #default="scope">
            <span class="badge" :class="scope.row.canExport === '1' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.canExport === '1' ? '允许' : '禁止' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="openGrantForm(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDeleteGrant(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="grantOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 授权新增/编辑 -->
    <el-dialog v-model="grantFormOpen" width="640px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon><Key /></el-icon></div>
          <span class="rd-detail-header-title">{{ grantForm.grantId ? '编辑授权' : '新增授权' }}</span>
        </div>
      </template>
      <el-form ref="grantRef" :model="grantForm" :rules="grantRules" label-width="110px" size="small">
        <el-form-item label="授权给" prop="grantType">
          <el-radio-group v-model="grantForm.grantType" @change="onGrantTypeChange">
            <el-radio value="all">全员</el-radio>
            <el-radio value="company">公司</el-radio>
            <el-radio value="dept">部门</el-radio>
            <el-radio value="role">角色</el-radio>
            <el-radio value="user">人员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="grantForm.grantType !== 'all'" label="授权对象" prop="targetId">
          <el-input :model-value="grantTargetName" readonly placeholder="点击右侧按钮选择" @click="openTargetPicker">
            <template #suffix><el-icon class="picker-icon" @click="openTargetPicker"><Search /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="可见数据范围" prop="scopeType">
          <el-select v-model="grantForm.scopeType" style="width:100%">
            <el-option v-for="s in scopeOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="grantForm.scopeType === 'customDept'" label="自定义部门">
          <div class="picker-readonly" @click="openCustomDeptPicker">
            <span v-if="customDepts.length === 0" class="placeholder">点击选择部门</span>
            <el-tag v-for="d in customDepts" :key="d.deptId" size="small" style="margin:2px">{{ d.deptName }}</el-tag>
          </div>
          <div style="margin-top:6px">
            <el-checkbox v-model="includeChild">包含下级部门</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item v-if="grantForm.scopeType === 'customUser'" label="自定义人员">
          <div class="picker-readonly" @click="openCustomUserPicker">
            <span v-if="customUsers.length === 0" class="placeholder">点击选择人员</span>
            <el-tag v-for="u in customUsers" :key="u.userId" size="small" style="margin:2px">{{ u.nickName }}</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="允许导出">
          <el-switch v-model="grantForm.canExport" active-value="1" inactive-value="0" />
          <span class="form-tip" style="margin-left:12px">台账总开关关闭时仍不可导出</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="grantSaving" @click="submitGrant">保 存</el-button>
        <el-button @click="grantFormOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 选择器 -->
    <DeptPicker ref="deptPickerRef" :title="deptPickerTitle" @confirm="onDeptPicked" />
    <DeptPicker ref="customDeptPickerRef" title="选择自定义部门" multiple @confirm="onCustomDeptPicked" />
    <RolePicker ref="rolePickerRef" @confirm="onRolePicked" />
    <UserPicker ref="userPickerRef" @confirm="onUserPicked" />
    <UserPicker ref="customUserPickerRef" title="选择自定义人员" :multiple="true" @confirm="onUserPickedMulti" />
  </div>
</template>

<script setup name="FlowLedgerConfig">
import { listLedger, getLedger, addLedger, updateLedger, delLedger, ledgerFields, listGrant, addGrant, updateGrant, delGrant } from '@/api/flowable/ledger'
import { allForms } from '@/api/flowable/form'
import DeptPicker from '@/components/DeptPicker/index.vue'
import RolePicker from '@/components/RolePicker/index.vue'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useColumnDrag } from '@/composables/useColumnDrag'
import Sortable from 'sortablejs'
import { Search, Filter } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { flowable_category } = proxy.useDict('flowable_category')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('flowable_ledger_index')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const forms = ref([])

const columns = ref({
  ledgerName: { label: '台账名称', visible: true },
  formName: { label: '绑定表单', visible: true },
  processKey: { label: '流程Key', visible: true },
  tableName: { label: '数据表', visible: true },
  exportFlag: { label: '允许导出', visible: true },
  status: { label: '状态', visible: true },
  orderNum: { label: '排序', visible: true }
})
// 列顺序拖拽持久化（序号列也可拖动，与列宽拖拽共存）
const { sortedColumnKeys, initSortable } = useColumnDrag(
  'flowable_ledger_index',
  ['序号', 'ledgerName', 'formName', 'processKey', 'tableName', 'exportFlag', 'status', 'orderNum'],
  tableRef
)
// 列显隐变化会改变实际渲染的列数量，需重新绑定拖拽以免索引错位
watch(columns, () => { initSortable() }, { deep: true })

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, ledgerName: undefined, formId: undefined, status: undefined }
})
const { queryParams } = toRefs(data)

const categoryOptions = computed(() => (flowable_category.value || []).map(d => ({ label: d.label, value: d.value })))

function getList() {
  loading.value = true
  listLedger(queryParams.value).then(res => {
    list.value = res.rows || []
    total.value = res.total
    loading.value = false
    applySavedWidths()
    initSortable()
  }).finally(() => { loading.value = false })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }

function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, ledgerName: undefined, formId: undefined, status: undefined }
  getList()
}

allForms().then(res => { forms.value = res.data || [] })

// ================= 台账配置向导 =================
const steps = [
  { key: 'basic', title: '基本信息' },
  { key: 'columns', title: '显示列' },
  { key: 'sort', title: '数据排序' },
  { key: 'query', title: '查询条件' },
  { key: 'grant', title: '数据权限' },
  { key: 'done', title: '设置完成' }
]

const open = ref(false)
const saving = ref(false)
const ledgerRef = ref(null)
const wizardStep = ref(0)
const editColumns = ref([])
const fieldOptions = ref([])
const fieldKeyword = ref('')
const sortFieldKeyword = ref('')
const filterFieldKeyword = ref('')
const selectedColListRef = ref(null)
const sortSelectedListRef = ref(null)
const filterSelectedListRef = ref(null)

function emptyForm() {
  return {
    ledgerId: undefined, ledgerKey: undefined, ledgerName: undefined, formId: undefined, category: undefined,
    exportFlag: '1', status: '0', orderNum: 0, ledgerDesc: undefined,
    processKey: undefined, tableName: undefined
  }
}

const form = ref(emptyForm())

const rules = {
  ledgerName: [{ required: true, message: '台账名称不能为空', trigger: 'blur' }],
  formId: [{ required: true, message: '请选择绑定表单', trigger: 'change' }]
}

/** 默认列：流程编号/标题/状态/发起人/发起部门/发起时间 */
const DEFAULT_FIELDS = ['processNo', 'title', 'status', 'startUserName', 'startDeptName', 'startTime']

function defaultColumns() {
  return DEFAULT_FIELDS.map(field => {
    const opt = fieldOptions.value.find(f => f.source === 'sys' && f.field === field)
    return {
      field, source: 'sys', label: opt ? opt.label : field, width: field === 'title' ? 220 : 150,
      sortable: ['startTime', 'processNo'].includes(field), sortOrder: 'desc',
      filterable: ['title', 'processNo', 'startUserName'].includes(field),
      align: 'left', format: ['startTime', 'endTime'].includes(field) ? 'datetime' : 'text'
    }
  })
}

/** 待选字段（过滤已添加项 + 关键字） */
const availableFields = computed(() => {
  const kw = (fieldKeyword.value || '').trim().toLowerCase()
  return fieldOptions.value.filter(f => {
    const added = editColumns.value.some(c => c.source === f.source && c.field === f.field)
    if (added) return false
    if (!kw) return true
    return (f.label || '').toLowerCase().includes(kw) || (f.field || '').toLowerCase().includes(kw)
  })
})

const sortableColumns = computed(() => editColumns.value.filter(c => c.sortable))
const filterColumns = computed(() => editColumns.value.filter(c => c.filterable))

function matchField(c, kw) {
  if (!kw) return true
  return (c.label || '').toLowerCase().includes(kw) || (c.field || '').toLowerCase().includes(kw)
}
const unsortableColumns = computed(() => {
  const kw = (sortFieldKeyword.value || '').trim().toLowerCase()
  return editColumns.value.filter(c => !c.sortable && matchField(c, kw))
})
const unfilterableColumns = computed(() => {
  const kw = (filterFieldKeyword.value || '').trim().toLowerCase()
  return editColumns.value.filter(c => !c.filterable && matchField(c, kw))
})

const formFormName = computed(() => {
  const f = forms.value.find(x => x.formId === form.value.formId)
  return f ? f.formName : (form.value.formId || '—')
})

function handleAdd() {
  form.value = emptyForm()
  editColumns.value = []
  fieldOptions.value = []
  fieldKeyword.value = ''
  sortFieldKeyword.value = ''
  filterFieldKeyword.value = ''
  wizardStep.value = 0
  grants.value = []
  grantsLoaded.value = false
  open.value = true
  nextTick(() => ledgerRef.value && ledgerRef.value.clearValidate())
  initWizardDrag()
}

async function handleEdit(row) {
  const res = await getLedger(row.ledgerId)
  const d = res.data || {}
  let sortCfg = {}
  try { sortCfg = d.queryJson ? JSON.parse(d.queryJson) : {} } catch (e) { sortCfg = {} }
  form.value = {
    ledgerId: d.ledgerId, ledgerKey: d.ledgerKey, ledgerName: d.ledgerName, formId: d.formId, category: d.category,
    exportFlag: d.exportFlag || '1', status: d.status || '0', orderNum: d.orderNum || 0,
    ledgerDesc: d.ledgerDesc, processKey: d.processKey, tableName: d.tableName
  }
  editColumns.value = parseColumns(d.columnsJson)
  // 恢复各排序字段的升降序（后端会规范化 columnsJson，方向冗余存放在 queryJson.sorts）
  const savedSorts = Array.isArray(sortCfg.sorts) ? sortCfg.sorts : []
  editColumns.value.forEach(c => {
    if (!c.sortable) return
    const hit = savedSorts.find(s => s.field === c.field && (s.source || c.source) === c.source)
    if (hit) c.sortOrder = hit.order === 'asc' ? 'asc' : 'desc'
  })
  // 恢复默认排序字段的方向，并置于可排序字段首位
  const def = editColumns.value.find(c => c.sortable && c.field === sortCfg.defaultSort)
  if (def) {
    def.sortOrder = sortCfg.defaultSortOrder === 'asc' ? 'asc' : 'desc'
    const arr = editColumns.value
    arr.splice(arr.indexOf(def), 1)
    const firstSortable = arr.findIndex(c => c.sortable)
    arr.splice(firstSortable < 0 ? arr.length : firstSortable, 0, def)
  }
  await loadFields(d.formId)
  fieldKeyword.value = ''
  sortFieldKeyword.value = ''
  filterFieldKeyword.value = ''
  wizardStep.value = 0
  grants.value = []
  grantsLoaded.value = false
  open.value = true
  nextTick(() => ledgerRef.value && ledgerRef.value.clearValidate())
  initWizardDrag()
}

function parseColumns(json) {
  try {
    const arr = JSON.parse(json || '[]')
    return arr.map(c => ({
      field: c.field, source: c.source || 'main', label: c.label || c.field,
      width: c.width || 140, sortable: !!c.sortable,
      sortOrder: c.sortOrder === 'asc' ? 'asc' : 'desc',
      filterable: !!c.filterable, align: c.align || 'left', format: c.format || ''
    }))
  } catch (e) { return [] }
}

async function loadFields(formId) {
  if (!formId) { fieldOptions.value = []; return }
  const res = await ledgerFields(formId)
  fieldOptions.value = res.data || []
}

async function onFormChange(formId) {
  await loadFields(formId)
  if (editColumns.value.length === 0) {
    editColumns.value = defaultColumns()
  } else {
    // 换表单后仅保留新表单存在的字段，避免提交不存在的列
    const valid = new Set(fieldOptions.value.map(f => f.source + ':' + f.field))
    editColumns.value = editColumns.value.filter(c => valid.has(c.source + ':' + c.field))
  }
}

function resetDefaultColumns() {
  if (editColumns.value.length && !window.confirm('将覆盖当前列配置，确认恢复默认列？')) return
  editColumns.value = defaultColumns()
}

function addField(f) {
  if (editColumns.value.some(c => c.source === f.source && c.field === f.field)) return
  editColumns.value.push({
    field: f.field, source: f.source, label: f.label || f.field, width: 140,
    sortable: false, sortOrder: 'desc', filterable: false, align: 'left',
    format: f.type && ['date', 'time', 'datetime'].includes(f.type)
      ? f.type
      : (f.source === 'sys' && String(f.field).endsWith('Time') ? 'datetime' : '')
  })
}

function addSortField(c) {
  c.sortable = true
  c.sortOrder = c.sortOrder || 'desc'
}
function removeSortField(c) { c.sortable = false }
function clearSortFields() { editColumns.value.forEach(c => { c.sortable = false }) }

function addFilterField(c) { c.filterable = true }
function removeFilterField(c) { c.filterable = false }
function clearFilterFields() { editColumns.value.forEach(c => { c.filterable = false }) }

/** 在子集（可排序/可筛选）内移动后，同步调整 editColumns 中的相对位置 */
function moveInSubset(subset, oldIndex, newIndex) {
  const keyOf = x => x.source + ':' + x.field
  const fromItem = subset[oldIndex]
  const toItem = subset[newIndex]
  if (!fromItem || !toItem) return
  const arr = editColumns.value
  const fromIdx = arr.findIndex(x => keyOf(x) === keyOf(fromItem))
  if (fromIdx < 0) return
  const [moved] = arr.splice(fromIdx, 1)
  const toIdx = arr.findIndex(x => keyOf(x) === keyOf(toItem))
  if (toIdx < 0) { arr.splice(fromIdx, 0, moved); return }
  arr.splice(newIndex > oldIndex ? toIdx + 1 : toIdx, 0, moved)
}

/** 三个已选列表的拖拽排序（SortableJS） */
let listSortables = []
function destroyListSortables() {
  listSortables.forEach(s => s.destroy())
  listSortables = []
}
function createDragList(el, onReorder) {
  if (!el) return
  let snapshot = []
  const inst = Sortable.create(el, {
    animation: 150,
    handle: '.drag-handle',
    ghostClass: 'drag-ghost',
    chosenClass: 'drag-chosen',
    onStart: () => { snapshot = Array.from(el.children) },
    onEnd: (evt) => {
      // 先还原 DOM，交由 Vue 按数据顺序重新渲染
      snapshot.forEach(node => el.appendChild(node))
      if (evt.oldIndex === evt.newIndex) return
      onReorder(evt.oldIndex, evt.newIndex)
    }
  })
  listSortables.push(inst)
}
function initWizardDrag() {
  destroyListSortables()
  nextTick(() => {
    createDragList(selectedColListRef.value, (o, n) => {
      const arr = editColumns.value
      const [moved] = arr.splice(o, 1)
      arr.splice(n, 0, moved)
    })
    createDragList(sortSelectedListRef.value, (o, n) => moveInSubset(sortableColumns.value, o, n))
    createDragList(filterSelectedListRef.value, (o, n) => moveInSubset(filterColumns.value, o, n))
  })
}
onBeforeUnmount(destroyListSortables)

function jumpStep(i) {
  if (i === wizardStep.value) return
  if (i > 0 && !form.value.ledgerId) {
    proxy.$modal.msgWarning('请先在「基本信息」中保存台账，再配置后续内容')
    return
  }
  goStep(i)
}

function goStep(i) {
  wizardStep.value = Math.min(steps.length - 1, Math.max(0, i))
  if (wizardStep.value === 4) {
    prepareGrants()
  }
}

function nextStep() {
  if (wizardStep.value === 0) { submitForm(true); return }
  goStep(wizardStep.value + 1)
}

function buildPayload() {
  const sortList = editColumns.value.filter(c => c.sortable)
  const defaultSort = sortList.length ? sortList[0].field : 'startTime'
  const defaultSortOrder = sortList.length && sortList[0].sortOrder === 'asc' ? 'asc' : 'desc'
  return {
    ledgerId: form.value.ledgerId,
    ledgerName: form.value.ledgerName,
    formId: form.value.formId,
    category: form.value.category,
    ledgerDesc: form.value.ledgerDesc,
    exportFlag: form.value.exportFlag,
    status: form.value.status,
    orderNum: form.value.orderNum,
    columnsJson: JSON.stringify(editColumns.value.map(c => ({
      field: c.field, source: c.source, label: c.label, width: c.width,
      sortable: c.sortable, sortOrder: c.sortable ? (c.sortOrder || 'desc') : undefined,
      filterable: c.filterable, align: c.align, format: c.format || undefined
    }))),
    queryJson: JSON.stringify({
      defaultSort,
      defaultSortOrder,
      sorts: sortList.map(c => ({ field: c.field, source: c.source, order: c.sortOrder === 'asc' ? 'asc' : 'desc' }))
    })
  }
}

async function submitForm(enterDetail) {
  try {
    await ledgerRef.value.validate()
  } catch (e) {
    wizardStep.value = 0
    return
  }
  if (editColumns.value.length === 0) {
    proxy.$modal.msgWarning('请至少添加一个台账列')
    wizardStep.value = 1
    return
  }
  saving.value = true
  try {
    if (form.value.ledgerId) {
      await updateLedger(buildPayload())
      proxy.$modal.msgSuccess('保存成功')
    } else {
      const res = await addLedger(buildPayload())
      const created = res.data || {}
      form.value.ledgerId = created.ledgerId
      form.value.ledgerKey = created.ledgerKey
      form.value.processKey = created.processKey
      form.value.tableName = created.tableName
      if (created.category) form.value.category = created.category
      proxy.$modal.msgSuccess(enterDetail ? '基本信息已保存，请继续配置' : '新增成功')
    }
    getList()
    if (enterDetail) {
      goStep(1)
    } else {
      open.value = false
    }
  } catch (e) {
    proxy.$modal.msgError('保存失败：' + (e?.message || e || '请稍后重试'))
  } finally {
    saving.value = false
  }
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除台账"' + row.ledgerName + '？其授权配置将一并删除。').then(() => delLedger(row.ledgerId)).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('删除失败：' + (e?.message || e)) })
}

// ================= 授权配置 =================
const grantOpen = ref(false)
const grantLoading = ref(false)
const grantLedger = ref({})
const grants = ref([])
const grantsLoaded = ref(false)

const grantFormOpen = ref(false)
const grantSaving = ref(false)
const grantRef = ref(null)
const grantForm = ref({})
const grantTargetName = ref('')
const customDepts = ref([])
const customUsers = ref([])
const includeChild = ref(true)

const deptPickerRef = ref(null)
const customDeptPickerRef = ref(null)
const rolePickerRef = ref(null)
const userPickerRef = ref(null)
const customUserPickerRef = ref(null)
const deptPickerTitle = ref('选择部门')

const grantRules = {
  grantType: [{ required: true, message: '请选择授权对象类型', trigger: 'change' }],
  targetId: [{ required: true, message: '请选择授权对象', trigger: 'change' }],
  scopeType: [{ required: true, message: '请选择数据范围', trigger: 'change' }]
}

const scopeOptions = [
  { label: '全部数据', value: 'all' },
  { label: '仅本人发起', value: 'self' },
  { label: '本部门', value: 'dept' },
  { label: '本部门及下级', value: 'deptAndChild' },
  { label: '本公司', value: 'company' },
  { label: '本公司及下级', value: 'companyAndChild' },
  { label: '自定义部门', value: 'customDept' },
  { label: '自定义人员', value: 'customUser' }
]

const GRANT_TYPE_LABELS = { all: '全员', company: '公司', dept: '部门', role: '角色', user: '人员' }

function grantTypeLabel(t) { return GRANT_TYPE_LABELS[t] || t }
function grantTypeTag(t) {
  return { all: 'info', company: 'warning', dept: 'success', role: 'primary', user: 'danger' }[t] || 'info'
}

function scopeLabel(row) {
  const map = {
    all: '全部数据', self: '仅本人发起', dept: '本部门', deptAndChild: '本部门及下级',
    company: '本公司', companyAndChild: '本公司及下级'
  }
  if (map[row.scopeType]) return map[row.scopeType]
  try {
    const json = row.scopeJson ? JSON.parse(row.scopeJson) : {}
    if (row.scopeType === 'customDept') return '自定义部门（' + (json.deptIds || []).length + '个' + (json.includeChild ? '，含下级' : '') + '）'
    if (row.scopeType === 'customUser') return '自定义人员（' + (json.userIds || []).length + '人）'
  } catch (e) { /* ignore */ }
  return row.scopeType || '—'
}

function openGrant(row) {
  grantLedger.value = row
  grantOpen.value = true
  loadGrants()
}

/** 向导内进入数据权限步骤：绑定当前台账并加载授权 */
function prepareGrants() {
  if (!form.value.ledgerId) return
  grantLedger.value = { ledgerId: form.value.ledgerId, ledgerName: form.value.ledgerName }
  loadGrants()
}

function loadGrants() {
  if (!grantLedger.value || !grantLedger.value.ledgerId) return
  grantLoading.value = true
  listGrant(grantLedger.value.ledgerId).then(res => {
    grants.value = res.data || []
    grantsLoaded.value = true
  }).finally(() => { grantLoading.value = false })
}

function openGrantForm(row) {
  if (row) {
    grantForm.value = { ...row }
    grantTargetName.value = row.grantName || ''
    const json = row.scopeJson ? JSON.parse(row.scopeJson) : {}
    customDepts.value = (json.deptIds || []).map((id, i) => ({ deptId: id, deptName: (json.deptNames || [])[i] || ('部门' + id) }))
    customUsers.value = (json.userIds || []).map((id, i) => ({ userId: id, nickName: (json.userNames || [])[i] || ('人员' + id) }))
    includeChild.value = json.includeChild !== false
  } else {
    grantForm.value = {
      ledgerId: grantLedger.value.ledgerId, grantType: 'all', targetId: undefined,
      scopeType: 'self', canExport: '0'
    }
    grantTargetName.value = ''
    customDepts.value = []
    customUsers.value = []
    includeChild.value = true
  }
  grantFormOpen.value = true
  nextTick(() => grantRef.value && grantRef.value.clearValidate())
}

function onGrantTypeChange(type) {
  grantForm.value.targetId = undefined
  grantTargetName.value = ''
  if (type === 'all') {
    grantRef.value && grantRef.value.clearValidate('targetId')
  }
}

function openTargetPicker() {
  const type = grantForm.value.grantType
  if (type === 'company' || type === 'dept') {
    deptPickerTitle.value = type === 'company' ? '选择公司' : '选择部门'
    deptPickerRef.value.open(grantForm.value.targetId)
  } else if (type === 'role') {
    rolePickerRef.value.open(grantForm.value.targetId ? [grantForm.value.targetId] : [])
  } else if (type === 'user') {
    userPickerRef.value.open()
  }
}

function onDeptPicked(d) {
  grantForm.value.targetId = d.deptId
  grantTargetName.value = d.deptName
}

function onRolePicked(roles) {
  const r = Array.isArray(roles) ? roles[0] : roles
  if (!r) return
  grantForm.value.targetId = r.roleId
  grantTargetName.value = r.roleName
}

function onUserPicked(users) {
  const u = Array.isArray(users) ? users[0] : users
  if (!u) return
  grantForm.value.targetId = u.userId
  grantTargetName.value = u.nickName
}

function openCustomDeptPicker() {
  customDeptPickerRef.value.open(customDepts.value.map(d => d.deptId))
}

function onCustomDeptPicked(depts) {
  customDepts.value = depts || []
}

function openCustomUserPicker() {
  customUserPickerRef.value.open()
}

// 自定义人员复用 UserPicker（多选）
function onUserPickedMulti(users) {
  customUsers.value = users || []
}

function submitGrant() {
  grantRef.value.validate(valid => {
    if (!valid) return
    let scopeJson = null
    if (grantForm.value.scopeType === 'customDept') {
      if (customDepts.value.length === 0) {
        proxy.$modal.msgWarning('请选择自定义部门')
        return
      }
      scopeJson = JSON.stringify({
        deptIds: customDepts.value.map(d => d.deptId),
        deptNames: customDepts.value.map(d => d.deptName),
        includeChild: includeChild.value
      })
    } else if (grantForm.value.scopeType === 'customUser') {
      if (customUsers.value.length === 0) {
        proxy.$modal.msgWarning('请选择自定义人员')
        return
      }
      scopeJson = JSON.stringify({
        userIds: customUsers.value.map(u => u.userId),
        userNames: customUsers.value.map(u => u.nickName)
      })
    }
    grantSaving.value = true
    const payload = {
      grantId: grantForm.value.grantId,
      ledgerId: grantLedger.value.ledgerId,
      grantType: grantForm.value.grantType,
      targetId: grantForm.value.grantType === 'all' ? null : grantForm.value.targetId,
      scopeType: grantForm.value.scopeType,
      scopeJson,
      canExport: grantForm.value.canExport
    }
    const request = payload.grantId ? updateGrant(payload) : addGrant(payload)
    request.then(() => {
      proxy.$modal.msgSuccess('保存成功')
      grantFormOpen.value = false
      loadGrants()
    }).catch(e => {
      proxy.$modal.msgError('保存失败：' + (e?.message || e || '请稍后重试'))
    }).finally(() => { grantSaving.value = false })
  })
}

function handleDeleteGrant(row) {
  proxy.$modal.confirm('确认删除该授权？').then(() => delGrant(row.grantId)).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    loadGrants()
  }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('删除失败：' + (e?.message || e)) })
}

getList()
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

/* ===== 向导通用样式（弹窗被 teleport 到 body，需放在顶层） ===== */
.wizard-steps {
  display: flex;
  align-items: flex-start;
  padding: 4px 8px 18px;
}
.wizard-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  min-width: 72px;
  cursor: pointer;
  user-select: none;
  .step-no {
    width: 34px;
    height: 34px;
    border-radius: 50%;
    border: 1px solid var(--ink-300, #cbd5e1);
    background: #fff;
    color: var(--ink-500, #64748b);
    font-size: 13px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all .2s;
  }
  .step-name {
    font-size: 12px;
    color: var(--ink-500, #64748b);
    white-space: nowrap;
    transition: color .2s;
  }
  &:hover .step-no { border-color: var(--brand-600, #2563eb); color: var(--brand-600, #2563eb); }
  &.active .step-no {
    background: var(--brand-600, #2563eb);
    border-color: var(--brand-600, #2563eb);
    color: #fff;
    box-shadow: 0 0 0 4px rgba(37, 99, 235, .12);
  }
  &.active .step-name { color: var(--brand-600, #2563eb); font-weight: 600; }
  &.done .step-no { border-color: var(--brand-600, #2563eb); color: var(--brand-600, #2563eb); background: #eef4ff; }
  &.done .step-name { color: var(--ink-700, #334155); }
  &.locked { cursor: not-allowed; }
  &.locked .step-no { background: #f5f7fa; color: #c0c4cc; border-color: #e4e7ed; }
  &.locked .step-name { color: #c0c4cc; }
}
.step-line {
  flex: 1;
  height: 2px;
  margin: 17px 6px 0;
  border-radius: 1px;
  background: #e8eef5;
  &.done { background: var(--brand-600, #2563eb); }
}

.wizard-body {
  min-height: 420px;
  max-height: 58vh;
  overflow: auto;
  padding: 2px 2px 8px;
}

.base-info-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 32px;
  margin: 4px 0 10px;
  padding: 10px 14px;
  border: 1px dashed var(--ink-200, #e5eaf2);
  border-radius: 8px;
  background: #fafbfd;
  .info-cell { display: flex; align-items: center; gap: 8px; font-size: 12px; }
  .info-cell .k { color: var(--ink-400, #94a3b8); }
  .info-cell .v { color: var(--ink-700, #334155); }
  .mono { font-family: Consolas, Monaco, monospace; }
}

.transfer {
  display: flex;
  gap: 14px;
  &.transfer-lg .panel { height: 500px; }
  .panel {
    flex: 1;
    min-width: 0;
    height: 340px;
    display: flex;
    flex-direction: column;
    border: 1px solid var(--ink-200, #e5eaf2);
    border-radius: 10px;
    background: #fff;
    overflow: hidden;
    box-shadow: 0 1px 2px rgba(15, 23, 42, .04);
  }
  .panel-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    padding: 9px 12px;
    border-bottom: 1px solid #eef2f7;
    background: #f8fafc;
    font-size: 13px;
    font-weight: 600;
    color: var(--ink-700, #334155);
    .count { margin-left: 4px; font-weight: 400; font-size: 12px; color: var(--ink-400, #94a3b8); }
  }
  .panel-body { flex: 1; overflow: auto; padding: 8px; }
}

.head-ops { display: flex; align-items: center; gap: 8px; }
.head-tip { font-size: 12px; font-weight: 400; color: var(--ink-400, #94a3b8); }

.field-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: var(--ink-600, #475569);
  transition: background .15s;
  &:hover { background: #f1f7ff; }
  .f-label { flex: none; }
  .mono {
    margin-left: auto;
    font-family: Consolas, Monaco, monospace;
    font-size: 12px;
    color: var(--ink-400, #94a3b8);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .add-icon { flex: none; color: #c3ccda; opacity: 0; transition: opacity .15s, color .15s; }
  &:hover .add-icon { opacity: 1; color: var(--brand-600, #2563eb); }
}

.drag-list { min-height: 100%; }

.selected-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
  padding: 6px 10px;
  border: 1px solid #eef2f7;
  border-radius: 6px;
  background: #fff;
  font-size: 13px;
  color: var(--ink-700, #334155);
  transition: border-color .15s, box-shadow .15s;
  &:hover { border-color: #cfe0fb; box-shadow: 0 2px 8px rgba(37, 99, 235, .08); }
  .drag-handle { flex: none; color: #b6c2d4; cursor: grab; font-size: 14px; }
  .drag-handle:active { cursor: grabbing; }
  .idx {
    flex: none;
    width: 18px;
    height: 18px;
    border-radius: 4px;
    background: #e8eef7;
    color: #64748b;
    font-size: 11px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .f-label { flex: none; }
  .mono {
    flex: 1;
    font-family: Consolas, Monaco, monospace;
    font-size: 12px;
    color: var(--ink-400, #94a3b8);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .default-tag { flex: none; }
  .dir-select { flex: none; width: 86px; }
  .row-del { flex: none; }
}

.drag-ghost {
  opacity: .45;
  background: #eef5ff !important;
  border-style: dashed !important;
  border-color: var(--brand-600, #2563eb) !important;
}
.drag-chosen { box-shadow: 0 4px 14px rgba(37, 99, 235, .18); }

.empty-tip { padding: 48px 0; text-align: center; font-size: 12px; color: #b0b8c4; }
.empty-inline { font-size: 12px; color: #b0b8c4; }

.step-section-title {
  position: relative;
  margin: 14px 0 10px;
  padding-left: 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-700, #334155);
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 13px;
    border-radius: 2px;
    background: var(--brand-600, #2563eb);
  }
}

.step-tip {
  margin-top: 10px;
  font-size: 12px;
  line-height: 1.7;
  color: var(--ink-400, #94a3b8);
}

.grant-toolbar {
  border-radius: 8px;
  margin-bottom: 10px;
}

.finish-card {
  padding: 6px 4px 20px;
  .finish-title { font-size: 17px; font-weight: 600; color: var(--ink-800, #1e293b); }
  .finish-desc { margin: 6px 0 18px; font-size: 13px; color: var(--ink-400, #94a3b8); }
}
.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px 16px;
  margin-bottom: 18px;
  .summary-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 12px 14px;
    border: 1px solid var(--ink-200, #e5eaf2);
    border-radius: 8px;
    background: #fafbfd;
    .k { font-size: 12px; color: var(--ink-400, #94a3b8); }
    .v { font-size: 14px; font-weight: 600; color: var(--ink-700, #334155); }
  }
}
.preview-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  .k { font-size: 13px; color: var(--ink-500, #64748b); margin-right: 4px; }
}

.wizard-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  .right { display: flex; align-items: center; gap: 8px; }
}

.form-tip { font-size: 12px; color: var(--ink-400, #94a3b8); line-height: 1.5; }

.picker-readonly {
  width: 100%;
  min-height: 32px;
  padding: 3px 8px;
  border: 1px solid var(--ink-200, #e5eaf2);
  border-radius: 6px;
  cursor: pointer;
  background: #fff;
  line-height: 24px;
  .placeholder { color: var(--ink-400, #94a3b8); font-size: 12px; }
}
.picker-icon { cursor: pointer; color: var(--ink-400, #94a3b8); }

/* ===== 列表页样式 ===== */
.flowable-ledger-page {
  .filter-card {
    padding: 12px 16px 14px;
    .filter-head { margin-bottom: 10px; }
    .filter-bar { gap: 10px 14px; }
    .filter-actions { margin-top: 10px; padding-top: 10px; }
    .field .control { height: 32px; padding: 0 10px; }
    .field .control :deep(.el-input__wrapper) { height: 30px; }
    .field .control :deep(.el-input__inner) { height: 30px; line-height: 30px; }
  }
  .toolbar { padding: 10px 16px; }
  .table-wrap { padding-top: 6px; }
  .table-wrap + .pagination-container { padding-top: 0; }
}
</style>

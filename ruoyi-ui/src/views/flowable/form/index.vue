<template>
  <div class="app-container flowable-form-page">
    <!-- 筛选 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>表单名称</label>
          <div class="control">
            <el-input v-model="queryParams.formName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>流程分类</label>
          <div class="control">
            <el-select v-model="queryParams.category" placeholder="全部" clearable @change="handleQuery" style="width: 100%">
              <el-option v-for="d in flowable_category" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 支持回车快速搜索</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- 列表 -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['flowable:form:add']">新增</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table" @header-dragend="onHeaderDragEnd">
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="表单名称" prop="formName" key="formName" :min-width="colWidth('formName', 180)" align="center" show-overflow-tooltip resizable>
            <template #default="scope"><span class="col-strong">{{ scope.row.formName }}</span></template>
          </el-table-column>
          <el-table-column label="流程分类" prop="category" key="category" :min-width="colWidth('category', 110)" align="center" resizable>
            <template #default="scope">
              <span v-if="scope.row.category" class="badge violet">{{ categoryLabel(scope.row.category) }}</span>
              <span v-else class="badge gray"><span class="dot"></span>未分类</span>
            </template>
          </el-table-column>
          <el-table-column label="数据库表名" prop="tableName" key="tableName" :min-width="colWidth('tableName', 180)" align="center" show-overflow-tooltip resizable>
            <template #default="scope">
              <span v-if="scope.row.tableName" class="badge green"><span class="dot"></span>{{ scope.row.tableName }}</span>
              <span v-else class="badge gray"><span class="dot"></span>未生成</span>
            </template>
          </el-table-column>
          <el-table-column label="表单描述" prop="formDesc" key="formDesc" :min-width="colWidth('formDesc', 200)" align="center" show-overflow-tooltip resizable>
            <template #default="scope">{{ scope.row.formDesc || '—' }}</template>
          </el-table-column>
          <el-table-column label="字段数" prop="fieldCount" key="fieldCount" width="90" align="center">
            <template #default="scope">{{ fieldCount(scope.row.fieldsJson) }}</template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" width="170" align="center" />
          <el-table-column label="操作" width="340" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleDesign(scope.row)" v-hasPermi="['flowable:form:edit']">设计</el-button>
                <el-button link type="success" icon="FullScreen" @click="handleDesign(scope.row, 'layout')" v-hasPermi="['flowable:form:edit']">页面设计</el-button>
                <el-button link type="warning" icon="Coin" @click="handleCreateTable(scope.row)" v-hasPermi="['flowable:form:table']">{{ scope.row.tableName ? '重建数据表' : '生成数据表' }}</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['flowable:form:remove']">删除</el-button>
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

    <!-- ===== 新建自定义表单弹窗（参照泛微e9：名称/复制/描述） ===== -->
    <el-dialog v-model="addOpen" width="520px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
          </div>
          <span class="rd-detail-header-title">新建自定义表单</span>
        </div>
      </template>
      <el-form ref="addRef" :model="addFormData" :rules="addRules" label-width="120px" size="small">
        <el-form-item label="表单名称" prop="formName"><el-input v-model="addFormData.formName" placeholder="如：销售合同" maxlength="50" /></el-form-item>
        <el-form-item label="流程分类"><el-select v-model="addFormData.category" placeholder="请选择分类（可留空）" clearable style="width:100%">
          <el-option v-for="d in flowable_category" :key="d.value" :label="d.label" :value="d.value" />
        </el-select></el-form-item>
        <el-form-item label="从已有表单复制">
          <div class="copy-field">
            <el-input :model-value="copySourceName" readonly placeholder="未选择（字段将为空，可稍后在编辑字段中设计）">
              <template #suffix>
                <el-icon class="copy-clear" v-if="addFormData.copyFrom" @click="clearCopySource"><CircleClose /></el-icon>
              </template>
            </el-input>
            <el-button icon="Search" @click="openCopyPicker">浏览</el-button>
          </div>
          <div class="form-tip">选择后将复制该表单的全部字段设计；数据库表名保存后自动生成为 flow_data_+唯一数字，无需填写</div>
        </el-form-item>
        <el-form-item label="表单描述"><el-input v-model="addFormData.formDesc" type="textarea" :rows="3" maxlength="200" placeholder="表单用途说明" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAdd(true)">保存并进入详细设置</el-button>
        <el-button @click="submitAdd(false)">保 存</el-button>
        <el-button @click="addOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 从已有表单复制：弹框选择器（按弹框选择器开发规范） ===== -->
    <el-dialog v-model="copyPickerOpen" width="640px" append-to-body draggable class="rd-dialog" title="从已有表单复制" :close-on-click-modal="false">
      <div class="copy-picker-bar">
        <el-input v-model="copyKeyword" placeholder="搜索表单名称" clearable @keyup.enter="loadCopyList">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button icon="Search" @click="loadCopyList">搜索</el-button>
      </div>
      <el-table :data="copyList" border size="small" max-height="360" highlight-current-row
        @current-change="onCopyRowSelect" @row-dblclick="confirmCopyPicker" class="copy-table">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="表单名称" prop="formName" min-width="160" align="center" show-overflow-tooltip>
          <template #default="scope"><span class="col-strong">{{ scope.row.formName }}</span></template>
        </el-table-column>
        <el-table-column label="表单描述" prop="formDesc" min-width="180" align="center" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.formDesc || '—' }}</template>
        </el-table-column>
        <el-table-column label="字段数" width="80" align="center">
          <template #default="scope">{{ fieldCount(scope.row.fieldsJson) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
      </el-table>
      <template #footer>
        <el-button type="primary" :disabled="!copySelected" @click="confirmCopyPicker">确 定</el-button>
        <el-button @click="copyPickerOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 表单设计弹窗（基本信息 / 编辑字段，参照泛微e9） ===== -->
    <el-dialog v-model="open" width="1100px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false" :fullscreen="designMax"
      :class="{ 'rd-no-footer': designMax || (activeTab === 'layout' && layout.mode === 'grid') }">
      <template #header>
        <div class="rd-detail-header" style="position:relative">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          </div>
          <span class="rd-detail-header-title">编辑表单：{{ form.formName || '新建表单' }}</span>
          <el-button class="rd-max-btn" type="warning" size="default" icon="FullScreen" @click="designMax = !designMax">{{ designMax ? '还原窗口' : '最大化' }}</el-button>
        </div>
      </template>
      <el-tabs v-model="activeTab" class="form-design-tabs">
        <!-- ===== 基本信息 ===== -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="form" label-width="110px" size="small" class="basic-form">
            <el-form-item label="表单名称" required><el-input v-model="form.formName" placeholder="如：销售合同" /></el-form-item>
            <el-form-item label="流程分类"><el-select v-model="form.category" placeholder="请选择分类（可留空）" clearable style="width:100%">
              <el-option v-for="d in flowable_category" :key="d.value" :label="d.label" :value="d.value" />
            </el-select></el-form-item>
            <el-form-item label="数据库表名">
              <div class="tbl-name-row">
                <span v-if="form.tableName" class="badge green"><span class="dot"></span>{{ form.tableName }}</span>
                <span v-else class="badge gray"><span class="dot"></span>{{ 'flow_data_' + (form.formKey || '{表单Key}') }}</span>
              </div>
              <div class="form-tip" v-if="!form.tableName">尚未生成数据表，点击列表"生成数据表"后按此名称创建（明细表自动追加 _dt1/_dt2…）</div>
            </el-form-item>
            <el-form-item label="表单Key"><el-input :model-value="form.formKey" disabled /><div class="form-tip">系统自动生成的唯一数字标识，用于数据表命名</div></el-form-item>
            <el-form-item label="表单描述"><el-input v-model="form.formDesc" type="textarea" :rows="2" maxlength="200" placeholder="表单用途说明" /></el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- ===== 编辑字段 ===== -->
        <el-tab-pane label="编辑字段" name="fields">
          <div class="field-tab-bar">
            <el-tabs v-model="activeTableTab" type="card" class="table-tabs"
              @tab-remove="removeDetailTable">
              <el-tab-pane name="main" :closable="false">
                <template #label><span>主表 <span class="tab-tbl">( {{ mainTableName }} )</span></span></template>
              </el-tab-pane>
              <el-tab-pane v-for="dt in detailTables" :key="dt" :name="dt" :closable="true">
                <template #label><span>明细表{{ dt.slice(2) }} <span class="tab-tbl">( {{ mainTableName + '_' + dt }} )</span></span></template>
              </el-tab-pane>
              <el-tab-pane name="__add" :closable="false">
                <template #label><span class="tab-add">＋</span></template>
              </el-tab-pane>
            </el-tabs>
          </div>

          <div class="field-toolbar">
            <el-button size="small" type="primary" plain icon="Plus" @click="openAddField">添加字段</el-button>
            <el-button size="small" type="primary" plain icon="DocumentAdd" @click="openBatchAdd">批量添加</el-button>
            <el-button size="small" plain icon="Delete" :disabled="!checkedFields.length" @click="removeCheckedFields">删除选中</el-button>
            <span class="field-tip">当前表：{{ activeTableTab === 'main' ? mainTableName : mainTableName + '_' + activeTableTab }}<template v-if="activeTableTab !== 'main'">（明细表通过 mainid 字段关联主表主键，生成数据表时自动创建）</template></span>
          </div>

          <el-table :data="activeFields" border size="small" row-key="rowKey" @selection-change="onFieldSelChange" class="field-table" max-height="380">
            <el-table-column type="selection" width="45" align="center" />
            <el-table-column label="字段显示名" min-width="130" align="center"><template #default="scope"><el-input v-model="scope.row.label" placeholder="如：合同名称" size="small" /></template></el-table-column>
            <el-table-column label="数据库字段名称" min-width="130" align="center"><template #default="scope"><el-input v-model="scope.row.field" placeholder="小写英文，如 htmc" size="small" /></template></el-table-column>
            <el-table-column label="表现形式" width="130" align="center">
              <template #default="scope">
                <el-select v-model="scope.row.form" size="small" @change="onFormChange(scope.row)">
                  <el-option v-for="(v, k) in FORM_OPTIONS" :key="k" :label="v" :value="k" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="类型" width="110" align="center">
              <template #default="scope">
                <span v-if="isDtypeLocked(scope.row.form)" style="color:#909399">{{ DTYPE_OPTIONS[scope.row.dtype] || scope.row.dtype }}（自动）</span>
                <el-select v-else v-model="scope.row.dtype" size="small">
                  <el-option v-for="(v, k) in dtypeOptions(scope.row.form)" :key="k" :label="v" :value="k" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="文本长度" width="100" align="center">
              <template #default="scope"><el-input-number v-if="scope.row.dtype === 'text' && !isLengthLocked(scope.row)" v-model="scope.row.length" :min="1" :max="8000" :controls="false" size="small" style="width:100%" /><span v-else style="color:#cbd5e1">自动</span></template>
            </el-table-column>
            <el-table-column label="格式/配置" min-width="150" align="center">
              <template #default="scope">
                <!-- 多行文本：行数（默认4，可自定义1-20） -->
                <el-input-number v-if="scope.row.form === 'textarea'" v-model="scope.row.rows" :min="1" :max="20" :controls="false" size="small" style="width:80px" />
                <!-- 浏览框：数据源类型 -->
                <el-select v-else-if="scope.row.form === 'browse'" v-model="scope.row.browseType" size="small" style="width:100%">
                  <el-option v-for="(v, k) in BROWSE_TYPES" :key="k" :label="v" :value="k" />
                </el-select>
                <!-- 附件：类型+数量 -->
                <div v-else-if="scope.row.form === 'attach'" style="display:flex;gap:4px">
                  <el-select v-model="scope.row.attachType" size="small" style="flex:1">
                    <el-option v-for="(v, k) in ATTACH_TYPES" :key="k" :label="v" :value="k" />
                  </el-select>
                  <el-input-number v-model="scope.row.attachLimit" :min="1" :max="20" :controls="false" size="small" style="width:64px" />
                </div>
                <!-- 选择框/单选框：选项键值对 -->
                <el-input v-else-if="scope.row.form === 'select' || scope.row.form === 'radio'" :model-value="selectOptionsText(scope.row)" @input="v => setSelectOptions(scope.row, v)" placeholder="显示值:存储值 每行一对" size="small" />
                <span v-else style="color:#cbd5e1">—</span>
              </template>
            </el-table-column>
            <el-table-column label="小数位数" width="95" align="center">
              <template #default="scope"><el-input-number v-if="scope.row.dtype === 'float'" v-model="scope.row.scale" :min="1" :max="6" :controls="false" size="small" style="width:100%" /><span v-else style="color:#cbd5e1">—</span></template>
            </el-table-column>
            <el-table-column label="必填" width="70" align="center"><template #default="scope"><el-switch v-model="scope.row.required" size="small" /></template></el-table-column>
            <el-table-column label="显示顺序" width="100" align="center"><template #default="scope"><el-input-number v-model="scope.row.order" :min="1" :max="999" :controls="false" size="small" style="width:100%" /></template></el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ===== 页面设计（类Excel设计器：拖拽布局/分栏/分组/说明行 + 预览） ===== -->
        <el-tab-pane label="页面设计" name="layout">
          <div class="layout-mode-bar">
            <el-button size="small" :type="layout.mode === 'grid' ? 'warning' : 'primary'" plain icon="Switch"
              @click="toggleLayoutMode">{{ layout.mode === 'grid' ? '切换为简易分组' : '切换为 Excel 网格（泛微式）' }}</el-button>
            <span class="field-tip">当前模式：{{ layout.mode === 'grid' ? 'Excel 网格（泛微式）' : '简易分组' }}<template v-if="layout.mode === 'grid'">，深蓝菜单栏 / 格式工具条 / 行列合并 / 单元格样式，操作见右上"帮助"</template></span>
          </div>
          <div class="ed-wrap" v-if="layout.mode !== 'grid'">
            <div class="ed-main">
              <div class="ed-toolbar">
                <el-button size="small" icon="RefreshLeft" :disabled="!canUndo" @click="undoLayout" title="撤销上一步">撤销</el-button>
                <el-button size="small" icon="RefreshRight" :disabled="!canRedo" @click="redoLayout" title="重做">重做</el-button>
                <el-button size="small" type="primary" plain icon="Plus" @click="addSection">添加分组</el-button>
                <el-button size="small" plain icon="MagicStick" @click="autoLayout">自动布局</el-button>
                <el-button size="small" plain icon="ChatLineSquare" @click="addNoteRow">插入说明行</el-button>
                <el-dropdown trigger="click" @command="addDetailBlock">
                  <el-button size="small" plain icon="Grid" :disabled="!detailTables.length">插入明细表</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-for="dt in detailTables" :key="dt" :command="dt">明细表{{ dt.slice(2) }}（{{ mainTableName + '_' + dt }}）</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-button size="small" :type="layoutPreview ? 'primary' : 'default'" :icon="layoutPreview ? 'EditPen' : 'View'" @click="layoutPreview = !layoutPreview">{{ layoutPreview ? '返回设计' : '预览效果' }}</el-button>
                <span class="field-tip">像 Excel 一样操作：拖拽布局、右键合并/拆分单元格、Ctrl+Z 撤销、Delete 删除选中</span>
              </div>
              <div class="ed-canvas" v-if="!layoutPreview">
                <div class="ed-ruler">
                  <span class="ed-ruler-corner"></span>
                  <span class="ed-ruler-col ed-ruler-label">标签</span>
                  <span class="ed-ruler-col" v-for="c in 4" :key="c">{{ String.fromCharCode(65 + c) }}</span>
                </div>
                <div v-if="!layout.sections.length" class="ed-empty">暂无分组：点击"添加分组"或"自动布局"开始设计，也可直接把右侧字段拖到下方</div>
                <div class="ed-section" v-for="(sec, si) in layout.sections" :key="si"
                  @dragover.prevent @drop.prevent="onDropToSection($event, si)">
                  <div class="ed-sec-bar">
                    <el-button size="small" link :icon="sec.collapsed ? 'CaretRight' : 'CaretBottom'" @click="sec.collapsed = !sec.collapsed" :title="sec.collapsed ? '展开分组' : '折叠分组'" />
                    <el-tag size="small" type="info" effect="plain">分组{{ si + 1 }}</el-tag>
                    <input class="ed-sec-title" v-model="sec.title" placeholder="输入分组标题，如：基本信息" />
                    <span class="ed-sec-ops">
                      <el-button size="small" link icon="Top" title="上移" @click="moveSection(si, -1)" />
                      <el-button size="small" link icon="Bottom" title="下移" @click="moveSection(si, 1)" />
                      <el-button size="small" link type="primary" icon="Plus" title="添加行" @click="addRow(si)" />
                      <el-button size="small" link type="danger" icon="Delete" title="删除分组" @click="removeSection(si)" />
                    </span>
                  </div>
                  <div class="ed-row" v-show="!sec.collapsed" v-for="(row, ri) in sec.rows" :key="ri">
                    <div class="ed-row-cells" @dragover.prevent @drop.prevent="onDropToRow($event, si, ri)">
                      <div class="ed-cell" v-for="(cell, ci) in row" :key="ci"
                        :class="['ed-cell', { 'is-active': isCellActive(si, ri, ci), 'is-note': !!cell.note, 'is-detail': !!cell.detailTable, 'cell-b-all': cell.border === 'all', 'cell-b-bottom': cell.border === 'bottom', 'cell-b-none': cell.border === 'none' }]"
                        :style="[{ width: (cell.span / 24 * 100) + '%' }, cellStyle(cell)]"
                        draggable="true" @dragstart="onCellDragStart($event, si, ri, ci)"
                        @dragover.prevent.stop @drop.prevent.stop="onDropToCell($event, si, ri, ci)"
                        @click.stop="selectCell(si, ri, ci)"
                        @dblclick.stop="cellEdit(si, ri, ci)"
                        @contextmenu.prevent.stop="openCtxMenu($event, si, ri, ci)">
                        <div class="ed-cell-label">
                          <el-icon v-if="cell.note"><ChatLineSquare /></el-icon>
                          <el-icon v-if="cell.detailTable"><Grid /></el-icon>
                          <span v-if="cell.detailTable">{{ detailTableLabel(cell.detailTable) }}</span>
                          <span v-else>{{ cell.note ? (cell.noteLabel || '说明') : (cell.label || fieldShortLabel(cell.field) || '未选字段') }}</span>
                          <span class="ed-merge-badge" v-if="cell.span > 12 && !cell.detailTable" title="合并单元格：右键可拆分">{{ (cell.span / 24 * 4).toFixed(cell.span % 6 === 0 ? 0 : 1) }}格</span>
                        </div>
                        <div class="ed-cell-ctrl">
                          <span v-if="cell.detailTable" class="ed-detail-mini">
                            <div class="ed-detail-head">
                              <span class="ed-detail-th" style="width:36px">序号</span>
                              <span class="ed-detail-th" v-for="f in detailTableFields(cell.detailTable)" :key="f.field">{{ f.label }}</span>
                              <span class="ed-detail-th" v-if="!detailTableFields(cell.detailTable).length" style="flex:1">（该明细表暂无字段）</span>
                            </div>
                            <div class="ed-detail-tr" v-for="i in 3" :key="i"><span class="ed-detail-td" style="width:36px;color:#cbd5e1">{{ i }}</span><span class="ed-detail-td" v-for="f in detailTableFields(cell.detailTable)" :key="f.field"></span></div>
                            <div class="ed-detail-tr ed-detail-sum" v-if="(cell.detailSum || []).length">
                              <span class="ed-detail-td" :style="{width:36+'px',flex:'none'}">合计</span>
                              <span class="ed-detail-td" v-for="f in detailTableFields(cell.detailTable)" :key="f.field">{{ cell.detailSum.includes(f.field) ? 'Σ' : '' }}</span>
                            </div>
                          </span>
                          <span v-else-if="cell.note" class="ed-note-text">{{ cell.note || '（点击输入说明文字）' }}</span>
                          <template v-else-if="cell.field"><span class="ed-ctrl-demo"></span>{{ fieldShortLabel(cell.field) }}输入框</template>
                          <span v-else class="ed-cell-placeholder">拖入字段 / 双击编辑</span>
                        </div>
                      </div>
                    </div>
                    <div class="ed-row-ops">
                      <el-button size="small" link icon="Top" title="上移" @click.stop="moveRow(si, ri, -1)" />
                      <el-button size="small" link icon="Bottom" title="下移" @click.stop="moveRow(si, ri, 1)" />
                      <el-button size="small" link icon="Plus" title="在此行后插入" @click.stop="insertRow(si, ri)" />
                      <el-button size="small" link type="danger" icon="Delete" title="删除本行" @click.stop="removeRow(si, ri)" />
                    </div>
                  </div>
                  <div class="ed-sec-empty" v-show="!sec.collapsed" v-if="!sec.rows.length">空分组：把右侧字段拖到这里，或点击"行"添加</div>
                </div>
                <div class="ed-drop-last" @dragover.prevent @drop.prevent="onDropToEnd($event)">拖字段到此处 → 新建分组存放</div>
              </div>
              <div class="layout-preview" v-else>
                <template v-for="(sec, si) in previewSections" :key="si">
                  <div class="pv-section-title" v-if="sec.title">{{ sec.title }}</div>
                  <el-row :gutter="12" class="pv-row">
                    <el-col v-for="(c, idx) in sec.cells" :key="idx" :span="c.span">
                      <div v-if="c.detailTable" class="pv-detail">
                        <div class="pv-detail-title">{{ detailTableLabel(c.detailTable) }}</div>
                        <table class="pv-detail-table">
                          <thead><tr><th style="width:44px">序号</th><th v-for="f in detailTableFields(c.detailTable)" :key="f.field">{{ f.label }}</th></tr></thead>
                          <tbody>
                            <tr v-for="i in 3" :key="i"><td style="color:#cbd5e1">{{ i }}</td><td v-for="f in detailTableFields(c.detailTable)" :key="f.field"></td></tr>
                            <tr v-if="(c.detailSum || []).length" class="pv-detail-sum"><td>合计</td><td v-for="f in detailTableFields(c.detailTable)" :key="f.field">{{ c.detailSum.includes(f.field) ? 'Σ' : '' }}</td></tr>
                          </tbody>
                        </table>
                      </div>
                      <div v-else-if="c.note" class="pv-note" :style="cellStyle(c)">{{ c.note }}</div>
                      <div v-else class="pv-item" :class="{ 'pv-b-all': c.border === 'all', 'pv-b-bottom': c.border === 'bottom' }" :style="cellStyle(c)">
                        <span class="pv-label">{{ c.field ? mainFieldLabel(c.field) : '（未选字段）' }}</span><div class="pv-value">—</div>
                      </div>
                    </el-col>
                  </el-row>
                </template>
                <div class="pv-section-title" v-if="detailFields.length">明细表</div>
                <div class="pv-item" v-for="f in detailFields" :key="f.field" style="width:100%"><span class="pv-label">{{ f.label }}</span><div class="pv-value">明细表字段随明细录入展示</div></div>
                <div v-if="!previewSections.length" class="layout-empty">未设计布局，发起时将按字段顺序单栏展示</div>
              </div>
            </div>
            <div class="ed-side">
              <div class="ed-side-title"><el-icon><Menu /></el-icon> 字段（拖拽到左侧画布）</div>
              <div class="ed-pool">
                <div class="ed-pool-item" v-for="f in unplacedFields" :key="f.field" draggable="true"
                  @dragstart="onPoolDragStart($event, f.field)" @dblclick="appendFieldToLastRow(f.field)" title="拖拽或双击添加">
                  <el-icon><Document /></el-icon><span class="pi-label">{{ f.label }}</span><em>{{ f.field }}</em>
                </div>
                <div class="ed-pool-empty" v-if="!unplacedFields.length">主表字段均已布局</div>
              </div>
              <div class="ed-side-title"><el-icon><Setting /></el-icon> 单元格属性</div>
              <div class="ed-props" v-if="activeCell && activeCell.detailTable">
                <div class="ed-prop"><label>明细表</label>
                  <el-select v-model="activeCell.detailTable" size="small">
                    <el-option v-for="dt in detailTables" :key="dt" :label="detailTableLabel(dt) + '（' + mainTableName + '_' + dt + '）'" :value="dt" />
                  </el-select>
                </div>
                <div class="ed-prop"><label>表尾合计字段（数值类）</label>
                  <el-select v-model="activeCell.detailSum" size="small" multiple collapse-tags style="width:100%" placeholder="选择需要自动合计的列">
                    <el-option v-for="f in detailTableFields(activeCell.detailTable)" :key="f.field" :label="f.label + '（' + f.field + '）'" :value="f.field" :disabled="f.dtype !== 'int' && f.dtype !== 'float'" />
                  </el-select>
                  <div class="form-tip">仅整数/浮点数字段可合计；勾选后明细表底部显示 Σ 合计行</div>
                </div>
                <el-button size="small" type="danger" link icon="Delete" @click="removeActiveCell">删除该明细表块</el-button>
              </div>
              <div class="ed-props" v-else-if="activeCell">
                <div class="ed-prop"><label>宽度{{ activeCell.span > 12 ? '（已合并 ' + activeCell.span + '/24）' : '' }}</label>
                  <el-select v-model="activeCell.span" size="small">
                    <el-option :value="6" label="1/4 宽" /><el-option :value="8" label="1/3 宽" />
                    <el-option :value="12" label="1/2 宽" /><el-option :value="24" label="整行" />
                    <el-option v-if="![6, 8, 12, 24].includes(activeCell.span)" :value="activeCell.span" :label="activeCell.span + '/24'" />
                  </el-select>
                </div>
                <div class="ed-prop" v-if="!activeCell.note"><label>字段</label>
                  <el-select v-model="activeCell.field" size="small" filterable clearable placeholder="选择字段">
                    <el-option v-for="f in mainFields" :key="f.field" :label="f.label + '（' + f.field + '）'" :value="f.field" />
                  </el-select>
                </div>
                <div class="ed-prop"><label>{{ activeCell.note ? '说明文字' : '显示标签（留空用字段显示名）' }}</label>
                  <el-input v-if="activeCell.note" v-model="activeCell.note" type="textarea" :rows="3" placeholder="如：有出差报销的必须填写出差天数" />
                  <el-input v-else v-model="activeCell.label" placeholder="留空使用字段显示名" />
                </div>
                <div class="ed-prop"><label>字体</label>
                  <el-select v-model="activeCell.fontFamily" size="small" clearable placeholder="默认">
                    <el-option v-for="f in FONT_OPTIONS" :key="f.v" :label="f.l" :value="f.v" />
                  </el-select>
                </div>
                <div class="ed-prop"><label>字号 / 字体样式</label>
                  <div class="ed-prop-row">
                    <el-select v-model="activeCell.fontSize" size="small" clearable placeholder="字号" style="width:86px">
                      <el-option v-for="s in [12, 13, 14, 16, 18, 20]" :key="s" :label="s + 'px'" :value="s" />
                    </el-select>
                    <el-button size="small" :type="activeCell.bold ? 'primary' : 'default'" @click="activeCell.bold = !activeCell.bold" title="加粗"><b>B</b></el-button>
                    <el-button size="small" :type="activeCell.italic ? 'primary' : 'default'" @click="activeCell.italic = !activeCell.italic" title="斜体"><i>I</i></el-button>
                  </div>
                </div>
                <div class="ed-prop"><label>颜色</label>
                  <div class="ed-prop-row">
                    <span class="ed-color-item">文字<el-color-picker v-model="activeCell.color" size="small" /></span>
                    <span class="ed-color-item">背景<el-color-picker v-model="activeCell.bgColor" size="small" /></span>
                  </div>
                </div>
                <div class="ed-prop"><label>边框（单元格）</label>
                  <div class="ed-prop-row">
                    <el-select v-model="activeCell.border" size="small" clearable placeholder="无边框" style="width:100px">
                      <el-option value="all" label="四边框" /><el-option value="bottom" label="仅下边框" /><el-option value="none" label="隐藏边框" />
                    </el-select>
                    <el-color-picker v-model="activeCell.borderColor" size="small" />
                  </div>
                </div>
                <el-button size="small" link @click="clearCellStyle">清除全部样式</el-button>
                <el-button size="small" type="danger" link icon="Delete" @click="removeActiveCell">删除该单元格</el-button>
              </div>
              <div class="ed-props-empty" v-else>点选左侧单元格后在此设置属性</div>
            </div>
          </div>
          <!-- 单元格右键菜单 -->
          <div class="ed-ctxmenu" v-if="ctxMenu.show" :style="{ left: ctxMenu.x + 'px', top: ctxMenu.y + 'px' }" @click.stop>
            <div class="ed-ctx-item" @click="ctxAction('mergeRight')" v-if="canMergeRight">与右侧单元格合并</div>
            <div class="ed-ctx-item" @click="ctxAction('splitCell')" v-if="ctxCell && ctxCell.span > 6">拆分单元格</div>
            <div class="ed-ctx-sep"></div>
            <div class="ed-ctx-item" @click="ctxAction('insertRight')">向右插入单元格</div>
            <div class="ed-ctx-item" @click="ctxAction('insertRowBelow')">下方插入一行</div>
            <div class="ed-ctx-item" @click="ctxAction('duplicate')">复制单元格</div>
            <div class="ed-ctx-item" v-if="!ctxCell?.note" @click="ctxAction('toNote')">转为说明行</div>
            <div class="ed-ctx-item" @click="ctxAction('editLabel')">{{ ctxCell?.note ? '编辑说明文字' : '自定义标签' }}</div>
            <div class="ed-ctx-item danger" @click="ctxAction('del')">删除单元格</div>
          </div>
          <!-- Excel 网格设计器（泛微式，P0） -->
          <ExcelGridDesigner v-if="layout.mode === 'grid'" :model-value="layout.grid"
            :fields="poolFields" :form="{ formName: form.formName, formKey: form.formKey }"
            :active="open && activeTab === 'layout'" :fullscreen="designMax"
            @update:model-value="v => (layout.grid = v)" @save="submitForm(true)" @toggle-fullscreen="designMax = !designMax" />
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button type="primary" @click="submitForm">保 存</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 添加字段弹窗（参照泛微e9） ===== -->
    <el-dialog v-model="addFieldOpen" width="520px" append-to-body draggable class="rd-dialog" title="添加字段" :close-on-click-modal="false">
      <el-form ref="addFieldRef" :model="newField" :rules="fieldRules" label-width="120px" size="small">
        <el-form-item label="字段显示名" prop="label"><el-input v-model="newField.label" placeholder="如：合同名称" @input="autoFillFieldName(newField, newField.label)" /></el-form-item>
        <el-form-item label="数据库字段名称" prop="field">
          <el-input v-model="newField.field" placeholder="小写英文，如 htmc（自动取首字母，可修改）" @input="newField.autoField = false" />
          <div class="form-tip">仅允许小写字母开头的字母/数字/下划线</div>
        </el-form-item>
        <el-form-item label="字段位置">
          <el-select v-model="newField.table" style="width:100%">
            <el-option label="主表" value="main" />
            <el-option v-for="dt in detailTables" :key="dt" :label="'明细表' + dt.slice(2) + '（' + mainTableName + '_' + dt + '）'" :value="dt" />
          </el-select>
        </el-form-item>
        <el-form-item label="显示顺序"><el-input-number v-model="newField.order" :min="1" :max="999" :controls="false" style="width:160px" /></el-form-item>
        <el-form-item label="表现形式">
          <el-select v-model="newField.form" style="width:100%" @change="onNewFormChange">
            <el-option v-for="(v, k) in FORM_OPTIONS" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <span v-if="isDtypeLocked(newField.form)" style="color:#909399">{{ DTYPE_OPTIONS[newField.dtype] || newField.dtype }}（随表现形式自动）</span>
          <el-select v-else v-model="newField.dtype" style="width:100%">
            <el-option v-for="(v, k) in dtypeOptions(newField.form)" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="文本长度" v-if="newField.dtype === 'text' && !isLengthLocked(newField)"><el-input-number v-model="newField.length" :min="1" :max="8000" :controls="false" style="width:160px" /></el-form-item>
        <el-form-item label="多行行数" v-if="newField.form === 'textarea'"><el-input-number v-model="newField.rows" :min="1" :max="20" :controls="false" style="width:160px" /><div class="form-tip">默认4行，可自定义1-20行</div></el-form-item>
        <el-form-item label="小数位数" v-if="newField.dtype === 'float'"><el-input-number v-model="newField.scale" :min="1" :max="6" :controls="false" style="width:160px" /></el-form-item>
        <el-form-item label="浏览数据源" v-if="newField.form === 'browse'">
          <el-select v-model="newField.browseType" style="width:100%">
            <el-option v-for="(v, k) in BROWSE_TYPES" :key="k" :label="v" :value="k" />
          </el-select>
          <div class="form-tip">人员/部门/岗位/物料/客户/供应商等主数据，选择"自定义"可在数据表扩展</div>
        </el-form-item>
        <el-form-item label="附件类型" v-if="newField.form === 'attach'">
          <div style="display:flex;gap:8px;width:100%">
            <el-select v-model="newField.attachType" style="flex:1">
              <el-option v-for="(v, k) in ATTACH_TYPES" :key="k" :label="v" :value="k" />
            </el-select>
            <el-input-number v-model="newField.attachLimit" :min="1" :max="20" :controls="false" style="width:100px" />
          </div>
          <div class="form-tip">类型约束+最大数量</div>
        </el-form-item>
        <el-form-item label="选项(显示值:存储值)" v-if="newField.form === 'select' || newField.form === 'radio'">
          <el-input :model-value="selectOptionsText(newField)" @input="v => setSelectOptions(newField, v)" type="textarea" :rows="4" placeholder="每行一个，格式：显示值:存储值（省略则存显示值）&#10;如：&#10;解除:1&#10;终止:2&#10;暂停:3" />
          <div class="form-tip">泛微式键值分离：界面显示中文，数据库存储实际值</div>
        </el-form-item>
        <el-form-item label="必填"><el-switch v-model="newField.required" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmAddField">确 定</el-button>
        <el-button @click="addFieldOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 批量添加字段弹窗（参照泛微e9：多行直接填写） ===== -->
    <el-dialog v-model="batchOpen" width="860px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
          </div>
          <span class="rd-detail-header-title">批量添加字段：{{ activeTableTab === 'main' ? '主表（' + mainTableName + '）' : '明细表' + activeTableTab.slice(2) + '（' + mainTableName + '_' + activeTableTab + '）' }}</span>
        </div>
      </template>
      <div class="batch-toolbar">
        <el-button size="small" type="primary" plain icon="Plus" @click="addBatchRow">再加一行</el-button>
        <span class="field-tip">直接在表格中逐行填写，确定后一次性追加到当前表；字段显示名与数据库字段名称为必填</span>
      </div>
      <el-table :data="batchRows" border size="small" row-key="batchKey" max-height="360" class="field-table">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="字段显示名" min-width="130" align="center"><template #default="scope"><el-input v-model="scope.row.label" placeholder="如：合同名称" size="small" @input="autoFillFieldName(scope.row, scope.row.label)" /></template></el-table-column>
        <el-table-column label="数据库字段名称" min-width="130" align="center"><template #default="scope"><el-input v-model="scope.row.field" placeholder="小写英文，如 htmc（自动取首字母，可修改）" size="small" @input="scope.row.autoField = false" /></template></el-table-column>
        <el-table-column label="表现形式" width="125" align="center">
          <template #default="scope">
            <el-select v-model="scope.row.form" size="small" @change="onBatchFormChange(scope.row)">
              <el-option v-for="(v, k) in FORM_OPTIONS" :key="k" :label="v" :value="k" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="105" align="center">
          <template #default="scope">
            <span v-if="isDtypeLocked(scope.row.form)" style="color:#909399">{{ DTYPE_OPTIONS[scope.row.dtype] || scope.row.dtype }}（自动）</span>
            <el-select v-else v-model="scope.row.dtype" size="small">
              <el-option v-for="(v, k) in dtypeOptions(scope.row.form)" :key="k" :label="v" :value="k" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="文本长度" width="90" align="center">
          <template #default="scope"><el-input-number v-if="scope.row.dtype === 'text' && !isLengthLocked(scope.row)" v-model="scope.row.length" :min="1" :max="8000" :controls="false" size="small" style="width:100%" /><span v-else style="color:#cbd5e1">自动</span></template>
        </el-table-column>
        <el-table-column label="格式/配置" min-width="130" align="center">
          <template #default="scope">
            <el-input-number v-if="scope.row.form === 'textarea'" v-model="scope.row.rows" :min="1" :max="20" :controls="false" size="small" style="width:72px" />
            <el-select v-else-if="scope.row.form === 'browse'" v-model="scope.row.browseType" size="small" style="width:100%">
              <el-option v-for="(v, k) in BROWSE_TYPES" :key="k" :label="v" :value="k" />
            </el-select>
            <el-input v-else-if="scope.row.form === 'select' || scope.row.form === 'radio'" :model-value="selectOptionsText(scope.row)" @input="v => setSelectOptions(scope.row, v)" placeholder="显示值:存储值,逗号分隔" size="small" />
            <span v-else style="color:#cbd5e1">—</span>
          </template>
        </el-table-column>
        <el-table-column label="必填" width="65" align="center"><template #default="scope"><el-switch v-model="scope.row.required" size="small" /></template></el-table-column>
        <el-table-column label="操作" width="60" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="removeBatchRow(scope.$index)" /></template></el-table-column>
      </el-table>
      <template #footer>
        <el-button type="primary" @click="confirmBatchAdd">确 定</el-button>
        <el-button @click="batchOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowForm">
import { listForm, getForm, addForm, updateForm, delForm, createTable } from '@/api/flowable/form'
import { Search, Filter, CircleClose } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { pinyin } from 'pinyin-pro'
import ExcelGridDesigner from './designer/ExcelGridDesigner.vue'
import { parseLayout, sectionsToGrid, fieldsToGrid, serializeLayout } from './designer/converters'

/** 中文名称 → 拼音首字母字段名（小写字母开头，仅含字母/数字/下划线），如：合同名称 → htmc */
function toFieldInitials(label) {
  if (!label) return ''
  const s = pinyin(String(label), { pattern: 'first', toneType: 'none', type: 'array', nonZh: 'concurrent' }).join('')
  return s.toLowerCase().replace(/[^a-z0-9_]/g, '').replace(/^[^a-z]+/, '').slice(0, 30)
}

/** 输入显示名时自动填充数据库字段名称：仅在字段为空或之前是自动生成的情况下覆盖，用户手工修改过则不覆盖 */
function autoFillFieldName(target, label) {
  const gen = toFieldInitials(label)
  if (target.autoField || !target.field) {
    target.field = gen || undefined
    target.autoField = !!gen
  } else if (gen && target.field === gen) {
    target.autoField = true
  }
}

const { proxy } = getCurrentInstance()
const { flowable_category } = proxy.useDict('flowable_category')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('flowable_form_index')
const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const activeTab = ref('basic')
const activeTableTab = ref('main')
const addFieldOpen = ref(false)
const addFieldRef = ref(null)
const checkedFields = ref([])

// ===== 新建自定义表单弹窗状态 =====
const addOpen = ref(false)
const addRef = ref(null)
const addFormData = ref({})
const addRules = {
  formName: [{ required: true, message: '表单名称不能为空', trigger: 'blur' }]
}

// ===== 从已有表单复制：弹框选择器状态 =====
const copyPickerOpen = ref(false)
const copyKeyword = ref('')
const copyList = ref([])
const copySelected = ref(null)
const copySourceName = computed(() => copySelected.value ? copySelected.value.formName : '')

function openCopyPicker() {
  copyKeyword.value = ''
  copySelected.value = null
  loadCopyList()
  copyPickerOpen.value = true
}

function loadCopyList() {
  listForm({ pageNum: 1, pageSize: 1000, formName: copyKeyword.value || undefined }).then(res => {
    copyList.value = (res.rows || []).filter(f => f.formId !== form.value.formId)
  })
}

function onCopyRowSelect(row) { copySelected.value = row }

/** 确认选择：立即拉取源表单字段暂存，保存时直接使用 */
async function confirmCopyPicker() {
  if (!copySelected.value) return
  const src = await getForm(copySelected.value.formId)
  addFormData.value.copyFrom = copySelected.value.formId
  addFormData.value.copyFieldsJson = src.data.fieldsJson || '[]'
  copyPickerOpen.value = false
}

function clearCopySource() {
  addFormData.value.copyFrom = undefined
  addFormData.value.copyFieldsJson = undefined
  copySelected.value = null
}

// 表现形式（参照泛微e9）
const FORM_OPTIONS = {
  text: '单行文本框', textarea: '多行文本框', browse: '浏览按钮', check: 'Check框',
  select: '选择框', attach: '附件上传', date: '日期框', datetime: '日期时间框',
  number: '数字框', radio: '单选框', switch: '开关框', rate: '评分框'
}
/** 数据类型（dtype）：决定数据库列类型 */
const DTYPE_OPTIONS = { text: '文本', date: '日期', datetime: '日期时间', float: '浮点数', int: '整数' }
/** 浏览框数据源（主数据类型，支持自定义扩展） */
const BROWSE_TYPES = {
  user: '人员', dept: '部门', post: '岗位', role: '角色',
  material: '物料', customer: '客户', supplier: '供应商', contract: '合同', custom: '自定义'
}
/** 附件类型约束 */
const ATTACH_TYPES = { all: '不限类型', image: '图片(image/*)', doc: '文档(doc/pdf/xls…)', custom: '自定义后缀' }
/** 表现形式→默认dtype/长度/默认配置（新增字段与切换类型时应用） */
const FORM_DEFAULTS = {
  text:     { dtype: 'text', length: 500 },
  textarea: { dtype: 'text', rows: 4, length: 4000 },
  browse:   { dtype: 'text', browseType: 'user', length: 1000 },
  check:    { dtype: 'text', length: 10 },
  select:   { dtype: 'text', length: 100, selectOptions: [] },
  attach:   { dtype: 'text', length: 2000, attachType: 'all', attachLimit: 5 },
  date:     { dtype: 'date' },
  datetime: { dtype: 'datetime' },
  number:   { dtype: 'float', scale: 2 },
  radio:    { dtype: 'text', length: 100, selectOptions: [] },
  switch:   { dtype: 'text', length: 10 },
  rate:     { dtype: 'int' }
}

const data = reactive({
  form: {},
  fields: [],
  detailTables: [],
  newField: {},
  queryParams: { pageNum: 1, pageSize: 10, formName: undefined, category: undefined }
})
const { queryParams, form, fields, detailTables, newField } = toRefs(data)

const fieldRules = {
  label: [{ required: true, message: '字段显示名不能为空', trigger: 'blur' }],
  field: [
    { required: true, message: '数据库字段名称不能为空', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]{0,39}$/, message: '仅允许小写字母开头的字母/数字/下划线', trigger: 'blur' }
  ]
}

const mainTableName = computed(() => form.value.formKey ? 'flow_data_' + form.value.formKey : 'flow_data_{表单Key}')

const activeFields = computed(() => fields.value.filter(f => (f.table || 'main') === activeTableTab.value))

function fieldCount(json) {
  try { return normalizeFields(JSON.parse(json || '[]')).length } catch (e) { return 0 }
}

/** 兼容旧结构 {type:text/textarea/number/date/select} → {form,dtype,length,scale,table} */
function normalizeFields(arr) {
  return (arr || []).map(f => {
    const n = { ...f, rowKey: f.rowKey || 'r' + Math.random().toString(36).slice(2, 8) }
    if (!n.form) {
      if (n.type === 'textarea') n.form = 'textarea'
      else if (n.type === 'select') n.form = 'select'
      else n.form = 'text'
      if (n.type === 'number') n.dtype = 'float'
      else if (n.type === 'date') n.dtype = 'date'
      else n.dtype = n.dtype || 'text'
    }
    n.dtype = n.dtype || 'text'
    n.length = n.length || 500
    n.scale = n.scale || 2
    n.table = n.table || 'main'
    n.order = n.order || 1
    // 旧选项字符串兼容：options逗号串 → selectOptions[{label,value}]
    if ((n.form === 'select' || n.form === 'radio') && !n.selectOptions && n.options) {
      n.selectOptions = String(n.options).split(/[,，]/).map(s => s.trim()).filter(Boolean).map(s => ({ label: s, value: s }))
    }
    // 新类型默认值兜底
    if (n.form === 'textarea') n.rows = n.rows || 4
    if (n.form === 'browse') n.browseType = n.browseType || 'user'
    if (n.form === 'attach') { n.attachType = n.attachType || 'all'; n.attachLimit = n.attachLimit || 5 }
    return n
  })
}

function getList() {
  loading.value = true
  listForm(queryParams.value).then(res => {
    list.value = res.rows || []
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}
/** 流程分类字典值 → 显示标签 */
function categoryLabel(value) {
  const item = (flowable_category.value || []).find(d => d.value == value)
  return item ? item.label : (value || '未分类')
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.formName = undefined; queryParams.value.category = undefined; handleQuery() }

function handleAdd() {
  addFormData.value = { formName: undefined, category: undefined, copyFrom: undefined, copyFieldsJson: undefined, formDesc: undefined }
  copySelected.value = null
  addOpen.value = true
  nextTick(() => addRef.value && addRef.value.clearValidate())
}

/** 新建：保存(enterDesign=false 仅保存) / 保存并进入详细设置(enterDesign=true) */
function submitAdd(enterDesign) {
  addRef.value.validate(async valid => {
    if (!valid) return
    const fieldsJson = addFormData.value.copyFieldsJson || '[]'
    const payload = { formName: addFormData.value.formName, category: addFormData.value.category || undefined, formDesc: addFormData.value.formDesc || undefined, fieldsJson }
    const res = await addForm(payload)
    const newId = res.data
    const copied = fieldsJson !== '[]'
    addOpen.value = false
    proxy.$modal.msgSuccess(copied ? '新建成功，已复制所选表单的全部字段' : '新建成功，表单Key已自动生成')
    getList()
    if (enterDesign) {
      const detail = await getForm(newId)
      form.value = detail.data
      fields.value = normalizeFields(safeParse(detail.data.fieldsJson))
      layout.value = normalizeLayout(detail.data.layoutJson)
      rebuildDetailTables()
      activeTab.value = 'fields'
      designMax.value = false
      activeTableTab.value = 'main'
      open.value = true
    }
  })
}

async function handleDesign(row, tab = 'basic') {
  const res = await getForm(row.formId)
  form.value = res.data
  fields.value = normalizeFields(safeParse(res.data.fieldsJson))
  layout.value = normalizeLayout(res.data.layoutJson)
  undoStack.value = []
  redoStack.value = []
  rebuildDetailTables()
  if (tab === 'layout') { ensureGridMode(); designMax.value = true } else { designMax.value = false }
  activeTab.value = tab
  activeTableTab.value = 'main'
  open.value = true
}

function safeParse(json) {
  try { return JSON.parse(json || '[]') } catch (e) { return [] }
}

function rebuildDetailTables() {
  const set = new Set(fields.value.map(f => f.table).filter(t => t && t !== 'main'))
  detailTables.value = [...set].sort()
}

function onFormChange(row) {
  applyFormDefaults(row)
}

/** 应用表现形式默认配置（保留用户已填项） */
function applyFormDefaults(row) {
  const d = FORM_DEFAULTS[row.form] || {}
  row.dtype = d.dtype || row.dtype
  if (d.length) row.length = d.length
  if (d.rows) row.rows = d.rows
  if (d.browseType) row.browseType = d.browseType
  if (d.attachType) row.attachType = d.attachType
  if (d.attachLimit) row.attachLimit = d.attachLimit
  if (d.selectOptions) row.selectOptions = d.selectOptions
  if (d.scale) row.scale = d.scale
  if (row.form === 'textarea') { row.dtype = 'text'; row.length = Math.max(row.length || 4000, 4000) }
  if (row.form === 'date') row.dtype = 'date'
  if (row.form === 'datetime') row.dtype = 'datetime'
  if (row.form === 'number') row.dtype = row.dtype === 'int' ? 'int' : 'float'
}

/** dtype 是否随表现形式锁定（日期/日期时间/数字类不允许改dtype） */
function isDtypeLocked(form) { return ['date', 'datetime', 'number'].includes(form) || form === 'textarea' }

/** 自由表现形式下可选的数据类型：日期/日期时间只能通过表现形式（日期框/日期时间框）选择，避免两处重复 */
function dtypeOptions(form) {
  if (isDtypeLocked(form)) return DTYPE_OPTIONS
  const { date, datetime, ...rest } = DTYPE_OPTIONS
  return rest
}

/** 长度是否自动推导（browse/attach/check 等不需要用户填长度） */
function isLengthLocked(row) { return ['browse', 'attach', 'check', 'switch'].includes(row.form) }

/** 选择框选项：[{label,value}] → 文本（显示值:存储值，每行一对） */
function selectOptionsText(row) {
  const opts = row.selectOptions || []
  return opts.map(o => (o.value === o.label || o.value === undefined) ? o.label : (o.label + ':' + o.value)).join('\n')
}

/** 文本 → 选项数组（兼容旧的逗号分隔字符串 options） */
function setSelectOptions(row, text) {
  const arr = (text || '').split(/[\n,，]/).map(s => s.trim()).filter(Boolean).map(pair => {
    const idx = pair.indexOf(':')
    if (idx > 0) return { label: pair.substring(0, idx).trim(), value: pair.substring(idx + 1).trim() }
    return { label: pair, value: pair }
  })
  row.selectOptions = arr
  row.length = Math.max(...arr.map(o => String(o.value || '').length), 10) * 2 + 20
}

function onNewFormChange() { applyFormDefaults(newField.value) }
function onBatchFormChange(row) { applyFormDefaults(row) }

function openAddField() {
  newField.value = { label: undefined, field: undefined, autoField: true, table: activeTableTab.value === '__add' ? 'main' : activeTableTab.value, order: fields.value.length + 1, form: 'text', dtype: 'text', length: 500, scale: 2, options: undefined, required: false }
  addFieldOpen.value = true
  nextTick(() => addFieldRef.value && addFieldRef.value.clearValidate())
}

function confirmAddField() {
  addFieldRef.value.validate(valid => {
    if (!valid) return
    const dup = fields.value.some(f => f.field === newField.value.field)
    if (dup) { proxy.$modal.msgWarning('数据库字段名称已存在：' + newField.value.field); return }
    fields.value.push({ rowKey: 'r' + Math.random().toString(36).slice(2, 8), ...newField.value })
    if (newField.value.table !== 'main' && !detailTables.value.includes(newField.value.table)) {
      detailTables.value.push(newField.value.table)
      detailTables.value.sort()
    }
    activeTableTab.value = newField.value.table
    addFieldOpen.value = false
  })
}

function onFieldSelChange(rows) { checkedFields.value = rows }

// ===== 批量添加字段 =====
const batchOpen = ref(false)
const batchRows = ref([])
function blankBatchRow() {
  return { batchKey: 'b' + Math.random().toString(36).slice(2, 8), label: undefined, field: undefined, autoField: true, form: 'text', dtype: 'text', length: 500, scale: 2, options: undefined, required: false }
}
function openBatchAdd() {
  batchRows.value = [blankBatchRow()]
  batchOpen.value = true
}
function addBatchRow() { batchRows.value.push(blankBatchRow()) }
function removeBatchRow(idx) { batchRows.value.splice(idx, 1) }
function confirmBatchAdd() {
  const rows = batchRows.value.filter(r => (r.label || '').trim() || (r.field || '').trim())
  if (!rows.length) { proxy.$modal.msgWarning('请至少填写一行字段'); return }
  let order = fields.value.length
  for (const r of rows) {
    if (!r.label || !r.field) { proxy.$modal.msgWarning('存在未填写完整的行（字段显示名/数据库字段名称）'); return }
    if (!/^[a-z][a-z0-9_]{0,39}$/.test(r.field)) { proxy.$modal.msgWarning('字段名[' + r.field + ']不合法：小写字母开头的字母/数字/下划线'); return }
  }
  const table = activeTableTab.value === '__add' ? 'main' : activeTableTab.value
  const seen = new Set(fields.value.filter(f => (f.table || 'main') === table).map(f => f.field))
  for (const r of rows) {
    if (seen.has(r.field)) { proxy.$modal.msgWarning('字段名[' + r.field + ']在当前表已存在或本批重复，请修改后再添加'); return }
    seen.add(r.field)
  }
  rows.forEach(r => {
    order++
    fields.value.push({ rowKey: 'r' + Math.random().toString(36).slice(2, 8), table, order, ...r })
  })
  if (table !== 'main' && !detailTables.value.includes(table)) {
    detailTables.value.push(table)
    detailTables.value.sort()
  }
  proxy.$modal.msgSuccess('已批量添加 ' + rows.length + ' 个字段')
  batchOpen.value = false
}

function removeCheckedFields() {
  const keys = checkedFields.value.map(r => r.rowKey)
  fields.value = fields.value.filter(f => !keys.includes(f.rowKey))
  rebuildDetailTables()
  if (activeTableTab.value !== 'main' && !detailTables.value.includes(activeTableTab.value)) {
    activeTableTab.value = 'main'
  }
}

function removeDetailTable(name) {
  fields.value = fields.value.filter(f => f.table !== name)
  detailTables.value = detailTables.value.filter(t => t !== name)
  // 同步清理布局中该明细表的占位块
  for (const sec of layout.value.sections) {
    sec.rows = sec.rows.filter(row => !(row.length === 1 && row[0].detailTable === name))
  }
  layout.value.sections = layout.value.sections.filter(sec => sec.rows.length || sec.title)
  activeTableTab.value = 'main'
}

// ===== 页面设计（类Excel布局设计器） =====
const layoutPreview = ref(false)
const designMax = ref(false)
const layout = ref({ mode: 'section', sections: [], grid: null })

const mainFields = computed(() => fields.value.filter(f => (f.table || 'main') === 'main'))
const detailFields = computed(() => fields.value.filter(f => f.table && f.table !== 'main'))
/** 字段池可选项：主表全部 + 明细表全部（明细可拖入布局单独成行） */
const poolFields = computed(() => [...mainFields.value, ...detailFields.value])

/** 已被布局占用的主表字段（说明行不占用字段） */
function placedFieldSet() {
  const used = new Set()
  for (const sec of layout.value.sections) for (const row of sec.rows) for (const c of row) if (c.field) used.add(c.field)
  return used
}
const unplacedFields = computed(() => {
  const used = placedFieldSet()
  return poolFields.value.filter(f => !used.has(f.field))
})
function mainFieldLabel(field) {
  const f = poolFields.value.find(x => x.field === field)
  return f ? (f.label + '（' + f.field + '）') : (field || '（未选字段）')
}
function fieldShortLabel(field) {
  const f = poolFields.value.find(x => x.field === field)
  return f ? f.label : field || ''
}
function addSection() { layout.value.sections.push({ title: '', rows: [] }) }
function removeSection(si) { layout.value.sections.splice(si, 1); sel.value = null }
function moveSection(si, dir) {
  const arr = layout.value.sections
  const t = si + dir
  if (t < 0 || t >= arr.length) return
  const [s] = arr.splice(si, 1); arr.splice(t, 0, s)
}
function addRow(si) { layout.value.sections[si].rows.push([{ field: undefined, span: 12 }]) }
function insertRow(si, ri) { layout.value.sections[si].rows.splice(ri + 1, 0, [{ field: undefined, span: 12 }]) }
function removeRow(si, ri) { layout.value.sections[si].rows.splice(ri, 1); sel.value = null }
function moveRow(si, ri, dir) {
  const rows = layout.value.sections[si].rows
  const t = ri + dir
  if (t < 0 || t >= rows.length) return
  const [r] = rows.splice(ri, 1); rows.splice(t, 0, r)
}
function addNoteRow() {
  if (!layout.value.sections.length) addSection()
  const sec = layout.value.sections[layout.value.sections.length - 1]
  sec.rows.push([{ note: '', span: 24 }])
  sel.value = { si: layout.value.sections.length - 1, ri: sec.rows.length - 1, ci: 0 }
}

// ===== 明细表块（参照泛微 e-cology：明细表整体作为占位块插入布局，含表头/空白行/表尾合计） =====
/** 明细表显示名：dt1 → 明细表1 */
function detailTableLabel(dt) {
  const idx = detailTables.value.indexOf(dt)
  return '明细表' + (idx >= 0 ? String(idx + 1) : dt.slice(2))
}
/** 明细表字段清单（按显示顺序） */
function detailTableFields(dt) {
  return fields.value.filter(f => f.table === dt).sort((a, b) => (a.order || 1) - (b.order || 1))
}
/** 插入明细表块：整行 24 格，独立占位（泛微式：明细表区域不与其他字段混排） */
function addDetailBlock(dt) {
  if (!dt) return
  if (!layout.value.sections.length) addSection()
  const sec = layout.value.sections[layout.value.sections.length - 1]
  pushUndo()
  sec.rows.push([{ detailTable: dt, detailSum: [], span: 24 }])
  sel.value = { si: layout.value.sections.length - 1, ri: sec.rows.length - 1, ci: 0 }
}
function appendFieldToLastRow(field) {
  if (!layout.value.sections.length) addSection()
  const sec = layout.value.sections[layout.value.sections.length - 1]
  if (!sec.rows.length) sec.rows.push([])
  const row = sec.rows[sec.rows.length - 1]
  const usedSpan = row.reduce((s, c) => s + (c.span || 12), 0)
  if (usedSpan + 12 <= 24) row.push({ field, span: 12 })
  else sec.rows.push([{ field, span: 12 }])
}

// ---- 撤销/重做（快照栈，上限50步） ----
const undoStack = ref([])
const redoStack = ref([])
function pushUndo() {
  undoStack.value.push(JSON.stringify(layout.value))
  if (undoStack.value.length > 50) undoStack.value.shift()
  redoStack.value = []
}
const canUndo = computed(() => undoStack.value.length > 0)
const canRedo = computed(() => redoStack.value.length > 0)
function undoLayout() {
  if (!undoStack.value.length) return
  redoStack.value.push(JSON.stringify(layout.value))
  layout.value = JSON.parse(undoStack.value.pop())
  sel.value = null
}
function redoLayout() {
  if (!redoStack.value.length) return
  undoStack.value.push(JSON.stringify(layout.value))
  layout.value = JSON.parse(redoStack.value.pop())
  sel.value = null
}

// ---- 单元格右键菜单 ----
const ctxMenu = reactive({ show: false, x: 0, y: 0, si: 0, ri: 0, ci: 0 })
const ctxCell = computed(() => {
  const sec = layout.value.sections[ctxMenu.si]
  const row = sec && sec.rows[ctxMenu.ri]
  return row ? row[ctxMenu.ci] : null
})
function openCtxMenu(e, si, ri, ci) {
  selectCell(si, ri, ci)
  const wrap = e.currentTarget.closest('.ed-wrap')
  const rect = wrap ? wrap.getBoundingClientRect() : { left: 0, top: 0 }
  ctxMenu.x = Math.min(e.clientX - rect.left, 620)
  ctxMenu.y = Math.min(e.clientY - rect.top, 360)
  ctxMenu.si = si; ctxMenu.ri = ri; ctxMenu.ci = ci
  ctxMenu.show = true
}
function closeCtxMenu() { ctxMenu.show = false }
// ---- 合并/拆分单元格（横向合并：并入右侧相邻单元格，宽度累加） ----
const canMergeRight = computed(() => {
  const { si, ri, ci } = ctxMenu
  const row = layout.value.sections[si]?.rows?.[ri]
  if (!row || ci >= row.length - 1) return false
  const cur = row[ci], nxt = row[ci + 1]
  if (cur.detailTable || nxt.detailTable) return false
  return (cur.span + nxt.span) <= 24
})
function ctxAction(action) {
  const { si, ri, ci } = ctxMenu
  const sec = layout.value.sections[si]
  const row = sec && sec.rows[ri]
  const cell = row && row[ci]
  if (!cell) { ctxMenu.show = false; return }
  if (action !== 'mergeRight' && action !== 'splitCell') ctxMenu.show = false
  else ctxMenu.show = false
  pushUndo()
  if (action === 'mergeRight') {
    const nxt = row[ci + 1]
    if (!nxt) return
    cell.span = (cell.span || 12) + (nxt.span || 12)
    // 内容合并策略：本格为空则吸收右格内容，否则丢弃右格内容
    if (!cell.field && !cell.note && !cell.label) { cell.field = nxt.field; cell.note = nxt.note; cell.label = nxt.label }
    row.splice(ci + 1, 1)
  }
  else if (action === 'splitCell') {
    const span = cell.span || 12
    if (span <= 6 || cell.detailTable) return
    const half = span / 2
    cell.span = half
    row.splice(ci + 1, 0, { field: undefined, span: half })
    selectCell(si, ri, ci + 1)
  }
  else if (action === 'insertRight') { row.splice(ci + 1, 0, { field: undefined, span: 12 }); selectCell(si, ri, ci + 1) }
  else if (action === 'insertRowBelow') { sec.rows.splice(ri + 1, 0, [{ field: undefined, span: 12 }]); selectCell(si, ri + 1, 0) }
  else if (action === 'duplicate') { row.splice(ci + 1, 0, { ...cell }); selectCell(si, ri, ci + 1) }
  else if (action === 'toNote') { cell.note = ''; delete cell.field; cell.span = 24; cell.noteLabel = undefined }
  else if (action === 'editLabel') { cellEdit(si, ri, ci) }
  else if (action === 'del') { removeActiveCell() }
}
function cellEdit(si, ri, ci) {
  const row = layout.value.sections[si]?.rows?.[ri]
  const cell = row && row[ci]
  if (!cell) return
  selectCell(si, ri, ci)
  proxy.$prompt(cell.note ? '请输入说明文字' : '自定义标签（留空使用字段显示名）', cell.note ? '编辑说明行' : '自定义标签', {
    confirmButtonText: '确 定', cancelButtonText: '取 消',
    inputValue: cell.note || cell.label || ''
  }).then(({ value }) => {
    pushUndo()
    if (cell.note) cell.note = value || ''
    else cell.label = value || undefined
  }).catch(() => {})
}

// ---- 单元格样式 ----
const FONT_OPTIONS = [
  { l: '默认', v: undefined }, { l: '微软雅黑', v: 'Microsoft YaHei' }, { l: '宋体', v: 'SimSun' },
  { l: '黑体', v: 'SimHei' }, { l: '楷体', v: 'KaiTi' }, { l: '仿宋', v: 'FangSong' }, { l: '等线', v: 'DengXian' }
]
/** 单元格样式 → 内联样式对象（设计器与发起页共用同一规则） */
function cellStyle(cell) {
  if (!cell) return {}
  const s = {}
  if (cell.fontFamily) s.fontFamily = cell.fontFamily
  if (cell.fontSize) s.fontSize = cell.fontSize + 'px'
  if (cell.bold) s.fontWeight = '700'
  if (cell.italic) s.fontStyle = 'italic'
  if (cell.color) s.color = cell.color
  if (cell.bgColor) s.backgroundColor = cell.bgColor
  if (cell.borderColor) s.borderColor = cell.borderColor
  return s
}
function clearCellStyle() {
  const c = activeCell.value
  if (!c) return
  pushUndo()
  ;['fontFamily', 'fontSize', 'bold', 'italic', 'color', 'bgColor', 'border', 'borderColor'].forEach(k => { delete c[k] })
}

// ---- 键盘：Delete 删除选中单元格，Ctrl+Z 撤销 ----
function onLayoutKeydown(e) {
  if (!open.value || activeTab.value !== 'layout' || layoutPreview.value) return
  const tag = (e.target.tagName || '').toLowerCase()
  if (tag === 'input' || tag === 'textarea' || e.target.isContentEditable) return
  if (e.key === 'Delete' && sel.value) { pushUndo(); removeActiveCell(); e.preventDefault() }
  else if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'z' && !e.shiftKey) { if (canUndo.value) { undoLayout(); e.preventDefault() } }
  else if ((e.ctrlKey || e.metaKey) && (e.key.toLowerCase() === 'y' || (e.shiftKey && e.key.toLowerCase() === 'z'))) { if (canRedo.value) { redoLayout(); e.preventDefault() } }
}
window.addEventListener('keydown', onLayoutKeydown)
onBeforeUnmount(() => window.removeEventListener('keydown', onLayoutKeydown))
watch(() => ctxMenu.show, v => { if (!v) return; document.addEventListener('click', closeCtxMenu, { once: true }) })

// ---- 拖拽 ----
const dragField = ref(null)
const sel = ref(null) // {si, ri, ci}
const activeCell = computed(() => {
  const s = sel.value
  if (!s) return null
  const row = (layout.value.sections[s.si] || {}).rows?.[s.ri]
  return row ? (row[s.ci] || null) : null
})
function selectCell(si, ri, ci) { sel.value = { si, ri, ci } }
function isCellActive(si, ri, ci) { const s = sel.value; return !!s && s.si === si && s.ri === ri && s.ci === ci }
function removeActiveCell() {
  const s = sel.value
  if (!s) return
  const row = layout.value.sections[s.si]?.rows?.[s.ri]
  if (row) row.splice(s.ci, 1)
  sel.value = null
}
function onPoolDragStart(e, field) {
  dragField.value = field
  if (e.dataTransfer) e.dataTransfer.setData('text/plain', 'field:' + field)
}
function onCellDragStart(e, si, ri, ci) {
  if (e.dataTransfer) e.dataTransfer.setData('text/plain', 'cell:' + si + ':' + ri + ':' + ci)
}
/** 拖入单元格：字段 → 插到该单元格后；单元格 → 换位 */
function onDropToCell(e, si, ri, ci) {
  const data = e.dataTransfer ? e.dataTransfer.getData('text/plain') || '' : ''
  if (data.startsWith('cell:')) {
    const [x, xsi, xri, xci] = data.split(':')
    if (+xsi === si && +xri === ri && +xci === ci) return
    const srcRow = layout.value.sections[+xsi]?.rows?.[+xri]
    if (!srcRow) return
    const [cell] = srcRow.splice(+xci, 1)
    const dstRow = layout.value.sections[si].rows[ri]
    dstRow.splice(ci > srcRow.length ? ci : ci, 0, cell)
    return
  }
  const field = dragField.value || (data.startsWith('field:') ? data.slice(6) : null)
  if (!field) return
  const sec = layout.value.sections[si]
  if (!sec) { appendFieldToLastRow(field); dragField.value = null; return }
  if (!sec.rows.length) sec.rows.push([])
  const row = sec.rows[Math.min(ri, sec.rows.length - 1)]
  const usedSpan = row.reduce((s, c) => s + (c.span || 12), 0)
  row.splice(Math.min(ci + 1, row.length), 0, { field, span: usedSpan + 12 > 24 ? 24 : 12 })
  dragField.value = null
}
/** 拖入行空白：追加到该行末尾 */
function onDropToRow(e, si, ri) {
  const data = e.dataTransfer ? e.dataTransfer.getData('text/plain') || '' : ''
  if (data.startsWith('cell:')) { onDropToCell(e, si, ri, 999); return }
  const field = dragField.value || (data.startsWith('field:') ? data.slice(6) : null)
  if (!field) return
  onDropToCell(e, si, ri, 999)
}
function onDropToSection(e, si) {
  const data = e.dataTransfer ? e.dataTransfer.getData('text/plain') || '' : ''
  const field = dragField.value || (data.startsWith('field:') ? data.slice(6) : null)
  if (!field) return
  const sec = layout.value.sections[si]
  if (!sec) return
  sec.rows.push([{ field, span: 12 }])
  dragField.value = null
}
function onDropToEnd(e) {
  const data = e.dataTransfer ? e.dataTransfer.getData('text/plain') || '' : ''
  const field = dragField.value || (data.startsWith('field:') ? data.slice(6) : null)
  if (!field) return
  layout.value.sections.push({ title: '', rows: [[{ field, span: 12 }]] })
  dragField.value = null
}

function autoLayout() {
  const secs = [{ title: '基本信息', rows: [] }]
  let row = []
  for (const f of mainFields.value) {
    row.push({ field: f.field, span: 12 })
    if (row.length === 2) { secs[0].rows.push(row); row = [] }
  }
  if (row.length) secs[0].rows.push(row)
  layout.value = { sections: secs }
  proxy.$modal.msgSuccess('已按两栏自动生成布局，可继续拖拽调整')
}

/** 预览：含未布局字段回退区与说明行 */
const previewSections = computed(() => {
  const list = layout.value.sections.map(sec => ({
    title: sec.title,
    cells: sec.rows.flat().filter(c => c.field || c.note || c.detailTable).map(c => ({ ...c }))
  }))
  const rest = unplacedFields.value
  if (rest.length) list.push({ title: '其他信息', cells: rest.map(f => ({ field: f.field, span: 12 })) })
  return list
})

/** 单元格合法宽度：标准值(6/8/12/24)直接放行，合并宽度(6<x<24)放行，其余归一为默认 */
function cellSpan(c) {
  const s = Number(c.span)
  if ([6, 8, 12, 24].includes(s)) return s
  if (s > 6 && s < 24) return s
  return c.note ? 24 : 12
}

function normalizeLayout(json) {
  const p = parseLayout(json)
  const sections = p.sections.map(s => ({
    title: s.title || '',
    collapsed: !!s.collapsed,
    rows: (Array.isArray(s.rows) ? s.rows : [])
      .map(r => (Array.isArray(r) ? r : []).filter(c => c && typeof c === 'object' && (c.field || c.note || c.detailTable)).map(c => {
        // 明细表块：整行占位，仅保留明细表 key 与合计字段配置
        if (c.detailTable) {
          return { detailTable: c.detailTable, detailSum: Array.isArray(c.detailSum) ? c.detailSum.filter(x => detailTableFields(c.detailTable).some(f => f.field === x)) : [], span: 24 }
        }
        const n = { field: c.note ? undefined : c.field, note: c.note || undefined, label: c.label || undefined, noteLabel: c.noteLabel || undefined, span: cellSpan(c) }
        for (const k of ['fontFamily', 'fontSize', 'bold', 'italic', 'color', 'bgColor', 'border', 'borderColor']) if (c[k] !== undefined && c[k] !== null && c[k] !== '') n[k] = c[k]
        return n
      }))
      .filter(r => r.length)
  }))
  return { mode: p.grid ? 'grid' : 'section', sections, grid: p.grid }
}

/** 确保 Excel 网格模式：无网格时按旧布局/字段一键初始化（旧布局保留不丢） */
function ensureGridMode() {
  if (!layout.value.grid) {
    layout.value.grid = (layout.value.sections && layout.value.sections.length)
      ? sectionsToGrid(layout.value.sections)
      : fieldsToGrid(mainFields.value)
    proxy.$modal.msgSuccess('已初始化 Excel 网格，可继续拖拽布局与格式化，操作见右上"帮助"')
  }
  layout.value.mode = 'grid'
}

/** 顶部模式切换按钮：grid ↔ section 双向切换（互不销毁，双布局并存） */
function toggleLayoutMode() {
  if (layout.value.mode === 'grid') {
    layout.value.mode = 'section'
  } else {
    ensureGridMode()
  }
}


// "+" 添加明细表页签：新增后直接弹出添加字段
watch(activeTableTab, v => {
  if (v === '__add') {
    const used = new Set(['main', ...detailTables.value])
    let i = 1
    while (used.has('dt' + i)) i++
    const dt = 'dt' + i
    detailTables.value.push(dt)
    detailTables.value.sort()
    nextTick(() => { activeTableTab.value = dt; openAddField() })
  }
})

function submitForm(keepOpen) {
  if (!form.value.formName || !form.value.formKey) { proxy.$modal.msgWarning('请填写表单名称与表单Key'); return }
  if (!/^[a-z0-9][a-z0-9_]{0,39}$/.test(form.value.formKey)) { proxy.$modal.msgWarning('表单Key仅允许小写字母/数字开头的字母、数字、下划线'); return }
  for (const f of fields.value) {
    if (!f.label || !f.field) { proxy.$modal.msgWarning('存在未填写完整（显示名/字段名）的字段'); return }
    if (!/^[a-z][a-z0-9_]{0,39}$/.test(f.field)) { proxy.$modal.msgWarning('字段名[' + f.field + ']不合法：小写字母开头的字母/数字/下划线'); return }
    if (f.dtype === 'text' && (!f.length || f.length < 1)) { proxy.$modal.msgWarning('字段[' + f.label + ']请填写文本长度'); return }
  }
  // 同一张表内数据库字段名不得重复（主表与明细表为独立物理表，跨表允许同名）
  const seen = {}
  for (const f of fields.value) {
    const key = (f.table || 'main') + '.' + f.field
    if (seen[key]) { proxy.$modal.msgWarning('字段名[' + f.field + ']在' + (f.table === 'main' || !f.table ? '主表' : '明细表 ' + f.table + ' ') + '中重复出现（' + seen[key] + ' / ' + f.label + '），请修改后再保存'); return }
    seen[key] = f.label
  }
  const payload = {
    formId: form.value.formId, formKey: form.value.formKey, formName: form.value.formName,
    category: form.value.category || undefined,
    formDesc: form.value.formDesc || undefined,
    processKey: form.value.processKey || undefined,
    fieldsJson: JSON.stringify(fields.value.map(({ rowKey, ...rest }) => rest)),
    layoutJson: serializeLayout({ mode: layout.value.mode, sections: layout.value.sections.map(s => ({ title: s.title, rows: s.rows })), grid: layout.value.grid })
  }
  if (form.value.formId) {
    updateForm(payload).then(() => {
      proxy.$modal.msgSuccess('保存成功')
      if (!keepOpen) open.value = false
      getList()
    })
  } else {
    addForm(payload).then(() => {
      proxy.$modal.msgSuccess('新增成功')
      if (!keepOpen) open.value = false
      getList()
    })
  }
}

function handleCreateTable(row) {
  proxy.$modal.confirm('将按当前字段定义生成数据表 ' + 'flow_data_' + row.formKey + '（明细表自动追加 _dt1/_dt2…，已存在的表仅补齐缺失字段列）。确认执行？')
    .then(() => createTable(row.formId))
    .then(res => { proxy.$modal.msgSuccess('数据表已就绪：' + res.data); getList() })
    .catch(() => {})
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除表单"' + row.formName + '"？其数据表不会被删除').then(() => delForm(row.formId)).then(() => {
    proxy.$modal.msgSuccess('删除成功'); getList()
  }).catch(() => {})
}

getList()
onActivated(() => { getList() })
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

.basic-form { padding: 8px 12px 0; }
.field-tab-bar { margin: 0 12px; }
.field-tab-bar .tab-tbl { font-size: 12px; color: var(--ink-500, #64748b); }
.field-tab-bar .tab-add { font-weight: 700; color: var(--brand-600, #4f46e5); }
.field-toolbar { display: flex; align-items: center; gap: 8px; margin: 10px 12px; }
.field-tip { font-size: 12px; color: var(--ink-500, #94a3b8); margin-left: 8px; }
.field-table { margin: 0 12px 8px; }
.form-tip { font-size: 12px; color: var(--ink-500, #94a3b8); line-height: 1.4; margin-top: 4px; }
.copy-field { display: flex; gap: 8px; width: 100%; }
.copy-field .el-input { flex: 1; }
.copy-clear { cursor: pointer; color: var(--ink-400, #94a3b8); }
.copy-clear:hover { color: var(--red-500, #ef4444); }
.copy-picker-bar { display: flex; gap: 8px; margin-bottom: 10px; }
.copy-table { width: 100%; }
.batch-toolbar { display: flex; align-items: center; gap: 8px; margin: 0 12px 10px; }
.tbl-name-row { line-height: 28px; }

/* ===== 明细表块（参照泛微 e-cology） ===== */
.ed-cell.is-detail { background: #f8fafc; }
.ed-detail-mini { display: block; width: 100%; font-size: 11px; }
.ed-detail-head { display: flex; background: #fef3c7; border: 1px solid #fcd34d; border-radius: 3px 3px 0 0; }
.ed-detail-th { flex: 1; padding: 3px 6px; color: #92400e; font-weight: 600; border-right: 1px solid #fde68a; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.ed-detail-th:last-child { border-right: none; }
.ed-detail-tr { display: flex; border: 1px solid #e2e8f0; border-top: none; background: #fff; }
.ed-detail-td { flex: 1; min-height: 16px; padding: 2px 6px; border-right: 1px dashed #eef2f7; text-align: center; color: #334155; }
.ed-detail-td:last-child { border-right: none; }
.ed-detail-sum { background: #f8fafc; color: #7c3aed; font-weight: 600; }
/* 预览态明细表 */
.pv-detail { border: 1px solid #e2e8f0; border-radius: 6px; overflow: hidden; margin: 2px 0 8px; }
.pv-detail-title { font-size: 13px; font-weight: 600; color: #0f172a; background: #f8fafc; padding: 5px 10px; border-bottom: 1px solid #e2e8f0; }
.pv-detail-table { width: 100%; border-collapse: collapse; font-size: 12px; }
.pv-detail-table th { background: #fef3c7; color: #92400e; padding: 5px 8px; border: 1px solid #fde68a; font-weight: 600; }
.pv-detail-table td { border: 1px solid #eef2f7; padding: 5px 8px; height: 22px; text-align: center; color: #94a3b8; }
.pv-detail-sum td { background: #f8fafc; color: #7c3aed; font-weight: 600; }

/* ===== 页面设计（类Excel布局设计器） ===== */
.ed-wrap { display: flex; gap: 12px; margin: 0 12px 10px; align-items: flex-start; position: relative; }
.ed-main { flex: 1; min-width: 0; }
.ed-toolbar { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; flex-wrap: wrap; }
.ed-canvas { max-height: 470px; overflow: auto; display: flex; flex-direction: column; gap: 10px; padding-right: 2px; }
.ed-ruler { display: flex; border: 1px solid #e2e8f0; border-radius: 6px 6px 0 0; background: #f1f5f9; overflow: hidden; }
.ed-ruler-corner { width: 42px; flex: none; border-right: 1px solid #e2e8f0; }
.ed-ruler-label { width: 110px; flex: none; }
.ed-ruler-col { flex: 1; text-align: center; font-size: 11px; color: #64748b; padding: 3px 0; border-right: 1px dashed #dbe3ec; font-family: Consolas, monospace; }
.ed-ruler-col:last-child { border-right: none; }
.ed-empty { text-align: center; color: var(--ink-400, #94a3b8); font-size: 13px; padding: 40px 0; border: 1px dashed #cbd5e1; border-radius: 8px; }
.ed-section { border: 1px solid #e2e8f0; border-radius: 8px; background: #fff; }
.ed-sec-bar { display: flex; align-items: center; gap: 8px; background: #f8fafc; border-bottom: 1px solid #e2e8f0; padding: 5px 8px; border-radius: 8px 8px 0 0; }
.ed-sec-title { flex: 1; border: 1px dashed transparent; background: transparent; font-size: 13px; font-weight: 600; color: #0f172a; padding: 3px 6px; border-radius: 4px; outline: none; min-width: 0; }
.ed-sec-title:hover { border-color: #cbd5e1; background: #fff; }
.ed-sec-title:focus { border-color: var(--brand-600, #4f46e5); background: #fff; }
.ed-sec-ops { display: flex; align-items: center; gap: 0; flex: none; }
.ed-row { display: flex; align-items: stretch; border-bottom: 1px dashed #eef2f7; }
.ed-row:last-of-type { border-bottom: none; }
.ed-row-cells { flex: 1; display: flex; min-width: 0; padding: 6px 0 6px 8px; gap: 0; }
.ed-cell { box-sizing: border-box; border: 1px solid #dbe3ec; border-left: none; background: #fbfcfe; cursor: grab; position: relative; display: flex; min-width: 0; }
.ed-cell:first-child { border-left: 1px solid #dbe3ec; }
.ed-cell:hover { border-color: #94a3b8; }
.ed-cell.is-active { border: 1px solid var(--brand-600, #4f46e5); box-shadow: 0 0 0 2px rgba(79, 70, 229, .12); z-index: 1; }
.ed-cell.is-note { background: #fffbeb; border-color: #fcd34d; }
.ed-cell-label { width: 96px; flex: none; font-size: 12px; color: #475569; background: #f1f5f9; border-right: 1px solid #dbe3ec; padding: 8px 6px; display: flex; align-items: center; gap: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.ed-cell.is-note .ed-cell-label { background: #fef3c7; }
.ed-cell-ctrl { flex: 1; font-size: 12px; color: #94a3b8; padding: 8px 8px; display: flex; align-items: center; gap: 6px; min-width: 0; background-image: repeating-linear-gradient(to right, transparent 0, transparent calc(25% - 1px), #eef2f7 calc(25% - 1px), #eef2f7 25%); }
.ed-cell-placeholder { color: #cbd5e1; }
.ed-note-text { color: #b45309; font-size: 12px; }
.ed-ctrl-demo { width: 40%; height: 14px; border: 1px solid #dbe3ec; border-radius: 3px; background: #fff; flex: none; }
.ed-row-ops { width: 34px; flex: none; display: flex; flex-direction: column; justify-content: center; align-items: center; gap: 0; opacity: .25; transition: opacity .15s; }
.ed-row:hover .ed-row-ops { opacity: 1; }
.ed-sec-empty { font-size: 12px; color: var(--ink-400, #94a3b8); text-align: center; padding: 10px 0; }
.ed-drop-last { border: 1px dashed #cbd5e1; border-radius: 8px; text-align: center; font-size: 12px; color: #94a3b8; padding: 10px 0; margin-top: 2px; }
.ed-drop-last:hover { border-color: var(--brand-600, #4f46e5); color: var(--brand-600, #4f46e5); }
.ed-side { width: 230px; flex: none; border: 1px solid #e2e8f0; border-radius: 8px; background: #fff; max-height: 500px; overflow: auto; }
.ed-side-title { display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 600; color: #0f172a; padding: 8px 10px; border-bottom: 1px solid #f1f5f9; position: sticky; top: 0; background: #fff; }
.ed-pool { padding: 8px; display: flex; flex-direction: column; gap: 6px; }
.ed-pool-item { display: flex; align-items: center; gap: 6px; border: 1px solid #e2e8f0; border-radius: 6px; padding: 6px 8px; cursor: grab; font-size: 12px; background: #fafbfc; }
.ed-pool-item:hover { border-color: var(--brand-600, #4f46e5); color: var(--brand-600, #4f46e5); }
.ed-pool-item .pi-label { flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.ed-pool-item em { font-style: normal; color: #94a3b8; font-size: 11px; }
.ed-pool-empty { font-size: 12px; color: #94a3b8; text-align: center; padding: 8px 0; }
.ed-props { padding: 10px; display: flex; flex-direction: column; gap: 10px; }
.ed-prop label { display: block; font-size: 12px; color: #64748b; margin-bottom: 4px; }
.ed-props-empty { font-size: 12px; color: #94a3b8; text-align: center; padding: 14px 8px; }
.ed-cell.cell-b-none { border-color: transparent !important; }
.ed-cell.cell-b-none .ed-cell-label { border-right-color: transparent; background: transparent; }
.ed-cell.cell-b-all { outline: 1px solid #94a3b8; outline-offset: -1px; }
.ed-cell.cell-b-bottom { border-bottom-width: 2px; }
.ed-props .ed-prop-row { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.ed-color-item { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #64748b; }
.ed-ctx-sep { height: 1px; background: #f1f5f9; margin: 4px 8px; }
.ed-merge-badge { margin-left: auto; font-size: 10px; color: #7c3aed; background: #f5f3ff; border: 1px solid #ddd6fe; border-radius: 4px; padding: 0 4px; flex: none; }
.ed-cell-label { justify-content: flex-start; gap: 4px; }
.ed-ctxmenu { position: absolute; z-index: 30; background: #fff; border: 1px solid #e2e8f0; border-radius: 8px; box-shadow: 0 8px 24px rgba(15, 23, 42, .12); padding: 4px; min-width: 148px; }
.ed-ctx-item { font-size: 13px; color: #334155; padding: 7px 12px; border-radius: 6px; cursor: pointer; }
.ed-ctx-item:hover { background: #f1f5f9; }
.ed-ctx-item.danger { color: #ef4444; }
.ed-ctx-item.danger:hover { background: #fef2f2; }
/* 预览态不显示默认背景框，只保留内容（用户设置的边框由 pv-b-* 条件类渲染） */
.layout-preview { margin: 0; padding: 14px 16px; max-height: 470px; overflow: auto; }
.pv-section-title { font-size: 14px; font-weight: 600; color: #0f172a; border-left: 3px solid var(--brand-600, #4f46e5); padding-left: 8px; margin: 6px 0 10px; }
.pv-row { margin-bottom: 4px; }
/* 预览态不显示默认线框，只显示用户设置的边框（pv-b-all / pv-b-bottom 由 c.border 条件类控制） */
.pv-item { display: flex; align-items: flex-start; padding: 6px 4px; min-height: 34px; }
.pv-label { width: 120px; flex: none; font-size: 13px; color: var(--ink-500, #64748b); }
.pv-value { flex: 1; font-size: 13px; color: #cbd5e1; }
.pv-note { background: #fffbeb; border: 1px dashed #fcd34d; border-radius: 6px; color: #b45309; font-size: 12px; padding: 6px 10px; margin: 2px 0 6px; }
.pv-b-all { border: 1px solid #94a3b8; border-radius: 4px; padding: 4px; }
.pv-b-bottom { border-bottom: 2px solid #94a3b8; }
.layout-mode-bar { display: flex; align-items: center; gap: 10px; margin: 0 12px 8px; }
.rd-max-btn {
  position: absolute; right: 52px; top: 50%; transform: translateY(-50%);
  font-weight: 600; padding: 0 14px; height: 32px; font-size: 13px;
}
</style>

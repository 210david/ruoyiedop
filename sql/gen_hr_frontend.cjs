// 批量生成HR模块前端API JS文件和Vue3 index.vue页面
const fs = require('fs');
const path = require('path');

const config = JSON.parse(fs.readFileSync(path.join(__dirname, 'hr_entities.json'), 'utf8'));

const apiDir = 'e:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-ui/src/api/hr';
const viewDir = 'e:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-ui/src/views/hr';

function cap(s) { return s.charAt(0).toUpperCase() + s.slice(1); }
function uncap(s) { return s.charAt(0).toLowerCase() + s.slice(1); }

// Generate API JS file
function genApiJS(e) {
  const name = uncap(e.cls);
  const idName = e.pk;
  return `import request from '@/utils/request'

// 查询${e.title}列表
export function list${e.cls}(query) {
  return request({
    url: '/hr/${e.path}/list',
    method: 'get',
    params: query
  })
}

// 查询${e.title}详细
export function get${e.cls}(${idName}) {
  return request({
    url: '/hr/${e.path}/' + ${idName},
    method: 'get'
  })
}

// 新增${e.title}
export function add${e.cls}(data) {
  return request({
    url: '/hr/${e.path}',
    method: 'post',
    data: data
  })
}

// 修改${e.title}
export function update${e.cls}(data) {
  return request({
    url: '/hr/${e.path}',
    method: 'put',
    data: data
  })
}

// 删除${e.title}
export function del${e.cls}(${idName}) {
  return request({
    url: '/hr/${e.path}/' + ${idName},
    method: 'delete'
  })
}
`;
}

// Determine which fields to show in the table (first 6 non-system fields)
function getTableFields(e) {
  return e.fields.slice(0, 8);
}

// Determine which fields to show in the form (exclude system fields)
function getFormFields(e) {
  return e.fields.filter(f => !['delFlag','status'].includes(f[0]));
}

// Generate Vue3 index.vue page
function genVuePage(e) {
  const name = uncap(e.cls);
  const tableFields = getTableFields(e);
  const formFields = getFormFields(e);
  const requiredFields = e.fields.filter(f => f[4] === true);
  
  // Build query params
  const queryParams = tableFields.slice(0, 4).map(f => `${f[0]}: undefined`).join(', ');
  
  // Build table columns
  const tableCols = tableFields.map(f => {
    const isDate = f[2] === 'Date';
    const col = `      <el-table-column label="${f[3]}" prop="${f[0]}" align="center" ${isDate ? 'width="120"' : ''} />`;
    return col;
  }).join('\n');
  
  // Build form items (2-column layout)
  const formItems = formFields.map(f => {
    const isDate = f[2] === 'Date';
    const isRequired = f[4] === true;
    let control;
    if (isDate) {
      control = `<el-date-picker v-model="form.${f[0]}" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />`;
    } else if (f[2] === 'Integer' || f[2] === 'Long' || f[2] === 'BigDecimal') {
      control = `<el-input-number v-model="form.${f[0]}" :precision="${f[2] === 'BigDecimal' ? 2 : 0}" :min="0" controls-position="right" style="width: 100%" />`;
    } else {
      control = `<el-input v-model="form.${f[0]}" placeholder="请输入${f[3]}" />`;
    }
    return `        <el-col :span="12">
          <el-form-item label="${f[3]}" prop="${f[0]}">
            ${control}
          </el-form-item>
        </el-col>`;
  }).join('\n');
  
  // Build rules
  const rules = requiredFields.map(f => `    ${f[0]}: [{ required: true, message: '${f[3]}不能为空', trigger: 'blur' }]`).join(',\n');
  
  // Build reset form
  const resetForm = formFields.map(f => {
    let defaultVal;
    if (f[2] === 'String') defaultVal = 'undefined';
    else if (f[2] === 'Integer' || f[2] === 'Long') defaultVal = 'undefined';
    else if (f[2] === 'BigDecimal') defaultVal = 'undefined';
    else if (f[2] === 'Date') defaultVal = 'undefined';
    else defaultVal = 'undefined';
    return `    ${f[0]}: ${defaultVal}`;
  }).join(',\n');
  
  return `<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
${tableFields.slice(0,4).map(f => `      <el-form-item label="${f[3]}" prop="${f[0]}">
        <el-input v-model="queryParams.${f[0]}" placeholder="请输入${f[3]}" clearable @keyup.enter="handleQuery" />
      </el-form-item>`).join('\n')}
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['${e.perms}:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['${e.perms}:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['${e.perms}:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['${e.perms}:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="${name}List" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
${tableCols}
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['${e.perms}:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['${e.perms}:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="${e.title}" append-to-body>
      <el-form ref="${name}Ref" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
${formItems}
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="${e.cls}">
import { list${e.cls}, get${e.cls}, add${e.cls}, update${e.cls}, del${e.cls} } from '@/api/hr/${name}'

const { proxy } = getCurrentInstance()

const ${name}List = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, ${queryParams} },
  rules: {
${rules}
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  list${e.cls}(queryParams.value).then(response => {
    ${name}List.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  ${tableFields.slice(0,4).map(f => `queryParams.value.${f[0]} = undefined`).join('\n  ')}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.${e.pk})
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
${resetForm}
  }
  proxy.resetForm('${name}Ref')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const ${e.pk} = row.${e.pk} || ids.value[0]
  get${e.cls}(${e.pk}).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['${name}Ref'].validate(valid => {
    if (valid) {
      if (form.value.${e.pk} != undefined) {
        update${e.cls}(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        add${e.cls}(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const ${e.pk}s = row.${e.pk} || ids.value
  proxy.$modal.confirm('是否确认删除选中的${e.title}？').then(function() {
    return del${e.cls}(${e.pk}s)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/${e.path}/export', { ...queryParams.value }, \`${name}_\${new Date().getTime()}.xlsx\`)
}

getList()
</script>
`;
}

// Generate all files
config.entities.forEach(e => {
  // API JS
  fs.mkdirSync(apiDir, { recursive: true });
  fs.writeFileSync(path.join(apiDir, `${uncap(e.cls)}.js`), genApiJS(e), 'utf8');
  
  // Vue page
  const pageDir = path.join(viewDir, e.path);
  fs.mkdirSync(pageDir, { recursive: true });
  fs.writeFileSync(path.join(pageDir, 'index.vue'), genVuePage(e), 'utf8');
  
  console.log(`Generated: ${e.cls} -> api/hr/${uncap(e.cls)}.js, views/hr/${e.path}/index.vue`);
});

console.log(`\nTotal: ${config.entities.length} entities, ${config.entities.length * 2} files generated.`);












const { proxy } = getCurrentInstance()
const { pms_invoice_status, pms_invoice_type, wms_payment_method } = proxy.useDict('pms_invoice_status', 'pms_invoice_type', 'wms_payment_method')
const baseUrl = ''

const { collapsedCards, toggleCard } = useDetailCard(["recognize","c1","c2","c3","c0","c4","v1","v3","v4","v5","v6","a1","a2","p1","m1","m2","m3","m4"])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pms_invoice_index')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const matchOpen = ref(false)
const matchData = ref({})
const auditOpen = ref(false)
const auditData = ref({})
const auditForm = ref({ invoiceId: undefined, auditOpinion: undefined })
const auditRules = { auditOpinion: [{ required: true, message: '请输入审批意见', trigger: 'blur' }] }
const payOpen = ref(false)
const payData = ref({})
const payForm = ref({ invoiceId: undefined, paymentAmount: undefined, paymentDate: undefined, paymentMethod: '0', bankName: undefined, bankAccount: undefined, payer: undefined, attachment: undefined, remark: undefined })
const payRules = {
  paymentAmount: [{ required: true, message: '请输入付款金额', trigger: 'blur' }],
  paymentDate: [{ required: true, message: '请选择付款日期', trigger: 'change' }],
  paymentMethod: [{ required: true, message: '请选择付款方式', trigger: 'change' }]
}
const payUnpaid = computed(() => {
  const total = Number(payData.value.totalAmount) || 0
  const paid = Number(payData.value.paymentAmount) || 0
  return Math.max(0, parseFloat((total - paid).toFixed(2)))
})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const supplierPickerRef = ref(null)
const contractPickerRef = ref(null)
const orderPickerRef = ref(null)
const ocrLoading = ref(false)
const recognizing = ref(false)
const recognizedFields = ref([])
const fileInputRef = ref(null)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const showStatusHelp = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '5': 0, '6': 0 })

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  invoiceNo: { label: '结算单号', visible: true },
  contractNo: { label: '合同编号', visible: true },
  orderNo: { label: '采购单号', visible: true },
  supplierName: { label: '供应商', visible: true },
  status: { label: '状态', visible: true },
  invoiceType: { label: '发票类型', visible: true },
  invoiceNumber: { label: '发票号码', visible: true },
  invoiceDate: { label: '开票日期', visible: true },
  invoiceAmount: { label: '不含税金额', visible: true },
  taxAmount: { label: '税额', visible: true },
  totalAmount: { label: '开票金额', visible: true },
  paymentAmount: { label: '已付金额', visible: true },
  paymentDate: { label: '付款日期', visible: true },
  createTime: { label: '创建时间', visible: true }
}

// 从 localStorage 读取保存的列显隐配置
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pms_invoice_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, invoiceNo: undefined, supplierName: undefined, status: undefined, invoiceType: undefined, invoiceNumber: undefined, params: {} },
  rules: {}
})
const { queryParams, form, rules } = toRefs(data)

const statusTabList = computed(() => pms_invoice_status.value)
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.invoiceNo) count++
  if (queryParams.value.supplierName) count++
  if (queryParams.value.status) count++
  if (queryParams.value.invoiceType) count++
  if (queryParams.value.invoiceNumber) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() { loading.value = true; listInvoice(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.invoiceNo = undefined; queryParams.value.supplierName = undefined; queryParams.value.status = undefined; queryParams.value.invoiceType = undefined; queryParams.value.invoiceNumber = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery()
}
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.invoiceId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { invoiceId: undefined, invoiceNo: undefined, contractId: undefined, contractNo: undefined, orderId: undefined, orderNo: undefined, supplierId: undefined, supplierName: undefined, status: '0', invoiceType: '0', invoiceNumber: undefined, invoiceDate: undefined, invoiceTitle: undefined, taxNumber: undefined, invoiceImageUrl: undefined, invoiceAttachment: undefined, taxRate: 0, taxAmount: 0, totalAmount: 0, paymentAmount: 0, payAmount: 0, paymentDate: undefined, paymentMethod: '0', bankName: undefined, bankAccount: undefined, remark: undefined }; proxy.resetForm('invoiceRef'); recognizedFields.value = [] }
function openSupplierPicker() { supplierPickerRef.value.open(form.value.supplierId) }
function onSupplierPickerConfirm(supplier) { form.value.supplierId = supplier.supplierId; form.value.supplierName = supplier.supplierName }
function clearSupplier() { form.value.supplierId = undefined; form.value.supplierName = undefined }
/** 打开合同选择弹窗 */
function openContractPicker() {
  contractPickerRef.value.open(form.value.contractId)
}

/** 合同选择确认回调 — 带出合同编号和供应商 */
function onContractPickerConfirm(contract) {
  form.value.contractId = contract.contractId
  form.value.contractNo = contract.contractNo
  form.value.supplierId = contract.supplierId
  form.value.supplierName = contract.supplierName
}

/** 清除合同 */
function clearContract() {
  form.value.contractId = undefined
  form.value.contractNo = undefined
}

/** 打开采购订单选择弹窗 */
function openOrderPicker() {
  orderPickerRef.value.open(form.value.orderId)
}

/** 采购订单选择确认回调 — 带出订单号、供应商并反填关联合同 */
function onOrderPickerConfirm(order) {
  form.value.orderId = order.orderId
  form.value.orderNo = order.orderNo
  applyOrderDetail(order.orderId)
}

/** 清除采购订单 */
function clearOrder() {
  form.value.orderId = undefined
  form.value.orderNo = undefined
}

/** 根据采购订单带出供应商并反填关联合同 */
function applyOrderDetail(orderId) {
  if (!orderId) return
  getOrder(orderId).then(res => {
    const order = res.data
    form.value.orderNo = order.orderNo
    form.value.supplierId = order.supplierId
    form.value.supplierName = order.supplierName
    if (order.contractId) { form.value.contractId = order.contractId; form.value.contractNo = order.contractNo }
  })
}
function handleAdd() { reset(); open.value = true; title.value = '添加发票结算' }
function handleUpdate(row) { reset(); getInvoice(row.invoiceId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改发票结算' }) }
function handleView(row) { getInvoice(row.invoiceId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function calcTax() { const total = form.value.totalAmount || 0; const rate = form.value.taxRate || 0; form.value.taxAmount = parseFloat((total * rate / 100).toFixed(2)); form.value.invoiceAmount = parseFloat((total - form.value.taxAmount).toFixed(2)); form.value.payAmount = total }
function submitForm() { proxy.$refs['invoiceRef'].validate(valid => { if (valid) { if (form.value.invoiceId != undefined) { updateInvoice(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addInvoice(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const invoiceIds = row.invoiceId || ids.value; proxy.$modal.confirm('确认删除编号为"' + invoiceIds + '"的数据？').then(() => delInvoice(invoiceIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('pms/invoice/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `invoice_${new Date().getTime()}.xlsx`) }
function handleAudit(row) { getInvoice(row.invoiceId).then(res => { auditData.value = res.data; auditForm.value = { invoiceId: row.invoiceId, auditOpinion: '' }; auditOpen.value = true }) }
function submitAudit(passed) { proxy.$refs['auditRef'].validate(valid => { if (valid) { const status = passed ? '2' : '5'; const actionText = passed ? '通过' : '驳回'; proxy.$modal.confirm(`确认${actionText}该发票结算？`).then(() => { return auditInvoice(auditForm.value.invoiceId, status, auditForm.value.auditOpinion) }).then(() => { proxy.$modal.msgSuccess(`${actionText}成功`); auditOpen.value = false; getList() }).catch(() => {}) } }) }
function handleSubmit(row) { proxy.$modal.confirm('确认提交该发票结算审核？').then(() => submitInvoice(row.invoiceId)).then(() => { getList(); proxy.$modal.msgSuccess('提交成功') }).catch(() => {}) }
function handlePay(row) {
  getInvoice(row.invoiceId).then(res => {
    payData.value = res.data
    const total = Number(res.data.totalAmount) || 0
    const paid = Number(res.data.paymentAmount) || 0
    const unpaid = parseFloat((total - paid).toFixed(2))
    payForm.value = {
      invoiceId: row.invoiceId,
      paymentAmount: unpaid > 0 ? unpaid : 0,
      paymentDate: new Date().toISOString().slice(0, 10),
      paymentMethod: res.data.paymentMethod || '0',
      bankName: res.data.bankName || undefined,
      bankAccount: res.data.bankAccount || undefined,
      payer: undefined,
      attachment: undefined,
      remark: undefined
    }
    payOpen.value = true
  })
}
function submitPay() {
  proxy.$refs['payRef'].validate(valid => {
    if (!valid) return
    if (payForm.value.paymentAmount > payUnpaid.value) {
      proxy.$modal.msgError('付款金额不能超过未付金额')
      return
    }
    proxy.$modal.confirm(`确认本次付款 ${formatMoney(payForm.value.paymentAmount)} 元？`).then(() => {
      return payInvoice(payForm.value)
    }).then(() => {
      proxy.$modal.msgSuccess('付款成功')
      payOpen.value = false
      getList()
    }).catch(() => {})
  })
}
/** 三方匹配 */
function handleMatch(row) {
  threeWayMatch(row.invoiceId).then(res => {
    matchData.value = res.data
    matchOpen.value = true
  })
}
/** PDF发票识别 */
function handleBeforeRecognize(file) {
  const fileName = file.name.split('.')
  const fileExt = fileName[fileName.length - 1].toLowerCase()
  if (fileExt !== 'pdf') { proxy.$modal.msgError('仅支持PDF格式发票文件!'); return false }
  if (file.size / 1024 / 1024 > 20) { proxy.$modal.msgError('上传文件大小不能超过 20 MB!'); return false }
  return true
}
function handleFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (!handleBeforeRecognize(file)) { e.target.value = ''; return }
  handleRecognizeUpload(file)
  e.target.value = ''
}
function handleRecognizeUpload(file) {
  const formData = new FormData()
  formData.append('file', file)
  recognizing.value = true
  recognizedFields.value = []
  recognizeInvoice(formData).then(res => {
    const data = res.data
    if (data) {
      if (data.invoiceNumber) form.value.invoiceNumber = data.invoiceNumber
      if (data.invoiceType) form.value.invoiceType = data.invoiceType
      if (data.invoiceDate) form.value.invoiceDate = data.invoiceDate
      if (data.invoiceAmount != null) form.value.invoiceAmount = Number(data.invoiceAmount)
      if (data.taxAmount != null) form.value.taxAmount = Number(data.taxAmount)
      if (data.totalAmount != null) form.value.totalAmount = Number(data.totalAmount)
      if (data.taxRate != null) form.value.taxRate = Number(data.taxRate)
      if (data.invoiceTitle) form.value.invoiceTitle = data.invoiceTitle
      if (data.taxNumber) form.value.taxNumber = data.taxNumber
      if (data.totalAmount != null && data.taxRate != null) { calcTax() }
      uploadAttachment(file)
      const fieldLabels = { invoiceNumber: '发票号码', invoiceType: '发票类型', invoiceDate: '开票日期', taxAmount: '税额', taxRate: '税率(%)', totalAmount: '开票金额', invoiceTitle: '发票抬头', taxNumber: '纳税人识别号' }
      recognizedFields.value = Object.keys(fieldLabels).filter(k => data[k] != null && data[k] !== '').map(k => ({ label: fieldLabels[k], value: data[k] }))
      if (recognizedFields.value.length > 0) { proxy.$modal.msgSuccess('识别成功，已回填' + recognizedFields.value.length + '项信息') } else { proxy.$modal.msgWarning('未识别到发票信息，请检查PDF文件是否为电子发票') }
    } else { proxy.$modal.msgWarning('未识别到发票信息') }
  }).catch(() => {}).finally(() => { recognizing.value = false })
}
function uploadAttachment(file) {
  const formData = new FormData()
  formData.append('file', file)
  request({ url: '/common/upload', method: 'post', data: formData, timeout: 60000, headers: { 'repeatSubmit': false, 'Content-Type': false } }).then(res => { if (res.fileName) { form.value.invoiceAttachment = res.fileName } }).catch(() => {})
}
function getFileName(name) { if (name && name.lastIndexOf('/') > -1) { return name.slice(name.lastIndexOf('/') + 1) } return name || '' }
function handlePreview(fileUrl) { proxy.$refs.filePreviewRef.open(fileUrl, getFileName(fileUrl)) }
function cancel() { open.value = false; reset() }
function loadStatusCounts() {
  listInvoice({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0, '3': 0, '5': 0, '6': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === 'all' ? undefined : status; handleQuery() }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'green', '5': 'red', '6': 'violet' }; return map[status] || 'gray' }
function statusLabel(status) { const item = pms_invoice_status.value.find(d => d.value == status); return item ? item.label : '-' }
function invoiceTypeLabel(type) { const item = pms_invoice_type.value.find(d => d.value == type); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-approved', '3': 'tab-done', '5': 'tab-reject', '6': 'tab-partial' }; return map[value] || '' }

getList()
null(() => { getList() })

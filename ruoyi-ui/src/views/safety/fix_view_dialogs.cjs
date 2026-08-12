const fs = require('fs');
const path = require('path');

const baseDir = path.resolve(__dirname);

// Helper: generate collapse button HTML
const cbtn = (k) => `<button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.${k} }" aria-label="\u6298\u53e0"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>`;

// Helper: generate rd-item HTML
const item = (label, prop, opts = {}) => {
  const full = opts.full ? ' rd-item--full' : '';
  if (opts.dict) {
    return `              <div class="rd-item${full}"><span class="rd-label">${label}</span><div class="rd-value"><dict-tag :options="${opts.dict}" :value="viewData.${prop}" /></div></div>`;
  }
  if (opts.badge) {
    return `              <div class="rd-item${full}"><span class="rd-label">${label}</span><div class="rd-value"><span class="badge" :class="${opts.badge}"><span class="dot"></span>{{ ${opts.badgeLabel} }}</span></div></div>`;
  }
  if (opts.file) {
    return `              <div class="rd-item rd-item--full" v-if="viewData.${prop}"><span class="rd-label">${label}</span><div class="rd-value"><file-upload v-model="viewData.${prop}" :disabled="true" /></div></div>`;
  }
  const valExpr = opts.unit ? `{{ viewData.${prop} != null ? viewData.${prop} + ' ${opts.unit}' : '-' }}` : `{{ viewData.${prop} || '-' }}`;
  return `              <div class="rd-item${full}"><span class="rd-label">${label}</span><div class="rd-value">${valExpr}</div></div>`;
};

// Page configurations
const pages = [
  // 1. Training - Course
  { file: 'training/course/index.vue', title: '课程详情', code: 'courseCode',
    cards: [
      { t: '课程信息', fields: [
        item('课程编号','courseCode'), item('课程名称','courseName'),
        item('课程类别','courseType',{dict:'safety_course_type'}), item('课程形式','courseForm',{dict:'safety_course_form'}),
        item('标准课时','hours',{unit:'学时'}), item('讲师','instructor'),
      ]},
      { t: '课件与描述', vif: 'viewData.materialUrl || viewData.description || viewData.remark', fields: [
        item('课件附件','materialUrl',{file:true}),
        item('课程描述','description',{full:true}), item('备注','remark',{full:true}),
      ]},
    ]},
  // 2. Training - Cert
  { file: 'training/cert/index.vue', title: '证书详情', code: 'certNo',
    cards: [
      { t: '证书信息', fields: [
        item('证书编号','certNo'), item('证书名称','certName'),
        item('证书类型','certType',{dict:'safety_cert_type'}), item('发证机构','issueOrg'),
      ]},
      { t: '获证人员', vif: 'viewData.userName || viewData.deptName', fields: [
        item('获证人员','userName'), item('所属部门','deptName'),
      ]},
      { t: '有效期与附件', vif: 'viewData.issueDate || viewData.expireDate || viewData.attachment || viewData.remark', fields: [
        item('发证日期','issueDate'), item('有效期至','expireDate'),
        item('附件','attachment',{file:true}), item('备注','remark',{full:true}),
      ]},
    ]},
  // 3. Training - Attendee
  { file: 'training/attendee/index.vue', title: '参训人员详情', code: null,
    cards: [
      { t: '人员信息', fields: [
        item('参训人员','userName'), item('所属部门','deptName'),
        item('关联课程','courseName',{full:true}), item('培训日期','trainingDate'),
      ]},
      { t: '考试成绩', vif: 'viewData.examScore != null || viewData.isPass || viewData.examTime || viewData.remark', fields: [
        item('考试分数','examScore',{unit:'分'}), item('是否合格','isPass'),
        item('考试时间','examTime'), item('备注','remark',{full:true}),
      ]},
    ]},
  // 4. Training - Plan
  { file: 'training/plan/index.vue', title: '培训计划详情', code: 'planCode',
    cards: [
      { t: '计划信息', fields: [
        item('计划编号','planCode'), item('计划名称','planName'),
        item('计划类型','planType',{dict:'safety_training_plan_type'}), item('年度','planYear'),
      ]},
      { t: '培训详情', vif: 'viewData.targetPersons || viewData.totalHours || viewData.planStatus', fields: [
        item('培训对象','targetPersons',{full:true}),
        item('总课时','totalHours',{unit:'学时'}), item('计划状态','planStatus'),
      ]},
      { t: '其他信息', vif: 'viewData.remark', fields: [
        item('备注','remark',{full:true}),
      ]},
    ]},
  // 5. Training - Record
  { file: 'training/record/index.vue', title: '培训记录详情', code: null,
    cards: [
      { t: '课程信息', fields: [
        item('关联计划','planName',{full:true}), item('课程名称','courseName'),
        item('课程类别','courseType',{dict:'safety_course_type'}), item('培训日期','trainingDate'),
        item('课时','hours',{unit:'学时'}), item('培训地点','trainingLocation'),
      ]},
      { t: '培训讲师', vif: 'viewData.trainer', fields: [
        item('培训讲师','trainer',{full:true}),
      ]},
      { t: '参训人员', vif: 'viewData.attendeeNames', fields: [
        item('参训人员','attendeeNames',{full:true}),
      ]},
      { t: '附件与备注', vif: 'viewData.attachment || viewData.remark', fields: [
        item('附件','attachment',{file:true}), item('备注','remark',{full:true}),
      ]},
    ]},
  // 6. Emergency - Plan
  { file: 'emergency/plan/index.vue', title: '应急预案详情', code: 'planCode',
    cards: [
      { t: '预案信息', fields: [
        item('预案编号','planCode'), item('预案名称','planName'),
        item('预案类型','planType',{dict:'safety_plan_type'}), item('预案级别','planLevel',{dict:'safety_plan_level'}),
      ]},
      { t: '备案与版本', vif: 'viewData.version || viewData.regNo || viewData.regDate || viewData.effectiveDate || viewData.expireDate', fields: [
        item('版本号','version'), item('备案号','regNo'), item('备案日期','regDate'),
        item('生效日期','effectiveDate'), item('修订到期','expireDate'),
      ]},
      { t: '内容与附件', vif: 'viewData.content || viewData.attachment || viewData.remark', fields: [
        item('预案内容','content',{full:true}), item('附件','attachment',{file:true}),
        item('备注','remark',{full:true}),
      ]},
    ]},
  // 7. Emergency - Material
  { file: 'emergency/material/index.vue', title: '应急物资详情', code: 'materialCode',
    cards: [
      { t: '物资信息', fields: [
        item('物资编码','materialCode'), item('物资名称','materialName'),
        item('物资类别','materialType',{dict:'safety_material_type'}), item('规格型号','specModel'),
      ]},
      { t: '储存信息', vif: 'viewData.quantity != null || viewData.unit || viewData.effectiveDate || viewData.storageLocation || viewData.personName', fields: [
        item('数量','quantity'), item('单位','unit',{dict:'wms_unit'}),
        item('有效期至','effectiveDate'), item('存放位置','storageLocation',{full:true}),
        item('管理人','personName'),
      ]},
      { t: '其他信息', vif: 'viewData.remark', fields: [
        item('备注','remark',{full:true}),
      ]},
    ]},
  // 8. Emergency - Incident
  { file: 'emergency/incident/index.vue', title: '事故详情', code: 'incidentCode',
    cards: [
      { t: '事故信息', fields: [
        item('事故编号','incidentCode'), item('事故等级','incidentLevel',{dict:'safety_incident_level'}),
        item('发生时间','incidentDate'), item('事故地点','incidentLocation',{full:true}),
        item('事故描述','incidentDesc',{full:true}),
      ]},
      { t: '伤亡与损失', vif: 'viewData.casualties != null || viewData.economicLoss != null', fields: [
        item('伤亡人数','casualties',{unit:'人'}), item('经济损失','economicLoss'),
      ]},
      { t: '原因与整改', vif: 'viewData.causeAnalysis || viewData.rectifyMeasures', fields: [
        item('原因分析','causeAnalysis',{full:true}), item('整改措施','rectifyMeasures',{full:true}),
      ]},
      { t: '其他信息', vif: 'viewData.reportPersonName || viewData.reportTime || viewData.remark', fields: [
        item('报告人','reportPersonName'), item('报告时间','reportTime'),
        item('备注','remark',{full:true}),
      ]},
    ]},
  // 9. Emergency - Drill
  { file: 'emergency/drill/index.vue', title: '应急演练详情', code: 'drillCode',
    cards: [
      { t: '基本信息', fields: [
        item('演练编号','drillCode'), item('演练名称','drillName'),
        item('演练类型','drillType',{dict:'safety_drill_type'}), item('演练日期','drillDate'),
        item('演练地点','drillLocation'), item('演练指挥','drillCommander'),
        item('演练目的','drillObjective',{full:true}),
        item('参与人数','participantCount',{unit:'人'}), item('参与人员','participants',{full:true}),
      ]},
      { t: '演练详情', vif: 'viewData.drillDesc || viewData.evalResult || viewData.problems || viewData.improvements', fields: [
        item('过程描述','drillDesc',{full:true}), item('演练评估','evalResult',{full:true}),
        item('发现问题','problems',{full:true}), item('改进措施','improvements',{full:true}),
      ]},
      { t: '附件与备注', vif: 'viewData.attachments || viewData.remark', fields: [
        item('附件','attachments',{file:true}), item('备注','remark',{full:true}),
      ]},
    ]},
];

pages.forEach(({file, title: detailTitle, code, cards}) => {
  const fullPath = path.join(baseDir, file);
  let content = fs.readFileSync(fullPath, 'utf8');
  let changed = false;

  // Step 1: Remove :disabled="formDisabled" from el-form
  content = content.replace(/ :disabled="formDisabled"/g, '');
  changed = true;

  // Step 2: Handle v-if="!formDisabled" / v-else patterns on input-number and spans
  // Pattern: <el-input-number v-if="!formDisabled" ... /><span v-else ...>...</span>
  // Just keep the el-input-number, remove the span
  content = content.replace(/<el-input-number v-if="!formDisabled"([^>]*)\/><span v-else[^>]*>.*?<\/span>/g, (match, attrs) => {
    return `<el-input-number${attrs}/>`;
  });
  // Pattern: <span v-if="formDisabled" ...>...</span><el-input-number v-else ... />
  content = content.replace(/<span v-if="formDisabled"[^>]*>.*?<\/span><el-input-number v-else([^>]*)\/>/g, (match, attrs) => {
    return `<el-input-number${attrs}/>`;
  });

  // Step 3: Fix footer - remove v-if conditions
  content = content.replace(
    /<template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm" v-if="!formDisabled">确 定<\/el-button><el-button @click="cancel" v-if="!formDisabled">取 消<\/el-button><el-button @click="open = false" v-if="formDisabled">关 闭<\/el-button><\/div><\/template>/g,
    '<template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>'
  );

  // Step 4: Add viewOpen and viewData refs, remove formDisabled
  // Find: const formDisabled = ref(false)
  content = content.replace(/const formDisabled = ref\(false\)\n/, 'const viewOpen = ref(false)\nconst viewData = ref({})\n');

  // Step 5: Modify handleView
  // Pattern: function handleView(row) { ... formDisabled.value = true; ... open.value = true; title.value = '查看XX' }
  content = content.replace(
    /function handleView\(row\) \{[^}]*?formDisabled\.value = true;[^}]*?open\.value = true;[^}]*?title\.value = '查看[^']*'[^}]*\}/g,
    (match) => {
      // Extract the API call and ID field
      const apiMatch = match.match(/get(\w+)\(row\.(\w+) \|\| ids\.value\[0\]\)/);
      if (apiMatch) {
        const apiFn = apiMatch[1];
        const idField = apiMatch[2];
        return `function handleView(row) { const id = row.${idField} || ids.value[0]; get${apiFn}(id).then(response => { viewData.value = response.data; viewOpen.value = true }) }`;
      }
      return match;
    }
  );

  // Step 6: Modify handleAdd - remove formDisabled.value = false;
  content = content.replace(/function handleAdd\(\) \{ reset\(\); formDisabled\.value = false; /g, 'function handleAdd() { reset(); ');

  // Step 7: Modify handleUpdate - remove formDisabled.value = false;
  content = content.replace(/function handleUpdate\(row\) \{ reset\(\); formDisabled\.value = false; /g, 'function handleUpdate(row) { reset(); ');

  // Step 8: Generate view dialog HTML
  const vcKeys = cards.map((_, i) => 'vc' + i);
  let viewDialog = `\n    <!-- ===== \u67e5\u770b\u8be6\u60c5\u5f39\u7a97 ===== -->\n    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">\n      <template #header>\n        <div class="rd-detail-header">\n          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg></div>\n          <span class="rd-detail-header-title">${detailTitle}</span>`;
  if (code) {
    viewDialog += `\n          <div class="rd-detail-header-sub" v-if="viewData.${code}"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">\u7f16\u53f7\uff1a{{ viewData.${code} }}</span></div>`;
  }
  viewDialog += `\n        </div>\n      </template>\n      <div class="rd-page">`;

  cards.forEach((card, idx) => {
    const vcKey = 'vc' + idx;
    const vif = card.vif ? ` v-if="${card.vif}"` : '';
    viewDialog += `\n        <section class="rd-card"${vif}>\n          <div class="rd-card-header" @click="toggleCard('${vcKey}')"><div class="rd-card-title">${card.t}</div>${cbtn(vcKey)}</div>\n          <div class="rd-card-body" v-show="!collapsedCards.${vcKey}" style="display:block">\n            <div class="rd-grid">\n${card.fields.join('\n')}\n            </div>\n          </div>\n        </section>`;
  });

  viewDialog += `\n      </div>\n      <template #footer><el-button @click="viewOpen = false">\u5173 \u95ed</el-button></template>\n    </el-dialog>\n`;

  // Find insertion point: after the edit dialog closing </el-dialog>
  // Look for the first </el-dialog> after v-model="open"
  const openIdx = content.indexOf('v-model="open"');
  if (openIdx !== -1) {
    const closeIdx = content.indexOf('</el-dialog>', openIdx);
    if (closeIdx !== -1) {
      const insertPos = closeIdx + '</el-dialog>'.length;
      // Check if view dialog already exists
      if (content.indexOf('v-model="viewOpen"') === -1) {
        content = content.substring(0, insertPos) + '\n' + viewDialog + content.substring(insertPos);
      }
    }
  }

  // Step 9: Update useDetailCard call
  const udMatch = content.match(/useDetailCard\(\[([^\]]+)\]\)/);
  if (udMatch && !udMatch[1].includes('vc0')) {
    const existing = udMatch[1];
    const newKeys = existing + ',' + vcKeys.map(k => '"' + k + '"').join(',');
    content = content.replace(/useDetailCard\(\[([^\]]+)\]\)/, 'useDetailCard([' + newKeys + '])');
  }

  fs.writeFileSync(fullPath, content, 'utf8');
  console.log('DONE: ' + file);
});

console.log('All 9 files processed.');

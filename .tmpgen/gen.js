const fs = require("fs");
const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
        AlignmentType, LevelFormat, HeadingLevel, BorderStyle, WidthType,
        ShadingType, PageNumber, Header, Footer, PageBreak } = require("docx");

const F = { ascii: "Arial", hAnsi: "Arial", eastAsia: "Microsoft YaHei" };
const CW = 9026;

const border = { style: BorderStyle.SINGLE, size: 1, color: "CCCCCC" };
const borders = { top: border, bottom: border, left: border, right: border };

function p(text) {
  return new Paragraph({ spacing: { after: 120, line: 300 },
    children: [new TextRun({ text, size: 21 })] });
}
function h1(text) {
  return new Paragraph({ heading: HeadingLevel.HEADING_1, spacing: { before: 280, after: 160 }, children: [new TextRun(text)] });
}
function h2(text) {
  return new Paragraph({ heading: HeadingLevel.HEADING_2, spacing: { before: 200, after: 120 }, children: [new TextRun(text)] });
}
function bullet(text) {
  return new Paragraph({ numbering: { reference: "bullets", level: 0 }, spacing: { after: 60, line: 300 },
    children: [new TextRun({ text, size: 21 })] });
}
function numbered(text) {
  return new Paragraph({ numbering: { reference: "nums", level: 0 }, spacing: { after: 60, line: 300 },
    children: [new TextRun({ text, size: 21 })] });
}
function cell(text, w, head) {
  return new TableCell({
    borders, width: { size: w, type: WidthType.DXA },
    shading: head ? { fill: "1F4E79", type: ShadingType.CLEAR } : undefined,
    margins: { top: 60, bottom: 60, left: 100, right: 100 },
    children: [new Paragraph({ spacing: { after: 0 },
      children: [new TextRun({ text, size: 20, bold: !!head, color: head ? "FFFFFF" : undefined })] })],
  });
}
function table(widths, rows) {
  return new Table({
    width: { size: CW, type: WidthType.DXA },
    columnWidths: widths,
    rows: rows.map((r, i) => new TableRow({
      cantSplit: true,
      children: r.map((t, j) => cell(t, widths[j], i === 0)),
    })),
  });
}

const children = [];

children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 2400, after: 300 },
  children: [new TextRun({ text: "曲靖传丰食品数字化平台", size: 52, bold: true })] }));
children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 600 },
  children: [new TextRun({ text: "模块开关功能实施方案", size: 72, bold: true })] }));
children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 120 },
  children: [new TextRun({ text: "版  本：V1.1（待评审，含完整性分析补强 4 项）", size: 24 })] }));
children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 120 },
  children: [new TextRun({ text: "编制日期：2026年10月13日", size: 24 })] }));
children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 120 },
  children: [new TextRun({ text: "适用环境：传丰开发环境（D:\\EDOP_QY\\EDOP_CFSP）及标准环境（E:\\AIcode\\EDOP\\RuoYi）", size: 21 })] }));
children.push(new Paragraph({ children: [new PageBreak()] }));

children.push(h1("一、背景与现状问题"));
children.push(p("传丰食品数字化平台按实施方案建设，涵盖生产管控、质量管理、采购管理、仓储管理、营销管理等业务模块。项目一期仅启用部分模块，其余模块（设备管理、安全生产、人力资源、财务管理、招投标管理等）需要临时关闭。"));
children.push(p("目前采用的关闭方式是手工分散操作：停用 sys_menu 菜单树、停用 sys_role 角色、暂停 sys_job 定时任务。该方式存在以下问题："));
children.push(bullet("角色残留：角色管理页仍显示已关闭模块的角色，界面杂乱，且新建用户分配角色时可能误选范围外角色；"));
children.push(bullet("操作分散易漏：一次模块开关涉及菜单、角色、定时任务、Redis 缓存等多处数据，纯手工操作容易遗漏、难以恢复；"));
children.push(bullet("无审计记录：谁在何时关闭/开启了哪个模块，没有操作留痕；"));
children.push(bullet("缓存滞后：若依框架在登录时将权限快照写入 Redis（login_tokens），直接改库后在线用户权限不刷新，必须重新登录才生效；"));
children.push(bullet("首页联动靠补丁：HomeController 已按菜单状态过滤模块概览，但属于点状修补，未形成统一机制。"));

children.push(h1("二、已确认的设计决策"));
children.push(table([700, 2800, 5526], [
  ["序号", "决策项", "结论"],
  ["1", "已停用模块的角色如何展示", "角色管理默认隐藏（列表查询过滤），支持勾选查看全部"],
  ["2", "开关切换时是否强制下线", "是。切换后清除全部登录缓存，所有在线用户重新登录生效"],
  ["3", "流程中心归属", "纳入业务模块开关管理（当前为停用状态）"],
  ["4", "AI中心归属", "纳入开关管理，默认开启"],
  ["5", "方案完整性分析补强", "V1.1 合入 4 项补强：停用标记精确恢复、缓存清除时序、并发切换防护、分环境种子数据（见 5.2 / 九）"],
]));

children.push(h1("三、总体设计"));
children.push(p("新增一张模块注册表 sys_module 作为模块开关的唯一控制点。每个业务模块注册一条记录，记录其名称、标识、根菜单 ID 和状态。管理页面提供开关切换，切换时在一个事务内级联处理所有关联数据，事务提交后清除登录缓存。"));
children.push(p("级联联动对象如下："));
children.push(table([2200, 2400, 4426], [
  ["联动对象", "数据位置", "关闭模块时的动作"],
  ["功能菜单", "sys_menu（根菜单整棵子树）", "status 置为 1（停用），路由、菜单、按钮权限全部消失"],
  ["角色", "sys_role（按 module_key 归属）", "status 置为 1，remark 追加停用标记，用于启用时精确恢复"],
  ["定时任务", "sys_job（按 module_key 归属）", "status 置为 1（暂停），remark 同样追加标记"],
  ["登录缓存", "Redis login_tokens:*", "事务提交后全部清除，强制在线用户重新登录"],
  ["首页概览", "HomeController /home/dashboard", "自动联动（按菜单状态过滤，现有逻辑保留为第二道防线）"],
  ["操作审计", "sys_oper_log", "通过标准 @Log 注解记录开关操作"],
]));
children.push(p("安全性说明：模块关闭后并非仅“看不见”——菜单与角色同时停用后，业务接口的 @PreAuthorize(hasPermi) 校验因权限点清空而自动返回 403，定时任务暂停，实现功能级关闭。"));

children.push(h1("四、数据库设计"));
children.push(h2("4.1 新增表 sys_module（模块注册表）"));
children.push(table([1800, 1600, 5626], [
  ["字段", "类型", "说明"],
  ["module_id", "bigint PK", "主键，自增"],
  ["module_name", "varchar(50)", "模块名称，如：生产管控"],
  ["module_key", "varchar(50)", "模块标识，如：mms；用于角色/任务归属匹配"],
  ["root_menu_id", "bigint", "模块根菜单 ID，级联停用/启用菜单子树的定位依据"],
  ["status", "char(1)", "0 启用 / 1 停用"],
  ["is_core", "char(1)", "1=平台基础模块，不允许关闭（如系统管理、消息中心）"],
  ["order_num", "int", "显示顺序"],
  ["通用字段", "-", "remark、create_by、create_time、update_by、update_time"],
]));
children.push(h2("4.2 存量表扩展"));
children.push(bullet("sys_role 新增 module_key varchar(50)：角色所属模块标识，NULL 表示平台级角色，不受开关影响；"));
children.push(bullet("sys_job 新增 module_key varchar(50)：定时任务所属模块，NULL 表示系统级任务；"));
children.push(bullet("两个新列均需一次性数据回填（见 4.4、4.5），回填后与当前实际状态零差异。"));
children.push(h2("4.3 模块种子数据（初始状态对齐传丰现状）"));
children.push(table([1500, 1900, 1400, 1300, 2926], [
  ["module_key", "模块名称", "根菜单ID", "初始状态", "类别"],
  ["mms", "生产管控", "5000", "启用", "业务模块"],
  ["qms", "质量管理", "4727", "启用", "业务模块"],
  ["pms", "采购管理", "4197", "启用", "业务模块"],
  ["wms", "仓储管理", "2000", "启用", "业务模块"],
  ["mk", "营销管理", "4000", "启用", "业务模块"],
  ["ai", "AI中心", "905788", "启用", "业务模块"],
  ["flow", "流程中心", "905824", "停用", "业务模块"],
  ["dms", "设备管理", "3000", "停用", "业务模块"],
  ["safety", "安全生产管理", "4638", "停用", "业务模块"],
  ["hr", "人力资源管理", "905538", "停用", "业务模块"],
  ["fin", "财务管理", "905938", "停用", "业务模块"],
  ["bid", "招投标管理", "905886", "停用", "业务模块"],
  ["tool", "系统工具", "3", "停用", "业务模块"],
]));
children.push(p("平台基础模块（is_core=1，不纳入开关、不可关闭）：系统管理（菜单ID=1）、消息中心（菜单ID=905404）。"));
children.push(h2("4.4 角色归属回填映射"));
children.push(table([1600, 4500, 2926], [
  ["module_key", "角色名称", "角色ID"],
  ["mms", "生产总监、计划员、车间主任、操作工", "600、601、602、603"],
  ["qms", "质量检验员、质量工程师、质量主管、质量总监、供应商质量工程师、质量体系专员、质量数据分析员", "120～126"],
  ["mk", "营销总监、销售经理、销售代表、财务专员、市场专员", "300～304"],
  ["wms", "仓库主管、入库管理员、出库管理员、盘点员、库存查询员", "400～404"],
  ["pms", "采购总监、采购经理、采购员、收货专员、供应商管理员、采购财务", "500～505"],
  ["dms", "设备管理员、维修工程师、车间操作员、车间主管", "200～203"],
  ["safety", "安全员、安全主管", "110、111"],
  ["fin", "财务人员、财务经理、会计、出纳", "101、610、611、612"],
  ["NULL（平台）", "超级管理员、普通角色、普通员工、部门负责人、业务经理、总经理", "1、2、100、102、103、104"],
]));
children.push(p("流程中心、AI中心、人力资源、招投标暂无专属角色，开关仅作用于菜单（后续新增模块角色时在角色管理中归属到对应模块即可）。"));
children.push(h2("4.5 定时任务归属回填"));
children.push(bullet("dms：PM工单自动生成、SLA超时扫描、保养点检任务生成（2个）、设备数据采集模拟，共 5 个任务（任务ID 100～104，当前已暂停）；"));
children.push(bullet("其余系统任务 module_key 为 NULL，不受开关影响。"));

children.push(h1("五、功能设计"));
children.push(h2("5.1 模块开关管理页面"));
children.push(bullet("菜单位置：系统管理 → 模块开关（新增菜单），权限标识 system:module:list / system:module:edit，仅超级管理员可见；"));
children.push(bullet("页面形式：卡片式列表，每张卡片展示模块名称、状态开关、包含菜单数、角色数、定时任务数；平台基础模块卡片置灰并标注“基础模块不可关闭”；"));
children.push(bullet("切换交互：点击开关弹出确认框，明确提示影响范围（菜单隐藏、角色停用、任务暂停、所有在线用户将被强制下线——包括当前操作者本人，需重新登录），确认后执行；"));
children.push(bullet("提供“一键恢复全部”按钮，可将所有已关闭业务模块一次性恢复（平台基础模块不受影响）。"));
children.push(h2("5.2 开关级联动作（含 V1.1 补强）"));
children.push(p("关闭模块（启用→停用），事务内依次执行："));
children.push(numbered("并发防护：以条件更新 UPDATE sys_module SET status='1' WHERE module_id=? AND status='0'，影响行数为 0 时提示“状态已被他人变更，请刷新后重试”；"));
children.push(numbered("sys_menu：以 root_menu_id 为根的整棵子树 status=1（含目录/菜单/按钮三类）；"));
children.push(numbered("sys_role：module_key 匹配且当前为启用状态的角色批量 status=1，并在 remark 追加标记 [模块停用:module_key]；"));
children.push(numbered("sys_job：module_key 匹配的任务批量 status=1，remark 同样追加标记；"));
children.push(numbered("提交事务；"));
children.push(numbered("事务提交后删除 Redis 全部 login_tokens:*，强制所有在线用户（包括操作者本人）重新登录；"));
children.push(numbered("@Log 记录操作日志。"));
children.push(p("启用模块（停用→启用）为逆操作，关键差异："));
children.push(bullet("仅恢复 remark 中带有 [模块停用:module_key] 标记的角色和任务，恢复后移除标记——避免把“本来就人为暂停”的角色/任务误激活（V1.1 补强：停用标记精确恢复）；"));
children.push(bullet("菜单整棵子树恢复 status=0；"));
children.push(bullet("事务提交后清除登录缓存。"));
children.push(h2("5.3 角色管理改造"));
children.push(bullet("角色列表默认过滤：查询条件追加 (r.module_key IS NULL OR 模块状态=启用)，已停用模块的角色不再显示（已确认决策1）；"));
children.push(bullet("新增“所属模块”列与“模块”下拉筛选，支持按模块查看角色；"));
children.push(bullet("列表增加“显示已停用模块角色”勾选项，勾选后显示全部，停用模块角色以灰色标记展示；"));
children.push(bullet("用户管理新建/编辑弹窗的角色下拉（selectRoleAll）同步过滤，分配角色时选不到已停用模块的角色；"));
children.push(bullet("角色新增/编辑界面增加“所属模块”下拉（数据来自 sys_module，含“平台角色”选项）。"));
children.push(h2("5.4 定时任务列表改造"));
children.push(bullet("任务列表增加“所属模块”列与模块筛选，已停用模块的任务默认过滤（与角色策略一致）。"));
children.push(h2("5.5 首页 Dashboard 联动"));
children.push(bullet("HomeController 按菜单状态过滤模块概览的逻辑（已上线）保留，作为第二道防线，与本功能天然联动；"));
children.push(bullet("模块概览、最近动态面板只展示启用模块的数据，无需额外修改。"));

children.push(h1("六、安全性与审计"));
children.push(bullet("权限校验：模块关闭后权限点清空，业务接口自动 403，防止绕过界面直接调用 API；"));
children.push(bullet("核心保护：is_core=1 的模块（系统管理、消息中心）后端拒绝关闭操作，前端开关置灰；"));
children.push(bullet("并发防护：开关接口使用条件更新（乐观语义），防止两个管理员同时切换同一模块造成状态错乱；"));
children.push(bullet("操作留痕：开关操作通过 @Log 注解写入 sys_oper_log，记录操作人、时间、模块、前后状态；"));
children.push(bullet("数据安全：开关操作只改状态字段和 remark 标记，不删除任何业务数据，恢复开关后数据原样可见。"));

children.push(h1("七、边界与风险"));
children.push(table([2200, 3600, 3226], [
  ["风险点", "说明", "对策"],
  ["子菜单个性化状态丢失", "若关闭前有菜单被单独停用，整树启用后会一并恢复", "V1 接受此粒度，文档注明；特殊需求手工微调"],
  ["强制下线影响", "开关切换后所有在线用户（含操作者本人）需重新登录", "确认弹窗明确提示（已确认决策2）"],
  ["停用状态歧义", "批量启用可能误激活“本来就人为暂停”的角色/任务", "V1.1 已补强：remark 停用标记，启用时精确恢复（见 5.2）"],
  ["个别接口未加权限注解", "少数公共下拉/查询接口无 @PreAuthorize，模块关闭后仍可被调用（页面入口已消失，风险低）", "V1 接受；V2 增加模块级 URL 拦截器按前缀二次校验"],
  ["Flowable 流程定义", "V1 不挂起流程引擎内已部署的流程定义", "流程中心关闭时菜单+角色已足够阻断入口；V2 可增加按分类挂起流程定义"],
  ["多环境同步", "传丰环境与标准环境需保持功能一致", "实施完成后按 HomeController 补丁同款流程同步至标准环境"],
]));

children.push(h1("八、测试用例"));
children.push(table([700, 3400, 4926], [
  ["编号", "测试场景", "预期结果"],
  ["T1", "关闭“营销管理”模块", "菜单消失、角色隐藏、任务暂停、在线用户被下线；重新登录后左侧无营销菜单"],
  ["T2", "以营销角色账号登录并调用营销接口", "无菜单权限，接口返回 403"],
  ["T3", "角色管理页查看", "默认不显示已停用模块角色；勾选“显示已停用模块角色”后可见并带灰色标记"],
  ["T4", "新建用户弹窗选择角色", "下拉中不含已停用模块的角色"],
  ["T5", "重新启用“营销管理”模块", "仅恢复因关模块而停用的角色/任务（带标记者），数据与关闭前一致"],
  ["T6", "尝试关闭“系统管理”", "前端开关置灰；直接调接口被后端拒绝并提示“平台基础模块不允许关闭”"],
  ["T7", "首页 Dashboard", "只展示启用模块的概览与动态，0 值模块不推送动态"],
  ["T8", "操作日志", "sys_oper_log 中可查到开关操作记录（操作人/时间/模块/结果）"],
  ["T9", "并发切换", "两个会话同时切换同一模块，后者收到“状态已被他人变更”提示"],
  ["T10", "人为暂停恢复验证", "预先人为暂停某模块角色，关闭再启用模块，该角色保持暂停（标记精确恢复）"],
]));

children.push(h1("九、实施步骤"));
children.push(numbered("SQL 脚本：创建 sys_module 表、sys_role/sys_job 扩展列、种子数据与回填（先备份快照）；"));
children.push(numbered("后端开发：SysModule 实体/Mapper/Service/Controller，级联开关事务服务（含停用标记、条件更新、事务后清缓存），角色与任务列表查询改造；"));
children.push(numbered("前端开发：模块开关页面、角色管理与定时任务列表改造、用户角色下拉过滤；"));
children.push(numbered("菜单与权限：注册“模块开关”菜单及权限标识到 sys_menu/sys_role_menu（超级管理员默认可见）；"));
children.push(numbered("编译重启并执行测试用例 T1～T10；"));
children.push(numbered("同步标准环境：代码文件差异比对后复制；种子 SQL 分环境生成（传丰环境初始状态对齐现状、标准环境业务模块全部启用），执行相同表结构与代码，编译验证（服务未运行则仅编译）。"));

children.push(h1("十、交付物清单"));
children.push(bullet("SQL 脚本：sys_module 建表 + 存量回填 + 模块开关菜单注册（含回滚语句，分环境版本）；"));
children.push(bullet("后端代码：SysModule 全套（domain/mapper/service/controller）及角色、定时任务查询改造；"));
children.push(bullet("前端代码：系统管理→模块开关页面（vue），角色管理/定时任务/用户角色下拉改造；"));
children.push(bullet("测试记录：T1～T10 执行结果；"));
children.push(bullet("标准环境同步记录。"));

children.push(h1("十一、V2 增强项规划（不阻塞本次上线）"));
children.push(bullet("模块级 URL 拦截器：按请求路径前缀匹配模块状态，对未加权限注解的公共接口做二次校验；"));
children.push(bullet("Flowable 流程定义按分类挂起/激活，随模块开关联动；"));
children.push(bullet("模块新增/编辑功能：新业务模块接入时在页面自助注册（名称、标识、根菜单、图标），无需改 SQL。"));

const doc = new Document({
  styles: {
    default: { document: { run: { font: F, size: 21 } } },
    paragraphStyles: [
      { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 30, bold: true, font: F, color: "1F4E79" },
        paragraph: { spacing: { before: 280, after: 160 }, outlineLevel: 0, keepNext: false, keepLines: false } },
      { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 25, bold: true, font: F, color: "2E5E8C" },
        paragraph: { spacing: { before: 200, after: 120 }, outlineLevel: 1, keepNext: false, keepLines: false } },
    ],
  },
  numbering: { config: [
    { reference: "bullets", levels: [{ level: 0, format: LevelFormat.BULLET, text: "\u2022", alignment: AlignmentType.LEFT,
      style: { paragraph: { indent: { left: 480, hanging: 240 } } } }] },
    { reference: "nums", levels: [{ level: 0, format: LevelFormat.DECIMAL, text: "%1.", alignment: AlignmentType.LEFT,
      style: { paragraph: { indent: { left: 480, hanging: 240 } } } }] },
  ] },
  sections: [{
    properties: { page: { size: { width: 11906, height: 16838 }, margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 } } },
    headers: { default: new Header({ children: [new Paragraph({ alignment: AlignmentType.RIGHT, children: [new TextRun({ text: "传丰数字化平台 · 模块开关功能实施方案 V1.1", size: 16, color: "999999" })] })] }) },
    footers: { default: new Footer({ children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [
      new TextRun({ text: "第 ", size: 16 }), new TextRun({ children: [PageNumber.CURRENT], size: 16 }), new TextRun({ text: " 页", size: 16 }),
    ] })] }) },
    children,
  }],
});

Packer.toBuffer(doc).then(buf => {
  fs.writeFileSync("d:\\EDOP_QY\\EDOP_CFSP\\EDOP_CFSP_KF\\传丰ERP模块开关功能实施方案.docx", buf);
  console.log("OK " + buf.length + " bytes");
});

# 传丰食品 ERP 营销管理——多类型客户体系开发方案

| 项目 | 内容 |
|---|---|
| 方案名称 | 营销管理模块客户体系扩展（企业客户 / 经销商 / 个人客户） |
| 版本 | V2.1（V2.0 → V2.1 变更：二次代码评审修正——①个人客户现结补拦截落点：实测 MkOrder 无付款方式字段，mk_order 新增 settle_type，下单+审核双节点强制；②现有返利 redeem 一次性兑现功能与冲抵台账冲突，明确改造 redeem 走冲抵流程并定义状态映射；③权限前缀统一为现有 marketing:* 体系；④供货价冲突规则修正为与现有"取最新一条"惯例一致（撤销 V2.0 最低价规则）；⑤补 mk_customer_change_log 变更日志表；⑥mk_dealer_profile 补合作起止字段；⑦补 customer_type/parent 索引；⑧补手机号格式校验；非目标补窜货区域拦截与保证金台账边界。V1.9 → V2.0 变更：技术评审修订——①手机号脱敏改后端统一实现（前端脱敏可被 F12 旁路）；②澄清个人客户现结实现（实测 creditLimit≤0 为"不启用额度控制"不拦截，须订单层显式拦截账期）；③补返利冲抵台账表设计 mk_rebate_offset；④补层级树防环、流向防重唯一键、供货价多政策冲突取价规则、mk_all.sql 同步更新约束；P1 工作量调整为 15~18 人天。V1.8 → V1.9 变更：核实两仓 git 拓扑——同一远程的 master/cfsp 两分支，同步机制确定为 git merge（禁止文件复制）；修正前端版本判断——两仓均为 Vue3 无写法差异；V1.7 → V1.8 变更：完成标准仓前置核对（7.4，11 项实测）；V1.6 → V1.7 变更：开发位置决策为"标准仓先行"；V1.5 → V1.6 变更：新增 4.1 通用化与版本策略、batch_no 改可空；V1.4 → V1.5 变更：风险 #7 CFSP 仓批次号核实关闭；V1.3 → V1.4 变更：完善性修订；V1.2 → V1.3 变更：返利冲抵审批简化为一级审批；V1.1 → V1.2 变更：新增第七章开发约束与变更管控；V1.0 → V1.1 变更：手机号明文存储+返利口径确认） |
| 编制日期 | 2026-10-29 |
| 编制 | 产品通（AI 产品搭档） |
| 关联文档 | 《传丰食品ERP行业匹配度分析报告》《传丰食品ERP行业化改造功能点与页面全景清单》 |
| 系统基线 | RuoYi-Vue 单体（Spring Boot + MyBatis + Vue），营销模块 `ruoyi-mk`，后端 8082 |

---

## 一、问题陈述（Problem Statement）

营销管理模块当前**仅支持企业客户（B2B）**：`mk_customer` 表注释即"企业客户"，字段全为企业属性（企业名称、统一社会信用代码、行业、企业规模），无客户类型字段；订单、合同、价格政策、返利政策全部通过 `customer_id` 单一外键挂接该表。

而传丰食品实际销售端是 **"B 端项目制（宾馆餐饮招投标）+ 流通渠道（商超铺货、礼盒团购）"双轨结构**，礼盒团购与散客销售中大量存在**个人客户**；商超铺货、区域分销则依赖**经销商**。当前系统对这两类客户完全没有承载：

- 个人客户无法建档（必填"统一社会信用代码"，语义不符）；
- 经销商与普通企业客户混在一起，无层级、无授权区域、无供货价体系、无流向登记，**食品召回追溯链"成品批次→发货客户"在经销商分销环节断链**。

**不解决的代价**：① 个人客户散客交易靠手工台账，客户资产沉淀不了；② 经销商价格/返利/账期靠口头约定，总经理授信无系统拦截；③ 食品安全召回演练无法闭环，影响大客户验厂与监管合规。

## 二、目标（Goals）

1. **三类客户统一建档**：一套客户主数据承载企业客户、经销商、个人客户，现有 B 端功能（订单/合同/商机/价格/返利）零破坏兼容；
2. **经销商全链路管理**：档案（授权区域/层级）→ 供货价 → 返利结算 → 信用账期 → 分销流向登记，全流程线上化；
3. **个人客户轻量承接**：手机号唯一建档、强制现款现结、脱敏合规，散客交易可录入、可统计；
4. **追溯链闭环**：经销商流向登记补齐"原料批次→工单→成品批次→发货客户→分销去向"的最后一环；
5. **管理可视化**：客户类型、经销商销量/返利/账龄进入现有数据分析看板。

## 三、非目标（Non-Goals）——本期明确不做

| 非目标 | 不做的理由 |
|---|---|
| 微信小程序商城 / 线上下单 | 用户明确排除；个人客户先线下录入，数据模型预留即可 |
| 会员储值 / 积分体系 | 涉及资金类功能，需财务模块联动，属独立立项 |
| POS 收银 / 门店零售 | 传丰无自营门店场景，属过度设计 |
| 财务模块（fin）接口改造 | 应收/账龄报表仅做营销侧视图，财务凭证规则不动 |
| 经销商门户（经销商自助下单） | 需新增外部账号体系，二期后再议 |
| 生产/质量/采购模块改动 | 本期仅营销域，追溯链依赖的批次数据由现有 wms/qms 提供 |
| 窜货下单区域拦截（V2.1 明确边界） | 授权区域本期仅作档案记录与市场边界参考，**不做下单时区域强制校验**；防窜货先靠流向登记事后核查，强制拦截待业务规则成熟后另立期 |
| 保证金收退台账（V2.1 明确边界） | `mk_dealer_profile.deposit_amount` 仅档案金额字段，保证金收款/退还/扣罚台账涉资金流水，与会员储值同属资金类功能，独立立项 |

## 四、总体设计思路

**不另建三套客户表，在 `mk_customer` 上做"客户类型分层"**，理由：

1. 现有订单、合同、商机、价格政策、返利、发货、回款、发票全链路以 `customer_id` 单一外键挂接——换表意味着全链路重构，成本与风险不可接受；
2. 经销商本质上是"特殊的 B 端客户"，个人客户是"轻量档案的 C 端客户"，共用主表 + 类型区分 + 差异化字段，符合主数据管理最佳实践；
3. 现有能力（信用控制、等级价、客户专价、返利政策/结算、客户公海）可直接复用，改造成本最低。

**类型模型**：

```
mk_customer（客户主数据）
├── customer_type = 1 企业客户   → 现有功能，行为不变
├── customer_type = 2 经销商     → 企业客户能力 + 经销商扩展档案 + 流向登记
└── customer_type = 3 个人客户   → 轻量档案 + 强制现结 + 手机号唯一
```

### 4.1 通用化与版本策略（开发位置决策）

**能力行业属性**：本方案 90% 为通用渠道分销管理能力，非食品行业专属——客户类型分层、经销商档案/层级/授权、供货价/返利/信用、个人客户现结，均为快消/日化/酒水/建材等"B 端+C 端混合销售"行业的通用范式；唯一带行业色彩的是流向登记的批次号追溯（食品召回/医药 GSP 合规刚需）。

**开发位置（已决策 2026-10-29）：标准仓先行开发 → 同步至 CFSP 及其他分支代码仓**：
1. 标准仓为唯一开发主线，一次开发、多仓受益，避免"分支先做再回移"的搬运与分歧；
2. **验证方式调整**：标准仓无真实业务数据，须在标准仓测试环境构造模拟数据（三类客户、含层级的经销商、价格政策、返利阶梯）完成自测，再同步 CFSP 用传丰真实业务做验收验证；
3. **同步机制（已核实两仓拓扑 2026-10-29）：git merge，禁止文件复制**：
- 两仓为同一远程仓库（github `210david/ruoyiedop` + gitee 镜像）的两个分支——标准仓 `master`、CFSP `cfsp` 分支，共享全部提交历史；
- 同步流程：标准仓 master 开发完成 push → CFSP 仓 `git fetch origin && git merge origin/master`（在 cfsp 分支上）→ 解决冲突 → push；
- **禁止整仓/目录文件复制覆盖**（会冲掉 cfsp 分支的行业化改造与提交历史）；也不建议"手工在 cfsp 重复改一遍"（产生同内容不同 SHA 的重复提交，历史持续劣化）；
- merge 时三个固定动作：① 冲突文件按 7.2 存量清单逐个处理并回归；② SQL 脚本在 CFSP 库执行前先对目标库 `information_schema` 校准（两库结构可能各自漂移）；③ CFSP 侧补流向登记与 `mk_shipment_detail.batch_no` 的批次取值衔接（标准仓无此代码）。
4. **存量改动以标准仓代码为准**：本方案 7.2 存量改动清单基于 CFSP 仓代码实测得出，标准仓同名文件可能存在版本差异——**标准仓动工前须逐文件重新核对**（核对结果登记进 7.2 清单），差异较大时先向用户确认再动。

**通用性硬性要求**（开发时执行）：
- 字典、文案、注释中不得出现"食品"等行业字样；
- `mk_dealer_sell_flow.batch_no` 为**可空**：项目启用批次管理（食品/医药等）时必填，无批次行业允许为空、仅记下游客户名称；
- 授权品类关联物料分类（通用模型），不绑定食品品类语义；
- 分支仓特有能力（如 CFSP 的 `food_batch` 批次主数据关联）不进入标准仓代码，以扩展点/钩子方式由各分支自行衔接。

## 五、需求明细与用户故事

### 5.1 一期 P0：客户类型化与个人客户承接

**用户故事**

- 作为**销售员**，我希望在客户管理中区分"企业客户 / 经销商 / 个人客户"三类建档，以便不同客户走不同的档案和交易规则；
- 作为**销售员**，我希望只凭姓名+手机号就给散客建档，以便礼盒团购散客快速录入下单；
- 作为**财务人员**，我希望个人客户订单强制现款现结、系统不给出账期，以便避免 C 端坏账；
- 作为**系统管理员**，我希望个人客户手机号在列表中默认脱敏，以便满足《个人信息保护法》最小可见要求。

**需求清单**

| 编号 | 需求 | 优先级 |
|---|---|---|
| P0-01 | 新增字典 `mk_customer_type`（1 企业客户 / 2 经销商 / 3 个人客户），存量数据默认刷为 1 | P0 |
| P0-02 | `mk_customer` 增加 `customer_type` 字段，列表增加类型筛选与标签列 | P0 |
| P0-03 | 新增/编辑表单按类型分支渲染：个人客户隐藏统一社会信用代码/行业/企业规模，必填姓名+手机号 | P0 |
| P0-04 | 个人客户以 `contact_phone` 唯一性校验（同号不允许重复建档，命中提示"已存在客户"并支持跳转）；手机号格式校验（11 位、1[3-9] 号段，V2.1 补） | P0 |
| P0-05 | 个人客户信用控制：`credit_limit` 强制为 0（现结）。**实现澄清（V2.0，实测 `MkCreditServiceImpl.checkCreditForOrder` L77-81）**：额度 ≤0 时直接 return"未启用信用额度控制"，即 0 **不会**拦截任何下单——"仅现结"必须在订单层按 `customer_type=3` 显式拦截账期类付款方式（提示"个人客户仅支持现结"），不能依赖额度为 0 天然生效；**拦截落点（V2.1，实测 `MkOrder.java` 无付款方式字段，`payment_method` 仅存在于 mk_contract）**：`mk_order` 新增 `settle_type`（0现结/1账期，默认0），个人客户**下单+订单审核双节点**强制 settle_type=0；个人客户签订合同时 `payment_method` 强制"一次性" | P0 |
| P0-06 | 手机号明文存储（本期不加密），**脱敏由后端统一实现（V2.0）**：列表/详情接口返回脱敏值（138\*\*\*\*8888），防止前端脱敏被 F12 查看响应旁路；导出接口独立鉴权（`marketing:customer:exportPersonal`，V2.1 统一权限前缀）返回明文；**导出记录操作日志**（导出人/时间/条数，明文导出留痕审计，V2.1 补） | P0 |
| P0-07 | 存量客户数据迁移：全部刷为 `customer_type=1`，业务零感知 | P0 |

**验收标准（关键项）**

- [ ] 新建个人客户仅需客户名称+手机号，无需信用代码；保存后列表类型列显示"个人客户"
- [ ] Given 已存在手机号 138xxxx8888 的个人客户，When 再次用同号建档，Then 拦截并提示已有客户编号
- [ ] 个人客户创建销售订单，When 结算方式选择"账期"，Then 系统拦截提示"个人客户仅支持现结"（订单审核节点同样拦截）
- [ ] 任意角色打开客户列表，手机号显示为脱敏格式（**后端接口返回值即为脱敏，F12 查看响应不含明文**）；持有"个人客户导出"权限的角色导出时可见全号
- [ ] 升级前已存在的全部客户在升级后类型均为"企业客户"，原有订单/合同/价格政策查询结果不变

### 5.2 二期 P1：经销商管理体系

**用户故事**

- 作为**销售经理**，我希望为经销商维护授权区域、授权品类和经销层级（一级/二级），以便划分市场边界、避免窜货纠纷；
- 作为**销售员**，我希望经销商下单自动取"经销商供货价"（按层级区分），以便报价不出错；
- 作为**财务人员**，我希望经销商的返利（按期间采购额阶梯）自动按现有返利结算流程计算，以便月度返利核算不靠 Excel；
- 作为**质量/仓库人员**，我希望登记经销商的分销流向（卖给谁、什么批次、多少量），以便发生食品安全召回时能逆向通知到位；
- 作为**总经理**，我希望经销商欠款超信用额度时下单被拦截，以便口头授信变成系统规则。

**需求清单**

| 编号 | 需求 | 优先级 |
|---|---|---|
| P1-01 | `mk_customer` 增加 `parent_customer_id`（上级经销商，可空）：支持二级分销层级树展示；经销商变更上级需记录变更日志；保存上级时必须做**环路检测**（沿 parent 链向上遍历遇到自身即拒绝，防 A→B→A 死循环） | P1 |
| P1-02 | 经销商扩展档案表 `mk_dealer_profile`：授权区域（省/市/区多选）、授权品类（关联物料分类）、经销层级（字典 `mk_dealer_level`：1 一级 / 2 二级）、保证金金额、合作起止 | P1 |
| P1-03 | 经销商客户等级字典扩展：`marketing_customer_level` 增加"一级经销商 / 二级经销商"档位（或独立经销商等级字典），供等级价取价 | P1 |
| P1-04 | 价格政策 `mk_price_policy` 价格类型扩展为 3=经销商供货价（按层级+品类配置），下单取价优先级：客户专价 → 经销商供货价 → 等级价 → 手填；**同层级同品类存在多条生效政策时取最新一条**（V2.1 修正：与现有 `selectCustomerPrice`/`selectLevelPrice`"启用+在有效期取最新一条"惯例一致，撤销 V2.0 的最低价规则，避免同一取价链两套规则） | P1 |
| P1-05 | 返利打通：现有 `mk_rebate_policy`（已含期间类型、阶梯区间、返利率、生效期）按经销商维度配置，`mk_rebate_settle` 自动归集经销商期间采购额并计算返利；**返利冲抵规则**（已确认口径）：① 财务性质按"销售折扣（销售折让）"处理，红字发票由财务在税控系统开具，ERP 侧只记录数据并向财务推送待办（消息中心）；② 冲抵走营销侧台账闭环——结算单状态流转：未冲抵→部分冲抵→全部冲抵，允许一张返利拆分多笔冲抵，**台账落 `mk_rebate_offset` 表（DDL 见 6.1，V2.0 补充）**；③ 预留 `FinVoucherRule` 凭证生成接口，财务模块启用后对接（对接工作另计 2~3 人天）；④ 冲抵审批：**一级审批即可——所有冲抵单统一由财务审核后生效，暂不设金额分级**（后续如需分级再扩展，阈值预留可配置）；⑤ **现有"兑现"功能改造（V2.1，实测 `MkRebateController.redeem`/`MkRebateServiceImpl.redeem` L233-256 为一次性兑现、无审批无拆分）**：现有 redeem 接口与前端"兑现"按钮改造为走冲抵台账流程，**原直接兑现入口关闭**（防止绕开财务审批）；状态映射：`offset_status=2 全部冲抵` 时结算单 `status` 才置"已兑现"，部分冲抵期间保持"待兑现" | P1 |
| P1-06 | 信用控制复用：经销商配置 `credit_limit`（额度）+ `credit_days`（账期）+ `credit_status`（冻结），下单校验欠款上限，超限拦截并提示额度余额 | P1 |
| P1-07 | **经销商流向登记**：新表 `mk_dealer_sell_flow`（经销商ID、日期、物料ID、**批次号**、数量、下游客户类型、下游客户名称、来源发货单号），支持手工录入 + 按发货单一键生成草稿（批次号直接取自 `mk_shipment_detail.batch_no`，已核实存在）；`shipment_id + material_id` 建唯一键**防同一发货单重复生成流向草稿**（手工录入时 shipment_id 为 NULL 不受约束） | P1 |
| P1-08 | 流向数据接入质量追溯：追溯查询（qms）"成品批次→发货客户"节点下挂"经销商→下游去向"明细，支持召回模拟演练导出 | P1 |
| P1-09 | 经销商列表页：独立 Tab / 菜单入口，展示层级树、授权区域、信用状态、本期采购额、未结返利 | P1 |
| P1-10 | 客户类型变更：企业客户 ↔ 经销商 允许互转（记变更日志、历史单据不受影响）；个人客户不允许转为企业/经销商（差异过大，需另建档案）；变更入口设独立按钮权限 | P1 |

**验收标准（关键项）**

- [ ] 一级经销商下可挂二级经销商，层级树正确展示；删除有下级的经销商被拦截
- [ ] 二级经销商下单，取价按"二级经销商供货价"档位返回，价格来源在订单明细可追溯
- [ ] 返利结算单按经销商月度采购额落阶梯区间，返利率计算结果与手工核算一致
- [ ] Given 经销商欠款 8 万、信用额度 10 万，When 再下 3 万订单，Then 拦截并提示"超信用额度，剩余可用 2 万"
- [ ] 流向登记记录批次号后，质量追溯查询输入该成品批次可看到"发货单 → 经销商 → 下游客户"完整链路
- [ ] 冻结状态的经销商无法创建新订单，已有在途订单不受影响
- [ ] 单笔冲抵（不论金额，如 8,000 元）：财务审核通过即完成冲抵并生效，结算单状态正确流转；财务未审核前冲抵不生效
- [ ] 一笔 6,000 元返利拆分为 2,500 + 3,500 两笔冲抵后，结算单状态为"部分冲抵"，剩余额度 0 后流转"全部冲抵"
- [ ] 冲抵生效后，消息中心向财务推送待办（含结算单号、冲抵金额、对应订单），红票开票以该待办为依据
- [ ] 企业客户变更为经销商后，其历史订单/合同/价格政策查询正常；个人客户编辑页无"变更类型"入口
- [ ] 将经销商 A 的上级改为其下级 B（形成 A→B→A 环）被拦截并提示
- [ ] 同一发货单重复执行"生成流向草稿"不产生重复流向记录

### 5.3 三期 P2：数据分析增强

| 编号 | 需求 | 优先级 |
|---|---|---|
| P2-01 | 数据分析看板按客户类型维度拆分（企业/经销商/个人：销售额、订单数、客单价、占比趋势） | P2 |
| P2-02 | 经销商专项报表：分经销商销量/返利/账龄/流向汇总表，支持导出（开发前评估 `auth_regions` JSON 存储的区域检索性能，必要时改关联表） | P2 |
| P2-03 | 客户流失预警（现有 AI 能力）扩展经销商维度 | P2 |
| P2-04 | 个人客户消费 Top 分析（脱敏统计口径，不输出明文手机号） | P2 |

## 六、技术设计

### 6.1 数据库变更（DDL 草案）

```sql
-- 1. 客户类型字典（沿用 mk_all.sql 第 405-434 行的字典规范）
INSERT INTO sys_dict_type (dict_name, dict_type, status, remark) VALUES
('客户类型', 'mk_customer_type', '0', '企业客户/经销商/个人客户');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status) VALUES
(1, '企业客户', '1', 'mk_customer_type', 'primary', 'Y', '0'),
(2, '经销商',   '2', 'mk_customer_type', 'warning', 'N', '0'),
(3, '个人客户', '3', 'mk_customer_type', 'info',    'N', '0');

-- 2. 主表扩展
ALTER TABLE mk_customer
  ADD COLUMN customer_type      char(1)  DEFAULT '1' COMMENT '客户类型（1企业 2经销商 3个人）',
  ADD COLUMN parent_customer_id bigint   DEFAULT NULL COMMENT '上级经销商ID（仅经销商使用）',
  ADD COLUMN contact_phone      varchar(20) DEFAULT NULL COMMENT '个人客户手机号（本期明文存储）',
  ADD UNIQUE KEY uk_phone (contact_phone),             -- 个人客户手机号唯一
  ADD KEY idx_customer_type (customer_type),           -- 列表类型筛选（V2.1 补）
  ADD KEY idx_parent (parent_customer_id);             -- 层级树递归（V2.1 补）

UPDATE mk_customer SET customer_type = '1';  -- 存量数据迁移

-- 2.1 订单结算方式（V2.1：个人客户现结拦截落点——实测 MkOrder 无付款方式字段，payment_method 仅在 mk_contract）
ALTER TABLE mk_order
  ADD COLUMN settle_type char(1) DEFAULT '0' COMMENT '结算方式（0现结 1账期）——个人客户强制0，下单与订单审核双节点校验';

-- 3. 经销商扩展档案
CREATE TABLE mk_dealer_profile (
  profile_id      bigint PRIMARY KEY AUTO_INCREMENT,
  customer_id     bigint NOT NULL COMMENT '客户ID',
  dealer_level    char(1)  COMMENT '经销层级（1一级 2二级）',
  auth_regions    varchar(500) COMMENT '授权区域（省市区JSON）',
  auth_categories varchar(500) COMMENT '授权品类（物料分类ID串）',
  deposit_amount  decimal(12,2) DEFAULT 0 COMMENT '保证金（仅档案金额，收退台账为非目标）',
  coop_start_date date COMMENT '合作开始日期（P1-02，V2.1 补）',
  coop_end_date   date COMMENT '合作结束日期（P1-02，V2.1 补）',
  status          char(1) DEFAULT '0',
  -- BaseEntity 标准审计字段（create_by/create_time/update_by/update_time/remark）
  UNIQUE KEY uk_customer (customer_id)
) COMMENT '经销商扩展档案';

-- 4. 经销商流向登记（追溯链关键表）
CREATE TABLE mk_dealer_sell_flow (
  flow_id         bigint PRIMARY KEY AUTO_INCREMENT,
  dealer_id       bigint NOT NULL COMMENT '经销商客户ID',
  flow_date       date NOT NULL COMMENT '流向日期',
  material_id     bigint NOT NULL COMMENT '物料ID',
  batch_no        varchar(64) DEFAULT NULL COMMENT '批次号（启用批次管理的行业/物料必填，其他可空）',
  quantity        decimal(12,3) NOT NULL,
  downstream_type char(1) COMMENT '下游客户类型（1企业 2经销商 3个人 4终端不详）',
  downstream_name varchar(200) COMMENT '下游客户名称',
  shipment_id     bigint COMMENT '来源发货单ID',
  -- BaseEntity 标准审计字段
  KEY idx_batch (batch_no),
  KEY idx_dealer_date (dealer_id, flow_date),
  UNIQUE KEY uk_shipment_material (shipment_id, material_id)  -- 防同一发货单重复生成流向草稿（NULL 不受约束）
) COMMENT '经销商分销流向登记';

-- 5. 返利冲抵台账（P1-05，V2.0 补充设计）
-- 注：mk_rebate_settle 实际列以目标库 information_schema 实测为准（同 6.4 前置动作）
ALTER TABLE mk_rebate_settle
  ADD COLUMN offset_status  char(1)       DEFAULT '0' COMMENT '冲抵状态（0未冲抵 1部分冲抵 2全部冲抵）',
  ADD COLUMN offset_amount  decimal(12,2) DEFAULT 0  COMMENT '已冲抵累计金额';

CREATE TABLE mk_rebate_offset (
  offset_id       bigint PRIMARY KEY AUTO_INCREMENT,
  settle_id       bigint NOT NULL COMMENT '返利结算单ID',
  offset_amount   decimal(12,2) NOT NULL COMMENT '本次冲抵金额',
  offset_date     date NOT NULL COMMENT '冲抵日期',
  audit_status    char(1) DEFAULT '0' COMMENT '审批状态（0待财务审核 1审核通过 2驳回）——一级审批，财务审核通过即生效',
  audit_by        varchar(64)  COMMENT '审核人',
  audit_time      datetime     COMMENT '审核时间',
  audit_opinion   varchar(500) COMMENT '审核意见',
  msg_id          bigint       COMMENT '财务待办消息ID（消息中心推送，红票开票依据）',
  -- BaseEntity 标准审计字段（create_by/create_time/update_by/update_time/remark）
  KEY idx_settle (settle_id)
) COMMENT '返利冲抵台账（财务性质按销售折扣处理，红票由财务据待办开具，ERP 仅记台账）';

-- 6. 客户变更日志（P1-01 上级经销商变更 / P1-10 客户类型变更，V2.1 补；参照 mk_contract_change_log 规范）
CREATE TABLE mk_customer_change_log (
  log_id        bigint PRIMARY KEY AUTO_INCREMENT,
  customer_id   bigint NOT NULL COMMENT '客户ID',
  change_type   varchar(20) NOT NULL COMMENT '变更类型（customer_type客户类型 parent上级经销商）',
  old_value     varchar(200) COMMENT '变更前值',
  new_value     varchar(200) COMMENT '变更后值',
  change_reason varchar(500) COMMENT '变更原因',
  -- BaseEntity 标准审计字段（create_by/create_time/update_by/update_time/remark）
  KEY idx_customer (customer_id)
) COMMENT '客户变更日志';
```

### 6.2 后端改动点（ruoyi-mk）

| 文件/层 | 改动 |
|---|---|
| `domain/MkCustomer.java` | 增加 customerType、parentCustomerId、contactPhone 字段；个人客户保存校验逻辑 |
| `domain/MkDealerProfile.java`、`MkDealerSellFlow.java`（新增） | 经销商档案与流向实体 |
| `mapper/`（新增） | `MkDealerProfileMapper.java/.xml`、`MkDealerSellFlowMapper.java/.xml`，按 RuoYi Mapper 接口 + XML 规范 |
| `controller/MkDealerProfileController.java`、`MkDealerSellFlowController.java`（新增） | 沿用现有 Controller 注解与权限前缀规范（V2.1 统一为现有 `marketing:*` 体系：`marketing:dealer:*`、`marketing:flow:*`，不得另起 `mk:*` 前缀） |
| `service/impl/MkCustomerServiceImpl.java` | 手机号唯一性校验（含 `DuplicateKeyException` 捕获转友好提示，兜底并发建档）、个人客户 creditLimit=0 强制、类型分支校验（如：个人客户不可设为上级经销商）、**上级经销商环路检测** |
| `domain/MkRebateOffset.java`、`mapper/MkRebateOffset*`、`controller/MkRebateOffsetController.java`（新增，V2.0 补充） | 返利冲抵台账实体/Mapper/接口，权限前缀 `marketing:rebate:offset:*`（V2.1 统一）；`MkRebateServiceImpl` 增加冲抵登记与结算单状态流转（未冲抵→部分冲抵→全部冲抵）及财务待办推送；**现有 `redeem` 接口与前端"兑现"按钮改造为走冲抵流程，全部冲抵后 status 才置"已兑现"（V2.1）** |
| `domain/MkCustomerChangeLog.java`、`mapper/MkCustomerChangeLog*`（新增，V2.1） | 客户变更日志（客户类型/上级经销商变更），由 `MkCustomerServiceImpl` 在变更动作中写入 |
| `service/impl/MkPricePolicyServiceImpl.java` | 取价链插入"经销商供货价"档位（客户专价 → 经销商供货价 → 等级价 → 手填）；同层级同品类多条生效政策**取最新一条**（V2.1 修正，与现有 selectCustomerPrice/selectLevelPrice 惯例一致） |
| `service/impl/MkOrderServiceImpl.java` | 下单校验区分类型：个人客户按 `customer_type` **显式拦截账期**（实测 `credit_limit=0` 在 `MkCreditServiceImpl` L77-81 为"不启用额度控制"，不会拦截，不能依赖）；**`mk_order` 新增 `settle_type`，下单+订单审核双节点强制个人客户现结（V2.1）**；经销商额度校验提示余额 |
| 脱敏展示（V2.0 改后端） | 列表/详情接口在 Service/Controller 层统一返回脱敏手机号（防 F12 查看响应旁路），导出接口单独鉴权返回明文；本期不做存储层加密，后续如需加密再补 TypeHandler，DDL 已预留扩展空间 |
| 权限 | 新增菜单+按钮权限 SQL（参照 `deploy/config/sql/add_customer_pool_menu.sql` 的脚本规范） |

### 6.3 前端改动点（两仓均为 Vue 3 + `<script setup>` + Element Plus，写法通用，可随 git merge 直接同步）

| 页面 | 改动 |
|---|---|
| `customer/index.vue` | 列表加"客户类型"筛选/标签列；表单按类型动态显隐字段；个人客户手机号列直接展示后端返回的脱敏值（前端不做二次处理） |
| `customer/`（经销商 Tab 或独立 `dealer/index.vue`） | 层级树、授权区域、信用状态、本期采购额、未结返利 |
| 新增 `dealerFlow/index.vue` | 流向登记列表 + 手工录入 + 按发货单生成草稿 |
| `pricePolicy/` | 价格类型选项增加"经销商供货价"，配置界面按层级+品类 |
| `order/` | 取价逻辑对接新价格链；订单表单新增"结算方式"（现结/账期，V2.1），个人客户强制现结并禁用账期选项 |
| `rebate/`（返利结算页，V2.1 补） | "兑现"按钮改造为冲抵登记（金额可拆分、财务一级审批）；结算单展示冲抵状态与冲抵明细；原直接兑现入口移除 |
| 字典管理 | `mk_customer_type`、`mk_dealer_level` 字典维护入口 |

### 6.4 数据迁移与兼容

> ⚠️ **DDL 执行前置动作（7.4 核对 #3 的结论）**：两仓建表脚本均滞后于 Java 实体（mk_customer 的 credit 列不在任何脚本中），本节 DDL 草案**不得直接执行**——第一步先对目标库执行 `SHOW CREATE TABLE mk_customer` / 查 `information_schema`，出库表实测基线后，将本节 DDL 与基线 diff 校准为正式升级脚本。

1. 存量 `mk_customer` 全量刷 `customer_type='1'`，现有页面行为不变；
2. 已有价格政策（客户专价/等级价）优先级不变，经销商供货价插在中间，**不改变存量客户取价结果**；
3. 手机号唯一键仅约束个人客户（企业客户该列为 NULL，MySQL 唯一索引对 NULL 不生效，天然兼容）；
4. **逻辑删除与唯一键冲突处理**：客户为 del_flag 逻辑删除，删除个人客户时必须同步置空 `contact_phone`，否则同手机号将因唯一键占用而无法重新建档（删除动作在 Service 层统一处理，并在删除前提示"删除后同手机号可重新建档"）；
5. **客户类型变更规则**：企业客户 ↔ 经销商 允许互相变更（记录变更日志，历史单据不受影响）；个人客户**不允许**变更为企业/经销商（数据模型差异大，如有需要走"新建档案"）；变更入口仅在客户编辑页，需"客户类型变更"独立按钮权限；
6. 升级脚本按项目惯例放 `sql/` + `deploy/config/sql/` 双份，附回滚语句（DROP 新增列/表不影响存量）；
7. **`sql/mk_all.sql` 全量初始化脚本须同步纳入本次全部新增列/表/字典**（V2.0 补充：否则新环境初始化即缺字段）；注意 mk_all.sql 为 DROP+CREATE 全量脚本，升级环境**严禁直接执行**，仅用于新库初始化。

## 七、开发约束与变更管控

**本节为强制约束，优先级高于排期与实现便利。**

### 7.1 基本要求

1. **新增功能一律按项目开发规范完善开发**：遵循 RuoYi 分层规范（domain/mapper/service/controller）、字典与权限菜单 SQL 脚本规范（参照 `sql/mk_all.sql` 字典写法、`deploy/config/sql/add_customer_pool_menu.sql` 菜单脚本写法）、前端页面规范（`ruoyi-ui/src/views/mk/` 现有页面结构）、编号规则（复用 `MkNumberRule` 体系）；
2. **存量功能"非必要不动"**：凡需修改已开发完成的功能代码（Java/Vue/SQL），必须在动工前将改动文件、改动内容、影响范围提交用户（阿磊）逐项确认，确认后方可修改；
3. **存量改动配套回归**：每确认一处存量改动，必须列出关联的既有功能回归点，随版本一起回归验证（如改取价链，须回归企业客户"客户专价→等级价"原有取价结果不变）。

### 7.2 新增与存量改动边界清单（开发前评审用）

**纯新增（不需确认，直接按规范开发）**：

| 类型 | 内容 |
|---|---|
| 数据库 | `mk_customer_type`/`mk_dealer_level` 字典、`mk_dealer_profile` 表、`mk_dealer_sell_flow` 表、`mk_rebate_offset` 表、`mk_customer_change_log` 表（V2.1 补）、新菜单/按钮权限 SQL |
| 后端 | `MkDealerProfile*`（domain/mapper/service/controller）、`MkDealerSellFlow*`（domain/mapper/service/controller）、`MkRebateOffset*`、`MkCustomerChangeLog*`（domain/mapper，V2.1 补） |
| 前端 | `views/mk/dealer/`（经销商列表页）、`views/mk/dealerFlow/`（流向登记页） |

**需动存量（动工前逐项向用户确认）**：

| 存量文件 | 改动内容 | 影响的既有功能 | 优先级 |
|---|---|---|---|
| `MkCustomer.java` + Customer 三层 | 加 customer_type、parent_customer_id、contact_phone 字段及校验 | 客户管理、客户公海、线索转化 | P0 确认 |
| `mk_customer` 表结构 | ALTER 加列 + 唯一键 | 全部挂客户的既有查询/报表 | P0 确认 |
| `views/mk/customer/index.vue` | 表单分支渲染、列表加类型列 | 客户管理页面交互 | P0 确认 |
| `MkOrderServiceImpl.java` | 下单信用校验按客户类型分支；settle_type 双节点强制现结 | 销售订单下单流程 | P0 确认 |
| `mk_order` 表 + `MkOrder.java` + 订单前端表单（V2.1 补） | 新增 `settle_type` 字段（默认 0 现结）及表单"结算方式" | 销售订单、订单报表/导出 | P0 确认 |
| `MkPricePolicyServiceImpl.java` | 取价链插入"经销商供货价"档位 | **企业客户取价结果必须不变** | P1 确认 |
| `views/mk/order/`、`pricePolicy/` | 取价对接、个人客户隐藏账期、价格类型选项 | 订单、价格政策页面 | P1 确认 |
| `mk_rebate_settle` 相关（`MkRebateServiceImpl` + 表加 offset_status/offset_amount 列） | 返利结算增加冲抵状态流转与审批（台账表 `mk_rebate_offset` 为纯新增） | 返利政策/结算既有流程 | P1 确认 |
| `MkRebateController.redeem` + `MkRebateServiceImpl.redeem` + `views/mk/rebate/`（V2.1 补） | 现有一次性"兑现"改造为冲抵台账流程，关闭直接兑现入口；状态映射：全部冲抵后 status 才置已兑现 | 返利兑现既有操作（防绕开财务审批） | P1 确认 |
| qms 追溯查询 | "发货客户"节点下挂经销商流向明细 | 质量追溯既有查询 | P1 确认 |

### 7.3 变更确认流程

新增存量改动需求（含开发过程中临时发现需要动存量的情况）→ 向用户说明：**改哪个文件、改什么、影响哪些既有功能、回归方案** → 用户确认 → 执行 → 回归验证 → 在本文档 7.2 清单中登记实际改动。

### 7.4 标准仓前置核对结果（2026-10-29 实测，标准仓：E:\AIcode\EDOP\RuoYi\RuoYi-Vue3）

**背景**：CFSP 分支仓由标准仓复制而来，仅做食品行业化改造（批次等）——核对结果与此吻合，差异面小且可枚举。

| # | 核对项 | CFSP 分支仓 | 标准仓 | 结论 |
|---|---|---|---|---|
| 1 | `ruoyi-mk` 模块 | 有 | 有 | ✅ 一致 |
| 2 | `MkCustomer.java` | 28 字段，含 credit 三件套，无 customerType | 同（字段一致） | ✅ 基线一致，P0 改法可直接套用 |
| 3 | `mk_customer` 建表 DDL | 脚本未见 credit 列 | `mk_all.sql` L12 建表，**无 credit_limit/days/status、无 phone** | ⚠️ **两仓建表脚本均滞后于 Java 实体**（实体有信用字段、订单服务有信用校验，DDL 脚本没有对应列）——实际列以目标库 `information_schema` 实测为准，DDL 草案执行前必须先出库表实测基线 |
| 4 | `MkOrderServiceImpl` 信用校验 | 有 | 有（L110/L163 冻结+额度拦截） | ✅ 一致，P0-05/P1-06 在此分支 |
| 5 | 价格/返利服务 | 有 | 有（`MkRebateServiceImpl` 合并实现返利政策+结算） | ✅ 基本一致 |
| 6 | `MkShipmentDetail.batch_no` | **有**（食品行业化改造项） | **无** | ⚠️ 核心差异：标准仓流向登记批次号将为空（设计已允许），CFSP 同步后由分支自行衔接批次取值 |
| 7 | `MkBidArchive`（招投标归档） | 有 | 无 | CFSP 特有，与本方案无关 |
| 8 | `marketing_customer_level` 字典 | 有 | 有（`mk_all.sql` L406） | ✅ 一致，P1-03 扩展档位做法通用 |
| 9 | ~~前端技术栈差异~~ **已核实修正（2026-10-29）**：两仓前端均为 Vue 3（标准仓 3.5.26 / CFSP 3.5.44，`<script setup>` + Element Plus）——此前"CFSP 为 Vue2"的判断有误（被仓库名 RuoYi-Vue 误导）；前端代码可随 git merge 直接同步 | ✅ 无差异 | 前端改动一次开发两仓通用 | 已修正 |
| 10 | 前端 mk 页面目录 | 26 个（含 bid） | 25 个（无 bid） | ✅ 结构一致，customer/order/pricePolicy/rebate 页面齐全 |
| 11 | qms 追溯 | `QmsTraceController` | `QmsTraceController` | ✅ 一致，P1-08 接入点存在 |
| 12 | `creditLimit=0` 语义（V2.0 实测补充） | 同实现 | `MkCreditServiceImpl` L77-81：额度 ≤0 直接 return"未启用信用额度控制"，**不拦截任何下单** | ⚠️ P0-05 已按此修正：个人客户现结须在订单层按 `customer_type` 显式拦截账期，不能依赖额度=0 |

**核对结论**：标准仓具备开发条件，方案第六章技术设计适用；三处差异（#3 表结构以库为准、#6 批次号、#9 Vue3 写法）已在本方案对应章节修正。

## 八、成功指标

| 指标 | 基线 | 目标（上线后 1 个月） | 测量方式 |
|---|---|---|---|
| 三类客户建档覆盖率 | 个人/经销商 0 | 散客交易 100% 系统内建档；经销商档案 100% 录入 | mk_customer 类型分布查询 |
| 经销商流向登记率 | 0 | 经销商发货单 ≥ 80% 生成流向记录 | mk_dealer_sell_flow / mk_shipment 关联比 |
| 召回演练闭环 | 无法演练 | 按批次正向/逆向追溯 ≤ 30 分钟出完整清单 | qms 追溯查询演练记录 |
| 散客坏账 | 手工台账无统计 | 个人客户赊销单量 = 0 | 订单付款方式校验日志 |
| 返利核算耗时 | Excel 手工核算（估 0.5~1 天/月） | 系统自动结算 ≤ 10 分钟人工复核 | 返利结算单操作日志 |

## 九、工作量估算与排期

| 阶段 | 内容 | 估算 |
|---|---|---|
| 一期 P0 | 字典+DDL、客户表单分支、手机号唯一校验与脱敏展示、现结拦截、数据迁移、回归 | 5~7 人天 |
| 二期 P1 | 经销商档案+层级（含防环）、供货价取价链（含多政策冲突规则）、返利打通（含冲抵台账 mk_rebate_offset+审批流）、信用校验、流向登记（含防重）+追溯接入、经销商列表页 | 15~18 人天（V2.0 补充冲抵台账表设计与防环/防重约束后调整；财务凭证接口对接 fin 启用后另计 2~3 人天） |
| 三期 P2 | 看板维度拆分、经销商专项报表、AI 预警扩展 | 6~8 人天 |
| 联调测试 | 全链路回归（订单→发货→流向→追溯）、权限回归 | 3~4 人天 |

建议节奏：P0 上线观察 1~2 周 → P1 分两批（档案+价格先行，返利+流向随后）→ P2 随月度迭代。

### 9.1 上线前检查清单（每个里程碑上线前逐项打勾）

- [ ] 升级 SQL 已在测试库完整演练（含回滚语句验证），备份生产库后方可执行
- [ ] 存量数据迁移后抽查：客户总数、类型分布、原订单/合同/价格政策数据比对无差异
- [ ] 第七章 7.2 清单中的存量改动全部经用户确认，回归清单全部通过（取价链硬标准：企业客户取价结果不变）
- [ ] 新菜单/按钮权限已按角色分配并实测（个人客户导出、客户类型变更、流向登记等）
- [ ] 消息中心推送通道实测（返利冲抵财务待办）
- [ ] 召回演练一次：选 1 个真实成品批次，走"发货单→经销商→流向"逆向追溯出清单

## 十、风险与开放问题

| # | 风险/问题 | 等级 | 应对 | 待确认方 |
|---|---|---|---|---|
| 1 | 手机号明文存储存在合规风险（个保法要求个人信息采取加密等措施） | 中 | 管理层已决策本期不做加密；以**后端接口层统一脱敏（V2.0）**+导出权限控制兜底，后续升级加密时 DDL 预留扩展空间 | 已决策关闭 |
| 2 | 经销商流向登记依赖经销商配合，人工登记易断 | 中 | 按发货单一键生成草稿降低录入成本；销售员月度核查 | 【非阻塞】销售经理 |
| 3 | "返利冲抵货款"财务口径 | 中 | **已决策关闭（2026-10-29）**：按"销售折扣（销售折让）"处理、红票由财务开；冲抵走营销侧台账闭环，预留凭证接口；冲抵审批为一级审批（财务审核），暂不设金额分级 | 已决策关闭 |
| 4 | 个人客户是否需要发票抬头（个人/单位代开） | 低 | 表单预留发票信息可选字段 | 【非阻塞】财务 |
| 5 | 传丰一线员工数字素养有限，经销商档案/流向录入界面需简洁 | 低 | 沿用现有大按钮/简化表单风格，必要字段最小化 | 【非阻塞】实施顾问 |
| 6 | 存量代码改动（取价链、下单校验、客户表单等）影响已上线功能 | 中 | 严格执行第七章变更管控：存量改动逐项经用户确认后方可实施，配套回归清单验证；取价链改动以"企业客户取价结果不变"为回归硬标准 | 已纳入开发约束 |
| 7 | ~~发货明细是否含批次号~~ **已核实关闭（CFSP 仓，2026-10-29）**：`mk_shipment_detail.batch_no` 字段存在（实体/Mapper/查询 SQL 三处确认），且 Mapper 已有 `food_batch` 批次主数据关联查询——P1-07"按发货单一键生成流向草稿"可直接取到批次号。**注意：此结论基于 CFSP 仓，标准仓动工前须重新核实同名表结构（标准仓可能无 `food_batch` 及批次字段）** | 已关闭（CFSP） | 批次号直接取自发货明细；标准仓无批次字段时流向批次号由分支仓扩展衔接 | 已核实关闭（标准仓待复核） |
| 8 | 【开放问题】个人客户是否纳入客户公海回收机制（销售员离职/长期未跟进的散客是否释放回公海） | 低 | P0 先默认个人客户不进公海、固定归属建档人；后续按业务需要再开 | 【非阻塞】销售经理 |
| 9 | 【开放问题】返利达成额归集口径（V2.1 补）：经销商期间采购额按订单/发货/回款哪个口径？退货与取消单是否扣减？含税还是不含税？ | 中 | 现有 `MkRebateServiceImpl.settle()` 已有归集逻辑（L142 起），P1 动工前与财务逐项确认口径并写入验收用例 | 【P1 动工前确认】财务 |
| 10 | 【开放问题】个人客户建档是否自动同步 `mk_contact` 联系人（V2.1 补） | 低 | 建议建档时以姓名+手机号自动创建一条联系人，避免客户详情页联系人空白；P0 实现时顺带确认 | 【非阻塞】销售经理 |

## 十一、里程碑

| 里程碑 | 交付物 | 依赖 |
|---|---|---|
| M1：方案评审通过 | 本方案定稿（V2.1） | 无——开放问题已全部关闭（手机号加密：管理层决策不做；返利口径：已确认销售折扣+营销侧闭环+冲抵一级审批不限额；技术评审修订已回写：后端脱敏/现结 settle_type 双节点拦截/冲抵台账与现有 redeem 改造/marketing:* 权限统一；仅剩风险 #9 返利归集口径待 P1 动工前与财务确认，不阻塞 P0） |
| M2：P0 上线 | 客户类型化 + 个人客户承接 | M1 |
| M3：P1 一批上线 | 经销商档案 + 层级 + 供货价 | M2 稳定运行 |
| M4：P1 二批上线 | 返利打通 + 信用校验 + 流向登记 + 追溯接入 | M3 |
| M5：P2 上线 | 看板与专项报表 | M4 |

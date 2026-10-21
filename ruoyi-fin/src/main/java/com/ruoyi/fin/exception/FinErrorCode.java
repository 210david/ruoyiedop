package com.ruoyi.fin.exception;

/**
 * 财务模块业务异常码。
 *
 * <p>【约束】异常抛出后统一交由 ruoyi-framework 的 GlobalExceptionHandler 捕获并转成
 * AjaxResult.error(msg)，业务代码不得自行 try-catch 吞掉财务异常。</p>
 *
 * <p>码段划分：10001 - 10999 为财务专用，避免与框架及其他业务模块冲突。</p>
 *
 * @author ruoyi
 */
public enum FinErrorCode
{
    /** 借贷不平衡 */
    UNBALANCED(10001, "借贷不平衡，差额 {0} 元"),

    /** 会计期间已结账 */
    PERIOD_CLOSED(10002, "会计期间 {0} 已结账，不允许修改"),

    /** 会计期间未初始化 */
    PERIOD_NOT_INIT(10003, "会计期间 {0} 未初始化，请先执行期间初始化"),

    /** 非明细科目不可录凭证 */
    SUBJECT_NOT_LEAF(10004, "科目 {0} 不是明细科目，不能直接录入凭证"),

    /** 已审核凭证不可修改 */
    VOUCHER_AUDITED(10005, "凭证 {0} 已审核，只能通过红字冲销修改"),

    /** 幂等重复：不算错误，返回已存在凭证 ID */
    VOUCHER_DUPLICATE(10006, "已存在对应凭证 {0}"),

    /** 成本台账数量不足 */
    COST_STOCK_NOT_ENOUGH(10007, "物料 {0} 库存成本数量不足，无法结转"),

    /** 未找到物料税率档案 */
    MATERIAL_TAX_NOT_FOUND(10008, "物料 {0} 未维护税率档案，请先在 fin_material_tax 中配置"),

    /** 单据已被下游引用，不可删除 */
    SOURCE_REFERENCED(10009, "单据 {0} 已被财务单据引用，不可删除"),

    /** 科目表为空 */
    SUBJECT_EMPTY(10010, "会计科目表为空，请先初始化科目"),

    /** 已记账凭证不允许反审核 */
    VOUCHER_POSTED(10011, "凭证 {0} 已记账，不允许反审核"),

    /** 期间解锁权限不足 */
    PERIOD_UNLOCK_FORBIDDEN(10012, "仅财务经理可解锁已结账期间"),

    /** 除税后偏差超阈值 */
    COST_TAX_DIFF_EXCEEDED(10013, "物料 {0} 不含税成本与发票金额偏差 {1} 元，已超过 1 元阈值"),

    /** 批量导入超量 */
    IMPORT_OVER_LIMIT(10014, "单次导入不能超过 {0} 行，请分批导入"),

    /** 银行账号格式不合法 */
    ACCOUNT_NO_INVALID(10015, "银行账号格式不合法：{0}，应为 6-32 位数字"),

    /** 科目编码重复 */
    SUBJECT_CODE_DUPLICATED(10016, "科目编码已存在：{0}"),

    /** 科目编码非数字 */
    SUBJECT_CODE_NOT_NUMERIC(10017, "科目编码必须为数字：{0}"),

    /** 科目层级超过 4 级 */
    SUBJECT_LEVEL_EXCEEDED(10018, "科目层级最多 4 级，无法在 {0} 下继续新增子科目"),

    /** 父科目不存在或已停用 */
    SUBJECT_PARENT_INVALID(10019, "上级科目不存在或已停用：{0}"),

    /** 存在子科目，不能删除 */
    SUBJECT_HAS_CHILDREN(10020, "科目「{0}」存在子科目，请先删除子科目"),

    /** 已有凭证分录，不能删除 */
    SUBJECT_HAS_VOUCHER(10021, "科目「{0}」已有凭证分录，不能删除；如不再使用请改为停用"),

    /** 停用层级节点时的级联影响提示 */
    SUBJECT_PARENT_SELF(10022, "上级科目不能是自己"),

    /** 凭证状态不允许当前操作（并发或状态已变更） */
    VOUCHER_STATUS_CONFLICT(10023, "凭证 {0} 当前状态为「{1}」，不允许执行此操作；可能已被他人处理"),

    /** 凭证无分录 */
    VOUCHER_NO_ENTRY(10024, "凭证必须至少包含一条分录"),

    /** 分录金额非法 */
    ENTRY_AMOUNT_INVALID(10025, "第 {0} 行分录金额必须大于 0"),

    /** 分录科目为空 */
    ENTRY_ACCOUNT_REQUIRED(10026, "第 {0} 行分录未选择会计科目"),

    /** 分录方向非法 */
    ENTRY_DIRECTION_INVALID(10027, "第 {0} 行分录方向非法，应为 DEBIT 或 CREDIT"),

    /** 制单人与审核人相同（职责分离） */
    VOUCHER_SELF_AUDIT(10028, "不允许审核自己录入的凭证（制单与审核须分离）"),

    /** 入库单价为零，无法计价 */
    INBOUND_PRICE_ZERO(10029, "入库单 {0} 的物料「{1}」入库单价为 0，无法计价；请先在 WMS 补录单价或设置默认成本价"),

    /** 出库数量超过结存 */
    COST_QTY_NOT_ENOUGH(10030, "物料「{0}」结存成本数量 {1}，不足出库数量 {2}"),

    /** 成本台账记录不存在 */
    COST_RECORD_NOT_FOUND(10031, "未找到物料「{0}」的成本台账记录，请先办理入库计价"),

    /** 出库单尚未完成 */
    OUTBOUND_NOT_FINISHED(10032, "出库单 {0} 状态为「{1}」，仅已完成（status=3）的单据才能成本结转"),

    /** 成本结转重复 */
    COST_ALREADY_CARRIED(10033, "单据 {0} 的成本已结转，重复请求已忽略"),

    /** 计价方法不支持 */
    COST_METHOD_UNSUPPORTED(10034, "不支持的计价方法：{0}，当前仅支持 WEIGHTED_AVG 与 FIFO"),

    /** 核销金额超未付余额 */
    AP_WRITEOFF_EXCEED(10035, "核销金额 {0} 超过未付余额 {1}"),

    /** 核销金额非法 */
    AP_WRITEOFF_INVALID(10036, "核销金额必须大于 0"),

    /** 应付单已核销，不可再操作 */
    AP_ALREADY_SETTLED(10037, "应付单「{0}」已核销完毕，不可重复核销"),

    /** 发票金额异常 */
    AP_AMOUNT_INVALID(10038, "发票「{0}」金额异常（价税合计 {1}，不含税 {2}，税额 {3}），无法生成应付单"),

    /** 应付单已存在 */
    AP_SOURCE_EXISTS(10039, "发票「{0}」已生成应付单「{1}」，请勿重复生成"),

    /** 账户余额不足 */
    ACCOUNT_BALANCE_NOT_ENOUGH(10040, "账户「{0}」余额 {1}，不足支付金额 {2}"),

    /** 账户编码重复 */
    ACCOUNT_CODE_DUPLICATED(10041, "账户编码已存在：{0}"),

    /** 账户已有流水不可删除 */
    ACCOUNT_HAS_FLOW(10042, "账户「{0}」已有资金流水，不能删除；如不再使用请改为停用"),

    /** 账户余额非零不可删除 */
    ACCOUNT_BALANCE_NOT_ZERO(10043, "账户「{0}」余额为 {1}，请先清零后再删除"),

    /** 转账账户相同 */
    TRANSFER_SAME_ACCOUNT(10044, "转出与转入账户不能相同"),

    /** 流水金额非法 */
    FLOW_AMOUNT_INVALID(10045, "流水金额必须大于 0"),

    /** 银行账户类型必须填账号 */
    ACCOUNT_NO_REQUIRED(10046, "银行账户必须填写银行账号"),

    /** 现金账户不应填账号 */
    ACCOUNT_NO_NOT_ALLOWED(10047, "现金与第三方支付账户无需填写银行账号"),

    /** 应收核销金额超未收余额 */
    AR_WRITEOFF_EXCEED(10048, "核销金额 {0} 超过未收余额 {1}"),

    /** 应收核销金额非法 */
    AR_WRITEOFF_INVALID(10049, "核销金额必须大于 0"),

    /** 应收单已核销 */
    AR_ALREADY_SETTLED(10050, "应收单「{0}」已核销完毕，不可重复核销"),

    /** 应收金额异常 */
    AR_AMOUNT_INVALID(10051, "发货单「{0}」金额为 {1}，无法生成应收单"),

    /** 应收单已存在 */
    AR_SOURCE_EXISTS(10052, "发货单「{0}」已生成应收单，请勿重复生成"),

    /** 销项发票号必填 */
    INVOICE_NO_REQUIRED(10053, "销项发票号码不能为空"),

    /** 凭证规则借贷不平 */
    RULE_UNBALANCED(10054, "凭证规则借贷不平：借 {0}，贷 {1}，差额 {2}。规则明细：{3}"),

    /** 期间已结账（结账模块） */
    PERIOD_ALREADY_CLOSED(10055, "会计期间「{0}」已结账，不允许新增或修改凭证；如需调整请先反结账"),

    /** 上一期间未结账 */
    PREV_PERIOD_NOT_CLOSED(10056, "上一期间「{0}」尚未结账，不允许结账「{1}」"),

    /** 期间已存在凭证 */
    PERIOD_HAS_VOUCHER(10057, "期间「{0}」存在未审核的凭证 {1} 张，请先处理后再结账"),

    /** 结账检查项未通过 */
    CLOSING_CHECK_FAILED(10058, "结账检查未通过：{0}"),

    /** 期间不存在 */
    PERIOD_NOT_FOUND(10059, "会计期间「{0}」不存在"),

    /** 凭证规则未配置 */
    RULE_NOT_FOUND(10060, "未找到来源类型「{0}」的凭证规则，无法生成凭证"),

    /** 规则科目非法 */
    RULE_ACCOUNT_INVALID(10061, "凭证规则引用的科目「{0}」不存在或非明细科目"),

    /** 应收单已作废 */
    AR_ALREADY_VOID(10063, "应收单「{0}」已作废，不可重复操作"),

    /** 应付单已作废 */
    AP_ALREADY_VOID(10064, "应付单「{0}」已作废，不可重复操作"),

    /** 规则分录方向与科目语义不符 */
    RULE_DIRECTION_INVALID(10062, "凭证规则「{0}」借贷方向配置错误：{1}");

    private final int code;
    private final String message;

    FinErrorCode(int code, String message)
    {
        this.code = code;
        this.message = message;
    }

    public int getCode()
    {
        return code;
    }

    public String getMessage()
    {
        return message;
    }

    /**
     * 格式化异常消息，占位符 {0} {1} 按顺序替换。
     *
     * @param args 替换参数
     * @return 完整消息
     */
    public String format(Object... args)
    {
        String msg = this.message;
        for (int i = 0; i < args.length; i++)
        {
            msg = msg.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return msg;
    }
}

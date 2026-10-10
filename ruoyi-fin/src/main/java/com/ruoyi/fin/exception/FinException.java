package com.ruoyi.fin.exception;

import com.ruoyi.common.exception.ServiceException;

/**
 * 财务业务异常工具类。
 *
 * <p>【P0-1 约束落地】框架的 ServiceException 是 final 类，不可继承，
 * 因此本类提供静态工厂方法，统一产出携带财务错误码的 ServiceException，
 * 由 GlobalExceptionHandler 统一捕获转成 AjaxResult.error(msg)。
 * 业务代码禁止自行 try-catch 吞掉财务异常（会掩盖事务回滚语义）。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 *   if (!Objects.equals(debitTotal, creditTotal)) {
 *       throw FinException.unbalanced(diff);
 *   }
 * </pre>
 *
 * @author ruoyi
 */
public final class FinException
{
    private FinException()
    {
    }

    /** 借贷不平衡 */
    public static ServiceException unbalanced(Object diff)
    {
        return build(FinErrorCode.UNBALANCED, diff);
    }

    /** 会计期间已结账 */
    public static ServiceException periodClosed(String period)
    {
        return build(FinErrorCode.PERIOD_CLOSED, period);
    }

    /** 会计期间未初始化 */
    public static ServiceException periodNotInit(String period)
    {
        return build(FinErrorCode.PERIOD_NOT_INIT, period);
    }

    /** 非明细科目不可录凭证 */
    public static ServiceException subjectNotLeaf(String subjectCode)
    {
        return build(FinErrorCode.SUBJECT_NOT_LEAF, subjectCode);
    }

    /** 已审核凭证不可修改 */
    public static ServiceException voucherAudited(String voucherNo)
    {
        return build(FinErrorCode.VOUCHER_AUDITED, voucherNo);
    }

    /** 已记账凭证不可反审核 */
    public static ServiceException voucherPosted(String voucherNo)
    {
        return build(FinErrorCode.VOUCHER_POSTED, voucherNo);
    }

    /** 成本台账数量不足 */
    public static ServiceException stockNotEnough(String materialCode)
    {
        return build(FinErrorCode.COST_STOCK_NOT_ENOUGH, materialCode);
    }

    /** 物料税率档案缺失 */
    public static ServiceException materialTaxNotFound(String materialCode)
    {
        return build(FinErrorCode.MATERIAL_TAX_NOT_FOUND, materialCode);
    }

    /** 来源单据已被下游引用 */
    public static ServiceException sourceReferenced(String sourceKey)
    {
        return build(FinErrorCode.SOURCE_REFERENCED, sourceKey);
    }

    /** 科目表为空 */
    public static ServiceException subjectEmpty()
    {
        return build(FinErrorCode.SUBJECT_EMPTY);
    }

    /** 期间解锁权限不足 */
    public static ServiceException periodUnlockForbidden()
    {
        return build(FinErrorCode.PERIOD_UNLOCK_FORBIDDEN);
    }

    /** 除税偏差超阈值 */
    public static ServiceException costTaxDiffExceeded(String materialCode, Object diff)
    {
        return build(FinErrorCode.COST_TAX_DIFF_EXCEEDED, materialCode, diff);
    }

    /** 批量导入超量 */
    public static ServiceException importOverLimit(int limit)
    {
        return build(FinErrorCode.IMPORT_OVER_LIMIT, limit);
    }

    /** 银行账号格式不合法 */
    public static ServiceException accountNoInvalid(String accountNo)
    {
        return build(FinErrorCode.ACCOUNT_NO_INVALID, accountNo);
    }

    /** 科目编码重复 */
    public static ServiceException subjectCodeDuplicated(String code)
    {
        return build(FinErrorCode.SUBJECT_CODE_DUPLICATED, code);
    }

    /** 科目编码非数字 */
    public static ServiceException subjectCodeNotNumeric(String code)
    {
        return build(FinErrorCode.SUBJECT_CODE_NOT_NUMERIC, code);
    }

    /** 科目层级超限 */
    public static ServiceException subjectLevelExceeded(String parentCode)
    {
        return build(FinErrorCode.SUBJECT_LEVEL_EXCEEDED, parentCode);
    }

    /** 上级科目无效 */
    public static ServiceException subjectParentInvalid(String parentCode)
    {
        return build(FinErrorCode.SUBJECT_PARENT_INVALID, parentCode);
    }

    /** 存在子科目不能删 */
    public static ServiceException subjectHasChildren(String subjectName)
    {
        return build(FinErrorCode.SUBJECT_HAS_CHILDREN, subjectName);
    }

    /** 已有凭证分录不能删 */
    public static ServiceException subjectHasVoucher(String subjectName)
    {
        return build(FinErrorCode.SUBJECT_HAS_VOUCHER, subjectName);
    }

    /** 上级科目不能是自己 */
    public static ServiceException subjectParentSelf()
    {
        return build(FinErrorCode.SUBJECT_PARENT_SELF);
    }

    /** 凭证状态冲突 */
    public static ServiceException voucherStatusConflict(String voucherNo, String status)
    {
        return build(FinErrorCode.VOUCHER_STATUS_CONFLICT, voucherNo, status);
    }

    /** 凭证无分录 */
    public static ServiceException voucherNoEntry()
    {
        return build(FinErrorCode.VOUCHER_NO_ENTRY);
    }

    /** 分录金额非法 */
    public static ServiceException entryAmountInvalid(int lineNo)
    {
        return build(FinErrorCode.ENTRY_AMOUNT_INVALID, lineNo);
    }

    /** 分录科目为空 */
    public static ServiceException entryAccountRequired(int lineNo)
    {
        return build(FinErrorCode.ENTRY_ACCOUNT_REQUIRED, lineNo);
    }

    /** 分录方向非法 */
    public static ServiceException entryDirectionInvalid(int lineNo)
    {
        return build(FinErrorCode.ENTRY_DIRECTION_INVALID, lineNo);
    }

    /** 不允许自审 */
    public static ServiceException voucherSelfAudit()
    {
        return build(FinErrorCode.VOUCHER_SELF_AUDIT);
    }

    /** 入库单价为零 */
    public static ServiceException inboundPriceZero(String orderNo, String materialCode)
    {
        return build(FinErrorCode.INBOUND_PRICE_ZERO, orderNo, materialCode);
    }

    /** 出库数量超结存 */
    public static ServiceException costQtyNotEnough(String materialCode, String stock, String outQty)
    {
        return build(FinErrorCode.COST_QTY_NOT_ENOUGH, materialCode, stock, outQty);
    }

    /** 成本台账不存在 */
    public static ServiceException costRecordNotFound(String materialCode)
    {
        return build(FinErrorCode.COST_RECORD_NOT_FOUND, materialCode);
    }

    /** 出库单未完成 */
    public static ServiceException outboundNotFinished(String orderNo, String status)
    {
        return build(FinErrorCode.OUTBOUND_NOT_FINISHED, orderNo, status);
    }

    /** 计价方法不支持 */
    public static ServiceException costMethodUnsupported(String method)
    {
        return build(FinErrorCode.COST_METHOD_UNSUPPORTED, method);
    }

    /** 核销金额超余额 */
    public static ServiceException apWriteoffExceed(String writeoff, String unpaid)
    {
        return build(FinErrorCode.AP_WRITEOFF_EXCEED, writeoff, unpaid);
    }

    /** 核销金额非法 */
    public static ServiceException apWriteoffInvalid()
    {
        return build(FinErrorCode.AP_WRITEOFF_INVALID);
    }

    /** 应付单已核销 */
    public static ServiceException apAlreadySettled(String apNo)
    {
        return build(FinErrorCode.AP_ALREADY_SETTLED, apNo);
    }

    /** 发票金额异常 */
    public static ServiceException apAmountInvalid(String invoiceNo, String total, String excl, String tax)
    {
        return build(FinErrorCode.AP_AMOUNT_INVALID, invoiceNo, total, excl, tax);
    }

    /** 应付单已存在 */
    public static ServiceException apSourceExists(String invoiceNo, String apNo)
    {
        return build(FinErrorCode.AP_SOURCE_EXISTS, invoiceNo, apNo);
    }

    /** 账户余额不足 */
    public static ServiceException accountBalanceNotEnough(String accountName, String balance, String amount)
    {
        return build(FinErrorCode.ACCOUNT_BALANCE_NOT_ENOUGH, accountName, balance, amount);
    }

    /** 账户编码重复 */
    public static ServiceException accountCodeDuplicated(String code)
    {
        return build(FinErrorCode.ACCOUNT_CODE_DUPLICATED, code);
    }

    /** 账户有流水不可删 */
    public static ServiceException accountHasFlow(String accountName)
    {
        return build(FinErrorCode.ACCOUNT_HAS_FLOW, accountName);
    }

    /** 余额非零不可删 */
    public static ServiceException accountBalanceNotZero(String accountName, String balance)
    {
        return build(FinErrorCode.ACCOUNT_BALANCE_NOT_ZERO, accountName, balance);
    }

    /** 转出转入账户相同 */
    public static ServiceException transferSameAccount()
    {
        return build(FinErrorCode.TRANSFER_SAME_ACCOUNT);
    }

    /** 流水金额非法 */
    public static ServiceException flowAmountInvalid()
    {
        return build(FinErrorCode.FLOW_AMOUNT_INVALID);
    }

    /** 银行账户必填账号 */
    public static ServiceException accountNoRequired()
    {
        return build(FinErrorCode.ACCOUNT_NO_REQUIRED);
    }

    /** 现金账户不应填账号 */
    public static ServiceException accountNoNotAllowed()
    {
        return build(FinErrorCode.ACCOUNT_NO_NOT_ALLOWED);
    }

    /** 应收核销金额超余额 */
    public static ServiceException arWriteoffExceed(String writeoff, String unreceived)
    {
        return build(FinErrorCode.AR_WRITEOFF_EXCEED, writeoff, unreceived);
    }

    /** 应收核销金额非法 */
    public static ServiceException arWriteoffInvalid()
    {
        return build(FinErrorCode.AR_WRITEOFF_INVALID);
    }

    /** 应收单已核销 */
    public static ServiceException arAlreadySettled(String arNo)
    {
        return build(FinErrorCode.AR_ALREADY_SETTLED, arNo);
    }

    /** 应收金额异常 */
    public static ServiceException arAmountInvalid(String shipmentNo, String amount)
    {
        return build(FinErrorCode.AR_AMOUNT_INVALID, shipmentNo, amount);
    }

    /** 应收单已存在 */
    public static ServiceException arSourceExists(String shipmentNo)
    {
        return build(FinErrorCode.AR_SOURCE_EXISTS, shipmentNo);
    }

    /** 销项发票号必填 */
    public static ServiceException invoiceNoRequired()
    {
        return build(FinErrorCode.INVOICE_NO_REQUIRED);
    }

    /** 凭证规则借贷不平 */
    public static ServiceException ruleUnbalanced(String debit, String credit, String diff, String detail)
    {
        return build(FinErrorCode.RULE_UNBALANCED, debit, credit, diff, detail);
    }

    /** 上期未结账 */
    public static ServiceException prevPeriodNotClosed(String prev, String current)
    {
        return build(FinErrorCode.PREV_PERIOD_NOT_CLOSED, prev, current);
    }

    /** 期间存在未审核凭证 */
    public static ServiceException periodHasVoucher(String periodName, long cnt)
    {
        return build(FinErrorCode.PERIOD_HAS_VOUCHER, periodName, String.valueOf(cnt));
    }

    /** 结账检查未通过 */
    public static ServiceException closingCheckFailed(String detail)
    {
        return build(FinErrorCode.CLOSING_CHECK_FAILED, detail);
    }

    /** 期间不存在 */
    public static ServiceException periodNotFound(String period)
    {
        return build(FinErrorCode.PERIOD_NOT_FOUND, period);
    }

    /** 凭证规则未配置 */
    public static ServiceException ruleNotFound(String sourceType)
    {
        return build(FinErrorCode.RULE_NOT_FOUND, sourceType);
    }

    /** 规则科目非法 */
    public static ServiceException ruleAccountInvalid(String accountCode)
    {
        return build(FinErrorCode.RULE_ACCOUNT_INVALID, accountCode);
    }

    /** 应收单已作废 */
    public static ServiceException arAlreadyVoid(String arNo)
    {
        return build(FinErrorCode.AR_ALREADY_VOID, arNo);
    }

    /** 应付单已作废 */
    public static ServiceException apAlreadyVoid(String apNo)
    {
        return build(FinErrorCode.AP_ALREADY_VOID, apNo);
    }

    /** 规则分录方向与科目语义不符 */
    public static ServiceException ruleDirectionInvalid(String accountCode, String reason)
    {
        return build(FinErrorCode.RULE_DIRECTION_INVALID, accountCode, reason);
    }

    private static ServiceException build(FinErrorCode errorCode, Object... args)
    {
        return new ServiceException(errorCode.format(args), errorCode.getCode());
    }
}

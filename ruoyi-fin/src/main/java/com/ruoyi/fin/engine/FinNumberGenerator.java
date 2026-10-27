package com.ruoyi.fin.engine;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mk.service.IMkNumberRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 财务单据号生成封装。
 *
 * <p>【设计依据】unified-development-standards.md 2.6「编号规则（跨模块共享）」：
 * 所有模块编号统一使用 ruoyi-mk 的 IMkNumberRuleService.generateNumber(ruleCode)。</p>
 *
 * <p>【已获批】2026-10-08 项目负责人确认，允许财务模块调用 MK 编号规则服务。
 * 此为「调用既有 Service」而非修改 MK 代码，符合零侵入约束。</p>
 *
 * <p>【并发安全】MK 侧实现已用 SELECT ... FOR UPDATE 行锁 + @Transactional，
 * 同一 rule_code 的序号生成互斥，不会重号。本类不额外加锁。</p>
 *
 * <p>【rule_code 清单】见 sql/fin/fin_05_code_rule.sql：</p>
 * <ul>
 *   fin_voucher 记账凭证号 PV / fin_ar_order 应收单 AR / fin_ap_order 应付单 AP</li>
 *   fin_receipt 收款单 SK / fin_payment 付款单 FK / fin_cost_cf 成本结转 CB</li>
 *   fin_expense 报销单 BX / fin_partner 往来单位 WL</li>
 * </ul>
 *
 * @author ruoyi
 */
@Component
public class FinNumberGenerator
{
    private static final Logger log = LoggerFactory.getLogger(FinNumberGenerator.class);

    /** 记账凭证号规则 */
    public static final String RULE_VOUCHER = "fin_voucher";

    /** 应收单号规则 */
    public static final String RULE_AR_ORDER = "fin_ar_order";

    /** 应付单号规则 */
    public static final String RULE_AP_ORDER = "fin_ap_order";

    /** 收款单号规则 */
    public static final String RULE_RECEIPT = "fin_receipt";

    /** 付款单号规则 */
    public static final String RULE_PAYMENT = "fin_payment";

    /** 成本结转单号规则 */
    public static final String RULE_COST_CF = "fin_cost_cf";

    /** 报销单号规则（P4 费用报销启用） */
    public static final String RULE_EXPENSE = "fin_expense";

    /** 往来单位编号规则 */
    public static final String RULE_PARTNER = "fin_partner";

    /** 坏账计提单号规则（2026-10-09 新增） */
    public static final String RULE_BAD_DEBT = "fin_bad_debt";

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    /**
     * 生成财务单据号。
     *
     * @param ruleCode 规则编码，见本类常量
     * @return 单据号，如 PV2026100800001
     */
    public String generate(String ruleCode)
    {
        return generate(ruleCode, null);
    }

    /**
     * 生成财务单据号（带动态前缀参数）。
     *
     * <p>适用于将来需要按业务维度分段编号的场景，如凭证按凭证类型分前缀。</p>
     *
     * @param ruleCode 规则编码
     * @param params   动态参数，可为 null
     * @return 单据号
     */
    public String generate(String ruleCode, Map<String, String> params)
    {
        try
        {
            String no = (params == null || params.isEmpty())
                    ? mkNumberRuleService.generateNumber(ruleCode)
                    : mkNumberRuleService.generateNumber(ruleCode, params);
            if (log.isDebugEnabled())
            {
                log.debug("生成财务单据号完成：rule={} no={}", ruleCode, no);
            }
            return no;
        }
        catch (ServiceException e)
        {
            // 规则不存在或已停用时 MK 侧会抛 ServiceException，此处包装为财务异常便于前端定位
            throw new ServiceException("单据号生成失败（规则 " + ruleCode + "）：" + e.getMessage(), e.getCode());
        }
    }

    /**
     * 预览下一个单据号，不占用序号。
     *
     * @param ruleCode 规则编码
     * @return 预览号，如 PV2026100800002
     */
    public String preview(String ruleCode)
    {
        return mkNumberRuleService.previewNumber(
                mkNumberRuleService.selectRuleByCode(ruleCode));
    }

    /**
     * 校验规则是否就绪。
     *
     * <p>上线自检用，避免运行期才发现规则缺失。</p>
     *
     * @param ruleCode 规则编码
     * @return true=已就绪且启用
     */
    public boolean isReady(String ruleCode)
    {
        try
        {
            return mkNumberRuleService.selectRuleByCode(ruleCode) != null;
        }
        catch (Exception e)
        {
            log.warn("编号规则 {} 查询失败", ruleCode, e);
            return false;
        }
    }
}
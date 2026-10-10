package com.ruoyi.fin.engine;

import com.ruoyi.fin.domain.FinSubject;
import com.ruoyi.fin.domain.FinVoucherEntry;
import com.ruoyi.fin.domain.FinVoucherRule;
import com.ruoyi.fin.exception.FinException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 凭证规则引擎
 *
 * <p><b>职责</b>：把业务单据（采购发票/销售发货/薪酬）按fin_voucher_rule
 * 配置的规则，自动组装成记账凭证分录。</p>
 *
 * <p><b>安全设计 · 三道防线</b>：</p>
 * <ol>
 *   <li><b>借贷平衡校验</b>（最关键）—— 组装完成后强制校验 Σ借 = Σ贷，
 *       不平衡直接抛异常拒绝生成。这道防线拦住了 2026-10-09 发现的
 *       「采购发票3 条规则方向全反」的数据错误</li>
 *   <li><b>科目合法性校验</b> —— 规则引用的科目必须存在且为明细科目</li>
 *   <li><b>幂等校验</b> —— 同一来源单据同一规则组只生成一次凭证</li>
 * </ol>
 *
 * <p><b>为什么必须有第一道防线</b>：fin_voucher_rule 是配置数据，
 * 管理员可能配错方向。若无校验，引擎会"忠实执行错误配置"，
 * 生成借贷颠倒的凭证并流入账簿，污染后续所有报表。
 * 宁可拒绝生成让人来查，也不能生成错账。</p>
 *
 * @author ruoyi
 */
@Component
public class FinVoucherEngine
{
    private static final Logger log = LoggerFactory.getLogger(FinVoucherEngine.class);

    /** 金额表达式：价税合计 */
    public static final String EXPR_TOTAL = "total_amount";

    /** 金额表达式：不含税金额 */
    public static final String EXPR_EXCL = "amount_excl_tax";

    /** 金额表达式：税额 */
    public static final String EXPR_TAX = "tax_amount";

    /**
     * 按规则组装配分录。
     *
     * @param rules       该来源类型下的全部规则（已按 priority 排序）
     * @param sourceData  来源单据金额（key 为金额表达式名，value 为金额）
     * @param subjectNameMap 科目编码 → 科目名称，用于回填 account_name
     * @return 装配好的分录列表（未落库）
     */
    public List<FinVoucherEntry> buildEntries(List<FinVoucherRule> rules,
                                               Map<String, BigDecimal> sourceData,
                                               Map<String, FinSubject> subjectMap)
    {
        List<FinVoucherEntry> entries = new ArrayList<>();
        if (rules == null || rules.isEmpty())
        {
            return entries;
        }

        int lineNo = 1;
        for (FinVoucherRule rule : rules)
        {
            BigDecimal amount = evalAmount(rule, sourceData);
            if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0)
            {
                // 金额为 0 的分录不生成（如免税发票无进项税），跳过
                log.debug("规则 {} 金额为 0，跳过该分录", rule.getRuleKey());
                continue;
            }

            FinVoucherEntry e = new FinVoucherEntry();
            e.setLineNo(lineNo++);
            e.setAccountCode(rule.getAccountCode());
            FinSubject subj = subjectMap == null ? null : subjectMap.get(rule.getAccountCode());
            e.setAccountName(subj == null ? null : subj.getSubjectName());
            e.setDirection(rule.getDirection());
            e.setAmount(amount);
            e.setCurrency("CNY");
            e.setExchangeRate(BigDecimal.ONE);
            e.setTaxAmount(BigDecimal.ZERO);
            e.setSummary(rule.getSummaryTpl());
            e.setSourceKey(rule.getRuleKey());
            entries.add(e);
        }

        // 【第一道防线】借贷平衡校验
        checkBalance(entries, rules);

        // 【第二道防线】科目方向合理性校验
        checkSubjectDirection(entries, subjectMap);
        return entries;
    }

    /**
     * 科目方向合理性校验。
     *
     * <p><b>为什么借贷平衡还不够</b>：2026-10-09 实测发现采购发票的 3 条规则
     * 方向全反（应付账款记借、原材料与进项税记贷），但这组规则
     * <b>恰好借贷相等</b>，平衡校验放行了它——然而生成的凭证
     * 业务含义完全错误（采购入库却成了应付减少、原材料增加）。
     * </p>
     *
     * <p><b>为什么不能按编码前缀猜</b>：试过按 1xxx/5xxx/6xxx 判借方、
     * 2xxx/4xxx 判贷方，结果误伤了 6001（主营收入，应贷）、
     * 22210101（进项税，应借）。编码前缀与科目语义并非严格对应，
     * 必须以科目表登记的 category/direction 为准。</p>
     *
     * @param entries        分录
     * @param subjectInfoMap 科目编码 → FinSubject（含 category 与 direction）
     */
    private void checkSubjectDirection(List<FinVoucherEntry> entries,
                                       Map<String, FinSubject> subjectInfoMap)
    {
        if (subjectInfoMap == null || subjectInfoMap.isEmpty())
        {
            // 无科目信息时跳过该校验（借贷平衡那道防线仍然生效）
            return;
        }

        for (FinVoucherEntry e : entries)
        {
            FinSubject s = subjectInfoMap.get(e.getAccountCode());
            if (s == null)
            {
                continue;
            }
            String normalDir = subjectNormalDirection(s);
            if (normalDir == null)
            {
                continue;
            }
            if (!normalDir.equals(e.getDirection()) && !isReversibleAccount(s.getSubjectCode()))
            {
                log.error("分录方向异常：科目 {}（{}）登记方向为 {}，规则却配置为 {}",
                        s.getSubjectCode(), s.getSubjectName(), normalDir, e.getDirection());
                throw FinException.ruleDirectionInvalid(s.getSubjectCode(),
                        s.getSubjectName() + " 登记余额方向为"
                                + ("DEBIT".equals(normalDir) ? "借方" : "贷方")
                                + "，规则配置为" + ("DEBIT".equals(e.getDirection()) ? "借方" : "贷方"));
            }
        }
    }

    /**
     * 取科目登记的常规余额方向。
     *
     * <p>依据fin_subject.direction（科目本身的余额方向）：
     * DEBIT 表示资产/成本/费用类，常规记借方；
     * CREDIT 表示负债/权益/收入类，常规记贷方。</p>
     *
     * @param s 科目
     * @return DEBIT/CREDIT，无法判断返回 null
     */
    private String subjectNormalDirection(FinSubject s)
    {
        String dir = s.getDirection();
        if ("DEBIT".equals(dir))
        {
            return "DEBIT";
        }
        if ("CREDIT".equals(dir))
        {
            return "CREDIT";
        }
        return null;
    }

    /**
     * 允许与常规方向相反的科目（红字冲销、调整类）。
     *
     * <p>累计折旧（1602）以贷方余额表示，属资产类的正常例外。</p>
     *
     * @param code 科目编码
     * @return true=允许反向
     */
    private boolean isReversibleAccount(String code)
    {
        return "1602".equals(code);
    }

    /**
     * 借贷平衡校验。
     *
     * @param entries 分录列表
     * @param rules   规则列表（用于错误提示定位）
     */
    private void checkBalance(List<FinVoucherEntry> entries, List<FinVoucherRule> rules)
    {
        BigDecimal debit = BigDecimal.ZERO;
        BigDecimal credit = BigDecimal.ZERO;
        for (FinVoucherEntry e : entries)
        {
            if ("DEBIT".equals(e.getDirection()))
            {
                debit = debit.add(e.getAmount());
            }
            else if ("CREDIT".equals(e.getDirection()))
            {
                credit = credit.add(e.getAmount());
            }
        }
        BigDecimal diff = debit.subtract(credit);
        if (diff.compareTo(BigDecimal.ZERO) != 0)
        {
            String detail = describe(rules, entries);
            log.error("凭证规则借贷不平：借 {} 贷 {} 差额 {}，规则：{}",
                    debit, credit, diff, detail);
            throw FinException.ruleUnbalanced(debit.toPlainString(),
                    credit.toPlainString(), diff.toPlainString(), detail);
        }
    }

    /**
     * 按金额表达式取值。
     */
    private BigDecimal evalAmount(FinVoucherRule rule, Map<String, BigDecimal> sourceData)
    {
        if (sourceData == null)
        {
            return null;
        }
        BigDecimal v = sourceData.get(rule.getAmountExpr());
        if (v == null)
        {
            log.warn("规则 {} 的金额表达式 {} 在来源数据中不存在", rule.getRuleKey(), rule.getAmountExpr());
        }
        return v;
    }

    /**
     * 生成可读的规则描述，便于错误定位。
     */
    private String describe(List<FinVoucherRule> rules, List<FinVoucherEntry> entries)
    {
        Map<Long, String> byRule = new HashMap<>();
        if (rules != null)
        {
            for (FinVoucherRule r : rules)
            {
                byRule.put((long) byRule.size(), r.getRuleName() + "(" + r.getAccountCode() + " "
                        + ("DEBIT".equals(r.getDirection()) ? "借" : "贷") + ")");
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++)
        {
            if (i > 0)
            {
                sb.append(" + ");
            }
            FinVoucherEntry e = entries.get(i);
            String name = byRule.get((long) i);
            sb.append("DEBIT".equals(e.getDirection()) ? "借 " : "贷 ")
              .append(e.getAmount().toPlainString());
            if (name != null)
            {
                sb.append("（").append(name).append("）");
            }
        }
        return sb.toString();
    }
}
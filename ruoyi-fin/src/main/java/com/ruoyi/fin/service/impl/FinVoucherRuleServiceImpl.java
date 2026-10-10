package com.ruoyi.fin.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fin.domain.FinSubject;
import com.ruoyi.fin.domain.FinVoucher;
import com.ruoyi.fin.domain.FinVoucherEntry;
import com.ruoyi.fin.domain.FinVoucherRule;
import com.ruoyi.fin.domain.vo.ApInvoiceRefVO;
import com.ruoyi.fin.domain.vo.ArShipmentRefVO;
import com.ruoyi.fin.engine.FinNumberGenerator;
import com.ruoyi.fin.engine.FinVoucherEngine;
import com.ruoyi.fin.exception.FinException;
import com.ruoyi.fin.mapper.FinSubjectMapper;
import com.ruoyi.fin.mapper.FinVoucherMapper;
import com.ruoyi.fin.mapper.FinRefMapper;
import com.ruoyi.fin.mapper.FinVoucherRuleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 凭证规则服务实现
 *
 * <p><b>自动凭证生成流程</b>：</p>
 * <pre>
 *   来源单据（pms_invoice / mk_shipment）
 *      ↓ 取金额（total_amount / amount_excl_tax / tax_amount）
 *   规则组（fin_voucher_rule 按 source_type 取，按 priority 排序）
 *      ↓ 引擎组装 + 借贷平衡校验（FinVoucherEngine）
 *   记账凭证（status=DRAFT 草稿，须人工审核）
 * </pre>
 *
 * <p><b>为什么生成的是草稿而非已审核</b>：自动凭证同样要走
 * 制单≠审核的内控流程——系统代替制单，但审核必须由人来做。</p>
 *
 * @author ruoyi
 */
@Service
public class FinVoucherRuleServiceImpl
{
    private static final Logger log = LoggerFactory.getLogger(FinVoucherRuleServiceImpl.class);

    @Autowired
    private FinVoucherRuleMapper ruleMapper;

    @Autowired
    private FinVoucherMapper voucherMapper;

    @Autowired
    private FinSubjectMapper subjectMapper;

    @Autowired
    private FinVoucherEngine engine;

    @Autowired
    private FinRefMapper refMapper;

    @Autowired
    private FinNumberGenerator numberGenerator;

    /**
     * 查询全部规则（规则维护页）
     */
    public List<FinVoucherRule> selectAllRules()
    {
        return ruleMapper.selectAllRules();
    }

    /**
     * 更新规则
     */
    public int updateRule(FinVoucherRule rule)
    {
        // 科目合法性校验：规则引用不存在的科目会导致运行期失败
        if (rule.getAccountCode() != null)
        {
            FinSubject s = subjectMapper.selectSubjectByCode(rule.getAccountCode());
            if (s == null)
            {
                throw FinException.ruleAccountInvalid(rule.getAccountCode());
            }
        }
        rule.setUpdateBy(SecurityUtils.getUsername());
        return ruleMapper.updateRule(rule);
    }

    /**
     * 由业务单据自动生成凭证。
     *
     * @param sourceModule 来源模块 pms/mk
     * @param sourceType   来源类型 invoice/shipment
     * @param sourceId     来源单据ID
     * @return 生成的凭证ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long generateVoucher(String sourceModule, String sourceType, Long sourceId)
    {
        Map<String, Object> src = ruleMapper.selectSourceAmount(sourceModule, sourceId);
        if (src == null || src.get("sourceId") == null)
        {
            throw FinException.ruleNotFound(sourceType + "/来源单据不存在，ID=" + sourceId);
        }

        List<FinVoucherRule> rules = ruleMapper.selectRulesBySourceType(sourceType);
        if (rules == null || rules.isEmpty())
        {
            throw FinException.ruleNotFound(sourceType);
        }

        // 构造引擎需要的金额映射
        Map<String, BigDecimal> amounts = new HashMap<>();
        amounts.put(FinVoucherEngine.EXPR_TOTAL, num(src.get("total_amount")));
        amounts.put(FinVoucherEngine.EXPR_EXCL, num(src.get("amount_excl_tax")));
        amounts.put(FinVoucherEngine.EXPR_TAX, num(src.get("tax_amount")));

        // 科目信息映射（回填科目名 + 方向合理性校验）
        Map<String, FinSubject> subjectMap = new HashMap<>();
        for (FinVoucherRule r : rules)
        {
            FinSubject s = subjectMapper.selectSubjectByCode(r.getAccountCode());
            if (s != null)
            {
                subjectMap.put(r.getAccountCode(), s);
            }
        }

        // 引擎组装（含借贷平衡与科目方向双重校验）
        List<FinVoucherEntry> entries = engine.buildEntries(rules, amounts, subjectMap);
        if (entries.isEmpty())
        {
            throw FinException.ruleUnbalanced("0", "0", "0",
                    "规则组「" + sourceType + "」生成的金额全为 0，请检查来源单据金额");
        }

        // 组装凭证（草稿，须人工审核）
        String user = SecurityUtils.getUsername();
        String period = str(src.get("sourceKey")).isEmpty() ? null : periodOf(src.get("sourceDate"));
        BigDecimal totalDebit = BigDecimal.ZERO;
        for (FinVoucherEntry e : entries)
        {
            if (FinVoucherEntry.DIR_DEBIT.equals(e.getDirection()))
            {
                totalDebit = totalDebit.add(e.getAmount());
            }
        }

        FinVoucher voucher = new FinVoucher();
        voucher.setVoucherNo(numberGenerator.generate(FinNumberGenerator.RULE_VOUCHER));
        voucher.setVoucherDate(dateOf(src.get("sourceDate")));
        voucher.setPeriod(period);
        voucher.setVoucherType(FinVoucher.TYPE_MANUAL);
        voucher.setSourceModule(sourceModule);
        voucher.setSourceType(sourceType);
        voucher.setSourceId(sourceId);
        voucher.setSourceKey(str(src.get("sourceKey")));
        voucher.setRuleKey(sourceType);
        voucher.setEntryCount(entries.size());
        voucher.setStatus(FinVoucher.STATUS_DRAFT);   // 须人工审核
        voucher.setAttachCount(0);
        voucher.setTotalAmount(totalDebit);
        voucher.setUserId(SecurityUtils.getUserId());
        voucher.setCreateBy(user);
        voucher.setRemark("由" + sourceType + " " + src.get("sourceKey") + " 自动生成");

        try
        {
            voucherMapper.insertVoucher(voucher);
        }
        catch (DuplicateKeyException e)
        {
            // 幂等：该单据已生成过凭证
            FinVoucher exist = voucherMapper.selectBySource(sourceModule, sourceType, sourceId, "");
            if (exist != null)
            {
                log.info("来源单据已生成凭证，返回已存在的凭证 {}", exist.getVoucherNo());
                return exist.getVoucherId();
            }
            throw e;
        }

        int lineNo = 1;
        for (FinVoucherEntry e : entries)
        {
            e.setVoucherId(voucher.getVoucherId());
            e.setLineNo(lineNo++);
            e.setPeriod(period);
            e.setCompanyId(0L);
            e.setCreateBy(user);
            e.setSourceKey(null);   // 运行时字段，不入库
        }
        voucherMapper.insertEntries(entries);

        log.info("自动凭证生成成功：{} → {}，{} 行分录，金额 {}",
                src.get("sourceKey"), voucher.getVoucherNo(), entries.size(), totalDebit);
        return voucher.getVoucherId();
    }

/**
     * 批量生成（扫描已审核业务单据）
 *
     * @param sourceModule 来源模块
     * @param sourceType   来源类型
     * @param limit        单批上限
     * @return 生成结果
     */
@Transactional(rollbackFor = Exception.class)
public Map<String, Object> generateBatch(String sourceModule, String sourceType, int limit)
    {
        int success = 0;
    int skip = 0;
        List<String> errors = new ArrayList<>();

        // 扫描已审核但未生成凭证的单据（复用 RefMapper，只读）
        List<Long> ids = new ArrayList<>();
      if ("invoice".equals(sourceType))
        {
    for (ApInvoiceRefVO inv : refMapper.selectUnmatchedInvoice(limit))
       {
            ids.add(inv.getInvoiceId());
 }
        }
        else if ("shipment".equals(sourceType))
        {
   for (ArShipmentRefVO sh : refMapper.selectUnmatchedShipment(limit))
   {
      ids.add(sh.getShipmentId());
  }
        }
        else
  {
     throw new IllegalArgumentException("暂不支持批量生成的来源类型：" + sourceType);
 }

      for (Long id : ids)
   {
         try
      {
  generateVoucher(sourceModule, sourceType, id);
         success++;
      }
            catch (Exception e)
            {
   skip++;
    errors.add("单据" + id + "：" + e.getMessage());
            }
 }

        Map<String, Object> r = new HashMap<>();
r.put("total", ids.size());
 r.put("success", success);
        r.put("skip", skip);
      r.put("errors", errors);
        return r;
    }

    /**
     * 规则引用的科目必须是存在的明细科目。
     */
    private void validateSubject(FinVoucherRule rule)
    {
        FinSubject s = subjectMapper.selectSubjectByCode(rule.getAccountCode());
        if (s == null)
        {
            throw FinException.ruleAccountInvalid(rule.getAccountCode());
        }
        if (!"1".equals(s.getIsLeaf()))
        {
            throw FinException.ruleAccountInvalid(rule.getAccountCode() + "（非明细科目）");
        }
    }

    // ==================== 私有方法 ====================

    private String periodOf(Object sourceDate)
    {
        if (sourceDate == null)
        {
            return java.time.LocalDate.now().toString().replace("-", "").substring(0, 6);
        }
        java.util.Date d = (java.util.Date) sourceDate;
        java.time.LocalDate ld = d.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return String.format("%04d%02d", ld.getYear(), ld.getMonthValue());
    }

    private java.util.Date dateOf(Object sourceDate)
    {
        if (sourceDate == null)
        {
            return new java.util.Date();
        }
        return (java.util.Date) sourceDate;
    }

    private static BigDecimal num(Object o)
    {
        if (o == null)
        {
            return BigDecimal.ZERO;
        }
        if (o instanceof BigDecimal)
        {
            return (BigDecimal) o;
        }
        return new BigDecimal(o.toString());
    }

    private static String str(Object o)
    {
        return o == null ? "" : o.toString();
    }
}

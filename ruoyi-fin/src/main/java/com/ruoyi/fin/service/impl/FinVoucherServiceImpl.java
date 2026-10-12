package com.ruoyi.fin.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fin.domain.FinSubject;
import com.ruoyi.fin.domain.FinVoucher;
import com.ruoyi.fin.domain.FinVoucherEntry;
import com.ruoyi.fin.domain.dto.VoucherDTO;
import com.ruoyi.fin.engine.FinNumberGenerator;
import com.ruoyi.fin.engine.FinPeriodGuard;
import com.ruoyi.fin.exception.FinException;
import com.ruoyi.fin.mapper.FinSubjectMapper;
import com.ruoyi.fin.mapper.FinVoucherMapper;
import com.ruoyi.fin.service.IFinVoucherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 记账凭证 Service 实现
 *
 * <p>【P0-5 借贷平衡】所有写操作前强制校验 Σ借 = Σ贷，不平直接抛异常回滚。</p>
 *
 * <p>【P0-4 期间】period 由 voucherDate 派生；已结账期间由 FinPeriodGuard 拦截。</p>
 *
 * <p>【职责分离】审核时校验 create_by != audit_by，代码层面兜底，
 * 不依赖角色配置（角色可被误配，代码不可）。</p>
 *
 * @author ruoyi
 */
@Service
public class FinVoucherServiceImpl implements IFinVoucherService
{
    private static final Logger log = LoggerFactory.getLogger(FinVoucherServiceImpl.class);

    @Autowired
    private FinVoucherMapper voucherMapper;

    @Autowired
    private FinSubjectMapper subjectMapper;

    @Autowired
    private FinPeriodGuard periodGuard;

    /** 已结账期间拦截（结账流程） */
    @Autowired
    private FinClosingServiceImpl closingService;

    @Autowired
    private FinNumberGenerator numberGenerator;

    @Override
    public List<FinVoucher> selectVoucherList(FinVoucher voucher)
    {
        return voucherMapper.selectVoucherList(voucher);
    }

    @Override
    public VoucherDTO selectVoucherDetail(Long voucherId)
    {
        FinVoucher voucher = voucherMapper.selectVoucherById(voucherId);
        if (voucher == null)
        {
            return null;
        }
        VoucherDTO dto = new VoucherDTO();
        dto.setVoucher(voucher);
        List<FinVoucherEntry> entries = voucherMapper.selectEntriesByVoucherId(voucherId);
        dto.setEntries(entries);
        fillBalance(dto, entries);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertVoucher(VoucherDTO dto)
    {
        FinVoucher voucher = dto.getVoucher();
        List<FinVoucherEntry> entries = dto.getEntries();

        // 1. 期间校验（已结账则拒绝）+ 科目表已初始化校验
        String period = derivePeriod(voucher);
        closingService.checkPeriodWritable(period);
        periodGuard.checkWritable(period);
        periodGuard.checkSubjectReady();

        // 2. 借贷平衡校验
        fillBalance(dto, entries);
        if (!dto.isBalanced())
        {
            throw FinException.unbalanced(dto.getDiff().toPlainString());
        }

        // 3. 分录合法性校验并补全字段
        prepareEntries(entries, period);

        // 4. 填充主表字段
        voucher.setPeriod(period);
        voucher.setVoucherNo(numberGenerator.generate(FinNumberGenerator.RULE_VOUCHER));
        voucher.setVoucherType(FinVoucher.TYPE_MANUAL);
        voucher.setSourceModule("manual");
        voucher.setSourceType("");
        voucher.setSourceId(0L);
        voucher.setSourceKey(null);
        // 手工凭证 rule_key 用凭证号占位，保证 uk_source(source_module,source_type,source_id,rule_key,del_flag)
        // 对每张手工凭证唯一；固定空串会导致第二张手工凭证必撞唯一索引（2026-10-10 修复）
        voucher.setRuleKey(voucher.getVoucherNo());
        voucher.setEntryCount(entries.size());
        voucher.setStatus(FinVoucher.STATUS_DRAFT);
        voucher.setAttachCount(0);
        voucher.setTotalAmount(dto.getDebitTotal());
        voucher.setUserId(SecurityUtils.getUserId());
        voucher.setCreateBy(SecurityUtils.getUsername());

        try
        {
            voucherMapper.insertVoucher(voucher);
        }
        catch (DuplicateKeyException e)
        {
            // 凭证号唯一索引冲突（极端并发下编号服务未及时刷新），给出明确提示
            log.error("凭证号冲突：{}", voucher.getVoucherNo(), e);
            throw FinException.voucherStatusConflict(voucher.getVoucherNo(), "编号冲突");
        }

        // 5. 回填分录的凭证ID与行号后批量插入
        int lineNo = 1;
        for (FinVoucherEntry e : entries)
        {
            e.setVoucherId(voucher.getVoucherId());
            e.setLineNo(lineNo++);
        }
        voucherMapper.insertEntries(entries);

        return voucher.getVoucherId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateVoucher(VoucherDTO dto)
    {
        FinVoucher exist = voucherMapper.selectVoucherById(dto.getVoucher().getVoucherId());
        if (exist == null)
        {
            throw new IllegalArgumentException("凭证不存在或已删除");
        }
        // 规则：已审核凭证只能红冲，不能直接改
        if (!FinVoucher.STATUS_DRAFT.equals(exist.getStatus()))
        {
            throw FinException.voucherAudited(exist.getVoucherNo());
        }

        String period = derivePeriod(dto.getVoucher());
        // 【结账约束】已结账期间不允许修改凭证
        closingService.checkPeriodWritable(period);
        periodGuard.checkWritable(period);

        fillBalance(dto, dto.getEntries());
        if (!dto.isBalanced())
        {
            throw FinException.unbalanced(dto.getDiff().toPlainString());
        }

        // 重建分录：先逻辑删除旧行，再插入新行
        voucherMapper.deleteEntriesByVoucherId(exist.getVoucherId());
        prepareEntries(dto.getEntries(), period);
        int lineNo = 1;
        for (FinVoucherEntry e : dto.getEntries())
        {
            e.setVoucherId(exist.getVoucherId());
            e.setLineNo(lineNo++);
        }
        voucherMapper.insertEntries(dto.getEntries());

        exist.setPeriod(period);
        exist.setVoucherDate(dto.getVoucher().getVoucherDate());
        exist.setTotalAmount(dto.getDebitTotal());
        exist.setEntryCount(dto.getEntries().size());
        exist.setRemark(dto.getVoucher().getRemark());
        exist.setUpdateBy(SecurityUtils.getUsername());
        // 状态不变更，仅更新期间/金额/分录数等
        int rows = voucherMapper.updateVoucher(exist);
        if (rows == 0)
        {
            // 并发下已被他人审核，前置条件拦截
            throw FinException.voucherStatusConflict(exist.getVoucherNo(), "已被处理");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteVoucherByIds(Long[] voucherIds)
    {
        if (voucherIds == null || voucherIds.length == 0)
        {
            return 0;
        }
        for (Long id : voucherIds)
        {
            FinVoucher v = voucherMapper.selectVoucherById(id);
            if (v == null)
            {
                continue;
            }
            // 只有草稿可删；已审核/已记账一律拒绝
            if (!FinVoucher.STATUS_DRAFT.equals(v.getStatus()))
            {
                throw FinException.voucherAudited(v.getVoucherNo());
            }
            voucherMapper.deleteEntriesByVoucherId(id);
        }
        return voucherMapper.deleteVoucherByIds(voucherIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditVoucher(Long voucherId, String auditOpinion)
    {
        String user = SecurityUtils.getUsername();
        // 行锁防并发：两个审核请求会串行化
     FinVoucher v = voucherMapper.selectVoucherForUpdate(voucherId);
        if (v == null)
        {
            throw new IllegalArgumentException("凭证不存在或已删除");
     }
        // 【结账约束】已结账期间不允许审核
        closingService.checkPeriodWritable(v.getPeriod());
        // 职责分离代码兜底：不允许自审（角色配置可能出错，这里再加一道）
        if (user != null && user.equals(v.getCreateBy()))
        {
            throw FinException.voucherSelfAudit();
        }
        int rows = voucherMapper.auditVoucher(voucherId, user, auditOpinion);
        if (rows == 0)
        {
            // 状态前置条件未命中，说明已被他人处理
            throw FinException.voucherStatusConflict(v.getVoucherNo(), statusLabel(v.getStatus()));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelAudit(Long voucherId)
    {
        FinVoucher v = voucherMapper.selectVoucherForUpdate(voucherId);
        if (v == null)
        {
            throw new IllegalArgumentException("凭证不存在或已删除");
        }
        int rows = voucherMapper.cancelAudit(voucherId, SecurityUtils.getUsername());
        if (rows == 0)
        {
            throw FinException.voucherStatusConflict(v.getVoucherNo(), statusLabel(v.getStatus()));
        }
        return rows;
    }

    @Override
    public Map<String, Object> trialBalance(String period)
    {
        List<Map<String, Object>> list = voucherMapper.selectTrialBalance(period);
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (Map<String, Object> row : list)
        {
            totalDebit = totalDebit.add(toDecimal(row.get("debitAmount")));
            totalCredit = totalCredit.add(toDecimal(row.get("creditAmount")));
        }
        BigDecimal diff = totalDebit.subtract(totalCredit);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("totalDebit", totalDebit);
        result.put("totalCredit", totalCredit);
        result.put("diff", diff);
        result.put("balanced", diff.compareTo(BigDecimal.ZERO) == 0);
        return result;
    }

    @Override
    public List<Map<String, Object>> subjectBalance(String period)
    {
        return voucherMapper.selectSubjectBalance(period);
    }

    // ==================== 私有方法 ====================

    /**
     * 由凭证日期派生会计期间（不接受前端传值）。
     */
    private String derivePeriod(FinVoucher voucher)
    {
        LocalDate date = voucher.getVoucherDate() == null
                ? LocalDate.now() : voucher.getVoucherDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return periodGuard.derivePeriod(date);
    }

    /**
     * 计算借贷合计并判断是否平衡。
     */
    private void fillBalance(VoucherDTO dto, List<FinVoucherEntry> entries)
    {
        BigDecimal debit = BigDecimal.ZERO;
        BigDecimal credit = BigDecimal.ZERO;
        if (entries != null)
        {
            for (FinVoucherEntry e : entries)
            {
                if (e.getAmount() == null)
                {
                    continue;
                }
                if (FinVoucherEntry.DIR_DEBIT.equals(e.getDirection()))
                {
                    debit = debit.add(e.getAmount());
                }
                else if (FinVoucherEntry.DIR_CREDIT.equals(e.getDirection()))
                {
                    credit = credit.add(e.getAmount());
                }
            }
        }
        dto.setDebitTotal(debit);
        dto.setCreditTotal(credit);
        BigDecimal diff = debit.subtract(credit);
        dto.setDiff(diff);
        dto.setBalanced(diff.compareTo(BigDecimal.ZERO) == 0);
    }

    /**
     * 分录合法性校验与字段补全。
     */
    private void prepareEntries(List<FinVoucherEntry> entries, String period)
    {
        if (entries == null || entries.isEmpty())
        {
            throw FinException.voucherNoEntry();
        }
        String user = SecurityUtils.getUsername();
        int i = 1;
        for (FinVoucherEntry e : entries)
        {
            if (e.getAccountCode() == null || e.getAccountCode().trim().isEmpty())
            {
                throw FinException.entryAccountRequired(i);
            }
            if (!FinVoucherEntry.DIR_DEBIT.equals(e.getDirection())
                    && !FinVoucherEntry.DIR_CREDIT.equals(e.getDirection()))
            {
                throw FinException.entryDirectionInvalid(i);
            }
            if (e.getAmount() == null || e.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw FinException.entryAmountInvalid(i);
            }
            // 科目须存在且为明细科目
            FinSubject subject = subjectMapper.selectSubjectByCode(e.getAccountCode());
            if (subject == null)
            {
                throw FinException.entryAccountRequired(i);
            }
            if (!"1".equals(subject.getIsLeaf()))
            {
                throw FinException.subjectNotLeaf(e.getAccountCode());
            }
            if ("1".equals(subject.getStatus()))
            {
                // 停用科目不允许新凭证
                throw FinException.voucherStatusConflict(e.getAccountCode(), "科目已停用");
            }

            e.setPeriod(period);
            e.setAccountName(subject.getSubjectName());
            e.setCreateBy(user);
            if (e.getCurrency() == null)
            {
                e.setCurrency("CNY");
            }
            if (e.getExchangeRate() == null)
            {
                e.setExchangeRate(BigDecimal.ONE);
            }
            if (e.getTaxAmount() == null)
            {
                e.setTaxAmount(BigDecimal.ZERO);
            }
            i++;
        }
    }

    private String statusLabel(String status)
    {
        if (FinVoucher.STATUS_DRAFT.equals(status)) return "草稿";
        if (FinVoucher.STATUS_AUDITED.equals(status)) return "已审核";
        if (FinVoucher.STATUS_POSTED.equals(status)) return "已记账";
        if (FinVoucher.STATUS_CANCELLED.equals(status)) return "已冲销";
        return status;
    }

    private BigDecimal toDecimal(Object o)
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

    /**
     * 红字冲销（D-01 修复）。
     *
     * <p>对已审核凭证生成一张金额取负的同向凭证（红字），原凭证不动。
     * 冲销凭证为草稿，须人工审核后生效——与制单≠审核内控一致。
     * 红字冲销在当前期间做（即使原凭证在已封期间），当前期间须未结账。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long redFlush(Long voucherId)
    {
        FinVoucher orig = voucherMapper.selectVoucherById(voucherId);
        if (orig == null)
        {
            throw new IllegalArgumentException("原凭证不存在或已删除");
        }
        if (!FinVoucher.STATUS_AUDITED.equals(orig.getStatus()))
        {
            throw new IllegalArgumentException("仅已审核凭证可红字冲销");
        }
        if ("carry_forward".equals(orig.getSourceType()))
        {
            throw new IllegalArgumentException("期末结转凭证不可红字冲销，如需调整请先反结账");
        }

        String user = SecurityUtils.getUsername();
        java.util.Date now = new java.util.Date();
        LocalDate today = LocalDate.now();
        String period = String.format("%04d%02d", today.getYear(), today.getMonthValue());
        // 冲销凭证落在当前期间，当前期间必须未结账
        closingService.checkPeriodWritable(period);

        java.util.List<FinVoucherEntry> origEntries = voucherMapper.selectEntriesByVoucherId(voucherId);
        if (origEntries == null || origEntries.isEmpty())
        {
            throw new IllegalArgumentException("原凭证无分录，无法冲销");
        }

        FinVoucher rv = new FinVoucher();
        rv.setVoucherNo(numberGenerator.generate(FinNumberGenerator.RULE_VOUCHER));
        rv.setVoucherDate(now);
        rv.setPeriod(period);
        rv.setVoucherType(FinVoucher.TYPE_MANUAL);
        rv.setSourceModule("fin");
        rv.setSourceType("redflush");
        rv.setSourceId(voucherId);
        rv.setSourceKey(orig.getVoucherNo());
        rv.setRuleKey("redflush");
        rv.setEntryCount(origEntries.size());
        rv.setStatus(FinVoucher.STATUS_DRAFT);
        rv.setAttachCount(0);
        rv.setTotalAmount(orig.getTotalAmount() == null ? java.math.BigDecimal.ZERO : orig.getTotalAmount().negate());
        rv.setUserId(SecurityUtils.getUserId());
        rv.setCreateBy(user);
        rv.setRemark("红字冲销 " + orig.getVoucherNo());

        java.util.List<FinVoucherEntry> entries = new java.util.ArrayList<>();
        java.math.BigDecimal debit = java.math.BigDecimal.ZERO;
        java.math.BigDecimal credit = java.math.BigDecimal.ZERO;
        int lineNo = 1;
        for (FinVoucherEntry e : origEntries)
        {
            FinVoucherEntry n = new FinVoucherEntry();
            n.setLineNo(lineNo++);
            n.setAccountCode(e.getAccountCode());
            n.setAccountName(e.getAccountName());
            n.setDirection(e.getDirection());
            n.setAmount(e.getAmount() == null ? java.math.BigDecimal.ZERO : e.getAmount().negate());
            n.setCurrency(e.getCurrency() == null ? "CNY" : e.getCurrency());
            n.setExchangeRate(java.math.BigDecimal.ONE);
            n.setTaxAmount(e.getTaxAmount() == null ? java.math.BigDecimal.ZERO : e.getTaxAmount().negate());
            n.setSummary("冲销 " + orig.getVoucherNo());
            entries.add(n);
            if (FinVoucherEntry.DIR_DEBIT.equals(n.getDirection()))
            {
                debit = debit.add(n.getAmount());
            }
            else
            {
                credit = credit.add(n.getAmount());
            }
        }
        // 红字也必须平衡（借负合计 = 贷负合计）
        if (debit.compareTo(credit) != 0)
        {
            throw FinException.ruleUnbalanced(debit.toPlainString(), credit.toPlainString(),
                    debit.subtract(credit).toPlainString(), "红字冲销 " + orig.getVoucherNo());
        }

        try
        {
            voucherMapper.insertVoucher(rv);
        }
        catch (DuplicateKeyException e)
        {
            throw new IllegalArgumentException("该凭证已被红字冲销，不可重复冲销");
        }

        for (FinVoucherEntry n : entries)
        {
            n.setVoucherId(rv.getVoucherId());
            n.setPeriod(period);
            n.setCompanyId(0L);
            n.setCreateBy(user);
        }
        voucherMapper.insertEntries(entries);

        log.info("红字冲销：{} → {}（{} 行分录，草稿待审核）", orig.getVoucherNo(), rv.getVoucherNo(), entries.size());
        return rv.getVoucherId();
    }
}

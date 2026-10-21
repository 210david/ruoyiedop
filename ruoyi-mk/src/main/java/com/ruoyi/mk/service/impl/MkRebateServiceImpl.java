package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.domain.MkRebatePolicy;
import com.ruoyi.mk.domain.MkRebateSettle;
import com.ruoyi.mk.mapper.MkCustomerMapper;
import com.ruoyi.mk.mapper.MkRebatePolicyMapper;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mk.service.IMkRebateService;

/**
 * 返利政策/结算 Service实现
 *
 * 核算逻辑：期间(yyyyMM)按周期类型展开统计区间（月=当月 / 季=当季 / 年=当年），
 * 统计已审核订单金额（状态2/3/4），命中启用政策阶梯生成结算单（同客户+期间+政策只生成一次）。
 *
 * @author ruoyi
 */
@Service
public class MkRebateServiceImpl implements IMkRebateService
{
    @Autowired
    private MkRebatePolicyMapper mkRebatePolicyMapper;

    @Autowired
    private MkCustomerMapper mkCustomerMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<Map<String, Object>> selectPolicyGroupList(MkRebatePolicy policy)
    {
        List<MkRebatePolicy> rows = mkRebatePolicyMapper.selectPolicyList(policy);
        // 按 policy_no 聚合为阶梯组
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (MkRebatePolicy row : rows)
        {
            Map<String, Object> group = grouped.get(row.getPolicyNo());
            if (group == null)
            {
                group = new HashMap<>();
                group.put("policyNo", row.getPolicyNo());
                group.put("policyName", row.getPolicyName());
                group.put("customerId", row.getCustomerId());
                group.put("customerName", row.getCustomerName());
                group.put("basis", row.getBasis());
                group.put("periodType", row.getPeriodType());
                group.put("startDate", row.getStartDate());
                group.put("endDate", row.getEndDate());
                group.put("status", row.getStatus());
                group.put("remark", row.getRemark());
                group.put("tiers", new ArrayList<MkRebatePolicy>());
                grouped.put(row.getPolicyNo(), group);
            }
            ((List<MkRebatePolicy>) group.get("tiers")).add(row);
        }
        return new ArrayList<>(grouped.values());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPolicyGroup(List<MkRebatePolicy> tiers)
    {
        validateTiers(tiers, null);
        String policyNo = mkNumberRuleService.generateNumber("mk_rebate_policy");
        String username = SecurityUtils.getUsername();
        for (MkRebatePolicy tier : tiers)
        {
            tier.setPolicyNo(policyNo);
            tier.setStatus(tier.getStatus() == null ? "0" : tier.getStatus());
            tier.setCreateBy(username);
            mkRebatePolicyMapper.insertPolicy(tier);
        }
        return tiers.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePolicyGroup(List<MkRebatePolicy> tiers)
    {
        if (tiers == null || tiers.isEmpty() || StringUtils.isEmpty(tiers.get(0).getPolicyNo()))
        {
            throw new ServiceException("政策编号不能为空");
        }
        String policyNo = tiers.get(0).getPolicyNo();
        validateTiers(tiers, policyNo);
        String username = SecurityUtils.getUsername();
        mkRebatePolicyMapper.deleteTiersByNo(policyNo);
        for (MkRebatePolicy tier : tiers)
        {
            tier.setPolicyNo(policyNo);
            tier.setStatus(tier.getStatus() == null ? "0" : tier.getStatus());
            tier.setCreateBy(username);
            mkRebatePolicyMapper.insertPolicy(tier);
        }
        return tiers.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePolicyGroup(String policyNo)
    {
        List<MkRebatePolicy> tiers = mkRebatePolicyMapper.selectTiersByNo(policyNo);
        if (tiers == null || tiers.isEmpty())
        {
            throw new ServiceException("返利政策不存在");
        }
        Long[] ids = new Long[tiers.size()];
        for (int i = 0; i < tiers.size(); i++)
        {
            ids[i] = tiers.get(i).getPolicyId();
        }
        return mkRebatePolicyMapper.deletePolicyByIds(ids);
    }

    @Override
    public List<MkRebateSettle> selectSettleList(MkRebateSettle settle)
    {
        return mkRebatePolicyMapper.selectSettleList(settle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MkRebateSettle> settle(Long customerId, String period)
    {
        if (customerId == null)
        {
            throw new ServiceException("客户不能为空");
        }
        if (period == null || !period.matches("\\d{6}"))
        {
            throw new ServiceException("核算期间格式必须为yyyyMM");
        }
        MkCustomer customer = mkCustomerMapper.selectCustomerById(customerId);
        if (customer == null)
        {
            throw new ServiceException("客户不存在");
        }
        List<MkRebateSettle> created = new ArrayList<>();
        // 该客户可用的启用政策（客户定向 或 全部客户）
        MkRebatePolicy query = new MkRebatePolicy();
        query.setStatus("0");
        List<MkRebatePolicy> allRows = mkRebatePolicyMapper.selectPolicyList(query);
        Set<String> policyNos = new HashSet<>();
        for (MkRebatePolicy row : allRows)
        {
            if (row.getCustomerId() == null || row.getCustomerId().equals(customerId))
            {
                policyNos.add(row.getPolicyNo());
            }
        }
        String username = SecurityUtils.getUsername();
        for (String policyNo : policyNos)
        {
            List<MkRebatePolicy> tiers = mkRebatePolicyMapper.selectTiersByNo(policyNo);
            if (tiers == null || tiers.isEmpty())
            {
                continue;
            }
            MkRebatePolicy head = tiers.get(0);
            // 政策有效期需覆盖期间起止
            String[] range = periodRange(period, head.getPeriodType());
            java.text.SimpleDateFormat dayFmt = new java.text.SimpleDateFormat("yyyyMMdd");
            if (head.getStartDate() != null && range[0].compareTo(dayFmt.format(head.getStartDate())) < 0)
            {
                continue;
            }
            if (head.getEndDate() != null && range[1].compareTo(dayFmt.format(head.getEndDate())) > 0)
            {
                continue;
            }
            // 已核算过则跳过
            MkRebateSettle exists = mkRebatePolicyMapper.selectSettleExists(customerId, period, policyNo);
            if (exists != null)
            {
                continue;
            }
            // 达成额（本版本按金额口径；数量口径预留，仍按订单金额口径提示）
            BigDecimal achieve = mkRebatePolicyMapper.selectOrderAchieveAmount(customerId, range[2], range[3]);
            // 命中阶梯
            MkRebatePolicy hit = null;
            for (MkRebatePolicy tier : tiers)
            {
                if (achieve.compareTo(tier.getTierMin()) >= 0
                        && (tier.getTierMax() == null || achieve.compareTo(tier.getTierMax()) <= 0))
                {
                    hit = tier;
                }
            }
            if (hit == null)
            {
                continue;
            }
            MkRebateSettle settle1 = new MkRebateSettle();
            settle1.setSettleNo(mkNumberRuleService.generateNumber("mk_rebate_settle"));
            settle1.setCustomerId(customerId);
            settle1.setCustomerName(customer.getCustomerName());
            settle1.setPeriod(period);
            settle1.setPolicyNo(policyNo);
            settle1.setPolicyName(head.getPolicyName());
            settle1.setAchieveAmount(achieve);
            settle1.setRebateRate(hit.getRebateRate());
            settle1.setRebateAmount(achieve.multiply(hit.getRebateRate())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            settle1.setStatus("0");
            settle1.setCreateBy(username);
            mkRebatePolicyMapper.insertSettle(settle1);
            created.add(settle1);
        }
        return created;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int redeem(Long settleId, String redeemType)
    {
        MkRebateSettle settle = mkRebatePolicyMapper.selectSettleById(settleId);
        if (settle == null)
        {
            throw new ServiceException("返利结算单不存在");
        }
        if (!"0".equals(settle.getStatus()))
        {
            throw new ServiceException("只有待兑现状态的结算单才能兑现");
        }
        if (StringUtils.isEmpty(redeemType))
        {
            throw new ServiceException("兑现方式不能为空");
        }
        MkRebateSettle update = new MkRebateSettle();
        update.setSettleId(settleId);
        update.setStatus("1");
        update.setRedeemType(redeemType);
        update.setRedeemDate(new Date());
        update.setRedeemBy(SecurityUtils.getUsername());
        update.setUpdateBy(SecurityUtils.getUsername());
        return mkRebatePolicyMapper.updateSettle(update);
    }

    private void validateTiers(List<MkRebatePolicy> tiers, String expectNo)
    {
        if (tiers == null || tiers.isEmpty())
        {
            throw new ServiceException("返利阶梯不能为空");
        }
        if (expectNo != null && !expectNo.equals(tiers.get(0).getPolicyNo()))
        {
            throw new ServiceException("政策编号不一致");
        }
        for (MkRebatePolicy tier : tiers)
        {
            if (tier.getTierMin() == null)
            {
                throw new ServiceException("阶梯下限不能为空");
            }
            if (tier.getRebateRate() == null || tier.getRebateRate().compareTo(BigDecimal.ZERO) < 0)
            {
                throw new ServiceException("返利比例不能为负数");
            }
            if (tier.getStartDate() == null || tier.getEndDate() == null)
            {
                throw new ServiceException("生效/失效日期不能为空");
            }
            if (tier.getEndDate().before(tier.getStartDate()))
            {
                throw new ServiceException("失效日期不能早于生效日期");
            }
            if (StringUtils.isEmpty(tier.getBasis()))
            {
                tier.setBasis("1");
            }
            if (StringUtils.isEmpty(tier.getPeriodType()))
            {
                tier.setPeriodType("1");
            }
        }
    }

    /**
     * 期间展开：range[0]=起yyyymmdd range[1]=止yyyymmdd range[2]=起datetime range[3]=止datetime(排他)
     */
    private String[] periodRange(String period, String periodType)
    {
        int year = Integer.parseInt(period.substring(0, 4));
        int month = Integer.parseInt(period.substring(4, 6));
        Calendar begin = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        if ("2".equals(periodType))
        {
            // 季
            int quarterStart = ((month - 1) / 3) * 3 + 1;
            begin.set(year, quarterStart - 1, 1, 0, 0, 0);
            end.set(year, quarterStart + 2, 1, 0, 0, 0);
        }
        else if ("3".equals(periodType))
        {
            // 年
            begin.set(year, 0, 1, 0, 0, 0);
            end.set(year + 1, 0, 1, 0, 0, 0);
        }
        else
        {
            // 月（默认）
            begin.set(year, month - 1, 1, 0, 0, 0);
            end.set(year, month, 1, 0, 0, 0);
        }
        begin.set(Calendar.MILLISECOND, 0);
        end.set(Calendar.MILLISECOND, 0);
        String beginDay = new java.text.SimpleDateFormat("yyyyMMdd").format(begin.getTime());
        String endDay = new java.text.SimpleDateFormat("yyyyMMdd").format(end.getTime());
        String beginDt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(begin.getTime());
        String endDt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(end.getTime());
        return new String[]{beginDay, endDay, beginDt, endDt};
    }
}

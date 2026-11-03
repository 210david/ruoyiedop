package com.ruoyi.hr.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrPerfManual;
import com.ruoyi.hr.domain.HrPerfMonth;
import com.ruoyi.hr.domain.HrSalesProfile;
import com.ruoyi.hr.mapper.HrSalesPerfMapper;
import com.ruoyi.hr.service.IHrSalesPerfService;

/**
 * 营销档案/业绩归集 Service业务层处理（需求三）
 *
 * 归集口径：
 * 成交金额 = 营销模块订单（按订单创建时间归属周期），业务归属 = 客户档案 user_id
 * 回款金额 = 回款记录（按记录创建时间归属周期）
 * 新开客户数 = 周期内建档且完成首笔成交的客户
 * 补录业绩 = 审批通过的线下补录（MR-02）
 */
@Service
public class HrSalesPerfServiceImpl implements IHrSalesPerfService {
    @Autowired
    private HrSalesPerfMapper mapper;

    // ---- 营销档案 ----
    @Override
    public List<HrSalesProfile> selectProfileList(HrSalesProfile profile) { return mapper.selectProfileList(profile); }

    @Override
    public HrSalesProfile selectProfileById(Long profileId) { return mapper.selectProfileById(profileId); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertProfile(HrSalesProfile profile) {
        if (profile.getEmployeeId() == null) throw new ServiceException("请选择员工");
        if (profile.getUserId() == null) throw new ServiceException("请填写关联系统用户ID（营销业绩归属）");
        if (mapper.selectProfileByEmployeeId(profile.getEmployeeId()) != null) {
            throw new ServiceException("该员工已存在营销档案扩展记录");
        }
        profile.setDelFlag("0");
        profile.setCreateBy(SecurityUtils.getUsername());
        return mapper.insertProfile(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProfile(HrSalesProfile profile) {
        profile.setUpdateBy(SecurityUtils.getUsername());
        return mapper.updateProfile(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProfileByIds(Long[] profileIds) { return mapper.deleteProfileByIds(profileIds); }

    // ---- 手工补录（需审批） ----
    @Override
    public List<HrPerfManual> selectManualList(HrPerfManual manual) { return mapper.selectManualList(manual); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertManual(HrPerfManual manual) {
        if (StringUtils.isEmpty(manual.getPeriod()) || !manual.getPeriod().matches("\\d{4}-\\d{2}")) {
            throw new ServiceException("归集周期格式应为 yyyy-MM");
        }
        if (manual.getEmployeeId() == null) throw new ServiceException("请选择员工");
        if (manual.getManualAmount() == null || manual.getManualAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("补录金额必须大于0");
        }
        manual.setPerfStatus("0");
        manual.setDelFlag("0");
        manual.setCreateBy(SecurityUtils.getUsername());
        return mapper.insertManual(manual);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveManual(Long manualId, boolean approved, String opinion) {
        HrPerfManual existing = mapper.selectManualById(manualId);
        if (existing == null) throw new ServiceException("补录记录不存在");
        if (!"0".equals(existing.getPerfStatus())) throw new ServiceException("该补录记录已审批，请勿重复审批");
        HrPerfManual update = new HrPerfManual();
        update.setManualId(manualId);
        update.setPerfStatus(approved ? "1" : "2");
        update.setApprover(SecurityUtils.getUsername());
        update.setApproveTime(DateUtils.getNowDate());
        update.setApproveOpinion(opinion);
        update.setUpdateBy(SecurityUtils.getUsername());
        return mapper.updateManual(update);
    }

    // ---- 月度归集与排名 ----
    @Override
    public List<HrPerfMonth> selectPerfList(HrPerfMonth perf) { return mapper.selectPerfList(perf); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int aggregate(String period) {
        if (StringUtils.isEmpty(period) || !period.matches("\\d{4}-\\d{2}")) {
            throw new ServiceException("归集周期格式应为 yyyy-MM");
        }
        List<HrSalesProfile> profiles = mapper.selectProfileList(new HrSalesProfile());
        if (profiles.isEmpty()) return 0;
        Map<Long, BigDecimal> dealMap = toAmountMap(mapper.aggDealAmount(period));
        Map<Long, BigDecimal> payMap = toAmountMap(mapper.aggPaymentAmount(period));
        Map<Long, BigDecimal> newCustMap = toAmountMap(mapper.aggNewCustomer(period));
        Map<Long, BigDecimal> manualMap = toAmountMap(mapper.aggManualAmount(period));
        // 幂等：清理周期快照后重算
        mapper.deletePerfByPeriod(period);
        String oper = SecurityUtils.getUsername();
        int rows = 0;
        for (HrSalesProfile p : profiles) {
            if (p.getUserId() == null) continue;
            HrPerfMonth perf = new HrPerfMonth();
            perf.setPeriod(period);
            perf.setEmployeeId(p.getEmployeeId());
            perf.setEmployeeName(p.getEmployeeName());
            perf.setUserId(p.getUserId());
            perf.setDealAmount(dealMap.getOrDefault(p.getUserId(), BigDecimal.ZERO));
            perf.setPaymentAmount(payMap.getOrDefault(p.getUserId(), BigDecimal.ZERO));
            BigDecimal nc = newCustMap.getOrDefault(p.getUserId(), BigDecimal.ZERO);
            perf.setNewCustomerCount(nc.intValue());
            BigDecimal manualAmt = manualMap.getOrDefault(p.getUserId(), BigDecimal.ZERO);
            perf.setManualAmount(manualAmt);
            perf.setTotalAmount(perf.getDealAmount().add(manualAmt));
            perf.setCreateBy(oper);
            rows += mapper.insertPerf(perf);
        }
        return rows;
    }

    @Override
    public List<HrPerfMonth> selectPerfRank(String period) { return mapper.selectPerfRank(period); }

    /** 聚合结果 Map 转换（userId → amount） */
    private Map<Long, BigDecimal> toAmountMap(List<Map<String, Object>> rows) {
        Map<Long, BigDecimal> map = new HashMap<>();
        if (rows == null) return map;
        for (Map<String, Object> row : rows) {
            Object uid = row.get("userId");
            Object amt = row.get("amount");
            if (uid == null) continue;
            BigDecimal amount = amt == null ? BigDecimal.ZERO : new BigDecimal(amt.toString());
            map.put(Long.valueOf(uid.toString()), amount);
        }
        return map;
    }
}

package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mk.domain.MkPaymentPlan;
import com.ruoyi.mk.domain.MkPaymentRecord;
import com.ruoyi.mk.mapper.MkPaymentPlanMapper;
import com.ruoyi.mk.mapper.MkPaymentRecordMapper;
import com.ruoyi.mk.service.IMkPaymentRecordService;

/**
 * 回款记录 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkPaymentRecordServiceImpl implements IMkPaymentRecordService
{
    @Autowired
    private MkPaymentRecordMapper mkPaymentRecordMapper;

    @Autowired
    private MkPaymentPlanMapper mkPaymentPlanMapper;

    @Override
    public List<MkPaymentRecord> selectPaymentRecordList(MkPaymentRecord record)
    {
        return mkPaymentRecordMapper.selectPaymentRecordList(record);
    }

    @Override
    public List<MkPaymentRecord> selectPaymentRecordByPlanId(Long planId)
    {
        return mkPaymentRecordMapper.selectPaymentRecordByPlanId(planId);
    }

    @Override
    public MkPaymentRecord selectPaymentRecordById(Long recordId)
    {
        return mkPaymentRecordMapper.selectPaymentRecordById(recordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPaymentRecord(MkPaymentRecord record)
    {
        if (record.getConfirmStatus() == null)
        {
            record.setConfirmStatus("0");
        }
        // 从回款计划中补充合同和客户信息
        if (record.getPlanId() != null)
        {
            MkPaymentPlan plan = mkPaymentPlanMapper.selectPaymentPlanById(record.getPlanId());
            if (plan != null)
            {
                record.setContractId(plan.getContractId());
                record.setCustomerId(plan.getCustomerId());
            }
        }
        int rows = mkPaymentRecordMapper.insertPaymentRecord(record);
        // 如果直接确认，更新回款计划的累计金额
        if ("1".equals(record.getConfirmStatus()))
        {
            updatePlanActualAmount(record.getPlanId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePaymentRecord(MkPaymentRecord record)
    {
        return mkPaymentRecordMapper.updatePaymentRecord(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePaymentRecordByIds(Long[] recordIds)
    {
        for (Long recordId : recordIds)
        {
            MkPaymentRecord record = mkPaymentRecordMapper.selectPaymentRecordById(recordId);
            if (record != null && "1".equals(record.getConfirmStatus()))
            {
                throw new ServiceException("已确认的回款记录不允许删除");
            }
        }
        int rows = mkPaymentRecordMapper.deletePaymentRecordByIds(recordIds);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmPaymentRecord(Long recordId, String confirmStatus, String remark)
    {
        MkPaymentRecord record = mkPaymentRecordMapper.selectPaymentRecordById(recordId);
        if (record == null)
        {
            throw new ServiceException("回款记录不存在");
        }
        if (!"0".equals(record.getConfirmStatus()))
        {
            throw new ServiceException("非待确认状态的记录不能操作");
        }
        record.setConfirmStatus(confirmStatus);
        record.setConfirmBy(SecurityUtils.getUsername());
        record.setConfirmTime(new Date());
        if (remark != null)
        {
            record.setRemark(remark);
        }
        int rows = mkPaymentRecordMapper.updatePaymentRecord(record);
        // 确认或驳回后，更新回款计划的累计金额和状态
        updatePlanActualAmount(record.getPlanId());
        return rows;
    }

    /**
     * 更新回款计划的累计实际回款金额和状态
     */
    private void updatePlanActualAmount(Long planId)
    {
        if (planId == null) return;
        MkPaymentPlan plan = mkPaymentPlanMapper.selectPaymentPlanById(planId);
        if (plan == null) return;

        BigDecimal confirmedAmount = mkPaymentRecordMapper.selectConfirmedAmountByPlanId(planId);
        if (confirmedAmount == null) confirmedAmount = BigDecimal.ZERO;

        plan.setActualAmount(confirmedAmount);
        // 设置实际回款日期为最近一次确认的回款日期
        List<MkPaymentRecord> records = mkPaymentRecordMapper.selectPaymentRecordByPlanId(planId);
        for (MkPaymentRecord r : records)
        {
            if ("1".equals(r.getConfirmStatus()) && r.getPaymentDate() != null)
            {
                plan.setActualDate(r.getPaymentDate());
            }
        }

        // 更新状态
        if (confirmedAmount.compareTo(BigDecimal.ZERO) == 0)
        {
            plan.setPaymentStatus("0"); // 待回款
        }
        else if (confirmedAmount.compareTo(plan.getPlanAmount()) >= 0)
        {
            plan.setPaymentStatus("1"); // 已回款
        }
        else
        {
            plan.setPaymentStatus("3"); // 部分回款
        }

        mkPaymentPlanMapper.updatePaymentPlan(plan);
    }
}

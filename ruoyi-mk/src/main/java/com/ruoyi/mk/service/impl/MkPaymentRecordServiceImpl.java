package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(MkPaymentRecordServiceImpl.class);

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
        // 设置创建者
        record.setCreateBy(SecurityUtils.getUsername());

        // 保存关键字段原始值（防止后续查询操作意外覆盖 BaseEntity 继承字段）
        String originalRemark = record.getRemark();
        String originalReceiptAttachment = record.getReceiptAttachment();

        log.info("[insertPaymentRecord] Before plan lookup: remark={}, receiptAttachment={}",
                originalRemark, originalReceiptAttachment);

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

        // 恢复关键字段（防止 selectPaymentPlanById 查询意外覆盖）
        record.setRemark(originalRemark);
        record.setReceiptAttachment(originalReceiptAttachment);

        log.info("[insertPaymentRecord] After plan lookup (restored): remark={}, receiptAttachment={}",
                record.getRemark(), record.getReceiptAttachment());

        int rows = mkPaymentRecordMapper.insertPaymentRecord(record);
        log.info("[insertPaymentRecord] After INSERT: recordId={}, remark={}, receiptAttachment={}",
                record.getRecordId(), record.getRemark(), record.getReceiptAttachment());

        // 验证数据库中实际存储的值
        MkPaymentRecord verifyRecord = mkPaymentRecordMapper.selectPaymentRecordById(record.getRecordId());
        if (verifyRecord != null)
        {
            log.info("[insertPaymentRecord] DB verify: recordId={}, remark={}, receiptAttachment={}",
                    verifyRecord.getRecordId(), verifyRecord.getRemark(), verifyRecord.getReceiptAttachment());
        }

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
        // 只更新确认相关字段，避免全量更新覆盖其他字段
        record.setConfirmStatus(confirmStatus);
        record.setConfirmBy(SecurityUtils.getUsername());
        record.setConfirmTime(new Date());
        record.setUpdateBy(SecurityUtils.getUsername());
        if (remark != null && !remark.trim().isEmpty())
        {
            record.setRemark(remark);
        }
        log.info("[confirmPaymentRecord] Before UPDATE: recordId={}, confirmStatus={}, remark={}, receiptAttachment={}",
                record.getRecordId(), record.getConfirmStatus(), record.getRemark(), record.getReceiptAttachment());
        int rows = mkPaymentRecordMapper.updatePaymentRecord(record);
        log.info("[confirmPaymentRecord] After UPDATE: recordId={}, remark={}, receiptAttachment={}",
                record.getRecordId(), record.getRemark(), record.getReceiptAttachment());
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

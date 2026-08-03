package com.ruoyi.pms.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.pms.domain.PmsSupplierEval;
import com.ruoyi.pms.domain.PmsSupplierEvalAuditLog;
import com.ruoyi.pms.mapper.PmsSupplierEvalAuditLogMapper;
import com.ruoyi.pms.mapper.PmsSupplierEvalMapper;
import com.ruoyi.pms.service.IPmsSupplierEvalService;

/**
 * 供应商评估 Service实现
 *
 * @author ruoyi
 */
@Service
public class PmsSupplierEvalServiceImpl implements IPmsSupplierEvalService
{
    @Autowired
    private PmsSupplierEvalMapper pmsSupplierEvalMapper;

    @Autowired
    private PmsSupplierEvalAuditLogMapper pmsSupplierEvalAuditLogMapper;

    @Override
    public List<PmsSupplierEval> selectEvalList(PmsSupplierEval eval)
    {
        return pmsSupplierEvalMapper.selectEvalList(eval);
    }

    @Override
    public PmsSupplierEval selectEvalById(Long evalId)
    {
        PmsSupplierEval eval = pmsSupplierEvalMapper.selectEvalById(evalId);
        if (eval != null)
        {
            // 加载审核日志列表
            List<PmsSupplierEvalAuditLog> auditLogList = pmsSupplierEvalAuditLogMapper.selectAuditLogByEvalId(evalId);
            eval.setAuditLogList(auditLogList);
        }
        return eval;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertEval(PmsSupplierEval eval)
    {
        eval.setDelFlag("0");
        if (eval.getStatus() == null)
        {
            eval.setStatus("0");
        }
        // 计算单项得分、综合评分、等级
        calculateScores(eval);
        return pmsSupplierEvalMapper.insertEval(eval);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEval(PmsSupplierEval eval)
    {
        PmsSupplierEval existing = pmsSupplierEvalMapper.selectEvalById(eval.getEvalId());
        if (existing == null)
        {
            throw new ServiceException("供应商评估记录不存在");
        }
        if ("1".equals(existing.getStatus()))
        {
            throw new ServiceException("已审核状态的评估记录不允许修改");
        }
        // 驳回状态修改后重置为待审核，清空主表审批信息（审核日志保留）
        if ("2".equals(existing.getStatus()))
        {
            eval.setStatus("0");
            eval.setAuditBy(null);
            eval.setAuditTime(null);
            eval.setAuditRemark(null);
        }
        // 计算单项得分、综合评分、等级
        calculateScores(eval);
        return pmsSupplierEvalMapper.updateEval(eval);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteEvalByIds(Long[] evalIds)
    {
        for (Long evalId : evalIds)
        {
            PmsSupplierEval existing = pmsSupplierEvalMapper.selectEvalById(evalId);
            if (existing != null && "1".equals(existing.getStatus()))
            {
                throw new ServiceException("已审核状态的评估记录不允许删除");
            }
        }
        return pmsSupplierEvalMapper.deleteEvalByIds(evalIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditEval(Long evalId, String status, String auditRemark)
    {
        PmsSupplierEval existing = pmsSupplierEvalMapper.selectEvalById(evalId);
        if (existing == null)
        {
            throw new ServiceException("供应商评估记录不存在");
        }
        if (!"0".equals(existing.getStatus()))
        {
            throw new ServiceException("只有待审核状态的评估记录才能审批");
        }
        existing.setStatus(status);
        existing.setAuditBy(SecurityUtils.getUsername());
        existing.setAuditTime(new Date());
        existing.setAuditRemark(auditRemark);
        int rows = pmsSupplierEvalMapper.updateEval(existing);
        // 写入审核日志（1通过 2驳回）
        PmsSupplierEvalAuditLog auditLog = new PmsSupplierEvalAuditLog();
        auditLog.setEvalId(evalId);
        auditLog.setAuditAction(status);
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditRemark(auditRemark);
        pmsSupplierEvalAuditLogMapper.insertAuditLog(auditLog);
        return rows;
    }

    /**
     * 计算单项得分（评分 × 权重 / 100）、综合评分、评估等级
     */
    private void calculateScores(PmsSupplierEval eval)
    {
        BigDecimal hundred = new BigDecimal("100");
        BigDecimal total = BigDecimal.ZERO;

        // 交货
        if (eval.getDeliveryScore() != null && eval.getDeliveryWeight() != null)
        {
            BigDecimal itemScore = eval.getDeliveryScore()
                    .multiply(eval.getDeliveryWeight())
                    .divide(hundred, 2, RoundingMode.HALF_UP);
            eval.setDeliveryItemScore(itemScore);
            total = total.add(itemScore);
        }
        // 质量
        if (eval.getQualityScore() != null && eval.getQualityWeight() != null)
        {
            BigDecimal itemScore = eval.getQualityScore()
                    .multiply(eval.getQualityWeight())
                    .divide(hundred, 2, RoundingMode.HALF_UP);
            eval.setQualityItemScore(itemScore);
            total = total.add(itemScore);
        }
        // 价格
        if (eval.getPriceScore() != null && eval.getPriceWeight() != null)
        {
            BigDecimal itemScore = eval.getPriceScore()
                    .multiply(eval.getPriceWeight())
                    .divide(hundred, 2, RoundingMode.HALF_UP);
            eval.setPriceItemScore(itemScore);
            total = total.add(itemScore);
        }
        // 服务
        if (eval.getServiceScore() != null && eval.getServiceWeight() != null)
        {
            BigDecimal itemScore = eval.getServiceScore()
                    .multiply(eval.getServiceWeight())
                    .divide(hundred, 2, RoundingMode.HALF_UP);
            eval.setServiceItemScore(itemScore);
            total = total.add(itemScore);
        }

        eval.setTotalScore(total.setScale(2, RoundingMode.HALF_UP));

        // 自动评定等级
        if (total.compareTo(new BigDecimal("90")) >= 0)
        {
            eval.setEvalLevel("A");
        }
        else if (total.compareTo(new BigDecimal("80")) >= 0)
        {
            eval.setEvalLevel("B");
        }
        else if (total.compareTo(new BigDecimal("60")) >= 0)
        {
            eval.setEvalLevel("C");
        }
        else
        {
            eval.setEvalLevel("D");
        }
    }
}

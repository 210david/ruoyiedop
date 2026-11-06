package com.ruoyi.pjm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.pjm.domain.PjmPayment;
import com.ruoyi.pjm.mapper.PjmPaymentMapper;
import com.ruoyi.pjm.service.IPjmPaymentService;

/**
 * 项目收款记录Service业务层处理
 */
@Service
public class PjmPaymentServiceImpl implements IPjmPaymentService
{
    @Autowired
    private PjmPaymentMapper pjmPaymentMapper;

    @Autowired
    private IMkNumberRuleService numberRuleService;

    @Override
    public List<PjmPayment> selectPaymentList(PjmPayment pjmPayment)
    {
        return pjmPaymentMapper.selectPaymentList(pjmPayment);
    }

    @Override
    public PjmPayment selectPaymentById(Long paymentId)
    {
        return pjmPaymentMapper.selectPaymentById(paymentId);
    }

    @Override
    public int insertPayment(PjmPayment pjmPayment)
    {
        if (pjmPayment.getPaymentNo() == null || pjmPayment.getPaymentNo().isEmpty())
        {
            pjmPayment.setPaymentNo(numberRuleService.generateNumber("PJM_PAYMENT"));
        }
        pjmPayment.setCreateBy(SecurityUtils.getUsername());
        pjmPayment.setCreateTime(DateUtils.getNowDate());
        return pjmPaymentMapper.insertPayment(pjmPayment);
    }

    @Override
    public int updatePayment(PjmPayment pjmPayment)
    {
        pjmPayment.setUpdateBy(SecurityUtils.getUsername());
        pjmPayment.setUpdateTime(DateUtils.getNowDate());
        return pjmPaymentMapper.updatePayment(pjmPayment);
    }

    @Override
    public int deletePaymentByIds(Long[] paymentIds)
    {
        return pjmPaymentMapper.deletePaymentByIds(paymentIds);
    }

    @Override
    public List<PjmPayment> selectPaymentSummary(PjmPayment pjmPayment)
    {
        return pjmPaymentMapper.selectPaymentSummary(pjmPayment);
    }
}

package com.ruoyi.pjm.service;

import java.util.List;
import com.ruoyi.pjm.domain.PjmPayment;

/**
 * 项目收款记录Service接口
 */
public interface IPjmPaymentService
{
    public List<PjmPayment> selectPaymentList(PjmPayment pjmPayment);

    public PjmPayment selectPaymentById(Long paymentId);

    public int insertPayment(PjmPayment pjmPayment);

    public int updatePayment(PjmPayment pjmPayment);

    public int deletePaymentByIds(Long[] paymentIds);

    /** 按项目汇总回款进度 */
    public List<PjmPayment> selectPaymentSummary(PjmPayment pjmPayment);
}

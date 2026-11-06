package com.ruoyi.pjm.mapper;

import java.util.List;
import com.ruoyi.pjm.domain.PjmPayment;

/**
 * 项目收款记录Mapper接口
 */
public interface PjmPaymentMapper
{
    public List<PjmPayment> selectPaymentList(PjmPayment pjmPayment);

    public PjmPayment selectPaymentById(Long paymentId);

    public int insertPayment(PjmPayment pjmPayment);

    public int updatePayment(PjmPayment pjmPayment);

    public int deletePaymentByIds(Long[] paymentIds);

    /** 按项目汇总回款 */
    public List<PjmPayment> selectPaymentSummary(PjmPayment pjmPayment);
}

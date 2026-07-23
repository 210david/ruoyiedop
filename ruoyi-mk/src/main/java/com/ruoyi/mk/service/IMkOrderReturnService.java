package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkOrderReturn;

/**
 * 订单退货 Service接口
 *
 * @author ruoyi
 */
public interface IMkOrderReturnService
{
    List<MkOrderReturn> selectReturnList(MkOrderReturn orderReturn);
    MkOrderReturn selectReturnById(Long returnId);
    int insertReturn(MkOrderReturn orderReturn);
    int deleteReturnByIds(Long[] returnIds);
    /** 审批退货 */
    int approveReturn(Long returnId, boolean approved, String opinion);
    /** 退款 */
    int refundReturn(Long returnId, java.math.BigDecimal refundAmount, java.util.Date refundDate);
}

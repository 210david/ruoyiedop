package com.ruoyi.pms.service;

import java.util.List;
import com.ruoyi.pms.domain.PmsReturn;

/**
 * 退货 Service接口
 *
 * @author ruoyi
 */
public interface IPmsReturnService
{
    public List<PmsReturn> selectReturnList(PmsReturn pmsReturn);
    public PmsReturn selectReturnById(Long returnId);
    public int insertReturn(PmsReturn pmsReturn);
    public int updateReturn(PmsReturn pmsReturn);
    public int deleteReturnByIds(Long[] returnIds);

    /** 提交审批 */
    public int submitReturn(Long returnId);

    /** 审批退货（返回出库单号等信息） */
    public java.util.Map<String, Object> auditReturn(Long returnId, String status, String auditOpinion);

    /**
     * 查询存在进行中退货单的采购订单ID列表
     * 进行中 = 状态为草稿(0)、待审批(1)、已审批(2)、已驳回(5)
     * 用于前端过滤，避免同一订单同时存在多个进行中的退货单
     */
    public List<Long> selectInProgressReturnOrderIds();
}

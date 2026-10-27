package com.ruoyi.pms.service;

import java.util.List;
import com.ruoyi.pms.domain.PmsReceive;

/**
 * 收货验收 Service接口
 *
 * @author ruoyi
 */
public interface IPmsReceiveService
{
    public List<PmsReceive> selectReceiveList(PmsReceive receive);
    public PmsReceive selectReceiveById(Long receiveId);
    public int insertReceive(PmsReceive receive);
    public int updateReceive(PmsReceive receive);
    public int deleteReceiveByIds(Long[] receiveIds);
    public int doInspect(PmsReceive receive);

    /** 提交审核 */
    public int submitReceive(Long receiveId);

    /** 审核收货 */
    public int auditReceive(Long receiveId, String status, String auditOpinion);

    /** 审核收货（返回入库单号等信息） */
    public java.util.Map<String, Object> auditReceiveWithInboundOrder(Long receiveId, String status, String auditOpinion);

    /**
     * 查询已有有效收货记录的采购订单ID列表
     * 用于前端过滤已收货的订单，避免重复发起收货
     */
    public List<Long> selectReceivedOrderIds();

    /**
     * 查询存在进行中收货单的采购订单ID列表
     * 进行中 = 状态为待验收(0)、待审核(5)、已驳回(6)
     * 用于前端过滤，避免同一订单同时存在多个进行中的收货单
     */
    public List<Long> selectInProgressReceiveOrderIds();
}

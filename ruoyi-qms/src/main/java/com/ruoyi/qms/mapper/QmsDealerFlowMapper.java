package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.vo.QmsDealerFlowNode;

/**
 * 经销商分销流向追溯 Mapper（跨模块只读 mk_dealer_sell_flow，P1-08 追溯链接入）
 *
 * @author ruoyi
 */
public interface QmsDealerFlowMapper
{
    /**
     * 按批次号查询经销商分销流向明细
     *
     * @param batchNo 批次号
     * @return 流向明细列表（含经销商名称、下游去向、物料、发货单号）
     */
    public List<QmsDealerFlowNode> selectFlowsByBatchNo(String batchNo);
}

package com.ruoyi.kingdee.mapper;

import java.util.List;

import com.ruoyi.kingdee.domain.KdSalOutbound;

/**
 * 金蝶销售出库单 数据层
 *
 * @author ruoyi
 */
public interface KdSalOutboundMapper
{
    /**
     * 查询销售出库单列表
     */
    List<KdSalOutbound> selectKdSalOutboundList(KdSalOutbound query);

    /**
     * 批量写入（按单据ID+分录ID 幂等）
     */
    int batchUpsert(List<KdSalOutbound> list);

    /**
     * 按单据ID删除旧分录（同步前清理，防止分录变更产生孤儿行）
     */
    int deleteByBillIds(List<String> billIds);
}

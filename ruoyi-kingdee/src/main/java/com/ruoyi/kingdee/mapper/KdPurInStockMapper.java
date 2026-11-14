package com.ruoyi.kingdee.mapper;

import java.util.List;

import com.ruoyi.kingdee.domain.KdPurInStock;

/**
 * 金蝶采购入库单 数据层
 */
public interface KdPurInStockMapper
{
    List<KdPurInStock> selectKdPurInStockList(KdPurInStock query);

    int batchUpsert(List<KdPurInStock> list);

    int deleteByBillIds(List<String> billIds);
}

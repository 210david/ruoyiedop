package com.ruoyi.kingdee.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.kingdee.domain.KdPurInStock;
import com.ruoyi.kingdee.mapper.KdPurInStockMapper;

/**
 * 金蝶采购入库单 服务
 */
@Service
public class KdPurInStockService
{
    @Autowired
    private KdPurInStockMapper purInStockMapper;

    public List<KdPurInStock> selectList(KdPurInStock query)
    {
        return purInStockMapper.selectKdPurInStockList(query);
    }

    public int batchUpsert(List<KdPurInStock> list)
    {
        if (list == null || list.isEmpty()) return 0;
        return purInStockMapper.batchUpsert(list);
    }

    public int deleteByBillIds(List<String> billIds)
    {
        if (billIds == null || billIds.isEmpty()) return 0;
        return purInStockMapper.deleteByBillIds(billIds);
    }
}

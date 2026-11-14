package com.ruoyi.kingdee.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.kingdee.domain.KdSalOutbound;
import com.ruoyi.kingdee.mapper.KdSalOutboundMapper;

/**
 * 金蝶销售出库单 服务
 *
 * @author ruoyi
 */
@Service
public class KdSalOutboundService
{
    @Autowired
    private KdSalOutboundMapper salOutboundMapper;

    /** 查询列表 */
    public List<KdSalOutbound> selectList(KdSalOutbound query)
    {
        return salOutboundMapper.selectKdSalOutboundList(query);
    }

    /** 按单据ID+分录ID 批量幂等写入 */
    public int batchUpsert(List<KdSalOutbound> list)
    {
        if (list == null || list.isEmpty())
        {
            return 0;
        }
        return salOutboundMapper.batchUpsert(list);
    }

    /** 按单据ID删除旧分录 */
    public int deleteByBillIds(List<String> billIds)
    {
        if (billIds == null || billIds.isEmpty())
        {
            return 0;
        }
        return salOutboundMapper.deleteByBillIds(billIds);
    }
}

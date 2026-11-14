package com.ruoyi.kingdee.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.kingdee.domain.KdInventory;
import com.ruoyi.kingdee.mapper.KdInventoryMapper;

/**
 * 金蝶库存查询 服务
 */
@Service
public class KdInventoryService
{
    @Autowired
    private KdInventoryMapper inventoryMapper;

    public List<KdInventory> selectList(KdInventory query)
    {
        return inventoryMapper.selectList(query);
    }

    public int batchUpsert(List<KdInventory> list)
    {
        if (list == null || list.isEmpty()) return 0;
        return inventoryMapper.batchUpsert(list);
    }

    public int deleteAll()
    {
        return inventoryMapper.deleteAll();
    }

    public List<String> selectDistinctCategories()
    {
        return inventoryMapper.selectDistinctCategories();
    }

    public List<KdInventory> selectCategoryList()
    {
        return inventoryMapper.selectCategoryList();
    }
}

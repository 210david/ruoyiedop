package com.ruoyi.kingdee.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.kingdee.domain.KdInventory;

/**
 * 金蝶库存查询 Mapper
 */
public interface KdInventoryMapper
{
    List<KdInventory> selectList(KdInventory query);

    int batchUpsert(List<KdInventory> list);

    int deleteAll();

    List<String> selectDistinctCategories();

    List<Map<String, Object>> selectCategoryList();
}

package com.ruoyi.kingdee.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 库存分类树（与金蝶一致的两级：一级商品类别 → 二级商品类别）
     */
    public List<Map<String, Object>> selectCategoryList()
    {
        List<Map<String, Object>> rows = inventoryMapper.selectCategoryList();
        Map<String, Map<String, Object>> roots = new LinkedHashMap<>();
        for (Map<String, Object> row : rows)
        {
            Object rootId = row.get("rootId");
            Object childId = row.get("childId");
            String rootKey = rootId == null ? "" : String.valueOf(rootId);
            String childKey = childId == null ? "" : String.valueOf(childId);
            Map<String, Object> root = roots.get(rootKey);
            if (root == null)
            {
                root = new LinkedHashMap<>();
                root.put("categoryId", rootKey);
                root.put("categoryName", row.get("rootName"));
                root.put("children", new ArrayList<Map<String, Object>>());
                roots.put(rootKey, root);
            }
            // 物料直接挂在一级类别下时不再生成子级
            if (childKey.isEmpty() || childKey.equals(rootKey)) continue;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) root.get("children");
            Map<String, Object> child = new LinkedHashMap<>();
            child.put("categoryId", childKey);
            child.put("categoryName", row.get("childName"));
            children.add(child);
        }
        return new ArrayList<>(roots.values());
    }
}

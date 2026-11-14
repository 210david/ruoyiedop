package com.ruoyi.kingdee.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.kingdee.domain.KdCategory;
import com.ruoyi.kingdee.domain.KdInventory;
import com.ruoyi.kingdee.mapper.KdCategoryMapper;
import com.ruoyi.kingdee.mapper.KdInventoryMapper;

/**
 * 金蝶库存查询 服务
 */
@Service
public class KdInventoryService
{
    @Autowired
    private KdInventoryMapper inventoryMapper;

    @Autowired
    private KdCategoryMapper categoryMapper;

    /**
     * 点击任意分类时，按该分类及其所有下级分类过滤。
     * 需在 startPage() 之前调用：此方法会查询 kd_category，若在分页之后调用会占用分页参数。
     */
    public void expandCategoryIds(KdInventory query)
    {
        if (StringUtils.isNotEmpty(query.getCategoryId()))
        {
            query.setCategoryIds(descendantIds(query.getCategoryId()));
        }
    }

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
     * 库存分类树（与金蝶商品类别树一致的多级结构）
     */
    public List<Map<String, Object>> selectCategoryList()
    {
        List<KdCategory> cats = categoryMapper.selectAllOrdered();
        if (cats.isEmpty()) return new ArrayList<>();
        List<String> used = inventoryMapper.selectDistinctCategories();
        Map<String, KdCategory> byId = new HashMap<>();
        for (KdCategory c : cats) byId.put(c.getId(), c);

        // 只展示有库存的分类及其所有上级分类
        Set<String> include = new HashSet<>();
        for (String id : used)
        {
            String cur = id;
            int guard = 0;
            while (cur != null && include.add(cur) && guard++ < 20)
            {
                KdCategory c = byId.get(cur);
                cur = c == null ? null : c.getParentId();
            }
        }

        Map<String, Map<String, Object>> nodes = new LinkedHashMap<>();
        for (KdCategory c : cats)
        {
            if (!include.contains(c.getId())) continue;
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("categoryId", c.getId());
            node.put("categoryName", c.getName());
            node.put("children", new ArrayList<Map<String, Object>>());
            nodes.put(c.getId(), node);
        }
        List<Map<String, Object>> roots = new ArrayList<>();
        for (KdCategory c : cats)
        {
            Map<String, Object> node = nodes.get(c.getId());
            if (node == null) continue;
            Map<String, Object> parent = c.getParentId() == null ? null : nodes.get(c.getParentId());
            if (parent == null) roots.add(node);
            else
            {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                children.add(node);
            }
        }
        return roots;
    }

    /** 分类及其所有下级分类 id */
    private List<String> descendantIds(String categoryId)
    {
        List<String> ids = new ArrayList<>();
        ids.add(categoryId);
        Map<String, List<String>> childrenOf = new HashMap<>();
        for (KdCategory c : categoryMapper.selectAllOrdered())
        {
            if (c.getParentId() == null) continue;
            childrenOf.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c.getId());
        }
        for (int i = 0; i < ids.size(); i++)
        {
            List<String> children = childrenOf.get(ids.get(i));
            if (children == null) continue;
            for (String child : children)
            {
                if (!ids.contains(child)) ids.add(child);
            }
        }
        return ids;
    }
}

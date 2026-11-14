package com.ruoyi.kingdee.sync;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.kingdee.adapter.KingdeeHttpClient;
import com.ruoyi.kingdee.domain.KdInventory;
import com.ruoyi.kingdee.service.KdInventoryService;

/**
 * 金蝶云星辰 库存查询同步服务
 * <p>
 * 库存接口：GET /scm/inventory（按商品+仓库分行，qty 即时库存，valid_qty 可用量）
 * 物料信息（名称/规格/单位/分类）从物料列表接口 /bd/material 补充
 */
@Service
public class KdInventorySyncService
{
    private static final Logger log = LoggerFactory.getLogger(KdInventorySyncService.class);

    private static final String INVENTORY_PATH = "/scm/inventory";
    private static final String MATERIAL_PATH = "/bd/material";
    /** 商品类别（用于按金蝶展示顺序排列库存分类树） */
    private static final String CATEGORY_PATH = "/bd/material_group";
    private static final int PAGE_SIZE = 100;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private KingdeeHttpClient kingdeeClient;
    @Autowired private KdInventoryService inventoryService;

    private boolean sampleLogged = false;

    public SyncResult syncAll()
    {
        SyncResult result = new SyncResult();
        long start = System.currentTimeMillis();

        try
        {
            // 1. 拉取物料信息（编号/名称/规格/单位/分类）
            Map<String, Map<String, Object>> materialMap = fetchMaterialMap();
            log.info("金蝶库存同步 物料信息加载完成: {} 条", materialMap.size());
            // 商品类别展示顺序（金蝶商品类别树顺序）
            Map<String, Integer> categoryOrder = loadCategoryOrder();
            log.info("金蝶库存同步 商品类别顺序加载完成: {} 条", categoryOrder.size());

            // 2. 拉取库存（接口按「商品+仓库+库位」返回，需合并到商品+仓库）
            Map<String, KdInventory> merged = new LinkedHashMap<>();
            int rawRows = 0;
            int page = 1;
            while (true)
            {
                Map<String, String> params = new HashMap<>();
                params.put("page", String.valueOf(page));
                params.put("pagesize", String.valueOf(PAGE_SIZE));

                String raw;
                try { raw = kingdeeClient.getWithRetry(INVENTORY_PATH, params); }
                catch (Exception e) { result.addSkipReason("库存接口拉取失败: " + e.getMessage()); break; }

                Map<String, Object> resp;
                try { resp = objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {}); }
                catch (Exception e) { result.addSkipReason("库存JSON解析失败: " + e.getMessage()); break; }

                Integer errcode = asInt(resp.get("errcode"));
                if (errcode == null || errcode != 0)
                {
                    result.addSkipReason("库存接口报错 errcode=" + errcode + ": " + resp.get("description"));
                    break;
                }

                if (!sampleLogged)
                {
                    log.info("金蝶库存列表原始响应: {}", raw);
                    sampleLogged = true;
                }

                Map<String, Object> data = asMap(resp.get("data"));
                if (data == null) break;

                List<Map<String, Object>> rows = asMapList(data.get("rows"));
                if (rows == null) rows = asMapList(data.get("list"));
                if (rows == null || rows.isEmpty()) break;

                for (Map<String, Object> row : rows)
                {
                    KdInventory inv = toEntity(row, materialMap, categoryOrder);
                    if (inv == null) continue;
                    rawRows++;
                    // 同一商品+仓库存在多个库位记录时按库位汇总，避免互相覆盖导致数量不一致
                    String key = inv.getMaterialId() + "|" + (inv.getStockId() == null ? "" : inv.getStockId());
                    KdInventory exist = merged.get(key);
                    if (exist == null)
                    {
                        merged.put(key, inv);
                    }
                    else
                    {
                        exist.setQty(add(exist.getQty(), inv.getQty()));
                        exist.setValidQty(add(exist.getValidQty(), inv.getValidQty()));
                        exist.setAvailableOutQty(add(exist.getAvailableOutQty(), inv.getAvailableOutQty()));
                    }
                }

                Object totalPage = data.get("total_page");
                boolean lastPage = (totalPage instanceof Number && page >= ((Number) totalPage).intValue());
                if (lastPage) break;
                page++;
            }

            // 3. 剔除库存为 0 的记录（与金蝶「库存查询」页面口径一致），全量替换写入
            List<KdInventory> all = new ArrayList<>();
            for (KdInventory inv : merged.values())
            {
                if (inv.getQty() != null && inv.getQty().compareTo(BigDecimal.ZERO) != 0)
                {
                    all.add(inv);
                }
            }
            log.info("金蝶库存同步 接口原始行 {}，商品+仓库合并后 {}，剔除零库存 {}，入库 {}",
                    rawRows, merged.size(), merged.size() - all.size(), all.size());
            if (!all.isEmpty())
            {
                inventoryService.deleteAll();
                for (int i = 0; i < all.size(); i += 500)
                {
                    List<KdInventory> batch = all.subList(i, Math.min(i + 500, all.size()));
                    inventoryService.batchUpsert(batch);
                }
                result.getStatistics().put("total", all.size());
                result.getStatistics().put("inserted", all.size());
            }
        }
        catch (Exception e)
        {
            log.error("金蝶库存同步异常", e);
            result.addSkipReason("同步异常: " + e.getMessage());
        }

        result.setElapsedMs(System.currentTimeMillis() - start);
        log.info("金蝶库存同步完成: {}", result.getStatistics());
        return result;
    }

    /** 拉取物料信息，构建 materialId -> 物料信息 映射 */
    private Map<String, Map<String, Object>> fetchMaterialMap()
    {
        Map<String, Map<String, Object>> map = new HashMap<>();
        int page = 1;
        while (true)
        {
            Map<String, String> params = new HashMap<>();
            params.put("page", String.valueOf(page));
            params.put("pagesize", String.valueOf(PAGE_SIZE));
            String raw;
            try { raw = kingdeeClient.getWithRetry(MATERIAL_PATH, params); }
            catch (Exception e) { log.warn("物料拉取失败 page={}: {}", page, e.getMessage()); break; }

            Map<String, Object> resp;
            try { resp = objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {}); }
            catch (Exception e) { break; }

            Integer errcode = asInt(resp.get("errcode"));
            if (errcode == null || errcode != 0) break;

            Map<String, Object> data = asMap(resp.get("data"));
            if (data == null) break;
            List<Map<String, Object>> rows = asMapList(data.get("rows"));
            if (rows == null || rows.isEmpty()) break;

            for (Map<String, Object> m : rows)
            {
                String id = str(m.get("id"));
                if (id != null) map.put(id, m);
            }

            Object totalPage = data.get("total_page");
            boolean lastPage = (totalPage instanceof Number && page >= ((Number) totalPage).intValue());
            if (lastPage) break;
            page++;
        }
        return map;
    }

    /**
     * 拉取金蝶商品类别树，按金蝶展示顺序（父级在前、同级保持接口返回顺序）遍历，
     * 生成 类别 id / number / name → 排序号，用于库存分类树与金蝶保持一致
     */
    private Map<String, Integer> loadCategoryOrder()
    {
        Map<String, Integer> order = new HashMap<>();
        List<Map<String, Object>> all = new ArrayList<>();
        int page = 1, totalPage = 1;
        try
        {
            while (page <= totalPage && page <= 100)
            {
                Map<String, String> params = new HashMap<>();
                params.put("page", String.valueOf(page));
                params.put("pagesize", "1000");
                String raw = kingdeeClient.getWithRetry(CATEGORY_PATH, params);
                Map<String, Object> resp = objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {});
                Integer errcode = asInt(resp.get("errcode"));
                if (errcode == null || errcode != 0)
                {
                    log.warn("金蝶商品类别接口报错 errcode={}: {}", errcode, resp.get("description"));
                    break;
                }
                Map<String, Object> data = asMap(resp.get("data"));
                if (data == null) break;
                List<Map<String, Object>> rows = asMapList(data.get("rows"));
                if (rows == null || rows.isEmpty()) break;
                all.addAll(rows);
                Object tp = data.get("total_page");
                totalPage = (tp instanceof Number) ? ((Number) tp).intValue() : 1;
                page++;
            }
        }
        catch (Exception e)
        {
            log.warn("拉取金蝶商品类别失败，库存分类将按名称排序: {}", e.getMessage());
            return order;
        }

        if (all.isEmpty()) return order;
        log.info("金蝶商品类别样例: {}", all.subList(0, Math.min(3, all.size())));

        Map<String, String> parentOf = new HashMap<>();
        Map<String, List<String>> childrenOf = new HashMap<>();
        Map<String, Map<String, Object>> byId = new LinkedHashMap<>();
        for (Map<String, Object> g : all)
        {
            String id = str(g.get("id"));
            if (id == null) continue;
            byId.put(id, g);
            String pid = str(g.get("parent_id"));
            parentOf.put(id, pid == null ? "" : pid);
        }
        List<String> roots = new ArrayList<>();
        for (String id : byId.keySet())
        {
            String pid = parentOf.get(id);
            if (pid == null || pid.isEmpty() || !byId.containsKey(pid)) roots.add(id);
            else childrenOf.computeIfAbsent(pid, k -> new ArrayList<>()).add(id);
        }
        int[] seq = { 0 };
        for (String root : roots)
        {
            fillCategoryOrder(root, 1, childrenOf, byId, order, seq);
        }
        return order;
    }

    private void fillCategoryOrder(String id, int depth, Map<String, List<String>> childrenOf,
                                   Map<String, Map<String, Object>> byId, Map<String, Integer> order, int[] seq)
    {
        Map<String, Object> g = byId.get(id);
        int index = seq[0]++;
        if (g != null)
        {
            order.put(id, index);
            String number = str(g.get("number"));
            if (number != null && !number.isEmpty()) order.putIfAbsent(number, index);
            String name = str(g.get("name"));
            if (name != null && !name.isEmpty()) order.putIfAbsent(name, index);
        }
        if (depth > 20) return;
        List<String> children = childrenOf.get(id);
        if (children == null) return;
        for (String child : children)
        {
            fillCategoryOrder(child, depth + 1, childrenOf, byId, order, seq);
        }
    }

    private KdInventory toEntity(Map<String, Object> row, Map<String, Map<String, Object>> materialMap,
                                 Map<String, Integer> categoryOrder)
    {
        String materialId = str(pick(row, "material_id", "materialid"));
        if (materialId == null) return null;

        KdInventory o = new KdInventory();
        o.setMaterialId(materialId);
        o.setStockId(str(pick(row, "stock_id", "stockid", "warehouse_id")));
        o.setStockName(str(pick(row, "stock_name", "stockname", "warehouse_name")));
        o.setQty(dec(pick(row, "qty", "quantity", "stock_qty")));
        o.setValidQty(dec(pick(row, "valid_qty", "available_qty", "can_use_qty")));

        // 可出库数量：优先取接口字段，否则用可用量
        BigDecimal availOut = dec(pick(row, "available_out_qty", "can_out_qty", "out_qty"));
        if (availOut == null) availOut = o.getValidQty();
        o.setAvailableOutQty(availOut);

        // 从物料信息补充名称/规格/单位/分类
        Map<String, Object> mat = materialMap.get(materialId);
        if (mat != null)
        {
            o.setMaterialNumber(str(pickAny(mat, "number", "material_number", "materialNumber")));
            o.setMaterialName(str(pickAny(mat, "name", "material_name", "materialName")));
            o.setSpecModel(str(pickAny(mat, "spec", "model", "spec_model")));
            o.setUnitName(str(pickAny(mat, "unit", "unit_name", "base_unit_name")));
            o.setCategoryId(str(pickAny(mat, "parent_id", "group_id", "category_id", "material_group_id")));
            o.setCategoryName(str(pickAny(mat, "parent_name", "group_name", "category_name", "material_group_name")));
            Integer sort = o.getCategoryId() == null ? null : categoryOrder.get(o.getCategoryId());
            if (sort == null && o.getCategoryName() != null) sort = categoryOrder.get(o.getCategoryName());
            o.setCategorySort(sort != null ? sort : Integer.MAX_VALUE);
            o.setImageUrl(str(pickAny(mat, "url", "image_url", "imageUrl", "picture_url")));
        }

        o.setSyncTime(new Date());
        return o;
    }

    // ===== 辅助方法 =====
    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
    private static BigDecimal dec(Object o) { if (o == null) return null; try { return new BigDecimal(String.valueOf(o)); } catch (Exception e) { return null; } }
    private static BigDecimal add(BigDecimal a, BigDecimal b) { if (a == null) return b; if (b == null) return a; return a.add(b); }
    private static Integer asInt(Object o) { if (o == null) return null; try { return Integer.valueOf(String.valueOf(o)); } catch (Exception e) { return null; } }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) { return o instanceof Map ? (Map<String, Object>) o : null; }
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asMapList(Object o) { return o instanceof List ? (List<Map<String, Object>>) o : null; }
    private static Object pick(Map<String, Object> m, String... keys) { for (String k : keys) { if (m.containsKey(k)) return m.get(k); } return null; }
    private static String pickAny(Map<String, Object> m, String... keys) { for (String k : keys) { Object v = m.get(k); if (v != null && !String.valueOf(v).isEmpty()) return String.valueOf(v); } return null; }
}

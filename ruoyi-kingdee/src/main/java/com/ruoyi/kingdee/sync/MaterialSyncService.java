package com.ruoyi.kingdee.sync;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.kingdee.adapter.KingdeeHttpClient;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.system.service.ISysDictDataService;
import com.ruoyi.wms.domain.WmsMaterial;
import com.ruoyi.wms.service.IWmsMaterialService;

/** 金蝶云星辰 物料主数据同步 */
@Service
public class MaterialSyncService
{
    private static final Logger log = LoggerFactory.getLogger(MaterialSyncService.class);
    private static final String MATERIAL_PATH = "/bd/material";
    /** 商品类别（用于把子类别归到一级分类） */
    private static final String MATERIAL_GROUP_PATH = "/bd/material_group";
    /** 物料类型字典类型（直接使用金蝶商品类别名称作为字典项） */
    private static final String MATERIAL_TYPE_DICT = "wms_material_type";
    /** 计量单位字典类型（与金蝶单位名称做对应，缺失则新增） */
    private static final String MATERIAL_UNIT_DICT = "wms_unit";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private KingdeeHttpClient kingdeeClient;
    @Autowired private IWmsMaterialService materialService;
    @Autowired private ISysDictDataService dictDataService;

    public SyncResult syncAll()
    {
        SyncResult result = new SyncResult();
        long start = System.currentTimeMillis();
        int page = 1, pageSize = 100;
        // 本次同步到的金蝶商品类别，用于维护物料类型字典
        Set<String> categories = new LinkedHashSet<>();
        // 商品类别 id/number → 一级分类名称（子类归父类）
        Map<String, String> categoryRoots = loadCategoryRoots();
        // 单位名称 → 字典值（金蝶单位在 WMS 字典中不存在时会自动新增）
        Map<String, String> unitDict = loadUnitDict();
        int unitDictSizeBefore = unitDict.size();

        while (true)
        {
            log.info("金蝶物料同步 拉取第 {} 页", page);
            Map<String, String> params = new HashMap<>();
            params.put("page", String.valueOf(page));
            params.put("pagesize", String.valueOf(pageSize));

            String raw;
            try { raw = kingdeeClient.getWithRetry(MATERIAL_PATH, params); }
            catch (Exception e) { result.addSkipReason("拉取失败: " + e.getMessage()); break; }

            List<Map<String, Object>> rows;
            int totalPage = 1;
            try
            {
                Map<String, Object> resp = objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {});
                Integer errcode = (Integer) resp.get("errcode");
                if (errcode == null || errcode != 0)
                {
                    result.addSkipReason("接口报错 errcode=" + errcode + ": " + resp.get("description"));
                    break;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                if (data == null) break;
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("rows");
                rows = list;
                Object tp = data.get("total_page");
                totalPage = (tp instanceof Number) ? ((Number) tp).intValue() : 1;
            }
            catch (Exception e) { break; }

            if (rows == null || rows.isEmpty()) break;

            // 先处理当前页数据，再判断是否最后一页（修复：最后一页数据不再丢失）
            for (Map<String, Object> kd : rows)
            {
                try { syncOne(kd, result, categories, categoryRoots, unitDict); }
                catch (Exception e)
                {
                    result.incSkipped();
                    result.addSkipReason(str(kd.get("number")) + ": " + e.getMessage());
                }
            }

            if (page >= totalPage) break;
            page++;
        }

        syncMaterialTypeDict(categories);
        if (unitDict.size() > unitDictSizeBefore)
        {
            log.info("计量单位字典新增 {} 个（金蝶单位）", unitDict.size() - unitDictSizeBefore);
        }
        result.setElapsedMs(System.currentTimeMillis() - start);
        log.info("金蝶物料同步完成: {}", result.getStatistics());
        return result;
    }

    private void syncOne(Map<String, Object> kd, SyncResult result, Set<String> categories,
                         Map<String, String> categoryRoots, Map<String, String> unitDict)
    {
        String kdNumber = str(kd.get("number"));
        String kdName   = nvl(str(kd.get("name")));
        String kdEnable = str(kd.get("enable"));
        // 金蝶商品列表字段：model=规格型号，base_unit_name=计量单位；兼容旧字段名
        String kdSpec   = nvl(pickAny(kd, "model", "spec"));
        String kdUnit   = resolveUnit(nvl(pickAny(kd, "base_unit_name", "unit_name", "unit")), unitDict);
        // 物料类型 = 金蝶商品类别的一级分类（子类归父类）
        String kdType   = resolveCategory(kd, categoryRoots);
        if (kdType != null) categories.add(kdType);
        String kdBatch  = flag(kd.get("is_batch"));
        String kdExpiry = flag(kd.get("is_kf_period"));
        String kdRemark = nvl(str(kd.get("remark")));

        if (kdNumber == null || kdNumber.isEmpty()) throw new IllegalArgumentException("金蝶物料编码为空");

        // 统一以金蝶为准：所有基础信息每次同步都用金蝶商品数据覆盖
        WmsMaterial existed = materialService.selectMaterialByCode(kdNumber);
        if (existed != null)
        {
            existed.setMaterialName(kdName);
            existed.setSpecModel(kdSpec);
            existed.setUnit(kdUnit);
            if (kdType != null) existed.setMaterialType(kdType);
            existed.setIsBatchManage(kdBatch);
            existed.setIsExpiryManage(kdExpiry);
            existed.setStatus(isDisabled(kdEnable) ? "1" : "0");
            existed.setRemark(kdRemark);
            materialService.updateMaterial(existed);
            result.incUpdated();
            result.addSample(kdNumber, kdName, "更新", null);
        }
        else
        {
            WmsMaterial m = new WmsMaterial();
            m.setMaterialCode(kdNumber);
            m.setMaterialName(kdName);
            m.setSpecModel(kdSpec);
            m.setUnit(kdUnit);
            m.setMaterialType(kdType != null ? kdType : "其他");
            m.setIsBatchManage(kdBatch);
            m.setIsExpiryManage(kdExpiry);
            m.setStatus(isDisabled(kdEnable) ? "1" : "0");
            m.setRemark(kdRemark);
            m.setDelFlag("0");
            materialService.insertMaterial(m);
            result.incInserted();
            result.addSample(kdNumber, kdName, "新增", null);
        }
        result.incTotal();
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }

    private static String nvl(String s) { return s == null ? "" : s; }

    /** 只有明确为 "0" 或 "false" 时才视为停用，其余情况（含 null）默认启用 */
    private static boolean isDisabled(String enable)
    {
        return "0".equals(enable) || "false".equalsIgnoreCase(enable);
    }

    /** 金蝶布尔字段（1/0、true/false、是/否）→ 0/1，缺失按 0 处理 */
    private static String flag(Object v)
    {
        if (v == null) return "0";
        String s = String.valueOf(v).trim();
        return ("1".equals(s) || "true".equalsIgnoreCase(s) || "是".equals(s)) ? "1" : "0";
    }

    /** 按优先级取第一个非空字段（金蝶商品字段在不同接口版本中命名有差异） */
    private static String pickAny(Map<String, Object> m, String... keys)
    {
        for (String k : keys)
        {
            Object v = m.get(k);
            if (v != null && !String.valueOf(v).isEmpty()) return String.valueOf(v);
        }
        return null;
    }

    /**
     * 拉取金蝶商品类别树，计算每个类别对应的「一级分类」名称。
     * 返回 key（类别 id 与 number 均作为 key）→ 一级分类名称。
     */
    private Map<String, String> loadCategoryRoots()
    {
        Map<String, String> keyToParent = new HashMap<>();
        Map<String, String> keyToName = new HashMap<>();
        try
        {
            int page = 1, totalPage = 1;
            while (page <= totalPage && page <= 100)
            {
                Map<String, String> params = new HashMap<>();
                params.put("page", String.valueOf(page));
                params.put("pagesize", "1000");
                String raw = kingdeeClient.getWithRetry(MATERIAL_GROUP_PATH, params);
                Map<String, Object> resp = objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {});
                Integer errcode = (Integer) resp.get("errcode");
                if (errcode == null || errcode != 0)
                {
                    log.warn("金蝶商品类别接口报错 errcode={}: {}", errcode, resp.get("description"));
                    break;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                if (data == null) break;
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rows = (List<Map<String, Object>>) data.get("rows");
                if (rows == null || rows.isEmpty()) break;
                for (Map<String, Object> g : rows)
                {
                    String id = str(g.get("id"));
                    String number = str(g.get("number"));
                    String name = str(g.get("name"));
                    String parentId = str(g.get("parent_id"));
                    if (id != null)
                    {
                        keyToParent.put(id, parentId);
                        keyToName.put(id, name);
                    }
                    if (number != null)
                    {
                        keyToParent.put(number, parentId);
                        keyToName.put(number, name);
                    }
                }
                Object tp = data.get("total_page");
                totalPage = (tp instanceof Number) ? ((Number) tp).intValue() : 1;
                page++;
            }
        }
        catch (Exception e)
        {
            log.warn("拉取金蝶商品类别失败，物料类型将使用商品直接上级类别: {}", e.getMessage());
        }

        Map<String, String> keyToRoot = new HashMap<>();
        for (String key : keyToName.keySet())
        {
            String cur = key;
            for (int i = 0; i < 20; i++)
            {
                String parent = keyToParent.get(cur);
                if (parent == null || parent.isEmpty() || !keyToName.containsKey(parent)) break;
                cur = parent;
            }
            keyToRoot.put(key, keyToName.get(cur));
        }
        return keyToRoot;
    }

    /** 加载计量单位字典：单位名称 → 字典值 */
    private Map<String, String> loadUnitDict()
    {
        Map<String, String> map = new HashMap<>();
        try
        {
            List<SysDictData> list = dictDataService.selectDictDataByType(MATERIAL_UNIT_DICT);
            for (SysDictData d : list)
            {
                if (d.getDictLabel() != null && d.getDictValue() != null)
                {
                    map.put(d.getDictLabel().trim(), d.getDictValue());
                }
            }
        }
        catch (Exception e)
        {
            log.warn("加载计量单位字典失败: {}", e.getMessage());
        }
        return map;
    }

    /**
     * 金蝶单位名称与 WMS 单位字典对应：
     * 已存在同名单位则复用其字典值，不存在则新增字典项（label=value=单位名称）。
     */
    private String resolveUnit(String unitName, Map<String, String> unitDict)
    {
        if (unitName == null || unitName.trim().isEmpty()) return "";
        String name = unitName.trim();
        String value = unitDict.get(name);
        if (value != null) return value;

        SysDictData d = new SysDictData();
        d.setDictSort((long) (unitDict.size() + 1));
        d.setDictLabel(name);
        d.setDictValue(name);
        d.setDictType(MATERIAL_UNIT_DICT);
        d.setCssClass("");
        d.setListClass("default");
        d.setIsDefault("N");
        d.setStatus("0");
        dictDataService.insertDictData(d);
        unitDict.put(name, name);
        log.info("新增计量单位字典项：{}", name);
        return name;
    }

    /**
     * 金蝶商品类别名称：子类别归到一级分类，只保留外层分类。
     */
    private static String resolveCategory(Map<String, Object> kd, Map<String, String> categoryRoots)
    {
        String parentId = str(pickAny(kd, "parent_id", "group_id", "category_id", "material_group_id"));
        if (parentId != null)
        {
            String root = categoryRoots.get(parentId);
            if (root != null) return root;
        }
        String parentNumber = str(kd.get("parent_number"));
        if (parentNumber != null)
        {
            String root = categoryRoots.get(parentNumber);
            if (root != null) return root;
        }
        // 兜底：未取到类别 id 时使用商品直接上级类别名称
        String category = pickAny(kd, "parent_name", "category_name", "group_name");
        if (category == null) return null;
        category = category.trim();
        return category.isEmpty() ? null : category;
    }

    /**
     * 物料类型字典以金蝶一级分类为准：
     * 1. 存量物料的旧编码（0-5）转换为类别名称，避免字典删除后页面显示编码；
     * 2. 删除不在本次金蝶一级分类中的字典项（旧 0-5、以及子类别）；
     * 3. 本次同步到的一级分类自动补进字典（label=value=分类名称），前端下拉立即可用。
     */
    private void syncMaterialTypeDict(Set<String> categories)
    {
        if (categories.isEmpty())
        {
            log.warn("金蝶物料同步未返回商品类别，跳过物料类型字典维护");
            return;
        }
        try
        {
            List<SysDictData> existing = dictDataService.selectDictDataByType(MATERIAL_TYPE_DICT);
            Set<String> existingValues = new LinkedHashSet<>();
            java.util.List<Long> obsoleteIds = new java.util.ArrayList<>();
            boolean hasLegacy = false;
            int maxSort = 0;
            for (SysDictData d : existing)
            {
                existingValues.add(d.getDictValue());
                if (d.getDictSort() != null && d.getDictSort() > maxSort) maxSort = d.getDictSort().intValue();
                if (!categories.contains(d.getDictValue()) || LEGACY_TYPE_NAMES.containsKey(d.getDictValue()))
                {
                    obsoleteIds.add(d.getDictCode());
                    if (LEGACY_TYPE_NAMES.containsKey(d.getDictValue())) hasLegacy = true;
                }
            }
            if (hasLegacy)
            {
                // 先把存量物料类型从 0-5 转成对应名称，再移除旧字典项
                materialService.migrateLegacyMaterialType();
            }
            if (!obsoleteIds.isEmpty())
            {
                dictDataService.deleteDictDataByIds(obsoleteIds.toArray(new Long[0]));
                log.info("已移除不在金蝶一级分类中的物料类型字典项 {} 个", obsoleteIds.size());
            }
            int sort = maxSort;
            int added = 0;
            for (String category : categories)
            {
                if (existingValues.contains(category)) continue;
                SysDictData d = new SysDictData();
                d.setDictSort((long) ++sort);
                d.setDictLabel(category);
                d.setDictValue(category);
                d.setDictType(MATERIAL_TYPE_DICT);
                d.setCssClass("");
                d.setListClass("default");
                d.setIsDefault("N");
                d.setStatus("0");
                dictDataService.insertDictData(d);
                added++;
            }
            log.info("物料类型字典同步完成：金蝶类别 {} 个，新增 {} 个", categories.size(), added);
        }
        catch (Exception e)
        {
            log.warn("维护物料类型字典失败: {}", e.getMessage());
        }
    }

    /** 旧物料类型编码 → 名称（仅用于存量数据迁移） */
    private static final Map<String, String> LEGACY_TYPE_NAMES = new HashMap<>();
    static
    {
        LEGACY_TYPE_NAMES.put("0", "原材料");
        LEGACY_TYPE_NAMES.put("1", "半成品");
        LEGACY_TYPE_NAMES.put("2", "成品");
        LEGACY_TYPE_NAMES.put("3", "辅料");
        LEGACY_TYPE_NAMES.put("4", "服务");
        LEGACY_TYPE_NAMES.put("5", "工程");
    }
}

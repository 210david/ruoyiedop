package com.ruoyi.wms.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.wms.domain.WmsInventory;
import com.ruoyi.wms.domain.vo.WmsInventoryCostVO;
import com.ruoyi.wms.domain.vo.WmsMaterialPriceVO;
import com.ruoyi.wms.mapper.WmsInventoryMapper;
import com.ruoyi.wms.support.InventoryCostAvailability;

/**
 * 库存金额回填服务
 *
 * <p>【为什么不落库】财务已确认旁路设计：WMS 只管数量，财务只管金额
 * （见 FinInventoryCost 类注释「不修改 wms_inventory」）。因此金额只在查询时回填，
 * 绝不在 wms_inventory 增加金额列，否则移库/盘点会让两个金额账各自漂移。</p>
 *
 * <p>【三级取数优先级】</p>
 * <ol>
 *   <li><b>cost</b>：财务成本台账 fin_inventory_cost.total_cost —— 权威口径，
 *       不含税、按物料+仓库+库位+批次四维分账</li>
 *   <li><b>estimate</b>：库存数量 × 物料入库加权均价 —— 降级口径，
 *       按物料拉平、含税，用于台账未覆盖（未计价或财务表未部署）的记录</li>
 *   <li><b>fallback</b>：库存数量 × 物料参考成本价 —— 最后兜底，
 *       不含税，仅在既无台账也无入库均价时使用</li>
 * </ol>
 *
 * <p>每行都会回填 amountSource，前端必须据此标注来源，
 * 不允许只显示金额而不说明口径（同一库存可能同时存在三种口径）。</p>
 *
 * @author ruoyi
 */
@Service
public class WmsInventoryAmountService
{
    private static final Logger log = LoggerFactory.getLogger(WmsInventoryAmountService.class);

    /** 金额保留 2 位小数（统一开发规范：金额 DECIMAL(14,2)/2 位） */
    private static final int AMOUNT_SCALE = 2;

    @Autowired
    private WmsInventoryMapper wmsInventoryMapper;

    @Autowired
    private InventoryCostAvailability costAvailability;

    /**
     * 为库存列表回填金额（就地修改传入对象）。
     *
     * <p>任何异常都不向外抛出：金额是增强信息，取数失败应降级而不是让库存查询整体失败。</p>
     *
     * @param list 库存列表
     */
    public void fillAmount(List<WmsInventory> list)
    {
        if (list == null || list.isEmpty())
        {
            return;
        }

        // 1. 成本台账（可用时才查，缺表直接跳过）
        Map<String, WmsInventoryCostVO> costMap = loadCostMap(list);

        // 2. 物料取价依据（估算与兜底都用）
        Map<Long, WmsMaterialPriceVO> priceMap = loadPriceMap(list);

        for (WmsInventory inv : list)
        {
            BigDecimal qty = inv.getQty() == null ? BigDecimal.ZERO : inv.getQty();
            if (qty.compareTo(BigDecimal.ZERO) <= 0)
            {
                // 零/负库存金额为 0，且不标注来源：没有数量就不存在"成本"或"估算"之分
                inv.setAmount(BigDecimal.ZERO.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP));
                inv.setUnitCost(null);
                inv.setAmountSource(null);
                continue;
            }

            // 优先：成本台账
            WmsInventoryCostVO cost = costMap.get(WmsInventoryCostVO.dimensionKey(
                    inv.getMaterialId(), inv.getWarehouseId(), inv.getLocationId(), inv.getBatchNo()));
            if (cost != null && cost.getTotalCost() != null)
            {
                inv.setAmount(cost.getTotalCost().setScale(AMOUNT_SCALE, RoundingMode.HALF_UP));
                inv.setUnitCost(cost.getUnitCost());
                inv.setAmountSource(WmsInventory.AMOUNT_SOURCE_COST);
                continue;
            }

            WmsMaterialPriceVO price = priceMap.get(inv.getMaterialId());

            // 次选：入库加权均价（含税）
            if (price != null && price.getAvgPrice() != null
                    && price.getAvgPrice().compareTo(BigDecimal.ZERO) > 0)
            {
                inv.setUnitCost(price.getAvgPrice());
                inv.setAmount(qty.multiply(price.getAvgPrice()).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP));
                inv.setAmountSource(WmsInventory.AMOUNT_SOURCE_ESTIMATE);
                continue;
            }

            // 兜底：物料参考成本价（不含税）
            if (price != null && price.getCostPrice() != null
                    && price.getCostPrice().compareTo(BigDecimal.ZERO) > 0)
            {
                inv.setUnitCost(price.getCostPrice());
                inv.setAmount(qty.multiply(price.getCostPrice()).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP));
                inv.setAmountSource(WmsInventory.AMOUNT_SOURCE_FALLBACK);
                continue;
            }

            // 三项都取不到：明确标记为无金额，不臆造 0 成本
            inv.setAmount(null);
            inv.setUnitCost(null);
            inv.setAmountSource(null);
        }
    }

    /**
     * 加载成本台账（四维键 → 台账记录）。表不存在时返回空 Map，静默降级。
     */
    private Map<String, WmsInventoryCostVO> loadCostMap(List<WmsInventory> list)
    {
        Map<String, WmsInventoryCostVO> map = new HashMap<>();
        if (!costAvailability.isCostTableAvailable())
        {
            return map;
        }
        try
        {
            // 分批，规避 IN/OR 条件过长与单次返回体量过大
            final int batchSize = 200;
            for (int i = 0; i < list.size(); i += batchSize)
            {
                List<WmsInventory> batch = list.subList(i, Math.min(i + batchSize, list.size()));
                List<WmsInventoryCostVO> rows = wmsInventoryMapper.selectCostByDimension(batch);
                if (rows == null)
                {
                    continue;
                }
                for (WmsInventoryCostVO row : rows)
                {
                    map.put(row.dimensionKey(), row);
                }
            }
        }
        catch (Exception e)
        {
            // 例如财务表刚被下线、权限变更等：降级为估算口径，不影响库存查询
            log.warn("取存货成本台账失败，库存金额降级为估算口径：{}", e.getMessage());
            map.clear();
        }
        return map;
    }

    /**
     * 加载物料取价依据。去重后批量查询，避免同一物料重复查询。
     */
    private Map<Long, WmsMaterialPriceVO> loadPriceMap(List<WmsInventory> list)
    {
        Map<Long, WmsMaterialPriceVO> map = new HashMap<>();
        List<Long> materialIds = new ArrayList<>();
        for (WmsInventory inv : list)
        {
            if (inv.getMaterialId() != null && !materialIds.contains(inv.getMaterialId()))
            {
                materialIds.add(inv.getMaterialId());
            }
        }
        if (materialIds.isEmpty())
        {
            return map;
        }
        try
        {
            final int batchSize = 500;
            for (int i = 0; i < materialIds.size(); i += batchSize)
            {
                List<Long> batch = materialIds.subList(i, Math.min(i + batchSize, materialIds.size()));
                List<WmsMaterialPriceVO> rows = wmsInventoryMapper.selectMaterialPrice(batch);
                if (rows == null)
                {
                    continue;
                }
                for (WmsMaterialPriceVO row : rows)
                {
                    map.put(row.getMaterialId(), row);
                }
            }
        }
        catch (Exception e)
        {
            log.warn("取物料取价依据失败，库存金额将显示为空：{}", e.getMessage());
            map.clear();
        }
        return map;
    }
}
